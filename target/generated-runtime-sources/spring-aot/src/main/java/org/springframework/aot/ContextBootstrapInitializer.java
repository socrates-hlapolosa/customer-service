package org.springframework.aot;

import co.za.ghdlogistics.service.CustomerServiceApplication;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmHeapPressureMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.r2dbc.spi.ConnectionFactory;
import java.lang.Override;
import java.lang.String;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.aot.beans.factory.BeanDefinitionRegistrar;
import org.springframework.aot.context.annotation.ImportAwareBeanPostProcessor;
import org.springframework.aot.context.annotation.InitDestroyBeanPostProcessor;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.LazyInitializationExcludeFilter;
import org.springframework.boot.actuate.autoconfigure.availability.AvailabilityHealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.expose.IncludeExcludeEndpointFilter;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.reactive.WebFluxEndpointManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.info.InfoContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.info.InfoContributorProperties;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.JvmMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.LogbackMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.actuate.autoconfigure.metrics.PropertiesMeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.data.RepositoryMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleProperties;
import org.springframework.boot.actuate.autoconfigure.metrics.r2dbc.ConnectionPoolMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.startup.StartupTimeMetricsListenerAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.task.TaskExecutorMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.web.client.HttpClientMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.web.reactive.WebFluxMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.r2dbc.ConnectionFactoryHealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.system.DiskSpaceHealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.system.DiskSpaceHealthIndicatorProperties;
import org.springframework.boot.actuate.autoconfigure.web.reactive.ReactiveManagementContextAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.invoke.ParameterValueMapper;
import org.springframework.boot.actuate.endpoint.invoker.cache.CachingOperationInvokerAdvisor;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.actuate.endpoint.web.PathMapper;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ExposableControllerEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.web.reactive.ControllerEndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.web.reactive.WebFluxEndpointHandlerMapping;
import org.springframework.boot.actuate.health.PingHealthIndicator;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.metrics.data.DefaultRepositoryTagsProvider;
import org.springframework.boot.actuate.metrics.data.MetricsRepositoryMethodInvocationListener;
import org.springframework.boot.actuate.metrics.data.RepositoryTagsProvider;
import org.springframework.boot.actuate.metrics.startup.StartupTimeMetricsListener;
import org.springframework.boot.actuate.metrics.system.DiskSpaceMetricsBinder;
import org.springframework.boot.actuate.metrics.web.reactive.server.DefaultWebFluxTagsProvider;
import org.springframework.boot.actuate.metrics.web.reactive.server.MetricsWebFilter;
import org.springframework.boot.actuate.metrics.web.reactive.server.WebFluxTagsProvider;
import org.springframework.boot.actuate.system.DiskSpaceHealthIndicator;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.availability.ApplicationAvailabilityAutoConfiguration;
import org.springframework.boot.autoconfigure.codec.CodecProperties;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.context.LifecycleAutoConfiguration;
import org.springframework.boot.autoconfigure.context.LifecycleProperties;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.autoconfigure.netty.NettyAutoConfiguration;
import org.springframework.boot.autoconfigure.netty.NettyProperties;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingProperties;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.autoconfigure.transaction.TransactionProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.embedded.NettyWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveMultipartAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveMultipartProperties;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.autoconfigure.web.reactive.WebSessionIdResolverAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.availability.ApplicationAvailabilityBean;
import org.springframework.boot.context.properties.BoundConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.boot.jackson.JsonMixinModule;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.boot.validation.beanvalidation.MethodValidationExcludeFilter;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.support.DefaultLifecycleProcessor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.AbstractTransactionManagementConfiguration;
import org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.config.WebFluxConfigurationSupport;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.support.HandlerFunctionAdapter;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;
import org.springframework.web.reactive.function.server.support.ServerResponseResultHandler;
import org.springframework.web.reactive.resource.ResourceUrlProvider;
import org.springframework.web.reactive.result.SimpleHandlerAdapter;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityResultHandler;
import org.springframework.web.reactive.result.view.ViewResolutionResultHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.i18n.LocaleContextResolver;
import org.springframework.web.server.session.WebSessionIdResolver;
import org.springframework.web.server.session.WebSessionManager;

public class ContextBootstrapInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
  private ImportAwareBeanPostProcessor createImportAwareBeanPostProcessor() {
    Map<String, String> mappings = new LinkedHashMap<>();
    mappings.put("org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration", "org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration$EnableTransactionManagementConfiguration$JdkDynamicAutoProxyConfiguration");
    return new ImportAwareBeanPostProcessor(mappings);
  }

  private InitDestroyBeanPostProcessor createInitDestroyBeanPostProcessor(
      ConfigurableBeanFactory beanFactory) {
    Map<String, List<String>> initMethods = new LinkedHashMap<>();
    Map<String, List<String>> destroyMethods = new LinkedHashMap<>();
    destroyMethods.put("connectionFactory", List.of("dispose"));
    destroyMethods.put("simpleMeterRegistry", List.of("close"));
    return new InitDestroyBeanPostProcessor(beanFactory, initMethods, destroyMethods);
  }

  @Override
  public void initialize(GenericApplicationContext context) {
    // infrastructure
    DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
    beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
    beanFactory.addBeanPostProcessor(createImportAwareBeanPostProcessor());
    beanFactory.addBeanPostProcessor(createInitDestroyBeanPostProcessor(beanFactory));

    BeanDefinitionRegistrar.of("co.za.ghdlogistics.service.CustomerServiceApplication", CustomerServiceApplication.class)
        .instanceSupplier(CustomerServiceApplication::new).register(beanFactory);
    co.za.ghdlogistics.service.ContextBootstrapInitializer.registerCustomerHttpController(beanFactory);
    org.springframework.boot.autoconfigure.ContextBootstrapInitializer.registerAutoConfigurationPackages_BasePackages(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration", PropertyPlaceholderAutoConfiguration.class)
        .instanceSupplier(PropertyPlaceholderAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("propertySourcesPlaceholderConfigurer", PropertySourcesPlaceholderConfigurer.class).withFactoryMethod(PropertyPlaceholderAutoConfiguration.class, "propertySourcesPlaceholderConfigurer")
        .instanceSupplier(() -> PropertyPlaceholderAutoConfiguration.propertySourcesPlaceholderConfigurer()).register(beanFactory);
    org.springframework.boot.autoconfigure.web.reactive.ContextBootstrapInitializer.registerReactiveWebServerFactoryConfiguration_EmbeddedNetty(beanFactory);
    org.springframework.boot.autoconfigure.web.reactive.ContextBootstrapInitializer.registerEmbeddedNetty_reactorServerResourceFactory(beanFactory);
    org.springframework.boot.autoconfigure.web.reactive.ContextBootstrapInitializer.registerEmbeddedNetty_nettyReactiveWebServerFactory(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration", ReactiveWebServerFactoryAutoConfiguration.class)
        .instanceSupplier(ReactiveWebServerFactoryAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("reactiveWebServerFactoryCustomizer", ReactiveWebServerFactoryCustomizer.class).withFactoryMethod(ReactiveWebServerFactoryAutoConfiguration.class, "reactiveWebServerFactoryCustomizer", ServerProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(ReactiveWebServerFactoryAutoConfiguration.class).reactiveWebServerFactoryCustomizer(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor", ConfigurationPropertiesBindingPostProcessor.class)
        .instanceSupplier(ConfigurationPropertiesBindingPostProcessor::new).customize((bd) -> bd.setRole(2)).register(beanFactory);
    org.springframework.boot.context.properties.ContextBootstrapInitializer.registerConfigurationPropertiesBinder_Factory(beanFactory);
    org.springframework.boot.context.properties.ContextBootstrapInitializer.registerConfigurationPropertiesBinder(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.context.properties.BoundConfigurationProperties", BoundConfigurationProperties.class)
        .instanceSupplier(BoundConfigurationProperties::new).customize((bd) -> bd.setRole(2)).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.context.properties.EnableConfigurationPropertiesRegistrar.methodValidationExcludeFilter", MethodValidationExcludeFilter.class)
        .instanceSupplier(() -> MethodValidationExcludeFilter.byAnnotation(ConfigurationProperties.class)).customize((bd) -> bd.setRole(2)).register(beanFactory);
    BeanDefinitionRegistrar.of("server-org.springframework.boot.autoconfigure.web.ServerProperties", ServerProperties.class)
        .instanceSupplier(ServerProperties::new).register(beanFactory);
    BeanDefinitionRegistrar.of("webServerFactoryCustomizerBeanPostProcessor", WebServerFactoryCustomizerBeanPostProcessor.class)
        .instanceSupplier(WebServerFactoryCustomizerBeanPostProcessor::new).customize((bd) -> bd.setSynthetic(true)).register(beanFactory);
    org.springframework.boot.autoconfigure.jackson.ContextBootstrapInitializer.registerJacksonAutoConfiguration_Jackson2ObjectMapperBuilderCustomizerConfiguration(beanFactory);
    org.springframework.boot.autoconfigure.jackson.ContextBootstrapInitializer.registerJackson2ObjectMapperBuilderCustomizerConfiguration_standardJacksonObjectMapperBuilderCustomizer(beanFactory);
    BeanDefinitionRegistrar.of("spring.jackson-org.springframework.boot.autoconfigure.jackson.JacksonProperties", JacksonProperties.class)
        .instanceSupplier(JacksonProperties::new).register(beanFactory);
    org.springframework.boot.autoconfigure.jackson.ContextBootstrapInitializer.registerJacksonAutoConfiguration_JacksonObjectMapperBuilderConfiguration(beanFactory);
    org.springframework.boot.autoconfigure.jackson.ContextBootstrapInitializer.registerJacksonObjectMapperBuilderConfiguration_jacksonObjectMapperBuilder(beanFactory);
    org.springframework.boot.autoconfigure.jackson.ContextBootstrapInitializer.registerJacksonAutoConfiguration_ParameterNamesModuleConfiguration(beanFactory);
    org.springframework.boot.autoconfigure.jackson.ContextBootstrapInitializer.registerParameterNamesModuleConfiguration_parameterNamesModule(beanFactory);
    org.springframework.boot.autoconfigure.jackson.ContextBootstrapInitializer.registerJacksonAutoConfiguration_JacksonObjectMapperConfiguration(beanFactory);
    org.springframework.boot.autoconfigure.jackson.ContextBootstrapInitializer.registerJacksonObjectMapperConfiguration_jacksonObjectMapper(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration", JacksonAutoConfiguration.class)
        .instanceSupplier(JacksonAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("jsonComponentModule", JsonComponentModule.class).withFactoryMethod(JacksonAutoConfiguration.class, "jsonComponentModule")
        .instanceSupplier(() -> beanFactory.getBean(JacksonAutoConfiguration.class).jsonComponentModule()).register(beanFactory);
    BeanDefinitionRegistrar.of("jsonMixinModule", JsonMixinModule.class).withFactoryMethod(JacksonAutoConfiguration.class, "jsonMixinModule", ApplicationContext.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(JacksonAutoConfiguration.class).jsonMixinModule(attributes.get(0)))).register(beanFactory);
    org.springframework.boot.autoconfigure.http.codec.ContextBootstrapInitializer.registerCodecsAutoConfiguration_DefaultCodecsConfiguration(beanFactory);
    org.springframework.boot.autoconfigure.http.codec.ContextBootstrapInitializer.registerDefaultCodecsConfiguration_defaultCodecCustomizer(beanFactory);
    org.springframework.boot.autoconfigure.http.codec.ContextBootstrapInitializer.registerCodecsAutoConfiguration_JacksonCodecConfiguration(beanFactory);
    org.springframework.boot.autoconfigure.http.codec.ContextBootstrapInitializer.registerJacksonCodecConfiguration_jacksonCodecCustomizer(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration", CodecsAutoConfiguration.class)
        .instanceSupplier(CodecsAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("spring.codec-org.springframework.boot.autoconfigure.codec.CodecProperties", CodecProperties.class)
        .instanceSupplier(CodecProperties::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.web.reactive.ReactiveMultipartAutoConfiguration", ReactiveMultipartAutoConfiguration.class)
        .instanceSupplier(ReactiveMultipartAutoConfiguration::new).register(beanFactory);
    org.springframework.boot.autoconfigure.web.reactive.ContextBootstrapInitializer.registerReactiveMultipartAutoConfiguration_defaultPartHttpMessageReaderCustomizer(beanFactory);
    BeanDefinitionRegistrar.of("spring.webflux.multipart-org.springframework.boot.autoconfigure.web.reactive.ReactiveMultipartProperties", ReactiveMultipartProperties.class)
        .instanceSupplier(ReactiveMultipartProperties::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.web.reactive.WebSessionIdResolverAutoConfiguration", WebSessionIdResolverAutoConfiguration.class).withConstructor(ServerProperties.class, WebFluxProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new WebSessionIdResolverAutoConfiguration(attributes.get(0), attributes.get(1)))).register(beanFactory);
    BeanDefinitionRegistrar.of("webSessionIdResolver", WebSessionIdResolver.class).withFactoryMethod(WebSessionIdResolverAutoConfiguration.class, "webSessionIdResolver")
        .instanceSupplier(() -> beanFactory.getBean(WebSessionIdResolverAutoConfiguration.class).webSessionIdResolver()).register(beanFactory);
    BeanDefinitionRegistrar.of("spring.webflux-org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties", WebFluxProperties.class)
        .instanceSupplier(WebFluxProperties::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration", ErrorWebFluxAutoConfiguration.class).withConstructor(ServerProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new ErrorWebFluxAutoConfiguration(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("errorWebExceptionHandler", ErrorWebExceptionHandler.class).withFactoryMethod(ErrorWebFluxAutoConfiguration.class, "errorWebExceptionHandler", ErrorAttributes.class, WebProperties.class, ObjectProvider.class, ServerCodecConfigurer.class, ApplicationContext.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(ErrorWebFluxAutoConfiguration.class).errorWebExceptionHandler(attributes.get(0), attributes.get(1), attributes.get(2), attributes.get(3), attributes.get(4)))).register(beanFactory);
    BeanDefinitionRegistrar.of("errorAttributes", DefaultErrorAttributes.class).withFactoryMethod(ErrorWebFluxAutoConfiguration.class, "errorAttributes")
        .instanceSupplier(() -> beanFactory.getBean(ErrorWebFluxAutoConfiguration.class).errorAttributes()).register(beanFactory);
    BeanDefinitionRegistrar.of("spring.web-org.springframework.boot.autoconfigure.web.WebProperties", WebProperties.class)
        .instanceSupplier(WebProperties::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration$EnableWebFluxConfiguration", WebFluxAutoConfiguration.EnableWebFluxConfiguration.class).withConstructor(WebFluxProperties.class, WebProperties.class, ServerProperties.class, ObjectProvider.class)
        .instanceSupplier((instanceContext) -> {
          WebFluxAutoConfiguration.EnableWebFluxConfiguration bean = instanceContext.create(beanFactory, (attributes) -> new WebFluxAutoConfiguration.EnableWebFluxConfiguration(attributes.get(0), attributes.get(1), attributes.get(2), attributes.get(3)));
          instanceContext.method("setConfigurers", List.class)
              .resolve(beanFactory, false).ifResolved((attributes) -> bean.setConfigurers(attributes.get(0)));
          return bean;
        }).register(beanFactory);
    BeanDefinitionRegistrar.of("webFluxConversionService", FormattingConversionService.class).withFactoryMethod(WebFluxAutoConfiguration.EnableWebFluxConfiguration.class, "webFluxConversionService")
        .instanceSupplier(() -> beanFactory.getBean(WebFluxAutoConfiguration.EnableWebFluxConfiguration.class).webFluxConversionService()).register(beanFactory);
    BeanDefinitionRegistrar.of("webFluxValidator", Validator.class).withFactoryMethod(WebFluxAutoConfiguration.EnableWebFluxConfiguration.class, "webFluxValidator")
        .instanceSupplier(() -> beanFactory.getBean(WebFluxAutoConfiguration.EnableWebFluxConfiguration.class).webFluxValidator()).register(beanFactory);
    BeanDefinitionRegistrar.of("localeContextResolver", LocaleContextResolver.class).withFactoryMethod(WebFluxAutoConfiguration.EnableWebFluxConfiguration.class, "localeContextResolver")
        .instanceSupplier(() -> beanFactory.getBean(WebFluxAutoConfiguration.EnableWebFluxConfiguration.class).localeContextResolver()).register(beanFactory);
    BeanDefinitionRegistrar.of("webSessionManager", WebSessionManager.class).withFactoryMethod(WebFluxAutoConfiguration.EnableWebFluxConfiguration.class, "webSessionManager", ObjectProvider.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebFluxAutoConfiguration.EnableWebFluxConfiguration.class).webSessionManager(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("webHandler", DispatcherHandler.class).withFactoryMethod(WebFluxConfigurationSupport.class, "webHandler")
        .instanceSupplier(() -> beanFactory.getBean(WebFluxConfigurationSupport.class).webHandler()).register(beanFactory);
    BeanDefinitionRegistrar.of("responseStatusExceptionHandler", WebExceptionHandler.class).withFactoryMethod(WebFluxConfigurationSupport.class, "responseStatusExceptionHandler")
        .instanceSupplier(() -> beanFactory.getBean(WebFluxConfigurationSupport.class).responseStatusExceptionHandler()).register(beanFactory);
    BeanDefinitionRegistrar.of("requestMappingHandlerMapping", RequestMappingHandlerMapping.class).withFactoryMethod(WebFluxConfigurationSupport.class, "requestMappingHandlerMapping", RequestedContentTypeResolver.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebFluxConfigurationSupport.class).requestMappingHandlerMapping(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("webFluxContentTypeResolver", RequestedContentTypeResolver.class).withFactoryMethod(WebFluxConfigurationSupport.class, "webFluxContentTypeResolver")
        .instanceSupplier(() -> beanFactory.getBean(WebFluxConfigurationSupport.class).webFluxContentTypeResolver()).register(beanFactory);
    BeanDefinitionRegistrar.of("routerFunctionMapping", RouterFunctionMapping.class).withFactoryMethod(WebFluxConfigurationSupport.class, "routerFunctionMapping", ServerCodecConfigurer.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebFluxConfigurationSupport.class).routerFunctionMapping(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("resourceHandlerMapping", HandlerMapping.class).withFactoryMethod(WebFluxConfigurationSupport.class, "resourceHandlerMapping", ResourceUrlProvider.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebFluxConfigurationSupport.class).resourceHandlerMapping(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("resourceUrlProvider", ResourceUrlProvider.class).withFactoryMethod(WebFluxConfigurationSupport.class, "resourceUrlProvider")
        .instanceSupplier(() -> beanFactory.getBean(WebFluxConfigurationSupport.class).resourceUrlProvider()).register(beanFactory);
    BeanDefinitionRegistrar.of("requestMappingHandlerAdapter", RequestMappingHandlerAdapter.class).withFactoryMethod(WebFluxConfigurationSupport.class, "requestMappingHandlerAdapter", ReactiveAdapterRegistry.class, ServerCodecConfigurer.class, FormattingConversionService.class, Validator.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebFluxConfigurationSupport.class).requestMappingHandlerAdapter(attributes.get(0), attributes.get(1), attributes.get(2), attributes.get(3)))).register(beanFactory);
    BeanDefinitionRegistrar.of("serverCodecConfigurer", ServerCodecConfigurer.class).withFactoryMethod(WebFluxConfigurationSupport.class, "serverCodecConfigurer")
        .instanceSupplier(() -> beanFactory.getBean(WebFluxConfigurationSupport.class).serverCodecConfigurer()).register(beanFactory);
    BeanDefinitionRegistrar.of("webFluxAdapterRegistry", ReactiveAdapterRegistry.class).withFactoryMethod(WebFluxConfigurationSupport.class, "webFluxAdapterRegistry")
        .instanceSupplier(() -> beanFactory.getBean(WebFluxConfigurationSupport.class).webFluxAdapterRegistry()).register(beanFactory);
    BeanDefinitionRegistrar.of("handlerFunctionAdapter", HandlerFunctionAdapter.class).withFactoryMethod(WebFluxConfigurationSupport.class, "handlerFunctionAdapter")
        .instanceSupplier(() -> beanFactory.getBean(WebFluxConfigurationSupport.class).handlerFunctionAdapter()).register(beanFactory);
    BeanDefinitionRegistrar.of("simpleHandlerAdapter", SimpleHandlerAdapter.class).withFactoryMethod(WebFluxConfigurationSupport.class, "simpleHandlerAdapter")
        .instanceSupplier(() -> beanFactory.getBean(WebFluxConfigurationSupport.class).simpleHandlerAdapter()).register(beanFactory);
    BeanDefinitionRegistrar.of("webFluxWebSocketHandlerAdapter", WebSocketHandlerAdapter.class).withFactoryMethod(WebFluxConfigurationSupport.class, "webFluxWebSocketHandlerAdapter")
        .instanceSupplier(() -> beanFactory.getBean(WebFluxConfigurationSupport.class).webFluxWebSocketHandlerAdapter()).register(beanFactory);
    BeanDefinitionRegistrar.of("responseEntityResultHandler", ResponseEntityResultHandler.class).withFactoryMethod(WebFluxConfigurationSupport.class, "responseEntityResultHandler", ReactiveAdapterRegistry.class, ServerCodecConfigurer.class, RequestedContentTypeResolver.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebFluxConfigurationSupport.class).responseEntityResultHandler(attributes.get(0), attributes.get(1), attributes.get(2)))).register(beanFactory);
    BeanDefinitionRegistrar.of("responseBodyResultHandler", ResponseBodyResultHandler.class).withFactoryMethod(WebFluxConfigurationSupport.class, "responseBodyResultHandler", ReactiveAdapterRegistry.class, ServerCodecConfigurer.class, RequestedContentTypeResolver.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebFluxConfigurationSupport.class).responseBodyResultHandler(attributes.get(0), attributes.get(1), attributes.get(2)))).register(beanFactory);
    BeanDefinitionRegistrar.of("viewResolutionResultHandler", ViewResolutionResultHandler.class).withFactoryMethod(WebFluxConfigurationSupport.class, "viewResolutionResultHandler", ReactiveAdapterRegistry.class, RequestedContentTypeResolver.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebFluxConfigurationSupport.class).viewResolutionResultHandler(attributes.get(0), attributes.get(1)))).register(beanFactory);
    BeanDefinitionRegistrar.of("serverResponseResultHandler", ServerResponseResultHandler.class).withFactoryMethod(WebFluxConfigurationSupport.class, "serverResponseResultHandler", ServerCodecConfigurer.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebFluxConfigurationSupport.class).serverResponseResultHandler(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration$WebFluxConfig", WebFluxAutoConfiguration.WebFluxConfig.class).withConstructor(WebProperties.class, WebFluxProperties.class, ListableBeanFactory.class, ObjectProvider.class, ObjectProvider.class, ObjectProvider.class, ObjectProvider.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new WebFluxAutoConfiguration.WebFluxConfig(attributes.get(0), attributes.get(1), attributes.get(2), attributes.get(3), attributes.get(4), attributes.get(5), attributes.get(6)))).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration$WelcomePageConfiguration", WebFluxAutoConfiguration.WelcomePageConfiguration.class)
        .instanceSupplier(WebFluxAutoConfiguration.WelcomePageConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("welcomePageRouterFunctionMapping", RouterFunctionMapping.class).withFactoryMethod(WebFluxAutoConfiguration.WelcomePageConfiguration.class, "welcomePageRouterFunctionMapping", ApplicationContext.class, WebFluxProperties.class, WebProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebFluxAutoConfiguration.WelcomePageConfiguration.class).welcomePageRouterFunctionMapping(attributes.get(0), attributes.get(1), attributes.get(2)))).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration", WebFluxAutoConfiguration.class)
        .instanceSupplier(WebFluxAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration$AnnotationConfig", HttpHandlerAutoConfiguration.AnnotationConfig.class).withConstructor(ApplicationContext.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new HttpHandlerAutoConfiguration.AnnotationConfig(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("httpHandler", HttpHandler.class).withFactoryMethod(HttpHandlerAutoConfiguration.AnnotationConfig.class, "httpHandler", ObjectProvider.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(HttpHandlerAutoConfiguration.AnnotationConfig.class).httpHandler(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration", HttpHandlerAutoConfiguration.class)
        .instanceSupplier(HttpHandlerAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.availability.ApplicationAvailabilityAutoConfiguration", ApplicationAvailabilityAutoConfiguration.class)
        .instanceSupplier(ApplicationAvailabilityAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("applicationAvailability", ApplicationAvailabilityBean.class).withFactoryMethod(ApplicationAvailabilityAutoConfiguration.class, "applicationAvailability")
        .instanceSupplier(() -> beanFactory.getBean(ApplicationAvailabilityAutoConfiguration.class).applicationAvailability()).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.availability.AvailabilityHealthContributorAutoConfiguration", AvailabilityHealthContributorAutoConfiguration.class)
        .instanceSupplier(AvailabilityHealthContributorAutoConfiguration::new).register(beanFactory);
    org.springframework.boot.autoconfigure.r2dbc.ContextBootstrapInitializer.registerPoolConfiguration_PooledConnectionFactoryConfiguration(beanFactory);
    org.springframework.boot.autoconfigure.r2dbc.ContextBootstrapInitializer.registerPooledConnectionFactoryConfiguration_connectionFactory(beanFactory);
    org.springframework.boot.autoconfigure.r2dbc.ContextBootstrapInitializer.registerConnectionFactoryConfigurations_PoolConfiguration(beanFactory);
    org.springframework.boot.autoconfigure.r2dbc.ContextBootstrapInitializer.registerConnectionFactoryDependentConfiguration(beanFactory);
    org.springframework.boot.autoconfigure.r2dbc.ContextBootstrapInitializer.registerConnectionFactoryDependentConfiguration_r2dbcDatabaseClient(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration", R2dbcAutoConfiguration.class)
        .instanceSupplier(R2dbcAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("spring.r2dbc-org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties", R2dbcProperties.class)
        .instanceSupplier(R2dbcProperties::new).register(beanFactory);
    org.springframework.boot.actuate.autoconfigure.health.ContextBootstrapInitializer.registerHealthEndpointConfiguration(beanFactory);
    org.springframework.boot.actuate.autoconfigure.health.ContextBootstrapInitializer.registerHealthEndpointConfiguration_healthStatusAggregator(beanFactory);
    org.springframework.boot.actuate.autoconfigure.health.ContextBootstrapInitializer.registerHealthEndpointConfiguration_healthHttpCodeStatusMapper(beanFactory);
    org.springframework.boot.actuate.autoconfigure.health.ContextBootstrapInitializer.registerHealthEndpointConfiguration_healthEndpointGroups(beanFactory);
    org.springframework.boot.actuate.autoconfigure.health.ContextBootstrapInitializer.registerHealthEndpointConfiguration_healthContributorRegistry(beanFactory);
    org.springframework.boot.actuate.autoconfigure.health.ContextBootstrapInitializer.registerHealthEndpointConfiguration_healthEndpoint(beanFactory);
    org.springframework.boot.actuate.autoconfigure.health.ContextBootstrapInitializer.registerHealthEndpointConfiguration_healthEndpointGroupsBeanPostProcessor(beanFactory);
    org.springframework.boot.actuate.autoconfigure.health.ContextBootstrapInitializer.registerReactiveHealthEndpointConfiguration(beanFactory);
    org.springframework.boot.actuate.autoconfigure.health.ContextBootstrapInitializer.registerReactiveHealthEndpointConfiguration_reactiveHealthContributorRegistry(beanFactory);
    org.springframework.boot.actuate.autoconfigure.health.ContextBootstrapInitializer.registerHealthEndpointReactiveWebExtensionConfiguration_WebFluxAdditionalHealthEndpointPathsConfiguration(beanFactory);
    org.springframework.boot.actuate.autoconfigure.health.ContextBootstrapInitializer.registerWebFluxAdditionalHealthEndpointPathsConfiguration_healthEndpointWebFluxHandlerMapping(beanFactory);
    org.springframework.boot.actuate.autoconfigure.health.ContextBootstrapInitializer.registerHealthEndpointReactiveWebExtensionConfiguration(beanFactory);
    org.springframework.boot.actuate.autoconfigure.health.ContextBootstrapInitializer.registerHealthEndpointReactiveWebExtensionConfiguration_reactiveHealthEndpointWebExtension(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration", HealthEndpointAutoConfiguration.class)
        .instanceSupplier(HealthEndpointAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("management.endpoint.health-org.springframework.boot.actuate.autoconfigure.health.HealthEndpointProperties", HealthEndpointProperties.class)
        .instanceSupplier(HealthEndpointProperties::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration", ProjectInfoAutoConfiguration.class).withConstructor(ProjectInfoProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new ProjectInfoAutoConfiguration(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("spring.info-org.springframework.boot.autoconfigure.info.ProjectInfoProperties", ProjectInfoProperties.class)
        .instanceSupplier(ProjectInfoProperties::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.info.InfoContributorAutoConfiguration", InfoContributorAutoConfiguration.class)
        .instanceSupplier(InfoContributorAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("management.info-org.springframework.boot.actuate.autoconfigure.info.InfoContributorProperties", InfoContributorProperties.class)
        .instanceSupplier(InfoContributorProperties::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration", EndpointAutoConfiguration.class)
        .instanceSupplier(EndpointAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("endpointOperationParameterMapper", ParameterValueMapper.class).withFactoryMethod(EndpointAutoConfiguration.class, "endpointOperationParameterMapper", ObjectProvider.class, ObjectProvider.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(EndpointAutoConfiguration.class).endpointOperationParameterMapper(attributes.get(0), attributes.get(1)))).register(beanFactory);
    BeanDefinitionRegistrar.of("endpointCachingOperationInvokerAdvisor", CachingOperationInvokerAdvisor.class).withFactoryMethod(EndpointAutoConfiguration.class, "endpointCachingOperationInvokerAdvisor", Environment.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(EndpointAutoConfiguration.class).endpointCachingOperationInvokerAdvisor(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration", WebEndpointAutoConfiguration.class).withConstructor(ApplicationContext.class, WebEndpointProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new WebEndpointAutoConfiguration(attributes.get(0), attributes.get(1)))).register(beanFactory);
    BeanDefinitionRegistrar.of("webEndpointPathMapper", PathMapper.class).withFactoryMethod(WebEndpointAutoConfiguration.class, "webEndpointPathMapper")
        .instanceSupplier(() -> beanFactory.getBean(WebEndpointAutoConfiguration.class).webEndpointPathMapper()).register(beanFactory);
    BeanDefinitionRegistrar.of("endpointMediaTypes", EndpointMediaTypes.class).withFactoryMethod(WebEndpointAutoConfiguration.class, "endpointMediaTypes")
        .instanceSupplier(() -> beanFactory.getBean(WebEndpointAutoConfiguration.class).endpointMediaTypes()).register(beanFactory);
    BeanDefinitionRegistrar.of("webEndpointDiscoverer", WebEndpointDiscoverer.class).withFactoryMethod(WebEndpointAutoConfiguration.class, "webEndpointDiscoverer", ParameterValueMapper.class, EndpointMediaTypes.class, ObjectProvider.class, ObjectProvider.class, ObjectProvider.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebEndpointAutoConfiguration.class).webEndpointDiscoverer(attributes.get(0), attributes.get(1), attributes.get(2), attributes.get(3), attributes.get(4)))).register(beanFactory);
    BeanDefinitionRegistrar.of("controllerEndpointDiscoverer", ControllerEndpointDiscoverer.class).withFactoryMethod(WebEndpointAutoConfiguration.class, "controllerEndpointDiscoverer", ObjectProvider.class, ObjectProvider.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebEndpointAutoConfiguration.class).controllerEndpointDiscoverer(attributes.get(0), attributes.get(1)))).register(beanFactory);
    BeanDefinitionRegistrar.of("pathMappedEndpoints", PathMappedEndpoints.class).withFactoryMethod(WebEndpointAutoConfiguration.class, "pathMappedEndpoints", Collection.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebEndpointAutoConfiguration.class).pathMappedEndpoints(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("webExposeExcludePropertyEndpointFilter", ResolvableType.forClassWithGenerics(IncludeExcludeEndpointFilter.class, ExposableWebEndpoint.class)).withFactoryMethod(WebEndpointAutoConfiguration.class, "webExposeExcludePropertyEndpointFilter")
        .instanceSupplier(() -> beanFactory.getBean(WebEndpointAutoConfiguration.class).webExposeExcludePropertyEndpointFilter()).register(beanFactory);
    BeanDefinitionRegistrar.of("controllerExposeExcludePropertyEndpointFilter", ResolvableType.forClassWithGenerics(IncludeExcludeEndpointFilter.class, ExposableControllerEndpoint.class)).withFactoryMethod(WebEndpointAutoConfiguration.class, "controllerExposeExcludePropertyEndpointFilter")
        .instanceSupplier(() -> beanFactory.getBean(WebEndpointAutoConfiguration.class).controllerExposeExcludePropertyEndpointFilter()).register(beanFactory);
    BeanDefinitionRegistrar.of("management.endpoints.web-org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties", WebEndpointProperties.class)
        .instanceSupplier(WebEndpointProperties::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.system.DiskSpaceHealthContributorAutoConfiguration", DiskSpaceHealthContributorAutoConfiguration.class)
        .instanceSupplier(DiskSpaceHealthContributorAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("diskSpaceHealthIndicator", DiskSpaceHealthIndicator.class).withFactoryMethod(DiskSpaceHealthContributorAutoConfiguration.class, "diskSpaceHealthIndicator", DiskSpaceHealthIndicatorProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(DiskSpaceHealthContributorAutoConfiguration.class).diskSpaceHealthIndicator(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("management.health.diskspace-org.springframework.boot.actuate.autoconfigure.system.DiskSpaceHealthIndicatorProperties", DiskSpaceHealthIndicatorProperties.class)
        .instanceSupplier(DiskSpaceHealthIndicatorProperties::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration", HealthContributorAutoConfiguration.class)
        .instanceSupplier(HealthContributorAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("pingHealthContributor", PingHealthIndicator.class).withFactoryMethod(HealthContributorAutoConfiguration.class, "pingHealthContributor")
        .instanceSupplier(() -> beanFactory.getBean(HealthContributorAutoConfiguration.class).pingHealthContributor()).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration", MetricsAutoConfiguration.class)
        .instanceSupplier(MetricsAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("micrometerClock", Clock.class).withFactoryMethod(MetricsAutoConfiguration.class, "micrometerClock")
        .instanceSupplier(() -> beanFactory.getBean(MetricsAutoConfiguration.class).micrometerClock()).register(beanFactory);
    org.springframework.boot.actuate.autoconfigure.metrics.ContextBootstrapInitializer.registerMetricsAutoConfiguration_meterRegistryPostProcessor(beanFactory);
    BeanDefinitionRegistrar.of("propertiesMeterFilter", PropertiesMeterFilter.class).withFactoryMethod(MetricsAutoConfiguration.class, "propertiesMeterFilter", MetricsProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(MetricsAutoConfiguration.class).propertiesMeterFilter(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("management.metrics-org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties", MetricsProperties.class)
        .instanceSupplier(MetricsProperties::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration", SimpleMetricsExportAutoConfiguration.class)
        .instanceSupplier(SimpleMetricsExportAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("simpleMeterRegistry", SimpleMeterRegistry.class).withFactoryMethod(SimpleMetricsExportAutoConfiguration.class, "simpleMeterRegistry", SimpleConfig.class, Clock.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(SimpleMetricsExportAutoConfiguration.class).simpleMeterRegistry(attributes.get(0), attributes.get(1)))).register(beanFactory);
    BeanDefinitionRegistrar.of("simpleConfig", SimpleConfig.class).withFactoryMethod(SimpleMetricsExportAutoConfiguration.class, "simpleConfig", SimpleProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(SimpleMetricsExportAutoConfiguration.class).simpleConfig(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("management.metrics.export.simple-org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleProperties", SimpleProperties.class)
        .instanceSupplier(SimpleProperties::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration", CompositeMeterRegistryAutoConfiguration.class)
        .instanceSupplier(CompositeMeterRegistryAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.metrics.JvmMetricsAutoConfiguration", JvmMetricsAutoConfiguration.class)
        .instanceSupplier(JvmMetricsAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("jvmGcMetrics", JvmGcMetrics.class).withFactoryMethod(JvmMetricsAutoConfiguration.class, "jvmGcMetrics")
        .instanceSupplier(() -> beanFactory.getBean(JvmMetricsAutoConfiguration.class).jvmGcMetrics()).register(beanFactory);
    BeanDefinitionRegistrar.of("jvmHeapPressureMetrics", JvmHeapPressureMetrics.class).withFactoryMethod(JvmMetricsAutoConfiguration.class, "jvmHeapPressureMetrics")
        .instanceSupplier(() -> beanFactory.getBean(JvmMetricsAutoConfiguration.class).jvmHeapPressureMetrics()).register(beanFactory);
    BeanDefinitionRegistrar.of("jvmMemoryMetrics", JvmMemoryMetrics.class).withFactoryMethod(JvmMetricsAutoConfiguration.class, "jvmMemoryMetrics")
        .instanceSupplier(() -> beanFactory.getBean(JvmMetricsAutoConfiguration.class).jvmMemoryMetrics()).register(beanFactory);
    BeanDefinitionRegistrar.of("jvmThreadMetrics", JvmThreadMetrics.class).withFactoryMethod(JvmMetricsAutoConfiguration.class, "jvmThreadMetrics")
        .instanceSupplier(() -> beanFactory.getBean(JvmMetricsAutoConfiguration.class).jvmThreadMetrics()).register(beanFactory);
    BeanDefinitionRegistrar.of("classLoaderMetrics", ClassLoaderMetrics.class).withFactoryMethod(JvmMetricsAutoConfiguration.class, "classLoaderMetrics")
        .instanceSupplier(() -> beanFactory.getBean(JvmMetricsAutoConfiguration.class).classLoaderMetrics()).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.metrics.LogbackMetricsAutoConfiguration", LogbackMetricsAutoConfiguration.class)
        .instanceSupplier(LogbackMetricsAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("logbackMetrics", LogbackMetrics.class).withFactoryMethod(LogbackMetricsAutoConfiguration.class, "logbackMetrics")
        .instanceSupplier(() -> beanFactory.getBean(LogbackMetricsAutoConfiguration.class).logbackMetrics()).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration", SystemMetricsAutoConfiguration.class)
        .instanceSupplier(SystemMetricsAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("uptimeMetrics", UptimeMetrics.class).withFactoryMethod(SystemMetricsAutoConfiguration.class, "uptimeMetrics")
        .instanceSupplier(() -> beanFactory.getBean(SystemMetricsAutoConfiguration.class).uptimeMetrics()).register(beanFactory);
    BeanDefinitionRegistrar.of("processorMetrics", ProcessorMetrics.class).withFactoryMethod(SystemMetricsAutoConfiguration.class, "processorMetrics")
        .instanceSupplier(() -> beanFactory.getBean(SystemMetricsAutoConfiguration.class).processorMetrics()).register(beanFactory);
    BeanDefinitionRegistrar.of("fileDescriptorMetrics", FileDescriptorMetrics.class).withFactoryMethod(SystemMetricsAutoConfiguration.class, "fileDescriptorMetrics")
        .instanceSupplier(() -> beanFactory.getBean(SystemMetricsAutoConfiguration.class).fileDescriptorMetrics()).register(beanFactory);
    BeanDefinitionRegistrar.of("diskSpaceMetrics", DiskSpaceMetricsBinder.class).withFactoryMethod(SystemMetricsAutoConfiguration.class, "diskSpaceMetrics", MetricsProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(SystemMetricsAutoConfiguration.class).diskSpaceMetrics(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.metrics.data.RepositoryMetricsAutoConfiguration", RepositoryMetricsAutoConfiguration.class).withConstructor(MetricsProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new RepositoryMetricsAutoConfiguration(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("repositoryTagsProvider", DefaultRepositoryTagsProvider.class).withFactoryMethod(RepositoryMetricsAutoConfiguration.class, "repositoryTagsProvider")
        .instanceSupplier(() -> beanFactory.getBean(RepositoryMetricsAutoConfiguration.class).repositoryTagsProvider()).register(beanFactory);
    BeanDefinitionRegistrar.of("metricsRepositoryMethodInvocationListener", MetricsRepositoryMethodInvocationListener.class).withFactoryMethod(RepositoryMetricsAutoConfiguration.class, "metricsRepositoryMethodInvocationListener", ObjectProvider.class, RepositoryTagsProvider.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(RepositoryMetricsAutoConfiguration.class).metricsRepositoryMethodInvocationListener(attributes.get(0), attributes.get(1)))).register(beanFactory);
    org.springframework.boot.actuate.autoconfigure.metrics.data.ContextBootstrapInitializer.registerRepositoryMetricsAutoConfiguration_metricsRepositoryMethodInvocationListenerBeanPostProcessor(beanFactory);
    org.springframework.boot.actuate.autoconfigure.metrics.integration.ContextBootstrapInitializer.registerIntegrationMetricsAutoConfiguration(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.metrics.r2dbc.ConnectionPoolMetricsAutoConfiguration", ConnectionPoolMetricsAutoConfiguration.class)
        .instanceSupplier((instanceContext) -> {
          ConnectionPoolMetricsAutoConfiguration bean = new ConnectionPoolMetricsAutoConfiguration();
          instanceContext.method("bindConnectionPoolsToRegistry", Map.class, MeterRegistry.class)
              .invoke(beanFactory, (attributes) -> bean.bindConnectionPoolsToRegistry(attributes.get(0), attributes.get(1)));
          return bean;
        }).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.metrics.startup.StartupTimeMetricsListenerAutoConfiguration", StartupTimeMetricsListenerAutoConfiguration.class)
        .instanceSupplier(StartupTimeMetricsListenerAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("startupTimeMetrics", StartupTimeMetricsListener.class).withFactoryMethod(StartupTimeMetricsListenerAutoConfiguration.class, "startupTimeMetrics", MeterRegistry.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(StartupTimeMetricsListenerAutoConfiguration.class).startupTimeMetrics(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration", TaskExecutionAutoConfiguration.class)
        .instanceSupplier(TaskExecutionAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("taskExecutorBuilder", TaskExecutorBuilder.class).withFactoryMethod(TaskExecutionAutoConfiguration.class, "taskExecutorBuilder", TaskExecutionProperties.class, ObjectProvider.class, ObjectProvider.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(TaskExecutionAutoConfiguration.class).taskExecutorBuilder(attributes.get(0), attributes.get(1), attributes.get(2)))).register(beanFactory);
    BeanDefinitionRegistrar.of("applicationTaskExecutor", ThreadPoolTaskExecutor.class).withFactoryMethod(TaskExecutionAutoConfiguration.class, "applicationTaskExecutor", TaskExecutorBuilder.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(TaskExecutionAutoConfiguration.class).applicationTaskExecutor(attributes.get(0)))).customize((bd) -> bd.setLazyInit(true)).register(beanFactory);
    BeanDefinitionRegistrar.of("spring.task.execution-org.springframework.boot.autoconfigure.task.TaskExecutionProperties", TaskExecutionProperties.class)
        .instanceSupplier(TaskExecutionProperties::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration", TaskSchedulingAutoConfiguration.class)
        .instanceSupplier(TaskSchedulingAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("scheduledBeanLazyInitializationExcludeFilter", LazyInitializationExcludeFilter.class).withFactoryMethod(TaskSchedulingAutoConfiguration.class, "scheduledBeanLazyInitializationExcludeFilter")
        .instanceSupplier(() -> TaskSchedulingAutoConfiguration.scheduledBeanLazyInitializationExcludeFilter()).register(beanFactory);
    BeanDefinitionRegistrar.of("taskSchedulerBuilder", TaskSchedulerBuilder.class).withFactoryMethod(TaskSchedulingAutoConfiguration.class, "taskSchedulerBuilder", TaskSchedulingProperties.class, ObjectProvider.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(TaskSchedulingAutoConfiguration.class).taskSchedulerBuilder(attributes.get(0), attributes.get(1)))).register(beanFactory);
    BeanDefinitionRegistrar.of("spring.task.scheduling-org.springframework.boot.autoconfigure.task.TaskSchedulingProperties", TaskSchedulingProperties.class)
        .instanceSupplier(TaskSchedulingProperties::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.metrics.task.TaskExecutorMetricsAutoConfiguration", TaskExecutorMetricsAutoConfiguration.class)
        .instanceSupplier((instanceContext) -> {
          TaskExecutorMetricsAutoConfiguration bean = new TaskExecutorMetricsAutoConfiguration();
          instanceContext.method("bindTaskExecutorsToRegistry", Map.class, MeterRegistry.class)
              .invoke(beanFactory, (attributes) -> bean.bindTaskExecutorsToRegistry(attributes.get(0), attributes.get(1)));
          return bean;
        }).register(beanFactory);
    org.springframework.boot.actuate.autoconfigure.metrics.web.client.ContextBootstrapInitializer.registerWebClientMetricsConfiguration(beanFactory);
    org.springframework.boot.actuate.autoconfigure.metrics.web.client.ContextBootstrapInitializer.registerWebClientMetricsConfiguration_defaultWebClientExchangeTagsProvider(beanFactory);
    org.springframework.boot.actuate.autoconfigure.metrics.web.client.ContextBootstrapInitializer.registerWebClientMetricsConfiguration_metricsWebClientCustomizer(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.metrics.web.client.HttpClientMetricsAutoConfiguration", HttpClientMetricsAutoConfiguration.class)
        .instanceSupplier(HttpClientMetricsAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("metricsHttpClientUriTagFilter", MeterFilter.class).withFactoryMethod(HttpClientMetricsAutoConfiguration.class, "metricsHttpClientUriTagFilter", MetricsProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(HttpClientMetricsAutoConfiguration.class).metricsHttpClientUriTagFilter(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.metrics.web.reactive.WebFluxMetricsAutoConfiguration", WebFluxMetricsAutoConfiguration.class).withConstructor(MetricsProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new WebFluxMetricsAutoConfiguration(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("webFluxTagsProvider", DefaultWebFluxTagsProvider.class).withFactoryMethod(WebFluxMetricsAutoConfiguration.class, "webFluxTagsProvider", ObjectProvider.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebFluxMetricsAutoConfiguration.class).webFluxTagsProvider(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("webfluxMetrics", MetricsWebFilter.class).withFactoryMethod(WebFluxMetricsAutoConfiguration.class, "webfluxMetrics", MeterRegistry.class, WebFluxTagsProvider.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebFluxMetricsAutoConfiguration.class).webfluxMetrics(attributes.get(0), attributes.get(1)))).register(beanFactory);
    BeanDefinitionRegistrar.of("metricsHttpServerUriTagFilter", MeterFilter.class).withFactoryMethod(WebFluxMetricsAutoConfiguration.class, "metricsHttpServerUriTagFilter")
        .instanceSupplier(() -> beanFactory.getBean(WebFluxMetricsAutoConfiguration.class).metricsHttpServerUriTagFilter()).register(beanFactory);
    org.springframework.boot.actuate.autoconfigure.r2dbc.ContextBootstrapInitializer.registerConnectionFactoryHealthContributorAutoConfiguration(beanFactory);
    BeanDefinitionRegistrar.of("r2dbcHealthContributor", ReactiveHealthContributor.class).withFactoryMethod(ConnectionFactoryHealthContributorAutoConfiguration.class, "r2dbcHealthContributor")
        .instanceSupplier(() -> beanFactory.getBean(ConnectionFactoryHealthContributorAutoConfiguration.class).r2dbcHealthContributor()).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.web.reactive.ReactiveManagementContextAutoConfiguration", ReactiveManagementContextAutoConfiguration.class)
        .instanceSupplier(ReactiveManagementContextAutoConfiguration::new).register(beanFactory);
    org.springframework.boot.actuate.autoconfigure.web.reactive.ContextBootstrapInitializer.registerReactiveManagementContextAutoConfiguration_reactiveWebChildContextFactory(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.aop.AopAutoConfiguration", AopAutoConfiguration.class)
        .instanceSupplier(AopAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration", ConfigurationPropertiesAutoConfiguration.class)
        .instanceSupplier(ConfigurationPropertiesAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.context.LifecycleAutoConfiguration", LifecycleAutoConfiguration.class)
        .instanceSupplier(LifecycleAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("lifecycleProcessor", DefaultLifecycleProcessor.class).withFactoryMethod(LifecycleAutoConfiguration.class, "defaultLifecycleProcessor", LifecycleProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(LifecycleAutoConfiguration.class).defaultLifecycleProcessor(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("spring.lifecycle-org.springframework.boot.autoconfigure.context.LifecycleProperties", LifecycleProperties.class)
        .instanceSupplier(LifecycleProperties::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration", PersistenceExceptionTranslationAutoConfiguration.class)
        .instanceSupplier(PersistenceExceptionTranslationAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("persistenceExceptionTranslationPostProcessor", PersistenceExceptionTranslationPostProcessor.class).withFactoryMethod(PersistenceExceptionTranslationAutoConfiguration.class, "persistenceExceptionTranslationPostProcessor", Environment.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> PersistenceExceptionTranslationAutoConfiguration.persistenceExceptionTranslationPostProcessor(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration", R2dbcDataAutoConfiguration.class).withConstructor(DatabaseClient.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new R2dbcDataAutoConfiguration(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("r2dbcEntityTemplate", R2dbcEntityTemplate.class).withFactoryMethod(R2dbcDataAutoConfiguration.class, "r2dbcEntityTemplate", R2dbcConverter.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(R2dbcDataAutoConfiguration.class).r2dbcEntityTemplate(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("r2dbcMappingContext", R2dbcMappingContext.class).withFactoryMethod(R2dbcDataAutoConfiguration.class, "r2dbcMappingContext", ObjectProvider.class, R2dbcCustomConversions.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(R2dbcDataAutoConfiguration.class).r2dbcMappingContext(attributes.get(0), attributes.get(1)))).register(beanFactory);
    BeanDefinitionRegistrar.of("r2dbcConverter", MappingR2dbcConverter.class).withFactoryMethod(R2dbcDataAutoConfiguration.class, "r2dbcConverter", R2dbcMappingContext.class, R2dbcCustomConversions.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(R2dbcDataAutoConfiguration.class).r2dbcConverter(attributes.get(0), attributes.get(1)))).register(beanFactory);
    BeanDefinitionRegistrar.of("r2dbcCustomConversions", R2dbcCustomConversions.class).withFactoryMethod(R2dbcDataAutoConfiguration.class, "r2dbcCustomConversions")
        .instanceSupplier(() -> beanFactory.getBean(R2dbcDataAutoConfiguration.class).r2dbcCustomConversions()).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.data.r2dbc.R2dbcRepositoriesAutoConfiguration", R2dbcRepositoriesAutoConfiguration.class)
        .instanceSupplier(R2dbcRepositoriesAutoConfiguration::new).register(beanFactory);
    co.za.ghdlogistics.service.ContextBootstrapInitializer.registerCustomerRepository(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.netty.NettyAutoConfiguration", NettyAutoConfiguration.class).withConstructor(NettyProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> new NettyAutoConfiguration(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("spring.netty-org.springframework.boot.autoconfigure.netty.NettyProperties", NettyProperties.class)
        .instanceSupplier(NettyProperties::new).register(beanFactory);
    org.springframework.boot.autoconfigure.sql.init.ContextBootstrapInitializer.registerR2dbcInitializationConfiguration(beanFactory);
    org.springframework.boot.autoconfigure.sql.init.ContextBootstrapInitializer.registerR2dbcInitializationConfiguration_r2dbcScriptDatabaseInitializer(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration", SqlInitializationAutoConfiguration.class)
        .instanceSupplier(SqlInitializationAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("spring.sql.init-org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties", SqlInitializationProperties.class)
        .instanceSupplier(SqlInitializationProperties::new).register(beanFactory);
    org.springframework.boot.sql.init.dependency.ContextBootstrapInitializer.registerDatabaseInitializationDependencyConfigurer_DependsOnDatabaseInitializationPostProcessor(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.r2dbc.R2dbcTransactionManagerAutoConfiguration", R2dbcTransactionManagerAutoConfiguration.class)
        .instanceSupplier(R2dbcTransactionManagerAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("connectionFactoryTransactionManager", R2dbcTransactionManager.class).withFactoryMethod(R2dbcTransactionManagerAutoConfiguration.class, "connectionFactoryTransactionManager", ConnectionFactory.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(R2dbcTransactionManagerAutoConfiguration.class).connectionFactoryTransactionManager(attributes.get(0)))).register(beanFactory);
    org.springframework.transaction.annotation.ContextBootstrapInitializer.registerProxyTransactionManagementConfiguration(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.transaction.config.internalTransactionAdvisor", BeanFactoryTransactionAttributeSourceAdvisor.class).withFactoryMethod(ProxyTransactionManagementConfiguration.class, "transactionAdvisor", TransactionAttributeSource.class, TransactionInterceptor.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(ProxyTransactionManagementConfiguration.class).transactionAdvisor(attributes.get(0), attributes.get(1)))).customize((bd) -> bd.setRole(2)).register(beanFactory);
    BeanDefinitionRegistrar.of("transactionAttributeSource", TransactionAttributeSource.class).withFactoryMethod(ProxyTransactionManagementConfiguration.class, "transactionAttributeSource")
        .instanceSupplier(() -> beanFactory.getBean(ProxyTransactionManagementConfiguration.class).transactionAttributeSource()).customize((bd) -> bd.setRole(2)).register(beanFactory);
    BeanDefinitionRegistrar.of("transactionInterceptor", TransactionInterceptor.class).withFactoryMethod(ProxyTransactionManagementConfiguration.class, "transactionInterceptor", TransactionAttributeSource.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(ProxyTransactionManagementConfiguration.class).transactionInterceptor(attributes.get(0)))).customize((bd) -> bd.setRole(2)).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.transaction.config.internalTransactionalEventListenerFactory", TransactionalEventListenerFactory.class).withFactoryMethod(AbstractTransactionManagementConfiguration.class, "transactionalEventListenerFactory")
        .instanceSupplier(() -> AbstractTransactionManagementConfiguration.transactionalEventListenerFactory()).customize((bd) -> bd.setRole(2)).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration$EnableTransactionManagementConfiguration$JdkDynamicAutoProxyConfiguration", TransactionAutoConfiguration.EnableTransactionManagementConfiguration.JdkDynamicAutoProxyConfiguration.class)
        .instanceSupplier(TransactionAutoConfiguration.EnableTransactionManagementConfiguration.JdkDynamicAutoProxyConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.aop.config.internalAutoProxyCreator", InfrastructureAdvisorAutoProxyCreator.class)
        .instanceSupplier(InfrastructureAdvisorAutoProxyCreator::new).customize((bd) -> {
      bd.setRole(2);
      bd.getPropertyValues().addPropertyValue("order", -2147483648);
    }).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration$EnableTransactionManagementConfiguration", TransactionAutoConfiguration.EnableTransactionManagementConfiguration.class)
        .instanceSupplier(TransactionAutoConfiguration.EnableTransactionManagementConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration", TransactionAutoConfiguration.class)
        .instanceSupplier(TransactionAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("platformTransactionManagerCustomizers", TransactionManagerCustomizers.class).withFactoryMethod(TransactionAutoConfiguration.class, "platformTransactionManagerCustomizers", ObjectProvider.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(TransactionAutoConfiguration.class).platformTransactionManagerCustomizers(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("transactionalOperator", TransactionalOperator.class).withFactoryMethod(TransactionAutoConfiguration.class, "transactionalOperator", ReactiveTransactionManager.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(TransactionAutoConfiguration.class).transactionalOperator(attributes.get(0)))).register(beanFactory);
    BeanDefinitionRegistrar.of("spring.transaction-org.springframework.boot.autoconfigure.transaction.TransactionProperties", TransactionProperties.class)
        .instanceSupplier(TransactionProperties::new).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration$NettyWebServerFactoryCustomizerConfiguration", EmbeddedWebServerFactoryCustomizerAutoConfiguration.NettyWebServerFactoryCustomizerConfiguration.class)
        .instanceSupplier(EmbeddedWebServerFactoryCustomizerAutoConfiguration.NettyWebServerFactoryCustomizerConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("nettyWebServerFactoryCustomizer", NettyWebServerFactoryCustomizer.class).withFactoryMethod(EmbeddedWebServerFactoryCustomizerAutoConfiguration.NettyWebServerFactoryCustomizerConfiguration.class, "nettyWebServerFactoryCustomizer", Environment.class, ServerProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(EmbeddedWebServerFactoryCustomizerAutoConfiguration.NettyWebServerFactoryCustomizerConfiguration.class).nettyWebServerFactoryCustomizer(attributes.get(0), attributes.get(1)))).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration", EmbeddedWebServerFactoryCustomizerAutoConfiguration.class)
        .instanceSupplier(EmbeddedWebServerFactoryCustomizerAutoConfiguration::new).register(beanFactory);
    org.springframework.boot.autoconfigure.web.reactive.function.client.ContextBootstrapInitializer.registerClientHttpConnectorConfiguration_ReactorNetty(beanFactory);
    org.springframework.boot.autoconfigure.web.reactive.function.client.ContextBootstrapInitializer.registerReactorNetty_reactorClientHttpConnector(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration", ClientHttpConnectorAutoConfiguration.class)
        .instanceSupplier(ClientHttpConnectorAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("clientConnectorCustomizer", WebClientCustomizer.class).withFactoryMethod(ClientHttpConnectorAutoConfiguration.class, "clientConnectorCustomizer", ClientHttpConnector.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(ClientHttpConnectorAutoConfiguration.class).clientConnectorCustomizer(attributes.get(0)))).customize((bd) -> bd.setLazyInit(true)).register(beanFactory);
    org.springframework.boot.autoconfigure.web.reactive.function.client.ContextBootstrapInitializer.registerWebClientAutoConfiguration_WebClientCodecsConfiguration(beanFactory);
    org.springframework.boot.autoconfigure.web.reactive.function.client.ContextBootstrapInitializer.registerWebClientCodecsConfiguration_exchangeStrategiesCustomizer(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration", WebClientAutoConfiguration.class)
        .instanceSupplier(WebClientAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("webClientBuilder", WebClient.Builder.class).withFactoryMethod(WebClientAutoConfiguration.class, "webClientBuilder", ObjectProvider.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebClientAutoConfiguration.class).webClientBuilder(attributes.get(0)))).customize((bd) -> bd.setScope("prototype")).register(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.endpoint.web.reactive.WebFluxEndpointManagementContextConfiguration", WebFluxEndpointManagementContextConfiguration.class)
        .instanceSupplier(WebFluxEndpointManagementContextConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("webEndpointReactiveHandlerMapping", WebFluxEndpointHandlerMapping.class).withFactoryMethod(WebFluxEndpointManagementContextConfiguration.class, "webEndpointReactiveHandlerMapping", WebEndpointsSupplier.class, ControllerEndpointsSupplier.class, EndpointMediaTypes.class, CorsEndpointProperties.class, WebEndpointProperties.class, Environment.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebFluxEndpointManagementContextConfiguration.class).webEndpointReactiveHandlerMapping(attributes.get(0), attributes.get(1), attributes.get(2), attributes.get(3), attributes.get(4), attributes.get(5)))).register(beanFactory);
    BeanDefinitionRegistrar.of("controllerEndpointHandlerMapping", ControllerEndpointHandlerMapping.class).withFactoryMethod(WebFluxEndpointManagementContextConfiguration.class, "controllerEndpointHandlerMapping", ControllerEndpointsSupplier.class, CorsEndpointProperties.class, WebEndpointProperties.class)
        .instanceSupplier((instanceContext) -> instanceContext.create(beanFactory, (attributes) -> beanFactory.getBean(WebFluxEndpointManagementContextConfiguration.class).controllerEndpointHandlerMapping(attributes.get(0), attributes.get(1), attributes.get(2)))).register(beanFactory);
    BeanDefinitionRegistrar.of("management.endpoints.web.cors-org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties", CorsEndpointProperties.class)
        .instanceSupplier(CorsEndpointProperties::new).register(beanFactory);
    org.springframework.boot.actuate.autoconfigure.web.server.ContextBootstrapInitializer.registerSameManagementContextConfiguration_EnableSameManagementContextConfiguration(beanFactory);
    org.springframework.boot.actuate.autoconfigure.web.server.ContextBootstrapInitializer.registerManagementContextAutoConfiguration_SameManagementContextConfiguration(beanFactory);
    BeanDefinitionRegistrar.of("org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration", ManagementContextAutoConfiguration.class)
        .instanceSupplier(ManagementContextAutoConfiguration::new).register(beanFactory);
    BeanDefinitionRegistrar.of("management.server-org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties", ManagementServerProperties.class)
        .instanceSupplier(ManagementServerProperties::new).register(beanFactory);
  }
}
