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

import javax.inject.{Inject, Singleton}

import auth.AuthorisedActions
import common.Constants.ErrorMessages._
import common.Keys.KeystoreKeys
import config.AppConfig
import connectors.KeystoreConnector
import forms.YesNoForm
import models.{CompanyAddressModel, YesNoModel}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

@Singleton
class CorrespondenceAddressConfirmController @Inject()(appConfig: AppConfig,
                                                       val messagesApi: MessagesApi,
                                                       sessionService: KeystoreConnector,
                                                       actions: AuthorisedActions,
                                                       form: YesNoForm)
  extends FrontendController with I18nSupport {

  val correspondenceAddressConfirm: Action[AnyContent] =
    actions.authorisedNonResidentOrganisationAction() { implicit user =>
      implicit request => {

        for {
          registrationDetails <- sessionService.fetchAndGetBusinessData()
          existingAnswer <- sessionService.fetchAndGetFormData[YesNoModel](KeystoreKeys.useRegistrationAddressKey)
        } yield {
          (existingAnswer, registrationDetails) match {

            case (_, None) =>
              Logger.warn(businessDataNotFound)
              throw new Exception(businessDataNotFound)

            case (None, Some(details)) =>
              val emptyForm = new YesNoForm(messagesApi).yesNoForm
              Ok(views.html.useRegisteredAddress(appConfig, emptyForm, details.businessAddress))

            case (Some(data), Some(details)) =>
              val populatedForm = new YesNoForm(messagesApi).yesNoForm.fill(data)
              Ok(views.html.useRegisteredAddress(appConfig, populatedForm, details.businessAddress))
          }
        }
      }
    }

  val submitCorrespondenceAddressConfirm: Action[AnyContent] =
    actions.authorisedNonResidentOrganisationAction() { implicit user =>
      implicit request => {

        def processRequest(address: CompanyAddressModel): Future[Result] = form.validate(
          errors => Future.successful(BadRequest(views.html.useRegisteredAddress(appConfig, errors, address))),
          success => {
            for {
              _ <- sessionService.saveFormData(KeystoreKeys.useRegistrationAddressKey, success)
              _ <- if (success.response) sessionService.saveFormData(KeystoreKeys.correspondenceAddressKey, address) else Future(false)
            } yield {
              if (success.response) Redirect(controllers.routes.ContactDetailsController.contactDetails().url)
              else Redirect(controllers.routes.EnterCorrespondenceAddressController.enterCorrespondenceAddress().url)
            }
          }
        )

        sessionService.fetchAndGetBusinessData().flatMap {
          case None =>
            Logger.warn(businessDataNotFound)
            Future.failed(new Exception(businessDataNotFound))

          case Some(details) => processRequest(details.businessAddress)
        }
      }
    }
}
