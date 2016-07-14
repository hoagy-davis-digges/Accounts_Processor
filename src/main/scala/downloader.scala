/**
  * Created by hoagydavis-digges on 10/07/2016.
  */

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.{Document, Element}
import sbt.IO.unzipURL
import java.net.URL
import java.io.File

import com.github.tototoshi.csv._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.rogach.scallop._

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val months = opt[Int](default = Some(12))
  val term = opt[String](default = Some("NetCurrentAssetsLiabilities"))
  val outfolder = opt[String](default = Some("../company_data"))
  verify()
}

object downloader {

  def get_urls(months: Int): List[String] = {
    val browser = JsoupBrowser()
    val format = DateTimeFormat.forPattern("MMMMyyyy")

    val doc = browser.get("http://download.companieshouse.gov.uk/en_monthlyaccountsdata.html")
    val link_area: List[Element] = doc >> elementList(".grid_7.push_1")
    val links: List[Element] = link_area.flatMap(_ >> elementList("a"))
    val link_urls: List[String] = links.map(_ >> attr("href")("a"))
    val ordered_urls = link_urls.sortBy(url => DateTime.parse(url.split("-|\\.")(1), format).getMillis)

    link_urls.takeRight(months).map(u => "http://download.companieshouse.gov.uk/" + u)
  }

  def get_most_recent_term(doc: Document, term: String) = {
    val all_matches = doc >> elementList(s"[name$$=$term]")
    if (all_matches.length == 1) {
      all_matches.head.text
    } else if (all_matches.length > 1) {
      val recent = all_matches.sortBy {
        one_match =>
          val context_id = one_match.attr("contextRef")
          val date = doc >> extractor(s"context#$context_id period", text, asDate("yyyy-MM-dd"))
          date.getMillis
      }(Ordering[Long].reverse).head
      recent.text
    }
  }


  def main(args: Array[String]) {

    val conf = new Conf(args)
    val url_list = get_urls(conf.months())
    val term = conf.term()
    val folder = new File(conf.outfolder())
    folder.mkdirs
    val browser = JsoupBrowser()

    url_list.par.foreach {
      url_string =>
        val csv = new File(folder.getParent + "/" + url_string + ".csv")
        val writer = CSVWriter.open(csv)
        writer.writeRow(List("Company Number", term))
        val month_folder = new File(folder.getPath + "/" + url_string)
        month_folder.mkdirs()
        unzipURL(new URL(url_string), month_folder)
          .par
          .foreach {
            file =>
              val f = browser.parseFile(file)
              val company_num = file.getName.split("_")(2)
              val recent = get_most_recent_term(f, term)
              writer.writeRow(List(company_num, recent))
          }
    }
  }
}
