Prereqs: jdk11.x, gradle 6.4.1, docker, docker-compose.
To build run

_gradle clean build dockerBuildImage_

to start loan-service container run

_docker-compose up -d loan-service_