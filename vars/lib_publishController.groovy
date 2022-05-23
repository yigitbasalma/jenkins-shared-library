def call(Map config) {
    config.b_config.projects.each { it ->
        if ( it.containsKey("push") && it.push ) {
            withCredentials([[$class:"UsernamePasswordMultiBinding", credentialsId: it.key, usernameVariable: "USERNAME", passwordVariable: "PASSWORD"]]) {
                sh """
                for artifact in \$(ls | grep -E "*.tgz")
                do
                    curl -v -u ${USERNAME}:${PASSWORD} \
                    -X POST "${it.target}/service/rest/v1/components?repository=${it.repo}" \
                    -F "npm.asset=@\${artifact}"
                done
                """
            }
        }
    }
}