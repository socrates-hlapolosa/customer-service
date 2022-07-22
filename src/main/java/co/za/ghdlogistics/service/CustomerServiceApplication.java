package co.za.ghdlogistics.service;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }

    @Bean
    ApplicationListener<ApplicationReadyEvent> applicationReadyEventApplicationListener(CustomerRepository repository) {

        return new ApplicationListener<ApplicationReadyEvent>() {
            @Override
            public void onApplicationEvent(ApplicationReadyEvent event) {
                Flux.just("Socrates", "Brain", "Patric", "Thori")
                        .map(name -> new Customer(null, name))
                        .flatMap(repository::save)
                        .subscribe(System.out::println);

            }
        };
    }
}

@Controller
@ResponseBody
@RequiredArgsConstructor
class CustomerHttpController {

    private final CustomerRepository customerRepository;

    @GetMapping("/customer")
    Flux<Customer> get() {
        return this.customerRepository.findAll();
    }
}

@NoArgsConstructor
@Data
class Customer {
    @Id
    Integer id;
    String name;

    public Customer(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}