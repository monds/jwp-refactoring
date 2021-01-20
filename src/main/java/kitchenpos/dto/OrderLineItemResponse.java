package kitchenpos.dto;

import kitchenpos.domain.order.OrderLineItem;

public class OrderLineItemResponse {
    private final Long seq;
    private final Long menuId;
    private final Long quantity;

    public OrderLineItemResponse(Long seq, Long menuId, Long quantity) {
        this.seq = seq;
        this.menuId = menuId;
        this.quantity = quantity;
    }

    public static OrderLineItemResponse of(OrderLineItem orderLineItem) {
        return new OrderLineItemResponse(orderLineItem.getSeq(),
                orderLineItem.getMenuId(),
                orderLineItem.getQuantity().getQuantity());
    }

    public Long getMenuId() {
        return menuId;
    }

    public Long getQuantity() {
        return quantity;
    }
}
