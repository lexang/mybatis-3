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
package org.apache.ibatis.scripting.defaults;

import java.util.HashMap;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;

/**
 * Static SqlSource. It is faster than {@link DynamicSqlSource} because mappings are
 * calculated during startup.
 * 静态的SQL会创建出RawSqlSource对象。
 * （1）通过configuration SqlNode(rootSqlNode-MixedSqlNode)获得原始SQL语句；（2）创建SqlSourceBuilder对象，解析SQL语句，
 * 并创建StaticSqlSource对象；
 * （3）将getBoundSql方法委托给内部的staticSqlSource对象
 * @since 3.2.0
 * @author Eduardo Macarron
 */
public class RawSqlSource implements SqlSource {
  //内部封装的sqlSource(StaticSqlSource)对象，getBoundSql方法会委托给这个对象
  private final SqlSource sqlSource;

  //rootSqlNode --  MixedSqlNode
  public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
    this(configuration, getSql(configuration, rootSqlNode), parameterType);
  }

  public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
    //创建sqlSourceBuilder
    SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
    Class<?> clazz = parameterType == null ? Object.class : parameterType;
    //解析sql，创建StaticSqlSource对象
    sqlSource = sqlSourceParser.parse(sql, clazz, new HashMap<>());
  }

  //通过SqlNode获得原始SQL语句  rootSqlNode--MixedSqlNode
  private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
    DynamicContext context = new DynamicContext(configuration, null);
    //这里的rootSqlNode就是之前得到的MixedSqlNode，它会遍历内部的SqlNode,逐个调用sqlNode的apply方法。
    //StaticTextSqlNode静态节点 会直接context.appendSql方法
    rootSqlNode.apply(context);
    return context.getSql();
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    return sqlSource.getBoundSql(parameterObject);
  }

}
