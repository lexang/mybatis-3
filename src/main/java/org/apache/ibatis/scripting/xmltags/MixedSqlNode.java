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

import java.util.List;

/**
 * @author Clinton Begin
 * MixedSqlNode是SqlNode的一个实现，包含了各个子节点，用来遍历输出子节点。SqlNode还有很多不同的实现，分别对应不同的节点类型。对应关系如下：
 * SqlNode实现->对应SQL语句中的类型
 * TextSqlNode->${}     IfSqlNode?->If节点     TrimSqlNode/WhereSqlNode/SetSqlNode->Trim/Where/Set节点
 * ForEachSqlNode->foreach标签    ChooseSqlNode->choose/when/otherwhise节点  ValDeclSqlNode->bind节点
 * StaticTextSqlNode->不含上述节点的  静态节点
 * 除了StaticTextSqlNode节点外，其余对应的都是动态语句。
 */
public class MixedSqlNode implements SqlNode {
  private final List<SqlNode> contents;

  public MixedSqlNode(List<SqlNode> contents) {
    this.contents = contents;
  }

  @Override
  public boolean apply(DynamicContext context) {
    contents.forEach(node -> node.apply(context));
    return true;
  }
}
