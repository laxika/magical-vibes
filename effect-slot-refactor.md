# EffectSlot refactor

Replaces `EffectSlot` with abilities that are objects, matched against rules events by one pipeline
and put on the stack by one service. Steps 1 to 14 are done. What is left is done in **sweeps**: each
takes a whole family, routes it through the one service, and deletes the family's own code.

History: the earlier plan, with 22,000 lines of findings for steps 1 to 15, is
`git show feeb04cb67:effect-slot-refactor.md` (2.9 MB). Never read it whole. To look something up,
grep its `^#### Run` headings and read that range. The invariants those runs established are in
`agent-docs/architecture/triggers.md` (86 KB: grep it, never read it whole), and that is the place to read them.

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
| M2 | Rules events: spells, targets, damage (combat damage too), counters, life, tap | done |
| M3 | Rules events: library, actions, sagas, the rest | done but: C47 |
| R0 | Delete the dead `TriggerCollectionService` injections | done |
| R1 | Emblems' abilities are `TriggerIndex` candidates for every family | done |
| R2 | Keyword abilities and departed sources are candidates; abilities as the layers leave them | done |
| R3 | One delayed-ability model (temporary-global abilities and every delayed list), listed for every family | done but: C51 |
| R4 | Cast bookkeeping at the cast's completion; `TriggerCollectionService` deleted | done |
| F | Delete the legacy types; docs | done |
| D | Durations: one `Duration` on floating effects, ended by a clock on the rules events; `DelayedAction` deleted | open |

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
- **Ledger.** `TriggerLedger`, fed by the test fixtures' `RulesEventRecorder` on
  `RulesObserver.triggerQueued` (env var `MV_TRIGGER_LEDGER`). With an environment variable set, it appends one line per
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

### R0 to R4: what is left of `TriggerCollectionService`

What is left of the class is abilities `TriggerIndex` doesn't list: ones the engine makes for keywords and rules, ones
kept in `GameData` lists (delayed, temporary-global, boons, emblems' zone-change abilities), and the cast bookkeeping
around them. Each sweep makes one kind an ability the pipeline lists and matches, and deletes its hand-written reader
and drain; nothing is relocated to keep the class alive elsewhere. M sweep steps apply (ledger before and after, the
moment kept or a rule cited for moving it).

| Sweep | What |
|---|---|
| R0 | Delete the injections of `TriggerCollectionService` that call nothing (about 280 main classes), and their tests' constructor arguments. No behaviour change. |
| R1 | `TriggerIndex` lists emblems' abilities (they function in the command zone, CR 114.4) as candidates for every family, as it lists them for steps; each family's matcher judges them as it judges any other candidate, and the hand-written emblem readers go (the zone-change readers here, and those of `TapTriggers`, `LifeTriggers`, `DamageTriggers`, `LibraryTriggers`, `SpellTriggers`, `AttackTriggers`). Close C48. |
| R2 | Prowess, increment and evolve are keyword abilities `TriggerIndex` lists for a permanent with the keyword (CR 702.108a, 702.100); a departed permanent's snapshot abilities are listed as it last existed (C46); becomes-the-target abilities are read as the layers leave them (C45). |
| R3 | An ability a resolving spell or ability creates is a delayed triggered ability (CR 603.7a), once only or for its stated duration (CR 603.7b). `TemporaryGlobalTriggeredAbility` is one already (source, controller, duration, the permanent and player it watches); make it the one delayed-ability model, listed by `TriggerIndex` for every family (deleting the hand loops over `temporaryGlobalTriggeredAbilities` in seven families and here), then convert the other delayed lists to it family by family (deaths and leaves, cast and activation copies, the rest), deleting each drain as its family moves. The step family lists them too, and a step trigger can name the turn or phase it means ("that turn's end step", "that combat": an added phase is its own, CR 500.8). `ReturnExiledCardToHandAtNextEndStep` and `DelayedGraveyardToHandReturn` resolve without the stack: they are delayed abilities and go on it (CR 603.3). A `DelayedAction` that is a duration, not an ability (play permissions, an attack requirement), stays for D, with a line under Carried over. Deep-copy test for new state. |
| R4 | The cast bookkeeping of `checkSpellCastTriggers` moves to the cast's completion (CR 601.2i), the ledger confirming the abilities' order; what is left (boons, sacrifice records) moves to the family that owns it; delete `TriggerCollectionService`. |

### F: delete the legacy types; docs

- Delete `EffectSlot`, `LegacySlotTrigger`, `LegacySlotTriggers`, the `addEffect(slot, …)` adapter,
  `TriggerCollectorRegistry`, `@CollectsWatchTrigger`, `TriggerContext`, `TriggerMatchContext`,
  `TriggerWatch` and `ShadowTriggerRecorder` with shadow mode.
- C47 (closed): list the face-up planes' and schemes'
  zone-change abilities as `TriggerIndex` candidates, matched by `TriggerMatcher`, and delete the planar and scheme
  readers. Take the ledger before and after over the planar and scheme card tests and the two route tests, and
  adjudicate the moment they now trigger as Rules correctness says (keep it, or cite the rule for moving it).
- Delete the `scripts/step*` conversion scripts and `scripts/effect-slot-*`.
- The legacy trigger choice types are deleted (S8). Delete `ChoiceContext.ChooseModeChoice.triggerTime` (always
  false since S8) and its readers.
- Rewrite `triggers.md` as the model that exists: abilities, rules
  events, the pipeline, the door. Remove every reference to this file's steps, runs and sweeps
  from `agent-docs/` (13 files) and Javadoc (22 files).
- Replace `agent-docs/TRIGGER_SLOT_TARGETING.md` with a short note on targets of triggered
  abilities. Update the two card skills.

**Done when:** C47 is closed with its ledger diff adjudicated, a grep for `EffectSlot` finds nothing and the docs
describe only the new model.

### D: durations

Depends on F. An effect a resolving spell or ability makes lasts as long as it states (CR 611.2a). The engine keeps
most such durations as state named for one effect: `GameData` fields (`…UntilNextTurn`, `exilePlayPermissionsExpire…`,
`…AwaitNextTurnOfPlayer`, and those `…ThisTurn` fields that are effects rather than turn history), their
`EndOfTurnStateField` resets, an `EffectDuration` constant per wording with an `expire…FloatingEffects…` method per
constant called from its own place, and the `DelayedAction`s R3 leaves (C51).

1. **Inventory** by script: the fields that hold an effect's duration, apart from turn history ("spells cast this
   turn" is not an effect and stays).
2. **`Duration`** replaces `EffectDuration`: a start (now, or after this turn, CR 702.185a), an end event (end of turn,
   CR 514.2; a player's next turn, upkeep or untap step; end of combat; a matching cast; the affected object leaving
   its zone, CR 400.7), and an optional "for as long as" condition (one that never starts does nothing, CR 611.2b).
3. **One model.** Every effect a resolution makes is a `FloatingContinuousEffect` with a `Duration`, rules-modifying
   ones too (play permissions, attack requirements CR 508.1d, prevention, can't attack, hand size, flash grants).
   Whether its affected set is locked as it begins follows CR 611.2c; the layers apply only those that change
   characteristics (CR 613.1). Each field's readers become a query over floating effects.
4. **The clock.** Durations end at the rules events that end them, through `RulesEventPipeline`, without the stack
   (as CR 514.2). The `expire…` methods and their call sites go. The moment an effect ends is kept, or a rule is cited
   for moving it.
5. **Family by family.** The ledger records triggered abilities only, so first extend its recorder with a line per
   floating effect as it begins and ends (test id, source, effect, end event). Take it before and after each family and
   adjudicate as Rules correctness says. Delete each family's fields, resets and deep-copy lines; deep-copy test for new
   state.
6. **Close.** Delete `DelayedAction` and `EffectDuration`. Update `LAYER_SYSTEM.md`, `game-state.md` and the effect
   indexes. Delete this file.

**Done when:** `DelayedAction` and `EffectDuration` are gone, no `GameData` field holds an effect's duration, and the
ledger diff is adjudicated.

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
| C45 | R2 (closed) | `TargetTriggers` reads each watcher's becomes-the-target abilities as the old collectors did (`WatchedAbilities.printed`, duration and static grants, the mannequin and cloaked-ward cases), not as the layers leave them (`TriggerIndex.permanentAbilities`): an Aura's printed ability, one granted to the enchanted creature, bestow and a face-down cloaked ward are read differently, so making them uniform changes behaviour and needs its own ledger pass |
| C46 | R2 (closed) | A damage source or watcher that may have left the battlefield before its ability is put (combat-damage dealers, departed ally watchers, Equipment, snapshotted self abilities: `CombatDamageTriggers.allWatchedAbilities` / `grantedCombatAbilities` / the ally and attached passes of `processCombatDamageToPlayerTriggers`, `DamageTriggers.dispatchSourceDealsCombatDamageTriggers` / `collectDealsDamageToCreatureAbilities` / `putEquipmentCombatDamageAbilities`) is read as it last existed by per-watch reads, not through `TriggerIndex`, which can't list a departed permanent's snapshot abilities yet |
| C47 | F (closed) | Planar and scheme zone-change abilities (`TriggerCollectionService`'s planar enters, leaves, land-enters and death readers, the scheme leaves reader, the planar nonblack-card reader) still run at their old call sites, selecting abilities by their trigger's fields; `TriggerIndex` doesn't list planar or scheme candidates for zone changes, and moving them changes the moment they trigger, which needs its own ledger pass |
| C48 | R1 (closed) | A battlefield departure's "put into a graveyard from anywhere" observers are read as leaves-the-battlefield abilities: `TriggerIndex.observerCandidates` lists them as they were before the event and `TriggerMatcher.filterMatches` judges their filter on the permanent as it last existed (CR 603.10a), but CR 603.6c says such an ability is never a leaves-the-battlefield ability (every observer written `Triggers.putIntoGraveyardFromAnywhere(filter, relation)`, The Dark Barony's "nonblack card" among them); changing it needs its own ledger pass over those cards |
| C49 | R2 (closed) | The combat-damage passes' emblem readers (`DamageTriggers.checkEmblemAllyCreatureCombatDamageToPlayerTriggers`, `checkEmblemCombatDamageTriggers`) are still hand-written: those passes read their watchers as `List<Permanent>` with departed watchers, batched fire marks and owner-relative passes, which C46 reworks; emblems join them as watchers there |
| C50 | R3 (closed; fixed) | Echo is `KeywordTriggers.echo`, listed by `TriggerIndex.permanentAbilities` for a card's static `EchoEffect` (CR 702.30a); `EchoAtNextUpkeep` and its drain are gone |
| C51 | R3 (closed) | `DelayedAction` keeps what isn't an ability: play permissions granted or revoked at a turn, `TargetCreatureMustAttackNextCombat`, `PhasedOutUntilEndOfNextTurn`, the attack restrictions of `DelayedAdditionalCombatBeginningEffect`, attacker and blocker declaration control, Camouflage, Sacred Boon's prevention shield (`DelayedPlusZeroPlusOneCounters`), and the damage doubling and multiplication replacements (CR 614.1a); closes in D |
| C52 | R3 (closed; fixed) | Siren's Call and Maddening Imp are a rules-modifying "attack this turn if able" (CR 611.2c) and a delayed end-step ability about that player; `DestroyNonAttackersAtEndStep` is gone |

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

**M2.**
- Rules events: a spell cast, copied, countered or fizzling, a spell's card advantage, a land played, an ability activated and a mana ability resolving are a `SpellEvent`; becoming the target and choosing targets a `TargetEvent`; damage a `DamageDealtEvent` and a combat damage step a `CombatDamageStepEvent` (`DEALT`, `AFTER_STATE_BASED_ACTIONS`); counters put or removed and the counter keyword actions a `CounterChangeEvent`; tapping, untapping, the untap step, teamwork and tapping for mana a `TapEvent`; gaining, losing and paying life a `LifeEvent`. Each is emitted as an occurrence of its own where its collectors ran and matched by `SpellTriggers`, `TargetTriggers`, `DamageTriggers`, `CombatDamageTriggers`, `CounterTriggers`, `TapTriggers` and `LifeTriggers`.
- Deleted: `SpellAbilityWatch`, `TargetWatch`, `DamageWatch`, `CombatWatch`, `CounterWatch`, `TapWatch`, `LifeWatch`, their readers, `SpellCastTriggerCollectorService`, `PlayedCardNameTriggerCollectorService`, `LandTapTriggerCollectorService`, `@Collects{Damage,Counter,Tap,Life}Trigger(s)`, these families' collectors in `TriggerCollectionService`, `CombatDamageService`, `DamageSupport` and `PermanentCounterSupport`, 27 `TriggerContext` variants (the doublers' cast, copy, becomes-the-target and damage variants stay for F).
- Moment kept: every printed cast ability triggers together where the battlefield ones did, the engine's keyword and delayed cast abilities just before or after the `Cast` event (a spell's own "when you cast" now goes on the stack before granted replicate and demonstrate, either order being the caster's, CR 603.3b); combat-damage abilities that wait out state-based actions are still held; `DamageSupport`'s permanent and player clusters run once after the damage bookkeeping; `LifeSupport.lifeLost` keeps "life lost this turn" and speed (CR 702.179d).
- Rules fix: a live permanent's spell, activation, damage, counter, tap and life abilities are the ones it has as the layers leave it (`TriggerIndex.permanentAbilities`): a face-down or ability-less permanent's card abilities don't trigger (CR 708.2, 613.1f), granted and statically granted abilities trigger where a collector read printed ones only, printed ones through their text changes (CR 612.1). `DeathPitsOfRathTest.faceDownDeathPitsDoesNotTrigger` fails before; no other card test did.
- Regression fixed on the way: the combat matcher still called the old temporary-global collector (Surge to Victory, Mistway Spy).
- Ledger: 68 baseline tests differ, all random (route fixtures' drawn, discarded, clashed or revealed cards and die rolls; Volatile Rig's, Creepy Doll's, Setzer's and Breeches's coin flips; Strax's random player; Outlaws' Merriment's random token; Consuming Oni's random card). No ability is gained or lost.
- Open: C45, C46. The counter-removal event is emitted on every state-based-action check, removed or not; the tap matcher keeps `creatureTapCountsThisTurn` and `playersWhoTappedLandForManaThisTurn` while matching, at the old moment. `BreathOfFuryTest`'s unsuccessful-sacrifice case fails run alone at HEAD too.

**M3.**
- Rules events: `LibraryEvent` (drew, revealed a drawn card, discarded, a discard event's cards, cycled, scried, surveilled, searched, shuffled), `ActionEvent` (emitted through `ActionEvents`, which keep the collectors' bookkeeping), `ControlChangeEvent`, `AttachmentEvent`, `CrewEvent`, `DayNightEvent`, `RoomEvent`, `SagaEvent`, `EntryCausedTriggerEvent`, each occurring where its collector ran and matched by its `…Triggers` class from `TriggerIndex`; state triggers have no event (CR 603.8) and stay checked by `StateTriggerService`, candidates from `TriggerIndex`.
- Deleted: every watch (`Library`, `Action`, `Misc`, `Saga`, `ZoneChange`, `CreatureDeath`, `TriggerWatch`), every family reader, `DiscardRecords`, `MiscTriggerCollectorService`, `@CollectsWatchTrigger(s)`, the family collectors and helpers of `TriggerCollectionService` and its registry dispatch chain, the `TriggerContext` variants nothing read (the doublers' stay).
- Zone changes: the door's routing and `zoneChangeRequest` read the matched trigger, "that player" by its relation (`triggers.md`). Cards declare what the relation doesn't give: Liability `TRIGGERING_CARD_OWNER`, Lucy MacLean "target player other than its controller"; Last Laugh, Yomiji, Tarrian's Soulcleaver are `ANOTHER`, Dredging Claw and Flayer of the Hatebound `YOU_OWN` (CR 400.3), Prized Amalgam's condition its trigger filter.
- Rules fixes (test fails before): a face-down or ability-less permanent's action abilities don't trigger (CR 708.2, 613.1f; `DeepmuckDesperadoTest`); "when you cycle this card" is read as the card has it where it wound up (CR 702.29c), so Krosan Tusker doesn't trigger under Yixlid Jailer (its ruling; `KrosanTuskerTest`); Siona sees an enchantment that becomes an Aura attached to your creature (`SionaCaptainOfThePyleasTest`).
- Now uniform, no card test failing before: grants, text changes and lost abilities apply to the draw, search, shuffle, discard, crew, station, attach, control-change, champion, exploit and lose-game readers; Strict Proctor and Equipment unattach judge their event condition.
- Moment kept: every event occurs at its collector's call site; a shuffle ability goes on the stack unlogged (a shuffle can happen outside a mutation scope); state triggers are checked as a discard event that discarded a card closes.
- Ledger: every difference is "that player" gained or lost by the relation rule (no lost one was read; no gained one binds a declared target), random (drawn and discarded cards, coin flips, dice, a clash's top cards: route reruns flip it), or a new test.
- Regression fixed: a land played from outside the game entered "from exile" (the dropped watch check had read `Permanent.isEnteredFromExile`); it now enters from `OUTSIDE_GAME` (CR 400.11a), so Fire Lord Zuko doesn't see it (`FrontierExplorerTest`).
- Open: C47. F must drop the ~267 unused `TriggerCollectionService` fields of handlers; `TriggerCollectorRegistry` has no handler left; Saga chapter abilities are still read by chapter in `SagaChapterService`.

**F (part 1: C47).**
- C47 closed: the face-up planes' and schemes' zone-change abilities are observer candidates (`TriggerIndex.commandZoneObjectCandidates`; CR 901.7, 314.4), matched by `TriggerMatcher` (a plane's whatever zones its conditions name); a plane's `TriggerCandidate` carries the `PlanarObject` its record stamps. The planar enters, leaves, land, death and graveyard readers and the scheme leaves reader are gone.
- Moment: enters unchanged (the readers ran right after `triggerEnteringObservers`); leaves, death and graveyard arrivals move from the removal and graveyard call sites into their zone change's batch: the same event, before any player receives priority (CR 603.3), the order among the planar controller's abilities theirs (CR 603.3b).
- Cards as printed: The Dark Barony's "that player" is the card's owner (`LoseLifeRecipient.TRIGGERING_CARD_OWNER`, CR 400.3). A zone-change record of a card moved between zones other than the battlefield stamps the owner the event names (`triggeringPermanentOwnerId`): a card in a hand or library may have no `ownerId`.
- Ledger: every line is a record gained (owner, last-known snapshot, toughness, mana value, non-targeting), Goldmeadow's own-land `targetId` dropped (its handler reads the land's controller first), The Dark Barony's `targetId` and `triggeringCards` replaced by the owner, or random (route-fixture discards and clashes).
- Open: C48.

**F (part 2: `TriggerContext`).**
- Doublers judge a `TriggerCause` (`TriggeredAbilityRequest.withCause`; CR 603.2d): entered, creature died, became the target, spell cast or copied, turned face up, drew, damage (combat damage to a player, permanents dealt damage, looked up as counted). The rules events are too coarse for it (a `TargetEvent` is the whole targeting spell, the damage events aggregate sources). Each judgment and lookup is the old one; the doubler card ledger is unchanged.
- `DamageTriggers` keeps the four damage records its matchers read (`SourceDealsDamage`, `SourceDealsCombatDamage`, `DamageToCreature`, `SourceDamageToYouOrYourPermanent`), each with its `cause()`; the variants no doubler read hand the door none. Deleted: `TriggerContext`, `TriggerCollectorRegistry`, `TriggerCollectorHandler`, `@CollectsTrigger(s)`.

**F (part 3: `TriggerCollectionService` split, mechanical).**
- Moved to their owners, no behaviour change: the door's choice pumps (`TriggeredAbilityQueueService.beginStackDoorChoices`; callers call the queue and `ETBTokenTargetService` pumps directly), the zone-change and permanent-change requests and records (`ZoneChangeRequests`), the becomes-the-target and target-choice events (`TargetTriggers`), land play (`ActionEvents.landPlayed`, a special action, CR 116.2a) and dungeon completion (`ActionEvents`), equipment becoming attached (`AttachmentTriggers`), clash (`ClashSupport`), the tap batch (`GameData`), the `granted…` lookups (`GrantedTriggeredAbilitySupport`), two static filters (`TriggerMatcher`, `TriggerTargetCollector`).
- Left: the engine-made cast and activation abilities (`checkSpellCastTriggers`, whose cast bookkeeping is interleaved with the abilities it puts, so moving it reorders them), the delayed, temporary, emblem and boon zone-change abilities, `recordSacrifice`'s boons. About 250 classes still inject the service without calling it.

**R1.**
- Emblems' zone-change abilities are observer candidates (`TriggerIndex.commandZoneObjectCandidates`, CR 114.4; `TriggerCandidate.emblem`, the request `ofEmblem` with `madeByResolution`); the enters, death, put-into-your-graveyard and graveyard readers are gone (`EmblemZoneChangeTriggersTest` replaces their unit tests).
- `Watcher` (a permanent under its controller, or an emblem) is what the life, tap, draw and attacks-you matchers iterate (`EventAbilitySupport.watchers` / `abilities` / `put`); the spell matcher reads emblems by the cast watch keys (`dispatchEmblems`). Their emblem readers are gone; emblems now see every watch of those families (an emblem's life-loss, opponent-draw and "attacks you or your planeswalker" abilities, which no reader read before). The life matcher's guard against an ability's own target filter is gone (the door chooses it: Professor Dellian Fel's emblem).
- C48 rules fix: "put into a graveyard from anywhere" doesn't look back (CR 603.6c, 603.10a; `TriggerMatcher.looksBack`): its filter is judged on the card in its new zone, and an observer that left in the same event doesn't see it (Planar Void's 2008-05-01 and Bloodchief Ascension's 2009-10-01 rulings). `PlanarVoidTest` (Akroma's Vengeance) and two `CompostTest` cases (Purelace, Deathlace) fail before.
- Ledger: every line is a record gained (emblems' triggering permanent, snapshot, owner, toughness, mana value; draw records' "that player" and drawn card), random (drawn cards, discards, clashes) or a new test; no ability gained or lost in an existing test.
- Open: C49.

**R1 follow-up (found by the full card suite).**
- R1 read "doesn't look back" as "the trigger's `from` isn't exactly the battlefield", so Reyhan's "a creature you control … is put into the command zone", written on the "from anywhere" factory, stopped seeing its own move. A leaves-the-battlefield ability is one whose text names the battlefield among the zones its object leaves (CR 603.6c); only it, a sacrifice and a move to a hand or library look back (CR 603.10a). This is now `ZoneChangeTrigger.leavesTheBattlefieldAbility` / `looksBackOnAPermanentLeaving`; `TriggerMatcher.looksBack` and the dead `leavesBattlefield` are gone.
- "From anywhere other than …" is "from anywhere" with `excludedFrom` (`notFrom`), so it never names the battlefield (Voracious Brood, Syr Konrad).
- Reyhan: `Triggers.putIntoCommandZoneFrom(BATTLEFIELD, creature with counters, YOU_CONTROL)`; it read `YOU_OWN`.
- `ZoneChangeRequests` let a "put into the command zone" ability trigger only for its controller's own commander, which is Myth Unbound's "your commander" applied to every card. Myth Unbound now has that as its filter (`PermanentIsCommanderPredicate(true)`), and the matcher judges a card's filter for the ability's controller.
- Rules fix: `ReyhanLastOfTheAbzanTest`'s commander case (the suite's one failure) and a new stolen-commander case fail before.
- Ledger (3,568 zone-change classes): Reyhan's records gained; 12 records from the new route watch; the rest random (conjured, drafted and drawn cards, coin flips).

**R2 (part 1: keyword abilities).**
- Prowess, evolve and increment are `KeywordTriggers` abilities (CR 702.108a, 702.100a, 702.191a) that `TriggerIndex.permanentAbilities` and the zone-change listing add once per instance of the keyword the permanent has as the layers leave it (CR 702.108b, 702.100d, 702.191b); the cast reader and the trigger pipeline match them like any ability. The `TriggerCollectionService` prowess loop, `collectIncrementTriggers`, `putEvolveAbility`, `IncrementTriggerEffect` and its handler are gone.
- Increment's "if" is `AllOf(SourceIsCreature, AnyOf(SpellManaSpentGreaterThanSourcePower, SpellManaSpentGreaterThanSourceToughness))` around `PutCountersOnSelfEffect`, judged as it triggers and as it resolves (CR 603.4) on the entry's triggering spell; the mana spent is no longer stamped as the entry's X.
- Rules fix: increment counts its instances (`CharacteristicState.TRIGGER_INSTANCE_KEYWORDS`); `KeywordTriggersTest` (a granted second instance) fails before.
- Ledger: every line is a record gained (the cast record's triggering spell and caster, the enters record's entering permanent), increment's effect renamed, or random (route-fixture discards); no ability gained or lost.

**R2 (part 2: C45, C46, C49).**
- Every damage and becomes-the-target reader reads a permanent's abilities through `EventAbilitySupport.abilities` (`ownAbilities` / `grantedAbilities` where a reader judges an Aura's or attached permanent's own ones apart); the hand-assembled printed, temporary, persistent and static-granted reads and their face-down, cloaked and lost-ability guards are gone.
- C46: the combat damage step takes `EventAbilitySupport.snapshot` as the damage is dealt and puts what it triggered after state-based actions inside `asOf` (CR 603.2, 510.2, 704.3), so a departed dealer or watcher is read as it had them; the per-source ability maps (`selfDeals…Abilities`, `damageToCreatureAbilities`, `AllyDamageTriggerSnapshot.abilities`) are gone.
- C49: the ally combat-damage pass and "an opponent is dealt combat damage by one or more creatures you control" read the controller's emblems as watchers (filters, limits, "one or more"); both emblem readers are gone.
- Rules fixes: a combat damage source's own "deals combat damage" abilities were read from its card even after it lost them (`CombatDamageAbilitiesAsDealtTest`, Leyline Phantom); several becomes-the-target readers ignored a static "loses all abilities" (`ProfessorHojoTest`, Frogify). Both fail before.
- Ledger: damage tests unchanged but Kaito Shizuki's emblem's record gained (the dealer, "that player"), and random lines (coin flips, a random player, combat damage iterated by identity hash); target tests unchanged.

**R3 (part 1: delayed abilities with a duration).**
- `TemporaryGlobalTriggeredAbility` is listed by `TriggerIndex` (`delayedAbilities`, `delayedAbilitiesAbout`) and matched by every family but steps as a `Watcher` (`Watcher.of(delayed)`) or a zone-change `TriggerCandidate` (`TriggerCandidate.of(delayed)`: an observer, or the departing permanent's own when it is about it); its request has its maker's source card and no source permanent (CR 603.7d, 603.7e, 603.2d), `EventAbilitySupport.delayedRecord` stamping it made by a resolution with its linked card and target filter. Gone: the hand loops of the life, tap, library, spell, block, attack and damage families and of `TriggerCollectionService` (death, leaves, enters), `putTemporaryGlobal`, `TriggerMatchContext.registeringPermanentId`.
- Matchers made uniform for every watcher: "a player gains life", "a creature becomes tapped", "a creature is dealt damage", "a creature an opponent controls is dealt damage" (one per damaged creature at the combat damage step's per-creature pass; records the creature), "a creature deals combat damage to a player" (records "that much" and "that player"), "you discard" (the discard's cards as the event's cards).
- Cards as printed, "it" of a delayed ability being the event's object, not its source: Okoye, The Last Ronin (`GrantScope.TRIGGERING_PERMANENT`), Basri, The Last Ronin, Ezio (`PutCounterOnTargetPermanentEffect`), The Sea Devils (`byTriggeringPermanent`), Shriveling Rot (`TriggeringPermanentToughness`, `TRIGGERING_PERMANENT_CONTROLLER`), Consumed by History (`toTriggeringCard`); Pure Intentions' trigger is `opponentCausesYouToDiscardCard` (its reader had read `opponentDiscardsCard` as that).
- Rules fix (test fails before): Desperate Measures draws you two cards only when it dies under your control (Oracle text, 2025-04-04 rulings; `TriggerMatcher.lastKnownMatches` judges "under your control" by the controller it left with, CR 603.10a). `DesperateMeasuresTest`'s case in which the opponent drew is rewritten.
- Door: whether the event's object fits an effect's target restriction is judged for the ability's controller (Harald chapter III's "target creature an opponent controls" took the attacking Elf); it applies to every ability, so the full card suite checks it.
- Ledger: no ability gained or lost but Desperate Measures'; every line is a record gained (the family's record), the event's creature no longer the ability's source (CR 603.7d, 603.7e), "that player" by the family's relation, or random (route-fixture draws, discards, dice).
- Open: the other delayed lists (`DelayedAction` drains, the watcher and boon lists, the next-spell copies) and the step listing.

**R3 (part 2: deaths and leaves).**
- Every death and leave delayed list is a `TemporaryGlobalTriggeredAbility` (`about` / `thisTurn`): creature-death and damaged-creature watchers, Xira's egg watch, return and effect on death, Golden Guardian, the destination, sacrifice and destroy leave actions, Earthbend (CR 701.66a), the token links. `untilTriggered` is "no stated duration": the door ends it as it triggers (CR 603.7b), cleanup prunes one whose permanent left. Gone: 13 `TriggerCollectionService` drains and their call sites, 8 `GameData` lists, `DelayedZoneChangeTrigger`, the sacrifice and destroy `DelayedAction`s, `RegisterDelayedCreatureDeathTriggerEffect` (cards write `Triggers.creatureDies`), the dead `GiveControllerPoisonCountersOnTargetDeathThisTurnEffect`.
- A delayed entry names its maker's source permanent (`DelayedRecord`, `entrySourcePermanentId`, CR 603.7e); one about a departing permanent records the card it became and its last controller. Untap and control-loss families list delayed abilities about the permanent. `PermanentDealtDamageThisTurnBySourcePredicate` judges "dealt damage this way / by that creature".
- Rules fix (tests fail before): Predatory Sludge's graveyard ability is its own (CR 113.6, 603.3a): it no longer conjures after Sludge left, and Sludge's controller conjures. Its two tests are rewritten.
- Cards as printed: Searing Blood, Virulent Wound ("that creature's controller" = `TRIGGERING_PERMANENT_CONTROLLER`), Burn Away (`DYING_CREATURE_CONTROLLER` reads it), Fight for the Throne (`ifCondition`, CR 603.4); Coffin Queen's ability also ends on it leaving (2014-02-01 ruling).
- Ledger: no ability gained or lost but Sludge's; every line is a record gained (source permanent, triggering card, permanent and controller), an effect renamed (Phantasmal Mount, the death registrations), or random (route-fixture draws, reveals, clashes).
- Open: Lagrella's `DelayedExileReturnCounterTrigger` ("when an exiled card enters under your control this way" needs the return to name its event), the step `DelayedAction` drains, the entering-creature watchers and boons, the next-spell copies, the step listing.

**R3 (part 3: cast and activation copies).**
- "When you next cast / activate … this turn" is a `TemporaryGlobalTriggeredAbility.nextThisTurn` (`next`: triggers once within its duration, CR 603.7b; the door removes it as it triggers, cleanup with the turn), made from each `CopyNext…ThisTurnEffect`'s / `WhenYouNextCastCreatureSpellThisTurnEffect`'s `delayedAbility()` by its normal or mana-ability handler. The activation family lists delayed abilities (`SpellTriggers.collectDelayedActivationTriggers`). Gone: the instant-or-sorcery, next-spell, filtered, X-ability, loyalty and exhaust copy queues and their drains, `PendingSpellCopy`, `PendingCopySource`, the link and linked-X bookkeeping, `DelayedSpellCastTrigger` and Yuna's queue, `GrantInstantSorceryCopyUntilEndOfTurnEffect` and `spellCopyGrantsUntilEndOfTurn`, `CopyNextActivatedAbilityWithXThisTurnEffect` (now `CopyNextSpellOrAbilityWithXThisTurnEffect`), ten `EndOfTurnStateField`s.
- Cards as printed: Repeated Reverberation and Magus Lucea Kane are one ability with a cast and an activation trigger event (`CopyTriggeringSpellOrAbilityEffect` gained `withCopies`, `permanentSpellCopiesAreTokens`); Sea Gate Stormcaller is `NotKicked` / `Kicked` with `Fixed(2)` copies; The Mirari Conjecture III is `RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect` + `CopyTriggeringSpellEffect`.
- Rules fixes (tests fail before): a delayed copy's source is its maker, not the copied spell (CR 603.7d, 603.7e: Magus Lucea Kane, Jeong Jeong, The Clone Saga, Gadwick's First Duel, The Mirari Conjecture; `MagusLuceaKaneTest`); Repeated Reverberation's loyalty copy is one ability making two copies, not two abilities (`RepeatedReverberationTest`); Karn Liberated's restart ends every delayed ability of the ended game (CR 728.1; it had cleared only the exhaust copies; `KarnLiberatedTest`).
- Ledger: no ability gained or lost but Repeated Reverberation's; every other line is an effect renamed (`CopyControllerCastSpellEffect` / `CopyControllerActivatedAbilityEffect` → the triggering-spell copies), the cast or activation record gained, the maker as source (above, and Yuna's source permanent), Sea Gate's unmet branch as `DrawCardEffect(0)`, The Mirari Conjecture's tests now resolving chapter III, or random (route-fixture draws, discards, clashes).
- Open: Leori's chosen-type copies (`chosenSubtypeActivationCopiesThisTurn`: the chosen type must be bound into the ability as it is made), `DelayedControllerSpellCastTrigger` (40 cards; it skips a registration whose source left, which a delayed ability doesn't), Lagrella, the step `DelayedAction` drains, the entering-creature watchers and boons, the step listing.

**R3 (part 4: the rest of the cast family).**
- Every delayed "whenever / when you next cast" registration is a `TemporaryGlobalTriggeredAbility` from the registration's `delayedAbility()` and `Duration` (`thisTurn`, `nextThisTurn`, `untilYourNextTurn`, `boon`: a one-time boon, `TemporaryGlobalTriggeredAbility.boon`, counted by `ControllerHasBoon`), matched by the cast family. Gone: `DelayedControllerSpellCastTrigger`, its drain and snapshot bookkeeping, `sourceMustRemainOnBattlefield` (a delayed ability doesn't skip a departed source; it just can't affect it, CR 603.7c), the boolean constructors (43 cards rewritten by factory).
- Leori is one ability, `CopyAbilitiesOfChosenPlaneswalkerTypeThisTurnEffect`: the type is chosen as it resolves (`SpellPlaneswalkerTypeChoice`) and bound into the delayed ability's source filter. Gone: `chosenSubtypeActivationCopiesThisTurn`, `StackEntryHasSourceChosenSubtypePredicate`, the until-end-of-turn subtype choice (`ChooseSubtypeForSourceEffect.untilEndOfTurn`, `temporaryChosenSubtypePermanentIds`) and Runed Terror's case for it.
- A delayed ability's source-relative stack-entry filter reads its maker's source, or its departure snapshot (Loki's power, CR 113.7a).
- Rules fixes (tests fail before): Solar Array and Chocobo Camp's "when you next cast ... this turn" ended with the turn, not when consumed (Oracle text, CR 603.7b); Mountain Titan's delayed ability triggers after it left, putting its counter nowhere (CR 603.7c); Leori chooses a type and copies even if it left before its ability resolved (Oracle text, CR 113.7a).
- Ledger: no ability gained or lost but Mountain Titan's and Leori's (effects renamed); every other line is the cast record gained (that player, triggering player, card snapshot, cast indices, spell targets), the registration's source snapshot and the target route's `activePlayerId` lost, or random (route-fixture draws, discards, dice, clashes; combat-damage fixture order).
- Open: the step `DelayedAction` drains and the step listing, Lagrella's `DelayedExileReturnCounterTrigger`, the entering-creature watchers (Beck's `creatureEntersDrawSourcesThisTurn`, `untapAttackedCreaturesEachCombatThisTurnSources`).

**R3 (part 5: step-timed delayed actions and the rest).**
- Every step-timed `DelayedAction` is a `TemporaryGlobalTriggeredAbility` (`atNextStep`, `atNextStepUnlessPaid`, `atEveryStep` with `repeats`/`lastsWhile` for "for the rest of the game" and "for as long as", CR 603.7b), listed by `TriggerIndex.stepCandidates` before the step's other abilities (the moment the drains ran) and matched by `TriggerMatcher.delayedStepMatch`. `DelayedReferents` names its other objects and the turn, extra turn or combat phase it waits for (CR 500.8); `Triggers.cleanup()` (CR 514.3a). The attack, block, combat-damage and damage "this turn" lists, Beck's and Full Throttle's are delayed abilities their families match. Gone: about 95 records and their drains, about 25 single-purpose registration effects, epic's and Lagrella's drains; `DelayedPermanentAction` and `PendingExileReturn` are payloads of their resolve effects.
- A delayed step ability triggers even if its object left, and doesn't affect it (CR 603.7c); a one-shot whose intervening if is false as its step begins is removed (CR 603.7b, 603.4).
- Rules fixes (tests fail before): Shirei's "if Shirei is still on the battlefield" (CR 603.4), Perilous Iteration's "your next turn's end step", Biolume Egg's return controlled by its controller (CR 603.7e), Jaya keeps damaging the chosen permanent that stopped being a creature (CR 603.7c), a permanent's "whenever a creature blocks" triggers once for a blocker an effect makes block more attackers (CR 509.3a; Noble Ox with Heat of Battle).
- Rules fixes without such a test: Norritt's and Nettling Imp's intervening ifs; a delayed sacrifice is made by the ability's controller (CR 701.21a); "at end of combat" abilities survive "end the turn"; Moraug's and World at War's "that combat"; Cyclopean Tomb triggers each upkeep; Terminal Velocity and Strago and Relm grant a lasting end-step ability; Lagrella triggers once (CR 603.7b, rulings 2022-04-29); Arcbond's creature deals the damage; Esper Terra's chapter makes the delayed sacrifice (no registration trigger on the token).
- Ledger: abilities gained are delayed abilities whose object left (CR 603.7c) and new tests; lost are Shirei's (fix), per-permanent sacrifices now one ability per resolution (CR 603.7a: Dalkovan Encampment, Fire Navy Trebuchet) and Terra's registration trigger; the rest is effects renamed or wrapped in the intervening if, the maker as source (CR 603.7e), the event's objects recorded, or random (Drover of the Swine, Jadelight Spelunker, Mirror March, route fixtures).
- Open: C50, C51, C52. `lastsWhile` is judged only as a step begins. The new `PermanentRemovalServiceTest$ResolveDelayedPermanentAction` cases fail on the class's HEAD mock wiring (`layerSystemServiceProvider` is null), as its other 42 cases do.

**R3 follow-up (C50, C52).**
- Echo is `KeywordTriggers.echo` (CR 702.30a), listed by `TriggerIndex.permanentAbilities` with a card's printed abilities for its static `EchoEffect` (was `RegisterEchoAtNextUpkeepEffect`); its intervening if `CameUnderControlSinceLastUpkeep` reads `Permanent.controllerUpkeepsSinceControlled` (entering and control changes restart it; the controller's upkeep counts it, phased out or not). Gone: `EchoAtNextUpkeep`, its drain, the entry and control-change registrations, the handler.
- Its entry is the step pipeline's ("<card>'s upkeep ability", not "echo ability"); `ForcedCostOrElseEffectHandler` sees the echo payment through the "if"'s `ConditionalEffect`, so a paid echo's "when ... is paid" stays its own ability (Shah of Naar Isle).
- Siren's Call and Maddening Imp are `MatchingCreaturesMustAttackThisTurnEffect(PermanentControlledByActivePlayerPredicate …)` (CR 611.2c) + `RegisterDelayedEndStepTriggerEffect.aboutActivePlayer(DestroyAllPermanentsEffect(…, TARGET_PLAYER, null))`. Gone: `SirensCallEffect`, its handler, `DestroyNonAttackersAtEndStep`, its drain and `AttackLegalityService`'s read of it.
- Rules fix (test fails before): echo triggers for a permanent that entered while it had lost its abilities, if it has echo as its controller's upkeep begins (`KeywordTriggersTest`; the registration had skipped it). A permanent put straight onto the battlefield now has echo too (Thran Weaponry's tests).
- Ledger: no ability gained or lost but Thran Weaponry's echo (above); echo lines gain the step record (active player, source snapshot) and are wrapped in their "if" (CR 603.4); Siren's Call's and the Imp's gain the active player, the Imp's its maker's source permanent (CR 603.7e).
- Open: C51.

**R4.**
- `TriggerCollectionService` is gone. `SpellEvents.cast` / `.activated` (pipeline, beside `ActionEvents`) are the cast's and activation's completion (CR 601.2i, 602.2b): they record the cast, take what its payment leaves into the event (`SpellEvent.Cast`: conspire / casualty costs paid, copying mana, first spell of the game; `Activated.manaLinkedCopies`), emit it, then clear the payment records. `SpellTriggers` puts every cast ability: boons and opening-hand reveals first, the copies and granted replicate / demonstrate last, as before.
- Cards as printed: Maelstrom Nexus, The First Sliver, Rain of Riches, Abaddon, Prismari, Quandrix, Wildsear and The Twelfth Doctor write `youCastSpell()….grantedToTheSpell()` (CR 113.7, 702.85a, 702.40a, 702.144a). `nthThisTurn` counts honour `notCastFrom` (by subtraction: a card cast twice is two casts) and `paidWithTreasureMana`; new `StackEntryManaValueAtMostAmountPredicate`. Gone: `GrantCastTriggerToSpellsEffect`, `SpellCastTriggerGrant`.
- "The next … spell you cast this turn has cascade / storm" (Sloppity Bilepiper, Storm, Force of Nature, Crackling Spellslinger) is a `nextThisTurn` delayed ability with a `grantedToTheSpell()` trigger, used up by the spell it applies to (CR 603.7b). Gone: `nextCreatureSpellCascadeThisTurn`, `pendingNextInstantSorceryStormThisTurnCount`, their `EndOfTurnStateField`s.
- Moved to their families: enter boons, First Day of Class's watchers, Cosima's voyage → `EtbTriggerService`; creature-cast boons → `SpellTriggers`; sacrifice records → `ActionEvents.sacrificed`, its boons matched by `ActionTriggers` on `ActionEvent.Sacrificed` (one per simultaneous sacrifice); Judgment of Alexander → `DamagePreventionService`; the enchanted-departure record → `PermanentRemovalService`.
- Rules fix (test fails before): a First Sliver that lost its abilities (Frogify) gives no cascade (CR 613.1f; `TheFirstSliverTest`). Now uniform, no test failing before: every grant reads abilities as the layers leave them; Sunken Palace's mana copies any activation it is spent on, hand and graveyard ones too (CR 603.7a).
- Ledger (2,206 card classes and the route tests): no ability gained, lost or reordered; every line is the granted cascade / storm / demonstrate gaining the cast record ("that player", event value, triggering card), or random (Breeches' coin flip, route-fixture draws, discards, dice, clashes, combat damage order).
- Gone too: Path of Ancestry's dead restrictions (`PATH_OF_ANCESTRY`, `COMMANDER_COLOR_IDENTITY_WITH_CREATURE_TYPE_SCRY`), their mana tag, pending choices and `PathOfAncestryManaColorChoice` (the card uses `paidWithManaFromThis`).
- Open: boons and the watcher lists aren't delayed abilities yet (D or later).

**F (part 4: legacy types, docs).**
- Gone: `EffectSlot`, `LegacySlotTrigger`, `LegacySlots` (its kind-neutral readers are `AbilityEffects`), `LegacySlotTriggers`, `Card.addEffect` / `addSimultaneousEffects` / `getEffects(slot)` / `getEffectRegistrations(slot)` / `getUnslottedTriggeredAbilities`, every slot-keyed grant constructor and `Permanent` / `GameData` / `StackEntry` / `SpellTarget` helper, `TriggerMatchContext` (now `SpellTriggers.SpellMatch`), `ChooseModeChoice.triggerTime`, `BecomeCopyOfTargetCreatureEffect.retainedEffectSlot`, `scripts/step*`, `scripts/effect-slot-*`.
- Shadow mode is gone (`ShadowTriggerRecorder`, `ShadowComparison`, `RulesObserver.shadowedSlots` / `wouldTrigger`, `GameData.CollectingTrigger`). `RulesEventRecorder` (`harness.getRulesEventRecorder()`) records drained rules event batches and hands stack entries to `TriggerLedger`.
- No behaviour change and no ledger pass: every removed branch was unreachable, `EffectSlot` having no constant. `GrantEffectToSourceUntilNextUpkeepEffect` (no card uses it) granted a null ability through the slot path; it now grants its ability.
- Docs: `triggers.md` rewritten as the model; `TRIGGER_SLOT_TARGETING.md` replaced by `TRIGGERED_ABILITY_TARGETS.md`; refactor references dropped from `agent-docs/`, both card skills (`.claude`, `.codex`) and Javadoc.
- Open: ~18 `ORACLE_TEXT_EFFECT_MAP.md` rows still use the removed `SpellCastTriggerEffect(...)` shorthand (the file's note says how to write it); dated history in the other plan docs still names `TriggerCollectionService`.

**D (part 1: `Duration`, floating effects in the ledger).**
- `Duration` (`model.effect`) replaces `EffectDuration`: an `End` event (`NEVER` for none, CR 611.2a) or an `AsLongAs` condition that ends it for good (CR 611.2b), never both. Its constants are the old names; `CONTINUOUS` is `STATIC` (an effect of a static ability, CR 611.3). Compare with `equals`; `ControlDuration.toDuration()`.
- No start yet: "after this turn" (CR 702.185a) comes with the play-permission family, the only one that needs it.
- Ledger: `RulesObserver.floatingEffectBegan` / `floatingEffectEnded`; `TriggerLedger` writes `floating began|ended` lines (source, effect class, duration, affected objects by role, turn and step). Only `GameData` changes `floatingEffects`: `putFloatingEffect` (keeps the effect's timestamp, CR 613.7a), `endFloatingEffect`, `replaceFloatingEffect` replace every direct `add` / `remove` / `removeIf` / `set` / `clear`.
- No behaviour change. Ledger over the 973 card test classes of the touched effects and the route tests: no ability gained, lost or reordered; every difference is random (drawn and discarded cards, Ebony Fly's die, Setzer's coin flips). One log text changed: a `WHILE_SOURCE_CONTROLLED` "loses all abilities" is logged "for as long as", not "until end of turn".
- Inventory: `python3 -I scripts/duration-inventory.py [--list FILE]` (counts per family; `--list` classifies each `GameData` field as an effect or turn history by name, so check its list before converting a family).
- Next: part 2, the clock. Each `End` and `AsLongAs` is ended by its rules event in `RulesEventPipeline` (step events, the cleanup step included; the turn's start; `SpellEvent`; zone changes; tap; attachment; control change), deleting the `expire…` methods; take the floating lines before and after and keep each moment or cite the rule that moves it. Then the `GameData` families in the inventory's grouping, `Permanent`'s end-of-turn fields, the other duration enums (`GrantDuration`, `ControlDuration`, `ExilePlayDuration`, `NoMaximumHandSizeDuration`, the delayed spell-cast `Duration`) and `DelayedAction` (C51).

**D (part 2: the clock).**
- `DurationClock` (`service/duration`) ends every floating effect whose `Duration.End` is an event: `RulesEventPipeline.process` hands it each batch before matching the batch's abilities, so an ability the same event triggers sees the effect ended. It undoes what the effect kept on its permanent (copy reverts, tap restriction, mirrored until-next-turn keywords), phased out or not.
- New events: `TurnEvent.Began` (turn start), `TurnEvent.StepEnded` (every step's end, CR 500.5; `END_OF_COMBAT` also when an effect ends combat), `TurnEvent.Cleanup` (CR 514.2), `ActionEvent.Planeswalked` (CR 701.31); the clock also reads `StepEvent` (upkeep, end step), `TapEvent.UntappedDuringUntapStep` and `SpellEvent.Cast`. Each is emitted with `occur` where the old expiry ran. Tests end a duration with `gd.rulesEvents.occur(...)`.
- Gone: the ten event-timed `expire…` methods of `GameData`, `TurnCleanupService.expireControlAtEndOfNextTurn`, `StepTriggerService.expireNextEndStepTemporaryCopies`, the turn-start revert loop and `ExpireControlAtEndOfNextTurn` (control effects "until the end of your next turn" end at the clock like the others; C51 less one).
- Moments: the upkeep's "until your next upkeep" now ends at the upkeep's `StepEvent`, the end step's "until the next end step" at its `StepEvent`, and "until the end of your next upkeep" as the step ends, before the mana empties (CR 500.5); all within the same step's turn-based actions, before its abilities trigger.
- Ledger (3,099 card test classes of time-bound effects and the route tests): every floating effect begins and ends in the same turn and step; no ability gained, lost or reordered; the rest is random (Umaro's modes, coin flips, dice, draws, Forth Eorlingas's opponent order). The ledger shows turn and step, not the order within a step. Gradle skips an up-to-date test task, so a ledger run must change its inputs or use `--rerun`.
- Next: part 3, the "for as long as" durations (`AsLongAs`: the departed, unattached and untapped source, control change; the GameData card-left-exile end of a teach grant), ended where the state changes; then the families.

**D (part 3: "for as long as").**
- `DurationClock` ends the `AsLongAs` durations where the state changes, not at a batch: `sourceLeft` (`PermanentRemovalService`, the orphaned-Aura sweeps), `sourceUntapped` (`CreatureControlService.onSourceUntapped`), `sourceUnattached` (every unattach or re-attach site) and `sourceChangedControl` (the control move). `Duration.AsLongAs.needsSourceOnBattlefield` / `needsSourceTapped`; which effects need their creator to control the source is the clock's one list (was two: the control move's and the reconciliation's). `holds` judges the conditions on the board for the reconciliation's safety net.
- Every end undoes what it kept, by event or by state: `end` runs `AuraCopyService.revertExpiredCopies` (a tapped-target copy is left to it, which keeps a newer copy). Gone: `GameData.expireFloatingEffectsForDepartedSource` / `…ForUnattachedSource` / `expireTappedSourceFloatingEffects`, `AuraCopyService` and `EquipSupport.expireAttachedCopyEffects`, `CreatureControlService.expireSourceControllerDependentEffects` and the move's untap-lock case.
- Teach's grant is `WHILE_SOURCE_CARD_EXILED` (`AsLongAs.SOURCE_CARD_EXILED`), ended by `GameData.removeFromExile`; no `TeachCastCopyEffect` check. Dungeon Geists' "for as long as you control" untap lock is `WHILE_SOURCE_CONTROLLED`.
- Rules fix (test fails before): an Equipment or Aura moved or unattached by an effect ends its "for as long as it remains attached" copy (Blade of Shared Souls, Grip of Phyresis; `BladeOfSharedSoulsTest`). About 15 sites expired the effect before the copy revert looked for it, and the Aura paths didn't revert at all.
- Ledger (1,192 card test classes of source-linked, attachment and teach effects and the route tests): every ability and floating effect begins and ends in the same turn and step; differences are the two renamed durations, the new test and random draws or drafts (Adaptive Armorer, Arms Scavenger, Masterpiece Vault, Diviner's Wand, route fixtures).
- Open: a "for as long as" effect that tracks a permanent ends when it phases out (CR 702.26f); `sourceLeft` isn't told then. Raw `Permanent.untap()` calls (untap costs, a few choices) don't tell the clock.
- Next: the families (inventory grouping), `Permanent`'s end-of-turn fields, `GrantDuration` / `ControlDuration` / `ExilePlayDuration` / `NoMaximumHandSizeDuration`, and `DelayedAction` (C51).

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
