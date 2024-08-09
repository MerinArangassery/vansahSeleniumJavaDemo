# Jenkins Pipeline to execute Vansah Automation Demo

## Overview

This Jenkins pipeline is designed to build and test a Maven-based Java project and integrate with Vansah Connect for test result management. It demonstrates a complete CI/CD workflow using Jenkins, Maven, NodeJS, TestNG and the Vansah Connect CLI.ent app, and handle post-build actions. It performs the following:

1. **Builds and tests the Maven project**.
2. **Installs the [@vansah/vansah-connect](https://www.npmjs.com/package/@vansah/vansah-connect) npm package**.
3. **Retrieves the Vansah API token from Jenkins credentials**.
4. **Uploads TestNG results to Vansah**.

## Prerequisites

### 1. Vansah Installed 
- Ensure the Vansah Test Mangement app is installed in your JIRA workspace.
- Set up your project and note the **IssueKey / FolderID** and  **two corresponding TestcaseKeys**
### 2. Jenkins Setup
   - **[Jenkins](https://www.jenkins.io/doc/pipeline/tour/getting-started/)** must be up and running
   - Add the following plugins to your Jenkins by **Jenkins Dashboard > Manage Jenkins > Plugins > Available Plugins**
   
     - **Maven Plugin**: For building Maven projects in Jenkins.
     - **NodeJS Plugin**: For managing Node.js and npm installations in Jenkins.
     - **GitHub Plugins**: For handling projects in GitHub repository.

### 3. **Repository Setup**:
The `Vansah Automation Demo` project contains two testcases (one **positive** and one **negative**) that verifies the Test website https://selenium.vansah.io.
   - Clone [Vansah Automation Demo](https://github.com/testpointcorp/vansahSeleniumJavaDemo/tree/jenkins-job) to your repository.
   - Update the `@CustomAttributes` in [VansahIOTests.java]() and [BaseTests.java](https://github.com/testpointcorp/vansahSeleniumJavaDemo/blob/jenkins-job/src/test/java/testpack/BaseTests.java)  according to your Vansah board.

## Jenkins Configuration

### Jenkins Plugins

Ensure the following Jenkins Tools are installed:
- **Maven Installations**: For building Maven projects. Add a name (eg: maven1)
- **NodeJS Installations**: For managing Node.js and npm installations. Add a name (eg: Node1)

### Credentials

 **Add Vansah API Token** as Credentials.
 
 [Click to know how to get Vansah API Token](https://community.vansah.com/posts/how-to-generate-a-vansah-api-token-from-jira)
   - Go to **Jenkins Dashboard > Manage Jenkins > Manage Credentials**.
   - Add a new `Secret text`  credential with your Vansah API token.
   - Use the ID for this credential in the pipeline script.
   
## How to Use

1. **Create a New Pipeline Job**:
   - Go to the Jenkins dashboard.
   - Click on **New Item**.
   -  Provide a name and choose **Pipeline project**.

2. **Configure Pipeline**:
   - In the pipeline configuration, choose **Pipeline script** and paste the provided pipeline script into the script area. 
   - Make sure to update the **Git URL** as per your repository.

##### Pipeline Script


```groovy
pipeline {
    agent any  // Use any available agent

    tools {
        // Specify Maven and Node.js versions
        maven 'maven1'  // Ensure this version is configured in Jenkins Global Tool Configuration
        nodejs 'Node1'
    }

    stages {
        stage('Checkout') {
            steps {
                // Clone Your Git repository
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
                // Install the vansah-connect npm package globally
                bat 'npm i -g @vansah/vansah-connect'

                // Fetch Vansah API Token from Jenkins credentials securely
                withCredentials([string(credentialsId: 'VANSAH_API_TOKEN', variable: 'MY_API_TOKEN')]) {
                    // Configure vansah-connect with the API token
                    bat 'vansah-connect -c %MY_API_TOKEN%'
                    
                    // Upload test results to Vansah
                    bat 'vansah-connect -f ./target/surefire-reports/testng-results.xml'
                }
            }
        }
    }
}
```

3. **Save and Build**:
   - Save the configuration and build the pipeline to execute the script.


## Post-Build Actions Breakdown

### Install Vansah-Connect

- **Command**: `npm i -g @vansah/vansah-connect`
- **Description**: Installs the Vansah CLI tool globally using npm. This tool is required for interacting with the Vansah API.

### Secure API Token Handling

- **Command**:
`withCredentials([string(credentialsId: 'VANSAH_API_TOKEN',        variable: 'MY_API_TOKEN')])'
- **Description:** This Script assigns the value of the credential `VANSAH_API_TOKEN` to the environment variable `MY_API_TOKEN` which is then used to establish connection with Vansah using the command                                                          
`vansah-connect -c %MY_API_TOKEN%`

### Upload Test Results

- **Command**: 


`bat 'vansah-connect -f ./target/surefire-reports/testng-results.xml'`
- **Description**: Uploads the test results as specified **testng-results.xml** file generated during the build, to Vansah. Ensure the file path matches the location of your **testng-results.xml**.

## Notes

- Due to the negative testcase the Jenkins job will **fail** but it uploads the results as one PASSED and one FAILED. 
- Ensure that the file path to the **testng-results.xml** in `vansah-connect -f` command is correct and matches the actual location of your test results.
- Double-check that the `VANSAH_API_TOKEN` credential ID matches the ID used in the Jenkins pipeline script.

