/**
  * Created by hoagydavis-digges on 10/07/2016.
  */

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.Element
import sbt.IO.unzipURL
import java.net.URL
import java.io.File
import com.github.tototoshi.csv._

object downloader {

  def main(args: Array[String]) {

    val browser = JsoupBrowser()
    val doc = browser.get("http://download.companieshouse.gov.uk/en_monthlyaccountsdata.html")
    val link_area: List[Element] = doc >> elementList(".grid_7.push_1")
    val links: List[Element] = link_area.flatMap(_ >> elementList("a"))
    val link_urls: List[String] = links.map(_ >> attr("href")("a")).filter(a => a.contains("2015") || a.contains("2016"))
    val last_12 = link_urls.takeRight(12)
    val url_list = last_12.map(u => "http://download.companieshouse.gov.uk/" + u )
    val folder = new File("/Users/hoagydavis-digges/test")
    folder.mkdirs

    url_list.par.foreach {
      url_string =>
        val csv = new File(folder.getParent + "/" + url_string + ".csv")
        val writer = CSVWriter.open(csv)
        writer.writeRow(List("Current "))
        val month_folder = new File(folder.getPath + "/" + url_string)
        month_folder.mkdirs()
        unzipURL(new URL(url_string), month_folder)
          .par
          .foreach {
            file =>
              val f = browser.get(file.toString)
              val current_assets = f >> text("#currentCurrentAssets")
          }

    }
  }
}
