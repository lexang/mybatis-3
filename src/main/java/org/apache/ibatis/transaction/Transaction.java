/**
 *    Copyright 2009-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Mybatis管理事务是分为两种方式:
 * (1)使用JDBC的事务管理机制,就是利用java.sql.Connection对象完成对事务的提交
 * (2)使用MANAGED的事务管理机制，这种机制mybatis自身不会去实现事务管理，而是让程序的容器（JBOSS,WebLogic）来实现对事务的管理
 *  事务接口
 *  Mybatis提供了一个事务接口Transaction，以及两个实现类jdbcTransaction和ManagedTransaction，
 *  当spring与Mybatis一起使用时，spring提供了一个实现类SpringManagedTransaction
 */
public interface Transaction {

  /**
   * 获得数据库连接
   * Retrieve inner database connection.
   * @return DataBase connection
   * @throws SQLException
   */
  Connection getConnection() throws SQLException;

  /**
   * 提交
   * Commit inner database connection.
   * @throws SQLException
   */
  void commit() throws SQLException;

  /**
   *  回滚
   * Rollback inner database connection.
   * @throws SQLException
   */
  void rollback() throws SQLException;

  /**
   * 关闭连接
   * Close inner database connection.
   * @throws SQLException
   */
  void close() throws SQLException;

  /**
   * Get transaction timeout if set.
   * @throws SQLException
   */
  Integer getTimeout() throws SQLException;

}
