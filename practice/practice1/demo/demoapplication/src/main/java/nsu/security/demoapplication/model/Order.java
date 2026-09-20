package nsu.security.demoapplication.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProductType productType;

    private String quantity;

    public Order(ProductType productType, String quantity) {
        this.productType = productType;
        this.quantity = quantity;
    }

    public enum ProductType {
        ELECTRONICS,
        CLOTHING,
        FOOD,
        BOOKS,
        FURNITURE
    }
}