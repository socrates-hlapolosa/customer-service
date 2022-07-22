package org.springframework.boot.autoconfigure.sql.init;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.aot.beans.factory.BeanDefinitionRegistrar;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public final class ContextBootstrapInitializer {
  public static void registerR2dbcInitializationConfiguration(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.sql.init.R2dbcInitializationConfiguration", R2dbcInitializationConfiguration.class)
        .instanceSupplier(R2dbcInitializationConfiguration::new).register(beanFactory);
  }

  public static void registerR2dbcInitializationConfiguration_r2dbcScriptDatabaseInitializer(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("r2dbcScriptDatabaseInitializer", SqlR2dbcScriptDatabaseInitializer.class).withFactoryMethod(R2dbcInitializationConfiguration.class, "r2dbcScriptDatabaseInitializer", ConnectionFactory.class, SqlInitializationProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(R2dbcInitializationConfiguration.class).r2dbcScriptDatabaseInitializer(attributes.get(0), attributes.get(1)))).register(beanFactory);
  }
}
