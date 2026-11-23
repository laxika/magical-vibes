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
| 9 | Sweep the single-card tail (repeatable) | open. Runs 1 to 41 closed Exile, Each, Return, Put, Reveal, Choose, Grant, Create, Prevent, May, Register and Target. 1,687 rare records have no ledger row. Triage run 1 gave Pay and Reduce (80 rare) and 32 stale step-4/5 blocked rows their verdicts (95 `step-9` rows wait for fold runs). From 2026-11-23 the step runs as **triage runs** (about 80 records each, verdicts only) followed by **fold runs** (see step 9). Open: Perpetually (30 rare) and Conjure's hand and library conjures (Conjure 39 rare) wait for the owner's decisions; run 41 named Destroy (37 rare) as the next family with no owner's question. |
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

Cross-cutting results of those runs, now conventions or shared pieces (Standing knowledge): the face-down hiding rules; stay checks
(CR 400.7, 603.7c) on every return and delayed return; what a put step moves enters through the entry batch (as-enters and Aura host
choices, a card that can't enter stays where it is, CR 101.2, 303.4f); choices drop a permanent that can't be chosen before counting
(CR 115.10a); name prompts offer only names the chooser can know; every prevention sends the damage-prevented event; protection is
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
  permanent, not its card (runs 25, 29, 30, 35).
- A hand choice is shown to its chooser alone and its log names no card; a prompt over exiled cards masks a face-down card from a
  player who may not look at it; a name prompt offers only the catalog's names and names of cards the chooser can know
  (`PlayerInputService.cardNamesOfferedTo`, checked by `ChoiceHandlerService.isCardNameAllowed`); the AI answers from what it can know
  (runs 12 to 16, 27).

**Rules a fold must keep:**

- A card that left a zone and came back is a new object (CR 400.7): returns, delayed returns and "that card" check the card's stay
  (CR 603.7c). Card subjects do this; a handler with its own lookup must too.
- Permanents one instruction moves leave or enter as one event (CR 603.10a, 608.2e); several players choose in APNAP order, then act.
- A choice with one possible answer is made unasked; a permanent that can't be chosen is neither offered, counted nor drawn at random
  (CR 115.10a, 608.2d).
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
