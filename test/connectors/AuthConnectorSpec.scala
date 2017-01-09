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

import builders.TestUserBuilder
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.http.{HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.http.Status._

import scala.concurrent.Future


class AuthConnectorSpec extends UnitSpec with MockitoSugar with WithFakeApplication{

  lazy val mockHttp = mock[HttpGet with HttpPost]

  object TestAuthConnector extends AuthConnector {
    override def serviceUrl: String = "localhost"
    override def authorityUri: String = "auth/authority"
    override def http: HttpGet with HttpPost = mockHttp
  }

  val nino = TestUserBuilder.createRandomNino

  def affinityResponse = (key: String, nino: String) => Json.parse(
    s"""{
        "uri":"/auth/oid/57e915480f00000f006d915b",
        "confidenceLevel":"200",
        "credentialStrength":"Strong",
        "userDetailsLink":"http://localhost:9978/user-details/id/57e915482200005f00b0b55e",
        "legacyOid":"57e915480f00000f006d915b",
        "new-session":"/auth/oid/57e915480f00000f006d915b/session",
        "ids":"/auth/oid/57e915480f00000f006d915b/ids",
        "credentials":
          {"gatewayId":"872334723473244"},
        "accounts":
          {"paye":
            {"nino": "$nino"}
          },
        "lastUpdated":"2016-09-26T12:32:08.734Z",
        "loggedInAt":"2016-09-26T12:32:08.734Z",
        "levelOfAssurance":"1",
        "enrolments":"/auth/oid/57e915480f00000f006d915b/enrolments",
        "affinityGroup":"$key",
        "correlationId":"9da194b9490024bae213f18d5b34fedf41f2c3236b434975333a7bdb0fe548ec",
        "credId":"872334723473244"
        }"""
  )

  "AuthConnector .getAuthResponse" should {

    "return a JSON result with a valid request" in {
      when(mockHttp.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK, Some(affinityResponse("Individual", nino)))))
        await(TestAuthConnector.getAuthResponse()).get shouldBe a[JsValue]
    }

    "return a None with an invalid request" in {
      when(mockHttp.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, Some(affinityResponse("Individual", nino)))))
      await(TestAuthConnector.getAuthResponse()) shouldBe None
    }
  }



















  "AuthService .getConfidenceLevel" should {

  }

  "AuthService. getCredentialStrength" should {

  }
}
