package org.chaos;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

public final class IPUtils {

    private static final String REAL_IP_BEFORE_NGINX_PROXY = "X-real-ip";
    private static final String FORWARD_IP_BEFORE_NGINX_PROXY = "X-Forwarded-For";
    private static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
    private static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    private static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
    private static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";
            
    private IPUtils() {
        throw new UnsupportedOperationException("singleton");
    }

    public static String getRealIp(HttpServletRequest request) {
        String ip = request.getHeader(FORWARD_IP_BEFORE_NGINX_PROXY);
        if (isValide(ip) && ip.contains(",")) {
            String[] proxyedIps = ip.split(",");
            if (ArrayUtils.isNotEmpty(proxyedIps)) {
                return proxyedIps[0];
            }
        }
        ip = request.getHeader(PROXY_CLIENT_IP);
        if (isValide(ip)) {
            return ip;
        }
        ip = request.getHeader(WL_PROXY_CLIENT_IP);
        if (isValide(ip)) {
            return ip;
        }

        ip = request.getHeader(HTTP_CLIENT_IP);
        if (isValide(ip)) {
            return ip;
        }

        ip = request.getHeader(HTTP_X_FORWARDED_FOR);
        if (isValide(ip)) {
            return ip;
        }

        ip = request.getHeader(REAL_IP_BEFORE_NGINX_PROXY);
        if (isValide(ip)) {
            return ip;
        }
        return request.getRemoteAddr();

    }

    private static Boolean isValide(String ip) {
        return !(StringUtils.isBlank(ip) || StringUtils.equalsIgnoreCase("unknown", ip));
    }
}
