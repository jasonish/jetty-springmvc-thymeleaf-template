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
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.GenericWebApplicationContext;

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

    @Value("${jetty.port:8080}")
    private int jettyPort;

    private void addMetricsServlet(WebAppContext webAppContext) {

        // Set Metric attributes on the handler for the metrics servlets, then
        // add the metrics servlet.
        webAppContext.setAttribute(MetricsServlet.METRICS_REGISTRY,
                metricRegistry);
        webAppContext.setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY,
                metricsHealthCheckRegistry);

        webAppContext.addServlet(new ServletHolder(new AdminServlet()),
                "/metrics/*");
    }

    @Bean
    public WebAppContext webAppContext() throws IOException {

        WebAppContext ctx = new WebAppContext();
        ctx.setContextPath("/");
        ctx.setWar(new ClassPathResource("webapp").getURI().toString());

        /* Disable directory listings if no index.html is found. */
        ctx.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed",
                "false");

        /* Create the root web application context and set it as a servlet
         * attribute so the dispatcher servlet can find it. */
        GenericWebApplicationContext webApplicationContext =
                new GenericWebApplicationContext();
        webApplicationContext.setParent(applicationContext);
        webApplicationContext.refresh();

        ctx.setParentLoaderPriority(true);

        ctx.addEventListener(new ContextLoaderListener(webApplicationContext));
        ctx.addEventListener(
                new WebAppInitializerLoader(new WebApplicationInitializer[]{
                        new SpringWebAppInitializer(),
                        new SpringSecurityWebAppInitializer()
                }));

        return ctx;
    }

    /**
     * Jetty Server bean.
     * <p/>
     * Instantiate the Jetty server.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server jettyServer() throws Exception {

        /* Create the server. */
        Server server = new Server();

        /* Create a basic connector. */
        ServerConnector httpConnector = new ServerConnector(server);
        httpConnector.setPort(jettyPort);
        server.addConnector(httpConnector);

        server.setHandler(webAppContext());

        /* We can add servlets or here, or we could do it in the
         * SpringWebAppInitializer. */
        addMetricsServlet(webAppContext());

        return server;
    }
}
