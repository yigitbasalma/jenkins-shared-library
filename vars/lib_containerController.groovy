def call(Map config) {
    // Check object existency
    if ( ! config.b_config.containerConfig ) {
        currentBuild.result = "ABORTED"
        error("You have to set 'containerConfig' in your yaml file.")
    }

    // Define constraints
    container_registry = config.container_registry
    container_remote_repository = "dockerhub.com"

    config.b_config.imageTag = sh(
        script: """
        git log --pretty=format:"%h" | head -1
        """,
        returnStdout: true
    ).trim()

    config.b_config.containerConfig.each { it ->
        sh """
        docker build --rm \
            -t ${container_registry}/${config.project_container_repo_folder}/${it.name.toLowerCase()}:${config.b_config.imageTag} \
            -t ${container_registry}/${config.project_container_repo_folder}/${it.name.toLowerCase()}:latest \
            -f ${it.dockerFilePath} \
            ${it.contextPath}
        """
        sh """
        docker push ${container_registry}/${config.project_container_repo_folder}/${it.name.toLowerCase()}:${config.b_config.imageTag} && \
            docker push ${container_registry}/${config.project_container_repo_folder}/${it.name.toLowerCase()}:latest
        """

        if ( it.containsKey("uploadToRemote") && it.uploadToRemote ) {
            sh """
            docker tag ${container_registry}/${config.project_container_repo_folder}/${it.name.toLowerCase()}:${config.b_config.imageTag} \
                ${container_remote_repository}/${config.project_container_repo_folder}/${it.name.toLowerCase()}:${config.b_config.imageTag} && \
            docker tag ${container_registry}/${config.project_container_repo_folder}/${it.name.toLowerCase()}:latest \
                ${container_remote_repository}/${config.project_container_repo_folder}/${it.name.toLowerCase()}:latest
            """
            withCredentials([[$class:"UsernamePasswordMultiBinding", credentialsId: config.container_artifact_repo_cred_id, usernameVariable: "USERNAME", passwordVariable: "PASSWORD"]]) {
                sh """
                docker login --username $USERNAME --password $PASSWORD ${container_remote_repository} && \
                    docker push ${container_remote_repository}/${config.project_container_repo_folder}/${it.name.toLowerCase()}:${config.b_config.imageTag} && \
                    docker push ${container_remote_repository}/${config.project_container_repo_folder}/${it.name.toLowerCase()}:latest
                """
            }
        }

        sh """
        ${config.script_base}/meta.py -p ${config.b_config.project.name} -a ${it.name} -e \$(cat /etc/stack-env) --provider ${config.meta_provider} set --image-id ${config.b_config.imageTag}
        """
    }
}