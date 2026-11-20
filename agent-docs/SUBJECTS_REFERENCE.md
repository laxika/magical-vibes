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
| `allTargets()` | "untap up to four target permanents" whose targets the ability's target filter or several slots declare (every permanent target of the entry, whichever group it was chosen for) | yes | no |
| `attached()` | "enchanted creature", "equipped creature" (now while the source is on the battlefield, else as it last was) | no | yes |
| `attachedOrSource()` | "this creature ... If it's an Aura, enchanted creature ... instead" (a bestow card's own ability: the host while the source, as it is or last was, is attached, else the source itself) | no | yes |
| `attachedAtTrigger()` | "that creature" after "at the beginning of the upkeep of enchanted creature's controller" (the host the stack entry recorded when the ability triggered or was activated, not the current one; Paroxysm, Venarian Gold; "that creature" after "whenever enchanted creature deals damage", Guilty Conscience) | no | no |
| `triggering()` | "it", "that creature" (the permanent the trigger event was about) | no | no |
| `triggeringAll()` | "them", "for each of them" (every permanent of a "one or more" event, else the one the event was about; Kambal, Profiteering Mayor). A one-permanent verb's `triggering()` may read it for "one of them" (token copies, tokens attached to a creature) | no | no |
| `triggeringOrSource()` | "it" after "whenever this creature or equipped creature attacks" (a reconfigure card: the permanent the event recorded, else the source, whose own event records none) | no | no |
| `triggeringCard()` | "that card" of a "whenever you play a card" event (the spell or land; with `ownerOf`, "its owner", which for an event with no spell or land is the owner of the card put into a graveyard or of the permanent that entered) | no | no |
| `targetSpell()` | "target spell", only inside `controllerOf` ("that spell's controller"): read from the stack, else as it was countered (a counter-unless-pays rider has no target, only that record); names no permanent; the spell target is declared by the effect reading it (damage) or by the counter effect it rides on (discard) | no | no |
| `triggeringSpell()` | "that spell or ability", only inside `controllerOf`: the spell or ability the trigger event was about, as it is on the stack or last was ("that spell or ability's controller sacrifices a land", Lava Runner) | no | no |
| `returned()` | "that creature" after "return target card ... to the battlefield" (step result) | no | no |
| `returnedThisResolution()` | "exile them again" after an earlier step of the same resolution returned permanents (every permanent the entry recorded as returned; step result; Lae'zel's Acrobatics) | no | no |
| `exiledThisResolution()` | "return that card" after an earlier step of the same resolution exiled the effect's target: names no permanent on the battlefield; `permanentIds` gives the target's id, whose card the entry remembered as it left (step result; Gilraen, Dúnedain Protector) | no | no |
| `granter()` | "return this Equipment to its owner's hand" in an ability the Equipment grants (the granting permanent: bound at activation as `granter(id)`, else the granted trigger's recorded granter) | no | no |
| `named(permanentId)` | "that creature" in a delayed triggered ability a resolving effect created about another permanent than its trigger event ("when this leaves the battlefield or becomes untapped, remove all -1/-1 counters from the creature", Giant Oyster: bound by `RemoveAllCountersEffect.aimedAt`; a permanent that left and came back is not named) | no | no |
| `controlledBy(players, filter)` | "each creature you control", "all creatures", "each land target player controls" (the filter knows the source, X, the defending player, the triggering player and permanent: "tap all lands that player controls") | only if `players` targets | if `players` may |
| `ownedBy(players, filter)` | "all artifacts target player owns" (each matching permanent whose card's owner is one of `players`, whoever controls it; the effect declares a player target) | no | if `players` may |
| `attachmentsOf(permanents, filter)` | "all Auras attached to target permanent" (each matching permanent attached to one of the named permanents still on the battlefield; the attachments are not targeted) | no | if `permanents` may |
| `allOf(subjects...)` | "this creature and target creature" | if one part targets | if every part may |
| `oneOf(options...)` | "exile this permanent or up to one target creature": one of the options, chosen by the controller as the effect resolves; the resolver refuses it, the verb's handler resolves each option and asks; a target option is declared by the ability (Kaya, Ghost Assassin) | no | no |

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
| `chosenPlayer()` | "the chosen player" (chosen as the source entered; while the enters ability that chooses it is still on the stack, that ability's target: Laquatus's Champion; for a spell, the player an earlier effect of the spell chose onto the entry: Backdraft) | no | yes |
| `chosenPlayers()` | "the chosen players" (every player chosen as the source entered) | no | yes |
| `acceptedPlayers()` | "each opponent who discarded their hand this way": the players who said yes to the preceding "each player may" choice of the same resolution (step result); `except(acceptedPlayers(), you())` for the opponents among them | no | no |
| `playersDealtDamageThisResolution()` | "each player dealt damage this way": the players an earlier step of the same resolution dealt damage to, as the entry recorded them (step result; Chandra, Roaring Flame's emblem) | no | no |
| `subgameWinners()` | "each player who doesn't win that game" is `except(eachPlayer(), subgameWinners())`: the winners of the subgame an earlier step of the same resolution played (step result; Shahrazad; resolving it with no recorded result is an engine error) | no | no |
| `except(players, excluded)` | "the other chosen player": `except(chosenPlayers(), triggeringPlayer())` | no | if both may |
| `allOf(players...)` | "that player or that planeswalker's controller": `allOf(targetPlayerDeclaredElsewhere(), controllerOf(target()))` over a "target player or planeswalker" another effect declares (each player once, APNAP order) | no | if every part may |
| `matching(players, PlayerPredicate)` | "each player who controls the most lands": the players `players` names who match the filter as the effect is applied (`matching(eachPlayer(), new PlayerControlsMostLandsPredicate())`, Tectonic Hellion; the filter is a `PlayerPredicate`, evaluated with the effect's controller as the comparing player) | no | if `players` may |

A `target()` inside `controllerOf` / `ownerOf` reads the entry's target without declaring one ("destroy target
creature; its controller loses 2 life").

`Subjects.narrowed(set, filter)` narrows a set subject (`controlledBy`, `ownedBy`, `attachmentsOf`) by an effect's own
filter. `GrantScope.subject()` gives a `GrantScope` constant's subject while the effects still carry the enum; a one-shot
handler that reads a constant including the source names the including-self constant's subject.

A static ability's handler asks about one permanent at a time: `SubjectResolutionService.names(StaticEffectContext,
subject, filter, StaticReads)` (through `StaticEffectSupport.names`) says whether the subject names the context's
target. It judges `source()`, `attached()`, `attachedOrSource()`, `named()`, `allOf()` and `controlledBy()` (with
`you()`, `eachPlayer()`, `eachOpponent()`, `enchantedPlayer()` read off the context, other players through the list
form) without looking anything up; creature, land and the effect's filter are answered by the static pass's
recursion-safe reads. `ownedBy` and `attachmentsOf` have no one-permanent form yet.

The same check is asked outside the static handlers. `names(GameData, source, sourceControllerId, targetControllerId,
target, subject, StaticReads)` takes whose battlefield the target is on, or is entering, instead of searching for it
(a creature still entering, an enchanted player's creatures), and accepts a `null` source for an ability with no source
permanent (an emblem's: `source()` and `attached()` then name nothing). The layered pass answers creature and land from
the states of the board it is building (`LayerSystemService.scopeTargets`; an effect's own filter is judged and
memoized apart, per filter instance, CR 613.6); a continuous query outside the pass asks
`GameQueryService.staticSubjectNames`, which answers them with the layered queries; a caller that already judged the
permanent's type passes `StaticReads.knownCreature()` or `knownLand()` (that part of a set holds, any other does not).
