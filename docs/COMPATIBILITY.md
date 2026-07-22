# 兼容性与冲突

- 目标平台为 Paper/Purpur 26.2，运行要求 Java 25+。
- 交互监听尊重其他插件已经取消的事件，不主动绕过 WorldGuard、Law 或 Bolt 的保护。
- 检测到 Lootr、Lootin 或名称相近的个人战利品插件时会输出冲突警告。不要让多个插件管理同一容器。
- Bolt 可能自动锁定管理员手动转换的容器。转换前请解除锁定或调整 Bolt 对应世界、区域和容器类型的规则。
- `excluded-worlds` 中不会自动接管新发现的容器，但管理员仍可维护已有 FiniteLoot 记录。

插件只自动识别仍带原版 LootTable 的方块 Lootable 容器。实体容器、模组容器以及 LootTable 已被原版清除的普通容器需要手动处理。
