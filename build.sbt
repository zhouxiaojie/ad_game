name := "ad_game"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

libraryDependencies ++= Seq(
  javaJdbc,
  "mysql" % "mysql-connector-java" % "5.1.18",
  javaWs,
  cache,
  "com.google.protobuf" % "protobuf-java" % "2.6.1"
  
)
