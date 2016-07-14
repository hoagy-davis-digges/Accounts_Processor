name := "Accounts_Processor"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += Resolver.url("typesafe", url("http://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns)

libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "1.0.0"
libraryDependencies += "org.scala-sbt" %% "io" % "0.13.9"
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.3"
libraryDependencies += "org.rogach" %% "scallop" % "1.0.2"