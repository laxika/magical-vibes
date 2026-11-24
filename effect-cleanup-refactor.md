# Effect cleanup: one verb, any subject

A step-by-step plan to stop writing a new effect class for every combination of *what happens* and *who it happens to*. An
effect names its verb (boost, destroy, draw, lose life) and takes a **subject** (this permanent, the target, the enchanted
creature, each opponent, that permanent's controller, the cards exiled this way) as an ordinary component. One engine service
resolves every subject. Effects that fuse two instructions become steps that talk through the resolution's step results.

This file was cut down on 2026-11-23. The full text, with every run's Findings (steps 1 to 9, run 40), is in git history:
`git show 6728593a9a:effect-cleanup-refactor.md` (grep it for a class or card name; don't read it whole). The Findings of step 9's
triage runs 1 to 7 and parallel triage chunks 01 to 16 were merged into Standing knowledge (Fold queue, Defect queue, Re-triage) the
same day; their text is at `git show 51f0afd546:effect-cleanup-refactor.md`.

**How to read it.** Read from the top down to `## Findings`, then the last run's section under Findings. Everything a run needs
is above `## Findings`: the Standing knowledge section replaces the older Findings. The Fold queue and the Defect queue are
reference tables: read their introductions and the rows of your run, not every row. To start a step, use the prompt at the bottom.

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
| 9 | Sweep the single-card tail (repeatable) | open. Runs 1 to 41 closed Exile, Each, Return, Put, Reveal, Choose, Grant, Create, Prevent, May, Register and Target. From 2026-11-23 the step runs as triage runs, then fold runs (see step 9). Triage runs 1 to 7 and parallel chunks 01 to 16 gave every rare record a ledger verdict (none is left unaudited); defect runs 1 and 2 fixed most of the defects triage runs 1 to 5 saw. Their results are merged under Standing knowledge: the **Fold queue** (1,322 `step-9` rows in 299 batches, 51 proposed fold runs), the **Defect queue** (220 lines, 12 proposed defect runs) and **Re-triage and stale rows**. Defect run D10 applied the owner's hidden-zone logging decision and re-verdicted its 73 rows (71 `step-9` in fold runs 12, 12a, 12b and 20). Fold runs done: 1, 2, 3, 4, 5, 6, 6b, 7, 8, 9, 10, 11, 9b, 12, 11b, 10b, 12a, 14, 13, 12b, 14b, 13b, 14c, 12c, 8b, 16, 15, 12d, 17, 16c, 18, 16b, 15b, 17b, 19, 18b, 19b, 20, 17c, 21, 12e, 19c, 22, 17d, 18c, 21b, 22b, 19d, 21c, 23, 25, 28, 31, 30 (each in Findings; the fold queue marks its rows). Fold runs 23b, 28b and 25b are running in worktrees; 26 waits to merge. Waiting on the owner: 5 rows (perpetual "it" 3, gift opponent 2) and about 20 "counter unless" rows (option C, step 10).                                               |
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

## Measurements (2026-11-23, at `51f0afd546`)

Re-measure with `python3 -I scripts/effect-inventory.py` (no arguments: the summary below; `--family`, `--siblings`, `--dead`,
`--unaudited [--family P]`, `--tests-for Class... [--max-per-class N]`).

| What | Count |
|---|---|
| `EffectCountRatchetTest` baselines (records / handler classes in `normalfx`, `mayfx`, `staticfx`) | 3,511 / 2,805 (after fold run 30) |
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
No rare record is left; the triage gave each a row, and the `step-9` rows wait in the Fold queue.

`scripts/effect-audit-ledger.csv` (header `class,verdict,step,note`): 5,827 rows over 4,450 classes (after fold run 30). Latest verdict per class: `step-9` 647, unique 1,171, absorbed 871, split 631, blocked-other 507, survivor 325, blocked-step-model 140, blocked-player-loop 96, dead 62. Waiting: 647 `step-9` rows for the fold runs still open; 5 rows on the owner's open questions (perpetual "it" 3, gift opponent 2) and about 20 "counter unless" rows on step 10's option C.

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

**Fold runs** take the `step-9` rows (`--unaudited` does not list them: grep the ledger), one survivor's batch or more per run, in the
order of the Fold queue under Standing knowledge, and give each a verdict in this order of preference:

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
| Perpetually | 27–29, P16, D10 | triaged: the hand and library records are in fold run 12b since D10; three wait for the owner's "it" decision |
| Conjure | 29–31, P16, D10 | triaged: the hand and library conjures are in fold run 12a since D10; the CONJURE step, duplicate-destination and graveyard-ability store batches are in the Fold queue |
| Register | 30–34 | closed; the delayed step and event registrations, `NoteAmountEffect` |
| Target | 32–41 | closed; `PlayerRestrictionEffect`, the gain-control and choose-permanents choosers, one cast check |
| Every other rare record (+ 34 stale step-4/5 rows) | T1–T7, P01–P16 | triaged, no code changed (T*n* = triage run *n*, P*nn* = parallel chunk *nn*): every rare record has a verdict; the fold batches, defects and re-triage rows are merged into the Fold queue, Defect queue and Re-triage sections (Standing knowledge) |

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
- **The public log says only what the card's text makes public** (owner's decision 2026-11-23; CR 400.2). The game log is one list every
  player gets (`GameLogService` emits to all players; `GameLogEntry` has no audience), so nothing is logged for one player: the owner's
  hand shows the card. A conjure of a card the text names may name it; a random pick (a random spellbook card, a random library card,
  a sought card) and a drafted or chosen spellbook card going into a hand are "a card" (`GameLog.conjuresIntoHand`), and so is a
  duplicate of a hidden card (a library or hand card, a drawn card); a duplicate of a public card may be named. A perpetual change to
  cards in a hand or library is one line per hand or library with no name and no count, logged also when no card there matches
  (`PerpetualChangeLog`); a choice among hidden cards is one line counting no more than the instruction does (`ChooseCardEffectHandler`).
  A hand count is public (CR 402.3), so "conjures 7 cards" is fine. Lines name players, never "you" (defect run D10).

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
- A fold that merges engine-side stores (several `GameData` maps into one keyed store) is in scope, as run 28 merged the
  perpetual stores; the counters on exiled cards and the Ring's two stores are the first (orchestrator's reading, 2026-11-23).

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
- `NoteAmountEffect` (StepKind NOTE, `AmountThisWay.noted()`); `QueueReflexiveAbilityEffect` for "when you do" (also inside
  `ForageEffect`, fold run 1).
- Costs (fold run 1): `ReduceCastCostForNextMatchingSpellEffect` fixes an `XValue`, `EventValue` or `DeterminedOnce` amount as it
  resolves ("X ... as this ability resolves"), any other amount at cast (affinity); `ReduceActivationCostEffect` /
  `IncreaseActivationCostEffect` take any `DynamicAmount` (`CountersOnSource` for "for each counter"); a turn-limited static
  cost change is `ConditionalEffect(ControllerTurn(), …)`; `IntensifySourceCardEffect` takes a `DynamicAmount`.
- Damage, boost, draw (fold run 2): `BoostTargetCreatureEffect(createdThisResolution(), …)` ("that creature gets +N/+M" after a
  token step) and `Duration.PERMANENT` (no stated duration, CR 611.2a); `DealDamageToTargetCreatureEffect(thisWay(kind), n)` (the
  creature a `ChoosePermanentsEffect` chose); "choose a creature type, [verb] for each permanent of it you control" is
  `ChooseCreatureTypeAtResolutionEffect` + the verb over `PermanentCount(PermanentHasTypeChosenThisResolutionPredicate, CONTROLLER)`;
  `TargetPolarityClassifier` reads a negatively `Scaled` boost as a debuff.
- Permanents and combat (fold run 3): `MustBlockEachCombatEffect(subject)` (`source()` default; a static set such as
  `allCreatures()` is the source's own rule, read by `GameQueryService.isNamedByStaticMustBlockSet`);
  `TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(…, Duration)` (`PERMANENT` for "it becomes the creature type of your
  choice"); `ExploreEffect(controlledBy(you(), filter))` (each explores in turn, the controller ordering them, CR 701.44d);
  `BalduvianWarlordEffect.thenMayReblock()` ("you may have it block"); a "destroy all [filter] of the [chosen type / colour]" is the
  chosen-value step + `DestroyAllPermanentsEffect` with `PermanentHasTypeChosenThisResolutionPredicate` /
  `PermanentHasChosenSpellColorPredicate`; "tokens created with this" is `PermanentCreatedBySourcePredicate`; "up to one [filter]
  attached to that creature" is `ChoosePermanentsEffect.upTo(1, attachmentsOf(target(), filter))` + the verb over `thisWay(CHOOSE)`.
- Copy, search, seek, base P/T (fold run 4): `SetBasePowerToughnessEffect` over `source()` + `Duration.PERMANENT` ("has base P/T X/Y"),
  over a narrowed set (each unblocked creature), or over a one-by-one subject (`except(triggeringAttackers(), source())`);
  `BecomeCopyOfSubjectEffect.of(target())` declares a benign permanent target and copies `PermanentCopierService.copiableCard`;
  `EachPlayerSearchesLibraryEffect.cardToTop(players)` (`TOP_OF_LIBRARY`); `SearchTargetLibraryEffect` with `CAST_WITHOUT_PAYING`;
  `SeekLibraryEffect` to the battlefield passes through the hand (a library-entry ban doesn't apply) and runs a land's enter abilities;
  "can't be blocked if [condition]" on a creature is a static `ConditionalEffect(condition, GrantEffectEffect(CantBeBlockedEffect, source()))`.
- Player floats, may-draws, random players, exile-and-cast (fold run 5): a resolution's "until end of turn / this combat / until your
  next turn, you [rule]" is `GrantStaticEffectToPlayerEffect` over the rule's static record, its reader asking for the player's floating
  copy beside the battlefield (`hasFloatingEffectOnPlayer`; floated now: `LookAtFaceDownCreaturesEffect`, `AlternativeCostForSpellsEffect`
  "{0}" hand only, `AllowExtraLoyaltyActivationEffect`, `DamageLifeFloorEffect`, `LifeTotalCantChangeEffect`, `MaximumCombatCreaturesEffect`
  with `UNTIL_END_OF_COMBAT`); "[player] may draw a card" is `MayEffect(DrawCardEffect(1, player), ..., chooser)` (`DEFENDING_PLAYER` reads
  the recorded defending player, CR 508.5); "choose an opponent at random ... that player or a planeswalker that player controls" is
  `ChoosePlayerAtRandomEffect` + `DealDamageToPlayersEffect.toPlayerOrTheirPlaneswalker(amount, playersThisWay(CHOOSE))`; "that player
  exiles a card from their graveyard" is `ChooseCardEffect.oneOf(cardsInGraveyards(playersThisWay(CHOOSE), f)).by(playersThisWay(CHOOSE))`
  + `ExileGraveyardCardsEffect.cards(cardsThisWay(CHOOSE))`; `ExileTopCardsAndMayCastSpellsEffect` casts an Adventure-only-fits card as
  its Adventure (CR 716.3a).

- Random picks, created tokens, as-enters sacrifices, returned permanents (fold run 6): "choose N target permanents, destroy one of them at
  random" is `ChoosePermanentsEffect.atRandom(1, Subjects.allTargets(), label)` (the spell's own declared targets; `getTargetIds` leaves
  out those marked illegal, CR 608.2b) + the verb over `thisWay(CHOOSE)`; "create a token ... destroy it at the beginning of the next end
  step" is `ScheduleCreatedPermanentsEffect(DESTROY_AT_END_STEP)`, "sacrifice the token at the beginning of the next end step"
  `SacrificeCreatedPermanentsAtEndStepEffect()`; `SacrificeAllPermanentsAsEntersEffect(PermanentHasSameNameAsSourcePredicate)` is
  Sheltered Valley (the filter's source is the entering permanent; batch-mates and a land losing its abilities as it enters are left alone);
  `ReturnCardFromGraveyardEffect` records what it puts onto the battlefield as `returnedThisResolution()` too, so "it becomes ..." after a
  return is the verb over `returnedThisResolution()` (with `HappenedThisWay.toA(RETURN, filter)` for "if it's a [subtype]");
  `IgnoreSourceAuraEffectsUntilEndOfTurnEffect` is any "ignore this effect until end of turn" (Damping Engine too).
- Chosen animations, flips, any-colour amounts, protection predicates (fold run 6b): "put counters on a [permanent] you control. It becomes
  ..." is `ChoosePermanentsEffect.exactly(1, controlledBy(you(), filter), label)` + the counter and animation verbs over `thisWay(CHOOSE)`
  (Cosmium Confluence); "that player flips a coin. If the player loses the flip, counter that spell" is
  `FlipCoinWinEffect.forTriggeringSpellController(null, new CounterSpellEffect())`, "when you win / lose the flip, ..." is
  `FlipCoinWinEffect` over `QueueReflexiveAbilityEffect` branches (Breeches); "add N mana of any one color" with a counted or conditional N is
  `AwardAnyColorManaEffect(DynamicAmount)` (`DistinctPowersAmongControlledCreatures`, a `PermanentCount`, `FixedIfCondition`, whose estimate
  is its smaller number); "protection from permanents with [kind] counters" is `ProtectionFromPermanentsMatchingEffect(PermanentHasCountersPredicate)`.
- Drafts, random modes, villainous options, parley, step reads (fold run 7): a static `DraftCardFromSpellbookToExileEffect` is the "as this enters,
  draft ... and exile it" of any permanent (`SpellbookDraftToExileChoice.AsEnters` says which entry the answer continues); a hand draft is
  `DraftCardFromSpellbookEffect(printings)`; a "choose one at random" is `ChooseOneAtRandomEffect` (each additional trigger chooses its own mode);
  "each opponent faces a villainous choice" options compose from steps over `targetPlayerDeclaredElsewhere()`; "each player reveals the top card
  ... for each [kind] revealed this way ... then each player draws" is `RevealTopCardOfLibraryEffect(eachPlayer())` + the verb over
  `AmountThisWay.revealedCards(filter)` + `DrawCardEffect(1, eachPlayer())`; "if it's a [kind] card" after a return to hand is
  `HappenedThisWay.toACard(RETURN, filter)`; "that spell's mana value" after countering it is `NoteAmountEffect(TargetSpellManaValue)` +
  `AmountThisWay.noted()`; "populate X times" is `RepeatEffect(X, PopulateEffect)`; a d6 odd/even split is `RollD6Effect` with six branches; a
  filtered per-turn spell limit is `LimitSpellsPerTurnEffect(n, scope, filter)` (Phyrexian Censor); a library put of Auras enters them through the entry
  batch when it has no follow-up (Knickknack Ouphe).

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

- `CantBlockThisTurnEffect(source())`: "this creature can't block this turn" in its own ability (Ydwen Efreet, fold run 8).
- "Remove all [kind] counters from this. If no counters were removed / Otherwise": `RemoveCounterFromSourceEffect(kind,
  MAX_VALUE)` (records REMOVE_COUNTERS) + `ConditionalEffect(NotCondition(HappenedThisWay.removedCounters()), …)` /
  `ConditionalEffect(HappenedThisWay.removedCounters(), …)` (Bounty of the Luxa, fold run 8).
- "Choose a number … equal to that number": `ChooseNumberAtResolutionEffect(0, MAX_VALUE)` (sets X) + the `…EqualsXPredicate`
  filters (Void, fold run 8).

- `AmountThisWay.milledManaValue()` (no filter): the total mana value of the cards a mill step recorded in the public zone they
  went to (CR 701.17c); Fateful Tempest, Heed the Mists, Palantír (fold run 9).
- "You may sacrifice another X. When you do / If you do": `MayEffect(SequenceEffect.of(SacrificePermanentsEffect(1, allOf(X,
  not(PermanentIsSourceCardPredicate)), you()), ConditionalEffect(sacrificed(), …)))`, offering no "may" when nothing can be
  sacrificed; "if you can't sacrifice" is `ConditionalEffect(NotCondition(sacrificed()), …)` after the step (fold run 9).

- `ChooseCardTypeAtResolutionEffect` + `CardHasCardTypeChosenThisResolutionPredicate`: "choose a card type … for each card of the
  chosen type revealed this way" (Blood Oath, Fertile Imagination); the type is kept on `StackEntry.chosenCardType` (fold run 10).
- `PutCardsOntoBattlefieldEffect.manifested()`: "manifest those cards", face down as manifested 2/2s through the entry batch (Scroll
  of Fate); the manifest step `SeekCreatureAndManifestEffect` and Lurker in the Deep's record wait for (fold run 10).
- "Put up to N … from graveyards onto the battlefield under your control": `ChooseCardEffect.upTo(cardsInGraveyards(eachPlayer(),
  filter), n)` + `PutCardsOntoBattlefieldEffect(cardsThisWay(CHOOSE)).underControlOf(you())` (Liliana, the Necromancer; fold run 10).

- `VoteEffect.startingWithYou(options)` / `.secretly(options)`: the VOTE step (`StepKind.VOTE`, `StepOutcome.votes()`), with
  additional votes through `VotingSupport`, open votes logged as cast and secret votes revealed together; word votes only (fold run 11).
- `VoteResult.moreVotes(option)` / `.moreVotesOrTied(option)` / `.anyVotes(option)` in a `ConditionalEffect` for "if [option] gets
  more votes [or the vote is tied]"; `VotesFor(option)` for "for each [option] vote", with `RepeatEffect` for one event per vote
  (fold run 11).

- "Choose two target creatures controlled by the same player; that player sacrifices one of them": `ChoosePermanentsEffect.exactly(1,
  allTargets(), label).chosenBy(controllerOf(allTargets()))` + `SacrificeSelfEffect(thisWay(CHOOSE), controllerOf(thisWay(CHOOSE)))`;
  "the other" is the verb over `except(allTargets(), thisWay(CHOOSE))` (fold run 9b).
- `DiscardEffect.eachEvaluatedFor(amount, players)`: the amount evaluated for each discarding player ("each opponent with more than N
  cards in hand discards the difference", fold run 9b).
- "Look at target player's hand. You may choose a card from it. If you do, …": `ChooseCardsFromTargetHandEffect(... KEEP_IN_HAND,
  upTo, look)` + `ConditionalEffect(HappenedThisWay(CHOOSE), …)` over `cardsThisWay(CHOOSE)` (Oildeep Gearhulk, Vendilion Clique;
  fold run 9b).

- `ConjureCardsEffect` + `ConjureDestination`: the CONJURE step. `atRandomFrom(printings)` / `duplicateOf(PermanentSubject |
  CardSubject)` / `duplicateOfTarget(filter)` into a graveyard, exile, exile with the source, top of library or top N at random;
  `.times(amount)`. Records `StepKind.CONJURE` for `cardsThisWay(CONJURE)`; the log names a card only where it was or goes public
  (fold run 12).
- A perpetually granted graveyard ability is `PerpetuallyGrantAbilitiesEffect(cards, ability.withZones(AbilityZones.GRAVEYARD))`, or
  `PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect` for a target permanent's card, in the one store `perpetualAbilityGrants`,
  never seeded onto a permanent (fold run 12).
- `PerpetuallySetSourceBasePowerToughnessEffect(p, t, PermanentSubject)` over `createdThisResolution()`;
  `ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect(spellbook)` takes any mana value (Blood Age Muster; fold run 12).

- `VoteEffect.forPermanents(subject)` / `.forCards(subject)` / `.forPlayers(subject)` / `.forAnotherPlayer()`, `.castSecretly()`,
  `.forUpToOne()`: votes for objects or players (CR 701.38b); a lone candidate is voted for unasked (CR 608.2d) (fold run 11b).
- `Subjects.mostVoted()` / `votedFor()` (permanents), `mostVotedCards()`, `playersWithNoVotes()`: readers of the last vote's tally;
  `VotesReceived.byPlayer()` / `.byPermanent()`: votes received by the player or permanent an amount is evaluated for (fold run 11b).
- `EachPlayerMayPutCardFromHandToBattlefieldEffect.by(players, predicate, label)`: Show and Tell's put, limited to the players a
  subject names (fold run 11b).

- `AmountThisWay.chosenManaValue()`: "where X is the mana value of that card" after a `ChooseCardEffect` (O-Kagachi; fold run 10b).
- `Subjects.exiledAsCost()` also names the cards a tracked exile-N-from-graveyard activation cost exiled (Coin of Fate; fold run 10b).
- `IntuitionEffect.cards(n)` / `.exactlyDifferentlyNamed(n, filter)`: "search your library for N cards and reveal them", the found
  cards staying in the library as the REVEAL result, then `ChooseCardEffect.oneOf(revealedThisWay()).by(...)` + movers +
  `ShuffleLibraryEffect(false)` (Intuition, Burning-Rune Demon; fold run 10b).
- "An opponent chooses one of the revealed cards": `ChoosePlayerEffect.opponent()` + `ChooseCardEffect.oneOf(revealedThisWay(filter))
  .by(playersThisWay(CHOOSE))`; "the rest" is `revealedThisWay()` once the chosen card has moved (Murmurs from Beyond, Animal
  Magnetism; fold run 10b).

- `ConjureCardsEffect` into a hand or a library position: `ConjureDestination.intoHand()`, `intoLibrary()`, `intoLibraryFromTop(n)`;
  `named(set, number, dest)`; `.into(players)` (the controller conjures into their zones), `.conjuredBy(players)` (they conjure);
  `.withAbilities(...)`; `.repeatingWhileHandSmallerThan(amount)`; `duplicateOf(targetSpell(), dest)` (fold run 12a).
- `PerpetuallyGrantAnyColorManaToCastEffect(CardSubject)`: "it perpetually gains prismatic" / "spend mana as though it were mana of
  any color to cast this spell", usually over `cardsThisWay(CONJURE)` (fold run 12a).
- `Subjects.cardsInLibraries(players, filter)`: a random card from another player's library, only for `ChooseCardEffect.atRandom`
  (fold run 12a).

- `TapPermanentsEffect` records `StepKind.TAP` (the permanents it newly tapped); "tap any number of untapped [f] you control" =
  `ChoosePermanentsEffect.anyNumber(controlledBy(you(), allOf(f, PermanentCanBecomeTappedPredicate)))` + `TapPermanentsEffect(thisWay(
  CHOOSE))` + the verb over `AmountThisWay(TAP)` / `thisWay(TAP)` (fold run 14).
- `QueueReflexiveAbilityEffect.withEventValue(effect, amount)`: the reflexive ability's "that much" / X, fixed as it is made and read
  by its target filter and effect (Yotia, Teferi; fold run 14).
- `GoadTargetCreatureUntilNextTurnEffect.of(subject, duration)` / `.targetWhileSourceRemains()`: goad any non-target subject for
  `UNTIL_YOUR_NEXT_TURN`, `PERMANENT` or `WHILE_SOURCE_REMAINS`; records `StepKind.GOAD` (fold run 14).

- `StepKind.SEARCH`: a library search's found cards and the zone each went to (`SearchLibraryEffect`, `SearchTargetLibraryEffect`,
  `IntuitionEffect`); cards Opposition Agent exiled are not recorded. Readers `HappenedThisWay.searched()`, `toACard(SEARCH, filter)`
  (with the entry's X), `AmountThisWay.searchedManaValue()`, `Subjects.foundThisWay()` (fold run 13).
- "Reveal it; if X put it onto the battlefield, otherwise into your hand": `SearchLibraryEffect(filter, REVEAL_ONLY, null, false)` +
  `PutCardsOntoBattlefieldEffect(cardsThisWay(SEARCH, X))` + `PutCardsIntoHandEffect(cardsThisWay(SEARCH, not X))` +
  `ShuffleLibraryEffect(false)` (Nazahn, Ruin in Their Wake, Treasure Chest; fold run 13).
- `SearchLibraryEffect(filter, EXILE_IMPRINT)`: "search for a card, exile it" on an imprint permanent (Strata Scythe, Hoarding
  Dragon; fold run 13).

- `Subjects.topmostCardInYourLibrary(filter)` (`CardSubject.TopmostInLibrary`): "the topmost [filter] card in your library", hidden,
  logged by `PerpetualChangeLog` (fold run 12b).
- `GameData.grantPerpetualKeywords(cardId, keywords)`: the one write into the merged `perpetualCardKeywords`; P/T changes live in
  `perpetualCardPowerToughnessModifiers` only (fold run 12b).

- `SeparateIntoPilesEffect.permanents(controlledBy(players, filter), label)` / `.cards(graveyardCards, chooser, label)`: "separate …
  into two piles … the pile of [player]'s choice"; the CHOOSE step is the chosen piles, the other pile is `except(set,
  thisWay(CHOOSE))`; each player of a permanent set separates and chooses on their own (fold run 14b).
- `CantAttackThisTurnEffect(except(controlledBy(...), thisWay(CHOOSE)))`: "only creatures in the chosen pile can attack this turn"
  (fold run 14b).
- `SacrificeSelfEffect(thisWay(CHOOSE), player)`: "that player sacrifices all permanents in the pile of their choice", as one event
  (fold run 14b).

- `SearchLibraryEffect(filter, KEEP_IN_LIBRARY, …)` + `PutCardsOntoBattlefieldEffect(foundThisWay()).attachedTo(host)` +
  `ShuffleLibraryEffect(false)`: "search for an Aura [that could enchant it], put it onto the battlefield attached to it"; a card that
  can't be attached stays hidden in the library (fold run 13b).
- `CardCouldEnchantPredicate(permanents)` and `CardSharesNameWithPermanentsPredicate(permanents)`, bound as the search begins;
  `Subjects.attachedToPlayers(players, filter)` (fold run 13b).
- `SearchLibraryForSameNameAsEachPermanentEffect(permanents, libraryOwner, creatureOnly, tapped, optional)`: "for each [permanent],
  search for a card with the same name"; one search, one pick per permanent, records SEARCH (fold run 13b).
- `EachPlayerSearchesLibraryEffect.oneSearchForEach()`: "for each [object] this way, its controller [may] search for a [card]";
  `SacrificeEachMatchingPermanentEffect` is a SACRIFICE step (fold run 13b).

- `BidLifeEffect(bidders, openingBid, prize)` / `.eachPlayerStartingAt(n, prize)` / `.eachPlayerStartingAtAnyNumber(prize)`: the BID
  step (`StepKind.BID`: the high bid, the high bidder); the controller opens, the others follow in turn order; read by
  `AmountThisWay(BID)` and `playersThisWay(BID)`. One `LifeBidChoice` prompt (fold run 14c).
- `HappenedThisWay.toAPlayer(kind, PlayerRelation)`: "if a player is dealt damage this way" (`ANY`), "if you win the bidding"
  (`SELF`) (fold run 14c).

- `Subjects.triggeringCardsInHand()` (`CardSubject.TriggeringCardsInHand`): "that card / those cards" of a put-into-hand or seek
  event; a duplicate reads them as last known (CR 608.2h) through `SubjectResolutionService.cardsToDuplicate(…, CardSubject)` (fold
  run 12c).
- `Subjects.cardsYouOwn(filter)` (`CardSubject.Owned`): "all [filter] cards you own", every zone including the battlefield (physical
  card) and the stack; only for the perpetual verbs; one log line per owner (fold run 12c).
- `PerpetualCardCastCostSupport.change/changeFor`: the one signed perpetual cast-cost store `perpetualGenericCastCostIncreases`
  (fold run 12c).

- `Subjects.playersThisWay(kind, CardPredicate)` (DISCARD, MILL, DRAW, REVEAL) and `playersThisWay(kind, PermanentPredicate)`: the
  players whose recorded cards, or the controllers whose recorded permanents, match (Kroxa, Creeping Dread, Desolation; fold run 8b).
- `Subjects.ownedBy(cards, owners)` (`CardSubject.OwnedBy`): "the card you discarded", found in exile too; `PlayerTappedLandForMana
  ThisTurnPredicate` (fold run 8b).
- `ChoosePermanentsEffect.forEachVote(option, candidates, CONTROLS | OWNS, byVoter, label)`: one choice per vote of the last
  `VoteEffect`, in voting order, then the verb over `thisWay(CHOOSE)` (Elrond of the White Council, Expropriate; fold run 8b).

- `GameData.exiledCardCounters` + `exiledCardCounterCount` / `exiledCardHasCounter` / `addExiledCardCounters` /
  `setExiledCardCounters` / `exiledCardsWithCounter(kind)`: the one store for counters on exiled cards (every kind but time), forgotten
  as a card leaves exile; `CardHasCounterInExilePredicate(CounterType)` for "with a [kind] counter on it" (fold run 16).
- `RemoveCountersFromExiledCardsEffect(cards, counter[, count])`: the REMOVE_COUNTERS step over cards in exile, read by
  `HappenedThisWay.removedCounters()` (fold run 16).
- `PlayFromAmongExiledCardsEffect(exiledCards(players, filter)[, snowManaAsAnyColor])`: static "you may [play lands and] cast spells
  from among …", judged by `SubjectResolutionService.namesExiledCard` (fold run 16).

- The attach verb `AttachSourceEquipmentToTargetCreatureEffect`: any Aura, Equipment or Fortification, a set attachment, step-result
  hosts (`thisWay(kind)`, `returnedThisResolution()`), `.toPlayer(attachments, player)`; legality and the move are
  `EquipSupport.canAttach` / `canAttachToPlayer` / `attach` (control reconciled); records `StepKind.ATTACH` (fold run 15).
- `ChoosePermanentsEffect.thatCanBeAttachedTo(host)` / `.thatCanHost(attachments)`: offer only what the attach verb could attach (fold
  run 15).
- `UnattachEquipmentFromTargetPermanentsEffect(PermanentSubject equipment)`; `TransformSelfEffect` over `granter()`, `thisWay(kind)`,
  `createdThisResolution()`; `CloakTopCardOfControllerLibraryEffect` records MANIFEST (fold run 15).

- `PerpetuallyGrantAlternateCastEffect.evoke(cards, costs…)` / `.ratherThanManaCost(cards, costs…)`: perpetual alternative costs in the
  one store `GameData.perpetualAlternateCasts`, read by `GameQueryService.findGrantedAlternateCast`; evoke's sacrifice trigger added on
  cast (fold run 12d).
- `Subjects.departedThisWay(kind)` (DESTROY, SACRIFICE, EXILE, RETURN): "each [permanent] destroyed / returned this way", names none on
  the battlefield, duplicated as last known; `Subjects.targetDeclaredElsewhere()` (permanent): "that permanent" without declaring a
  target (fold run 12d).
- `ChoosePlayerEffect.chosenBy(player)`: "target opponent chooses a player", "they choose another player" (fold run 12d).

- `BecomeCopyOfSubjectEffect.by(copier)` / `.lasting(UNTIL_YOUR_NEXT_TURN | WHILE_REMAINS_ATTACHED)` / `.except(CopyExceptions)`: who
  becomes the copy (default `source()`, a target, `triggering()`, `attachedAtTrigger()`, a set), how long, with exceptions as copiable
  values (CR 707.9b); `CopyExceptions.withKeywords(...)` / `.stillA(types...)` (fold run 17).
- `PermanentCopierService.lastKnownCopiableCard(gameData, entry, id)`: a departed permanent's copiable values from its departure
  snapshot, not its effect-laden last-known card (CR 707.2, 608.2h) (fold run 17).
- `Subjects.exiledWithSourceMatching(filter)`: "a creature card exiled with this"; a card exiled face down matches nothing (CR 406.3a)
  (fold run 17).

- `SubjectResolutionService.namedCards(gameData, source, controllerId, cards, reads)`: the static list form of a card subject; and
  `namesExiledCard` also judges `ExiledWithSource` (fold run 16c).
- `CardSubject.ExiledWithSource.abilityLink` / `Subjects.exiledWithSourceBy(link, filter)`: "the cards exiled with it" by one linked
  ability (CR 607.2a, 607.5) (fold run 16c).
- `GainActivatedAbilitiesOfExiledCardsEffect(cards, filter, oncePerTurn)` and `GainKeywordsOfCreatureCardsExiledWithSourceEffect(cards,
  cardFilter, watched, copiesProtection, recipient)` (`COMMON_KEYWORDS`): borrowers over any static card subject (fold run 16c).

- `CastCardsWithoutPayingManaCostEffect.anyNumber(cards)` / `.upTo(n, cards)` / `.withTotalManaValueAtMost(amount)`: the cast-now step
  over a `CardSubject`: face-up exiled spell cards, one ordered choice (CR 608.2g), uncast cards stay (fold run 18).
- `MayCastAnySpellFromHandWithoutPayingManaCostEffect.anyNumber()` / `.upTo(n)`: hand free casts with a count, each cast followed by a
  new round of offers (`MayCastFromHandWithoutPayingManaCostEffect.nextOffers`) (fold run 18).

- `GameData.isSuspended(cardId)` / `exiledCardHasSuspend(card)`: the one "suspended" rule (CR 702.62b); time counters are
  `exiledCardCounters` TIME; `CardIsSuspendedPredicate` for "each suspended card [you own]" (fold run 16b).
- `RemoveCountersFromExiledCardsEffect` with TIME removes one counter at a time through suspend's removal; any other kind raises
  `CounterChangeEvent.Kind.COUNTER_REMOVED_IN_EXILE` (fold run 16b).
- `Subjects.sourceCard()` in exile, checked against the card's exile stay (`StackEntry.sourceExileEntryVersion`);
  `SourceCardInExileWithCounter(CounterType)` (fold run 16b).

- `Subjects.picked()` / `picked(filter)` (`PermanentSubject.Picked`): "it" / "those Equipment" after a put that recorded picks; a
  `MayEffect` over picks that name nothing asks nothing (fold run 15b).
- `LibrarySelectionFollowUp.inResolution(effect)`: a library follow-up that is the next step of the same resolution ("if it's …, you
  may …"); `AttachSourceEquipmentToTargetCreatureEffect.pickedEquipmentToCreatureYouControl()` (fold run 15b).

- `BecomeCopyOfSubjectEffect.of(targetCard(...) | targetExiledCard(...) | targetCardExiledWithSource(...))` and `.ofCreatureOrCard(card)`:
  copies of a target card, or "target creature on the battlefield or creature card in a graveyard" as one target (fold run 17b).
- `CopyExceptions.named(n)`, `.alsoLegendary()`, `withBasePowerToughness(p, t)` / `.alsoWithBasePowerToughness`, `.alsoWithKeywords`,
  `.alsoACreatureOfTypes(...)`: name, added supertype, base P/T (CR 707.9d) and replaced types (CR 205.1a) as copiable values (fold run
  17b).

- `GameData.ringStates` (`RingState`) + `ringLevel` / `ringBearerId` / `temptByRing` / `setRingBearer`: the one Ring store (fold run 19).
- `PersistReturnEffect(CounterType)` / `.undying()`: the keyword dies-return (fold run 19).
- `ReselectAttackingCreatureAttackTargetEffect(PermanentSubject attacker, boolean playersOnly)`; `FirstTokenCreationCopiesAttachedPermanentEffect`
  + `EsixTokenReplacementSupport.offerAttachedCopy`; `ControllerSpellsCantBeCounteredEffect(PlayerSubject players, Set<CardType>,
  CardPredicate)` read over active static abilities (fold run 19).

- `CastCardFromGraveyardEffect.of(CardSubject)` (`sourceCard()`, `triggeringCards(filter)`, `targetCard(filter, scope)`) with `.withoutPayingItsManaCost()`, `.spendingManaOfAnyType()`, `.exilingInsteadOfGraveyard()`, `.allowingAdventure()`, `.ifCast(effect)`: the graveyard cast-now offer. Several named cards form one choice group; a free cast pays its cost increases (CR 601.2f) (fold run 18b).
- `CastCardsWithoutPayingManaCostEffect` over graveyard cards (`cardsInGraveyards(you(), filter)`): offered one round at a time, a declined card offered again after each cast (CR 608.2g; Chandra Ablaze) (fold run 18b).
- "If you haven't cast a spell this turn, you may cast … If you do, you can't cast additional spells this turn": `ConditionalEffect(NotCondition(ControllerCastSpellThisTurn(null)), CastCardFromGraveyardEffect.of(…).ifCast(PlayerRestrictionEffect.thisTurn(you(), castingSpells())))` (Conduit of Worlds; fold run 18b).

- `ChooseOneEffect.anOpponentChooses(options)` / `ModeChooser` / `isCastersModal`: "an opponent chooses one —", announced by `StackedModalTriggerService` before priority (controller picks among several opponents, CR 700.2e; targets after the mode, CR 601.2c) (fold run 19b).
- `Subjects.modeChooser()` (`StackEntry.modeChooserId`) and `DrawUpToRecipient.MODE_CHOOSER`: "that player" of an opponent-chosen mode (fold run 19b).
- `MultiPermanentChoiceContext.TargetsAfterOpponentModeChoice(entry, controller, targetFilter)`: the mode's targets chosen after the mode (fold run 19b).

- `SeekLibraryEffect` records `StepKind.SEEK` (cards in the zone each went to, permanents on the battlefield, seekers); `GRAVEYARD` / `EXILE` destinations, `exiledPlayableUntilNextTurn`, `.fromAmongTop(n)`, `.by(seeker)`; the filter is bound by `bindCardSubjects` + `bindAmounts` ("greater/lesser mana value than X") (fold run 20).
- `Subjects.ownersOf(CardSubject)` (`PlayerSubject.OwnersOfCards`): "the exiled card's owner [seeks]" (fold run 20).
- `ChooseOneAtResolutionEffect.secretly(choice)`: "secretly choose land or nonland", mode not logged (fold run 20).
- "Seek N; put one onto the battlefield / keep one and shuffle the rest": `SeekLibraryEffect` + `ChooseCardEffect.oneOf(cardsThisWay(SEEK))` + mover + `PutCardsIntoLibraryEffect.onBottomInRandomOrder(except(cardsThisWay(SEEK), cardsThisWay(CHOOSE)))` + `ShuffleLibraryEffect(false)`; a choice among sought cards is hidden (`CardsThisWay(SEEK).inHiddenZone()`) (fold run 20).

- `Subjects.paidAsCost()` (`PermanentSubject.PaidAsCost`): "the sacrificed / exiled creature" of the cost, read as it last existed; `lastKnownCopiableCard` reads the cost-tracked snapshots (fold run 17c).
- `CopyExceptions.alsoStillA`, `.alsoWithSubtypes` (CR 205.3d), `.alsoColored`, `.withAbilities` / `.alsoWithAbilities` (CR 707.9a); a kept ability is one instance, a second one when the copied values already have it (CR 707.9a, 113.2c), and a kept trigger keeps its target group (fold run 17c).
- `CopyCreatureCardInGraveyardOnEnterEffect.of(cardsInGraveyards(...) | exiledCards(...))` + `.except`, `.gainingHaste`, `.exilingCopiedCard`, `.exilingTwoAddingTheOthersPower`; `CardPutIntoGraveyardFromLibraryThisTurnPredicate` (fold run 17c).

- `PutCardsIntoLibraryEffect.shuffledIn(cards)` / `.alwaysShuffling(players)`: "shuffle it into its owner's library", "their owners shuffle those cards into their libraries"; each library shuffled once, records `SHUFFLE_INTO_LIBRARY` per owner; a library with nothing moved is shuffled only for the named players (CR 701.24c, 701.24d) (fold run 21).
- `ChooseCardEffect.fromEachOwner()`: "choose three cards in each graveyard", "exile a card from each graveyard", one choice per owner in turn order (fold run 21).
- `PutOnTopOfLibraryInsteadOfDyingEffect.onBottom()` / `.shuffledIn()` (Position TOP, BOTTOM, SHUFFLED): the dying-library replacements (fold run 21).
- `SearchOutsideGameForCardToHandEffect(filter, reveals, destination)`, `.unrevealed(filter)`, `.onTopOfLibrary()` (fold run 21).
- `SearchLibraryForOneCardOfEachSubtypeEffect.ofEachColor(colors, HAND | GRAVEYARD)` (fold run 21).

- `DraftCardFromSpellbookEffect` records `StepKind.CONJURE` as each draft is answered; `.times(n)`: "draft a card from [its] spellbook twice", one result (fold run 12e).
- `ConjureCardFromSpellbookToHandEffect(spellbook, count)`: "conjure two cards of your choice from [a] spellbook", records CONJURE (fold run 12e).
- `ExileTopCardOfOwnLibraryEffect.exiledFaceDown()`: "[they] exile the top card of their library face down" (CR 406.3), unnamed, records EXILE (fold run 12e).
- `new CardSubject.TopmostInLibrary(players, null)` for another player's top card, e.g. "a duplicate of the top card of their library" (fold run 12e).
- A card chosen by `ChooseCardEffect` while it is in a hand is logged without its name (fold run 12e).

- `TemptingOfferEffect.of(prompt, players -> payload)`: "each opponent may [X]. For each opponent who does, [X for you]": all decide in turn order, then the payload over `acceptedPlayers()`, then `RepeatEffect(n)` over `you()` (fold run 19c).
- `CreateTokenCopyOfTargetPermanentEffect.forController(PlayerSubject)`: each named player creates, owns and controls the copies (CR 111.2), with no Esix-style offer for them (Glimmervoid Basin, Echocasting Symposium, Fractured Identity, Tempt with Reflections; fold run 19c).
- `PutCounterOnTargetPermanentEffect.placedByEachController()`: "target player puts / each opponent may put a counter on each creature they control"; the permanent's controller is the placer (Shadrix, Tempt with Glory; fold run 19c).

- `AddOnePlusOneCountersEffect(counterType, permanents, players, placedBy, Change)`, with `plusOne` / `twice` / `halfRoundedDown` / `plusOneForYou`, `.placedByYou()` / `.placedByAnOpponent()` / `.alsoOnPlayers()`, and `youControl([filter])` / `anyPermanent()` / `creatureOr(subtypes…)`: every static counter-placement replacement. A placement naming no placer is put by the affected controller; "this permanent" applies to its own entering counters (CR 614.12) (fold run 22).
- `GameQueryService.placementReplacementApplies` / `placementAffects`: the one applicability judge for counter-placement replacements on every placement path, planes and player grants included (fold run 22).

- `EffectResolutionService.resolveNestedInCurrentResolution(gameData, nested)`: steps resolved inside whatever is resolving, without the stack; if they stop for an answer they resume first, then the outer resolution with its next effect (fold run 17d).
- `StackEntry.eventCardExileEntryVersions`: each card's exile stay for a one-or-more event; `triggeringCards` names every exiled card in its stay (fold run 17d).
- `Subjects.triggeringCardsAsTheyLastExisted(filter)` (`CardSubject.TriggeringCards.asLastKnown`): "a card from among those cards" after they left a zone, offered and copied as last known; `cardsToDuplicate(cardsThisWay(CHOOSE))` reads such a chosen card as last known (fold run 17d).
- `BecomeCopyOfSubjectEffect.lasting(WHILE_ATTACHED)`: the "as this Equipment becomes attached" copy kept while attached (Dinosaur Headdress; fold run 17d).

- `LookAtTopCardsAndMayCastEffect.lookAt(n | DynamicAmount)` / `.reveal(DynamicAmount)` + `.matching(filter)`, `.withSpellManaValueAtMost(bound)` ("less than X" = `Sum(X, Fixed(-1))`), `.leavingTheRest()`: "look at / reveal the top N, you may cast a spell from among them without paying its mana cost". The cards wait in the library and the spell is cast from it; a split card is judged by its half (CR 709.3a); choosing to cast takes a do-only-once action (CR 603.2h) (fold run 18c).

- `ShuffleTargetPermanentIntoLibraryEffect.of(subject)` / `.sourceAndTarget(f)` / `.sourceThen(effect, recipient)`: "shuffle [this / enchanted creature / each nonland permanent target player controls / the chosen creature] into its owner's library". One leave event, each library shuffled once (deferred to a following shuffle), CR 701.24c for a departed specific object. Records SHUFFLE_INTO_LIBRARY per owner for `playersThisWay` / `HappenedThisWay` (fold run 21b).
- `RevealTopCardPermanentToBattlefieldEffect(PlayerSubject)`: "each player who [did X] reveals the top card … puts it onto the battlefield if it's a permanent card", one entry batch (fold run 21b).
- `SacrificeSelfEffect(allTargets(), controllerOf(allTargets()), filter)`: "those players sacrifice those permanents", one event (fold run 21b).

- `AddTokenCreationEffect.plus(token, when)` / `.plusThatMany(token)` / `.oneOfEach(when, tokens…)` with `TokenKind.any()` / `creature()` / `artifact()` / `withAnySubtype(…)` and the shared blueprints `TREASURE`, `CLUE`, `FOOD`, `MAP`, `MUTAGEN`, `THOPTER`, `FROG`, `SQUIRREL`, `SOLDIER`: every static token addition, read by `TokenAdditionReplacements` for the tokens' creator after Crafty Cutpurse (fold run 22b).
- `TokenCreationReplacementSupport.withEventModifiers(token, original)`: the one rider rule for added and substituted tokens (attacking, untapped attacking, granted keywords, delayed exile; a noncreature enters tapped instead of attacking, CR 506.3) (fold run 22b).

- `TemptingOfferEffect.offeredOnlyTo(PlayerSubject)`: the offer goes only to opponents who could do the payload (CR 608.2d): `ownersOf(cardsInGraveyards(eachOpponent(), creature))`, `matching(eachOpponent(), new PlayerCanSearchTheirLibraryPredicate())` (fold run 19d).
- `EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect.by(players)`: "return a [filter] card from your graveyard" over any player subject; returners control what they return (fold run 19d).
- `EachPlayerSearchesLibraryEffect.leavingLibrariesUnshuffled()` + `ShuffleLibraryEffect(PlayerSubject)`: "then each player who searched a library this way shuffles" after several searches, one shuffle each (fold run 19d).
- `PlayerCanSearchTheirLibraryPredicate` / `LibrarySearchRestrictions.find`: the quiet "may this player search" check (fold run 19d).

- `SearchLibraryAndOrGraveyardForCardToHandEffect.inZones(filter, Zone...)` / `.mayTakeFrom(filter, Zone...)`: "search your library, graveyard, and/or outside the game", "you may reveal ... from outside the game or choose a face-up ... in exile"; only a library search records, shuffles or answers to Opposition Agent (fold run 21c).
- `SearchLibraryAndOrGraveyardForCardToBattlefieldEffect.inZones(filter, Zone...)` (GRAVEYARD, HAND, LIBRARY): "search your hand and/or library for a [filter] card and put it onto the battlefield", permanent cards only (fold run 21c).
- `Subjects.cardsOutsideTheGame(players, filter)` (`CardSubject.OutsideTheGame`): "cards you own from outside the game", hidden, judged with no game effects; `ChooseCardEffect` and `PutCardsIntoLibraryEffect` handle it (fold run 21c).

- `MoveCounterFromSourceToTargetCreatureEffect.move(kind, from, to)` / `.all(from, to)` / `.anyNumber(kind, from, to)`: the counter move over any subjects (`CounterType.ANY` is a kind the controller chooses). The mover places the counters; CR 122.5 is checked per kind (fold run 23).
- `PermanentCounterSupport.moveCounters` / `removeCountersForMove` / `canReceiveCounter` / `askNextCounterMove` / `answerCounterMove`, with `ChoiceContext.MoveCountersAmountChoice` + `CounterMoveSlot`: the one any-number move prompt (fold run 23).
- `StackEntry.targetsForGroup`: an activated ability's target groups are positional (fold run 23).
- `AddAnotherCounterOfChosenTypeToTargetEffect(PermanentSubject)`: "for each [permanent], choose a counter on it; you may put another" (fold run 23).
- `RemoveAllCountersFromTargetPermanentEffect(Subjects.allPermanents())`: "remove all counters from all permanents"; every form sends the last-counter-removed event (fold run 23).

- `DoubleDamageFromCreaturesEffect.fromSourcesYouControl(permanentFilter, spellFilter)` / `.onlyDoubling(DamageScope.COMBAT | COMBAT_TO_PLAYERS)`: "a [quality] source you control would deal damage", spells judged on their card with their X (`SourceDamageMultiplyingEffect.spellSourceFilter`) (fold run 25).
- `DoubleDamageToOpponentsAndTheirPermanentsEffect(players, permanents)` / `.toPlayersOnly(players)`: recipient doublers over `eachOpponent()`, `chosenPlayer()`, `you()` + `source()`, `enchantedPlayer()`; floated over `you()` on a player by `GrantStaticEffectToPlayerEffect` (fold run 25).
- `DoubleControllerDamageEffect(multiplier, …)` and `DoubleControllerDamageToOpponentsAndTheirPermanentsEffect(multiplier, noncombatOnly, toTheirPermanents)`, floated by `GrantStaticEffectToPlayerEffect.untilEndOfTurn` and read as floating copies on the source's controller (fold run 25).
- `BoostControllerDamageThisTurnEffect(DynamicAmount, color, noncombatOnly)` / `.fromSourcesOfColor` / `.noncombatPlusX()`: "+N this turn" for your sources, fixed on resolution (fold run 25).

- `RegisterDelayedStepTriggerEffect.aboutAtNext(step, Subjects.target(), effects…)`: "destroy / sacrifice target X at the beginning of the next end step", "this creature deals N damage to that creature at end of combat". The registering object declares the harmful target; the delayed ability is about every permanent named, "it" `triggering()`, "them" `triggeringAll()` (fold run 28).
- `SacrificeSelfEffect(Subjects.triggeringAll())`: "sacrifice them" of a delayed ability about several permanents, those you control, as one event (Depthshaker Titan; fold run 28).
- `RegisterGlobalTriggeredAbilityEffect.nextThisTurn(ability)`: "when you next cast / activate … this turn": triggers once and is used up (CR 603.7b). It keeps the noted amount and works in a mana ability through `RegisterGlobalTriggeredAbilityManaAbilityEffectHandler` (fold run 28).
- `AiUtils.withEffectsDelayedAboutTheTarget(effects)`: the AI's damage readers look into a delayed ability about the target (fold run 28).

- `BecomeChosenColorsUntilEndOfTurnEffect.chosenColors(subject, d)` / `.chosenColor(subject, d)` / `.fixed(colors, subject, d)`: "[permanents] become the color(s) of your choice / [colors] / colorless" for a `Duration`, one prompt over the whole set, nothing asked when the subject names none (fold run 31).
- `GrantColorEffect.chosenColor(subject[, filter])`: static "[permanents] are the chosen color" from the source's stored choice; over `source()` it is not a CDA and takes the choice's timestamp (fold run 31).
- `BecomeNightEffect.day()` / `.toggle()`: "it becomes day", "if it's night, it becomes day; otherwise night" (fold run 31).
- `SetCardTypesEffect` one-shot over any subject (Hydro-Man's `source()`, `UNTIL_YOUR_NEXT_TURN`) and `SetTargetPermanentSupertypeEffect(supertype, gained, subject)` (fold run 31).

- `AllowCastFromTopOfLibraryEffect.anySpell()` / `.payingLifeEqualToManaValue()` (`alternativeCost`, CR 118.9): "cast spells from the top", and Bolas's Citadel's life alternative (fold run 30).
- Library-top permissions given by a resolution are `GrantStaticEffectToPlayerEffect.untilEndOfTurn` over `LookAtTopCardOfOwnLibraryEffect`, `PlayLandsFromTopOfLibraryEffect` and `AllowCastFromTopOfLibraryEffect`. `CastingPermissionService.libraryTopGrants` reads the floating copies first, then the permanents' statics (fold run 30).
- `AllowCastMatchingCardsFromGraveyardThisTurnEffect.fromGraveyardsOf(players)` / `.exilingInsteadOfGraveyard()` / `.spendingManaOfAnyType()`: "from that player's graveyard" or "from any graveyard" (fold run 30).
- `AllowCastTargetCardFromGraveyardThisTurnEffect.lasting(Duration)` / `.asAdventure()` (CR 716.3) over `sourceCard()`: "cast it from your graveyard as an Adventure until the end of your next turn" (fold run 30).

### Blocked causes still open

Counts are ledger rows of existing classes whose latest note names the cause (approximate, by keyword).

| Cause | Rows | What would unblock it |
|---|---|---|
| First half prompts mid-step (`blocked-step-model`) | 140 | moving the handler onto the step model (`leave-battlefield-redesign.md` step 14) |
| Each player chooses, then all act (`blocked-player-loop`) | 97 | a choose-then-act loop (not in this plan) |
| Cost combinators, "counter unless", cost heads | about 85 | step 10 (sign-off): option C and the cost-combinator merge |
| A delayed trigger shape (end-of-combat, coin flip, the old delayed mechanism) | about 20 | the delayed-mechanism unification onto `RegisterDelayed*` |
| Planar deck cards | 9 | a card subject for the planar deck with its reveal, bottom-order and planeswalk steps |
| Next-draw replacement payloads (`DrawService`'s inline payloads) | about 20 | a "this draw was replaced" record in `DrawService` |
| Owner's questions (perpetual "it", gift opponent) | 5 | the owner's decisions below |
| A token controller on `CreateTokenEffect` / the token copies | 5 | a `PlayerSubject` token controller with the Esix, investigate and attack paths agreeing; the token copies have one since fold run 19c (`forController`), the four `CreateTokenFor*` records remain |
| Out-of-battlefield characteristics (the all-zones grants) | 5 | a redesign of the out-of-battlefield characteristic queries (outside this plan) |
| One or two classes each | most of the other blocked-other rows | a third user; each note names the part |

The stale step-4/5 rows and the seven older `step-9` rows this section listed were re-triaged by triage run 1 and the consolidation;
the 3+-card fold leads are Fold queue rows (`delayed-leads`, `life-lock`, `copier`).

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

### The owner's open questions

- **"Counter unless"** (8.27, 8.28): the orchestrator took option B in 8.28 (the ransom records stay; the declined ransoms counter
  through `CounterSupport`) and set option C aside for step 10 (one `CounterUnlessEffect`-shaped record replacing every "counter
  unless" record, ward {N} included). The owner hasn't confirmed either; ask before step 10 builds option C. About 20 blocked rows
  wait for it (the ransom records, the ward variants, the per-object ransoms).
- **The perpetual "it"** (CR 400.7): whether a perpetual change follows a card that changed zones before the grant resolved. Holds
  `PerpetuallyGrantAbilityToTriggeringCardEffect` (the triggering card anywhere; Forgeborn Phoenix), `PerpetuallyBoostTriggeringCardEffect`
  (4 cards), Rothga's and Mutable Pupa's records against `Subjects.triggeringCards()`. 3 rows wait for it (Rothga's, Mutable Pupa's,
  Mephidross Slime's `ConjureCardIntoGraveyardThenDoubleAndShuffleEffect`).
- **The gift opponent** (CR 702.174a, `missing-features-to-do.md` entry 80): no gift opponent is recorded; every gift handler
  (`TargetOpponentCreatesTokenEffect`, `ConjureCardToOpponentBattlefieldEffect`, `EachOtherPlayerDrawsCardEffect`, ...) gives it to
  `getOpponentId`, every other player, or the spell's target, right with two players only; the gifts of Cruelclaw's Heist, Consumed by
  Greed and Perch Protection never raise the gift-given event (CR 702.174c), so Jolly Gerbils misses them. 2 rows wait for it
  (`EachOtherPlayerDrawsCardEffect`, `TargetOpponentCreatesTokenEffect`).
- **Face-down board views** (`missing-features-to-do.md` entry 81): a player can't tell their own face-down permanents apart on the
  board or in id-only prompts; needs a per-viewer permanent view and client work. No ledger row waits for it.

### Fold and defect queues

The fold queue (299 batches over 1,322 `step-9` rows, ordered into 51 fold runs), the defect queue (220 defects in nine groups,
proposed as twelve defect runs) and the re-triage list are in `effect-cleanup-queues.md`. A fold or defect run reads its own rows
there and updates them when it lands.

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
- **Player grants**: `GrantPlayerStaticEffectsUntilEndOfTurnEffect` (10 cards, Angel's Grace) is `GrantStaticEffectToPlayerEffect.untilEndOfTurn` over a list,
  plus its escape-grant snapshot; merge them (fold run 5).

## Findings

Each run appends its section below the last one. Older sections are in git history (see the top); triage runs 1 to 7 and parallel
triage chunks 01 to 16 are merged into the Fold queue, Defect queue and Re-triage sections above (their text:
`git show 51f0afd546:effect-cleanup-refactor.md`).

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
| cast the library top for free (Galvanoth) | `LibraryChoiceHandlerService.handleCastWithoutPayingChoice` since fold run 18c (`handleCastFromLibraryChoice` is gone) | stays on top | `GalvanothTest` (Teferi) |
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

### Step 9, defect run D10 (2026-11-23)

Defect run D10 (logs and hidden information) with the owner's hidden-zone logging decision, no folding. Ratchet unchanged at
4,044 / 3,299 (no record went away; `PerpetualChangeLog` is a helper, not a handler). Ledger: 73 rows appended for the rows that waited on the
decision (71 `step-9`, `ConjureSpellbookIntoLibraryEffect` `survivor`, `DestroyTargetPermanentThenPerpetuallyBoostEffect` `blocked-other`).
Two card files changed (Pinchy McStingbutt's `.revealed()`; no card rewritten otherwise). Oracle text checked with `get_card` for
Hydroponics Architect, Undercity Plunder, Clone Crafter, Agent of Raffine, Juggle the Performance, Runecarved Obelisk, Cerise, Chaos Balor,
Naktamun Shines Again, Holographic Double, Spellchain Scatter, Scalesoul Gnome, Kithkin Brinefarer, Tome of Gadwick, Pinchy McStingbutt,
Stormkeld Curator // Giant Secrets, Mischievous Lookout, Kavaron Consumed, Housemeld, Eumidian Lifeseed, Tireless Angler, Saint Elenda,
Glimmer Hoarder, Relics of the Rubblebelt, Support Skyforge, Swine Rebellion, Ravenous Pursuit, Molten Sentry and Rakdos, the Showstopper;
rulings with `get_rulings` for Juggle the Performance (none) and Expert-Level Safe. CR 400.2, 402.3, 406.3, 705.2, 705.3, 707.2, 708.2,
708.2a checked with `get_rule` (2026-11-23).

**Can the log address one player?** No. `GameLogEntry` has segments and no audience; `GameLogService.record` emits
`GameLogAppended` to `GameEventAudience.allPlayers()`, and `JoinGame` sends the whole log to every seat. No per-player line was built: the
owner's hand shows the card. The rule is under Conventions ("The public log says only what the card's text makes public") and in
`agent-docs/architecture/casting-stack.md`.

| Defect | Verdict | Test (failed first?) |
|---|---|---|
| `DraftCardIntoHandEffect` named the drafted card; so did the draft mode label, `SpellbookDraftChoice` (into a hand), `SpellbookCardChoice` (conjure or draft into hand, may-cast, enchantment), the draft-twice and the artifact-creature draft lines | fixed: "X drafts a card from S's spellbook into their hand.", the mode label of a draft into a hand unlogged (`ChooseModeChoice.secret`, `PlayerInputService.beginSecretChooseModeChoice`), the card named only when it goes public or is revealed; the Skyforge selection is `withRevealSelected(false)` and the rest-to-exile branch honours it | Eumidian Lifeseed, Tireless Angler, Saint Elenda, Glimmer Hoarder, Relics of the Rubblebelt, Support Skyforge, Swine Rebellion (yes, all) |
| Hydroponics Architect named its random library card | fixed: "A random land card in X's library perpetually becomes ..." | `HydroponicsArchitectTest.logNamesNoLibraryCard` (yes) |
| `PerpetuallyGrantAbilitiesEffect`, `PerpetuallyChangeCastCostEffect` logged one line per hidden card (a count) | fixed: `PerpetualChangeLog`, one "Each affected card in X's hand ..." line per hand or library, also when none matches | Rope-Line Attendant (2), Racketeer Boss (yes, all) |
| `ChooseCardEffectHandler` logged one line per hidden chosen card | fixed: one line per hand or library, "a card" only where the instruction picks one | `RacketeerBossTest` (yes) |
| Naktamun Shines Again counted the boosted cards | fixed: the line states the instruction | `NaktamunShinesAgainTest.chapterILogsNoCount` (yes) |
| Undercity Plunder, Clone Crafter / Lae'zel, Agent of Raffine, Juggle the Performance named a duplicate of a random or top library card; Holographic Double of a hand card, Kithkin Brinefarer of a drawn card | fixed: `GameLog.conjuresIntoHand` ("X conjures a card / 7 cards into their hand."); Holographic Double's hand choice (`RevealedHandChoice` keep-in-hand) names no card | six card tests (yes, all) |
| Tome of Gadwick, Giant Secrets revealed and named their random spellbook card; only Pinchy McStingbutt says "and reveal it" | fixed: `ConjureRandomCardFromSpellbookToHandEffect.reveal` (`.revealed()`, Pinchy); unrevealed, "a card" | `TomeOfGadwickTest.conjuredCardIsNotRevealed` (yes); `PinchyMcStingbuttTest` still passes |
| Runecarved Obelisk / Cerise and Chaos Balor named the sought card | fixed: "seeks a card into their hand" | Runecarved Obelisk, Chaos Balor (yes) |
| Second person in the shared log (twelve conjure handlers, Storyweave) | fixed: the player is named | Warzone Duplicator, Chitinous Crawler, Storyweave (yes: those three handlers were edited back for the failing run); the other nine handlers have no new test |
| Molten Sentry, Rakdos logged a winner of a heads-or-tails flip (CR 705.2) | fixed: "X's coin flip for S comes up heads / tails" (no event changed) | Molten Sentry, Rakdos (yes) |
| `MayCopyHandlerService.handleBecomeCopyChoice` logged a lost target after a copy | fixed: "X becomes a copy of Y." | `CryptoplasmTest.copyIsLoggedAsCopy` (yes) |
| Expert-Level Safe's mismatch exile with a look permission | not a defect in behaviour: the look is granted only to a spell's exile (`ExileTopCardsToSourceEffectHandler`) or read from the card's printed effects (the view), so the engine-built exile gave none; the record now says `faceDownWithoutLookPermission(1)` as the ruling does | `ExpertLevelSafeTest.mismatchExileGivesNoLookPermission` (no; kept as a guard) |
| Grant handlers naming a floating effect's source | not a defect: `GameData.addFloatingEffect` relabels it (`withPublicSourceName`) | none |
| Laquatus's Creativity at an empty hand | not a defect: "discards 0 cards." tells nothing a hand count doesn't (CR 402.3) | none |
| Malik and Vault 11's secret choices; Mycosynth Gardens and Valki's missing line; Moonring Mirror / Duplicity's look, the face-down Aura host, `cardsThisWay(REVEAL)` | left: the vote fold; a missing line breaks no rule; not verified this run | none |

**The 73 waiting rows.** Re-verdicted from their notes, each note's fold re-read: 6 join `seek-step` (run 20), 4 `drafted-step` and 3
`conjure-dup` (run 12); new batches `conjure-named` (8 + the survivor) and `dup-hand` (21) as fold run 12a, `perp-hidden-sets` (12),
`perp-chosen` (11), `perp-owned` (3) and `perp-topmost` (3) as fold run 12b. Changed from the note: Ravenous Pursuit's record folds onto
`PerpetuallyBoostCardEffect` (it takes a `DynamicAmount`, so the one-card amount run 37 feared isn't needed); Territorial Strike's stays
`blocked-other` (the beheld object has no subject and one reader). The fold runs keep the logging rule; each ledger note says the line.

**Behaviour changes.** Log lines only, but the Tome of Gadwick / Giant Secrets reveal (a `REVEAL_HAND` message to every player) is gone, and
Holographic Double's, Swine Rebellion's and Relics' keep-in-hand prompts no longer name the card ("X keeps the chosen card(s) in Y's hand.").
A `LibraryRevealChoice` that puts a card into a hand and exiles the rest now names that card only with `revealSelected` (the
default stays true; Support Skyforge's selection is the caller set to false; Browse and Plunge into Darkness, which may use the branch,
passed unchanged).

**What a player sees.** No new prompt. The drafting player's mode prompt is unchanged; the other player no longer reads the drafted,
sought, conjured or perpetually changed hidden card in the log.

**Shared-engine changes.** `ChooseCardEffectHandler` (hidden choice line), `CardChoiceHandlerService` (keep-in-hand lines), `LibraryChoiceHandlerService`
(rest-to-exile honours `revealSelected`), `ChoiceHandlerService` + `PlayerInputService` + `ChoiceContext.ChooseModeChoice.secret`,
the three spellbook interaction handlers, `GameLog.conjuresIntoHand`, `PerpetualChangeLog`, `MayCopyHandlerService`, the two flip handlers.

**Seen, not changed.** In the Defect queue's group 8: the may-copy handler copies `targetPerm.getCard()`, not `cardToCopy` (CR 707.2,
708.2); Support Skyforge's unchosen spellbook cards are exiled and named; Juggle the Performance picks seven from a smaller library; a
hidden "up to" choice of none and a random pick that finds none log nothing.

**Tests edited.** `MoltenSentryTest`, `RakdosTheShowstopperTest` and `KrarksThumbTest`'s two Molten Sentry cases read "comes up heads /
tails" (they read the old flip line); `TirelessAnglerTest` expectation set before the run. New cases in 26 card test classes. No assertion
was loosened.

**For the next run.** Fold run 1 of the Fold queue; fold runs 12a and 12b take the rows this run unblocked, after run 12's CONJURE step.

### Step 9, fold run 1 (2026-11-23)

Fold run 1 of the Fold queue: batches `dead`, `p-cost`, `p-lib` (28 rows). No "Fold:" defect line named these batches. Ratchet
4,044 / 3,299 → 4,025 / 3,286 (19 records; 13 normal, may and static handlers, plus two cost-modification handlers). Ledger: 28 rows
appended. 15 card files changed (4 only lost an unused import); Oracle text checked with `get_card` for all 11 rewritten cards but
Memory Bank (no Scryfall entry: its reminder text from a retailer listing, through a web search), and for Don & Raph and Strong Back;
rulings for Strong Back and Guided Passage. CR 118.9d, 601.2f, 602.2b, 608.2d, 608.2h, 614.1c, 614.12, 701.29a, 702.41a, 702.151a,
708.4 checked with `get_rule`.

| Batch | Result |
|---|---|
| `dead` (10) | all deleted: `git grep -w` found only imports; with them the Mimeoplasm choice chain (`CloneService`, `MayCopyHandlerService`, `GraveyardChoiceHandlerService`, three `CloneOperationState` fields), the void-counter permission branch, `WheelOfMisfortuneState`, `MultiPermanentChoiceContext.GoadDamagedPlayerControls` |
| `p-cost` (7) | 5 absorbed (Kaza, Maelstrom Muse; Diary of Dreams; Chromatic Armor, Skeleton Scavengers; Momo; Damping Sphere); 2 re-batched into `targets-x` (the equip-targeting and graveyard-target reductions need a target kind and an attached subject, not parameters) |
| `p-lib` (11) | 4 absorbed (Braids, Curious Forager, Memory Bank, Quickbeast Amulet); 7 `blocked-other`: Mana Echoes (the count's filter has no triggering permanent), Oath of Druids (the player-target declaration, as Oath of Ghouls), Ajani (look vs reveal), Counterlash (the target spell's types after it left), Fateseal (the decision maker of an untargeted library), Guided Passage (one prompt over three categories with a land creature), Phyrexian Portal (look, both piles face down) |

**Read at the same moment (p-cost).** The activation modifiers are read only through `ActivationCostModifierEffect.amount()` /
`reducesGenericCost()` at activation (CR 602.2b), so the counter forms say the same as `CountersOnSource`. Momo's reduction stays a
battlefield cost modifier (CR 601.2f), gated by `ControllerTurn` as Geyser Drake's is. Kaza and Maelstrom Muse read their X as the
ability resolves (CR 608.2h): the survivor's handler now fixes a `DeterminedOnce` amount, as it fixed `XValue` and `EventValue`. The
triage note said to lock every amount; that would have broken Saheeli, the Gifted and Don & Raph, whose affinity counts at cast
(CR 702.41a).

**Behaviour changes.**
- Damping Sphere's tax now applies to an alternative cost (CR 118.9d; `IncreaseSpellCostEffectHandler.modifyAlternateCost`):
  `DampingSphereTest.taxAppliesToAlternativeCost` (Ragavan's dash). Not run on the old code (the old handler had no alternate-cost
  hook, so `getAlternateHandCastCostModifier` added nothing).
- Strong Back no longer reduces reconfigure (CR 702.151a; its ruling): `StrongBackTest.doesNotReduceReconfigureCost`, failed first.
- Momo judges a face-down cast by its face-down characteristics (CR 708.4; the survivor's), no test (no flying morph at hand).
- Braids: the card choice prompt (the hand only to its chooser) instead of the hand-card prompt; a lone eligible card is put unasked
  (CR 608.2d); the put goes through the entry batch. Curious Forager's "when you do" carries the resolution's results and reads
  "'s reflexive ability". Memory Bank logs "Memory Bank is exiled."

**Tests changed.** `BraidsConjurerAdeptTest` answers the card choice (five cases; one also checks the other player is sent no hand card
id); `CastingCostServiceTest`'s two Damping Sphere cases build the survivor; `CostModificationTestRegistry` and
`CostEffectClassificationTest` lose the deleted classes. New: the Damping Sphere and Strong Back cases.

**Shared pieces.** None new; `ReduceCastCostForNextMatchingSpellEffect`'s `DeterminedOnce` lock and `IntensifySourceCardEffect`'s
`DynamicAmount` are under Standing knowledge.

**Seen, not changed.** Oath of Druids skips the entry batch (Defect queue group 9). Fateseal's `getOpponentId` was already in group 2.

**For the next run.** Fold run 2 (`p-dmg`). Several `p-…` notes name a head that needs a component or a reader change; check each
against the survivor's readers first, as this run's seven `blocked-other` rows show.

### Step 9, fold run 2 (2026-11-23)

Fold run 2 of the Fold queue: batch `p-dmg` (32 rows) and its one "Fold:" defect line (Wicked Guardian). Ratchet 4,025 / 3,286 →
4,003 / 3,263 (22 records; 23 handlers, `PowerToughnessBoostEffectHandler` included). Ledger: 32 rows appended (absorbed 14, split 9,
unique 4, blocked-other 5). 28 card files rewritten, Oracle text checked with `get_card` for all 28 and for the 4 left cards
(Malignant Growth, Medomai's Prophecy, Psychic Transfer, Mage Slayer); rulings for Wicked Guardian. CR 603.7b, 608.2d, 608.2h, 611.2a,
613.4c, 614.6, 701.12a checked with `get_rule`.

| Group | Result |
|---|---|
| chosen type (4) | Distant Melody, Harmonized Crescendo, Pack's Disdain, Roar of the Crowd, Coordinated Barrage: `ChooseCreatureTypeAtResolutionEffect` + the verb over `PermanentCount(PermanentHasTypeChosenThisResolutionPredicate, CONTROLLER)`; Crippling Fear left (`blocked-other`: `BoostAllCreaturesEffect` is no `BoardWipeEffect`) |
| draw (8) | Archivist of Gondor and the monarch's draw (`triggeringPlayer()`), Bebop (noted X), Sudden Salvation, Anticausal Vestige (draw, then the may-put), Beck (`RegisterGlobalTriggeredAbilityEffect`), Loran (`allOf(you(), targetPlayerDeclaredElsewhere())`); Black Panther / Farideh left (`blocked-other`: the AI's `CardDrawingEffect` view of Black Panther's ability), Malignant Growth left (`blocked-other`: replaced draws) |
| damage (9) | Wicked Guardian, Disorder, Flames of the Raze-Boar, Burn the Impure, Aether Charge, Lavabelly Sliver, Sonic Shrieker, Firesong and Sunspeaker (onto `DealDamageToAnyTargetEffect` + a creature-or-player filter); left: Comet's resolution-time choice (`unique`), Mage Slayer and Play with Fire (one-card parameters) |
| life, win, boost (11) | Foul-Tongue Shriek, Tezzeret, Nihiloor, Long Rest, Luck Bobblehead, Call the Spirit Dragons' engine step, Blessing of the Nephilim, Elemental Appeal, Riding the Dilu Horse and Xathrid Gorgon (`PowerToughnessBoostEffect` stays as the boost payload); left: Psychic Transfer and its exchange survivor (`unique`), Medomai's Prophecy (`blocked-other`: the name must be fixed as chapter III resolves) |

**Where the triage was wrong.** Pack's Disdain's note would have flipped its AI target to BENEFICIAL (a count of 0 evaluates to 0):
`TargetPolarityClassifier` now reads a negatively `Scaled` amount as a debuff (`TargetPolarityGuardTest.negativePerCountBoostIsHarmful`;
it would fail without the change by reading, not run so). Medomai's note would read the Saga's name at cast time, not as chapter III resolves. Malignant Growth's
"cards they drew this way" is not `AmountThisWay(DRAW)` while a replaced draw records nothing. Two notes kept AI views a wrapper hides.

**Shared pieces widened** (each used by the cards above and open to the next): `BoostTargetCreatureEffect` takes
`createdThisResolution()` and keeps `Duration.PERMANENT` (logged "indefinitely"); `DealDamageToTargetCreatureEffect` takes
`thisWay(kind)` (each creature the step named, the targeted-creature prevention check).

**Behaviour changes.**
- Wicked Guardian: no draw when no other creature was chosen ("If you do"; its ruling: prevented damage still draws), a lone other
  creature is chosen unasked (CR 608.2d): `WickedGuardianTest.acceptingWithNoOtherCreatureDoesNotDraw` (fails on the old code by
  reading: the old sequence drew after an empty choice; not run on it).
- Aether Charge's Beast deals the damage through `DamageSupport.entryDealtByTriggeringPermanent` (its carrier entry, CR 608.2h).
- Logs: the survivors' lines (Elemental Appeal's per-creature line, Beck's registration line, an unmet top-level `ConditionalEffect`'s
  "does nothing" line for Long Rest, Burn the Impure, Call the Spirit Dragons; Anticausal Vestige's prompt names no number).
- AI only: Pack's Disdain-shaped boosts (and Induce Despair, Flunk, Escaped Experiment) are HARMFUL at a count of 0; Disorder, Roar of
  the Crowd and Loran now carry the survivors' damage and draw capability interfaces.

**Tests changed.** `WickedGuardianTest`: the prompt test adds a second creature; three tests no longer answer a one-creature prompt
(CR 608.2d); display names added; one new case. `TargetPolarityGuardTest`: one new case.
`DealDamageToTargetControllerIfTargetHasKeywordEffectHandlerTest` deleted with its handler (`BurnTheImpureTest` covers the card).

**Tests run.** 51 test classes that name a rewritten card (`git grep -lwF 'X.class'`), 18 survivor and neighbour classes, 28
`Mastery|DeathReplacement` classes (the deleted drain handler asked death replacements first), `TargetPolarityGuardTest`,
`EffectCountRatchetTest`, `DealDamageToTargetCreatureEffectHandlerTest`, `LayerClassifierTest`, `StepTriggerServiceTest`: 117 XML
files, 1,195 cases, 0 failures.

**Seen, not changed** (Defect queue): Disorder's damage is two events (group 3); `DrawCardEffectHandler` records no cards for a replaced
draw (group 4); `DealDamageToTargetCreatureOrPlayerEffect`'s targeted form is dead (group 9). Re-triage: Luminescent Rain's
`GainLifePerChosenTypeCountEffect` now folds as its chosen-type siblings did.

**For the next run.** Fold run 3. Before a `p-…` note's fold, check every AI reader of the absorbed record's capability interfaces and
what a dynamic amount evaluates to in the AI's estimation context: a count over a resolution choice is 0 there.

### Step 9, fold run 3 (2026-11-23)

Fold run 3 of the Fold queue: batches `p-perm`, `sac-singles`, `destroy-singles` (29 rows); no "Fold:" defect line named them.
Ratchet 4,003 / 3,263 → 3,984 / 3,246 (19 records, 17 handlers; the `GlobalMustBlockEachCombatEffect` interface went too). Ledger: 29
rows (absorbed 10, split 9, survivor 1, `blocked-other` 6, re-batched 3). 24 card files rewritten, Oracle text checked with `get_card`
for all 37 cards of the batches; rulings for Turnabout. CR 101.4, 118.12a, 400.7, 608.2b, 608.2d, 608.2h, 611.2a, 613.1f, 701.44d
checked with `get_rule`.

| Batch | Result |
|---|---|
| `p-perm` (15) | 12 folded: Arthur (`searched()`), Soul Sculptor and Phyrexian Splicer (split onto the type, lose-all and keyword records), Proteus Machine, Hei Bai's Spirits (one static ability of the two restrictions), Rhythm of the Wild and Uncivil Unrest (`GrantKeywordEffect(RIOT, …, nontoken)`), Grand Melee and Invasion Plans, Mirkwood Trapper, Protean Hydra, Hakbal, False Orders, Inner-Flame Igniter. Left: Horizon Boughs (a plane's every-player untap set), Turnabout (both choices on resolution; `ChooseOneAtResolutionEffect` can't declare its player target), and `CanOnlyAttackAloneEffect` stays the self record that `attached-restr` (run 37) wraps for Errantry |
| `sac-singles` (6) | 2 folded: Dispense Justice and Ends (`SacrificePermanentsEffect`, metalcraft as `FixedIfCondition`), Bringer of the Last Gift (sacrifice step, then the graveyards' creature cards except `cardsThisWay(SACRIFICE)`). Left: Vaevictis to `perm-shuffle`, Prototype X-8 to `conjure-dup`; the two return-or-sacrifice records (5 cards) `blocked-other` for step 10 |
| `destroy-singles` (8) | 5 folded: Saproling Burst and Tombstone Stairwell (`PermanentCreatedBySourcePredicate`), Extinction and Kindred Dominance, Root Greevil (as Wash Out), Chain of Acid (as Chain of Smog, `TARGET_PERMANENT_CONTROLLER` for the last-known controller), Light of Judgment. Left: Broken Visage to `delayed-created`, Heliod's Punishment (a condition over the granter), Krenko's Buzzcrusher (`oneForEach` is exactly one) |

**Where the triage was wrong.** The return-or-sacrifice note's `MayEffect(ReturnChosenPermanentsToHandEffect, prompt, SacrificeSelfEffect)`
would keep the creature after an accepted payment with nothing left to return (CR 118.12a), so the payment stays (cost-shape) and step 10
merges it. Turnabout's `ChooseOneEffect` is cast-time; its ruling puts both choices on resolution. Chain of Acid's
`TARGET_PLAYER_OR_PERMANENT_CONTROLLER` finds no controller once the permanent is destroyed: `TARGET_PERMANENT_CONTROLLER` reads it last
known (CR 608.2h). Broken Visage's "AmountThisWay destroyed-power" would make a 0/0 for an indestructible target; `TargetPower` /
`TargetToughness` read it, but the end-step sacrifice of the created token is `delayed-created`'s piece. Master of Cruelties' subject
fold is `attached-restr`'s (Errantry wraps the self record).

**Shared pieces widened** (Standing knowledge, Shared pieces): `MustBlockEachCombatEffect(PermanentSubject)` with
`GameQueryService.isNamedByStaticMustBlockSet`; `TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect`'s `Duration`; `ExploreEffect`
over a set (the order choice moved into `ExploreEffectHandler`); `BalduvianWarlordEffect.thenMayReblock()` (one removal path,
`CombatRemovalSupport`, for both cards); `LosesAllAbilitiesEffectHandler` logs "until a player casts a creature spell".

**Behaviour changes.**
- Master of Cruelties that lost its abilities (or is face down) may attack with other creatures: `CombatAttackService.canOnlyAttackAlone`
  reads the active static effect (CR 613.1f). `MasterOfCrueltiesTest.attacksWithOthersAfterLosingAbilities` (fails on the old code by
  reading: the old read was the card's printed static effects; not run on it).
- Survivors' guards now apply: Phyrexian Splicer doesn't grant a keyword its recipient can't have; Dispense Justice and Ends skip
  permanents that can't be sacrificed and sacrifice-protected players, and sacrifice the chosen attackers as one event (CR 101.4);
  Soul Sculptor's enchantment also loses artifact and land subtypes (`SetCardTypesEffect`'s layer-4 branch); Grand Melee's requirement is
  read through `activeStaticEffects` by the engine and the AI alike (the AI's scan ignored a Grand Melee that lost its abilities).
- Prompts and logs (the survivors' lines): Mirkwood Trapper's choice is `RecordChosenPermanents` with a "chooses" line; Root Greevil's colour
  prompt is the spell colour choice; Dispense Justice's "has no matching permanents to sacrifice"; Balduvian Warlord logs the removal;
  Inner-Flame Igniter's and Proteus Machine's lines; Soul Sculptor now logs the loss of abilities.
- AI only: Kindred Dominance is valued (and seen on the stack) as a wipe of every creature, Extinction of none, since a resolution-time
  type choice has no value in the AI's estimate; before, neither record was a `DestroyAllPermanentsEffect`.

**Tests changed.** `MirkwoodTrapperTest` (the choice context is `RecordChosenPermanents`; display names added), `DispenseJusticeTest`
(the context is `ForcedSacrifice`; the survivor's "no matching permanents" log line), `TargetPolarityGuardTest` (False Orders' record).
New: the Master of Cruelties case.

**Tests run.** 146 classes (every test class naming a rewritten card or a survivor's card, `git grep -lwF 'X.class'`; four explore
cards; the 28 `Mastery|DeathReplacement` classes; `CombatAttackServiceTest`, `CombatBlockServiceTest`, `BattlefieldPlacementServiceTest`,
`GameQueryServiceTest`, `SubjectResolutionServiceTest`, `LayerClassifierTest`, the return-or-sacrifice handler test, the three AI
decision-engine tests, `TargetPolarityGuardTest`, `EffectCountRatchetTest`): 248 XML files, 2,213 cases, 0 failures. A first run's card-test JVM for letter `m`
died of a SIGSEGV in the garbage collector (hs_err in the module directory, removed); the rerun passed.

**Seen, not changed** (Defect queue group 6): the return-or-sacrifice payments find "it" by card id, so a source that left and came
back is sacrificed (CR 400.7).

**For the next run.** Fold run 4. Several singles notes name a head whose option exists only for "exactly one" (`oneForEach`) or only
at cast (`ChooseOneEffect`); read the head's constructor guards before taking the note's composition.

### Step 9, fold run 4 (2026-11-23)

Fold run 4 of the Fold queue: batches `gc-singles`, `search-singles`, `cant-singles`, `shuffle-params`, `copy-params`, `base-pt`,
`seek-params` (28 rows) and the one "Fold:" defect line (copy-params, Unstable Shapeshifter). Ratchet 3,984 / 3,246 → 3,964 / 3,227
(20 records, 19 handlers; Relic Runner's record had none). Ledger: 28 rows (absorbed 17, split 3, `blocked-other` 8). 25 card files
rewritten (Valki's rewrite reverted), Oracle text checked with `get_card` for all 25 and 7 of the 8 left cards (Squidnapper has no
Scryfall entry, MB1 29); rulings for Tevesh Szat and Spirited Simulacrum. CR 111.7, 509.1h, 603.4, 607.2a, 608.2b, 608.2c, 608.2d, 608.2h, 611.2c,
613.3, 701.23a, 702.16b, 702.73a, 707.2, 708.2a, 903.3 checked with `get_rule`.

| Batch | Result |
|---|---|
| `gc-singles` (6) | Tevesh Szat split (control of the commanders, then `PutCardsOntoBattlefieldEffect` over every command zone's commanders), Fractured Loyalty (`gainedBy(controllerOf(triggeringSpell()))` over the triggering permanent, not `attached()`: "that creature" is the one that became the target). Left: Preacher, Squidnapper, Aura Graft, Peer Pressure (each a one-card part; ledger notes) |
| `search-singles` (5) | all folded: the Recruiters onto `SearchLibraryForCardsToTopEffect` (it already records the search and honours Aven Mindcensor), Scheming Symmetry and Varragoth onto `EachPlayerSearchesLibraryEffect.cardToTop`, Remembrance onto the same-name predicate, Vivien's then-step onto `SearchLibraryEffect` with an exact `SacrificedPermanentManaValue` bound, Knowledge Exploitation onto `SearchTargetLibraryEffect` |
| `cant-singles` (2) | Relic Runner folded (Jace's Sentinel's shape; the facet and its three readers deleted). Kraken left: no strict "power less than a count" predicate |
| `shuffle-params` (4) | all folded: Psychic Spiral and Elvish Soultiller split onto existing steps, Zoyowa's Justice onto the then-effect with `TargetManaValue` (last known, face down 0), Jace's −12 onto `handsOf` |
| `copy-params` (4) | Unstable Shapeshifter and The Mycosynth Gardens folded. Left: Deepfathom Echo, Valki |
| `base-pt` (4) | all folded onto `SetBasePowerToughnessEffect` / `SetBasePowerToughnessToAmountEffect` |
| `seek-params` (3) | Spirited Simulacrum, Worldweave, Wandering Treefolk folded onto `SeekLibraryEffect`. Left: Diviner of Fates |

**Where the triage was wrong.** Valki's composition (`ChooseCardEffect` over `exiledWithSource`) would let a Valki ability gained by
Patchwork Crawler copy the Crawler's own exiles (CR 607.2a): `PatchworkCrawlerTest` failed, the fold was reverted. Deepfathom's
`ChoosePermanentsEffect` would record the copied creature as the source's chosen permanent. Diviner's `triggeringCards()` loses a discarded
card exiled instead (Rest in Peace), where the type is read last known (CR 608.2h). Fractured Loyalty's `attached()` would follow a moved
Aura. The Recruiters' caution (no search record, no Mindcensor) was stale. Creepy Puppeteer's `except(triggeringAttackers(), source())` is no
set (`isSet()` false), so the survivor's handler gained the one-by-one path rather than the set path.

**Shared pieces widened** (Standing knowledge): `SetBasePowerToughnessEffectHandler` resolves a subject that names permanents one by one
(no card used that path before; the set path's log no longer says "other"); `BecomeCopyOfSubjectEffect` declares `target()` and copies
copiable values; `EachPlayerSearchesLibraryEffect` `TOP_OF_LIBRARY` (any one card, a must, guarded in the constructor);
`SearchTargetLibraryEffect` `CAST_WITHOUT_PAYING`; `SeekLibraryEffectHandler`'s hand-entry check and land enter processing.

**Behaviour changes.**
- Seek to the battlefield: the entry check is the hand's, as the survivor's own comment and the "seek ..., put one of them onto the
  battlefield" wording (Song of Seasons) say, so Grafdigger's Cage doesn't stop a sought creature (Kav Landseeker, Kardum, Spawning Pod and the
  other survivor cards too); a sought land's enter abilities run: `WorldweaveTest`/`SpiritedSimulacrumTest` Cage cases (fail on the old
  survivor by reading), new `SpiritedSimulacrumTest.soughtLandsEnterAbilityTriggers` (Blossoming Sands).
- `CardHasTypeChosenThisResolutionPredicate` doesn't count a changeling whose types Conspiracy replaced (CR 613.3, 702.73a):
  `ElvishSoultillerTest`'s Conspiracy case failed on the survivor; Bloodline Bidding, Bloodline Shaman, Haunting Voyage, Kindred Summons,
  Riptide Shapeshifter change the same way.
- Unstable Shapeshifter copies a face-down creature's subtypes and ward (CR 707.2, 708.2a): new `copiesACloakedCreaturesWard`; the survivor
  copies `copiableCard` (bestowed, Licid and text-borrowed permanents too) for Identity Thief and Mycosynth Gardens as well.
- Tevesh Szat's −10 moves only commanders out of the command zones (the text), through the entry batch: new
  `minusTenLeavesNoncommanderCardsInTheCommandZone`; `minusTenTakesAndReturnsCommanders` now registers its command-zone card as a commander
  (its setup made a non-commander card the "commander").
- Knowledge Exploitation shuffles an empty library too; Wandering Treefolk raises the sought event; Remembrance finds nothing for a face-down
  creature (no name, CR 708.2a); Mycosynth Gardens and the base-P/T cards log the survivors' lines; AI only: Storvald's two modes now carry
  `SetBasePowerToughnessEffect`'s polarity (7/7 beneficial, 1/1 harmful), Fractured Loyalty is no longer seen as the Aura controller's steal.

**Tests changed.** `UnstableShapeshifterTest` (a face-down copy's name is `""`, the engine's nameless convention, not `null`), `TeveshSzatDoomOfFoolsTest`
(setup), `TargetPolarityGuardTest` (Zoyowa's survivor record). New: the three cases above.

**Tests run.** 185 classes: every test class naming a rewritten card (`git grep -lwF 'X.class'`), the survivors' and chosen-type predicate's
cards, the AI combat and polarity tests, and the application combat, block, library-choice, interaction, layer, predicate, subject and
ratchet tests: 328 XML files (172 card and AI, 156 application, nested classes counted), 2,618 cases, 0 failures. The first
run's application JVM died of a SIGSEGV in the garbage collector (hs_err removed); the rerun, after the ratchet change, passed. No deleted handler asked death replacements first.

**Seen, not changed** (Defect queue groups 5 and 9): `ExiledWithSource` ignores ability links; `ChoosePermanentsEffect`'s kept chosen
permanent; `triggeringCards()` loses a moved discarded card; seek enter processing skips noncreature, nonland permanents; the shuffle
then-effect waits on the removal's success.

**For the next run.** Fold run 5. Before folding onto a card subject, check what it drops: a stay check or an ability link the old
handler didn't have can narrow a card, and a gained ability (Patchwork Crawler, Dark Impostor) is where it shows.

### Step 9, fold run 5 (2026-11-23)

Fold run 5 of the Fold queue: batches `player-floated`, `life-lock`, `may-draw`, `random-player`, `exile-cast`, `lukka`, `look-top-n`
(27 rows) and their "Fold:" defect lines (may-draw, lukka, exile-cast, player-floated). Ratchet 3,964 / 3,227 → 3,951 / 3,211
(13 records; 16 handlers: the 13 records' normal handlers, Minamo's may handler, and the handlers of the two records that stay as
floated payloads). Ledger: 27 rows (absorbed 9, split 6, survivor 3, `blocked-other` 7, re-batched 2). 19 card files rewritten,
Oracle text checked with `get_card` for all of them (Yawgmoth's Testament under CMB1 52); rulings for Lukka, Coppercoat Outcast.
CR 118.9, 400.7, 508.5, 603.5, 608.2d, 608.2h, 611.2c, 702.73a, 716.3a checked with `get_rule`.

| Batch | Result |
|---|---|
| `player-floated` (8) | all folded onto `GrantStaticEffectToPlayerEffect`, each reader asking for the player's floating copy: Lumbering Laundry (`LookAtFaceDownCreaturesEffect`), Chandra, Flame's Catalyst (Yusri's `AlternativeCostForSpellsEffect("{0}", hand only)`), Urza Assembles the Titans (`AllowExtraLoyaltyActivationEffect`, now the survivor of the loyalty pair), Angel of Grace (`DamageLifeFloorEffect(1, LIFE_AT_LEAST_FLOOR)`), Mirri (`MaximumCombatCreaturesEffect(MAX, 1)` over `eachOpponent()`, `UNTIL_END_OF_COMBAT`). Yawgmoth's Testament's and Fixed Point in Time's records stay as floated payloads (split), their handlers gone |
| `life-lock` (2) | Teferi's Protection, Perch Protection: `untilYourNextTurn(new LifeTotalCantChangeEffect())`; `canPlayerLifeChange` reads the floating copy |
| `may-draw` (3) | Sibilant Spirit, Harbor Guardian (`MayEffect(DrawCardEffect(1, defendingPlayer()), DEFENDING_PLAYER)`), Minamo (as Fields of Summer); Phelddagrif and Tataru Taru moved to `MayEffect(..., TARGET_PLAYER)`. Left: Soldevi Heretic (`blocked-other`) |
| `random-player` (4) | Vial Smasher (`ChoosePlayerAtRandomEffect` + `toPlayerOrTheirPlaneswalker(DynamicAmount, playersThisWay(CHOOSE))`), Wildfire Devils (random player, `ChooseCardEffect.by(playersThisWay(CHOOSE))`, exile, copy). Left: Strax's two records (`blocked-other`) |
| `exile-cast` (4) | Villainous Wealth, Hazoret's Undying Fury (with `ShuffleLibraryEffect`). Left: Epic Experiment (`blocked-other`) |
| `lukka` (2) | none: both `blocked-other` |
| `look-top-n` (4) | For the Ancestors (`ChooseCreatureTypeAtResolutionEffect` + `mayRevealUpToToHandRestOnBottomRandom`). Left: Break Out (`blocked-other`); Call to the Kindred and Conjurer's Mantle re-batched into `same-quality` (run 41) |

**Where the triage was wrong.** Yawgmoth's Testament's "exiled-counter batch's static exile permission" does not exist, and the
one-shot `AllowPlayCardsFromExileEffect` would lock the set (CR 611.2c: the rule applies to cards exiled later), so the record stays as
the floated rule. Lukka's `mayOnlyCast(creature)` would stop an adventurer card being cast as its Adventure (ruling 2020-04-17). The
look-top-n "same-quality predicate" is run 41's unbuilt piece. Break Out's survivor path puts the card into the hand before its
follow-up. Soldevi Heretic's second target can't be read by `MayEffect`'s `TARGET_PLAYER` on an activated ability. Strax's and Epic
Experiment's notes each need an option only that card would set.

**Shared pieces widened** (Standing knowledge): `GrantStaticEffectToPlayerEffect` accepts `UNTIL_END_OF_COMBAT`; the floating copies
read by `mayLookAtOpposingFaceDownCreatures`, `hasExtraLoyaltyActivation`, `canPlayerLifeChange`, `damageLifeFloor` (its turn-floor
branch gone), `CastingCostService.findFreeCastSource` (a floating unlimited alternative cost is used before a next-spell grant);
`MayEffect`'s `DEFENDING_PLAYER` reads the recorded defending player (CR 508.5) and a battle's protector;
`DealDamageToPlayersEffect.toPlayerOrTheirPlaneswalker(DynamicAmount, player)`; `ExileTopCardsAndMayCastSpellsEffect` casts an
Adventure-only-fits card as its Adventure.

**Behaviour changes.**
- Mirri, Weatherlight Duelist limits each opponent, not only the defending player (the Oracle text): new
  `MirriWeatherlightDuelistTest.attackTriggerLimitsEveryOpponent` (three players; fails on the old code by reading).
- `MayEffect` `DEFENDING_PLAYER`: Shakedown Heavy now asks the defending player after the attacked planeswalker left, or a battle's
  protector (CR 508.5): new `ShakedownHeavyTest.attackedPlaneswalkerLeavingStillAsksDefendingPlayer` (fails on the old code by reading:
  `findPermanentController` of a gone planeswalker found nobody). Sibilant Spirit's and Harbor Guardian's existing leave/battle cases
  pass on the survivor.
- Hazoret's Undying Fury offers battles and Adventures whose Adventure fits (defect line, CR 716.3a): new
  `HazoretsUndyingFuryTest.offersABattleSpell`. Every `ExileTopCardsAndMayCastSpellsEffect` card casts an exiled Adventure card whose
  card is over the limit but whose Adventure fits as its Adventure (before, the queue cast the card itself); Villainous Wealth's
  existing Adventure case covers it.
- `toPlayerOrTheirPlaneswalker` with no planeswalker deals the damage to that player, never the entry's own target (a cast trigger's
  "that player"); its prompt names the player instead of "? Choose the enchanted player" (Curse of the Pierced Heart too).
- Logs: the survivors' (silent) lines replace Lumbering Laundry's, Chandra's, Angel of Grace's, Teferi's Protection's grant lines and
  Phelddagrif's/Tataru's "offers ... a card"; Vial Smasher and Wildfire Devils log "chooses ... at random". Wildfire Devils' card choice
  is the card choice prompt (`MultiGraveyardChoice`, at least one).
- AI only: Minamo's draw is now a `CardDrawingEffect` the AI accepts; Chandra's free casts and Yusri's are used before a next-spell grant.

**Tests changed.** `TeferisProtectionTest` (asks for the floating `LifeTotalCantChangeEffect`), `Prevention.damageLifeFloor` fixture
(reads the floating `DamageLifeFloorEffect`; Runed Terror's cases), `WildfireDevilsTest` (the card choice prompt; display names added),
`EffectCountRatchetTest` (baselines). New: the Mirri, Shakedown Heavy and Hazoret cases.

**Tests run.** 182 classes: every test class naming a rewritten card or a survivor's or reader's card (`git grep -lwF 'X.class'`, 166),
plus the application game-query, ability-activation, planechase, interaction-contract, targeting-consistency, subject, casting-cost,
may, look-at-top, player-damage, block, graveyard-choice, may-ability, cost-classification, coin-flip and ratchet tests: 326 XML
files (nested classes counted), 2,498 cases, 0 failures. No deleted handler asked death replacements first.

**Seen, not changed.** Lukka's subtype check still needs a `CardSubtype` LUKKA (a conversion-version bump; its Defect queue line);
Soldevi Heretic still targets no opponent (its line). Step-10 hand-off: `GrantPlayerStaticEffectsUntilEndOfTurnEffect` (10 cards) is
`GrantStaticEffectToPlayerEffect.untilEndOfTurn` over a list.

**For the next run.** Fold run 6. A `step-9` note that names a predicate or option "from another batch" may name one not built yet
(run 41's same-quality, the exiled-counter subject): check it exists before folding, and re-batch rather than build it for one card.

### Step 9, fold run 6 (2026-11-23)

Fold run 6 of the Fold queue: batches `delayed-created`, `asenters-sac-all`, `random-pick`, `colour-grants`, `returned-animation`,
`chosen-animation`, `set-exchange`, `ignore-source`, `any-colour`, `prot-perm`, `coin-params` (31 rows) and the one "Fold:" defect line
(returned-animation). 17 rows taken, 14 left for the next run. Ratchet 3,951 / 3,211 → 3,940 / 3,201 (11 records, 10 handlers; Sheltered
Valley's record had none). Ledger: 17 rows (absorbed 6, split 5, survivor 3, `blocked-other` 2, Mondo Gecko's note corrected and left
`step-9`). 13 card files rewritten, Oracle text checked with `get_card` for all 13 and the 5 left cards read (Kav Landseeker, Brilliance
Unleashed, Cosmium Confluence, Turbocharged Escape, Mondo Gecko by its record); rulings for Kav Landseeker, Capricious Efreet, Haphazard
Bombardment, Tune Up, The War in Heaven, Brilliance Unleashed, Sheltered Valley (none). CR 608.2b, 608.2d, 608.2h, 701.21a checked
with `get_rule`.

| Batch | Result |
|---|---|
| `delayed-created` (3) | Old Hob onto `ScheduleCreatedPermanentsEffect(DESTROY_AT_END_STEP)` (Hornet Cannon's record, the same scheduling code), Broken Visage split (destroy, a Spirit with `TargetPower(true)` / `TargetToughness`, `SacrificeCreatedPermanentsAtEndStepEffect`). Left: Kav Landseeker (`blocked-other`: "the end step on your next turn" for every created token, its ruling; no sacrifice of a token set waits for a later turn) |
| `asenters-sac-all` (2) | Sheltered Valley onto `SacrificeAllPermanentsAsEntersEffect(PermanentHasSameNameAsSourcePredicate)`; the handler judges the filter with the entering permanent as the source, its land-loses-abilities guard moved onto the survivor's call |
| `random-pick` (3) | Capricious Efreet, Wild Swing, Metzali, Haphazard Bombardment: `ChoosePermanentsEffect.atRandom` + `DestroyTargetPermanentEffect(thisWay(CHOOSE))`; `atRandom` now accepts `allTargets()` |
| `returned-animation` (3) | Tune Up (`HappenedThisWay.toA(RETURN, Vehicle)`), The War in Heaven: `AnimatePermanentsEffect` over `returnedThisResolution()`. Left: Brilliance Unleashed (`blocked-other`: "if it's an artifact creature card" judges the card; every permanent reader judges the permanent) |
| `set-exchange`, `ignore-source` (2 + 2) | Reins of Power onto Twist Allegiance's record; Damping Engine onto `IgnoreSourceAuraEffectsUntilEndOfTurnEffect` (its two readers ask for that class) |
| `coin-params` (3) | Scoria Wurm, Wild Wurm onto `FlipCoinWinEffect(null, ReturnToHandEffect.self())`. Breeches and Mirrored Depths not taken |
| not taken (14) | `colour-grants` (needs C, not P: a chosen-this-resolution colour on the colour and hexproof grants, with 8.29's targeted twin), `chosen-animation`, `any-colour`, `prot-perm`; the queue rows say what each needs checked |

**Where the triage was wrong.** `thisWay(RETURN)` is not a subject (it points to `returnedThisResolution()`), and the targeted
`ReturnCardFromGraveyardEffect` didn't fill that set: it now does, beside its RETURN step. Old Hob's existing head
(`ScheduleCreatedPermanentsEffect`) fits exactly; the `aboutCreated` form names one triggering permanent only for a lone token, which is
also why Kav stays. Capricious Efreet's "the pick must leave out illegal targets" was already true: `StackEntry.getTargetIds` leaves out the
targets the stack marked illegal (CR 608.2b), for the old handler and the survivor alike. Brilliance Unleashed's card-type check and Mondo
Gecko's colour step are not parameters.

**Shared pieces widened** (Standing knowledge): `ChoosePermanentsEffect.atRandom` over `allTargets()`; `SacrificeAllPermanentsAsEntersEffect`
with a source-relative filter; `ReturnCardFromGraveyardEffect` remembers its returned permanents; `IgnoreSourceAuraEffectsUntilEndOfTurnEffect`
for any "ignore this effect".

**Behaviour changes.**
- Sheltered Valley: a Valley entering together with another (Splendid Reclamation) no longer sacrifices it (neither is on the battlefield as
  the replacement applies): the survivor runs after the batch-mates are hidden. New `ShelteredValleyTest.copiesEnteringTogetherSurvive`
  (not run on the old code; by reading, the old call ran before the batch-mates were hidden). Its sacrifice now skips a permanent that can't
  be sacrificed, is one leave batch, and logs the survivor's "X sacrifices Y" line.
- Random picks: the survivor's "Chosen at random: ..." log line, a "can't be chosen" permanent left out (CR 608.2d), and the source keeps the
  pick as its chosen permanent (no reader on these cards). AI only: the four cards now carry `DestroyTargetPermanentEffect`.
- Broken Visage keeps a negative last-known power on the Spirit (the old handler did too; `TargetPower(true)`); the survivors' logs.
- Back for More, It Doesn't Add Up, Jailbreak, Pre-War Formalwear, Too Greedily, Too Deep and the other `ReturnCardFromGraveyardEffect` cards
  with a returned-permanent reader now see the permanent the return made (their tests pass unchanged).

**Tests changed.** `GameDataPermanentEffectTest` (its other-class check names `DampingEngineEffect`), `DampingEngineTest` (the survivor's class),
`ReturnSelfToHandOnCoinFlipLossEffectHandlerTest` deleted with its handler (Scoria Wurm's and Wild Wurm's tests cover the card),
`EffectCountRatchetTest` (baselines). New: the Sheltered Valley case.

**Tests run.** 116 classes in two runs: every test class naming a rewritten or survivor card (`git grep -lwF 'X.class'`, 34 + 49 for the
returned-permanent readers), the 28 `Mastery|DeathReplacement` classes (Broken Visage's handler asked death replacements first), and
`TargetPolarityGuardTest`, `EffectCountRatchetTest`, `GameDataPermanentEffectTest`, `BattlefieldPlacementServiceTest`,
`CastingPermissionServiceTest`, `SubjectResolutionServiceTest`, `LayerClassifierTest`: 118 XML files, 949 cases, 0 failures after the
`ReturnCardFromGraveyardEffect` fix (the first run's `TuneUpTest` case failed on the empty returned set).

**Seen, not changed.** The returned-animation defect line stays open (no ruling settles "return it ... and it's a [type]" as an as-enters
change; the fold keeps the timing).

**For the next run.** Finish fold run 6's 14 rows before fold run 7: Cosmium Confluence is ready (its test answers a multi-permanent prompt
today, the survivor asks a single choice); for `any-colour` and `prot-perm` read every reader of the absorbed records' interface methods
first (the queue rows list them).

### Step 9, fold run 6b (2026-11-23)

The 16 rows fold run 6 left (`chosen-animation`, `coin-params`' two, `any-colour`, `prot-perm`, `colour-grants`); no "Fold:" defect line named
them. Ratchet 3,940 / 3,201 → 3,933 / 3,196 (7 records; 5 normal / static handlers, plus 2 manafx handlers the ratchet doesn't count).
Ledger: 16 rows (absorbed 7, survivor 3, `blocked-other` 5, unique 1); Mondo Gecko's row stays `step-9`. 7 card files rewritten, Oracle text
checked with `get_card` for all 7 and Turbocharged Escape; Louvaq from the cache text (no Scryfall entry); rulings for Breeches. CR 106.5,
106.7, 603.12, 608.2d, 616.1, 700.9, 702.16b, 705.2 checked with `get_rule`.

| Batch | Result |
|---|---|
| `chosen-animation` (2) | Cosmium Confluence: `ChoosePermanentsEffect.exactly(1, Caves you control)` + counters and animation over `thisWay(CHOOSE)`. Left: Turbocharged Escape (`blocked-other`: no card subject for a chosen permanent's card) |
| `coin-params` (2) | Mirrored Depths (Planar Chaos' `forTriggeringSpellController(null, CounterSpellEffect)`), Breeches (`QueueReflexiveAbilityEffect` branches, CR 603.12) |
| `any-colour` (5) | Selvala, Three Tree City, Brood Astronomer onto `AwardAnyColorManaEffect(DynamicAmount)`. Left: Corrupted Grafstone (a one-card restriction value), Wrath of Sod (the survivor's `SOURCE_PERMANENT_COLORS` has no current-colour potential-mana model and gives a colourless source mana under a land replacement, CR 106.5) |
| `prot-perm` (5) | Geyadrone Dihada (`PermanentHasCountersPredicate`; `ProtectionGrantingEffect.protectionFromPermanentsWithCounters` and its readers gone). Left: Louvaq (`unique`: its playtest "modified" test), Ring-bearers (no predicate; wait for `ring`), non-subtype creatures (the survivor has no spell path) |
| `colour-grants` (1) | not taken: a colour CHOOSE step read by the colour, hexproof and can't-be-blocked grants is an engine step of its own (queue row) |

**Where the triage was wrong.** Mirrored Depths' "benign spell target" is the survivor's too (`CounterSpellEffect`'s spec), and its
`CounterSpellingEffect` marker has no reader for a plane's trigger (`InstantCategoryClassifier` reads spell effects only). Three Tree City's
and Selvala's records had no estimate facets, so the survivor changes no AI estimate for them; Brood Astronomer's did, hence the widening.

**Shared pieces widened** (Standing knowledge): `AwardAnyColorManaEffect`'s `estimatedCountsAllColors` / `estimatedWildcardMana` model a
`FixedIfCondition` amount by its smaller number; `ProtectionFromPermanentsMatchingEffect` takes the counter quality; agent-docs rows for both.

**Behaviour changes.**
- Breeches: a lost flip raises the coin-flip-lost event (CR 705.2; Karplusan Minotaur): new `BreechesTheBlastmakerTest.eitherFlipResultTriggersCoinFlipAbilities`
  (fails on the old code whenever the flip is lost, by reading). A Pokey d20 heads now takes the win branch, as on every `FlipCoinWinEffect`
  card (no test: the d20 is random); the reflexive entries read "'s reflexive ability".
- Cosmium Confluence: the +1/+1 counters order counter replacements (CR 616.1, the survivor's); the Cave choice is the single-permanent
  prompt naming "a Cave you control" (test asserts the offered Caves); log "chooses ...".
- Three Tree City and Selvala: no colour prompt when the amount is 0; their mana is tagged by its source (cave, snow, artifact) as every
  any-colour source's is; Three Tree City under a land-mana replacement produces its count, not 0; a land "could produce" reader now sees its five
  colours (see Seen, not changed).
- Geyadrone Dihada: the protection follows the survivor's guards (none while face down or after losing its abilities); no test (no effect
  at hand makes a planeswalker lose abilities).
- AI and potential mana only: Brood Astronomer is estimated at 1 mana of any colour (was 3, the condition's larger number); Ilysian Caryatid
  is now estimated at 1 (was not modelled).

**Tests changed.** `CosmiumConfluenceTest` (the Cave prompt is a single choice: `handlePermanentChosen`, offered Caves asserted, display name
added); `ThreeTreeCityTest.producesNoManaWithoutMatchingCreatures` (no colour is asked for 0 mana; the no-mana assertion kept);
`EffectCountRatchetTest` (baselines). New: the Breeches case.

**Tests run.** 47 classes: every test class naming a rewritten card, Planar Chaos, Ilysian Caryatid, Katilda, Ronom Hulk, Tsabo Tavoc,
Concerted Effort, Karplusan Minotaur, Louvaq, Lord of the Nazgûl (`git grep -lwF 'X.class'`, 36), and the application and AI tests naming
`AwardAnyColorManaEffect`, `FlipCoinWinEffect` or the protection readers, `GameQueryServiceTest`, `LayerClassifierTest`,
`GrantedAbilityViewFactoryTest`, `EffectCountRatchetTest`: 155 XML files, 1,026 cases, 1 failure (the Three Tree City case above), then
`ThreeTreeCityTest` and `BreechesTheBlastmakerTest` (five runs, both flip results) passed. No deleted handler asked death replacements first.

**Seen, not changed** (Defect queue group 9): `LandManaTypeSupport` / `ManaSourceColorSupport` give an any-colour ability every colour even
at amount 0 (CR 106.7); Louvaq's "modified" is not CR 700.9's (unsure: the playtest reminder text).

**For the next run.** Fold run 7. `colour-grants` wants its own run (a colour CHOOSE step with three readers); Wrath of Sod waits for a
`SOURCE_PERMANENT_COLORS` potential-mana model.

### Step 9, fold run 7 (2026-11-23)

The run-7 rows (`scheme-abandon`, `drafts`, `choose-random`, `villainous`, `parley`, `repeat`, `step-reads`, `aura-host`, `must-attack`, `roll`,
`spell-limits`, `delayed-leads`) and the one "Fold:" defect line (Dark Wings). Ratchet 3,933 / 3,196 → 3,915 / 3,180 (18 records, 16 handlers;
the Aura follow-up and Censor's record had none). Ledger: 27 rows (absorbed 15, split 3, survivor 2, unique 3, `blocked-other` 1, 3 re-noted
`step-9`). 22 card files rewritten, Oracle text checked with `get_card` for all 22; rulings for Ensnared by the Mara and Great Intelligence's
Plan. CR 101.4, 303.4f, 303.4g, 603.2d, 603.3c, 608.2d, 614.12, 700.2b, 701.33b, 701.55a, 701.55c, 702.37c, 708.2 checked with `get_rule`.

| Batch | Result |
|---|---|
| `scheme-abandon`, `repeat`, `roll` | Dark Wings onto `SacrificeSelfEffect` (its scheme branch); Full Flowering onto `RepeatEffect(X, PopulateEffect)`; Clown Car onto `RollD6Effect` with six branches. Left: Berserker's Frenzy `unique` (a keep-higher count only it sets) |
| `drafts` (3) | Raddic, Recruit Instructor, Slimefoot onto `DraftCardFromSpellbookEffect(printings)`; Protean War Engine onto a static `DraftCardFromSpellbookToExileEffect(printings, false)`: `EtbTriggerService` asks it as any permanent enters, `SpellbookDraftToExileChoice.AsEnters` says which entry the answer continues. The Protean and Slimefoot choice records, handlers, projections and AI strategies are gone |
| `choose-random` (2) | Cult of Skaro, Typhoid Mary onto `ChooseOneAtRandomEffect`; `StackedModalTriggerService`'s random branch gone |
| `villainous` (2) | Ensnared split into `villainousChoiceFacedBy(eachOpponent(), …)` over exile-until + free cast and exile-four + `AmountThisWay.exiledManaValue()` damage; Great Intelligence's Plan onto `forTargetedPlayerVillainousChoice([DiscardEffect, MayCastAnySpellFromHand…])` |
| `parley` (2) | Storm Fleet Negotiator, Phabine split: `RevealTopCardOfLibraryEffect(eachPlayer())` + the verb over `AmountThisWay.revealedCards(filter)` + `DrawCardEffect(1, eachPlayer())` (not `ParleyEffect`'s token form: Defect queue 3) |
| `step-reads` (3) | Reckless Handling `toACard(DISCARD, artifact)`; Cemetery Recruitment, Jared `toACard(RETURN, filter)`; Hurl into History, Overwhelming Intellect `NoteAmountEffect(TargetSpellManaValue)` + `noted()` |
| `aura-host` (2) | Knickknack Ouphe's put has no follow-up, so `LibraryChoiceHandlerService` enters its Auras through the entry batch; both records, the handler and `AttachSelectedAuraToLegalTarget` gone |
| `must-attack` (5) | Tolsimir, Fighter Class onto `MustBlockSourceEffect(null, null, true, triggering())`. Arcum's Whistle `blocked-other` (cost-shape: step 10's merge makes it `MayPayManaEffect.dynamic` paid by the active player, else the two penalty steps). Not taken: Taunt / Gideon and My Crushing Masterstroke (an attacked-object component, and a next-turn duration only Taunt's record sets), Slayer's Cleaver / Ace's Bat (merge two `CombatBlockService` paths, teach three AI readers) |
| `spell-limits` (3) | Phyrexian Censor onto `LimitSpellsPerTurnEffect(1, EACH_PLAYER, not Phyrexian)`. Left `unique`: Irencrag Feat (a number fixed at resolution), Ethersworn Canonist (artifact as cast) |
| `delayed-leads` | not taken (16 cards, three timings): its own run |

**Where the triage was wrong.** `HappenedThisWay.toACard` refused RETURN: it now judges a card a return step put into a hand. Parley folds as
steps, not onto `ParleyEffect`. Arcum's Whistle has no `MayPayPayer` for the active player. Irencrag's limit is not a parameter.

**Shared pieces widened** (Standing knowledge): `DraftCardFromSpellbookToExileEffect` as any permanent's as-enters draft (`AsEnters`; owner set on
the offered cards; no prompt when nothing is offered); `ChooseOneAtRandomEffect` chooses per instance (`TriggeredAbilityStackService.write`);
`HappenedThisWay.toACard(RETURN, …)`; agent-docs rows for each record gone.

**Behaviour changes.**
- Dark Wings: the abandoned scheme goes face down to the bottom of its scheme deck and is logged (CR 701.33b): new `abandonedSchemeGoesToBottomOfSchemeDeck` (by reading, fails on the old code).
- Great Intelligence's Plan: the opponent faces the choice when neither option can do anything (ruling): new test, failed on the old code. One free-cast offer (the survivor's).
- Cult of Skaro under Isshin: each instance chooses its mode (CR 603.2d, 603.3c): new `additionalTriggerChoosesItsOwnRandomMode` (passes on the old Cult, fails on the old survivor by reading). Umaro, Outlaws' Merriment, Magus Sisters gain the same.
- Hurl into History, Overwhelming Intellect: a face-down spell's mana value is 0 (CR 702.37c, 708.2): new `faceDownCreatureSpellDrawsNothing` (by reading, the old handler read the card's 6).
- Knickknack Ouphe: an Aura with nothing to enchant stays in the library and goes to the bottom (CR 303.4g), where it entered and died: new `auraWithNoLegalHostStaysInLibrary`.
- Storm Fleet Negotiator, Phabine: draws in APNAP order (CR 101.4); after a token-creation prompt the boost and draws still happen (no test: no such prompt at hand).
- Protean War Engine: real printings (Steel-Plume Marshal's included); prompt "Choose a card from Protean War Engine's spellbook."; the offered cards are owned by the drafter (The Mystical Archive too). A land's as-enters draft entering during a parked resolution now continues the land's entry instead of resuming the parked entry (no test).
- Raddic, Recruit, Slimefoot: the survivor's draft prompt and its "drafts a card from X's spellbook" line; an unimplemented printing is left out.
- Clown Car: one roll's robots are created together.

**Tests changed.** Named a deleted record or prompt type: `CultOfSkaroTest` (`ChooseOneAtRandomEffect`), `EnsnaredByTheMaraTest` and
`GreatIntelligencesPlanTest` (the option labels moved to the cards), `ProteanWarEngineTest` (`SpellbookDraftToExileChoice`, Steel-Plume Marshal in
`@CardUsed`), `SlimefootThallidTransplantTest`, `RaddicTalZealotTest`, `RecruitInstructorTest` (`SpellbookDraftChoice`), `ClownCarTest`
(`RollD6EffectHandler`'s dice service), `EffectCountRatchetTest` (baselines). New: the five tests above.

**Tests run.** 160 classes: every test class naming a rewritten or survivor card (`git grep -lwF 'X.class'`: 27 for the rewritten cards, 145 with the
survivors' cards), the scheme cards, Isshin, `CastingPermissionServiceTest`, `ParleyEffectHandlerTest`, `TriggeredAbilityStackServiceTest`,
`CombatDamageCollectorAbilityStackObjectTest`, `GameDataDeepCopyTest`, `PlanarAndInvokeCalamityAiStrategyTest`, `EffectCountRatchetTest`: 168 XML
files, 1,622 cases, 0 failures (the first run's 23 were `toACard(RETURN)` refused and a must-block guard I had added, then removed: Turntimber
Basilisk asserts the requirement after its source left), then `OverwhelmingIntellectTest`, `HurlIntoHistoryTest`: 14 cases, 0 failures. No deleted
handler asked death replacements first.

**Seen, not changed** (Defect queue 3): additional triggers copy the first instance's targets and announced modes; `ParleyEffect`'s token form
creates tokens inside its handler.

**For the next run.** Fold run 8 (`splits`). The three `must-attack` rows want an attacked-object component with the `*MustAttack*UntilNextTurn`
siblings; `delayed-leads` and `colour-grants` want runs of their own.

### Step 9, fold run 8 (2026-11-24, parallel worktree)

Batch `splits` (39 rows): 14 taken (9 folded: 2 absorbed, 7 split; 5 `blocked-other`), 25 not taken. Ratchet 3,933 / 3,196 →
3,924 / 3,187 in its worktree (main after merging run 7 and run 8: 3,906 / 3,171). CR 120.3c, 120.8, 303.4f, 603.12, 608.2b,
608.2d, 608.2h, 609.3 checked with `get_rule`.

| Folded | Into |
|---|---|
| Werewolf Ransacker | `DestroyTargetPermanentThenEffect.ifDiesThisWay` (Cinder Cloud's form) |
| Ydwen Efreet | `RemoveTargetFromCombatEffect(source(), true)` + `CantBlockThisTurnEffect(source())` (subject widened) |
| Garruk Relentless | damage step + `TargetDealsPowerDamageToTargetEffect(target(), source())` |
| Binding Negotiation | `ChooseCardsFromTargetHandEffect` with `declineEffect` |
| Bounty of the Luxa | remove-all flood counters + `NotCondition(removedCounters())` / `removedCounters()` branches |
| Cemetery Desecrator | `ChooseCardEffect` + `ExileGraveyardCardsEffect.cards` + reflexive modal reading `AmountThisWay.exiledManaValue()` |
| Void | `ChooseNumberAtResolutionEffect` (X) + `DestroyAllPermanentsEffect` + reveal + `DiscardCardsEffect(revealedThisWay(...X))` |
| Madcap Experiment | reveal-until + put onto battlefield + bottom random + `AmountThisWay(REVEAL)` damage |
| Mind Spike | reveal matching + choose + discard + lose 2 + `NotCondition(HappenedThisWay(REVEAL))` draw |

**Where the triage was wrong.** Werewolf: the single-target `DestroyTargetPermanentEffect` records no departures (so `died()`
reads 0), and `DestroyEachTargetPermanentEffect` records no last-known controller; the existing then-wrapper fits exactly.
Archfiend: `SacrificeSelfEffect` sacrifices one permanent, not a set. Binding Negotiation already had a decline effect on the
hand-choice head. Primordial Ooze: the fallback interpreter takes only a fixed amount.

**Behaviour changes.** Garruk's return damage is now damage (CR 120.3c); new Furnace of Rath test. Void's discard now goes
through the shared discard rules; new Tamiyo test. The number prompt is the standard number choice. Madcap's artifact uses the
entry batch (CR 303.4f), with no failing-first test: a Painter's Servant attempt saw no colour prompt (defect queue). Bounty's
counters fire the counter events. Cemetery Desecrator and Mind Spike choose a lone option unasked (CR 608.2d).

**Blocked.** Primordial Ooze (cost-shape, step 10: the pay-or-else fallback takes only a fixed damage amount); Choose Your
Champion ×2 (pieces only it would set); Archfiend of Depravity (no verb sacrifices a set named by a subject); Tephraderm (the
damage trigger can't tell a creature source from a spell source).

**For the next run.** Desolation, Creeping Dread and Kroxa share one missing piece: a `playersThisWay(kind, card/permanent
filter)` subject plus checking `DiscardEffect(eachPlayer())` APNAP; build it in its own run. Ripples of Potential needs proliferate
to record PUT_COUNTERS. 25 `splits` rows left; the queue lists them.

### Step 9, fold run 9 (2026-11-24, parallel worktree)

Batches `mill-split`, `sac-gated`, `sac-or-else`, `one-of-two`, `look-reveal-then`, `hand-choice-draw`, `draw-discard` (29 rows); no
"Fold:" defect line named them. Taken: the first three and `look-reveal-then` (19 rows); `one-of-two`, `hand-choice-draw` and
`draw-discard` (10 rows) not taken (time; fold run 9b). Ratchet in its worktree 3,933 / 3,196 → 3,918 / 3,178 (15 records; 15
normalfx + 3 mayfx handlers). Ledger: 31 rows (split 15, blocked-other 3, re-batched 1, step-9 kept 10, survivor 2). 17 card files
rewritten, Oracle text checked for all 17 and the 3 left; CR 400.7, 603.12, 608.2c, 608.2d, 608.2h, 701.17a–c, 701.23a–c checked.

| Batch | Result |
|---|---|
| `mill-split` (4) | Fateful Tempest (the vote handler inserts `MillEffect` + `DealDamageToPlayersEffect(milledManaValue(), eachOpponent())`), Heed the Mists, Palantír (decline branch `SequenceEffect(MillEffect, LoseLifeEffect(milledManaValue(), targetPlayer()))`). Left: Heretic's Punishment (`blocked-other`: a max over the milled cards, one user) |
| `sac-gated` (8) | Heart-Piercer, Surtland Flinger, Tiger-Tribe Hunter, Ziatora (Minsc & Boo's form; Giant doubling = `Sum(sacrificedPower(), sacrificedPower().matching(Giant))`), Venom (`PutCardToBattlefieldEffect.boundedBy(sacrificedManaValue())`), Garruk, Scapeshift (Sylvan Primordial's form), Feed the Pack (`withAmount(sacrificedToughness())`), Kethek (`CardManaValueAtMostAmountPredicate(Sum(sacrificedManaValue(), -1))`). Left: Kuldotha (`blocked-other`: the stacked-division announcer finds the division by class), Descendants' Fury (re-batched into `same-quality`) |
| `sac-or-else` (4) | Xathrid Demon, Doom Foretold, Cruel Reality: the sacrifice step + `ConditionalEffect(NotCondition(sacrificed()), …)` ("if you can't" = nothing was sacrificed). Left: Lich (`blocked-other`: `atLeast` needs a `DynamicAmount`, one user) |
| `look-reveal-then` (3) | Quest for Ula's Temple (Plane-Merge Elf's form inside its may), Runo, Delver: look + `ConditionalEffect(CardsInLibraryAtLeast(1), MayEffect(RevealTopCardOfLibraryEffect))` + `HappenedThisWay.toACard(REVEAL, f)` + `TransformSelfEffect` |

**Behaviour changes.** Fateful Tempest and Palantír no longer count a milled card a replacement put into a hidden zone (Wheel of Sun
and Moon; CR 701.17c); new `FatefulTempestTest` case. The sacrifice-gated cards follow the survivor: no "may" prompt when nothing
can be sacrificed (CR 608.2d), a lone candidate sacrificed unasked, several by the multi-permanent prompt; the "can't be sacrificed"
and token-protection guards now apply to Heart-Piercer's family; sacrifices go through the departure step. Cruel Reality respects
"can't cause sacrifice". Scapeshift with zero lands sacrificed is still a search for up to zero (recorded; no ruling says
otherwise). Runo respects transform prevention and transforms a transformed copy back. Logs: the survivors' lines.

**For the next run.** Fold run 9b: `one-of-two` (allow `ChoosePermanentsEffect` over `allTargets()` without random, widen
`SacrificeSelfEffect` to `thisWay(CHOOSE)`, keep the AI's `RemovalEffect` view), `hand-choice-draw` (check that the DISCARD
destination records a DISCARD step), `draw-discard` (DISCARD mana-value stat, per-player `DiscardEffect` amounts). The re-triage
note's 25 other stale 4f Mill rows were not checked.

### Step 9, fold run 10 (2026-11-24, parallel worktree)

Rows: `if-instead`, `hand-reveal-dmg`, `manifest-hand`, `manifest-dread`, `gy-batch`, `opp-perm` (14 rows) plus Fertile Imagination;
`opp-card` (8) and `returned-reflexive` (2) not taken. Ratchet in its worktree 3,915 / 3,180 → 3,907 / 3,172. Ledger 16 rows (split
6, absorbed 3, survivor 1, blocked-other 6). 8 card files rewritten, Oracle checked for all. CR 603.10a, 603.12, 608.2d, 701.40a/b,
508.5 checked.

| Batch | Result |
|---|---|
| `if-instead` | Jetmir's Fixer onto `ConditionalReplacementEffect` (one branch, no unmet-branch log). Paragon `blocked-other` (no colours-spent-to-activate condition) |
| `hand-reveal-dmg` | Darigaaz (Dromar's shape), Inquisition, Blood Oath split onto `RevealCardsFromHandEffect` + damage over `AmountThisWay.revealedCards`; new step `ChooseCardTypeAtResolutionEffect` + `CardHasCardTypeChosenThisResolutionPredicate`, also folding Fertile Imagination |
| `manifest-hand` | Scroll of Fate: `ChooseCardEffect.oneOf(hand)` + `PutCardsOntoBattlefieldEffect.manifested()`; Mark… absorbed |
| `manifest-dread` | Valgavoth's Onslaught `blocked-other`: the repetitions can't share one MANIFEST step |
| `gy-batch` | Liliana −7: `ChooseCardEffect.upTo(2, every graveyard's creatures)` + put under you; `closedByEffect` gone |
| `opp-perm` | Bane split; Crashing Boars, Wormfang Crab `blocked-other` (no blocker subject; no source-linked exile over a subject); Demonic Hordes, Infernal Denizen `blocked-other` cost-shape (their `ForcedCostOrElseEffect` fallback stops at the first prompt; step 10) |

**Where the triage was wrong.** Crashing Boars' `MustBlockTriggeringAttackerEffect` doesn't exist. Valgavoth's "NONE once, addToStep
each" has no place to record the NONE once. Demonic Hordes and Infernal Denizen can't compose inside a `ForcedCostOrElseEffect`
fallback.

**Behaviour changes.** Bane of Bala Ged exiles its two as one event (CR 603.10a), with a new test (not run on the old code). Scroll
of Fate takes a lone card unasked (CR 608.2d) and its prompt is private. Liliana's −7 has one up-to-two prompt (its text doesn't
target). Colours and card types are judged as effects leave them. Blood Oath no longer leaves `chosenSpellPermanentType` set.
Darigaaz declares no target ("that player").

**For the next run.** `opp-card` wants its own run; Murmurs folds as `RevealTopCardOfLibraryEffect.topCards(you(), 3)` +
`ChoosePlayerEffect.opponent()` + `ChooseCardEffect.oneOf(cardsThisWay(REVEAL)).by(playersThisWay(CHOOSE))` +
`PutExiledCardsIntoOwnersGraveyardEffect(cardsThisWay(CHOOSE))` + the rest to hand. Burning-Rune Demon's unrecorded search (its
Fold: line) is still open. `returned-reflexive` needs a reflexive ability about the permanent a return step put onto the battlefield
(making the return record picks is unsafe: 7 handlers read picks); Jailbreak also needs its Hushbringer-style check and a mana-value
bound.

### Step 9, fold run 11 (2026-11-24, parallel worktree)

Batch `vote` (19 rows; Fateful Tempest left for main by the orchestrator, after fold run 9's mill split) plus the unaudited
`WillOfThePlaneswalkersEffect` (5 cards; the shared record says the same for each). Ratchet in its worktree 3,906 / 3,171 → 3,894 /
3,159. Ledger 21 rows (split 13, survivor 1, blocked-other 3, `step-9` 4). 17 card files rewritten, Oracle checked for all; rulings
for Plea for Power, Tivit, Selvala's Stampede, Trap the Trespassers, Truth or Consequences. CR 101.4, 120.8, 401.4, 608.2c, 608.2d,
701.38a–d, 800.4a checked.

**The step's shape.** `VoteEffect(options, firstVoter, secret)`, built with `startingWithYou(...)` or `secretly(...)` (`secretly`
starts with the active player, CR 101.4, as Truth or Consequences did). Each player in turn order answers a list prompt
(`ChoiceContext.VoteChoice`, a `VoteChoiceContext`), so `VotingSupport`'s additional-vote offer works unchanged (CR 701.38d). Players
who have left the game don't vote. After the last vote the step records `StepKind.VOTE` with `StepOutcome.votes()` (each
`Vote(voterId, choice)` in order; amount = votes; players = voters). Readers: `VoteResult` (`moreVotes`, `moreVotesOrTied` — "tied"
means more than one option has the most votes, per the Plea for Power ruling, 3-1-1 is not tied — and `anyVotes`) and the
`VotesFor(option)` amount, with `RepeatEffect` when each vote is its own event. P10's payload-per-outcome form became plain
`ConditionalEffect` steps.

| Batch row | Result |
|---|---|
| Will of the council (Plea for Power, Tyrant's Choice, Magister of Worth, Coercive Portal, Galadriel, Split Decision, Sail into the West, Trial of a Time Lord, the five Paths) | `VoteEffect` + `moreVotes` / `moreVotesOrTied` conditionals; Split Decision's two conditionals share its single spell target |
| Council's dilemma (Tivit, Travel Through Caradhras, Selvala's Stampede) | `RepeatEffect(VotesFor(x), …)`, or `anyVotes`-gated steps where the old code skipped a zero count |
| Secret council (Truth or Consequences) | `VoteEffect.secretly` + `anyVotes`-gated draw and random-opponent damage `Scaled(VotesFor, 3)` |
| Left `step-9` | Vault 11, Trap the Trespassers, Círdan (votes on permanents or players: fold run 11b); Fateful Tempest (take on main) |
| Left `blocked-other` | Elrond, Expropriate (one choice per vote by or about that vote's voter); Erestor (reads the triggering vote) |

**Where the triage was wrong.** P14's "secret votes need a tally per candidate" holds only for votes on objects and players.
Expropriate's and Elrond's notes assumed `oneForEach` / `playersThisWay(VOTE)` could do the per-vote choice; `oneForEach` is one
controller choice per player among what that player controls.

**Deleted.** 12 vote `ChoiceContext` records, `TruthOrConsequencesState`, `GameData.additionalVoteAccepted`,
`VotingSupport.offerAdditionalVoteToRerunningEffect`.

**Behaviour changes.** Open votes are logged as cast (Plea for Power ruling: later voters know earlier votes); new test. Secret
votes are revealed in one line after the last vote; Truth's prompt is a list. The losing branch logs "'s vote ability does nothing
(…)". The left-the-game voter guard (Tivit's) applies to every vote. Trial of a Time Lord's guilty put goes through
`PutCardsIntoLibraryEffect` (CR 401.4). AI and simulation answer a `VoteChoice` as before.

**For the next run.** Fold run 11b (`object-vote`): candidates on `VoteEffect` for permanents and players (one candidate voted
unasked, CR 608.2d) plus readers: a "most votes or tied for most" permanent subject (Vault 11, WillOfTheCouncil ×3), per-permanent
and per-player vote counts (Trap the Trespassers, Círdan, Mob Verdict), a "received no votes" player subject; Vault 11's secret votes
revealed in its finish step (once, not again on the death-replacement replay). Then a per-vote choose step for Elrond and
Expropriate. Fateful Tempest onto `VoteEffect` on main.

### Step 9, fold run 9b (2026-11-24, parallel worktree)

Batches `one-of-two`, `hand-choice-draw`, `draw-discard` (10 rows): 10 folded (8 split, 2 absorbed), and `InlineDiscardFollowUpEffect`
deleted. `splits` members Desolation, Creeping Dread, Kroxa not taken (time). Ratchet in its worktree 3,891 / 3,153 → 3,881 / 3,143.
Ledger 18 rows. CR 101.4, 608.2b–e, 701.21a, 701.27g checked. All 11 card files had their Oracle text checked.

| Batch | Result |
|---|---|
| `one-of-two` (3) | Incriminate: `ChoosePermanentsEffect.exactly(1, allTargets(), label).chosenBy(controllerOf(allTargets()))` + `SacrificeSelfEffect(thisWay(CHOOSE), controllerOf(thisWay(CHOOSE)))`; Retribution adds `PutCounterOnTargetPermanentEffect(except(allTargets(), thisWay(CHOOSE)), -1/-1)`, Barrin's Spite `ReturnToHandEffect(except(...))`. The resolution check for `CONTROLLED_BY_FIRST_TARGET` already makes the targets illegal once they no longer share a controller. The AI keeps `HARMFUL_REMOVAL` through the classifier's `SacrificeSelfEffect` branch |
| `hand-choice-draw` (2) | The `DISCARD` destination records no `DISCARD` step and skips the choice under Tamiyo, so it was not used: `KEEP_IN_HAND` (records `CHOOSE`) + `ConditionalEffect(HappenedThisWay(CHOOSE), …)`. Oildeep: `DiscardCardsEffect(cardsThisWay(CHOOSE))` + draw. Vendilion Clique: `RevealCardsFromHandEffect.cards(...)` + `PutCardsIntoLibraryEffect.onBottom` + draw, each for `targetPlayerDeclaredElsewhere()` |
| `draw-discard` (5) | Spellbound Dragon: draw + discard + `BoostSelfEffect(LastDiscardedCardManaValue, 0)`. Civilized Scholar: draw + discard + `ConditionalEffect(toACard(DISCARD, creature), untap + TransformSelfEffect)`. Occult Epiphany: draw X + discard X + `CreateTokensForDistinctDiscardedCardTypesEffect` reading the `DISCARD` step. Kozilek, Compleated and Your Nightmares Are Delicious: `DiscardEffect.eachEvaluatedFor(Max(0, CardsInHand(CONTROLLER) − N), eachOpponent())`; Nightmares adds `ConditionalEffect(NotCondition(HappenedThisWay(DISCARD, 3)), DrawCardEffect(3))`. Deleted with them: `GameData.pendingTransformOnCreatureDiscard`, `pendingBoostSourceByDiscardedManaValue`, their hooks, `DiscardFollowUp.eachPlayerVariableAmountsWithThenEffect` |

**Behaviour changes.** Retribution, Incriminate and Barrin's Spite respect "opponents can't cause you to sacrifice" (the old path
skipped `canEffectCauseSacrifice`); Retribution's chosen creature then gets no counter, only "the other" does (new test). Oildeep
Gearhulk under Tamiyo: the card isn't discarded and the player still draws, since "if you do" is the choice (new test). Civilized
Scholar obeys CR 701.27g and transform prevention. A lone legal option is chosen unasked (CR 608.2d). Logs: the survivors' lines; the
`KEEP_IN_HAND` prompt no longer says "to keep in hand".

**For the next run.** Kroxa and Creeping Dread: build `playersThisWay(kind, CardPredicate)` (`DiscardEffect(eachPlayer())` already
chooses in APNAP order and discards at once, CR 101.4); Creeping Dread also names only the controller's own discarded card for
`CardSharesCardTypeWithCardsPredicate`; Desolation needs the permanent-filter form over `SACRIFICE` departures and a "tapped a land
for mana this turn" `PlayerPredicate`. Take all three in one run.

### Step 9, fold run 12 (2026-11-24, parallel worktree)

Rows: `conjure-exile` (3), `conjure-dup` (8), `gy-ability-store` (3), `spellbook-bf` (2); `drafted-step` (7) not taken. Ratchet in its
worktree 3,883 / 3,145 → 3,872 / 3,133. Ledger 18 rows (absorbed 6, split 8, blocked-other 1, re-batched 1, survivor 3). 14 card files
rewritten, Oracle text checked for all and for Darigaaz, Prototype X-8, Brazen Boarding, Tajic and the seven YECL pool cards. CR
400.2, 400.7j, 603.10a, 607.2a, 608.2d, 608.2h, 701.24b/c, 707.2, 708.2a, 113.2c checked. Merged with fold run 11 (`StepKind` gained
both VOTE and CONJURE).

**The CONJURE step.** `ConjureCardsEffect(cards, count, ConjureDestination)`. What it conjures: `atRandomFrom(printings)`,
`duplicateOf(PermanentSubject)` (through `cardsToDuplicate`, copiable values, CR 707.2, 708.2a), `duplicateOfTarget(filter)`, or
`duplicateOf(CardSubject)` (tokens skipped); `.times(amount)`. Where: `intoGraveyard()`, `intoExile()`, `intoExileWithSource()` (CR
607.2a), `ontoTopOfLibrary()`, `intoTopCardsOfLibraryAtRandom(n)`; the owner is the controller. It records `StepKind.CONJURE` with each
card in the zone it ended in (a replacement's zone followed), read by `cardsThisWay(CONJURE)`; one conjure event, no prompt. Log: a card
is named where it goes to a public zone or was public; a random pick into a library is "a card"; no library position is logged; a
nameless duplicate is "a face-down permanent". Battlefield conjures stay on `ConjureDuplicateOntoBattlefieldEffect` /
`ConjureCardToBattlefieldEffect`. For 12a and 20: add a hand destination (`GameLog.conjuresIntoHand`) and a recipient `PlayerSubject`.

| Batch | Result |
|---|---|
| `conjure-exile` | Limitless Rekindling and Dazzling Flameweaver split onto the step + `AllowPlayCardsFromExileEffect`. Darigaaz re-batched to `exiled-store` (run 16): egg counters on exiled cards, a perpetual exile trigger |
| `conjure-dup` | 7 folded (Fallaji Antiquarian, Gyox, Chitinous Crawler, Smog Smasher, Goblin Morale Sergeant, Sheoldred's Assimilator, Pampered Loamfrill). Prototype X-8 `blocked-other`: a battlefield duplicate that "perpetually becomes a Robot artifact" needs a perpetual type grant over a created permanent |
| `gy-ability-store` | Store merge as in run 28: `perpetualGraveyardAbilities` folded into `perpetualAbilityGrants` as `ActivatedAbility.withZones(GRAVEYARD)`; the graveyard reader takes those, the layered pass seeds only battlefield ones; `GameDataDeepCopyTest` covers it. 3 folded |
| `spellbook-bf` | The WithManaValue record's mana value is optional; `PerpetuallySetSourceBasePowerToughnessEffect` takes a non-target subject. Blood Age Muster split |

**Behaviour changes.** Effluence Devourer grants the sacrificed creature's card, as its text's "it" says (a token gets nothing; its
first test asserted the old, wrong card and was rewritten). Gyox conjures into exile, changes the cards, then shuffles them in, and
always shuffles (CR 701.24c). Fallaji copies a face-down target's face-down values. Goblin Morale Sergeant's haste is perpetual. No log
gives a library position. Graveyard grants stack (CR 113.2c). `cardsToDuplicate` reads a died triggering permanent from the entry's
snapshot.

**For the next run.** 12a on `ConjureCardsEffect` (hand destination, recipient player), then `drafted-step` (the draft choice records
its card).

### Step 9, fold run 11b (2026-11-24, parallel worktree)

Batch `object-vote` (Vault 11, Trap the Trespassers, Círdan, `WillOfTheCouncilEffect` (3 cards, unaudited), Mob Verdict (re-triage
of `unique`)) plus Fateful Tempest from `vote`. Ratchet in its worktree 3,871 / 3,133 → 3,866 / 3,128. Ledger 8 rows (split 6,
survivor 2). 8 card files rewritten, Oracle checked for all; rulings for Vault 11, Trap, Círdan, Council's Judgment, Custodi Squire,
Mob Verdict. CR 101.4, 400.7, 603.3, 603.3b, 608.2d, 701.38a–d checked.

**The shape.** `VoteEffect(Candidates, firstVoter, secret, upToOne)`; candidates `Words` (run 11), `Permanents(subject)`,
`Cards(subject)` or `Players(subject, otherThanVoter)` (CR 701.38b). Factories `forPermanents`, `forCards`, `forPlayers`,
`forAnotherPlayer`; withers `castSecretly()` (keeps the first voter) and `forUpToOne()` (Vault 11's abstention). Candidates are found
as the vote begins, unchoosable ones dropped. Each voter answers a one-of `MultiPermanentChoice` (`ObjectVoteChoice`, a
`VoteChoiceContext`, so `VotingSupport`'s extra vote works); a voter with no candidate doesn't vote, one with a single candidate and no
extra vote votes unasked (CR 608.2d). A vote is `StepOutcome.Vote(voter, "permanent:|card:|player:<id>", candidateId)`, the keys
`VotingFinishedSupport` gave Erestor and Model of Unity. Readers: `Subjects.mostVoted()` / `votedFor()` (`PermanentSubject.Voted`;
nothing is "most" when no vote was cast), `mostVotedCards()` (`CardSubject.Voted`, while the card is still in its graveyard or exile),
`playersWithNoVotes()`, `VotesReceived.byPlayer()` / `.byPermanent()`; "if no creature got votes" is `NotCondition(HappenedThisWay(VOTE,
1))`.

| Member | Result |
|---|---|
| `WillOfTheCouncilEffect` (Council's Judgment, Prime Minister's Cabinet Room, Custodi Squire) | split: `forPermanents(controlledBy(eachOpponent(), …))` + `ExileTargetPermanentEffect(mostVoted())`; `forCards(cardsInYourGraveyard(…))` + `PutCardsIntoHandEffect(mostVotedCards())` |
| `VoteForCreatureThenDestroyMostVotedEffect` (Vault 11) | split: `forPermanents(allCreatures()).castSecretly().forUpToOne()` + the no-votes draw + `DestroyTargetPermanentEffect(mostVoted())` |
| `SecretCouncilEffect` (Trap) | split: `PutCounterOnTargetPermanentEffect(votedFor(), STUN, VotesReceived.byPermanent())` + `TapPermanentsEffect(votedFor())` |
| `CirdanTheShipwrightEffect` | split: `EachPlayerDrawsCardEffect(VotesReceived.byPlayer())` + `EachPlayerMayPutCardFromHandToBattlefieldEffect.by(playersWithNoVotes(), permanent card)` |
| `MobVerdictEffect` | split; the record stays as the per-vote damage payload (one event per vote); the vote and `DrawCardEffect(VotesReceived.byPlayer())` are steps |
| `FatefulTempestEffect` | split: `startingWithYou(PAST, PRESENT)` + `anyVotes`-gated mill-then-damage and exile-and-play steps (fold run 9's mill split kept) |

`forUpToOne` (Vault 11) and `otherThanVoter` (Mob Verdict) are each set by one card: each is the card's own wording on a candidate
kind two or more records use.

**Behaviour changes.** Open object votes and Fateful Tempest's votes are logged as cast (Council's Judgment and Custodi Squire
rulings), a face-down permanent's name redacted. Trap's, Círdan's, Mob Verdict's and Vault 11's secret votes are revealed in one line
after the last vote, once (the destroy step's death-replacement replay no longer reaches the vote). Vault 11's vote finishes before its
destroy step, so Model of Unity's trigger goes on the stack then, unordered, and is ordered with the other new triggers before priority
(`TriggeredAbilityQueueService.beginTriggerOrder`, CR 603.3b); the test's mid-prompt stack assertion, which checked the old replay
order, was dropped (reviewed by the orchestrator). Trap's counters go through the counter verb. Círdan's hand put uses Show and Tell's
prompt, which the AI answers.

**For the next run.** The per-vote choose step for Elrond and Expropriate: one choice per vote, made by or about that vote's voter, in
voting order; a `ChoosePermanentsEffect` form over `StepOutcome.votes()` of one option, then their gain-control and counter steps.

### Step 9, fold run 10b (2026-11-24, parallel worktree)

Batch `opp-card` (8 rows): split 4, absorbed 1, survivor 1, blocked-other 2. Ratchet in its worktree 3,861 / 3,123 → 3,856 / 3,118. 7
card files rewritten; Oracle text checked for all 9 cards, rulings for Allure, Animal Magnetism, Coin of Fate and Burning-Rune Demon.
CR 400.7, 608.2c, 608.2d, 701.20b, 701.23a–d checked.

| Member | Result |
|---|---|
| Murmurs from Beyond | `RevealTopCardOfLibraryEffect.topCards(you(), 3)` + `ChoosePlayerEffect.opponent()` + `ChooseCardEffect.oneOf(revealedThisWay()).by(playersThisWay(CHOOSE))` + `PutExiledCardsIntoOwnersGraveyardEffect(cardsThisWay(CHOOSE))` + `PutCardsIntoHandEffect(revealedThisWay())` |
| Animal Magnetism | the same with five cards and a creature filter + `PutCardsOntoBattlefieldEffect(cardsThisWay(CHOOSE))` (entry batch) + the rest to the graveyard |
| Tasigur / O-Kagachi | `ChoosePlayerEffect.opponent()` (O-Kagachi: `.by(defendingPlayer())`) + the graveyard choice + `PutCardsIntoHandEffect`; O-Kagachi's boost reads `AmountThisWay.chosenManaValue()` behind `HappenedThisWay(CHOOSE)` |
| Coin of Fate | `ChooseCardEffect.oneOf(exiledAsCost())` + `PutCardsIntoLibraryEffect.onBottom` + `PutCardsOntoBattlefieldEffect(except(exiledAsCost(), cardsThisWay(CHOOSE))).enteringTapped()`; `ExiledAsCost` names a tracked exile-N cost's cards |
| Intuition (survivor) / Burning-Rune Demon | `IntuitionEffect(count, filter, differentNames)` is the search-and-reveal step: the found cards stay in the library as its REVEAL result (CR 701.20b), the search recorded as the answer ends the step; then the choice, hand, rest to graveyard, `ShuffleLibraryEffect(false)` |
| Allure of the Unknown | `blocked-other`: the cast-now step offers only the controller (Possibility Storm needs the same), and after a card CHOOSE `playersThisWay(CHOOSE)` is the cards' owner, not the chooser |
| Phyrexian Grimoire | `blocked-other`: no "top two cards of your graveyard" subject (one card would set the count) |

**Where the triage was wrong.** Intuition's note leaned on a SEARCH step that doesn't exist yet (fold run 13 may move it to
`StepKind.SEARCH`; take `IntuitionEffect` as its first member). Coin of Fate's `exiledAsCost()` named only a single cost-exiled card.

**Behaviour changes.** Burning-Rune Demon's search is recorded (Ob Nixilis triggers) and obeys Opposition Agent and Aven Mindcensor;
its chooser is the controller's choice of opponent, not the first seat; "exactly two" finds two or none (one was accepted; no ruling
covers it, read from "exactly" with the right to fail to find, CR 701.23b — the orchestrator's judgement). A lone legal card is taken
unasked (Animal Magnetism, Coin of Fate; CR 608.2d). Animal Magnetism and Coin of Fate enter through the entry batch. The AI search
answer comes from the deciding player and respects different names. Prompts are `ChooseCardEffect` prompts; logs the survivors'.

**For the next run.** Allure waits for a caster `PlayerSubject` on `MayCastDiscardedCardWithoutPayingManaCostEffect` (with
Possibility Storm, `exile-until-rel`, run 29) and a "chooser of the last card choice" subject. Grimoire waits for a counted
`TopOfGraveyard`.

### Step 9, fold run 12a (2026-11-24, parallel worktree)

Rows: `conjure-named` (8), `dup-hand` (21). Ratchet in its worktree 3,850 / 3,111 → 3,837 / 3,098. Ledger 33 rows (absorbed 14, survivor
4, `step-9` left 15). 18 card files rewritten; Oracle text checked for all and for Warzone Duplicator; CR 115.1, 400.2, 400.7, 402.3,
601.2c, 608.2d, 608.2h, 707.2, 707.10, 708.2a, 111.1 checked.

**The CONJURE step, widened.** `ConjureDestination.intoHand()`, `intoLibrary()` (anywhere), `intoLibraryFromTop(n)`; random positions
of one step are distinct among the top N, the other cards keep their order. `ConjureCardsEffect.named(set, number, dest)` (one
printing; may be named), `.into(players)` (the controller conjures into their zones, each owns theirs), `.conjuredBy(players)` (each
conjures into their own), `.withAbilities(...)` ("perpetually gain", built into the card), `.repeatingWhileHandSmallerThan(amount)`,
`.times(int)`, and `duplicateOf(targetSpell())` (declares the spell target; face-down values for a face-down spell). Log: a hand or
library card is "a card" (`GameLog.conjuresIntoHand`) unless the original was public or the text names it. One conjure event per
conjuring player per round. New step `PerpetuallyGrantAnyColorManaToCastEffect(CardSubject)`.

| Batch | Result |
|---|---|
| `conjure-named` | 8 folded: Indris, Sanguine (`withAbilities`), Calim, Juggernaut Peddler (`conjuredBy`), Sandcloud Harbinger (`into(eachPlayer())`), Goblin Influx Array, Skywriter Djinn (`repeatingWhileHandSmallerThan`, with Suntail Squadron moved off ConjureCardNamedIntoHandEffect), Mothlight onto ConjureCardNamedIntoHandEffect, Power Nine onto ConjureSpellbookIntoLibraryEffect |
| `dup-hand` | 6 folded (Sinister Reflections/Ulder, Bind to Secrecy, Futurist Spellthief, Undercity Plunder, Clone Crafter/Lae'zel, Semblance Scanner); 15 left: triggering cards put into a hand have no subject (Kithkin, Veteran Ghoulcaller, Lurker: three, build next), a returned-to-hand permanent's last-known copy (Vesuvan Mist/Warzone), a destroyed-this-way subject (Flames of Moradin, Vona), an any-type grant step (Vona, PerpetualAnyManaTypeToCastSelfEffect), a top-of-library subject (Agent of Raffine), bespoke prompts (Vodalian, Gutmorn, Snowborn), tests on printed keywords (Ace), Viconia, Grizzled Huntmaster, the payload ConjureDuplicateOfCardIntoHandEffect |

**Behaviour changes.** Calim's conjured card is a real printing marked conjured. Sandcloud no longer shuffles the cards already in the
top ten. Prismatic is "as though any colour" (Bind, Vesuvan, Warzone had "any type": coloured mana paid {C}). Sinister/Ulder copy a
face-down target's face-down values; Futurist a face-down spell's (untested: no face-down cast in the harness). Goblin Influx Array
and Oracle of the Alpha conjure real printings; Reckless Ringleader (J21 21) isn't implemented, so Goblin Influx Array's spellbook
lacks it (it was a hand-built 1/1 haste Goblin without its enter ability; implement it in fold run 12b, defect queue). Logs: the
survivor's lines.

**For the next run.** A card subject for the triggering cards put into a hand (Kithkin, Veteran, Lurker), then Vodalian/Gutmorn with
`conjuredBy`, then a destroyed-this-way duplicate subject with an any-type grant step (Flames, Vona).

### Step 9, fold run 14 (2026-11-24, parallel worktree)

Batches `tap` (8) and `goad` (7) taken; `piles` (5) and `auction` (3) not taken (fold run 14b). Ratchet in its worktree 3,845 / 3,106 →
3,832 / 3,094. Ledger 18 rows (absorbed 13, split 1, survivor 3, unique 1, blocked-other 1). 19 card files rewritten, Oracle checked
for all; rulings Yotia, Teferi (MOM 239), Kaima, Vislor. CR 701.15a–d, 608.2d, 608.2h, 611.2c, 119.10 checked.

**The TAP step.** `TapPermanentsEffect` records `StepKind.TAP` on every path: the permanents it newly tapped (not one already tapped
or unable to become tapped) and their controllers. "Tap any number of untapped [f] you control. … for each tapped this way" =
`ChoosePermanentsEffect.anyNumber(controlledBy(you(), allOf(f, PermanentCanBecomeTappedPredicate)), label)` +
`TapPermanentsEffect(thisWay(CHOOSE))` + the verb over `AmountThisWay(TAP)` / `thisWay(TAP)` (the new predicate keeps the old offer,
which left out creatures under a tap restriction). "When you do … that much / X" = `QueueReflexiveAbilityEffect.withEventValue(effect,
AmountThisWay(TAP))`, fixed as the reflexive ability is made and read by its target filter (Teferi); both rulings say it triggers with
none tapped. "Tap all [f] that player controls … X damage" = `TapPermanentsEffect(controlledBy(triggeringPlayer(), f))` +
`DealDamageToPlayersEffect(AmountThisWay(TAP), triggeringPlayer())`.

**The GOAD step.** `GoadTargetCreatureUntilNextTurnEffect(targetRestriction, goaded, Duration)`: `UNTIL_YOUR_NEXT_TURN` by default
(CR 701.15a), `PERMANENT` for "the rest of the game", `WHILE_SOURCE_REMAINS`; `goaded` is any subject that doesn't target (`of(subject,
duration)`, `targetWhileSourceRemains()`); a set is locked at resolution (Kaima's ruling, CR 611.2c); records `StepKind.GOAD`; a re-goad
by the same player is recorded and adds no requirement (CR 701.15d).

| Batch | Result |
|---|---|
| `tap` | 8 folded; 7 tap contexts and their handlers left `MultiPermanentChoiceHandlerService` |
| `goad` | 6 folded (Life of the Party, Rendmaw, The War Games, Hot Pursuit, Kaima split, Nettling Nuisance). Vislor Turlough `blocked-other` ("as long as they control it" needs a duration following another player's control while you stay the goader; one card). `GoadEquippedCreatureEffect` unique (its planned second user was a resolution's goad) |

**Behaviour changes.** Nettling Nuisance's Pirate is goaded by Nettling's controller, as its reminder text says ("a player other than
you", CR 701.15b), and survives losing abilities; the old static self-goad named the token's own controller. Urge to Feed's and
Kaima's counters go through the counter verbs. The AI taps all its own for a tap step. Prompts and logs are the survivor's. Ledev
Champion and Orphans of the Wheat with nothing tapped get a +0/+0 boost.

**For the next run.** `piles`: today's pile machinery is `PendingPileSeparation` + about 20 `CardPileDisposition` values handled in
`DestructionSupport`, `FightOrFlightSupport`, `StandOrFallSupport` and the multi-permanent and graveyard choice handlers; build one
`SeparateIntoPilesEffect(permanents | cards, separator, chooser, faceDown)` recording CHOOSE (the other pile is `except(…)`), then the
verbs over it; Stand or Fall takes each defending player. `auction`: one bid step recording the high bidder (`playersThisWay`) and the
bid (`AmountThisWay`), keeping the death-replacement asks. Kaima's "goaded this way" counts a creature you already goaded (CR 701.15d;
unsure, unchanged).

### Step 9, fold run 13 (2026-11-24, parallel worktree)

`search-step` (13 rows) and `IntuitionEffect`: 8 folded (5 split, 3 absorbed), 1 survivor, 5 blocked-other. `search-attach`,
`same-name-search` and `per-object-search` not taken (time; fold run 13b). Ratchet in its worktree 3,840 / 3,101 → 3,832 / 3,093. 15
card files rewritten, all checked with `get_card`; rulings Scion, Distant Memories, Transmute Artifact, Treasure Chest, Nazahn, Ruin,
Opposition Agent. CR 400.2, 400.7j, 608.2c, 608.2d, 608.2h, 701.20b, 701.23a–d checked. Merged with fold run 14 (`StepKind` holds TAP,
GOAD and SEARCH).

**The SEARCH step (`StepKind.SEARCH`).** `SearchLibraryEffect` and `SearchTargetLibraryEffect` record nothing as they begin;
`LibraryChoiceHandlerService.recordFoundCard` adds each card as its pick is answered, before it moves: the card, the searcher, and the
zone it goes to (a card going onto the battlefield gets no zone; `searched()` still names its permanent). A card Opposition Agent
exiles (its ruling) and a prevented search are left out; a search that finds nothing records nothing found (CR 701.23a, 701.23b).
`IntuitionEffect` records SEARCH beside REVEAL. Readers: `HappenedThisWay.searched()`, `toACard(SEARCH, filter)` (wherever the card is
now, with the entry's X), `AmountThisWay(SEARCH)` and `.searchedManaValue()`, `Subjects.foundThisWay()` and `cardsThisWay(SEARCH,
filter)` (a graveyard card a replacement exiled instead is followed into exile). `REVEAL_ONLY` keeps the found card in the library for
a later mover (CR 701.20b), revealing it only when the search states a quality; `EXILE_IMPRINT` imprints it on the source.
`PutCardsIntoHandEffect` doesn't name a library card a search found and no REVEAL step showed (CR 400.2). Not recorded yet:
`HAND_OR_GRAVEYARD` and the flows handled before the generic path (same-name queue, subtype and colour picks, basic-land queues,
held-out pools, d20, the may-put-selected branch).

| Batch | Result |
|---|---|
| `search-step` | Courier `MayEffect(Sequence(search, ConditionalEffect(NotCondition(searched()), Food)), …, Food)`; Myr Incubator search to exile + tokens `AmountThisWay(SEARCH)`; Scion graveyard search + `BecomeCopyOfSubjectEffect.of(foundThisWay()).untilEndOfTurn()`; Seek target-library exile + `GainLifeEffect(searchedManaValue())`; Anchor, Instrument, Oriq use `toACard(SEARCH, …)`; Woodland `PerpetuallyGrantCardCharacteristicsEffect.to(foundThisWay())`; Nazahn, Ruin, Treasure Chest `REVEAL_ONLY` + battlefield or may-battlefield mover + hand + shuffle; Strata Scythe, Hoarding Dragon `EXILE_IMPRINT`; Stonehewer, Quest `BATTLEFIELD_ATTACHED_TO_CREATURE`. Distant Memories (+ payload) blocked (an any-opponent-in-turn may); Transmute ×3 blocked (cost-shape: a filter against `sacrificedManaValue()` and a difference amount; step 10) |

**Behaviour changes.** Opposition Agent (ruling: an effect can't see the found cards in exile): Courier makes its Food; Oriq,
Instrument, Anchor, Woodland, Nazahn, Ruin, Treasure Chest and Scion no longer act on the exiled card. Stonehewer and Quest record an
empty-library search and shuffle (CR 701.23a), and skip cards that can't enter from the library. Nazahn, Ruin and Treasure Chest put
the card onto the battlefield from the library through the entry batch (Treasure Chest ruling); Ruin's land enters tapped. A found card
put into a hand without being revealed is logged as "a card". New tests: Courier, Oriq, Stonehewer, Treasure Chest.

**For the next run.** `search-attach`: found-and-kept + `PutCardsOntoBattlefieldEffect.attachedTo(host)` needs a could-enchant-host
card predicate (enchant restriction, protection) and Curse of Misfortunes' "not the name of a Curse attached to that player".
`same-name-search`: one per-permanent same-name search over a `PermanentSubject` and a library-owner `PlayerSubject`, recorded as
SEARCH. `per-object-search`: one may-search per land on `EachPlayerSearchesLibraryEffect` (Wave of Vitriol, Winds of Abandon's
defect). Delete `EXILE_AND_CREATE_TOKENS` with its test.

### Step 9, fold run 12b (2026-11-24, parallel worktree)

Rows: `perp-hidden-sets` (12), `perp-chosen` (11), `perp-owned` (3), `perp-topmost` (3), plus Reckless Ringleader. Ratchet in its
worktree 3,827 / 3,088 → 3,808 / 3,069. Ledger 29 rows (absorbed 9, split 10, blocked-other 6, unique 1, `step-9` left 3). 20 card
files rewritten and 1 card added; Oracle text checked for all 31 cards. CR 608.2d, 400.2, 400.7, 613.4b, 613.4c, 113.2c, 708.2a,
608.2h, 402.3 checked.

**Store merges (St).** `perpetualPowerToughnessModifiers` went into `perpetualCardPowerToughnessModifiers`, `perpetualKeywords` into
`perpetualCardKeywords`; keywords are written only through `GameData.grantPerpetualKeywords` (immutable values replaced on each
change). A permanent reads its card's entry and, when it is a copy, its physical card's entry. `PerpetuallyBoostCardEffect`'s write to
both cards for a Clone's self-boost is one write. Naktamun's `perpetualCardPowerModifiers` and its read on entry are gone. The evoke
store and the third cost store (`perpetualGenericCastCostIncreases`) stay.

| Batch | Result |
|---|---|
| `perp-hidden-sets` | 9 folded: Bramblearmor (`toCards(cardsInYourLibrary)`), Rotboar, Topsoil Turner (`PerpetuallyGrantAbilitiesEffect` over `cardsInHands(defendingPlayer())` / `cardsInYourHand`), Kami of Transmutation (`PerpetuallyGrantCardCharacteristicsEffect.to(...)`), Fountainport and Charged Conjuration (`PerpetuallyChangeCastCostEffect(..., -1)`); Lae'zel and Coalition Construct split into a permanent boost plus a hand boost. Left: Aquatic Subtlety (an evoke ability record), Seething Skitter-Priest (a stacking set grant, CR 113.2c), Wildgrove Summoner (base P/T over cards), Absorb Energy (target-spell types, CR 608.2h) |
| `perp-chosen` | 8 folded: `ChooseCardEffect.atRandom` / `.oneOf` or `RevealMatchingCardsFromTargetHandAndKeepEffect`, then the verb over `cardsThisWay(CHOOSE)`; Ravenous Pursuit uses the excess-damage bite. Left: Fuel Tank Feaster (a greatest-MV card predicate), Boareskyr Tollkeeper (an enter-tapped ability), Hydroponics Architect (a supertype store), Thought Partition (unique: colour and mana cost) |
| `perp-owned` | Naktamun onto `PerpetuallyBoostOwnedCardsEffect`. Tend to the Kiln and Scalar Scholar left: a cards-you-own subject would have to name battlefield and stack cards |
| `perp-topmost` | 3 folded onto `Subjects.topmostCardInYourLibrary(filter)` (new `CardSubject.TopmostInLibrary`) |

**Reckless Ringleader (J21 21).** `ChooseCardEffect.oneOf(cardsInYourHand(creature))` +
`PerpetuallyGrantKeywordsToTriggeringCardEffect.toCards(cardsThisWay(CHOOSE), HASTE)`; new `CardSet.SET_J21`. Goblin Influx Array's
spellbook holds it again.

**Behaviour changes.** Golden Sidekick, Dalkovan Outrider and Thorna boost in layer 7c (CR 613.4b, 613.4c), not printed values (Turn
to Frog tests). Skullpiercer Gnat and Wagon Wrecker no longer target "that player" (hexproof test). Ravenous Pursuit chooses a lone
card unasked. Copies read their physical card's perpetual P/T and keywords. The keyword subject path restores perpetually lost
keywords and counts extra instances. The card-subject paths of the boost, characteristics and keyword survivors log through
`PerpetualChangeLog`. Wingbright Thief offers only the revealed cards (its old prompt showed the whole target hand). "It" across zone
changes (CR 400.7) keeps today's behaviour.

**For the next run.** The cards-you-own step (Tend, Scalar, `PerpetuallyBoostOwnedCardsEffect`), a static evoke ability (Aquatic
Subtlety), and merging the three cast-cost stores.

### Step 9, fold run 14b (2026-11-24, parallel worktree)

Batch `piles` (5) taken; `auction` (3) not taken (fold run 14c). Ratchet in its worktree 3,814 / 3,076 → 3,809 / 3,072. Ledger 7 rows
(survivor 1, absorbed 5, dead 1). 5 card files rewritten, Oracle and rulings checked for all. CR 700.3a, 700.3c, 700.3d, 506.2a,
509.1b, 608.2c, 101.4, 611.2c, 708.2 checked.

**The pile step.** `SeparateIntoPilesEffect.permanents(controlledBy(players, filter), label)` / `.cards(cardsInGraveyards(...),
chooser, label)`. The controller separates each group into Pile 1 and the rest; either pile may be empty (CR 700.3d), and nothing
moves (CR 700.3c). Then the group's chooser takes a pile through a yes/no prompt (Yes = Pile 1). Permanents are grouped by
controller, and each of those players chooses their own pile ("for each defending player"). A `targetPlayer()` set declares the
benign player target. The step records CHOOSE with the chosen piles; the other pile is `except(set, thisWay(CHOOSE))`. Between prompts
the state rides on the queue-only `PendingPileStep`; the step pauses at its end. `separator` (always the controller) and `faceDown`
are left out (no member sets them; Make an Example and the Fact-or-Fiction family when they fold). `PendingPileSeparation` stays for
the card dispositions and Make an Example; ATTACKERS, BLOCKERS, DESTROY and OPPONENT_CHOOSES_EXILE are gone, as are
`FightOrFlightSupport`, `StandOrFallSupport`, and DestructionSupport's and GraveyardReturnSupport's pile code.

| Batch | Result |
|---|---|
| `piles` | 5 folded: Fight or Flight (+ `CantAttackThisTurnEffect(except(controlledBy(activePlayer(), creature), thisWay(CHOOSE)))`), Stand or Fall (over `eachOpponent()` + `CantBlockThisTurnEffect(except(...))`), Do or Die (+ `DestroyTargetPermanentEffect(thisWay(CHOOSE, creature), true)`), Liliana of the Veil (+ `SacrificeSelfEffect(thisWay(CHOOSE), targetPlayer())`), Death or Glory (`ChoosePlayerEffect.opponent()` + `.cards` + exile + `PutCardsOntoBattlefieldEffect(except(...))`); `OnlyChosenCreaturesCanBlockEffect` dead |
| `auction` | not taken: one bid step needs the three bid prompts (`IllicitAuctionBidChoice`, `MagesContestBidChoice`, `PainsRewardBidChoice`), their projections, AI strategies and interaction handlers merged, with the prize on the prompt |

**Behaviour changes.** Stand or Fall separates each opponent's creatures, each choosing their own (it was `getOpponentId` only; CR
506.2a). Death or Glory's controller chooses the opponent who picks the pile (it was the first other seat); the exile and the return
are two steps (CR 608.2c). The two restrictions carry their source's name (`refactors-to-do.md` entry 9 marked done).
`BlockLegalityService.canBlock` rejects a creature under a board-wide "can't block" whose attackers are every creature (CR 509.1b):
it was offered as a blocker and refused only per attacker (Bedlam-style statics, Goblin War Cry); `CombatBlockService` keeps that
restriction's message. A face-down permanent in a pile is named by its public description (tested). Prompts and logs are the
survivor's.

**For the next run.** `auction` (14c): one bid step recording the high bidder (`playersThisWay`) and the bid (`AmountThisWay`), the
death-replacement asks kept, the three bid prompts and AI strategies merged with the prize kind on the prompt. Make an Example
(`blocked-player-loop`) now fits the pile step with a `separator` option and `you()` choosing, then one sacrifice batch, which also fixes
its "sacrifices each opponent's piles before the next separates" defect.

### Step 9, fold run 13b (2026-11-24, parallel worktree)

`search-attach` (4), `same-name-search` (3), `per-object-search` (1 + the Wave of Vitriol joiner): 9 folded (8 absorbed, 1 split), 4
survivor rows (1 new record). Ratchet in its worktree 3,806 / 3,068 → 3,798 / 3,060. 11 card files rewritten, all checked with
`get_card`; rulings for Sovereigns, Light-Paws, Auratouched Mage, Curse of Misfortunes, Bitterheart, Clarion, Dichotomancy, Doubling
Chant, Winds, Leonin Arbiter. CR 101.4, 201.2a, 303.4a, 303.4f, 303.4g, 608.2d, 608.2e, 608.2h, 701.23a, 701.23b, 702.16c, 708.2
checked.

| Batch | Result |
|---|---|
| `search-attach` | Search with `KEEP_IN_LIBRARY` + `PutCardsOntoBattlefieldEffect(foundThisWay()).attachedTo(host)` + `ShuffleLibraryEffect(false)`, in a `MayEffect`. Hosts: Sovereigns `triggering()`; Light-Paws `source()`; Curse `enchantedPlayer()`; Bitterheart a card-level player target read by `targetPlayerDeclaredElsewhere()`. Sovereigns and the Mage filter with `CardCouldEnchantPredicate`; Light-Paws has none (its ruling: an Aura that can't be attached stays in the library). The Mage uses `REVEAL_ONLY` + `ConditionalEffect(SourceIsOnBattlefield)` for its battlefield and hand branches |
| `same-name-search` | New `SearchLibraryForSameNameAsEachPermanentEffect(permanents, libraryOwner, creatureOnly, tapped, optional)` over the existing same-name pick queue: one search, one pick per permanent, records SEARCH. Clarion = `ChoosePermanentsEffect.exactly(5, controlledBy(you()))` + the record over `thisWay(CHOOSE)` |
| `per-object-search` | `EachPlayerSearchesLibraryEffect.oneSearchForEach()`: that many one-card searches. From the Ashes = `DestroyAllPermanentsEffect` + the search over `playersThisWay(DESTROY)`; Wave = `SacrificeEachMatchingPermanentEffect` (now a SACRIFICE step) + the search over `playersThisWay(SACRIFICE)`; Winds of Abandon's overload gains `.oneSearchForEach()` |

**Behaviour changes.** Light-Paws can find an Aura that can't be attached to it (it stays in the library) and may search with
Light-Paws gone. Sovereigns with the attacker gone searches and shuffles (ruling). Bitterheart and Curse of Misfortunes: a Curse that
can't enchant the player stays in the library (CR 303.4g, 702.16c). Winds (overload) and Wave: one search per object (the P15 defect;
Winds' test failed first); found lands enter together after all searches (CR 101.4); the queue no longer offers again a card an earlier
search found. Clarion: a choice among five or fewer is made unasked (ruling, CR 608.2d), and it reads effective names, so a face-down
permanent finds nothing. SEARCH is now recorded by same-name picks and the deferred each-player queue. Deleted:
`EXILE_AND_CREATE_TOKENS`, `LibrarySearchParams.tokenTemplate` / `sourceSetCode`.

**For the next run.** Queue-based searches still without SEARCH: subtype and colour picks, held-out pools, d20, the may-put-selected
branch, `HAND_OR_GRAVEYARD`. The "may search … then shuffle" shuffle under a search prohibition needs a may-search option on
`SearchLibraryEffect` or a shuffle gated on `HappenedThisWay.searched()` (defect queue).

### Step 9, fold run 14c (2026-11-24, parallel worktree)

Batch `auction` (3 rows) taken, plus `ScryIfPlayerDealtDamageThisWayEffect` (blocked since fold run 2 on a player form of
`HappenedThisWay`). Ratchet in its worktree 3,787 / 3,049 → 3,784 / 3,046. Ledger 6 rows (split 3, absorbed 1, survivor 1, blocked-other
1). 4 card files rewritten, Oracle text checked for all; rulings for Illicit Auction and Pain's Reward (Mages' Contest has none). CR
101.4, 608.2c, 608.2h, 616.1 checked.

**The BID step.** `BidLifeEffect(PlayerSubject bidders, Integer openingBid, Prize prize)` (`eachPlayerStartingAt(n, prize)`,
`eachPlayerStartingAtAnyNumber(prize)`). The controller opens, at `openingBid` or asked when that is `null`; the other bidders follow in
turn order from the controller, round after round, as the cards' text says ("in turn order, each player may top the high bid"; CR
101.4). A player who passed may top a later bid (Pain's Reward ruling). The bidding ends when it comes back to the high bidder; a lone
bidder's opening bid stands unasked. It resolves again on each answer, changing nothing but `GameData.lifeBid` and the bid log lines;
once the high bid stands it records `StepKind.BID` (the high bid as the amount, the high bidder as the player). One prompt,
`PendingInteraction.LifeBidChoice` (with `prize` and `prizeTargetId`), one projection (face-down names masked), one interaction
handler, one AI strategy (bids 0). `Prize` (`CONTROL_OF_TARGET`, `COUNTER_TARGET_SPELL`, `CARDS`) drives the prompt text and keeps the
AI's views (HARMFUL target polarity, COUNTERSPELL category).

**The player form of `HappenedThisWay`.** `HappenedThisWay.toAPlayer(kind, PlayerRelation)`: a player the step recorded has that
relation to the controller. Two users: Mages' Contest (`BID`, `SELF`) and Play with Fire (`DEAL_DAMAGE`, `ANY`).

| Batch | Result |
|---|---|
| `auction` | 3 split. Illicit Auction: `target(creature)` + `BidLifeEffect.eachPlayerStartingAt(0, CONTROL_OF_TARGET)` + `LoseLifeEffect(new AmountThisWay(BID), playersThisWay(BID))` + `GainControlOfTargetEffect(PERMANENT).gainedBy(playersThisWay(BID))`. Mages' Contest: `BidLifeEffect(allOf(you(), controllerOf(targetSpell())), 1, COUNTER_TARGET_SPELL)` + the loss + `ConditionalEffect(toAPlayer(BID, SELF), CounterSpellEffect())`. Pain's Reward: `eachPlayerStartingAtAnyNumber(CARDS)` + the loss + `DrawCardEffect(4, playersThisWay(BID))`. Gone: 3 records, 3 handlers, 3 prompts with their projections, AI strategies and handlers, `LifeBidAuctionSupport`, `IllicitAuctionState` (now `LifeBidState`) |
| Play with Fire (fold run 2 leftover) | `ScryIfPlayerDealtDamageThisWayEffect` absorbed into `ConditionalEffect(HappenedThisWay.toAPlayer(DEAL_DAMAGE, ANY), ScryEffect(1))` |

**Behaviour changes.** Bidders after the caster follow turn order from the caster (the old order was seat order with the caster moved
to the front); test: Pain's Reward with three players. The Mastery sacrifice that replaces the high bidder's loss is asked in the loss
step, after the bid is recorded and before any life is lost (CR 616.1); test assertions unchanged. An opening bid of 0 logs "bids 0
life". Illicit Auction's extra "gains control of" line is gone.

**For the next run.** `ResolveRiderForPlayerEffect` now has one user, `DestroyTargetPermanentThenEffectHandler`'s riders. The
life-bid AI always bids 0; `LifeBidChoice.prize()` is there for an AI that values the prize.

### Step 9, fold run 12c (2026-11-24, parallel worktree)

Rows: the `dup-hand` and `perp-owned` leftovers of 12a/12b. Ratchet in its worktree 3,787 / 3,049 → 3,780 / 3,042. Ledger 16 rows
(absorbed 5, split 2, survivor 4, blocked-other 1, `step-9` 4). 10 card files rewritten; Oracle text checked for those and for Vona,
Flames and Nantuko Slicer. CR 608.2h, 707.2, 707.10, 708.2a, 608.2d, 400.2, 108.2b checked. (This run ran `gradlew --stop` at its end,
which stopped the shared daemons and one of the orchestrator's sweeps; the parallel brief now forbids it.)

| Batch | Result |
|---|---|
| triggering cards in hand | 4 folded: Kithkin Brinefarer and Veteran Ghoulcaller onto `ConjureCardsEffect.duplicateOf(triggeringCardsInHand(), intoHand())`; Lurker in the Deep split (conjure + `manifested()` put); Vodalian Tide Mage split (`ChoosePermanentsEffect.exactly(1, triggeringAll())` + duplicate of `thisWay(CHOOSE)`, its bespoke context deleted). Gutmorn `blocked-other`: no chooser on `ChoosePlayerEffect` (one user), and `triggeringCards()` loses a discarded card that left the graveyard |
| cards you own | 3 folded onto `PerpetuallyBoostCardEffect.toCards` / `PerpetuallyGrantCardCharacteristicsEffect.to` / `PerpetuallyChangeCastCostEffect` over `cardsYouOwn(filter)`; the three cast-cost stores merged into the signed `perpetualGenericCastCostIncreases` (deep-copy test) |
| destroyed this way / any type | not folded: Flames of Moradin needs a departed-this-way subject (one user) and a perpetual alternate-cast grant (second user: Aquatic Subtlety's evoke); Vona's `duplicateOf(target())` would declare a second target spec |
| evoke / last-known copy | not reached |

**Shared pieces.** `CardSubject.TriggeringCardsInHand` names an event's cards in a hand while they are there;
`SubjectResolutionService.cardsToDuplicate(…, CardSubject)` reads them as last known, also once they leave (CR 608.2h).
`CardSubject.Owned` (`Subjects.cardsYouOwn(filter)`): every zone, the battlefield (the physical card, judged as the permanent is now)
and the stack (no copies); only for the perpetual verbs; one unnamed log line per owner. `PerpetualCardCastCostSupport.change/changeFor`
writes and reads the one cast-cost store.

**Behaviour changes.** Vodalian's duplicate of a face-down dealer is a nameless 2/2 (CR 707.2, 708.2a; the defect line). The
owned-cards verbs change a copy's physical card, not its copy card. Veteran Ghoulcaller logs "a card"; owned-cards changes log "Each
affected card P owns perpetually …"; Tend to the Kiln and Scalar Scholar now log. Lurker's manifest goes through the entry batch. "It"
across zone changes (CR 400.7) keeps today's reading.

**For the next run.** Flames of Moradin and Aquatic Subtlety together: one perpetual alternate-cast grant step and store (the evoke
store folded in) plus a departed-this-way subject for Flames. Then Vona with a non-declaring "that permanent" reading. Then Vesuvan
Mist / Warzone Duplicator, and Gutmorn (a chooser on `ChoosePlayerEffect`, with Choose Your Champion).

### Step 9, fold run 8b (2026-11-24, parallel worktree)

Two leftover sets: Desolation, Creeping Dread and Kroxa from `splits` (fold runs 8 and 9b), and Elrond and Expropriate from `vote` (fold
runs 11 and 11b). All 5 folded (split onto existing steps). Ratchet in its worktree 3,774 / 3,037 → 3,769 / 3,032. Ledger 6 rows (5
split, 1 survivor). 5 card files rewritten; Oracle text and rulings checked for all. CR 101.4, 400.3, 608.2c, 608.2d, 608.2h, 603.7c,
701.9a, 701.38a, 701.38d checked. Merged with fold run 12c (`CardSubject` permits both `TriggeringCardsInHand` and `OwnedBy`).

**The pieces.** `playersThisWay(kind, CardPredicate)` (DISCARD, MILL, DRAW, REVEAL): the players whose zone now holds a recorded card
that matches (its owner, CR 400.3, or its controller when a replacement put it onto the battlefield), a share-type filter bound as the
subject resolves. `playersThisWay(kind, PermanentPredicate)`: the controllers of the recorded permanents that match, as they are or last
were. `CardSubject.OwnedBy(cards, owners)` / `Subjects.ownedBy`: "the card you discarded", found in exile too (Leyline of the Void).
`PlayerTappedLandForManaThisTurnPredicate`. `ChoosePermanentsEffect.forEachVote(option, candidates, CONTROLS | OWNS, byVoter, label)`:
one choice per vote for `option` in the last vote, in voting order (extra votes together, CR 701.38d), the voter or you choosing, a lone
candidate taken unasked (CR 608.2d), a permanent chosen twice recorded once; `RecordChosenPermanents.laterChoices` is a list of
`Offer(chooserId, permanentIds, prompt)`, shared with `oneForEach`.

| Member | Result |
|---|---|
| Kroxa | `DiscardEffect(1, eachOpponent())` + `LoseLifeEffect(3, except(eachOpponent(), playersThisWay(DISCARD, nonland)))` |
| Creeping Dread | `DiscardEffect(1, eachPlayer())` + `LoseLifeEffect(3, except(playersThisWay(DISCARD, shares(ownedBy(cardsThisWay(DISCARD), you()))), you()))` |
| Desolation | `SacrificePermanentsEffect(1, land, matching(eachPlayer(), tapped-land))` + `DealDamageToPlayersEffect(2, playersThisWay(SACRIFICE, Plains))` |
| Elrond of the White Council | `secretly(FELLOWSHIP, AID)` + `forEachVote(FELLOWSHIP, allCreatures(), CONTROLS, true)` + `GainControlOfTargetEffect.of(thisWay(CHOOSE), PERMANENT)` + the can't-attack-its-owner grant + `anyVotes(AID)` counters |
| Expropriate | `startingWithYou(TIME, MONEY)` + `anyVotes(TIME)` extra turns + `forEachVote(MONEY, allPermanents(), OWNS, false)` + gain control + exile |

**Deleted.** The 5 records and handlers, `CreepingDreadState` / `GameData.creepingDread`, `ForcedSacrificeThenDamageIfSubtype`, four vote
and choice contexts, `ChoiceHandlerService.recordVotingChoiceIfApplicable`.

**Behaviour changes.** Creeping Dread: an opponent with Tamiyo no longer discards (the old handler ignored the prevention; new test).
Expropriate: the time step comes before the money choices (Oracle order). Elrond's secret vote starts with the active player and is
revealed after the last vote. The fellowship and money choices are single-permanent prompts. Logs: the survivors' lines.

**For the next run.** `splits` has 22 rows left, each with its own missing piece (ledger notes). Erestor stays `blocked-other` (it reads
the triggering vote).

### Step 9, fold run 16 (2026-11-24, parallel worktree)

Batch `exiled-store` taken; `card-borrowers` and `kw-borrowers` not taken (16c). Ratchet in its worktree 3,782 / 3,045 → 3,777 / 3,041.
Ledger 17 rows (survivor 3, absorbed 4, split 3, blocked-other 1, `step-9` left 6). 15 card files rewritten (9 only by the store and
enum change); Oracle text checked for 13. CR 607.2a, 613.1f, 708.2, 406.3a, 608.2d, 400.7, 122.1, 702.62a checked.

**Store merge (St).** `GameData.exiledCardCounters` (card id → `CounterType` → count, immutable values) replaces 23 stores (egg, scream,
dream, hit, delay, refine, voyage, silver, ice, blood, croak, void, discovery, collection, fetch, hatching, study, intel, kick, memory,
takeover, brain, cage, stash), accessed through `exiledCardCounterCount`, `exiledCardHasCounter`, `addExiledCardCounters`,
`setExiledCardCounters` and `exiledCardsWithCounter(kind)`. `removeFromExile`, `clearExiledByPermanent` and the token cleanup forget
the whole entry; one copy line replaces 27; `GameDataDeepCopyTest` covers it. `ExiledCardCounter` is gone (any `CounterType`; 14
constants added at the end, no `CONVERSION_VERSION` bump needed). `CardHasCounterInExilePredicate(kind)` replaces the study, memory and
void predicates. Voyage keeps `exiledVoyageControllerIds` as its "is voyaging" marker. Time counters stay apart
(`exiledCardTimeCounters`, `suspendedSpellExiles`, the non-suspend and Alaundo sets).

| Batch | Result |
|---|---|
| `exiled-store` | 7 folded: Mari (`ChooseCardEffect.oneOf(exiledCards(triggeringPlayer(), has HIT))` + `RemoveCountersFromExiledCardsEffect(cardsThisWay(CHOOSE), HIT)` + `ConditionalEffect(removedCounters(), …)`), All Hallow's Eve (remove from itself + `PutExiledCardsIntoOwnersGraveyardEffect(self, no SCREAM)` + `ConditionalEffect(PUT_INTO_GRAVEYARD)`, one `SequenceEffect` under the intervening if), Draugr Necromancer and Grolnok onto `PlayFromAmongExiledCardsEffect`, Karn, Scion of Urza ±1 split, Goliath Daydreamer split. Left: hatching (`blocked-other`, face-down cards the owner may look at, CR 406.3a), egg and Darigaaz (`sourceCard()` in exile), refine and delay (last-counter events), suspend and time adjust (time store) |
| `card-borrowers`, `kw-borrowers` | not taken; `SubjectResolutionService.namesExiledCard` is ready for them |

**Behaviour changes.** A face-down Draugr Necromancer or Grolnok grants no permission (CR 708.2; read through `activeStaticEffects`;
test). Karn's +1 has its controller choose the opponent (it was the first other seat). Leaving exile clears every counter kind (CR
400.7): egg and dream counters had survived `removeFromExile`, about 10 kinds `clearExiledByPermanent`. A lone Mari card is chosen
unasked. Croak cards answer `OptionalInt.of(0)` from `findAdditionalCounterCostFromSource`. Prompts and logs are the survivors'.
`HitCounterExiledCardChoice`, `PendingKarnScionRevealChoice`, `PendingKarnScionExileReturn`, their handlers, projection and AI strategy
are deleted.

**Lesson.** An intervening "if" is checked again before each effect of a triggered ability, so a remove-then-act sequence whose own
removal falsifies the condition must be one `SequenceEffect`.

**For the next run.** 16b: merge time counters into the store, add a generic counters-removed-in-exile event and `sourceCard()` in
exile with its stay, then fold refine, delay, egg, Darigaaz, suspend and time adjust. 16c: card-borrowers and kw-borrowers over
`namesExiledCard`, with the CR 607.2a link (`CardSubject.ExiledWithSource` still ignores `abilityLink`).

### Step 9, fold run 15 (2026-11-24, parallel worktree)

`attach` (23), `unattach` (3), `transform` (7): 18 folded (10 absorbed, 8 split), 3 survivor rows, 7 blocked-other, 7 left `step-9`.
Ratchet in its worktree 3,774 / 3,037 → 3,756 / 3,019. 34 card files rewritten, all checked with `get_card`; rulings for Glamer
Spinners, Simic Guildmage, Heavenly Blademaster, Maddening Hex, Steam Vines, Lynde. CR 301.5c, 301.6, 303.4d, 303.4i, 603.12, 608.2d,
613.7e, 701.3a–d, 702.16c, 702.16d checked. Merged with fold run 8b (`ChoosePermanentsEffect` ends `chooser, forEachVote, attachableTo,
hostFor`; `StepKind` ends `BID, ATTACH`).

**Shape.** `AttachSourceEquipmentToTargetCreatureEffect(thenEffect, optional, targetPredicate, equipment, host, playerHost)` is the
attach verb: the attachment is any `PermanentSubject` (one or a set); the host is one permanent (`source`, `target`, `triggering`,
`triggeringOrSource`, `thisWay(kind)`, `returnedThisResolution`) or a player (`.toPlayer(attachments, player)`). Legality is
`EquipSupport.canAttach` / `canAttachToPlayer`: an Aura must be able to enchant the host (judged with its controller) and is never itself
or a creature (CR 303.4d); an Equipment goes through `canAttachEquipment` (CR 301.5c); a Fortification needs a land, protection checked
(CR 301.6, 702.16d); anything else doesn't move (CR 701.3b). `EquipSupport.attach` does nothing on the host it is already attached to;
otherwise it ends the while-attached effects, gives a new timestamp (CR 613.7e), raises the attach events, and reconciles control. The
verb records `StepKind.ATTACH` ("if you do" = `HappenedThisWay(ATTACH)`, "unattach it" = `thisWay(ATTACH)`).
`ChoosePermanentsEffect.thatCanBeAttachedTo(host)` / `.thatCanHost(attachments)` narrow a choice to what the verb could attach (Glamer,
Simic and Heavenly Blademaster rulings). CR 303.4i stays with `PutCardsOntoBattlefieldEffect.attachedTo`.

| Batch | Result |
|---|---|
| `attach` | 12 folded: the Aura source forms (9 cards), Aura Finesse, Archnemesis (`toPlayer(source(), triggeringPlayer())`), Fortify (2), Codsworth / Halvar, Pre-War Formalwear (`returnedThisResolution()`), Balan / Vulshok (`controlledBy` sets), Kudzu / Steam Vines, Resolute Strike / Vow / Unexpected Request, Winter Soldier / Yuffie, Heavenly Blademaster, Cryptic Coat (cloak records MANIFEST). 11 left: Grip of Phyresis (living weapon builds its record), Thorin (a reflexive sourced at the host), Glamer (no effect declares its target), Simic (no "host of target Aura" subject), Lynde (two CHOOSE results), Maddening Hex (an enchantable-opponent filter), Iron Man / Armored Skyhunter / Palico / Gilgamesh (`picked-subject`), Shifting Shadow (time) |
| `unattach` | `UnattachEquipmentFromTargetPermanentsEffect(PermanentSubject equipment)` (null keeps "all Equipment attached to the targets"); Carry Away and Tamiyo's Compleation use `attached()`; reads effective subtypes. `UnattachEquipmentIfAttachedToControlledCreatureEffect` left (one-user condition) |
| `transform` | `TransformSelfEffect` accepts `granter()`, `thisWay(kind)`, `createdThisResolution()` and transforms each permanent its subject names: Optimus Prime, High Marshal Arguel, Tovolar, Sidequest: Play Blitzball (transform, choose, attach), Soul Seizer. Left: Sunder the Gateway / Nick Fury (`picked-subject`) |

**Behaviour changes.** Fortify on its own land does nothing; protection stops it (new test). Archnemesis does nothing on the player it
already enchants. Codsworth and Halvar raise the Equipment becomes-attached event. A moved control Aura reconciles control at once. An
Aura that is a creature can't attach. Sidequest: Play Blitzball's attach can't be skipped ("transform this enchantment, then attach it
to a creature you control"), a lone creature chosen unasked. Unexpected Request schedules no unattach for an Equipment already on the
creature. Soul Seizer's target spec is benign. Cloak records MANIFEST; TRANSFORM records every transformed permanent.

**For the next run.** A `picked-subject` batch: a `PermanentSubject` naming the resolution's picks (`StackEntry.recordPicks`;
`LookAtTopCardsEffect` follow-ups, `PutCardToBattlefieldThenEffect`) folds Nick Fury, Armored Skyhunter, Palico, Gilgamesh (with its
reflexive), The Invincible Iron Man and Sunder the Gateway. Then a living-weapon run folding Grip of Phyresis with
`LivingWeaponEffectHandler`'s source form.

### Step 9, fold run 12d (2026-11-24, parallel worktree)

Rows: what 12c left in `dup-hand` and `perp-hidden-sets` (Flames, Aquatic, Vona, Vesuvan/Warzone, Gutmorn), plus Choose Your Champion
from `splits`. Ratchet in its worktree 3,764 / 3,027 → 3,759 / 3,022. Ledger 11 rows (split 5, absorbed 1, survivor 2, unique 2,
blocked-step-model 1). 7 card files rewritten and one handler fixed; Oracle text checked for all 8. CR 608.2h, 707.2, 702.74a, 609.4b,
400.7 checked.

| Batch | Result |
|---|---|
| perpetual alternate cast (Flames, Aquatic) | 2 folded. New step `PerpetuallyGrantAlternateCastEffect` (`.evoke`, `.ratherThanManaCost`) writes the one store `GameData.perpetualAlternateCasts` (`GrantedAlternateCast`); the evoke store is merged into it (deep-copy test). Flames of Moradin: `DestroyEachTargetPermanentEffect(artifact)` + `duplicateOf(departedThisWay(DESTROY))` with the end-step sacrifice as `withAbilities` + `ratherThanManaCost({R})` |
| Vona / any type | Vona split: destroy + `MayEffect(Sequence(DiscardEffect, ConditionalEffect(discarded(), duplicateOf(targetDeclaredElsewhere()).withAbilities(StaticAbility.of(PerpetualAnyManaTypeToCastSelfEffect)))))`. Nantuko Slicer's handler now gives prismatic. No any-type step: it would have one user |
| last-known copy (Vesuvan, Warzone) | 1 record split: Vesuvan Mist uses `departedThisWay(RETURN)` (CR 608.2h); Warzone Duplicator `targetDeclaredElsewhere()` |
| chooser (Gutmorn, Choose Your Champion) | 2 split onto `ChoosePlayerEffect.chosenBy(...)`. Gutmorn: conjure `.conjuredBy(playersThisWay(CHOOSE))` + prismatic. Choose Your Champion: `GrantStaticEffectToPlayerEffect(PlayerCantCastSpellsAndAttackWithCreaturesEffect, except(eachPlayer(), allOf(you(), playersThisWay(CHOOSE))), UNTIL_YOUR_NEXT_TURN)`; both choice contexts gone |

**Behaviour changes.** Vona's duplicate gets "mana of any type" (it had any colour), and its conjure is in the same resolution (it was
a reflexive ability); failing-first test, and its old any-colour assertion, which contradicted the Oracle text, now checks any type.
Nantuko Slicer's duplicate gets prismatic (it had any type); failing-first test. Gutmorn duplicates a discarded card that left its
graveyard as last known. Warzone, Vesuvan and Flames copy last-known copiable values (a face-down original gives a nameless 2/2, CR
707.2, 708.2a). Flames' {R} is in the store, not the card's printed options; the AI's filter for unpayable alternative costs reads the
store. `SpellCastingService` adds evoke's sacrifice trigger only for an evoke grant (CR 702.74a). Logs are the survivors' lines; Aquatic
Subtlety's grant logs one line naming no card. "It" across zone changes (CR 400.7) keeps today's behaviour.

**For the next run.** The `dup-hand` leftovers (Agent of Raffine: a top-of-library subject; Snowborn: a face-down duplicate; Ace,
Viconia, Grizzled Huntmaster). `ChooseCardFromHandAndApplyPerpetualBlitzEffect` could move its blitz onto
`PerpetuallyGrantAlternateCastEffect` if a second user appears.

### Step 9, fold run 17 (2026-11-24, parallel worktree)

Batches `copier` and part of `copy-exc`; `cost-paid-copy` and `copy-on-enter` not reached (17b). Ratchet in its worktree 3,764 / 3,027
→ 3,752 / 3,014. Ledger 20 rows (absorbed 9, split 3, survivor 2, blocked-other 4, `step-9` 2). 16 card files rewritten, Oracle text
checked for all; rulings for Infinite Reflection, Permeating Mass, Brudiclad, Blade of Shared Souls. CR 406.3a, 707.2, 707.3, 707.9b,
608.2b, 608.2d, 608.2h, 611.2b, 613.2a, 613.7, 708.2a checked.

**The shape.** `BecomeCopyOfSubjectEffect(card | permanent, copier, duration, keepsThisAbility, exceptions)`: `.by(PermanentSubject)`
sets the copier (default `source()`; the set found once, a copier that left not affected); `.lasting(PERMANENT | UNTIL_END_OF_TURN |
UNTIL_YOUR_NEXT_TURN | WHILE_REMAINS_ATTACHED)` (the attached duration does nothing when the source isn't attached to the copier, CR
611.2b); `.except(CopyExceptions)` (`withKeywords`, `stillA`) applies before the layer-1 float, so the exceptions are copiable (CR
707.9b); `keepingThisAbility` for the source copier only; a benign permanent target is declared when the copier or the copied permanent
is a target. The copied value is computed once before any copier changes: a live permanent gives `copiableCard`, a departed one
`PermanentCopierService.lastKnownCopiableCard` (its departure snapshot through `copiableCard`, so face-down values, CR 708.2a, and
copies of copies, CR 707.3, hold).

| Batch | Result |
|---|---|
| copier | 10 folded (7 absorbed, 3 split): Permeating Mass, Flood of Mars, Fleeting Reflection, Shapesharer, True Polymorph, Prime Minister's Cabinet Room, Cytoshape, Polymorphous Rush, Deceiver of Form, Brudiclad, Infinite Reflection, Blade of Shared Souls, Reflection Net, Loose in the Park. Gone: `PolymorphousRushCreatureChoice`, `BrudicladTokenChoice`, `AuraCopyService.applyEquippedCreatureCopy`. Left: The Animus (fold run 16's store and a targeting card subject); Myriad Pools `blocked-other` (no card subject for a spell on the stack); the Metamorphic Alteration pair `blocked-other` (a static copy); Aegis/Headdress not reached; Kaya `blocked-other` (`TriggeringCards` finds only one card of a one-or-more exile batch) |
| copy-exc | 2 folded: Vesuvan Drifter (`withKeywords(FLYING)`), Nascent Metamorph (split onto `RevealCardsFromTopUntilEffect`). Not reached: Lazav ×2, Likeness Looter, Mimeoplasm, Taskmaster (a targeting card subject), `BecomeCopyOfCardUntilEndOfTurnEffect`, Spirit of Resilience and `BecomeCopyOfOneOfCardsUntilEndOfTurnEffect` (event cards as last known) |

**Behaviour changes.** Infinite Reflection copies the creature it entered attached to (ruling; failing-first). A departed permanent is
copied without its non-copy effects (CR 707.2; Identity Thief with Arcane Flight, failing-first). Metamorphic Alteration's choice drops
creatures that can't be chosen (CR 608.2d). Permeating Mass no longer declares a target. Nascent Metamorph's opponent target is
harmful. Loose in the Park reads the enchanted land as last known once the Aura has left (CR 608.2h). The survivor's log line wins.

**For the next run (17b).** A targeting card subject on the survivor (Lazav, Likeness Looter, Mimeoplasm, Taskmaster, then The Animus
over fold run 16's store); an event-cards-as-last-known subject (Spirit of Resilience, Lazav Dimir Mastermind, Kaya: three users); then
`cost-paid-copy`, `copy-on-enter`, Aegis/Headdress.

### Step 9, fold run 16c (2026-11-24, parallel worktree)

`card-borrowers` and `kw-borrowers`: 8 folded, 2 left. Ratchet in its worktree 3,754 / 3,018 → 3,746 / 3,011. Ledger 12 rows (survivor
2, absorbed 8, blocked-other 2). 18 card files rewritten, Oracle text checked for all. CR 607.1, 607.2a, 607.3, 607.5, 613.1f, 708.2,
406.3a, 702.66a, 702.11d, 702.16a, 108.2b, 702.167c checked.

**The shape.** `SubjectResolutionService.namedCards(gameData, source, controllerId, cards, reads)` is the static list form of a card
subject (`InExile` and `ExiledWithSource` judged by `namesExiledCard`; `InGraveyards` by `namesPlayer`, in turn order, no token;
`TopmostInLibrary`; `ExiledAsCost` as the source's delve cards wherever they are now); it never re-enters the layered pass.
`CardSubject.ExiledWithSource(owners, filter, abilityLink)`: with a link, only the cards that ability exiled (CR 607.2a), honoured by
the resolving form too (null keeps "every card"); `exiledWithSourceBy(link, filter)`. `GainActivatedAbilitiesOfExiledCardsEffect(CardSubject
cards, CardPredicate filter, boolean oncePerTurn)` and `GainKeywordsOfCreatureCardsExiledWithSourceEffect(cards, cardFilter,
watchedKeywords, copiesProtection, recipient)` (`COMMON_KEYWORDS`) share one lending check: a face-down exiled card lends nothing (CR
406.3a), nor a graveyard card while graveyard cards have lost all abilities; "hexproof from" lends with hexproof (CR 702.11d); every
self-scoped protection lends when protection is listed. Layer 6 (CR 613.1f); a source that lost its abilities or is face down gives none
(CR 708.2).

| Batch | Result |
|---|---|
| `card-borrowers` | 5 folded (all-graveyards, controller-graveyard, top-library, brain, cage). Idris and Koh `blocked-other` (Koh's "last chosen card" subject and Idris's triggered-abilities option would each have one user) |
| `kw-borrowers` | 3 folded (Urborg Scavengers / Wretched Bonemass, Eater of Virtue, Cairn Wanderer) |

**Behaviour changes.** Patchwork Crawler no longer has the abilities of a card it exiled with a gained Dark Impostor ability (CR
607.2a, 607.5; failing-first). Urborg Scavengers and Wretched Bonemass gain "hexproof from" (test). Cairn Wanderer gains every
protection, per its ruling (test with Tel-Jilad Chosen). Soulflayer reads only its delve cards. Face-down exiled cards lend nothing.
Merged with fold run 17, which had added the same `Subjects.exiledWithSourceMatching(filter)`: one factory kept (fold run 17's Javadoc, the link-aware constructor).

**For the next run.** Give the linkless printed exiles of Myr Welder, Territory Forge and Idris a link, and rewrite gained readers
(`ReturnCardExiledWithSourceToBattlefieldEffect`, Valki's copy) onto the gained link as Koh does; Valki's copy can take
`exiledWithSourceBy`.

### Step 9, fold run 18 (2026-11-24, parallel worktree)

Batches `free-cast` and `hand-free-cast` taken; `gy-cast-now`, `exile-markers`, `look-cast` not taken (18b). Ratchet in its worktree
3,754 / 3,018 → 3,747 / 3,010. Ledger 12 rows (survivor 2, absorbed 6, split 2, blocked-other 2). 9 card files rewritten, Oracle text
checked for 9. CR 608.2g, 608.2h, 601.3, 118.9, 118.9a, 118.8, 709.3a, 716.3, 716.3a, 406.3a checked.

**Record shape.** `CastCardsWithoutPayingManaCostEffect(CardSubject cards, int maxCount, DynamicAmount totalManaValue)` (`anyNumber(cards)`,
`upTo(n, cards)`, `.withTotalManaValueAtMost(amount)`): it offers the face-up spell cards in exile the subject names (a land can't be
cast; a face-down card has no characteristics, CR 406.3a); one `ImprovisationCapstoneCastChoice` answers the spells and their order
(CR 608.2g); `ExileFreeCastQueueSupport` casts them, asking `castableFromExile` (`canCastDuringResolution`) first; additional costs
are paid (CR 118.8), the free cast being the alternative cost (CR 118.9, 118.9a); the total cap is evaluated once (CR 608.2h), each
split card counting its half (CR 709.3a); a card not cast stays where it is. No caster component yet (Allure of the Unknown and
Possibility Storm would be its two users, both waiting on other parts).

| Batch | Result |
|---|---|
| `free-cast` | 5 folded: Izzet Chemister, Forger's Foundry (`exiledWithSource()`), Rod of Absorption (`XValue` cap), Ashiok (`upTo(3, exiledCards(eachOpponent(), null))`), Improvisation Capstone and Etali split onto the exile steps + `cardsThisWay(EXILE)`. Invoke Calamity's two records `blocked-other` (hand-and-graveyard casts under one cap) |
| `hand-free-cast` | 3 folded into `MayCastAnySpellFromHandWithoutPayingManaCostEffect.maxCount` (`anyNumber()`, `upTo(n)`); each offer carries `MayCastFromHandWithoutPayingManaCostEffect.nextOffers`, inserted into the parked resolution when a cast begins, so a declined card is offered again (CR 608.2g) |

**Behaviour changes.** Improvisation Capstone offers battle cards (CR 601.3; test). Aetherflux Conduit and The Great Synthesis offer
a declined card again after each cast, so the caster picks the order (CR 608.2g; test). Power Without Equal offers a card castable only
from a graveyard (refused when accepted, as before). Face-down exiled cards are never offered. Etali's and Capstone's logs are the
survivors' lines.

**For the next run (18b).** `gy-cast-now`: `CastCardFromGraveyardEffect` with a `CardSubject` (`sourceCard()`, `triggeringCards(nonland)`,
`targetCard`), and Chandra Ablaze's any-number graveyard record onto the ordered choice. `look-cast`: one `LookAtTopCardsAndMayCastEffect`
with a `DynamicAmount` count, a reveal flag and one place for the looked-at cards to wait (Kiora keeps them in exile, paying additional
costs; Aetherworks leaves them in the library; Sunbird uses a split-half cap). `exile-markers`: suspend's haste "until you lose
control" (CR 702.62a) on `MayPlayExiledCardWithoutPayingManaCostEffect`. Invoke Calamity: a hand-and-graveyard source on the cast-now
queue.

### Step 9, fold run 16b (2026-11-24, parallel worktree)

Batch `exiled-store`: the 6 members fold run 16 left. Ratchet in its worktree 3,754 / 3,018 → 3,750 / 3,014. Ledger 8 rows (absorbed 3,
split 1, unique 1, survivor 2, `step-9` 1). 6 card files rewritten; Oracle text checked for Dust of Moments, Talon Gates, Ertai's
Meddling, Uvilda and both Darigaaz. CR 702.62a, 702.62b, 122.1, 400.7, 603.4, 608.2h, 708.2, 110.2a, 707.10 checked.

**Time-counter merge (St).** `exiledCardTimeCounters`, `suspendedSpellExiles` and `GameData.SuspendedSpellExile` are gone: time
counters are `exiledCardCounters` TIME. "Suspended" has one rule, `GameData.isSuspended` (CR 702.62b: in exile, has suspend, has a time
counter), over `exiledCardHasSuspend`: a card has suspend when it prints it or gained it; every card exiled with time counters gains it
unless the non-suspend set marks it (only Alaundo's exile). Every reader asks the rule; StepTriggerService's separate loop for exiled
spells is gone. `GameDataDeepCopyTest` covers TIME.

| Member | Result |
|---|---|
| RemoveSuspendCounterFromExiledSpellEffect | absorbed into `RemoveTimeCounterFromExiledCardEffect` |
| AdjustTimeCountersOnEachSuspendedCardEffect | absorbed: remove / put step over `exiledCards([you(),] CardIsSuspendedPredicate)`, TIME, 2 |
| RemoveRefineCounterFromExiledCardEffect | absorbed: `RemoveCountersFromExiledCardsEffect(sourceCard(), REFINE)` + the last-refine event (`COUNTER_REMOVED_IN_EXILE`) |
| RemoveEggCounterFromExileAndReturnEffect | split: remove from `sourceCard()` + `ConditionalEffect(NotCondition(SourceCardInExileWithCounter(EGG)), PutCardsOntoBattlefieldEffect(sourceCard()))`; Darigaaz Reincarnated prints it |
| ConjureDarigaazShivanChampionSpellbookEffect | left: needs a face-down exile `ConjureDestination` and 13 printings |
| RemoveDelayCounterFromExiledSpellEffect | unique: one ability with an "if"; putting the original spell back (CR 707.10) has no second user |

**Behaviour changes.** Spells that gained suspend (Delay, Rory Williams, Taigam, self-exiling spells such as Arc Blade) raise the
removed-in-exile event on every time counter, so a delayed Aeon Chronicler draws (CR 702.62a; new test); they count as suspended for
every reader and their upkeep trigger is ordered with the other upkeep triggers. A card that prints suspend is suspended even when
marked non-suspend (CR 702.62b; Fury Charm's test moved from Ancestral Vision to Grizzly Bears to keep its "not suspended" card).
`SuspendedCards` no longer counts Alaundo's cards without suspend. Darigaaz Reincarnated checks that it hasn't left exile since (CR
400.7; new test). Logs use the survivors' lines. 110 test files rewritten by script onto the store's accessors.

**For the next run.** Darigaaz's conjure waits for a face-down exile conjure destination. `SourceCardInExile`'s Javadoc says "face-up"
but the code doesn't check it; the refine and suspend upkeep abilities are still built from the counters rather than granted.

### Step 9, fold run 15b (2026-11-24, parallel worktree)

`picked-subject` (6 rows): 5 folded (4 absorbed, 1 split), 1 blocked-other (Gilgamesh), plus 2 survivor rows. Ratchet in its worktree
3,731 / 2,995 → 3,726 / 2,991. 5 card files rewritten plus Vault 101's engine follow-up; Oracle text checked for all 5. CR 603.12,
608.2c, 608.2d, 701.27a, 701.27d, 701.27e, 702.92a checked.

**Shape.** `PermanentSubject.Picked(PermanentPredicate filter)` (`Subjects.picked()` / `picked(filter)`): the permanents the entry last
recorded as its picks (`StackEntry.recordPicks`), each once, still on the battlefield as the same objects, narrowed by `filter`; no
target, a set. Writers: the library follow-ups, Vault 101's search follow-up, and `PutCardToBattlefieldThenEffect`'s then-effect path.
Readers: `TransformSelfEffect`, the attach verb's attachment, `ChoosePermanentsEffect.thatCanHost`. A `MayEffect` whose leading step reads
picks that name nothing asks nothing (`MayEffectHandler.picksNameNothing`). Helpers: `LibrarySelectionFollowUp.inResolution(effect)`
(the next step of the same resolution) and `AttachSourceEquipmentToTargetCreatureEffect.pickedEquipmentToCreatureYouControl()`.

| Batch | Result |
|---|---|
| `picked-subject` | The Invincible Iron Man: `AttachSourceEquipmentToTargetCreatureEffect(picked(), source())`. Armored Skyhunter, Palico: `inResolution(MayEffect(pickedEquipmentToCreatureYouControl()))` (Vault 101's follow-up builds the same). Nick Fury: `inResolution(MayEffect(TransformSelfEffect(picked(PermanentIsDoubleFacedPredicate))))`. Sunder the Gateway: `ChoosePermanentsEffect.exactly(1, controlledBy(you(), Incubator token))` + `TransformSelfEffect(thisWay(CHOOSE))`. Left: Gilgamesh `blocked-other` (two choices in one resolution, a Samurai then an Equipment; one CHOOSE outcome is kept per kind, and `thatCanHost` needs every attachment to fit; Lynde waits on the same gap). Living weapon / Grip of Phyresis not taken |

**Behaviour changes.** Armored Skyhunter's and Palico's "If an Equipment is put onto the battlefield this way, you may attach it" is
asked inside the attack trigger's resolution, not as a reflexive ability on the stack: the text says "if", not "when" (CR 603.12, 608.2c;
failing-first tests). Logs are the survivors' lines ("X chooses Y.", "is now attached to"). Nick Fury's gate is the Oracle's
"double-faced card" (CR 701.27a); the old "back face is a permanent" check is the verb's own no-op (CR 701.27e).

**For the next run.** A two-choice run for Gilgamesh and Lynde (a second CHOOSE result, or an any-of `thatCanHost` plus a one-of
attach). A living-weapon run folding Grip of Phyresis with `LivingWeaponEffectHandler`'s source form (58 cards; decide first whether
every living weapon logs a CHOOSE line and sets a chosen permanent on the Equipment). `MakeChosenPermanentAttackingEffect.pickedPermanent()`
and `GrantKeywordToChosenCreatureUntilEndOfTurnEffect` could read `picked()`.

### Step 9, fold run 17b (2026-11-24, parallel worktree)

Fold run 17's leftovers in `copy-exc` and `copier`. Ratchet in its worktree 3,719 / 2,982 → 3,714 / 2,977. Ledger 9 rows (absorbed 5,
survivor 1, blocked-other 3). 6 card files rewritten, Oracle text checked for all; rulings for Lazav ×2, Taskmaster, Kaya, Spirit of
Resilience, 'Dane, Curie. CR 205.1a, 406.3a, 608.2b, 608.2d, 608.2h, 707.2, 707.3, 707.9a–d checked.

**The shape.** `BecomeCopyOfSubjectEffect` accepts a target card subject (`targetCard`, `targetExiledCard`, `targetCardExiledWithSource`),
its `targetSpec` from the subject alone or joined with the permanent target; `ofCreatureOrCard(card)` is one target, a creature or that
card, copied whichever it is. The copied card is read through `SubjectResolutionService.cardsToDuplicate`, so an event card that left
its zone is copied as it last existed (CR 608.2h). `CopyExceptions` gains `named`, `alsoLegendary`, `withBasePowerToughness` (a set P/T
drops a P/T-defining ability, CR 707.9d) and `alsoACreatureOfTypes` (card and creature types replaced, CR 205.1a); all exceptions are
copiable values (CR 707.9b). Every copy resets the copier's per-turn activation counts (new ability instances; Lazav's ruling).

| Batch | Result |
|---|---|
| copy-exc | 5 folded: Lazav, the Multifarious; Likeness Looter; Mimeoplasm, Revered One; Taskmaster; Lazav, Dimir Mastermind (over `triggeringCards()`). Left: Spirit of Resilience's pair `blocked-other` (a choice among event cards that left the graveyard). `BecomeCopyOfCardUntilEndOfTurnEffect` stays (still built by the unaudited exiled-with-source handler and Spirit's) |
| copier | The Animus folded (`targetExiledCard(allOf(creature, CardHasCounterInExilePredicate(MEMORY)))` + `.by(target(0))`). Kaya `blocked-other` (no per-card exile stay for a one-or-more exile batch); its target defect fixed. Aegis/Headdress not reached |
| cost-paid-copy | not reached: a paid-as-cost `PermanentSubject` (read by `lastKnownCopiableCard` too), Curie's added-ability exception (one user), and 'Dane's rule of not adding its kept ability twice |
| copy-on-enter | not reached (`CloneService` / `MayCopyHandlerService` path) |

**Behaviour changes.** Taskmaster copies a face-down target by its face-down values and doesn't log its name (CR 707.2, 708.2a; new
test). Kaya's exile trigger targets only a token you control, as its text says (new test). Every survivor copy resets per-turn
activation counts. `cardsToDuplicate(TriggeringCards)` reads a single-card graveyard event's card as it last existed. The survivor's log
line wins.

**For the next run.** Kaya: a per-card exile-stay map on `StackEntry` (simulation-copied, deep-copy test), then `ChooseCardEffect.upTo(1,
triggeringCards(creature))` + the survivor over `cardsThisWay(CHOOSE)`. Spirit: a CHOOSE over last-known event cards. Then
`cost-paid-copy` and `copy-on-enter`. `BecomeCopyOfTargetPermanentUntilYourNextTurnEffect` (Absorbing Man, Crystalline Resonance,
Volrath; unaudited) can fold onto `CopyExceptions`.

### Step 9, fold run 19 (2026-11-24, parallel worktree)

Batches `ring`, `undying`, `reselect`, `attached-copy-token`, `cant-be-countered`; `opp-mode` and `tempting` left (19b). Ratchet in its
worktree 3,731 / 2,995 → 3,724 / 2,992. Ledger 13 rows (absorbed 9, survivor 4). 14 card files rewritten, Oracle text checked for all.
CR 701.54a, 701.54c, 701.54d, 701.54e, 702.79a, 702.93a, 400.7, 700.2e, 708.2, 613.1f checked.

| Batch | Result |
|---|---|
| ring | `TemptTheRingEffect` into `RingTemptsYouEffect`. One store `GameData.ringStates` (`RingState`; `ringLevel`, `ringBearerId`, `temptByRing`, `setRingBearer`); `ringLevels`, `ringBearerIds` and `TemptTheRingBearerChoice` deleted. The tempt logs, raises `TEMPTED_BY_RING` and, when a creature was chosen, `CHOOSES_RING_BEARER`. Deep-copy test |
| undying | `UndyingReturnEffect` into `PersistReturnEffect(CounterType)` / `.undying()`, with the stay check (CR 400.7). +1/+1 placement stays on `doublePlusOnePlusOneCounters` (its can't-have check); -1/-1 on `replaceCounters` |
| reselect | `ReselectAttackingCreatureEffect` into `ReselectAttackingCreatureAttackTargetEffect(PermanentSubject attacker, boolean playersOnly)` (`target()` for Portal Mage and Signpost, `source()` for Capricopian); one reselection query and completion |
| attached-copy-token | Mirrormind Crown's and Moonlit Meditation's records into the new marker `FirstTokenCreationCopiesAttachedPermanentEffect`; one offer `EsixTokenReplacementSupport.offerAttachedCopy` over `activeStaticEffects` |
| cant-be-countered | `SpellsCantBeCounteredEffect`, `CreatureSpellsCantBeCounteredEffect` and `ControllerCreatureSpellsCantBeCounteredEffect` into `ControllerSpellsCantBeCounteredEffect` with a `PlayerSubject players` (`you()` default, `eachPlayer()`); Spellbreaker Behemoth uses `CardPowerAtLeastPredicate(5)`; one reader `spellsCantBeCounteredProtects` over active static abilities |

**Behaviour changes.** The Ring is stored once (the queue's most serious defect): Slip On the Ring triggers "whenever the Ring tempts
you", logs the temptation and shares the Ring with the other tempt cards (CR 701.54c, 701.54d); `RingTemptsYouEffect` cards raise the
bearer-chosen event (Gandalf, Friend of the Shire draws on any tempt). A tempt with no choosable creature keeps the bearer (CR
701.54a). Ring-bearers can be sacrificed again: `cantBeSacrificed` had a Ring-bearer ban no rule supports. Karn's restart clears every
Ring. Undying checks the stay. Portal Mage may reselect a planeswalker that can't be attacked (ruling). Moonlit Meditation's copies of
tapped tokens enter tapped (ruling); it and Mirrormind Crown do nothing once they've lost their abilities (CR 613.1f). A face-down or
ability-less can't-be-countered permanent protects nothing (CR 708.2, 613.1f). New test for each.

**Found.** The Ring's second, third and fourth abilities (loot on attack, the blocker sacrificed at end of combat, each opponent
losing 3) aren't implemented (CR 701.54c): `missing-features-to-do.md` entry 82.

**For the next run (19b).** `opp-mode`: an opponent chooser on `ChooseOneEffect` at cast (CR 700.2e: the controller picks which
opponent chooses), with `FatalLoreEffect` and the targets-after-mode-choice context, replacing `SpellCastingService.queueOpponentModeChoice`.
`tempting`: `TemptingOfferEffect(payload)`, all opponents deciding in turn order, then the payloads (C13 ruling).

### Step 9, fold run 18b (2026-11-24, parallel worktree)

Batches: `gy-cast-now` taken; `exile-markers` given a verdict; `look-cast` not taken. Ratchet in its worktree 3,700 / 2,963 → 3,693 / 2,952. Ledger 11 rows (absorbed 6, split 1, unique 2, survivor 2). 8 card files rewritten, Oracle text checked for 9. CR 608.2g, 601.2f, 118.8, 118.9, 118.9a, 716.3a, 608.2b, 608.2d, 400.7, 702.62a, 709.3a, 305.2, 305.3 checked.

**Record shape.** `CastCardFromGraveyardEffect` gains `CardSubject cards`, plus the engine-built `choiceGroupId` and `nextOffers`. `CastCardFromGraveyardEffect.of(cards)` takes `sourceCard()`, `triggeringCards(filter)` or `targetCard(filter, scope)`; the target subject declares the target, and its scope becomes the record's scope. Withers: `.withoutPayingItsManaCost()`, `.spendingManaOfAnyType()`, `.exilingInsteadOfGraveyard()`, `.allowingAdventure()`, `.ifCast(effect)`. Each named card is offered with a "may". Several named cards form one choice group: accepting one withdraws the others. The legacy filter-and-scope search is unchanged when `cards` is null.

`CastCardsWithoutPayingManaCostEffect` also accepts graveyard cards, alone and with no total cap. Each round offers every matching card as a `CastCardFromGraveyardEffect` offer in one group. Once a cast begins, the next round is inserted, bounded by this round's offers, so a cast that fails can't loop. The exile queue (`ExileFreeCastQueueSupport`) is exile-bound, so the graveyard branch reuses the graveyard cast flow instead.

| Batch | Result |
|---|---|
| `gy-cast-now` | 7 folded. Oskar (`triggeringCards(nonland)`, Adventure allowed), Sproutback (`sourceCard()`), Thunderblade (`sourceCard()` free, inside its MayPayManaEffect; cost-shape payment unchanged), Tinybones (`targetCard(…, OPPONENT_GRAVEYARD)`, any mana type), Vadrok (`targetCard`, free) and Victor (`targetCard`, free, exile instead) are absorbed. Conduit is split: `ConditionalEffect(NotCondition(ControllerCastSpellThisTurn(null)), ….ifCast(PlayerRestrictionEffect.thisTurn(you(), castingSpells())))`. Chandra moves onto `CastCardsWithoutPayingManaCostEffect.anyNumber(cardsInGraveyards(you(), filter))`. 1 left: `PlayTargetCardFromGraveyardWithoutPayingManaCostEffect` is unique, kept for Horde's land play and two engine builders |
| `exile-markers` | `CastExiledCardWithoutPayingManaCostEffect` is unique. It is a stack-resolved cast step naming its card (suspend's trigger wraps it in a MayEffect; Cruelclaw's paid discard runs it with no second ask). The survivor is a PendingMayAbility marker, so folding would only swap shapes |
| `look-cast` | not taken: the looked-at cards wait in three places (taken out of the library, left in it, Kiora's exile) and Sunbird's split-half cap needs its own run |

**Behaviour changes.** Free survivor casts pay cost increases (CR 601.2f): Spectral Arcanist tests. A Phyrexian life payment is recorded on the survivor's spell, so compleated loyalty is right. Chandra's order is the caster's and a declined card is offered again (CR 608.2g; test). Thunderblade's graveyard stay was already held by the stay-aware `SourceCardInGraveyard`; `sourceCard()` keeps it (regression test). Moved cards get the survivor's checks: additional costs paid on free casts (CR 118.8), X, modes and Adventure. Prompts and logs are the survivor's.

**Seen, not changed.**
- Horde of Notions plays a land from its graveyard without checking the turn or a remaining land play (CR 305.2, 305.3). Fairly sure. Untestable without an Elemental land card.
- `MayCastDiscardedCardWithoutPayingManaCostEffect(CardSubject)` is a second graveyard/exile cast-now record over a card subject: a step-10 merge candidate with the survivor.

**For the next run (18c).** `look-cast`: one `LookAtTopCardsAndMayCastEffect` with a `DynamicAmount` count, a reveal flag and one place for the looked-at cards to wait (Aetherworks leaves them in the library, Velomachus and Cosmic Cube take them out, Kiora exiles them). Read `LibrarySearch CAST_WITHOUT_PAYING`'s completion first, to see how it treats both placements.

### Step 9, fold run 19b (2026-11-24, parallel worktree)

Batch `opp-mode` done; `tempting` not taken (19c). Ratchet 3,700 / 2,963 → 3,697 / 2,957. Ledger 11 rows (absorbed 3, survivor 1, `step-9` 7). 3 card files rewritten, Oracle text checked for 3; rulings for Fatal Lore and the tempting offers. CR 700.2e, 700.2a, 601.2b, 601.2c, 608.2d, 101.4 checked.

**Shape (opp-mode).** `ChooseOneEffect.anOpponentChooses(options)` (`ModeChooser.AN_OPPONENT`). The caster announces no mode, so every cast, target, view and AI site that looks for the caster's modal skips it (`ChooseOneEffect.isCastersModal`, `chosenByCaster`). The spell goes on the stack with the modal unresolved. Before priority, `StackedModalTriggerService.announceNextMode` does three things:
- the controller picks the opponent when there are several (`PermanentChoiceContext.ModeChoosingOpponent`); a lone opponent is chosen unasked;
- that opponent picks among the modes that can be chosen (`StackedModalTriggerModeChoice`; a mode with fewer legal targets than its minimum is left out, CR 700.2a);
- the controller picks the mode's targets among that player's permanents (`TargetsAfterOpponentModeChoice(entry, chooser, targetFilter)`, the option's min/max).

The opponent is kept on `StackEntry.modeChooserId`, read by `Subjects.modeChooser()` ("that player") and `DrawUpToRecipient.MODE_CHOOSER`.

| Member | Result |
|---|---|
| LibraryOfLatNamEffect | absorbed: [RegisterDrawCardsAtNextUpkeepEffect(3)] / [SearchLibraryEffect()] |
| MisfortuneEffect | absorbed: [+1/+1 on each creature you control, gain 4] / [PutCounterOnTargetPermanentEffect(controlledBy(modeChooser(), creature), -1/-1) + DealDamageToPlayersEffect(4, modeChooser())] |
| FatalLoreEffect | absorbed: [DrawCardEffect(3)] / option(DestroyEachTargetPermanentEffect(true) + DrawUpToNCardsEffect(3, MODE_CHOOSER), creature, 0..2) |
| `tempting` (7) | left for 19c, design below |

**Behaviour changes.**
- Misfortune's second mode hits only the choosing opponent (Oracle "that player"; new test).
- Fatal Lore's targets and draw belong to the choosing opponent (new test).
- The controller picks the choosing opponent among several (CR 700.2e; new test).
- The prompt is a mode list instead of accept/decline, so five card tests answer with `handleListChoice`. Logs are the survivor's.
- The 3 records, 3 normalfx handlers, 3 mayfx handlers, `SpellCastingService.queueOpponentModeChoice` and three `MayPenaltyChoiceHandlerService` branches are gone.

**Tempting design (19c).** The C13 rulings say opponents decide in turn order, then the effect happens at once for each acceptor, then for you once per acceptor. Shape:
1. An each-opponent-may accept step, asked in turn order from the opponent after you (`apnapOpponents`), that records `StackEntry.acceptedPlayerIds`.
2. The payload over `acceptedPlayers()`, done once for all of them.
3. `RepeatEffect(accepted count, payload over you())`.

Payloads:
- Glory: counters over `controlledBy(acceptedPlayers(), creature)`.
- Bunnies: `DrawCardEffect(1, acceptedPlayers())` + a token.
- Discovery: `EachPlayerSearchesLibraryEffect` without shuffling, then the shuffle step for the players who searched; count only opponents who searched.
- Immortality: a per-player graveyard return; count only opponents who returned one.

Blockers found:
- Vengeance, Contract, Bunnies and Reflections need a `PlayerSubject` token controller on `CreateTokenEffect` / the token copies (the open token-controller cause).
- Mayhem needs a player form of the spell copy.
- `ResolveRiderForPlayerEffect` is not a fit: its derived entry drops X and can't pause for a prompt.

**Seen, not changed.** A free cast that skips `finishSpellCast` (exile and library free casts) should now get its mode announced at the next auto-pass; not tested.

**For the next run.** 19c builds the token-controller subject first, then folds `tempting` as above; the Fold-line defect "Tempt with Glory, Bunnies, Mayhem, Immortality act as each opponent accepts" is still open.

### Step 9, fold run 20 (2026-11-24, parallel worktree)

`seek-step`, `three-way`, `two-picks`: 20 folded (6 absorbed, 14 split), 6 left, 1 re-batched. Ratchet 3,711 / 2,975 → 3,691 / 2,955. Ledger 29 rows (survivor 2, absorbed 6, split 14, blocked-other 5, unique 1, step-9 1). 23 card files rewritten, Oracle text checked for all; CR 400.2, 608.2h, 701.24c, 701.24d checked.

**The shape.** `SeekLibraryEffect` is the SEEK step.
- It records `StepKind.SEEK` (no new kind): the sought cards with a `MovedCard` in the hand, graveyard or exile each went to (a replacement's zone, bound to the stay), the permanent each became on the battlefield, and the seekers as players. It records NONE first.
- New: `GRAVEYARD` and `EXILE` destinations (`exiledPlayableUntilNextTurn`), `.fromAmongTop(n)` and `.by(PlayerSubject seeker)` (the new `Subjects.ownersOf(CardSubject)`).
- The filter goes through `bindCardSubjects` and `bindAmounts`, so "greater/lesser mana value than the discarded card" is `CardNotPredicate(CardManaValueAtMostAmountPredicate(x))` / `CardManaValueAtMostAmountPredicate(Sum(x, Fixed(-1)))`; no new ManaValueBound relation.
- A seek passes through the hand; `randomWithoutSeeking` doesn't, and records SEEK too.
- `CardsThisWay(SEEK).inHiddenZone()` is true, so "choose one of them" is a hidden hand choice.
- `ChooseOneAtResolutionEffect.secretly(...)`: "secretly choose land or nonland".

| Batch | Result |
|---|---|
| `seek-step` | 20 folded: land/nonland (2, onto `ChooseOneAtResolutionEffect`), graveyard, top-N, manifest, the two mana-value relations, Sarevok, both owner seeks, Choice of Fortunes, the three seek-then-choose, the six perpetual. Left: Karlach's blue (a free-cast grant bound to a card subject: one user; sought event added), Plunderer's Prize (no card subject for the spell on the stack, no this-spell return step), Song of Seasons (one seek of three filters: one user) |
| `three-way` | 0: Expressive Iteration's prompt has its own client flow (handBottomExile), so folding it needs frontend work; Write into Being's "top or bottom" is the player's choice (`unique`; the triage note was wrong) |
| `two-picks` | 0: Wakanda Forever! would need an entry-counter component only it sets; re-batched to `splits` (reveal top 6 + two `ChooseCardEffect.upTo` + movers) |

**Behaviour changes.** The eleven missing sought events are all raised now (Vexyr tests). Crucias no longer shuffles. Vhal's green face goes through the hand, so Cage doesn't stop it. Inquisitor and Vhal shuffle with fewer than two cards sought (CR 701.24c). The choice among sought cards names no card in the log. Spade's cards go hand → graveyard; Vinesoul's random land goes library → graveyard. Thought Rattle uses the perpetual cost store. Hamza manifests through the entry batch.

**Tests changed.** Abundant Harvest and Azurescale answer the mode labels "Land"/"Nonland". Inquisitor and Vhal answer `MultiGraveyardChoice`; two order assertions became any-order (CR 701.24c). 11 tests added.

**Seen, not changed.**
- Plunderer's Prize seeks onto the battlefield straight from the library, so Grafdigger's Cage stops it. Code, sure.
- `ChooseCardEffect`'s hidden flag is fixed per subject: a choice among cards that RETURN, CONJURE or SEARCH put into a hand would name them in the log. Unverified, no card found.
- `SeekLibraryEffect` to a hand logs no line, while `SeekCardsToHandEffect` logs "seeks N cards". Low.
- Fold run 4's line (no enter abilities for a sought noncreature, nonland permanent) is still open.

**For the next run.** Step 10 can merge `SeekEffect` (17 cards), `SeekCardsToHandEffect` (21), `SeekLibraryToHandEffect` (4) and `SeekCardToBattlefieldEffect` (5) into `SeekLibraryEffect`; they differ only in options it now has. A zone-based hidden check in `ChooseCardEffectHandler` breaks choices among revealed cards still in a hand or library (Addle, Intuition): keep hidden-ness on the subject.

### Step 9, fold run 17c (2026-11-24, parallel worktree)
Fold run 17b's leftovers in `cost-paid-copy`, `copy-on-enter`, plus Absorbing Man's record. Ratchet 3,690 / 2,954 → 3,685 / 2,949. Ledger 8 rows (absorbed 5, survivor 2, step-9 1). 11 card files rewritten, Oracle text checked for all; rulings for Curie, 'Dane, Crystalline Resonance, Volrath, Thespian's Stage, Absorbing Man (none). CR 113.2c, 205.1b, 205.3d, 608.2h, 707.2, 707.3, 707.9a, 707.9b, 707.9d, 613.2a checked.

**The shape.** `Subjects.paidAsCost()` (`PermanentSubject.PaidAsCost`): the entry's sacrificed snapshot, then its cost-tracked permanents; copied through `lastKnownCopiableCard`, which also reads `chosenCostPermanentSnapshots`. `CopyExceptions` gains `alsoStillA`, `alsoWithSubtypes` (a creature type only on a creature or kindred copy, CR 205.3d), `alsoColored` and `withAbilities` (CR 707.9a). The survivor's kept ability is one instance, and a second one when the copied values already have it (CR 707.9a, 113.2c); this drops the old rule of 'Dane, Crystalline Resonance and Thespian's Stage of not adding it twice. A kept triggered ability keeps its target group. `CopyCreatureCardInGraveyardOnEnterEffect(candidates, exceptions, grantHaste, exileCopied, exileTwo)`: the candidates are a static `cardsInGraveyards` / `exiledCards` subject (`SubjectResolutionService.namedCards`). Graveyard candidates go to the graveyard prompt, exiled ones to `ExiledCreatureCopyChoice` (a lone one is taken unasked); `CloneService.copyCandidates` / `isCopyCandidate` validate every answer.

| Batch | Result |
|---|---|
| cost-paid-copy | 2 folded: Curie (`withAbilities(trigger)`), 'Dane (`keepingThisAbility`) |
| copy-on-enter | 2 folded: The Master (`exiledCards(allOf(creature, CardHasCounterInExilePredicate(TAKEOVER)))`), Lazotep Convert (`withBasePowerToughness(4, 4).alsoColored(BLACK).alsoWithSubtypes(ZOMBIE)`); the survivor's 4 cards moved to the candidate form (`CardPutIntoGraveyardFromLibraryThisTurnPredicate` for The Fourteenth Doctor). Gone: `ExiledCreatureCopyOnEnterService`, two mayfx handlers, CloneService's fixed graveyard path |
| copy-exc / copier (17 rows) | `BecomeCopyOfTargetPermanentUntilYourNextTurnEffect` folded (Absorbing Man, Crystalline Resonance with a card target filter, Volrath). Not reached: Aegis/Headdress, Kaya, Spirit of Resilience's pair |

**Behaviour changes.** Curie copies an animated noncreature artifact as printed (the old creature-card check did nothing; CR 707.2; new test). 'Dane copies a face-down sacrificed creature by its face-down values (CR 708.2a; new test). A copy of Volrath keeps the ability it activated (ruling; new test). A kept triggered ability keeps its target. The Fourteenth Doctor's candidates are Doctor cards, as its text says (no creature requirement). The survivor's log line wins (The Master logs its decline). A copy of a copy of 'Dane, Crystalline Resonance or Thespian's Stage has two instances of the kept ability, each working on its own, and a Resonance copy of a Resonance triggers twice (CR 707.9a, 113.2c; tests); Thespian's Stage's handler changed to match.

**Seen, not changed.** None new.

**For the next run.** Aegis: `ChooseCardEffect.oneOf(exiledWithSourceMatching(creature))` + `BecomeCopyOfSubjectEffect.of(cardsThisWay(CHOOSE)).by(triggering()).lasting(WHILE_REMAINS_ATTACHED)`; Headdress needs `AttachmentTriggers`' direct handler call replaced by a two-step resolution and `WHILE_ATTACHED` on the survivor. Then Kaya (per-card exile-stay map on `StackEntry`) and Spirit (CHOOSE over last-known event cards).

### Step 9, fold run 21 (2026-11-24, parallel worktree)

Five of seven batches done. Ratchet 3,690 / 2,954 → 3,674 / 2,940. Ledger 25 rows: absorbed 14, split 2, survivor 5, step-9 3, plus the deleted interface. 20 card files rewritten, Oracle text checked for all; rulings for Lat-Nam's Legacy, See Beyond, The Three Seasons, Stream of Thought. CR 400.7j, 401.4, 608.2d, 614.1a, 614.6, 701.23a, 701.23b, 701.24a–e checked.

**The shape.**
- `PutCardsIntoLibraryEffect.shuffledIn(cards)`: each library a card went into is shuffled once through `LibraryShuffleHelper`, and the step records `SHUFFLE_INTO_LIBRARY` per owner.
- `.alwaysShuffling(players)`: also shuffles a library nothing went into, for a specific object or a possibly empty set (CR 701.24c, 701.24d). An impossible choice shuffles nothing (the See Beyond, Lat-Nam's and Three Seasons rulings).
- `ChooseCardEffect.fromEachOwner()`: one choice per owner's cards, in turn order.

| Batch | Result |
|---|---|
| card-shuffle | 7 folded: Credit Voucher, Lat-Nam's Legacy, See Beyond, Stream of Thought, Assemble from Parts, Riftsweeper, The Three Seasons, King Narfi's Betrayal, Summon: Esper Valigarmanda. Also gone: the hand-shuffle payload, `PutCardsFromHandOnLibraryCardChoice.shuffleIn`, five `GraveyardTargetOperationState` fields, two completion branches and an AI branch. Left: Serene Remembrance / Turn the Earth (`targetCard` names one card; the single-graveyard cast check reads the record) |
| perm-shuffle | not reached |
| dying-lib | 2 folded onto `PutOnTopOfLibraryInsteadOfDyingEffect(mayChoose, Position)` (`onBottom`, `shuffledIn`); the interface went |
| outside-game | 3 folded (incl. unaudited `SearchOutsideGameToHandEffect`, 3 cards) onto `SearchOutsideGameForCardToHandEffect(filter, reveals, destination)`. Left: Research (up to four shuffled in needs a card subject for outside-the-game cards) |
| multi-zone | not reached |
| one-per-pred | 2 folded (Conflux, Lotuslight Dancers) via `SearchLibraryForOneCardOfEachSubtypeEffect.ofEachColor`. Left: Firemind's Foresight (an exact-mana-value pick, a third pick queue; folds when `ToHandPick` takes a `CardPredicate`) |
| cond-dest | 2 split (Guidelight Pathmaker, Archdruid's Charm) into run 13's reveal-search composition (`REVEAL_ONLY` + put to battlefield + put to hand + shuffle) |

**Behaviour changes.**
- The Three Seasons shuffle event (fixes the "Fold:" defect; new test).
- Assemble from Parts shuffles after its card left exile (new test). A graveyard ability with `ExileSelfFromGraveyardCost` records the card's exile stay, so `sourceCard()` finds it.
- A lone hand or graveyard card is chosen unasked (Lat-Nam's, Esper).
- Saiba Syphoner's log no longer says "revealed".
- The outside-the-game "card card" log is fixed.
- The survivors' log lines win.

**Seen, not changed.**
- Firemind's Foresight's picks ignore Aven Mindcensor's top-N limit (sure of the code).
- Invasion of Arcavios's search passes no Opposition Agent controller (sure of the code, low).
- `LibrarySearchParams.battlefieldIfManaValueAtMost` / `battlefieldIfChosenPredicate` have no producer left; `battlefieldIfChosenPredicate` is still passed along at `LibraryChoiceHandlerService` line 2025 (dead code).
- The dying-library reader still asks only the floating loss of abilities (P03 line).

**For the next run.**
- `perm-shuffle`: `ShuffleTargetPermanentIntoLibraryEffect` over a `PermanentSubject` recording `SHUFFLE_INTO_LIBRARY` with the owners, so `playersThisWay` can replace `ThenEffectRecipient`; also Teferi's −12 as one event, and Mirror-Mad Phantasm through the entry batch.
- `multi-zone` with Research: one outside-the-game card subject.
- Serene Remembrance / Turn the Earth: a `targetCards` subject.

### Step 9, fold run 12e (2026-11-24, parallel worktree)

Rows: `dup-hand` leftovers (5) and `drafted-step` (7). Ratchet 3,690 / 2,954 → 3,681 / 2,945. Ledger: 16 rows (absorbed 3, split 6, blocked-other 2, `step-9` re-batched 1, survivor 3, Necropotence re-noted 1). 8 card files rewritten, Oracle text checked for all and for Viconia, Huntmaster and Skyforge. CR 205.1a, 400.2, 406.3, 608.2b, 608.2d, 608.2h, 707.2 and 708.2a checked.

**The drafted step.** `DraftCardFromSpellbookEffect` records `StepKind.CONJURE` (begun as NONE) as each draft is answered: the card in the hand or exile it went to, or the permanent it became. `.times(n)` drafts one after another into the hand, each from three new random cards, as one result. `ConjureCardFromSpellbookToHandEffect(spellbook, count)` records CONJURE as its choice is answered. A drafted card going into a hand is named only to its drafter.

| Batch | Result |
|---|---|
| `dup-hand` | 3 folded. Ace: `duplicateOf(triggeringSpell())` + perpetual flying. Snowborn: `duplicateOfTarget` + any-colour grant + `SpellXAtLeast(5)` choose-and-put. Agent: `TopmostInLibrary(targetPlayer())` + any-colour grant + `exiledFaceDown()`. Left: Viconia (re-batched to `specialize`; the faces are built by hand), Grizzled Huntmaster (`blocked-other`: no card subject outside the game) |
| `drafted-step` | 6 folded. Hourglass (Hold + PutHeld; the held store gone), Relics (`DraftTwiceSpellbookChoice` gone), Swine, Ominous, Chef. Left: Support Skyforge (`blocked-other`: "becomes an artifact creature" replaces types, CR 205.1a, and no perpetual step does that for a hand card) |

**Behaviour changes.** Snowborn's duplicate of a face-down target is a nameless 2/2 (CR 707.2, 708.2a; defect-queue line fixed, new test). Snowborn's put goes through the entry batch. Ace's flying is perpetual. Hourglass's drafted cards pass through the hand. A card chosen from a hand by `ChooseCardEffect` is logged unnamed (CR 400.2). The spellbook-conjure prompt names its own source. Logs are the survivors' lines. "It" across zone changes keeps today's reading.

**Seen, not changed.** `TargetPolarityClassifier` has the duplicated `ConjureCardsEffect` BENEFICIAL block twice (harmless). The selection-follow-up drafts (`LibraryRevealChoice`: Skyforge, Adaptive Armorer, Dragon Typhoon, Masterpiece Vault) record no CONJURE.

**For the next run.** Viconia in `specialize`. Necropotence's exile half is `ExileTopCardOfOwnLibraryEffect.of(you()).exiledFaceDown()`. A types-replacing perpetual option would fold Skyforge and Housemeld together.

### Step 9, fold run 19c (2026-11-24, parallel worktree)

`tempting`: 5 of 7 folded, plus 3 token-controller records the new subject unblocked. Ratchet 3,653 / 2,914 → 3,647 / 2,905. Ledger 13 rows (survivor 3, absorbed 6, split 1, unique 1, step-9 2). 9 card files rewritten, Oracle text checked for all 9. CR 101.4, 111.2, 608.2d, 608.2h, 707.10c checked.

**Shape.** `TemptingOfferEffect.of(prompt, players -> payload)` stores the payload twice: over `acceptedPlayers()` and over `you()`.
- Its handler asks each opponent in turn order (`apnapOpponents`) and changes nothing while they decide.
- After the last answer it sets `StackEntry.acceptedPlayerIds` and inserts `[payload over acceptors, RepeatEffect(n, payload over you)]` into the parked resolution.
- The text before "each opponent may" is the payload over `you()` as its own step.

New pieces:
- `CreateTokenCopyOfTargetPermanentEffect.forController(PlayerSubject)`: each named player creates, owns and controls the copies (CR 111.2). No Esix-style replacement is offered for them.
- `PutCounterOnTargetPermanentEffect.placedByEachController()`: each permanent's controller puts the counters.

| Member | Result |
|---|---|
| TemptingOfferPutCountersEffect | absorbed: counters `controlledBy(players, creature)`.placedByEachController() over you + offer |
| TemptingOfferCreateTokensEffect | absorbed: token + offer(`token::forController`) (Vengeance; Contract has no first batch) |
| TemptingOfferDrawAndCreateTokenEffect | absorbed: `SequenceEffect(DrawCardEffect(1, p), rabbit.forController(p))` |
| TemptingOfferCreateTokenCopyEffect | absorbed: target-group copy + offer(`copy::forController`) |
| TemptingOfferCopySpellEffect | unique: the M3C ruling has each opponent choose new targets while deciding; the old handler already does that |
| TemptingOfferReturnCreatureFromGraveyardEffect | left (19d): needs a `PlayerSubject` on `EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect` and an offer limited to opponents who can (CR 608.2d) |
| TemptingOfferSearchLibraryForLandToBattlefieldEffect | left (19d): needs to count only opponents who searched and one final shuffle over several SEARCH steps |
| CreateTokenCopyOfTargetCreatureForEachOtherPlayerEffect | absorbed: `forController(except(eachPlayer(), controllerOf(target())))` (Glimmervoid Basin) |
| CreateTokenCopyOfTargetCreatureForTargetPlayerEffect | absorbed: player group marker + `forController(targetPlayer(0))` (Echocasting Symposium) |
| ExileTargetPermanentAndCreateTokenCopyForEachOtherPlayerEffect | split: `ExileTargetPermanentEffect` + the copy for the others, read as last known (Fractured Identity) |

**Behaviour changes.**
- Glory and Bunnies wait for every opponent's decision (fixes the defect for these two; new tests).
- Glory's acceptors and Shadrix's target player put the counters, so Vorinclex sees them (Oracle; new Shadrix test).
- Reflections' copies for you are now offered Esix-style replacements (not tested).
- Fractured Identity exiles through the exile step.
- Logs: "accepts / declines the offer".
- AI polarity: Reflections NEUTRAL (was BENEFICIAL), Glimmervoid NEUTRAL (was none).

**Tests changed.** 4 added. DisplayNames added to the Bunnies, Vengeance and Contract tests. One allow-list line removed from `CardImmutabilityArchTest`.

**Seen, not changed.** The defect line naming Mayhem is wrong for Mayhem (M3C ruling). Sure.

**For the next run.** 19d:
- Give `EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect` a `PlayerSubject` (15 cards use it; check how `underOwnersControl` decides the controller).
- Give `TemptingOfferEffect` an "offered only to opponents who could" filter (CR 608.2d).
- Fold Immortality and Discovery; Discovery's final shuffle needs the union of the SEARCH steps' players.
- The four `CreateTokenFor*` records (40 and 27 cards) need their own run. Terrapact Intimidator's is a may-carrier with a target, not a one-liner.

### Step 9, fold run 22 (2026-11-24, parallel worktree)

`counter-place`: 13 folded (absorbed), 1 left. `token-count`: 0 folded, 1 unique. `token-add`: not started (11 left). Ratchet 3,653 / 2,914 → 3,640 / 2,914. Ledger 16 rows (survivor 1, absorbed 13, unique 2). 16 card files rewritten, Oracle text checked for all of them and for the survivor's 5 cards. CR 614.1a, 614.5, 614.12, 616.1, 702.104a and 702.104b checked.

**The shape.** `AddOnePlusOneCountersEffect(counterType, permanents, players, placedBy, change)`, with Hardened Scales as `()`:
- `counterType`: `CounterType.ANY` means "of each of those kinds".
- `permanents`: a `PermanentPredicate` judged against the source (`youControl([filter])`, `anyPermanent()`, `creatureOr(subtypes…)`, `PermanentIsSourceCardPredicate`).
- `players`: SELF ("you would get") or ANY ("on a permanent or player").
- `placedBy`: ANY, SELF or OPPONENT. A placement that names no placer is put by the affected object's controller (Vorinclex and Halving Season rulings).
- `Change`: PLUS_ONE, TWICE or HALF_ROUNDED_DOWN.
- A replacement whose only object is its source applies to the counters that source enters with (CR 614.12; Mowu's ruling). Conclave Mentor and Winding Constrictor don't apply to themselves entering (their rulings).
- In `GameQueryService`, one helper judges the predicate for every placement path. Type and control come from the placement's own reads (layered on the battlefield, printed while entering). Subtypes come from the layered board. Any other leaf goes to `PredicateEvaluationService`.
- Order prompts are unchanged: `counterReplacementsFor` and the entering placement record still ask where the order changes the result (CR 616.1).

| Batch | Result |
|---|---|
| `counter-place` | 13 folded: Doc Samson, Pir, Kami of Whispered Hopes, Solid Ground, Winding Constrictor (both lines), Ozolith, Caradora, Benevolent Hydra, Mowu, Vorinclex (both lines), Innkeeper's Talent, Loading Zone, Selesnya Loft Gardens, Doubling Season, Halving Season. Prairie Dog's grant is `.placedByYou()`. Left: Aether Refinery (energy only; player counters have no kind, so this would be a one-card option) |
| `token-count` | 0: HalveTokenCreationEffect is `unique`. `MultiplyTokenCreationEffect` multiplies by an int, so halving and an opponents-only scope would both be Halving Season-only values. No count-change component fits both counters and tokens (tokens take ×3, counters take +1) |
| `token-add` | not started: 11 rows stay `step-9` |

**Behaviour changes.**
- On paths that name no placer, Vorinclex doubles its controller's monstrosity and other counters instead of doubling and then halving them, and Halving Season no longer halves its controller's own counters.
- On those paths an opponent's Halving Season or Vorinclex now halves (before, only the affected player's own battlefield was read).
- Tribute names the paying opponent as placer (CR 702.104a): Doc Samson adds nothing, and Vorinclex halves.
- Mowu returned with a +1/+1 counter (Cauldron's Gift) gets one more.
- The noncreature-Vehicle path is the generic path.

**Tests changed.** `GameQueryServiceTest` builds the survivor in two places; no assertion changed. 5 tests added (Vorinclex, Halving Season ×2, Doc Samson, Mowu).

**Seen, not changed.**
- Tribute's "paid" is `placed >= N`, so a halved tribute counts as unpaid (CR 702.104b says "as specified"). Code sure, rule unsure.
- Experience counters and `MultiPermanentChoiceHandlerService`'s poison name no placer, so Vorinclex and Halving Season don't apply to them. Code, sure.
- The entering path that names a placer (6-argument `replaceCounters`) reads `appliesToAllPermanents` replacements only for the controller's own permanents (Primal Vigor's +1/+1 doubling). Code, fairly sure.
- Entering permanents are judged by printed types (Winding Constrictor's Mycosynth Lattice ruling, CR 614.12). Code, sure.

**For the next run.** `token-add` is its own run: the per-kind counters in `TokenCreationReplacementSupport` and the fixed-order addition counts in `PermanentControlSupport` should move onto `AddTokenCreationEffect`. Corpsejack Menace's and Primal Vigor's +1/+1 doublers (ledgered `unique` in step 5e), Zabaz and Vizier of Remedies could now be the survivor's TWICE or a minus-one change in step 10.

### Step 9, fold run 17d (2026-11-24, parallel worktree)

Fold runs 17 to 17c left these copy members in `copier` and `copy-exc`. Ratchet 3,648 / 2,909 → 3,644 / 2,905. Ledger 6 rows (absorbed 3, split 1, survivor 1, step-9 1). 4 card files rewritten; Oracle text and rulings checked for Assimilation Aegis, Dinosaur Headdress (LCC 16), Kaya, Spirit of Resilience. CR 400.7, 400.7e, 406.3a, 603.6c, 603.10a, 608.2b, 608.2c, 608.2d, 608.2h, 611.2b, 613.2a, 702.26f, 707.2, 707.9b checked.

**The shape.**
- Aegis: `SequenceEffect.of(ChooseCardEffect.oneOf(exiledWithSourceMatching(creature card)), BecomeCopyOfSubjectEffect.of(cardsThisWay(CHOOSE)).by(triggering()).lasting(WHILE_REMAINS_ATTACHED))`. Headdress puts the same two steps, lasting `WHILE_ATTACHED`, in `AsThisBecomesAttachedEffect`.
- `AttachmentTriggers` resolves any as-attached ability other than the color and name/type choices on a carrier (the Equipment as source, the creature as triggering permanent) through the new `EffectResolutionService.resolveNestedInCurrentResolution`.
- With nothing resolving, it resolves on its own, or as `resolveNestedInParkedResolution` when a resolution is parked.
- With one resolving, a nested entry that stops for an answer pushes a placeholder `ParkedResolution(outer, -1, nested)`. The outer loop's awaiting branch turns it into `(outer, i + 1, nested)` instead of overwriting the park, so the nested steps resume first and the outer goes on with its next effect.
- Kaya: the token-you-control target is a card target filter, with `ChooseCardEffect.upTo(triggeringCards(creature card), 1)` + `BecomeCopyOfSubjectEffect.of(cardsThisWay(CHOOSE)).by(target()).untilEndOfTurn().except(withKeywords(FLYING))`. A one-or-more zone-change record now stores each exiled card's exile stay (`StackEntry.eventCardExileEntryVersions`, copied by `copyTriggerEventFrom`, deep-copy test). `triggeringCards` names each card in that stay; before, only the first card of the batch was named.
- Spirit: `PutCountersOnSourceEffect(1,1,1)` + `ChooseCardEffect.upTo(Subjects.triggeringCardsAsTheyLastExisted(artifact or creature card), 1)` + `BecomeCopyOfSubjectEffect.of(cardsThisWay(CHOOSE)).untilEndOfTurn()`.
- `TriggeringCards.asLastKnown` names event cards that left their zone as the event recorded them (CR 608.2h).
- `cardsToDuplicate(cardsThisWay(CHOOSE))` reads a chosen event card that has left as last known too.

| Batch | Result |
|---|---|
| copier | Aegis/Headdress and Kaya folded. Gone: `AssimilationAegisCopyChoice` (record, interaction handler, AI strategy, projection), the Kaya branch of `GraveyardChoiceHandlerService` and its resume flag. Left: Myriad Pools and the Metamorphic Alteration pair (`blocked-other`, as before) |
| copy-exc | Spirit of Resilience's pair folded (1 split, 1 absorbed). Gone: the mayfx choice handler, `COPY_FROM_LEAVING_GRAVEYARD`, the copy-handler dependency of `GraveyardChoiceHandlerService`. `BecomeCopyOfCardUntilEndOfTurnEffect` stays `step-9`; its one maker is the unaudited exiled-with-source handler |

**Behaviour changes.**
- Kaya offers every creature card of a one-or-more exile, each only in its exile stay; a card that left exile and was exiled again is not offered (CR 400.7, ruling; new tests).
- Spirit's yes/no then pick is one optional pick (same outcome; tests moved to the pick).
- The Aegis prompt is the generic card choice.
- The survivor's log lines win (Kaya's "finds no creature cards" line is gone).
- Aegis and Spirit still show the choice when the Equipment has come off or Spirit has left; the copy then does nothing.

**Seen, not changed.** None new.

**For the next run.** Myriad Pools needs a card subject for a spell on the stack. The Metamorphic Alteration pair needs a static copy verb. `BecomeCopyOfCardUntilEndOfTurnEffect` folds with `BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffectHandler`'s cards (Dermotaxi, Lazav Wearer of Faces, Protean War Engine).

### Step 9, fold run 18c (2026-11-24, parallel worktree)

Batch `look-cast`. Ratchet 3,623 / 2,886 → 3,618 / 2,881. Ledger 6 rows (survivor 1, absorbed 5). 9 card files rewritten, Oracle text checked for all 9. Rulings checked for Velomachus, Sunbird, Kiora, Aetherworks, Cosmic Cube, Perception, Planetarium and Galvanoth. CR 118.8, 118.9, 601.2a, 601.2f, 601.3, 603.2h, 608.2g, 608.2h, 701.20a, 701.20b, 701.20e, 709.3a, 709.3b, 712.8b and 716.3 checked.

**Record shape.** `LookAtTopCardsAndMayCastEffect(DynamicAmount count, CardPredicate castFilter, DynamicAmount spellManaValueAtMost, boolean reveals, boolean restOnBottom)`:
- Factories: `lookAt(n | amount)`, `reveal(amount)`; withers `.matching`, `.withSpellManaValueAtMost`, `.leavingTheRest()`.
- Count and bound are evaluated once (CR 608.2h).
- The cards stay in the library (CR 701.20b, 701.20e), shown to the controller or to every player.
- One LibrarySearch `CAST_WITHOUT_PAYING` prompt casts from the library during the resolution (CR 608.2g), paying additional costs (CR 118.8).
- A land face is never offered. A modal double-faced card is offered by either face (CR 712.8b). A split card is offered by its cheapest half, and `mayCastManaValueAtMost` allows only fitting halves (CR 709.3a).
- "Less than X" is written `Sum(X, Fixed(-1))`.

**Where the cards wait.** The completion handled three placements: cards taken out of the library (into no zone), cards left in it, and Kiora's exile. Exile sent Kiora's spell through the exile queue, so it was cast from exile. The library placement is the rules one (Aetherworks ruling), and the library flow already pays additional costs through `beginSacrificeCostIfNeeded`. So every member now waits in the library.

| Batch | Result |
|---|---|
| `look-cast` | 5 folded. Cosmic Cube (`lookAt(6).withSpellManaValueAtMost(GreatestPowerAmongControlled…)`); Perception (`lookAt(PermanentCount)`, bound 3); Aetherworks / Svella (`lookAt(6 / 4)`); Kiora (`lookAt(TargetSpellManaValue)`, bound `Sum(TSMV, -1)`); Sunbird (`reveal(TSMV)`, bound TSMV); Galvanoth (`MayEffect(lookAt(1).matching(instant or sorcery).leavingTheRest())`); Planetarium (`lookAt(1).leavingTheRest()`). Velomachus (survivor's card): `matching(instant or sorcery).withSpellManaValueAtMost(SourcePower(true))`. |

**Completion changes (`handleCastWithoutPayingChoice`).**
- `restStays` (the `reorderRemainingToTop` flag on this destination): only the cast card leaves the library.
- Choosing to cast marks the resolving entry's "do this only once each turn" action (CR 603.2h; Planetarium's limit, previously marked by the deleted may branch).

**Behaviour changes:**
- Cards stay in the library during the choice (7 test assertions updated per CR 701.20b).
- Kiora casts from the library, so Melek copies it (test).
- The split-half bound now covers Cosmic Cube, Kiora, Velomachus and Perception (Cosmic Cube test).
- Galvanoth and Planetarium pay mandatory additional costs. `handleCastFromLibraryChoice` never paid them (Kuldotha Rebirth test).
- The looked-at cards are revealed privately to the controller even when none can be cast.
- The Galvanoth and Planetarium cast prompt is the LibrarySearch prompt; their tests were updated.
- Logs are the survivor's.

**Seen, not changed:**
- The library and exile free-cast flows (`castCardWithoutPaying`, `ExileFreeCastQueueSupport`) pay no cost increases (CR 601.2f): cascade, Sunbird, Aetherworks, Kiora and the rest. Sure of the code.
- The library flow offers no Adventure (CR 716.3). Kiora's old exile path did, but with no mana-value bound on the Adventure. Sure of the code.
- A modal double-faced card offered by its back face is then cast with both faces on offer (`legalModalOptions` ignores the cast filter). Velomachus could cast Jadzi's creature face. Fairly sure.
- A refused cast (Teferi, Rule of Law) logs "has no legal targets". Low.

**For the next run.** `LookAtTopCardsMayCastExiledNonlandCardEffect` (Duelist's Flame, Keldon Flamesage, The Key to the Vault) could join the survivor with an exile-first option. The cost-increase gap above needs a defect run over the two free-cast flows.

### Step 9, fold run 21b (2026-11-24, parallel worktree)

Only the `perm-shuffle` batch. Ratchet 3,623 / 2,886 → 3,616 / 2,879. Ledger: 11 rows (absorbed 3, split 4, survivor 3, unique 1). 16 card files rewritten, Oracle text checked for all. Rulings read for Vaevictis, Mirror-Mad, Elixir, This Is How It Ends and Teferi TP. CR 111.7, 400.7, 603.10a, 608.2b, 608.2c, 608.2h, 701.24a–e checked.

**The shape.** `ShuffleTargetPermanentIntoLibraryEffect(subject, predicate, thenEffect, recipient)`:
- Subjects: `target()` by default; `.of(subject)` for `source()`, `attached()`, `thisWay(CHOOSE)`, `createdThisResolution()` and `controlledBy(targetPlayer(), f)` (declares the player target); `.sourceAndTarget(f)`; `.sourceThen(...)`.
- The permanents leave as one batch and each library is shuffled once.
- A specific non-target object that left still has its owner's library shuffled (CR 701.24c). For an Aura that left with its host, the host is the target captured at activation.
- It records SHUFFLE_INTO_LIBRARY per owner. Tokens and exile replacements count, as the StepKind doc says.
- When the next effect is the same record or `EachPlayerShufflesZonesIntoLibraryEffect`, the libraries go into `deferredLibraryShuffleOwnerIds` and that effect shuffles them.
- The then-effect stays for riders that take no player subject: Zoyowa's Discover, Audacious Swap's cast, Blink, Chaos Warp, Oblation, Gandalf.

| Batch | Result |
|---|---|
| perm-shuffle | 3 absorbed: shuffle-self (7 cards), referenced (4 cards), Teferi TP's set record. 4 split: Elixir, Mirror-Mad, This Is How It Ends, Vaevictis. `RevealTopCardPermanentToBattlefieldEffect` gains a `PlayerSubject`. `SacrificeSelfEffect` takes `allTargets()` with several sacrificers. Audacious Swap's rider is `unique`. |
| multi-zone, outside-game (Research), card-shuffle (Serene) | not reached |

**Behaviour changes.**
- Teferi's −12 is one leave event (Cosmographer test).
- Dramatic Accusation, Watery Grasp and Stay Hidden shuffle the owner's library when the enchanted creature left (CR 701.24c; Probe test).
- Mirror-Mad's found card and Vaevictis's revealed cards enter through the entry batch.
- Vaevictis's sacrifices are one event.
- Shuffle log lines use `publicName`. The survivors' log lines win.

**Also gone:** `DestroyChosenCreature.shuffleIntoOwnersLibrary` and its branch; This Is How It Ends's `VillainousChoiceState` rerun.

**Seen, not changed.**
- `SubjectResolutionService.attachedId` loses the host of an Aura that left with it when the entry has no source snapshot; the survivor reads the captured target instead. Sure of the code.
- Elixir's 2011 ruling ("shuffle just your graveyard") predates CR 701.24c; the current rule was followed.

**For the next run.** `multi-zone` with Research: a zone set (hand, library, graveyard, outside the game, face-up exile) on the to-hand / to-battlefield multi-zone search. `SearchLibraryAndOrGraveyardChoice` already carries the outside-the-game and hand id sets, but `SearchHandAndOrLibraryChoice` and `SearchOutsideGameOrExileCardChoice` are separate interactions to merge. Research needs an outside-the-game card subject. Serene Remembrance needs a `targetCards` subject.

### Step 9, fold run 22b (2026-11-24, parallel worktree)

`token-add`: 11 of 11 folded (1 survivor, 10 absorbed). Ratchet 3,604 / 2,877 → 3,594 / 2,877. 12 card files rewritten, Oracle text checked for all 12; rulings read for Academy Manufactor, Xorn, Chatterfang and Bilbo. CR 614.1a, 614.5, 616.1, 616.1f, 506.3 checked.

**Shape.** `AddTokenCreationEffect(List<CreateTokenEffect> tokens, TokenKind when, Count count)`:
- `Count.ONE`: each blueprint once per event ("those tokens plus an additional X").
- `Count.PER_TOKEN`: once per token of `when` ("plus that many").
- `Count.ONE_OF_EACH`: per token of `when`, each blueprint it shares no subtype with ("instead create a Food token and a Treasure token", "one of each").
- `TokenKind.any()` / `creature()` / `artifact()` / `withAnySubtype(…)`.
- Shared blueprint constants (`TREASURE`, `CLUE`, `FOOD`, `MAP`, `MUTAGEN`, `THOPTER`, `FROG`, `SQUIRREL`, `SOLDIER`), so equal additions from different permanents are equal and orders that end alike need no prompt.
- A solved Case is `ConditionalEffect(SourceIsSolved, …)`.

**How it is read.** `TokenAdditionReplacements` takes one option per active static instance (`getActiveStaticEffects`, plus `removesOwnStaticAbility`), from the battlefield of the player the tokens are created for after Crafty Cutpurse (`resolveTokenCreationController(…, false)`). Xorn and Jolene left `TokenCreationReplacements`; the count path is now multipliers and Halving Season only.

**Riders.** `TokenCreationReplacementSupport.withEventModifiers` is the one rider rule for every added or substituted token:
- an added creature keeps "attacking" and "attacking untapped";
- an added noncreature is tapped where the others are tapped and attacking (CR 506.3);
- granted keywords and delayed exile are kept.

**Fixed-order path deleted.** `PermanentControlSupport` creates only through the ordered event. A creation with count 0 but simultaneous tokens is ordered too; a creation of no token is no event. Gone: the per-kind counters, Manufactor's ×3 compounding, Xorn's special case inside Manufactor, the Clue re-create, and `GameQueryService.getNewCreatureTokenCreationAmount`.

**Behaviour changes.**
- Under Crafty Cutpurse, additions come from the player the tokens are created for (Chatterfang's ruling). New test, fails on old code.
- Added creature tokens of an "attacking" creation attack untapped (ruling). New Quina test, fails on old code.
- An added noncreature token of a tapped-and-attacking creation is flagged tapped only. The Tippy-Toe test also passes on old code, which already kept the Food out of combat.
- Map and Bilbo's Treasure keep "gains [keyword] until end of turn" riders. Untested.
- In a token-copy event, Xorn adds a plain Treasure, not another copy (Oracle). Not observable.
- Additions read active statics (layered card, conditions); Divine Visitation and Fisher's substitutes keep "attacking untapped" too. Untested.
- Labels: Xorn "create an additional Treasure token"; Bilbo and Manufactor "create one each of … for each … token".

**Tests changed.** 3 added (Crafty Cutpurse, Quina, Tippy-Toe). `WorldwalkerHelmTest` suppresses `AddTokenCreationEffect` instead of the old marker; no assertion changed.

**Defects.** Partly fixed: "`TokenCreationReplacementSupport`'s static counts … ask only the floating flag". The Bilbo, Manufactor and Map counts are gone. The subtype and Divine Visitation reads on the non-ordered entry path (`activeTokenSubtypeReplacements`, `staticEffectCount`) remain.

**Seen, not changed.**
- `TokenKindReplacements` and `JinnieFayTokenReplacements` read `event.controllerId()`'s battlefield, not the creator after Cutpurse; the count replacements resolve with Gather Specimens for creature tokens, though Chatterfang's ruling says entering-control changes apply after. Fairly sure.
- Stridehangar Automaton's "Thopters you control get +1/+1" uses `otherCreaturesYouControl()`, so it would skip itself if it became a Thopter. Sure, low.

**For the next run.** 19d as fold run 19c's plan says. The two remaining fixed-order `getTokenMultiplier` callers (`CreateTokensAttachedToControlledCreaturesEffectHandler`, `ReturnTargetCreatureToHandAndCreateTokenCopyEffectHandler`) could move onto the ordered event in a defect run.

### Step 9, fold run 19d (2026-11-24, parallel worktree)

`tempting` leftovers: 2 of 2 folded. Ratchet 3,617 / 2,877 → 3,615 / 2,873. Ledger 6 rows (absorbed 2, survivor 4). 2 card files rewritten, Oracle text and rulings checked for both. CR 101.4, 110.2a, 608.2d, 701.23a, 701.23b, 701.24a checked.

**Shape.**
- `TemptingOfferEffect.offeredOnlyTo(PlayerSubject)`: only the opponents the subject names are asked, in turn order. These are the ones who could do the payload (CR 608.2d). Nothing changes while they decide, so every acceptor still can, and `RepeatEffect(accepted count)` counts exactly those who did it.
- `EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect.by(players)`: a `PlayerSubject` component (`eachPlayer()` by default). It is resolved through the subject resolver in the same APNAP order as the old private list. Each returner controls what they return (the batch's graveyard owner, CR 110.2a); `underOwnersControl` is unchanged. Its 15 cards are untouched.
- `EachPlayerSearchesLibraryEffect.leavingLibrariesUnshuffled()` (`BasicLandSearchQueue.leavesLibrariesUnshuffled`): no library is shuffled, not even a prevented must-search.
- `ShuffleLibraryEffect(PlayerSubject players)`: each named player shuffles once, in APNAP order. The boolean form maps to `you()` / `targetPlayer()`.
- `PlayerCanSearchTheirLibraryPredicate`, evaluated by `LibrarySearchRestrictions.find`. This is the quiet form of `checkSearchRestriction`, which now calls it.

| Member | Result |
|---|---|
| TemptingOfferReturnCreatureFromGraveyardEffect | absorbed: `returnOne(you())` + offer(`returnOne`).offeredOnlyTo(`ownersOf(cardsInGraveyards(eachOpponent(), creature))`), with `returnOne = mandatory(1, creature).by(players)` |
| TemptingOfferSearchLibraryForLandToBattlefieldEffect | absorbed: `search(you())` + offer(`search`).offeredOnlyTo(`matching(eachOpponent(), canSearch)`) + `ShuffleLibraryEffect(allOf(matching(you(), canSearch), acceptedPlayers()))` |

**Behaviour changes.**
- Immortality waits for every opponent's decision; the acceptors choose in turn order and their creatures enter together (C13 rulings, CR 101.4). Fixes the remaining part of the "Fold:" defect; new 3-player test.
- Immortality returns a lone creature card unasked (CR 608.2d); 4 tests drop the prompt.
- Discovery no longer offers the search to an opponent who can't search (CR 608.2d); the Stranglehold test changed. Previously such an opponent was asked, accepted, and then did nothing.
- "Each player who searched" is read as the acceptors plus you if you could search. Acceptors always search once the filter applies, and your must-search happens exactly when you could.
- Logs are the survivors' lines ("X shuffles their library.", "returns X from graveyard to the battlefield").

**Tests changed.** Immortality: 4 changed, 2 added. Discovery: 1 changed, 2 added (a controller who can't search doesn't shuffle; each searcher shuffles once).

**Seen, not changed.** None new.

**For the next run.** The four `CreateTokenFor*` records (token-controller cause) need their own run; Terrapact Intimidator's is a may-carrier with a target.

### Step 9, fold run 21c (2026-11-24, parallel worktree)

Two of three batches done. Ratchet 3,600 / 2,873 → 3,596 / 2,869. Ledger 7 rows: absorbed 3, split 1, survivor 2, step-9 1. 5 card files rewritten, Oracle text checked for all (and for Serene Remembrance and Turn the Earth). Rulings read for Coax, Karn, Last Light and Arcavios. CR 400.2, 400.4a, 400.11, 406.3, 608.2b, 608.2c, 608.2d, 701.23a–e, 701.23h and 701.24a–e checked.

| Batch | Result |
|---|---|
| multi-zone | 3 folded. The to-hand survivor gains `Set<Zone> zones` (LIBRARY, GRAVEYARD, OUTSIDE_GAME, face-up EXILE) and `optional`, with factories `inZones` (Arcavios) and `mayTakeFrom` (Coax, Karn). In the battlefield survivor, `includeHand` / `includeLibrary` became a zone set (the boolean constructors stay), plus `inZones` (Last Light). `SearchLibraryAndOrGraveyardChoice` gains `exileCardIds` and `optional`. `SearchHandAndOrLibraryChoice` (interaction, handler, AI strategy, projection) is gone. `SearchOutsideGameOrExileCardChoice` keeps only Grizzled Huntmaster's choose-and-keep use. |
| outside-game (Research) | 1 split into `ChooseCardEffect.upTo(cardsOutsideTheGame(you(), null), 4)` + `PutCardsIntoLibraryEffect.shuffledIn(cardsThisWay(CHOOSE)).alwaysShuffling(you())`. `ShuffleCardsFromOutsideGameChoice` (interaction, handler, AI strategy, projection) is gone. |
| card-shuffle (Serene, Turn the Earth) | left; see "For the next run" |

**Behaviour changes.**
- Arcavios's library search passes Opposition Agent's controller (defect fixed; new test).
- Last Light offers only permanent cards (CR 400.4a, ruling). The survivor's entry-ban check now applies to it.
- Last Light with no Dragon anywhere now searches and shuffles the library. The old skip used the library's hidden contents and showed them to the opponent through the missing shuffle (CR 400.2); test rewritten.
- A card taken from outside the game spends the play permission naming it (Arcavios now as well).
- Cards outside the game are judged with no game effects for all searchers (CR 400.11). Coax already did this; Arcavios now does too.
- A battlefield search whose zones exclude the library no longer shuffles it on a decline.
- Research's choice is logged as "A card / Cards [player] owns outside the game is / are chosen.", naming no card (new test). The survivors' log lines win.

**Seen, not changed.**
- `SearchLibraryAndOrGraveyardChoiceAiStrategy` answers only when the AI is `playerId`, not `decidingPlayerId()`, so an AI that controls Opposition Agent never answers these searches. Sure of the code.
- When a library search is prevented, the to-hand survivor still shuffles on a fruitless search or a decline ("If you search your library this way, shuffle"). Sure of the code, low.
- `SearchOutsideGameOrExileCardChoice` keeps a name that no longer fits; rename it to an outside-the-game choice when it is next touched.

**For the next run.** Serene Remembrance / Turn the Earth need:
1. A `CardSubject` that names every target card of the effect, with the max count and the single-graveyard rule. `TargetCard` names one.
2. `SpellCastingService`'s two branches keyed on the record (the hand cast at the up-to-N all-graveyards check, and the flashback path) keyed on that subject instead.
3. Serene's `ShuffleIntoLibraryEffect` merged into the owner's library shuffle, possibly deferring through fold run 21b's `deferredLibraryShuffleOwnerIds`, so Psychogenic Probe sees one shuffle.

### Step 9, fold run 23 (2026-11-24, parallel worktree)

Batches `counter-move`, `counter-kind` and part of `all-counters`. `chosen-counters`, `player-counters`, `counters-among` and `asenters-counter-removal` are not started. Ratchet 3,595 / 2,868 → 3,587 / 2,860. Ledger 13 rows (survivor 3, absorbed 8, step-9 2). 19 card files rewritten, Oracle text checked for all 19 and for Vorinclex. Rulings checked for Forgotten Ancient, Aetherborn Marauder, Agent's Toolkit, Slippery Bogbonder, Scrounging Bandar and Aether Snap. CR 115.3, 122.1, 122.5, 608.2d, 608.2h and 702.63a checked.

**The shape.** `MoveCounterFromSourceToTargetCreatureEffect(counterType, amount, anotherTarget, from, to, anyPermanent, Quantity)`:
- `Quantity`: `AMOUNT` (the old forms), `ALL` (every kind for `CounterType.ANY`), `ANY_NUMBER`.
- `from` and `to` take any `PermanentSubject`. Factories: `move`, `all`, `anyNumber`.
- `CounterType.ANY` with `AMOUNT` means "a counter". The kind is asked (`MoveOneCounterChoice`) only when several kinds can move (CR 608.2d).
- `ANY_NUMBER` asks one 0..N question per from-permanent, to-permanent and kind (`ChoiceContext.MoveCountersAmountChoice` of `CounterMoveSlot`s). Each answer removes at once. The counters then go on each destination together, one placement per destination and kind.
- Every move runs through the `PermanentCounterSupport` move helpers:
- CR 122.5 is checked per kind before anything is removed.
- The entry's controller places the counters.
- Removal sends the oil and vanishing events.
- `StackEntry.targetsForGroup`: an activated ability's targets are read by position.
- `AddAnotherCounterOfChosenTypeToTargetEffect(PermanentSubject)`: over a non-target set it asks each permanent in turn, a kind or skip.
- `RemoveAllCountersFromTargetPermanentEffect`: takes `allPermanents()` and sends the last-counter-removed event for each kind.

| Batch | Result |
|---|---|
| `counter-move` | 6 of 7 folded: Bogbonder, Resourceful Defense, Agent's Toolkit, Forgotten Ancient, Scrounging Bandar, and the 11-card target-to-target record. Aetherborn moved off `MoveCountersFromControlledPermanentsToSourceEffect`; Galloping Lizrog is left on it, because its text is a removal |
| `counter-kind` | 1 folded: The Caves of Androzani |
| `all-counters` | 1 of 2 folded: Aether Snap. Left: Suncleanser's creature lock. It is a rule of the resolution, not an ability, so `GrantEffectToTargetEffect` can't express it |
| others | not started (13 rows) |

**Behaviour changes.**
- The mover is the placer (Nesting Grounds and Vorinclex, test).
- Aetherborn Marauder moves nothing once it has left the battlefield (CR 122.5, test).
- A kind the destination can't have is not offered or moved (CR 122.5).
- Moving a permanent's last time counter sacrifices it (CR 702.63a).
- Remove-all sends the last-counter event (Hexmage and Divine Intervention, test). Aether Snap gains the vanishing and defense checks.
- Forgotten Ancient asks per creature.
- Tidus declares two target groups.
- The Caves judges Saga on layered subtypes, once, as it resolves (CR 608.2h).
- AI: it moves all counters, except from its own permanent onto one it doesn't control.
- The survivors' log lines win.

**Tests changed.** ForgottenAncientTest (4), TidusYunasGuardianTest. 4 tests added.

**Seen, not changed.**
- `RemoveCounterFromControlledPermanentCost` removes the first allowed kind instead of letting the payer choose. Code, sure.
- Moving counters raises no counters-removed event (the Scrounging Bandar ruling says such abilities apply). The old handlers didn't either. Fairly sure.

**For the next run.** `player-counters` with Suncleanser: one player-counter removal over a `PlayerSubject` and a kind, recording REMOVE_COUNTERS, plus one counter-lock floating rule for a permanent or a player. Then Lizrog joins `counters-among` as `controlledBy(you(), creature)` + `PutCountersOnSelfEffect(Scaled(AmountThisWay(REMOVE_COUNTERS), 2))`. A slot with no destination can reuse the `CounterMoveSlot` question.

### Step 9, fold run 25 (2026-11-24, parallel worktree)
Ratchet 3,595 / 2,868 → 3,578 / 2,862. Ledger 25 rows (survivor 4, absorbed 18, step-9 3). 20 card files rewritten, Oracle text checked for all. CR 202.3e, 608.2h, 611.2c, 614.1a, 616.1 checked.

| Batch | Result |
|---|---|
| `src-doublers` | 6 folded, plus The Sound of Drums' unique record (the second user of `DamageScope`). `DoubleDamageFromCreaturesEffect` gains `permanentsYouControl()` sources, `spellSourceFilter` (a `CardPredicate` on a spell's card, X counted) and `DamageScope` ALL / COMBAT / COMBAT_TO_PLAYERS. All members already shared its reader. |
| `rcpt-doublers` | 4 folded. `DoubleDamageToOpponentsAndTheirPermanentsEffect(players, permanents)`, permanents being `controlledBy(players)`, `source()` or null. The Curse stays its own whole-event option (`getEnchantedPlayerDamageMultiplier`), so the order prompt does not lose a step. Lightning floats the survivor over `you()` on the damaged player. |
| `floated-doublers` | 4 folded, 3 left. Fiery Emancipation / City on Fire → `DoubleControllerDamageEffect(3, …)`. Insult / Quest float `DoubleControllerDamageEffect(null, true)`. Goliath / Isengard float `DoubleControllerDamageToOpponentsAndTheirPermanentsEffect(multiplier, noncombatOnly, toTheirPermanents)`. The triage note named the any-source recipient doubler; the texts say "a source you control". Left: Overblaze, Jeska (a doubler granted to a target permanent; Jeska's "your opponents" is the caster's), Blind Fury (any creature to a creature). |
| `floated-dmg-bonus` | 2 folded into `BoostControllerDamageThisTurnEffect(DynamicAmount, color, noncombatOnly)`, the amount fixed on resolution (CR 608.2h). Each floating reader keeps its family, so option keys and order are unchanged. |
| `add-dmg` | Not started. Eight readers with different guards (`hasLostAllAbilities`, `hasLostPrintedAbilities`, `losesAllAbilitiesByEffect` + `computeStaticBonus`, none) and colour reads (Ghostly Flame's `getDamageSourceColors` vs effective colours). Folding them needs one bonus-reader design. |

**Behaviour changes:**
- Obosh counts each X of an XX spell (CR 202.3e). Untested.
- Spell subtypes count perpetual and all-zone subtypes.
- Curse of Bloodletting stops doubling once it has lost its abilities (the survivor's guard).
- Goblin Goliath takes Maro's Gone Nuts (test).
- Insult, Quest, Goliath, Isengard and Lightning log nothing on resolution; the survivor's log wins.

**Seen, not changed:**
- All recipient multipliers are one option, and all controller and source multipliers another. A "plus N" can't be applied between Gisela and Goldnight Castigator (CR 616.1). Sure of the code.
- Blind Fury's doubling skips Maro's Gone Nuts. Sure.

**For the next run.** `add-dmg` needs one bonus reader with source and recipient filters, judged like `getAdditionalDamageToOpponentsBonuses` (Ghostly Flame colours). The add-dmg "Fold:" defect (Sulfuric Vapors, Disciples, Enhanced Surveillance guards) goes with it. Overblaze and Jeska need a timed grant of `DoubleDamageFromCreaturesEffect(source())` onto a permanent, read by `getSourceDamageMultiplier`.

### Step 9, fold run 28 (2026-11-24, parallel worktree)

Batches `delayed-target` and `next-time`. Ratchet 3,588 / 2,861 → 3,573 / 2,847. Ledger: 17 rows (absorbed 15, survivor 2). 33 card files rewritten, Oracle text checked for all (and Arcum's Whistle). Rulings read: Angrath, Norritt, Berserk, Storm King's Thunder. CR 115.10a, 603.4, 603.7a–e, 608.2h, 701.21a checked.

| Batch | Result |
|---|---|
| delayed-target | 9 absorbed into `RegisterDelayedStepTriggerEffect.aboutAtNext(step, target() / triggering(), effects)`. The registering object declares the harmful target and the delayed ability is about it without targeting it. "It" is `triggering()`; "them" is `triggeringAll()`. "If it [didn't] attack / has mana value N or less" is `ConditionalEffect(TriggeringPermanentMatches(...))` checked as it resolves. Spinal Embrace is sacrifice + `HappenedThisWay.sacrificed()` + `AmountThisWay.sacrificedToughness()`. |
| next-time | 6 absorbed into `RegisterGlobalTriggeredAbilityEffect.nextThisTurn(TriggeredAbility)` (CR 603.7b). The survivor keeps the noted amount, so Storm King's Thunder is `NoteAmountEffect(XValue)` + `ConditionalEffect(atLeast(NOTE, 1), …)`, which keeps the old "none at X 0". A new manafx handler serves Magus Lucea Kane and Yuna. |
| value-binding, ability-copy | not reached |

**Behaviour changes.**
- Depthshaker Titan sacrifices only the animated artifacts you control (CR 701.21a; the old record made their new controller sacrifice them). Test.
- Norritt, Nettling Imp, Arcum's Whistle: the "if" is not an intervening "if" (CR 603.4: it doesn't follow the trigger condition; Angrath's ruling uses the same template). The ability triggers and checks the attack as it resolves. Norritt test. NettlingImpTest's precombat case asserts before the end step, because the harness overshoots after the new trigger.
- Basalt Golem, Tolarian Entrancer, Sawtooth Ogre no longer declare a harmful target (the triage 3/5 "Seen" item).
- Angrath and Spinal Embrace register even if control wasn't gained (CR 603.7a). Not observable in practice.
- Registration log lines are gone. The survivor's lines win.

**Also gone:** `CopyNextInstantOrSorceryCastThisTurnEffect.delayedAbility(int)`, and four by-name entries in `TargetPolarityClassifier`.

**Seen, not changed.** `leave-battlefield-redesign.md` still names the deleted Spinal Embrace handler in its history; I left it, since it is a history document.

**For the next run.** `value-binding` needs one binder: a resolution choice (creature type, planeswalker type, chosen player) or a source's chosen subtype or card type, written into a `PermanentPredicate` / `CardPredicate` when the trigger or rule is registered. `bindCardSubjects` and `bindAmounts` are the models, and its first users are Walking Desecration, Leori, The Black Gate, Progenitor's Icon and Apex Observatory. `ability-copy` (Gogo and the engine-built copy and retarget markers) also needs the "Unverified" queue line on `CopyControllerActivatedAbilityEffect` checked. `CopyNextSpellCastThisTurnEffect` (4 cards) and `CopyNextInstantOrSorceryCastThisTurnEffect` (21 cards) could fold onto `nextThisTurn` the same way; I did not audit them.

### Step 9, fold run 31 (2026-11-24, parallel worktree)

Batches `res-colour`, `static-colour`, `day-night` and `self-grants` are done: 14 folded. `pt-amount`, `base-pt-new` and `floated-name` are left untouched. Ratchet 3,576 / 2,857 → 3,562 / 2,842. Ledger 19 rows (absorbed 14, survivor 5). 15 card files rewritten, Oracle text checked for all of them. CR 105.3, 205.1a, 502.2, 604.3a, 611.2a, 611.2c, 613.1e, 613.7 and 732.1 checked.

**Shape.**
- `BecomeChosenColorsUntilEndOfTurnEffect(colors, subject, duration, ColorChoice)`, with `ColorChoice` being `FIXED`, `ONE` or `ONE_OR_MORE`. Factories: `.chosenColors(subject, d)`, `.chosenColor(subject, d)` and `.fixed(colors, subject, d)`; an empty fixed set is colorless.
- The subject is resolved once (CR 611.2c). A subject that names nothing asks nothing.
- One `BecomeChosenColorsChoice(targetIds, …, oneColor)` covers every permanent the subject names.
- The floated `FIXED` instance is the layer-5 setter.
- `ColorSetTargetsChoice` and `beginColorSetChoiceForTargets` are gone.
- `GrantColorEffect.ofChosenColor` / `.chosenColor(subject[, filter])`: the source's stored chosen colour, which replaces the permanent's colours.
- Over `source()` it is not a CDA (CR 604.3a; Puca's Eye's colour comes from a triggered effect). It keeps the old choice-timestamp rule.
- The static handlers, `StaticEffectSupport`, `TextChangeTransformer` and the token-copy colour filter all read the new component.
- `BecomeNightEffect(Change)` with `.day()` and `.toggle()` (CR 732.1, 502.2). `DayNightService` is unchanged.
- `SetCardTypesEffect`'s one-shot handler takes any subject and floats a `target()` copy on each permanent.
- `SetTargetPermanentSupertypeEffect` gains a `PermanentSubject` and resolves it through the subject resolver.

| Member | Result |
|---|---|
| BecomeChosenColorsIndefinitelyEffect | absorbed: `chosenColors(target() / source(), PERMANENT)` (Prismatic Lace, Shyft) |
| BecomeAllColorsUntilEndOfTurnEffect | absorbed: `fixed(all, source(), UNTIL_END_OF_TURN)` |
| BecomeColorlessIndefinitelyEffect | absorbed: `fixed({}, target(), PERMANENT)` |
| SetChosenColorForTargetCreaturesUntilEndOfTurnEffect | absorbed: `chosenColor(target(), UNTIL_END_OF_TURN)` over any number of targets |
| SetSelfColorIndefinitelyEffect | absorbed: `fixed({BLUE}, source(), PERMANENT)` |
| EnchantedPermanentBecomesChosenColorsIndefinitelyEffect | absorbed: `chosenColors(attachedCreature(), PERMANENT)` |
| BecomeChosenColorEffect | absorbed: `GrantColorEffect.chosenColor(source())` |
| EnchantedPermanentBecomesChosenColorEffect | absorbed: `chosenColor(attached())` |
| AllNonlandPermanentsAreChosenColorEffect | absorbed: `chosenColor(allPermanents(), not land)`; both handlers gone |
| BecomeDayEffect / ToggleDayNightEffect | absorbed: `BecomeNightEffect.day()` / `.toggle()` |
| SetCardTypesUntilYourNextTurnEffect | absorbed: `SetCardTypesEffect({LAND}, source(), UNTIL_YOUR_NEXT_TURN)` |
| SetSelfCantBeBlockedIndefinitelyEffect | absorbed: `GrantEffectToTargetEffect.toSource(StaticAbility.of(CantBeBlockedEffect), PERMANENT)` (same layer-6 payload) |
| SetSelfPermanentSupertypeIndefinitelyEffect | absorbed: `SetTargetPermanentSupertypeEffect(LEGENDARY, true, source())` |

**Behaviour changes.**
- Hydro-Man loses its creature types while it is a land (CR 205.1a); new test.
- Sway of Illusion asks for exactly one colour with no DONE; new test.
- A floated empty colour set now makes the permanent colorless (Xathrid Gorgon, harness `setColors()`).
- Logs are the survivors' lines: "becomes Blue.", "all colors", the grant line, "becomes legendary.".

**Tests changed.** 2 added. `@DisplayName` added to all methods of `SurgeEngineTest` and `HydroManFluidFelonTest`.

**Left.**
- `pt-amount` (8): the survivor's resolution path needs four things.
- A null side (it evaluates every amount; `evaluate(null)` would throw).
- The live source on the target path (it passes `null`, so `SourcePower` reads the snapshot).
- A target spec for its target forms.
- Two last-known reads settled: Shape Stealer's combat-opponent-at-trigger fallback, and Eldrazi Mimic's departure snapshot over `TriggeringPermanentPower`'s at-trigger value. The amounts would yield 0/0 where the old handlers did nothing.
- Riptide Mangler, Sentinel, Wall of Tombstones, Sworn Defender, Halfdane and Wolfbear/Galion are by parameters once the first three exist (`TargetPower(true)` keeps negatives).
- `base-pt-new` (2): Cycle of Life is `SetBasePowerToughnessEffect(0, 1, target(), UNTIL_CONTROLLERS_NEXT_UPKEEP)` + `RegisterDelayedStepTriggerEffect.aboutAtNext`. Brine Hag needs the damaged-the-source predicate.
- `floated-name` (1): removing `Permanent.persistentName` touches The Irencrag's record and two tests' setters.

**Seen, not changed.**
- `AllPermanentsGainChosenColorEffect` (Painter's Servant) and `ControlledNonlandPermanentsAreColorEffect` (Celestial Dawn) are now `GrantColorEffect` parameters.
- `BecomeColorlessUntilEndOfTurnEffect`, `SetTargetColorEffect` (its colorless branch), `SetChosenColorUntilEndOfTurnEffect` and `GrantColorUntilEndOfTurnEffect` overlap the survivor (step 10).
- `SetCardTypesUntilEndOfTurnEffect`'s layer case has the same subtype gap Hydro-Man had (CR 205.1a). Sure.

**For the next run.** Take `pt-amount` with the four pieces above first, then `base-pt-new`. Put `floated-name` in a run of its own with The Irencrag.

### Step 9, fold run 30 (2026-11-24, parallel worktree)

Five batches touched, two left. Ratchet 3,572 / 2,853 → 3,565 / 2,848. Ledger 16 rows: absorbed 7, survivor 3, blocked-other 4, unique 1, step-9 1. 10 card files rewritten, Oracle text checked for 9. Rulings read for Bolas's Citadel, Gwenom and Ichor Aberration. CR 118.6, 118.9, 118.9a, 305.2, 400.7, 406.3, 514.2, 601.2f, 601.3, 607.2a, 608.2g, 609.4b, 611.2a, 611.2c, 702.37c, 708.4 and 716.3 checked.

| Batch | Result |
|---|---|
| lib-top | 3 folded, 2 left. `AllowCastFromTopOfLibraryEffect` gains `alternativeCost` (`payingLifeEqualToManaValue()`, PayLife only) and `anySpell()` / `SPELL_TYPES`. The Belligerent and Gwenom float three `GrantStaticEffectToPlayerEffect.untilEndOfTurn` grants (look, `PlayLandsFromTopOfLibraryEffect`, the cast permission). `CastingPermissionService.libraryTopGrants` gives every reader the player's floating copies first, then the permanents' statics. Left: Windriddle Palaces and Xanathar (see For the next run). |
| gy-grants | 2 folded, 1 unique. The survivor gains `graveyards` (`fromGraveyardsOf`: you, one player, or every player), `exilingInsteadOfGraveyard()` and `spendingManaOfAnyType()`. Shaman's Trance stays `unique`: it treats other graveyards as yours for every permission, which a cast filter grant can't say. |
| src-gy-perm | 1 folded, 1 blocked. The survivor gains `lasting(Duration)` and `asAdventure()` (CR 716.3), over `sourceCard()` (Hildibrand, Mosswood). Ichor Aberration waits on the owner's "it" question. |
| exiled-as-cost | Primordial Mist moved to `AllowPlayCardsFromExileEffect.thisTurn(exiledAsCost())`; the record's this-turn mode is gone. The record stays for Chitinous Crawler's play-now offer. |
| gy-statics | 1 of 6 folded: Sarcophagus → `CastSpellsFromGraveyardEffect(new CardHasCyclingPredicate())`. Raul is not folded (below); the other four were not started. |
| exiled-with-src, spend-mana | not started |

**Behaviour changes.**
- The Belligerent may cast a morph card face down from the top (CR 702.37c); the float is now a normal permission (new test).
- Gwenom's top spell is offered when life pays for it with no mana (new test).
- Bolas's Citadel offers and casts a no-mana-cost top card for 0 life (CR 118.9; new test).
- The library-top flash grant skips face-down and ability-less permanents (Elsha; new test).
- The library-top counter-cost label is shown when the life alternative can't be paid.
- Adventure graveyard grants log the survivor's line.
- A card-subject graveyard grant covers only cards in your graveyard; no card changes.
- Floating library-top grants have no source permanent id (`GrantStaticEffectToPlayerEffect`).

**Seen, not changed.**
- Whispersteel Dagger and Xanathar say "as though it were mana of any color", but both use any-type spending, so coloured mana pays {C} (CR 609.4b). Same as the open spend-mana defect. Fairly sure.
- Graveyard permission filters are matched with `matchesCardPredicate(card, filter, null)`, with no GameData and no owner. Game-state card predicates (milled this turn, put there from a library this turn) can't be used in them, which blocks Raul's predicate fold. Sure of the code.

**For the next run.**
1. Windriddle and Xanathar need a library-owner `PlayerSubject` on `AllowCastFromTopOfLibraryEffect` / `PlayLandsFromTopOfLibraryEffect`, read from face-up planes. Xanathar's floating copy also needs a player bound at resolution (CR 611.2c) and its any-colour option.
2. exiled-with-src should fold together with the unaudited `AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect` (4 cards). The open-set option (CR 611.2c) would then have 6 users; Urianger's {2} reduction is its own.
3. gy-statics: for Raul, first pass GameData and the owner into the static graveyard filter matching. Eye of Duskmantle needs `CastSpellsFromGraveyardEffect` to carry the interface's `alternateCost`. Ninja Teen, Coram and Jaya's emblem are not started.
4. spend-mana: all 8 rows.

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
