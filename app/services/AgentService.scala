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

import javax.inject.Inject

import connectors.{AgentEnrolmentResponse, SubscriptionConnector, SuccessAgentEnrolmentResponse}
import models.AgentSubmissionModel
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class AgentService @Inject()(connector: SubscriptionConnector) {

  def getAgentEnrolmentResponse(agentSubmissionModel: AgentSubmissionModel)(implicit hc: HeaderCarrier): Future[AgentEnrolmentResponse] = {
    connector.enrolAgent(agentSubmissionModel)
  }
}
