@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET "MVN_CMD=mvn") ELSE (SET "MVN_CMD=%__MVNW_ARG0_NAME__%")
@SET MAVEN_WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
@SET MAVEN_WRAPPER_PROPERTIES="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"
@SET DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

@SET JAVA_HOME_CANDIDATE=%JAVA_HOME%
@IF NOT "%JAVA_HOME_CANDIDATE%"=="" (
  SET "JAVA_CMD=%JAVA_HOME_CANDIDATE%\bin\java.exe"
) ELSE (
  SET "JAVA_CMD=java"
)

@SET MAVEN_PROJECTBASEDIR=%~dp0
@IF "%MAVEN_PROJECTBASEDIR:~-1%"=="\" SET "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

@IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
  echo Downloading Maven Wrapper JAR...
  "%JAVA_CMD%" -classpath "" ^
    "-Dmaven.wrapper.jarPath=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" ^
    org.apache.maven.wrapper.MavenWrapperDownloader ^
    "%DOWNLOAD_URL%" ^
    "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" 2>nul
  IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
    powershell -Command "Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar'"
  )
)

@"%JAVA_CMD%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  "-Dmaven.wrapper.propertiesFile=%MAVEN_WRAPPER_PROPERTIES%" ^
  -jar "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" ^
  "-Dmaven.wrapper.propertiesFile=%MAVEN_WRAPPER_PROPERTIES%" ^
  %*
