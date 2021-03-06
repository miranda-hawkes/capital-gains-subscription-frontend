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

import auth.{AuthenticatedIndividualAction, AuthorisedActions, CgtIndividual}
import common.Constants.AffinityGroup
import common.Keys
import config.WSHttp
import data.MessageLookup
import helpers.LogicHelpers
import models.{AuthorisationDataModel, Enrolment, Identifier, SubscriptionReference}
import org.jsoup.Jsoup
import connectors.{AuthorisationConnector, KeystoreConnector, SubscriptionConnector}
import data.TestUserBuilder
import models._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{AuthorisationService, SubscriptionService}
import traits.ControllerTestSpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, ConfidenceLevel, CredentialStrength, PayeAccount}

import scala.concurrent.Future

class ResidentIndividualSubscriptionControllerSpec extends ControllerTestSpec {

  val unauthorisedLoginUri = "some-url"

  def createMockActions(valid: Boolean = false, authContext: AuthContext = TestUserBuilder.userWithNINO): AuthorisedActions = {

    val mockActions = mock[AuthorisedActions]

    if (valid) {
      when(mockActions.authorisedResidentIndividualAction(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenAnswer(new Answer[Action[AnyContent]] {

          override def answer(invocation: InvocationOnMock): Action[AnyContent] = {
            val action = invocation.getArgument[AuthenticatedIndividualAction](1)
            val individual = CgtIndividual(authContext)
            Action.async(action(individual))
          }
        })
    }
    else {
      when(mockActions.authorisedResidentIndividualAction(ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Action.async(Results.Redirect(unauthorisedLoginUri)))
    }

    mockActions
  }

  def createMockSubscriptionService(response: Option[String]): SubscriptionService = {
    implicit val mockHttp = mock[WSHttp]

    val mockConnector = mock[SubscriptionConnector]

    when(mockConnector.getSubscriptionResponse(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(response.map(SubscriptionReference(_))))

    when(mockConnector.getSubscriptionResponseGhost(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(response.map(SubscriptionReference(_))))

    new SubscriptionService(mockConnector)
  }

  def createMockAuthorisationService(enrolmentsResponse: Option[Seq[Enrolment]], authResponse: Option[AuthorisationDataModel]): AuthorisationService = {
    implicit val mockHttp = mock[WSHttp]

    val mockConnector = mock[AuthorisationConnector]

    when(mockConnector.getAuthResponse()(ArgumentMatchers.any())).thenReturn(Future.successful(authResponse))

    when(mockConnector.getEnrolmentsResponse(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(enrolmentsResponse)

    new AuthorisationService(mockConnector)
  }

  def mockLogicHelper(valid: Boolean): LogicHelpers = {
    val helper = mock[LogicHelpers]

    when(helper.bindAndValidateCallbackUrl(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(valid))

    helper
  }

  def mockKeystore(keystoreResponse: Option[RedirectModel] = None): KeystoreConnector = {
    val mockKeystoreConnector = mock[KeystoreConnector]
    when(mockKeystoreConnector.fetchAndGetFormData[RedirectModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(keystoreResponse))

    mockKeystoreConnector
  }

  "Calling .residentIndividualSubscription" when {

    val nino = TestUserBuilder.createRandomNino

    val authorisationDataModelPass = AuthorisationDataModel(CredentialStrength.Strong, AffinityGroup.Individual,
      ConfidenceLevel.L500, "example.com", Accounts(paye = Some(PayeAccount(s"/paye/$nino", Nino(nino)))))

    val authorisationDataModelFail = AuthorisationDataModel(CredentialStrength.None, AffinityGroup.Organisation,
      ConfidenceLevel.L50, "example.com", Accounts(paye = Some(PayeAccount(s"/paye/$nino", Nino(nino)))))

    "provided with an invalid callback URL" should {
      val fakeRequest = FakeRequest("GET", "/")
      lazy val actions = createMockActions(valid = true)
      val mockSubscriptionService = createMockSubscriptionService(Some("eee"))
      val enrolments = Seq(Enrolment("otherKey", Seq(), ""), Enrolment("key", Seq(), ""))
      lazy val authorisationService = createMockAuthorisationService(Some(enrolments), Some(authorisationDataModelPass))

      lazy val target = new ResidentIndividualSubscriptionController(actions, mockConfig, mockSubscriptionService,
        authorisationService, mockLogicHelper(false), messagesApi)

      lazy val result = target.residentIndividualSubscription("http://www.google.com")(fakeRequest)
      lazy val body = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "redirect to the CGT confirmation screen" in {
        body.title() shouldBe MessageLookup.Common.badRequest
      }
    }

    "provided with a valid user who has a nino and a subscription service that has a CGT reference" should {

      val fakeRequest = FakeRequest("GET", "/")
      lazy val actions = createMockActions(valid = true)
      val mockSubscriptionService = createMockSubscriptionService(Some("eee"))
      val enrolments = Seq(Enrolment("otherKey", Seq(), ""), Enrolment("key", Seq(), ""))
      lazy val authorisationService = createMockAuthorisationService(Some(enrolments), Some(authorisationDataModelPass))

      lazy val target = new ResidentIndividualSubscriptionController(actions, mockConfig, mockSubscriptionService,
        authorisationService, mockLogicHelper(true), messagesApi)

      lazy val result = target.residentIndividualSubscription("/test/route")(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the CGT confirmation screen" in {
        redirectLocation(result).get.toString shouldBe Some(routes.CGTSubscriptionController.confirmationOfSubscriptionResidentIndv("eee")).get.url
      }
    }

    "provided with a valid user who has a nino, a preexisting CGT enrolment and a callback-url" should {
      val fakeRequest = FakeRequest("GET", "/")
      lazy val actions = createMockActions(valid = true)
      val mockSubscriptionService = createMockSubscriptionService(Some("eee"))
      val enrolments = Seq(Enrolment(Keys.cgtIndividualEnrolmentKey, Seq(Identifier("test", "test")), ""), Enrolment("key", Seq(), ""))

      lazy val authorisationService = createMockAuthorisationService(Some(enrolments), Some(authorisationDataModelPass))

      lazy val target = new ResidentIndividualSubscriptionController(actions, mockConfig, mockSubscriptionService,
        authorisationService, mockLogicHelper(true), messagesApi)

      lazy val result = target.residentIndividualSubscription("/test/route")(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the I-form screen" in {
        redirectLocation(result).get.toString shouldBe "/test/route"
      }
    }

    "provided with a valid user but the subscription fails" should {
      val fakeRequest = FakeRequest("GET", "/")
      lazy val actions = createMockActions(valid = true)
      val mockSubscriptionService = createMockSubscriptionService(None)
      val enrolments = Seq(Enrolment("otherKey", Seq(), ""), Enrolment("key", Seq(), ""))
      lazy val authorisationService = createMockAuthorisationService(Some(enrolments), Some(authorisationDataModelPass))

      lazy val target = new ResidentIndividualSubscriptionController(actions, mockConfig, mockSubscriptionService,
        authorisationService, mockLogicHelper(true), messagesApi)
      lazy val result = target.residentIndividualSubscription("/test/route")(fakeRequest)

      "redirect to the callback page" in {
        redirectLocation(result).get.toString shouldBe "/test/route"
      }
    }

    "provided with an invalid user" should {
      val fakeRequest = FakeRequest("GET", "/")
      lazy val actions = createMockActions(valid = false, TestUserBuilder.userWithNINO)
      val mockSubscriptionService = createMockSubscriptionService(None)
      lazy val authorisationService = createMockAuthorisationService(None, Some(authorisationDataModelFail))
      lazy val target = new ResidentIndividualSubscriptionController(actions, mockConfig, mockSubscriptionService,
        authorisationService, mockLogicHelper(true), messagesApi)
      lazy val result = target.residentIndividualSubscription("/test/route")(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the mock gg-login page" in {
        redirectLocation(result) shouldBe Some(unauthorisedLoginUri)
      }
    }
  }
}
