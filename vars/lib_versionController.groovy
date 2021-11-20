def call(Map config) {
    // Configure project version if not configured
    if ( ! config.project_version ) {
        config.project_version = sh(
            script: "${config.script_base}/infos.py --project \"${config.b_config.project.name}\" --workspace \"${WORKSPACE}\" -m get_version",
            returnStdout: true
        ).trim()
        def full_version = sh(
            script: "cat ${config.config_file} | python3 -c 'import sys, yaml; print(\".\".join(yaml.load(sys.stdin, Loader=yaml.CLoader)[\"project\"][\"version\"].split(\".\")[:2]))'",
            returnStdout: true
        ).trim()
        config.project_full_version = "${full_version}.${config.project_version}"
    }
}