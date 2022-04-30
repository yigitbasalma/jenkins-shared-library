def call(Map config) {
    config.b_config.deploy.each { it ->
        def image = sh(
            script: "${config.script_base}/meta.py -p ${config.b_config.project.name} -a ${it.name} -e \$(cat /etc/stack-env) --provider ${config.meta_provider} get --image-name",
            returnStdout: true
        ).trim()

        "${it.type}"(config, image, it.repo, it.path, it.name, config.b_config.project.name)

        sh """
        ${config.script_base}/meta.py -p ${config.b_config.project.name} -a ${it.name} -e \$(cat /etc/stack-env) --provider ${config.meta_provider} set --image-id ${image}
        """
    }
}

def argocd(Map config, String image, String repo, String path, String appName, String projectName) {
    // Change image version on argocd repo and push
    sh """
    ${config.script_base}/argocd.py --image "${image}" -r ${repo} --application-path ${path}/${config.environment}
    """
}

def lambda(Map config, String image, String repo, String path, String appName, String projectName) {
    // Change image version on argocd repo and push
    sh """
    aws lambda invoke --function-name promote-image --payload '{"project-identifier": "${projectName}", "app-identifier": "${appName}", "environment": "\$(cat /etc/stack-env)", "image": "${image}"}' lambda-out.json
    """
}