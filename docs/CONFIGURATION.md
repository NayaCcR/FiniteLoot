# 配置说明

配置文件：`plugins/FiniteLoot/config.yml`。

## 领取规则

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `default-max-claims` | `3` | 新容器默认全服领取人数上限 |
| `per-player-limit` | `1` | 每名玩家每个容器最多领取次数，目前只支持 `1` |
| `exhausted-action` | `DENY` | 容器耗尽后的行为，目前只支持 `DENY` |
| `loot-table-overrides` | 见默认配置 | 按 LootTable 覆盖容器领取上限 |

## GUI 与表现

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `show-remaining-claims` | `true` | 打开个人奖励时在聊天显示剩余名额 |
| `play-container-animation` | `true` | 播放箱子/木桶等容器的开合动画 |
| `play-container-sounds` | `true` | 播放对应原版开合声音 |
| `trigger-piglin-anger` | `true` | 打开受猪灵保护的虚拟容器时触发附近猪灵仇恨 |
| `prevent-item-insertion` | `false` | `false` 时可反复打开未取空的个人库存并放入物品 |
| `completed-container-becomes-normal` | `true` | `PERSONAL` 模式下，个人奖励取空后打开普通共享容器 |
| `final-claim-action` | `VANILLA_CONTAINER` | 最后一个计数名额使用真实容器，或继续使用个人库存 (`PERSONAL`) |
| `clear-personal-inventories-on-final-claim` | `true` | 恢复原版容器时是否清除此前所有个人库存记录 |
| `show-final-claim-message` | `true` | 是否显示“这是最后一次领取，容器已恢复为标准奖励箱” |

默认组合会让前面的领取玩家反复打开并存放物品；最后一个计数名额会把最后一次奖励写入真实容器并恢复原版行为。将 `final-claim-action` 设为 `PERSONAL` 可保留最后一次个人库存模式；将 `prevent-item-insertion` 设为 `true` 则恢复为只能取出奖励的模式。

## 保护与计数

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `prevent-hopper-extraction` | `true` | 阻止漏斗和漏斗矿车抽取奖励 |
| `prevent-breaking` | `true` | 阻止破坏受管理容器 |
| `allow-breaking-exhausted-containers` | `false` | 允许直接破坏已领取完毕的容器，点击或破坏时恢复为普通容器 |
| `prevent-explosions` | `true` | 防止爆炸破坏受管理容器 |
| `count-creative-players` | `false` | 创造模式玩家是否占用名额 |
| `admin-bypass-counts` | `false` | 拥有 `finiteloot.bypass` 的管理员是否不占用名额 |
| `excluded-worlds` | `[]` | 不自动接管容器的世界列表 |
| `language` | `zh_CN` | 语言文件，支持 `zh_CN` 和 `en_US` |

启动时插件会检查数据库并在日志中给出已领取完毕容器的数量。“最后一次领取恢复原版容器”只在最后一个计数名额被占用或最后一名计数领取者再次开箱时触发，因此旧版本遗留的已领完容器会一直保持保护状态。开启 `allow-breaking-exhausted-containers` 后，这些容器会在下次被点击或破坏时恢复为普通容器并删除数据库记录；若仍有玩家在其个人库存中存放物品，这些记录会被一并清除，请谨慎开启。

修改配置后执行 `/fl reload`。非法值会拒绝重载并保留旧配置。
