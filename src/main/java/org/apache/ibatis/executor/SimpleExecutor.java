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
package org.apache.ibatis.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

/**
 * @author Clinton Begin
 * 简单执行器，是MyBatis中默认使用的执行器，每执行一次update或select，就开启一个Statement对象，
 * 用完就直接关闭Statement对象(可以是Statement或者是PreparedStatment对象)
 */
public class SimpleExecutor extends BaseExecutor {

  public SimpleExecutor(Configuration configuration, Transaction transaction) {
    super(configuration, transaction);
  }

  @Override
  public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
    Statement stmt = null;
    try {
      Configuration configuration = ms.getConfiguration();
      StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, RowBounds.DEFAULT, null, null);
      stmt = prepareStatement(handler, ms.getStatementLog());
      return handler.update(stmt);
    } finally {
      closeStatement(stmt);
    }
  }
//Executor执行器所起的作用相当于是管理StatementHandler 的整个生命周期的工作，包括创建、初始化、解析、关闭。
  @Override
  public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
    Statement stmt = null;
    try {
      Configuration configuration = ms.getConfiguration();// 获取环境配置
      // 创建StatementHandler，解析SQL语句
      StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
      stmt = prepareStatement(handler, ms.getStatementLog());
      // 由handler来对SQL语句执行解析工作
      return handler.query(stmt, resultHandler);
    } finally {
      closeStatement(stmt);
    }
  }

  // 查询可以返回Cursor<T>类型的数据，类似于JDBC里的ResultSet类，
  // 当查询百万级的数据的时候，使用游标可以节省内存的消耗，不需要一次性取出所有数据，可以进行逐条处理或逐条取出部分批量处理。
  @Override
  protected <E> Cursor<E> doQueryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
    Configuration configuration = ms.getConfiguration();
    StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, null, boundSql);
    Statement stmt = prepareStatement(handler, ms.getStatementLog());
    Cursor<E> cursor = handler.queryCursor(stmt);
    stmt.closeOnCompletion();
    return cursor;
  }

  @Override
  public List<BatchResult> doFlushStatements(boolean isRollback) {
    return Collections.emptyList();
  }

  private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {
    Statement stmt;
    Connection connection = getConnection(statementLog);
    // prepare 方法负责生成 Statement 实例对象 调用到 StatementHandler 的实现类RoutingStatementHandler，再由RoutingStatementHandler调用
    // BaseStatementHandler中的prepare 方法
    stmt = handler.prepare(connection, transaction.getTimeout());
    //parameterize 方法用于处理 Statement 实例对应的参数。
    //这次我们还想以SimpleStatementHandler 为例但是却不行了？为什么呢？因为 SimpleStatementHandler 是个空实现了，为什么是null呢？
    // 因为 SimpleStatementHandler 只负责处理简单SQL，能够直接查询得到结果的SQL，例如:
    //select studenname from Student
    //而 SimpleStatementHandler 又不涉及到参数的赋值问题，那么参数赋值该在哪里进行呢？实际上为参数赋值这步操作是在
    // PreparedStatementHandler 中进行的，因此我们的主要关注点在 PreparedStatementHandler 中的parameterize 方法
    handler.parameterize(stmt);
    return stmt;
  }

}
