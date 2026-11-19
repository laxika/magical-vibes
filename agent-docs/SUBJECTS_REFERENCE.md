# Subjects reference

Who or what an effect acts on. A verb effect takes a `PermanentSubject` or `PlayerSubject` built with the factory
`Subjects` (domain, `model/effect/`); `SubjectResolutionService` (engine, `service/effect/`) is the only code that
resolves one. "Targets" = the variant declares the effect's target. "Static" = a static ability may use it (the
resolver's static entry point rejects the others).

## PermanentSubject

| Factory | Card wording | Targets | Static |
|---|---|---|---|
| `source()` | "this creature", "this permanent" (the entry's source permanent only, never its target) | no | yes |
| `target()`, `target(group)` | "target creature", "target permanent" | yes | no |
| `attached()` | "enchanted creature", "equipped creature" (now while the source is on the battlefield, else as it last was) | no | yes |
| `attachedAtTrigger()` | "that creature" after "at the beginning of the upkeep of enchanted creature's controller" (the host the stack entry recorded when the ability triggered or was activated, not the current one; Paroxysm, Venarian Gold) | no | no |
| `triggering()` | "it", "that creature" (the permanent the trigger event was about) | no | no |
| `triggeringCard()` | "that card" of a "whenever you play a card" event (the spell or land; with `ownerOf`, "its owner", which for an event with no spell or land is the owner of the card put into a graveyard or of the permanent that entered) | no | no |
| `returned()` | "that creature" after "return target card ... to the battlefield" (step result) | no | no |
| `controlledBy(players, filter)` | "each creature you control", "all creatures", "each land target player controls" | only if `players` targets | if `players` may |
| `allOf(subjects...)` | "this creature and target creature" | if one part targets | if every part may |
| `of(PermanentReference)` | temporary adapter for effects that still carry a `PermanentReference` | no | `SOURCE`, `ATTACHED` |

## PlayerSubject

| Factory | Card wording | Targets | Static |
|---|---|---|---|
| `you()` | "you" | no | yes |
| `targetPlayer()`, `targetPlayer(group)` | "target player", "target opponent" | yes | no |
| `targetPlayerDeclaredElsewhere()` | "target player" whose target the card's target filter or another effect of the ability declares ("target player draws two cards and loses 2 life"); reads the same targets | no | no |
| `eachPlayer()`, `eachOpponent()` | "each player", "each opponent" (APNAP order) | no | yes |
| `opponentsOf(player)` | "each of that player's opponents", "each other player" (APNAP order; nobody if `player` names nobody) | no | if `player` may |
| `controllerOf(permanent)` | "its controller", "that creature's controller" (last-known once it left) | no | if `permanent` may |
| `ownerOf(permanent)` | "its owner" | no | if `permanent` may |
| `triggeringPlayer()` | "that player" (the player the trigger event was about) | no | no |
| `activePlayer()` | "the active player" | no | yes |
| `defendingPlayer()` | "defending player" | no | no |
| `enchantedPlayer()` | "enchanted player" (a Curse's player) | no | yes |
| `chosenPlayer()` | "the chosen player" (chosen as the source entered) | no | yes |
| `chosenPlayers()` | "the chosen players" (every player chosen as the source entered) | no | yes |
| `except(players, excluded)` | "the other chosen player": `except(chosenPlayers(), triggeringPlayer())` | no | if both may |

A `target()` inside `controllerOf` / `ownerOf` reads the entry's target without declaring one ("destroy target
creature; its controller loses 2 life").
