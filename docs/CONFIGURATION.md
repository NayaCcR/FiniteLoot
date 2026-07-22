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
| `prevent-item-insertion` | `false` | `true` 时禁止向个人库存放入物品 |

## 保护与计数

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `prevent-hopper-extraction` | `true` | 阻止漏斗和漏斗矿车抽取奖励 |
| `prevent-breaking` | `true` | 阻止破坏受管理容器 |
| `prevent-explosions` | `true` | 防止爆炸破坏受管理容器 |
| `count-creative-players` | `false` | 创造模式玩家是否占用名额 |
| `admin-bypass-counts` | `false` | 拥有 `finiteloot.bypass` 的管理员是否不占用名额 |
| `excluded-worlds` | `[]` | 不自动接管容器的世界列表 |
| `language` | `zh_CN` | 语言文件，支持 `zh_CN` 和 `en_US` |

修改配置后执行 `/fl reload`。非法值会拒绝重载并保留旧配置。
