pipeline {
    agent any  // Use any available agent

    environment {
        // Set environment variables if needed
        // PATH = "${env.PATH};C:\\Users\\merin\\AppData\\Roaming\\npm"
        VANS_H_PATH = "C:\\Users\\merin\\AppData\\Roaming\\npm"
    }

    tools {
        // Specify Maven version
        maven 'maven1'  // Ensure this version is configured in Jenkins Global Tool Configuration
        nodejs 'Node 1'
        
    }

    stages {
        stage('Checkout') {
            steps {
                // Clone the Git repository
              // git 'https://github.com/MerinArangassery/vansahSeleniumJavaDemo.git'
                git url: 'https://github.com/MerinArangassery/vansahSeleniumJavaDemo.git', branch: 'jenkins-job'
            }
        }

       stage('Build and Test') {
            steps {
                script {
                    try {
                        // Run Maven build
                        bat 'mvn clean install'
                        
                        // Run Maven tests if the build succeeds
                        bat 'mvn test'
                    } catch (Exception e) {
                        echo "Build or test failed: ${e.getMessage()}"
                        currentBuild.result = 'FAILURE'
                        throw e
                    }
                }
            }
        }
    }


    post {
        always {
            script {
                bat "${VANS_H_PATH} -f target/surefire-reports/testng-results.xml"
        }
}
}
