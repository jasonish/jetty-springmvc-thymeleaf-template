/*
 * The MIT License (MIT)
 * Copyright (C) 2012 Jason Ish
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the “Software”), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package config;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;

/**
 * Configure the embedded Jetty server and the SpringMVC dispatcher servlet.
 */
@Configuration
public class JettyConfiguration {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MetricRegistry metricRegistry;

    @Autowired
    private HealthCheckRegistry metricsHealthCheckRegistry;

    @Bean
    public ServletHolder dispatcherServlet() {
        AnnotationConfigWebApplicationContext ctx =
                new AnnotationConfigWebApplicationContext();
        ctx.register(MvcConfiguration.class);
        DispatcherServlet servlet = new DispatcherServlet(ctx);
        ServletHolder holder = new ServletHolder("dispatcher-servlet", servlet);
        holder.setInitOrder(1);
        return holder;
    }

    @Bean
    public ServletContextHandler servletContext() throws IOException {
        ServletContextHandler handler =
                new ServletContextHandler(ServletContextHandler.SESSIONS);

        AnnotationConfigWebApplicationContext webApplicationContext =
                new AnnotationConfigWebApplicationContext();
        webApplicationContext.setParent(applicationContext);
        webApplicationContext.refresh();
        handler.setAttribute(
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                webApplicationContext);

        handler.setContextPath("/");
        handler.setResourceBase(
                new ClassPathResource("webapp").getURI().toString());

        // Set Metric attributes on the handler for the metrics servlets, then
        // add the metrics servlet.
        handler.setAttribute(MetricsServlet.METRICS_REGISTRY, metricRegistry);
        handler.setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY,
                metricsHealthCheckRegistry);
        handler.addServlet(AdminServlet.class, "/metrics/*");

        handler.addServlet(dispatcherServlet(), "/");

        handler.addFilter(new FilterHolder(
                new DelegatingFilterProxy("springSecurityFilterChain")), "/*",
                null);

        return handler;
    }

    /**
     * Jetty Server bean.
     * <p/>
     * Instantiate the Jetty server.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server jettyServer() throws IOException {

        /* Create the server. */
        Server server = new Server();

        /* Create a basic connector. */
        ServerConnector httpConnector = new ServerConnector(server);
        httpConnector.setPort(8080);
        server.addConnector(httpConnector);

        server.setHandler(servletContext());

        return server;
    }
}
