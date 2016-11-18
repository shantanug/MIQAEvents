compile : 
mvn clean compile assembly:single

run 
nohup java -jar  MiqaEvents-1.0-SNAPSHOT-jar-with-dependencies.jar  &