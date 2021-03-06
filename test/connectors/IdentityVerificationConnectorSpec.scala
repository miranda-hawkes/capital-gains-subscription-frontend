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

package connectors

import common.IdentityVerificationResult
import config.WSHttp
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.Status._
import play.api.libs.json.JsString
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class IdentityVerificationConnectorSpec extends UnitSpec with MockitoSugar {

  implicit val hc:HeaderCarrier = mock[HeaderCarrier]

  def mockConnector(response: String): IdentityVerificationConnector = {
    val mockHttp = mock[WSHttp]
    val result = JsString(response)

    when(mockHttp.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(HttpResponse(OK, Some(result))))

    val connector = new IdentityVerificationConnector(mockHttp) {
      override lazy val serviceUrl: String = ""
    }

    connector
  }

  "Calling identity verification response" should {

    "return a valid response when returning a Success" in {
      val target = mockConnector("Success")
      val result = target.identityVerificationResponse("")

      await(result) shouldBe IdentityVerificationResult.Success
    }

    "return a valid response when returning an Incomplete response" in {
      val target = mockConnector("Incomplete")
      val result = target.identityVerificationResponse("")

      await(result) shouldBe IdentityVerificationResult.Incomplete
    }

    "return a valid response when returning a Failed Matching response" in {
      val target = mockConnector("FailedMatching")
      val result = target.identityVerificationResponse("")

      await(result) shouldBe IdentityVerificationResult.FailedMatching
    }

    "return a valid response when returning an Insufficient Evidence response" in {
      val target = mockConnector("InsufficientEvidence")
      val result = target.identityVerificationResponse("")

      await(result) shouldBe IdentityVerificationResult.InsufficientEvidence
    }

    "return a valid response when returning a Locked Out response" in {
      val target = mockConnector("LockedOut")
      val result = target.identityVerificationResponse("")

      await(result) shouldBe IdentityVerificationResult.LockedOut
    }

    "return a valid response when returning an User Aborted response" in {
      val target = mockConnector("UserAborted")
      val result = target.identityVerificationResponse("")

      await(result) shouldBe IdentityVerificationResult.UserAborted
    }

    "return a valid response when returning a Timeout response" in {
      val target = mockConnector("Timeout")
      val result = target.identityVerificationResponse("")

      await(result) shouldBe IdentityVerificationResult.Timeout
    }

    "return a valid response when returning a Technical Issue response" in {
      val target = mockConnector("TechnicalIssue")
      val result = target.identityVerificationResponse("")

      await(result) shouldBe IdentityVerificationResult.TechnicalIssue
    }

    "return a valid response when returning a Precondition Failed response" in {
      val target = mockConnector("PreconditionFailed")
      val result = target.identityVerificationResponse("")

      await(result) shouldBe IdentityVerificationResult.PreconditionFailed
    }

    "return a valid response when returning a Failed IV response" in {
      val target = mockConnector("FailedIV")
      val result = target.identityVerificationResponse("")

      await(result) shouldBe IdentityVerificationResult.FailedIV
    }

    "return an unknown response when provided with a non recognised response" in {
      val target = mockConnector("test")
      val result = target.identityVerificationResponse("")

      await(result) shouldBe IdentityVerificationResult.UnknownOutcome
    }
  }
}
