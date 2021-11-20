def call(Map config) {
    config.b_config.deploy.each { it ->
        def image = sh(
            script: "${config.script_base}/infos.py --project ${config.b_config.project.name} --workspace ${WORKSPACE} -m get_container_version --container-name ${it.name}",
            returnStdout: true
        ).trim()

        "${it.type}"(config, image, it.repo, it.path, it.name)
    }
}

def argocd(Map config, String image, String repo, String path, String appName) {
    // Change image version on argocd repo and push
    sh """
    ${config.script_base}/argocd.py --image "${image}" -r ${repo} --application-path ${path}/${config.environment}
    """

    // Save image version for environment
    sh """
    ${config.script_base}/infos.py --project ${config.b_config.project.name} --workspace ${WORKSPACE} -m save_env_container_map --container-environment-image-map "${config.environment};${image}" --app-name ${appName}
    """
}