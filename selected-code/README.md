# 精选代码 / Selected Code Samples

本目录包含我在该双人数据库应用项目中负责实现的部分代表性代码摘录。

This directory contains selected excerpts from the components I implemented in this two-person database application project.

这些文件并不构成一个可独立运行的完整应用，而是从原项目源码中选取，用于展示我在 Java、JDBC、PostgreSQL、SQL 和数据处理方面的开发工作。

These files do not form a standalone runnable application. They are selected from the original project source code to demonstrate my work with Java, JDBC, PostgreSQL, SQL, and data processing.

## 文件说明 / Files

### `User.java`
- 用户注册与登录认证 / User registration and authentication
- BCrypt 密码哈希 / BCrypt password hashing
- JDBC / PreparedStatement 数据库访问 / JDBC / PreparedStatement database access

### `FriendRequest.java`
- 好友申请、接受与关系管理 / Friend request, acceptance, and relationship management
- 重复申请和好友关系校验 / Duplicate request and friendship validation
- 双向好友关系查询 / Bidirectional friendship queries

### `StockList.java`
- Stock List 创建、修改与删除 / Stock-list creation, modification, and deletion
- PRIVATE / FRIENDS / PUBLIC 权限控制 / PRIVATE / FRIENDS / PUBLIC access control
- JOIN、EXISTS 与 UPSERT SQL / SQL using JOIN, EXISTS, and UPSERT

### `HistoricalDataLoader.java`
- 历史股票数据批量导入 / Batch loading of historical stock data
- 重复记录处理 / Duplicate record handling
- 数据统计与 CV / Beta 更新 / Statistical queries and CV / Beta updates

### `StockPrediction.java`
- 从 PostgreSQL 获取历史股票价格 / Historical stock price retrieval from PostgreSQL
- SMA、EMA 与线性回归预测 / SMA, EMA, and linear regression prediction
- 多种预测方法结果对比 / Comparison of multiple prediction methods
