package config;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Controller;

/**
 * The root application context.
 *
 * Beans can also be configured by XML in root-context.xml which is imported by
 * this context class.
 *
 * Component scanning is also done to pickup any components other than
 *
 * @Controllers. @Controllers will be picked up by the SpringMVC context.
 */
@Configuration
@ImportResource({"classpath:META-INF/spring/root-context.xml",
        "classpath:META-INF/spring/security.xml"})
@Import({config.JettyConfiguration.class})
@ComponentScan(basePackages = {"ca.unx.template"},
        excludeFilters = {@ComponentScan.Filter(Controller.class)})
public class RootConfiguration {

    /**
     * The 'default' metrics registry.
     */
    @Bean
    public MetricRegistry metricsRegistry() {
        return new MetricRegistry();
    }

    /**
     * The metrics health check registry.
     */
    @Bean
    public HealthCheckRegistry metricsHealthCheckRegistry() {
        return new HealthCheckRegistry();
    }
}
