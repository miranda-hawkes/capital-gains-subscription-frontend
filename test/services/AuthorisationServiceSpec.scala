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

package services

import connectors.AuthorisationConnector
import models.AuthorisationDataModel
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.mockito.ArgumentMatchers
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class AuthorisationServiceSpec extends UnitSpec with MockitoSugar {

  implicit val hc = mock[HeaderCarrier]

  def mockedService(response: Option[AuthorisationDataModel]): AuthorisationService = {

    val mockConnector = mock[AuthorisationConnector]

    when(mockConnector.getAuthResponse()(ArgumentMatchers.any()))
      .thenReturn(Future.successful(response))

    new AuthorisationService(mockConnector)
  }

  "Calling AuthorisationService .getAuthData" should {

    "return an AuthDataModel with a valid request" in {
      val service = mockedService(Some(AuthorisationDataModel(CredentialStrength.Strong, "", ConfidenceLevel.L200, "", Accounts())))
      val result = service.getAuthDataModel(hc)
      await(result) shouldBe Some(AuthorisationDataModel(CredentialStrength.Strong, "", ConfidenceLevel.L200, "", Accounts()))
    }

    "return a None with an invalid request" in {
      val service = mockedService(None)
      val result = service.getAuthDataModel(hc)
      await(result) shouldBe None
    }
  }

  "Calling .getAffinityGroup" should {

    "Return an affinity group when a valid request is sent" in {
      val service = mockedService(Some(AuthorisationDataModel(CredentialStrength.Strong, "DummyAffinity", ConfidenceLevel.L200, "", Accounts())))
      val result = service.getAffinityGroup(hc)
      await(result) shouldBe Some("DummyAffinity")
    }

    "return a None with an invalid request" in {
      val service = mockedService(None)
      val result = service.getAffinityGroup(hc)
      await(result) shouldBe None
    }
  }
}
