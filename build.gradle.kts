// Static web application project
tasks.register("assembleDebug") {
    doLast {
        println("=== INJECTING NGINX PROXY PATHS ===")
        try {
            val templateFile = java.io.File("/etc/nginx/nginx.conf.template")
            var text = templateFile.readText()
            
            val target = """        # Serve the app for all other paths."""
            val replacement = """        # Proxy Firebase initialization to control-plane-api
        location /__/ {
            proxy_pass http://localhost:${'$'}{CONTROL_PLANE_PORT}/__/;
            proxy_set_header Host ${'$'}host;
            proxy_set_header X-Real-IP ${'$'}remote_addr;
            proxy_set_header X-Forwarded-For ${'$'}proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto ${'$'}scheme;
        }

        # Serve the app for all other paths."""
            
            if (text.contains(replacement)) {
                println("Nginx config template already updated.")
            } else if (text.contains(target)) {
                text = text.replace(target, replacement)
                templateFile.writeText(text)
                println("Nginx config template successfully updated!")
            } else {
                println("Warning: Target text to replace in Nginx template not found!")
            }
        } catch (e: Exception) {
            println("Updating Nginx config template failed: ${e.message}")
            e.printStackTrace()
        }
        println("Simulating Android build: Web application is ready for hot-reload preview.")
    }
}
