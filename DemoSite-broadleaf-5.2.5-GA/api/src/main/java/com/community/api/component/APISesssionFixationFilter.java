package com.community.api.component;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.common.security.RandomGenerator;
import org.broadleafcommerce.profile.web.site.security.SessionFixationProtectionCookie;
import org.broadleafcommerce.profile.web.site.security.SessionFixationProtectionFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("blSessionFixationProtectionFilter")
public class APISesssionFixationFilter extends SessionFixationProtectionFilter {

	private static final Log LOG = LogFactory.getLog(APISesssionFixationFilter.class);
	
	@Override
    public void doFilter(ServletRequest sRequest, ServletResponse sResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) sRequest;
        HttpServletResponse response = (HttpServletResponse) sResponse;
        HttpSession session = request.getSession(false);

        if (SecurityContextHolder.getContext() == null) {
            chain.doFilter(request, response);
        }

        String activeIdSessionValue = (session == null) ? null : (String) session.getAttribute(SESSION_ATTR);
        LOG.info("activeIdSessionValue " + activeIdSessionValue);
        if (StringUtils.isNotBlank(activeIdSessionValue) && request.isSecure()) {
            // The request is secure and and we've set a session fixation protection cookie

            String activeIdCookieValue = cookieUtils.getCookieValue(request, SessionFixationProtectionCookie.COOKIE_NAME);
            String decryptedActiveIdValue = encryptionModule.decrypt(activeIdCookieValue);
            LOG.info("activeIdSessionValue :: " + activeIdSessionValue + "  decryptedActiveIdValue :: " + decryptedActiveIdValue);
            if (!activeIdSessionValue.equals(decryptedActiveIdValue)) {
                abortUser(request, response);
                LOG.info("Session has been terminated. ActiveID did not match expected value.");
                return;
            }
        } else if (request.isSecure() && session != null) {
            // If there is no session (session == null) then there isn't anything to worry about

            // The request is secure, but we haven't set a session fixation protection cookie yet
            String token;
            try {
                token = RandomGenerator.generateRandomId("SHA1PRNG", 32);
            } catch (NoSuchAlgorithmException e) {
                throw new ServletException(e);
            }

            String encryptedActiveIdValue = encryptionModule.encrypt(token);

            session.setAttribute(SESSION_ATTR, token);
            cookieUtils.setCookieValue(response, SessionFixationProtectionCookie.COOKIE_NAME, encryptedActiveIdValue, "/", -1, true);
        }

        chain.doFilter(request, response);
    }
	
}
