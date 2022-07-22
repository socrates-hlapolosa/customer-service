package org.springframework.boot.actuate.autoconfigure.health;

import java.util.Map;
import org.springframework.aot.beans.factory.BeanDefinitionRegistrar;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.reactive.AdditionalHealthEndpointPathsWebFluxHandlerMapping;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.HealthEndpointGroups;
import org.springframework.boot.actuate.health.HttpCodeStatusMapper;
import org.springframework.boot.actuate.health.ReactiveHealthContributorRegistry;
import org.springframework.boot.actuate.health.ReactiveHealthEndpointWebExtension;
import org.springframework.boot.actuate.health.StatusAggregator;
import org.springframework.context.ApplicationContext;

public final class ContextBootstrapInitializer {
  public static void registerHealthEndpointConfiguration(DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.health.HealthEndpointConfiguration", HealthEndpointConfiguration.class)
        .instanceSupplier(HealthEndpointConfiguration::new).register(beanFactory);
  }

  public static void registerHealthEndpointConfiguration_healthStatusAggregator(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("healthStatusAggregator", StatusAggregator.class).withFactoryMethod(HealthEndpointConfiguration.class, "healthStatusAggregator", HealthEndpointProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(HealthEndpointConfiguration.class).healthStatusAggregator(attributes.get(0)))).register(beanFactory);
  }

  public static void registerHealthEndpointConfiguration_healthHttpCodeStatusMapper(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("healthHttpCodeStatusMapper", HttpCodeStatusMapper.class).withFactoryMethod(HealthEndpointConfiguration.class, "healthHttpCodeStatusMapper", HealthEndpointProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(HealthEndpointConfiguration.class).healthHttpCodeStatusMapper(attributes.get(0)))).register(beanFactory);
  }

  public static void registerHealthEndpointConfiguration_healthEndpointGroups(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("healthEndpointGroups", HealthEndpointGroups.class).withFactoryMethod(HealthEndpointConfiguration.class, "healthEndpointGroups", ApplicationContext.class, HealthEndpointProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(HealthEndpointConfiguration.class).healthEndpointGroups(attributes.get(0), attributes.get(1)))).register(beanFactory);
  }

  public static void registerHealthEndpointConfiguration_healthContributorRegistry(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("healthContributorRegistry", HealthContributorRegistry.class).withFactoryMethod(HealthEndpointConfiguration.class, "healthContributorRegistry", ApplicationContext.class, HealthEndpointGroups.class, Map.class, Map.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(HealthEndpointConfiguration.class).healthContributorRegistry(attributes.get(0), attributes.get(1), attributes.get(2), attributes.get(3)))).register(beanFactory);
  }

  public static void registerHealthEndpointConfiguration_healthEndpoint(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("healthEndpoint", HealthEndpoint.class).withFactoryMethod(HealthEndpointConfiguration.class, "healthEndpoint", HealthContributorRegistry.class, HealthEndpointGroups.class, HealthEndpointProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(HealthEndpointConfiguration.class).healthEndpoint(attributes.get(0), attributes.get(1), attributes.get(2)))).register(beanFactory);
  }

  public static void registerHealthEndpointConfiguration_healthEndpointGroupsBeanPostProcessor(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("healthEndpointGroupsBeanPostProcessor", HealthEndpointConfiguration.HealthEndpointGroupsBeanPostProcessor.class).withFactoryMethod(HealthEndpointConfiguration.class, "healthEndpointGroupsBeanPostProcessor", ObjectProvider.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> HealthEndpointConfiguration.healthEndpointGroupsBeanPostProcessor(attributes.get(0)))).register(beanFactory);
  }

  public static void registerReactiveHealthEndpointConfiguration(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.health.ReactiveHealthEndpointConfiguration", ReactiveHealthEndpointConfiguration.class)
        .instanceSupplier(ReactiveHealthEndpointConfiguration::new).register(beanFactory);
  }

  public static void registerReactiveHealthEndpointConfiguration_reactiveHealthContributorRegistry(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("reactiveHealthContributorRegistry", ReactiveHealthContributorRegistry.class).withFactoryMethod(ReactiveHealthEndpointConfiguration.class, "reactiveHealthContributorRegistry", Map.class, Map.class, HealthEndpointGroups.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(ReactiveHealthEndpointConfiguration.class).reactiveHealthContributorRegistry(attributes.get(0), attributes.get(1), attributes.get(2)))).register(beanFactory);
  }

  public static void registerHealthEndpointReactiveWebExtensionConfiguration_WebFluxAdditionalHealthEndpointPathsConfiguration(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.health.HealthEndpointReactiveWebExtensionConfiguration$WebFluxAdditionalHealthEndpointPathsConfiguration", HealthEndpointReactiveWebExtensionConfiguration.WebFluxAdditionalHealthEndpointPathsConfiguration.class)
        .instanceSupplier(HealthEndpointReactiveWebExtensionConfiguration.WebFluxAdditionalHealthEndpointPathsConfiguration::new).register(beanFactory);
  }

  public static void registerWebFluxAdditionalHealthEndpointPathsConfiguration_healthEndpointWebFluxHandlerMapping(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("healthEndpointWebFluxHandlerMapping", AdditionalHealthEndpointPathsWebFluxHandlerMapping.class).withFactoryMethod(HealthEndpointReactiveWebExtensionConfiguration.WebFluxAdditionalHealthEndpointPathsConfiguration.class, "healthEndpointWebFluxHandlerMapping", WebEndpointsSupplier.class, HealthEndpointGroups.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(HealthEndpointReactiveWebExtensionConfiguration.WebFluxAdditionalHealthEndpointPathsConfiguration.class).healthEndpointWebFluxHandlerMapping(attributes.get(0), attributes.get(1)))).register(beanFactory);
  }

  public static void registerHealthEndpointReactiveWebExtensionConfiguration(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.health.HealthEndpointReactiveWebExtensionConfiguration", HealthEndpointReactiveWebExtensionConfiguration.class)
        .instanceSupplier(HealthEndpointReactiveWebExtensionConfiguration::new).register(beanFactory);
  }

  public static void registerHealthEndpointReactiveWebExtensionConfiguration_reactiveHealthEndpointWebExtension(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("reactiveHealthEndpointWebExtension", ReactiveHealthEndpointWebExtension.class).withFactoryMethod(HealthEndpointReactiveWebExtensionConfiguration.class, "reactiveHealthEndpointWebExtension", ReactiveHealthContributorRegistry.class, HealthEndpointGroups.class, HealthEndpointProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(HealthEndpointReactiveWebExtensionConfiguration.class).reactiveHealthEndpointWebExtension(attributes.get(0), attributes.get(1), attributes.get(2)))).register(beanFactory);
  }
}
