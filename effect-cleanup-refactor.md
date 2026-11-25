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
| 8 | Split the fused effects (repeatable) | done (runs 1 to 29; "counter unless": option B now, option C as a generic "unless" record in step 10, owner's decision 2026-11-25) |
| 9 | Sweep the single-card tail (repeatable) | open. Runs 1 to 41 closed Exile, Each, Return, Put, Reveal, Choose, Grant, Create, Prevent, May, Register and Target. From 2026-11-23 the step runs as triage runs, then fold runs (see step 9). Triage runs 1 to 7 and parallel chunks 01 to 16 gave every rare record a ledger verdict (none is left unaudited); defect runs 1 and 2 fixed most of the defects triage runs 1 to 5 saw. Their results are merged under Standing knowledge: the **Fold queue** (1,322 `step-9` rows in 299 batches, 51 proposed fold runs), the **Defect queue** (220 lines, 12 proposed defect runs) and **Re-triage and stale rows**. Defect run D10 applied the owner's hidden-zone logging decision and re-verdicted its 73 rows (71 `step-9` in fold runs 12, 12a, 12b and 20). Fold runs done: 1, 2, 3, 4, 5, 6, 6b, 7, 8, 9, 10, 11, 9b, 12, 11b, 10b, 12a, 14, 13, 12b, 14b, 13b, 14c, 12c, 8b, 16, 15, 12d, 17, 16c, 18, 16b, 15b, 17b, 19, 18b, 19b, 20, 17c, 21, 12e, 19c, 22, 17d, 18c, 21b, 22b, 19d, 21c, 23, 25, 28, 31, 30, 26, 23b, 25b, 28b, 31b, 29, 26b, 34, 23c, 33, 29b, 27, 32, 28c, 34b, 23d, 35, 33b, 36, 29c, 27b, 32b, 35b, 29d, 38, 36b, 35c, 41, 39, 40, 43, 37, 42, 41b, 44, 45, 46, 37b, 41c, 40b, 44b, 45b, 46b, 47, 49, 24, 7b, 30b, 6c, 46c, 47b, 24b, 30c, 49b, 47c, 24c, 48, 8c, 7c, 8d, 50, 48b, 8e, 50b, 50c (each in Findings; the fold queue marks its rows). Step-9 fold queue closed with fold run 50c (no step-9 rows left; blocked-other rows stay for their missing pieces). Defect runs D11 to D22 (one per group) and D23 (the owner's perpetual "it" decision) done 2026-11-25; the defect queue keeps the lines left, each with what it needs. Next: step 10 (needs the owner's sign-off per verb). Waiting on the owner: 2 rows (gift opponent, deferred), Tribute's reduced-placement reading (CR 702.104b), "if you do" after a replaced draw (D14 L17), Nowhere to Run's any-player hexproof option (D16 L12), set exchanges under CR 701.12a (D19 L22), Reality Twist on unlisted types and Boseiju's rider under a land-mana replacement (D21), and a seeded game random source.  |
| 10 | Collapse the head: one record per verb (repeatable, needs sign-off) | open. Run Counters done 2026-11-25 (`PutCountersEffect`, the target record renamed, absorbs the two self records). Run Remove counters done 2026-11-25 (`RemoveCountersEffect`, the source record renamed, absorbs the target, plural-target and two mass records). Defect run D24 (2026-11-25) cleared the Damage run's prevention-path difference (plan section 2 (a)): one permanent-damage path. Run Damage done 2026-11-25 (`DealDamageEffect`, the any-target record renamed, absorbs the creature, creature-or-planeswalker, player-or-planeswalker and permanent records; 1,614 card files). Next: "unless" (owner's order) |
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
| `EffectCountRatchetTest` baselines (records / handler classes in `normalfx`, `mayfx`, `staticfx`) | 2,992 / 2,418 (after step 10 run Damage) |
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

`scripts/effect-audit-ledger.csv` (header `class,verdict,step,note`): 6,775 rows over 4,581 classes (after fold run D23). Latest verdict per class: unique 1,195, absorbed 1,362, split 691, blocked-other 560, survivor 460, blocked-step-model 140, blocked-player-loop 95, dead 78. Waiting: 0 `step-9` rows for the fold runs still open; 2 rows on the owner's deferred gift opponent and about 20 "counter unless" rows on step 10's "unless" run (after Counters and Damage).

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
| Perpetually | 27–29, P16, D10 | triaged: the hand and library records are in fold run 12b since D10; the "it" records done in defect run D23 |
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
(195); the damage records (done: run Damage, `DealDamageEffect`, 1,614 card files); the counters pair (done: run Counters, `PutCountersEffect`). One run merges one verb into one record with static
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
  token step) and `Duration.PERMANENT` (no stated duration, CR 611.2a); `DealDamageEffect.to(thisWay(kind), n)` (the
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
- "Remove all [kind] counters from this. If no counters were removed / Otherwise": `RemoveCountersEffect.fromSelf(kind,
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
- `PutCountersEffect.placedByEachController()`: "target player puts / each opponent may put a counter on each creature they control"; the permanent's controller is the placer (Shadrix, Tempt with Glory; fold run 19c).
- `PutCountersEffect.onSelf(kind[, int | DynamicAmount])`, `.onSelfCountingTargetPlayer(kind, amount)`, `.onSelfWithTargetPermanent(kind)`:
  "put N [kind] counters on this" (`Subjects.source()`, the self-targeting marker kept; a planar ability's face-up plane), through the
  one placement: counter events, the CR 616.1 order, PUT_COUNTERS, the source snapshot refreshed. A `MayEffect` over it asks nothing when
  the source can't have the counters, or is gone and an else follows (CR 608.2d); gone with no else, it still asks (Kresh). The mana-ability
  path places it inline (Pyramid of the Pantheon) (step 10 run Counters).
- `RemoveCountersEffect.fromSelf(kind, int | DynamicAmount)`, `.fromSelfCountingTargetPlayer(kind, amount)`, `.fromTarget()` (any one
  kind), `.fromTarget(kind, int | DynamicAmount[, targetPredicate])`, `.fromTarget(kind, targetPredicate)`, `new RemoveCountersEffect(subject,
  kind[, int])` (`attached`, `granter`, `thisWay(CHOOSE)`, `controlledBy(...)` sets): "remove N [kind] counters from this / target / each
  ...". One removal path (`PermanentCounterSupport.removeCounterFromPermanent`): vanishing's last time counter (CR 702.63a), oil, the
  controller's LAST_REMOVED event per permanent (CR 603.2c), a Siege's defeat after its last defense counter (CR 310.12b), REMOVE_COUNTERS
  recorded after the kind is chosen (`ANY`: a spliced mode choice). "Up to N from target" is `RemoveUpToCountersFromAllPermanentsEffect(kind,
  N, target())` (Gremlin Mine) (step 10 run Remove counters).

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

- `TargetCreatureDealsDamageToControllerEffect.dealtBy(dealers, amount, player)`: any non-targeting dealer, players per dealer (`controllerOf` / `ownerOf(dealers)`) or found once; dealers as last known (fold run 26).
- `DamageSupport.dealerAsItIsOrWas` / `dealerControllerId` / `entryDealtByPermanent`: a permanent deals damage as itself, live or last known (CR 120.1, 608.2h) (fold run 26).
- `TargetCreatureDealsPowerDamageToEachOtherCreatureEffect(dealer, recipients)` and `TargetDealsPowerDamageToTargetEffect` with a set dealer (`thisWay(CHOOSE)`, `controlledBy(...)`, `except(…, targetDeclaredElsewhere())`) (fold run 26).
- `DealDamageToPlayersEffect.eachEvaluatedFor(amount, players)`: per-player amounts, all fixed before damage (CR 608.2h) (fold run 26).
- `PlayerWithoutMaxSpeedPredicate` (CR 702.179e); `PermanentHasLeastToughnessAmongOpponentCreaturesPredicate(CountScope)` with OPPONENTS, CONTROLLER or ANY_PLAYER (fold run 26).

- `RemoveAllCountersFromTargetPermanentEffect.fromPlayers(players[, PlayerCounterType])` / `.fromPermanentsAndPlayers(permanents, players)`: "[players] lose all [kind] counters", "remove all counters from up to one target permanent or opponent"; one REMOVE_COUNTERS step (total, permanents, controllers, players) that also keeps the event value (fold run 23b).
- `PlayerCounterType` (model) + `GameData.playerCounterCount` / `removeAllPlayerCounters`: the five tracked player counter kinds and their stores (fold run 23b).
- `CantGetCountersEffect.onPermanents(subject, duration)` / `.onPlayers(players, duration)`: "can't have counters put on it / can't get counters for as long as …", a floated rule (not an ability) read by `cantHaveCounters` and `GameQueryService.playerCantGetCounters` (fold run 23b).

- `AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect` with `.anotherSource()`, `.fromSources(PermanentPredicate)`, `.fromAnyonesSources()`, `.fromSpells()`, `.to(players, permanents)`, `.toOpponentsOnly()`, `.toAnyPermanentOrPlayer()`: every static "deals that much damage plus N"; floated "this turn" by `GrantStaticEffectToPlayerEffect.untilEndOfTurn` (fold run 25b).
- `GameQueryService.getAdditionalDamageBonuses(gameData, additionalDamageEvent(entry, dealingPermanent, sourceCard, controller, dealtByEntry, combat, recipientPlayer, recipientPermanent))`: the one plus-N reader. It reads active static effects and floated copies, judges the source as it deals damage (colour leaves read through Ghostly Flame, a departed source by its last known information) and gives one option per instance (fold run 25b).

- `SubjectResolutionService.bindChosenValues(gd, entry, effect, PermanentPredicate | CardPredicate)`: writes the resolution's chosen type, card type or player (`PermanentControlledByPlayersPredicate(PlayerSubject)`) and the source's as-enters subtype or card type (as it is or last was) into a rule's or grant's filter as it is registered (CR 608.2h, 611.2c). Used by `MatchingCreaturesMustAttackThisTurnEffect`, `MakeCreatureBlockableOnlyByFilterThisTurnEffect`, `GrantFlashToNextMatchingSpellEffect` and `GrantFreeCastForNextMatchingSpellEffect` (fold run 28b).
- `PlayerHasMostLifePredicate` ("a player with the most life or tied", with `matching(eachPlayer(), …)`) and `PermanentControlledByPlayersPredicate(PlayerSubject)` ("creatures that player controls", bound) (fold run 28b).
- `GrantFlashToNextMatchingSpellEffect(CardPredicate)` / `.nextSpell()`: "the next [matching] spell you cast this turn can be cast as though it had flash", resolving itself (fold run 28b).
- `CopyTargetActivatedOrTriggeredAbilityEffect.times(DynamicAmount)` / `.thatCantBeCopied()`; `CopyControllerActivatedAbilityEffectHandler.copy(gd, entry, original, controller, ability)`, the one ability copier; `CopyAbilityRetargetEffect([ActivatedAbility])`, the one retarget marker (fold run 28b).

- `SetBasePowerToughnessToAmountEffect` with a null side, the live source on `target()`, and a benign target declared by `target()` or a target-reading amount: "this creature's base power becomes equal to target creature's power", "target creature's base P/T become this creature's" (fold run 31b).
- `SourceToughness(true)` / `TargetToughness(true)` / `TriggeringPermanentToughness(true)`: keep negative toughness for P/T setters (CR 107.1b) (fold run 31b).
- `PermanentDealtDamageToSourceThisTurnPredicate`: "each creature that dealt damage to it this turn", the source judged as it last existed (CR 603.10a; Brine Hag) (fold run 31b).

- `CopyTriggeringSpellEffect.copiedBy(players)` / `.mayBeCopiedBy(players)`: "each other player [may] copy that spell" (`opponentsOf(controllerOf(triggeringSpell()))`), "target opponent may also copy that spell" (`targetPlayer()`); copiers in APNAP order (fold run 29).
- `CopyTriggeringSpellEffect.targetingEach(PermanentSubject)`: "copy the spell for each other [permanent] that spell could target, each copy targeting a different one", "the copy targets the chosen permanent" over `thisWay(CHOOSE)` (fold run 29).
- `ChoosePermanentsEffect.thatTheTriggeringSpellCouldTarget()` and `ValidTargetService.permanentIdsSpellCouldTarget(gameData, spell)` (fold run 29).

- `TargetSpec.chosenAtRandom` / `atRandom()`: "target … chosen at random", read by `CardEffect.targetChosenAtRandom()` at the stack door and the activation path; it fires "becomes the target" abilities and the crime event (CR 601.2c, 603.3d, 700.13) (fold run 26b).
- `Subjects.targetPlayerChosenAtRandom()` (`PlayerSubject.TargetPlayer.chosenAtRandom`): "target opponent chosen at random", "select target player at random", for `DealDamageToPlayersEffect`, `DiscardEffect` and `GainControlOfTargetEffect.gainedBy`; opponent-only through the card's target filter (fold run 26b).
- `DealDamageEffect.toAnyTargetChosenAtRandom(amount)` and `SourceFightsTargetCreatureEffect.atRandom()`; the survivor's fight form goes through `FightSupport.fight` (fold run 26b).

- `SubjectResolutionService.namesPlayer(gameData, source, controller, player, subject)`: also judges `controllerOf(attached())`, "enchanted creature's controller" (fold run 34).
- `OpponentsCantCastSpellsMatchingPredicateEffect`: amounts bound for the casting player; {X} spells are offered unless every X is stopped; `AmountEvaluationService.bindAmounts(…, asSpell)` and `bindTriggeringSpellManaValue(…, asSpell)` count X (fold run 34).
- `PlayerRestriction.castingSpellsMatching(filter)`, `.playingCardsFrom(zones)` (PLAY_CARDS), `.from(zones)` / `.fromAnywhereBut(zones)`; `stopsSpellsFrom`, `stopsSpellFrom(zone, creature)`, `stopsLandsFrom` (fold run 34).
- `PlayersCantCastSpellsFromZonesEffect(PlayerSubject, PlayerRestriction)`: the static restriction; `GameQueryService.isPlayerRestricted(gd, player, predicate)` reads it with the resolved restrictions (fold run 34).
- `CantSearchLibrariesEffect(players, payableToIgnore)`, floated by `GrantStaticEffectToPlayerEffect`, a floated "you" being its player (fold run 34).
- `PlayersCantPayLifeEffect(includingManaAbilities, sacrificeFilter)`: every "can't pay life [or sacrifice X] to cast or activate" (fold run 34).

- `RemoveUpToCountersFromAllPermanentsEffect(kind, maxAmount, PermanentSubject)` / `.anyNumber(kind, permanents)`: "remove up to N / any number of [kind | ANY] counters from among [permanents]", one how-many question per permanent and kind, counters removed together, records REMOVE_COUNTERS (fold run 23c).
- `ChoiceContext.MoveCountersAmountChoice.totalLimit` and removal slots (`CounterMoveSlot.toPermanentId == null`), answered by `RemoveUpToCountersFromAllPermanentsEffectHandler.answer` (fold run 23c).
- "Remove a [kind] counter from each of any number of [permanents]": `ChoosePermanentsEffect.anyNumber(set, label)` + `new RemoveCountersEffect(thisWay(CHOOSE), kind)` + `AmountThisWay(REMOVE_COUNTERS)` (Chandra, Legacy of Fire; Garnet; fold run 23c).

- `ReplaceLandManaWithColorEffect` (`colorOfYourChoice()`, `.forLands` `.youControl` `.whenYouTap` `.replacing` `.producing` `.on(subject)` `.lasting(Duration)`): every "if a land is tapped for mana, it produces X instead" replacement, static or floated, read by `GameQueryService.landManaColorReplacements` (fold run 33).
- `TwistBasicLandManaColorsEffect(Map<CardSubtype, ManaColor>)`: the basic-land type to colour twist (fold run 33).
- `DrainTargetPlayersLandManaEffect(PlayerSubject, delayedColorless)` / `.forDelayedColorless(player)` (fold run 33).
- `ManaColorLandScope.SACRIFICED_FOR_COST` / `UNTAPPED_FOR_COST` on `AwardManaOfTypeLandsCouldProduceEffect` (fold run 33).
- `AwardManaToActivePlayerEffect.to(PlayerSubject)`: "that player / target player adds" (fold run 33).
- `AwardHasteGrantingManaEffect.Rider` (`HASTE_UNTIL_END_OF_TURN`, `PERMANENT_HASTE`, `INSTANT_OR_SORCERY_CANT_BE_COUNTERED`) (fold run 33).

- `Subjects.lastAttachedCreature()` (`PermanentSubject.Attached(creature, lastAttached)`): "the creature it was last enchanting" for a source that left unattached, read from the stack entry's recorded host; only for cards whose ruling says so (Bind the Monster). Plain `attached()` names nothing then (CR 608.2h, 704.5m; Followed Footsteps, Mechanized Production rulings) (fix after fold run 26).

- `CopyImprintedCardAndMayCastCopyEffect.of(cards)` copies cards a subject names in exile, a graveyard or a hand (`of(cardsThisWay(CHOOSE))` after a revealed-hand choice); `.castingUpTo(n)` / `.castingAnyNumber()`: "you may cast up to N / any number of the copies", one ordered choice among free copies (CR 608.2g) (fold run 29b).
- `ExileCardAtRandomEffect.withSource()` and `ExileCardsFromTopUntilEffect.withSource()`: the exiled cards are exiled with the source permanent for a linked ability (CR 607.2a) (fold run 29b).
- Cipher's granted trigger is `CopyImprintedCardAndMayCastCopyEffect.of(Subjects.exiledCards(new CardIdSetPredicate(Set.of(id))))`, the pattern for an engine-built copy of one known exiled card (fold run 29b).

- `RedirectYourDamageToTargetCreatureThisTurnEffect.to(destination, RedirectedDamage)` + `.lasting(d)` / `.combatDamageOnly()` / `.dealtBy(source)` / `.targeting(filter)` / `.orPlaneswalkerDestination()`: every "all damage that would be dealt [this turn] to you / your permanents / your creatures is dealt to [destination] instead"; with `ChoosePermanentsEffect` + `thisWay(CHOOSE)` for a chosen destination (fold run 27).
- `RedirectNextDamageEffect.allThisTurn(protected, destination, target)`: "all damage that would be dealt to [permanent] this turn is dealt to [destination] instead" (fold run 27).
- `DamageCantBePreventedEffect()` / `.combatDamage()` / `.dealtBySource()` / `.combatDamageDealtBy(controlledBy(you(), filter))`, read by `GameQueryService.isDamagePreventable` / `isCombatDamageCantBePrevented` / `damageCantBePreventedFromSource` (fold run 27).
- `PreventDamageAndAddPlusCountersEffect.replacingWith(type)` / `.preventingAndPutting(type)` / `.preventingAndPuttingPerPrevented(type)`: damage to +1/+1 or -1/-1 counters, CR 615.12 per kind (fold run 27).
- `GrantStaticEffectToPlayerEffect.untilEndOfTurn(new ReplaceDamageAboveThresholdEffect(t, n))`: "this turn" threshold replacement (fold run 27).

- `LoseAllLandTypesEffect.subtypes(Set<CardSubtype>, subject, Duration)` / `.supertype(CardSupertype, subject, filter)`: "no longer an Equipment", "isn't a Human" (resolved, floated; an Equipment that stops being one is unattached) and "all lands are no longer snow" / "isn't legendary if it's a token" (layered pass, timestamp order) (fold run 32).
- `GrantSubtypeEffect.chosenSubtype(subject, overriding, filter)` / `.secondChosenSubtype(...)` (`ChosenSubtype`, `subtypeFor(source)`): "enchanted land is the chosen type", "this land is the chosen type [in addition]", Illusionary Terrain; an overriding basic land type over any subject is the CR 305.7 setter (Celestial Dawn, Conversion) (fold run 32).
- `GrantBasicLandTypeToTargetEffect.of(lands, fixedSubtype, replacing, Duration)`: a set of lands found once (CR 611.2c) with one choice; `AddBasicLandTypeChoice(landIds, …)` (fold run 32).
- `LosesAllAbilitiesEffect.exceptManaAbilities(subject)` / `losesEveryAbility()`: "loses all abilities except mana abilities" (Blood Sun, Town-Razer Tyrant) (fold run 32).

- `CardPredicateUtils.instantOrSorcery()` / `.instantOrSorceryWithManaValueAtMost(n)`: "an instant or sorcery spell [with mana value n or less]" (fold run 28c).
- `RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(filter, List.of(new CopyTriggeringSpellEffect()[.withCopies(…)]))`: "when you next cast a [filter] spell this turn, copy that spell [n times]". It works in a mana ability too (Ether) (fold run 28c).

- `OpponentsCantCastOrActivateDuringYourTurnEffect(players, spells, abilities, condition)` / `.onlyDuringTheirOwnTurns(players, abilities)` / `.onlyAtSorceryTiming(players)` / `.duringCombat(players)`: every static cast and activation lock, its condition judged with the restricted player as the context's target (fold run 34b).
- `PlayerRestriction.activatingAbilities()` / `.activatingAbilitiesOf(types…)` (`Kind.ACTIVATE_ABILITIES`, `abilitySourceTypes`): "can't activate abilities [of artifacts, creatures, or enchantments]", mana abilities included, permanents only when typed (fold run 34b).
- `GameQueryService.findRestriction` (returns `RestrictionInForce`), `restrictedSpellTypes`, `abilityLockRefusal(player, source)`, `nonManaAbilityLockRefusal`, `isHeldToSorceryTiming`: the restriction readers over resolved and static restrictions (fold run 34b).
- Conditions `StackIsEmpty` and `TargetPlayerCouldCastSorcery` (CR 307.5, timing only); `TargetPlayerTurn` read for the restricted player (fold run 34b).

- `RemoveUpToCountersFromAllPermanentsEffect` with `(kind, DynamicAmount, subject)`, `.exactly(kind, amount, subject)`, `.matchingTarget(filter)` and `.orFromPlayers(players)`: "remove up to N / N / any number of counters from target permanent or player". Exact X removes all when fewer are there, and one-answer slots are answered unasked (fold run 23d).
- `ChoiceContext.MoveCountersAmountChoice.requiredTotal` (lowest answer per slot) and `CounterMoveSlot.fromPlayer(playerId, PlayerCounterType)` (player removal slots); `PlayerInputService.beginMoveCountersAmountChoice(…, min, max)` (fold run 23d).
- `GameData.removePlayerCounters(playerId, PlayerCounterType, amount)`: removes N counters of one kind from a player, energy counting as lost (fold run 23d).
- `ChoiceContext.RemoveOneCounterChoice.counterLabel(CounterType)`: the counter-kind option label, moved from the deleted `RemoveChosenCountersChoice` (fold run 23d).

- `SkipDrawStepEffect(SkipStepOrPhaseKind, PlayerSubject)` (`UPKEEP_STEP`): static "skip your draw / upkeep step", floated "skip the chosen step or phase this turn", read by `skipsDrawStepByStaticAbility`, `playersSkipUpkeepStepApplies` and `GameData.skipsStepOrPhase` (fold run 35).
- `SkipDrawsEffect(PlayerSubject)`: "that player skips that draw instead", read by DrawService over active statics (fold run 35).
- `SetControllerMaximumHandSizeEffect(players, Change, DynamicAmount)` / `.set` / `.increase` / `.reduce` / `.fixedAt`: every maximum hand size change, static or resolved (fold run 35).
- `PlayWithHandsRevealedEffect(PlayerSubject)`: static or floated hand reveal, judged per hand (fold run 35).
- `GrantStaticEffectToPlayerEffect` with `PERMANENT` and `WHILE_SOURCE_REMAINS` (source-linked; nothing begins if the source left, CR 611.2b) (fold run 35).
- `GrantProtectionFromPlayerEffect(from, permanents, duration)`: "you and [permanents] gain protection from [player]"; floated `ProtectionFromPlayersEffect` is read by `playerHasProtectionFromOpponents` (fold run 35).
- `IgnoreOpponentHexproofEffect(opponents, Breadth)` / `.opponentsCreatures()` / `.opponentsAndTheirCreatures()`: the one hexproof-ignore, static or floated (fold run 35).
- `GrantControllerKeywordUntilEndOfTurnEffect(keyword, PlayerSubject, duration)` / `.eachPlayer(keyword)`: player keywords shroud, hexproof, flying (fold run 35).

- `AddManaOfTypeProducedByTappedPermanentEffect` (`()`, `(ManaColor)`, `ofSourceChosenColor()`, `.toItsController()`): every "whenever you tap / a land is tapped for [type / the chosen colour] mana, add one [more] of it"; reads only the produced types and waits for the land's own mana choice (fold run 33b).
- `AwardPersistentManaEffect` (`(ManaColor, amount[, Recipient])`, `anyOneColor(amount)`, `inAnyCombinationOf(colors, amount)`, `.spendOnlyToCastSpells()`): every "until end of turn, you don't lose this mana as steps and phases end" (fold run 33b).
- `ChoiceHandlerService.handleSpellOnlyManaColorChosen` resolves the tap triggers waiting on the produced mana type (Piracy) (fold run 33b).

- `CantBlockThisTurnEffect(subject, Duration)`: `UNTIL_END_OF_COMBAT` is "can't block this combat" (fold run 36).
- `CantAttackThisTurnEffect(triggering(), DURING_AFFECTED_PLAYERS_NEXT_TURN)`: "that creature can't attack during its controller's next turn". `except(allCreatures(), targetDeclaredElsewhere())`: "other creatures can't attack this turn" (fold run 36).
- `SubjectResolutionService.lastingRule` binds chosen values; `bindChosenValues` binds `PermanentHasChosenParityPredicate(MANA_VALUE)` (fold run 36).
- `GrantEffectToTargetUntilEndOfTurnEffect(ability, withCreaturesSharingAColor(target()))` (fold run 36).
- `CantAttackOrBlockAloneEffect` (`unlessOthersAlsoDo`, `unlessGreaterPowerAlsoDoes`, `cantAttackUnlessAlsoAttacks`) plus `CombatCompanionRestrictions`, also exposed as `CombatAttackService.hasUnmetCompanionRestriction` (fold run 36).
- `CreaturesCantAttackControllerUntilNextTurnEffect(PlayerSubject attackers, PlayerSubject protectedPlayers, boolean protectsPlaneswalkers, Duration)` (fold run 36).
- `OpponentsMustAttackControllerEffect.attackingAnyone()` (fold run 36).
- `CreaturesWithCounterAttackTogetherEffect(attackers, required)`: `ifSourceAttacks()`, `ifAnotherYouControlAttacks()`, `opponentsAttackTogether()` (fold run 36).

- `ExileTargetSpellEffect.triggeringSpellWithSource()` (`spell` subject `targetSpell()` or `triggeringSpell()`): a spell-cast trigger's non-target "[that player] exiles it", only while the spell is on the stack, exiled with the source (Possibility Storm; fold run 29c).
- `MayCastDiscardedCardWithoutPayingManaCostEffect.castBy(PlayerSubject)`: "that player / they may cast that card"; the graveyard check is the caster's own graveyard (Possibility Storm, Tibalt's Trickery; fold run 29c).
- `SubjectResolutionService.bindResolutionFilter(gd, entry, effect, filter)`: card subjects plus the triggering spell's types, name and mana value, bound as the step resolves. Read by the exile-until stop, the reveal-until predicate and the cast-now filter, so "that card" is `cardsThisWay(EXILE)` with the step's own stop (fold run 29c).
- `CardSharesCardTypeWithPermanentsPredicate(PermanentSubject)` + `SubjectResolutionService.cardTypes`: "shares a card type with the sacrificed permanent", read live or from the removal step's `DepartureResult.lastKnownCardTypes` (Reweave; Reality Scramble once its put-on-bottom step records a departure; fold run 29c).
- `TargetPolarityClassifier.tradesTheTargetForAnother`: an edict on the target followed by a reveal-onto-battlefield for its controller is NEUTRAL (Shape Anew, Reweave; fold run 29c).

- `ChooseDamageSourceEffect.any()` / `.youControl()`: "choose a source [you control]" as a CHOOSE step over CR 609.7a's candidates (`DamageSourceChoiceSupport`: candidates, controllerOf, sourceCard). A lone source is chosen unasked; read with `thisWay(CHOOSE)` via `SubjectResolutionService.damageSourceIds` (fold run 27b).
- `RedirectNextDamageFromChosenSourceToSelfEffect.to(source, permanent)` / `.toPlayer(source, player)` / `.toItsController(source)` + `.dealtTo(players)` / `.allThisTurn()` / `.combatDamageOnly()`: every source-keyed redirection. One `SourceKeyedRedirectShield` per source, read by `DamagePreventionService.applicableSourceKeyedRedirects` / `applySourceKeyedRedirect` (CR 614.9) (fold run 27b).
- `PreventNextDamageByTargetCreatureEffect.preventing(source)` / `.doubling(source)` + `.combatDamageOnly()`: "the next time [source] would deal damage this turn, prevent it / it deals double instead", with `target()`, `triggering()` or `thisWay(CHOOSE)` (fold run 27b).
- `SubjectResolutionService.damageSourceIds(...)`: the ids of damage sources a subject names, a chosen spell and `targetSpell()` included (fold run 27b).

- `AnimateNoncreatureArtifactsEffect(PermanentSubject subject, PermanentPredicate filter, boolean losesAllAbilities)` / `.otherNonAuraEnchantments()`: "each [filter] permanent is a creature in addition to its other types with base P/T equal to its mana value". The filter is judged in layer 4, and layers 6 and 7b apply to that set (CR 613.6) (fold run 32b).
- `LayerSystemService.LayeredBoardState.staticAnimations` + `animatedBy(effect)` / `recordStaticAnimation(effect, id)`: the permanents each multi-layer static animation instance animated in layer 4, copied into dependency trials (fold run 32b).
- `GameQueryService.isAnimatedByPrintedStaticAnimation(gameData, permanent, ownCardId, skipSourcesWithoutAbilities)`: the creature check for a permanent with no layered state (fold run 32b).

- `EnterPermanentsOfTypesTappedEffect(types, PlayerSubject players, filter, castOnly)` / `.matching(filter, players)`: static "[permanents] enter tapped" for `eachPlayer()`, `eachOpponent()` or `enchantedPlayer()`, with filters judged against the source; floated on a player for "this turn" (Due Respect, Nahiri's Lithoforming) (fold run 35b).
- `PermanentHasSourceChosenParityPredicate(chosenQuality)`: "with / without mana value of the chosen quality", the source's as-enters odd or even, in static and dynamic evaluation (fold run 35b).
- `SkipDrawStepEffect(UNTAP_STEP, players)`: static "players skip their untap steps", planes included, read by `UntapStepService.playersSkipUntapStepApplies` (fold run 35b).

- `CastCardWithoutPayingManaCostIfAbleEffect(CardSubject)`: "[the player] casts it without paying its mana cost if able", the mandatory cast of one exiled or hand card by the player whose zone it is in. After `PutCardsOntoBattlefieldEffect(cardsThisWay(kind, land))` for "if it's a land card … Otherwise" (Omen Machine, Wild Evocation; fold run 29d).
- `SpellCastingService.payFreeCastCostIncreases` / `canPayFreeCastCostIncreases(gd, player, card, zone)`: the one cost-increase payment of a free cast (CR 601.2f), used by the exile, queue and library free casts (fold run 29d).

- `GainActivatedAbilitiesOfOwnCreaturesEffect(PermanentSubject, PermanentPredicate, Abilities)` / `.of(subject[, filter])` / `.loyaltyAbilitiesOfOtherPlaneswalkers()` / `.exceptLoyaltyAbilities()`: every "has all [loyalty] activated abilities of [permanents]" (fold run 38).
- `GainActivatedAbilitiesOfOwnCreaturesSelfEffectHandler.effectiveActivatedAbilities`: the one borrower read, moved from the deleted opponents' handler; a permanent being assembled lends only its card's abilities. `GameQueryService.isAssemblingStaticBonus(id)` (fold run 38).
- `SpendManaAsAnyColorForActivatedAbilitiesEffect.forBorrowedAbilities()`, read by `GameQueryService.isBorrowedAbility` (fold run 38).
- `StaticEffectSupport.namesOnBattlefieldOf(context, permanent, controllerId, subject)`: static membership for a scan of other permanents (fold run 38).
- `GainControlOfTargetEffect.whileMatches(subject, predicate)` and `CreatureControlService.maintainedPredicateHolds`: "for as long as it [matches]" (CR 611.2b) (fold run 38).
- `PermanentIsHauntedPredicate`; `DealDamageToPlayersEffect` over `ownerOf(target())` declares the permanent target (fold run 38).
- `PlayerHasLeastLifePredicate`, `PlayerHasMostCardsInHandPredicate`, `PlayerSeatedInDirectionPredicate(direction)`, `PlayerControlsMostCreaturesPredicate(creatureFilter)` (fold run 38).
- `IgnoreLegendRuleEffect(PermanentSubject)` and `IgnoreLegendRuleWhenExactlyTwoSameNameEffect(PermanentSubject counted)`, read by LegendRuleService per permanent (fold run 38).

- `CantBeBlockedIfDefenderControlsMatchingPermanentEffect.whileDefendingPlayer(Condition)`: "can't be blocked as long as / unless defending player …", the condition judged with the defending player as the context's target by `GameQueryService.defendingPlayerConditionHolds` (CR 508.5, 509.1b) (fold run 36b).
- `TargetPlayerMatches(PlayerPredicate)`: a `Condition` true when the player it is judged for (the target player, or a target permanent's controller) matches any player predicate (fold run 36b).
- `PlayerControlsCreaturesSharingCreatureTypePredicate(min)`: "controls N or more creatures that share a creature type" (fold run 36b).
- `CantAttackCardOwnerEffect(attacker, players, includePlaneswalkers)` / `.cantAttack(players, pw)` / `.enchantedCreatureCantAttack(players, pw)`: every "[this / enchanted / equipped creature] can't attack [its owner / its controller / you] [or planeswalkers …]", read by `AttackLegalityService.isRestrictedFromAttackingTarget` (fold run 36b).
- `SubjectResolutionService.namesPlayer` also judges `ownerOf(source())` (current owner, with the board) and `controllerOf(source())` (fold run 36b).

- `PlayerHasProtectionEffect(Quality EACH_OPPONENT | CHOSEN_NAME)`: static "you have protection from [quality]", printed (protects its controller) or floated by `GrantStaticEffectToPlayerEffect` (fold run 35c).
- `GameQueryService.playerProtectionFrom(gd, player, ProtectionSource(card, permanent, controllerId, colors))`: the one player-protection reader, giving the quality text or null. Used by targeting, Aura attachment and the `PlayerProtection` damage rule. It reads everything, players (owner when uncontrolled, CR 702.16k), colours, chosen card type and chosen name (fold run 35c).

- `SacrificePermanentsAsEntersForCountersEffect.Payoff` (`COUNTERS`, `.basePowerToughnessEqualToTotalOfCreatures()`, `.powerToughnessEqualToNumberSacrificed(filter)`): every "as this enters, sacrifice any number of [permanents]", paid off from the sacrifice's `DepartureResult`s (fold run 41).
- `PermanentPowerAtLeastAmountPredicate` / `PermanentPowerAtMostAmountPredicate(DynamicAmount)` + `AmountEvaluationService.bindAmounts(gd, PermanentPredicate, ctx)`: power compared with an amount fixed as the effect resolves, read by `DestroyAllPermanentsEffect` (fold run 41).
- `DestroyAllPermanentsEffect.readingTarget(filter, TargetPredicate)` (`referencedTarget`): a wipe whose amount reads a target it doesn't destroy for being that target, AI NEUTRAL (Fell the Mighty, Mandate of Abaddon; fold run 41).
- `CreateTokenCopyOfTargetPermanentEffect.withSubject(departedThisWay(kind))`: "for each [permanent] destroyed this way, create a token that's a copy of it", each copied as it last existed (Red Sun's Twilight; fold run 41).

- `LossReplacer.sources` / `replace` + `GameOutcomeService.applyChosenLossReplacement` and `PermanentChoiceContext.LossReplacementChoice`: every loss replacement offered for the losing player's CR 616.1 choice (fold run 39).
- `ReplaceControllerLossWithExileAndLifeTotalEffect(DynamicAmount)` / `.startingLifeTotal()` (fold run 39).
- `ReplaceTokenSubtypeCreationEffect(TokenKind, token, level)` / `.creatureTokens(token)` (fold run 39).
- `AdditionalUpkeepStepEffect(DynamicAmount, Placement)` / `.afterThisCombatPhase(count)` (fold run 39).
- `CounterSpellEffect` over a target group with several targets: "counter up to N target spells" (fold run 39).
- `CounterMatchingSpellsEffect(filter, types)` / `.abilities` / `.spellsAndAbilities`: records COUNTER_SPELL and the event value (fold run 39).
- `StackEntryControllerPoisonCountersAtLeastPredicate(n)`, read in `TargetSpellMatches` with the game (fold run 39).
- `OpponentExtraDrawsRedirectedEffect(Payload)` / `.createTreasureInstead()` (fold run 39).
- `SkipAllExtraTurnsReplacementEffect(PlayerSubject)` / `.opponents()` (fold run 39).
- `CreatureEnteringDontCauseTriggersEffect(Event, types, abilityControllers)`, with `.artifactsOrCreaturesEntering()` / `.creaturesDying()` / `.permanentsEnteringForOpponents()` (fold run 39).
- `LookAtTopCardsChooseOneToHandDrawReplacementEffect(lookCount, restToGraveyard)` / `.restOnBottom(n)` / `.restIntoGraveyard(n)` (fold run 39).

- `StepKind.FLIP` + `AmountThisWay(FLIP)`: "for each flip you won", read after `FlipUntilLoseEffect` (one event: copies, counters, tokens) (fold run 40).
- `FlipUntilLoseEffect.untilLose()` / `.upTo(coins, allWins)` / `.flip(coins, perWin, perLoss, allWins)` / `.headsOrTails(n, perHeads)`: every multi-coin flip by the controller (fold run 40).
- `SacrificedCardMatches.permanent(PermanentPredicate, description)` + `ConditionContext.sacrificedPermanent`: "if the sacrificed permanent was [suspected / a commander]", read as it last existed (fold run 40).
- `DrawFromBottomOfLibraryEffect.fromAnywhere()` (`From.ANY_POSITION`), `DoubleDrawReplacementEffect.exceptFirstDrawInEachOfYourDrawSteps()`, `DoubleTargetCreaturePowerEffect.of(subject)` / `.ofCreaturesYouControl(filter)` (fold run 40).

- `SpecializeEffect(CardColor color, SpecializeEffect.Faces faces)`: "Specialize {N}". The card's static `specializedFace(Card face, CardColor color)` writes the face onto a cleared runtime copy and returns its "when this creature specializes" effects (fold run 43).
- `UnlockControlledRoomDoorEffect(PermanentSubject rooms, boolean lockOrUnlock)`: `()`, `targetRoom()`, `lockOrUnlockTargetRoom()`; one `UnlockRoomDoorChoice` context (fold run 43).

- `OnlyMatchingCreaturesCanAttackThisCombatEffect.lands()` / `.source()` / `.only(ids)`: every "only [these] creatures can attack" float (an added combat, the aggressive combat, en-Vec's next turn), judged with the float's source and controller (fold run 37).
- `MakeTargetAttackingCreatureBlockedEffect(PermanentSubject)`: "[subject] becomes blocked" (CR 509.1h) (fold run 37).
- `CanBlockCreaturesWithShadowEffect(Mode, attackers)` / `.ignoringLandwalk()` / `.asThoughReachAgainst(subtype)`: every "can block … as though" permission (fold run 37).
- `MatchingPermanentsCantActivateTapAbilitiesEffect(PermanentSubject)`: "[set / enchanted creature]'s {T} abilities can't be activated" (CR 602.5) (fold run 37).
- `AttachedPermanentRestrictionEffect` also carries `PreventTransformEffect()`, `CanOnlyAttackAloneEffect`, `CantAttackOrBlockAloneEffect`, `TargetingRestrictionEffect.opponentSpellsAndAbilities()` (opponents of the attachment's controller) and `CanAttackAsThoughHasteUnlessEnteredThisTurnEffect(false)`. It is read through `GameQueryService.hasAttachedPermanentRestriction` / `attachedPermanentRestrictions(gd, permanent, type)` (fold run 37).
- `ProtectionFromManaValueEffect` Quality (`atMost`, `chosenParity`, `parity`, `otherThanChosenNumber`, `otherThan`) and `ProtectionFromMulticoloredEffect` ColorCount (`monocolored`, `enemyColoredMulticolored`, `colorless`) (fold run 37).

- `ModifyKeywordCostEffect(KeywordCost, signed amount, scope)` / `reduceYours`: buyback, dash, flashback, cycling, ninjutsu, morph and unlock costs.
- `ModifyCastCostPerTargetEffect(amount, scope, targetFilter, spellFilter)`: "for each target" / "for each [permanent] it targets".
- `ReduceActivatedAbilityCostEffect(..., ActivatedAbilityKind)`: BOAST, EXHAUST, NON_MANA, POWER_UP.
- `IncreaseSpellCostEffect(predicate, n, PlayerSubject players)` and `SubjectResolutionService.namesPlayer` judging `activePlayer()` and `except(...)`.
- `ReduceOwnColoredCastCostEffect("{2}{U}")`: a mana unit times its amount.
- `ReduceOwnCastCostEffect.increase` and `ReduceOwnCastCostIfTargetingPermanentEffect.increaseIfTargeting`.
- `CostModificationScope.names(controller, player)`.

- `CardSharesCreatureTypeWithPermanentsPredicate(PermanentSubject)` / `PermanentSharesCreatureTypeWithPermanentsPredicate(PermanentSubject)` + `SubjectResolutionService.creatureTypes` / `bindPermanentSubjects`: "shares a creature type with [the sacrificed / enchanted / that creature]", live or last known, changeling = every type, bound once (Pyre of Heroes, Endemic Plague, Call to the Kindred, Conjurer's Mantle; fold run 41b).
- `CardSharesNameWithCardsPredicate(CardSubject)`: "a card with the same name as that card" over `revealedThisWay()` (Infernal Tutor, Assembly Hall; fold run 41b).
- `SearchLibraryEffect.targetSpec` declares a benign permanent target when its filter reads a target subject ("cards with the same name as target creature", Pack Hunt; fold run 41b).
- `LookAtTopCardsOfTargetLibraryEffect.lookingAt(count, PlayerSubject)`: private, promptless LOOK_ONLY at `you()`, `eachPlayer()` or `targetPlayerDeclaredElsewhere()` libraries (fold run 41b).
- `LookAtFaceDownCreatureEffect(PermanentSubject)`: look at each face-down creature a set names (Revealing Wind, Spy Network; fold run 41b).

- `Subjects.convokedThisSpell()` (`PermanentSubject.Convoked`): "each creature that convoked this spell / it"; `permanentIds` names those that left too (connive, Venerated Loxodon, Zephyr Singer) (fold run 44).
- `DrawDiscardAndConniveEffect(amount, PermanentSubject)` / `.of(subject)` and `PermanentChoiceContext.ConniveOrderChoice`: several permanents connive one at a time, the APNAP player choosing (CR 701.50c) (fold run 44).
- `AirbendTargetPermanentEffect(PermanentSubject, TargetSpec)` / `.targeting(TargetPredicate)` / `.of(subject)`: airbend permanents (one exile event) and a target spell (fold run 44).
- `ManifestTopCardEffect.fromLibraryOf(player)` / `CloakTopCardOfControllerLibraryEffect.fromLibraryOf(player)` and `ManifestService.manifestTopCard(gd, libraryOwner, controller, source)`: manifest or cloak from another player's library, tracked as the manifester's (fold run 44).
- `TurnSourceFaceUpEffect.ofTarget()`: every effect turn-face-up goes through `GameService.turnPermanentFaceUpWithoutPayingManaCost` and reveals a face-down instant or sorcery (CR 701.40g) (fold run 44).
- `AssignNoCombatDamageEffect(PermanentSubject, thisCombatOnly)` / `.of(subject)`, `SuppressStaticEffectUntilEndOfTurnEffect(type, subject)`, `SimultaneouslyFlipAllPermanentsTapStatesEffect(PermanentSubject)`, `BecomePreparedEffect.ofTarget()` / `(asTriggeredAbility, subject)` (fold run 44).

Files are in /tmp/claude-1000/-mnt-ramdisk-magical-vibes/300dbf19-8a01-4cdb-8671-d50e7ccdb4f0/scratchpad/fold-44:
- ledger-rows.csv
- test1.log

- `CrewAndSaddlePowerModifierEffect.Action` (`CREW`, `SADDLE`, `STATION`) + `actions()` / `appliesTo`: "crews Vehicles [and saddles Mounts / and stations permanents] as though its power were N greater / using its toughness" (CR 702.184c) (fold run 45).
- `MasterOfPredicamentsEffect(DynamicAmount threshold, PlayerSubject guesser, CardEffect ifNoSpellCast)`: the hidden-card mana-value guess (fold run 45).
- `ChangeTargetOfTargetSpellToSourceEffect(spell, newTarget)` / `.triggeringSpellToEnchantedCreature()` (fold run 45).
- `GatherSpecimensEffect(Kind CREATURES_ENTERING | TOKENS_CREATED)` / `.tokensCreated()` (fold run 45).
- `CountScope.TARGET_PLAYERS`: counts what every still-legal target player controls (fold run 45).
- `SpellsWithChosenNameCantBeCastEffect.spellsAndLands()` (`landsToo`, both chosen names) (fold run 45).
- `ExchangeGraveyardAndLibraryEffect(Zone first, Zone second)`: exchange two of library, graveyard, hand; CR 404.3 order prompt; no shuffle (fold run 45).
- `ExchangeControlOfTargetPermanentsEffect.sharedCardTypes` (`ANY_CARD_TYPE`, `ARTIFACT_OR_CREATURE`, `ARTIFACT_CREATURE_OR_LAND`), `.forControlledTargetsSharingOneOf(first, types)`, records GAIN_CONTROL; `GameQueryService.sharesCardTypeAmong`; `Subjects.thisWay(GAIN_CONTROL)` (fold run 45).
- `CreateTokenEffect` with investigate: "investigate N times" is always N creation events (CR 701.16a) (fold run 45).

- `EachPlayerPaysAnyManaEffect(startingWithYou, PlayerSubject payers, Resource)` / `.byYou(Resource)`: Resource `MANA`, `X_MANA` ("pay {X}"), `ENERGY`, `LIFE`. "Pay any amount", "you may pay one or more {E}" and "you may pay {X}" at resolution. Records PAY_MANA / PAY_ENERGY / PAY_LIFE, read by `AmountThisWay.manaPaid()` / `energyPaid()` / `lifePaid()` and `HappenedThisWay(kind)`. "You may pay {X}. If you do" where X = 0 matters is `MayEffect(SequenceEffect.of(pay, …))` (fold run 46).
- `StepKind.PAY_ENERGY`, `StepKind.PAY_LIFE` (fold run 46).

- `ProtectionFromColorsEffect.ColorSet` (`FIXED`, `CHOSEN`, `AMONG_PERMANENTS_YOU_CONTROL`, `OUTSIDE_COMMANDER_IDENTITY`) with `chosenColor([subject])`, `amongPermanentsYouControl([subject])`, `outsideCommanderColorIdentity(subject)`, `withColors(colors)`: every "protection from [colour set]" (fold run 37b).
- `GameQueryService.protectionColors(gd, protection, holder)` / `protectionColorsForStaticEvaluation(gd, protection, holder, controllerId)`: the one colour-protection reader, judged for the permanent that has the ability; `protectionFromColors()` is the fixed colours only (fold run 37b).
- `ProtectionFromColorsSelfEffectHandler`: a permanent's own board-reading colour protection applied in layer 6 (fold run 37b).

- `LookAtTopCardMayRevealMatchingToHandEffect` `OtherwiseDestination.MAY_BOTTOM`, predicate subjects bound to the entry, `StepKind.REVEAL` of the card put into hand: "you may put it on the bottom", "if it shares a card type with that permanent", "if you do, lose life equal to its mana value" (`effectsIfCardPutIntoHand` + `AmountThisWay.revealedManaValue()`) (fold run 41c).
- `LookAtTopCardMayPutMatchingOntoBattlefieldEffect.Otherwise {TOP, HAND, MAY_BOTTOM}`, the predicate judged with the source or its snapshot, `StepKind.RETURN`: "if you didn't put a card onto the battlefield this way" = `ConditionalEffect(NotCondition(HappenedThisWay.returned()), …)` (Rulik Mons; fold run 41c).
- `ChoosePermanentsEffect.toSacrifice()`: "[that player] sacrifices one of them", "you may sacrifice one of them": only the candidates the chooser controls and could sacrifice (CR 608.2d, 701.21a), then `SacrificeSelfEffect(thisWay(CHOOSE))` (Incriminate, Descendants' Fury; fold run 41c).
- `SubjectResolutionService.cardTypes` falls back to the entry's last-known snapshot of a permanent no removal step recorded (CR 608.2h) (fold run 41c).

- `RollTwoDiceChooseOneEffect(sides)`: "roll two d{sides} and choose one result": one advantage-aware roll of two dice and the choice; X is the chosen result and the event value the other, for the card's following steps (fold run 40b).
- `DiceRollService.rollDice(gameData, player, sides, count)`: one roll of `count` dice; each `RollWithAdvantageEffect` adds one die to the roll and the lowest are ignored (CR 706.6) (fold run 40b).
- `RedirectPlayerDamageToSelfEffect.combatOnly` / `.combatDamageFromUnblockedCreatures(requiresUntapped)`: floated on a player by `GrantStaticEffectToPlayerEffect`, "this creature" is the float's source (fold run 40b).
- `SetNameEffect(name, subject, Duration)` one-shot path: a resolved name change floats a layer-3 `target()` copy on each permanent, in timestamp order with static name setters and kept after a loss of all abilities; `Permanent.persistentName` is gone (fold run 40b).

- `EmpowerNextCreatureSpellThisTurnEffect(CardPredicate spellFilter, uncounterable, counters)` / `.cantBeCountered(filter)`: every "the next [matching] spell you cast this turn can't be countered / enters with N additional +1/+1 counters", applied by `SpellEvents.cast` to the spell that ends it (fold run 44b).
- `DurationClock.waitsForSpell(gd, floatingEffect, spellCard, castingPlayer, castFaceDown)`: the one judge of whether a cast is the "next matching spell" a `NextMatchingSpellEffect` waits for (fold run 44b).
- `GrantFlashToNextMatchingSpellEffectHandler.spellDescription(CardPredicate)`: log noun for a next-spell filter ("spell", "creature spell", "instant or sorcery spell") (fold run 44b).
- "You may destroy target X. If you do, …" is `MayEffect(SequenceEffect.of(destroy, ConditionalEffect(TargetPermanentMatches(not indestructible), …)))`; "you may pay {c}. If you do, destroy … and …" is `MayPayManaEffect(c, SequenceEffect.of(destroy, …))` with no gate (CR 118.12) (fold run 44b).

- `ExchangeControlOfPermanentAndSpellEffect(PermanentSubject permanent, PermanentSubject spell)` / `.sourceAndTriggeringSpell()` / `.targetCreatureAndTargetSpell()`: exchange control of a permanent and a spell, all or nothing (CR 701.12a), records GAIN_CONTROL (fold run 45b).
- `ChooseNewTargetsForTargetSpellEffect(mustChangeAll, PermanentSubject spell, onlyIfItHasTargets)` / `.ifItHasTargets(targetSpell() | triggeringSpell())`: the one "[player] may choose new targets for [spell]" step inside a `MayEffect` (CR 115.7d) (fold run 45b).
- `SubjectResolutionService.spellOnStack(gd, entry, targetSpell() | triggeringSpell())`: the spell a spell subject names while it is on the stack, from any target group (fold run 45b).
- `LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect.revealing(always, mvCapped)`: "reveal the top X … put the rest into your graveyard" (Genesis Wave, Saheeli's Directive) (fold run 45b).

- `TargetGraveyardCardManaValueEqualsStepAmount(StepKind)`: "return target … with mana value X" after "you may pay {X}", the target declared on the trigger and compared with the latest step's amount; reads a triggered ability's single graveyard target (fold run 46b).
- `GrantKeywordEffect` over `controlledBy(players, filter)`: amount predicates in the filter (`PermanentPowerAtLeast/AtMostAmountPredicate`) are fixed as it resolves (CR 608.2h) (fold run 46b).
- "You may pay {X}. When you do, [ability reading X]": `MayEffect(SequenceEffect.of(EachPlayerPaysAnyManaEffect.byYou(X_MANA), QueueReflexiveAbilityEffect.withEventValue(effect, manaPaid())))`, X read as the event value; `CastTargetInstantOrSorceryFromGraveyardEffect` binds `CardManaValueEqualsEventValuePredicate` (fold run 46b).

- `MayPayManaEffect.anyPlayerMay(payers, payment, prompt, ifAPlayerDoes, ifNoOneDoes)` with `PayerLoop`: "any player / any opponent may [non-mana payment]. If a player does / if no one does", and "[effect] unless any player …". The payment is over `Subjects.acceptedPlayers()`. Players are asked in APNAP order, sacrifices are picked as they accept, and the payment is made together after the last answer (CR 101.4, 118.3, 118.12, 118.12a; fold run 47).
- `SacrificePermanentsEffectHandler.sacrificeCandidates` / `sacrificeCount`: the permanents a player could sacrifice for a sacrifice spec (CR 608.2d; fold run 47).
- `MayPayManaEffectHandler.apnapOpponents` (moved from the deleted Vexing Devil handler; 11 callers).

- `PayZeroRatherThanAbilityCostEffect(ActivatedAbilityKind kind, boolean firstEachTurn)` / `.firstEachTurn(kind)` / `.always(kind)`: "you may pay {0} rather than pay the [equip / power-up / cycling / unearth] cost [of the first … each turn]" (CR 118.9, 602.2b). Conditions go in a `ConditionalEffect` around it. Read by `CastingCostService.paysZeroRatherThanAbilityCost` (fold run 49).
- `ActivatedAbilityKind.EQUIP`, `CYCLING`, `UNEARTH` (fold run 49).
- `PutCounterOnControlledCreatureCost(CounterType, DynamicAmount amount, boolean optional)` / `.blight(n)` / `.blightX()`: blight on spells and abilities (CR 701.68a, 701.68b) (fold run 49).
- `ActivatedAbility.withMaximumXValue(DynamicAmount)`: "X can't be greater than …" on an activated ability, checked as it is activated (fold run 49).

- `CantHaveCountersEffect(subject, counterType)` / `.ofKind(type)`: "can't have [kind] counters put on it" (null is every kind); read by `cantHaveCounters`, `cantHavePlusOnePlusOneCounters` and `cantHaveMinusOneMinusOneCounters` (fold run 24).
- `DoubleCountersOnTargetPermanentEffect(type, controlledBy(...) | creaturesYouControl())`: "double the counters on each [permanent] you control", each placement through the shared pipeline (fold run 24).
- `ControlledPermanentsEnterUntappedEffect(filter, PlayerSubject players)`, `.lands()`, `.forEveryPlayer(filter)`: every "[permanents] enter untapped", printed or as the upgrade of a `ConditionalReplacementEffect` (fold run 24).
- `PutCountersEffect(createdThisResolution() | returnedThisResolution(), …)`: "put counters on it / each of them" after a token or return step; a source-relative amount is noted first (`NoteAmountEffect` + `AmountThisWay.noted()`) (fold run 24).

- `RegisterDelayedStepTriggerEffect.aboutCardAtNext(...).ofThatPlayer(player)`: "at the beginning of their next upkeep" with `Triggers.upkeep(CHOSEN_PLAYER)`, that player's next step in any turn (Phytotitan: `Subjects.ownersOf(Subjects.sourceCard())`) (fold run 7b).
- `ReturnTriggeringCardFromGraveyardToBattlefieldEffect(tapped, underController, counterType, DynamicAmount)`: a counter amount evaluated as it resolves; "with one fewer revival counter" is `NoteAmountEffect` then `AmountThisWay.noted()` in the delayed return (Nine-Lives Familiar, fold run 7b).
- "Return that card / it to its owner's hand at the beginning of the next end step": `aboutCardAtNext(endStep(ANY), triggeringCards() | sourceCard(), PutCardsIntoHandEffect(same subject))`, wrapped in `MayEffect` for "you may" (fold run 7b).

- `AllowPlayCardsFromExileEffect.alsoCardsExiledLaterThisTurn()`: a this-turn permission over `exiledWithSource(...)` that also covers cards exiled with the source later that turn (CR 611.2c), stored as `model/ExilePlayGrantBySource` on the source and applied by `ExileService` (fold run 30b).
- `CardMilledThisTurnPredicate`: "cards in your graveyard that were milled this turn", read by owner (fold run 30b).
- `SpendAnyManaTypeToCastEffect(CardPredicate spellFilter)` / `.spells(filter)`: any-mana cast permission for matching spells, judged with the caster as perspective (fold run 30b).
- `ExileTopCardsToSourceEffect` and `SearchLibraryForCardsToExileWithSourceEffect` record `StepKind.EXILE`, so "those cards" is `cardsThisWay(EXILE)` (fold run 30b).

- `GrantControllerAndPermanentsHexproofFromColorsEffect.chosenColor(target() | source())`: "[it] gains hexproof from that color until end of turn" after `ChooseColorAtResolutionEffect`, the colour fixed as it applies (CR 608.2h); default `(colors)` is "you and permanents you control" (fold run 6c).
- `SubjectResolutionService.bindChosenValues` binds `PermanentHasChosenSpellColorPredicate`: "can't be blocked by creatures of that color this turn" is `MakeCreatureBlockableOnlyByFilterThisTurnEffect(not(PermanentHasChosenSpellColorPredicate))` (fold run 6c).
- `SetChosenColorUntilEndOfTurnEffect` after `ChooseColorAtResolutionEffect` applies that colour without asking; `SetChosenColorUntilEndOfTurnEffectHandler.apply` is the one colour application (fold run 6c).

Sources (Torpor Orb rulings search):
- [Torpor Orb (NPH 162)](https://scryfall.com/card/nph/162)
- [Torpor Orb (BIG 57)](https://scryfall.com/card/big/57)

- `ExileTopCardsAndMayCastSpellsEffect.controllerMayPlayUpTo(DynamicAmount count, int maxPlayCount)`: "exile [that many] from the top; you may play up to N of those cards without paying [its] mana cost". Lands are played during resolution (your turn, a land play left, CR 305.2, 305.3); the rest stay exiled (Vault 112; fold run 46c).

- `MayPayManaEffect.payingAsEachAccepts()` (`PayerLoop.anyPaid`): each accepting player pays at once, "if a player does" after the first payment (the PLC "any player may … If a player does, counter [this spell]" rulings) (fold run 47b).
- `MayPayManaEffect.anyPlayerMay` payments `TapPermanentsEffect(controlledBy(acceptedPlayers(), filter), null, n)` (picked by each acceptor), `DiscardEffect` (hand size), `EachPlayerPaysAnyManaEffect.lifeExactly`; pick contexts `PermanentChoiceContext` / `MultiPermanentChoiceContext.AnyPlayerPaymentPick` (fold run 47b).
- `CounterSpellEffect.triggeringSpell()` (`spell` subject `targetSpell()` or `triggeringSpell()`): a cast trigger's untargeted "counter that spell", nothing if it left the stack (CR 701.6a, 608.2b) (fold run 47b).
- `EachPlayerPaysAnyManaEffect.lifeExactly(payers, DynamicAmount)`: "[the players] pay N life", evaluated per payer, Mastery asked first, records PAY_LIFE (fold run 47b).
- `CantAttackOrBlockUnlessPaysEffect(amount, Side, attackerFilter)` / `.attacking(amount)` / `.blocking(amount, attackerFilter)`: every self combat mana cost; unfiltered block cost once per blocker, filtered per matching attacker (CR 508.1h, 509.1d) (fold run 47b).

Files are in /tmp/claude-1000/-mnt-ramdisk-magical-vibes/300dbf19-8a01-4cdb-8671-d50e7ccdb4f0/scratchpad/fold-47b:
- ledger-rows.csv

- Manifest dread is a MANIFEST step: the choice's answer records the manifested permanent, so "that creature" is `Subjects.thisWay(StepKind.MANIFEST)`.
- `paidAsCost()` covers a spell's tap-cost permanents (`chosenCostPermanentIds`), and every copy carries them (`CopySupport.createCopyStackEntry`, CR 707.10).
- `GrantSubtypeEffect(subtype, thisWay(kind))` as a one-shot: "it becomes a [subtype] in addition to its other types" after a choice (amass).
- Amass = `ChoosePermanentsEffect.exactly(1, controlledBy(you(), Army), "an Army you control")` + `PutCountersEffect(thisWay(CHOOSE), +1/+1, N)` + `GrantSubtypeEffect(subtype, thisWay(CHOOSE))`.

Ledger rows are in /tmp/claude-1000/-mnt-ramdisk-magical-vibes/300dbf19-8a01-4cdb-8671-d50e7ccdb4f0/scratchpad/fold-24b/ledger-rows.csv.

- `SpendManaAsAnyColorEffect.whiteMana()` (`fromColor` WHITE), floated by `GrantStaticEffectToPlayerEffect.untilEndOfTurn`, read printed or floated by `GameQueryService.canSpendManaAsAnyColor` / `canSpendWhiteManaAsAnyColorWithoutRestriction` (fold run 30c).
- `SpendManaAsAnyColorForActivatedAbilitiesEffect(subject, borrowedAbilitiesOnly, fromColor)` / `.blueManaForOwnAbilities()`; `controlledBy(you(), not(PermanentOwnedBySourceControllerPredicate))` is "permanents you control but don't own" (fold run 30c).
- `SpendAnyManaTypeToCastEffect.asThoughAnyColor(CardPredicate)`: any-colour cast permission ({C} still needs colorless), read by `CastingPermissionService.canSpendManaAsAnyColorToCastFromBattlefield` (fold run 30c).
- Static subject sets judge `PermanentOwnedBySourceControllerPredicate` against the ability's controller (`SubjectResolutionService.setFilterMatches`) (fold run 30c).

- `EscalateCost(perModeCost, Kind ESCALATE | ENTWINE)` with `escalate("{M}")` / `escalate(cost)` / `entwine("{M}")` / `entwine(cost)`: "Escalate [cost]" and two-mode "Entwine [cost]" (CR 702.120a, 702.42a). Payments are counted by `AdditionalSpellCostService.escalatePayments` (fold run 49b).
- `SacrificeAllMatchingPermanentsCost(filter, trackSacrificedPower)`: "sacrifice all [permanents] you control" as an activated-ability cost or a spell's additional cost, with total power as X (Soulblast) (fold run 49b).

- `PayLifeCost(DynamicAmount amount)` / `PayEnergyCost(DynamicAmount amount)`, `PayEnergyCost.x()`, `fixedAmount()`: costs with an amount the engine reads as the total cost is determined (CR 601.2f, 602.2b); `fixedAmount()` for the spell, forced-energy and alternative-spell readers (fold run 47c).
- `AmountEvaluationService.evaluateCost(gameData, amount, payer, source, x)` and `isUnpayableCost(gameData, amount, payer)` (CR 903.4f) (fold run 47c).
- `ColorsInCommandersColorIdentity` (`DynamicAmount`): the colours in the controller's commanders' colour identity (CR 903.4), 0 without a commander, unpayable as a cost then (fold run 47c).

Ledger rows are in /tmp/claude-1000/-mnt-ramdisk-magical-vibes/300dbf19-8a01-4cdb-8671-d50e7ccdb4f0/scratchpad/fold-47c/ledger-rows.csv (3 absorbed, 2 survivor).

- `ControlledPermanentsEnterWithAdditionalCountersByAmountEffect.ofEnteringPermanent(predicate, amount)`: an "enters with additional counters" amount evaluated with the entering permanent as its source, as it would exist on the battlefield (CR 614.12): mana spent to cast it, its mana value (0 face down).
- `ArtifactManaSpentToCast()` (DynamicAmount): mana from artifact sources, Treasures included, spent to cast the source spell or the entering permanent.

- `MayPayLifeEffect`: a payer who can't pay that much life isn't asked; its else effect follows (CR 119.4, 608.2d) (fold run 48).
- `MayEffect` over a `controllerOf(...)` `SacrificePermanentsEffect` (any count) isn't asked when its chooser can't sacrifice that many (CR 608.2d; Archfiend of Spite) (fold run 48).
- "[Abandon this scheme] unless you discard a card or pay N life" is `MayEffect(DiscardEffect, …, MayPayLifeEffect(N, null, …, CONTROLLER, SacrificeSelfEffect()))`; on a scheme, `SacrificeSelfEffect` abandons it (CR 701.33b) (fold run 48).

- `ProliferateEffect` is a PUT_COUNTERS step: each answered proliferate adds the permanents (and their controllers) and players it gave counters to; "that had a counter put on them this way" is `thisWay(PUT_COUNTERS)` (Ripples of Potential; fold run 8c).
- `PlayerLifeExactlyPredicate(int life)`: "each player with exactly N life", with `matching(eachPlayer(), …)` (Triskaidekaphobia; fold run 8c).
- `ControllerLosesGameEffect` over several players: all lose at once, and a draw when every remaining player loses (CR 104.4a); a resolution stops once the game has ended (CR 104.1) (fold run 8c).
- Several targets in one triggered ability: plain `target(...)` groups plus `addTrigger(trigger, e1, e2)` with `registerEffectTargetIndex(e, group)` for each effect; the stack door walks the groups (Decimator Beetle; fold run 8c).

- `MatchingCreaturesMustAttackThisTurnEffect.attacking(PlayerSubject | PermanentSubject)` / `.lasting(Duration)`: "creatures [players] control attack [you / CARDNAME] [during their next turn] if able"; the next-turn form makes one float per player (fold run 7c).
- `MustBeBlockedIfAbleEffect(PermanentPredicate blockerFilter)` and `GameQueryService.mustBeBlockedRequirements` / `mustBeBlockedIfAble` / `mustBeBlockedByMatchingCreatureRequirements` / `obeysAMustBeBlockedRequirement`: the one must-be-blocked reader (fold run 7c).
- `ProtectionFromColorsEffect` with a set subject (`chosenColor(controlledBy(...))`) also protects its source, through `ProtectionFromColorsSelfEffectHandler` (fold run 7c).

- `AmountEvaluationService.bindTriggeringSpellCardTypes(gd, filter, entry, GameQueryService)`: binds "shares a card type with that spell", reading the spell's types with `cardHasType` for its controller. `EffectResolutionService` applies it to every `HappenedThisWay` card filter (Gandalf, Westward Voyager; fold run 8d).
- `ExileGraveyardCardsEffect.cards(subject).imprintingOnSource()`: "you may exile that card" that becomes the source's imprinted card (Mimic Vat; fold run 8d).
- `RevealTopCardOfLibraryEffect` shows each revealed card to every player through `CardRevealService.revealToAllPlayers` (CR 701.20a; fold run 8d).

Ledger rows are in /tmp/claude-1000/-mnt-ramdisk-magical-vibes/300dbf19-8a01-4cdb-8671-d50e7ccdb4f0/scratchpad/fold-8d/ledger-rows.csv.

- `PayLifeCost.x()`: "Pay X life" with the announced X, for spells (no {X} needed in the mana cost) and abilities; spells are validated by `AdditionalSpellCostService.validateSpellLifeCost` and paid by `SpellCastingService.paySpellLifeCost` through `evaluateCost` (fold run 50).
- `ChoiceContext.ExileFreeCastModeChoice.requiredOrAllModes` / `mayStopAt(n)`: a resolution-cast mode picker that stops only at the number specified or at every mode (entwine, CR 702.42a); set by `ExileFreeCastQueueSupport` for `EscalateCost.Kind.ENTWINE` (fold run 50).

- `AmountsFixedWhenFloatedEffect.withAmountsFixed(fix)`: a static rule whose amounts `GrantStaticEffectToPlayerEffect` evaluates once as it floats it (CR 608.2h). Implemented by `RequirePaymentToAttackEffect` and `RequirePaymentToBlockEffect`; "this turn, creatures can't attack / block unless their controller pays {X}" (War Tax, War Cadence) (fold run 48b).
- `RequirePaymentToAttackEffect` floated on players (`eachPlayer()` for "creatures can't attack", `you()` for "can't attack you"). It is judged for the attacked player or the attacked planeswalker's controller (CR 506.2a, 508.5). A target-relative amount reads the attacker: `CountersOnTargetPermanent(null)` is "the number of counters on that creature" (Nils) (fold run 48b).
- `RequirePaymentToBlockEffect(DynamicAmount[, Condition])`, floated and read board-wide by `getGlobalBlockManaTax` (fold run 48b).
- `MayEffect` over a leading `source()` `RemoveCountersEffect` isn't asked when the source is gone or lacks the counters; the else effect follows (CR 608.2d) (fold run 48b).
- `PermanentRemovalService` refreshes `resolvingEntry` / `pendingEffectResolutionEntry`'s source snapshot as the source leaves, and last known information keeps lifelink, deathtouch, infect and wither, so "sacrifice it and it deals damage" after `SacrificeSelfEffect` reads it as it last existed (CR 608.2h) (fold run 48b).

Files are in /tmp/claude-1000/-mnt-ramdisk-magical-vibes/300dbf19-8a01-4cdb-8671-d50e7ccdb4f0/scratchpad/fold-48b:
- ledger-rows.csv (23 rows)

- `LookAtTopCardsAndMayCastEffect.reveal(...)` records REVEAL (cards, controller, library) before its cast choice, and the cast resumes the later steps (CR 608.2g). "If it's a land card …" is `toACard(REVEAL, land)`; "if you don't cast it, put it into your hand" is `PutCardsIntoHandEffect(revealedThisWay())` (Unexpected Results, Rashmi; fold run 8e).
- `PlayerLifeExactlyPredicate(DynamicAmount)`: "an opponent with the most life among your opponents" is `matching(eachOpponent(), new PlayerLifeExactlyPredicate(new HighestOpponentLifeTotal()))`, evaluated for the controller (The Master, Gallifrey's End; fold run 8e).

- `Subjects.exiledWithSourceUnlinked(filter)` / `CardSubject.ExiledWithSource.UNLINKED`: the cards exiled with the source under no ability link ("the exiled card" of an imprint; Dermotaxi's ruling, CR 607.2a), static and resolving (fold run 50b).
- `LibrarySearchFollowUp.ToHandPick.of(CardPredicate, label)` and `SearchLibraryForOneCardOfEachSubtypeEffect.ofEach(ChooseCardEffect.Each...)`: one revealed to-hand pick per quality within one search, judged by `PredicateEvaluationService`; colour and subtype picks are filters too (fold run 50b).

- `GameQueryService.entersWithCardType` (private): an entering permanent's card type as it would exist on the battlefield (CR 614.12); the counter-placement paths use it.
- `isManifestBlockedFromZone` now judges the face-down 2/2 (`faceDownSpellCharacteristics`) for manifest and cloak.

- `SpellCastingService.paySacrificeCostTogether`: a cost sacrificing several permanents leaves as one departure batch.
- Choose-then-act for "each opponent chooses, then [verb]": collect the choices in the choice context, then act once (leave batch or departure batch). Reference handlers: Sothera, Ol Rin's Searing Light.

- `ChoosePlayerEffectHandler.opponentToBeChosenFirst(gd, entry, rerun)` / `onlyOpponent(gd, controllerId)`: "an opponent" the controller chooses, asked only with two or more; the rerun reads `playersThisWay(CHOOSE)`.
- `ClashSupport.performClash(gd, player, opponent)`: clash with a chosen opponent.
- `AttackLegalityService.canAttackAPlayer(gd, creature, controllerId)`: "attacks a player if able" against any player.

Files are in `/tmp/claude-1000/-mnt-ramdisk-magical-vibes/300dbf19-8a01-4cdb-8671-d50e7ccdb4f0/scratchpad/d12/`:
- `ledger-rows.csv`
- `verdicts.txt` (fuller verdict notes)

- `Subjects.targetGraveyardCards(filter, scope, n)` / `.fromASingleGraveyard()` (`CardSubject.TargetGraveyardCards`): "up to N target cards in graveyards / from a single graveyard / in your graveyard". Non-target; the card's target group declares them; found by `EffectResolution.targetGraveyardCards` for the cast path (`SpellCastingService.beginTargetGraveyardCardsChoice`, hand and flashback). A put, a choice and `except(...)` read the same targets (Serene Remembrance, Turn the Earth, Deliver Unto Evil; fold run 50c).
- `PutCardsIntoLibraryEffect.shuffledIn(...)` directly before `ShuffleIntoLibraryEffect`: the spell owner's library is shuffled once by the latter ("shuffle this and up to three target cards into their owners' libraries", CR 701.24c; fold run 50c).

- `PermanentCounterSupport.removeAllCounters(gameData, permanent, removingPlayerId)`: "remove all counters from [it]" with the oil record, the last-counter event and the vanishing check.
- `MayEffect` over a leading `SearchLibraryEffect` whose searcher can't search is not asked (Leonin Arbiter ruling, CR 608.2d).

- `PlayerInputService.beginAnyTargetChoice` and `beginMultiPermanentOrPlayerChoice` now drop permanents that can't be chosen, like the other two shared prompts.

Files are in /tmp/claude-1000/-mnt-ramdisk-magical-vibes/300dbf19-8a01-4cdb-8671-d50e7ccdb4f0/scratchpad/d15:
- ledger-rows.csv (one row, for the deleted record)
- run3.log (the main test run)

- `CardSharesNameWithSpellCastThisTurnPredicate`: "if it has the same name as a spell that was cast this turn" (any caster; split halves, all card names, no name when face down).
- `LibraryChoiceHandlerService.canCastFromLibraryWithoutPaying(gd, player, card, limit)`: the cast-now check for a library card offered to cast for free.
- `ChoosePermanentsEffect.toSacrifice()` with a required choice falls back to all of the chooser's candidates when none can be sacrificed.
- `ReturnTargetCardsFromGraveyardToBattlefieldEffect` with `OPPONENT_GRAVEYARD`: "from that player's graveyard", narrowed by the stack door's triggering player.

- `GameQueryService.canPayLifeForCosts(gameData, payerId)`: may this player pay life as a spell or ability cost (no prohibition applies, and they can lose life; CR 119.8).
- `SacrificeUnlessReturnPermanentTypeToHandEffectHandler.findSourcePermanent(gd, controllerId, sourcePermanentId)`: the source permanent as an object (CR 400.7).

No Gradle process of mine is running. The result is left uncommitted in `/tmp/claude-1000/-mnt-ramdisk-magical-vibes/300dbf19-8a01-4cdb-8671-d50e7ccdb4f0/scratchpad/wt/d17`.

- `GainAbilitiesOfLastChosenExiledCardSelfEffectHandler.linkGainedEffect(effect, link)` (package-private): re-links a gained exile/return pair (CR 607.5). Used by both borrowers.
- `DiscardHandEffectHandler.discardHand(gd, player, controller, name)`: the one discard-hand primitive, with the Tamiyo guard.
- The entry batch asks every as-enters choice through `handleCreatureEnteredBattlefield`.

Files are in /tmp/claude-1000/-mnt-ramdisk-magical-vibes/300dbf19-8a01-4cdb-8671-d50e7ccdb4f0/scratchpad/d19:
- ledger-rows.csv
- run3.log (the main test run)

- The perpetual stores, not the card's printed fields: a perpetual change goes through `PerpetuallyGrantCardCharacteristicsEffectHandler.grant` plus `perpetualCardBasePowerToughness`, `grantPerpetualKeywords` and `grantPerpetualAlternateCast`, keyed by the physical card id.
- `GameQueryService.entersWithCardType(gameData, controllerId, permanent, type)` (now public): a card type as the permanent would exist on the battlefield (CR 614.12).
- `ControlledCreaturesEnterWithAdditionalCountersEffect.creatures()`: "each other [type] creature you control".

Files are in `/tmp/claude-1000/-mnt-ramdisk-magical-vibes/300dbf19-8a01-4cdb-8671-d50e7ccdb4f0/scratchpad/d18/`:
- ledger-rows.csv
- logs/big2.log

- `SeekCardsToHandEffect.alsoSought(amount, filter)`: "seek a [X] and a [Y]" as one instruction, the second seek's cards added to the SEEK result.
- `PermanentCounterSupport.moveCounters` per kind: the way to "move all counters" (CR 122.5).

- `GameQueryService.asDamageSourcePredicate(gameData, predicate, sourceColors)`: a source filter as it judges damage under Ghostly Flame (CR 609.7b).
- `ChoiceContext.OutsideGameCardChoice` (renamed): "choose a card you own from outside the game", which stays there (Grizzled Huntmaster).

- `RequirePaymentToAttackEffect.includingAttacksOnBattles()`: "creatures can't attack unless …" naming no player also taxes battle attacks, judged for the protector.
- `AdjustChosenCounterOnTargetEffect(…, optional)`: "you may" adds the skip; without it the choice is add or remove only.
- `GameData.anyPlayerMayPayManaPaid`: whether an earlier player paid in the any-player mana loop; read by `AiDecisionEngine.anyPlayerManaPaymentAlreadyMade`.

The scratch files are in /tmp/claude-1000/-mnt-ramdisk-magical-vibes/300dbf19-8a01-4cdb-8671-d50e7ccdb4f0/scratchpad/d22:
- ledger-rows.csv (header only)
- run3.log (the broad run)
- broad.txt (the classes run)

- `Subjects.cardsOf(PermanentSubject)` (`CardSubject.CardsOf`): "that creature / that spell perpetually …"; a permanent's own card while it is still that permanent, or `cardsOf(triggeringSpell())`'s card while the spell is on the stack. Perpetual verbs only.
- `Subjects.sourceObjectCard()` / `SubjectResolutionService.sourceObjectCard(gd, entry, effect)`: "this card" as the same object in whatever zone the ability refers to it; no card once it has changed zones. Perpetual verbs and graveyard cast permissions.
- `PerpetuallyBoostCardEffect.toCards(CardSubject, DynamicAmount, DynamicAmount)`.

- `DealDamageEffect` (step 10 run Damage): one source deals N damage to one recipient. `Recipient` kind (any target, creature,
  creature or planeswalker, player or planeswalker, permanent) fixes `targetSpec()`; factories `toAnyTarget`, `toAnyTargetInGroup`,
  `toAnyOtherTarget`, `toEventObject`, `toAnyTargetOfAnOpponentsChoice`, `toAnyTargetChosenAtRandom`, `toTargetCreature`,
  `to(PermanentSubject, amount)`, `toTargetCreatureOrPlaneswalker`, `toTargetPlayerOrPlaneswalker`, `toTargetOpponentOrPlaneswalker`,
  `toTargetPermanent`; riders `byTriggeringPermanent()`, `unpreventable()`, `unpreventableWhen(c)`, `unpreventableAndUnredirectableWhen(c)`,
  `cantBeRegenerated()`, `withUnconditionalRegenerationPrevention()`, `exilingDamagedCreaturesThatWouldDie()`, `exilingIfItWouldDie()`,
  `exilingPermanentsDealtDamage()`, `recordingDamageDealt()`, `withDamage(a)`. Every kind: one permanent-damage path, DEAL_DAMAGE begun,
  one excess tracker (CR 120.4a, 120.10; the dealer's deathtouch), the win check. The creature kinds loop over the effect's targets; the
  any-target kind can't (Magma Burst kicked: its unbound part would hit the kicked target too).
- `DamageSupport.entryDealtByTriggeringPermanent` is the one "it deals" carrier (run Damage): a departed dealer is its departure snapshot
  first (its keywords as it left, a static grant's included), then the trigger-time snapshot; an entry that names no triggering
  permanent is dealt by its source (a self-triggered "this or another X enters, it deals"). Its damage result is added to the resolving
  entry's DEAL_DAMAGE step.

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
| Owner's questions (gift opponent) | 2 | the owner's decisions below |
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
- **Counters**: the put pair is done (run Counters, `PutCountersEffect`), the remove family too (run Remove counters,
  `RemoveCountersEffect`); left: the remove-all records (`RemoveAllCountersEffect`, `RemoveAllCountersFromTargetPermanentEffect`, the
  matching / of-type / fusion forms) for a later "remove all" run, and the mass placements (`PutCounterOnEachControlledPermanentEffect`,
  `PutCounterOnEachMatchingPermanentEffect`, `PutPlusOnePlusOneCounterOnEachCreatureTargetPlayerControlsEffect`), which need a
  batched path in `PutCountersEffect` first (owner's decision 2026-11-25).
- **Damage**: the five one-recipient records are `DealDamageEffect` (run Damage). Left for later runs: the player subject record `DealDamageToPlayersEffect` (a `to(PlayerSubject)` fold, its per-player amounts fixed first, CR 608.2h); mass and set damage (`MassDamageEffect`, `DealDamageToEachMatchingPermanentEffect`, `DealDamageToEachTargetEffect`, `DealDamageToPermanentsTargetControlsEffect`, `DealDamageToDefendingPlayerCreaturesEffect`: a "mass damage" run, one event for many recipients); another dealer (bite, fight, `TargetCreatureDealsPowerDamageToAnyTargetEffect`, the `ControlledCreaturesDeal…` records: a "dealer" run); divided damage (`DealDividedDamageEffect`, CR 601.2d); excess redirected (`DealDamageToTargetCreatureWithExcessToControllerEffect`); the cost combinators (`PayXManaDealXDamageToAnyTargetEffect`, the "unless takes damage" records) for the "unless" run.
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

### Step 9, fold run 26 (2026-11-24, parallel worktree)
6 of 7 batches taken: 19 of 20 members folded (13 absorbed, 6 split), 1 left. Ratchet 3,576 / 2,857 → 3,560 / 2,841. CR 101.4, 108.3, 115.10a, 120.1, 120.3, 120.8, 208.3, 608.2b, 608.2d, 608.2h, 702.16e, 702.179e, 704.3 checked.

**dealers** (5 of 5 folded): `TargetCreatureDealsDamageToControllerEffect(damage, dealers, player)` takes any non-targeting dealer: `attachedCreature()`, `triggering()`, `triggeringOrSource()` or a set. Players are `controllerOf(dealers)` / `ownerOf(dealers)` per dealer, or any non-targeting subject found once (`defendingPlayer()`, `except(eachOpponent(), triggeringPlayer())`). Each dealer is its own source, read as it last existed, through `DamageSupport.dealerAsItIsOrWas` / `dealerControllerId` / `entryDealtByPermanent`. The step records DEAL_DAMAGE.
**bite-each** (2 of 3): the survivor is now `(dealer, recipients)` and is a `BoardWipeEffect` for a non-target dealer. Its spec is harmful when the recipients are the target's controller's creatures. Alpha Brawl is two steps with no SBA between them (its ruling). Widespread Brutality is left: the amass step records no Army.
**bite-dealer** (2 of 2): `TargetDealsPowerDamageToTargetEffect` takes a set or step-result dealer with a `target()` / `targetDeclaredElsewhere()` recipient (Crush Underfoot, Judgment of Alexander, Alpha Brawl's second step).
**per-player-dmg** (3 of 3): `DealDamageToPlayersEffect.eachEvaluatedFor` (new `amountForEachPlayer` component). Every player's amount is fixed first (CR 608.2h).
**deal-pred** (2 of 2): Searing Rays uses the existing chosen-colour step and predicate. New `PlayerWithoutMaxSpeedPredicate` (CR 702.179e).
**least-tough** (2 of 2): `PermanentHasLeastToughnessAmongOpponentCreaturesPredicate(CountScope)` with OPPONENTS, CONTROLLER or ANY_PLAYER. The least toughness is found among all creatures in scope, and the choice drops what can't be chosen.
**random-target**: not taken (time).

**Behaviour changes.** Enslave deals damage as last known, to the card's owner (test). `attached()` falls back to the recorded host when the source left unattached (Bind the Monster). *Corrected after fold run 23c's sweep:* that fallback broke Followed Footsteps (its ruling: no copy once the Aura went to the graveyard unattached), so `attached()` names nothing again (CR 608.2h, 704.5m; Mechanized Production's ruling agrees) and only Bind the Monster reads `Subjects.lastAttachedCreature()` (`Attached(creature, lastAttached)`), per its own ruling. Enslave has no ruling, so it takes the default and deals no damage; its test was reversed. Crush Underfoot chooses a lone Giant unasked. Purging Scythe no longer offers a creature that can't be chosen. Source-prevention pre-checks are dropped in the rewritten dealer handlers. Damage amounts are fixed first. Alpha Brawl's AI polarity is now HARMFUL_DAMAGE.
**Tests changed.** Crush Underfoot (prompt dropped per CR 608.2d, 2 rewritten, 1 added), The Dining Car (multi-permanent prompt), Enslave (+1).
**Seen, not changed.** Several dealers still deal one dealer at a time (Rakdos Charm, Judgment of Alexander, Alpha Brawl's second step). The TargetDealsPowerDamageToTarget paths keep their source-prevention pre-checks.
**For the next run.** `random-target` (43 cards on `SourceFightsTargetCreatureEffect`). Widespread Brutality waits for the amass step to record CHOOSE.

### Step 9, fold run 23b (2026-11-24, parallel worktree)
Batches `player-counters` with Suncleanser's `all-counters` leftover, plus Final Act's unique record. `counters-among` (with Lizrog), `chosen-counters` and `asenters-counter-removal` are not started. Ratchet 3,547 / 2,839 → 3,542 / 2,834. Ledger 8 rows (absorbed 5, split 1, survivor 2). 5 card files rewritten, Oracle text checked for all 5, Suncleanser's rulings checked. CR 122.1, 122.5, 608.2d, 608.2h and 611.2b checked.

**The shape.**
- `RemoveAllCountersFromTargetPermanentEffect(subject, targetFilter, players, playerCounterType)`:
- Factories: `fromPlayers(players[, PlayerCounterType])` and `fromPermanentsAndPlayers(permanents, players)`.
- It is one REMOVE_COUNTERS step that never prompts. It records the total, the permanents, their controllers and the players that lost counters, and keeps the event value.
- It is one step on purpose: Mutated Cultist's "permanent or opponent" would otherwise need two removals, and the second's NONE would hide the first.
- `PlayerCounterType` (model) names the five tracked player kinds. `GameData.playerCounterCount` and `removeAllPlayerCounters` read and empty each store; energy goes through `removePlayerEnergyCounters`.
- `CantGetCountersEffect.onPermanents(subject, duration)` / `.onPlayers(players, duration)`:
- A floated rule, not an ability, so it outlives the loss of abilities.
- A duration that no longer holds makes nothing (`DurationClock.holds`, CR 611.2b).
- Read by `cantHaveCounters` and the new `GameQueryService.playerCantGetCounters`, which covers Solemnity and the lock.

| Batch | Result |
|---|---|
| `player-counters` | 4 of 4: Med Kit (RAD), Leeches (split: POISON removal + damage over `AmountThisWay(REMOVE_COUNTERS)`), Mutated Cultist (`fromPermanentsAndPlayers`, keeps `EventValue`: the cost reduction fixes only `EventValue`/X as it resolves), Suncleanser's player mode |
| `all-counters` | Suncleanser's creature mode folded: `fromTargetCreature()` + the lock |
| extra | Final Act's `EachOpponentLosesAllCountersEffect` → `fromPlayers(eachOpponent())` |
| `counters-among`, `chosen-counters`, `asenters-counter-removal` | not started |

**Behaviour changes.**
- Suncleanser's player lock now stops rad, experience and placed-by poison counters too (`replacePlayerCounters`, `applyRadCounters`; Oracle text: "can't get counters"). Test written failing first.
- Suncleanser's creature mode gains the last-counter, vanishing and defense checks.
- Energy removal counts as energy lost this turn.
- The AI reads Leeches' damage as `AmountThisWay`.
- The survivor's log lines win.

**Tests changed.** `SuncleanserTest`: rad-lock test added.

**Seen, not changed.**
- The Suncleanser player lock still misses spark counters (`AbilityActivationService.paySparkCost`), proliferate's experience, energy and poison writes (`MultiPermanentChoiceHandlerService`), `AddAnotherCounterOfEachKindToTargetEffectHandler`, `DamagePreventionService`'s rad, `MayHaveThisLandEnterTappedForRadCountersHandler`, `ExperienceCountersEffectHandler`'s direct put after the replace, and `DoubleCountersOnControllerEffectHandler`'s energy. Rule: Suncleanser's ruling. Sure of the code.
- `RemoveCounterFromChosenPermanents` (Chandra, Garnet) removes with `setCounterCount`: no log, no time-counter or oil checks. Sure; `counters-among` fixes it.

**For the next run.** 23c: `counters-among` + Lizrog.
- Widen `RemoveUpToCountersFromAllPermanentsEffect` to (kind or `ANY`, total bound, per-permanent bound, `PermanentSubject` defaulting to `allPermanents()`), recording REMOVE_COUNTERS at the end of its prompt chain.
- Reuse `MoveCountersAmountChoice` with `CounterMoveSlot.toPermanentId == null` as a removal slot. Add a total limit and a per-permanent limit, record the step instead of `putMovedCounters`, and skip the CR 122.5 check (122.5 is for moves only).
- The AI's `givesAway` must handle a null destination.
- Cards: Eventide's Shadow (`ANY`, unbounded), Spider-Man (STUN, 3), Chandra and Garnet (LOYALTY / LORE, 1 per permanent, `controlledBy(you(), f)`), Lizrog (`controlledBy(you(), creature)` + `PutCountersOnSelfEffect(Scaled(AmountThisWay(REMOVE_COUNTERS), 2))`). Followers read `AmountThisWay`.
- Expect about 30 test-interaction rewrites in 5 tests, and delete the three old choice contexts with their `PlayerInputService` and `ChoiceHandlerService` code.
- Then `chosen-counters`: Price of Betrayal's string kinds can take `PlayerCounterType`.

### Step 9, fold run 25b (2026-11-24, parallel worktree)

Batch `add-dmg`. Ratchet 3,532 / 2,825 → 3,521 / 2,824. Ledger 12 rows (absorbed 11, survivor 1). 11 card files rewritten; Oracle text checked for all 16 cards involved; Rem Karolus and Sulfuric Vapors rulings read. CR 113.7a, 608.2h, 609.7b, 613.1f, 614.1a, 614.5, 616.1, 708.2a checked.

| Batch | Result |
|---|---|
| `add-dmg` | 11 folded, 0 left. The survivor gains `sourceControllers` (`you()` / `eachPlayer()`), `sourceFilter` (a `PermanentPredicate`), `spellsOnly`, `players` and `permanents` (`controlledBy(eachOpponent() / eachPlayer(), filter)`), with factories `anotherSource`, `fromSources`, `fromAnyonesSources`, `fromSpells`, `to`, `toOpponentsOnly`, `toAnyPermanentOrPlayer`. Eight readers and six capability interfaces become one, `GameQueryService.getAdditionalDamageBonuses(AdditionalDamageEvent)`: active static effects (face down or lost abilities apply nothing, conditionals unwrapped) plus floated copies, whose "you" is the player they are on. One option per instance in one family, `AdditionalDamageBonus`. The recipient multipliers are untouched, so the order prompt loses nothing. |

Design notes:
- The source is judged as it deals damage. A permanent source is the one on the battlefield or its last known information (CR 113.7a); otherwise its card. Colour leaves of the filter are rewritten from the colours it has for damage: effective permanent or spell colours, then Ghostly Flame (CR 609.7b). That keeps "red or artifact" one predicate.
- "A spell" means damage the spell deals itself, never damage from a fight it causes (Rem's ruling).
- Rankle's turn bonus is floated by `GrantStaticEffectToPlayerEffect.untilEndOfTurn`.
- No card needs a combat-only scope, so `noncombatOnly` stays a boolean.

**Behaviour changes:**
- Sulfuric Vapors, Disciples and Enhanced Surveillance check lost abilities and face down (the P04 defect). Tests.
- Every carrier ignores a face-down permanent; before, only Valley Flamecaller did (Torbran test).
- Ghostly Flame applies to every colour check, Embermaw and Jaya included (Embermaw test).
- A spell's colour is its effective colour; a departed source in the player pool is judged by its last known information. Untested.
- Tok-Tok's bonus stays spent after a redirect (CR 614.5). Untested.
- Embermaw, Valley and Sulfuric options moved from family `SourceBonus` to `AdditionalDamageBonus`. Labels unchanged.

**Also gone:** six capability interfaces (`ControllerOpponentDamageBonusEffect`, `SourceOpponentDamageBonusEffect`, `SpellDamageBonusEffect`, `NoncreatureSourceDamageBonusEffect`, `OpponentDamageBonusEffect`, `DamageToPlayersAndBattlesBonusEffect`, `SubtypeSourceDamageBonusEffect`), the old readers and their int overloads, and `PermanentDamageModifiers.isCreatureExplicitSource`. `applyDamageMultiplier(…, entry)`, used only by tests, no longer predicts the static "plus N" abilities.

**Seen, not changed:**
- `GameQueryService.matchesDamageSourcePredicate`, the prevention source filter ("red sources", `fromSourcesOfChosenColor`), ignores Ghostly Flame (CR 609.7b). Sure of the code, fairly sure of the rule.
- The player damage pool ignores a fighting or biting permanent as the source of damage to a player. Fairly sure.

**For the next run.** Overblaze and Jeska need a timed grant of `DoubleDamageFromCreaturesEffect(source())` onto a permanent, read by `getSourceDamageMultiplier` (fold run 25's note). The Ghostly Flame line above could fold into the colour rewrite this run added (`withDamageColors`) if `matchesDamageSourcePredicate` gets the damage colours.

### Step 9, fold run 28b (2026-11-24, parallel worktree)

Batches `value-binding` and `ability-copy`. Ratchet 3,532 / 2,825 → 3,522 / 2,816. Ledger: 15 rows (absorbed 10, split 1, survivor 3, unique 1). 13 card files rewritten, Oracle text checked for all. Black Gate and Gogo rulings read. CR 608.2d, 608.2h, 611.2c, 603.7a/b, 702.73a, 707.10 and 707.10c checked.

| Batch | Result |
|---|---|
| value-binding | 4 folded, plus Quicken's `GrantFlashToNextSpellOfTypeThisTurnEffect` (4 cards). Each goes through one binder, `SubjectResolutionService.bindChosenValues`, which writes the chosen value into the filter of the rule or grant as it is registered. Walking Desecration is the choose step + `MatchingCreaturesMustAttackThisTurnEffect`. The Black Gate is `ChoosePlayerEffect` + `MakeCreatureBlockableOnlyByFilterThisTurnEffect`, and the bound player covers creatures that player controls later (ruling; CR 611.2c). Progenitor's Icon and Apex Observatory read the source's choice as it is or last was. `GrantFlashToNextMatchingSpellEffect` now resolves itself. Left: Leori (`unique`: the only resolution-time planeswalker-type choice). |
| ability-copy | 5 folded, plus `CopyTargetTriggeredAbilityEffect` (4 cards). `CopyTargetActivatedOrTriggeredAbilityEffect` gains `times(DynamicAmount)` (Gogo X, Mister Fantastic 2) and `.thatCantBeCopied()`. Every ability copy goes through `CopyControllerActivatedAbilityEffectHandler.copy` and one marker, `CopyAbilityRetargetEffect([ability])`. |

**Behaviour changes.**
- Snapshot ability copies (Rings, Firebender Ascension, Ulalek) now keep the chosen permanent, target groups and convoking creatures (CR 707.10). Rings + Impelled Giant test, failed first. The "Unverified" defect-queue line was half right: `createCopyStackEntry` already copied the source snapshot, but not the chosen permanent.
- Card-made copies now also copy convoking creatures and target groups. An entry whose targets are stored as a list now gets the one-at-a-time retarget.
- The survivor's same-controller check is gone. The target filters already say "you control", and Mirror-Shield Hoplite copies an ability whoever controls it.
- The Black Gate's player choice is now parked inside the resolution (`ChoosePlayerEffect`) instead of a standalone completion.
- Log and prompt wording: the survivors' lines win. TheBlackGateTest's block-rejection message assertion was updated.

**Also gone:** `PermanentChoiceContext.BlackGateMostLifeChoice` and its routes; `MayCopyHandlerService.handleCopyActivatedAbilityRetargetChoice` and `handleCopyTriggeredAbilityRetargetChoice`.

**Seen, not changed.** Progenitor's Icon or Apex Observatory with no chosen value now floats a grant that matches nothing, which is invisible; before it registered nothing. I did not touch Leori: its type is bound by its record, and `RegisterGlobalTriggeredAbilityEffect` could bind trigger filters through `bindChosenValues` once a second card needs it.

**For the next run.** Fold `CopyNextSpellCastThisTurnEffect` (4 cards) and `CopyNextInstantOrSorceryCastThisTurnEffect` (21) onto `RegisterGlobalTriggeredAbilityEffect.nextThisTurn` with the abilities their `delayedAbility()` builds. I audited them:
- Each has a normalfx and a manafx handler; the survivor has both.
- `TemporaryGlobalTriggeredAbility.nextThisTurn` sets no watched player, linked exile card or noted amount. `RegisterGlobalTriggeredAbilityEffectHandler` sets them from the entry, so check the cards that target a player.
- Storm King's Thunder reaches `delayedAbility()` directly.
- GameDataDeepCopyTest, KarnLiberatedTest and RunedTerrorTest name the records.

### Step 9, fold run 31b (2026-11-24, parallel worktree)

Both batches are done: 10 folded (9 absorbed, 1 split), plus one survivor row. Ratchet 3,511 / 2,805 → 3,501 / 2,795. 11 card files rewritten; Oracle text checked for all of them; rulings read for Eldrazi Mimic and Shape Stealer.

**Shape.**
- `SetBasePowerToughnessToAmountEffect`:
- A null amount leaves that base value alone, in both the normal and the static handler.
- The `target()` path evaluates against the live source, else its snapshot (CR 608.2h).
- It declares a benign creature target when the subject is `target()`, or when an amount reads the target (`TargetPower`, `TargetToughness`, `TargetPowerPlusToughness`, also inside `Sum` / `Min` / `Max` / `Scaled`).
- `SourceToughness`, `TargetToughness` and `TriggeringPermanentToughness` gain `allowNegative`, matching their power twins. A base-P/T setter keeps negative values (CR 107.1b; the Mimic ruling says so).
- `PermanentDealtDamageToSourceThisTurnPredicate`: the source's `permanentIdsThatDealtDamageThisTurn`. The source is a death trigger's triggering snapshot, else the live source, else the source snapshot (CR 603.10a).

**Settled last-known reads.**
- Shape Stealer's "that creature" is the block trigger's triggering permanent.
- When it leaves, `PermanentRemovalService` overwrites the entry's triggering at-trigger values and last-known maps with its departure values. So `TriggeringPermanent*` equals the old departure-snapshot read.
- The old `combatOpponent*AtTrigger` fallback was unreachable on the same turn, so the fields are deleted.
- Eldrazi Mimic works the same way: the at-trigger value equals the departure value.
- In both cases a read never falls to 0 without information, so there is no 0/0 where the old handler did nothing.

| Batch | Result |
|---|---|
| pt-amount | 8 folded: Wolfbear/Galion, Riptide Mangler, Shape Stealer, Eldrazi Mimic, Halfdane, Sentinel, Wall of Tombstones, Sworn Defender |
| base-pt-new | 2 folded: Brine Hag (set path + the new predicate), Cycle of Life (split onto `aboutAtNext`) |

**Behaviour changes.**
- Logs are the survivors' lines: no "indefinitely", no duration text for next-upkeep durations, and one set line for Brine Hag.
- The set-path log of `SetBasePowerToughnessEffectHandler` now says "until end of turn" only for that duration.
- A triggering permanent or source that is a noncreature permanent on the battlefield reads 0 (CR 208.3). Not tested.
- `TargetPolarityClassifier`: `SetBasePowerToughnessToAmountEffect` is NEUTRAL. It now declares a target (Fractalize), and its card's other effects decide the polarity.

**Tests.** 3 added: the predicate (2) and the Wolfbear negative toughness copy. The route-record tests and `TriggerLedger` no longer name the deleted fields.

**Seen, not changed.**
- `SetBasePowerToughnessEffectHandler`'s set and player-target logs print "null" for a null side. No card hits it. Sure.
- Giant Albatross's blocked `DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect` reads the same damager set by hand. Step 10 should use the new predicate.

**For the next run.** `floated-name` should get a run of its own with The Irencrag (removing `Permanent.persistentName`).

### Step 9, fold run 29 (2026-11-24, parallel worktree)
Two batches of five. Ratchet 3,511 / 2,805 → 3,505 / 2,799. Ledger 10 rows: absorbed 6 (one split), survivor 3, blocked-other 1. 9 card files rewritten, Oracle text checked for all 9. Rulings read for Hive Mind, Curse of Echoes, Parnesse, Magmarch, Ink-Treader and Precursor Golem. CR 101.4, 115.1, 603.4, 608.2d, 608.2h, 707.10 and 707.10c checked.

| Batch | Result |
|---|---|
| spell-copier | 2 folded, survivor kept. `CopyTriggeringSpellEffect.copiedBy(players)` / `.mayBeCopiedBy(players)`: each copier in APNAP order makes and retargets a copy through `CopyControllerCastSpellEffect`. A "may" is a `MayEffect.forPlayer` per copier. A declared `targetPlayer()` declares a benign player target (Parnesse). |
| copy-per-perm | 3 members + the unaudited `CopySpellForEachOtherControlledCreatureEffect` (3 cards) folded. `.targetingEach(PermanentSubject)` makes one copy per named permanent the spell could target, with every target and the divided damage moved onto it and no retarget prompt. Ink-Treader's intervening "if" is a `ConditionalEffect(TriggeringSpellMatches)` (CR 603.4). Magmarch and Beamsplitter choose first with `ChoosePermanentsEffect.exactly(1, …).thatTheTriggeringSpellCouldTarget()`. Feather is left: cost-shape, step 10. |
| card-copy-cast, exile-until-rel, land-or-cast | not started |

**Behaviour changes.**
- Hive Mind and Curse of Echoes copy in APNAP order, not seat order (rulings; new tests).
- Precursor Golem judges Golems by effective subtypes, so a changeling gets a copy (new test).
- "That spell could target" is the spell's full target rules from its controller (`ValidTargetService.permanentIdsSpellCouldTarget`, no Flagbearer; Ink-Treader ruling) for every copy-per-permanent card.
- A copy of a multi-target spell for another player gets the per-target retarget prompt. The copy log line is the survivor's.
- `SubjectResolutionService.controllerOf` reads a departed permanent's last controller from the departure snapshots (CR 608.2h). This keeps Magmarch's "another opponent" (new test).
- The copy handler finds the triggering spell through `CopySupport.triggeringSpell`; a stack ability is never copied.

**Seen, not changed.**
- Precursor Golem and Ink-Treader: the copies' controller doesn't choose the order they go on the stack (rulings). Sure.
- Feather's {2} per creature is still paid from the pool only. Fairly sure.

**For the next run.** Do `card-copy-cast` next (7 members; it needs zones outside exile, a {2} reduction, an up-to-N cap and the accept path), then `exile-until-rel`, then `land-or-cast`. Feather can fold once a pay-per-chosen step with mana abilities exists.

### Step 9, fold run 26b (2026-11-24, parallel worktree)
The `random-target` batch: 6 rows; 3 absorbed, 2 split, 1 survivor, 0 left. Ratchet 3,495 / 2,789 → 3,490 / 2,784. CR 601.2c, 603.3d, 608.2b, 700.13, 701.14a and 701.14b were checked.

**Which "random" each card is.** "Target … chosen at random" is a real target, picked as the ability goes on the stack. These cards are:
- Goblin Test Pilot (any target);
- Scab-Clan Giant (a creature an opponent controls);
- Witch Hunt (target opponent);
- Furnace Layer (target player);
- Knight Rampager's death trigger (target opponent);
- Explosion of Riches' reflexive ability (target opponent).

"An opponent chosen at random" with no "target" is a choice made on resolution: Zaffai and Truth or Consequences, done with `ChoosePlayerAtRandomEffect` + `playersThisWay(CHOOSE)`. Knight Rampager's combat trigger is also the non-target kind, but it is not in this batch.

**Design.**
- `TargetSpec.chosenAtRandom` (wither `atRandom()`) is the one switch, and `CardEffect.targetChosenAtRandom()` reads it. The stack door and the activation path read only that.
- Player verbs set it with `Subjects.targetPlayerChosenAtRandom()`. `DealDamageToPlayersEffect`, `DiscardEffect` and `GainControlOfTargetEffect`'s gainer pass it into their specs.
- `DealDamageToAnyTargetEffect` sets it with `toAnyTargetChosenAtRandom`, a negative group marker like the existing opponent's-choice factory. `SourceFightsTargetCreatureEffect` sets it with `atRandom()`.
- Power Pack's record now declares it through its spec as well.
- An opponent-only player target is the card's target filter (Witch Hunt, Knight Rampager) or the reflexive record's own filter (Explosion of Riches).

**Survivor.** `SourceFightsTargetCreatureEffect`'s fight form now goes through `FightSupport.fight`, which the Scab-Clan Giant handler already used.
- Every fight card now counts as having fought (Boxing Ring).
- "Can't deal damage" is checked.
- Each creature deals damage as itself.
- The log is "X fights Y.".

The not-a-fight departed-source form (`false`) keeps its own path.

**Behaviour changes.**
- A target chosen at random by a trigger fires "becomes the target" abilities and the crime event (Frost Titan, Blood Hustler tests). Before, `putWithRandomTarget` fired neither.
- Goblin Test Pilot no longer uses a target the activating player names.
- Witch Hunt is not a steal in the AI's view.
- The random player specs (Knight Rampager, Furnace Layer, Explosion of Riches) are now benign. Nobody chooses these targets.
- Truth or Consequences chooses its opponent even with no consequences votes, as the Oracle text says (log only).

**Tests changed.** None rewritten. Added one each to Scab-Clan Giant, Witch Hunt, Goblin Test Pilot, Knight Rampager and Pheres-Band Brawler.

**Seen, not changed.**
- No seeded game random source exists: about 180 `ThreadLocalRandom` sites. The random picks stay unfixable in tests.
- `ActivatedAbility.isNeedsTarget` still makes the client ask for Goblin Test Pilot's target, which the server now ignores.
- Power Pack's random graveyard target goes on the stack as `nonTargeting(true)`: no targets-chosen event (CR 601.2c).

**For the next run.** The seeded-random question is for the owner. Power Pack's non-targeting random graveyard target could join a defect run.

### Step 9, fold run 34 (2026-11-24, parallel worktree)
Six of seven batches done. Ratchet 3,511 / 2,805 → 3,493 / 2,800. Ledger 25 rows: absorbed 18, survivor 6, unique 1. 20 card files rewritten; Oracle text checked for 22 cards.

| Batch | Result |
|---|---|
| static-cast-restr | 4 folded, 1 unique. The survivor's players also take `controllerOf(attached())`, judged by the new `SubjectResolutionService.namesPlayer(GameData, …)`. Predicate amounts are bound for the casting player (`bindAmounts(…, asSpell)` counts X, CR 202.3e). Before X is chosen, an {X} spell is stopped only if X = 0, 1 and a large X all stop it. Wardscale stays `unique` (its static defending-player subject would have one user). |
| restr-filter | 4 folded. `PlayerRestriction` gains `castingSpellsMatching(filter)` (bound as it resolves through `bindTriggeringSpellManaValue(…, asSpell)`, CR 611.2c), `playingCardsFrom(zones)` (PLAY_CARDS, CR 701.18b) and `.from` / `.fromAnywhereBut`. `isOpponentsManaValueSpellCastRestricted` is renamed `isSpellRestrictedByResolvedFilter`. |
| zone-play | 5 folded into `PlayersCantCastSpellsFromZonesEffect(PlayerSubject, PlayerRestriction)`, the static form of the restriction; the old constructors stay. One reader, `GameQueryService.isPlayerRestricted`, covers resolved and static restrictions over `activeStaticEffects`, so Rock Jockey's condition still works. The land-free and noncreature forms are allowed with zones. `GameData.isRestricted` counts only zone-free restrictions. |
| search-prohib | 2 folded. `CantSearchLibrariesEffect(players, payable)`; Shadow of Doubt floats `(you(), false)` on each player, and "you" is read as the player the copy is on. |
| cast-lock | not started (6 records with spell, ability and condition readers: needs its own run) |
| faceup-prohib | 1 folded: Karlov is a `ConditionalEffect(ControllerTurn, …)`; `isTurnFaceUpPrevented` reads the layered pass only. |
| cost-prohib | 2 folded, no new record: `PlayersCantPayLifeEffect(includingManaAbilities, sacrificeFilter)`, read over `activeStaticEffects` behind a printed-static prefilter. |

**Behaviour changes.**
- Iona reads a spell's colours after effects.
- The static zone and land prohibitions skip face-down and ability-less sources (CR 708.2a, 613.1f). This fixes the three can't-play/cast callers in defect line "`controlsStatic`'s remaining callers"; Ward of Bones remains.
- In `canCastSpellFromZone` a land card is stopped only by land or card-play restrictions (CR 305.1).
- Logs: Avatar's Wrath, Memory Vessel, Shaman's Trance and Living Breakthrough log the survivor's line; Shadow of Doubt's line is gone.

**Tests changed.** Fixtures that named deleted records were moved (`CastingPermissionServiceTest`, `GameQueryServiceTest`, `SpellCastingServiceTest`, MemoryVessel, NewFrontiers, NobleBenefactor, ShadowOfDoubt, WeirdHarvest). Added: Brisela X playability, two `namesPlayer` tests.

**Seen, not changed.**
- `canPayLifeForCosts` takes no player, so a per-player "can't lose life" or "life total can't change" (Platinum Emperion) is not asked on the cost paths (CR 119.8). It is unverified whether those paths check it elsewhere. Fairly sure of the code.
- Shaman's Trance and Tomik are judged by zone, not by whose graveyard: "from their graveyards" also stops a cast from another player's graveyard. Sure of the code, low impact.
- The face-down and lost-ability fix and the land exemption above have no test of their own.

**For the next run.** Take `cast-lock` onto `OpponentsCantCastOrActivateDuringYourTurnEffect`. It needs a static `PlayerSubject`, a spell filter (or `PlayerRestriction`), an ability filter and a `Condition` judged for the restricted player. Teferi's sorcery timing must stay a timing rule (CR 307.5).

### Step 9, fold run 23c (2026-11-24, parallel worktree)
Batches `counters-among` with Galloping Lizrog, and `asenters-counter-removal` (verdicts only). `chosen-counters` not started. Ratchet 3,479 / 2,783 → 3,476 / 2,780. Ledger 6 rows (survivor 1, absorbed 3, unique 2). 5 card files rewritten, Oracle text and rulings checked for all 5. CR 122.1, 122.5, 608.2d, 310.12b, 614.1c, 702.63a and 704.5v checked.

**The shape.**
- `RemoveUpToCountersFromAllPermanentsEffect(kind, maxAmount, PermanentSubject permanents)` and `anyNumber(kind, permanents)`:
- The subject is non-target and defaults to `allPermanents()`. `CounterType.ANY` means every kind.
- The handler asks one 0..N question per permanent and kind. These are removal slots of `MoveCountersAmountChoice`: `CounterMoveSlot.toPermanentId == null`, plus a new `totalLimit`, and a slot's maximum is what the total leaves (CR 608.2d).
- The counters all come off once the last answer is in, through `removeCounterFromPermanent`. That sends the oil and vanishing checks, plus `notifyLastCounterRemoved` and the Siege check.
- It records REMOVE_COUNTERS (total, permanents, controllers). It skips the CR 122.5 check, which is for moves only.
- "Remove a [kind] counter from each of any number of [permanents]" needs no new piece. It is `ChoosePermanentsEffect.anyNumber(...)` + `RemoveCounterFromSourceEffect(thisWay(CHOOSE), kind)`.

| Batch | Result |
|---|---|
| `counters-among` + Lizrog | 4 of 4: Eventide's Shadow and Lizrog onto the survivor; Spider-Man moved to `AmountThisWay`; Chandra and Garnet by parameters (same multi-permanent prompt) |
| `asenters-counter-removal` | 0 folded, 2 `unique`. Folding Thief onto Sin's record needs an all-matching option only Thief would set (guard rail). It would also move Thief off its entry replacement, which removes the counters before it enters (CR 614.1c), onto Sin's choice path, which runs after placement |
| `chosen-counters` | not started |

**Behaviour changes.**
- Eventide's Shadow: a Siege's last defense counter removed triggers its defeat ability (CR 310.12b, test written failing first).
- Removals now log per permanent and send the last-counter event: Eventide, Spider-Man, Lizrog, Chandra, Garnet.
- AI: a removal slot removes everything (Lizrog's AI used to remove 0). `ChosenPermanentsAiChoice` prefers its own permanents for a LORE `RemoveCounterFromSourceEffect` over `thisWay(CHOOSE)`, so it picks its Sagas for Garnet. For Chandra's 0 it picks nothing; the old fallback picked one.
- The survivor's prompts and logs win.

**Tests changed.** `EventidesShadowTest` (interactions, 2 display names, 1 Siege test added), `SensationalSpiderManTest` (interactions, an at-most-3 check).

**Defect fixed.** "Chandra, Legacy of Fire and Garnet remove counters with `setCounterCount`" (Fold: counters-among). For loyalty and lore counters nothing changes in the rules: the counters-removed triggers already fired at the SBA check. The existing tests cover it.

**Seen, not changed.**
- Sin's handler (`MultiPermanentChoiceHandlerService.handleRemoveAllCountersAsEntersForCounters`) removes counters with `setCounterCount`. It makes no vanishing last-time-counter check (CR 702.63a) and sends no last-counter event, so Divine Intervention, an enchantment, is missed. Sure of the code.
- Thief of Blood's entry handler sends no last-counter event (Divine Intervention). Sure of the code.
- Sin's after-placement choice may offer permanents entering in the same batch. Unverified.

**For the next run.** 23d: `chosen-counters` (Heartless Act, Price of Betrayal, Rhys, Tetravus, Hex Parasite). All five are "remove up to N / any number of counters from one permanent (or opponent)". The removal slots built here could carry them if the survivor gains:
- a target subject;
- a `DynamicAmount` bound (X for Hex Parasite);
- player slots with `PlayerCounterType` (Price of Betrayal).

The followers are `CreateTokenEffect(AmountThisWay)` tracked as created with the source (Tetravus) and `BoostSelfEffect(AmountThisWay)` (Hex Parasite). First decide whether the queue's survivor `RemoveChosenCountersFromTargetPermanentEffect` (5 cards, its own `RemoveChosenCountersChoice`) moves onto the same slots too. That rewrites 5 more cards' tests.

### Step 9, fold run 33 (2026-11-24, parallel worktree)
Batches `land-mana`, `mana-twist`, `mana-drain`, `cost-paid-land`, `player-mana` and `mana-rider` are done. `produced-mana` and `persistent-mana` are left. Ratchet 3,490 / 2,784 → 3,477 / 2,776. Ledger 24 rows (absorbed 14, survivor 7, unique 1, `step-9` 1). 14 card files rewritten, Oracle text checked for all 14. CR 106.4, 106.6, 106.6a, 106.7, 106.12a, 106.12b, 305.7, 605.1a, 605.1b, 605.3a, 608.2h, 614.1a and 616.1 checked.

| Batch | Result |
|---|---|
| land-mana | 6 of 7 folded onto `ReplaceLandManaWithColorEffect` (filters `forLands`/`youControl`, `whenYouTap`, `replacing(types)`, `producing(n)`, `on(subject)`, `lasting(Duration)`). Static or floated by its new handler. One reader (`GameQueryService.landManaColorReplacements`) and one pool source (`LandManaColorReplacements`). Each text is a mana replacement (CR 614.1a, 106.12b), not a type change (CR 305.7). Hall of Gemstone keeps its choice record (unique); its floated payload is the survivor `.replacing(COLORS)`. |
| mana-twist | `TwistBasicLandManaColorsEffect` became the record with the map. It stays apart from the survivor: a twist is one replacement whose multi-type lands choose a colour (2013-04-15 ruling). |
| mana-drain | `DrainTargetPlayersLandManaEffect(PlayerSubject, delayedColorless)`. The drained player travels on `LandManaDrainAbilityChoice` / `LandManaDrainContinuation`. |
| produced-mana | Not started: the two tap fallbacks differ (Ultima: the land's mana abilities, which covers Wastes; Caged Sun: basic land types). Check first whether any tap path still reports no produced type. |
| persistent-mana | Left: the option would be Radha's alone. Fold it with `AwardPersistentManaEffect` (19) and `AwardPersistentAnyColorManaEffect` (3). |
| cost-paid-land | `ManaColorLandScope.SACRIFICED_FOR_COST` / `UNTAPPED_FOR_COST` (`paidInCost()`) on `AwardManaOfTypeLandsCouldProduceEffect`. The inline branches and collectors are gone. |
| player-mana | `AwardManaToActivePlayerEffect.to(PlayerSubject)`; `null` keeps the recorded active player. Jetfire is no longer a `ManaProducingEffect` (targeted, CR 605.1a). |
| mana-rider | `AwardHasteGrantingManaEffect.Rider` (haste until end of turn, permanent haste, instant/sorcery can't be countered) takes Boseiju. `ManaCopyRiderEffect` stays the survivor; Primal Wellspring was not moved. |

**Behaviour changes.**
- Harvest Mage replaces an opponent's land you tap through Piracy ("if you tap a land"). Test.
- Benthic Explorers' mana is doubled by Mana Reflection (CR 106.12b). Test.
- A sole type from `AwardManaOfTypeLandsCouldProduceEffect` goes through False Dawn (`effectiveColor`), so Reflecting Pool now matches Squandered Resources. Test.
- The survivors' labels and logs win.

**Tests changed.** `DeepWaterTest` and `SunkenPalaceTest`: the option prefix is now "Deep Water". `GameQueryServiceTest`: asks about a real land. `RandomAiDecisionEngineTest` and `GameQueryServiceTest`: float the survivor. Three tests added.

**Seen, not changed.**
- Reality Twist offers U for a Volcanic Island (an unlisted Island type keeps its colour). `RealityTwistTest` asserts that, and the 2013 ruling is ambiguous for unlisted types. Unsure.
- Reflecting Pool, Cactus Preserve, Horizon of Progress and Plaza of Harmony report base amount 0 to `LandManaReplacementService.produce`, so Infernal Darkness never replaces their mana. Fairly sure of the code.
- Boseiju and Hall of the Bandit Lord: when a land-mana replacement applies, the produced mana may lose its rider (CR 106.6a). Unverified.

**For the next run.** `produced-mana`: settle the fallback first, then one `AddManaOfTypeProducedByTappedPermanentEffect` with a colour (fixed or the source's chosen colour) and a recipient. Then `persistent-mana` with the two persistent siblings.

### Step 9, fold run 29b (2026-11-24, parallel worktree)
`card-copy-cast` done, `exile-until-rel` started (1 of 4), `land-or-cast` not started. Ratchet 3,453 / 2,758 → 3,448 / 2,753. Ledger 11 rows: absorbed 4, split 1, blocked-other 3, survivor 3. 5 card files rewritten plus `CipherSupport`; Oracle text checked for all. Rulings read for Kefnet, Zemo, Volute, Reversal, Arcane Bombardment, Eye of the Storm, Possibility Storm and Tibalt's Trickery. CR 118.9, 305.1, 601.2f, 607.2a, 608.2g, 702.99a, 707.10, 707.10a and 707.12 checked.

| Batch | Result |
|---|---|
| card-copy-cast | 4 folded, 3 left. Survivor `CopyImprintedCardAndMayCastCopyEffect`: a card subject's cards are copied from exile, a graveyard or a hand (the copy is parked in exile). `castsAtMost` (`castingUpTo(n)` / `castingAnyNumber()`) offers copies of the named spell cards in one ordered `ImprovisationCapstoneCastChoice` (CR 608.2g); free only, and unchosen copies cease to exist (CR 707.10a). Zemo: `of(exiledAsCost()).castingUpTo(3)`. Reversal: `ChooseCardsFromTargetHandEffect` (KEEP_IN_HAND, instant or sorcery, up to one, hand revealed) + `of(cardsThisWay(CHOOSE))`; `TargetHandSpellCopyChoice` and its handler, projection and AI strategy are gone. Arcane Bombardment: split into `ExileCardAtRandomEffect.fromGraveyard(you(), instant or sorcery).withSource()` + `of(exiledWithSource()).castingAnyNumber()`. Cipher: `CipherSupport` grants `of(exiledCards(CardIdSetPredicate(encoded id)))`. Left, see below. |
| exile-until-rel | 1 folded: Krang & Shredder → `ExileCardsFromTopUntilEffect.untilNonland(eachOpponent()).withSource()`. Possibility Storm, Tibalt's Trickery and Reweave not started. |
| land-or-cast | not started |

**Left.**
- Kefnet (blocked-other): the {2} reduction would be a one-card option (guard rail). The copy is also created in and cast from the hand (ruling, CR 707.12).
- Spellweaver Volute (blocked-other): needs a card subject for an Aura's enchanted card as the Aura last existed, a graveyard-zone copy, and the reattach step.
- Eye of the Storm (blocked-other): needs a caster subject (the triggering spell's caster) and a non-target exile of the triggering spell with the source.

**Behaviour changes.**
- Cipher asks once (the copy step's "may", CR 702.99a) and casts in the resolution path, with the CR 601.3 check.
- Arcane Bombardment and Zemo show the ImprovisationCapstone choice. Arcane's test was updated for the new prompt type, and a new test covers unchosen copies.
- Reversal's card choice is the revealed-hand index prompt; its test was updated.
- Krang records EXILE and logs the survivor's line.
- Zemo no longer skips "this spell can't be copied" cards. That flag is about a spell, not a card; no black card has it, so nothing observable changes.

**Seen, not changed.**
- Kefnet, Reversal and Volute copies are created and cast in exile, not in the hand or graveyard the ruling names (CR 707.12). A "cast from hand / graveyard" trigger would miss them. Sure of the code.

**For the next run.**
- `exile-until-rel`:
- Possibility Storm and Tibalt's Trickery can use the new `ExileCardsFromTopUntilEffect.withSource()`.
- Both still need a spell-relative filter bound as the step resolves: "shares a card type with it", its types as last known and face-down aware; "nonland with a different name than that spell".
- Possibility Storm also needs a non-target "exile the triggering spell with the source" step.
- Tibalt's Trickery also needs a random 1-to-3 mill amount.
- Both need "may cast that card, then put the exiled cards on the bottom in a random order", with the bottoming after the cast completes. Check `MayCastDiscardedCardWithoutPayingManaCostEffect` against the step-model rule first. Today's `deferredExileBottomSourceIds` exists for exactly that wait.
- Reweave is a reveal (`AsksDeathReplacementsFirst`), not an exile step; it needs a filter over a departed permanent's last-known types.
- `land-or-cast`: a mandatory "land onto the battlefield, else cast it free if able" step. Omen Machine refuses any card with a cost increase today; CR 601.2f says the increase is paid. It also skips additional costs (the "Fold:" defect).

### Step 9, fold run 27 (2026-11-24, parallel worktree)
5 of 8 batches done: 14 folded (11 absorbed, 2 split, plus Equal Treatment), 1 unique. Ratchet 3,459 / 2,764 → 3,446 / 2,756. 17 card files rewritten, Oracle text checked for all. Saving Grace, Ascent and Gideon rulings read. CR 608.2b, 608.2d, 609.7b, 611.2c, 614.5, 614.9, 615.1, 615.6, 615.7, 615.12 checked.

| Batch | Result |
|---|---|
| `redir-your` | 6 of 6. `RedirectYourDamageToTargetCreatureThisTurnEffect(destination, RedirectedDamage, damageSource, combatOnly, Duration, targetFilter, destinationMayBePlaneswalker)` with `.to`, `.lasting`, `.combatDamageOnly`, `.dealtBy`, `.targeting`, `.orPlaneswalkerDestination`. `RedirectedDamage` is YOU, YOU_AND_CREATURES, YOU_AND_PERMANENTS or CREATURES. One `TurnDamageRedirectToCreatureShield` shape, and Ascent's `CreatureControllerDamageRedirectShield` and its rule merged into it. Ascent and Gideon are `ChoosePermanentsEffect` plus `thisWay(CHOOSE)`; both choice contexts are gone. The target is harmful only when the destination takes only your damage (Turn the Tables). |
| `redir-creature` | 2 of 3. `RedirectNextDamageEffect.allThisTurn(protected, destination, target)` (Karona, Sivvi). Shield Dancer is `unique`: its source filter and combat-only scope would be set by it alone. |
| `redir-source` | Not started. 8 different shields and readers (Harsh Judgment's static, Reverberation, Mirror Strike, Psychopath, Soltari, Aegis, Reflect Damage, Opal-Eye). It needs one source-keyed redirect shield, and a choose-a-damage-source step for Opal-Eye, Reflect Damage and Desperate Gambit. |
| `redir-absorb` | Not started. Weathered Bodyguards needs `combatOnly` on `RedirectPlayerDamageToSelfEffect`, and the Royal Guard's floated form needs the floated copy to know its "this creature". `GrantStaticEffectToPlayerEffect` records no source permanent, and 111 readers use `FloatingContinuousEffect.sourcePermanentId`. |
| `next-dmg-shields` | Not started. Doubling and combat-only (Impulsive Maneuvers) and a fixed amount (Barbed Wire) would each be set by one card until Desperate Gambit's choose-a-source step exists. |
| `cant-prevent` | 3 of 3. `DamageCantBePreventedEffect(sources, combatOnly)`: `()`, `.combatDamage()`, `.dealtBySource()`, `.combatDamageDealtBy(controlledBy(you(), creature))`. One reader family: `isDamagePreventable`, `isCombatDamageCantBePrevented`, and `damageCantBePreventedFromSource` (own and granted copies on the source's controller's permanents, `staticSubjectNames`). |
| `dmg-counters` | 2 of 2. `PreventDamageAndAddPlusCountersEffect(CounterType, Kind)` with REPLACE (Phytohydra, Lichenthrope), PREVENT_AND_PUT (Panther Habit: counters even on unpreventable damage, which is dealt, CR 615.12) and PREVENT_PUT_PER_PREVENTED (Phyrexian Hydra). One creature-damage step per counter type. |
| `dmg-threshold` | 1 of 1. Equal Treatment floats `ReplaceDamageAboveThresholdEffect` through `GrantStaticEffectToPlayerEffect.untilEndOfTurn`, read for every recipient. |

**Behaviour changes.**
- Ascent drops creatures that can't be chosen (CR 608.2d; test).
- Ascent and Gideon choose a lone candidate unasked, and Gideon now asks with a single-permanent prompt.
- Ascent's redirections are per-instance options, and damage one already moved isn't moved again (Saving Grace ruling).
- Shimian, Turn the Tables and Heroic redirect whenever the destination is a creature at damage time, not only if it was one at resolution (CR 614.9). Untested.
- Questing Beast's reader checks the source is a creature; can't-be-prevented checks read active static abilities.
- Karona and Sivvi log the survivor's line; Equal Treatment's line is gone; a self-redirect installs nothing.

**Tests changed.** AscentOfTheWorthyTest (shield class; lone-candidate prompt dropped; +1 for CR 608.2d), GideonsSacrificeTest (prompt helper), lenient stubs of deleted methods in AbstractDamageHandlerTest and CombatDamageServiceTest.

**Seen, not changed.**
- The damage-to-counters step reads `getCard().getStaticEffects()` with only `hasLostAllAbilities`; a face-down Phytohydra-like creature may still apply. Unverified.
- Kjeldoran Royal Guard's float sets `affectedPermanentId` on a player-scoped floating effect. Code.

**For the next run.** `redir-source`, `next-dmg-shields` and Desperate Gambit together: a choose-a-damage-source step (permanents and stack objects you control, recording CHOOSE) and one source-keyed redirect shield (source, recipient, destination, amount or next event, combat-only, Duration). Then `redir-absorb` once a resolution-floated static can name its source.

### Step 9, fold run 32 (2026-11-24, parallel worktree)

13 folded, 12 blocked-other, 1 unique, 4 not started (mv-animation, floated-name). Ratchet 3,459 / 2,764 → 3,447 / 2,752. 21 card files rewritten, Oracle text checked for all. CR 205.1a, 205.1b, 205.3d, 205.3g, 205.3h, 205.3q, 301.5c, 305.6, 305.7, 611.2c, 612.2, 613.1d, 613.1f, 613.4b, 613.4c, 613.6, 613.7a, 613.8a, 608.2b, 608.2h, 115.3, 601.2c, 704.5n and 704.5p checked.

| Batch | Result |
|---|---|
| set-card-types | 1 folded: Imprisoned in the Moon and Song of the Dryads are `SetCardTypesEffect({LAND}, attached())`; the layer case was fixed (Fold: defect) |
| type-removal | 2 folded onto `LoseAllLandTypesEffect.subtypes(...)` (resolved, floated, Equipment unattach kept) and `.supertype(...)` (layered pass) |
| land-type-static | 5 folded onto `GrantSubtypeEffect` (P05's reading: it already takes a subject and filter, and its overriding basic land type is the same CR 305.7 setter as Blood Moon's); `ChosenSubtype` for Convincing Mirage, Phantasmal Terrain, Thran Portal, Multiversal Passage and Illusionary Terrain |
| land-types-oneshot | 2 folded (Nightcreep; Elsewhere Flask and Terraformer) onto `GrantBasicLandTypeToTargetEffect.of(lands, …)`. Vision Charm left: no land-type choice step, one card. Energybending left: its own-type skip guards against the in-addition path's explicit mana ability |
| loses-abilities | 1 folded: `LosesAllAbilitiesEffect.exceptManaAbilities`; the two staticfx handlers merged, readers guarded |
| mv-animation | not started: three board tables, the 7b injection and the GameQueryService creature fallbacks need a run of their own |
| set-boost | 3 left: Haunted One needs `otherPermanentsSharing` to read a departed permanent as last known (CR 608.2h; live-only would narrow it). Phyresis Outbreak and Jurin need per-boosted-creature amounts (step 10's boost merge, 5i) plus a one-card amount |
| perm-static-boost | 1 folded: Heroes' Podium and Jodah onto `DynamicStaticBoostEffect(PermanentCount(legendary creature, CONTROLLER, excludeSource), …, amountsFromTarget)`; Jodah's +1/+1 `StaticBoostEffect` kept for the AI. Attack count left (AI reads `StaticCreatureBoostEffect`). Bushido, creature-type count, colour count and chroma left: each would be a one-card amount |
| target-groups | unique: Arm the Cathars is one ordered group; three groups would change the cast prompt and need a one-card constraint |
| floated-name | not started (its own run with The Irencrag) |

**Behaviour changes.**
- `SetCardTypesEffect` drops the enchantment and Siege subtypes of removed types and keeps creature types with kindred (CR 205.1a). Shrine test, which fails on the old case.
- Static supertype removals are applied in the layered pass, so they follow timestamp order and dependency. A layer-6 loss of the source's abilities no longer switches them off (CR 613.6). `hasEffectiveSupertype` reads the layered state on the battlefield. Melting test.
- Conversion and Glaciers are now text-changeable (CR 612.2, the P09 defect). Magical Hack test.
- Thran Portal's in-addition type no longer records a land-type override, so its mana comes from its effective types (CR 305.6).
- Logs: one line per land for set land-type changes; the "no longer [subtype]" line is the new resolved path's.

**Seen, not changed.**
- Energybending skips a type the land has printed, so under Blood Moon a Taiga misses Forest (CR 305.7, 613.7). Fairly sure.
- `losesNonManaAbilitiesAsLandEnters` ignores full "loses all abilities" statics on lands (Alpine Moon) for entering lands' replacements (CR 614.12). Unverified.
- `GrantBasicLandTypeToTargetEffect`'s in-addition path grants an explicit mana ability beside the intrinsic one, so a land that already had the type has it twice. Sure of the code, low.

**For the next run.** Take mv-animation alone (merge `marchAnimatedIds` / `starfieldAnimatedIds` / `opalescenceAnimatedIds`, a per-permanent base-P/T amount, Spark Rupture's planeswalker read from the state). Then floated-name with The Irencrag.

### Step 9, fold run 28c (2026-11-24, parallel worktree)
Both members folded, 0 left. Ratchet 3,430 / 2,748 → 3,428 / 2,746. Ledger 3 rows (absorbed 2, survivor 1). 26 card files rewritten, Oracle text checked for all. CR 603.7a, 603.7b, 603.7d, 603.7e, 608.3f, 702.140a, 707.10 and 707.10c checked.

| Batch | Result |
|---|---|
| next-time (28b leftover) | `CopyNextInstantOrSorceryCastThisTurnEffect` (21 cards) and `CopyNextSpellCastThisTurnEffect` (4) are now `RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(filter, List.of(CopyTriggeringSpellEffect…))`. "With mana value N or less" is `CardPredicateUtils.instantOrSorceryWithManaValueAtMost(N)`. "Twice" is `.withCopies(2)`. "For each …" / "X times" is `.withCopies(DynamicAmount)` (Thunderclap Drake, Storm King's Thunder). "Copy it" of any spell is `.permanentSpellCopiesAreTokens()`, and The Clone Saga adds `.withoutSupertypes(LEGENDARY)`. Four records and handlers gone (2 normalfx, 2 manafx). |

**Survivor.** Not `RegisterGlobalTriggeredAbilityEffect.nextThisTurn`, which 28b proposed. The spell-cast survivor builds the same `TemporaryGlobalTriggeredAbility.nextThisTurn` as the deleted handlers and keeps the noted amount. Its mana-ability path is `ActivatedAbilityExecutionService`'s inline branch (Ether). It adds no registration log line and no watched-player or linked-exile fields. So nothing changes, not even the log.

**Behaviour changes.** None.

**Tests changed.** GameDataDeepCopyTest, KarnLiberatedTest and RunedTerrorTest build the same delayed abilities through the survivor's `delayedAbility()`.

**Seen, not changed.** The Clone Saga II says "copy it" with no "you may choose new targets", but the copy still offers new targets (CR 707.10). This is not observable today: the engine casts no creature spell with targets, because mutate is not a casting option. Fairly sure.

**For the next run.** Nothing is left in fold run 28's rows. `RegisterDelayedControllerSpellCastTriggerEffect` and `RegisterGlobalTriggeredAbilityEffect` overlap on "next this turn"; that is a step-10 question.

### Step 9, fold run 34b (2026-11-24, parallel worktree)
`cast-lock` done: 6 absorbed, 1 survivor, 1 dead (interface). Ratchet 3,430 / 2,748 → 3,424 / 2,748. 10 card files rewritten; Oracle text checked for 19 cards.

| Batch | Result |
|---|---|
| cast-lock | 6 folded into `OpponentsCantCastOrActivateDuringYourTurnEffect(PlayerSubject players, PlayerRestriction spells, PlayerRestriction abilities, Condition condition)`. The condition is judged with the restricted player as the context's target: `ControllerTurn` (Abolisher and the spells-only cards), `NotCondition(TargetPlayerTurn)` (City, Dosan, Fires), `DuringCombat` (Hand to Hand, Basandra, Marisi, Megatron), `StackIsEmpty` (Priority Avenger), `NotCondition(TargetPlayerCouldCastSorcery)` (Teferi ×2). Factories: `.onlyDuringTheirOwnTurns`, `.onlyAtSorceryTiming`, `.duringCombat`. `SpellCastingTimingRestrictionEffect` deleted. |

**Readers.** `GameQueryService.findRestriction` (behind `isPlayerRestricted`) reads resolved restrictions, `PlayersCantCastSpellsFromZonesEffect` and the survivor. `restrictedSpellTypes` covers "can't cast instant spells". `abilityLockRefusal(player, source)` covers City and Abolisher (mana abilities included, CR 605.3a gives no exception, CR 101.2). `nonManaAbilityLockRefusal` covers Abeyance and Hand to Hand. `isHeldToSorceryTiming` replaces Teferi's two readers and is used by cast timing, `canCastDuringResolution` and the two suspend casts. A resolution is never sorcery timing (CR 608.2g; Teferi, Mage of Zhalfir's ruling), because an ability leaves the engine's stack before it resolves. Teferi stays timing only (CR 307.5): nothing reads it as a sorcery cast.

**Behaviour changes.**
- Hand to Hand now also stops activating an ability of a spell on the stack during combat. Oracle: "abilities that aren't mana abilities". The stack-ability path read only Abeyance. HandToHandTest covers it.
- Basandra, Marisi and Megatron stop casting at player level (`stopsEverySpell`), not by an all-types set. This matches the convention "a can't cast spells restriction is never a type set".
- Teferi is also refused through `isPlayerPreventedFromCasting`; the timing hook is kept.
- City and Teferi are now read from granted static abilities too (the old readers saw printed ones only).
- Error messages are built from the restriction and the condition: "You can't activate abilities outside your own turn", "… of artifacts, creatures, or enchantments during your opponent's turn". The existing `hasMessageContaining` assertions all hold.

**Tests changed.** `CastingPermissionServiceTest`'s Silence case gained the class's own `isPlayerRestricted` stub, because the mocked `GameQueryService` now answers that check. Added HandToHandTest "An ability of a spell on the stack can't be activated during combat".

**Seen, not changed.**
- Priority Avenger: while an activated or triggered ability resolves, the engine's stack can be empty, so "unless a spell or ability is on the stack" restricts an instant cast during that resolution. This is unchanged from before. Sure of the code, low impact.
- `PlanarAbilityService` still reads `GameData.isRestricted(ACTIVATE_NON_MANA_ABILITIES)` only, not Hand to Hand. Unverified whether any planar activated ability exists. Low.

**For the next run.** The survivor's name no longer says what it does (step 10: `PlayerCastLockEffect`-style rename, if approved). `PlayersCantCastSpellsFromZonesEffect` and the survivor are two static restriction records over one `PlayerRestriction` vocabulary, so step 10 could merge them, with the condition as the only difference.

### Step 9, fold run 23d (2026-11-24, parallel worktree)
Batch `chosen-counters`, the last Run 23 batch.
- Ratchet 3,427 / 2,745 → 3,422 / 2,740.
- Ledger 6 rows (absorbed 3, split 2, survivor 1).
- 10 card files rewritten; Oracle text checked for all 10. Rulings checked for Cemetery Desecrator, Thornmantle Striker, Price of Betrayal, Hex Parasite, Tetravus and Rhys.
- CR 122.1, 122.5, 608.2b, 608.2d and 611.2a checked.

**The decision.** `RemoveChosenCountersFromTargetPermanentEffect` and its `RemoveChosenCountersChoice` moved onto 23c's removal slots too.
- Counters of one kind are interchangeable (CR 122.1), so "pick one counter at a time, or Done" says the same as one how-many question per kind.
- Its exact form fits as a lowest answer per slot.
- Keeping a target record beside the non-target one would be two records for one verb that differ only in subject.
- The 23c record keeps its name until step 10.

**The shape.**
- `RemoveUpToCountersFromAllPermanentsEffect(kind, DynamicAmount amount, exactly, permanents, targetFilter, players)`. The int constructors stay. New: `exactly(kind, amount, subject)`, `.matchingTarget(filter)` and `.orFromPlayers(players)`.
- `target()` and `targetPlayer()` declare a benign target. The non-target forms are unchanged.
- The amount is evaluated on resolution (`XValue`).
- `exactly`: each slot's lowest answer is what the later slots can't make up (`MoveCountersAmountChoice.requiredTotal`), and a slot with one possible answer is answered unasked (CR 608.2d).
- Player slots: `CounterMoveSlot.fromPlayer(playerId, PlayerCounterType)`, removed through `GameData.removePlayerCounters`. REMOVE_COUNTERS records those players too.
- The removal is not a move, so CR 122.5 is not checked.
- Why `orFromPlayers` despite the one-card guard rail: only Price of Betrayal sets it, but it is a player subject for the verb, not a card-specific option. RemoveAll already takes players (23b).

| Member | Result |
|---|---|
| `RemoveChosenCountersFromTargetPermanentEffect` (5 cards) | absorbed: `(ANY, N, target())`, `exactly(ANY, X, target())` |
| `RemoveUpToCountersFromTargetEffect` | absorbed: Heartless Act `.matchingTarget(creature)`, Price of Betrayal `.orFromPlayers(targetPlayer())` |
| `RemoveAnyNumberOfCountersFromTargetPermanentEffect` | absorbed: Rhys `anyNumber(ANY, target()).matchingTarget(creatureYouControl)` |
| `RemoveCountersToCreateTetraviteTokensEffect` | split: `anyNumber(+1/+1, source())` + `ConditionalEffect(removedCounters(), CreateTokenAndLinkToSourceEffect(token.withAmount(AmountThisWay(REMOVE_COUNTERS)), false))` |
| `RemoveCountersFromTargetAndBoostSelfEffect` | split: `(ANY, XValue, target())` + `BoostSelfEffect(AmountThisWay(REMOVE_COUNTERS), Fixed(0))` |

**Deleted.**
- The five records and their handlers.
- `ChoiceContext.RemoveChosenCountersChoice`, `RemoveCountersOfKindChoice` and `TetravusCounterRemoval`, with their `PlayerInputService` begins, `ChoiceHandlerService` handlers and helpers, and the AI's Tetravus branch.
- `counterTypesOn` → `PermanentCounterSupport.counterKindsOn`, used by four handlers.

**Behaviour changes.**
- Exact X removes all counters when fewer are there and asks nothing then; one-answer questions are not asked (Cemetery Desecrator and Thornmantle Striker rulings, CR 608.2d).
- Ferropede must remove a counter once it says yes (Oracle text).
- All 10 cards now get the oil, time, last-counter and Siege defense checks and a log line per permanent; Render Inert has a Siege test.
- Out-of-range answers are refused instead of clamped.
- Heartless Act's spec no longer admits players.
- AI: target forms are HARMFUL (-1/-1 removal BENEFICIAL). Rhys and Hex Parasite were unclassified before.
- The survivor's prompts and log lines win.

**Tests changed.** RenderInert, Glissa, Ferropede, CemeteryDesecrator, ThornmantleStriker, Rhys and HexParasite: their prompt interactions; a Render Inert Siege test was added. Price of Betrayal, Heartless Act and Tetravus pass unchanged.

**Seen, not changed.**
- The AI's removal slot removes everything, so Rhys's AI strips its own +1/+1 counters. It needs an answer that keeps a counter kind the AI wants. Sure.

**For the next run.** Run 23 is closed. Candidates for step 10:
- rename the survivor (e.g. `RemoveCountersEffect`);
- merge it with `RemoveAllCountersFromTargetPermanentEffect` and `RemoveCounterFromTargetPermanentEffect`.

### Step 9, fold run 35 (2026-11-24, parallel worktree)
Six of seven batches done. Ratchet 3,427 / 2,745 → 3,411 / 2,739. Ledger 31 rows: absorbed 23 (4 interfaces), survivor 8. 26 card files rewritten; Oracle text checked for 24.

| Batch | Result |
|---|---|
| player-kw | 1 folded. `GrantControllerKeywordUntilEndOfTurnEffect` gains a non-target `PlayerSubject players` and FLYING (Sarah's Wings' player float); one log line per player. |
| skip-step | 4 folded. `SkipDrawStepEffect(SkipStepOrPhaseKind, PlayerSubject)` (UPKEEP_STEP added); hellbent is a `ConditionalEffect(ControllerHandEmpty)`; Fatespinner floats it by `GrantStaticEffectToPlayerEffect` on `triggeringPlayer()`. A draw skip is not a step skip (CR 614.10): the new `SkipDrawsEffect(PlayerSubject)` takes Possessed Portal and Living Conundrum (in a `ConditionalEffect`), and its two one-implementer interfaces are gone. |
| hand-size | 6 folded plus ReduceControllerMaxHandSizeEffect (its 5m block was this batch). `SetControllerMaximumHandSizeEffect(players, Change SET/INCREASE/REDUCE, DynamicAmount)`, applied in timestamp order (CR 613.11). Players are judged by `namesPlayer`, so Cursed Rack is `chosenPlayer()` and the inference is gone. Winter is delirium + `Sum(Fixed(7), Scaled(CardTypesAmongCardsInGraveyard, -1))`. Resolved (Inspired Idea), it floats PERMANENT with the amount fixed. The Controller/Opponent interfaces are gone. |
| hand-reveal | 3 folded, plus the `PubliclyRevealedHandEffect` interface. `PlayWithHandsRevealedEffect(PlayerSubject)`; Truth or Dare and Stromgald Spy float it through `GrantStaticEffectToPlayerEffect`, which now accepts PERMANENT and WHILE_SOURCE_REMAINS (source kept on the float; nothing begins if it left, CR 611.2b). |
| player-prot | 2 folded into the new `GrantProtectionFromPlayerEffect(from, permanents, duration)`: it floats `ProtectionFromPlayersEffect` on the controller and grants it to the permanents found as it resolves. Runed Halo and Absolute Virtue are left: they need the single player-protection reader (6 TargetLegalityService sites, damage preventions, aura attach). Seht's Tiger and Faith's Shield are unchanged. |
| hexproof-ignore | 2 folded. `IgnoreOpponentHexproofEffect(opponents, Breadth CREATURES/PERMANENTS)`; Detection Tower floats it until end of turn; the three reads go through one `hasHexproofIgnore` (active statics plus floated copies). |
| enters-tapped | not started |

**Behaviour changes.**
- Face-down or ability-less sources no longer skip draw steps or draws, or reveal hands (CR 708.2a, 613.1f). New tests: Necropotence, Possessed Portal, Living Conundrum, Wandering Eye.
- Hands are revealed per player: in multiplayer one opponent's reveal no longer shows every opponent's hand.
- Cursed Rack with no chosen player affects nobody.
- Logs: Everybody Lives! logs per player; Spy and Truth or Dare lose their reveal lines; Archon and Frolicker log the survivor's lines.

**Tests changed.** DrawServiceTest (stub), TurnCleanupServiceTest (fixtures; the Winter case moved to its card test), CursedRackTest (setup sets the chosen player, as BlackViseTest does), plus class renames in Fatespinner, RunedTerror, Fynn and PsychicSpiral.

**Seen, not changed.**
- Nowhere to Run says "spells and abilities" (anyone's), but is modelled as its controller's only. Multiplayer only. Sure of the text.
- `playerHasProtectionFromOpponents` ignores objects the player owns that no other player controls (CR 702.16k). Sure of the code.
- `getRevealedOpponentHand` returns only the first opponent's hand. Sure of the code.
- An increase or reduction newer than "no maximum hand size" overrides it. Not checked against the rulings.

**For the next run.**
- 35b: `enters-tapped`. Give `EnterPermanentsOfTypesTappedEffect` a `PlayerSubject` in place of `opponentsOnly` (Radiant Restraints: `enchantedPlayer()`).
- Due Respect and Nahiri's Lithoforming become floated copies on players.
- Ashling's Prerogative needs a parity predicate judged against its source.
- PlayersSkipUntapStepEffect (3 cards, one of them a plane) and RevealOpponentHandsEffect (Telepathy) can join this run's survivors.

### Step 9, fold run 33b (2026-11-24, parallel worktree)
Batches `produced-mana` and `persistent-mana` are done. Ratchet 3,414 / 2,737 → 3,410 / 2,733. Ledger 6 rows (absorbed 4, survivor 2). 7 card files rewritten, Oracle text checked for all 7. CR 106.4, 106.12, 106.12a, 106.12b, 605.1b and 605.4a checked.

| Batch | Result |
|---|---|
| produced-mana | 2 of 2 folded onto `AddManaOfTypeProducedByTappedPermanentEffect` (`requiredColor`, `ofSourceChosenColor()`, `.toItsController()`). The fallback question is settled: every land-tap event reports the pool's newly produced types, except Piracy's basic-type path, which fires while its spell-only colour choice is open. The survivor already waits (`readsProducedMana`), so both fallbacks are gone, and `handleSpellOnlyManaColorChosen` now resolves the waiting triggers. A tap that produced none of the type adds nothing (CR 106.12a; Forsaken Monument ruling: "only if it produces one or more {C} as it resolves"). |
| persistent-mana | 2 of 2 folded onto `AwardPersistentManaEffect` (the most-used record), not onto `AwardManaOfColorsEffect`. That record would have needed one colour for all of it, spell-only and a target recipient for these cards alone, and it would have pulled in its creature-mana and riot tags. The survivor takes `colors` (one: no choice), `eachChosenSeparately` and a `NONE` / `SPELL_ONLY` restriction; its 19 fixed-colour cards keep their constructors. Radha's attacker count stays the X recorded when the ability triggers. |

**Behaviour changes.**
- Ultima with Crystal Vein: sacrificing it for {C}{C} gets the extra {C} (the old guard required the land on the battlefield). Test.
- Caged Sun with Piracy on an opponent's Taiga: the extra mana waits for the chosen colour (it used to be added from the land's basic land types, whatever was picked). Test ×2.
- Through Piracy, the triggers waiting on a replacement-order prompt are also resolved now, by the same `ChoiceHandlerService` path (no test of their own).
- The survivors' log lines win.

**Tests changed.** `TapTriggersTest`: the Ultima case asserted nothing for a land that had left, which contradicts CR 106.12a; handlers re-registered. `AddManaPerAttackingCreatureEffectHandlerTest` deleted with its handler; `GrandWarlordRadhaTest` covers it.

**Seen, not changed.**
- Piracy's nonbasic path replaces the land's own `ManaColorChoice` with an any-colour `SpellOnlyManaColorChoice`. The parked activation's land-tap event never fires, so Caged Sun and Mana Flare miss a City of Brass tapped through Piracy. Fairly sure, from reading the code.
- Caged Sun's "a land's ability causes you to add" also covers land mana abilities without {T}. `youTapLandForMana` fires only for {T} abilities. Sure; low impact.
- `TappedLandManaSupport.producedManaTypes` keeps its basic-land-type fallback for Mana Flare's `AddOneOfEachManaTypeProducedByLandEffect` and `AddManaForEachOtherLandWithSameNameEffect`. That fallback fires only for an empty report with no choice open, where CR 106.12a says nothing triggers. Unsure whether such a tap exists.
- The tap family's "its controller adds" uses `TappedLandManaSupport.tappedPermanentController`, not a `PlayerSubject`. `controllerOf(triggering())` would lose a departed land, because the tap request records no controller.

**For the next run.** Step 10 can move the tap family's recipients (`AddManaWhenTappedForManaEffect`, `AddManaOnEnchantedLandTapEffect`, this survivor) onto a `PlayerSubject` once `TapTriggers.tapRequest` records the tapped permanent's controller.

### Step 9, fold run 36 (2026-11-24, parallel worktree)
Five of seven batches done (`oneshot-combat`, `companion`, `attack-ban`, `attack-req`, `attack-together`); `defender-cond` and
`attack-target` left. Ratchet 3,414 / 2,737 → 3,397 / 2,727 (17 records, 10 handlers; the `MatchingAttackerRestrictionEffect` and
`OpponentsMustAttackRequirementEffect` interfaces went too). Ledger 32 rows: absorbed 17, survivor 6, dead 2, `step-9` 7 (re-noted).
22 card files rewritten, Oracle text checked for all 22; rulings for Intimidation Bolt, Immobilizer Eldrazi, Twins of Discord,
Okk, Champions of Minas Tirith, Call for Aid, Seeker of Slaanesh, Viashino Bey, War's Toll. CR 506.4, 506.5, 508.1c, 508.1d,
509.1b, 509.1c, 611.2c checked.

| Batch | Result |
|---|---|
| oneshot-combat | 7 folded. `CantBlockThisTurnEffect` gains a `Duration` (`UNTIL_END_OF_COMBAT`: Forgestoker `target()`, Flummoxed Cyclops `source()`); Twins of Discord and Immobilizer Eldrazi are `narrowed(allCreatures(), …)` rules; `CantAttackThisTurnEffect` takes Festival / Orim's Chant (`allCreatures()`), Intimidation Bolt (`except(allCreatures(), targetDeclaredElsewhere())`) and Wall of Dust (`triggering()`, `DURING_AFFECTED_PLAYERS_NEXT_TURN`). Incite Hysteria grants an ability ("gain 'This creature can't block'"), so it went onto `GrantEffectToTargetUntilEndOfTurnEffect(StaticAbility.of(CantBlockEffect), withCreaturesSharingAColor(target()))`, not the triage's survivor. |
| companion | 3 folded onto `CantAttackOrBlockAloneEffect(…, otherCount, companion, description)` (`unlessOthersAlsoDo(2)`, `unlessGreaterPowerAlsoDoes()`, `cantAttackUnlessAlsoAttacks`), read by one engine component, `CombatCompanionRestrictions`, in the attack and block declarations and the AI. |
| defender-cond | not taken: the three need a `Condition` judged for the defending (blocking) player in `CombatHelper`'s three scans and `BlockLegalityService`; fold run 34b builds player-judged conditions and is not on this tree. |
| attack-target | not taken (time): two attacker-side readers (granted vs printed statics) and the Aura path with Vow floating copies must become one restricted-`PlayerSubject` reader. |
| attack-ban | 3 folded onto `CreaturesCantAttackControllerUntilNextTurnEffect(PlayerSubject attackers, PlayerSubject protectedPlayers, protectsPlaneswalkers, Duration)`. Champions' payment stays in `MayPayManaEffect.dynamic` (step 10 needs nothing for it). |
| attack-req | 1 folded: `OpponentsMustAttackControllerEffect.attackingAnyone()` (Seeker of Slaanesh), fixing the Fold: defect. |
| attack-together | 3 folded plus Ekundu Cyclops (its `unique` row was the queue's "check"): `CreaturesWithCounterAttackTogetherEffect(attackers, required)` judged with the source as attackers are declared; one validator and the alongside reader. |

**Behaviour changes.**
- Seeker of Slaanesh: an attack on a battle (or another opponent) obeys it (Oracle: "attack with at least one creature"). Test.
- A lone Okk, Orcish Conscripts or Scarred Puma is no longer offered as an attacker (or Okk, Conscripts as a blocker): each restriction is disobeyed alone (CR 508.1c, 509.1b); the declaration names the restriction as before.
- Okk, Conscripts, Puma, War's Toll, Magnetic Web, Viashino Bey, Ekundu, Seeker and Trove are read through active static abilities (face down or lost abilities: no restriction or requirement; War's Toll, Magnetic Web and Trove/Seeker read printed statics before). No new test.
- Viashino Bey is in `getMustAttackAlongsideIndices` (AI, prompt); the attack-together requirements waive a creature with its own attack tax (CR 508.1d), Viashino's guard before. No new test.
- `lastingRule` binds chosen values (`bindChosenValues`, now with mana-value parity): a resolved rule with a chosen type or chosen players keeps them.
- Logs and denials (the survivors'): Twins/Immobilizer "Matching creatures can't block this turn." and "can't block this turn"; Forgestoker/Cyclops "can't block this combat"; Wall of Dust "can't attack during its controller's next turn"; Festival/Orim's/Intimidation Bolt per-player lines; Incite Hysteria the grant line.

**Tests changed.** `ImmobilizerEldraziTest` (4 asserts: the survivor's denial "can't block this turn", not "toughness greater than power"); `FlummoxedCyclopsTest`, `ForgestokerDragonTest`, `WallOfDustTest`, `AttackLegalityServiceTest` (named deleted records; the two service fixtures now resolve the survivor through its handler). Added: the Seeker battle test.

**Tests run.** 133 classes (every test class naming a rewritten card, `git grep -lwF`; the 14 "alone" cards; samples of the survivors' cards; combat, block, attack-legality, subject and AI decision-engine tests; `EffectCountRatchetTest`): 193 XML files, 1,899 cases; two message assertions failed and were fixed (Immobilizer's denial, Viashino's "must attack"), then the 10 touched classes again: 169 cases, 0 failures.

**Seen, not changed.** `CantAttackThisTurnEffectHandler`'s set branch floats `entry.getCard().getName()` (no face-down redaction) and doesn't bind its filter's chosen values (only the except branch goes through `lastingRule`). Sure of the code, no card hits it today.

**For the next run.** `defender-cond` after fold run 34b lands (defending-player conditions), then `attack-target` as its own run.

### Step 9, fold run 29c (2026-11-24, parallel worktree)
`exile-until-rel` done (3 of 3 left by 29b), `land-or-cast` not started. Ratchet 3,396 / 2,724 → 3,393 / 2,721. Ledger 10 rows: split 3, survivor 4, blocked-other 1 (updated), step-9 2 (left). 4 card files rewritten (Possibility Storm, Tibalt's Trickery, Reweave, Shape Anew), Oracle text checked for all 4. Rulings read for Possibility Storm, Tibalt's Trickery, Reweave, Shape Anew, Omen Machine and Wild Evocation. CR 118.9, 201.2a, 201.2c, 305.1, 400.7, 601.2f, 607.2a, 608.2g and 608.2h checked.

| Batch | Result |
|---|---|
| exile-until-rel | 3 folded (all split). Possibility Storm: `SequenceEffect.of(ExileTargetSpellEffect.triggeringSpellWithSource(), new ExileCardsFromTopUntilEffect(controllerOf(triggeringSpell()), CardSharesCardTypeWithTriggeringSpellPredicate).withSource(), MayCastDiscardedCardWithoutPayingManaCostEffect(cardsThisWay(EXILE), same filter).castBy(caster), PutCardsIntoLibraryEffect.onBottomInRandomOrder(exiledWithSource()))`. Tibalt's Trickery: `CounterSpellEffect` + `ChooseOneAtRandomEffect` of `MillEffect(1/2/3, controllerOf(targetSpell()))` + the exile-until, cast and bottom steps with `allOf(NONLAND, not(CardSharesNameWithPermanentsPredicate(targetSpell())))`. Reweave and Shape Anew (one record): `SacrificeSelfEffect(target(), controllerOf(target()), filter)` + `RevealUntilCardPredicateRestOnBottomRandomEffect.ontoBattlefield(filter, Players(controllerOf(target()))).shufflingRest()`, with Reweave's reveal behind `HappenedThisWay.sacrificed()` over `CardSharesCardTypeWithPermanentsPredicate(departedThisWay(SACRIFICE))`. |
| land-or-cast | not started (see For the next run) |

**Step model.** `MayCastDiscardedCardWithoutPayingManaCostEffect` is an earlier step: it ends its step by queuing the offer, and the resolution waits for the pending offer and any target prompt before it resumes. So "then put the exiled cards on the bottom" is a plain later step, run after the cast is complete. Possibility Storm and Tibalt's Trickery no longer use `MayCastExiledCardThenBottomRestEffect` or `deferredExileBottomSourceIds`; both stay for Unpredictable Cyclone's draw-replacement payload.

**Rules checked.** "A different name than that spell" with a face-down spell: every named card has a different name (CR 201.2c). "Shares a card type with it" reads the triggering spell as effects saw it, face-down aware, and as it last existed on the stack once it has left (CR 608.2h). "Choose 1, 2, or 3 at random" is not a die roll; `ChooseOneAtRandomEffect` over three mill steps expresses it without a new parameter.

**Behaviour changes.**
- Possibility Storm doesn't offer a shared-type land card (it can't be cast); it is bottomed with the rest (new test).
- Shape Anew reveals even when the artifact can't be sacrificed (ruling; new test with Hithlain Rope).
- Shape Anew and Reweave respect "can't cause you to sacrifice" (Tajuru Preserver, Sigarda); the old handler ignored it. Shape Anew still reveals, Reweave doesn't (new tests).
- Shape Anew and Reweave put the found card through the entry batch; a card that can't enter from a library stays there.
- The spell Possibility Storm exiles goes to its owner's exile as its physical card (was the caster's). Untested.
- Tibalt's Trickery's exiled cards are no longer linked to the spell's card. Log lines are the shared steps' lines.

**Seen, not changed.**
- `ExileFreeCastSupport.castSelectedFace` (every exile free cast: Possibility Storm, Tibalt's Trickery, the cast-now step from exile) charges no cost increase (CR 601.2f). It belongs with the existing defect line for the library and exile free-cast flows. Sure.
- Reality Scramble stays blocked: `PutTargetOnBottomOfLibraryEffect` records no `DepartureResult`, so `departedThisWay` has nothing to read. The filter now exists. Sure.

**For the next run.**
- `land-or-cast` (Omen Machine, Wild Evocation): build one mandatory step over a `CardSubject`.
- A land card goes onto the battlefield through the entry batch. This is a special action, not a cast (CR 305.1).
- Otherwise the card is cast without paying its mana cost if able (CR 118.9). It pays cost increases and mandatory additional costs (CR 601.2f): base it on `MayCastHandlerService`'s free-cast payment, not on `ExileFreeCastSupport`.
- Omen Machine: `ExileTopCardOfOwnLibraryEffect` over the active player + the step.
- Wild Evocation: `RevealCardsFromHandEffect.atRandom` + the step over the hand card.
- The "Fold:" defect on Omen Machine is fixed with it.
- Eye of the Storm can use `ExileTargetSpellEffect.triggeringSpellWithSource()` once `CopyImprintedCardAndMayCastCopyEffect` has a caster.

### Step 9, fold run 27b (2026-11-24, parallel worktree)
2 of 3 batches done: 7 folded (4 absorbed, 3 split), 1 unique, 2 blocked-other. Ratchet 3,396 / 2,724 → 3,390 / 2,718. 8 card files rewritten; Oracle text checked for all 8. Rulings read for Desperate Gambit, Reverberation, Soltari, Psychopath, Impulsive Maneuvers, Reflect Damage and Opal-Eye. CR 400.7, 510.2, 608.2b, 608.2d, 608.2h, 608.2n, 609.7, 609.7a, 609.7b, 611.2c, 614.5, 614.9, 615.7, 615.8, 615.12, 616.1 checked.

| Batch | Result |
|---|---|
| `redir-source` | 6 of 8. Survivor `RedirectNextDamageFromChosenSourceToSelfEffect(source, recipients, toPermanent, toPlayer, wholeTurn, combatOnly)` with `.to`, `.toPlayer`, `.toItsController`, `.dealtTo`, `.allThisTurn()`, `.combatDamageOnly()`. Sources: `thisWay(CHOOSE)` (Opal-Eye, Reflect Damage), `source()` (Psychopath, Soltari), `target()` (Mirror Strike), `targetSpell()` (Reverberation). Six shields became one `SourceKeyedRedirectShield` (a spell keeps its graveyard stay). One `SourceKeyedRedirect` rule per recipient kind, one option per shield, redirected via `pendingSourceRedirectDamage` + `processRedirects` (CR 614.9, 614.5). Harsh Judgment and Aegis of Honor are `blocked-other`: they are keyed by a source property, not an object, and each would set its own option. |
| `next-dmg-shields` | 2 of 3, plus the survivor. `PreventNextDamageByTargetCreatureEffect(source, gainLife, doubling, combatOnly)` with `.preventing`, `.doubling`, `.combatDamageOnly()`. Desperate Gambit is `ChooseDamageSourceEffect.youControl()` + `FlipCoinWinEffect`. Impulsive Maneuvers is `FlipCoinWinEffect` over `triggering()`. Barbed Wire is `unique`: a fixed amount only it sets. combatOnly is set only by Impulsive on this record, but it is the shield's existing component (Ria Ivor) and the queue row named it. |
| `redir-absorb` | Not started. This tree's `GrantStaticEffectToPlayerEffect` floats with no source (fold run 35 is not here). |

**New shared step.** `ChooseDamageSourceEffect.any()` / `.youControl()` takes its CR 609.7a candidates from `DamageSourceChoiceSupport`: permanents; spells on the stack, the resolving one included; ability sources, even after they left; cards that stack objects refer to; sources of waiting shields. A lone candidate is chosen unasked. It records CHOOSE (id and controller), and later steps read it with `SubjectResolutionService.damageSourceIds`. `PreventDamageFromChosenSourceEffect` now uses the same collector.

**One-shot redirect, one event.** A used shield keeps applying to the rest of the event: `CombatDamageState.usedSourceKeyedRedirects` for a combat damage step (CR 510.2), `GameData.sourceRedirectShieldsUsedInOpenEvent` for simultaneous noncombat damage. This replaces Psychopath's consumed map and Reflect Damage's combat reinstating.

**Behaviour changes.**
- A lone source is chosen unasked; the resolving spell is a legal source; Desperate Gambit flips even with no other source (tests).
- Reflect Damage redirects in the same event, not a fresh event. Untested.
- Opal-Eye redirects all of a source's simultaneous combat damage (test).
- Destinations follow CR 614.9 (creature, planeswalker or battle); all redirects respect "can't be redirected" on a permanent. Untested.
- Options and log lines are the survivor's.

**Tests changed.** DesperateGambitTest (the spell is a candidate; lone-source test), OpalEyeKondasYojimboTest (+1), shield class in ReflectDamageTest and SoltariGuerrillasTest, lenient stubs in CombatDamageServiceTest and AbstractDamageHandlerTest.

**Seen, not changed.**
- `TargetPolarityClassifier` marks `PreventNextDamageByTargetCreatureEffect` BENEFICIAL, but Awe Strike is used on an opponent's attacker. Fairly sure.
- EyeForAnEye, Harm's Way, Jade Monolith, Shaman en-Kor, General's Regalia and Nova Pentacle still collect damage sources their own way (`PreventionSupport.collectAllDamageSourceIds` / battlefield-only lists). Sure.

**For the next run.**
- Move the other chosen-source records onto `ChooseDamageSourceEffect` + `thisWay(CHOOSE)`: `PreventDamageFromChosenSourceEffect`'s scopes, EyeForAnEye, `PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect`, `RedirectTargetCreature*FromChosenSource*`, `RedirectNextDamageFromChosenSourceToTargetCreatureEffect`. Their shields could join `SourceKeyedRedirectShield` (with an amount) or `SourceNextDamageToAnyTargetShield`.
- Then do `redir-absorb` once fold run 35 is in.

### Step 9, fold run 32b (2026-11-24, parallel worktree)

1 folded, 1 survivor, 1 blocked-other, 1 unique. Ratchet 3,384 / 2,712 → 3,383 / 2,711. 1 card file rewritten (Opalescence). Oracle text and rulings checked for March of the Machines, Titania's Song, Opalescence, Starfield of Nyx and Spark Rupture. CR 205.1a, 205.1b, 613.1d, 613.1f, 613.4b, 613.6, 613.7, 613.7a and 613.8a checked.

| Batch | Result |
|---|---|
| mv-animation | 1 folded: Opalescence is `AnimateNoncreatureArtifactsEffect.otherNonAuraEnchantments()` (`otherPermanents()`, non-Aura enchantment filter). Starfield left `blocked-other`: its threshold is a one-card option, and the `ConditionalEffect` route needs `ControlsPermanentCount` board-stable (default-deny; its filter can read combat state) plus the wrapper admitted to 7b. Spark Rupture left `unique`: replacing the type (CR 205.1a, ruling) and loyalty-based P/T are two one-card options. |

**Shape.**
- The survivor's filter is judged in layer 4 against the states as of the earlier layer-4 effects. "Noncreature" uses `PermanentIsCreaturePredicate`, which counts awakened lands, so March's old awakening skip is now part of its filter. Opalescence still animates an awakened enchantment land.
- `LayeredBoardState.staticAnimations` (identity-keyed by `instance.original()`) replaces `marchAnimatedIds`, `starfieldAnimatedIds` and `opalescenceAnimatedIds`. It is written by the layer-4 cases of the survivor, Starfield and Spark Rupture, and read by their layer-6 gates, their 7b entries and their handlers.
- Trials deep-copy the table, and the footprint tag is `STATIC_ANIMATIONS`. No layer-6 reader uses `l4Contributions` any more; that table is empty in trials.

**Behaviour changes.**
- Layers 6 and 7b apply to the set each instance animated, not to the union (CR 613.6). A second March gives no base P/T to an artifact the first already animated, so Lignify between them wins (0/4). New test.
- Spark Rupture reads the layered planeswalker type. A Planeswalkerificate'd Grizzly Bears with 4 loyalty counters becomes a 4/4 with no abilities; before it was 2/2 and kept its abilities. New test; this is the "Fold:" defect.
- Titania's Song and Spark Rupture's layer-6 dependency trials now see their animated set. Not tested.
- Creature fallbacks (`isCreatureWithBonus`, `StaticEffectSupport.isEffectivelyCreature`): a battlefield permanent answers from its layered state. A permanent with no state is judged by each printed survivor's filter on its own characteristics (`isAnimatedByPrintedStaticAnimation`), so Opalescence now counts there too. `wouldEnterAsCreature` uses the same check. The two same-name boost handlers pass `gameData`, so they also count awakened lands and conditional self-animations. Not tested.
- Opalescence's handler no longer adds a granted creature type in assembly; the layered state already has it.

**Tests.** 2 added (March, Spark Rupture). `DependencyFootprintTest` only renamed its table constant.

**Seen, not changed.**
- `isAnimatedByStarfield` still answers for battlefield permanents, ignoring a later type removal. `wouldEnterAsCreature` ignores Starfield, so Torpor Orb misses an enchantment entering under an active Starfield. Sure of the code.
- Titania's Song's "continues until end of turn" floating copy is keyed by `losesAllAbilities`, which only Titania sets. A second user of that flag would need its own marker. Sure.

**For the next run.** Starfield of Nyx: make `ControlsPermanentCount` with a pure type filter board-stable, admit `ConditionalEffect(…, AnimateNoncreatureArtifactsEffect)` to layer 7b, and delete `isAnimatedByStarfield`. Then floated-name with The Irencrag.

### Step 9, fold run 35b (2026-11-24, parallel worktree)
Five members done, plus Ashling's haste grant. Ratchet 3,355 / 2,699 → 3,349 / 2,696. Ledger 9 rows: absorbed 6, survivor 3. 10 card files rewritten; Oracle text checked for 9.

| Batch | Result |
|---|---|
| enters-tapped | 3 folded plus `GrantKeywordToCreaturesOfChosenParityEffect`. `EnterPermanentsOfTypesTappedEffect`'s `opponentsOnly` is now a non-target `PlayerSubject players`; the boolean overloads stay and map to eachOpponent / eachPlayer. Radiant Restraints is `enchantedPlayer()`. Ashling's Prerogative uses the new `PermanentHasSourceChosenParityPredicate(chosenQuality)`; a static enters-tapped filter now gets its source in the `FilterContext`. Due Respect and Nahiri's Lithoforming float the survivor through `GrantStaticEffectToPlayerEffect` (CR 611.2c: a rule change covers later entries). The batch snapshot takes only `everyPlayersByTypeAlone()` effects. |
| joiners | `PlayersSkipUntapStepEffect` is `SkipDrawStepEffect(UNTAP_STEP, eachPlayer())`; the untap reader judges the active player over active statics and face-up planes (CR 614.10). `RevealOpponentHandsEffect` is `PlayWithHandsRevealedEffect(eachOpponent())`. |
| player-prot leftovers | not started: Runed Halo, Absolute Virtue. |

**Behaviour changes.**
- A Radiant Restraints that lost its abilities taps nothing (CR 613.1f); a face-down Telepathy, Stasis, Sands of Time or The Eon Fog does nothing (CR 708.2a). Tests: RadiantGrace, Telepathy, Stasis.
- Telepathy's opponents' hands are revealed to all players (CR 701.20a). Observable only with 3+ players, and only for some seat orders because of the first-opponent view defect.
- Logs: Due Respect and Nahiri's Lithoforming lose their enters-tapped lines.

**Tests changed.** RunedTerrorTest (the float is per player, read through `floatingPlayerEffects`). New: RadiantGrace, Telepathy, Stasis, Nahiri (an opponent's land enters untapped), PredicateEvaluationServiceTest (2).

**Seen, not changed.**
- Radiant Restraints' "ignored Aura" half of the P03 defect (Volrath's Curse) was not checked. Its lost-abilities half is fixed.
- `snapshotEnterTappedTypes` matches the printed card types (`matchesAnyType(card)`), not the permanent as it would exist on the battlefield (CR 614.12). Sure of the code, untested.

**For the next run.**
- Runed Halo and Absolute Virtue onto one player-protection reader (6 TargetLegalityService sites, damage preventions, Aura attach), with the `playerHasProtectionFromOpponents` owned-but-uncontrolled defect (CR 702.16k).

### Step 9, fold run 29d (2026-11-24, parallel worktree)
`land-or-cast` done: 2 split, 1 survivor (new). Ratchet 3,376 / 2,710 → 3,375 / 2,709. 2 card files rewritten; Oracle text and rulings checked for both. CR 118.9, 305.1, 305.9, 601.2c, 601.2f, 601.3, 608.2g, 702.11b, 702.85a, 707.12, 716.3 checked.

| Batch | Result |
|---|---|
| land-or-cast | 2 folded (split). Omen Machine: `SequenceEffect.of(ExileTopCardOfOwnLibraryEffect.of(triggeringPlayer()), PutCardsOntoBattlefieldEffect(cardsThisWay(EXILE, land)), CastCardWithoutPayingManaCostIfAbleEffect(cardsThisWay(EXILE)))`. Wild Evocation: the same over `RevealCardsFromHandEffect.atRandom(triggeringPlayer(), 1)` and `revealedThisWay(...)`. The land step puts the card in through the entry batch under its owner's control, and doesn't play it as a land (CR 305.1). |

**New step.** `CastCardWithoutPayingManaCostIfAbleEffect(CardSubject)`: the mandatory cast-now step. The player whose zone holds the card casts it, with no "may" (CR 608.2g; both rulings say casting is not optional). An exiled card goes through `ExileFreeCastSupport` (Adventure and modal faces, split halves, targets, additional costs). A hand card is checked by `canCastRevealedFromHandWithoutPaying`, then cast through `HandCastSpellTarget` or `beginSacrificeCostIfNeeded`. A land, or a card that can't be cast, stays where it is. The step ends on any prompt it begins, so it is the last step. I chose a new record over a mandatory option on `CastCardsWithoutPayingManaCostEffect`, because that one is a "may" choice over exile and graveyard and has no hand path.

**Free-cast cost increases (defect).** There is now one payment, `SpellCastingService.payFreeCastCostIncreases` / `canPayFreeCastCostIncreases`. It pays {0} plus increases minus reductions from the pool, with the spell's mana restrictions (CR 601.2f). It is used by `ExileFreeCastSupport.castSelectedFace`, `ExileFreeCastQueueSupport` (cascade, Improvisation Capstone and the other queued casts, copies included, CR 707.12), `LibraryChoiceHandlerService.canCastWithoutPaying` / `castCardWithoutPaying`, and the new step. A cost increase that can't be paid makes the cast impossible: the card stays where it is or follows the flow's uncast path (a cascade hit is bottomed).

**Behaviour changes.**
- Omen Machine and Wild Evocation pay cost increases instead of refusing (Omen) or ignoring them (Wild). Tested with Thalia: paid from floating mana; unpaid, the card stays in hand.
- Omen Machine and Wild Evocation pay a non-targeted spell's mandatory sacrifice (Village Rites). This fixes the "Fold:" defect.
- Omen Machine offers an Adventure (CR 716.3). Tested with Lovestruck Beast.
- A cascade hit pays the increase or is bottomed (two Bloodbraid Elf tests).
- `ExileFreeCastSupport` judges each target candidate as a target of the spell. Before, a single-target card used the unchecked flat candidates and was offered a hexproof creature (CR 702.11b; the existing Omen Troll Ascetic test).
- `ExileFreeCastSupport.completeFaceChoice` ends through the input epilogue. Before, a face choice begun with `completeInput=false` (suspend, Omen) left its resolution parked.
- Log lines are the shared steps' lines.

**Tests changed.** I deleted the two handler unit tests and added 8 card tests. No assertion changed.

**Seen, not changed.**
- Free-cast cost increases are paid only from the pool. No mana abilities can be activated during the cast (CR 608.2g allows them through 601.2g). This matches every other free-cast path. Sure of the code.
- Kicker isn't offered on a mandatory exile cast (`completeInput=false`), though the ruling allows optional additional costs. Sure.
- If a kicker is paid and then the cost increase can't be paid, the kicker mana is lost. Low, sure of the code.
- `ExileCastTargetSupport.flatSingleTargetCandidates` doesn't check hexproof or protection for permanents. It is still used through the 3-argument `firstSlotCandidates` (Chandra, Vaan, Paradigm, `ExileReducedCastSupport`, `OutsideGameNormalCostCastSupport`, `CopyDrawnInstantOrSorcery…`). Sure of the code; whether their completions reject the target is unverified.
- Free casts that still pay no cost increase: `MayCastHandlerService.castRevealedCardWithoutPaying` and `SplitCardCastSupport` with `payManaCost=false` (except the sideboard flow). Sure. Other per-card free-cast handlers are unaudited.

**For the next run.**
- A defect run moving the remaining free-cast paths onto `payFreeCastCostIncreases`, and switching the 3-argument candidate users to legality-checked candidates.
- `ExileTopCardPutLandOntoBattlefieldOrMayCastFreeEffect` looks like the "may" twin of this batch (`PutCardsOntoBattlefieldEffect` + a may cast step). Not checked.

**Follow-up (same run, after review).** The first version paid free-cast cost increases from the pool only, so a cascade hit under Thalia was bottomed even with untapped lands (CR 601.2g, 608.2g). Now a free cast whose increase the pool doesn't cover, but the pool plus untapped sources would, pauses on the existing mid-resolution mana window (`PendingMayAbility` with a mana cost, `GameService.isMayCostManaPayment`): "Pay {N} to cast X (its cost increase)?". Accepted with enough mana, the cast continues; declined, the card follows its flow's uncast path (cascade: bottom; Omen: stays in exile; Wild Evocation: stays in hand). New `PayFreeCastCostIncreaseEffect` (engine-made, `unique`) + `FreeCastCostIncreaseSupport`, asked by `ExileFreeCastSupport`, `ExileFreeCastQueueSupport` (before taking the next card) and the hand cast. `LibraryChoiceHandlerService` is back to HEAD: its search casts (Knowledge Exploitation, Sunbird-style, cascade's non-queue branch) still cast free, because each caller needs its own uncast destination. Ratchet after the follow-up 3,376 / 2,710 (the new record and handler offset the two removed). 5 new tests (Bloodbraid Elf ×2, Omen Machine ×2, Wild Evocation). Seen: the Hard AI likely scores the payment "may" at 0 and declines; the queue asks for mana before modes and targets, so a spell left with no legal target leaves the mana in the pool.

### Step 9, fold run 38 (2026-11-24, parallel worktree)
Four of five batches done. Ratchet 3,355 / 2,699 → 3,337 / 2,686. Ledger 32 rows: absorbed 25 (8 interfaces), split 1, survivor 4, unique 2. 25 card files rewritten; Oracle text checked for 27. CR 604.3, 611.2b, 611.2c, 613.1b, 613.8b, 704.5j, 724.1 and 800.4a were checked.

| Batch | Result |
|---|---|
| perm-borrowers | 5 folded. `GainActivatedAbilitiesOfOwnCreaturesEffect(PermanentSubject, filter, Abilities ALL/NON_LOYALTY/LOYALTY)` never counts its source. The one read `effectiveActivatedAbilities` moved to the survivor's handler. A permanent whose bonus is being assembled further up the thread lends only its card's abilities, which ends the Marvin/Robaran recursion. The "those abilities" mana permission is `SpendManaAsAnyColorForActivatedAbilitiesEffect.forBorrowedAbilities()`, read by `GameQueryService.isBorrowedAbility`. |
| maintained-control | 3 folded. `GainControlOfTargetEffect.whileMatches(subject, predicate)`: nothing happens if the predicate fails as the effect begins (CR 611.2b), and control ends for good once it stops holding. Both are judged by `CreatureControlService.maintainedPredicateHolds`, with no source needed. Old Man of the Sea's noncreature exemption moved into its own `anyOf(not creature, power)` predicate. New `PermanentIsHauntedPredicate`. Kaya's emblems carry a haunted-creature ability target filter, which replaced the validator class. |
| leader-control | 5 folded. `.of(source(), PERMANENT).gainedBy(matching(eachPlayer(), …))` sits after each card's intervening "if" (CR 603.4 not cited; it was not checked), which leaves one player. New `PlayerHasLeastLifePredicate`, `PlayerHasMostCardsInHandPredicate`, `PlayerSeatedInDirectionPredicate`; `PlayerControlsMostCreaturesPredicate` gains a creature filter. Loxodon's tie is `ChoosePlayerEffect`, unasked with one candidate. All are judged on resolution, as the cards say. |
| legend-rule | 7 folded. `IgnoreLegendRuleEffect(PermanentSubject)` and `IgnoreLegendRuleWhenExactlyTwoSameNameEffect(PermanentSubject counted)`. LegendRuleService exempts each permanent an active effect's static set names, then asks only when two or more same-named legends are left. |
| player-control | Left, 2 `unique`. Secret of Bloodbending would need a next-combat Duration, and Cruel Entertainment a controlling subject plus positional targets inside one two-target group. Each option has one user (guard rail). Two target groups would also change Cruel Entertainment's cast-time prompt. |

**Behaviour changes.**
- Borrowers lending to each other no longer recurse (Robaran test).
- The Master, Multiplied exempts only creature tokens (Oracle; the Fold: defect).
- Mothers Yamazaki counts every permanent named it that you control, not only legendary ones (Oracle; test).
- Nicol Bolas gains granted loyalty abilities and nothing from a face-down or ability-less planeswalker; his borrowed abilities are no longer stamped with Bolas as grant source.
- Kraj drops the abilities a land-type change removed.
- The token exemption reads active statics.
- Drana, Manascape and Fence are plain layer 6, no CDA flag (CR 604.3).
- Sokenzan finds its trigger's object (CR 400.7).
- The handoffs are not steals for the AI.
- Logs: Loxodon's prompt label; Kaya's damage emblem logs the survivor's damage line.

**Tests changed.** CreatureControlServiceTest, LayerClassifierTest and StepTriggerServiceTest fixtures; FuzzInvariants; LegendRuleServiceTest (stub plus 3 cases); Robaran (1 case).

**Seen, not changed.**
- Borrowers in a dependency loop (CR 613.8b) are approximated by assembly order. In a three-borrower cycle a lender's borrowed abilities can be missed. Sure of the code, rare.
- No test of their own: Kraj's printed-ability loss, Bolas's granted loyalty abilities, the token scan's active statics.

**For the next run.** Take Secret of Bloodbending and Cruel Entertainment together with ControlTargetPlayerNextTurnEffect's six cards, once a second user needs a Duration or a controller subject.

### Step 9, fold run 36b (2026-11-24, parallel worktree)
Both Run 36 batches left by fold run 36 are done. Ratchet 3,334 / 2,685 → 3,328 / 2,685: 6 records, plus 2 interfaces that are not counted. Ledger 10 rows: absorbed 6, survivor 2, dead 2. 13 card files rewritten; Oracle text checked for 16 cards; rulings checked for Hooded Horror, Graxiplon, Nightkin Ambusher, Xantcha and Fealty to the Realm. CR 506.2, 508.1c, 508.5, 509.1b and 613.1f checked.

| Batch | Result |
|---|---|
| defender-cond | 3 folded. `CantBeBlockedIfDefenderControlsMatchingPermanentEffect` gains a `Condition defendingPlayerCondition` (`whileDefendingPlayer(c)`; exactly one of predicate and condition is set; landwalk needs the predicate). The condition is judged as a static ability of the evasion's source, with the defending player as the context's target (CR 508.5; fold run 34b's `withTargetId`), as blockers are declared, so a made block stays (CR 509.1b; Hooded Horror and Nightkin rulings). One judge, `GameQueryService.defendingPlayerConditionHolds`, serves CombatHelper's scans and `BlockLegalityService`. `BlockabilityRestrictionEffect.unblockableWhileDefendingPlayer()` replaces the three per-record facets. New `Condition` `TargetPlayerMatches(PlayerPredicate)`: Hooded Horror reuses `PlayerControlsMostCreaturesPredicate`. New predicate `PlayerControlsCreaturesSharingCreatureTypePredicate(min)`: Graxiplon's "unless" is a `NotCondition`. Nightkin uses the existing `TargetPlayerHasRadCounters`. |
| attack-target | 3 records folded. `CantAttackCardOwnerEffect(PermanentSubject attacker, PlayerSubject players, boolean includePlaneswalkers)`: `attacker` is `source()` or `attachedCreature()`; the old constructors stay as `ownerOf(source())`; factories `cantAttack(players, pw)` and `enchantedCreatureCantAttack(players, pw)`. One reader, `AttackLegalityService.isRestrictedFromAttackingTarget`, covers the creature's active and granted static abilities (`source()`) and the active static abilities of the permanents attached to it (`attachedCreature()`, Aura-ignore honoured). Players are judged by `SubjectResolutionService.namesPlayer` for the ability's source; a planeswalker target is judged through its controller when `includePlaneswalkers`, and a battle is never restricted (CR 508.1c). `namesPlayer` gains `ownerOf(source())` (with the board, using the current owner) and `controllerOf(source())`. The 8 "can't attack you or planeswalkers you control" cards share one form; Fealty is `(you(), false)`. |

**Behaviour changes.**
- "Its owner" is the card's current owner (`cardOwnerOverrides`, the ante exchanges), no longer the owner stamped on the card. Test: Xantcha.
- Xantcha's, Alexios's and Capricopian's own restrictions are read through active static abilities: they go when the creature loses all non-mana abilities, loses its printed abilities, or has that one ability suppressed (CR 613.1f). Before, only Xantcha's and Alexios's path checked anything, and only for losing all abilities. No card reaches the difference; no test.
- Capricopian's restriction is also read when granted. No card grants it.
- CombatHelper now also judges the most-creatures condition for granted effects (the old granted-effects scan skipped it). The defending player for every condition is found the way the rad-counter check found it: battlefield identity first, then a permanent's controller.
- Dead code deleted: the "Vow" floating-copy branch in `canAttackDefender`. Nothing has floated `EnchantedCreatureCantAttackControllerEffect` since Promise of Loyalty moved to `CreaturesCantAttackControllerUnlessPredicateEffect`.

**Tests.** Added the Xantcha ownership-change test and `SubjectResolutionServiceTest$StaticSourceOwnerAndController` (2 cases). None changed. Ran 93 classes plus a re-run of 2: 189 XML files, 1,503 cases, 0 failures.

**Docs.** `ORACLE_TEXT_EFFECT_MAP` named the wrong record for Fealty to the Realm (`EnchantedCreatureCantAttackControllerEffect`); now corrected.

**Seen, not changed.**
- CombatHelper's first scan reads the attacker's printed static abilities without face-down or lost-ability checks. It only pre-filters `getBlockableAttackerIndices`; `BlockLegalityService` decides. Sure of the code, low impact.
- The block step's defender is `getOpponentId(activePlayer)` (CombatBlockService), and `DefendingPlayerControlsPermanent` also uses `getOpponentId`. Both are right for two players only. Sure.

**For the next run.** Step 10 could rename `CantAttackCardOwnerEffect` (to something like `CantAttackPlayersEffect`) and `CantBeBlockedIfDefenderControlsMatchingPermanentEffect` (to something like `CantBeBlockedWhileDefenderEffect`, with its predicate as `TargetPlayerControlsPermanent`), and could fold `CreaturesCantAttackControllerUnlessPredicateEffect`'s Aura-less forms onto the same restricted-`PlayerSubject` reader.

### Step 9, fold run 35c (2026-11-24, parallel worktree)
Batch `player-prot` is done: 2 folded, 0 left. Ratchet 3,325 / 2,676 → 3,324 / 2,676. Ledger 3 rows (absorbed 2, survivor 1). 2 card files rewritten, Oracle text checked for both, plus the rulings of Seht's Tiger, Faith's Shield, Serra's Emissary and Teferi's Protection. CR 702.16a, b, c, e, j and k, 702.11d, 708.2, 708.2a, 613.1f and 709.4a checked.

| Batch | Result |
|---|---|
| player-prot | Absolute Virtue and Runed Halo moved onto the new `PlayerHasProtectionEffect(Quality EACH_OPPONENT / CHOSEN_NAME)`. It is a static printed on a permanent (protecting its controller), or floated by `GrantStaticEffectToPlayerEffect`. `GameQueryService.playerProtectionFrom(gd, player, ProtectionSource(card, permanent, controllerId, colors))` is now the one reader. It is used by every player-target check in TargetLegalityService (cast, flagbearer, trigger and resolution paths), by Aura attachment (`canEnchantPlayer`, `illegalAttachmentReason`) and by one damage rule (`PlayerProtection`). It reads protection from everything, from players, from colours, from the chosen card type and from the chosen name, over active statics, granted statics and floated copies. |

**Behaviour changes**
- CR 702.16k: protection from a player covers objects that player owns and no player controls. The old reader returned false for a null controller. Test: GameQueryServiceTest$PlayerProtectionFrom.
- CR 702.16e: player colour protection against damage now checks every colour of the source after Ghostly Flame. Before, it checked the printed colour (noncombat) or one layered colour (combat). Test: SehtsTiger vs Cindervines, RED and GREEN.
- CR 708.2a and 613.1f: a face-down or ability-less Absolute Virtue or Runed Halo gives no protection. Copies of Runed Halo now protect (active statics read the current card).
- Protection from everything is now also read by the damage rule (CR 702.16j). Teferi's Protection keeps its separate prevention float; both are pure, so nobody is prompted, but one "prevented" log line is added.
- At cast time, the chosen card type is now judged against the source permanent's layered types.
- Logs: player-target reasons are now "<player> has protection from <quality> and can't be targeted"; Aura reasons are "enchanted player has protection from <quality>"; the four damage families are now one, `PlayerProtection`, labelled "Protection from <quality>".

**Tests changed:** TargetLegalityServiceTest and AuraAttachmentServiceTest now stub `playerProtectionFrom`. New: GameQueryServiceTest$PlayerProtectionFrom (6) and SehtsTigerTest noncombat multicolour (2).

**Seen, not changed**
- Serra's Emissary's chosen-card-type read uses only the printed statics, with no face-down check (CR 708.2a). Sure of the code.
- Player "hexproof from [colour]" checks one effective colour of a multicolour source (CR 702.11d). Fairly sure.
- Teferi's Protection-style handlers float a redundant "prevent all damage" next to protection from everything. Harmless; could be dropped.

**For the next run**
- Move the remaining player-protection payloads onto `PlayerHasProtectionEffect` qualities:
- colour: the floated `GrantProtectionFromColorUntilEndOfTurnEffect` from the run-18 `GrantProtectionChoiceToController*` rows (Seht's Tiger, Faith's Shield);
- everything: `GrantProtectionFromEverythingUntilNextTurnEffect`, 4 cards;
- the chosen card type: Serra's Emissary's controller half;
- players: the `ProtectionFromPlayersEffect` floats.
- The reader already reads all of them, so these become parameter folds.

### Step 9, fold run 41 (2026-11-24, parallel worktree)
3 of 8 batches done, each complete. Ratchet 3,318 / 2,672 → 3,312 / 2,668. Ledger 10 rows: absorbed 4, split 2, survivor 3, blocked-other 1. 6 card files rewritten, Oracle text checked for all 6 and for Saw in Half; rulings read for Dracoplasm, Wood Elemental, Fell the Mighty, Mandate of Abaddon, Expel the Interlopers, Red Sun's Twilight and Saw in Half. CR 604.3, 608.2b, 608.2h, 613.1f, 613.4a, 613.4b, 614.1c and 707.9b checked.

| Batch | Result |
|---|---|
| asenters-sac-any | 2 folded. `SacrificePermanentsAsEntersForCountersEffect` gains `Payoff`: `COUNTERS`, `BASE_POWER_TOUGHNESS_TOTAL` (Dracoplasm: last-known totals from the `DepartureResult`s of `sacrificeTogether`, layer 7b with the entry's timestamp) and `POWER_TOUGHNESS_EQUAL_TO_COUNT` (Wood Elemental: a CDA, layer 7a, timestamp 0). It now has one as-enters branch, one `MultiPermanentChoiceContext.SacrificeAsEntersForCounters(effect)` and one answer handler. Two contexts and two handler methods are gone. |
| power-bound | 3 folded. New `PermanentPowerAtLeastAmountPredicate` / `PermanentPowerAtMostAmountPredicate(DynamicAmount)`, bound once as the wipe resolves (CR 608.2h) by the new `AmountEvaluationService.bindAmounts(gd, PermanentPredicate, ctx)`. `DestroyAllPermanentsEffect` gains `referencedTarget` and `.readingTarget(filter, target)`, because only an effect's `targetSpec` declares a single target (a target group with a non-targeting effect does not). Fell the Mighty and Mandate of Abaddon use `TargetPower(true)`, so a negative power isn't clamped. An illegal target counters the spell (CR 608.2b), as the rulings say. Expel the Interlopers is `ChooseNumberAtResolutionEffect(0, 10)` + `XValue`. |
| departed-copies | 1 folded, 1 left. Red Sun's Twilight: `DestroyTargetPermanentEffect(allTargets(), artifact)` + `ConditionalEffect(SpellXAtLeast(5), CreateTokenCopyOfTargetPermanentEffect(true, true).withSubject(departedThisWay(DESTROY)))`. Each destroyed artifact is copied as it last existed. `target()` destroys only the first target. Saw in Half is blocked-other (see the ledger row). |
| same-quality, topcard-look, facedown-look, may-reveal-hand, topcard-bf | not started (run length) |

**Behaviour changes.** None observable. The as-enters answer check (controller, filter, not the entering permanent) now covers Shimatsu, Caprichrome, Devouring Hellion, Famished Worldsire, Feasting Hobbit and Dracoplasm. Dracoplasm sums only what was actually sacrificed. Red Sun's Twilight's destroy log is the survivor's.

**Tests changed.** `DestroyAllPermanentsEffectHandlerTest`: constructor wiring only.

**Seen, not changed.**
- `CreateTokenCopyOfTargetPermanentEffectHandler.resolveEsixReplacement` counts only copy sources still on the battlefield. So copies of departed permanents (Red Sun's Twilight; the old handler was the same) never get Esix's first-token-creation offer. Fairly sure.
- `ExileTargetPermanentAndCreateTokenCopyEffect` (Dedicated Dollmaker, Kaya, Nemesis Trap; blocked-other, "needs the exiled card's last-known copiable values") may now fold onto the copy survivor over `departedThisWay(EXILE)`. Unverified.

**For the next run.**
- `same-quality` (12 members) needs:
- a `CardSharesCreatureTypeWithPermanentsPredicate(PermanentSubject)` bound in `bindCardSubjects` from live or `DepartureResult.lastKnownSubtypes`, used by Pyre of Heroes `paidAsCost()`, Call to the Kindred `attached()`, Conjurer's Mantle and Descendants' Fury;
- its permanent twin for Endemic Plague's wipe, bound in `DestroyAllPermanentsEffectHandler` next to `bindAmounts`;
- a card-subject name predicate for Infernal Tutor and Assembly Hall (`revealedThisWay()`);
- a target declaration for Pack Hunt's search (the problem `readingTarget` solved for wipes);
- Mimeofacture: `SearchTargetLibraryEffect` must bind its filter (its handler matches without game state);
- Canoptek Wraith fits `CardSharesNameWithPermanentsPredicate(thisWay(CHOOSE))` as it is.
- Then `topcard-look`, `facedown-look`, `may-reveal-hand`, `topcard-bf` (CR 701.20a/b, 708.5 checked).

### Step 9, fold run 39 (2026-11-24, parallel worktree)
All 11 batches done. Ratchet 3,324 / 2,675 → 3,311 / 2,670. Ledger 24 rows: absorbed 14, survivor 8, unique 2. 17 card files rewritten; Oracle text checked for 21 cards. CR 104.3, 121.6, 500.7, 614.6, 614.10, 616.1, 616.1e, 701.6a checked.

| Batch | Result |
|---|---|
| loss-repl | 1 folded. `ReplaceControllerLossWithExileAndLifeTotalEffect(DynamicAmount)`, with `startingLifeTotal()`; one `ExileSourceAndSetLifeTotalLossReplacer`. `LossReplacer` now has `sources` / `replace` (plus `replacesBeforeLoss` for Enduring Angel). `GameOutcomeService.replaceLoss` gathers every source and the losing player chooses (`LossReplacementChoice`). A choice made before the Lich's Duel Mastery questions is kept on `PendingLossReplacement.sourceId`. Lich's Mirror gives one source, since every Mirror's reset is the same event. |
| token-kind | 1 folded. The survivor takes `TokenKind replaces` (`creatureTokens(token)`). The entry path applies subtype forms first (by class level), then the type forms. |
| extra-upkeep | 1 folded. `AdditionalUpkeepStepEffect(DynamicAmount, Placement)`. No store merge: the two counters are consumed at different points of the turn, and one has a return step. The no-extra-from-an-extra guard is kept for AFTER_THIS_STEP only. |
| counter-each | 1 folded. `CounterSpellEffect` bound to a group with several targets counters each one and records a single COUNTER_SPELL step. |
| counter-set | 2 folded. `CounterMatchingSpellsEffect(filter, types)` with `abilities` / `spellsAndAbilities`. It looks entries up by targetable id, records COUNTER_SPELL and implements `CounterSpellingEffect`. Glen Elendra's Answer creates its tokens with `EventValue`. |
| poison-counter | 1 folded, onto the new `StackEntryControllerPoisonCountersAtLeastPredicate`. |
| extra-draw | 1 folded. `Payload` DRAW_A_CARD / CREATE_A_TREASURE. Notion Thief is still taken before Hullbreacher. |
| extra-turn | 1 folded. `SkipAllExtraTurnsReplacementEffect(PlayerSubject)`, judged by `namesPlayer`; `appliesToExtraTurn` is gone. |
| enter-triggers | 3 folded. `CreatureEnteringDontCauseTriggersEffect(Event, types, abilityControllers)`. A form naming only some players requires ENTERING and no type filter, because the reader for opponents' abilities doesn't know the entering permanent. One scan, `anyActiveTriggerSuppression`. |
| draw-repl-look | 2 folded. No existing draw replacement takes a look count and a destination (Sages of the Anima reveals and keeps every creature), so the interface became the record `(lookCount, restToGraveyard)`. |
| life-to-loss | 0 folded, 2 `unique`. Rain of Gore's gains are any player's from a spell or ability they control, on any battlefield. That condition would be Rain's option alone (guard rail). |

**Behaviour changes.**
- Loss replacements: the losing player chooses among all that apply (CR 616.1), where the first replacer bean in order used to win. This fixes the "Fold: loss-repl" defect. Tests: Throne+Archangel, Throne+Lich's Mirror, picking one of two Archangels.
- Throne: a face-down Throne has no replacement.
- Hullbreacher and Notion Thief: a face-down copy replaces nothing (CR 708.2a). Test added for Hullbreacher.
- Token entry path: asks `hasLostPrintedAbilities` (CR 613.1f). This is part of the "TokenCreationReplacementSupport's static counts" defect line; Divine Visitation's and the subtype reads there are fixed, `staticEffectCount` is gone.
- Elesh Norn: every battlefield is checked, with `hasLostPrintedAbilities`.
- Double Negative and Katara's Reversal: record COUNTER_SPELL and keep a countered ability's chosen player.
- AI: Swift Silence is a counterspell, and Double Negative is scored as one.
- Logs: the survivors' log lines win (Divine Visitation's option label).

**Tests changed.**
- Deleted `CounterSpellIfControllerPoisonedEffectHandlerTest` (handler gone; its cases are in `CorruptedResolveTest`).
- Removed unused helpers from `CounterSpellEffectHandlerTest` and `CounterUnlessPaysEffectHandlerTest`.
- `TurnProgressionServiceTest`: record rename.
- Added 4 tests.

**Seen, not changed.**
- `CounterSpellEffect` over several targets with `LIBRARY_TOP_OR_BOTTOM` would open one prompt per target. No card does this.
- `KatarasReversalTest` methods have no `@DisplayName`. Sure.
- The Tomorrow and Underrealm Lich scan doesn't skip face-down sources. Sure.
- Not tested: the Throne face-down fix, the token entry-path fix, the Elesh Norn multiplayer change.

**For the next run.** The face-down draw-replacement defects: Pursuit of Knowledge, Uba Mask, Tomorrow.

### Step 9, fold run 40 (2026-11-24, parallel worktree)

11 folded, 6 survivor, 5 blocked-other, 2 dead. Ratchet 3,324 / 2,675 → 3,313 / 2,666. 19 card files rewritten; Oracle text checked for 14. CR 121.2, 608.2h, 613.1f, 705.2 and 707.10 checked.

| Batch | Result |
|---|---|
| per-object-flips | 0 folded, 3 `blocked-other`. Which grouping is right is unsettled: Edgar's ruling ("flip multiple coins at once") doesn't say whether "flip a coin for each X" is one set. Rakdos flips at once (and the rulings have its tails creatures destroyed together); Fighting Chance and Mutalith flip one at a time, so a survivor changes one side (owner's question). Also missing: a per-flip "that creature / that player" binding, a one-shot prevent-combat-damage verb that takes a subject (Fighting Chance), and a FLIP result naming the tails permanents (Rakdos). |
| multi-flip | 4 folded into `FlipUntilLoseEffect(coins, stopAtLoss, contested, perWin, perLoss, allWins)`. Squee's Revenge is `upTo`. Yusri is `flip`, with `allWins` under `SpellXAtLeast(5)` (same as the old coins==5 && wins==5). Crazed Firecat is the flip plus `PutCountersOnSelfEffect(AmountThisWay(FLIP))`. Goblin Traprunner is the flip plus `CreateTokenEffect.withAmount(AmountThisWay(FLIP))`. Both Ral Zareks are `headsOrTails`. Mirror March became the flip plus one copy step with `AmountThisWay(FLIP)`. `perLoss` has one user (Yusri), as the queue row named; Mutalith would be the second. |
| two-dice | not started (an R record with spell-number prompts); stays `step-9`. |
| sac-condition | 3 folded. `SacrificedCardMatches` gains `permanentFilter`, judged on `ConditionContext.sacrificedPermanent` (the entry's snapshot, CR 608.2h). The card filter falls back to the snapshot's card, as the old handler did (copies, CR 707.10). |
| lib-pos-draw | 1 folded. `DrawFromBottomOfLibraryEffect(From BOTTOM / ANY_POSITION)` is read through one `activeStaticEffects` pass. Both marker interfaces deleted. |
| draw-new | 0 folded. Wyll: a cost-shape row that needs a target payer, for step 10. Jon Irenicus: a NotCondition step would judge the libraries again after the draw (6 vs 5 draws, then 5 vs 5 also mills); it needs `ConditionalEffect` otherwise-branch, which only it would use. |
| draw-doublers | `DoubleDrawReplacementEffect(exceptFirstDrawStepDraw)` absorbed DoubleDrawExceptFirstDrawStepDrawEffect (3 cards, same text). One reader takes the kind as a parameter; the dead `findExceptFirstDoubleDrawSourceCard` is removed. |
| power-doublers | 2 folded. `DoubleTargetCreaturePowerEffect(times, subject)` with `of(subject)` and `ofCreaturesYouControl(filter)`. |

**Shape.** `StepKind.FLIP` is recorded after the last flip, before `allWins`: the flips won, or the heads where no one wins. Flips that stop at a loss are separate flips with checkpoints, and the resume point is the wins so far. A counted set flipped at once is one `flipCoins` call. Lost flips fire the lost event. Heads-or-tails coins fire nothing, unless Edgar made them won flips.

**Behaviour changes.**
- Mirror March: the tokens enter at once (ruling). Test with Woodland Champion.
- Squee's Revenge, Okaun, Zndrsplt and Goblin Traprunner: a lost flip fires the lost event (CR 705.2). Tests with Karplusan Minotaur. This is the "Fold: multi-flip" defect.
- Heart of a Duelist: it no longer replaces draws after losing its abilities (CR 613.1f). Test with Opalescence and Humility.
- Two Maro's Gone Nuts: a targeted power doubling is ×8, not ×16, the shared `MaroGoneNutsSupport` rule. Test with Unleash Fury.
- The AI no longer sees the conditional draws of Agency Coroner, Splitting the Powerstone and Tevesh Szat (`ConditionalEffect` hides `CardDrawingEffect`).
- Log lines: the survivor's lines win. A set flipped at once logs "flips N coins … K heads" and, when contested, a line per flip. Power doubling logs one line per creature. No asserting test changed.

**Tests.**
- Added: Squee's Revenge, Goblin Traprunner, Mirror March, Heart of a Duelist, Maro's Gone Nuts.
- Moved: the two flip handler tests went into `FlipUntilLoseEffectHandlerTest`.
- Changed: `DrawServiceTest` stubs `activeStaticEffects` for its Lantern case.

**Seen, not changed.**
- Edgar's ruling makes heads-or-tails flips won flips. `FlipCoinWinEffect.headsOrTails` (Odds) and Rakdos fire no won event under Edgar. Fairly sure.
- Mutalith fires coin-flip events for a Pokey d20 result (already queued).

**For the next run.** `two-dice`: check `RollTwoD20IgnoreLowerEffect` and `RollWithAdvantageEffect` first. Then the per-object flips, after the owner rules on grouping.

### Step 9, fold run 43 (2026-11-24, parallel worktree)

**specialize** (20 rows): 18 folded, 2 left.
- No existing transform or perpetual record could carry it. `TransformSelfEffect` has one back face, and the perpetual stores can't change name, mana cost, colors or subtypes. So this is a new record: `SpecializeEffect(CardColor color, SpecializeEffect.Faces faces)`, with one handler.
- The handler finds the source permanent and checks it is still the card the ability was activated from (`entry.getCard()` name). It makes a runtime copy, clears its abilities, calls `faces.writeFace(face, color)`, swaps the face in (`exchangeCard`), and puts the returned "when this creature specializes" effects on the stack as a made ability.
- Each card passes `Card::specializedFace`, a static method in its own class that holds what its old handler did. So every face keeps its own text, abilities and target groups.
- The `Faces` hook follows the `Supplier<Card>` records (`DraftCardIntoHandEffect`).
- Vhal: `RemoveVhalStudyCountersEffectHandler` now puts the face's "when you do" ability on the stack itself.
- Wyll and Vhal no longer set an entry target filter or `nonTargeting` on the made ability. The face's target group gives the same targets (tests pass).
- Left, `unique`: `SpecializeKarlachEffect`, the only specialize that works from a graveyard; its graveyard return is an option only Karlach would set. Also `UnspecializeLukaminaEffect`, the only unspecialize.
- Fixed, failing-first: Karlach, Raging Tiefling. Activated on the battlefield and killed in response, it used to be specialized in the graveyard and returned (CR 400.7). The handler now returns when the source permanent is gone.

**room-door** (1 row, plus the unaudited `LockOrUnlockTargetRoomDoorEffect`): folded.
- New shape: `UnlockControlledRoomDoorEffect(PermanentSubject rooms, boolean lockOrUnlock)`. `()` means the Rooms you control (Ghostly Dancers). `targetRoom()` means up to one target Room you control (Ghostly Keybearer). `lockOrUnlockTargetRoom()` is for Keys to the House and Marina Vendrell.
- The Rooms are resolved by `SubjectResolutionService`. Unlocking gives the designation, a new timestamp and the DoorUnlocked event, plus FullyUnlocked on the second door (CR 709.5f, 709.5i). Locking removes the designation (CR 709.5g).
- One choice context: `UnlockRoomDoorChoice.lockOrUnlock`; `LockOrUnlockRoomDoorChoice` is deleted.
- Survivor guard: the door index check (0 or 1) now applies to the unlock-only cards too. It changes nothing for them.
- The prompt texts, option labels and log lines are kept for each mode.

**Seen, not changed:**
- Rasaad, Radiant Monk: the white face also gets an "enters" trigger, but its card text says only "When this creature specializes" (CR none; HBG 4; unsure, the face's Oracle couldn't be checked).
- Marina Vendrell: the ETB "reveal the top seven" goes through `LookAtTopCardsEffect`; unchecked whether it reveals.

**Ratchet:** records 3300 → 3282, handlers 2659 → 2641.

**For the next run:** card-side hooks passed as method references (`SpecializeEffect.Faces`) are a way to fold per-card "build this face" records. Karlach's graveyard specialize would fold if a second card ever specializes from a graveyard.

### Step 9, fold run 37 (2026-11-24, parallel worktree)
8 of 9 batches done; `prot-dyn` left (time). Ratchet 3,294 / 2,659 → 3,274 / 2,656. Ledger 32 rows: absorbed 18, split 1, survivor 11, `step-9` 2. 20 card files rewritten, Oracle text checked for all 20. CR 105.2, 105.2a–c, 113.10, 202.3e, 508.1c, 508.1d, 509.1b, 509.1h, 510.1c, 511.3, 602.5, 611.2a, 613.1f, 702.16a–f, 702.16i and 708.2a checked.

| Batch | Result |
|---|---|
| power-gated | 1 folded. Ensnaring Bridge is `CreaturesCantAttackUnlessPredicateEffect(Not(PermanentPowerGreaterThanSourceControllerHandSizePredicate))`. The exemption is now judged with the source's card and controller as filter context; the old branch passed none. `AmountEvaluationService` was dropped from `AttackLegalityService`. |
| become-blocked | 1 folded. `MakeTargetAttackingCreatureBlockedEffect(PermanentSubject)` resolves through the resolver (no-arg = `target()`, harmful). Fog Patch uses `controlledBy(eachPlayer(), PermanentIsAttackingPredicate)` with no target. |
| only-attackers | 4 folded, 1 split. `OnlyMatchingCreaturesCanAttackThisCombatEffect` gets `lands()`, `source()` (`PermanentIsSourcePermanentPredicate`) and `only(ids)`; its duration is the float's. One reader covers every float, with the float's source permanent and controller as context. Oracle en-Vec is now two floats, `MatchingCreaturesMustAttackEffect(chosen)` (CR 508.1d) and `only(chosen)` (CR 508.1c). `isOutsideChosenAttackers` is gone. Throat Wolf's active-player switch reads `onlySource()`. |
| blockability | 2 folded. `CanBlockCreaturesWithShadowEffect(Mode, attackers)` has modes AS_THOUGH_SHADOW, IGNORING_SHADOW, IGNORING_LANDWALK and AS_THOUGH_REACH; only reach takes the attacker filter. `BlockLegalityService` is unchanged. |
| tap-lock | 1 folded. `MatchingPermanentsCantActivateTapAbilitiesEffect(PermanentSubject)` takes either a `controlledBy` set (judged as an active static ability with `staticSubjectNames`) or `attachedCreature()` (through `hasAuraWithEffect`). |
| attached-restr | 5 folded onto the existing `AttachedPermanentRestrictionEffect`; no new record. All five texts are rules of the Aura, not granted abilities (CR 113.10, 613.1f). `CanAttackAsThoughHasteUnlessEnteredThisTurnEffect` gets `unlessEnteredThisTurn` (Chaos Lord true, Instill Energy false); `PreventTransformEffect()` is any permanent. Readers: `hasAttachedPermanentRestriction` / `attachedPermanentRestrictions(type)` (now public), `CombatCompanionRestrictions`, `canOnlyAttackAlone`, `isTransformPrevented`. `cantBeTargetedByOpponentSpellsOrAbilities` counts opponents of the attachment's controller. |
| prot-mv | 3 folded. `ProtectionFromManaValueEffect(Quality AT_LEAST/AT_MOST/PARITY/OTHER_THAN, …)` with factories `atMost`, `chosenParity`, `parity`, `otherThanChosenNumber`, `otherThan`. The `ProtectionGrantingEffect` facets are unchanged, so no reader changed; Concerted Effort's share uses the factories. |
| prot-colourcount | 3 folded. `ProtectionFromMulticoloredEffect(subject, ColorCount MULTICOLORED/MONOCOLORED/ENEMY_COLORED_PAIR/COLORLESS)`. Two empty staticfx handlers and two `LayerClassifier` entries were deleted. |
| prot-dyn | not taken. `ProtectionFromColorsEffect`'s colours are the static facet `protectionFromColors()`, read by 12 readers. A set judged as asked needs that facet to take the game and source, or the layered pass to supply it, plus Kannushi's two staticfx handlers and readers. |

**Behaviour changes.**
- The Katabatic Winds kind is read through active statics: a face-down or ability-less source locks nothing (CR 613.1f, 708.2a). Test: `GameQueryServiceTest` face-down lock, which fails on the old code.
- An attached restriction from an attachment that lost its abilities (Instill Energy, Errantry, Brawler, Moonsilver, Collar, Canopy Cover, Plax) no longer applies, and Canopy Cover and Plax honour the Aura-ignore effect. No card reaches this; no test.
- Bestowed Sightless Brawler's restriction is read by `CombatCompanionRestrictions` (the AI too). The declaration message is the same.
- The colorless protection reader also reads printed protection. No card has it printed.

**Tests changed.** `GameQueryServiceTest` (named deleted records; added the face-down case), `GameDataDeepCopyTest`, `FloatingCombatRules` (reads the survivor's ids). Ran 228 classes: 355 XML files, 3,121 cases, 0 failures.

**Seen, not changed.**
- Katabatic Winds' lock set is "flying", not "creatures with flying": a noncreature flying permanent's {T} abilities are locked. Sure of the code, low.
- `hasProtectionFromMulticoloredEffects` / Mono / Enemy ignore `protectionSubject()`, so Guildscorn Ward itself has protection from multicolored. Fairly sure.

**For the next run.** `prot-dyn` as its own run, with `ProtectionFromChosenColorEffect` (8 cards): a dynamic colour set on `ProtectionFromColorsEffect`, with its readers moved onto a context-taking facet or the layered pass.

### Step 9, fold run 42 (2026-11-24, parallel worktree)
The 8 cost batches (39 rows). Ratchet 3,300 / 2,659 → 3,279 / 2,658. 28 card files rewritten; Oracle text checked for those and the cards left; Hinata's rulings checked. CR 113.6d, 118.7, 118.7a, 118.7c, 118.7d, 118.8, 118.9d, 601.2f, 702.27a, 702.29a, 702.34a, 702.37a, 702.37b, 702.37e, 702.49a, 702.109a, 702.143a, 702.151a, 708.2a, 708.4, 709.5e, 709.5f and 903.3 checked.

| Batch | Result |
|---|---|
| match-spell | 4 folded into `ReduceCastCostForMatchingSpellsEffect` (chosen subtype, graveyard zone, chosen name, imprint predicates). 5 `unique`: Seal, Locket and Prowler each need a count against the spell being cast that only they would use; Myth Unbound's "your commander"; Eberhart's drawn-by predicate. |
| targets-x | 6 `blocked-other`: target-dependent costs are read by separate ad-hoc readers (first-category-wins `computeTargetBasedCostReduction`, one availability preview per kind, the AI). A reader redesign comes first. |
| mana-unit | 1 folded (`ReduceOwnColoredCastCostEffect` takes a ManaCost unit). Eluge and Flying Drone `unique` (one-card options). |
| kw-costs | 7 folded into new `ModifyKeywordCostEffect`. Cosmos Charger `unique` (any-turn foretell timing). |
| ability-kind | 3 folded into `ReduceActivatedAbilityCostEffect`'s new `ActivatedAbilityKind`. |
| per-target | 3 folded into new `ModifyCastCostPerTargetEffect`. |
| cost-scope | 3 folded: `IncreaseSpellCostEffect` gains a static `players` subject; Elspeth and Tax Collector are a floated `untilYourNextTurn` grant. Loremaster and Realmwarden `blocked-other`. |
| own-sign | 2 folded: `ReduceOwnCastCostEffect.increase`, `ReduceOwnCastCostIfTargetingPermanentEffect.increaseIfTargeting`. |

Behaviour changes: alternative costs reduced or taxed (CR 118.9d; 3 tests); the face-down judging (CR 708.4); stack zone for own surcharges (CR 113.6d).

Seen, not changed:
- `computeTargetBasedCostReduction` returns the first non-zero category, so a per-target reduction hides a spell-self "if it targets" reduction (CR 601.2f). Sure of the code.
- Infectious Curse's preview runs only for instants and sorceries, so a Curse cast at the cursed player isn't shown as playable at the reduced cost. Sure of the code.
- The per-target readers still have no lost-abilities or face-down check. Sure.

For the next run: a targets-x run (one target-aware reader that sums every category, one preview), then fold the 6 rows and `ReduceOwnCastCostIfTargetingStackEntryEffect`.

### Step 9, fold run 41b (2026-11-24, parallel worktree)
3 of 5 batches taken (same-quality partly, topcard-look and facedown-look whole); may-reveal-hand and topcard-bf not started (run length). Ratchet 3,287 / 2,655 → 3,273 / 2,641. Ledger 19 rows: absorbed 10, split 4, survivor 2, blocked-other 2, step-9 1. 17 card files rewritten, Oracle text checked for all; rulings read for Call to the Kindred, Endemic Plague, Pyre of Heroes, Infernal Tutor, Pattern Matcher, Conjurer's Mantle, Followed Footsteps, Mechanized Production. CR 201.2a, 400.2, 608.2b, 608.2h, 701.20a, 701.20b, 701.20e, 701.23a, 702.73a, 704.5m, 708.2a, 708.5 checked.

| Batch | Result |
|---|---|
| same-quality | 9 folded, 3 left. New `CardSharesCreatureTypeWithPermanentsPredicate` and `PermanentSharesCreatureTypeWithPermanentsPredicate`, bound by `SubjectResolutionService.bindCardSubjects` / new `bindPermanentSubjects` from `creatureTypes(...)` (live layered subtypes, else the entry's sacrificed/triggering/attached/source snapshot, `DepartureResult.lastKnownSubtypes` or the departure snapshot; changeling = every type; a face-down snapshot without recorded types names none). New `CardSharesNameWithCardsPredicate(CardSubject)`. Pyre (`paidAsCost()`), Endemic Plague (wipe, bound next to `bindAmounts`), Call to the Kindred (`attachedCreature()`), Conjurer's Mantle (`triggering()`), Pattern Matcher, Canoptek Wraith (`thisWay(CHOOSE)`), Infernal Tutor and Assembly Hall (choose + reveal + `revealedThisWay()`), Pack Hunt (`SearchLibraryEffect.targetSpec` declares a benign permanent target when its filter reads a target subject). `LookAtTopCardsEffectHandler` binds card subjects. Left: Mimeofacture and Kasmina blocked-other (one-card options), Descendants' Fury step-9 (not reached). |
| topcard-look | 3 folded onto `LookAtTopCardsOfTargetLibraryEffect.lookingAt(count, PlayerSubject)`: a LOOK_ONLY over named libraries, private, promptless, APNAP (6 cards). |
| facedown-look | 2 folded: `LookAtFaceDownCreatureEffect(PermanentSubject)` (`target()` default); Spy Network split into hand look + `lookingAt(1, targetPlayerDeclaredElsewhere())` + face-down look. |
| may-reveal-hand, topcard-bf | not started |

**Behaviour changes.** Call to the Kindred: a creature that left first leaves the Aura enchanting nothing, so no match (CR 608.2h, 704.5m; Followed Footsteps and Mechanized Production rulings); failing-first, as the old handler used the creature's snapshot. Assembly Hall with no creature card searches, finding nothing (CR 701.23a); new Ob Nixilis test, failing on the old code. Infernal Tutor and Assembly Hall reveal a lone card unasked (CR 608.2d) through the card prompt. Endemic Plague records DESTROY; spell-cost sacrifice snapshots keep effective subtypes. Prompts and look lines are the survivors'.

**Tests changed.** AssemblyHallTest, InfernalTutorTest (prompt mechanics); CallToTheKindredTest (rules); MedomaisProphecyTest (log line); DestroyAllPermanentsEffectHandlerTest, LookAtTopCardsEffectHandlerTest, LookAtTopCardsOfTargetLibraryEffectHandlerTest (wiring). Added: Assembly Hall empty reveal, Endemic Plague changeling, SubjectResolutionServiceTest creature-type binding.

**Seen, not changed.** Pack Hunt's AI aim stays HARMFUL (the old name entry), although your own creature is the useful target. AI only, unsure.

**For the next run.**
- may-reveal-hand (3) and topcard-bf (4): their records stage may-abilities through `pendingMayAbilities` with `Stage` enums; the fold moves the stages onto the survivors.
- Descendants' Fury: the recipe is in its ledger row; `CardSharesCreatureTypeWithPermanentsPredicate(departedThisWay(SACRIFICE))` now exists. Compare the old `cantBeChosenToSacrifice` filter with `ChoosePermanentsEffect`'s.

### Step 9, fold run 44 (2026-11-24, parallel worktree)
10 of 12 batches done (next-spell-rider waits for 44b, timing-perm stays `unique`). Ratchet 3,274 / 2,650 → 3,255 / 2,632. Ledger 37 rows: survivor 10, absorbed 20, split 1, dead 1, unique 3, step-9 4. 29 card files rewritten; Oracle text checked for 13. CR numbers checked: 101.2, 400.7, 603.10a, 608.2b, 613.1f, 613.7, 701.40a, 701.40g, 701.50b, 701.50c, 701.50f, 701.60b, 701.60c, 701.60d, 701.65a, 701.65b, 702.51a, 708.8.

| Batch | Result |
|---|---|
| airbend | 2 folded. `AirbendTargetPermanentEffect(PermanentSubject, TargetSpec declaredTarget)` with `targeting(TargetPredicate)` (Aang's spell branch is the verb's rules, CR 701.65a) and `of(subject)` (Monk Gyatso's `triggering()`). Avatar's Wrath is `except(allCreatures(), targetDeclaredElsewhere())` with its spared target. One exile event, one airbend event if anything was exiled. AirbendSupport is gone. |
| connive | 2 folded, plus PutCounterOnConvokeCreaturesEffect. `DrawDiscardAndConniveEffect(amount, PermanentSubject)`; the flags became subjects. Several conniving permanents go one at a time (CR 701.50c) through `ConniveOrderChoice`, inserting single connives the way explore does. |
| suspect | 2 absorbed, SuspectedEffect dead, Snarlfang `unique` (its trigger would have to contain itself). |
| unsuspect | 1 folded over set subjects. |
| suppression | 1 folded (`target()`). |
| tap-flip | 1 folded (`controlledBy(activePlayer(), …)`). |
| turn-face-up | 3 folded onto `TurnSourceFaceUpEffect.ofTarget()`. Every subject now uses the GameService path (prevention check, turn tracking) plus the CR 701.40g reveal. Expose the Culprit's option got a face-down creature filter. |
| no-combat-dmg | 1 absorbed, 1 split (Cloak of Confusion: the no-damage step + `DiscardEffect(defendingPlayer(), random)`). `AssignNoCombatDamageEffect(PermanentSubject, thisCombatOnly)`. Floral Spuzzem and Goblin Vandal's record left for 44b: does a regeneration count for HappenedThisWay.destroyed()? Vandal's "if you do" is the payment; the TriggerTargetCollector special case is shared. |
| manifest-other | 2 folded. A library-owner PlayerSubject on `ManifestTopCardEffect` and `CloakTopCardOfControllerLibraryEffect` (`fromLibraryOf(triggeringPlayer())`). One `ManifestService.manifestTopCard(owner, controller)`. |
| next-spell-rider | 0 folded. `GameData.applyNextSpellRiders` (domain) checks hard-coded types; a CardPredicate needs an engine cast hook ahead of the enter-counter copy in `SpellEvents.cast`, reached by every recordSpellCast path. |
| prepared | 1 folded (`BecomePreparedEffect.ofTarget()`; the flag became a subject). The copy goes to the prepared permanent's controller. |
| timing-perm | 0 folded, 2 `unique`. No existing static carries an activated-ability filter, and an equip-only option would be Leonin's alone (guard rail). |

**Behaviour changes:** as listed in the report above. Two survivor guards now cover the absorbed cards: the CR 701.60d skip for J. Jonah Jameson, and the manifest-blocked check for Orochi Soul-Reaver and Thieving Amalgam.

**Tests changed:** LethalSchemeTest (the order prompt, CR 701.50c); 4 tests added.

**Seen, not changed:**
- Goblin Vandal with an indestructible artifact: once you pay, the creature must still assign no combat damage (Oracle text), but the handler returns early. Sure.
- The AI reads Avatar's Wrath's spared target as harmful removal. Sure.
- AvatarsWrathTest and AppaSteadfastGuardianTest have methods without `@DisplayName`. Sure.
- The copy-service turn-face-up path used by SBA does not check "can't be turned face up". Unverified.

**For the next run (44b):** the next-spell-rider cast hook; Floral Spuzzem and Goblin Vandal's split together with `GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect`.

### Step 9, fold run 45 (2026-11-24, parallel worktree)
13 absorbed, 2 split, 8 survivor, 5 unique, 1 blocked-other; `rest-of-set` and `spell-perm-exchange` are left for 45b. Ratchet 3,263 / 2,641 → 3,251 / 2,629. 13 card files rewritten. CR 111.7, 115.7d, 118.12, 404.3, 608.2b, 608.2h, 613.1f, 614.1, 614.5, 701.12a, 701.12b, 701.16a, 701.17c, 702.122a, 702.171a, 702.184a and 702.184c checked.

| Batch | Result |
|---|---|
| crew-power | 1 folded. `CrewAndSaddlePowerModifierEffect.actions()` (`Action.CREW/SADDLE/STATION`) replaces `crewOnly`. Stoic Star-Captain is `PowerBoostForCrewAndSaddleEffect(2, {CREW, STATION})`. `StationPowerModifierEffect` is deleted. `UseToughnessForStationEffect` stays `unique`: Warden's rule covers your other creatures with T>P, a subject + filter only it would set, and a grant would wrongly be layer 6. |
| guess | 1 folded. `MasterOfPredicamentsEffect(threshold, guesser, ifNoSpellCast)`. `ifNoSpellCast` has one user (Seventh Doctor), as the row named. |
| retarget | 1 folded. The survivor gains a spell subject (`targetSpell`/`triggeringSpell`) and a new-target subject (`source`/`attachedCreature`). Captured now logs the survivor's lines. Muck Drubb stays `unique` (its every-occurrence mode). |
| rest-of-set | not started. Four bespoke interaction chains with AI strategies, plus Deliver Unto Evil's casting branch: a run of its own (45b). |
| entry-controller | 1 folded. `GatherSpecimensEffect(Kind)`, `.tokensCreated()`. |
| target-players-count | 2 folded. New `CountScope.TARGET_PLAYERS`. Officious needed CreateTokenEffect to split investigate-N always (CR 701.16a), which also fixes Pilfered Proof for the other investigate-N cards. |
| chosen-name-prohib | 1 folded. `SpellsWithChosenNameCantBeCastEffect(opponentsOnly, landsToo)` reads both chosen names; `landsToo` has one user, as the row named. |
| zone-exchange | 2 folded. `ExchangeGraveyardAndLibraryEffect(Zone, Zone)`: no shuffle, CR 404.3 prompt, one batch. |
| shared-exchange | 1 split. `sharedCardTypes` set + GAIN_CONTROL record + conditional Aura destroy over `attachmentsOf(thisWay(GAIN_CONTROL))`. `ThisWay` accepts GAIN_CONTROL. |
| spell-perm-exchange | not started (R). The new-targets offer is queued the same way by three handlers (incl. `GainControlOfTargetSpellEffect`); compose a "[player] may choose new targets for [spell]" step instead of one-card options. |
| opp-punisher | Gearhulk absorbed and Covenant split onto `MayEffect(..., else, TARGET_PLAYER)`. Willie stays `blocked-other`: "during their next turn" is measured from the attacking player, a duration no ban record has. |
| weigh | 0 folded. Dwarven Catapult and Celestial Judgment stay `unique` (no second user). |

**Behaviour changes.** The ones in the report above (Harness, investigate-N, Gauntlets, logs, AI view).
**Tests.** The ones in the report above (Harness, `ArmedWithProofTest`, the retarget handler test).
**Seen, not changed.**
- Stoic Star-Captain and Zilortha model their source's rule as a granted ability, which a "loses all abilities" on the creature removes (CR 613.1f). Fairly sure.
- Muck Drubb's "spell that targets only a single creature" is not enforced at targeting (its ruling). Sure.
- `PutExiledCardsIntoOwnersGraveyardEffect` (Covenant, Dakra) gives no CR 404.3 order prompt. Sure, low.
- Willie Lumpkin triggers on damage to any player, not "an opponent". Sure, low.

**For the next run.** 45b: `rest-of-set`, then `spell-perm-exchange` (new exchange record + a new-targets step, folding `GainControlOfTargetSpellEffect`'s offer too).

**Follow-up.** All 13 rewritten cards' Oracle text checked. The investigate split touches 14 "investigate N / X times / for each" cards; their 39 test classes (with the Esix and Pilfered Proof classes) pass, 311 tests. Fixed failing-first: Nyla, Shirshu Sleuth's "create X Clue tokens" is not investigating (CR 701.16a) but fired an investigated event per Clue; its Clues are now `withInvestigate(false)` (new `NylaShirshuSleuthTest` case with Val). An `ofClueToken(n)` for "create n Clue tokens" must be marked `.withInvestigate(false)`.

### Step 9, fold run 46 (2026-11-24, parallel worktree)
`pay-any`: 12 absorbed, 1 survivor widened, 2 unique, 2 blocked-other, 9 left for 46b. Ratchet 3,225 / 2,620 → 3,213 / 2,608. 14 card files rewritten; Oracle text checked for all 26 batch cards, rulings for 15. CR 101.4, 107.1c, 107.3a, 107.14, 118.3, 118.3a, 118.12, 118.12a, 119.4, 119.4b, 119.8, 601.2g, 603.12, 605.3a, 608.2b, 608.2d, 608.2g, 608.2h checked.

| Batch | Result |
|---|---|
| pay-any | Absorbed into `EachPlayerPaysAnyManaEffect(startingWithYou, PlayerSubject payers, Resource)`, `byYou(resource)`: PayXMana (Karn MANA, Rose Room X_MANA), PayXLife (Plunge), the energy token/boost/damage/each-other/counters records (Aether Refinery, Pia, Die Young, Harnessed Lightning, Territorial Aetherkite, Rampaging Aetherhood), PayXManaGainXLife (Vigil), PayXManaBoost (Squealing Devil), PayXManaSearchLibrary (Taj-Nar, `MayEffect(SequenceEffect.of(pay, search))`), PayAnyAmountOfLifeRevealAndExile (Vizkopa, `RevealCardsChooseOneToExileEffect(lifePaid())`), PayXManaRevealCardsChooseOneToDiscard (Hollow Specter). "If you paid one or more" is `ConditionalEffect(HappenedThisWay(kind))`. |

**The survivor.**
- `Resource` MANA ("any amount of mana"), X_MANA ("pay {X}": a cost with {X}, so X-only mana pays it), ENERGY (removes counters, CR 107.14), LIFE (up to the life total, none while life can't be lost, CR 119.4, 119.8; asks death replacements first).
- Mana is paid as a resolution cost through `ManaCost.payResolutionCost`, so Powerstone mana pays it. The prompt's maximum counts untapped sources (CR 605.3a, 608.2g).
- A payer who can pay nothing isn't asked. Records PAY_MANA / PAY_ENERGY / PAY_LIFE.
- The payers are never a target.

**Behaviour changes.**
- Pia creates one X/X Aetherjet (was X tokens).
- Aetherkite's damage is a reflexive ability (CR 603.12; ruling).
- Plunge can't pay life while its controller can't lose life (CR 119.8).
- Powerstone mana pays the join forces, Taj-Nar and Hollow Specter payments.
- Karn's −1 no longer takes X-only mana.
- Vizkopa's exiled card is linked to Vizkopa.
- Squealing Devil's ability with an illegal target no longer resolves (CR 608.2b); the sacrifice trigger is separate and unaffected.
- Log lines are the survivor's.

**Tests changed.**
- Aetherkite: resolves the reflexive ability before asserting the damage.
- Taj-Nar: 3 tests no longer expect an X prompt with max 0.
- 5 new tests (Pia ×2, Aetherkite, Plunge, Collective Voyage).
- Ran 56 classes: 684 cases, 0 failures.

**Left.**
- `unique`: Leyline Tyrant ("any amount of {R}": a colour option only it would set) and Azor ({X}{W}{U}{U}: a fixed part only it would set).
- `blocked-other`: Confiscation Coup and Behemoth of Vault 0. "May pay an amount of {E} equal to N" is a fixed may-cost (CR 118.12), so they go to step 10's cost-combinator merge beside `ForcedCostOrElseEffect(PayEnergyCost)`.
- 46b: Vault 112, Localized Destruction (a grant's controlledBy filter needs `bindAmounts` at resolution), Relentless Dead (MayEffect + a mana-value-equals-paid check; fixes the X = 0 defect), Halo Forager, Tester, Isareth, Fire Lord Sozin (reflexive X target filters through `QueueReflexiveAbilityEffect.withEventValue`), Depala (a reveal-top-N step with a DynamicAmount and a random bottom), Errant Minion/Power Leak (the upkeep player as payer + prevention of X of the damage).

**Seen, not changed.**
- The AI's `XValueChoiceAiStrategy` answers a life payment with its maximum: it pays all its life (Plunge, Vizkopa; existing behaviour). Sure of the code.
- Depala's payable pool counts artifact-only and Myr-only mana. Sure of the code.
- AI value readers don't see the payoffs inside `ConditionalEffect` or the reflexive ability (Aether Refinery, Pia, Aetherkite).

**For the next run.** 46b: the reflexive four first (one pattern), then Relentless Dead with `MayEffect`, then Errant Minion/Power Leak with a non-you payer.

### Step 9, fold run 37b (2026-11-24, parallel worktree)
1 of 1 batch done (`prot-dyn`), with `ProtectionFromChosenColorEffect`. Ratchet 3,190 / 2,605 → 3,187 / 2,602. Ledger 4 rows: absorbed 3, survivor 1. 11 card files rewritten, Oracle text checked for all 11; rulings read for Commander's Plate, Pledge of Loyalty, Empty-Shrine Kannushi, Sanctuary Blade. CR 105.2, 301.5c, 613.1e, 613.1f, 702.16a–f, 704.5m, 704.5n, 708.2a and 903.4 checked.

| Batch | Result |
|---|---|
| prot-dyn (+ chosen) | 3 folded into `ProtectionFromColorsEffect(colors, subject, doesNotRemoveAuras, ColorSet)`. The colour sets are `FIXED`, `CHOSEN` (`chosenColor([subject])`, 8 cards), `AMONG_PERMANENTS_YOU_CONTROL` (`amongPermanentsYouControl([subject])`: Kannushi, Pledge) and `OUTSIDE_COMMANDER_IDENTITY` (`outsideCommanderColorIdentity(attachedCreature())`: Commander's Plate). `protectionFromColors()` is now only the fixed colours. There is one reader, judged for the permanent whose ability it is: `GameQueryService.protectionColors(gd, protection, holder)` reads other permanents' layered colours; `protectionColorsForStaticEvaluation(gd, protection, holder, controllerId)` reads the in-flight layer-5 colours and is recursion-safe. Callers: `hasProtectionFrom` (printed and granted), the explain attribution filter, the layer seeding, the floated-grant case, the attached handler, the self handler, `StaticEffectSupport`'s conditional self branch, `PredicateEvaluationService`'s fallback and Concerted Effort's share. `GrantedAbilityViewFactory` and the `PermanentGrants` fixture keep the fixed facet: handlers harvest a dynamic set into a fixed attribution, so it never reaches a granted-effect list. Fixed and chosen self sets are seeded. A board-reading self set is applied in layer 6 by the new `ProtectionFromColorsSelfEffectHandler`. Kannushi's two handlers, the chosen handler and the commander handler are gone, as are the Kannushi branches in `hasProtectionFrom` and the share handler (the layered state already has those colours). |

**Behaviour changes.**
- Equipment is no longer exempt from the protection it gives. A Sword of War and Peace made red by Painter's Servant becomes unattached (CR 702.16d, 704.5n). Only an Aura's "This effect doesn't remove this Aura" keeps it on: `grantsSelfExemptProtection` is now asked for Auras only. Test: `SwordOfWarAndPeaceTest`; it fails on the old code, where every attached colour protection was exempt. Without this, Commander's Plate would have become exempt in the fold; `CommandersPlateTest` covers an identity colour and an outside colour.
- On an untouched board, `hasProtectionFrom` no longer reads a face-down permanent's printed protection (CR 708.2a). Test: Kannushi face-down case (not shown failing on the old code).
- `TextChangeTransformer` keeps `doesNotRemoveAuras` and the colour set when it rewrites a colour (`withColors`). No card reaches it; no test.
- The no-game fallback now counts a self chosen colour. No test.

**Tests changed.** `GrantedAbilityViewFactoryTest` (named the deleted record). Added: Sword of War and Peace red Sword; Commander's Plate red and green; Kannushi layer-5 colour and face-down; Concerted Effort sharing Kannushi's colours and Voice of All's chosen colour. Ran 76 classes, 176 XML files, 1,544 cases, 0 failures.

**Seen, not changed.**
- `ProtectionFromColorsEffectHandler` still gives a fixed self-subject protection to the host when its source is attached (a bestowed or reconfigured creature with printed protection). CR 702.16a makes it the object's own. No card has it. Sure of the code.
- Fixed-colour Auras without "doesn't remove" text (Mask of Law and Grace, Shield of Duty and Reason, the Strength Auras) stay exempt from their own protection when recoloured. Fairly sure; low impact.

**For the next run.** `prot-dyn` is closed. Re-triage `GrantProtectionFromChosenColorToPermanentsEffect` (Ward Sliver, `unique`) against `ProtectionFromColorsEffect.chosenColor(subject)` over a `controlledBy` set.

### Step 9, fold run 41c (2026-11-24, parallel worktree)
3 batches, 8 members: absorbed 6, split 2; survivors 3 (two look survivors, ChoosePermanentsEffect). Ratchet 3,190 / 2,605 → 3,182 / 2,590. 11 card files rewritten, all 11 checked with get_card; rulings read for Descendants' Fury and Incriminate. CR 400.7, 608.2c, 608.2d, 608.2h, 616.1, 701.20a, 701.20b, 701.20e, 701.21a, 702.73a checked.

| Batch | Result |
|---|---|
| may-reveal-hand | 3 folded. The survivor gains `OtherwiseDestination.MAY_BOTTOM` / `Stage.MAY_BOTTOM` (Bucolic Ranch, Cabaretti), binds card subjects in its predicate (`CardSharesCardTypeWithPermanentsPredicate(triggering())`, Amareth) and records `StepKind.REVEAL` for the card put into hand; Sorin is `(CardTruePredicate, TOP, [LoseLifeEffect(AmountThisWay.revealedManaValue(), you())])`. `SubjectResolutionService.cardTypes` reads the entry's last-known snapshot of a permanent that left (CR 608.2h), as the old Amareth handler did. |
| topcard-bf | 4 folded. `boolean otherwiseToHand` becomes `Otherwise {TOP, HAND, MAY_BOTTOM}` + `Stage` (the boolean constructor kept); the predicate is judged with the game and the source permanent or its snapshot (Nissa: `CardManaValueAtMostSourceCountersPredicate(LOYALTY)`); the step records RETURN, so Rulik Mons is the survivor + `ConditionalEffect(NotCondition(HappenedThisWay.returned()), token)`. A HAND card enters through `GameData.addCardToHand` (the old survivor added it to the list directly). |
| same-quality (Descendants' Fury) | Split onto choose + `SacrificeSelfEffect(thisWay(CHOOSE))` + `ConditionalEffect(sacrificed(), RevealUntil…(allOf(CREATURE, CardSharesCreatureTypeWithPermanentsPredicate(departedThisWay(SACRIFICE)))))`. The old `cantBeChosenToSacrifice` filter became `ChoosePermanentsEffect.toSacrifice()`: only the candidates the chooser controls and could sacrifice (CR 608.2d), also given to Incriminate. |

**Behaviour changes.**
- Descendants' Fury: a Kindred non-creature card is no longer "a creature card" (the old predicate took Kindred).
- Incriminate: a target that can't be sacrificed is not offered, so the other one is sacrificed without asking.
- Bucolic Ranch, Cabaretti, Lantern, Believe, Sorin: the look is now shown to its looker only (CR 701.20e).
- Sorin: the Mastery's question comes after the card is in the hand and before the loss (CR 608.2c, 616.1), matching the rider test class's own rule.

All of these have tests that would fail on the old code by reasoning. Prompts and logs are the survivors'.

**Tests changed.** LichsDuelMasteryRiderTest (Sorin mid-state, rules), GravebaneZombieTest (log line), GameEngineComponentScanTest (repointed to ExileTopUntilNonlandMayCastByDiscard's same-named pair). Added: DescendantsFury ×3 (can't be sacrificed, Kindred, changeling), Incriminate, and private-look tests in Bucolic, Lantern, ReasonBelieve and Sorin.

**Seen, not changed.**
- Barrin's Spite ("chooses and sacrifices one of them"): it can choose a target that can't be sacrificed. Left alone because with neither sacrificeable `.toSacrifice()` would return both; unsure.
- The battlefield survivor's Aura put path (`beginAnyTargetChoice`) records no RETURN; no current card reaches it.

**For the next run.**
- `LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect` (5 cards, unaudited) is the bf survivor with `Otherwise.HAND` plus `tapped`. Its Aura put goes through the entry batch, which the survivor's own Aura path should adopt when they merge.

### Step 9, fold run 40b (2026-11-24, parallel worktree)
6 folded (absorbed), 3 survivor (1 new), 4 blocked-other, 1 unique note updated. Ratchet 3,190 / 2,605 → 3,185 / 2,601. 9 card files rewritten, Oracle text checked for 9 of 9. CR 510.2, 611.2c, 612.8, 613.1c, 613.7, 614.9, 616.1, 706.2, 706.6 checked.

| Batch | Result |
|---|---|
| two-dice | 4 folded into the new `RollTwoDiceChooseOneEffect(sides)`: Valiant, Wild, Arcane, and Reckless (no ledger row before this run). It makes one roll of two dice through the new `DiceRollService.rollDice(gameData, player, sides, count)`: each advantage replacement adds one die to the roll, and the lowest results are ignored (CR 706.6). The dice events, log and spell-number prompt are unchanged (no prompt when both are equal). The chosen result becomes X and the other the event value, for the card's steps: `PermanentPowerAtLeastAmountPredicate(XValue)`, `CreateTokenEffect(XValue / EventValue)`, `SearchLibraryEffect(EventValue)`, `DrawCardEffect(XValue)`, the free cast's `EventValue` bound, `MassDamageEffect(XValue, false)`. Existing roll effects (`RollTwoD20IgnoreLowerEffect`, `RollD*Effect`) have no second result or choice. Grave left: its return "with N +1/+1 counters" carries an `int` through the graveyard prompt; its roll now uses `rollDice`. |
| redir-absorb | 1 folded. `RedirectPlayerDamageToSelfEffect` gains `combatOnly` and `.combatDamageFromUnblockedCreatures(requiresUntapped)`. Weathered Bodyguards: static, untapped. Kjeldoran Royal Guard: `GrantStaticEffectToPlayerEffect.untilEndOfTurn(...(false))`; that handler now keeps the source on a float whose payload names "this creature". `playerDamageAbsorbers` reads static and floated copies through one predicate; a source that left absorbs nothing (CR 614.9, Royal Guard ruling). |
| floated-name | 1 folded. `SetNameEffect(name, subject, Duration)` with a normalfx path: permanents found once (CR 611.2c), one layer-3 `target()` float each (CR 612.8, 613.1c). It takes Awakening of Vitu-Ghazi, The Curse of Fenric, Tenth District Hero's Mileva (whose `GrantStaticEffectToSourceEffect(SetNameEffect)` special cases in `LayerClassifier` and `applyL3Instance` are gone) and The Irencrag's name. `Permanent.persistentName`, its copy, its seed in `CharacteristicState` and its fingerprint are removed. |
| floated-doublers | 0 folded, 3 blocked-other. Overblaze: a resolved effect on the permanent, not an ability (a layer-6 grant would end under Humility); a shared non-ability floated source doubler would have only Overblaze and Jeska, and one record would change Overblaze's AI polarity (HARMFUL_DAMAGE). Jeska: "your opponents" is the ability controller's; it needs a multiplier and a caster's-opponents scope only it sets. Blind Fury: any controller's creatures dealing to creatures, a source set and scope only it sets. |

**Behaviour changes.**
- Pixie Guide and Barbarian Class apply to Valiant, Arcane and Grave Endeavor. Wild Endeavor rolls three d4 and ignores one (it used to roll an extra die per d4). Source: Pixie Guide's Oracle text, the Endeavor rulings. 4 new tests; each fails on the old code (old options were "4,2", "2,4", "3,1", "5,2").
- A resolved name follows timestamp order with static name setters (CR 613.7): Fenric after Witness Protection is named Fenric. New test; on the old code the static name beat the persistent seed.
- Blind Fury's doubling takes Maro's Gone Nuts' factor (Maro + Blind Fury: a 2/2 now kills a 2/5 blocker). New test; this is the queued defect.
- Logs: Royal Guard's activation line is gone (the survivor logs nothing). The name line "is named X." is kept on the new path.
- The AI now sees the Endeavors' follow-up steps (draw, destroy-all, mass damage) for its multipliers; the board-wipe view is kept through `DestroyAllPermanentsEffect` and `MassDamageEffect`.

**Tests changed.** The 5 Endeavor tests inject a fixed `DiceRollService` into `RollTwoDiceChooseOneEffectHandler` (Grave: its own handler), with `@DisplayName` added. ScourTest and TheCurseOfFenricTest replace `setPersistentName` with a floating `SetNameEffect`. KjeldoranRoyalGuardTest's end-of-turn check reads the floated survivor. `TargetPolarityClassifier`'s name entry is now `SetNameEffect` (NEUTRAL). Added: 4 Pixie Guide tests, the Fenric timestamp test, the Blind Fury + Maro test.

**Seen, not changed.**
- `RollD6Effect`, `RollD8Effect`, `RollD10Effect`, Clown Car, Neverwinter Hydra and Sword of Hours still ignore dice advantage (the queued line, minus the Endeavors). Sure.
- Overblaze's AI polarity entry is HARMFUL_DAMAGE, though one's own creature is the usual target. AI only, unsure.

**For the next run.** The rest of the advantage defect can move onto `DiceRollService.rollDice` (the d6, d8 and d10 rolls, Clown Car, Hydra, Sword of Hours). Grave Endeavor folds once a second card needs a dynamic "return with N counters". The three doublers need the owner's leave to add one-card options, or a second user.

### Step 9, fold run 44b (2026-11-24, parallel worktree)
2 batches done: absorbed 2, split 2, survivor 1. Ratchet 3,147 / 2,563 → 3,143 / 2,559. 6 card files rewritten; Oracle text checked for 8 cards, rulings for 7. CR checked: 118.3, 118.12, 506.2, 508.5, 601.2i, 608.2b, 608.2c, 613.1f, 701.8a, 701.19a, 702.12b, 708.2.

| Batch | Result |
|---|---|
| next-spell-rider | 2 absorbed, survivor widened. `EmpowerNextCreatureSpellThisTurnEffect(CardPredicate spellFilter, uncounterable, counters)`, `cantBeCountered(filter)`. `SpellEvents.cast` applies the riders first, before the entry takes `spellAdditionalEnterCounters`, through `DurationClock.waitsForSpell`, the judge that also ends the effect. Every `recordSpellCast` path reaches it. `GameData.applyNextSpellRiders` is gone. |
| no-combat-dmg | 2 split. Goblin Vandal: `MayPayManaEffect("{R}", SequenceEffect.of(DestroyTargetPermanentEffect(allOf(artifact, PermanentControlledByDefendingPlayerPredicate)), AssignNoCombatDamageEffect()))`, with no gate: "if you do" is the payment (CR 118.12). Floral Spuzzem: `MayEffect` of the same destroy + `ConditionalEffect(TargetPermanentMatches(not indestructible), AssignNoCombatDamageEffect)`. The destruction is the cost: a regenerated artifact was paid for even though it wasn't destroyed (CR 701.19a, so `destroyed()` would be the wrong reader), and an indestructible one can't be paid for (CR 702.12b, 118.3). Kukemssa Pirates / Orcish Squatters: `GainControlOfTargetEffect.withTargetPredicate(duration, allOf(filter, defending))` + `ConditionalEffect(HappenedThisWay(GAIN_CONTROL), …)`. The source-control check for Squatters stays in the survivor's while-source path. The `TriggerTargetCollector` special case and two unused `MultiPermanentChoiceContext` records went. |

**Behaviour changes.** A perpetually or grant-typed creature card gets Insist's rider (it used to use the rider up without getting it). Vandal's paid {R} stops damage when the artifact survives. Log lines are the survivors'.
**Tests changed.** RunedTerrorTest (class rename). Added: InsistTest perpetual creature, GoblinVandalTest indestructible.
**Seen, not changed.** None new.
**For the next run.** Nothing is left in these batches; the Laccolith Rig Disarm half stays with `unattach`.

### Step 9, fold run 45b (2026-11-24, parallel worktree)
3 absorbed, 1 split, 3 survivors (1 new, 2 widened), 1 unique kept, 2 blocked-other. Ratchet 3,147 / 2,563 → 3,144 / 2,560. 7 card files rewritten, Oracle text checked for 7 of 7, rulings read for 6. CR 115.7d, 400.7j, 608.2b, 608.2d, 701.12a, 701.12b, 701.20a checked.

| Batch | Result |
|---|---|
| rest-of-set | 2 folded, 2 left. Genesis Wave absorbed into `LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect.revealing(null, CardIsPermanentPredicate())`: Saheeli's Directive's template, and the triage note's reveal + choose + except chain wasn't needed. Emergent Ultimatum split: `SearchLibraryEffect(Fixed(3), CardHasExactlyNColorsPredicate(1), EXILE, null, true).withoutShuffle()` + `ChoosePlayerEffect.opponent()` + `ChooseCardEffect.oneOf(cardsThisWay(SEARCH)).by(playersThisWay(CHOOSE))` + `PutCardsIntoLibraryEffect.shuffledIn(cardsThisWay(CHOOSE))` + `CastCardsWithoutPayingManaCostEffect.anyNumber(cardsThisWay(SEARCH))`. The shuffled card has left exile, so no except is needed. Deliver Unto Evil: blocked-other (no non-target subject over all target graveyard cards; cast branch keyed by class; Wake to Slaughter's `opponentChoosesOneForHand` would be the second user). Dubious Challenge: blocked-other (no look-top-N, exile-up-to-M, rest-stays step that records EXILE). |
| spell-perm-exchange | 2 folded into the new `ExchangeControlOfPermanentAndSpellEffect(permanent, spell)` (`sourceAndTriggeringSpell()`, `targetCreatureAndTargetSpell()`): all or nothing (CR 701.12a), nothing for one controller, records GAIN_CONTROL. The new-targets offer is `MayEffect(ChooseNewTargetsForTargetSpellEffect.ifItHasTargets(spell))`. Sudden Substitution puts it on the spell's target group with `TARGET_SPELL_CONTROLLER`. Chimera uses `SequenceEffect.of(MayEffect(exchange), ConditionalEffect(HappenedThisWay(GAIN_CONTROL, 1), MayEffect(…triggeringSpell())))`. Commandeer and Aethersnatch use it on their target. `GainControlOfTargetSpellEffect` no longer queues the offer. |

**Guard rail.** The control-change offers skipped spells with no targets, and about 20 steal tests rely on that. Redirect's family asks anyway, and 5 tests assert it. Both behaviours are kept: `onlyIfItHasTargets` is set by the 4 control-change cards.
**Behaviour changes.**
- Saheeli's Directive logs a reveal (Oracle text "Reveal"; CR 701.20a; failing-first).
- The EXILE multi-pick library search keeps `requireDifferentNames` and no longer logs a shuffle that didn't happen.
- Emergent Ultimatum asks for cards pick by pick; a lone found card is chosen unasked (CR 608.2d).
- Chimera applies the creature's control first, so a refused change gives no spell (CR 701.12a; untestable, only Guardian Beast grants the ability).
- Log lines are the survivors'.
**Tests.** EmergentUltimatumTest rewritten for the new prompts. New: PerplexingChimeraTest +3, SaheelisDirectiveTest +1, SubjectResolutionServiceTest +1. Ran 73 classes, 848 cases, 0 failures.
**Seen, not changed.**
- Transcantation queues its own new-targets offer through a `CopySpellEffect` payload (`TargetSpellBecomesCopyOfCardEffectHandler`). Sure.
- Deliver Unto Evil still gives the choice to `getOpponentId` (multiplayer chooser; existing defect). Sure.
**For the next run.** Build the multi-target-card subject and a generic graveyard up-to-N cast path (Deliver Unto Evil, Wake to Slaughter), and a look-top-N exile step (Dubious Challenge).

### Step 9, fold run 46b (2026-11-24, parallel worktree)
`pay-any` (rest): 7 absorbed, 2 left for 46c. Ratchet 3,144 / 2,560 → 3,137 / 2,553. 7 card files rewritten; Oracle text checked for all 10 batch cards, rulings for 5 (Halo, Tester, Isareth, Sozin, Relentless Dead). CR 107.1c, 118.12, 601.2g, 603.12, 608.2g, 608.2h, 615.1 checked.

| Batch | Result |
|---|---|
| reflexive four | Halo Forager, Tester, Isareth, Fire Lord Sozin → `MayEffect(SequenceEffect.of(EachPlayerPaysAnyManaEffect.byYou(X_MANA), QueueReflexiveAbilityEffect.withEventValue(effect, AmountThisWay.manaPaid())))`; X is the reflexive's event value (`CardManaValueEqualsEventValuePredicate`, `EventValue()` count or total cap). |
| Relentless Dead | `MayEffect(Seq(pay, ConditionalEffect(TargetGraveyardCardManaValueEqualsStepAmount(PAY_MANA), ReturnCardFromGraveyardEffect(target))))`; target declared on the trigger, X on resolution; paid {0} returns an MV 0 Zombie. |
| Localized Destruction | `byYou(ENERGY)` + `ConditionalEffect(HappenedThisWay(PAY_ENERGY), GrantKeywordEffect(INDESTRUCTIBLE, controlledBy(you(), creature + PermanentPowerAtLeast/AtMostAmountPredicate(energyPaid()))))`. |
| Depala | parameters only: `LookAtTopCardsEffect(manaPaid(), manaPaid(), Dwarf|Vehicle, BOTTOM_OF_LIBRARY_RANDOM, true)` (Lair Delve's choice-free shape). |
| left | Errant Minion/Power Leak (needs a "prevent N of that damage" rider on the damage event, CR 615.1); Vault 112 (needs a play-now option on `ExileTopCardMayPlayThisTurnEffect.chooseOne`). |

**Behaviour changes.**
- "You may pay {X}" now asks a may first; declining pays nothing and makes no reflexive ability (was: X prompt only, X = 0 still queued; Isareth was forced to return an MV 0 card) (CR 603.12, 118.12).
- Relentless Dead's paid {0} returns an MV 0 Zombie (was: X = 0 declined).
- Myr-only and artifact-only mana no longer pays Halo, Tester, Sozin, Relentless Dead or Depala.
- Sozin asks even when nothing is returnable.
- Log lines are the survivor's.

**Shared engine.**
- The cast-from-graveyard handler and its target validator bind the event value.
- `GrantKeywordEffectHandler` fixes amount predicates in a `controlledBy` set as it resolves.
- New `TargetGraveyardCardManaValueEqualsStepAmount` reads a trigger's single graveyard target from the resolving entry.

**Tests changed.**
- 6 card tests now answer the may; Depala's crew test declines it.
- 9 new tests.
- Ran 61 classes, 755 cases, 0 failures.

**Seen, not changed.**
- Fire Lord Sozin's reflexive targets `ALL_GRAVEYARDS`; Oracle says "from that player's graveyard", so the controller's own graveyard is offered. This is old behaviour. `ReturnTargetCards…` resolves only CONTROLLERS or ALL scopes. Sure of the code.
- Rose Room Treasurer (fold run 46) queues its reflexive without a may, so declining still targets (with X = 0). Fairly sure; low impact.
- The AI's value readers don't look inside `MayEffect` / `ConditionalEffect` / the reflexive ability (Localized Destruction's `KeywordGrantingEffect` view is now inside a `ConditionalEffect`).

**For the next run.** 46c: Errant Minion/Power Leak (prevent-N-of-that-damage rider, payer `targetPlayerDeclaredElsewhere()`), Vault 112 (play-now `chooseOne`, look for a second user).

### Step 9, fold run 47 (2026-11-24, parallel worktree)
2 of 4 batches. 14 absorbed, 1 survivor widened, 5 left `step-9` for 47b. `dyn-cost` and `combat-cost` not started. Ratchet 3,136 / 2,545 → 3,122 / 2,519. 16 card files rewritten, Oracle text checked for all 16, rulings for 9. CR 101.4, 118.3, 118.12, 118.12a, 603.12 and 608.2d checked.

| Batch | Result |
|---|---|
| punisher | 12 folded into `MayPayManaEffect.anyPlayerMay(payers, payment, prompt, ifAPlayerDoes, ifNoOneDoes)`. Damage payments: Vexing Devil, Longhorn Firebeast, Browbeat, Book Burning, Breaking Point, Barbarian Bully. Sacrifice payments: Desecration Demon, Clackbridge Troll, Gitrog, Innocent Traveler, Prowling Pangolin, Argothian Wurm, Shivan Wumpus. Draw payment: Development. 5 left: Phantasmagorian, Dash Hopes / Temporal Extortion and Brain Gorgers need a step that counters the triggering spell (`CounterSpellEffect` counters only its target); Reservoir Kraken needs a tap each acceptor picks for themselves; Worms of the Earth is an either-payment. |
| may-pay-fixed | 2 folded. Llanowar Sentinel is `MayPayManaEffect({1}{G}, SearchLibraryEffect(CardNamedPredicate, BATTLEFIELD))`. Tolarian Kraken is `MayPayManaEffect({1}, QueueReflexiveAbilityEffect(MayEffect(TapOrUntap…)))` (CR 603.12). |
| dyn-cost, combat-cost | Not started. dyn-cost touches the cost checks and payments of `AbilityActivationService` and `ActivatedAbilityExecutionService`, and the X flag in `CardViewFactory` (PayXEnergyCost). |

**The survivor.**
- `payment` is a step over `Subjects.acceptedPlayers()`, or another subject ("has you draw a card"). `PayerLoop` holds the remaining players, the accepted players and the picked permanents.
- Every player the payer names who could pay in full (CR 118.3) is asked in APNAP order (CR 101.4), and later players are still asked after one accepts.
- A sacrifice is picked as the player accepts: unasked when only that many could be sacrificed (CR 608.2d), else a pick prompt.
- After the last answer, the payment is made once for everyone who accepted: sacrifices as one departure step, damage as one event. Then "if a player does" follows, whatever the payment did (CR 118.12), else "if no one does" (CR 118.12a).
- The mana loop is unchanged.

**Behaviour changes.**
- Bully, Breaking Point, Book Burning ask every player (the old code stopped at the first acceptance).
- Demon, Troll, Gitrog, Wurm, Wumpus sacrifice together after every answer (Clackbridge and Innocent Traveler rulings; defect 449).
- The Devil's damage goes to all acceptors together.
- Sacrifice candidates are the same check for every card (`SacrificePermanentsEffectHandler.sacrificeCandidates`).
- Breaking Point's destroy and Innocent Traveler's transform are now real steps of the entry.
- The AI's board-wipe reader looks into `MayPayManaEffect`.

**Tests changed.** BookBurning, BreakingPoint and BarbarianBully (answer the second player; two renames). GravebaneZombie ×3, InnocentTraveler and LlanowarSentinel: log lines only. New: one per damage card, Clackbridge with three players, 2 handler unit tests. Ran 59 classes, 763 cases, 0 failures.

**Seen, not changed.**
- The mana any-player loop still stops at the first payer (Scarwood Bandits and others), unlike the punisher rulings. Sure of the code; low impact.
- Hard AI values every punisher offer at 0 and declines it (existing behaviour).
- `leave-battlefield-redesign.md` still names `AnyOpponentMayTakeDamageSacrificeSource`.

**For the next run.** 47b, in this order:
1. A step that counters the triggering spell (`CounterSpellEffect` over `triggeringSpell()`), then fold the three counter records with discard and life `canPay`.
2. Kraken's tap pick.
3. Worms.
4. dyn-cost.
5. combat-cost.

### Step 9, fold run 49 (2026-11-24, parallel worktree)
Group D cost heads. Ratchet 3,127 / 2,537 → 3,118 / 2,537. 13 card files rewritten; Oracle text checked for all 13; rulings checked for Gavi, Bruenor, Highway Reaver and Advancing the Spirit. CR checked: 118.3, 118.9, 601.2f, 601.2h, 602.2b, 701.68a, 701.68b, 702.6a, 702.29a, 702.42a, 702.42b, 702.84a, 702.120a.

| Batch | Result |
|---|---|
| free-ability-cost | 6 folded into the new `PayZeroRatherThanAbilityCostEffect(ActivatedAbilityKind, firstEachTurn)`. Fold run 42's cost statics can't carry it: they lower generic mana, and an alternative cost replaces the whole cost, coloured mana and Street Wraith's life included. Conditions are a `ConditionalEffect` around the record (`ControllerHasEnduringStory`, `ControllerTurn`, `MaxSpeed`, `CardsInHandAtLeast(7)`), read through `activeStaticEffects`. "First each turn" reads the existing per-player records (equip, power-up, unearth sets and `cardsCycledThisTurn`). Gavi's per-source store went. Its 4 capability interfaces are dead. |
| modal-cost | 0 folded, 3 left for 49b. The four per-mode costs are four payment mechanics: a mana suffix, hand discard picks, a multi-permanent sacrifice and a multi-permanent tap. Each has its own `ExtractedCosts` field, validator and payer, plus special cases in `ExileFreeCastQueueSupport` and `MultiPermanentChoiceHandlerService`. One record means an `EscalateCost(perModeCost, ESCALATE / ENTWINE)` and reshaping those fields across 4 services. Betrayal of Flesh and Solar Tide are Entwine (sacrifice lands). |
| blight-cost | 2 folded. `PutCounterOnControlledCreatureCost` takes a `DynamicAmount amount`, with `blight(n)` / `blightX()`; the int constructors stay. The spell path evaluates X; `PutCounterOnCreatureCostHandler` takes the activation's X. Blighted Nightmare's bound is the new `ActivatedAbility.withMaximumXValue`, the ability's version of `Card.setXValueCap`, checked in `validateActivationLegality`. `BlightXCostHandler` went. |
| sac-all-cost | 0 folded, 3 left for 49b. Soulblast's `sacrificeAllCreatures()` boolean also steers three StackEntry target branches (SpellCastingService ~7600-7626), `ExileFreeCastQueueSupport` 353/847 and `PermanentChoiceHandlerService` 456. 49b must find out why before it swaps the booleans for one `SacrificeAllMatchingPermanentsCost` field. Total power as X is the `trackSacrificedPower` kind. |
| cost-aliases | 1 folded: `SacrificeCreatureOrPayManaCost` (the legacy branches went). `SacrificePermanentOrDiscardCardCost` left for 49b: validation differs (both picks chosen: the survivor refuses, the member takes the sacrifice), the payers differ, and the free-cast readers (`ExileFreeCastQueueSupport` 803/1006/1031/1052, `MayCastHandlerService` 1421, `PermanentChoiceHandlerService` 455) and the AI read only one of the pair. Folding opens those free casts to Bone Shards, Minion Missile and Souls of the Lost, which needs tests. |
| ctrl-counters-cost | 1 folded (Ooze Flux, Retribution). The survivor's filter, auto-pay and zero rule match; its log line ("as a cost") and prompt win. |

Behaviour changes:
- A face-down Gavi or New Perspectives no longer makes cycling free (CR 708.2a).
- Gavi's {0} now also replaces a cycling cost with no mana in it, such as Street Wraith's life payment (CR 118.9).
- Blight on an activated ability refuses a creature that can't get -1/-1 counters (CR 701.68b).
- Bayou Groff and Spark Harvest can be cast from exile by paying the mana option; before, the cast was refused.
- The free-ability readers now also skip a permanent that lost its non-mana abilities (not reachable by any card found).

Cost-shape: every payment stays where it was. Step 10's cost-combinator merge would join the blight payers (spell, ability, forced) and keep `paysZeroRatherThanAbilityCost` as the one alternative-activation-cost source.

Seen, not changed:
- With a {0} equip or power-up cost, `getActivatedAbilityAdditionalGenericCost` returns the tax without applying cost reductions to it (CR 601.2f). Fairly sure of the code, not tested.
- Hand-ability availability may not show a free cycle as playable without mana. Unverified.

For the next run: 49b takes `modal-cost` (EscalateCost plus the ExtractedCosts reshape), `sac-all-cost` (find out why Soulblast is excluded from the target branches first) and `SacrificePermanentOrDiscardCardCost` (match the validation and payment, then point the free-cast readers at the survivor).

### Step 9, fold run 24 (2026-11-24, parallel worktree)
- **counters-subject**: 2 of 4 folded.
- `PutCounterOnReturnedPermanentsEffect` → `PutCounterOnTargetPermanentEffect(returnedThisResolution(), type, n)` (Rise and Shine, Scout for Survivors, Smile at Death).
- `PutCountersOnCreatedPermanentsEffect` → `PutCounterOnTargetPermanentEffect(createdThisResolution(), type, amount)` (23 cards plus two engine inserts). The survivor counts a non-target amount against each named permanent, and I checked every amount. All are controller-, X- or entry-relative except Lattice Library's `CountersOnSource(STUDY)`. That one, and the paused `CreateXTokenWithXCountersEffect` insert (whose amounts include `CountersOnSource`), now note the amount first: `NoteAmountEffect` + `AmountThisWay.noted()`. The survivor's guards now apply to these cards: PUT_COUNTERS is recorded and the CR 616.1 replacement order is asked.
- Left for 24b:
  - `PutCountersOnChosenPermanentEffect`: the manifest-dread completion must record MANIFEST (the Rooms), Swallow Whole's spell tap cost must reach `chosenCostPermanentIds` (`paidAsCost()`), and Emergent Sequence moves to `searched()`.
  - `PutCounterOnChosenOwnPermanentEffect` (21 cards): `GrantSubtypeToChosenPermanentEffect`, `GrantKeywordToChosenCreatureUntilEndOfTurnEffect` and the Dragon handler must move to `thisWay(CHOOSE)` before `ChoosePermanentsEffect.exactly(1, …)` can replace it.
- **counter-adjust**: 0 folded, both `unique`. "Choose a counter" (one kind, one action) and "for each kind" are different prompt flows. Timebug's time-only, "may" and own-only target would be one-card options.
- **double-counters-set**: 2 folded. `DoubleCountersOnTargetPermanentEffect` takes any subject: `creaturesYouControl()`, `controlledBy(you(), filter)`. Placements now fire the "you put +1/+1 counters on another creature" and other-Hero triggers with the real count (CR 701.10e). The log is the survivor's, one line per permanent.
- **cant-have-counters**: 2 folded into `CantHaveCountersEffect(subject, counterType)` / `ofKind`. The three `GameQueryService` readers keep their asymmetry: Melira's grant is still read as a rule of its source. `cantHaveCounters` reads only the every-kind form.
- **lose-protection**: 0 folded, both `unique`. The from-colour option would be Cephalid Snitch's alone (guard rail).
- **enter-counters**: 0 folded, all 5 left for 24b. The mana-value, artifact-mana and Treasure-mana records need amounts evaluated against the entering permanent. Master Biomancer needs an entry-time subtype grant. Dearly Departed's closest survivor is `ControlledCreaturesEnterWithAdditionalCountersEffect` (subtype lookahead), not the queue's `ControlledPermanentsEnterWithAdditionalCountersEffect`, and a graveyard form would be a one-card option.
- **enter-untapped**: 1 folded, plus the unaudited 3-card `ControlledLandsEnterUntappedEffect`. Survivor is `ControlledPermanentsEnterUntappedEffect(filter, PlayerSubject players)` with `lands()` and `forEveryPlayer`. One reader, `applyEnterUntappedEffects`, handles the printed and `ConditionalReplacementEffect` forms, with players judged by `namesPlayer`. The `PermanentsEnterUntappedEffect` interface is gone.
- **Defect fixed**: Dearly Departed's reader now asks `graveyardCardsHaveLostAllAbilities`. New Yixlid Jailer test.
- **Seen, not changed**:
- Dearly Departed's reader still judges the printed creature type (CR 614.12).
- Clockspinning offers "skip", but its text has no "may".
- Quarry Hauler's and Dramatist's Puppet's removal never sends last-counter-removed (vanishing, CR 702.63a).
- Non-mandatory amass choices accept an empty answer (CR 701.47a).
- `resolveReplacementOrder` resumes later named permanents with the first permanent's count.
- **Ratchet**: 3,127 / 2,537 → 3,119 / 2,533.
- **For the next run**: 24b. Record MANIFEST in the manifest-dread completion, then fold the chosen-counter pair, then enter-counters with an entering-permanent amount context.

### Step 9, fold run 7b (2026-11-24, parallel worktree)
Batch `delayed-leads`: 2 records, 16 cards, both folded. Ratchet 3,117 / 2,525 → 3,115 / 2,525 handlers 2,527 → 2,525. Ledger 4 rows (absorbed 2, survivor 1, blocked-other 1 re-noted). 16 card files rewritten, Oracle text checked for all 16; rulings for the Locust God, Tiana, Prized Amalgam, Ivory Gargoyle, Agyrem, Phytotitan (Breathkeeper Seraph, Nine-Lives Familiar: none). CR 110.2a, 400.7, 500.8, 503.1a, 603.3a, 603.4, 603.7, 603.7a–e, 608.2h checked.

| Batch | Result |
|---|---|
| `delayed-leads` | `RegisterDelayedSelfReturnFromGraveyardEffect` (7 cards):<br>• next end step: Ivory Gargoyle and Molten Firebird (before `SkipNextEffect`), Prized Amalgam tapped, Sand Golem with a +1/+1 counter<br>• `upkeep(YOU)`: Breathkeeper Seraph<br>• `upkeep(CHOSEN_PLAYER).ofThatPlayer(ownersOf(sourceCard()))`: Phytotitan<br>• Nine-Lives Familiar: `NoteAmountEffect(Sum(CountersOnTriggeringPermanent(REVIVAL), -1))` then the return with `AmountThisWay.noted()`<br>`RegisterDelayedReturnCardFromGraveyardToHandEffect` (9 cards):<br>• "that card" via `triggeringCards()`: Liesa, Rienne, Agyrem, Tiana (`MayEffect` inside the delayed ability, as before)<br>• "it / this card" via `sourceCard()`: three HOU Gods, Mangara's Blessing, Pure Intentions |

**Where the triage note needed more.**
- "Their next upkeep" (Phytotitan) is the owner's next upkeep, whichever turn it falls in. `ofThatPlayersNextTurn` would skip an additional upkeep step this turn (`AdditionalUpkeepStepEffect` exists), so the survivor got `ofThatPlayer`: the existing `player` component without `afterThisTurn`. Its future users are the other `CHOSEN_PLAYER` registrars.
- "Your next upkeep" (Breathkeeper) needs no player: it is `upkeep(YOU)`, the delayed ability's controller.
- The counter amount needed `ReturnTriggeringCardFromGraveyardToBattlefieldEffect.counterAmount` widened to a `DynamicAmount`. This is not a new component; the `int` overloads are kept.

**Behaviour changes.**
- Nine-Lives Familiar returns under the delayed ability's controller (CR 110.2a, 603.7e). Its text has no "under its owner's control"; the old code used the owner. New test `stolenFamiliarReturnsUnderItsLastControllersControl` (fails on the old code by reading).
- A source card that left its graveyard and came back is no longer returned by the Gods, Mangara's Blessing or Pure Intentions (CR 400.7, 603.7c). The old hand handler searched every graveyard with no stay check when the entry had no triggering card.
- Registration and fizzle log lines are gone (the survivor logs nothing). Hand returns log `PutCardsIntoHandEffect`'s line. Tiana's prompt is "Return that card to its owner's hand?". No test asserted these.
- Self-returns now carry their maker's source permanent (CR 603.7e). Nothing observable.
- Phytotitan returns at an additional upkeep in the same turn (no test).

**Tests changed.** None. New: the Nine-Lives case.

**Tests run.** 55 classes: every test naming a rewritten card, a `aboutCardAtNext` / `ReturnTriggeringCardFromGraveyardToBattlefieldEffect` / `ReturnTargetCardOnDeathThisTurnEffect` card, `DelayedStepAbilityTest`, `StepTriggerServiceTest`, `CardEffectTargetingConsistencyTest`, `TargetPolarityGuardTest` and `EffectCountRatchetTest`. Result: 61 XML files, 658 cases, 0 failures. The deleted handlers asked no death replacements first, and no `src/test` tree named the deleted records.

**Seen, not changed.**
- Breathkeeper Seraph asks its "you may" as the dies trigger resolves. The text "you may return it … at the beginning of your next upkeep" has the template Tiana's ruling reads as a choice made when the delayed ability resolves. Breathkeeper has no ruling of its own. 9 test flows answer the may at death. Fairly sure.

**For the next run.** `ofThatPlayer` is ready for the `CHOSEN_PLAYER` registrars (`RegisterPoisonAtNextUpkeepUnlessPaysEffect`, `RegisterLoseLifeAtNextDrawStepUnlessPaysEffect`, `ExileTargetSpellWithDelayCountersEffect`).

### Step 9, fold run 30b (2026-11-24, parallel worktree)
Three batches touched: 4 absorbed, 3 survivor, 4 unique, 1 blocked-other; 5 spend-mana rows not started. Ratchet 3,117 / 2,526 is now 3,113 / 2,526 (records / handlers; was 3,117 / 2,527). 8 card files rewritten; Oracle text checked for all 8 and for 15 cards in all; rulings for Kayla and Urianger. CR 106.1b, 113.6b, 118.9a, 305.1, 400.7, 601.3, 609.4b, 611.2c, 701.17a, 702.34a checked.

| Batch | Result |
|---|---|
| exiled-with-src | 2 folded (Kayla's Music Box and the 4-card cast-all record), Urianger left `unique`. The survivor gains `alsoCardsExiledLaterThisTurn()`: a this-turn permission over `exiledWithSource(...)` floats an `ExilePlayGrantBySource` on the source, and `ExileService` applies it to each card exiled with the source later that turn (CR 611.2c). "Those cards" is `cardsThisWay(EXILE)`, which needed EXILE recorded by `ExileTopCardsToSourceEffect` and the exile-with-source search. Urianger's handler uses the same store. |
| gy-statics | Raul folded: `CastSpellFromGraveyardOncePerYourTurnEffect(new CardMilledThisTurnPredicate())`. The blocker was only that `findFilteredGraveyardPermission` passed the caster as owner; it now passes the graveyard owner. The emblem branch and a dead matcher were the null-GameData readers. Ninja Teen (granted sneak keyword), Eye of Duskmantle (only life alternative) and Coram (only all-graveyards scope) are `unique` under the guard rail. Jaya's emblem is `blocked-other`: exile-instead would be a one-card option, and emblem permissions have no filtered reader. |
| spend-mana | 2 folded: `SpendAnyManaTypeToCastEffect(CardPredicate spellFilter)` (Case: `CardSubtypePredicate(CASE)`; Nathan: `allOf(not(LAND), CardControllerDoesNotOwnPredicate)`); `AnyManaTypeCastEffect` deleted. Not started: Quicksilver, `SpendManaAsAnyColorEffect` + False Dawn + Sunglasses, Nathan's activated-ability record. |

**Behaviour changes.**
- Kayla's Music Box lets you play only cards you own (Oracle "cards you own"; new test, fails on the old code).
- Kayla's and Narfi's grant covers cards exiled with the source later that turn (CR 611.2c; new Kayla test, fails on the old code).
- `ExileTopCardsToSourceEffect` and `SearchLibraryForCardsToExileWithSourceEffect` record EXILE; none of their users reads an earlier EXILE step.
- Ugin's −11 gives free play permissions instead of single-use cast grants (filter is nonland: same plays).

**Seen, not changed.**
- King Narfi's Betrayal says "as though it were mana of any color" but uses any-type spending, so coloured mana pays {C} (CR 609.4b). Same family as Whispersteel/Xanathar. Fairly sure.
- Every `SpendAnyManaTypeToCastEffect` user pays as any colour, so Vizier's "mana of any type" can't pay {C} with coloured mana. This is the open spend-mana defect; it needs the payment-side change first. Fairly sure.
- Jaya's emblem still grants flashback (an alternative cost, CR 702.34a, 118.9a). Open.

**For the next run.**
1. 30c spend-mana: fold False Dawn into `GrantStaticEffectToPlayerEffect.untilEndOfTurn` over a from-white `SpendManaAsAnyColorEffect`; weigh Sunglasses' as-red set (one card). For Quicksilver's blue-only and Nathan's non-owned activated abilities, `controlledBy(you(), not owned)` and dropping the creature gate need a reader change. Build the any-type mode only together with the {C} payment fix.
2. Jaya: add emblems to `findFilteredGraveyardPermission` if a second exile-instead user of `CastSpellsFromGraveyardEffect` appears.

### Step 9, fold run 6c (2026-11-24, parallel worktree)
`colour-grants` done (2 absorbed); `returned-reflexive` left (2 `blocked-other`). Ratchet 3,103 / 2,501 → 3,101 / 2,499. 3 card files rewritten; Oracle text and rulings checked for all 3, and for Back for More and Jailbreak. CR 105.4, 509.1b, 603.7d, 603.7e, 603.12, 603.12a, 608.2h, 611.2c, 613.1f, 613.7, 702.11d and 702.11e checked with `get_rule`.

| Batch | Result |
|---|---|
| colour-grants | Mondo Gecko, Skrelv and Sungold Sentinel use the choose step, then readers, then the clear step. Hexproof is `GrantControllerAndPermanentsHexproofFromColorsEffect.chosenColor(target() / source())`. Evasion is `MakeCreatureBlockableOnlyByFilterThisTurnEffect(not(PermanentHasChosenSpellColorPredicate))`, bound to the chosen colour by `bindChosenValues`. Mondo's colour is `SetChosenColorUntilEndOfTurnEffect(false, false)`, which applies a colour already chosen and asks nothing. That adds no parameter; `SetTargetColorEffect` reads a chosen colour the same way. The two choice contexts and their `ChoiceHandlerService` branches are gone. |
| returned-reflexive | Left. Back for More would be the only user of an about-returned reflexive or a fighter subject. Jailbreak is an enters trigger made by the spell, not a reflexive. Torpor Orb and Hushbringer rulings judge the permanent as it exists. |

**Where the triage was wrong.** Jailbreak was batched as a reflexive; it is a delayed enters trigger (its own ruling; CR 603.7d). Mondo's colour step needed no colour-reading option on `GrantColorEffect`.

**Behaviour changes.** Mondo Gecko and Sungold Sentinel now ask for a colour when their source has left before resolution, and apply nothing. Skrelv still fizzles when its target is illegal. Log lines are the survivors'. The evasion description doesn't name the colour.

**Shared engine.** `GrantControllerAndPermanentsHexproofFromColorsEffect(colors, controllerToo, permanents, ofChosenColor)`: Veil of Summer goes through `permanentsYouControl()`, the same loop as before. `bindChosenValues` gains the `PermanentHasChosenSpellColorPredicate` leaf. `SetChosenColorUntilEndOfTurnEffectHandler.apply` is now shared with `handleColorSetChoice`.

**Tests changed.** None changed. Added: two `bindChosenValues` tests (fail on the old code: the leaf was unbound and matched nothing once the colour was cleared), Skrelv's five-colour prompt, and Sungold with two activations of different colours (coverage, CR 608.2h). Ran 48 classes, 0 failures after correcting my own test's blocker colour.

**Seen, not changed.**
- Hexproof-from-colour grants from Veil of Summer, Mondo Gecko, Skrelv and Sungold Sentinel are floating effects outside layer 6. A later "loses all abilities" doesn't remove them (CR 613.1f). Sure of the code; moderate impact.
- Jailbreak's ability uses the returned permanent as its source; CR 603.7d says the source is the spell. Its suppression check reads the card, not the permanent as it exists (Torpor Orb ruling). Sure of the code.
- Whether Torpor Orb stops Back for More's "when you do" isn't settled by any ruling found.

**For the next run.** Re-triage `GrantProtectionFromChosenColorToPermanentsEffect` (Ward Sliver) against the new subject on `GrantControllerAndPermanentsHexproofFromColorsEffect`. The returned-reflexive rows wait for a third user of an "enters during this resolution" trigger.

### Step 9, fold run 46c (2026-11-24, parallel worktree)
`pay-any` (last two): 1 absorbed, 1 unique. Ratchet 3,094 / 2,501 → 3,093 / 2,500. 1 card file rewritten; Oracle text checked for all 3 batch cards, rulings for 3 (Errant Minion and Power Leak have none). CR 107.14, 118.12, 305.1, 305.2, 305.3, 608.2g, 615.1, 615.1a, 616.1 checked.

| Batch | Result |
|---|---|
| Vault 112 | Absorbed into `byYou(ENERGY)` + `ConditionalEffect(HappenedThisWay(PAY_ENERGY), SequenceEffect.of(ShuffleLibraryEffect(you()), ExileTopCardsAndMayCastSpellsEffect.controllerMayPlayUpTo(energyPaid(), 1)))`. The triage note's play-now option on `ExileTopCardMayPlayThisTurnEffect.chooseOne` wasn't needed: the survivor's `playLands` + `maxCastCount` (Gix's path) plays the chosen card during resolution, or none. Lands are played only on your turn with a land play left (CR 305.2, 305.3); spells are cast without priority (CR 608.2g). It is the same free-cast queue the old handler used. |
| Errant Minion / Power Leak | `unique`. They are the only one-shot "deals N damage. Prevent X of that damage" text in the card data. Every other "prevent N of that damage" is a static shield (`PreventDamageToRecipientsEffect`). A floated shield would outlive the event and sit on the Aura's controller (CR 615.1). With no second user, the damage-step rider stays out. |

**Removed.** `ExiledCardMayPlayChoice.allowNoChoice` / `castChosenImmediately`. Only Vault set them, and its 6-argument constructor set the second one implicitly for free until-end-of-turn choices. The prompt now always requires one pick, and its interaction handler loses the decline and cast-now branches.

**Behaviour changes.** None in rules behaviour. Vault's prompt is the survivor's ("You may play one of the exiled cards without paying its mana cost."; it was "play until the end of this turn"), and the log lines are the survivors'.

**Tests.** Vault: prompt class updated, `@DisplayName` on all tests, +2 tests (one play only; a land stays exiled without a land play), coverage not failing-first. `SpecializedInteractionProjectionTest`: the optional-play bounds use the new prompt, plus a must-pick check. Ran 21 classes, 268 cases, 0 failures.

**Seen, not changed.**
- Errant Minion / Power Leak deal `2 - X` instead of preventing X. No damage-prevented event is sent, and the affected player can't order it among other prevention or replacement effects (CR 615.1, 616.1). Sure of the code; low impact.
- Their payment drains the pool with `ManaCost("{0}").pay` instead of `ManaCost.payResolutionCost`, so restricted mana (Powerstone) may be judged differently from the survivor. Unsure.
- `ExiledCardMayPlayChoiceInteractionHandler` injects an unused `ExileSupport`. Sure; cosmetic.

**For the next run.** `pay-any` is closed. Errant Minion / Power Leak fold only if a second one-shot "prevent N of that damage" card appears.

### Step 9, fold run 47b (2026-11-24, parallel worktree)
2 of 3 batches. 7 absorbed, 4 survivors widened, 3 `blocked-other`, 3 left `step-9` for 47c (`dyn-cost`, not started). Ratchet 3,094 / 2,501 → 3,087 / 2,493. 8 card files rewritten, Oracle text checked for all 8, rulings for 8 (and Worms).

| Batch | Result |
|---|---|
| punisher | 4 folded. Phantasmagorian, Dash Hopes / Temporal Extortion and Brain Gorgers use `anyPlayerMay(...).payingAsEachAccepts()` with `CounterSpellEffect.triggeringSpell()`. Reservoir Kraken uses a tap payment that each acceptor picks. Worms left `blocked-other`: the acceptor chooses between two payments, there is one user, and it belongs to step 10's either-costs. |
| combat-cost | 3 folded into `CantAttackOrBlockUnlessPaysEffect(amount, Side, attackerFilter)`: Marauder `.attacking(CountersOnSource)`, Myr Prototype, Hipparion `.blocking(Fixed(1), PowerAtLeast(3))`. Floodtide Serpent and Hollow Warrior left `blocked-other`: non-mana costs paid by `AttackReturnToHandCostService` / `CombatTapCostService`, one user each. |
| dyn-cost | Not started. `PayLifeCost`'s domain `effectiveAmount` / `lifePaid` readers (AI, spells, kicker, forced costs) need an engine-evaluated amount. |

**The survivors.**
- `paysAsEachAccepts`: an accepting player pays before the next player is asked. "If a player does" follows the first payment only, and `PayerLoop.anyPaid` keeps "if no one does" from running after a payment. PLC rulings: "as soon as any player performs the action, the spell is countered, but the remaining players still get the option".
- Payments now accepted:
- `TapPermanentsEffect(controlledBy(acceptedPlayers(), f), null, n)`: picked on acceptance, recorded as CHOOSE, then tapped with `TapPermanentsEffect(thisWay(CHOOSE))`.
- `DiscardEffect`: needs that many cards in hand (ruling).
- `EachPlayerPaysAnyManaEffect.lifeExactly`: life at least the amount, the player can lose life, and life can be paid for costs (CR 119.4, 119.8).
- `CounterSpellEffect` gains a spell subject. `triggeringSpell()` declares no target, excludes the trigger's own entry, and does nothing if the spell left the stack (CR 701.6a, 608.2b).
- A block cost with no attacker filter is paid once per blocking creature; with a filter, once per matching attacker blocked.

**Behaviour changes.**
- Brain Gorgers: each sacrifice is made as its player accepts (ruling). Before, it waited for every answer.
- Kraken: taps are made together after the last answer (CR 101.4). A Kraken destroyed in response still asks, and still gives a Fish, because the intervening "if" reads its last known state (CR 113.7a, 603.4, 608.2h). This reverts the test flip in `74811db8fd`.
- Myr Prototype: blocking two attackers pays its cost once.

**Tests changed.** BrainGorgers 1 rewritten (ruling). GravebaneZombie: 2 log lines, 1 rewritten (the death-replacement question now comes at acceptance). Kraken: 1 rewritten. New tests: Kraken with three players, Myr blocking two attackers, 2 handler cases each for `MayPayManaEffect` and `CounterSpellEffect`. Ran 73 classes; the last run had 0 failures.

**Cost-shape.** Every payment stays where it was. Step 10's cost-combinator merge would take the tap, discard and life payments of `anyPlayerMay`, the combat return and tap costs, and Worms' either-cost.

**Seen, not changed.**
- A Temporal Extortion payer whose half life is 0 is not asked (amount > 0 kept), although CR 119.4 allows paying 0 life. Low impact (needs a player at 0 life who hasn't lost).
- Kraken's record was a `TokenCreatingEffect`; the AI's token reader no longer sees the Fish inside `MayPayManaEffect`. Low impact.
- `ReservoirKrakenTest` has 5 tests with no `@DisplayName`.

**For the next run.** 47c: `dyn-cost`, a `DynamicAmount` on `PayEnergyCost` / `PayLifeCost`:
- move `effectiveAmount` / `lifePaid` / `paysLifeForEachCommanderColorIdentity` to engine evaluation;
- `AbilityActivationService` ~1636–1665 and ~7105–7330;
- `ActivatedAbilityExecutionService` ~406 / 782–830;
- the X flag in `CardViewFactory` ~526.

### Step 9, fold run 24b (2026-11-24, parallel worktree)
- **counters-subject**: 2 of 2 folded (plus Widespread Brutality's record from fold run 26).
- `PutCountersOnChosenPermanentEffect` → `PutCounterOnTargetPermanentEffect` over three subjects:
  - the Rooms use `thisWay(MANIFEST)`: `ManifestDreadEffectHandler` records NONE as it begins, and `resolveManifestChoice` records the manifested permanent;
  - Swallow Whole uses `paidAsCost()`: `AdditionalCostPayment.tappedPermanentIds` now reach `chosenCostPermanentIds`;
  - Emergent Sequence uses `searched()`.
- `CopySupport.createCopyStackEntry` now copies `chosenCostPermanentIds` (CR 707.10). Before this, a Twincast copy of Swallow Whole put no counter. New test.
- `PutCounterOnChosenOwnPermanentEffect` (21 cards) → `ChoosePermanentsEffect.exactly(1, controlledBy(you(), f), label)` + `PutCounterOnTargetPermanentEffect(thisWay(CHOOSE), …)`. Its followers moved too:
  - `GrantSubtypeEffect(subtype, thisWay(CHOOSE))`; its resolution handler now accepts `thisWay`;
  - `GrantKeywordEffect(k, thisWay(CHOOSE), UNTIL_END_OF_TURN)` for Kayla's Command, My Followers Ascend and Season of Gathering;
  - Call the Spirit Dragons' handler, now one color at a time: choose, counter, then tally the distinct Dragons from PUT_COUNTERS.
- Every choice is now mandatory. 12 cards accepted an empty answer: CR 701.47a for amass, the Oracle "a creature you control" for the rest. Failing-first tests on Eternal Skylord and Contractual Safeguard.
- The survivor's guards now apply to these cards: PUT_COUNTERS is recorded and the CR 616.1 order is asked. The AI's `favorsChosen` sees +1/+1 counters on `thisWay(CHOOSE)`.
- Prompt-shape test changes in 21 classes: the counter choice is now a `PermanentChoice`.
- Widespread Brutality → `TargetCreatureDealsPowerDamageToEachOtherCreatureEffect(thisWay(CHOOSE), controlledBy(eachPlayer(), non-Army creature))`. The Army is the damage source, with the protection check. This fixes the queue's line 512 defect.
- `GrantSubtypeToChosenPermanentEffect`: no card uses it. Only `CompleteAmassGoblinsEffectHandler`'s bespoke choice still inserts it (blocked-step-model).
- **enter-counters**: 0 of 5, not reached; ledger rows say how each folds.
- **Seen, not changed**:
  - Thunderous Velocipede reads the printed card's mana value, so a face-down entering creature gets three counters instead of one (CR 708.2a). Likely.
  - Dearly Departed's reader and `ControlledCreaturesEnterWithAdditionalCountersEffect`'s `allCreatures` path judge creature-ness from the printed card type (CR 614.12). Sure of the code.
  - `CopyControllerActivatedAbilityEffectHandler` copies `chosenPermanentId` itself; it now gets `chosenCostPermanentIds` through `CopySupport`.
- **Ratchet**: 3,086 / 2,497 → 3,083 / 2,494.
- **For the next run**: 24c, enter-counters. Build an "amount evaluated against the entering permanent" option on `ControlledPermanentsEnterWithAdditionalCountersByAmountEffect`; three records need it. Read the face-down mana value. Decide Master Biomancer (layer-4 "as a Mutant" entry grant: unique unless a second user exists). Fix the CR 614.12 creature-ness read for Dearly Departed and the survivor.

### Step 9, fold run 30c (2026-11-24, parallel worktree)
`spend-mana` closed: 3 absorbed, 1 dead interface, 1 unique, 3 survivor rows. Ratchet 3,080 / 2,494 is now 3,077 / 2,493. 7 card files rewritten; Oracle text checked for all of them (plus Vizier, Sunglasses, Lattice, Orrery, Mnemonic Betrayal, Thought-Knot Seer). Rulings read for Quicksilver and False Dawn. CR 106.1b, 107.4c, 609.4b checked.

| Batch | Result |
|---|---|
| spend-mana | Quicksilver → `SpendManaAsAnyColorForActivatedAbilitiesEffect.blueManaForOwnAbilities()`; Nathan's activated record → the same survivor over `controlledBy(you(), not(PermanentOwnedBySourceControllerPredicate))`; False Dawn → `GrantStaticEffectToPlayerEffect.untilEndOfTurn(SpendManaAsAnyColorEffect.whiteMana())`; `ActivatedAbilityManaColorPermissionEffect` deleted. Sunglasses `unique`: as-red would be a one-card option. |

**Shared pieces.** `fromColor` on both spend-as-any-colour records (null = all mana; WHITE on the player record, BLUE on the source's own abilities, both validated). `SpendAnyManaTypeToCastEffect.asThoughAnyColor(filter)`, read by `CastingPermissionService.canSpendManaAsAnyColorToCastFromBattlefield`. The static one-permanent subject form now reads `PermanentOwnedBySourceControllerPredicate` (before, it was always false).

**Behaviour changes.**
- Oath of Nissa, Case File Auditor and Nathan pay "as though any color": coloured mana no longer pays {C} (CR 107.4c). Failing-first: the old code cast an opponent-owned Thought-Knot Seer with GGGG under Nathan (probe; new test).
- The activated-ability subject path lost its creature gate. Nathan's non-owned noncreature permanents still work (Pyrite Spellbomb test); `creaturesYouControl()` still judges creature-ness.
- False Dawn's own log line goes (the survivor's wins).

**Defects.**
- Fixed: the battlefield any-colour cast permissions paid {C} like any type.
- Not reproduced: Vizier (hand and library top) and North Star do pay {C} with green mana (probe). King Narfi's Betrayal can't pay {C} with green: its exile cast uses the any-colour path at `SpellCastingService` (`anyColorForColoredSymbols`).
- Seen, not changed: that same exile-path code turns every `withAnyManaType()` / `spendingManaOfAnyType()` permission into any colour when the cost has {C}. So "mana of any type" exile and graveyard permissions (Mnemonic Betrayal, verified with a probe: GGGG can't cast an exiled Thought-Knot Seer) can't pay {C} with coloured mana (CR 106.1b, 609.4b). Narfi, The Ruinous Powers and Whispersteel are right only by accident. The fix needs an any-type / any-colour mode on the exile and graveyard permission records, read at that payment site (about 30 cards). Sure.

**Tests changed.** `CaseFileAuditorTest` service assertion → behaviour test (red mana casts a {W} Case).

**For the next run.**
1. A defect run: split `AllowPlayCardsFromExileEffect.withAnyManaType()` / `AllowCastMatchingCardsFromGraveyardThisTurnEffect.spendingManaOfAnyType()` into any type and any colour per Oracle text, and make the exile payment's `anyColorForColoredSymbols` apply to any colour only.

### Step 9, fold run 49b (2026-11-24, parallel worktree)
Ratchet goes from 3,080 / 2,494 to 3,074 / 2,494: 7 records absorbed, 1 new, no handlers deleted. 38 card files were rewritten, with Oracle text checked for all 38 plus the three free-cast test cards. CR rules checked: 118.3, 118.9, 118.9d, 601.2f, 601.2h, 603.10a, 616.1, 702.42a, 702.42b, 702.120a.

| Batch | Result |
|---|---|
| modal-cost | 3 folded, plus `EscalateManaCost` (needed for one field), into the new `EscalateCost(CostEffect perModeCost, Kind ESCALATE \| ENTWINE)`. One payment is `PayManaCost`, `DiscardCardTypeCost(null, null)`, `SacrificeMultiplePermanentsCost(n, f)` or `TapMultiplePermanentsCost(1, f)`. `AdditionalSpellCostService.escalatePayments(card, cost, modes)` counts payments: ESCALATE pays once per mode beyond the first, ENTWINE once for all modes and rejects an in-between count. The four `ExtractedCosts` fields became one; validators and payers stayed where they were. `consumedPermanentFilter` delegates to the per-mode cost, so the AI sees what it did before. The 24 Mirrodin-block/MH1 mana cards and Betrayal of Flesh / Solar Tide are entwine. With two modes the payment is the same, and the log now says "entwine". |
| sac-all-cost | 2 folded into `SacrificeAllMatchingPermanentsCost(filter, trackSacrificedPower)`, which is now also a spell cost. The Soulblast guard on the three StackEntry multi-target branches dates from Soulblast's first implementation (a955e96b6c) and has no rule behind it: it makes a single-target spell ignore a stray `targetIds` list. It now covers any sacrifice-all cost. The free-cast readers (`ExileFreeCastQueueSupport`, `askFreeCastSacrificeAllFirst`, `paysOnlySacrificeCosts`) use the cost's filter. Kaervek's Spite stays refused in exile free casts because it has two costs. |
| cost-aliases | `SacrificePermanentOrDiscardCardCost` folded into `DiscardCardOrSacrificePermanentCost`. The survivor's validation refuses both picks (Bone Shards ruling: exactly one) and records the sacrificed card. The free-cast readers, Jetsam's refusal list and the AI now read the survivor. |

Behaviour changes:
- Exile free casts pay Bone Shards', Minion Missile's and Souls of the Lost's sacrifice-or-discard cost; before, the cast was skipped (CR 118.9d; 3 Hazoret tests).
- Jetsam refuses Bone Shards instead of casting it with the cost skipped (CR 601.2h; Flotsam // Jetsam test).
- Demand Answers refuses a request naming both a sacrifice and a discard (Demand Answers test).
- The escalate tap log line has its missing space (Collective Effort test).
- The graveyard-cast refusal keeps Collective Effort's tap escalate allowed, as before.

Cost-shape: payments stay where they were. Step 10's cost-combinator merge would join the escalate payers with the `SacrificeMultiplePermanentsCost` / `TapMultiplePermanentsCost` / discard payers they wrap. It would also join `paySacrificeAllMatchingPermanentsCost` with `AllMatchingPermanentSacrificeCostHandler`.

Seen, not changed:
- The escalate/entwine sacrifice payer and `payMultipleSacrificeCost` sacrifice one permanent at a time, so Betrayal of Flesh's three lands leave as three separate events (CR 603.10a). Fairly sure of the code.
- `EntwineManaCost` (Kaya's Guile) is refused in exile free casts, because it counts as a non-escalate cost. Sure of the code.

For the next run: fold `EntwineManaCost` (5 cards) onto `EscalateCost.entwine` once the free-cast mode picker can express "the required number or all modes".

### Step 9, fold run 47c (2026-11-24, parallel worktree)
1 batch, 3 folded, 0 left. Ratchet 3,070 / 2,483 → 3,067 / 2,483. 4 card files rewritten; Oracle text and rulings checked for all 4. CR checked: 107.3a, 107.14, 118.3, 119.4, 119.8, 601.2f, 602.2b, 903.4, 903.4a, 903.4f.

| Batch | Result |
|---|---|
| dyn-cost | 3 folded. `PayLifeCost(DynamicAmount amount)`: the int constructor stays, `halfLife()` is `HalfControllerLifeRoundedUp`, `perSourceCounter(n, t)` is `Scaled(CountersOnSource(t), n)`. Hand of Vecna uses `CardsInHand(CONTROLLER)`, War Room the new `ColorsInCommandersColorIdentity`. `PayEnergyCost(DynamicAmount amount)` with `x()` for HELIOS One and Sphinx. `CardViewFactory`'s X-from-energy flag reads `PayEnergyCost` with an `XValue` amount. |

**How it is evaluated.** The domain `effectiveAmount` and the `lifePaid` override are gone. The activation paths (battlefield, mana-ability additional costs, graveyard, hand), the forced-cost paths and the AI evaluate the amount with `AmountEvaluationService.evaluateCost(gameData, amount, payer, source, x)`, which is fixed as the total cost is determined (CR 601.2f, 602.2b). The spell paths (additional cast cost, kicker, buyback) and the energy forced and alternative-spell paths take `fixedAmount()`, which throws when the amount is not fixed. No card puts a non-fixed amount there; fold run 49 did the same. `AlternativeSpellCost.amount()` is now `fixedAmount()`.

**Behaviour changes.**
- War Room can't be activated without a commander (CR 903.4f, ruling). `isUnpayableCost` reports it; the old test asserted the opposite and is rewritten.
- Hand of Vecna and War Room are refused while the life total can't change (CR 119.8): the survivor's guard now covers them.
- AI life-cost reads now count Tornado's counters, the hand size and the colour count (before: 0).

**Tests.** WarRoom: 1 rewritten, 2 added, `@DisplayName` added to 5. HandOfVecna: 2 added. CardViewFactory: 1 added. The AAS / AAE unit tests moved onto `PayLifeCost(CardsInHand)`. Ran 293 classes (3,581 cases); 1 stub failure was fixed and its class rerun (95 cases, 0 failures).

**Cost-shape.** Every payment stays where it was. Step 10's cost-combinator merge would join the `PayLifeCost` payers (battlefield, mana-ability additional, graveyard, hand, spell, kicker, buyback, forced) into one payer on `evaluateCost` and drop `fixedAmount()` from the spell paths.

**Seen, not changed.**
- Battlefield and hand `PayLifeCost` validation checks `canPlayerLifeChange` but not `canPlayerLoseLife` (`PlayersCantLoseLifeThisTurnEffect`); the graveyard path checks both (CR 119.8). Fairly sure from the code; not tested.

**For the next run.** `PayXLifeCost` (8 cards, unaudited) folds onto `PayLifeCost(new XValue())`; its spell path must then call `evaluateCost` with the announced X instead of `fixedAmount()`.

### Step 9, fold run 24c (2026-11-24, parallel worktree)
- **enter-counters**: 3 of 5 folded, batch closed.
- New option `ControlledPermanentsEnterWithAdditionalCountersByAmountEffect.ofEnteringPermanent(predicate, amount)` (`readsEnteringPermanent`). It evaluates the amount with `AmountContext.forEnteringPermanent`, so the entering permanent is the amount's source. The handler reads it through `ControlledPermanentEntryReplacementEffect.amountReadsEnteringPermanent()`.
- Velocipede uses `FixedIfCondition(SourceMatchesPermanentPredicate(PermanentMaxManaValuePredicate(4)), 1, 3)`. The predicate reads `getPermanentManaValue`, which is 0 face down.
- Coin of Mastery uses the new `ArtifactManaSpentToCast` (same snapshot as before, Treasures included).
- Kalain uses `TreasureManaSpentToCast` (same read by the entering card's id).
- Master Biomancer stays `unique`: the "as a Mutant in addition" half is a layer-4 grant from the replacement. No other card in the 730 cached sets says it (guard rail). Splitting off the counter half would keep the record and cut one replacement in two.
- Dearly Departed stays `unique` (the graveyard form is one card's, guard rail).
- **Defects fixed** (each test failed on the old code in a run):
- Thunderous Velocipede read the printed mana value: a face-down Akroma got 3 counters, now 1 (CR 202.3a, 708.2a).
- `ControlledCreaturesEnterWithAdditionalCountersEffect`'s `allCreatures` path and Dearly Departed's reader checked whether the printed card was a creature. Both now use `isCreatureAsEntering`, which looks at the permanent as it would exist on the battlefield (CR 614.12, Dearly Departed's ruling). Mind Stone under March of the Machines now gets Arwen's counters.
- `resolveEnteringSubtypes` gave a face-down permanent its card's subtypes and changeling. It now uses `getFaceDownSubtypes()` and no changeling (CR 708.2a): a face-down Daru Lancer no longer gets Dearly Departed's counter. This applies to every subtype-counter card on the path.
- **Seen, not changed**:
- The subtype path of `ControlledCreaturesEnterWithAdditionalCountersEffect` never checks that the entering permanent is a creature. Sage of Fables, Bramblewood Paragon, Metallic Mimic and Oona's Blackguard say "creature"; Dragonstorm Globe, Giada and Vampire Socialite don't. A noncreature Kindred Wizard (Diviner's Wand) would get Sage's counter. Likely; untested.
- The per-card mana-spent snapshots (Treasure, artifact) are keyed by card id. A copy of a permanent spell, or a countered spell that is later put onto the battlefield, might read a stale value. Unverified.
- **Tests changed**: none. New: Velocipede face-down; Arwen animated artifact and plain artifact; Dearly Departed face-down Human, animated Kindred Human, plain Kindred Human.
- **Ratchet**: 3,067 / 2,480 → 3,064 / 2,480.
- **For the next run**: none in this batch. The creature check on the subtype path (above) needs a per-record "creature" flag; leave it to a defect run.

### Step 9, fold run 48 (2026-11-24, parallel worktree)
1 of 3 batches. 6 absorbed, 2 `blocked-other`, 3 left `step-9`; `attack-tax` and `alt-cost` not started. Ratchet 3,070 / 2,483 → 3,064 / 2,474. 6 card files rewritten; Oracle text and rulings checked for all 6. CR checked: 118.3, 118.12, 118.12a, 119.4, 119.8, 608.2d, 608.2h, 701.33b, 508.1h, 509.1d, 509.1e, 118.9, 118.9d, 101.4.

| Batch | Result |
|---|---|
| cost-shape-singles | 6 folded onto existing heads. Mishra's War Machine: `MayEffect(DiscardEffect, …, Sequence(damage 3 to you, ConditionalEffect(toAPlayer(DEAL_DAMAGE, SELF), tap source)))`. Archfiend: `MayEffect(SacrificePermanentsEffect(EventValue, any, controllerOf(triggering())), …, LoseLifeEffect(EventValue, same), TRIGGERING_PERMANENT_CONTROLLER)`, not the queue's `EachPlayerSacrificesPermanentOrLosesLifeEffect` (its player subject and count would be one-card options). Energy Vortex: `MayPayManaEffect.dynamic(CountersOnSource(VORTEX), …, TRIGGERING_PLAYER, damage to chosenPlayer())`. Fear My Authority: `MayEffect(discard, …, MayPayLifeEffect(3, …, SacrificeSelfEffect()))`. Delaying Shield: `Sequence(RemoveAllCounters(DELAY), RepeatEffect(EventValue, ForcedCostOrElse({1}{W}, lose 1)))`. Gorilla: the 8.20/8.22 may-sacrifice pattern. Left: Soul Barrier and Divert `blocked-other`; Magmasaur and the ante pair for 48b. |
| attack-tax, alt-cost | Not started. |

**Survivors.**
- `MayPayLifeEffect` doesn't ask a payer who can't pay (life below the cost, or can't lose life); the else effect follows (CR 119.4, 608.2d). 14 card tests answered a no-op prompt and now assert none is shown.
- `MayEffect` withholds a `controllerOf(...)` sacrifice of any count that its chooser can't make in full. Chain of Silence now gets this guard as a fix (CR 608.2d; 6 tests lost a no-op answer).

**Behaviour changes.**
- Fear: declining the discard offers the life payment, and both can be declined (CR 118.12a; defect fixed, failing-first).
- Mishra taps only if damage was dealt to you.
- Vortex's payment gets `MayPayManaEffect`'s life alternatives and the tax-paid event.
- Archfiend's choice is a yes/no question; the Gorilla's Forest pick is the multi-permanent sacrifice prompt.

**Cost-shape.** Every payment stays where it was. Step 10's cost-combinator merge would take: the unasked-when-unpayable rule for `MayPayManaEffect` (Soul Barrier); a target-spell-controller payer (Divert, with "counter unless" option C); Fear's two "may" costs as one either-cost (with Worms of the Earth).

**Seen, not changed.**
- `sourceHasKeyword` reads the trigger-time snapshot for a source sacrificed earlier in the same resolution, so lifelink from an Aura is lost (Gargantuan Gorilla, Bronze Bombshell, Elder Spawn; Magmasaur does it by hand). Sure of the code, not tested.
- `MayPayManaEffect` asks payers who can't pay (406 cards). Sure.
- `RemoveAllCountersEffect` removes counters with `setCounterCount`, outside the removal path (Delaying Shield, Energy Vortex). Code; low impact.
- `DefenderAttackLifeCostEffect` has no implementor (dead interface).

**For the next run.** 48b:
- attack-tax: floating `RequirePaymentToAttackEffect` / `RequirePaymentToBlockEffect` on a player needs the amount fixed at resolution (War Tax's and War Cadence's X); `GrantStaticEffectToPlayerEffect` fixes no `DynamicAmount`. The readers are `CastingCostService.getAttackPaymentPerCreature` (~2715) and `getAttackLifePaymentPerCreature`, and `GameQueryService.getGlobalBlockManaTax` / `getGlobalBlockLifeTax` (~11660). Fix the `getOpponentId` defender on the way.
- alt-cost: Dream Halls and web-slinging use their own flags in `SpellCastingService` (~3900–4760), `GameViewProjectionFactory` (~1232) and `CastingCostService` (~1788, ~2154).
- Then Magmasaur and the ante pair.

### Step 9, fold run 8c (2026-11-24, parallel worktree)
Batch `splits`, first half (11 rows): 5 split onto existing steps, 1 `blocked-other`, 5 not taken. Ratchet 3,067 / 2,480 → 3,062 / 2,475. CR 101.4, 104.1, 104.4a, 603.12, 608.2c, 608.2h, 701.34a checked. 5 card files rewritten, Oracle text checked for all.

| Folded | Into |
|---|---|
| Ripples of Potential | `ProliferateEffect` (a PUT_COUNTERS step now) + `ChoosePermanentsEffect.anyNumber(thisWay(PUT_COUNTERS, PermanentControlledBySourceControllerPredicate))` + `PhaseOutEffect(thisWay(CHOOSE))` |
| Decimator Beetle | two plain target groups + `addTrigger(attacks(), RemoveUpToCountersFromAllPermanentsEffect.exactly(-1/-1, 1, target()), PutCounterOnTargetPermanentEffect(-1/-1, 1))`, each bound by `registerEffectTargetIndex` |
| Vhal, Eager Scholar | `RemoveCounterFromSourceEffect(STUDY, MAX_VALUE)` + white/black/red `ConditionalEffect(removedCounters(), QueueReflexiveAbilityEffect.withEventValue(face, AmountThisWay(REMOVE_COUNTERS)))`, blue/green the face's step over `AmountThisWay(REMOVE_COUNTERS)` |
| Consuming Ferocity | `PutCounterOnTargetPermanentEffect(attachedCreature(), +1/+0)` + `ConditionalEffect(EnchantedPermanentMatches(PermanentCounterCountAtLeastPredicate(+1/+0, 3)), dealtBy(attachedCreature(), SourcePower, controllerOf(…)) + DestroyTargetPermanentEffect(attachedCreature(), true))` |
| Triskaidekaphobia | per mode `ControllerLosesGameEffect(matching(eachPlayer(), PlayerLifeExactlyPredicate(13)))` + `GainLifeEffect` / `LoseLifeEffect(1, eachPlayer())` |

**Where the triage was wrong.**
- Decimator: `PutCounterOnTargetPermanentEffect` ignores a `target(n)` group, and a `SequenceEffect` bound to one group lost the second group. The fix is two effects, each bound to its own group.
- Vhal: the blue and green faces have no "when you do" (HBG 8u, 8g). The old handler made a reflexive ability for every face, even with nothing removed.
- Triskaidekaphobia: the survivor ended the game at its first loser. It now resolves every loss before deciding the result.

**Behaviour changes.**
- Vhal: blue and green act in the specialize ability itself; the white, black and red reflexive abilities need counters to have been removed (CR 603.12). Black's target filter reads the event value. New tests.
- `ControllerLosesGameEffect`: simultaneous losses, and a draw when all remaining players lose (CR 104.4a).
- `EffectResolutionService` stops resolving once `gameResult` is set (CR 104.1). This is what keeps Triskaidekaphobia's life step from running after a loss, as the old handler did.
- Decimator's targets are now chosen by the generic stack door. Its counter removal fires the counter-removed events.
- Ripples' phase-out log line is the survivor's.

**Deleted.**
- The 5 records and handlers.
- `AttackCounterMoveEffect`, the `AttackCounterMoveFirstTarget` / `SecondTarget` contexts, their queue pump, handlers and `TargetLegalityService` branch, and `targetableCreaturesControlledBy`.
- `MultiPermanentChoiceContext.PhaseOutPermanentsThatReceivedCountersThisWay` and `StackEntry.counteredPermanentIdsThisResolution`.
- The dead `recordPlacement` component of `OwnPermanentCounterPlacementWithChosenReference`.

**Blocked.** Necropotence: the AI reads `CardDrawingEffect` to value an activated ability's draw, and neither replacement step has it.

**For the next run (8d).**
- Unexpected Results and Rashmi share one missing piece. `LookAtTopCardsAndMayCastEffect.reveal(1).matching(…).leavingTheRest()` would need to record REVEAL, and its `CAST_WITHOUT_PAYING` answer would need to resume the remaining steps. The second point is unverified: no card has a step after it.
- Separately, `LibrarySearchDestination.CAST_WITHOUT_PAYING_OR_PUT_INTO_HAND` already exists and fits Rashmi.
- After that, the land branch of Unexpected Results is `ConditionalEffect(toACard(REVEAL, land), MayEffect(put onto battlefield + return the spell))`, and Rashmi's is `PutCardsIntoHandEffect(revealedThisWay())`.
- Specter's Shriek, The Master and Danse Macabre were not read in depth.

### Step 9, fold run 7c (2026-11-24, parallel worktree)
The three `must-attack` rows fold run 7 left, plus the Ward Sliver re-triage. Ratchet 3,058 / 2,479 → 3,054 / 2,475 (4 records, 4 handlers; `TauntTarget` enum also deleted). Ledger 7 rows: absorbed 4, survivor 3. 6 card files rewritten; Oracle text checked for all 6; rulings read for Taunt, Gideon Jura, My Crushing Masterstroke, Slayer's Cleaver and Ward Sliver. CR 506.2, 508.1d, 509.1c, 608.2h, 611.2c, 613.1e, 613.1f, 702.16b–f and 301.5c checked.

| Batch | Result |
|---|---|
| must-attack (attacked object) | Taunt and Gideon Jura onto `MatchingCreaturesMustAttackThisTurnEffect.controlledBy(targetPlayer()).attacking(you() / source()).lasting(DURING_AFFECTED_PLAYERS_NEXT_TURN)`. New components: `attackedPlayer`, `attackedPermanent` and `Duration`. The next-turn form makes one float per named player, so creatures gained later are bound too (Gideon ruling, CR 611.2c). A player who is attacked is the player, not their planeswalkers. A planeswalker gone before resolution means nothing is required. My Crushing Masterstroke folds onto `SetCombatRequirementThisTurnEffect.mustAttack(target(), ownerOf(target()))`; the triage named the wrong survivor, which has no per-creature subject. Siblings checked and left as they are: Gideon, Battle-Forged (a target creature), Nahiri (a different payload), Silver Surfer, The Brothers' War (pairs players by index). |
| must-be-blocked filter | Slayer's Cleaver and Ace's Baseball Bat onto `MustBeBlockedIfAbleEffect(blockerFilter)`, keeping the no-arg constructor. One reader, `GameQueryService.mustBeBlockedRequirements`, covers floating effects, the attacker's own active statics (not an Equipment's own) and attached Auras/Equipment, including conditional wrappers. CombatBlockService keeps its unfiltered and filtered checks separate: a joint bipartite matching would undercount one blocker obeying two requirements on the same attacker, and the fallback search already weighs everything together. The AI readers now ask `obeysAMustBeBlockedRequirement`. |
| Ward Sliver re-triage | Folded onto `ProtectionFromColorsEffect.chosenColor(controlledBy(eachPlayer(), Sliver))`. The colour is judged for the source. `ProtectionFromColorsSelfEffectHandler` now protects the source when the set names it. Fold run 37b's Attached-only readers removed the old blocker. |

**Behaviour changes.**
- My Crushing Masterstroke: each creature attacks its current owner. Test `seizedCreatureAttacksItsCurrentOwner`; it fails on the old code by reasoning, because the old handler read the stamped owner.
- AI: CombatSimulator, Easy and Hard AI see filtered requirements and pick an obeying blocker, reading through the engine reader. Test `exhaustiveMustBeBlockedByMatchingCreature`; it fails on the old code by reasoning (no forced block).
- An Equipment that is an attacking creature no longer carries its own "equipped creature must be blocked" requirement (CR 301.5c). No card reaches this today. Test `animatedCleaverItselfNeedNotBeBlockedByEldrazi` guards the fold.
- The survivors' log lines are used for Taunt, Gideon Jura and My Crushing Masterstroke.

**Tests.** None changed; 3 added. Ran 253 classes, 2,170 cases, 0 failures.

**Seen, not changed.**
- `CombatAttackService` throws "must attack the specified player" for a `MustAttackIfAbleEffect(defenderId)` instead of weighing it against other requirements (CR 508.1d). Sure of the code, low impact.
- The AI's filtered must-be-blocked choice with menace (two blockers needed) is approximate: the greedy simulator doesn't narrow, and Easy and the exhaustive phase may skip the block. Fairly sure, low impact.
- `MustBeBlockedIfAbleEffect` has no subject: an Aura's or Equipment's static is told apart from the creature's own only by the Equipment check. Low impact.

**For the next run.** Run 7 is closed. Step 10 could give `MustBeBlockedIfAbleEffect` a subject (`source()` / `attachedCreature()`) and merge it with `SetCombatRequirementThisTurnEffect`'s MUST_BE_BLOCKED payload.

### Step 9, fold run 8d (2026-11-24, parallel worktree)
Batch `splits` (second half, 11 rows): 7 split, 4 `blocked-other`. Records and handlers 3,059 / 2,480 → 3,052 / 2,473 in the worktree (7 records and 7 handlers deleted). CR 101.4, 118.12, 400.7, 406.3, 603.4, 603.6a, 603.12, 608.2c, 608.2d, 608.2h, 701.20a and 701.23d checked with `get_rule`. Oracle text checked for 7 of 7 rewritten cards, plus the 4 left cards.

| Folded | Into |
|---|---|
| Dismantle | `NoteAmountEffect(CountersOnTargetPermanent(null))` + destroy + `ConditionalEffect(HappenedThisWay(NOTE), ChooseOneAtResolutionEffect(+1/+1 \| charge: choose an artifact you control + place counters over `AmountThisWay.noted()`))` |
| Gandalf, Westward Voyager | reveal over each opponent + `toACard(REVEAL, CardSharesCardTypeWithTriggeringSpellPredicate)` gating the copy and each opponent's draw + `NotCondition` "otherwise" (nothing revealed means none shares, as before) |
| Worlds Within Worlds | exile all creatures + `EachPlayerMayPutCardFromHandToBattlefieldEffect(..., anyNumber)` + `PutCardsIntoHandEffect(cardsThisWay(EXILE))` |
| Mimic Vat | `ExileGraveyardCardsEffect.cards(triggeringCards()).imprintingOnSource()` + `exiled()` gating `PutExiledCardsIntoOwnersGraveyardEffect(except(exiledWithSource(), cardsThisWay(EXILE)))` |
| Infernal Offering | choose an opponent + sacrifice by you and that player (APNAP, one event) + draw for the players who sacrificed + choose an opponent + two mandatory returns, one step each |
| Turf War | choice by the creature's controller + `GainControlOfTargetEffect.gainedBy` + untap of the chosen land (untapped even if control doesn't change) |
| Thran Tome | reveal 3 + choice by the target opponent + put the chosen card into the graveyard + draw 2 (Murmurs from Beyond's pattern) |

**Pieces closed.** `EffectResolutionService` now binds a `HappenedThisWay` card filter's triggering-spell types, through a new `bindTriggeringSpellCardTypes(..., GameQueryService)` overload that reads perpetual types and Encroaching Mycosynth's spell types. The CARDS scope of `ExileGraveyardCardsEffect` now imprints. `RevealTopCardOfLibraryEffect` now shows the revealed cards to every player. Deleted with the members: `DismantleCounterTypeChoice`, `WorldsWithinWorldsChoice` (with its support, handler, projection and AI strategy), `TurfWarLandChoice`, the two Infernal Offering contexts and `PendingThranTomeChoice`.

**Behaviour changes.**
- Worlds Within Worlds' creatures enter through the entry batch, so their enters abilities trigger (CR 603.6a; the old `place` path skipped them). New Elvish Visionary test.
- Every revealed top card now raises the reveal event (CR 701.20a). New handler test. Kellan's test card was nameless and is now a real Shock.
- A lone Infernal Offering return is made unasked (CR 608.2d).
- The survivor's AI answer for an any-number put-from-hand choice puts every eligible card (Tempting Wurm too).
- Mimic Vat finds the dying card in its graveyard stay (CR 400.7) instead of by id in any graveyard.
- Gandalf: a type granted to the spell counts. New Encroaching Mycosynth test.

**Where the triage was wrong.** Turf War: untapping `thisWay(GAIN_CONTROL)` would skip a land its controller already controls (the Ray of Command test). Mimic Vat: the note's `triggeringCards(creature)` would refuse the noncreature cards its rulings allow. Risky Move: the two CHOOSE steps collide.

**Blocked.**
- Earth Rumble: no way to declare a two-group reflexive target with one optional group; earthbend records no step.
- Head Games: needs an unrevealed HAND destination on `SearchTargetLibraryEffect`.
- Mirror of Fate: no face-up exiled card filter.
- Risky Move: two choice results of one kind in one resolution.
Each would be a one-card piece.

**Seen, not changed.** `SubjectResolutionService.bindResolutionFilter` still binds the triggering spell's printed types only, so Baral and Kari Zev and Possibility Storm miss types an effect grants (Encroaching Mycosynth). Fairly sure.

**For the next run.** Risky Move needs a way to keep two choice results of one kind. Earth Rumble needs two-group reflexive targets. No other blocked row names either piece.

### Step 9, fold run 50 (2026-11-24, parallel worktree)
Leftovers. 2 folded, 7 left (not reached). Ratchet 3,055 / 2,479 → 3,053 / 2,479. 13 card files rewritten, Oracle text checked for all 13. CR checked: 107.3a, 118.9d, 119.4, 119.8, 601.2b, 601.2f, 702.42a, 702.120a.

| Member | Result |
|---|---|
| `PayXLifeCost` (8) | absorbed into `PayLifeCost.x()`. Activation paths: the `PayLifeCost` readers already evaluate with the activation's X. Spell path: `ExtractedCosts.payXLife` is gone. `validateAll` checks `validateSpellLifeCost` with the announced X (paying 0 life is no payment, so Angel of Jubilation doesn't stop X = 0). `paySpellLifeCost` and `castLifePayments` use `AmountEvaluationService.evaluateCost`. `AdditionalSpellCostService` gets the evaluator through an `ObjectProvider`; its compat constructor reads only fixed amounts. Satisfiability treats X as 0. Kicker and buyback keep `fixedAmount()`. |
| `EntwineManaCost` (5) | absorbed into `EscalateCost.entwine("{M}")`. Hand casts compute the same cost through `escalateManaSuffix` / `escalatePayments`, including the in-between refusal. Free casts: `maxEscalateModes` gives all modes when entwine is payable, else the number specified. `ExileFreeCastModeChoice.requiredOrAllModes` offers Done only at that number, refuses a Done in between, and skips a card stuck in between. Graveyard casts: refused only when a non-tap escalate or entwine payment is owed (`escalatePaymentsOwed`, read before the modes are unwrapped). |
| 7 others | not reached; ledger rows restate or sharpen each note (copy pair and Shifting Shadow sharpened). |

Behaviour changes:
- Krumar Initiate is refused while its life total can't change (CR 119.8): the survivor's guard now applies (Platinum Emperion test; X = 0 still allowed).
- The 5 entwine cards are cast from exile for free instead of skipped (CR 118.9d; 3 Hazoret-driven Kaya's Guile tests). Fixes defect line 627.
- A graveyard cast with all modes of an entwine card is refused. Before, entwine was unpaid (CR 702.42a; Past in Flames test). Escalate cards with one mode are now allowed from the graveyard (CR 702.120a).

Cost-shape: payments stay where they were. Step 10's cost-combinator merge would join the spell `paySpellLifeCost` / `validateSpellLifeCost` with the activation and forced payers on `evaluateCost` and drop `fixedAmount()` from kicker and buyback.

Seen, not changed:
- The graveyard cast path never pays a tap escalate (Collective Effort with 2+ modes via a flashback grant taps nothing; CR 702.120a). Fairly sure from the code (no escalate payer after the refusal list).
- The outside-game normal-cost modal path (`OutsideGameNormalCostCastSupport`, maximum `choicesMax + 1`) doesn't use `requiredOrAllModes`, so Kaya's 3-of-4 is refused only by the hand path's in-between check. Sure of the code; unchanged.

For the next run: the copy pair (`BecomeCopyOfCardUntilEndOfTurnEffect` with `BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect`: Dermotaxi, Protean War Engine, Lazav) onto `BecomeCopyOfSubjectEffect`. It needs a `CardSubject` option for the unlinked (imprint) ability's card, `abilityLink == null` (Dermotaxi ruling), and Lazav's pick as `ChooseCardEffect.oneOf` + `cardsThisWay(CHOOSE)` replacing `ExiledCreatureCopyChoice`.

### Step 9, fold run 48b (2026-11-25, parallel worktree)
3 of 3 batches taken: 10 absorbed, 1 split, 1 dead, 6 survivor, 3 unique, 2 blocked-other. Ratchet 3,046 / 2,470 → 3,037 / 2,463. 7 card files rewritten, Oracle text checked for 7 of 7. CR checked: 506.2, 506.2a, 508.1d, 508.1h, 508.5, 509.1c, 509.1d, 509.1e, 608.2b, 608.2d, 608.2h, 611.2c, 702.188a, 708.2a, 118.9, 118.9d.

| Batch | Result |
|---|---|
| attack-tax | 8 of 11 folded:
- **War Tax:** `GrantStaticEffectToPlayerEffect(RequirePaymentToAttackEffect(XValue), eachPlayer(), UNTIL_END_OF_TURN)`.
- **Forbidding Spirit:** `untilYourNextTurn(new RequirePaymentToAttackEffect(2))`.
- **Summon: Yojimbo:** `untilYourNextTurn(playerOnly(2))`.
- **War Cadence:** `untilEndOfTurn(new RequirePaymentToBlockEffect(XValue))`.
- **Sivitri:** `untilYourNextTurn(new GlobalAttackLifeTaxEffect(2))`.
- **Nils:** `RequirePaymentToAttackEffect(CountersOnTargetPermanent(null))`, the amount read against each attacker.
- `GlobalAttackTaxEffect` and `GlobalBlockTaxEffect` are gone, with 4 capability interfaces (one of them dead).
- Left: `GlobalAttackLifeTaxEffect` stays as Sivitri's floated payload (a life kind would be hers alone). Heat Wave is `unique`: its life payment and its blocker and attacker filters would be three options only it sets.
- `RequirePaymentToAttackEffect` and `RequirePaymentToBlockEffect` are survivors. |
| alt-cost | 0 folded, 2 `blocked-other`:
- **Dream Halls:** its discard is a chosen non-mana alternative cost, carried by `sharedColorDiscardHandCardIndex` through the request, cast path, projection, AI and frontend. `AlternativeCostForSpellsEffect` only pays automatic `AlternativeSpellCost`s, so moving onto it would only rename the check.
- **`WebSlingingEffect`:** it grants a keyword (CR 702.188a), the sibling of the prowl, blitz and freerunning grants, each with its own `findGranted*AlternateCast` and cast flag. Folding these is a casting redesign (step 10). |
| cost-shape-singles (rest) | **Magmasaur** split: `MayEffect(RemoveCounterFromSourceEffect(+1/+1, 1), …, Sequence(SacrificeSelfEffect, MassDamageEffect(CountersOnSource(+1/+1), …)))`; 2 handlers and the support class are gone. **Tempest Efreet and Timmerian Fiends** are `unique` (weighed): the exchange-ownership, from-anywhere mover and ante pieces would serve only these two cards. |

**Survivors and shared paths.**
- `GrantStaticEffectToPlayerEffect` fixes an `AmountsFixedWhenFloatedEffect`'s amounts once, as it resolves (CR 608.2h).
- The attack-tax readers judge the attacked player or the attacked planeswalker's controller (CR 506.2a, 508.5; was `getOpponentId`; failing-first tests in Nils and Forbidding Spirit). They also read copies floated on that player.
- A floated block tax is board-wide.
- `MayEffect` withholds a leading source counter removal the source can't make (CR 608.2d). This is a fix for 10 other cards; 12 tests that answered a no-op prompt now assert no prompt.
- `PermanentRemovalService` refreshes the resolving entry's source snapshot when the source leaves mid-resolution. Last known information keeps lifelink, deathtouch, infect and wither (CR 608.2h). Magmasaur's lifelink test is the failing-first case: the new shape fails without the refresh.

**Behaviour changes.** Multiplayer attack taxes, the face-down Nils (CR 708.2a), and the counter-removal guard, as above.

**Cost-shape.** Payments stay where they were. Step 10's cost-combinator merge would take: `GlobalAttackLifeTaxEffect` (a life kind), Heat Wave's filtered life block tax, Dream Halls' chosen non-mana alternative cost, and the granted alternate-cast keywords.

**Seen, not changed.**
- War Tax doesn't tax attacks on battles (CR 506.2 battles; "creatures can't attack" is unrestricted). Its battle test looks vacuous: it checks that the pool is empty after the step moves on. Sure of the code; vacuity likely.
- Several attack-tax card tests assert `pool.isZero()` after declaring, which may hold whatever the tax. Fairly sure.
- `TimmerianFiendsAnteExchangeEffect`'s Javadoc said ownership stays unchanged. Fixed: the handler already swaps owners.

**For the next run.** All three Run 48 batches are closed. The alt-cost pair waits for a casting-path redesign (step 10).

### Step 9, fold run 8e (2026-11-24, parallel worktree)
Batch `splits`, last 5 rows: all 5 split onto existing steps; the batch is closed. Ratchet 3,041 / 2,465 → 3,036 / 2,459. CR 101.4, 118.8, 400.7, 601.3, 603.12, 608.2c, 608.2d, 608.2g, 608.2h, 701.20b, 701.55d, 709.3a, 709.4, 712.8c checked with `get_rule`. 5 card files rewritten; Oracle text checked for all 5, rulings for all 5.

| Folded | Into |
|---|---|
| Unexpected Results | shuffle + `LookAtTopCardsAndMayCastEffect.reveal(1).leavingTheRest()` + `ConditionalEffect(toACard(REVEAL, land), MayEffect(put revealedThisWay(land) + ConditionalEffect(returned(), ReturnToHandEffect.selfSpell())))` |
| Rashmi, Eternities Crafter | `reveal(1).withSpellManaValueAtMost(Sum(TargetSpellManaValue, -1)).leavingTheRest()` + `PutCardsIntoHandEffect(revealedThisWay())` |
| Specter's Shriek | `ChooseCardsFromTargetHandEffect` (up to one nonland, EXILE) + `ConditionalEffect(toACard(EXILE, nonblack), ExileCardFromHandEffect())` |
| The Master, Gallifrey's End | `MayEffect`(exile `triggeringCards()` + `ConditionalEffect(exiled(), ChoosePlayerEffect(matching(eachOpponent(), PlayerLifeExactlyPredicate(HighestOpponentLifeTotal))) + villainousChoiceFacedBy(playersThisWay(CHOOSE), …))`) |
| Danse Macabre | `SacrificePermanentsEffect(…, eachPlayer()).withSimultaneousChoices()` + `RollD20Effect.withAddedAmount(sacrificedToughness().controlledByYou(), oneOf / upTo(2) of cardsThisWay(SACRIFICE, creature) + put under your control)` |

**The pieces.**
- The reveal form of `LookAtTopCardsAndMayCastEffect` records REVEAL (cards, controller, library) before its cast choice, which gives it two users.
- The cast resuming the later steps was already true: every epilogue resumes the parked resolution, and a targeted or modal cast resumes after its answer (CR 608.2g). Tests cover it.
- `PlayerLifeExactlyPredicate` takes a `DynamicAmount` (int overload kept for Triskaidekaphobia).

**Where the triage / 8c notes were wrong.**
- `LibrarySearchDestination.CAST_WITHOUT_PAYING_OR_PUT_INTO_HAND` does not fit Rashmi: it casts an exiled card, and nothing produces it (dead).
- Specter's Shriek's record was harmful and the hand head is benign. Only activated abilities read the polarity, and the opponent-only filter decides the target, so nothing changes.
- Danse's ruling says choices go "starting with you". At sorcery speed that is the active player, so APNAP (CR 101.4) is the same.

**Behaviour changes.**
- Unexpected Results: the land goes through the entry batch and the can't-enter check (Worms of the Earth; the Fold: defect). The spell returns only if the land entered (ruling). New test.
- Rashmi: a split card's half within the bound is offered (Oracle "a spell with lesser mana value", CR 709.3a). New test.
- Both now use the library-cast prompt.
- Danse: a lone returnable card is taken unasked (CR 608.2d).
- The survivors' log lines win.

**Deleted.**
- The 5 records, their 6 handlers (one mayfx).
- `MayMiscHandlerService.handleUnexpectedResultsLandChoice`.
- `PendingInteraction.SpectersShriekChoice`, its projection, interaction handler, AI strategy and `CardChoiceHandlerService` path.
- `PermanentChoiceContext.TheMasterMostLifeChoice` and its completion.
- `MultiPermanentChoiceContext.DanseMacabreSacrifice` and its completion.

**Seen, not changed.**
- `LookAtTopCardsAndMayCastEffect` offers spells that can't be cast (no legal target, a spell limit, an unpayable mandatory additional cost); picking one logs "can't be cast" (CR 608.2d: an impossible option can't be chosen). Fairly sure.
- `CAST_WITHOUT_PAYING_OR_PUT_INTO_HAND` and `handleCastOrPutIntoHandChoice` are dead.

**For the next run.** `splits` is closed. Remove the dead destination and stop offering uncastable spells in the library-cast prompt in a defect run.

### Step 9, fold run 50b (2026-11-24, parallel worktree)
Leftovers. 6 folded (4 absorbed, 2 split; Valki extra), 1 blocked-other, 2 not reached. Ratchet 3,041 / 2,465 → 3,035 / 2,458. 7 card files rewritten; Oracle text checked for all 7 (and Darigaaz). CR checked: 400.7, 514.2, 607.2a, 608.2b, 608.2d, 608.2h, 701.23a, 707.2, 707.9b.

| Member | Result |
|---|---|
| copy pair (Dermotaxi, Protean War Engine, Lazav, Wearer of Faces) | absorbed into `BecomeCopyOfSubjectEffect.of(exiledWithSourceUnlinked(creature card)).untilEndOfTurn().except(CopyExceptions.stillA(ARTIFACT).alsoWithSubtypes(VEHICLE))`; Lazav adds `ChooseCardEffect.oneOf(...)` + `cardsThisWay(CHOOSE)` inside its `MayEffect`. `BecomeCopyOfCardUntilEndOfTurnEffect` and its dead mayfx handler gone. `ExiledCreatureCopyChoice` stays (token copy and enter-as-copy flows) |
| Valki (`BecomeCopyOfExiledCreatureWithManaValueEffect`, fold run 4's blocked row) | absorbed by the same option: choose `allOf(creature, CardManaValueEqualsXPredicate)` + permanent copy; `PendingValkiCopyChoice` gone |
| Shifting Shadow | split: destroy `source()` + `RevealCardsFromTopUntilEffect` + `PutCardsOntoBattlefieldEffect(revealedThisWay)` + attach `(granter(), returnedThisResolution())` + rest on the bottom at random. The put step already remembers the permanent it put (RETURN, `returnedThisResolution()`); `thisWay(RETURN)` is refused by the subject |
| Darigaaz | blocked-other: everything exists but a face-down exile `ConjureDestination`, which only Darigaaz would set |
| Wakanda Forever! | split as fold run 20 wrote; MultiGraveyardChoice picks over the revealed cards |
| Firemind's Foresight | absorbed: `SearchLibraryForOneCardOfEachSubtypeEffect.ofEach(Each...)`; `ToHandPick` is (filter, label \| cardName), colours and subtypes as predicates (Conflux, Gem, Yasharn), `remainingInstantManaValueToHandPicks` and `startNextInstantManaValueToHandPick` gone |
| Viconia, Serene Remembrance / Turn the Earth | not reached (run length) |

Behaviour changes:
- Firemind's Foresight keeps Aven Mindcensor's original top four for all three picks (old: re-limited to the current top four for each pick; test). Defect lines 497 and 508.
- Valki logs its copy (defect line 682; test); a card exiled under an ability link isn't copyable (test).
- Dermotaxi, Protean War Engine and Lazav get the survivor's once-each-turn activation reset on copying (untested).
- Conflux, Gem and Yasharn picks are judged by `PredicateEvaluationService` (effective colours and subtypes; untested); prompts read "an Island card".
- Log and prompt lines of Shifting Shadow, Wakanda, Lazav and Firemind are the survivors' (tests updated: Lazav, Wakanda, Firemind, Conflux).

Seen, not changed:
- A Valki ability gained by another permanent still reads that permanent's unlinked exiled cards, not a link of its own (line 705 stays). Sure of the code.

For the next run: Serene Remembrance / Turn the Earth with Deliver Unto Evil and Wake to Slaughter (a CardSubject naming every target card, SpellCastingService's record-keyed branches), then Viconia's faces (card data, a target group per `TargetExiledCard`).

### Step 9, defect run D11 (2026-11-25, parallel worktree)
Group 1, 34 lines: 18 fixed (2 partly), 10 already fixed, 4 not a defect, 4 left. No record added or removed; ratchet unchanged.

- **Pattern.** Printed-static readers now use `GameQueryService.getActiveStaticEffects(gd, permanent, controller)` / `hasActiveStaticEffect` / `activeStaticEffects(gd, type)`. These return nothing while the permanent is face down or has lost its abilities, and they unwrap conditional statics.
- **Readers fixed:**
  - `CastingPermissionService` (`controlsStatic`, the battlefield spend permission, Uba Mask's permission).
  - `DrawService` (cycling, Shared Fate, Uba Mask, counter and threshold, Abundance, Tomorrow, Enduring Renewal, win-on-empty).
  - The Crypt handler, explore counts, `PlayerDamageFinalReplacements`, `DamageSupport` (Lich, Coil, Sanctuary, counter replacements), Enduring Angel.
  - `CastingCostService` (loyalty tax, per-target changes, battlefield targeting reductions, web-slinging).
  - `GameQueryService` (Ghostly Flame, Overwhelming Splendor, Aura absorbers, Serra's Emissary), Phytohydra's damage-to-counters step.
- **Entering permanents.** `GameQueryService.entersWithCardType` judges an entering permanent's creature/artifact type inside `withEnteringPermanent` (CR 614.12). `losesNonManaAbilitiesAsLandEnters` is now `hasLostPrintedAbilitiesAsEntering`, so full loss (Alpine Moon), Blood Sun and Blood Moon all count.
- **Manifest and cloak.** `isManifestBlockedFromZone` tests `CastingPermissionService.faceDownSpellCharacteristics(card)` (CR 701.40f, 701.58f).
- **Kappa Cannoneer.** Its "it can't be blocked this turn" is a rule (`MakeCreatureUnblockableEffect`), which ability loss doesn't remove (CR 611.2c).
- **Seen, not changed:**
  - The other `CastingPermissionService` readers (spell limits, Ethersworn Canonist, most-recent-spell, global land restriction) skip lost abilities but not face-down sources.
  - `DamagePreventionService` per-source preventions (around lines 1284 and 1315) have no guard at all; the cleric, all-but-one and spell-token preventions ask only `hasLostAllAbilities`; `grantedAttachedPreventionSurvives` reads the Aura's printed statics.
  - `getEffectiveCardColors`' off-battlefield scan (Celestial Dawn, Painter's Servant) asks only the floating flag.
  - `areCreatureDeathTriggersSuppressed`' simultaneous-death scan asks only the floating flag and skips no face-down source.
  - `findAlmsCollectorSource`, `findReturnFromGraveyardInsteadOfDrawSourceCard` and Quantum Riddler's reader skip no face-down source.
- **For the next run:** a "rule, not ability" mode for `GrantEffectToTargetUntilEndOfTurnEffect` / `GrantTargetingRestrictionToTargetUntilEndOfTurnEffect` (floated on the permanent, like `makeUnblockableThisTurn`) would close Lockjaw, Slicer and Vines. A sweep of the remaining printed readers above onto active statics.

### Step 9, defect run D13 (2026-11-25, parallel worktree)
Group 3. 6 fixed, 4 not a defect, 9 left, 5 not reached. Ratchet unchanged.
- **Main finding:** leaving one at a time inside one handler run, with no prompt in between, is already one event for triggers. The resolving effect's rules-event batch matches "one or more" triggers once and keeps leave snapshots for look-back (Ulamog, Resounding Wave, Descent tests). Real defects are the ones where a prompt or answer comes between departures: the choice completions.
- **Fix pattern:** collect the choices in the choice context or state, then act once: `destroyBatch` (Albatross, with the payment in `beforeDestruction`), a leave batch (Sothera, Ol Rin's), `departNow` (Make an Example). Highcliff Felidar's `chosenPermanentIds` was the model.
- **Spell costs:** `paySacrificeCostTogether` checks each permanent, then sacrifices them all with one `sacrificeForSpellCost`.
- **Seen, not changed:**
- Eumidian Wastewaker counts a sacrificed land token, and a permanent that is a land only on the battlefield, as a land card (its ruling). Sure.
- Ezuri's Predation fights the opponents' creatures as of the fight, not the ones it counted (CR 608.2h). Low.
- SotheraTheSupervoidTest and OlRinsSearingLightTest have methods without `@DisplayName`.
- Cut the Tethers (`ReturnMatchingPermanentsUnlessOwnerPaysEffectHandler`) bounces at each answer, the same shape as Albatross was.
- **For the next run:** Gilded Ambusher and Dispersal (Sothera pattern), Eumidian Wastewaker, Cut the Tethers, L23, then L17–L21.

### Step 9, defect run D12 (2026-11-25, parallel worktree)
Groups 2 and 8, 34 lines: fixed 15, not a defect 7, already fixed 4, left 8. Ratchet 3,029 / 2,455 unchanged. Ledger: 1 row (`AlmsCollectorDrawReplacement` dead, deleted with `DrawService`'s unused `findAlmsCollectorSource`).

Oracle text checked for 30 cards; rulings for Distant Memories, Abstract Performance, Brilliant Ultimatum, Deliver Unto Evil, Ecological Appreciation, Infernal Denizen, Moonring Mirror, Duplicity, Galactus and Malik. CR checked: 101.4, 104.4a, 303.4f, 400.2, 406.3, 508.1d, 508.4, 508.5, 509.1, 608.2d, 609.3, 614.5, 616.1, 701.20a/d/e, 701.29a, 701.30a/b/d, 702.174a, 707.2, 708.2, 800.4a.

**"An opponent" the controller picks.** `ChoosePlayerEffectHandler.opponentToBeChosenFirst(gd, entry, rerun)`: with two or more opponents it inserts `ChoosePlayerEffect.opponent()` and a rerun that reads `playersThisWay(CHOOSE)`, and the handler returns before it changes anything. With one opponent nothing is asked (`onlyOpponent`). Used by:
- `ClashEffectHandler` (`.against`), `ClashForControlOfEnchantedCreatureEffectHandler`;
- `FatesealEffectHandler`, `DeliverUntoEvilEffectHandler`.

`ClashSupport.performClash` takes the opponent; the two-argument overload stays for tests.

It doesn't fit when the choice must follow an exile, search or reveal inside the same handler or interaction: Abstract Performance, Brilliant Ultimatum, Hostile Negotiations, Ecological Appreciation, Threats Undetected, Turtles Forever, Verdant Mastery. Seven cards wait for a choose-opponent prompt mid-flow.

**DrawService.** `OpponentSource` holds a source and its controller.
- The extra-draw and two-or-more-draw sources are searched on every opponent's battlefield.
- Notion Thief loops: each Thief controlled by an opponent of the current drawer applies once (CR 614.5).
- With several sources, the first found in the service's APNAP-order helper applies; the affected player's choice (CR 616.1) is a new line.

**Other fixes**
- Distant Memories: `OpponentMayReturnExiledCardOrDrawEffect(drawCount, controllerId)` chains the may over each opponent in APNAP order; the controller draws only when all decline.
- Retribution: `CONTROLLED_BY_FIRST_TARGET`.
- `AttackLegalityService.canAttackAPlayer` covers the "a player" requirement count and its check in `CombatAttackService`.
- `TargetOpponentCreatesTokenEffect` reads a declared player target; a gift falls back to the first other seat.
- `MayCopyHandlerService` copies `cardToCopy`.
- Moonring Mirror (`faceDownWithoutLookPermission(1)`) and Duplicity (`mayLook` false) show no card to their owner.
- Juggle: shuffle, take at most count, log the real count.
- Absorb Energy: `spellAsEffectsSeeIt`.
- Logs: seek into hand ("X seeks N card(s)."); Malik ("X chose A and Y chose B."); Valki ("V becomes a copy of C.").

**Tests edited**
- `JuggleThePerformanceTest`: seven-card libraries in two cases (they asserted seven duplicates of one card).
- `DrawServiceTest`: Alms Collector case seats both players; unused stub removed.

**Seen, not changed (new defect lines below)**
- Ingenious Mastery's "they scry 2" scries every opponent.
- Several replacement sources from different opponents are not offered to the affected player.
- `ReturnCardExiledWithSourceToBattlefieldEffectHandler` sets a creature attacking with no target.
- Clash log lines say "opponent", not the player's name.
- `exilesHandsFaceDown` doesn't unwrap `MayEffect`.

**For the next run:** a choose-opponent prompt mid-flow for the seven cards above, then Zur's Weirding's turn-order offers.

### Step 9, fold run 50c (2026-11-24, parallel worktree)
Leftovers. 2 folded (1 absorbed, 1 split), 1 blocked-other. Ratchet 3,009 / 2,435 → 3,007 / 2,433. 3 card files rewritten, Oracle text and rulings checked for all 3 (and Viconia, Wake to Slaughter read). CR checked: 400.7, 601.2c, 608.2b, 608.2d, 701.24a–e.

| Member | Result |
|---|---|
| Serene Remembrance / Turn the Earth | absorbed into `PutCardsIntoLibraryEffect.shuffledIn(Subjects.targetGraveyardCards(null, ALL_GRAVEYARDS, 3)[.fromASingleGraveyard()])`; the card keeps `target(GraveyardCardPredicateTargetFilter, 0, 3)` |
| Deliver Unto Evil | split: `ConditionalEffect(NotCondition(ControlsPermanent(Bolas PW)), ChoosePlayerEffect.opponent())` + `ChooseCardEffect(targets, …, alwaysAsked, Fixed(2)).by(playersThisWay(CHOOSE))` + `PutCardsIntoHandEffect(except(targets, cardsThisWay(CHOOSE)))` + `ExileSpellEffect` (Burning-Rune Demon's shape; with Bolas nobody is chosen, so no card is) |
| Viconia | blocked-other (ledger note: per-step target groups in the made specialize trigger, blue's two conjures vs one CONJURE result, white's queued may) |
| Wake to Slaughter | not moved: removing `opponentChoosesOneForHand` means reshaping a 17-component record used by about 100 cards, plus a haste and delayed-exile composition; not a clear win in this run |

New shared piece: `CardSubject.TargetGraveyardCards(filter, scope, maxTargets, singleGraveyard)` / `Subjects.targetGraveyardCards(...)` / `.fromASingleGraveyard()`. It is non-target: the card's target group declares the targets. It names every target still in a graveyard of the scope and matching (CR 608.2b). `EffectResolution.targetGraveyardCards(effects)` finds it in `PutCardsIntoLibraryEffect`, `PutCardsIntoHandEffect` and `ChooseCardEffect` (also inside `Except`, a `SequenceEffect` or a condition branch). `SpellCastingService.beginTargetGraveyardCardsChoice` asks for the targets on the hand and flashback paths, replacing both record-keyed branches. `PutCardsIntoLibraryEffect.shuffledIn` followed by `ShuffleIntoLibraryEffect` leaves the spell owner's library to it (`deferredLibraryShuffleOwnerIds`), so "shuffle this and up to three target cards" shuffles each library once (CR 701.24c; Cosi's Trickster tests).

Behaviour changes:
- Deliver Unto Evil: the caster chooses the opponent (ruling; was `getOpponentId`; three-player test). Fixes the defect line on Deliver's chooser.
- Deliver Unto Evil with Ground Seal: cast with no target and exiled. The old branch counted cards ignoring `canGraveyardCardsBeTargeted` and dropped the spell (failing-first test).
- Flashback path: keeps Serene's single-graveyard rule (old: ignored) and now asks for Deliver's targets (untested, no granting card in tests).
- The survivor's log lines win. The `handleUpToNGraveyardSpellTargeting` all-graveyards prompt says "from graveyards" (was "from a graveyard").
- Deliver's "an opponent chooses two" stays asked with one or two targets (`alwaysAsked`, "chooses" text); its tests are unchanged.

Removed: `ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect` and `DeliverUntoEvilEffect` with their handlers, `CardPileDisposition.DELIVER_UNTO_EVIL`, `GraveyardReturnSupport.completeDeliverUntoEvilChoice` and its `GraveyardChoiceHandlerService` branch.

Tests: new SubjectResolutionServiceTest +1, ShuffleIntoLibraryEffectHandlerTest +1, DeliverUntoEvilTest +2, SereneRemembranceTest +2. No existing test changed. 37 classes, 482 cases, 0 failures.

Seen, not changed:
- Wake to Slaughter's opponent choice still uses its own `wakeToSlaughterChosenCardId` state and `getOpponentId`-style chooser (not checked in multiplayer). Fairly sure from the code.
- The effects over the subject declare no `TargetSpec`, so resolution legality of these targets is "still in a targetable graveyard". That is equal for these three cards (no filters; a graveyard card can't change owner).

For the next run: Bow of Nylea (`PutTargetCardsFromGraveyardOnBottomOfLibraryEffect`) and The Moonbase (Cybermen face-down return) wait in `blocked-other` for "a subject naming every target". They are abilities, so the activation path (`AbilityActivationService`'s `TargetedGraveyardCardsEffect` keying) needs the same subject branch. Then Viconia's faces; Wake to Slaughter onto the subject when its record is next reshaped.

### Step 9, defect run D14 (2026-11-25, parallel worktree)
Group 4: 13 fixed, 7 not a defect or already fixed, 11 left. Ratchet unchanged at 3,015 / 2,442. CR 508.1d, 608.2d, 614.1c, 614.6, 616.1, 701.23a, 701.23b, 702.63a, 705.2 and 705.3 checked with `get_rule`.
- **Coin flips:** `FlipUntilLoseOrStopSupport.flip` raises the won or lost event. Mutalith's handler gates on `isActualCoinFlip`. Heads-or-tails handlers (Odds through `FlipCoinWinEffect`, Rakdos) raise "won" when `edgarApplied`.
- **Dice:** every d6, d8, d10, d12 (Sword of Hours) and entry dice roll goes through `DiceRollService.rollDice`. Advantage dice ignored as never rolled, so they raise no die events (CR 706.6).
- **Vanishing:** the keyword's own trigger (`PermanentCounterSupport.checkLastTimeCounterRemoved`) is the only sacrifice. Four cards dropped their duplicate trigger, which had doubled even the normal upkeep sacrifice.
- **`PermanentCounterSupport.removeAllCounters`:** one "remove all counters" removal: oil record, last-counter event per kind, vanishing check.
- **`removeCountersForMove(movingPlayerId)`:** raises the last-counter event.
- **Sin's as-enters choice** skips `permanentsEnteringSimultaneously`.
- **Parley (token form):** inserts `CreateTokenEffect` + `DrawCardEffect(1, eachPlayer())` as later steps.
- **`MayEffect`** over a leading `SearchLibraryEffect` whose searcher can't search (`LibrarySearchRestrictions.find`) isn't asked; the else effect follows.

Seen, not changed: L12 confirmed real by Pact Weapon's ruling. L13 and L31 need 3+ players.

For the next run: L24 (seven guards); L30 (prevention shield); L21/L22 together with Journey's "then shuffle".

### Step 9, defect run D15 (2026-11-25, parallel worktree)
28 group-5 lines: 11 fixed, 7 already fixed, 5 not a defect, 5 left. Ratchet 3,009 / 2,435 → 3,008 / 2,434: `EachPlayerChoosesLandAndPutCounterEffect`, its handler and `EachPlayerChoosesLandAndPutCounterChoice` were made dead by the Turf War fix and deleted. CR checked: 110.2a, 115.3, 508.4, 601.2c, 603.3d, 607.2d, 607.5, 608.2d, 707.7, 708.5.
- Turf War: `ONE_PER_PLAYER` + `target(land(), 0, 99)` + `PutCounterOnTargetPermanentEffect(CONTESTED)`. The attack-trigger door already enforces one target per player and removes the trigger with too few (Vaevictis' ruling).
- Infectious Rage enters under the ability controller and records the owner (`trackStolenCreature`).
- Conspiracy Theorist: `youDiscardOneOrMoreCards().withFilter(nonland)`.
- `GainControlOfTargetEffectHandler`: the permanent duration reads `targetsForEffect`, like the temporary path.
- `PlayerInputService.beginAnyTargetChoice` and `beginMultiPermanentOrPlayerChoice` drop can't-be-chosen permanents, as `beginPermanentChoice` and `beginMultiPermanentChoice` already did. `ReturnChosenPermanentsToHandEffectHandler` and Scour the Scene also drop them before counting.
- `LibrarySearchSupport`: no cast-only offer under Opposition Agent; no reverse-miracle offer when the bottom card is outside the top-N limit.
- `CardChoiceHandlerService.handleTargetedHandBattlefieldCardChosen`: when the defending player controls a planeswalker, it asks which to attack through `TokenAttackTargetChoice`.
- Rambling Possum: `controlledBy(eachPlayer(), saddled-it)`.
- Horus Heresy: prompt text.

Seen, not changed:
- `ExileGraveyardCardsEffect.defendingPlayerOnly` is never read (Graven Abomination; multiplayer).
- Ulamog's Nullifier picks face-down exiled cards by id.
- Zenos' chosen permanent leaks into later copies (L1).

For the next run: the L25 sweep of callers that count candidates, and L26's copy name types.

### Step 9, defect run D16 (2026-11-25, parallel worktree)
Group 5, last 27 lines: 15 fixed, 6 not a defect, 6 left. Ratchet 3,009 / 2,435 → 3,008 / 2,434. 5 card files rewritten, Oracle text checked for all 5. CR checked: 101.2, 113.7a, 115.3, 118.12a, 201.2a, 509.1c, 601.2c, 603.3d, 608.2b, 608.2d, 608.2g, 608.2h, 609.7a, 700.13, 702.11b–f, 702.16b, 702.21a, 707.10, 707.10c, 707.12, 708.2a, 709.4a, 716.3, 716.3a.

Behaviour changes:
- A spell from `LookAtTopCardsAndMayCastEffect` that couldn't be cast now isn't offered (permission, timing, additional costs, legal target; CR 608.2d). It goes through `LibraryChoiceHandlerService.canCastFromLibraryWithoutPaying`; split cards are still judged half by half when one is chosen.
- `MayEffect(DiscardCardThenEffect)` by you isn't asked when nothing matching could be discarded, with or without an else effect (`thenWithoutDiscard` is excluded).
- `ExileCastTargetSupport.flatSingleTargetCandidates` checks each permanent with `checkSpellTargeting` (hexproof, shroud, protection) before offering it.
- Player "hexproof from [colour]" counts every effective colour of the source (`effectiveSourceColors`), in both `TargetLegalityService` and `ValidTargetService`.
- `GameQueryService.isLockedFromActivatingTapAbilities` judges a set's filter on the permanent as it is now, so a creature given flying is locked; the static-set reads saw only printed keywords. Katabatic Winds names creatures only.
- `ChoosePermanentsEffect.toSacrifice()` falls back to all the chooser's candidates when none could be sacrificed and the choice is required (Barrin's Spite's "the other"). Barrin's Spite now uses `toSacrifice()`.
- `EnchantedPermanentManaValue` reads the host the entry recorded at trigger, and a face-down host's mana value is 0 (Soul Tithe).
- `ReturnTargetCardsFromGraveyardToBattlefieldEffect` with `OPPONENT_GRAVEYARD` resolves each target from its owner's graveyard. Fire Lord Sozin uses it, so the stack door narrows the choice to the damaged player's graveyard.
- Twinning Glass is `MayCastAnySpellFromHandWithoutPayingManaCostEffect(new CardSharesNameWithSpellCastThisTurnPredicate())`: Adventures, back faces and split halves are judged by the face being cast, and additional costs are checked before the offer.
- AI: Avatar's Wrath (airbend everything except the target), Overblaze and Pack Hunt now aim at the AI's own creature (BENEFICIAL).

Shared engine files whose meaning changed: `ChoosePermanentsEffectHandler`, `AmountEvaluationService`, `GameQueryService`, `TargetLegalityService`, `ValidTargetService`, `ExileCastTargetSupport`, `LookAtTopCardsAndMayCastEffectHandler`, `LibraryChoiceHandlerService` (new public method), `MayEffectHandler`, `ReturnTargetCardsFromGraveyardToBattlefieldEffectHandler`, `PredicateEvaluationService` (new predicate), `TargetPolarityClassifier`.

Seen, not changed:
- `GameQueryService.layeredQueryReads.matchesLeaf` evaluates a static set's filter without game data, so it uses printed keywords. Katabatic's attack/block half is unverified for flying given by an effect.
- Painter's Servant colours only permanents; its Oracle text also covers cards that aren't on the battlefield and spells.
- Descendants' Fury: when none of the triggering creatures can be sacrificed, the "may" now leads to a pointless choice (same outcome as before).
- `IntiSeneschalOfTheSunTest` has 5 tests with no `@DisplayName`.

For the next run: L6/L7 (an Adventure face prompt in the free-cast queue), L19 (move the masked turn-face-up into the engine), L13 as a fold run, and the owner's answer on L12.

### Step 9, defect run D17 (2026-11-25, parallel worktree)
Group 6. 12 fixed, 2 already fixed, 6 not a defect, 16 left (two of those partly fixed or partly already fixed). Ratchet unchanged (3,009 records).

**Life payments (CR 119.8).**
- `GameQueryService.canPayLifeForCosts(gd, payerId)` is the "may pay life as a cost" check: no prohibition applies and the payer can lose life.
- The activation, spell, flashback, kicker and graveyard-permission life checks now ask `canPlayerLoseLife` instead of `canPlayerLifeChange`, or use the overload. Courageous Resolve's "you can't lose life" now blocks Greed and Toxic Deluge.
- `AdditionalSpellCostService.validateCanPayLifeForCost` now takes the payer.

**Other fixes.**
- `hasCreatureSpellAdditionalCountersCost` reads only the caster's battlefield (CR 109.5).
- The return-or-sacrifice handlers and their may completions find the source by `entry.getSourcePermanentId()`. `SacrificeUnlessReturnPermanentTypeToHandEffectHandler.findSourcePermanent` is now shared, and the pending ability carries the source id.
- The graveyard play path refuses a land without the player's turn or a land play left (`canPlayLandForMadness`).
- `computeTargetBasedCostReduction` sums all its categories. The graveyard and spell first-target reduction is now `firstTargetCardOrSpellReduction`.
- The availability preview of the enchanted-player reduction covers every spell type, not only instants and sorceries.
- The controlled counter-cost kind prompt is asked at every pick, not only the last.
- Charmed Pendant drops the generic half of a monocoloured hybrid symbol.
- The Errant Minion handler pays as a resolution cost: payable amounts come from `canPayResolutionCost`, and the maximum from the special-action virtual pool.
- Rose Room Treasurer: `MayEffect` around its pay and reflexive steps.
- `MayPayManaEffectHandler.canPay`: a payment of 0 life is always payable.

**Seen, not changed:**
- Resolution-time "pay N life" options (`MayAbilityHandlerService`, `MayPenaltyChoiceHandlerService`, about 15 handlers) still ask only `canPlayerLifeChange`, so Courageous Resolve doesn't stop them (CR 119.8). Sure of the code.
- `ForcedCostOrElseEffect` with `RemoveCounterFromControlledPermanentCost` (Chisei) may still remove the first kind of counter. Unverified.
- The graveyard cast path pays no tap escalate (L35, confirmed from the code).

**For the next run:**
- The free-cast flow defects (L11 to L13, L17 to L19).
- The any-colour mode (L15, L30).
- L34 waits for your decision.

### Step 9, defect run D19 (2026-11-25, parallel worktree)
Group 9, L1–L28: 10 fixed, 5 not a defect or not reproduced, 3 already fixed, 13 left. Ratchet 3,007 / 2,433 unchanged.

CR checked: 303.4f, 400.4a, 400.7, 607.1, 607.2a, 607.3, 607.5, 607.5a, 608.2h, 614.12, 701.12a, 701.44c, 701.44d, 721.3, 721.3b–d.

Oracle text and rulings checked: Biting Remark, Bad Wolf Bay, Cunning Azurescale, Limitless Rekindling, Forethought Amulet, Pestilent Spirit, Eumidian Wastewaker, Tamiyo, Oath of Druids, Grafdigger's Cage.

**Fixes:**
- **Linked pairs gained together** (CR 607.5): `GainActivatedAbilitiesOfExiledCardsSelfEffectHandler` links every gained `ExileTargetPermanentAndTrackWithSourceEffect` and `ReturnCardExiledWithSourceToBattlefieldEffect` with `"gained:"+cardId`, through Koh's `GainAbilitiesOfLastChosenExiledCardSelfEffectHandler.linkGainedEffect`, which is now package-private.
- **Entry batch:** members get `handleCreatureEnteredBattlefield` (every as-enters prompt) while no prompt is open; `entersWithPlayerChoice` is gone. `AsEntersInteractionService` reads the entering permanent found by card, not `battlefield.getLast()`.
- **Seek:** every non-land, non-cloaked sought permanent gets its enter processing.
- **Oath of Druids:** enters through the batch (Grafdigger's Cage holds the card in the library).
- **Discard hand:** `discardHand` holds the "can't cause you to discard" guard (self-discards skip the call). The two "each player discards their hand, then draws" handlers reuse it. `EachPlayerMayDiscardHandThenDrawEffectHandler` doesn't ask a protected player (Tamiyo ruling).
- **Divining Dive** is cast with `OmenCast` (CR 721.3d).
- **Pestilent Spirit:** `GameQueryService` gives deathtouch to spells only.
- **Eumidian Wastewaker:** `countSacrificedLandCard` checks whether the sacrificed card is a land card in a graveyard (ruling: no land token, no replacement-exiled land).
- **Dead code:** `DamagePreventionService.applyPlayerPreventionShield` / `applyCombatPlayerPreventionShield` and `applyPerSourceCreatureDamagePreventionShield` are removed. agent-docs now name `PlayerDamagePreventions` / `PlayerDamageRedirects`.

**Not defects:** L3, L8, L23. L21 was not reproduced through enter triggers.

**Seen, not changed:**
- In a multi-member batch, only the first member that asks an as-enters choice gets it; the later ones go straight to their enter abilities.
- Eumidian's discard branch counts by `lastDiscardedCardTypes`.
- Valki-as-copy readers.

**For the next run:**
- L5's Scrycast check, which needs `CastingPermissionService` in `ScryAiStrategy`'s context.
- L27 together with any other conversion bump.
- Then L9's Aura route for the dying-creature returns.

### Step 9, defect run D18 (2026-11-25, parallel worktree)
Group 7, 32 lines: 17 fixed (2 partly), 2 already fixed, 3 not a defect, 9 left, 1 not reached. Ratchet 3,009 unchanged. Ledger: 5 rows.

- **Perpetual changes stay off the copiable values (CR 707.2).**
  - Handlers no longer exchange the card. They write `perpetualCardBasePowerToughness`, `perpetualCardTypes` / `Subtypes` / `CreatureTypeReplacedCardIds` (through the now package-visible `PerpetuallyGrantCardCharacteristicsEffectHandler.grant`), `grantPerpetualKeywords` and `grantPerpetualAlternateCast`.
  - Covered: Prankster, Little Pig, Muster, Wildgrove, Veko, Skyforge.
  - Layer 7b reads the perpetual base from the card, then the physical card.
  - `isCreatureWithBonus` counts a perpetual creature type off the battlefield (Volatile Rift's trigger filter).
  - Source: mtg.wiki ("a copy uses the original card's characteristics"); no official ruling.
- **Rules readings.** Veko's "becomes a Spirit" replaces the creature types (CR 205.1a). Skyforge's "becomes an artifact creature" keeps the other types (CR 205.1b); fold run 12e's ledger note had cited 205.1a.
- **Type setting.** The CR 205.1a subtype cleanup is shared by `SetCardTypesEffect` and `SetCardTypesUntilEndOfTurnEffect`.
- **Copies.**
  - `BecomeEnchantmentEffect` restores `becomeCreatureBaseCard`.
  - `BecomeCopyOfSubjectEffect` records a copy with no duration when older copies with a duration exist (CR 613.7).
- **Text changes.**
  - `TextChangeTransformer` gained cases for `SequenceEffect`, `HappenedThisWay` and Autumn's Veil's two records.
  - `TriggerIndex.stepCandidates` reads a permanent's own step abilities through `EventAbilitySupport.textChanged`. Before this, upkeep and other step abilities ignored text changes.
- **Readers.**
  - Starfield's override was removed from `isCreatureWithBonus`; layer 4 already applies it.
  - Enter-tapped by type uses `entersWithCardType` (CR 614.12).
  - `printedProtectionEffects` skips protection granted to the host.
  - The hexproof-from-colour grant checks `grantedAbilitySurvivesRemoval` (CR 613.1f).
- **Entry counters.** `ControlledCreaturesEnterWithAdditionalCountersEffect.creatures()` for "[type] creature" wording; `ofChosenSubtype` sets it. Sage, Bramblewood, Oona's and Mimic use it; Giada, Globe and Socialite don't.
- **Energybending** grants all five basic types, also ones the land already has.
- **Seen, not changed:**
  - `BecomeCreatureEffect` (24 cards) swaps the card, so a later copy effect drops its type change.
  - These handlers still exchange cards: `PerpetuallyGrantStaticEffectToTargetCreatureEffectHandler`, `PerpetuallyGrantStaticEffectToSourceEffectHandler`, `DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectsIfCreatureDamagedEffectHandler`, `PerpetuallyBecomeCreatureEffectHandler`, and the card copy in `ExileArtifactThenSeekArtifactAndPerpetuallyBecomeCreatureEffectHandler`.
  - A text-changed targeted trigger gets new effect instances, so its identity-keyed target group may be lost. This was already true for the `ConditionalEffect` / `MayEffect` cases.
  - The enter-tapped filter path judges the entering permanent as it is now.
- **For the next run:** the card-exchanging perpetual handlers above; L29's Ward component; L19's colour, mana-cost and supertype stores; L12's attach continuation.

### Step 9, defect run D20 (2026-11-25, parallel worktree)
Group 9 second quarter: 13 fixed, 8 already fixed, 7 not a defect, 4 left (L12, L14 frontend, L21, Vislor 3+). Ratchet unchanged at 3,007 / 2,433. CR 106.7, 122.5, 400.7f, 603.7d, 603.10a, 603.10c, 603.12, 609.3, 700.9, 701.15a–d and 701.21a checked with `get_rule`.
- **Goad:** `GoadTargetCreatureUntilNextTurnEffectHandler` neither re-adds nor records a creature its controller already goaded for the same duration (and source, for while-source). Kaima counts only new goads (CR 701.15d).
- **Jailbreak:** the made ability has no source permanent (CR 603.7d).
- **Seek:** `SeekCardsToHandEffect.alsoSought(amount, filter)` adds its cards to the earlier SEEK result. Perilous Iteration has one delayed discard.
- **Plunderer's Prize:** its handler delegates the seek to `SeekLibraryEffectHandler`. The card passes through the hand (Cage doesn't apply), gets the creature enter handling, and the mana-value check reads the SEEK permanent.
- **The Ozolith:** moves each kind through `PermanentCounterSupport.moveCounters` (CR 122.5; last-counter events).
- **Could-produce:** `LandManaTypeSupport` evaluates `AwardAnyColorManaEffect` amounts (0 gives no type) and treats `XValue` as producing (costs are ignored, CR 106.7).
- **AI:** the library-and/or-graveyard search strategy keys on `decidingPlayerId()`.
- **Dead code removed:** `MultiGraveyardChoice.reorderToLibraryTop`, `ExileFromHandChoice.untapPermanentId`, `LibrarySearchParams.battlefieldIfManaValueAtMost` / `battlefieldIfChosenPredicate` with their readers, the targeted form of `DealDamageToTargetCreatureOrPlayerEffect`, the duplicate `TargetPolarityClassifier` block and the `GraveyardReturnSupport` orphan Javadoc.
- **Seen, not changed:**
  - The goads `EachOpponentChoosesCreatureToTapAndGoadEffectHandler` and `PutTargetCreatureCardFromControllerGraveyardUnderTargetPlayerEffectHandler` float don't dedupe (CR 701.15d).
  - `ManaSourceColorSupport`'s non-land branch counts any-colour abilities with amount 0.
  - Jailbreak's Torpor Orb check reads the card (ledger note).
  - Several counter handlers treat SILVER as a wildcard like ANY, although Karn's silver counters are real.
  - `git grep -w` misses matches in this worktree; use `/usr/bin/grep -r`.

For the next run: L12 (chosen EXILE destination on `LookAtTopCardsEffect` + `PlotExiledCardsEffect`); the goad dedupe in the two other handlers; a 3-player Vislor test.

### Step 9, defect run D21 (2026-11-25, parallel worktree)
27 group-9 lines: 14 fixed (L8 with L13), 1 already fixed, 2 not a defect, 10 left. Ratchet unchanged at 3,006 / 2,432. CR checked: 106.7, 106.12a, 106.12b, 115.7d, 608.2g, 608.2n, 609.7b, 613.11.
- `GameQueryService.asDamageSourcePredicate(gd, predicate, colors)`: colour leaves are judged on the damage colours while Ghostly Flame applies. Used by `matchesDamageSourcePredicate` and by the combat exempt preventions (`combatDamageExemptPredicate`, `ALL_COMBAT_EXCEPT`).
- `StackIsEmpty` is false while `resolvingEntry` or `pendingEffectResolutionEntry` is set (CR 608.2n).
- `TurnCleanupService`: only a SET change overrides an older "no maximum hand size".
- Piracy on a nonbasic land: one-colour `SpellOnlyManaColorChoice`; the answer completes the parked mana activation, which fires `LandTappedForMana`.
- `AwardManaOfTypeLandsCouldProduceEffectHandler.calculateManaProduction` reports its amount to the land-replacement path.
- Esix: copies of departed permanents count (`lastKnownCopySource`).
- `CardViewFactory`: `needsTarget` is false when an effect's target is chosen at random.
- AI: counter-removal slots follow counter polarity; `PreventNextDamageByTargetCreatureEffect` is HARMFUL unless it doubles.
- Willie Lumpkin: `AttackedTargetIsOpponent` event condition. Rasaad's Radiant face: no enters trigger.
- Rename `SearchOutsideGameOrExileCardChoice` → `OutsideGameCardChoice`.

Seen, not changed:
- Coastal Piracy and other "deals combat damage to an opponent" cards use a to-player trigger with no opponent condition.
- Rasaad, Radiant Monk's specialize trigger can target any exiled creature card, not one "exiled with this creature".
- Many test methods in `RasaadMonkOfSelNeTest`, `TranscantationTest`, `ProtectiveSphereTest` and `TargetPolarityGuardTest` lack `@DisplayName`.

For the next run: L5 with a redirect test; L27 needs a keep-current-target and target-less option on the choose-new-targets step.

### Step 9, defect run D22 (2026-11-25, parallel worktree)
Group 9, last quarter. 16 fixed, 2 already fixed, 3 not a defect, 5 left, 1 not reached. Ratchet 3,005 / 2,431, unchanged.
- **Any-player mana payments** (Rhystic Cave, Scrying, Tutor, Wild Might, Scarwood): after a payment the loop keeps asking (`GameData.anyPlayerMayPayManaPaid`). The payment counts after the last answer. The AI (base and hard) declines a payment already made.
- **Counter choices.** `AdjustChosenCounterOnTargetEffect.optional` (Timebug). `AdjustChosen*Choice.optional` with `options()`, so Clockspinning has no SKIP. The counter-kind REMOVE (Quarry Hauler, Dramatist's Puppet) calls `checkLastTimeCounterRemoved` and `notifyLastCounterRemoved`, as does `RemoveAllCountersEffectHandler`.
- **Counter-replacement order:** a non-target subject resumes through `placeOnNamed`, each permanent with its own amount.
- **Breathkeeper Seraph:** the `MayEffect` is inside the delayed return.
- **Jailbreak:** the delayed ability is recorded with the spell as its source (CR 603.7d) and reads `getPermanentManaValue`.
- **Spell types:** `bindResolutionFilter` and Baral's handler pass `gameQueryService` to `bindTriggeringSpellCardTypes` (Encroaching Mycosynth, perpetual types).
- **Attack taxes:** `RequirePaymentToAttackEffect.includingAttacksOnBattles()` (War Tax); battle attacks are judged for the protector. The three vacuous charge cases now assert leftover mana under `withAutoStop`.
- **Limitless Rekindling:** 59 ECL instant and sorcery printings, read from the card-data cache.
- **AI:** `MultiPermanentChoiceAiStrategy` falls back to `validCardIds`.
- **Meld cards:** `PermanentIsDoubleFacedPredicate` excludes them (CR 712.1). Tetzin and Golden-Gear Colossus no longer count them.
- **Dead code deleted:** `LibrarySearchDestination.CAST_WITHOUT_PAYING_OR_PUT_INTO_HAND` and its handler branch; `GameData.clearExiledByPermanent`.
- **Seen, not changed:** Terminal Velocity's then-effects read `Subjects.source()` for the put permanent (unverified). The hard AI pays "unless any player pays" costs as controller when the value is positive.
- **For the next run:** L14 (multi-pick search casts), L22 (exile links), L15 (step 11), L23.

### Step 9, defect run D23 (2026-11-25)

The owner's perpetual "it" decision (B, taken 2026-11-25) is applied: a change to "it", "this card" or "that card" reaches a
card only while it is still the object the ability refers to (CR 400.7), with the CR's own exceptions (a zone-change trigger
finds the card in the zone it went to, CR 400.7e, 603.6c; a card the same resolution moved, CR 400.7j). Three records folded,
three fixed in place, `PerpetuallyBoostSourceEffect` changed meaning; ratchet 3,002 / 2,428.

**New subjects.** `Subjects.cardsOf(PermanentSubject)` names a permanent's own card while it is still that permanent; over
`triggeringSpell()` the spell's card while the spell is on the stack, never a copy (CR 707.10). `Subjects.sourceObjectCard()`
(`SubjectResolutionService.sourceObjectCard`) names "this card" as the same object: the source permanent, a graveyard or
exile stay (dies triggers too), a hand ability's card, or the permanent this resolution returned. It never names the card a
battlefield ability's permanent became in a graveyard, which the lenient `sourceCard()` with no recorded stay still finds.

**Folds.** The boons (Flaming Fist, Dragonborn, Mephit's, Valiant), Volatile Rift, Darigaaz's Whelp and Tiana on
`PerpetuallyBoostCardEffect.toCards(cardsOf(triggeringSpell()) | triggeringCardsInHand() | cardsOf(triggering()), ...)`
(`PerpetuallyBoostTriggeringCardEffect` gone); Forgeborn Phoenix, Oglor, Gitrog on `PerpetuallyGrantAbilitiesEffect`
(`PerpetuallyGrantAbilityToTriggeringCardEffect` gone); Ichor Aberration, Skyclave Shade on
`AllowCastTargetCardFromGraveyardThisTurnEffect.of(...)` (`AllowCastSourceCardFromGraveyardThisTurnEffect` gone); Emperor
Apatzec, Jaheira, Lulu, Sworn to the Legion on `PerpetuallyGrantKeywordsToTriggeringCardEffect.toCards(cardsOf(...))` (its
any-zone card-id path removed). `PerpetuallyBoostCardEffect(this, ...)` and `PerpetuallyBoostSourceEffect` read
`sourceObjectCard`; Cantor of the Refrain is `PerpetuallyBoostCardEffect(this, 1, 0)`. Rothga, Mutable Pupa and Mephidross
Slime are fixed in place (`blocked-other` for their own shapes).

**Behaviour changes.** A creature spell that left the stack (Rothga, the boons), a card that left the hand (Volatile Rift,
Darigaaz's draw), a source that left the battlefield (Mutable Pupa, Perennial Gravewarden, Darigaaz's Whelp, Wary Zone Guard),
a Vehicle or land that left (Tiana, Gitrog), a card exiled from the graveyard (Forgeborn Phoenix, Oglor, Tomakul Phoenix)
gets nothing. Cantor boosts only the permanent it returned. Mephidross Slime: a Slime exiled from the graveyard in response
is neither doubled nor shuffled (the conjured card still is), and each card's own P/T is doubled (CR 701.10b). Ichor
Aberration's permission from its battlefield trigger no longer lets the card be cast after it dies that turn. Face-down
spells and permanents are logged unnamed by `PerpetualChangeLog` (CR 708.2). Tests rewritten to the decision: Volatile Rift,
Perennial Gravewarden, Wary Zone Guard (2), Wildgrove Summoner, Ichor Aberration; Mephidross Slime's exile test strengthened.

**Seen, not changed** (new defect lines): `PerpetuallyGrantKeywordToSourceEffect` and
`PerpetuallyGrantStaticEffectToSourceEffect` swap the permanent's card for a modified copy (the change is lost on a zone
change); `PerpetuallyBoostSourceEffect`'s graveyard path rewrites printed P/T; Rothga reads a face-down spell's real power;
`RemovePerpetualTriggeredAbilityEffect` and `PerpetuallyGrantKeywordsToStationingCreatureEffect` find their card by id in any
zone. `PerpetuallyRemoveKeywordEffect` has no card users.

**For the next run.** The two card-swapping source grants onto the perpetual stores and `sourceObjectCard`; then step 10 can
fold `PerpetuallyBoostSourceEffect` onto `toCards(sourceObjectCard())`.

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
