# Effect cleanup: one verb, any subject

A step-by-step plan to stop writing a new effect class for every combination of *what happens* and
*who it happens to*. An effect names its verb (boost, destroy, draw, lose life) and takes a **subject**
(this permanent, the target, the enchanted creature, each opponent, that permanent's controller) as an
ordinary component. One engine service resolves every subject. Effects that fuse two instructions
("discard a card, then untap this") become two steps that talk through the resolution scratchpad.

To start a step, use the prompt at the bottom with the step number. Steps are done in order unless
their "Depends on" says otherwise. Steps marked *repeatable* are run many times, one family per run.

## Status

| Step | Title | Status |
|---|---|---|
| 1 | Inventory script, audit ledger and count ratchet | done |
| 2 | Delete the effects nothing uses | done |
| 3 | Subject types and the one resolver | done |
| 4 | Player verbs take a `PlayerSubject` (repeatable) | in progress: 4a, 4b done |
| 5 | Permanent verbs take a `PermanentSubject` (repeatable) | open |
| 6 | `GrantScope` becomes a `PermanentSubject`, static abilities included | open |
| 7 | Step results: one way for a step to tell the next what happened | open |
| 8 | Split the fused effects (repeatable) | open |
| 9 | Sweep the single-card tail (repeatable) | open |
| 10 | Collapse the head: one record per verb (repeatable, needs sign-off) | open |
| 11 | Close: docs, leftovers, final ratchet | open |

## Why

The engine has one effect record per *sentence shape*, not per *action*. `BoostSelfEffect`,
`BoostTargetCreatureEffect`, `BoostAllOwnCreaturesEffect`, `BoostEquippedCreatureUntilEndOfTurnEffect`
and `BoostReferencedPermanentEffect` are the same action with a different subject, and each has its own
handler. Where a verb did get parameterised, it got its own private enum: there are about 50 "who"
enums in `model/effect/` (`LoseLifeRecipient`, `DamageRecipient`, `DiscardRecipient`, `MillRecipient`,
`PoisonRecipient`, `GrantScope`, `TapUntapScope`, `BounceScope`, `FlickerScope`, `PermanentReference`,
`PhaseOutSubject`, `LibraryOwner`, ...), and each handler resolves its enum with its own `switch`.

That costs code, and it costs rules accuracy, because the copies drift:

- **"Enchanted creature" is looked up three ways.** `BoostReferencedPermanentEffectHandler` reads the
  live source's `attachedTo` and falls back to `StackEntry.attachedPermanentSnapshot`.
  `DestroyReferencedPermanentEffectHandler` reads the source *snapshot* first, then the live source,
  then falls back to the triggering permanent. `TapPermanentsEffectHandler.resolveEnchanted` reads the
  live source, then the source snapshot, and nothing else. 52 handlers carry a private `findAttached` /
  `findEnchanted` / `findReferencedPermanent` helper and 149 handler files call `getAttachedTo()`.
- **"Its controller" has two constants with different lookups.** `LoseLifeRecipient` has
  `DYING_CREATURE_CONTROLLER` (reads `entry.getTriggeringPermanentControllerId()`) and
  `TRIGGERING_PERMANENT_CONTROLLER` (calls `GameQueryService.findTriggeringPermanentController`). Until
  2026-11-19 two draw effects disagreed the same way, so Selvala, Heart of the Wilds asked one player
  and drew for another after a control change (`refactors-to-do.md` entry 14).
- **"This permanent" has two meanings.** `TapUntapScope.SELF` falls back to the entry's target when
  there is no source permanent; `SOURCE_PERMANENT` never does. `BoostSelfEffectHandler` has the
  fallback inline.
- **A new card needs a new class.** "That player draws", "its owner gains", "each opponent mills"
  each need a constant or a class that the verb's enum happens not to have yet.

The second half of the problem is fused effects. `DiscardCardAndUntapSelfEffect` exists because the
untap must happen after a discard prompt, so the pair was written as one effect with a bespoke
continuation (`DiscardFollowUp.untap`). 111 records carry a nested `CardEffect` ("... then X") and
hundreds more hard-code the second half in their handler.

## Measurements (2026-11-19, at `2d1c6d681a`)

Counted by name-token matching over `magical-vibes-card/**/src/main` (32,382 card classes). Step 1
turns the throwaway script into `scripts/effect-inventory.py`; re-measure with it, don't trust these.

| What | Count |
|---|---|
| Types in `model/effect/` | 5,305 (4,979 records, 105 enums, the rest interfaces) |
| Handler classes in `service/effect/normalfx` | 3,879 files, 323,886 lines |
| Records used by exactly one card | 2,980 (1,675 of them have no components at all) |
| ... of which have a handler of their own | 2,407 handlers, 175,808 lines |
| Records used by exactly two cards | 558 |
| Records no card uses, referenced by nothing or only by their own handler | 49 |
| Groups of classes whose names differ only in the subject word | 121 groups, 285 classes, 164 removable |
| Records with a nested `CardEffect` component | 111 (44 more hold a `List<CardEffect>`) |
| Effect names containing `Then` / `And` | 326 / 661 |
| Effects with a `GrantScope` component | 85 (5,581 card files name `GrantScope`; 227 engine files read it) |
| Effects with a `PermanentReference` component | 15 (126 card files) |
| Handlers that `switch` on a recipient / scope / reference component | 62 |
| Application tests named `*EffectHandlerTest` | 153 |

Verb families (classes whose name starts with the verb; "rare" = used by at most two cards):

| Verb | Classes | Rare | Cards using the family |
|---|---|---|---|
| Exile | 421 | 342 | 1,762 |
| Grant | 192 | 130 | 4,806 |
| Return | 174 | 134 | 2,332 |
| Sacrifice | 87 | 60 | 1,079 |
| Destroy | 86 | 70 | 1,687 |
| DealDamage | 83 | 60 | 2,446 |
| Double | 60 | 46 | 130 |
| Mill | 56 | 47 | 526 |
| Boost | 45 | 28 | 3,208 |
| PutCounter(s) | 65 | 37 | 4,312 |
| Draw | 45 | 39 | 3,042 |
| Counter | 44 | 35 | 699 |
| Discard | 43 | 32 | 936 |
| Copy | 43 | 29 | 353 |
| Attach | 40 | 26 | 285 |
| CreateTokenCopy | 35 | 25 | 302 |
| Tap / Untap | 39 | 34 | 1,360 |
| RemoveCounter(s) | 28 | 16 | 208 |
| GainControl | 20 | 17 | 225 |

Rare effects by first word of the name (the tail step 9 sweeps): Exile 342, Each 174, Return 131,
Put 131, Grant 129, Reveal 123, Choose 104, Target 96, Create 83, Prevent 78, Destroy 70,
Perpetually 62, May 60, Sacrifice 60, Deal 60, Register 54, Conjure 51, Mill 47, Double 46, Search 44,
Remove 44, Look 42, Cant 42, Reduce 40, Draw 39, Gain 38, Become 34, Pay 33, Counter 33, Discard 32.

The largest subject-sibling groups found: `*DealsPowerDamageTo*` (7 classes), `Give*RadCounters` (5),
`DealDamageTo*` (5), `DrawCardFor*` (5), `*DealsDamageTo*` (5), `Attach*EquipmentTo*` (5),
`CreateTokenCopyOf*` (4), `Exile*` (4), `CreateTokenAttachedTo*` (4), `*ExilesFromHand` (3),
`Sacrifice*` (3), `DoublePlusOneCountersOn*` (3), `Remove*FromCombat` (3).

**What the numbers say.** Pure subject variants are a small, cheap win (about 160 classes). The mass
is the single-card tail: 2,980 records, most of them a whole card written as one handler. Subjects and
step results are what make that tail expressible; steps 8 and 9 are where most of the code goes away,
and they are the long part of this plan.

## Design

### Words used below

- **Verb**: the action an effect performs. **Subject**: who or what it is performed on.
- **Survivor**: the record a family folds into. It is the family's most used record, kept under its
  current name. **Absorbed**: a record (and its handler) deleted because the survivor now expresses it.
- **Head**: effects used by many cards. **Tail**: effects used by one or two.

### Two subject types

Both are sealed interfaces of small records in `magical-vibes-domain`, built through one factory class
(`Subjects`), in the style of `TargetPredicate` / `TargetPredicates`. They compose, so
"that creature's controller" is `controllerOf(triggering())` instead of a constant per verb.

`PermanentSubject` — which permanents:

| Variant | Meaning | Replaces (examples) |
|---|---|---|
| `source()` | the permanent the ability came from | `SELF`, `SOURCE`, `SOURCE_PERMANENT` |
| `target()` / `target(group)` | the effect's target(s); the only variant that targets | `TARGET`, `TARGETS`, `ALL_TARGETS` |
| `attached()` | what the source Aura or Equipment is attached to | `ENCHANTED`, `EQUIPPED_CREATURE`, `ATTACHED` |
| `triggering()` | the permanent the trigger event was about | `TRIGGERING`, `TRIGGERING_PERMANENT` |
| `controlledBy(PlayerSubject, PermanentPredicate)` | every matching permanent those players control, found once at resolution | `OWN_CREATURES`, `OPPONENT_CREATURES`, `TARGET_PLAYERS_PERMANENTS`, `ALL_CREATURES` |
| step-result variants (step 7) | the permanents an earlier step created, returned or picked | `TOKENS_CREATED_THIS_RESOLUTION`, `RETURNED`, `RETURNED_PERMANENTS` |
| `allOf(subjects...)` | several of the above together | `TARGET_AND_SELF`, `SELF_AND_PAIRED`, `ENCHANTED_AND_AURAS` |

`PlayerSubject` — which players:

| Variant | Meaning | Replaces (examples) |
|---|---|---|
| `you()` | the controller of the spell or ability | `CONTROLLER` |
| `targetPlayer()` / `targetPlayer(group)` | the effect's player target; the only variant that targets | `TARGET_PLAYER` |
| `each(PlayerRelation)` | every player, every opponent, every other player, in APNAP order | `EACH_PLAYER`, `EACH_OPPONENT` |
| `controllerOf(PermanentSubject)` / `ownerOf(PermanentSubject)` | read from the permanent, or from its last-known information | `TARGET_PERMANENT_CONTROLLER`, `TRIGGERING_PERMANENT_CONTROLLER`, `DYING_CREATURE_CONTROLLER`, `SOURCE_CONTROLLER`, `ENCHANTED_PERMANENT_CONTROLLER` |
| event players | the player the trigger event was about, the active player, the defending player, the enchanted player, the player chosen as the source entered | `TRIGGERING_PLAYER`, `ACTIVE_PLAYER`, `DEFENDING_PLAYER`, `ENCHANTED_PLAYER`, `CHOSEN_PLAYER` |

Step 3 fixes the exact variant list after reading every existing enum. A variant is added only when a
card needs it and no composition expresses it.

### One resolver

`SubjectResolutionService` (engine, `service/effect/`) is the only code that turns a subject into
permanents or players. It has two entry points:

- **For a resolving effect**: `(GameData, StackEntry, CardEffect, subject)`. Handlers call it and keep
  no lookup of their own.
- **For a static ability**: `(GameData, source Permanent, subject)`, called by the layered pass. A
  subject that needs a stack entry (`target()`, `triggering()`, a step result) is rejected when the
  effect is added as a static ability.

Its rules, each checked with `get_rule` on 2026-11-19 (check again before writing a number anywhere):

1. **Only a target variant declares a target.** An effect derives `targetSpec()` from its subject, so
   declaration and resolution cannot disagree. Targets are chosen as the object goes on the stack
   (CR 115.1). As it resolves, an illegal target is not affected and a part that needs information
   about it does nothing (CR 608.2b). A subject that is not a target never makes anything fizzle.
2. **A reference reads current information, else last-known information.** If the referenced object
   is still in the public zone it was expected in, its current state is used; otherwise its last-known
   information (CR 608.2h). "Its controller" of a creature that died is the controller it last had.
3. **Nothing is done to an object that left.** A reference to a permanent that is gone affects
   nothing (CR 608.2h for the lookup, CR 609.3 for doing only what is possible); a permanent that left
   and came back is a new object (CR 400.7).
4. **A set is determined once.** "Each creature you control" is found when the effect is applied
   (CR 608.2h). A continuous effect made by a resolution keeps the set it found (CR 611.2c). A static
   ability's set is re-evaluated by the layered pass (`agent-docs/LAYER_SYSTEM.md`).
5. **Several players act in APNAP order.** Their choices are made in APNAP order, then the action
   happens at once (CR 101.4, CR 608.2e). `each(...)` returns players in that order; a verb whose
   handler cannot choose-then-act for several players does not accept `each(...)` (see step 4).

### Equivalence first

A conversion makes the resolver return, for each old constant, exactly what the old handler found.
Where two old constants mean the same thing in the rules but were looked up differently, the step
picks the rules-correct lookup, says so in Findings, and adds a test that fails on the old one. Several
enums read a non-target player out of `entry.getTargetId()` ("baked at trigger time"); the resolver
keeps reading it from there. Moving that data is trigger work, not this plan.

### Survivors keep their names

Until step 10 a family folds into its most used record, which gains a subject component and keeps its
existing constructors as overloads. So a fold touches the absorbed classes' cards (few) and not the
survivor's (hundreds). An old per-verb enum is deleted when its last reader is gone; its card call
sites are rewritten by a script that the step writes, runs and then deletes.

### Step results

Step 7 gives every primitive handler one typed way to record what it did (how many cards, which
objects, whether it happened) on `ResolutionResults`, and three reader families: a `Condition`
("if you do", "this way"), a `DynamicAmount` ("that many", "equal to the damage dealt this way") and a
subject ("those tokens", "the exiled cards"). With that, a fused effect is a `SequenceEffect` of two
primitives, and the fused class goes away.

**A fused effect is split only if splitting is safe.** `SequenceEffect` splices its steps into the
entry's effect list, and a later step runs only after the earlier one finished. That holds when the
earlier step resolves without a prompt, or when its handler pauses *at the end of the step* (the step
model of `leave-battlefield-redesign.md`). A handler that pauses by `rerunCurrentEffectAfterInteraction`
(194 files today) or by a bespoke continuation cannot be an earlier step: its second half would be
dropped or run twice. Such an effect is recorded in the audit ledger as `blocked-step-model` and left.

### Not in this plan

- Moving handlers onto the step model (`leave-battlefield-redesign.md` step 14).
- A generic "each player chooses, then all act" loop for verbs whose handler has no such loop.
- Subjects inside `DynamicAmount` and `Condition` classes (they have the same who-axis; a follow-up).
- `agent-docs/EFFECT_MERGE_PLAN.md` steps 19–29. They stay valid. When a run here absorbs a class one
  of them lists, mark that step there.

## Ground rules for every step

- **Rules accuracy first** (AGENTS.md). Behaviour stays identical unless the old behaviour was
  rules-wrong; then cite the Oracle text or ruling, add a test, and list the change in Findings.
  Check card rulings with the Scryfall MCP `get_rulings`; verify every CR number with `get_rule`
  before writing it in code, a comment, a test name or this file.
- **Verify the premise before editing.** Read the records *and* their handlers. Earlier audits here
  found "identical" handlers differing in a guard, a log line, a `checkWinCondition` call or
  `TargetSpec.harmful`. If a premise does not hold, say so and stop; do not force the merge.
- **Read first**: `agent-docs/ARCHITECTURE.md` whole, then `agent-docs/architecture/resolution.md`;
  for static abilities `agent-docs/LAYER_SYSTEM.md`; for triggers grep `agent-docs/architecture/triggers.md`.
- **Grep every reader of a class before changing it**: engine, `magical-vibes-ai`, networking, the
  application tests, card tests, `agent-docs/`, `.claude/skills/`. AI code reads the capability
  interfaces (`RemovalEffect`, `CreatureBoostEffect`, ...); a survivor keeps implementing every
  capability interface its absorbed classes implemented.
- **Do not regress**: the fixes listed under "Do not regress these fixes" in
  `agent-docs/EFFECT_MERGE_PLAN.md`, and the invariants in `ARCHITECTURE.md` (cards freeze, continuous
  effects are computed, a lasting change is a floating effect with a `Duration`, new game state is
  copied for simulations).
- **Tests.** Add or extend tests for every survivor: resolve real cards through the harness, never
  inspect a card's effect lists. Every test method and `@Nested` class has a `@DisplayName`.
  - Run the test classes of every card that used an absorbed class, the handler and service tests you
    touched, and a sample of the survivor's cards. `scripts/effect-inventory.py --tests-for` prints
    the Gradle arguments.
  - Gradle's `--tests` binds to the task before it: write
    `bash gradlew :magical-vibes-card-test:b:test --tests com.github.laxika.magicalvibes.cards.b.BoneSplintersTest :magical-vibes-card-test:c:test --tests ...`.
    Pass fully qualified class names, never wildcards. Never start a second Gradle run while one is
    going.
  - **Never run the full suite.** Ask for it where a step says so.
- **Docs.** For every absorbed class, update `agent-docs/EFFECTS_INDEX.md`,
  `EFFECTS_QUICK_REFERENCE.md`, `ORACLE_TEXT_EFFECT_MAP.md` and any pattern, template or playbook file
  that names it (grep; never read those files whole).
- **Bookkeeping.** Record each audited class in `scripts/effect-audit-ledger.csv`, lower the baselines
  in `EffectCountRatchetTest` to the new counts, mark the step (or the run) in Status and write what
  later steps need under Findings.
- No banner comments. No commit until asked.

## Steps

### 1. Inventory script, audit ledger and count ratchet

Depends on: nothing.

Write `scripts/effect-inventory.py` (Python 3, standard library only, run as `python3 -I`, in the style
of `scripts/duration-inventory.py`). For every type in `model/effect/` it reports: kind (record, enum,
interface), component list, the number of card classes that name it, the main-code files outside its
own handler that name it (engine, ai, networking, webservice, domain), its handler class and line
count, and the test files that name it. Modes:

- no arguments: the summary table of Measurements above, recomputed;
- `--family <prefix>`: every class whose name starts with the prefix, most used first;
- `--siblings`: groups of classes whose names differ only in a subject word;
- `--dead`: records no card names and no main code outside their own handler names;
- `--unaudited [--family <prefix>]`: classes with no row in the ledger;
- `--tests-for <Class>... [--max-per-class N]`: the Gradle arguments (one test task per letter module,
  each followed by its own `--tests <FQCN>` filters) for the existing test classes of the cards that
  name those effects; with `--max-per-class`, an evenly spaced deterministic sample.

Create `scripts/effect-audit-ledger.csv` with the header `class,verdict,step,note`. Verdicts:
`absorbed` (folded and deleted; note names the survivor), `survivor`, `unique` (one card, no
composition expresses it; note says why in a few words), `blocked-step-model`, `blocked-player-loop`,
`blocked-other` (note says on what), `dead`.

Add `EffectCountRatchetTest` next to `TargetSpecRatchetTest` (application module, `architecture`
package). It counts the records in `model/effect/` and the handler classes under
`service/effect/{normalfx,mayfx,staticfx}` and fails when either count is above its baseline. Each
later step lowers the baselines.

Record the script's summary under Findings, with any number that differs from Measurements.

### 2. Delete the effects nothing uses

Depends on: 1.

`--dead` lists about 49 records: about 12 that nothing references and about 37 referenced only by their
own handler. For each, confirm that no main code, test, card or reflection constructs it (grep the
simple name across the repository, `build/` excluded), then delete the record, its handler, its tests
and its doc entries. `DrawCardForDamagedPlayerEffect` is one of them. A record that turns out to be
used goes in the ledger as `blocked-other` with the reader named. Interfaces and enums are out of
scope here.

### 3. Subject types and the one resolver

Depends on: 1.

Additive: no card changes behaviour.

1. Read every "who" enum in `model/effect/` (`--family` does not find them; grep
   `enum \w*(Recipient|Scope|Reference|Subject|Owner|Role)`), and the handler `switch` of each. Write
   under Findings one table: old constant → the subject expression that replaces it → how its handler
   finds it today (which `StackEntry` field, which fallback). This table is the specification for
   steps 4 to 6.
2. Add `PermanentSubject`, `PlayerSubject` and `Subjects` to the domain as in Design. Keep the variant
   list as small as the table allows. Constructors reject impossible combinations.
3. Add `SubjectResolutionService` with both entry points, implementing the five rules of Design. It
   reuses `GameQueryService.findTriggeringPermanentController` and the other existing last-known
   lookups; it does not copy them.
4. Settle the known divergences with the rules, card rulings and a test each, and record each decision:
   - the three "enchanted / equipped permanent" lookups (see Why);
   - `source()` when the entry has no source permanent (the `SELF` fallback to the entry's target):
     find which entries rely on it and decide whether the resolver or the caller owns the fallback;
   - `DYING_CREATURE_CONTROLLER` against `TRIGGERING_PERMANENT_CONTROLLER`;
   - an owner that is `null` (`LoseLifeRecipient.OWNER` falls back to the controller).
5. Pilot: the 15 effects with a `PermanentReference` component resolve through the service
   (`Subjects.of(PermanentReference)` as a temporary adapter). Their handlers lose their private
   lookup helpers. Run those effects' card tests.
6. Add `SubjectResolutionServiceTest` (harness tests: each variant, live and after the object left)
   and `agent-docs/SUBJECTS_REFERENCE.md` (one line per variant: wording it stands for, whether it
   targets, whether a static ability may use it). Link it from the `ARCHITECTURE.md` topic table and
   add a three-line invariant to `architecture/resolution.md`: a handler finds its permanents and
   players through `SubjectResolutionService`, never through its own lookup.

### 4. Player verbs take a `PlayerSubject` (repeatable)

Depends on: 3.

One verb per run, in this order. Each run: the survivor's recipient enum is replaced by a
`PlayerSubject` component, the survivor's handler resolves it through the service, the sibling classes
fold in, the enum and the absorbed classes are deleted.

| Run | Verb and survivor | Old enum | Known siblings | Status |
|---|---|---|---|---|
| 4a | Draw: `DrawCardEffect` | none (plus `DrawUpToRecipient`, `UpkeepDrawRecipient`, `EndStepDrawRecipient` to check) | `DrawCardFor{Owner,TargetPlayer,DefendingPlayer,TriggeringPlayer,Player}Effect`, `DrawCardsForEachOpponentOfTriggeringPlayerEffect` | | done |
| 4b | Lose life: `LoseLifeEffect` | `LoseLifeRecipient` (740 cards) | check `--family LoseLife` | done |
| 4c | Gain life: `GainLifeEffect` | `GainLifeRecipient` | check `--family GainLife` | open |
| 4d | Damage to players: `DealDamageToPlayersEffect` | `DamageRecipient` (650 cards) | `DealDamageTo{EndStepPlayer,TriggeringSpellController,EnchantedPermanentController,RandomOpponent,CombatOpponentController}Effect` | open |
| 4e | Discard: `DiscardEffect` | `DiscardRecipient` (683 cards) | `--family Discard`, `TargetPlayerDiscards*`, `EachOpponentDiscards*` | open |
| 4f | Mill: `MillEffect` | `MillRecipient` (419 cards) | `--family Mill` | open |
| 4g | Player sacrifices: `SacrificePermanentsEffect` | `SacrificeRecipient` (301 cards) | `--family Sacrifice`, `TargetPlayerSacrifices*`, `EachPlayerSacrifices*` | open |
| 4h | Player counters: poison, rad, energy, experience | `PoisonRecipient` | `Give{TargetPlayer,Controller,EachPlayer,EachOpponent,DefendingPlayer}RadCountersEffect` | open |
| 4i | The small ones: `SetLifeTotalRecipient`, `SkipRecipient`, `EmblemRecipient`, `LibraryOwner`, `LibraryScope`, `GraveyardOwner`, `DestroyRecipient`, `DraftCardRecipient`, `AnteRecipient`, `FractionalLifeLossRecipient` | each | their own families | open |

Notes for every run:

- `refactors-to-do.md` entry 14 describes run 4a; remove the entry when 4a is done.
- A targeting sibling (`DrawCardForTargetPlayerEffect`, 152 cards, about 12 engine readers) is folded
  last in its run, and `targetSpec()` of the survivor must return for `targetPlayer()` exactly what
  the sibling declared (benign or harmful, player predicate, target group).
- `each(...)` is accepted by a verb only if its handler already makes every player's choice in APNAP
  order and then acts at once. Where the "each player" variant is a separate class whose handler has
  its own loop, either move that loop into the survivor's handler or leave the class and record it as
  `blocked-player-loop`.
- Keep each handler's amount context: `DealDamageToPlayersEffect` and `LoseLifeEffect` evaluate a
  player-relative amount once per receiving player for the multi-player recipients.
- The card call sites of a deleted enum are rewritten with a script (constant → `Subjects` call,
  import fixed). Check the diff of ten files by eye, compile every letter module you touched, delete
  the script. Ask for a full card-test run after a run that rewrote more than 300 card files.

### 5. Permanent verbs take a `PermanentSubject` (repeatable)

Depends on: 3. Independent of step 4.

Same shape as step 4, for verbs that act on permanents. A run takes one verb: list it with
`--family` and `--siblings`, pick the survivor (the most used record), fold the classes that differ
only in subject, replace the verb's scope enum if it has one.

| Run | Verb | Old enum | Known siblings (not exhaustive) | Status |
|---|---|---|---|---|
| 5a | Destroy | `PermanentReference` users | `DestroyTargetPermanentEffect`, `DestroyReferencedPermanentEffect`, `--family Destroy` | open |
| 5b | Exile a permanent | none | `Exile{TargetPermanent,Self,EnchantedCreature,EquippedCreature}Effect` | open |
| 5c | Return to hand | `BounceScope` | `ReturnToHandEffect`, `Return*ToHand*` | open |
| 5d | Tap and untap | `TapUntapScope` (1,419 cards) | `--family Tap`, `--family Untap` | open |
| 5e | Put counters | `PermanentReference` users | `PutCountersOnSelfEffect`, `PutCounterOnTargetPermanentEffect`, `PutCounterOnReferencedPermanentEffect`, `DoublePlusOneCountersOn{TargetCreature,Source,EnchantedCreature}Effect` | open |
| 5f | Remove counters | `CounterRemovalSubject` | `--family RemoveCounter` | open |
| 5g | Damage to permanents | `DamagedPermanentScope` | `DealDamageTo{TargetCreature,Source,TriggeringPermanent,EnchantedCreature,TargetPermanent}Effect` | open |
| 5h | A creature deals damage | none | `*DealsPowerDamageTo*` (7), `*DealsDamageTo*` (5) | open |
| 5i | Boost (one-shot) | `PermanentReference` users | `Boost{Self,TargetCreature,EquippedCreatureUntilEndOfTurn,ReferencedPermanent,TargetPermanent}Effect` | open |
| 5j | Sacrifice a named permanent | none | `Sacrifice{Self,EnchantedCreature,TargetCreature}Effect` | open |
| 5k | Flicker, phase out, remove from combat | `FlickerScope`, `PhaseOutSubject`, `PhaseOutScope` | `Remove{Target,Self,Source}FromCombatEffect` | open |
| 5l | Attach and token copies | none | `Attach*EquipmentTo*` (5), `CreateTokenCopyOf*` (4), `CreateTokenAttachedTo*` (4) | open |
| 5m | Whatever `--siblings` still lists | — | — | open |

Notes for every run:

- Do not merge the high-use siblings into each other here (`BoostSelfEffect` with
  `BoostTargetCreatureEffect`); that is step 10. Here each of them gains what it needs to absorb its
  rare siblings, and the rare ones go.
- A removal verb keeps its departure path: `DepartureService.departAsStep`, `StepDepartureSupport`,
  `DestructionSupport.destroyBatch` for simultaneous deaths (`architecture/resolution.md`,
  `architecture/replacement-damage.md`). A subject that yields several permanents removes them as one
  batch.
- A harmful verb's `targetSpec()` stays `harmful` for `target()`; a non-target subject declares no
  target, so protection does not stop it.
- 5i covers the boosts of one named permanent. `StaticBoostEffect` (it carries a `GrantScope`) and the
  set-wide `BoostAllOwnCreaturesEffect` / `BoostAllCreaturesEffect` (a predicate and no scope, used
  one-shot and static) wait for step 6.

### 6. `GrantScope` becomes a `PermanentSubject`, static abilities included

Depends on: 3, and 5i.

`GrantScope` has 28 constants, 85 effects carry it, 5,581 card files name it and 227 engine
files read it (`LayerSystemService`, `GameQueryService` and `StaticEffectSupport` about 36 times each).
It mixes single references (`SELF`, `TARGET`, `ENCHANTED_CREATURE`) with sets (`OWN_CREATURES`,
`ALL_CREATURES_INCLUDING_SELF`, `OPPONENT_LANDS`), and the "other / including self" pairs encode a
predicate in the constant name.

1. Inventory: for each constant, the subject expression (a set is `controlledBy(players, predicate)`;
   "other" is a not-the-source predicate, which `TapUntapScope`'s Javadoc says exists: find it, don't
   add a second) and every engine reader. Add it to Findings.
2. Give `GrantScope` one method returning its `PermanentSubject` and move the engine readers onto the
   resolver, one file family per sub-run (one-shot handlers; `staticfx` and `StaticEffectSupport`;
   `LayerSystemService` and `GameQueryService`; the AI). The static path uses the resolver's static
   entry point. A new game-state input to the layered pass goes into
   `LayerSystemService.computeBoardFingerprint`.
3. When no engine code reads a constant directly, the 85 effects take a `PermanentSubject` component,
   the cards are rewritten by script and `GrantScope` is deleted.
4. Rule 4 of Design is the risk: a one-shot "creatures you control get +1/+1 until end of turn" locks
   its set at resolution (CR 611.2c), a static "creatures you control get +1/+1" does not. Add a test
   of each for at least Boost, GrantKeyword and Animate, with a creature entering after the effect
   began.

Ask for a full card-test run after sub-step 2 and again after sub-step 3. Run the application
`layers` tests yourself.

### 7. Step results: one way for a step to tell the next what happened

Depends on: 3.

1. Inventory how a later effect learns what an earlier one did today. Known channels on `StackEntry`:
   `resolutionResults` (departures, picked ids, drawn cards, countered, copied), `eventValue`
   (`setEventValue`), `createdPermanentIds`, `resolutionExiledCardIds`, `drawnCardIdsThisResolution`,
   `returnedPermanentIds`, `acceptedPlayerIds`, `playersDealtDamageThisResolution`,
   `permanentsDealtDamageThisResolution`, `counteredPermanentIdsThisResolution`, `sacrificedCard`; and
   the readers: conditions (`PermanentLeftThisWay`, `CardDrawnThisWay`, `SpellCounteredThisWay`,
   `SpellCopiedThisWay`), amounts (`EventValue`, `CardsDrawnThisResolution`,
   `PermanentLeftThisWayPower`), scopes (`TOKENS_CREATED_THIS_RESOLUTION`, `RETURNED`). List writer and
   reader of each under Findings.
2. Design the typed record of a step's outcome on `ResolutionResults` (which objects, how many, did
   it happen) and say which existing channels it replaces now and which stay. It stays immutable and
   is copied for simulations (`GameDataDeepCopyTest`). A step records its result even when nothing
   happened, so a later step never reads an older step's result (the existing rule for departures).
3. Add the three reader families: a `Condition` for "if you do / this way", a `DynamicAmount` for
   "that many / that much", and `PermanentSubject` / `PlayerSubject` variants for "those permanents /
   that player". Reading follows CR 608.2c (instructions in written order) and CR 608.2h (last-known
   information of what left).
4. Make the head primitives record their results: draw, discard, mill, exile, destroy, sacrifice,
   return, create token, deal damage, gain and lose life, put and remove counters. Add a test per
   primitive with a real card pair ("X. If you do, Y" and "X, then Y equal to the number").
5. Write under Findings the list of handlers that may be an *earlier* step: those that never prompt
   and those that pause at the end of a step. Everything else is `blocked-step-model` as an earlier
   step. Add this rule to `architecture/resolution.md`.

### 8. Split the fused effects (repeatable)

Depends on: 7, and the step-4 or step-5 run of the verbs involved.

A fused effect does two things the engine already has primitives for. One run takes one family:
`--unaudited --family <first word>` restricted to names containing `Then`, `And`, `IfYouDo` or
`Unless`, or to records with a nested `CardEffect`. Start with the wrappers that already take a
`thenEffect` (`DestroyTargetPermanentThenEffect`, `ExileTargetPermanentThenEffect`,
`ReturnTargetPermanentToHandThenEffect`, `DiscardCardThenEffect`, `DrawCardsThenEffect`,
`MillControllerThenEffect`, `CreateTokenThenEffect`, `ExileSelfThenEffect`, ...): their
`ThenEffectRecipient` becomes a subject on the second step (`controllerOf` / `ownerOf` of what left).

For each class, read the record and the handler and give a verdict:

- **split**: the card becomes `SequenceEffect.of(first, second)` (or flat `addEffect` calls on a plain
  spell), the second step reading the first's result. Delete the class and handler.
- **blocked-step-model**: the first half prompts and its handler is not on the list from step 7.
- **blocked-player-loop**: it needs several players to choose and then act together.
- **unique**: the two halves are not separable in the rules (later text changes the meaning of
  earlier text, CR 608.2c), or a half has no primitive and is used by this card only.

Rules to hold on to:

- "If you do" is gated on the first step having happened, read from its result. `SequenceEffect` has
  no gate of its own; the gate is a `ConditionalEffect` step (see `SequenceEffect`'s Javadoc and the
  `SacrificeSelfThenEffect` row in `EFFECT_MERGE_PLAN.md`).
- "X and Y" in one sentence is often simultaneous (one damage event, deaths together): use
  `SequenceEffect.simultaneous` or the survivor's batch form, not two sequential steps.
- Targets: a fused effect with one target becomes steps that share the entry's target; with two
  targets, each step names its target group. Check that the cast-time target prompt is unchanged.
- A trigger slot holds one effect, so a split on a trigger slot stays one `SequenceEffect`.

A run stops at about 15 split classes. Record the verdict counts and the blocked classes' common
cause under Findings.

### 9. Sweep the single-card tail (repeatable)

Depends on: 7. Gets better after each run of steps 4, 5 and 8.

One run takes one first-word family, largest first (see Measurements): `--unaudited --family Exile`,
then `Each`, `Return`, `Put`, `Grant`, `Reveal`, `Choose`, `Target`, `Create`, `Prevent`, ... A run
audits about 40 classes and gives each a verdict, in this order of preference:

1. **absorbed, by parameters**: an existing effect expresses it with a subject, a `DynamicAmount`, a
   predicate, a `Duration`, a `Condition` or a `MayEffect` / `ConditionalEffect` / `ChooseOneEffect`
   wrapper. Rewrite the card, delete the class and handler.
2. **absorbed, by one new parameter**: an existing effect would express it and at least two other
   audited classes with one more component (not a boolean that only this card sets). Add the
   component, fold all of them.
3. **split**: as in step 8.
4. **blocked-***: as in step 8.
5. **unique**: a card whose text has no reusable part (Karn's restart, Word of Command, a subgame).
   It stays. Note the reason in the ledger in a few words.

Guard rails:

- Never add a parameter that only one card sets; that moves the special case instead of removing it.
- Before deleting a handler, read it to the end. Tail handlers hide rules knowledge: a guard, a
  last-known read, a trigger it fires, an "as much as possible" branch (CR 609.3). The replacement
  must do each of them, or the class is not absorbed.
- Check the card's Oracle text with `get_card` and its rulings with `get_rulings` whenever the
  handler does something its name does not say.
- The log line of the survivor wins. A test that asserted the old line is updated; say so.

Each run ends with the family's row in Findings: audited, absorbed, split, blocked (by cause), unique,
left unaudited. The step is done when `--unaudited` lists no record used by fewer than three cards.

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

### 11. Close: docs, leftovers, final ratchet

Depends on: everything above that is going to be done.

- Delete the temporary adapters (`Subjects.of(PermanentReference)`, `GrantScope`'s subject method) and
  any per-verb enum left without readers.
- `agent-docs`: the effect indexes describe verbs with a subject column; `SUBJECTS_REFERENCE.md` is
  complete; `CARD_IMPLEMENTATION_PLAYBOOK.md`, the templates and the `implement-card` / `review-card`
  skills say "pick the verb, then the subject; compose fused text from steps" and name no deleted
  class.
- Set `EffectCountRatchetTest` to the final counts. Keep `scripts/effect-inventory.py` and the ledger:
  they tell a card author whether an effect already exists.
- Move what is still `blocked-*` in the ledger to `refactors-to-do.md` (or to the step-model plan) as
  entries grouped by cause, then delete this file.

## Findings

### Step 1 (2026-11-19, at `2d1c6d681a` plus the step's files)

`scripts/effect-inventory.py` summary, against Measurements:

| What | Script | Measurements | Why it differs |
|---|---|---|---|
| Types in `model/effect/` | 5,305: 4,979 records, 69 enums, 255 interfaces, 2 classes | 5,305 (105 enums) | the script counts top-level types only; about 48 more enums are nested in 41 files |
| Handler files in `normalfx` | 3,879 files, 323,886 lines | same | |
| Records used by exactly one card | 2,980 (1,675 with no components) | same | |
| ... their own handlers | 2,571 handlers, 184,667 lines | 2,407, 175,808 | the script maps a handler by its `handledEffect()`, in `normalfx`, `mayfx` and `staticfx` |
| Records used by exactly two cards | 552 | 558 | |
| Dead records | 47: 9 named by nothing, 38 only by their own handler | 49 (12 + 37) | |
| Subject-sibling groups | 191 groups, 474 classes, 283 removable | 121, 285, 164 | the script's subject words include the player subjects (`Opponents`, `Players`, `Controller`, `ActivePlayer`, ...) |
| Records with a nested `CardEffect` component | 191 (36 more hold a `List<CardEffect>`) | 111 (44) | the script parses multi-line record headers; a grep of component declarations agrees (191) |
| Names with `Then` / `And` | 326 / 654 | 326 / 661 | |
| `GrantScope` | 62 records have the component; 87 effect types name it; 5,581 card files; 234 engine files | 85; 5,581; 227 | 25 of the 87 use it only inside a method, not as a component |
| `PermanentReference` component | 15 (126 card files) | same | |
| Handlers that `switch` on a subject component | 63 | 62 | |
| `*EffectHandlerTest` | 153 | same | |
| Ratchet: records / handler classes | 4,979 / 4,205 | | `EffectCountRatchetTest` baselines |

Verb families (records and effect classes; "rare" = at most two cards, zero included): Exile 436/346/1,983 cards,
Grant 191/129/4,411, Return 177/134/2,344, Sacrifice 104/68/3,097, Destroy 86/70/1,665, DealDamage 83/60/2,312,
Double 60/46/128, Mill 57/47/533, Boost 45/28/3,171, PutCounter(s) 49/29/2,793, Draw 45/39/3,003, Counter 36/27/692,
Discard 51/34/1,299, Copy 43/29/348, Attach 36/24/175, CreateTokenCopy 35/25/284, Tap/Untap 45/36/1,533,
RemoveCounter(s) 26/13/444, GainControl 21/18/223. A family is matched on a camel-word boundary
(`Sacrifice` takes `SacrificeSelf`, not `Sacrifices...`); `--family` matches the same way.

For later steps:

- **Step 2.** `--dead` also lists the dead records whose handler other main code names as a bean (10 of the 38):
  `ChooseAttackTargetsForCreatedTokensEffectHandler` is called by four token handlers and
  `PermanentChoiceTriggerHandlerService`; `EachPlayerReturnsPermanentToHandEffect`,
  `EachPlayerChoosesCreatureThenSacrificesRestEffect`, `OpponentChoosesCreatureTheyControlTokenCopyEffect`,
  `ReboundCastFromExileEffect`, `PutExiledCardsIntoOwnersGraveyardEffect`,
  `PutStillExiledCardsIntoGraveyardAndCreateTokensEffect`, `TargetPlayerChoosesCardsFromHandThenMayCastOneEffect`
  are the rest, plus `AnyPlayerMayTakeDamageOrDrawTargetPlayerEffect` and
  `DamageTargetPlayerOrPlaneswalkerUnlessPaysEffect`, whose `normalfx` and `mayfx` handlers name each other (a
  dead pair). For the first eight the handler's code is still used by others; deleting them is a move, not a delete.
- **Counting.** "Names it" is a whole-word match of the simple name anywhere in the file, comments included, so
  the script errs towards "used". Two effects live outside `model/effect/`
  (`magical-vibes-card/core/.../effect/{l,w}`: `LivingDeathEffect`, `WarpWorldEffect`) and are not inventoried.
- **`--tests-for`** prints one line of Gradle arguments: each effect's own `<Effect>Test` /
  `<Effect>HandlerTest` under `:magical-vibes-application:test`, then one task per card-test letter module with
  its `--tests` filters. Cards without a `<Card>Test` class are listed on stderr (57 of `DrawCardEffect`'s 2,772).
- **Ledger.** `scripts/effect-audit-ledger.csv` has its header only: step 1 audits no class.

### Step 2 (2026-11-19)

Deleted 39 records (9 named by nothing, 30 named only by their own handler) and their 32 handlers (30 in
`normalfx`; the dead pairs `AnyPlayerMayTakeDamageOrDrawTargetPlayer*` and `DamageTargetPlayerOrPlaneswalkerUnlessPays*`
also had a `mayfx` handler). Ratchet: 4,940 records, 4,173 handler classes. Every deleted name was grepped across all
tracked files (engine, AI, networking, webservice, frontend, cards, tests, docs); no reflection, registry list or
Jackson subtype list names effects by string. Tests: `DrawCardForDamagedPlayerEffectHandlerTest` deleted;
`GameQueryServiceTest`'s Goblin Goliath case (the only user of `DoubleControllerDamageToOpponentsEffect`) deleted;
`TurnProgressionServiceTest` now uses `OpponentExtraTurnSkipReplacementEffect`, which is identical to the deleted
`SkipExtraTurnReplacementEffect`. Many agent-docs rows still described cards with the deleted classes although the
cards had moved to other effects; those rows now name what the card uses.

Kept as `blocked-other` (8), all listed by `--dead`:

- Built at runtime, so not dead: `ChooseAttackTargetsForCreatedTokensEffect`, `PutExiledCardsIntoOwnersGraveyardEffect`,
  `PutStillExiledCardsIntoGraveyardAndCreateTokensEffect`, `ReboundCastFromExileEffect`. Their handler builds the
  record in a public method other handlers call. `--dead` cannot see this; it should not list a record its own handler
  constructs outside `resolve`.
- Record unused, handler used as a service: `EachPlayerReturnsPermanentToHandEffect` (Words of Wind's draw
  replacement calls `beginReplacement` / `completeChoice` from `DrawService` and `MultiPermanentChoiceHandlerService`).
- Whole flow dead, but more than a handler: `EachPlayerChoosesCreatureThenSacrificesRestEffect`,
  `OpponentChoosesCreatureTheyControlTokenCopyEffect`, `TargetPlayerChoosesCardsFromHandThenMayCastOneEffect`. Only
  their own `resolve` creates the choice context / pending interaction that their completion handles, so the
  completion branch in the input service, the context record and (for the last) the `PendingInteraction` variant,
  AI strategy and prompt projection are dead too. Deleting them is a multi-file removal for step 9 or 11.

For later steps:

- Interfaces left with no implementer (out of scope here): `CastTimeXValueModifierEffect` (read by
  `SpellCastingService` and `ValidTargetService`); `ControllerRecipientDamageMultiplyingEffect.appliesToOpponentPermanents()`
  has no overrider, so its `false` branch in `GameQueryService.getDamageToRecipientMultiplier` is unreachable.
  `AttackerTargetRestrictionEffect` is down to `CantAttackControllerEffect`. Step 11 should sweep interfaces and enums
  with no implementer or reader.
- `EFFECT_MERGE_PLAN.md` step 20's `SacrificeCreatureForTokenEffect` row is marked moot (one of its two classes was dead).

### Step 3 (2026-11-19)

Added `PermanentSubject`, `PlayerSubject` and `Subjects` (domain, `model/effect/`, sealed interfaces of records, not
counted by the ratchet) and `SubjectResolutionService` (engine, `service/effect/`) with the two entry points of
Design: `permanents` / `permanent` / `permanentIds` / `players` / `player` for a resolving entry, and `permanents` /
`players` for a static ability's source (a subject with `needsStackEntry()` is rejected there). Variants:
`source()`, `target([group])`, `attached()`, `triggering()`, `returned()` (the one step-result variant, needed by the
pilot), `controlledBy(players, filter)`, `allOf(...)`; `you()`, `targetPlayer([group])`, `eachPlayer()`,
`eachOpponent()`, `controllerOf(p)`, `ownerOf(p)`, `triggeringPlayer()`, `activePlayer()`, `defendingPlayer()`,
`enchantedPlayer()`, `chosenPlayer()`. `permanentIds` exists because some verbs act on last-known information of a
permanent that left (an explore, CR 701.44c; "block it" names an id). Records, handlers: ratchet unchanged
(4,940 / 4,173).

**Pilot.** The 15 effects with a `PermanentReference` component resolve it through `Subjects.of(PermanentReference)`;
the private `findReferencedPermanent` / `findAttached` / `findPermanentByCardId` helpers of `BoostReferenced`,
`DestroyReferenced`, `PutCounterOnReferenced`, `RemoveCounterFromReferenced`, `RemoveReferencedPermanentFromCombat`,
`ShuffleReferencedPermanentIntoLibrary`, `PutOrRemoveCounterIfAttackedOrBlockedSinceLastUpkeep`,
`RemoveAllCountersThenDestroyReferencedPermanentAndDamageController`, `Explore`, `MustBlockSource`,
`GoadTargetCreatureUntilNextTurn`, `TurnSourceFaceUp`, `DoublePlusOneCountersOnSource` and `SacrificeSelf` handlers are
gone; `UntapStepService` reads `DoesntUntapWithCounterEffect.counterBearer` through the static entry point. Not
converted (they name `PermanentReference` but are not one of the 15): `PredicateEvaluationService`'s
`PermanentBlockingSourcePredicate.blocked()`, and the handlers that only construct a referenced effect
(`DestructionSupport`, `ForcedCostOrElse`, `DestroyUnlessPaysPerCounter`, `PayXManaPutCountersOnSelf`, ...).

**Divergences settled.**

- *Enchanted / equipped permanent.* `attached()` is: the live source's `attachedTo` while the source is on the
  battlefield (nothing if it is unattached), else the source's last-known snapshot's `attachedTo`, else (no snapshot
  recorded) `StackEntry.attachedPermanentSnapshot`; then only if that permanent is still on the battlefield. Basis:
  CR 608.2h (current information while the object is in the expected zone, else last-known). `PutCounterOnReferenced`
  already did this. Changed: `DestroyReferenced` read the activation-time snapshot first (so an Aura moved after
  activation destroyed its old host) and fell back to the triggering permanent; `BoostReferenced` and
  `PutOrRemoveCounter...` fell back to `attachedPermanentSnapshot` while the source was still on the battlefield
  unattached; `RemoveCounterFromReferenced` used the snapshot's `lastAttachedTo`; `RemoveFromCombat`,
  `RemoveAllCountersThenDestroy...` and `Explore` had no last-known branch; `Shuffle` had no
  `attachedPermanentSnapshot` branch. Destroy's snapshot-first order was standing in for a second rules concept:
  "destroy it" / "destroy that creature" in an Aura's trigger about its creature means the creature the event was
  about, fixed when it triggered (Oracle text of Dwindle M19 53, Spinal Graft TMP 159, Cryoshatter EOE 53; no ruling says
  otherwise). Those three cards now use `PermanentReference.TRIGGERING` (their collectors record the triggering
  permanent), so `DwindleTest` / `SpinalGraftTest`'s "moving the Aura in response" cases pass unchanged.
  *Paroxysm* (EXO 94) and *Venarian Gold* (LEG 83) ("that creature" in an upkeep trigger of the enchanted
  creature's controller) use the new `attachedAtTrigger()` / `PermanentReference.ATTACHED_AT_TRIGGER`: the host the
  entry's `attachedPermanentSnapshot` recorded when the ability triggered, live only while still on the battlefield.
  Paroxysm's +3/+3 moved to `BoostReferencedPermanentEffect(ATTACHED_AT_TRIGGER, 3, 3)`. The upkeep record was left
  alone: recording the host as the triggering permanent on upkeep/draw `ENCHANTED_CONTROLLER` entries (then
  `TRIGGERING`) first needs `DealDamageToPlayersEffectHandler` (`ENCHANTED_PERMANENT_CONTROLLER` recipient) and
  `ForcedCostOrElseEffectHandler` (`payerIsEnchantedController`) guarded, since both read a set
  `triggeringPermanentId` as "its current controller" and would move "that player" off the upkeep player; whichever
  step-4 run touches them does that. Tests: the "Aura moved in response" cases in `ParoxysmTest` / `VenarianGoldTest`,
  `SubjectResolutionServiceTest$AttachedAtTrigger`. Other tests: `PoolingVenomTest.abilityDestroysTheLandEnchantedAtResolution`
  (fails on the old Destroy lookup), `SubjectResolutionServiceTest$Attached`.
- *`source()` without a source permanent.* Strict: the entry's `getSourcePermanentId()` only. The `SELF` fallback to
  the entry's target (`TapPermanents`, `UntapPermanents`, `BoostSelf`, `RemoveAllCounters(SOURCE)`,
  `RemoveCounterAndGainLife`, about 30 `...Self...` handlers) is legacy: every permanent's activated and triggered
  ability has a source (`TriggeredAbilityStackService.abilityEntry`, `payCostsAndActivate`; an Aura's "attached"
  ability puts the host in `targetId` but keeps the source), and no card puts those effects on a source-less entry
  (spell, cast trigger, graveyard/emblem trigger). On such an entry the fallback acted on the spell's real target,
  which is wrong. So the resolver owns no fallback and no caller needs one; the steps that convert those handlers drop
  theirs and make `TapUntapScope.SOURCE_PERMANENT` the same as `SELF`. Test: `SubjectResolutionServiceTest$Source`.
- *`DYING_CREATURE_CONTROLLER` vs `TRIGGERING_PERMANENT_CONTROLLER`.* Both are `controllerOf(triggering())`, resolved
  by `GameQueryService.findTriggeringPermanentController` (current controller, then
  `entry.lastKnownPermanentController`, then `getTriggeringPermanentControllerId`). For every death trigger they give
  the same player: the death collectors (`ZoneChangeRequests.zoneChangeRecord`, `TriggeredAbilityStackService.aboutRecord`)
  record the controller as it left, and nothing is live. Test:
  `SubjectResolutionServiceTest$ControllerAndOwner.controllerAfterControlChangeAndDeath` (a stale recorded controller
  loses to the controller it had as it left, CR 608.2h).
- *An owner that is `null`.* Every object has an owner (CR 108.3 cards, CR 111.2 tokens, CR 110.2); a `null` owner is
  an unstamped engine-made or hand-built test card. `ownerOf` reads `gameData.ownerOf(card)` (honours
  `cardOwnerOverrides`, unlike `LoseLifeRecipient.OWNER`'s raw `Card.getOwnerId()`), else
  `gameData.defaultControllerOf` while the permanent is on the battlefield (stolen-creature record, else the battlefield
  it is on), else its last-known controller. `LoseLifeRecipient.OWNER` and `EACH_OTHER_PLAYER` fall back to the
  entry's controller; for the source's own abilities that is the same player. Test:
  `SubjectResolutionServiceTest$ControllerAndOwner.unrecordedOwner`.
- `returned()` reads the entry's target, else its first targeted card (`BoostReferenced`, `PutCounterOnReferenced`);
  `Destroy`, `Explore` and the others read only the target. No card puts those on an entry with only targeted cards.

**Resolver choices later steps must check per card** (not divergences of the pilot, so not settled here):
`triggeringPlayer()` is `entry.getTriggeringPlayerId()`, else the entry's non-targeting `targetId` when it is a player;
`activePlayer()` is `gameData.activePlayerId` (live); `enchantedPlayer()` is the source's `attachedTo` as it is or last
was, else the baked `targetId` only when no source is known; `chosenPlayer()` has no `targetId` fallback; `each(...)`
and every multi-player result is rotated APNAP order (active player, then turn order), where most handlers today use
raw `orderedPlayerIds` (`SkipNext` even the unordered `playerIds`); `controllerOf(source())` falls back to
`entry.getSourcePermanentControllerId()`.

#### Old constant → subject

Columns: the constant, the subject that replaces it, and how its handler finds it today (getter → fallback). "baked"
= a non-targeting player a trigger collector wrote into `entry.getTargetId()`. "LKI chain" =
`findPermanentController` → `entry.lastKnownPermanentController` → `getTriggeringPermanentControllerId` (the
`findTriggeringPermanentController` order). **New** marks a variant the run that needs it adds (smallest form given).
A constant with no subject is a quantity, mode or filter that stays a component.

**LoseLifeRecipient** (`LoseLifeEffectHandler`; seat order, not APNAP)

| Constant | Subject | Today |
|---|---|---|
| CONTROLLER | `you()` | `getControllerId()`; mana path: activating player |
| OWNER | `ownerOf(source())` | `entry.getCard().getOwnerId()` raw → controller; amount context re-based to owner |
| SOURCE_CONTROLLER | `controllerOf(source())` | live → `getSourcePermanentControllerId()`; dead (no card, no engine use) |
| TARGET_PLAYER | `targetPlayer()` | `targetsForEffect` → `getTargetId()` → `getTargetIds().first`; amount per target |
| TRIGGERING_PLAYER | `triggeringPlayer()` | baked → `getTargetIds().first`; counts as targeted for `recordTargetLifeLost` |
| TARGET_PERMANENT_CONTROLLER | `controllerOf(target())` | live → `removedPermanentControllers` → triggering controller if target is the triggering permanent |
| DYING_CREATURE_CONTROLLER | `controllerOf(triggering())` | raw `getTriggeringPermanentControllerId()` (same player, see above) |
| TRIGGERING_PERMANENT_CONTROLLER | `controllerOf(triggering())` | `findTriggeringPermanentController` |
| TRIGGERING_CARD_OWNER | **new** `ownerOf(triggeringCard())` (spell, land played, or permanent) | `findTriggeringCardOwner` → `getTriggeringPermanentOwnerId()` → `findTriggeringPermanentOwner` |
| DEFENDING_PLAYER | `defendingPlayer()` | `getDefendingPlayerId()` → `getAttackedTargetId()` (player, or planeswalker's live controller; resolver uses `defendingPlayerOfAttackTarget`, which also handles battles) |
| OTHER_CHOSEN_PLAYER | **new** `except(chosenPlayers(), triggeringPlayer())` | source live → snapshot, `getRememberedTargetPlayerIds()` minus `getTriggeringPlayerId()` |
| ACTIVE_PLAYER | `activePlayer()` | `entry.getActivePlayerId()` → `gameData.activePlayerId` |
| EACH_PLAYER | `eachPlayer()` | seat order, minus `exemptIfControls` matches |
| EACH_OPPONENT | `eachOpponent()` | seat order, minus controller and `exemptIfControls` |
| EACH_OTHER_PLAYER | **new** `except(eachPlayer(), ownerOf(source()))` | seat order minus `getCard().getOwnerId()` → controller |

**GainLifeRecipient** (`GainLifeEffectHandler`)

| Constant | Subject | Today |
|---|---|---|
| CONTROLLER | `you()` | `getControllerId()`; `targetsPlayer` flag declares an unused player target (keep it as its own component) |
| TARGET_CONTROLLER | `controllerOf(target())` | `findPermanentById` + battlefield scan → `removedPermanentControllers`; no triggering fallback |
| OPPONENT | `targetPlayer(group)` with an opponent filter | `getOpponentId` (two players), own shroud/hexproof check; no declared target |
| TRIGGERING_PLAYER | `triggeringPlayer()` | baked, must be a player |
| TRIGGERING_PERMANENT_CONTROLLER | `controllerOf(triggering())` | inline copy of the LKI chain |
| SOURCE_REMEMBERED_PLAYER | `chosenPlayer()` | source live → snapshot `getRememberedTargetPlayerId()` → `targetId` of a stack entry from the same source carrying `RememberTargetPlayerEffect` (the resolver lacks this last fallback) |
| EACH_PLAYER | `eachPlayer()` | seat order |

**PoisonRecipient** (`GivePoisonCountersEffectHandler`)

| Constant | Subject | Today |
|---|---|---|
| CONTROLLER | `you()` | `getControllerId()` |
| TARGET_PLAYER | `targetPlayer()` | `getTargetId()` only |
| EACH_PLAYER / EACH_OPPONENT | `eachPlayer()` / `eachOpponent()` | seat order |
| TARGET_PERMANENT_CONTROLLER | `controllerOf(target())` | as LoseLife's |
| TRIGGERING_PERMANENT_CONTROLLER | `controllerOf(triggering())` | `findTriggeringPermanentController` |

**SetLifeTotalRecipient**: CONTROLLER `you()`; TARGET_PLAYER `targetPlayer()` (`getTargetId()`, null = nothing);
EACH_PLAYER `eachPlayer()` (seat order; amount evaluated per player, all totals computed before any is set; keep).
**SkipRecipient**: CONTROLLER `you()`; DAMAGED_PLAYER `triggeringPlayer()` (baked); TARGET_PLAYER `targetPlayer()`
(`getTargetId()` → first player in `getTargetIds()`); EACH_OPPONENT `eachOpponent()` (iterates unordered `playerIds`).
**EmblemRecipient**: CONTROLLER `you()`; TARGET_PLAYER `targetPlayer()` (`getTargetId()`, no null guard);
EACH_OPPONENT `eachOpponent()`; EACH_PLAYER_DEALT_DAMAGE_THIS_WAY **new** step-7 `playersDealtDamageThisResolution()`.
**FractionalLifeLossRecipient**: ALL `eachPlayer()`; OPPONENTS `eachOpponent()`; SUBGAME_NON_WINNERS **new**
`except(eachPlayer(), subgameWinners())` (step 7). **PlayersCantGainLifeEffect.Scope** (static,
`GameQueryService.canPlayerGainLife`): ALL_PLAYERS `eachPlayer()`; ENCHANTED_PLAYER `enchantedPlayer()`.
**ExchangeLifeTotalWithCreatureStatEffect.Recipient**: CONTROLLER `you()`; TARGET_PLAYER `targetPlayer()`.
**AwardPersistentManaEffect.Recipient**: CONTROLLER `you()`; TARGET_PLAYER is `triggeringPlayer()` (baked, no
target declared, falls back to the controller). **AnteRecipient**: CONTROLLER `you()`; EACH_PLAYER `eachPlayer()`.
**DraftCardRecipient**: CONTROLLER `you()`; TARGET_PLAYER reads a companion effect's player target without declaring
one: **new** non-declaring `targetPlayer` read (e.g. a `declares=false` flag on `TargetPlayer`).
**DestroyRecipient**: CONTROLLER `you()`; TARGET_PLAYER `targetPlayer()`.

**DamageRecipient** (`DealDamageToPlayersEffectHandler`; seat order; one damage call per player)

| Constant | Subject | Today |
|---|---|---|
| TARGET_PLAYER | `targetPlayer()` | `getTargetId()` only |
| EACH_OPPONENT / EACH_PLAYER | `eachOpponent()` / `eachPlayer()` | seat order; EACH_PLAYER evaluates the amount per player |
| SELECTED_OPPONENTS | **new** step-7 `acceptedPlayers()` ∩ opponents | `getAcceptedPlayerIds()` minus controller |
| EACH_OTHER_OPPONENT | **new** `except(eachOpponent(), triggeringPlayer())` | seat order minus controller and `getTargetId()` |
| CHOSEN_PLAYER | `chosenPlayer()` | source live → snapshot `getRememberedTargetPlayerId()` → `getTargetId()` |
| ACTIVE_PLAYER | `triggeringPlayer()` (baked upkeep player) | baked |
| CONTROLLER | `you()` | `getControllerId()` |
| ENCHANTED_PLAYER | `enchantedPlayer()` | baked |
| ENCHANTED_PLAYER_OR_PLANESWALKER | no subject: a resolution-time "player or one of their planeswalkers" choice; keep as a verb option over `enchantedPlayer()` | `getOpponentChosenTargetPlayerId()` → baked; prompt |
| ENCHANTED_PERMANENT_CONTROLLER | `controllerOf(triggering())` | live controller of `getTriggeringPermanentId()` → TRIGGERING_PERMANENT_CONTROLLER path; else baked (Feedback) |
| DEFENDING_PLAYER | `defendingPlayer()` | `getDefendingPlayerId()` → `getAttackedTargetId()` |
| TARGET_PERMANENT_CONTROLLER | `controllerOf(target())` **but declares the permanent target** | `targetsForEffect.first` → `getTargetId()`; triggering controller if it is the triggering permanent → live → `removedPermanentControllers` |
| TARGET_SPELL_CONTROLLER | **new** `controllerOf(targetSpell())` (declares a spell target here) | stack scan by `getTargetableId()` → `getCounteredSpellControllerId()` → `removedPermanentControllers` |
| TRIGGERING_PERMANENT_CONTROLLER | `controllerOf(triggering())` | inline LKI chain → extra `getTargetId()` fallback |
| TRIGGERING_PLAYER | `triggeringPlayer()` | baked |

**DiscardRecipient** (`DiscardEffectHandler`, `DiscardHandEffectHandler`, `DiscardCardThenEffectHandler`;
`discardCausedByOpponent` stays a derived flag)

| Constant | Subject | Today |
|---|---|---|
| CONTROLLER | `you()` | `getControllerId()` |
| TARGET_PLAYER | `targetPlayer()` | `targetsForEffect` → `getTargetId()`, players only; several targets choose active player first |
| TRIGGERING_PLAYER | `triggeringPlayer()` | baked (no null check); `DiscardCardThen` falls to controller |
| TRIGGERING_PERMANENT_CONTROLLER | `controllerOf(triggering())` | `findTriggeringPermanentController`; `DiscardHand` does nothing |
| ACTIVE_PLAYER | `triggeringPlayer()` (baked upkeep player) | baked; `DiscardCardThen` falls to controller |
| TARGET_PERMANENT_CONTROLLER | `controllerOf(target())` | live → `removedPermanentControllers` (`DiscardCardThen`: live only) |
| TARGET_PLAYER_OR_PERMANENT_CONTROLLER | **new**: `controllerOf(target())` where a player target stands for itself (or a `targetPlayerOrControllerOf()` variant) | `getTargetId()` player, else live controller; no LKI |
| DEFENDING_PLAYER | `defendingPlayer()` | `getDefendingPlayerId()` → `getAttackedTargetId()` |
| EACH_PLAYER / EACH_OPPONENT | `eachPlayer()` / `eachOpponent()` | non-random: all choose (active first, then seat order), then discard at once; random and `DiscardHand`: one player at a time |

**MillRecipient** (`MillEffectHandler`)

| Constant | Subject | Today |
|---|---|---|
| CONTROLLER | `you()` | `getControllerId()` |
| CHOSEN_PLAYER | `chosenPlayer()` | source live → snapshot `getRememberedTargetPlayerId()` |
| TARGET_PLAYER | `targetPlayer()` | `targetsForEffect` → `getTargetId()` |
| TRIGGERING_PLAYER | `triggeringPlayer()` | baked |
| ACTIVE_PLAYER | `triggeringPlayer()` (baked) | same code as TARGET_PLAYER |
| EACH_OPPONENT | `eachOpponent()` | one player at a time, seat order |
| EACH_PLAYER | `eachPlayer()` | one simultaneous event (`resolveMillPlayersSimultaneously`) — EACH_OPPONENT must become simultaneous too or the run says why not |
| TARGET_SPELL_CONTROLLER | **new** `controllerOf(targetSpell())`, non-declaring | stack scan → countered controller → `removedPermanentControllers` |
| TARGET_PERMANENT_CONTROLLER | `controllerOf(target())` | live → triggering controller if it is the triggering permanent; no `removedPermanentControllers` |
| TRIGGERING_PERMANENT_CONTROLLER | `controllerOf(triggering())` | `findTriggeringPermanentController` → `getEventPlayerIds().first` (Belltower Sphinx: keep as the triggering record's controller) |

**SacrificeRecipient** (`SacrificePermanentsEffectHandler`; the only one already in rotated APNAP order, choose then
act at once)

| Constant | Subject | Today |
|---|---|---|
| CONTROLLER | `you()` | `getControllerId()` |
| TARGET_PLAYER | `targetPlayer()` | `getTargetId()` → `targetsForEffect.first` |
| CONTROLLER_AND_TARGET_PLAYER | none: dead (no card, no engine construction); delete | `[controller, target]` |
| ACTIVE_PLAYER | `activePlayer()` | live `gameData.activePlayerId` |
| TRIGGERING_PERMANENT_CONTROLLER | `controllerOf(triggering())` | inline LKI chain |
| TARGET_PLAYER_OR_PERMANENT_CONTROLLER | as Discard's | `getTargetId()` player, else live controller |
| EACH_PLAYER / EACH_OPPONENT | `eachPlayer()` / `eachOpponent()` | `apnapPlayers()`, choose then sacrifice together |
| PLAYERS_WITH_MOST_LANDS | **new** player filter on `each` (e.g. `eachPlayer(PlayerPredicate)`) | `apnapPlayers()` filtered by `controlsMoreLandsThan` |
| DEFENDING_PLAYER | `defendingPlayer()` | `getDefendingPlayerId()` → `getAttackedTargetId()` |

**Draw** (4a): `RegisterDrawCardsAtNextUpkeepEffect`'s **UpkeepDrawRecipient** CONTROLLER `you()`; TARGET_PLAYER
`targetPlayer()`; TARGET_SPELL_CONTROLLER **new** `controllerOf(targetSpell())` (stack scan by target, before the
counter); TARGET_GRAVEYARD_OWNER **new** `ownerOf(targetCards())` (first targeted card still in a graveyard →
`findGraveyardOwnerById`; else the target player; else controller). The handler then bakes the drawer into the delayed
entry's `targetId` (decided twice). **EndStepDrawRecipient**: CONTROLLER `you()` (constructor default only);
TRIGGERING_PLAYER `triggeringPlayer()`. **DrawUpToRecipient** (engine-built only): CONTROLLER `you()`; OPPONENT
`eachOpponent()` (today `getOpponentId`, two players); TARGET_PLAYER is `triggeringPlayer()` (baked, never declares).
Siblings: `DrawCardForOwnerEffect` `ownerOf(source())` (`getCard().getOwnerId()` → controller);
`DrawCardForTargetPlayerEffect` `targetPlayer([group])` when `targetsPlayer`, else `triggeringPlayer()`, or
`ownerOf(triggeringCard())` when `triggeringCardOwner`; `DrawCardForDefendingPlayerEffect` `defendingPlayer()`
(`getAttackedTargetId()` only); `DrawCardForTriggeringPlayerEffect` `controllerOf(triggering())` → `triggeringPlayer()`
(today `findTriggeringPermanentController` → **source permanent's controller** → baked: the middle step looks wrong,
check its 12 cards); `DrawCardForPlayerEffect` engine-internal fixed id, not a subject;
`DrawCardsForEachOpponentOfTriggeringPlayerEffect` **new** `opponentsOf(triggeringPlayer())` (triggering spell's
controller → baked).

**LibraryOwner** (`Scry`, `ReorderTopCards`, `RevealTopCard` handlers and the `TopCardOfLibrary*` conditions; each
handler supports a different subset and falls back to the controller silently)

| Constant | Subject | Today |
|---|---|---|
| CONTROLLER | `you()` | `getControllerId()` |
| TARGET_PLAYER | `targetPlayer()` | `getTargetId()` → controller (condition: no fallback) |
| OPPONENT | `eachOpponent()` (single) | Scry, condition: `getOpponentId`; Reorder, Reveal: controller |
| ENCHANTED_PERMANENT_CONTROLLER | `triggeringPlayer()` (Paroxysm's baked upkeep player) | Reveal: baked → controller; condition: `targetId` |
| DYING_CREATURE_CONTROLLER | `controllerOf(triggering())` | Scry: raw `getTriggeringPermanentControllerId()` → controller; condition: `targetId` → controller |

**LibraryScope** (the `ExileTopCards*`, `ExileBottomCardsToSource`, `EachPlayerExilesTopUntilNonland...`,
`LookAtTopCardsRevealTwoTypes...` handlers)

| Constant | Subject | Today |
|---|---|---|
| CONTROLLER | `you()` | `getControllerId()` |
| TARGET_PLAYER | `targetPlayer()` (`ExileTopCardsAndMayCastSpells` declares none) | `getTargetId()` if a player; ChooseOne: `targetsForEffect.first` → `getTargetId()`; Bottom: nothing |
| TARGET_OPPONENT | split: `defendingPlayer()` (attack triggers), `triggeringPlayer()` (combat damage), `targetPlayer(group)` with an opponent filter (`targetedOpponent`), `eachOpponent()` (sole opponent, Grimoire Thief) | ToSource: baked player ≠ controller → `getAttackedTargetId()` → first non-controller; Cast: `getTargetId()` only |
| TRIGGERING_PLAYER | `triggeringPlayer()` | baked (same path as TARGET_PLAYER) |
| EACH_PLAYER / EACH_OPPONENT | `eachPlayer()` / `eachOpponent()` | seat order (Look handler: active first) |

**GraveyardOwner** (`ReturnTopCreatureCardFromGraveyardToBattlefieldEffect`): CONTROLLER `you()`; TARGET_PLAYER is
`defendingPlayer()` (Bone Dancer's baked defending player, no target).
**GraveyardExileScope** (`ExileGraveyardCardsEffectHandler`): split into who × quantity (count / all matching / entire /
keep N) × card targets. Who: OWN, OWN_ALL_MATCHING `you()`; TRIGGERING_PLAYER `triggeringPlayer()` (baked);
TARGET_PLAYER_ENTIRE, TARGET_PLAYER_ALL_MATCHING `targetPlayer()` (multi-target lists vs `getTargetId()` only);
DYING_CREATURE_CONTROLLER `controllerOf(triggering())` (raw recorded controller today); ALL_PLAYERS `eachPlayer()`;
ALL_OPPONENTS, EACH_OPPONENT_KEEP `eachOpponent()`. TARGET_CARDS_* are card targets (owner per card), not a subject;
they become a graveyard-card-target effect with the existing `GraveyardSearchScope`.
**ThenEffectRecipient** (the `...ThenEffect` wrappers; controller/owner snapshotted before the permanent leaves:
`findPermanentController`, owner `defaultControllerOf`; Destroy's departed path `removedPermanentControllers` /
`lastKnownPermanentCard.getOwnerId()`): CONTROLLER `you()`; TARGET_CONTROLLER and TARGET_CONTROLLER_AS_TARGET
`controllerOf(target())`; TARGET_OWNER and TARGET_OWNER_AS_TARGET `ownerOf(target())`. The `_AS_TARGET` pair exists
only so damage-source checks still see the caster; with a player subject on the rider both pairs are one. Step 8 owns
it.
**BouncePermanentOnUpkeepEffect.Scope**: SOURCE_CONTROLLER `you()`; TRIGGER_TARGET_PLAYER `triggeringPlayer()`
(baked upkeep player → controller). The verb is "that player returns a permanent they control": `controlledBy(p,
filter)` plus that player's choice.

**TapUntapScope** (`TapPermanents`, `UntapPermanents`, `SkipNextUntap`, `CantAttackThisTurn`, `CantBlockThisTurn`,
`DoesntUntap`, `DoesntUntapWithCounter`, `UntapAllPermanentsYouControlDuringEachOtherPlayersStep`)

| Constant | Subject | Today |
|---|---|---|
| TARGET | `target()` (`DoesntUntap`: non-declaring read) | `targetsForEffect` → `getTargetId()`; CantAttack: flat `getTargetIds()` |
| ALL_TARGETS | `target(group)` | Untap: `getTargetIds()` → `getTargetId()`; Tap: throws |
| SELF | `source()` | Tap/Untap: source → `getTargetId()` (dropped, see above); others strict |
| SOURCE_PERMANENT | `source()` | strict; the same as SELF once the fallback goes |
| TRIGGERING | `triggering()` | `getTriggeringPermanentId()`; SkipNextUntap → `getTargetId()` |
| ENCHANTED | `attached()` | Tap/Untap: live → source snapshot; SkipNextUntap: live only; CantBlock: live → activation-captured `targetId`; static readers scan live attachments |
| CONTROLLED | `controlledBy(you(), filter)` | controller's battlefield (Untap `chosenCount`, Skip `matchAtUntap` keep their own modes) |
| OTHER_CONTROLLED_CREATURES | `controlledBy(you(), creature ∧ ¬source)` | skips source id |
| TARGET_PLAYERS_PERMANENTS | `controlledBy(targetPlayer(), filter)` | `getTargetId()`; CantBlock accepts a planeswalker's controller; CantAttack/CantBlock install player-scoped floating rules |
| TARGET_CONTROLLERS_OTHER_CREATURES | `controlledBy(controllerOf(target()), creature ∧ ¬target)` — **new** "is not the target" predicate | floating rule (covers later arrivals) |
| ALL_CREATURES / ALL_PERMANENTS | `controlledBy(eachPlayer(), [creature ∧] filter)` | `forEachPermanent`; filter context carries defending / triggering player |
| ATTACKED_CREATURES | `controlledBy(eachPlayer(), creature ∧ PermanentAttackedThisTurnPredicate)` | `isAttackedThisTurn` |
| OPPONENTS_PERMANENTS | `controlledBy(eachOpponent(), filter)` | unordered `playerIds` |

The floating-rule uses (CantBlock/CantAttack player scopes, SkipNextUntap `matchAtUntap`) are re-evaluated later, so
they are not "found once": they keep a player-scoped rule, not a resolved set (CR 611.2c second sentence).

**BounceScope** (`ReturnToHandEffect` factories): TARGET, TARGET_NONLAND_PERMANENT_OR_SUSPENDED_CARD,
TARGET_CHOSEN_CREATURE_TYPE `target()` (the last two keep their filter/zone); TARGET_AND_SELF `allOf(source(),
target())`; SELF `source()` (strict); TRIGGERING `triggering()`; SELF_SPELL no subject (spell disposition);
ALL_MATCHING `controlledBy(eachPlayer(), filter)`; TARGET_PLAYERS_PERMANENTS `controlledBy(targetPlayer(), filter)`;
TARGET_PLAYERS_OWNED **new** `ownedBy(targetPlayer(), filter)`; AURAS_ATTACHED_TO_TARGET **new**
`attachmentsOf(target(), aura)`; ENCHANTED `attached()` (today live → effect-baked `enchantedPermanentId` →
`departedPermanentSnapshots` → `getTargetId()`); ENCHANTED_AND_AURAS `allOf(attached(), attachmentsOf(attached(),
aura))`; GRANTING_EQUIPMENT **new** `granter()` (activation-baked `grantingEquipmentSnapshot`; triggered:
`getGrantingPermanentId()`).
**FlickerScope**: TARGET `target()` (IMMEDIATE reads every bound group); SELF `source()`; TRIGGERING `triggering()`;
SELF_OR_TARGET **new** `oneOf(source(), target())` chosen at resolution; TARGET_PLAYERS_PERMANENTS
`controlledBy(targetPlayer(), filter)`; CONTROLLERS_PERMANENTS `controlledBy(you(), filter)`; ALL_PLAYERS_PERMANENTS
`controlledBy(eachPlayer(), filter)`; RETURNED_PERMANENTS step-7 `returnedThisResolution()`;
ENCHANTED_CREATURE_AND_AURAS `allOf(attached(), attachmentsOf(attached(), aura))` (today the activation-captured
`getTargetId()` only); TARGET_EXILED_EARLIER step-7 `exiledThisResolution()` (a card).
**PhaseOutSubject**: SOURCE `source()`; TARGET `target()` (PhaseOutAttackingCreatureAtEndOfCombat builds it on a
delayed entry as a non-declared referent: `triggering()`); ATTACHED `attached()` (live → snapshot).
**PhaseOutScope**: CONTROLLER `controlledBy(you(), filter)`; TARGET_PLAYER `controlledBy(targetPlayer(), filter)`;
ALL dead (no card or engine use).
**CounterRemovalSubject**: SOURCE `source()` (fallback dropped); TARGET `target()`; NAMED (engine-built, Giant Oyster's
delayed ability) a referent: `triggering()` once the delayed ability records its permanent there.
**PutOnTopOfLibraryScope**: TARGET `target()`; SELF `source()`; SELF_AND_TARGET `allOf(source(), target())` (keeps its
shuffle).
**EachPermanentScope**: ALL_PLAYERS `controlledBy(eachPlayer(), pred)`; TARGET_PLAYER `controlledBy(targetPlayer(),
pred)` (RemoveCounterFromEachMatching: baked upkeep player, `triggeringPlayer()`); TRIGGERING_PERMANENTS **new** plural
`triggeringAll()` (`getTriggeringPermanentIds()`).
**DamagedPermanentScope**: not a who-axis (creature vs creature-or-planeswalker filter); its player is
`controllerOf(target())` / `targetPlayer()` read without declaring (TargetSpec `harmful` with no predicate).
**RedirectRole**: SOURCE_PERMANENT `source()`; TARGET `target()` / `targetPlayer()` (declared by `declaredTarget`);
CONTROLLER `you()`; SOURCE_PERMANENT_AND_CONTROLLER a permanent and a player together (stays special);
ENCHANTED_PERMANENT `attached()` (live → snapshot → `lastAttachedTo`).

**GrantScope** (step 6; one-shot handlers each have their own chain, static path in `StaticEffectSupport`,
`LayerSystemService.scopeTargets`, `GameQueryService`). "Not the source" is the existing
`PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())`.

| Constant | Subject |
|---|---|
| SELF | `source()` (GrantKeyword one-shot falls back to `getTargetId()`; dropped) |
| TRIGGERING_PERMANENT | `triggering()` |
| SELF_AND_PAIRED | `controlledBy(eachPlayer(), PermanentIsSourceOrPairedPredicate)` or **new** `paired()` |
| TARGET | `target()` (also the internal marker of per-permanent floating effects with `affectedPermanentId`) |
| TARGET_AND_SHARING_CREATURES | `allOf(target(), controlledBy(eachPlayer(), creature ∧ **new** shares-a-colour-with-target))` |
| TARGETS | `target(group)`; GrantKeyword's use is the triggering spell's targets: **new** `triggeringSpellTargets()` |
| DECLARED_ATTACKERS | **new** `triggeringAttackers()` (`getAttackingPermanentSnapshots()`) |
| ENCHANTED_CREATURE, EQUIPPED_CREATURE | `attached()` with a creature check (static path has none today) |
| ENCHANTED_PERMANENT | `attached()` |
| OWN_TAPPED_CREATURES | `controlledBy(you(), creature ∧ tapped ∧ ¬source)` |
| OWN_UNTAPPED_CREATURES | `controlledBy(you(), creature ∧ untapped)` |
| OWN_CREATURES | `controlledBy(you(), creature ∧ ¬source)` |
| ALL_OWN_CREATURES | `controlledBy(you(), creature)` |
| OPPONENT_CREATURES | `controlledBy(eachOpponent(), creature)` |
| OWN_PERMANENTS | `controlledBy(you())` (static keyword grants exclude the source today: decide per verb) |
| TARGET_PLAYERS_CREATURES / _LANDS | `controlledBy(targetPlayer(), creature / land)` |
| ALL_CREATURES | `controlledBy(eachPlayer(), creature ∧ ¬source)` (one-shot GrantKeyword, Doran and `selfInScope` include the source today) |
| ALL_CREATURES_INCLUDING_SELF | `controlledBy(eachPlayer(), creature)` |
| ALL_PERMANENTS | `controlledBy(eachPlayer(), ¬source)` |
| ENCHANTED_PLAYER_CREATURES | `controlledBy(enchantedPlayer(), creature)` |
| OWN_LANDS / OPPONENT_LANDS | `controlledBy(you() / eachOpponent(), land)` (OWN_LANDS self-inclusion differs per verb today) |
| ALL_LANDS | `controlledBy(eachPlayer(), land ∧ ¬source)` (one-shot includes the source) |
| ALL_LANDS_INCLUDING_SELF | `controlledBy(eachPlayer(), land)` |
| TOKENS_CREATED_THIS_RESOLUTION | step-7 `createdThisResolution()` |
| BANDED_WITH_SELF | **new** `bandmatesOf(source())` or a band predicate |

**Not a who-axis, or static player sets**: `SpellLimitScope` (EACH_PLAYER `eachPlayer()`, CONTROLLER `you()`,
ENCHANTED_PLAYER `enchantedPlayer()`; static), `CostModificationScope` (SELF `you()`, OPPONENT `eachOpponent()`, ALL
`eachPlayer()`; static; `IncreaseSpellCostForTargetPlayerUntilEndOfTurn` writes SELF under the target player),
`ManaColorLandScope` (`controlledBy(you() / eachOpponent(), land ∧ filter)`), `CombatAttackTargetScope` (CONTROLLER
`you()`, SOURCE_PERMANENT `source()`, ALL unrestricted: a player-or-permanent attack target, stays),
`ChosenSourcePreventionScope` and `PreventionScope` (shield semantics; their recipients map to `you()`,
`controlledBy(you(), X)`, `source()`, `attached()`, `target()`, and the damage-source side to `target()` /
`controlledBy(...)`), `DestroyedPermanentCountScope` (a count filter; CONTROLLER* is "controlled by `you()`"),
`DamagedPermanentScope` (filter).

**For later steps.**

- New variants, each added by the first run that needs it: `except(players, players)`, `chosenPlayers()`,
  `triggeringCard()`, `targetSpell()` / `targetCards()` (subjects over a stack object or graveyard cards for
  `controllerOf` / `ownerOf`), a non-declaring player-target read, `opponentsOf(player)`, a filtered `each`,
  `ownedBy`, `attachmentsOf`, `granter()`, `oneOf` (chosen at resolution), plural `triggeringAll()`,
  `triggeringAttackers()`, `paired()` / `bandmatesOf()`, and the step-7 results (`createdThisResolution()`,
  `returnedThisResolution()`, `exiledThisResolution()`, `acceptedPlayers()`, `playersDealtDamageThisResolution()`,
  `subgameWinners()`).
- "That player" is read four ways today (baked `getTargetId()`; that → `getTargetIds().first`;
  `getTriggeringPlayerId()`; `getActivePlayerId()`); ACTIVE_PLAYER constants that read the baked target are
  `triggeringPlayer()`, Sacrifice's live one is `activePlayer()`.
- Multi-player order: only `SacrificePermanentsEffectHandler` is rotated APNAP; most use seat order, `SkipNext` the
  unordered `playerIds`. Several private `apnapOrder` / `apnapPlayers` copies exist (`TriggerIndex`,
  `PlayerInputService`, `WorldsWithinWorldsSupport`, a few handlers), some unrotated.
- Misnamed constants that never declare a target: `GraveyardOwner.TARGET_PLAYER`, `DrawUpToRecipient.TARGET_PLAYER`,
  `AwardPersistentManaEffect.Recipient.TARGET_PLAYER`, `BouncePermanentOnUpkeepEffect.Scope.TRIGGER_TARGET_PLAYER`,
  LibraryScope TARGET_PLAYER on `ExileTopCardsAndMayCastSpells`, EachPermanentScope TARGET_PLAYER on
  `RemoveCounterFromEachMatching`.
- Dead constants: `LoseLifeRecipient.SOURCE_CONTROLLER`, `SacrificeRecipient.CONTROLLER_AND_TARGET_PLAYER`,
  `PhaseOutScope.ALL`; constructor-default-only: `EndStepDrawRecipient.CONTROLLER`.
- Ledger: the 15 pilot records are `blocked-other` "kept for step 5"; they are audited for their subject, not folded.

### Step 4, run 4a (2026-11-19)

**Survivor.** `DrawCardEffect` gained a last component `PlayerSubject player` (default `you()`; every old constructor
kept, plus `(int|DynamicAmount, PlayerSubject)`). Its handler evaluates the amount once in the entry's context, asks
`SubjectResolutionService` for the players and has each draw it in APNAP order (`resolveDrawCards`, so a draw of N is
one instruction for Alms Collector / Quantum Riddler); the cycling draw path is kept for the controller; each player's
cards drawn from the library are recorded with the old over-draw rule and the step result (`recordDrawnCards`) is the
union. `targetSpec()`: benign player target when the subject declares one, or (as before) a controller draw with a
target-relative amount. `CardDrawingEffect` gained `drawingPlayer()` (default `you()`).

**Absorbed** (5 records, 5 handlers; ratchet 4,935 / 4,168): `DrawCardForOwnerEffect` → `ownerOf(source())`;
`DrawCardForDefendingPlayerEffect` → `defendingPlayer()`; `DrawCardsForEachOpponentOfTriggeringPlayerEffect` →
`opponentsOf(triggeringPlayer())`; `DrawCardForTriggeringPlayerEffect` → `controllerOf(triggering())` for "its
controller" (8 cards) and `triggeringPlayer()` for "that attacking player" (Breena, Norn's Decree, Curse of Verbosity,
Curse of Chaos: their collectors record the attacker as both `triggeringPermanentControllerId` and the baked `targetId`,
so both lookups gave the same player; the old middle fallback to the source's controller was never reached);
`DrawCardForTargetPlayerEffect` → `targetPlayer([group])` where `targetsPlayer` was set (93 cards + 4 `forTargetGroup`),
`ownerOf(triggeringCard())` for Apple of Eden, and for the 55 non-declaring uses one of three by card:
`targetPlayerDeclaredElsewhere()` (24: spells and abilities whose target filter or another effect declares the player,
which also keeps multi-target lists and target groups, e.g. Jace, Memory Adept, Toil // Trouble), `triggeringPlayer()`
(30: draw/upkeep/end-step, cast, land and combat triggers; none of their collectors records a triggering player other
than the baked one) and `controllerOf(triggering())` (Undercity Reaches). Its unused `requireSourceUntapped` flag is
gone. 169 card files rewritten (by a deleted scratch script), plus 9 engine constructors.

**New variants.** `targetPlayerDeclaredElsewhere()` (`TargetPlayer` with `declared = false`: same targets, declares
nothing), `opponentsOf(player)` (nobody when `player` names nobody) and `triggeringCard()` (`ownerOf` reads
`findTriggeringCardOwner`; as a permanent it is the land played, if on the battlefield).

**Resolver fix.** `ownerOf(source())` of a card with no recorded owner (a hand-built test card) whose source left
with no last-known controller recorded found nobody; it now falls back to the resolving ability's controller, as
`DrawCardForOwnerEffect` did (`OftNabbedGoatTest`'s death case).

**AI parity.** `DrawCardForTargetPlayerEffect` was not a `CardDrawingEffect`; the other absorbed classes were. The AI
readers (`BoardEvaluator`, `HardAiDecisionEngine`, `SpellEvaluator`, `InstantCategoryClassifier`) now skip a
`CardDrawingEffect` whose `drawingPlayer()` is a `TargetPlayer` (declared or not), which is exactly the old set in the
contexts they read (activated abilities, spells, modes, enters abilities). `MayEffectHandler`'s "the controller can't
draw" shortcut now applies only to a `you()` draw (before, only controller draws were `DrawCardEffect`), and its
`TRIGGERING_PERMANENT_CONTROLLER` special case for the target sibling is gone (Undercity Reaches reads
`controllerOf(triggering())` like Synapse Sliver).

**Behaviour differences** (none changes a tested outcome): several opponents of a caster draw in APNAP order instead
of seat order, and "that player" is the caster recorded as the spell was cast rather than the spell's controller at
resolution (Standstill, Zenith Chronicler); the owner draw no longer re-bases a controller-relative amount to the owner
(no card's amount depends on it); the target draw records only cards drawn from the library, like the controller draw.

**For later runs.**

- Not folded, in the ledger: `EachOpponentDrawsCardEffect` (5 cards) is `DrawCardEffect(eachOpponent())`, but the AI
  would then count it as its own draw; fold it with an AI rule for draws by opponents. `DrawCardForYouAndDamagedPlayerEffect`
  needs a player union. `DrawCardForPlayerEffect` is engine-built with a fixed id. `EachOpponentOfTriggeringPlayerMayDrawCardEffect`
  asks each opponent its own may choice. `DrawUpToRecipient`, `UpkeepDrawRecipient`, `EndStepDrawRecipient` stay on
  their verbs (draw up to, delayed draw registration); their mappings are in step 3's table.
- Two inline draw paths ignore the subject and draw for the controller: the mana-ability path in
  `ActivatedAbilityExecutionService` and `FlickerEffectHandler`'s bonus draw. Only `you()` reaches them today.
- The non-declaring player read is what most "`TARGET_PLAYER`" constants without a declared target are (4b–4i): use
  `targetPlayerDeclaredElsewhere()` where a card's target filter or a companion effect declares the player, and
  `triggeringPlayer()` where a trigger baked it.

### Step 4, run 4b (2026-11-19)

**Survivor.** `LoseLifeEffect`'s `LoseLifeRecipient recipient` became `PlayerSubject player` (every constructor kept
with the subject in the recipient's place; `(int)` is `you()`). Its handler asks `SubjectResolutionService` for the
players and applies one `LifeSupport.applyLifeLosses` map; it keeps the amount context: one evaluation in the
entry's context, re-based to the receiving player as target for `defendingPlayer()` and as controller for
`ownerOf(source())` (Personal Incarnation), and once per target for a `targetPlayer` whose effect has several targets
(raw `targetsForEffect` size, as before). The "each player" subjects are **not** evaluated per receiving player (they
never were; the plan's note was wrong for this verb). `exemptIfControls` applies to `Each` / `OpponentsOf` only.
`targetSpec()` is a benign player target iff `player.declaresTarget()`. The drain (`controllerGainsLifeLost`) and
`recordTargetLifeLost` no longer check the recipient: every card using them had a recipient that allowed them, the
drain always goes to the controller (the owner branch was only for `EACH_OTHER_PLAYER`, whose one card doesn't drain),
and the Exsanguinate early-out on X ≤ 0 is gone (`applyLifeLoss` ignores a non-positive loss, so nothing drains).

**Deleted**: the enum `LoseLifeRecipient` (no record or handler, so the ratchet stays 4,935 / 4,168). Mapping (step 3's
table): `TARGET_PLAYER` → `targetPlayer()` everywhere it was used (it always declared, nested ones too; Pillar Tombs
of Aku's `ForcedCostOrElseEffect` else-branch therefore still declares a benign player target, as before, for a
"that player"; a later run can make it `triggeringPlayer()` once `DestructionSupport`'s else-branch reads it);
`TRIGGERING_PLAYER` → `triggeringPlayer()` (35 cards plus Nafs Asp's delayed draw-step loss; every collector their
triggers use writes the same player as `triggeringPlayerId` and the baked `targetId`); `ACTIVE_PLAYER` →
`activePlayer()` (live; the entry's recorded active player is the same player for all ten cards: upkeep and combat
triggers resolve in their turn); `DYING_CREATURE_CONTROLLER` / `TRIGGERING_PERMANENT_CONTROLLER` →
`controllerOf(triggering())`; `TARGET_PERMANENT_CONTROLLER` → `controllerOf(target())` (all 24 are single-target);
`TRIGGERING_CARD_OWNER` → `ownerOf(triggeringCard())`; `OWNER` → `ownerOf(source())`; `EACH_OTHER_PLAYER` →
`opponentsOf(ownerOf(source()))`; `OTHER_CHOSEN_PLAYER` → `except(chosenPlayers(), triggeringPlayer())`;
`SOURCE_CONTROLLER` was dead. `LoseLifeUnlessSacrificeNonlandOrDiscardEffect` (same enum) now takes a `PlayerSubject`:
`triggeringPlayer()` (Torment of Scarabs, the `(int)` default), `targetPlayer()` (Thornplate Intimidator, Starseer
Mentor; was the `targetsPlayer` flag), `targetPlayerDeclaredElsewhere()` (Tergrid's Lantern), `controllerOf(target())`
(Torment of Venom, which still excludes the target from the sacrifice options). 742 card files rewritten by a
deleted scratch script (740 named the enum, plus the two `targetsPlayer` cards), 25 engine and 12 application-test
files.

**New variants.** `chosenPlayers()` (every player the source remembered, in play, APNAP order) and
`except(players, excluded)`. `ownerOf(triggeringCard())` now falls back like the old `TRIGGERING_CARD_OWNER`:
spell or land played → the entry's recorded triggering-permanent owner → the triggering permanent's owner as it is or
last was (Liability, The Dark Barony, Brainstealer Dragon); Apple of Eden always has a played card, so it is unchanged.

**AI and other readers.** `SpellEvaluator` scores `you()` as before and `eachOpponent()` drains as before; the mana
ability path (`ActivatedAbilityExecutionService.isNonTargetedLifeLoss` / `lifeLossRecipients`) accepts `you()` and
`each*` (it still applies them inline, seat order, no exemption); `DestructionSupport`'s unless-branches match `You` /
`TargetPlayer`; `LifeTargetValidators` requires a player target iff the subject declares one. `AiDecisionEngine`
reads only the amount.

**Behaviour differences** (none changes a tested outcome): several players lose life in APNAP order instead of seat
order (life-loss triggers are put on the stack in that order); several target players likewise; `controllerOf(target())`
also finds a target that left through the entry's last-known controller (Torment of Venom: only if the creature left
before the loss, which no card does); the owner of Personal Incarnation is `GameData.ownerOf` (honours owner overrides)
instead of the raw card owner; a `targetPlayer()` whose bound target group is empty no longer falls back to the first
flat target.

**Doc fix.** The old Javadoc and `DestructionSupport` cited CR 118.2 for "life loss is not damage"; 118.2 is about
paying mana. Now CR 119.3 (an effect makes a player lose life; the total is adjusted).

**For later runs.**

- The lose-life siblings stay (ledger, `blocked-other`): the per-receiving-player amounts
  (`EachPlayerLosesLifePer{Creature,Permanent}Controlled`, `EachOpponentLosesLifeEqualTo{CardsInTheirGraveyard,LifeLostThisTurn}`)
  need the survivor to evaluate the amount per player with that player as target, which changes nothing for today's
  each-player cards only if their amounts don't read the target: check before adding it. Filtered each
  (`EachOpponentWith{EmptyHand,PoisonCounters}LosesLife`, `LoseLifeToOpponentsWhoCastNamedSpellThisTurn`) needs a
  filtered `each`. `TargetSpellControllerLosesLife` needs `controllerOf(targetSpell())`.
- `TARGET_PLAYER`-style constants on trigger entries whose player is baked: LoseLife kept `targetPlayer()` (declared)
  where the old recipient declared, even in `ForcedCostOrElseEffect` else-branches (Pillar Tombs of Aku). 4d's
  `DealDamageToPlayersEffect` should check the same `DestructionSupport` branches.
- No LoseLife constant had the Paroxysm "baked player vs current controller of the triggering permanent" split:
  upkeep `ENCHANTED_CONTROLLER` entries leave `triggeringPermanentId` unset at upkeep, and the five Aura upkeep cards
  (Stab Wound, Soul Bleed, One Thousand Lashes, Numbing Dose, Torment of Scarabs) read the baked player through
  `triggeringPlayer()`.

## Prompt

Replace `<N>` with the step number. For a repeatable step, add the run (`step 4, run 4b`; `step 9,
family Exile`).

```
Implement step <N> of effect-cleanup-refactor.md. First read the whole file (Why, Measurements, Design,
Ground rules, Findings) and step <N>'s section, and check in the Status table that every step it depends
on is done; if one isn't, stop and tell me. Follow the ground rules: read agent-docs/ARCHITECTURE.md and
agent-docs/architecture/resolution.md before touching engine or domain code, verify the premise by
reading the effect records and their handlers before editing, keep behaviour identical unless the old
behaviour was rules-wrong (then cite the Oracle text or ruling and add a test), verify every CR number
with get_rule, and test behaviour through the engine. Run only the tests the step names, with
fully qualified class names and one --tests list per Gradle test task; never the full suite, ask me for
it. When the step or run is done: update agent-docs for every class you removed, add the audited classes
to scripts/effect-audit-ledger.csv, lower the EffectCountRatchetTest baselines, mark the step or run in
Status, add what later steps need under Findings, and tell me which classes you deleted, which tests
changed and why, and any behaviour change. Don't commit until I say so.
```
