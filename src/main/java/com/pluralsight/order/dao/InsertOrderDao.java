package com.pluralsight.order.dao;

import com.pluralsight.order.dto.OrderDto;
import com.pluralsight.order.dto.OrderDetailDto;
import com.pluralsight.order.util.Database;
import com.pluralsight.order.util.ExceptionHandler;
import com.pluralsight.order.util.OrderStatus;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * DAO to insert an order
 */
public class InsertOrderDao {
    private String sqlOrder = "INSERT INTO orders " +
            "(order_customer_id, order_date, order_status) " +
            "VALUES (?, ?, ?)";
    String sqlOrderDetail =
            "INSERT INTO order_details "
                    + "(order_detail_order_id, order_detail_product_id, order_detail_quantity) "
                    + "VALUES (?, ?, ?)";
    private Database database;

    /**
     * Constructor
     * @param database Database object
     */
    public InsertOrderDao(Database database) {
        this.database = database;
    }

    /**
     * Inserts an order
     * @param orderDto Object with the information to insert
     * @return The ID of the order inserted
     */
    public long insertOrder(OrderDto orderDto) {
        long orderId = -1;

        try (Connection con = database.getConnection();
             PreparedStatement ps = createOrderPreparedStatement(con, orderDto)
        ) {
            con.setAutoCommit( false );
            ps.executeUpdate();

            try (ResultSet result = ps.getGeneratedKeys()) {
                if(result != null) {
                    if(!result.next()){
                        con.rollback();
                    } else {
                        orderId = result.getLong( "order_id" );

                        for (OrderDetailDto orderDetailDto : orderDto.getOrderDetail()) {
                            orderDetailDto.setOrderId(orderId);

                            try (PreparedStatement detailsPS =
                                         createOrderDetailPreparedStatement(con, orderDetailDto)) {
                                if (detailsPS.executeUpdate() != 1) {
                                    con.rollback();
                                    orderId = -1;
                                }
                            }
                        }
                        con.commit();
                    }
                }
            } catch(SQLException ex) {
                con.rollback();
                ExceptionHandler.handleException(ex);
            }
        } catch (SQLException ex) {
            ExceptionHandler.handleException(ex);
        }

        return orderId;
    }

    /**
     * Creates a PreparedStatement object to insert the order record
     * @param con Connnection object
     * @param orderDto Object with the parameters to set on the PreparedStatement
     * @return A PreparedStatement object
     * @throws SQLException In case of an error
     */
    private PreparedStatement createOrderPreparedStatement(Connection con, OrderDto orderDto) throws SQLException {
        PreparedStatement preparedStatement = con.prepareStatement( sqlOrder, Statement.RETURN_GENERATED_KEYS );
        preparedStatement.setLong( 1, orderDto.getCustomerId() );
        preparedStatement.setTimestamp( 2, Timestamp.valueOf( LocalDateTime.now() ) );
        preparedStatement.setString( 3, OrderStatus.CREATED.getStatus() );
        return preparedStatement;
    }

    /**
     * Creates a PreparedStatement object to insert the details of the order
     * @param con Connnection object
     * @param orderDetailDto Object with the parameters to set on the PreparedStatement
     * @return A PreparedStatement object
     * @throws SQLException In case of an error
     */
    private PreparedStatement createOrderDetailPreparedStatement(Connection con, OrderDetailDto orderDetailDto) throws SQLException {
        PreparedStatement preparedStatement = con.prepareStatement( sqlOrderDetail );
        preparedStatement.setLong( 1, orderDetailDto.getOrderId() );
        preparedStatement.setLong( 2, orderDetailDto.getProductId() );
        preparedStatement.setInt( 3, orderDetailDto.getQuantity() );
        return preparedStatement;
    }
}
