lazy val commonSettings = Seq(
  scalaVersion := "2.12.6",
  organization := "com.emarsys"
)

lazy val `scala-logger` = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "scala-logger",
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-explaintypes",
      "-Yrangepos",
      "-feature",
      "-Xfuture",
      "-Ypartial-unification",
      "-language:higherKinds",
      "-language:existentials",
      "-unchecked",
      "-Yno-adapted-args",
      "-Xlint:_,-type-parameter-shadow",
      "-Xsource:2.13",
      "-Ywarn-dead-code",
      "-Ywarn-inaccessible",
      "-Ywarn-infer-any",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Ywarn-extra-implicit",
      "-Ywarn-unused:_,imports",
      "-Ywarn-unused:imports",
      "-opt-warnings"
    ),
    libraryDependencies ++= {
      Seq(
        "org.typelevel"        %% "cats-core"               % "1.1.0",
        "org.typelevel"        %% "cats-mtl-core"           % "0.3.0",
        "org.typelevel"        %% "cats-effect"             % "1.0.0-RC2",
        "ch.qos.logback"       % "logback-classic"          % "1.2.3",
        "net.logstash.logback" % "logstash-logback-encoder" % "5.1",
        "org.scalatest"        %% "scalatest"               % "3.0.5" % "test",
        "org.scalacheck"       %% "scalacheck"              % "1.14.0" % "test",
        "com.github.mpilquist" %% "simulacrum"              % "0.12.0",
        "com.propensive"       %% "magnolia"                % "0.10.0"
      )
    }
  )

credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "nexus.service.emarsys.net",
  sys.env("NEXUS_USERNAME"),
  sys.env("NEXUS_PASSWORD")
)

publishTo := Some("releases" at "https://nexus.service.emarsys.net/repository/emartech/")
addCompilerPlugin("org.spire-math"  %% "kind-projector" % "0.9.6")
addCompilerPlugin("org.scalamacros" % "paradise"        % "2.1.0" cross CrossVersion.full)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
