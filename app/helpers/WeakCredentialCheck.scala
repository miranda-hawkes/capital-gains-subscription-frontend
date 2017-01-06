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

package helpers

import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.domain.CredentialStrength

import scala.concurrent.Future

object WeakCredentialCheck extends WeakCredentialCheck

trait WeakCredentialCheck {
  def weakCredentialCheck(authContext: AuthContext): Future[Boolean] = authContext.user.credentialStrength match {
    case CredentialStrength.None => Future.successful(false)
    case _ => Future.successful(true)
  }
}