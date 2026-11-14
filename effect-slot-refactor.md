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
| S4 | Damage, counters, life, tap | done but: C16, C28, C29, C30, C31, C32 |
| S5 | Library, draw, discard, actions, sagas, the rest | done but: C16, C19, C33, C34, C35, C36, C37, C38 |
| S6 | Zone changes: enters, dies, leaves, graveyard | done but: C6, C13, C29, C39, C40, C41, C42 |
| S7 | Abilities a resolution makes: reflexive, delayed, granted, emblems | done but: C16, C43, C44 |
| S8 | Engine-made abilities; the door is the only route | done |
| M1 | Rules events: attacks, blocks, steps and phases | done |
| M2 | Rules events: spells, targets, damage (combat damage too), counters, life, tap | open |
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
Carried over: C2, C21, C22 (closed), C28 (to S8).

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
Carried over: C5, C6, C13, C29, C36.

### S7: abilities a resolution makes

The 260 sites in `service/effect`: reflexive abilities (`ReflexiveAbilitySupport`, 49 files),
delayed abilities (`DelayedAction` and the queues that are not one yet), abilities a resolution
registers, temporary global abilities, granted abilities, emblems, the may and pay handlers that
put an accepted follow-up on the stack. Extend `ReflexiveRouteRecordTest` to the routes it lacks
first (the accepted "you may", exert, earthbend, the forced cost, the after-cast ability, the
library follow-up, a reflexive group walk). Ask the user decision D2 before starting.
Carried over: C7, C9, C10, C11, C12, C13, C14, C15, C19, C22, C24, C26, C31, C37, C39, C42 (C39 and C42 are S6 remainders: fix C42 first, then move C39's watches to the door).

### S8: engine-made abilities; the door is the only route

- Abilities the engine makes for a keyword or a rule (storm, cascade, and the like; state
  triggers) go through the door as any other ability.
- The ratchet allowlist is empty for lists 1 and 2. Then make the rule structural: only the door
  can build a triggered stack entry (a constructor or factory nothing else can reach), and the
  ratchet's allowlist file is deleted.
- Abilities a resolution makes go through `putMade` / `putDelayed`, temporary-global and emblem abilities through `put` (done: C7, C9 to C12, C14, C15, C17, C19, C22, C24, C26, C31, C37, C43, C44).
- Done: C6, C16, C18, C23, C25, C27 to C30, C32 to C35, C38, C40, C41. The ratchet's allowlist is deleted; the ratchet now only guards that no file outside the door names `TRIGGERED_ABILITY` as a value.

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
| M1 | attacks and blocks; steps and phases |
| M2 | spells and abilities; becomes the target; damage, combat damage (what is left of `CombatWatch`, decided 2026-11-14: it runs interleaved with the damage family's collectors around state-based actions); counters; life; tap |
| M3 | library, draw and discard; actions; sagas; the rest; what is left of `ZoneChangeWatch` |

**Done when:** the sweep's watches, readers and collectors are gone and the ledger diff is
adjudicated.

### F: delete the legacy types; docs

- Delete `EffectSlot`, `LegacySlotTrigger`, `LegacySlotTriggers`, the `addEffect(slot, …)` adapter,
  `TriggerCollectorRegistry`, `@CollectsWatchTrigger`, `TriggerContext`, `TriggerMatchContext`,
  `TriggerWatch`, what is left of `TriggerCollectionService`, and `ShadowTriggerRecorder` with
  shadow mode.
- Delete the `scripts/step*` conversion scripts and `scripts/effect-slot-*`.
- The legacy trigger choice types are deleted (S8). Delete `ChoiceContext.ChooseModeChoice.triggerTime` (always
  false since S8) and its readers.
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
- D3 (S8, C44). "Create N tokens. When you do" (N > 1) under a doubler. **Answer:** it triggers once for each
  written quantity of the written tokens created, rounded down (one doubler: 2, two doublers: 4, a tripler: 3); a
  replacement's different tokens (Divine Visitation) and added tokens of another kind (Chatterfang's Squirrels) don't
  count; D2 is the quantity-1 case. Decided by extrapolation from Ajani's ruling (2024-06-07), Faebloom Trick's ruling
  and CR 603.12a; no official ruling covers it, and the rounding down is unconfirmed.
- D4 (S8). Enlist's "when you do" (CR 702.154a) isn't caused by the creature attacking. **Answer:** not
  attack-caused: Isshin doesn't double it, Firebender Ascension doesn't count it, as for exert's.

**Sites to adjudicate** (a violation to fix, or correct with a citation)

| ID | Sweep | Site |
|---|---|---|
| C2 | S4 (closed) | `TriggerCollectionService.checkEnchantedPermanentTapTriggers`: a `MayPayManaEffect` that is not the lone effect gets no target as the ability goes on the stack |
| C5 | S6 (closed) | `EnterTriggerCollectorService.handleAnyPermanentEnterDefault`: a player target is chosen only for a `MayEffect` |
| C6 | S8 (closed) | `EtbTriggerService.processCreatureETBEffects`: a lone "you may" is routed by the wrapped effect's class (with C41) |
| C7 | S8 (closed) | `TemporaryGlobalTriggeredAbility.effect()` keeps only the ability's first effect |
| C9 | S8 (closed) | Outfitted Jouster's reflexive ability is built by `DamagePreventionService` without `ReflexiveAbilitySupport` |
| C10 | S8 (closed) | `SpecializeKarlachEffectHandler` builds its own stacked entry |
| C11 | S8 (closed) | My Laughter Echoes: the replayed scheme keeps its old target |
| C12 | S8 (closed) | `ForcedCostOrElseEffectHandler.enqueuePaidEffects` has a fall-through that would put an "if you do" on the stack |
| C16 | S8 (closed) | The door parks every route; the attack-target answers, the no-target chosen mode, the group-by-group walk of the record's own effects and the graveyard target's general answer finish through `finishParked`. A chosen mode's group-by-group or non-attack target (`EntersTriggerTarget`, `ETBTokenMultiTargetTrigger` on a mode's card copy, `ETBSpellTargetTrigger`), the counter move (`AttackCounterMoveFirstTarget`), the other branches of `processNextSpellGraveyardTargetTrigger`, `StackedModalTriggerService`, `StackedDividedDamageTriggerService` and `TriggerTargetCollector` still finish with their own entry and the hand-off carry; each moves behind the door in the first sweep whose family reaches it |
| C17 | S8 (closed) | The combat files' delayed abilities build their own entries (no `TriggeredAbility` to hand the door): `CombatService`'s end-of-combat actions, the `processDelayed…` readers of `CombatAttackService`, `CombatDamageService` and `CombatBlockService`, `checkDelayedBecomesBlockedTriggers`, Harsh Justice's reflection, Okoye's temporary global watcher |
| C18 | S8 (closed) | Engine-made combat abilities build their own entries: decayed, melee, battle cry, dethrone, training, double team, granted exalted, enlist's own ability, flanking, the monarch and initiative transfers, combat damage put on the stack |
| C19 | S8 (closed) | "You may exert this creature as it attacks" is written as an attack trigger; `CombatAttackService` reads its effect and asks it with `queueMayAbilityForPlayer` (CR 701.43d: it is an optional cost to attack, no ability) |
| C21 | S4 (closed) | A trigger record left in hand reaches an end-of-combat delayed ability (Scion of Calamity's myriad exile gets a combat-damage record, in one test or another between runs): a hand-off outside a routing scope, by the combat-damage collectors C22 leaves to S4 |
| C22 | S8 (closed) | Combat-watch collectors in `TriggerCollectionService` still build entries: emblems and temporary global abilities (S7) |
| C23 | S8 (closed) | Upkeep target choices the door can't express stay in `StepTriggerService` (`queueOwnUpkeepTargetChoice`, `queueAnyNumberOfOtherPlayersTarget`): Axis of Mortality's two players, Puca's Mischief's interdependent pair, Cryptoplasm's and Vesuvan Doppelganger's copy target, Capricious Efreet's own permanent then the others, Curse of Surveillance's "any number of target players other than that player" |
| C24 | S8 (closed) | Delayed step abilities build their own entries: the `DelayedAction` drains of `StepTriggerService` (upkeep, draw step, main phases, end of combat, end step, cleanup), pending exile and delayed self returns, boons, `TurnCleanupService`'s cleanup triggers, `TurnProgressionService`'s additional-combat trigger |
| C25 | S8 (closed) | Engine-made step abilities build their own entries: the initiative, echo, suspend and time counters, Ertai's Meddling's delay counter, egg and refine counters, rad counters, epic, opening-hand reveals, the planar die's "Planeswalk" |
| C26 | S8 (closed) | Delayed, temporary and resolution-made cast and activation abilities build their own entries: `processDelayedControllerSpellCastTriggers`, `collectTemporaryControllerSpellCastTriggers` / `collectTemporaryGlobalSpellCastTriggers`, `collectCreatureSpellBoonTriggers`, the next-creature-spell cascade and delayed abilities, the Mirari Conjecture's copy grant, the mana-linked copies and Path of Ancestry's scry (`queueManaLinkedSpellCopyTriggers`, `checkManaLinkedAbilityCopyTrigger`, the spell-or-ability copy mana), `collectDelayedActivationCopies` (with C15) |
| C27 | S8 (closed) | Engine-made cast abilities build their own entries: prowess, increment, granted cascade, demonstrate and storm (`castTriggerGrants`, `queueGrantedStorm`), replicate and demonstrate (`collectSelfCastAbilities`), conspire, casualty, a cloaked permanent's ward (`enqueueCloakedWardTrigger`) |
| C28 | S8 (closed) | `LandTapTriggerCollectorService` is `TapWatch`'s handler, reached by the tap readers: a triggered mana ability resolves at once (CR 605.4a) and an ability waiting for the tapped permanent's mana choice is parked in `pendingManaAbilityTriggers`; the door has neither route |
| C29 | S8 (closed) | `TrainEffectHandler` builds its own entry: Savior of Ollenbock's "up to one other target creature or creature card from a graveyard" is a mixed-zone choice (`BattlefieldAndGraveyardCardChoosingEffect`, `GraveyardTargetingService.handleBattlefieldAndGraveyardExileTargeting`) the door has no route for; Angel of Serenity's enters needs the same route |
| C30 | S8 (closed) | Engine-made counter, life and damage abilities build their own entries: speed (`increaseActivePlayerSpeedIfEligible`), vanishing (`PermanentCounterSupport.checkLastTimeCounterRemoved`), evolve (`collectEvolveTrigger`), Judgment of Alexander's prevention (`checkCommanderDamagePreventedTriggers`) |
| C31 | S8 (closed) | Delayed, temporary and emblem damage, life and tap abilities build their own entries: the `DelayedWatchedCreatureDealtDamage`, `…DealsDamage` and `…DealtDamageByAttackingCreature` drains, `collectLifeGainOpponentLifeLossTriggers`, `collectEmblemLifeGainTriggers`, `collectEmblemTapTriggers`, `collectTemporaryGlobalTriggers` for these watches, the temporary watchers in `dispatchSourceDealsCombatDamageTriggers` and `enqueueTemporaryAnyCreatureCombatDamageToPlayerTriggers`, `processDelayedDestroyTargetWhenSourceUntaps` |
| C32 | S8 (closed) | The combat damage step holds its entries off the stack while state-based actions run (`CombatDamageService`: `globalDamageTriggers`, the source-damage reflections), so a creature they name that dies isn't stamped with its last-known information (CR 608.2h) unless the entry is parked for a choice; `TriggerRouteRecordTest` excludes the targeted variants of "deals damage to a creature" and "a creature you control deals damage to a creature" |
| C33 | S8 (closed) | `LibrarySearchTriggerHelper` and `LibraryShuffleHelper` are static helpers (326 call sites) with no service to reach the door: the search and shuffle abilities build their own entries |
| C34 | S8 (closed) | `SagaChapterService`: a chapter that chooses targets (the chapter's own target groups and filters, a random mode's targets) builds its own entry; the door has no chapter-target route |
| C35 | S8 (closed) | The door has no spell-target route: a "when you cycle this card" ability that targets a spell and an exploit ability that targets one (Silumgar Sorcerer) keep their own choices, as does the cycling ability that announces its trigger's targets (Rampaging War Mammoth) |
| C36 | S6 (closed; rest in C40) | The transform, turned-face-up and commander-zone collectors (`TransformTriggerCollectorService`, `FaceUpTriggerCollectorService`, `CommanderZoneTriggerCollectorService`, `TurnFaceUpCopyService`) are reached through the door's own `triggerPermanentChange` and the zone-change dispatch |
| C37 | S8 (closed) | Emblem draw abilities (`DrawService.checkEmblemDrawTriggers`), the temporary discard watchers (`collectTemporaryGlobalDiscardTriggers`) and Cosima's voyage (`collectExiledVoyageTriggers`) build their own entries |
| C39 | S7 (closed) | The zone-change watches `TriggerCollectionService.keepsLegacyZoneChangeRoute` lists still go to their old collectors: every `CreatureDeathWatch`, tokens entering (a one-or-more ability split per token by its effects), a creature you control entering, an enchanted permanent dying or leaving, another nontoken artifact to a graveyard or exile, a creature card into an opponent's graveyard (Lazav), a permanent you sacrifice (C13), a card into your graveyard, cards exiled from hand, a spell exiling a permanent; through the door their card tests failed (C42, Lazav's copy and Hero of Bretagard's count, Measure of Wickedness, Necrotic Plague) |
| C40 | S8 (closed) | Turned-face-up and transform abilities keep their old putters (`keepsLegacyPermanentChangeRoute`, `TurnFaceUpCopyService`, `AnimationSupport.putTransformAbilityOnStack`): a spell target (C35), a battlefield-or-graveyard choice (C29), divided damage, Ravager of the Fells' "target opponent or planeswalker and up to two creatures that player controls", Brutal Cathar's exile |
| C41 | S8 (closed) | A permanent's own enters abilities (`EtbTriggerService`) still build their entries: the trigger-time value materialisation (`EtbEffectResolver`: entry facts, evoke, tribute), the 13 graveyard and spell routes of `OwnEntryRoute`, the lone-"you may" routes (C6) and the additional copies; the door has none of those routes |
| C42 | S7 (closed) | Two or more target choices one source's abilities park at the stack door in one event lose one: Psychomancer and a second one dying together put three of four abilities on the stack (`PsychomancerTest.eachPsychomancerSeesBothSimultaneousDeaths`), Blood Artist, Vigilante Justice; why is open (the pending count drops by two on the first answer) |
| C43 | S8 (closed) | An ability a resolution makes with target groups of its own that its card can't declare: Earth Rumble's "up to one target creature you control fights target creature an opponent controls" (a sorcery, whose card groups are its spell's) is still asked by `EarthbendThenFightTarget` under `ReflexiveAbilitySupport.route`; only its finished entry goes through `putMade`. Also left: Espers to Magicite's "up to one target creature card exiled this way" (`EspersToMagiciteCreatureChoiceInteractionHandler` builds its entry), `PermanentChoiceTriggerHandlerService`'s and `GraveyardChoiceHandlerService`'s triggered-entry continuations, still fed by ETB, Saga and spell routes (C34, C35, C41) |
| C44 | S8 (closed) | D2's open case: a "create N tokens. When you do" (N > 1 as written: Diregraf Horde, Faebloom Trick) under a doubler still triggers once; Ajani, Nacatl Avenger's ruling covers a one-token action only, and no ruling or rule says whether doubled multi-token actions trigger once or once per written batch |
| C38 | S8 (closed) | "As this Equipment becomes attached" choices (a color, a name and a type, an exiled creature to copy) are written as trigger effects and read by class in `checkEquipmentAttachedTriggers`: they are static abilities |

**Fixes**

- C13 (closed in S7). Riveteers Ascendancy reads `TriggeringPermanentManaValue`, so a sacrificed token or
  commander has its last-known mana value (CR 603.10a).
- C14 (closed in S8). `continueAfterTapCost` marks "do this only once each turn" as the other accepted
  answers do. No card has both today.
- C15 (closed in S8). "Copy the next instant or sorcery spell you cast this turn" keeps no per-resolution
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

**S4.**
- Door API: the damage, counter, life and tap collectors hand each ability to `put` (`TriggerCollectionService.putEventAbility` over `watchedAbilities` / `printedWatchedAbilities`, which read text changes and static grants; `PermanentCounterSupport.putCounterAbility`); `DamageTriggerCollectorService`, `LifeGainTriggerCollectorService`, `PermanentCounter…` and `ExiledCardTriggerCollectorService` are gone (the fights handler moved to `MiscTriggerCollectorService`). The door judges "once each turn for each creature" by `TriggeredAbilityRequest.withLimitCreature`.
- Cards as printed: Brash Taunter and Donna Noble declare "target opponent" (the door doesn't read `DamageDealingEffect.triggeredTargetFilter`), Toralf "any target other than that permanent"; Ghyrson Starn's "that permanent or player" is `DealDamageToAnyTargetEffect.toEventObject` (CR 115.10a); `SacrificeSelfThenEffect.reflexive`'s target is its reflexive ability's (Claim the Kingdom, CR 603.12).
- Door rule: a card's one bare target group is each of its abilities' target (Protector of the Wastes). Prompt shape: monstrous, Class level, untap and tap abilities answer `AttackTriggerTarget`; "up to N" is one `MultiPermanentChoice` (Shipbreaker Kraken, Protector).
- Now uniform, no card test failing before: the intervening "if" is judged as these abilities trigger where a collector judged none (graveyard life gain, Valgavoth, "you're dealt damage", self counters, CR 603.4), doublers apply to life-loss, monstrous and planeswalker-damage abilities (CR 603.3), Aura and planeswalker damage abilities go to the door as collected instead of being held for the stack.
- C2 closed (the tap collector goes through the door; C1's door test). C21 closed (combat-damage collectors route inside the door's scope; Scion of Calamity passes). C22's S4 part closed. C28 moves to S8; C29–C32 open.
- Ledger: route fixtures now announce modes and choose targets as they trigger (CR 603.3c, 603.3d); every other line is a record gained (source snapshot, triggering player, attacked target, granting permanent, graveyard incarnation) or random (Volatile Rig's flips, Strax's random player).

**S5.**
- Door API: the library, draw, discard, player and keyword action, misc and Saga collectors hand each ability to `put` (`TriggerCollectionService.putFamilyAbilities` / `putFamilyAbility` / `putJudgedFamilyAbility` with `familyRecord`, `putGraveyardFamilyAbilities`, `DrawService.putDrawAbility`, `DiscardRecords.record`); the 15 small collector services, the S5 handlers of `MiscTriggerCollectorService` and the `@Collects{Library,Action,Misc,Saga}Trigger` annotations are gone. Door choices begin where the old routes began theirs (`beginStackDoorChoices`, now public: after a resolution and after each old `processNext…` pump).
- Door routes added: "of an opponent's choice" (`CardEffect.targetChosenByAnOpponent`, Karplusan Minotaur); draw doublers through `TriggerContext.Draw`; cost-payment abilities (crew, saddle, station) held for the activation by `holdForActivation` (CR 603.3); the graveyard choice honours `graveyardChoiceMaxTotalPower` (Nethroi).
- Cards as printed: Entangling Trap declares "target creature an opponent controls", Choking Tethers its cycling trigger's own filter, Resounding Wave's two targets a group (the `selfCycledTriggerPermanentTargets` knob is gone), Jace's Erasure and Master's Councillors "target player" (a draw names the drawer as "that player"); the discard-to-battlefield replacement is a hand static (`EnterBattlefieldOnDiscardEffect`, CR 113.6m; five cards); Eriette's source is Eriette, the Aura its triggering permanent (CR 113.7).
- Now uniform, no card test failing before: these abilities' intervening "if", limits and "triggers an additional time" effects are the door's (CR 603.4, 603.2h, 603.3), a returned transformed Saga triggers each chapter its lore counters reach.
- C19 moves to S7: exert as it attacks is an optional cost to attack (CR 508.1g, 701.43d), a static-ability authoring change for 25 cards that S7's exert route belongs with.
- Ledger: every line is a record gained (source snapshot, non-targeting, the ability's target filter, triggering player and permanent controller, the crewed Vehicle), a route fixture announcing its mode or choosing its targets as it triggers (CR 603.3c, 603.3d), the door's target walk, or random (drawn cards, coin flips, conjures, random modes); a fourth Monument to Endurance discard with no mode left is removed (CR 603.3c). The fully unlocked Room's lost triggering permanent was a regression, fixed.
- Open (F): the choice types and pumps no collector queues any more (listed under F).

**S6.**
- D1 rules fix: a triggered "choose one" is announced as the ability goes on the stack (CR 603.3c, 700.2b), never as the spell is cast: 30 enters cards, Oracle of Tragedy, Invasion of Fiora and the five Retreats and Felidar Retreat are `ChooseOneAtTriggerTimeEffect`; the cast-time ETB mode is gone (`SpellCastingService`, `ValidTargetService`, `CardViewFactory`, `EtbEffectResolver`, `StackEntry.etbMode`; the old cast X no longer doubles as the mode). Propeller Pioneer's fabricate is `ChooseOneAtResolutionEffect` (CR 702.123a). D1 card tests choose the mode as the ability triggers.
- Door API: a source's own zone change goes through `put` (`ownZoneChangeRecord`; `triggerOwn` gone); another object's zone change and permanent change through `TriggerCollectionService.zoneChangeRequest` / `permanentChangeRequest` and `put`. The request carries `withCreatureDying()` (Teysa, not for sacrifice triggers) and a `PermanentEnters` event (Panharmonicon); an entry fact isn't rewrapped for entering events.
- Door routes added: `targetChosenByTriggeringPermanentController` (Necrotic Plague, `MayEffect` of `TRIGGERING_PERMANENT_CONTROLLER`, Confusion in the Ranks), a departed source's described graveyard target (Iname), its graveyard target's mana value limit as it last existed (Soulshift), "another target card" on a copy's physical card (Gixian Puppeteer), a chosen mode's graveyard choice reads the record's X (Orcus).
- Cards as printed: Ragnarok, Divine Deliverance declares its two targets as two groups; Iceberg Cancrix declares "target player"; Daily Bugle Reporters' mode takes "up to two target creatures".
- Prompt shape: an own death or leave target is the door's (`AttackTriggerTarget`, one `MultiPermanentChoice` for one group of several, group by group for several groups, removed at the group with no legal target); an enters watch's target is `AttackTriggerTarget`.
- Deleted: `TransformTriggerCollectorService`, `CommanderZoneTriggerCollectorService`, the converted watches' arms and handlers in `TriggerCollectionService` and the Death, Enter and Misc collectors, and their service tests.
- Ledger: every line is a record gained (source snapshot, non-targeting, triggering permanent, controller, owner, card snapshot, the batch's objects), an intervening "if" now outside a "you may" (graft, CR 603.4), a mode announced on the stack (CR 603.3c), the D1 cards' X 0 instead of their cast mode, or a test the run changed; `triggeringCards` of a one-card graveyard event is gone (its `triggeringCard` stays).
- Open: C39 and C42 (watches kept on their old collectors after their card tests failed through the door), C40 (face-up and transform putters), C41 and C6 (a permanent's own enters abilities), C13, C29.
- Environment: the card-data cache refetched `set-sld.json` on 2026-11-13 and maps SLD #921 to Return of the Wildspeaker, so every test class whose Spring context loads Goblin Matron fails with `initializationError` at HEAD too.


**S7.**
- C42 fixed: a door choice pump (`processNextAttackTriggerTarget`, the group walk, the graveyard and mode pumps) begins nothing while a choice awaits input; a second caller wrote over the prompt and lost a parked ability (Psychomancer, Blood Artist, Vigilante Justice, Falkenrath Noble each see every simultaneous death, CR 603.10a).
- C39 closed: every zone-change and creature-death watch goes through the door; `DeathTriggerCollectorService`, the legacy entry, token, sacrifice and graveyard handlers and `keepsLegacyZoneChangeRoute` are gone. The door chooses a described graveyard target on any departure (Exhumer Thrull's haunted creature) with its limit judged on the record.
- Rules fixes (cards as printed): Hero of Bretagard and Ranar the Ever-Watchful's "a spell or ability you control exiles one or more permanents" triggers once for that many (CR 603.2c; it triggered per permanent; `HeroOfBretagardTest` fails before); Blowfly Infestation declares "target creature"; Illicit Masquerade's and Puca's Covenant's "other" excludes the dying creature (`CardIsTriggeringCardPredicate`), Puca's counts that creature's counters and its target isn't "up to" (CR 603.3d).
- Cards as printed, no behaviour change: Belladonna Took and Nesting Dovehawk trigger per token ("a token enters"; the old collector split the batch by a resolution condition, Dovehawk put "1" as the event value); Fate Foretold draws for the dying creature's controller without targeting; Impending Doom reads the record's controller; Lazav reads the triggering card wherever it is.
- C13 closed: Riveteers Ascendancy reads the sacrificed permanent's last-known mana value (its token test also passes with the old amount: the token's card is still found as the target is chosen). D2 applied: a one-token "when you do" triggers once per token created (Ajani, Nacatl Avenger's ruling); a replacement's different tokens don't trigger it; C44 is the multi-token case.
- Prompt shape: ally-enter, sacrifice and haunt targets are `AttackTriggerTarget`; Necrotic Plague's controller is asked even with one legal creature.
- Ledger: every other line is a record gained (owner, mana value, exile stay, target filter, source snapshot, triggering permanent and cards) or an intervening "if" now outside a "you may" (soulbond, CR 603.4).
- Open: C43 (S8) — no ability a resolution makes reached the door in S7; the routes `ReflexiveRouteRecordTest` now covers (accepted "you may", paid cost, group walk, doubled token) are the regression net for that work. Exert, earthbend, the after-cast and library follow-ups are still unmeasured.

**S8 (part 1: abilities a resolution makes).**
- Door API: `TriggeredAbilityStackService.madeAbility(...)` builds the entry of an ability a resolution makes; `putMade` (reflexive, CR 603.12) and `putDelayed` (delayed, CR 603.7: an object its maker named is kept, nothing chosen again, CR 603.7c) route it as `put` does. `StackEntry.madeByResolution` limits it to the card groups its own effects are bound to. Temporary-global and emblem abilities go through `put` (`TriggeredAbilityRequest.ofEmblem`). Door choices begin after the resolution or answer (`InputCompletionService`), not mid-resolution. New door routes: spell targets, the event value in target filters, a target's declared player restriction, a restricted divided-damage group announcing its division (CR 601.2d via 603.3d).
- C19: "you may exert this creature as it attacks" is the static `ExertAsItAttacksEffect` (CR 701.43d, 508.1g); its "when you do" is the linked `Triggers.exertedAsItAttacks()` (CR 607.2h). C15: one `GameData.PendingSpellCopy` per delayed ability, each that applies one stack object with its maker as source (CR 603.7e); `SeaGateStormcallerTest` (two Stormcallers) fails before.
- Rules fixes: C11 My Laughter Echoes' replayed scheme chooses its targets again (CR 603.3d, new test). A paid cost's "if you do", Your Nightmares Are Delicious' draw and Vault 101's attach resolve inline (CR 608.2c; the Vault test fails before); the echo-paid ability still goes on the stack. "Up to one target" reflexive effects are written `SequenceEffect.upToOneTarget` (Rhino's Rampage, Obscura Interceptor, Psychic Pickpocket, Masterpiece Vault, Thorin, Season of the Bold); Tester of the Tangential's target is "another".
- Kept, unsettled by rulings: "triggers an additional time" effects don't count abilities a resolution makes.
- Prompt shape: a made ability's "up to one" target with none legal goes on the stack unasked; its graveyard target is one `MultiGraveyardChoice`; its other targets are `AttackTriggerTarget`.
- Ledger: count changes are new cases, abilities removed for want of a legal target (CR 603.3d), the C15 split, or the inline fixes; every other line is a record gained (source snapshot, source permanent, target filter, the exerted creature) or lost by a leak (Scion of Calamity's combat record), random library cards, or a prompt-shape target field.
- Open: C43's remainder (Earth Rumble's own target groups, Espers to Magicite, continuations still fed by C34/C35/C41 routes), C44, C16.

**S8 (part 2: engine-made abilities; the door is the only route).**
- Structural: `TriggeredAbilityStackService` is the one `StackDoor`; `StackEntry` constructors refuse `TRIGGERED_ABILITY`, the stack refuses an unreleased triggered entry or a carrier; `derivedFrom` for copies and nested records, `carrier` for entries never stacked. Allowlist deleted.
- Door API: `putInherent` (sourceless rule abilities, CR 726.2, 727.2), `putManaAbility` / `holdForProducedMana` / `putHeld` (CR 605.4a), `putWithAnnouncedTargets`, `finishParked(..., card, ...)`; `LibraryEvent` rules event; `CardEffect.asItTriggersOnEntering(EntryFacts)`, `battlefieldAndGraveyardChoice`, `choosesSecondTargetAgainstFirst`. C16: every choice holds the parked record itself; 16 legacy choice types deleted.
- Rules fixes (test fails before): granted exalted doubled by Isshin and decayed seen by Firebender (CR 508.3a); the initiative's abilities have no source (CR 727.2, `SeasonedDungeoneerTest`); echo, undying, persist and triggered mana abilities counted by "triggers an additional time" (CR 603.2d; Roaming Throne, Teysa, `BadgermoleCubTest`); storm's and demonstrate's source is the spell (CR 702.40a, 702.144a); combat-damage abilities get last-known information (CR 608.2h).
- D3/C44: `CreateTokenThenEffectHandler` triggers once per written quantity (`FaebloomTrickTest`: one doubler 2, two 4, Ojer Taq 3, Chatterfang 1). D4: enlist's ability no longer attack-caused (`IsshinTwoHeavensAsOneTest`, `FirebenderAscensionTest`).
- Cards as printed: Axis of Mortality, Curse of Surveillance, Capricious Efreet, Puca's Mischief (`MANA_VALUE_AT_MOST_FIRST_TARGET`), Ravager of the Fells (`CONTROLLED_BY_FIRST_TARGET`) declare their groups; Sanctuary Blade, Psychic Paper, Dinosaur Headdress are `AsThisBecomesAttachedEffect` (CR 614.1c); dash's return marker is gone (keyword-driven).
- Prompt shape: an own enters ability's group of several targets or "up to N" is one `MultiPermanentChoice`; a single target is `AttackTriggerTarget`. Regression fixed on the new prompt: Mysterious Stranger's "for each graveyard … target card" must pick in every eligible graveyard (`ONE_PER_CONTROLLER_IF_ABLE` enforced by the group walk); Orzhova and The Moonbase print "up to one" (`AT_MOST_ONE_PER_CONTROLLER`).
- Regressions fixed: two abilities of a card whose filters differ only by a one-component wrapper shared a "once each turn" key (Oasis of Renewal; `TriggeredAbility.limitKey`); Puca's Mischief's first target again requires a mana-value partner; Stockpiling Celebrant's target is one, not "up to one" (CR 603.3d). Open: other `MayEffect` cards written with a `0, 1` target where Oracle prints one (Deftblade Elite).
- Kept: exile-return "leaves" abilities and planar / graveyard-card abilities aren't counted by doublers; Ravenous Slime's "when a card is exiled this way" resolves inside the replacement; a state trigger removed for want of a target stays marked (no card has one).

**M1.**
- Rules events: a step or phase beginning is a `StepEvent`, a declaration of attackers an `AttackEvent`, a block (declared, none possible, made by an effect, entered blocking, blocked without blockers) a `BlockEvent`, each emitted as an occurrence of its own where its collectors ran and matched by `TriggerIndex` (`stepCandidates`, `permanentAbilities`) and `TriggerMatcher` (`matchedStep`, `combatCondition`) in `RulesEventPipeline.triggerStep`, `AttackTriggers` and `BlockTriggers`. Deleted: `StepAbilityReader`, `GrantedUpkeepEffectSupport`, every step, emblem-step and planar-step reader, the attack and block collectors of `CombatAttackService`, `CombatBlockService`, `CombatTriggerService` and `TriggerCollectionService`, the attack and block `CombatWatch` constants, `@CollectsCombatTrigger`.
- Scope (user, 2026-11-14): the combat-damage watches move to M2; they run interleaved with the damage family around state-based actions.
- Rules fixes: a face-down or ability-less permanent's card abilities never trigger and its static grants follow layer order (CR 708.2, 613.7; a manifested Ugin's Mastery no longer triggers on the attack, `UginsMasteryTest` fails before; Aura Flux after Humility, Town-Razer Tyrant); "whenever you attack" needs an attacker (CR 508.3d); a step ability triggers once however many of its conditions the step meets (CR 603.2c); a command-zone card's battlefield abilities don't trigger at beginning of combat (CR 113.6), Arahbo declares its eminence's command zone.
- Moment kept; within it the exile egg, refine and suspend counter abilities are step candidates in their old place, and the delayed and keyword abilities go on the stack just before (end-step boons, the keyword attack abilities) or after (flanking, delayed block abilities) the event's. Hero of Bladehold's default order is now tokens then battle cry (either is legal, its 2011-06-01 ruling).
- Records gained: the active player on every step ability, "that player" as triggering player on each, each-opponent's, beginning-of-combat and emblem abilities, the attached permanent and source snapshot on step abilities, both graveyard-stay fields on a graveyard step ability; lost: the your-draw-step `targetId` binding (no "that player" is printed). Ledger: every other line is random (modes, dice, coin flips, spellbook cards).

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
