# Effect cleanup: one verb, any subject

A step-by-step plan to stop writing a new effect class for every combination of *what happens* and *who it happens to*. An
effect names its verb (boost, destroy, draw, lose life) and takes a **subject** (this permanent, the target, the enchanted
creature, each opponent, that permanent's controller, the cards exiled this way) as an ordinary component. One engine service
resolves every subject. Effects that fuse two instructions become steps that talk through the resolution's step results.

This file was cut down on 2026-11-23 and again on 2026-11-25. The full text is in git history, to be grepped for a run, class or
card name, not read whole: `git show ef0278a44e:effect-cleanup-refactor.md` (every run's Findings up to step 10 run U3b', with the
standing knowledge before the second cut) and `git show 6728593a9a:effect-cleanup-refactor.md` (before 2026-11-23, steps 1 to 9 run
40).

**How to read it.** Read from the top down to `## Findings`, then the Findings sections your run follows on (step 10's runs, defect
runs D24 and D25). Everything a run needs is above `## Findings`. The fold and defect queues are in `effect-cleanup-queues.md`: read
their introductions and the rows of your run, not every row. To start a step, use the prompt at the bottom.

## Status

| Step | Title | Status |
|---|---|---|
| 1 | Inventory script, audit ledger and count ratchet | done |
| 2 | Delete the effects nothing uses | done |
| 3 | Subject types and the one resolver | done |
| 4 | Player verbs take a `PlayerSubject` (repeatable) | done (4a–4j) |
| 5 | Permanent verbs take a `PermanentSubject` (repeatable) | done (5a–5m) |
| 6 | `GrantScope` becomes a `PermanentSubject`, static abilities included | done (6.1–6.4; `GrantScope` and `TapUntapScope` deleted) |
| 7 | Step results: one way for a step to tell the next what happened | done |
| 8 | Split the fused effects (repeatable) | done (runs 1 to 29; "counter unless" went to step 10's "unless" runs, owner's decision 2026-11-25) |
| 9 | Sweep the single-card tail (repeatable) | open, nearly done. Every rare record has a ledger verdict; the fold queue is closed (fold run 50c; no `step-9` row left); defect runs 1, 2 and D10 to D26 are done. Left: the defect queue's open lines in `effect-cleanup-queues.md` (218 not struck through, each with what it needs) and the `blocked-*` rows (Blocked causes below). Waiting on the owner: 2 rows (gift opponent, deferred), Tribute's reduced-placement reading (CR 702.104b), "if you do" after a replaced draw (D14 L17), Nowhere to Run's any-player hexproof option (D16 L12), set exchanges under CR 701.12a (D19 L22), Reality Twist on unlisted types and Boseiju's rider under a land-mana replacement (D21), and a seeded game random source. |
| 10 | Collapse the head: one record per verb (repeatable, needs sign-off) | open. Done: runs Counters (`PutCountersEffect`), Remove counters (`RemoveCountersEffect`), Damage (`DealDamageEffect`, after defect run D24), U1, U2, U3a, U3b', U3c, U3b1, U3b2 and U4a to U4d (`UnlessPaysEffect`, with D25's `ResolutionCostPaymentService`; `MayPayManaEffect`, `MayAbilityHandlerService`'s generic mana branch and `ForcedCostOrElseEffect` with both its handlers are gone: U4a and U4b moved its 159 card files and four engine builders, U4c echo, U4d cumulative upkeep; U5 the damage ransoms, five records and Worms of the Earth's; U6 the for-each loop `UnlessPaysEffect.forEach` with Cut the Tethers, Cleansing and Giant Albatross; U6b `forEachCard` (Sirocco, Sword-Point Diplomacy) and the discard paid together (Ob Nixilis, Professor Onyx); U6c the same on The Long Reach of Night and Possessed Portal; U7a the non-mana `XOrY…Cost` records as a cast-time `AnyOfCost`, U7b the five mana ones with `PayManaCost` paid in the total cost, U7c `ForageCost`; Boost B1: `BoostEffect`, renamed from `BoostTargetCreatureEffect`, absorbed `BoostSelfEffect` and `BoostTargetPermanentEffect`, 2,542 card files; Boost B2: the two mass records as `creaturesYouControl` / `allCreatures` / `creaturesTargetPlayerControls`, 609 card files; Damage P: `DealDamageEffect` took the player recipients, `DealDamageToPlayersEffect` and Typhoon's record absorbed, 704 card files; Damage M1: the four set records as `toEach(set)` / `toEachTarget`, 112 card files; Damage M2: `MassDamageEffect` as `toEachCreature` / `toEachCreatureAndPlaneswalker` / `eachEvaluatedFor(amount, set)` with `.andTo(players)`, 241 card files; Damage R: the other-dealer records as the rider `.dealtBy(dealers)` with `bite`, `targetCreatureToItself`, `toEachOther`, 138 card files; run A: the four graveyard card movers into `PutCardsIntoHandEffect` / `ExileGraveyardCardsEffect.cards` over `sourceCard()` / `triggeringCards()`, 122 card files; run X: the excess redirect as `DealDamageEffect`'s `excessToControllerInstead()` / `bite().excessToControllerInsteadIfDealerHasTrample()`, 5 card files, the two records gone; run S: `CantAttackUnlessEffect` / `CantBlockUnlessEffect` into `CantAttackOrBlockUnlessEffect.attack` / `block` / `attackOrBlock`, the Arrest record into `EnchantedPermanentCantActivateAbilitiesEffect.includingManaAbilities()`, the controller-scoped attack lock into `CreaturesCantAttackUnlessPredicateEffect.youControl`, 145 card files; run G: the two goad-set records into `GoadTargetCreatureUntilNextTurnEffect.eachCreature` / `creaturesControlledBy` / `creaturesTargetPlayerControls` with a player-target spec, 10 card files). Next: the rest-of-step-10 runs in the order of the orchestrator's decisions 2026-11-26 (D, P / O / U, C1, C2, K / K2, then R2, J, T, U8) |
| 11 | Close: docs, leftovers, final ratchet | open |

## Why

The engine had one effect record per *sentence shape*, not per *action*: `BoostSelfEffect`, `BoostTargetCreatureEffect`,
`BoostAllOwnCreaturesEffect` and `BoostEquippedCreatureUntilEndOfTurnEffect` were one action with four subjects and four handlers,
and about 50 private "who" enums (`LoseLifeRecipient`, `DamageRecipient`, `GrantScope`, `TapUntapScope`, `PermanentReference`, ...)
were each resolved by their own `switch`. The copies drifted and cost rules accuracy ("enchanted creature" was looked up three ways,
"its controller" two ways, "this permanent" had two meanings), and every new card needed a new class.

The second half was fused effects: an effect like `DiscardCardAndUntapSelfEffect` existed because the untap had to wait for a
discard prompt, so the pair was one record with a bespoke continuation. Subjects and step results make both expressible from
shared parts; the single-card tail (steps 8 and 9) is where most of the code goes away.

## Measurements (2026-11-23, at `51f0afd546`)

Re-measure with `python3 -I scripts/effect-inventory.py` (no arguments: the summary below; `--family`, `--siblings`, `--dead`,
`--unaudited [--family P]`, `--tests-for Class... [--max-per-class N]`).

| What | Count |
|---|---|
| `EffectCountRatchetTest` baselines (records / handler classes in `normalfx`, `mayfx`, `staticfx`) | 2,881 / 2,265 (after step 10 run G) |
| Types in `model/effect/` | 4,338 (4,044 records, 43 enums, 248 interfaces, 3 classes) |
| Handler files in `service/effect/normalfx` | 3,012 files, 264,494 lines |
| Records used by exactly one card / two cards | 2,353 / 404 |
| Dead records (named only by their own handler) | 7 |
| Subject-sibling groups | 116 groups, 264 classes, 148 removable |
| Records with a nested `CardEffect` (or a `List<CardEffect>`) | 136 (36) |
| Names containing `Then` / `And` | 207 / 434 |
| Handlers that `switch` on a recipient / scope / reference component | 20 |
| Classes with no ledger row (`--unaudited`) | 656 (each used by three or more cards) |
| **Rare records with no ledger row** (records used by at most two cards, zero included) | **0** (1,767 before the triage) |

Count the rare ones with `python3 -I scripts/effect-inventory.py --unaudited | awk 'NR>1 && $3<=2 && $2=="record"' | wc -l`.
No rare record is left; the triage gave each a row, and the fold runs took every `step-9` row.

`scripts/effect-audit-ledger.csv` (header `class,verdict,step,note`): 6,775 rows over 4,581 classes (after fold run D23). Latest verdict per class: unique 1,195, absorbed 1,362, split 691, blocked-other 560, survivor 460, blocked-step-model 140, blocked-player-loop 95, dead 78. Waiting: 0 `step-9` rows; 2 rows on the owner's deferred gift opponent; the "unless" rows on step 10's runs U3c to U5.

## Design

### Words used below

- **Verb**: the action an effect performs. **Subject**: who or what it is performed on.
- **Survivor**: the record a family folds into, kept under its current name. **Absorbed**: a record (and its handler) deleted
  because the survivor now expresses it.
- **Head**: effects used by many cards. **Tail** / **rare**: effects used by at most two cards.

### Subjects and the one resolver

Three sealed interfaces of small records in `model/effect`, built through one factory class, `Subjects`; they compose
(`controllerOf(triggering())`). `agent-docs/SUBJECTS_REFERENCE.md` has one line per variant: the wording it stands for, whether it
targets, whether a static ability may use it.

- `PermanentSubject`: `source()`, `target([group])`, `attached()`, `triggering()`, `controlledBy(players, filter)` and its named
  sets, `allOf(...)`, `except(...)`, `otherPermanentsSharing(...)`, `searched()`, `targetSpell()`, and the step-result variants
  (`thisWay(kind)`, `createdThisResolution()`, `returnedThisResolution()`, `exiledThisResolution()`).
- `PlayerSubject`: `you()`, `targetPlayer([group])`, `targetPlayerDeclaredElsewhere()`, `eachPlayer()`, `eachOpponent()`,
  `opponentsOf(...)`, `controllerOf(p)`, `ownerOf(p)`, the event players (`triggeringPlayer()`, `activePlayer()`, `defendingPlayer()`,
  `enchantedPlayer()`, `chosenPlayer()`), `playersThisWay(kind)`, `playersDealtDamageThisResolution()`.
- `CardSubject`: cards outside the battlefield. `cardsThisWay(kind[, filter])`, `targetCard(filter, scope)`,
  `triggeringCards(filter)`, `sourceCard()`, `exiledWithSource()`, `exiledCards(...)`, `cardsInYourHand(filter)` and the other zone
  sets, `revealedThisWay()`. It names a card only in the zone stay it is expected in: a card that moved is a new object (CR 400.7);
  the rest of the effect can find a card it moved to a public zone (CR 400.7j).

`SubjectResolutionService` (engine, `service/effect/`) is the only code that turns a subject into permanents, players or cards. Its
entry points: a resolving effect `(GameData, StackEntry, CardEffect, subject)`; a static ability `(GameData, source Permanent,
subject)`, which rejects a subject with `needsStackEntry()`. Its rules:

1. **Only a target variant declares a target.** An effect derives `targetSpec()` from its subject, so declaration and resolution
   cannot disagree. Targets are chosen as the object goes on the stack (CR 115.1). As it resolves, an illegal target is not affected
   and a part that needs information about it does nothing (CR 608.2b). A subject that is not a target never makes anything fizzle.
2. **A reference reads current information, else last-known information** (CR 608.2h). "Its controller" of a creature that died is
   the controller it last had.
3. **Nothing is done to an object that left** (CR 608.2h, CR 609.3); a permanent that left and came back is a new object (CR 400.7).
4. **A set is determined once.** A one-shot effect finds its set when applied (CR 608.2h) and a continuous effect made by a
   resolution keeps it (CR 611.2c); a static ability's set is re-evaluated by the layered pass (`agent-docs/LAYER_SYSTEM.md`).
5. **Several players act in APNAP order**: choices in APNAP order, then the action at once (CR 101.4, CR 608.2e). A verb whose handler
   can't choose-then-act for several players does not accept `eachPlayer()` / `eachOpponent()`.

`source()` is strict: it never falls back to the entry's target.

### Step results

Every step of a `StepKind` records its `StepOutcome(amount, objectIds, playerIds)` on the entry's `ResolutionResults`
(`StackEntry.recordStep(kind, outcome)`; `addToStep` for results that arrive in parts, such as damage or answered prompts), and
records `StepOutcome.NONE` when nothing happened, so a later step never reads an older step's result. One outcome per kind: the
latest step's. Kinds: DRAW, DISCARD, MILL, EXILE, DESTROY, SACRIFICE, RETURN, CREATE_TOKEN, DEAL_DAMAGE, GAIN_LIFE, LOSE_LIFE,
PUT_COUNTERS, REMOVE_COUNTERS, COUNTER_SPELL, COPY_SPELL, MANIFEST, TRANSFORM, PUT_INTO_GRAVEYARD, SEEK, CHOOSE, REVEAL, PAY_MANA,
SHUFFLE_INTO_LIBRARY, PUT_INTO_LIBRARY, GAIN_CONTROL, NOTE (`StepKind`'s Javadoc says what each records and which handlers record
it). Removal steps also record `DepartureResult`s (last-known power, toughness, types) and `StepOutcome.movedCards` /
`lastKnownCards`.

Readers, and nothing else: the `Condition` `HappenedThisWay(kind)` ("if you do", "if a [kind] was … this way": `toA`, `toACard`,
`atLeast`, `drew()`, `sacrificed()`, `exiled()`, `discarded()`, `transformed()`, `removedCounters()`, ...); the `DynamicAmount`
`AmountThisWay(kind)` ("that many", "its power": `died()`, `matching(...)`, `controlledByYou()`, `destroyedManaValue()`,
`exiledCards()`, `noted()`, `keepingNegative()`, ...); the subjects `thisWay(kind[, filter])`, `playersThisWay(kind)` and
`cardsThisWay(kind[, filter])`. Reading follows CR 608.2c (instructions in written order) and CR 608.2h. A reflexive or delayed
ability made by a resolution carries that resolution's results (`StackEntry.readStepResults`).

**Which handlers may be an earlier step** (rule in `agent-docs/architecture/resolution.md`): one that never prompts, one that pauses
by resolving itself again before it changes anything, or one that pauses at the end of its step with its result recorded. A handler
that pauses mid-step by `rerunCurrentEffectAfterInteraction` or a bespoke continuation is not: its second half would be dropped or run
twice, so a fused record whose first half is such a handler is `blocked-step-model`.

### Durations

A lasting change made by a resolution is a floating effect with a `Duration` (`model/effect/Duration`: a start, an end event or an
"as long as" condition; its named constants are the durations cards write), ended by `DurationClock`; a delayed
triggered ability is a `TemporaryGlobalTriggeredAbility`. Never a `Permanent` or `GameData` field with a cleanup reset
(`ARCHITECTURE.md`). Records that differed only in duration fold onto one record with a `Duration` component
(`RegisterGlobalTriggeredAbilityEffect`, `GrantStaticEffectToPlayerEffect`, `PlayerRestrictionEffect`,
`ExileTopCardMayPlayThisTurnEffect.until(...)`).

### Splitting a fused effect

A fused effect becomes `SequenceEffect.of(first, second)` (or flat `addEffect` calls on a plain spell), the second step reading the
first's result. Rules:

- "If you do" is gated on the first step having happened, read from its result: a `ConditionalEffect(HappenedThisWay(...), ...)`
  step (`SequenceEffect` has no gate of its own). "When you do" is reflexive (CR 603.12): check the Oracle text.
- "X and Y" in one sentence is often one event (one damage event, deaths together): `SequenceEffect.simultaneous` or the survivor's
  batch form, not two sequential steps.
- One target shared by the steps stays one entry target; with two targets, each step names its target group. The cast-time target
  prompt must not change.
- A trigger slot holds one effect, so a split on a trigger slot stays one `SequenceEffect`.
- The first half must be an earlier step (above); otherwise the record is `blocked-step-model`.

### Survivors keep their names

A family folds into its most used record, which gains a component and keeps its existing constructors as overloads,
so a fold touches the absorbed classes' cards and not the survivor's. In step 10 the survivor may take a neutral verb name instead
(owner's decision 2026-11-25: `PutCountersEffect`); its own cards then change only the class name. Card call sites of a deleted class or enum are rewritten by a
scratch script the run writes, runs and deletes.

### Not in this plan

- Moving handlers onto the step model (`leave-battlefield-redesign.md` step 14).
- A generic "each player chooses, then all act" loop for verbs whose handler has none.
- Subjects inside `DynamicAmount` and `Condition` classes.
- `agent-docs/EFFECT_MERGE_PLAN.md` steps 19–29 stay valid; when a run here absorbs a class one of them lists, mark that step there.

## Ground rules for every step

- **Rules accuracy first** (AGENTS.md). Behaviour stays identical unless the old behaviour was rules-wrong; then cite the Oracle text
  or ruling, add a test that fails on the old code, and list the change under Findings. Check Oracle text with the Scryfall MCP
  `get_card` and rulings with `get_rulings`; verify every CR number with `get_rule` before writing it anywhere.
- **Verify the premise before editing.** Read the records *and* their handlers to the end. "Identical" handlers have differed in a
  guard, a log line, a `checkWinCondition` call or `TargetSpec.harmful`. If a premise does not hold, say so and stop.
- **Read first**: `agent-docs/ARCHITECTURE.md` whole, then `agent-docs/architecture/resolution.md`; for static abilities
  `agent-docs/LAYER_SYSTEM.md`; for triggers grep `agent-docs/architecture/triggers.md`; for casting `architecture/casting-stack.md`.
- **Grep every reader of a class before changing it**: engine, `magical-vibes-ai`, networking, application tests, card tests,
  `agent-docs/`, `.claude/skills/`. A survivor keeps implementing every capability interface its absorbed classes implemented
  (`RemovalEffect`, `CreatureBoostEffect`, `TokenCreatingEffect`, `ControlStealingEffect`, ...): the AI reads them.
- **Do not regress**: the fixes under "Do not regress these fixes" in `agent-docs/EFFECT_MERGE_PLAN.md`, the invariants in
  `ARCHITECTURE.md`, and the conventions under Standing knowledge below.
- **Tests.** Resolve real cards through the harness, never inspect a card's effect lists; every test method and `@Nested` class has a
  `@DisplayName`. Run the test classes of every card that used an absorbed class, the handler and service tests you touched, and a
  sample of the survivor's cards (`scripts/effect-inventory.py --tests-for` prints the Gradle arguments). Gradle's `--tests` binds to
  the task before it: `bash gradlew :magical-vibes-card-test:b:test --tests com.github.laxika.magicalvibes.cards.b.BoneSplintersTest
  :magical-vibes-card-test:c:test --tests ...`, fully qualified names, no wildcards, never two Gradle runs at once. **Never run the
  full suite**; ask for it where a step says so. Count results from the XML files by name.
- **Docs.** For every absorbed class, update `agent-docs/EFFECTS_INDEX.md`, `EFFECTS_QUICK_REFERENCE.md`, `ORACLE_TEXT_EFFECT_MAP.md`
  and any pattern, template or playbook file that names it (grep; never read those files whole). A new effect or predicate gets its
  agent-docs rows.
- **Bookkeeping.** Record each audited class in `scripts/effect-audit-ledger.csv`, lower the `EffectCountRatchetTest` baselines to the
  new counts, update the Status row, and add the run's line and section under Findings.
- No banner comments. No commit until asked.

## Steps

### 1–8 (done)

1. **Inventory, ledger, ratchet.** `scripts/effect-inventory.py`, `scripts/effect-audit-ledger.csv` (verdicts `absorbed`, `survivor`,
   `unique`, `blocked-step-model`, `blocked-player-loop`, `blocked-other`, `dead`, `step-9`) and `EffectCountRatchetTest`
   (application, `architecture` package).
2. **Dead effects.** 39 records and 32 handlers nothing used were deleted.
3. **Subjects and the resolver.** `PermanentSubject`, `PlayerSubject`, `Subjects`, `SubjectResolutionService` (+ its test) and
   `agent-docs/SUBJECTS_REFERENCE.md`; the divergent "enchanted", "its controller" and "this permanent" lookups were settled.
4. **Player verbs** (4a–4j) take a `PlayerSubject`: draw, lose and gain life, damage to players, discard, mill, player sacrifice,
   player counters and the small ones; their recipient enums are gone (`LibraryOwner` stays for conditions).
5. **Permanent verbs** (5a–5m) take a `PermanentSubject`: destroy, exile, bounce, tap / untap, counters, damage, boost, sacrifice,
   flicker / phase out / remove from combat, attach and token copies; `BounceScope`, `CounterRemovalSubject`, `DamagedPermanentScope`,
   `FlickerScope`, `PhaseOutSubject`, `PhaseOutScope`, `Copied` and `PermanentReference` are gone, and every subject-sibling pair of
   5m has a ledger row.
6. **`GrantScope`** is gone: static and one-shot grants take a `PermanentSubject`, the static ones through the resolver's static
   entry point, with the rule-4 tests (a one-shot set locks, a static one doesn't).
7. **Step results** exist as described under Design.
8. **Fused effects** (runs 1 to 29): 478 classes audited (split 109, absorbed 57, survivor 44, unique 123, blocked-other 85,
   blocked-step-model 41, blocked-player-loop 11, `step-9` 8). The card subject came from run 23. "Counter unless" stays as option B
   (owner's decisions below).

### 9. Sweep the single-card tail (repeatable)

Depends on: 7.

**Triage runs (from 2026-11-23).** A triage run takes about 80 rare records with no ledger row (largest first-word families first,
or as the brief says) and gives each a ledger verdict **without changing code**:

- `step-9`: a fold candidate. The note names the survivor and the parameter or composition that would express it. Batch the
  candidates by survivor, so a fold run can take one survivor's batch.
- `blocked-*`: the note names the cause (a missing part, a step-model prompt, a player loop, an owner's question).
- `unique`: the note says why in a few words.

A triage run reads every record and its handler to the end (a tail handler hides rules knowledge: a guard, a last-known read, a
trigger it fires, an "as much as possible" branch, CR 609.3), checks Oracle text with `get_card` when the handler does something its
name doesn't say, and writes the ledger rows and its Findings section; the ratchet does not move.

**Fold runs** take the `step-9` rows (`--unaudited` does not list them: grep the ledger), one survivor's batch or more per run, in the
order of the fold queue in `effect-cleanup-queues.md`, and give each a verdict in this order of preference:

1. **absorbed, by parameters**: an existing effect expresses it with a subject, a `DynamicAmount`, a predicate, a `Duration`, a
   `Condition` or a `MayEffect` / `ConditionalEffect` / `ChooseOneEffect` wrapper. Rewrite the card, delete the class and handler.
2. **absorbed, by one new parameter**: an existing effect would express it and at least two other audited classes with one more
   component. Add the component, fold all of them.
3. **split**: as under "Splitting a fused effect".
4. **blocked-***: the cause in the note.
5. **unique**: a card whose text has no reusable part (Karn's restart, Word of Command, a subgame).

Guard rails:

- Never add a parameter that only one card sets; that moves the special case instead of removing it.
- Before deleting a handler, read it to the end; the replacement must do everything it did, or the class is not absorbed.
- Check the card's Oracle text with `get_card` and its rulings with `get_rulings` whenever the handler does something its name does
  not say.
- The log line of the survivor wins. A test that asserted the old line is updated; say so.
- Records used by three or more cards may be folded where the shared record says the same thing for every card (owner's decision,
  run 38); say why for each.

Each run ends with its family row in Findings: audited, absorbed, split, blocked (by cause), unique, left. The step is done when
`--unaudited` lists no record used by fewer than three cards.

**Where it stands.** Triage, the fold queue and defect runs 1, 2 and D10 to D26 are done (one line per run under Findings). Left:
the defect queue's open lines (`effect-cleanup-queues.md`); the `blocked-*` rows wait for the causes under Standing knowledge.

### 10. Collapse the head: one record per verb (repeatable, needs sign-off)

Depends on: 5 and 6 done for the verb.

High churn, low behaviour risk. After steps 5 and 6 the high-use siblings of a verb differ only in
their subject component and constructor shapes: `BoostSelfEffect` (1,079 cards),
`BoostTargetCreatureEffect` (1,356), `BoostAllOwnCreaturesEffect` (418), `BoostAllCreaturesEffect`
(195); the damage records (done: run Damage, `DealDamageEffect`, 1,614 card files); the counters pair (done: run Counters, `PutCountersEffect`). One run merges one verb into one record with static
factories that read like the old names, rewrites every card with a script, and deletes the others.

Before each run, state the verb, the card-file count and the factory names, and ask. After each run,
ask for a full card-test run. Skip a verb whose siblings still differ in more than the subject. Since 2026-11-25 the orchestrator decides
and logs instead of asking, and the owner runs the full suite on CI (owner's decision "how the rest of the refactor runs").

The hand-offs earlier runs set aside for this step are listed under Standing knowledge.

### 11. Close: docs, leftovers, final ratchet

Depends on: everything above that is going to be done.

- Delete the temporary adapters (`Subjects.of(PermanentReference)`, `GrantScope`'s subject method) and
  any per-verb enum left without readers. (Both adapters went in 5m and 6.3.)
- `agent-docs`: the effect indexes describe verbs with a subject column; `SUBJECTS_REFERENCE.md` is
  complete; `CARD_IMPLEMENTATION_PLAYBOOK.md`, the templates and the `implement-card` / `review-card`
  skills say "pick the verb, then the subject; compose fused text from steps" and name no deleted
  class.
- Set `EffectCountRatchetTest` to the final counts. Keep `scripts/effect-inventory.py` and the ledger:
  they tell a card author whether an effect already exists.
- Move what is still `blocked-*` in the ledger to `refactors-to-do.md` (or to the step-model plan) as
  entries grouped by cause, then delete this file.

## Standing knowledge

What earlier runs established and later runs need. A run tag ("run U1", "D25") points at that run's Findings section below or in
the git history named at the top. Each shared piece's factories and Oracle wording are also in `agent-docs/` (`EFFECTS_INDEX.md`,
`ORACLE_TEXT_EFFECT_MAP.md`): grep there for the class.

### Conventions

**Face-down and hidden information** (CR 406.3, 400.2, 708.2, 708.4, 708.5):

- `FaceDownVisibility` is the one rule for whether a spell, ability or permanent gives away a face-down card: `nameShownTo` /
  `descriptionShownTo` (per viewer), `publicName` (what every player may read), `sourceName(entry)` (a stack entry's source),
  `maySee` / `maySeeOnStack`. Every prompt, log line, option label and text that names a source or permanent asks it, never
  `getCard().getName()` (text built from a name bypasses the log's redaction; a card segment is redacted by `GameLogService`).
- `Permanent.displayName(card)`: a nameless card's description for text; the card keeps no name for rules that compare names
  (CR 201.2a).
- A copy of a permanent reads `PermanentCopierService.cardToCopy` (face-down copiable values while face down: a nameless 2/2,
  CR 707.2, 708.2a); a copy of a spell `CopySupport.createCopyCard(StackEntry)` (a face-down spell's copy is a nameless 2/2); token
  copies read copiable values.
- An effect that cares about a spell's characteristics judges `CastingPermissionService.spellAsEffectsSeeIt(entry)` (a spell cast
  face down has only its face-down characteristics).
- A face-down permanent's mana value is 0 and it has none of its card's abilities or perpetual changes (CR 708.2a): read the
  permanent, not its card. The reads: `GameQueryService.getPermanentManaValue` (a sacrificed permanent's snapshot too),
  `getEffectiveName` (null while face down) / `permanentHasName`, `hasEffectiveCardType`, `activeStaticEffects` for a static ability;
  a spell being cast face down has no subtypes or abilities (`CostModificationContext.castFaceDown`).
- An ability borrower reads a permanent's activated abilities through one method,
  `GainActivatedAbilitiesOfOwnCreaturesSelfEffectHandler.effectiveActivatedAbilities` (none of the card's while face down or after
  losing them; only its mana abilities under Blood Sun); a graveyard borrower asks `graveyardCardsHaveLostAllAbilities` first; a static
  replacement source asks `hasLostPrintedAbilities`, not only the floating `losesAllAbilitiesByEffect` (CR 613.1f).
- A hand choice is shown to its chooser alone and its log names no card; a prompt over exiled cards masks a face-down card from a
  player who may not look at it; a name prompt offers only the catalog's names and names of cards the chooser can know
  (`PlayerInputService.cardNamesOfferedTo`, checked by `ChoiceHandlerService.isCardNameAllowed`); the AI answers from what it can know.
- **The public log says only what the card's text makes public** (owner's decision 2026-11-23; CR 400.2). The game log is one list every
  player gets (`GameLogService` emits to all players; `GameLogEntry` has no audience), so nothing is logged for one player: the owner's
  hand shows the card. A conjure of a card the text names may name it; a random pick (a random spellbook card, a random library card,
  a sought card) and a drafted or chosen spellbook card going into a hand are "a card" (`GameLog.conjuresIntoHand`), and so is a
  duplicate of a hidden card (a library or hand card, a drawn card); a duplicate of a public card may be named. A perpetual change to
  cards in a hand or library is one line per hand or library with no name and no count, logged also when no card there matches
  (`PerpetualChangeLog`); a choice among hidden cards is one line counting no more than the instruction does (`ChooseCardEffectHandler`);
  a card chosen by `ChooseCardEffect` while it is in a hand is logged without its name. A hand count is public (CR 402.3), so "conjures
  7 cards" is fine. Lines name players, never "you".

**Rules a fold must keep:**

- A card that left a zone and came back is a new object (CR 400.7): returns, delayed returns and "that card" check the card's stay
  (CR 603.7c). Card subjects do this; a handler with its own lookup must too.
- Permanents one instruction moves leave or enter as one event (CR 603.10a, 608.2e); several players choose in APNAP order, then act:
  collect the choices in the choice context, then act once (leave batch or departure batch); reference handlers Sothera, Ol Rin's
  Searing Light. A cost sacrificing several permanents leaves as one batch (`SpellCastingService.paySacrificeCostTogether`).
- A choice with one possible answer is made unasked; a permanent that can't be chosen is neither offered, counted nor drawn at random
  (CR 608.2d); the shared prompts (`PlayerInputService.beginAnyTargetChoice`, `beginMultiPermanentOrPlayerChoice` and the others) drop
  it. A `MayEffect` whose leading step can do nothing isn't asked (a `source()` counter step whose source is gone or can't have the
  counters, a `controllerOf(...)` sacrifice its chooser can't make, a search its searcher can't make; CR 608.2d); gone with no else
  effect, a source counter `MayEffect` still asks (Kresh).
- What a put step moves enters through the entry batch: as-enters, copy and Aura host choices are asked (`handleCreatureEnteredBattlefield`),
  and a card that can't enter stays where it is (CR 101.2, 303.4f, 303.4g); an instant or sorcery card is never put onto the
  battlefield face up (CR 400.4a).
- A ward or ransom decline counters through `CounterSupport.counterSpell(gameData, counterControllerId, description, target)`, so
  Guile's replacement, flashback's exile and the countered events apply.
- Protection is checked at targeting for every target of a spell or ability, benign or harmful (CR 702.16b). A target spec stays
  `harmful` for a harmful verb's `target()`; a non-target subject declares no target.
- Every spell cast is asked through `CastingPermissionService`: `isSpellCastingAllowed` (spell limits, type and name restrictions,
  Ethersworn Canonist, split second; it asks `isPlayerPreventedFromCasting`, the one player-level check) on every casting path, and
  `canCastDuringResolution(gameData, player, card)` / `mayBeginCastDuringResolution` (a land is played, not cast; a split card is
  judged half by half, CR 709.3a) before any cast during a resolution (CR 601.3, 608.2g). Refused, the flow ends as a decline does.
  A free cast pays its cost increases through `SpellCastingService.payFreeCastCostIncreases` / `canPayFreeCastCostIncreases`
  (CR 601.2f).
- "Until [source] leaves the battlefield" is `UNTIL_SOURCE_LEAVES`, and a control effect with it (or "until you lose control")
  holds while its source is phased out (`DurationClock.holds`; CR 702.26d); a resolution's "for as long as that Aura is attached"
  is `WHILE_REMAINS_ATTACHED` (CR 702.26f).
- A prevention prevents in the damage pools so the affected player orders it (CR 616.1), and every prevention sends the
  damage-prevented event.
- The log line of the survivor wins.
- A search that finds nothing (an empty library, no match) is still a search (CR 701.23a): it is recorded, once per search however
  many picks it takes (`LibrarySearchSupport.endSearchFindingNothing`).
- A declined `MayEffect` runs no step and records nothing: an "if you do" after it would read an older step of that kind.
- A new damage, discard or sacrifice primitive calls `recordStep(kind, NONE)` first: central hooks add results to the latest step of
  that kind.
- AI value readers (`SpellEvaluator`) don't look inside a `ConditionalEffect`; a survivor that keeps a capability interface keeps the
  AI's view.
- A fold that merges engine-side stores (several `GameData` maps into one keyed store) is in scope (orchestrator's reading, 2026-11-23).
- The perpetual stores, not the card's printed fields, hold perpetual changes (`PerpetuallyGrantCardCharacteristicsEffectHandler.grant`,
  `perpetualCardBasePowerToughness`, `grantPerpetualKeywords`, `grantPerpetualAlternateCast`, keyed by the physical card id), and they
  stay off copiable values (CR 707.2).

### Composition patterns (Oracle wording → steps)

- "If you do" → `ConditionalEffect(HappenedThisWay.x(), …)` after the step; "Otherwise" → the negated filter, not a `NotCondition`
  (which would also fire when nothing happened); "if you don't [draw] this way" → `NotCondition(HappenedThisWay.drew())`.
- "That many" / "its power" → `AmountThisWay(kind)`; "its controller" of what a step moved → `playersThisWay(kind)`; "it" / "that
  creature" after a step → `thisWay(kind[, filter])` or `cardsThisWay(kind[, filter])`.
- "[Sacrifice / exile] this unless you [discard / sacrifice] …" → `UnlessPaysEffect` with the ransom as a cost (CR 118.12a); "you may
  sacrifice N … If you do / Otherwise" → `MayEffect(SequenceEffect.of(SacrificePermanentsEffect(N, …),
  ConditionalEffect(HappenedThisWay.atLeast(SACRIFICE, N), X)), prompt, Y)`; "if you can't sacrifice" →
  `ConditionalEffect(NotCondition(sacrificed()), …)` after the step.
- "Sacrifice any number … draw that many" → `SacrificePermanentsEffect.anyNumber(filter, you())` + `AmountThisWay(SACRIFICE)`.
- "Exile target card from a graveyard. If it was a [type] card" → the exile + `HappenedThisWay.toACard(EXILE, filter)`.
- "[Another player] may play those cards" → `.playableBy(subject)`; "during your next turn" → `.until(DURING_YOUR_NEXT_TURN)` on the
  library-exile permission step.
- "Destroy target X and all other … that share a [quality] with it" → `allOf(target(), otherPermanentsSharing(target(), quality,
  filter))`.
- "For each permanent destroyed this way, its controller …" → `AmountThisWay(DESTROY)` with `died` / `matching` / `controlledByYou`, or
  the verb over `eachPlayer()` evaluated per player.
- "Note its power … that much" → `NoteAmountEffect` + `AmountThisWay.noted()`; a delayed ability keeps it
  (`TemporaryGlobalTriggeredAbility.keepingNotedAmount`).
- "Choose N target permanents, [verb] one of them at random" → `ChoosePermanentsEffect.atRandom(1, Subjects.allTargets(), label)` + the
  verb over `thisWay(CHOOSE)`; "the other" → the verb over `except(allTargets(), thisWay(CHOOSE))`.
- "Choose a creature type, [verb] for each permanent of it you control" → `ChooseCreatureTypeAtResolutionEffect` + the verb over
  `PermanentCount(PermanentHasTypeChosenThisResolutionPredicate, CONTROLLER)`; a chosen card type is `ChooseCardTypeAtResolutionEffect` +
  `CardHasCardTypeChosenThisResolutionPredicate`; "choose a number" is `ChooseNumberAtResolutionEffect` (sets X).
- "Put counters on a [permanent] you control. It becomes …" → `ChoosePermanentsEffect.exactly(1, controlledBy(you(), filter), label)` +
  the verbs over `thisWay(CHOOSE)`. Amass = that choice over Armies + `PutCountersEffect(thisWay(CHOOSE), …)` +
  `GrantSubtypeEffect(subtype, thisWay(CHOOSE))`.
- "Tap any number of untapped [f] you control" → `ChoosePermanentsEffect.anyNumber(controlledBy(you(), allOf(f,
  PermanentCanBecomeTappedPredicate)))` + `TapPermanentsEffect(thisWay(CHOOSE))` + the verb over `AmountThisWay(TAP)` / `thisWay(TAP)`.
- "Remove all [kind] counters from this. If no counters were removed" → `RemoveCountersEffect.fromSelf(kind, MAX_VALUE)` +
  `ConditionalEffect(NotCondition(HappenedThisWay.removedCounters()), …)`.
- "Reveal it; if X put it onto the battlefield, otherwise into your hand" → `SearchLibraryEffect(filter, REVEAL_ONLY, null, false)` +
  `PutCardsOntoBattlefieldEffect(cardsThisWay(SEARCH, X))` + `PutCardsIntoHandEffect(cardsThisWay(SEARCH, not X))` +
  `ShuffleLibraryEffect(false)`.
- "Seek N; put one onto the battlefield, the rest on the bottom" → `SeekLibraryEffect` + `ChooseCardEffect.oneOf(cardsThisWay(SEEK))` +
  mover + `PutCardsIntoLibraryEffect.onBottomInRandomOrder(except(cardsThisWay(SEEK), cardsThisWay(CHOOSE)))`; a choice among sought
  cards is hidden.
- "An opponent chooses one of the revealed cards" → `ChoosePlayerEffect.opponent()` + `ChooseCardEffect.oneOf(revealedThisWay(filter))
  .by(playersThisWay(CHOOSE))`; "the rest" is `revealedThisWay()` once the chosen card has moved.
- "Look at target player's hand. You may choose a card from it. If you do" → `ChooseCardsFromTargetHandEffect(... KEEP_IN_HAND, upTo,
  look)` + `ConditionalEffect(HappenedThisWay(CHOOSE), …)` over `cardsThisWay(CHOOSE)`.
- "Choose two target creatures controlled by the same player; that player sacrifices one" → `ChoosePermanentsEffect.exactly(1,
  allTargets(), label).chosenBy(controllerOf(allTargets()))` + `SacrificeSelfEffect(thisWay(CHOOSE), controllerOf(thisWay(CHOOSE)))`.
- "You may destroy target X. If you do" → `MayEffect(SequenceEffect.of(destroy, ConditionalEffect(TargetPermanentMatches(not
  indestructible), …)))`; "you may pay {c}. If you do, destroy … and …" has no gate (CR 118.12).
- "You may pay {X}. When you do, [ability reading X]" → `MayEffect(SequenceEffect.of(EachPlayerPaysAnyManaEffect.byYou(X_MANA),
  QueueReflexiveAbilityEffect.withEventValue(effect, manaPaid())))`.
- "If you haven't cast a spell this turn, you may cast … If you do, you can't cast additional spells" →
  `ConditionalEffect(NotCondition(ControllerCastSpellThisTurn(null)), CastCardFromGraveyardEffect.of(…).ifCast(
  PlayerRestrictionEffect.thisTurn(you(), castingSpells())))`.
- "Return that card to its owner's hand at the beginning of the next end step" → `aboutCardAtNext(endStep(ANY), triggeringCards() |
  sourceCard(), PutCardsIntoHandEffect(same subject))`.
- A static "can't be blocked if [condition]" → `ConditionalEffect(condition, GrantEffectEffect(CantBeBlockedEffect, source()))`; a
  turn-limited static cost change → `ConditionalEffect(ControllerTurn(), …)`.
- Several targets in one triggered ability → plain `target(...)` groups plus `addTrigger(trigger, e1, e2)` with
  `registerEffectTargetIndex(e, group)` for each effect.

### Shared pieces (reuse before adding)

**Step 10's records** (the runs' Findings below have the detail):

- `PutCountersEffect` (run Counters): `onSelf(kind[, int | DynamicAmount])`, `onSelfCountingTargetPlayer(kind, amount)`,
  `onSelfWithTargetPermanent(kind)`, `placedByEachController()`, over any subject (`createdThisResolution()`, `returnedThisResolution()`,
  `thisWay(kind)`). One placement: counter events, the CR 616.1 order, PUT_COUNTERS, the source snapshot refreshed; the mana-ability
  path places it inline. A source-relative amount after a token or return step is noted first (`NoteAmountEffect`).
- `RemoveCountersEffect` (run Remove counters): `fromSelf(kind, int | DynamicAmount)`, `fromSelfCountingTargetPlayer(kind, amount)`,
  `fromTarget()` (any one kind), `fromTarget(kind, int | DynamicAmount[, targetPredicate])`, `fromTarget(kind, targetPredicate)`,
  `new RemoveCountersEffect(subject, kind[, int])`. One removal path (`PermanentCounterSupport.removeCounterFromPermanent`): vanishing's
  last time counter (CR 702.63a), oil, the controller's LAST_REMOVED event per permanent (CR 603.2c), a Siege's defeat after its last
  defense counter (CR 310.12b), REMOVE_COUNTERS recorded after the kind is chosen (`ANY`). "Remove all counters from it" is
  `PermanentCounterSupport.removeAllCounters`.
- `DealDamageEffect` (run Damage): one source deals N damage to one recipient. A `Recipient` kind (any target, creature, creature or
  planeswalker, player or planeswalker, permanent) fixes `targetSpec()`; factories `toAnyTarget`, `toAnyTargetInGroup`,
  `toAnyOtherTarget`, `toEventObject`, `toAnyTargetOfAnOpponentsChoice`, `toAnyTargetChosenAtRandom`, `toTargetCreature`,
  `to(PermanentSubject, amount)`, `toTargetCreatureOrPlaneswalker`, `toTargetPlayerOrPlaneswalker`, `toTargetOpponentOrPlaneswalker`,
  `toTargetPermanent`; riders `byTriggeringPermanent()`, `unpreventable()`, `unpreventableWhen(c)`, `unpreventableAndUnredirectableWhen(c)`,
  `cantBeRegenerated()`, `withUnconditionalRegenerationPrevention()`, `exilingDamagedCreaturesThatWouldDie()`, `exilingIfItWouldDie()`,
  `exilingPermanentsDealtDamage()`, `recordingDamageDealt()`, `withDamage(a)`. Run Damage P: the player kinds `to(PlayerSubject, int |
  DynamicAmount)`, `eachEvaluatedFor(amount, players)` (each amount fixed first, CR 608.2h), `toPlayerOrTheirPlaneswalker`, rider
  `byEventDamageSource()`; `subject` is null for them. Run Damage M1: the set kinds `toEach(PermanentSubject set, n)` (found once, CR
  608.2h; `toEachCreatureOfTargetedPlayer`, `toEachControlledByTargetedPlayer`, `toCreaturesOfTargetedPlayer`) and `toEachTarget`
  (`toEachTargetCreature`, `toEachTargetCreatureOrPlaneswalker`), one damage event, no AI one-target view. Run Damage M2: the mass factories
  `toEachCreature(n[, filter])`, `toEachCreatureAndPlaneswalker(n[, filter])` (the filter narrows both) over `controlledBy(eachPlayer(), …)`,
  `eachEvaluatedFor(amount, set)` (per permanent, the permanent the amount's source), riders `andTo(PlayerSubject)` (players in the set's
  event), `exilingDamagedCreaturesThatWouldDie()` / `exilingPermanentsDealtDamage()` / `cantBeRegenerated()` on a set; `sweepsBoard()` for
  an all-player set (`BoardWipeEffect`). Run Damage R: the rider `dealtBy(PermanentSubject dealers)` (each dealer deals its own
  damage through `entryDealtByPermanent`, amount per dealer, recipients found once; a target dealer nothing once gone, CR 608.2b), factories
  `bite([g, g'[, n]])`, `toTargetOfGroup`, `toTargetPermanentOfGroup`, `targetCreatureToItself(n)`, `toEachOther(dealer, set, amount)`. Run X: the
  rider `excessToControllerInstead()` / `excessToControllerInsteadIfDealerHasTrample()` (one target creature; excess worked out before the
  damage, dealt to the controller in the same event, the event value; CR 120.4a). Every kind: one permanent-damage path (D24), DEAL_DAMAGE
  begun, one excess tracker (CR 120.4a, 120.10; the dealer's deathtouch), the win check. The creature kinds loop over the effect's
  targets; the any-target kind can't (Magma Burst kicked: its unbound part would hit the kicked target too).
- `DamageSupport.entryDealtByTriggeringPermanent` is the one "it deals" carrier: a departed dealer is its departure snapshot first (its
  keywords as it left, a static grant's included), then the trigger-time snapshot; an entry that names no triggering permanent is dealt
  by its source. Its damage result is added to the resolving entry's DEAL_DAMAGE step. `DamageSupport.dealerAsItIsOrWas` /
  `dealerControllerId` / `entryDealtByPermanent`: a permanent deals damage as itself, live or last known (CR 120.1, 608.2h).
  `PermanentRemovalService` refreshes the resolving entry's source snapshot as the source leaves, and last known information keeps
  lifelink, deathtouch, infect and wither (CR 608.2h).
- `BoostEffect` (run Boost B1): "[permanent] gets +X/+Y [and gains K] [for a duration]", a layer-7c floating effect per permanent
  (CR 613.4c, 611.2a). Factories `self`, `of(subject, …[, keywords])` (amounts read the boosted permanent, `AmountsRead.BOOSTED`),
  `target`, `targetInGroup`, `withCastTimeXValue`, `targetPermanent` (creature or Vehicle, harmful spec), `each(subject, …)` (only creatures,
  the set found once, CR 611.2c) (amounts read the source, `AmountsRead.SOURCE`, CR 608.2h). B2's mass forms `creaturesYouControl`,
  `allCreatures`, `creaturesTargetPlayerControls` (`[, filter]`, a `controlledBy` subject; `isMassBoost()`: one summary log line, left out of
  the AI's pump timing, the trigger label and text changes) and `each(triggeringAttackers(), …)`. The static self bonus is `self` in the
  `STATIC` slot. `CreatureBoostEffect.targetedBoost(effect)` is the AI's targeted shape (a `BoostEffect` only with a target subject).
- `UnlessPaysEffect(PlayerSubject payer, CostEffect ransom, CardEffect ifNotPaid, CardEffect ifPaid)` (runs U1 to U3b'): "[X] unless [P]
  pays [cost]" (CR 118.12a) and "[P] may pay. If they do" (`ifNotPaid` null). Counter factories `counterUnlessPays(int | String |
  DynamicAmount)`, `counterUnlessPays(mana, life)`, `counterUnlessPaysX()`, `counterUnlessPaysLife(int | DynamicAmount)`,
  `counterUnlessDiscards()` / `(filter, label)`, `counterUnlessDiscardsAtRandom()`, `counterUnlessDiscardsHand()`,
  `counterUnlessDiscardsOrPays(n)`, `counterUnlessSacrifices(filter, description)` / `(count, filter, description)`,
  `counterThatSpellUnlessYouSacrifice(filter, description)`, `counterUnlessBlights(n)`, `counterUnlessCollectsEvidence(n)`,
  `counterUnlessExilesGraveyard()`, `counterUnlessGetsPoisonCounters(n)`, `counterUnlessTakesDamage(n)`, `counterUnlessWaterbends(n)`,
  `changeTargetUnlessPays(n)`, `.exilingIfCountered()`, `counteredSpell()`, `retargets()`; "may pay" factories `youMayPay(mana, ifYouDo)`,
  `youMayPay(mana, life, ifYouDo)`, `youMayPayLife(int | DynamicAmount, ifYouDo)`, `unlessYouPay(mana, effect)`,
  `unlessYouPayLife(life, effect)`, `mayPay(payer, mana, ifTheyDo)`, `unlessPays(payer, mana, effect)`, `unlessPaysLife(payer, life,
  effect)`, `.ifPaid(effect)`, `.ifNotPaid(effect)`, `.withPrompt` (else the ransom's "Pay {1}?"). Rules: the payer is asked even when
  the inner effect can do nothing; a counter whose spell already left the stack asks nothing; a paid ransom never takes `ifNotPaid`
  (CR 118.12); the question is asked unless `couldPossiblyPay` proves no payment is possible; a paid answer takes a "do this only once
  each turn" action and a taken one makes the payment impossible (CR 603.2h); an intervening "if" is a `ConditionalEffect` around the
  record (CR 603.4). Payers: `you()`, `controllerOf(target())` (CR 608.2h), `triggeringPlayer()`, `defendingPlayer()`, `activePlayer()`,
  `chosenPlayer()`, `targetPlayerDeclaredElsewhere()`, and any several-player subject. `.payingTogether()` (run U3c): each answer only
  chooses (`ResolutionCostPaymentService.choose`, a pick made unasked when only that many could be chosen, `completePick`), the choices on
  `UnlessAnswer.choices`, and after the last answer `paymentMadeTogether` / `payTogether` splices one departure, tap, damage or draw step
  before the branch (CR 101.4, 608.2e); for sacrifice, tap, damage and draw ransoms. `unlessPays(payer, CostEffect, effect)`; a life
  ransom is read for each payer (`alternatives(…, payerId)`). How it runs: the payers are `players(payer)`
  (APNAP, CR 101.4), named once; a player who left the game or has no way at all to pay is dropped, every other one asked, even after
  another paid. The state is the entry's `UnlessAnswer(effectIndex, remainingPayers, anyPaid, alternative, payingPlayerId, partsToPay)`
  (`StackEntry.unlessAnswer(effectIndex)` / `setUnlessAnswer` / `clearUnlessAnswer`), not `resolvedMayAccepted`; `UnlessPaysEffectHandler.nextStep` (called by
  `EffectResolutionService` before the handler) returns ASK, PAID, NOT_PAID (the branch, once, after the last answer), NOTHING (the
  spell left) or PAYING (an all-of part asks); `answer` (from `MayAbilityHandlerService.handleResolutionTimeMayChoice`) pays and is
  idempotent for a Lich's Duel Mastery re-ask; `UnlessAnswer.askingAlternative` asks each payable alternative of an `AnyOfCost` in Oracle order;
  `anotherPlayerAlreadyPaid` is the AI's read (`AiDecisionEngine.anyPlayerManaPaymentAlreadyMade`). The chosen branch is put next
  (`spliceSequenceSteps`) and resolved through the loop, so a wrapper in it is judged as at the top level; `Card.bindEffectToTargetGroup`
  binds both branches. U6: `forEach(objects, payer, ransom, unpaid -> X)` is "for each [object], [X to it] unless [P] pays": one question per object (`controllerOf(objects)` / `ownerOf(objects)` read for that object, else every player the payer names), in the payers' APNAP order, objects gone before their turn skipped; after the last answer the unpaid objects are the entry's CHOOSE step and X resolves once over `thisWay(CHOOSE)` (CR 608.2c, 101.4); `UnlessAnswer.remainingObjects` / `unpaidObjectIds`. U6b: `forEachCard(cards, payer, ransom, unpaid -> X)` is the same over revealed cards (every player the payer names asked about each card, a card that left its zone skipped, CR 400.7; the unpaid ones the CHOOSE step where they are, `cardsThisWay(CHOOSE)`); a `DiscardCardTypeCost` ransom can be `.payingTogether()` (nothing picked at the answer; after the last one a `DiscardEffect` over `acceptedPlayers()` has each payer pick in APNAP order and discards at once, CR 101.4, 101.4a); `perPayer()` (set by `eachUnless`) keeps the AI from reading another payer's payment as paying for it. The hard AI scores it as value(`ifPaid`) − value(`ifNotPaid`) for the payer (`SpellEvaluator`), a branch making the payer lose life or take damage (named by the payer's own subject) on the ransom's life scale, an any-of ransom priced by the alternative asked (U3b1). Card tests answer an any-of ransom with `BaseCardTest.payUnlessAlternative(payer, action)` (U3b1). U3b2: `eachUnless(payers, ransom, players -> X(players))` is "each of them [does X] unless they pay": every payer chooses, then X happens to those who didn't, together (CR 101.4), `UnlessAnswer.paidPlayerIds` being the entry's accepted players at the branch; a payer `targetPlayer()` declares the target when no branch does (harmful); ransoms `ReturnPermanentToHandCost.youControl` / `.anyPlayers`, `PutCardFromHandOnTopOfLibraryCost`, `LandDropCost`, `SacrificeCreaturesWithTotalPowerCost`, `PayManaCost.perAmount(symbols, amount)`; amount `PlayerCount(PlayerSubject)`. Engine-built
  "you may pay" steps insert an `UnlessPaysEffect`. U4a: `PayManaCost.forSourceManaCost()` is "its mana cost" (the source live, else as it
  last existed, {X} as 0; face down or none is unpayable and not asked, CR 708.2, 118.6); a sacrifice description may carry its
  determiner ("another creature"); "do X. If you can't, Y" is `SacrificePermanentsEffect` + `ConditionalEffect(NotCondition(sacrificed()), Y)`. U4b
  ransoms (each a service case): `PayEnergyCost` (any amount, read as it resolves; `SourcePermanentManaValue` is "its mana value"),
  `ExileTopCardOfGraveyardCost`, `RemoveCounterFromSourceCost` of one kind, `RemoveCounterFromControlledPermanentCost` (one pick per
  counter, `ChoiceContext.ResolutionCostCounterRemoval`), `PutCardsFromGraveyardOnBottomOfLibraryCost(n)`, `ReturnCardFromGraveyardToHandCost`,
  `MillControllerCost`, `PutTypedCounterOnSourceCost` (put by the payer), `ReturnPermanentToHandCost.youControl(count, filter, words)`; a
  Phyrexian symbol is paid with its colour or 2 life (CR 107.4f); mandatory "pay {E}{E}. If you can't" is `PayEnergyEffect` +
  `ConditionalEffect(NotCondition(new HappenedThisWay(PAY_ENERGY)), …)`. U4d: the cumulative upkeep ransoms `FlipCoinsCost` (in the answer's Krark's Thumb window), `OpponentCreatesTokensCost`, `PutCounterOnOpponentCreatureCost` (a pick per counter), `DrawCardsCost`, `ExileTopCardOfLibraryCost`, `PutCardsFromSingleGraveyardOnBottomOfLibraryCost` (a graveyard per payment), `OpponentGainsLifeCost`, `GainControlOfPermanentsCost` (a different permanent per counter); `CumulativeUpkeepEffectHandler` puts `UnlessPaysEffect(you(), cost x age counters, sacrifice, paid)` next (`spliceSequenceSteps`), a mana ransom keeping `forCumulativeUpkeep` through the answer; every mana ransom payment records PAY_MANA by type (`StepOutcome.manaByColor`, `AmountThisWay.manaPaid(ManaColor...)`). U4c: `PayEchoCost` (echo, `KeywordTriggers.echo`) is read by
  `UnlessPaysEffectHandler.alternatives` as the cost it names and paid through that cost's case; an `AlternativeEchoCostEffect` makes it
  `AnyOfCost({0}, echo cost)`; "when its echo cost is paid" is `ifPaid` = `QueueReflexiveAbilityEffect` (a trigger on the stack).
- `ResolutionCostPaymentService` (`service/effect/cost`, D25): a cost a resolving effect asks any player for (CR 118.12, 118.12a). A new
  ransom kind is a case here. `couldPossiblyPay(gd, payerId, cost, sourceCard)` (takes the resolving entry) answers "no" only when it
  is certain (owner's rule): life alone that `canPayLife` refuses (CR 119.4, 119.8); mana the pool can't pay ({B} as life under K'rrik
  included) **and** no mana source at all (`hasManaSource`: a permanent with an intrinsic or an activated mana ability, printed or
  granted, tapped or not; another player's "any player may activate" mana ability; an owned card in hand, graveyard, exile or the
  command zone with a mana ability that works there; Channel's or the foreign-land permission); every other cost `canPay`; `AnyOfCost`
  any alternative; `AllOfCost` per owner's decision U3b'-1. `canPay(gd, payerId, CostEffect, sourceCard)` (CR 118.3: pool plus makeable
  mana) keeps the battlefield estimate for its other callers. `pay(gd, payer, CostEffect, Payment(sourceCard, sourceControllerId,
  answering))` returns `PAID`, `NOT_PAID` or `ASKING` (a discard / sacrifice / tap prompt or Lich's Duel Mastery's death replacements
  finishes it); `Payment` carries the resolving `entry`. Cases: `PayManaCost` (already read: no reduction, increase or source cost;
  `announcedX`, `lifeAmount`; `PayManaCost.x()`, `PayManaCost.ofTriggeringSpell()`: live spell, else its snapshot, face-down or no mana
  cost unpayable, CR 708.2, 118.6), fixed `PayLifeCost`, `DiscardCardTypeCost`, `DiscardRandomCardCost`, `DiscardHandCost`,
  `SacrificePermanentCost` (candidates judged from the entry's source, `excludeSource`), `SacrificeMultiplePermanentsCost` (exactly N,
  death replacements first, CR 616.1), `SacrificeSelfCost`, blight (`PutCounterOnControlledCreatureCost.blight`, CR 701.68), plain
  `CollectEvidenceCost` (CR 701.59), `ExileGraveyardCost`, `ExileNCardsFromGraveyardCost`, `ExileCardsCost`, `GetPoisonCountersCost`,
  `DamageFromSourceCost` (dealt by `Payment.entry`, CR 608.2h), fixed `WaterbendCost` (CR 701.67a; its mana is a tax),
  `TapMultiplePermanentsCost` (`MultiplePermanentTapCostHandler`), `SourceControllerDrawsCost`, `AnyOfCost`, `AllOfCost`. A sacrifice
  offers only what the payer could sacrifice for that entry (U3c): none while an opponent's effect can't make them sacrifice (Sigarda,
  `canEffectCauseSacrifice`), never a permanent that can't be sacrificed or chosen (`cantBeChosenToSacrifice`), nor a creature token the
  asking triggered ability can't make its controller sacrifice (`GameQueryService.isCreatureTokenProtectedFromTriggeredAbility`, The
  Master, Multiplied); its filter context names the entry's target's card, so "another" than the target (`PermanentIsTargetCardPredicate`, Torment of Venom) is judged (U3b1). `DiscardHandCost` pays through `DiscardHandEffectHandler` (U3b1). Discards and sacrifices go through the shared
  prompts, opponent-caused when the payer isn't the source's controller; one context pair, `PermanentChoiceContext.ResolutionCostPayment`
  / `MultiPermanentChoiceContext.ResolutionCostPayment`, completed by `completePermanentChoice` / `completeMultiPermanentChoice`. Mana
  raises `ActionEvents.taxPaid` for the mana spent when the payer isn't `sourceControllerId`; `payManaFromPool` is the hand-rolled pool
  payment; static `canPayLife(GameQueryService, gd, player, amount)` is the one resolution-time life check (CR 119.4, 119.8). A mana
  ability that works from the hand may be activated while a may-pay mana prompt is open (`GameService.activateHandAbility`,
  `AbilityActivationService.isHandManaAbilityAt`, CR 605.3a).
- Ransom cost types (`model/effect`): `AnyOfCost(List<CostEffect>)` / `AnyOfCost.of(a, b[, c])` (at least two alternatives, none nested;
  payable while any is, CR 118.3; run U7a: also an additional cast cost, `addCastingCost(AnyOfCost.of(...))` of standalone cast costs,
  the alternative read from the request field it consumes by `AdditionalSpellCostService.chosenAlternative` (none: mana, else life;
  two: refused) and swapped into its own `ExtractedCosts` slot by `withChosenAlternative` before validation and payment, CR 601.2b,
  601.2h; a free cast offers the payable ones in record order, `ExileFreeCastQueueSupport`; AI facets read the first alternative;
  run U7b: a `PayManaCost` alternative is `ExtractedCosts.additionalManaCost`, paid as part of the total cost (CR 601.2f) and
  chosen when no other alternative's field is filled; `chosenFrom` keeps the any-of; exile casts take a sacrifice through
  `additionalCostSacrificePermanentIds`; run U7c: `ForageCost()` (CR 701.61a), "forage or pay {B}" an any-of); `AllOfCost(List<CostEffect> parts)` / `AllOfCost.of(...)` (all or none, CR 601.2h, 118.3; at most one
  mana part, no nesting); `ExileGraveyardCost()` (always payable); `GetPoisonCountersCost(int)`; `DamageFromSourceCost(DynamicAmount)`
  (paid even if prevented, CR 118.12; every "unless [P] has [this] deal N damage to them" since U5, `new DamageFromSourceCost(new SourcePower())` for "equal to his power"; `DamageFromSourceCost.of(int)`); `ExileCardsCost(CardSubject)` (the entry's EXILE
  step); `SourceControllerDrawsCost(int)` (U3c: "has you draw a card", the payer chooses and the source's controller draws; paid together
  it draws once for the round, Development's rulings); `DiscardCardTypeCost(null, null, n)` of several cards (Phantasmagorian).
- `SubjectResolutionService.apnapOpponents(gd, controllerId)` (U3c, moved from the deleted `MayPayManaEffectHandler`): the opponents in
  APNAP order, a new mutable list.
- `EachPlayerPaysAnyManaEffect(startingWithYou, PlayerSubject payers, Resource)` / `.byYou(Resource)` (`lifeExactly` went in U3c): Resource `MANA`, `X_MANA`, `ENERGY`, `LIFE`, "pay any amount" at resolution; records PAY_MANA / PAY_ENERGY / PAY_LIFE,
  read by `AmountThisWay.manaPaid()` / `energyPaid()` / `lifePaid()`.
- Static combat taxes: `CantAttackOrBlockUnlessPaysEffect(amount, Side, attackerFilter)` / `.attacking(amount)` / `.blocking(amount,
  attackerFilter)` (unfiltered block cost once per blocker, filtered per matching attacker; CR 508.1h, 509.1d); `RequirePaymentToAttackEffect`
  / `RequirePaymentToBlockEffect` floated on players, `AmountsFixedWhenFloatedEffect.withAmountsFixed` (CR 608.2h), judged for the attacked
  player or planeswalker's controller (CR 506.2a, 508.5), `.includingAttacksOnBattles()`; `getGlobalBlockManaTax`.
- Other costs: `PayLifeCost(DynamicAmount)` / `PayLifeCost.x()`, `PayEnergyCost(DynamicAmount)` / `.x()`, `fixedAmount()`
  (CR 601.2f, 602.2b); `AmountEvaluationService.evaluateCost` / `isUnpayableCost` (CR 903.4f); `GameQueryService.canPayLifeForCosts`
  (CR 119.8); `PutCounterOnControlledCreatureCost.blight(n)` / `.blightX()` (CR 701.68a, 701.68b); `EscalateCost` (`escalate`, `entwine`;
  CR 702.120a, 702.42a); `SacrificeAllMatchingPermanentsCost`; `ActivatedAbility.withMaximumXValue`; `PayZeroRatherThanAbilityCostEffect`
  (CR 118.9, 602.2b); `ModifyKeywordCostEffect`, `ModifyCastCostPerTargetEffect`, `ReduceActivatedAbilityCostEffect`
  (`ActivatedAbilityKind`), `IncreaseSpellCostEffect(predicate, n, PlayerSubject)`, `ReduceCastCostForNextMatchingSpellEffect`,
  `ReduceActivationCostEffect` / `IncreaseActivationCostEffect` (any `DynamicAmount`), `CostModificationScope.names(controller, player)`.

**Steps, choices and readers:**

- `ChooseCardEffect` (`oneOf`, `upTo`, `oneOfEach`, `atRandom`, `fromEachOwner()`, a chooser `.by(...)`) records CHOOSE over cards;
  `ChoosePermanentsEffect` (`exactly`, `upTo`, `anyNumber`, `atRandom`, `chosenBy(player)`, `oneForEach`, `forEachVote`, `toSacrifice()`
  (only candidates the chooser could sacrifice, CR 608.2d, 701.21a), `thatCanBeAttachedTo` / `thatCanHost`,
  `thatTheTriggeringSpellCouldTarget()`) over permanents; `ChoosePlayerEffect` (`opponent()`, `chosenBy(player)`) /
  `ChoosePlayerAtRandomEffect` (`playersThisWay(CHOOSE)`); `ChooseCardNameAtResolutionEffect` + `CardHasNameChosenThisResolutionPredicate`;
  `ChooseDamageSourceEffect.any()` / `.youControl()` (CR 609.7a candidates); `ChooseOneAtRandomEffect`; `ChooseOneEffect.anOpponentChooses`
  (announced before priority, CR 700.2e; targets after the mode, CR 601.2c; `Subjects.modeChooser()`); `ChoosePermanentOnEnterEffect`.
- Step kinds beyond Design's list, each with its readers: TAP (`TapPermanentsEffect`), GOAD (`GoadTargetCreatureUntilNextTurnEffect.of`),
  SEARCH (`HappenedThisWay.searched()`, `toACard(SEARCH, filter)`, `AmountThisWay.searchedManaValue()`, `Subjects.foundThisWay()`), SEEK
  (`SeekLibraryEffect`), CONJURE (`ConjureCardsEffect`, the drafts), VOTE (`VoteEffect`, `VoteResult`, `VotesFor`, `VotesReceived`,
  `Subjects.mostVoted()` / `votedFor()` / `mostVotedCards()` / `playersWithNoVotes()`), BID (`BidLifeEffect`), ATTACH, FLIP
  (`FlipUntilLoseEffect`), PAY_ENERGY, PAY_LIFE, and MANIFEST (manifest dread, `CloakTopCardOfControllerLibraryEffect`). `ProliferateEffect`
  is a PUT_COUNTERS step; `HappenedThisWay.toAPlayer(kind, PlayerRelation)`; `AmountThisWay.milledManaValue()` (CR 701.17c),
  `chosenManaValue()`, `revealedCards(filter)`, `revealedManaValue()`.
- `NoteAmountEffect` (NOTE, `AmountThisWay.noted()`); `QueueReflexiveAbilityEffect` for "when you do" (CR 603.12),
  `.withEventValue(effect, amount)` fixes the reflexive ability's X; `RepeatEffect(n, effect)`; `SeparateIntoPilesEffect`;
  `TemptingOfferEffect.of` / `.offeredOnlyTo` (all decide, then act; CR 608.2d); `IntuitionEffect`; `RollTwoDiceChooseOneEffect` and
  `DiceRollService.rollDice` (CR 706.6).
- `EffectResolutionService.resolveNestedInCurrentResolution(gameData, nested)`: steps resolved inside whatever is resolving, without the
  stack; if they stop for an answer they resume first.

**Subjects and their resolution:**

- Permanent subjects beyond Design's list: `allTargets()`, `targetDeclaredElsewhere()`, `departedThisWay(kind)` (duplicated as last
  known), `picked()`, `paidAsCost()` (spell tap costs too; copies carry them, CR 707.10), `convokedThisSpell()`, `lastAttachedCreature()`
  (only where a ruling says so; plain `attached()` names nothing once the source left, CR 608.2h, 704.5m), `attachmentsOf(...)`,
  `triggeringAll()`, `granter()`.
- Player subjects: `ownersOf(CardSubject)`, `acceptedPlayers()`, `targetPlayerChosenAtRandom()`, `playersThisWay(kind, CardPredicate |
  PermanentPredicate)`, `matching(players, PlayerPredicate)`.
- Card subjects: `cardsOf(PermanentSubject)` and `sourceObjectCard()` (perpetual verbs; the same object only, CR 400.7),
  `triggeringCardsInHand()`, `triggeringCardsAsTheyLastExisted(filter)`, `cardsYouOwn(filter)`, `ownedBy(cards, owners)`,
  `topmostCardInYourLibrary(filter)`, `cardsInLibraries(players, filter)` (only for `ChooseCardEffect.atRandom`), `cardsOutsideTheGame`,
  `exiledWithSourceMatching(filter)` (a face-down card matches nothing, CR 406.3a), `exiledWithSourceBy(link, filter)` (CR 607.2a,
  607.5), `exiledWithSourceUnlinked(filter)`, `exiledAsCost()`, `targetGraveyardCards(filter, scope, n)` (non-target; the card's target
  group declares them), `cardsInGraveyards(players, filter)`; `sourceCard()` in exile is checked against its exile stay.
- `SubjectResolutionService` helpers: `namesPlayer` (judges `controllerOf(attached())`, `ownerOf(source())`, `controllerOf(source())`,
  `activePlayer()`, `except(...)`), `namedCards` (static card subjects), `namesExiledCard`, `cardsToDuplicate` (last known, CR 608.2h),
  `spellOnStack`, `damageSourceIds`, `cardTypes` / `creatureTypes` (live or last known), `bindChosenValues` (chosen type, card type,
  player, parity, spell colour into a rule's filter as it registers, CR 608.2h, 611.2c), `bindResolutionFilter`, `bindPermanentSubjects`,
  `lastingRule`, `setFilterMatches`; `AmountEvaluationService.bindAmounts` / `bindTriggeringSpellManaValue` /
  `bindTriggeringSpellCardTypes`; `StaticEffectSupport.namesOnBattlefieldOf`.
- Targets: `TargetSpec.chosenAtRandom` / `atRandom()` (fires "becomes the target" and crime, CR 601.2c, 603.3d, 700.13);
  `StackEntry.targetsForGroup` (an activated ability's groups are positional); `ValidTargetService.permanentIdsSpellCouldTarget`.

**Movers, library, casting:**

- Movers over card subjects: `PutCardsOntoBattlefieldEffect` (`underControlOf`, `manifested()`, `attachedTo(host)`, face down, entering
  effects), `PutCardsIntoHandEffect`, `PutCardsIntoLibraryEffect` (`shuffledIn`, `alwaysShuffling`, `onBottomInRandomOrder`; each library
  shuffled once, CR 701.24c, 701.24d), `ShuffleTargetPermanentIntoLibraryEffect.of(subject)`, `ReturnChosenPermanentsToHandEffect`,
  `ExileChosenPermanentsEffect`, `ExileCardFromHandEffect`, `ExileCardsWithSameNameEffect`, `ExileCardAtRandomEffect` / 
  `ExileCardsFromTopUntilEffect` (`.withSource()`, CR 607.2a), `ExileGraveyardCardsEffect.cards(...)` (`.imprintingOnSource()`),
  `ExileTargetSpellEffect` (`.triggeringSpellWithSource()`), `ExileTopCardOfOwnLibraryEffect.exiledFaceDown()`, `RevealCardsFromTopUntilEffect`,
  `RevealCardsFromHandEffect`, `RevealTopCardOfLibraryEffect` (shown to every player, CR 701.20a), `DiscardCardsEffect`,
  `DiscardEffect.matching` / `.eachEvaluatedFor`, `DiscardHandEffectHandler.discardHand` (the one discard-hand primitive),
  `ReturnCardFromGraveyardEffect` (records `returnedThisResolution()`), `EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect.by`.
- Library: `SearchLibraryEffect` (`KEEP_IN_LIBRARY`, `EXILE_IMPRINT`, `REVEAL_ONLY`), `SearchTargetLibraryEffect`,
  `EachPlayerSearchesLibraryEffect` (`cardToTop`, `oneSearchForEach()`, `leavingLibrariesUnshuffled()`), `SearchLibraryAndOrGraveyardForCardToHandEffect.inZones`,
  `SearchOutsideGameForCardToHandEffect`, `SearchLibraryForSameNameAsEachPermanentEffect`, `SearchLibraryForOneCardOfEachSubtypeEffect`,
  `LibrarySearchFollowUp.ToHandPick`, `PlayerCanSearchTheirLibraryPredicate` / `LibrarySearchRestrictions.find`; `SeekLibraryEffect`
  (`.fromAmongTop(n)`, `.by(seeker)`), `SeekCardsToHandEffect.alsoSought`; `LookAtTopCardsAndMayCastEffect` (`lookAt`, `reveal`, records
  REVEAL; split cards by half, CR 709.3a; CR 603.2h), `LookAtTopCardMayRevealMatchingToHandEffect`,
  `LookAtTopCardMayPutMatchingOntoBattlefieldEffect`, `LookAtTopCardsOfTargetLibraryEffect.lookingAt`, `LibrarySelectionFollowUp.inResolution`.
- Casting now: `CastCardsWithoutPayingManaCostEffect` (`anyNumber`, `upTo`, `withTotalManaValueAtMost`; one ordered choice, CR 608.2g),
  `CastCardFromGraveyardEffect.of(CardSubject)` (`.ifCast`), `CastCardWithoutPayingManaCostIfAbleEffect`,
  `MayCastAnySpellFromHandWithoutPayingManaCostEffect` (`anyNumber`, `upTo`, `orElse`), `MayCastDiscardedCardWithoutPayingManaCostEffect.castBy`,
  `CopyImprintedCardAndMayCastCopyEffect.of(cards)` (`.castingUpTo`, `.castingAnyNumber`), `ExileTopCardsAndMayCastSpellsEffect`
  (Adventure-only fits cast as its Adventure, CR 716.3a; `controllerMayPlayUpTo`, CR 305.2, 305.3).

**Permissions, restrictions and grants:**

- `AllowPlayCardsFromExileEffect` over a `CardSubject` (`thisTurn`, `untilEndOfYourNextTurn`, `whileExiled`, `whileYouControlSource`,
  `alsoCardsExiledLaterThisTurn()` (CR 611.2c); `playableByOwner()`, `playableOnlyIf`, `withAnyManaType`, `withoutPayingManaCost`,
  `exilingInsteadOfGraveyard`); `ExileTopCardMayPlayThisTurnEffect` (`fromLibraryOf`, `until`, `playableBy`, `choosingOne`, `upTo`,
  `mayOnlyCast`); `PlayFromAmongExiledCardsEffect`; `AllowCastFromTopOfLibraryEffect` (CR 118.9), `AllowCastMatchingCardsFromGraveyardThisTurnEffect`,
  `AllowCastTargetCardFromGraveyardThisTurnEffect` (CR 716.3); `SpendAnyManaTypeToCastEffect`, `SpendManaAsAnyColorEffect`,
  `SpendManaAsAnyColorForActivatedAbilitiesEffect`.
- `PlayerRestrictionEffect(players, PlayerRestriction, duration)` (`PlayerRestriction.castingSpells()`, `castingNoncreatureSpells()`,
  `castingSpellsMatching`, `playingCardsFrom`, `activatingAbilities()` / `activatingAbilitiesOf(types…)`), stored in
  `GameData.restrictionsOnPlayer`; static locks `PlayersCantCastSpellsFromZonesEffect`, `OpponentsCantCastOrActivateDuringYourTurnEffect`,
  `OpponentsCantCastSpellsMatchingPredicateEffect`, `CantSearchLibrariesEffect`, `PlayersCantPayLifeEffect`; readers
  `GameQueryService.isPlayerRestricted`, `findRestriction`, `abilityLockRefusal`, `isHeldToSorceryTiming`; conditions `StackIsEmpty`,
  `TargetPlayerCouldCastSorcery` (CR 307.5), `TargetPlayerMatches(PlayerPredicate)`.
- `GrantStaticEffectToPlayerEffect` (`untilEndOfTurn`, `untilYourNextTurn`, `untilPlaneswalk`, `PERMANENT`, `WHILE_SOURCE_REMAINS`:
  nothing begins if the source left, CR 611.2b): a resolution's "until …, you [rule]" floats the rule's static record on the player, its
  reader asking for the floating copy beside the battlefield (`hasFloatingEffectOnPlayer`). Floated rules include the damage doublers and
  plus-N, `SkipDrawStepEffect`, `PlayWithHandsRevealedEffect`, `ProtectionFromPlayersEffect`, `IgnoreOpponentHexproofEffect`,
  `PlayerHasProtectionEffect`, `EnterPermanentsOfTypesTappedEffect`, `ReplaceDamageAboveThresholdEffect`, the library-top permissions
  (`CastingPermissionService.libraryTopGrants`).
- Next-spell riders: `GrantFlashToNextMatchingSpellEffect`, `GrantFreeCastForNextMatchingSpellEffect`, `EmpowerNextCreatureSpellThisTurnEffect`,
  `GrantAbilityToNextSpellThisTurnEffect`, judged by `DurationClock.waitsForSpell`.
- Perpetual verbs over card subjects, each with one store keyed by the physical card id: `PerpetuallyGrantAbilitiesEffect`
  (`perpetualAbilityGrants`; a graveyard ability with `AbilityZones.GRAVEYARD`, never seeded onto a permanent), `PerpetuallyChangeCastCostEffect`
  / `PerpetualCardCastCostSupport`, `PerpetuallyBoostCardEffect.toCards`, `PerpetuallyGrantAlternateCastEffect` (`perpetualAlternateCasts`),
  `PerpetuallyGrantAnyColorManaToCastEffect`, `GameData.grantPerpetualKeywords`; `GrantGraveyardAbilityToCardsEffect`, `GrantSupertypeEffect`.

**Damage, prevention, redirection:**

- Player damage is `DealDamageEffect.to(PlayerSubject, …)` (run Damage P; Typhoon's "equal to the number of [X] that player controls" is
  `eachEvaluatedFor(PermanentCount(filter, CONTROLLER), eachOpponent())`); `TargetCreatureDealsDamageToControllerEffect.dealtBy`, `TargetCreatureDealsPowerDamageToEachOtherCreatureEffect`,
  `TargetDealsPowerDamageToTargetEffect`, `SourceFightsTargetCreatureEffect` (`FightSupport.fight`).
- `PreventDamageToRecipientsEffect` (static prevention: `to(...)`, `toYouAnd(...)`, `toEveryone()`, `fromSpells()`,
  `fromInstantAndSorcerySpells()`, `fromSourcesOfChosenColor()`, `dealtBy(filter)`, `preventing(amount)`); `PreventNextDamageByTargetCreatureEffect`,
  `PreventDamageAndAddPlusCountersEffect` (CR 615.12), `DamageCantBePreventedEffect` (`GameQueryService.isDamagePreventable`).
- Redirects: `RedirectYourDamageToTargetCreatureThisTurnEffect.to(destination, RedirectedDamage)`, `RedirectNextDamageEffect.allThisTurn`,
  `RedirectNextDamageFromChosenSourceToSelfEffect` (one `SourceKeyedRedirectShield` per source, CR 614.9), `RedirectPlayerDamageToSelfEffect`.
- Multipliers and plus-N: `DoubleDamageFromCreaturesEffect.fromSourcesYouControl` / `.onlyDoubling(DamageScope)`,
  `DoubleDamageToOpponentsAndTheirPermanentsEffect`, `DoubleControllerDamageEffect`, `BoostControllerDamageThisTurnEffect`,
  `AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect`, read by `GameQueryService.getAdditionalDamageBonuses` (one option per
  instance); `GameQueryService.asDamageSourcePredicate` (Ghostly Flame, CR 609.7b).

**Counters:**

- `AddOnePlusOneCountersEffect` (every static counter-placement replacement; a placement naming no placer is put by the affected
  controller; CR 614.12) judged by `GameQueryService.placementReplacementApplies` / `placementAffects`; `GameQueryService.entersWithCardType`
  (CR 614.12); `ControlledPermanentsEnterWithAdditionalCountersByAmountEffect.ofEnteringPermanent`.
- Moves and removals among several: `MoveCounterFromSourceToTargetCreatureEffect` (`move`, `all`, `anyNumber`; CR 122.5 per kind;
  `PermanentCounterSupport.moveCounters`), `RemoveUpToCountersFromAllPermanentsEffect` (`anyNumber`, `exactly`, `matchingTarget`,
  `orFromPlayers`; one how-many prompt, `ChoiceContext.MoveCountersAmountChoice`), `RemoveAllCountersFromTargetPermanentEffect`
  (`fromPlayers`, `fromPermanentsAndPlayers`), `RemoveCountersFromExiledCardsEffect`, `DoubleCountersOnTargetPermanentEffect`,
  `AddAnotherCounterOfChosenTypeToTargetEffect`, `AdjustChosenCounterOnTargetEffect`.
- Stores: `GameData.exiledCardCounters` (every kind but time is forgotten as the card leaves exile; `GameData.isSuspended`, CR 702.62b),
  `PlayerCounterType` + `GameData.playerCounterCount` / `removePlayerCounters`; `CantGetCountersEffect`, `CantHaveCountersEffect`.

**Copies, tokens, conjures:**

- `BecomeCopyOfSubjectEffect` (`of(target())`, card targets, `.by(copier)`, `.lasting(...)`, `.except(CopyExceptions)`: copiable-value
  exceptions, CR 707.9a, 707.9b, 707.9d, 205.1a, 205.3d); `PermanentCopierService.copiableCard` / `lastKnownCopiableCard` (a departed
  permanent's copiable values from its departure snapshot, CR 707.2, 608.2h); `CopyCreatureCardInGraveyardOnEnterEffect`.
- `CreateTokenCopyOfTargetPermanentEffect` (`forController(PlayerSubject)`, CR 111.2; `withSubject(departedThisWay(kind))`),
  `CreateTokenCopyOfCardEffect.of`, `AddTokenCreationEffect` and `TokenCreationReplacementSupport.withEventModifiers` (CR 506.3);
  `CreateTokenEffect` investigate N times is N events (CR 701.16a).
- `CopyTriggeringSpellEffect` (`copiedBy`, `mayBeCopiedBy`, `targetingEach`), `CopyTargetActivatedOrTriggeredAbilityEffect`,
  `CopyControllerActivatedAbilityEffectHandler.copy` (the one ability copier), `RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn`.
- `ConjureCardsEffect` + `ConjureDestination` (`atRandomFrom`, `duplicateOf`, `named`, `intoHand()`, `intoLibrary()`, `.into(players)`,
  `.conjuredBy(players)`, `.times`); `DraftCardFromSpellbookEffect` (`.times(n)`); `ConjureDuplicateOntoBattlefieldEffect`.

**Delayed and global triggers:**

- `RegisterDelayedStepTriggerEffect` (`atNext`, `atNextMainPhase`, `aboutSourceAtNext`, `aboutCardAtNext` (`.ofThatPlayer`),
  `aboutAtNext(step, Subjects.target(), effects…)` (the registering object declares the harmful target), `aboutCreated`,
  `aboutPutOntoBattlefield`); `RegisterDelayedEventTriggerEffect` (`aboutWhenSourceLeavesThisTurn`, `whenLeavesThisTurn`, ...);
  `RegisterGlobalTriggeredAbilityEffect` with a `Duration`, `.nextThisTurn(ability)` (used up once, CR 603.7b);
  `ScheduleCreatedPermanentsEffect`, `SacrificeCreatedPermanentsAtEndStepEffect`; `AiUtils.withEffectsDelayedAboutTheTarget`.

**Combat, types, colours, protection, mana:**

- Combat: `MustBlockEachCombatEffect`, `MustBeBlockedIfAbleEffect`, `MatchingCreaturesMustAttackThisTurnEffect` (`attacking`, `lasting`,
  `controlledBy`), `CantAttackThisTurnEffect`, `CantBlockThisTurnEffect(subject, Duration)`, `CantAttackOrBlockAloneEffect`,
  `CantAttackCardOwnerEffect`, `CreaturesCantAttackControllerUntilNextTurnEffect`, `OnlyMatchingCreaturesCanAttackThisCombatEffect`,
  `MakeTargetAttackingCreatureBlockedEffect` (CR 509.1h), `CanBlockCreaturesWithShadowEffect`, `CantBeBlockedIfDefenderControlsMatchingPermanentEffect.whileDefendingPlayer`
  (CR 508.5, 509.1b), `AttachedPermanentRestrictionEffect`, `ExploreEffect` (CR 701.44d). Run S: `CantAttackOrBlockUnlessEffect.attack` / `block` /
  `attackOrBlock(condition, text)` (CR 508.1c, 509.1b), `CreaturesCantAttackUnlessPredicateEffect.youControl(exemption)`, `EnchantedPermanentCantActivateAbilitiesEffect.includingManaAbilities()` (CR 602.5).
  Run G: `GoadTargetCreatureUntilNextTurnEffect.eachCreature(filter)` / `creaturesControlledBy(player)` / `creaturesTargetPlayerControls()` (a
  `controlledBy(targetPlayer(), …)` subject declares the harmful player target; the set found once, CR 608.2h; CR 701.15d).
- Types, colours, names: `SetCardTypesEffect`, `LoseAllLandTypesEffect`, `GrantSubtypeEffect.chosenSubtype` (CR 305.7),
  `GrantBasicLandTypeToTargetEffect.of` (CR 611.2c), `LosesAllAbilitiesEffect.exceptManaAbilities`, `AnimateNoncreatureArtifactsEffect`
  (CR 613.6), `SetBasePowerToughnessEffect` / `SetBasePowerToughnessToAmountEffect` (keep negative toughness: `SourceToughness(true)`,
  CR 107.1b), `BecomeChosenColorsUntilEndOfTurnEffect`, `GrantColorEffect.chosenColor`, `SetChosenColorUntilEndOfTurnEffect`,
  `SetNameEffect`.
- Protection and hexproof: `ProtectionFromColorsEffect.ColorSet` + `GameQueryService.protectionColors`, `ProtectionFromManaValueEffect`,
  `ProtectionFromMulticoloredEffect`, `ProtectionFromPermanentsMatchingEffect`, `PlayerHasProtectionEffect` +
  `GameQueryService.playerProtectionFrom` (CR 702.16k), `GrantControllerAndPermanentsHexproofFromColorsEffect`, `IgnoreOpponentHexproofEffect`.
- Mana: `ReplaceLandManaWithColorEffect` (`GameQueryService.landManaColorReplacements`), `AddManaOfTypeProducedByTappedPermanentEffect`,
  `AwardPersistentManaEffect`, `AwardAnyColorManaEffect(DynamicAmount)`, `AwardManaToActivePlayerEffect.to`, `ArtifactManaSpentToCast`.
- Players and turns: `SkipDrawStepEffect(SkipStepOrPhaseKind, PlayerSubject)`, `SkipDrawsEffect`, `SetControllerMaximumHandSizeEffect`,
  `AdditionalUpkeepStepEffect`, `ControllerLosesGameEffect` over several players (CR 104.4a, 104.1), `LossReplacer` (CR 616.1),
  `GrantControllerKeywordUntilEndOfTurnEffect`, `IgnoreLegendRuleEffect`; player predicates `PlayerHasMostLifePredicate`,
  `PlayerHasLeastLifePredicate`, `PlayerLifeExactlyPredicate`, `PlayerControlsMostCreaturesPredicate`, `PlayerWithoutMaxSpeedPredicate`
  (CR 702.179e).

### Blocked causes still open

Counts are ledger rows of existing classes whose latest note names the cause (approximate, by keyword).

| Cause | Rows | What would unblock it |
|---|---|---|
| First half prompts mid-step (`blocked-step-model`) | 140 | moving the handler onto the step model (`leave-battlefield-redesign.md` step 14) |
| Each player chooses, then all act (`blocked-player-loop`) | 97 | a choose-then-act loop (not in this plan) |
| Cost combinators, "unless", cost heads | fewer than 85 (about 85 before run U1) | step 10's "unless" run U7 and a "pay X at resolution" run (Step-10 hand-offs) |
| A delayed trigger shape (end-of-combat, coin flip, the old delayed mechanism) | about 20 | the delayed-mechanism unification onto `RegisterDelayedStepTriggerEffect` / `RegisterDelayedEventTriggerEffect` |
| Planar deck cards | 9 | a card subject for the planar deck with its reveal, bottom-order and planeswalk steps |
| Next-draw replacement payloads (`DrawService`'s inline payloads) | about 20 | a "this draw was replaced" record in `DrawService` |
| Owner's questions (gift opponent) | 2 | the owner's decisions below |
| A token controller on `CreateTokenEffect` | 5 | a `PlayerSubject` token controller with the Esix, investigate and attack paths agreeing; the token copies have one (`forController`), the `CreateTokenFor…` records remain |
| Out-of-battlefield characteristics (the all-zones grants) | 5 | a redesign of the out-of-battlefield characteristic queries (outside this plan) |
| One or two classes each | most of the other blocked-other rows | a third user; each note names the part |

### The owner's decisions

- **2026-11-20, card subjects** (8.23): a third subject type, `CardSubject`.
- **Face-down token copies** (run 19 left it to the owner; settled before run 20): token copies read copiable values, face-down
  values included.
- **Run 21's brief**: go-ahead for the static prevention record (built as `PreventDamageToRecipientsEffect`).
- **2026-11-23, 3+-card folds** (run 38): folding records used by three or more cards is approved where the shared record says the
  same thing for every card.
- **May's 11 cost combinators set aside for step 10** (run 25's verdict, not an owner's decision; on 2026-11-23 the owner approved step 10's
  merges as long as they logically make sense, each verb's plan described as it starts): `MayPayLandDropEffect`, `MayPayTapAndSacrificePermanentEffect`,
  `MayPayLifeAndCreateTokenEqualToAmountEffect`, `MayPayLifeAndDrawEqualToAmountEffect`,
  `MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect`, `MaySacrificePermanentForCounterOrTapSourceEffect`,
  `MaySacrificePermanentForCounterOrRemoveSourceCounterEffect`, `MayReturnPermanentToHandAndEnterWithCountersEffect`,
  `MayHaveThisLandEnterTappedForRadCountersEffect`, `MayPayForArtifactsOrCreaturesCantAttackThisCombatEffect`, and
  `MayPayLifeForColorlessManaUntilEndOfTurnEffect` (Channel, a timed special action listed with them).
- **2026-11-23, triage**: step 9 switches to triage runs (verdicts only, about 80 records each), then fold runs.

- **2026-11-23, hidden-zone logging**: the public log says only what the card's text already makes public. A conjure of a named
  card may name it ("conjures a Grizzly Bears into their hand"); a random pick (a random spellbook card, a random library card) and a
  drafted card are logged as "a card", and a perpetual change to cards in a hand or library is one line for the instruction, with no
  count and no per-card lines. Where the log can address one player, the owner also gets the details; otherwise the owner's hand shows
  the card. This unblocks the 73 rows that waited on the question: re-verdict them as fold candidates, and fix today's leaks
  (`DraftCardIntoHandEffect` naming the drafted card, Hydroponics Architect naming its random library card, the per-card perpetual
  hand lines) in a defect run with failing-first tests. Done in defect run D10: the leaks fixed, the rule under Conventions, the 73 rows
  re-verdicted (71 `step-9` in fold runs 12, 12a, 12b and 20; one survivor; Territorial Strike's `blocked-other` for a beheld-object subject).
- **2026-11-25, the perpetual "it"** (option B): a change an ability makes to "it", "this card" or "that card" (perpetual
  changes and cast permissions alike) reaches the card only while it is still the object the ability refers to (CR 400.7);
  the CR's own exceptions stand (a zone-change trigger finds the card in the zone it went to, CR 400.7e, 603.6c; a card the
  same resolution moved, CR 400.7j). Card subjects: `Subjects.cardsOf(...)`, `Subjects.sourceObjectCard()`. Done in defect
  run D23.
- **2026-11-25, "counter unless" and the step-10 order**: option C, built as a generic "unless" record (CR 118.12a: payer +
  ransom + inner effect, the counter one inner effect among others: Cleansing, Cut the Tethers, "destroy unless takes damage"),
  which also covers the "may pay; if they don't" form (`ForcedCostOrElseEffect`, `MayPayManaEffect` and the cost combinators),
  so one construction has one record. The payer varies (its controller, that player, any player, you); a ransom can be
  alternatives (Lim-Dûl's Hex: {B} or {3}; discard or pay); the ransom is a cost (CR 118.12), ideally the activated-ability cost
  type if it can be paid by another player at resolution. `CounterUnlessPaysEffect`'s 308 cards move to a static factory by
  script. Before that run: check whether each ransom handler discards, sacrifices and pays life through the shared action code
  (madness, discard / sacrifice / life-paid triggers), and pin today's behaviour (flashback, ward countering an ability, one
  test per ransom kind). Order: Counters, then Damage, then "unless"; Boost where it fits. The gift opponent stays deferred.
- **2026-11-25, the "unless" runs** (the owner's answers to the "unless" plan): (1) the record is `UnlessPaysEffect`
  (CR 118.12a: "[do X] unless [P pays]" means "[P] may pay; if they don't, [do X]"); (2) a payer is asked even when the
  inner effect can do nothing (a spell that can't be countered): the payment can still be made; (3) run U1 first (the record,
  `CounterUnlessPaysEffect` and `CounterUnlessPaysLifeEffect`), the ransom a cost (`CostEffect`), not a step (paying life
  isn't losing life, CR 119.4, 119.8). **Asking a payer who can't pay** (D17 L34, for run U3): no question only when the
  payer has no way at all to pay, with anything, cards in every zone included; where the engine can't be sure of that, it
  asks. **"Any player may pay"**: every player is asked in turn order from the active player, even after one paid (CR 101.4;
  Rhystic Shield's and Nakaya Shade's rulings: "each player gets the option to pay"); Icy Prison's ruling doesn't say
  otherwise, so `ForcedCostOrElseEffect` stopping at the first payer is a defect for run U4.

  **U3** (the owner's answers to the U3 plan, 2026-11-25): (1) "could possibly pay" is strict: no question only when it is
  certain the payer can't pay (life only they can't pay; mana with a short pool and no mana source in any zone and no permission;
  mana and life as the plan's case 3; the other costs' own checks), no "tier B" estimate of what untapped lands make, so a payer with
  too few lands is asked (U1 / U2's paths too); (2) the prompt reads the ransom ("Pay {1}?"), with an optional custom prompt kept
  only where it says more; (3) the split U3a, then U3b', then U3c; (4) a payer subject naming several players means each of them may
  pay, asked in APNAP order, every one even after another paid (CR 101.4), no `Subjects.anyOf(...)` (U3b').

  **U3b'** (the owner's answers to the U3b' plan, 2026-11-25): (1) a non-mana part of an `AllOfCost` (short hand, graveyard or
  battlefield) proves "certainly can't pay" only when the payer has no mana source at all, or the cost has no mana part; otherwise the
  payer is asked (Lotus Petal, Black Lotus, Lion's Eye Diamond, Chromatic Star go to the graveyard; Chromatic Sphere draws); (2) single
  and several payers share one path, `players(payer)` always; (3) Apathy's random discard is caused by the Aura's controller when that
  player is the payer's opponent (D25's rule; Apathy's and Obstinate Baloth's / Wilt-Leaf Liege's rulings agree).

  **U2** (the owner's answers to the U2 plan, 2026-11-25): (1) `AnyOfCost(List<CostEffect>)`, one yes/no question per alternative the
  payer can pay, in Oracle order, an unpayable one not offered (CR 118.3, 608.2d); only Titania uses it now, U3b may absorb the `XOrY…Cost`
  records; (2) Whirlwind Denial keeps its record (APNAP choices, then one counter, CR 101.4), only its payment moves onto the service; Ice Cave
  goes to U3; (3) the paid answer lives on the stack entry, not in `resolvedMayAccepted`, for U1's path too.

  **U3c** (orchestrator's decisions for U3c, owner to review): (1) the four PLC "counter it" cards (Brain Gorgers, Dash Hopes,
  Phantasmagorian, Temporal Extortion) use the plain loop, paid as each player chooses and countered after the last answer: no player gets
  priority meanwhile and triggers wait (CR 603.3), so "as soon as" can't be told apart; (2) Development draws at most one card per round,
  every opponent still asked (its 2006 rulings; owner to review, defect queue); (3) its draw is the new `SourceControllerDrawsCost`; (4) the
  hard AI subtracts an estimate of a non-mana ransom and never pays life or damage at or above its life total; (5) the sacrifice-candidate
  guards (Sigarda, "can't be sacrificed", The Master's tokens) are fixed in U3c, U2's ward included.

  **U4** (orchestrator's decisions for U4 (owner to review)): (1) split U4a to U4d, the record and its handlers go at the end of
  U4d; (2) the mandatory "do X. If you can't, Y" forms are steps plus a conditional, not `UnlessPaysEffect` (CR 118.12); (3) "that
  player" with no "target" (Tomb Blade, Pillar Tombs of Aku) is the event's player, not a declared target (CR 115.1); (4) Satya (U4b):
  its mana value read as the delayed ability resolves, from last-known information once the token left (CR 608.2h), the payer still
  asked; (5) Fraying Line's exile is one event (CR 608.2c, 603.10a); (6) the hard AI keeps scoring a source-sacrifice branch as nothing
  in U4 (parity), a defect-queue line to score it as the source's value; (7) Balduvian Fallen in U4d, Flash in U4b reduced by the full
  {2}; (8) U4a's defects fixed failing-first: "any player may pay" asks every player even after one paid (Icy Prison, Nakaya Shade,
  Rhystic Circle, Rhystic Shield; CR 101.4, their rulings), an upkeep mana payer with no mana source isn't asked (owner's strict rule).

  **U3b** (orchestrator's decisions for U3b, owner to review): (Q3) Spelltithe Enforcer asks only the opponent who cast the spell ("that
  player"); (Q4) at an amount of 0, Tymna and Madame Null keep not asking; (Q5) the special-action delayed payments (Quenchable Fire, the
  Asps, Sabertooth Cobra) stay as they are, a defect-queue line for the owner (no tax-paid event; it changes play only with Tax Taker);
  (Q1, Q2) the per-payer branch and the Lurking Spinecrawler, Read the Runes and The Dalek Emperor fixes are U3b2's; (Q6) Decoy Gambit is
  U6's; (Q7) the `XOrY…Cost` fold is run U7.

  **U7** (orchestrator's decisions for U7 (owner to review)): (Q1, Q2) the client stays unchanged: the free-cast options keep today's
  order (discard first for Bone Shards, Demand Answers, Minion Missile) and "P pays {m} for C." stays where it is logged; (Q3) a human
  client chooser is the owner's missing feature (defect queue, with the hand-cast discard gap); (Q4) the mana alternative is paid with
  the total cost (CR 601.2f); (Q5) Redirect Lightning stays on `PayLifeOrPayManaCost`; (Q6) the chooser after targets is a defect-queue
  line; (Q7) the split U7a / U7b / U7c; the engine's free-cast chooser for every shape and the exile / library-top request fields are in
  scope.

- **2026-11-26, the rest of step 10: orchestrator's decisions (owner to review)** (the plan `s10-rest`, its section 6): (Q1) run S's
  survivor is `CantAttackOrBlockUnlessEffect` with `attack` / `block` / `attackOrBlock` factories; (Q2) run D fixes every player's
  amount before anyone draws (CR 608.2h); (Q3) run D's AI own-draw rule is in scope and logged; (Q4) R2's "this card" is
  `sourceObjectCard()` (the owner's "it" decision 2026-11-25); (Q5) run T checks Esix's and Mirrormind Crown's rulings and stops if they
  don't settle the offer; (Q6) `MakeAllCreaturesUnblockableEffect` stays out (CR 611.2c, Venser's ruling); `ExileGraveyardCardsEffect.eventValueFilter`
  stays (7 users); the log line of the survivor wins. Run order: A, X, S, G, D, P / O / U, C1, C2-pre and C2, D-Destroy and K / K2, then
  R2, J, T, U8 after their prerequisites.
- **2026-11-26, step 10 run Boost: orchestrator's decisions (owner to review)**: (Q1) the survivor is `BoostEffect` (handler
  `BoostEffectHandler`), every call site on factories; (Q2) an explicit `AmountsRead {SOURCE, BOOSTED}` set by the factory; (Q3) a target or
  set boost refreshes the boosted creature's waiting abilities' snapshots too (CR 608.2h, 113.7a); (Q4) today's log lines kept; (Q5)
  `CreatureBoostEffect.targetedBoost` by `declaresTarget()`, AI-only; (Q6, Q7, Q8) parity, one defect-queue line each; (Q9) split B1 / B2.
- **2026-11-26, step 10 runs Damage P to R: orchestrator's decisions (owner to review)**: (Q1) the player kinds leave `subject` null, so a
  misuse fails loudly; (Q2) Curse of Thirst is `eachEvaluatedFor(PermanentCount(allOf(Curse, PermanentAttachedToSourceControllerPredicate)),
  triggeringPlayer())`, pinned first; (Q3) the player handler's prevention checks stay before the pool in P (parity) and move in the defect
  run before M1, failing-first (CR 616.1); (Q4) Typhoon's record is absorbed in P; (Q5) the excess redirect waits for R; (Q6) Aegis of
  Honor's redirect applied before the player pool in `resolveAnyTargetDamage` is a defect-queue line; the engine's one-damage-event-per-effect
  rule is cited by no CR number. Run order: P, a defect run (the set handlers' protection-before-pool checks, the missing DEAL_DAMAGE steps,
  face-down names in logs), M1, M2, R.
- **2026-11-25, how the rest of the refactor runs** (the owner, after run U3b'): the orchestrator answers the planners' questions itself from
  the Comprehensive Rules, the card rulings and the owner's earlier decisions, and logs each answer here as an orchestrator decision for the
  owner to review; there is no per-run sign-off. It stops only for a question that changes play and that the rules and rulings don't settle,
  a player-facing change beyond what the rules require, a run growing past its plan, or a failure it can't explain. The owner watches the
  full suite on CI and reports failures; the targeted sweep before each commit stays, and the full suite is still never run locally.
- **2026-11-25, step 10 run Damage** (the owner's go-ahead, with these answers): (1) one run, all five one-recipient records (any
  target, creature, creature or planeswalker, player or planeswalker, permanent) with one permanent-damage path (D24 answered the plan's
  question 5); the target loop is one rule only if every kind's own targets give the same result, else per kind with the reason. (2) The
  survivor is renamed `DealDamageEffect` (handler `DealDamageEffectHandler`), every call site on factories and chained riders. (3) Pin all
  triggering-dealer cards, then route every kind through `DamageSupport.entryDealtByTriggeringPermanent`; a pin that changes is decided
  by the rule (CR 608.2h: the object as it exists or most recently existed deals it), with a failing-first test. (4) One excess detection
  list (the union, guarded by "the amount doesn't read the event value" and "not `recordDamageDealt`") and one formula for every kind
  (CR 120.4a, 120.10), deathtouch the dealer's; the excess-risk files checked by hand and by their tests.
- **2026-11-25, step 10 run Remove counters** (the plan's four questions): the survivor `RemoveCounterFromSourceEffect` is renamed
  `RemoveCountersEffect` (handler `RemoveCountersEffectHandler`) with `fromSelf` / `fromSelfCountingTargetPlayer` / `fromTarget`
  factories, all 128 card files moved to them; the 4 mass cards come in as `controlledBy(...)` subjects (their summary log goes); Gremlin
  Mine moves to `RemoveUpToCountersFromAllPermanentsEffect(CHARGE, 4, target())`; AI polarity of a removal from a target is by kind
  (-1/-1 beneficial, any other harmful, the `RemoveUpTo…` rule), NEUTRAL only for a kind picked at resolution (`ANY`).
- **2026-11-25, step 10 run Counters**: the survivor is renamed `PutCountersEffect` (step 10 survivors may take a neutral verb
  name), its 1,137 cards keep their constructors; every `source()` counter `MayEffect` follows the source rule (CR 608.2d; Kresh still
  asks); the 5-card target-player mass record waits for a mass-counters run; remove counters is the next run.

### The owner's open questions

- **The gift opponent** (deferred by the owner 2026-11-25; CR 702.174a, `missing-features-to-do.md` entry 80): no gift opponent is recorded; every gift handler
  (`TargetOpponentCreatesTokenEffect`, `ConjureCardToOpponentBattlefieldEffect`, `EachOtherPlayerDrawsCardEffect`, ...) gives it to
  `getOpponentId`, every other player, or the spell's target, right with two players only; the gifts of Cruelclaw's Heist, Consumed by
  Greed and Perch Protection never raise the gift-given event (CR 702.174c), so Jolly Gerbils misses them. 2 rows wait for it
  (`EachOtherPlayerDrawsCardEffect`, `TargetOpponentCreatesTokenEffect`).
- **Face-down board views** (`missing-features-to-do.md` entry 81): a player can't tell their own face-down permanents apart on the
  board or in id-only prompts; needs a per-viewer permanent view and client work. No ledger row waits for it.

### Fold and defect queues

The fold queue, the defect queue and the re-triage list are in `effect-cleanup-queues.md`. The fold queue is closed (no class's latest
ledger row is `step-9`). The defect queue has 216 lines not struck through, each with what it needs; a defect run reads its own lines
there and updates them when it lands.

### Step-10 hand-offs

- **"Unless"** (owner's decisions 2026-11-25; the plan in the U1 Findings): `UnlessPaysEffect` covers the counter and retarget ransoms, every
  single-payer mana / life "may pay", the several-payer loop, `AllOfCost`, `ExileCardsCost`, Ice Cave, the Chain family and the tap and
  mana-and-sacrifice payments (runs U1 to U3b'), and the any-player non-mana payments with `.payingTogether()` (U3c; `MayPayManaEffect`
  is gone), and 25 single-verb records with existing factories (U3b1), and 15 with the per-payer branch `eachUnless`, the payer-declared target and four new ransoms (U3b2; Lim-Dûl's Hex's card too; Lurking Spinecrawler, Read the Runes and The Dalek Emperor fixed). U6 built `UnlessPaysEffect.forEach` (Cut the Tethers, Cleansing, Giant Albatross). U6b moved Sirocco and Sword-Point Diplomacy (`forEachCard`) and Ob Nixilis and Professor Onyx (a discard paid together), U6c put The Long Reach of Night and Possessed Portal on that discard; left on their records, each a one-card part (U6b's Findings): Stench of Evil with `EachPlayerTakesDamageUnlessPaysEffect` (departed objects, damage summed per player), Heroism (combat-damage prevention over a subject), Tidal Flats (a "blocking that creature" subject), Tariff (a ransom read from the object), Decoy Gambit (target objects, a draw per payer before the returns, and its can't-draw question for the owner); Fade Away and Killing Wave stay (a subset chosen and paid as one event, their rulings);
  **U7** (done), the `XOrY…Cost` records as a cast-time `AnyOfCost`: U7a the four non-mana ones, U7b the five mana ones (16 card
  files), U7c `ForageCost` (CR 701.61a, Feed the Cycle an any-of); `PayLifeOrPayManaCost` stays (Redirect Lightning, the Defiler units);
  the client chooser is the owner's missing feature (defect queue). Left of the "unless" family beside U7: Decoy Gambit, Stench of Evil, Heroism,
  Tidal Flats, Tariff, Whirlwind Denial, the `PayXMana…` records, Enchanter's Bane / Star Athlete (below);
  U4a moved `ForcedCostOrElseEffect`'s 121 card files on paid ransoms, the mandatory "if you can't" files (steps) and Midnight Crusader
  Shuttle (a villainous choice); U4b the other 38 card files (nine new ransom cases, Egon / Charnel Troll / Greenbelt Rampager as steps,
  Ogre Marauder's granted ability), the Satya, Ashling and Flash builders and Koya's split, so no card file names the record; left
  beside it: Confiscation Coup (`UnlessPaysEffect.youMayPay(new PayEnergyCost(new TargetManaValue()), …)` now expresses it) and Behemoth
  of Vault 0 (its "when you do, destroy that permanent" is about the declared target, which `QueueReflexiveAbilityEffect` would choose
  again), and `ReturnMultiplePermanentsToHandCost`'s 26 activation-cost cards (fold into `ReturnPermanentToHandCost.count` with the
  activation path); U4c moved echo (`KeywordTriggers.echo` builds `UnlessPaysEffect` with `PayEchoCost`); U4d moved cumulative upkeep (84 cards, eight new ransom cases, `AmountThisWay.manaPaid(colours)` for Balduvian Fallen) and deleted the record, both handlers, `MayPenaltyChoiceHandlerService`'s branch and `DestructionSupport.resolveForcedCostElseEffects`; U5 moved the damage ransoms (Combustion Man, Dwarven Driller and Scorcher, Craig Boone, Tibalt, Pia's Revolution,
  Worms of the Earth's any-of ransom; Blazing Salvo, Lava Blister, Skullscorch and Elektra off `MayEffect`); left beside it: the "pay X at
  resolution" records (`PayXManaDealXDamageToAnyTargetEffect` and the five other `PayXMana…`: an {X} chosen during the payment, a ransom
  kind of its own), `TargetPermanentControllerMaySacrificeOrDamageEffect` (Enchanter's Bane's damage is dealt by the target: the dealer
  run; Star Athlete with it) and `SacrificeOtherCreatureOrDamageEffect` (Unnatural Hunger's sacrifice ransom, the others "if you can't" steps). Whirlwind Denial stays its own record (owner's decision U2; its
  payment is on the service). `PendingMayAbility.tapPermanentsCost` is null everywhere (a component to drop with the record's next change).
- **Cost combinators beside `MayEffect`** (CR 118.12: their "if you do" is the choice to pay): the old forced-cost record is gone (U4d);
  May's 11 (owner's decisions; six went in U3b1, `MayPayLandDropEffect` and Overencumbered's in U3b2),
  `TargetPermanentControllerMaySacrificeOrDamageEffect` (Star Athlete, Enchanter's Bane), the cost pairs `Sacrifice*Cost`, `Exile*Cost`,
  `Return*ToHandCost`, `PutCounterOn*Cost`.
- **Static restrictions**: run S merged the condition trio (`CantAttackOrBlockUnlessEffect` factories), the can't-activate pair and the two
  `…CantAttackUnlessPredicateEffect` records. Left: `CreaturesCantAttackControllerUnlessPredicateEffect` (whom a creature may attack, also
  floated: stays out), the unblockable subject (run U), `MustBlockTargetCreatureEffect` (stays out). Goad: run G folded the two set records;
  `GoadCreaturesUntilNextTurnEffect` (a rule that covers later creatures, Kardur's and Maximum Carnage's rulings) and the static
  `GoadEquippedCreatureEffect` stay out.
- **Boost**: B1 and B2 done (`BoostEffect`; the mass records are `controlledBy` subjects). Stay out (more than the subject): the
  `StaticBoostEffect` family (per-creature amounts, a layered set, CR 611.3a), the while-tapped record, the chosen-type / Hand of Vecna
  records (a choice at resolution), the per-creature and other-target amounts (`BoostOpponentCreaturesByPoisonCountersEffect`,
  `BoostOwnCreaturesAttackingPlayersByDefendingPlayerCreatureCountEffect`: a follow-on fold once `BOOSTED` reads over a set), the
  composites (they hold a `BoostEffect`), `BoostControllerDamageThisTurnEffect` (not a boost).
- **Counters**: left the remove-all records (`RemoveAllCountersEffect`, `RemoveAllCountersFromTargetPermanentEffect`, the matching /
  of-type / fusion forms) for a "remove all" run, and the mass placements (`PutCounterOnEachControlledPermanentEffect`,
  `PutCounterOnEachMatchingPermanentEffect`, `PutPlusOnePlusOneCounterOnEachCreatureTargetPlayerControlsEffect`), which need a batched
  path in `PutCountersEffect` first (owner's decision 2026-11-25).
- **Damage**: run Damage P folded the player record (`to(PlayerSubject)`); defect run D26 moved the player kinds' and the set handlers'
  pre-pool checks into the pools and begins their DEAL_DAMAGE steps; run M1 folded the four set records (`toEach`, `toEachTarget`); run
  M2 folded `MassDamageEffect` (Falling Star's tap a step; left out: `DealDamageToEachPlayerAndTheirCreaturesEqualToControlledCreatureCountEffect`,
  an amount per recipient's controller, and `DiscardRandomCardThenMassDamageEffect`, its own repeat loop). Next (orchestrator's decisions
  2026-11-26): run R folded the other-dealer records (`dealtBy`); run X the excess redirect with Ram Through (`excessToControllerInstead`); left out of R: fight (CR 701.14b), `EachCreatureDealsPowerDamageToItselfEffect` (each dealer to itself),
  the one-card dealer records (Mage Slayer, Aura Barbs, Grim Contest, Spinning Wheel Kick, the target land, the two enchanted-creature
  and the up-to-one source records: own chains, specs or markers) and Enchanter's Bane / Star Athlete (an "unless" ransom); divided damage (`DealDividedDamageEffect`, CR 601.2d); the cost combinators (`PayXManaDealXDamageToAnyTargetEffect`, the "unless
  takes damage" records) for the "unless" runs.
- **Destroy**: `DestroyEachTargetPermanentEffect` and `DestroyAllPermanentsEffect` (`sparing`, `readingTarget`).
- **Tokens and conjure**: `CreateTokenCopyOfSourceEffect`; the two multi-card battlefield conjure records
  (`ConjureCardToBattlefieldEffect`, 25 cards; `ConjureCardNamedOntoBattlefieldEffect`, 9).
- **Register**: `RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect` (Season of the Bold, Nuka-Nuke Launcher): its watched player is a
  permanent target's controller or the defending player, which the survivor can't take.
- **Other pairs**: subtype grant, choose-one-for-target, perpetual boost, the returns from a graveyard; the reveal heads of three or more
  cards; the graveyard-ability grant with the scavenge record; `GrantSupertypeEffect` with its 4-card enchanted twin; a copy verb with a
  copier subject (Mirrorform).
- **`ExileGraveyardCardsEffect.eventValueFilter` stays**: the earlier "no card left" note was false; 7 card files set it (rest-of-step-10 plan, orchestrator's decision 2026-11-26).
- **Player grants**: `GrantPlayerStaticEffectsUntilEndOfTurnEffect` (10 cards, Angel's Grace) is `GrantStaticEffectToPlayerEffect.untilEndOfTurn`
  over a list, plus its escape-grant snapshot; merge them.

## Findings

The run sections up to and including step 10 run U3b' are in git history: `git show ef0278a44e:effect-cleanup-refactor.md` (from
step 9 run 41, 2026-11-23, to 2026-11-25) and `git show 6728593a9a:effect-cleanup-refactor.md` (before 2026-11-23). Grep them for the
run's heading (`### Step 9, fold run 23c`) or a class or card name; don't read them whole. Triage runs 1 to 7 and the parallel triage
chunks had their Findings merged into `effect-cleanup-queues.md` (their text: `git show 51f0afd546:effect-cleanup-refactor.md`).

Each run adds its line to the list below and appends its section after the last kept one.

**Runs since the 2026-11-23 cut** (in commit order; the commit is the one that landed the run):

- Step 9, triage run 1 (2026-11-23, `16d0d5553a`): Pay and Reduce triaged, 32 stale step-4/5 blocked rows re-triaged, 88 fold candidates batched
- Step 9, triage run 2 (2026-11-23, `59e4e8f512`): Search and Cant triaged, 52 fold candidates batched, a SEARCH step proposed
- Step 9, triage run 3 (2026-11-23, `2293807ad4`): Sacrifice and Destroy triaged, 54 fold candidates batched, a delayed ability about the target proposed
- Step 9, defect run 1 (2026-11-23, `7ddb111060`): face-down reads, search records, Territorial Strike's beheld Dragon, Tidal Influence's cast condition
- Step 9, triage run 4 (2026-11-23, `7f67501916`): Look and Remove triaged, 53 fold candidates batched, one store for counters on exiled cards proposed
- Step 9, triage run 5 (2026-11-23, `76313a758e`): Add and Gain triaged, 49 fold candidates batched, static card subjects for the ability borrowers proposed
- Step 9, defect run 2 (2026-11-23, `8796a64774`): Fatal Lore's targets at cast, Moxite Refinery's cost
- Step 9, triage run 6 (2026-11-23, `db7a565bf1`): Become, Draw and Double triaged, 63 fold candidates batched, a copier subject and copy exceptions proposed
- Step 9, triage run 7 (2026-11-23, `941630b74d`): Set, Copy and Seek triaged, 69 fold candidates batched, one seek step recording SEEK proposed
- Step 9, parallel triage chunks 02 and 04 (2026-11-23, `e3c5d02ca8`): Boost, Opponent, Specialize, Attach, Replace, Additional, Any, Counter and Flip triaged
- Step 9, parallel triage chunk 01 (2026-11-23, `4457b7d143`): Allow, Cast and Deal triaged, 55 fold candidates batched, a free-cast choice over card subjects proposed
- Step 9, parallel triage chunk 03 (2026-11-23, `bfe3eb624f`): Shuffle, Enchanted, Protection and Redirect triaged, 59 fold candidates batched
- Step 9, parallel triage chunk 06 (2026-11-23, `546a19ede6`): Award, Exchange, Creatures, May, Damage, Ignore and Player triaged
- Step 9, parallel triage chunk 07 (2026-11-23, `4d3fe3f4a6`): Spend, Animate, Must, Roll, Move, Require, Source, Tempting Offer, Transform, Can and Controller triaged
- Step 9, parallel triage chunk 05 (2026-11-23, `cc2d6a59fe`): Increase, Make, Opponents, Players, Controlled and All triaged
- Step 9, parallel triage chunk 08 (2026-11-23, `ba83b08045`): Goad, Other, Play, Turn and 14 small families triaged, 55 fold candidates batched, a pile step proposed
- Step 9, parallel triage chunk 09 (2026-11-23, `7fd81e1c03`): Chosen through Transmute, 28 small families triaged, 58 fold candidates batched
- Step 9, parallel triage chunk 10 (2026-11-23, `c2514dd407`): Vote, Win, Airbend, Connive, Control, Life, Miracle, Paradigm, Reflect and 32 more small families triaged
- Step 9, parallel triage chunk 11 (2026-11-23, `7f58dc485c`): the Remember-to-Z and Abstract-to-Close tail triaged
- Step 9, parallel triage chunk 12 (2026-11-23, `37eaafe14e`): Coercive Portal to Guild Feud triaged, a VOTE step and a card-subject "except" proposed
- Step 9, parallel triage chunk 15 (2026-11-23, `c89e6a965c`): Unexpected to Zyym triaged, a two-dice step across the Endeavors
- Step 9, parallel triage chunk 16 (2026-11-23, `62dd174dda`): Perpetually and Conjure triaged, 60 rows held for the owner's open questions, a CONJURE step proposed
- Step 9, parallel triage chunk 13 (2026-11-23, `d38e6d80ee`): Harness through Phased, 85 small families triaged, 39 fold candidates batched
- Step 9, parallel triage chunk 14 (2026-11-23, `51f0afd546`): Phyrexian through Undying, 85 single-record families triaged, 33 fold candidates batched
- Step 9, triage consolidation (2026-11-23, `330070f072`): the triage merged into a fold queue and a defect queue (now `effect-cleanup-queues.md`), the owner's hidden-zone logging decision
- Step 9, defect run D10 (2026-11-23, `a43140f0d8`): hidden-zone logging rule, draft/conjure/seek/perpetual leaks fixed
- Step 9, fold run 1 (2026-11-23, `7ac421c9de`): 10 dead records deleted, 9 cost and library singles folded, Strong Back reconfigure fix, 7 rows blocked
- Step 9, fold run 2 (2026-11-23, `63e80d227e`): p-dmg, 23 singles folded (22 records gone), Wicked Guardian if-you-do fix, 9 rows left
- Step 9, fold run 3 (2026-11-23, `ab89cf9ccc`): p-perm, sac-singles, destroy-singles, 19 records folded, Master of Cruelties lost-abilities fix, 10 rows left
- Step 9, fold run 4 (2026-11-23, `93cd214f04`): gc, search, cant, shuffle, copy, base-pt, seek singles
- Step 9, fold run 5 (2026-11-23, `43f28175e8`): player-floated, life-lock, may-draw, random-player
- Step 9, fold run 6 (2026-11-23, `3ab6027d48`): delayed-created, asenters, random-pick, returned-animation
- Step 9, fold run 6b (2026-11-24, `ba28a22440`): chosen-animation, coin-params, any-colour, prot-perm
- Step 9, fold run 7 (2026-11-24, `c7f7204172`): scheme, drafts, random modes, villainous, parley
- Step 9, fold run 8 (2026-11-24, `10e0b00e2d`): splits (part), 9 records folded, Garruk damage, Void discard rules, 5 blocked, 25 rows left
- Step 9, fold run 9 (2026-11-24, `ce68b826f4`): mill-split, sac-gated, sac-or-else, look-reveal-then, 15 records folded, hidden-zone mill fix, 14 rows left
- Step 9, fold run 10 (2026-11-24, `6941f260ac`): if-instead, hand-reveal-dmg, manifest-hand, gy-batch
- Step 9, fold run 11 (2026-11-24, `9713d5f2c7`): VOTE step (VoteEffect, VoteResult, VotesFor), 13 records folded, open votes logged, 3 blocked, 4 left
- Step 9, fold run 9b (2026-11-24, `dd77555f82`): one-of-two, hand-choice-draw, draw-discard, 10 records folded
- Step 9, fold run 12 (2026-11-24, `8cb0f0c0de`): CONJURE step (ConjureCardsEffect), conjure-exile
- Step 9, fold run 11b (2026-11-24, `566be9aad7`): object votes (VoteEffect candidates, mostVoted/votedFor/mostVotedCards/playersWithNoVotes)
- Step 9, fold run 10b (2026-11-24, `ecd00952c7`): opp-card, 6 records folded, Burning-Rune Demon search recorded and opponent chosen
- Step 9, fold run 12a (2026-11-24, `23d40664c8`): hand and recipient on ConjureCardsEffect, conjure-named folded
- Step 9, fold run 14 (2026-11-24, `bc664c90c7`): TAP step (TapPermanentsEffect, PermanentCanBecomeTappedPredicate)
- Step 9, fold run 13 (2026-11-24, `9e58a0e801`): SEARCH step (StepKind.SEARCH, foundThisWay, searched)
- Step 9, fold run 12b (2026-11-24, `c001cd7590`): perpetual hand and library changes onto card subjects
- Step 9, fold run 14b (2026-11-24, `a024253d0a`): pile step (SeparateIntoPilesEffect, PendingPileStep)
- Step 9, fold run 13b (2026-11-24, `1da4551f13`): could-enchant and same-name search predicates
- Step 9, fold run 14c (2026-11-24, `b415d5aff9`): BID step (BidLifeEffect, StepKind.BID, one LifeBidChoice)
- Step 9, fold run 12c (2026-11-24, `ae24d8c390`): triggering-cards-in-hand and cards-you-own subjects
- Step 9, fold run 8b (2026-11-24, `b72e39b68c`): filtered playersThisWay (card and permanent forms)
- Step 9, fold run 16 (2026-11-24, `24faebf568`): one exiled-card counter store (23 stores merged)
- Step 9, fold run 15 (2026-11-24, `1b2afa0990`): attach verb (any attachment, set / step-result / player hosts)
- Step 9, fold run 12d (2026-11-24, `2c80988d22`): perpetual alternate-cast step and store, departed-this-way and non-declaring target subjects
- Step 9, fold run 17 (2026-11-24, `898ea33741`): copier subject and CopyExceptions on BecomeCopyOfSubjectEffect
- Step 9, fold run 16c (2026-11-24, `4b0fe70337`): static card subjects (namedCards, ExiledWithSource link)
- Step 9, fold run 18 (2026-11-24, `41f2bfa43a`): CastCardsWithoutPayingManaCostEffect cast-now step
- Step 9, fold run 16b (2026-11-24, `836295810c`): time counters into the exiled-card counter store
- Step 9, fold run 15b (2026-11-24, `5321936b46`): picked subject, inResolution library follow-up
- Step 9, fold run 17b (2026-11-24, `69df43cc82`): target card subjects, ofCreatureOrCard and name/legendary/P-T/type-line CopyExceptions on BecomeCopyOfSubjectEffect
- Step 9, fold run 19 (2026-11-24, `dc9606e6c5`): one Ring store with both tempt events, PersistReturnEffect.undying
- Step 9, fold run 18b (2026-11-24, `2e820afc6a`): CastCardFromGraveyardEffect.of(CardSubject) (source)
- Step 9, fold run 19b (2026-11-24, `1a34ab5109`): ChooseOneEffect opponent chooser announced on the stack
- Step 9, fold run 20 (2026-11-24, `822cd30fc0`): SEEK step on SeekLibraryEffect (graveyard/exile)
- Step 9, fold run 17c (2026-11-24, `d9307d51f6`): paidAsCost subject, CopyExceptions subtypes/colors/abilities
- Step 9, fold run 21 (2026-11-24, `183cf33c95`): shuffledIn and fromEachOwner, 16 records folded
- Step 9, fold run 12e (2026-11-24, `d892838e15`): the draft steps record CONJURE, .times(n), exiledFaceDown
- Step 9, fold run 19c (2026-11-24, `11b4afc875`): TemptingOfferEffect (all decide, then act), PlayerSubject token controller on CreateTokenCopyOfTargetPermanentEffect
- Step 9, fold run 22 (2026-11-24, `13015ca05c`): counter-placement replacements on AddOnePlusOneCountersEffect
- Step 9, fold run 17d (2026-11-24, `5c5936c0da`): nested as-attached resolution resumes first, per-card exile stays
- Step 9, fold run 18c (2026-11-24, `6ab1242d5d`): one LookAtTopCardsAndMayCastEffect (DynamicAmount count)
- Step 9, fold run 21b (2026-11-24, `ee8835e83d`): perm-shuffle, ShuffleTargetPermanentIntoLibraryEffect over a PermanentSubject recording SHUFFLE_INTO_LIBRARY
- Step 9, fold run 22b (2026-11-24, `29fb1762e1`): token additions on AddTokenCreationEffect (blueprints)
- Step 9, fold run 19d (2026-11-24, `11e144ad68`): offeredOnlyTo on TemptingOfferEffect (CR 608.2d)
- Step 9, fold run 21c (2026-11-24, `fb5f3f72bb`): zone sets on the and/or search survivors (outside the game)
- Step 9, fold run 23 (2026-11-24, `07e7985fc2`): one counter move (move/all/anyNumber over any subjects)
- Step 9, fold run 25 (2026-11-24, `02e2c8308c`): damage doublers onto DoubleDamageFromCreaturesEffect
- Step 9, fold run 28 (2026-11-24, `60b9701ac6`): delayed-target and next-time, aboutAtNext over target
- Step 9, fold run 31 (2026-11-24, `a1543cb015`): colour-setter subject/Duration/ColorChoice on BecomeChosenColorsUntilEndOfTurnEffect
- Step 9, fold run 30 (2026-11-24, `26a2b4df52`): library-top floating grants and life alternative on AllowCastFromTopOfLibraryEffect
- Step 9, fold run 26 (2026-11-24, `21198e64d2`): dealer subjects on TargetCreatureDealsDamageToControllerEffect
- Step 9, fold run 23b (2026-11-24, `04ebf2305b`): player counters onto RemoveAllCountersFromTargetPermanentEffect
- Step 9, fold run 25b (2026-11-24, `cab330ef61`): add-dmg onto AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect
- Step 9, fold run 28b (2026-11-24, `79c225fc1f`): value-binding and ability-copy, SubjectResolutionService.bindChosenValues
- Step 9, fold run 31b (2026-11-24, `2bd773bf75`): pt-amount and base-pt-new onto SetBasePowerToughnessToAmountEffect
- Step 9, fold run 29 (2026-11-24, `413b765fac`): copier PlayerSubject and targetingEach on CopyTriggeringSpellEffect
- Step 9, fold run 26b (2026-11-24, `12e00e7ce1`): TargetSpec.chosenAtRandom and targetPlayerChosenAtRandom
- Step 9, fold run 34 (2026-11-24, `b2157e5b8b`): static and floated cast/play/search/face-up/cost prohibitions over PlayerSubject + PlayerRestriction
- Step 9, fold run 23c (2026-11-24, `0b929980ec`): counters-among onto RemoveUpToCountersFromAllPermanentsEffect
- Step 9, fold run 33 (2026-11-24, `604affc599`): land-mana colour replacement onto ReplaceLandManaWithColorEffect
- Step 9, fix after fold run 26 (2026-11-24, `0252b264c6`): an Aura that left unattached after its creature left names nothing again
- Step 9, fold run 29b (2026-11-24, `ba4d464ea4`): card subjects in any public or revealed zone and castingUpTo/AnyNumber on CopyImprintedCardAndMayCastCopyEffect
- Step 9, fold run 27 (2026-11-24, `a419e1031c`): your-damage redirects onto RedirectYourDamageToTargetCreatureThisTurnEffect
- Step 9, fold run 32 (2026-11-24, `65a8c45bf2`): type statics onto SetCardTypesEffect / LoseAllLandTypesEffect
- Step 9, fold run 28c (2026-11-24, `17d0d674b7`): next-cast spell copies onto RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn + CopyTriggeringSpellEffect
- Step 9, fold run 34b (2026-11-24, `b27974c108`): static cast and activation locks onto OpponentsCantCastOrActivateDuringYourTurnEffect
- Step 9, fold run 23d (2026-11-24, `0f2f1f392b`): chosen-counters onto RemoveUpToCountersFromAllPermanentsEffect
- Step 9, fold run 35 (2026-11-24, `e647cc03df`): player keyword, step skips, hand size, hand reveals
- Step 9, fold run 33b (2026-11-24, `6436ab7b01`): produced-mana onto AddManaOfTypeProducedByTappedPermanentEffect
- Step 9, fold run 36 (2026-11-24, `0d24d8dd49`): combat restriction folds onto CantBlockThisTurnEffect
- Step 9, fold run 29c (2026-11-24, `72dff7cb99`): exile-until-rel onto ExileTargetSpellEffect.triggeringSpellWithSource
- Step 9, fold run 27b (2026-11-24, `164f2bb98e`): source-keyed redirects onto RedirectNextDamageFromChosenSourceToSelfEffect
- Step 9, fold run 32b (2026-11-24, `b573239e26`): Opalescence onto AnimateNoncreatureArtifactsEffect
- Step 9, fold run 35b (2026-11-24, `7b579358bf`): enters-tapped over PlayerSubject (EnterPermanentsOfTypesTappedEffect players)
- Step 9, fold run 29d (2026-11-24, `8e922281ba`): land-or-cast onto PutCardsOntoBattlefieldEffect + CastCardWithoutPayingManaCostIfAbleEffect
- Step 9, fold run 38 (2026-11-24, `23012888b0`): ability borrowers onto GainActivatedAbilitiesOfOwnCreaturesEffect
- Step 9, fold run 36b (2026-11-24, `158acfe38d`): defender conditions onto CantBeBlockedIfDefenderControlsMatchingPermanentEffect.whileDefendingPlayer
- Step 9, fold run 35c (2026-11-24, `d0a9f6c0a7`): player protection onto PlayerHasProtectionEffect
- Step 9, fold run 41 (2026-11-24, `bb381c2006`): as-enters sacrifices onto SacrificePermanentsAsEntersForCountersEffect
- Step 9, fold run 39 (2026-11-24, `fe05f2cbbe`): loss replacements offered for the losing player's choice
- Step 9, fold run 40 (2026-11-24, `8adda9a125`): flip-until-lose onto FlipUntilLoseEffect (+ StepKind.FLIP) firing the lost event
- Step 9, fold run 43 (2026-11-24, `60d5829b37`): HBG specialize cards onto SpecializeEffect(color)
- Step 9, fold run 37 (2026-11-24, `613f94df35`): only-attackers onto OnlyMatchingCreaturesCanAttackThisCombatEffect
- Step 9, fold run 42 (2026-11-24, `1114c929b1`): keyword costs onto ModifyKeywordCostEffect, per-target onto ModifyCastCostPerTargetEffect
- Step 9, fold run 41b (2026-11-24, `76356d7a12`): same-quality onto CardSharesCreatureTypeWithPermanentsPredicate / …
- Step 9, fold run 44 (2026-11-24, `24e42b8a23`): airbend (one exile event), connive over convokedThisSpell in CR 701.50c order
- Step 9, fold run 45 (2026-11-24, `4d1dd51a43`): crew/saddle/station action sets, guess threshold and guesser
- Step 9, fold run 46 (2026-11-24, `b4b0bcba8b`): pay-any onto EachPlayerPaysAnyManaEffect(payers)
- Step 9, fold run 37b (2026-11-24, `3e2603acb8`): chosen, among-your-permanents and outside-commander-identity protection onto …
- Step 9, fold run 41c (2026-11-24, `efa6524a90`): may-reveal-hand onto LookAtTopCardMayRevealMatchingToHandEffect
- Step 9, fold run 40b (2026-11-24, `aa4953e30c`): Endeavors onto RollTwoDiceChooseOneEffect(sides) with advantage-aware rolls
- Step 9, fold run 44b (2026-11-24, `9da2e92812`): next-spell riders onto EmpowerNextCreatureSpellThisTurnEffect
- Step 9, fold run 45b (2026-11-24, `5ba165227d`): spell-permanent exchange onto ExchangeControlOfPermanentAndSpellEffect
- Step 9, fold run 46b (2026-11-24, `f62572a7a9`): pay-{X} reflexives onto MayEffect + EachPlayerPaysAnyManaEffect + QueueReflexiveAbilityEffect.withEventValue
- Step 9, fold run 47 (2026-11-24, `73345359b0`): punisher onto MayPayManaEffect.anyPlayerMay (non-mana payment over acceptedPlayers)
- Step 9, fold run 49 (2026-11-24, `20e1fefb8a`): pay-{0} statics onto PayZeroRatherThanAbilityCostEffect
- Step 9, fold run 24 (2026-11-24, `292016e06a`): created/returned counters onto PutCounterOnTargetPermanentEffect
- Step 9, fold run 7b (2026-11-24, `942b44fe7c`): delayed graveyard returns onto RegisterDelayedStepTriggerEffect.aboutCardAtNext
- Step 9, fold run 30b (2026-11-24, `3101735d55`): exiled-with-source permissions onto AllowPlayCardsFromExileEffect.alsoCardsExiledLaterThisTurn
- Step 9, fold run 6c (2026-11-24, `71f697599e`): chosen-colour hexproof onto GrantControllerAndPermanentsHexproofFromColorsEffect.chosenColor
- Step 9, fold run 46c (2026-11-24, `a87a20a310`): Vault 112 onto pay-energy + shuffle + ExileTopCardsAndMayCastSpellsEffect.controllerMayPlayUpTo
- Step 9, fold run 47b (2026-11-24, `32668a8aa1`): counter punishers onto MayPayManaEffect.anyPlayerMay
- Step 9, fold run 24b (2026-11-24, `538eec35b0`): chosen counters onto PutCounterOnTargetPermanentEffect over thisWay
- Step 9, fold run 30c (2026-11-24, `873c399e05`): Quicksilver and Nathan's activated permissions onto SpendManaAsAnyColorForActivatedAbilitiesEffect
- Step 9, fold run 49b (2026-11-24, `731098381b`): escalate/entwine costs onto EscalateCost(perModeCost)
- Step 9, fold run 47c (2026-11-24, `d3ac8e08c9`): X energy and dynamic life costs onto PayEnergyCost
- Step 9, fold run 24c (2026-11-24, `ee282a8a74`): entering-permanent amounts onto ControlledPermanentsEnterWithAdditionalCountersByAmountEffect.ofEnteringPermanent
- Step 9, fold run 48 (2026-11-24, `c9a3cf6f20`): cost-shape singles onto MayEffect / MayPayManaEffect.dynamic / MayPayLifeEffect / RepeatEffect
- Step 9, fold run 8c (2026-11-24, `0da7cc8f66`): proliferate records PUT_COUNTERS (Ripples onto choose + phase out)
- Step 9, fold run 7c (2026-11-24, `6c778db591`): Taunt/Gideon Jura onto MatchingCreaturesMustAttackThisTurnEffect.attacking
- Step 9, fold run 8d (2026-11-25, `7e11b0c9e2`): Dismantle, Gandalf, Worlds Within Worlds, Mimic Vat
- Step 9, fold run 50 (2026-11-25, `7613422ead`): pay X life onto PayLifeCost.x() evaluated at the announced X
- Step 9, fold run 48b (2026-11-25, `43a3b63a0b`): floated attack/block taxes onto GrantStaticEffectToPlayerEffect
- Step 9, fold run 8e (2026-11-25, `2710d8f638`): LookAtTopCardsAndMayCastEffect's reveal records REVEAL and resumes after the cast
- Step 9, fold run 50b (2026-11-25, `f03f1ac695`): Dermotaxi, Protean War Engine, Lazav and Valki onto BecomeCopyOfSubjectEffect over exiledWithSourceUnlinked
- Step 9, defect run D11 (2026-11-25, `e83879bad6`): face-down and lost-ability reads of draw, damage
- Step 9, defect run D13 (2026-11-25, `e4f7325a66`): Giant Albatross, Disorienting Choice, Sothera
- Step 9, defect run D12 (2026-11-25, `ae3acf587e`): multiplayer opponents for clash, fateseal, Deliver Unto Evil
- Step 9, fold run 50c (2026-11-25, `4fbce06cd7`): Serene Remembrance and Turn the Earth onto PutCardsIntoLibraryEffect.shuffledIn over the new non-target …
- Step 9, defect run D14 (2026-11-25, `a80d85441d`): Fiery Gambit/Mutalith flip events, dice advantage on every roll
- Step 9, defect run D15 (2026-11-25, `4775fe3354`): Turf War targets one land per player (CR 603.3d)
- Step 9, defect run D16 (2026-11-25, `6db0a41a17`): group 5 targets and choices (CR 608.2d offers)
- Step 9, defect run D17 (2026-11-25, `adf7d50eeb`): life payments respect "can't lose life" (CR 119.8)
- Step 9, defect run D19 (2026-11-25, `b6bd28c2dd`): borrowed exile/return pairs keep their link (CR 607.5)
- Step 9, defect run D18 (2026-11-25, `a746cbfbab`): perpetual changes kept off copiable values (Prankster)
- Step 9, defect run D20 (2026-11-25, `03b150de61`): Kaima skips re-goads (CR 701.15d), Jailbreak's made ability sourced by the spell
- Step 9, defect run D21 (2026-11-25, `ac8eb9e81e`): Ghostly Flame colourless damage in source filters and combat exemptions
- Step 9, defect run D22 (2026-11-25, `ff7f7f9ea2`): any-player mana payments ask every player (PCY rulings)
- Step 9, defect run D23 (2026-11-25, `48e81e2ca3`): the perpetual "it" stays with the object (CR 400.7, owner's decision B)
- Owner's decisions (2026-11-25, `a920f9a079`): "counter unless" as a generic "unless" record in step 10, after Counters and Damage
- Step 10, run Counters (2026-11-25, `0fa7edc6e0`): `PutCountersEffect` (the renamed target record) absorbs the two self-counter records
- Step 10, run Remove counters (2026-11-25, `bfb123b7cd`): `RemoveCountersEffect` (the renamed source record) absorbs the target, plural-target and two mass records
- Step 9, defect run D24 (2026-11-25, `7cb5eee82e`): one permanent-damage path for every damage kind (the Damage run's prerequisite)
- Step 10, run Damage (2026-11-25, `0e4430a3cd`): `DealDamageEffect` (the renamed any-target record) absorbs the other four one-recipient damage records
- Owner's decisions (2026-11-25, `fe14f3c6fa`): the "unless" runs (`UnlessPaysEffect`, U1 first, the ransom a cost)
- Step 10, run U1 (2026-11-25, `70c7787bdf`): new `UnlessPaysEffect` absorbs `CounterUnlessPaysEffect` and `CounterUnlessPaysLifeEffect`
- Step 9, defect run D25 (2026-11-25, `752fca8bea`): `ResolutionCostPaymentService` pays resolution-time costs for any payer through the shared actions
- Step 10, run U2 (2026-11-25, `d8dc2bd8d9`): `UnlessPaysEffect` absorbs the other eleven counter / retarget ransoms; `AnyOfCost` and three new ransom costs
- Step 10, run U3a (2026-11-25, `576045365e`): `UnlessPaysEffect` absorbs every single-payer mana and life "may pay"; `couldPossiblyPay`
- Step 10, run U3b' (2026-11-25, `ef0278a44e`): the several-payer loop on `UnlessAnswer` (APNAP), `AllOfCost`, `ExileCardsCost`, five "may pay" records absorbed
- Step 10, run U3c (2026-11-25, `11b14b7f88`): `UnlessPaysEffect.payingTogether()` absorbs `MayPayManaEffect` and `MayPayPayer` (19 cards); `SourceControllerDrawsCost`; the sacrifice-candidate guards
- Step 10, run U3b1 (2026-11-26): 25 single-verb "unless" / "may pay" records (44 card files) onto `UnlessPaysEffect`'s existing factories; Erosion's, Arcum's Whistle's, Aether Rift's and Spelltithe Enforcer's payers fixed
- Step 10, run U3b2 (2026-11-26): 15 single-verb records (24 card files) onto `UnlessPaysEffect` with `eachUnless`, the payer's target and four ransoms; Spinecrawler, Read the Runes, The Dalek Emperor, Overencumbered fixed
- Step 10, run U4a (2026-11-26): 121 `ForcedCostOrElseEffect` card files onto `UnlessPaysEffect` and steps (Cosmic Horror's and Minion of Leshrac's fused records split); every player asked in "any player may pay", a payer with no mana source left unasked, Fraying Line's exile one event
- Step 10, run U4b (2026-11-26): the last 38 `ForcedCostOrElseEffect` card files and its Satya, Ashling and Flash builders onto `UnlessPaysEffect` (nine new ransom cases) and steps, Koya's record split; Satya pays the token's mana value as it resolves, a Phyrexian symbol in a ransom is paid with its colour or 2 life
- Step 10, run U4c (2026-11-26): echo (51 cards) onto `UnlessPaysEffect` with `PayEchoCost` read as the cost it names; Thick-Skinned Goblin's {0} offered beside the echo cost (its ruling), an echo mana payer with no mana source left unasked
- Step 10, run U4d (2026-11-26): cumulative upkeep (84 cards) onto `UnlessPaysEffect` with eight new ransom cases and a coloured PAY_MANA reading (Balduvian Fallen); `ForcedCostOrElseEffect`, both its handlers and `DestructionSupport.resolveForcedCostElseEffects` deleted; a payer with no way to pay unasked
- Step 10, run U5 (2026-11-26): the damage ransoms onto `UnlessPaysEffect` with `DamageFromSourceCost` (11 card files); five records and ten handlers deleted (Worms of the Earth's with them); Worms asks every player (CR 101.4) and offers no sacrifice its payer can't make
- Step 10, run U6 (2026-11-26): `UnlessPaysEffect.forEach`, the for-each loop; Cut the Tethers, Cleansing and Giant Albatross onto it, three records and six handlers deleted; Cut the Tethers returns the unpaid Spirits together, Cleansing asks every player for each land
- Step 10, run U6b (2026-11-26): `UnlessPaysEffect.forEachCard` (Sirocco, Sword-Point Diplomacy) and a discard ransom paid together (Ob Nixilis, the Adversary, Professor Onyx); four records, `LoseLifeUnlessDiscardEffect` and six handlers deleted; Sword-Point asks every opponent per card, the discards and losses come after every choice (CR 101.4)
- Step 10, run U6c (2026-11-26): `.payingTogether()` on The Long Reach of Night and Possessed Portal; every payer chooses before any card is discarded, the chosen cards discarded together (CR 101.4)
- Step 10, run U7a (2026-11-26): the four non-mana `XOrY…Cost` records (7 card files) onto a cast-time `AnyOfCost` with `AdditionalSpellCostService.chosenAlternative`; free Final Payment offered its alternatives, the AI pays one alternative of Dusk Mangler's cost
- Step 10, run U7b (2026-11-26): the five mana `XOrY…Cost` records (16 card files) onto `AnyOfCost` with a `PayManaCost` alternative paid in the total cost (CR 601.2f); free Eaten Alive and Bogslither's Embrace offered their alternatives, Eaten Alive cast from exile sacrifices (CR 601.2b)
- Step 10, run U7c (2026-11-26): `ForageOrPayManaCost` replaced by `ForageCost` (CR 701.61a); Feed the Cycle an `AnyOfCost`, castable free for {B}
- Step 10, run Boost B1 (2026-11-26): `BoostTargetCreatureEffect` renamed `BoostEffect` with factories, `BoostSelfEffect` and `BoostTargetPermanentEffect` absorbed (2,542 card files by script); `AmountsRead`, `CreatureBoostEffect.targetedBoost`
- Step 10, run Boost B2 (2026-11-26): `BoostAllOwnCreaturesEffect` and `BoostAllCreaturesEffect` absorbed into `BoostEffect.creaturesYouControl` / `allCreatures` / `creaturesTargetPlayerControls` (609 card files by script); the resolver binds a rider's target card
- Step 10, run Damage P (2026-11-26): `DealDamageEffect` takes the player recipients; `DealDamageToPlayersEffect` and `DealDamageToEachOpponentEqualToControlledPermanentCountEffect` absorbed (704 card files by script)
- Step 9, defect run D26 (2026-11-26): the set handlers' and the player kinds' preventions, protection and Aegis of Honor's redirect applied in the damage pools (CR 616.1); DEAL_DAMAGE begun by the set handlers; no face-down source named
- Step 10, run Damage M1 (2026-11-26): `DealDamageEffect` takes the set recipients (`toEach`, `toEachTarget`); the four set records absorbed (112 card files by script)
- Step 10, run Damage M2 (2026-11-26): `MassDamageEffect` absorbed into `DealDamageEffect`'s set kind (`toEachCreature`, `toEachCreatureAndPlaneswalker`, `eachEvaluatedFor`, `andTo`; 241 card files by script); Riveteers Confluence spares its controller's planeswalkers
- Step 10, run Damage R (2026-11-26): ten other-dealer records absorbed into `DealDamageEffect.dealtBy(dealers)` (`bite`, `targetCreatureToItself`, `toEachOther`; 138 card files by script); the bite's pre-pool checks are pool options (CR 616.1); Vengeful Ancestor's attacker deals as the triggering permanent
- Step 10, run A (2026-11-26): the four graveyard card movers absorbed into `PutCardsIntoHandEffect` and `ExileGraveyardCardsEffect.cards` over `sourceCard()` / `triggeringCards()` (122 card files by script); the survivors' log lines
- Step 10, run X (2026-11-26): the excess redirect absorbed into `DealDamageEffect.excessToControllerInstead()` / `bite().excessToControllerInsteadIfDealerHasTrample()` (5 card files by hand); a creature-planeswalker's greatest excess, the damage step begun anew
- Step 10, run S (2026-11-26): the attack and block restrictions absorbed into `CantAttackOrBlockUnlessEffect.attack` / `block` / `attackOrBlock`, the Arrest lock into `EnchantedPermanentCantActivateAbilitiesEffect.includingManaAbilities()`, Evil Eye's lock into `CreaturesCantAttackUnlessPredicateEffect.youControl` (145 card files by script); no behaviour change
- Step 10, run G (2026-11-26): the two goad-set records absorbed into `GoadTargetCreatureUntilNextTurnEffect.eachCreature` / `creaturesControlledBy` / `creaturesTargetPlayerControls` (10 card files by hand); their goads are a GOAD step under the CR 701.15d check

**Kept in full** (the sections the remaining runs read): step 10's runs and defect runs D24 and D25.

### Step 10, run Counters (2026-11-25)

Put counters on one permanent, signed off by the owner on 2026-11-25 (decisions under Standing knowledge). The target record is
renamed `PutCountersEffect` (handler `PutCountersEffectHandler`) and absorbs `PutCountersOnSelfEffect` and `PutCountersOnSourceEffect`
as `Subjects.source()`: `onSelf(kind[, int | DynamicAmount])`, `onSelfCountingTargetPlayer` (Gideon, Champion of Justice; Sting),
`onSelfWithTargetPermanent` (Super-Adaptoid). Ledger: 2 absorbed, 1 survivor (the rename). Ratchet 3,002 / 2,428 → 3,000 / 2,426.
Nothing persists or serialises effect class names (no `@JsonTypeInfo`; frontend, networking, the inventory script name none).

**Card rewrite.** 1,391 card files, 1,451 calls, by a scratch script (balanced parentheses, a table of shapes, loud failure on any
other shape or leftover name; deleted): `onSelf` 1,448, `onSelfCountingTargetPlayer` 2, `onSelfWithTargetPermanent` 1; 7 local
variable types, 1 unused import (Cool but Rude). The 1,137 survivor files changed only the name. Oracle text checked for all 1,391
files (1,451 calls) with the card-info server's `get-card` (the MCP `get_card` implementation), back faces through their front card:
1,366 calls matched kind, count and "this / it / its name" automatically, 85 by hand, the 155 "it"-only matches screened for another
referent (23 read, all the source). Not checked: Experiment Five (MB1 73 is in no data feed; checked against its ability text) and
Shadowheart's green specialize face (no Oracle data). Engine and AI sites (17) and 12 test files by hand; agent-docs and both skills
name no deleted class.

**Probe.** The self handler's `entry.getTargetId()` fallback made to throw, then 1,036 classes run (every self card, the Pay-X /
pay-any-times / exile-sacrificed / end-of-combat / increment cards, the application and AI tests that build it): 12,267 tests, nobody
hit it. `source()` stays strict.

**Behaviour changes** (the old self-counter path was rules-wrong; failing-first tests, all seen failing on the old code):
- The 382 former `PutCountersOnSourceEffect` cards now place through the shared placement: Knight of Wundagore sees the counter
  (`JenaraAsuraOfWarTest`), "whenever counters are put on this creature" triggers (Danny Pink's grant, same class), the controller
  orders Hardened Scales and Doubling Season (CR 616.1; Jenara, `ParagonOfModernityTest`), a later "that many" reads PUT_COUNTERS
  (CR 608.2c) and a per-counter trigger sees each of two counters (CR 603.2c) (`SelfCounterPlacementTest`). Their log line is now
  the shared one ("X puts a +1/+1 counter on Y").
- A `MayEffect` over a source placement asks nothing when the source can't have the counters, or is gone and an else follows
  (CR 608.2d), now for the former self record too: Kresh under Solemnity (`KreshTheBloodbraidedTest`). Fabricate's departed-source
  check reads the merged record.
- Dethrone (attack and the cloned second instance), the regeneration counters (Matopi Golem, Skeleton Scavengers), endure, increment,
  Pyramid of the Pantheon's mana-ability counter and Black Widow build `onSelf`; Black Widow places through
  `PermanentCounterSupport.placeCounterOnPermanent` (no prompt inside its may handler, the shared events now fire).
- AI: `BoardEvaluator.growthThreatBonus` reads `source()` +1/+1 and -1/-1 with a fixed amount (the former self sliths now count);
  `TargetPolarityClassifier` classifies a `source()` placement NEUTRAL (its name-table entry is gone).

**Pins added first** (passed on the old code and after): Graypelt Hunter under Solemnity asks nothing. The plan's other pins
existed (Kresh after Kresh left still asks; fabricate after the source left creates Servos; the 7 planes; Gideon; Sting; Super-
Adaptoid; Oft-Nabbed Goat; Pyramid; Dack's Duplicate's second dethrone; Matopi Golem; Skeleton Scavengers; endure; increment).

**Shared engine files that changed meaning.** `PutCountersEffect` (accepts `source()`, two target-declaring booleans valid only with
it, `onSelf*` factories, self marker spec); `PutCountersEffectHandler` (source path: planar object, snapshot refresh;
`beginReplacementOrderIfNeeded` deleted, its only caller gone); `MayEffectHandler` (one source rule for every `source()` counter
may); `EffectResolutionService` (fabricate check on the merged record); `ActivatedAbilityExecutionService` (mana-ability path on
`source()`); `BoardEvaluator`, `TargetPolarityClassifier`.

**Tests run** (after the merge, XML of this run): 1,469 classes (1,389 rewritten-card classes incl. back faces under their front card, 40 survivor sample, the pins, the services, readers, AI, both ratchets), 16,066 tests, 0 failures. Compiled every module's main and test sources.

**Seen, not changed** (in the defect queue):
- The spore-counter cards use `CounterType.FUNGUS`, also Sporogenesis' and Mindbender Spores' real fungus counters; Thallid
  Shell-Dweller uses `SPORE` (CR 122.1: Thelon counts fungus as spore, Sporoloth can't remove Shell-Dweller's). Sure of the code.
- Voracious Brood's "whenever one or more creature cards are put into your graveyard from anywhere, put that many" is one trigger per
  card outside the library path (CR 603.2c). Fairly sure, minor.

**For the next run.** Remove counters, as the owner ordered: `RemoveCounterFromTargetPermanentEffect` and
`RemoveCounterFromSourceEffect`. Not surveyed here; what this run saw: `MayEffectHandler` already pre-checks a leading
`RemoveCounterFromSourceEffect` (`sourceHasCountersToRemove`, fold run 48b) and the source record already takes a subject
(`RemoveCounterFromSourceEffect(thisWay(CHOOSE), kind)`, Chandra, Legacy of Fire), so whether they differ only in subject needs a read
of both handlers. The mass placements wait for a batched path in `PutCountersEffect`.

### Step 10, run Remove counters (2026-11-25)

Remove N counters from permanents, signed off by the owner on 2026-11-25 (decisions under Standing knowledge). The source record is
renamed `RemoveCountersEffect` (handler `RemoveCountersEffectHandler`) and absorbs `RemoveCounterFromTargetPermanentEffect`,
`RemoveCountersFromTargetPermanentEffect` (no ledger row before), `RemoveCounterFromEachMatchingPermanentEffect` and
`RemoveCounterFromEachControlledPermanentEffect` as `target()` / `controlledBy(...)` subjects: `fromSelf`, `fromSelfCountingTargetPlayer`,
`fromTarget()` / `fromTarget(kind, int | DynamicAmount[, predicate])` / `fromTarget(kind, predicate)`, `new RemoveCountersEffect(subject,
kind[, int])`. Ledger: 4 absorbed, 1 survivor (the rename). Ratchet 3,000 / 2,426 → 2,996 / 2,422. Nothing persists effect class names
(no `@JsonTypeInfo`; networking, frontend, scripts name none). No `CONVERSION_VERSION` bump (no mapper or enum change).

**Premise checked.** Every difference beyond the subject was a defect of the non-source paths (below) or presentation. The mass
records remove per permanent on both paths (one `setCounterCount` per permanent, so the removal events and the new LAST_REMOVED are per
permanent, CR 603.2c): no batched trigger to keep, so they are in. Gremlin Mine (Oracle; no rulings) moved to `RemoveUpTo…(CHARGE, 4,
target())`. The target spec base widened from creature to permanent for the 5
predicate cards; each predicate includes `PermanentIsCreaturePredicate` and no `declaredTarget()` reader branches on a creature base.

**Card rewrite.** 128 card files, 135 calls, by a scratch script (balanced parentheses, a table of shapes, loud failure on any other
shape or leftover name; deleted): `fromSelf` 111, `fromSelfCountingTargetPlayer` 1, `fromTarget` 14, subject constructor 8 (4 named,
4 mass), Gremlin Mine 1. Oracle text checked for all 128 files (135 calls) with the MCP `get_card` implementation, back faces through their
front card (Temple of Cyclical Time via Ojer Pakpatiq; Omenpath to Naya via MB2 616, 379 is in no feed): 100 calls matched kind, count and
"this / it" automatically, 35 by hand. Engine and AI sites (10) and 7 test files by hand.

**Behaviour changes** (old behaviour rules-wrong; failing-first tests, all seen failing on the old code):
- A target removal now goes through the shared removal: the last time counter triggers vanishing (CR 702.63a; `ThrullParasiteTest`
  Waning Wurm), the controller's last-counter event fires (Divine Intervention's ruling: the controller's own Thrull Parasite or Medicine
  Runner removing the last intervention counter draws the game; `DivineInterventionTest`, both paths), and a Siege that loses its last
  defense counter on the kind-choice path waits for its defeat (CR 310.12b, 704.5v; `MedicineRunnerTest`). `DistributeCountersAmongTargets`'
  delayed removals get the same path.
- The kind among several is a spliced mode choice, and REMOVE_COUNTERS holds the chosen removal, not the pre-choice result (CR 608.2c);
  the plural and both mass records now record the step (`StepResultsTest` Counters, 4 cases); a mass removal raises the last-counter
  event once per permanent (CR 603.2c; `LastCounterRemovedTriggerTest`).
- Presentation: the survivor's log line per permanent replaces the target and mass lines; Gremlin Mine asks a how-many list (0 to 4).
- AI (owner's decision 4, `TargetPolarityGuardTest`): a removal from a target scores by kind through the `RemoveUpTo…` rule, now one
  helper (`removedCountersPolarity`): -1/-1 beneficial (Defiant Greatmaw, Chainbreaker, Shed Weakness, Woeleecher), any other kind harmful
  (Bloodcrazed Hoplite; Etched Host, Portent Tracker; Clash of the Eikons, Lonely End, Sigurd, Confront the Past, formerly unclassified);
  `fromTarget()` (Medicine Runner, Spinal and Thrull Parasite) NEUTRAL as before; non-target forms null as before.

**Pins added first** (passed on the old code and after): Thrull Parasite answering the kind choice; Medicine Runner removing a lone
defense counter defeats the Siege; Portent Tracker's last defense counter defeats the Siege. Etched Host, Clash and Sigurd (the mode's
target), Gremlin Mine and Confront the Past (2X) were pinned already. `@DisplayName` added to Etched Host's and Portent Tracker's 20 methods.

**Tests changed.** `GremlinMineTest` answers the how-many list, not an X value (owner's decision 3; same counts); the application tests
use the factories (`RemoveCountersEffectTest` renamed).

**Shared engine files that changed meaning.** `RemoveCountersEffect`, `RemoveCountersEffectHandler` (amount against the source for a
target, defense check), `TargetPolarityClassifier`, `DistributeCountersAmongTargetsEffectHandler`. Renamed only: the MayEffect, ChooseOne,
mode-prompt, activation, clone, combat (paralyzation constant still `equals` `fromSelf`) and AI chosen-permanents readers.

**Tests run** (after the merge, XML of this run): 344 classes (328 card classes: the rewritten cards' incl. Temple under Ojer Pakpatiq, the
pins, Dread Wight, the Distribute and remove-up-to cards, and every class matching `Vanishing|Fading|Suspend|DivineIntervention|Defense|Battle|Siege`
or naming vanishing; 13 application, 3 AI), 3,502 tests, 1 failure (my new guard case asserting a modal card, rewritten to a non-modal
one, rerun: 17 / 0). Compiled every module's main, test and fixture sources and all card-test modules.

**Seen, not changed** (defect queue): by-kind polarity reads HARMFUL where the target is the AI's own (lore off "a Saga you control",
defense off a battle an opponent protects); the Distribute delayed removals declare a target where `named(id)` would do (unverified).

**For the next run.** Damage, as the owner ordered, then "unless". Not surveyed here; what this run saw: `DamageSupport` and
`CombatDamageService` subtract defense counters inline and call `BattleDefeatSupport.checkAfterDefenseRemoved` themselves, as the
removal path now does; the hand-off pairs are `DealDamageToTargetPermanentEffect` with the creature-or-planeswalker damage records (5g).
The remove-all records (`RemoveAllCountersEffect` 31 and its forms) wait for a "remove all" run.

### Step 9, defect run D24 (2026-11-25): one permanent-damage path

The prerequisite of step 10's Damage run (its plan, section 2 (a)); no record merged or renamed. `DamageSupport.resolveAnyTargetDamage`
(any-target, player-or-planeswalker, permanent records) and `resolveCreatureTargetDamage` (creature, creature-or-planeswalker; now a call
of the first) send damage to a permanent straight to its pool; the two target-creature multi-target loops do too. Ratchet unchanged.

**Pre-check differences found, and what became of them** (every one now a rule of `PermanentDamagePreventions`, so it is ordered (CR 616.1)
and logged by the pool's line):
- Source / stack-entry prevention (both paths pre-checked it): pool `SourceDamagePrevented` / `SourcePreventedFromDealing`.
- "Prevent damage from target spell" without life gain (any-target only): pool `TargetSpellPrevented`.
- "Prevent all damage by creatures" (any-target only, Chant's life gain from the raw amount): pool `DamageByCreaturePrevented`, which now reads
  a departed source's last-known controller (CR 608.2h; plan edge case (iii): the creature path let Thwart the Enemy's damage through), and
  gains life for the damage actually prevented. `SourcePreventedFromDealing` no longer also offers it (`isPreventedFromDealingDamage(…, false)`):
  the creature path asked Ethereal Haze's prevention twice.
- Source prevented from dealing damage (any-target only): pool `SourcePreventedFromDealing`.
- Protection (both pre-checked; the pool had only Phantom Centaur's step): one pool rule `Protection` replaces the step
  `ProtectionCompetingWithCounterRemoval` (CR 702.16e). A targeted spell never reaches it (the target is illegal, CR 702.16b, 608.2b);
  non-target recipients (Ghyrson Starn's event object) and other dealers (Razor Boomerang) do.
- Unpreventable damage: both paths now feed only `DamageEvent.unpreventable` (the handler flag and `dealCreatureDamageUnpreventable` set it);
  each rule asks `isDamagePreventable`, so a prevention prevents nothing and its life gain is 0 (CR 615.12); shields aren't reduced.
- The toughness-as-loyalty branch skipped every pre-check (no protection for a planeswalkerified creature): gone. The player branch's
  `isDamageSourcePreventedWithLog` pre-check went too; `PlayerDamagePreventions` already had both rules.

**Behaviour changes** (failing-first, each seen failing on the old code): the any-target path asks the CR 616.1 order for a red-source
prevention, a spell shield, Chant of Vitu-Ghazi, a prevented source and protection beside a creature shield; the creature path asks it for
a source prevention; the player branch asks it for both pre-checked kinds; Ethereal Haze isn't asked twice on the creature path; Thwart the
Enemy prevents a departed source's creature-path damage; protection applies to a toughness-as-loyalty recipient; Ethereal Haze no longer
prevents Razor Boomerang's damage (its ruling: the Equipment is the source). Logs: "Damage dealt by X is prevented." (creature
prevention), "X's damage to P is prevented." (player).

**Tests.** New `PermanentDamagePathTest` (application, 37: pins per prevention on both paths, unpreventable, ordering, player branch); 2 in
`RazorBoomerangTest`. Changed: `AbstractDamageHandlerTest.stubDamageFromSourceNotPrevented` lenient and two protection stubs dropped in
`DealDamageToTargetCreatureEffectHandlerTest` (the paths no longer ask). Run: 2,337 classes (the cards of the five records and of every
handler calling the changed methods, every `Prevent|Prevention|Protection|Unpreventable|DamageReplacement|Redirect|ChantOfVitu` class, 24
application classes incl. `EffectCountRatchetTest`), 0 failures.

**Seen, not changed** (defect queue, group 9): ~38 other handlers still pre-check; the `triggering()` subject's double log; fight / bite protection.

**For the next run.** The Damage merge (plan `s10-damage`): the merged handler needs no second permanent path now; question 5 is answered
by this run, questions 1 to 4 wait on the owner.

### Step 10, run Damage (2026-11-25)

One source deals N damage to one recipient, signed off by the owner on 2026-11-25 (decisions under Standing knowledge). The any-target
record is renamed `DealDamageEffect` (handler `DealDamageEffectHandler`) and absorbs `DealDamageToTargetCreatureEffect`,
`DealDamageToTargetCreatureOrPlaneswalkerEffect`, `DealDamageToTargetPlayerOrPlaneswalkerEffect` and `DealDamageToTargetPermanentEffect`
as a `Recipient` kind (factories and riders under Shared pieces). Ledger: 4 absorbed, 1 survivor. Ratchet 2,996 / 2,422 → 2,992 / 2,418.
Nothing persists effect class names (no `@JsonTypeInfo`; networking, frontend, scripts name none); `TargetPolarityClassifier`'s name
entry and C-PW branch and `InstantCategoryClassifier`'s C-PW line were dead (the `DamageDealingEffect` branch answers first) and went.
No `CONVERSION_VERSION` bump (no mapper or enum change).

**Premise.** After D24 there is one permanent-damage path (plan section 2 (a) no longer held): every targeted kind goes through
`resolveAnyTargetDamage`; "can't be prevented" is one flag for every kind (`dealCreatureDamageUnpreventable` set the same event fields,
deleted with `resolveCreatureTargetDamage` and `computeExcessDamageToTargetedPermanent`). The target loop stays per kind (owner's question
1): the creature kinds deal to each of the effect's targets (Dual Shot), the any-target kind to one, since an unbound any-target part of a
two-target spell (Magma Burst kicked) would hit the other part's target.

**Card rewrite.** 1,614 card files (1,738 calls) by a scratch script (balanced parentheses, 30 shapes, loud failure on any other shape,
variable types resolved for the ambiguous arities, leftover-name check; deleted): any target 808 (17 by the triggering permanent), creature
617, creature or planeswalker 121, player or planeswalker 124, opponent or planeswalker 38, subjects 28, permanent 3. Oracle text checked for 1,610 files with the MCP `get_card` implementation (recipient wording, "can't be
prevented", "can't be regenerated", exile riders, "it deals"), back faces through their front card: 1,596 matched automatically, 14 by hand
(the "damage can't be prevented this turn" cards use their own record); not checked: Lazier Goblin, Lightning Colt, Sliv-Mizzet, Trial and
Error (MB1, in no feed). Engine, domain and AI sites (24) and 51 test files by hand or by the same script.

**Behaviour changes** (failing-first, each seen failing on the old code):
- Terror of the Peaks deals the damage itself, "equal to that creature's power" (Oracle; its ruling): it was dealt by the entering
  creature (its lifelink gained life). `TerrorOfThePeaksTest`. Éomer, King of Rohan ("Éomer deals") is the plain source form.
- A departed triggering dealer is its departure snapshot before the trigger-time one (CR 608.2h, "as it most recently existed"): Gaze of
  Pain's attacker that left kept no Whip of Erebos lifelink. `GazeOfPainTest`.
- The target-permanent kind begins its DEAL_DAMAGE step (`StepResultsTest`); a triggering dealer's damage now reaches the resolving
  entry's DEAL_DAMAGE step (any-target and player-or-planeswalker kinds lost it in the carrier; `DealDamageEffectTest`).
- Excess damage reads the dealer's deathtouch (CR 120.4a), not the ability's (`DealDamageEffectTest`); one formula and one detection list
  for every kind (owner's question 4), measured before the damage (the creature kind read toughness after it: wither or infect damage
  lowered it). Check of the 63 excess-risk files: no card changes which effect is tracked (the new union readers sit only beside
  event-value amounts, which the guard excludes; Corrupt records instead); all their test classes pass.
- Archery Training's granted any-target damage keeps every component when its counters are bound (`ActivatedAbilityExecutionServiceTest`).
- A self-triggered "this or another X enters, it deals" names no triggering permanent: the carrier deals it from the source (Scourge of
  Valkas' own entry kept working through the any-target record's fallback; pinned, see below).

**Pins added first** (passed on the old code and after): a departed dealer with Whip of Erebos lifelink for Aether Charge, Bélakor,
Fiendlash, Flayer of the Hatebound, Immersturm, Éomer, Defiant Thundermaw (Invasion of Tarkir), Extra Arms (and its live lifelink), Farrel's
Mantle, Rage Forger, Pyrogoyf, Scourge of Valkas (and its own entry with lifelink), The Sea Devils, Lukka's emblem (Mila); Wrathful Red
Dragon's lethally damaged Dragon. The plan's other pins existed (`PermanentDamagePathTest`, the card tests named in section 5).

**Tests changed.** The two handler tests are one `DealDamageEffectHandlerTest` (creature cases `@Nested`); tests use the factories.
**Shared engine files that changed meaning.** `DealDamageEffect` / `DealDamageEffectHandler` (new); `DamageSupport` (carrier snapshot
order and source fallback; three methods deleted); `ActivatedAbilityExecutionService` (`withDamage`); `EffectResolution` (converge and
mana-spent readers on the creature kind only, as before); `DamageTargetValidators` (keyed on the merged record, opponent relation only).

**Tests run** (after the merge, XML of this run): 1,658 card classes (every rewritten card's class, back faces under their front card,
every `Prevent|Protection|Unpreventable|Redirect|Excess|Deathtouch|Lifelink|Infect` class) 15,126 tests; 69 application, AI and
carrier classes (the 36 the plan names, the pins, `PermanentDamagePathTest`, both ratchets, `GameDataDeepCopyTest`) — 0 failures.

**Seen, not changed** (defect queue): self-triggered "this or another X enters" entries record no triggering permanent, so other
`triggering()` readers there may find nothing (fairly sure). AGENTS.md's reuse example names the old any-target record (owner's file).

**For the next run.** The generic "unless" record (owner's order; Standing knowledge's decision of 2026-11-25). Damage siblings left out:
`DealDamageToPlayersEffect` (a `to(PlayerSubject)` fold of this record; amounts per player fixed first), the mass and set damage records (a
"mass damage" run), the other-dealer records (bite, fight, power damage: a "dealer" run), `DealDividedDamageEffect` (division as cast),
`DealDamageToTargetCreatureWithExcessToControllerEffect` (a modified damage event), and the pay / "unless takes damage" damage records,
which go with the "unless" run.

### Step 10, run U1 ("unless": counter unless pays) (2026-11-25)

The generic "unless" record, signed off by the owner on 2026-11-25 (decisions under Standing knowledge): `UnlessPaysEffect(PlayerSubject
payer, CostEffect ransom, CardEffect ifNotPaid, CardEffect ifPaid)` (CR 118.12a), handler `UnlessPaysEffectHandler`, absorbing
`CounterUnlessPaysEffect` (308 card files) and `CounterUnlessPaysLifeEffect` (14). Factory names as the plan proposed (Shared pieces).
Ransoms: `PayManaCost` (new `announcedX` component, `PayManaCost.x()`) and `PayLifeCost`. Deleted: both records, both normalfx handlers,
mayfx `CounterUnlessPaysHandler`, `handleCounterUnlessPaysChoice`, `CounterSupport`'s rider methods (no caller left, and with them its
`EffectResolutionService` dependency), `RansomKind.PAY_MANA` / `PAY_LIFE`. Ledger: 2 absorbed, 1 survivor (new). Ratchet 2,992 / 2,418 →
2,991 / 2,416. No `CONVERSION_VERSION` bump (no mapper or enum change); nothing persists effect class names.

**How it runs (d3).** The handler asks while its own entry resolves; the answer is paid by `UnlessPaysEffectHandler.answer` (today's log
lines kept) and `EffectResolutionService` resolves the chosen branch in place of the record, after the target-group remap, so a
counter is a step of the ward or counterspell entry (COUNTER_SPELL recorded; its controller and source stay the entry's). Payer
`controllerOf(targetSpell())`: after the counter it reads `counteredSpellControllerId` (CR 608.2h), so the Power Sink / Frightful
Delusion premise held (their existing tests pin it). Ward detection, the stack door and `actsOnASpell` read `targetSpec()`, the
counter's; the AI prompt carries the same `manaCost` (none for a life-only ransom) with the life as `additionalLifeCost`;
`InstantCategoryClassifier` reads `counters()`. A counter whose spell an earlier instruction already countered asks nothing (Bring the
Ending's "counter that spell instead", caught by its test during the run).

**Card rewrite.** 322 files: 320 by a scratch script (balanced parentheses, 13 shapes, loud failure on any other, leftover-name check,
import in place; deleted), 341 calls (int 269, dynamic 25, life 26, X / exile 13, mana + life 3, exact cost 2, `ifPaid` 3); Power Sink
(`new UnlessPaysEffect(controllerOf(targetSpell()), PayManaCost.x(), SequenceEffect.of(counter, tap lands, lose mana), null)`) and
Frightful Delusion (the discard as its next spell effect, CR 608.2c) by hand. Engine sites: cloaked ward, `withWardTwo`, perpetual ward,
Chancellor's delayed counter, Titania's pay-only branch (now the next step instead of a queued prompt). Oracle text checked for all 322
with the card-info server's `get-card`: 299 matched automatically, 23 by hand (11 back faces through their front card); 2 contradicted it.

**Behaviour changes** (failing-first, each seen failing on the old code):
- (d1) A life ward asks when the spell can't be countered; paying spends the life (owner's decision). `OwlinShieldmageTest` (Combust).
- (d2) A payer who can't lose life can't pay a life ransom and isn't asked (CR 119.8; Courageous Resolve's fateful hour).
  `OwlinShieldmageTest`.
- (d4) K'rrik's controller may pay a ransom's {B} with 2 life (K'rrik's rulings: any cost, a hybrid's {B} included). `ThrullWizardTest`.
- Aurelia's Vindicator's ward was "pay 2 life"; Oracle: ward {2}. `AureliasVindicatorTest`.
- Captain Howler's ward was {2}; Oracle: "Ward—{2}, Pay 2 life". `CaptainHowlerSeaScourgeTest`.
- (d3) The payment is asked before the counter's entry leaves the stack; a declined ward aimed at an ability counters it through
  `CounterSpellEffect`'s ability rules (the old answer judged an ability by its source card's "can't be countered").

**Pins added first** (passed on the old code and after): Guile with a declined ward {2}; Guile with an unpayable Mana Leak; ward {2}
countering Flametongue Kavu's targeted trigger; Mana Leak against an unpayable flashback spell (exiled); X-only mana paying Condescend;
Gisa's "{2}, Pay 2 life" paid, declined and unpayable. The plan's other pins existed (Spell Syphon, Fleshgorger after it left,
Syncopate, Power Sink, Frightful Delusion, cloaked and perpetual ward, Codsworth, Waterfall Aerialist's flashback and Lullmage Mentor,
Disruptive Pitmage).

**Tests changed.** `CounterUnlessPaysEffectHandlerTest` became `UnlessPaysEffectHandlerTest` (ask, payability, ransom reads); the
application tests build the factories; `AureliasVindicatorTest`'s ward test declined a life payment (the wrong reading) and now has {2}.

**Tests run** (after the merge, XML of this run): 491 classes, 5,219 tests, 0 failures: every rewritten card's class (back faces under
their front card), the pins, every `Ward|Counter|Guile|Flashback|Rhystic|PayLife|KrrikSonOfYawgmoth|CourageousResolve|Mastery|DeathReplacement`
class, the application tests the plan names (`UnlessPaysEffectHandlerTest`, `CounterSpellEffectHandlerTest`, `TargetTriggerTest`,
`SpellTriggersTest`, `TriggeredAbilityStackServiceTest`, `MayPayManaEffectHandlerTest`, `EffectResolutionServiceTest`, `StepResultsTest`,
`GameDataDeepCopyTest`, both ratchets), the AI's `AiManaManagerTest`, `MCTSEngineTest`, `AiDecisionEngineTest`, `HardAiDecisionEngineTest`.
Compiled every module's main and test sources. Not run: the classes that merely reference a rewritten card (the orchestrator's sweep).

**Seen, not changed** (defect queue):
- The generic resolution-time "pay N life" paths still check `canPlayerLifeChange` (the D17 line, noted there). Sure.
- K'rrik's life estimate for a ransom payable only with mana not yet made reads the current pool. Minor, fairly sure.

**For the next run.** U2, the other counter ransoms and `AnyOfCost` (Step-10 hand-offs), after a resolution-time payment of any
`CostEffect` by a non-controller on the shared action code and the hand-rolled tax events fixed failing-first. A new ransom is a case in
`UnlessPaysEffectHandler.ransom` / `cannotBePaid` / `answer` and in the record's constructor check; a new payer is any `PlayerSubject`.

### Step 9, defect run D25 (2026-11-25): resolution-time cost payment by any player

The shared piece U2, U3b and U5 need, and the hand-rolled payments the "unless" plan found (its pre-check 1). Survey of every
resolution-time payment site: `scratchpad/d25/survey.md`. No record or handler added or removed (ratchet unchanged); no card file
changed; no `CONVERSION_VERSION` bump.

**The piece.** `ResolutionCostPaymentService` (Shared pieces). The activation-cost code pays costs chosen before activation (ids and
indices on the request), so it has no resolution-time entry to reuse; the piece calls the same shared actions instead (discard prompt,
`resolveRandomDiscardCards`, `SacrificePermanentThen` → `sacrificeAndLog`, `applyLifePayment` behind `answerAskingDeathReplacementsFirst`,
`ActionEvents.taxPaid`). `UnlessPaysEffectHandler.canPay` / `answer` pay through it (its unit tests unchanged: a second constructor
builds a pool-only service); the counter-unless discard and one-permanent sacrifice ransoms pay through it (same shared calls).

**Behaviour changes** (failing-first, each seen failing on the old code):
- Mana paid as a ransom for another player's spell or permanent raises the tax-paid event (Tax Taker's Oracle text and ruling): Soul
  Barrier, Isolation Cell (Phyrexian Tyranny, Rhystic Syphon, Elesh Norn), Lim-Dûl's Hex, Erosion (both prompts), Cut the Tethers,
  Crystal Shard / Withdraw, Arcum's Whistle, Titania, Rugged Rumbler, `ForcedCostOrElseEffect`'s mana (Mind Whip).
  `TaxTakerTest.ResolutionTimeTaxes`: 9 of 10 failed first (the ward {2} pin passed).
- The tax is the mana actually spent, not the cost's mana value (Lim-Dûl's {B/3} paid with {B}: one Treasure; a K'rrik payment counts
  only its mana); `UnlessPaysEffectHandler` with it.
- Titania's ward offers its {2} when the payer's lands could make it with an empty pool, and the prompt carries the mana cost so they
  can tap while asked (CR 118.3, 605.3a). `TitaniaRuggedRumblerTest.wardOffersManaThePayerCanStillMake`.
- Every resolution-time life payment needs "can lose life", not only "life total can change" (CR 119.8): Courageous Resolve stops
  Erosion's 1 life (`ErosionTest.controllerWhoCantLoseLifeCantPayLife`) and Arrogant Poet's 2 life
  (`ArrogantPoetTest.controllerWhoCantLoseLifeIsNotAsked`); about 30 sites (survey). 0 life is always payable (CR 119.4).

**Pins** (passed before and after): a Fiery Temper discarded to Forum Necroscribe's ward is exiled and its madness triggers; a creature
sacrificed to Vein Ripper's ward triggers Ashad's "whenever you sacrifice"; an opponent paying ward {2} makes two Treasures. The other
sites' pay / decline / unpayable tests existed (Soul Barrier, Isolation Cell, Lim-Dûl's Hex, Erosion, Cut the Tethers, Crystal Shard,
Withdraw, Arcum's Whistle, Titania, Mind Whip).

**The plan's unverified risk.** `SacrificePermanentThen` (sacrifice ransom) and the collect-evidence resume write into
`pendingEffectResolutionEntry`; at the ransom's answer that is the ward's own entry, already off the stack (probe on Vein Ripper), so
the writes land on a finished entry. Not a defect; under U2 (asked while the ward resolves) the write is that entry's own.

**Tests.** None changed; added to `TaxTakerTest`, `TitaniaRuggedRumblerTest`, `ErosionTest`, `ArrogantPoetTest`, `ForumNecroscribeTest`,
`VeinRipperTest`; new `ResolutionCostPaymentServiceTest`. Run (XML of this run): 1,496 files (nested suites counted), 13,194 tests,
0 failures: every card naming the 28 records whose handler or answer path changed (`--tests-for`), every
`Ward|Unless|MayPay|ForcedCost|Madness|Discard|Sacrifice|PayLife|CourageousResolve|Titania|ArcumsWhistle|Mastery|DeathReplacement`
class, Tax Taker, Sylvan Library, Lim-Dûl's Vault, Zur's Weirding, `MayAbilityHandlerServiceTest`, both ratchets; then the new unit test.

**Seen, not changed** (defect queue, "defect run D25"):
- About 15 more "unless / may pay" pool payments raise no tax event (Whirlwind Denial, Tariff, Winter's Chill, Heroism, Reflect, the
  any-player `MayPayManaEffect` path, Emberwilde Djinn, ...). Sure; for U2 / U3 / U3b.
- Erosion's and Emberwilde Djinn's two-option prompts judge the mana from the pool only (CR 118.3, 605.3a). Sure.
- The morph face-up life cost and the attack / block life taxes check `canPlayerLifeChange` (CR 119.8; not resolution-time). Sure.

**For the next run.** U2: the other counter ransoms and `AnyOfCost`, each kind a case in `ResolutionCostPaymentService` (several
permanents, blight, evidence, poison, exile graveyard, hand, waterbend, damage) and in `UnlessPaysEffectHandler.ransom` and the record's
constructor check; the discard and sacrifice ransom handlers already pay through the service.

### Step 10, run U2 ("unless": the other counter ransoms) (2026-11-25)

The signed-off U2 plan (`scratchpad/s10-unless/u2-plan.md`) with the owner's three answers (Standing knowledge, "the unless runs"). Absorbed
into `UnlessPaysEffect` factories (Shared pieces): the 11 records `CounterUnlessDiscardsEffect`, `CounterUnlessSacrificesEffect`,
`CounterUnlessBlightsEffect`, `CounterUnlessCollectsEvidenceEffect`, `CounterUnlessDiscardsHandEffect`, `CounterUnlessDiscardsOrPaysEffect`,
`CounterUnlessExilesGraveyardEffect`, `CounterUnlessGetsPoisonCountersEffect`, `CounterUnlessTakesDamageEffect`, `CounterUnlessWaterbendsEffect`,
`ChangeTargetOfTargetSpellUnlessControllerPaysEffect`, their 11 normalfx and 11 mayfx handlers, the `CounterUnlessEffect` interface, eight
`MayPenaltyChoiceHandlerService` answers and its two counter helpers, the contexts `CounterUnlessBlightsCreatureChoice`, `WardWaterbendPayment`
and `SacrificePermanentsOrElse`. New: `AnyOfCost`, `ExileGraveyardCost`, `GetPoisonCountersCost`, `DamageFromSourceCost`, `UnlessAnswer`, one
`ResolutionCostPayment` context pair. Whirlwind Denial kept (its payment is `payManaFromPool`); Ice Cave to U3. Ledger: 11 absorbed, 1 dead,
4 survivors, 1 unique, 1 blocked. Ratchet 2,991 / 2,416 → 2,984 / 2,394. No `CONVERSION_VERSION` bump (no mapper or enum change).

**Engine.** A payment with a choice (discard, sacrifice, N-sacrifice, blight, evidence, waterbend taps) records the choice to pay on the
waiting entry and ends with the prompt; its completion pays through the shared action and resumes the entry through the epilogue, where
`EffectResolutionService` takes the entry's answer (U1's mana / life path too; `resolvedMayAccepted` is no longer read for it). The handler
reads the counter's own spell subject (Blood Funnel, Mana Vortex: payer `you()`, the triggering spell). `SacrificeMultiplePermanentsCost`
gained an optional `description`. Logs: "P pays …" / "P discards a card at random …" when paid at once, "P chooses to <cost>. X is not
countered." when a prompt pays it, "P declines to <cost>." otherwise. A `MayEffect` around an `UnlessPaysEffect` (Complicate's cycling
trigger) lists the wrapped effect on its "you may" question: `MayAbilityHandlerService` answers a ransom only when the waiting slot is the
`UnlessPaysEffect` itself (found by the orchestrator's sweep; U1 paid a phantom "0 life" there). `CostEffectClassificationTest` got a
`RESOLUTION_ONLY_COST_TYPES` list for the four new ransom-only costs.

**Behaviour changes** (failing-first, each seen failing on the old code, unless marked):
- (u1) An uncounterable target still asks for the ransom and paying spends it (owner's decision): Titania, Auntie Ool, Axebane Ferox,
  The Serpent Society, The Unagi of Kyoshi Island (`wardAsksWhenTheSpellCantBeCountered` in each test class, Sphinx of the Final Word).
- (u2) Saruman's ward isn't asked of a payer with no enchantment, instant or sorcery card; the spell is countered (CR 118.3, 608.2d).
- (u3) A paid blight keeps the spell (CR 118.12): Auntie Ool under an opposing Vorinclex puts one counter and Shock still resolves.
- (u4) Valgavoth: yes / no, then exactly three; "choose none to decline" is gone (two was refused before too: a pin).
- (u5) Divert offers its {2} when the payer's lands could make it (CR 118.3, 605.3a) and its payment raises the tax-paid event (Tax Taker).
- Whirlwind Denial's {4} and ward waterbend's mana part raise the tax-paid event (Tax Taker's Oracle: "the amount of mana they paid").
- (u6) Grip of Amnesia against a spell protected from it (Autumn's Veil) exiles the graveyard its controller chose to exile (CR 118.12).
- Titania asks in Oracle order, the discard first, then {2} (owner's decision 1; was mana first; the tests that changed, below).
- Molten Influence's 4 damage is dealt by the spell entry itself (CR 608.2h; as before, pinned); no longer a `DamageDealingEffect` (AI).

**Pins** (passed before and after): Perplex paid with an empty hand; Grip with an empty graveyard, then its draw; Axebane's evidence
exiles and triggers Evidence Examiner; Auntie Ool's blight triggers her own life loss; Molten Influence's damage counted to its source;
an uncounterable Mana Vortex still sacrifices; a Library of Leng question during a ward discard keeps the paid branch.

**Tests changed.** `TitaniaRuggedRumblerTest` (4 tests) and `TaxTakerTest.titaniaWardPayment` answered the mana prompt first; they now
decline the discard first (owner's decision 1, Oracle order). Added: the tests above, `ValgavothTerrorEaterTest`'s exact-three pin,
`ResolutionCostPaymentServiceTest` (any-of, empty-zone and poison payability), `GameDataDeepCopyTest` (the entry's answer, the evidence flag).

**Tests run** (XML of this run): every rewritten card's class (Glutton under Trespasser), the 46 classes referencing a rewritten card, every
`Ward|Unless|Counter|Guile|Madness|Discard|Sacrifice|Blight|Evidence|Poison|Waterbend|TaxTaker|Mastery|DeathReplacement` class, the
`CollectEvidenceEffect` cards, Ice Cave, Guile, Vorinclex, Carnage Tyrant, Gaea's Herald, Hindering Touch, Terra Stomper, Library of Leng,
the application tests the plan names, both ratchets, the whole AI module (counts in the run's report). Compiled every module's main and
test sources. Oracle text checked with `get_card` for all 32 card files (Glutton through Trespasser); rulings for Valgavoth, Grip, Auntie
Ool, Divert, Perplex, Molten Influence, Titania, Library of Leng.

**Seen, not changed** (defect queue, "step 10 run U2"):
- Library of Leng's replacement is offered for a discard paid as a cost; its ruling says costs aren't effects. Sure of the ruling.
- Ward—Waterbend's payability counts the pool, not mana the payer could still make (CR 605.3a). Sure of the code.
- Who "puts" Serpent Society's poison counters (the ward's controller) and who causes Perplex's discard (the payer) kept as they were. Unsure.

**For the next run.** U3: `MayPayManaEffect` & co. as `UnlessPaysEffect` factories, `MayPayPayer` → `PlayerSubject`, the any-player loop
(every player asked, CR 101.4) and Ice Cave; ask a payer unless no payment is possible at all (owner's D17 L34 rule). A new ransom is a case
in `ResolutionCostPaymentService` (`canPay`, `pay`, a completion if it prompts) and in `UnlessPaysEffect`'s constructor check and the
handler's `ransom` words; the choice lives on the entry, so a new prompt path must resume the entry through the epilogue.

### Step 10, run U3a ("unless": single-payer "may pay") (2026-11-25)

The signed-off U3 plan (`scratchpad/s10-unless/u3-plan.md`) with the owner's four answers (Standing knowledge, "the unless runs", U3).
Absorbed into `UnlessPaysEffect` (Shared pieces): `MayPayLifeEffect` (13 cards, both handlers), `MayPayLifeEqualToAmountEffect` (4, its
handler), `TargetPlayerMayPayManaOrLifeEffect` (Emberwilde Djinn, its handler, `ChoiceContext.TargetPlayerManaOrLifePaymentChoice` and its
answer); 371 files of `MayPayManaEffect` (its single-payer mana forms). New: `couldPossiblyPay` / `hasManaSource`, the "may pay" factories,
`CountScope.ACTIVE_PLAYER` (Champions of Minas Tirith's "cards in their hand"). Ledger: 3 absorbed, 3 survivors (`MayPayManaEffect`,
`MayPayPayer`, `UnlessPaysEffect`). Ratchet 2,984 / 2,394 → 2,981 / 2,390. No `CONVERSION_VERSION` bump (no mapper or `Keyword` /
`CardSubtype` / `CardType` / `CardSupertype` change; `CountScope` isn't stored).

**Engine.** (k1) `ifNotPaid` may be `null`, `prompt` optional. (k2) The chosen branch is spliced in as the next step and resolved through
the loop (was dispatched directly: Relentless Dead's `ConditionalEffect` and Wandering Archaic's `MayEffect` branches need it). (k3) A paid
answer marks "do this only once each turn" and a marked one isn't asked (CR 603.2h). (k4) `SpellEvaluator` scores the record value(ifPaid) −
value(ifNotPaid); the AI and the eleven engine readers of `MayPayManaEffect` (plan section 1) look into both branches. (k5) The tax is the
mana spent (D25). (k7) Wyll, Tivash, Tymna, Madame Null and the engine-built payments (Transmute, Crooked Scales, the death follow-up, the
end-step "pay {3}{B}", the top-card cast) insert an `UnlessPaysEffect` step. `couldPossiblyPay` replaces `canPay` as the question gate in
`UnlessPaysEffectHandler` (U1 / U2's paths too).

**Card rewrite.** 388 files: 373 by a scratch script (balanced parentheses, 9 shapes, loud failure otherwise, reader and leftover-name
checks; deleted); 15 by hand (the plan's eight, the four life-equal-to cards, Fear My Authority, Meathook Massacre II, Emberwilde Djinn),
and 7 engine sites. Prompts: 127 informative ones kept (`.withPrompt`), 11 that only said "Pay {X}?" dropped. Oracle text checked with
`get_card` for all 388 (back faces through their front card; Jasconian Isle as CMB1 117).

**Behaviour changes** (failing-first, each seen failing on the old code):
- (f1) A payer with no mana source and a short pool isn't asked; "if you don't" happens at once (CR 118.3, 608.2d; owner's rule).
  `FlightSpellbombTest.noPromptWithoutAnyWayToPay`, `KnightOfTheMistsTest.destroysAtOnceWithoutAnyWayToPay`.
- (f2) A payer whose only source is Elvish Spirit Guide in hand is asked and pays by exiling it during the prompt (CR 605.3a).
  `ManaLeakTest.payerWithOnlyASpiritGuideInHandIsAskedAndPays`.
- (f3) The same with Channel's permission. `ManaLeakTest.payerWithOnlyChannelsPermissionIsAskedAndPays`.
- (f4) Relentless Dead whose card left and came back is still asked; paying spends {B} and returns nothing (CR 118.12; owner's decision 2).
  `RelentlessDeadTest.oldDeathTriggerDoesNotReturnNewGraveyardObject` (changed).
- (f5) Emberwilde Djinn asks {R}{R}, then 2 life, one yes/no each (owner's decision U2-1); was one three-option choice.
- (f6) A payer with too few lands is asked (no tier B). `ManaLeakTest.payerWithTooFewLandsIsStillAsked`.
- Fear My Authority's "unless you discard a card or pay 3 life" is an `AnyOfCost`: an empty hand isn't offered the discard (CR 118.3).
- A "may pay" asked of another player (Horn of Plenty, Smothering Tithe, Kazuul, …) now pays through `ResolutionCostPaymentService`: the tax
  is the mana spent (D25), and its decline / shortfall log is the survivor's ("declines to pay {2}{W}", Treva and Skyline Scout tests).
**Pins** (before and after): K'rrik paying Nihil Spellbomb's {B} with life; the hard AI paying Flight Spellbomb's {U} and declining Knight
of the Mists' {U} that would save an opposing Knight; existing: Inalla, Valentin, Lich's Duel Mastery, Iron Star, the plan's others.

**Tests changed** (406 methods in 269 files, each against the card's Oracle text and the owner's rule): mana moved before the prompt so it
is asked, 58; "not asked, and the decline / 'if you don't' happens", 290 (DisplayNames renamed where they said "accepting"); a mana source
added to one test, 18 (a land, or an Elvish Spirit Guide in hand where battlefield indices matter), and to 55 whole classes through the new
`BaseCardTest.giveManaSource` (Channel's permission, no zone touched) where combat or upkeep flows answer with mana added while asked; U1 /
U2 tests that now ask a payer with too few sources,  10 (Power Sink, Brass\'s Tunnel-Grinder, Reasonable Doubt, Ayesha Tanaka, Mirari's Divert, Wandering Archaic's
opponent given {2}). Emberwilde's four list-choice tests and Lich's Duel Mastery's Emberwilde test answer two yes/no questions.
`EasyAiDecisionEngineTest`'s AI with no source isn't asked; an allowlist entry went. Harness: `activateHandAbility` /
`payLifeForColorlessMana` keep an open may-pay mana prompt (as `tapPermanent`).
**Tests run** (XML of this run, counted by file): the plan's selection, 1,164 classes (section 6: the rewritten cards, the 608 classes
naming one, the name-pattern classes, every `UnlessPaysEffect` / `MayPayManaEffect` card, the application tests, both ratchets) and the
whole AI module: 1,347 files, 12,768 tests, 0 failures (the last 7 failures fixed and their 4 classes rerun); the orchestrator\'s sweep 188 found 9 more in Cyclone and Brass\'s Tunnel-Grinder (same cause), fixed, with their siblings 5 classes, 49 tests, 0 failures. Compiled every module's main
and test sources and all 26 card-test modules.
**Seen, not changed** (defect queue): Gix only for your creatures (multiplayer; sure); the hard AI declines a ward on its own spell (sure of
the code); graveyard / exile / command mana abilities count as sources but can't be used while asked (none exist).

**For the next run.** U3b': the any-player mana loop as an `UnlessPaysEffect` whose payer subject names several players (each asked in APNAP
order, every one even after one paid, CR 101.4; the loop state on the entry's `UnlessAnswer`, the tax event), Ice Cave, `AllOfCost` and the
cost-as-if-you-do payments, the Chain family and Rhystic Lightning's payers, `MayPayTapPermanentsEffect`,
`MayPayManaAndSacrificePermanentEffect`; then `MayPayPayer` and `MayPayManaEffectHandler`'s controller code go. Tests that need a "you may
pay" asked with an empty pool give the payer a land (or an Elvish Spirit Guide in hand, which keeps battlefield indices) or call
`giveManaSource`.

### Step 10, run U3b' ("unless": any-player and combined "may pay") (2026-11-25)

The signed-off plan (`scratchpad/s10-unless/u3b-plan.md`) with the owner's three answers (Standing knowledge, "the unless runs", U3b').
Absorbed into `UnlessPaysEffect`: `CounterUnlessOtherPlayerPaysManaCostOnSpellCastEffect`, `MayPayTapPermanentsEffect`,
`MayPayManaAndSacrificePermanentEffect`, `MayPayTapAndSacrificePermanentEffect`, `DiscardRandomCardThenUntapEnchantedCreatureEffect` and their
six handlers, `MayAbilityTapCostService`, `PermanentChoiceContext.MayAbilityTapCostChoice`, `MayChoicePlayer.LAST_MANA_PAYMENT_PLAYER`,
`StackEntry.lastManaPaymentPlayerId`, `GameData.anyPlayerMayPayMana{RemainingPlayers,Paid}`; `ExileDyingCreatureCardAndCreateTokenEffect` became
`CreateTokenLinkedToCardsExiledThisWayEffect` (its exile is Timothar's payment). New: `AllOfCost`, `ExileCardsCost`, `PayManaCost.ofTriggeringSpell()`,
the several-payer `UnlessAnswer`, `UnlessPaysEffectHandler.nextStep` / `Step`. `MayPayManaEffect` keeps only `anyPlayerMay`, `MayPayPayer` only
`ANY_PLAYER` / `ANY_OPPONENT` (U3c). Ratchet 2,981 / 2,390 → 2,978 / 2,384. Ledger: 6 absorbed, 1 unique, 3 survivors, 2 blocked (U3c). No
`CONVERSION_VERSION` bump (no mapper or `Keyword` / `CardSubtype` / `CardType` / `CardSupertype` change).

**Engine.** Payers are `players(payer)` for one or several (owner's decision 2); `nextStep` drops a payer who left or has no way to pay, asks the
next, and picks the branch once after the last answer; the tax is per payer (D25). `AllOfCost`: `canPayNow` for all parts, mana first, then the
others in Oracle order, an asking part's successors on `UnlessAnswer.partsToPay` (A Golden Opportunity). Service cases `SacrificeSelfCost`,
`ExileNCardsFromGraveyardCost`, `TapMultiplePermanentsCost` (today's one-at-a-time picks), `ExileCardsCost`; `canPay` / `couldPossiblyPay` take the
entry; sacrifice candidates are judged from the entry's source (Three Dog's "attached to Three Dog") and honour `excludeSource`.
`QueueReflexiveAbilityEffectHandler` carries the sacrificed snapshot to the reflexive record. Soul Strings' handler inserts the
`UnlessPaysEffect` in place of its return. The Chain copier reads the target-based `MayChoicePlayer`. `MayAbilityHandlerService` keeps its
generic mana branch for `MayPayForArtifactsOrCreaturesCantAttackThisCombatEffect` alone (U3b); the tap branch is gone.

**Behaviour changes** (failing-first, seen failing on the old code unless marked):
- An opponent paying Rhystic Tutor's {2} raises the tax-paid event (Tax Taker). `TaxTakerTest.rhysticTutorPayment`.
- In the any-player loop a player with no mana source and an empty pool isn't asked (CR 118.3, 608.2d; owner's rule). `RhysticTutorTest.playerWithNoWayToPayIsNotAsked`.
- Three Dog's copies come from a reflexive trigger on the stack (CR 603.12; its ruling). `ThreeDogGalaxyNewsDJTest.copiesComeFromAReflexiveTrigger`.
- A tap payer with too few untapped matching permanents isn't asked and "if you don't" happens (CR 118.3, 608.2d). `CommandBridgeTest.noUntappedPermanentSacrificesUnasked`.
- Ultimecia has no intervening "if" (Oracle); seven cards and a Lotus Petal: asked (owner's decision 1), and sacrificing the Petal for the
  last {B} while asked fills the graveyard and the payment is made (the engine lets that mana pay). `sevenCardsAndAManaSourceThatFillsTheGraveyardIsAsked` (failed first), `lotusPetalSacrificedWhileAskedPays…` (written after).
- Apathy's random discard counts as caused by the Aura's controller, the payer's opponent (owner's decision 3; Apathy's and Obstinate Baloth's
  rulings agree). `ApathyTest.discardIsCausedByTheAurasControllerOpponent` (not run on the old code; its handler set it self-caused).
- Prompts: Rhystic Scrying "Pay {2} to make the caster discard three cards?", Scarwood Bandits names what is prevented (the old text spoke
  to the controller).
- Pins that passed on the old code too: the mana-and-sacrifice payer may tap a land while asked (Sokenzan); Ultimecia's payer picks the
  eight cards; the hard AI declines its own Rhystic Tutor ransom and a payment another player already made.

**Pins** (passed before and after): Rhystic Scrying / Wild Might "one pays, the other is still asked"; Banon with an empty hand, Timothar with
the card gone and Canoptek after it left (no mana source) aren't asked; Ice Cave and a face-down spell; the existing Soul Strings, Rhystic Cave,
Chain, Wolf (discarded-card value), Three Dog (sacrificed Aura copied), Kitt Kanto, Aziza, Command Bridge / Koskun decline tests.

**Tests changed.** 28 methods in 13 tap / discard classes: "not asked, and the outcome follows" (renamed DisplayNames); 2 "declining" tests got a
Forest so they are asked; mana sources (`giveManaSource`, BeforeEach or per test) in 27 classes whose flows add mana after the prompt opens
(the owner's rule), and the tests that answered the old order (Tax Taker, hard AI) add the mana first; `EchocastingSymposiumTest` (Aziza alone
can't tap three: not asked). `KoyaDeathFromAboveTest` got a mana source: its end-step "pay {3}{B}" (U3a) had no payer source, so it isn't asked
under U3a's rule too. Application: 8 test classes rebuilt with the factories; `MayPayManaEffectHandlerTest` lost its 10 mana / controller cases
(payers are subjects now; loop cases added to `UnlessPaysEffectHandlerTest`); `MayAbilityHandlerServiceTest` lost the tap-cost once-each-turn case
(the handler's paid answer marks it); `EffectResolutionServiceTest`'s nested-input re-entry now uses `MayEffect`; `GameDataDeepCopyTest`.

**Tests run** (XML of this run): 1,105 classes plus the whole AI module (1,291 files, 11,924 tests): the 37 card classes, Soul Strings, the 121
classes naming one, every `UnlessPaysEffect` / `MayPayManaEffect` card's class (U3c's 19 included), the name-pattern classes, the application
tests the plan names, both ratchets; 24 failures, all fixed, their 13 classes rerun (98 tests, 0 failures). Compiled every module's main and
test sources and all card-test modules. The orchestrator's sweep 189 found 4 more (Transmute Artifact, Jadzi: engine-built "you may pay" sites of U3a asking a payer with no mana source; the same gate as at HEAD, by reading, so U3a's): those tests now say "not asked" or give the payer a source (Puppet Master too); the 30 classes of every engine-built payment site and their referrers, 1 run plus a rerun of the 7 changed / failing ones (61 tests, 0 failures). Oracle text checked with `get_card` for all 37 card files and Jadzi; rulings for Apathy, Obstinate Baloth,
Wilt-Leaf Liege, Timothar.

**Seen, not changed** (defect queue, "step 10 run U3b'"): Timothar's link covers one Bat only when a replacement makes several (ruling: all;
sure); Ice Cave and Phyrexian symbols (unsure); "Choose a Aura" in the sacrifice prompt (cosmetic). `PendingMayAbility.tapPermanentsCost` is
null everywhere (drop with the record's next change).

**For the next run.** U3c: the 19 `anyPlayerMay` non-mana payments onto the same `UnlessAnswer` loop as a batched mode (choose in APNAP order, pay
together), then `MayPayManaEffect`, `MayPayPayer` and `MayPayManaEffectHandler` go (`apnapOpponents` moves to a support class). Tests that rely
on a payer being asked need a mana source (`giveManaSource`) or the resources at the time the question is asked.

### Step 10, run U3c ("unless": batched any-player payments) (2026-11-25)

The plan (`scratchpad/s10-unless/u3c-plan.md`) with the orchestrator's five decisions (The owner's decisions, "the unless runs", U3c).
Absorbed into `UnlessPaysEffect`: `MayPayManaEffect` (+ `PayerLoop`, `MayPayManaEffectHandler`, `MayAbilityHandlerService`'s non-mana
branch) and `MayPayPayer`; `EachPlayerPaysAnyManaEffect` lost `exactly` / `lifeExactly`. New: `.payingTogether()`, `UnlessAnswer.choices`,
`unlessPays(PlayerSubject, CostEffect, effect)`, `SourceControllerDrawsCost`, `DamageFromSourceCost.of`, `ResolutionCostPaymentService.choose` /
`completePick` / `payTogether`, `SubjectResolutionService.apnapOpponents` (13 files, 14 calls, by a scratch script, deleted), the sacrifice guards.
Ratchet 2,978 / 2,384 → 2,978 / 2,383. Ledger: 2 absorbed, 2 survivors, 1 unique. No `CONVERSION_VERSION` bump (no mapper or enum change).

**Cards.** 19 files by hand: 15 `.payingTogether()` (prompts kept), the PLC four on the plain loop with the derived prompt. Oracle text
checked with `get_card` for all 19; rulings for Sigarda and Research // Development (the plan checked the rest).

**Behaviour changes** (failing-first, seen failing on the old code unless marked):
- A sacrifice ransom offers only what the payer could sacrifice (CR 608.2d, 701.21a), for U2 / U3b' too: under Sigarda an opponent's ward
  isn't asked and counters (its ruling: "you can't take that option"), `VeinRipperTest.sigardaKeepsThePayerFromTakingTheSacrificeOption`;
  a creature that can't be sacrificed doesn't pay, `creatureThatCantBeSacrificedDoesNotPayTheWard`; The Master's tokens don't pay Blood
  Funnel, `BloodFunnelTest.protectedCreatureTokensCantPayTheSacrifice`.
- The base AI declines a paid-together ransom another player chose (3+ players),
  `AnyPlayerPaymentAiTest.baseAiDeclinesASacrificeAnotherOpponentAlreadyChose`.
- The hard AI subtracts a non-mana ransom's estimate and never pays life or damage at or above its life
  (`hardAiDoesNotPayDamageEqualToItsLife`, passed on the old code too: it scored the old record as free and declined).
- The PLC four pay as each player chooses but counter once after the last answer (was: at the first payment); unobservable, CR 603.3.
- Brain Gorgers' lone creature is now picked through the shared prompt (U2's sacrifice ransom, seen below); its log is the survivor's
  ("chooses to sacrifice a creature", "Bob sacrifices Gravebane Zombie"); the old "accepts …'s offer" / "declines" lines are gone.

**Pins** (passed before and after): Clackbridge's two sacrifices die together (a sacrificed Blood Artist sees the other die), Desecration
Demon's one counter for two sacrifices, Vexing Devil prevented still sacrificed, Phantasmagorian's discarded Fiery Temper's madness (the
count-3 discard completion), Development three rounds with a different opponent and two opponents in one round (one card, no token),
Brain Gorgers under The Master; the existing card tests (Dash Hopes' 4 life, Temporal Extortion's half of 7, Pangolin, Kraken, Gitrog).

**Tests changed** (each against the Oracle text and decision 1): `BrainGorgersTest` 5 methods (answer the one-creature pick; the
"counters at once" case now asserts the counter after the last answer), `DashHopesTest.bothPlayersMayPay`,
`TemporalExtortionTest.bothPlayersMayPay`, `LichsDuelMasteryLifePaymentEffectTest.dashHopesPaymentAsksTheMasteryFirst` (player 2 still
asked before the counter), `GravebaneZombieTest` 3 Brain Gorgers methods (the pick, the survivor's sacrifice log line, the counter after the
last answer). `MayPayManaEffectHandlerTest` deleted: its cases are `UnlessPaysEffectHandlerTest`'s paid-together ones and
`ResolutionCostPaymentServiceTest.PaidTogether` (choose, payTogether, the guards) and the count-3 discard. Comments in 11 card tests and
unused imports in 7 application tests renamed or dropped; `CostEffectClassificationTest` lists the new cost.

**Tests run** (XML of this run): 1,160 classes (the 19 cards and the 30 classes naming them, every `UnlessPaysEffect` card, the cards of the
22 records whose handler or path changed, the Sigarda / can't-be-sacrificed / The Master cards, the name-pattern classes, the application
tests, both ratchets) plus the whole AI module: 12,783 tests, 3 failures (Gravebane Zombie, fixed; its class rerun, 81 tests, 0 failures).
Compiled every module's main and test sources and all card-test modules.

**Seen, not changed** (defect queue, "step 10 run U3c"): Development's draw per round (owner to review); a one-permanent sacrifice ransom
paid at once asks the pick (CR 608.2d convention; cosmetic); Kraken's ransom text says "permanent" (cosmetic).

**For the next run.** U3b: the single-verb "X unless pays" records and May's 11 onto `UnlessPaysEffect`; a ransom chosen first and paid with
the others is `.payingTogether()` with a `choose` / `payTogether` case in `ResolutionCostPaymentService`; "unless its controllers draw" can
take `SourceControllerDrawsCost`.

### Step 10, run U3b1 ("unless": single-verb records) (2026-11-26)

The plan's part 1 (`scratchpad/s10-unless/u3bb-plan.md`, surveys `u3bb/group-{A..D}.md`) with the orchestrator's U3b decisions (owner's
decisions, "the unless runs", U3b). Absorbed into `UnlessPaysEffect` (existing factories; compositions in the ledger notes): the plan's 25
records, their 43 handlers (25 normalfx, 18 mayfx), 11 `MayPenaltyChoiceHandlerService` methods, `ChoiceContext.EnchantedPermanentManaOr-
LifePaymentChoice` and `OathOfLimDulPenaltyChoice` with their `ChoiceHandlerService` / AI branches, two `MaySacrificeForCounterSupport`
methods. Card-only: Spelltithe Enforcer, Rogue Skycaptain, Painful Quandary. Ratchet 2,978 / 2,383 → 2,953 / 2,340; ledger 25 absorbed. No
`CONVERSION_VERSION` bump (no mapper or `Keyword` / `CardSubtype` / `CardType` / `CardSupertype` change).
**Engine.** `DiscardHandCost` pays through `DiscardHandEffectHandler` in the service; the sacrifice filter context names the entry's target's
card (Torment of Venom's "another"); new condition `OpponentDealtCombatDamageThisTurn` (Tymna at X = 0, Q4); hard AI: an any-of ransom
priced by the alternative asked, a branch making the payer lose life / take damage on the ransom's life scale, `ExileNCardsFromGraveyardCost`
priced. Harness: `BaseCardTest.payUnlessAlternative`. **Cards.** 44 files by hand; Oracle text checked with `get_card` for all 44 (Tergrid's Lantern as KHM 112); rulings for Nazgûl Battle-Mace,
Aether Rift, Spelltithe Enforcer, Primordial Ooze, Esper Sentinel, Ravenous Vampire. Prompts are the ransom's ("Pay {2}?") except Rhystic
Study and Mystic Remora.
**Behaviour changes** (failing-first, each seen failing on the old code unless marked):
- Erosion offers {1} (then 1 life) while an untapped land could make it (CR 605.3a): `controllerWhoCantLoseLifeIsOfferedManaFrom…`.
- A payer with no way at all to make mana isn't asked (owner's rule; CR 118.3, 608.2d): Arcum's Whistle
  (`playerWithNoManaSourceIsNotAsked`); Pacts, Carrionette (written after); the Ooze, Musician, Sphere, Rogue Skycaptain the same.
- Aether Rift asks every player, even after one paid (its ruling, CR 101.4): `everyPlayerIsAskedEvenAfterOnePaid`.
- Excise, Carrionette: an opponent's payment is a tax (Tax Taker): `paymentIsATaxPaidToTheCaster` / `…ToTheController`.
- Nazgûl Battle-Mace: a card Rest in Peace exiled instead is still stolen (its ruling): `cardExiledInsteadOfGoingToTheGraveyardIsStillStolen`.
- Ravenous Vampire's tap is the tap action ("becomes tapped" triggers fire); Spelltithe Enforcer asks only the casting opponent (Oracle
  "that player", Q3): `declineTapTriggersBecomesTapped`, `onlyTheCasterIsAsked`.
- Written after: Oath honours "can't be sacrificed" (Hithlain Rope); a gone Sphere / Musician target is still asked for its last counters
  (CR 608.2h); Urza's Tome exiles a lone historic card unasked (CR 608.2d); Aether Rift asks when Library of Leng put the discarded creature
  card on the library (CR 118.12a); Soul Barrier's damage keeps its real controller once it left (no test).
**Pins** (passed before and after): Erosion's life with an untapped land; Slaughter Pact's undestroyed target still owes; Prince of Thralls'
card that left isn't returned but the payment is asked; the existing Study / Remora, Oath, Torment, Indoraptor, Withdraw, Tymna X = 0,
Musician tests. AI pin `UnlessAlternativeRansomAiTest` (written after).
**Tests changed** (against the Oracle text or the owner's rule; about 110 methods in 31 card classes): `giveManaSource` for payers with no
source (62 methods, 12 classes); the Torment family, Tergrid, the Survivors, Ominous Sphinx, Lich's Duel Mastery, Erosion and Tax Taker answer
the alternatives one at a time; Oath and Essence Vortex the shared sacrifice prompt (a lone candidate unasked); Vampire, Crovax, Indoraptor
and Glimpse answer the forced one-permanent pick (U3c's line); Urza's Tome no pick; Aether Rift's two ruling-wrong tests; Painful Quandary's
and Indoraptor's log lines; `MusicianTest`'s granted effect; `SpellTriggersTest`'s two samples; the two deleted records' handler tests went.
**Tests run** (XML of this run): 1,626 card classes (the 44 cards' own and 1,442 classes naming one of 867 cards: the 44, every
`UnlessPaysEffect` card, the touched paths' cards; the name-pattern classes), 17 application classes, the engine scan, the whole AI module:
1,808 files, 16,756 tests, 4 failures (Tax Taker, Lich's Duel Mastery ×2, Glimpse; the same causes), fixed, rerun 101 tests, 0 failures.
Compiled every module's main and test sources and all card-test modules.
**Seen, not changed** (defect queue, "step 10 run U3b1"): Quenchable Fire, the Asps, Sabertooth Cobra raise no tax event (Q5, owner); the
one-permanent sacrifice pick now reaches Vampire, Crovax, Indoraptor (cosmetic); the AI values other payer-harming branches from its side.

**For the next run.** U3b2 (part 2): `ReturnPermanentToHandCost`, `PutCardFromHandOnTopOfLibraryCost`, `LandDropCost` as ransoms (each a
case in `requirePayableAtResolution`, `UnlessPaysEffectHandler.ransom`, the service and `SpellEvaluator.ransomCost`), the payer-declared
target, `eachUnless` + `PlayerCount`, `SacrificeCreaturesWithTotalPowerCost`, Overencumbered, Cyclone, the three rules fixes. Tests answer an
any-of ransom with `payUnlessAlternative` and give a payer asked with an empty pool `giveManaSource`.

### Step 10, run U3b2 ("unless": single-verb records) (2026-11-26)

The plan's part 2 (`scratchpad/s10-unless/u3bb-plan.md`, surveys `u3bb/group-{B,C,D}.md`) with the orchestrator's U3b decisions (Q1 per-payer
branch, Q2 the three fixes). Absorbed into `UnlessPaysEffect`: the plan's 15 records and their 22 handlers (15 normalfx, 7 mayfx), three
`MayPenaltyChoiceHandlerService` methods, `MayAbilityHandlerService`'s generic mana block, `EffectResolutionService`'s land-drop branch,
`AttackLegalityService`'s Overencumbered check, `IndulgentTormentorState` / `ChoiceContext.IndulgentTormentorChoice`,
`EachPlayerSacrificeOrDiscardState`, `PermanentChoiceContext.Bounce(Own)PermanentOrSacrificeSelf`, `MultiPermanentChoiceContext.SacrificeCreaturesWithTotalPowerOrSacrificeSource`.
Card-only: Lim-Dûl's Hex (`EachPlayerTakesDamageUnlessPaysEffect` stays for Stench of Evil, U6). Ratchet 2,953 / 2,340 → 2,939 / 2,318; ledger 15
absorbed, 1 unique (`SacrificeCreaturesWithTotalPowerCost`), 1 blocked-other (the Hex record). No `CONVERSION_VERSION` bump (no mapper or enum change).
**Engine.** `UnlessPaysEffect.eachUnless(payers, ransom, players -> X)`; `UnlessAnswer.paidPlayerIds` (was `anyPaid`), set as the entry's accepted players
by `nextStep` before every branch; a payer `targetPlayer()` declares a harmful player target when no branch does; ransoms `ReturnPermanentToHandCost`
(`youControl` / `anyPlayers`, a `description`), `PutCardFromHandOnTopOfLibraryCost`, `LandDropCost`, new `SacrificeCreaturesWithTotalPowerCost`, each
a case in the record, the handler's words, `ResolutionCostPaymentService` and `SpellEvaluator.ransomCost`; `PayManaCost.timesPaid` / `perAmount`;
`PlayerCount(PlayerSubject)` (`AmountEvaluationService`, lazy `SubjectResolutionService`); `CantAttackThisTurnEffect` takes `UNTIL_END_OF_COMBAT` for
a set ("this combat" log); the target-player sacrifice-or-token completion moved into `TargetPlayerSacrificesCreatureOrControllerCreatesTokenEffectHandler`.
AI: the multi-permanent strategy picks the most powerful creatures up to a total-power ransom.
**Cards.** 24 files by hand. Oracle text checked with `get_card` for 23 (Boulder Jockey's MB2 printing isn't in the feed: the survey's text); rulings for
Read the Runes, The Dalek Emperor, Acererak, Phyrexian Dreadnought, Drake Familiar, Overencumbered, Possessed Portal, Cyclone. CR 101.4, 701.55b verified.
**Behaviour changes** (failing-first, each seen failing on the old code: 15 tests):
- Rishadans, The Long Reach of Night, Lim-Dûl's Hex, Acererak: every payer chooses, then the sacrifices / damage / tokens of those who didn't pay come
  together (CR 101.4; Acererak's ruling). `opponentsWhoDidNotPaySacrificeTogetherAfterTheLastAnswer`, `…DiscardSacrificeTogether…`,
  `damageIsDealtTogetherAfterTheLastAnswer`, `sacrificesAndTokensComeTogetherAfterEveryOpponentChose`.
- A Rishadan's mana is a tax paid to its controller (Tax Taker): `TaxTakerTest.rishadanCutpursePayment`.
- Lurking Spinecrawler's granted trigger: each opponent sacrifices a creature, those who can't discard (Oracle; was "unless they discard"): 2 tests.
- Read the Runes: sacrifice any number up to the cards drawn, then discard the rest, never forced to sacrifice (rulings): `sacrificesComeBeforeTheDiscards`.
- The Dalek Emperor is a villainous choice; a creatureless opponent may choose the sacrifice and no Dalek is made (CR 701.55b): `creaturelessOpponent…`.
- Phyrexian Dreadnought gone: the creatures may still be sacrificed (ruling): `creaturesMayStillBeSacrificedAfterDreadnoughtLeft`.
- Tragic Lesson: a land by an effect may be returned (CR 613.1d): `permanentThatIsALandByAnEffectCanBeReturned`.
- Boulder Jockey: no land drop left, not asked (CR 118.3, 608.2d): 2 tests. Overencumbered triggers only on the enchanted opponent's turn (Oracle) and
  isn't asked of a payer with no mana source (owner's rule): 2 tests. Not failing-first: Indulgent Tormentor asks each alternative.
**Pins** (passed before and after): the 24 cards' existing tests (Glint Hawk family, Drake Familiar, Tainted Specter, Athreos, Wyll, Cyclone), Withdraw, Prince / Mace.
**Tests changed** (against the Oracle text, the rules or the owner's rule): Phyrexian Dreadnought 14 methods (answer the yes/no first, decline with
no); Read the Runes 3 (sacrifices first, ruling), Possessed Portal 6, The Long Reach of Night 3, The Dalek Emperor 4 (villainous options), Indulgent
Tormentor 4 plus Gravebane Zombie / Lich's Duel Mastery Punisher 1 each (`payUnlessAlternative`; a lone creature is the shared pick, U3c's line), Lim-Dûl's
Hex 3 (damage after the last answer) and a `BeforeEach` mana source, Boulder Jockey 2, Rishadans 7 and Overencumbered 5 and Tax Taker 1 (`giveManaSource`),
Gravebane Zombie 6 (the new prompts; Portal and Long Reach's replacement now asked in the departure batch); `SacrificeUnlessReturnOwnPermanentTypeToHandEffectHandlerTest`
deleted; `CostEffectClassificationTest` lists the new cost. Added: `ResolutionCostPaymentServiceTest.OtherRansoms` (5), `UnlessPaysEffectHandlerTest` (4),
`SpellEvaluatorTest.otherRansomsAreNotFree`.
**Tests run** (XML of this run): 1,643 card-test classes (the 24 cards, every `UnlessPaysEffect` card and the cards of the touched paths: 914 cards,
the classes naming one; the name-pattern classes), 157 application files, the whole AI module (118 files): 1,918 files, 17,343 tests, 0 failures; then the
ratchet with the new baselines. Compiled every module's main and test sources and all card-test modules.
**Seen, not changed** (defect queue, "step 10 run U3b2"): Acererak's tokens follow the sacrifice step instead of the same event (ruling; unsure it is
observable); `nextStep` setting the accepted players could overwrite a Tempting Offer's list in one entry (no card has both).

**For the next run.** U4 (`ForcedCostOrElseEffect`) or U6: "for each [object] … unless" can build on `eachUnless` and `UnlessAnswer.paidPlayerIds`.

### Step 10, run U4a ("unless": forced costs, part A) (2026-11-26)

The plan's part A (`scratchpad/s10-unless/u4-plan.md`, survey `u4/inv.tsv`) with the orchestrator's U4 decisions (owner's decisions, "the
unless runs", U4). 121 card files moved off `ForcedCostOrElseEffect`: 54 literal "sacrifice unless you pay {cost}" by a scratch script
(one shape, balanced parentheses, failed on any other; deleted), 67 by hand. Ogre Marauder stays (its granted "can't be blocked" ability
needs a one-shot timed ability grant; U4b). Absorbed (split into steps): `DestroySourceAndDamageControllerIfDestroyedEffect` (Cosmic
Horror), `DealDamageToControllerThenTapSourceIfDamageDealtEffect` (Minion of Leshrac), their `DestructionSupport` branches. Ratchet
2,939 / 2,318 → 2,937 / 2,318. Ledger: 2 absorbed, `ForcedCostOrElseEffect` re-noted "U4b-d". No `CONVERSION_VERSION` bump. **Engine.** k1: `PayManaCost.forSourceManaCost()` as a ransom ("its mana cost": live, else last known, {X} as 0; face down or none
unpayable and not asked, CR 708.2, 118.6). k2: Carnophage, Sangrophage, Vile Consumption are `unlessYouPayLife`. Ransom words: a
description may carry its determiner ("another creature"), counts as words ("Sacrifice two Swamps"). `ResolutionCostPaymentService`: a
`SacrificeMultiplePermanentsCost` ransom sacrifices its picks together (`sacrificeTogether`, one event). Harness: `BaseCardTest.answerForcedPick`.
**Cards.** Payers: `you()`; `eachPlayer()` (Icy Prison, Nakaya Shade, Rhystic Circle, Rhystic Shield); `activePlayer()` for "that player" of
an upkeep (Mind Whip, Power Taint, Soul Tithe, Slow Motion, Mogis, Pillar Tombs, Blood Clock, Umbilicus, Fraying Line, Woebringer; the
Mind Whip pin shows today's handler asks the upkeep player too); `controllerOf(triggering())` (Seizures, the Chants, their prompt kept as
Oracle words); `triggeringPlayer()` (Tomb Blade); `targetPlayerDeclaredElsewhere()` (Demanding Dragon). Mandatory "do X. If you can't, Y"
(Archdemon of Greed, Eldrazi Monument, Infernal Denizen, Invoke Despair, Lair of the Ashen Idol, Rust Elemental, Woebringer):
`SacrificePermanentsEffect` + `ConditionalEffect(NotCondition(sacrificed()), Y)`; Infernal Denizen sacrifices only with two Swamps
(`ControlsPermanentCount`, CR 118.3, 118.12). Shuttle: `TargetPlayerChoosesOneEffect.villainousChoiceFacedBy(defendingPlayer(), …)`
(CR 701.55b). Oracle text checked with `get_card` for all 122 files; rulings for 13 (Mind Whip, Icy Prison, the Rhystics, Invoke Despair, …).
**Behaviour changes** (failing-first, seen failing on the old code unless marked):
- "Any player may pay" asks every player even after one paid (CR 101.4; Rhystic Shield's, Nakaya Shade's rulings): Icy Prison
  `everyPlayerIsAskedEvenAfterOnePaid`, Rhystic Shield / Nakaya Shade `currentPlayerPaysAndTheOtherPlayerIsStillAsked`, Rhystic Circle
  `controllerCanPayOptionalCost` (4 failed first).
- A payer with no mana source isn't asked (owner's rule, CR 118.3, 608.2d): `PhantasmalForcesTest.controllerWithNoManaSourceIsNotAsked`.
  A payer who can't pay the life isn't asked (Blood Clock, Umbilicus, Sangrophage at 1 life; CR 119.4): tests changed, not failing-first.
- Hecatomb's four creatures (and a "sacrifice N" ward) leave together (CR 603.10a): `HecatombTest.sacrificedCreatureSeesAllFourDeaths`
  failed on the service's one-at-a-time loop, passes with `sacrificeTogether`.
- Not failing-first: Fraying Line's exile is one event over `allOf(source(), creatures without a rope counter)` (CR 608.2c, 603.10a; no card
  in the pool observes the difference); prompts read the ransom ("Pay {U}?"); a lone sacrifice / -1/-1 candidate is the shared pick (U3c).
**Pins** (passed before and after): Tomb Blade and Pillar Tombs against hexproof (today's code already resolves: only the subject changed),
Mind Whip after a control change, Woebringer under Sigarda, Ogre Marauder attacking a planeswalker, `UpkeepRansomAiTest` (parity); the
existing death-replacement, Soul Tithe, Draco, Disruption Aura, Hecatomb, Garza's recover, Shuttle and Valeyard tests.
**Tests changed** (owner's rule, U3c's pick line, the Oracle text): `giveManaSource` in about 360 methods of 100 classes whose payer was
asked with no source; `answerForcedPick` in 44 methods; the Chants, Thing from the Deep, Plant Elemental, Rust Elemental, Archdemon answer
the pick the new prompt asks; Shuttle 7 and The Lunar Whale 1 answer the villainous list; Blood Clock, Umbilicus, Sangrophage not asked;
Elder Spawn's log filter skips the decline line; Hecatomb's trigger loop; DisplayNames added to about 30 touched methods.
**Tests run** (XML of this run): 2,177 files, 20,569 tests, 0 failures: every rewritten card's class and the classes naming one, every
`UnlessPaysEffect` card's, `u4/cardtests.txt`, the name-pattern classes, the readers of `DestructionSupport` / both handlers /
`ResolutionCostPaymentService`, the application tests the plan lists, both ratchets, the whole AI module (first run 424 failures, all from
the payer rule and the pick, fixed). Compiled every module's main and test sources and all card-test modules.
**Seen, not changed** (defect queue, "step 10 run U4a"): the hard AI scores a source-sacrifice branch as nothing (decision 6); Ogre's grant.

**For the next run.** U4b: the 34 new-ransom files (each kind a case in the record's check, the handler's words, the service and
`SpellEvaluator.ransomCost`), Egon / Charnel / Greenbelt as steps, Satya, Koya, Ashling, Flash, Ogre Marauder. Tests whose payer is asked
with an empty pool need `giveManaSource`; a lone-candidate pick needs `answerForcedPick`.

### Step 10, run U4b ("unless": forced costs, part B) (2026-11-26)

The plan's part B (`scratchpad/s10-unless/u4b-plan.md`) with the orchestrator's U4 decisions (4 Satya, 6 AI parity, 7 Flash). The last 38
card files naming `ForcedCostOrElseEffect` moved by hand (none names it now; echo and cumulative upkeep still build it), and its Satya, Ashling
and Flash builders. Split: Koya's `ExileTargetPermanentAndReturnAtNextEndStepUnlessPaysEffect` (+ handler) into `ExileTargetPermanentEffect` +
`RegisterDelayedStepTriggerEffect.aboutExiled(…, unlessYouPay("{3}{B}", PutCardsOntoBattlefieldEffect(cardsThisWay(EXILE)))).evenIfNothingHappened()`.
Absorbed: `PutCardFromGraveyardOnBottomOfLibraryCost` (`PutCardsFromGraveyardOnBottomOfLibraryCost(1)`). New: `PayEnergyEffect` (+ handler), the
amount `SourcePermanentManaValue`, `ReturnPermanentToHandCost.count`, `ChoiceContext.ResolutionCostCounterRemoval`,
`GraveyardTargetOperationState.resolutionCostLibraryBottom`. Ratchet 2,937 / 2,318 → 2,936 / 2,318. Ledger: 1 split, 1 absorbed, 1 unique, 3
blocked-other re-noted, `ForcedCostOrElseEffect` re-noted "U4c-d". No `CONVERSION_VERSION` bump (no mapper or enum change).
**Engine.** Nine ransom cases (record check, handler words, `ResolutionCostPaymentService.canPay` / `pay` / completion, `SpellEvaluator.ransomCost`),
each through the action its other payments use: energy `GameData.setPlayerEnergyCounters`; `ExileTopCardOfGraveyardCost` `GraveyardTopExileSupport`;
the two counter-removal costs `removeCounterFromPermanent`; graveyard-to-library-bottom and graveyard-to-hand the shared `GraveyardChoice` (a
multi-card choice for two); mill `resolveMillPlayer`; `PutTypedCounterOnSourceCost` `placeCounterOnPermanentForPlayer` by the payer; a return of
several one multi-pick and the shared bounce. Rotting Giant / Treacherous Vampire use `ExileNCardsFromGraveyardCost(1, null)`. Steps: Charnel Troll
`ExileGraveyardCardsEffect.fromOwnGraveyard(1, creature)` + conditionals on `exiled()`; Egon the exile of two inside `GraveyardCardThreshold(2)` (ruling:
one card isn't exiled); Greenbelt `PayEnergyEffect(2)` + `NotCondition(PAY_ENERGY)` (rulings). Ogre Marauder: `GrantEffectToTargetUntilEndOfTurnEffect(
StaticAbility.of(CantBeBlockedEffect), source())` (Lockjaw's grant). Flash: the creature put is the parked entry's CHOOSE step, the "unless" its next step.
**Cards.** Oracle text checked with `get_card` for all 42; rulings for Egon, Satya, Koya, Flash, Overseer, Greenbelt, Ogre. CR 101.4, 107.4f, 107.14,
118.3, 118.6, 118.12, 118.12a, 400.7, 603.10a, 603.12, 605.3a, 608.2d, 608.2h, 613.1f, 616.1, 708.2a verified.
**Behaviour changes:**
- Satya's delayed ability pays the token's mana value as it resolves, last known once it left (decision 4, CR 608.2h):
  `SatyaAetherfluxGeniusTest.paysTheTokensManaValueAsTheDelayedAbilityResolves` (failed first: paid 2 for a token now a Hill Giant copy).
- A Phyrexian symbol in a resolution mana ransom is paid with its colour, else 2 life (CR 107.4f); the service paid nothing for it before (Flash
  reached it now; Disruption Aura's and Ice Cave's "its mana cost" reach it too): `ResolutionCostPaymentServiceTest.ForcedCostRansoms.phyrexianManaIsPaidWithLife`
  (not run on the old code; by reading), Flash's two Phyrexian tests.
- A payer with no mana source isn't asked (owner's rule): Ashling's {W}{U}{B}{R}{G}, Flash. Prompts read the ransom ("Pay {E}{E}?"); a lone return
  or sacrifice candidate is the shared pick (U3c's line); Egon's two cards are picked one at a time (the shared exile step). The hard AI prices the
  new ransoms (energy 0.5, mill 0.25, a graveyard card 0.5, a counter 1, a return its mana value) instead of 0.
**Pins** (passed before and after): the 42 cards' existing tests (Egon's rulings, Greenbelt's single {E}, Cheering Crowd under Vorinclex and
Solemnity, Koya with no target, Ogre on a planeswalker and losing abilities, Flash's death replacement, Satya's 0-mana-value copy).
**Tests changed** (owner's rule, U3c's pick line): `answerForcedPick` or a single `PermanentChoice` in the 12 Karoo / Lair / Waterspout / Living Tsunami
classes (about 40 methods), Bull Elephant 3, Ovinomancer 2, Ogre 1; Egon 1 (two graveyard picks); a `giveManaSource` `@BeforeEach` in Ashling and
Flash, `giveManaSource` in `CycleOfLifeTest`'s Flash method; `CostEffectClassificationTest` drops the absorbed cost. Added: Satya 1,
`ResolutionCostPaymentServiceTest.ForcedCostRansoms` (7), `UnlessPaysEffectHandlerTest.forcedCostRansomsAskInTheirWords`,
`SpellEvaluatorTest.forcedCostRansomsAreNotFree`.
**Tests run** (XML of this run): 1,503 classes (the 42 cards and the 71 classes naming one, every `UnlessPaysEffect` card, `u4/cardtests.txt`, the
name-pattern classes, the readers of `DestructionSupport` / both handlers / the service / the choice services, the plan's application tests, every
architecture test, the whole AI module): 1,627 files, 15,172 tests, 1 failure (Cycle of Life's Flash method, the owner's rule; fixed, class rerun with
the ratchet, 13 tests, 0 failures). Compiled every module's main and test sources and all card-test modules.
**Seen, not changed** (defect queue, "step 10 run U4b"): a ransom's several returned permanents leave one by one, not as one departure (CR 603.10a;
not observable in the pool); Confiscation Coup now foldable, Behemoth of Vault 0 needs a reflexive ability about the declared target;
`ReturnMultiplePermanentsToHandCost` stays for 26 activation costs.

**For the next run.** U4c (echo): `PayEchoCost` as a ransom case with its alternative, hand-card and dynamic forms read as the ransom is read;
the `ForcedCostOrElseEffectHandler` / `MayPenaltyChoiceHandlerService` branches for energy, mill, graveyard and counter costs now serve only echo
and cumulative upkeep.

### Step 10, run U4c ("unless": echo) (2026-11-26)

The plan (`scratchpad/s10-unless/u4c-plan.md`) with the orchestrator's U4 decisions (the strict "could possibly pay" rule, hard AI
parity). `KeywordTriggers.echo` builds `UnlessPaysEffect(you(), PayEchoCost, SacrificeSelfEffect, ifPaid)`, so only cumulative
upkeep (U4d) builds `ForcedCostOrElseEffect`; its handler's `PayEchoCost` branch and echo `putMade` path are gone. No record or handler
added or removed: ratchet 2,936 / 2,318 unchanged. Ledger: `PayEchoCost` survivor, `ForcedCostOrElseEffect` re-noted "U4d". No
`CONVERSION_VERSION` bump (no mapper or enum change).
**The case.** Echo's cost kinds (Oracle text of all 51 echo cards and Thick-Skinned Goblin checked with `get_card`): fixed mana (47, Shah's
{0}), {X} = your life total (Volcano Hellion: `PayManaCost.withGenericIncrease`, read as it resolves, its ruling), discard a card (Deepcavern
Imp, Rakdos Headliner), sacrifice two lands (Skizzik Surger; its cost now says "lands" for the prompt). `UnlessPaysEffectHandler.alternatives`
reads `PayEchoCost` as that cost, then every existing path follows (words, `couldPossiblyPay`, `pay`, completion): each kind pays through the
case its cost type already has, no second payment path. `UnlessPaysEffect.requirePayableAtResolution` checks the named cost;
`SpellEvaluator.ransomCost` prices it as the named cost. "When this creature's echo cost is paid" (Shah) is `ifPaid` =
`QueueReflexiveAbilityEffect(…)`, a triggered ability the payment puts on the stack, as today's `putMade` did.
**Behaviour changes:**
- Thick-Skinned Goblin's "rather than pay the echo cost" offers {0}, then the echo cost (`AnyOfCost`): its ruling ("you may pay {0}, pay its
  echo cost, or choose not to pay"); today only {0} was offered. `ThickSkinnedGoblinTest.mayPayThePrintedEchoCostInsteadOfZero` (failed first).
- An echo mana payer with no mana source isn't asked and the permanent is sacrificed (owner's rule, CR 118.3, 608.2d):
  `KeywordTriggersTest.echoPayerWithNoManaSourceIsNotAsked` (failed first).
- Prompts and log lines are the ransom's ("Pay {1}{G}?", "Discard a card?" where the old discard prompt said "cumulative upkeep";
  `echoAsksInTheCostsWords` failed first); the two lands are the shared pick (U3c's line).
**Pins** (passed before and after, `KeywordTriggersTest`): echo for the player who gained control (Persuasion); a token copy has echo, the
copied original doesn't trigger; a face-down echo card has no echo (CR 708.2a); a control change in response makes the "if" false on
resolution, nothing asked (CR 603.4); a permanent that left in response is judged by last-known information, still asked, nothing
sacrificed (CR 608.2h; the condition's documented reading, owner's decision 2). Existing: Shah's separate trigger, Volcano Hellion's life
total in response, Skizzik's two lands, the Goblin with lost abilities or gone.
**Tests changed** (owner's rule, U3c's pick line): a `@BeforeEach` `giveManaSource` for both players in 46 echo card classes (their payer was
asked with no mana source), `giveManaSource` in 4 Volcano Hellion, 3 Thick-Skinned Goblin, 1 Thran Turbine and 1 `KeywordTriggersTest`
methods; `answerForcedPick` in 3 Skizzik methods; `CostEffectClassificationTest` lists `PayEchoCost` as resolution-only; DisplayNames on
the Goblin's 8 and Skizzik's 10 methods. Added: 7 `KeywordTriggersTest`, 1 Goblin, `UnlessPaysEffectHandlerTest.echoCostIsAskedAsTheCostItNames`,
`SpellEvaluatorTest.echoCostIsPricedAsTheCostItNames`. CR 101.4, 118.3, 118.12a, 603.2, 603.3a, 603.4, 603.12, 605.3a, 608.2d, 608.2h,
701.21a, 702.30a, 708.2a verified.
**Tests run** (XML of this run): 1,343 classes (`u4/cardtests.txt`, the 53 echo / Goblin / Skizzik classes and the 82 naming one, the
readers of `DestructionSupport` / both handlers / `KeywordTriggers` / the service, every `UnlessPaysEffect` card class, the name-pattern
classes, the plan's application tests, the architecture ratchets, the whole AI module): 1,439 files, 13,738 tests, 165 failures, all
the owner's rule in 47 classes (fixed; those 47 rerun: 354 tests, 0 failures). Compiled every module's main and test sources and all
card-test modules.
**Seen, not changed** (defect queue, "step 10 run U4c"): `ORACLE_TEXT_EFFECT_MAP.md` still shows `ForcedCostOrElseEffect` shapes for
cards U4a moved (docs only); the hard AI still never pays echo (decision 6's line).

**For the next run.** U4d (cumulative upkeep): `CumulativeUpkeepEffectHandler` is the last builder; its paid trigger ("whenever ... is
paid") can be `ifPaid` = `QueueReflexiveAbilityEffect` as echo's is; Braid of Fire's mana is the payment itself. Then the record, both
handlers, `MayPenaltyChoiceHandlerService`'s branch and `DestructionSupport.resolveForcedCostElseEffects` go.

### Step 10, run U4d ("unless": cumulative upkeep) (2026-11-26)

The plan (`scratchpad/s10-unless/u4d-plan.md`) with the orchestrator's U4 decisions (the strict "could possibly pay" rule, hard AI
parity). `CumulativeUpkeepEffectHandler` puts the age counter, reads the counters then (CR 702.24b) and splices
`UnlessPaysEffect(you(), cost x age counters, SacrificeSelfEffect, paid)` as the next step of the same resolution (CR 702.24a,
118.12a); the "doesn't pay" / "is paid" abilities are `QueueReflexiveAbilityEffect`s (triggers, CR 603.2, 603.12), Braid of Fire's
`AwardManaEffect` the payment itself (its ruling). Deleted: `ForcedCostOrElseEffect`, `ForcedCostOrElseEffectHandler`,
`mayfx/ForcedCostOrElseHandler`, `MayPenaltyChoiceHandlerService`'s branch, `DestructionSupport.resolveForcedCostElseEffects` /
`completeForcedCostOrElse` (and 26 collaborators only they used), their choice contexts, `GameData` fields and readers, four unit tests. Ratchet 2,936 / 2,318 -> 2,935 / 2,316.
Ledger: `ForcedCostOrElseEffect` absorbed, eight ransom costs survivor. No `CONVERSION_VERSION` bump (no mapper or enum change).
**Cases.** Eight new ransom cases (record check, handler words, `canPay`, `pay`, completion, `SpellEvaluator.ransomCost`), each through
the action it used: `FlipCoinsCost` (`CoinFlipCostSupport`, in the answer's Krark's Thumb window; `CoinFlipService` replays an "unless"
answer through `MayAbilityHandlerService`), `OpponentCreatesTokensCost`, `PutCounterOnOpponentCreatureCost` (the existing per-counter
`OpponentCreatureCounterPlacement` pick), `DrawCardsCost`, `ExileTopCardOfLibraryCost`, `PutCardsFromSingleGraveyardOnBottomOfLibraryCost`
(the existing per-payment graveyard choice, its context without the record), `OpponentGainsLifeCost`, `GainControlOfPermanentsCost`
(a different land per counter, Herald's ruling, `PermanentChoiceContext.ResolutionCostPayment` picks). Mana, life, discard, sacrifice,
-1/-1 on self use their existing cases; a mana ransom keeps `forCumulativeUpkeep` through the answer (Snowfall, Adarkar Unicorn);
generic mana is summed ("Pay {4}", the rulings' wording); `CumulativeUpkeepEffect.sacrifice(filter, noun)` names its permanents.
**Paid mana.** `payMana` records PAY_MANA by player and type (`StepOutcome.manaByColor`), read by `AmountThisWay.manaPaid(ManaColor...)` (Fallen).
**Cards.** Oracle text of all 83 card files and Dreams of the Dead checked (`get_card`), rulings of 10; card files changed: Balduvian Fallen, Polar Kraken,
Phyrexian Soulgorger. CR 101.4, 115.1, 118.3, 118.12(a), 119.4, 603.2, 603.4, 603.10a, 603.12, 608.2d, 608.2h, 701.55b, 702.24a/b verified.
**Behaviour changes:**
- A cumulative upkeep mana payer with no mana source isn't asked and the permanent is sacrificed (owner's rule, CR 118.3, 608.2d):
  `MysticRemoraTest.CumulativeUpkeepPayment.payerWithNoManaSourceIsNotAsked` (failed first).
- A payer who can't pay the life isn't asked (CR 119.4): `GlacialChasmTest.cannotPayUpkeepWhenLifeIsInsufficient` (changed, failed first);
  Gallowbraid, Morinfen, Inner Sanctum, Decomposition likewise (tests changed).
- Prompts and log lines are the ransom's ("Pay {2}?", "Flip two coins?", "Add {R}{R}?"); an exact-count sacrifice is the shared pick
  (U3c's line, Polar Kraken, Phyrexian Soulgorger); the hard AI prices the new ransoms (opponent's life 0.5, a token 2, a counter 1).
**Pins** (passed before and after): a counter removed while the ability waits, Mana Flare's extra mana (Mystic Remora), Krark's Thumb
+ Karplusan Minotaur, Jotun Grunt, Sheltering Ancient, Heart of Bogardan, Thought Lash, Braid, Hibernation's End, Snowfall, Balduvian Fallen.
**Tests changed** (owner's rule, U3c's pick line): a `@BeforeEach` `giveManaSource` for both players in 70 cumulative upkeep classes (Dreams of the Dead's, a granted upkeep, after the orchestrator's sweep 196),
`giveManaSource` in 5 Mystic Remora methods; the 5 life tests above; `answerForcedPick` in 2 Polar Kraken and 2 Soulgorger methods;
`StepTriggerServiceTest` and `ReflexiveRouteRecordTest` build `UnlessPaysEffect`. Added: 7 `ResolutionCostPaymentServiceTest.CumulativeUpkeepRansoms`,
`UnlessPaysEffectHandlerTest.cumulativeUpkeepRansomsAskInTheirWords`, `SpellEvaluatorTest.cumulativeUpkeepRansomsArePriced`, 3 Mystic Remora.
**Tests run** (XML of this run): 433 classes (`u4/cardtests.txt`, the 83 cards' classes and the 144 naming one, the
`Unless|ForcedCost|Upkeep|CumulativeUpkeep|AgeCounter|Sacrifice|Rhystic|IcyPrison|ManaFlare|Mastery|DeathReplacement` classes, the readers of
the changed services, every architecture test, the plan's application tests) and the whole AI module: 656 files, 6,333 tests, 0 failures
(first run 269 failures in 72 classes, the owner's rule, the pick line and a lost `forCumulativeUpkeep`, fixed); after sweep 196's three Dreams of the Dead failures (the owner's rule), it and the 11 granting / granted-upkeep classes: 12 files, 137 tests, 0 failures. Compiled every module's main
and test sources and all card-test modules.
**Seen, not changed** (defect queue, "step 10 run U4d"): Varchild's War-Riders and Wall of Shards pay to the first opponent (multiplayer,
their rulings); Demonic Hordes' and Infernal Denizen's blocked folds are open now that their branches are resolution steps.
**For the next run.** U5 ("unless takes damage": `DamageFromSourceCost`, `DestroyTargetPermanentUnlessControllerTakesDamageEffect`), then U6
and U7. No reader of the old forced-cost record is left; an "unless" ransom that pays mana records PAY_MANA for any later step.

### Step 10, run U5 ("unless": takes damage) (2026-11-26)

The plan (`scratchpad/s10-unless/u5-plan.md`) with the orchestrator's U4 decisions (the strict "could possibly pay" rule, hard AI
parity). The service case existed (U2, U3c: `DamageFromSourceCost`, dealt by `Payment.entry` behind the prevention check, always
payable, read by `UnlessPaysEffectHandler` as it resolves from the source live or last known, CR 608.2h); every card pays through it,
no second path. Deleted: `DestroyTargetPermanentUnlessControllerTakesDamageEffect`, `DealDamageToTargetCreatureUnlessControllerTakesDamageEffect`,
`DealDamageToTargetCreatureOrPlaneswalkerUnlessControllerTakesDamageEffect`, `ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect`,
`WormsOfTheEarthEffect`, their ten normalfx / mayfx handlers, `MultiPermanentChoiceContext.WormsOfTheEarthSacrificeLands` and its four
branches in `MultiPermanentChoiceHandlerService`. Ratchet 2,935 / 2,316 -> 2,930 / 2,306. Ledger: 5 absorbed, 1 unique, 1 blocked-other.
No `CONVERSION_VERSION` bump (no mapper or enum change).
**Cards** (11 files by hand; Oracle text of all 11 and Risk Factor, Star Athlete, Enchanter's Bane checked with `get_card`, rulings of
Pia's Revolution, Worms of the Earth, Craig Boone, Star Athlete, Enchanter's Bane, Risk Factor, Combustion Man, Tibalt, Skullscorch):
Combustion Man (`new DamageFromSourceCost(new SourcePower())`), Dwarven Driller, Lava Blister (`unlessPays(controllerOf(target()), …,
DestroyTargetPermanentEffect)`), Dwarven Scorcher, Craig Boone, Blazing Salvo (`DealDamageEffect.toTargetCreature`), Tibalt (`.ifPaid(MayEffect
rummage)`), Pia's Revolution (Athreos's shape, `targetPlayer()` payer, `PutCardsIntoHandEffect(triggeringCards())`), Skullscorch
(`targetPlayer()`), Elektra (`youMayPay(…, QueueReflexiveAbilityEffect)`), Worms of the Earth (`mayPay(eachPlayer(), AnyOfCost.of(two lands,
5 damage), destroy source)`).
**Out.** `PayXManaDealXDamageToAnyTargetEffect` (an {X} chosen while paying, like five other `PayXMana…` records: a ransom kind of its
own, blocked-other); `TargetPermanentControllerMaySacrificeOrDamageEffect` (Enchanter's Bane's dealer is the target; unique until the dealer
run); Demonic Hordes and Infernal Denizen (opponent-chosen permanent folds, not "unless" shapes); `SacrificeOtherCreatureOrDamageEffect`,
Curse Artifact, Risk Factor (`TargetPlayerChoosesOneEffect`: same outcomes, only the prompt kind would change).
**Behaviour changes:**
- Worms of the Earth asks every player even after one paid and is destroyed once, after the last answer (owner's "any player may"
  rule, CR 101.4): `WormsOfTheEarthTest.everyPlayerIsAskedEvenAfterOnePaid` (failed first).
- Worms offers a payer only a sacrifice they could make (Sigarda's "you can't take that option", U3c's guards, CR 608.2d):
  `sigardasControllerIsOfferedOnlyTheDamage` (failed first).
- Tibalt's "you may discard a card" isn't asked with an empty hand (it resolves as a step now, CR 608.2d); was asked and did nothing.
- Prompts and log lines are the ransom's ("Have Combustion Man deal 4 damage to you?", "Sacrifice two lands?"); Worms' two lands are the
  shared pick (U3c's line); Pia's player target is harmful (asking to pay, U3b2); the damage is dealt by the real entry, not a carrier.
**Pins** (passed before and after): Dwarven Driller declined on a Magemarked Dryad Arbor under Kalitas asks its death replacements before
it leaves (`decliningAsksTheDestroyedLandsDeathReplacementsFirst`); existing: Craig Boone's lifelink on the ransom damage, Combustion
Man's prevented damage still saving, power read at resolution, the sources gone (Driller, Pia's, Craig Boone's last-known counters),
Scorcher after its sacrifice, Pia's stay check, Worms' Dryad Arbor ones.
**Tests changed** (owner's rule, U3c's pick line, CR 608.2d): `WormsOfTheEarthTest` 7 methods (player 2 answers after player 1 paid;
`answerForcedPick` for exactly two lands); `TibaltWickedTormentorTest.cannotDrawWithoutDiscarding` (no rummage prompt with an empty hand,
renamed DisplayName). Added: 2 Worms, 1 Driller, `UnlessPaysEffectHandlerTest.damageRansomIsReadAsItResolves`. CR 101.4, 118.3, 118.12,
118.12a, 120.3, 603.3, 608.2b, 608.2c, 608.2d, 608.2h, 615.1, 701.21a, 702.15b, 702.90b verified.
**Tests run** (XML of this run): 138 classes (the 11 cards' classes and the 25 naming one, the 7 DamageFromSourceCost cards' classes, the 90
`Unless|Damage|Prevent|Redirect|Lifelink|Infect|Sacrifice|Mastery|DeathReplacement` classes, every architecture test, `UnlessPaysEffectHandlerTest`,
`ResolutionCostPaymentServiceTest`, `MayAbilityHandlerServiceTest`) and the whole AI module: 296 files, 3,054 tests, 0 failures (a first run of
the 25 classes: 8 failures, the Worms and Tibalt changes above, fixed). Compiled every module's main and test sources and all card-test modules.
**Seen, not changed:** none new.
**For the next run.** U6 ("for each … unless", the per-object iteration) and U7 (the `XOrY…Cost` cast-time records). A "pay X at
resolution" run could fold the six `PayXMana…` records onto an `UnlessPaysEffect` ransom whose {X} the payer chooses while paying
(`EachPlayerPaysAnyManaEffect.X_MANA` is the reflexive form). The dealer run takes Enchanter's Bane and Star Athlete.

### Step 10, run U6 ("unless": for each object) (2026-11-26)

The plan (`scratchpad/s10-unless/u6-plan.md`): the loop and the three cards it fits without another part; the rest left for U6b
(below). Absorbed into `UnlessPaysEffect`: `ReturnMatchingPermanentsUnlessOwnerPaysEffect`, `DestroyLandsUnlessAnyPlayerPaysLifeEffect`,
`DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect`, their six normalfx / mayfx handlers, two `MayPenaltyChoiceHandlerService`
methods, three `GameData` queues. Ratchet 2,930 / 2,306 -> 2,927 / 2,300. Ledger: 3 absorbed, 11 blocked-other (re-noted). No
`CONVERSION_VERSION` bump (no mapper or enum change).
**Engine.** `UnlessPaysEffect.forEach(objects, payer, ransom, unpaid -> X)` (a seventh component, `forEach`; the loop lives on the record
because the payer loop, the entry's answer state and the branch splice are already there): objects found once (CR 608.2h), asked about
in their payers' APNAP order (CR 101.4); `controllerOf(objects)` / `ownerOf(objects)` is read per object (`payerReadPerObject`,
`SubjectResolutionService.controllerOf` / `ownerOf` by id), any other payer asks every player it names (the owner's "any player" rule);
payments made as answered (U3c decision 1, CR 603.3); an object gone before its turn skipped; after the last answer the unpaid objects
are the entry's CHOOSE step and X resolves once over `thisWay(CHOOSE)` (one event, CR 608.2c, 101.4), NOTHING when all were paid.
`UnlessAnswer.remainingObjects` / `unpaidObjectIds` / `nextObject`; prompt "Pay {3} for Lantern Kami?". AI: `SpellEvaluator.isBoardWipeEffect`
reads a for-each over every player's permanents with a board-wipe branch (Cut the Tethers keeps its sweep marker). No service case.
**Cards** (3 files by hand; Oracle text of all three checked with `get_card`, rulings of the three, Professor Onyx, Decoy Gambit, Heroism,
Tidal Flats, Sirocco, Stench of Evil, Sword-Point Diplomacy; CR 101.4, 118.3, 118.12a, 119.4, 603.3, 608.2c, 608.2d, 608.2h verified).
**Behaviour changes** (failing-first, each seen failing on the old code):
- Cut the Tethers returns the unpaid Spirits together after the last answer (one instruction, CR 608.2c, 101.4; queue line D13):
  `CutTheTethersTest.declinedSpiritsAreReturnedTogetherAfterTheLastAnswer`.
- Cut the Tethers doesn't ask an owner with no way at all to make mana (owner's strict rule): `ownerWithNoManaSourceIsNotAsked`.
- Cleansing asks every player for each land even after one paid (owner's "any player" rule, CR 101.4): `everyPlayerIsAskedEvenAfterOnePaid`.
- Prompts and log lines are the survivor's; Cut the Tethers' mana pays through `ResolutionCostPaymentService.pay` (tax kept).
**Pins** (passed before and after): the three cards' 31 existing tests (stolen Spirit decided by its owner, Albatross's damage history after
exile, no regeneration, destroyed together, the Dryad Arbor death replacements), Tax Taker's {3}, Lich's Duel Mastery's Cleansing and
Albatross payments, Gravebane Zombie under Kalitas.
**Tests changed** (owner's rules): `CleansingTest` 2 methods (player 2 answers after player 1 paid for a land), `CutTheTethersTest` cast
helper gives both players a mana source, `TaxTakerTest.cutTheTethersPayment` (`giveManaSource`). Added: 3 card tests, 2
`UnlessPaysEffectHandlerTest` (per-object payers and the CHOOSE record; a gone object skipped, NOTHING when all paid).
**Tests run** (XML of this run): 1,286 classes (every `UnlessPaysEffect` card's own class, the U4 / U4d sets, the 8 classes naming the three
cards, the `Unless|ForEach|Sacrific|Bounce|Return|Tap|Discard|Mastery` classes, readers of `UnlessAnswer` / `UnlessPaysEffectHandler` /
`MayPenaltyChoiceHandlerService`, every architecture test, `MayAbilityHandlerServiceTest`, `ResolutionCostPaymentServiceTest`,
`SubjectResolutionServiceTest`, `GameDataDeepCopyTest`) and the whole AI module: 1,492 files, 13,781 tests, 0 failures. Compiled every
module's main and test sources and all card-test modules.
**Left for U6b**, each needing one more part: Sirocco, Sword-Point Diplomacy (card objects: a `CardSubject` loop); Stench of Evil with
`EachPlayerTakesDamageUnlessPaysEffect` (departed objects, damage summed per controller); Heroism (combat-damage prevention over a
subject); Tidal Flats (a "blocking that creature" subject); Decoy Gambit (target objects, one draw per payer before the returns; its
can't-draw question with it, not decided here); Ob Nixilis, Professor Onyx (`eachUnless` fits, but Onyx's ruling has the cards chosen
hidden and discarded together: a `DiscardCardTypeCost` case in `choose` / `payTogether`); Tariff (a ransom read from a chosen object).
Fade Away and Killing Wave stay their own records: one subset choice per player, paid and sacrificed as one event (their rulings).
**Seen, not changed** (defect queue, "step 10 run U6"): Sword-Point Diplomacy asks only the first opponent (its ruling, CR 101.4; sure,
multiplayer); the hard AI scores a for-each branch as a whole, not the object asked about (unsure of the score).
**For the next run.** U6b: extend `forEach` to card objects (CHOOSE over cards, `cardsThisWay`) and add the subjects / prevention named above;
the discard-together case unblocks Ob Nixilis and Professor Onyx (and `LoseLifeUnlessDiscardEffect` with them). U7 is unchanged.

### Step 10, run U6b ("unless": for each object, the rest) (2026-11-26)

The plan (`scratchpad/s10-unless/u6b-plan.md`). Absorbed into `UnlessPaysEffect`: `RevealHandDiscardMatchingCardsUnlessPaysLifeEffect`,
`RevealTopCardsOpponentPaysLifeOrToHandEffect`, `EachOpponentMayDiscardOrLoseLifeEffect`, `LoseLifeUnlessDiscardEffect` (built only by the
last one's handler), their six normalfx / mayfx handlers, two `MayPenaltyChoiceHandlerService` methods, `GameData.revealHandDiscardUnlessPaysRemaining`.
Ratchet 2,927 / 2,300 -> 2,923 / 2,294. Ledger: 4 absorbed, 6 blocked-other (re-noted). No `CONVERSION_VERSION` bump (no mapper or enum change).
**Engine.** `UnlessPaysEffect` gains `forEachCard` (`forEachCard(cards, payer, ransom, unpaid -> X)`: the U6 loop over revealed cards, every
player the payer names asked about each card in the subject's order, a card that left its zone skipped, CR 400.7; the unpaid cards the
CHOOSE step where they are, bound to their stay, X once over `cardsThisWay(CHOOSE)`, CR 608.2c) and `perPayer` (set by `eachUnless`; read by
`anotherPlayerAlreadyPaid`, so the AI no longer declines its own payment because another payer paid: Lim-Dûl's Hex and the other
`eachUnless` files too). Service case: `DiscardCardTypeCost` in `choose` (nothing picked, the hand checked) and `payTogether` (one
`DiscardEffect` over `acceptedPlayers()`, which has each payer pick in APNAP order and discards at once through `DiscardEffectHandler`).
Picking the card after every answer instead of with it is the same game: the card is hidden (CR 101.4a), later players know only that a card
was chosen (CR 101.4b).
**Cards** (4 files by hand, Oracle text of all four checked with `get_card`, rulings of all four and Decoy Gambit): Sirocco
(`RevealCardsFromHandEffect.hand(targetPlayer())` + `forEachCard(revealedThisWay(blue instant), targetPlayerDeclaredElsewhere(), PayLifeCost(4),
DiscardCardsEffect::new)`), Sword-Point Diplomacy (`RevealTopCardOfLibraryEffect.topCards(you(), 3)` + `forEachCard(revealedThisWay(),
eachOpponent(), PayLifeCost(3), PutCardsIntoHandEffect::new)` + `ExileGraveyardCardsEffect.cards(revealedThisWay())`), Ob Nixilis and Professor
Onyx (`eachUnless(eachOpponent(), DiscardCardTypeCost, players -> LoseLifeEffect(n, players)).payingTogether()`).
**Behaviour changes** (failing-first, each seen failing on the old code):
- Sirocco discards the unpaid blue instants together after the last answer (one instruction, CR 608.2c): `declinedCardsAreDiscardedTogetherAfterTheLastDecision`.
- Sword-Point Diplomacy asks one yes/no per card (was one subset pick) and every opponent in turn order, even after another paid (its ruling, CR 101.4;
  queue line struck): `eachRevealedCardIsItsOwnQuestion`, `everyOpponentIsAskedForEachCard`.
- Ob Nixilis and Professor Onyx: every opponent chooses before anyone discards or loses life; the chosen cards are discarded together, then the
  others lose life together (Onyx's ruling, CR 101.4): `ObNixilisTheAdversaryTest` 2, `ProfessorOnyxTest.minusEightEveryOpponentChoosesBeforeTheRoundHappens`.
- The AI's "another player already paid" skips a per-payer branch: `UnlessPaysEffectHandlerTest.perPayerBranchIsNotPaidByAnotherPayer`.
- Prompts and log lines are the survivor's ("Pay 4 life for Memory Lapse?", "chooses to discard a card").
**Pins** (passed before and after): the four cards' tests and the 12 classes naming them (17 files, 232 tests), Lich's Duel Mastery's Ob Nixilis
punisher, Tamiyo and Library of Leng on Sirocco's discard, Font of Agonies on Sword-Point's life payment.
**Tests changed** (each against the Oracle text and the rulings): `SwordPointDiplomacyTest` 10 methods (the subset pick became one answer per card;
`opponentCannotOverpay` now asserts the unpayable cards aren't asked, CR 119.4; DisplayNames added to three), `LichsDuelMasteryLifePaymentEffectTest`
Sword-Point method (answer per card). Deleted `LoseLifeUnlessDiscardEffectHandlerTest` (its record is gone; the cases are the card tests and
`ResolutionCostPaymentServiceTest.PaidTogether`'s two new discard ones). Added: 1 Sirocco, 2 Sword-Point, 2 Ob Nixilis, 1 Onyx, 2 handler, 2 service tests.
CR 101.4, 101.4a, 101.4b, 118.3, 118.12, 119.4, 121.3, 400.7, 603.3, 608.2c, 608.2d, 608.2h verified.
**Tests run** (XML of this run): 1,299 classes (every `UnlessPaysEffect` card's own class, the U4 / U4d sets, the 16 + 8 classes naming the U6 / U6b cards,
the `Unless|ForEach|Sacrific|Bounce|Return|Tap|Discard|Mastery` classes, the readers of `UnlessAnswer` / `UnlessPaysEffectHandler` /
`MayPenaltyChoiceHandlerService`, every architecture test, `MayAbilityHandlerServiceTest`, `ResolutionCostPaymentServiceTest`, `SubjectResolutionServiceTest`,
`GameDataDeepCopyTest`) and the whole AI module: 1,505 files, 13,953 tests, 1 failure (the Mastery Sword-Point method above, answering the old
prompt; fixed, its class rerun: 36 tests, 0 failures). Compiled every module's main and test sources and all card-test modules.
**Out** (each a one-card part, ledger re-noted): Stench of Evil with `EachPlayerTakesDamageUnlessPaysEffect` (departed objects, damage summed per
player), Heroism (`PreventDamageEffect` takes scopes, not subjects), Tidal Flats (no "blocking that creature" subject), Tariff (a ransom read from
the object), Decoy Gambit (target objects, a draw per payer before the returns, and the can't-draw question: owner's). Fade Away, Killing Wave stay.
**Seen, not changed** (defect queue, "step 10 run U6b"): The Long Reach of Night and Possessed Portal discard at each answer (CR 101.4a; add
`.payingTogether()`); Sword-Point's caster doesn't choose the card order (its ruling); Decoy Gambit's can't-draw question.
**For the next run.** U7 (the `XOrY…Cost` cast-time records). A small fold: `.payingTogether()` on The Long Reach of Night and Possessed Portal.
The five cards left here wait for their part's second user.

### Step 10, run U6c ("unless": discards paid together) (2026-11-26)

A small fold, U6b's "Seen, not changed" line. No record or handler added or removed (ratchet 2,923 / 2,294 unchanged), no engine file
changed, no ledger row (nothing absorbed), no `CONVERSION_VERSION` bump.
**Cards** (2 files by hand; Oracle text of both checked with `get_card`, rulings of both, Possessed Portal has none): The Long Reach of
Night and Possessed Portal's `eachUnless(…, DiscardCardTypeCost, players -> SacrificePermanentsEffect(…))` take `.payingTogether()`
(U6b's discard case): every payer answers in APNAP order, then each one who chose to discard picks the card in APNAP order and the cards
are discarded at once, then the others sacrifice together (CR 101.4, 101.4a, 101.4b verified; as Ob Nixilis and Professor Onyx).
**Behaviour change** (failing-first, each seen failing on the old code): nobody discards before the last answer, and the chosen cards
(madness ones too) are discarded together: `TheLongReachOfNightTest.everyOpponentChoosesBeforeAnyoneDiscards` /
`madnessCardsAreDiscardedTogetherAfterTheLastChoice`, `PossessedPortalTest.everyPlayerChoosesBeforeAnyoneDiscards` /
`declinerSacrificesAfterTheDiscards`. **Pins** (passed before and after): one madness discard each (`discardedMadnessCardTriggersMadness`
in both classes) and the cards' 28 existing tests. No test changed.
**Sibling check**: every card file with a discard ransom (`DiscardCardTypeCost`, `DiscardRandomCardCost`, `DiscardHandCost`) and a
several-player payer: only Phantasmagorian is left, on the plain loop by U3c decision 1 (its ruling: "as soon as any player performs the
action"), not a gap. No engine builder makes a discard ransom.
**Tests run** (XML of this run): the two cards' classes, Gravebane Zombie (names Possessed Portal), the U6b reference set, Ob Nixilis,
Professor Onyx, Phantasmagorian, the `Unless|ForEach|Sacrific|Bounce|Return|Tap|Discard|Mastery` classes, the readers of `UnlessAnswer` /
`UnlessPaysEffectHandler` / `MayPenaltyChoiceHandlerService`, every architecture test, the service tests, and the whole AI module: 345
files, 3,384 tests, 0 failures. Compiled every module's main and test sources and all card-test modules.
**Seen, not changed**: none new. The discards come one step before the sacrifices, not in the same event (as U6b's Onyx); no rule or
ruling tells them apart here, as their triggers wait for the resolution to end (CR 603.3).
**For the next run.** U7 (the `XOrY…Cost` cast-time records), unchanged.

### Step 10, run U7 (cast-time AnyOfCost) (2026-11-26)

The plan (`scratchpad/s10-unless/u7-plan.md`) with the orchestrator's decisions for U7 (Standing knowledge). **U7a** first, U7b and U7c
below. Absorbed into `AnyOfCost`: `DiscardCardOrSacrificePermanentCost`, `DiscardCardOrPayLifeCost`,
`PayLifeOrSacrificePermanentCost`, `SacrificeCreatureOrDiscardCardOrPayLifeCost` (no handlers). Ratchet 2,923 / 2,294 -> 2,919 / 2,294.
Ledger: 4 absorbed, `AnyOfCost` survivor. No `CONVERSION_VERSION` bump (no mapper or enum change).
**Engine.** `AnyOfCost` is a handled spell cost (`HANDLED_SPELL_COST_TYPES`; off the classification test's resolution-only list).
`ExtractedCosts` keeps it unchosen in `anyOfCost` with the removed costs in `extractedCosts`; `withChosenAlternative(costs, selection,
card)` extracts again with `chosenAlternative`'s pick in its place, so the standalone slots validate and pay it (CR 601.2b, 601.2h).
Called by `validateAll`, `payAdditionalCosts`, `castLifePayments`, `castCostSacrificeIds` and once on the hand path after extraction
(idempotent). The pick: the alternative whose request field is filled in (sacrifice / counters id, discard index, graveyard indices,
tap ids, `payLifeForAdditionalCost`); none: mana, else life, else "Must A or B to cast C"; two: "Must choose only one additional cost
payment" appended. `satisfiable` is one per-cost method, an any-of payable while any alternative is (CR 118.3; `alternativeSatisfiable`).
`validDiscardCostIndices` reads the discard alternative. Free casts (`ExileFreeCastQueueSupport`): one chooser over a sole `AnyOfCost`
(same message, labels "Discard a card", "Pay N life", "Sacrifice <description>"), only payable alternatives, one picked unasked; the pick
replaces the cost on the entry and continues into the standalone pickers or the life payment (Lich's Duel Mastery asked first). The
queue gate takes any sole any-of. Graveyard casts reject it; the library-top path keeps only "discard or pay life" with an empty hand.
AI: `AnyOfCost` delegates `consumedPermanentFilter`, `sacrificesChosenCreature`, `tappedPermanentCount`, the graveyard facets;
`hasAvailableDiscardAlternative` / `computeSacrificeTarget` read an any-of with a discard part; `SpellEvaluator.lifePaidForCost` reads its
life alternative (Final Payment 5 as before; Bitter Triumph 3, Dusk Mangler 4, were 0). No resolution-time reader reads the facets
(`ransomCost` switches on `AnyOfCost` first).
**Cards** (7 files by hand; Oracle text of all 7 checked with `get_card`): Bone Shards, Demand Answers, Minion Missile, Souls of the Lost
(`AnyOfCost.of(new DiscardCardTypeCost(null, null), new SacrificePermanentCost(filter, description, false))`, discard first: decision Q1),
Bitter Triumph (discard, 3 life), Final Payment (5 life, sacrifice a creature or enchantment), Dusk Mangler (`of(SacrificeCreatureCost,
discard, 4 life)`).
**Behaviour changes** (failing-first, each seen failing on the code before the run, in a `git archive HEAD` copy):
- A free Final Payment is cast and offered "Pay 5 life" / "Sacrifice a creature or enchantment", and pays 5 life unasked with nothing to
  sacrifice (was skipped as unsupported; CR 601.2b, 601.2f): `HazoretsUndyingFuryTest.freeFinalPaymentOffersBothAlternatives`,
  `freeFinalPaymentPaysLifeWithNothingToSacrifice`. Dusk Mangler (mana value 7) takes the same path, untested (no free-cast test card).
- The AI casts Dusk Mangler paying one alternative (it sent a sacrifice and a discard, refused): `EasyAiDecisionEngineTest.castsDuskManglerPayingOneAlternative`.
- Free casts on the other paths that call `beginSacrificeCostIfNeeded` now pay Final Payment's and Dusk Mangler's cost (it found no branch).
- Messages: "Not enough life to pay 3 life for C" (was "pay 3 for C"); a request naming two alternatives says "Must A or B to cast C:
  choose only one additional cost payment" (Dusk Mangler said only the second half).
**Pins** (passed before and after): free Bitter Triumph's "Discard a card" / "Pay 3 life" chooser, Bitter Triumph from the library top
with an empty hand (`BitterTriumphTest.castFromLibraryTopWithAnEmptyHandPaysLife`), the AI sacrificing for Final Payment, and every
existing class of the 7 cards (both alternatives by request, Bone Shards / Minion Missile / Souls free, the Mastery life cost).
**Tests changed**: `CastingCostServiceTest` 2 methods and the 3 AI engine tests' either-or creature build the cost as an `AnyOfCost`
(record gone, same assertions); `CostEffectClassificationTest` lists. CR 101.4, 118.3, 118.8, 118.12a, 601.2b, 601.2c, 601.2f, 601.2h,
608.2d, 701.61a verified.
**Tests run** (XML of this run): the 485 card classes with an `addCastingCost`, the 24 classes naming a rewritten card, the
`Cost|Cast|Free|Alternative|Additional|Forage|Discard|Sacrific|Exile|Kicker|Flashback|Mastery|DeathReplacement` classes, every
architecture test, the casting, availability, activation, deep-copy and networking application tests, the whole AI module: 903 files,
7,940 tests, 1 failure (`DemandAnswersTest`'s two-alternatives message; the message now keeps "Must discard a card or sacrifice an
artifact", rerun with the touched classes: 15 files, 138 tests, 0 failures). Compiled every module's main and test sources and all card-test modules.
**Seen, not changed** (defect queue, "step 10 run U7"): the client chooser and hand-cast discard gap (owner, Q3); the chooser after
targets (Q6); a graveyard play without paying (Horde of Notions) skips Bitter Triumph's, Final Payment's and Dusk Mangler's cost; a
library-top Bitter Triumph with cards in hand is refused.
**U7b** (the mana alternatives). Absorbed: `DiscardCardOrPayManaCost`, `ExileNCardsFromGraveyardOrPayManaCost`,
`PutCountersOnControlledCreatureOrPayManaCost`, `SacrificePermanentOrPayManaCost`, `TapPermanentOrPayManaCost` (no handlers). Ratchet
2,919 / 2,294 -> 2,914 / 2,294. Cards (16 files by hand, Oracle text of all 16 checked with `get_card`): `AnyOfCost.of(discard, new
PayManaCost("{m}"))` (Lightning Axe, Pumpkin Bombardment, Titania), `of(new ExileNCardsFromGraveyardCost(2, null), mana)` (Stoneglider),
`of(PutCounterOnControlledCreatureCost.blight(n), mana)`, `of(new SacrificeCreatureCost(), mana)` for "sacrifice a creature" (keeps the AI's
weakest-creature pick), `of(new SacrificePermanentCost(filter, words), mana)` (mana first for Annihilating Glare and Deadly Precision, Oracle
order), `of(new TapMultiplePermanentsCost(1, artifact), mana)`. **Engine.** `PayManaCost` is a handled spell cost; `ExtractedCosts` has
`additionalManaCost` (the chosen mana alternative) and `chosenFrom`; the hand paths fold it into the total cost through
`alternativeAdditionalManaSuffix` (CR 601.2f); the five after-base blocks are one `payAdditionalManaAfterBase` (free casts, alternative
costs); "P pays {m} for C." stays where it was logged (decision Q2: discard, counter, exile and forage alternatives,
`logsManaAlternativePaidWithBase`). `costSatisfiable(PayManaCost)`: total cost, free casts the pool. Free casts: discard-or-mana and
counters-or-mana branches gone into the any-of chooser (labels "Pay {m}", "Put counters on a creature you control"); new sole
`PayManaCost` and blight branches; graveyard-exile and forage alternatives aren't offered (no free-cast prompt). Exile casts: a sole any-of
of mana with discard / sacrifice takes the sacrifice from `additionalCostSacrificePermanentIds` (validated first, death replacements asked,
paid after mana); library top keeps sacrifice-or-mana (`isSacrificeOrManaCost`). AI stubs read the any-of.
**Behaviour changes** (failing-first, each seen failing on the code before the run): a free Eaten Alive offers "Sacrifice a creature" /
"Pay {3}{B}" (was skipped; CR 601.2b, `HazoretsUndyingFuryTest.freeEatenAliveOffersBothAlternatives`), and so every free sacrifice-, tap- or
blight-or-mana spell; a free Bogslither's Embrace offers blight / {3} (`freeBogslithersEmbraceBlightsTheChosenCreature`, was skipped);
Eaten Alive cast from exile sacrifices the named creature (was forced to mana; `EatenAliveTest.castFromExileSacrificesTheChosenCreature`).
Free sacrifice-or-mana satisfiability counts the pool alone (CR 601.2f; untested). Messages: "Must A or B to cast C" in record order
("Must pay {4} or sacrifice an artifact or creature"; "tap an untapped permanent" for Disruption Protocol).
**Pins**: free Lightning Axe's "Discard a card" / "Pay {5}" paying from the pool (`freeLightningAxePaysTheChosenMana`); Stoneglider's {1}{W}
from WWW+CC (`paysTheManaAlternativeAsPartOfTheTotalCost`: the old after-base route also paid it, so decision Q4 is a pin); every class of
the 16 cards (both ways by request; Bayou Groff from exile; Bolas's Citadel, Chase Stein, Extract Power, Siphon Insight casts).
**Tests changed**: `CastingCostServiceTest` 2 methods build the cost as an `AnyOfCost` (same assertions); `CostEffectClassificationTest`
lists. **Tests run**: 921 files, 8,167 tests, 0 failures (the 486 `addCastingCost` card classes, 52 classes naming a rewritten card, the
`Cost|Cast|Free|…` classes, architecture, casting / availability / networking application tests, the whole AI module).

**U7c** (`ForageCost`). `ForageOrPayManaCost` replaced by `ForageCost()` (CR 701.61a; ±0 records): ability costs (Camellia, Thornvault),
`KickerEffect.forage()` (Traverse Valley), Osteomancer's permission, and Feed the Cycle `AnyOfCost.of(new ForageCost(), new
PayManaCost("{B}"))` (Oracle text of all 5 checked). `chosenAlternative` selects forage by a Food id or graveyard indices; the graveyard path
accepts "forage or pay {m}" (`isForageOrManaCost`, mana after the base as before); the dead mana branches of the forage paths are gone.
**Behaviour change**: a free Feed the Cycle is cast paying {B} (was skipped; `HazoretsUndyingFuryTest.freeFeedTheCyclePaysItsManaAlternative`,
failing-first by reading the old gate, which had no forage branch; not run on the old code). **Tests run**: the U7b set plus the forage cards: 928 files, 8,226 tests, 0 failures.
**Seen, not changed** (defect queue, "step 10 run U7"): exile casts can't choose a discard alternative (no discard index reaches the
exile path; Lightning Axe from exile pays {5}); free casts don't offer graveyard-exile or forage alternatives (Stoneglider, Feed the Cycle);
exile and library-top casts still refuse tap-, blight- and exile-or-mana spells.
**For the next run.** The "unless" family's rest (Step-10 hand-offs): Decoy Gambit, Stench of Evil, Heroism, Tidal Flats, Tariff,
Whirlwind Denial, the `PayXMana…` records, Enchanter's Bane / Star Athlete. A free-cast graveyard picker would close the two free-cast
lines above; a discard index on the exile path the first.

### Step 10, run Boost B1 (2026-11-26)

"[permanent] gets +X/+Y", orchestrator's decisions under Standing knowledge. `BoostTargetCreatureEffect` is renamed `BoostEffect` (handler
`BoostEffectHandler`) and absorbs `BoostSelfEffect` and `BoostTargetPermanentEffect` (factories and `AmountsRead` under Shared pieces).
Ledger: 2 absorbed, 1 survivor. Ratchet 2,914 / 2,294 -> 2,912 / 2,292. No `CONVERSION_VERSION` bump (no mapper or enum AGENTS.md names).
**Premise.** The three handlers differed in more than the subject: the amount source (self: the boosted permanent; target and Vehicle: the
source, live else last known), durations (target coerced the unused ones to end of turn; self passed them through), the face-down label
(`effectSourceName` vs `FaceDownVisibility.sourceName`), a non-end-of-turn self boost's floating effect had no source permanent id and was
added at +0/+0, and the log's duration words. One handler keeps every reading through the explicit `AmountsRead`; the rest is unified.
**Card rewrite.** 2,542 card files, 2,700 calls (scratch script: balanced parentheses, 13 shapes, loud failure on any other, leftover check,
imports; deleted): `target` 1,540, `self` 1,209, `of` 72, `each` 12, `targetPermanent` 9, `targetInGroup` 6, `withCastTimeXValue` 2; plus 37
engine, domain and test files by the same script, 13 by hand (local types, builders, composites). Oracle text checked for 2,540 files with
the MCP `get_card` implementation (back faces through their front card): 2,488 matched subject wording, amounts and duration automatically,
52 by hand (variants: "double its power", "each get", specialize faces, prowess reminder text, the `each` subjects); Growth Charm and Swarm
of Locus (MB1, in no feed) checked against their code text. None differed.
**Behaviour changes** (log and label only; no play change):
- The log's duration words follow the duration: "until end of combat" (Glyph of Destruction; the target record said "until end of turn"),
  "indefinitely" (Goblin Ski Patrol; the self record said "until end of turn"), `GlyphOfDestructionTest` / `GoblinSkiPatrolTest` new log tests
  (not run on the old code; the old handlers' text, read, fails them).
- A self boost's floating effect is labelled through `FaceDownVisibility.sourceName` ("a face-down card" for a face-down source, was "a
  face-down permanent"; Conventions: the one rule), carries its source permanent id, and +0/+0 adds nothing.
- AI (Q5): `CreatureBoostEffect.targetedBoost` drops the non-targeting source-reading boosts (the three Crowns, Elemental Appeal, Mirkwood
  Trapper, Sigardian Zealot, Zenos yae Galvus) from the combat-trick, pump-score, polarity and size-gate readers. The pin
  `HardAiAbilityAndInteractionTest` "keeps Crown of Vigor" (main phase and combat) failed after the change (the AI sacrificed the Crown at
  any priority), so `isGoodTimingForAbility` still times a SOURCE boost with no target as a targeted pump: both pins pass as before.
**Q3.** `AbyssalHunterTest` "pumped while its ability waits, then bounced" passed on the old code: not failing-first. The premise doesn't
hold: `PermanentRemovalService` refreshes every waiting entry's source snapshot as the permanent leaves, and a live source is read live, so
the refresh (now after target and set boosts too, as decided) changes nothing observable (defect queue: a cleanup candidate).
**Pins** (passed before and after): the Q3 test, the two Crown pins; existing: Two-Handed Axe, Junk Jet, Beastmaster's Magemark, Rabble-Rouser,
Cultivator of Blades, Anya, Goblin Ski Patrol, Murk Dwellers, Riding the Dilu Horse, Jace, Howler's Heavy, Fang Guardian, Pack's Disdain.
**Readers.** `triggerOrderLabel`, `TextChangeTransformer`: the old record's set (SOURCE, not `targetPermanent`); `selfBoost`, `SpellEvaluator`: a
`source()` subject; `readsExcessDamage` and the destroy-then handler read only B2's records.
**Shared engine files that changed meaning.** `BoostEffect` / `BoostEffectHandler` (new); `CreatureBoostEffect` (`targetedBoost`);
`BoostSelfSelfEffectHandler`, `StaticEffectSupport` (keyed on `BoostEffect`); `TextChangeTransformer`, `TriggeredAbilityQueueService`,
`LayerClassifier`; the AI readers (`HardAiDecisionEngine`, `SpellEvaluator`, `InstantCategoryClassifier`, `SizeGatedRemovalPump`,
`AiTargetSelector`, `TargetPolarityClassifier`, `ChosenPermanentsAiChoice`).
**Tests changed.** Three `@DisplayName`s named the deleted record (`SpellEvaluatorTest`, `ActivatedAbilityExecutionServiceTest`,
`SpellTriggersTest`); 27 test files build boosts through the factories, `DurationClockTest` filters on `BoostEffect.class` (same assertions).
**Tests run** (one Gradle run, XML of this run): 5,698 classes (every rewritten card's class, the 5,549 classes naming a rewritten card, the
`Boost|Pump|Layer|Static|Polarity|TextChange|Equip|Aura|Vehicle` classes, the whole AI module, the readers' and changed application tests,
every architecture ratchet), 53,856 tests, 0 failures. Compiled every module's main, test and test-fixture sources.
**Seen, not changed** (defect queue, "step 10 run Boost B1"): the Vehicle boost's harmful prior (Q6); battle cry vs CR 702.91a (Q7); text
changes not reaching self amounts, Vehicle filters or set filters (CR 612.2, Q8); the trigger-order label of a source-reading boost with no
target says "target creature".

**For the next run.** Boost B2: `BoostAllOwnCreaturesEffect` and `BoostAllCreaturesEffect` onto `BoostEffect` as `controlledBy` subjects
(plan section 3: `creaturesYouControl` / `allCreatures` / `creaturesTargetPlayerControls` factories, Exhilarating Elocution's `except`, the
declared attackers, Strategic Intervention), widening the SOURCE guard, the summary log line for a set, `readsExcessDamage`, the destroy-then
handler and battle cry; the AI's `targetedBoost` already leaves non-targeting sets out.

### Step 10, run Boost B2 (2026-11-26)

"[set of creatures] get +X/+Y", orchestrator's decisions under Standing knowledge (Q4 summary log, Q7 / Q8 parity). `BoostAllOwnCreaturesEffect`
and `BoostAllCreaturesEffect` (records and handlers) are gone: `BoostEffect.creaturesYouControl`, `allCreatures`,
`creaturesTargetPlayerControls` (`[, filter]`, a `narrowed` `controlledBy` subject, amounts read once from the source, the set found once,
CR 611.2c, 608.2h). Ledger: 2 absorbed. Ratchet 2,912 / 2,292 -> 2,910 / 2,290. No `CONVERSION_VERSION` bump.
**Premise.** The handlers differed from `BoostEffectHandler` in the filter context (AO: no defending player; AC: no triggering permanent,
`targetCardIds[0]` as the target card), the log (one summary line), AO's all-battlefield search for declared attackers and a triggering filter,
and AC's early return without a target player. `BoostEffect.isMassBoost()` (`ControlledBy` or `TriggeringAttackers`) gives the summary line and
keeps the B1 readers' meaning: the AI's pump timing (else a mass pump ability looked like a targeted pump with no target and was never
activated), the trigger-order label, text changes (Q8). `targetSpec` declares the player of `creaturesTargetPlayerControls` (benign, as AC).
**Card rewrite.** 609 card files, 642 calls (scratch script: balanced parentheses, 6 shapes, loud failure on any other or a chained call,
leftover check, imports in place; deleted): `creaturesYouControl` 432, `allCreatures` 189, `creaturesTargetPlayerControls` 15; by hand 6:
Exhilarating Elocution (`excludeTargets` = filter `Not(PermanentIsTargetCardPredicate)`, keeps the summary line), Champions from Beyond, Lulu,
Sabotage Strategist (`each(triggeringAttackers(), …)`), Strategic Intervention (`of(triggering(), 1, 1)`), Public Execution. Engine: battle cry,
`BoostAllCreaturesExceptChosenTypeEffectHandler`, `readsExcessDamage` (you-controlled sets and the attackers, as AO), the destroy-then handler.
Oracle text checked for all 609: 597 with the MCP `get_card` implementation (570 automatically, 27 by hand: "those creatures get",
"additional", kicker, the specialize faces' own text), 11 back faces and constructed cards through their front card, Plane-Merge Elf (MB1, in
no feed) against its code text. None differed.
**Behaviour changes** (none in play):
- `SubjectResolutionService.bindTarget`: with no permanent target, `PermanentIsTargetCardPredicate` names the permanents of the entry's
  target cards (the rider of Public Execution targets the player). Without it the regenerated target got -2/-0: `PublicExecutionTest`
  "A regenerating target is excluded" and the new `SubjectResolutionServiceTest` case both failed with the binding switched off.
- Log: Strategic Intervention logs "gets +1/+1 until end of turn" for its creature (was the summary line); a mass boost's floating effect is
  labelled through `FaceDownVisibility.sourceName` (B1's rule) and +0/+0 adds nothing.
- Set filters read the resolver's filter context (defending player, X, triggering permanent for every set); no test changed meaning.
**Pins** (passed on the old code and after): `InfestTest` "dies together" (CR 704.5f); `SpellEvaluatorTest` mass pump and mass debuff scores;
`HardAiAbilityAndInteractionTest` "activates a mass pump ability while its creature attacks" (Akroan Phalanx; not run against the code
without the timing guard). Existing: Inspired Charge and Gruesome Realization (CR 611.2c), Earthshaker Giant and Kodama ("other"), Baru and Elvish Warmaster
(colour, type), Shields of Velis Vel, Gnashing of Teeth, Public Execution, Overwhelming Stampede, Cultivator of Blades, Sorin, Mouth of the
Storm, Signal Pest (battle cry), Exhilarating Elocution, Champions from Beyond, Lulu, Sabotage Strategist, Strategic Intervention.
**Tests changed.** None changed; 4 pins and 1 resolver test added.
**Tests run** (one Gradle run, XML of this run): 6,568 classes (every `BoostEffect` card's class, the classes naming one, the
`Boost|Pump|Layer|Polarity|TextChange|BattleCry|Anthem|Wrath|Sweep|Ratchet` classes, the whole AI module, the readers' application and engine
tests), 61,859 tests, 0 failures; then the resolver, Public Execution and ratchet classes again. Compiled every module's main and test sources.
**Seen, not changed** (defect queue, "step 10 run Boost B2"): "attacking creatures get" written over your creatures (Iroh, Kangee's
Lieutenant, Motivated Pony, Targ Nar, Honored Crop-Captain), as battle cry (Q7).

**For the next run.** The Boost family is done but for what stays out (Step-10 hand-offs); next the rest of the "unless" family or another
verb there. A follow-on fold can take `BoostOpponentCreaturesByPoisonCountersEffect` and its sibling once `BOOSTED` reads over a set.

### Step 10, run Damage P (players) (2026-11-26)

Run P of the plan `s10-damage2` (orchestrator's decisions under the owner's decisions, 2026-11-26). `DealDamageEffect` keeps its name and
gains two `Recipient` kinds, `PLAYERS` and `PLAYER_OR_THEIR_PLANESWALKER`, with components `players` and `amountForEachPlayer` (`subject`
null for them, Q1); factories `to(PlayerSubject, int | DynamicAmount)`, `eachEvaluatedFor`, `toPlayerOrTheirPlaneswalker`, rider
`byEventDamageSource()`; `unpreventable()`, `recordingDamageDealt()` (now any target or players) and `byTriggeringPermanent()` serve both.
Absorbed: `DealDamageToPlayersEffect` (and its handler, its body moved verbatim into `DealDamageEffectHandler.damagePlayers`: dealer
selection, every amount fixed before any damage (CR 608.2h), the pre-pool checks and log lines (Q3), the planeswalker prompt) and
`DealDamageToEachOpponentEqualToControlledPermanentCountEffect` (Q4). Dropped components: `triggeredTargetFilter` (no caller),
`attachedCountFilter` (Curse of Thirst, Q2). Ledger: 2 absorbed, 1 survivor. Ratchet 2,910 / 2,290 → 2,908 / 2,288. No
`CONVERSION_VERSION` bump.

**Card rewrite.** 704 card files (11 in `shared`), 759 calls, by a scratch script (balanced parentheses, 9 shapes, loud failure, leftover-name
check, imports; written, run, deleted): `new(a, p)` 741 (+ 4 `.byTriggeringPermanent()`, 4 `.byEventDamageSource()`, 1
`.recordingDamageDealt()`), `(a, you(), true)` 1 (`.unpreventable()`), `eachEvaluatedFor` 2, `toPlayerOrTheirPlaneswalker` 2,
`selectedOpponents` 1 (`to(except(acceptedPlayers(), you()), 5)`), Typhoon's record 3 (`eachEvaluatedFor(PermanentCount(filter,
CONTROLLER), eachOpponent())`). By hand: 9 typed locals (the script after a type rename), Curse of Thirst (`eachEvaluatedFor(PermanentCount(
allOf(Curse, PermanentAttachedToSourceControllerPredicate), ANY_PLAYER), triggeringPlayer())`), 17 engine builders (the injected player
handler is now `DealDamageEffectHandler`), 8 AI sites (`players()`, `damage()`), 22 test files, 15 agent-docs files. Oracle text checked
with `get_card` for 51 of 704 files: every specially shaped or hand-rewritten card (28; The Great Work through Urabrask) and 23 sampled;
all match; not checked: Zone of Flame (MB2, in no feed).

**Behaviour changes** (code only, no card outcome): Typhoon, Treacherous Terrain and Mjölnir now begin a DEAL_DAMAGE step, fix every
opponent's amount before dealing (CR 608.2h; a land or tapped-creature count can't change between them), deal 0 instead of skipping a
0 amount (the 0 path deals nothing), and check prevention once through `isDamageSourcePreventedWithLog` (also a
targeted-spell shield, which the pool already applied) instead of per opponent: the same outcome, another log line.

**Pins added first** (passed on the old code and after): Heartless Hidetsugu with Whip of Erebos, amounts fixed before the lifelink of the
first damage (CR 608.2h); Spiteful Shadows, a dead creature's last-known lifelink; Agate Instigator, a player prevention and the source's
lifelink on `eachOpponent()` damage; the existing Curse of Thirst, Curse of the Pierced Heart, Vial Smasher, Greatbow Doyen, Barbflare
Gremlin, Syphon Soul, Volcano Hellion, Snort, Typhoon, Stormbreath Dragon, Adarkar Wastes and AI mana tests. Failing-first: none (parity).

**Tests changed.** `DealDamageToPlayersEffectHandlerTest` is `DealDamageEffectHandlerTest.Players` (`@Nested`, four `@DisplayName`s
added); `StepTriggerServiceTest`'s attached-count test now checks the per-player amount (the component is gone); Curse of Thirst's and
Agate Instigator's unnamed tests got `@DisplayName`s; the rest only build the effect with the factories.

**Tests run** (one Gradle run, XML of this run): 8,067 result files, 73,460 tests, 0 failures: every card test class naming a card of
`DealDamageEffect` (2,287 cards with back faces' fronts), the `Damage|Prevent|Redirect|Lifelink|Infect|Curse|Planeswalker|PainLand|Ping`
classes, 59 application classes (the changed ones, both ratchets, `PermanentDamagePathTest`, `StepResultsTest`,
`SubjectResolutionServiceTest`, `GameDataDeepCopyTest`), the whole AI module.

**Seen, not changed** (defect queue, group 9): Aegis of Honor's redirect applied before the player pool in `resolveAnyTargetDamage`
(CR 616.1, Q6); the set handlers' face-down names in logs and missing DEAL_DAMAGE steps; the player kinds' pre-pool checks (Q3).
AGENTS.md's reuse example still names `new DealDamageToPlayersEffect(3, Subjects.you())` (the owner's file).

**For the next run.** The damage defect run (orchestrator's order): move the player kinds' `isPlayerDamagePrevented` /
`isDamageSourcePreventedWithLog` and the set handlers' protection pre-checks into the pools with D24's failing-first tests (CR 616.1),
begin DEAL_DAMAGE in the set handlers, read names through `FaceDownVisibility`; then M1 (set damage), M2 (mass), R (another dealer).

### Step 9, defect run D26 (2026-11-26): damage pools for set and player damage

The prerequisite of step 10's runs M1, M2 and R (plan `s10-damage2`), as D24 was of run Damage; no record merged, no card file, ratchet,
ledger, `CONVERSION_VERSION` or shared API changed.

**Sites changed** (each prevention and protection is now an option of the recipient's pool, ordered by its controller or the player,
CR 616.1, 615.1, 702.16e; the pre-checks and their own log lines are gone):
- The five set handlers (each-matching, permanents-target-controls, defending-player creatures, each-target, mass): no
  `isDamageSourcePreventedWithLog`, no protection pre-check (mass's sat in `DamageSupport.damageFilteredCreatures`, shared with Mob
  Verdict, the reveal-mana-value and each-player-and-their-creatures handlers; the pool's predicate is the same). Each-matching, defending
  player, each-target and mass begin DEAL_DAMAGE; each-matching and mass add a triggering dealer's damage to the resolving entry's step.
- `PermanentDamagePreventions` `Protection`: also noncombat damage the dealing entry's own source permanent deals as itself (each-matching
  passes it as the dealer; fight and bite carriers, which still pre-check, and Aura Barbs' carrier reach it too).
- `DealDamageEffectHandler`'s player kinds: `isPlayerDamagePrevented` and the or-their-planeswalker pre-check are gone (the player pool's
  `SourcePrevented` and `TargetSpellPrevented` apply).
- `DamageSupport.resolveAnyTargetDamage`'s player branch: Aegis of Honor's shield is no longer applied before the pool; the pool's
  `NextInstantOrSorceryRedirect` applies it once, in the player's order (CR 616.1, 614.9).

**Behaviour changes** (failing-first, each seen failing on the old code; `SetAndPlayerDamagePoolTest`, `StepResultsTest`): protection
(a red ability, or a red permanent dealing each-matching damage) or a red-source prevention beside a creature shield asks the creature's
controller the order, for each of the five set records; a red-source prevention beside a player shield asks the player for
`eachOpponent()`, `you()` and `toPlayerOrTheirPlaneswalker`; Aegis of Honor beside a player shield asks, the shield first leaving the
Aegis unused; a face-down source's prevented set damage names no hidden card (CR 708.2; the leaking lines were the protection lines);
each-matching, defending-player, each-target and mass damage begin their own DEAL_DAMAGE step ("that much" reads only theirs). From the
pool, not tested one by one: those preventions send the damage-prevented event (Selfless Squire); the defending-player handler judges
protection by the source's controller, not owner, with Ghostly Flame's colours (CR 702.16e). Logs: the pools' lines replace "X's damage
to controller is prevented." and "X's damage to Y is prevented.".

**Tests.** New `SetAndPlayerDamagePoolTest` (application, 20; pins passing before and after: protection prevents each set record's
damage, a red-source prevention each-target and each-opponent damage, Aegis redirects a Shock); 4 in `StepResultsTest`. Changed:
`DealDamageEffectHandlerTest.enteringPermanentDamageUsesItsControllerWithoutChangingAbilityOwnership` observed the dealer through the
removed Aegis call; it reads the damage event the pool is given. Run (XML of this run): 3,090 files, 26,420 tests, 0 failures (the
changed handlers' and the fight / bite / Aura Barbs records' card classes, the pattern classes, the AI module, both ratchets); the new
class, untracked and so missed by the selection, after it: 20, 0 failures.

**Seen, not changed** (defect queue): a player `DamageEvent` has no can't-be-redirected flag (no card needs it; sure); about 20 other
damage handlers still pre-check (D24's line).

**For the next run.** M1: the four set records onto `DealDamageEffect` over a set subject, no pre-pool difference left between them.

### Step 10, run Damage M1 (set damage) (2026-11-26)

Run M1 of the plan `s10-damage2` (orchestrator's decisions 2026-11-26). `DealDamageEffect` keeps its name and gains two `Recipient`
kinds, no component: `EACH_PERMANENT` (each permanent a non-target set `subject` names, found once, CR 608.2h) and `EACH_TARGET`
(each target, players included, CR 115.4; `targetFilter` the targeting restriction). Factories under Shared pieces. Absorbed, with
their handlers: `DealDamageToEachMatchingPermanentEffect`, `DealDamageToPermanentsTargetControlsEffect`,
`DealDamageToDefendingPlayerCreaturesEffect`, `DealDamageToEachTargetEffect`; the handler bodies moved into
`DealDamageEffectHandler.damageEachPermanent` / `damageEachTarget`. Ledger: 4 absorbed, 1 survivor. Ratchet 2,908 / 2,288 → 2,904 /
2,284. No `CONVERSION_VERSION` bump. Both set kinds answer `canDamageCreatures()` / `canDamagePlayers()` false (the records were no
`DamageDealingEffect`; AI parity), `GameSimulator.isAimedDamage` leaves them out, `TargetPolarityClassifier` keeps HARMFUL_DAMAGE for
each target by a branch (its name entry was dead). Specs as before: a set over `targetPlayer()` declares the player, any other set
is harmful with no target, each target `harmful(ANY_TARGET)` or narrowed.

**Card rewrite.** 112 card files (2 in `shared`) by a scratch script (balanced parentheses, 11 shapes, loud failure, leftover-name
check, imports; written, run, deleted): each-matching 49 calls (`controlledBy(eachPlayer() | targetPlayer(), filter)`), the
permanents-target-controls ones (`toEach(subject)`, `toEachCreatureOfTargetedPlayer`, `toEachControlledByTargetedPlayer`,
`toCreaturesOfTargetedPlayer`, two typed locals), defending player 5 (`controlledBy(defendingPlayer(), creature [and filter])`), each
target 32. By hand: Aggravate, Electryte, Lightmine Field (`triggeringAttackers()`), Winter Blast, the long-line wrapping; one engine
builder (`DealDamageToTargetPlayerControlsCreaturesThenPerpetuallyBoostEffectHandler` injects `DealDamageEffectHandler`), 2 AI
sites, 5 test files, 9 agent-docs files.
Oracle text checked with `get_card` for 25 of 112 files: every hand-rewritten card, the six defending-player cards, both target
restrictions (Rowan, Nahiri's Wrath) and 13 sampled; all match the new expression.

**Behaviour changes:**
- A set dealt damage only to its creatures, planeswalkers and battles (CR 120.1): Siege Dragon's filter ("each creature without
  flying that player controls") admitted lands, which were marked damage. Failing-first: `SiegeDragonTest.attackDealsNoDamageToLands`.
- Electryte deals damage to "each blocking creature" (Oracle), whoever controls it, not only the defending player's: the same with
  two players (no multiplayer test possible in the harness).
- Aggravate's rider is a step, `SetCombatRequirementThisTurnEffect(MUST_ATTACK, thisWay(DEAL_DAMAGE, creature))`: the same creatures
  (only those actually dealt damage), now with the requirement's log line.
- Code only: the defending player is the one the entry recorded (or the attacked target's), no longer the entry's target as a fallback
  (CR 506.2; the test entries now record it); each-matching no longer passes its dealer explicitly, like every other kind (a live source
  deals as itself either way; pinned with a recoloured dealer); the set is found before any damage instead of one filter check per
  permanent during the loop (CR 608.2h).

**Pins added first** (passed on the old code and after): `SetDamageTest` (application, 10: for each of the four shapes, a creature
arriving after the entry went on the stack is dealt damage and one gone isn't; a lifelink dealer gains the total once with one
Ajani's Pridemate counter; one "is dealt damage" trigger; deathtouch kills each member; a face-down dealer is named by no card; a
recoloured red dealer's set and target damage get past a red-source prevention); AI: `TargetPolarityGuardTest.setDamagePolarity`,
`SpellEvaluatorTest.setDamageSweeperIsNotScoredAsTargetedDamage`, `InstantCategoryClassifierTest.classifiesSetDamageAsNoRemoval`.

**Tests changed.** `SetAndPlayerDamagePoolTest`, `StepResultsTest`: the factories, and the defending-player entries record the
defending player instead of carrying it as a target. `TargetLegalityServiceTest`, `ValidTargetServiceTest`: the factories only.

**Tests run** (XML of this run): 2,912 result files, 27,957 tests, 0 failures (the card classes of every `DealDamageEffect` and
`SetCombatRequirementThisTurnEffect` card, the 368 classes naming a rewritten card, the
`Damage|Prevent|Protection|Redirect|Lifelink|Infect|Deathtouch|Excess|Sweep|Wrath|FaceDown` classes, the AI module's 59 classes, both
ratchets and the named application classes); after the CR 120.1 guard, 373 classes (the set cards and their readers): 3,655, 0 failures.

**Seen, not changed** (defect queue): the hard AI gives set damage no value (parity, pinned).

**For the next run.** M2: `MassDamageEffect` onto `toEach` (players beside permanents in one event, a per-creature amount, the tap
and exile riders, `BoardWipeEffect` for the AI); then R (another dealer).

### Step 10, run Damage M2 (mass damage) (2026-11-26)

Run M2 of the plan `s10-damage2` (orchestrator's decisions 2026-11-26). `MassDamageEffect` and its handler are absorbed into
`DealDamageEffect`'s `EACH_PERMANENT` kind over `controlledBy(eachPlayer(), filter)`; the kind takes `players` dealt the same damage in the
same event (`andTo`), the exile and can't-regenerate riders, and a per-permanent amount (`amountForEachPlayer` renamed
`amountForEachRecipient`: for a set, the permanent is the amount's source, every amount fixed first, CR 608.2h). Factories under Shared
pieces. `DealDamageEffect` implements `BoardWipeEffect` (`sweepsBoard()`: an all-player set). Ledger: 1 absorbed, 1 survivor. Ratchet
2,904 / 2,284 → 2,903 / 2,283. No `CONVERSION_VERSION` bump.

**Card rewrite.** 241 card files (1 in `shared`), 266 calls, by a scratch script (balanced parentheses, the 11 constructor arities and four
factories resolved to components, typed locals, loud failure on any other shape, leftover-name check, imports added and sorted; written,
run, deleted): `toEachCreature(n)` 112, `toEachCreature(n, filter)` 127, `toEachCreatureAndPlaneswalker(n)` 17, `(n, filter)` 3,
`eachEvaluatedFor` 1, riders `andTo(eachPlayer())` 72, exile 6, `cantBeRegenerated` 1, `byTriggeringPermanent` 1. By hand: Falling Star
(the tap a step, `TapPermanentsEffect(thisWay(DEAL_DAMAGE, creature))`), Into the Fire and Magmaquake and Battle of Frost and Fire
(`toEach(controlledBy(eachPlayer(), anyOf(...)))`), Cavalier of Flame ("each planeswalker they control": `controlledBy(eachOpponent(),
planeswalker)`), Riveteers Confluence; the Kindle the Carnage handler, 3 AI sites, 7 test files, 7 agent-docs files. Oracle text checked
with `get_card` for 42 of 241 files: every hand-rewritten and specially shaped card (exile, can't regenerate, per creature, triggering
dealer, filter on planeswalkers) and 22 sampled; all match the new expression.

**Behaviour changes:**
- Riveteers Confluence ("each creature and planeswalker you don't control", Oracle) dealt damage to its controller's planeswalkers: the
  filter applied to creatures only. Failing-first: `RiveteersConfluenceTest.damageModeSparesOwnPlaneswalker`.
- The set kind's amount reads the resolving entry with the dealer as source (the carrier of a triggering dealer has no event value):
  Arcbond's "that much" (CR 608.2h); M1's Ib Halfheart (fixed 4) unchanged.
- AI only: an all-player set is a board sweep for the hard AI's board-wipe value, `BoardEvaluator` and the easy AI, so M1's all-player
  each-matching cards are read as sweeps too; the set's spec is harmful with no target (read only by `GameSimulator.rankAbilityTargets`).
- Code only: players in APNAP order (one event), planeswalkers and battles by the layered type in the set filter, then the printed-type
  recipient guard; Falling Star taps after the damage instead of between creatures.

**Pins added first** (passed on the old code and after): `MassDamageTest` (application, 8: a creature arriving after the entry went on the
stack is dealt damage and one gone isn't; a lifelink dealer gains the total for creatures and players once with one Pridemate counter;
Fungus Sliver once; Chandra's Spitfire once beside the creatures; one Magmatic Galleon excess trigger for two creatures; deathtouch kills
each; a face-down dealer is named by no card); the existing card tests of Baki's Curse, Falling Star, Anger of the Gods, Underworld Fires,
Flamebreak, Arcbond, Into the Fire and the AI's `SpellEvaluatorTest` / `EasyAiDecisionEngineTest` sweeper cases.

**Tests changed.** `MassDamageEffectHandlerTest` is `DealDamageEffectHandlerTest.Mass` (4); `SetAndPlayerDamagePoolTest`,
`StepResultsTest`, `TriggerRouteRecordTest`, `EasyAiDecisionEngineTest`: the factories; `SpellEvaluatorTest`: one `@DisplayName` named the
deleted record; `StepTriggerServiceTest`: the renamed component.

**Tests run** (one Gradle run, XML of this run): 3,163 result files, 29,603 tests, 0 failures: every card test class of a
`DealDamageEffect` card (back faces through their front), the 513 classes naming a rewritten card, the
`Damage|Prevent|Protection|Redirect|Lifelink|Infect|Deathtouch|Excess|Sweep|Wrath|FaceDown` classes, the AI module's 59 classes, both
ratchets and the named application classes.

**Seen, not changed** (defect queue): a set over one player's permanents or each target is still unscored by the hard AI (M1's line,
updated). AGENTS.md names no deleted class.

**For the next run.** R (another dealer: bite, fight-like power damage, `TargetCreatureDealsPowerDamageToEachOtherCreatureEffect`, the
`ControlledCreaturesDeal…` records). Left out of M2: `DealDamageToEachPlayerAndTheirCreaturesEqualToControlledCreatureCountEffect` (an
amount per recipient's controller) and `DiscardRandomCardThenMassDamageEffect` (its repeat loop; it builds `toEachCreature`).

### Step 10, run Damage R (another dealer) (2026-11-26)

Run R of the plan `s10-damage2` (orchestrator's decisions 2026-11-26; scratch plan `r-plan.md`). `DealDamageEffect` keeps its name and
gains one component, `PermanentSubject dealers`, set by the rider `dealtBy(dealers)` on the one-target, named-permanent (`source()`,
`targetDeclaredElsewhere()`), target-group (`target(g)` for the creature and permanent kinds), set and player kinds. Absorbed, with
their handlers: `TargetDealsPowerDamageToTargetEffect`, `TargetCreatureDealsDamageToControllerEffect`,
`TargetCreatureDealsPowerDamageToEachOtherCreatureEffect` and its `…AndEachOpponentEffect`, `TargetCreatureDealsDamageToSelfEffect`, the
five `ControlledCreaturesDeal…` records. Factories under Shared pieces. Ledger: 10 absorbed, 1 survivor, 1 blocked-other
(`TargetCreatureDealsPowerDamageToAnyTargetEffect`, Ram Through only). Ratchet 2,903 / 2,283 → 2,893 / 2,273. No `CONVERSION_VERSION` bump.

**Premise.** Each record was "each dealer deals its own damage through a carrier"; they differed in recipients and pre-pool checks.
`DealDamageEffectHandler.damageByDealers`: recipients found once (CR 608.2h); each dealer through `entryDealtByPermanent` (a target
dealer only while on the battlefield, CR 608.2b; any other as it is or last was), amount per dealer, 0 deals nothing (CR 120.8), the
dealer's "can't be prevented" (CR 615.12), its damage added to DEAL_DAMAGE, excess against the dealer (CR 120.4a; Ravenous Pursuit's
perpetual boost joined the excess readers, so the `recordExcessDamage` flag went). The source's own power damage is the plain form, the
triggering dealer `byTriggeringPermanent()`. Specs and AI answers kept per shape (`dealerTargetSpec`, `canDamage…`, `sweepsBoard`, one
`TargetPolarityClassifier` branch, `isAimedDamage`); the four excess bites now share the benign two-group spec (spells; read only by the
activated-ability ranking).

**Card rewrite.** 138 card files (none in `shared`), 147 calls, by a scratch script (balanced parentheses, 27 shapes, loud failure,
typed locals, leftover-name check, imports, new long lines wrapped; written, run, deleted): bite 88 (`bite` 66, `targetCreatureToItself`
10, the plain source form 4, `byTriggeringPermanent` 1, set or Aura dealers 3, `to(source())` 4; one typed local), any target 23,
to players 18, each other creature 8, controlled creatures 9, itself 1. By hand: Self-Destruct's second typed local, Vengeful Ancestor,
`DamagePreventionService` (Judgment of Alexander), 3 AI sites, `TargetPolarityGuardTest`, 12 agent-docs files. Oracle text checked with
`get_card` for 35 of 138 files (every set, Aura, triggering, source-recipient, player and each-other shape and 12 bites); all match.

**Behaviour changes:**
- The bite's protection and "prevented from dealing damage" are pool options ordered by the recipient's controller (CR 616.1, 702.16e;
  D24's rule): a red creature's bite to Silver Knight beside a shield asks the order. Failing-first:
  `DealerDamageTest.ProtectionInThePool.protectionAndShieldOrdered`. Log lines: the pool's replace "X's damage is prevented.".
- Vengeful Ancestor: "it deals 1 damage to its controller" is the triggering attacker (`controllerOf(triggering())`,
  `byTriggeringPermanent()`), no longer a target slot naming it (its spec declared a creature target the Oracle text doesn't have).
- A departed triggering bite dealer (Lothlórien Blade) deals as its departure snapshot, not its power as the ability triggered (CR 608.2h).
- Code only: Case of the Gateway Express's and Sarkhan the Masterless's fixed damage goes through each creature's carrier.

**Pins added first** (passed on the old code and after): `DealerDamageTest` (application, 7, beside the failing-first one): bite lifelink and
deathtouch, infect, Justice Strike, each other creature, Moonlight Hunt's Wolves only, a face-down dealer, protection; the existing card
tests (Rabid Bite, Aggressive Instinct, Terrific Team-Up, Animist's Might, Windswift Slice, Contest of Claws) and `TargetPolarityGuardTest`.

**Tests changed.** The deleted handler's two mocked unit tests went (their cases are the card tests above); `TargetPolarityGuardTest`
uses the factory. **Tests run** (XML of this run): one Gradle run of 3,049 classes, 3,278 files, 27,879 tests, 13 failures, all fixed
(Thrash / Threat and Giantfall: a modal bite to any target reads its group; Windswift Slice, Ravenous Pursuit, Band Together: the dealer's
"can't be prevented"; Vengeful Ancestor); rerun of the 253 classes naming a rewritten card or the changed tests: 2,322 tests, 0 failures.

**Seen, not changed** (defect queue): `resolveAnyTargetDamage` ignores the entry source's "can't be prevented" for permanents (unverified).
Left out (Step-10 hand-offs): Ram Through, fight, the one-card dealer records, Enchanter's Bane / Star Athlete (a "sacrifice it" ransom).

**For the next run.** The excess redirect (`DealDamageToTargetCreatureWithExcessToControllerEffect`, 4 cards) with Ram Through as one
rider "excess dealt to that creature's controller instead" (CR 120.4a), optionally when the dealer has trample; then the rest of the
"unless" family.

### Step 10, run A (card movers) (2026-11-26)

Run A of the plan `s10-rest` (orchestrator's decisions 2026-11-26, "the rest of step 10", entered by this run). Absorbed, with their
handlers: `ReturnSourceCardFromGraveyardToOwnerHandEffect` and `ReturnTriggeringCardToOwnerHandEffect` into `new PutCardsIntoHandEffect(
Subjects.sourceCard() | triggeringCards())`, `ExileSourceCardFromGraveyardEffect` and `ExileTriggeringCardFromGraveyardEffect` into
`ExileGraveyardCardsEffect.cards(...)`. Ledger: 4 absorbed, 2 survivor. Ratchet 2,893 / 2,273 → 2,889 / 2,269. No `CONVERSION_VERSION` bump.

**Premises.** Held: each handler found its card as the subject does (`sourceCard()`: the entry's card in its recorded graveyard stay;
`triggeringCards()`: the triggering card with `isTriggeringCardIncarnation`; a per-card event sets no `triggeringCards` list, so no second
card is named); no `MayEffect` / `ChooseOne` pre-check or AI reader names them. Not held: (a) `PutCardsIntoHandEffect` records no RETURN
step, and neither did the old hand records, so nothing changes there; the exile records already recorded EXILE as the survivor does.
(b) The `(true)` file (Skyfisher Spider, "exile this card from your graveyard") takes `Subjects.inYourGraveyard(sourceCard())`, the wording's
own subject, not the plan's `ownedBy(…, you())` (same result).

**Card rewrite.** 122 card files (1 in `shared`), 130 calls, by a scratch script (balanced parentheses, five shapes, loud failure on any
other, leftover-name check, imports; written, run, deleted): hand `sourceCard()` 57, hand `triggeringCards()` 11, exile `sourceCard()` 45,
`inYourGraveyard(sourceCard())` 1, exile `triggeringCards()` 16; new long lines wrapped. By hand: the Avatar Destiny mill handler (injects
`PutCardsIntoHandEffectHandler`), `ReturnEnchantedCreatureToOwnerHandOnDeathEffectHandler`'s follow-up (Puppet Master, the Zendikons),
`CloneService`'s reflexive "exile that card", three sibling Javadocs, 4 test files, 7 agent-docs files. Oracle text checked with `get_card`
for 121 of 122 files (Bone Rattler, MB1 35, isn't in the data); all match the new expression.

**Behaviour changes** (log only; the survivor's line wins): "P puts X from their graveyard into their hand." for "X returns from graveyard
to P's hand."; "S exiles X from P's graveyard." for "X is exiled." / "X is exiled by S."; the "'s ability fizzles (card not in graveyard)."
line is gone. Failing-first: `AngelicDestinyTest.logsTheReturnFromTheGraveyard`, `CyclopeanMummyTest.logsTheExileFromTheGraveyard`.

**Pins** (passed on the old code and after): `AngelicDestinyTest.doesNotReturnANewGraveyardObject` (CR 400.7, new); the existing
Skyfisher Spider (stolen Spider), Enduring Renewal and Planar Void (left and came back), Cyclopean Mummy, Avatar Destiny (no creature
milled, Tormod's one batch), Puppet Master, Ashnod's Intervention (the exile trigger) and `StepResultsTest` cases.

**Tests changed.** `EnduringRenewalTest` (2): asserted the deleted "fizzles" line; now that no card goes to a hand (and the moved card
stays in exile). `StepResultsTest`, `MayAbilityHandlerServiceTest`, `ClassKeyedRecordCollectorTest`, `BridgeFromBelowTest`: the factory.
**Tests run** (one Gradle run, XML of this run): 539 classes (3 abstract skipped), 744 result files, 5,872 tests, 2 failures (the
`EnduringRenewalTest` log lines, fixed; rerun 14 tests, 0 failures): every changed card's class and the 219 classes naming one, the
`Return|Hand|Graveyard|Exile|Dredge|Recursion|Bounce` classes, the readers' tests, the AI module's 59 classes, the ratchets.

**Seen, not changed** (defect queue): `CloneService`'s reflexive "exile that card" records no graveyard stay (CR 400.7). AGENTS.md
names no deleted class.

**For the next run.** X (the excess redirect with Ram Through, `s10-rest` plan section 4), then S.

### Step 10, run X (excess redirect) (2026-11-26)

Run X of the plan `s10-rest`. `DealDamageEffect` gains `ExcessRedirect excessRedirect` (`NONE`, `TO_CONTROLLER`,
`TO_CONTROLLER_IF_DEALER_HAS_TRAMPLE`), riders `excessToControllerInstead()` / `excessToControllerInsteadIfDealerHasTrample()`, valid on one
target creature dealt by the source or a target creature. Absorbed with their handlers: `DealDamageToTargetCreatureWithExcessToControllerEffect`,
`TargetCreatureDealsPowerDamageToAnyTargetEffect`. Ledger: 2 absorbed, 1 survivor. Ratchet 2,889 / 2,269 → 2,887 / 2,267. No bump.

**Premises.** Held: the excess is worked out before the damage from the amount, ignoring prevention, replacement and indestructible (the
four cards' rulings; the Obosh and prevention-shield pins); `readsExcessDamage` already knows Megatron's condition. Not held: (a) the plan's
`Keyword` component became one enum value (one card reads a keyword); (b) "one event" and the dealer's last-known power already held (both
old handlers dealt inside one effect's damage event; `SourcePower` read the snapshot), so their tests are pins; (c) the excess comes before
the source's replacements (Obosh), not after them as the plan says.

**Handler.** `DealDamageEffectHandler.excessToController`, on the target path and the bite path: the dealer's trample (trample form), the
controller found at resolution, the excess from `ExcessBaseline` on the raw amount (greatest over creature / planeswalker / battle, the
dealer's deathtouch); the creature is dealt the rest, the controller the excess, in the effect's damage event; the redirected excess is the
event value when a later step reads it. AI: `canDamagePlayers()` true for the plain rider (the old record's view); Ram Through is a bite.

**Cards by hand** (5): Flame Spill / Pigment Storm `toTargetCreature(4 | 5)`, Ravenous Tyrannosaurus `toTargetCreature(new SourcePower())`,
each `.excessToControllerInstead()`; Megatron `SequenceEffect.of(… .excessToControllerInstead(), new ConditionalEffect(new
EventValueAtLeast(1), new TransformSelfEffect()))`; Ram Through `bite().excessToControllerInsteadIfDealerHasTrample()`. Oracle text checked
with `get_card` for all 5, rulings for 4. 3 agent-docs files.

**Behaviour changes** (failing-first, each failed on the old code): a creature that is also a planeswalker takes the greatest excess
(CR 120.4a; `FlameSpillTest.creaturePlaneswalkerTakesTheGreatestExcess`); the DEAL_DAMAGE step is begun anew, an earlier damage step not
counted (`ExcessDamageRedirectTest.theDamageStepBeginsAnew`). Code only: Ram Through's "prevented from dealing damage" is the pool's (CR 616.1).

**Pins** (passed before and after): `RamThroughTest.lifelinkDeathtouchTramplerDealsOneEvent` (one gain, one Pridemate counter),
`RavenousTyrannosaurusTest.leavingBeforeTheTriggerResolvesDealsItsLastKnownPower` (CR 608.2h), `FlameSpillTest.preventionOfThePlayersDamage…`,
`ExcessDamageRedirectTest.creatureAndControllerDamageIsTheDamageStep`, the five cards' tests; new after: `theExcessIsTheEventValue`.

**Tests changed.** `FlameSpillTest` (7), `RamThroughTest` (8): `@DisplayName`s only. `ExcessDamageRedirectTest`'s failing-first tests moved
to the factory. **Tests run** (one Gradle run, XML of this run): 3,020 result files, 25,914 tests, 0 failures (every `DealDamageEffect`
card's class, the 7 naming a changed card, the `Damage|Excess|Lifelink|Deathtouch|Trample|Prevent|Redirect` classes, the AI module,
`DealerDamageTest`, `StepResultsTest`, the ratchets); `ExcessDamageRedirectTest` (untracked, missed by the list) alone: 3, 0 failures.

**Seen, not changed** (defect queue): the excess ignores other sources' damage dealt at the same time (CR 120.4a). AGENTS.md names no
deleted class.

**For the next run.** S, then G (`s10-rest` plan section 2). The Damage family's step-10 records are done; the rest are under Step-10 hand-offs.

### Step 10, run S (attack and block restrictions) (2026-11-26)

Run S of the plan `s10-rest` (the orchestrator's decision Q1, already entered under the owner's decisions). Absorbed: `CantAttackUnlessEffect`,
`CantBlockUnlessEffect` into the survivor `CantAttackOrBlockUnlessEffect(condition, requirementDescription, restrictsAttacking,
restrictsBlocking)` with `attack` / `block` / `attackOrBlock` (CR 508.1c, 509.1b); `EnchantedCreatureCantActivateAbilitiesEffect` into
`EnchantedPermanentCantActivateAbilitiesEffect.includingManaAbilities()` (CR 602.5); `ControlledCreaturesCantAttackUnlessPredicateEffect` into
`CreaturesCantAttackUnlessPredicateEffect.youControl(exemption)`. Static records, no handlers. Ledger: 4 absorbed, 3 survivor. Ratchet
2,887 / 2,267 → 2,883 / 2,267. No `CONVERSION_VERSION` bump.

**Premises.** Held: the attack and block readers read the trio the same way (face-down and lost-abilities guards, printed plus granted
statics, one `ConditionContext.forPermanent`); nothing reads `requirementDescription`, no AI reader; the Arrest record equals the `(true)`
lock in its three readers. Not held: (a) the plan's `creature()` factory became `includingManaAbilities()` (what it does; the 3 `(true)` cards
moved too); (b) `youControl` needs no component: the exemption is `anyOf(X, not(PermanentControlledBySourceControllerPredicate))`, judged with
the source context the global branch already passes (the four predicates read no context); (c) 145 card files, not 148. **Survivor.** `AttackOrBlockRestrictionEffect.cantAttackOrBlockUnless()` is renamed `cantAttackUnless()` (the attack side); the survivor also
implements `BlockingRestrictionEffect.cantBlockUnless()` (the block side). `AttackLegalityService.isCantAttackUnlessConditionUnmet` and
`BlockLegalityService.hasUnmetBlockRequirement` read one facet each; every other `BlockingRestrictionEffect` facet stays inert for it. The
survivor's 25 cards moved to `attackOrBlock` (no two-argument constructor kept: one spelling). **Card rewrite.** 145 card files, 152 calls, by a scratch script (prefix shapes, an import fix-up, a leftover-name check failing loudly;
written, run, deleted): `attack` 66, `block` 28 (Tesak's helper's return type with it), `attackOrBlock` 25, `includingManaAbilities()` 25 + 3,
`youControl` 4. Oracle text checked with `get_card` for 143 of 145 (Avacyn's Collar MB2 273 and Lazier Goblin MB1 56 aren't in the data);
all match the factory chosen.

**Behaviour changes.** None in play; an Arrest-style refusal now reads "(enchanted permanent lock)", not "(Arrest)" (no test asserted it). **Pins** (passed on the old code and after): `DesperateCastawaysTest.canAttackWithoutArtifactWhileItHasLostItsAbilities`,
`MaraudingBoneslasherTest.canBlockAsOnlyZombieWhileItHasLostItsAbilities`, `BlindSpotGiantTest.canAttackAsOnlyGiantWhileItHasLostItsAbilities`
(Humility; the readers' lost-abilities guard), plus the existing Veiled Serpent (granted), Tesak (granted block side), Evil Eye, Akron Legionnaire,
Glacial Chasm (controller scope), Arrest and Ice Cage (mana abilities) tests. No failing-first test: no behaviour change.

**Tests changed.** `AbilityActivationServiceTest`: 59 strict stubs of the deleted class (all `false`) removed; the two Arrest tests stub the
predicate overload with a matcher that accepts `includingManaAbilities()`. **Tests run** (one Gradle run, XML of this run): 476 classes, 608
result files, 5,874 tests, 0 failures (every changed card's class and the classes naming one, 360; the `CantAttack|CantBlock|Attack|Block|
Restriction|Combat|Activate` classes, `AbilityActivationServiceTest`, `GameQueryServiceTest`, the AI module, both ratchets).

**Seen, not changed** (defect queue): the planechase branch of `isCantAttackDueToGlobalRestriction` judges a plane's exemption without its
filter context (latent: no plane uses a source-relative one). AGENTS.md names no deleted class; `EFFECT_MERGE_PLAN.md` keeps one historical mention.

**For the next run.** G (goad, `s10-rest` plan section 2): `GoadTargetCreatureUntilNextTurnEffect` with a player-target spec, the GOAD step
and the CR 701.15d check for the snapshot and target-player records.

### Step 10, run G (goad) (2026-11-26)

Run G of the plan `s10-rest`. Absorbed with their handlers: `GoadCreaturesUntilNextTurnSnapshotEffect` (6 card files) into
`GoadTargetCreatureUntilNextTurnEffect.eachCreature(filter)` = `of(controlledBy(eachPlayer(), creature and filter), UNTIL_YOUR_NEXT_TURN)`, and
`GoadCreaturesTargetPlayerControlsUntilNextTurnEffect` (4) into `creaturesTargetPlayerControls()` / `creaturesControlledBy(PlayerSubject)`. The
survivor's `targetSpec()` is the harmful player target when its subject declares one (`controlledBy(targetPlayer(), …)`, as `BoostEffect`). Ledger:
2 absorbed, 1 survivor. Ratchet 2,883 / 2,267 → 2,881 / 2,265. No `CONVERSION_VERSION` bump.

**Premises.** Held: both records locked the set as they resolved, as the survivor does; neither had a GOAD step, the CR 701.15d check or the
face-down source name; no engine, AI or test reader named either record (the attack readers go through `CombatAttackRequirementEffect` /
`GoadStatusEffect`); the subject resolver's filter context carries what the snapshot handler passed (source card, controller, permanent,
snapshot), so Day of the Moon's chosen names still match (its tests pass). Not held: the floats' `sourcePermanentId` (always set by the old
handlers, only for `WHILE_SOURCE_REMAINS` by the survivor) is read by no "until your next turn" goad reader, so dropping it changes nothing.

**Cards by hand** (10): Day of the Moon, Disrupt Decorum, Fast Forward, Nelly Borca, Take the Bait, Taunt from the Rampart on `eachCreature`
(their filters unchanged); Geode Rager, Maestros Confluence (`::creaturesTargetPlayerControls` as the mode's supplier) on
`creaturesTargetPlayerControls()`; Komainu Battle Armor, Marisi on `creaturesControlledBy(triggeringPlayer())`. Oracle text checked with `get_card`
for all 10; Kaima's rulings with `get_rulings` (the locked set only). 4 agent-docs files.

**Behaviour changes.** The ten cards' goads are a GOAD step under the survivor's CR 701.15d check, so a creature the same player already goaded
for as long isn't goaded or counted again, across these cards and the survivor's (defect run D20's reading of CR 701.15d for Kaima). Failing-first:
`KaimaTheFracturedCalmTest.creatureGoadedByYourOtherSpellIsNotCountedAsGoadedThisWay` (Disrupt Decorum, then Kaima: no counter; the old code gave
one). Code only: the float's source name through `FaceDownVisibility`; the AI reads Geode Rager's and Maestros Confluence's player target through
the survivor's HARMFUL entry. The record's and `StepKind.GOAD`'s Javadoc said a re-goad "is recorded / counts", contradicting D20's handler: corrected.

**Pins** (passed on the old code and after): `DisruptDecorumTest.castTwiceAddsOneRequirement`, `MaestrosConfluenceTest.goadModeChosenThriceAddsOneRequirement`
(CR 701.15d on the attack side), `GeodeRagerTest.mayTargetItsController`, plus the ten cards' existing tests (later creatures not goaded, the
Saga gone before resolution, Marisi's untargeted hexproof player, control change).

**Tests changed.** `GeodeRagerTest` (3) and `MaestrosConfluenceTest` (10): `@DisplayName`s added only. **Tests run** (one Gradle run, XML of this run):
174 classes, 265 result files, 5,097 tests, 0 failures (the 49 survivor, dungeon-venture and goad-builder cards' classes and the classes naming one,
the `Goad|Attack|Combat|MustAttack|Dungeon|Venture` classes, `TriggerRouteRecordTest`, the AI module, both ratchets).

**Seen, not changed.** None new. AGENTS.md names no deleted class. `GoadCreaturesUntilNextTurnEffect` (Kardur, Maximum Carnage, a rule that covers
later creatures) and `GoadEquippedCreatureEffect` (static) stay out.

**For the next run.** D (draw, `s10-rest` plan section 2): the AI own-draw helper first, then `DrawCardEffect.eachEvaluatedFor` with every amount
fixed before the first draw (CR 608.2h).

## Prompt

Replace `<N>` with the step number. For step 9, add the run kind and its scope (`step 9, triage run: Destroy, Search`;
`step 9, fold run: the step-9 rows for <survivor>`).

```
Implement step <N> of effect-cleanup-refactor.md. Read the file from the top down to `## Findings` (Status, Design, Ground
rules, your step's section, Standing knowledge) and the Findings sections your run follows on; for an older run's details, grep
`git show ef0278a44e:effect-cleanup-refactor.md` (or `6728593a9a` before 2026-11-23) for the run, class or card name instead
of reading it. Check in the Status table that
every step yours depends on is done; if one isn't, stop and tell me. Follow the ground rules: read agent-docs/ARCHITECTURE.md
and agent-docs/architecture/resolution.md before touching engine or domain code, verify the premise by reading the effect
records and their handlers to the end before editing, keep behaviour identical unless the old behaviour was rules-wrong (then
cite the Oracle text or ruling and add a test), keep the conventions under Standing knowledge, verify every CR number with
get_rule, and test behaviour through the engine. Run only the tests the step names, with fully qualified class names and one
--tests list per Gradle test task; never the full suite, ask me for it. When the run is done: update agent-docs for every class
you removed, add the audited classes to scripts/effect-audit-ledger.csv, lower the EffectCountRatchetTest baselines, update the
Status row and Standing knowledge (new shared pieces, conventions, fixed or new open defects, owner's questions), add your
run's line to the Findings list and append its section under Findings, and tell me which classes you deleted, which tests changed and why, and any behaviour change.
Don't commit until I say so.
```
