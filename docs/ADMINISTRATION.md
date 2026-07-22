# 管理员操作

主命令为 `/finiteloot`，别名为 `/fl`。管理操作通常先执行命令，再右键目标容器。

| 命令 | 权限 | 作用 |
| --- | --- | --- |
| `/fl reload` | `finiteloot.reload` | 重载配置和语言 |
| `/fl inspect` | `finiteloot.inspect` | 查看容器 ID、LootTable、上限和领取记录 |
| `/fl set <次数>` | `finiteloot.set` | 转换容器或修改领取上限 |
| `/fl reset container` | `finiteloot.reset` | 清除容器全部领取状态 |
| `/fl reset player <玩家>` | `finiteloot.reset` | 清除指定玩家对选中容器的记录 |
| `/fl remove` | `finiteloot.remove` | 取消 FiniteLoot 管理 |
| `/fl migrate` | `finiteloot.admin` | 备份、迁移和校验数据库 |

`/fl set <次数>` 可以把没有 LootTable 的非空普通容器转换为奖励箱；转换时公共库存会被保存为奖励模板。空容器不能转换。

`/fl remove` 会写入忽略标记，避免原版 LootTable 容器再次被自动接管。之后再次使用 `/fl set` 可重新管理。

权限默认授予 OP：`finiteloot.admin`、`finiteloot.inspect`、`finiteloot.set`、`finiteloot.reset`、`finiteloot.remove`、`finiteloot.reload`、`finiteloot.bypass`。`finiteloot.*` 是父权限。
