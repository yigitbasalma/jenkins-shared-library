def configureInit(Map config) {
    // Define constraints
    config.script_base = "/scripts"

    // Configure repository settings
    config.scm_global_config = [url: config.scm_address]
    if ( config.scm_security ) {
        config.scm_global_config.credentialsId = "${config.scm_credentials_id}"
    }

    // SonarQube settings
    config.sonarqube_env_name = "sonarqube01"
    config.sonarqube_home = tool config.sonarqube_env_name
}