/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package predicates

import java.net.URI

import javax.inject.Inject
import helpers.EnrolmentToCGTCheck
import models.Enrolment
import play.api.mvc.Results._
import play.api.mvc.{AnyContent, Request}
import services.AuthorisationService
import uk.gov.hmrc.play.frontend.auth._
import uk.gov.hmrc.play.http.HeaderCarrier
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class EnrolmentPredicate @Inject()(enrolmentURI: URI, authorisationService: AuthorisationService) extends PageVisibilityPredicate {
  def apply(authContext: AuthContext, request: Request[AnyContent]): Future[PageVisibilityResult] = {

    implicit def hc(implicit request: Request[_]): HeaderCarrier = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))

    def checkEnrolments(enrolments: Option[Seq[Enrolment]]): Future[Boolean] = {
      enrolments match {
        case Some(data) => EnrolmentToCGTCheck.checkEnrolments(data)
        case None => Future.successful(false)
      }
    }

    def getPageVisibility(enrolled: Boolean): Future[PageVisibilityResult] = {
      if (enrolled) Future.successful(PageIsVisible)
      else Future.successful(PageBlocked(needsEnrolment))
    }

    for {
      enrolments <- authorisationService.getEnrolments(hc(request))
      enrolled <- checkEnrolments(enrolments)
      display <- getPageVisibility(enrolled)
    } yield display
  }

  private val needsEnrolment = Future.successful(Redirect(enrolmentURI.toString))
}
