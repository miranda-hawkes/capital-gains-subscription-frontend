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

import com.google.inject.{Inject, Singleton}
import config.{AppConfig}
import uk.gov.hmrc.play.frontend.auth.{CompositePageVisibilityPredicate, PageVisibilityPredicate}

@Singleton
class CompositePredicate @Inject()(applicationConfig: AppConfig)(postSignInRedirectUrl: String,
                             notAuthorisedRedirectUrl: String,
                             ivUpliftUrl: String,
                             twoFactorUrl: String
                            ) extends CompositePageVisibilityPredicate  {
  override def children: Seq[PageVisibilityPredicate] = Seq (
    new IVUpliftPredicate(new URI(ivUpliftUrl)),
    new LoginPredicate(new URI(applicationConfig.governmentGateway)),
    new TwoFAPredicate(new URI(twoFactorUrl)),
    new NINOPredicate(new URI(ivUpliftUrl))
  )

  private val ivUpliftURI: URI =
    new URI(s"${ivUpliftUrl}?origin=CGT&"  +
      s"completionURL=$postSignInRedirectUrl&" +
      s"failureURL=$notAuthorisedRedirectUrl" +
      s"&confidenceLevel=200")

  private val twoFactorURI: URI =
    new URI(s"${twoFactorUrl}?" +
      s"continute=$postSignInRedirectUrl&" +
    s"failureURL=$notAuthorisedRedirectUrl")
}