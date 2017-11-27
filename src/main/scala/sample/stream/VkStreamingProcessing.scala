package sample.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.ServiceActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.streaming.clients.actors.StreamingActor
import com.vk.api.sdk.streaming.clients.{StreamingEventHandler, VkStreamingApiClient}
import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage
import com.vk.api.sdk.streaming.objects.responses.StreamingGetRulesResponse

import scala.collection.JavaConversions._

object VkStreamingProcessing {
  val OK_CODE = 100

  val myConfig = new MyConfig()
  val appId: String = myConfig.envOrElseConfig("appId")
  val accessToken: String = myConfig.envOrElseConfig("accessToken")
  val clientSecret: String = myConfig.envOrElseConfig("clientSecret")

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("Sys")
    implicit val materializer = ActorMaterializer()

    val transportClient = new HttpTransportClient
    val vkClient = new VkApiClient(transportClient)
    val streamingClient = new VkStreamingApiClient(transportClient)
    val actor = new ServiceActor(appId.toInt, clientSecret, accessToken)
    var getServerUrlResponse = vkClient.streaming.getServerUrl(actor).execute
    val streamingActor = new StreamingActor(getServerUrlResponse.getEndpoint, getServerUrlResponse.getKey)

    val tag = "44"
    val value = "привет"
    val rules: StreamingGetRulesResponse = streamingClient.rules.get(streamingActor).execute()
    if (!rules.getRules.toList.map(r => r.getValue).contains(value)) {
      streamingClient.rules.add(streamingActor, tag, value).execute
    }

    val bufferSize = 10
    val overflowStrategy = akka.stream.OverflowStrategy.backpressure
    val queue = Source.queue[StreamingCallbackMessage](bufferSize, overflowStrategy)
      .filter(_.getCode == OK_CODE)
      .map(m => s"queue ${m.getEvent.getText}")
      .to(akka.stream.scaladsl.Sink foreach println)
      .run()

    streamingClient.stream.get(streamingActor, new StreamingEventHandler() {
      override def handle(message: StreamingCallbackMessage): Unit = {
        queue offer message
      }
    }).execute

  }
}
