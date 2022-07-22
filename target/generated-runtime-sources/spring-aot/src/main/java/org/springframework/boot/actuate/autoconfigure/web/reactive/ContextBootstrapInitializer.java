package org.springframework.boot.actuate.autoconfigure.web.reactive;

import org.springframework.aot.beans.factory.BeanDefinitionRegistrar;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public final class ContextBootstrapInitializer {
  public static void registerReactiveManagementContextAutoConfiguration_reactiveWebChildContextFactory(
      DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("reactiveWebChildContextFactory", ReactiveManagementContextFactory.class).withFactoryMethod(ReactiveManagementContextAutoConfiguration.class, "reactiveWebChildContextFactory")
        .instanceSupplier(() -> beanFactory.getBean(ReactiveManagementContextAutoConfiguration.class).reactiveWebChildContextFactory()).register(beanFactory);
  }
}
