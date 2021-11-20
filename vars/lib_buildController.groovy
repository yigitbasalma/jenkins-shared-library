def call(Map config) {
    config.b_config.projects.each { it ->
        def buildArgs = []

        if ( config.b_config.containsKey("buildArgs") ) {
            buildArgs.addAll(config.b_config.buildArgs)
        }

        if ( it.containsKey("buildArgs") ) {
            buildArgs.addAll(it.buildArgs)
        }

        sh """
        ${config.b_config.project.builderVersion} build -c Release --no-restore \
            -o ${it.path}/out \
            ${buildArgs.unique().join(" ")} \
            /p:Version="${config.project_full_version}" \
            ${it.path}
        """
    }
}