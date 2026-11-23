# Effect cleanup: one verb, any subject

A step-by-step plan to stop writing a new effect class for every combination of *what happens* and *who it happens to*. An
effect names its verb (boost, destroy, draw, lose life) and takes a **subject** (this permanent, the target, the enchanted
creature, each opponent, that permanent's controller, the cards exiled this way) as an ordinary component. One engine service
resolves every subject. Effects that fuse two instructions become steps that talk through the resolution's step results.

This file was cut down on 2026-11-23. The full text, with every run's Findings (steps 1 to 9, run 40), is in git history:
`git show 6728593a9a:effect-cleanup-refactor.md` (grep it for a class or card name; don't read it whole).

**How to read it.** Read from the top down to `## Findings`, then the last run's section under Findings. Everything a run needs
is above `## Findings`: the Standing knowledge section replaces the older Findings. To start a step, use the prompt at the
bottom.

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
| 8 | Split the fused effects (repeatable) | done (runs 1 to 29; "counter unless": option B now, option C for step 10) |
| 9 | Sweep the single-card tail (repeatable) | open. Runs 1 to 41 closed Exile, Each, Return, Put, Reveal, Choose, Grant, Create, Prevent, May, Register and Target. 1,234 rare records have no ledger row. Triage run 1 gave Pay and Reduce (80 rare) and 32 stale step-4/5 blocked rows their verdicts; triage run 2 gave Search and Cant (79 rare) theirs; triage run 3 gave Sacrifice and Destroy (75 rare) theirs; triage run 4 gave Look and Remove (71 rare) theirs; triage run 5 gave Add and Gain (64 rare) theirs; triage run 6 gave Become, Draw and Double (85 rare) theirs; triage run 7 gave Set, Copy and Seek (79 rare) and two stale step-4/5 rows theirs (434 `step-9` rows wait for fold runs). Defect runs 1 and 2 fixed most of the defects the triage runs saw (their Findings). From 2026-11-23 the step runs as **triage runs** (about 80 records each, verdicts only) followed by **fold runs** (see step 9). Open: Perpetually (30 rare) and Conjure's hand and library conjures (Conjure 39 rare) wait for the owner's decisions; the next families with no owner's question are Deal (25 rare), Cast (25), Allow (25), Boost (23), Specialize (19) and Opponent (19). |
| 10 | Collapse the head: one record per verb (repeatable, needs sign-off) | open |
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

## Measurements (2026-11-23, at `6728593a9a`)

Re-measure with `python3 -I scripts/effect-inventory.py` (no arguments: the summary below; `--family`, `--siblings`, `--dead`,
`--unaudited [--family P]`, `--tests-for Class... [--max-per-class N]`).

| What | Count |
|---|---|
| `EffectCountRatchetTest` baselines (records / handler classes in `normalfx`, `mayfx`, `staticfx`) | 4,046 / 3,300 |
| Types in `model/effect/` | 4,340 (4,046 records, 43 enums, 248 interfaces, 3 classes) |
| Handler files in `service/effect/normalfx` | 3,013 files, 264,482 lines |
| Records used by exactly one card / two cards | 2,354 / 404 |
| Dead records (named only by their own handler) | 7 |
| Subject-sibling groups | 116 groups, 264 classes, 148 removable |
| Records with a nested `CardEffect` (or a `List<CardEffect>`) | 136 (36) |
| Names containing `Then` / `And` | 207 / 434 |
| Handlers that `switch` on a recipient / scope / reference component | 20 |
| Classes with no ledger row (`--unaudited`) | 2,423 |
| **Rare records with no ledger row** (records used by at most two cards, zero included) | **1,767** (114 of them used by no card: engine-built records) |

Count the rare ones with `python3 -I scripts/effect-inventory.py --unaudited | awk 'NR>1 && $3<=2 && $2=="record"' | wc -l`.
By first word: Search 40, Reduce 40, Pay 40, Conjure 39, Cant 39, Sacrifice 38, Look 37, Destroy 37, Remove 34, Gain 32, Add 32,
Perpetually 30, Become 30, Draw 28, Set 27, Double 27, Seek 26, Copy 26, Deal 25, Cast 25, Allow 25, Boost 23, Specialize 19,
Opponent 19, Shuffle 18, Attach 18, Replace 17, Redirect 17, Protection 17, Enchanted 17.

`scripts/effect-audit-ledger.csv` (header `class,verdict,step,note`): 2,957 rows over 2,637 classes. Latest verdict per class:
unique 722, absorbed 504, split 454, blocked-other 409, survivor 259, blocked-step-model 149, blocked-player-loop 83, dead 50,
`step-9` 7.

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
- `CardSubject` (decided 2026-11-20): cards outside the battlefield. `cardsThisWay(kind[, filter])`, `targetCard(filter, scope)`,
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

`source()` is strict: it never falls back to the entry's target (steps 3 to 5 probed every user).

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
  step (`SequenceEffect` has no gate of its own). "When you do" is reflexive (CR 603.12): check the Oracle text, as step 8 found
  ten cards with the wrong one.
- "X and Y" in one sentence is often one event (one damage event, deaths together): `SequenceEffect.simultaneous` or the survivor's
  batch form, not two sequential steps.
- One target shared by the steps stays one entry target; with two targets, each step names its target group. The cast-time target
  prompt must not change.
- A trigger slot holds one effect, so a split on a trigger slot stays one `SequenceEffect`.
- The first half must be an earlier step (above); otherwise the record is `blocked-step-model`.

### Survivors keep their names

Until step 10 a family folds into its most used record, which gains a component and keeps its existing constructors as overloads,
so a fold touches the absorbed classes' cards and not the survivor's. Card call sites of a deleted class or enum are rewritten by a
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
  new counts, update the Status row, and append the run's section under Findings.
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

**Fold runs** take the `step-9` rows (`--unaudited` does not list them: grep the ledger), one survivor's batch or more per run, and
give each a verdict in this order of preference:

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

**Runs 1 to 41, in short.**

| Family | Runs | Result |
|---|---|---|
| Exile | 1–7 | closed; the random-card, exile-until, hand, same-name, face-down pile and timed-permission steps |
| Each | 7–9 | closed; 33 rows `blocked-player-loop` (choose in turn, then act) |
| Return | 10–11 | closed; the put / return steps over card subjects, stay checks on every return |
| Put | 11–12 | closed; `PutCardsIntoLibraryEffect`, five `CardSubject` variants |
| Reveal | 13 | closed; `RevealCardsFromTopUntilEffect`, `RevealCardsFromHandEffect`, `DiscardCardsEffect` |
| Choose | 14–17 | closed; the chosen name, player, permanents and card steps |
| Grant | 18 | closed |
| Create | 19–20 | closed; token copies read copiable values |
| Prevent | 20–24 | closed; `PreventDamageToRecipientsEffect`, preventions moved into the damage pools |
| May | 25–27 | closed; 11 cost combinators listed for step 10 |
| Perpetually | 27–29 | **open**: 30 rare, waiting for the owner's logging and "it" decisions |
| Conjure | 29–31 | **open**: 39 rare; the hand and library conjures wait for the logging decision; the graveyard-ability store (the unearth duplicate) and the random-spellbook battlefield records left |
| Register | 30–34 | closed; the delayed step and event registrations, `NoteAmountEffect` |
| Target | 32–41 | closed; `PlayerRestrictionEffect`, the gain-control and choose-permanents choosers, one cast check |
| Pay, Reduce (+ 32 stale step-4/5 rows) | T1 | triaged, no code changed: 112 rows (`step-9` 88, `blocked-other` 7, `unique` 17); fold batches in T1's Findings |
| Search, Cant | T2 | triaged, no code changed: 79 rows (`step-9` 52, `blocked-other` 1, `unique` 26); fold batches in T2's Findings, among them a SEARCH step kind |
| Sacrifice, Destroy | T3 | triaged, no code changed: 75 rows (`step-9` 54, `blocked-other` 10, `blocked-player-loop` 1, `unique` 10); fold batches in T3's Findings, among them a delayed ability about the entry's target |
| Look, Remove | T4 | triaged, no code changed: 71 rows (`step-9` 53, `unique` 18); fold batches in T4's Findings, among them one store for counters on exiled cards and a player-counter removal |
| Add, Gain | T5 | triaged, no code changed: 64 rows (`step-9` 49, `blocked-other` 3, `unique` 12); fold batches in T5's Findings, among them one counter-placement replacement, one token addition, the ability borrowers over permanent and static card subjects |
| Become, Draw, Double | T6 | triaged, no code changed: 85 rows (`step-9` 63, `blocked-other` 6, `unique` 16); fold batches in T6's Findings, among them a copier subject and copy exceptions on `BecomeCopyOfSubjectEffect`, the draw-then-discard reads of the DISCARD step, the source, recipient and floated damage doublers, the counter doublers onto triage 5's counter-placement batch |
| Set, Copy, Seek (+ 2 stale step-4/5 rows) | T7 | triaged, no code changed: 81 rows (`step-9` 69, `blocked-other` 6, `unique` 6); fold batches in T7's Findings, among them a P/T-from-amount batch on `SetBasePowerToughnessToAmountEffect`, one seek step recording SEEK, the spell-copier and copy-per-permanent batches, a next-time delayed registration, the card-copy-cast batch on `CopyImprintedCardAndMayCastCopyEffect` |

Cross-cutting results of those runs, now conventions or shared pieces (Standing knowledge): the face-down hiding rules; stay checks
(CR 400.7, 603.7c) on every return and delayed return; what a put step moves enters through the entry batch (as-enters and Aura host
choices, a card that can't enter stays where it is, CR 101.2, 303.4f); choices drop a permanent that can't be chosen before counting
(CR 608.2d); name prompts offer only names the chooser can know; every prevention sends the damage-prevented event; protection is
checked at targeting for every target (CR 702.16b); one player-level cast check and the resolution-cast check (runs 40 and 41).

### 10. Collapse the head: one record per verb (repeatable, needs sign-off)

Depends on: 5 and 6 done for the verb.

High churn, low behaviour risk. After steps 5 and 6 the high-use siblings of a verb differ only in
their subject component and constructor shapes: `BoostSelfEffect` (1,079 cards),
`BoostTargetCreatureEffect` (1,356), `BoostAllOwnCreaturesEffect` (418), `BoostAllCreaturesEffect`
(195); `DealDamageToAnyTargetEffect` (740), `DealDamageToTargetCreatureEffect` (569),
`DealDamageToTargetPlayerOrPlaneswalkerEffect` (148); `PutCountersOnSelfEffect` (988),
`PutCounterOnTargetPermanentEffect` (973). One run merges one verb into one record with static
factories that read like the old names, rewrites every card with a script, and deletes the others.

Before each run, state the verb, the card-file count and the factory names, and ask. After each run,
ask for a full card-test run. Skip a verb whose siblings still differ in more than the subject.

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

What earlier runs established and later runs need. Each item names the run that wrote it ("run N" is step 9's run N; "8.N" is
step 8's run N); its full text is in the git history named at the top.

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
  copies read copiable values (run 20).
- An effect that cares about a spell's characteristics judges `CastingPermissionService.spellAsEffectsSeeIt(entry)` (a spell cast
  face down has only its face-down characteristics).
- A face-down permanent's mana value is 0 and it has none of its card's abilities or perpetual changes (CR 708.2a): read the
  permanent, not its card (runs 25, 29, 30, 35). The reads: `GameQueryService.getPermanentManaValue` (a sacrificed permanent's
  snapshot too), `getEffectiveName` (null while face down) / `permanentHasName`, `hasEffectiveCardType`, `activeStaticEffects`
  for a static ability; a spell being cast face down has no subtypes or abilities (`CostModificationContext.castFaceDown`)
  (defect run 1).
- An ability borrower reads a permanent's activated abilities through one method,
  `GainActivatedAbilitiesOfCreaturesOpponentsControlSelfEffectHandler.effectiveActivatedAbilities` (none of the card's while face
  down or after losing them; only its mana abilities under Blood Sun); a graveyard borrower asks
  `graveyardCardsHaveLostAllAbilities` first; a static replacement source asks `hasLostPrintedAbilities`, not only the floating
  `losesAllAbilitiesByEffect` (CR 613.1f) (defect run 2).
- A hand choice is shown to its chooser alone and its log names no card; a prompt over exiled cards masks a face-down card from a
  player who may not look at it; a name prompt offers only the catalog's names and names of cards the chooser can know
  (`PlayerInputService.cardNamesOfferedTo`, checked by `ChoiceHandlerService.isCardNameAllowed`); the AI answers from what it can know
  (runs 12 to 16, 27).

**Rules a fold must keep:**

- A card that left a zone and came back is a new object (CR 400.7): returns, delayed returns and "that card" check the card's stay
  (CR 603.7c). Card subjects do this; a handler with its own lookup must too.
- Permanents one instruction moves leave or enter as one event (CR 603.10a, 608.2e); several players choose in APNAP order, then act.
- A choice with one possible answer is made unasked; a permanent that can't be chosen is neither offered, counted nor drawn at random
  (CR 608.2d).
- What a put step moves enters through the entry batch: as-enters, copy and Aura host choices are asked, and a card that can't enter
  stays where it is (CR 101.2, 303.4f, 303.4g); an instant or sorcery card is never put onto the battlefield face up (CR 400.4a).
- A ward or ransom decline counters through `CounterSupport.counterSpell(gameData, counterControllerId, description, target)`, so
  Guile's replacement, flashback's exile and the countered events apply (8.28, 8.29).
- Protection is checked at targeting for every target of a spell or ability, benign or harmful (CR 702.16b; run 37). A target spec
  stays `harmful` for a harmful verb's `target()`; a non-target subject declares no target.
- Every spell cast is asked through `CastingPermissionService`: `isSpellCastingAllowed` (spell limits, type and name restrictions,
  Ethersworn Canonist, split second; it asks `isPlayerPreventedFromCasting`, the one player-level check) on every casting path, and
  `canCastDuringResolution(gameData, player, card)` / `mayBeginCastDuringResolution` (a land is played, not cast; a split card is
  judged half by half, CR 709.3a) before any cast during a resolution (CR 601.3, 608.2g). Refused, the flow ends as a decline does
  (runs 40, 41). Known gaps: Scrycast, Knowledge Pool's "cast only during", `SplitCardCastSupport.beginFromExile` (open defects below).
- "Until [source] leaves the battlefield" is `UNTIL_SOURCE_LEAVES`, and a control effect with it (or "until you lose control")
  holds while its source is phased out (`DurationClock.holds`; CR 702.26d); a resolution's "for as long as that Aura is attached"
  is `WHILE_REMAINS_ATTACHED` (CR 702.26f) (defect run 2).
- Targets chosen after an opponent's mode choice (Fatal Lore) are recorded on the spell on the stack through
  `MultiPermanentChoiceContext.TargetsAfterOpponentModeChoice`: declared targets, a target filter, the controllers at choice,
  then the "becomes the target" and crime events (CR 601.2c) (defect run 2).
- A prevention prevents in the damage pools so the affected player orders it (CR 616.1), and every prevention sends the
  damage-prevented event (Selfless Squire; runs 22 to 24).
- The log line of the survivor wins.
- A search that finds nothing (an empty library, no match) is still a search (CR 701.23a): it is recorded, once per search however
  many picks it takes (`LibrarySearchSupport.endSearchFindingNothing`; defect run 1).
- A declined `MayEffect` runs no step and records nothing: an "if you do" after it would read an older step of that kind (step 7).
- A new damage, discard or sacrifice primitive calls `recordStep(kind, NONE)` first: central hooks add results to the latest step of
  that kind (step 7).
- AI value readers (`SpellEvaluator`) don't look inside a `ConditionalEffect`; a survivor that keeps a capability interface keeps the
  AI's view.

### Composition patterns (Oracle wording → steps)

- "If you do" → `ConditionalEffect(HappenedThisWay.x(), …)` after the step; "Otherwise" → the negated filter, not a `NotCondition`
  (which would also fire when nothing happened); "if you don't [draw] this way" → `NotCondition(HappenedThisWay.drew())` (8.9, 8.19).
- "That many" / "its power" → `AmountThisWay(kind)`; "its controller" of what a step moved → `playersThisWay(kind)`; "it" / "that
  creature" after a step → `thisWay(kind[, filter])` or `cardsThisWay(kind[, filter])`.
- "[Sacrifice / exile] this unless you [discard / sacrifice] …" → `MayEffect(that step, prompt, penalty)` (CR 118.12a); "you may
  sacrifice N … If you do / Otherwise" → `MayEffect(SequenceEffect.of(SacrificePermanentsEffect(N, …),
  ConditionalEffect(HappenedThisWay.atLeast(SACRIFICE, N), X)), prompt, Y)` (8.20, 8.22).
- "Sacrifice any number … draw that many" → `SacrificePermanentsEffect.anyNumber(filter, you())` + `AmountThisWay(SACRIFICE)` (8.19).
- "Exile target card from a graveyard. If it was a [type] card" → the exile + `HappenedThisWay.toACard(EXILE, filter)` (8.9).
- "[Another player] may play those cards" → `.playableBy(subject)`; "during your next turn" → `.until(DURING_YOUR_NEXT_TURN)` on the
  library-exile permission step (8.12, 8.13).
- "Destroy target X and all other … that share a [quality] with it" → `allOf(target(), otherPermanentsSharing(target(), quality,
  filter))` (8.16).
- "For each permanent destroyed this way, its controller …" → `AmountThisWay(DESTROY)` with `died` / `matching` / `controlledByYou`, or
  the verb over `eachPlayer()` evaluated per player (8.17, 8.18).
- "Note its power … that much" → `NoteAmountEffect` + `AmountThisWay.noted()`; a delayed ability keeps it (run 33).

### Shared pieces (reuse before adding)

Step and choice records:

- `ChooseCardEffect` (`oneOf`, `upTo`, `oneOfEach`, `atRandom`, a chooser) records CHOOSE over cards; `ChoosePermanentsEffect`
  (`chosenBy(player)`, `oneForEach`, `atRandom`) over permanents, read by `thisWay(CHOOSE)`; `ChoosePlayerEffect` /
  `ChoosePlayerAtRandomEffect` (`playersThisWay(CHOOSE)`); `ChooseCardNameAtResolutionEffect` + `CardHasNameChosenThisResolutionPredicate`;
  `ChooseCreatureTypeAtResolutionEffect`; `ChoosePermanentOnEnterEffect` (as-enters).
- Movers over card subjects: `PutCardsOntoBattlefieldEffect` (controller, face down, entering effects), `PutCardsIntoHandEffect`,
  `PutCardsIntoLibraryEffect`, `ReturnChosenPermanentsToHandEffect`, `ExileChosenPermanentsEffect`, `ExileCardFromHandEffect`,
  `ExileCardsWithSameNameEffect` (by the chosen name too), `ExileCardAtRandomEffect`, `ExileCardsFromTopUntilEffect`,
  `RevealCardsFromTopUntilEffect`, `RevealCardsFromHandEffect` (`.atRandom`), `DiscardCardsEffect`, `ExileTargetSpellEffect`,
  `ExileFaceDownPileEffect`, `ExileGraveyardCardsEffect.cards(...)`, `ExileUpToOneCardFromGraveyardEffect` (`.matching`, `.from`),
  `CreateTokenCopyOfCardEffect.of(cards, options)`, `CopyImprintedCardAndMayCastCopyEffect.of(cards)`,
  `ConjureDuplicateOntoBattlefieldEffect` (originals from `SubjectResolutionService.cardsToDuplicate`).
- Players: `EachPlayerSearchesLibraryEffect`, `EachPlayerPaysAnyManaEffect`, `TargetPlayerChoosesOneEffect.villainousChoiceFacedBy`,
  `LookAtHandEffect(PlayerSubject)`, `TargetPlayerLosesAllUnspentManaEffect(PlayerSubject)`.
- Head records with options worth knowing: `GainControlOfTargetEffect.gainedBy(player)` (records GAIN_CONTROL),
  `SacrificePermanentsEffect.anyNumber` / `.upTo`, `DiscardEffect.matching`, `DestroyAllPermanentsEffect.sparing`,
  `BecomeCopyOfSubjectEffect`, `MayCastAnySpellFromHandWithoutPayingManaCostEffect.orElse`, `SetCombatRequirementThisTurnEffect.mustAttack`,
  `MatchingCreaturesMustAttackThisTurnEffect.controlledBy`.
- `NoteAmountEffect` (StepKind NOTE, `AmountThisWay.noted()`); `QueueReflexiveAbilityEffect` for "when you do".

Permissions, restrictions and grants:

- `AllowPlayCardsFromExileEffect` over a `CardSubject` (`thisTurn`, `untilEndOfYourNextTurn`, `whileExiled`, `whileYouControlSource`;
  `playableByOwner()`, `playableOnlyIf`, `withAnyManaType`, `withoutPayingManaCost`, `exilingInsteadOfGraveyard`);
  `ExileTopCardMayPlayThisTurnEffect` (`fromLibraryOf`, `until`, `playableBy`, `choosingOne`, `upTo`, `mayOnlyCast[(filter)]`).
- `PlayerRestrictionEffect(players, PlayerRestriction, duration)` (`thisTurn`, ...; `PlayerRestriction.castingSpells()`,
  `castingNoncreatureSpells()`, ...), stored in `GameData.restrictionsOnPlayer`; `GrantStaticEffectToPlayerEffect` (`untilYourNextTurn`,
  `untilPlaneswalk`, ...); `CantPlayChosenNameUntilNextTurnEffect`.
- `GrantGraveyardAbilityToCardsEffect`, `GrantSupertypeEffect`, `GrantAbilityToNextSpellThisTurnEffect`; the perpetual verbs
  `PerpetuallyGrantAbilitiesEffect`, `PerpetuallyGrantAbilityToTriggeringCardEffect`, `PerpetuallyChangeCastCostEffect`,
  `PerpetuallyBoostCardEffect` over card subjects, with one perpetual ability store, `GameData.perpetualAbilityGrants` (run 28).
- `PreventDamageToRecipientsEffect` (static prevention: `to(...)`, `toYouAnd(...)`, `toEveryone()`, `fromSpells()`,
  `fromInstantAndSorcerySpells()`, `fromSourcesOfChosenColor()`, `dealtBy(filter)`, `preventing(amount)`);
  `PreventNextDamageAndRedirectEffect`; `PreventAllButOneDamageToControllerAndPermanentsEffect`.

Delayed and global triggers:

- `RegisterDelayedStepTriggerEffect`: "at the beginning of the next [step]" with `atNext`, `atNextMainPhase`,
  `atNextMainPhaseThisTurn`, and "about" forms naming what the ability acts on: `aboutSourceAtNext`, `aboutCardAtNext` (a card in its
  stay), `aboutAtNext`, `aboutCreated`, `aboutPutOntoBattlefield`.
- `RegisterDelayedEventTriggerEffect`: "when [event]" with `aboutWhenSourceLeavesThisTurn`, `aboutWhenSourceLeavesOrUntaps`,
  `aboutWhenSourceUntapsOrControlLost`, `whenLeavesThisTurn` (`PERMANENT_LEAVES`).
- `RegisterGlobalTriggeredAbilityEffect` (renamed in run 39) with a `Duration` (`untilYourNextTurn`).
- `TemporaryGlobalTriggeredAbility.keepingNotedAmount` carries the noted amount.

### Blocked causes still open

Counts are ledger rows of existing classes whose latest note names the cause (approximate, by keyword).

| Cause | Rows | What would unblock it |
|---|---|---|
| First half prompts mid-step (`blocked-step-model`) | 149 | moving the handler onto the step model (`leave-battlefield-redesign.md` step 14) |
| Each player chooses, then all act (`blocked-player-loop`) | 83 | a choose-then-act loop (not in this plan) |
| Cost combinators, "counter unless", cost heads | about 50 | step 10 (sign-off): option C and the cost-combinator merge |
| A delayed trigger shape (end-of-combat, coin flip, the old delayed mechanism) | about 20 | the delayed-mechanism unification onto `RegisterDelayed*` |
| Planar deck cards | 7 | a card subject for the planar deck with its reveal, bottom-order and planeswalk steps |
| Next-draw replacement payloads | 9 | a "this draw was replaced" record in `DrawService` |
| Owner's questions (hidden-zone logging, perpetual "it", gift opponent) | about 10, plus Perpetually's 30 and Conjure's hand and library records unaudited | the owner's decisions below |
| A token controller on `CreateTokenEffect` / the token copies | 8 | a `PlayerSubject` token controller with the Esix, investigate and attack paths agreeing |
| Out-of-battlefield characteristics (the all-zones grants) | 5 | a redesign of the out-of-battlefield characteristic queries (outside this plan) |
| One or two classes each | most of the other blocked-other rows | a third user; each note names the part |

About 30 blocked-other rows from steps 4 and 5 still name step 6, 7 or 8 as the blocker ("step-7 picked subject", "a GrantScope until
step 6", "fused (step 7/8)"); those steps are done, so re-triage them. The 7 `step-9` rows: two from 8.29
(`SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect`, `DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect`),
the can't-be-countered trio (settle the face-down and lost-abilities checks of the readers first), and the two may-draws
(`TargetOpponentMayDrawCardEffect`; `DefendingPlayerMayDrawCardEffect` once `MayEffectHandler` reads the recorded defending player,
CR 508.5).

Fold leads with three or more cards (owner-approved where the shared record says the same for every card):
`RegisterDelayedSelfReturnFromGraveyardEffect` (7 cards) and `RegisterDelayedReturnCardFromGraveyardToHandEffect` (9) onto
`aboutCardAtNext`, the counter amount onto the noted amount (runs 32, 33); `LifeTotalCantChangeUntilNextTurnEffect` (Teferi's
Protection, Perch Protection) onto `GrantStaticEffectToPlayerEffect.untilYourNextTurn(new LifeTotalCantChangeEffect())`, its
`GameQueryService` reader not yet compared (run 40); a copier subject on `BecomeCopyOfSubjectEffect` for the Become family's copy
records (run 38).

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

### The owner's open questions

- **"Counter unless"** (8.27, 8.28): the orchestrator took option B in 8.28 (the ransom records stay; the declined ransoms counter
  through `CounterSupport`) and set option C aside for step 10 (one `CounterUnlessEffect`-shaped record replacing every "counter
  unless" record, ward {N} included). The owner hasn't confirmed either; ask before step 10 builds option C.
- **Hidden-zone logging**: what a perpetual change or a conjure to a card in a hand or library logs (one line per card, per zone, or
  none). The perpetual `CardSubject` survivors already log one line per hidden card, telling opponents how many cards of a hand set
  changed (runs 7, 10, 15). Holds Perpetually's hand and library records and Conjure's hand and library conjures. Spellbook drafts into a hand belong to
  it too: `DraftCardIntoHandEffect` logs the drafted card's name to every player (parallel triage chunk 08). Close to the
  masked-prompt question: opponents may learn from a hand put whether a prompt was opened (run 27).
- **The perpetual "it"** (CR 400.7): whether a perpetual change follows a card that changed zones before the grant resolved. Holds
  `PerpetuallyGrantAbilityToTriggeringCardEffect` (the triggering card anywhere; Forgeborn Phoenix), `PerpetuallyBoostTriggeringCardEffect`
  (4 cards), Rothga's and Mutable Pupa's records against `Subjects.triggeringCards()`.
- **The gift opponent** (CR 702.174a, `missing-features-to-do.md` entry 80): no gift opponent is recorded; every gift handler
  (`TargetOpponentCreatesTokenEffect`, `ConjureCardToOpponentBattlefieldEffect`, `EachOtherPlayerDrawsCardEffect`, ...) gives it to
  `getOpponentId`, every other player, or the spell's target, right with two players only; the gifts of Cruelclaw's Heist, Consumed by
  Greed and Perch Protection never raise the gift-given event (CR 702.174c), so Jolly Gerbils misses them.
- **Face-down board views** (`missing-features-to-do.md` entry 81): a player can't tell their own face-down permanents apart on the
  board or in id-only prompts; needs a per-viewer permanent view and client work.

### Open defects seen and not fixed

Collected from every "Seen, not changed" list; items a later run fixed are dropped. "Code" = still present in the code on
2026-11-23; "unverified" = not re-checked.

- `MayCopyHandlerService.handleBecomeCopyChoice` logs "'s copy target is no longer on the battlefield." when the copy succeeds (5m). Code.
- Courageous Resolve: "you can't lose life" doesn't stop paying life as a cost (CR 119.8); `canPayLifeForCosts` takes no player (5m). Code.
- Kappa Cannoneer, Lockjaw, Slicer, Full Steam Ahead, Vines of Vastwood: "can't be blocked / sacrificed / the target" are granted
  abilities that "loses all abilities" removes; they are rules of the game (CR 611.2c) (5m). Unverified.
- The Ozolith removes counters it then can't put on the target (CR 122.5) (5m). Unverified.
- Pariah, the attached preventions and Curse of Bloodletting are read without a lost-abilities check (CR 613.1f) (5m). Unverified.
- `MayEffect`'s `DEFENDING_PLAYER` doesn't read the recorded defending player (CR 508.5; Harbor Guardian's kind); Soldevi Heretic
  derives "target opponent" and targets nobody (5m). Unverified.
- `TargetOpponentCreatesTokenEffect` derives "target opponent" and `OpponentGainsControlOfSourceCreatureEffect` picks the opponent by
  seat: right with two players only (5m). Unverified.
- `GraveyardReturnSupport.exileCardFromAnyGraveyard` doesn't clear the "put into a graveyard from the battlefield this turn" sets
  (CR 400.7), nor (unverified) the own-all sweep of `ExileGraveyardCardsEffect` (run 5). Code.
- `DiscardHandEffectHandler.discardHand` (Wheel of Misfortune, Goblin Game, the may-discard wheel, ...) and the private copies in the
  two "each player discards their hand, then draws" handlers ignore "can't cause you to discard" (Tamiyo, Collector of Tales) (run 6). Code.
- `PutUnplayedExiledCardsIntoOwnersGraveyardEffect` (`LibraryChoiceHandlerService`) and a reflexive ability's carried step result don't
  check the exile stay (CR 400.7) (run 6). Unverified.
- `ExileTargetPermanentEffect`'s target path exiles several targets one at a time (CR 603.10a); the subject path batches (run 6). Unverified.
- Moonring Mirror's and Duplicity's hand exile keeps the implicit look of a source-linked face-down card (CR 406.3); the view's
  implicit look and `mayLookAtExiledCard` are two answers to one question (runs 6, 13). Unverified.
- `ExileTopCardMayPlayThisTurnEffect.withAnyManaType()` skips land cards (a land's Adventure) (run 6). Code.
- Perilous Iteration makes two delayed abilities, one per seek, where its text has one (run 7). Unverified.
- `GameQueryService.isManifestBlockedFromZone` judges the card, not the face-down object (Worms of the Earth; CR 701.58f, 708.2a); the
  pile step doesn't call it (run 7). Unverified.
- Two perpetual keyword stores, `perpetualKeywords` and `perpetualCardKeywords` (runs 7, 29). Code.
- Shape Anew reveals nothing when the artifact can't be sacrificed (CR 609.3) (run 7). Unverified.
- A same-name search with no match asks nothing, so the searcher doesn't get the look CR 701.23a allows; Haunting Echoes with nothing
  exiled likewise (runs 7, 8). Unverified.
- Summon: Valefor compares the cards' mana values, not the permanents' (CR 708.2a) (run 8). Unverified.
- Descent into Madness and Sothera exile the chosen permanents one at a time (CR 603.10a; Sothera before the next opponent chooses,
  CR 101.4) (runs 8, 9). Unverified.
- Conspiracy Theorist's discard trigger has no nonland filter (Oracle: "one or more nonland cards"); only the choice filters (run 8). Code.
- The Ruinous Powers' "mana of any color" is the any-type permission, which also pays {C} (run 8). Unverified.
- The Horus Heresy's prompt says "a creature you control" over every creature (run 8). Unverified.
- The queue's may-searches (New Frontiers, Natural Balance, ...) have no separate "do you search?", so a player can't search and find
  nothing (CR 701.23a, 701.23b) (run 9). Unverified.
- Sunimret's reverse-miracle offer reads the bottom card also when only the top four may be searched (run 9). Unverified.
- Opposition Agent's player can take a cast-only card (Panglacial Wurm) as the find; the AI's search strategy answers only for the
  searcher (run 9). Unverified.
- A cast that empties a multi-card search's offer drops what that search accumulated (`continueLibrarySearchAfterLibraryAction`) (run 9). Unverified.
- Ol Rin's Searing Light and Gilded Ambusher exile each opponent's choice before the next chooses; Dispersal returns and discards one
  opponent at a time; The Second Doctor and Kwain draw before the next player chooses (CR 101.4, 608.2e) (run 9). Unverified.
- Winds of Abandon's player searches once for that many, where its text has one search per creature (run 9). Unverified.
- The two-player win declaration of "loses the game" doesn't make a draw of simultaneous losses (CR 104.4a) (run 9). Unverified.
- Infectious Rage returns the Aura under its owner's control; the instructed player is the ability's controller (CR 110.2a) (runs 10, 11). Code.
- Veko adds Spirit where "becomes a Spirit" replaces the creature types (CR 205.1a) (run 10). Unverified.
- Kairi's bound reads a face-down permanent's card mana value (CR 708.2a); Kairi, Leadership Vacuum and Resounding Wave's cycling return
  one permanent at a time (CR 603.10a); Resounding Wave's cycling trigger chooses as it resolves where its text targets (CR 603.3d)
  (run 10). Unverified.
- Unfinished Business puts its two attachments onto the battlefield one after the other (run 10). Unverified.
- Rambling Possum offers only saddlers its controller still controls (run 10). Unverified.
- A face-down entry's triggers are suppressed by the card's printed types (CR 708.2a) (run 10). Unverified.
- The chosen returns to a hand don't leave out a permanent that can't be chosen (`CantBeChosenEffect`) (run 10). Unverified.
- `cardsThisWay(kind, filter)`, the readers that copy, offer to cast or read a named card's name or mana value, and the entry batch's
  "can't enter" filter read a face-down exiled card's hidden face (CR 406.3a) (run 10). Unverified.
- An entry from face-down exile that asks its controller a choice (an Aura's host) names the card in that prompt (CR 406.3) (run 10). Unverified.
- Puts outside the guarded paths don't ask CR 400.4a (`ReturnVoyagingCardFromExileEffect`, the exiled-permanent choice handlers, Meld
  weren't read) (run 11). Unverified.
- Lim-Dûl's returned bestow Aura and the triggering-card heads (but `ReturnTriggeringCardFromGraveyardToBattlefieldEffect`) enter without
  a host choice (CR 303.4f) (run 11). Unverified.
- `PutExiledCardOntoBattlefieldUnderControllerEffect` checks no stay (CR 400.7) (run 11). Unverified.
- The opponent-owned processor head and Ulamog's Nullifier offer face-down cards one by one where Ruin Processor chooses piles (run 12). Unverified.
- `PutCardToBattlefieldThenEffect` judges its predicate without X or the source's last-known state when its entry has no snapshot
  (run 12). Unverified.
- An Aura attached elsewhere before the state-based check and put into the graveyard later is found by the first host's ability:
  CR 400.7f's second sentence read by its words; confirm the reading (run 12). Unverified.
- `cardsThisWay(REVEAL)` names a card a later step reordered within its library (CR 701.20d); no card reads it so (run 13). Unverified.
- A draw replaced by exactly one draw of the same player (Enduring Renewal, Aladdin's Lamp, ...) still reveals: `DrawService` reports
  no replacement (run 14). Unverified.
- Frontend: the client ignores the `MULTI_PERMANENT_PICK` minimum (code: only the graveyard pick reads `minCount`) and shows graveyards
  for an index pick over a pool (unverified); `GameSimulator` answers a multi-pick with the first maximum (runs 14, 15).
- A name prompt that requires a type offers a copy's name by the copy's types (CR 201.4a) (run 15). Unverified.
- Scour the Scene's choice offers battlefield creatures by card id; the other perpetual hand choices' prompts weren't audited (runs 16, 17). Unverified.
- Token copies of face-down permanents, other consumers of a face-down object's real characteristics and opaque card ids: not looked
  at (runs 16, 17). Unverified.
- The whole-card type and colour prohibitions of a split card weren't traced (run 17). Unverified.
- Zara attacks what the attackers' defender was: no choice between that player and a planeswalker of theirs (CR 508.4) (run 17). Unverified.
- Pestilent Spirit's deathtouch also goes to abilities of instant and sorcery cards (a cycling trigger's damage): needs a "source is a
  spell" fact on the stack entry (runs 18, 19). Unverified.
- Several kept Grant handlers name a floating effect's source by `entry.getCard().getName()` (CR 708.2) (run 18). Unverified.
- `beginAnyTargetChoice` and the permanent-or-player prompt don't drop what can't be chosen; about 400 permanent-prompt callers that
  count first weren't swept (runs 18, 19). Unverified.
- A colour-word change doesn't rewrite the floating targeting rule of Autumn's Veil (`LAYER_SYSTEM.md`'s known gap) (run 18). Unverified.
- `CreateTokensAttachedToControlledCreaturesEffect` pairs tokens by `getTokenMultiplier`, and a creation that pauses for an order prompt
  returns no ids, so nothing is attached (not reproduced; Halving Season over distinct Roles is a rules question) (runs 19, 20). Unverified.
- Esix's copies keep none of the replaced copy's instructions (tracking, haste, end-step exile: Faerie Artisans); Dance of Many waits
  for it (runs 20, 21). Unverified.
- `VengefulArchonTest` has four test methods without `@DisplayName` (run 21). Code.
- `DamagePreventionService.applyPlayerPreventionShield` (and its combat entry points) and `applyPerSourceCreatureDamagePreventionShield`
  have no main caller (runs 22, 23). Code.
- A split spell on the stack has only the cast half's name (CR 709.3b), but the chosen-name predicate reads both names for a spell
  source (run 23). Unverified.
- The ~25 handler pre-checks of `isPreventedFromDealingDamage` (fight, "each creature deals damage", Defang, floating "can't deal
  damage") skip the damage event, so Selfless Squire misses them and their order is fixed (runs 24, 37). Unverified.
- Twinning Glass offers neither an Adventure nor a modal back face sharing the name, and checks additional costs only on accept
  (run 26). Unverified.
- Gitrog's enter trigger records the entering permanent's `getCard()` id, so a land copy gains the ability on the copied card's id
  (run 28). Unverified.
- The `Supplier`-built conjures and the spellbook drafts create cards with no printing identity; the hand paths don't mark a conjured
  card conjured (runs 29 to 31). Unverified.
- `PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect` mutates the selected card and freezes it in the handler (run 29). Unverified.
- `ConjureCardToBattlefieldEffectHandler` gives a conjured land no land-enter handling (run 30). Unverified.
- Mythweaver Poq's simultaneous duplicates are one conjure event each; whether "for each of them, conjure" is one event isn't settled
  (run 31). Unverified.
- Chitinous Crawler's (and the Skeleton duplicate's) log: "A duplicate of X is conjured into your graveyard." (second person in a shared
  log) (run 31). Code.
- Vivien's Stampede grants its keywords through `otherCreaturesYouControl()` on a sorcery (no source, so every creature you control)
  (run 31). Unverified.
- Laquatus's Creativity with an empty hand runs a draw and a discard of 0; what they log wasn't checked (run 33). Unverified.
- `GainControlOfTargetEffectHandler.resolvePermanent` reads all of the entry's targets, not the effect's group (runs 35, 38). Unverified.
- A multi-group permanent spell's enter-trigger targets can be announced at cast: Ravener with no targetable opponent is refused
  (CR 603.3d); the trigger-time prompt asks for the creature before finding no opponent (run 37). Unverified.
- Ravenous Pursuit's recipient is declared as "creature an opponent controls" for "creature you don't control" (run 37). Unverified.
- Graceful Takedown: the same creature can be chosen for both dealer groups (CR 115.3) (run 38). Unverified.
- The bite's damage from several dealers is dealt one dealer at a time, not as one event (runs 37, 38). Unverified.
- Suspend asks the player check and name prohibitions only, not a type restriction (Cease-Fire on a creature with suspend; CR 601.3
  suggests it should, no ruling settles it) (run 40). Unverified.
- Scrycast (Biting Remark): `ScryInteractionHandler` casts it without the shared check; refusing it needs the projection, the AI
  (`ScryAiStrategy`) and the answer's validation together (run 41). Code.
- Knowledge Pool doesn't check the chosen card's own "cast only during …" restriction (its ruling) (run 41). Unverified.
- `SplitCardCastSupport.beginFromExile` asks `isSpellCastingAllowed` on the whole split card before judging the halves (CR 709.3a)
  (run 41). Code.
- An activated or triggered ability is removed from the stack before it resolves (`StackResolutionService`), where CR 608.2n removes
  it last (run 41). Unverified.
- The may-cast offers don't ask the shared cast check, so a prohibited cast is offered and then refused (run 41). Unverified.
- `TextChangeTransformer` has no `SequenceEffect` case, so a split trigger with a colour or land-type word isn't rewritten (8.17). Code.
- Origin of Thor: the saga chapter's target prompt doesn't consult protection (5h). Unverified.
- Soul Tithe's {X} reads the current host's mana value; "its mana value" is arguably the trigger-time permanent's (5j). Unverified.
- `DealDamageTo{AnyTarget,Players,TargetCreature}Effect` lose a player hit from the step result when a replacement-order prompt reruns
  them (step 7). Unverified.
- `DiscardCardThenEffect`'s reflexive cards inside a `MayEffect` are asked with no matching card (8.22). Unverified.
- Soundwave's "from their graveyard" is the damaged player's; the opponent-graveyard scope admits any opponent's (8.24). Unverified.
- Giant Albatross destroys each creature right after its controller answers, before the next controller chooses (CR 101.4, 608.2e);
  needs the unpaid creatures collected across the pay-life prompts (GameData state copied for simulations) and one
  `DestructionSupport.destroyBatchWithOutcome` when the queue empties, its death-replacement asks moved from each decline to the
  batch (triage 3, defect runs 1 and 2). Code.
- Last Light of Durin's Day with no Dragon in hand or library searches nothing, where the player may search and find nothing
  (CR 701.23b); its hand-and-library prompt also takes "choose nothing" as "searched the library" only when the library had a
  match. Needs a separate "do you search your library?" answer, kin of the queue's may-searches above (triage 2, defect runs 1
  and 2). Code.
- The pay-{X} handlers treat X = 0 as declining (Relentless Dead, Squealing Devil, Vigil for the Lost, Azor, Roalesk, Hollow
  Specter, Depala) while Isareth and Fire Lord Sozin queue at 0; paying {0} is paying (CR 118.12) (triage 1). Code, low impact.
- Rakdos Charm's third mode deals each creature's damage one at a time (kin of the bite's defect): needs the multi-dealer damage
  held in the open simultaneous damage event (`DamageSupport.beginDamageEvent`) (triage 1, defect run 2). Code.
- Moxite Refinery's `CounterType.ANY` cost removes the chosen permanent's counter kinds in enum order; the player should choose
  which counters (the controlled-counter kind prompt exists only for a single counter, `ControlledCounterCostTypeChoice`)
  (triage 4, defect run 2). Code.
- Make Your Own Luck puts all three looked-at cards into hand, then plots the chosen one from hand; the text exiles it from among
  them and puts only the rest into hand. Needs a library pick that exiles and plots (`LibrarySearchDestination` has no plot
  destination) (triage 4, defect run 2). Code, low impact.
- Nicol Bolas, Dragon-God borrows printed loyalty abilities only and asks only the floating "loses all abilities"; no planeswalker
  losing its abilities to a static effect could be set up without a layer-dependency case (Humility on an animated Gideon
  Blackblade) (triage 5, defect run 2). Code.
- `TokenCreationReplacementSupport`'s static counts (Bilbo's Treasure count, the Academy Manufactor and Map counts, the subtype
  replacements, `staticEffectCount`, `countActiveStaticEffects`) still ask only the floating "loses all abilities"; a static
  utility with no query service (triage 5, defect run 2). Code.
- Aven Riftwatcher, Calciderm, Chronozoa and Regenerations Restored carry both the Scryfall Vanishing keyword and their own
  "last time counter removed" trigger, so a removal that calls `PermanentCounterSupport.checkLastTimeCounterRemoved` (Aether
  Snap, Thief of Blood) may put two sacrifice triggers on the stack (defect run 2). Unverified.
- Charmed Pendant offers a monocoloured hybrid symbol's generic half; Animation Module offers only poison and energy player
  counters; Thran Portal's added life cost applies to its non-mana activated abilities too; Tevesh Szat's -10 puts commanders
  outside the entry batch (triage 5). Code, low impact.
- `ErietteTheBeguilerTest`'s older test methods have no `@DisplayName` (defect run 2). Code.
- `SearchTargetLibraryEffectHandler` matches its filter without game state (`matchesCardPredicate(card, filter, null)`); no card
  found whose filter needs it (triage 2). Code.
- Die Young declares a benign target for its -X/-X; only the AI reads the polarity (protection is checked for every target,
  CR 702.16b) (triage 1). Code, AI only.
- Enslave's owner is taken from `stolenCreatures`, else the controller, not from the card's owner (triage 1). Unverified.
- A permanent spell's behold (the permanent-spell branch of `SpellCastingService.playCardInternalImpl`) records no beheld permanent
  or card on its entry; only instants and sorceries do (defect run 1). Unverified whether a permanent card reads it.
- When Aven Mindcensor leaves no match among the top cards, `sendLibrarySearchToPlayer` begins no prompt but its caller
  (`startNextInstantManaValueToHandPick`, Firemind's Foresight) reports a begun pick, so the later picks may be dropped (defect
  run 1). Unverified.
- `TheDiningCarTest`'s four older test methods have no `@DisplayName` (defect run 1). Code.
- Dead code: `MultiGraveyardChoice.reorderToLibraryTop` and its handler branch (run 12; the component is still there),
  `ExileFromHandChoice.untapPermanentId` (run 7), the multi-zone exile prompt's draw, token and set-code fields (run 16),
  `GraveyardReturnSupport`'s Javadoc blocks with no method (8.25). Unverified.

### Step-10 hand-offs

- **"Counter unless"** (option C, 8.27 to 8.29): one `CounterUnlessEffect`-shaped record (ransom step + counter) replacing
  `CounterUnlessPaysEffect` (ward {N}, 308 cards, its X / dynamic / rider forms) and the 37 ransom records at once, the AI and ward
  detection reading the ransom from it; take `DestroyTargetPermanentUnlessControllerTakesDamageEffect` into that design.
- **Cost combinators beside `MayEffect`** (CR 118.12: their "if you do" is the choice to pay): `ForcedCostOrElseEffect` (158 cards; its
  fallback runs through a per-class list in `DestructionSupport.resolveForcedCostElseEffects` on a carrier entry, so a fallback not on
  the list, or one reading a step result, silently does nothing: Koya), `MayPayManaEffect`, `MayPayLifeEffect`, `MayPayTapPermanentsEffect`,
  `PayPerCounterOrElseEffect`, `DestroyUnlessPaysPerCounterEffect`, May's 11 (owner's decisions), `DrawCardUnlessPaysEffect` and its kin
  (8.29), `TargetPermanentControllerMaySacrificeOrDamageEffect` (Star Athlete, Enchanter's Bane; run 38), `TargetPlayerMayPayManaOrLifeEffect`
  (Emberwilde Djinn; run 41), the cost pairs `Sacrifice*Cost`, `Exile*Cost`, `Return*ToHandCost`, `PutCounterOn*Cost` (5m).
- **Static restrictions**: `CantAttackUnlessEffect` (66), `CantBlockUnlessEffect` (28), `CantAttackOrBlockUnlessEffect` (25) and the three
  `…CantAttack…UnlessPredicateEffect` records; can't-activate, unblockable, goad and must-block pairs (5m).
- **Boost**: `BoostSelfEffect`, `BoostTargetCreatureEffect` (now with a subject), `BoostAllOwnCreaturesEffect`, `BoostAllCreaturesEffect`;
  `BoostSelfEffect` reads amounts against the boosted permanent, `BoostTargetCreatureEffect` against the source, so the merged record
  needs that as a property of the subject or an explicit choice (5i). `BoostTargetPermanentEffect` (harmful, Vehicles) (5i).
- **Counters**: `PutCountersOnSelfEffect` (988 cards) with `PutCountersOnSourceEffect` (370, its own P/T-modifier placement) (5e);
  `RemoveCounterFromTargetPermanentEffect` with `RemoveCounterFromSourceEffect` (5f).
- **Damage**: `DealDamageToTargetPermanentEffect` with the creature-or-planeswalker damage records (5g).
- **Destroy**: `DestroyEachTargetPermanentEffect` and `DestroyAllPermanentsEffect` (5a).
- **Tokens and conjure**: `CreateTokenCopyOfSourceEffect` (5l); the two multi-card battlefield conjure records kept apart in run 30
  (`ConjureCardToBattlefieldEffect`, 25 cards; `ConjureCardNamedOntoBattlefieldEffect`, 9).
- **Register**: `RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect` (Season of the Bold, Nuka-Nuke Launcher): its watched player is a
  permanent target's controller or the defending player, which the survivor can't take (runs 33, 38).
- **Other pairs** (5m, runs 13 and 18): subtype grant, choose-one-for-target, perpetual boost, the returns from a graveyard; the reveal heads
  of three or more cards (17); the graveyard-ability grant with the scavenge record; `GrantSupertypeEffect` with its 4-card enchanted twin;
  a copy verb with a copier subject (Mirrorform, run 8).
- **Leftover to drop in a collapse**: `ExileGraveyardCardsEffect.eventValueFilter` (no card left, run 5).

## Findings

Each run appends its section below the last one. Older sections are in git history (see the top).

### Step 9, run 41 (2026-11-23, Target closed)

Run 40's resolution casts first, then its two small defects, then Target's four rare records. Ratchet 4,048 / 3,302 → 4,046 / 3,300
records / handlers (2 records and 2 handlers deleted; nothing added). 3 card files rewritten (Oko, Thief of Crowns; Vronos, Masked
Inquisitor; Pact of the Serpent). Oracle text checked with `get_card` for those three, Hunted by The Family (its handler changed) and The
Brothers' War; rulings with `get_rulings` for Grand Abolisher, Rule of Law, Ethersworn Canonist, Teferi, Time Raveler, Knowledge Pool and
The Brothers' War. CR 101.2, 205.1b, 400.7, 601.2, 601.3, 608.2g, 608.2h, 608.2n, 611.2c, 702.35a, 702.61a, 702.61b, 702.94a, 702.174a and
709.3a checked with `get_rule` (2026-11-23).

| Family | Audited | Absorbed | Split | Survivor | Blocked (cause) | Unique | Left unaudited |
|---|---|---|---|---|---|---|---|
| Target | 4 rare records | 1 | 1 | 0 | 1 (a choice between two costs, a step-10 cost combinator) | 1 | 0 rare, 8 in all (3 to 8 cards each) |

All families: 1,767 rare records without a row (run 40: 1,771). Perpetually 30 rare and Conjure 39 rare, unchanged. Rare records touched
this run: Target 4 (Oko and Vronos's subtype record, Emberwilde Djinn, Rakdos Augermage, Pact of the Serpent); 4 in all.

**The casts during a resolution.** A cast during a resolution follows the casting steps (CR 608.2g), which a player may begin only when
nothing prohibits it (CR 601.2, 601.3), so every "can't" applies to it, not just the player locks. `CastingPermissionService` gained
`canCastDuringResolution(gameData, player, card)` (every check of `isSpellCastingAllowed`: spell limits, type and name restrictions,
Ethersworn Canonist, split second (CR 702.61a), plus the player and timing locks of the player overload: Silence, Grand Abolisher, Teferi,
Time Raveler) and `mayBeginCastDuringResolution` for a card offered as a whole (a land is played, not cast; a split card is judged half by
half, CR 709.3a). Each flow asks it before anything moves and, refused, ends the way it ends when the player declines (the card stays where
the Oracle text leaves an uncast card). The flows, one by one, each with a failing-first test:

| Flow | Where | Refused, the card | Test (restriction) |
|---|---|---|---|
| cast the library top for free (Galvanoth) | `handleCastFromLibraryChoice` | stays on top | `GalvanothTest` (Teferi) |
| play the revealed top card (Djinn of Wishes, Leaf-Crowned Elder, Ziatora's Envoy, Quicksilver Sea, Descendants' Path) | `cannotPlayFromLibrary` | goes where a declined card goes | `DjinnOfWishesTest` (Teferi) |
| the revealed and ripple casts (Talent of the Telepath, ripple) | `hasLegalFreeCastTarget` | goes where an uncast card goes | `TalentOfTheTelepathTest` (Rule of Law) |
| the instant-or-sorcery graveyard casts (Dreadhorde Arcanist, Thunderblade Charge, Chandra's) | `handleCastFromGraveyardChoice` | stays in the graveyard | `DreadhordeArcanistTest` (Teferi) |
| `CastCardFromGraveyardEffect` (Spectral Arcanist, Diviner of Mist, Sword of Once and Future …) | `handleCastCardFromGraveyardChoice` | stays in the graveyard | `SpectralArcanistTest` (Rule of Law) |
| Conduit of Worlds | `handleCastCardFromGraveyardIfNoSpellThisTurnChoice` | stays in the graveyard | `ConduitOfWorldsTest` (Teferi) |
| play from a graveyard (Horde of Notions, Vadrok, Victor, opponent graveyards) | `handlePlayFromGraveyardChoice` | stays in the graveyard | `HordeOfNotionsTest` (Teferi) |
| exile free casts (Hideaway, cascade-style, suspend; `ExileFreeCastSupport`, which asked only the player locks) | `castSelectedFace` | stays exiled | `HowltoothHollowTest` (Rule of Law) |
| miracle (asked only the spell limit) | `handleMayCastForMiracleCost` | stays in hand | `BonfireOfTheDamnedTest` (Teferi) |
| madness | `handleMayCastForMadnessCost` | goes to the graveyard (CR 702.35a) | `ArrogantWurmTest` (Grand Abolisher) |
| from outside the game for free (Spawnsire, the sideboard casts; asked only the spell limit) | `handleCastFromOutsideGameChoice` | stays outside the game | `SpawnsireOfUlamogTest` (Teferi) |
| an artifact for life (Anrakyr) | its accept handler | stays; no life paid | `AnrakyrTheTravellerTest` (Teferi) |
| the hand free casts and the suspend-cost cast (asked `isSpellCastingAllowed` only) | `handleMayCastFromHandWithoutPaying`, `handleMayCastSpellWithSuspendCost` | stays in hand ("if you don't" queued) | `BaralsExpertiseTest`, `TheFaceOfBoeTest` (Teferi) |
| the commander for free (Geode Golem) | its may handler | stays in the command zone | `GeodeGolemTest` (Teferi) |
| Word of Command's chosen card | `handleWordOfCommandPlay` | stays in hand | `WordOfCommandTest` (Teferi) |
| Jadzi's top card for {1} | `RevealTopCardMayCastForManaOrPutLandOntoBattlefieldEffectHandler` (before the pay offer) | stays on top | `JadziTest` (Rule of Law) |
| Knowledge Pool | `ExileSupport.handleKnowledgePoolCastChoice` | stays exiled with it | `KnowledgePoolTest` (Rule of Law) |
| Omen Machine (asked `isSpellCastingAllowed` only) | its handler | stays exiled | `OmenMachineTest` (Teferi) |
| Wild Evocation, Sunbird's Invocation and the search casts (`canCastWithoutPaying`, `isSpellCastingAllowed` only) | `LibraryChoiceHandlerService` | as each effect leaves an uncast card | `WildEvocationTest` (Teferi) |
| the exile casts of the queue, Chandra, Torch of Defiance, Vaan, the copy casts (`castableFromExile`, `isSpellCastingAllowed` only) | `ExileFreeCastQueueSupport` | as each effect leaves it | `ChandraTorchOfDefianceTest` (Teferi) |
| Paradigm (asked only the spell limit) | `ParadigmCastSupport` | stays exiled | `DecorumDissertationTest` (Teferi) |
| a defeated Siege's transformed cast | `BattleDefeatSupport.castExiledBattleTransformed` | stays exiled | `InvasionOfAmonkhetTest` (Teferi) |
| from outside the game for its normal cost (Your Wish Is My Command) | `OutsideGameNormalCostCastSupport` | stays outside the game | `YourWishIsMyCommandTest` (Teferi) |
| every split card these flows offer | `SplitCardCastSupport.isCastableMode`, per half | no half offered; the flow's "no half" ending | `GalvanothTest` (Rule of Law) |

`PermanentChoiceSpellHandlerService`'s target continuations and `GraveyardChoiceHandlerService`'s graveyard-target continuation are reached
only from a flow that has asked by then (nothing changes while a target is chosen), so they need no check of their own.
`SpellCastingService.playCardFromExileInternal` asks the card overload for a resolution cast too; its callers already ask
`castableFromExile`, so it changes nothing observable. Not routed: Scrycast (Biting Remark), see below. A `casting-stack.md` entry says so.

Failing first: the 25 new flow tests were run with both new methods stubbed to `true` (each test uses a restriction the flow's old partial
check didn't see, so the stub reproduces the old behaviour for it): 24 classes, 303 cases, 29 failures (the 25 new tests at their "not
cast" assertion, plus four older tests of the partial checks the stub also removed). With the methods: 24 classes, 303 cases, 0 failures
after three test-only fixes (a planeswalker added with no loyalty died to state-based actions; two typed prompt assertions; Geode Golem's
commander tax entry is 0, not absent).

**Run 40's two small defects.**

- The Brothers' War chapter II, a defect: "each creature they control attacks the other chosen player each combat if able" changes no
  characteristic and no control, so it modifies the rules of the game and binds creatures the chosen players control later (CR 611.2c);
  the handler put a per-creature effect on each creature present at resolution. It now floats one `MatchingCreaturesMustAttackEffect`
  (controlled by that player, the other player required) per chosen player until your next turn, as Taunt does:
  `TheBrothersWarTest.chapterIIBindsCreaturesThatArriveLater` (failed with the old handler, put back in the file for that one Gradle run
  from a scratch copy, then replaced).
- Celestine Reef, not a defect: `CantLoseGameEffect()` defaults `opponentsCantWin` to true, and `GameQueryService.opponentPreventsWinningGame`
  reads it from the floating effect `GrantStaticEffectToPlayerEffect.untilPlaneswalk` puts on the player;
  `CelestineReefTest.chaosPreventsOpponentWinUntilPlaneswalk` covers it (passed).

**Target's last rare records, and why each fold makes sense or was left.**

- **`TargetPermanentBecomesSubtypeEffect` → `GrantSubtypeEffect(subtype, Subjects.target(), true)`** (Oko's +1, Vronos's −7, Hunted by The
  Family's engine call). The old handler floated exactly that record on the target, `PERMANENT`, overriding the creature types (CR 205.1b:
  "becomes a 9/9 Construct artifact creature" keeps the other types and replaces creature types); the survivor's handler resolves the same
  target through the resolver and stores the same floating effect, with the same benign permanent spec. No AI reader. The old "X becomes an
  Elk." log line is gone (no test asserted it; it read "an Construct").
- **`TargetPlayerDrawsAndLosesLifePerChosenTypeEffect` (Pact of the Serpent) split** into `ChooseCreatureTypeAtResolutionEffect`,
  `NoteAmountEffect(PermanentCount(creature of the type chosen this resolution, TARGET_PLAYER))`, `DrawCardEffect(noted,
  targetPlayerDeclaredElsewhere())` and `LoseLifeEffect(noted, targetPlayer())` (harmful player target, as before). X is counted once,
  before the draw (CR 608.2h), and the life loss is a step after the draw, so Alms Collector and Underrealm Lich behave as before
  (`PactOfTheSerpentTest`'s ten cases unchanged, pass).
- **Blocked-other: `TargetPlayerMayPayManaOrLifeEffect`** (Emberwilde Djinn): "that player may pay {R}{R} or 2 life" is a choice between
  two costs paid by the triggering player, one of the cost combinators beside `MayEffect` step 10 folds (sign-off needed).
- **Unique: `TargetPlayerChoosesCardFromControllerHandToDiscardEffect`** (Rakdos Augermage): the target opponent picks the controller's
  discard from the controller's revealed hand; `ChooseCardEffect` would need a chooser choosing against the cards' owner (its prompt and
  AI answer serve the chooser), a parameter only this card would set.

**Behaviour changes.**

1. Every flow in the table: a cast during a resolution is refused under any "can't" that applies (CR 601.3, 608.2g); before, most flows
   asked nothing, some only the spell limit, the player locks or `isSpellCastingAllowed` without Teferi's lock. 25 new tests.
2. A split card offered by these flows offers only the halves that may be cast now (CR 709.3a); the hand free cast judges a split card's
   halves instead of the whole card.
3. The Brothers' War chapter II binds creatures that arrive later (CR 611.2c). 1 new test.
4. Log lines: the refusal lines are new ("… can't be cast right now …"); the miracle, Paradigm and outside-game spell-limit lines are
   replaced by them; Oko's, Vronos's and Hunted by The Family's "becomes an [subtype]." line is gone. No test asserted any.

**What a player sees.** The same prompts: an offer is still made, and accepting a prohibited cast is refused with a public log line naming
the card the offer named. No prompt or log names a hidden card.

**Seen, not changed.**

- Scrycast (Biting Remark): the scry prompt offers the Scrycast card and `ScryInteractionHandler` casts it without the shared check; refusing
  it needs the projection, the AI (`ScryAiStrategy` always picks it) and the answer's validation together. Not routed.
- Knowledge Pool doesn't check the chosen card's own "cast only during …" restriction (its ruling: "Other restrictions, such as Spinal
  Embrace's … are not ignored").
- `SplitCardCastSupport.beginFromExile` still asks `isSpellCastingAllowed` on the whole split card before judging the halves (CR 709.3a).
- An activated or triggered ability is removed from the stack before it resolves (`StackResolutionService`), while CR 608.2n removes it as
  the final part of its resolution; a "sorcery timing" read during an ability's resolution sees an empty stack (read, not tested).
- The offers themselves (the may prompts) don't ask the shared check, so a prohibited cast is offered and then refused.

**Tests edited.** `OmenMachineDrawStepEffectHandlerTest` stubs `canCastDuringResolution(…, card)` (the method its handler now asks) instead
of `isSpellCastingAllowed`; `MayCastHandlerServiceTest` stubs `mayBeginCastDuringResolution` to true in its set-up (its mocked permission
service answered false, which refused three casts); `EffectCountRatchetTest`'s baselines. New cases in the 24 flow test classes and
`TheBrothersWarTest`. No assertion was loosened or flipped.

**Tests run.** Counted from the XML by file name and timestamp. The flow run: 580 card test classes (every test class of a card using the
cast effects of the touched flows, 302 from `--tests-for` over 85 effect classes, plus every class naming miracle, madness, suspend, cascade,
Knowledge Pool, Scrycast, Paradigm, Hideaway, Teferi, Time Raveler, Rule of Law, Grand Abolisher or a split card) and
`CastingPermissionServiceTest`, `LibraryChoiceHandlerServiceTest`, `MayCastHandlerServiceTest`, `SpellCastingServiceTest`,
`OmenMachineDrawStepEffectHandlerTest`, `EffectCountRatchetTest`: 611 XML files, 5,624 cases, 3 failures (`MayCastHandlerServiceTest`'s mock,
above; rerun: 14 cases, 0 failures). The fold run: the 10 classes referencing Oko, Vronos, Pact of the Serpent, Hunted by The Family, The
Brothers' War and Celestine Reef, `CardEffectTargetingConsistencyTest`, `EffectCountRatchetTest` at 4,046 / 3,300 and the AI's
`TargetPolarityGuardTest`: 13 XML files, 91 cases, 0 failures; then `JadziTest`, `JadziStewardOfFateOraclesGiftTest` and
`CastingPermissionServiceTest` on the final sources: 8 XML files, 67 cases, 0 failures; `ArrogantWurmTest` after the madness refusal's
own log line: 8 cases, 0 failures. No deleted handler was `AsksDeathReplacementsFirst`
or called `LifeSupport.askBefore*`. Compiled: domain, engine (and its test fixtures), AI, the application test sources, card modules o, p,
v, shared and the card-test modules a, b, c, d, g, h, i, j, k, o, p, s, t, v, w, y and those the runs reached. No deleted type is named by
any source (grepped). Not run: the full suites, the AI fuzz and stress tests, the frontend. Not verified: more than two players.

**For the next run (run 42: Destroy's rare records).**

- Target is closed; the next family in the plan's order with rare records and no owner's question is Destroy (37 rare).
- The resolution-cast leftovers above: Scrycast's prompt, AI and answer; Knowledge Pool's "cast only during" check; `beginFromExile`'s
  whole-card check.
- Still the owner's: the gift opponent (CR 702.174a, missing-features entry 80), the hidden-zone conjure and perpetual logging, the perpetual
  "it" (CR 400.7).

### Step 9, triage run 1 (2026-11-23)

Verdicts only: no Java, test or agent-docs file changed; the ratchet did not move (4,046 / 3,300). 112 ledger rows appended (step 9,
notes `triage 1: …`): the 32 stale step-4/5 `blocked-other` rows, then the whole of Pay (40 rare) and Reduce (40 rare). Every record and
`normalfx` handler was read (log lines skipped), with the card classes that use it; of the static cost reducers, the `costmod` handlers of the
chosen-subtype, graveyard, legacy-word, commander, imprint, first-matching, dash and buyback records and the matching-spells survivor were
read, the interface-read ones through their records' methods, and the other `costmod` handlers were not read (a fold run reads them). Oracle
text checked with `get_card` for Fateful Tempest, The Serpent Society, Arthur, Marigold Knight, Super State, Hawkeye's Bow, Ydwen Efreet,
Ripples of Potential, Angel's Trumpet, Unexpected Request, Yotia Declares War, Behemoth of Vault 0, Localized Destruction, Vault 112:
Sadistic Simulation, Tolarian Kraken, Momo, Friendly Flier, Eluge, the Shoreless Sea, Kaza, Roil Chaser, Strong Back, Myth Unbound and Diary
of Dreams; the other cards were judged from their card classes and the records' Javadoc, not checked with `get_card`. Rulings with `get_rulings` for Yotia Declares War (its
handler queues the reflexive ability with zero tapped: the ruling agrees). CR 118.12, 118.3, 107.14, 119.4, 608.2h, 613.1f, 702.37c and
708.2a checked with `get_rule` (2026-11-23).

| Family | Triaged | `step-9` | `blocked-other` | `unique` | Left unaudited |
|---|---|---|---|---|---|
| stale step-4/5 `blocked-other` rows | 32 | 28 | 4 | 0 | n/a (re-triaged rows) |
| Pay | 40 rare | 30 (1 of them `dead: delete`) | 3 | 7 | 0 rare, 11 in all |
| Reduce | 40 rare | 30 | 0 | 10 | 0 rare, 16 in all |

All families: 1,687 rare records without a row (before: 1,767). `step-9` rows waiting for fold runs: 95 (7 older + 88).

**The stale rows.** Steps 6 to 8 are done, so the 32 rows whose note named them got new verdicts. Four stay blocked, by a new cause:
The Serpent Society's poison ward (`CounterUnlessGetsPoisonCountersEffect`: "counter unless", step 10's option C), Cosmic Horror's
`DestroySourceAndDamageControllerIfDestroyedEffect` (a `ForcedCostOrElseEffect` fallback on the per-class list, step 10), Feint (no
creatures-blocking subject and no prevent-combat-damage-dealt-by-subject record) and the Rune pair (`BoostEquippedCreatureOfEnchantedEquipmentEffect`:
the Equipment *has* the granted static ability, so it needs a static grant of a static ability; its only kin is
`GrantKeywordToEquippedCreatureOfEnchantedEquipmentEffect`, 5 Runes, unaudited). Not re-triaged: `BoostAllCreaturesEffect` and
`EnchantedCreatureCantActivateAbilitiesEffect` (their blocker is step 10). Three blocked-step-model 4f rows name step 7/8 too and are stale:
`MillControllerAndDrawByManaValueEffect`, `MillControllerAndTargetPlayerLosesLifeByMilledManaValueEffect`,
`MillControllerAndDealDamageByHighestManaValueEffect` (they would split like Fateful Tempest's record).

**Fold batches** (survivor or shared component → records, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| TAP | `ChoosePermanentsEffect.anyNumber` + `TapPermanentsEffect(thisWay(CHOOSE))` recording a new `StepKind.TAP`, payloads over `AmountThisWay(TAP)` / `thisWay(TAP)` | `TapCreaturesGainLifePerCreatureEffect` (3), `TapAnyNumberOfPermanentsThenDrawPerTappedEffect` (2), `TapCreaturesBoostSelfEffect` (2), `TapPlayersPermanentsAndDamageEqualToCountEffect` (2, no choice), `TapAnyNumberOfCreaturesThenReflexiveAbilityEffect` (1), `TapAnyNumberOfPermanentsThenReflexiveAbilityEffect` (1), `TapCreaturesCreateTokensPerCreatureEffect` (1), `TapPermanentsAndPutCountersEffect` (1): 8 (13) |
| counters on a subject | `PutCounterOnTargetPermanentEffect` with `returnedThisResolution()`, `createdThisResolution()`, a chosen-permanent subject, its predicate mode | `PutCounterOnReturnedPermanentsEffect` (3), `PutCountersOnCreatedPermanentsEffect` (23), `PutCountersOnChosenPermanentEffect` (5), `PutCounterOnChosenOwnPermanentEffect` (22): 4 (53) |
| dealers | `TargetCreatureDealsDamageToControllerEffect` with non-target dealer subjects (last-known host) and player subjects | `EnchantedCreatureDealsPowerDamageToControllerEffect` (1), `EnchantedCreatureDealsDamageToEachOpponentEffect` (3), `EquippedCreatureDealsDamageToDefendingPlayerEffect` (2), `EachCreatureDealsDamageToItsControllerEffect` (1), `EnchantedCreatureDealsDamageToItsOwnerEffect` (1): 5 (8) |
| choose and attach | `ChoosePermanentsEffect.exactly(1, Equipment you control)` + `AttachSourceEquipmentToTargetCreatureEffect(thisWay(CHOOSE), host)` | `AttachOneOfControlledEquipmentToTargetCreatureEffect` (3), `…ToSourceEffect` (2): 2 (5) |
| static goad | `GoadEquippedCreatureEffect` gains a `PermanentSubject` (static entry point) | `GoadEquippedCreatureEffect` (12, survivor), `GoadSourceCreatureEffect` (1), and the unaudited `GoadSourceCreatureWhileControllerControlsItEffect`: 2 (13) |
| singles | existing records | Fateful Tempest's mill split (engine-built), Werewolf Ransacker's split, Arthur onto `ReturnTargetPermanentToHandAtEndOfCombatEffect(searched())`, the Wurms onto `FlipCoinWinEffect(null, ReturnToHandEffect(source()))`, Elemental Appeal onto `BoostTargetCreatureEffect(createdThisResolution())`, Ydwen Efreet's split, Ripples of Potential's split (proliferate records PUT_COUNTERS): 7 (8) |
| pay any amount | `EachPlayerPaysAnyManaEffect` with a payer subject (`you()`) and a resource (mana, mana of one colour, life, energy), recording its pay step; "you may pay {X}" is a `MayEffect` around it (CR 118.12: paying {0} is paying); payloads read the paid amount | `PayXManaEffect` (2), `PayXLifeEffect` (1), the seven `PayAnyAmountOfEnergy…` records (8), `PayEnergyToGainControlOfTargetEffect` (1), `PayEnergyThenDestroyTargetPermanentEffect` (1), `PayAnyAmountOfColorManaToDealDamageToAnyTargetEffect` (1), `PayAnyAmountOfLifeRevealAndExileEffect` (1), eleven `PayXMana…` records (11): 24 (26) |
| may pay a fixed cost | `MayPayManaEffect` | `PayManaAndSearchLibraryForCardNamedToBattlefieldEffect` (1), `PayManaThenMayTapOrUntapTargetCreatureEffect` (1): 2 (2) |
| dynamic cost amount | `PayLifeCost` / `PayEnergyCost` with a `DynamicAmount` amount | `PayXEnergyCost` (2), `PayLifeForEachCardInHandCost` (1), `PayLifeForEachCommanderColorCost` (1), the unaudited `PayXLifeCost` (8); `PayLifeEqualToCommanderColorIdentityCost` dead: 3 + 1 dead (4) |
| matching-spell predicates | `ReduceCastCostForMatchingSpellsEffect` with existing predicates or source zones | `ReduceCastCostForChosenSubtypeSpellsEffect` (2), `ReduceGraveyardSpellCastCostEffect` (2), `ReduceCastCostForChosenNameSpellsEffect` (1), `ReduceCommanderCastCostEffect` (1), `ReduceOwnCastCostForSharedCardTypeWithImprintEffect` (1): 5 (7) |
| spell-relative amount | the same survivor with an amount evaluated against the spell being cast | `ReduceCastCostForEachChosenColorEffect`, `ReduceCastCostForSameNameCardsInGraveyardEffect`, `ReduceOwnCastCostForEachSharedCardTypeWithExiledCardsEffect`: 3 (3) |
| spells that target X | one targets-[X] reduction (X: your permanent, the source, its attachment, the enchanted player, anything) | `ReduceOpponentCostForTargetingControlledPermanentEffect` (2), `ReduceOwnCastCostIfTargetingEnchantedPlayerEffect` (1), `ReduceAuraCastCostForTargetingAttachedPermanentEffect` (1), `ReduceTargetedCostEffect` (1): 4 (5) |
| mana unit × amount | a `ManaCost` reduction unit times a `DynamicAmount` | `ReduceOwnCastCostByManaCostEffect` (2), `ReduceColoredCastCostForMatchingSpellsByAmountEffect` (1), `ReduceActivationCostByManaCostEffect` (1), the unaudited `ReduceOwnColoredCastCostEffect` (4): 3 (4) |
| keyword costs | one keyword-cost reduction (buyback, dash, cycling, ninjutsu, Room unlock), each kind's reader kept | `ReduceBuybackCostEffect`, `ReduceDashCostEffect`, `ReduceCyclingCostEffect`, `ReduceNinjutsuCostEffect`, `ReduceRoomUnlockCostEffect`: 5 (5) |
| ability kind | `ReduceActivatedAbilityCostEffect` with an ability-kind filter replacing `powerUpOnly` | `ReduceBoastActivationCostEffect`, `ReduceExhaustAbilityCostEffect`, `ReduceNonManaActivatedAbilityCostEffect`: 3 (3) |
| per target | one per-target cost change (sign, whose spells, target filter) | `ReduceOwnCastCostForEachTargetEffect`, `ReduceOwnCastCostPerTargetEffect`, and `IncreaseOpponentCastCostPerTargetEffect`: 2 (2) |
| reducer singles | existing heads | `ReduceCastCostForNextSpellOfTypesThisTurnEffect` (2) onto `ReduceCastCostForNextMatchingSpellEffect`, `ReduceActivationCostPerCounterEffect` onto `ReduceActivationCostEffect(CountersOnSource)`, `ReduceFirstMatchingSpellCastCostEffect` onto `ReduceCastCostForFirstMatchingSpellEachTurnEffect` (a turn condition, not a flag), `ReduceEquipCostForTargetingAttachedPermanentEffect` onto `ReduceActivatedAbilityCostForTargetingSourceEffect`, `ReduceOwnCastCostIfTargetingGraveyardCardEffect` onto `ReduceOwnCastCostIfTargetingPermanentEffect`: 5 (6) |

Fold-run cautions written in the rows: `PutCountersOnCreatedPermanentsEffect` evaluated its amount against the source, the survivor evaluates a
non-target amount against the named permanent; Teferi's −3, Halo Forager and Isareth's reflexive target filters read X, so the reflexive
must carry the step's amount as X; `ReduceCastCostForNextMatchingSpellEffect`'s handler locks only `XValue` and `EventValue` at resolution,
Kaza's X is "as this ability resolves" (CR 608.2h); the TAP payloads count what was tapped, not what was chosen.

Unique (17): the engine-built `PayBlockCostsEffect`, `PayEchoCost`, `PayRansomEffect`, `ReduceTurnFaceUpCostForPermanentEffect`; one-card
mechanics `PayLifeEqualToSpellManaValueCost` (an alternative spell cost kind), `PayLifeInsteadOfCommanderTaxEffect`, `PayBlackManaWithLifeEffect`,
`PayMulticoloredSourceManaCost` ({Z}), `ReduceCastCostForChosenLegacyWordSpellsEffect`, `ReduceEnchantedHandCardCastCostEffect`,
`ReduceFirstActivatedAbilityCostForControlledCreatureEffect`, `ReduceFirstForetellCostEachTurnEffect`,
`ReduceColoredCastCostForFirstSpellTargetingCreatureEachTurnEffect`, `ReduceMinusOneMinusOneCountersEffect`; the two damage-minus-N
replacements `ReduceSpellDamageEffect` and `ReduceDamageToControlledCreaturesEffect` (two users of the shape); `ReduceGraveyardCardActivatedAbilityCostEffect`
(already the one record of its two cards). Blocked (Pay): `PayLifeOrPayManaCost` and `PayLifeOrSacrificePermanentCost` (A-or-B costs, step 10's
cost combinators) and `PayXManaConjureRandomCreatureWithManaValueAndCloakEffect` (Roalesk: its conjure-and-cloak half waits for Conjure).

**Seen, not changed** (confirmed by reading, not run):

- `PayEnergyThenDestroyTargetPermanentEffectHandler` (Behemoth of Vault 0) pays with `playerEnergyCounters.put`, skipping
  `GameData.setPlayerEnergyCounters`' record in `energyCountersPaidOrLostThisTurn`, so "energy paid this turn" readers miss it. Sure.
- `PayAnyAmountOfLifeRevealAndExileEffectHandler` (Vizkopa Confessor) pays life without `AsksDeathReplacementsFirst`, unlike
  `PayXLifeEffectHandler`; paying life is losing it (CR 119.4), so a Lich's Mastery-style choice isn't asked first. Fairly sure (read only).
- The pay-{X} handlers treat X = 0 as declining (Relentless Dead, Squealing Devil, Vigil for the Lost, Azor, Roalesk, Hollow Specter, Depala),
  while Isareth and Fire Lord Sozin queue their reflexive ability at 0; the prompt has no separate decline, and paying {0} is paying (CR 118.12).
  Low impact. Sure of the code, unsure of any visible case.
- `ReduceCastCostForChosenSubtypeSpellsEffectHandler` (Herald's Horn, Urza's Incubator) judges the card's printed types and subtypes for a spell
  cast face down; a face-down spell has no subtypes (CR 702.37c, 708.2a), and the matching-spells handler builds the face-down copy. Sure.
- Lashknife Barrier's `ReduceDamageToControlledCreaturesEffect` is read from `source.getCard().getStaticEffects()` in
  `DamagePreventionService.applyControlledCreaturesDamageReduction`: a face-down Barrier or one that lost its abilities still reduces (CR 613.1f,
  708.2a). Fairly sure (did not check whether another path filters).
- `PayAnyAmountOfEnergyToBoostTargetCreatureEffect` (Die Young) declares a benign target for a -X/-X (the AI's polarity). Sure, low impact.
- Rakdos Charm's third mode deals each creature's damage one at a time, not as one event (kin of the bite's open defect). Sure.
- `EnchantedCreatureDealsDamageToItsOwnerEffectHandler` (Enslave) takes the owner from `stolenCreatures`, else the controller, not from the card's
  owner. Unverified whether a case differs.
- `ReduceCastCostForChosenNameSpellsEffect`'s Javadoc says the card predicate path carries no source; `CardHasSourceChosenNamePredicate` finds it.
  Stale doc.

**For the next run.**

- Triage run 2: Search (40 rare), then Cant (39) and Sacrifice (38); Look (37) and Destroy (37) next. Conjure (39) and Perpetually (30) still wait
  for the owner. Re-triage the three stale Mill blocked-step-model rows above with the Mill split.
- Fold runs can start with the singles and the counters-on-a-subject batch (no new component); the TAP step and the pay-any-amount step are
  the two new components with the most records (8 and 24).

### Step 9, triage run 2 (2026-11-23)

Verdicts only: no Java, test or agent-docs file changed; the ratchet did not move (4,046 / 3,300). 79 ledger rows appended (step 9,
notes `triage 2: …`): the whole of Search (40 rare) and Cant (39 rare). Every record was read with its `normalfx` / `staticfx` handler to
the end and the card classes that use it; for the static Cant records with no handler, their readers were read in `AttackLegalityService`,
`CombatAttackService`, `GameQueryService` (counters, suspect, untap) and `CastingPermissionService`, and the capability interfaces
(`BlockabilityRestrictionEffect`, `BlockingRestrictionEffect`, `AttackCostEffect`, `BlockCostEffect`, `SpellCastingRestrictionEffect`)
with their implementers; the `LibrarySearchSupport` paths the search handlers call were read up to the prompt (`sendLibrarySearchToPlayer`,
`isSearchPrevented`), the `LibraryChoiceHandlerService` completions were not. Oracle text checked with `get_card` for Courier of
Comestibles, Last Light of Durin's Day, Beseech the Mirror, Auratouched Mage, Grim Reminder, Evolving Door, Wild Pair, Dwarven Recruiter,
Capricopian, Incite Hysteria, Ood Sphere, Flummoxed Cyclops, Canoptek Wraith, Skybreen, Graxiplon, Hollow Warrior and Tidal Influence; the
other cards were judged from their card classes and the records' Javadoc. No rulings were needed. CR 608.2h, 701.23a and 708.2a checked
with `get_rule` (2026-11-23).

| Family | Triaged | `step-9` | `blocked-other` | `unique` | Left unaudited |
|---|---|---|---|---|---|
| Search | 40 rare | 31 | 1 | 8 | 0 rare, 9 in all |
| Cant | 39 rare | 21 | 0 | 18 | 0 rare, 21 in all |

All families: 1,608 rare records without a row (before: 1,687). `step-9` rows waiting for fold runs: 147 (95 + 52).

**A SEARCH step.** Run 29 (step 8) gave the search-then-X records `blocked-step-model` ("no SEARCH step kind"):
`SearchLibraryAndConditionalEffect` (7 cards), `SearchLibraryForCardTypeToExileAndImprintEffect`, `SearchLibraryForEquipmentToBattlefieldAndAttachEffect`.
The library search parks the resolution at its prompt and its completion writes the parked entry before the epilogue resumes the next
step: that is how `searched()` reads what a search put onto the battlefield (Savage Order). So the search ends before the next step
starts; what is missing is a `StepKind.SEARCH` (found cards, their destination) recorded by that completion. Four more records need it
(Courier, Myr Incubator, Scion of the Ur-Dragon, Hide // Seek), seven with run 29's three, so they are `step-9` here with that component;
a fold run builds it and re-triages run 29's rows. The orchestrator should confirm this reading of "an earlier step" first.

**Fold batches** (survivor or shared component → records, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| same-quality filter | a card predicate "shares [`SharedQuality`] with [subject]" bound to the entry, generalizing `CardSharesNameWithTriggeringPermanentPredicate`; subjects target(), revealedThisWay(), thisWay(CHOOSE), source(), other creatures you control, a cost-sacrificed permanent | Pack Hunt, Mimeofacture (on `SearchTargetLibraryEffect`), Pattern Matcher, Infernal Tutor and Assembly Hall (`RevealCardsFromHandEffect` first), Canoptek Wraith (`ChoosePermanentsEffect` first), Kasmina (colour), Pyre of Heroes (creature type, with Birthing Pod's `SacrificeCreatureCost(true)` + `ManaValueBound(true, 1)`): 8 (8) |
| SEARCH step | `StepKind.SEARCH` recorded by the search completion; payloads gated or counted on it | `SearchLibraryForCardToHandOrCreateTokenEffect` (Courier), `SearchLibraryForCardsToExileAndCreateTokensEffect` (Myr Incubator), `SearchLibraryForDragonToGraveyardAndBecomeCopyUntilEndOfTurnEffect` (Scion), `SearchTargetLibraryAndConditionalEffect` (Hide // Seek), plus run 29's three: 4 (4) |
| attach host | `SearchLibraryEffect` with an attach-host subject (permanent: triggering() / source(); player: targetPlayer() / enchantedPlayer()) and the could-enchant filter | Sovereigns of Lost Alara + Light-Paws (one record), Auratouched Mage, Curse of Misfortunes, Bitterheart Witch: 4 (5); run 29's Equipment search-and-attach row joins |
| multi-zone search | a zone set on the two library-and/or-graveyard survivors (as `SearchZonesForCardNamedToBattlefieldEffect.selectedZones`) | Last Light of Durin's Day (hand, library), Coax from the Blind Eternities + Karn, the Great Creator (outside the game, face-up exile), Invasion of Arcavios (library, graveyard, outside the game): 3 (4) |
| one pick per predicate | `SearchLibraryForOneCardOfEachSubtypeEffect` (4, unaudited) with a list of card predicates and a destination | Conflux, Lotuslight Dancers (graveyard), Firemind's Foresight (mana values 3, 2, 1): 3 (3) |
| conditional destination | `SearchLibraryEffect` "onto the battlefield [tapped] if the found card matches P", folding `battlefieldIfChosenBeholdType` | Archdruid's Charm, Guidelight Pathmaker: 2 (2) |
| outside the game | `SearchOutsideGameForCardToHandEffect` (7) with a reveal flag and a destination | Vivien, Arkbow Ranger, The Raven's Warning (top of library), and the unaudited `SearchOutsideGameToHandEffect` (3): 2 (2) |
| search singles | existing records | Recruiters onto `SearchLibraryForCardsToTopEffect(subtype)`, Remembrance onto `SearchLibraryEffect(CardSharesNameWithTriggeringPermanentPredicate)`, Vivien on the Hunt onto a sacrifice step + `ManaValueBound(AmountThisWay.sacrificedManaValue(), true, 1)`, Scheming Symmetry + Varragoth onto `EachPlayerSearchesLibraryEffect` with `TOP_OF_LIBRARY`, Knowledge Exploitation onto `SearchTargetLibraryEffect(CAST_WITHOUT_PAYING)`: 5 (7) |
| combat cost | `CantAttackOrBlockUnlessPaysEffect` (2) with a side (attack / block / both), an attacker filter, and for cost-shape a non-mana cost | Phyrexian Marauder, Myr Prototype, Hipparion; Floodtide Serpent (return) and Hollow Warrior (tap), both `cost-shape`: 5 (5) |
| one-shot combat restriction | `CantBlockThisTurnEffect` / `CantAttackThisTurnEffect` with source(), triggering() and radiance subjects and the until-end-of-combat / affected player's next turn durations | Forgestoker Dragon + Flummoxed Cyclops (one record), Wall of Dust, Incite Hysteria: 3 (4) |
| companion | `CantAttackOrBlockAloneEffect` (14) with a count and a companion predicate | Orcish Conscripts, Okk, Scarred Puma: 3 (3) |
| defender condition | `CantBeBlockedIfDefenderControlsMatchingPermanentEffect` (19) with a defending-player condition | Hooded Horror, Nightkin Ambusher, Graxiplon (negated): 3 (3) |
| attack target | `CantAttackCardOwnerEffect` with a restricted `PlayerSubject` (owner, controller) | Xantcha + Alexios (survivor), Capricopian: 2 (3); 5m's two unique Enchanted attack-restriction rows could join |
| counters | `CantHaveCountersEffect` with a counter type | Melira (-1/-1), Blightbeetle (+1/+1): 2 (2) |
| Cant singles | existing records | Relic Runner onto `ConditionalEffect(ControllerCastSpellThisTurn(historic), CantBeBlockedEffect)`, Kraken of the Straits onto `CantBeBlockedByCreaturesMatchingPredicateEffect` (needs a strict count predicate), Tidal Influence onto `setCastCondition` (Blizzard's shape): 3 (3) |

Unique (26): Search: Jace, Architect of Thought's −8, Beseech the Mirror, Evolving Door, Wild Pair, Protean Hulk, Grim Reminder, Myriad
Landscape, Druid of the Emerald Grove. Cant: the one-card prohibitions with their own reader (Airtight Alibi's suspect, Glimpse's chosen,
Guardian Beast's control, Goblin Brawler's equip, Unable to Scream's face up, Angel of Eternal Dawn's night, Ood Sphere's tapping, Ogre
Enforcer's lethal damage), the combat rules of one card each (Ironclaw Curse, Rule with an Even Hand, The Aetherspark, Tromokratis, Become
the Pilot), the cast rules relative to turn state (Ethersworn Canonist, Mana Maze, Skybreen), and the two-card records already one record
(Leonin Arbiter + Mindlock Orb, Bloodthirster + Port Razer). Blocked (1): Doubling Chant, with Clarion Ultimatum's run-17 cause.

**Seen, not changed** (confirmed by reading, not run):

- Search handlers that never call `LibrarySearchTriggerHelper.recordSearchAndQueueTriggers` when nothing matches or the library is empty, so
  "whenever a player searches" triggers miss a search that found nothing (CR 701.23a): Sovereigns / Light-Paws, Auratouched Mage, Curse of
  Misfortunes, Bitterheart Witch, Guidelight Pathmaker, Archdruid's Charm, Firemind's Foresight (empty library), Myr Incubator (no match).
  Several empty-library branches also log "Library is shuffled." without shuffling. Sure of the code.
- `SearchLibraryForCardsToTopEffectHandler` (Congregation at Dawn, Insidious Dreams, Scouting Trek) neither records the search nor applies
  Aven Mindcensor's top-N limit; the Recruiters' handler does both. Fairly sure (its interaction handler doesn't either).
- Pattern Matcher and Doubling Chant read `getCard().getName()` of a face-down creature, which has no name (CR 708.2a). Sure of the code.
- Pyre of Heroes and Vivien on the Hunt read the sacrificed permanent's card mana value, not a face-down permanent's (CR 708.2a). Sure of
  the code; a rare case.
- Tidal Influence is a static ability of the permanent ("can't cast spells with this name"), skipped when that permanent is face down or
  has lost its abilities; Oracle is the spell's own cast condition about any permanent named Tidal Influence. Sure; low impact.
- Last Light of Durin's Day with no Dragon in hand or library searches nothing (no shuffle, no search record), where the player may search
  the library and find nothing (CR 701.23b). Fairly sure; low impact.
- Wall of Dust's record declares a harmful permanent `TargetSpec` for "that creature", which the text doesn't target. Unverified (how
  `Triggers.blocks()` fills the target wasn't traced).
- `SearchTargetLibraryEffectHandler` matches its filter without game state (`matchesCardPredicate(card, filter, null)`). Unverified impact.

**For the next run.**

- Triage run 3: Sacrifice (38 rare), then Destroy (37) and Look (37); Remove (34), Add (32), Gain (32), Become (30) next. Conjure (39) and
  Perpetually (30) still wait for the owner.
- A fold run for the SEARCH step (7 records with run 29's) once its reading is confirmed; the same-quality filter (8) is the largest new
  component of this run.
- The unaudited 3+-card records these batches touch: `SearchOutsideGameToHandEffect` (3), `SearchLibraryForOneCardOfEachSubtypeEffect` (4),
  `CantBeBlockedByCreaturesWithLessPowerEffect` (11, onto `CantBeBlockedByCreaturesMatchingPredicateEffect(PermanentPowerLessThanSourcePowerPredicate)`).

### Step 9, triage run 3 (2026-11-23)

Verdicts only: no Java, test or agent-docs file changed; the ratchet did not move (4,046 / 3,300). 75 ledger rows appended (step 9,
notes `triage 3: …`): the whole of Sacrifice (38 rare) and Destroy (37 rare). Every record was read with its `normalfx` / `mayfx` handler to
the end and the card classes that use it; for the cost records with no handler, their branches in `AdditionalSpellCostService`
(`extractAndRemove`, the satisfiability switch, the validators) and `SpellCastingService`'s pay methods, and the two activated-ability cost
handlers (`AllMatchingPermanentSacrificeCostHandler`, `DistinctNamePermanentSacrificeCostHandler`); for the as-enters records,
`AsEntersInteractionService`'s prompts and `BattlefieldPlacementService.applySacrificeOtherPermanentsWithSameName`; for Kuldotha Flamefiend,
`StackedDividedDamageTriggerService.unannouncedDivision`. The completion handlers behind the permanent-choice contexts were not read (a fold
run reads them). Oracle text checked with `get_card` for every `unique` and `blocked-*` card (Tectonic Split, Transmutation Font, Phyrexian
Dreadnought, Goblin Welder, Dusk Mangler, Oath of Lim-Dûl, Drop of Honey, Glyph of Reincarnation, Kill-Suit Cultist, Dead Ringers, Artificer's
Hex, Steel Hellkite, Giant Albatross, Erosion, Harsh Mercy, Cleansing, Flames of Moradin, Essence Vortex, Vona de Iedo, Territorial Strike,
Fatal Lore) and for Venom, Lethal Protector, The Dining Car, Prototype X-8, Vaevictis Asmadi, Mandate of Abaddon, Old Hob, Doom Foretold, Bringer
of the Last Gift, Red Sun's Twilight, Saw in Half, Broken Visage, Lich and Retribution; the other cards were judged from their card classes and
the records' Javadoc. Rulings with `get_rulings` for Barrin's Spite, Incriminate, Vaevictis Asmadi, Haphazard Bombardment (indestructible
permanents are left out of the random pick: the handler agrees), Dead Ringers, Territorial Strike (none) and Fatal Lore. CR 101.3, 101.4,
118.12a, 400.7, 601.2b, 601.2c, 603.7c, 608.2b, 608.2e, 700.2a, 701.21a and 708.2a checked with `get_rule` (2026-11-23).

| Family | Triaged | `step-9` | `blocked-other` | `blocked-player-loop` | `unique` | Left unaudited |
|---|---|---|---|---|---|---|
| Sacrifice | 38 rare | 32 | 2 | 0 | 4 | 0 rare, 13 in all |
| Destroy | 37 rare | 22 (1 of them `dead: delete`) | 8 | 1 | 6 | 0 rare, 6 in all |

All families: 1,533 rare records without a row (before: 1,608). `step-9` rows waiting for fold runs: 201 (147 + 54).

**A delayed ability about the entry's target.** `RegisterDelayedStepTriggerEffect.aboutAtNext` takes a permanent subject but refuses one that
declares a target, so "destroy / sacrifice it at the beginning of the next end step" after a targeted step has no form. Four records here need
it (Angrath's −3, Norritt and Nettling Imp, Berserk; Basalt Golem's is about the triggering blocker and fits `aboutAtNext(triggering())` as it
stands), and three older blocked rows wait for the same thing: `SacrificeTargetPermanentAtEndStepEffect`, `DestroyTargetPermanentAtEndStepEffect`
(5a, "unify the delayed mechanism") and Spinal Embrace's `SacrificeTargetPermanentAtEndStepAndGainLifeEqualToToughnessEffect` (8). A fold run
gives `aboutAtNext` a non-declaring reference to the entry's target and re-triages those three.

**Fold batches** (survivor or shared component → records, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| sacrifice-gated steps | `SacrificePermanentsEffect` + `ConditionalEffect(sacrificed(), X)` (inside the card's `MayEffect`) | Venom, Lethal Protector (draw and put, bounded by `AmountThisWay.sacrificedManaValue()`), Kuldotha Flamefiend (divided damage; the division service must look into the sequence), Garruk, the Veil-Cursed (search), Feed the Pack (tokens = sacrificed toughness), Scapeshift (`anyNumber` + search that many), Kethek (reveal until a lower mana value: a card bound by an amount) and Descendants' Fury (reveal until sharing a creature type: triage 2's same-quality filter over `thisWay(SACRIFICE)`): 7 (7) |
| sacrifice or else | the sacrifice step + `ConditionalEffect(NotCondition(sacrificed()), Y)` | Xathrid Demon, Doom Foretold, Cruel Reality, Lich (`HappenedThisWay.atLeast` with a dynamic count): 4 (4) |
| one of two targets | `ChoosePermanentsEffect.chosenBy(controllerOf(target()))` exactly one among `allTargets()`, then the sacrifice and a step over `except(allTargets(), thisWay(CHOOSE))` | Incriminate, Retribution (-1/-1 counter), Barrin's Spite (return): 3 (3) |
| delayed about the target | `aboutAtNext` over the entry's target (above) | Angrath, Norritt + Nettling Imp, Berserk; Basalt Golem over `triggering()`: 4 (5), plus the three blocked rows |
| delayed about created | `RegisterDelayedStepTriggerEffect.aboutCreated` | Kav Landseeker (your next turn's end step, `afterThisTurn`), Old Hob (replaces `DelayedPermanentActions`): 2 (2) |
| sacrifice-all cost | `SacrificeAllMatchingPermanentsCost(filter)` read on the spell path too, with a total-power-as-X option; cost-shape | Tomb of Urami (survivor), Soulblast, Kaervek's Spite: 3 (3) |
| cost aliases | the existing twin; cost-shape | `SacrificeCreatureOrPayManaCost` onto `SacrificePermanentOrPayManaCost` (Bayou Groff, Spark Harvest), `SacrificePermanentOrDiscardCardCost` onto `DiscardCardOrSacrificePermanentCost` (Demand Answers; its free-cast and AI readers move to the survivor): 2 (3) |
| as-enters sacrifice any number | `SacrificePermanentsAsEntersForCountersEffect` (Shimatsu, 5 cards, unaudited) with a payoff (counters per permanent, base P/T = count, base P/T = total power and toughness) | Dracoplasm, Wood Elemental: 2 (2) |
| as-enters sacrifice all | `SacrificeAllPermanentsAsEntersEffect(filter)` | Overlaid Terrain (survivor), Sheltered Valley (`PermanentHasSameNameAsSourcePredicate`): 2 (2) |
| random pick | `ChoosePermanentsEffect.atRandom` + `DestroyTargetPermanentEffect(thisWay(CHOOSE))` | Capricious Efreet + Wild Swing (among the targets), Metzali (creatures that attacked this turn), Haphazard Bombardment (aim counter, not indestructible): 3 (4) |
| power bound | `DestroyAllPermanentsEffect` with a power comparison against an amount locked at resolution | Fell the Mighty (`TargetPower` + 1), Mandate of Abaddon (`TargetPower` − 1), Expel the Interlopers (`ChooseNumberAtResolutionEffect` + X): 3 (3) |
| departed copies | token copies over a DESTROY step's departures (copiable values as they last existed) | Red Sun's Twilight, Saw in Half; the token-copy riders left in `DestroyTargetPermanentThenEffect`'s blocked row could join: 2 (2) |
| sacrifice singles | existing records | Dispense Justice + Odds // Ends (`FixedIfCondition(Metalcraft, 2, 1)`), Drake Familiar (`MayEffect(ReturnChosenPermanentsToHandEffect, prompt, SacrificeSelfEffect())`, with run 20's stale `SacrificeUnlessReturnOwnPermanentTypeToHandEffect` row, 4 cards), Bringer of the Last Gift (`except(cardsInGraveyards, cardsThisWay(SACRIFICE))`), Vaevictis Asmadi (`RevealTopCardPermanentToBattlefieldEffect` over `playersThisWay(SACRIFICE)`), Prototype X-8 (`ConjureDuplicateOntoBattlefieldEffect` with perpetual added types), The Dining Car (a least-toughness predicate with a scope; with the unaudited `DealDamageToCreatureWithLeastToughnessEffect`): 6 (7) |
| destroy singles | existing records | Extinction + Kindred Dominance (`ChooseCreatureTypeAtResolutionEffect`), Root Greevil (`ChooseColorAtResolutionEffect`, as Wash Out), Saproling Burst + Tombstone Stairwell (`PermanentCreatedBySourcePredicate`), Heliod's Punishment (a counters condition on `granter()`), Light of Judgment (`attachmentsOf(target(), Equipment)`), Krenko's Buzzcrusher (`oneForEach` + `EachPlayerSearchesLibraryEffect`), Chain of Acid (`CopyThisSpellForTargetControllerEffect`, as Chain of Smog), Shifting Shadow (an attach step with an attachment and a host subject, with triage 1's choose-and-attach batch), Broken Visage (token P/T from the destroyed target), Endemic Plague (triage 2's same-quality filter, cost-sacrificed subject): 10 (12) |
| dead | delete | `DestroyTargetCreatureThenCreateTokenCopiesWithHalfPowerToughnessEffect` (only an unused import in `SawInHalf.java`): 1 (0) |

Unique (10): Tectonic Split (half rounded up), Transmutation Font (different names), Phyrexian Dreadnought (an unless-cost chosen by total
power), Goblin Welder (simultaneous sacrifice and return), Drop of Honey + Porphyry Nodes (already one record, with its tie ruling), Glyph of
Reincarnation, Kill-Suit Cultist (a damage-to-destruction replacement), Dead Ringers, Artificer's Hex (the attachment's attachment), Steel
Hellkite. Blocked (11): the ransoms Giant Albatross, Erosion (an A-or-B ransom), Cleansing (any player may pay) and Essence Vortex wait for step
10's ransom design with `DestroyTargetPermanentUnlessControllerTakesDamageEffect`; Dusk Mangler's three-way cost for step 10's A-or-B cost
combinators; Oath of Lim-Dûl for a repeat-N-times wrapper (it keeps Torment of Hailfire's plumbing); Flames of Moradin, Vona de Iedo and
Territorial Strike for the owner's hidden-zone logging question (hand conjures, a perpetual change to a beheld card in hand); Fatal Lore's
engine-built record for an opponent mode chooser at cast; Harsh Mercy (`blocked-player-loop`: each player chooses a creature type).

**Seen, not changed** (confirmed by reading, not run):

- Territorial Strike: "If a Dragon creature was beheld, it perpetually gets +2/+2." `DestroyTargetPermanentThenPerpetuallyBoostEffectHandler`
  gives the +2/+2 to the destroyed permanent's card, not to the beheld Dragon. No ruling exists; "it" names the Dragon (the boost to an
  opponent's destroyed card would be pointless). Fairly sure.
- Fatal Lore's destroy mode (`FatalLoreEffectHandler` → `DestroyUpToNCreaturesOpponentControlsEffect`) chooses the two creatures as it
  resolves, with only hexproof filtered: they are not targets, so protection and the legality recheck (CR 608.2b) don't apply. The ruling says
  the caster chooses the targets after the opponent's mode choice, as the spell is cast (CR 601.2b, 601.2c). Sure of the code; whether
  `FatalLoreEffect`'s answer is asked at cast wasn't traced.
- Face-down reads (CR 708.2a): Venom and Kethek read the sacrificed permanent's card mana value; Sheltered Valley compares
  `getCard().getName()`, so a face-down permanent whose card is a Sheltered Valley is sacrificed. Sure of the code; rare.
- The Dining Car leaves creatures that can't be sacrificed out before finding the least toughness, so it sacrifices a creature with more
  toughness when the least-toughness one can't be sacrificed; that part should be ignored instead (CR 101.3). Fairly sure.
- One at a time where the text is one event (CR 101.4, 608.2e): `DestroyTokensCreatedWithSourceEffectHandler` (Saproling Burst, Tombstone
  Stairwell) destroys in a `tryDestroyAndLog` loop; Giant Albatross destroys each creature right after its controller answers, before the next
  controller chooses. Sure of the code.
- Drake Familiar offers enchantments by the card's printed type (`getCard().hasType`), not the permanent's type. Sure; low impact.
- Basalt Golem's record declares a harmful permanent target for "that creature", which the text doesn't target (triage 2's Wall of Dust
  kind). Unverified whether protection then stops it.
- Scapeshift, Feed the Pack and Phyrexian Dreadnought offer permanents that can't be chosen to sacrifice (no `cantBeChosenToSacrifice`
  filter). Sure of the code; low impact.
- Capricious Efreet and Wild Swing pick at random among targets still on the battlefield, not among legal ones (CR 608.2b). Unverified
  whether the resolution drops illegal targets first.

**For the next run.**

- Triage run 4: Look (37 rare), then Remove (34), Gain (32) and Add (32); Become (30), Draw (28), Set (27) and Double (27) next. Conjure (39)
  and Perpetually (30) still wait for the owner.
- Fold runs: the delayed-about-the-target component (4 records here and 3 blocked rows) and the sacrifice-gated and sacrifice-or-else splits
  (11 records, no new component but the dynamic `atLeast` for Lich) are the cheapest; the one-of-two-targets and random-pick batches need only
  existing pieces.
- The unaudited 3+-card records these batches touch: `SacrificePermanentsAsEntersForCountersEffect` (5), `ExileTokensCreatedWithSourceEffect`
  (5); and run 20's `SacrificeUnlessReturnOwnPermanentTypeToHandEffect` row is stale.

### Step 9, defect run 1 (2026-11-23)

Fixes for the defects triage runs 1 to 3 saw, no folding. Ratchet 4,046 / 3,300 → 4,045 / 3,300 (one record went away:
`CantCastSpellsWithSourceNameEffect`, Tidal Influence's; ledger row `absorbed`). One card file rewritten (Tidal Influence). Oracle text
checked with `get_card` for Herald's Horn, Pattern Matcher, Doubling Chant, Eddie Brock // Venom, Kethek, Sheltered Valley, Pyre of Heroes,
Vivien on the Hunt, Drake Familiar, Lashknife Barrier, Tidal Influence, The Dining Car, Territorial Strike, Giant Albatross and Fatal Lore;
rulings with `get_rulings` for Herald's Horn, Tidal Influence, Territorial Strike (none) and Fatal Lore. CR 101.3, 101.4, 118.12, 119.4,
201.2a, 400.7, 601.2c, 608.2b, 608.2e, 608.2h, 613.1f, 701.4a, 701.21a, 701.23a, 701.23b, 702.16b, 702.37c and 708.2a checked with
`get_rule` (2026-11-23).

| Defect | Verdict | Test (failed first?) |
|---|---|---|
| Herald's Horn reduced a creature spell cast face down (CR 702.37c, 708.2a) | fixed in `ReduceCastCostForChosenSubtypeSpellsEffectHandler` | `HeraldsHornTest.doesNotReduceFaceDownSpell` (yes) |
| Pattern Matcher, Doubling Chant read a face-down creature's card name (CR 201.2a) | fixed: `getEffectiveName` (null face down) | `PatternMatcherTest`, `DoublingChantTest.faceDown…` (yes) |
| Venom, Kethek, Pyre of Heroes, Vivien on the Hunt read the sacrificed card's mana value | fixed: `getPermanentManaValue` of the permanent or its snapshot | `EddieBrockTest`, `KethekCrucibleGoliathTest`, `PyreOfHeroesTest`, `VivienOnTheHuntTest` (yes) |
| Sheltered Valley sacrificed a face-down permanent whose card is a Sheltered Valley | fixed: `permanentHasName` | `ShelteredValleyTest.faceDownCopySurvives` (yes) |
| Drake Familiar offered permanents by the card's printed type | fixed: `hasEffectiveCardType` in the handler and `MayPenaltyChoiceHandlerService` | `DrakeFamiliarTest.faceDownEnchantmentCardIsNotAnEnchantment` (yes) |
| Lashknife Barrier reduced while face down or without abilities (CR 613.1f) | fixed: `DamagePreventionService` reads `activeStaticEffects` | `LashknifeBarrierTest` face-down and Humility cases (yes, both) |
| Tidal Influence's restriction was a static ability of the permanent | fixed: the card's cast condition; the record deleted | `TidalInfluenceTest.tidalInfluenceWithoutAbilitiesStillPreventsCasting` (yes) |
| Eight search handlers recorded no search when nothing matched or the library was empty (CR 701.23a) | fixed: `LibrarySearchSupport.endSearchFindingNothing` (records, shuffles, logs) | Sovereigns, Auratouched Mage, Curse of Misfortunes, Bitterheart Witch, Guidelight Pathmaker, Archdruid's Charm, Firemind's Foresight (2), Myr Incubator (yes, all) |
| Firemind's Foresight recorded one search per pick (Ob Nixilis, Unshackled triggered up to three times) | fixed: recorded once by the handler, the picks suppress theirs | `FiremindsForesightTest.threePicksAreOneSearch` (yes) |
| `SearchTargetLibraryEffectHandler` recorded no own-library search that found nothing (found while fixing the above) | fixed | `QuietSpeculationTest.ownSearchFindingNothingIsRecorded` (yes) |
| `SearchLibraryForCardsToTopEffectHandler` (Congregation at Dawn, Insidious Dreams, Scouting Trek) recorded no search and ignored Aven Mindcensor | fixed, as the Recruiters' handler | `CongregationAtDawnTest` (yes, both) |
| Behemoth of Vault 0 paid energy past the setter | fixed: `setPlayerEnergyCounters` | `BehemothOfVault0Test.deathTriggerPaymentCountsAsEnergyPaidThisTurn` (yes) |
| The Dining Car dropped unsacrificeable creatures before finding the least toughness (CR 101.3) | fixed | `TheDiningCarTest.leastToughnessCreatureThatCantBeSacrificedIsNotSkipped` (yes) |
| Territorial Strike boosted the destroyed permanent, not the beheld Dragon | fixed: the entry records the beheld hand card (`StackEntry.beholdCardId`, new, copied); the boost goes to the beheld permanent or hand card while it is still there (CR 400.7), the hand card logged as the perpetual hand grants log | two new `TerritorialStrikeTest` cases (yes); the old case asserting the destroyed permanent's boost replaced (it contradicted the Oracle text) |
| Saproling Burst, Tombstone Stairwell destroyed their tokens one at a time | fixed: `destroyBatchWithOutcome`, as `DestroyAllPermanentsEffectHandler` | `SaprolingBurstTest.destroysCreatedTokensTogether` (no: Blood Spatter Analysis's "one or more" triggered once either way; kept as a guard) |
| Scapeshift, Feed the Pack offered permanents that can't be sacrificed | fixed: `cantBeChosenToSacrifice` | `ScapeshiftTest`, `FeedThePackTest` (yes, the handler edit undone for that run) |
| Vizkopa Confessor without `AsksDeathReplacementsFirst` | not a defect: the handler has carried the mark since `c3adb102b7`, as `PayXLifeEffectHandler` | none |
| Capricious Efreet, Wild Swing among targets on the battlefield | not a defect: `StackEntry.getTargetIds()` returns only targets still legal after the resolution check | none |
| Wall of Dust, Basalt Golem "target" the other creature | not a defect: the entries are non-targeting, a hexproof creature is still affected; the harmful spec is read by the AI only | `WallOfDustTest`, `BasaltGolemTest` hexproof guards (passed) |
| Fatal Lore, Giant Albatross, Last Light of Durin's Day, pay-{X} at 0, Rakdos Charm, Phyrexian Dreadnought, `SearchTargetLibraryEffectHandler`'s game state, Die Young | left: see "Open defects seen and not fixed" | none |

**Behaviour changes.** Each row marked fixed above. Log lines: the empty-library searches of the eight handlers now shuffle (they logged
"Library is shuffled." without shuffling); Territorial Strike logs "A card in X's hand perpetually gets +2/+2." or "[Dragon] perpetually
gets +2/+2."; Doubling Chant with only face-down creatures no longer logs "controls no creatures". No test asserted a changed line.

**What a player sees.** No new prompt. The Territorial Strike hand line names no card; the face-down reads hide nothing more than before.

**Seen, not changed.** Added to "Open defects seen and not fixed": the triage leftovers above, the permanent-spell behold path that records
no beheld object, the Mindcensor branch that may drop Firemind's Foresight's later picks, `TheDiningCarTest`'s missing display names.
The Standing knowledge cites CR 115.10a for "a permanent that can't be chosen is neither offered, counted nor drawn"; 115.10a says that
an affected object isn't a target, so that citation is wrong (the orchestrator changed both to CR 608.2d: a player can't choose an
illegal or impossible option).

**Tests edited.** `TerritorialStrikeTest`'s boost case (contradicted the Oracle text); `EffectCountRatchetTest`'s record baseline. New
cases in 29 card test classes. No assertion was loosened.

**For the next run.** Fold runs as triage run 3 recommends; Fatal Lore and Giant Albatross need the pieces named in the open defects.

### Step 9, triage run 4 (2026-11-23)

Verdicts only: no Java, test or agent-docs file changed; the ratchet did not move (4,045 / 3,300). 71 ledger rows appended (step 9,
notes `triage 4: …`): the whole of Look (37 rare) and Remove (34 rare). Every record was read with its `normalfx` / `mayfx` / `entryfx`
handler to the end and the card classes that use it; for the records with no handler, their readers: the two controlled-counters cost
handlers (`RemoveCounterFromCreatureCostHandler`, `RemoveCounterFromControlledPermanentsCostHandler`) and their `AbilityActivationService`
branches, Rift Elemental's cost branches, `AsEntersInteractionService`'s Sin prompt (its completion was not read),
`GameQueryService.mayLookAtOpposingFaceDownCreatures`, and the may-completions in `MayMiscHandlerService` that the top-card records call.
`LookAtTopCardsEffectHandler` was read up to its dispatch and its may-put-onto-battlefield path, not whole. Oracle text checked with
`get_card` for every `unique` card (Ziatora's Envoy, Bison Whistle, Omnath, Locus of All, Make Your Own Luck, Zimone's Experiment, Choco,
Seeker of Paradise, Stroke of Luck, Orcish Librarian, Priest of Possibility, Susan Foreman, Diabolic Servitude, Tyrannical Pitlord, Pass the
Torch, Racketeer Boss, Hammerheim, Hierophant Bio-Titan, Rift Elemental, Freyalise's Winds, Cyclopean Tomb, Pyramids) and for Moment of
Truth, Telling Time, Sorin the Mirthless, Amareth, the Lustrous, Wakanda Forever!, Break Out, Moxite Refinery, Mutated Cultist, Eventide's
Shadow and Cosmic Cube; the other cards were judged from their card classes and the records' Javadoc. No rulings were needed. CR 113.7a,
611.2b, 702.62a, 702.63a, 702.73a and 707.10 checked with `get_rule` (2026-11-23).

| Family | Triaged | `step-9` | `unique` | Left unaudited |
|---|---|---|---|---|
| Look | 37 rare | 27 | 10 | 0 rare, 7 in all |
| Remove | 34 rare | 26 | 8 | 0 rare, 12 in all |

All families: 1,462 rare records without a row (before: 1,533). `step-9` rows waiting for fold runs: 253 (latest verdict per class).

**Counters on exiled cards.** Each kind of counter on a card in exile has its own `GameData` store and its own removal record: time
(`exiledCardTimeCounters`, and `suspendedSpellExiles` for a suspended spell), hit, scream, delay, refine and egg. Six records here
(Mari's hit, All Hallows' Eve's scream, and the engine-built suspend, delay, refine and egg removals) and the unaudited
`RemoveTimeCounterFromExiledCardEffect` (3 cards) are one verb over a card subject; what differs is the last-counter payload, which is the
card's own ability (suspend's cast, CR 702.62a; Uvilda's cast; the egg return; Ertai's Meddling's put-back as a copy, CR 707.10). The fold
needs one counter store on the exiled card keyed by kind (copied for simulations) and a removal step recording REMOVE_COUNTERS. That is
larger than a parameter: the fold run should say whether the store change is in scope, and give the batch `blocked-other` if not.

**Fold batches** (survivor or shared component → records, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| top-card look by player | `LookAtTopCardsOfTargetLibraryEffect(1, LOOK_ONLY)` with a `PlayerSubject` (`targetPlayer()` default, `you()`, `eachPlayer()`) | Plane-Merge Elf, Medomai's Prophecy and Case the Joint (the last two are the same record twice); Spy Network's top-card part: 3 (3) |
| face-down look | `LookAtFaceDownCreatureEffect` with a `PermanentSubject` (`target()` default) | Revealing Wind (face-down attackers and blockers), Spy Network (split: `LookAtHandEffect` + the top-card look + this over the face-down creatures the target player controls): 2 (2) |
| look, may reveal, then | Plane-Merge Elf's composition: the top-card look + `MayEffect(RevealTopCardOfLibraryEffect(you()))` + `ConditionalEffect(HappenedThisWay.toACard(REVEAL, filter), X)` | Quest for Ula's Temple (quest counter), Runo Stromkirk and Delver of Secrets (`TransformSelfEffect`, which keeps the prevention check and the Olag transform-back): 3 (3) |
| may reveal to hand | `LookAtTopCardMayRevealMatchingToHandEffect` | Bucolic Ranch + Cabaretti Ascendancy (a may-bottom otherwise destination), Amareth (triage 2's same-quality predicate over `triggering()`), Sorin the Mirthless (the survivor records REVEAL; a loss of the revealed card's mana value, keeping the Mastery ask-first): 3 (4) |
| top card onto the battlefield | `LookAtTopCardMayPutMatchingOntoBattlefieldEffect` with an otherwise destination (stay, hand, may bottom) and an effect-if-not-put (as `LookAtTopCardsEffect.effectIfNoCardChosen`); its match passes the source and its snapshot | Believe (hand), Lantern of Revealing (may bottom), Rulik Mons (Goblin token), Nissa, Steward of Elements (`CardManaValueAtMostSourceCountersPredicate`); the unaudited `LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect` (5) is the same record: 4 (4) |
| look and cast | `LookAtTopCardsAndMayCastEffect` (Velomachus Lorehold, survivor) with a `DynamicAmount` count and `CardManaValueAtMostAmountPredicate` | Cosmic Cube + Perception Bobblehead (one record); the unaudited `LookAtTopCardsMayCastExiledNonlandCardEffect` (3) could join with an exile-first destination: 1 (2) |
| three-way split | `LookAtTopCardsHandTopBottomEffect` (Telling Time, Moment of Truth, survivor) with an exile-playable-this-turn second destination | Expressive Iteration: 1 (1) |
| look at the top N | `LookAtTopCardsEffect` | Call to the Kindred and Conjurer's Mantle (triage 2's same-quality predicate: shares a creature type with `attached()`), Break Out (its selected-card follow-up, `MayPutSelectedCardOntoBattlefieldEffect(2)`), For the Ancestors (split: `ChooseCreatureTypeAtResolutionEffect` + `CardHasTypeChosenThisResolutionPredicate`, changelings kept, CR 702.73a): 4 (4) |
| two bounded picks | `LookAtTopCardsRevealTwoTypesToHandThenRestEffect` | Wakanda Forever! (permanent to the battlefield with an indestructible counter, permanent to hand, rest to the graveyard; the entry replacement would have this one user in that survivor): 1 (1) |
| hand choice, then draw | `ChooseCardsFromTargetHandEffect` (up to one) + `ConditionalEffect` on its step + `DrawCardEffect` over `targetPlayer()` | Oildeep Gearhulk (discard), Vendilion Clique (nonland, revealed, library bottom): 2 (2) |
| controlled-counters cost | `RemoveOneOrMoreCountersFromControlledPermanentsCost` (Jetfire, Moxite Refinery, survivor) with `allowsZero` and a from-one-permanent option; cost-shape | `RemoveOneOrMoreCountersFromControlledCreaturesCost` (Ooze Flux, Retribution of the Ancients): 1 (2) |
| chosen counters | `RemoveChosenCountersFromTargetPermanentEffect` (4, unaudited) recording REMOVE_COUNTERS as its choice ends, with a player target and a `source()` subject | Heartless Act + Price of Betrayal (one record), Rhys the Evermore (any number), Hex Parasite (+ `BoostSelfEffect(AmountThisWay(REMOVE_COUNTERS), 0)`), Tetravus (+ `CreateTokenEffect(AmountThisWay(REMOVE_COUNTERS))` created with the source): 4 (5) |
| player counters | a shared removal of a player's counters (one kind or all) over a `PlayerSubject`, recording REMOVE_COUNTERS | Survivor's Med Kit (rad), Leeches (poison, then damage `AmountThisWay`), Suncleanser's opponent mode (all kinds; its lock a `GrantStaticEffectToPlayerEffect` with `WHILE_SOURCE_REMAINS`), Mutated Cultist (permanent or opponent): 4 (4) |
| all counters | `RemoveAllCountersFromTargetPermanentEffect` | Suncleanser's creature mode (+ triage 2's `CantHaveCountersEffect` granted `WHILE_SOURCE_REMAINS`, CR 611.2b), Aether Snap (`allPermanents()`, one action, last-counter notifications kept): 2 (2) |
| counters from among permanents | `RemoveUpToCountersFromAllPermanentsEffect` (Sensational Spider-Man, survivor) recording REMOVE_COUNTERS | Eventide's Shadow (any kind, no bound); 5f's unique `RemoveCounterFromChosenPermanentsEffect` could join: 1 (1) |
| as-enters counter removal | `RemoveAllCountersFromChosenPermanentsThenEnterWithCountersEffect` (Sin, survivor) with an all-matching option | Thief of Blood: 1 (1) |
| lose protection | `RemoveAllProtectionUntilEndOfTurnEffect` (Shay Cormac, survivor) with a from-colour option | Cephalid Snitch: 1 (1) |
| exiled-card counters | one store and one removal (above) | Mari, All Hallows' Eve, and the engine-built suspend, delay (Ertai's Meddling), refine (Uvilda) and egg (Darigaaz) records: 6 (2 + engine-built) |
| Remove splits | existing records | Decimator Beetle (a one-counter removal over `target(0)` + `PutCounterOnTargetPermanentEffect` over `target(1)`, as Raubahn's two-group attack trigger; `AttackCounterMoveEffect` and its pipeline branches go), Vhal's specialized face (`RemoveAllCountersEffect(STUDY, source())` + `QueueReflexiveAbilityEffect`, as run 21's split): 2 (1 + engine-built) |
| Look single | existing record | Lumbering Laundry (`GrantStaticEffectToPlayerEffect.untilEndOfTurn(new LookAtFaceDownCreaturesEffect())`, its reader asking for the player's floating effect): 1 (1) |

Unique (18): Look: Ziatora's Envoy (a land play or a free cast from the top, else to hand), Bison Whistle (a type ladder over one card),
Omnath, Locus of All (mana from the revealed card's colours), Make Your Own Luck (plot), Zimone's Experiment (destinations split by type),
Choco (two ordered picks), Stroke of Luck (the same-name set), Orcish Librarian (a random exile among looked-at cards), Priest of
Possibility (a keyword census), Susan Foreman (the planar deck). Remove: Diabolic Servitude + Tyrannical Pitlord (the linked permanent and
its mutual-bond bookkeeping, already one record), Pass the Torch + Racketeer Boss (a perpetual ability that removes itself, already one
record), Hammerheim (landwalk kinds modelled as predicates), Hierophant Bio-Titan (a cost that reduces per counter removed), Rift
Elemental (a cost from a permanent or a suspended card), Freyalise's Winds (an untap-step replacement), Cyclopean Tomb (its tracked
lands), Pyramids (a damage-removal destruction replacement that isn't regeneration).

**Seen, not changed** (confirmed by reading, not run):

- Moxite Refinery: "Remove X counters from an artifact or creature you control" takes them from one permanent, but
  `RemoveCounterFromControlledPermanentsCostHandler` lets X be split across several; X = 0 is refused (the cost can be paid with zero);
  its two modes target only an artifact or creature you control, where the Oracle text says "target artifact" and "target creature";
  with `CounterType.ANY` the handler picks which kinds to remove in enum order, not the player. Sure of the code; fairly sure of the reading.
- Make Your Own Luck puts all three looked-at cards into hand, then plots the chosen one from hand; the text exiles it from among the
  looked-at cards and puts only the rest into hand. Sure of the code; low impact (a put-into-hand trigger would see it).
- Thief of Blood's entry handler removes counters with `setCounterCount` and sends no last-counter-removed notification, which Aether Snap's
  handler sends, so a vanishing permanent losing its last time counter this way isn't sacrificed (CR 702.63a). Sure of the code; whether
  another hook catches it wasn't traced.
- Hammerheim ends the target's floating predicate-landwalk grants outright (`endFloatingEffectsOnPermanent`) instead of suppressing them
  until end of turn, so a grant lasting longer is lost. Sure of the code; rare.
- Runo Stromkirk transforms without `isTransformPrevented`, which Delver of Secrets and `TransformSelfEffect` check. Sure of the code; low
  impact.

**For the next run.**

- Triage run 5: Add (32 rare), Gain (32), then Become (30) and Draw (28); Double (27), Set (27), Copy (26) and Seek (26) next. Conjure (39)
  and Perpetually (30) still wait for the owner.
- Fold runs: the cheapest batches here need only existing records: the top-card look by player (3 records), the face-down look subject,
  the look-may-reveal compositions (3), the top-card battlefield record's otherwise destination (4 + the 5-card duplicate), lose protection,
  the as-enters counter removal, and Lumbering Laundry. The chosen-counters and player-counters batches need REMOVE_COUNTERS recorded by a
  prompted removal; the exiled-card counter batch needs the store decision above.
- The unaudited 3+-card records these batches touch: `LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect` (5),
  `LookAtTopCardsMayCastExiledNonlandCardEffect` (3), `RemoveChosenCountersFromTargetPermanentEffect` (4), `RemoveTimeCounterFromExiledCardEffect` (3).

### Step 9, triage run 5 (2026-11-23)

Verdicts only: no Java, test or agent-docs file changed; the ratchet did not move. 64 ledger rows appended (step 9, notes
`triage 5: …`): the whole of Add (32 rare) and Gain (32 rare); no third family was started, since the brief named these two. Every
record was read with its handler to the end and the card classes that use it; for the records with no handler, their readers:
`GameQueryService`'s counter-replacement paths (`replaceCounters`, `collectCounterReplacements`, the entering, planar and player-counter
paths), `TokenAdditionReplacements`, `TokenCreationReplacements` (its apply step), `TokenCreationReplacementSupport`'s per-kind counts and
blueprints, `PermanentControlSupport.createTokens` up to the Food additions, `ActivatedAbilityExecutionService`'s noted-mana methods,
`CastingCostService.getActivatedAbilityAdditionalCosts`, `CreatureControlService.expireStaleControlEffects`, `TargetLegalityService`'s
Preacher branch, `GainControlOfTargetEffectHandler` and `PayPerCounterOrElseEffectHandler`. `GainControlOfPermanentsCost`'s branches in
`ForcedCostOrElseEffectHandler`, `DestructionSupport` and `MayPenaltyChoiceHandlerService` were located, not read. Oracle text checked with
`get_card` for every `unique` and `blocked-*` card (Snowfall, Sasaya's Essence, Elemental Resonance, Charmed Pendant, Thran Portal,
Brutal Suppression, Jeweled Amulet, Ice Cauldron, Zabaz, Aminatou, Commandeer, Aethersnatch, Garland, Quicksilver Elemental, Midnight
Crusader Shuttle, Chef's Kiss, Eriette, Herald of Leshrac) and for the counter-replacement cards, Xorn, Jolene, Tippy-Toe, Bilbo, Case of
the Pilfered Proof, Ultima, Caged Sun, Gauntlet of Power, The Caves of Androzani, Ichormoon Gauntlet, Shield Broker, Kaya, Ghost Haunter
and Squidnapper (both `CMB1`), Preacher, Aura Graft, Tevesh Szat, Peer Pressure, Urborg Scavengers, Wretched Bonemass, Eater of Virtue,
Cairn Wanderer, Koh, Idris, Rex, Mairsil, Thranduil, Manascape Refractor and Scheming Fence; the others were judged from their card
classes. Rulings read: Charmed Pendant. CR 305.6, 611.2b, 611.2c, 613.1f, 702.26d, 702.26f and 708.2a checked with `get_rule` (2026-11-23).

| Family | Triaged | `step-9` | `blocked-other` | `unique` | Left unaudited |
|---|---|---|---|---|---|
| Add | 32 rare | 25 | 1 | 6 | 0 rare, 8 in all |
| Gain | 32 rare | 24 | 2 | 6 | 0 rare, 2 in all |

All families: 1,398 rare records without a row (before: 1,462). `step-9` rows waiting for fold runs: 302 (latest verdict per class).

**Two static card-subject batches.** Ten records give their permanent the activated (and for Idris and Koh, triggered) abilities or the
keywords of cards in a graveyard, on top of a library or in exile. Each walks its zone itself. They fold onto the two exiled-card
records with a `CardSubject`, but no `CardSubject` variant is static today (`SUBJECTS_REFERENCE.md`): the fold run first needs the
resolver's static entry to name card sets (`cardsInGraveyards`, `cardsInYourGraveyard`, a top-of-library card, `exiledWithSource`,
`exiledCards` with a counter filter), judged per layered pass like the permanent sets. Rex's brain and Mairsil's cage stores (and
Rayami's blood store, read by the keyword survivor) belong with triage 4's one store for counters on exiled cards.

**Fold batches** (survivor or shared component → records, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| counter-placement replacement | `AddOnePlusOneCountersEffect` (Hardened Scales, 5 cards, unaudited) with a counter kind (any, +1/+1), an affected `PermanentPredicate` judged against the source and a placed-by-you flag; `GameQueryService`'s `appliesGlobally` / `instanceof` branches become the predicate read | Pir + Doc Samson (one record), Solid Ground + Kami of Whispered Hopes (one record), Winding Constrictor (two records: permanents, and the player side, which joins only with the Double family's player-counter records), Ozolith, Caradora, Benevolent Hydra, Mowu (keeps its entering branch): 8 (9) |
| token addition | `AddTokenCreationEffect` (Xorn, Jolene; survivor) with an added blueprint, an event filter and a count (one, that many, one per matching token); the `Addition` enum, the fixed-order counts and the per-kind helpers collapse | Case of the Pilfered Proof (`ConditionalEffect(SourceIsSolved)`), Tippy-Toe, Quina, Worldwalker Helm, Donatello, Queen Allenal, Chatterfang, Stridehangar Automaton, Bilbo; the unaudited Academy Manufactor could join: 10 (11) |
| produced-type mana | `AddManaOfTypeProducedByTappedPermanentEffect` (4 cards, unaudited) with a required colour (fixed or the source's chosen colour) and a recipient `PlayerSubject` | Ultima (`COLORLESS`, Forsaken Monument's shape), Caged Sun + Gauntlet of Power (one record); the three fallbacks for a tap that reported no produced type must become one: 2 (3) |
| persistent mana | `AwardManaOfColorsEffect` with a "you don't lose this mana until end of turn" option, with the unaudited `AwardPersistentManaEffect` (19) and `AwardPersistentAnyColorManaEffect` (3) | Grand Warlord Radha (`XValue`, the trigger-time attacker count): 1 (1) |
| counter-kind choice | `AddAnotherCounterOfChosenTypeToTargetEffect` (Ichormoon Gauntlet, Animation Module; survivor) with a `PermanentSubject` and the prompt's optional flag | The Caves of Androzani: 2 (3) |
| Add splits | existing records | Primordial Ooze (`PutCountersOnSelfEffect` + `PayPerCounterOrElseEffect`; cost-shape), Consuming Ferocity (a counter on `attached()` + `ConditionalEffect(EnchantedPermanentMatches(...))` over triage 1's dealer batch + destroy without regeneration): 2 (2) |
| permanent-ability borrowers | `GainActivatedAbilitiesOfOwnCreaturesEffect` (Robaran Mercenaries, Marvin; survivor) with a static `PermanentSubject`, an ability filter (all, non-loyalty, loyalty only) and one read of a permanent's current activated abilities (five drifting copies today) | Drana and Linvala, Manascape Refractor (keeps CR 305.6), Experiment Kraj, Scheming Fence, Nicol Bolas, Dragon-God: 6 (7) |
| card-ability borrowers | `GainActivatedAbilitiesOfExiledCardsEffect` (5 cards, unaudited) with a static `CardSubject` and a triggered-too option | Necrotic Ooze + Mirran Safehouse, Trazyn + Thranduil, Skill Borrower + Conspicuous Snoop (one record each), Rex, Mairsil, Idris, Koh (a last-chosen card subject; stays if it isn't added): 7 (10) |
| card-keyword borrowers | `GainKeywordsOfCreatureCardsExiledWithSourceEffect` (3 cards, unaudited) with a static `CardSubject` and a recipient `PermanentSubject` | Urborg Scavengers + Wretched Bonemass (one record), Eater of Virtue, Cairn Wanderer; the four hexproof and protection copies differ and must become one rule: 3 (4) |
| maintained-predicate control | `GainControlOfTargetEffect`'s `maintainTargetPredicate` (Old Man of the Sea), Old Man's "a noncreature still holds" special case moved into its predicate | Shield Broker (has a shield counter), Kaya, Ghost Haunter's emblem (is haunted; replaces `KayaHauntedCreatureTargetValidators`' check): 2 (2) |
| Gain control, single | existing records | Preacher (`GainControlOfTargetEffect.opponentChosenTarget(WHILE_SOURCE_REMAINS_TAPPED)`, Evangelize's form), Squidnapper (split: control + the granted ransom ability, both until it leaves), Aura Graft (split: control + the attach-to-another-permanent step of the two unaudited `AttachTargetAuraToAnotherPermanent*` records), Tevesh Szat (split: control of all commanders + a put over every command zone through the entry batch), Peer Pressure (split: `ChooseCreatureTypeAtResolutionEffect` + a filtered `ControllerControlsMorePermanentsThanEachOtherPlayer` + control): 5 (5) |
| delayed about a permanent | triage 3's batch (`RegisterDelayedStepTriggerEffect.aboutAtNext`) | Tolarian Entrancer (end of combat, the blocker): 1 (1) |

Blocked (3): Zabaz (`AddOnePlusOneCountersToModularAbilityEffect`: a modular-only placement condition, one card), Eriette
(`GainControlOfAuraAttachedPermanentEffect`: a duration tracking the triggering Aura, not the source) and `GainControlOfPermanentsCost`
(engine-built for Herald of Leshrac's cumulative upkeep: a cost-combinator head for step 10).

Unique (12): Add: Snowfall (snow-scaled, cumulative-upkeep-only mana), Sasaya's Essence (a same-name land count, one type choice per
mana), Elemental Resonance + Charmed Pendant (mana from a mana cost, already one record), Thran Portal + Brutal Suppression (already the
one non-mana additional activation cost record), Jeweled Amulet and Ice Cauldron (the noted-mana pair; a merge needs a one-card
restriction). Gain: Aminatou (a control rotation around the table), Commandeer + Aethersnatch (already the one spell-control record),
Garland (control while a recorded player is the monarch), Quicksilver Elemental (a one-shot borrow of a target's abilities, CR 611.2c),
Midnight Crusader Shuttle (a stolen creature put into the attack), Chef's Kiss (random retargeting of a stolen spell and its copy).

**Seen, not changed** (confirmed by reading, not run):

- Eriette, the Beguiler keeps control with `Duration.WHILE_ATTACHED`, which phasing out doesn't end; a resolution's "for as long as
  that Aura is attached to it" ends when the permanent phases out (CR 702.26f; `REMAINS_ATTACHED` is that form). Sure of the code; fairly
  sure of the reading.
- Squidnapper: "until Squidnapper leaves the battlefield" is `WHILE_SOURCE_REMAINS`, which ends as Squidnapper phases out; phasing isn't
  leaving (CR 702.26d), so `UNTIL_SOURCE_LEAVES`. The ransom is payable by the creature's owner, where the text says "that player".
  Sure of the code; rare.
- Scheming Fence reads the chosen permanent's card abilities with no face-down check, so a face-down chosen permanent gives its hidden
  abilities (CR 708.2a); the other borrowers check. Sure of the code; whether its as-enters choice offers a face-down permanent wasn't
  traced.
- The ability borrowers drift: Nicol Bolas, Dragon-God reads printed loyalty abilities only (no granted ones) and asks only the floating
  `losesAllAbilitiesByEffect`; Robaran / Marvin, Experiment Kraj and Scheming Fence don't ask `hasLostPrintedAbilities`, Drana and Linvala's
  does; Trazyn and Thranduil don't ask `graveyardCardsHaveLostAllAbilities`, Necrotic Ooze's does. Sure of the code.
- The ordered token-addition path (`TokenAdditionReplacements.instances`) and Bilbo's fixed count read printed static effects and ask
  only the floating "loses all abilities", so a static ability-removing effect (Humility on Chatterfang) leaves the addition working
  (CR 613.1f). Sure of the code; fairly sure of the consequence.
- Charmed Pendant offers a monocoloured hybrid symbol's generic half ({2/W} as {C}{C}); its ruling gives one mana of a colour per
  symbol. Sure of the code; low impact.
- Animation Module offers a player's poison and energy counters only, not experience, rad or other player counters. Sure of the code;
  low impact.
- Tolarian Entrancer's record declares a harmful permanent target its text doesn't have (Basalt Golem's kind). Sure of the record; the
  consequence for a blocker with protection wasn't traced.
- Thran Portal's "mana abilities of this land cost an additional 1 life" applies to all its activated abilities, granted non-mana ones too.
  Sure of the code; low impact.
- Tevesh Szat's -10 puts the commanders from the command zones with `putPermanentOntoBattlefield`, not through the entry batch (no
  as-enters choices), and marks battlefield permanents commanders by card id as it resolves. Sure of the code; low impact.

**For the next run.**

- Triage run 6: Become (30 rare), Draw (28), then Set (27) and Double (27); Seek (26), Copy (26), Deal (25), Cast (25) and Allow (25) next.
  Conjure (39) and Perpetually (30) still wait for the owner. Double's counter-replacement records (`DoubleCountersOn*`,
  `HalveCountersPutByOpponentsEffect`'s kin) should be judged against this run's counter-placement batch (a doubling operation on the same
  survivor would also take Winding Constrictor's player record).
- Fold runs: the cheapest batches here need only existing records: the maintained-predicate control pair, Preacher, Tolarian Entrancer
  (with triage 3's delayed batch), the counter-kind choice, Peer Pressure and Consuming Ferocity. The two card-subject batches need the
  static card subjects first; the counter-placement and token-addition batches each rewrite one `GameQueryService` / token reader path.
- The unaudited 3+-card records these batches touch: `AddOnePlusOneCountersEffect` (5), `AddManaOfTypeProducedByTappedPermanentEffect` (4),
  `AddOneOfEachManaTypeProducedByLandEffect` (17, the same "one mana of a type it produced" with another fallback), `AwardPersistentManaEffect`
  (19), `AwardPersistentAnyColorManaEffect` (3), `GainActivatedAbilitiesOfExiledCardsEffect` (5), `GainKeywordsOfCreatureCardsExiledWithSourceEffect`
  (3), `AttachTargetAuraToAnotherPermanentOfSameTypeEffect` (3).

### Step 9, defect run 2 (2026-11-23)

Fixes for the defects triage runs 4 and 5 saw and defect run 1 left, no folding. Ratchet 4,045 / 3,300 → 4,044 / 3,299 (one record went
away: `DestroyUpToNCreaturesOpponentControlsEffect`, engine-built for Fatal Lore, with its handler and
`MultiPermanentChoiceContext.DestroyCreaturesOpponentControls`; ledger row `absorbed`). One card file rewritten (Moxite Refinery). Oracle
text checked with `get_card` for Fatal Lore, Moxite Refinery, Make Your Own Luck, Thief of Blood, Eriette, the Beguiler, Squidnapper
(`CMB1`) and Last Light of Durin's Day; rulings with `get_rulings` for Fatal Lore, Moxite Refinery (none), Make Your Own Luck, Eriette and
Nicol Bolas, Dragon-God. CR 101.3, 101.4, 118.3, 601.2c, 608.2b, 608.2d, 608.2e, 609.3, 613.1f, 701.23a, 701.23b, 701.23d, 702.26d,
702.26f, 702.63a and 708.2a checked with `get_rule` (2026-11-23).

| Defect | Verdict | Test (failed first?) |
|---|---|---|
| Fatal Lore's destroy mode chose its creatures at resolution without targeting (its ruling, CR 601.2c, 608.2b) | fixed: the caster chooses up to two target creatures that player controls right after the opponent's choice (`MayPenaltyChoiceHandlerService`, completed by `MultiPermanentChoiceHandlerService` through `TargetsAfterOpponentModeChoice`); the mode destroys the spell's legal targets together; all targets gone, the spell doesn't resolve | three new `FatalLoreTest` cases (yes, all); five older cases now answer the target prompt before priority |
| Moxite Refinery split X across permanents, refused X = 0, targeted only your own permanents | fixed: `RemoveOneOrMoreCountersFromControlledPermanentsCost.xFromOnePermanent` (`allowsZero`, `fromOnePermanent`), targets any artifact / creature | four new `MoxiteRefineryTest` cases (yes, all); its opponent-target assertion replaced (it contradicted the Oracle text) |
| Thief of Blood removed a vanishing permanent's last time counter without the sacrifice trigger (CR 702.63a) | fixed: its entry handler runs the last-time-counter and battle-defense checks Aether Snap's handler runs | `ThiefOfBloodTest.removingLastTimeCounterTriggersVanishing` (yes, with Ravaging Riftwurm; a card with its own trigger, Aven Riftwatcher, was already caught by `CounterTriggers`) |
| Squidnapper's control and ransom ended as it phased out; only the owner could pay | fixed: `UNTIL_SOURCE_LEAVES` (new branch in `GainControlOfTargetEffectHandler`, the grant handler and the ransom layer read), `DurationClock.holds` keeps "until it leaves" / "until you lose control" while the source is phased out (CR 702.26d), the ransom is activatable only by the player it was taken from (a preset granting player kept by `GrantActivatedAbilityEffectHandler`) | two new `SquidnapperTest` cases (yes, both) |
| Eriette's control used `WHILE_ATTACHED` | not a defect in behaviour: the control sweep (`expireStaleControlEffects`) already ended it as the Aura phased out with the permanent; the duration is now the resolution form `WHILE_REMAINS_ATTACHED` | `ErietteTheBeguilerTest.controlEndsAsStolenPermanentPhasesOut` (no; kept as a guard) |
| Scheming Fence gained a face-down chosen permanent's hidden abilities (CR 708.2a); the borrowers' reads drifted | fixed: Scheming Fence and Robaran / Marvin / Experiment Kraj read Drana and Linvala's one `effectiveActivatedAbilities`, which now keeps a Blood Sun creature's mana abilities | `SchemingFenceTest.gainsNothingFromAFaceDownPermanent` (yes); the Blood Sun branch is untested (read only) |
| Trazyn, Thranduil ignored "cards in graveyards lose all abilities" | fixed: `graveyardCardsHaveLostAllAbilities`, as Necrotic Ooze | `TrazynTheInfiniteTest.gainsNothingWhileGraveyardCardsLoseAbilities` (yes) |
| Token additions ignored a static "loses all abilities" (Humility on Chatterfang, CR 613.1f) | fixed in the three replacement sources (`TokenAdditionReplacements`, `TokenKindReplacements`, `JinnieFayTokenReplacements`): `hasLostPrintedAbilities` | `ChatterfangSquirrelGeneralTest.addsNothingWhileHumilityRemovesItsAbilities` (yes) |
| Phyrexian Dreadnought offered and counted creatures that can't be sacrificed (CR 608.2d) | fixed: `cantBeChosenToSacrifice` | `PhyrexianDreadnoughtTest.creatureThatCantBeSacrificedIsNotOffered` (yes) |
| Runo Stromkirk transformed without the prevention check | not a defect: `AnimationSupport.transformToBackFace` asks `isTransformPrevented` for every transform | `RunoStromkirkTest.doesNotTransformWhileTransformingIsPrevented` (no; kept as a guard) |
| Hammerheim ended floating predicate-landwalk grants for good | not a defect: every such grant lasts until end of turn, and ending it keeps an earlier grant from coming back when a later snow-landwalk grant expires Hammerheim's "ignore landwalk" (the grant handler expires it) | none |
| Tolarian Entrancer's harmful target spec | not a defect: the trigger and its delayed ability don't target; the spec is read by the AI only | `TolarianEntrancerTest.gainsControlOfHexproofBlocker` (no; kept as a guard) |
| Make Your Own Luck, Bolas, the token support's static counts, Giant Albatross, Last Light of Durin's Day, Rakdos Charm, pay-{X} at 0, `SearchTargetLibraryEffectHandler`'s game state, Charmed Pendant, Animation Module, Thran Portal, Tevesh Szat, Moxite's counter-kind order | left: see "Open defects seen and not fixed" (each entry says what it needs) | none |

**Behaviour changes.** Each row marked fixed. Log lines: Fatal Lore logs "[player] targets A, B." or "[player] chooses no targets." as
the targets are chosen; its old "chooses not to destroy any creatures" line is gone; the Squidnapper grant logs "until the source leaves
the battlefield".

**What a player sees.** One new prompt: Fatal Lore's caster gets "Choose up to N target creatures to destroy." over the targetable
creatures the choosing opponent controls, right after the opponent's mode answer (the AI answers it with its default opposing pick,
`MultiPermanentChoiceAiStrategy`). The target log names a face-down creature by `FaceDownVisibility.publicName`.

**Shared-engine changes.** `DurationClock.holds` (phased-out source of an "until" duration), `GrantActivatedAbilityEffectHandler` (keeps a
preset granting player; `UNTIL_SOURCE_LEAVES` grants), `GainControlOfTargetEffectHandler` (`UNTIL_SOURCE_LEAVES`),
`LayerSystemService.removeAbilitiesGrantedByLaterAbilityLoss` (the ransom grant's new duration), `MultiPermanentChoiceHandlerService`
(the new context, the old one gone), `MayPenaltyChoiceHandlerService` (Fatal Lore's target prompt), the three token replacement sources,
the borrower read.

**Seen, not changed.** Added to "Open defects seen and not fixed": Moxite's counter-kind order, Make Your Own Luck, Bolas, the token
support's static counts, the possible double vanishing trigger, the triage-5 low-impact four, `ErietteTheBeguilerTest`'s missing display
names.

**Tests edited.** `FatalLoreTest` (five cases answer the target prompt before priority: the target choice moved to casting),
`MoxiteRefineryTest` (the opponent-target assertion contradicted the Oracle text; display names added), `SquidnapperTest.thiefCannotPayRansom`
(no longer asserts the "owner" message), `MultiPermanentChoiceAiStrategyTest` (builds the new context), `EffectCountRatchetTest`'s
baselines. New cases in 12 card test classes. No assertion was loosened.

**For the next run.** Fold runs as triage runs 3 to 5 recommend. Giant Albatross and Last Light of Durin's Day each need a piece named in
the open defects; Rakdos Charm waits with the bite's simultaneous damage.

### Step 9, triage run 6 (2026-11-23)

Verdicts only: no Java, test or agent-docs file changed; the ratchet did not move. 85 ledger rows appended (step 9, notes
`triage 6: …`): the whole of Become (30 rare), Draw (28 rare) and Double (27 rare). Every record was read with its handler to the
end and the card classes that use it; for the records with no handler, their readers: `GameQueryService`'s controller, source,
permanent, recipient and combat damage multipliers and its counter-replacement branches, `DrawService`'s position and doubling
scans, `StepTriggerService` (the monarch draw, the emblem draw), `ExploreEffectHandler`, the three connive handlers,
`LibraryChoiceHandlerService`'s search-to-graveyard copy, `GraveyardChoiceHandlerService`'s two copy completions, the two copy
mayfx handlers, `PermanentCopierService` and `LayerSystemService`'s Soul Sculptor branches. Oracle text checked with `get_card` for
every `unique` and `blocked-*` card (Opal Acrolith, Hidden Stag, Grist, Necromancy, Zygon Infiltrator, The Irencrag, Impatient
Iguana (`CMB1`), Leader, Super-Genius, Teferi, Who Slows the Sunset, Stinging Study, Rowdy Crew, Sigarda's Splendor, Indulgent
Tormentor, Divine Intervention, Mathemagics, Psychic Vortex, Bitter Feud, Topography Tracker, Doubling Cube, Bruvac, Bloodletter
of Aclazotz, Tekuthal, Unbound Flourishing) and for Mimeoplasm, Revered One, Taskmaster, Vesuvan Drifter, Sudden Salvation, Jon
Irenicus, Malignant Growth, Lightning, Army of One and Goldnight Castigator; the others were judged from their card classes.
Rulings read: Lazav, Dimir Mastermind; Curie, Emergent Intelligence. CR 205.1a, 205.1b, 406.3, 608.2h, 616.1, 707.2, 707.9b,
707.10 and 708.2a checked with `get_rule` (2026-11-23).

| Family | Triaged | `step-9` | `blocked-other` | `unique` | Left unaudited |
|---|---|---|---|---|---|
| Become | 30 rare | 24 | 3 | 3 | 0 rare, 13 in all |
| Draw | 28 rare | 19 | 3 | 6 | 0 rare, 0 in all |
| Double | 27 rare | 20 | 0 | 7 | 0 rare, 6 in all |

All families: 1,313 rare records without a row (before: 1,398). `step-9` rows waiting for fold runs: 365 (latest verdict per
class).

**The copy batches.** Eighteen Become records are copies, each with its own handler around `PermanentCopierService`. They fold onto
run 8's `BecomeCopyOfSubjectEffect` with four additions, each used by three or more records: a **copier `PermanentSubject`**
(default `source()`; run 38's lead), the **copy exceptions** as one component (name, P/T, added or replaced types, subtypes,
supertypes and keywords, an added ability; CR 707.9b; `BecomeCopyOfTargetCreatureUntilEndOfTurnEffect` and
`BecomeCopyOfTargetPermanentUntilYourNextTurnEffect` already carry them field by field), **targeting card subjects** (the record
refuses a card subject that declares a target today) and the durations `UNTIL_YOUR_NEXT_TURN`. Its permanent path must read
`copiableCard` (it reads `getCard()`), and a card subject must be able to read a card as it last existed in its zone (Lazav, Dimir
Mastermind's ruling).

**Fold batches** (survivor or shared component → records, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| copy, by parameters | `BecomeCopyOfSubjectEffect` as it is | Unstable Shapeshifter (`triggering()`, keeping this ability), Deepfathom Echo (`ChoosePermanentsEffect` + `thisWay(CHOOSE)`), Valki (`ChooseCardEffect` over `exiledWithSource()`), The Mycosynth Gardens (`target()`, once the target is declared and `copiableCard` read): 4 (4) |
| copy exceptions + card targets | `BecomeCopyOfSubjectEffect` + the copy-exceptions component | Lazav the Multifarious + Likeness Looter (one record), Mimeoplasm, Lazav Dimir Mastermind, Taskmaster, Vesuvan Drifter (`cardsThisWay(REVEAL, creature)`), the engine-built `BecomeCopyOfCardUntilEndOfTurnEffect` (its makers: the unaudited exiled-with-source head, Nascent Metamorph's blocked row, Spirit of Resilience, a token copy): 6 (6) |
| copier subject | `BecomeCopyOfSubjectEffect` + a copier `PermanentSubject` | Permeating Mass + The Flood of Mars (one record), The Animus, The Myriad Pools; with run 38's Reflection Net row and the three attached-copier records: 3 (4) |
| cost-paid copy | a permanent subject naming the permanent paid as the cost, as it last existed | Curie (+ an added ability), The Ever-Changing 'Dane (keeps its no-duplicate rule): 2 (2) |
| copy, with their makers | triage 2's SEARCH batch; Spirit of Resilience's split | `BecomeCopyOfCardInGraveyardUntilEndOfTurnEffect` (Scion), `BecomeCopyOfOneOfCardsUntilEndOfTurnEffect`: 2 (engine-built) |
| resolution colour | `BecomeChosenColorsUntilEndOfTurnEffect` (5 cards) with a `PermanentSubject`, a `Duration` and a colour spec (chosen, fixed, colorless) | Prismatic Lace + Shyft (one record), Xathrid Gorgon, Scrapbasket; the unaudited `BecomeColorlessUntilEndOfTurnEffect` (5) and Dream Coat's record join: 3 (4) |
| static chosen colour | `BecomeColorlessEffect` (9 cards) with a colour component | Alloy Golem + Puca's Eye (one record); Shimmerwilds Growth's and Shifting Sky's unaudited records join: 1 (2) |
| chosen-colour grants | `ChooseColorAtResolutionEffect` + grants reading the resolution's colour + `ClearChosenColorEffect` | Mondo Gecko; 8.29's blocked Skrelv / Sungold Sentinel row: 1 (1) |
| Become, single | existing records | Soul Sculptor (`SetCardTypesEffect` + `LosesAllAbilitiesEffect` until a creature spell is cast), Angel of Eternal Dawn (`BecomeNightEffect` with a day / night component): 2 (2) |
| draw then discard | `DrawCardEffect` + `DiscardEffect` (an earlier step) + DISCARD reads | Civilized Scholar (`HappenedThisWay.toACard(DISCARD, creature)`), Spellbound Dragon (a discarded mana value read), Occult Epiphany (`cardsThisWay(DISCARD)`), Your Nightmares Are Delicious (once the discard-down records DISCARD); two `GameData` pending fields and the inline follow-up go: 4 (4) |
| sacrificed-permanent condition | `SacrificedCardMatches` also taking a `PermanentPredicate` judged on the sacrificed snapshot | Splitting the Powerstone (as it is), Agency Coroner (suspected), Tevesh Szat, Doom of Fools (commander): 3 (3) |
| library-position draw | `DrawFromBottomOfLibraryEffect` with a position | Lantern of Undersight + River Song (one record), Heart of a Duelist: 2 (3) |
| Draw, by parameters | existing records | Distant Melody + Harmonized Crescendo (`ChooseCreatureTypeAtResolutionEffect` + a count), Black Panther + Farideh (`EventValueAtLeast`), Archivist of Gondor and the monarch draw (`activePlayer()` / `you()`), Bebop (`NoteAmountEffect` + draw + loss), Sudden Salvation, Anticausal Vestige (`CardManaValueAtMostControlledLandsPredicate`), Beck (`RegisterGlobalTriggeredAbilityEffect` this turn), Malignant Growth (`AmountThisWay(DRAW)` damage): 8 (10) |
| Draw, with a new part | an otherwise branch judged once; a target payer on `MayPayLifeEffect` (cost-shape) | Jon Irenicus, Wyll's black specialization: 2 (2) |
| source doublers | `DoubleDamageFromCreaturesEffect` with a source filter judged on permanents and spells, "any source", and a combat-to-players option | Calamity Bearer + Trance Kuja (one record), Collective Inferno, Obosh, Charging Tuskodon; Neriv and Uncivil Unrest by parameters today: 6 (7) |
| recipient doublers | `DoubleDamageToOpponentsAndTheirPermanentsEffect` with a recipient permanent subject and `you()` | Goldnight Castigator, Lightning (floated); the blocked `DoubleDamageToOpponentsEffect` and `DoubleDamageToEnchantedPlayerEffect` rows: 2 (2) |
| floated doublers | the static doublers floated with a `Duration`, one floating read in `GameQueryService` | Insult + Quest for Pure Flame (one record), Overblaze, Blind Fury (with Lightning above): 3 (4) |
| counter doublers | triage 5's counter-placement survivor with a doubling operation | Vorinclex + Innkeeper's Talent (one record), Loading Zone, Selesnya Loft Gardens, Doubling Season, Aether Refinery (player side): 5 (6) |
| counters on a set | `DoubleCountersOnTargetPermanentEffect` with `controlledBy(...)` | Ultimate Spider-Man; the blocked `DoublePlusOneCountersOnControlledCreaturesEffect` row: 1 (1) |
| power doublers | `DoubleTargetCreaturePowerEffect` (5 cards) with a `PermanentSubject` | Double Trouble + Thrakkus (one record), God-Eternal Rhonas: 2 (3) |
| draw doublers | `DoubleDrawReplacementEffect` with an except-the-first-draw-step-draw flag | Thought Reflection + Vnwxt; the unaudited `DoubleDrawExceptFirstDrawStepDrawEffect` (3): 1 (2) |

Blocked (6): Opal Acrolith + Hidden Stag (`BecomeEnchantmentEffect`: the undo of `BecomeCreatureEffect`'s card swap; a layered type
set folds it only once that animation is a timestamped floating effect), Grist (an out-of-battlefield characteristic, run 18's
redesign), Zygon Infiltrator (a duration anchored on the copied target, one card), Rowdy Crew (a shared-card-type read of the
discard, one card), Indulgent Tormentor (a two-payment punisher, step 10), `DrawCardsCost` (Psychic Vortex's cumulative upkeep
cost, step 10).

Unique (16): Become: Necromancy, The Irencrag, Impatient Iguana. Draw: Leader, Super-Genius (the connive replacement), Teferi's
emblem draw, Stinging Study (a commander choice across zones), Sigarda's Splendor (a note kept across turns), Divine Intervention,
Mathemagics (2^X). Double: Topography Tracker, Doubling Cube, Bitter Feud, Bruvac, Bloodletter of Aclazotz, Tekuthal, Unbound
Flourishing (each the only implementer of its replacement or the only reader of its object).

**Seen, not changed** (confirmed by reading, not run):

- `BecomeEnchantmentEffect` sets the permanent's card back to `getOriginalCard()`, which `CloneService`'s `applyCloneCopy` leaves as
  the copier's own card: a Clone copying Opal Acrolith that activates "{0}: becomes an enchantment" becomes a Clone again (a 0/0) and
  any later copy effect is dropped. Sure of the code; fairly sure of the consequence.
- Taskmaster, Mercenary Mimic copies a face-down target creature's card (`targetPermanent.getCard()`), not its face-down copiable
  values, and names the card in its log (`publicName` of a card) (CR 707.2, 708.2a). Sure of the code.
- `BecomeCopyOfEnteringCreatureEffect` (Unstable Shapeshifter) copies a face-down entering creature as a nameless 2/2 without the
  face-down subtypes and ward `faceDownCopiableCard` gives, and reads a face-up one's `getCard()`, not `copiableCard`;
  `BecomeCopyOfSubjectEffect`'s permanent path reads `getCard()` too (an animated or bestowed copied permanent). Sure of the code; the
  consequence for an animated land copied by Identity Thief wasn't traced.
- `DrawService` reads Heart of a Duelist's and the draw-from-bottom replacements from printed static effects: a face-down one still
  applies (CR 708.2a), the any-position scan has no lost-abilities check, and when both apply the engine always takes "any
  position" first where the drawing player chooses (CR 616.1). Sure of the code; low impact.
- Blind Fury's doubling skips Maro's Gone Nuts' factor, which every other doubler applies. Sure of the code; playtest card only.
- `BecomeCopyOfCardUntilEndOfTurnChoiceHandler`'s question path (`ofQuestionCard()`) has no maker. Dead code.
- The Mycosynth Gardens and Valki log nothing when they become a copy. Low impact.

**For the next run.**

- Triage run 7: Set (27 rare), Seek (26), Copy (26); then Deal (25), Cast (25), Allow (25), Boost (23). Conjure (39) and
  Perpetually (30) still wait for the owner. Copy's records should be judged against this run's copy batches (the copier subject and
  the copy exceptions on `BecomeCopyOfSubjectEffect`).
- Stale rows found: `PutCounterThenMayBecomeCopyOfCardsEffect` (5e, "fused (step 8)") and `EachOpponentDiscardsDownToHandSizeEffect`
  (4e, "fused … step 8") block two of this run's folds; re-triage them with their batches.
- Fold runs: the cheapest batches here need only existing records: copy by parameters, Soul Sculptor, Angel of Eternal Dawn, Draw by
  parameters, Neriv and Uncivil Unrest, the draw-then-discard splits whose discard already records DISCARD. The copy batches need the
  four `BecomeCopyOfSubjectEffect` additions first; the doublers each rewrite one `GameQueryService` reader.
- The unaudited 3+-card records these batches touch: `BecomeChosenColorsUntilEndOfTurnEffect` (5), `BecomeColorlessUntilEndOfTurnEffect`
  (5), `BecomeColorlessEffect` (9), `BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect` (3),
  `BecomeCopyOfTargetPermanentUntilYourNextTurnEffect` (3), `BecomeNightEffect` (3), `DoubleTargetCreaturePowerEffect` (5),
  `DoubleControllerDamageEffect` (4), `DoubleDamageEffect` (3), `DoubleDrawExceptFirstDrawStepDrawEffect` (3).

### Step 9, triage run 7 (2026-11-23)

Verdicts only: no Java, test or agent-docs file changed; the ratchet did not move. 81 ledger rows appended (step 9, notes
`triage 7: …`): the whole of Set (27 rare), Copy (26 rare) and Seek (26 rare), and the two stale rows triage run 6 listed
(`PutCounterThenMayBecomeCopyOfCardsEffect`, 5e; `EachOpponentDiscardsDownToHandSizeEffect`, 4e; new rows, the old ones kept). Every record was read with its handler to the end
and the card classes that use it; for the records with no handler of their own, their readers: `TurnCleanupService`'s hand-size
fold, `GameData.getMaximumLifeTotal`, `LayerSystemService`'s layer-3 and type cases with `LayerClassifier`, `CloneService` /
`ExiledCreatureCopyOnEnterService` / `MayCopyHandlerService` (the copy-on-enter and retarget completions), `CopySupport`, and the
survivors they were judged against (`SetBasePowerToughnessEffect` and `SetBasePowerToughnessToAmountEffect` with their handlers and
the `TargetPower` / `SourcePower` / `TriggeringPermanentPower` evaluators, `SetCardTypesEffect`, `GrantEffectToTargetEffect`,
`CopyTriggeringSpellEffect`, `CopyImprintedCardAndMayCastCopyEffect`, `RegisterGlobalTriggeredAbilityEffect`, `SeekLibraryEffect`,
`SeekCardsToHandEffect`). Oracle text checked with `get_card` for every `unique` and `blocked-*` card (Psychic Paper, Duplicant,
You Compleat Me (`MB2`), Mystic Barrier, Ulalek, Cerise, Darigaaz's Whelp, Hardened Bonds, Goblin Trapfinder, Thought Rattle,
Circadian Struggle, Chronicler of Worship) and for Galion, Shape Stealer, The Curse of Fenric, Brine Hag, Exuberant Wolfbear,
Hydro-Man, Midnight Oil, Winter, Sway of Illusion, The Master, Leori, Baron Helmut Zemo, Spellweaver Volute, Reversal of Fortune,
God-Eternal Kefnet, Gogo, Exterminator Magmarch, Feather, Parnesse, Hive Mind, Curse of Echoes, Trove Mage, Vinesoul Spider,
Crucias, Inquisitor Captain, Archaeomancer's Spade, Darkstar Banisher, Song of Seasons, Choice of Fortunes, Grow Old Together,
Hamza, Diviner of Fates, Plunderer's Prize and Cunning Azurescale // Divining Dive; the others were judged from their card classes.
Rulings read: Curse of Echoes (it turned the record's draft `blocked-player-loop` into a fold: each copier creates its copy in
turn, APNAP). CR 101.4, 205.1a, 205.1b, 509.1h, 603.4, 603.7b, 605.3a, 608.2b, 608.2h, 611.2c, 707.10, 708.2a and 721.3d checked with
`get_rule` (2026-11-23).

| Family | Triaged | `step-9` | `blocked-other` | `unique` | Left unaudited |
|---|---|---|---|---|---|
| Set | 27 rare | 23 | 0 | 4 | 0 rare, 11 in all |
| Copy | 26 rare | 25 | 0 | 1 | 0 rare, 8 in all |
| Seek | 26 rare | 19 | 6 | 1 | 0 rare, 4 in all |
| stale step-4/5 rows | 2 | 2 | 0 | 0 | |

All families: 1,234 rare records without a row (before: 1,313). `step-9` rows waiting for fold runs: 434 (latest verdict per
class).

**Two seek primitives.** `SeekLibraryEffect` (33 cards: count, filter, hand / battlefield / exile-with-source destination, a
`ManaValueBound`, the cloak and face-down options) raises the sought event but records no step; `SeekCardsToHandEffect` (run 24)
records SEEK but only seeks to hand. Most Seek records fold onto one of them once the survivor (`SeekLibraryEffect`, recording SEEK
as the other does) has: a GRAVEYARD destination, a top-N scope, a `ManaValueBound` relation (greater / less than, three records), a
seeker `PlayerSubject` (the exiled card's owner, two records), and SEEK for the follow-up steps (`cardsThisWay(SEEK)`). The
unaudited 3+-card `SeekEffect` (17), `SeekCardToBattlefieldEffect` (5) and `SeekLibraryToHandEffect` (4) belong to the same fold.

**Fold batches** (survivor or shared component → records, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| P/T from an amount | `SetBasePowerToughnessToAmountEffect` (8 cards) with a null side ("leave alone"), any `Duration` on `source()` and `target()`, a target path evaluated against the live source, a derived benign `targetSpec` | Exuberant Wolfbear + Galion (one record), Riptide Mangler, Shape Stealer (keeps its combat-opponent-at-trigger fallback), Eldrazi Mimic (keeps the departure-snapshot read), Halfdane, Sentinel, Wall of Tombstones, Sworn Defender: 8 (9) |
| base P/T, by parameters | `SetBasePowerToughnessEffect` as it is | Surge Engine + Tenth District Hero (`source()`, PERMANENT), Minsc + Storvald (Storvald's fixed modes; Minsc onto the amount record), Inkfathom Witch (`narrowed(allCreatures(), unblocked attacking)`), Creepy Puppeteer (`except(triggeringAttackers(), source())`): 4 (6) |
| base P/T, with a new part | a "dealt damage to the source this turn" predicate (with Giant Albatross's blocked set); triage 3's delayed-about-the-target batch | Brine Hag, Cycle of Life (split): 2 (2) |
| floated name | `SetNameEffect` with a one-shot path, a subject and a `Duration`; `Permanent.persistentName` goes | Awakening of Vitu-Ghazi + The Curse of Fenric (one record); Tenth District Hero's granted-name special cases and The Irencrag's name join: 1 (2) |
| self grants, by parameters | `GrantEffectToTargetEffect.toSource`, `SetTargetPermanentSupertypeEffect` (5) with a subject, `SetCardTypesEffect` one-shot on `source()` (with the unaudited `SetCardTypesUntilEndOfTurnEffect`, 5) | Surge Engine's can't-be-blocked, Tenth District Hero's legendary, Hydro-Man: 3 (3) |
| resolution colour (triage 6) | `BecomeChosenColorsUntilEndOfTurnEffect` with a subject, a `Duration` and a colour spec | Sway of Illusion (one chosen colour over all targets), Surge Engine's blue: 2 (2) |
| maximum hand size | one hand-size record with a `PlayerSubject` and a `DynamicAmount`, read by `TurnCleanupService` | Midnight Oil, Winter (+ `Delirium` and a difference amount); the 5m blocked `SetControllerMaximumHandSizeEffect`, `SetOpponentMaximumHandSizeEffect`, `ReduceOpponentMaxHandSizeEffect` (their blocker is this batch) and the unaudited controller reduce / increase records: 2 (2) |
| Set, single | `ConditionalEffect(EventValueAtLeast)` | Long Rest: 1 (1) |
| stale rows | triage 6's copy batch (`BecomeCopyOfSubjectEffect` over a chosen triggering card); `DiscardEffect(eachOpponent())` with a per-player amount, recording DISCARD (triage 6's draw-then-discard batch) | Spirit of Resilience (split: counter, then may copy), Kozilek, Compleated + Your Nightmares Are Delicious (one record; its `thenEffect` goes): 2 (3) |
| spell copier | `CopyTriggeringSpellEffect` with a copier `PlayerSubject`, resolving through the engine-built `CopyControllerCastSpellEffect` (kept) | Hive Mind + Curse of Echoes (one record; APNAP, optional per copier), Parnesse, `CopyControllerCastSpellEffect`: 3 (3) |
| copy per permanent | one "copy that spell, the copy targets [permanent]" step over a `PermanentSubject`, copies under a copier subject | Exterminator Magmarch (`ChoosePermanentsEffect`), Feather (choose and pay per creature, cost-shape), Ink-Treader Nephilim, Precursor Golem; the unaudited `CopySpellForEachOtherControlledCreatureEffect` (3) and `CopySpellForEachOtherPermanentOrPlayerEffect` (3): 4 (4) |
| copy on enter | `CopyCreatureCardInGraveyardOnEnterEffect` (4) with a candidate `CardSubject` and triage 6's copy exceptions; triage 4's one store for counters on exiled cards | The Master, Lazotep Convert: 2 (2) |
| copy a card, may cast it | `CopyImprintedCardAndMayCastCopyEffect.of(cards)` with card subjects outside exile, a cost reduction and an up-to-N cap | God-Eternal Kefnet, Baron Helmut Zemo, Spellweaver Volute (`ifCastThen`), Reversal of Fortune (reveal + choose): 4 (4) |
| next-time delayed | `RegisterGlobalTriggeredAbilityEffect` with a next-time-only form (`nextThisTurn`, CR 603.7b) and a manafx handler | Magus Lucea Kane, Pit Automaton, Storm King's Thunder (noted X), Repeated Reverberation, Jaya's Phoenix; the unaudited `CopyNextSpellCastThisTurnEffect` (4) and `CopyNextInstantOrSorceryCastThisTurnEffect` (21): 5 (5) |
| chosen-value binding | a choice at resolution bound into a registered trigger's or rule's filter when it is made | Leori; run 18's blocked `GrantFlashToNextSpellOfChosenSubtypeThisTurnEffect` and `GrantFreeCastForNextSpellOfChosenCardTypeEffect`: 1 (1) |
| ability copy | `CopyControllerActivatedAbilityEffect` over a snapshot of either kind; `CopyTargetActivatedOrTriggeredAbilityEffect` (10) with a copies amount and a "can't be copied" ability flag | Gogo, `CopyTriggeredAbilityFromSnapshotEffect`, `CopyControllerActivatedAbilityEffect`: 3 (1) |
| retarget markers | `CopyAbilityRetargetEffect` with an optional activated ability | `CopyTriggeredAbilityRetargetEffect`, `CopyActivatedAbilityRetargetEffect`, `CopyAbilityRetargetEffect`: 3 (engine-built) |
| seek, by parameters | `SeekLibraryEffect` / `SeekCardsToHandEffect` as they are | Spirited Simulacrum + Worldweave (one record), Wandering Treefolk, Diviner of Fates (`CardSharesCardTypeWithCardsPredicate(triggeringCards())`): 3 (4) |
| seek, survivor's additions | `SeekLibraryEffect` recording SEEK, with the additions above | Archaeomancer's Spade + Vinesoul Spider (one record; Vinesoul's is not a seek), Trove Mage + Grow Old Together (one record), Crucias, Ghitu Embercoiler, Cackling Observer, Darkstar Banisher, Karlach's blue face (+ a free-cast grant bound to `cardsThisWay(SEEK)`), Hamza (+ a manifest step), Sarevok (+ a graveyard duplicate, with Chitinous Crawler's unique row), Plunderer's Prize (+ the spell's perpetual cost on the stack and `ReturnToHandEffect`'s this-spell form), Cunning Azurescale (a secret land-or-nonland choice, with Abundant Harvest's unique row): 11 (13) |
| seek, then choose | the SEEK step + `ChooseCardEffect` over `cardsThisWay(SEEK)` + the put steps | Song of Seasons, Choice of Fortunes, Inquisitor Captain, Vhal's green face, Klement's engine-built record: 5 (4) |

Blocked (6), all the owner's hidden-zone logging question (a perpetual change to a sought card in hand): Darigaaz's Whelp, Hardened
Bonds, Goblin Trapfinder, Thought Rattle, Circadian Struggle, Chronicler of Worship. Each folds as the seek step plus the perpetual
`CardSubject` verbs over `cardsThisWay(SEEK)` once the owner decides.

Unique (6): Set: Psychic Paper (the one static chosen-name setter), Duplicant (the imprinted card's creature types), You Compleat Me
(the one maximum life total), Mystic Barrier (the left-or-right direction). Copy: Ulalek (the one stack-wide copier). Seek: Cerise and
Runecarved Obelisk (the highest-mana-value pick).

**Seen, not changed** (confirmed by reading, not run):

- Eleven seek handlers raise no sought event, so "whenever you seek" (Lurker in the Deep, Val, Vexyr) misses them: Archaeomancer's
  Spade, Trove Mage and Grow Old Together, Wandering Treefolk, Crucias, Ghitu Embercoiler, Karlach's blue face, Sarevok's
  specialization, Darkstar Banisher, Vhal's green face, Klement's specialization (Chronicler of Worship and Vinesoul Spider don't
  seek, so they are right). Sure of the code.
- Crucias's seek shuffles the library afterwards; its text has no shuffle. Sure of the code.
- Cunning Azurescale // Divining Dive is cast through `AdventureCast`; Divining Dive is an Omen, which is shuffled into its owner's
  library as it resolves (CR 721.3d), and 13 other cards use `OmenCast`. Sure of the code.
- Hydro-Man becoming a land keeps its creature types: `SetCardTypesUntilYourNextTurnEffect`'s layer case only overrides the card
  types, where `SetCardTypesEffect`'s drops subtypes of a removed card type (CR 205.1a). `SetCardTypesUntilEndOfTurnEffect`'s case
  has the same shape (its cards not checked). Sure of the code.
- Precursor Golem counts Golems by printed subtypes (`perm.getCard().getSubtypes()`): a face-down Golem counts (CR 708.2a) and a
  creature that became a Golem doesn't. Sure of the code.
- Feather, Radiant Arbiter's {2} per creature is paid only from mana already in the pool: no mana abilities can be activated for it
  (CR 605.3a). Fairly sure (no payment prompt on the path).
- Hive Mind and Curse of Echoes create the copies in seat order from the first seat, where the active player creates first and the
  others follow in turn order (Curse of Echoes ruling, CR 101.4); multiplayer only. Hive Mind's handler also has an unreachable
  optional branch. Sure of the code.
- `TriggeringPermanentPower` / `Toughness` prefer the value recorded at trigger time over the departure snapshot of a triggering
  permanent that left; CR 608.2h reads it as it last existed (Eldrazi Mimic's handler does). Fairly sure; not traced which cards
  differ.
- `CopyControllerActivatedAbilityEffect` copies neither the source snapshot nor the chosen permanent of an activated ability, which
  the target ability copiers carry (CR 707.10 copies decisions made for it). Unverified whether a card reads them.
- Vhal's green face takes its sought cards straight from the library to the battlefield and the library, where Inquisitor Captain's
  pass through the hand; which one the text wants wasn't checked (its face text wasn't read).

**For the next run.**

- Triage run 8: Deal (25 rare), Cast (25), Allow (25); then Boost (23), Specialize (19), Opponent (19). Conjure (39) and Perpetually
  (30) still wait for the owner, and so do this run's six perpetual seeks.
- The two stale rows triage run 6 listed are re-triaged (above); they unblock its Spirit of Resilience and Your Nightmares folds.
- Stale or re-triage rows these batches touch: the 5m hand-size rows (`SetControllerMaximumHandSizeEffect`,
  `SetOpponentMaximumHandSizeEffect`, `ReduceOpponentMaxHandSizeEffect`), run 18's two chosen-value rows, run 13's
  `RevealUntilChosenLandOrNonlandToHandEffect` and run 31's `ConjureDuplicateOfTargetCreatureCardIntoGraveyardEffect` (unique, now with
  a second user each).
- Fold runs: the cheapest batches need only existing records: base P/T by parameters, seek by parameters, Long Rest, the retarget
  markers. The seek step (one primitive recording SEEK) unlocks 16 records and the six blocked ones later; the P/T-from-amount batch
  needs the null side and the live-source target path first.
- The unaudited 3+-card records these batches touch: `SeekEffect` (17), `SeekCardToBattlefieldEffect` (5), `SeekLibraryToHandEffect`
  (4), `CopyNextInstantOrSorceryCastThisTurnEffect` (21), `CopyNextSpellCastThisTurnEffect` (4), `CopyTargetActivatedOrTriggeredAbilityEffect`
  (10), `CopyTargetTriggeredAbilityEffect` (4), `CopyCreatureCardInGraveyardOnEnterEffect` (4), `CopySpellForEachOtherControlledCreatureEffect`
  (3), `CopySpellForEachOtherPermanentOrPlayerEffect` (3), `SetTargetPermanentSupertypeEffect` (5), `SetCardTypesUntilEndOfTurnEffect`
  (5).

### Step 9, parallel triage chunk 02 (2026-11-23)

Verdicts only: no Java, test, agent-docs or repo file changed; the ratchet did not move. 79 ledger rows (notes `triage P02: …`), read against `db7a565bf1`: the rare Boost (23), Opponent (19), Specialize (19) and Attach (18) records of the chunk. Every record was read with its handler to the end and the card classes that use it. For the records with no handler, their readers were read: `GameQueryService`'s damage-bonus, discard, life-gain and entering-trigger reads; `DrawService`'s Notion Thief / Hullbreacher / Alms Collector scans; `TurnProgressionService`'s extra-turn skip; `LibrarySearchSupport`'s Mindcensor read; `CombatAttackService`; `CumulativeUpkeepEffectHandler`; `ForcedCostOrElseEffectHandler`; `MayPenaltyChoiceHandlerService`'s Distant Memories branch; `LibraryChoiceHandlerService`'s opponent-exile follow-up. Also read: Coin of Fate's interaction handler, the static boost support and Self handlers, `AmountEvaluationService`'s colour-count amounts, `CreatureControlService` and `AuraAttachmentService`'s reconcile.

Oracle text was checked with `get_card` for every `unique` and `blocked-*` card: Archangel of Strife, Crown of Convergence, Inferno of the Star Mounts, War's Toll, Alms Collector, Tamiyo, Collector of Tales, Tainted Remedy, Plague Drone, Aven Mindcensor, Dark Salvation, Varchild's War-Riders and Wall of Shards. Chatzuk's MB2 playtest printing is in no feed, so its card class's registered Oracle was used. Oracle was also checked for Jurin, Phyresis Outbreak, Haunted One, Crippling Fear, Takeno, Champions of Minas Tirith, Gilgamesh, Armored Skyhunter, Thorin and Grip of Phyresis. Rulings read: Inferno of the Star Mounts, Infernal Denizen. CR 101.4, 115.3, 301.5c, 301.6, 303.4f, 303.4g, 400.7, 603.12, 608.2d, 613.7e, 701.3a, 701.3b, 702.24a and 708.2a were checked with `get_rule` (2026-11-23).

| Family | Triaged | `step-9` | `blocked-other` | `unique` |
|---|---|---|---|---|
| Boost | 23 rare | 18 | 2 | 3 |
| Opponent | 19 rare | 12 | 2 | 5 |
| Specialize | 19 rare | 19 | 0 | 0 |
| Attach | 18 rare | 18 | 0 | 0 |
| All | 79 | 67 | 4 | 8 |

**Fold batches** (survivor or shared component → records, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| per-permanent static boost | `DynamicStaticBoostEffect` with `amountsFromTarget` and an amount of the boosted permanent (its attacks, bushido, creature types capped by `Min`, colour count, own chroma; Heroes' Podium's is the existing `PermanentCount(…, excludeSource)`); the Self handlers and the legendary support class go | `BoostByAttackCountEffect` (2), `BoostByBushidoEffect`, `BoostLegendaryCreaturesByOtherLegendaryCreaturesEffect` (2), `BoostNonHumanCreaturesByCreatureTypeCountEffect`, `BoostOtherMulticoloredCreaturesByColorCountEffect`, `BoostOwnCreaturesByManaSymbolEffect`: 6 (8). Bushido's, chroma's and creature-type's amounts are one card each: weigh them in the fold run |
| attached colour count | `AttachedBoostEffect(AttachedPermanentColorCount(), …, attachedCreature())`, Civic Saber's form, as it is | Blessing of the Nephilim: 1 (1) |
| chosen type at resolution | `ChooseCreatureTypeAtResolutionEffect` + `PermanentHasTypeChosenThisResolutionPredicate` in `BoostAllCreaturesEffect`'s filter / `PermanentCount` (Raise the Palisade, Pact of the Serpent) | Crippling Fear (keep `BoardWipeEffect.sweepsBoard` for the AI), Pack's Disdain: 2 (2); the other `…PerChosenTypeCount…` records can follow |
| set boost | `BoostTargetCreatureEffect` taking a non-targeting set subject, amounts optionally read against each boosted creature (step 10's boost hand-off names the same property), keywords by `GrantKeywordEffect` over the same subject | Haunted One, Phyresis Outbreak, Jurin: 3 (3) |
| floated damage bonus | `BoostControllerDamageThisTurnEffect` + a source filter (colour), a noncombat-only option and an amount locked at resolution; one floating read in `GameQueryService` | the survivor, `BoostColorSourceDamageThisTurnEffect`, `BoostControllerNoncombatDamageThisTurnEffect`: 3 (3); kin of triage 6's floated doublers |
| if-instead counter | `ConditionalEffect(cond, PutCountersOnSourceEffect)` + `ConditionalEffect(NotCondition(cond), BoostSelfEffect)` | Jetmir's Fixer (`TreasureManaSpentToActivate`, as it is), Paragon of Modernity (needs a colours-spent-to-activate read): 2 (2) |
| target groups | three groups with `BoostTargetCreatureEffect.forTargetGroup`, plus a distinct-across-groups constraint for "other" (CR 115.3 allows the same object once per "target") | Arm the Cathars: 1 (1) |
| attack ban between players | `CreaturesCantAttackControllerUntilNextTurnEffect` with attacker and protected `PlayerSubject`s, planeswalker breadth and a `Duration` | Champions of Minas Tirith (cost-shape): 1 (1); joins the unique rows `ControllerCantAttackTargetPlayerThisTurnEffect` (Call for Aid) and `OpponentCantAttackSourceControllerThisTurnEffect` (Sandswirl Wanderglyph), and the unaudited survivor (Chronomantic Escape) |
| opponent-chosen card | `ChoosePlayerEffect` (an opponent) + `ChooseCardEffect` with chooser `playersThisWay(CHOOSE)` / `defendingPlayer()` / `targetPlayerDeclaredElsewhere()` + the card movers; `topOfGraveyard` needs a count | Tasigur + O-Kagachi (one record), Phyrexian Grimoire, Coin of Fate: 3 (4) |
| opponent-chosen permanent | `ChoosePlayerEffect` + `ChoosePermanentsEffect.chosenBy(playersThisWay(CHOOSE))` + the verb over `thisWay(CHOOSE)` (exile tracked with source, sacrifice, `GainControlOfTargetEffect.gainedBy`) | Wormfang Crab, Demonic Hordes (cost-shape), Infernal Denizen (cost-shape): 3 (3); the two `ForcedCostOrElseEffect` fallbacks need `DestructionSupport`'s per-class list to run the composition |
| extra-draw replacement | `OpponentExtraDrawsRedirectedEffect` + a payload (draw a card / create a Treasure) | Notion Thief, Hullbreacher: 2 (2); shares `markFirstDrawStepDraw` with triage 6's draw doublers |
| extra-turn skip | `SkipAllExtraTurnsReplacementEffect` (Gerrard's Hourglass Pendant) + a `PlayerSubject` | Stranglehold + Trouble in Pairs (one record): 1 (2) |
| entering triggers | `CreatureEnteringDontCauseTriggersEffect` (4, unaudited) + an entering filter + a whose-abilities `PlayerSubject` | Elesh Norn: 1 (1); `ArtifactOrCreatureEnteringDontCauseTriggersEffect` joins |
| with its maker | `DistantMemoriesEffect` (unaudited): its exile step + an any-opponent choice + put into hand / else draw three | `OpponentMayReturnExiledCardOrDrawEffect`: 1 (engine-built), then dead |
| specialize | one `SpecializeEffect(color)` and one handler. All 19 handlers share one skeleton: name check, runtime copy, `clearRulesTextAndAbilities`, face characteristics, `exchangeCard`, the face's "when this specializes" ability through the stack door. Each card class supplies its face (characteristics, abilities, target group, trigger) through a card-side hook; the face builders now in the Vhal, Karlach, Alora, … handlers move to the cards. Karlach's graveyard return stays a made ability of its card, not a parameter | all 19 `Specialize…Effect`: 19 (19); `UnspecializeLukaminaEffect` joins |
| attach verb, Equipment | `AttachSourceEquipmentToTargetCreatureEffect` with its subject guard (`isNamedOne`) taking set and step-result subjects (`returnedThisResolution()`, `createdThisResolution()`, the put permanent) | Balan + Vulshok Battlemaster (one record), The Invincible Iron Man, Pre-War Formalwear, Grip of Phyresis, Thorin: 5 (6) |
| attach verb, Auras and player hosts | the same verb generalised to any attachment (Aura legality by `AuraAttachmentService.canEnchant[Player]`, Fortification by CR 301.6), player hosts, `thisWay(CHOOSE)` hosts, with the control recompute | Glamer Spinners, Heavenly Blademaster, Lynde, Kudzu + Steam Vines, Maddening Hex, C.A.M.P. + Darksteel Garrison, Codsworth + Halvar, Simic Guildmage: 8 (11). The blocked rows `AttachSourceAuraToTargetCreatureEffect`, `AttachTargetAuraToTargetCreatureEffect` and `AttachSourceAuraToTargetPlayerEffect` wait for it (three or more: build it); `AttachTargetAuraToAnotherPermanentOfSameTypeEffect` is Simic's sibling |
| put then attach | the library put's step result + `MayEffect(ChoosePermanentsEffect` + the attach verb`)`; Gilgamesh's as a reflexive ability (`QueueReflexiveAbilityEffect`) | Armored Skyhunter, Palico (same text), Gilgamesh: 3 (3) |
| Aura host as it enters | the put step's entry batch (CR 303.4f, 303.4g) | `AttachSelectedAurasToLegalTargetsEffect` + `…FollowUp` (Knickknack Ouphe): 2 (1), both then dead |

Blocked (4):
- Chatzuk: counting a subject's permanents needs subjects inside `DynamicAmount`, which is outside this plan.
- Dark Salvation: an amount read against another target group, the same missing part as Orim's Thunder's row (2 classes).
- `OpponentCreatesTokensCost` and `OpponentGainsLifeCost`: cumulative-upkeep cost heads for Varchild's War-Riders and Wall of Shards. Step 10, as `DrawCardsCost`.

Unique (8):
- Boost: Archangel of Strife and Two Streams Facility (players' mode choices), Crown of Convergence, Inferno of the Star Mounts.
- Opponent: War's Toll, Alms Collector, Tamiyo's discard prohibition (a discard component would be hers alone beside the 3-card sacrifice record), Tainted Remedy + Plague Drone, Aven Mindcensor.

**Seen, not changed** (confirmed by reading, not run):

- Notion Thief, Hullbreacher and Alms Collector (`DrawService`) and Elesh Norn (`areOpponentPermanentETBTriggersSuppressed`) look at one opponent's battlefield (`getOpponentId`). Distant Memories asks only the first opponent by seat for "any opponent may" and finds its controller as "the other player". Infernal Denizen offers the first opponent by seat. All are right with two players only. Sure of the code.
- Knickknack Ouphe's Auras are attached after they enter, by a follow-up, and one with nothing to enchant enters and is left unattached for the state-based check. It should be attached as it enters or stay in the library (CR 303.4f, 303.4g). Sure of the follow-up handler; the put step itself wasn't read.
- Gilgamesh's "When you put one or more Equipment onto the battlefield this way" is a reflexive ability (CR 603.12). The engine asks it inline as a library follow-up, with no stack object. Fairly sure.
- Moving a control-changing Aura with Halvar / Codsworth, Heavenly Blademaster or Kudzu / Steam Vines doesn't recompute control. Simic Guildmage's handler does; Glamer Spinners' completion does so only for the new host. Sure of the code; whether a later reconcile repairs it wasn't traced.
- `AttachSourceFortificationToTargetLandEffectHandler` gives a new timestamp and a log line to a Fortification already attached to the target (CR 701.3b: the effect does nothing). Sure, low impact.
- Karlach's specialize-from-graveyard return enters through `putPermanentOntoBattlefield`, not the entry batch. Its "perpetually can't block" is a static effect on a runtime copy, not the perpetual store. Sure of the code; low impact.
- `AttachTargetEquipmentToCreatedPermanentEffect.useSourceEquipment` has no maker. Dead component.

**Left.** Nothing in the chunk. Suggested fold-run order:
1. The by-parameter rows: Blessing, Heroes' Podium, Crippling Fear, Pack's Disdain, Jetmir's Fixer, Pre-War Formalwear, Grip, Thorin.
2. The specialize batch (one card-side hook).
3. The attach verb (three blocked rows wait for it).
4. The opponent-choice batches.

### Step 9, parallel triage chunk 04 (2026-11-23)

Verdicts only: no repo file changed. 81 ledger rows (notes `triage P04: …`) for chunk 04's Replace (17 rare), Additional (16),
AnyOpponent (7), AnyPlayer (9), Counter (16) and Flip (16) records.

Every record was read with its handler to the end, and every card class that uses it was read too. For the records with no handler,
their readers were read: `StaticLandManaReplacements`, `TurnScopedLandManaReplacements`, `ManaProductionSupport` and `TapTriggers`;
`CombatDamageService`'s mill replacement; `CounterSupport` and `GameQueryService.controlsCounterExileReplacement`; the five
`LossReplacer`s and `GameOutcomeService.resolveLoss`; `TokenKindReplacements` and `TokenCreationReplacementSupport`;
`GameQueryService`'s damage-replacement and damage-bonus readers, and `DamagePreventionService`'s creature-damage pool steps;
`MillReplacements`, `VentureIntoDungeonEffectHandler`, `SurveilEffectHandler` and `CastingCostService`'s mana-symbol tax;
`DrawService` and `MayMiscHandlerService`; the mayfx halves of the punisher records and `WhirlwindDenialSupport`;
`CumulativeUpkeepEffectHandler` and `ForcedCostOrElseEffectHandler`; `FlipUntilLoseOrStopSupport` and `CoinFlipService.CoinFlipResult`.

Oracle text was checked with `get_card` for every `unique` and `blocked-*` card, and also for Breeches, Impulsive Maneuvers, Mutalith
Vortex Beast, Gitrog, Research // Development, Barbarian Bully, Innocent Traveler, Kadena's Silencer, Mirrored Depths, Rakdos and Pokey,
the Scallywagg. The other cards were judged from their card classes. Rulings read: Desecration Demon, Dash Hopes, Brain Gorgers,
Phantasmagorian, Reservoir Kraken. CR 101.4, 603.12, 613.1f, 616.1, 705.1, 705.2, 705.3 and 708.2a were checked with `get_rule`.

| Family | Triaged | `step-9` | `blocked-other` | `unique` |
|---|---|---|---|---|
| Replace | 17 | 9 | 0 | 8 |
| Additional | 16 | 11 | 0 | 5 |
| AnyOpponent | 7 | 7 | 0 | 0 |
| AnyPlayer | 9 | 9 | 0 | 0 |
| Counter | 16 | 4 | 8 | 4 |
| Flip | 16 | 10 | 1 | 5 |
| All | 81 | 50 | 9 | 22 |

**Fold batches** (survivor or shared component → records of this chunk, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| punisher (cost-shape) | `MayPayManaEffect`'s any-player payer loop (`MayPayPayer.ANY_PLAYER` / `ANY_OPPONENT`, with its `elseEffect` for "if no one does"), taking a payment other than mana: sacrifice N matching (nontoken; collected or at once), discard N, pay life (a `DynamicAmount`), tap an untapped creature, be dealt N damage by the source, or "have you draw". The reward happens once if anyone paid, or at once on the first payer. The PLC counter cycle's ruling: "as soon as any player performs the action, the spell is countered, but the remaining players still get the option". | All 7 AnyOpponent records (Development, Innocent Traveler, Desecration Demon, Clackbridge Troll, Gitrog, Vexing Devil + Longhorn Firebeast, Reservoir Kraken) and all 9 AnyPlayer records (Phantasmagorian, Dash Hopes + Temporal Extortion, Brain Gorgers, Prowling Pangolin, Argothian Wurm + Shivan Wumpus, Barbarian Bully, Breaking Point, Book Burning, Browbeat): 16 (19). The five private `apnapPlayers` copies and `apnapOpponents` go. Weigh against step 10's cost-combinator merge. |
| additional damage | `AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect` (5 cards) gains a source filter judged on permanents and spells (colour, red-or-artifact, noncreature, subtypes, spell, "another"), a source-controller scope (you / any), a recipient filter (any, players, opponents, opponents and their permanents, creature-battle-or-opponent, players and battles) and a `Duration` for the floated form; one `DamageBonus` reader. | Embermaw Hellion + Jaya; Spiked Corridor (already expressible by parameters); Sulfuric Vapors; Disciples of the Inferno (Invasion of Regatha's back face); Rem Karolus; Mechanized Warfare; Rankle and Torbran (two records: the floater and the engine-built static); Valley Flamecaller: 9 (9). Re-triage with this batch: `AdditionalControllerDamageEffect` (3 cards, unaudited), the 5m unique rows of Torbran's and Tok-Tok's records, and 8.29's unique row for the survivor. Same design as triage 6's source doublers. |
| land mana | `ReplaceLandManaWithColorEffect` (3 cards) gains a land `PermanentPredicate` judged against the source controller, a from-colour set, a to-spec (a fixed colour, or "a colour of your choice") and, in the floated form, a subject and a `Duration`. | Pulse of Llanowar, Quarum Trench Gnomes: 2 (2). The unaudited floated land-mana records join (Hall of Gemstone, Harvest Mage, Chaos Moon, Pale Moon, Deep Water). |
| loss replacement | `ReplaceControllerLossWithExileAndLifeTotalEffect` gains a starting-life amount; one replacer keeps Exquisite Archangel's CR 616.1 choice among several sources. | The Golden Throne, Exquisite Archangel: 2 (2) |
| token kind | `ReplaceTokenSubtypeCreationEffect`: its subtype becomes a token filter (creature token or subtype). | Fisher's Talent, Divine Visitation: 2 (2) |
| damage threshold | `ReplaceDamageAboveThresholdEffect`, static or floated with a `Duration`. | Divine Presence, Equal Treatment: 2 (2) |
| damage to counters | `PreventDamageAndAddPlusCountersEffect` (2 cards) gains a `CounterType` and a replacement-or-prevention kind. | Lichenthrope: 1 (1); with the unique-rowed `PreventDamageAndAddMinusCountersEffect` (Phyrexian Hydra) |
| extra upkeep | `AdditionalUpkeepStepEffect` (2 cards) gains a count and a placement (after this step / after this phase); the two `GameData` counters become one store. | Paradox Haze + The Ninth Doctor, Obeka: 2 (3) |
| counter each target | `CounterSpellEffect` counters every target of its group and records COUNTER_SPELL over all of them. | Double Negative + Katara's Reversal: 1 (2) |
| counter a set | `CounterMatchingSpellsEffect` (3 cards) admits abilities through its filter; a following `CreateTokenEffect` reads the countered count. | Kadena's Silencer, Glen Elendra's Answer: 2 (2) |
| poison-gated counter | `ConditionalEffect(TargetSpellMatches(a new "controller has at least N poison counters" stack-entry predicate), CounterSpellEffect)` | Corrupted Resolve + Bring the Ending: 1 (2) |
| coin flip, by parameters | `FlipCoinWinEffect` (42 cards): `forTriggeringSpellController(null, CounterSpellEffect)`; reflexive branches (`QueueReflexiveAbilityEffect`, CR 603.12) that carry the triggering spell. | Mirrored Depths, Breeches: 2 (2) |
| per-object flips | `FlipCoinWinEffect` gains a `forEach` subject (players or permanents) whose branches read "that player / that creature"; the per-index checkpoint resume is kept. Rakdos's tails set is recorded as a step result so one `DestroyAllPermanentsEffect` destroys them together. | Mutalith Vortex Beast, Fighting Chance, Rakdos: 3 (3) |
| multi-flip | `FlipUntilLoseEffect` (3 cards) gains a count, a stop-at-loss flag, a per-loss effect, an all-wins effect and a record of its wins. | Squee's Revenge, Yusri, Crazed Firecat: 3 (3); with the unique-rowed `FlipCoinsPerHeadsEffect` |
| next-damage shields | `PreventNextDamageByTargetCreatureEffect` (2 cards) gains a source `PermanentSubject`, a doubling mode and a combat-only flag, used under `FlipCoinWinEffect`. Desperate Gambit also needs a choose-a-damage-source step that records CHOOSE (permanents and stack objects you control); the chosen-source redirect and reflect records could share that step. | Impulsive Maneuvers, Desperate Gambit: 2 (2); with the unique-rowed `PreventNextDamageBySelfEffect` |

**Blocked (9):** the seven counter-unless ransom records (Auntie Ool, Axebane Ferox, Perplex, Titania, Grip of Amnesia, Molten
Influence, The Unagi of Kyoshi Island) and Whirlwind Denial's for-each ransom wait for step 10's option C, the owner's open
counter-unless question. `FlipCoinsCost` (Karplusan Minotaur's cumulative upkeep, engine-built) is a forced cost of
`ForcedCostOrElseEffect` and waits for step 10's cost-combinator merge.

**Unique (22):** Replace: False Dawn (it covers every mana ability a player controls), Damping Sphere (its replacement depends on the
amount of mana), Undead Alchemist + Szadek (already one record), Guile, Liliana's Other Contract, Lich's Mirror, Forethought Amulet, and
the engine-built prompt carrier for "may replace this draw". Additional: Hama Pashar (dungeon rooms), The Water Crystal, Drought,
Twinning Staff, Enhanced Surveillance. Counter: Rasputin's counter cap, Bazaar of Wonders, Grimoire Thief (turning the exiled cards face
up), Ice Cave (its only own part is paying "that spell's mana cost"). Flip: Crooked Scales (the repeat loop), Molten Sentry, Two-Headed
Giant, Fiery Gambit and its engine-built continuation.

**Seen, not changed** (confirmed by reading, not run):
- Loss replacements apply in `LossReplacer` bean order (`GameOutcomeService.resolveLoss`). When different ones apply together (The
  Golden Throne, Exquisite Archangel, Lich's Mirror, Liliana's Other Contract), the losing player doesn't choose which (CR 616.1). Only
  several Archangels get a choice. Sure of the code.
- Coin-flip events (CR 705.2): Fiery Gambit (`FlipUntilLoseOrStopSupport.flip`) fires neither the won nor the lost event; Squee's
  Revenge (`FlipCoinsUntilLoseEffect`) and `FlipUntilLoseEffect` (Mirror March, Okaun, Zndrsplt) fire no lost event for the losing flip,
  so Karplusan Minotaur's "whenever you lose a coin flip" misses them. Sure of the code.
- Mutalith Vortex Beast (`FlipCoinForEachOpponentEffect`) fires the won/lost events for a Pokey, the Scallywagg d20 result, although
  Pokey's text says the roll "counts as rolling a die"; every other flip handler gates these events on `isActualCoinFlip`. Sure of the
  code.
- Breeches sends a Pokey d20 heads to its lose branch (the damage), where `FlipCoinWinEffect` treats heads as a win. Sure of the code;
  which is right is unclear under CR 705.2, since no call is made.
- Sulfuric Vapors (`getSpellDamageBonuses`) and Disciples of the Inferno (`getNoncreatureSourceDamageBonuses`) apply with no
  lost-abilities or face-down check (CR 613.1f, 708.2a); Sulfuric Vapors also reads the spell's card colours, not
  `spellAsEffectsSeeIt`. Enhanced Surveillance's reader has no lost-abilities check either. Sure of the code; low impact.
- The "any opponent may sacrifice" handlers disagree on timing: Desecration Demon, Clackbridge Troll, Gitrog and Argothian Wurm
  sacrifice each creature as it is chosen; Innocent Traveler and Brain Gorgers collect the creatures and sacrifice them together
  (CR 101.4). The PLC cycle's ruling supports acting at once for the counter cards; for the combat punishers it isn't settled.
- Molten Sentry and Rakdos log "wins / loses the coin flip" for heads-or-tails flips that nobody wins (CR 705.2). Log only.
- Forethought Amulet replaces damage only from instant and sorcery spells, not from abilities of instant or sorcery sources (kin of
  Pestilent Spirit's open defect). Low impact.

**Anything left.** Nothing in this chunk. The batches reach records outside it that are unaudited or were ruled unique earlier: the
floated land-mana records and `AdditionalControllerDamageEffect` (3 cards); the unique damage-bonus rows from 5m and 8.29;
`PreventDamageAndAddMinusCountersEffect`, `PreventNextDamageBySelfEffect` and `FlipCoinsPerHeadsEffect` (unique rows); the chosen-source
redirect records.

### Step 9, parallel triage chunk 01 (2026-11-23)

Only verdicts this time. No repo file changed; the plan and ledger were read at `db7a565bf1`, and the ratchet did not move. The 75 ledger rows (notes `triage P01: …`) are in `/tmp/claude-1000/-mnt-ramdisk-magical-vibes/300dbf19-8a01-4cdb-8671-d50e7ccdb4f0/scratchpad/ptriage/out-01/ledger-rows.csv`.

**What was read**
- Every record, with its handler read to the end, and every card class that uses it.
- For records with no handler, their readers:
  - `CastingPermissionService`: the ice, croak, void, planar, library-top, pay-life and foretold branches
  - `CastingCostService.findFreeCastSource`
  - `GameQueryService`'s boast, exhaust, power-up and loyalty readers
  - `AbilityActivationService`'s loyalty count
  - `HandCastCardCharacteristicsService`
  - `GameViewProjectionFactory`'s library-top look
- For engine-built records, their builders and the may handlers they route to.
- Oracle text: checked with `get_card` for every card. The Colossal Dreadmaw and Comet, Stellar Pup were checked from the text their classes register.
- Rulings: Urianger Augurelt, Kayla's Music Box, Chitinous Crawler (it has none), Planetarium of Wan Shi Tong, Wicked Guardian.
- Rules: CR 400.7, 608.2g, 608.2h, 611.2c, 702.16b, 702.62a, 708.2a and 709.3a, checked with `get_rule`.

| Family | Triaged | step-9 | blocked-other | blocked-step-model | unique |
|---|---|---|---|---|---|
| Allow | 25 rare | 16 | 1 | 0 | 8 |
| Cast | 25 rare | 18 | 0 | 1 | 6 |
| Deal | 25 rare | 21 | 0 | 0 | 4 |

Two `step-9` rows are dead (`AllowPlayAndCastCardsExiledWithVoidCountersEffect`, `DealDamageToControllerIfSacrificedCardMatchesEffect`). One is a survivor whose reader changes: `AllowExtraLoyaltyActivationEffect`.

**Permissions are rules of the game (CR 611.2c).** A resolution's "you may play" changes no object's characteristics. So it also covers cards that join the set later in its duration:
- Kayla's Music Box and Urianger: cards exiled with them later that turn.
- Yawgmoth's Testament: face-up cards exiled later.
- Singing Towers: cards foretold later.

`AllowPlayCardsFromExileEffect` fixes its card set when it resolves. That is right for "those cards" and wrong for "cards exiled with this". The exiled-with-source fold therefore needs an option that keeps the set open, as `grantExilePlayUntilEndOfTurnBySource` does today.

**Casting several spells during a resolution (CR 608.2g): the caster picks the order.** Two mechanisms exist:
1. A fixed queue of one `PendingMayAbility` per card. A declined card is gone.
2. `ImprovisationCapstoneCastChoice`: pick the cards, then cast them in the chosen order. About 15 handlers use it.

The free-cast choice batch moves the "cast any number / up to N from among" records onto the second mechanism, over a `CardSubject`.

**Fold batches** (records / cards)

- **player-floated** — survivor: `GrantStaticEffectToPlayerEffect.untilEndOfTurn(static)`, with the reader asking for the player's floating copy. 4 records / 5 cards.
  - Chandra, Flame's Catalyst: Omniscience's `AlternativeCostForSpellsEffect`, which is already read as a floating effect. By parameters.
  - Urza Assembles the Titans: `AllowExtraLoyaltyActivationEffect`, which is the survivor row; Oath of Teferi and Urza, Planeswalker use it.
  - Yawgmoth's Testament: the static exile permission over `exiledCards(you(), face up)`.

- **library-top** — survivor: `AllowCastFromTopOfLibraryEffect` (44 cards) with `PlayLandsFromTopOfLibraryEffect`. They gain an alternative cost (life equal to mana value, `PayLifeEqualToSpellManaValueCost`), a library-owner `PlayerSubject`, and a face-up plane read as its controller's; the player-floated grant floats them. 3 records / 3 cards.
  - Bolas's Citadel, Windriddle Palaces, The Belligerent.
  - Gwenom's (run 25) and Xanathar's (run 18) unique rows join; re-triage them.

- **exiled-counter permissions** — survivor: one static play-from-exile permission over `exiledCards` with a counter kind, reading triage 4's single counter store. 2 records / 2 cards, plus the dead void record.
  - Draugr Necromancer (ice), Grolnok (croak).
  - Run 25's blocked dream and hatching rows wait for the same subject.

- **graveyard grants** — survivor: `AllowCastMatchingCardsFromGraveyardThisTurnEffect` (6 cards, unaudited) with a graveyard `PlayerSubject`. 3 records / 3 cards.
  - The Great Work, Whispersteel Dagger, Shaman's Trance.
  - Shaman's Trance merges `GraveyardPlayFilterPermission` into `GraveyardCastFilterPermission`. Only it sets the lands-too option.

- **source-card graveyard permission** — survivor: `AllowCastTargetCardFromGraveyardThisTurnEffect.of(sourceCard(), …)` with a `Duration` and an as-Adventure option. 1 record / 2 cards.
  - Hildibrand Manderville and Mosswood Dreadknight share one record.
  - 5m's blocked `AllowCastSourceCardFromGraveyardThisTurnEffect` can join now.

- **exiled with source** — survivor: `AllowPlayCardsFromExileEffect.thisTurn(exiledWithSource())` with an open-set option and a spell cost reduction. 1 record / 2 cards.
  - Kayla's Music Box and Urianger Augurelt.
  - The unaudited `AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect` (4 cards) is the same permission.

- **exiled as cost** — 1 record / 2 cards.
  - Primordial Mist: `AllowPlayCardsFromExileEffect.thisTurn(exiledAsCost())`.
  - Chitinous Crawler: the play-now offer `MayCastExiledCardWithNormalCostEffect` over `exiledAsCost()`.

- **free-cast choice** — survivor: one cast-now record over a `CardSubject`, through `ImprovisationCapstoneCastChoice` (max count, total mana-value cap). 5 records / 5 cards.
  - Izzet Chemister and Forger's Foundry (one record), Ashiok, Rod of Absorption, Invoke Calamity and its engine-built `CastInvokeCalamityChosenSpellEffect`.
  - Invoke Calamity keeps its split-card half rule (CR 709.3a).
  - Run 4's blocked `ExileTopCardOfEachLibraryAndMayCastSpellsEffect` waits for this.

- **hand free-cast count** — survivor: `MayCastAnySpellFromHandWithoutPayingManaCostEffect` (31 cards) with a count. 2 records / 3 cards.
  - Aetherflux Conduit and The Great Synthesis (one record), Power Without Equal.
  - Run 27's `MayCastAnyNumberOfSpellsFromHandWithoutPayingManaCostEffect` marker goes.

- **graveyard-permission statics** — survivor: `CastSpellsFromGraveyardEffect` / `CastSpellFromGraveyardOncePerYourTurnEffect` with an alternative-cost component. 4 records / 4 cards.
  - Abandoned Sarcophagus (as it is), Raul (a milled-this-turn predicate), Eye of Duskmantle (pay life), Ninja Teen (sneak).

- **graveyard cast-now** — survivor: `CastCardFromGraveyardEffect` over a non-target `CardSubject`, with an any-number option. 3 records / 3 cards.
  - Oskar, Thunderblade Charge (cost-shape), Chandra Ablaze.
  - 5m's Sproutback Trudge row joins.

- **targeted graveyard cast-now** — survivor: `CastCardFromGraveyardEffect` over `targetCard(filter, scope)`. 2 records / 2 cards.
  - Conduit of Worlds: behind a no-spell-cast-this-turn condition, with a "can't cast spells this turn" restriction after a successful cast.
  - Vadrok.
  - 5m's Tinybones row joins.

- **exile free-cast markers** — survivor: `MayPlayExiledCardWithoutPayingManaCostEffect` with suspend's haste and the sorcery-timing lock. 1 record / 0 cards: `CastExiledCardWithoutPayingManaCostEffect`, which is engine-built.

- **look and cast** — survivor: triage 4's `LookAtTopCardsAndMayCastEffect` with a "card stays on top" destination. 1 record / 2 cards: Galvanoth and Planetarium of Wan Shi Tong.

- **per-player damage** — survivor: `DealDamageToPlayersEffect`, with its amount evaluated per player for `eachOpponent()` too, and all amounts fixed before any damage (CR 608.2h). 3 records / 3 cards: Molten Psyche, Stormbreath Dragon, Heartless Hidetsugu.

- **Deal, by parameters** — existing records. 6 records / 7 cards: Disorder, Burn the Impure, Flames of the Raze-Boar, Vial Smasher, Roar of the Crowd and Coordinated Barrage (one record), Wicked Guardian (choose, then damage, with the draw gated on the choice).

- **Deal, with a new predicate** — 2 records / 2 cards.
  - Outpace Oblivion: a below-max-speed player predicate.
  - Searing Rays: triage 6's resolution-colour predicate.

- **hand-reveal damage** — `RevealCardsFromHandEffect` plus `DealDamageToPlayersEffect` over a REVEAL count matching the chosen quality. 2 records / 3 cards.
  - Blood Oath needs a card-type chooser at resolution.
  - Darigaaz and Inquisition share one record; Darigaaz is cost-shape.

- **least toughness** — `ChoosePermanentsEffect` with a least-toughness predicate, then damage over `thisWay(CHOOSE)`. 1 record / 1 card: Purging Scythe. The unaudited `SacrificeCreatureWithLeastToughnessEffect` joins.

- **delayed about the target** — triage 3's `aboutAtNext` over the entry's target. 1 record / 2 cards: Dwarven Sea Clan and Sawtooth Ogre.

- **random target** — `DealDamageToAnyTargetEffect` with a target chosen at random. 1 record / 1 card: Goblin Test Pilot. Run 9's blocked Knight Rampager row joins.

- **haunted** — triage 5's "is haunted" predicate. 1 record / 1 card: Kaya's damage emblem, as `DealDamageToPlayersEffect(3, ownerOf(target()))`.

- **single** — 3 records / 4 cards.
  - Firesong and Sunspeaker and Comet (one record): onto `DealDamageToAnyTargetEffect` with a creature-or-player target.
  - Tephraderm: split into its two Oracle abilities.
  - Dwarven Catapult: `Divided` with a dynamic divisor. Only this card sets it, so the fold run should weigh it.

- **dead** — delete. 2 records / 0 cards.

**Blocked (2)**
- The Matrix of Time: `blocked-other`. It plays cards linked to a face-up plane and waits with run 5's planar exile row.
- `CastTopCardFromLibraryForManaEffect`: `blocked-step-model`. It goes with run 13's blocked reveal record.

**Unique (18)**
- Allow: Singing Towers, Chittering Illuminator and Pearl Lake Warden, Birgi, Elvish Refueler, Wonder Man, Wish and Frontier Explorer, Biomancer's Familiar, Autumn Willow.
- Cast: Spawnsire, The Colossal Dreadmaw, Illusionary Mask, Panglacial Wurm, Muldrotha, Jetsam.
- Deal: Cathedral Membrane, Spiteful Repossession, Sorcerer Class, Suffocation.

**Seen, not changed**
- **Face-down permanents still grant permissions.** `CastingPermissionService.hasIceCounterPermission` and `controlsStatic` check only `hasLostAllAbilities` before reading a permanent's card abilities. A face-down permanent doesn't set that flag, so a face-down Draugr Necromancer or Grolnok still grants its permission (CR 708.2a). `controlsStatic` has seven callers: croak, void, three "can't play / can't cast" effects and Ward of Bones. Sure of the code; the consequence is inferred.
- **Wicked Guardian draws with no creature to damage.** With no other creature, accepting the may still draws, though its Oracle text says "If you do". Fairly sure of the code. `WickedGuardianTest`'s methods also lack `@DisplayName`.
- **Fixed casting order.** The per-card free-cast queues fix the order and drop a declined card (CR 608.2g). Power Without Equal is only partly affected.
- **No graveyard stay check.** Thunderblade Charge and the Adventure permission find the card by id with no stay check (CR 400.7). Low impact.
- **Floating effect source, not traced.** `GrantStaticEffectToPlayerEffect` floats with no source permanent and timestamp 0, where the records it would absorb carry their source. The fold run should check that no reader needs that source.

**Left**
- Nothing in the chunk.
- Rows to re-triage with their batches:
  - Gwenom, and the dream and hatching rows (run 25)
  - Xanathar (run 18)
  - `AllowCastSourceCardFromGraveyardThisTurnEffect`, Sproutback Trudge and Tinybones (5m)
  - The two markers from run 27
  - `ExileTopCardOfEachLibraryAndMayCastSpellsEffect` (run 4)
  - Knight Rampager (run 9)
- Unaudited records these batches touch:
  - `AllowCastMatchingCardsFromGraveyardThisTurnEffect` (6)
  - `AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect` (4)
  - `BoostTargetCreaturePerChosenTypeCountEffect` (1)
  - `SacrificeCreatureWithLeastToughnessEffect` (1)

### Step 9, parallel triage chunk 03 (2026-11-23)

Verdicts only: no repo file changed and the ratchet did not move. 69 ledger rows (notes `triage P03: …`) cover all of Shuffle (18 rare), Enchanted (17), Protection (17) and Redirect (17).

Every record was read with its handler to the end, plus the card classes that use it. For records with no handler, these readers were read:
- `BattlefieldPlacementService`: the exile-entry shuffle and the enters-tapped helper.
- `GraveyardService`: the dying-library replacements.
- `GraveyardChoiceHandlerService`: The Three Seasons and Stream of Thought.
- `AttackLegalityService` and `CombatAttackService`.
- `GameQueryService`: transform, the {T}-ability lock, targeting, loyalty, protection and attached restrictions.
- `CastingPermissionService`, `AuraCopyService`, and the type-setting cases of `LayerSystemService`.
- `DamagePreventionService` and `DamageSupport`: the redirect shields.
- `DrawService`.
- The Blood of the Martyr and Protection Racket may handlers.

Oracle text was checked with `get_card` for every `unique` and `blocked-*` card: Don't Blink, Bad Wolf Bay, Emrakul, Mirror Golem, Brand of Ill Omen, Overwhelming Splendor, Protection Racket, Guff Rewrites History, Blood of the Martyr, Treacherous Link and Plagiarize. Planeswalkerificate is a playtest card Scryfall doesn't list, so it was judged from its class. Oracle text was also checked for Saiba Syphoner, Assemble from Parts, Credit Voucher, Lord of the Nazgûl, Commander's Plate, Weathered Bodyguards, Kjeldoran Royal Guard, Soltari Guerrillas, Goblin Psychopath, Harsh Judgment, Shield Dancer and Shimian Night Stalker. The other cards were judged from their classes.

Rulings read: Bad Wolf Bay, Commander's Plate. CR 111.7, 205.1a, 400.7, 400.7j, 603.10a, 608.2d, 613.1f, 614.9, 614.12, 701.24a, 701.24d, 702.16b and 708.2a were checked with `get_rule` (2026-11-23).

| Family | Triaged | `step-9` | `blocked-other` | `blocked-player-loop` | `unique` |
|---|---|---|---|---|---|
| Shuffle | 18 rare | 16 | 0 | 1 | 1 |
| Enchanted | 17 rare | 15 | 1 | 0 | 1 |
| Protection | 17 rare | 14 | 1 | 0 | 2 |
| Redirect | 17 rare | 14 | 0 | 0 | 3 |
| All | 69 | 59 | 2 | 1 | 7 |

No record is dead. `ProtectionFromColorlessEffect` has no card, but `ChoiceHandlerService`'s protection colour choice builds it.

**Fold batches** (survivor or shared component → records of this chunk, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| card shuffle | `PutCardsIntoLibraryEffect` + a shuffle placement, `shuffledIn(cards)`: each owner's library is shuffled once, also when nothing moved (CR 701.24d), and the step records SHUFFLE_INTO_LIBRARY. Uses `ChooseCardEffect` where the cards are chosen | Credit Voucher (+ draw `AmountThisWay(SHUFFLE_INTO_LIBRARY)`), Stream of Thought, Assemble from Parts, Riftsweeper, Serene Remembrance + Turn the Earth (one record), The Three Seasons. Run 12's blocked `ShuffleCardsFromHandIntoLibraryThenEffect` joins: 6 (7) |
| per-graveyard choice | `ChooseCardEffect` with a for-each-graveyard count | The Three Seasons (above). Re-triage run 1's unique `ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffect` with it |
| permanent shuffle | `ShuffleReferencedPermanentIntoLibraryEffect` / `ShuffleTargetPermanentIntoLibraryEffect` record SHUFFLE_INTO_LIBRARY (owners as players) and take set subjects: one leave event, one shuffle per library | Mirror-Mad Phantasm, This Is How It Ends (`villainousChoiceFacedBy(playersThisWay(...))`), Teferi, Temporal Pilgrim, Elixir of Immortality (deferred shared shuffle): 4 (4). Step 8's blocked-step-model rows for `ShuffleSelfIntoOwnerLibraryEffect` and `ShuffleTargetPermanentIntoLibraryEffect` say "no StepKind". They are stale: SHUFFLE_INTO_LIBRARY exists since run 9. The rows waiting on that parent (`ExileTopCardPutLandOntoBattlefieldOrMayCastFreeEffect`, `RevealTopCardPermanentToBattlefieldEffect`) go with them |
| Shuffle, by parameters | existing records | Psychic Spiral (`EachPlayerShufflesZonesIntoLibraryEffect(you(), {GRAVEYARD})` + `MillEffect(shuffledAwayByYou(), targetPlayer())`), Elvish Soultiller (`ChooseCreatureTypeAtResolutionEffect` + `ShuffleGraveyardIntoLibraryEffect` with `CardHasTypeChosenThisResolutionPredicate`), Jace, the Mind Sculptor (`handsOf(targetPlayerDeclaredElsewhere())`), Zoyowa's Justice (`NoteAmountEffect(TargetManaValue)` + the then-wrapper with `DiscoverEffect(noted())`, `TARGET_OWNER`): 4 (4) |
| dying-library position | `PutOnTopOfLibraryInsteadOfDyingEffect` + a position (top, bottom, shuffled) | Saiba Syphoner: 1 (1). Re-triage run 12's unique `PutOnBottomOfLibraryInsteadOfDyingEffect` with it |
| outside the game | triage 2's `SearchOutsideGameForCardToHandEffect` + a destination + a count | Research // Development (shuffled in, up to four): 1 (1) |
| pay any amount | triage 1's pay batch (`EachPlayerPaysAnyManaEffect`, the payer is the carried upkeep player) + a "prevent X of that damage" read of PAY_MANA; cost-shape | Errant Minion + Power Leak (one record): 1 (2) |
| attached restrictions | `AttachedPermanentRestrictionEffect` wrapping the self record; each self record's query reads the attached restrictions. These are the attachment's own abilities, so the host losing its abilities doesn't remove them (CR 613.1f) | Instill Energy (Chaos Lord's as-though-haste record, with its unless-entered part as an option), Errantry (`CanOnlyAttackAloneEffect`), Sightless Brawler (`CantAttackOrBlockAloneEffect(false, true)`), Avacyn's Collar + Bound by Moonsilver (can't transform), Canopy Cover + Shielding Plax (`TargetingRestrictionEffect`, opponents of the attachment's controller): 5 (7) |
| {T}-ability lock | `MatchingPermanentsCantActivateTapAbilitiesEffect` + a static `PermanentSubject` | Serra Bestiary: 1 (1) |
| copier subject (triage 6) | `BecomeCopyOfSubjectEffect` + a copier subject (+ copy exceptions, `WHILE_ATTACHED`) | Metamorphic Alteration (with `ChoosePermanentOnEnterEffect`), Loose in the Park: 2 (2) |
| colour (triage 6) | the static chosen-colour batch; the resolution colour batch | Shimmerwilds Growth; Dream Coat: 2 (2) |
| chosen type | `EnchantedPermanentBecomesTypeEffect` (9 cards) + the source's chosen subtype + a `PermanentSubject` | Convincing Mirage + Phantasmal Terrain (one record): 1 (2). `SourceBecomesChosenBasicLandTypeEffect` (2 cards, unaudited) joins |
| set card types | `SetCardTypesEffect(Set.of(LAND), attached())`, once its layer case drops enchantment subtypes | Imprisoned in the Moon + Song of the Dryads (one record): 1 (2) |
| static cast restriction | `OpponentsCantCastSpellsMatchingPredicateEffect` taking `controllerOf(attached())` and `you()` | Brand of Ill Omen: 1 (1). `CantCastSpellTypeEffect` (4 cards, unaudited) joins |
| enters tapped | `EnterPermanentsOfTypesTappedEffect` (23 cards) + a `PlayerSubject` in place of `opponentsOnly` | Radiant Restraints: 1 (1). `CreaturesOfUnchosenParityEnterTappedEffect` (unaudited) joins |
| mana-value protection | `ProtectionFromManaValueEffect` + a mana-value quality (at least, at most, parity, other than a number) | Mistmeadow Skulk (survivor), Reaver Titan, Lavabrink Venturer, Haktos: 4 (4) |
| colour-count protection | `ProtectionFromMulticoloredEffect` (6 cards) + a colour-count quality | Guardian of the Guildpact + Providence of Night (one record), Frenemy of the Guildpact, the engine-built colorless grant: 3 (3) |
| permanent-quality protection | `ProtectionFromPermanentsMatchingEffect` + predicates (modified creature, counter of a type, Ring-bearer, non-[subtype] creature) | Ronom Hulk + Tsabo Tavoc (survivor), Louvaq, Geyadrone Dihada, Lord of the Nazgûl, Harbinger of Spring + Spare from Evil: 5 (7) |
| dynamic protection colours | `ProtectionFromColorsEffect` (170 cards) + a colour set judged as asked | Empty-Shrine Kannushi + Pledge of Loyalty (one record), Commander's Plate: 2 (3). `ProtectionFromChosenColorEffect` (8 cards, unaudited) joins |
| your-damage redirects | `RedirectYourDamageToTargetCreatureThisTurnEffect` (5m unique, re-triage) + a destination subject, a protected set, a source subject, combat-only and a `Duration`, on `TurnDamageRedirectToCreatureShield` | Gideon's Sacrifice, Turn the Tables, Shimian Night Stalker, Ascent of the Worthy: 4 (4). 5m's unique Saving Grace row joins |
| creature redirects | `RedirectNextDamageEffect` (16 cards) + an all-this-turn amount, a source filter and combat-only | Karona's Zealot, Sivvi's Valor, Shield Dancer: 3 (3) |
| source-keyed redirects | `RedirectNextDamageFromChosenSourceToSelfEffect` (5m unique, Opal-Eye; re-triage) + a source subject or filter, a recipient filter, a destination (the source's controller, you, a permanent), an amount, combat-only, and a `Duration` or a static form | Reverberation, Mirror Strike, Aegis of Honor, Harsh Judgment (static), Goblin Psychopath, Soltari Guerrillas: 6 (6) |
| unblocked absorbers | `RedirectPlayerDamageToSelfEffect` (8 cards) + combat-only + a floated this-turn form | Kjeldoran Royal Guard + Weathered Bodyguards (one record): 1 (2) |

**Blocked (3):**
- Overwhelming Splendor, `blocked-other`. There is no static form of the player restriction and no activating kind that leaves loyalty abilities usable. One card.
- Protection Racket, `blocked-other`. Its text repeats a process for each opponent in turn order, and no step repeats a sequence for each player. Cost-shape.
- Guff Rewrites History, `blocked-player-loop`. Each player who controlled a shuffled permanent may cast the card they exiled. The rest of the card folds.

**Unique (7):**
- Don't Blink: the shuffle-instead-of-entering replacement.
- Planeswalkerificate: toughness used as loyalty.
- Emrakul, the Aeons Torn: protection from coloured spells only.
- Mirror Golem: protection from the card types of the cards it exiled.
- Blood of the Martyr: an optional redirect asked for each creature damage event.
- Treacherous Link: a static redirect to the enchanted creature's controller.
- Plagiarize: a draw redirect for the turn.

**Seen, not changed** (confirmed by reading, not run):

- **Bad Wolf Bay.** Its chaos ability ("cards can't enter from exile this turn") is `ShuffleCreaturesEnteringFromExileEffect`, Don't Blink's replacement. A creature card entering from exile is shuffled into its owner's library, and a noncreature card still enters, where the text keeps every card in exile. `CardsCantEnterBattlefieldFromZonesEffect` would say it, but it enforces only graveyard and library (its Javadoc). Sure of the code; it only matters in Planechase.
- **`SetCardTypesEffect`'s layer-4 case.** It drops the creature, artifact, planeswalker and land subtypes of the card types it removes, but keeps enchantment and battle subtypes (CR 205.1a). Minimus Containment, Sugar Coat, In Too Deep and Swift Reconfiguration on an Aura, Saga, Class or Shrine leave it with its enchantment subtype. `EnchantedPermanentBecomesOnlyLandEffect`'s case drops them. Sure of the code; the Saga consequence wasn't traced.
- **Dying-library replacements in `GraveyardService`** (Saiba Syphoner, Gravebane Zombie, the put-on-bottom card). They check only the floating `losesAllAbilitiesByEffect`, never `hasLostPrintedAbilities` (CR 613.1f). A creature that lost its abilities to a static effect (Humility, Overwhelming Splendor) still uses its replacement. The shuffle branch also logs "is revealed and", which Saiba's text doesn't say. Fairly sure; same kind as defect run 2's fixes.
- **The Three Seasons (III).** `GraveyardChoiceHandlerService` moves the chosen cards outside a graveyard-leave batch and shuffles with `Collections.shuffle` instead of `LibraryShuffleHelper.shuffleLibrary`. No shuffle event is sent, so "whenever a player shuffles" triggers miss it, and the Bombardment restore and the top-card free-play end are skipped. Sure of the code.
- **Radiant Restraints and Overwhelming Splendor.** Their readers (`EnchantedPlayerCreaturesEnterTappedEffectHandler` and `GameQueryService.playerCantActivateNonManaOrLoyaltyAbilities`) don't check whether the Aura lost its abilities or is ignored (Volrath's Curse). Sure of the code; low impact.
- **Choices without the can't-be-chosen check** (CR 608.2d convention): `AuraCopyService.beginChooseCreatureOnEnter` (Metamorphic Alteration) and Ascent of the Worthy's creature choice. Sure of the code. Gideon's Sacrifice's multi-permanent choice wasn't checked.
- **Mirror-Mad Phantasm and Teferi, Temporal Pilgrim.** Mirror-Mad puts the found card onto the battlefield with `putPermanentOntoBattlefield`, outside the entry batch. Teferi's -12 removes the permanents one at a time. Low impact; the folds above fix both.

**Left.** Nothing in chunk 03. Re-triage these rows when their batch is folded:
- `PutOnBottomOfLibraryInsteadOfDyingEffect`
- `ExileUpToOneMatchingCardFromEachGraveyardWithSourceEffect`
- `ChooseCreatureOnEnterEffect`
- `RedirectYourDamageToTargetCreatureThisTurnEffect`
- `RedirectYourDamageToEnchantedCreatureThisTurnEffect`
- `RedirectNextDamageFromChosenSourceToSelfEffect`
- the two stale shuffle blocked-step-model rows
- `ShuffleCardsFromHandIntoLibraryThenEffect`

### Step 9, parallel triage chunk 06 (2026-11-23)

Verdicts only: no repo file changed, and the ratchet did not move. There are 76 ledger rows (notes `triage P06: …`), read against `e3c5d02ca8`: Award 12, Exchange 12, Creatures 11, May 11, Damage 10, Ignore 10, Player 10.

What was read:
- Every record and every handler to its end, including the `manafx` handlers, and every card class that uses a record.
- For records with no handler, their readers: `ActivatedAbilityExecutionService`'s mana branches and cost-land collectors, `AbilityActivationService`'s cost recorders, `AnyColorManaChoiceSupport`, `AttackLegalityService`, `CombatAttackService`'s attack-together validator, `CombatBlockService`'s must-block recompute, `CastingCostService.getAttackPaymentPerCreature`, `BattlefieldPlacementService`'s enter-tapped paths, `LegendRuleService`, `DamageSupport`'s damage-to-counters path, `CastingPermissionService` and the player-protection, life-floor, infect, hexproof-ignore and damage-colour readers in `GameQueryService`.
- Oracle text with `get_card` for every `unique` and `blocked-*` card, and for Twins of Discord, Nils, Festival, Energy Vortex, Loxodon Peacekeeper, Archfiend of Depravity, Archfiend of Spite, Detection Tower, Spider-Verse, Council of Reeds, Cadric, The Master Multiplied, Sliver Gravemother and Mirror Gallery.
- The MB1 and MB2 playtest cards (Wizened Arbiter, Wrath of Sod, Boulder Jockey, Mothers Yamazaki) are not in Scryfall; their text was read from `card-data-cache`.
- Rulings: Rhystic Cave.
- CR checked with `get_rule`: 101.4, 118.12, 400.7, 404.3, 605.1a, 605.1b, 611.2c, 613.1f, 616.1, 701.12a, 704.5j, 708.2a.

| Family | Triaged | `step-9` | `blocked-other` | `unique` |
|---|---|---|---|---|
| Award | 12 | 9 | 0 | 3 |
| Exchange | 12 | 8 | 0 | 4 |
| Creatures | 11 | 9 | 0 | 2 |
| May | 11 | 0 | 11 | 0 |
| Damage | 10 | 5 | 0 | 5 |
| Ignore | 10 | 10 | 0 | 0 |
| Player | 10 | 8 | 0 | 2 |
| All | 76 | 49 | 11 | 16 |

**Fold batches** (survivor or component → records, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| any-colour amount | `AwardAnyColorManaEffect`. Selvala uses `DistinctPowersAmongControlledCreatures`. Three Tree City uses `PermanentCount(creature + PermanentHasSourceChosenSubtypePredicate)`. Wrath of Sod uses `SOURCE_PERMANENT_COLORS`, Katilda's form. Corrupted Grafstone needs a new `GRAVEYARD_CARD_COLORS` colour-source value; it is a one-card value, so weigh it in the fold run. | 4 (4) |
| cost-paid land | `AwardManaOfTypeLandsCouldProduceEffect` with a `ManaColorLandScope` for the land paid in the cost (the sacrificed snapshot, or the untapped land). | Squandered Resources, Benthic Explorers: 2 (2) |
| player-recipient mana | `AwardManaToActivePlayerEffect` (5 cards, blocked row; re-triage it) with a recipient `PlayerSubject`. | Tangleroot (`triggeringPlayer()`), Jetfire (`targetPlayer()` + Powerstone restriction): 2 (2) |
| mana rider | `AwardHasteGrantingManaEffect` (3, unaudited) with a rider kind. | Boseiju: 1 (1) |
| set exchange | `ExchangeControlOfAllMatchingPermanentsWithTargetPlayerEffect`, as it stands. | Twist Allegiance (survivor), Reins of Power: 2 (2) |
| shared-type exchange | Split: `ExchangeControlOfTargetPermanentsEffect` with a shared-types set replacing its two shared-type booleans and recording GAIN_CONTROL, then a conditional destroy over `attachmentsOf(thisWay(GAIN_CONTROL), Aura)`. | Gauntlets of Chaos: 1 (1) |
| spell-and-permanent exchange | One record over a `PermanentSubject` and a spell subject, with a new-targets chooser and the CR 701.12a check. | Perplexing Chimera, Sudden Substitution: 2 (2) |
| zone exchange | `ExchangeGraveyardAndLibraryEffect` with a zone pair and the CR 404.3 order prompt; its shuffle becomes `ShuffleLibraryEffect`. | Morality Shift, Harness Infinity, Mordenkainen: 3 (3) |
| player-floated statics | `GrantStaticEffectToPlayerEffect`. Chronomantic Escape: `untilYourNextTurn(CreaturesCantAttackControllerUnlessPredicateEffect)`; P02's attack-ban batch should take this survivor too. Angel of Grace: `untilEndOfTurn(DamageLifeFloorEffect(1, LIFE_AT_LEAST_FLOOR))`. | 2 (2) |
| global can't-attack | `CantAttackThisTurnEffect(controlledBy(eachPlayer(), creature))`. | Festival + Orim's Chant: 1 (2) |
| set can't-block | `CantBlockThisTurnEffect(controlledBy(eachPlayer(), …))`; Twins needs `lastingRule` to bind `PermanentHasChosenParityPredicate`. | Twins of Discord, Immobilizer Eldrazi: 2 (2) |
| chosen-type must attack | `ChooseCreatureTypeAtResolutionEffect` + `MatchingCreaturesMustAttackThisTurnEffect`, with the chosen type bound when the rule floats. | Walking Desecration: 1 (1) |
| parity enters tapped | `EnterPermanentsOfTypesTappedEffect.matching` + a source's-chosen-parity predicate. | Ashling's Prerogative: 1 (1); `GrantKeywordToCreaturesOfChosenParityEffect` joins |
| attack together | `CreaturesWithCounterAttackTogetherEffect` with an attacking filter and a bound filter. | Magnetic Web: 1 (1); joined by Ekundu Cyclops (unaudited) and War's Toll (P02 unique row; re-triage) |
| per-attacker tax | `RequirePaymentToAttackEffect` with an amount read against the attacker (cost-shape). | Nils: 1 (1) |
| power-gated attack | `CreaturesCantAttackUnlessPredicateEffect` + `PermanentPowerGreaterThanSourceControllerHandSizePredicate`; the reader must pass the source context. | Ensnaring Bridge: 1 (1) |
| unless discard | Split: `MayEffect(discard, …, damage + ConditionalEffect(HappenedThisWay(DEAL_DAMAGE), tap source()))` (cost-shape). | Mishra's War Machine: 1 (1) |
| sacrifice N or lose life | `EachPlayerSacrificesPermanentOrLosesLifeEffect` + a `PlayerSubject` + a count (cost-shape). | Archfiend of Spite: 1 (1) |
| single-payer damage unless pays | `EachPlayerTakesDamageUnlessPaysEffect` + a `PlayerSubject` + a per-counter cost (cost-shape). | Energy Vortex, Soul Barrier: 2 (2) |
| legend-rule exemption | `IgnoreLegendRuleEffect` + a static `PermanentSubject`. | Mirror Gallery, Council of Reeds, Mirror Box + Sakashima, Spider-Verse + Sliver Gravemother, Cadric + The Master: 5 (8) |
| exactly two | `IgnoreLegendRuleWhenExactlyTwoSameNameEffect` + a count scope. | Brothers Yamazaki, Mothers Yamazaki + Syr Joshua and Syr Saxon: 2 (3) |
| hexproof ignore | `IgnoreOpponentHexproofEffect` + a breadth, floated by `GrantStaticEffectToPlayerEffect`. | Detection Tower: 1 (1); the two 5m unique rows join |
| ignore source | `IgnoreSourceAuraEffectsUntilEndOfTurnEffect`. | Lost in Thought + Volrath's Curse, Damping Engine: 2 (3) |
| Choose Your Champion | Split: `ChoosePlayerEffect` + `PlayerRestrictionEffect` + `CantAttackThisTurnEffect(controlledBy(except(...)), UNTIL_YOUR_NEXT_TURN)`; re-triage run 39's blocked row. | `PlayerCantCastSpellsAndAttackWithCreaturesEffect`: 1 (engine-built, then dead) |
| choose, sacrifice the rest | Split: `ChoosePermanentsEffect.chosenBy` + `SacrificeSelfEffect(except(…, thisWay(CHOOSE)))`. | Archfiend of Depravity: 1 (1) |
| player protection | One player-protection static with a quality, floated by `GrantStaticEffectToPlayerEffect`. | Runed Halo, Absolute Virtue: 2 (2); joined by run 18's two blocked rows, Serra's Emissary's player half and the unaudited colour (13) and everything (4) grants |
| leader gains control | `GainControlOfTargetEffect.gainedBy(matching(eachPlayer(), unique-leader predicate))`; Loxodon Peacekeeper picks among tied players through `ChoosePlayerEffect`. | Loxodon Peacekeeper, Sokenzan Renegade, Thoughtbound Primoc + Wild Mammoth, Ghazb'n Ogre + Wild Dogs: 4 (6) |

**Blocked (11):** all of May's 11 cost combinators. They wait for step 10's cost-combinator merge, by the owner's decision of 2026-11-23. The three pay-life-equal records point at `MayPayLifeEqualToAmountEffect`.

**Unique (16):**
- Award: Rhystic Cave, Sunbird Effigy, Open the Omenpaths.
- Exchange: Wizened Arbiter, Serene Master, Darkpact, Deadpool.
- Creatures: Arboria, Magnetic Web's must-block rule.
- Damage: Phyrexian Unlife, Archon of Coronation, Ghostly Flame, Delaying Shield + Force Bubble (already one record), Whippoorwill.
- Player: Melira, Celestial Convergence.

**Seen, not changed** (confirmed by reading, not run):
- Sokenzan Renegade's handler finds its source by card id, while its three siblings use the entry's permanent id, so a Renegade that left and returned is moved by the old trigger (CR 400.7). Fairly sure.
- The Master, Multiplied: the legend rule should not apply to creature tokens only, but the record exempts every token you control (CR 704.5j). Sure; low impact.
- `LegendRuleService.hasControlledTokensExemption`, Magnetic Web's `getAttackTogetherCounterTypes`, Ghostly Flame's `isDamageSourceColorNullified` and `damageToControllerCounterReplacementSources` read printed static effects with no lost-abilities or face-down check (CR 613.1f, 708.2a). Sure of the code; low impact.
- With Delaying Shield and Force Bubble both out, damage goes to the first source found; the player should choose (CR 616.1). Sure; rare.
- `CastingCostService.getAttackPaymentPerCreature` takes the defender as `getOpponentId` (Nils and the other taxes), so it is right with two players only. Sure.
- Harness Infinity puts the hand into the graveyard with no order prompt (CR 404.3); Morality Shift asks. Sure; low impact.
- The set exchanges (Reins of Power, Twist Allegiance) and Perplexing Chimera don't check `cantBeControlledByOtherPlayers`. CR 701.12a's all-or-nothing rule for a set exchange wasn't settled. Unverified.
- Chunk 02 ruled War's Toll `unique` as the only attack requirement that depends on the declared attackers; Magnetic Web and Ekundu Cyclops have the same shape. Re-triage that row.

**Anything left.** Nothing in this chunk. The batches reach records outside it:
- Rows to re-triage: `AwardManaToActivePlayerEffect` (blocked), `TargetOpponentChoosesPlayerForRestrictionEffect` (blocked), `OpponentCreaturesAttackTogetherEffect` (P02 unique), and the 5m hexproof-ignore and run-18 player-protection rows.
- Unaudited records to fold with the batches: `AwardHasteGrantingManaEffect`, `MustAttackIfAnotherCreatureAttacksEffect`, `GrantKeywordToCreaturesOfChosenParityEffect`, `OtherCreaturesCantAttackThisTurnEffect`, and the colour and everything protection grants.

Suggested fold order:
1. The by-parameter rows: Wrath of Sod, Reins of Power, Festival and Orim's Chant, Immobilizer Eldrazi, Chronomantic Escape, Angel of Grace, Damping Engine.
2. The legend-rule exemption batch.
3. The leader-gains-control batch.
4. The any-colour amount batch.

### Step 9, parallel triage chunk 07 (2026-11-23)

Only verdicts this time. No repo file changed; the plan and ledger were read at `4457b7d143`, and the ratchet did not move. The 83 ledger rows (notes `triage P07: …`) are in /tmp/claude-1000/-mnt-ramdisk-magical-vibes/300dbf19-8a01-4cdb-8671-d50e7ccdb4f0/scratchpad/ptriage/out-07/ledger-rows.csv.

**What was read**
- Every record, its handler read to the end, and every card class that uses it.
- For records with no handler, their readers:
  - `CastingPermissionService`: any-mana cast, `isLandPlayRestricted`, the from-hand restrictions
  - `GameQueryService`: the white-mana, any-colour and activated-ability spend reads, `damageCantBePreventedFromSource`, `getGlobalBlockManaTax`, `isCardNameUnchoosable`
  - `SpellCastingService`'s {C} payment branch and `ManaPool`'s spend flags
  - `LayerSystemService`'s layer-4 and 7b cases for March, Starfield and Opalescence
  - `AttackLegalityService` and `CombatAttackService`'s requirement and permission reads
  - `BlockLegalityService`'s blockability permissions and `CombatBlockService`'s must-be-blocked paths
  - `CastingCostService.getPhyrexianAttackPaymentsPerCreature`
  - `PlanechaseService.roll` and the D4, D12, D20 and `DiceRollService` roll services
- Also read: `AnimationSupport`, the target-to-target counter head, `MatchingCreaturesMustAttackThisTurnEffect`, `ChooseRandomOpponentMustAttackThisCombatEffect`, `RandomPlayerThenFightsTargetCreatureEffectHandler` and `DoubleControllerDamageEffect`.
- Oracle text: checked with `get_card` for every `unique` card and for most `step-9` cards. Microscope and Glimpse, the Unthinkable are MB2 playtest printings in no feed, so their class Oracle was used.
- Rulings: Tempt with Glory, Vizier of the Menagerie, Brilliance Unleashed, The War in Heaven, Liliana, Death's Majesty.
- Rules checked with `get_rule`: CR 106.1b, 107.4c, 122.5, 122.8, 400.7, 609.4b, 613.1f, 701.27a, 701.27c, 701.27d, 701.27e, 701.27g, 706.1, 706.2, 706.6, 708.2a, 712.10, 901.3a, 901.9.

| Family | Triaged | step-9 | unique |
|---|---|---|---|
| Spend | 10 | 8 | 2 |
| Animate | 9 | 8 | 1 |
| Must | 9 | 4 | 5 |
| Roll | 8 | 2 | 6 |
| Move | 7 | 7 (1 dead) | 0 |
| Require | 7 | 6 | 1 |
| Source | 7 | 6 (1 dead) | 1 |
| TemptingOffer | 7 | 7 | 0 |
| Transform | 7 | 7 | 0 |
| Can | 6 | 4 | 2 |
| Controller | 6 | 4 | 2 |
| All | 83 | 63 | 20 |

**Fold batches** (records / cards)

- **any-mana cast** — survivor: `SpendAnyManaTypeToCastEffect`, gaining a `CardPredicate` spell filter (types, subtypes, not owned by you) and an any-type / any-colour mode. 3 records / 4 cards.
  - Vizier and Oath of Nissa (one record), Case File Auditor, Nathan Drake.
  - The three `AnyManaTypeCastEffect` interface methods go.
- **player spend** — survivor: `SpendManaAsAnyColorEffect`, gaining a from-colour set and an as-colour set. 3 records / 4 cards.
  - Mycosynth Lattice and Chromatic Orrery (the survivor), Sunglasses of Urza, False Dawn.
  - False Dawn is floated by chunk 01's `GrantStaticEffectToPlayerEffect.untilEndOfTurn`.
  - Three `ManaPool` flags become one table. Sunglasses alone sets the red as-colour set: weigh it.
- **activated-ability spend** — survivor: `SpendManaAsAnyColorForActivatedAbilitiesEffect`. 2 records / 2 cards.
  - Quicksilver Elemental: blue only.
  - Nathan Drake: permanents you control but don't own. The reader's creature gate must go for him.
- **mana-value animation** — survivor: `AnimateNoncreatureArtifactsEffect`, with a subject and filter, a threshold, a skip-creatures option and a loses-all-abilities option. 3 records / 4 cards.
  - March of the Machines and Titania's Song (the survivor), Starfield of Nyx, Opalescence.
  - Three board id tables become one.
- **returned animation** — by parameters: `AnimatePermanentsEffect` over `thisWay(RETURN[, filter])` with `PERMANENT`. 3 records / 3 cards.
  - Brilliance Unleashed: judge the returned card's printed types.
  - Tune Up, The War in Heaven.
- **chosen-permanent animation** — 2 records / 2 cards.
  - Cosmium Confluence: `AnimatePermanentsEffect` over `thisWay(PUT_COUNTERS)`, with triage 1's `PutCounterOnChosenOwnPermanentEffect` batch.
  - Turbocharged Escape: `ChoosePermanentsEffect` plus `PerpetuallyGrantCardCharacteristicsEffect`. This is a battlefield permanent, so not an owner's question.
- **counter move** — survivor: `MoveCounterFromSourceToTargetCreatureEffect`, gaining set from/to subjects, a chosen kind, any number per permanent and kind, and distribution. 5 records / 6 cards.
  - Slippery Bogbonder, Agent's Toolkit, Aetherborn Marauder, Forgotten Ancient.
  - Resourceful Defense goes onto the target-to-target head `MoveCounterFromTargetCreatureToTargetCreatureEffect`.
  - Galloping Lizrog, which shares Aetherborn's record, is a removal: it splits onto triage 4's any-number removal plus `PutCountersOnSelfEffect`.
  - 5m's blocked `MoveCountersFromSourceToTargetCreatureEffect` joins.
- **floated tax** — survivor: `RequirePaymentToAttackThisTurnEffect`, gaining a kind (attack mana, attack life, block mana), protected players, a `Duration` and an amount locked at resolution. 5 records / 5 cards.
  - War Tax, Forbidding Spirit, Summon: Yojimbo, Sivitri, War Cadence.
  - The payloads become the static records floated.
  - Archangel of Tithes' `RequirePaymentToBlockEffect` is the block-side static survivor.
- **tempting offer** — a new `TemptingOfferEffect(payload)`: one opponent accept loop in turn order, then the payload for each acceptor and for you once per acceptor, as the C13 ruling says. 7 records / 8 cards.
  - The seven mayfx halves go.
  - The search and return payloads prompt, so the combinator resumes after them as Discovery's continuation does.
- **transform subject** — survivor: `TransformSelfEffect`, whose subject guard widens to `thisWay(CHOOSE)`, `createdThisResolution()`, `granter()` and the put-permanent result. 7 records / 7 cards.
  - Tovolar, Sunder the Gateway, High Marshal Arguel, Optimus Prime, Nick Fury.
  - Blitzball and Soul Seizer split onto the transform plus chunk 02's attach verb.
- **must-attack / block, by parameters** — 4 records / 6 cards.
  - Taunt and Gideon Jura: `MatchingCreaturesMustAttackThisTurnEffect.controlledBy` with an attacked object and a `Duration`.
  - Tolsimir and Fighter Class: `MustBlockSourceEffect(…, true, triggering())`.
  - Slayer's Cleaver and Ace's Baseball Bat: `MustBeBlockedIfAbleEffect` with a blocker filter.
  - Arcum's Whistle (cost-shape): `MayPayManaEffect` with its else-effects.
- **Roll** — 2 records / 2 cards.
  - Clown Car onto `RollD6Effect` by parameters.
  - Berserker's Frenzy onto `RollD20Effect` with a two-dice keep-higher count. Only it sets the count: weigh it.
- **Source** — 5 records / 7 cards.
  - Thran Portal and Multiversal Passage: a chosen-land-type static over `source()` / `attached()`. The unaudited `EnchantedPermanentBecomesChosenTypeEffect` (2 cards) joins.
  - Proteus Machine: `TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(source())` with `PERMANENT`.
  - Malignus and Excruciator: `DamageCantBePreventedEffect` with a source subject. The unaudited `CombatDamageCantBePreventedEffect` joins.
  - Scab-Clan Giant: chunk 01's random-target batch.
  - Strax's engine-built fight record: `SourceFightsTargetCreatureEffect` with a target filter in a reflexive ability.
- **Can** — 4 records / 4 cards.
  - Street Savvy and Dragon Hunter onto `CanBlockCreaturesWithShadowEffect` with a mode and an attacker filter.
  - Hei Bai's token, by parameters: `CanBeBlockedOnlyByFilterEffect` plus `CanBlockOnlyIfAttackerMatchesPredicateEffect`.
  - Master of Cruelties: `CanOnlyAttackAloneEffect` with a subject. The unaudited `EnchantedCreatureCanOnlyAttackAloneEffect` joins.
- **static player restriction** — survivor: `ControllerCantPlayLandsEffect` carrying a `PlayerRestriction`, which gains a from-hand zone. 3 records / 4 cards: Aggressive Mining, Rock Jockey, Experimental Frenzy (2 records).
- **multiplier** — `DoubleControllerDamageEffect` (4 cards, unaudited) gains a multiplier. 1 record / 2 cards: Fiery Emancipation, City on Fire.
- **other single folds** — 1 record / 1 card each.
  - Phyrexian Splicer: `RemoveKeywordEffect` plus `GrantKeywordEffect` over target groups.
- **dead** — delete. 2 records / 0 cards.
  - `MoveAllCountersOfTypeFromTargetPermanentToTargetPermanentEffect`: only Black Panther imports it.
  - `SourceMustAttackRandomOpponentThisCombatEffect`: only Ruhan imports it.

**Unique (20)**
- Spend: North Star, Celestial Dawn.
- Animate: Microscope.
- Must:
  - Ekundu Cyclops, Galactus, Blaze of Glory.
  - The engine-built payloads `MustAttackIfAbleEffect` and `MustAttackPlayerEffect` (Nahiri's).
- Roll: Sword of Hours, Chaos Dragon, Neverwinter Hydra, Fractured Powerstone, Ichor Elixir, `RollWithAdvantageEffect` (Pixie Guide and Barbarian Class: already one record).
- Require: Norn's Annex.
- Source: Glimpse.
- Can: Chaos Lord, Weathered Sentinels.
- Controller: Malik, Call for Aid.

**Seen, not changed** (confirmed by reading, not run)
- **Dice advantage.** Pixie Guide and Barbarian Class (Oracle: "if you would roll one or more dice") are applied only by `D4RollService`, `D12RollService` and `D20RollService`. These roll through `DiceRollService`, which has no advantage: `RollD6Effect`, `RollD8Effect`, `RollD10Effect`, Clown Car, Neverwinter Hydra's d6s and Sword of Hours' d12 (`D12RollService` exists but isn't used there). Sure of the code.
- **Any type vs any colour.** `SpellCastingService` (the {C} branch near line 14639) treats every any-mana permission as any colour when the cost has {C}. So "mana of any type" (Vizier, North Star) can't pay {C} with colored mana, though {C} is a mana type (CR 106.1b, 609.4b). The any-colour cards (Oath of Nissa, Case File Auditor, Nathan Drake) are paid right. Fairly sure, low impact. The any-mana cast fold must keep the two modes apart.
- **Tempting offer timing.** The C13 ruling: all opponents decide, then the effect happens for the acceptors together, then for you. Tempt with Glory, Tempt with Bunnies, Tempt with Mayhem and Tempt with Immortality act as each opponent accepts. Sure of the code; low impact.
- **Missing ability checks in combat readers.**
  - `CombatAttackService.canOnlyAttackAlone` reads Master of Cruelties' printed statics with no lost-abilities or face-down check (CR 613.1f, 708.2a). Sure.
  - Ekundu Cyclops' first reader (`CombatAttackService` ~line 419) lacks the lost-abilities check its validator (~line 1736) has. Sure.
- **Two-player assumptions.**
  - Galactus: on a tie for most life it forces the first opponent by seat, where the controller may pick any tied opponent.
  - Norn's Annex's tax uses `getOpponentId` as the defender.
  - Nahiri's `MustAttackPlayerEffect` count uses `getOpponentId`.
  - All right with two players only. Sure of the code.
- **White-mana readers.** Celestial Dawn and Sunglasses of Urza (`playerBattlefieldHasStaticEffect`) check only the floating `losesAllAbilitiesByEffect` (CR 613.1f). Sure, low impact.
- **Face-down any-mana source.** `CastingPermissionService.canSpendAnyManaTypeFromBattlefield` doesn't skip face-down permanents, so a face-down Vizier still grants (CR 708.2a; kin of chunk 01's face-down permission defect). Fairly sure.
- **Returned animations come too late.** The returned-permanent animations (Brilliance Unleashed, Tune Up, The War in Heaven) apply after the permanent entered, so "enters" triggers don't see it as an artifact or creature. No ruling settles whether the text applies as it enters. Unsure.
- **Malik's choices aren't revealed.** Its handler logs neither chosen creature, though the Oracle says "those choices are revealed". Low impact.

**Left**
- Nothing in the chunk.
- Unaudited records these batches touch: `EnchantedPermanentBecomesChosenTypeEffect` (2), `DamageCantBePreventedEffect` (4), `CombatDamageCantBePreventedEffect` (1), `EnchantedCreatureCanOnlyAttackAloneEffect` (1), `DoubleControllerDamageEffect` (4), `RandomPlayerThenFightsTargetCreatureEffect` (Strax), `PlayersCantPlayCardsFromHandUntilNextTurnEffect`.
- Rows to re-triage with their batches:
  - 5m's `MoveCountersFromSourceToTargetCreatureEffect` and `MoveCounterFromTargetCreatureToTargetCreatureEffect`
  - 5m's `SourceFightsTargetCreatureEffect`
- Suggested fold-run order:
  1. Dead, by-parameter and single folds.
  2. Transform subject.
  3. Tempting offer.
  4. Counter move.
  5. Floated tax.
  6. Spend batches.
  7. Mana-value animation.

### Step 9, parallel triage chunk 05 (2026-11-23)

Verdicts only: no repo file changed. 84 ledger rows (notes `triage P05: …`), read against `e3c5d02ca8`: chunk 05's rare Increase (16), Make (15), Opponents (14), Players (14), Controlled (13) and All (12) records.

Every record was read with its handler to the end, along with every card class that uses it. For the records with no handler, their readers were read:
- `CastingCostService`: the cost-modifier snapshot, the targeting taxes, the per-target and own-target costs, and the loyalty tax.
- The costmod handlers.
- `CastingPermissionService`: the mana-value, chosen-colour, sorcery-timing and land-from-graveyard checks.
- `GameQueryService`: the life and sacrifice cost checks, turning face up, the own-turn and combat locks, player keywords, and the controlled combat-damage prevention.
- `LibrarySearchSupport.checkSearchRestriction`.
- `BattlefieldPlacementService`: riot, the entry counters and characteristics, enter-untapped, and Biomancer.
- `CombatAttackService`'s opponent attack requirement.
- `CombatBlockService`'s global must-block.
- `StepTriggerService`'s upkeep skip.
- `UntapStepService`'s planar untap.
- `LayerSystemService`'s subtype and land-type cases.
- `DurationClock`'s next-matching-spell ends.
- `GameData.applyNextSpellRiders`.

Oracle text was checked with `get_card` for every `unique` and `blocked-*` card: Kaervek's Torch, Altar of the Pantheon, Carth the Lion, Terror of the Peaks, Biblioplex Tomekeeper, Vexing Shusher, Ashiok, Tomik, Keen-Eared Sentry, Ashes of the Abhorrent, Stabilizer, Two Streams Facility, Masako, Deepchannel Mentor, Tetsuko, Displaced Dinosaurs, Blue, Loyal Raptor, Swirl the Mists, Everlasting Torment, Make an Example, Celestial Dawn, Mycosynth Lattice and Painter's Servant. The playtest cards (Noble Ox, You're in Command, Intangible Vibes, Revel in Silence, Priority Avenger, Terry Pin) are in no feed, so their text was read from the card-data cache or their classes.

Oracle text was also checked for both Teferis, Seeker of Slaanesh, Thunderous Velocipede, Coin of Mastery, Kalain, Desolation, Karlov Watchdog, Living Breakthrough, Avatar's Wrath, Memory Vessel, Horizon Boughs, Archelos, Gibbering Descent and Stalwart Realmwarden (from the cache).

Rulings were read for Make an Example, Seeker of Slaanesh, Hinata and Master Biomancer. CR 101.4, 118.9d, 508.1d, 608.2h, 613.1f and 708.2a were checked with `get_rule` (2026-11-23).

| Family | Triaged | `step-9` | `blocked-other` | `blocked-player-loop` | `unique` |
|---|---|---|---|---|---|
| Increase | 16 | 11 | 0 | 0 | 5 |
| Make | 15 | 10 | 0 | 1 | 4 |
| Opponents | 14 | 10 | 0 | 0 | 4 |
| Players | 14 | 11 | 0 | 0 | 3 |
| Controlled | 13 | 8 | 1 | 0 | 4 |
| All | 12 | 7 | 2 | 0 | 3 |
| All families | 84 | 57 | 3 | 1 | 23 |

**Fold batches** (survivor or shared component → records of this chunk, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| cost scope as a player subject | `IncreaseSpellCostEffect`'s `CostModificationScope` becomes a static `PlayerSubject` judged by `SubjectResolutionService.namesPlayer` (`you()`, `eachOpponent()`, `eachPlayer()`, `enchantedPlayer()`, `except(eachPlayer(), activePlayer())`). A player float made by `GrantStaticEffectToPlayerEffect` judges `you()` as its affected player; the cost collector passes `floating.controllerId()` today. `UNTIL_TARGET_PLAYER_MATCHING_SPELL_CAST` is added to the player grant. | Curse of Silence, Defense Grid, Academy Loremaster, Stalwart Realmwarden; Elspeth Conquers Death + Tax Collector already work by parameters (`GrantStaticEffectToPlayerEffect.untilYourNextTurn(IncreaseSpellCostEffect(…, OPPONENT))`): 5 (6) |
| Increase, by parameters | existing records | Chromatic Armor + Skeleton Scavengers (`IncreaseActivationCostEffect(Scaled(CountersOnSource))`), Damping Sphere (`IncreaseSpellCostEffect(any, SpellsCastThisTurn(CONTROLLER), ALL)`): 2 (3) |
| own-spell cost sign | `ReduceOwnCastCostEffect` / `ReduceOwnCastCostIfTargetingPermanentEffect` gain an increase direction; the `TargetBasedCastCostIncreaseEffect` reads for playability and the AI are kept | Hurkyl's Final Meditation + Risen Executioner, Dragon's Prey + Vanish into Eternity: 2 (4) |
| triage 1's per-target batch | one per-target cost change | Hinata (the opponents' half): 1 (1) |
| triage 7's hand-size batch | one hand-size record with a `PlayerSubject` and a `DynamicAmount` | Minamo Scrollkeeper + Trusted Advisor: 1 (2) |
| become blocked | `MakeTargetAttackingCreatureBlockedEffect` (4 cards) with a `PermanentSubject` | Fog Patch: 1 (1) |
| next-spell riders | `EmpowerNextCreatureSpellThisTurnEffect` (2 cards, unaudited) with a `CardPredicate` | Overmaster, Mistrise Village: 2 (2) |
| triage 6's copier subject | `BecomeCopyOfSubjectEffect` + a copier `PermanentSubject` (+ `UNTIL_YOUR_NEXT_TURN`); Cytoshape and Polymorphous Rush add `ChoosePermanentsEffect` + `thisWay(CHOOSE)` | Fleeting Reflection, Shapesharer, True Polymorph + Prime Minister's Cabinet Room, Cytoshape + Polymorphous Rush: 4 (6) |
| prepared | `BecomePreparedEffect` (49 cards) with a `PermanentSubject` (the granting-permanent flag becomes the subject) | Biblioplex Tomekeeper + Skycoach Waypoint: 1 (2) |
| Make singles | existing parts | Stromgald Spy (`GrantStaticEffectToPlayerEffect(PlayWithOwnHandRevealedEffect, defendingPlayer(), WHILE_SOURCE_REMAINS)` + the projection reading it), The Black Gate (split: `ChoosePlayerEffect` over a most-life `PlayerPredicate` + a this-turn can't-be-blocked bound to `playersThisWay(CHOOSE)`, triage 7's chosen-value binding): 2 (2) |
| static cast restrictions | `OpponentsCantCastSpellsMatchingPredicateEffect` (4 cards) | Iona (`CardHasSourceChosenColorPredicate`, by parameters), Brisela (`CardMaxManaValuePredicate(3)`, keeping the X playability rule), Angel of Eternal Dawn + Lavinia (a static mana-value-over-amount predicate whose amount reads the casting player): 3 (4) |
| player-restriction filter | `PlayerRestrictionEffect`: `PlayerRestriction`'s spell kinds take a `CardPredicate` and source zones (generalising `spellTypes` and noncreature) | Avatar's Wrath (outside the hand), Memory Vessel (play from the hand), Living Breakthrough (a mana value bound at resolution): 3 (3) |
| static zone play | `PlayersCantCastSpellsFromZonesEffect` (5 cards) with a static `PlayerSubject` and an action (cast spells / play lands) | Tomik's lands from graveyards; the unaudited `ControllerCantCastSpellsFromHandEffect` and `ControllerCantPlayLandsFromHandEffect` join: 1 (1) |
| search prohibition | `CantSearchLibrariesEffect` (triage 2 unique row) with a static `PlayerSubject`, also read as a player float | Stranglehold, Shadow of Doubt: 2 (2) |
| conditional cast lock | `OpponentsCantCastOrActivateDuringYourTurnEffect` (9 cards) with a static `PlayerSubject`, a spell filter, an ability filter (none / non-mana / all) and a `Condition` judged for the restricted player | Teferi ×2 (sorcery timing), City of Solitude (own turn, mana abilities too), Hand to Hand (combat), Priority Avenger (stack empty); `OpponentsCantCastSpellsDuringCombatEffect` and `PlayersCanCastSpellsOnlyDuringOwnTurnEffect` (5m survivors) join: 4 (5) |
| attack requirement | merge with `OpponentsMustAttackControllerEffect` (5m unique), adding a required-target option | Seeker of Slaanesh: 1 (1) |
| face-up prohibition | `CantBeTurnedFaceUpEffect` (triage 2 unique) with a static `PermanentSubject` and a your-turn `Condition` | Karlov Watchdog: 1 (1) |
| cost prohibition | one static "can't pay life [or sacrifice matching permanents]" record with a mana-ability exemption and a sacrifice predicate | Karn's Sylex, Angel of Jubilation, Yasharn: 3 (3) |
| player keyword | `GrantControllerKeywordUntilEndOfTurnEffect` (6 cards) with a `PlayerSubject` | Everybody Lives; Sarah's Wings' engine-built flying float joins: 1 (1) |
| skip step | `SkipDrawStepEffect` (13 cards, unaudited) with a step kind, a static `PlayerSubject` and a `Condition` | Eon Hub + Gibbering Descent; `PlayersSkipUntapStepEffect` (3) joins: 1 (2) |
| Desolation split | `SacrificePermanentsEffect` over a tapped-a-land-for-mana `PlayerPredicate` + `DealDamageToPlayersEffect(playersThisWay(SACRIFICE))` narrowed to a Plains by last-known subtypes | Desolation: 1 (1) |
| damage can't be prevented | `DamageCantBePreventedEffect` (4 cards) with a static source `PermanentSubject` and a combat-only flag | Questing Beast + Questing Cosplayer; the unaudited `CombatDamageCantBePreventedEffect` and `SourceDamageCantBePreventedEffect` join: 1 (2) |
| entering counters | `ControlledPermanentsEnterWithAdditionalCountersByAmountEffect` with an amount evaluated against the entering permanent | Thunderous Velocipede, Coin of Mastery, Kalain, Master Biomancer (split: plus the Mutant grant on entry): 4 (4) |
| enter untapped | one enter-untapped record with a scope | The Wandering Minstrel (by parameters onto the unaudited `ControlledLandsEnterUntappedEffect`), Archelos: 2 (2) |
| Controlled and All by parameters | existing records | Rhythm of the Wild + Uncivil Unrest (`GrantKeywordEffect(RIOT, creaturesYouControl(), nontoken)`), Celestial Dawn's lands (`GrantSubtypeEffect(PLAINS, landsYouControl(), true)`): 2 (3) |
| All singles | existing heads + one part | Terry Pin (merge with `EquipAbilitiesCanBeActivatedAtInstantSpeedEffect`), Grand Melee + Invasion Plans (`MustBlockEachCombatEffect` with a static subject, as a rule), Nightcreep (`GrantSubtypeUntilEndOfTurnEffect` with overriding), Hall of Gemstone (triage P04's land-mana batch), Shifting Sky (`GrantColorEffect` with a chosen-colour spec; `EnchantedPermanentBecomesChosenColorEffect` joins), Horizon Boughs (`UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect` over all permanents, with a planar scan): 6 (7) |

**Blocked (4):**
- Celestial Dawn's colours, Mycosynth Lattice and Painter's Servant: their colours reach spells and cards outside the battlefield. This is run 18's all-zones cause (the out-of-battlefield query redesign, outside this plan).
- Make an Example (`blocked-player-loop`): every opponent separates, you choose a pile of each, then all sacrifice together.

**Unique (23):**
- Increase: the engine-built speed trigger, Kaervek's Torch, Altar of the Pantheon, Carth the Lion, Terror of the Peaks.
- Make: Noble Ox, You're in Command, Biblioplex Tomekeeper's unprepare, Vexing Shusher.
- Opponents: Revel in Silence's loyalty half, Ashiok, Tomik's targeting, Keen-Eared Sentry.
- Players: Ashes' graveyard abilities, Stabilizer, Two Streams Facility.
- Controlled: Masako, Deepchannel Mentor + Tetsuko (already one record), Displaced Dinosaurs, Blue, Loyal Raptor.
- All: Swirl the Mists, Intangible Vibes, Everlasting Torment's wither.

**Seen, not changed** (confirmed by reading, not run):
- Seeker of Slaanesh: `CombatAttackService.getOpponentRequiredAttackTargetIds` treats it as Trove of Temptation. Its controller and their planeswalkers become the required targets, but "must attack with at least one creature" is met by any attack (another opponent, a battle). Sure of the code; matters only in multiplayer or with battles.
- Make an Example sacrifices each opponent's pile, then asks the next opponent to separate. CR 101.4 has every separation made first and the sacrifices happen at once. Sure of the code (its Javadoc says so).
- `CastingCostService.getLoyaltyAbilityCostIncrease` (Carth) and `getOpponentCastCostPerTarget` (Hinata) read `getCard().getStaticEffects()` with no face-down or lost-abilities check (CR 613.1f, 708.2a). So does the battlefield branch of `ReduceOwnCastCostIfTargetingPermanentEffect`. Sure of the code.
- `IncreaseOwnCastCostEffect` lacks the `staticAbilityZones()` STACK override that `ReduceOwnCastCostEffect` has. Impact not traced.
- `MakeTargetCopyOfTargetPermanentEffectHandler` (True Polymorph) records no `ResolvedCopyEffect` float, unlike the timed copy handlers. Unverified whether anything reads it for a permanent copy.

**Anything left.** Nothing in this chunk. The batches reach records outside it:
- Unaudited: `EmpowerNextCreatureSpellThisTurnEffect`, `ControlledLandsEnterUntappedEffect`, `SkipDrawStepEffect`, `PlayersSkipUntapStepEffect`, `CombatDamageCantBePreventedEffect`, `SourceDamageCantBePreventedEffect`, `EquipAbilitiesCanBeActivatedAtInstantSpeedEffect`, `EnchantedPermanentBecomesChosenColorEffect`, `ControllerCantCastSpellsFromHandEffect`, `ControllerCantPlayLandsFromHandEffect`, `BecomeCopyOfTargetPermanentUntilYourNextTurnEffect`.
- Unique rows that now gain a second user: `CantSearchLibrariesEffect` and `CantBeTurnedFaceUpEffect` (triage 2), `OpponentsMustAttackControllerEffect` (5m).

Cheapest fold-run starts, by parameters only: Chromatic Armor / Skeleton Scavengers, Damping Sphere, Elspeth Conquers Death / Tax Collector, Iona, riot, Celestial Dawn's lands, The Wandering Minstrel.

### Step 9, parallel triage chunk 08 (2026-11-23)

Verdicts only: no repo file was changed and the ratchet did not move. 84 ledger rows (notes `triage P08: …`) cover the chunk's rare records in Goad (6), Other (6), Play (6), Turn (6), Assign (5), Draft (5), Land (5), Random (5), Spells (5), Manifest (4), Modify (4), Note (4), Only (4), Resolve (4), Separate (4), Suspect (4), Unattach (4) and Adjust (3).

Every record was read with its handler to the end, plus the card classes that use it. For records with no handler, these readers were read:
- `AttackLegalityService`: the goading player, the only-attackers switch, the other-creatures lock.
- `CombatAttackService`'s conditional attack validators.
- `CastingPermissionService`: the chosen-name and name-restriction readers, the top-library and graveyard permissions.
- `GameActionAvailabilityService`'s graveyard play checks.
- `GameQueryService`: can't be countered, combat damage assignment, land-mana colour.
- `TurnScopedLandManaReplacements`, `TurnFaceUpCopyService`, `AsEntersInteractionService` (Ixidron), `EtbTriggerService` (spellbook drafts).
- `GameViewProjectionFactory`'s hand reveals.
- `MultiPermanentChoiceHandlerService`'s goad branch, `StandOrFallSupport`, `SchemeAbandonSupport` and `GameData.abandonScheme`.

Oracle text was checked with `get_card` for every `unique` and `blocked-*` card, and for many `step-9` cards:
- Kaima, Vislor Turlough, The War Games, Life of the Party, Rendmaw, Hot Pursuit.
- Brudiclad, Intimidation Bolt, Viashino Bey, Deceiver of Form, Shaman's Trance, Coram, The Fourth Doctor, Victor Timely, Horde of Notions.
- Dark Wings Bring Your Downfall, Gift of Doom, Vesuvan Shapeshifter, Ixidron, Showstopping Surprise, Etrata.
- Butcher Orgg, Cunning Giant, Laccolith Rig, Cloak of Confusion.
- Protean War Engine, Slimefoot, Relics of the Rubblebelt, Land Equilibrium.
- Witch Hunt, Wildfire Devils, Strax, Furnace Layer, Spider-Punk.
- Wickerwing Effigy, Sigarda's Splendor, Favored Enemy, Bell Borca, Jeweled Amulet, Ice Cauldron.
- Fight or Flight, Stand or Fall, Do or Die, Bumi.
- Snarlfang Vermin, Frantic Scapegoat, J. Jonah Jameson, Captain America.
- Talon Gates, Valgavoth's Onslaught, Write into Being, Jhoira's Timebug.

Narod, Maestros' Totally Safe Hideout, Boulder Jockey and Truth or Dare are playtest printings Scryfall doesn't list, so they were judged from their classes. Rulings read: Kaima. CR 400.7, 508.1d, 611.2c, 701.15a–d and 701.33b were checked with the rules MCP (2026-11-23).

| Family | Triaged | `step-9` | `blocked-other` | `unique` |
|---|---|---|---|---|
| Goad | 6 | 6 (1 `dead: delete`) | 0 | 0 |
| Other | 6 | 6 | 0 | 0 |
| Play | 6 | 4 | 0 | 2 |
| Turn | 6 | 2 | 1 | 3 |
| Assign | 5 | 2 | 0 | 3 |
| Draft | 5 | 3 | 1 | 1 |
| Land | 5 | 2 | 1 | 2 |
| Random | 5 | 5 | 0 | 0 |
| Spells | 5 | 2 | 0 | 3 |
| Manifest | 4 | 4 | 0 | 0 |
| Modify | 4 | 3 | 0 | 1 |
| Note | 4 | 0 | 0 | 4 |
| Only | 4 | 3 | 0 | 1 |
| Resolve | 4 | 0 | 3 | 1 |
| Separate | 4 | 4 | 0 | 0 |
| Suspect | 4 | 3 | 1 | 0 |
| Unattach | 4 | 3 | 0 | 1 |
| Adjust | 3 | 3 | 0 | 0 |
| All | 84 | 55 | 7 | 22 |

**Dead:** `GoadChosenCreatureDamagedPlayerControlsEffect`. No card names it, so its handler never runs, and the `GoadDamagedPlayerControls` choice context it begins is never made. Delete the record, its handler, that context and `MultiPermanentChoiceHandlerService.handleGoadDamagedPlayerControls`. Grenzo, Havoc Raiser uses `GoadTargetCreatureUntilNextTurnEffect`.

**Fold batches** (survivor or shared component → records of this chunk, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| goad duration | `GoadTargetCreatureUntilNextTurnEffect` + a `Duration` component (`PERMANENT`, `WHILE_SOURCE_REMAINS`, "while its current controller controls it") + subjects `createdThisResolution()`, `source()`, `controlledBy(...)` + a new `StepKind.GOAD` (Kaima's "for each creature goaded this way") | Life of the Party + Rendmaw (one record), The War Games, Hot Pursuit, Kaima, Vislor Turlough: 5 (6). Vislor supersedes triage 1's static-goad note: its goad is made by a resolution |
| copier subject (triage 6) | `BecomeCopyOfSubjectEffect` + a copier subject | Deceiver of Form (revealed card, until end of turn), Brudiclad (chosen token, no duration), Infinite Reflection (`attached()`): 3 (3) |
| can't-attack set | `CantAttackThisTurnEffect` over `except(controlledBy(eachPlayer(), creature), target())` as a rule (CR 611.2c) | Intimidation Bolt: 1 (1). Chunk 06's `CreaturesCantAttackThisTurnEffect` joins |
| conditional attack requirement | `MustAttackIfAnotherCreatureAttacksEffect` (chunk 07) with a trigger set and a required set (CR 508.1d) | Viashino Bey: 1 (1). Chunk 06's Magnetic Web joins |
| from-zone restriction | `PlayerRestrictionEffect` + a from-zone `PlayerRestriction` | Shaman's Trance: 1 (1). Chunk 05's Avatar's Wrath joins |
| graveyard-permission statics (triage P01) | `CastSpellFromGraveyardOncePerYourTurnEffect` + `PlayLandsFromGraveyardEffect`, each taking the existing graveyard scope as a component | Coram: 1 (1) |
| graveyard cast-now (triage P01) | `CastCardFromGraveyardEffect` over `targetCard(...)`, free, land play, exile instead | Horde of Notions + Victor Timely (one record): 1 (2) |
| hand reveal | `PlayWithHandsRevealedEffect` + a `PlayerSubject`; floated through `GrantStaticEffectToPlayerEffect` | Enduring Renewal, Truth or Dare: 2 (2). Chunk 05's Stromgald Spy joins |
| scheme abandon | `SacrificeSelfEffect`'s scheme branch (`SchemeAbandonSupport`) | Dark Wings Bring Your Downfall: 1 (1) |
| turn face up | `TurnSourceFaceUpEffect` with `target()` on one path that checks prevention | Showstopping Surprise: 1 (1). Unblocks 5m's `TurnTargetCreatureFaceUpEffect` (3) and `TurnTargetFaceUpEffect` (2) |
| assign no combat damage | `AssignNoCombatDamageEffect` + a `PermanentSubject` replacing its flags | Laccolith Rig, Cloak of Confusion (split with `DiscardEffect` at random): 2 (2) |
| spellbook drafts | `DraftCardFromSpellbookEffect` / `DraftCardFromSpellbookToExileEffect`, by parameters | Raddic + Recruit Instructor (one record), Slimefoot, Protean War Engine: 3 (4) |
| land mana (triage P04) | `ReplaceLandManaWithColorEffect`, floated | Deep Water, Harvest Mage: 2 (2) |
| choose one at random | `ChooseOneAtRandomEffect` (run 17 unique) | Cult of Skaro + Typhoid Mary (one record): 1 (2) |
| random target | a "chosen at random" option on the target spec, replacing each record's `targetChosenAtRandom()` | Witch Hunt (`GainControlOfTargetEffect.gainedBy`), Furnace Layer (`DiscardCardThenEffect`): 2 (2). The unaudited `DealDamageToRandomOpponentEffect`, `SourceFightsRandomOpponentCreatureEffect` and `DealDamageToRandomAnyTargetEffect` join |
| random player | `ChoosePlayerAtRandomEffect` + `playersThisWay(CHOOSE)` | Wildfire Devils (+ triage 7's `CopyImprintedCardAndMayCastCopyEffect.of`), Strax (reflexive fight): 2 (2) |
| chosen-name prohibition | `SpellsWithChosenNameCantBeCastEffect` (6 cards), reading every chosen name, + `landsToo` | Null Chamber: 1 (1) |
| can't be countered | `SpellsCantBeCounteredEffect` (survivor) + a `PlayerSubject` | Lier + Root Sliver (one record): 1 (2). With 5m's step-9 trio |
| manifest from another library | `ManifestTopCardEffect` + a library-owner subject | Orochi Soul-Reaver + Thieving Amalgam (one record): 1 (2). Re-triage 5m's unique `CloakTopCardOfDamagedPlayerLibraryEffect` with it |
| manifest from hand | `ChooseCardEffect` + `PutCardsOntoBattlefieldEffect` face down as a manifest | Scroll of Fate: 1 (1) |
| repeated manifest dread | `RepeatEffect(X, ManifestDreadEffect)`, with that record recording MANIFEST, + counters on `thisWay(MANIFEST)` | Valgavoth's Onslaught: 1 (1) |
| three-way split (triage 4) | `LookAtTopCardsHandTopBottomEffect` + a manifest destination | Write into Being: 1 (1) |
| matching-spell predicates (triage 1) | `ReduceCastCostForMatchingSpellsEffect` / the increase head + a drawn-this-turn predicate | Captain Eberhart: 1 (1) |
| keyword costs (triage 1) | flashback and morph cost changes | Catalyst Stone, Exiled Doomsayer: 2 (2) |
| only attackers | `OnlyMatchingCreaturesCanAttackThisCombatEffect` (engine-built survivor) + a `Duration` | Bumi, en-Vec's engine-built record, the survivor itself: 3 (1). Re-triage 5m's `OnlySource…` and `OnlyTarget…` uniques with it |
| piles | a new "separate [set] into two piles, [player] chooses one" step recording CHOOSE, then the disposition over `thisWay(CHOOSE)` | Fight or Flight, Stand or Fall, Do or Die, Liliana of the Veil: 4 (4). Step 8's blocked `ExileTargetGraveyardCardsAndSeparateIntoPilesEffect` is kin (card piles) |
| suspect | `SuspectEffect` | J. Jonah Jameson (`target()`), Presumed Dead + It Doesn't Add Up (`returnedThisResolution()`), Snarlfang Vermin (+ `PerpetuallyGrantAbilitiesEffect`): 3 (4) |
| unattach | `UnattachEquipmentFromTargetPermanentsEffect` (survivor) + an Equipment subject | Disarm + Fulgent Distraction, Carry Away + Tamiyo's Compleation, the engine-built remembered unattach: 3 (4) |
| counter adjust | `AdjustEachCounterKindOnTargetEffect` (survivor) + a one-chosen-kind option + a suspended-card target | Dramatist's Puppet + Quarry Hauler, Clockspinning + Jhoira's Timebug: 2 (4) |
| exiled-card counters (triage 4) | the one counter store and its removal step | Dust of Moments + Talon Gates (one record): 1 (2) |

**Blocked (7):**
- Etrata's granted ability (`TurnSourceFaceUpOrExileAndMayCastEffect`), `blocked-other`. "If you can't" needs a turn-face-up step result and the one turn-face-up path.
- Relics of the Rubblebelt, `blocked-other`. No step records the drafted cards.
- Boulder Jockey's `LandDropCost`, `blocked-other`. Step 10's cost-combinator merge, with `MayPayLandDropEffect`.
- `ResolveDelayedPermanentActionEffect` and `ResolvePendingExileReturnEffect`, `blocked-other`. They wait for the delayed-mechanism unification onto `RegisterDelayed*`.
- `ResolveRiderForPlayerEffect`, `blocked-other`. It goes when `DestroyTargetPermanentThenEffect`'s 14 cards and Pain's Reward split.
- Frantic Scapegoat, `blocked-other`. No verb removes the suspected designation.

**Unique (22):**
- Word of Command's marker.
- The Fourth Doctor: one shared land-or-cast use per turn.
- Gift of Doom, Vesuvan Shapeshifter, Ixidron.
- Butcher Orgg, Cunning Giant, Narod.
- `DraftCardIntoHandEffect`: the hand twin of run 30's `ConjureCardOntoBattlefieldEffect`.
- Land casualty, Land Equilibrium.
- Spider-Punk's abilities half, City in a Bottle, Cornered Market: the last two already share `CardNameRestrictionEffect`.
- Wickerwing Effigy.
- Sigarda's Splendor, Jeweled Amulet / Ice Cauldron (already one record), Bell Borca, Favored Enemy.
- Stand or Fall's block-side payload, the rad-counter trigger, Captain America's unattach cost.

**Seen, not changed** (confirmed by reading the code, not run):

- **Dark Wings Bring Your Downfall.** "Abandon this scheme" is `TurnFaceDownCommandZoneCardEffect`, which only adds the card to `faceDownCommandZoneCards`. It never puts the scheme on the bottom of its owner's scheme deck (CR 701.33b) and logs nothing. `SacrificeSelfEffect`'s scheme branch (`SchemeAbandonSupport`) does both. Sure of the code; Archenemy only.
- **`DraftCardIntoHandEffect`** logs "drafts <name> into their hand" to every player, although the card goes to a hidden hand. Sure of the code. Whether a draft is hidden is next to the owner's hidden-zone logging question.
- **Stand or Fall.** It serves only `getOpponentId` where the text says "for each defending player". `OnlyChosenCreaturesCanBlockEffect` is floated on every player, so several piles would bar each other's creatures. Sure of the code; multiplayer only.
- **Wickerwing Effigy.** "It becomes a black Bird … base 1/1" is stored as on-entry fields, so the spell on the stack keeps its colours and types. Only target legality reads the added colour. Fairly sure; low impact.
- **Favored Enemy.** It reads the first opponent's library, with no choice of "an opponent". Sure; multiplayer only.
- **`DraftCardFromSpellbookToHandEffectHandler`** throws on an unknown set or unimplemented printing, where the survivor omits it. Sure of the code; I did not check whether Raddic's or Recruit Instructor's printings are all implemented.
- **`GameData.notedMana`** (Ice Cauldron, Jeweled Amulet) is keyed by card id, so a note survives a zone change (CR 400.7). Sure; low impact, because a charge counter is needed to read it.
- **Snarlfang Vermin.** "That creature" is declared as a benign creature target spec. Whether the trigger door checks hexproof or protection on it was not traced. Low confidence.
- **Laccolith Rig.** Its assign-none step reads the Aura's current attachment, while its damage step reads `attachedAtTrigger()`. Low impact.
- **Disarm's handler** fires `EquipmentUnattached` before unattaching, reads printed subtypes, and reads all the entry's targets. Low impact.
- **Kaima.** It counts creatures you had already goaded as "goaded this way", although CR 701.15d says goading again has no effect. Unsure of the reading.
- **Vislor Turlough.** Its goad is not gated on "if you do". Practically unreachable.
- **Protean War Engine's handler** builds its spellbook by hand, setting names, costs and text in engine code, with Steel-Plume Marshal's ability written inline. Not a rules defect.
- **`SuspectTargetCreatureEffect`** adds a `SuspectedEffect` floating effect that nothing reads.

**Left.** Nothing in chunk 08. Re-triage these rows when their batch is folded:
- `TurnTargetCreatureFaceUpEffect`, `TurnTargetFaceUpEffect` (5m blocked).
- `CloakTopCardOfDamagedPlayerLibraryEffect` (5m unique).
- `OnlySourceCreatureCanAttackThisCombatEffect`, `OnlyTargetCreaturesCanAttackThisCombatEffect` (5m unique).
- `ChooseOneAtRandomEffect` (run 17 unique).
- Triage 1's `GoadSourceCreatureWhileControllerControlsItEffect` mention in the static-goad batch.
- 5m's can't-be-countered trio.

### Step 9, parallel triage chunk 09 (2026-11-23)

Verdicts only. No repo file changed and the ratchet did not move. The chunk has 84 ledger rows (notes start `triage P09: …`), read against `546a19ede6`.

What was read:
- Every record and every handler to its end. Where a record has no handler, its readers were read instead: `CastingCostService` (free equip, free power-up, floating attack taxes), `AbilityActivationService` (the two cycling reads), `DrawService` (Reed Richards), `CastingPermissionService` (Wardscale Dragon, Phyrexian Censor, the spell limit), `GameQueryService` (prevention, damage replacement, counter, supertype and own-effects readers), `LayerSystemService` (land types), `AdditionalSpellCostService` (escalate), `CombatDamageService`, `ActionEvents.completeDungeonRoomIfReady`, `BattlefieldPlacementService` (enter-tapped floats), `PredicateEvaluationService` (the chosen-name and shared-creature-type predicates) and `AttackLegalityService`.
- Every card class that uses a record in the chunk.
- Oracle text with `get_card` for every `unique` and `blocked-*` card, and for most `step-9` cards.
- Stack of Paperwork, A Good Thing, Map to Lorthos's Temple and Halving Season were read from `card-data-cache`, because they are MB1/MB2 playtest cards that Scryfall doesn't have.
- Rulings: Portal Mage, Karn, Scion of Urza, Transmute Artifact.
- CR numbers checked with `get_rule`: 101.4, 400.7, 603.7d, 603.10a, 612.2, 613.1d, 616.1, 702.42a, 702.120a, 708.2a.

| Family | Triaged | `step-9` | `blocked-other` | `blocked-player-loop` | `blocked-step-model` | `unique` |
|---|---|---|---|---|---|---|
| Chosen | 3 | 3 | 0 | 0 | 0 | 0 |
| Combat | 3 | 1 | 0 | 0 | 0 | 2 |
| Complete | 3 | 0 | 0 | 0 | 1 | 2 |
| Defending | 3 | 3 | 0 | 0 | 0 | 0 |
| Drain | 3 | 3 | 0 | 0 | 0 | 0 |
| Escalate | 3 | 3 | 0 | 0 | 0 | 0 |
| First | 3 | 2 | 0 | 0 | 0 | 1 |
| Free | 3 | 3 | 0 | 0 | 0 | 0 |
| Global | 3 | 3 | 0 | 0 | 0 | 0 |
| Halve | 3 | 2 | 0 | 0 | 0 | 1 |
| Karn | 3 | 2 | 0 | 0 | 0 | 1 |
| Lands | 3 | 3 | 0 | 0 | 0 | 0 |
| Limit | 3 | 3 | 0 | 0 | 0 | 0 |
| Lose | 3 | 1 | 0 | 0 | 0 | 2 |
| Mana | 3 | 2 | 0 | 0 | 0 | 1 |
| Morphic | 3 | 0 | 0 | 3 | 0 | 0 |
| Multiply | 3 | 3 | 0 | 0 | 0 | 0 |
| Nihiloor | 3 | 1 | 2 | 0 | 0 | 0 |
| Own | 3 | 2 | 0 | 0 | 0 | 1 |
| Permanents | 3 | 2 | 0 | 0 | 0 | 1 |
| Perpetual | 3 | 0 | 3 | 0 | 0 | 0 |
| Power | 3 | 2 | 0 | 0 | 0 | 1 |
| Psychic | 3 | 1 | 0 | 0 | 0 | 2 |
| Record | 3 | 3 | 0 | 0 | 0 | 0 |
| Reselect | 3 | 2 | 0 | 0 | 0 | 1 |
| Returned | 3 | 3 | 0 | 0 | 0 | 0 |
| The | 3 | 2 | 0 | 0 | 0 | 1 |
| Transmute | 3 | 3 | 0 | 0 | 0 | 0 |
| All | 84 | 58 | 5 | 3 | 1 | 17 |

**Fold batches** (survivor or component → records, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| each-creature bite | `TargetCreatureDealsPowerDamageToEachOtherCreatureEffect` (4 cards, unaudited) gains a dealer `PermanentSubject` and a recipient filter | Widespread Brutality (the amassed Army, read from the amass step's chosen permanent), Too Greedily, Too Deep (`thisWay(RETURN)`): 2 (2) |
| chosen dealer | `ChoosePermanentsEffect` + `TargetDealsPowerDamageToTargetEffect(thisWay(CHOOSE), target())`; the survivor needs that dealer shape | Crush Underfoot: 1 (1) |
| can't-be-prevented | `DamageCantBePreventedEffect` (4, unaudited) with a damage scope (all / combat / combat by your creatures) | Frenzied Baloth; `ControlledCreaturesCombatDamageCantBePreventedEffect` (2, unaudited) joins: 1 (1) |
| Medomai, by parameters | `ConditionalEffect(NotCondition(ControllerCastSpellThisTurn(chosen name)), RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(...))` | Medomai's Prophecy: 1 (1) |
| static cast restriction (P03) | `OpponentsCantCastSpellsMatchingPredicateEffect` with a static "player the source attacks" subject (used by this card only: weigh it) | Wardscale Dragon: 1 (1) |
| defending-player choices | `ChoosePermanentsEffect.chosenBy(defendingPlayer())` + `MustBlockTriggeringAttackerEffect` with a blocker subject; + `ExileTargetPermanentEffect(thisWay(CHOOSE))` for P02's opponent-chosen batch | Crashing Boars, Bane of Bala Ged: 2 (2) |
| land-mana drain | `DrainTargetPlayersLandManaEffect` (survivor) with a `PlayerSubject`; its handler already runs the delayed-{C} branch | Drain Power, Pygmy Hippo: 2 (2) |
| drain, by parameters | `LoseLifeEffect` (`controllerGainsLifeLost`; for Tezzeret, `Scaled` + `GainLifeEffect`) | Foul-Tongue Shriek, Tezzeret, Agent of Bolas: 1 (2) |
| modal additional cost | `EscalateManaCost` (31, unaudited) with a per-mode cost and an escalate / entwine kind (`EntwineManaCost`, 5, joins); cost-shape | Collective Brutality, Betrayal of Flesh + Solar Tide (Entwine cards on the escalate record), Collective Effort: 3 (4) |
| free ability cost | a new "pay {0} rather than the [equip / power-up / cycling] cost" static with first-each-turn and your-turn options and a `Condition`; cost-shape | Bruenor, Kíli, Advancing the Spirit, Gavi, New Perspectives: 5 (5) |
| floated taxes | `RequirePaymentToAttackEffect` (12) / `RequirePaymentToBlockEffect` floated with a `Duration`, plus a life kind; the Require*ThisTurn / UntilNextTurn makers fold onto `GrantStaticEffectToPlayerEffect`; cost-shape | the three Global* records: 3 (engine-built) |
| counter placement (T5/T6) | triage 5's counter-placement survivor with a halving operation, for counters an opponent puts | Halving Season + Vorinclex: 1 (2) |
| token count | `MultiplyTokenCreationEffect` (12) with a halving operation and an opponents'-tokens scope | Halving Season: 1 (1) |
| Karn's silver counters | the reveal / opponent-choose / hand / exile-with-counter steps; `ChooseCardEffect` over `exiledCards(silver)` + `PutCardsIntoHandEffect`; the silver store joins T4's store for counters on exiled cards | Karn, Scion of Urza: 2 (1) |
| land types | `GrantBasicLandTypeToTargetEffect` (26, unaudited) with a `PermanentSubject` | Vision Charm (needs a land-type choice made at resolution; weigh it), Elsewhere Flask + Terraformer, Energybending: 3 (4) |
| static land-type setter | `NonbasicLandsBecomeTypeEffect` (4) with a land predicate; `ControlledLandsBecomeTypeEffect` and `BasicLandsOfChosenTypesBecomeTypeEffect` join | Conversion + Glaciers: 1 (2) |
| land mana colour | `AllLandsProduceChosenColorUntilEndOfTurnEffect` with a land filter and a fixed colour | Chaos Moon: 1 (1) |
| spell limits | `LimitSpellsPerTurnEffect` (11): by parameters for Phyrexian Censor; floated with an amount fixed at resolution for Irencrag Feat | 2 (2) |
| player-floated statics (P06) | `GrantStaticEffectToPlayerEffect` with `MaximumCombatCreaturesEffect` / `EnterPermanentsOfTypesTappedEffect.matching` | Mirri, Due Respect + Nahiri's Lithoforming: 2 (3) |
| type removal | `LoseAllLandTypesEffect` (4) widened to subtypes and a supertype, floated with a `Duration` | Haunted Plate Mail + Werewolf Pack Leader, Melting + Aeve: 2 (4) |
| mana rider | `ManaCopyRiderEffect` (survivor); Primal Wellspring's `INSTANT_SORCERY_COPY` restriction moves to it; P06's haste rider joins | Pyromancer's Goggles + Sunken Palace: 1 (2) |
| by parameters | `AwardManaEffect(PermanentCount(shares a creature type with triggering))`; `GainLifeEffect` + `LoseLifeEffect(ownerOf(triggering()))` | Mana Echoes, Nihiloor's attack trigger: 2 (2) |
| floated doublers (T6) | triage 6's doublers with a multiplier, floated | Jeska, Isengard Unleashed, Goblin Goliath: 3 (3) |
| crew power | `PowerBoostForCrewAndSaddleEffect` (20) with a set of cost kinds; the two use-toughness records join | Stoic Star-Captain: 1 (1) |
| indefinite boost | card uses move to `BoostTargetCreatureEffect` + `GrantKeywordEffect` with `PERMANENT`; the record stays as the boost payload | Riding the Dilu Horse, Xathrid Gorgon: 1 (2) |
| life exchange | `ConditionalEffect(life difference ≤ 5, ExchangeControllerAndTargetPlayerLifeTotalsEffect)` | Psychic Transfer: 1 (1) |
| step-result reads | `HappenedThisWay.toACard(DISCARD / RETURN, …)`, `NoteAmountEffect(TargetSpellManaValue)` | Reckless Handling, Cemetery Recruitment + Jared, Hurl into History + Overwhelming Intellect: 3 (5) |
| reselect | `ReselectAttackingCreatureAttackTargetEffect` with a subject and the reselection query | Portal Mage + Capricopian, Misleading Signpost: 2 (3) |
| returned-permanent reflexive | `ConditionalEffect(returned(), QueueReflexiveAbilityEffect` about `thisWay(RETURN))` | Back for More, Jailbreak: 2 (2) |
| The Master split | exile + `ChoosePlayerEffect` + `TargetPlayerChoosesOneEffect.villainousChoiceFacedBy` | The Master, Gallifrey's End: 1 (1) |
| guess | `MasterOfPredicamentsEffect` (unaudited) with a threshold amount, a guesser and an "if you don't cast" step | The Seventh Doctor: 1 (1) |
| SEARCH step (T2) | sacrifice + `SearchLibraryEffect` recording SEARCH + mana-value branch + `MayPayManaEffect(X)`; cost-shape | Transmute Artifact: 3 (1) |

**Blocked (9):**
- Morphic: `MorphicTideEffect` and its two continuations are `blocked-player-loop`. Every player shuffles, reveals "that many" and puts the revealed cards in type batches all at once. Glimpse of Tomorrow alone could be split with existing steps.
- Nihiloor's enter trigger and its engine-built tap-and-steal record are `blocked-other`: they need one reflexive ability per opponent, naming each player.
- Perpetual's 3 records are `blocked-other`: they make perpetual changes to cards in a hand or library, which waits on the owner's hidden-zone logging question.
- `CompleteAmassGoblinsEffect` is `blocked-step-model`: it is the amass continuation after token creation pauses for a choice.

**Unique (17):**
- Combat: Stack of Paperwork's rule and its engine-built stack object.
- Complete: Map to Lorthos's Temple's checklist, the dungeon-completion marker.
- First: Reed Richards.
- Halve: Ghosts of the Innocent.
- Karn: Karn Liberated.
- Lose: Soulgorger Orgg, Great Fang Chroniclers.
- Mana: Mana Clash.
- Own: A Good Thing.
- Permanents: Disciple of Caelus Nin.
- Power: Kang the Conqueror's power-up marker.
- Psychic: Psychic Battle and its retarget answer.
- Reselect: Grip of Chaos.
- The: The Valeyard.

**Seen, not changed** (confirmed by reading, not run):
- Portal Mage and Capricopian: `ReselectAttackingCreatureAttackTargetEffectHandler` offers `getValidAttackTargetIds`, so a planeswalker that can't be attacked is left out. Portal Mage's ruling says reselecting ignores all restrictions, and Misleading Signpost's handler uses `getValidReselectionTargetIds`. Sure; low impact.
- Mirri, Weatherlight Duelist: only the defending player is limited, but the Oracle text says "each opponent". Sure; multiplayer only.
- Karn, Scion of Urza +1 hands the choice to the first other seat. Wardscale Dragon's defender falls back to the first other seat when no attack target is recorded. Sure; multiplayer only.
- `TextChangeTransformer` has no `LandsOfSubtypeBecomeTypeEffect` case, so a land-type text change (CR 612.2) doesn't rewrite Conversion or Glaciers. Blood Moon's record is rewritten. Sure; low impact.
- Bane of Bala Ged exiles the chosen permanents one at a time rather than together (CR 603.10a look-back). Fairly sure; low impact.
- Glimpse of Tomorrow and Morphic Tide put each Aura onto the battlefield in its own continuation, after the earlier batch's enter processing, instead of as one batch. Medium sureness; low impact.
- Jailbreak's "when that permanent enters" record takes the returned permanent as its source, where CR 603.7d makes the spell the source. No visible effect was found. Not verified.
- Notes, not defects:
  - Betrayal of Flesh and Solar Tide have Entwine but use `EscalateSacrificeCost`. With two modes the payment is the same (CR 702.42a vs 702.120a).
  - `ManaCopyRider.INSTANT_OR_SORCERY_SPELL` is set by no card.

**Anything left.** No record in this chunk is without a row. The batches reach records outside the chunk:
- **Rows to re-triage:**
  - Ethersworn Canonist's `CantCastAdditionalNonartifactSpellsEffect` (triage 2, `unique`) belongs with `LimitSpellsPerTurnEffect`.
  - The "not fused" step-8 `unique` rows of `PowerBoostForCrewAndSaddleEffect`, `UseToughnessForCrewAndSaddleEffect` and `ExchangeControllerAndTargetPlayerLifeTotalsEffect` are now batch survivors or members.
- **Unaudited records the batches touch:**
  - `TargetCreatureDealsPowerDamageToEachOtherCreatureEffect` (4)
  - `DamageCantBePreventedEffect` (4)
  - `ControlledCreaturesCombatDamageCantBePreventedEffect` (2)
  - `MustBlockTriggeringAttackerEffect` (2)
  - `EscalateManaCost` (31)
  - `EntwineManaCost` (5)
  - `RequirePaymentToAttackEffect` (12)
  - `RequirePaymentToBlockEffect` and the five Require*Turn makers
  - `MultiplyTokenCreationEffect` (12)
  - `GrantBasicLandTypeToTargetEffect` (26)
  - `NonbasicLandsBecomeTypeEffect` (4)
  - `ControlledLandsBecomeTypeEffect`
  - `BasicLandsOfChosenTypesBecomeTypeEffect`
  - `AllLandsProduceChosenColorUntilEndOfTurnEffect`
  - `LimitSpellsPerTurnEffect` (11)
  - `MaximumCombatCreaturesEffect` (8)
  - `EnterPermanentsOfTypesTappedEffect` (23)
  - `LoseAllLandTypesEffect` (4)
  - `UseToughnessForStationEffect`
  - `MasterOfPredicamentsEffect`
  - `AddOnePlusOneCountersEffect` (5)

Suggested fold order:
1. The by-parameter rows: Foul-Tongue Shriek and Tezzeret, Phyrexian Censor, Mana Echoes, Nihiloor's attack, Reckless Handling, Medomai's Prophecy.
2. The reselect pair, which also fixes Portal Mage's restriction defect.
3. The free-ability-cost batch.
4. The land-type batch.
5. The modal additional-cost batch, together with step 10's cost-pair weighing.

### Step 9, parallel triage chunk 10 (2026-11-23)

Only verdicts this time. No repo file changed; the plan and ledger were read at `4d3fe3f4a6`, and the ratchet did not move. The 84 ledger rows (notes `triage P10: …`) are in /tmp/claude-1000/-mnt-ramdisk-magical-vibes/300dbf19-8a01-4cdb-8671-d50e7ccdb4f0/scratchpad/ptriage/out-10/ledger-rows.csv.

**What was read**
- Every record, its handler read to the end, and every card class that uses it.
- For records with no handler, their readers:
  - `DrawService`: win on an empty-library draw, the Enduring Renewal replacement, the miracle reveal
  - `LifeSupport` and `EnduringAngelLifeReplacement`
  - `GameQueryService`: `canPlayerLifeChange`, the dying-trigger check, the loyalty lock, the tap-ability lock, `canCastSpellFromZone`, the loyalty damage replacement
  - `AdditionalSpellCostService` (Chorus), `BattlefieldPlacementService` (the as-enters keyword, token and graveyard counter replacements, Storyweave's entry)
  - `CombatBlockService`, `CreatureCountSupport`, `ZoneChangeCounterSupport`, `PermanentRemovalService`'s hand markers
  - `LifeGainReplacements`, `PlayerDamageFinalReplacements`, `PreventionSupport.collectAllDamageSourceIds`
  - `BattleDefeatSupport`, `EpicService`, `ParadigmService`, `ForageEffectHandler`, `AirbendSupport`, `GameOfChaosSupport`, the scheme abandon may handler
- Siblings compared: the vote handlers (Coercive Portal, Secret Council, Will of the Council), `AirbendTargetPermanentEffect`, `ForcedCostOrElseEffect`, `PutCounterOnControlledCreatureCost`, `DrawDiscardAndConniveEffect`, `ControlTargetPlayerNextTurnEffect`, `ExileTopCardsAndMayCastSpellsEffect`, `DealDamageToAttackedTargetEffect`, `TargetDealsPowerDamageToTargetEffect`, `PlayersCantCastSpellsFromZonesEffect`, `RequirePaymentToBlockEffect`, `ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect`, `ControlledPermanentsEnterWithAdditionalCountersEffect`, `ChooseOneEffect.ModeRestriction`, `FixedIfCondition`, the condition list (`EventValueAtLeast`, `AllOf`, `WaterbendCostPaid`).
- Oracle text: checked with `get_card` for every `unique` and `blocked` card and for most `step-9` cards. The MB1/MB2 playtest cards (Five Kids in a Trenchcoat, Domesticated Mammoth, Mirrored Lotus, Champion of the Hare-ish, Duelists' Convocation International, Planeswalkerificate, Panglacial Shinobi, No-Regrets Egret) are in no feed, so their class Oracle was used.
- Rulings: Lethal Scheme, Chorus of the Conclave, Deification.
- Rules checked with `get_rule`: CR 109.5, 118.12, 118.12a, 119.4, 120.3c, 400.7, 401.4, 603.12, 608.2b, 608.2h, 613.1f, 614.1c, 700.2e, 701.32b, 701.33b, 701.38a, 701.50a, 701.50b, 701.50c, 702.50a, 702.94a, 702.192a, 708.2a. CR 310.11b doesn't exist; it isn't cited.

| Family | Triaged | step-9 | unique | blocked-other |
|---|---|---|---|---|
| Vote | 3 | 3 | 0 | 0 |
| Win | 3 | 2 | 1 | 0 |
| Abandon, Active, Airbend, Arcane, Attacking | 10 | 7 | 3 | 0 |
| Battle, Blight, Buddy, Card, Change | 10 | 4 | 5 | 1 |
| Conditional, Connive, Control, Count, Creature | 10 | 6 | 4 | 0 |
| Delayed, Enduring, Enter, Epic, Equipped | 10 | 4 | 6 | 0 |
| Eye, Forage, Game, Garth, Graveyard | 10 | 3 | 7 | 0 |
| Initialize, Library, Life, Matching, Miracle | 10 | 5 | 5 | 0 |
| Nefarious, Next, No, Noncreature, Paradigm | 10 | 2 | 8 | 0 |
| Permanent, Planeswalker, Preserve, Reflect | 8 | 3 | 5 | 0 |
| All | 84 | 39 | 44 | 1 |

**Fold batches** (records / cards)
- **two-option vote** — a new `VoteEffect`: two named options, a payload when the first gets more votes, an otherwise payload, ties to otherwise, voters in turn order from you (CR 701.38a). 2 records / 2 cards.
  - Split Decision: `CounterSpellEffect` / `CopySpellEffect`.
  - Trial of a Time Lord: guilty puts `exiledWithSource()` on the bottom, each owner ordering their cards (CR 401.4).
  - Joins chunks 12 and 14's `CoercivePortalEffect`, `GraceOrCondemnationEffect`, `TyrantsChoiceEffect` and `PleaForPowerEffect`, which have the same handler shape.
- **permanent vote** — `WillOfTheCouncilEffect` (3 cards, unaudited) becomes a vote step over permanents that records the most-voted as CHOOSE. 1 record / 1 card.
  - Vault 11: a destroy over `thisWay(CHOOSE)`, else each player draws.
  - Keep the death replacements asked before `finishVoting`.
- **event-value win, by parameters** — `ConditionalEffect(EventValueAtLeast…, WinGameEffect)`. 2 records / 1 card.
  - Call the Spirit Dragons' engine-built record.
  - Luck Bobblehead: `AllOf(EventValueAtLeast(7), NotCondition(EventValueAtLeast(8)))`.
- **active-player puts, by parameters** — 2 records / 2 cards.
  - Braids, Conjurer Adept: `ChooseCardEffect` over `cardsInHands(activePlayer(), …)` + `PutCardsOntoBattlefieldEffect`.
  - Oath of Druids: `RevealCardsFromTopUntilEffect` + `PutCardsOntoBattlefieldEffect(revealedThisWay(creature))` + the rest into the graveyard.
- **airbend** — `AirbendTargetPermanentEffect` (11 cards) gains a `PermanentSubject`. 2 records / 2 cards.
  - Avatar's Wrath: `except(allCreatures(), target())`, as one exile event.
  - Aang, Swift Savior: target creature or `targetSpell()`. Only Aang airbends a spell: weigh it.
- **card-copy-cast** — triage 7's `CopyImprintedCardAndMayCastCopyEffect.of(exiledWithSource())`, free. 2 records / 2 cards.
  - Arcane Bombardment: after `ExileCardAtRandomEffect`.
  - Eye of the Storm: after `ExileTargetSpellEffect` over the triggering spell.
- **blight costs** — cost-shape: `PutCounterOnControlledCreatureCost` (16 cards) gains a `DynamicAmount` count. 2 records / 3 cards: Soul Immolation, Gristle Glutton, Blighted Nightmare.
- **connive** — `DrawDiscardAndConniveEffect` (41 cards, run 26 unique) gains a `PermanentSubject` replacing its flags. 2 records / 2 cards: Lethal Scheme, Change of Plans.
  - Several connivers act one at a time in their controller's order (CR 701.50c).
- **player control** — `ControlTargetPlayerNextTurnEffect` (6 cards) gains a `Duration` and a controlling `PlayerSubject`. 2 records / 2 cards.
  - Secret of Bloodbending: next turn or next combat through `WaterbendCostPaid`.
  - Cruel Entertainment: two crossed applications.
- **life lock** — survivor `LifeTotalCantChangeEffect` (Platinum Emperion). 2 records / 3 cards.
  - Teferi's Protection and Perch Protection: `GrantStaticEffectToPlayerEffect.untilYourNextTurn(…)`.
  - The reader was compared: `canPlayerLifeChange` must read the floated copy.
- **cost-shape singles** — 2 records / 2 cards; weigh against step 10's cost-combinator merge.
  - Fear My Authority: `ForcedCostOrElseEffect` over a discard-or-3-life choice, else `SacrificeSelfEffect`, which is the scheme abandon.
  - Divert: `MayPayManaEffect({2})` by the spell's controller, else `ChangeTargetOfTargetSpellWithSingleTargetEffect`.
- **joins other chunks' batches** — 1 record / 1 card each unless noted.
  - Hushbringer: P02's entering-trigger batch gains a dies kind.
  - Heat Wave: P07's floated-tax batch, block side, gains a life kind and filters.
  - Katabatic Winds: survivor of P03's tap-ability lock pair.
  - Hithlain Rope and Bucknard's Everfull Purse: P06's leader-gains-control batch with a seat-direction `PlayerSubject`. 1 record / 2 cards.
  - Soulless Jailer: P05's static zone-play batch gains a spell filter.
  - My Crushing Masterstroke: P07's must-attack batch with `ownerOf(target())`.
  - Reflect Damage: P03's source-keyed redirect batch.
  - Library of Lat-Nam: triage 3's opponent-chooses-mode `ChooseOneEffect`, with `FatalLoreEffect`.
  - Blade of Shared Souls: triage 6's copier subject on `BecomeCopyOfSubjectEffect`, `WHILE_REMAINS_ATTACHED`.
- **other single folds** — 1 record each.
  - Muck Drubb: `ChangeTargetOfTargetSpellToSourceEffect` gains a new-target subject; 5m's `ChangeTargetOfTargetSpellToEnchantedCreatureEffect` joins. Weigh.
  - Brood Astronomer: `AwardAnyColorManaEffect(FixedIfCondition(…, 3, 1))`.
  - Mirkwood Trapper: `ChoosePermanentsEffect.chosenBy(attacking player)` + `BoostTargetCreatureEffect` over `thisWay(CHOOSE)`.
  - Protean Hydra: `RegisterDelayedStepTriggerEffect.aboutSourceAtNext`.
  - Epic Experiment: `ExileTopCardsAndMayCastSpellsEffect` gains an uncast-into-graveyard option. Weigh.
  - Mage Slayer: `DealDamageToAttackedTargetEffect` gains a dealer subject.
  - Dearly Departed: `ControlledPermanentsEnterWithAdditionalCountersEffect` gains a graveyard form.
  - Garruk Relentless: split into `DealDamageToTargetCreatureEffect(3)` + `TargetDealsPowerDamageToTargetEffect(target(), source())`.
  - The forage follow-up (engine-built, no card): `QueueReflexiveAbilityEffect`.

**Blocked (1)**
- Battle Angels of Tyr, `blocked-other`. The split needs three Conditions on the damaged player: more cards in hand, more lands and more life than each other player. Subjects inside Conditions are outside this plan, and each Condition would be used by this card alone.

**Unique (44)**
- Engine-built records:
  - siege defeat
  - epic's upkeep copy
  - miracle ×2
  - paradigm ×2
  - the two hand markers
  - forage's event
  - Game of Chaos' continuation
  - Garth One-Eye's continuation
- Playtest one-offs (MB1/MB2): Buddy List ×2, Five Kids in a Trenchcoat, Domesticated Mammoth, Duelists' Convocation International, Planeswalkerificate, Panglacial Shinobi, No-Regrets Egret, Mirrored Lotus.
- Already one record for two cards:
  - win on an empty-library draw (Laboratory Maniac, Jace, Wielder of Mysteries)
  - `CountAsNamedCardForSpellEffect` (Pardic Firecat, Diligent Farmhand)
  - Nefarious Lich's life-gain replacement (Nefarious Lich, Lich)
  - `PreserveCountersOnZoneChangeEffect` (Skullbriar, the Walking Grave, Me, the Immortal)
- Card mechanics no other record shares:
  - My Laughter Echoes, Arcane Endeavor, Invasion Plans, River of Tears, Chorus of the Conclave
  - `DelayedTargetGroup` (a component record of two unique delayed-trigger records, not an effect)
  - Enduring Angel, Enduring Renewal, Duskwalker, Eye for an Eye, Game of Chaos, Garth One-Eye, Yixlid Jailer
  - Nefarious Lich's damage replacement, Storyweave, Serene Sleuth, Sanctum Prelate, Thieves' Auction, The Immortal Sun, Deification

**Seen, not changed** (confirmed by reading, not run)
- **Fear My Authority can't be abandoned voluntarily.** When both a discard and 3 life are payable, declining the discard prompt pays the life. "Unless" is a "may" (CR 118.12a), so the player should be able to decline both. Sure.
- **Chorus of the Conclave ignores the caster.** `AdditionalSpellCostService.hasCreatureSpellAdditionalCountersCost` checks every battlefield and not the caster, so an opponent's Chorus gives you the option. The rules say "you" is the static's controller (CR 109.5). Sure.
- **Garruk Relentless's return damage isn't damage.** "That creature deals damage equal to its power to him" is a direct loyalty subtraction. It skips damage prevention, protection, damage replacements (doublers) and "deals damage" triggers; under CR 120.3c that damage is what removes the loyalty. Sure. The split fixes it.
- **Lethal Scheme's connive order is fixed.** The convoking creatures connive in cast order; the controller should choose the order (CR 701.50c, its ruling). Sure, low impact.
- **Dearly Departed under Yixlid Jailer.** `applyGraveyardEnterWithAdditionalCounters` never asks `graveyardCardsHaveLostAllAbilities`, and it judges the entering card's printed creature type. Sure of the code.
- **Readers of printed statics with no ability check** (CR 613.1f, 708.2a):
  - Enduring Angel (`LifeSupport.applyEnduringAngelReplacement`) and Enduring Renewal (`DrawService.findRevealTopCreatureToGraveyardElseDrawSource`) have no lost-abilities check. Sure.
  - Laboratory Maniac and Jace (`findWinOnEmptyLibraryDrawSource`) check lost abilities but not face down. Fairly sure.
- **Enduring Angel that can't transform.** Its replacement isn't applied at all, so the life total isn't set to 3 and the loss comes from state-based actions. The result differs only under "can't lose the game". Fairly sure, low impact.
- **Avatar's Wrath exiles one at a time.** It airbends each creature as its own exile, not as one event. Sure of the code.
- **Vault 11's secret votes are never revealed.** Nothing logs them, though the text reveals them. Fairly sure, low impact.
- **Storyweave's log is in the second person** ("your control") in a shared log. Sure, low impact.
- **Soulless Jailer judges the card's printed type.** `canCastSpellFromZone` reads `card.hasType(CREATURE)`, not the spell being cast (an Adventure or a modal back face). Unsure, low impact.

**Left**
- Nothing in the chunk.
- Unaudited records these batches touch: `WillOfTheCouncilEffect` (3 cards), `AirbendTargetPermanentEffect` (11), `PutCounterOnControlledCreatureCost` (16), `ControlTargetPlayerNextTurnEffect` (6), `DealDamageToAttackedTargetEffect` (6), `ControlledPermanentsEnterWithAdditionalCountersEffect` (13), `CreatureEnteringDontCauseTriggersEffect` (4), `CrumblingSanctuaryDamageReplacementEffect`.
- Rows to re-triage with their batches:
  - 5m's `ChangeTargetOfTargetSpellToSourceEffect` and `ChangeTargetOfTargetSpellToEnchantedCreatureEffect`
  - run 26's `DrawDiscardAndConniveEffect`
  - 4j's `ExileTopCardsAndMayCastSpellsEffect`
- Suggested fold-run order:
  1. By-parameter and single folds.
  2. Life lock.
  3. Two-option vote, together with chunks 12 and 14.
  4. Airbend.
  5. Connive.
  6. Player control.
  7. Blight costs, with the cost-shape singles and step 10.

### Step 9, parallel triage chunk 11 (2026-11-23)

Verdicts only: no repo file changed. 85 ledger rows (notes `triage P11: …`), read against `cc2d6a59fe`. Chunk 11 holds the rare records whose first word runs from Remember to Zndrsplats (40), and from Abstract to Close (45).

Every record was read with its handler to the end, along with every card class that uses it. For records with no handler, their readers were read:
- `ExploreEffectHandler.countExploreReplacements`.
- `CastingCostService`'s shared-colour discard path.
- `DrawService` (Abundance, Archmage Ascension, Chains, Shared Fate, the exile-replacement collector) and `BreathstealersCryptDrawReplacementHandler`.
- `TurnProgressionService.hasApplicableExtraTurnSkipReplacement`.
- `GameQueryService`'s lethal-damage, crew/saddle and entering-trigger checks.
- `AmountEvaluationService`'s station reads.
- `TokenAdditionReplacements`.
- `LayerSystemService` / `GameData`'s suppression cases.
- `PlayerDamagePreventions`' Channel Harm shield.
- `SecretMissionService` / `MayMiscHandlerService`.

Oracle text was checked with `get_card` for every `unique` and `blocked-*` card. The MB1/MB2 playtest cards (Buried Ogre, Pyromancy 101, Marchesa's Surprise Party, Bucket List, Whammy Burn, Memory Bank, Bombardment) are in no feed, so their text was read from the card-data cache. Oracle text was also checked for Play with Fire, Sonic Shrieker, Sands of Time, Fatespinner, Gerrard's Hourglass Pendant, Loran, Doorkeeper Thrull, Academy Manufactor, Ajani, Liliana, Cryptic Coat, Burning-Rune Demon, Binding Negotiation, Animal Magnetism, Bounty of the Luxa, Cemetery Desecrator and Celestial Judgment. Rulings were read for Sorrow's Path. CR 101.4, 119.8, 607.2a, 608.2d, 613.1f, 614.5, 616.1, 701.23a, 701.38a, 702.65a and 708.2a were checked with `get_rule` (2026-11-23).

| Family | Triaged | `step-9` | `blocked-other` | `unique` |
|---|---|---|---|---|
| Remember … Zndrsplats | 40 | 12 | 2 | 26 |
| Abstract … Close | 45 | 18 | 5 | 22 |
| All | 85 | 30 | 7 | 48 |

**Fold batches** (survivor or shared component → records of this chunk, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| suppression pair | `SuppressStaticEffectUntilEndOfTurnEffect` + `PermanentSubject` (source() / target()) | itself, `SuppressStaticEffectOnTargetUntilEndOfTurnEffect`: 2 (4) |
| unsuspect pair | `UnsuspectEffect` (subject) | itself, `UnsuspectAllCreaturesEffect`: 2 (4) |
| tap-flip pair | `SimultaneouslyFlipAllPermanentsTapStatesEffect` + `PermanentSubject` | itself, `SimultaneouslyFlipControlledPermanentsTapStatesEffect`: 2 (2) |
| P02 extra-turn skip | `SkipAllExtraTurnsReplacementEffect` + whose `PlayerSubject` (survivor) | itself; joins `OpponentExtraTurnSkipReplacementEffect`: 1 (1) |
| P02 entering-trigger | `CreatureEnteringDontCauseTriggersEffect` + entering filter + whose subject | `ArtifactOrCreatureEnteringDontCauseTriggersEffect`: 1 (1) |
| P02 opponent-chosen card | `ChoosePlayerEffect.opponent()` + `ChooseCardEffect(chooser playersThisWay(CHOOSE))` + mover; needs a rest-of-revealed card subject and the card CHOOSE to keep its chooser | `AnimalMagnetismEffect`, `AllureOfTheUnknownEffect`: 2 (2) |
| P05 skip-step | `SkipDrawStepEffect` with a step/phase kind, floated by `GrantStaticEffectToPlayerEffect.thisTurn` | `SkipChosenStepOrPhaseEffect`: 1 (1) |
| P07 chosen-land-type | `GrantSubtypeEffect` with the source's second chosen subtype over basics of the first, overriding | `BasicLandsOfChosenTypesBecomeTypeEffect`: 1 (1) |
| T4 look-and-cast | `LookAtTopCardsAndMayCastEffect` | `AetherworksMarvelEffect`: 1 (2) |
| T5 token addition | `AddTokenCreationEffect` (Clue/Food/Treasure filter, one per token, two blueprints) | `AcademyManufactorTokenReplacementEffect`: 1 (1) |
| T6 copier subject | `BecomeCopyOfSubjectEffect` copier attached() over a chosen `exiledWithSource()` creature card, with a Duration | `AttachedCreatureBecomesCopyOfExiledCreatureEffect`: 1 (2) |
| T7 card-copy-cast | `CopyImprintedCardAndMayCastCopyEffect.of(the encoded card)` free | `CipherCastCopyEffect` (engine-built): 1 (0) |
| may-draw rows | `MayEffect(DrawCardEffect(1, triggeringPlayer()), TRIGGERING_SPELL_CONTROLLER)` | `CastingPlayerMayDrawCardEffect`: 1 (1) |
| Intuition | `IntuitionEffect` + count, distinct names, filter, untargeted opponent chooser | `BurningRuneDemonEffect`: 1 (1) |
| look-X to battlefield | `LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect(…, ControllerLifeTotal)` + shuffle | `AjaniUltimateEffect`: 1 (1) |
| by parameters | `DrawCardEffect(1, allOf(you(), targetPlayerDeclaredElsewhere()))`; `ConditionalEffect(CastFromZone(HAND), ExileSpellEffect.immediately())` (check Bank's text) | `YouAndTargetPlayerEachDrawCardEffect`, `BankEffect`: 2 (2) |
| player damage-this-way | `ConditionalEffect(player form of HappenedThisWay(DEAL_DAMAGE), ScryEffect(1))`; stale `DiscardIfPlayerDealtDamageThisWayEffect` → `DiscardEffect(playersThisWay(DEAL_DAMAGE))` | `ScryIfPlayerDealtDamageThisWayEffect`: 1 (1) |
| keyword-action power | `UseToughnessForCrewAndSaddleEffect` with an action set (crew/saddle/station), granted with a toughness>power filter; `PowerBoostForCrewAndStationEffect` → `PowerBoostForCrewAndSaddleEffect` | `UseToughnessForStationEffect`: 1 (1) |
| alternative cost (cost-shape) | `AlternativeCostForSpellsEffect` + a shares-a-colour discard non-mana cost, all players | `SharedColorDiscardAlternativeCostEffect`: 1 (1) |
| choose then sparing destroy | `ChoosePermanentsEffect` (different POWER, one per distinct power) + `DestroyAllPermanentsEffect.sparing` (find a second user first) | `CelestialJudgmentEffect`: 1 (1) |
| splits | existing steps | Binding Negotiation (hand reveal, choose, discard, otherwise exile-to-graveyard), Bounty of the Luxa (remove counters + negated / plain `removedCounters()` gates), Cemetery Desecrator (choose, exile, reflexive modal on `exiledManaValue()`), Cryptic Coat (cloak recording MANIFEST + attach to `thisWay(MANIFEST)`), Liliana's −7 (`ChooseCardEffect.upTo(2)` + `PutCardsOntoBattlefieldEffect`; `OpenGraveyardReturnBatchEffect` goes): 5 (5) |

**Blocked (7):**
- Draw replacements whose payload `DrawService` runs inline (run 32's DrawService replacement-pass redesign): Abundance, Archmage Ascension, Breathstealer's Crypt, Chains of Mephistopheles (+ Magus), Shared Fate.
- A VOTE step whose result carries a tally per candidate (StepOutcome has one amount): Trap the Trespassers (`SecretCouncilEffect`), Círdan. The unaudited `VoteForCreatureThenDestroyMostVotedEffect`, `WillOfTheCouncilEffect` and `CouncilDilemmaEffect` share this need. That is five or more records, so a run could build the step.

**Unique (48):**
- Remember … Zndrsplats (26): Cyclopean Tomb's mire store, the shared remember-target-player record, Twists and Turns' explore replacement, Secret Mission, both Soul Burn / Drain Life, Soul Echo, Sphinx Ambassador and its engine marker, Buried Ogre, Shahrazad, Sorrow's Path, General Jarkeld, Tainted Pact and its marker, Teach and its cast copy, Time and Tide, Time Vault, Bucket List, Cackling Observer's link step, Zilortha, Whammy Burn and its continuation, Null Chamber, Zndrsplt's Judgment and its action.
- Abstract … Close (22): Abstract Performance, Ad Nauseam, Struggle for Sanity, Thick-Skinned Goblin, Amulet of Quoz, Applejack, Olag, Approach of the Second Sun, `AsEntersDiscardEntry`, the shared attack-direction record, Arcanum Wings (aura swap), Balduvian Warlord, Bend or Break, Chaotic Aether, Bombardment, Brilliant Ultimatum, Bronze Tablet, Cait, Camouflage, Channel Harm, the shared chaos-ensues record, Captivating Glance.

**Seen, not changed** (confirmed by reading, not run):
- Burning-Rune Demon (`BurningRuneDemonEffectHandler`) never calls `LibrarySearchTriggerHelper.recordSearchAndQueueTriggers`. It also ignores Opposition Agent and Aven Mindcensor's top-cards limit, all of which `IntuitionEffectHandler` handles. Searching with it misses "whenever a player searches" (CR 701.23a). Sure of the code.
- Shared Fate exiles from `getOpponentId`, where its text says "one of their opponents' libraries", chosen by the drawing player. Abstract Performance, Brilliant Ultimatum, Burning-Rune Demon and Captivating Glance's clash all take the first other player as "an opponent". Wrong only with more than two players. Sure of the code.
- `SwapBlockingAssignmentsBetweenTwoCreaturesEffect`'s Javadoc says the blocks don't trigger again. Its handler re-blocks through `applyBlockFromEffect`, and the rulings say "blocks" abilities trigger again, so the handler is right and the Javadoc is stale. Sure.
- Bounty of the Luxa sets flood counters with `setCounterCount`. Placement replacements (counter doublers) and the counter-removed events are skipped. Sure of the code, low impact.
- Animal Magnetism puts the chosen creature onto the battlefield with `putPermanentOntoBattlefield` + `handleCreatureEnteredBattlefield`, outside the entry batch. As-enters copy choices may be skipped. Unverified.
- The draw-replacement source scans for Abundance (`findAbundanceSourceCard`), Archmage Ascension (`findCounterThresholdDrawReplacementSource`), Shared Fate (`findSharedFateSources`) and Breathstealer's Crypt (`findCryptSources`) read `getCard().getStaticEffects()` with no lost-abilities or face-down check (CR 613.1f, 708.2a). `ExploreEffectHandler` checks lost abilities but not face-down. Sure of the code.

**Anything left.** Nothing in this chunk. The batches reach records outside it:
- Unaudited: `OpenGraveyardReturnBatchEffect`, `CreatureEnteringDontCauseTriggersEffect`, `SkipDrawStepEffect`, `PowerBoostForCrewAndSaddleEffect`, `PowerBoostForCrewAndStationEffect`, the Vote family.
- Rows to update: `DiscardIfPlayerDealtDamageThisWayEffect` (stale 4e row, now a parameters fold); `UseToughnessForCrewAndSaddleEffect` and `ClashEffect` (run 29 unique rows; the first now gains a station use).

Cheapest fold-run starts, by parameters or a subject only: the suppression, unsuspect and tap-flip pairs; Loran; Aetherworks Marvel; Minamo; Doorkeeper Thrull; Gerrard's Hourglass Pendant.

### Step 9, parallel triage chunk 12 (2026-11-23)

Verdicts only: no repo file was changed and the ratchet did not move. 85 ledger rows (notes `triage P12: …`) cover the chunk's rare records from `CoercivePortalEffect` to `GuildFeudEffect`. Nearly every first word is a one-record family, so they are counted by first letter.

Every record was read with its handler to the end, plus the cards that use it. For records with no handler of their own, these readers were read:
- `GameQueryService`: the counter locks, the token and creature control replacements.
- `CastingPermissionService` (Damping Engine), `DrawService` (Blood Scrivener) and `CombatDamageService` (Defensive Formation).
- `PlayerDamageFinalReplacements` and `DamageSupport` (Crumbling Sanctuary, Dralnu).
- `CoinFlipService` (Edgar) and `DamagePreventionService` (Judgment of Alexander).
- `MayPenaltyChoiceHandlerService` (Combustible Gearhulk, Covenant of Minds), `MayCastHandlerService`'s free-cast clearing, `ChoiceHandlerService` (Dismantle) and `MayMiscHandlerService` (Gemstone Caverns).
- `KeywordTriggers.EVOLVE`, and `GameData`'s untap-skip and declaration-replacement stores.

Oracle text was checked with `get_card` for:
- Underbridge Warlock, Combustible Gearhulk, Willie Lumpkin, Cultural Exchange, Danse Macabre, Damping Engine.
- Discord, Disorienting Choice, Emergent Ultimatum, Great Intelligence's Plan, Expert-Level Safe.
- Gandalf, Westward Voyager; Rainbow Dash; Fatal Lore; Balduvian Warlord.

Rulings were read for Ezuri's Predation, Dawnbreak Reclaimer and Jaya Ballard. These CR rules were checked with the rules MCP (2026-11-23): 101.4, 104.4a, 118.9a, 400.7, 406.3, 603.12, 608.2d, 608.2h, 613.1f, 616.1, 700.2e, 701.29a, 701.43b, 701.44c, 701.44d, 702.16b, 702.34a, 702.100a, 702.100b, 702.174a and 708.4.

| Letter | Triaged | `step-9` | `blocked-other` | `blocked-player-loop` | `unique` |
|---|---|---|---|---|---|
| C | 15 | 8 | 1 | 0 | 6 |
| D | 22 | 9 | 0 | 1 | 12 |
| E | 25 | 12 | 1 | 2 | 10 |
| F | 10 | 4 | 0 | 0 | 6 |
| G | 13 | 8 | 1 | 0 | 4 |
| All | 85 | 41 | 3 | 3 | 38 |

**Fold batches** (survivor or shared component → records of this chunk, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| vote | A new two-option vote step (`VotingSupport` order, `MayVoteAdditionalTimeEffect` honoured, a secret option) recording a new `StepKind.VOTE` (votes per option). Read by a "more votes for X" condition, `AmountThisWay(VOTE)` per option, `playersThisWay(VOTE)` of an option, and the triggering-vote readers. | Coercive Portal, Tivit, Elrond, Erestor, Expropriate, Fateful Tempest, Galadriel, Magister of Worth: 8 (8). Unaudited joiners: `PleaForPowerEffect`, `TyrantsChoiceEffect`, `VoteForDenialOrDuplicationEffect`, `VoteForInnocentOrGuiltyEffect`, `SailIntoTheWestEffect`, `WillOfThePlaneswalkersEffect`, `SelvalasStampedeEffect`, `TravelThroughCaradhrasEffect`, `ModelOfUnityEffect` |
| opponent punisher | `MayEffect` with another player as chooser (`TARGET_PLAYER`), its else branch composed from steps (`AmountThisWay.milledManaValue`, `RevealTopCardOfLibraryEffect.topCards`). | Combustible Gearhulk, Covenant of Minds, Willie Lumpkin: 3 (3) |
| rest of a chosen set | A `CardSubject` "except" (the cards of one step but not another's choice) after a `ChooseCardEffect` made by an opponent or by you. | Deliver Unto Evil, Dubious Challenge, Emergent Ultimatum, Genesis Wave: 4 (4). Ecological Appreciation joins once a library-and-graveyard search exists |
| entry controller | `GatherSpecimensEffect` (survivor) + an object filter (token created / creature entering) | Gather Specimens, Crafty Cutpurse: 2 (2) |
| villainous choice (run 9) | `TargetPlayerChoosesOneEffect.villainousChoiceFacedBy` / `forTargetedPlayerVillainousChoice` | Ensnared by the Mara, Great Intelligence's Plan: 2 (2) |
| SEARCH step (triage 2) | The search's exile step + an any-opponent may | Distant Memories: 1 (1), with P02's `OpponentMayReturnExiledCardOrDrawEffect` |
| same-name search | One "for each permanent of a set, search for a card with the same name" record over a `PermanentSubject` and a library owner | Dichotomancy: 1 (1). Re-triage triage 2's blocked Doubling Chant and Clarion Ultimatum with it (now three classes) |
| piles (P08) | The separate-into-piles step over a card set | Death or Glory: 1 (1) |
| next-spell rider (P05) | `EmpowerNextCreatureSpellThisTurnEffect` as survivor + a `CardPredicate` | Insist, Savage Summoning: 1 (2) |
| timing permission (P05) | One `ActivatedAbilityTimingEffect` with an ability filter | Leonin Shikari: 1 (1) |
| keyword costs (triage 1) | One keyword-cost change (foretell, a special action) | Cosmos Charger: 1 (1). Its any-turn flag is weighed by the fold run |
| damage dealer | `DealDamageToTargetPlayerOrPlaneswalkerEffect.byTriggeringCreature` / the plain source form | Aether Charge, Lavabelly Sliver: 1 (2) |
| set-dealer bite | `TargetDealsPowerDamageToTargetEffect` + a set dealer and a non-target recipient | Judgment of Alexander's engine-built record: 1 (0). 5m's blocked `ControlledCreaturesDealPowerDamageToTargetEffect` (Moonlight Hunt) is the other user |
| singles | Existing heads | Counterlash (`CounterSpellEffect` + `MayCastAnySpellFromHandWithoutPayingManaCostEffect` with a shares-a-type-with-the-target predicate); Creeping Dread (`DiscardEffect(eachPlayer())` + `LoseLifeEffect` over a filtered `playersThisWay(DISCARD)`); Danse Macabre (sacrifice + `RollD20Effect.withAddedAmount(sacrificedToughness)` + choose/put); Delaying Shield (`RemoveAllCountersEffect` + `RepeatEffect` of `ForcedCostOrElseEffect`, cost-shape); Dismantle (`NoteAmountEffect` + destroy + a counter-kind choice); Earth Rumble (earthbend + `QueueReflexiveAbilityEffect` fight); Jaya's emblem (`CastSpellsFromGraveyardEffect` + exile instead); Triskaidekaphobia (`ControllerLosesGameEffect` + life, after the CR 104.4a draw); Hakbal (`ExploreEffect` over a set, CR 701.44d); False Orders (`BalduvianWarlordEffect` with an optional reblock); Fateseal (`ScryEffect`, no scry triggers, the controller decides, log kept); Gandalf (reveal + conditional copy / else draw); Gargantuan Gorilla (8.20's may-sacrifice pattern, cost-shape); Guided Passage (`ChooseCardEffect.oneOfEach` chosen by an opponent): 14 (14) |

**Blocked (6):**
- Ezuri's Predation, `blocked-other`. It needs 5m's fighter-subject fight path plus a pairing the controller chooses (both rulings).
- Ecological Appreciation, `blocked-other`. It needs a library-and-graveyard search step; Doomsday holds the only other such pool.
- Grizzled Huntmaster, `blocked-other`. It conjures into a hand, which is the owner's hidden-zone logging question.
- Disorienting Choice, Eumidian Wastewaker and Explosion of Riches, `blocked-player-loop`. In each, every player chooses and then all act.

**Unique (38):**
- Engine-internal or keyword payloads: `ContinueAfterDepartureEffect`, `DepartPermanentsEffect`, `DoesntUntapDuringNextUntapStepsEffect`, `DeclarationReplacementEffect`, `EvolveEffect`.
- Already one record for their cards: Forgotten Lore + Shrouded Lore, Defensive Formation.
- Static rules and replacements: Solemnity, Crumbling Sanctuary, Dralnu, Blood Scrivener, Damping Engine, Edgar, Fasting, Firestorm Phoenix, Gemstone Caverns, Esix, Guardian Angel.
- Card-specific loops and choices: Dance with Calamity, Forbidden Ritual, Doomsday, Cultural Exchange, Dawnbreak Reclaimer, Guild Feud, Expert-Level Safe, Grave Endeavor, Equipoise, Fatal Lore, Mandate of Peace, Discord, Underbridge Warlock's boon use-up, Druid of the Emerald Grove's d20 payload.
- Joke and playtest cards: Rainbow Dash, Duelists' Convocation International, Don't Worry About It, Arcanum Things, Ruff, Throat Wolf.

**Seen, not changed** (confirmed by reading the code, not run):
- **Jaya Ballard's emblem.** `EmblemGrantsFlashbackEffect` grants flashback, which is an alternative cost (CR 702.34a), so no other alternative cost can be combined with it (CR 118.9a). The text is a permission to cast for the mana cost. Fairly sure of the rule; I did not trace how `FlashbackCast` interacts with the other alternative costs.
- **Expert-Level Safe.** On a mismatch it uses `new ExileTopCardsToSourceEffect(1, true)`, whose `mayLookAtFaceDownCards` defaults to true, so its controller may look at that face-down card (CR 406.3). The enter trigger uses `faceDownWithoutLookPermission`. Sure.
- **Ezuri's Predation.** It pairs tokens with creatures by battlefield order and fights one at a time. The rulings say you choose the pairs and the fights happen at the same time. Sure.
- **Lost-abilities check missing.** `PlayerDamageFinalReplacements.printedOnBattlefield` (Crumbling Sanctuary, Nefarious Lich, Immortal Coil) and `DamageSupport.applyCrumblingSanctuaryReplacement` have no lost-abilities check (CR 613.1f). Sure.
- **Each player acts at their own answer (CR 101.4).** Disorienting Choice exiles at each controller's answer, and Eumidian Wastewaker sacrifices or discards at each answer. Sure.
- **Eumidian Wastewaker's land count.** A land counts as put into a graveyard when the permanent is gone, so a land token or a land exiled by a replacement counts. Fairly sure.
- **Hakbal of the Surging Soul.** A Merfolk that left the battlefield before its turn doesn't explore (CR 701.44c says it should). Fairly sure.
- **Triskaidekaphobia.** When some but not all players lose, the handler declares the first survivor the winner. Multiplayer only; sure.
- **Covenant of Minds.** The may handler takes the first seat that isn't the opponent as the controller. Multiplayer only; sure.
- **"An opponent" not chosen by the controller.** Death or Glory and Ecological Appreciation take the first other seat; Deliver Unto Evil and Fateseal use `getOpponentId`. The controller should choose which opponent (CR 700.2e for a mode; the same reading for the others). Multiplayer only; sure.
- **Judgment of Alexander.** Its commanders' damage is dealt one dealer at a time, the same open defect as the bite's. Sure.
- **Delaying Shield.** It removes delay counters with `setCounterCount`, outside the removal path, so no counter-removed events are sent. Sure; low impact.
- **Dismantle.** Its counter total skips SILVER counters. The reason is unknown.
- **Great Intelligence's Plan.** The handler skips the villainous choice when neither option can do anything. I did not check whether the player still faces it (The Valeyard). Low confidence.

**Left.**
- Three uniques (P07's Malik, Cultural Exchange, Dawnbreak Reclaimer) and Guild Feud's two puts all need two results of one step kind in a single resolution. If a fold run adds a CHOOSE slot, re-triage them.
- Dance with Calamity and Forbidden Ritual are the only repeat-while-you-choose records.
- Re-triage triage 2's Doubling Chant and Clarion Ultimatum rows with the same-name batch.
- `ResetCoolnessEffect` (unaudited) goes with Coolness's unique row.

For the next run: the vote step is the largest batch. Build it once, then fold the 8 records here and the unaudited vote records in other chunks.

### Step 9, parallel triage chunk 15 (2026-11-23)

Verdicts only: no repo file changed. 26 ledger rows (notes `triage P15: …`), read against `7f58dc485c`. Chunk 15 holds the rare records whose first word runs from Unexpected to Zyym.

Every record was read with its handler to the end, along with every card class that uses it. For records with no handler, their readers were read:
- `DrawService` (Cyclone, Zur's Weirding).
- `GameQueryService.cantBeTargetedByWallOnlySources` and its callers.
- `CastingPermissionService` (Ward of Bones).
- `CastingCostService` / `SpellCastingService` (web-slinging).
- `WerewhatSupport`, `UpgradeSupport`, `StackStaticEffectHandler`.
- The manafx handler (Yuna).

Also read: the mayfx and continuation handlers of Unexpected Results, Worms of the Earth and Zyym; `GoblinGameEffectHandler`; the dice roll services; the other three Endeavor handlers.

Oracle text was checked with `get_card` for Wave of Vitriol, Void, Villainous Wealth, Wall of Shadows, Worlds Within Worlds, Winter's Chill, Wheel of Misfortune, Whims of the Fates, Unleash the Flux, Lukamina, Unpredictable Cyclone, Unexpected Results, Ward of Bones, Word of Command, Zur's Weirding, Verdant Mastery, Valiant Endeavor, Wild Endeavor, both Ghostly cards, Yuna, Peter Parker // Amazing Spider-Man, Worms of the Earth, Barbarian Class and Pixie Guide. Weaponized Scrap and Zyym are MB1 playtest cards in no feed, so their text was read from the card-data cache; Werewhat's text is registered in its class. Rulings were read for Worms of the Earth, Wave of Vitriol and From the Ashes (the last two have none). CR 101.4, 113.6, 118.12, 603.7b, 608.2e, 613.1f, 701.23a and 706.6 were checked with `get_rule`.

| Family | Triaged | `step-9` | `blocked-other` | `blocked-player-loop` | `unique` |
|---|---|---|---|---|---|
| Unexpected … Upgrade | 6 | 3 | 1 | 0 | 2 |
| Valiant … Void | 4 | 3 | 0 | 0 | 1 |
| Wall … Wrapped | 14 | 6 | 0 | 1 | 7 |
| Zur's, Zyym | 2 | 0 | 1 | 0 | 1 |
| All | 26 | 12 | 2 | 1 | 11 |

**Fold batches** (survivor or shared component → records of this chunk, cards):

| Batch | Survivor / component | Records (cards) |
|---|---|---|
| two-dice (new) | `RollTwoDiceChooseOneEffect(sides)`: the roll, dice events and spell-number choice, chosen result as X and the other as the event value. The cards' own steps read `XValue` / `EventValue`; `MassDamageEffect` and `ReturnCardFromGraveyardEffect.plusOneCounterCount` need dynamic amounts, Valiant needs triage 3's power-at-least-X predicate | `ValiantEndeavorEffect`, `WildEndeavorEffect`: 2 (2); joins unaudited `RecklessEndeavorEffect`, `GraveEndeavorEffect` and P10's `ArcaneEndeavorEffect` (5 records, 5 cards) |
| room door | `UnlockControlledRoomDoorEffect` + a Room `PermanentSubject` (target() / controlledBy(you(), Room)) + a lock-allowed mode | itself: 1 (2); joins unaudited `LockOrUnlockTargetRoomDoorEffect` (2 cards) |
| P02 specialize | one `SpecializeEffect(color)` handler with the card-side face hook | `UnspecializeLukaminaEffect` (engine-built): 1 (0) |
| T7 next-time delayed | `RegisterGlobalTriggeredAbilityEffect` next-time form (`TemporaryGlobalTriggeredAbility.nextThisTurn`) + manafx handler | `WhenYouNextCastCreatureSpellThisTurnEffect`: 1 (1) |
| P04 punisher (cost-shape) | `MayPayManaEffect`'s ANY_PLAYER payer with an either-payment (sacrifice two lands / be dealt 5), reward destroy source() | `WormsOfTheEarthEffect`: 1 (1); the either-payment is set by this card alone, so the fold run weighs it |
| P11 alternative cost (cost-shape) | `AlternativeCostForSpellsEffect` + a non-mana cost component (`ReturnPermanentsCost(1, tapped creature)`), keeping the returned mana value | `WebSlingingEffect`: 1 (1); with `SharedColorDiscardAlternativeCostEffect` |
| by parameters | `ExileTopCardsAndMayCastSpellsEffect(XValue, targetPlayer(), manaValueLimit XValue)` | `VillainousWealthEffect`: 1 (1) |
| splits | existing steps | Void (`ChooseNumberAtResolutionEffect` + `DestroyAllPermanentsEffect` with `PermanentManaValueEqualsXPredicate` + `RevealCardsFromHandEffect` + `DiscardCardsEffect(cardsInHands(…, CardManaValueEqualsXPredicate))`); Unexpected Results (`ShuffleLibraryEffect` + `RevealTopCardOfLibraryEffect` + REVEAL-gated free cast / `MayEffect`(put land + `ReturnToHandEffect(source())`)); Worlds Within Worlds (`ExileAllPermanentsEffect` + `EachPlayerMayPutCardFromHandToBattlefieldEffect` anyNumber + `PutCardsIntoHandEffect(cardsThisWay(EXILE))`): 3 (3) |
| dead | delete | `WheelOfMisfortuneEffect`: the card only imports it and uses `GoblinGameEffect.wheelOfMisfortune()`. `WheelOfMisfortuneState` and `GameData.wheelOfMisfortune` go with it: 1 (0) |

**Blocked (3):**
- Draw replacements whose payload `DrawService` runs inline (run 32's DrawService replacement-pass redesign): Unpredictable Cyclone and Zur's Weirding. They join P11's five.
- One may-search per object, the cause of From the Ashes's run 9 row: Wave of Vitriol. With Winds of Abandon's seen defect, three classes now share it, so a run could build a per-object may-search on `EachPlayerSearchesLibraryEffect`.

**Unique (11):**
- Unleash the Flux: its "repeat this process" loop marker.
- Weaponized Scrap: the upgrade overlay.
- Verdant Mastery: the three-way land split.
- Wall of Shadows: the Walls-only targeting restriction.
- Ward of Bones: the per-type count comparisons.
- Werewhat: a back face built as it enters.
- Whims of the Fates: three random piles per player.
- Winter's Chill: a {1}/{2} payment with three outcomes (cost-shape).
- Word of Command.
- Torrent of Lava's `WrappedStackStaticEffect`: already the generic CR 113.6 wrapper.
- Zyym.

**Seen, not changed** (confirmed by reading, not run):
- Valiant, Arcane and Grave Endeavor roll through `DiceRollService.roll(sides)`, which ignores `RollWithAdvantageEffect`. Barbarian Class and Pixie Guide both say "if you would roll one or more dice, roll that many plus one". Wild Endeavor calls `D4RollService.roll` once per die, giving each d4 its own extra die instead of one extra die for the roll. Reckless Endeavor's `D12RollService.rollDice` does it right. Sure of the code.
- Void discards with its own loop: no `isDiscardPrevented` check (Tamiyo) and no discard-to-library replacement. `PlayerInteractionSupport.discardCards` has both. Sure.
- Unexpected Results' land branch (`MayMiscHandlerService.handleUnexpectedResultsLandChoice`) skips `isCardBlockedFromEnteringFromZone`, so a land enters under Worms of the Earth (its ruling: the land stays where it is). It also skips the entry batch. Sure of the code.
- Zur's Weirding offers the 2-life payment only to `getOpponentId`; the text says any other player. Verdant Mastery likewise takes `getOpponentId` for "an opponent". Both are wrong only with more than two players. Sure.
- `DrawService.findCyclingDrawReplacementSource` (Unpredictable Cyclone) and `CastingCostService.findWebSlingingEffectFromBattlefield` (Amazing Spider-Man) read `getCard().getStaticEffects()` with no lost-abilities check (CR 613.1f). Sure of the code.
- Wave of Vitriol gives one search for up to N (`BasicLandsPick(count, true)`), where the text has one may-search per land sacrificed. That changes how many search events happen (CR 701.23a). Medium; no ruling settles it.
- P10's `ArcaneEndeavorEffect` row says "no other roll assigns two results to two instructions". That is false: four other Endeavor handlers share its skeleton line for line.

**Anything left.** Nothing in this chunk. The batches reach records outside it:
- Unaudited: `RecklessEndeavorEffect`, `GraveEndeavorEffect`, `LockOrUnlockTargetRoomDoorEffect`.
- Rows to update: `ArcaneEndeavorEffect` (P10 unique → two-dice batch); `EachDestroyedPermanentControllerMaySearchBasicLandToBattlefieldEffect` (run 9, now the third class of its cause).

Cheapest fold-run starts: Villainous Wealth (parameters only), `WheelOfMisfortuneEffect` (dead), Worlds Within Worlds and Void (existing steps), and the room-door pair.

## Prompt

Replace `<N>` with the step number. For step 9, add the run kind and its scope (`step 9, triage run: Destroy, Search`;
`step 9, fold run: the step-9 rows for <survivor>`).

```
Implement step <N> of effect-cleanup-refactor.md. Read the file from the top down to `## Findings` (Status, Design, Ground
rules, your step's section, Standing knowledge) and the last run's section under Findings; for an older run's details, grep
`git show 6728593a9a:effect-cleanup-refactor.md` for the class or card name instead of reading it. Check in the Status table that
every step yours depends on is done; if one isn't, stop and tell me. Follow the ground rules: read agent-docs/ARCHITECTURE.md
and agent-docs/architecture/resolution.md before touching engine or domain code, verify the premise by reading the effect
records and their handlers to the end before editing, keep behaviour identical unless the old behaviour was rules-wrong (then
cite the Oracle text or ruling and add a test), keep the conventions under Standing knowledge, verify every CR number with
get_rule, and test behaviour through the engine. Run only the tests the step names, with fully qualified class names and one
--tests list per Gradle test task; never the full suite, ask me for it. When the run is done: update agent-docs for every class
you removed, add the audited classes to scripts/effect-audit-ledger.csv, lower the EffectCountRatchetTest baselines, update the
Status row and Standing knowledge (new shared pieces, conventions, fixed or new open defects, owner's questions), append your
run's section under Findings, and tell me which classes you deleted, which tests changed and why, and any behaviour change.
Don't commit until I say so.
```
