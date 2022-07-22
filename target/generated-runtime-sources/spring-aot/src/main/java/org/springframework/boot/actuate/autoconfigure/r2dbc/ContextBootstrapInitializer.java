package org.springframework.boot.actuate.autoconfigure.r2dbc;

import java.util.Map;
import org.springframework.aot.beans.factory.BeanDefinitionRegistrar;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public final class ContextBootstrapInitializer {
  public static void registerConnectionFactoryHealthContributorAutoConfiguration(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.r2dbc.ConnectionFactoryHealthContributorAutoConfiguration", ConnectionFactoryHealthContributorAutoConfiguration.class).withConstructor(Map.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new ConnectionFactoryHealthContributorAutoConfiguration(attributes.get(0)))).register(beanFactory);
  }
}
