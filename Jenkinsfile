pipeline {
    agent {
        docker { 
            image 'maven:3.6-jdk-11-slim' 
            args env.JOB_DOCKER_OPTS
        }
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn -B clean verify'
            }
        }
         stage('Dist Local') {
            steps {
                sh "mvn -B -DskipTests -DaltSnapshotDeploymentRepository=nexus::default::${env.NEXUS_SNAPSHOTS_URL} -DaltReleaseDeploymentRepository=nexus::default::${env.NEXUS_RELEASES_URL} -DaltDeploymentRepository=nexus::default::${env.NEXUS_SNAPSHOTS_URL} deploy"
            }
        }
    }
}
