# flowable-springboot
the demo for flowable-springboot
# 采用springboot+flowable快速实现工作流
```
postgresql
flowable
mybatis-plus
fastjson
```
# 测试api
## 发起报销
http://localhost:8080/expense/add?userId=bossTask&money=300
## 查看任务
http://localhost:8080/expense/list?userId=boss
## 通过
http://localhost:8080/expense/apply?taskId=17507
## 驳回
http://localhost:8080/expense/reject?taskId=25017
## 查看流程图
http://localhost:8080/expense/processDiagram?processId=25008