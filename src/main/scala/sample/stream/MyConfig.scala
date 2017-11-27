package sample.stream

import com.typesafe.config.ConfigFactory
import scala.util.Properties

class MyConfig(fileNameOption: Option[String] = None) {

  val config = fileNameOption.fold(
    ifEmpty = ConfigFactory.load() )(
    file => ConfigFactory.load(file) )

  def envOrElseConfig(name: String): String = {
    Properties.envOrElse(
      name.toUpperCase.replaceAll("""\.""", "_"),
      config.getString(name)
    )
  }
}
