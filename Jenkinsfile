pipeline {
    agent {
        label 'docker-ce'
    }
    stages {
        stage('Build and publish') {
            agent {
                docker {
                    image 'europeanspallationsource/oracle-jdk-maven-jenkins:8u161-b12-2'
                    reuseNode true
                }
            }
            steps {
                script {
                    env.POM_VERSION = readMavenPom().version
                    currentBuild.displayName = env.POM_VERSION
                }
                withCredentials([file(credentialsId: 'gpg-privatekey', variable: 'GPG_PRIVATE_KEY'),
                                 string(credentialsId: 'gpg-passphrase', variable: 'GPG_PASSPHRASE'),
                                 usernamePassword(credentialsId: 'maven-central', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    sh 'gpg --import ${GPG_PRIVATE_KEY}'
                    sh "mvn --batch-mode -DskipTests -Dgpg.passphrase='${GPG_PASSPHRASE}' -Dserver.id=ossrh -Dserver.username=${USERNAME} -Dserver.password='${PASSWORD}' clean deploy"
                }
            }
        }
        stage('SonarQube analysis') {
            agent {
                docker {
                    image 'europeanspallationsource/oracle-jdk-maven-jenkins:8u161-b12-2'
                    reuseNode true
                }
            }
            steps {
                withCredentials([string(credentialsId: 'sonarqube', variable: 'TOKEN')]) {
                    sh 'mvn --batch-mode -Dsonar.scm.disabled=true -Dsonar.login=$TOKEN -Dsonar.branch=${BRANCH_NAME} sonar:sonar'
                }
            }
        }
    }
}
