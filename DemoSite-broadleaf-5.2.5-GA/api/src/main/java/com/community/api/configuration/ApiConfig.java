package com.community.api.configuration;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.valves.RemoteIpValve;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.community.core.config.CoreConfig;

/**
 * @author Elbert Bautista (elbertbautista)
 */
@Configuration
@Import({CoreConfig.class, ApiSecurityConfig.class})
public class ApiConfig {

	@Bean
	@SuppressWarnings("static-method")
	public EmbeddedServletContainerFactory servletContainer() {
	    TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
	    tomcat.addAdditionalTomcatConnectors(createConnector());
	    tomcat.addContextValves(createRemoteIpValves());
	    return tomcat;
	}

	private static RemoteIpValve createRemoteIpValves() {
	    RemoteIpValve remoteIpValve = new RemoteIpValve();
	    remoteIpValve.setRemoteIpHeader("x-forwarded-for");
	    remoteIpValve.setProtocolHeader("x-forwarded-proto");
	    return remoteIpValve;
	}

	private static Connector createConnector() {
	    Connector connector = new Connector("AJP/1.3");
	    connector.setPort(8009);
	    return connector;
	}

}
