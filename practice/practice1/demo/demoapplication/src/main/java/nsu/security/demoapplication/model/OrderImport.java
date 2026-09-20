package nsu.security.demoapplication.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderImport implements Serializable {

    private Long id;

    private Order.ProductType productType;

    private String quantity;
}