package com.jumei.wmls.commons.utils

import javax.servlet.http.HttpServletRequest

object IPUtils {
    private const val REAL_IP_BEFORE_NGINX_PROXY = "X-real-ip"
    private const val FORWARD_IP_BEFORE_NGINX_PROXY = "X-Forwarded-For"
    private const val PROXY_CLIENT_IP = "Proxy-Client-IP"
    private const val WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP"
    private const val HTTP_CLIENT_IP = "HTTP_CLIENT_IP"
    private const val HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR"
    fun getRealIp(request: HttpServletRequest): String {
        var ip: String? = request.getHeader(FORWARD_IP_BEFORE_NGINX_PROXY)
        if (isValide(ip) && ip!!.contains(",")) {
            val proxyedIps = ip.split(",")
            if (proxyedIps.isNotEmpty()) {
                return proxyedIps[0]
            }
        }
        ip = request.getHeader(PROXY_CLIENT_IP)
        if (isValide(ip)) {
            return ip
        }
        ip = request.getHeader(WL_PROXY_CLIENT_IP)
        if (isValide(ip)) {
            return ip
        }

        ip = request.getHeader(HTTP_CLIENT_IP)
        if (isValide(ip)) {
            return ip
        }

        ip = request.getHeader(HTTP_X_FORWARDED_FOR)
        if (isValide(ip)) {
            return ip
        }


        ip = request.getHeader(REAL_IP_BEFORE_NGINX_PROXY)
        if (isValide(ip)) {
            return ip
        }
        return request.remoteAddr!!

    }

    private fun isValide(ip: String?): Boolean {
        return !(ip.isNullOrBlank() || "unknown".equals(ip, ignoreCase = true))
    }

}