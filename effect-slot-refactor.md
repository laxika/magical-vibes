# EffectSlot refactor

Replaces `EffectSlot` with abilities that are objects, matched against rules events by one pipeline
and put on the stack by one service. Steps 1 to 14 are done. What is left is done in **sweeps**: each
takes a whole family, routes it through the one service, and deletes the family's own code.

History: the earlier plan, with 22,000 lines of findings for steps 1 to 15, is
`git show feeb04cb67:effect-slot-refactor.md` (2.9 MB). Never read it whole. To look something up,
grep its `^#### Run` headings and read that range. The invariants those runs established are in
`agent-docs/architecture/triggers.md` (137 KB: grep it, never read it whole), and that is the place to read them.

## Status

| ID | Sweep | Status |
|---|---|---|
| 1–14 | Ability model, every family off the enum, trigger options | done |
| S0 | The stack door, the ratchet, the ledger; pilot: block triggers | done |
| S1 | Combat | done but: C16, C17, C18, C19, C20, C21, C22 |
| S2 | Steps, phases, draw step, planar | done but: C16, C21, C22, C23, C24, C25 |
| S3 | Spells, abilities, becomes the target | done but: C16, C26, C27, C28 |
| S4 | Damage, counters, life, tap | open |
| S5 | Library, draw, discard, actions, sagas, the rest | open |
| S6 | Zone changes: enters, dies, leaves, graveyard | open |
| S7 | Abilities a resolution makes: reflexive, delayed, granted, emblems | open |
| S8 | Engine-made abilities; the door is the only route | open |
| M1 | Rules events: combat, steps and phases | open |
| M2 | Rules events: spells, targets, damage, counters, life, tap | open |
| M3 | Rules events: library, actions, sagas, the rest | open |
| F | Delete the legacy types; docs | open |

A row's status is `open`, `done`, or `done but: <ids from Carried over>`. Nothing else goes in it.

## Where the code stands

Measured at `feeb04cb67`. Re-check a count with a grep before acting on it.

- `EffectSlot` is an empty enum. 103 main source files outside the card modules and 18 card files
  still name it, through the `addEffect(slot, …)` adapter and `LegacySlotTrigger`.
- Cards write a triggered ability as one `addTrigger(Triggers.x()…, effects)`. The effects a card
  writes are what should reach the stack.
- Two kinds of rules event exist (`ZoneChangeEvent`, `PermanentChangeEvent`). They go through
  `RulesEventPipeline` → `TriggerIndex` → `TriggerMatcher` → `TriggeredAbilityStackService`
  (`service/trigger/pipeline/`).
- Every other family is still matched by its old collector: 13 `…Watch` enums (about 460 constants,
  137 of them in `ZoneChangeWatch`), the `…AbilityReader`s, 92 `@CollectsWatchTrigger` handlers in 6
  files, `TriggerCollectorRegistry`, `TriggerContext`, `TriggerMatchContext`.
- A triggered stack entry is built by hand in many places. `StackEntryType.TRIGGERED_ABILITY` is
  named 1,058 times: `service/trigger` 341, `service/effect` 260, `service/turn` 127,
  `service/input` 93, `service/combat` 89, `service/battlefield` 42, others 106.
  `enqueueTrigger(` is called from 64 files, `handOffTriggerRecord(` from 38.
- Tests that guard the stack side: `TriggerRouteRecordTest` (386 watches) and
  `ReflexiveRouteRecordTest`, in the application module's `service/trigger` test package.
- Test baseline: no card test is known to fail. Application 597 cases, engine 1 and AI 10 fail at
  HEAD (mock wiring). Compare a run per case against a HEAD worktree run, not against these numbers.

## Why sweeps

Step 15 tried to make every hand-written route behave the way one route would, and checked it by
audit. Six independent verifications each found another way two routes differed, and each added a
clause to the done condition. The rules bugs they found were real. The cause is that the routes
exist, so the fix is to remove them: a family that reaches the stack through one service has
nothing left to compare.

## Target

**The stack door.** `TriggeredAbilityStackService` gets one entry point that takes a triggered
ability whole, its source and controller, and the record of the event. For every ability it does
the same things in the same order:

1. Checks the intervening "if" as the ability triggers and wraps it to be checked again on
   resolution (CR 603.4). Checks and marks limits. Adds additional triggers.
2. Builds one stack entry from the ability's effects as the card wrote them (CR 603.3). A "you may"
   stays in the effects and is asked on resolution (CR 603.5).
3. Stamps the event record on the entry. The record does not depend on what the effects are.
4. Announces the mode (CR 603.3c) and chooses targets (CR 603.3d). If the ability has to wait for
   its controller, the door parks it with its record and finishes it through one continuation. An
   ability with no legal choice is removed.
5. Hands the entry to `GameData.enqueueTrigger`. Ordering stays where it is (CR 603.3b).

The door never branches on an effect's concrete class. What it needs from an effect (its target
spec, whether it is modal) it asks through the effect's interface.

**A collector** detects the event, finds the abilities that watch it, judges the trigger's filters
and event conditions, builds the record, and calls the door. It does not read, unwrap, rebuild or
reorder `effects()`, and it does not build a stack entry.

**Rules events.** After the S sweeps a collector is only a matcher. The M sweeps replace each with
an event kind in the sealed `RulesEvent` hierarchy, emitted at the choke point into the current
batch (CR 603.2c) and matched by `TriggerIndex` and `TriggerMatcher`.

## Rules correctness

This comes before everything else in a sweep, including finishing it.

- **Oracle text decides.** Before changing how a card's ability reaches the stack, read the card
  with the Scryfall MCP `get_card`. If the wording leaves a question, read `get_rulings`, then
  search the web. A test that contradicts Oracle text is rewritten, and the report says so.
- **Every difference is adjudicated.** The ledger (below) lists what changed on the stack. Each
  line is a rules fix or a regression. A rules fix cites the Oracle text and a CR number and gets
  a card test that fails before the change. A regression is fixed. "Explained" without a citation
  is not an outcome.
- **CR numbers** are verified with the `rules` MCP `get_rule` before they are written anywhere.
- **Timing is behaviour.** When an ability triggers, what it sees and what it remembers must not
  move: look-back triggers read the object as it last existed (CR 603.10a), a permanent entering is
  seen by the permanents that entered with it (CR 603.6a), a reflexive ability triggers during the
  resolution that made it (CR 603.12), and a delayed ability keeps its source and controller
  (CR 603.7e).
- **No guessing under uncertainty.** If Oracle text, rulings and the CR do not settle a case, stop
  and ask the user. The two decisions under Carried over are of this kind.
- The user runs the full card suite at the end of every sweep, before the commit.

## Tools built in S0

- **Ratchet** (`TriggerStackDoorRatchetTest`, engine module, beside
  `GameMutationBoundaryRatchetTest`). It scans the main sources and counts, per file outside the
  door: (1) `StackEntryType.TRIGGERED_ABILITY`; (2) calls of `enqueueTrigger`,
  `handOffTriggerRecord` and `queueMayAbility`; (3) in the files on lists 1 and 2, `instanceof` of
  an effect class and reads of an ability's `effects()`. A committed allowlist holds the exact
  count per file. The test fails when a count is above or below its entry, so the list is always
  the true remainder and can only be lowered.
- **Ledger.** A test-fixture recorder on `RulesObserver.triggerQueued` (the hook
  `ShadowTriggerRecorder` uses). With an environment variable set, it appends one line per
  triggered ability that reaches the stack: test id, source name, effect classes in order, and the
  record fields by role (source, player, target), not by raw id. Run it over a sweep's card test
  classes before and after, and diff.

## Ground rules

- AGENTS.md applies in full. Work on main. Don't commit until the user says so.
- Don't keep two paths. When a family goes through the door, delete its entry building, its
  class-keyed branches, and every handler that now does what the door does.
- New `GameData`, `StackEntry` or `Permanent` state is copied for simulations with a deep-copy test
  case. A new `Card` field is copied, frozen, and counted in `CardFreezeTest`.
- Card classes and card tests change by script, per letter module. Commit a script only if a later
  sweep reuses it.
- Convert every reader of what a sweep removes: engine, AI, networking, test fixtures.
- When the authoring API changes, update `agent-docs/` and the `implement-card` and `review-card`
  skills in the same sweep.
- **A sweep is not split by what is left.** A site that can't be converted stays on the ratchet
  allowlist and gets one line under Carried over with the reason. The sweep closes as `done but`.
  S8 can't close while the list has an entry.
- No verification runs between sweeps. The ratchet count and the ledger diff are the check.

**Token rules.**

- This file stays short. A Status row is one line. A sweep's Findings are at most 12 lines.
- Findings hold only: rules fixes (card, what changed, CR), what a later sweep must know, and
  open items. No narrative, no test lists, no counts that a grep gives. Tests changed and test
  results go in the report to the user.
- An invariant goes in `agent-docs/architecture/triggers.md`, not here. Replace the paragraphs a sweep makes
  obsolete; don't append. Don't cite sweep or run ids there or in Javadoc.
- Never read whole: the history file, `TriggerCollectionService.java` (17,000 lines),
  `agent-docs/TRIGGER_SLOT_TARGETING.md`, `agent-docs/architecture/triggers.md`. Grep for the method or
  heading and read that range.
- Take inventories from the ratchet allowlist and from scripts that print counts. Don't paste
  listings into the conversation or into this file.
- Read test results from the JUnit XML with a script that prints only the failures that differ
  from the baseline.

## Every S sweep

1. **Inventory.** The ratchet entries for the sweep's files, and the cards whose abilities those
   files collect.
2. **Ledger before**, over the card test classes of those cards and the two route tests.
3. **Convert.** Each collector builds the record and calls the door. Delete what the door replaces.
4. **Cards.** Where a collector knew something by an effect's class, the card writes it as printed
   (a trigger filter, an event condition, an option, an effect that reads the entry). Read the
   Oracle text of each such card first.
5. **Ledger after.** Adjudicate every difference as Rules correctness says.
6. **Tests.** The unit tests of the services touched (add cases for new door behaviour), the two
   route tests, the card classes of the cards touched. Put the `--tests` filter after each
   module's test task. One Gradle build at a time.
7. **Close.** Lower the ratchet. Update `triggers.md`. Set the Status row. Write Findings.
   Report tests changed and why. Ask the user to run the full card suite.

**Done when:** the sweep's files have no ratchet entry (or are listed under Carried over), the
ledger diff has no unadjudicated line, and no new test fails.

## Sweeps

### S0: the door, the ratchet, the ledger

- Build the ratchet and the ledger. Commit the first allowlist as measured.
- Give `TriggeredAbilityStackService` the entry point under Target. Read how
  `GameData.handOffTriggerRecord`, `StackEntry.carryTriggerRecord`, `TriggerTargetCollector`,
  `StackedModalTriggerService` and `StackedDividedDamageTriggerService` work today and move that
  behaviour behind the door. The parked routes (target choice, two target groups, graveyard
  target, mode, mode with target, division) finish through one continuation. The interaction
  handlers in `service/input` and `TriggeredAbilityQueueService` that finish a route today call it.
- Measure which abilities reach `ChoiceHandlerService.beginResolvingModalTargetChoice` (a probe
  over the card suite) and decide from the result whether the door owns that choice.
- Pilot: route the block family through the door (`CombatBlockService`'s blocker, effect-made
  block and "becomes blocked" collectors). It proves the entry point before S1 uses it.
- Write the door's contract into `triggers.md`.

### S1: combat

`CombatAttackService`, `CombatDamageService`, `CombatService`, `CombatTriggerService`, the rest of
`CombatBlockService`, `CombatAbilityReader`, `CombatWatch` (56). About 89 sites.
Carried over: C3, C4.

### S2: steps, phases, draw step, planar

`StepTriggerService`, `TurnCleanupService`, `DrawService`'s step abilities, `PlanechaseService`,
`StepAbilityReader`. About 135 sites.

### S3: spells, abilities, becomes the target

`SpellCastTriggerCollectorService`, `LandTapTriggerCollectorService`,
`PlayedCardNameTriggerCollectorService`, and in `TriggerCollectionService` the cast, copy,
activation and becomes-the-target collectors. `SpellAbilityWatch` (26), `TargetWatch` (21).
Carried over: C1, C8 (closed).

### S4: damage, counters, life, tap

`DamageTriggerCollectorService`, the counter and life collector services, and in
`TriggerCollectionService` the damage, counter, life and tap collectors. `DamageWatch` (52),
`CounterWatch` (43), `LifeWatch` (8), `TapWatch` (16).
Carried over: C2, C21, C22, C28.

### S5: library, draw, discard, actions, sagas, the rest

`MiscTriggerCollectorService`, `DiscardTriggerCollectorService`, the small
`…TriggerCollectorService` classes (scry, seek, dice, dungeon, the Ring, voting, day and night,
conjure, discover, forage, exert, manifest dread, sagas, transform, face up, exiled card, commander
zone). `LibraryWatch` (23), `ActionWatch` (45), `MiscWatch` (21), `SagaWatch` (2).

### S6: zone changes

`EtbTriggerService`, `EnterTriggerCollectorService`, `DeathTriggerCollectorService`,
`PermanentRemovalService`, `GraveyardService`, `CreatureControlService`, the door's own
`triggerOwn` / `triggerOnce` / `triggerPermanentChange` paths, `ZoneChangeWatch` (137),
`CreatureDeathWatch` (13). Ask the user decision D1 before starting.
Carried over: C5, C6, C13.

### S7: abilities a resolution makes

The 260 sites in `service/effect`: reflexive abilities (`ReflexiveAbilitySupport`, 49 files),
delayed abilities (`DelayedAction` and the queues that are not one yet), abilities a resolution
registers, temporary global abilities, granted abilities, emblems, the may and pay handlers that
put an accepted follow-up on the stack. Extend `ReflexiveRouteRecordTest` to the routes it lacks
first (the accepted "you may", exert, earthbend, the forced cost, the after-cast ability, the
library follow-up, a reflexive group walk). Ask the user decision D2 before starting.
Carried over: C7, C9, C10, C11, C12, C14, C15, C24, C26.

### S8: engine-made abilities; the door is the only route

- Abilities the engine makes for a keyword or a rule (storm, cascade, and the like; state
  triggers) go through the door as any other ability.
- The ratchet allowlist is empty for lists 1 and 2. Then make the rule structural: only the door
  can build a triggered stack entry (a constructor or factory nothing else can reach), and the
  ratchet's allowlist file is deleted.
- C23, C25 and C27 (the upkeep target choices the door can't express, the engine-made step and cast abilities).
- Carried over is empty.

**Done when:** no code outside the door builds or queues a triggered stack entry, and the compiler
or the ratchet enforces it.

### M1 to M3: rules events

Depends on S8. For each family in the sweep:

1. Add its event kinds to `RulesEvent` and emit them at the choke point into the current batch.
   One occurrence is one batch (CR 603.2c).
2. List its candidates in `TriggerIndex`, as the layers leave them and in the zone the ability
   functions from. Match with `TriggerMatcher`.
3. Take the ledger before, switch the family to the pipeline, take the ledger after, adjudicate.
   The moment the old collector ran is part of the behaviour: keep it, or cite the rule for
   moving it.
4. Delete the family's `…Watch`, reader, collector, handlers and `TriggerContext` variants.

| Sweep | Families |
|---|---|
| M1 | combat; steps and phases |
| M2 | spells and abilities; becomes the target; damage; counters; life; tap |
| M3 | library, draw and discard; actions; sagas; the rest; what is left of `ZoneChangeWatch` |

**Done when:** the sweep's watches, readers and collectors are gone and the ledger diff is
adjudicated.

### F: delete the legacy types; docs

- Delete `EffectSlot`, `LegacySlotTrigger`, `LegacySlotTriggers`, the `addEffect(slot, …)` adapter,
  `TriggerCollectorRegistry`, `@CollectsWatchTrigger`, `TriggerContext`, `TriggerMatchContext`,
  `TriggerWatch`, what is left of `TriggerCollectionService`, and `ShadowTriggerRecorder` with
  shadow mode.
- Delete the `scripts/step*` conversion scripts and `scripts/effect-slot-*`.
- Rewrite `triggers.md` as the model that exists: abilities, rules
  events, the pipeline, the door. Remove every reference to this file's steps, runs and sweeps
  from `agent-docs/` (13 files) and Javadoc (22 files).
- Replace `agent-docs/TRIGGER_SLOT_TARGETING.md` with a short note on targets of triggered
  abilities. Update the two card skills.
- Delete this file.

**Done when:** a grep for `EffectSlot` finds nothing and the docs describe only the new model.

## Carried over

Open items from step 15 (rows 17z17d2 and 17z17e of the history file). Each closes in the sweep
named, with the Oracle text of the cards involved read first.

**Decisions for the user**

- D1 (before S6). A cast creature's own "enters, choose one" ability takes its mode from the cast
  request today (32 cards). CR 603.3c says the mode is announced as the ability is put on the
  stack. Keep or change? **Answer: change.** The mode is announced as the ability is put on the
  stack (CR 603.3c), not taken from the cast request.
- D2 (before S7). Two tokens made at once under a doubling effect: one reflexive ability or two?
  **Answer: one per token.** Ajani, Nacatl Avenger's ruling (2024-06-07): each Cat Warrior the
  ability made triggers "when you do" once, each with its own target (one doubler: two tokens, two
  abilities; two doublers: four and four). Tokens a replacement effect makes instead (Divine
  Visitation) don't trigger it.

**Sites to adjudicate** (a violation to fix, or correct with a citation)

| ID | Sweep | Site |
|---|---|---|
| C2 | S4 | `TriggerCollectionService.checkEnchantedPermanentTapTriggers`: a `MayPayManaEffect` that is not the lone effect gets no target as the ability goes on the stack |
| C5 | S6 | `EnterTriggerCollectorService.handleAnyPermanentEnterDefault`: a player target is chosen only for a `MayEffect` |
| C6 | S6 | `EtbTriggerService.processCreatureETBEffects`: a lone "you may" is routed by the wrapped effect's class |
| C7 | S7 | `TemporaryGlobalTriggeredAbility.effect()` keeps only the ability's first effect |
| C9 | S7 | Outfitted Jouster's reflexive ability is built by `DamagePreventionService` without `ReflexiveAbilitySupport` |
| C10 | S7 | `SpecializeKarlachEffectHandler` builds its own stacked entry |
| C11 | S7 | My Laughter Echoes: the replayed scheme keeps its old target |
| C12 | S7 | `ForcedCostOrElseEffectHandler.enqueuePaidEffects` has a fall-through that would put an "if you do" on the stack |
| C16 | S2–S7 | The door parks every route; the attack-target answers, the no-target chosen mode, the group-by-group walk of the record's own effects and the graveyard target's general answer finish through `finishParked`. A chosen mode's group-by-group or non-attack target (`EntersTriggerTarget`, `ETBTokenMultiTargetTrigger` on a mode's card copy, `ETBSpellTargetTrigger`), the counter move (`AttackCounterMoveFirstTarget`), the other branches of `processNextSpellGraveyardTargetTrigger`, `StackedModalTriggerService`, `StackedDividedDamageTriggerService` and `TriggerTargetCollector` still finish with their own entry and the hand-off carry; each moves behind the door in the first sweep whose family reaches it |
| C17 | S7 | The combat files' delayed abilities build their own entries (no `TriggeredAbility` to hand the door): `CombatService`'s end-of-combat actions, the `processDelayed…` readers of `CombatAttackService`, `CombatDamageService` and `CombatBlockService`, `checkDelayedBecomesBlockedTriggers`, Harsh Justice's reflection, Okoye's temporary global watcher |
| C18 | S8 | Engine-made combat abilities build their own entries: decayed, melee, battle cry, dethrone, training, double team, granted exalted, enlist's own ability, flanking, the monarch and initiative transfers, combat damage put on the stack |
| C19 | S5 | "You may exert this creature as it attacks" is written as an attack trigger; `CombatAttackService` reads its effect and asks it with `queueMayAbilityForPlayer` (CR 701.43d: it is an optional cost to attack, no ability) |
| C21 | S4 | A trigger record left in hand reaches an end-of-combat delayed ability (Scion of Calamity's myriad exile gets a combat-damage record, in one test or another between runs): a hand-off outside a routing scope, by the combat-damage collectors C22 leaves to S4 |
| C22 | S4–S7 | Combat-watch collectors in `TriggerCollectionService` still build entries: ally/any creature combat damage to a player, an opponent or its owner and "creatures you control deal damage" (S4), emblems and temporary global abilities (S7) |
| C23 | S8 | Upkeep target choices the door can't express stay in `StepTriggerService` (`queueOwnUpkeepTargetChoice`, `queueAnyNumberOfOtherPlayersTarget`): Axis of Mortality's two players, Puca's Mischief's interdependent pair, Cryptoplasm's and Vesuvan Doppelganger's copy target, Capricious Efreet's own permanent then the others, Curse of Surveillance's "any number of target players other than that player" |
| C24 | S7 | Delayed step abilities build their own entries: the `DelayedAction` drains of `StepTriggerService` (upkeep, draw step, main phases, end of combat, end step, cleanup), pending exile and delayed self returns, boons, `TurnCleanupService`'s cleanup triggers, `TurnProgressionService`'s additional-combat trigger |
| C25 | S8 | Engine-made step abilities build their own entries: the initiative, echo, suspend and time counters, Ertai's Meddling's delay counter, egg and refine counters, rad counters, epic, opening-hand reveals, the planar die's "Planeswalk" |
| C26 | S7 | Delayed, temporary and resolution-made cast and activation abilities build their own entries: `processDelayedControllerSpellCastTriggers`, `collectTemporaryControllerSpellCastTriggers` / `collectTemporaryGlobalSpellCastTriggers`, `collectCreatureSpellBoonTriggers`, the next-creature-spell cascade and delayed abilities, the Mirari Conjecture's copy grant, the mana-linked copies and Path of Ancestry's scry (`queueManaLinkedSpellCopyTriggers`, `checkManaLinkedAbilityCopyTrigger`, the spell-or-ability copy mana), `collectDelayedActivationCopies` (with C15) |
| C27 | S8 | Engine-made cast abilities build their own entries: prowess, increment, granted cascade, demonstrate and storm (`castTriggerGrants`, `queueGrantedStorm`), replicate and demonstrate (`collectSelfCastAbilities`), conspire, casualty, a cloaked permanent's ward (`enqueueCloakedWardTrigger`) |
| C28 | S4 | `LandTapTriggerCollectorService` is `TapWatch`'s handler, reached by the tap readers: a triggered mana ability resolves at once (CR 605.4a) and an ability waiting for the tapped permanent's mana choice is parked in `pendingManaAbilityTriggers`; the door has neither route |

**Fixes**

- C13 (S6). Riveteers Ascendancy reads `TriggeringPermanentManaValue`, so a sacrificed token or
  commander has its last-known mana value (CR 603.10a). Needs a card test.
- C14 (S7). `continueAfterTapCost` marks "do this only once each turn" as the other accepted
  answers do. No card has both today.
- C15 (S7). "Copy the next instant or sorcery spell you cast this turn" keeps no per-resolution
  breakdown: `GameData.pendingNextInstantSorceryCopyThisTurnCount`, `…SingleTriggerCount`,
  `…CountSource` and `…MaxManaValues` merge two resolutions into one stack object
  (`TriggerCollectionService.checkSpellCastTriggers`: `multiCopies`, `limitedCopies`; also
  `CopyNextInstantOrSorceryCastThisTurnForXValueEffectHandler`,
  `CopyNextInstantSorceryOrLoyaltyAbilityThisTurnEffectHandler`, two Sea Gate Stormcallers). Done
  means: one list of pending abilities per player (copies, limit, source card and permanent, the
  loyalty link); `checkSpellCastTriggers` and `collectDelayedActivationCopies` make one entry per
  element; a kicked Stormcaller's two limits stay one ability; the state is copied for simulations
  with a deep-copy case; a card test of two resolutions (Repeated Reverberation, Storm King's
  Thunder or Chandra, the Firebrand) fails before the change. 4 maps, 4 handlers, 23 card classes.

## Findings

One block per sweep, at most 12 lines, in the form the token rules give.

**S0.**
- Rules fix: block-family abilities now trigger an additional time under "triggers an additional time" effects (Roaming Throne, CR 603.3); the old collectors skipped them. Two `RoamingThroneTest` cases fail before.
- Rules fix: a target the card declares is chosen as the ability goes on the stack (CR 603.3d) in the "a creature blocks", "creatures block", "creatures you control become blocked", "blocks two or more" and graveyard routes, and a chosen target is no longer marked non-targeting (route-test fixtures; no printed card changed). Close Quarters declares its "any target" (CR 115.10a).
- Door API: `TriggeredAbilityStackService.put(TriggeredAbilityRequest)` with a `TriggerRecord` stamp; collectors judge filters with `ConditionEvaluationService.eventConditionMet`. Parked entries carry `StackEntry.parkedAtStackDoor` in the choice's `triggerRecord`; handlers call `finishParked`.
- Target rule: an effect chooses unless the record names a non-targeting object other than the source and the card declares no target. A later family whose cards print "target" beside "that creature" must declare it.
- The door counts additional triggers without an attack flag or `TriggerContext`; S1 (attack doublers) and S6 (death doublers) must add them to the request.
- Probe: over the 1,019 modal-card test classes only Gideon Blackblade's +1 (an activated ability with "up to one" target and none chosen) reaches `beginResolvingModalTargetChoice`, so the door doesn't own it. Open: that ability asks for a target as it resolves (CR 601.2c via 602.2b), outside this plan.
- Ledger: drop the ` #n` sequence and sort; two players' abilities arrive in map order.
- C16, graveyard part: Groffskithur's graveyard target finishes through `finishParked`, which now counts graveyard-card targets for the becomes-the-target and crime checks (CR 700.13). Ledger unchanged.

**S1.**
- Door API: requests carry the attack cause (`causedByAttack`, `causedBySourceAttacking`, CR 508.3a) and the doublers' event (`withEvent`); the door stamps the granting permanent and `StackEntry.isTriggeredBySourceAttacking`, and routes random graveyard targets, graveyard choices (`CardEffect.graveyardCardChoice`, narrowed to the record's triggering player), the defending or active player's target choice and the group-by-group walk. Its rules are in `triggers.md`.
- Target rule: the object a record names stands in only for an effect that could target nothing but its kind and type; "any target" beside "that creature/player" is chosen. Cards that print "that player" behind a fluent `target(...)` binding must split it (Destructive Urge).
- Prompt shape: two or more groups, dynamic counts, "up to one" and an Aura's later group are walked group by group; one group of several targets is one choice. Brago, Numot, Kitsune, Casey Jones, Norika and Heiko tests answer the new shape.
- Rules fix: "a backup ability" is the backup creature's own entering ability (CR 702.165a), not its attack trigger (Mirror-Shield Hoplite; the old route hid the bug by not setting `targetId`).
- C3 closed: a graveyard target takes the count its effect declares; the "you may" is asked as it resolves (CR 603.5). C4 closed: a mode chosen as it triggers isn't wrapped in the intervening "if"; the one such combat card, Jin Sakai, repeats it in each mode (CR 603.4).
- Records gained, no behavior change: the unblocked record's triggering player (the defending player), graveyard abilities' graveyard incarnation, Ebony Fly's own power, granting permanents. Stinkdrinker Bandit boosts "it" without targeting (its source is now the Bandit, CR 113.7).
- Dead code removed: the combat graveyard-targeting helpers of `GraveyardTargetingService`, `CombatTriggerService.putOnStackOrChooseMode`, `GainLifeEqualToDamageDealtEffect`.

**S2.**
- Door API: every step, phase, emblem and planar reader hands the ability to `put` with the step's record; `StepAbilityReader.triggering` judges only the event condition. Step handlers end with `StepTriggerService.beginNextStepTriggerChoice` (the auto-pass loop doesn't drain door choices after a step advance); tests that trigger through a service call `GameTestHarness.beginTriggerChoices()`.
- Door routes added, asked through `CardEffect` (`modal`, `modesChosenAtRandomAsItTriggers`, `dividesAmongTargetsAsItGoesOnTheStack`) and the card's groups; their rules are in `triggers.md`. `ConditionalEffect` and `SequenceEffect` now pass target hooks through (opponent-only, reflexive targets), which S3–S7 collectors inherit.
- Rules fix: Roaming Throne doubles every step ability of a creature of the chosen type (CR 603.3); the old readers did only your upkeep, precombat main and beginning of combat. `RoamingThroneTest` (Jin-Gitaxias's end step) fails before.
- Rules fix: Furnace Layer's "select target player at random" picks among legal targets (CR 115.1); a hexproof player was picked before. `FurnaceLayerTest` fails before.
- C20 closed: Cloak of Confusion's and Farrel's Mantle's source is the Aura (CR 113.7), the enchanted attacker the triggering permanent that deals the Mantle's damage (CR 608.2h). `CloakOfConfusionTest` (Roaming Throne naming the attacker's type) fails before.
- Rules fix (route fixtures, no printed card): a targeted end-of-combat, each-draw-step or graveyard step ability chooses its targets (CR 603.3d). Dread Wight, Joven's Ferrets and Kjeldoran Frostbeast declare no target, as printed.
- Prompt shape: one group of several targets is one `MultiPermanentChoice`; a permanent-only "up to one" target is one choice declined by choosing yourself; a "choose one" whose modes take one target is announced on the stack.
- `DrawService` has no step abilities: its entries are the draw family's (S5), as are sagas. HEAD failures outside S2: Star-Crowned Stag attacking a battle; Prying Blade is flaky.

**S3.**
- Door API: every cast reader (permanent, graveyard, hand, exile, command zone, emblem, plane, scheme, suspended card, a spell's own), the copy, card-advantage, land-play, countered-spell, mana-ability, activation, becomes-the-target and target-choice collectors and Firebender Ascension's reader hand the ability to `put` (`putTargetAbility`, `putSelfCastAbility`, `spellWatchRecord`, `activationRecord`); their handlers are gone. A cast's or activation's door choices begin at once (`TriggerCollectionService.beginStackDoorChoices`).
- Door rules added: a spell-only effect is aimed at the record's triggering spell unless the record names an object other than a player; "choose up to X" reads X as it goes on the stack (`CardEffect.maximumModesChosenAsItTriggers`); an `EventFactCondition` is judged once; an ability's own target filter declares its target; `TriggeringSpellMatches` is judged on the context's triggering spell; the door's player-target choice reads the record's triggering player.
- Rules fix: every "triggers an additional time" effect doubles becomes-the-target abilities (CR 603.3); `MonkGyatsoTest` (Annie Joins Up) fails before. Valiant Emberkin reads `TriggerContext.BecomesTarget`; Roaming Throne's duplicate target doubler is gone.
- C1 closed: a "you may pay" beside another effect has its offered target chosen as it goes on the stack (CR 603.3d; door test, no printed card). C8 closed: each granted cast trigger is its own entry (CR 603.3; no card has two). C22's S3 part closed.
- Cards as printed: ten cast triggers declare "target player" (Drowned Secrets), Pawpatch Recruit "other than that creature", Scalelord Reckoner "that player controls", Monk Gyatso airbends "that creature" (`AirbendTargetPermanentEffect(restriction)`), Artisan of Forms may target itself, Lozhan's two conditions share one group.
- Prompt shape: a cast "choose one" announceable on the stack is announced there; a cast trigger's "up to N target permanents" is one multi-permanent choice. Records gained: target and activation entries non-targeting with the targeting player; granted cascade's source is the spell (CR 113.7).
- `TriggerRouteRecordTest` excludes the target-watch variants whose own target retriggers the watch without end.

## Prompt

Replace `<ID>` with the sweep:

```
Implement sweep <ID> of effect-slot-refactor.md. Read that file whole (it is short) and check in
Status that the sweeps before it are done; if one isn't, stop and tell me. Follow its Rules
correctness, Ground rules and token rules and the steps under "Every S sweep" (or the M or F
section). Ask me the decisions it lists before you start. When it is done, set the Status row,
write at most 12 lines of Findings, tell me which tests changed and why, and ask me to run the
full card suite. Don't commit until I say so.
```
