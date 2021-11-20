def call(Map config) {
    withSonarQubeEnv(config.sonarqube_env_name) {
        sh """
        /root/.dotnet/tools/dotnet-sonarscanner begin \
            /k:${config.b_config.project.name} \
            /v:${config.project_full_version} \
            /n:${config.b_config.project.name} \
            /d:sonar.links.ci=${BUILD_URL}
        """
        lib_buildController(
            config
        )
        sh "/root/.dotnet/tools/dotnet-sonarscanner end"
    }
}