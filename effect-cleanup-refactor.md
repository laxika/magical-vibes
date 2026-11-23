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
| 9 | Sweep the single-card tail (repeatable) | open. Runs 1 to 41 closed Exile, Each, Return, Put, Reveal, Choose, Grant, Create, Prevent, May, Register and Target. 1,398 rare records have no ledger row. Triage run 1 gave Pay and Reduce (80 rare) and 32 stale step-4/5 blocked rows their verdicts; triage run 2 gave Search and Cant (79 rare) theirs; triage run 3 gave Sacrifice and Destroy (75 rare) theirs; triage run 4 gave Look and Remove (71 rare) theirs; triage run 5 gave Add and Gain (64 rare) theirs (302 `step-9` rows wait for fold runs). Defect run 1 fixed most of the defects the triage runs saw (its Findings). From 2026-11-23 the step runs as **triage runs** (about 80 records each, verdicts only) followed by **fold runs** (see step 9). Open: Perpetually (30 rare) and Conjure's hand and library conjures (Conjure 39 rare) wait for the owner's decisions; the next families with no owner's question are Become (30 rare), Draw (28), Set (27) and Double (27). |
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
- **2026-11-23, May's 11 cost combinators for step 10** (run 25): `MayPayLandDropEffect`, `MayPayTapAndSacrificePermanentEffect`,
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
  changed (runs 7, 10, 15). Holds Perpetually's hand and library records and Conjure's hand and library conjures. Close to the
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
- Fatal Lore's destroy mode chooses its two creatures at resolution without targeting; its ruling has the caster choose targets
  after the opponent's mode choice, as the spell is cast (CR 601.2c). Needs a target prompt after the opponent's answer, which
  arrives after the spell is on the stack (triage 3, defect run 1). Code.
- Giant Albatross destroys each creature right after its controller answers, before the next controller chooses (CR 101.4, 608.2e);
  needs the unpaid creatures collected across the pay-life prompts (GameData state copied for simulations) and one
  `DestructionSupport.destroyBatchWithOutcome` when the queue empties (triage 3, defect run 1). Code.
- Last Light of Durin's Day with no Dragon in hand or library searches nothing, where the player may search and find nothing
  (CR 701.23b); kin of the queue's may-searches above (triage 2, defect run 1). Code.
- The pay-{X} handlers treat X = 0 as declining (Relentless Dead, Squealing Devil, Vigil for the Lost, Azor, Roalesk, Hollow
  Specter, Depala) while Isareth and Fire Lord Sozin queue at 0; paying {0} is paying (CR 118.12) (triage 1). Code, low impact.
- Rakdos Charm's third mode deals each creature's damage one at a time (kin of the bite's defect) (triage 1). Code.
- Phyrexian Dreadnought offers permanents that can't be chosen to sacrifice (its as-enters prompt) (triage 3). Unverified.
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
