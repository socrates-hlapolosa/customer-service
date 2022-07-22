package co.za.ghdlogistics.service;

import java.lang.Class;
import java.lang.Integer;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.springframework.aot.beans.factory.BeanDefinitionRegistrar;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactoryBean;
import org.springframework.data.repository.core.support.PropertiesBasedNamedQueries;
import org.springframework.data.repository.core.support.RepositoryFragmentsFactoryBean;
import org.springframework.data.repository.query.QueryLookupStrategy;

public final class ContextBootstrapInitializer {
  public static void registerCustomerHttpController(DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("customerHttpController", CustomerHttpController.class).withConstructor(CustomerRepository.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new CustomerHttpController(attributes.get(0)))).register(beanFactory);
  }

  public static void registerCustomerRepository(DefaultListableBeanFactory beanFactory) {
    BeanDefinitionRegistrar.of("customerRepository", ResolvableType.forClassWithGenerics(R2dbcRepositoryFactoryBean.class, CustomerRepository.class, Customer.class, Integer.class)).withConstructor(Class.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new R2dbcRepositoryFactoryBean(attributes.get(0)))).customize((bd) -> {
      bd.getConstructorArgumentValues().addIndexedArgumentValue(0, "co.za.ghdlogistics.service.CustomerRepository");
      MutablePropertyValues propertyValues = bd.getPropertyValues();
      propertyValues.addPropertyValue("queryLookupStrategyKey", QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND);
      propertyValues.addPropertyValue("lazyInit", false);
      propertyValues.addPropertyValue("namedQueries", BeanDefinitionRegistrar.inner(PropertiesBasedNamedQueries.class).withConstructor(Properties.class)
          .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new PropertiesBasedNamedQueries(attributes.get(0)))).customize((bd_) -> bd_.getConstructorArgumentValues().addIndexedArgumentValue(0, BeanDefinitionRegistrar.inner(PropertiesFactoryBean.class)
          .instanceSupplier(PropertiesFactoryBean::new).customize((bd__) -> {
        MutablePropertyValues propertyValues__ = bd__.getPropertyValues();
        propertyValues__.addPropertyValue("locations", "classpath*:META-INF/r2dbc-named-queries.properties");
        propertyValues__.addPropertyValue("ignoreResourceNotFound", true);
      }).toBeanDefinition())).toBeanDefinition());
      propertyValues.addPropertyValue("repositoryFragments", BeanDefinitionRegistrar.inner(RepositoryFragmentsFactoryBean.class).withConstructor(List.class)
          .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new RepositoryFragmentsFactoryBean(attributes.get(0)))).customize((bd_) -> bd_.getConstructorArgumentValues().addIndexedArgumentValue(0, Collections.emptyList())).toBeanDefinition());
      propertyValues.addPropertyValue("entityOperations", new RuntimeBeanReference("r2dbcEntityTemplate"));
    }).register(beanFactory);
  }
}
