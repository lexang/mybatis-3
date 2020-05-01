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
package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 * 动态SQL的解析
 * 动态SQL对应的SqlSource实现主要是DynamicSqlSource
 * 对比RawSqlSource和DynamicSqlSource的字段值，我们可以很直观的发现RawSqlSource直接有一个SqlSource属性，构造函数中通过configuration
 * 和SqlNode直接解析SqlSource对象，而DynamicSqlSource相反，他没有SqlSource属性，反而是保留了configuration和SqlNode作为属性，
 * 只有在getBoundSql时，才会去创建SqlSource对象。
 * 这正是因为动态Sql的sqlsource是无法直接确定的，需要在运行时根据条件才能确定。
 *
 * 所以，对于动态SQL的解析其实是分为两阶段的：
 *
 * 1.解析XML资源：之前的解析过程都类似（可参考前一篇文章），XMLScriptBuilder会将XML中的节点解析成各个类型的SqlNode，然后封装成MixedSqlNode，
 * 它和Configuration对象一起作为参数，创建DynamicSqlSource对象。
 */
public class DynamicSqlSource implements SqlSource {

  private final Configuration configuration;
  private final SqlNode rootSqlNode;

  public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
    this.configuration = configuration;
    this.rootSqlNode = rootSqlNode;
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {//参数对象:{"id"->1 ; "param1" -> 1}
    //传入configuration和运行时的参数，创建DynamicContext对象
    DynamicContext context = new DynamicContext(configuration, parameterObject);
    //应用每个SqlNode，拼接Sql片段，这里只替换动态部分
    rootSqlNode.apply(context);//此时context的sqlBuilder已经被解析成了:select * from author where id = 1
    //继续解析SQL，将#{}替换成?
    SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
    Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
    SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
    //创建BoundSql对象
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
    context.getBindings().forEach(boundSql::setAdditionalParameter);
    return boundSql;
  }

}
