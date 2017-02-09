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

package controllers

import com.google.inject.{Inject, Singleton}
import config.AppConfig
import connectors.KeystoreConnector
import play.api.mvc.Action
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

@Singleton
class CorrespondenceAddressConfirmController @Inject()(appConfig: AppConfig, keystoreConnector: KeystoreConnector) extends FrontendController {

  val correspondenceAddressConfirm = Action.async { implicit request =>
    keystoreConnector.fetchAndGetBusinessData().flatMap { data =>
      println(s"#############$data####################")
      Future.successful(Ok(""))
    }
  }

  val submitCorrespondenceAddressConfirm = TODO

}