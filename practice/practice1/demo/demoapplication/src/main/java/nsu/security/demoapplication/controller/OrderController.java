package nsu.security.demoapplication.controller;

import nsu.security.demoapplication.model.Order;
import nsu.security.demoapplication.model.OrderImport;
import nsu.security.demoapplication.repository.OrderRepository;
import nsu.security.demoapplication.repository.OrderJdbcRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.util.List;


@RestController
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderJdbcRepository orderJdbcRepository;

    public OrderController(OrderRepository orderRepository, OrderJdbcRepository orderJdbcRepository) {
        this.orderRepository = orderRepository;
        this.orderJdbcRepository = orderJdbcRepository;
    }
    
    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/orders")
    public Long createOrder(@RequestBody Order order) {
        Order saved = orderRepository.save(order);
        return saved.getId();
    }

    @PostMapping("/ordersql")
    public Long createOrderSql(@RequestBody Order order) {
        Order saved = orderRepository.insertOrder(order.getProductType().name(), order.getQuantity());
        return saved.getId();
    }

    @GetMapping("/orders/jdbc/{id}")
    public List<Order> getOrdersJdbc(@PathVariable String id) {
        return orderJdbcRepository.getOrders(id);
    }

    /**
     * Импорт заказа. Cистема присылает сериализованный объект Order.
     * Метод с уязвимостью Java Deserialization
     */
    @PostMapping(value = "/orders/import", consumes = "application/octet-stream")
    public ResponseEntity<String> importOrder(@RequestBody byte[] data) {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            Object obj = ois.readObject();

            if (obj instanceof OrderImport dto) {
                Order order = new Order();
                order.setProductType(dto.getProductType());
                order.setQuantity(dto.getQuantity());
                Order saved = orderRepository.save(order);
                return ResponseEntity.ok("id = " + saved.getId());
            }

            return ResponseEntity.badRequest().body("Unknown payload type");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }

    /**
     * Импорт заказа. Cистема присылает сериализованный объект Order.
     * Метод без уязвимости Java Deserialization
     */
    @PostMapping(value = "/orders/import-correct", consumes = "application/octet-stream")
    public ResponseEntity<String> importOrderCorrect(@RequestBody byte[] data) {
        // Создаём фильтр: разрешаем только OrderImport и его зависимости, всё остальное отклоняем
        ObjectInputFilter filter = ObjectInputFilter.Config.createFilter(
                "nsu.security.demoapplication.model.OrderImport;" +  // ваш DTO
                        "nsu.security.demoapplication.model.Order$ProductType;" + // enum внутри Order
                        "java.lang.String;" +   // quantity
                        "java.lang.Long;" +     // id
                        "!*"                    // всё остальное — запрещено
        );

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            ois.setObjectInputFilter(filter);

            Object obj = ois.readObject();

            if (obj instanceof OrderImport dto) {
                Order order = new Order();
                order.setProductType(dto.getProductType());
                order.setQuantity(dto.getQuantity());
                Order saved = orderRepository.save(order);
                return ResponseEntity.ok("id = " + saved.getId());
            }

            return ResponseEntity.badRequest().body("Unknown payload type");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }
}