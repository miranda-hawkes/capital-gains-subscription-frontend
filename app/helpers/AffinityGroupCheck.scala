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

import scala.concurrent.Future
import common.Constants.AffinityGroup._

object AffinityGroupCheck extends AffinityGroupCheck

trait AffinityGroupCheck {

  def affinityGroupCheckIndividual(affinityGroup: String): Future[Boolean] = Future.successful(affinityGroup == Individual)

  def affinityGroupCheckCompany(affinityGroup: String): Future[Boolean] = Future.successful(affinityGroup == Organisation)

  def affinityGroupCheckAgent(affinityGroup: String): Future[Boolean] = Future.successful(affinityGroup == Agent)

}
