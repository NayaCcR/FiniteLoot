# 发布流程

发布标签必须与插件版本一致，例如插件版本为 `1.1.1` 时使用标签 `v1.1.1`。

```powershell
git tag v1.1.1
git push origin v1.1.1
```

推送标签会触发 `release.yml`，流程会：

1. 使用标签对应的源码构建、测试和运行 Checkstyle。
2. 校验 JAR 内 `paper-plugin.yml` 的版本。
3. 创建 GitHub Release 并上传可部署的 shaded JAR。

也可以在 GitHub Actions 页面手动运行 Release 工作流并输入标签。版本不匹配或构建失败时不会发布 Release。
