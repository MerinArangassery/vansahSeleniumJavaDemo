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
                // Check for the existence of the TestNG results file
                bat 'dir /s /b target\\surefire-reports'
                def testResultsPath = '\\target\\surefire-reports\\testng-results.xml'
                
                if (fileExists(testResultsPath)) {
                    // Archive the TestNG results
                    junit testResultsPath

                    // Ensure the vansah-connect tool is available
                    if (fileExists(VANS_H_PATH)) {
                        // Upload test results to vansah-connect
                        bat "${VANS_H_PATH} -f target/surefire-reports/testng-results.xml"
                    } else {
                        error "vansah-connect tool not found at ${VANS_H_PATH}"
                    }
                } else {
                    echo "TestNG results file not found: ${testResultsPath}"
                }
            }
        }
}
}
