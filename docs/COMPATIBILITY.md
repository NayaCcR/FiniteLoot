# 兼容性与冲突

- 目标平台为 Paper/Purpur 26.2，运行要求 Java 25+。
- 交互监听尊重其他插件已经取消的事件，不主动绕过 WorldGuard、Law 或 Bolt 的保护。
- 检测到 Lootr、Lootin 或名称相近的个人战利品插件时会输出冲突警告。不要让多个插件管理同一容器。
- Bolt 可能自动锁定管理员手动转换的容器。转换前请解除锁定或调整 Bolt 对应世界、区域和容器类型的规则。
- `excluded-worlds` 中不会自动接管新发现的容器，但管理员仍可维护已有 FiniteLoot 记录。
- FiniteLoot 会在堡垒遗迹奖励成功持久化后，通过 Paper Advancement API 补发原版“战猪”进度；四种堡垒 LootTable 均受支持，旧领取记录在玩家再次打开时也会补发。
- `trigger-piglin-anger: true` 时，打开箱子、木桶、铜箱或潜影盒等原版受保护容器，会通过 Paper 的公开仇恨记忆和目标 API触发 16 格内可见成年猪灵；其他插件仍可取消目标事件。

插件只自动识别仍带原版 LootTable 的方块 Lootable 容器。实体容器、模组容器以及 LootTable 已被原版清除的普通容器需要手动处理。

Minecraft 26.2 中另一项使用 `player_generates_container_loot` 的原版进度是考古相关的 `Salvage Sherd`。可疑沙子和可疑沙砾不是 `InventoryHolder`，FiniteLoot 不会接管，因此原版触发路径不受影响。Paper API 不公开数据包进度 criterion 的触发器和条件，所以插件无法自动发现并补发自定义数据包中依赖该触发器的进度。

Paper 的公开 Memory API不能设置原版内部的记忆过期时间。FiniteLoot 使用原版 20–39 秒范围的主线程任务清理自己建立的猪灵仇恨，并在实体卸载或插件关闭时提前清理，避免永久仇恨；因此跨区块卸载时，仇恨持续时间可能短于原版。
