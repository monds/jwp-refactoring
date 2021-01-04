package kitchenpos.application;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.dao.TableGroupDao;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import kitchenpos.dto.TableGroupRequest;

@Service
public class TableGroupService {
	private final OrderDao orderDao;
	private final OrderTableDao orderTableDao;
	private final TableGroupDao tableGroupDao;

	public TableGroupService(final OrderDao orderDao, final OrderTableDao orderTableDao, final TableGroupDao tableGroupDao) {
		this.orderDao = orderDao;
		this.orderTableDao = orderTableDao;
		this.tableGroupDao = tableGroupDao;
	}

	@Transactional
	public TableGroup create(final TableGroupRequest tableGroupRequest) {
		final List<Long> orderTableIds = tableGroupRequest.getOrderTableIds();

		if (CollectionUtils.isEmpty(orderTableIds) || orderTableIds.size() < 2) {
			throw new IllegalArgumentException();
		}

		final List<OrderTable> savedOrderTables = orderTableDao.findAllByIdIn(orderTableIds);

		if (orderTableIds.size() != savedOrderTables.size()) {
			throw new IllegalArgumentException();
		}

		for (final OrderTable savedOrderTable : savedOrderTables) {
			if (!savedOrderTable.isEmpty() || Objects.nonNull(savedOrderTable.getTableGroup())) {
				throw new IllegalArgumentException();
			}
		}

		final TableGroup savedTableGroup = tableGroupDao.save(TableGroup.create());

		for (final OrderTable savedOrderTable : savedOrderTables) {
			savedOrderTable.setTableGroup(savedTableGroup);
			savedOrderTable.setEmpty(false);
			orderTableDao.save(savedOrderTable);
		}
		savedTableGroup.setOrderTables(savedOrderTables);

		return savedTableGroup;
	}

	@Transactional
	public void ungroup(final Long tableGroupId) {
		final List<OrderTable> orderTables = orderTableDao.findAllByTableGroupId(tableGroupId);

		final List<Long> orderTableIds = orderTables.stream()
			.map(OrderTable::getId)
			.collect(Collectors.toList());

		if (orderDao.existsByOrderTableIdInAndOrderStatusIn(
			orderTableIds, Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name()))) {
			throw new IllegalArgumentException();
		}

		for (final OrderTable orderTable : orderTables) {
			orderTable.setTableGroup(null);
			orderTableDao.save(orderTable);
		}
	}
}
