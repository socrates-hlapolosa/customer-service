package org.springframework.boot.actuate.autoconfigure.metrics.web.client;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.aot.beans.factory.BeanDefinitionRegistrar;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.actuate.metrics.web.reactive.client.MetricsWebClientCustomizer;
import org.springframework.boot.actuate.metrics.web.reactive.client.WebClientExchangeTagsProvider;

public final class ContextBootstrapInitializer {
  public static void registerWebClientMetricsConfiguration(DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.metrics.web.client.WebClientMetricsConfiguration", WebClientMetricsConfiguration.class)
        .instanceSupplier(WebClientMetricsConfiguration::new).register(beanFactory);
  }

  public static void registerWebClientMetricsConfiguration_defaultWebClientExchangeTagsProvider(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("defaultWebClientExchangeTagsProvider", WebClientExchangeTagsProvider.class).withFactoryMethod(WebClientMetricsConfiguration.class, "defaultWebClientExchangeTagsProvider")
        .instanceSupplier(() -> beanFactory.getBean(WebClientMetricsConfiguration.class).defaultWebClientExchangeTagsProvider()).register(beanFactory);
  }

  public static void registerWebClientMetricsConfiguration_metricsWebClientCustomizer(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("metricsWebClientCustomizer", MetricsWebClientCustomizer.class).withFactoryMethod(WebClientMetricsConfiguration.class, "metricsWebClientCustomizer", MeterRegistry.class, WebClientExchangeTagsProvider.class, MetricsProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebClientMetricsConfiguration.class).metricsWebClientCustomizer(attributes.get(0), attributes.get(1), attributes.get(2)))).register(beanFactory);
  }
}
