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

import javax.inject.{Inject, Singleton}

import config.{AppConfig, WSHttp}
import models.{CompanySubmissionModel, SubscriptionReference, UserFactsModel}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SubscriptionConnector @Inject()(http: WSHttp, appConfig: AppConfig) extends ServicesConfig {

  lazy val serviceUrl: String = appConfig.subscription
  val companyUrl: String = "/subscribe/company"
  val subscriptionResidentUrl: String = "subscribe/resident/individual"
  val subscriptionNonResidentUrl: String = "subscribe/non-resident/individual"
  val subscriptionNonResidentNinoUrl: String = "subscribe/non-resident/individual-nino"

  def getSubscriptionResponse(nino: String)(implicit hc: HeaderCarrier): Future[Option[SubscriptionReference]] = {
    val postUrl = s"""$serviceUrl/$subscriptionResidentUrl/?nino=$nino"""
    http.POST[JsValue, HttpResponse](postUrl, Json.toJson("")).map {
      response =>
        response.status match {
          case OK =>
            Some(response.json.as[SubscriptionReference])
          case _ => None
        }
    }
  }

  def getSubscriptionNonResidentNinoResponse(nino: String)(implicit hc: HeaderCarrier): Future[Option[SubscriptionReference]] = {
    val postUrl =s"""$serviceUrl/$subscriptionNonResidentNinoUrl/?nino=$nino"""
    http.POST[JsValue, HttpResponse](postUrl, Json.toJson("")).map{
      response =>
        response.status match {
          case OK =>
            Some(response.json.as[SubscriptionReference])
          case _ => None
        }
    }
  }

  def getSubscriptionResponseGhost(userFacts: UserFactsModel)(implicit hc: HeaderCarrier): Future[Option[SubscriptionReference]] = {
    val postUrl =s"""$serviceUrl/$subscriptionNonResidentUrl/"""
    http.POST[JsValue, HttpResponse](postUrl, Json.toJson(userFacts)).map{
      response =>
        response.status match {
          case OK =>
            Some(response.json.as[SubscriptionReference])
          case _ => None
        }
    }
  }

  def getSubscriptionResponseCompany(companySubmissionModel: CompanySubmissionModel)(implicit hc: HeaderCarrier): Future[Option[SubscriptionReference]] = {

    val postUrl = s"""$serviceUrl$companyUrl"""
    http.POST[JsValue, HttpResponse](postUrl, Json.toJson(companySubmissionModel)).map {
      response =>
        response.status match {
          case OK =>
            Some(response.json.as[SubscriptionReference])
          case _ => None
        }
    }
  }

}
