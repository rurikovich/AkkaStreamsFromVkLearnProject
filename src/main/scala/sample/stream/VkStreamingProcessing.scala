package sample.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.OverflowStrategy.fail
import akka.stream.scaladsl.{Flow, Source}
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.ServiceActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.streaming.clients.actors.StreamingActor
import com.vk.api.sdk.streaming.clients.{StreamingEventHandler, VkStreamingApiClient}
import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage

object VkStreamingProcessing {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("Sys")
    implicit val materializer = ActorMaterializer()

    val transportClient = new HttpTransportClient
    val vkClient = new VkApiClient(transportClient)

    val streamingClient = new VkStreamingApiClient(transportClient)

    val appId = 6273957
    val accessToken = "039164ab039164ab039164abfb03cedf0e00391039164ab598bc5f21d283fc052250a2d"
    val clientSecret = "5rLSNuklraQD6mRjJXPx"
    val actor = new ServiceActor(appId, clientSecret, accessToken)

    //Get streaming actor
    var getServerUrlResponse = vkClient.streaming.getServerUrl(actor).execute
    val streamingActortor = new StreamingActor(getServerUrlResponse.getEndpoint, getServerUrlResponse.getKey)

    val tag = "3"
    val value = "vk"

    try
      streamingClient.rules.add(streamingActortor, tag, value).execute
    catch {
      case e: Exception =>
        e.printStackTrace()
    }


    val vkMessagesSource = Source.actorRef[String](200, fail)

    val sunnySource = vkMessagesSource.filter(!_.isEmpty)

    val ref = Flow[String]
      .to(akka.stream.scaladsl.Sink foreach println)
      .runWith(sunnySource)


    streamingClient.stream.get(streamingActortor, new StreamingEventHandler() {
      override def handle(message: StreamingCallbackMessage): Unit = {
        ref ! message.getEvent.getText
      }
    }).execute


  }

}
