def call(Map config) {

    pipeline {
       agent {
         kubernetes {
              yaml '''
      spec:
        containers:
        - name: nodejs
          image: node:16
          command:
          - sleep
          - 3000
'''
         }
       }

        stages {
            stage("Configure Init") {
                steps {
                    script {
                        lib_helper.configureInit(
                            config
                        )
                    }
                }
            }

            stage("Checkout Project Code") {
                steps {
                    checkout scm: [
                        $class: "GitSCM",
                        branches: [[name: "refs/heads/${config.target_branch}"]],
                        submoduleCfg: [],
                        userRemoteConfigs: [
                            config.scm_global_config
                        ]
                    ]
                }
            }

            stage("Read Project Config") {
                steps {
                    script {
                        // Create config file variable
                        config.config_file = ".jenkins/buildspec.yaml"
                        config.b_config = readYaml file: config.config_file
                        config.job_base = sh(
                            script: "python3 -c 'print(\"${JENKINS_HOME}/jobs/%s\" % \"/jobs/\".join(\"${JOB_NAME}\".split(\"/\")))'",
                            returnStdout: true
                        ).trim()
                    }
                }
            }

            stage("Change Version Number") {
                when {
                    expression {
                        return config.b_config.controllers.versionNumberController
                    }
                }
                steps {
                    script {
                        lib_versionController(
                            config
                        )
                    }
                }
            }

            stage("Resolve Dependency") {
                when {
                    expression {
                        return config.b_config.controllers.restoreController
                    }
                }
                steps {
                    script {
                        lib_restoreController(
                            config
                        )
                    }
                }
            }

            stage("Run Unit tests") {
                when {
                    expression {
                        return config.b_config.controllers.unitTestController && 
                            config.b_config.controllers.buildController
                    }
                }
                steps {
                    script {
                        lib_unitTestController(
                            config
                        )
                    }
                }
            }

            stage("Run SonarQube Code Quality") {
                when {
                    expression {
                        return config.b_config.controllers.codeQualityTestController && 
                            config.b_config.controllers.buildController
                    }
                }
                steps {
                    script {
                        lib_codeQualityTestController(
                            config
                        )
                    }
                }
            }

            stage("Build Project") {
                when {
                    expression {
                        return config.b_config.controllers.buildController && 
                            config.b_config.controllers.restoreController &&
                            ! config.b_config.controllers.codeQualityTestController
                    }
                }
                steps {
                    script {
                        lib_buildController(
                            config
                        )
                    }
                }
            }

            stage("Publish Artifact") {
                when {
                    expression {
                        return config.b_config.controllers.publishController && 
                            config.b_config.controllers.buildController
                    }
                }
                steps {
                    script {
                        lib_publishController(
                            config
                        )
                    }
                }
            }

            stage("Build and Publish as a Container") {
                when {
                    expression {
                        return config.b_config.controllers.containerController
                    }
                }
                steps {
                    script {
                        lib_containerController(
                            config
                        )
                    }
                }
            }

        }

        post {
            always {
                // Take necessary actions
                script {
                    // Cleanup
                    lib_cleanupController(
                        config
                    )

                    lib_postbuildController(
                        config
                    )
                }
            }
        }

    }
}
