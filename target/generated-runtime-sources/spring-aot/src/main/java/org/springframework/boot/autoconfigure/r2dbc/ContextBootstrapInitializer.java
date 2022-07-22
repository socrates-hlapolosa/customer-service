package org.springframework.boot.autoconfigure.r2dbc;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.aot.beans.factory.BeanDefinitionRegistrar;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.r2dbc.core.DatabaseClient;

public final class ContextBootstrapInitializer {
  public static void registerPoolConfiguration_PooledConnectionFactoryConfiguration(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryConfigurations$PoolConfiguration$PooledConnectionFactoryConfiguration", ConnectionFactoryConfigurations.PoolConfiguration.PooledConnectionFactoryConfiguration.class)
        .instanceSupplier(ConnectionFactoryConfigurations.PoolConfiguration.PooledConnectionFactoryConfiguration::new).register(beanFactory);
  }

  public static void registerPooledConnectionFactoryConfiguration_connectionFactory(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("connectionFactory", ConnectionPool.class).withFactoryMethod(ConnectionFactoryConfigurations.PoolConfiguration.PooledConnectionFactoryConfiguration.class, "connectionFactory", R2dbcProperties.class, ResourceLoader.class, ObjectProvider.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(ConnectionFactoryConfigurations.PoolConfiguration.PooledConnectionFactoryConfiguration.class).connectionFactory(attributes.get(0), attributes.get(1), attributes.get(2)))).register(beanFactory);
  }

  public static void registerConnectionFactoryConfigurations_PoolConfiguration(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryConfigurations$PoolConfiguration", ConnectionFactoryConfigurations.PoolConfiguration.class)
        .instanceSupplier(ConnectionFactoryConfigurations.PoolConfiguration::new).register(beanFactory);
  }

  public static void registerConnectionFactoryDependentConfiguration(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryDependentConfiguration", ConnectionFactoryDependentConfiguration.class)
        .instanceSupplier(ConnectionFactoryDependentConfiguration::new).register(beanFactory);
  }

  public static void registerConnectionFactoryDependentConfiguration_r2dbcDatabaseClient(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("r2dbcDatabaseClient", DatabaseClient.class).withFactoryMethod(ConnectionFactoryDependentConfiguration.class, "r2dbcDatabaseClient", ConnectionFactory.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(ConnectionFactoryDependentConfiguration.class).r2dbcDatabaseClient(attributes.get(0)))).register(beanFactory);
  }
}
