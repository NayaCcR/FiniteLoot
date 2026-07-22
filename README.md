# FiniteLoot / 限量战利品

FiniteLoot 是面向 Paper/Purpur 26.2 的纯服务端战利品插件。每名玩家看到独立奖励，但每个结构战利品容器只有有限个全服领取名额，避免遗迹资源随在线人数无限复制。客户端不需要安装模组。

## 运行要求

- Paper 或 Purpur 26.2
- Java 25 或更高版本
- 不使用 NMS、反射或版本绑定代码
- 数据库固定为 `plugins/FiniteLoot/data.db`

Paper 26.2 官方项目配置使用 `io.papermc.paper:paper-api:26.2.build.+` 和 Java 25；插件清单使用 `api-version: '26.2'`。参考 [Paper 项目配置](https://docs.papermc.io/paper/dev/project-setup/) 和 [plugin.yml 字段说明](https://docs.papermc.io/paper/dev/plugin-yml/)。

## 安装

1. 停止服务器。
2. 将 `FiniteLoot-<version>.jar` 放入服务器的 `plugins` 目录。
3. 使用 Java 25 启动 Paper/Purpur 26.2。
4. 检查控制台中 FiniteLoot 的数据库初始化日志和潜在冲突警告。

首次启动会生成 `plugins/FiniteLoot/config.yml`、所选语言文件和 SQLite 数据库。不要在服务器运行时直接替换或编辑 `data.db`。

## 工作方式

- 仅自动识别实现 `Lootable`、仍带原版 LootTable 的 TileState 方块容器。
- 首次访问时向方块 PDC 写入随机 UUID；数据库以 UUID 为主键，坐标只用于诊断。
- 双箱两半按固定坐标顺序归一，并写入同一 UUID。
- 原版已清除 LootTable 且没有 FiniteLoot PDC 的普通容器不会被自动转换。
- 领取资格在 SQLite `BEGIN IMMEDIATE` 事务中分配；条件计数更新和 `(container_id, player_uuid)` 主键共同防止突破上限和重复计数。
- 资格成功后才在主线程调用 Bukkit LootTable API，随后用 Paper `ItemStack.serializeItemsAsBytes` 持久化；数据库写入成功后才显示虚拟库存。
- 世界容器库存不用于显示个人奖励。漏斗移动、破坏和爆炸保护均针对未开启原版战利品容器及已管理容器。

数据库异常时新的领取会被拒绝，不会回退为公共箱或无限生成模式。

## 配置

```yaml
default-max-claims: 3
show-remaining-claims: true
play-container-animation: true
play-container-sounds: true
per-player-limit: 1
exhausted-action: DENY
prevent-item-insertion: false
prevent-hopper-extraction: true
prevent-breaking: true
prevent-explosions: true
count-creative-players: false
admin-bypass-counts: false
excluded-worlds: []
language: zh_CN
loot-table-overrides:
  minecraft:chests/end_city_treasure: 2
  minecraft:chests/ancient_city: 2
  minecraft:chests/simple_dungeon: 5
  minecraft:chests/abandoned_mineshaft: 4
```

`show-remaining-claims: true` 会在成功打开个人奖励库存时，在聊天框显示当前剩余领取名额；设为 `false` 可关闭。`play-container-animation` 和 `play-container-sounds` 控制 Lootr 风格的世界容器开合表现。`prevent-item-insertion: false` 允许玩家把物品存入自己的持久化容器；改为 `true` 后只能取出奖励。`per-player-limit` 必须为 `1`，这是插件的数据不变量。`admin-bypass-counts: true` 时，拥有 `finiteloot.bypass` 的玩家仍会获得唯一个人记录，但不消耗全服名额。`count-creative-players: false` 同理。未计数玩家可在普通名额耗尽后领取。

允许放入物品时，取空后的个人库存仍可再次打开作为储物空间；从旧配置切换到该模式时，已有 `completed` 记录会自动恢复为可打开状态且不会再次占用名额。开盖状态按容器统计查看人数：首位玩家打开时开盖，最后一位玩家关闭时合盖。箱子、铜箱、木桶和潜影盒使用各自原版音效；没有 Bukkit `Lidded` 表现的其他 Lootable 只显示个人 GUI。

修改配置后执行 `/finiteloot reload`。非法值会拒绝重载并保留旧的运行配置。

## 管理命令

主命令 `/finiteloot`，别名 `/fl`。

| 命令 | 行为 |
| --- | --- |
| `/fl reload` | 重新加载配置和语言 |
| `/fl inspect` | 准备检查操作，随后右键容器 |
| `/fl set <次数>` | 准备设置操作，随后右键容器 |
| `/fl reset container` | 准备清空全部领取状态 |
| `/fl reset player <玩家>` | 准备清除指定玩家的领取状态 |
| `/fl remove` | 准备取消管理并删除数据库记录 |
| `/fl migrate` | 备份数据库、执行迁移、清理孤立记录并进行完整性校验 |

`set` 可修改已有容器上限。对没有 LootTable 的普通容器使用时，会把当前公共库存持久化为个人奖励模板，数据库确认写入后再清空公共库存；空容器不能转换。

## 权限

| 权限 | 默认值 | 用途 |
| --- | --- | --- |
| `finiteloot.admin` | OP | 所有管理命令 |
| `finiteloot.inspect` | OP | 检查容器 |
| `finiteloot.set` | OP | 转换或设置上限 |
| `finiteloot.reset` | OP | 重置记录 |
| `finiteloot.remove` | OP | 取消管理 |
| `finiteloot.reload` | OP | 重载配置 |
| `finiteloot.bypass` | OP | 在配置允许时不计数 |
| `finiteloot.*` | OP | `admin` 与 `bypass` 的父权限 |

Paper 插件在启用时注册这些权限；命令入口也逐项检查权限。

## 数据、备份与升级

数据库包含 `containers`、`claims`、`personal_inventories` 和 `schema_version` 表，并启用外键、WAL、FULL synchronous 与 10 秒 busy timeout。插件关闭时会在主线程快照仍打开的库存，等待数据库队列刷新，再关闭连接。

升级步骤：

1. 执行 `/fl migrate`，确认 `plugins/FiniteLoot/backups` 中产生带时间戳的备份。
2. 停止服务器，并额外备份整个 `plugins/FiniteLoot` 目录。
3. 替换 JAR 后启动服务器；启动迁移只前进，不会删除旧版本数据。
4. 检查控制台是否出现完整性、外键或版本过新的错误。

孤立数据清理只由 `/fl migrate` 执行，并且始终先通过 SQLite `VACUUM INTO` 创建一致性备份。不要只复制正在运行中的单个 `data.db` 文件，因为 WAL 中可能仍有已提交页面。

## 兼容与冲突

交互监听使用 `ignoreCancelled = true`，因此会尊重 WorldGuard、Law、Bolt 等插件更早取消的交互。FiniteLoot 不主动绕过领地或锁插件。

- 检测到名称包含 Lootr 或 Lootin 的插件时会输出明确警告。不要让两个个人战利品插件同时管理同一容器。
- Bolt 可能自动锁定管理员手动放置或转换的奖励箱。请在转换前配置 Bolt 对该世界/区域/容器类型的自动上锁规则，或由管理员先解除锁定；FiniteLoot 不修改 Bolt 所有权数据。
- `excluded-worlds` 中的世界不会自动拦截或转换战利品容器，但管理员点击操作仍可用于检查和维护已有数据。

`/fl remove` 会在方块 PDC 写入忽略标记，防止仍带原版 LootTable 的容器被下一次点击立即重新接管。管理员再次执行 `/fl set <次数>` 会清除此标记；世界或区块重新生成后的新方块不会继承它。

## 构建与测试

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.3'
.\gradlew.bat clean build
```

部署文件位于 `build/libs/FiniteLoot-<version>.jar`。补丁版本由 Git 提交数自动生成：干净工作区使用 `1.1.<自动补丁号>`，未提交修改的构建附加 `-dev`。`versionCommitOffset` 保存当前版本线的基线提交数；需要开启新的次版本线时修改 `versionLine` 并同步更新偏移量。测试覆盖领取上限、重复开启、24 个并发请求争抢最后名额、数据库重启恢复、双箱 ID 归一、LootTable 上限覆盖和版本迁移。`check` 同时执行 JUnit 与 Checkstyle。

## 已知限制

- 仅支持方块 TileState Lootable 容器；实体容器和模组容器不在范围内。
- `exhausted-action` 当前只接受 `DENY`，`per-player-limit` 当前只接受 `1`。
- 原版 LootTable 在奖励生成前必须仍可由服务器注册表解析；移除数据包 LootTable 会拒绝新领取并保留资格回滚能力。
- Paper 插件格式和 26.2 API 属于明确版本目标；旧版 Paper 不会加载此 JAR。

## 实际服务端验证

- Paper 26.2 build 63（API `26.2.build.63-beta`）、Java 25.0.3：插件加载、启用、SQLite 初始化、达到 `Done`、正常禁用和退出均成功。
- Purpur 26.2 build 2613（API `26.2.build.2613-stable`）、Java 25.0.3：上述流程成功，并实际验证 `/fl reload`、`/finiteloot migrate` 和一致性备份生成。
- 验证在隔离的离线模式测试目录中完成，未进行真实客户端多人交互；并发与库存持久化边界由 JUnit 数据层测试覆盖。

## 许可证

本项目使用 GNU General Public License v3.0，详见 `LICENSE`。
