/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.datasource;

import java.util.Properties;
import javax.sql.DataSource;

/**
 * 在使用 Mybatis 的时候，数据库的连接一般都会使用第三方的数据源组件，如 C3P0，DBCP 和 Druid 等，
 * 其实 Mybatis 也有自己的数据源实现，可以连接数据库，还有连接池的功能，下面就来看看 Mybatis
 * 自己实现的数据源和连接池
 * 我们常见的数据源组件都实现了 Javax.sql.DataSource 接口，Mybatis 也实现该接口并且提供了两个实现类
 * UnpooledDataSource 和 PooledDataSource 一个使用连接池，一个不使用连接池，此外，对于这两个类，
 * Mybatis 还提供了两个工厂类进行创建对象，是工厂方法模式的一个应用
 * @author Clinton Begin
 */
public interface DataSourceFactory {

  void setProperties(Properties props);

  DataSource getDataSource();

}
