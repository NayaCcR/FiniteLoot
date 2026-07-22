# 开发与测试

## 本地构建

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.3'
.\gradlew.bat clean build
```

产物位于 `build/libs/`。项目使用 Gradle Kotlin DSL、Paper API、Java 25，不使用 NMS、反射或版本绑定代码。

## 测试

`build` 会执行 JUnit 和 Checkstyle。核心领取逻辑与 Bukkit 事件监听器分离，测试覆盖领取上限、重复开启、并发争抢、重启恢复、双箱归一、LootTable 覆盖和数据库迁移。

版本按 Git 提交数自动生成：干净工作区为 `1.1.<补丁号>`，脏工作区附加 `-dev`。同一提交重试不会跳号。

## GitHub Actions

- `quick-test.yml`：分支推送和 PR 自动执行快速单元测试。
- `build.yml`：手动执行完整构建、测试、Checkstyle 并上传构建产物。
- `release.yml`：`main` 分支快速测试成功后自动创建版本标签，并完成构建、测试和 GitHub Release 发布。
