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
| 4 | Player verbs take a `PlayerSubject` (repeatable) | done |
| 5 | Permanent verbs take a `PermanentSubject` (repeatable) | open (5a–5c, 5e–5l done; 5d partly done: its restriction readers move to step 6; 5m partly done) |
| 6 | `GrantScope` becomes a `PermanentSubject`, static abilities included | in progress: 6.1, 6.2a, 6.2b, 6.2c, 6.2d done (6.2b: `GrantScope` part; the static `TapUntapScope` readers moved to 6.2e) |
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
| 4c | Gain life: `GainLifeEffect` | `GainLifeRecipient` | check `--family GainLife` | done |
| 4d | Damage to players: `DealDamageToPlayersEffect` | `DamageRecipient` (650 cards) | `DealDamageTo{EndStepPlayer,TriggeringSpellController,EnchantedPermanentController,RandomOpponent,CombatOpponentController}Effect` | | done |
| 4e | Discard: `DiscardEffect` | `DiscardRecipient` (683 cards) | `--family Discard`, `TargetPlayerDiscards*`, `EachOpponentDiscards*` | done |
| 4f | Mill: `MillEffect` | `MillRecipient` (419 cards) | `--family Mill` | done |
| 4g | Player sacrifices: `SacrificePermanentsEffect` | `SacrificeRecipient` (301 cards) | `--family Sacrifice`, `TargetPlayerSacrifices*`, `EachPlayerSacrifices*` | done |
| 4h | Player counters: poison, rad, energy, experience | `PoisonRecipient` | `Give{TargetPlayer,Controller,EachPlayer,EachOpponent,DefendingPlayer}RadCountersEffect` | done |
| 4i | The small ones: `SetLifeTotalRecipient`, `SkipRecipient`, `EmblemRecipient`, `LibraryOwner`, `LibraryScope`, `GraveyardOwner`, `DestroyRecipient`, `DraftCardRecipient`, `AnteRecipient`, `FractionalLifeLossRecipient` | each | their own families | done (`LibraryOwner` kept for conditions; `LibraryScope` as 4j) |
| 4j | The rest of 4i: the library-exile records | `LibraryScope` | `ExileTopCardsToSource`, `ExileBottomCardsToSource`, `ExileTopCardsAndMayCastSpells`, `ExileTopCardsChooseOneMayPlayUntil{NextEndStep,NextTurn}`, `EachPlayerExilesTopUntilNonlandAndMayCastSpells`, `LookAtTopCardsRevealTwoTypesToHandThenRest` | done |

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
| 5a | Destroy | `PermanentReference` users | `DestroyTargetPermanentEffect`, `DestroyReferencedPermanentEffect`, `--family Destroy` | done |
| 5b | Exile a permanent | none | `Exile{TargetPermanent,Self,EnchantedCreature,EquippedCreature}Effect` | done |
| 5c | Return to hand | `BounceScope` | `ReturnToHandEffect`, `Return*ToHand*` | done |
| 5d | Tap and untap | `TapUntapScope` (1,419 cards) | `--family Tap`, `--family Untap` | partly done (the tap and untap verbs; the six restriction records still carry `TapUntapScope`: planned for step 6) |
| 5e | Put counters | `PermanentReference` users | `PutCountersOnSelfEffect`, `PutCounterOnTargetPermanentEffect`, `PutCounterOnReferencedPermanentEffect`, `DoublePlusOneCountersOn{TargetCreature,Source,EnchantedCreature}Effect` | done |
| 5f | Remove counters | `CounterRemovalSubject` | `--family RemoveCounter` | done |
| 5g | Damage to permanents | `DamagedPermanentScope` | `DealDamageTo{TargetCreature,Source,TriggeringPermanent,EnchantedCreature,TargetPermanent}Effect` | done (`DealDamageToTargetPermanentEffect` kept: not a subject sibling) |
| 5h | A creature deals damage | none | `*DealsPowerDamageTo*` (7), `*DealsDamageTo*` (5) | done (8 folded into two survivors; 13 left in the ledger) |
| 5i | Boost (one-shot) | `PermanentReference` users | `Boost{Self,TargetCreature,EquippedCreatureUntilEndOfTurn,ReferencedPermanent,TargetPermanent}Effect` | done (3 folded into `BoostSelfEffect`; Hand of Vecna's choice record and `BoostTargetPermanentEffect` kept) |
| 5j | Sacrifice a named permanent | none | `Sacrifice{Self,EnchantedCreature,TargetCreature}Effect` | done (3 folded into `SacrificeSelfEffect`; the delayed and Animate Dead records kept) |
| 5k | Flicker, phase out, remove from combat | `FlickerScope`, `PhaseOutSubject`, `PhaseOutScope` | `Remove{Target,Self,Source}FromCombatEffect` | done (3 enums gone; 4 records folded into `RemoveTargetFromCombatEffect` and `PhaseOutEffect`) |
| 5l | Attach and token copies | none | `Attach*EquipmentTo*` (5), `CreateTokenCopyOf*` (4), `CreateTokenAttachedTo*` (4) | done (9 folded into three survivors; `Copied` enum gone; the defending-player token and `CreateTokenCopyOfSourceEffect` kept) |
| 5m | Whatever `--siblings` still lists | `PermanentReference` | — | partly done (14 folded into 11 survivors; `PermanentReference` and `Subjects.of` gone; 135 sibling groups still have unaudited classes) |

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

| Run | Takes | Status |
|---|---|---|
| 6.1 | the inventory | done |
| 6.2a | the one-shot `normalfx` readers | done |
| 6.2b | `staticfx` + `StaticEffectSupport`, the static `TapUntapScope` readers | done for `GrantScope`; the static `TapUntapScope` readers moved to 6.2e |
| 6.2c | `LayerSystemService` + `GameQueryService`, the engine classifiers outside them | done |
| 6.2d | the AI | done (the AI `TapUntapScope` reads go with 6.2e) |
| 6.2e | the one-shot `TapUntapScope` restriction handlers, and the static ones 6.2b left (`UntapStepService`, `UntapPreventionSupport`) | open |
| 6.3 | records take a `PermanentSubject`, cards rewritten, `GrantScope` / `TapUntapScope` deleted | open |
| 6.4 | the rule-4 tests | open |

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

### Step 4, run 4c (2026-11-19)

**Survivor.** `GainLifeEffect`'s `GainLifeRecipient recipient` became `PlayerSubject player`; `targetsPlayer` stays (a
you-gain whose amount reads a target player: Renewing Dawn, Keeper of the Light, Gerrard Capashen, ...). Constructors:
`(DynamicAmount|int)` is `you()`, `(DynamicAmount|int, PlayerSubject)`, `(DynamicAmount, PlayerSubject, boolean)`.
`targetSpec()` is a benign player target iff `player.declaresTarget() || targetsPlayer`; a `@ValidatesTarget` requires a
player iff the subject declares. The handler resolves the players through `SubjectResolutionService` and keeps both old
amount contexts: one evaluation against the entry with the live source or its snapshot, except for a `TargetPlayer`
subject, which is evaluated per target player with that player as controller and target and no source (the old
`TargetPlayerGainsLifeEffect` context). `LifeGainEffect` gained `lifeGainingPlayer()` (default `you()`).

**Deleted**: the enum `GainLifeRecipient` and `TargetPlayerGainsLifeEffect` + handler (ratchet 4,934 / 4,167).
Mapping: `CONTROLLER` → `you()`; `TARGET_CONTROLLER` → `controllerOf(target())` (Condemn, Last Breath) and
`controllerOf(triggering())` for Curse of the Forsaken (its creature-attacks entry records the attacker as both target
and triggering permanent); `OPPONENT` (two-player derived opponent with its own shroud/hexproof check) by Oracle text:
`eachOpponent()` for "each opponent" (Aria of Flame, Grollub, Grove of the Burnwillows, Mathas's granted ability,
Wingbright Thief's granted ability) and Benevolent Offering's "choose an opponent" (two-player); Centaur Peacemaker
and Putrid Warrior ("each player gains", written as you + opponent) are one `eachPlayer()` effect;
`targetPlayerDeclaredElsewhere()` for Phelddagrif and Questing Phelddagrif (the ability's opponent filter declares the
target); Soldevi Steam Beast's trigger now declares "target opponent" (`target(opponent filter)` + `targetPlayer()`);
`TRIGGERING_PLAYER` → `triggeringPlayer()` (Aven Shrine, Collapsing Borders, Curse of Vitality, Fields of Summer, Sea of
Sand, Spiritual Sanctuary, Angel of Destiny), `you()` for Armadillo Cloak and Spirit Loop ("you gain"; the collector
bakes the Aura's controller, which is the entry's controller) and `targetPlayerDeclaredElsewhere()` for Cease // Desist
(a spell: the mode's player target); `TRIGGERING_PERMANENT_CONTROLLER` → `controllerOf(triggering())`;
`SOURCE_REMEMBERED_PLAYER` → `chosenPlayer()`; `EACH_PLAYER` → `eachPlayer()`. `TargetPlayerGainsLifeEffect(a[, g])` →
`GainLifeEffect(a, targetPlayer([g]))`. 79 card files (41 named the enum, 38 the record), 4 application/AI test files.

**Resolver.** `chosenPlayer()` gained the old `SOURCE_REMEMBERED_PLAYER` fallback: while the enters ability carrying
`RememberTargetPlayerEffect` from the same source is still on the stack, its target (Laquatus's Champion ruling
2018-03-16: "or, if that ability is still on the stack, the player who is its target").

**AI parity.** The AI readers of `LifeGainEffect` (`SpellEvaluator` x3, `InstantCategoryClassifier`) skip a gain
whose `lifeGainingPlayer().declaresTarget()`: exactly the old `TargetPlayerGainsLifeEffect` set (not a
`LifeGainEffect`); the old `GainLifeEffect` uses, Phelddagrif's and Cease // Desist's non-declaring reads included,
are scored as before. The mana-ability path (`ActivatedAbilityExecutionService`) accepts `you()` and `each*` and shares
`nonTargetedLifeRecipients` with life loss (seat order, as before).

**Behaviour changes.** (1) Every recipient now passes the resolving card as the life gain's source; `TARGET_CONTROLLER`
and `OPPONENT` passed none, so "whenever a white instant or sorcery spell causes you to gain life" missed Last Breath /
Condemn on your own creature. Test: `FiresongAndSunspeakerTest.whiteSpellGivingTargetControllerLifeTriggersDamage`.
(2) "Each opponent" gains are not targeted (CR 115.10, CR 702.11c), so a hexproof or shroud opponent still gains;
`OPPONENT` skipped them. Test: `GrollubTest.hexproofOpponentStillGainsLife`. (3) Soldevi Steam Beast's controller
chooses the opponent as the trigger goes on the stack; `SoldeviSteamBeastTest` now answers that prompt, and its
hexproof case (trigger removed, no gain) is unchanged. Not tested, no card depends on them: several players gain in
APNAP order instead of seat order; Curse of the Forsaken's creature that left with no removal record now uses the
recorded triggering controller (CR 608.2h) instead of nobody.

**For later runs.**

- Kept (ledger): `EachTargetPlayerGainsLifeEffect` (Hunters' Feast, Reward the Faithful) evaluates its amount once in
  the caster's context; folding it into `targetPlayer()` would re-base Reward the Faithful's "permanents you control" to
  each target, and the AI scores it as own gain. It also still passes no life-gain source (the Firesong gap of change 1).
  Fold it with a flag for the caster's amount context. `TargetSpellControllerGainsLifeEqualToManaValueEffect` needs
  `controllerOf(targetSpell())`.
- 4d–4i: "target opponent" constants that derive the opponent without declaring (two-player `getOpponentId`) split by
  Oracle text into `eachOpponent()`, `targetPlayerDeclaredElsewhere()` (the ability's filter declares it) and a real
  declared target; a trigger that newly declares one prompts its controller in tests.

### Step 4, run 4d (2026-11-19)

**Survivor.** `DealDamageToPlayersEffect`'s `DamageRecipient recipient` became `PlayerSubject player` (every constructor
kept with the subject in the recipient's place) plus a last component `playerOrTheirPlaneswalker` (factory
`toPlayerOrTheirPlaneswalker(int, player)`, Curse of the Pierced Heart's resolution-time "that player or a planeswalker
that player controls" choice, kept as a verb option). The handler resolves the players through
`SubjectResolutionService` and keeps, per subject shape, what the old handler did: one `dealDamageToPlayer` call per
player, one `checkWinCondition`, the `EVENT_DAMAGE_SOURCE` / `TRIGGERING_PERMANENT` dealing entry, `recordDamageDealt`,
`unpreventable`; the amount is evaluated once per receiving player in the entry's context, re-based as controller for
`eachPlayer()` and as target for `defendingPlayer()` (the old `EACH_PLAYER` / `DEFENDING_PLAYER` contexts), and
`attachedCountFilter` counts per receiving player; `you()` and `controllerOf(target())` check only the entry's own
prevention with their old log lines (the other subjects also check instant/sorcery and targeted-spell prevention, as
before); `controllerOf(targetSpell())` skips a non-positive amount. `targetSpec()`: benign player iff
`player.declaresTarget()`, benign permanent for `controllerOf(target())`, benign spell for `controllerOf(targetSpell())`
(exactly the old `TARGET_PLAYER` / `TARGET_PERMANENT_CONTROLLER` / `TARGET_SPELL_CONTROLLER` specs).
`damagesController()` is `player instanceof You`.

**Deleted**: the enum `DamageRecipient`, `DealDamageToEndStepPlayerEffect` and `DealDamageToTriggeringSpellControllerEffect`
with their handlers (ratchet 4,932 / 4,165). Mapping: `TARGET_PLAYER` → `targetPlayer()` everywhere (it always
declared; Psychic Allergy's upkeep trigger and engine-built payloads included); `EACH_OPPONENT` / `EACH_PLAYER` →
`eachOpponent()` / `eachPlayer()`; `CONTROLLER` → `you()`; `TRIGGERING_PLAYER`, `ACTIVE_PLAYER` (baked upkeep player) and
`ENCHANTED_PLAYER` (Maddening Hex, Curse of Shaken Faith, Curse of Thirst: "that player" / "them") →
`triggeringPlayer()`; `ENCHANTED_PLAYER_OR_PLANESWALKER` → `toPlayerOrTheirPlaneswalker(1, triggeringPlayer())`;
`EACH_OTHER_OPPONENT` → `except(eachOpponent(), triggeringPlayer())`; `SELECTED_OPPONENTS` → `except(acceptedPlayers(),
you())` (Snort, through `selectedOpponents`); `CHOSEN_PLAYER` → `chosenPlayer()`; `DEFENDING_PLAYER` → `defendingPlayer()`;
`TARGET_PERMANENT_CONTROLLER` → `controllerOf(target())`, except Insubordination ("deals 2 damage to that player" at the end step of enchanted creature's controller), which is `activePlayer()`: its end-step record bakes the enchanted creature as the target and the old lookup preferred the recorded controller, so `InsubordinationTest`'s control-change case pinned the player whose end step it is; `TARGET_SPELL_CONTROLLER` → `controllerOf(targetSpell())`;
`TRIGGERING_PERMANENT_CONTROLLER` → `controllerOf(triggering())`; `ENCHANTED_PERMANENT_CONTROLLER` split by Oracle text:
`triggeringPlayer()` for the Aura upkeep cards ("that player" = the player whose upkeep it is, as the orchestrator's
note asked: Warp Artifact, Wanderlust, Parasitic Bond, Maddening Wind, Leeching Licid, Festering Wound, Feedback, Cursed
Land, Mind Whip, Curse Artifact, Unnatural Hunger) and Mogis; `controllerOf(triggering())` for "that creature's
controller" / "that player" of a tapped or entering permanent (Insolence, Lust for War, Seizures, Tourach's Chant,
Thelon's Chant); `targetPlayerDeclaredElsewhere()` for Demanding Dragon (its target filter declares the opponent). The
upkeep `ENCHANTED_CONTROLLER` step record sets only the baked `targetId` (no triggering permanent, no triggering player),
so the old "live controller of the triggering permanent" branch was never reached for those cards and
`triggeringPlayer()` reads the same baked player; the tapped record sets the tapped permanent and its controller as both
the triggering controller and the baked target, so `controllerOf(triggering())` gives the old answer.
`DealDamageToEndStepPlayerEffect(a)` → `DealDamageToPlayersEffect(a, triggeringPlayer())` (6 cards; same context, same
prevention); `DealDamageToTriggeringSpellControllerEffect(n)` → `DealDamageToPlayersEffect(n,
controllerOf(triggeringSpell()))` (Retromancer, Thunderbreak Regent, Bonecrusher Giant). The two other records naming
the enum now take a `PlayerSubject`: `SacrificeOtherCreatureOrDamageEffect` (`you()`; `triggeringPlayer()` for Unnatural
Hunger, resolved through the service; the handler gained the resolver) and `TargetCreatureDealsDamageToControllerEffect`
(`controllerOf(target())` default, `you()` for the half-life token handler; a mode, the handler still uses each
damaging creature's controller). 659 card files rewritten by a deleted scratch script (649 named the enum, 9 the absorbed
records, plus a stray import), 48 engine, 10 AI and 14 application-test files.

**New variants.** `targetSpell()` and `triggeringSpell()` (permanent subjects naming a stack object, meant only inside
`controllerOf`; they name no permanent): `controllerOf(targetSpell())` is the targeted spell's controller on the stack,
else `counteredSpellControllerId`, else the entry's last-known controller of that id (the old `TARGET_SPELL_CONTROLLER`
chain); `controllerOf(triggeringSpell())` is `CopySupport.triggeringSpell`'s controller. `acceptedPlayers()` reads
`StackEntry.getAcceptedPlayerIds()`. `chosenPlayer()` on an entry with no source permanent now reads the entry's
player target (Backdraft's spell, whose first effect writes the chosen player there; the old `CHOSEN_PLAYER` fallback).
The resolver's APNAP rotation tolerates a `null` active player (Mockito handler tests).

**Other readers.** `DestructionSupport.resolveForcedCostElseEffects` handles `you()` as before and, for the six
enchanted-controller-may-pay cards, `triggeringPlayer()` / `controllerOf(triggering())` /
`targetPlayerDeclaredElsewhere()` by reading the baked `targetId` exactly as the old `ENCHANTED_PERMANENT_CONTROLLER`
branch did (no other card has those subjects in a forced-cost else-branch). `DamageTargetValidators` requires a player iff
the subject declares; `DealDamageToTargetCreatureEffectHandler`'s excess-damage check matches `controllerOf(target())`;
the mana-ability path (`ActivatedAbilityExecutionService`) and the AI (`TargetPolarityClassifier`, `SpellEvaluator`,
`InstantCategoryClassifier`, `AiManaManager`, `GameSimulator`) match `you()` where they matched `CONTROLLER` and
`declaresTarget()` where they matched `TARGET_PLAYER` (only `targetPlayer()` declares, so the AI sees the same sets;
Demanding Dragon's non-declaring read was not `TARGET_PLAYER` before either). The folded siblings implemented only
`DamageDealingEffect`, which the survivor implements, with the same `damageAmount`, `canDamage*` and (for these
subjects) `damagesController() == false` and no target spec.

**Behaviour differences** (none changes a tested outcome): several players are dealt damage in APNAP order (CR 101.4)
instead of seat order; `triggeringPlayer()` prefers the recorded triggering player over the baked target (the same
player for every collector these cards use); `targetPlayer()` reads the effect's bound or flat targets before the
single `targetId`, so an effect with several player targets damages each; `controllerOf(target())` reads the permanent's
current controller before the recorded triggering controller, so Heat of Battle / Battle Strain (the only other trigger users) damage the blocker's
current controller after a control change in response ("that creature's controller", CR 608.2h);
`controllerOf(triggering())` has no last fallback to the baked `targetId` (every damage collector records the
triggering controller, a spell source's included); `defendingPlayer()` no longer needs `attackedTargetId` when
`defendingPlayerId` is set and handles a battle's protector; a permanent's `chosenPlayer()` with nothing remembered
looks for its pending choose-a-player enters ability instead of the baked target.

**For later runs.**

- Kept (ledger): `DealDamageToEnchantedPermanentControllerEffect` (Orcish Mine, Gremlin Infestation, Impending Doom,
  Smoke Spirits' Aid) has its own Aura-gone fallback chain and an enchanted-permanent-as-source swap that differ from
  `controllerOf(attached())` / `controllerOf(triggering())` + `dealtByTriggeringPermanent()`; fold it with tests of the
  host-left cases. `DealDamageToRandomOpponentEffect` (a random pick is a choice) and
  `DealDamageToCombatOpponentControllerEffect` (harmful permanent spec over a baked target) stay.
- Now expressible: `controllerOf(targetSpell())` unblocks `TargetSpellControllerLosesLifeEffect` (4b),
  `TargetSpellControllerGainsLifeEqualToManaValueEffect` (4c), Mill's `TARGET_SPELL_CONTROLLER` (4f) and
  `UpkeepDrawRecipient.TARGET_SPELL_CONTROLLER`; `acceptedPlayers()` is the step-7 accepted-players read.
- Not a gap (checked after 4d): `you()` and `controllerOf(target())` damage skips only the handler's early
  `isDamageSourcePreventedWithLog` check; `dealDamageToPlayer`'s pipeline (`PlayerDamagePreventions`) still applies
  instant/sorcery, targeted-spell, colour, chosen-source and protection prevention. `EnergyStormTest` covers Psionic
  Blast and Chandra's Outrage. The early check is the questionable part: it decides prevention without the CR 616.1
  order choice the pipeline offers. Dropping it for every subject changes logs and prompts across many cards; that
  needs the user's sign-off and is not part of this plan.
- Per-receiving-player damage siblings (`DealDamageToEachOpponentEqualTo*`, `DealDamageToEachPlayerControllingMatchingPermanentEffect`)
  need a per-player amount context or a filtered `each`, as 4b noted for life loss.

### Step 4, run 4e (2026-11-19)

**Survivors.** `DiscardEffect`, `DiscardHandEffect` and `DiscardCardThenEffect` replaced `DiscardRecipient recipient` with
`PlayerSubject player` (every constructor kept with the subject in the recipient's place; no-arg / filter-only
`DiscardHandEffect` and `DiscardCardThenEffect` constructors are `you()`). `targetSpec()`: `DiscardEffect` and
`DiscardHandEffect` declare a benign player target iff `player.declaresTarget()` (the old `TARGET_PLAYER` spec);
`DiscardCardThenEffect` never declared a player, so its constructor rejects a declaring subject. The three handlers resolve
the players through `SubjectResolutionService`. `DiscardEffectHandler` keeps its three paths, picked by subject shape:
a `TargetPlayer` with several players chooses and discards one player after another (the old multi-target
`DiscardFollowUp.eachPlayer` queue); `Each` (and any other subject naming several players) makes every player choose in
APNAP order and then discards the chosen cards at once (`eachPlayerRummage`, deferred discards: CR 101.4, so `each(...)`
is accepted), random ones inline; one player goes through the single-player path (zero-amount log, drawn-card-only,
stop-after-type/predicate). `discardCausedByOpponent` is "discarding player is not the controller" everywhere (the old
`DEFENDING_PLAYER` `true` and `CONTROLLER` `false` are the same values). `DiscardHandEffect` sets the event value only for
`you()`, as before; its other players discard one hand at a time (no choice to make). Discard triggers, madness,
"if you do" follow-ups, drawn-card and last-discarded recording are untouched (same `PlayerInteractionSupport` /
`PlayerInputService` / `discardHand` calls).

**Deleted**: the enum `DiscardRecipient`, `EachTargetPlayerDiscardsEffect` and `TargetSpellControllerDiscardsEffect` with
their handlers (ratchet 4,930 / 4,163). Mapping: `CONTROLLER` → `you()`; `TARGET_PLAYER` → `targetPlayer()` for every
`DiscardEffect` / `DiscardHandEffect` use (it always declared; engine-built payloads included, so `MayEffectHandler`'s
"empty hand, skip the may" check and the AI see the same set), and for the two non-declaring `DiscardCardThenEffect` uses:
`triggeringPlayer()` (Curse of Chaos, "that attacking player", the same player its draw already reads) and
`targetPlayerDeclaredElsewhere()` (Furnace Layer's engine-built discard, the trigger's random player target);
`TRIGGERING_PLAYER` and `ACTIVE_PLAYER` (Necrogoyf, Necrogen Mists, Gibbering Descent, Doom Foretold, Bottomless Pit:
"that player" of an upkeep trigger) → `triggeringPlayer()` (baked player kept); `TRIGGERING_PERMANENT_CONTROLLER` →
`controllerOf(triggering())`; `TARGET_PERMANENT_CONTROLLER` → `controllerOf(target())`; `TARGET_PLAYER_OR_PERMANENT_CONTROLLER`
(Blightning, Rakdos's Return, Nicol Bolas, Planeswalker, Chain of Plasma) → `allOf(targetPlayerDeclaredElsewhere(),
controllerOf(target()))`; `DEFENDING_PLAYER` → `defendingPlayer()`; `EACH_PLAYER` / `EACH_OPPONENT` → `eachPlayer()` /
`eachOpponent()`. `EachTargetPlayerDiscardsEffect(n)` → `DiscardEffect(n, targetPlayer())` (Ral Zarek, Guest Lecturer);
`TargetSpellControllerDiscardsEffect(n)` → `DiscardEffect(n, controllerOf(targetSpell()))` (Frightful Delusion, Dismal
Failure; its handler test moved into `DiscardEffectHandlerTest`). 686 card files rewritten by a deleted scratch script (683
named the enum, 3 the absorbed records), 18 engine, 1 AI and 11 application-test files.

**New variant.** `allOf(PlayerSubject...)` (`PlayerSubject.AllOf`): every player any part names, once, APNAP order; it
rejects a declaring part. **Resolver fix.** `controllerOf(targetSpell())` on an entry with no target now reads the
countered-spell controller the entry recorded: a counter-unless-pays rider entry carries only that
(`FrightfulDelusionTest`'s paid case failed without it; the old sibling read it).

**AI and other readers.** `SpellEvaluator` scores a non-random discard whose `player().declaresTarget()` (was
`TARGET_PLAYER`); `MayPayManaEffectHandler` matches `you()` where it matched `CONTROLLER`; `MayEffectHandler` matches `you()`
and `declaresTarget()`. The absorbed records implemented no capability interface.

**Behaviour differences** (none changes a tested outcome): several players discard in rotated APNAP order (CR 101.4)
instead of "active player, then seat order"; `triggeringPlayer()` prefers the recorded triggering player over the baked
target (the same player for every collector these cards use) and a missing one discards nothing instead of throwing;
"that planeswalker's controller" and `controllerOf(target())` fall back to the last-known controller once the permanent
left (unreachable for the damage cards: the planeswalker leaves by state-based action after the resolution); a single
Ral Zarek target discards through the single-player path rather than a one-player queue (same prompt, same flag).

**For later runs.**

- 4g: `SacrificeRecipient.TARGET_PLAYER_OR_PERMANENT_CONTROLLER` (Nicol Bolas, Planeswalker) is the same
  `allOf(targetPlayerDeclaredElsewhere(), controllerOf(target()))`. `allOf` also unblocks 4a's
  `DrawCardForYouAndDamagedPlayerEffect` (`allOf(you(), triggeringPlayer())`). `MayChoicePlayer` still has its own
  `TARGET_PLAYER` / `TARGET_PLAYER_OR_PERMANENT_CONTROLLER` constants (Chain of Plasma, Curse of Chaos); it is a who-axis
  for the "may" asker, not converted here.
- Kept (ledger): `TargetPlayerDiscardsByConvergeEffect` is the converge marker of `EffectResolution.hasConvergeEffect`;
  fold it with a converge `DynamicAmount`. `EachPlayerDiscards{DownToFewest,FractionOfHandRounded{Up,Down},NonlandCard}`
  need a per-player amount or a discard filter (`blocked-player-loop`). The rest of the family is fused discard + another
  instruction (step 8), including the subject siblings `{Active,Defending,Target,Each}Player...DiscardsHandThenDraws*`.
- `DiscardEffect`'s random each-player path discards one player at a time (no choice is made, so only the "at once" half
  of CR 101.4 differs: each player's discards are their own discard event).

### Step 4, run 4f (2026-11-19)

**Survivor.** `MillEffect`'s `MillRecipient recipient` became `PlayerSubject player` (constructors `(DynamicAmount|int,
PlayerSubject)`). `targetSpec()` is a benign player target iff `player.declaresTarget()`; `LibraryTargetValidators`
requires a player iff it declares. The handler resolves the players through `SubjectResolutionService` and keeps the
old amount contexts by subject shape: `targetPlayer()`, `triggeringPlayer()` and `chosenPlayer()` (the old
`TARGET_PLAYER` / `ACTIVE_PLAYER` / `TRIGGERING_PLAYER` / `CHOSEN_PLAYER`) evaluate the amount per milling player with
that player as the amount's target and mill one player after another; every other subject evaluates it once in the
entry's context. The mill itself is unchanged: the same `GraveyardService.resolveMillPlayer` /
`resolveMillPlayersSimultaneously` calls, so milled cards, `cardsMilledThisTurn`, the cards a caller gets back, the
replacement-order journal and the graveyard / exile triggers are recorded exactly as before (`MillEffectHandler` itself
records nothing on the entry; the fused `Mill...And...` handlers that read "cards milled this way" were not touched).

**Deleted**: the enum `MillRecipient`, `MillDefendingPlayerEffect` and `MillHalfDefendingPlayerEffect` with their
handlers (ratchet 4,928 / 4,161). Mapping: `CONTROLLER` → `you()`; `TARGET_PLAYER` → `targetPlayer()` everywhere (it
always declared; engine-built payloads too); `TRIGGERING_PLAYER` (Mindcrank, The Master of Lake-town, Memory Erosion) and
`ACTIVE_PLAYER` (Worry Beads, Dreamborn Muse, Curse of the Bloody Tome: "that player" of an upkeep trigger, the baked
player) → `triggeringPlayer()`; `CHOSEN_PLAYER` → `chosenPlayer()`; `EACH_OPPONENT` / `EACH_PLAYER` → `eachOpponent()` /
`eachPlayer()`; `TARGET_SPELL_CONTROLLER` → `controllerOf(targetSpell())` (non-declaring, as before: the counter effect
declares the spell; the resolver's stack → countered → last-known chain is the old one, and 4e's no-target fallback to
the countered controller is the old `getCounteredSpellControllerId()` step, so no mill card needed anything more);
`TARGET_PERMANENT_CONTROLLER` → `controllerOf(target())` (Zulaport Duelist, Riddlekeeper, Glacial Grasp);
`TRIGGERING_PERMANENT_CONTROLLER` → `controllerOf(triggering())` (Mesmeric Orb, Chronic Flooding, Belltower Sphinx).
`MillDefendingPlayerEffect(n)` → `MillEffect(n, defendingPlayer())` (Nemesis of Reason, Specimen Freighter, Flint
Golem); `MillHalfDefendingPlayerEffect(r)` → `MillHalfLibraryEffect(r, defendingPlayer())` (Terisian Mindbreaker, Lord
Xander, the Collector): `MillHalfLibraryEffect` gained a `PlayerSubject player` (default `targetPlayer()`, which keeps
its benign player spec). `RevealUntilLandsMillTargetPlayerEffect` (same enum) now takes a `PlayerSubject`:
`targetPlayer()` (default, harmful spec as before; Mirko Vosk's combat-damage binding unchanged), `controllerOf(target())`
(Destroy the Evidence), `eachOpponent()` (Mind Grind, Consuming Aberration). 424 card files rewritten by a deleted
scratch script (419 named the enum, 5 the absorbed records), 7 engine and 8 application-test files.

**AI and other readers.** No AI code read `MillEffect` or the enum. The mana-ability classifier
(`AbilityActivationService.movesCardToOrFromLibrary`) treats every `MillEffect` as moving a library card, as before.

**Behaviour changes.** (1) `eachOpponent()` mills all opponents at once as one event, like `EACH_PLAYER` always did
(CR 101.4, CR 608.2e); `EACH_OPPONENT` milled one opponent after another, so with two or more opponents a
"one or more cards are milled" batch saw separate events. Test: `MillEffectHandlerTest$EachOpponent.severalOpponentsMillSimultaneously`.
Not tested, no card depends on them (two-player harness): several players mill / reveal in APNAP order instead of seat
order (Mind Grind and Consuming Aberration still reveal one opponent after another: a reveal-until loop, not a count);
`triggeringPlayer()` prefers the recorded triggering player over the baked target (the same player for every collector
these cards use; their tests pass unchanged); `controllerOf(target())` falls back to the target's last-known controller
once it left (CR 608.2h; old `TARGET_PERMANENT_CONTROLLER` had only the triggering-controller fallback);
`controllerOf(triggering())` lost the old last fallback to `eventPlayerIds.first` (every collector the three cards use
records the triggering controller: `TapTriggers.tappedRecord`, `DamageTriggers.collectDealtDamageAbilities`; its
hand-built handler test was removed); the defending-player mills no longer need `attackedTargetId` when
`defendingPlayerId` is set and handle a battle's protector; a defending player's empty library logs "mills nothing"
like the target-player half mill.

**For later runs.**

- "Each player mills N" is still written on older cards as `MillEffect(N, you())` + `MillEffect(N, eachOpponent())`
  (Ghoulcaller's Bell, Chill of Foreboding, ...): two events where the rules make one. `MillEffect(N, eachPlayer())` is
  the rules-correct form; converting them changes "one or more cards are milled" batching, so it needs its own run with
  tests.
- Done (follow-up to 4f): the 26 "each player mills" cards now use one `MillEffect(N, eachPlayer())` (CR 101.4,
  CR 608.2e): Ashiok, Sculptor of Fears; Bond of Insight; Breach the Multiverse; Chill of Foreboding; Coram, the
  Undertaker; Disturbing Conversion; Extract from Darkness; Eye Collector; Fascination; Flitting Guerrilla; Ghoulcaller's
  Bell; Halo-Charged Skaab; Invasion of Amonkhet; King Narfi's Betrayal; Koma's Faithful; Mindwrack Harpy; Nezumi
  Freewheeler; Path of the Schemer; Returned Reveler; Sibsig Host; Syr Konrad, the Grim; The Binding of the Titans;
  Unseal the Necropolis; Vantress Gargoyle; Whetstone; Winds of Rebuke (five single-step `SequenceEffect` wrappers
  dropped). Flotsam // Jetsam keeps its two mills (different halves). Tests: `bothPlayersMillInOneEvent` in `GhoulcallersBellTest` and
  `ChillOfForebodingTest` (The Wise Mothman triggers once, not twice).
- `MillHalfLibraryEffect` could become `MillEffect` with a `Halved(DynamicAmount, roundUp)` amount over the milling
  player's library (`EFFECT_MERGE_PLAN.md`'s blocked row), evaluated per player like the target-player mill.
- The rest of the family (ledger) is fused mill + an instruction on "the cards milled this way": step 7 gives
  `resolveMillPlayer`'s returned cards a step result, then step 8 splits them.


### Step 4, run 4g (2026-11-19)

**Survivor.** `SacrificePermanentsEffect`'s `SacrificeRecipient recipient` became `PlayerSubject player` (every
constructor kept with the subject in the recipient's place). `targetSpec()` is a benign player target iff
`player.declaresTarget()`; `DestructionTargetValidators` requires a player iff it declares. The handler asks
`SubjectResolutionService` for the players: an `Each` or `Matching` subject, or any subject naming several players, goes
through the existing multi-player path (every player chooses in APNAP order, then everything chosen is sacrificed at once
through `performSimultaneousSacrifice` / the `ForcedSacrifice` queue; CR 101.4, CR 608.2e), one player through the
single-player path, nobody does nothing. That is exactly the old split (`EACH_*` and `PLAYERS_WITH_MOST_LANDS` used the
multi path even for one or no player). The choice flows, `sacrificeTogether` / `DepartureService.departNow`, the
recorded count and power, "can't be sacrificed", Sigarda's `canEffectCauseSacrifice` and The Master, Multiplied's token
check are untouched. `each(...)` is accepted: the handler already chose-then-acted.

**Deleted**: the enum `SacrificeRecipient`, `EachPlayerSacrificesCreatureEffect` and
`TriggeringSpellControllerSacrificesPermanentsEffect` with their handlers, plus the
`MultiPermanentChoiceContext.EachPlayerSacrificesCreature` context and its three branches in
`MultiPermanentChoiceHandlerService` (ratchet 4,926 / 4,159). Mapping: `CONTROLLER` → `you()`; `TARGET_PLAYER` →
`targetPlayer()` (always declared; the engine-built payloads of the may/unless handlers too, on carrier entries whose
`targetId` is the player); `ACTIVE_PLAYER` → `activePlayer()` (live, as before); `TRIGGERING_PERMANENT_CONTROLLER` →
`controllerOf(triggering())` (the old inline chain is `findTriggeringPermanentController`'s); `TARGET_PLAYER_OR_PERMANENT_CONTROLLER`
→ `allOf(targetPlayerDeclaredElsewhere(), controllerOf(target()))` (Nicol Bolas, Planeswalker); `EACH_PLAYER` /
`EACH_OPPONENT` → `eachPlayer()` / `eachOpponent()`; `PLAYERS_WITH_MOST_LANDS` → `matching(eachPlayer(), new
PlayerControlsMostLandsPredicate())` (Tectonic Hellion; rulings 2019-08-23: ties all sacrifice, decided as the ability
resolves); `DEFENDING_PLAYER` → `defendingPlayer()`; `CONTROLLER_AND_TARGET_PLAYER` was dead (no card, no engine
construction). `EachPlayerSacrificesCreatureEffect()` → `SacrificePermanentsEffect(1, creature, eachPlayer())` (Wasteland
Raider, Rankle, Pitiless Trickster, Grave Studies); `TriggeringSpellControllerSacrificesPermanentsEffect(n, f)` →
`SacrificePermanentsEffect(n, f, controllerOf(triggeringSpell()))` (Lava Runner; same `CopySupport.triggeringSpell`
lookup, same single-player path). `ChooseKeptPermanentOfEachTypeThenSacrificeRestEffect` (same enum) now takes a
`PlayerSubject players` that must be an `Each` (default `eachPlayer()`, Liliana, Dreadhorde General `eachOpponent()`),
resolved through the service. 305 card files (301 named the enum, 4 the absorbed records), 26 engine (plus the 2
deleted handlers) and 9 application-test files.

**New variant.** `matching(players, PlayerPredicate)` (`PlayerSubject.Matching`): the players `players` names who match
the filter as the effect is applied, evaluated by `TargetLegalityService.matchesPlayerPredicate` with the effect's
controller, source and triggering player (the resolver gained that dependency; Mockito tests pass a mock). New predicate
`PlayerControlsMostLandsPredicate` (no other player controls more lands). It is a separate record, not a filter on
`Each`, so the readers that treat `Each` as "everyone" (life loss / gain on mana abilities, AI) don't see a filtered set
as everyone. Test: `SubjectResolutionServiceTest$Players.matchingPlayers`.

**AI and other readers.** No AI code reads `SacrificePermanentsEffect`. `EffectResolutionService` (target player may skip
a sacrifice it can't make) and `MayEffectHandler` match `declaresTarget()` where they matched `TARGET_PLAYER` and `You`
where they matched `CONTROLLER`. The absorbed records implemented no capability interface.

**Behaviour changes.** (1) Wasteland Raider, Rankle and Grave Studies now use the survivor's eligibility, which honours
The Master, Multiplied ("triggered abilities you control can't cause you to sacrifice ... creature tokens you
control"): the old each-player handler offered a protected token. Test:
`WastelandRaiderTest.protectedCreatureTokenIsNotOffered`. They now also log "has no matching permanents to sacrifice" for
a player with no creature. Not tested, no card depends on them: a `targetPlayer()` sacrifice reads the effect's bound
targets before the entry's `targetId` (no sacrifice card has several targets); the planeswalker controller of Nicol Bolas's
-9 falls back to its last-known controller; `defendingPlayer()` handles a battle's protector.

**For later runs.**

- `matching(...)` is the filtered `each` 4b and 4d asked for (`EachOpponentWith{EmptyHand,PoisonCounters}LosesLife`,
  `DealDamageToEachPlayerControllingMatchingPermanentEffect`, ...): give it the `PlayerPredicate` they need. Readers that
  switch on `Each` must decide per verb whether a `Matching` behaves like it.
- Kept (ledger): `EachPlayerSacrifices{DownToCount,DownToFewest,FractionRoundedDown,FractionRoundedUp}` need a
  per-player amount; the survivor's `countPerSacrificingPlayer` evaluates the count per player, so they fold once a
  "matching permanents you control minus N" / halved `DynamicAmount` exists. The rest of the `*Sacrifices*` siblings are
  fused (step 7/8) or one-card sacrifices with two filters. `SacrificeEachMatchingPermanentEffect` (no choice) and
  `SacrificePermanentDamagedPlayerControlsEffect` are permanent verbs for step 5.

### Step 4, run 4h (2026-11-19)

**Survivors.** Poison: `GivePoisonCountersEffect`'s `PoisonRecipient recipient` became `PlayerSubject player` (constructors
`(int|DynamicAmount, PlayerSubject)`). Rad: the five `Give*RadCounters*` siblings (six records: `GiveEachPlayerRadCounterEffect`
and `GiveEachPlayerRadCountersEffect` were duplicates) fold into one `GiveRadCountersEffect(int|DynamicAmount,
PlayerSubject)`. It is the most used record, `GiveTargetPlayerRadCountersEffect`, renamed: every rad call site was
rewritten anyway (20 card files), and a "target player" name with an `eachPlayer()` subject would mislead. Energy
(`EnergyCountersEffect`) and experience (`ExperienceCountersEffect`) have no subject siblings and only ever act on the
controller, so they got no subject (ledger `survivor`). Both survivors declare a benign player target iff
`player.declaresTarget()`; `LifeTargetValidators` requires a player for poison iff it declares. Both handlers evaluate the
amount once in the entry's context (rad: the defending player is the amount's target for `defendingPlayer()`, as the old
defending handler did) and ask `SubjectResolutionService` for the players.

**Deleted**: the enum `PoisonRecipient`; `GiveTargetPlayerPoisonCountersEffect`, `GiveTargetPlayerRadCountersEffect`,
`GiveControllerRadCountersEffect`, `GiveDefendingPlayerRadCountersEffect`, `GiveEachOpponentRadCountersEffect`,
`GiveEachPlayerRadCounterEffect`, `GiveEachPlayerRadCountersEffect` and six handlers (the target-rad handler became
`GiveRadCountersEffectHandler`, the mana-ability rider handler `GiveRadCountersManaAbilityEffectHandler`, `you()` only);
`GiveRadCountersEffect` is new (ratchet 4,920 / 4,153). Mapping: `CONTROLLER` → `you()`; `TARGET_PLAYER` →
`targetPlayer()` everywhere (it always declared; the Sabertooth Cobra upkeep payload and `CreateEmblemEffect`'s Javadoc
example too); `EACH_PLAYER` / `EACH_OPPONENT` → `eachPlayer()` / `eachOpponent()`; `TARGET_PERMANENT_CONTROLLER` →
`controllerOf(target())` (Noxious Assault, Norn's Decree, Pistus Strike: the resolver's chain is the old one, current →
`removedPermanentControllers` → recorded triggering controller); `TRIGGERING_PERMANENT_CONTROLLER` →
`controllerOf(triggering())` (`findTriggeringPermanentController`, as before). Rad: the target/controller/defending/
each-opponent/each-player records → `targetPlayer()` (declared, as before, also on the combat-damage and cast triggers
of Vexing Radgull, Infesting Radroach, Glowing One, Nuka-Nuke Launcher, Struggle for Project Purity) / `you()` /
`defendingPlayer()` / `eachOpponent()` / `eachPlayer()`. `DestructionSupport` (Sabertooth Cobra's else-branch) and
`GivePoisonCountersManaAbilityEffectHandler` match `You` where they matched `CONTROLLER`. 61 card files rewritten by a
deleted scratch script (40 named the enum, 21 the absorbed records), 3 domain, 8 engine and 2 application-test files. No AI
code read any of these classes; none implemented a capability interface.

**Kept exactly.** Poison still goes only through `LifeSupport.applyPoisonCounters` (Melira, can't-get-poison, counter
replacements, one "you put counters" event per player); the ten-counter loss stays the state-based action (CR 704.5c),
and `DestructionSupport`'s `checkWinCondition` call is unchanged. Proliferate (`MultiPermanentChoiceHandlerService`),
infect/toxic damage (`DamageSupport`, `CombatDamageService`) and `CounterTriggers` were not touched.

**Behaviour changes.** The rad handlers disagreed: only the target-player and `GiveEachPlayerRadCountersEffect` paths
raised the "you put counters on a player" event, only `GiveEachPlayerRadCounterEffect` applied counter replacements, and
the defending/each-opponent paths ignored "players can't get counters". Now every rad placement is
`GameQueryService.replaceRadCounters` then `LifeSupport.applyRadCounters` (the mana rider too). (1) Vorinclex,
Monstrous Raider ("If you would put one or more counters on a permanent or player, put twice that many ...") doubles
rad counters its controller gives. Test: `NightkinAmbusherTest.vorinclexDoublesRadCountersGivenToTargetPlayer`. (2) All
Will Be One (ruling 2023-02-04: "will trigger any time you put one or more counters on a permanent or player") triggers on
rad counters from the controller, defending-player, each-opponent and Vault 12 / Scorchbeast / Nuclear Fallout / Megaton's
Fate paths. Test: `FeralGhoulTest.radCountersOnOpponentTriggerAllWillBeOne`. Rad logs now name the source. Not tested,
no card depends on them: several players get poison / rad in APNAP order instead of seat order; a `targetPlayer()` reads
the effect's bound targets before the entry's `targetId`.

**For later runs.**

- Proliferate's rad step (`applyRadCounters` without `replaceRadCounters`) and `ExperienceCountersEffectHandler` (no "you
  put counters" event) still skip what the rad verb now does; both were out of scope (proliferate kept as is).
- An "each player" poison / rad is one event per player, as before; whether it should be one batched event for "you put
  counters" triggers is undecided (All Will Be One's rulings only settle simultaneous toxic damage).
- `CounterUnlessGetsPoisonCountersEffect` is fused (step 8); `EachOpponentWithPoisonCountersLosesLifeEffect` can now use
  `matching(eachOpponent(), <has poison counters>)` (4b's ledger row).

### Step 4, run 4i (2026-11-19)

**Survivors** (each replaced its enum with a `PlayerSubject` component, every constructor kept with the subject in the
enum's place, and resolves it through `SubjectResolutionService`; each declares a benign player target iff
`declaresTarget()`): `PlayerDestroysPermanentsEffect` (`DestroyRecipient`; one player), `DraftCardFromSpellbookEffect`
(`DraftCardRecipient`), `AnteTopCardEffect` (`AnteRecipient`; rejects a declaring subject),
`EachPlayerLosesFractionOfLifeRoundedUpEffect` (`FractionalLifeLossRecipient`; the unused `(int, boolean)` constructor and
`opponentsOnly()` accessor are gone, the `opponentsOnly(int)` factory stays), `ReturnTopCreatureCardFromGraveyardToBattlefieldEffect`
(`GraveyardOwner`, component `graveyardOwner`), `CreateEmblemEffect` (`EmblemRecipient`), `SetLifeTotalEffect`
(`SetLifeTotalRecipient`), `SkipNextEffect` (`SkipRecipient`), and the library-inspection trio `ScryEffect`,
`ReorderTopCardsOfLibraryEffect`, `RevealTopCardOfLibraryEffect` (`LibraryOwner`; component `player`, the old silent
fallback to the controller when the subject names nobody kept). No record or handler deleted: ratchet unchanged
(4,920 / 4,153).

**Deleted enums**: `DestroyRecipient`, `DraftCardRecipient`, `AnteRecipient`, `FractionalLifeLossRecipient`,
`GraveyardOwner`, `EmblemRecipient`, `SetLifeTotalRecipient`, `SkipRecipient`. Mapping: `CONTROLLER` → `you()`;
`TARGET_PLAYER` → `targetPlayer()` everywhere it declared (Burning of Xinye, Garruk, Apex Predator, the `SetLifeTotal` /
`SkipNext` / library spell and ability targets, Ral Zarek, Guest Lecturer's nested skip), except
`DraftCardRecipient.TARGET_PLAYER` (never declared) → `targetPlayerDeclaredElsewhere()` (Break Expectations) and
`GraveyardOwner.TARGET_PLAYER` → `defendingPlayer()` (Bone Dancer: the unblocked-attack collector records the defender
as `defendingPlayerId`); `SetLifeTotalRecipient.TARGET_PLAYER` on Master of Cruelties ("that player's life total
becomes 1", a non-targeting attack trigger whose collector baked the defender) → `defendingPlayer()`;
`EACH_PLAYER` / `EACH_OPPONENT` / `OPPONENTS` / `ALL` → `eachPlayer()` / `eachOpponent()`; `SkipRecipient.DAMAGED_PLAYER`
→ `triggeringPlayer()`; `EmblemRecipient.EACH_PLAYER_DEALT_DAMAGE_THIS_WAY` → `playersDealtDamageThisResolution()`;
`FractionalLifeLossRecipient.SUBGAME_NON_WINNERS` → `except(eachPlayer(), subgameWinners())`. `LibraryOwner` on effects:
`OPPONENT` → `eachOpponent()` (Ingenious Mastery; first opponent, as the two-player `getOpponentId` was),
`DYING_CREATURE_CONTROLLER` → `controllerOf(triggering())` (Gardens of Tranquil Repose), `ENCHANTED_PERMANENT_CONTROLLER`
→ `triggeringPlayer()` (Paroxysm's reveal). The scrying player is the library's owner except for a `targetPlayer()`
library with a `LibraryDecisionMaker.CONTROLLER` (Ransack), exactly the old switch. 73 card files rewritten (a deleted
scratch script for `LibraryOwner`, sed for the rest), the engine reader (`ConditionEvaluationService`'s
Burning of Xinye check matches `You`), `LibraryTargetValidators`, 4 application test files.

**`LibraryOwner` kept** (ledger `blocked-other`), trimmed to `CONTROLLER` and `ENCHANTED_PERMANENT_CONTROLLER`: only the
conditions `TopCardOfLibraryType` / `TopCardOfLibraryMatchesPredicate` read it now (subjects in conditions are a
follow-up). Paroxysm reads the same player in both places: its reveal's `triggeringPlayer()` falls back to the entry's
baked `targetId` (the upkeep `ENCHANTED_CONTROLLER` record sets no triggering player), and the condition reads
`ConditionContext.targetId()`, the same id; `ParoxysmTest` passes.

**New variants** (step results, `needsStackEntry()`): `playersDealtDamageThisResolution()` (`StackEntry.playersDealtDamageThisResolution`)
and `subgameWinners()` (the entry's `SubgameResult.winners()`; resolving it with no recorded result throws, as the old
handler did). Tests: `SubjectResolutionServiceTest$Players.playersDealtDamageVariant` / `subgameWinnersVariant`.
`EachPlayerLosesFractionOfLifeRoundedUpEffectHandler` now clears any subgame result the entry carries (only Shahrazad
sets one).

**AI and other readers.** No AI code reads a converted subject (`SpellEvaluator` reads only `ScryEffect.count()`).

**Behaviour differences** (none changes a tested outcome): several players ante, skip, get emblems, set life totals or
lose a fraction of life in APNAP order (CR 101.4) instead of seat order (`SkipNext` used the unordered `playerIds`);
`targetPlayer()` reads the effect's bound or flat targets before the entry's `targetId`, so a skip with several player
targets skips each (no card has several); Master of Cruelties' trigger no longer carries a player target spec (its
collector already made the entry non-targeting); Gardens of Tranquil Repose's "its controller" is the dying creature's
controller through `findTriggeringPermanentController` (same player, step 3).

**Lesson.** A `targetPlayer()` nested in a `SequenceEffect` bound to another target group reads that group: Yosei, the
Morning Star's skip (player in group 0, the permanents in group 1) needs `targetPlayer(0)`; the old handler's
`getTargetId()` → first player in `getTargetIds()` hid this. `YoseiTheMorningStarTest` caught it.

**Tests**: 261 classes (every rewritten card's test, a 30-card sample of `ScryEffect`, all cards of the other
survivors, the touched handler / resolver / floating-effect / route / ratchet tests, `SpellEvaluatorTest`): 4,748 test
cases, all pass after the Yosei fix.

**Left for a run 4j** (done, see run 4j): `LibraryScope` (7 records: `ExileTopCardsToSource`, `ExileBottomCardsToSource`,
`ExileTopCardsAndMayCastSpells`, `ExileTopCardsChooseOneMayPlayUntil{NextEndStep,NextTurn}`,
`EachPlayerExilesTopUntilNonlandAndMayCastSpells`, `LookAtTopCardsRevealTwoTypesToHandThenRest`; about 40 cards, plus
`GameViewProjectionFactory` and `ExileTopCardsToSourceEffectHandlerTest`). Its 16 `TARGET_OPPONENT` uses must be split
by Oracle text (step 3's table: `defendingPlayer()` for attack triggers, `triggeringPlayer()` for combat damage, a
declared `targetPlayer` for `targetedOpponent`, `eachOpponent()` for the sole opponent), and each handler supports a
different subset, so it is a run of its own. When it is done, step 4 is done.

### Step 4, run 4j (2026-11-19)

**Survivors** (each replaced `LibraryScope` with a `PlayerSubject` component resolved through
`SubjectResolutionService`; constructors kept with the subject in the enum's place): `ExileTopCardsToSourceEffect`
(`player`), `ExileBottomCardsToSourceEffect` (`player`; rejects a declaring subject, as its handler found nobody for the
target constants), `ExileTopCardsAndMayCastSpellsEffect` (`player`), `ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect`
and `...UntilNextTurnEffect` (`player`), `EachPlayerExilesTopUntilNonlandAndMayCastSpellsEffect` (`players`),
`LookAtTopCardsRevealTwoTypesToHandThenRestEffect` (`player`; a non-`you()` subject with no fixed `playerId` is split into
one `forPlayer` copy per resolved player, as `EACH_PLAYER` was). The `targetedOpponent` flag of `ExileTopCardsToSource`
and `ExileTopCardsAndMayCastSpells` is gone: it was set exactly on the declaring `TARGET_OPPONENT` uses, so "declares a
target" is now `player.declaresTarget()`. Target specs are unchanged: ToSource harmful `player()` (old `TARGET_PLAYER` or
`targetedOpponent`), CastSpells harmful opponent players, the two ChooseOne records benign `player()`; no group anywhere.
No record or handler deleted: ratchet unchanged (4,920 / 4,153).

**Deleted**: the enum `LibraryScope`. Mapping: `CONTROLLER` → `you()`; `TARGET_PLAYER` → `targetPlayer()` (Mindreaver,
Circu, Dimir Lobotomist, Jester's Scepter, You Find Some Prisoners, Casey Jones & Raphael; all declared before);
`TRIGGERING_PLAYER` → `triggeringPlayer()` (Share the Spoils); `EACH_PLAYER` / `EACH_OPPONENT` → `eachPlayer()` /
`eachOpponent()`. The 16 `TARGET_OPPONENT` uses by Oracle text: declared target (the old `targetedOpponent = true`, same
spec) → `targetPlayer()`: Ashiok, Nightmare Weaver; Court of Locthwain; Expensive Taste; Grimoire Thief ("target
opponent", so not the step-3 table's `eachOpponent()`: it always declared); Laughing Jasper Flint; Maralen, Fae
Ascendant; Outrageous Robbery; Petty Larceny; and the `targetedOpponent` factories (Gix, Yawgmoth Praetor; Doom Reigns
Supreme). Non-declaring: "that player" of a combat-damage trigger → `triggeringPlayer()` (Nightveil Specter, Rogue Class,
Reno and Rude, Ramirez DePietro, Pillager, The Omenkeel, Kotis, the Fangkeeper); "that player" of a draw trigger →
`triggeringPlayer()` (Ian Malcolm, Chaotician; `LibraryTriggers` bakes the drawing player); "defending player" →
`defendingPlayer()` (Robber of the Rich). 36 card files rewritten (a deleted scratch script, plus the 18 dropped
`targetedOpponent` arguments and When Will You Learn's positional flag), the 7 handlers, `GameViewProjectionFactory`
(the Urianger "may look while deciding" check matches `PlayerSubject.You` where it matched `CONTROLLER`: same cards, same
viewer) and `ExileTopCardsToSourceEffectHandlerTest` (its manual constructor takes the resolver; the "falls back to the
sole opponent with no target" case became an `eachOpponent()` case, since no card relies on that fallback any more).

**AI and other readers.** No AI, networking or webservice code read the enum or `targetedOpponent()`.

**Behaviour differences** (none changes a tested outcome): each-player / each-opponent exiles happen in APNAP order
(CR 101.4) instead of seat order, and Look-at's per-player split uses the resolver's rotated APNAP order instead of
active player then seat order (same for two players); the non-declaring `TARGET_OPPONENT` lost its last fallbacks
(attacked planeswalker's controller, then the first non-controller): `triggeringPlayer()` reads the recorded or baked
player and `defendingPlayer()` the recorded defender (handling a battle's protector), which every collector these cards
use records; a `targetPlayer()` reads the effect's bound targets before the entry's `targetId`.

**Tests**: 95 classes (every card test of the 7 records, `ExileTopCardsToSourceEffectHandlerTest`,
`SubjectResolutionServiceTest`, `GameResyncProjectionServiceTest`, `EffectCountRatchetTest`): 816 test cases, all pass.
The Omenkeel, Expensive Taste, Tibalt, Cosmic Impostor, Flamewar, Hauken's Insight and Explore the Vastlands have no test
class (Rogue Class covers The Omenkeel's collector, Petty Larceny Expensive Taste's shape).

**Step 4 is done.** What stays open is in the ledger (`blocked-*` rows); `LibraryOwner` waits for subjects in
conditions.

### Step 5, run 5a (2026-11-19)

**Survivors.** `DestroyTargetPermanentEffect` gained a last component `PermanentSubject subject` (every old constructor
kept; null normalises to `target()`, or `target(targetGroup)` when a group is given, and a `target(g)` subject sets
`targetGroup`; a non-target subject rejects a group, a token rider and the active-player choice, and a target filter
unless the subject has a target part). New constructors `(PermanentSubject)`, `(PermanentSubject, boolean
cannotBeRegenerated)`, `(PermanentSubject, PermanentPredicate targetFilter)`. `targetSpec()` is the old harmful permanent
spec iff `subject.declaresTarget()`, else `NONE`; `removalKind()` is `DESTROY` iff it declares (the absorbed non-target
records were not `RemovalEffect`s, or only nested where no AI reader looks). The handler keeps the old target path
untouched (`tryDestroyAndLog`, the multi-group simultaneous batch, the token rider, LKI bookkeeping); every other subject
is resolved by `SubjectResolutionService` and destroyed as one `DestructionSupport.destroyBatch` (the old
`DestroyReferenced` path), with LKI bookkeeping only for the target part of an `allOf` and the "controller as it was
destroyed" write for `triggering()`. `DestroyTargetPermanentAtEndOfCombatEffect`'s `triggeringPermanent` flag became
`PermanentSubject subject` (`target()` default, `triggering()` via `ofTriggeringPermanent()`, `source()`); its handler
schedules each named permanent through `DelayedPermanentActions` as both old handlers did; `removalKind()` stays `DESTROY`
except for `source()` (the old self record was no `RemovalEffect`), `targetSpec()` is harmful creature only for `target()`.

**Deleted** (5 records, 5 handlers; ratchet 4,915 / 4,148): `DestroyReferencedPermanentEffect` →
`DestroyTargetPermanentEffect(Subjects.x()[, b])` (54 cards plus the two engine-built payloads in
`DestroyTargetIfDidNotAttackAtEndStepEffectHandler` / `DestroyUnlessPaysPerCounterEffectHandler`; `DestructionSupport`'s
forced-cost else-branch and `ForcedCostOrElseEffectHandler.killsBeforeChangingAnything` match `subject() instanceof
PermanentSubject.Source`); `DestroySourceEffect` → `source()` (Chaos Orb); `DestroySourceAndTargetPermanentEffect(f)` →
`(allOf(source(), target()), f)` (Wall of Vipers); `DestroyEnchantedCreatureOnLeaveEffect()` → `(attached(), true)`
(Parallax Dementia: the resolver reads the departed Aura's snapshot `attachedTo`, the old lookup);
`DestroySelfAtEndOfCombatEffect` → `DestroyTargetPermanentAtEndOfCombatEffect(source())` (Cinder Wall, Ceremonial Guard,
Arrogant Bloodlord, Vebulid, Goblin Sappers). 62 card files rewritten by a deleted scratch script, 4 engine, 2 domain
Javadoc, 3 test files. `PermanentReference` stays (Boost, PutCounter, RemoveCounter, RemoveFromCombat, ... are runs 5e,
5i, 5k).

**AI parity.** `DestroyTargetPermanentEffect` with a non-target subject reports `removalKind() == null`, so
`SpellEvaluator`, `HardAiDecisionEngine`, `BoardEvaluator`, `InstantCategoryClassifier`, `ConditionEvaluationService` and
`TargetPolarityClassifier` see what they saw for the old non-`RemovalEffect` records. `SizeGatedRemovalPump` counts any
`RemovalEffect` instance; the new instances are on the converted cards' activated abilities, none of which has a
power/toughness target filter, so no size gate changes.

**Behaviour differences** (none changes a tested outcome): Chaos Orb's and Parallax Dementia's destruction no longer
puts Auras left attached to nothing into the graveyard inside the resolution; the state-based action right after does
(CR 704.5m), as for every old `DestroyReferenced` card; Wall of Vipers and the end-of-combat target read the effect's
bound targets before the entry's `targetId`. New test: `MortalWoundTest.destroysEnchantedCreatureWithShroud` (a
non-target subject is not stopped by shroud).

**Tests**: 116 classes (every rewritten card's test, a 40-per-class sample of `DestroyTargetPermanentEffect`, all
`DestroyTargetPermanentAtEndOfCombatEffect` cards, `DestroyTargetPermanentEffectHandlerTest`,
`TriggeredAbilityStackServiceTest`, `SubjectResolutionServiceTest`, the two ratchet tests, the five AI tests naming the
classes): 1,524 test cases, all pass.

**For later runs.**

- Kept (ledger, `blocked-other`): `DestroyTargetPermanentAtEndStepEffect` / `DestroySelfAtEndStepEffect` differ in more
  than the subject (a `TemporaryGlobalTriggeredAbility` with a baked `DestroyTargetPermanentEffect` vs
  `DelayedPermanentActions`); move the target one onto `DelayedPermanentActions` first. `DestroyUpToOneTargetPermanentEffect`
  carries the `OptionalTargetEffect` marker. The two `Destroy*AndDamageControllerIfDestroyed` records are fused (step 8).
  `DestroyEachTargetPermanentEffect` (59 cards) and `DestroyAllPermanentsEffect` (`EachPermanentScope`) are head records
  for step 10 / a `controlledBy` run.
- `PermanentSubject.Source` is the forced-cost else-branch marker now; 5j (`SacrificeSelfEffect`) should match the same
  way.

### Step 5, run 5b (2026-11-19)

**Survivor.** `ExileTargetPermanentEffect`'s `boolean triggeringPermanent` became `PermanentSubject subject` and it
gained `boolean outsideGame`: `(tokenForController, targetFilter, subject, outsideGame)`, every old constructor kept,
null subject normalises to `target()`; a non-target subject rejects the token rider and a target filter, `outsideGame`
rejects a target subject. New `(PermanentSubject)`, `sourceOutsideGame()`; `ofTriggeringPermanent()` is now
`(Subjects.triggering())`. `targetSpec()` is the old harmful permanent spec iff the subject declares a target.
`removalKind()` is `EXILE` for `target()` and `triggering()` (both old values), `null` otherwise (the absorbed records
were no `RemovalEffect`s), so the AI sees what it saw. `resolvesAgainstAttachedPermanent()` is true for `attached()`
(what `ExileEnchantedCreatureEffect` declared, so an Aura's activation still captures the host before a sacrifice
cost). The handler keeps the target / triggering path untouched (targetsForEffect fallbacks, `removedPermanentControllers`,
last-known card and power, the token rider); every other subject is resolved by `SubjectResolutionService` and each
permanent goes through `removePermanentToExile` (or `removePermanentToOutsideGame`), so The Master, Multiplied's
creature-token check and the owner-exile handling in `PermanentRemovalService` are unchanged. The manafx
`ExileSelfManaAbilityEffectHandler` became `ExileTargetPermanentManaAbilityEffectHandler` (a mana ability has no target,
CR 605.1a; it throws for any subject but `source()`). `DestructionSupport`'s forced-cost else-branch matches
`ExileTargetPermanentEffect` with `PermanentSubject.Source` (through an `ObjectProvider`: the exile handler depends on
`DestructionSupport`); `ActivatedAbilityExecutionService`'s no-op snapshot branch for the Aura record is gone.

**Deleted** (3 records, 3 normalfx handlers; ratchet 4,912 / 4,145): `ExileSelfEffect` → `(Subjects.source())`
(`ExileSelfEffect(true)` → `sourceOutsideGame()`, Blacker Lotus), `ExileEnchantedCreatureEffect` and
`ExileEquippedCreatureEffect` → `(Subjects.attached())`. 34 card files rewritten by a deleted scratch script (Pit
Spawn and Sword of Kaldra keep `ofTriggeringPermanent()`), 3 engine, 2 domain, 2 test files.

**Behaviour differences** (none changes a tested outcome): "exile enchanted creature" logs "X is exiled." instead of
"X is exiled (Aura)."; the self and equipped paths no longer log an exile that `removePermanentToExile` refused. The
lookups are the old ones: `attached()` reads the live Aura, else the source snapshot (the old Aura handler's order), and
for Oathkeeper's dies trigger the departed Equipment's snapshot (the old handler read the triggering-permanent
snapshot, the same Equipment). New test: `DreadfulApathyTest.exilesEnchantedCreatureWithShroud`.

**Tests**: 85 classes (every rewritten card's test, Pit Spawn, Sword of Kaldra, a 30-per-class sample of
`ExileTargetPermanentEffect`, `ExileTargetPermanentEffectHandlerTest`, `TriggeredAbilityStackServiceTest`,
`SubjectResolutionServiceTest`, the two ratchet tests, `AiTargetSelectorTest`): 754 test cases, all pass.

**For later runs.**

- The Exile family has no other exile-a-permanent subject siblings: the `ExileTriggering*` records mostly exile the
  dying creature's *card* from a graveyard (not a permanent); "exile until / return" records are 5k; `ExileSelfThenEffect`
  is fused (step 8); `ExileSelfAtEndStepEffect` waits for the delayed-mechanism unification noted in 5a. All in the ledger.
- A self-removal rider in a mana ability needs a manafx handler keyed on the survivor's class (registry is by class);
  5j (`SacrificeSelfEffect`) should check for one the same way.

### Step 5, run 5c (2026-11-19)

**Survivor.** `ReturnToHandEffect` (still a final class with factories; every factory kept, so its 582 cards are
untouched) replaced `BounceScope scope` with `PermanentSubject subject` (`null` only for `selfSpell()`, which has no
subject: `returnsResolvingSpell()`), plus two target modes `orSuspendedCard` and `ofChosenCreatureType`; the baked
`enchantedPermanentId` / `grantingEquipmentId` fields and the `enchantedSnapshot` / `enchantedAndAurasSnapshot` /
`grantingEquipmentSnapshot` factories are gone. New public `(PermanentSubject)` constructor and `withSubject`; `filter()`
/ `withFilter` read and replace the target filter, or the `controlledBy` / `ownedBy` set's filter (TextChangeTransformer,
AI). Constants mapped per step 3's table: TARGET (+ suspended / chosen-type modes) `target()`, TARGET_AND_SELF
`allOf(source(), target())`, SELF `source()`, TRIGGERING `triggering()`, ALL_MATCHING `controlledBy(eachPlayer(),
filter)`, TARGET_PLAYERS_PERMANENTS `controlledBy(targetPlayer(), filter)`, TARGET_PLAYERS_OWNED **new**
`ownedBy(targetPlayer(), filter)`, AURAS_ATTACHED_TO_TARGET **new** `attachmentsOf(target(), Aura)`, ENCHANTED
`attached()`, ENCHANTED_AND_AURAS `allOf(attachmentsOf(attached(), Aura), attached())` (Auras first, as before),
GRANTING_EQUIPMENT **new** `granter()` (`Granter(UUID boundPermanentId)`: `ActivatedAbilityExecutionService` binds
`granter(ability.getGrantSourcePermanentId())` at activation; unbound it reads `entry.getGrantingPermanentId()`, set only
for granted triggered abilities). `targetSpec()`: suspended-card and chosen-type specs as before; a benign permanent spec
(with the target filter) when the subject is, holds or reads attachments of `target()`; a benign player spec for a
`controlledBy` / `ownedBy` of `targetPlayer()`; the self-targeting marker for `source()`; else `NONE`. `removalKind()` is
`BOUNCE` iff `subject.declaresTarget()` (Target, or `allOf` holding it: exactly the old four scopes), `sweepsBoard()` iff
`controlledBy(each...)` (= ALL_MATCHING). The handler keeps the departure paths: `target()` and `triggering()` permanents
one at a time through `bounceTarget` (controller rider, `rememberRemovedPermanentController`, last-known card, then
orphaned Auras), `source()` through `BounceSupport.applyReturnSelfToHand` (new overload taking the resolved permanent),
every other subject as one `applyReturnPermanentsToHand` leave batch (so `allPermanentsMatchingThen`'s count is kept).
Readers converted: `ExileTargetValidators` (`orSuspendedCard()`), `DestructionSupport`'s forced-cost else-branch
(`subject() instanceof PermanentSubject.Source`), `SpellEvaluator` (`sweepsBoard()`).

**Resolver.** `SubjectResolutionService` gained `OwnedBy` (owner = the card's owner, else `defaultControllerOf`, read
from the permanent on the battlefield), `AttachmentsOf` (live hosts, then every battlefield permanent attached to one
and matching the filter) and `Granter`. A set's `FilterContext` now also carries the source permanent id and the
entry's X (the old ALL_MATCHING context; `controlledBy` had no other user yet).

**Deleted** (`BounceScope`; 2 records, 2 handlers; ratchet 4,910 / 4,143): `ReturnSelfToHandAtEndOfCombatEffect` →
`ReturnTargetPermanentToHandAtEndOfCombatEffect(Subjects.source())` (Windscouter, Wall of Junk, Quicksilver Behemoth,
Phantom Whelp), `ReturnSelfToHandAtEndStepEffect` → `ReturnTargetPermanentToHandAtEndStepEffect(Subjects.source())`
(Rakalite, Sakashima the Impostor); both survivors gained a `PermanentSubject subject` (`target()` default), declare
the benign permanent target only for `target()` and schedule each resolved permanent through `DelayedPermanentActions`
as before. 6 card files rewritten by a deleted scratch script.

**Behaviour differences.** `ownedBy` reads the card's owner (CR 108.3) where the old TARGET_PLAYERS_OWNED read
`stolenCreatures` else the battlefield holder, so Hurkyl's Recall now also returns a permanent the target player owns
that another player controls without a recorded steal (test
`ReturnToHandEffectHandlerTest$TargetPlayersOwned.readsCardOwnerNotEnteringController`). `attachmentsOf(..., Aura)`
tests the effective subtype (`PermanentHasSubtypePredicate`) instead of the printed one. Sweep batches are in APNAP
order instead of seat order (simultaneous; only log order differs). `attached()` replaces ENCHANTED's chain (live →
baked host → departed snapshot → entry target); Phantom Wings / Sun Clasp / Ghostly Wings / Disappear tests pass on the
resolver's live → source snapshot → attached snapshot. The end-step self return lost its source-to-`targetId` fallback
(step 3: strict `source()`). River's Rebuke's filter now gets the source context (it is "nonland", so no change).

**Tests**: 159 classes (every card using a non-plain factory, the rewritten cards, a 60-per-class sample of
`ReturnToHandEffect`, the end-of-combat / end-step / delayed bounce cards, `ReturnToHandEffectHandlerTest` (now over a
real resolver on the mocks; new Granter and owner cases), `SubjectResolutionServiceTest`, the two ratchets,
`DestructionSupportFallbackTest`, `ActivatedAbilityExecutionServiceTest`, `TargetLegalityServiceTest`, the AI tests
naming the class): 1,776 test cases, all pass.

**For later runs.**

- Kept (ledger, `blocked-other`): `ReturnCombatOpponentToHandAtEndOfCombatEffect` and
  `ReturnEnchantedCreatureAndSelfToHandAtEndOfCombatEffect` use a `TemporaryGlobalTriggeredAbility`, not
  `DelayedPermanentActions` (the latter returns Aura and host as one event); `ReturnSelectedPermanentToHandAtEndOfCombat`
  needs a step-7 `picked()` subject; the `Return*ToHandThen` pair and `ReturnSelfToHandOnCoinFlipLoss` are fused (step 8).
- `ownedBy`, `attachmentsOf` and `granter()` are available; `Granter` carries an activation-bound id, the pattern for
  other "granting permanent" effects (`PutCountersOnGrantingEquipment`, `DestroyGrantingPermanentIfNoCounters`, ...).
- `PermanentSubject.declaresTarget()` stays false for `controlledBy` / `ownedBy` / `attachmentsOf` even when they read
  a target; an effect's `targetSpec()` inspects those shapes itself (as `ReturnToHandEffect` does).

### Step 5, run 5d (2026-11-20, partly done)

**Done: the tap and untap verbs.** `TapPermanentsEffect` and `UntapPermanentsEffect` replaced `TapUntapScope scope`
with `PermanentSubject subject`: `(subject, filter, chosenCount)` plus `(subject)` and `(subject, filter)`. `filter` is
now only the target restriction of a `target()` (and, for tap, `allTargets()`) subject; a set's filter lives in the
subject; `chosenCount > 0` needs a set subject (both rejected in the compact constructor). `targetSpec()` is the old
spec per old constant: benign permanent (+ filter) for `target()`, untap's `allTargets()` keeps the no-op
`playerOrPermanent()` spec of the multi-target path, benign player for `controlledBy(targetPlayer(), ...)` (tap's old
TARGET_PLAYERS_PERMANENTS), the self-targeting marker for `source()`, else `NONE`. Both handlers lost every private
lookup: `SubjectResolutionService` names the permanents; a named subject is tapped one by one ("taps itself." for
`source()`), a set is tapped together (an explicit tapping player only for `controlledBy(you(), ...)`, as before), and a
set with `chosenCount` prompts the controller as before (untap offers only the tapped ones). Readers converted:
`DestructionSupport` forced-cost else-branch (`subject() instanceof Source / Attached`), `MayEffectHandler`
(`Triggering`), 13 engine-built payloads, `SpellEvaluator` (target tap; `untapsAttackedCreatures` looks for
`PermanentAttackedThisTurnPredicate` in the set's filter), `TargetPolarityClassifier` (Tap: `Target` harmful; Untap:
`Target` / `AllTargets` beneficial; the AI sees what it saw).

Constants mapped: TARGET `target()`; ALL_TARGETS **new** `allTargets()` (every permanent target of the entry, any
group: `getTargetIds()` else `getTargetId()`, the old read; `target()` would read only the effect's group, which
differs for Kiora / Nissa, Genesis Mage style multi-slot abilities); SELF and SOURCE_PERMANENT `source()`; TRIGGERING
`triggering()`; ENCHANTED `attached()`; CONTROLLED `controlledBy(you(), f)`; OTHER_CONTROLLED_CREATURES
`controlledBy(you(), AllOf(creature, Not(IsSourcePermanent), f))`; TARGET_PLAYERS_PERMANENTS `controlledBy(targetPlayer(),
f)` for tap (it declared the player) and `controlledBy(targetPlayerDeclaredElsewhere(), f)` for untap (it declared
nothing: Reins of Power, Twist Allegiance, Curse of Bounty, Great Oak Guardian, Early Harvest, Rustler Rampage,
Trystan's Command, the Choose-Opponent-Draw-And-Untap payload); ALL_CREATURES `controlledBy(eachPlayer(), AllOf(creature,
f))`; ALL_PERMANENTS `controlledBy(eachPlayer(), f)`; ATTACKED_CREATURES `controlledBy(eachPlayer(), AllOf(creature,
PermanentAttackedThisTurnPredicate))`. `TapUntapScope` lost ALL_TARGETS, SOURCE_PERMANENT, OTHER_CONTROLLED_CREATURES,
ATTACKED_CREATURES and ALL_PERMANENTS (no other reader). 1,231 card files rewritten by a deleted scratch script (no call
site needed a hand edit), 15 engine files, 2 AI files, 10 test files, 13 agent-docs files. No record deleted: ratchet
unchanged (4,910 / 4,143).

**Resolver.** New `PermanentSubject.AllTargets` / `Subjects.allTargets()` (declares a target) and
`PermanentSubject.isSet()` (true for `controlledBy`, `ownedBy`, `attachmentsOf`). A set's `FilterContext` now also
carries the defending player (recorded, else `defendingPlayerOfAttackTarget`), the triggering player and the triggering
permanent id and snapshot: the old ALL_CREATURES / ALL_PERMANENTS contexts (War's Toll's "lands that player controls",
"creatures defending player controls"). Other `controlledBy` users gain that context; a filter that read one of them
previously saw nothing.

**The `source()` fallback.** No entry relied on SELF's fallback to the entry's target: with a temporary probe that threw
when a `source()` tap or untap resolved on an entry with no source permanent and a target, all 470 test classes of
non-target tap/untap cards (plus the sample) passed. Statically, every user is a permanent's activated or triggered
ability or an engine payload inserted into such an entry (`ChooseRandomOpponentMustAttackThisCombat`,
`AddCounterThenPayCountersOrTapAndDamage`), which all record the source. So the fallback is gone with no card change.

**Behaviour differences** (none changes a tested outcome): `attached()` falls back to the entry's
`attachedPermanentSnapshot` when the source left and recorded no snapshot (the old lookup did nothing); sets are
tapped and untapped in APNAP order instead of seat order (simultaneous; log and "becomes tapped" trigger collection
order only); untap's "up to N" set choice gets the full filter context (it had none); the set log lines say
"taps/untaps N permanent(s)." for every set; untap's TARGET_PLAYERS read `targetsForEffect` before `getTargetId()`
(the resolver's order; the cards have one player target).

**Tests**: 767 suites, 8,626 test cases, all pass: every test class of a card whose tap/untap is not `target()` (470),
every sixth of the 737 target-only cards (123), the AI tests naming the classes, `SubjectResolutionServiceTest` (new
`$AllTargets`, and `$Sets` cases for a set's triggering-player and defending-player filter), the resolver-backed handler
tests, the application tests naming `TapUntapScope`, the two ratchets. Ask for a full card-test run (1,231 card files).

**Left for the rest of 5d.** `TapUntapScope` stays for `SkipNextUntapEffect` (about 145 cards), `CantBlockThisTurnEffect`
(about 130), `CantAttackThisTurnEffect` (7, plus the dungeon room payload in `VentureIntoDungeonEffectHandler`),
`DoesntUntapEffect` (factories; static `self()` / `enchanted()` read by `UntapStepService` / `UntapPreventionSupport`),
`DoesntUntapWithCounterEffect` (static, with a `PermanentReference counterBearer`) and
`UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect` (static, `UntapStepService`). Their remaining constants:
TARGET, SELF, TRIGGERING, ENCHANTED, CONTROLLED, TARGET_PLAYERS_PERMANENTS, TARGET_CONTROLLERS_OTHER_CREATURES,
ALL_CREATURES, OPPONENTS_PERMANENTS. Notes for that run: CantBlock/CantAttack TARGET_PLAYERS_PERMANENTS,
TARGET_CONTROLLERS_OTHER_CREATURES and SkipNextUntap `matchAtUntap` install player-scoped floating rules (later arrivals
included), so their handlers keep a rule over the subject's players rather than a resolved set; the static ones need
the resolver's static entry point; `SkipNextUntapEffect`'s TARGET on a blocks trigger reads the collector's
non-targeting target; `ActivatedAbilityExecutionService`, `UntapPreventionSupport`, `TargetPolarityClassifier` and
`UntapStepService` match on the scope.

**For later runs.**

- Not-the-source predicate (for step 6): `PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())` matches by
  permanent id (the set's `FilterContext` carries the source permanent id), the form OTHER_CONTROLLED_CREATURES now
  uses (12 tap/untap cards wrote it themselves); 32 tap/untap cards use the card-id form
  `PermanentNotPredicate(new PermanentIsSourceCardPredicate())`, which matches by the source card's id; check a card
  before swapping one form for the other.
- Kept (ledger): the costs (`Tap*Cost`, `UntapMultiplePermanentsCost`: a cost is not the tap effect); the fused
  `Tap*Then*`, `TapAnyNumber*`, `Tap...AndPutCounters` records (steps 7/8); `UntapSearchedPermanent(s)` (step-7
  result subject); `UntapTargetAndSharingCreatures` (step 6's shares-a-colour predicate); the rest unique.
- "Becomes tapped" / "becomes untapped" triggers, "doesn't untap" restrictions, mana-ability taps, tapped-and-attacking
  tokens and `{T}` costs were not touched.

### Step 5, run 5e (2026-11-20)

**Survivors.** `PutCounterOnTargetPermanentEffect` gained a last component `PermanentSubject subject` (every old
constructor and factory kept; null normalises to `target()`). New `(PermanentSubject, CounterType[, int|DynamicAmount[,
PermanentPredicate condition]])` constructors and `withSubject`. A non-target subject rejects `predicate`,
`targetPredicate`, `regenerateIfSurvives` and `optionalTarget`; `source()` is rejected (the self records own it, as
`PutCounterOnReferenced` did). `targetSpec()` is the old benign permanent spec iff `predicate == null &&
subject.declaresTarget()`, else `NONE`; `resolvesAgainstAttachedPermanent()` is true for `attached()`. The handler keeps
the target path and the resolution-time own-permanent choice untouched; any other subject is resolved by
`SubjectResolutionService` and each permanent goes through the same `placeOnTarget` (replacement-order prompt,
`resolutionCondition`), with the count evaluated against the named permanent (the old referenced context: Experiment
Twelve's "equal to its power", She-Hulk's "for each creature blocking it"). `onTriggeringPermanent` is now
`(triggering(), ...)` (all 14 users have fixed counts, so the amount context change is moot); the
`PermanentIsTriggeringPermanentPredicate` special case is gone (Keleth rewritten to the factory).
`DoublePlusOneCountersOnTargetCreatureEffect` gained `PermanentSubject subject` (`()` = `target()`); target keeps its old
lookup (`targetsForEffect` then `targetId`, creature check), other subjects resolve through the service; every subject
places through `PermanentCounterSupport.placeCounterOnPermanent`.

**Deleted** (5 records, 5 handlers; ratchet 4,905 / 4,138): `PutCounterOnReferencedPermanentEffect` (`TRIGGERING`
`triggering()`, `ATTACHED` and the reference-less constructors `attached()`, `RETURNED` `returned()`; `condition` became
`resolutionCondition`), `PutCountersOnEnchantedCreatureEffect` (`attached()`), `PutCountersOnGrantingEquipmentEffect`
(`granter()`, bound at activation by `ActivatedAbilityExecutionService.snapshotEffects` like the 5c bounce),
`DoublePlusOneCountersOnSourceEffect` (`source()`; `ofTriggeringPermanent()` `triggering()`),
`DoublePlusOneCountersOnEnchantedCreatureEffect` (by Oracle text: "double the number of +1/+1 counters on it" after
"whenever equipped/enchanted creature attacks / deals combat damage" is `triggering()` for Sword of Hours, Fractal
Harness, Seismic Tutelage; "on enchanted creature" is `attached()` for Hydra's Growth; the old handler preferred the
triggering permanent when the snapshot was attached to it, else the attachment). 96 card files rewritten by a deleted
scratch script (plus Keleth by hand), 2 engine payloads, 1 application test, agent-docs. Readers: `MayEffectHandler`'s
"can't have counters" else-branch now also requires `Target`; `SpellEvaluator` scores a +1/+1 placement only for a
target or `triggering()` subject (what it saw before, plus the old referenced-triggering form); `TargetPolarityClassifier`
unchanged (only targets reach it). `PermanentReference` stays (Boost 5i, RemoveFromCombat 5k, RemoveCounter 5f, ...).

**Behaviour changes.** (1) Doubling +1/+1 counters is putting counters (CR 701.10e): the source and target doublings
used private placement paths; the source path skipped the "you put +1/+1 counters on another creature" event, the
self-placed event, the Hero event, the log and the can't-have-+1/+1 check, the target path skipped can't-have-counters.
All now use `placeCounterOnPermanent`. Test: `DragonsguardEliteTest.doublingCountersIsPuttingCountersForOtherWatchers`
(Knight of Wundagore triggers). (2) Non-target placements now ask the affected permanent's controller to order counter
replacements when the order matters (CR 616.1), as targets did. Test:
`SeismicTutelageTest.enchantedCreaturesControllerOrdersCounterReplacements`. (3) An Aura or Equipment whose activation
cost moves it off the battlefield (Krasis Incubation) had its stack-entry snapshot taken after the cost had detached
it; `ActivatedAbilityExecutionService.sourceAsItLastExisted` restores the host captured before costs, so `attached()`
reads it as the Aura last existed (CR 608.2h). `KrasisIncubationTest` covers it; a resolver fallback to
`attachedPermanentSnapshot` was tried and rejected because it broke `TortureTest`'s "Aura became unattached, then left"
case. Not tested, no card depends on it: the referenced placements' log line is the shared one.

**Kept exactly.** Counter replacements (`GameQueryService.replaceCounters` / `counterReplacementsFor`, Vorinclex,
Hardened Scales, Doubling Season, can't-have-counters), `notifyCountersPlaced` and its batching, the +1/+1 and -1/-1
annihilation state-based action (CR 704.5q), enter-with-counters replacements, proliferate (rad counters still skip
replacements, run 4h).

**Tests**: 155 classes, 4,090 test cases (every rewritten card's test, a 40-per-class sample of
`PutCounterOnTargetPermanentEffect`, every `DoublePlusOneCountersOnTargetCreatureEffect` card, `SpellTriggersTest`,
`SubjectResolutionServiceTest`, `ActivatedAbilityExecutionServiceTest`, the two ratchets, the application and AI tests
naming the survivor), then 130 classes (every card naming `attached()` or `PermanentReference.ATTACHED`) after the
activation-snapshot fix: all pass.

**For later runs.**

- Kept (ledger): `PutCountersOnSelfEffect` (988 cards, no rare subject sibling; still has its source-to-`targetId`
  lookup and planar path) and `PutCountersOnSourceEffect` (370 cards, its own inline P/T-modifier placement) are step 10;
  `DoublePlusOneCountersOnControlledCreaturesEffect`, `PutCounterOnEachControlledPermanentEffect` and
  `PutCounterOnEachMatchingPermanentEffect` are `controlledBy` sets (one summary log, triggers after all placements:
  check before folding); `PutCounterOnReturnedPermanentsEffect`, `PutCountersOnCreatedPermanentsEffect`,
  `PutCountersOnChosenPermanentEffect` need step-7 subjects; the `...Then...` records are fused (step 8).
- 5f (`RemoveCounterFromReferencedPermanentEffect`) and 5i should check the same activation-cost case; the snapshot
  restore now covers every `attached()` reader.

### Step 5, run 5f (2026-11-20)

**Survivors.** `RemoveCounterFromSourceEffect` gained a last component `PermanentSubject subject` (every old
constructor kept; null normalises to `source()`). New `(PermanentSubject, CounterType[, int])` constructors and
`withSubject`. A target subject is rejected (`RemoveCounterFromTargetPermanentEffect` owns it until step 10), and
`targetsPlayer` needs `source()`. `targetSpec()`: the player spec for `targetsPlayer`, the self-targeting marker for
`source()`, else `NONE`; `resolvesAgainstAttachedPermanent()` is true for `attached()`. The handler resolves the subject
through `SubjectResolutionService` and, per permanent, evaluates the amount against it, removes through
`PermanentCounterSupport.removeCounterFromPermanent` (vanishing's last-time-counter check, oil record, log) and emits
`notifyLastCounterRemoved`. `RemoveAllCountersEffect` and `RemoveCounterAndGainLifeEffect` replaced `CounterRemovalSubject`
with `PermanentSubject` (`source()` default): `target()` keeps the benign creature spec, `source()` the self-targeting
marker; both handlers lost their private lookups.

**Resolver.** New `PermanentSubject.Named(UUID)` / `Subjects.named(id)`: a permanent a resolving effect bound for the
delayed ability it creates (CR 603.7c; a permanent that came back is a new object). The step-3 table's plan for NAMED
(`triggering()` once the delayed ability records it) does not fit: Giant Oyster's untaps-or-leaves ability is about the
Oyster (`TemporaryGlobalTriggeredAbility.about(..., oyster, oyster)`), so its triggering permanent is the Oyster, and
event-trigger collectors don't apply `DelayedReferents`. `RemoveAllCountersEffect.aimedAt(id)` builds `named(id)`.

**Deleted** (`CounterRemovalSubject`; 2 records, 2 handlers; ratchet 4,903 / 4,136):
`RemoveCounterFromReferencedPermanentEffect` → `RemoveCounterFromSourceEffect(Subjects.attachedAtTrigger(), SLEEP)`
(Venarian Gold, its only user); `RemoveCounterFromGrantingPermanentEffect` → `(Subjects.granter(), TASK)` (Heliod's
Punishment), bound at activation by `ActivatedAbilityExecutionService.snapshotEffects` like the 5c/5e granter effects.
Constants: SOURCE `source()` (the fallback to `targetId` dropped, step 3), TARGET `target()`, NAMED `named(id)`.
10 card files rewritten by a deleted scratch script. Readers converted: `DestructionSupport`'s forced-cost else-branch
(`subject() instanceof Source`), `TargetPolarityClassifier` (`declaresTarget()`), `PlayerInputService`'s mode greying
(only a `source()` removal greys a mode), the mana-ability path in `ActivatedAbilityExecutionService` (throws for a
non-`source()` subject: a mana ability has no stack entry, CR 605.3b). `PermanentReference` stays (Boost 5i,
RemoveFromCombat 5k, `DoesntUntapWithCounterEffect`, the fused destroy record).

**The `source()` fallback.** With a temporary probe that threw when a `source()` removal resolved on an entry with no
source permanent and a target, the test classes of every card using `RemoveCounterFromSourceEffect`,
`RemoveAllCountersEffect`, `RemoveCounterAndGainLifeEffect` and `RemoveCounterFromSourceAtEndOfCombatEffect` passed;
the end-of-combat payload's delayed entry records the source (`atNextStep(..., sourcePermanentId, ...)`).

**Behaviour differences** (none changes a tested outcome): the referenced and granter removals now emit the "you
removed the last counter" event as the source removal did; only Divine Intervention listens for it (on itself), so no
card changes. `RemoveCounterAndGainLife`'s target reads the effect's target group before the entry's `targetId` (the
resolver's order; the cards have one target). Test added: `RemoveCounterFromSourceEffectTest` (an Aura with "sacrifice
this Aura: remove a +1/+1 counter from enchanted creature" removes it from the host it had before the cost, the 5e
activation-cost case), `SubjectResolutionServiceTest$Named`. Venarian Gold's "Aura moved in response" case passes.

**Kept exactly.** Counter-removal costs (`RemoveCounterFrom*Cost`, `RemoveAllCountersAsCostEffect`), the "one or more
counters removed" triggers (`drainCountersRemovedSinceTriggerCheck`), time / suspend / lore counter paths.

**Tests**: 140 suites, 1,189 test cases, all pass (every card test class of the four records above, Venarian Gold,
Heliod's Punishment, Giant Oyster, `RemoveCounterFromSourceEffectTest`, `SubjectResolutionServiceTest`,
`ActivatedAbilityExecutionServiceTest`, `DestructionSupportFallbackTest`, the two ratchets, `TargetPolarityGuardTest`).

**For later runs.**

- Kept (ledger): `RemoveCounterFromTargetPermanentEffect` (9 cards; own removal path with the defense-counter battle
  check, kind choice and up-to prompt, and no vanishing check) is step 10's to merge with the source record;
  `RemoveCounterFromEachControlledPermanentEffect` / `RemoveCounterFromEachMatchingPermanentEffect` are `controlledBy`
  sets; the `...Then...`, `...OrSacrifice...` and gain-life records are fused (step 8).
- Not done here (5m): the `RemoveAllCountersFrom{TargetPermanent,TargetCreature,EnchantedCreature}Effect` siblings
  (every counter kind). Only the target-permanent handler runs the vanishing last-time-counter check and the battle
  defense check; folding the other two into it fixes that for them.
- `named(id)` is the subject for any other effect a resolving effect binds to one permanent for a delayed ability.

### Step 5, run 5g (2026-11-20)

**Survivor.** `DealDamageToTargetCreatureEffect` gained a last component `PermanentSubject subject` (every old
constructor kept; null normalises to `target()`), new `(PermanentSubject, DynamicAmount|int)` constructors and
`withDamage` (the granted-ability snapshot in `ActivatedAbilityExecutionService` now keeps every component). A non-target
subject must be `source()`, `triggering()`, `attached()` or `attachedAtTrigger()` and rejects `unpreventable`, a
`targetPredicate` and a non-`ABILITY_SOURCE` damage source. `targetSpec()` is the old harmful creature spec iff the
subject declares a target, else `NONE`. The handler keeps the target path untouched; each other subject is named by
`SubjectResolutionService` and keeps its old handler's path exactly: `source()` the live source through
`resolveAnyTargetDamage` (old Source handler); `triggering()` the creature/planeswalker/battle check, the early
`isDamagePreventedForCreature` check and its log line (old TriggeringPermanent handler); `attached()` /
`attachedAtTrigger()` the Aura (live, else snapshot) must be attached and is the explicit damage source, no early check
(old EnchantedCreature handler). The early prevention check was not spread to any path that lacked it.
`DealDamageToPermanentsTargetControlsEffect` replaced `DamagedPermanentScope scope` with a `controlledBy` subject:
the short constructors are `controlledBy(allOf(targetPlayerDeclaredElsewhere(), controllerOf(target())), creature)`
("that player or that planeswalker's controller", the old target-player-else-permanent's-controller read),
`toTargetedPlayers(n, filter)` for Chandra, Bold Pyromancer's creature-or-planeswalker −7; the handler returns before
its early prevention check when the players name nobody, as before.

**Deleted** (3 records, 3 handlers, the enum `DamagedPermanentScope`; ratchet 4,900 / 4,133):
`DealDamageToSourceEffect` → `(Subjects.source(), n)` (9 cards), `DealDamageToTriggeringPermanentEffect` →
`(Subjects.triggering(), n)` (8 cards plus the planeswalker payload of `RegisterDamageAtNextUpkeepUnlessPaysEffectHandler`),
`DealDamageToEnchantedCreatureEffect` → `attached()` (Fatal Attraction, Tilonalli's Crown's `toCurrentlyEnchanted`) or
`attachedAtTrigger()` (Guilty Conscience). 19 card files rewritten by a deleted scratch script, Chandra, Bold Pyromancer by
hand. Kept: `DealDamageToTargetPermanentEffect` (Volcanic Spite, Shatter the Source, Rampaging Raptor) is not a subject
sibling: its subject is `target()` too, but its target kind is creature/planeswalker/battle and it uses the any-target
damage path; it belongs with the creature-or-planeswalker damage records (step 10).

**Guilty Conscience.** "That creature" is the enchanted creature as the ability triggered, which the old default mode
read as the triggering permanent with the Aura path. `DamageTriggers.queueEnchantedCreatureDealsDamageTriggers` now also
records it as the entry's `attachedPermanentSnapshot`, so it is `attachedAtTrigger()`. The six other Auras with that
trigger (Spirit Link, Soul Link, Vampiric Link, Spirit Loop, Armadillo Cloak, Visions of Brutality) use gain/lose-life
effects that never read the snapshot; their tests pass.

**AI parity.** The self-damage record was no `DamageDealingEffect`; the survivor is. `canDamageCreatures()` is false
for `source()`, so every AI reader gated on it (SpellEvaluator, InstantCategoryClassifier, BoardEvaluator,
HardAiDecisionEngine, AiTargetSelector) sees what it saw; `GameSimulator`'s ability-target ranking (which counted every
`DamageDealingEffect`) skips a `source()` damage (`isAimedDamage`), so Psionic Sliver / Reckless Embermage rank targets
by their 2 / 1 damage as before. `TargetPolarityClassifier` returns null for a non-target subject (no permanent in the
spec), as the fall-through did. Triggering and Aura subjects were `DamageDealingEffect`s with the same answers before.

**Behaviour differences.** (1) Fatal Attraction's enters trigger damages the creature the Aura enchants as it resolves
("deals 2 damage to enchanted creature", the reading Tilonalli's Crown already had; CR 608.2h), not the host recorded
when it triggered. Test: `FatalAttractionTest.enteringDamageFollowsAuraMovedInResponse`. (2) Chandra, Bold Pyromancer's
−7 judges "planeswalker" with the layered type (`PermanentIsPlaneswalkerPredicate`); the old printed-type check was
justified by a printed-type loyalty branch that `DamageSupport` no longer has (it uses `isPlaneswalker`). (3) The
target-controls players come from the effect's own targets (`targetsForEffect`) before the entry's `targetId`, and a
departed planeswalker's controller is its last-known one.

**Tests**: 109 suites, 826 test cases, all pass: every rewritten card's test class, the target-controls cards, Quenchable Fire, the six
enchanted-creature-deals-damage Auras, a 40-per-class sample of `DealDamageToTargetCreatureEffect`,
`DealDamageToTargetCreatureEffectHandlerTest`, `SubjectResolutionServiceTest`, `ActivatedAbilityExecutionServiceTest`,
the two ratchets, `GameSimulatorTest`.

**For later runs.**

- 5h ("a creature deals damage") is untouched. `DealDamageToDefendingPlayerCreaturesEffect` is almost
  `DealDamageToPermanentsTargetControlsEffect` with `controlledBy(defendingPlayer(), creature ∧ filter)` but differs in
  amount type, filter context and protection call; `DealDamageToEachMatchingPermanentEffect` (`EachPermanentScope`),
  `DealDamageToEachCreatureDamagedPlayerControlsEffect`, `DealDamageToOtherCreaturesControlledByTargetEffect` and
  `DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffect` are the other set-wide damage records for a
  `controlledBy` damage run (ledger).
- The early `isDamageSourcePreventedWithLog` / `isDamagePreventedForCreature` checks still differ per path (4d); a run
  that unifies them needs the user's sign-off.

### Step 5, run 5h (2026-11-20)

`--siblings` lists `*DealsPowerDamageTo*` (8 with the survivor), `*DealsDamageTo*` (8), `*DealsPowerDamageToAny*` (2)
and `Each*DealsPowerDamageTo*` (2). Two shapes absorb at least two classes each: a permanent dealt power damage by
another permanent, and a player dealt damage by a target creature. All are one-sided damage, not fight (CR 701.14a);
no fight record was touched.

**Survivor A.** `TargetDealsPowerDamageToTargetEffect` replaced `int sourceTargetGroup, int victimTargetGroup` with
`PermanentSubject dealer, PermanentSubject recipient` and gained `PermanentPredicate targetFilter`; it is now a
`DamageDealingEffect` (`damageAmount()` `SourcePower`, `canDamageCreatures()` only for a `source()` / `triggering()`
dealer, the two old DDE shapes, `canDamagePlayers()` false). Every old constructor is kept (`target(g)` subjects), so
its 57 cards are untouched. New `(dealer, recipient)`, `toItself([m])`, `bySource([filter])`, `readsTwoTargetGroups()`.
The compact constructor accepts only the six shapes in its Javadoc. `targetSpec()`: two target groups keep the old
rule (harmful creature if it records excess, else benign `playerOrPermanent`); a group-less `target()` recipient is
harmful creature (+ filter), as all four absorbed target records were; `(target(), source())` is `NONE`. The handler
resolves every id through `SubjectResolutionService`; target dealers share the old bite path (loop over the dealers);
`source()`, `attachedAtTrigger()` and `triggering()` dealers keep their old handlers' paths exactly (the ability's own
entry and last-known power for `source()`; the trigger-time host, its attached snapshot or departed snapshot and its
last controller for Laccolith Rig; the trigger's recorded snapshot, controller and power for Lothlórien Blade).

**Survivor B.** `TargetCreatureDealsDamageToControllerEffect` accepts `eachOpponent()` (target scope only) beside
`controllerOf(target())` and `you()`; `damagesController()` is true for `you()`. The `eachOpponent()` path is the old
Origin of Thor handler (a carrier with the ability's controller handed to `DealDamageToPlayersEffectHandler`). Its
dealer stays a `GrantScope` (`TARGET` / `TOKENS_CREATED_THIS_RESOLUTION`) until step 6 / step 7.

**Deleted** (8 records, 8 handlers; ratchet 4,892 / 4,125): `TargetCreatureDealsPowerDamageToSelfEffect` → A
`toItself([m])` (9 cards), `EachTargetCreatureDealsPowerDamageToSourceEffect` → A `(target(), source())` (Living
Inferno, Polukranos, Ravenous Gigantotherium), `SourcePermanentDealsPowerDamageToTargetCreatureEffect` → A
`bySource([f])` (4), `EnchantedCreatureDealsPowerDamageToTargetCreatureEffect` → A `(attachedAtTrigger(), target())`
(Laccolith Rig: "have it deal" after "whenever enchanted creature becomes blocked" is the host as it triggered; its
"Aura moved after the trigger" test passes), `EquippedCreatureDealsPowerDamageToTargetCreatureEffect` → A
`(triggering(), target())` (Lothlórien Blade; the attack trigger always records the creature, so the old
"no triggering id: the Equipment's host" fallback had no user), `TargetCreatureDealsPowerDamageToControllerEffect` → B
`(new SourcePower())` (5), `TargetCreatureDealsPowerDamageToAbilityControllerEffect` → B `(new TargetPower(), TARGET,
you())` (Form of the Dinosaur), `TargetCreatureDealsPowerDamageToEachOpponentEffect` → B `(new SourcePower(), TARGET,
eachOpponent())` (Origin of Thor). 25 card files rewritten by a deleted scratch script (Self-Destruct's local variable
type by hand), 1 handler test renamed (`TargetDealsPowerDamageToItselfEffectHandlerTest`), 4 agent-docs files.

**AI parity.** `TargetPolarityClassifier` lost the name entries for the survivor, the self record and the controller
record; a structural branch gives the old answers (two groups `HARMFUL`; a `target()` recipient `HARMFUL_DAMAGE`
except the Aura shape, which had no polarity; `(target(), source())` none; B with `eachOpponent()` none, as Origin of
Thor had). `GameSimulator.isAimedDamage` skips A unless `canDamageCreatures()`, so the bite and self shapes (never
`DamageDealingEffect`s) still add no damage to an ability's target ranking. B was already a `DamageDealingEffect`; the
five power-to-controller cards become one with `canDamageCreatures()` false and a non-`Fixed` amount, which every AI
reader of the interface ignores (`RaceEvaluator` counts only `Fixed`; none is a mana ability).

**Behaviour changes.** (1) A target dealer that is no longer a creature deals nothing: a noncreature permanent has no
power (CR 208.3) and 0 damage is no damage (CR 120.8); the bite and self paths read the printed power. Test:
`TargetDealsPowerDamageToTargetEffectHandlerTest.noncreatureDealerDealsNoDamage`. (2) The self and "damage to this
creature" shapes now skip their protection check when the dealer's damage can't be prevented (the bite's check).
(3) Origin of Thor's target spec is harmful (protection from the saga is honoured where the engine reads `harmful`,
CR 702.16b) instead of benign; the saga chapter's target prompt does not consult it (a pro-red creature is still
offered: a separate gap in the chapter target path, not fixed here). (4) Log lines of the absorbed paths are the
bite's ("X's damage is prevented."). Damage multipliers saturate (`SaturatingMath`) instead of throwing.

**Kept exactly.** The damage source (the dealer, through a carrier entry or, for `source()`, the ability's entry):
deathtouch, lifelink, infect, wither and damage triggers key off it; power read at resolution, last-known for a
non-target dealer that left; nothing happens when a target dealer or the recipient is gone; prevention and protection
checks; every target spec.

**Tests**: 120 suites, 1,060 test cases, all pass: every test class of every card naming either survivor (84: all 57
bite cards, every rewritten card), `TargetDealsPowerDamageToTargetEffectHandlerTest`,
`TargetDealsPowerDamageToItselfEffectHandlerTest`, `SubjectResolutionServiceTest`, the two ratchets,
`TargetPolarityGuardTest`, `GameSimulatorTest`, `AiTargetSelectorTest`.

**For later runs.**

- Kept (ledger, `blocked-other`): `EachTargetCreatureDealsPowerDamageToTargetCreatureEffect` is the two-group shape but
  declares a harmful creature spec where the bite declares benign `playerOrPermanent`; deciding which is right (the
  benign bite skips protection at targeting) folds it. The any-target records
  (`TargetCreatureDealsPowerDamageToAnyTargetEffect` 21 cards, `EnchantedCreatureDealsPowerDamageToAnyTargetEffect`) have a
  player-or-permanent recipient. `SourcePermanentDealsPowerDamageToUpToOneTargetCreatureEffect` carries
  `OptionalTargetEffect`. The non-target creature-to-player records (`EnchantedCreatureDealsPowerDamageToController`,
  `EnchantedCreatureDealsDamageToEachOpponent`, `EquippedCreatureDealsDamageToDefendingPlayer`,
  `EachCreatureDealsDamageToItsController`, `EnchantedCreatureDealsDamageToItsOwner`) fold into B once its `GrantScope`
  dealer is a `PermanentSubject` (step 6; tokens need a step-7 "created" subject) and a per-dealer "its controller"
  is expressible. The fixed-amount permanent records (`EnchantedCreatureDealsDamageToTargetCreature`,
  `TargetCreatureDealsDamageToSelf`, `TargetLandDealsDamageToTriggeringCreature`) fold into A if it gains a
  `DynamicAmount`.
- `DealDamageToTargetPermanentEffect` and the set-damage records of 5g were not taken: neither is a dealer/recipient
  shape. Not in the two groups and still unaudited: the `TargetCreatureDealsPowerDamageToEachOther*`,
  `TargetCreatureDealsPowerDamageToEachTarget`, `...ThenApplyPerpetual...`, `ControlledCreaturesDealPowerDamage*`,
  `ChosenPermanentDealsPowerDamage*` records (5m).
- `agent-docs/EFFECT_MERGE_PLAN.md`'s `DealsPowerDamageEffect(...)` row (4 classes, unnamed) is about these records;
  this run gave every absorbed shape the carrier form except `source()`, which keeps the ability's entry.

### Step 5, run 5i (2026-11-20)

`--family Boost` / `--siblings` give the one-permanent one-shot boosts `BoostSelfEffect` (1,079 cards),
`BoostTargetCreatureEffect` (1,356), `BoostEquippedCreatureUntilEndOfTurnEffect` (32), `BoostReferencedPermanentEffect` (21),
`BoostTargetPermanentEffect` (9), `BoostSelfOrEnchantedCreatureUntilEndOfTurnEffect` (7) and
`BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect` (5). The split between the two survivors is by amount context,
not by name: every non-target sibling but Referenced evaluated its amounts against the boosted permanent (Two-Handed Axe's
"double its power", Sigil of Valor's "each other creature you control"), as `BoostSelfEffect` does, and Referenced's
amounts never read the context permanent except Junk Jet's. So `BoostSelfEffect` takes every non-targeting one-permanent
boost and `BoostTargetCreatureEffect` is unchanged.

**Survivor.** `BoostSelfEffect(powerBoost, toughnessBoost, duration, PermanentSubject subject, Set<Keyword> keywords)`;
every old constructor kept (`source()`, no keywords), so its 1,079 cards are untouched. New `(subject, p, t[, keywords])`
and `(subject, int, int[, Keyword])`. The compact constructor rejects a target or set subject. `targetSpec()` is the old
self-targeting marker for `source()` and `attachedOrSource()` (old SelfOrEnchanted), else `NONE` (old Equipped,
Referenced); `resolvesAgainstAttachedPermanent()` for `attached()` / `attachedOrSource()`; `hasAbilityResolutionCondition()`
from the amounts (Bronze Cudgels; no `source()` card has such an amount). The handler resolves the subject through
`SubjectResolutionService`, evaluates the amounts with the boosted permanent as source, adds the 7c boost (UEOT through
`addPowerToughnessBoost`, other durations as before) and a layer-6 `GrantKeywordEffect` for the keywords, both on that
permanent with the effect's duration (CR 611.2a, timestamps as before), and for a non-`source()` subject refreshes the
source snapshots of the boosted permanent's waiting abilities (the old Referenced step, Ogre Battledriver). The static
paths (`BoostSelfSelfEffectHandler`, `StaticEffectSupport`) throw for any other subject or a keyword.

**Resolver.** Two variants, each for a shape no composition expresses: `attachedOrSource()` (bestow "this creature ...; if
it's an Aura, enchanted creature ... instead": the host while the source, as it is or last was, is attached, else the
source; Everflame Eidolon, Erebos's Emissary) and `triggeringOrSource()` ("whenever this creature or equipped creature
attacks, it gets": the own attack trigger records no triggering permanent; Chainflail Centipede, Razorfield Ripper).
Tests: `SubjectResolutionServiceTest$AttachedOrSource`, `$Triggering.triggeringOrSourcePermanent`.

**Deleted** (3 records, 3 handlers; ratchet 4,889 / 4,122): `BoostReferencedPermanentEffect` (TRIGGERING `triggering()`,
ATTACHED `attached()`, ATTACHED_AT_TRIGGER `attachedAtTrigger()` (Paroxysm; its "Aura moved in response" test passes),
RETURNED `returned()`), `BoostSelfOrEnchantedCreatureUntilEndOfTurnEffect` (by Oracle text: Fiery Mantle, Pemmin's Aura
`attached()`; Scriv's Contract `triggering()`; the bestow and reconfigure cards above),
`BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect` (Bestial Fury, Hedron Blade, Spiked Ripsaw `triggering()`;
Luxior, Ring of Evos Isle `attached()`). `BoostEquippedCreatureUntilEndOfTurnEffect` keeps only Hand of Vecna's choice form
(`orControlledCreatureMatching` required; the equipped candidate is `attached()`); its 31 other cards are
`BoostSelfEffect(attached(), ...)` (activated abilities and triggers not about the host: Adventuring Gear's ruling) or
`(triggering(), ...)` ("whenever equipped/enchanted creature attacks/blocks, it gets": Gift of the Woods, Two-Handed Axe,
Sigil of Valor, Thunderous Might, Veteran's Armaments, Slab Hammer; attached combat triggers record the creature). 64
card files rewritten by a deleted scratch script, Junk Jet by hand (`EnchantedPermanentPower(true)` → `SourcePower(true)`,
the boosted creature's power), 1 application test, 12 agent-docs files.

**`source()` fallback.** With a temporary probe that threw when a `BoostSelfEffect` resolved on an entry with no source
permanent and a target, 408 test classes (a 350-per-class sample of `BoostSelfEffect` cards and every card of the ten
effects whose handlers call `BoostSelfEffectHandler`) passed; no spell carries the record. `source()` stays strict and the
fallback is gone.

**AI parity.** `SpellEvaluator` (self-pump value, sacrifice-only-beneficiary check) and `HardAiDecisionEngine.selfBoost`
match `BoostSelfEffect` only with a `source()` subject; the absorbed records were not read by any AI code.

**Behaviour changes.** (1) Hand of Vecna's "equipped creature" is the one the Equipment is attached to as the ability
resolves, not as it triggered (the old step-trigger read of the attached snapshot; Adventuring Gear ruling, Hand of
Vecna's "choose as it resolves" ruling). Test: `HandOfVecnaTest.boostsCreatureEquippedAtResolution`. (2) "It" in an
attached combat trigger is the triggering creature only: the old keyword and Equipped handlers fell back to the
Equipment's current host (or the entry's target) once that creature had left, boosting a new object (CR 400.7). (3) The
waiting-ability snapshot refresh now also follows the former Equipped / SelfOrEnchanted / keyword boosts. (4) Log lines
are `BoostSelfEffect`'s ("gets +X/+Y [and gains K] until end of turn."); the former Referenced keyword boosts now name
the keyword. Nothing tested changed.

**Kept exactly.** Layer 7c placement and timestamps (CR 613.7), durations, the set of one permanent fixed as the effect
begins (CR 611.2c), nothing for a permanent that left (CR 400.7), the 5e activation-cost capture (`BriarShieldTest`'s
sacrifice case), last-known hosts through the resolver (CR 608.2h).

**Tests**: 264 suites, 2,038 test cases, all pass: every rewritten card's test, Hand of Vecna, a 60-per-class sample of
`BoostSelfEffect`, `SubjectResolutionServiceTest`, the two ratchets, `SpellTriggersTest`, `StepTriggerServiceTest`,
`DrawServiceTest`, `LibraryTriggersTest`, `GameQueryServiceTest`, `AbilityActivationServiceTest`,
`ActivatedAbilityExecutionServiceTest`, `SpellEvaluatorTest`, `HardAiDecisionEngineTest`. `StepTriggerServiceTest` did not
compile on HEAD (`d563f5e806` changed the `StepTriggerService` constructor to `TargetLegalityService`); fixed to pass it.

**For later runs.**

- Kept (ledger): `BoostTargetPermanentEffect` (target permanent incl. Vehicles, harmful spec; not a subject sibling of the
  benign creature target; step 10), `BoostPairedCreatureOfTargetEffect` (needs a paired-with subject),
  `BoostCreatedTokensEffect` (step 7), `BoostEquippedCreatureOfEnchantedEquipmentEffect` (static, step 6), Hand of Vecna's
  record (`unique`). The set-wide and `GrantScope` boosts wait for step 6 as planned.
- `PermanentReference` and `Subjects.of` stay: their readers are now Sacrifice (5j), RemoveFromCombat (5k), Explore, Goad,
  TurnSourceFaceUp, Shuffle, MustBlockSource, `DoesntUntapWithCounterEffect`, `PutOrRemoveCounterIfAttackedOrBlocked...`,
  the fused destroy record and `PermanentBlockingSourcePredicate`, not only 5k.
- Step 10's Boost merge: `BoostSelfEffect` reads amounts against the boosted permanent, `BoostTargetCreatureEffect` against
  the source. A merged record needs that as a property of the subject (target → source context) or an explicit choice.
- "Gains [keyword] until end of turn" for one named permanent is `BoostSelfEffect(subject, 0, 0, keyword)`; Ogre
  Battledriver still pairs a boost with `GrantKeywordEffect(GrantScope.TRIGGERING_PERMANENT)` (step 6).

### Step 5, run 5j (2026-11-20)

`--family Sacrifice` / `--siblings` give the one-named-permanent sacrifices `SacrificeSelfEffect` (314 cards),
`SacrificeEnchantedCreatureEffect` (5), `SacrificeEnchantedCreatureOnLeaveEffect` (4), `SacrificeTargetCreatureEffect` (2)
and `SacrificePermanentDamagedPlayerControlsEffect` (1, the 4g leftover); the rest are player-choice sacrifices (4g),
delayed sacrifices, sets or fused records.

**Survivor.** `SacrificeSelfEffect(PermanentSubject subject, PlayerSubject sacrificer, CreateTokenEffect
tokenForSacrificingPlayer, PermanentPredicate targetFilter)` replaced `PermanentReference sacrificed` and the
`currentControllerSacrifices` flag. Every old constructor and `ofTriggeringPermanent(...)` is kept (the flag maps to
`controllerOf(subject)`, else `you()`), so its 314 cards are untouched. New `(subject)`, `(subject, sacrificer)`,
`(subject, sacrificer, targetFilter)`. Subjects: `source()`, `triggering()`, `attached()`, `target([group])`; a target
filter needs `target()`. `targetSpec()`: the self-targeting marker for `source()`, harmful creature (or
`permanents(targetFilter)`) for `target()`, else `NONE`; `targetGroup()` is the target subject's group (the old
`SacrificeTargetCreatureEffect` component); `resolvesAgainstAttachedPermanent()` for `attached()`. `sacrificesSource()`
is kept, so every engine reader of the source form (mana-ability inline path in `ActivatedAbilityExecutionService`,
`TriggeredAbilityQueueService`, `MayAbilityHandlerService`, `DestructionSupport.sacrificeSource`,
`ForcedCostOrElseEffectHandler`) is unchanged. The handler resolves both subjects through `SubjectResolutionService`
and does nothing unless the sacrificer controls the permanent (CR 701.21a), a target still matches its filter (creature
when none; CR 608.2b), it can be sacrificed, and Sigarda allows it (`canEffectCauseSacrifice`); then the old step
departure (`departAsStep`, or `sacrificeThenContinue` for the token rider). The Master, Multiplied stays in
`DepartureService.isUnaffected`; the scheme abandon branch is unchanged. No manafx handler exists for the record: the
mana-ability sacrifice is inline and matches the class, which did not change (5b's concern).

**Deleted** (3 records, 3 handlers; ratchet 4,886 / 4,119): `SacrificeEnchantedCreatureEffect` →
`(attached())` (Thirsting Axe: "if equipped creature didn't deal combat damage ..., sacrifice it", read as it resolves)
or `(attachedAtTrigger(), activePlayer())` ("that player sacrifices that creature / it / that artifact" on the upkeep /
end step of the enchanted permanent's controller: Lingering Death, Soul Tithe, Slow Motion, Curse Artifact; "that
creature" is the permanent the trigger condition named, as for Paroxysm and Venarian Gold in step 3; the old handler
read the recorded `triggeringPermanentControllerId`, which is that active player);
`SacrificeTargetCreatureEffect(g)` → `(target(g))` (Burn Together, Special Move); `SacrificePermanentDamagedPlayerControlsEffect`
→ `(target(), triggeringPlayer(), creature ∧ PermanentControlledByTriggeringPlayerPredicate)` (Ashling). 8 card files
rewritten by a deleted scratch script. Readers converted: `DestructionSupport`'s forced-cost else-branch (`SacrificeSelfEffect`
with `PermanentSubject.Attached` or `AttachedAtTrigger`, through an `ObjectProvider<SacrificeSelfEffectHandler>`), `ForcedCostOrElseEffectHandler.
killsBeforeChangingAnything` (`Source`, `Attached` or `AttachedAtTrigger`), `MayEffectHandler`'s "can that player
sacrifice it" check (Curse Artifact; now the resolver on the effect's subject). The declined-payment synthetic entry in
`MayPenaltyChoiceHandlerService` (enchanted-controller form) now carries the trigger-time host as its
`attachedPermanentSnapshot`, read from the Aura's trigger-time source snapshot, so `attachedAtTrigger()` works there. No AI code read the absorbed records (`TargetPolarityClassifier` had no entry for them or the
survivor; unchanged).

**The `source()` fallback.** A temporary probe that threw when a `source()` sacrifice resolved on an entry with no
source permanent and a target never fired across every test class of the 314 `SacrificeSelfEffect` cards (plus the
handlers that call `SacrificeSelfEffectHandler` directly); step 3 had already made the lookup strict.

**Behaviour changes.** (1) Sigarda, Host of Herons ("spells and abilities your opponents control can't cause you to
sacrifice permanents") now stops every `SacrificeSelfEffect` whose sacrificer isn't the ability's controller: Ashling,
Basalt Golem, the evoke and end-step "its controller sacrifices it" forms; only the enchanted-creature record checked
it before. Test: `AshlingTheExtinguisherTest.sigardaStopsTheSacrifice`. (2) Burn Together / Foot Toss: "then
sacrifice it" is the spell's controller; if another player gained control of the creature, nothing is sacrificed
(CR 701.21a), where the old handler made its new controller sacrifice it. Test:
`CallousSellSwordTest.adventureDoesNotSacrificeASourceAnotherPlayerNowControls`. (3) The target and Ashling sacrifices
are a step departure (CR 616.1 question first) instead of `sacrificeAndLog`; Ashling's target is rechecked against its
filter as it resolves. (4) An unattached Aura's sacrifice records an empty departure result and no longer logs a skip. (5) Lingering Death,
Soul Tithe, Slow Motion and Curse Artifact sacrifice the permanent the Aura enchanted as the ability triggered, not the
one it enchants as it resolves (Oracle "that creature" / "it" / "that artifact" refers to the trigger condition's
permanent). Tests: `SlowMotionTest.sacrificesCreatureEnchantedAtTriggerAfterAuraMoves` (new);
`SoulTitheTest`'s Aura Graft case rewritten (it asserted the old current-host reading; now "still sacrifices the
permanent it enchanted as it triggered"). Soul Tithe's {X} is still the current host's mana value
(`EnchantedPermanentManaValue`); "its mana value" is arguably the trigger-time permanent's too, not changed here.

**Tests**: 376 classes, 3,766 test cases, all pass (then, after the `attachedAtTrigger()` follow-up, 76 suites /
670 cases: the four Aura cards, Thirsting Axe, Paroxysm, Venarian Gold, every enchanted-controller may-pay card, the
`ForcedCostOrElseEffect` / `MayEffect` sample and the forced-cost application tests): every test class of a `SacrificeSelfEffect` card (with the probe),
the rewritten cards' tests, Sigarda, Animate Dead, a 30-per-class sample of `ForcedCostOrElseEffect` and `MayEffect`
cards, `TargetPolarityGuardTest`, the two ratchets, `DestructionSupportFallbackTest`, `SubjectResolutionServiceTest`,
the application tests naming `SacrificeSelfEffect`.

**For later runs.**

- Kept (ledger, `blocked-other`): `SacrificeEnchantedCreatureOnLeaveEffect` reads the departed Aura's
  `chosenPermanentId` when it never attached (Animate Dead, Necromancy, Dance of the Dead; Reality Acid), which no
  subject reads; it folds into `(attached(), controllerOf(attached()))` once one does. The delayed records
  (`SacrificeSelfAtEndStepEffect`, `SacrificeTargetPermanentAtEndStepEffect`, `SacrificeAtEndOfCombatEffect`,
  `SacrificeSelfAtNextCleanupEffect`) use `TemporaryGlobalTriggeredAbility`, not `DelayedPermanentActions` (5a's note).
  `SacrificeSelfUnlessEscapedEffect` is a condition wrapper (step 9), `SacrificeSelfThenEffect` fused (step 8),
  `SacrificeEachMatchingPermanentEffect` a `controlledBy` set (per-player Sigarda skip, sacrificed together).
- `PermanentReference` lost its Sacrifice reader; the rest of 5i's list stands.
- A sacrifice "that player" on an enchanted-controller step trigger is `activePlayer()`; `triggeringPlayer()` does not
  work there for the end step (its entry's `targetId` is the enchanted permanent).

### Step 5, run 5k (2026-11-20)

`--family Flicker` / `PhaseOut` / `Remove` and `--siblings` give one flicker record (`FlickerEffect`, 147 cards), the
phase-out records (`PhaseOutEffect` 33, `PhaseOutPermanentsEffect` 4, `PreventPhaseOutEffect` 2, 12 one-card records with
their own duration or choice) and `Remove*FromCombat` (4 plus the fused Ydwen Efreet record).

**Survivors.** (1) `FlickerEffect`'s `FlickerScope scope` became `PermanentSubject subject` in every constructor (first
parameter, `null` normalises to `target()`); `withScope` became `withSubject`, new `exilesHostWithAuras()`. `filter` is now
only a target restriction (rejected for any other subject); a set keeps its filter in the subject; `chooseAnyNumber`
needs `controlledBy(you(), ...)`. `targetSpec()` per old constant: benign permanent (+ filter) for `target()`, benign
player for a `controlledBy` whose players declare a target, the self-targeting marker for `source()` and the
host-with-Auras shape (Flickerform's activation capture), else `NONE` (Kaya's `oneOf`, as SELF_OR_TARGET had). The
handler keeps every per-shape path (immediate batch with every bound group exiled first, delayed return through
`PendingExileReturns` / `exileAndScheduleReturn`, tokens not returning, counters and Auras falling off, return under
owner's or your control, `returnedPermanentIds` cleared after a re-exile) and names its permanents only through
`SubjectResolutionService`; the three set paths (target player's, your, every player's, plus returned permanents) are
one `exileSetAtStep` (the two old copies differed only in the owner fallback, which is the same player for permanents
you control, and in the controller-next-step flag, false on every non-`you()` card). `MultiPermanentChoiceHandlerService`'s
"still eligible" check for an any-number flicker reads `FlickerEffectHandler.eligibleChoices` (the subject) instead of its
own filter lookup. (2) `PhaseOutEffect(PermanentSubject)`: `target()` harmful permanent, `controlledBy(targetPlayer(),
f)` harmful player, else `NONE`; one handler phases every named permanent out together through `PhasingService`
(attachments indirectly, CR 702.26g; removed from combat, CR 506.4; no zone change) and logs a count for a set. Teferi's
Veil's delayed payload is `named(attackerId)`. (3) `PreventPhaseOutEffect(PermanentSubject)`, `target()` or `source()`
only. (4) `RemoveTargetFromCombatEffect(PermanentSubject subject, boolean unblockSoleBlockedAttackers)`, every old
constructor kept plus `(PermanentSubject)`; benign creature spec only for `target()`; the handler removes each named
permanent through `CombatRemovalSupport`.

Constants mapped. FlickerScope: TARGET `target()`, SELF `source()`, TRIGGERING `triggering()`, SELF_OR_TARGET **new**
`oneOf(source(), target())`, TARGET_PLAYERS_PERMANENTS `controlledBy(targetPlayer(), f)`, CONTROLLERS_PERMANENTS
`controlledBy(you(), f)`, ALL_PLAYERS_PERMANENTS `controlledBy(eachPlayer(), f)`, RETURNED_PERMANENTS **new**
`returnedThisResolution()`, ENCHANTED_CREATURE_AND_AURAS `allOf(attached(), attachmentsOf(attached(), Aura))`,
TARGET_EXILED_EARLIER **new** `exiledThisResolution()`. PhaseOutSubject: SOURCE `source()`, TARGET `target()`, ATTACHED
`attached()`. PhaseOutScope: CONTROLLER `controlledBy(you(), f)`, TARGET_PLAYER `controlledBy(targetPlayer(), f)`, ALL
dead (confirmed: only the `(filter, false)` compatibility constructor reached it and no card or engine code called that).

**Resolver.** Three variants: `returnedThisResolution()` (the entry's `returnedPermanentIds`), `exiledThisResolution()`
(names nothing on the battlefield; `permanentIds` is the entry's target id, whose card the entry remembered; the old read)
and `oneOf(options...)` (refused by the resolver: the verb's handler resolves each option and asks). Tests:
`SubjectResolutionServiceTest$ReturnedThisResolution`, `$ExiledThisResolution`, `$OneOf`.

**Deleted** (`FlickerScope`, `PhaseOutSubject`, `PhaseOutScope`; 4 records, 4 handlers; ratchet 4,882 / 4,115):
`RemoveSelfFromCombatEffect` and `RemoveSourceFromCombatEffect` → `RemoveTargetFromCombatEffect(Subjects.source())` (five
Gustcloaks; Mijae Djinn, Shakedown Heavy), `RemoveReferencedPermanentFromCombatEffect` → `(attached())` (Observed
Stasis), `(triggering())` (Gustcloak Savior), `(triggering(), true)` (Imprison), `PhaseOutPermanentsEffect` →
`PhaseOutEffect(controlledBy(you(), f))` (Taniwha, Teferi's Protection, Perch Protection) or `(controlledBy(targetPlayer(),
f))` (Galadriel's Dismissal). 61 card files rewritten (a deleted scratch script, Ghostway, Golden Argosy and the four
phase-out sets by hand), 8 engine, 8 domain, 1 AI main, 5 test and 7 agent-docs files changed.

**`source()` fallback.** `RemoveSelfFromCombatEffect` fell back to the entry's target; a temporary probe that threw when
a `source()` removal resolved on an entry with no source and a target never fired across every card test of the
survivors. The other `source()` lookups (flicker SELF, phase-out SOURCE) were already strict.

**AI parity.** `TargetPolarityClassifier`: `PhaseOutEffect` is HARMFUL_REMOVAL only for `target()` (the old TARGET; the
absorbed set record had no entry); `RemoveTargetFromCombatEffect`'s name entry became a structural branch, HARMFUL for
`target()` and `triggering()` (what the name gave both), none for the absorbed `source()` / `attached()` forms (their
classes had no entry, so Observed Stasis's own-entry effects classify as before). `FlickerEffect` keeps its name entry.

**Behaviour changes.** (1) Flickerform exiles the creature it enchants as the ability resolves ("exile enchanted
creature", CR 608.2h), not the host captured at activation. Test: `FlickerformTest.exilesTheCreatureEnchantedAtResolution`.
(2) An any-number flicker (Yorion, Nahiri's Resolve, Another Round) re-checks that a chosen permanent is still one you
control matching the filter with the resolver's fuller filter context. (3) Its attached Auras are found by effective
subtype instead of the printed one. (4) Gustcloak removals log "X removes X from combat." and leave an attacker marked
blocked-without-blockers only when no other blocker remains (the attacker stays blocked either way, CR 509.1h).
(5) Set flickers and set phase-outs list players in APNAP order instead of seat order (simultaneous; log order only).

**Kept exactly.** New objects on return (CR 400.7), return under owner's / your control, immediate vs. delayed return
(the delayed ones still `PendingExileReturns` / `TemporaryGlobalTriggeredAbility`, CR 603.7), tokens not returning
(CR 111.7), `returnedPermanentIds` recording; indirect phasing and no leave/enter triggers; combat removal through
`CombatRemovalSupport`.

**Tests**: 253 suites, 2,068 test cases, all pass: every test class of a card naming one of the four survivors (with the
probe), Melee, Skywriter Djinn, Sword of Hearth and Home, Teferi's Veil, Ydwen Efreet, Fortune, Zuko, Sol'kanar,
`FlickerEffectHandlerTest` (now over a real resolver on its mocks), `SubjectResolutionServiceTest`,
`StepTriggerServiceTest`, `UntapStepServiceTest`, `PermanentRemovalServiceTest`, `DestructionSupportFallbackTest`, the
two ratchets, `TargetPolarityGuardTest`.

**For later runs.**

- `PermanentReference` and `Subjects.of` stay; their readers are now Explore, Goad, TurnSourceFaceUp, Shuffle,
  MustBlockSource, `DoesntUntapWithCounterEffect`, `PutOrRemoveCounterIfAttackedOrBlockedSinceLastUpkeep`, the fused
  `RemoveAllCountersThenDestroyReferencedPermanentAndDamageController` and `PermanentBlockingSourcePredicate`.
- Kept (ledger): `RemoveSourceFromCombatAndUnblockSoleBlockersEffect` is fused with a can't-block effect (step 8; its
  removal half is `RemoveTargetFromCombatEffect(source(), true)`); `PhaseOutAttackingCreatureAtEndOfCombatEffect` uses
  `TemporaryGlobalTriggeredAbility` (5a's delayed-mechanism note); `ExileTargetCreatureAndReturnAtNextEndStepEffect`
  declares a harmful creature target and is a `RemovalEffect` where the flicker target is benign; the other
  `ExileSelfReturnAt*` / `...DeclareAttackers` records have their own return timing; the one-card phase-out records
  carry a duration or a resolution-time choice.
- `oneOf` is the shape for any "this or target" choice; `returnedThisResolution()` / `exiledThisResolution()` are early
  step-7 results that still read the entry's `returnedPermanentIds` / target id; step 7 moves them to the scratchpad.

### Step 5, run 5l (2026-11-20)

`--siblings` gives `Attach*EquipmentTo*` (5), `CreateTokenCopyOf*` (5: Target 139, Source 84, Enchanted 5, Dying 2, Equipped 1)
and `CreateToken*To*` (4: AttachedToTarget 35, Source 3, DefendingPlayer 1, TriggeringPermanent 1). Each family has at least
two true subject siblings once the handlers are read.

**Survivor A.** `AttachSourceEquipmentToTargetCreatureEffect(thenEffect, thenEffectOptionalTarget, targetPredicate,
PermanentSubject equipment, PermanentSubject host)` (the `useTriggeringPermanent` flag became `host` `triggering()`; every
old constructor and `forCreatureYouControl()` / `forTriggeringCreature()` kept, so its 84 cards are untouched). New `(equipment,
host[, targetPredicate])` and `targetEquipmentToTriggeringOrSource()`. Each subject is one of `source()`, `target([g])`,
`triggering()`, `triggeringOrSource()`; two targets need two groups. `targetSpec()` keeps every old spec: benign permanent
for two targets; for a host target benign permanent, or creature + predicate; for an Equipment target benign permanent, or
permanent + predicate (the old triggering-host record's Equipment predicate); else `NONE`. `actsOnlyOnItsTargets()` for an
Equipment target with a non-target host (the old triggering-host record; also the target-to-source shape, never inside a
`MayEffect`). The handler names both through `SubjectResolutionService`, attaches each Equipment `canAttachEquipment` allows
(CR 301.5c), leaves one already on the host where it is (CR 701.3b), restamps a moved one (CR 613.7e) and fires the
unattach / "becomes attached" triggers, then queues the reflexive continuation if any Equipment is on the host.

**Survivor B.** `CreateTokenCopyOfTargetPermanentEffect`'s `Copied copied` became `PermanentSubject subject` (`null` =
`target()`): TARGET `target()`, TRIGGERING_PERMANENT `triggering()` ("that creature", or "one of them" chosen as it resolves
when the event was about several: the handler reads the group through the resolver), EACH_TRIGGERING_PERMANENT **new**
`triggeringAll()`, plus `attached()` (`ofAttached()`) and `named(id)` (`ofNamed(id)`); `withSubject` is public. The target
path reads its targets as before; every other subject goes through the resolver. A non-target permanent that left is copied
from the entry's last-known card, else from `GameData.departedPermanentSnapshots` (the old Aura / Equipment lookup).
`createCopiesOfHost(gd, entry, attachmentId, amount)` is the Mirrormind Crown / Moonlit Meditation replacement (copies of the
host as `named(hostId)`, no second replacement offered); `CreateTokenEffectHandler` calls it and `EsixTokenReplacementSupport`'s
pending may abilities list `ofNamed(host)`.

**Survivor C.** `CreateTokenAttachedToTargetEffect`'s `boolean oneOfTriggeringPermanents` became `PermanentSubject subject`
(`target()` default, `source()`, `triggering()`); new `(token, subject)` (relation ANY, creature filter). `triggering()` is the
old "one of them" path (group through the resolver's `triggeringAll()`, else the one permanent; filter, live, a choice when
several); `source()` attaches to the live source; the target path is unchanged.

**Resolver.** New `PermanentSubject.TriggeringAll` / `Subjects.triggeringAll()` (`getTriggeringPermanentIds()`, else the
triggering permanent). Test: `SubjectResolutionServiceTest$Triggering.triggeringAllPermanents`.

**Deleted** (enum `CreateTokenCopyOfTargetPermanentEffect.Copied`; 9 records, 9 handlers; ratchet 4,873 / 4,106):
`AttachTargetEquipmentToTargetCreatureEffect` → A `(target(0), target(1))` (`creatureFirst()` `(target(1), target(0))`;
11 cards), `AttachTargetEquipmentToTriggeringPermanentEffect` → A `targetEquipmentToTriggeringOrSource()` (6),
`AttachTargetEquipmentToSourceEffect` → A `(target(), source())` (3), `AttachTriggeringEquipmentToTargetCreatureEffect` → A
`(triggering(), target())` (2); `CreateTokenCopyOfEnchantedPermanentEffect` → B profile `.ofAttached()` (Followed Footsteps,
Mechanized Production, Ghastly Mimicry, Penumbra Umbra) or `.ofTriggeringPermanent()` (Mirror Mockery),
`CreateTokenCopyOfEquippedCreatureEffect` → B `(…, grantHaste, …, removeLegendary).ofAttached()` (Helm of the Host),
`CreateTokenCopyOfDyingCreatureEffect` → B profile `.ofTriggeringPermanent()` (Ratadrabik, Genestealer Patriarch; the
death collector records the dying creature's last-known card, so the old card-id and graveyard-card fallbacks had no user);
`CreateTokenAttachedToSourceEffect` → C `(token, source())` (3), `CreateTokenAttachedToTriggeringPermanentEffect` → C
`(token, triggering())` (Gylwain; a per-creature enter trigger records a group of one, CR 603.2c). 33 card files rewritten
(a deleted scratch script for the attach family, the rest by hand); 6 engine, 5 domain, 1 AI main, 4 application test, 4 card test and
10 agent-docs files changed.

**`source()` fallback.** The attach handler found a source Equipment by card id when the entry had no source permanent ("death
trigger path"); a temporary probe that threw there never fired across every test class of the survivors' cards, so the
lookup is the strict `source()`. `EquipSupport.findEquipmentByCardId` stays for its other four callers.

**AI parity.** `TargetPolarityClassifier`: the four attach name entries became one structural branch (BENEFICIAL, none for
a `triggering()` Equipment, which had no entry); the copy name entry became a branch (NEUTRAL, none for `attached()` /
`named`); the token-attached branch returns none for `source()`. The Dying cards now classify NEUTRAL (was none) and
Gylwain BENEFICIAL (was none); both abilities declare no target, so no target choice reads it. `SpellEvaluator` scores
`TokenCreatingEffect`s only on ETB and spell effects; the Dying record's interface was on a death trigger.

**Behaviour changes.** (1) An Equipment already attached to the host doesn't move, get a new timestamp or fire
"becomes attached" (CR 701.3b); the target-to-triggering, target-to-source and triggering-to-target records re-attached it.
Test: `ArmoryAutomatonTest.alreadyAttachedEquipmentDoesNotMove`. (2) Mirror Mockery copies "that creature", the attacking
creature, even after the Aura moved in response (it copied the current host). Test:
`MirrorMockeryTest.copiesTheAttackingCreatureAfterTheAuraMoves`. (3) Helm of the Host copies the equipped creature's copiable
values (CR 707.2: a face-down creature is a nameless 2/2) instead of its card. Test:
`HelmOfTheHostTest.copiesFaceDownCreatureAsItsCopiableValues`. (4) Copies of an Aura's or Equipment's host offer the "first
time you would create tokens" replacements (Esix, Mirrormind Crown, Moonlit Meditation), as every other token creation does.
Test: `FollowedFootstepsTest.tokenCreationReplacementApplies`. (5) Attaching no longer requires the printed Equipment subtype
as well as the effective one, and the target-to-source shape's "you control" is the target filter's resolution recheck. (6)
Log lines are the survivor's ("X is now attached to Y."; none when nothing moved).

**Kept exactly.** Copiable values and the "except …" modifications (CR 707.2, 707.9), copies of a permanent that left by
last-known information (CR 608.2h, 111.12), the target paths of both copy and token-attached records (bound groups, the
legacy target-less "one of them" check), Esix's offer, the reflexive attach continuation, every target spec.

**Tests**: 304 suites, 2,519 test cases, all pass (every test class of every card naming one of the three survivors, with
the probe; Esix, Mirrormind Crown, Moonlit Meditation; `DrawServiceTest`, `LibraryTriggersTest`, `MayEffectHandlerTest`,
`CreateTokenEffectHandlerTest`, `CardImmutabilityArchTest`, `SubjectResolutionServiceTest`, the two ratchets,
`TargetPolarityGuardTest`), then the five new tests' classes.

**For later runs.**

- Kept (ledger, `blocked-other`): `CreateTokenAttachedToDefendingPlayerEffect` (a player host; an attach verb with a player
  host subject folds it and `AttachSourceAuraToTargetPlayerEffect`); `CreateTokenCopyOfSourceEffect` (head record, step 10).
- Not audited (5m): `AttachCreatedEquipmentTo*` (2, a step-7 "created" subject), `AttachOneOfControlledEquipmentTo*` (2, a
  resolution-time choice), `Attach*AuraTo*` (3), `CreateTokenCopyOf*For*` (4, a token controller subject),
  `CreateTokenFor*` (4).
- `triggeringAll()` is the plural "them"; a one-permanent verb reads it for "one of them" (B, C).

### Step 5, run 5m (2026-11-20, partly done)

`--siblings` listed 151 groups with 332 unaudited classes at the start, 135 groups / 289 classes now. Worked largest
group first, then the leftovers earlier runs named, and stopped at 14 folded classes because the next candidates each need
a mechanism first (below). 5d's restriction readers (`SkipNextUntap`, `CantBlockThisTurn`, `CantAttackThisTurn`,
`DoesntUntap`, `DoesntUntapWithCounter`, `UntapAllPermanentsYouControlDuringEachOtherPlayersStep`) are planned for step 6.

**`PermanentReference` is gone**, with `Subjects.of`. Its nine readers now carry a `PermanentSubject` component of the same
name and default: `ExploreEffect.subject`, `GoadTargetCreatureUntilNextTurnEffect.goaded`, `TurnSourceFaceUpEffect.subject`,
`ShuffleReferencedPermanentIntoLibraryEffect.subject`, `MustBlockSourceEffect.blocked`, `DoesntUntapWithCounterEffect.counterBearer`,
`PutOrRemoveCounterIfAttackedOrBlockedSinceLastUpkeepEffect.subject`, the fused
`RemoveAllCountersThenDestroyReferencedPermanentAndDamageControllerEffect.subject` and
`PermanentBlockingSourcePredicate.blocked` (domain filter). Each compact constructor keeps its old allowed set; the readers
(`UntapStepService`, `PredicateEvaluationService`, six handlers) read the subject directly. 4 card files rewritten (Shape of
the Wiitigo, Path of Discovery, Defossilize, Bomb Squad). Since `Subjects.of` returned the same constants, nothing resolves
differently.

**Survivors and what they absorbed** (14 records, 14 handlers deleted; ratchet 4,859 / 4,092):

- `RemoveAllCountersFromTargetPermanentEffect(PermanentSubject subject, PermanentPredicate targetFilter)`: `target()` default,
  `fromTargetCreature()` (benign `permanents(creature)` spec) absorbs `RemoveAllCountersFromTargetCreatureEffect` (Perfect
  Intimidation, Thorna and Twigtooth); `(Subjects.attached())` absorbs `RemoveAllCountersFromEnchantedCreatureEffect`
  (Enchanted River's Grasp, Honest Work). The handler resolves the subject and keeps the target handler's path for each.
- Player verbs (4b/4c/4d/4a leftovers): `TargetSpellControllerLosesLifeEffect` → `LoseLifeEffect(n, controllerOf(targetSpell()))`
  (Countersquall, Psychic Barrier, Deny the Witch, Punish Ignorance, Undermine) and `controllerOf(triggeringSpell())` (Forsaken
  Wastes, whose old handler special-cased the triggering spell); `TargetSpellControllerGainsLifeEqualToManaValueEffect` →
  `GainLifeEffect(new TargetSpellManaValue(), controllerOf(targetSpell()))` (Illumination);
  `EachOpponentWithPoisonCountersLosesLifeEffect(m, n)` → `LoseLifeEffect(m, matching(eachOpponent(), new
  PlayerHasPoisonCountersAtLeastPredicate(n)))` (Feed the Infection, Phyrexian Atlas; new `PlayerPredicate`, evaluated in
  `TargetLegalityService`); `DrawCardForYouAndDamagedPlayerEffect` → `DrawCardEffect(n, allOf(you(), triggeringPlayer()))`
  (Xyris, Black Widow; APNAP order and one draw for one player, as before). Its mock handler test is deleted; Xyris's
  "active player draws first" card test covers the order.
- `ShuffleReferencedPermanentIntoLibraryEffect` (`attached()` default, rejects a target) absorbs
  `ShuffleEnchantedPermanentIntoOwnerLibraryEffect` (Watery Grasp, Stay Hidden, Stay Silent); `resolvesAgainstAttachedPermanent()`
  keeps the activation-time host capture the old record's `AttachedPermanentSelfTargetingEffect` marker gave.
- `AttachSourceEquipmentToTargetCreatureEffect.forCreatureYouControl()` absorbs `AttachCreatedEquipmentToTargetCreatureEffect`
  (Dáin Ironfoot, Adaptive Armorer, Masterpiece Vault): the reflexive entry's source is the created or drafted Equipment, the
  spec is the same, and the target's "creature you control" recheck is the resolution-time target legality check.
- `ControllerLosesGameEffect(PlayerSubject player)` (`you()` default, rejects a target) absorbs `EnchantedPlayerLosesGameEffect`
  → `(enchantedPlayer())` (Sinner's Judgment).
- `PutTargetPermanentIntoLibraryNFromTopEffect(DynamicAmount position, PermanentSubject subject)` (`boolean self` became
  `source()`; `self(n)` kept) absorbs `PutSourcePermanentIntoLibraryNFromTopEffect` (Braided Quipu) and
  `PutEnchantedPermanentIntoLibraryNFromTopEffect` → `(attached(), n)` (Shattered Ego; keeps the self-targeting marker and the
  attached capture). Positions are clamped at 0 for every form.
- `DoubleCountersOnTargetPermanentEffect(CounterType, PermanentSubject)`: `onSource(t)` absorbs `DoubleCountersOnSourceEffect`
  (The Millennium Calendar, Ascendant Acolyte); the target path is untouched.
- `DoubleSelfPowerToughnessEffect(boolean powerOnly, PermanentSubject)`: `ofTargetCreature()` (benign creature spec, creature
  recheck) absorbs `DoubleTargetCreaturePowerToughnessEffect` (World War Hulk, Nylea's Colossus) and replaces the source record on
  the three spells that relied on its legacy source-to-`targetId` fallback (Epic Fight, Dragonclaw Strike, Choose Your Weapon);
  `source()` is now strict (step 3).
- `MoveDyingSourceCountersToTargetCreatureEffect(boolean controllerOnly, PermanentSubject)`: `toSource()` absorbs
  `MoveDyingSourceCountersToSourceEffect` (Buzzard-Wasp Colony; keeps its self-targeting marker).

36 card files rewritten (scratch scripts, deleted; 4 by hand), 30 engine (14 handlers deleted), 33 domain (14 records and
`PermanentReference` deleted, the predicate added), 2 AI main, 1 AI test, 3 application test, 2 card test and 10 agent-docs
files changed.

**Behaviour changes.** (1) Removing all counters from target creature now runs the vanishing last-time-counter check and the
battle defense check, as the target-permanent record did: "when the last time counter is removed from it, sacrifice it"
(CR 702.63a). Test: `PerfectIntimidationTest.counterModeRemovingLastTimeCounterTriggersVanishing` (fails on the old handler).
The Aura cards make their host lose all abilities, so vanishing does not reach them. (2) Feed the Infection / Phyrexian Atlas
lose life in APNAP order (simultaneous; log order only). (3) "That spell's controller" also falls back to the controller the
entry recorded as the spell left (`targetSpellController`), where the old handler did nothing. (4) Illumination gains through
`GainLifeEffect` even for a mana value of 0 (no life changes: `completeGainLife` returns for 0). (5) Log lines are the
survivors'. Test added for the dropped explicit check: `DainIronfootTest.axeDoesNotAttachToCreatureAnOpponentNowControls`.

**AI parity.** `TargetPolarityClassifier`: the two remove-all name entries became a structural branch (HARMFUL with a filter,
NEUTRAL without, none for `attached()`); `DoubleTargetCreaturePowerToughness`'s entry became a branch on
`DoubleSelfPowerToughnessEffect` (BENEFICIAL for `target()`), so the three spells above now classify their target BENEFICIAL
(was none). `SpellEvaluator.gainsForTargetPlayer` also excludes `controllerOf(targetSpell())`, so Illumination's gain for the
countered spell's controller is not scored as the AI's own. `LoseLifeEffect` with `controllerOf` / `matching` players scores 0,
as the absorbed records (never read) did. The created-Equipment attach now classifies BENEFICIAL (was none; its target must be
a creature you control).

**Tests**: 390 suites, 3,588 test cases, all pass: every test class of every card naming a survivor or a `PermanentReference`
reader (or `PermanentBlockingSourcePredicate`), a 12-per-class sample of `LoseLifeEffect`, `GainLifeEffect`, `DrawCardEffect`,
the rewritten cards, `EffectCountRatchetTest`, `SubjectResolutionServiceTest`, `CardEffectTargetingConsistencyTest`,
`UntapStepServiceTest`, `TargetLegalityServiceTest`, `TriggeredAbilityStackServiceTest`, `EndTurnEffectHandlerTest`,
`EtbEffectResolverTest`, `TargetPolarityGuardTest`, `SpellEvaluatorTest`.

**For later runs (what 5m left).**

- Ledgered here (`blocked-other` unless noted), each with the mechanism it waits for: the token-controller axis
  (`CreateTokenFor{TargetPlayer,TriggeringPlayer,SourceController,Player}Effect`, `CreateTokenCopyOf*For*`: `CreateTokenEffect`
  and `CreateTokenCopyOfTargetPermanentEffect` need a `PlayerSubject` token controller, whose `createForTargetController` flag is
  `controllerOf(target())`; the creation paths differ in the Esix offer, the investigate event and the attack choice);
  `AttachCreatedEquipmentToSourceEffect` (step-7 created subject), `AttachOneOfControlledEquipmentTo*` (step-7 picked subject),
  `Attach*AuraTo*` (target groups for Aura Finesse; a player host); the static `GoadEquippedCreature` / `GoadSourceCreature`
  (step 6); `TurnTarget{Creature,}FaceUpEffect` (two different turn-face-up paths: unify first); `ShuffleSelf…` /
  `ShuffleTargetPermanentIntoLibraryEffect` (then-riders, step 8); `TargetPlayerLosesGameEffect` (folds into
  `ControllerLosesGameEffect` with a per-card `triggeringPlayer()` mapping of its combat-damage cards); `GainControlOf*` (the
  record is the floating control effect's payload); `*ExilesFromHand` (`blocked-player-loop`).
- Still unaudited (`--siblings`, 135 groups / 289 classes), largest first: `GrantStaticEffectTo*UntilEndOfTurn` (6, a
  `GrantScope` family: step 6), `*DrawsCard` (`EachPlayer`/`EachOtherPlayer`/`TargetPermanentController`; 4a's AI rule for
  opponents' draws), the `*BecomesChosenSubtype*`, `*CantCastSpellsThisTurn`, `*CantGainLife*`, `*ChoosesCardFromGraveyardToBattlefield`,
  `*CreatesToken`, `*MustAttack*`, `*SpellsCantBeCountered`, `BecomeCopyOf*`, `Choose*OnEnter`, `Controlled*DealPowerDamageTo*`,
  `CopyThisSpellFor*`, `Double*Life`, `DoubleDamageFrom*`, `Exile*Until*Leaves`, `GrantEffectTo*UntilEndOfTurn`, `MoveCounter(s)From*To*`,
  `RedirectNextDamageFromChosen*To*` groups of three, and the pairs. Many are player-scoped or static restrictions
  (step 6 / a player-restriction run), not one-shot permanent verbs.
- `PutTargetOnTopOfLibraryEffect.self()` is the same verb as `PutTargetPermanentIntoLibraryNFromTopEffect.self(0)`; not a
  name sibling, not checked.

### Step 6, runs 6.1 and 6.2a (2026-11-20)

**6.1 Inventory.** `GrantScope.subject()` (domain) gives each constant's subject; `Subjects.narrowed(set, filter)` ANDs an
effect's own filter into a set (`controlledBy` / `ownedBy` / `attachmentsOf`; a non-set throws). "Other" is
`PermanentNotPredicate(PermanentIsSourcePermanentPredicate)` (by permanent id, 5d's form; 32 tap/untap cards use the card-id
form `PermanentIsSourceCardPredicate`: check a card before swapping forms in 6.3). Readers were found by a scratch grep of
`GrantScope.X` comparisons and `case X` over engine and AI main code; a construction of a `GrantScope.TARGET` floating payload
is not a reader. One-shot = `normalfx`; static = `staticfx` + `StaticEffectSupport` (SES); L/Q = `LayerSystemService` (LSS) +
`GameQueryService` ("both" = the two); other = engine classifiers outside those.

| Constant | `subject()` | One-shot (6.2a, done) | Static (6.2b) | L/Q (6.2c) | AI (6.2d) | Other (6.2c) |
|---|---|---|---|---|---|---|
| SELF | `source()` | 26 handlers | 20 `*Self*` handlers, `SetCardTypes`, `SetName`, `SetChosenNameAndCreatureType`, `GrantTriggeredAbility`, `BoostByAttackCount`, SES | both | | `LayerClassifier`, `PermanentCopierService`, `TriggerIndex`, `TokenCreationReplacementSupport`, `EachOpponentChoosesCreatureCreateTokenCopy…` handler |
| TRIGGERING_PERMANENT | `triggering()` | AnimationSupport, BecomeSaddled, GrantColorUEOT, GrantKeyword, GrantSubtypeResolution | | | | |
| SELF_AND_PAIRED | `controlledBy(eachPlayer(), PermanentIsSourceOrPairedPredicate)` | | 8 (`*Self*`, `GrantEffect`, `GrantTriggeredAbility`), SES | | | |
| TARGET | `target()` | 18 handlers | | LSS (payload marker) | `AiTargetSelector`, `SpellEvaluator`, `TargetPolarityClassifier` | |
| TARGET_AND_SHARING_CREATURES | throws: needs a shares-a-colour-with-target predicate | GrantKeyword (own lookup kept) | | | `AiTargetSelector`, `TargetPolarityClassifier` | |
| TARGETS | `target()` | GrantColorUEOT, SetCombatRequirement; GrantKeyword's is the triggering spell's targets (own lookup kept: needs `triggeringSpellTargets()`) | | | | |
| DECLARED_ATTACKERS | throws: needs `triggeringAttackers()` | GrantKeyword (own lookup kept) | | | | |
| ENCHANTED_CREATURE, EQUIPPED_CREATURE | `attached()` (the creature check stays in the reader) | GrantKeyword, SetCombatRequirement, Suspect, Unsuspect | `ProtectionFromChosenColor`, `ProtectionFromColorsOfPermanentsYouControl`, `ProtectionFromMulticolored`, SES | both | `AiTargetSelector`, `GameSimulator`, `SpellEvaluator`, `TargetPolarityClassifier` | `AuraAttachmentService`, `PredicateEvaluationService` |
| ENCHANTED_PERMANENT | `attached()` | AnimationSupport, GrantActivatedAbility, GrantEffectToTarget, GrantKeyword, GrantSupertypeUEOT, Unsuspect | SES | both | | `PredicateEvaluationService` |
| OWN_TAPPED_CREATURES | `controlledBy(you(), creature ∧ tapped ∧ ¬source)` | | SES | LSS | | |
| OWN_UNTAPPED_CREATURES | `controlledBy(you(), creature ∧ ¬tapped)` | | SES | | | |
| OWN_CREATURES | `controlledBy(you(), creature ∧ ¬source)` | 11 (most read it including the source, below) | SES | both | `BoardEvaluator`, `HardAiDecisionEngine`, `SpellEvaluator` | `AttackLegalityService`, `BattlefieldPlacementService` |
| ALL_OWN_CREATURES | `controlledBy(you(), creature)` | BoostAllCreaturesOfChosenSubtype, GrantKeyword, SetCombatRequirement | `DynamicStaticBoostSelf`, `GrantActivatedAbilitySelf`, `GrantColorSelf`, `StaticBoostSelf`, SES | both | same three | same two |
| OPPONENT_CREATURES | `controlledBy(eachOpponent(), creature)` | LosesAllAbilities, RemoveKeyword, SetBasePowerToughness | SES | both | | |
| OWN_PERMANENTS | `controlledBy(you())` | AnimatePermanents, GrantActivatedAbility, GrantKeyword | `AnimatePermanents`, `GrantColorSelf`, `GrantEffect`, `GrantKeywordSelf`, `GrantTriggeredAbility`, `SetCardTypes`, SES | both | | `LayerClassifier` |
| TARGET_PLAYERS_CREATURES | `controlledBy(targetPlayer(), creature)` | 9 handlers | | | | |
| TARGET_PLAYERS_LANDS | `controlledBy(targetPlayer(), land)` | AnimatePermanents | | | | |
| ALL_CREATURES | `controlledBy(eachPlayer(), creature ∧ ¬source)` | 8 (most read it including the source, below) | SES | both | `BoardEvaluator`, `SpellEvaluator` | |
| ALL_CREATURES_INCLUDING_SELF | `controlledBy(eachPlayer(), creature)` | 5 handlers | 5 `*Self*` handlers, SES | LSS | | |
| ALL_PERMANENTS | `controlledBy(eachPlayer(), ¬source)` | RemoveAllProtection, RemoveKeyword (including the source) | `AnimatePermanents`, `BecomeColorless`, `GrantCardType`, `GrantColor`, `GrantEffect`, `GrantSubtype`, `GrantTriggeredAbility`, `SetCardTypes` | LSS | | `LayerClassifier` |
| ENCHANTED_PLAYER_CREATURES | `controlledBy(enchantedPlayer(), creature)` | | SES | LSS | | `AttackLegalityService` |
| OWN_LANDS | `controlledBy(you(), land)` | AnimatePermanents, GrantActivatedAbility | `AllLandsAreCreatures`, `GrantActivatedAbilitySelf`, `LosesAllNonManaAbilities`, SES | both | | |
| OPPONENT_LANDS | `controlledBy(eachOpponent(), land)` | | `AllLandsAreCreatures`, SES | both | | |
| ALL_LANDS | `controlledBy(eachPlayer(), land ∧ ¬source)` | AnimatePermanents, GrantKeyword (both including the source) | `AllLandsAreCreatures`, `LosesAllNonManaAbilities`, SES | both | | `BattlefieldPlacementService` |
| ALL_LANDS_INCLUDING_SELF | `controlledBy(eachPlayer(), land)` | | 4 `*Self*` handlers, `LosesAllNonManaAbilities`, SES | LSS | | `BattlefieldPlacementService` |
| TOKENS_CREATED_THIS_RESOLUTION | throws: step 7's `createdThisResolution()` | AnimationSupport, GrantKeyword, Suspect, TargetCreatureDealsDamageToController (own lookups kept) | | | | |
| BANDED_WITH_SELF | throws: needs `bandmatesOf(source())` | GrantKeyword (own lookup kept) | | | | |

**Self-inclusion differs per verb on the one-shot path.** `subject()` follows step 3's table. These one-shot handlers read
the constant including the source and now name the including-self constant's subject, so 6.3 maps a card's constant by
verb, not by constant: ALL_CREATURES in GrantKeyword, RemoveKeyword, GrantProtectionChoice, TargetCreatureBecomesChosenSubtype
(GrantActivatedAbility, GrantColorUEOT, LosesAllAbilities and SetBasePowerToughness exclude it, as `subject()` does);
OWN_CREATURES in LosesAllAbilities, GrantColorUEOT, GrantSubtypeUEOT, TemporaryCreatureTypes
(`GrantAllCreatureTypesToOwnCreaturesEffect`), TargetCreatureBecomesSubtype, TargetCreatureBecomesChosenSubtype,
GrantProtectionChoice, GrantProtectionFromColorUEOT; ALL_LANDS in GrantKeyword and AnimationSupport (Natural Affinity);
ALL_PERMANENTS in RemoveKeyword (written `controlledBy(eachPlayer())`). BoostAllCreaturesOfChosenSubtype reads every own
constant as ALL_OWN_CREATURES and everything else as ALL_CREATURES_INCLUDING_SELF; GrantActivatedAbility's unknown-constant
default is ALL_OWN_CREATURES.

**`TapUntapScope` leftovers (5d).** Lasting player-scoped rules, which reach creatures arriving later (they change rules, not
characteristics: CR 611.2c, second sentence): `CantBlockThisTurnEffect` TARGET_PLAYERS_PERMANENTS,
TARGET_CONTROLLERS_OTHER_CREATURES and ALL_CREATURES (floating rules with a predicate); `CantAttackThisTurnEffect`
TARGET_PLAYERS_PERMANENTS (a player rule); `SkipNextUntapEffect` CONTROLLED / TARGET_PLAYERS_PERMANENTS / OPPONENTS_PERMANENTS
with `matchAtUntap` (`restrictNextUntapStep`). Sets fixed at resolution: `SkipNextUntapEffect` without `matchAtUntap` and its
ALL_CREATURES (a skip per permanent), and `CantAttackThisTurnEffect` ALL_CREATURES, one effect per creature: a "can't attack"
is a rules change and should reach creatures entering later (611.2c), so 6.2e checks its cards' Oracle text and adds a test.
TARGET / SELF / TRIGGERING / ENCHANTED name one permanent. Static (the resolver's static entry point, 6.2b): `DoesntUntapEffect`
(`self()` / `enchanted()`, read by `UntapStepService` and `UntapPreventionSupport`; its TRIGGERING form is the one-shot
`DoesntUntapEffectHandler`), `DoesntUntapWithCounterEffect` (already a subject) and
`UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect` (CONTROLLED, `UntapStepService`). Other readers:
`ActivatedAbilityExecutionService` (SELF ×3), `TargetPolarityClassifier` (TARGET, TARGET_PLAYERS_PERMANENTS,
TARGET_CONTROLLERS_OTHER_CREATURES; 6.2d), `VentureIntoDungeonEffectHandler` (a TARGET payload).

**Sub-run plan**: 6.2b `staticfx` + SES on the static entry point, and the static `TapUntapScope` readers; 6.2c LSS
(`scopeTargets`) + `GameQueryService` and the "other" classifiers (a new pass input goes into `computeBoardFingerprint`);
6.2d the AI; 6.2e the one-shot `TapUntapScope` restriction handlers (a rule keeps a player-scoped predicate rule, a set
resolves through the resolver); 6.3 the 85 effects take a `PermanentSubject`, cards rewritten by script per verb (list
above), `GrantScope` and `TapUntapScope` deleted, after the four missing variants (shares-a-colour-with-target,
`triggeringSpellTargets()`, `triggeringAttackers()`, `bandmatesOf()`) and step 7's `createdThisResolution()` exist, else those
constants stay; 6.4 the rule-4 tests.

**6.2a done.** The one-shot readers find their permanents with `SubjectResolutionService.permanents(gameData, entry, effect,
subject)` and `scope.subject()` (narrowed by the effect's filter where the old code filtered the set): GrantKeyword,
GrantActivatedAbility, RemoveKeyword, LosesAllAbilities, RemoveAllProtectionUEOT, GrantChosenKeyword, GrantChosenLandwalk,
GrantEffectToTarget, GrantAdditionalBlockToTargetUEOT, GrantProtectionChoiceUEOT, GrantProtectionFromColorUEOT,
SetBasePowerToughness, SetBasePowerToughnessToAmount, GrantColorUEOT, GrantColorEffectResolution, GrantSubtypeEffectResolution,
GrantSubtypeUEOT, GrantSupertypeUEOT, TemporaryCreatureTypes, TargetCreatureBecomesSubtypeUEOT,
TargetCreatureBecomesChosenSubtypeUEOT, SetCardTypesUEOT, SetCardTypes, RemoveCardTypeFromTargetPermanent,
LoseAllCreatureTypes, BoostAllCreaturesOfChosenSubtype, AnimationSupport (every scope-reading method), BecomeSaddledUEOT,
SetCombatRequirementThisTurn, TargetCreatureDealsDamageToController, CantBecomeUntapped, Suspect, Unsuspect (33 files).
Each still makes one floating effect per permanent found as it resolves: the set is locked (CR 611.2c), as before. No card
file changed, no record deleted, ratchet unchanged (no ledger rows: nothing was folded).

**Kept on purpose.** (1) GrantActivatedAbility OWN_CREATURES + WHILE_SOURCE_REMAINS still installs a predicate-scoped
floating grant that reaches creatures entering later. It grants an ability, a characteristic, so by 611.2c its set should be
locked: 6.4 checks its cards' text and tests it. (2) The SELF fallback to the entry's target, only in GrantKeyword and
GrantEffectToTarget and only on an entry with no source permanent: Terminal Velocity's "put an artifact or creature card onto
the battlefield ... it gains haste" (`PutCardToBattlefieldThenEffect`'s "then" on a spell) finds the entered permanent there,
written by `CardChoiceHandlerService`. It is written as `target()`; 6.3/step 7 give it a "the permanent put onto the
battlefield" step-result subject and the fallback goes. Temporary probes (thrown where the old lookup would have named
another permanent: the SELF fallback, an activation-captured ENCHANTED_PERMANENT target (the Genju cycle), AnimationSupport's
target-before-self reads, `getTargetId()` outside the effect's target group, the flat target list) fired only for Terminal
Velocity over the 1,446 test classes below, and were removed.

**Behaviour differences** (the kinds 5d and 5i accepted): sets in APNAP order (log and creation order only); the resolver's
fuller filter context; `target()` / `targetPlayer()` read the effect's target group before `getTargetId()`; `attached()` is
live, then last-known (CR 608.2h), so the Genju and GrantSupertype ENCHANTED_PERMANENT read the host as the ability resolves
rather than the activation-captured one (no test differs: the probes never fired); SetCombatRequirement's ENCHANTED_CREATURE
uses the last-known host once the Aura has left (it did nothing); TemporaryCreatureTypes no longer adds a floating effect to a
target that left; LosesAllAbilities' TARGET_PLAYERS_CREATURES logs "0 creature(s)" when there is no player target.

**Tests**: 1,502 suites, 12,183 test cases, all pass, after the probes were removed: every test class of every card of the
33 records the converted handlers resolve (`AnimateReturnedPermanentIfSubtype`, `SetTargetBasePowerToughnessFromSource` and
`SetBasePowerToughnessOfCreaturesThatDamagedSource` included), except `GrantKeywordEffect`, `AnimatePermanentsEffect` and
`GrantActivatedAbilityEffect`: those three ran a 50-per-class sample plus every card that builds one of them outside a static
ability with a constant other than TARGET or SELF (634 cards, the Genju cycle among them); the other
`PutCardToBattlefieldThenEffect` cards; the application `layers` package (`SevenLayerTest`, `LayerDependencyTest`,
`LayeredBoardCacheTest`, `FloatingEffectLifecycleTest`, `ModifierExplanationTest`, `StaticSourceCacheTest`,
`LayerPassBenchmarkTest`), `SubjectResolutionServiceTest`, `LayerClassifierTest`, `EffectCountRatchetTest`,
`LibraryChoiceHandlerServiceTest`, `FlickerEffectHandlerTest`; application, AI and testFixtures test sources compile. Ask for a
full card-test run (the plan asks for one after sub-step 2: run it after 6.2e, or now).


### Step 6, run 6.2b (2026-11-20)

**Done.** Every `staticfx` handler and `StaticEffectSupport` (SES) finds its permanents through the resolver's static
entry point asked about one permanent: `SubjectResolutionService.names(StaticEffectContext, subject, filter,
StaticReads)`, called as `support.names(context, scope.subject(), filter)`. It judges the subject's structure itself
(`source()` by id, `attached()` by the source's `attachedTo`, `controlledBy` players from the context's
`targetOnSameBattlefield` / the Curse's player, the set predicate's not-the-source / paired / tapped parts) and hands
creature, land and the effect's filter to `StaticReads`, which SES implements with its old recursion-safe reads
(`isEffectivelyCreature`, `matchesStaticLeaf`, `matchesStaticFilter`). No lookup, no allocation per call, nothing new
read by the pass, so `computeBoardFingerprint` is unchanged; the set is re-evaluated by every pass (CR 611.3a).
`matchesCreatureScope`, `matchesLandScope` and `selfInScope` are gone. `matchesAnimateLand` (the AllLandsAreCreatures
scan) uses the context-free overload `names(GameData, source, controllerId, onSourcesBattlefield, target, subject,
reads)`. `GrantScope.subject()` now returns one cached instance per constant, its set predicates ordered cheap first
(not-the-source, tapped, then creature / land) so the per-permanent cost and the dependency footprint stay as they
were. No stack-entry or no-variant constant reached a static handler in the probed tests (the resolver would throw).

**Per-verb readings 6.3 must apply when it rewrites cards** (kept from the old handlers, named in SES):
`withAllPermanentsIncludingSource`: `ALL_PERMANENTS` includes the source for BecomeColorless, GrantCardType, GrantColor,
GrantSubtype, static AnimatePermanents, GrantEffect and GrantActivatedAbility (Thran Lens and Ghostflame Sliver are
colourless themselves; GrantKeyword, GrantTriggeredAbility and SetCardTypes exclude it, as `subject()` does).
`withOwnPermanentsAsOthers`: `OWN_PERMANENTS` is "other permanents you control" for GrantKeyword (both handlers) and
GrantActivatedAbilitySelf (the activated-ability grant reaches its source through the non-self handler: the
`LayerSystemService` / `GameQueryService` dispatch exception for GrantActivatedAbility OWN_PERMANENTS, 6.2c moves both
together). The conditional self path (`applySelfOnlyConditionalStaticEffect`) reads `ALL_CREATURES` including the
source. Non-self handlers whose old code returned nothing for `SELF` use `namesBeyondSource` (a `source()` subject is the
self handler's), because the assembly hands non-self handlers the source itself for granted static effects (Sedge
Sliver's granted "this creature gets +1/+1" would otherwise count twice). The self handlers that handle only
`source()` (SetCardTypesSelf, GrantAllCreatureTypesToSelf, GrantChosenSubtypeToSelf) and the graveyard keyword scan
test `subject() instanceof Source`; GrantTriggeredAbility's attachment check is `instanceof Attached`.

**Subject change.** `OWN_UNTAPPED_CREATURES` is now `controlledBy(you(), ¬tapped ∧ ¬source ∧ creature)`, like
`OWN_TAPPED_CREATURES`: Augusta, Dean of Order (STX 155) says "Other untapped creatures you control get +0/+1"; the old
self handler never gave it to her. Its other users (Sword of the Paruns, Dragonologist) can't be their own target.

**Behaviour changes** (found by a temporary probe that compared old and new per call over the tests below, removed):
the self-including sets now check that the source is a creature / matches the filter where the old self handlers did
not. Cid, Timeless Artificer (FIN 216, a Human Artificer: "Artifact creatures and Heroes you control get +1/+1 ...")
no longer boosts itself (`CidTimelessArtificerTest.doesNotBoostItselfWhileNotArtifactOrHero`, fails on the old
code); the noncreature sources Knowledge Is Power, Commander's Insignia (DynamicStaticBoost), Sorcerer Class
(conditional activated-ability grant) and Common Cause (conditional boost) no longer give "creatures you control" /
"nonartifact creatures" effects to themselves. AllLandsAreCreatures reads land-ness from the layered state, not the
printed card: a land a later effect makes a non-land (Soul Sculptor, CR 613.7 in layer 4) is no longer animated
(`LivingPlaneTest.landThatStopsBeingALandIsNoLongerAnimated`).

**Left for 6.2e.** The static `TapUntapScope` readers (`UntapStepService`'s self / enchanted / controlled cross-player
untap and counter-lock reads, `UntapPreventionSupport.hasActiveSelfDoesntUntap`) dispatch by kind into three untap
routines with their own logs, not by membership; moving them is a restructure of `UntapStepService`, and
`TapUntapScope` has no `subject()` yet. 6.2c note: `GrantScope.subject()` instances are now shared per constant; the
CR 613.6 layer-4 verdict memo must keep keying on an effect's own filter, never on a subject's set predicate.

**Tests**: 1,968 test classes (2,094 suites with the nested ones, 17,749 test cases), all pass (1 skipped): the application `layers` package, `SubjectResolutionServiceTest` (new
`StaticMembership`: source / attached, sets with players, predicate and filter, tapped, the Curse's player, agreement
with the list form for every static `GrantScope` subject on a mixed board, stack subjects rejected),
`LayerClassifierTest`, `EffectCountRatchetTest`, `GameQueryServiceTest`, `PredicateEvaluationServiceTest`; every test
class of the cards of every scoped static record with at most 112 test classes, a 160-per-class sample of the rest
(GrantKeyword, ConditionalEffect, StaticBoost, AnimatePermanents, GrantActivatedAbility, GrantTriggeredAbility,
SetBasePowerToughness, ProtectionFromColors), and the cards above. Application, AI and testFixtures test sources and the
touched card-test modules compile. Ratchet unchanged (no record or handler deleted; no ledger rows: nothing folded).
Ask for the full card-test run the plan asks for after sub-step 2 (6.2b is part of it).

### Step 6, run 6.2c (2026-11-20)

**Done.** `LayerSystemService` (LSS), `GameQueryService` (GQS) and the "other" classifiers read `scope.subject()`, not
constants. Three entry forms:
- *Membership of one permanent*: the resolver's static `names(...)`, with a new overload
  `names(GameData, source, sourceControllerId, targetControllerId, target, subject, StaticReads)`: it takes whose
  battlefield the target is on or is entering (an entering creature, an enchanted player's set judged by controller,
  no battlefield search), and a `null` source for an emblem (`source()` / `attached()` name nothing). Reads:
  LSS `TypeLayerReads` (creature / land from the states of the board this thread's pass is building, incl. trial
  boards; layer 3/4 counts awakened lands, the 7b emblem setter does not, as before), GQS `assemblyReads` (emblems in
  the assembly, the finished board), GQS `layeredQueryReads` behind the new public `GameQueryService.staticSubjectNames`
  (continuous queries: counter locks, lethal-damage, toughness-assignment, mana-colour permissions), and
  `StaticReads.knownCreature()` / `knownLand()` for a caller that already judged the type (riot on an entering
  creature, Blood Sun on an entering land, attack requirements, `matchesAnimateLand`). All singletons: nothing is
  allocated per call beyond `scopeTargets`' existing result list.
- *"Is it the source itself"*: `subject() instanceof Source` (SELF checks in LSS, `LayerClassifier`, GQS,
  `TriggerIndex`, `PermanentCopierService`, `TokenCreationReplacementSupport`, the EachOpponent token-copy handler);
  floating payload markers test `Target` / `Source`; "attached" tests `Attached` (`PredicateEvaluationService`,
  `AuraAttachmentService`, which now also exempts an `ENCHANTED_PERMANENT` / `EQUIPPED_CREATURE` protection form: no
  card has one on an Aura).
- *Shape only*: AnimatePermanents' "set" branch is `instanceof ControlledBy` (LSS L4 and 7b, `LayerClassifier`); the
  global combat-damage assignment (Doran) is "every player's set" (`namesEveryPlayersPermanents`), since that rule
  reaches its own source.

`scopeTargets` takes a `PermanentSubject`: `source()` / `attached()` judge their one candidate, a set walks the slots;
the effect's own filter is still `matchesL4Filter`'s, memoized per filter instance (CR 613.6), never keyed on a
subject's shared set predicate. The fingerprint is unchanged (nothing new is read). CR 613 placement, dependency
order and timestamps are untouched.

**Dispatch exception moved.** GrantActivatedAbility OWN_PERMANENTS no longer hands the source to the non-self
handler (LSS `applyStaticInstanceViaHandlers`, GQS assembly); `GrantActivatedAbilitySelfEffectHandler` reads the plain
subject (`controlledBy(you())` names the source), so `withOwnPermanentsAsOthers` is GrantKeyword's alone.

**Constant reads left** (6.3 must give them a subject form): GQS `hasAttachmentRestriction`'s creature check
(`ENCHANTED_PERMANENT` vs the creature forms, as 6.1 planned); the attack requirements and the emblem / counter-lock
readers restrict to a `ControlledBy` with a players kind (`You`, `EnchantedPlayer`) where the old code took only those
constants. GQS reads none of the four no-variant constants. A floating payload's `scope().subject()` is now asked in
L4 (changeling) and L6 (GrantEffect / GrantNonAbility markers); no card makes such a payload with a no-variant
constant (checked by grep), but 6.3 must keep it so.

**Behaviour**: identical. A temporary probe compared old and new per call at every converted site (and that the
reads' board is the scope's board) over the tests below, never fired, and was removed.

**Tests** (after the probe was removed): application `layers` package, `GameQueryServiceTest`,
`SubjectResolutionServiceTest` (new `StaticMembershipByBattlefield`: entering creature, enchanted player by
controller, no source, known characteristics), `LayerClassifierTest`, `PredicateEvaluationServiceTest`,
`BattlefieldPlacementServiceTest`, `UntapStepServiceTest`, `AuraAttachmentServiceTest`, `AttackLegalityServiceTest`,
`EffectCountRatchetTest`; every card test of the small L3/L4 and query records, a 40-per-class sample of
AnimatePermanents, SetBasePowerToughness, GrantActivatedAbility, MustAttack, ProtectionFromColors,
GrantTriggeredAbility, StaticBoost, DynamicStaticBoost, GrantKeyword, plus every card with a static
GrantActivatedAbility OWN_PERMANENTS, an emblem, a static AnimatePermanents set, riot, a scoped MustAttack, a changeling
grant, SELF_AND_PAIRED, ENCHANTED_PLAYER_CREATURES or OWN_(UN)TAPPED_CREATURES: 1,065 suites, 9,161 test cases (1 skipped), all pass. Hand-built test wiring sets
the resolver on LSS and GQS. Ratchet unchanged; no ledger rows (nothing folded).

### Step 6, run 6.2d (2026-11-20)

**Done.** The AI's `GrantScope` readers ask structural questions of the subject through one new AI helper,
`GrantSubjectShapes` (`magical-vibes-ai`): `subjectOf(scope)` (`null` for the four no-variant constants, via the new
`GrantScope.hasSubject()`, so no reader throws on Valor's Reach or Tend to the Kiln), `namesTarget` (`Target`),
`namesAttached` (`Attached`), `namesCreaturesYouControl` (`ControlledBy` with `You` and a predicate that is creature, or
creature and not-the-source, nothing else), `namesEachPlayersOtherCreatures` (`Each(ALL)`, creature and not-the-source),
`namesAnthemSet` (either), `namesAttachedCreature(scope)` and `grantsToItsTarget(effect)` (the effect's `targetSpec()`
admits a permanent: only a `target()` subject declares one, and it also covers TARGET_AND_SHARING_CREATURES, which has no
variant). Readers moved: `BoardEvaluator.lordBonus` / `countBuffedCreatures`, `SpellEvaluator` (equipment-with-evasion,
anthem-on-wide-board, `evaluateAura`), `HardAiDecisionEngine` (3 "creatures you control" pump checks), `AiTargetSelector`
(Aura beneficial check, ability beneficial grant), `GameSimulator` (Aura candidates), `TargetPolarityClassifier`
(keyword grant, Suspect). `countBuffedCreatures` now counts with `GameQueryService.staticSubjectNames` (the engine's
static membership check) plus the effect's own filter, as before.

**How parity was checked.** (1) A scratch census test drove every registered card class (31,682) through the old
constant gate and the new structural gate of every site, with the card's Aura / Equipment status and the effect's
location (static for the scorers, activated for the ability selector, spell / ETB nested for the classifier). (2) The
6.2b probe: each converted site computed old and new side by side and logged differences while the whole AI test task
ran. Both are removed. Differences found and how they were settled:
- TARGETS (Myojin of Towering Might, Swashbuckler Extraordinaire, the choose-different-powers records) and EQUIPPED_CREATURE
  / ENCHANTED_PERMANENT grants are `target()` / `attached()` too: a plain `instanceof` would have widened the ability
  selector and the classifier. `grantsToItsTarget` (declared permanent target) and `namesAttachedCreature` match the old
  answer for every card.
- ENCHANTED_PERMANENT vs ENCHANTED_CREATURE: the subject has no creature-only form, so `namesAttachedCreature` still
  names `ENCHANTED_PERMANENT` (`GrantSubjectShapes`:49), as GQS `hasAttachmentRestriction` does. Without it 23 Auras
  (Zendikons, Indestructibility, Silken Strength, Clawing Torment, ...) would become "beneficial creature Auras".
- Tidal Flats: `GrantKeywordToBlockingCreaturesUnlessAttackerPaysEffect` reports scope TARGET but declares no target;
  `grantsToItsTarget` is false where the old check was true. Unobservable: the ability can target nothing, so
  `canTargetPermanent` / `canTargetPlayer` are false and the flag is never read.
- **One scoring change, old AI inconsistent with the engine**: `lordBonus` counted the lord itself for ALL_CREATURES
  ("other Goblins get +1/+1": Goblin King, Lord of Atlantis, Zombie Master, ... 37 cards, planes included), which the
  engine never boosts (the subject excludes the source). The count now asks the engine.
  `BoardEvaluatorTest.otherCreaturesLordDoesNotCountItself` fails on the old code (the probe fired there, and only there).

**Constant reads left in main code** (scratch grep of `==`/`!=`/`case` on a scope and `GrantScope.X.subject()`,
2026-11-20; constructions of a `GrantScope.TARGET` payload are not listed). Networking, webservice, websocket,
application main: none.
- AI: `GrantSubjectShapes`:49 (ENCHANTED_PERMANENT, above). Not `GrantScope` but in the AI and due before 6.3 deletes
  `TapUntapScope`: `TargetPolarityClassifier`:315, 318, 324-326 and `SpellEvaluator`:1106-1108 (`switch` on
  `CantBlockThisTurnEffect.scope()`), for 6.2e once `TapUntapScope` has a subject.
- Engine: GQS `GameQueryService`:4871 (ENCHANTED_PERMANENT creature check); `StaticEffectSupport`:119, 127, 301-302 (the
  per-verb including-source / other readings); the one-shot handlers' dispatch and per-verb readings, which 6.3 turns
  into the cards' subjects: `GrantKeywordEffectHandler` 57-58, 83, 86, 105, 124, 132, 138, 158, 163-168, 196-201, 221,
  225, 248-257; `GrantActivatedAbilityEffectHandler` 47, 85-107, 134-136; `GrantColorUntilEndOfTurnEffectHandler` 40, 55,
  70, 74, 83, 95, 112; `AnimationSupport` 340, 389, 421, 442, 490, 527; `AnimatePermanentsEffectHandler` 33, 44-47;
  `LosesAllAbilitiesEffectHandler` 39, 41, 53-55, 68; `SetBasePowerToughnessEffectHandler` 40, 56, 70, 90-91, 109;
  `SetBasePowerToughnessToAmountEffectHandler` 34, 45; `TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffectHandler`
  49-81; `TargetCreatureBecomesSubtypeUntilEndOfTurnEffectHandler` 38, 54, 58, 68; `GrantSubtypeUntilEndOfTurnEffectHandler`
  37, 45, 47, 51, 69; `GrantSubtypeEffectResolutionHandler` 37-38, 43; `TemporaryCreatureTypesEffectHandler` 34-39;
  `LoseAllCreatureTypesEffectHandler` 36, 50; `RemoveKeywordEffectHandler` 43-47; `RemoveAllProtectionUntilEndOfTurnEffectHandler`
  46, 48, 53; `RemoveCardTypeFromTargetPermanentEffectHandler` 35; `SetCombatRequirementThisTurnEffectHandler` 48, 56, 128;
  `CantBecomeUntappedEffectHandler` 41, 49; `BecomeSaddledUntilEndOfTurnEffectHandler` 36-37;
  `TargetCreatureDealsDamageToControllerEffectHandler` 48, 76; `GrantEffectToTargetEffectHandler` 48-69;
  `SetCardTypesUntilEndOfTurnEffectHandler` 31; `SetCardTypesEffectHandler` 33; `GrantSupertypeUntilEndOfTurnEffectHandler`
  35-36; `GrantProtectionFromColorUntilEndOfTurnEffectHandler` 38, 40, 46; `GrantProtectionChoiceUntilEndOfTurnEffectHandler`
  60-63; `GrantColorEffectResolutionHandler` 31; `GrantChosenLandwalkEffectHandler` 34-39; `GrantChosenKeywordEffectHandler`
  31; `GrantAdditionalBlockToTargetUntilEndOfTurnEffectHandler` 36; `BoostAllCreaturesOfChosenSubtypeEffectHandler` 76-78.
- Domain (the records' own `targetSpec()` / validation / `isAttached`-style accessors, rewritten with the records in
  6.3): `Card`:2712; `GrantKeywordEffect` 61-63; `GrantActivatedAbilityEffect` 34, 43; `AnimatePermanentsEffect` 176-178,
  190; `SetCombatRequirementThisTurnEffect` 64-85; `SetBasePowerToughnessEffect` 108, 111; `GrantProtectionChoiceUntilEndOfTurnEffect`
  36-42, 97-98; `TargetCreatureDealsDamageToControllerEffect` 26, 34, 49; `RemoveKeywordEffect` 31-37; `LoseAllCreatureTypesEffect`
  29, 32; `LosesAllAbilitiesEffect` 40-42; `GrantSubtypeUntilEndOfTurnEffect` 32-34; `GrantColorUntilEndOfTurnEffect` 47, 50;
  `TargetCreatureBecomesSubtypeUntilEndOfTurnEffect` 47-48; `TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect` 35;
  `SuspectEffect` 9-10; `UnsuspectEffect` 9-10; `SetCardTypesUntilEndOfTurnEffect` 18-19; `SetCardTypesEffect` 21;
  `RemoveCardTypeFromTargetPermanentEffect` 19; `RemoveAllProtectionUntilEndOfTurnEffect` 18-19; `LosesAllNonManaAbilitiesEffect`
  21; `GrantSupertypeUntilEndOfTurnEffect` 19-20; `GrantSubtypeEffect` 32; `GrantRandomKeywordEffect` 23, 41;
  `GrantProtectionFromColorUntilEndOfTurnEffect` 49-50; `GrantEffectToTargetEffect` 62; `GrantColorEffect` 31;
  `GrantChosenLandwalkEffect` 15, 32; `GrantChosenKeywordEffect` 34, 44; `GrantAllCreatureTypesToOwnCreaturesEffect` 28;
  `GrantAdditionalBlockToTargetUntilEndOfTurnEffect` 21; `BecomeSaddledUntilEndOfTurnEffect` 9-10;
  `CreateTokenCopyOfTargetPermanentEffect` 85; `AnimateReturnedPermanentsEffect` 15; `AnimateReturnedPermanentIfNotCreatureEffect` 14.
  The grep misses reads split oddly across lines; 6.3 deletes the enum and lets the compiler find the rest.

6.3 note: when the records expose `subject()`, the AI callers change `GrantSubjectShapes.subjectOf(x.scope())` to
`x.subject()`; `namesAttachedCreature` needs the creature-only attached form 6.3 gives ENCHANTED_CREATURE /
EQUIPPED_CREATURE, and `hasSubject()` goes with the enum.

**Tests**: the whole `magical-vibes-ai` test task (3 min), after the probes were removed: 115 suites, 1,649 test cases
(4 skipped), all pass (it includes `HardAiDecisionEngineTest`, `SpellEvaluatorTest`, `BoardEvaluatorTest`,
`GameSimulatorTest`, `AiTargetSelectorTest`, `TargetPolarityGuardTest`, `InstantCategoryClassifierTest`). Application, AI
and engine testFixtures test sources compile. No card file changed, nothing folded: no ledger rows, ratchet unchanged.

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
