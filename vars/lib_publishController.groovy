def call(Map config) {
    config.b_config.projects.each { it ->
        if ( it.containsKey("push") && it.push ) {
            sh """
            ${config.b_config.project.builderVersion} nuget push --skip-duplicate \
                -s ${it.target} \
                -k ${it.key} \
                ${WORKSPACE}/nupkgs/${it.name}
            """
        }
    }
}