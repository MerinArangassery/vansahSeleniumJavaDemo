pipeline {
    agent any  // Use any available agent

    environment {
        // Set environment variables if needed
         PATH = "${env.PATH};C:\\Users\\merin\\AppData\\Roaming\\npm"
    }

    tools {
        // Specify Maven version
        maven 'maven1'  // Ensure this version is configured in Jenkins Global Tool Configuration
    }

    stages {
        stage('Checkout') {
            steps {
                // Clone the Git repository
               git 'https://github.com/MerinArangassery/vansahSeleniumJavaDemo.git'
            }
        }

        stage('Build') {
            steps {
                // Run Maven build
                bat 'mvn clean install'  // Use 'bat' instead of 'sh' if running on Windows
            }
        }

        stage('Test') {
            steps {
                // Run Maven tests
                bat 'mvn test'  // Use 'bat' instead of 'sh' if running on Windows
            }
        }

        stage('Archive Results') {
            steps {
                // Archive the test results
                junit '**/target/test-classes/testng-results.xml'
                // Or archive other build artifacts
                archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
            }
        }
    }

    post {
      
        success {
            // Actions that run only if the build is successful
            echo 'Build succeeded!'
        }
        failure {
            // Actions that run only if the build fails
            echo 'Build failed!'
        }
        always {
            script {
                // Run vansah-connect command
                bat 'vansah-connect -f testng-reports.xml'  // Use 'bat' instead of 'sh' if running on Windows
            }
        }
    }
}
