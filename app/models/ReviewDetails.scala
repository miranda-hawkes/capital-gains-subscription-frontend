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

package models

import play.api.libs.json.Json

case class Identification(idNumber: String, issuingInstitution: String, issuingCountryCode: String)

object Identification {
  implicit val formats = Json.format[Identification]
}

case class ReviewDetails(businessName: String,
                         businessType: Option[String],
                         businessAddress: CompanyAddressModel,
                         sapNumber: String,
                         safeId: String,
                         isAGroup: Boolean = false,
                         directMatch: Boolean = false,
                         agentReferenceNumber: Option[String],
                         firstName: Option[String] = None,
                         lastName: Option[String] = None,
                         utr: Option[String] = None,
                         identification: Option[Identification] = None,
                         isBusinessDetailsEditable: Boolean = false)

object ReviewDetails {
  implicit val formats = Json.format[ReviewDetails]
}