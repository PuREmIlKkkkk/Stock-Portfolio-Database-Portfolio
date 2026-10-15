# Stock Portfolio Database 项目展示 / Stock Portfolio Database Project Overview

这是一个基于 **Java、PostgreSQL 与 JDBC** 开发的股票投资组合与社交功能数据库应用。项目围绕关系数据库设计、用户与好友关系、股票列表权限管理、历史股票数据处理及价格预测等功能展开。

This is a database-backed stock portfolio and social networking application built with **Java, PostgreSQL, and JDBC**. The project focuses on relational database design, user and friendship management, stock-list access control, historical stock data processing, and stock price prediction.

---

## 项目概览 / Project Overview

该项目由两人协作完成，目标是从数据库建模开始，设计并实现一个完整的股票投资与社交应用。

系统主要支持：

- 用户注册与登录认证
- 好友申请、接受、删除与关系管理
- 投资组合 Portfolio 管理
- Stock List 创建与股票管理
- `PRIVATE / FRIENDS / PUBLIC` 多级访问权限
- 历史股票数据导入与查询
- 股票统计分析
- 股票历史价格查看
- 股票价格预测

This project was developed by a two-person team and covers the full process from database modeling and relational design to application implementation.

The system supports:

- User registration and authentication
- Friend requests, acceptance, removal, and relationship management
- Portfolio management
- Stock-list creation and stock management
- `PRIVATE / FRIENDS / PUBLIC` access control
- Historical stock data import and querying
- Stock statistics and analysis
- Historical stock price exploration
- Stock price prediction

---

## 技术栈 / Tech Stack

### 应用开发 / Application
- Java
- JDBC
- Maven

### 数据库 / Database
- PostgreSQL
- SQL
- Relational Database Design

### 数据库设计 / Database Design
- E/R Modeling
- Relational Schema Design
- BCNF Normalization
- Relational Algebra

### SQL 与数据处理 / SQL & Data Processing
- JOIN
- EXISTS
- UPSERT
- Aggregate Queries
- Subqueries
- Batch Insert

### 环境 / Environment
- Linux
- Google Cloud Platform

---

## 数据规模 / Dataset

项目使用约 **61.9 万条 S&P 500 历史股票日线记录**，包含：

- Timestamp
- Open
- High
- Low
- Close
- Volume
- Symbol

The project works with approximately **619,000 historical S&P 500 daily stock records**, including timestamp, OHLC prices, trading volume, and stock symbols.

---

## 我的主要贡献 / My Contributions

### 1. 用户认证与数据库交互 / User Authentication & Database Interaction

负责用户相关模块开发，包括：

- 用户注册与登录
- 用户信息查询
- 密码哈希处理
- 基于 JDBC 的数据库访问
- 使用参数化 SQL 进行数据库交互

Responsible for the user module, including:

- User registration and login
- User lookup
- Password hashing
- JDBC-based database access
- Parameterized SQL queries

---

### 2. 好友关系管理 / Friendship Management

实现用户社交关系相关功能，包括：

- 发送好友申请
- 接受或拒绝好友申请
- 删除好友
- 重复好友关系检测
- 双向好友关系查询

Implemented friendship-related functionality including:

- Sending friend requests
- Accepting and rejecting requests
- Removing friends
- Duplicate relationship validation
- Bidirectional friendship lookup

---

### 3. Stock List 管理与访问权限 / Stock List Management & Access Control

负责 Stock List 相关逻辑，实现：

- Stock List 创建与删除
- 股票添加、修改与删除
- `PRIVATE / FRIENDS / PUBLIC` 三种可见性
- 基于好友关系判断访问权限
- 使用 `JOIN`、`EXISTS`、`UPSERT` 等 SQL 完成数据查询与更新

Developed stock-list management and access-control logic, including:

- Stock-list creation and deletion
- Adding, updating, and removing stocks
- `PRIVATE / FRIENDS / PUBLIC` visibility levels
- Friendship-based access validation
- SQL queries using `JOIN`, `EXISTS`, and `UPSERT`

---

### 4. 历史股票数据处理 / Historical Stock Data Processing

实现历史股票数据加载与查询功能，包括：

- 大规模历史数据批量导入
- 股票代码与时间范围查询
- 历史价格数据检索
- 数据统计与更新

Implemented historical stock data loading and querying, including:

- Batch import of large historical datasets
- Symbol- and time-based queries
- Historical price retrieval
- Statistical data updates

---

### 5. 股票价格预测 / Stock Price Prediction

实现股票预测相关模块，将历史价格数据从 PostgreSQL 中读取并用于未来价格预测与趋势展示。

Implemented the stock prediction module by retrieving historical price data from PostgreSQL and integrating it with future price prediction and trend visualization.

---

## 数据库设计 / Database Design

项目从 E/R 建模开始，进一步转换为关系模式，并对数据库结构进行规范化设计。

The project begins with E/R modeling, followed by relational schema design and database normalization.

数据库主要涉及以下核心实体与关系：

- User
- Friends
- Friend Requests
- Portfolio
- Stock List
- Stock List Items
- Stock
- Historical Stock Data

The main database entities and relationships include:

- Users
- Friend relationships
- Friend requests
- Portfolios
- Stock lists
- Stock-list items
- Stocks
- Historical stock data

数据库设计图可以在以下目录查看：

Database design diagrams can be found under:

```text
architecture/
```

---

## SQL 设计亮点 / SQL Highlights

项目中使用了多种关系数据库查询与更新方式，例如：

The project uses a range of relational database operations, including:

- 多表 `JOIN`
- `EXISTS` 子查询
- `ON CONFLICT` / UPSERT
- 聚合函数
- `GROUP BY`
- 子查询
- 批量数据插入
- 基于主键与外键的数据完整性约束

Examples include:

- Multi-table `JOIN`
- `EXISTS` subqueries
- `ON CONFLICT` / UPSERT
- Aggregate functions
- `GROUP BY`
- Nested queries
- Batch insertion
- Primary-key and foreign-key constraints

---

## 精选代码 / Selected Code Samples

`selected-code/` 目录包含我在该双人项目中负责或参与实现的代表性代码摘录。

The `selected-code/` directory contains representative excerpts from the parts of the two-person project that I was responsible for or contributed to.

计划展示的代码包括：

Selected samples include:

- `user-authentication.java`
- `friend-management.java`
- `stock-list.java`
- `historical-data-loader.java`
- `stock-prediction.java`

这些文件并不是一个可以独立运行的完整应用，而是从原项目中选取的代表性代码，用于展示我在 Java、JDBC、SQL 和 PostgreSQL 方面的开发工作。

These files are not intended to form a standalone runnable application. They are selected excerpts from the original project and are included to demonstrate my work with Java, JDBC, SQL, and PostgreSQL.

---

## 项目演示 / Project Demo

该项目主要以命令行应用形式运行。

A short terminal demonstration will be included to show the main application workflow.

```text
demo/
```

演示内容计划包括：

The demo will cover:

- 用户登录
- 好友关系管理
- Stock List 查看与修改
- 股票历史数据查询
- 股票价格预测

---

## 项目说明 / Repository Note

本仓库用于求职和个人作品展示。

This repository is intended for portfolio and recruiting purposes.

由于原项目由两人协作开发，本仓库不会公开完整项目源码，仅展示我负责或参与开发的代表性代码摘录、数据库设计和项目说明。

Because the original application was developed as a two-person team project, this repository does not contain the complete project source code. It only includes selected excerpts from code I contributed to, along with database design materials and project documentation.

精选代码保持原项目的实现逻辑，仅进行必要的片段截取和展示说明。

The selected code samples preserve the implementation logic of the original project and are presented as excerpts for portfolio purposes.
