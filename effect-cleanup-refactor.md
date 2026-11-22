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
| 5 | Permanent verbs take a `PermanentSubject` (repeatable) | done (5a–5m; 5d finished by 6.2e; no sibling class unaudited) |
| 6 | `GrantScope` becomes a `PermanentSubject`, static abilities included | done (`TapUntapScope` deleted in 6.2e, `GrantScope` in 6.3, rule-4 tests in 6.4) |
| 7 | Step results: one way for a step to tell the next what happened | done |
| 8 | Split the fused effects (repeatable) | done (runs 1 to 29; closing summary under Findings, step 8 run 29; "counter unless": option B taken by the orchestrator in run 28, pending the human; option C for step 10) |
| 9 | Sweep the single-card tail (repeatable) | open (runs 1 to 17: Exile, Each, Return, Put, Reveal and Choose closed; 2,192 rare records left, Grant next) |
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

### Card subjects

Decided 2026-11-20 (step 8, run 23). A third subject type, `CardSubject` (sealed interface in `model/effect`, factories on
`Subjects`), names cards outside the battlefield. `SubjectResolutionService.cards` / `locatedCards` resolve it with the
resolving-effect entry point to the cards as they are now: each card in the public zone it is expected to be in, nothing
for a card that has left that zone since (a card that moves is a new object, CR 400.7; the rest of the effect can find a
card it moved to a public zone, CR 400.7j). Variants:

| Variant | Meaning |
|---|---|
| `cardsThisWay(StepKind)` | the cards the most recent step of that kind of the same resolution moved: `EXILE` the exiled cards in exile; `DESTROY` / `SACRIFICE` the card each removed permanent became in the zone it went to (`StepOutcome.movedCards`, recorded by the departure results and the destroy / exile handlers); `DISCARD` the discarded cards in the graveyard, or in exile where a replacement put them; `COUNTER_SPELL` (run 27) the countered spell's card in its graveyard or exile; `cardsThisWay(kind, filter)` (step 9 run 3) only those matching the filter as they are now |
| `targetCard(filter, scope)` | the effect's graveyard card target, declared by the effect reading it (added in run 23 for Feldon of the Third Path's copy without exiling) |
| `triggeringCards(filter)` (step 9 run 8) | the trigger event's cards still in the graveyard stay it put them in; `cardsThisWay` also reads `MILL` and `CHOOSE` (a chosen card, in a revealed hand too) |

Readers: `CreateTokenCopyOfCardEffect.of(cards, options)` (the token-copy step), `ReturnCardFromGraveyardEffect.returnAll(true).cards(...)`
(the return step), `MayCastDiscardedCardWithoutPayingManaCostEffect(cards[, filter])` (the "cast it now for free" step),
`CopyImprintedCardAndMayCastCopyEffect.of(cards)` (the may-cast-a-copy step, run 24), `ExileGraveyardCardsEffect.cards(cards)` (the
exile step, run 24), `AllowPlayCardsFromExileEffect` (the permission step, run 27; durations and riders in step 9 run 2) and, inside a card filter, `CardSharesCardTypeWithCardsPredicate(cards)`, which a handler binds through
`SubjectResolutionService.bindCardSubjects` (the seek, run 24).

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
`blocked-other` (note says on what), `dead`, and `step-9` (added in step 8, run 29: split- or absorb-ready
single-family work handed to step 9; the note says what to use).

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
| 5d | Tap and untap | `TapUntapScope` (1,419 cards) | `--family Tap`, `--family Untap` | done (the tap and untap verbs; the six restriction records finished in 6.2e) |
| 5e | Put counters | `PermanentReference` users | `PutCountersOnSelfEffect`, `PutCounterOnTargetPermanentEffect`, `PutCounterOnReferencedPermanentEffect`, `DoublePlusOneCountersOn{TargetCreature,Source,EnchantedCreature}Effect` | done |
| 5f | Remove counters | `CounterRemovalSubject` | `--family RemoveCounter` | done |
| 5g | Damage to permanents | `DamagedPermanentScope` | `DealDamageTo{TargetCreature,Source,TriggeringPermanent,EnchantedCreature,TargetPermanent}Effect` | done (`DealDamageToTargetPermanentEffect` kept: not a subject sibling) |
| 5h | A creature deals damage | none | `*DealsPowerDamageTo*` (7), `*DealsDamageTo*` (5) | done (8 folded into two survivors; 13 left in the ledger) |
| 5i | Boost (one-shot) | `PermanentReference` users | `Boost{Self,TargetCreature,EquippedCreatureUntilEndOfTurn,ReferencedPermanent,TargetPermanent}Effect` | done (3 folded into `BoostSelfEffect`; Hand of Vecna's choice record and `BoostTargetPermanentEffect` kept) |
| 5j | Sacrifice a named permanent | none | `Sacrifice{Self,EnchantedCreature,TargetCreature}Effect` | done (3 folded into `SacrificeSelfEffect`; the delayed and Animate Dead records kept) |
| 5k | Flicker, phase out, remove from combat | `FlickerScope`, `PhaseOutSubject`, `PhaseOutScope` | `Remove{Target,Self,Source}FromCombatEffect` | done (3 enums gone; 4 records folded into `RemoveTargetFromCombatEffect` and `PhaseOutEffect`) |
| 5l | Attach and token copies | none | `Attach*EquipmentTo*` (5), `CreateTokenCopyOf*` (4), `CreateTokenAttachedTo*` (4) | done (9 folded into three survivors; `Copied` enum gone; the defending-player token and `CreateTokenCopyOfSourceEffect` kept) |
| 5m | Whatever `--siblings` still lists | `PermanentReference` | — | done (14 folded into 11 survivors; `PermanentReference` and `Subjects.of` gone; continued 2026-11-22: 28 folded, every group of three or more done; finished 2026-11-22: 31 folded into 30 survivors, the static player and host pairs among them, every pair ledgered) |

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
| 6.2e | the one-shot `TapUntapScope` restriction handlers, and the static ones 6.2b left (`UntapStepService`, `UntapPreventionSupport`) | done (the six records take a `PermanentSubject`, cards rewritten, `TapUntapScope` deleted) |
| 6.3 | records take a `PermanentSubject`, cards rewritten, `GrantScope` deleted (`TapUntapScope` went in 6.2e) | done (every record converted, the four no-variant constants got variants, `GrantScope` deleted) |
| 6.4 | the rule-4 tests | done (tests added where missing; Roar of the Fifth People's chapter II is a static ability the Saga gains; `UntapTargetAndSharingCreatures` folded) |

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

| Run | Takes | Status |
|---|---|---|
| 1 | the `thenEffect` wrappers of one named permanent (sacrifice / exile / return this, the granting Equipment), mill and draw "then", the library-move and tap wrappers | done (6 split, 8 blocked-step-model, 1 unique; mill "when you do" gated, Necrite and Search the City gated; the Destroy / Exile / Return target wrappers and `DiscardCardThenEffect` need rider subjects first) |
| 2 | the target "then" wrappers: `DestroyTargetPermanentThenEffect`, `ExileTargetPermanentThenEffect`, `ReturnTargetPermanentToHandThenEffect`, `DiscardCardThenEffect` | done (78 cards split: Destroy 54 of 89, Exile 15 of 25, Return 9 of 12; no wrapper emptied, all three kept as blocked-other; `DiscardCardThenEffect` audited, blocked-step-model; `ThenEffectRecipient` stays) |
| 3 | the groups run 2 left on the target wrappers: "its controller may search" (`LibrarySearchPlayer` → `PlayerSubject`), "its controller creates a token" (`CreateTokenEffect` token controller), the layer-sensitive "if it was a …" checks | done (28 cards split: 15 search, 5 token, 8 layered; `LibrarySearchPlayer` deleted; `TargetPermanentMatches` reads a departed target's layered last-known card; no wrapper emptied: Destroy 14, Exile 5, Return 1 left; `ThenEffectRecipient` stays) |
| 4 | `--unaudited --family Deal`, the damage-then families ("deals N damage … If a [creature] is dealt damage this way, …") | done (12 split, 4 blocked-other, 0 blocked-step-model, 0 unique; `thisWay(kind, filter)` and `HappenedThisWay.toA`; Smite the Deathless and The War Doctor fixed by Oracle text) |
| 5 | `--unaudited --family Deal`, the rest: the "And" simultaneous sets, "Unless" and perpetual riders | done (3 split, 2 absorbed, 4 blocked-step-model, 6 blocked-other, 2 unique; 7 of `DealDamageToTargetAndTheirCreaturesEffect`'s 8 cards split; `otherCreaturesSharing` subject; next-damage shields cover a whole simultaneous event; Goblin Chainwhirler and Sarkhan's Unsealing made one damage event by Oracle text) |
| 6 | `--unaudited --family Sacrifice` (the family with the most first halves on step 7's earlier-step list: `SacrificeSelfEffect` and `SacrificePermanentsEffect`) | done (12 split, 8 blocked-other, 0 blocked-step-model, 0 blocked-player-loop, 0 unique; 17 cards rewritten; the blocked ones need the chosen sacrifice's last-known stats from `SacrificePermanentsEffect`, a granted ability's granter, or a delayed trigger that names a permanent) |
| 7 | the chosen sacrifice's last-known stats: `SacrificePermanentsEffect` records `DepartureResult`s; `SacrificePermanentThenEffect`'s cards, Disciple of Bolas, Rupture, the Tribute to Hunger record | done (3 records split, 2 amounts absorbed into `AmountThisWay`; 111 of `SacrificePermanentThenEffect`'s 121 cards split, 10 kept; 10 "If you do" cards fixed from reflexive by Oracle text) |
| 8 | `--unaudited --family Exile` restricted to `Then` / `And` / `IfYouDo` / `Unless` names: the permanent exiles whose first half is `ExileTargetPermanentEffect` | done (6 split, 2 absorbed, 2 survivors, 10 blocked-other, 2 blocked-step-model, 3 unique; 9 cards rewritten; `ManifestTopCardEffect` and `CloakTopCardOfControllerLibraryEffect` take a `PlayerSubject`) |
| 9 | run 8's prerequisite: graveyard and library exiles record an EXILE step; the fused Exile records that unblocks (graveyard target, source card, top of library) and the Swords family's one-target cards | done (11 split, 1 amount absorbed, 7 survivors, 2 blocked-other, 4 blocked-step-model, 1 unique; 37 cards rewritten; `HappenedThisWay.toACard` and `AmountThisWay.exiled*`) |
| 10 | run 9's next prerequisites: a count-honouring controller-graveyard target (Quintorius, Loremaster; Nyla), a `PlayerSubject` for whose library the two library-exile primitives take, and the "that player / target opponent exiles the top card, you may play it" records that unblocks | done (5 split, 5 absorbed, 3 survivors, 1 blocked-other, item 3 not started; 16 cards rewritten; `fromLibraryOf`, `mayOnlyCast`, `withAnyManaType`) |
| 11 | run 10's leftovers: permission durations on the library-exile primitive and the records they absorb; the recording choose-a-card graveyard exile and the "you may exile a card from your graveyard. If you do" records | done (4 split, 12 absorbed, 1 amount absorbed, 4 survivors; 39 card files rewritten; `until(Duration)`, `exiledFaceDown`, `payingLifeEqualToManaValue`, a count on `ExileTopCardOfOwnLibraryEffect`, `fromOwnGraveyard`) |
| 12 | run 11's leftovers: `ExileTopCardsMayPlayUntilNextTurnEffect` and Curse of Hospitality's record (a who-may-play subject), `ExileTopCardsOfDefendingPlayerLibraryEffect`, `ExileTopCardMayPlayWhileExiledEffect`, `ExileNCardsFromGraveyardThenEffect` ("exactly N or none") | done (4 absorbed, 1 split, 1 unique, 2 survivors; 72 card files rewritten; `playableBy(PlayerSubject)`, `playableOnlyIf(Condition)`; Blightwing Bandit and Skywarp Skaab fixed by Oracle text) |
| 13 | run 12's leftovers: the choose-one, cast-matching, random-opponent, token and Hidetsugu library records; Heated Argument's graveyard record; Raven Guild Master and Sire of Stagnation's player | done (6 absorbed, 2 split, 2 blocked-other, 1 unique; 32 card files rewritten; `mayOnlyCast(filter)`, `upTo(n)`, `choosingOne()`; Narset's Adventure fixed by a ruling; the four multi-card graveyard "and" records not reached) |
| 14 | run 13's graveyard "and" records: the imprint and track-with-source target exiles, then the token-copy and may-cast-a-copy records | done (2 absorbed into `ExileGraveyardCardsEffect`, `imprintingOnSource()`; 16 card files rewritten; stopped at `ExileTargetCardFromGraveyardAndCreateTokenCopyEffect`: no token-copy step copies a card an EXILE step recorded, a design decision; the may-cast-a-copy record not started) |
| 15 | the Return family (fused names or a nested `CardEffect`; Exile left for the card-subject decision): the chosen return records a RETURN step, then its "then" records, the target-return "then" records, the attached bounce | done (6 split, 1 absorbed, 4 survivors, 1 unique, 9 blocked-other; 23 card files rewritten; `choosingOneOf`, `ReturnToHandEffect.targetCreatureAndAttached`, `toA(RETURN)`; Paradoxical Outcome kept on its record; the batch bounce now looks back, CR 603.10a) |
| 16 | the Destroy family: the mana-value conditional (Perilous Voyage pattern), the same-name / same-mana-value / shared-colour sets (`otherCreaturesSharing` widened to any permanent), the block-trigger `allOf` pairs, the rider splits | done (3 split, 6 absorbed, 1 survivor, 1 blocked-player-loop, 10 blocked-other; 23 card files rewritten, 16 of them by hand; `otherPermanentsSharing(permanent, quality, filter)`, `SharedQuality.MANA_VALUE`; a permanent with all card names now shares every name; `DestroyAllPermanentsEffect`'s rider not started) |
| 17 | `DestroyAllPermanentsEffect`'s rider: the wipe records a DESTROY step, the plain-count, count-scope and per-controller riders become later steps; Voodoo Doll if budget remains | done (1 split, 1 absorbed, 5 survivors, 2 blocked-player-loop, 3 blocked-other; 27 of the 30 rider cards split, 33 card files rewritten; `AmountThisWay.died` / `matching` / `controlledByYou`; the rider stays for From the Ashes, Seeds of Innocence, Stench of Evil; Voodoo Doll tried and kept) |
| 18 | run 17's next: `DestroyEachTargetPermanentEffect` records a DESTROY step and its 9 rider cards split; then the draw-then-put-from-hand family (first half `DrawCardEffect`) | done (3 split, 1 absorbed, 1 amount absorbed, 4 survivors, 1 unique, 1 blocked-player-loop, 3 blocked-other; 27 card files rewritten; `AmountThisWay.destroyedManaValue()` / `.includingUnremoved()`, `TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect` takes a placement and a `PlayerSubject`; three stamped `StackEntry` fields deleted; Seeds of Innocence not split) |
| 19 | run 18's next: `SacrificePermanentsEffect` gains an "any number" choice recording the SACRIFICE step and the two any-number records split; the sacrifice-or-discard and manifest-then-counters records; then the next fused families whose first half records a step | done (8 split, 3 survivors, 4 blocked-other; 24 card files rewritten; `SacrificePermanentsEffect.anyNumber`, `StepKind.MANIFEST`, `ChooseOneAtResolutionEffect` drops impossible sacrifice / discard options; `DiscardCardAndBoostSelfEffect` split with Furyblade Vampire) |
| 20 | run 19's next: `MayEffectHandler`'s can't-sacrifice check reads a sequence's leading sacrifice with its count; `SacrificePermanentsThenEffect` split; `SacrificeAnyNumberOfPermanentsEffect` absorbed; `DiscardFollowUp`'s `boost*` dropped; then the Sacrifice family's "unless" / "or else" records and the remove-a-counter "if you do" record | done (4 split, 1 absorbed, 3 survivors, 1 unique, 5 blocked-other; 31 card files rewritten; `SacrificePermanentsEffect.upTo`, `HappenedThisWay.removedCounters()`, `RemoveCounterFromSourceEffect` records REMOVE_COUNTERS; a reflexive ability's dynamic target count reads its carried step results; `AmountThisWay` totals floor once, CR 107.1b) |
| 21 | run 20's next: the rest of the counter family (the source removal's "then" record, vanishing's upkeep record, the put-counters reflexive records, the threshold transform); `DiscardEffect`'s card filter not reached | done (3 split, 1 absorbed, 4 survivors, 1 blocked-other; 33 card files rewritten; `RemoveCounterFromSourceEffect` takes `CounterType.ANY`, `GoadTargetCreatureUntilNextTurnEffect.ofCreaturesGivenCountersThisWay()`, a sequence's own target is chosen beside a reflexive step; Verazol and Spectacular Showdown fixed by Oracle text, vanishing's sacrifice left to vanishing, CR 702.63a) |
| 22 | run 21's next: a card filter on `DiscardEffect` and a filter-aware can't-discard check; `SacrificeUnlessDiscardCardTypeEffect` and the "discard a … If you do" `DiscardCardThenEffect` cards; Spectacular Showdown's overload record; the threshold transform if it fits | done (1 split, 2 absorbed, 4 survivors, `DiscardCardThenEffect` blocked-other with 41 of its 70 cards split; 61 card files rewritten; `DiscardEffect.matching`, `HappenedThisWay.toACard(DISCARD, …)`, `DiscardCardThenEffect.withDiscardedManaValueAsX`; Great Desert Hellion fixed by Oracle text; the threshold transform not reached) |
| 23 | the card subject (human decision 2026-11-20): `CardSubject`, `cardsThisWay(kind)` and the records blocked on it (token copy of an exiled card, destroy-then-return, Hidetsugu and Kairi, the discarded-card riders) | done (3 split, 1 absorbed, 7 survivors, 3 blocked-other; 20 card files rewritten; `CardSubject.cardsThisWay` / `targetCard`, `StepOutcome.movedCards`, `PermanentSubject.Except`, `CardPutIntoGraveyardThisTurnPredicate`; Mordor on the March and Sorin fixed by Oracle text, Gyrus by a ruling; the may-cast-a-copy record not split) |
| 24 | run 23's next: the trigger's event value in the graveyard exile's target filter, the may-cast-a-copy step and `ExileTargetCardFromGraveyardAndMayCastCopyEffect`'s 14 cards; an exile step and a seek filter over a card subject (Leonardo da Vinci, Anguished Recollection) | done (2 split, 1 absorbed, 3 survivors; 16 card files rewritten; `CopyImprintedCardAndMayCastCopyEffect.of(cards)` with `payingItsCosts` / `losingLifeOnCast` / `ifCastThen`, `ExileGraveyardCardsEffect.cards` / `oneTargetFromOpponentGraveyard`, `CardSharesCardTypeWithCardsPredicate`; no marker record kept; Hour of Eternity, The Ruinous Powers and the threshold transform not reached) |
| 25 | run 24's next: the subject-path cast-copy restriction check (a rules bug), the X-target graveyard exile (Hour of Eternity, Midnight Ritual, Foggy Swamp Visions, Necromancer's Covenant), the threshold transform, The Ruinous Powers | done (2 split, 4 survivors, 1 blocked-other; 11 card files rewritten; `ExileFreeCastQueueSupport.castableFromExile` for both copy accept paths, `ExileCardsFromGraveyardEffect.exactlyXFromOwnGraveyard` / `exactlyXFromGraveyards` recording EXILE, `GraveyardCardChoosingEffect.choosesFromControllersGraveyardOnly`; Mizzix's Mastery fixed by a ruling, the threshold transforms made transform events; The Ruinous Powers kept; item 4 audited, not started) |
| 26 | run 25's next: non-copy resolution casts from exile honour the card's own timing restriction, AnimationSupport's CR citation, Primal Amulet's departed-source question; the Counter family (a countered-ability-source subject, `LockTargetPermanentEffect` with a subject, the "and" records, the "unless" records); the next fused family | done (4 split, 3 survivors, 8 blocked-other, 1 blocked-step-model, 5 unique; 6 card files rewritten; `Subjects.targetAbilitySource([filter])`, `StackEntry.counteredAbilitySourceId`, `LockTargetPermanentEffect.activatedAbilitiesOf`; Chandra, Torch of Defiance's +1 and the other resolution casts from exile fixed by a ruling, Interdict and Ouphe Vandals by Oracle text; Primal Amulet kept by Treasure Map's ruling; the "unless" counters ledgered; no further fused family split) |
| 27 | run 26's next: the "counter unless" records (investigate the ransom kind first, ask before changing ward), COUNTER_SPELL in `cardsThisWay` + a cast-permission step, the dying-creature exiles, verdicts for `ExileTargetPermanentAndTrackWithSourceEffect` and `ExileSelfAndReturnTransformedEffect` | done but item 1 (2 split, 4 survivors, 1 new step record, 1 blocked-other kept, 2 unique; 9 card files rewritten; `cardsThisWay(COUNTER_SPELL)`, `AllowPlayCardsWithoutPayingManaCostWhileExiledEffect`, `ExileSelfAndReturnTransformedEffect` records RETURN; the "counter unless" split held for the human: options in Findings) |
| 28 | the "counter unless" decision (option B: keep the records, fix the declined-ransom counter for Guile), then run 27's named largest unaudited fused names: `ForcedCostOrElseEffect`, the `CantAttack` / `CantBlock` "unless" restrictions, `SearchLibraryAndOrGraveyardForNamedCardToHandEffect`, `TransformSelfThenEffect`, and the next fused families | done (1 split, 1 absorbed, 2 survivors, 7 unique, 5 blocked-other, 1 blocked-step-model, the three counter-unless records re-ledgered; 26 card files rewritten; `StepKind.TRANSFORM`, `HappenedThisWay.transformed()`, `CounterSupport.counterSpell(gameData, counterControllerId, description, target)`; the declined discard / sacrifice / evidence / poison / blight / graveyard-exile / discard-hand ransoms now apply Guile's replacement, flashback's exile and the countered events) |
| 29 | run 28's next: the ward {N} decline through `CounterSupport`, a set subject on a boost record and the two sharing-boost records, a recording "you may exile a card from a graveyard" choice and `ExileAnyGraveyardCardThenIfMatchesEffect`; close step 8 (a verdict for every remaining fused multi-card record) | done (1 split, 2 absorbed, 2 survivors; 159 records ledgered without splitting: 97 unique, 26 blocked-step-model, 21 blocked-other, 7 blocked-player-loop, 8 `step-9`; 7 card files rewritten; `BoostTargetCreatureEffect` takes a target-declaring subject, `ExileUpToOneCardFromGraveyardEffect.matching` records EXILE; a declined ward payment exiles a flashback spell and fires the countered events) |

### 9. Sweep the single-card tail (repeatable)

Depends on: 7. Gets better after each run of steps 4, 5 and 8.

One run takes one first-word family, largest first (see Measurements): `--unaudited --family Exile`,
then `Each`, `Return`, `Put`, `Grant`, `Reveal`, `Choose`, `Target`, `Create`, `Prevent`, ... A run
also takes the family's ledger rows whose verdict is `step-9` (step 8's hand-offs: `--unaudited` does not
list them, grep the ledger). A run audits about 40 classes and gives each a verdict, in this order of
preference:

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

| Run | Family | Status |
|---|---|---|
| 1 | Exile: the five `step-9` hand-offs and the records used by one or two cards (the two-card ones, then their one-card siblings) | done (40 audited: 5 absorbed, 4 split, 5 survivors (one new step record), 9 unique, 17 blocked-other; 21 card files rewritten; `ExileTargetPlayerHandEffect` takes a `PlayerSubject`, `ExileUpToOneCardFromGraveyardEffect.withSource()`, `PutTimeCountersAndGainSuspendEffect`; two `step-9` rows left for run 2) |
| 2 | Exile: the graveyard last-known P/T bug, the until-source-leaves family with the Detention Sphere `step-9` row, a timed permission step over a `CardSubject` and the records it splits | done (22 audited: 3 absorbed, 7 split, 5 survivors (one new step record), 7 blocked-other; 19 card files rewritten; graveyard exile steps record `StepOutcome.lastKnownCards`, `AmountThisWay.keepingNegative()`, `ExileTargetPermanentUntilSourceLeavesEffect` takes a `PermanentSubject`, `AllowPlayCardsFromExileEffect`; Glorious Protector's simultaneous return fixed; random / exile-until steps and the cast-copies `step-9` row not reached) |
| 3 | Exile: the random-card exile step and the exile-until step with the records they split, the `AllowPlayCardsFromExileEffect` rider rows (Suspend Aggression, Mnemonic Betrayal), the cast-copies `step-9` row, the top-card permission records | done (42 audited: 2 absorbed, 15 split, 2 survivors (two new step records), 17 blocked-other, 7 unique; 21 card files rewritten; `ExileCardAtRandomEffect`, `ExileCardsFromTopUntilEffect`, `cardsThisWay(kind, filter)`; no `AllowPlayCardsFromExileEffect` rider added (each had one user); the cast-copies row ledgered blocked-other) |
| 4 | Exile: run 3's shared blockers (put-into-hand step, "onto the battlefield or into your hand", the source-linked library exile), then the one-card tail (no two-card record was left) | done (51 audited: 3 absorbed, 16 split, 7 survivors (one new step record), 14 blocked-other, 11 unique; 22 card files rewritten; `PutCardsIntoHandEffect`, `CardManaValueAtMostAmountPredicate`, `ReturnAllCardsExiledWithSourceEffect.putOntoBattlefield`, `ExileTopCardOfOwnLibraryEffect.linkedToSource`, `AllowPlayCardsFromExileEffect.playableOnlyIf` / `.playableByOwner()`, `EachOpponentExilesFromHandEffect.byPlayers`; the targeted graveyard exile no longer exiles a card that left and came back) |
| 5 | Exile: run 4's God-Pharaoh's Gift row and Agatha's `step-9` row, a graveyard-owner subject on the choose-a-card exile, a counter step on exiled cards, a card-filtered exile count, then the one-card tail | done (92 audited: 3 absorbed, 20 split, 2 dead, 2 survivors (one new step record), 55 blocked-other, 10 unique; 27 card files rewritten; `ExileUpToOneCardFromGraveyardEffect.from`, `PutCounterOnExiledCardsEffect`, `AmountThisWay.exiledCards`; six resume flag groups deleted; Fishing Gear's empty library fixed by Oracle text) |
| 6 | Exile: run 5's shared blockers (exile target spell, a delayed trigger bound to a step result, the face-down hand exile) with their ledgered rows, then the rare tail | done (93 audited: 9 absorbed, 27 split, 1 dead, 7 survivors (five new step records), 8 blocked-other, 2 blocked-step-model, 39 unique; 39 card files rewritten; `RegisterDelayedStepTriggerEffect`, `ExileTargetSpellEffect`, `ExileChosenPermanentsEffect`, `ExileTargetPlayerHandEffect.exiledFaceDown`, `StepKind.PUT_INTO_GRAVEYARD`; 15 rare records left on five shared blockers) |
| 7 | Exile: the five shared blockers with their ledgered rows, the family's rare tail closed; Each started | done for Exile, Each open (51 audited: 8 absorbed, 21 split, 12 survivors, 5 blocked-other, 2 blocked-step-model, 3 unique; 50 card files changed; `ExileCardsWithSameNameEffect`, `ExileCardFromHandEffect`, `ExileFaceDownPileEffect`, `revealedBy` on the reveal-until step, `StepKind.SEEK`; four ledgered Exile causes and 113 rare Each records left) |
| 8 | The five shared blockers run 7 named with their ledgered rows; Each's rare tail, continued | done for the blockers, Each open (77 audited: 6 absorbed, 19 split, 1 dead, 15 survivors, 27 blocked-player-loop, 1 blocked-step-model, 3 blocked-other, 5 unique; 34 card files changed; `StepKind.CHOOSE`, `ChooseCardEffect`, `ChoosePlayerAtRandomEffect`, `BecomeCopyOfSubjectEffect`, `RegisterDelayedEventTriggerEffect`, EXILE recorded by the exile wipe and the revealed-hand choice; 69 rare Each records left) |
| 9 | The Yangchen fix, run 8's shared blockers with the waiting records and ledgered rows, the rest of Each's rare tail | done, Each closed (101 rows: 26 absorbed, 18 split, 1 dead, 17 survivors, 33 blocked-player-loop, 4 blocked-other, 2 unique; 76 card files changed; `StepKind.REVEAL`, `PAY_MANA`, `SHUFFLE_INTO_LIBRARY`, `EachPlayerSearchesLibraryEffect`, `EachPlayerPaysAnyManaEffect`, `PutCardsOntoBattlefieldEffect`, `villainousChoiceFacedBy`) |
| 10 | Run 9's two search defects, the Return family's shared blockers with the waiting rows, then its rare tail | done but 17 rare Return records (95 rows: 20 absorbed, 14 split, 3 dead, 8 survivors, 14 blocked-other, 36 unique; 53 card files changed; `ReturnChosenPermanentsToHandEffect`, `cardsThisWay(RETURN)`, `triggeringCards()` / `sourceCard()` / `inYourGraveyard`, `PutCardsOntoBattlefieldEffect` with controller, face down and entering effects; Krenko's Buzzcrusher on the search queue) |
| 11 | Run 10's two defects, the Return family's five shared blockers with the waiting rows and the Aura hosts; Put started | done for Return, Put barely started (44 rows: 13 absorbed, 20 split, 5 survivors, 2 blocked-other, 4 unique; 58 card files changed; `ChooseCardEffect` up to N / one of each, `exiledWithSource()` / `exiledCards` / `except`, the creature-type step, `attachedTo`, CR 400.4a on the returns) |
| 12 | Run 11's two defects, the Put records' shared blockers (exiled-card target, command zone, hand, graveyard top, random) with their ledgered rows, then Put's rare tail | done, Put closed (115 rows: 19 absorbed, 23 split, 15 survivors, 28 blocked-other, 29 unique, 1 `step-9`; 49 card files changed; five `CardSubject` variants, a chooser and a random choice on `ChooseCardEffect`, `PutCardsIntoLibraryEffect` with `StepKind.PUT_INTO_LIBRARY`; four prompts and log lines no longer show face-down cards) |
| 13 | Run 12's defects (the prompts' minimum, face-down cards in prompts, Liege of the Tangle, Gruesome Encore), the Reveal family's shared parts with their ledgered rows, then its rare records | done, Reveal closed (120 rare records: 2 absorbed, 71 split, 22 blocked, 25 unique; with the other rows 80 records deleted; 96 card files changed; `RevealCardsFromTopUntilEffect`, `RevealCardsFromHandEffect`, `DiscardCardsEffect`, the library step over revealed cards) |
| 14 | Run 13's defects (pooled index prompts, the two discards, Invasion of Alara, replaced draws), the reveal-from-hand row and the planar verdicts, then Choose: the chosen name and player and the hand and exile choices with existing second halves | done for the defects and parts, Choose open (30 rows: 21 split, 2 survivors, 7 blocked-other, six of them corrected planar verdicts, 21 records deleted; 28 card files changed; `ChooseCardNameAtResolutionEffect` with `CardHasNameChosenThisResolutionPredicate`, `ChoosePlayerEffect`; 80 rare Choose records left) |
| 15 | Run 14's defects (the other name prompts, the perpetual choices' log, the three draw handlers), the choose-permanents and perpetual grant steps, users of the player and name steps, then Choose's rare records | done for the defects and the two steps, Choose open (26 rows: 2 absorbed, 19 split, 5 survivors, 21 records deleted; 29 card files changed; `ChoosePermanentsEffect` with `thisWay(CHOOSE)`, `DestroyAllPermanentsEffect.sparing`, `PerpetuallyGrantAbilitiesEffect`; 64 rare Choose records left) |
| 16 | Run 15's defects (exile-by-name searches, as-enters copy log, APNAP draws, the AI's dig), the chosen name's remaining users, the choose-permanents step per player and at random, then Choose's rare records | done for the defects and those parts, Choose open (28 rows: 1 absorbed, 8 split, 4 survivors, 15 unique, 9 records deleted; 20 card files changed; `ExileCardsWithSameNameEffect` by the chosen name, `CantPlayChosenNameUntilNextTurnEffect`, `ChoosePermanentsEffect.oneForEach` / `.atRandom`; 43 rare Choose records left) |
| 17 | Run 16's small defects and dead code (Mindblaze's shuffle, split names), its shared parts, then the rest of Choose's rare records | done, Choose closed (50 rows: 2 absorbed, 12 split, 6 survivors, 8 blocked-other, 22 unique; 14 records deleted; 27 card files changed, 16 rewritten; `RegisterDelayedStepTriggerEffect.aboutPutOntoBattlefield`, subjects on four survivors, `ChoosePermanentOnEnterEffect`) |

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

### Step 6, run 6.2e (2026-11-20)

**Done: `TapUntapScope` is gone.** The six records that still carried it take a `PermanentSubject` (a set's filter
lives in the subject, as in 5d): `SkipNextUntapEffect(subject, untapSteps, matchAtUntap, allPlayersAtUntap,
controllerStepOnly, activationCost)` (overloads drop trailing arguments; `withOneStepFewer()` for the untap step),
`CantBlockThisTurnEffect(subject)`, `CantAttackThisTurnEffect(subject[, duration])`, `DoesntUntapEffect(subject,
condition)` (factories unchanged), `DoesntUntapWithCounterEffect(counterType, subject, counterBearer)` (the
`(CounterType, TapUntapScope)` overload went: no caller), `UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(step,
subject)` (the `(step[, filter])` forms build `controlledBy(you()[, filter])`). Compact constructors reject subjects the
handlers can't resolve. 268 card files were rewritten by a deleted scratch script, 8 by hand (Tangle, Spore Cloud,
Clinging Mists, Breaching Leviathan, Oriss, Ember Gale, Concussive Bolt, Mark for Death); 10 test files, 13 engine/AI
files. No record deleted: ratchet unchanged.

Constants mapped: TARGET `target()`; SELF `source()`; TRIGGERING `triggering()`; ENCHANTED `attached()`; CONTROLLED
`controlledBy(you(), f)`; TARGET_PLAYERS_PERMANENTS `controlledBy(targetPlayer(), f)` (Concussive Bolt:
`controlledBy(allOf(targetPlayerDeclaredElsewhere(), controllerOf(target())))`, "that player or that planeswalker's
controller", declared by the damage); OPPONENTS_PERMANENTS `controlledBy(eachOpponent(), f)`; ALL_CREATURES
`controlledBy(eachPlayer(), f)` for can't-block (the old rule had no creature part), `controlledBy(eachPlayer(),
AllOf(creature, f))` for skip-untap; TARGET_CONTROLLERS_OTHER_CREATURES `controlledBy(controllerOf(target()),
AllOf(creature, Not(PermanentIsTargetCardPredicate)))`. `targetSpec()`: `target()` as before; a set whose players
declare a target is a benign player target; else none.

**Lasting rules vs locked sets (CR 611.2c, checked with `get_rule`).** A rule is expressed as the subject judged when
the rule is checked: its players are bound as it resolves, its filter later. New
`SubjectResolutionService.lastingRule(gameData, entry, effect, ControlledBy)` binds the players (`eachPlayer()`: none)
and a `PermanentIsTargetCardPredicate` leaf to the effect's target permanent; `CantBlockThisTurnEffectHandler` makes its
floating `MatchingCreaturesCantBlockMatchingCreaturesEffect` from it (same predicates as before). Can't-attack sets are
an `onPlayer` floating effect per bound player; `AttackLegalityService` judges the set's filter. Skip-untap
`matchAtUntap` sets register `restrictNextUntapStep` per bound player; `UntapStepService` judges the set's filter. Sets
fixed as they resolve: skip-untap without `matchAtUntap` (Sleep, Kiora Bests the Sea God: "those creatures" / "they";
Tangle, Spore Cloud, Clinging Mists, Breaching Leviathan: the attacking / nonblue creatures, Tangle's ruling "determined
at the time this spell resolves"). Oracle text and rulings were checked for every set card. `CantAttackThisTurnEffect`
ALL_CREATURES had no card (only TARGET and Oriss's TARGET_PLAYERS_PERMANENTS), so no card needed the 6.1 fix; a set is
now always the per-player rule (`AttackLegalityServiceTest.resolvedCantAttackRuleCoversLaterCreatures`).

**Handlers.** All four one-shot handlers resolve through the resolver and lost their private lookups:
`SkipNextUntap` (named subjects marked; every set marks what it finds now, owner = the permanent's controller unless
the set is every player's; `matchAtUntap` also registers the rule), `CantBlock`, `CantAttack`, `DoesntUntap`
(`permanentIds`). Static readers: `UntapStepService`'s three cross-player routines are one
(`untapDuringOtherPlayersStep`): it collects every active ability (permanents of any battlefield, conditionals unwrapped,
emblems' sets), resolves each subject with the static entry point (new overload `permanents(gameData, source,
controllerId, subject)` for an emblem), untaps a set's permanents with the per-player Seedborn / filtered log and a
named permanent with its per-permanent log; the CR 502.3 lock-at-start, restricted-ids and lost-abilities checks are
kept per subject shape. Counter locks and the attached `DoesntUntapEffect` check ask whether the lock's subject names
the permanent (`namesOwnOrHost`); `UntapPreventionSupport` and `ActivatedAbilityExecutionService` test `instanceof
Source`. AI: `TargetPolarityClassifier` (target, or a set whose players come from a target) and `SpellEvaluator`
(`Target` capped at max targets, a set not of `controllerOf` players scores every blocker) see what they saw.

**Behaviour changes.** (1) Ember Gale ("creatures target player controls") declared a player-or-planeswalker target;
now a player (`EmberGaleTest.cannotTargetPlaneswalker`). (2) Pollen Lullaby's marks were for "whichever untap step comes
next"; now for that player's step, so a creature stolen before it untaps under its new controller ("creatures that
player controls don't untap during the player's next untap step"; `PollenLullabyTest.frozenCreatureUntapsUnderNewController`,
fails on the old code). (3) A `matchAtUntap` target-player set also marks the permanents found now (Exhaustion, Misstep,
Imaginary Threats...), as CONTROLLED and OPPONENTS already did: redundant with the rule, but Static Orb no longer counts
them as untap candidates. (4) Kinds 5d accepted: `target()` reads the effect's group before `getTargetId()` (CantAttack read
the flat list); `attached()` falls back to last-known information where CantBlock ENCHANTED read the activation-captured
target; logs: a can't-attack player rule logs even with no creature, the skip-untap every-player set says
"permanent(s)" not "creature(s)", cross-player untap logs are grouped per player. A temporary probe (thrown where
`triggering()` would differ from the old skip-untap TRIGGERING fallback to `getTargetId()`) never fired over the tests
below and was removed.

**Tests**, all pass: every test class of every card naming the six records (all of them, no sample: 505 card-test
classes, which include every "this turn" rule card: `CreaturesCantAttackThisTurn`, `CreaturesOfChosenManaValueParity...`,
`CreaturesWithToughnessGreaterThanPower...`, `OtherCreaturesCantAttackThisTurn`, `MatchingCreaturesCantBlock...`,
`MatchingPermanentsDoesntUntap`, `TapAndLockOtherPermanents`, exert cards) and the AI test classes reading them
(`TargetPolarityGuardTest`, `HardAiDecisionEngineTest`, `EasyAiDecisionEngineTest`, `SpellEvaluatorTest`,
`AiTargetSelectorTest`, ...): 563 suites, 5,148 test cases (1 skipped); then, after the probe was removed, the
application selection (`UntapStepServiceTest`, `TurnCleanupServiceTest`, `TapTriggersTest`, `SpellCastingServiceTest`,
`EffectResolutionServiceTest`, `AttackLegalityServiceTest`, `ActivatedAbilityExecutionServiceTest`,
`SubjectResolutionServiceTest` with new `$LastingRule` and the emblem overload, `EffectCountRatchetTest`, the `layers`
package) and the four `triggering()` cards: 101 suites, 541 test cases (1 skipped). `UntapStepServiceTest`'s hand-built
resolver now uses the test's predicate mock and finds controllers on the board; its "filtered" case uses a creature
filter (a `PermanentTruePredicate` set is the unfiltered Seedborn form) and the attached-lock case checks the subject.
Application, AI, testFixtures and card-test `e`, `p`, `s` test sources compile. Ask for a full card-test run (276 card
files rewritten; the plan's run after sub-step 2 is due now).

**For later runs.** 6.3 has only `GrantScope` left. `DoesntUntapWithCounterEffect` / `DoesntUntapEffect` static
subjects are judged structurally (`source()` / `attached()`), not through `names(...)`: they need no characteristic reads.

### Step 6, run 6.3 (2026-11-20)

**Done: `GrantScope` is gone.** Every record that carried it (the 62 with the component and the interfaces/records that
derived one: `KeywordGrantingEffect`, `StaticCreatureBoostEffect`, `CombatDamageAssignmentEffect`,
`ProtectionGrantingEffect.protectionSubject()`, ...) now has `PermanentSubject subject` (accessor `subject()`); constructors
and overloads keep their shapes with the type swapped. `targetSpec()` and validation are structural (`instanceof Target /
Source / Triggering`, `subject.isSet()`, new `PermanentSubject.declaresPlayerTarget()` for "creatures target player
controls", record `equals` against a named set where one constant was meant); each gives the old spec for every old
constant. 5,582 card files were rewritten by a deleted scratch script (`GrantScope.X` → `Subjects.<named set>()`, imports
fixed), 2 by hand (Mirror Entity, Volatile Claws: the no-argument `GrantAllCreatureTypesToOwnCreaturesEffect()` they used
meant "creatures you control" on the one-shot path), comments in 8; 97 domain files, 253 engine/AI main files (most only an import or a `.subject()` read), 29 test
files, 20 doc files.
No record or handler deleted: ratchet unchanged.

**Named sets** (`Subjects`, one shared instance each, predicates in the order `GrantScope` built them): `sourceAndPaired()`,
`creaturesYouControl()` / `otherCreaturesYouControl()`, `otherTappedCreaturesYouControl()`,
`otherUntappedCreaturesYouControl()`, `creaturesYourOpponentsControl()`, `permanentsYouControl()` /
`otherPermanentsYouControl()`, `creaturesTargetPlayerControls()` / `landsTargetPlayerControls()`, `allCreatures()` /
`otherCreatures()`, `allPermanents()` / `otherPermanents()`, `creaturesEnchantedPlayerControls()`, `landsYouControl()`,
`landsYourOpponentsControl()`, `allLands()` / `otherLands()`. Default constant → set: OWN_X "other", ALL_OWN_CREATURES
"creatures you control", ALL_CREATURES/ALL_PERMANENTS/ALL_LANDS "other", *_INCLUDING_SELF "all".

**New variants** (each with a `SubjectResolutionServiceTest` nested class and a SUBJECTS_REFERENCE row; semantics copied
from the old handler lookups): `attachedCreature()` = `Attached(creature = true)` for ENCHANTED_CREATURE /
EQUIPPED_CREATURE (`attached()` stays ENCHANTED_PERMANENT; a resolving effect names the host only while it is a
creature, the static path judges no type, as before; GQS `hasAttachmentRestriction` and the AI's
`namesAttachedCreature` read the flag); `triggeringAttackers()` (DECLARED_ATTACKERS); `triggeringSpellTargets()`
(GrantKeyword's TARGETS: the triggering spell's targets on the stack, else as recorded, else the effect's own targets;
declares nothing); `createdThisResolution()` (TOKENS_CREATED_THIS_RESOLUTION: the entry's `createdPermanentIds`, live; no
step-7 design was needed, it reads the existing channel like `returnedThisResolution()`); `bandmatesOf(source())`
(BANDED_WITH_SELF); `withCreaturesSharingAColor(target())` (TARGET_AND_SHARING_CREATURES; declares the target through
its part). TARGETS on GrantColorUEOT and SetCombatRequirement became `allTargets()` (only the branch choice: those
handlers still read the effect's group, so the targets are as before; `targetSpec()` unchanged).

**Per-verb mapping, applied per card by context.** A scratch census test (deleted) constructed every card class (31k)
and recorded each `GrantScope`-bearing record instance with its path (static ability, conditional static, one-shot
ability, emblem or other payload). The script mapped a constant by (record, constant, context) and the census was rerun
after the rewrite: every instance got exactly the subject its old reader meant (7,552 instances, no mismatch). Remapped
from the default: one-shot GrantKeyword ALL_CREATURES → `allCreatures()` (27) and ALL_LANDS → `allLands()` (1);
RemoveKeyword ALL_CREATURES → `allCreatures()` (5), ALL_PERMANENTS → `allPermanents()` (10); GrantProtectionChoice,
TargetCreatureBecomesChosenSubtype ALL_CREATURES → `allCreatures()`; one-shot OWN_CREATURES → `creaturesYouControl()` for
GrantProtectionChoice, TargetCreatureBecomesChosenSubtype, LosesAllAbilities, GrantColorUEOT, GrantSubtypeUEOT,
TemporaryCreatureTypes, TargetCreatureBecomesSubtype, GrantProtectionFromColorUEOT; AnimatePermanents one-shot ALL_LANDS
→ `allLands()` (Natural Affinity); BoostAllCreaturesOfChosenSubtype's constructor defaults → `allCreatures()` /
`creaturesYouControl()`; static GrantKeyword OWN_PERMANENTS → `otherPermanentsYouControl()` (32; inside a conditional
static it stays `permanentsYouControl()`, since the conditional self path gave the source the keyword); BecomeColorless
static ALL_PERMANENTS → `allPermanents()` (Thran Lens, Ghostflame Sliver: its self handler reused the including-source
reading); conditional static StaticBoost ALL_CREATURES → `allCreatures()` (8 cards: Ivory Guardians, Nut Collector,
Common Cause, Call to Arms, Jihad, Raph's Bravado, Tidal Influence, Watchdog; Oracle text says "creatures named ...",
"all creatures", "nonartifact creatures", nothing "other"). `withAllPermanentsIncludingSource` turned out to matter only
for BecomeColorless: the other verbs' non-self handlers never see the source for a printed ability and their self
handlers (or the layer-4 pass) read the plain subject, so their ALL_PERMANENTS keeps "other permanents" (Crypt Sliver and
the Sliver lords carry a separate `source()` grant; Zombie Master says "other Zombie creatures"). Emblems and payloads
(`CreateEmblemEffect`, `GrantEffectsToCounterBearersEffect`, ...) keep the default (their readers used `subject()`).

**Deleted readers.** `StaticEffectSupport.withAllPermanentsIncludingSource`, `withOwnPermanentsAsOthers`, the conditional
path's ALL_CREATURES special case, `CREATURES_YOU_CONTROL` / `OTHER_PERMANENTS_YOU_CONTROL` /
`ALL_PERMANENTS_INCLUDING_SOURCE` constants; `GrantSubjectShapes.subjectOf` (AI callers read `x.subject()`);
`GrantScope.hasSubject()`; every one-shot handler's constant dispatch (6.2d's list) now dispatches on the subject's shape
(`isSet()`, `declaresPlayerTarget()`, `instanceof`, or `equals` a named set where a branch was for one constant), and
the private lookups for the bandmates, the sharing creatures, the tokens and the triggering spell's targets moved into
the resolver. `Card.hasKeyword` (changeling self grant), GQS emblem/combat-damage/attachment readers,
`AttackLegalityService.namesCreature` take a subject.

**Behaviour changes** (accepted kinds; none changes a test): `attachedCreature()` on the one-shot path names nothing
when the host is no longer a creature (SetCombatRequirement, Suspect and Unsuspect did not check before, the
GrantKeyword handler did; an "enchant creature" Aura or an Equipment on a noncreature leaves it
before anything resolves); the one-shot handlers whose unknown-constant default resolved something odd
(GrantActivatedAbility: "creatures you control"; SetBasePT: the source) now use any set subject as given (no card had
such a constant on those paths); GrantKeyword's set grants all log "N creature(s)/land(s)/permanent(s)" from the set's
filter and their floating payloads carry the grant's filter (no reader).

**Tests** (after the census test was removed): application selection (the `layers` package, `SubjectResolutionServiceTest`
with new `$AttachedCreature`, `$TriggeringAttackers`, `$TriggeringSpellTargets`, `$CreatedThisResolution`,
`$BandmatesOf`, `$WithCreaturesSharingAColor` and the static agreement check over the named sets, `GameQueryServiceTest`,
`LayerClassifierTest`, `PredicateEvaluationServiceTest`, `BattlefieldPlacementServiceTest`, `AttackLegalityServiceTest`,
`AuraAttachmentServiceTest`, `TextChangeTransformerTest`, `EffectCountRatchetTest` and every application test touched):
193 suites, 3,731 test cases (30 skipped), all pass; card tests: every test class of a card whose constant was
remapped by context, got a new variant, sits in a one-shot set/attached path of a converted handler, or names
ALL_PERMANENTS / SELF_AND_PAIRED / GrantKeyword OWN_PERMANENTS, plus a 40-per-class sample of every converted record:
1,681 suites, 14,170 test cases, all pass; the whole `magical-vibes-ai` test task: 115 suites, 1,649 test cases (4
skipped), all pass. Application, AI, engine testFixtures and card-test `b`, `d`, `l`, `m`, `u` test sources compile,
every card module compiles. **Ask for a full card-test run** (5,582 card files rewritten).

**For later runs.** 6.4 (rule-4 tests) is open. Doc prose still names old constants in places (`ENCHANTED_CREATURE`,
`ALL_CREATURES_INCLUDING_SELF` in EFFECTS_QUICK_REFERENCE / ORACLE_TEXT_EFFECT_MAP lines): read them as the named sets
above. `scripts/effect-inventory.py` still prints a `GrantScope` row (now 0). `UntapTargetAndSharingCreatures` (5d,
kept) could fold onto `withCreaturesSharingAColor(target())`; `TemporaryCreatureTypes`' no-argument constructor still
means "other creatures you control" (static Maskwood Nexus).

### Step 6, run 6.4 (2026-11-20)

**Rule-4 tests** (CR 611.2c / 611.3a, checked with `get_rule`). Already covered, nothing added: one-shot Boost
(`WarriorsHonorTest.affectsOnlyCreaturesPresentAtResolution`, `OverrunTest.onlyCreaturesPresentAtResolutionAreAffected`),
one-shot GrantKeyword (the same Overrun test's trample; Huatli chapter IV's later Dinosaur), one-shot Animate
(`NaturalAffinityTest.doesNotAnimateLandEnteringAfterResolution` / `doesNotAnimateLandsEnteringAfterResolution`), static
GrantKeyword (`LevitationTest.laterCreaturesGainFlying`), static Animate (`LivingPlaneTest.animatesLandsEnteringLater`,
`NaturesRevoltTest`, `KormusBellTest`). Added: static Boost `GloriousAnthemTest.buffsCreatureEnteringLater`.

**The GrantActivatedAbility live scope is gone.** Its only user was Roar of the Fifth People (LCI 189 back face), chapter II:
"This Saga gains 'Creatures you control have ...'" (ruling: no duration, applies while the Saga remains). That is a static
ability the Saga gains, so it rightly reaches later creatures, but it was encoded as a one-shot grant special-cased in
`GrantActivatedAbilityEffectHandler` (OWN_CREATURES + WHILE_SOURCE_REMAINS, excluding the Saga). It is now
`GrantStaticEffectToSourceEffect(new GrantActivatedAbilityEffect(ability, creaturesYouControl()))` and the special path is
deleted: every resolving set grant now locks its set. Chapter IV's "Dinosaurs you control" became `creaturesYouControl()`.
Two engine fixes the conversion exposed: (1) `LayerSystemService.withoutStaticGrantsLostToLaterAbilityLoss` now also drops a
gained static ability when a *static* lose-all-abilities effect with a later timestamp names its source (Minimus
Containment attached afterwards, CR 613.7; it only checked floating ones); (2) the March of the Machines / Starfield of Nyx /
Opalescence base P/T read `getCard().getManaValue()`, so a transformed back face was 0/0; it now uses
`GameQueryService.getPermanentManaValue` (front face's mana value, CR 712.6d). Test:
`HuatliPoetOfUnityTest.chapterIIManaAbilityReachesTheSagaWhileItIsACreature` (the Saga is a 3/3 under Opalescence and has the
mana ability; fails on the old code twice over). The class's ten test methods without `@DisplayName` got one.

**Fold.** `UntapTargetAndSharingCreaturesEffect` (+ handler) absorbed: Rally the Righteous is
`UntapPermanentsEffect(withCreaturesSharingAColor(target()))`. Same lookup (the resolver's was copied from GrantKeyword's,
which matched the old handler: target must be a creature, colorless target names only itself); `targetSpec()` gives the old
`benign(creature)`; the handler untaps and logs it like a set; the AI polarity entry became a shape check (BENEFICIAL).
Test: `RallyTheRighteousTest.cannotTargetNoncreature`. Ratchet 4858 / 4091.

**Leftovers closed.** Doc prose in agent-docs and `.claude/skills` no longer names `GrantScope` / `TapUntapScope` constants
(a deleted scratch script mapped each to its subject, picking "other"/"all" from the referenced card's own code, else the
Oracle words on the line; ~570 lines in 14 files, the plan files `EFFECT_MERGE_PLAN.md` / `TARGET_PREDICATE_PLAN.md` and the
audit ledger left as history). `scripts/effect-inventory.py` prints a PermanentSubject / PlayerSubject row instead.

**For later runs.** `BoostTargetAndSharingCreaturesUntilEndOfTurnEffect` (Rally's second half) could fold the same way onto
a boost survivor. Ask for a full card-test run (6.3's 5,582 rewritten card files are still unconfirmed by one).

### Step 7 (2026-11-20)

**Inventory: how a later step learns what an earlier one did** (writer → reader; "stays" = kept as it is).

| Channel on `StackEntry` | Writers | Readers | Now |
|---|---|---|---|
| `resolutionResults.departures` | `DepartureService.departAsStep` / `finishStepDeparture`, `SacrificeSelfEffectHandler`, `DepartPermanentsEffectHandler` (empty list) | ~20 fused handlers (`SacrificeTarget*Then*`, `SacrificeSelfAnd*`, `DestroyTargetThenRevealUntilType*`, `InfernalOffering`, `ResolveDelayedPermanentAction`, `RemoveAllCountersThenDestroy...`, `PlayersWhoTappedLandForMana...`), `StepDepartureSupport`, `PermanentLeftThisWayPower` / `Toughness` | stays (last-known power, toughness, types); also gives the DESTROY / SACRIFICE outcomes |
| `resolutionResults.pickedIds` | `LibraryChoiceHandlerService`, `LibrarySelectionFollowUp`, `SearchLibraryAndOrGraveyardChoiceInteractionHandler`, `QueueReflexiveAbility`, `CreateTokenThen`, `BendOrBreak`, `AttachSelectedAurasToLegalTargets`, `RevealUntilNonlandBottomThenDealManaValueDamage` | `TransformChosenPermanent`, `ReturnSelectedPermanentToHandAtEndOfCombat`, `MakeChosenPermanentAttacking`, `ConjureDuplicateOfSelectedPermanent`, `AttachSelected*`, `AttachOneOfEquipment*`, the writers above | stays (a choice, not an outcome) |
| `resolutionResults.drawnCardIds` | `DrawCardEffectHandler` (`recordDrawnCards`) | `CardDrawnThisWay` | **replaced** by the DRAW outcome |
| `resolutionResults.counteredSpell` | `CounterSpellEffectHandler` | `SpellCounteredThisWay` | **replaced** by COUNTER_SPELL |
| `resolutionResults.copiedSpell` | `CopyTriggeringSpellEffectHandler` | `SpellCopiedThisWay` | **replaced** by COPY_SPELL |
| `eventValue` | 11 trigger collectors (the event's number) and ~135 resolution writers: 123 `normalfx` handlers (`rememberAmount`, `recordDamageDealt`, `recordTargetLifeLost`, `recordSacrificedCount` / `Power` flags; the roll, pay-X, mill/exile/reveal "then" handlers), 2 `mayfx`, 8 input/interaction completions, `ReflexiveAbilitySupport`, `DamagePreventionService`, `PermanentRemovalService` | `EventValue` (555 card files, mostly the trigger payload), `AmountEvaluationService` (target-spell mana value fallback) | stays: a trigger's payload; the in-resolution writers move to `AmountThisWay` card by card in steps 8–9 |
| `createdPermanentIds` (whole resolution) | `CreateTokenEffectHandler`, ~50 token handlers, `TokenCopySupport`, `ChoiceHandlerService` | `createdThisResolution()`, ~37 `*CreatedPermanents*` / `CreateToken*` / `Attach*ToCreatedPermanent` handlers, `DestroyAllPermanents` (spares them), `AmountEvaluationService` | stays (the CREATE_TOKEN outcome is the step's share) |
| `resolutionExiledCardIds` | `LibraryChoiceHandlerService`, `ExileTopCardsToSource`, `ExileGraveyardCards`, `LivingDeath` | `ChooseNonlandCardExiledWithSourceMayCast`, `AllowCastAllCardsExiledWithSourceUntilEndOfTurn`, `LivingDeath`, `LibraryChoiceHandlerService` | stays (cards exiled from libraries and graveyards) |
| `drawnCardIdsThisResolution` (whole resolution) | `DrawCardEffectHandler`, `EachOpponentDrawsCard`, `DrawCardForEachOpponentControllingReturnedPermanent`, `TargetPlayerGainsControlOfTargetPermanentsAndDrawPerPermanent` | `CardsDrawnThisResolution`, `DiscardEffect.onlyCardsDrawnThisResolution`, `RevealDrawnCard*` (2), `MayCastAnySpellFromHandWithoutPayingManaCost`, `EachPlayerSacrificesPermanentUnlessDiscard`, `DrawCardsThen`, `DrawCardThenIfNoCardsDrawn` | stays |
| `returnedPermanentIds` | `ReturnTargetCardsFromGraveyardToBattlefield`, `ReturnAllCardsExiledWithSource`, `AnimationSupport`, `FlickerEffectHandler` (clears) | `returnedThisResolution()`, `PutCounterOnReturnedPermanents`, `PutCounterOnOneTarget`, `AmountEvaluationService.opponentsControllingReturnedPermanents`, `ReturnTargetCardsFromGraveyardToBattlefield` | stays |
| `acceptedPlayerIds` | `EachPlayerMayDiscardHandThenDrawEffectHandler` | `acceptedPlayers()`, `EachPlayerMayDiscardHandThenDrawEffect` | stays |
| `playersDealtDamageThisResolution` | `DamageSupport.finishPlayerDamage` | `playersDealtDamageThisResolution()`, `DealDividedDamage`, `ScryIfPlayerDealtDamageThisWay`, `DiscardIfPlayerDealtDamageThisWay` | stays |
| `permanentsDealtDamageThisResolution` | `DamageSupport` (2 sites) | `DealDamageToTriggeringPermanentThenBoostSourceIfDamaged` | stays (DEAL_DAMAGE's objects are the step's share) |
| `counteredPermanentIdsThisResolution` (permanents given counters) | `MultiPermanentChoiceHandlerService`, `PutCounterOnChosenOwnPermanent` | `PhaseOutPermanentsThatReceivedCountersThisWay`, `MultiPermanentChoiceHandlerService` | stays |
| `sacrificedCard*` | `SpellCastingService`, `StackResolutionService`, `PermanentChoiceBattlefieldHandlerService`, `SacrificeEnchantedPermanentAndReattachSourceAura` | `StackResolutionService`, `ConditionContext` (`SacrificedCardMatches`) | stays: a cost, not a step |
| `subgameResult` | `SubgameService`, `EachPlayerLosesFractionOfLifeRoundedUp` (clears) | `subgameWinners()` | stays |
| `targetId` read as a result | the exile / return steps' targets | `returned()`, `exiledThisResolution()` | stays |

Step-result subjects that already existed and are reused, not duplicated: `returned()`, `returnedThisResolution()`,
`exiledThisResolution()`, `createdThisResolution()`, `playersDealtDamageThisResolution()`, `subgameWinners()`,
`acceptedPlayers()`.

**Design.** `ResolutionResults(departures, pickedIds, Map<StepKind, StepOutcome> outcomes)` (domain `model/`, immutable,
an unmodifiable `EnumMap`, shared by the `StackEntry` copy like before). `StepKind` has DRAW, DISCARD, MILL, EXILE,
DESTROY, SACRIFICE, RETURN, CREATE_TOKEN, DEAL_DAMAGE, GAIN_LIFE, LOSE_LIFE, PUT_COUNTERS, REMOVE_COUNTERS,
COUNTER_SPELL, COPY_SPELL; its Javadoc says what each kind puts in `StepOutcome(amount, objectIds, playerIds)` (how
many; which cards or permanents, by the id the step acted on; which players, the controllers or owners as the step
acted, CR 608.2h). One outcome per kind, the latest step's: `StackEntry.recordStep(kind, outcome)` replaces it and
every step calls it, `StepOutcome.NONE` when nothing happened, so a later step never reads an older one (keyed by kind
so a step of another kind in between, or a wrapper, does not hide it). `addToStep` is for results that arrive in parts
(damage as its event ends, a discard or sacrifice answered at a prompt). `withDepartures` also writes DESTROY and
SACRIFICE from the departures, so the removal steps on the step model need no second record. Deep copy: shared
immutable value; `GameDataDeepCopyTest.deepCopyKeepsStepResultsIndependently`.

**Readers.** Condition `HappenedThisWay(StepKind)` (factories `drew()`, `sacrificed()`, `destroyed()`, `countered()`,
`copied()`); it absorbed `PermanentLeftThisWay`, `CardDrawnThisWay`, `SpellCounteredThisWay`, `SpellCopiedThisWay`
(deleted; Dreamcatcher, Baral, Fold into Aether, Shiko and Narset, `StepDepartureSupport`, two application tests and
the docs moved; the same meaning: PermanentLeftThisWay read the departures, which now are the SACRIFICE / DESTROY
outcome; the log reason text changed to "nothing was <verb> this way", no test asserted it). Amount
`AmountThisWay(StepKind)` (`model/amount`; `CardsDrawnThisResolution` stays: it counts the whole resolution).
Subjects `thisWay(StepKind)` (`PermanentSubject.ThisWay`: PUT_COUNTERS, REMOVE_COUNTERS, DEAL_DAMAGE only, live
permanents only, CR 400.7; CREATE_TOKEN / RETURN are refused in favour of `createdThisResolution()` /
`returnedThisResolution()`, the removal kinds because nothing is left to name) and `playersThisWay(StepKind)`
(`PlayerSubject.PlayersThisWay`: not DEAL_DAMAGE, COUNTER_SPELL, COPY_SPELL, refused in favour of
`playersDealtDamageThisResolution()` / `controllerOf(targetSpell())`). Both `needsStackEntry()`; SUBJECTS_REFERENCE rows.
CR 608.2c and 608.2h checked with `get_rule` (2026-11-20).

**Recording primitives** (each handler's `resolve` records; behaviour otherwise unchanged): `DrawCardEffect` (cards
drawn from the library, as before), `DiscardEffect` (begins the step unless it is a rerun of its each-player loop;
`GameData.discarded` adds each discard of that resolution, the resolving entry or the one parked on its prompt),
`MillEffect` (the milled cards in any public zone, CR 701.17c: it now calls `resolveMillPlayerIncludingExiled`, and
`GraveyardService.resolveMillPlayersSimultaneously` returns the cards; the flag only changes what is returned),
`ExileTargetPermanentEffect` (both paths), `DestroyTargetPermanentEffect` (target path: `tryDestroyAndLog`'s result;
batch paths: `destroyBatchDestroyed`), `SacrificePermanentsEffect` (begins the step; `GameData.recordSacrificedPermanent`
adds each sacrifice; `SacrificeSelfEffect` already recorded departures), `ReturnToHandEffect` (permanents whose card is
in a hand afterwards; players = the hand's owner, Recoil's "that player"), `ReturnCardFromGraveyardEffect` (cards gone
from graveyards into a hand or onto the battlefield; owners), `CreateTokenEffect` (`resolve` only; tokens and their
controllers), `DealDamageToAnyTarget` / `ToPlayers` / `ToTargetCreature` (begin the step; `DamageSupport.finishDamage`
adds noncombat damage as dealt, replayed permanent damage after a journal rerun too; carrier entries hand theirs
back), `GainLifeEffect` / `LoseLifeEffect` (life-total change per player, `LifeSupport.lifeChange`),
`PutCountersOnSelfEffect` / `PutCounterOnTargetPermanentEffect` (counter-count change on the candidates,
`PermanentCounterSupport.counterCounts` / `counterChange`; a counter-replacement order answered later adds the rest),
`RemoveCounterFromTargetPermanentEffect`, `CounterSpellEffect`, `CopyTriggeringSpellEffect`.

Tests (`StepResultsTest`, harness, 23 cases): one or more per primitive composed as a real card's text reads, without its
fused effect — Baral (draw / if you do), Mishra's Command ("a card for each card discarded this way", across the discard
prompt), Saprazzan Breaker and CR 701.17b (mill), Swords to Plowshares (exile → its controller), Break the Spell and an
indestructible target (destroy), Victimize (sacrifice / if you do), Recoil (return → that player), Turntimber Ranger
(token / if you do), Syphon Soul (damage dealt this way), Provoke the Trolls (creature dealt damage this way, and a
player target naming none), Gonti's Machinations (life lost this way), a 0-life gain, counters put and Living Artifact's
"remove a counter. If you do"; plus "if you don't", a later same-kind step replacing the earlier, and the refused
duplicate subjects. `MillEffectHandlerTest` verifies the including-exiled call. Oracle text of every named card checked
with `get_card`.

**Test runs**: application selection (`StepResultsTest`, `EffectResolutionServiceTest`, `ResolutionScratchpadAmountTest`,
`SubjectResolutionServiceTest`, `LibraryChoiceHandlerServiceTest`, `EffectCountRatchetTest`, the handler tests of every
touched handler and every application test naming them or `GraveyardService`): 3,553 test cases, all pass after
`MillEffectHandlerTest` was updated; card tests (`--tests-for` of the 19 recording records, 25 per class, plus Dreamcatcher,
Baral, Fold into Aether, Shiko and Narset): 511 suites, 3,914 test cases, all pass; `GameDataDeepCopyTest` 51, all pass.
Application, AI, engine testFixtures and card-test `b`, `d`, `f`, `s` test sources compile. Ratchet unchanged (no
`model/effect` record or handler added or removed; the new types are in `model/`, `model/condition`, `model/amount`).

**Handlers that may be an earlier step** (rule added to `architecture/resolution.md`):

- Never prompt: `GainLifeEffect`, `LoseLifeEffect` (Lich's Duel Mastery's question reruns it before anything changes),
  `ExileTargetPermanentEffect`, `DestroyTargetPermanentEffect` (`AsksDeathReplacementsFirst`: asked before anything
  changes), `CopyTriggeringSpellEffect` (records before the new-target choice), `CreateTokenEffect` (a token-creation
  replacement question reruns it before anything is created), `MillEffect` (journal reruns replay the milled cards),
  `DrawCardEffect`.
- Pause at the end of the step with the result recorded by then: `SacrificeSelfEffect` (`departAsStep`),
  `DiscardEffect`, `SacrificePermanentsEffect` (results added as the prompts are answered),
  `PutCountersOnSelfEffect` / `PutCounterOnTargetPermanentEffect` (counter-replacement order),
  `CounterSpellEffect` (the top-or-bottom choice comes after the record).
- Only on some paths: `DealDamageTo{AnyTarget,Players,TargetCreature}Effect` are safe unless a replacement-order prompt
  reruns them after player damage was dealt (a replayed player hit reports 0 dealt, so it is lost from the step result;
  permanent damage is replayed); `ReturnCardFromGraveyardEffect` only on the pre-targeted, topmost, return-all and random
  paths (the choose-at-resolution completions record nothing); `PutCounterOnTargetPermanentEffect` not with its
  predicate (choose your own permanent) path; `RemoveCounterFromTargetPermanentEffect` not when the controller picks the
  kind of counter; `ReturnToHandEffect` not when a commander's zone choice is pending.
- Everything else, and every `rerunCurrentEffectAfterInteraction` user (194 files), is `blocked-step-model` as an earlier
  step.

**For later runs.**

- Step 8 reads results only through these: "if you do" = `ConditionalEffect(new HappenedThisWay(kind), then)` after the
  step, "that many" = `AmountThisWay(kind)`, "its controller" of what a step exiled / destroyed / returned =
  `playersThisWay(kind)` (not `controllerOf(target())` when the card means the target whether or not it was removed:
  Victorious Destruction's "its controller loses 1 life" has no "if", so by its text it happens even if the land is
  indestructible; no ruling says otherwise).
- Damage, discard and sacrifice results are added by central hooks once a step of that kind began: damage, discards or
  sacrifices from a later handler that records no begin are added to the latest step of that kind. Make a new
  damage/discard/sacrifice primitive call `recordStep(kind, NONE)` first.
- A declined `MayEffect` runs no step and records nothing; an "if you do" after it would read an older step of that
  kind. No card has two same-kind steps around a may yet; record NONE on decline if one appears.
- Turntimber Ranger's `MayEffect(SequenceEffect.of(CreateTokenEffect, PutCountersOnSourceEffect))` has no "if you do"
  gate; step 8 should gate it with `HappenedThisWay(CREATE_TOKEN)`.
- The in-resolution `eventValue` writers (`rememberAmount`, `recordDamageDealt`, `recordTargetLifeLost`,
  `recordSacrificedCount`) now have a typed twin; steps 8–9 move their cards to `AmountThisWay` and drop the flags.

### Step 8, run 1 (2026-11-20)

The wrappers with a `thenEffect`, smallest and self-subject first. Verdicts (ledger, step `8`): **split 6**, **blocked-step-model
8**, **unique 1**, blocked-player-loop 0.

- **Split** (records and handlers deleted; ratchet 4,858 / 4,091 → 4,852 / 4,085):
  `SacrificeSelfThenEffect` (62 cards, script) → `SequenceEffect.of(new SacrificeSelfEffect(), new ConditionalEffect(HappenedThisWay.sacrificed(), X))`,
  `.reflexive(X)` → `X` in a `QueueReflexiveAbilityEffect`, `.ofTriggeringPermanent` → `SacrificeSelfEffect(Subjects.triggeringOrSource())`;
  `SacrificeGrantingEquipmentThenEffect` (2) → `SacrificeSelfEffect(Subjects.granter())` + gate;
  `ExileSelfThenEffect` (2) → `ExileTargetPermanentEffect(source())` + `HappenedThisWay.exiled()`;
  `ReturnSelfToHandThenEffect` (2) → `ReturnToHandEffect.self()` + `HappenedThisWay.returned()`;
  `MillControllerThenEffect` (4) → `SequenceEffect.of(MillEffect(N, you()), ConditionalEffect(HappenedThisWay.atLeast(MILL, N), QueueReflexiveAbilityEffect(X)))`
  for "mill N. When you do", a plain `SequenceEffect.of(MillEffect, X)` for Fell Gravship's "then";
  `DrawCardsThenEffect` (1) → `DrawCardEffect(4)` + `ConditionalEffect(HappenedThisWay.atLeast(DRAW, 4), QueueReflexiveAbilityEffect(X))`.
  Every first half is on step 7's earlier-step list. Target specs: the sequence's first declared target is the payload's,
  as the wrapper's was; a sequence with none keeps `SacrificeSelfEffect`'s self-targeting spec (`SacrificeSelfEffect` now
  gives `triggeringOrSource()` that spec too, as the wrapper had). `targetsChosenByReflexiveAbility` comes through
  `ConditionalEffect`. No AI class named the split records.
- **Survivor changes.** `HappenedThisWay` gained `minimum` (default 1; `atLeast(kind, n)`) and `exiled()` / `returned()` /
  `created()`. `SacrificeSelfEffect` accepts `triggeringOrSource()` and `granter()`. `MayEffectHandler` keeps the wrapper's
  "a permanent you can't sacrifice isn't offered" check, now for `MayEffect(SequenceEffect.of(SacrificeSelfEffect(source() |
  triggeringOrSource()), ConditionalEffect(HappenedThisWay(SACRIFICE), …)))`; Dreamcatcher has that shape, so it is no
  longer asked after losing control of it (same outcome; `DreamcatcherTest.noSacrificeMeansNoDraw` updated). Necrite's
  ungated `SequenceEffect.of(SacrificeSelfEffect(), Destroy...)` is not that shape and is still asked.
  `FlipCoinWinEffectHandler`'s synchronous branch dispatch judges a `ConditionalEffect` (Negative Zone Portal, Sorcerer's
  Strongbox-style branches); `FlipCoinWinEffectHandlerTest.conditionalStepOfBranchIsGated`. `ExileDyingCreatureCardAndCreateTokenEffectHandler.bindReturn`
  descends into `ConditionalEffect`. `SacrificeSelfThenDealDamageToEnteringCreatureEffectHandler` (Flame-Kin War Scout) dispatches
  `SacrificeSelfEffect` and reads its SACRIFICE outcome. `Card.bindEffectToTargetGroup` and `TriggeredAbilityQueueService`
  lost their wrapper cases (sequence and conditional cases cover them).
- **Behaviour changes (Oracle text).** Fell Gravship (EOE 101) "mill three cards, then return …": the return was a reflexive
  ability; it is now part of the trigger's resolution (`FellGravshipTest.millingAndReturningOccurDuringTheSameTriggerResolution`
  fails on the old code). Trickster's Talisman (AFR 79) "If you do, create a token": `SacrificeGrantingEquipmentThenEffect`
  made every payload reflexive; the copy is now made as the trigger resolves (`TrickstersTalismanTest.copyIsCreatedDuringTheTriggersResolution`).
  Turntimber Ranger's counter is gated on `HappenedThisWay.created()`; no game state makes a token creation fail today,
  so no test can fail on the old card (the accept test covers the gated path). The survivor's log wins: Impaler Shrike's
  "fizzles — source no longer on the battlefield" line is gone (`ImpalerShrikeTest` now asserts no sacrifice and no draw).
- **"Mill N. When you do"** (Yathan Roadwatcher TDM 236, The Last Ronin TMT 154, Kraven's Last Hunt SPM 105): gated with
  `HappenedThisWay.atLeast(StepKind.MILL, N)`. The Yathan and Last Ronin rulings say the reflexive ability "triggers when you
  mill four cards this way" (Kraven has no rulings and the same wording), and The Royal Scions' ruling says fewer cards than
  written don't trigger; so a library shorter than N no longer triggers it (the old wrapper queued it always). The tests
  that asserted a short-library trigger were changed to full libraries, and each card has a short-library test that
  nothing triggers (`YathanRoadwatcherTest.shortLibraryDoesNotTriggerTheReturn`,
  `TheLastRoninTest.chapterIIShortLibraryDoesNotTrigger`, `KravensLastHuntTest.chapterIShortLibraryDoesNotTrigger`). No other
  card has "mill N. When you do".
- **Ungated sacrifices.** The 20 cards that start a sequence with a bare `SacrificeSelfEffect`, by Oracle text: "if you do"
  on Necrite (5ED 181) and Search the City (RTR 49, whose ruling says a Search the City that wasn't sacrificed gives no extra
  turn): both gated with `HappenedThisWay.sacrificed()` (`NecriteTest.noDestroyWhenNecriteLeavesBeforeResolution`,
  `SearchTheCityTest.notSacrificedMeansNoExtraTurn`, both failing on the old cards; Necrite's stolen-source test no longer
  answers a prompt, as the can't-sacrifice check now applies to it). The other 18 read "sacrifice X and Y", "sacrifice X,
  [then] Y" or "sacrifice X. Y" (Timothar's Bat, The Heron Moon, Tellah, Sothera, Sorcerer's Strongbox, Second Chance,
  Robot Domination, Political Triumph, Planar Collapse, Monkey Cage, Mogg Bombers, Measure of Wickedness, Impending
  Disaster, Hellish Rebuke, Defense of the Heart, Dawn of a New Age, Daredevil Dragster, Brink of Madness): left ungated,
  as the Mogg Bombers and Second Chance rulings require (the second part happens even if the sacrifice didn't;
  `SecondChanceTest.extraTurnEvenIfNotSacrificed`).
- **Blocked-step-model** (first half not on step 7's list, or a bespoke continuation): `TapSourceThenEffect`,
  `TapTargetThenEffect` (tap has no `StepKind`), `SurveilThenEffect` (already splices `SurveilEffect` + continuation; adding
  surveil to the list would split it), `PutCardToBattlefieldThenEffect`, `ShuffleCardsFromHandIntoLibraryThenEffect`
  (choice continuations), `ShuffleTargetPermanentIntoLibraryEffect`, `ShuffleSelfIntoOwnerLibraryEffect`,
  `PutTargetPermanentIntoLibraryNFromTopThenEffect` (library moves have no `StepKind`). Common cause: no recorded step result
  for tap / surveil / shuffle-into-library, or a prompt with a bespoke continuation.
- **Unique**: `CreateTokenThenEffect`: its "when you do" triggers once per written quantity of tokens made (CR 603.12a, checked
  2026-11-20) and `aboutThatToken` binds each token; a gate step can't count occurrences.
- **`ThenEffectRecipient` stays**: its readers are `DestroyTargetPermanentThenEffect`, `ExileTargetPermanentThenEffect`,
  `ReturnTargetPermanentToHandThenEffect`, `DestroyCombatOpponentAtEndOfCombatEffectHandler` and the three blocked library
  records.

**Tests**: card tests of every rewritten card plus the cards of `SacrificeSelfThenDealDamageToEnteringCreatureEffect`,
`ExileDyingCreatureCardAndCreateTokenEffect`, `FlipCoinWinEffect` and every `MayEffect(SequenceEffect.of(new SacrificeSelfEffect(), …))`:
120 classes, 1,180 test cases, all pass; after the follow-up (mill gates, Necrite, Search the City, Second Chance) those
six classes again: 56 test cases, all pass. Application: `StepResultsTest` (+2, `atLeast`), `ReflexiveRouteRecordTest` (`DRAW_THEN`
rebuilt from the steps), `FlipCoinWinEffectHandlerTest` (+1), `MayEffectHandlerTest`, `EffectResolutionServiceTest`,
`SubjectResolutionServiceTest`, `EffectCountRatchetTest` and the 9 other application tests naming `SacrificeSelfEffect`,
`HappenedThisWay` or `FlipCoinWinEffect`: 534 test cases, all pass. Application, AI, engine testFixtures and the touched
card-test modules compile.

**For later runs.**

- Not audited here, and what each needs first: `DestroyTargetPermanentThenEffect` (89 cards), `ExileTargetPermanentThenEffect`
  (25), `ReturnTargetPermanentToHandThenEffect` (12): their riders for "its controller" (`TARGET_CONTROLLER*`) include verbs
  with no `PlayerSubject` (`CreateTokenEffect`, `SearchLibraryEffect` with its `MayEffect` binding, `SeekEffect`,
  `SurveilEffect`, `ManifestDreadEffect`, `CreateTokenCopyOfCardEffect`, `TargetPlayerExilesFromHandEffect`, `ProliferateEffect`),
  their `EventStat` snapshot needs last-known amounts of the removed target (toughness, power, mana value, counters; the
  replaced-destruction case reads the survivor), `thenCondition` needs a last-known "it was a …" condition, Destroy's
  `ResolveRiderForPlayerEffect` rider steps and requires-death check need a replacement, Exile's leave batch makes both
  leave together (CR 603.10a), and `TargetPolarityClassifier` names all three (HARMFUL_REMOVAL; `ReturnToHandEffect` is
  benign). Give those verbs a player subject first (a step-4-style run), then split per card. `DiscardCardThenEffect` (70):
  `DiscardEffect` needs a card filter and a "the discarded card was a …" condition (and the alternate-type branch) first.
- `HappenedThisWay.atLeast` is the gate for any "when you [do N things] this way" (The Royal Scions, the three mill cards).

### Step 8, run 2 (2026-11-20)

The four target "then" wrappers. Verdicts (ledger, step `8`): **split 0 classes** (78 of their cards split, no wrapper
emptied), **blocked-other 3** (`DestroyTargetPermanentThenEffect`, `ExileTargetPermanentThenEffect`,
`ReturnTargetPermanentToHandThenEffect`, each keeping the cards below), **blocked-step-model 1** (`DiscardCardThenEffect`),
unique 0. Ratchet unchanged (4,852 / 4,085: no record or handler deleted).

- **No new prerequisite was needed for the split cards.** The riders that matter already take a `PlayerSubject`
  (life, damage, draw, discard, mill, poison), and the last-known reads exist: `controllerOf(target())` reads the
  controller `DestroyTargetPermanentEffect` / `ExileTargetPermanentEffect` / `ReturnToHandEffect` record as they remove
  the target, `ownerOf(target())` the remembered card's owner, `TargetPower` / `TargetToughness` / `TargetManaValue`
  the target's last-known stats (the live permanent when the destruction was replaced, as the wrapper's survivor
  read), `TargetPermanentMatches` the departed snapshot (Thermokarst's existing pattern). Shapes (card text checked
  with `get_card`):
  - "Its controller loses / gains / draws / discards / mills / gets a poison counter", "~ deals N damage to its
    controller": removal, then the rider with `controllerOf(target())` (`ownerOf` for Path of Peace and Misfortune's
    Gain): Bitter Downfall, Despoil, Exterminate, Foolish Fate, Glissa's Scorn, Hideous End, Spreading Rot, Victorious
    Destruction, Flesh Allergy, Cindervines, Consign to the Pit, Cryoclasm, Destructive Revelry, Grotag Siege-Runner,
    Melt Terrain, Minion Missile, Peak Eruption, Smash to Smithereens, Viashino Heretic, Agonizing Demise, Crumble,
    Grisly Spectacle, Nature's Claim, Pistus Strike, Ob Nixilis the Hate-Twisted, Assassin's Strike, Unlicensed
    Disintegration, Common Black Removal's mill mode; Ajani Inspiring Leader, Lay Down Arms, Ajani Unyielding,
    Solitude, Introduction to Annihilation, Triad of Fates, Season of the Burrow, Structural Distortion; Recoil,
    Dinrova Horror, Compelling Deterrence (owner discards, caused by the caster).
  - "You gain / lose life equal to its …", "+X/+0 where X is its mana value", "X Saprolings": Divine Offering,
    Terashi's Grasp, Garruk Apex Predator, Feed the Swarm, Orzhov Charm, Vendetta, Devour in Shadow, Hoard-Smelter
    Dragon, Artifact / Aura / Death Mutation, Exile.
  - Plain "you" riders: Common Black Removal's other modes, King Macar, Forum Familiar, Alora's four.
  - "If it was …": `ConditionalEffect(TargetPermanentMatches(...))` for colour (last-known colours), snow (the
    supertype LKI branch), mana value, counters, "you controlled it" (the recorded controller), attacking, Gideon /
    Nissa / Liliana: Icequake, Filigree Fracture, Gloomwidow's Feast, Gloomlance, Raze to the Ground, Carnivorous
    Canopy, Tainted Treats, Rite of the Serpent, Kellan, Nissa's / Liliana's / Gideon's Defeat, Consuming Ashes,
    Boomerang Basics, Geistwave, Desculpting Blast.
  - "If … destroyed / returned / exiled this way", "if you do": `HappenedThisWay`: Acolyte Hybrid, Noxious Gearhulk
    (its "if a creature" is the target filter, so only the destroyed gate), King Macar, Mirkwood Nurturer, Alora.
  - "Exile this creature and target creature": `ExileTargetPermanentEffect(allOf(source(), target()))` (Giant Trap
    Door Spider, Hunting Kavu, Mangara of Corondor).
- **Survivor changes.** `ExileTargetPermanentEffectHandler`'s subject path exiles several permanents inside one
  permanent-leave batch (CR 603.10a, as the wrapper did) and records the target part's controller, card, power and
  toughness (`ExileTargetPermanentEffectHandlerTest.exilesSourceAndTargetTogether`). `DestroyTargetPermanentEffectHandler`
  and the exile target path also record the target's last-known toughness (the removal service records it only for
  entries on the stack, so a resolving loyalty ability read 0: Garruk's tests caught it). `TargetManaValue` reads a
  departed target's snapshot through `getPermanentManaValue` (a transformed permanent's front-face mana value, face
  down 0; `ViashinoHereticTest.transformedArtifactDealsDamageUsingFrontFaceManaValue`).
- **Behaviour changes (Oracle text).** Alora's four modes and Mirkwood Nurturer read "If you do": gated on
  `HappenedThisWay.returned()` (no game state the tests reach makes a return fail, so no test fails on the old cards).
  Gloomlance's discard is now caused by the spell's controller (the wrapper made the target's controller the entry
  controller), so opponent-caused discard replacements apply. `ownerOf(target())` reads the
  card's owner, not the wrapper's `defaultControllerOf`; `PathOfPeaceTest.ownerGainsLifeForStolenCreature` now gives
  its hand-built card an owner, as every card in a real game has.
- **AI.** `TargetPolarityClassifier` gives `DestroyTargetPermanentEffect`, `ExileTargetPermanentEffect` and
  `ReturnToHandEffect.target()` HARMFUL_REMOVAL, as its name entries gave the wrappers, and a sequence takes its
  strongest step, so the target choice is unchanged. `SpellEvaluator`, `InstantCategoryClassifier` and
  `BoardEvaluator` now see the split cards as single-target removal (`RemovalEffect.removalKind()`), which the
  wrappers (not `RemovalEffect`) were never scored as.
- **Kept, by cause** (what a later run needs):
  - a rider with no player subject: "its controller may search for a basic land" (13 Destroy cards, Path to Exile,
    Winds of Abandon; `SearchLibraryEffect.searchPlayer` is the `LibrarySearchPlayer` enum: convert it to a
    `PlayerSubject` step-4 style, with `MayEffect` chooser `TARGET_PERMANENT_CONTROLLER`), "its controller creates a
    token" (Get Lost, Wasteland playtest, Ravenform, Resculpt, Secure the Scene; `CreateTokenEffect` needs a controller
    subject: the `tokenForController` fields of the removal records skip `CreateTokenEffectHandler`'s Mirrormind Crown
    / Moonlit Meditation / Esix choices, so they are not a substitute), Dwarven Confluencer (token copy), Unwanted
    Remake (manifest dread), Transforming Flourish (exile-top-until), Excise the Imperfect (incubator), Fragment
    Reality and Obscura Polymorphist (seek), Ashiok (`TargetPlayerExilesFromHandEffect`, a subject-sibling group with
    `EachOpponentExilesFromHandEffect` / `EachPlayerExilesFromHandEffect`);
  - "if it was a …" of a type or subtype layers can change (Death's Caress Human, Shredder's Technique enchantment,
    Smashing Success artifact, Rescue artifact, Zephyr Sentinel Soldier) and "nonbasic" through a not-predicate
    (Choking Sands, Molten Rain, Helldozer): `TargetPermanentMatches` judges the departed snapshot without its layered
    state (only colours and a direct supertype predicate have last-known fields), so Maskwood Nexus, Liquimetal
    Coating or Arcum's Weathervane would be missed; needs the effective last-known card on the departed snapshot;
  - "if it was dealt damage this turn" (Faller's Faithful, Sold Out): the damaged-this-turn sets forget a departed card;
  - "dies this way" (Cinder Cloud, Kaervek's Purge, Tuktuk Scrapper): no DIED result (DESTROY counts a replaced
    destruction);
  - Break the Spell (any-of controlled-or-token after the destroy: the controlled special case matches only a bare
    `PermanentControlledBySourceControllerPredicate`); target-player scopes after the destroy (Feast of Worms, Public
    Execution, Impose Hierarchy, Roiling Terrain); counter totals (Parting Thoughts, Flay Essence:
    `CountersOnTargetPermanent` has no last-known read); Engulfing Slagwurm (a non-targeting departed blocker).
  - `DiscardCardThenEffect`: 46 of 72 cards are "discard. When you do" (reflexive); the 26 "if you do" ones need
    `DiscardEffect` to take a card filter and `LastDiscardedCard*` / "the discarded card was a …" read from the
    DISCARD result, and `SpellEvaluator` scores a bare `DiscardEffect(you())` as card loss, so an AI check comes with it.
- `ThenEffectRecipient` stays: the three wrappers, `DestroyCombatOpponentAtEndOfCombatEffectHandler` and the three
  blocked library records read it.

**Tests**: card tests of the 78 rewritten cards: 80 suites, 648 test cases, all pass (after the toughness, mana-value
and Path of Peace fixes above; the eight layer-sensitive cards were put back on their wrappers after Zephyr Sentinel's
Maskwood Nexus test failed). Selection: `--tests-for DestroyTargetPermanentEffect ExileTargetPermanentEffect
ReturnToHandEffect --max-per-class 15`, every card naming `TargetManaValue` / `TargetToughness`, the rewritten cards,
`ExileTargetPermanentEffectHandlerTest` (+1), `DestroyTargetPermanentEffectHandlerTest`, `StepResultsTest`,
`SubjectResolutionServiceTest`, `ResolutionScratchpadAmountTest`, `EffectCountRatchetTest`, AI
`TargetPolarityGuardTest`: 159 suites, 1,295 test cases, all pass. Application, AI, engine testFixtures and the touched
card-test modules compile.

### Step 8, run 3 (2026-11-20)

Unlocked and split the three largest groups run 2 left on the target wrappers. Verdicts (ledger, step `8`): **split 0 classes**
(28 more of the wrappers' cards split, no wrapper emptied), the three wrappers stay **blocked-other** with new notes,
`LibrarySearchPlayer` **absorbed** (enum deleted), `SearchLibraryEffect` / `SearchTargetLibraryEffect` **survivor**. Ratchet
unchanged (4,852 / 4,085: no `model/effect` record or handler deleted; the enum lived in `model/`).

- **`LibrarySearchPlayer` → `PlayerSubject`** (step-4 style). `SearchLibraryEffect.searchPlayer` and
  `SearchTargetLibraryEffect.searchPlayer` are `PlayerSubject`s; 19 card files rewritten by a deleted script: `CONTROLLER` →
  `you()`, `ACTIVE_PLAYER` → `activePlayer()` (Oath of Lieges; the old read was the entry's recorded active player, the same
  player during the upkeep trigger), `TRIGGERING_PERMANENT_CONTROLLER` → `controllerOf(triggering())` (Verdant Succession,
  Pattern of Rebirth, Murasa: the `MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER` chooser already wrote that player into the
  baked id the old switch read), `TARGET_PLAYER` → `targetPlayer()` on `SearchLibraryEffect` (Jacob Frye, Evie Frye, Blue: it
  declares the benign player target the old `targetSpec` declared) and `targetPlayerDeclaredElsewhere()` on
  `SearchTargetLibraryEffect` (seven partner cards; that record declares its own player target, and its compact constructor
  now accepts only `you()` or that). `SearchLibraryEffectHandler` resolves the searcher through `SubjectResolutionService`;
  `SearchLibraryEffect.searchedBy(subject)` is the wither. AI: no AI class reads either record.
- **"Its controller may search"** (Assassin's Trophy, Avengers Disassembled, Boseiju, Cleansing Wildfire, Erode, Geomancer's
  Gambit, Ghost Quarter, Magmatic Hellkite (no "may"), Path to Exile, Price of Freedom, Sandworm, Volatile Fault, White Orchid
  Phantom, Dire-Strain Rampage, Winds of Abandon): removal primitive, then
  `MayEffect(SearchLibraryEffect(...).searchedBy(controllerOf(target())), prompt, null, MayChoicePlayer.TARGET_PERMANENT_CONTROLLER)`
  (flat on spells, `SequenceEffect.of` on triggers, abilities and modes; split by a deleted script). The chooser reads the
  controller the removal recorded, which is what the wrapper bound by identity. Dire-Strain Rampage:
  `ConditionalEffect(AllOf(HappenedThisWay.destroyed(), TargetPermanentMatches(land)), may search for two)` and its
  `NotCondition` with the one-card search (`EventStat.BASIC_LAND_SEARCH_COUNT` deleted; the prompts now say one or two cards).
  Winds of Abandon's "for each creature exiled this way" is gated on `HappenedThisWay.exiled()` (the wrapper didn't gate; no game
  state the tests reach makes the exile fail). The wrappers' bespoke may-search binding branches are deleted.
- **"Its controller creates a token"** (Get Lost, Waste Land playtest, Ravenform, Resculpt, Secure the Scene):
  `CreateTokenEffect(...).forController(controllerOf(target()))`. `CreateTokenEffect` gained a last component
  `PlayerSubject controller` (default `you()`, every old constructor kept, every wither carries it); its handler resolves it and
  runs `resolveForController` for that player, so Mirrormind Crown, Moonlit Meditation and Esix are offered to the token
  controller, and an accepted replacement makes that player create the copies (`createCopiesOfHost(..., tokenControllerId)`,
  `CreateTokenCopyOfChosenCreatureEffectHandler.resolveFor`). Test: `GetLostTest.controllersMirrormindCrownReplacesTheMaps`.
  AI parity: `TokenCreatingEffect.tokenController()` (default `you()`); `SpellEvaluator`'s four token reads score only `you()`
  tokens, so the split cards don't count the opponent's tokens as the caster's (the wrappers weren't `TokenCreatingEffect`).
- **Layered last-known types.** A departure snapshot now carries `Permanent.lastKnownCard`, the effective card the removal
  already built (types, subtypes, supertypes, colours, keywords with the layers applied), and `TargetPermanentMatches` judges a
  departed target as that card (CR 608.2h, checked with `get_rule` 2026-11-20). The old supertype-only branch is the
  fallback for a snapshot without it. Copied by `Permanent(Permanent)`; `GameDataDeepCopyTest.deepCopyKeepsDepartedSnapshotLastKnownCard`.
  Split with it: Death's Caress (`TargetToughness`), Shredder's Technique and Smashing Success (`AllOf(destroyed(), type)` for
  "if an … was destroyed this way"), Rescue, Pepper Potts and Zephyr Sentinel (`ReturnToHandEffect.target()` then the check),
  Choking Sands and Molten Rain (damage to `controllerOf(target())`), Helldozer. Tests: `ZephyrSentinelTest`'s Maskwood Nexus
  case passes on the split card; `DeathsCaressTest.humanThroughMaskwoodNexusGainsLife` (new).
- **Test changes.** `CreateTokenEffectHandlerTest` and `SearchLibraryEffectHandlerTest` wire a mocked `SubjectResolutionService`
  (answers the entry's controller); the Moonlit Meditation case verifies the new `createCopiesOfHost` overload.

**Tests**: card tests of the 47 rewritten cards, every card naming the three wrappers or `SearchTargetLibraryEffect`, a
25-per-class sample of `SearchLibraryEffect` and `CreateTokenEffect`, and every card naming `TargetPermanentMatches`,
`CreateTokenCopyOfChosenCreatureEffect`, Mirrormind Crown, Moonlit Meditation, Esix, Liquimetal Coating or Maskwood Nexus:
302 suites, 2,551 test cases, all pass. Application (every test naming the touched records, conditions or handlers, plus
`EffectCountRatchetTest`, `StepResultsTest`, `SubjectResolutionServiceTest`): 133 suites, 2,868 test cases, all pass after the
`CreateTokenEffectHandlerTest` verify update; AI `SpellEvaluatorTest`, `GameDataDeepCopyTest`, `TargetPolarityGuardTest`: 150,
all pass. Application, AI, engine testFixtures and the touched card-test modules compile.

**For later runs.**

- Left on the wrappers (ledger notes): Destroy 14 (manifest dread, token copy and exile-top riders; dies-this-way; dealt-damage
  LKI; target-player sets after the destroy; counter totals; Break the Spell; Engulfing Slagwurm), Exile 5 (seek, incubate,
  counters LKI, dealt-damage LKI), Return 1 (Ashiok). `ThenEffectRecipient` stays for them, `DestroyCombatOpponentAtEndOfCombatEffectHandler`
  and the three blocked library records.
- A rider whose verb has a player subject now splits with no wrapper; `forController` / `searchedBy` are the patterns for a verb
  whose actor is a component. `TargetPermanentMatches` on a departed target is layer-correct, so "if it was a <type>" splits.

### Step 8, run 4 (2026-11-20)

The general fused families, by `--unaudited --family Deal` restricted to `Then` / `And` / `IfYouDo` / `Unless` names: of
the 33 such Deal records, 16 are "damage, then a rider on what was dealt damage", and every first half is a damage
primitive on step 7's earlier-step list (they never prompt but for a replacement-order rerun, which replays permanent
damage). Verdicts (ledger, step `8`): **split 12**, **blocked-other 4**, blocked-step-model 0, blocked-player-loop 0,
unique 0. Ratchet 4,852 / 4,085 → 4,840 / 4,073 (12 records, 12 handlers deleted). The other 17 Deal names (the "And"
simultaneous sets such as `DealDamageToTargetAndTheirCreaturesEffect`, the "Unless" pay-or-take ones, Trial of Agony,
Chaos Balor) are left unaudited.

- **Split** (card text checked with `get_card`; flat effects on spells and activated abilities, `SequenceEffect.of` on
  the two trigger slots): Provoke the Trolls (`BoostSelfEffect(thisWay(DEAL_DAMAGE, creature))`), Ballista Wielder
  (`CantBlockThisTurnEffect(thisWay(DEAL_DAMAGE, creature))`), The Black Arrow and Silver Bolt
  (`DestroyTargetPermanentEffect(thisWay(DEAL_DAMAGE, Dragon | Werewolf))`), Pathway Arrows
  (`TapPermanentsEffect(thisWay(DEAL_DAMAGE, colorless creature))`), Burn from Within (damage with `exileInsteadOfDie` +
  `RemoveKeywordEffect(thisWay(DEAL_DAMAGE, creature))`), Spikefield Hazard (damage +
  `MarkTargetCreatureExileInsteadOfDieThisTurnEffect(thisWay(DEAL_DAMAGE))`), The War Doctor (damage with
  `exileInsteadOfDie`), Enter the God-Eternals and Rite of Consumption (`GainLifeEffect(AmountThisWay(DEAL_DAMAGE))`),
  Marauding Raptor and Redcap Melee (`ConditionalEffect(HappenedThisWay.toA(DEAL_DAMAGE, Dinosaur | nonred), …)`),
  Acorn Catapult (`CreateTokenEffect.forController(allOf(controllerOf(target()), targetPlayerDeclaredElsewhere()))`). The
  damage step's recorded permanents are the ones actually dealt damage, redirected damage included, which is what the
  fused handlers read from their before/after damage maps. Target specs are unchanged: the damage step is the only one
  that declares a target (the riders read a step result), and its spec is the fused record's.
- **Survivor changes.** `PermanentSubject.ThisWay` gained a `filter` (`Subjects.thisWay(kind, filter)`; the resolver keeps
  the recorded permanents on the battlefield that match it now). `HappenedThisWay` gained a `filter` component
  (`HappenedThisWay.toA(kind, filter)`: a recorded permanent matches now). `CantBlockThisTurnEffect` and
  `RemoveKeywordEffect`'s handler accept a `ThisWay` subject. `MarkTargetCreatureExileInsteadOfDieThisTurnEffect` takes a
  `PermanentSubject` (`target()` or a `ThisWay` step result; the old constructors kept). `DealDamageToTargetCreatureOrPlaneswalkerEffect`
  and `DealDamageToTargetPlayerOrPlaneswalkerEffect` now begin a DEAL_DAMAGE step (`recordStep(DEAL_DAMAGE, NONE)`), as
  step 7 asks of a damage primitive; no card read a DEAL_DAMAGE result before this run.
- **Behaviour changes (Oracle text, rulings).** Smite the Deathless (HOC 202): "That creature loses indestructible … If
  that creature would die this turn, exile it instead" has no "if dealt damage", and its ruling says both still apply when
  the damage is prevented; the fused record applied them only after damage
  (`SmiteTheDeathlessTest.preventedDamageStillRemovesIndestructibleAndExiles`). The War Doctor (WHO 167): "If a creature
  dealt damage this way would die"; the fused record marked any permanent, so a planeswalker it killed was exiled
  (`TheWarDoctorTest.planeswalkerDealtLethalDamageIsNotExiled`). Spikefield Hazard's "a permanent" keeps marking
  planeswalkers. Rite of Consumption gains the damage dealt as the damage step records it, not the life the target lost
  (the same number in every case its tests reach).
- **AI.** Target polarity is unchanged: in every split card the damage step (HARMFUL_DAMAGE) is the strongest step, and the
  riders declare no target (`TargetPolarityGuardTest` passes). `SpellEvaluator` sums a card's steps, so the riders are now
  scored where the fused record was scored only as damage (its damage branch came first): the life gain of Enter the
  God-Eternals and Rite of Consumption, Provoke the Trolls' boost (`CreatureBoostEffect`), and Acorn Catapult's Squirrel is
  no longer counted as the caster's token (`tokenController()` is not `you()`).
- **Blocked, by cause** (none is the step model: each needs a reader or a subject that doesn't exist): Vikya (excess damage
  to any creature dealt damage this way; the damage primitives' excess read is the target's, planeswalkers and battles
  too), Screaming Nemesis (the "can't gain life for the rest of the game" records take no `PlayerSubject`: fold
  `PlayersCantGainLifeRestOfGameEffect` / `TargetPlayerCantGainLifeRestOfGameEffect` first, then
  `playersThisWay(DEAL_DAMAGE)`), Shellshock (a `DynamicAmount` counting the permanents a step result names, and a
  multi-group damage primitive), Minion of Leshrac (an else-effect `DestructionSupport` runs synchronously, not dispatched).

**Tests**: card tests of the 14 rewritten cards (Ballista Wielder through `BallistaWatcherTest`) with the two new tests:
14 suites, 128 test cases, all pass. Samples: every card naming `MarkTargetCreatureExileInsteadOfDieThisTurnEffect`, and 12
per class of `DealDamageToTargetCreatureOrPlaneswalkerEffect`, `DealDamageToTargetPlayerOrPlaneswalkerEffect`,
`RemoveKeywordEffect`, `CantBlockThisTurnEffect`, `DealDamageToAnyTargetEffect`, `DealDamageToTargetCreatureEffect`,
`DestroyTargetPermanentEffect`, `TapPermanentsEffect`, `BoostSelfEffect`, `ConditionalEffect` (with their handler tests):
127 suites, 1,086 test cases, all pass. Application (`StepResultsTest` +4 for the filtered subject and condition, every
test naming the touched records, `ConditionEvaluationService` or `SubjectResolutionService`, `EffectCountRatchetTest`,
`TargetSpecRatchetTest`): 322 suites, 4,254 test cases, all pass; AI (`TargetPolarityGuardTest`, `SpellEvaluatorTest`, the
Easy and Medium decision-engine tests): 305, all pass. Application, AI, engine testFixtures and card-test `a`, `b`, `e`, `m`,
`p`, `r`, `s`, `t` test sources compile.

**For later runs.**

- "If a [kind of permanent] is dealt damage this way" is `HappenedThisWay.toA(DEAL_DAMAGE, filter)`; "it" / "that
  creature" after it is `thisWay(DEAL_DAMAGE, filter)`. A verb that should read one needs to accept `ThisWay` (most take any
  subject through the resolver; `CantBlockThisTurnEffect`, `RemoveKeywordEffect` and the exile mark had a whitelist).
- "A creature dealt damage this way would die … exile it instead" is the damage effect's `exileInsteadOfDie` flag (creatures
  only); "a permanent" is the mark with `thisWay(DEAL_DAMAGE)`.
- Next Deal candidates: the "And" simultaneous sets (`DealDamageToTargetAndTheirCreaturesEffect` 8 cards,
  `…AndUpToCreaturesThatPlayerControls`, `…AndSharingCreatures`, `…AndAllWithSameName`): one damage event, so
  `SequenceEffect.simultaneous` with a set damage subject, after checking which mass-damage record takes a
  `controlledBy(targetPlayer())` subject. The same reading likely unlocks the `DealDamageToAnyTargetThenPerpetually…`
  pair once `Perpetually*` grants accept a step-result subject.

### Step 8, run 5 (2026-11-20)

The other 17 `--unaudited --family Deal` records restricted to `Then` / `And` / `IfYouDo` / `Unless` (run 4 left them). Ten are
"damage to X and to a set": every first half is a damage primitive on step 7's earlier-step list, and the set half is
`DealDamageToPermanentsTargetControlsEffect` with a set subject, added beside the target's damage as one damage event
(CR 120.3 / 120.4 and 615.8 checked with `get_rule` 2026-11-20). Verdicts (ledger, step `8`): **split 3**, **absorbed 2**,
**blocked-step-model 4**, **blocked-other 6**, **unique 2**, blocked-player-loop 0. Ratchet 4,840 / 4,073 → 4,835 / 4,068
(5 records, 5 handlers deleted). No `Deal` record with those name parts is left unaudited.

- **Split** (card text checked with `get_card`; spells `target(...).bindEffect(targetDamage)` + `addSimultaneousSpellEffects`,
  abilities and modes `SequenceEffect.simultaneous`): `DealDamageToTargetAndSharingCreaturesEffect` (Cleansing Beam, Wojek
  Embermage, Brightflame), `DealDamageToTargetCreatureAndAllWithSameNameEffect` (Homing Lightning, Izzet Staticaster),
  `DealDamageToTargetAndCreaturesSharingCreatureTypeEffect` (Killer) → `DealDamageToTargetCreatureEffect(N)` +
  `DealDamageToPermanentsTargetControlsEffect(N, otherCreaturesSharing(target(), COLOR | NAME | CREATURE_TYPE))`. Brightflame's
  "you gain life equal to the damage dealt this way" is `GainLifeEffect(AmountThisWay(DEAL_DAMAGE))` (its ruling: the total
  dealt to all creatures, prevented damage not counted), replacing the handler's `eventValue` write.
- **Absorbed** (not fused; a set the survivor now names): `DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffect` →
  `controlledBy(eachOpponent(), creature or planeswalker)` (creature only for Delayed Blast Fireball);
  `DealDamageToEachCreatureAndPlaneswalkerDamagedPlayerControlsEffect` → `EventValue` with
  `controlledBy(triggeringPlayer(), creature or planeswalker)` in Incinerator of the Guilty's reflexive ability (the baked
  damaged player). The planeswalker check is now the layered predicate (the old one read the printed card's type).
- **`DealDamageToTargetAndTheirCreaturesEffect`**: 7 of 8 cards split (Flame Wave, Lavalanche, Bonfire of the Damned, Chandra's
  Flame Wave, and the Nalaar, Pyrogenius and Flame's Fury ultimates) into `DealDamageToTargetPlayerOrPlaneswalkerEffect(amount)` +
  `DealDamageToPermanentsTargetControlsEffect.toCreaturesOfTargetedPlayer(amount)`, which leaves out the targeted planeswalker
  (the old handler skipped it, so an animated planeswalker is dealt the damage once). The record stays for Heart of Bogardan:
  `DestructionSupport.resolveForcedCostElseEffects` runs it synchronously with an untargeted opponent (blocked-other, like
  Minion of Leshrac).
- **Survivor changes.** `DealDamageToPermanentsTargetControlsEffect` takes a `DynamicAmount` (int constructors kept, evaluated
  once with the source as it is or last was), accepts an `otherCreaturesSharing` subject as well as `controlledBy`, begins a
  DEAL_DAMAGE step, and gained `toTargetedPlayers(DynamicAmount, …)` and `toCreaturesOfTargetedPlayer`. New subject
  `PermanentSubject.OtherCreaturesSharing(permanent, SharedQuality)` / `Subjects.otherCreaturesSharing` (declares no target;
  `withCreaturesSharingAColor` stays for Surge of Zeal and Rally the Righteous). `SubjectResolutionService` binds a
  `PermanentIsTargetCardPredicate` leaf of a one-shot `controlledBy` filter to the entry's target (as `lastingRule` already did;
  no one-shot user had it before). `DamageSupport.dealSimultaneousDamage` reinstates a "prevent the next time this source would
  deal damage" shield (`SourceNextDamageToAnyTargetShield`, unlimited) for each later recipient of the event and ends it with
  the event, which only `DealDamageToTargetAndTheirCreaturesEffectHandler` and combat damage did: a shield on Chandra's Fury's
  or Chandra Nalaar's source now prevents the whole event. Tests: `SubjectResolutionServiceTest` (+5: the three qualities, a
  gone target, the target-card binding), `ChandraNalaarTest.minusEightNextDamageShieldPreventsTheWholeEvent`.
- **Behaviour change (Oracle text).** Goblin Chainwhirler and Sarkhan's Unsealing: "deals N damage to each opponent and each
  creature and planeswalker they control" is one damage event; their two effects were added with `addTrigger` (two events)
  and are now `addSimultaneousTrigger`. `GoblinChainwhirlerTest.etbDamageIsOneEventForANextDamageShield` fails on the old card.
- **AI.** The split cards' target step is `DealDamageToTargetPlayerOrPlaneswalkerEffect` / `DealDamageToTargetCreatureEffect`,
  so `SpellEvaluator` and the other `DamageDealingEffect` readers now score the player damage of the seven "their creatures"
  cards (the fused record was not `DamageDealingEffect`) as they score Chandra's Fury; the set step is not `DamageDealingEffect`,
  as before for that survivor. The sharing cards keep a `DamageDealingEffect` target step with the same amount. Target polarity
  of the seven "their creatures" cards is now the primitive's `HARMFUL` (the record's name entry gave `HARMFUL_DAMAGE`), the
  same as every other `DealDamageToTargetPlayerOrPlaneswalkerEffect` card; `TargetPolarityGuardTest` passes.
- **Blocked, by cause.** Bespoke prompt before anything happens (blocked-step-model): the two "unless its controller has it deal
  damage to them" records, Indoraptor's random opponent's sacrifice choice, Trial of Agony's which-creature choice. No
  `PermanentSubject` on the `Perpetually*` grants (Melt Through, Switchgrass Grazer, Chaos Balor; fold those first, then read
  `thisWay(DEAL_DAMAGE, creature)`); a multi-target-group damage primitive (Chandra Pyromaster / Ravager of the Fells / Soul of
  Shandalar, with Shellshock); a per-controller amount for the set damage (Incite Rebellion); Heart of Bogardan's synchronous
  else-effect path. Unique: The Fallen (recipients damaged by the source earlier), Antagonism (a condition on that player's
  opponents).

**Tests**: card tests of the 17 rewritten cards and Heart of Bogardan, every card naming `DealDamageToPermanentsTargetControlsEffect`,
`withCreaturesSharingAColor` or `PermanentIsTargetCardPredicate`, Sanctum Guardian, a 12-per-class sample of
`DealDamageToTargetCreatureEffect`, `DealDamageToTargetPlayerOrPlaneswalkerEffect`, `SequenceEffect` and `GrantKeywordEffect`,
application (`SubjectResolutionServiceTest`, `StepResultsTest`, `EffectResolutionServiceTest`, `AbstractDamageHandlerTest`,
`DealDamageToPlayersEffectHandlerTest`, `DealDamageToTargetCreatureEffectHandlerTest`, `PredicateEvaluationServiceTest`, the
ratchets) and AI (`TargetPolarityGuardTest`, `SpellEvaluatorTest`): 163 suites, 1,250 test cases, all pass; then every card using
`addSimultaneous*` or `SequenceEffect.simultaneous` (the shield change): 79 suites, 668 test cases, all pass.

**For later runs.**

- "Target X and each other creature that shares a …" is the target's damage + `otherCreaturesSharing(target(), quality)`; a verb
  that should act on such a set needs only to accept the subject (the resolver has it). `DealDamageToDefendingPlayerCreaturesEffect`
  (ledger 5g) was blocked on a `DynamicAmount`, which `DealDamageToPermanentsTargetControlsEffect` now has.
- The `Perpetually*` grants are the next prerequisite for three damage riders; a `PermanentSubject` on
  `PerpetuallyBoostTargetCreatureEffect` / `PerpetuallyGrantStaticEffectToTargetCreatureEffect` unlocks them.
- Other cards that add "N damage to each opponent and each creature they control" with `addTrigger` (two events) may exist outside
  this family; grep `addTrigger(` with two damage effects.
- **Follow-up sweep (done 2026-11-20).** One-sentence "damage to players and permanents" now one event (`addSimultaneous*` /
  `SequenceEffect.simultaneous`, Oracle text checked with `get_card`): Wildfire Cerberus, Warmonger, Volcano Hellion, Volatile
  Arsonist, The Elder Dragon War, Soul Immolation, Sorrow's Path, Splintering Wind (Splinter's leave trigger), Friendly Fire,
  Sarkhan, Dragonsoul (+2), Rockcaster Platoon, Rakdos Firewheeler, Omnath, Locus of Creation, Kumano Faces Kakkazan (I), Joyful
  Stormsculptor, Invasion of Regatha, Chandra, Bold Pyromancer (−7), Carnival // Carnage (Carnival), Avacyn, the Purifier, Claws
  of Wirewood; next-damage-shield tests on Warmonger, Rockcaster Platoon, Sarkhan, Dragonsoul and Claws of Wirewood.

### Step 8, run 6 (2026-11-20)

Family choice: of the candidate families, `--unaudited --family Sacrifice` restricted to `Then` / `And` / `IfYouDo` / `Unless` names
or a nested `CardEffect` has 47 records, and the most of them start with a step on step 7's earlier-step list
(`SacrificeSelfEffect` with a source, target, attached or triggering subject; `SacrificePermanentsEffect` for a chosen
permanent). Exile has more names (193) but most start with a graveyard or library exile that has no recording primitive.
Verdicts (ledger, step `8`): **split 12**, **blocked-other 8**, blocked-step-model 0, blocked-player-loop 0, unique 0. Ratchet
4,835 / 4,068 → 4,823 / 4,056 (12 records, 12 handlers deleted). Card text of every rewritten card checked with `get_card`.

- **Split** (spells and activated abilities flat, trigger slots one `SequenceEffect`, "you may" a `MayEffect` around the
  sequence): Soul Ransom (`SacrificeSelfEffect(source(), controllerOf(source()))` + `DrawCardEffect(2, controllerOf(source()))`, the
  draw ungated as its ruling says), Furnace Scamp (`DealDamageToPlayersEffect(3, triggeringPlayer())` gated on `sacrificed()`),
  Sarkhan the Mad −2 and Mercy Killing (`SacrificeSelfEffect(target(), controllerOf(target()))` + `CreateTokenEffect…forController(controllerOf(target()))`,
  Mercy Killing's count `TargetPower`: last-known once sacrificed, live if it couldn't be, as the handler did), Parasitic Implant
  (`SacrificeSelfEffect(attached(), controllerOf(attached()))`, token ungated), Foot Chopper (`SacrificeSelfEffect(triggering())`, the creature that dealt the damage, even after the Equipment moved, as
  `FootChopperTest` checks; `attachedAtTrigger()` sacrificed nothing behind the `MayEffect`; `DrawCardEffect(PermanentLeftThisWayPower)` gated),
  Colfenor's Urn (`ReturnAllCardsExiledWithSourceEffect` gated), Whispering Specter (`DiscardEffect(TargetPlayerPoisonCounters,
  triggeringPlayer())` gated; the trigger bakes the damaged player as the entry's target and triggering player), Flame-Kin War Scout
  (`DealDamageToTargetCreatureEffect(triggering(), 4)` gated), Arcum Dagsson (run 3's may-search pattern after
  `SacrificeSelfEffect(target(), controllerOf(target()), artifact creature)`), and the "you may sacrifice another … If you do, this
  creature gets / gains …" seven (Brawl-Bash Ogre, Comet Crawler, Dire Fleet Warmonger, Bloodcrazed Socialite, Seraph of New
  Phyrexia; Ragefire Hellkite, Kill-Zone Acrobat): `SacrificePermanentsEffect(1, filter, you())` + `ConditionalEffect(sacrificed(),
  BoostSelfEffect | GrantKeywordEffect(source()))`; their `PermanentChoiceContext` records and completion methods are deleted.
  Target specs are unchanged: the sacrifice step is the only one that declares a target, with the fused record's legal set.
- **Survivor change.** `SacrificeSelfEffectHandler` records a target's controller, card, power and toughness before sacrificing
  it (as `DestroyTargetPermanentEffectHandler` does): the removal service records them only for entries on the stack, so Sarkhan's
  resolving loyalty ability read no controller (`SarkhanTheMadTest` caught it).
- **Behaviour changes (Oracle text).** The seven "you may sacrifice another …" cards are gated on the sacrifice ("If you do"):
  the fused handlers applied the bonus after `sacrificeAndLog` whether or not it happened, and offered permanents that can't be
  chosen to be sacrificed; `SacrificePermanentsEffect` doesn't offer those, and sacrifices the only eligible permanent without a
  prompt (several eligible: a multi-permanent choice for one). Their tests answer the new prompt shapes. Soul Ransom's, Parasitic
  Implant's, Mercy Killing's and Sarkhan's sacrifices now respect "spells and abilities your opponents control can't cause you to
  sacrifice" (`SacrificeSelfEffect`'s `canEffectCauseSacrifice` check); Soul Ransom is activated by an opponent, so a Tajuru
  Preserver controller keeps the Aura and still draws (`SoulRansomTest.tajuruPreserverKeepsTheAuraButStillDraws`; the ruling
  "you'll draw two cards even if you can't sacrifice Soul Ransom"). The survivors' log lines win (the "fizzles — source no longer
  on the battlefield" lines are gone; no test asserted them).
- **AI.** `TargetPolarityClassifier`: `SacrificeSelfEffect` with a target subject and `controllerOf` sacrificer is HARMFUL_REMOVAL
  (Mercy Killing's name entry gave it that; Mercy Killing unchanged). **Target polarity changes** for Sarkhan the Mad −2 and Arcum
  Dagsson: no polarity (their records had no entry; harmful target spec) → HARMFUL_REMOVAL. `SpellEvaluator` sums the steps: Mercy
  Killing's and Sarkhan's tokens are the target controller's (`tokenController()` not `you()`), so not counted as the caster's.
- **Test changes.** `SacrificeSelfAndTargetDiscardsPerPoisonCounterEffectHandlerTest` deleted with its handler (Whispering
  Specter's card test covers the card). `AcceptedOptionResolutionTest`'s fixture spell used the Arcum record, which queued a
  bespoke pending may ability and parked the spell; it now uses the split steps, so the spell stays the pending resolution while
  the search is answered (assertion and display name changed); the parked-outer-resolution path lost that fixture.
- **Blocked, by cause** (all blocked-other; none is the step model once the first half is a primitive):
  - the chosen sacrifice's last-known stats: `SacrificePermanentsEffect` records which permanents left, not their power, toughness,
    colours or types: `SacrificePermanentThenEffect` (121 cards; its continuation also snapshots colours and types, abandons a face-up
    scheme and includes or excludes the granting permanent), Disciple of Bolas, Rupture (power), the Tribute to Hunger record
    (toughness; Tribute to Hunger, Devour Flesh, Doomgape, Consuming Vapors). Make `SacrificePermanentsEffect`'s completions record
    `DepartureResult`s on the step (`PermanentLeftThisWayPower` / `Toughness` then read them) and these split;
  - a granted activated ability binds no `granter()` at activation and `controllerOf(granter())` has no last-known controller
    (Hold for Ransom);
  - no delayed end-step trigger names a permanent for a following `SacrificeSelfEffect` (Spinal Embrace's target, Goblin Crash
    Pilot's Vehicle, which rides as the entry's `targetId`);
  - Victimize: its cast-time graveyard targets are special-cased by class in `SpellCastingService`, `TargetLegalityService` and
    `GameActionAvailabilityService`.
- Not audited (read only in part): `SacrificePermanentsThenEffect` (sacrifices nothing when fewer than N match; check the
  rules for "sacrifice N. If you do" first), `SacrificeArtifactThenDealDividedDamageEffect`,
  `SacrificeTargetArtifactAndReturnTargetArtifactFromGraveyardEffect` (Goblin Welder's simultaneous swap), and the "Unless" /
  "OrElse" records.

**Tests** (one Gradle run, one `--tests` list per task): the 17 rewritten cards' tests (16 classes, Seraph of New Phyrexia has
none), `--tests-for SacrificeSelfEffect SacrificePermanentsEffect ReturnAllCardsExiledWithSourceEffect --max-per-class 20`, Special
Move and Ashling (the other target-subject sacrifices): 78 suites, 660 test cases, all pass; application (`StepResultsTest`,
`EffectResolutionServiceTest`, `SubjectResolutionServiceTest`, `MayEffectHandlerTest`, `GameDataDeepCopyTest`, both ratchets,
`AcceptedOptionResolutionTest`, `SacrificePermanentsEffectHandlerTest` and every test naming `SacrificeSelfEffect` or the
permanent-choice services and contexts: 40 classes): 1,037 test cases, all pass; AI `TargetPolarityGuardTest`, `SpellEvaluatorTest`:
98, all pass. Application, AI, engine testFixtures and card-test `a`, `b`, `c`, `d`, `f`, `k`, `m`, `p`, `r`, `s`, `w` test sources
compile. A repeated `:module:test` task on one command line keeps only its last `--tests` list: give each task one merged list.

**For later runs.**

- "X's controller sacrifices it, then …" is `SacrificeSelfEffect(target(), controllerOf(target()))` plus the rider with
  `controllerOf(target())` (it now reads the recorded controller even for a resolving loyalty ability); "you may sacrifice a/another
  [filter]. If you do" is `SacrificePermanentsEffect(1, filter, you())` + `ConditionalEffect(HappenedThisWay.sacrificed(), …)`.
- The next prerequisite for this family is last-known stats from `SacrificePermanentsEffect` (unlocks `SacrificePermanentThenEffect`
  and four more records).

### Step 8, run 7 (2026-11-20)

Run 6's prerequisite: what `SacrificePermanentsEffect` sacrificed, with its last-known stats. Verdicts (ledger, step `8`): **split 3**
(`SacrificeAnotherCreatureGainLifeAndDrawEqualToPowerEffect`, `SacrificeCreatureThenMassDamageEqualToPowerEffect`,
`SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect`, with their handlers, choice contexts and completion methods),
**absorbed 2** (`PermanentLeftThisWayPower` / `PermanentLeftThisWayToughness` into `AmountThisWay`, `model/amount`), `AmountThisWay`
survivor; `SacrificePermanentThenEffect` stays **blocked-other** with 10 cards. Ratchet 4,823 / 4,056 → 4,820 / 4,053.

- **Recording.** `DepartureResult` gained `lastKnownManaValue` (`DepartureService.report`, `getPermanentManaValue` of the snapshot:
  front face of a transformed permanent, 0 face down). `SacrificePermanentsEffectHandler` begins its step with
  `StackEntry.beginSacrificeStep()` (departures emptied, SACRIFICE = NONE, other outcomes kept, so a sacrifice step no longer
  clears a DESTROY outcome the way `recordDepartures` does). `DestructionSupport.sacrificeTogether`, which every path goes through
  (auto-pick `performSimultaneousSacrifice` / `sacrificeAndLog`, the single-creature `SacrificeCreature` prompt, the
  `ForcedSacrifice` multi-choice and the APNAP choose-then-sacrifice), adds its `DepartureResult`s to the resolving or parked entry
  once a SACRIFICE step has begun (`addSacrificeDepartures`), and the SACRIFICE outcome is remade from them with step 7's mapping
  (`ResolutionResults.departed`): its objects are now permanent ids, not card ids (no reader used the card ids).
  `GameData.recordSacrificedPermanent` still adds to the outcome for sacrifices outside `sacrificeTogether` (Make an Example, pile
  separation).
- **Readers.** `AmountThisWay(kind, Stat)`: `Stat.AMOUNT` (the old meaning), `POWER`, `TOUGHNESS`, `MANA_VALUE` (SACRIFICE only:
  the total of the last-known stat of the departures that were sacrificed, each at least 0); factories `sacrificedPower()`,
  `sacrificedToughness()`, `sacrificedManaValue()`. `HappenedThisWay.toA(SACRIFICE | DESTROY, filter)` judges a permanent that
  left as it last existed (`departedPermanentSnapshots` with its layered `lastKnownCard`, the lookup `TargetPermanentMatches`
  already had, now a shared helper). `QueueReflexiveAbilityEffectHandler` gives the reflexive record its maker's resolution
  results (`StackEntry.carryResolutionResultsFrom`), so "When you do, … that creature's power" reads the sacrifice (CR 603.12,
  608.2h checked with `get_rule` 2026-11-20). Tests: `StepResultsTest` +6 (auto-pick with Glorious Anthem power and mana value,
  the single-player prompt's toughness, each player choosing then sacrificing together, a Bear / non-Bear `toA`, a reflexive
  ability reading the power), `ResolutionScratchpadAmountTest` rewritten for the stats (+2). Real cards: Disciple of Bolas and Foot
  Chopper (power), Tribute to Hunger, Devour Flesh, Doomgape (toughness), Shadow / The Meep / Enigmatic Incarnation (mana value),
  Serendib Djinn (`toA` Island), Minsc & Boo (reflexive power and `toA` Hamster).
- **Split cards** (Oracle text of each checked with `get_card`). Disciple of Bolas, Rupture (`MassDamageEffect(sacrificedPower())`),
  Tribute to Hunger, Consuming Vapors (`targetPlayer()` sacrificer, you gain), Devour Flesh (gain for
  `targetPlayerDeclaredElsewhere()`), Doomgape. 111 `SacrificePermanentThenEffect` cards by a deleted script:
  `SacrificePermanentsEffect(1, filter, you())` then `ConditionalEffect(HappenedThisWay.sacrificed(), X)` for "If you do"
  (`reflexive = false`, also the 6 `targetBeforeSacrifice` cards: the target is the rider's, declared by the sequence as before),
  `ConditionalEffect(sacrificed(), QueueReflexiveAbilityEffect(X))` for "When you do", ungated for "then" (Lorehold Command, Grab
  the Reins) and bare for a `null` rider; `SacrificedPermanentPower` / `Toughness` / `ManaValue` in the riders became the
  `AmountThisWay` factories; Serendib Djinn and Minsc & Boo by hand (`toA` replaced `DealDamageToControllerIfSacrificedCardMatchesEffect`
  / `DrawCardIfSacrificedCardMatchesEffect` on them). Target specs and the reflexive target choice are unchanged (the rider is the
  only step that declares a target; `targetsChosenByReflexiveAbility` comes through `ConditionalEffect`).
- **Behaviour changes.**
  - Oracle text: ten cards read "If you do" but were reflexive (the wrapper's 3-argument constructor defaulted to reflexive): Altar
    of the Wretched, Euru Acorn Scrounger (the token sacrifice; its forage line stays "when you do"), Greven Predator Captain, Hei Bai
    Spirit of Balance, Provisions Merchant, Sephiroth Fabled Soldier, Echo of Death's Wail, The Gitrog Ravenous Ride, The Goose
    Mother, Wilhelt the Rotcleaver: the rider now happens in the same resolution, with no stack object of its own (their tests
    assert that). Grab the Reins ("Sacrifice a creature. Grab the Reins deals damage equal to that creature's power to any target",
    no "if you do") is ungated.
  - Prompt shapes: the wrapper always asked, even with one candidate; `SacrificePermanentsEffect` sacrifices a lone eligible
    permanent without a prompt, asks a `SacrificeCreature` permanent choice for a bare creature filter and a one-of
    `MultiPermanentChoice` otherwise. It also skips a sacrifice an opponent's effect can't cause (Tajuru Preserver style) and
    creature tokens protected from a resolving triggered ability, which the old handlers didn't check. Tribute to Hunger / Doomgape
    gain the last-known toughness from the departure (before: the effective toughness read before the sacrifice; same value).
  - Rupture's damage is dealt by the spell's own entry (the old handler built a throwaway entry); a 0-power sacrifice deals 0.
- **Tests.** All 114 card test classes of the split cards were updated by two delegated runs (prompt shapes, the ten "if you do"
  cards; no test deleted, about 35 tests gained a second eligible permanent so the prompt still shows its exclusions, a few now assert
  the outcome instead of a lone-candidate prompt; missing `@DisplayName`s added in touched files). Final run (one `--tests` list per
  task): the rewritten cards' tests, the 10 kept wrapper cards, `--tests-for SacrificePermanentsEffect --max-per-class 25`,
  `--tests-for QueueReflexiveAbilityEffect SacrificeSelfEffect MassDamageEffect --max-per-class 15`, application (`StepResultsTest`,
  `ResolutionScratchpadAmountTest`, both ratchets, `SacrificePermanentsEffectHandlerTest`, `SacrificePermanentThenEffectHandlerTest`,
  `EffectResolutionServiceTest`, `SubjectResolutionServiceTest`, `ReflexiveRouteRecordTest`, `MayEffectHandlerTest`,
  `EtbTriggerServiceTest`, `InputHandlerEpilogueRatchetTest`), AI (`TargetPolarityGuardTest`, `SpellEvaluatorTest`,
  `GameDataDeepCopyTest`): 197 classes, 1,977 test cases, all pass. Application, AI, engine testFixtures and every touched card-test
  module compile.

**For later runs.**

- `SacrificePermanentThenEffect` keeps 10 cards and three engine-internal builders (`ForageEffectHandler`,
  `MayPayTapAndSacrificePermanentEffectHandler`, `SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffectHandler`). Each card's
  cause: granting-permanent restriction (Dire Blunderbuss, Sunfire Torch: needs a "the granter" / "not the granter" permanent
  predicate in `SacrificePermanentsEffect`'s filter context), a face-up scheme is abandoned, not sacrificed (Reality Is Mine to
  Control), a rider reading the entry's sacrificed snapshot (Bound // Determined colour count, Braids Arisen Nightmare, Heart-Shaped
  Herb's return, Transmute Artifact, Vivien on the Hunt, Tevesh Szat's commander check: `PermanentIsCommanderPredicate` with `toA`
  likely works), and Wyll's per-colour reflexive flag (mechanical: pick the shape per colour). Convert the internal builders, then
  delete the record.
- "That creature's power / toughness / mana value" after any sacrifice step is `AmountThisWay.sacrificed*()`; "if you sacrifice an X
  this way" is `HappenedThisWay.toA(StepKind.SACRIFICE, filter)`. The `Sacrificed*` amounts are now for sacrifice costs only.
- Check "When you do" against the Oracle text when splitting: the wrapper's short constructors were reflexive by default, and ten
  "If you do" cards had that wrong.
- Hotel of Fears (WHO 584, no rulings) "Put X +1/+1 counters on target creature you control … Then sacrifice another creature" uses
  "other than the source" (a plane), so the targeted creature can be the sacrifice; "another" most likely means other than the
  target. Unchanged by this run; needs a target-relative predicate in `SacrificePermanentsEffect`'s filter context.
- Fixed after this run (2026-11-20): Hotel of Fears now sacrifices "another creature" other than the targeted one
  (`PermanentNotPredicate(PermanentIsTargetCardPredicate)`; `SacrificePermanentsEffectHandler` binds the entry's target permanent
  as the target card when no rider handed it one; `HotelOfFearsTest`, `SacrificePermanentsEffectHandlerTest`).

### Step 8, run 8 (2026-11-20)

Family choice: of the candidate families (Draw 14, Create 17, Put 27, Return 39, Each 54, Reveal 46, Exile 189 fused names;
Discard and Mill none), Exile has the most first halves on step 7's earlier-step list (`ExileTargetPermanentEffect` with a
target, source or set subject). Most Exile names start with a graveyard, library or hand exile, which record no step, so the run
took the permanent exiles plus the two graveyard records that need no result. Verdicts (ledger, step `8`): **split 6**,
**absorbed 2**, **survivor 2**, **blocked-other 10**, **blocked-step-model 2**, **unique 3**, blocked-player-loop 0. Ratchet
4,820 / 4,053 → 4,812 / 4,045 (8 records, 8 handlers deleted). Card text of every rewritten card checked with `get_card`.

- **Split**: Dire Tactics (`ExileTargetPermanentEffect` + `ConditionalEffect(NotCondition(ControlsPermanent(Human)),
  LoseLifeEffect(TargetToughness, you()))`; its rulings: last-known toughness, the Human checked after the exile), Archfiend's
  Vessel (`SequenceEffect.of(ExileTargetPermanentEffect(source()), ConditionalEffect(HappenedThisWay.exiled(), token))`, "If you
  do"), Diabolic Servitude (`SequenceEffect.of(ExileTriggeringCardFromGraveyardEffect, ReturnToHandEffect.self())`, ungated:
  "exile it and return"), King Solomon's Frogs and Unexplained Absence (`ExileTargetPermanentEffect` over every chosen target, then
  `DrawCardEffect(1, playersThisWay(EXILE))` / `CloakTopCardOfControllerLibraryEffect(playersThisWay(EXILE))`: one target per
  player, so the resolver's distinct player list gives one draw or cloak per exiled permanent), Reality Shift
  (`ManifestTopCardEffect(Fixed(1), controllerOf(target()))`). Target specs unchanged: the exile step is the only one that
  declares a target, with the same harmful spec (the card's filter narrows it, as before).
- **Absorbed** into `ExileTargetPermanentEffect` with a set subject (one leave batch, CR 603.10a as run 2 checked):
  `ExileTargetAndAttachedMatchingEffect` → `allOf(attachmentsOf(target() | allTargets(), Equipment | Aura), target() |
  allTargets())` (Soul Nova, Silence the Believers); `ExileTargetCreatureAndAllWithSameNameEffect` → `allOf(target(),
  otherCreaturesSharing(target(), NAME))` (Sever the Bloodline; a face-down target shares its name with nothing, as before).
- **Survivor changes.** `ManifestTopCardEffect(DynamicAmount, PlayerSubject)` and `CloakTopCardOfControllerLibraryEffect(PlayerSubject)`
  (default `you()`, old constructors kept): each named player manifests or cloaks from their own library, through
  `SubjectResolutionService`; their handlers gained that service (no hand-built test wiring named them).
- **Behaviour changes.** King Solomon's Frogs and Unexplained Absence now exile every target before the first draw or cloak (the
  old handler drew after each exile; the text reads exile, then "for each permanent exiled this way"; `UnexplainedAbsenceTest`'s
  exile-first case passes). Archfiend's Vessel's token is gated on the exile ("If you do"; the old handler created it whether or not
  the exile happened; no reachable state makes the exile fail, so no test fails on the old card). Reality Shift and Dire Tactics
  apply their second sentence after the exile as the text reads, with no "if" (the old handlers skipped it when the exile was
  replaced). The survivors' log lines win.
- **AI.** Target polarity unchanged: every split card's target step is `ExileTargetPermanentEffect` (HARMFUL_REMOVAL through
  `RemovalEffect`, as the records were), the riders declare no target; Sever the Bloodline's name entry in `TargetPolarityClassifier`
  is deleted (the survivor gives the same HARMFUL_REMOVAL). The split cards' riders are now separate steps the evaluators see on
  their own; no AI class named the deleted records but Sever's entry.
- **Blocked, by cause**: a per-object or per-player amount of what was exiled (Swords family: Luminate Primordial's per-target
  power, so `ExileTargetCreaturesAndControllersGainLifeEqualToPowerEffect` stays though its three one-target cards would split as
  exile + `GainLifeEffect(TargetPower | TargetToughness, controllerOf(target()))`; Martyr's Cry; Declaration in Stone's nontoken
  count); a token copy of a departed target (Dedicated Dollmaker, Kaya, Nemesis Trap); no same-name-token predicate (Legions to
  Ashes); a cast permission on the cards exiled this way (Hurl Through Hell); graveyard exiles record no EXILE step and
  `GraveyardTargetingSupport` names their records by class (`ExileSourceCardFromGraveyardThenEffect` 11 cards,
  `ExileTargetCardFromGraveyardThenEffect`, `ExileTargetCardFromGraveyardAndGainLifeEffect`, Sphere of Annihilation's graveyard
  half). Blocked-step-model: Forsaken City (hand-exile choice), Fortune (saddler choice). Unique: Unyielding Gatekeeper, Ugin the
  Ineffable's linked token, `ExileSelfAndReturnFrontEffect`.

**Tests** (one Gradle run, one `--tests` list per task): the 9 rewritten cards' tests, `--tests-for ManifestTopCardEffect
CloakTopCardOfControllerLibraryEffect ExileTriggeringCardFromGraveyardEffect --max-per-class 8`, `--tests-for ExileTargetPermanentEffect
--max-per-class 12`, Hotel of Fears, Feast of Worms, Mark for Death, Public Execution, application (`SacrificePermanentsEffectHandlerTest`,
`ExileTargetPermanentEffectHandlerTest`, `EffectCountRatchetTest`, `SubjectResolutionServiceTest`, `StepResultsTest`,
`CardImmutabilityArchTest`), AI `TargetPolarityGuardTest`: 48 top-level classes, 490 test cases (plus their `@Nested` suites), all
pass. Application, AI, engine testFixtures and card-test `a`, `d`, `f`, `h`, `k`, `m`, `p`, `r`, `s`, `u` test sources compile.

**For later runs.**

- "Exile [one per player]. For each permanent exiled this way, its controller …" is the exile + the verb with
  `playersThisWay(EXILE)`; a verb acting for "its controller" from their own library needs only a `PlayerSubject` (manifest and
  cloak have one now).
- The next prerequisite for Exile is a recorded EXILE step for graveyard, library and hand exiles (and taking those records out of
  `GraveyardTargetingSupport`'s class list); it unlocks most of the 189 names. A per-object amount ("that creature's power" per
  target) would finish the Swords family.

### Step 8, run 9 (2026-11-20)

Run 8's prerequisite: graveyard and library exiles record an EXILE step. Verdicts (ledger, step `8`): **split 11**
(`ExileSourceCardFromGraveyardThenEffect`, `ExileTargetCardFromGraveyardThenEffect`, `ExileTargetCardFromGraveyardAndGainLifeEffect`,
`ExileGraveyardCardCreateTokenIfCreatureEffect`, `ExileTargetCardFromGraveyardWithConditionalEffectsEffect`,
`ExileTargetCreatureCardFromGraveyardThenReflexiveEffect`, `ExileTopCardMayPlayThisTurnThenDealManaValueDamageEffect`,
`ExileTopCardMayPlayThisTurnThenDealManaValueDamageToControllerEffect`, `ExileTopCardAndBoostSelfIfCreatureEffect`,
`ExileTopCardMayPlayThisTurnAndBoostSelfIfMatchingEffect`, `ExileTopCardMayPlayThisTurnAndBoostTargetCreatureByManaValueEffect`, with
their handlers), **absorbed 1** (`ToughnessOfLastCardExiledWithSource` into `AmountThisWay`, `model/amount`), **survivor 7**
(`HappenedThisWay`, `AmountThisWay`, `ExileGraveyardCardsEffect`, `ExileSourceCardFromGraveyardEffect`, `ExileTriggeringCardFromGraveyardEffect`,
`ExileTopCardMayPlayThisTurnEffect`, `ExileTopCardOfOwnLibraryEffect`), **blocked-other 2**, **blocked-step-model 4**, **unique 1**,
blocked-player-loop 0. The Swords record keeps Luminate Primordial; its three one-target cards split. Ratchet 4,812 / 4,045 →
4,801 / 4,034. 37 card files rewritten by hand-written scripts (deleted); Oracle text of every one checked with `get_card`, Swords to
Plowshares' ruling with `get_rulings`. CR 608.2b, 608.2c, 608.2h, 400.7, 603.12 and 120.8 checked with `get_rule` (2026-11-20).

- **Recording** (each records `StepOutcome.NONE` first, then the cards it exiled and their owners, the graveyard's or library's player;
  `StepKind` Javadoc updated): `ExileSourceCardFromGraveyardEffect`, `ExileTriggeringCardFromGraveyardEffect`, `ExileGraveyardCardsEffect`
  (every branch but the cards chosen at a prompt: `OWN` / `TRIGGERING_PLAYER` with more cards than the count, `EACH_OPPONENT_KEEP`, so
  those paths are not an earlier step), `ExileTopCardMayPlayThisTurnEffect`, `ExileTopCardOfOwnLibraryEffect`. `ExileGraveyardCardsEffect`'s
  target filter is now judged with the ability's X, source and source power, as the fused records judged it ("creature card with mana
  value X", Sequence Engine); predicates that need none of those are unaffected.
- **Readers.** `HappenedThisWay` gained a `CardPredicate cardFilter` component and `toACard(StepKind.EXILE, filter)`: "if it was a
  creature card", "when you exile a nonland card this way", one exiled card matches as it is now (`GameQueryService.findCardById`; a card
  keeps its characteristics in exile). `AmountThisWay` accepts POWER / TOUGHNESS / MANA_VALUE for EXILE (`exiledPower()`,
  `exiledToughness()`, `exiledManaValue()`): an exiled permanent as the target path recorded it, a card as it is now, each at least 0.
- **`GraveyardTargetingSupport`** lost the five deleted records' branches (the `ExileSourceCardFromGraveyardThenEffect` same-ability
  special case included): a sequence recurses into its steps, `ConditionalEffect` is unwrapped, and a `QueueReflexiveAbilityEffect` declares
  no graveyard target, so "When you do" targets are still chosen by the reflexive ability. `Card.bindEffectToTargetGroup` lost its case.
- **Split cards.** "Exile it. If you do" (Rooting Kavu, Iname Life Aspect, Iname as One, Greenwarden of Murasa):
  `SequenceEffect.of(ExileSourceCardFromGraveyardEffect(), ConditionalEffect(HappenedThisWay.exiled(), X))`; "When you do" (The Balrog of
  Moria, Sandbender Scavengers, Paramecia Coloniex, Glissa's Retriever, Flitting Guerrilla, Doombot Harbinger, Bone Rattler): X in a
  `QueueReflexiveAbilityEffect`. Target graveyard card (`ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard`, up to one:
  `upToOneTargetFromAnyGraveyard`, an opponent's graveyard: the `TARGET_CARDS_OPPONENT_GRAVEYARD` exact form): Kirin-Touched Orochi (both modes,
  "When you do"), Sheoldred's Assimilator ("If a card is exiled this way"), Sequence Engine, Selesnya Eulogist, Grave Robbers, Shamble Back
  (riders ungated: no "if", and an illegal only target makes the ability not resolve, CR 608.2b), Cleanup Crew's mode (the bare exile),
  Boneclad Necromancer (`MayEffect` around the sequence, gated on `exiled()`), Deluge of the Dead, Raven Eagle, Ignis Scientia, General's
  Enforcer, Ethereal Absolution, Feral Appetite (gated on `toACard(creature)`), Lion Sash (permanent card), Klothys (land / nonland),
  Misfortune Teller (creature, land, and "Otherwise" as `toACard(Not(AnyOf(creature, land)))`, so a creature land card makes both tokens as
  before and no card exiled gains nothing), Venom (tracked exile, reflexive counters read `exiledToughness()`). Library: Hidetsugu
  (`toACard(nonland)` + reflexive damage of `exiledManaValue()`), Daredevil Fearless Fighter (damage to you of `exiledManaValue()`, ungated;
  0 is no damage, CR 120.8), Bioplasm (`ExileTopCardOfOwnLibraryEffect` + `toACard(creature)` + `BoostSelfEffect(exiledPower(),
  exiledToughness())`), Daredevil Man Without Fear (`toACard(Hero)`), Cait Sith (`exiled()` + reflexive `BoostTargetCreatureEffect(exiledManaValue())`).
  Swords to Plowshares, Avenger en-Dal, Captain Marvel: `ExileTargetPermanentEffect` + `GainLifeEffect(TargetPower | TargetToughness,
  controllerOf(target()))`. Target specs are unchanged: the exile step declares the target with the same predicate and scope; the riders
  declare none.
- **Behaviour changes.** Swords to Plowshares, Avenger en-Dal and Captain Marvel's life gain no longer depends on the exile happening
  ("Its controller gains life", no "if"; run 7's Victorious Destruction reading; the ruling only says last-known power): no reachable state
  makes the exile of a legal target fail, so no test fails on the old cards. Shamble Back's Zombie is unconditional by its text (its target
  is a creature card, so the same outcome). The log lines of `ExileGraveyardCardsEffect` / `ExileTopCardMayPlayThisTurnEffect` win (Hidetsugu
  and Daredevil said "and may play it this turn", now "(may play it this turn)"). Raven Eagle's Clue is created without investigating, as
  the old handler made it (`withInvestigate(false)`; `RavenEagleTest` caught it).
- **AI.** No AI class named a deleted record. The split cards' riders are separate steps now; tokens and boosts inside a `ConditionalEffect`
  are scored as the evaluators score other gated riders. Swords to Plowshares, Avenger and Captain Marvel keep HARMFUL_REMOVAL through
  `ExileTargetPermanentEffect` (`RemovalEffect`, EXILE).
- **Blocked, by cause.** blocked-other: an own-graveyard single target (`ExileTargetCardFromGraveyardAndCreateTokenEffect`, Quintorius;
  `ExileTargetCardFromGraveyardAndTrackWithSourceThenEffect`, Nyla): `ExileGraveyardCardsEffect.TARGET_CARDS_CONTROLLER_GRAVEYARD` means "any
  number of target cards" in `GraveyardTargetingSupport` and four validators (`GraveyardTargetValidators`, `ValidTargetService`,
  `TargetLegalityService`); with a count-honouring controller-graveyard target both split (Nyla's X is `exiledManaValue()`).
  blocked-step-model: the resolution-time own-graveyard choices (`ExileOwnGraveyardCardThenEffect`, `ExileCardFromGraveyardThenEffect`,
  `ExileNCardsFromGraveyardThenEffect`, `ExileOwnGraveyardCardThenCreateTokenEffect`: `rerunCurrentEffectAfterInteraction` or a bespoke
  continuation). Unique: `ExileSourceCardFromGraveyardAndShuffleTopCardsEffect` (Triumph of Saint Katherine).
- **Hand exiles** record nothing yet: every hand-exile primitive chooses at a prompt (`TargetPlayerExilesFromHandEffect`,
  `ExileFromHandToImprintEffect`, `ExileCardFromHandFaceDownWithSourceEffect`), and the whole-hand exiles are themselves fused records
  with a delayed return.

**Tests**: one Gradle run, one `--tests` list per task: the tests of the 35 rewritten cards that have one (Deluge of the Dead and
Kirin-Touched Orochi through Invasion of Innistrad and Teachings of the Kirin, plus the seven other tests naming them), Luminate
Primordial, `--tests-for ExileGraveyardCardsEffect ExileSourceCardFromGraveyardEffect ExileTriggeringCardFromGraveyardEffect
ExileTopCardMayPlayThisTurnEffect ExileTopCardOfOwnLibraryEffect --max-per-class 15`, 25 cards reading `HappenedThisWay.exiled()` /
`AmountThisWay`, AI `TargetPolarityGuardTest`, `SpellEvaluatorTest`: 112 top-level classes, 1,126 test cases, all pass (Raven Eagle's
investigate check failed first, fixed in the card); application (`StepResultsTest` +7: graveyard target exile with its owner and
`toACard`, a land not a creature card, the source card exiled or gone, library exile stats, an exiled permanent's last-known power;
`StepTriggerServiceTest` (its Klothys fixture rewritten to the steps), `ExileGraveyardCardsEffectHandlerTest`,
`ResolutionScratchpadAmountTest`, `GraveyardTargetingServiceTest`, `EffectResolutionServiceTest`, `SubjectResolutionServiceTest`,
`EffectCountRatchetTest`, `CardImmutabilityArchTest`): 345 test cases, all pass. Application, AI, engine testFixtures and card
modules `a b c d e f g h i k l m p r s shared t v` compile.

**For later runs.**

- "Exile target card from a graveyard. If it was a [type] card" is the exile + `ConditionalEffect(HappenedThisWay.toACard(StepKind.EXILE,
  filter), …)`; "Otherwise" is the negated filter, not a `NotCondition` (which would also fire when nothing was exiled). "Exile the top card
  of your library … that card's mana value / power" is `ExileTopCardMayPlayThisTurnEffect` or `ExileTopCardOfOwnLibraryEffect` + an
  `AmountThisWay.exiled*()` amount; a reflexive ability reads it too (results are carried).
- Next for Exile: (1) make `ExileGraveyardCardsEffect`'s controller-graveyard scope honour `count` (a single "target card from your
  graveyard") and split Quintorius and Nyla; (2) the other library exiles with a play permission
  (`ExileTopCardOfDamagedPlayerLibrary…`, `ExileTopCardOfTargetOpponentLibrary…`, `ExileTopCardsMayPlay…AndCreateTokens…`) need a library
  exile primitive with a `PlayerSubject` for whose library, then record and split like Hidetsugu; (3) the choose-at-resolution graveyard
  and hand exiles need a recording choice primitive (pause at the end of the step, as `DiscardEffect` does) before their fused records
  split; (4) Luminate Primordial needs a per-object amount.

### Step 8, run 10 (2026-11-20)

Run 9's next prerequisites for the Exile family (items 1 and 2 of its "Next for Exile"). Verdicts (ledger, step `8`): **split 5**
(`ExileTargetCardFromGraveyardAndCreateTokenEffect`, `ExileTargetCardFromGraveyardAndTrackWithSourceThenEffect`,
`ExileTopCardOfDamagedPlayerLibraryGainLifeAndMayCastThisTurnEffect`, `ExileTopCardLoseLifeEqualToManaValueAndMayPlayThisTurnEffect`,
`ExileTopCardOfTargetPlayerLibraryEffect`), **absorbed 5** (`ExileTopCardOfOpponentLibraryControllerMayPlayThisTurnEffect`,
`ExileTopCardsOfTargetPlayerControllerMayPlayThisTurnEffect`, `ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerCastPermissionUntilEndOfTurnEffect`,
`ExileTopCardOfDamagedPlayerLibraryMayCastThisTurnEffect`, `ExileTopCardOfTriggeringPlayerLibraryEffect`), with their handlers; **survivor 3**
(`ExileGraveyardCardsEffect`, `ExileTopCardMayPlayThisTurnEffect`, `ExileTopCardOfOwnLibraryEffect`); **blocked-other 1**
(`ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffect`, Curse of Hospitality only). Ratchet 4,801 /
4,034 → 4,791 / 4,024. 16 card files rewritten (a deleted scratch script plus Quintorius and Nyla by hand); Oracle text of each checked with
`get_card`, rulings of Quintorius, Nyla, Daxos, Ragavan, Fallen Shinobi, Scrib Nibblers and Dimensional Infiltrator with `get_rulings`. CR 608.2b
and 109.5 checked with `get_rule` (2026-11-20).

- **Controller-graveyard target honours its count.** `ExileGraveyardCardsEffect.TARGET_CARDS_CONTROLLER_GRAVEYARD` meant "any number of target
  cards" in `GraveyardTargetingSupport`, `ValidTargetService` (activated-ability prompt) and `TargetLegalityService` (zero targets always
  allowed, no upper bound). It now reads `count`, `exactTargetCount` and `allowZeroTargets` like the other target scopes; "any number" is
  `count = Integer.MAX_VALUE` with `allowZeroTargets` (Quintorius Kand's −6, Zethi's kick-counter exile, whose target group still caps it).
  `GraveyardTargetValidators` was already count-free. New factories `oneTargetFromControllerGraveyardWithSource(filter)` and
  `upToOneTargetFromControllerGraveyardWithSource(filter)`. Cast-time prompts unchanged: Quintorius's end-step trigger still asks exactly one
  noncreature, nonland card of your graveyard (min 1, max 1), Nyla's up to one creature card (min 0, max 1); both tests now assert min and max,
  the Kand and Zethi tests pass unchanged.
- **Split, graveyard.** Quintorius, Loremaster: `SequenceEffect.of(oneTarget…(noncreature nonland), Spirit token)`, ungated ("Create", no "if";
  its ruling and CR 608.2b: an illegal only target makes the ability not resolve, so no token). Nyla: `SequenceEffect.of(upToOne…(creature),
  ConditionalEffect(HappenedThisWay.exiled(), lose life + Clues of AmountThisWay.exiledManaValue()))`, replacing the handler's `eventValue`.
- **Library subject.** `ExileTopCardMayPlayThisTurnEffect` gained `PlayerSubject library` (`fromLibraryOf`, default `you()`), `castOnly`
  (`mayOnlyCast()`: "you may cast that card", a land gets no permission) and `anyManaType` (`withAnyManaType()`: any-type spending for nonland
  cards); `ExileTopCardOfOwnLibraryEffect` gained `PlayerSubject player` (`of(player)`). Each named player exiles from their own library in
  APNAP order through `SubjectResolutionService`, the controller gets the permission, and the EXILE step records the cards with their
  owners. `targetPlayer()` declares a harmful player target (the old records: Korvold, Thought Harvester, Scrib Nibblers, Dimensional
  Infiltrator harmful; Knacksaw Clique / Ornate Kanzashi benign; harmful is checked only for permanent targets and in the AI's player ranking,
  and the cards' opponent target filters leave one legal player, so the legal targets and choices are unchanged).
- **Rewritten cards.** Absorbed: Knacksaw Clique, Ornate Kanzashi (`targetPlayer()`), Korvold and the Noble Thief (3 cards), Fallen Shinobi
  (2, free, `triggeringPlayer()`), Breeches (`withAnyManaType()`), Ragavan (`mayOnlyCast()`), Grenzo's mode (both), Fathom Feeder
  (`ExileTopCardOfOwnLibraryEffect.of(triggeringPlayer())`), The Matrix of Time (`of(ownerOf(triggeringCard()))`, the subject its life loss
  already used). Split: Daxos (the exile, then `GainLifeEffect(exiledManaValue(), you())`; a land gives 0 life, as its ruling says), Hotel of
  Fears (`ExileTopCardMayPlayThisTurnEffect` then `LoseLifeEffect(exiledManaValue(), you())`), Scrib Nibblers and Dimensional Infiltrator
  (the exile, then `ConditionalEffect(HappenedThisWay.toACard(EXILE, land card), …)`), Thought Harvester (the exile alone).
- **Behaviour changes.** The damaged-player records gave the permission to the triggering creature's current controller; Fallen Shinobi,
  Breeches, Ragavan, Grenzo and Daxos say "you", the ability's controller as it triggered (CR 109.5): the same player unless control
  changed in between. Hotel of Fears' Lich's-Duel-Mastery-style life-loss question is now asked by the life-loss step after the exile, not
  before it (the exile is an earlier finished step, so nothing is redone). Log lines of another player's library exile are the survivor's
  ("<owner> exiles <card> from the top of their library (<controller> may play it this turn)."); the old Grenzo/Daxos "cannot cast it" line
  is gone. `ExileTopCardOfTargetPlayerLibraryEffect`'s fallback to the controller's library with no target is gone (it always declared one).
- **Blocked.** Curse of Hospitality: "that creature's controller may play that card" needs a who-may-play subject on the primitive that no
  other card sets, so the record stays with one card (its `count` / `withoutPayingManaCost` components now have no other user).
- **Item 3 not started** (the recording choose-a-card exile primitive): see "For later runs".
- **AI.** No AI class named a deleted record. `TargetPolarityGuardTest` and `AiTargetSelectorTest` pass. Scrib Nibblers' land rider is now a
  `GainLifeEffect` step the evaluators see on its own.

**Tests** (one Gradle run, one `--tests` list per task): the tests of the 16 rewritten cards and of Curse of Hospitality, Quintorius Kand,
Zethi, Oracle's Vault, Rona and Precognition Field, `--tests-for ExileTopCardMayPlayThisTurnEffect ExileTopCardOfOwnLibraryEffect
ExileGraveyardCardsEffect --max-per-class 12`, application (`StepResultsTest` +3: another player's library exile records its owner and gives
the controller the permission, a cast-only land gets none, a target player's land card judged by `toACard`; `ExileGraveyardCardsEffectHandlerTest`,
`TargetLegalityServiceTest`, `ValidTargetServiceTest`, `GraveyardTargetingServiceTest`, `SubjectResolutionServiceTest`,
`TriggeredAbilityStackServiceTest`, `StepTriggerServiceTest`, `MayEffectHandlerTest`, `EtbTriggerServiceTest`,
`ActivatedAbilityExecutionServiceTest`, `CardImmutabilityArchTest`, `EffectCountRatchetTest`), AI (`TargetPolarityGuardTest`,
`AiTargetSelectorTest`): 66 top-level classes, 1,128 test cases (plus their `@Nested` suites), all pass; `EffectCountRatchetTest` again after
the baseline change, passes. Nyla's test gained `@DisplayName`s and two cases (a target that left the graveyard gives no life loss or Clues;
the trigger can't target an opponent's graveyard). Application, AI, engine testFixtures and card-test `b c d f g h k n o q r s t z` test sources
compile.

**For later runs.**

- "That player / target opponent exiles the top N cards of their library. You may play / cast them this turn" is
  `ExileTopCardMayPlayThisTurnEffect(...).fromLibraryOf(subject)` (+ `mayOnlyCast()`, `withAnyManaType()`); "… exiles the top card of their
  library" without a permission is `ExileTopCardOfOwnLibraryEffect.of(subject)`. Their riders read `AmountThisWay.exiled*()` /
  `HappenedThisWay.toACard(EXILE, …)` like Hidetsugu. Candidates left in the library family: `ExileTopCardsOfEachOpponentEffect` (Fathom
  Feeder's activated ability: `of(eachOpponent())` if its handler has nothing more), `ExileTopCardOfRandomOpponentLibrary…` (random opponent
  and source tracking: no subject), `ExileTopCardOfTargetOpponentLibraryLoseLifeEqualToManaValueAndGrantPlayPermissionEffect` (face down,
  permission while exiled), `ExileTopCardOfAttackingPlayerLibrary…` (permission while exiled), `ExileTopCardsOfTargetPlayerMayPlayForLife…`,
  `ExileTopCardLoseLifeEqualToManaValueAndMayCastInstantOrSorceryEffect` (Hidetsugu and Kairi: target opponent loses life, free cast of an
  instant or sorcery), `ExileTopCardsMayPlay…AndCreateTokens…`. A "until your next turn" / "while exiled" duration on the primitive would
  absorb several of them; check each handler first.
- Item 3, the recording choose-a-card exile (`ExileOwnGraveyardCardThenEffect` 5 cards, `ExileCardFromGraveyardThenEffect` 2,
  `ExileNCardsFromGraveyardThenEffect` 4, `ExileOwnGraveyardCardThenCreateTokenEffect` 2): `ExileGraveyardCardsEffect.OWN` already pauses on
  `GraveyardReturnSupport.beginGraveyardExileChoice`, whose `GraveyardChoiceHandlerService` EXILE completion exiles one card at a time and
  resumes the parked entry (it also carries a bespoke `afterExileEffects` "if you do" continuation). The primitive needs: the completion
  adding each exiled card to the parked entry's EXILE outcome (`addToStep`, as `DiscardEffect`'s prompt does) so the step ends with its result
  recorded; `resolveOwn` honouring `filter` (it ignores it today) and offering only matching cards; a "you may" form that asks even when the
  graveyard has no more cards than the count; source tracking on the chosen path; and for "exile N … When you do" a gate
  `HappenedThisWay.atLeast(EXILE, N)`. Decide first whether the optional "may" is `allowZeroTargets` on the OWN scope or a `MayEffect`
  around it (the record's Javadoc says the 'If you do' riders must not see a declined choice). Luminate Primordial still needs a per-object
  amount.

### Step 8, run 11 (2026-11-20)

Run 10's "For later runs": the library-exile records a permission duration absorbs, then item 3, the recording choose-a-card
exile. Verdicts (ledger, step `8`): **absorbed 12** (`ExileTopCardsMayPlayThisTurnEffect`, `ExileTopCardsMayPlayUntilNextEndStepEffect`,
`ExileTopCardMayPlayUntilNextUpkeepEffect`, `ExileTopCardOfEachOpponentAndMayCastThisTurnEffect`,
`ExileTopCardOfEachPlayersLibraryAndGrantPlayPermissionUntilEndOfTurnEffect`, `ExileTopCardOfEachPlayersLibraryMayPlayUntilNextTurnEffect`,
`ExileTopCardOfEachOpponentLibraryAndGrantPlayPermissionEffect`, `ExileTopCardOfAttackingPlayerLibraryAndGrantControllerPlayPermissionEffect`,
`ExileTopCardsOfTargetPlayerMayPlayForLifeThisTurnEffect`, `ExileTopCardOfEachOpponentAndMayCastForLifeThisTurnEffect`,
`ExileTopCardsOfEachOpponentEffect`, `ExileTopCardsOfTargetPlayerLibraryEffect`), **split 4** (`ExileTopCardOfTargetOpponentLibraryLoseLifeEqualToManaValueAndGrantPlayPermissionEffect`,
`ExileOwnGraveyardCardThenEffect`, `ExileCardFromGraveyardThenEffect`, `ExileOwnGraveyardCardThenCreateTokenEffect`), with their handlers;
**amount absorbed 1** (`ManaValueOfLastCardExiledWithSource` into `AmountThisWay.exiledManaValue()`, `model/amount`); **survivor 4**
(`ExileTopCardMayPlayThisTurnEffect`, `ExileTopCardOfOwnLibraryEffect`, `ExileGraveyardCardsEffect`, `MayEffect`'s handler). Ratchet 4,791 /
4,024 → 4,775 / 4,008. 39 card files rewritten (deleted scratch scripts for the 20 mechanical ones, the rest by hand); Oracle text of each
checked with `get_card`, rulings of Thought-String Analyst (none), Ixhel, Cunning Rhetoric, Xander's Pact, Inside Information and
Brainstealer Dragon (none) with `get_rulings`. No CR number is cited below.

- **Library primitive.** `ExileTopCardMayPlayThisTurnEffect` gained `Duration duration` (`until(...)`, default `UNTIL_END_OF_TURN`; "your" is the
  controller's), `faceDown` (`exiledFaceDown()`: the controller is the exiler and may look) and `forLife` (`payingLifeEqualToManaValue()`); the
  old 7-argument constructor is kept. The any-mana rider now lasts the permission's duration with the controller as its "your" (the
  end-of-turn rider ignores the controller, so unchanged). Every rewritten duration was a plain `Duration` before (`UNTIL_YOUR_NEXT_END_STEP`,
  `UNTIL_CONTROLLERS_NEXT_UPKEEP`, `UNTIL_END_OF_YOUR_NEXT_TURN`, `PERMANENT`). The static draw-replacement reader is unaffected (its cards keep the
  default). Log: one line per card, "(… may play it until <player>'s next end step)" / "for as long as it remains exiled"; face-down lines hide
  the card. `ExileTopCardOfOwnLibraryEffect` gained `DynamicAmount count` (`of(player, count)`, evaluated once with the source as it is or last
  was; `trackWithSource` only for one card).
- **Rewritten library cards.** Plain: Jeska's Will, Memorial Vault, Witch-king, Chandra Legacy of Fire (this turn); Haste Magic, Inti, Opera
  Love Song, Ob Nixilis Captive Kingpin, Riveteers Charm, Seek the Beast, Shadow Urchin, Wiccan (until your next end step); Elkin Bottle (next
  upkeep). Other libraries: Nassari (`eachOpponent()`, cast only, any mana), Mezzio Mugger and Lidless Gaze (`eachPlayer()`, any mana; Lidless
  until the end of your next turn), Brainstealer Dragon (`eachOpponent()`, `PERMANENT`), Ixhel (`matching(eachOpponent(),
  PlayerHasPoisonCountersAtLeastPredicate(3))`, face down, `PERMANENT`), Cunning Rhetoric (`triggeringPlayer()`, the attacking player the
  trigger baked), Inside Information (`targetPlayer()`, for life), Xander's Pact (`eachOpponent()`, cast only, for life). Split: Thought-String
  Analyst (the face-down `PERMANENT` exile of `targetPlayer()`, then `LoseLifeEffect(AmountThisWay.exiledManaValue(), you())`). No play
  permission: Malboro, Ashiok Nightmare Muse's token, Fathom Feeder, Processing Plant (`of(eachOpponent(), n)`), Ashiok Wicked Manipulator,
  Oblivion Sower, Raven Guild Master, Sire of Stagnation (`of(targetPlayer(), n)`, the same harmful player target the old record declared).
- **Recording choose-a-card exile.** `ExileGraveyardCardsEffect`'s `OWN` (and `TRIGGERING_PLAYER`) scope now offers only cards matching `filter`
  (judged with the source card, so "other than this card" works), exiles a lone match without a prompt, honours `trackWithSource` (source still
  on the battlefield) and begins its choice through `GraveyardReturnSupport.beginGraveyardExileChoice(gameData, player, candidates, count,
  trackingPermanentId)` (mandatory, the matching indices). `GraveyardChoiceHandlerService`'s EXILE completion adds each exiled card and its
  graveyard's owner to the parked entry's EXILE outcome (`addToStep`), keeps the tracking permanent, and a later pick of a multi-card exile
  offers only the cards the first pick offered (it offered the whole graveyard; the spell-excluding variant now stays excluded). The step pauses
  at its end and the parked entry resumes once (the existing completion tail). No existing `OWN` user set a filter. Factories
  `fromOwnGraveyard(count, filter)` / `fromOwnGraveyardWithSource(count, filter)`.
- **The "may" decision.** "You may exile a … card from your graveyard. If you do, X" is
  `MayEffect(SequenceEffect.of(fromOwnGraveyard(1, filter), ConditionalEffect(HappenedThisWay.exiled(), X)))` ("When you do": X in a
  `QueueReflexiveAbilityEffect`); the existing `MayEffect` path wraps the pausing step: an accepted may splices the sequence, the exile parks at
  index + 1 and the conditional runs after the choice. `MayEffectHandler` offers no choice when the controller's graveyard has no matching card
  (as the old records skipped the prompt). Split cards: Ichorid, Kishla Trawlers (reflexive), Creeping Inn (`WithSource`), Imoen's five faces
  (the specialized trigger is already a `MayEffect` in `SpecializeImoenEffectHandler`, so the helper returns the sequence), Master Skald and
  Masked Vandal (the rider's target is still chosen as the trigger goes on the stack: `GraveyardTargetingSupport` and the AI's
  `TargetPolarityClassifier` unwrap may / sequence / conditional, their `ExileCardFromGraveyardThenEffect` branches are deleted), Aphemia and
  Tymaret Calls the Dead (the Zombie now goes through `CreateTokenEffectHandler`, so token replacements apply; the old handler called
  `applyCreateToken` directly), The Aesir Escape Valhalla chapter I (mandatory, no `MayEffect`: `fromOwnGraveyardWithSource(1, permanent card)`
  then `GainLifeEffect(AmountThisWay.exiledManaValue())`).
- **Behaviour changes.** Ixhel (ONC 1, "exiles the top card of their library face down. You may look at and play those cards"; its ruling "the
  cards remain exiled face down"): the old record exiled face up; now face down with Ixhel's controller as exiler
  (`IxhelScionOfAtraxaTest.exilesTheCardFaceDownForItsController`, fails on the old card). Thought-String Analyst's Lich's-Duel-Mastery-style
  question is now asked by the life-loss step after the exile (as Hotel of Fears in run 10; `LichsDuelMasteryTest` updated). Prompt shapes of the
  nine graveyard cards: a yes/no may, then a forced `GraveyardChoice` of the matching cards only when several match (the old records asked one
  optional multi-graveyard choice even for a lone card; Creeping Inn and Imoen asked a may and then an optional choice); Masked Vandal and Master
  Skald are no longer asked with no creature card in the graveyard; a Creeping Inn / Aesir source that left before resolution no longer tracks
  the card (nothing reads it then). The survivors' log lines win (no test asserted the old ones).
- **AI.** No AI class named a deleted record but `TargetPolarityClassifier`'s `ExileCardFromGraveyardThenEffect` branch (its may / sequence /
  conditional unwrapping gives Masked Vandal's target the same HARMFUL_REMOVAL); `TargetPolarityGuardTest` passes.
- **Tests.** The nine graveyard cards' tests were updated for the prompt shapes by a delegated run (no test deleted; missing `@DisplayName`s added;
  Tymaret gained "only offers own creature or enchantment cards"); it found the doubled Imoen may, fixed in the card. `StepResultsTest` +5: a
  graveyard card chosen at the prompt is the step's result and the rider runs once, a lone match / no match, a two-card exile keeps its filter on
  the second pick, a face-down `PERMANENT` for-life library exile, Malboro's three-card each-opponent exile. `ReflexiveRouteRecordTest`'s
  `EXILE_OWN_GRAVEYARD_CARD` route is rebuilt from the steps. One Gradle run (one `--tests` list per task): the tests of every rewritten card and
  of the cards that use one as a helper, `--tests-for ExileTopCardMayPlayThisTurnEffect ExileTopCardOfOwnLibraryEffect ExileGraveyardCardsEffect
  --max-per-class 12`, `--tests-for MayEffect --max-per-class 15`, application (`StepResultsTest`, `ExileGraveyardCardsEffectHandlerTest`,
  `ReflexiveRouteRecordTest`, `GraveyardTargetingServiceTest`, `MayEffectHandlerTest`, `MayAbilityHandlerServiceTest`,
  `GraveyardChoiceInteractionHandlersTest`, `ResolutionScratchpadAmountTest`, `EffectResolutionServiceTest`, `SubjectResolutionServiceTest`, both
  ratchets, `InputHandlerEpilogueRatchetTest`, `CardImmutabilityArchTest`), AI `TargetPolarityGuardTest`: 120 top-level classes, 1,327 test
  cases (174 result files with the `@Nested` suites), all pass. Application, AI, engine testFixtures and card-test modules `a`–`z` that ran
  compile.

**For later runs.**

- Library exiles still unaudited, with what each needs (read the handler first): `ExileTopCardsMayPlayUntilNextTurnEffect` (53 cards) is now
  `ExileTopCardMayPlayThisTurnEffect(count, false).until(UNTIL_END_OF_YOUR_NEXT_TURN | DURING_YOUR_NEXT_TURN)` for 51 of them; its
  `forTriggeringPermanentController` card needs the permission holder to be another player (with Curse of Hospitality, a who-may-play subject
  now used by two records) and its `freeCastIfCastFromGraveyard` card a free-if-cast-from-graveyard flag; the log changes from one aggregate line
  to one per card. `ExileTopCardsOfDefendingPlayerLibraryEffect` (10 cards) is `ExileTopCardOfOwnLibraryEffect.of(defendingPlayer(), n)` if
  `defendingPlayer()` resolves the same player (its handler also falls back to the attacked permanent's controller). `ExileTopCardMayPlayWhileExiledEffect`
  (4) needs a permission condition and a spell filter on the primitive. Then the choose-one / matching / random-opponent / token records
  (`ExileTopCardsChooseOne*`, `ExileTopCardsMayCastMatching*`, `ExileTopCardOfRandomOpponent…`, `ExileTopCardsMayPlayThisTurnAndCreateTokens*`,
  Hidetsugu and Kairi).
- Raven Guild Master and Sire of Stagnation say "that player" but still declare a (harmful) player target, as their old record did; a rules
  fix would be `triggeringPlayer()` after checking what the two triggers bake.
- Choose-a-card graveyard exiles left: `ExileNCardsFromGraveyardThenEffect` (4 cards and `CreateTokenThenMayExileCardsAndRepeatEffectHandler`'s
  builder): `fromOwnGraveyard(N, filter)` + `HappenedThisWay.atLeast(EXILE, N)`, but with fewer than N matching cards the old record exiled nothing
  and the new step would exile them all, so add an "exactly N or none" guard (a `MayEffectHandler` check like the one for no matching card)
  first; `ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect` (the `afterExileEffects` continuation of `beginGraveyardExileChoice`).
  Hand exiles still record nothing (every hand-exile primitive chooses at a prompt). Luminate Primordial still needs a per-object amount.

### Step 8, run 12 (2026-11-20)

Run 11's "For later runs", library and graveyard exiles. Verdicts (ledger, step `8`): **absorbed 4**
(`ExileTopCardsMayPlayUntilNextTurnEffect`, `ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionUntilEndOfTurnEffect`,
`ExileTopCardsOfDefendingPlayerLibraryEffect`, `ExileTopCardMayPlayWhileExiledEffect`), **split 1** (`ExileNCardsFromGraveyardThenEffect`),
with their handlers; **unique 1** (`CreateTokenThenMayExileCardsAndRepeatEffect`, Insatiable Frugivore; its loop rebuilt from the steps);
**survivor 2** (`ExileTopCardMayPlayThisTurnEffect`, `ExileTopCardOfOwnLibraryEffect`). Ratchet 4,775 / 4,008 → 4,770 / 4,003. 72 card files
rewritten (a deleted scratch script for the 51 plain until-next-turn cards and the 10 ingest / Ulamog cards, the rest by hand); Oracle text of every
rewritten card checked with `get_card` (Oddric's MB2 315 printing is not in Scryfall; it grants ingest), rulings of Expedited Inheritance, Ignite
the Future, Galvanic Relay, Curse of Hospitality, Blightwing Bandit, Neyali, Monk Class, Ulamog, Mist Intruder, Young Necromancer, Kroxa and
Kunoros, Skywarp Skaab (none) and Calim (none) with `get_rulings`. CR 508.5, 702.115a, 603.12 and 608.2c checked with `get_rule` (2026-11-20).

- **Survivor.** `ExileTopCardMayPlayThisTurnEffect` gained `PlayerSubject player` (`playableBy(...)`, default `you()`, never a target: who may play
  the cards, resolved once as the effect resolves; nobody named, nothing is exiled) and `Condition permissionCondition` (`playableOnlyIf(...)`, put
  on the `ExilePlayPermissionEffect` as the old while-exiled record did). The permission, a face-down card's look, the lore-counter renewal and the
  any-mana rider all go to that player, and the duration's "your" is theirs (`grantExilePlayPermission(card, holder, duration)` already took the
  holder as the duration player). The log names `DURING_YOUR_NEXT_TURN` ("during <player>'s next turn"). Readers moved:
  `DealDamageToTargetCreatureEffectHandler.referencesExcessDamage`, `EffectResolution.hasConvergeEffect` (no existing survivor card pairs an
  `EventValue` count with that damage, so nothing else starts tracking excess) and `FatefulTempestEffectHandler`.
- **Until your next turn.** 50 cards and Fateful Tempest: `new ExileTopCardMayPlayThisTurnEffect(n, false).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN)`
  (the old handler's `grantPlayUntilOwnersNextTurn` was that same grant); Galvanic Relay `until(DURING_YOUR_NEXT_TURN)`. Expedited Inheritance
  ("its controller may exile that many cards from the top of their library. They may play those cards until the end of their next turn"):
  `fromLibraryOf(controllerOf(triggering())).playableBy(controllerOf(triggering()))`; the old raw `getTriggeringPermanentControllerId()` is what
  `MayEffectHandler` writes as the choosing player (live controller, else as it left), which `controllerOf(triggering())` reads after the live and
  last-known controller, so the same player. Ignite the Future needs no flag: `ConditionalReplacementEffect(new CastFromZone(Zone.GRAVEYARD),
  normal, free)`; `CastFromZone` also refuses a copy, which was not cast ("If this spell was cast from a graveyard"; the old `getSourceZone()` read
  did not check). The log is now one line per card.
- **Curse of Hospitality.** `fromLibraryOf(triggeringPlayer()).playableBy(controllerOf(triggering())).withAnyManaType()`: the combat-damage entry
  records the damaged player as `targetId` / `triggeringPlayerId` and the creature as the triggering permanent; "that creature's controller" is its
  controller as the trigger resolves (live, else last-known), as the old handler read it. The old fallback to the Curse's controller when nothing
  was recorded is gone (no entry lacks it). The any-mana rider now names the holder; the end-of-turn rider ignores it.
- **Defending player.** `defendingPlayer()` is right for Ulamog only (`of(defendingPlayer(), 20)`: the attack trigger records `defendingPlayerId`,
  the CR 508.5 player, which the old handler also read first; the resolver's fallback `defendingPlayerOfAttackTarget` also knows a battle's
  protector). The 9 ingest cards (8 BFZ creatures and Oddric's granted ingest) are "that player" of CR 702.115a, the damaged player:
  `of(triggeringPlayer())`. Combat-damage entries record no `defendingPlayerId` or attacked target, so `defendingPlayer()` would have named nobody
  there; the old handler fell through to the baked `targetId`, which `triggeringPlayer()` reads (`triggeringPlayerId`, else that `targetId`). An empty
  library now logs a line (the old handler returned silently); one log line per card.
- **While exiled.** Rassilon `until(PERMANENT)`; Neyali `until(PERMANENT).playableOnlyIf(new AttackedWithTokenThisTurn())`; Monk Class
  `mayOnlyCast().until(PERMANENT).playableOnlyIf(ControllerCastSpellThisTurn)` ("You may cast this card", so a land gets no permission instead of
  a permission whose spell filter refused it; test added); Blightwing Bandit below.
- **Graveyard "exactly N".** `MayEffectHandler`'s run-11 guard now offers no choice when the controller's graveyard has fewer matching cards than
  the leading `OWN` exile's count (count 1 unchanged; Emeritus of Ideation and Aegis Sculptor are gated on the same threshold before the may), so
  nothing is exiled and the rider never happens, as the old record did. Young Necromancer, Kroxa and Kunoros, Calim: `MayEffect(SequenceEffect.of(
  fromOwnGraveyard(N, filter), ConditionalEffect(HappenedThisWay.atLeast(EXILE, N), QueueReflexiveAbilityEffect(...))))`. Insatiable Frugivore's
  loop inserts the same shape around its next iteration. The old record's `GraveyardTargetOperationState` fields and their
  `GraveyardChoiceHandlerService` branch are deleted. Prompt shape: with more matching cards than N, one `GraveyardChoice` per card (the survivor's
  one-at-a-time choice inside one graveyard-leave batch) instead of one `MultiGraveyardChoice`; with too few, no may prompt at all.
- **Behaviour changes, by Oracle text.** Blightwing Bandit (WOC 13, "look at the top card of that player's library, then exile it face down")
  exiled its controller's own top card; now `fromLibraryOf(activePlayer())` (the opponent whose turn it is, live while the trigger resolves in
  that turn), face down with the controller as exiler, any mana, `PERMANENT` (`BlightwingBanditTest` rewritten: its fixtures used player 1's
  library; the first case fails on the old card). Skywarp Skaab (VOW 78, "If you do, draw a card") put a reflexive ability on the stack, which is
  CR 603.12's "when you do"; the draw is now a `ConditionalEffect` step of the same ability (`SkywarpSkaabTest.drawsAsPartOfTheSameAbility`,
  fails on the old card). Insatiable Frugivore's "If you do, repeat this process" likewise repeats inside the resolution instead of as a reflexive
  ability. The survivor's log lines win (no test asserted the old ones).
- **AI.** No AI class named a deleted record; `TargetPolarityGuardTest` and `AiTargetSelectorTest` pass.
- **Tests.** One Gradle run (one `--tests` list per task): the tests of the 71 rewritten cards that have one (Compleated Conjurer through
  `CaptiveWeirdTest`) and of the other card tests that construct one, Insatiable Frugivore, Fateful Tempest, Emeritus, Aegis Sculptor, Kishla
  Trawlers, Ichorid, Ultimecia, `--tests-for ExileTopCardMayPlayThisTurnEffect ExileTopCardOfOwnLibraryEffect ExileGraveyardCardsEffect
  --max-per-class 10`: 129 card test classes; application `EffectCountRatchetTest`, `CardImmutabilityArchTest`, `ReflexiveRouteRecordTest` (its
  `EXILE_GRAVEYARD_CARDS` continuation rebuilt from the steps, two cards), `GraveyardChoiceInteractionHandlersTest`, `MayAbilityHandlerServiceTest`,
  `StepResultsTest`, `SubjectResolutionServiceTest`, `EffectResolutionServiceTest`, `GraveyardTargetingServiceTest`, `MayEffectHandlerTest`,
  `ExileGraveyardCardsEffectHandlerTest`; AI `TargetPolarityGuardTest`, `AiTargetSelectorTest`. About 1,580 test cases. Seven card cases failed
  first, all prompt-shape changes (a may that is no longer offered with too few cards, a `MultiGraveyardChoice` that is now one `GraveyardChoice`
  per card); the four test classes were updated and pass on a rerun. `@DisplayName`s were added to `ExpeditedInheritanceTest`,
  `IgniteTheFutureTest` and `MonkClassTest` (Monk Class gained the land case), Young Necromancer and Kroxa gained a choice-shape case. Application,
  AI, engine testFixtures and card-test `a b c d e f g h i k l m n o p r s t u v w y z` test sources compile.

**For later runs.**

- "[Another player] may play those cards" is `.playableBy(subject)`; "during any turn …, you may play that card" / "you may cast it as long as …"
  is `.playableOnlyIf(condition)`; "during your next turn" is `.until(DURING_YOUR_NEXT_TURN)`; "if this spell was cast from a graveyard, … without
  paying" is a `ConditionalReplacementEffect(CastFromZone(GRAVEYARD))` over the free form. "You may exile N cards from your graveyard. If / When you
  do" is `MayEffect(SequenceEffect.of(fromOwnGraveyard(N, filter), ConditionalEffect(HappenedThisWay.atLeast(EXILE, N), …)))`.
- Next for Exile (read each handler first): the library records run 11 named and this run did not reach: `ExileTopCardsChooseOne*`,
  `ExileTopCardsMayCastMatching*` (a cast filter: `freeCastFilter` already exists for the free half; a play filter may be `mayOnlyCast` plus a
  `CardPredicate`), `ExileTopCardOfRandomOpponent…` (random opponent: no subject), `ExileTopCardsMayPlayThisTurnAndCreateTokens*`, Hidetsugu and
  Kairi; then the graveyard "and" records, the largest unaudited multi-card ones: `ExileTargetCardFromGraveyardAndCreateTokenCopyEffect` (15
  cards), `ExileTargetCardFromGraveyardAndMayCastCopyEffect` (14), `ExileTargetCardFromGraveyardAndImprintOnSourceEffect` (10), and
  `ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect` (the `afterExileEffects` continuation). `ExileTargetPermanentAndTrackWithSourceEffect`
  (24) is a tracking variant, not a fused one.
- Raven Guild Master and Sire of Stagnation still declare a harmful player target for "that player" (run 11).

**How much of step 8 is left (estimate).** `effect-inventory.py --unaudited`: 774 unaudited records whose name has `Then` / `And` / `IfYouDo` /
`Unless`, 143 of them used by two or more cards (1,165 card uses; by first word Exile 25, Search 10, Sacrifice 9, Reveal 9, Counter 8, Prevent 7,
Return 7, Destroy 7, Draw 6, Each 6). Roughly a third are not two instructions (the `CantAttack/BlockUnless` and `CounterUnless*` restrictions and
taxes, the "and/or graveyard" searches, `DrawDiscardAndConnive`), so about 90 to 100 fused multi-card families remain: some six or seven runs at this
run's size, the Exile family two of them. Counting nested-`CardEffect` records too gives 236 multi-card records, but most of the extra are wrappers
(`ConditionalEffect`, `MayEffect`, `SequenceEffect`, `MayPayManaEffect`).

### Step 8, run 13 (2026-11-20)

Run 12's "Next for Exile": the library records it named, then the graveyard records as far as the budget went. Verdicts (ledger,
step `8`): **absorbed 6** (`ExileTopCardsChooseOneMayPlayThisTurnEffect`, `ExileTopCardsChooseOneMayPlayUntilNextEndStepEffect`,
`ExileTopCardsChooseOneMayPlayUntilNextTurnEffect`, `ExileTopCardsMayCastMatchingThisTurnEffect`, `ExileTopCardsMayCastMatchingUntilNextTurnEffect`,
`ExileTopCardsMayPlayUpToNUntilNextTurnEffect`), **split 2** (`ExileTopCardsMayPlayThisTurnAndCreateTokensEffect`,
`ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect`), with their handlers; **blocked-other 2**
(`ExileTopCardOfRandomOpponentLibraryAndGrantPlayPermissionUntilEndOfTurnEffect`, `ExileTopCardLoseLifeEqualToManaValueAndMayCastInstantOrSorceryEffect`),
**unique 1** (`ExileTopCardsMayPlayThisTurnAndCreateTokensForStillExiledEffect`); **survivor 1** (`ExileTopCardMayPlayThisTurnEffect`).
Ratchet 4,770 / 4,003 → 4,762 / 3,995. 32 card files rewritten (a deleted scratch script for the 28 library cards, Bonehoard
Dracosaur, Heated Argument, Raven Guild Master and Sire of Stagnation by hand); Oracle text of every one checked with `get_card`, rulings
of Narset, Chandra Hope's Beacon, Magmatic Channeler, March of Reckless Joy and Heated Argument with `get_rulings`. No CR number is cited
below.

- **Survivor.** `ExileTopCardMayPlayThisTurnEffect` gained three components (old 12-argument constructor kept):
  - `CardPredicate castFilter` (`mayOnlyCast(filter)`, also sets `castOnly`): "you may cast [red | instant or sorcery | noncreature]
    spells from among them", "if it's a nonland card, you may cast it". A card is permitted when its front face, a modal double-faced
    card's back face or its Adventure matches (Chandra, Hope's Beacon's ruling: "if you exile a creature card that has an Adventure, you
    can cast that Adventure spell"), and the permission carries the filter as its spell filter, as both old records did.
  - `int playLimit` (`upTo(n)`): "you may play one of those cards", "up to two", "an instant or sorcery spell from among those": the
    permitted cards share one `registerExilePlayPermissionGroup`, then one log line ("<holder> may play one of those cards …").
  - `boolean chooseOne` (`choosingOne()`): "choose one of them. You may play that card": the cards are exiled and recorded, then the
    holder answers the existing `ExiledCardMayPlayChoice` (duration, any-type mana and free play carried; never cast at once). The
    step pauses at its end with its EXILE result recorded, so a later step may read it (`StepResultsTest.chooseOneOfThemEndsTheStepOnTheChoice`:
    the rider runs after the choice). The compact constructor refuses `chooseOne` with the per-card options (cast filter, limit, face
    down, for life, condition, free filter, lore renewal).
  - `targetSpec()`: a target library stays harmful, but is benign when `player` is `targetPlayerDeclaredElsewhere()` (the target player
    gets the permission: Casey & Raph, as its old record declared).
  - The handler gained `InteractionHandlerRegistry` (no hand-built wiring names it) and the `UNTIL_YOUR_NEXT_TURN` log text (Nivix).
- **Rewritten cards.** Choose one: Magmatic Channeler, Jaya, Fireglass Mentor, Chandra Flameshaper, Case of the Burning Masks (this
  turn); Tectonic Giant, Strongbox Raider, Riverwheel Sweep, Mishra's Research Desk, Heroes' Hangout, Feldon, End-Blaze Epiphany
  (`until(UNTIL_END_OF_YOUR_NEXT_TURN)`), You Find Some Prisoners (`fromLibraryOf(targetPlayer()).withAnyManaType()`). One of them:
  Electrostatic Blast (`upTo(1)`), Unlucky Witness (`until(UNTIL_YOUR_NEXT_END_STEP).upTo(1)`), March of Reckless Joy (`upTo(2)`). One card,
  no choice: Yasmin Khan (`until(UNTIL_YOUR_NEXT_END_STEP)`), Casey & Raph (`fromLibraryOf(targetPlayer()).playableBy(targetPlayerDeclaredElsewhere())`,
  free; `targetPlayerDeclaredElsewhere()` reads the same `targetsForEffect` target). Cast filter: Vance's Blasting Cannons, Narset (free),
  Meeting of the Five, Dream Pillager, Chandra Dressed to Kill (both), Apex of Power; Rundvelt Hordemaster, Eager Flameguide,
  Chandra Hope's Beacon (`upTo(1)`) until the end of your next turn; Nivix `until(UNTIL_YOUR_NEXT_TURN)`. Split: Bonehoard Dracosaur
  (`SequenceEffect.of(exile 2, ConditionalEffect(toACard(EXILE, land), Dinosaur), ConditionalEffect(toACard(EXILE, nonland), Treasure))`;
  the tokens now go through `CreateTokenEffectHandler`, so token replacements apply), Heated Argument (`MayEffect(SequenceEffect.of(
  fromOwnGraveyard(1, not this card), ConditionalEffect(exiled(), DealDamageToPlayersEffect(2, controllerOf(target())))))`; the
  not-this-card filter keeps the old handler's exclusion of the resolving spell). Raven Guild Master `of(triggeringPlayer(), 10)` and Sire
  of Stagnation `of(controllerOf(triggering()), 2)` ("that player"): neither declares a player target any more (run 11's open item).
- **Behaviour changes.** Narset, Vance, Meeting of the Five, Dream Pillager, Chandra Dressed to Kill and Apex of Power now permit an
  Adventure card whose Adventure matches (the this-turn record checked modal back faces only; the until-next-turn record already checked
  Adventures): `NarsetEnlightenedMasterTest.creatureCardWithANoncreatureAdventureIsPermitted` fails on the old card. Prompt shapes: Yasmin
  Khan and Casey & Raph no longer ask a one-of-one choice; Heated Argument no longer offers its "you may" with no other card in the
  graveyard (`MayEffectHandler`'s run-11 guard). The target spec of You Find Some Prisoners' mode is harmful now (benign before; its
  opponent filter leaves one legal player). Log lines are the survivor's: one line per exiled card; the cast-filter and choose-one cards
  name the permission per card or after the choice instead of one aggregate line. Dream Pillager's count is evaluated with the source as it
  is or last was (it read none; `EventValue` needs none).
- **Blocked / unique.** The Ruinous Powers: no random-opponent `PlayerSubject`, and its "when you cast a spell this way" delayed trigger
  names the exiled card. Hidetsugu and Kairi: the exile and `LoseLifeEffect(AmountThisWay.exiledManaValue(), targetPlayer())` would split,
  but no step offers "you may cast it without paying its mana cost" now for the card an EXILE step recorded (a `MayCast…ExiledThisWay`
  primitive would also serve the `ExileTopUntilNonland…MayCast…` and `MillTargetPlayerAndMayCastMilledSpell` records; build it when two
  of them can take it). Glimpse the Impossible: its end-step delayed trigger about the cards exiled this way has no step-reading primitive.
- **AI.** No AI class named a deleted library record; `TargetPolarityClassifier`'s `ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect`
  entry is deleted (Heated Argument's target step is `DealDamageToTargetCreatureEffect`). `TargetPolarityGuardTest`, `AiTargetSelectorTest`
  and `SpecializedInteractionAiStrategiesTest` (the `ExiledCardMayPlayChoice` strategy) pass.
- **Tests.** One Gradle run (one `--tests` list per task): the tests of the 32 rewritten cards and of the 31 other card tests that construct
  one, `--tests-for ExileTopCardMayPlayThisTurnEffect --max-per-class 15`, `--tests-for ExileGraveyardCardsEffect MayEffect
  ExileTopCardOfOwnLibraryEffect --max-per-class 8`, application (`StepResultsTest` +2: choose one ends the step on the choice, a cast filter
  permits a matching Adventure; `EffectCountRatchetTest`, `CardImmutabilityArchTest`, `SubjectResolutionServiceTest`,
  `EffectResolutionServiceTest`, `GraveyardTargetingServiceTest`, `MayEffectHandlerTest`, `MayAbilityHandlerServiceTest`,
  `ExileGraveyardCardsEffectHandlerTest`, `GraveyardChoiceInteractionHandlersTest`, `ReflexiveRouteRecordTest`, `StepTriggerServiceTest`,
  `TriggeredAbilityStackServiceTest`, `ValidTargetServiceTest`, `TargetLegalityServiceTest`, `EtbTriggerServiceTest`,
  `ActivatedAbilityExecutionServiceTest`) and the three AI tests: 119 top-level classes (219 result files with the `@Nested` suites), 2,002
  test cases, all pass; Narset's new case passes. Updated: `YasminKhanTest` (no choice; `@DisplayName`s added), `CaseyRaphHotheadsTest` (no
  choice; `@DisplayName`s added, +1: an opponent targeted by the exile mode gets the permission), `HeatedArgumentTest` (no may prompt with an
  empty graveyard). Application, AI, engine testFixtures and card-test `a b c d e f h j m n r s t u v y` test sources compile.

**For later runs.**

- "Choose one of them. You may play that card [duration]" is `new ExileTopCardMayPlayThisTurnEffect(n, false)[.until(d)].choosingOne()`;
  "you may play one / up to N of those cards" is `.upTo(n)`; "you may cast [filter] spells from among them" is `.mayOnlyCast(filter)`.
- Next for Exile, the graveyard "and" records this run did not reach (read each handler and its engine readers first):
  `ExileTargetCardFromGraveyardAndImprintOnSourceEffect` (10 cards; readers `GraveyardTargetValidators`, `ValidTargetService`,
  `GraveyardTargetingSupport`, `GraveyardReturnSupport`, `AiTargetSelector`, `ValidTargetServiceTest`, `AiTargetSelectorTest`) and
  `ExileTargetCardFromGraveyardAndTrackWithSourceEffect` (6) are tracking variants of `ExileGraveyardCardsEffect`'s target scopes
  (`trackWithSource` exists; imprint and the spell's `setImprintedCard` fallback do not), not fused records;
  `ExileTargetCardFromGraveyardAndCreateTokenCopyEffect` (15, 20 components) needs a token-copy step reading the EXILE result;
  `ExileTargetCardFromGraveyardAndMayCastCopyEffect` (14, 321-line handler) a may-cast-a-copy step reading it. Each is probably a run.
- A "you may cast the card exiled this way without paying its mana cost" step (now, at resolution) would unblock Hidetsugu and Kairi.

### Step 8, run 14 (2026-11-20)

Run 13's "Next for Exile", graveyard "and" records. Verdicts (ledger, step `8`): **absorbed 2**
(`ExileTargetCardFromGraveyardAndImprintOnSourceEffect`, `ExileTargetCardFromGraveyardAndTrackWithSourceEffect`, with their handlers),
**survivor 1** (`ExileGraveyardCardsEffect`), **blocked-other 1** (`ExileTargetCardFromGraveyardAndCreateTokenCopyEffect`, where the run stopped);
`ExileTargetCardFromGraveyardAndMayCastCopyEffect` not started. Ratchet 4,762 / 3,995 → 4,760 / 3,993. 16 card files rewritten (a deleted
scratch script); Oracle text of every one checked with `get_card`, rulings of Blue Mage's Cane, Surge to Victory and Mirror Golem (none) with
`get_rulings`. No CR number is cited below.

- **Neither record was fused.** Both were "exile target card from a graveyard", tracked with the source; the imprint record also called
  `ExileService.setImprintedCardOnPermanent`, and the track record, for a spell (no source permanent), `GameData.setImprintedCard(spell card)`
  (Surge to Victory's delayed trigger copies "the exiled card" through `CopyImprintedCardAndMayCastCopyEffect`). Every scope they used maps to a
  target scope of `ExileGraveyardCardsEffect`: `ALL_GRAVEYARDS` → `exactTargetedFromAnyGraveyard(1, filter, true)`, `CONTROLLERS_GRAVEYARD` →
  `oneTargetFromControllerGraveyardWithSource(filter)`, Blue Mage's Cane's up-to-one `OPPONENT_GRAVEYARD` → new
  `upToOneTargetFromOpponentGraveyardWithSource(filter)`.
- **Survivor.** `ExileGraveyardCardsEffect` gained a 13th component `imprint` (`imprintingOnSource()`, also sets `trackWithSource`; the compact
  constructor refuses it on a scope with no target card; the old 12-argument constructor is kept): the handler's target paths imprint the
  exiled card on the source permanent, or remember it on the resolving spell's card when there is no source permanent, exactly the two
  records' branches. Imprint cards: Mirror Golem, Mourner's Shield, Myr Welder, Death-Mask Duplicant, Dino DNA, Rona, Wizard's Spellbook,
  Dawnhand Dissident, Boiling Rock Rioter, Blue Mage's Cane, Surge to Victory; tracking only: Ghost Vacuum, Lazav, Izzet Chemister,
  Underworld Sentinel, Trove Warden. The `TARGET_CARDS_OPPONENT_GRAVEYARD` path now honours `trackWithSource` (no existing user set it).
  Target specs, prompts and target groups are unchanged: the survivor declares the same benign graveyard target with the filter, and
  `GraveyardTargetingSupport`'s generic and `ExileGraveyardCardsEffect` branches give the same `Target` (min 1, or 0 for the Cane).
- **Readers.** The imprint record's branches in `GraveyardTargetValidators`, `ValidTargetService.matchesGraveyardEffectTypeFilter`,
  `GraveyardTargetingSupport`, `AiTargetSelector` and an orphan Javadoc in `GraveyardReturnSupport` are deleted (the survivor's branches cover
  them; the AI's spell-target filter only ever saw spells, and no imprint card is one). The track record had no reader. Parity fixes kept for
  the moved cards: `ValidTargetService`'s activated-ability controller-graveyard enumeration now skips land cards an opponent's Tomik makes
  untargetable, as the generic path the two records used did (it applies to Quintorius Kand's −6 too), and `validateExileGraveyardCards`
  falls back to the source permanent's controller when the context has none, as the imprint validator did.
- **Behaviour.** No rules change. The survivor's log lines win; a controller-graveyard target exile also writes the exiled count to
  `eventValue` (no rewritten card reads it), and every rewritten card now records an EXILE step. Death-Mask Duplicant's and Izzet Chemister's
  activation prompt reads "Select 1 target cards from your graveyard" (the controller-graveyard branch's text; min and max still 1).
- **AI.** `AiTargetSelectorTest` and `ValidTargetServiceTest`'s imprint cases now build the survivor; both pass. No capability interface
  involved.
- **Stopped at `ExileTargetCardFromGraveyardAndCreateTokenCopyEffect`** (15 cards, 20 components; blocked-other in the ledger). The plan's
  premise was "if the existing token-copy primitive can take the exiled card as its subject": it can't. `CreateTokenCopyOfTargetPermanentEffect`
  resolves a `PermanentSubject` to permanent ids and copies a live permanent or its last-known snapshot; nothing names a card in exile, and
  `thisWay(EXILE)` is refused as a permanent subject. `CreateTokenCopyOfCardEffect` holds a fixed `Card` (engine-built). Splitting needs a
  decision first: a card-subject on the token-copy primitive (e.g. a `PermanentSubject` variant for "the cards an EXILE step recorded" that
  the copy handler reads from exile), or a new token-copy-of-exiled-card primitive. The record's other parts then need: Feldon of the Third
  Path's copy without exiling (a copy of a target graveyard card: the same card subject, read from the graveyard), Soul Separator's Zombie
  (`CreateTokenEffect` with `AmountThisWay.exiledPower()` / `exiledToughness()` if it takes dynamic P/T), Abyssal Harvester's "exile all other
  Nightmare tokens you control" (an exile of a set excluding `createdThisResolution()`), the tapped-and-attacking copy (Mordor on the March,
  Gyrus), `targetPutIntoGraveyardFromAnywhereThisTurn` (a target filter; `ValidTargetService` and the validator special-case it by class).

**Tests** (one Gradle run, one `--tests` list per task): the tests of the 16 rewritten cards and of the two other card tests that construct one
(Doran, Besieged by Time; Dermotaxi), `--tests-for ExileGraveyardCardsEffect --max-per-class 12`, 14 more `ExileGraveyardCardsEffect` target-scope
cards (Quintorius, Quintorius Kand, Nyla, Zethi, Rag Dealer, Deadeye Tracker, General Kudro, Ethereal Absolution, Zombie Cannibal, Spellweaver
Helix, Graven Abomination, Sheoldred's Assimilator, Withered Wretch, Scrabbling Claws) and Tomik, application (`ExileGraveyardCardsEffectHandlerTest`
+4: an ability imprints on its source permanent, a spell remembers the card on itself, tracking alone doesn't imprint, a non-target scope
refuses it; `ValidTargetServiceTest`, `TargetLegalityServiceTest`, `GraveyardTargetingServiceTest`, `StepResultsTest`, `EffectCountRatchetTest`,
`CardImmutabilityArchTest`, `MayEffectHandlerTest`, `EffectResolutionServiceTest`, `MayAbilityHandlerServiceTest`,
`ActivatedAbilityExecutionServiceTest`, `TriggeredAbilityStackServiceTest`), AI (`AiTargetSelectorTest`, `TargetPolarityGuardTest`): 58 top-level
classes (124 result files with the `@Nested` suites), 1,153 test cases, all pass. Application, AI, engine testFixtures and card-test
`b d g i l m r s t u w` test sources compile.

**For later runs.**

- "Imprint — exile target card from a graveyard" is `ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, filter, true).imprintingOnSource()`
  (`oneTargetFromControllerGraveyardWithSource(filter)` for "from your graveyard"); "exile target card from a graveyard" whose card the source
  later reads as "exiled with this" is the same without `imprintingOnSource()`.
- Next for Exile: decide the card subject for token copies (above), then split `ExileTargetCardFromGraveyardAndCreateTokenCopyEffect`'s cards.
  `ExileTargetCardFromGraveyardAndMayCastCopyEffect` (14 cards, 321-line handler plus a `mayfx` handler, 7 engine readers) needs a
  "copy the card an EXILE step recorded; you may cast the copy" step, the same primitive run 13 named for Hidetsugu and Kairi
  (`ExileTopCardLoseLifeEqualToManaValueAndMayCastInstantOrSorceryEffect`) and that `RandomPlayerExilesInstantOrSorceryAndMayCastCopyEffect`,
  `ExileOwnGraveyardInstantsOrSorceriesAndMayCastCopiesEffect` and `ExileGraveyardCardsAndMayCastCopiesEffect` might share: one run to build it
  and fold those, after the human signs off on its shape. Blue Mage's Cane says "defending player's graveyard" (its record kept the
  opponent-graveyard scope; `fromDefendingPlayerGraveyard()`'s `defendingPlayerOnly` differs only with more than one opponent).

### Step 8, run 15 (2026-11-20)

Family choice: run 14 left the Exile family on a human decision (a "card an EXILE step recorded" subject for the token-copy
and may-cast-a-copy steps), so its records, Hidetsugu and Kairi stay as they are. Of the other first-word families with
`Then` / `And` / `IfYouDo` / `Unless` / `OrElse` names or a nested `CardEffect` (`--unaudited`), Return had the most
split-able multi-card records whose first half records a step: `ReturnToHandEffect` (step 7) for the target ones, and
`ReturnPermanentControlledByPlayerToHandEffect` once it records (a run-9-size engine change, done first). Grant's nested
effects are static abilities, not second instructions; Search's are mostly "and/or graveyard"; Sacrifice's remainder is
mostly "unless". Verdicts (ledger, step `8`): **split 6** (`ReturnPermanentControlledByPlayerToHandThenEffect`,
`ReturnOneOfCombatDamageDealersToHandThenEffect`, `ReturnPermanentOwnedByPlayerToHandThenEffect`,
`ReturnPermanentAndPutCounterOnSourceEffect`, `ReturnTargetPermanentToHandWithManaValueConditionalEffect`,
`ReturnTargetCreatureToHandThenCreateTokensEqualToManaValueEffect`), **absorbed 1** (`ReturnTargetAndAttachedMatchingToHandEffect`),
with their handlers; **survivor 4** (`ReturnPermanentControlledByPlayerToHandEffect`, `ReturnToHandEffect`, `HappenedThisWay`,
`RegisterDelayedCreateTokenAtNextUpkeepEffect`); **unique 1** (`ReturnUpToNTargetPermanentsToHandEffect`, Devastating Mastery
only now); **blocked-other 9** (`ReturnTargetPermanentsThenEffect` with Paradoxical Outcome, the five "Unless" records, the two
perpetual riders, Supplant Form's token copy); blocked-step-model 0, blocked-player-loop 0. `ReturnSourceAsAuraEffect` (3 cards) is
left unaudited: its nested effect is the static ability the returned Aura gets, not a second instruction. Ratchet 4,760 / 3,993 →
4,753 / 3,986. 23 card files rewritten (a deleted scratch script); Oracle text of every one checked with `get_card`, rulings of
Temur Sabertooth, Mistbreath Elder, Kaito, Wanderwine Farewell, Paradoxical Outcome and The Eagles Are Coming! with `get_rulings`.
CR 603.10a, 707.2 and 608.2c checked with `get_rule` (2026-11-20).

- **The chosen return is a step.** `ReturnPermanentControlledByPlayerToHandEffect` records `StepOutcome.NONE` for RETURN as it
  begins; its `PermanentChoiceContext.BounceCreature(bouncingPlayerId, recordsStep)` completion adds the returned permanent and
  the owner of the hand its card went to to the parked entry's outcome (`addToStep`), so the step ends on the choice and a later
  step reads it (`StepKind` Javadoc lists it). The other `BounceCreature` users (Sunken Hope, upkeep bounces, the may-penalty and
  sharing-type returns) don't record. The record lost `thenCondition` / `thenEffect` and gained `PermanentSubject candidates`
  (`choosingOneOf(subject, noun)`: the controller chooses among the live permanents a subject names); its filter context now
  has the source permanent (Mistbreath Elder's "another creature" is `PermanentIsSourcePermanentPredicate`). The prompt reads
  "Choose another creature …" instead of "Choose a another creature …". `HappenedThisWay.toA(RETURN, filter)` judges a returned
  permanent as it last existed (the departure snapshot, as for SACRIFICE / DESTROY).
- **Split, chosen return** (`MayEffect` around the sequence for "you may"): Geothermal Kami, Indoctrination Attendant, Slab Hammer
  (`ConditionalEffect(HappenedThisWay.returned(), X)`), Pyroclastic Hellion ("When you do": X in a `QueueReflexiveAbilityEffect`,
  as before), Arid Archway (`toA(RETURN, another Desert)`), Kaito (`choosingOneOf(triggeringAll())` + gate around its own may),
  Zurgo and Ojutai (`choosingOneOf(triggeringAll())`, no rider), Phenomenon Investigators (`choosingOneOf(ownedBy(you(), nonland))`),
  Mistbreath Elder (gate for the counter, `NotCondition(returned())` for "Otherwise, you may return this creature"), Temur Sabertooth
  (`MayEffect(controller(another creature) + gate)`; its ruling: you choose whether and which as it resolves).
- **Split, target return** (`ReturnToHandEffect.target()`, which records RETURN): Nurturing Pixie and Stockpiling Celebrant (gate),
  Niambi (gate + `GainLifeEffect(TargetManaValue)`), The Kami War II (ungated "Then", `target(chapter filter)`), Wanderwine Farewell
  (`AmountThisWay(StepKind.RETURN)` inside the Merfolk condition), The Eagles Are Coming! (gate + the delayed tokens'
  `AmountThisWay(RETURN)`, evaluated by `RegisterDelayedCreateTokenAtNextUpkeepEffectHandler` as it registers), Perilous Voyage and
  Serum Snare (`TargetPermanentMatches(PermanentMaxManaValuePredicate(n))`, the departed target's mana value), Aether Mutation
  (`CreateTokenEffect(TargetManaValue)`). Target specs are unchanged: every target step is a benign permanent target with the
  card's filter, as the records declared, and `RemovalEffect` BOUNCE, so the AI's polarity is the same.
- **Absorbed.** `ReturnToHandEffect.targetCreatureAndAttached(attachedFilter)` = `allOf(attachmentsOf(target(), filter), target())`
  with a creature target filter (Hubris, Orzhov Charm, Portal of Sanctuary, Word of Undoing).
- **Engine change: a batch bounce looks back (CR 603.10a).** `BounceSupport.applyReturnPermanentsToHand` (every non-target
  `ReturnToHandEffect` subject, three other mass-bounce handlers and the opponent-chosen bounce) now runs its returns inside
  `PermanentRemovalService.performSimultaneousRemovals`, as the attached record's handler did: leave abilities judge the board as it
  was before any of them left. Hubris', Word of Undoing's and Portal of Sanctuary's look-back tests (Darksteel Mutation on
  Thragtusk, Funeral March) failed on the leave batch alone and pass with it. `ReturnToHandEffectHandlerTest` stubs the mocked
  `performSimultaneousRemovals` to run its removals.
- **Removed context paths.** `PermanentChoiceContext.BouncePermanentThen` and `ReturnPermanentAndPutCounterOnSource` and their
  completions are gone; `MultiPermanentChoiceContext.ReturnTargetPermanentsToHand` lost `thenEffect`.
- **Behaviour changes.** Temur Sabertooth asks "you may" and then a permanent choice instead of an up-to-one multi-select (its
  tests updated). Mistbreath Elder's "Otherwise" also follows a chosen return that didn't happen (an exile replacement), as its
  ruling reads "If you don't"; the self-return may is asked as a step of the trigger, not as a parked pending may. Wanderwine
  Farewell and The Eagles Are Coming! count permanents returned, not cards (a melded permanent once: "for each permanent / creature
  returned"). Niambi's and Aether Mutation's mana value is the permanent's (`getPermanentManaValue`: a face-down creature's 0).
  Kaito's, Zurgo's and Phenomenon Investigators' choice prompts lost the card-name prefix. The survivors' log lines win.
- **AI.** No AI class named a deleted record; the split cards' target steps are `RemovalEffect` BOUNCE as the records were.
- **Tests.** `StepResultsTest` +4 (a chosen return ends the step on the choice and its "if you do" runs after it; nothing to choose
  is no return; `toA(RETURN)` judges the returned permanent as it last existed; `choosingOneOf` offers only the subject's
  permanents). `ReflexiveRouteRecordTest`'s `BOUNCE_THEN` route is rebuilt from the steps. One Gradle run of card tests (one
  `--tests` list per task): the tests of every rewritten card, of Devastating Mastery and Paradoxical Outcome, of the 40 other card
  tests that construct one of them, every card test naming `BounceCreature`, `--tests-for ReturnPermanentControlledByPlayerToHandEffect
  --max-per-class 60`, `--tests-for ReturnToHandEffect --max-per-class 30`, `--tests-for` the up-to-N, half-creatures, delayed-token,
  three mass-bounce, upkeep-bounce and sharing-type return records: 170 classes, 1,362 test cases (top-level suites), all pass
  after the look-back fix and the Temur update. Application (`StepResultsTest`, `ReflexiveRouteRecordTest`,
  `ReturnToHandEffectHandlerTest`, `BouncePermanentOnUpkeepEffectHandlerTest`, `PermanentChoiceInteractionHandlerTest`,
  `MultiSelectionInteractionHandlersTest`, `PlayerInputServiceTest`, `MayEffectHandlerTest`, `EffectResolutionServiceTest`,
  `SubjectResolutionServiceTest`, `ResolutionScratchpadAmountTest`, both ratchets, `CardImmutabilityArchTest`, two others):
  374 test cases, all pass; AI `TargetPolarityGuardTest`, `SpellEvaluatorTest`: 98, all pass. Application, AI, engine testFixtures
  and card-test `a g h i k m n o p s t w z` test sources compile.

**For later runs.**

- "You may return a [filter] you control to its owner's hand. If you do / When you do" is
  `MayEffect(SequenceEffect.of(ReturnPermanentControlledByPlayerToHandEffect.controller(filter, noun), ConditionalEffect(HappenedThisWay.returned(), X)))`;
  "return one of them" `choosingOneOf(Subjects.triggeringAll(), noun)`; "if a [kind] was returned this way" `HappenedThisWay.toA(StepKind.RETURN, filter)`.
  "Return target …. If its mana value was N or less" is `ReturnToHandEffect.target()` + `TargetPermanentMatches(PermanentMaxManaValuePredicate(N))`.
- Still fused in Return: Paradoxical Outcome (a "cards returned to your hand" count, melded components each: an `AmountThisWay`
  stat over the RETURN objects' departed snapshots would do it once a second card needs it), Supplant Form (a token copy of a
  face-down departed target), the perpetual riders, `ReturnCreatureToHandAndPutCountersOnSourceEqualToPowerEffect` (First
  Responder: needs `AmountThisWay` POWER for RETURN), Echoing Truth / Mists of Lórien (same name / same mana value as the target
  among all permanents).
- **Next run: the Destroy family.** Its split-able multi-card records: `DestroyTargetPermanentWithManaValueConditionalEffect`
  (3 cards, this run's Perilous Voyage pattern), `DestroyTargetPermanentAndAllWithSameNameEffect` (5; with Echoing Truth and Mists
  of Lórien it wants `otherCreaturesSharing` widened to any permanent with a filter, `SharedQuality.NAME` / a new `MANA_VALUE`),
  `DestroyBlockedCreatureAndSelfEffect` (2, `allOf`), `DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect` (2, run 3's
  search subject), `DestroyUpToTargetsThenReturnFromGraveyardEffect` (2), and `DestroyAllPermanentsEffect`'s `thenEffect` rider
  (260 cards use the record; count the rider's users first). Skip the two "Unless" records and
  `DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect` unless its halves separate.
- **Remaining fused multi-card records** (`--unaudited`, used by two or more cards, a `Then` / `And` / `IfYouDo` / `Unless` /
  `OrElse` name): 137 (1,275 card uses); by first word Exile 22, Search 10, Sacrifice 10, Reveal 9, Counter 8, Prevent 7,
  Destroy 7, Draw 6, Each 6, Cant 5, Choose 5, Put 4. About a third are not two instructions ("unless" taxes and restrictions,
  "and/or graveyard" searches), so roughly 90 split-able families remain.

### Step 8, run 16 (2026-11-20)

The Destroy family, as run 15's Findings named it. Verdicts (ledger, step `8`): **split 3**
(`DestroyTargetPermanentWithManaValueConditionalEffect`, `DestroyTargetWallDealManaValueDamageToControllerEffect`,
`DestroyTargetPermanentsThenEffect`), **absorbed 6** (`DestroyTargetPermanentAndAllWithSameNameEffect`,
`ReturnTargetPermanentAndAllWithSameNameToHandEffect`, `ReturnTargetPermanentAndAllWithSameManaValueToHandEffect`,
`DestroyTargetAndOtherEnchantmentsSharingColorEffect`, `DestroyBlockedCreatureAndSelfEffect`,
`DestroyEquippedCreatureAndCombatOpponentEffect`), with their handlers; **survivor 1** (`PermanentSubject`), **blocked-player-loop 1**,
**blocked-other 10**, blocked-step-model 0, unique 0. Ratchet 4,753 / 3,986 → 4,744 / 3,977. 23 card files rewritten: 16 by hand (Oracle text of each
checked with `get_card`; rulings of Maelstrom Pulse, Isle of Vesuva, Containment Breach, Seedship Impact, Extinguish the Light (none),
Echoing Truth (none), Mists of Lórien, Leave No Trace (none), Loyal Sentry, Dead-Iron Sledge, Word of Blasting (none) and Sylvan
Primordial with `get_rulings`), 7 by the record rename only (Cleansing Beam, Wojek Embermage, Brightflame, Homing Lightning, Izzet
Staticaster, Coward // Killer, Sever the Bloodline). CR 608.2b, 608.2h, 603.10a and 708.2 checked with `get_rule` (2026-11-20).

- **Subject widened.** `PermanentSubject.OtherCreaturesSharing` became `OtherPermanentsSharing(permanent, quality, filter)`:
  every permanent matching `filter`, other than the one `permanent` names, sharing the quality with it; nothing once the named
  permanent is gone or no longer matches `filter`. `Subjects.otherCreaturesSharing(p, q)` is the creature filter (old meaning);
  `Subjects.otherPermanentsSharing(p, q, filter)` is new. `SharedQuality.MANA_VALUE` compares `getPermanentManaValue` (face down
  0, a transformed permanent its front face's); `NAME` now also matches when either permanent has all card names (not face down),
  as the Echoing Truth record did. Readers renamed: `DealDamageToPermanentsTargetControlsEffect`, `SubjectResolutionService`,
  `SubjectResolutionServiceTest`, the seven cards above. The filter is judged with the resolver's set filter context.
- **Rewritten cards.** Mana value (run 15's Perilous Voyage pattern): Extinguish the Light, Seedship Impact, Containment Breach =
  `DestroyTargetPermanentEffect()` + `ConditionalEffect(TargetPermanentMatches(PermanentMaxManaValuePredicate(n)), X)`, ungated
  ("If its mana value was …", and the Containment Breach ruling: an indestructible target that isn't destroyed still makes the
  Pest; the departed target is judged as it last existed). Same name: Maelstrom Pulse, Wake of Destruction, Echoing Ruin, Echoing
  Calm = `DestroyTargetPermanentEffect(allOf(target(), otherPermanentsSharing(target(), NAME, filter)))`, Isle of Vesuva with
  `otherCreaturesSharing(target(), NAME)` (one batch; the rulings say the others die even when the target is indestructible or
  regenerates, and an illegal target makes nothing happen, CR 608.2b); Echoing Truth `ReturnToHandEffect(allOf(target(),
  otherPermanentsSharing(target(), NAME, any)))`, Mists of Lórien the same with `MANA_VALUE` and a nonland filter; Leave No Trace
  `allOf(target(), otherPermanentsSharing(target(), COLOR, enchantment))`. Block triggers: Loyal Sentry and Alaborn Zealot
  `DestroyTargetPermanentEffect(allOf(triggering(), source()))` (the block trigger records the blocked attacker as triggering; the
  Loyal Sentry ruling: the attacker is destroyed even if the Sentry left), Dead-Iron Sledge `allOf(triggering(), target())` (both
  its triggers record the equipped creature as triggering and the combat opponent as the non-targeting target; its ruling: moving
  the Equipment changes nothing). Word of Blasting `DestroyTargetPermanentEffect(true)` + `DealDamageToPlayersEffect(TargetManaValue,
  controllerOf(target()))` (Viashino Heretic's shape). Sylvan Primordial `SequenceEffect.of(DestroyTargetPermanentEffect(allTargets()),
  SearchLibraryEffect(AmountThisWay(DESTROY), Forest, battlefield tapped))` (its ruling: an indestructible or regenerated target
  doesn't count; the DESTROY step counts only what was destroyed). Target specs: every target step is the harmful permanent target
  the card's filter narrows, as before; Loyal Sentry / Alaborn Zealot declare none, as before.
- **Behaviour changes.** A permanent with all card names (Omnipresent Impostor, MB2 504: "has all card names") now shares a name
  with the other permanents for the destroy and damage cards (Maelstrom Pulse, Wake of Destruction, Echoing Ruin / Calm, Isle of
  Vesuva, Homing Lightning, Izzet Staticaster, Sever the Bloodline); only the Echoing Truth record knew it
  (`MaelstromPulseTest.permanentWithAllCardNamesSharesEveryName`, fails on the old card). Echoing Truth and Mists of Lórien return
  their set through `BounceSupport.applyReturnPermanentsToHand`, one leave batch that looks back (CR 603.10a, run 15's change); the
  old handlers returned one at a time (no test reaches a difference). Mists of Lórien's mana value is the permanent's (a
  transformed permanent's front face). The same-name / sharing destroys leave orphaned Auras to the state-based action right after
  (as every `DestroyTargetPermanentEffect` batch since 5a). Survivors' log lines win.
- **AI.** No AI class named a deleted record. The rewritten target steps are `RemovalEffect` DESTROY / BOUNCE as the records were.
  Dead-Iron Sledge's trigger is now a `RemovalEffect` (DESTROY; the old record declared the same harmful target but wasn't one).
  `TargetPolarityGuardTest`, `SpellEvaluatorTest` pass.
- **Blocked, by cause** (ledger): a card subject for cards a DESTROY step put into graveyards (`DestroyUpToTargetsThenReturnFromGraveyardEffect`,
  Sorin Lord of Innistrad and Come Back Wrong: waits with the EXILE card-subject decision); an each-player search with a `PlayerSubject`
  (`DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect`, Field of Ruin, blocked-player-loop; Demolition Field alone would
  split); a DIED step result (Kalitas); a departed target's attachments or `attachedTo` (Unforge, Murderous Spoils); a departed
  target as damage source (Goblin Tinkerer); another target group's mana value (Orim's Thunder); amass by "that creature's
  controller" (Azog); "if a creature you controlled was destroyed this way" (Demonic Junker: a condition on the DESTROY outcome's
  players); a last-known fallback for the named permanent of `otherPermanentsSharing` (Eye of Singularity's and Spreading
  Plague's entering-permanent records would then be absorbed).

**Tests** (one Gradle run, one `--tests` list per task): every card test that constructs one of the 23 rewritten
cards, their own tests included (66 classes), `--tests-for DestroyTargetPermanentEffect ReturnToHandEffect --max-per-class 15`, `--tests-for
DealDamageToPermanentsTargetControlsEffect SearchLibraryEffect DealDamageToPlayersEffect --max-per-class 6`, application
(`SubjectResolutionServiceTest` +4: a mana value with a filter, a land target's name, the filter narrowing the others, a named
permanent outside the filter; `StepResultsTest`, `EffectCountRatchetTest`, `EffectResolutionServiceTest`, `CardImmutabilityArchTest`,
`DestroyTargetPermanentEffectHandlerTest`, `ReturnToHandEffectHandlerTest`, `DealDamageToPlayersEffectHandlerTest`), AI
(`TargetPolarityGuardTest`, `SpellEvaluatorTest`): 124 top-level classes (186 result files with the `@Nested` suites), 1,267 test
cases, all pass; `MaelstromPulseTest` +1. Application, AI, engine testFixtures and card-test `a b c d e h i l m o s w` test sources
compile.

**For later runs.**

- "Destroy target X and all other [filter] permanents with the same name as / that share a color with it" is
  `DestroyTargetPermanentEffect(allOf(target(), otherPermanentsSharing(target(), quality, filter)))`; "return …" the same in
  `ReturnToHandEffect`. "Destroy that creature and this creature" in a block trigger is `allOf(triggering(), source())`; an
  Equipment's "destroy both creatures" `allOf(triggering(), target())`. "Destroy target X. If its mana value was N or less" is run
  15's `TargetPermanentMatches(PermanentMaxManaValuePredicate(N))` after the destroy.
- **Next run: `DestroyAllPermanentsEffect`'s `thenEffect` rider** (not started here). 30 cards use a rider: 15 read a plain
  `EventValue` count (Avenge, Bane of Progress, Culling Ritual, Death Begets Life, Decree of Pain, Fracturing Gust, Fumigate,
  Multani's Decree, Ori-Plate Stacker, Overwhelming Forces, Paraselene, Phyrexian Rebirth, Rain of Daggers, Righteous Fury, Whiptongue
  Hydra); Chaos Orb (ungated self-destroy) and Anzrag's Rampage (a look-at-top rider: read what it counts) are their own cases; 6 a `DestroyedPermanentCountScope` (Blood Money `ALL_NONTOKEN`,
  Ceaseless Conflict `CONTROLLER_NONTOKEN`, Kaya's Wrath `CONTROLLER`, Hellfire / Mogg Infestation / Reign of Terror `DIED`); 7 a
  per-destroyed-permanent-controller record (`EachDestroyedPermanentControllerLosesLifeEffect` Deadly Tempest,
  `…MaySearchBasicLandToBattlefieldEffect` From the Ashes, `CreateTokenForEachDestroyedPermanentControllerEffect` March of Souls /
  Rampage of the Clans / The Phasing of Zhalfir, `EachDestroyedPermanentControllerGainsLifeEqualToManaValueEffect` Seeds of Innocence
  (every artifact the instruction applied to, indestructible ones included), `DamageEachDestroyedPermanentControllerUnlessPaysEffect`
  Stench of Evil). Its handler records no DESTROY step yet: make it record one (objects destroyed, their controllers), move the 15
  plain riders to `AmountThisWay(DESTROY)` as later steps, then decide the filtered counts (an `AmountThisWay` filter on the departed
  snapshots: token, controller) and a DIED kind or filter, and the per-controller riders (`playersThisWay(DESTROY)` with one entry per
  permanent, not per player). Then `thenEffect`, `destroyedCountScope` and the rider stamping (`eventPlayerIds`, `eventManaValues`)
  can go.
- Also open in Destroy: Voodoo Doll's `DestroySourceThenDealDamageToControllerEffect` (not audited: check that `CountersOnSource`
  and `DealDamageToPlayersEffect` read a destroyed source as it last existed, then it is `SequenceEffect.of(DestroyTargetPermanentEffect(source()),
  DealDamageToPlayersEffect(CountersOnSource(PIN), you()))`), the not-fused `DestroyCombatOpponentEffect` (4 cards, a step-9 absorption
  into `DestroyTargetPermanentEffect(target(), b)` once the combat triggers' recording is checked per path), the two "Unless" records.
- **Remaining fused multi-card records** (`--unaudited`, used by two or more cards, a `Then` / `And` / `IfYouDo` / `Unless` /
  `OrElse` name): 133 (1,264 card uses); by first word Exile 22, Search 10, Sacrifice 10, Reveal 9, Counter 8, Prevent 7, Each 6,
  Draw 6, Choose 5, Cant 5, Put 4, Destroy 3. About a third are not two instructions, so roughly 85 split-able families remain.

### Step 8, run 17 (2026-11-20)

Run 16's "Next run": `DestroyAllPermanentsEffect`'s `thenEffect` rider (30 cards). Verdicts (ledger, step `8`): **split 1**
(`EachDestroyedPermanentControllerLosesLifeEffect`), **absorbed 1** (`DestroyedPermanentCountScope`, an enum), **survivor 5**
(`DestroyAllPermanentsEffect`, `AmountThisWay`, `LoseLifeEffect`, `EachPlayerCreatesTokenEffect`, and `DestroyEachTargetPermanentEffect`
whose count-scope component became a boolean), **blocked-player-loop 2** (From the Ashes', Stench of Evil's riders), **blocked-other 3**
(Seeds of Innocence's rider, `CreateTokenForEachDestroyedPermanentControllerEffect`, Voodoo Doll's
`DestroySourceThenDealDamageToControllerEffect`). Ratchet 4,744 / 3,977 → 4,743 / 3,976. 33 card files rewritten: 27 by a deleted
scratch script (one replacement per card, read back by eye), 6 for the removed `(filter, scope, null)` constructor (Primeval Light, Behold
the Power of Destruction, Ajani Vengeant, Total War, Siren's Call, Maddening Imp). Oracle text of every rider card, Chaos Orb, Anzrag's
Rampage and Voodoo Doll checked with `get_card`; rulings of all of them with `get_rulings` (Fumigate, Deadly Tempest, Kaya's Wrath,
Rampage of the Clans, Hellfire, Blood Money, Death Begets Life, Phyrexian Rebirth, Culling Ritual, Anzrag's Rampage, Bane of Progress,
Whiptongue Hydra, Ori, Fracturing Gust and Decree of Pain have rulings; none contradicts the split). CR 700.4, 608.2c, 608.2h, 111.7 and
603.10a checked with `get_rule` (2026-11-20).

- **The wipe is a step.** `DestroyAllPermanentsEffectHandler` records `StackEntry.recordDestroyDepartures` (new; `ResolutionResults.withDestroyDepartures`
  replaces the departures and the DESTROY outcome and keeps the other outcomes, so it does not clear a SACRIFICE result): NONE as it
  begins (also when a `TARGET_PLAYER` sweep has no player), then every departure of the batch (`DestructionSupport.BatchOutcome.results`,
  new). The DESTROY outcome is what was destroyed, wherever a replacement put it, with the controllers as they were destroyed;
  indestructible and regenerated permanents are departures with `destroyed() == false`. No separate DIED step kind was needed: whether
  each one died is in its departure (`DepartureResult.died(token)`, CR 700.4; `DestructionSupport.diedFromBattlefield` now calls it).
- **Readers** (step 7's `AmountThisWay` pattern, run 7's `Stat`): `Stat.DIED` (`AmountThisWay.died(kind)`: how many of what the step
  removed were put into a graveyard; a token in no zone afterwards died), `filter` (`.matching(filter)`: only those that, as they last
  existed, match; the departed snapshot with its layered card, `ConditionEvaluationService.lastKnownPermanent`, now package-visible) and
  `yours` (`.controlledByYou()`: only those the amount's controller controlled as they left). They read the departures of a SACRIFICE or
  DESTROY step (refused for other kinds), counting only departures among the step's outcome objects, so a destroy step that records no
  departures (`DestroyTargetPermanentEffect`) never reads an older step's. "The amount's controller" is the effect's controller, or the
  player an effect evaluates its amount for: `LoseLifeEffectHandler` now evaluates the amount per player with that player as controller
  for `eachPlayer()` (as `DealDamageToPlayersEffectHandler` already did; every existing `LoseLifeEffect(…, eachPlayer())` amount is
  controller-independent: fixed, X, Shepherd of Rot's any-player Zombie count), and `EachPlayerCreatesTokenEffectHandler` builds each
  player's context with `withControllerId` instead of a fresh `AmountContext` that dropped the stack entry.
- **Split cards.** Spells: the wipe, then the rider as the next spell effect (targeted spells chain it on the target builder, Word of
  Blasting's shape); triggers and modes: `SequenceEffect.of(wipe, rider)` (Bane of Progress, Whiptongue Hydra, Ori, The Phasing of
  Zhalfir, Reign of Terror's modes, Chaos Orb inside its `ConditionalEffect`). Plain count `AmountThisWay(DESTROY)`: Avenge, Bane of
  Progress, Culling Ritual, Death Begets Life, Decree of Pain, Fracturing Gust, Fumigate, Multani's Decree, Ori, Overwhelming Forces,
  Paraselene, Phyrexian Rebirth, Rain of Daggers, Righteous Fury, Whiptongue Hydra. Count scopes: Blood Money `.matching(nontoken)`,
  Ceaseless Conflict `.matching(nontoken).controlledByYou()`, Kaya's Wrath `.controlledByYou()`, Hellfire and Reign of Terror
  `AmountThisWay.died(DESTROY)`, Mogg Infestation `died` with `CreateTokenEffect(…).forController(targetPlayerDeclaredElsewhere())` ("that
  player creates", which also offers that player's token replacements; it declares no second target). Per controller: Deadly Tempest
  `LoseLifeEffect(AmountThisWay(DESTROY).controlledByYou(), eachPlayer())`; March of Souls, Rampage of the Clans, The Phasing of Zhalfir
  `EachPlayerCreatesTokenEffect(CreateTokenEffect(AmountThisWay(DESTROY).controlledByYou(), …))`. Chaos Orb: "Then destroy this artifact"
  is the second step of the sequence (ungated, as written). Anzrag's Rampage: its look-at-top rider reads only
  `ArtifactsPutIntoGraveyardFromBattlefieldThisTurn` (its ruling: any artifact this turn), so it is a plain second spell effect. Target
  specs are unchanged: the wipe is the only step that declares a target (`TARGET_PLAYER`), and no rider declares one.
- **Record.** `DestroyAllPermanentsEffect` lost `destroyedCountScope`; its constructors are `(filter)`, `(filter, cannotBeRegenerated)`,
  `(filter, scope)` (new, for "all X target player controls"), `(filter, thenEffect)` and the canonical five-component one. The handler no
  longer stamps `eventNontokenPlayerIds`. `DestroyEachTargetPermanentEffect`'s `DestroyedPermanentCountScope` component (only ALL / DIED were
  allowed) is the boolean `countsOnlyDeaths` (`countingPermanentsPutIntoAGraveyard()` unchanged). `TextChangeTransformer` copies the five
  components.
- **Behaviour.** No rules change for the split cards: every count is the one the stamped `eventValue` gave (destroyed wherever it went,
  died for the three "died" cards, nontoken / yours from the departures), and the rider ran as the next step before too. Token creation
  order across players in March of Souls / Rampage / Phasing is now seat order (`EachPlayerCreatesTokenEffect`); before it was the order
  the destroyed permanents were found in. The survivors' log lines win. AI: `SpellEvaluator` now also scores the riders as steps (the
  life gain, draws, tokens), estimated with no resolution results (an `AmountThisWay` estimates 0), as for earlier split cards.
- **Blocked, by cause.** From the Ashes (each controller may search once per land they lost, then the searchers shuffle) and Stench of
  Evil (one pay-or-take per destroyed land for its controller): blocked-player-loop. Seeds of Innocence: "the controller of each of those
  artifacts" includes indestructible ones the wipe didn't destroy, with each one's mana value per player; no step result names what an
  instruction applied to. `CreateTokenForEachDestroyedPermanentControllerEffect`: its four other cards (Terastodon, Soul of Emancipation,
  Descent of the Dragons, The Curse of Fenric) ride on `DestroyEachTargetPermanentEffect`, which stamps but records no step. Voodoo Doll:
  the split `SequenceEffect.of(DestroyTargetPermanentEffect(source()), DealDamageToPlayersEffect(CountersOnSource(PIN), you()))` passed the
  card's tests, but after the destroy `CountersOnSource` reads the entry's source snapshot taken when the ability triggered, so pin counters
  put on the Doll in response were missed (new `VoodooDollTest.endStepDamageCountsCountersAddedInResponse` fails on the split, passes on the
  fused record, which reads the live Doll before destroying it); the card is back on its record.

**Tests** (one Gradle run, one `--tests` list per task): every card test that constructs a rewritten card (55 classes), the tests of
every card naming `AmountThisWay`, `--tests-for DestroyAllPermanentsEffect DestroyEachTargetPermanentEffect EachPlayerCreatesTokenEffect
--max-per-class 40`, `--tests-for LoseLifeEffect --max-per-class 20`, every `EachPlayerCreatesTokenEffect` card, Volcanic Eruption /
Builder's Bane / Terastodon (203 card-test classes); application (every test naming `DestroyAllPermanentsEffect`, `DestructionSupport`,
`AmountThisWay`, `DestroyEachTargetPermanentEffect`, `EachPlayerCreatesTokenEffect` or the life-loss handler, plus `TextChangeTransformerTest`,
`EffectCountRatchetTest`, `SubjectResolutionServiceTest`, `EffectResolutionServiceTest`, `CardImmutabilityArchTest`) and AI
(`GameDataDeepCopyTest`, `TargetPolarityGuardTest`, `SpellEvaluatorTest`, `BoardEvaluatorTest`): 311 result files, 4,695 test cases, all
pass. New tests: `ResolutionScratchpadAmountTest` +3 (died vs destroyed, yours per controller, refused kinds),
`DestroyAllPermanentsEffectHandlerTest` (the count-scope rider test became "records the destroy step" and "records an empty step without
a player"), `KayasWrathTest` +2 (Rest in Peace exile still counts, by its ruling; a stolen creature counts for its controller),
`BloodMoneyTest` +1 (an exiled nontoken creature still counts; `@DisplayName`s added), `VoodooDollTest` +1. Application, AI, engine
testFixtures and every touched card and card-test module compile.

**For later runs.**

- "For each [filter] permanent destroyed / that died this way", "the number of creatures you / they controlled that were destroyed this
  way" after a `DestroyAllPermanentsEffect` (or a sacrifice step) is `AmountThisWay(DESTROY | SACRIFICE)` with `died` / `matching` /
  `controlledByYou`; "each player … they controlled" is `LoseLifeEffect(…, eachPlayer())`, "its controller creates" per destroyed permanent
  is `EachPlayerCreatesTokenEffect(CreateTokenEffect(….controlledByYou()))`.
- **Next run: `DestroyEachTargetPermanentEffect` records a DESTROY step** the same way (`recordDestroyDepartures` from
  `destroyBatchWithOutcome(...).results()`), then its 9 rider cards split (Volcanic Eruption, The Elderspell, The Curse of Fenric,
  Terastodon, Soul of Emancipation, Indomitable Creativity, Descent of the Dragons, Convert to Slime, Builder's Bane): that empties
  `CreateTokenForEachDestroyedPermanentControllerEffect` (→ `EachPlayerCreatesTokenEffect`), likely `DealDamageToEachDestroyedPermanentControllerEffect`
  (Builder's Bane; check its per-permanent damage events) and `countsOnlyDeaths` (→ `AmountThisWay.died`), and moves
  `TotalManaValueOfDestroyedPermanents` to a `Stat.MANA_VALUE` over DESTROY (allow it for DESTROY in `AmountThisWay`'s constructor). Then
  the stamping (`eventPlayerIds`, `eventNontokenPlayerIds`, `eventDestroyedPlayerIds`, `eventTargetedPlayerIds`, `eventManaValues`) can go
  where nothing else reads it.
- Voodoo Doll needs the amount source of a step after the source's own destruction to be its departure snapshot (as it last existed,
  CR 608.2h), not the trigger-time `sourcePermanentSnapshot`: a fix in `AmountContext.forStackEntry` callers or in the DESTROY recording
  (refresh the entry's source snapshot when a step removes the source) would also cover other "destroy this, then … equal to its
  counters" texts.
- `DestroyAllPermanentsEffect.thenEffect` stays for From the Ashes, Seeds of Innocence and Stench of Evil (each-player loops / an
  "applied to" set); `TextChangeTransformer` has no `SequenceEffect` case (none of this run's triggers has a text-change word, but a later
  split of a trigger with a colour or land-type word needs one).
- **Remaining fused multi-card records** (`--unaudited`, used by two or more cards, a `Then` / `And` / `IfYouDo` / `Unless` /
  `OrElse` name): 133 (1,264 card uses), unchanged (this run's classes had no such names); by first word Exile 22, Search 10, Sacrifice 10,
  Reveal 9, Counter 8, Prevent 7, Each 6, Draw 6, Choose 5, Cant 5, Put 4, Destroy 3.

### Step 8, run 18 (2026-11-20)

Run 17's "Next run": `DestroyEachTargetPermanentEffect` records a DESTROY step and its 9 rider cards split; then, as the next fused
family by multi-card use whose first half records a step, the draw-then-put-from-hand records (first half `DrawCardEffect`). Verdicts
(ledger, step `8`): **split 3** (`CreateTokenForEachDestroyedPermanentControllerEffect`, `DealDamageToEachDestroyedPermanentControllerEffect`,
`DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect`), **absorbed 1** (`PutControllerCardFromHandOnTopOfLibraryEffect`), **amount absorbed 1**
(`TotalManaValueOfDestroyedPermanents`, `model/amount`), with their handlers; **survivor 4** (`DestroyEachTargetPermanentEffect`, `AmountThisWay`,
`DestroyTargetPermanentsWithinTotalManaValueEffect`, `TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect`), **unique 1**
(`RevealUntilCountMatchingCardsForDestroyedPermanentControllersEffect`, Indomitable Creativity), **blocked-player-loop 1**
(`EachPlayerPutsCardFromHandOnTopOfLibraryEffect`), **blocked-other 3** (Seeds of Innocence's rider, re-audited;
`MayPutCardFromHandOnBottomOfLibraryThenDrawEffect`, `PutHandOnBottomOfLibraryAndDrawEffect`). Ratchet 4,743 / 3,976 → 4,739 / 3,972. 27 card
files rewritten: the 9 rider cards by hand, the 17 draw-then-put / put-a-card cards by a deleted scratch script (read back by eye), Widespread
Panic by hand. Oracle text of every one checked with `get_card`; rulings of Soul of Emancipation, Volcanic Eruption, The Elderspell, The Curse of
Fenric (none), Terastodon, Indomitable Creativity, Descent of the Dragons, Convert to Slime, Builder's Bane (none), Dream Cache (none) and Widespread
Panic with `get_rulings`. CR 700.4, 608.2h, 608.2b, 120.8 and 708.2 checked with `get_rule` (2026-11-20).

- **The targeted destroy is a step.** `DestroyEachTargetPermanentEffectHandler` records `recordDestroyDepartures` as run 17's wipe does: NONE as
  it begins, then the departure of every target it was told to destroy (legal, still on the battlefield, matching its filter), those that stayed
  included. Its stamping (`eventValue`, `eventPlayerIds`, `eventNontokenPlayerIds`, `eventDestroyedPlayerIds`, `eventTargetedPlayerIds`,
  `eventManaValues`) and the `countsOnlyDeaths` component (`countingPermanentsPutIntoAGraveyard()`) are gone. `DestroyTargetPermanentsWithinTotalManaValueEffect`
  (Rampaging Yao Guai) records the same way instead of stamping three fields nothing read. `StackEntry.eventNontokenPlayerIds`,
  `eventDestroyedPlayerIds` and `eventTargetedPlayerIds` had no writer left and are deleted; `eventPlayerIds` / `eventManaValues` stay for
  `DestroyAllPermanentsEffect`'s three remaining riders.
- **`AmountThisWay`** accepts POWER / TOUGHNESS / MANA_VALUE for DESTROY (`destroyedManaValue()`: the departures' last-known mana value, the
  Convert to Slime ruling's "as they last existed on the battlefield"), and gained `includesUnremoved` (`.includingUnremoved()`, DESTROY only, not
  with DIED): every departure of the destroy step, those not destroyed included, "for each of those permanents". It reads nothing unless the
  departures are that step's (the destroyed ones among them are exactly the DESTROY outcome's objects), since `DestroyTargetPermanentEffect`
  records an outcome without departures. A permanent that stayed is judged as it is now for a filter.
- **Split cards.** Volcanic Eruption `MassDamageEffect(AmountThisWay.died(DESTROY), true)`; The Elderspell `Scaled(AmountThisWay(DESTROY), 2)`;
  Descent of the Dragons and The Curse of Fenric (chapter I) `EachPlayerCreatesTokenEffect(token.withAmount(AmountThisWay(DESTROY).controlledByYou()))`;
  Terastodon the same with `died` inside its `MayEffect(SequenceEffect.of(...))`; Soul of Emancipation with `.includingUnremoved().controlledByYou()`
  (its rulings: a shield-countered target still gives an Angel, an illegal one doesn't); Convert to Slime `AmountThisWay.destroyedManaValue()`;
  Builder's Bane `DealDamageToPlayersEffect(AmountThisWay.died(DESTROY).controlledByYou(), eachPlayer())` (amount per player; 0 deals no damage,
  CR 120.8). Indomitable Creativity keeps its record, whose handler now reads `playersThisWay(DESTROY)` and `AmountThisWay(DESTROY).controlledByYou()`
  per player. Target specs are unchanged: the destroy step is the only one that declares a target.
- **Hand to library.** `TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect` gained `HandToLibraryPlacement placement` and `PlayerSubject player`
  (`(count)` keeps target player, TOP; `yours(count, placement)`; one player only, `each(...)` refused), resolved through `SubjectResolutionService`;
  harmful player target only when the subject declares one. "Draw N, then put M cards from your hand on …" is `DrawCardEffect(N)` then
  `yours(M, placement)`: flat spell effects (Brainstorm, Dream Cache, Aquatic Subtlety, Amass the Components), trigger effects (Riverwise Augur,
  Cavalier of Gales, Hidetsugu and Kairi, Sawtooth Loon), ability lists (Jace, the Mind Sculptor, Mordenkainen, Brainstone); the put alone for
  Seek New Knowledge and Errand-Rider of Gondor (draw count 0). The four `PutControllerCardFromHandOnTopOfLibraryEffect` cards (Enter the Infinite,
  Conch Horn, Survivor of the Unseen, Aminatou) take `yours(1, TOP)` (the same min = max = 1 choice). The draw never prompts (step 7's list) and the
  put pauses at its end, so the split is safe; the old handler inserted the put after drawing, the same order.
- **Behaviour changes.** Convert to Slime's X is the departures' last-known mana value (`getPermanentManaValue`: a face-down creature 0, CR 708.2;
  a transformed permanent its front face's) instead of the printed card's. Widespread Panic's "that player" is `triggeringPlayer()` (the shuffling
  player the trigger bakes as a non-targeting id): the record no longer declares a player target on it. Token order across players in Descent, Fenric,
  Terastodon and Soul of Emancipation is seat order (`EachPlayerCreatesTokenEffect`); Indomitable Creativity reveals in APNAP order. The
  survivors' log lines win.
- **AI.** No AI class named a deleted record. Builder's Bane's rider is still a `DamageDealingEffect` (`DealDamageToPlayersEffect`); the split cards'
  target steps are unchanged (`DestroyEachTargetPermanentEffect` HARMFUL_REMOVAL by name entry).
- **Blocked, by cause.** Seeds of Innocence: `AmountThisWay(DESTROY, MANA_VALUE).includingUnremoved().controlledByYou()` names the set now, but the
  record gains life once per artifact (one life-gain event each, which "whenever you gain life" counts) and a departure that stayed without a
  snapshot (indestructible) records no mana value. The two hand-to-library "then draw" records: the put from hand records no step (no `StepKind`
  for hand to library). `EachPlayerPutsCardFromHandOnTopOfLibraryEffect` keeps its own APNAP loop.

**Tests**: one Gradle run, one `--tests` list per task: the tests of every rewritten card and of every other card test that
constructs one or a card naming `AmountThisWay` (Circle of Protection: Blue among them), `--tests-for DestroyEachTargetPermanentEffect
--max-per-class 30`, `--tests-for TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect EachPlayerCreatesTokenEffect DrawCardEffect DealDamageToPlayersEffect
MassDamageEffect DestroyTargetPermanentsWithinTotalManaValueEffect --max-per-class 8` (207 card-test classes); application (`ResolutionScratchpadAmountTest`
+4: a destroy step's mana value, including what wasn't destroyed per controller, nothing read from an older step's departures, refused kinds;
`StepResultsTest`, `EffectResolutionServiceTest`, `SubjectResolutionServiceTest`, `SpellCastingServiceTest`, `DestroyAllPermanentsEffectHandlerTest`,
`EffectCountRatchetTest`, `CardImmutabilityArchTest`) and AI (`TargetPolarityGuardTest`, `GameDataDeepCopyTest`, `SpellEvaluatorTest`,
`PutCardsFromHandOnLibraryAiStrategyTest`): 219 top-level classes (283 result files with the `@Nested` suites), 2,255 test cases, all pass, no card
test needed a change. Application, AI, engine testFixtures and card-test `a b c d e h i j m r s t v w` test sources compile.

**For later runs.**

- "For each [permanent] destroyed / put into a graveyard this way, its controller …" after `DestroyEachTargetPermanentEffect` is the same as after
  `DestroyAllPermanentsEffect` (run 17); "for each of those permanents" is `.includingUnremoved()`; "the total mana value of permanents destroyed
  this way" is `AmountThisWay.destroyedManaValue()`. "Draw N, then put M cards from your hand on top / the bottom of your library" is
  `DrawCardEffect(N)` + `TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect.yours(M, placement)`.
- **Next run.** By multi-card use, the fused records whose first half records a step: `SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect`
  (5 cards; Reprocess, Pitiless Carnage, Malevolent Witchkite, God-Eternal Bontu, Sephiroth) with its sibling
  `SacrificeAnyNumberOfPermanentsThenAddManaPerSacrificedEffect`: give `SacrificePermanentsEffect` an "any number" choice that records the
  SACRIFICE step, then `DrawCardEffect(AmountThisWay(SACRIFICE))`; `SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect` (4) after reading
  its choice; `ManifestTopCardAndPutCountersEffect` (3) once manifest records a step. The "And" sets with no step are step-5m work:
  `BoostTargetCreatureAndAllWithSameNameEffect` (Echoing Decay / Courage, Bile Blight) and `BoostEnchantedAndSharingCreaturesUntilEndOfTurnEffect`
  (the three Crowns) need a target-declaring boost with an `allOf(target(), otherCreaturesSharing(...))` subject, minding the AI polarity of a
  positive pump. Seeds of Innocence needs the per-artifact life-gain decision above.
- **Remaining fused multi-card records** (`--unaudited`, used by two or more cards, a `Then` / `And` / `IfYouDo` / `Unless` / `OrElse` name): 130
  (1,244 card uses); by first word Exile 22, Search 10, Sacrifice 10, Reveal 9, Counter 8, Prevent 7, Each 6, Draw 5, Choose 5, Cant 5, Put 3,
  Destroy 3, Creatures 3, Create 3. About a third are not two instructions, so roughly 85 split-able families remain.

### Step 8, run 19 (2026-11-20)

Run 18's "Next run": an "any number" sacrifice that records its step, the two any-number records, the sacrifice-or-discard and
manifest-then-counters records; then, by multi-card use, the next fused families whose first half records a step (draw, lose life).
Verdicts (ledger, step `8`): **split 8** (`SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect`,
`SacrificeAnyNumberOfPermanentsThenAddManaPerSacrificedEffect`, `SacrificePermanentOrDiscardCardThenDrawAndBoostSelfEffect`,
`DiscardCardAndBoostSelfEffect`, `ManifestTopCardAndPutCountersEffect`, `DrawAndLoseLifePerSubtypeEffect`,
`TargetPlayerLosesLifeAndSearchesLibraryToHandEffect`, `DrawCardThenIfNoCardsDrawnEffect`, with their handlers), **survivor 3**
(`SacrificePermanentsEffect`, `ManifestTopCardEffect`, `ChooseOneAtResolutionEffect`), **blocked-other 4**
(`SacrificePermanentsThenEffect`, `CreateTokensAndAttachEquipmentEffect`, `ExileCreaturesFromGraveyardAndCreateTokensEffect`,
`CounterSpellAndExileAllWithSameNameEffect`), blocked-step-model 0, blocked-player-loop 0, unique 0. Ratchet 4,739 / 3,972 → 4,731 / 3,964.
24 card files rewritten by deleted scratch scripts (read back by eye, imports sorted). Oracle text of every one checked with `get_card`;
rulings with `get_rulings` (Reprocess, Pitiless Carnage, Malevolent Witchkite, God-Eternal Bontu and Sephiroth: zero may be sacrificed,
triggers wait until after the draw; Furyblade Vampire; Maralen: mandatory loss and search; Champion of Dusk: counted as it resolves;
Mr. Foxglove; Wildcall; none for Mana Seism, Reckless Detective, K'un-Lun Warrior, Contract Hero, Bebop & Rocksteady, Graveborn Muse,
Trade Route Envoy). CR 608.2c, 608.2d, 608.2h and 701.40e checked with `get_rule` (2026-11-20).

- **Any number.** `SacrificePermanentsEffect` gained `upToCount` and `anyNumber(filter, player)` (count `Fixed(Integer.MAX_VALUE)`; one
  player only: an `each(...)` subject, simultaneous choices or the recorded-count flags are refused). The handler begins the SACRIFICE step
  as before and always asks, even with one candidate ("any number" includes none), through the existing
  `MultiPermanentChoiceContext.SacrificeAnyNumberAndRecordCount`, whose completion sacrifices through `DestructionSupport.sacrificeTogether`
  (death replacements asked first), so the step result is the chosen permanents. `hasLegalSacrificeChoice` is true with one candidate.
  "Draw a card for each permanent sacrificed this way" / "then draw that many" is `DrawCardEffect(new AmountThisWay(StepKind.SACRIFICE))`
  (Reprocess, Pitiless Carnage: flat spell effects; Malevolent Witchkite, God-Eternal Bontu, Sephiroth One-Winged Angel: one trigger
  `SequenceEffect`); Mana Seism's "then add that much {C}" is `AwardManaEffect(COLORLESS, AmountThisWay(SACRIFICE))` (a spell effect,
  as Dark Ritual's). The two `MultiPermanentChoiceContext` records and their completion methods are deleted.
- **Sacrifice or discard.** `ChooseOneAtResolutionEffectHandler`'s CR 608.2d filter now also drops an option that is only "sacrifice …"
  (`SacrificePermanentsEffect` of `you()`, judged by `hasLegalSacrificeChoice`) or only "discard …" (`DiscardEffect` of `you()`, empty
  hand); a choice made only of such options resolves its lone possible option without a prompt and does nothing when none is possible
  (the counter-removal cards keep their old behaviour). Reckless Detective, K'un-Lun Warrior, Contract Hero:
  `MayEffect(SequenceEffect.of(ChooseOneAtResolutionEffect(sacrifice | discard), ConditionalEffect(AnyOf(sacrificed(),
  HappenedThisWay(DISCARD)), draw / boost)))`; Bebop & Rocksteady's "sacrifice a permanent unless you discard a card" is the bare choice.
  The chosen option is spliced as the next step, so the gate reads it. Furyblade Vampire's `DiscardCardAndBoostSelfEffect` (the
  record's only card) is `MayEffect(SequenceEffect.of(DiscardEffect(1, you()), ConditionalEffect(HappenedThisWay(DISCARD),
  BoostSelfEffect(3, 0))))`; `DiscardFollowUp.boost` / `rummageAndBoost` are deleted (the `boost*` components stay, now never set).
  `SacrificePermanentThenEffect` lost that internal builder (two left: Forage, `MayPayTapAndSacrificePermanent`).
- **Manifest step.** `StepKind.MANIFEST` (the permanents manifested, their controllers), recorded by `ManifestTopCardEffectHandler`;
  `Subjects.thisWay(MANIFEST)` is allowed (SUBJECTS_REFERENCE row). Wildcall, Formless Nurturing, Fierce Invocation: `ManifestTopCardEffect()`
  then `PutCounterOnTargetPermanentEffect(thisWay(MANIFEST), +1/+1, X | 1 | 2)`.
- **Draw / lose life firsts.** Graveborn Muse, Champion of Dusk: `SequenceEffect.of(DrawCardEffect(PermanentCount(subtype, CONTROLLER)),
  LoseLifeEffect(same, you()))` (the loss stays its own step for Lich's Duel Mastery). Maralen of the Mornsong, Mornsong Aria:
  `SequenceEffect.of(LoseLifeEffect(3, triggeringPlayer()), new SearchLibraryEffect().searchedBy(triggeringPlayer()))` (an unrestricted
  search can't fail to find, as the ruling requires; `CardEffectTargetingConsistencyTest`'s allowlist entry is gone). Trade Route Envoy,
  Mr. Foxglove: `DrawCardEffect(amount)` + `ConditionalEffect(NotCondition(HappenedThisWay.drew()), …)` (the DRAW step records even a draw
  of 0 or a prevented one). Target specs are unchanged: none of the rewritten cards' steps declares a target the record didn't.
- **Behaviour changes.** Furyblade Vampire with an empty hand is not asked (CR 608.2d; before: asked, then nothing;
  `FurybladeVampireTest` updated). A lone eligible artifact (Reckless Detective, K'un-Lun Warrior, Contract Hero) or Bebop & Rocksteady
  alone is sacrificed without a permanent prompt, and Bebop with several permanents gets the multi-permanent choice (run 7's
  `SacrificePermanentsEffect` shapes; their tests updated, `ContractHeroTest`'s missing `@DisplayName`s added). The any-number sacrifices
  also skip permanents that can't be chosen to be sacrificed and creature tokens protected from a resolving triggered ability, which the
  old handlers didn't check. Graveborn Muse and Champion of Dusk count every Zombie / Vampire you control, not only creatures ("Zombies you
  control": a noncreature kindred permanent counts; no test card reaches it), and evaluate X once per step (the same value: the draws
  change no permanent). The survivors' log lines win (the "controls no Zombies" and "sacrifices no permanents" variants changed; no test
  asserted them). New tests: `StepResultsTest` +4 (any number counts the chosen ones; none chosen with a lone candidate; Formless
  Nurturing's counter on the manifested permanent; an empty library manifests nothing), `SacrificePermanentsEffectHandlerTest` +4 (the
  `AnyNumber` group), `ReprocessTest` +1. Deleted with their handlers: `DiscardCardAndBoostSelfEffectHandlerTest`,
  `DrawAndLoseLifePerSubtypeEffectHandlerTest` (the cards' tests cover them).
- **AI.** No AI class named a deleted record. The split cards' steps are scored as the evaluators score other steps (a draw of
  `AmountThisWay` estimates 0, as for earlier splits).
- **Blocked, by cause.** `SacrificePermanentsThenEffect` (The Aether Flues, Strefan, Scavenger's Talent: "you may sacrifice N. If you do"):
  `MayEffectHandler`'s can't-sacrifice check reads only a directly wrapped `SacrificePermanentsEffect`, not a sequence's first step, and
  doesn't compare with the count, so a player with fewer than N would be offered a partial sacrifice (CR 608.2d); also its riders run on a
  non-targeting carrier entry today. `CreateTokensAndAttachEquipmentEffect`: no "attach an Equipment you control to each created token"
  primitive. `ExileCreaturesFromGraveyardAndCreateTokensEffect`: Midnight Ritual's single-graveyard X targets are special-cased by class in
  `SpellCastingService`, Hour of Eternity / Foggy Swamp Visions copy exiled cards (card subject). `CounterSpellAndExileAllWithSameNameEffect`:
  no search-and-exile-by-name primitive.

**Tests** (one Gradle run, one `--tests` list per task): every card test constructing a rewritten card (the helper sweep: Claim the Kingdom,
Dalkovan Encampment, Killmonger, Lich's Duel Mastery, Thinking Cap among them), every `ChooseOneAtResolutionEffect`,
`SacrificeAnyNumberOfPermanentsEffect`, `ManifestTopCardEffect` and `CloakTopCardOfControllerLibraryEffect` card test, `--tests-for
SacrificePermanentsEffect --max-per-class 25`, `--tests-for PutCounterOnTargetPermanentEffect DrawCardEffect LoseLifeEffect SearchLibraryEffect
AwardManaEffect DiscardEffect MayEffect --max-per-class 6`; application (`StepResultsTest`, `SacrificePermanentsEffectHandlerTest`,
`EffectResolutionServiceTest`, `SubjectResolutionServiceTest`, `ResolutionScratchpadAmountTest`, `MayEffectHandlerTest`,
`CardEffectTargetingConsistencyTest`, both ratchets, `CardImmutabilityArchTest`, `PlayerInputServiceTest`, the interaction-handler tests and
the discard handler tests naming `DiscardFollowUp`); AI (`TargetPolarityGuardTest`, `SpellEvaluatorTest`, `GameDataDeepCopyTest`): 165 top-level
classes (255 result files with the `@Nested` suites), 1,670 test cases, all pass. Application, AI, engine testFixtures and card-test
`b c f g k m p r s t w` test sources compile.

**For later runs.**

- "Sacrifice any number of [filter]. Draw / add / gain that many" is `SacrificePermanentsEffect.anyNumber(filter, you())` + the verb with
  `AmountThisWay(StepKind.SACRIFICE)` (or `sacrificedPower()` …). "X or Y" between your own sacrifice and discard is a
  `ChooseOneAtResolutionEffect` of the two lone primitives, gated by `AnyOf` of their `HappenedThisWay`s. "Manifest …, then put counters on
  it" is `thisWay(StepKind.MANIFEST)`. "If you don't draw a card this way" is `NotCondition(HappenedThisWay.drew())`.
- **Next run.** (1) `MayEffectHandler`'s can't-sacrifice check: read a `SequenceEffect`'s first `SacrificePermanentsEffect` too and require
  the count (`hasLegalSacrificeChoice`); this also stops asking run 7's "you may sacrifice a creature. If you do" cards when nothing can be
  sacrificed (check their tests), then split `SacrificePermanentsThenEffect` (3 cards; gate `HappenedThisWay.atLeast(SACRIFICE, N)`, the
  riders in the main entry). (2) Absorb `SacrificeAnyNumberOfPermanentsEffect` (12 cards) into `SacrificePermanentsEffect` (an `upTo(n)`
  factory for Ravenous Rotbelly), moving its `EventValue` / `EventValueAtLeast` / `SacrificedPermanentPower` readers to
  `AmountThisWay(SACRIFICE)` / `HappenedThisWay.sacrificed()` / `sacrificedPower()`; Swashbuckler Extraordinaire, Nyssa of Traken and
  Campsite Cuisine size a reflexive ability's target count with `targetUpTo(new EventValue(), …)`, so check that a carried step result
  reaches that count first. (3) Then the next fused family by multi-card use with a recording first half (Necromancer's Covenant alone in
  `ExileCreaturesFromGraveyardAndCreateTokensEffect`; `RevealTopCardPutIntoHandThenEachOpponentLosesLifeEffect` needs a reveal result).
- `DiscardFollowUp`'s `boostPermanentId` / `boostPower` / `boostToughness` components and their `CardChoiceHandlerService` reader have no
  writer left; drop them when the record is next touched.
- **Remaining fused multi-card records** (`--unaudited`, used by two or more cards, a `Then` / `And` / `IfYouDo` / `Unless` / `OrElse` name):
  119 (1,206 card uses); by first word Exile 21, Search 10, Reveal 9, Sacrifice 7, Prevent 7, Counter 7, Each 6, Choose 5, Cant 5, Put 3,
  Draw 3, Destroy 3, Creatures 3, Boost 3. About a third are not two instructions, so roughly 75 split-able families remain.

### Step 8, run 20 (2026-11-20)

Run 19's "Next run": the can't-sacrifice check of a "you may sacrifice N" sequence, `SacrificePermanentsThenEffect`, the any-number record,
`DiscardFollowUp`'s writerless `boost*` components; then the Sacrifice family's remaining fused records (their first halves are
`SacrificePermanentsEffect` / `SacrificeSelfEffect`, which record their step) and the remove-a-counter "if you do" record (one recording
change). Verdicts (ledger, step `8`): **split 4** (`SacrificePermanentsThenEffect`, `SacrificeUnlessSacrificeOwnPermanentEffect`,
`SacrificePermanentsOrElseEffect`, `RemoveCounterAndGainLifeEffect`), **absorbed 1** (`SacrificeAnyNumberOfPermanentsEffect`), with their
handlers (and `SacrificeUnlessSacrificeOwnPermanentHandler` in `mayfx`); **survivor 3** (`SacrificePermanentsEffect`,
`RemoveCounterFromSourceEffect`, `MayEffect`'s handler), **unique 1** (`SacrificeAuraAndGrantIndestructibleEffect`), **blocked-other 5**
(`SacrificeTargetThenRevealUntilTypeToBattlefieldEffect`, `ExileTargetThenRevealUntilTypeToBattlefieldEffect`,
`SacrificeUnlessReturnOwnPermanentTypeToHandEffect`, `SacrificeUnlessDiscardCardTypeEffect`,
`SacrificeEnchantedPermanentAndReattachSourceAuraEffect`), blocked-step-model 0, blocked-player-loop 0. Ratchet 4,731 / 3,964 → 4,726 /
3,958. 31 card files rewritten by deleted scratch scripts (read back by eye; imports sorted, blank line before `java.*` kept). Oracle text
of every one checked with `get_card`; rulings with `get_rulings` (The Aether Flues: the sacrifice is chosen as it resolves; Strefan;
Scavenger's Talent: the returned card may be one sacrificed; Ravenous Rotbelly: opponents choose in APNAP order then sacrifice together;
Nyssa: fewer targets don't change the draw; Lich-Knights' Conquest: zero may be sacrificed; Kylox: total power as they last existed;
Last-Ditch Effort; Giant Opportunity, Insatiable Appetite: the choice is made on resolution, an illegal target means no sacrifice; Gitrog,
Sacred Mesa, Exemplar of Strength, Sun Droplet; none for Swashbuckler Extraordinaire, Woeleecher). CR 101.3, 107.1b, 118.12, 118.12a,
608.2c, 608.2d and 609.3 checked with `get_rule` (2026-11-20).

- **The can't-sacrifice check.** `MayEffectHandler` judges the may's leading step (a bare `SacrificePermanentsEffect` or the first step
  of its `SequenceEffect`) with `SacrificePermanentsEffectHandler.hasLegalSacrificeChoice`: the source-aware filter context, can't-be-
  sacrificed and protected tokens, an opponent's "can't cause you to sacrifice", and the count (an up-to count needs one). With fewer
  candidates than N the choice isn't offered and the `elseEffect` happens (CR 608.2d: an impossible option can't be chosen; the old check
  only looked at a directly wrapped sacrifice and asked for one). Run 7's "you may sacrifice a/another … If you do / When you do"
  cards (about 100) are no longer asked when nothing can be sacrificed; 49 of their tests answered that prompt and now assert it isn't
  shown (`@DisplayName`s that said "accepting …" reworded; the missing ones in `InvasionOfNewCapennaTest`, `TheAetherFluesTest`,
  `StrefanMaurerProgenitorTest` added; `AkutaBornOfAshTest`'s prompt test gained a Swamp).
- **`SacrificePermanentsThenEffect` split.** `MayEffect(SequenceEffect.of(new SacrificePermanentsEffect(N, filter, you()), new
  ConditionalEffect(HappenedThisWay.sacrificed() | atLeast(SACRIFICE, N), X)))`: The Aether Flues (1, reveal until a creature), Strefan
  (2 Blood tokens, the put-a-Vampire record), Scavenger's Talent (3 other nonland permanents, the finality return). The rider now runs in
  the trigger's own entry instead of a non-targeting carrier; `MultiPermanentChoiceContext.SacrificePermanentsThen` deleted.
  `StrefanMaurerProgenitorTest.cannotSacrificeOnlyOneBlood` now asserts no prompt (fails on the old record, which asked).
- **Any number absorbed.** `SacrificePermanentsEffect.upTo(n, filter, player)` (Ravenous Rotbelly) beside `anyNumber`; the 12 cards'
  `EventValue` → `AmountThisWay(StepKind.SACRIFICE)`, `EventValueAtLeast(1)` → `HappenedThisWay.sacrificed()`, `SacrificedPermanentPower`
  → `AmountThisWay.sacrificedPower()` (Kylox). `SacrificeAnyNumberAndRecordCount` lost its power flag and no longer writes the event value
  or the sacrificed power. Two engine changes made the reflexive cards read the step: `SacrificePermanentsEffectHandler` evaluates each
  player's count before `beginSacrificeStep()` (Rotbelly's reflexive "each opponent sacrifices that many" reads the carried SACRIFICE
  outcome, CR 608.2c; before, beginning the step would have cleared it), and `ETBTokenTargetService` reads a group's dynamic target count
  with the ability's trigger record as the amount's stack entry, so a reflexive `targetUpTo(new AmountThisWay(SACRIFICE), …)` (Swashbuckler
  Extraordinaire, Nyssa of Traken, Campsite Cuisine) reads the results `QueueReflexiveAbilityEffectHandler` carried onto it.
- **"Unless" and "or else".** "Sacrifice this unless you sacrifice a [filter]" is "you may sacrifice a [filter]. If you don't, sacrifice
  this" (CR 118.12a): `MayEffect(new SacrificePermanentsEffect(1, filter, you()), prompt, new SacrificeSelfEffect())` (Bog Elemental,
  Contamination, Endless Wurm, Lithophage, Rogue Elephant, Scythe Tiger, Sacred Mesa, The Gitrog Monster, Vulshok War Boar);
  `PermanentChoiceContext.SacrificeOwnPermanentOrSacrificeSelf` and `MayPenaltyChoiceHandlerService.handleSacrificeUnlessSacrificeOwnPermanentChoice`
  deleted. Giant Opportunity / Insatiable Appetite: `MayEffect(SequenceEffect.of(sacrifice N Foods, ConditionalEffect(atLeast, X)), prompt,
  Y)`; `TargetPolarityClassifier`'s branch for the record deleted (the may's wrapped boost gives the same BENEFICIAL; its guard-test line
  removed).
- **Remove a counter.** `RemoveCounterFromSourceEffectHandler` records a REMOVE_COUNTERS step (`counterCounts` / `counterChange`, as the
  target form does; it never prompts). `HappenedThisWay.removedCounters()`. Exemplar of Strength, Living Artifact, Sun Droplet:
  `RemoveCounterFromSourceEffect(type, 1)` + `ConditionalEffect(removedCounters(), GainLifeEffect(1))` (in their `MayEffect`); Woeleecher:
  `RemoveCounterFromTargetPermanentEffect(-1/-1, creature)` + the gate; Chainbreaker: the removal alone. The removal now goes through
  `removeCounterFromPermanent` and `notifyLastCounterRemoved` (the old handler set the count directly).
- **`DiscardFollowUp`** lost `boostPermanentId` / `boostPower` / `boostToughness` (every constructor and factory) and
  `CardChoiceHandlerService`'s reader.
- **Behaviour changes.** The ~100 may-sacrifice cards, the "unless" cards and the Food cards with nothing (or fewer than N) to sacrifice
  are not asked (CR 608.2d). A lone eligible permanent is sacrificed without a permanent prompt; several give the one-of
  `MultiPermanentChoice` (run 7's shapes; the nine "unless" cards always asked a `PermanentChoice` before). The any-number and "unless"
  sacrifices skip permanents that can't be chosen and protected tokens. Kylox's X sums negative powers before flooring the total at 0:
  `AmountThisWay` totals (and the exile-step stats) now floor once, not per permanent (CR 107.1b; `KyloxVisionaryInventorTest.includesNegativePowerInTotal`
  passes again; `ResolutionScratchpadAmountTest`'s per-permanent-floor case now expects the floored total, +1 all-negative case; a single permanent gives the same value). Woeleecher and Chainbreaker's target polarity: none → NEUTRAL (the removal's
  name entry). Lithophage's "accepting with no Mountain left" test (the Mountain was deleted mid-prompt, unreachable in a game) became the
  reachable "with no Mountain, the death replacement is asked before it is sacrificed". The survivors' log lines win.
- **Blocked, by cause.** No reveal-until-type primitive for "its controller" (the two reveal-until records; Reweave also needs the
  departure's card types), no chosen "return a [type] you control" that records RETURN, no card filter on `DiscardEffect`, no "attach this
  Aura to a permanent that player controls" primitive. Unique: the Aura destruction replacement.

**Tests** (one Gradle run, one `--tests` list per task): every rewritten card's test, every card test constructing a rewritten card (the helper sweep, 43 classes), every "you may sacrifice …" sequence card (116 names), every card naming `AmountThisWay`, a dynamic `targetUpTo` or `RemoveCounterFromSourceEffect`, `--tests-for SacrificePermanentsEffect --max-per-class 25`, `--tests-for MayEffect SequenceEffect --max-per-class 8` (340 card-test result files, 2,883 test cases); application (every test naming `SacrificePermanentsEffect`, `MayEffectHandler`, `DiscardFollowUp`, the permanent-choice contexts and services, `MayPenaltyChoiceHandlerService`, `ETBTokenTargetService`, `AmountThisWay`, `HappenedThisWay`, `RemoveCounterFromSourceEffect` or `CardChoiceHandlerService`, plus `StepResultsTest`, `EffectResolutionServiceTest`, `SubjectResolutionServiceTest`, both ratchets, `CardImmutabilityArchTest`, `CardEffectTargetingConsistencyTest`, `InputHandlerEpilogueRatchetTest`, `AcceptedOptionResolutionTest`, `MayAbilityHandlerServiceTest`: 226 result files, 3,611 test cases) and AI (`TargetPolarityGuardTest`, `SpellEvaluatorTest`, `GameDataDeepCopyTest`: 150): all pass after the `ResolutionScratchpadAmountTest` update (rerun: 13 pass). About 90 card tests were updated across the run (the unoffered prompts, the new prompt shapes). Application, AI, engine testFixtures and card-test `a b c d e g i k l m p r s t u v w y` test sources compile.

**For later runs.**

- "You may sacrifice N [filter]. If you do / Otherwise" is `MayEffect(SequenceEffect.of(SacrificePermanentsEffect(N, …), ConditionalEffect(
  HappenedThisWay.atLeast(SACRIFICE, N) | sacrificed(), X)), prompt, Y)`; "sacrifice A unless you sacrifice B" is `MayEffect(sacrifice B,
  prompt, sacrifice A)`. "Remove a counter from this / target creature. If you do" is the removal + `HappenedThisWay.removedCounters()`. A
  reflexive "up to that many targets" is `targetUpTo(new AmountThisWay(kind), …)`.
- **Next run.** The rest of the remove-counter family now that the source removal records its step: `RemoveCounterFromSourceThenEffect`
  (9 cards; `CounterType.ANY` needs the source removal to pick a present kind, "remove two" needs `atLeast(REMOVE_COUNTERS, 2)`, and
  `onlyIfLastCounterRemoved` a "no counters of that kind remain" condition), `RemoveCounterAndSacrificeSelfOnLastEffect` (18; check vanishing's
  separate "when the last is removed" trigger, CR 702.63a, before splitting), then `PutCountersOnSelfThenReflexiveEffect` (4) /
  `PutCountersOnTargetPermanentThenReflexiveEffect` (2) / `PutCounterOnSelfThenTransformIfThresholdEffect` (6) (the put records PUT_COUNTERS;
  the reflexive condition is checked twice). `EachTargetPlayerLosesLifeAndSacrificesCreatureEffect` (2) is ledgered "step 8" from 4g too.
  A card filter on `DiscardEffect` would unblock `SacrificeUnlessDiscardCardTypeEffect` (18) and run 2's 26 "discard a … If you do" cards.
- **Remaining fused multi-card records** (no ledger row, used by two or more cards, a `Then` / `And` / `IfYouDo` / `Unless` / `OrElse`
  name): 112 (1,169 card uses); by first word Exile 20, Search 10, Reveal 9, Counter 7, Prevent 7, Each 6, Cant 5, Choose 5, Boost 3,
  Creatures 3, Destroy 3, Draw 3, Put 3. The step-4/5 "step 8" deferrals with a ledger row (the discard-then-draw, mill-then and counter
  records above) are not in that count.

### Step 8, run 21 (2026-11-20)

Run 20's "Next run", item 1: the rest of the counter family now that the source removal records its step. Item 2 (a card
filter on `DiscardEffect`) was not reached. Verdicts (ledger, step `8`): **split 3** (`RemoveCounterFromSourceThenEffect`,
`PutCountersOnSelfThenReflexiveEffect`, `PutCountersOnTargetPermanentThenReflexiveEffect`), **absorbed 1**
(`RemoveCounterAndSacrificeSelfOnLastEffect`), with their handlers; **survivor 4** (`RemoveCounterFromSourceEffect`,
`GoadTargetCreatureUntilNextTurnEffect`, `ChooseOneAtResolutionEffect`, `SequenceEffect`), **blocked-other 1**
(`PutCounterOnSelfThenTransformIfThresholdEffect`). Ratchet 4,726 / 3,958 → 4,722 / 3,954. 33 card files rewritten: the 18
vanishing cards by a deleted scratch script, the 15 others by deleted per-card scripts (read back by eye, imports sorted).
Oracle text of every split card checked with `get_card`; rulings with `get_rulings` (Verazol: "you can't remove just one
counter from it if it has only one"; Spectacular Showdown: nothing on the goad). CR 702.63a, 603.12, 608.2c, 608.2d and 609.3
checked with `get_rule` (2026-11-20); 118.12a for the next run.

- **Any kind of counter.** `RemoveCounterFromSourceEffect` accepts `CounterType.ANY` (source subject, fixed amount only): it
  records REMOVE_COUNTERS NONE, then removes the one kind the source has enough of, or begins a mode choice ("Remove N
  [kind] counters", the old labels) whose chosen removal is spliced in as the next step and records the step.
  `PermanentCounterSupport.removableCounterKinds` / `hasCountersToRemove` are shared with `PlayerInputService`'s greyed modes
  and `ChooseOneAtResolutionEffectHandler`'s CR 608.2d option check, which now reads a mode that starts with a source
  `RemoveCounterFromSourceEffect` (Immard). The removal also goes through `removeCounterFromPermanent` and
  `notifyLastCounterRemoved` (the old record set the count directly).
- **"Remove a counter from this. If you do / When you do"** is `SequenceEffect.of(RemoveCounterFromSourceEffect(type, 1),
  ConditionalEffect(HappenedThisWay.removedCounters(), X | QueueReflexiveAbilityEffect(X)))`: Slumbering Walker and
  Leatherhead (`ANY`), Kappa Tech-Wrecker, Biting-Palm Ninja, Atraxa's Skitterfang, Immard (the mode's two steps) reflexive;
  Purestrain Genestealer, Noosegraf Mob "if you do". "Remove two … If you do" (Verazol) is
  `ConditionalEffect(SourceCounterThreshold(2, +1/+1), SequenceEffect.of(remove 2, ConditionalEffect(atLeast(REMOVE_COUNTERS, 2),
  copy)))`, the Mercurial Spelldancer / Forgehammer Centurion shape, so one counter is never removed (its ruling). The
  old record's `onlyIfLastCounterRemoved` had no card. `ReflexiveRouteRecordTest`'s REMOVE_COUNTER continuation is the
  sequence and still records the trigger event field by field.
- **Vanishing.** The 18 vanishing upkeep abilities and `CloneService`'s granted one are `RemoveCounterFromSourceEffect(TIME, 1)`.
  The sacrifice is vanishing's own third ability (CR 702.63a), which `PermanentCounterSupport.checkLastTimeCounterRemoved`
  already put on the stack for a permanent with vanishing; the deleted handler's own sacrifice for one without it was
  rules-wrong (`WaningWurmTest.notSacrificedAfterLosingVanishing`, fails on the old record). The last removal now also emits
  LAST_REMOVED ("when you remove the last time counter"), which the old handler skipped.
- **Put counters, then reflexive.** `PutCountersOnSelfEffect` / `PutCounterOnTargetPermanentEffect` record PUT_COUNTERS, so
  "put N counters on X. When you do, Y" is `SequenceEffect.of(put, ConditionalEffect(new HappenedThisWay(PUT_COUNTERS),
  QueueReflexiveAbilityEffect[.of(Y, true)]))`: Lockjaw, Auron, Craig Boone (up to one target), Dimension X Pizzasaur (up to
  one); Earthbender Ascension's "when you do, if it has four or more quest counters" gates on `AllOf(HappenedThisWay(PUT_COUNTERS),
  SourceCounterThreshold(4, QUEST))` and wraps the reflexive effect in the same threshold (judged as it triggers and as it
  resolves, as the record did). **`SequenceEffect.targetsChosenByReflexiveAbility`** is now true only when no other step
  declares a target of its own: Pizzasaur's sequence chose no counter target before this (its tests caught it). No card in
  the 159-card `QueueReflexiveAbilityEffect` sweep changed.
- **Behaviour changes (Oracle text).** Verazol "If you do, copy that spell": the copy is made as the trigger resolves, not by
  a reflexive ability (`VerazolTheSplitCurrentTest.createsCopyDuringCounterRemovalAbilityResolution` rewritten; it fails on
  the old card). Spectacular Showdown "then goad each creature that had a double strike counter put on it this way": the
  targeted branch is `SequenceEffect.of(PutCounterOnTargetPermanentEffect(DOUBLE_STRIKE, 1),
  GoadTargetCreatureUntilNextTurnEffect.ofCreaturesGivenCountersThisWay())` (a `thisWay(PUT_COUNTERS)` goad; no ability on
  the stack: `SpectacularShowdownTest.targetIsGoadedAsTheSpellResolves`, fails on the old card); its overload branch keeps
  `PutCounterOnEachMatchingPermanentThenGoadEffect`. The reflexive abilities' description is now "[card]'s reflexive
  ability" (no test asserted the old "'s ability"). Missing `@DisplayName`s added in the Verazol, Waning Wurm, Leatherhead
  and Spectacular Showdown tests.
- **AI.** `TargetPolarityClassifier`'s branch for the put-then-reflexive record is gone: a sequence takes its strongest step
  and a `QueueReflexiveAbilityEffect` classifies as nothing, so Pizzasaur stays BENEFICIAL (`TargetPolarityGuardTest`'s case
  is the sequence). Spectacular Showdown's targeted branch is now HARMFUL (the goad's name entry) instead of NEUTRAL (a double
  strike counter): the AI puts the counter on an opposing creature it goads. `EffectResolution.targetPredicateOf` lost the
  record's branch.
- **Blocked.** `PutCounterOnSelfThenTransformIfThresholdEffect` (6 cards): splitting into put + `SourceCounterThreshold`-gated
  `RemoveAllCountersEffect` / `TransformSelfEffect` needs the source's last-known counters when it left (Treasure Map still
  makes its Treasures), the handler's original-card transform path (`removeCountersAndTransform`) matched against
  `TransformSelfEffect`'s `AnimationSupport` path, and Primal Amulet's optional "you may remove and transform".

**Tests** (one Gradle run, one `--tests` list per task): every rewritten card's test and every card test constructing a
rewritten card (60 classes with Flesh Duplicate, Timebender and the may-remove cards), every card naming
`QueueReflexiveAbilityEffect` or `RemoveCounterFromSourceEffect`, `--tests-for PutCountersOnSelfEffect
PutCounterOnTargetPermanentEffect GoadTargetCreatureUntilNextTurnEffect ChooseOneAtResolutionEffect CopyTriggeringSpellEffect
--max-per-class 8`; application (`RemoveCounterFromSourceEffectTest`, `ReflexiveRouteRecordTest`, `LastCounterRemovedTriggerTest`,
`StepResultsTest`, `EffectResolutionServiceTest`, `SubjectResolutionServiceTest`, `CardImmutabilityArchTest`,
`CardEffectTargetingConsistencyTest`, `PlayerInputServiceTest`, `TriggerRouteRecordTest`, `CollectorAbilityStackObjectTest`,
`ClassKeyedRecordCollectorTest`, `MayEffectHandlerTest`) and AI (`TargetPolarityGuardTest`, `SpellEvaluatorTest`,
`GameDataDeepCopyTest`): 322 top-level classes (395 result files with the `@Nested` suites), 5,366 test cases, all pass
(the first run's six Pizzasaur failures led to the `SequenceEffect` change); `EffectCountRatchetTest` passes. Application,
AI, engine testFixtures and card-test `a b c d e f i k l m n o p r s t v w` test sources compile.

**For later runs.**

- "Remove a counter (of any kind) from this. If you do / When you do" and "put N counters on this / target creature. When
  you do" are the removal or placement step + `ConditionalEffect(HappenedThisWay(REMOVE_COUNTERS | PUT_COUNTERS), …)`;
  "remove N" needs `SourceCounterThreshold(N)` around the sequence (Verazol's ruling). "Then [verb] each creature that had a
  counter put on it this way" is a `thisWay(PUT_COUNTERS)` subject.
- **Next run.** (1) A card filter on `DiscardEffect` (the single-player chosen path already takes valid indices, as
  `onlyCardsDrawnThisResolution` does; the each-player and random paths need it too or must refuse it), a filter-aware
  "can't discard N matching" check in `MayEffectHandler` beside its sacrifice check (CR 608.2d), and the AI's
  `SpellEvaluator` read of a filtered own discard; then `SacrificeUnlessDiscardCardTypeEffect` (18 cards) as
  `MayEffect(DiscardEffect(N, you(), filter[, random]), prompt, SacrificeSelfEffect()[ + DrawCardEffect(1)])` (CR 118.12a's
  "unless" is "you may … If you don't"), its `thenEffect` cards needing the discarded card's mana value from the DISCARD
  result, and run 2's 26 "discard a … If you do" `DiscardCardThenEffect` cards. (2) `PutCounterOnEachMatchingPermanentThenGoadEffect`
  (Spectacular Showdown's overload, and any other card) is now `PutCounterOnTargetPermanentEffect` over a set subject +
  `ofCreaturesGivenCountersThisWay()` if the placement records each permanent; check it. (3) The threshold transform above.
- **Remaining fused multi-card records** (no ledger row, used by two or more cards, a `Then` / `And` / `IfYouDo` / `Unless` /
  `OrElse` name): 112 (1,169 card uses), unchanged (this run's classes had ledger rows from 5e / 5f); by first word Exile 20,
  Search 10, Reveal 9, Counter 7, Prevent 7, Each 6, Cant 5, Choose 5, Boost 3, Creatures 3, Destroy 3, Draw 3, Put 3.

### Step 8, run 22 (2026-11-20)

Run 21's "Next run": (1) a card filter on `DiscardEffect`, a filter-aware can't-discard check in `MayEffectHandler`, then
`SacrificeUnlessDiscardCardTypeEffect` and run 2's "discard a … If you do" `DiscardCardThenEffect` cards; (2) Spectacular Showdown's overload
record. (3) The threshold transform was not reached. Verdicts (ledger, step `8`): **split 1** (`PutCounterOnEachMatchingPermanentThenGoadEffect`),
**absorbed 2** (`SacrificeUnlessDiscardCardTypeEffect`, `ExileUnlessDiscardCardTypeEffect`, with their `normalfx` and `mayfx` handlers and
`MayPenaltyChoiceHandlerService.handleSacrificeUnlessDiscardChoice` / `handleExileUnlessDiscardChoice`), **survivor 4** (`DiscardEffect`,
`HappenedThisWay`, `PutCounterOnEachMatchingPermanentEffect`, `MayEffect`'s handler), **blocked-other 1** (`DiscardCardThenEffect`: 41 of its 70
cards split, 30 users left). Ratchet 4,722 / 3,954 → 4,719 / 3,949. 61 card files rewritten (the 19 unless-discard cards by
a deleted scratch script, 37 discard-then cards by another, 5 by hand: Spectacular Showdown, Old One Eye, Lord Windgrace, Lukka, Chandra Ablaze; read back by eye,
imports sorted). Oracle text of every rewritten card checked with `get_card` (Alberix's MB2 playtest printing isn't on Scryfall; its own
option text was used); rulings with `get_rulings` (Hidden Horror: you choose as it resolves, must sacrifice if you can't discard, may still
discard once it left; Argentum Masticore: the reflexive target is chosen after the discard, discarding is allowed with no legal target;
Ill-Timed Explosion; none for Avatar of Discord, Great Desert Hellion, Pillaging Horde; Lim-Dûl's Paladin's is about its attack trigger).
CR 101.3, 118.3, 118.12, 118.12a, 603.12, 608.2c, 608.2d, 609.3, 701.9a and 701.9b checked with `get_rule` (2026-11-20).

- **The discard filter.** `DiscardEffect` gained a `filter` component and `DiscardEffect.matching(n, player, CardPredicate)`: a chosen
  discard offers only matching cards (the single-player path through `resolveDiscardCards(validIndices)`, the each-player path's choosable
  indices); a player with fewer matching cards discards those they have (CR 609.3). It is refused with `random`, a declared player target,
  the stop-after or drawn-this-resolution forms. `HappenedThisWay.toACard` accepts `StepKind.DISCARD` (the cards the step discarded, judged
  wherever they went) and `HappenedThisWay.discarded()` is "if you do".
- **The can't-discard check.** `MayEffectHandler` counts the discarder's matching cards against the leading `DiscardEffect`'s count
  (evaluated as the choice is offered, at least one): fewer, and the choice isn't offered and the `elseEffect` happens (CR 608.2d; "you may
  discard N. If you do" is a cost, CR 118.12, paid only in full, CR 118.3). Before, only an empty hand withheld it. A discard by a player
  named without targeting ("that attacking player may discard", Curse of Chaos) is judged when that player is the deciding one. A leading
  `DiscardCardThenEffect` of `you()` with an `elseEffect` is judged too (Argentum Masticore).
- **Unless discard** (CR 118.12a): `MayEffect(new DiscardEffect(N, you()) | DiscardEffect.matching(1, you(), filter) | new DiscardEffect(1, you(),
  true), prompt, new SacrificeSelfEffect())`: Thundering Wurm, Fallow Wurm, Cursed Monstrosity (land), Mercenary Knight, Hidden Horror (creature),
  Drekavac (noncreature), Pillaging Horde, Minotaur Explorer, Balduvian Horde (random), Avatar of Discord (two), Solitary Confinement, the three
  Masticores, Coral Net's granted ability; Lim-Dûl's Paladin's penalty is `SequenceEffect.of(SacrificeSelfEffect, DrawCardEffect(1))` (the draw
  happens whether or not the sacrifice does, as before); Body Snatcher's is `ExileTargetPermanentEffect(source())`. Great Desert Hellion: the may
  wraps `SequenceEffect.of(discard, ConditionalEffect(HappenedThisWay.discarded(), IntensifySourceCardEffect(1)))`. Argentum Masticore's "when you
  discard a card this way" with the discarded card's mana value as the reflexive target's X is `DiscardCardThenEffect.withDiscardedManaValueAsX(destroy)`
  inside the may: a new `discardedManaValueIsX` component that sets the existing `DiscardFollowUp.thenEffectUsesDiscardedManaValue`
  (`withDiscardedManaValueAsX()`; `DiscardFollowUp.thenEffectWithDiscardedManaValue` deleted). That moves the one-card special case onto a record
  that stays for the reflexive cards; it let the record, two handlers and the penalty method go.
- **Discard, if you do.** "[You may] discard a [kind] card. If you do / If a [kind] card is discarded this way, X" is
  `SequenceEffect.of(new DiscardEffect(1, P) | DiscardEffect.matching(1, P, filter), new ConditionalEffect(HappenedThisWay.discarded() |
  HappenedThisWay.toACard(StepKind.DISCARD, kind), X))` (in its `MayEffect` / choose-one option); "discard a card, then X" is a plain sequence
  (Rix Maadi Reveler). Split: the ten recruit cards (Bard's Company, Celebrate the Mountain-king, Esgaroth Garrison, Great Gilded Boat, Lake-town
  Lookout, Long Lake Nuisance, Patient Instructor, The Mountain-king's Return, The Queen of Dale, Sound the Trumpets: "if you discarded a nonland
  card"), Vohar, Chandra Ablaze ("if a red card"), Lord Windgrace (draw, then `toACard(land)` draws one more), Lukka ("draw two instead":
  `AllOf(discarded, Not(toACard(creature)))` draws one, `toACard(creature)` draws two), Old One Eye (`DiscardEffect(2)` + `atLeast(DISCARD, 2)`),
  Alberix, Artillery Enthusiast, Chandra Spark Hunter, Crucias, Crypt Lurker, Curse of Chaos (`triggeringPlayer()`), Disciple of Deceit, Erebos's
  Titan, Formidable Speaker, Frenzied Geistblaster, Gert and Old Lace, Ghitu Embercoiler, Gigapede, Green Goblin, Gristle Glutton, Journey to the
  Oracle, Melded Moxite, Olivia, Pack Guardian, Quintorius, Rank Officer, Rydia, Territorial Kavu, Truga Cliffcharger, Vision of Love. "The discarded
  card's mana value" stays `LastDiscardedCardManaValue` (the discard completion stamps the entry's discarded-card snapshot).
- **Spectacular Showdown's overload** is `SequenceEffect.of(new PutCounterOnEachMatchingPermanentEffect(DOUBLE_STRIKE, 1, creature, ALL_PLAYERS),
  GoadTargetCreatureUntilNextTurnEffect.ofCreaturesGivenCountersThisWay())`: `PutCounterOnEachMatchingPermanentEffectHandler` records a
  PUT_COUNTERS step (`counterCounts` / `counterChange`; it never prompts; `StepKind` Javadoc lists it).
- **Behaviour changes.** Great Desert Hellion (YBRO 19) "If you discard a card this way, this creature intensifies by 1": the old record queued
  a reflexive ability; it now intensifies as the upkeep ability resolves (`GreatDesertHellionTest.intensifiesDuringTheUpkeepAbilitysResolution`,
  fails on the old card). A "you may discard [a kind of] card" with no such card is not offered (CR 608.2d): Disciple of Deceit, Gigapede,
  Curse of Chaos, Melded Moxite, Quintorius, Pack Guardian, Truga Cliffcharger and the other split "you may" cards (before: asked, then nothing);
  Ill-Timed Explosion with fewer than two cards isn't asked and its reflexive damage no longer happens off a one-card discard
  (`IllTimedExplosionTest.fewerThanTwoCardsIsNotOffered`, fails on the old check; the Royal Scions reading of "when you [do N]"). Masticore's
  "hand emptied while the prompt was open" test (unreachable in a game) became "with an empty hand the death replacement is asked before it is
  sacrificed". The survivors' log lines win (Hidden Horror's "does not have enough creature cards to discard" line is gone; its test asserted
  it). Missing `@DisplayName`s added in the Quintorius, Invasion of Ergamon and Great Desert Hellion tests.
- **AI.** No AI class named a deleted record. `SpellEvaluator` scores only a targeted `DiscardEffect` (run 2's note that it scores a bare
  `DiscardEffect(you())` as card loss no longer holds), so the split cards' own discards score 0 as the wrapper did.
- **Blocked, by cause.** `DiscardCardThenEffect` keeps 22 reflexive "when you do" cards (Hypothesizzle, Glacial Dragonhunt, Dokuchi Silencer,
  …; correct as they are), Argentum Masticore, and: a card subject for the discarded card (Malcolm, Silvan Reveler, Anguished Recollection,
  Leonardo da Vinci), a card-filtered count of a discard step ("for each nonland card discarded this way", Seasoned Pyromancer, still two
  single discards), the `MayPayManaEffect` wrapper with a target (Wolf of Devil's Breach) and a graveyard target (Toph). The threshold transform
  (item 3) was not reached.

**Tests** (one Gradle run, one `--tests` list per task, then the three modules a compile slip and the new Hellion test stopped): every
rewritten card's test, every card test constructing a rewritten card (the helper sweep: 82 classes), Ill-Timed Explosion, `--tests-for
DiscardEffect MayEffect PutCounterOnEachMatchingPermanentEffect DiscardCardThenEffect GoadTargetCreatureUntilNextTurnEffect --max-per-class 8`;
application (`StepResultsTest` +3, `DiscardEffectHandlerTest`, `DiscardCardThenEffectHandlerTest`, `MayEffectHandlerTest`,
`LoseLifeUnlessDiscardEffectHandlerTest`, `DiscardAndDrawCardEffectHandlerTest`, `FlipCoinWinEffectHandlerTest`, `ReflexiveRouteRecordTest`,
`StepTriggerServiceTest`, `HandCardChoiceInteractionHandlersTest`, `ResolutionScratchpadAmountTest`, `EffectResolutionServiceTest`,
`SubjectResolutionServiceTest`, `EffectCountRatchetTest`, `CardImmutabilityArchTest`) and AI (`TargetPolarityGuardTest`, `SpellEvaluatorTest`,
`GameDataDeepCopyTest`): 207 result files, 1,741 test cases, all pass after 11 tests were updated for the unoffered prompts (above).
`SacrificeUnlessDiscardCardTypeEffectHandlerTest` deleted with its handler (the cards' tests cover it). Application, AI, engine testFixtures
and card-test `a b c d e f g h i j l m o p q r s t v` test sources compile.

**For later runs.**

- "Sacrifice / exile this unless you discard [N] [kind]" is `MayEffect(discard, prompt, penalty)`; "[you may] discard a [kind] card. If you
  do / if a [kind] card is discarded this way" is the discard + `ConditionalEffect(HappenedThisWay.discarded() | toACard(DISCARD, kind), X)`.
  Keep `DiscardCardThenEffect` for "when you do" (reflexive, CR 603.12) and for a rider that names the discarded card.
- **Next run.** (1) The threshold transform (`PutCounterOnSelfThenTransformIfThresholdEffect`, 6 cards; run 21's blockers). (2)
  `LoseLifeUnlessDiscardEffect` (Painful Quandary) and `EachOpponentMayDiscardOrLoseLifeEffect`: "that player loses N life unless they discard a
  card" is `MayEffect(DiscardEffect(1, triggeringPlayer()), prompt, LoseLifeEffect(N, triggeringPlayer()), MayChoicePlayer…)` if a may chooser
  names the triggering player and the each-opponent form has a player loop (else blocked-player-loop). (3) Seasoned Pyromancer needs
  `AmountThisWay(DISCARD)` over a card filter. (4) Then the next fused multi-card families: Search 10, Reveal 9, Counter 7, Prevent 7.
- The `DiscardCardThenEffect` reflexive cards inside a `MayEffect` are still asked with no matching card (then nothing happens); the check
  covers that record only with an `elseEffect`. Widen it when the record is next touched.
- **Remaining fused multi-card records** (no ledger row, used by two or more cards, a `Then` / `And` / `IfYouDo` / `Unless` / `OrElse`
  name): 112 (1,169 card uses), unchanged (this run's classes had ledger rows); by first word Exile 20, Search 10, Reveal 9, Counter 7,
  Prevent 7, Each 6, Cant 5, Choose 5, Boost 3, Creatures 3, Destroy 3, Draw 3, Put 3.

### Step 8, run 23 (2026-11-20)

The human's decision of 2026-11-20: a card subject, the third subject type, and the records blocked on it ("needs a card
subject" in runs 13, 14, 16, 19, 22). Verdicts (ledger, step `8`): **split 3** (`ExileTargetCardFromGraveyardAndCreateTokenCopyEffect`,
`DestroyUpToTargetsThenReturnFromGraveyardEffect`, `ExileTopCardLoseLifeEqualToManaValueAndMayCastInstantOrSorceryEffect`), **absorbed 1**
(`ReturnDiscardedCardFromGraveyardToBattlefieldEffect`), with their handlers; **survivor 7** (`CreateTokenCopyOfCardEffect`,
`ReturnCardFromGraveyardEffect`, `MayCastDiscardedCardWithoutPayingManaCostEffect`, `ExileGraveyardCardsEffect`, `PermanentSubject`,
`DestroyTargetPermanentEffect`, `ExileTargetPermanentEffect`), **blocked-other 3** (`ExileTargetCardFromGraveyardAndMayCastCopyEffect`,
`ExileDiscardedCardAndCreateTokenCopyEffect`, `SeekCardsNotSharingCardTypeWithTriggeringCardEffect`). Ratchet 4,719 / 3,949 → 4,715 / 3,945.
20 card files rewritten: the 15 token-copy cards by a deleted scratch script (read back by eye, imports sorted), Sorin, Come Back Wrong,
Hidetsugu and Kairi, Malcolm and Silvan Reveler by hand. Oracle text of every one checked with `get_card` (Restore Relic as Lorehold
Archivist's prepared face, SOC 49); rulings with `get_rulings` for all of them (the deciding ones below). CR 400.7, 400.7j, 508.4, 608.2b,
608.2h, 616.1, 707.9b, 707.10 and 707.12 checked with `get_rule` (2026-11-20).

- **Card subjects** (Design, "Card subjects"; SUBJECTS_REFERENCE section). `CardSubject` is sealed with `CardsThisWay(StepKind)`
  (`Subjects.cardsThisWay`: EXILE, DESTROY, SACRIFICE, DISCARD; other kinds refused) and `TargetCard(filter, scope)`
  (`Subjects.targetCard`, added because Feldon of the Third Path copies a target graveyard card without exiling it; the effect
  reading it declares a benign graveyard target). `SubjectResolutionService.cards` / `locatedCards` (card, zone, owner) name each
  card in the zone the step put it in and nothing once it has left that zone (CR 400.7, 400.7j): an exiled card that kept its id in
  exile, a discarded card in its graveyard or in exile under a replacement, a removed permanent's card through the new
  `StepOutcome.movedCards` (`MovedCard(objectId, cardId, zone, ownerId)`). `ResolutionResults.departed` fills it from each
  `DepartureResult`'s `newCardId` / `destination`; `DestroyTargetPermanentEffectHandler` now records its batch path from
  `destroyBatchWithOutcome`'s results and its target path by locating the card, and `ExileTargetPermanentEffectHandler` records the
  exiled card. A token becomes no card. `SubjectResolutionServiceTest$CardSubjects` (+7).
- **The token-copy step.** `CreateTokenCopyOfCardEffect` gained `CardSubject cards` (`of(cards, options)`; `null` keeps the fixed or
  triggering card) and `creatureTypesInstead` (`withCreatureTypesInstead`: "except it's a 4/4 black Zombie", the replace-creature-types
  copy exception, CR 707.9b; The Scarab God, Lazotep Quarry, Ardyn). A subject's copies are one ordered creation event (CR 616.1); a
  tapped-and-attacking copy attacks what the source attacks, or with `chooseAttackTarget` what its controller chooses (CR 508.4). The 15
  cards are the target exile (`ExileGraveyardCardsEffect.oneTargetFromControllerGraveyard` / `upToOneTargetFromControllerGraveyard` /
  `upToOneTargetFromAnyGraveyard`, new untracked factories, or `exactTargetedFromAnyGraveyard`; The Cloning of Shredder keeps
  `oneTargetFromControllerGraveyardWithSource`) + `CreateTokenCopyOfCardEffect.of(Subjects.cardsThisWay(EXILE), options)`, in a
  `SequenceEffect` on a trigger and a `MayEffect` for "you may" (Séance, Gyrus). Soul Separator's Zombie is `CreateTokenEffect("Zombie",
  AmountThisWay.exiledPower(), exiledToughness(), …)` as the next step (its ruling: the Spirit is on the battlefield first); Abyssal
  Harvester's "then exile all other Nightmare tokens you control" is `ExileTargetPermanentEffect(Subjects.except(controlledBy(you(),
  Nightmare token), createdThisResolution()))` (new `PermanentSubject.Except`, the `PlayerSubject.Except` shape; its ruling: all other
  Nightmare tokens, not only those it made) and its "put there this turn" target filter the new `CardPutIntoGraveyardThisTurnPredicate`
  (PREDICATES_REFERENCE row). The four engine branches of the old record (`GraveyardTargetValidators`, `ValidTargetService`,
  `TargetLegalityService`, `GraveyardTargetingSupport`) are deleted.
- **`ExileGraveyardCardsEffect`'s target filter gets its context** (parity for the moved cards): the validator, `TargetLegalityService`'s
  spell-target checks and `ValidTargetService`'s candidate list now judge the filter with the graveyard's owner, X, the source and its
  snapshot (context-free before, which rejected Gyrus's "lesser power" and Nahiri's "less than Nahiri's loyalty"), and the resolution
  check passes the source snapshot ("uses last-known loyalty when Nahiri leaves"). The declared-filter check in `ValidTargetService`
  passes the card's graveyard owner instead of the controller as the card owner (the parameter's meaning; it differs only for another
  player's graveyard).
- **The return step.** `ReturnCardFromGraveyardEffect` gained `CardSubject cards` (with `returnAll`; refused otherwise) and
  `sacrificeAtYourNextEndStep` (card-subject path only): the subject's cards still in a graveyard and matching the filter return
  together through the all-graveyards tracked return (`GraveyardReturnSupport.resolveReturnOfCards`, the old tracked method's body
  shared; simultaneous entry, legend rule after). Sorin −6 = `DestroyTargetPermanentEffect(allTargets())` + the return over
  `cardsThisWay(DESTROY)`; Come Back Wrong = `DestroyTargetPermanentEffect()` + the return with a creature filter and
  `sacrificeAtYourNextEndStep(true)` (its ruling: nothing can happen between the destroy and the return); Silvan Reveler = `DiscardEffect(1,
  you())` + the return over `cardsThisWay(DISCARD)` with a land filter, tapped. `TargetPolarityClassifier`'s entry for the old record is
  gone (the destroy step is `RemovalEffect` DESTROY, HARMFUL_REMOVAL; `TargetPolarityGuardTest` now asserts it).
- **The "cast it now for free" step.** `MayCastDiscardedCardWithoutPayingManaCostEffect(CardSubject cards[, CardPredicate filter])`:
  each card the subject names that you own and that matches (default nonland) is offered as the resolution's free cast from its
  graveyard or exile (the old handler's two branches). Malcolm: `DiscardEffect(1, you())` + `ConditionalEffect(SourceCounterThreshold(4,
  CHORUS), …(cardsThisWay(DISCARD)))` (the counter check moved out of the handler; the condition reads Malcolm as it last existed, as its
  ruling requires). Hidetsugu and Kairi: `ExileTopCardOfOwnLibraryEffect(false)` + `LoseLifeEffect(AmountThisWay.exiledManaValue(),
  targetPlayer())` + `…(cardsThisWay(EXILE), instant or sorcery)` (its rulings: an illegal target means no exile; the cast is part of the
  resolution). The loss is its own step, so Lich's Duel Mastery's question is asked by `LoseLifeEffect` before the loss, after the exile.
  The record's name now under-describes it (step 10).
- **Behaviour changes (Oracle text / rulings).** Mordor on the March (LTC 512) "Exile a creature card from your graveyard" doesn't target:
  it was a targeted exile; now `ExileGraveyardCardsEffect.fromOwnGraveyard(1, creature)` chosen as it resolves, and its storm copies (put
  straight on the stack, its ruling) each choose; the haste is "until end of turn" (`MordorOnTheMarchTest` rewritten: +2 cases, the
  choice and an empty graveyard; `@DisplayName`s added). Sorin (DKA 142) "return each card put into a graveyard this way": the old handler
  returned only creature and planeswalker cards (`SorinLordOfInnistradTest.minusSixReturnsANoncreatureCardWhosePermanentWasACreature`,
  an Opalescence-animated Glorious Anthem, fails on the old card). Gyrus (C18 41): its ruling "you choose which player or planeswalker the
  token is attacking. It doesn't have to be attacking the same player or planeswalker that Gyrus is attacking" (CR 508.4): the copy
  attacked Gyrus's target; now `tappedAndAttackingWithTargetChoiceExiledAtEndOfCombat()` (`GyrusWakerOfCorpsesTest.copyChoosesItsOwnAttackTarget`,
  fails on the old card). Prompt shapes: the target exiles' prompts are `ExileGraveyardCardsEffect`'s ("to exile"; Feldon's generic
  graveyard target too). The survivors' log lines win.
- **AI.** No AI class named the deleted records but `TargetPolarityClassifier` (above). The token-copy cards' target steps are benign
  graveyard targets as before.

**Tests** (one Gradle run of card tests, one `--tests` list per task, after per-card runs while converting): every rewritten card's test
and every card test constructing one (the helper sweep: Zodiark, Providence of Night, Otawara, Master's Rebuke, March of Wretched Sorrow,
Hour of Glory, God-Pharaoh's Faithful, Flame Discharge, Fires of Undeath, Hellrider, Huntmaster of the Fells, Lost in the Woods, Wolfhunter's
Quiver), Thrilling Encore and Myojin of Grim Betrayal (the shared tracked return), `--tests-for ExileGraveyardCardsEffect --max-per-class 20`,
`ReturnCardFromGraveyardEffect 15`, `CreateTokenCopyOfCardEffect 50`, `DestroyTargetPermanentEffect 10`, `ExileTargetPermanentEffect 10`,
`CreateTokenCopyOfTargetPermanentEffect 8`, `DiscardEffect 5`, `MayEffect 5`, `ExileTopCardOfOwnLibraryEffect 10`,
`ExileTargetCardFromGraveyardAndMayCastCopyEffect 20`: 128 classes (130 result files), 1,171 test cases, all pass; Gyrus again after its fix.
Application (`SubjectResolutionServiceTest` +7, `StepResultsTest`, `EffectCountRatchetTest`, `CardImmutabilityArchTest`,
`CardEffectTargetingConsistencyTest`, `ValidTargetServiceTest`, `TargetLegalityServiceTest`, `GraveyardTargetingServiceTest`,
`ReturnCardFromGraveyardEffectHandlerTest` (hand-built wiring gained the resolver), `ExileTargetPermanentEffectHandlerTest`,
`DestroyTargetPermanentEffectHandlerTest`, `ExileGraveyardCardsEffectHandlerTest`, `PredicateEvaluationServiceTest`,
`EffectResolutionServiceTest`, `MayEffectHandlerTest`, `ResolutionScratchpadAmountTest`, `DestroyAllPermanentsEffectHandlerTest`,
`DestructionSupportFallbackTest`, `MayAbilityHandlerServiceTest`): 837 test cases; AI (`TargetPolarityGuardTest`, `GameDataDeepCopyTest`,
`SpellEvaluatorTest`, `AiTargetSelectorTest`): 278; all pass. Application, AI, engine testFixtures and card-test `a c f g h l m n o s t`
test sources compile.

**For later runs.**

- "Exile target [filter] card from your graveyard / a graveyard. Create a token that's a copy of it [except …]" is the target exile +
  `CreateTokenCopyOfCardEffect.of(Subjects.cardsThisWay(StepKind.EXILE), options)[.withCreatureTypesInstead(types)]`; "return each card put
  into a graveyard this way" is `ReturnCardFromGraveyardEffect.builder()….returnAll(true).cards(Subjects.cardsThisWay(StepKind.DESTROY))`;
  "you may cast it / the discarded card without paying its mana cost" is `MayCastDiscardedCardWithoutPayingManaCostEffect(cardsThisWay(kind)[,
  filter])`.
- **Next run.** (1) The may-cast-a-copy step, then `ExileTargetCardFromGraveyardAndMayCastCopyEffect` (14 cards): give
  `CopyImprintedCardAndMayCastCopyEffect` a `CardSubject` (default: the imprinted / exiled-with-source cards) and the record's cast options
  (`lifeLossOnCast`, `withoutPayingManaCost`, `afterSuccessfulCastEffect`); the copy's accept path is `ExileTargetCardFromGraveyardAndMayCastCopyHandler`'s
  (split cards, modes, X, life loss, the free-cast queue), and that record is also the `PendingMayAbility` marker three engine handlers build
  (`RandomPlayerExiles…`, `ExileOwnGraveyardInstantsOrSorceries…`, `ExileGraveyardCardsAndMayCastCopies…`), so it stays as the marker or the
  marker moves with the accept path. First make `ExileGraveyardCardsEffect`'s target filter read the trigger's event value (Soundwave's
  `CardManaValueEqualsEventValuePredicate`, Saruman's reflexive `CardManaValueAtMostEventValuePredicate`: its validator, `ValidTargetService`
  and the resolution check pass none) and use `CardPutIntoGraveyardThisTurnPredicate` for Reenact the Crime. (2) An exile step over a
  `CardSubject` (Leonardo da Vinci: "exile it from your graveyard. If you do, create a token copy"), and a seek filter reading one
  (Anguished Recollection). (3) Hour of Eternity and Foggy Swamp Visions can take `CreateTokenCopyOfCardEffect.of(cardsThisWay(EXILE))`
  once their "X target creature cards" exile is a step (`SpellCastingService` and `AiDecisionEngine` special-case
  `ExileCreaturesFromGraveyardAndCreateTokensEffect` by class; `ExileCardsFromGraveyardEffect` has `maxTargets` / `singleGraveyard`, check it).
  (4) The Ruinous Powers (a random-opponent `PlayerSubject` and a "cast a spell this way" delayed trigger naming the exiled card) and the
  threshold transform are still open.
- **Remaining fused multi-card records** (no ledger row, used by two or more cards, a `Then` / `And` / `IfYouDo` / `Unless` / `OrElse`
  name): 111 (1,155 card uses); by first word Exile 19, Search 10, Reveal 9, Prevent 7, Counter 7, Each 6, Choose 5, Cant 5, Put 3, Draw 3,
  Destroy 3, Creatures 3, Boost 3, Shuffle 2.

### Step 8, run 24 (2026-11-20)

Run 23's "Next run" items (1) and (2); item (3) not reached. Verdicts (ledger, step `8`): **absorbed 1**
(`ExileTargetCardFromGraveyardAndMayCastCopyEffect`, with its `normalfx` and `mayfx` handlers), **split 2**
(`ExileDiscardedCardAndCreateTokenCopyEffect`, `SeekCardsNotSharingCardTypeWithTriggeringCardEffect`, with their handlers), **survivor 3**
(`CopyImprintedCardAndMayCastCopyEffect`, `ExileGraveyardCardsEffect`, `SeekCardsToHandEffect`). Ratchet 4,715 / 3,945 → 4,712 / 3,941. 16 card
files rewritten by hand (a deleted scratch script did the 14 exact replacements; each read back by eye, imports sorted). Oracle text of every
rewritten card checked with `get_card`; rulings with `get_rulings` for Soundwave, Saruman, Reenact the Crime, Kaervek, Jacob Frye, Mizzix's
Mastery, Shiko, Narset, Flawless Forgery and Demilich (none contradicts the split; the deciding ones below). CR 400.7, 603.3d, 608.2b, 608.2c,
707.10a and 707.12 checked with `get_rule` (2026-11-20).

- **The graveyard exile's target filter reads the trigger's event value.** `ExileGraveyardCardsEffect`'s target paths judge the filter with
  the stack entry's event value when it is a triggered ability (read before the step writes its own count), and `validateExileGraveyardCards`
  passes the context's: Soundwave's `CardManaValueEqualsEventValuePredicate` ("mana value equal to the damage dealt", its ruling) and Saruman's
  reflexive `CardManaValueAtMostEventValuePredicate` are rechecked as the ability resolves, as the absorbed handler did. The opponent-graveyard
  path judged its filter with no context at all; it now passes the graveyard owner, X, the source and its snapshot too, and it reads a spell's
  single graveyard target from the entry's target id (Flawless Forgery's target rides there; an ability's on its target card ids). The
  spell and activated-ability candidate lists need no event value (only a trigger has one). New factory `oneTargetFromOpponentGraveyard(filter)`.
- **The may-cast-a-copy step.** `CopyImprintedCardAndMayCastCopyEffect` gained `CardSubject cards` and the cast options
  (`withoutPayingManaCost`, `lifeLossOnCast`, `afterSuccessfulCastEffect`): `of(cards)`, `.payingItsCosts()`, `.losingLifeOnCast(n)`,
  `.ifCastThen(effect)`; the compact constructor refuses cast options without a subject and an imprint flag with one. With a subject its
  handler copies each named card that is in exile (one may-choice per copy, the first card's first, the source permanent kept for the
  "if you do" rider); its `mayfx` handler casts such a copy with the absorbed record's accept path (split halves, a modal mode, X when the
  costs are paid, the life loss as a step of the parked resolution, the rider after the cast, the free-cast queue for an additional cost),
  while the imprinted default still goes through `ExileFreeCastQueueSupport` as before. `ChoiceHandlerService` (the copy's mode choice) and
  `XValueChoiceInteractionHandler` (its X) now reach the survivor's `mayfx` handler. **No marker record is kept**: the three engine handlers
  that exile and copy cards themselves (`RandomPlayerExilesInstantOrSorceryAndMayCastCopy…`, `ExileOwnGraveyardInstantsOrSorceriesAndMayCastCopies…`,
  `ExileGraveyardCardsAndMayCastCopies…`; Wildfire Devils, Mizzix's Mastery overloaded, The Tale of Tamiyo) park their copies with
  `CopyImprintedCardAndMayCastCopyEffect.of(cardsThisWay(EXILE))` (`.payingItsCosts()` for The Tale of Tamiyo), which their handlers never
  resolve; its subject only selects the accept path. The 14 cards are the target exile (`oneTargetFromControllerGraveyard`,
  `upToOneTargetFromControllerGraveyard`, `oneTargetFromOpponentGraveyard`, `exactTargetedFromAnyGraveyard(1, filter, false)`) + the copy step,
  in a `SequenceEffect` on a trigger slot (Saruman's inside its `QueueReflexiveAbilityEffect`, Mizzix's inside the `ConditionalReplacementEffect`)
  and as two spell effects on Reenact the Crime and Flawless Forgery. Reenact the Crime's "put there from anywhere this turn" is the
  `CardPutIntoGraveyardThisTurnPredicate` in the filter (the absorbed record's boolean and its `ValidTargetService` / validator branches are gone).
  Jacob Frye's "exile up to one target … If you do, copy it": the copy step names nothing when nothing was exiled.
- **An exile step over a card subject.** `ExileGraveyardCardsEffect.cards(CardSubject)` (new scope `GraveyardExileScope.CARDS`, 14th component
  `cards`; the compact constructor ties the two): each named card still in a graveyard is exiled and recorded as the EXILE step; the subject is
  read before the step records its own empty result. Leonardo da Vinci = `DrawCardEffect` + `SequenceEffect.of(DiscardEffect(1, you()),
  ConditionalEffect(toACard(DISCARD, artifact), SequenceEffect.of(ExileGraveyardCardsEffect.cards(cardsThisWay(DISCARD)),
  CreateTokenCopyOfCardEffect.of(cardsThisWay(EXILE), Thopter options))))`; a discarded artifact a replacement put into exile is not in the
  graveyard, so nothing is exiled or copied (`LeonardoDaVinciTest.discardedArtifactExiledByReplacementIsNotCopied`, Rest in Peace; the old
  handler behaved the same).
- **A seek filter over a card subject.** `CardSharesCardTypeWithCardsPredicate(CardSubject)` (PREDICATES_REFERENCE row): unbound it matches
  nothing (like the event-value predicates); `SubjectResolutionService.bindCardSubjects` turns it into "any of the card types those cards have
  now" (layered, through `GameQueryService.cardHasType` with each card's owner) inside `AllOf` / `AnyOf` / `Not`, and `SeekCardsToHandEffect`
  binds its filter before seeking. Anguished Recollection = `DiscardEffect(1, you())` + `ConditionalEffect(HappenedThisWay.discarded(),
  SeekCardsToHandEffect(Fixed(2), not(CardSharesCardTypeWithCardsPredicate(cardsThisWay(DISCARD)))))`.
- **Behaviour.** No rules change. Log lines are the survivors' (the exile's "exiles … from a graveyard / an opponent's graveyard", the seek's
  "seeks N cards"); Anguished Recollection's seek no longer shuffles the library afterwards (a seek is a random pick; nothing reads the order);
  the copy prompt text is unchanged.
- **AI.** No AI class named a deleted record; the rewritten cards' targets are the exile step's benign graveyard target as before
  (`TargetPolarityGuardTest`, `AiTargetSelectorTest`, `AiDecisionEngineTest` pass).

**Tests** (one Gradle run of card tests, one `--tests` list per task, after per-card runs while converting): the 16 rewritten cards, the five
imprint-copy cards, The Tale of Tamiyo, Wildfire Devils and every card test constructing one of them (the helper sweep), `--tests-for
ExileGraveyardCardsEffect --max-per-class 25`, `SeekCardsToHandEffect` (all), `CreateTokenCopyOfCardEffect 10`, `DiscardEffect 5`,
`QueueReflexiveAbilityEffect 8`: 86 classes (86 result files), 769 test cases, all pass (Flawless Forgery failed before the opponent path read
the spell's target id). Application (`ExileGraveyardCardsEffectHandlerTest` (hand-built wiring gained the resolver), `DiscardEffectHandlerTest`,
`SubjectResolutionServiceTest`, `StepResultsTest`, `EffectCountRatchetTest`, `CardImmutabilityArchTest`, `TargetSpecRatchetTest`,
`InputHandlerEpilogueRatchetTest`, `SimulationCopyCompletenessTest`, `CardEffectTargetingConsistencyTest`, `ValidTargetServiceTest`,
`TargetLegalityServiceTest`, `GraveyardTargetingServiceTest`, `PredicateEvaluationServiceTest`, `EffectResolutionServiceTest`,
`MayEffectHandlerTest`, `MayAbilityHandlerServiceTest`, `SpellTriggersTest`, `TriggeredAbilityStackServiceTest`,
`XValueChoiceInteractionHandlerTest`, `ColorChoiceInteractionHandlerTest`, `ReflexiveRouteRecordTest`, `ActivatedAbilityExecutionServiceTest`,
`GraveyardChoiceInteractionHandlersTest`, `StackResolutionServiceTest`, `CreateTokenEffectHandlerTest`) and AI (`AiDecisionEngineTest`,
`AiTargetSelectorTest`, `TargetPolarityGuardTest`, `GameDataDeepCopyTest`, `SpellEvaluatorTest`): 170 result files, 1,556 test cases, all pass.
Application, AI, engine testFixtures and card-test `a d e f i j k l m n r s t w` test sources compile.

**For later runs.**

- "Exile target [filter] card from your graveyard / an opponent's graveyard / a graveyard. Copy it. You may cast the copy [without paying its
  mana cost]" is the target exile + `CopyImprintedCardAndMayCastCopyEffect.of(Subjects.cardsThisWay(StepKind.EXILE))` (`.payingItsCosts()`,
  `.losingLifeOnCast(n)`, `.ifCastThen(effect)`); "exile it from your graveyard" after a step that put a card there is
  `ExileGraveyardCardsEffect.cards(cardsThisWay(kind))`; a card filter reading a card subject is bound by its handler through
  `SubjectResolutionService.bindCardSubjects` (only `SeekCardsToHandEffect` does so now; another reader must bind too).
- **Next run.** (1) The X-target exile: `ExileCreaturesFromGraveyardAndCreateTokensEffect` (Hour of Eternity, Midnight Ritual, Foggy Swamp
  Visions, Necromancer's Covenant). `ExileCardsFromGraveyardEffect` is the candidate first step (X-scaled `maxTargets` 0, `exactTargets`), but
  its two spell-cast paths in `SpellCastingService` (the `exileFromGraveyardEffect` branches near the cast-target code and the X validation)
  offer only "exactly N from a single graveyard" or "up to N from all graveyards", ignore `ownGraveyardOnly` for a spell, and its handler
  records no EXILE step; `AiDecisionEngine.getMaxXForGraveyardRequirements` caps X by class. Give it a spell-time graveyard scope (yours / all,
  exact X) honoured in both places and the AI cap, record EXILE, then Hour = it + `CreateTokenCopyOfCardEffect.of(cardsThisWay(EXILE), 4/4
  black).withCreatureTypesInstead(ZOMBIE)`, Foggy Swamp Visions the same copy + its end-step sacrifice, Midnight Ritual the Zombies ×
  `AmountThisWay` of the exile. Necromancer's Covenant can split now (`TARGET_PLAYER_ALL_MATCHING` with a creature filter records EXILE).
  (2) The threshold transform (run 21's blockers unchanged). (3) The Ruinous Powers. (4) Check whether the subject path's resolution cast
  honours a copy's own "cast only during …" restriction (Mizzix's Mastery's ruling: timing restrictions from the copy's type are ignored, other
  restrictions are not); the imprinted default's free-cast queue checks it, the subject path (the absorbed record's) may not; unify the two
  accept paths if it doesn't. (5) Soundwave's "from their graveyard" is the damaged player's; the opponent-graveyard scope admits any
  opponent's (unchanged; matters with more than one opponent).
- **Remaining fused multi-card records** (no ledger row, used by two or more cards, a `Then` / `And` / `IfYouDo` / `Unless` / `OrElse`
  name): 110 (1,150 card uses); by first word Exile 19, Search 10, Reveal 9, Prevent 7, Counter 7, Each 6, Choose 5, Cant 5, Put 3, Draw 3,
  Destroy 3, Creatures 3, Boost 3, Shuffle 2, Grant 2, Create 2.

### Step 8, run 25 (2026-11-20)

Run 24's "Next run" items (4), (1), (2) and (3), in that order; item (4) of the brief (the next fused family) audited, not started.
Verdicts (ledger, step `8`): **split 2** (`ExileCreaturesFromGraveyardAndCreateTokensEffect`, `PutCounterOnSelfThenTransformIfThresholdEffect`,
with their handlers), **survivor 4** (`ExileCardsFromGraveyardEffect`, `GraveyardCardChoosingEffect`, `CopyImprintedCardAndMayCastCopyEffect`'s
`mayfx` handler, `PermanentCounterSupport` lost `removeCountersAndTransform`), **blocked-other 1** (The Ruinous Powers' record, unchanged).
Ratchet 4,712 / 3,941 → 4,710 / 3,939. 11 card files rewritten (Hour of Eternity, Midnight Ritual, Foggy Swamp Visions and Necromancer's
Covenant by hand; the six threshold cards by a deleted scratch script, read back by eye, imports sorted; Mizzix's Mastery's test). Oracle text
of every rewritten card checked with `get_card` (Edgar Markov's Coffin as Edgar, Charmed Groom's back face, VOW 236; Cult of the Waxing Moon);
rulings with `get_rulings` for all of them and for Elite Arcanist (Midnight Ritual has none). CR 400.7, 601.2, 601.3, 608.2b, 608.2g, 608.2h,
616.1, 701.27f, 701.27g, 707.10a and 707.12 checked with `get_rule` (2026-11-20).

- **The cast-copy restriction bug (fixed).** A copy cast through the card-subject path (`CopyImprintedCardAndMayCastCopyEffect.of(cards)`: the
  14 run-24 cards, Mizzix's Mastery overloaded, Wildfire Devils, The Tale of Tamiyo) ignored the copy's own "cast this spell only during …"
  restriction: `SpellCastingService.playCardFromExileInternalOnce` checks `isSpellCastingAllowed` and the cast condition but never
  `canCastWithSpellTimingRestriction` (the free-cast queue checks it for copies). Mizzix's Mastery's ruling: "Timing restrictions based on the
  copy's type are ignored. Other restrictions (such as "Cast [this spell] only during combat") are not"; Elite Arcanist's says the same; CR
  601.3 (a player begins to cast a spell only if no rule or effect prohibits it). The check is now `ExileFreeCastQueueSupport.castableFromExile`
  (cast-only-from-graveyard, casting prohibitions, and for a copy its timing restriction), asked by the free-cast queue and by the subject
  path's accept before the split / mode / X branches; a copy that fails it ceases to exist with "cannot be cast from exile."
  (`MizzixsMasteryTest.copyKeepsItsOwnCastOnlyDuringCombatRestriction`, Gorilla War Cry in a main phase, fails on the old code: the copy
  went on the stack). Only the check is shared; the two accept paths stay (the subject path pays costs, loses life and runs the rider; the queue
  pays additional costs and escalate). A non-copy resolution cast from exile (Hidetsugu and Kairi's free cast, the queue's non-copy cards) still
  skips the timing-restriction check; CR 601.3 applies to them too, so the next run that touches `playCardFromExileInternalOnce` should check it there.
- **The X-target graveyard exile.** `ExileCardsFromGraveyardEffect.exactlyXFromOwnGraveyard(filter)` / `exactlyXFromGraveyards(filter)` (the
  existing `ownGraveyardOnly` / `allGraveyardsForSpell` with `exactTargets`; `exactSpellGraveyardScope()`): both `SpellCastingService` cast
  paths (hand and flashback) offer exactly X matching cards of that scope through `handleExactNGraveyardSpellTargeting` (X=0: no targets), and
  the cast-time check refuses an X above the matching cards ("Not enough creature cards in graveyard", the old text; it replaces the
  by-class `needsGraveyardCreatureTargeting` block). The handler now records the EXILE step (cards, graveyard owners; it never prompts) and
  rechecks each target's scope and filter as it resolves (CR 608.2b; before, any card still in any graveyard was exiled). The AI's X cap
  lost its by-class branch: `GraveyardCardChoosingEffect.choosesFromControllersGraveyardOnly()` makes the exact-X cap count only the caster's
  graveyard (`AiDecisionEngineTest.doesNotCastMidnightRitualWithOnlyOpponentCreaturesInGraveyard`, new). Hour of Eternity =
  `exactlyXFromOwnGraveyard(creature)` + `CreateTokenCopyOfCardEffect.of(cardsThisWay(EXILE), 4/4 black).withCreatureTypesInstead(ZOMBIE)`;
  Foggy Swamp Visions = `exactlyXFromGraveyards(creature)` + the plain copy + `SacrificeCreatedPermanentsAtEndStepEffect(true)`; Midnight Ritual
  = `exactlyXFromOwnGraveyard(creature)` + `CreateTokenEffect(new AmountThisWay(EXILE), Zombie)`; Necromancer's Covenant = `ExileGraveyardCardsEffect(0,
  TARGET_PLAYER_ALL_MATCHING, creature)` + the same Zombies, in a `SequenceEffect` on its trigger slot.
- **The threshold transform.** "Put a [kind] counter on this. Then if there are N or more, remove those counters and transform it" =
  `PutCountersOnSelfEffect(kind)` + `ConditionalEffect(new SourceCounterThreshold(N, kind), SequenceEffect.of(new RemoveAllCountersEffect(kind), new
  TransformSelfEffect()[, Treasure tokens]))`, a `MayEffect` around the sequence for Primal Amulet. Run 21's blockers held no longer: the condition
  reads the entry's source snapshot once the source left (the old handler read the same snapshot; Treasure Map's ruling: three counters before it
  left still make the Treasures); `TransformSelfEffect` has the old guards (transformed since the ability went on the stack, CR 701.27g; transform
  prevented). Ludevic's Test Subject, Heirloom Mirror, Edgar Markov's Coffin, Brass's Tunnel-Grinder, Treasure Map, Primal Amulet.
- **Behaviour changes.** The threshold transform was a bare face swap (`removeCountersAndTransform`): no transform event, no unattach, no
  as-transforms abilities. It now transforms through `AnimationSupport`, so "whenever a permanent you control transforms into a non-Human
  creature" sees it (CR 701.27f; `LudevicsTestSubjectTest.transformationTriggersTransformAbilities`, Cult of the Waxing Moon, which the old code
  could not trigger: it emitted no `PermanentChangeEvent`). The activated threshold abilities declared the source as a target
  (`selfTargeting`); they target nothing now. The counter-removal log line is `RemoveAllCountersEffect`'s. Primal Amulet's "you may" is asked
  even when it left with four or more counters (then nothing happens; before, it was not asked). Hour of Eternity's and Midnight Ritual's
  exile logs one line for all cards (the survivor's).
- **The Ruinous Powers** stays (blocked-other): "choose an opponent at random. Exile the top card of that player's library" needs the random
  choice made once and read by later steps (a recorded choose-player step; a random `PlayerSubject` resolved per step would pick again), and
  "when you cast a spell this way" a delayed cast trigger bound to `cardsThisWay(EXILE)`. One card; build them when another record needs them
  (`AttachSourceAuraToRandomOpponentEffect`, `ChooseRandomOpponentMustAttackThisCombatEffect`, `DealDamageToRandomOpponent*` are candidates).
- **Item 4 audited, not started.** The Counter family's step-recording first halves: `CounterSpellOrAbilityAndDestroySourceEffect` (Teferi's
  Response, Ouphe Vandals: "if a permanent's ability is countered this way, destroy that permanent") and `CounterAbilityAndLockSourceEffect`
  (Interdict, Trickbind: "that permanent's activated abilities can't be activated") split into `CounterSpellEffect` + a step on "the source of
  the countered spell or ability", which no subject names: add a `PermanentSubject` for it (the countered entry's `sourcePermanentId`, remembered
  on the entry as it is countered, like `counteredSpellControllerId`; a new `StackEntry` field is copied for simulations) and a subject on
  `LockTargetPermanentEffect` (31 cards; keep its target constructors). `CounterSpellAndExileThenGrantFreeCastPermissionEffect` (4) is
  `CounterSpellEffect(EXILE)` + a permission over `cardsThisWay`-style countered cards (COUNTER_SPELL is not a `CardSubject` kind yet).
- **Not changed, noted.** `AnimationSupport` cites "CR 701.27d" for the instant-or-sorcery face; that text is CR 701.27e now (701.27d is
  "not a double-faced card"). `GraveyardReturnSupport` still holds about a dozen Javadoc blocks with no method under them.

**Tests** (per-card runs while converting, then one Gradle run, one `--tests` list per task): the 10 rewritten cards' tests and
every card test constructing one (the helper sweep: Deeproot Champion, Sorcerous Spyglass, Swashbuckling, The Ruinous Powers), Mizzix's
Mastery, Cult of the Waxing Moon, Gorilla War Cry, Elite Arcanist, Wildfire Devils, The Tale of Tamiyo, `--tests-for
ExileCardsFromGraveyardEffect` (all), `CopyImprintedCardAndMayCastCopyEffect` (all), `CreateTokenCopyOfCardEffect PutCountersOnSelfEffect
TransformSelfEffect RemoveAllCountersEffect --max-per-class 8`, `ExileGraveyardCardsEffect --max-per-class 10`; application
(`ExileCardsFromGraveyardEffectHandlerTest`, `ExileGraveyardCardsEffectHandlerTest`, `SpellCastingServiceTest`, `CollectorRouteRecordTest`,
`LoneMayAbilityRecordTest`, `MayCastHandlerServiceTest`, `TargetLegalityServiceTest`, `StackResolutionServiceTest`, `SpellTriggersTest`,
`LastCounterRemovedTriggerTest`, `LibraryChoiceHandlerServiceTest`, `CardImmutabilityArchTest`, `EffectCountRatchetTest`, `StepResultsTest`,
`SubjectResolutionServiceTest`, `SimulationCopyCompletenessTest`, `CardEffectTargetingConsistencyTest`, `MayAbilityHandlerServiceTest`,
`GraveyardTargetingServiceTest`, `EffectResolutionServiceTest`, `AbilityActivationServiceTest`, `ActivatedAbilityExecutionServiceTest` and
three graveyard handler tests) and AI (`AiDecisionEngineTest` +1, `EasyAiDecisionEngineTest`, `MediumAiDecisionEngineTest`,
`AiTargetSelectorTest`, `TargetPolarityGuardTest`, `GameDataDeepCopyTest`, `SpellEvaluatorTest`): 140 classes, 299 result files, 2,605 test
cases, all pass (five Hour of Eternity / Midnight Ritual X-check tests failed on the new message text before it named the filter again).
New tests: Mizzix's Mastery (fails on the old code), Ludevic's Test Subject, the AI cap. Application, AI, engine testFixtures and card-test
`b e f h l m n p t` test sources compile.

**For later runs.**

- "Exile X target [filter] cards from your graveyard / from graveyards" on a spell is `ExileCardsFromGraveyardEffect.exactlyXFromOwnGraveyard` /
  `exactlyXFromGraveyards`; "for each card exiled this way" after it is `new AmountThisWay(StepKind.EXILE)`, "a copy of it" is
  `CreateTokenCopyOfCardEffect.of(cardsThisWay(EXILE), …)`. A counter-threshold transform composes from `PutCountersOnSelfEffect`,
  `SourceCounterThreshold`, `RemoveAllCountersEffect` and `TransformSelfEffect`.
- **Next run.** (1) The Counter family: the countered-source subject, `LockTargetPermanentEffect` with a subject, then
  `CounterSpellOrAbilityAndDestroySourceEffect` and `CounterAbilityAndLockSourceEffect` (4 cards); `CounterSpellAndExileThenGrantFreeCastPermissionEffect`
  if a COUNTER_SPELL card subject fits. (2) The timing-restriction check for non-copy resolution casts from exile (above). (3) The "unless"
  counters (`CounterUnlessDiscardsEffect` 14, `CounterUnlessPaysLifeEffect` 14, `CounterUnlessSacrificesEffect` 9) need a `MayEffect` whose
  chooser is the target spell's controller; check `MayEffect`'s chooser options first. (4) Then Search 10, Reveal 9, Prevent 7.
- **Remaining fused multi-card records** (no ledger row, used by two or more cards, a `Then` / `And` / `IfYouDo` / `Unless` / `OrElse`
  name): 110 (1,150 card uses), unchanged (this run's two classes had ledger rows); by first word Exile 19, Search 10, Reveal 9, Prevent 7,
  Counter 7, Each 6, Choose 5, Cant 5, Put 3, Draw 3, Destroy 3, Creatures 3, Boost 3, Shuffle 2, Grant 2, Create 2.

### Step 8, run 26 (2026-11-20)

Run 25's "Next run" items (2), the two noted fixes, then (1) and (3); item (4) (Search, Reveal, Prevent) audited, none split.
Verdicts (ledger, step `8`): **split 4** (`CounterSpellOrAbilityAndDestroySourceEffect`, `CounterAbilityAndLockSourceEffect`,
`CounterAbilityAndRemoveSourceAbilitiesEffect`, `CounterSpellAndCreateTreasureTokensEffect`, with their handlers), **survivor 3**
(`LockTargetPermanentEffect`, `CounterSpellEffect`, `LosesAllAbilitiesEffect`), **blocked-other 8**, **blocked-step-model 1**, **unique 5**.
Ratchet 4,710 / 3,939 → 4,706 / 3,935. 6 card files rewritten by hand (Teferi's Response, Ouphe Vandals, Interdict, Trickbind, Tishana's
Tidebinder, Spell Swindle; read back by eye, imports sorted). Oracle text of every rewritten card checked with `get_card`, rulings with
`get_rulings` (and Chandra, Torch of Defiance, Gorilla War Cry, Spider-Punk, Primal Amulet, Treasure Map). CR 113.7a, 121.6c, 400.7, 601.3,
608.2c, 608.2g, 608.2h, 701.27d, 701.27e, 701.50a and 707.2 checked with `get_rule` (2026-11-20).

- **Resolution casts from exile keep the card's own timing restriction (fixed).** Run 25 made the copy paths check "cast this spell only
  during …"; the non-copy resolution casts from exile did not. Chandra, Torch of Defiance's ruling: "Timing permissions based on the card's
  type are ignored, but other restrictions (such as "Cast [this card] only during combat") are not" (CR 601.3, 608.2g).
  `ExileFreeCastQueueSupport.castableFromExile(gameData, playerId, card)` (the `asCopy` parameter is gone) now checks the restriction for
  every card, and is asked before they begin by the free-cast queue's non-copy cards (discover with an additional cost, Improvisation
  Capstone, Brilliant Ultimatum, the exiled-card may-play choice), `ChandraTorchExileCastSupport` and `VaanExileCastSupport` (a card that
  fails takes the "if you don't" branch: the damage, the Treasure), `ExileReducedCastSupport` (stays exiled) and `ExileNormalCostCopySupport`
  (the copy ceases to exist). `LibraryChoiceHandlerService.canCastWithoutPaying` (its library searches and reveals that cast for free, discover, Sunbird's
  Invocation, Wild Evocation's hand card) checks it too. `ExileFreeCastSupport` (Hidetsugu and Kairi's free cast, Guile, Spell Queller) already
  did. `ChandraTorchOfDefianceTest.firstPlusOneHonoursExiledSpellsTimingRestriction` (Gorilla War Cry in a main phase) fails on the old code
  (it was cast). Not changed: `OutsideGameNormalCostCastSupport` (sideboard casts) and `PermanentChoiceSpellHandlerService`'s target
  completions, which call `playCardFromExileAsResolutionCast` after the castability was judged by their callers; a central guard in
  `playCardFromExileInternalOnce` would throw mid-prompt.
- **`AnimationSupport`'s citation** is CR 701.27e now (both comments).
- **Primal Amulet kept.** Its "you may" is asked when it left with four or more counters (nothing then happens). The threshold reads the
  source's last-known counters, which Treasure Map's ruling requires ("if it somehow already had three landmark counters on it before it
  left the battlefield, you'll get three Treasures"), so reading nothing for a departed source would be wrong for Treasure Map; the
  question is harmless.
- **The countered ability's source.** `Subjects.targetAbilitySource()` / `targetAbilitySource(filter)` (`PermanentSubject.TargetAbilitySource`,
  SUBJECTS_REFERENCE row): the permanent the targeted ability came from, read from the stack while the ability is there, else from the new
  `StackEntry.counteredAbilitySourceId`, which `CounterSpellEffectHandler` records beside `counteredSpellControllerId` when its target is an
  activated or triggered ability (copied by the `StackEntry` copy; a UUID). Nothing for a spell target, a sourceless ability or a permanent
  that left; the filter is judged as the reading effect applies (Tishana's Tidebinder's ruling: "checks … as it resolves").
  `SubjectResolutionServiceTest.targetAbilitySourceVariant`.
- **`LockTargetPermanentEffect` takes a subject** (sixth component, default `target()`; the 4- and 5-argument constructors keep the target;
  a non-target subject declares no target and skips the `declaredTarget` check). `activatedAbilitiesOf(subject, duration)`.
  `LosesAllAbilitiesEffectHandler`'s one-permanent path accepts the new subject.
- **The splits.** Teferi's Response = `CounterSpellEffect` + `ConditionalEffect(HappenedThisWay.countered(), DestroyTargetPermanentEffect(targetAbilitySource()))`;
  Ouphe Vandals the same without the gate; Interdict = `CounterSpellEffect` + `LockTargetPermanentEffect.activatedAbilitiesOf(targetAbilitySource(),
  UNTIL_END_OF_TURN)`; Trickbind the same, gated; Tishana's Tidebinder = `SequenceEffect.of(CounterSpellEffect, ConditionalEffect(countered(),
  LosesAllAbilitiesEffect(targetAbilitySource(artifact | creature | planeswalker), WHILE_SOURCE_REMAINS)))`; Spell Swindle = `CounterSpellEffect` +
  `CreateTokenEffect.ofTreasureToken(TargetSpellManaValue)` (Mystic Genesis's shape; the counter step records the mana value).
- **Behaviour changes (Oracle text).** Interdict ("That permanent's activated abilities can't be activated this turn") and Ouphe Vandals
  ("and destroy that artifact if it's on the battlefield") have no "countered this way": the old handlers did nothing when the ability
  couldn't be countered (Spider-Punk); now the lock and the destroy happen (`InterdictTest.locksSourceWhenTheAbilityCantBeCountered`,
  `OupheVandalsTest.destroysTheArtifactWhenTheAbilityCantBeCountered`, both fail on the old cards by their handlers' early return). Teferi's
  Response and Trickbind stay gated (`TeferisResponseTest.doesNotDestroySourceWhenTheAbilityIsNotCountered`). Log lines are the survivors'
  (the lock's "can't have its activated abilities activated", the loss's "for as long as its source remains on the battlefield"); no test
  asserted the old ones. Spell Swindle now carries `CounterSpellEffect`, so the AI sees a counterspell (it saw a plain effect).
- **Not split, and why.** The "unless" counters (`CounterUnlessDiscardsEffect` 14, `CounterUnlessPaysLifeEffect` 14, `CounterUnlessSacrificesEffect` 9)
  are blocked-other: `MayEffect` has a `TARGET_SPELL_CONTROLLER` chooser and an else-branch, but the records implement `CounterUnlessEffect`,
  whose ransom kind and magnitude the AI and ward read; the split needs that capability on the `MayEffect` (or on a marker), plus the
  can't-pay-life check in `MayEffectHandler`. `CounterSpellAndExileThenGrantFreeCastPermissionEffect` (4) and Delay's record need COUNTER_SPELL
  in `cardsThisWay` and a permission / suspend step over a `CardSubject`. Dovescape's record counters the triggering spell (with the caster
  baked as the target), which `CounterSpellEffect` can't name. Desertion's is a destination replacement (unique). Counterpoint, Teferi's
  Contingency and Minamo's Meddling have no primitive second half (unique). Item 4: no Search, Reveal or Prevent fused record has a first half
  that records a step (no `StepKind` for search, reveal or prevention); of the other families, `DrawDiscardAndConniveEffect` (44) is the
  connive keyword action (unique, CR 701.50a), the two dying-creature exile records need a triggering-card `CardSubject` (and Unscythe's /
  Timothar's token link, Nightmare Shepherd's copy of the creature as it last existed, CR 707.2), and Sindbad's draw-reveal-discard reruns
  after a draw replacement (blocked-step-model, CR 121.6c).

**Tests** (one Gradle run of card tests, one `--tests` list per task, after per-card runs while converting): the six rewritten cards, Chandra,
Torch of Defiance, and every card test constructing one of them, Gorilla War Cry or Spider-Punk (the helper sweep: Spider-Verse, Subterranean
Shambler, Mogg War Marshal, Mizzix's Mastery, Glen Elendra's Answer, Furious Reprisal, Fireforger's Puzzleknot, Fateful Showdown, Dynavolt
Tower, Draconic Intervention, Deadlock Trap, Aethertorch Renegade, Abuna's Chant), `--tests-for LockTargetPermanentEffect LockMatchingPermanentsEffect`
(all), `LosesAllAbilitiesEffect BrilliantUltimatumEffect DiscoverEffect ExileTopCardMayCastOrDealDamageEffect
ExileTopCardOfDamagedPlayerLibraryMayCastOrCreateTreasureEffect ImprovisationCapstoneEffect MayCastCopyWithManaCostEffect
MayCastCopyWithNormalCostEffect MayCastExiledCardWithCostReductionEffect MayCastExiledCardWithNormalCostEffect SunbirdsInvocationRevealAndCastEffect
TeachCastCopyEffect CascadeEffect CopyImprintedCardAndMayCastCopyEffect --max-per-class 12`, `CounterSpellEffect DestroyTargetPermanentEffect
ConditionalEffect --max-per-class 6`: 124 classes (124 result files), 1,191 test cases, all pass. Application (`SubjectResolutionServiceTest` +1,
`EffectCountRatchetTest`, `CardImmutabilityArchTest`, `TargetSpecRatchetTest`, `CardEffectTargetingConsistencyTest`, `StepResultsTest`,
`SimulationCopyCompletenessTest`, `EffectResolutionServiceTest`, `StackResolutionServiceTest`, `MayAbilityHandlerServiceTest`,
`LibraryChoiceHandlerServiceTest`, `SpellCastingServiceTest`, `LoneMayAbilityRecordTest`, `CollectorRouteRecordTest`, `MayCastHandlerServiceTest`
and every application test naming a touched class: `CounterSpellEffectHandlerTest`, `CreatureControlServiceTest`, `GameQueryServiceTest`, the
handler tests that wire `SubjectResolutionService`) and AI (`TargetPolarityGuardTest`, `GameDataDeepCopyTest`, `AiDecisionEngineTest`,
`SpellEvaluatorTest`, `AiTargetSelectorTest`): 300 result files, 2,067 test cases, all pass. Application, AI, engine testFixtures and card-test
`c i o s t` test sources compile.

**For later runs.**

- "Counter target activated or triggered ability. [If a permanent's ability is countered this way,] [verb] that permanent" is
  `CounterSpellEffect` + [`ConditionalEffect(HappenedThisWay.countered(), …)`] + the verb over `Subjects.targetAbilitySource([filter])`; "that
  permanent's activated abilities can't be activated" is `LockTargetPermanentEffect.activatedAbilitiesOf(subject, duration)`. A resolution
  cast from exile asks `ExileFreeCastQueueSupport.castableFromExile` before it begins.
- **Next run.** (1) The "unless" counters: give the `CounterUnlessEffect` capability (ransom kind, magnitude) a home that survives the split
  (a `MayEffect` with `TARGET_SPELL_CONTROLLER` whose else-branch is `CounterSpellEffect`, the AI and ward reading the ransom from it), move the
  can't-pay-life check beside the can't-sacrifice / can't-discard checks in `MayEffectHandler`, then `CounterUnlessPaysLifeEffect` (14),
  `CounterUnlessDiscardsEffect` (14), `CounterUnlessSacrificesEffect` (9); ask the human first if that changes how ward is modelled.
  (2) COUNTER_SPELL in `cardsThisWay` (the countered card where its destination put it), then
  `CounterSpellAndExileThenGrantFreeCastPermissionEffect` (4) with a cast-permission step over a `CardSubject`. (3) A triggering-card `CardSubject`
  ("exile that card" of a dies trigger) for the two dying-creature exile records. (4) `ExileTargetPermanentAndTrackWithSourceEffect` (24) and
  `ExileSelfAndReturnTransformedEffect` (54) are the largest unaudited "And" names; audit whether they are fused at all.
- **Remaining fused multi-card records** (no ledger row, used by two or more cards, a `Then` / `And` / `IfYouDo` / `Unless` / `OrElse`
  name): 100 (1,055 card uses); by first word Exile 17, Search 10, Reveal 9, Prevent 7, Each 6, Choose 5, Cant 5, Put 3, Destroy 3,
  Creatures 3, Boost 3, Shuffle 2, Grant 2, Create 2, and 23 first words with one each (Counter 1: `CounterUnlessPaysEffect`).

### Step 8, run 27 (2026-11-20)

Run 26's "Next run" items (2), (3) and (4) done; item (1) investigated and held for the human (below). Verdicts (ledger, step `8`):
**split 2** (`CounterSpellAndExileThenGrantFreeCastPermissionEffect`, `ExileDyingCreatureAndCreateTokenCopyEffect`, with their handlers),
**survivor 4** (`CounterSpellEffect`, `MayCastDiscardedCardWithoutPayingManaCostEffect`, `ExileSelfAndReturnTransformedEffect`, and the new step
`AllowPlayCardsWithoutPayingManaCostWhileExiledEffect`), **blocked-other 4** (`ExileDyingCreatureCardAndCreateTokenEffect` kept for Timothar; the three
"counter unless" records re-ledgered), **unique 2** (Delay's `CounterSpellAndExileWithSuspendCountersEffect`, `ExileTargetPermanentAndTrackWithSourceEffect`).
Ratchet 4,706 / 3,935 → 4,705 / 3,934 (two records and handlers gone, one of each added). 9 card files rewritten by hand (Spelljack, Kheru
Spellsnatcher, Thranduil's Decree, Transcendent Dragon, Nightmare Shepherd, Brenard, Unscythe, Liliana, Heretical Healer; read back by eye, imports
sorted). Oracle text of every rewritten card checked with `get_card`, rulings with `get_rulings` (and Delay's). CR 118.12, 118.12a, 400.7, 607.2a,
608.2c, 608.2d, 614.1a, 701.6a, 702.21a and 707.2 checked with `get_rule` (2026-11-20).

- **The "counter unless" records (item 1): not split, the human decides.** Premise check: `CounterUnlessEffect.ransomKind()` /
  `ransomMagnitude()` have no reader in `magical-vibes-ai` and none in ward detection. Their only readers are `MayPenaltyChoiceHandlerService`'s
  own completions, which use the kind to find their record among a pending ability's effects (PAY_MANA, PAY_WATERBEND, DISCARD_HAND) and the
  magnitude as a count. Ward is recognised by the WARD keyword plus `TargetTriggers.actsOnASpell` (the effect's `targetSpec` admits only a
  spell or ability); `MayEffect.targetSpec()` falls through to its else-branch, so `MayEffect(pay, prompt, CounterSpellEffect(), TARGET_SPELL_CONTROLLER)`
  would be recognised the same. The Hard AI scores a pending ransom with `SpellEvaluator.evaluateAbilityEffects` (0 for the old records and for an
  untargeted `DiscardEffect`, so it declines either way). So the capability needs no new home, but the split changes rules behaviour, so it was
  not done:
  - *Pay life* (14 cards): the old handler offers no payment when the spell can't be countered (`findCounterTarget` respects counterability), and
    counters at once when the life can't be paid. A `MayEffect` / `MayPayLifeEffect` asks in both cases (CR 118.12a lets a player pay an "unless"
    cost though nothing would be countered), `MayPayLifeEffect` has no `DynamicAmount` (Phyrexian Fleshgorger's power) and no target-spell
    payer, and the prompt text ("Pay 3 life to prevent X from being countered?") would lose the spell's name.
  - *Discard* (14 cards): equivalent except that `DiscardEffect.matching`'s filter-aware check withholds the offer when no card matches (the old
    handler asked, then countered), and a declined discard counters through `CounterSpellEffect`, which applies Guile's replacement.
  - *Sacrifice* (9 cards): Mana Vortex and Blood Funnel counter the triggering spell (no target), which `CounterSpellEffect` can't name;
    `MayEffectHandler`'s can't-sacrifice check covers only `you()` and a targeted player, not `controllerOf(targetSpell())`; the two-permanent
    ward (Valgavoth, Terror Eater: three nonland permanents) uses a "choose none to decline" multi-choice.
  - *A current rules bug either way*: `MayPenaltyChoiceHandlerService.counterUnlessCounter` (the declined discard and sacrifice ransoms) puts the
    spell into its graveyard without Guile's "would counter a spell … exile it instead" replacement (`CounterSupport.applyControlledCounterExileReplacement`),
    which the pay-mana path applies.
  - *Options*: (A) split the discard kind now (`MayEffect(DiscardEffect(1, controllerOf(targetSpell())) | DiscardEffect.matching(…) | random,
    prompt, CounterSpellEffect(), TARGET_SPELL_CONTROLLER)`), then pay life after giving `MayPayLifeEffect` a `DynamicAmount` and a target-spell
    payer (or after making `MayEffectHandler` judge a leading pay-life step), accepting "asked even when it can't be countered" (CR 118.12a);
    keep the sacrifice kind for its triggering-spell cards. Ward ends up two shapes: ward {N} (`CounterUnlessPaysEffect`, 308 cards, its X /
    dynamic / rider forms) stays on the capability. (B) Leave all "counter unless" records as they are (one ward shape); fix only the Guile bug
    in `counterUnlessCounter`. (C) A `CounterUnlessEffect`-shaped wrapper record (ransom step + counter) replacing all of them at once, including
    ward {N}: a step-10-sized change.
- **COUNTER_SPELL in `cardsThisWay`.** `CounterSpellEffectHandler` records the countered card where it went as `StepOutcome.movedCards`
  (exile or graveyard; none for a copy, an ability or a hidden zone); `CardSubject.CardsThisWay` accepts COUNTER_SPELL. Under Guile's
  replacement the counter step records NONE (as before, the spell is not "countered this way"). `SubjectResolutionServiceTest.counteredSpellsCard`.
- **The permission step.** `AllowPlayCardsWithoutPayingManaCostWhileExiledEffect(CardSubject)`: a non-expiring play permission without paying
  the mana cost (`ExileSupport.grantPlayWhileExiled` + `setExilePlayWithoutPayingManaCost`, as the old handler did) on each named card in exile; a
  card in another zone gets none. Spelljack = `CounterSpellEffect(EXILE)` + it over `cardsThisWay(COUNTER_SPELL)` (two spell effects); Kheru
  Spellsnatcher the same in a `SequenceEffect` on its turned-face-up trigger; Thranduil's Decree = `ConditionalReplacementEffect(TargetSpellMatches(
  StackEntryTypeInPredicate(creature / artifact / enchantment / planeswalker / battle spell)), CounterSpellEffect(), SequenceEffect.of(CounterSpellEffect(EXILE),
  permission))` (an Adventure cast is an instant or sorcery entry, as the old `spellFace` judged; the permission sits in the permanent branch so a
  nonpermanent spell cast with flashback, exiled by the counter, gets none: `ThranduilsDecreeTest.flashbackNonpermanentSpellGetsNoCastingPermission`).
  Transcendent Dragon = `CounterSpellEffect(EXILE)` + `MayCastDiscardedCardWithoutPayingManaCostEffect(cardsThisWay(COUNTER_SPELL))`, whose handler now
  offers a card in exile whoever owns it (graveyard cards still only from the controller's own graveyard; Malcolm's and Hidetsugu and Kairi's cards
  are their own, so nothing changes for them).
- **Delay is unique.** "Exile it with three time counters on it instead of putting it into its owner's graveyard. If it doesn't have suspend, it
  gains suspend" is one replacement of the countered card's destination (CR 614.1a), not a step after the counter, and no other card would use a
  give-suspend step (`ExileTriggeringSpellWithSuspendCountersEffect` exiles a triggering spell, a different first half).
- **The dying-creature exiles.** No new card subject: `ExileTriggeringCardFromGraveyardEffect` (run 9's survivor) already exiles the stack entry's
  triggering card and records EXILE, and also checks the card is the same graveyard incarnation (CR 400.7), which the absorbed handlers did not.
  Nightmare Shepherd / Brenard = `MayEffect(SequenceEffect.of(ExileTriggeringCardFromGraveyardEffect, ConditionalEffect(HappenedThisWay.exiled(),
  copy.ofTriggeringPermanent())))`: the copy step reads the departed creature's copiable values (`PermanentCopierService.copiableCard` of the
  departed snapshot), so a creature that was copying something is copied as that copy (Nightmare Shepherd's ruling, CR 707.2;
  `NightmareShepherdTest` covers it). Unscythe = the exile + `ConditionalEffect(exiled(), CreateTokenEffect.blackZombie(1))` (its ruling: a card
  removed in response, or a token, gives no Zombie). `ExileDyingCreatureCardAndCreateTokenEffect` stays for Timothar, Baron of Bats: its handler
  binds the Bat token's "return the exiled card" to that card and links the exile to the token, which no step expresses.
- **Item 4.** `ExileTargetPermanentAndTrackWithSourceEffect` (24 cards) is not fused: one exile whose card is linked to the source for its
  "exiled with" ability (CR 607.2a); folding the link into `ExileTargetPermanentEffect` is step 9 work. `ExileSelfAndReturnTransformedEffect`
  (54 cards) is one action except Liliana, Heretical Healer's `thenEffect` rider: the component and the handler's synchronous dispatch are
  gone, the handler records RETURN (the permanent it became, its owner; NONE when the source is gone or has no back face), and Liliana =
  `SequenceEffect.of(ExileSelfAndReturnTransformedEffect(), ConditionalEffect(HappenedThisWay.returned(), CreateTokenEffect.blackZombie(1)))`
  (its ruling: only the first of several triggers makes a Zombie).
- **Behaviour.** Thranduil's Decree, Spelljack and Kheru Spellsnatcher: none. Transcendent Dragon's prompt reads "Transcendent Dragon — Cast X
  without paying its mana cost?". The dying-creature exiles' log lines are `ExileTriggeringCardFromGraveyardEffect`'s; their "does nothing"
  line is gone. **AI:** `InstantCategoryClassifier` sees a `ConditionalReplacementEffect` whose base is a counter as a counterspell (Thranduil's
  Decree stays COUNTERSPELL, `InstantCategoryClassifierTest`); Spelljack now carries `CounterSpellEffect`, which `SpellEvaluator` scores (the old
  record scored 0). Transcendent Dragon was and stays FLASH_CREATURE (its intervening "if" wraps the trigger in a `ConditionalEffect`).
  `@DisplayName`s added to every test method of the Spelljack, Kheru Spellsnatcher, Thranduil's Decree, Brenard, Unscythe and Liliana tests.

**Tests** (per-card runs while converting, then one Gradle run, one `--tests` list per task): the 8 rewritten cards' tests and every card test
constructing one (the helper sweep: Liliana, Defiant Necromancer), Timothar, Hofri, Delay, Gale's Redirection, Curse of Clinging Webs, `--tests-for
MayCastDiscardedCardWithoutPayingManaCostEffect ExileTriggeringCardFromGraveyardEffect ExileDyingCreatureCardAndCreateTokenEffect` (all),
`CounterSpellEffect ExileSelfAndReturnTransformedEffect CreateTokenCopyOfTargetPermanentEffect ConditionalReplacementEffect --max-per-class 8`;
application (`SubjectResolutionServiceTest` +1, `CounterSpellEffectHandlerTest` (hand-built wiring gained `GameQueryService`), `EffectCountRatchetTest`,
`CardImmutabilityArchTest`, `TargetSpecRatchetTest`, `StepResultsTest`, `EffectResolutionServiceTest`, `StackResolutionServiceTest`,
`LibraryChoiceHandlerServiceTest`, `CounterUnlessPaysEffectHandlerTest` and every application test naming `CounterSupport` or a touched handler)
and AI (`InstantCategoryClassifierTest` +1, `TargetPolarityGuardTest`, `AiDecisionEngineTest`, `SpellEvaluatorTest`, `GameDataDeepCopyTest`): 87 classes,
200 result files, 3,779 test cases, all pass (Brenard and Unscythe from the earlier per-card run). Application, AI, engine testFixtures and
card-test `b k l n s t u` test sources compile.

**For later runs.**

- "Counter target spell. If that spell is countered this way, exile it instead … You may cast it [for as long as it remains exiled]" is
  `CounterSpellEffect(EXILE)` + `AllowPlayCardsWithoutPayingManaCostWhileExiledEffect` / `MayCastDiscardedCardWithoutPayingManaCostEffect` over
  `Subjects.cardsThisWay(StepKind.COUNTER_SPELL)`; "exile it" of a dies trigger is `ExileTriggeringCardFromGraveyardEffect` (records EXILE), "a token
  copy of that creature" after it `CreateTokenCopyOfTargetPermanentEffect.ofTriggeringPermanent()`; "if you do" after a transform is
  `HappenedThisWay.returned()`.
- **Next run.** (1) The human's answer on the "counter unless" options above (and the Guile fix in `counterUnlessCounter` whatever the
  answer). (2) Then the largest unaudited fused names: `ForcedCostOrElseEffect` (158 cards; likely a cost-or-else wrapper, audit before
  splitting), `CantAttackUnlessEffect` / `CantBlockUnlessEffect` / `CantAttackOrBlockUnlessEffect` (66 / 28 / 25; static restrictions, probably not
  fused), `SearchLibraryAndOrGraveyardForNamedCardToHandEffect` (21), `PowerBoostForCrewAndSaddleEffect` (20),
  `PreventDamageAndRemovePlusOnePlusOneCountersEffect` (18), `TransformSelfThenEffect` (5; RETURN-like gate now exists).
- **Remaining fused multi-card records** (no ledger row, used by two or more cards, a `Then` / `And` / `IfYouDo` / `Unless` / `OrElse`
  name): 98 (977 card uses); by first word Search 10, Reveal 9, Prevent 7, Each 6, Exile 15, Choose 5, Cant 5, Put 3, Destroy 3, Creatures 3,
  Boost 3, Shuffle 2, Grant 2, Create 2, and 22 first words with one each (Counter 1: `CounterUnlessPaysEffect`).

### Step 8, run 28 (2026-11-20)

The orchestrator's decision on run 27's "counter unless" options, then run 27's named largest unaudited fused names and the
fused families after them. Verdicts (ledger, step `8`): **split 1** (`TransformSelfThenEffect`), **absorbed 1**
(`SearchLibraryAndOrGraveyardForNamedCardToHandEffect`), with their handlers; **survivor 2** (`TransformSelfEffect`,
`SearchLibraryAndOrGraveyardForCardToHandEffect`), **unique 7**, **blocked-other 5**, **blocked-step-model 1**; the three
"counter unless" records re-ledgered. Ratchet 4,705 / 3,934 → 4,703 / 3,932. 26 card files rewritten: the 21 named-search cards by a
deleted scratch script, the five transform cards by hand (read back by eye, imports sorted). Oracle text of every rewritten card
checked with `get_card` (the transform back faces through their front faces, BOT 2, 9, 12, 14, TLA 207) and rulings with
`get_rulings` (and Guile's). CR 118.12, 118.12a, 400.7, 607.2a, 608.2c, 614.1a, 701.27g, 701.28a, 702.24a, 702.30a and 702.34a
checked with `get_rule` (2026-11-20).

- **"Counter unless": option B (the orchestrator's decision, pending the human's override).** The 37 ransom records stay as they are
  (unique for now). **Option C is the plan for step 10:** one `CounterUnlessEffect`-shaped record (the ransom step + the counter)
  replacing every "counter unless" record at once, ward {N} (`CounterUnlessPaysEffect`, 308 cards, its X / dynamic / rider forms)
  included, with the AI and ward detection reading the ransom from it. Step 10 should also take
  `DestroyTargetPermanentUnlessControllerTakesDamageEffect` (an "unless" ransom carrying `RemovalEffect` / `DamageDealingEffect`) into
  that design.
- **The declined-ransom counter (fixed).** `MayPenaltyChoiceHandlerService.counterUnlessCounter` (the declined discard, sacrifice,
  collect-evidence, exile-graveyard, discard-hand, discard-or-pay, poison and blight ransoms) moved the spell to its graveyard itself,
  so Guile's "If a spell or ability you control would counter a spell, instead exile that spell and you may play that card" never
  applied (Guile's ruling: it replaces "counter [a certain spell]"), and neither did flashback's exile (CR 702.34a), nor the countered
  events (`notifyCounteredSpell`: "whenever a spell is countered" triggers, the countered-creature-by-an-opponent record). It now calls
  `CounterSupport.counterSpell(gameData, counterControllerId, description, target)`, the normal counter path, as the ransom's
  controller (the `StackEntry` form delegates to it). The collect-evidence, discard-hand, exile-graveyard and sacrifice handlers now
  pass the ransom's controller to their `PendingMayAbility` (the others did). `GuileTest.declinedDiscardRansomExilesInsteadOfCountering`
  (Guile and Reality Smasher; the opponent declines to discard) fails on the old code: Shock went to the graveyard. The log line is
  the normal path's ("X is countered."; no test asserted the old "(source)" suffix). **Not changed, for a later run:** the ward {N} /
  "unless pays" decline path, `MayPenaltyChoiceHandlerService.counterSpell` (a private copy), applies Guile but still puts a flashback
  spell into its graveyard and emits no countered events; route it through `CounterSupport` too (keeping its `exileIfCountered` and
  not-paid rider).
- **The transform step.** `StepKind.TRANSFORM` (1 when the source transformed or converted, that permanent, its controller), recorded by
  `TransformSelfEffectHandler` (NONE when the source is gone, transformed since the ability was put on the stack, CR 701.27g, can't
  transform, or has no other face), and `HappenedThisWay.transformed()`. "Transform / convert this. If you do / When you do, X" is
  `SequenceEffect.of(new TransformSelfEffect(), new ConditionalEffect(HappenedThisWay.transformed(), X))` (convert follows the transform
  rules, CR 701.28a): Soundwave, Superior Captain's two triggers, Cyclonus, Cybertronian Fighter, Ratchet, Field Medic (the reflexive
  return inside the gate), Megatron, Tyrant and Aang, Master of Elements (a `SequenceEffect` of the four riders inside the gate), the
  optional ones in their `MayEffect`. Behaviour is unchanged (the old handler ran its riders only when the transform happened);
  `StepResultsTest` +2 (Transform).
- **The named search.** `SearchLibraryAndOrGraveyardForNamedCardToHandEffect(name)` was `SearchLibraryAndOrGraveyardForCardToHandEffect`
  with a `CardNamedPredicate` (its handler built exactly that); the 21 planeswalker-deck cards use it directly.
- **Not fused (unique).** `ForcedCostOrElseEffect` (158 cards): the resolution-time cost combinator ("unless" costs, echo, cumulative
  upkeep, about 20 `CostEffect` kinds, the any-player / enchanted-controller / defending-player payers, villainous choice). Its "if you
  do" asks whether the player chose to pay, not what happened (CR 118.12: "regardless of what events actually occurred"), so it is not a
  step + `HappenedThisWay`; it is a wrapper beside `MayEffect`, a step-10 question. `CantAttackUnlessEffect` (66), `CantBlockUnlessEffect`
  (28), `CantAttackOrBlockUnlessEffect` (25): one static restriction with a `Condition` each (merging the three into one restriction
  record is step 10). `PowerBoostForCrewAndSaddleEffect` (20): one crew/saddle power modifier. `PreventDamageAndRemovePlusOnePlusOneCountersEffect`
  (18): a static replacement whose replacing event prevents and removes counters (CR 614.1a), not a resolution, so no step result
  exists for its riders. `ExileTargetPermanentAndImprintEffect` (6): one exile linked to the source (CR 607.2a), as run 27's track-with-source
  exile; step 9.
- **Blocked.** No boost record takes a set `PermanentSubject`: `BoostTargetCreatureAndAllWithSameNameEffect` (3) is a boost over
  `allOf(target(), otherCreaturesSharing(target(), NAME))` once one does (`BoostTargetAndSharingCreaturesUntilEndOfTurnEffect`, 2,
  likely the same). The Crowns (`BoostEnchantedAndSharingCreaturesUntilEndOfTurnEffect`, `GrantKeywordToEnchantedAndSharingCreaturesUntilEndOfTurnEffect`)
  also need the host's last-known creature types once it left: `otherCreaturesSharing` names nothing then, while their handlers still
  give the sharing creatures the bonus. `CreateTokenAndLinkToSourceEffect` (4): the source link and the per-token delayed triggers have
  no step over `createdThisResolution()`. `ExileAnyGraveyardCardThenIfMatchesEffect` (2, blocked-step-model): its resolution-time
  any-graveyard choice reruns the handler and records no EXILE step.
- **Pre-existing failure, not this run's.** `MegatronTyrantTest`'s three back-face attack tests (`attackMaySacrificeArtifactToDamageTargetCreature`,
  `deathtouchMakesOneDamageLethalForDeterminingExcess`, `damageWithoutExcessDoesNotConvertOrDamageController`) answer a sacrifice prompt
  that `SacrificePermanentsEffectHandler` no longer opens for a lone artifact (it sacrifices it; two artifacts open a multi-permanent
  choice), so `handlePermanentChosen` hits the target prompt. Megatron, Destructive Force and the sacrifice handler are untouched here;
  the tests (not the card) need the prompts they now get.

**Tests** (one Gradle run, one `--tests` list per task; six card-test modules hit a compiler out-of-memory and were rerun with two
workers): every rewritten card's test, every card test constructing a rewritten card, Guile or Reality Smasher (the helper sweep),
`--tests-for SearchLibraryAndOrGraveyardForCardToHandEffect`, the eight counter-unless records named above and
`ReplaceControlledCounterWithExileAndPlayEffect` (all), `TransformSelfEffect --max-per-class 25`, `CounterSpellEffect ConditionalEffect
SequenceEffect --max-per-class 6`; application (`StepResultsTest` +2, `EffectCountRatchetTest`, `CardImmutabilityArchTest`, `TargetSpecRatchetTest`,
`CardEffectTargetingConsistencyTest`, `SubjectResolutionServiceTest`, `EffectResolutionServiceTest`, `StackResolutionServiceTest`, the four
counter handler tests, `MayAbilityHandlerServiceTest`, `MayEffectHandlerTest`, `LibraryChoiceHandlerServiceTest`, `ReflexiveRouteRecordTest`,
`FlipCoinWinEffectHandlerTest`, `ResolutionScratchpadAmountTest`, `StepTriggerServiceTest`, `SimulationCopyCompletenessTest`, the trigger
record tests, `LastCounterRemovedTriggerTest` and three handler tests naming `StepKind`) and AI (`TargetPolarityGuardTest`, `GameDataDeepCopyTest`,
`AiDecisionEngineTest`, `SpellEvaluatorTest`): 125 card classes and 31 application / AI classes, 238 result files, 4,482 test cases; all pass but the three
`MegatronTyrantTest` cases above. Application, AI, engine testFixtures and card-test `a b c d e g j l m n r s t v y` test sources compile.

**For later runs.**

- "Transform / convert this. If you do / When you do" is `TransformSelfEffect` + `ConditionalEffect(HappenedThisWay.transformed(), …)`; "a
  card named X" in a library-and/or-graveyard search is `SearchLibraryAndOrGraveyardForCardToHandEffect(new CardNamedPredicate("X"))`. A
  countering effect with no stack entry of its own counters through `CounterSupport.counterSpell(gameData, controllerId, description,
  target)`.
- **Next run.** (1) Route the ward {N} / "unless pays" decline (`MayPenaltyChoiceHandlerService.counterSpell`) through `CounterSupport`
  (flashback exile, countered events), with a test. (2) A set subject on a boost record (`BoostAllCreaturesEffect` or a subject form of
  `BoostTargetCreatureEffect`), then `BoostTargetCreatureAndAllWithSameNameEffect` (3) and `BoostTargetAndSharingCreaturesUntilEndOfTurnEffect`
  (2); the Crowns only with a last-known "sharing" read. (3) A recording resolution-time "you may exile a card from a graveyard" choice,
  then `ExileAnyGraveyardCardThenIfMatchesEffect` (2). (4) The fused families left have almost no step-recording first half: Reveal 9,
  Search 8 and Prevent 6 have no `StepKind` (a REVEAL or SEARCH step would be the prerequisite; decide whether that is step 8 or step 9
  work), Each 6 are mostly player loops. Consider closing step 8 after (1)–(3) and moving the rest to step 9's family sweeps.
- **Remaining fused multi-card records** (no ledger row, used by two or more cards, a `Then` / `And` / `IfYouDo` / `Unless` / `OrElse`
  name): 82 (600 card uses, `CounterUnlessPaysEffect`'s 308 included); by first word Exile 13, Reveal 9, Search 8, Prevent 6, Each 6,
  Choose 5, Put 3, Creatures 3, Shuffle 2, Destroy 2, Cant 2, and 18 first words with one each.

### Step 8, run 29 (2026-11-20)

Run 28's "Next run" items (1), (2) and (3), then the close of step 8 (item 4). Verdicts (ledger, step `8`): **split 1**
(`ExileAnyGraveyardCardThenIfMatchesEffect`), **absorbed 2** (`BoostTargetCreatureAndAllWithSameNameEffect`,
`BoostTargetAndSharingCreaturesUntilEndOfTurnEffect`), with their handlers; **survivor 2** (`BoostTargetCreatureEffect`,
`ExileUpToOneCardFromGraveyardEffect`); and 159 fused multi-card records ledgered without splitting (below). Ratchet 4,703 / 3,932 →
4,700 / 3,929. 7 card files rewritten by hand (Echoing Courage, Echoing Decay, Bile Blight, Wojek Siren, Rally the Righteous, Entrails
Feaster, Deathbonnet Hulk; read back by eye, imports sorted). Oracle text of every rewritten card checked with `get_card` (Deathbonnet
Hulk through Deathbonnet Sprout, MID 181), rulings with `get_rulings` (Echoing Courage's and Bile Blight's: only the target is targeted,
an illegal target means no creature is affected, a face-down creature shares no name; the others have none). CR 118.12, 603.7a, 607.2a,
608.2b, 608.2h, 611.2c, 701.12a, 702.24a, 702.30a and 702.34a checked with `get_rule` (2026-11-20).

- **The ward {N} / "counter unless pays" decline (fixed).** `MayPenaltyChoiceHandlerService.counterSpell` (the declined, or
  unpayable-at-the-prompt, pay-mana and waterbend ransoms) kept a private copy of the counter: it applied Guile but put a flashback
  spell into its graveyard (CR 702.34a: "exile this card instead of putting it anywhere else any time it would leave the stack") and
  emitted none of `notifyCounteredSpell`'s events ("whenever a spell or ability you control counters a spell", "whenever a spell is
  countered", the creature-countered-by-an-opponent record), nor the countered spell's mana-value record. It keeps its counterability
  check, its "cannot be countered" line and the not-paid rider, and now counters through `CounterSupport.counterSpell(gameData,
  controllerId, description, target)` or, for `exileIfCountered` (Syncopate), the new `CounterSupport.counterSpellAndExile(gameData,
  controllerId, description, target)` (the `StackEntry` form delegates to it), as the ransom's controller. The service lost its unused
  `StateTriggerService`. `WaterfallAerialistTest.declinedFlashbackSpellIsExiled` (Firebolt cast with flashback) and
  `.declinedPaymentTriggersCounteredSpellAbilities` (Lullmage Mentor) fail on the old code. The log is now two lines ("P declines to pay
  {2}." and the normal path's "X is countered."); `DisruptivePitmageTest`'s "declines to pay {1}" still holds. Not the step-10
  unification of ransoms and ward.
- **A set subject on the targeted boost.** `BoostTargetCreatureEffect` gained a seventh component, `PermanentSubject subject` (default
  `target()`; the six-argument constructor keeps it; a subject that declares no target is refused: that is `BoostSelfEffect`), and the
  constructor `(PermanentSubject, int, int)`. Any other subject than `target()` boosts every creature the resolver names, found once as
  the effect resolves; each gets its own floating effect, so the set is locked then (CR 611.2c, "the set of objects it affects is
  determined when that continuous effect begins"). It keeps `CreatureBoostEffect` (the AI's pump shape and sign-aware polarity, as both
  absorbed records had). `TextChangeTransformer` carries the subject. Echoing Courage / Echoing Decay / Bile Blight =
  `BoostTargetCreatureEffect(allOf(target(), otherCreaturesSharing(target(), NAME)), p, t)`; Wojek Siren and Rally the Righteous's boost =
  `BoostTargetCreatureEffect(withCreaturesSharingAColor(target()), p, t)`. Behaviour as before (the cards' 50-odd tests, entering-after
  and face-down cases included, pass unchanged); the log is the survivor's per-creature "X gets +N/+N until end of turn." (Wojek Siren's
  "gives +1/+1 to N creature(s)" line is gone, no test asserted it). The same-name records declared a harmful target spec; the survivor's
  is benign. For a spell that changes nothing: spell targeting checks protection on its own (`TargetLegalityService`), and the AI reads
  the boost's sign. The Crowns (`BoostEnchantedAndSharingCreaturesUntilEndOfTurnEffect`, grant twin) stay blocked on a last-known
  "sharing" read (run 28).
- **The recording graveyard exile.** `ExileUpToOneCardFromGraveyardEffect` ("you may exile a card from a graveyard", resolution-time,
  no target; Mechanical Mobster, Kaya, Spirits' Justice) gained a `CardPredicate filter` (`matching(filter)`) and became an EXILE step:
  its handler records NONE, asks (through the generic `resolutionTimeExileThenEffect*` completion), resolves itself again with the answer
  before anything changed (an earlier step by the `architecture/resolution.md` rule) and records the exiled card and its graveyard's
  owner. Its old bespoke completion (the `resolutionTimeExileResume` block, which `ExileMatchingCardFromTargetGraveyardEffect` still
  uses) is no longer reached from it. Entrails Feaster = `SequenceEffect.of(matching(creature card), ConditionalEffect(HappenedThisWay.exiled(),
  +1/+1 counter), ConditionalEffect(NotCondition(HappenedThisWay.exiled()), tap this))`; Deathbonnet Hulk = `SequenceEffect.of(new
  ExileUpToOneCardFromGraveyardEffect(), ConditionalEffect(HappenedThisWay.toACard(EXILE, creature card), +1/+1 counter))`.
  `StepResultsTest` +1 (the chosen card and owner recorded; declining exiles nothing this way). Prompts read "Choose up to one card from a
  graveyard to exile." (Mobster and Kaya's said "target card"); the exile line is "P exiles X from a graveyard." for all four cards.
  `@DisplayName`s added to every Entrails Feaster and Waterfall Aerialist test method.
- **Closing step 8 (item 4).** Every record from `--unaudited` used by two or more cards with a `Then` / `And` / `Unless` / `IfYouDo` /
  `OrElse` name or a `CardEffect` / `List<CardEffect>` component (159, 5,691 card uses; run 28 counted only the 82 names) has a ledger row
  (step `8`, "run 29: …"), none split: **unique 97** (combinators whose nested effect is their payload: the conditionals, reflexive and
  granted abilities, coin flips and die rolls, delayed-trigger registrations, extra combats; cost combinators whose "if you do / unless"
  is the choice to pay, CR 118.12: the `MayPay*` family, echo, cumulative upkeep, punishers, static attack costs, ward {N}; one static
  rule, replacement or shield named with "and": attack restrictions, damage replacements, prevent-and-counter shields, one search over
  two zones or with two destinations, one exchange, CR 701.12a), **blocked-step-model 26**, **blocked-other 21**, **blocked-player-loop 7**
  (the `EachPlayer…` / `EachOpponent…` / `AnyPlayer…` records), and **`step-9` 8**: a new verdict for split- or absorb-ready single-family
  work handed to step 9 (`ExileTriggeringSpellWithSuspendCountersEffect`, `ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect`,
  `ExileAnyGraveyardCardAndImprintOnSourceEffect`, `ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffect`,
  `ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect`, `SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect`,
  `DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect`, `RevealHandAndDiscardMatchingCardsEffect`; each note says what to
  use). Step 9's section now says to take them. No unaudited record matches that filter now.

**Tests** (one Gradle run, one `--tests` list per task, 113 classes; then the three edited classes again): the 7 rewritten cards and every card
test constructing one of them, Mechanical Mobster or Kaya (the helper sweep: Arcbound Hybrid, Darksteel Gargoyle, Lightning Helix, Ornitharch,
Panoptic Mirror, Pristine Angel, Reap What Is Sown, Sanguimancy, Screams from Within, Servant of Tymaret, Siren of the Fanged Coast, Skullclamp,
Thought Dissector, Tomik, Unholy Grotto, Vortex Elemental, Whispersilk Cloak), Waterfall Aerialist (+2), The Unagi of Kyoshi Island, Syncopate,
Power Sink, Disruptive Pitmage, Spell Pierce, Mana Leak, Guile, Lullmage Mentor, Firebolt, Cemetery Desecrator, Surge of Zeal, Homing Lightning,
`--tests-for CounterUnlessPaysEffect --max-per-class 15`, `BoostTargetCreatureEffect --max-per-class 20`, `ExileUpToOneCardFromGraveyardEffect`
and `ExileAnyGraveyardCardAndImprintOnSourceEffect` (all); application (`EffectCountRatchetTest`, `CardImmutabilityArchTest`, `TargetSpecRatchetTest`,
`SimulationCopyCompletenessTest`, `CardEffectTargetingConsistencyTest`, `StepResultsTest` +1, `SubjectResolutionServiceTest`, `TextChangeTransformerTest`,
the graveyard choice handler tests, the four counter handler tests, `EffectResolutionServiceTest`, `StackResolutionServiceTest`, `SpellTriggersTest`,
`StepTriggerServiceTest`, `MayAbilityTargetTimingTest`, `StackEntryTargetGroupsTest`, `DurationClockTest`, `FloatingEffectLifecycleTest`, the
route-record tests, `CombatServiceTest`, the activation tests, `ExileGraveyardCardsEffectHandlerTest`) and AI (`TargetPolarityGuardTest`,
`SpellEvaluatorTest`, `AiDecisionEngineTest`, `InstantCategoryClassifierTest`, `AiTargetSelectorTest`, `GameDataDeepCopyTest`): 243 result files,
4,323 test cases, all pass; the rerun of `StepResultsTest`, `EntrailsFeasterTest` and `WaterfallAerialistTest` passes. Application, AI, engine
testFixtures and card-test `b d e k m r w` test sources compile.

**For later runs.**

- "Target creature and all other creatures with the same name as that creature / each other creature that shares a color with it get
  +X/+Y" is `BoostTargetCreatureEffect(subject, p, t)` over `allOf(target(), otherCreaturesSharing(target(), NAME))` /
  `withCreaturesSharingAColor(target())`. "You may exile a [creature] card from a graveyard. If you do / If a creature card was exiled this
  way" is `ExileUpToOneCardFromGraveyardEffect[.matching(filter)]` + `ConditionalEffect(HappenedThisWay.exiled() / toACard(EXILE, filter), …)`.
  A ward or ransom decline counters through `CounterSupport`, never by moving the card itself.

### Step 8 closing summary (2026-11-20)

**Totals, runs 1 to 29** (each class's latest step-8 ledger row): 478 classes audited: **split 109**, **absorbed 57**, **survivor 44**,
**unique 123**, **blocked-other 85**, **blocked-step-model 41**, **blocked-player-loop 11**, **`step-9` 8**. The ratchet went from 4,858 /
4,091 (before run 1) to 4,700 / 3,929 records / handlers. The largest card rewrites onto `SequenceEffect` steps were
`SacrificePermanentThenEffect`'s 111 cards (run 7), the target "then" wrappers' 106 (runs 2 and 3), `SacrificeSelfThenEffect`'s 62 (run 1)
and `DiscardCardThenEffect`'s 41 (run 22), all with the step-result readers `HappenedThisWay`, `AmountThisWay`, `thisWay` / `playersThisWay` and the card subject
`cardsThisWay` (run 23).

**Why the blocked ones are blocked** (by first word: step-model Reveal 6, Shuffle 4, Put 4, Deal 4, Exile 4, Look 3, Search 3; other Exile 17,
Destroy 12, Return 10, Deal 10, Sacrifice 8, Counter 5, Choose 5; player loop Each 8):

- *No step kind for the first half.* REVEAL (reveal from hand, reveal the top card, reveal until), SEARCH (search then "if it is a …", then
  attach / imprint), LOOK (look at the top N and choose), CAST ("if you cast it this way"), GAIN_CONTROL ("if control changes", the gained
  permanents), shuffle-into-library ("draw that many"), put-from-hand, tap, surveil, behold, collect evidence, forage, SEEK. Each family needs
  its `StepKind` recorded by its primitive (and that primitive on the earlier-step list) before its fused records can split.
- *The first half prompts mid-step* (`rerunCurrentEffectAfterInteraction` after a change, or a bespoke continuation): the look-and-choose,
  hand-choice, put-from-hand and choose-your-own-permanent counter paths, the chosen-permanent exile; Sindbad's draw replacement.
- *No subject for what the second half names:* a card chosen in hand, the cards exiled with the source, a permanent or color or name chosen
  at resolution by a choice step, the triggering spell for `CounterSpellEffect`, the host's last-known creature types (the Crowns), the
  removed target's last-known values for some riders.
- *No delayed trigger bound to a step result:* "return them at the next end step", "when you cast a spell this way", the per-token links of
  `CreateTokenAndLinkToSourceEffect`, Timothar's Bat.
- *No primitive for the second half:* the same-name search of graveyard, hand and library; separate-into-piles over exiled cards.
- *Player loops:* "each player may …, then …" needs the choose-then-act loop that this plan leaves out.

**What step 9 picks up.** The 8 `step-9` ledger rows (each note names the parts to use), then the family sweeps as planned; the recorded
step kinds above are the cheapest prerequisites to add when a sweep meets their family (REVEAL and SEARCH unblock the most records). The
in-resolution `eventValue` writers still move to `AmountThisWay` card by card. The track-with-source / imprint exiles (runs 27, 28, 29) fold
their link into `ExileTargetPermanentEffect` / `ExileUpToOneCardFromGraveyardEffect` there.

**Left for step 10.** (1) Option C (runs 27 and 28): one `CounterUnlessEffect`-shaped record (the ransom step + the counter) replacing
`CounterUnlessPaysEffect` (ward {N}, 308 cards, with its X / dynamic / rider forms) and the 37 ransom records at once, the AI and ward
detection reading the ransom from it; take `DestroyTargetPermanentUnlessControllerTakesDamageEffect` into that design. (2) The cost
combinators beside `MayEffect`: `ForcedCostOrElseEffect`, `MayPayManaEffect`, `MayPayLifeEffect`, `MayPayTapPermanentsEffect`,
`PayPerCounterOrElseEffect`, `DestroyUnlessPaysPerCounterEffect` (CR 118.12: their "if you do" is the choice to pay). (3) The static
restriction merges: `CantAttackUnlessEffect` / `CantBlockUnlessEffect` / `CantAttackOrBlockUnlessEffect` and the three
`…CantAttack…UnlessPredicateEffect` records. (4) The boost head: `BoostSelfEffect`, `BoostTargetCreatureEffect` (now with a subject),
`BoostAllOwnCreaturesEffect`, `BoostAllCreaturesEffect`.

### Step 9, run 1 (2026-11-20)

Family Exile: the five `step-9` Exile hand-offs of step 8 run 29, then the unaudited records used by one or two cards (every
two-card one, and the one-card siblings that share their shape). Ratchet 4,700 / 3,929 → 4,692 / 3,921 records / handlers
(10 records and handlers deleted, 1 added). 21 card files rewritten by hand (diffs read back by eye, imports sorted). Oracle text
checked with `get_card` for every card whose handler did something its name didn't say (the six face-down exiles, Urborg
Scavengers, Doom's Time Platform, Alaundo, The Wedding of River Song); rulings with `get_rulings` (Rotfeaster Maggot, Morbid Bloom,
Scour the Desert, Ritual of the Returned, Urborg Scavengers, Patchwork Crawler, Sinister Concierge, Epochrasite). CR 400.3, 400.7,
607.2a, 608.2b, 608.2h, 609.3, 610.3, 614.1c, 702.62a and 702.62b checked with `get_rule` (2026-11-20).

| Family | Audited | Absorbed | Split | Survivor | Blocked (cause) | Unique | Left unaudited |
|---|---|---|---|---|---|---|---|
| Exile | 40 (39 existing + 1 new step record) | 5 | 4 | 5 | 17 blocked-other: 5 no exile-until step, 4 no random-exile step / timed permission step, 3 graveyard last-known P/T, 2 no hand-choice exile step, 1 permission step for Nita, 1 source link on the triggering-card exile, 1 graveyard-owner subject | 9 | 274 records (238 used by fewer than three cards); `step-9` rows: `ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffect`, `ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect` |

- **Hands are a player subject.** `ExileTargetPlayerHandEffect(PlayerSubject players, boolean trackWithSource)` (old constructors keep
  `targetPlayer()`, the only subject that declares its harmful target; `handsOf(players)` / `handsOfWithSource(players)`) absorbed
  `ExileAllHandsEffect` (Worldfire, Decree of Annihilation: `eachPlayer()`), `ExileAllOpponentsHandsEffect` (Ashiok, Nightmare Weaver:
  `eachOpponent()`, tracked) and `ExileControllerHandWithSourceEffect` (Wormfang Behemoth: `you()`, tracked). Players in APNAP order
  (the all-hands handler used seat order); each card is filed in its owner's exile, as Wormfang's handler did (its tests hand a player
  a card another player owns). Log: the survivor's "P's hand is exiled (N cards)." per player, plus "P's hand is already empty." for an
  empty hand (the all-hands records said nothing); Wormfang's "P exiles N cards from their hand." is gone. No test asserted either.
- **The face-down damaged-player exile is the library-exile primitive.** `ExileTopCardOfDamagedPlayerLibraryFaceDownAndGrantCreatureControllerPlayPermissionEffect`
  (6 cards) = `new ExileTopCardMayPlayThisTurnEffect(1, false).fromLibraryOf(triggeringPlayer()).exiledFaceDown().withAnyManaType().until(PERMANENT)`;
  Edward Kenway without `withAnyManaType()`; Gonti, Night Minister ("its controller looks at … They may play") with
  `.playableBy(controllerOf(triggering()))`; every other card says "you" (Klaw, Gonti Canny Acquisitor, Predators' Hour's granted
  trigger, Edward Kenway, Dream-Thief's Bandana: `forSourceController()` is gone). Two differences, neither in the Oracle text: the
  face-down card is no longer linked to the source permanent (nothing reads that link; the look permission is the exiler id), and any-type
  mana goes only to a nonland card (a land isn't cast). The source-link assertions of the Gonti, Gonti Canny Acquisitor, Dream-Thief's
  Bandana and Predators' Hour tests were dropped, and Bandana's any-type-mana case now exiles a creature card instead of an Island. Log
  lines are the survivor's per-card "P exiles a card face down from the top of their library (…)." line.
- **The Cemetery cycle.** `ExileAnyGraveyardCardAndImprintOnSourceEffect` = `new ExileUpToOneCardFromGraveyardEffect(true).withSource()`
  (new `trackWithSource` component, set by four cards). Its imprint was never read: `CardSharesCardTypeWithImprintedCardPredicate(true)`
  and `ReduceOwnCastCostForEachSharedCardTypeWithExiledCardsEffect` read the cards exiled with the source's id, which the stack entry
  keeps after the source left. New `CemeteryGatekeeperTest` case: a matching spell still deals damage when Gatekeeper left before its
  ability resolved. The three tests that asserted the imprint now assert the source-tracked exile. Prompt and log are the survivor's.
- **A suspend step.** New `PutTimeCountersAndGainSuspendEffect(CardSubject cards, int timeCounters)` (`onCardsExiledThisWay(n)` over
  `cardsThisWay(EXILE)`): each named card in exile, not a token, gets the counters and is suspended (CR 702.62b). It split
  `ExileSourceCardWithSuspendCountersEffect` (Epochrasite, Sinister Concierge: after `ExileSourceCardFromGraveyardEffect`, which also
  leaves a card that left its graveyard and came back, CR 400.7; the old handler had no such guard), `ExileTargetCreatureWithSuspendEffect`
  (Suspend, Sinister Concierge: after `ExileTargetPermanentEffect`) and `ExileTargetNonlandCardFromGraveyardWithSuspendEffect` (Doom's
  Time Platform: after `ExileGraveyardCardsEffect.oneTargetFromControllerGraveyard(nonland)`). Sinister Concierge =
  `SequenceEffect.of(MayEffect(exile source), suspend, ConditionalEffect(SourceCardSuspended, SequenceEffect.of(exile target, suspend)))`;
  a declined "may" leaves no EXILE step, so the first suspend names nothing. Log: the exile step's line, then "X gets N time counters and
  gains suspend.".
- **Graveyard exile, then a counter on this.** `ExileTargetCardFromGraveyardPutCounterOnSourceEffect` (Urborg Scavengers) and Inherited Fiend's
  use of `ExileTargetCreatureCardFromGraveyardPutCounterOnSourceEffect` split into `ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, filter,
  track)` + `PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE)`: an illegal sole target fizzles the whole ability, so no counter (Urborg Scavengers'
  ruling). Patchwork Crawler stays on the record: its handler tags the exile with an ability link so an "exiled with" ability Crawler gains
  from an exiled card (Valki) does not read Crawler's own exiles (CR 607.2a; `PatchworkCrawlerTest`'s Valki case fails without it). An
  ability link on `ExileGraveyardCardsEffect` would be a parameter only one card sets.
- **Rory Williams.** `ExileTriggeringSpellWithSuspendCountersEffect` lost its `followUp` component: the Clue is the ability's second effect,
  which resolves whether or not the spell was still on the stack, as before.
- **Graveyard last-known P/T (a finding beyond the run).** `AmountThisWay.exiledPower()` / `exiledToughness()` read a card exiled from a
  graveyard as it is now, in exile. The rulings of Morbid Bloom ("the toughness of the targeted creature card as it last existed in the
  graveyard"), Scour the Desert ("checks the exiled card's toughness in the graveyard, not what it is in exile") and Ritual of the Returned
  say otherwise (CR 608.2h: the card left the zone the effect expected it in). The three exile-then-token / life records stay on their
  handlers, which read the stats before the exile. Cards already on the amount from a graveyard exile (Soul Separator, The Aesir Escape
  Valhalla, …) differ only for a characteristic-defining P/T that changes as the card leaves the graveyard; worth fixing when the graveyard
  exile steps record last-known stats.

**Tests** (one Gradle run, one `--tests` list per task, then the changed test classes again): the 21 rewritten cards and every card test
constructing one of them (the helper sweep: All of History, All at Once; Amy Pond; Martha Jones; Summoner's Grimoire; Tymaret, the Murder King;
Yixlid Jailer; Heirloom Mirror, whose back face is Inherited Fiend), Patchwork Crawler, Identity Crisis, Hypnox, Taigam, Entrails Feaster,
`--tests-for` `ExileUpToOneCardFromGraveyardEffect`, `ExileTriggeringSpellWithSuspendCountersEffect`, `ExileTargetPlayerHandEffect`,
`PutTimeCountersAndGainSuspendEffect` (all), `ExileTopCardMayPlayThisTurnEffect` and `ExileGraveyardCardsEffect` (`--max-per-class 10`),
`ExileTargetPermanentEffect` and `ExileSourceCardFromGraveyardEffect` (`--max-per-class 6`); application (`EffectCountRatchetTest`,
`TargetSpecRatchetTest`, `CardEffectTargetingConsistencyTest`, `CardImmutabilityArchTest`, `SimulationCopyCompletenessTest`, `StepResultsTest`,
`SubjectResolutionServiceTest`, `ExileGraveyardCardsEffectHandlerTest`, `ExileTargetPermanentEffectHandlerTest`) and AI (`GameDataDeepCopyTest`,
`TargetPolarityGuardTest`): 66 card test classes, 9 application and 2 AI classes; the first run's 11 failures (the source-link, imprint and
Crawler cases above, and Wormfang's owner cases) fixed, then all pass. Application, AI, engine testFixtures and card-test `a c d e g h k p r s
u w` test sources compile.

**For the next run (Exile, run 2).**

- The until-source-leaves subfamily with its `step-9` row: give `ExileTargetPermanentUntilSourceLeavesEffect` (101 cards) a `PermanentSubject`
  and fold `ExileTargetPermanentAndAllWithSameNameUntilSourceLeavesEffect` (`allOf(target(), otherPermanentsSharing(target(), NAME, filter))`,
  Detention Sphere, Deputy of Detention), `ExileTargetCreaturesUntilSourceLeavesEffect`, `ExileTargetCreatureAndAurasUntilSourceLeavesEffect`,
  `ExileTriggeringCreatureUntilSourceLeavesEffect`, `ExileOwnNontokenCreaturesUntilSourceLeavesEffect` and `ExileAllPermanentsUntilSourceLeavesEffect`
  where their handlers agree (read each to the end: the source-already-left branch, CR 610.3, the return-on-leave-trigger flag, tokens).
- The cheapest prerequisites the blocked rows name: a timed play-permission step over a `CardSubject` (duration, any-type mana, free, exile
  instead) generalising `AllowPlayCardsWithoutPayingManaCostWhileExiledEffect` (Nita, Dire Fleet Daredevil, the graveyard may-play records
  `ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect` / `…WhileExiledEffect` / `ExileDiscardedCardFromGraveyardMayPlayUntilNextTurnEffect`);
  then a random-card exile step (the four random records); then an exile-until step recording the hit card (the five exile-until-nonland
  records); graveyard exile steps recording last-known P/T (the three stat records, and Soul Separator's reads).
- `ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect` (`step-9`) still needs "cast the copy if able" and the random single copy on
  `CopyImprintedCardAndMayCastCopyEffect`.

### Step 9, run 2 (2026-11-20)

Family Exile, continued: the graveyard last-known P/T bug run 1 found, the until-source-leaves subfamily with its `step-9` row
(Detention Sphere, Deputy of Detention), then the cheapest prerequisite run 1's blocked rows named (a timed play-permission step over a
`CardSubject`) and the records it splits. Ratchet 4,692 / 3,921 → 4,683 / 3,912 records / handlers (9 records and handlers deleted, one
renamed into the new step record). 19 card files rewritten (11 by a scratch script, 8 by hand; diffs read back by eye). Oracle text
checked with `get_card` (Victor Mancha, Thor, Rooting Moloch, Savvy Trader, Dire Fleet Daredevil, Moonstone, Cryptic Pursuit); rulings
with `get_rulings` (Glorious Protector, Detention Sphere, Deputy of Detention, Lumbering Battlement, Venom); CR 107.1b, 400.7, 604.3,
608.2h, 610.3, 610.3b, 610.3c and 610.3d checked with `get_rule` (2026-11-20).

| Family | Audited | Absorbed | Split | Survivor | Blocked (cause) | Unique | Left unaudited |
|---|---|---|---|---|---|---|---|
| Exile | 22 (20 classes + 2 survivors re-ledgered) | 3 | 7 | 5 (one new step record) | 7 blocked-other: 3 delayed trigger bound to an exiled card (Portcullis, Lagrella, Mysterious Limousine's "return each other card"), 1 permission held by each card's owner (Suspend Aggression), 1 filter on the triggering-card exile (Cryptic Pursuit), 1 cast-only permission + next-end-step return (Mnemonic Betrayal), 1 random-card exile step (re-noted) | 0 | 260 records (227 used by fewer than three cards); `step-9` row left: `ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect` |

- **Graveyard last-known stats (rules fix).** A graveyard exile step now records each card as it last existed in the graveyard
  (`StepOutcome.lastKnownCards`, `LastKnownCardStats`, read by `GameQueryService.asInGraveyard` before any of the cards leave; a sweep reads
  every graveyard first) on every recording path: each `ExileGraveyardCardsEffect` branch, the graveyard choice's exile
  (`GraveyardChoiceHandlerService`, each card before its own removal), `ExileCardsFromGraveyardEffect`, `ExileUpToOneCardFromGraveyardEffect`,
  `ExileTriggeringCardFromGraveyardEffect`, `ExileSourceCardFromGraveyardEffect`. `AmountThisWay.exiled*` read it (CR 608.2h: the card left the
  zone the effect expected it in; Morbid Bloom's, Scour the Desert's and Ritual of the Returned's rulings). A reflexive "when you do" ability
  reads the card as it is in exile, where that ability expects it (`ResolutionResults.asReadByLaterAbility()`, applied by
  `StackEntry.carryResolutionResultsFrom`; `VenomDeadlyDevourerTest`'s Tarmogoyf case holds it). New `SoulSeparatorTest` case: a Boneyard Wurm
  exiled beside a Grizzly Bears makes a 2/2 Zombie (1/1 on the old code). Library and hand exiles still read the card in exile.
- **A set P/T keeps a negative value.** `AmountThisWay` totals are at least 0 (CR 107.1b) unless `.keepingNegative()`, for an effect that sets
  a power or toughness to it: Ritual of the Returned and Soul Separator's Zombie (new `SoulSeparatorTest` case: Char-Rumbler makes a -1/3
  Zombie; it made a 0/3 one). Two cards set it.
- **The three stat records split.** Morbid Bloom = `ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, creature, false)` +
  `CreateTokenEffect(...).withAmount(exiledToughness())`; Scour the Desert the same over `oneTargetFromControllerGraveyard(creature)`; Ritual of
  the Returned = `oneTargetFromControllerGraveyard(creature)` + `CreateTokenEffect("Zombie", exiledPower().keepingNegative(),
  exiledToughness().keepingNegative(), …)`; Rotfeaster Maggot = `exactTargetedFromAnyGraveyard(1, creature, false)` +
  `GainLifeEffect(exiledToughness())`. The spells declare the target through the effect only: a card-level `target(filter)` bound to
  `ExileGraveyardCardsEffect` makes the single-UUID cast path refuse the graveyard target ("Invalid target"). Their CDA tests (Lord of
  Extinction, Mortivore, Consuming Aberration) pass unchanged; logs are the exile step's.
- **Until-source-leaves takes a subject.** `ExileTargetPermanentUntilSourceLeavesEffect` gained `PermanentSubject permanents` (default
  `target()`; it must declare the target), resolved by `SubjectResolutionService` as the effect applies (CR 608.2h). Detention Sphere =
  `allOf(target(), otherPermanentsSharing(target(), NAME, any))`, `exileIfSourceAlreadyLeft` and `returnsOnSourceLeavesTrigger` (its ruling:
  if Sphere left first, the exile is indefinite); Deputy of Detention = `allOf(target(), except(otherPermanentsSharing(target(), NAME,
  nonland), controlledBy(opponentsOf(controllerOf(target())), any)))`, neither flag (its ruling: nothing is exiled once Deputy left). Gained
  from the survivor's handler: the melded-card returns, the LKI power, the skip of a permanent that a replacement kept on the battlefield;
  the same-name handler's fallback to a source found by card identity is gone (CR 400.7). `PermanentRemovalService` merges the returns of
  any survivor that returns through its leaves ability (CR 610.3: one one-shot effect returns them all). Lumbering Battlement's record is
  `ExileAnyNumberOfPermanentsUntilSourceLeavesEffect(other nontoken creature you control)`, and that record's cards now return together:
  Glorious Protector's returned one at a time (new `GloriousProtectorTest` case: two Soul Wardens see each other enter; fails on the old
  return path). The prompt text is the survivor's.
- **A timed permission step.** `AllowPlayCardsWithoutPayingManaCostWhileExiledEffect` became `AllowPlayCardsFromExileEffect(CardSubject,
  Duration, freeCast, anyManaType, exileInsteadOfGraveyard)` with `whileExiled` / `thisTurn` / `untilEndOfYourNextTurn` /
  `whileYouControlSource` and `.withoutPayingManaCost()` / `.withAnyManaType()` / `.exilingInsteadOfGraveyard()`. It split Thor, Rooting
  Moloch, Practiced Scrollsmith, Victor Mancha (`oneTargetFromControllerGraveyardWithSource(null)` + `whileYouControlSource`: granted only while
  the controller controls Victor), Savvy Trader, Nita and Dire Fleet Daredevil (`oneTargetFromOpponentGraveyard(instant or sorcery)` +
  `thisTurn(...).withAnyManaType().exilingInsteadOfGraveyard()`), Moonstone (`MayEffect(SequenceEffect.of(ExileTriggeringCardFromGraveyardEffect,
  untilEndOfYourNextTurn(...)))`). Any-type mana now goes to the permission holder for the duration (Nita's had no holder). The logs are the
  exile steps' lines without the permission note; no test asserted them.

**Tests** (one Gradle run, one `--tests` list per task, counted by XML file name, then the changed classes again): the 19 rewritten cards,
Glorious Protector, the helper sweep (Storm the Vault; AI `AiTargetSelectorTest`, `InstantCategoryClassifierTest`), every card reading
`AmountThisWay.exiled*`, the leave-trigger survivor cards (Oblivion Ring, Fiend Hunter, Journey to Nowhere, Icy Prison, Leonin Relic-Warder,
Faceless Devourer, Parallax Wave and Tide), Portcullis, Mysterious Limousine, Urborg Scavengers, Patchwork Crawler, `--tests-for`
`ExileTargetPermanentUntilSourceLeavesEffect` (20 per class), `ExileGraveyardCardsEffect` (15), the four other graveyard exile steps (6),
`ExileAnyNumberOfPermanentsUntilSourceLeavesEffect` and `AllowPlayCardsFromExileEffect` (all): 104 card result files, 899 tests; 6 failures
(Scour the Desert's bound target, Venom's reflexive read) fixed, then those classes pass. Application (`EffectCountRatchetTest`,
`TargetSpecRatchetTest`, `CardEffectTargetingConsistencyTest`, `CardImmutabilityArchTest`, `SimulationCopyCompletenessTest`, `StepResultsTest`,
`SubjectResolutionServiceTest`, the three graveyard / until-source-leaves handler tests (`ExileTargetPermanentUntilSourceLeavesEffectHandlerTest`'s
hand-built handler takes the resolver), `TriggerTargetCollectorTest`, `ETBTokenTargetServiceTest`, the graveyard-choice and multi-selection
interaction tests): 234 tests; AI (`GameDataDeepCopyTest`, `TargetPolarityGuardTest`, the two above): 200 tests; all pass. Application, AI,
engine testFixtures and card-test `d g k l m n p r s t v` test sources compile.

**For the next run (Exile, run 3).**

- The random-card exile step (`ExileRandomCardFromGraveyardMayPlayThisTurnEffect`, `…FromHand…` with Elkin Lair's delayed rider,
  `…FromLibrary…`, `…FromDamagedPlayerLibrary…`): with it, each splits into the step + `AllowPlayCardsFromExileEffect`.
- `AllowPlayCardsFromExileEffect` riders the blocked rows name: a holder `PlayerSubject` (Suspend Aggression: `ownerOf`), cast-only (Mnemonic
  Betrayal); then the delayed "put the cards still exiled into their graveyards at the next end step" over `cardsThisWay(EXILE)`.
- The exile-until step recording the card it stops on (the five exile-until-nonland records), then `ExileGraveyardInstantsOrSorceriesAndCastCopiesEffect`
  (`step-9`: "cast the copy if able" and the random single copy on `CopyImprintedCardAndMayCastCopyEffect`).
- Then the rest of the 227 tail records, two-card ones first.

### Step 9, run 3 (2026-11-20)

Family Exile, continued: the random-card exile step and the exile-until step run 2 named, with the records they split; the
`AllowPlayCardsFromExileEffect` rider rows (Suspend Aggression's owner as holder, Mnemonic Betrayal's cast-only) checked against the
guard rail; the cast-copies `step-9` row; the rest of the random and exile-until records and the top-card permission records used by
fewer than three cards. Ratchet 4,683 / 3,912 → 4,670 / 3,899 records / handlers (15 records and handlers deleted, 2 added). 21 card
files rewritten by hand (diffs read back by eye). Oracle text checked with `get_card` for every card rewritten or ledgered (Tersa
Lightshatter, Advanced Reconstruction, Elkin Lair, Tempest Trapper, Rahilda, Kamachal, Radiant Scrollwielder, Wondrous Crucible, Sin,
Fear of Ridicule, Capricious Hellraiser, Power Pack, Suspend Aggression, Mnemonic Betrayal, the exile-until cards, Umbris, Bismuth
Mindrender, Krang & Shredder, The Day of the Doctor, Solstice Revelations, Flameshape, Possibility Technician, Lightning, Voltaic
Visionary); no ruling was needed beyond the Oracle text. CR 400.7 and 608.2h checked with `get_rule` (2026-11-20).

| Family | Audited | Absorbed | Split | Survivor | Blocked (cause) | Unique | Left unaudited |
|---|---|---|---|---|---|---|---|
| Exile | 42 (40 classes, 2 of them re-ledgered rider rows, + 2 new step records) | 2 | 15 | 2 (both new step records) | 17 blocked-other: 4 a permission rider only one card would set (Rahilda's play condition, Elkin Lair's / Suspend Aggression's holder, Mnemonic Betrayal's cast-only and end-step return), 3 no step for "if you don't cast it, put it into your hand" / "onto the battlefield or into your hand" over a card subject (Breaching Dragonstorm and Bre, Solstice Revelations, Ketria and Illuna), 3 a source-linked library exile (Krang & Shredder, Voltaic Visionary, Pick Up the Pace), 1 each: Storm of Memories' exile-instead on the cast-now step, Power Pack's random target and next-upkeep cast, Capricious Hellraiser's choose-among, Planeswalker's Mischief's reveal, Auspicious Starrix's count-until and battlefield step, Bismuth Mindrender's cast-now-for-life, The Day of the Doctor's bottom-random rest, the cast-copies `step-9` row | 7 | 233 records (200 used by fewer than three cards); no `step-9` Exile row left |

- **A random-card exile step.** New `ExileCardAtRandomEffect(Zone zone, PlayerSubject player, CardPredicate filter)` (`fromGraveyard` /
  `fromLibrary`; the player is named without a target, in APNAP order; the filter is judged with the trigger's event value, so
  Kamachal's "mana value equal to the damage dealt" is `CardManaValueEqualsEventValuePredicate`; a graveyard card is recorded with its
  last-known stats as run 2's graveyard steps are, CR 608.2h). It records EXILE and never prompts. Split onto it: Tersa Lightshatter and
  Advanced Reconstruction (+ `AllowPlayCardsFromExileEffect.thisTurn`), Tempest Trapper (`.withoutPayingManaCost()`), Kamachal, Radiant
  Scrollwielder (instant or sorcery, `.exilingInsteadOfGraveyard()`), Wondrous Crucible (+ `CopyImprintedCardAndMayCastCopyEffect.of`: the
  copy is now cast as part of the resolution, as run 24's copy cards are, not through a parked may-ability) and Fear of Ridicule (+
  `CreateTokenCopyOfCardEffect.of(cardsThisWay(EXILE), options)`). A hand zone was not added: its two users (Elkin Lair, Planeswalker's
  Mischief) stay blocked for other reasons.
- **An exile-until step.** New `ExileCardsFromTopUntilEffect(PlayerSubject library, CardPredicate until)` (`untilNonland(library)`,
  `NONLAND`; `targetPlayer()` declares the harmful player target the old records declared): each card is its own zone change, every
  exiled card is recorded on the EXILE step. "That card" is the new filtered card subject `cardsThisWay(EXILE, filter)` (a
  `CardSubject.CardsThisWay` component, resolved by `SubjectResolutionService` on the cards as they are now): only the card that matched,
  since no earlier card did. Split onto it: Territorial Bruntar (`thisTurn`; new `TerritorialBruntarTest` case: a land exiled before the
  nonland card gets no permission), Stolen Goods and Nicol Bolas +2 (`thisTurn(...).withoutPayingManaCost()`), Kang Prime and The Tenth
  Doctor (`PutTimeCountersAndGainSuspendEffect`), Wanda's Vision, Nico Minoru and No Secret Is Hidden from Me
  (`MayCastDiscardedCardWithoutPayingManaCostEffect(cardsThisWay(EXILE))`, which skips land cards; No Secret's spell is now flat effects
  plus the repeat `ConditionalEffect(..., SequenceEffect.of(exile, cast))`), Undying Flames (`DealDamageToAnyTargetEffect(AmountThisWay
  .exiledManaValue())`: the lands before the nonland card have mana value 0). Umbris's until-land record is absorbed
  (`new ExileCardsFromTopUntilEffect(targetPlayer(), land)`). The AI's `TargetPolarityClassifier` entry for Undying Flames' record is gone
  (its damage effect is classified already).
- **Top-card permissions.** `ExileTopCardMayPlayWhileControllingSubtypeEffect` (Flameshape, Possibility Technician) is
  `ExileTopCardMayPlayThisTurnEffect(n, false)[.exiledFaceDown()].until(PERMANENT).playableOnlyIf(ControlsPermanent(subtype))`; Flameshape's
  controller may now look at its face-down cards ("Look at the top two cards of your library and exile them face down"; the old handler
  granted no look permission). Lightning, Security Sergeant = `ExileTopCardOfOwnLibraryEffect.of(you())` +
  `AllowPlayCardsFromExileEffect.whileYouControlSource(cardsThisWay(EXILE))` (the exile's source link, which nothing read, is gone).
- **The `AllowPlayCardsFromExileEffect` riders were not added.** A holder subject has two users with two meanings (Elkin Lair's "that
  player", Suspend Aggression's "its owner" per card); cast-only is needed only by Mnemonic Betrayal once the exile-until cards name the
  nonland card with the filtered subject; a play condition only by Rahilda (Pick Up the Pace shares it but also needs a source-linked
  exile); the next-end-step "put the cards still exiled into their graveyards" by Mnemonic Betrayal and Elkin Lair, both still blocked.
  Each stays a rider only one card would set (guard rail).
- **The cast-copies `step-9` row** stays: "cast the copy if able" with the order choice (Spelltwine, CR 608.2g is in its handler) and the
  random single copy (Mysterious Stranger) are one card each; Arcane Proxy alone would split.
- Logs: the new steps' lines ("P exiles X at random from their graveyard.", "P exiles N card(s) from the top of their library until X.")
  replace the fused records' lines and play notes; the may-cast prompts are `MayCastDiscardedCardWithoutPayingManaCostEffect`'s. No test
  asserted the old lines.

**Tests** (one Gradle run, one `--tests` list per task, counted by XML file name): the 21 rewritten cards and every card test
constructing one of them (the helper sweep: Chandra's Defeat, Clara Oswald, Dark Intimations, Fervent Paincaster, Gallifrey Stands,
Inferno Jet, Liliana's Defeat, Manticore Eternal, Neheb the Eternal, Open Fire, Overwhelming Splendor, Samut the Tested, The Day of the
Doctor, The Eleventh Hour, The Five Doctors, The Girl in the Fireplace, Time Beetle, Time Lord Regeneration, Trenzalore Clocktower, Twice
Upon a Time, Wall of Forgotten Pharaohs, Warmaker Gunship, Zealot of the God-Pharaoh), `--tests-for` `AllowPlayCardsFromExileEffect`,
`MayCastDiscardedCardWithoutPayingManaCostEffect`, `PutTimeCountersAndGainSuspendEffect`, `CopyImprintedCardAndMayCastCopyEffect`,
`CreateTokenCopyOfCardEffect` (all), `ExileTopCardMayPlayThisTurnEffect` and `ExileTopCardOfOwnLibraryEffect` (`--max-per-class 8`):
115 card test classes, 1,102 tests; application (`EffectCountRatchetTest`, `TargetSpecRatchetTest`, `CardEffectTargetingConsistencyTest`,
`CardImmutabilityArchTest`, `SimulationCopyCompletenessTest`, `StepResultsTest`, `SubjectResolutionServiceTest`): 171 tests; AI
(`GameDataDeepCopyTest`, `TargetPolarityGuardTest`): 65 tests; all pass on the first run. Application, AI, engine testFixtures and
card-test `a f g k l n p r s t u w` test sources compile.

**For the next run (Exile, run 4).**

- The shared blockers this run counted, cheapest first: a "put the cards still in exile into your hand" step over a `CardSubject`
  (Breaching Dragonstorm, Bre, Solstice Revelations: after `MayCastDiscardedCardWithoutPayingManaCostEffect` the cast card has left exile,
  so "if you don't cast it, put it into your hand" is that step; Solstice also needs a "less than the number of Mountains" mana-value
  filter, Bre one read from a `DynamicAmount`); a "put onto the battlefield or into your hand" choice step (Ketria, Illuna); a source link
  on `ExileTopCardOfOwnLibraryEffect` without the imprint (Voltaic Visionary, Pick Up the Pace, and Krang & Shredder on
  `ExileCardsFromTopUntilEffect`).
- Then the rest of the 200 tail records, two-card ones first (`python3 -I scripts/effect-inventory.py --unaudited --family Exile`): the
  token-copy exiles (`ExileTargetPermanentAndCreateTokenCopy*ForEachOtherPlayer*`), the "instead of dying" replacement records, the
  target-hand face-down exiles with a delayed return, and the remaining top-of-library "and" records.

### Step 9, run 4 (2026-11-21)

Family Exile, continued: the three shared blockers run 3 named (a put-into-hand step over a `CardSubject`, a "put onto the
battlefield or into your hand" choice, a source link on the library exile), then the one-card tail (`--unaudited --family Exile`
listed no two-card record any more), cheapest compositions first. Ratchet 4,670 / 3,899 → 4,652 / 3,879 records / handlers (19 records
and 21 handlers deleted, 1 record and handler added). 22 card files rewritten by hand (diffs read back by eye). Oracle text checked with
`get_card` (Breaching Dragonstorm, Bre of Clan Stoutarm, Solstice Revelations, Ketria, Illuna, Voltaic Visionary, Pick Up the Pace,
Rahilda, Norin, Mazzy, Release to the Wind, Suspend Aggression, Bruse Tarl); rulings with `get_rulings` (Breaching Dragonstorm, Bre,
Solstice Revelations, Illuna). CR 101.4, 110.2, 303.4f, 303.4g, 400.3, 400.7 and 608.2h checked with `get_rule` (2026-11-21).

| Family | Audited | Absorbed | Split | Survivor | Blocked (cause) | Unique | Left unaudited |
|---|---|---|---|---|---|---|---|
| Exile | 51 (44 classes + 7 survivors re-ledgered, one a new step record) | 3 | 16 | 7 (one new step record) | 14 blocked-other: 3 no card subject for a card chosen in hand / a cost's sacrificed cards / X targets in one player's graveyard (Dreams of Steel and Oil, Sword of the Ages, Suffer the Past), 2 a delayed trigger or completion flag (Three Wishes' next-upkeep cleanup, God-Pharaoh's Gift's `GraveyardTargetOperation` resume shared with Grixis Sojourners), 2 a source link on a primitive only one card sets (Krang & Shredder on `ExileCardsFromTopUntilEffect`, Prowl on `ExileTargetPermanentEffect`), 2 a suspend-step option (counters equal to mana value, only cards that have suspend), 1 each: Jace's benign player target, Thought Distortion's hand filter, Audacious Swap's shuffle-then rider run as the owner, Etali's chosen cast order, Extract Power's free face-down permission | 11 | 198 records (165 used by fewer than three cards); no `step-9` Exile row left |

- **"If you don't, put that card into your hand" is a step.** New `PutCardsIntoHandEffect(CardSubject cards)`: each named card still in
  exile goes to its owner's hand (CR 400.3). After `MayCastDiscardedCardWithoutPayingManaCostEffect` a cast card has left exile (CR 400.7),
  so the step names only a declined, ineligible or uncastable card: Breaching Dragonstorm = `SequenceEffect.of(ExileCardsFromTopUntilEffect
  .untilNonland(you()), MayCast…(cardsThisWay(EXILE, NONLAND), new CardMaxManaValuePredicate(8)), PutCardsIntoHandEffect(same))`. The cast
  step parks the resolution on its may prompt and the hand step runs after the answer and the cast (Breaching Dragonstorm's split-card and
  no-legal-target cases and Solstice's new uncastable-card case hold it).
- **A mana-value cap read from an amount.** New card predicate `CardManaValueAtMostAmountPredicate(DynamicAmount)`, bound once as the
  cast-now step resolves (`AmountEvaluationService.bindAmounts`, CR 608.2h; unbound it matches nothing): Bre's "less than or equal to the
  amount of life you gained this turn" (`LifeGainedThisTurn(CONTROLLER)`), Solstice's "less than the number of Mountains you control"
  (`Sum(PermanentCount(Mountain, CONTROLLER), Fixed(-1))`). Solstice's offer is now the may-cast prompt instead of a library-search
  choice; its test moved to `handleMayAbilityChosen`. Like the old handlers, the cap is judged on the card's own mana value.
- **"Put that card onto the battlefield or into your hand."** `ReturnAllCardsExiledWithSourceEffect` gained a `CardSubject cards`
  component (`putOntoBattlefield(cards)`, under your control): the named exiled cards enter together, with that handler's one-Aura choice
  and its "an Aura with nothing to enchant stays in exile" (CR 303.4f, 303.4g; Illuna's ruling). Ketria and Illuna =
  `ConditionalEffect(HappenedThisWay.toACard(EXILE, nonland permanent), MayEffect(putOntoBattlefield(cardsThisWay(EXILE, nonland permanent)),
  prompt, PutCardsIntoHandEffect(same)))` (the condition keeps the old "no prompt when nothing was found"); Tezzeret's −8 =
  `ExileTopCardOfOwnLibraryEffect.of(you(), 10)` + `putOntoBattlefield(cardsThisWay(EXILE, artifact))`. Log: "P puts X onto the battlefield."
- **Source link without imprint.** `ExileTopCardOfOwnLibraryEffect.linkedToSource(player)` (Voltaic Visionary's "exiled with this creature",
  Pick Up the Pace's "cards exiled with this enchantment"); `trackWithSource` still links and imprints. Krang & Shredder stays: the same
  link on `ExileCardsFromTopUntilEffect` would have one user.
- **Two permission riders, two users each.** `AllowPlayCardsFromExileEffect.playableOnlyIf(Condition)` (Pick Up the Pace; Rahilda, whose
  random exile is now `ExileCardAtRandomEffect.fromLibrary(triggeringPlayer(), NONLAND)`) and `.playableByOwner()` (each card's owner holds
  the permission, "their next turn" being the owner's: Suspend Aggression, Release to the Wind, both now `ExileTargetPermanentEffect` + the
  permission). Run 3's guard-rail reading still holds for Elkin Lair's "that player" and Mnemonic Betrayal's end-step return.
- **The hand exiles take a player subject.** `EachOpponentExilesFromHandEffect` gained `PlayerSubject players` (`byPlayers(eachPlayer(), 2)`
  Mind Swords, `byPlayers(you(), 1)` Ego Drain and Azor's Gateway, whose card is exiled with the source as the old record did). The players
  are asked in the resolver's APNAP order (CR 101.4); the old loops put the active player first and then the raw seat order, which differs
  only with three or more players. Step 5m's blocked-player-loop note on the family stands: each chooser's card is exiled as they answer.
- **Rules fix in the targeted graveyard exile.** `ExileGraveyardCardsEffect`'s any-graveyard / controller-graveyard target path exiled a card
  that had left its graveyard and come back since it was targeted (CR 400.7: a new object); it now checks the target's recorded graveyard
  incarnation (`targetGraveyardEntryVersion`, the per-card and mixed-zone versions), as Funeral Pyre's old handler did.
  `FuneralPyreTest`'s leave-and-return case fails on the old survivor. Morbid Bloom, Scour the Desert, Ritual of the Returned and the other
  users of that path change the same way.
- **Smaller splits.** Norin (`MayEffect(SequenceEffect.of(ExileTargetPermanentEffect.ofTriggeringPermanent(), thisTurn))`), Mazzy
  (`ExileTriggeringCardFromGraveyardEffect` + `untilEndOfYourNextTurn`), Mind's Dilation (`ExileTopCardOfOwnLibraryEffect.of(triggeringPlayer())`
  + the cast-now step), Funeral Pyre and Pharika (`exactTargetedFromAnyGraveyard` + `CreateTokenEffect.forController(playersThisWay(EXILE))`),
  Dead Man's Chest (`ExileTopCardOfOwnLibraryEffect.of(ownerOf(triggering()), TriggeringPermanentPower)` + `whileExiled(cardsThisWay(EXILE,
  NONLAND)).withAnyManaType()`, "cast spells from among them" being the nonland cards), Bruse Tarl (library exile +
  `ConditionalEffect(HappenedThisWay.toACard(EXILE, land), token)` + `untilEndOfYourNextTurn(cardsThisWay(EXILE, NONLAND))`).
- **Unread links and impossible states dropped.** Rahilda's and Dead Man's Chest's exiles are no longer linked to their source (nothing
  read the link; `RahildaWantedCutthroatTest` dropped its source-link assertion). Two `FuneralPyreTest` cases put a card into another
  player's graveyard than its owner's, which the rules never allow (CR 400.3); the token now goes to the graveyard's owner, so the cases
  were removed. The AI's `TargetPolarityClassifier` entry for Suspend Aggression's record is gone (`ExileTargetPermanentEffect` is a
  `RemovalEffect`). Logs are the steps' lines; no test asserted the old ones.

**Tests** (one Gradle run, one `--tests` list per task, counted by XML file name, then the changed classes again): the 22 rewritten cards'
tests and every card test constructing one of them (the helper sweep: Cabal Therapy, Forcemage / Nullmage / Pulsemage Advocate, Hero of
Bretagard, Luminollusk, Stitch Together, Wormfang Crab), `--tests-for` `ReturnAllCardsExiledWithSourceEffect`, `AllowPlayCardsFromExileEffect`,
`MayCastDiscardedCardWithoutPayingManaCostEffect`, `EachOpponentExilesFromHandEffect`, `PutCardsIntoHandEffect` (all) and
`ExileTopCardOfOwnLibraryEffect`, `ExileGraveyardCardsEffect`, `ExileTargetPermanentEffect`, `ExileTriggeringCardFromGraveyardEffect`,
`ExileCardsFromTopUntilEffect`, `ExileCardAtRandomEffect` (`--max-per-class 8`): 120 card test classes, 1,106 tests, 3 failures (the Funeral
Pyre cases above), fixed; then every `ExileGraveyardCardsEffect` card test after the graveyard fix: 248 classes, 2,175 tests, all pass.
Application (`EffectCountRatchetTest`, `TargetSpecRatchetTest`, `CardEffectTargetingConsistencyTest`, `CardImmutabilityArchTest`,
`SimulationCopyCompletenessTest`, `StepResultsTest`, `SubjectResolutionServiceTest`, `ExileGraveyardCardsEffectHandlerTest`,
`PredicateEvaluationServiceTest`, `MayEffectHandlerTest`, `ReturnCardFromGraveyardEffectHandlerTest`, the trigger, targeting and activation
tests naming a changed record, `InputHandlerEpilogueRatchetTest`): 1,095 tests; AI (`GameDataDeepCopyTest`, `TargetPolarityGuardTest`,
`AiTargetSelectorTest`): 193 tests; all pass. Application, AI, engine testFixtures and card-test `a b d e f i k m n p r s t v` test sources
compile.

**For the next run (Exile, run 5).**

- The cheapest shared blockers this run counted: a source link on `ExileTargetPermanentEffect` (Prowl; the track-with-source / imprint
  records of step 8 runs 27 to 29), a delayed "put the cards still exiled into their owners' graveyards" over `cardsThisWay(EXILE)` with a
  timing (Three Wishes at the next upkeep, Elkin Lair and Mnemonic Betrayal at the next end step), a suspend step whose counters are each
  card's mana value or that skips cards without suspend (The Parting of the Ways, Ecstatic Beauty).
- God-Pharaoh's Gift with Grixis Sojourners: drop `GraveyardTargetOperation.resolutionTimeExile*Resume` for
  `ExileUpToOneCardFromGraveyardEffect.matching` (+ `CreateTokenCopyOfCardEffect.of(cardsThisWay(EXILE), 4/4 black Zombie, haste)`).
- Then the rest of the 165 one-card tail records: the "instead of dying" replacement records, the token-copy exiles
  (`ExileTargetPermanentAndCreateTokenCopy*ForEachOtherPlayer*`), the target-hand face-down exiles with a delayed return, the graveyard
  exiles with counters (memory, discovery, brain, croak).

### Step 5, run 5m, continued (2026-11-22, partly done)

Re-measured: 122 sibling groups held 247 unaudited classes. Every group of three or more is done, and the three Grant / GainControl
pairs that share a survivor with one of them (`GrantEffectTo*`, `GrantEffectTo*UntilEndOfCombat`, `*GainsControlOf*UntilEndOfTurn`).
94 groups / 174 classes are left, all pairs. Ratchet 4,652 / 3,879 → 4,624 / 3,853 records / handlers (28 records, 26 handlers deleted).
84 card files rewritten (a deleted scratch script; every changed line read back by eye).

| Audited | Absorbed | Survivor | Blocked-other | Unique | `step-9` | Left |
|---|---|---|---|---|---|---|
| 79 ledger rows (73 classes without one before, 6 re-ledgered) | 28 | 14 | 21 | 13 | 3 | 94 pairs, 174 classes |

**Folds** (survivor ← absorbed).

- `GrantEffectToTargetUntilEndOfTurnEffect(ability, …, PermanentSubject subject)` ← the own-creatures and source grants and the four
  `GrantStaticEffectTo{Target,Source,OwnCreatures,AllCreatures}UntilEndOfTurnEffect`: `StaticAbility.of(effect)` makes the same floating
  `GrantEffectEffect`. Step 8 run 29 had ledgered three of those `unique` as "not fused"; as subject siblings they fold.
- `GrantEffectToTargetEffect(ability, PERMANENT, false, source())` ← `GrantEffectToSourceEffect`; `GrantEffectToTargetUntilEndOfCombatEffect(ability,
  set)` ← the own-creatures record (its granted ability now also names the granter, which Love on the Battlefield's doesn't read).
- `TargetPlayerGainsControlOfSourceCreatureEffect(PlayerSubject player, then, duration, PermanentSubject permanent)` ← the target permanent's
  controller, the activating player, the attached permanent and the two until-end-of-turn records. The non-targeting form stays
  `targetPlayerDeclaredElsewhere()` (the player a trigger bakes into the entry's target), as the old flag read it. Its validator asks for a
  player target only when the subject declares one (Starke of Rath's target is a permanent).
- `TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect` ← source, enchanted (`retainOtherTypes` for Mistform Sliver). The source record had
  more cards (9 to 6); the target record already carried the subject, so it is the one kept.
- `OpponentsCantCastSpellsThisTurnEffect(players)` ← defending player, every player; `PlayersCantGainLifeThisTurnEffect(players)` ← opponents.
  `eachPlayer()` is still one floating effect on every player (affected player null), as the tests and the readers know it.
- `DoubleControllerLifeEffect(player)`, `DoubleDamageFromCreaturesEffect(filter, sources)`, `EachPlayerCreatesTokenEffect(token, record, players)`,
  `RedirectNextDamageFromChosenSourceToTargetCreatureEffect(subject)`, `MoveCounterFromSourceToTargetCreatureEffect(…, from, to, anyPermanent)`,
  `CopyThisSpellForTargetControllerEffect` (← the target player's copy) and `DrawCardEffect(n, controllerOf(target()))` ← their siblings.

**Behaviour changes**, each with a test that fails on the old handler unless noted.

1. The Cheetah Planet: "all Cats gain provoke until end of turn" locks the Cats as it resolves (CR 611.2c); Cats that arrived later gained it.
   `TheCheetahPlanetTest.catEnteringAfterChaosDoesNotGainProvoke`.
2. Celestial Mantle doubles the life of the controller of the creature that dealt the damage, whatever the Aura enchants as it resolves (ruling
   2009-10-01). `CelestialMantleTest.doublesLifeOfDamagingCreaturesControllerAfterAuraMoves`.
3. Blaster, Morale Booster removes what it moves, not what was put on after Hardened Scales (CR 122.5; it lost two counters to move one).
   `BlasterCombatDJTest.movedCounterIsRemovedOnceWhenPlacementIsIncreased`.
4. Cytoplast Root-Kin and Arcbound Fiend remove no counter they can't put on themselves (CR 122.5; Blightbeetle).
   `CytoplastRootKinTest.activatedAbilityKeepsTargetCounterWhenRootKinCantHavePlusOneCounters`.
5. "Each player creates" makes the tokens in APNAP order, so their timestamps follow CR 613.7m (it was seat order).
   `ElephantMandrillTest.tokensAreCreatedInApnapOrder`.
6. Slicer, Hired Muscle declares no player target ("that player", Oracle). The old record's declared target was never rechecked, so
   `SlicerHiredMuscleTest.opponentWithHexproofCanStillGainControlOfSlicer` passes on both and pins it.
7. Log lines are the survivors'.

**AI parity.** A static grant to a target classifies as its payload does, else BENEFICIAL (the absorbed record gave none for a payload without
a polarity; the survivor's earlier static grants keep BENEFICIAL unless the payload says otherwise); a non-target grant classifies none, as
before. The controller draw is still scored as the absorbed record was. `TargetPolarityGuardTest` passes.

**Seen, not changed.**

- "Can't be blocked / can't be sacrificed this turn" (Kappa Cannoneer, Lockjaw, Slicer) and Full Steam Ahead's blocking limit are modelled as
  granted abilities, which a "loses all abilities" removes; they are rules of the game (CR 611.2c). Converted as they were.
- `TargetOpponentCreatesTokenEffect` derives "target opponent" and `OpponentGainsControlOfSourceCreatureEffect` picks the opponent by seat: both
  are right with two players only.
- `MayCopyHandlerService.handleBecomeCopyChoice` logs "'s copy target is no longer on the battlefield." when the copy succeeds.

**Tests edited**: the five behaviour tests and the Slicer pin above added; `TreeOfRedemptionTest`, `TreeOfPerditionTest` and
`FlamesOfTheBloodHandTest` build the survivor instead of the deleted opponents record; `CardEffectTargetingConsistencyTest` lost two deleted
names; `BaseCardTest.silencedPlayers` names the survivor.

**Tests run**: 336 classes (464 suites, 3,943 cases by the JUnit XML file names, forced rerun of the final tree), all pass: every card test of the 13 other survivors, 16 of
`DrawCardEffect`'s, every card test naming a rewritten card, the targeting, legality, casting-permission, layer and registry tests of the
application and the AI's polarity, selector and copy tests, the ratchets. All test sources compile.

**For the next run** (5m, the pairs).

- Start with those that look like plain folds: `*BecomesSubtypeUntilEndOfTurn`, `*CantCastNoncreatureSpellsThisTurn`, `*CantGainLifeRestOfGame`,
  `*CantLoseLifeThisTurn`, `*BecomesMonarch`, `Redirect*DamageTo*ThisTurn`, `GrantTargetingRestrictionTo*UntilEndOfTurn`, `GrantSubtypeTo*`,
  `MoveAllCountersFrom*To*`, `PerpetuallyBoost*`, `Turn*FaceDown`, `Goad*ControlsUntilNextTurn` (none of these read yet except
  `MoveAllCountersFromSourceToTargetCreatureEffect`, which is another "all counters" amount, not a subject).
- The static pairs (`*CantCastSpells…`, `PreventAllDamageTo*`, `DoubleDamageTo*`, `Ignore*Hexproof`, `Set*MaximumHandSize`) are read by one
  query each: fold through `staticSubjectNames` and check the face-down and lost-abilities guards of both readers first (they differ in
  `*SpellsCantBeCountered`, hence its three `step-9` rows).
- The cost pairs (`Sacrifice*Cost`, `Exile*Cost`, `Return*ToHandCost`, `PutCounterOn*Cost`) are payments, probably heads for step 10.

### Step 5, run 5m, finished (2026-11-22)

Re-measured: 94 pairs held 174 unaudited classes. Each was read; `--siblings` now lists no unaudited class, so 5m and step 5 are done.
Ratchet 4,624 / 3,853 → 4,593 / 3,835 records / handlers (31 records, 18 handlers and the two capability interfaces without another
implementer deleted: `ControllerTurnRestrictionEffect`, `ChosenPlayerRecipientDamageMultiplyingEffect`). 60 card files rewritten
(deleted scratch script; every changed line read back by eye), each card's Oracle text checked with `get_card`.

| Audited | Absorbed | Survivor | Blocked-other | Blocked-player-loop | Unique | `step-9` | Left |
|---|---|---|---|---|---|---|---|
| 182 ledger rows (176 classes without one before, 6 re-ledgered) | 31 | 30 | 66 | 5 | 47 | 3 | 0 |

**Folds** (survivor ← absorbed; the ledger has each expression).

- One-shot, player subject: `BecomeMonarchEffect` ← target and damaged player (the inherent monarch ability, CR 726.2, uses
  `targetPlayerDeclaredElsewhere()`); `BlightEffect` ← target player; `PlayersCantCastNoncreatureSpellsThisTurnEffect` ← opponents;
  `PlayersCantGainLifeRestOfGameEffect` ← that player; `PlayersCantLoseLifeThisTurnEffect` ← you;
  `GoadCreaturesTargetPlayerControlsUntilNextTurnEffect` ← damaged player; `ExileControllerLibraryEffect` ← target player (its benign
  spec is kept, which settles the step 9 run 4 polarity row); `GrantStaticEffectToPlayerUntilPlaneswalkEffect` ← target player.
- One-shot, permanent subject: `TargetCreatureBecomesSubtypeUntilEndOfTurnEffect` ← source (Adrestia's additive form is
  `GrantSubtypeUntilEndOfTurnEffect`); `GrantSubtypeUntilEndOfTurnEffect` ← the own-creatures permanent grant;
  `BoostTargetCreatureWhileSourceTappedEffect` ← all creatures; `DealDamageToPermanentsTargetControlsEffect` ← that player's creatures;
  `GrantTargetingRestrictionToTargetUntilEndOfTurnEffect` ← own creatures (`lastingRule`); `PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect`
  ← own creatures; `PutAuraFromHandOntoSelfEffect` ← equipped (`triggering()`); `RedirectTargetCreatureDamageFromChosenSourceToTargetEffect`
  ← to this creature; `TransformSelfEffect` ← target and triggering (the 701.28f check stays the source's).
- Static, player subject through the new `SubjectResolutionService.namesPlayer`: own-turn casting, casting during combat, the two
  Angelic Arbiter / Prahv restrictions, can't cast matching spells, can't gain life (`Scope` enum gone), one draw each turn (judged by
  the record, which `DrawService` asks), double damage to opponents / the chosen player and their permanents.
- Static, permanent subject: combat prevention to, and to and by; artifact prevention; the player-damage redirection; damage kept
  through cleanup (a set judged through `staticSubjectNames`). Self and attached forms keep their own guards (`hasActiveStaticEffect`
  with a matcher, `hasAuraWithEffect`), so those read as before.
- The three player queries in `GameQueryService` share one scan over `staticEffectsIncludingTemporary`. Both old readers of each had
  its face-down and lost-abilities guards, except the every-player and enchanted-player gain-life scans, which tested only "lost all
  abilities" where the scan also counts "all but mana abilities" (CR 613.1f). Every card that does that names lands only, so no card
  behaves differently.

**Behaviour changes.**

1. Void Winnower, Llawan and Archon of Valor's Reach restrict no casting while face down (CR 708.2a).
   `VoidWinnowerTest.faceDownWinnowerDoesNotRestrictCasting` fails on the old code.
2. Marisi declares no player target ("that player", Oracle; CR 115.10a). The old record's declared target was never rechecked, so
   `MarisiBreakerOfTheCoilTest.goadsCreaturesOfDamagedPlayerWithHexproof` passes on both and pins it.
3. Log lines and the prompt texts of the two folded prompts (Aura from hand, the redirect's source choice) are the survivors';
   Balefire and Shockmaw now record a DEAL_DAMAGE step and a target transform a TRANSFORM step (nothing reads either there).

**AI parity.** `TargetPolarityClassifier` gives the absorbed forms what they had (source subtype change, set restriction, set perpetual
grant, redirect to the source: none; target transform: BENEFICIAL). Thran Weaponry's boost is now a `CreatureBoostEffect`, as the set
form of `BoostTargetCreatureEffect` already is.

**Seen, not changed.**

- Fixed after the run: `SeraphOfNewCapennaTest` and `MidgarCityOfMakoTest` expected the prompts from before step 8's runs 6, 7 and 20
  (the engine was right). A back face is tested under its front face's name, which `--tests-for` missed; it now adds the tests of
  the cards constructing each card.
- Courageous Resolve: "you can't lose life" doesn't stop paying life as a cost (CR 119.8); `canPayLifeForCosts` takes no player.
- The Ozolith removes counters it then can't put on the target (CR 122.5).
- Vines of Vastwood's "can't be the target" is a granted ability, which "loses all abilities" removes (CR 611.2c makes it a rule).
- Pariah, the attached preventions and Curse of Bloodletting are read without a lost-abilities check (CR 613.1f).
- Harbor Guardian's kind of "defending player may": `MayEffect`'s `DEFENDING_PLAYER` doesn't read the recorded defending player
  (CR 508.5); Soldevi Heretic derives "target opponent" and targets nobody.

**Tests edited**: the two tests above added; `PermissionDeniedTest`, `TempestEfreetTest`, `TreeOfPerditionTest`, `TreeOfRedemptionTest`,
`GameQueryServiceTest` and `TriggeredAbilityStackServiceTest` build or name the survivor; `CardEffectTargetingConsistencyTest` lost a
deleted name. No assertion changed.

**Tests run**: 407 classes (542 suites, 4,783 cases by the JUnit XML file names): the card tests of every rewritten card and of the
small survivors, 60 of `TransformSelfEffect`'s, every other test naming a rewritten card, the application's ratchet, resolver, query,
permission, legality, cleanup, stack and combat tests, the AI polarity, evaluator, selector, engine and copy tests. All pass but the
four Seraph cases above. All test sources compile.

**For the next run** (step 5 is done).

- Step 9 gets three rows: the two "may draw a card" records and Agatha's Soul Cauldron's exile-then-counter.
- Step 10 gets the pairs the ledger sends there: the cost heads, can't-activate, unblockable, subtype grant, choose-one-for-target,
  perpetual boost, the returns from a graveyard, goad and must-block.
- The `blocked-other` pairs that need one path before a subject: fight, heist, turn face down, hand size (one interface with players),
  return the dying creature, copy a spell or ability, and a source-card `CardSubject` for the card-axis pairs.

### Step 9, run 5 (2026-11-22)

Family Exile, continued: run 4's God-Pharaoh's Gift item, the `step-9` row step 5m handed over (Agatha's Soul Cauldron), three shared
parameters or steps the tail needed, then the one-card tail, cheapest compositions first. Ratchet 4,593 / 3,835 → 4,569 / 3,808 records /
handlers (25 records and 28 handlers deleted, 1 record and handler added). 27 card files changed (by scratch scripts; every changed line
read back by eye; Fractured Identity and Winds of Abandon lost an unused import only). Oracle text checked with `get_card` for each of the
25 rewritten cards; rulings with `get_rulings` (Fishing Gear, Pako, Altaïr, Culmination of Studies, Torrent Sculptor, Ruinous Intrusion,
Summoner's Sending, Induced Amnesia, Prismatic Ending, Rakdos the Muscle, The Tale of Tamiyo, Spark of Creativity). CR 122.1, 400.7,
603.12, 608.2c and 608.2h checked with `get_rule` (2026-11-22).

| Family | Audited | Absorbed | Split | Survivor | Dead | Blocked (cause) | Unique | Left unaudited |
|---|---|---|---|---|---|---|---|---|
| Exile | 92 (87 classes without a row, 3 ledger rows taken up, 2 survivors, one a new step record) | 3 | 20 | 2 (one new step record) | 2 | 55 blocked-other: 9 a delayed trigger bound to a step's cards, permanents or count (Dragonhawk, Bank Job, Synthetic Destiny, the three created-permanent exiles, the delayed hand returns, Hofri's link), 4 no choose-one step over cards or triggering cards, 4 same-name sets over a card, 4 per-player results of an exile step or a count per player, 3 no exile-target-spell step, 4 cast-now options, 3 become-a-copy steps, 3 a permission group or predicate over several exiled cards, 2 face-down hand exile, 2 ransom heads for step 10, 2 choose-a-quality steps, the rest one each (the ledger names it) | 10 | 101 records (73 used by fewer than three cards); no `step-9` Exile row left. All families: 2,810 rare records without a row |

- **"From your graveyard", "from that player's graveyard".** `ExileUpToOneCardFromGraveyardEffect` gained `PlayerSubject graveyardOf`
  (`.from(players)`, `oneMatching(filter)` for the mandatory form; `targetPlayer()` declares the benign player target). It absorbed
  `ExileMatchingCardFromTargetGraveyardEffect` (Hama, Dreams of Steel and Oil; run 1's blocker) and carries five splits: Trystan,
  God-Pharaoh's Gift, Torrent Sculptor, Forgotten Harvest, Resplendent Marshal. A mandatory choice of the only matching card in your own
  graveyard is not asked, as those records didn't ask. Six bespoke resume flag groups and God-Pharaoh's Gift's token helper left
  `GraveyardChoiceHandlerService`. Resplendent Marshal's reflexive effect reads the EXILE step it carries, not a cost-card snapshot.
- **A counter on the exiled cards.** New `PutCounterOnExiledCardsEffect(CardSubject, ExiledCardCounter)` (memory, brain, croak, fetch)
  after an exile step: Altaïr, The Animus, Rex, Grolnok, Pako. `GameData.addToExileWithCroakCounter` is gone. The counter (CR 122.1) is
  its own step right after the exile, within the same resolution.
- **"For each blue card exiled this way".** `AmountThisWay.exiledCards(CardPredicate)` (the cards judged as they are now, like
  `HappenedThisWay.toACard`): Culmination of Studies, Pako, and The Binding of the Titans, which no longer reads the event value.
  `ExileGraveyardCardsEffect.eventValueFilter` has no card left; its component can go with step 10's collapse of that record.
- **Smaller ones.** Agatha's Soul Cauldron, Undead Alchemist, Summoner's Sending, Ruinous Intrusion, Fishing Gear, The Tale of Tamiyo
  (`anyNumberOfTargetsFromControllerGraveyard`), Mizzix's Mastery overloaded, Spark of Creativity and Synth Eradicator split onto existing
  steps; Bogardan Phoenix and Nicol Bolas, the Arisen absorbed by parameters (the ledger has each expression). Two records were dead:
  only unused imports named them.

**Behaviour changes.**

1. Fishing Gear creates the Fish when the damaged player's library is empty ("If you don't", Oracle; CR 608.2c). The old handler did
   nothing. `FishingGearTest.createsFishForEmptyLibrary`.
2. Agatha, Altaïr, The Animus, Rex, Summoner's Sending and The Tale of Tamiyo don't exile a target that left its graveyard and came back
   (CR 400.7), as run 4 fixed the survivor. `SummonersSendingTest.returnedTargetIsNotExiled`.
3. A card the choose-a-card graveyard exile moves is no longer "put into a graveyard from the battlefield this turn" (CR 400.7), as the
   other graveyard exiles already had it. `EntrailsFeasterTest.exiledCardIsForgottenAsPutIntoTheGraveyardThisTurn`.
4. AI only: Nicol Bolas's −12 declares a harmful player target, so the simulator aims it at the opponent (it preferred itself).
   `RolloutPolicyTest.libraryExileTargetsTheOpponent`.
5. Log lines and prompt texts are the steps'; Pako's players are in APNAP order (they were in seat order). No test asserted the old ones.

**Seen, not changed.**

- `AllowPlayCardsFromExileEffect.withAnyManaType()` gives no any-type mana to a land card, so a land with an Adventure exiled that way
  can't be cast as the Adventure with mana of any type ("mana of any type can be spent to cast those spells"). Rakdos, the Muscle stays
  on its record for it; its test asserts the mark on an exiled land.
- `SpellEvaluator`'s removal scoring doesn't look inside a `ConditionalEffect`; Prismatic Ending stays on its record for it.
- The exile steps record one card per permanent, so a step over `cardsThisWay(EXILE)` sees only one card of a merged permanent (Hex).
- `GraveyardReturnSupport.exileCardFromAnyGraveyard` doesn't clear the "put into a graveyard from the battlefield this turn" sets that
  `PermanentRemovalService.removeCardFromGraveyardByIdForExile` clears, nor does the own-all sweep of `ExileGraveyardCardsEffect`
  (CR 400.7); only the choose-a-card step was moved to the latter.

**Tests edited.** The three new card cases and the AI case above;
`ExileTargetPermanentPutManaValueCountersOnTargetCreatureEffectHandlerTest` went with its handler (`RuinousIntrusionTest` covers the case).
No assertion changed.

**Tests run** (one Gradle run, forced rerun of the final tree, one `--tests` list per task, counted by XML file name): 213 classes
(388 suites, 3,362 cases), all pass: the changed cards' tests and the card tests constructing one of them, every card test of
`ExileUpToOneCardFromGraveyardEffect` and the new step, every card reading `AmountThisWay.exiled*` or `cardsThisWay(EXILE)`, samples of
`ExileGraveyardCardsEffect`, `CopyImprintedCardAndMayCastCopyEffect`, `ExileTopCardOfOwnLibraryEffect`, `AllowPlayCardsFromExileEffect`
and `MayEffect`; the application's ratchets, resolver, step-result, targeting, trigger and graveyard-choice tests; the AI polarity, copy,
selector and simulator tests. The application, AI, engine testFixtures and all 26 letters' card-test sources compile. Not run: the full
suites.

**For the next run (Exile, run 6, then Each).**

- The cheapest shared blockers this run counted: a delayed trigger over `createdThisResolution()` with a timing (Stone Idol Trap, Waylay,
  Tilonalli's Summoner) and over `cardsThisWay(EXILE)` (Three Wishes, Elkin Lair, Mnemonic Betrayal, Dragonhawk, Bank Job, the hand
  returns); an exile-target-spell step recording EXILE (Mindbreak Trap, Aven Interrupter, Gale's Redirection); a face-down option and an
  EXILE record on `ExileTargetPlayerHandEffect` (Bottled Cloister, Induced Amnesia, then Hex Magic / Invasion of Kaldheim's record, whose
  AI draw estimate must survive); a choose-one step over `cardsThisWay(EXILE)` (Lord of the Void, Chandra Pyromaster, Capricious
  Hellraiser); a same-name predicate over a `CardSubject`.
- Not read yet, in the 73: the "instead of dying" replacement records, the hand exiles with counters, Settle the Wreckage, Dragon's
  Approach, Chandra, Heart of Fire, Primal Surge, the chosen-permanent exiles (Dalek, The War Games, Fugitive of the Judoon), the cost
  records and the opening-hand and as-enters exiles. Run 6 can close the family.

### Step 9, run 6 (2026-11-22)

Family Exile, continued: run 5's three named blockers (exile target spell, a delayed trigger bound to a step result, the face-down hand
exile) with the ledger rows that waited for them, the chosen-permanent exile the tail showed six times, then verdicts for the rare tail.
Ratchet 4,569 / 3,808 → 4,537 / 3,776 records / handlers (37 records and 37 handlers deleted, 5 and 5 added). 39 card files rewritten (by
scratch scripts; every changed line read back). Oracle text checked with `get_card` for all 39; rulings with `get_rulings` where the old
handler or the text left a doubt. CR 400.7, 400.7j, 406.3, 603.4, 603.7a to 603.7e, 603.12, 608.2c, 608.2d, 608.2h, 610.3a, 610.3b,
704.5s, 708.2a and 731.3c checked with `get_rule` (2026-11-22).

| Family | Audited | Absorbed | Split | Survivor | Dead | Blocked (cause) | Unique | Left unaudited |
|---|---|---|---|---|---|---|---|---|
| Exile | 93 rows (58 of the 73 rare records, ledgered rows taken up, 7 survivors) | 9 | 27 | 7 (five new step records) | 1 | 10: 8 blocked-other, each with a cause of one or two cards or step 10's cost combinator (Koya), 2 blocked-step-model (Winter, the hatching look) | 39 | 15 rare records (five shared blockers, below). All families: 2,751 rare records without a row |

- **A delayed trigger bound to a step result.** New `RegisterDelayedStepTriggerEffect` (`aboutExiled` / `aboutCreated(step, effects...)`).
  `DelayedReferents.stepResults` carries that one step's outcome, each card bound to its stay (`MovedCard.entryVersion`), and the delayed
  ability's entry reads it as its own (`StackEntry.readStepResults`), so the body is ordinary steps: `cardsThisWay(EXILE)`,
  `AmountThisWay.stillExiled()`, `createdThisResolution()`. An intervening "if any remain exiled" wraps a body of several steps as one
  sequence: the stack door checks the "if" around each effect, and the first step makes it false. An ability that only acts on those
  objects isn't created when there are none (as the old handlers, and it could affect nothing); `evenIfNothingHappened()` is for a body
  that does more. `PutExiledCardsIntoOwnersGraveyardEffect` takes a `CardSubject` and records the new `StepKind.PUT_INTO_GRAVEYARD`.
- **Exile target spell.** New `ExileTargetSpellEffect` (`withSource()`) records EXILE; it absorbed the four records. Ertai's Meddling
  stays unique (it keeps the stack entry).
- **Face-down hand exile.** `ExileTargetPlayerHandEffect.exiledFaceDown()`, recording EXILE. Hex Magic's AI draw estimate is
  `SpellEvaluator.drawsForExiledHand`. Induced Amnesia keeps its benign target: its draw step declares it.
- **The chosen-permanent exile.** New `ExileChosenPermanentsEffect` pauses at its end and records EXILE as answered; it replaced six
  records, five choice contexts and their completions. `StepOutcome.lastKnownPermanents` gives "the exiled creature's power" and mana
  value to later steps and to the reflexive and delayed abilities that read the step.

**Behaviour changes.**

1. A delayed return or put-into-graveyard skips a card that left exile and was exiled again (CR 603.7c, 400.7; Apple of Eden's ruling):
   Ignorant Bliss, Suppress, Apple, the Jars, Three Wishes, Dragonhawk. `IgnorantBlissTest.cardExiledAgainIsNotReturned`.
2. A face-down card returned to a hand is no longer named in the log (CR 406.3). `IgnorantBlissTest.returnDoesNotRevealTheFaceDownCard`.
3. The cards of a face-down hand exile are shown to nobody (CR 406.3; Induced Amnesia's ruling; also Bottled Cloister).
   `InducedAmnesiaTest.noPlayerMayLookAtTheExiledCards`.
4. `ExileTopCardMayPlayThisTurnEffect.exiledFaceDown()` grants the look its cards' texts give (Oracle text of its eleven users).
   `BlightwingBanditTest.controllerMayLookAtTheExiledCard`.
5. A flashforward spell exiled from the stack goes to the bottom for Aven Interrupter, Gale's Redirection and Spell Queller's record as
   it did for Mindbreak Trap; Aven exiles the physical card. `BoltfireTest.flashforwardSpellExiledFromTheStackGoesToTheBottomInstead`.
6. `DiscardHandEffect` can't make a protected opponent discard (Tamiyo, Collector of Tales's text), as Memory Jar's own record knew.
   `WheelOfFortuneTest.discardPreventedPlayerKeepsTheirHand`.
7. The exiled permanent's mana value and power are read as it last existed, also for a subject exile: a face-down one has mana value 0
   (CR 608.2h, 708.2a). `FearOfChangeTest.faceDownCreatureHasNoManaValue`, `YannikScavengingSentinelTest`.
8. A reflexive ability of `QueueReflexiveAbilityEffect` doesn't keep a finished Saga (CR 704.5s: it is no chapter ability), as The War
   Games's record knew. `TheWarGamesTest.chapterIVCreatesASeparateTriggerBeforeExilingWarriors` fails without it.
9. A reflexive division's total reads the steps that made the ability (CR 603.12): `ETBTokenTargetService`. `YannikScavengingSentinelTest`.
10. Two chosen permanents are offered only so that each choice has one, and leave together (CR 608.2d; Fugitive of the Judoon).
    `FugitiveOfTheJudoonTest.humanThatIsTheOnlyArtifactIsLeftForTheArtifactChoice`.
11. An exiled merged permanent is each of its cards for `cardsThisWay(EXILE)` (CR 731.3c), which Hex's split needs. No new test:
    `HexKellansCompanionTest` covers the single-card path, not a merged Hex.
12. `AllowPlayCardsFromExileEffect.withAnyManaType()` marks land cards too (a land's Adventure is a spell). `RakdosTheMuscleTest`.
13. Memory Jar: every player discards, then the cards return, in one order of two steps (it alternated per player). Stone Idol Trap and
    other created sets: one delayed ability for the set. Mass Polymorph skips a card that can't enter from a library and leaves the
    legend rule to state-based actions. These three are covered by the cards' existing tests only for the common path.
14. AI only: an enters trigger's `SequenceEffect` is scored by its steps; "exile target creature you control" is no removal.
    `SpellEvaluatorTest` (two cases).
15. Log lines and prompts are the steps'.

**Seen, not changed.**

- `ForcedCostOrElseEffect`'s fallback runs through a per-class list in `DestructionSupport.resolveForcedCostElseEffects`: a fallback not
  on it, or one reading a step, silently does nothing (found with Koya).
- The other whole-hand discarders that call `discardHand` (the two "each player discards their hand, then draws" records, Wheel of
  Misfortune, Goblin Game, the may-discard wheel) still ignore "can't cause you to discard" (Tamiyo's text).
- `ReturnExiledCardToHandEffect` (Necropotence and three other callers) and `PutUnplayedExiledCardsIntoOwnersGraveyardEffect`
  (`LibraryChoiceHandlerService`) don't check the exile stay (CR 400.7); nor does a reflexive ability's carried step result.
- `ExileTargetPermanentEffect`'s target path exiles several targets one at a time (CR 603.10a); the subject path batches.
- Moonring Mirror's and Duplicity's hand exile keeps the implicit look of a source-linked face-down card; neither text gives one
  (CR 406.3).
- `ExileTopCardMayPlayThisTurnEffect.withAnyManaType()` still skips land cards.

**Tests edited.** The new cases above. `StepTriggerServiceTest`'s Memory Jar case and `ReflexiveRouteRecordTest`'s exile route name the
replacing steps (the records are deleted; same assertions). `DiscardHandEffectHandlerTest` passes the new dependency and stubs "can be
made to discard" for its target-group case. `CardImmutabilityArchTest` lost the deleted handler's allow-list entry. No assertion changed.

**Tests run** (one Gradle run, forced rerun of the final tree, one `--tests` list per task, counted by XML file name): 808 classes (970
suites, 10,134 cases), all pass: the changed cards' tests and the card tests constructing one of them, every card test of the new steps
and of the survivors and readers with changed semantics (`DiscardHandEffect`, `AllowPlayCardsFromExileEffect`,
`QueueReflexiveAbilityEffect`, `PutCardsIntoHandEffect`, the reveal-until step, the `cardsThisWay(EXILE)` and `AmountThisWay.exiled*`
readers), samples of 60 of `ExileTargetPermanentEffect`, `ExileTopCardMayPlayThisTurnEffect`, `ExileAllPermanentsEffect` and
`ExileGraveyardCardsEffect`, the face-down view tests; the application's ratchets, resolver, step-result, targeting, trigger and
view tests; the AI evaluator, polarity, copy, selector and simulator tests. The application, AI, engine testFixtures and all 26 letters'
card-test sources compile. Not run: the full suites, the AI fuzz and stress tests.

**For the next run (Exile, run 7: 15 rare records, then Each).** Each blocker below has three or more classes, so none was ledgered:

- same-name cards from zones (Deadly Cover-Up, Unexpected Conversion, Legion's End, Hour of Glory, and the ledgered Secret Salvage,
  Haunting Echoes, Moratorium Stone, the two FromZones records, the counter record);
- a recording choose-a-card-from-hand exile (Mairsil, Ethereal Valkyrie, Uvilda; ledgered Alaundo and River Song records, the token-copy
  one, Forsaken City), then become-foretold for The Foretold Soldier too;
- a shuffled face-down pile manifested or cloaked from exile steps (Become Anonymous, Expose the Culprit, Jeskai Infiltrator; ledgered
  Ghastly Conscription);
- a player subject, a shuffle and a per-exiled-permanent repeat on the reveal-until step (Chaos Mutation, Chaos Is My Plaything; ledgered
  Transmogrify's record and the sacrifice sibling; keep Divergent Transformations' sequential ruling);
- SEEK as a step kind and a perpetual change over a `CardSubject` (Cogwork Progenitor, Housemeld, the seek's `ExileSpecificCardFromHandEffect`;
  ledgered Puppet Raiser, Pep, the seek-and-discard record).
- Ledgered causes of three or more still unbuilt: choose one of `cardsThisWay` (Lord of the Void, Chandra Pyromaster, Capricious
  Hellraiser, Conspiracy Theorist), per-player results of an exile step (Oversimplify, Grand Ossuary, Winds of Abandon, Martyr's Cry),
  become a copy over a `CardSubject` (Dimir Doppelganger, Lazav, Identity Thief), a delayed trigger on an event rather than a step
  (Portcullis, Lagrella, The Ruinous Powers).

### Step 9, run 7 (2026-11-22, Each only started)

The Exile tail's shared blockers (same-name zones, hand-choice exile, face-down pile, reveal-until player, seek and perpetual subjects)
with the ledger rows that waited for them, then the first two Each absorptions. Ratchet 4,537 / 3,776 → 4,513 / 3,752 records / handlers
(29 records and 29 handlers deleted, 5 and 5 added). 50 card files changed (49 rewritten by scratch scripts; Homeward Path lost an unused
import). Oracle text checked with `get_card` for the 49; rulings with `get_rulings` for the cards whose handler or text left a doubt.
CR 201.2a, 400.7, 400.7j, 406.3, 603.4, 603.7c, 608.2h, 609.3, 701.23a, 701.23b, 701.40a, 701.58a, 701.58f, 702.62b, 708.2, 708.2a and
709.4a checked with `get_rule` (2026-11-22).

| Family | Audited | Absorbed | Split | Survivor | Dead | Blocked (cause) | Unique | Left unaudited |
|---|---|---|---|---|---|---|---|---|
| Exile | 47 rows (the 15 rare records, 22 ledgered rows waiting for the blockers or narrowed by them, the survivors) | 6 | 21 | 10 (five new records) | 0 | 7: 5 blocked-other, each now a cause of one card or a chosen name, 2 blocked-step-model (the revealed-hand continuation) | 3 | no rare record |
| Each | 4 (2 absorbed, their 2 survivors) | 2 | 0 | 2 | 0 | 0 | 0 | 113 rare records. All families: 2,732 rare records without a row |

- **Same name.** New `ExileCardsWithSameNameEffect` (named by permanents, `targetSpell()` or cards; ANY_NUMBER, ALL per CR 701.23b,
  EVERY), recording EXILE and `StepOutcome.fromHand` (`AmountThisWay.exiledFromHand()`). An exile step records the exiled permanent's
  name (`LastKnownCardStats.name`); `othersItsControllerControlsSharing` names "all others that player controls with the same name".
  Nine records went. The prompt is `MultiZoneExileChoice`, changed: it carries the zones searched (`SearchedZones`, with how many
  cards from the top of a library) and shows exactly those (CR 701.23a), apart from whether the library is shuffled. The deleted
  handlers showed only the matching cards. (The run's first tree showed all three zones, the hand to a library-only search too;
  corrected in the orchestrator's review.)
- **Exile from hand.** New `ExileCardFromHandEffect`, pausing at its end with EXILE recorded by the shared answer; five records, four
  bespoke prompts and their completions went. `BecomeForetoldEffect`, a refine count on `PutCounterOnExiledCardsEffect`, the mana-value
  form of `PutTimeCountersAndGainSuspendEffect`.
- **Face-down pile.** New `ExileFaceDownPileEffect` (three ways to make a pile, manifest or cloak) absorbed four records.
- **Reveal until.** `RevealUntilCardPredicateRestOnBottomRandomEffect` gained `revealedBy` (players at once, or one reveal per exiled
  permanent's controller in turn, as Divergent Transformations' rulings say) and `shufflingRest()`; four records went, Polymorph's too.
- **Seek and perpetual.** `StepKind.SEEK`; `cardsThisWay(SEEK)` follows a sought card into the hand (the one hidden-zone case, digital
  only), bound to its stay for `RegisterDelayedStepTriggerEffect.aboutSought`. Two perpetual grants and two specific-card steps take a
  `CardSubject`. Four records went; Housemeld split onto `PerpetuallyBecomeOnlyCardTypeEffect`.
- **`HappenedThisWay` over an exile step.** `toACard` judges an exiled permanent as its card in exile (Deicide's ruling); `toA` as it
  last existed (Hour of Glory, CR 608.2h).

**Behaviour changes.**

1. Mairsil's choice from hand or graveyard doesn't shuffle the library (Oracle: no search).
   `MairsilThePretenderTest.cagingACardFromHandKeepsTheLibraryOrder`.
2. A converted same-name search shows the searcher every card of the zones it searches and no other (CR 701.23a;
   `HauntingEchoesTest.showsTheSearchedLibraryButNotTheHand`, `SpecializedInteractionProjectionTest` for a hand without a library,
   a shape no card has). A library that can't be searched is neither offered nor shown, and is still shuffled (CR 609.3;
   `SplinterTest.searchProhibitionLeavesTheLibraryOutOfTheSearch`, `doesNotShowALibraryThatCantBeSearched`); under Aven Mindcensor
   only its top four cards are (Oracle; `SplinterTest.searchesOnlyTheTopFourCardsUnderAvenMindcensor`). Kotose's search doesn't
   offer the card her first sentence exiled, which stays linked to her, and asks nothing when nothing else matches
   (`KotoseTheSilentSpiderTest.searchPromptDoesNotOfferTheCardAlreadyExiled`). The answer's log names the zones searched.
3. A graveyard card a multi-zone choice exiles is no longer "put into a graveyard from the battlefield this turn" (CR 400.7).
   `SurgicalExtractionTest.exiledCardIsForgottenAsPutIntoTheGraveyardThisTurn`.
4. "The same name as that creature" is the name it had on the battlefield, effects included (CR 201.2a, 608.2h; Legion's End, Hour of
   Glory, Legions to Ashes). `LegionsEndTest.renamedCreatureIsReadByTheNameItHad`.
5. Jeskai Infiltrator's pile enters together, and with the source gone the top card is still exiled first (Oracle; CR 609.3).
   `JeskaiInfiltratorTest.topCardGoesThroughExileWhenTheSourceIsGone`.
6. Alaundo removes the time counters also when no card was exiled (Oracle's "Then"; CR 609.3).
   `AlaundoTheSeerTest.removesTimeCountersWithNoCardToExile`.
7. A sought card that left the hand and came back isn't discarded (CR 603.7c, 400.7), and a seek to hand is a new stay there.
   `AriusFlybyTrawlerTest.soughtCardThatReturnedToTheHandIsNotDiscarded`.
8. The sought cards of one ability are discarded as one event (Oracle's "discard those cards").
   `PhyrexianHarvesterTest.discardsTheSoughtCardsAsOneEvent`.
9. Thoughtweft's Call's seek triggers "whenever you seek" (Oracle). `ThoughtweftsCallTest.seekingTriggersWheneverYouSeek`.
10. Housemeld doesn't put a card onto the battlefield that left exile and was exiled again (CR 603.7c).
    `HousemeldTest.cardExiledAgainDoesNotReturn`.
11. Ethereal Valkyrie's log no longer names the face-down card (CR 406.3). `EtherealValkyrieTest.faceDownCardIsNotNamedInTheLog`.
12. Same-name matching reads each half's name of a split card, the pile choice honours "can't be chosen" and Moratorium Stone's
    permanents leave as one event and are matched by the names they have (a Room with both doors locked has none, CR 709.5): the
    resolver's and the sibling verbs' rules. No card test exercises these edge cases.
13. AI only: the counter-and-exile spells are counterspells to it; the hand exiles use the generic hand-choice answer (the token-copy
    choice picked the highest mana value); Chaos Mutation's target is neutral like Polymorph's; the owned-permanents control effect
    carries the absorbed record's `ControlStealingEffect`.
14. Log lines and prompt classes are the steps'.

**Seen, not changed.**

- Perilous Iteration makes two delayed abilities, one per seek, where its text has one ("Discard those cards"): a second seek step
  replaces the first one's outcome.
- `GameQueryService.isManifestBlockedFromZone` judges the card, not the face-down object, so Worms of the Earth stops a land card from
  being manifested (CR 701.58f, 708.2a). The pile step doesn't call it.
- Two perpetual keyword stores: `PerpetuallyGrantKeywordsToTriggeringCardEffect`'s triggering form writes `perpetualCardKeywords` (by
  the current card's id, unread by the flash permission), most others `perpetualKeywords`. The cards form writes the latter.
- Shape Anew reveals nothing when the artifact can't be sacrificed, though its "then reveals" doesn't depend on it (CR 609.3).
- The name-choice and revealed-hand same-name searches (Memoricide, Lobotomy, Unexpected Conversion, Grizzled Huntmaster) still offer a
  library that can't be searched, or all of it under Aven Mindcensor; Memoricide's family also shows that library to the searcher
  (CR 701.23a), and the other three show only the matches of what they search.
- A same-name search with no match asks nothing, so the searcher doesn't get the look CR 701.23a allows.
- `ExileFromHandChoice.untapPermanentId` has no setter left.

**Tests edited.** The new cases above and `DivergentTransformationsTest.bothCreaturesLeaveAsOneEvent`. `HauntingEchoesTest`'s log
assertion names the exile step's line (the survivor's log wins). `KotoseTheSilentSpiderTest.resolveKotose` no longer picks the
already exiled target from the search, and asserts no prompt when nothing else matches (CR 701.23a: exile isn't searched). Five card tests name `ExileFromHandChoice` for the deleted prompt
classes (same assertions). Deleted with their classes: two handler tests (their cases are in the Counterbore and Surgical Extraction
card tests) and the token-copy prompt's AI strategy case. `ValidTargetServiceTest`, `AiTargetSelectorTest`, `TargetPolarityGuardTest`
and `LibraryChoiceHandlerServiceTest` name the replacing records (same assertions); `PendingInteractionContractTest` and
`CardImmutabilityArchTest` lost or renamed the deleted classes' lines.

**Tests run** (one Gradle run, forced rerun of the final tree, one `--tests` list per task, counted by XML file name): 813 classes
(957 suites, 8,361 cases), all pass: the changed cards' tests and the card tests constructing one, every card test of the survivors with changed semantics
and of their readers, samples of `ExileTargetPermanentEffect` (90), `CounterSpellEffect`, `DestroyTargetPermanentEffect`,
`ExileGraveyardCardsEffect` and `SearchLibraryEffect`; the application's ratchets, resolver, targeting, input-handler and contract
tests; the AI polarity, selector, strategy and evaluator tests; after the review, every card test of the same-name step and of the
other users of `MultiZoneExileChoice`, and its projection test. The application, AI, engine testFixtures and all 26 letters' card-test sources compile. Not run: the full suites, the AI fuzz
and stress tests.

**For the next run (Each, run 8).**

- Not built here, the other three-plus Exile causes run 6 listed, all still ledgered: choose one of `cardsThisWay` (Lord of the Void,
  Chandra Pyromaster, Capricious Hellraiser, Conspiracy Theorist: a choose step recording the card, the copy-cast prompt of two of
  them reusable), per-player results of an exile step (Oversimplify, Grand Ossuary, Winds of Abandon, Martyr's Cry: departures on
  `ExileAllPermanentsEffect`), become a copy over a `CardSubject` (Dimir Doppelganger, Lazav, Identity Thief), a delayed trigger on
  an event rather than a step (Portcullis, Lagrella, The Ruinous Powers). Build those first: none is out of the plan's scope.
- Then the 113 rare Each records. Read "Not in this plan" first: an each-player choose-then-act loop stays `blocked-player-loop`, but a
  record without a prompt is a subject fold (`each(...)`, `matching(players, filter)`, `playersThisWay`), and a prompt per player may
  be the `revealedBy` / `byPlayers` shape the survivors now have.

### Step 9, run 8 (2026-11-22, Each partly done)

The five shared blockers run 7 named, with the ledger rows that waited for them, then as much of the Each family as one careful run took.
Ratchet 4,513 / 3,752 → 4,491 / 3,729 records / handlers (26 records and 27 handlers deleted, 4 and 4 added). 34 card files changed by
scratch scripts (Collective Voyage only lost an unused import). Oracle text checked with `get_card` for all 34; rulings with `get_rulings`
where the handler or the text left a doubt. CR 101.4, 400.7, 400.7j, 603.7a to 603.7c, 603.10a, 608.2c, 608.2d, 608.2h, 609.3, 610.3b,
701.17c, 701.23a, 701.23b, 707.2 and 708.2a checked with `get_rule` (2026-11-22).

| Family | Audited | Absorbed | Split | Survivor | Dead | Blocked (cause) | Unique | Left unaudited |
|---|---|---|---|---|---|---|---|---|
| Each | 46 rows (44 of the 113 rare records, one ledgered row a survivor now expresses, one survivor) | 5 | 2 | 2 | 1 | 31: 27 player loop (each read: choose in turn, then act), 1 step model, 3 other (causes of one or two cards) | 5 | 69 rare records: 30 read and waiting for shared blockers (below), 39 not read |
| The blockers' rows (Exile, Reveal, Mill and their survivors) | 31 rows | 1 | 17 | 13 (four new records) | 0 | 0 | 0 | Exile: no rare record. All families: 2,687 rare records without a row |

- **A choice is a step.** New `StepKind.CHOOSE` and `ChooseCardEffect` (one of the cards a `CardSubject` names; a lone card is asked
  about only where the card says "choose"). `cardsThisWay(CHOOSE)` names it in the zone and stay it was chosen in, `cardsThisWay(MILL)`
  the milled cards (CR 701.17c), `triggeringCards(filter)` the event's. `ChoosePlayerAtRandomEffect` records a random pick, which
  `playersThisWay(CHOOSE)` reads: a step, as a random pick is no subject.
- **The card chosen from a revealed hand.** `ChooseCardsFromTargetHandEffect` and `RevealMatchingCardsFromTargetHandAndKeepEffect` record
  EXILE, or CHOOSE for a card kept, as they are answered. Their chosen-card rider components are gone (five cards are steps now), the
  decline rider stays. `ExileCardsWithSameNameEffect` takes the chosen card: for "all" that card is exiled unasked (Lobotomy's ruling),
  and with none chosen the search is still offered (Pick the Brain's ruling).
- **Per-player results of an exile step.** `ExileAllPermanentsEffect` and `ExileTargetPermanentUntilSourceLeavesEffect` record EXILE;
  `LastKnownCardStats.controllerId` makes `AmountThisWay(EXILE, ...).controlledByYou()` each player's own. `EachPlayerDrawsCardEffect`
  hands its amount the resolving entry, `CreateTokenThenPutCountersEffect` honours its template's token controller, the basic-land
  search queue's record takes players and a count per searcher.
- **Become a copy.** New `BecomeCopyOfSubjectEffect` over a card or a permanent as it is or last was; `keepingThisAbility()` keeps
  exactly the abilities that effect is part of, without the by-class scan and the trigger parameter.
- **A delayed trigger on an event.** New `RegisterDelayedEventTriggerEffect`, the event sibling of run 6's step trigger.
  `EventAbilitySupport.DelayedRecord` gives every delayed ability made after a step that step's results, whatever its trigger event.
  Portcullis's by-class branch in `PermanentRemovalService` is gone.

**Behaviour changes.**

1. Conspiracy Theorist's card can be cast only that turn (Oracle: "you may cast it this turn"; the permission lasted while it was
   exiled). `ConspiracyTheoristTest.castPermissionEndsWithTheTurn`.
2. Pick the Brain may leave the copies in the graveyard (Oracle: "any number"; they were forced).
   `PickTheBrainTest.deliriumMayLeaveTheGraveyardCopy`.
3. Lobotomy, Shimian Specter, Pick the Brain and Unexpected Conversion search as the same-name step does: the searcher, and nobody
   else, is shown the zones searched, and a library that can't be searched is neither shown nor offered (CR 701.23a, 609.3).
   `LobotomyTest.onlyTheSearcherIsShownTheSearchedLibrary`, `.doesNotShowALibraryThatCantBeSearched`. Unexpected Conversion's own-hand
   choice shows that hand to nobody (`UnexpectedConversionTest.handChoiceIsPrivate`).
4. Shadow Kin keeps only this ability, not every upkeep ability of what it copied before (Oracle: "except it has this ability").
   `ShadowKinTest.laterCopyKeepsOnlyThisAbility`.
5. Portcullis's return is made as it exiles: it doesn't return a card that left exile and was exiled again (CR 603.7c, 400.7), and
   doesn't depend on Portcullis's abilities as it leaves (its ruling).
   `PortcullisTest.doesNotReturnACardThatWasExiledAgain`.
6. Martyr's Cry's creatures leave as one event (CR 603.10a), and it, Winds of Abandon, Oversimplify and Grand Ossuary count only what
   was exiled (Oracle: "exiled this way"): the survivor's rules. No new test: the cards' tests cover the common path.
7. Oversimplify's counters are put on the token by the player who created it, after it is created (Oracle: "creates ... and puts"),
   in APNAP order. `OversimplifyTest.eachPlayerPutsTheirOwnCountersForInnkeepersTalent` covers who puts them.
8. Deicide offers the library search also when no card names what is searched (a token God; CR 701.23a). No test for that shape.
9. Bounty Board: every player draws, then every player gains, in one order of two steps (it alternated per player).
10. AI only: Divine Reckoning is a board wipe to it, as its survivor; Death by Dragons and Lair of the Ashen Idol aren't scored as its
    own tokens; Lobotomy's and Shimian Specter's target spec is the hand choice's; the copy choice of Chandra and Capricious Hellraiser
    uses the generic card-choice answer.
11. Log lines and prompt classes are the steps'.

**Seen, not changed.**

- The Legend of Yangchen's chapter I offers each player the permanents of that player's opponents; its text says "permanents your
  opponents control", the Saga controller's, so the opponent can now pick the controller's own (Oracle;
  `EachPlayerChoosesOpponentPermanentToExileEffect`; its destroy sibling has `opponentsOfSourceController`).
- Summon: Valefor compares the cards' mana values, not the permanents' (a face-down creature's is 0, CR 708.2a).
- Descent into Madness, The Legend of Yangchen and Sothera exile the chosen permanents one at a time (CR 603.10a; Sothera also before
  the next opponent chooses, CR 101.4).
- Conspiracy Theorist's trigger has no nonland filter, so it triggers and asks for a discard of only lands (Oracle: "one or more
  nonland cards").
- The Ruinous Powers' "mana of any color" is the engine's any-type permission, which also pays {C}.
- Haunting Echoes with nothing exiled doesn't offer the library look CR 701.23a allows; Reap Intellect's same-name path still offers a
  library that can't be searched (run 7's note).
- The Horus Heresy's prompt says "a creature you control" over every creature.

**Tests edited.** The new cases above and `DeathByDragonsTest.illegalTargetMeansNoDragons`. Renamed prompts, same assertions:
`ChandraPyromasterTest`, `CapriciousHellraiserTest` and `LordOfTheVoidTest` name `MultiGraveyardChoice`; `ShadowKinTest` names it and
answers by card id; `LobotomyTest`, `ShimianSpecterTest` and `PickTheBrainTest` answer the revealed-hand choice by the card where they
answered a list of names. `CardImmutabilityArchTest` names the new copy handler for the deleted one. One assertion was replaced:
`ResolutionScratchpadAmountTest.diedOfAnotherKindIsRefused` expected `AmountThisWay(EXILE).controlledByYou()` to be refused, the guard
this run's per-player exile results lift. It now checks the refusals that remain (a draw's, died after an exile, still exiled by
controller), and `exiledYoursCountsOnlyTheAmountControllersPermanents` tests what is allowed.

**Tests run** (one Gradle run, forced rerun of the final tree, one `--tests` list per task, counted from the XML of that run by class
name): 912 classes (1,359 suites, 12,625 cases, 29 skipped by `TriggerRouteRecordTest`'s assumptions), all pass: the changed cards' tests
and the card tests constructing one, every card test of the new steps and of the survivors with changed semantics
(`ChooseCardsFromTargetHandEffect`, the exile-until-the-source-leaves and exile-all records, the each-player draw and token records, the
same-name and hand-exile steps, the delayed step trigger, the random exile), samples of `ExileTargetPermanentEffect`,
`ExileGraveyardCardsEffect`, `LoseLifeEffect`, `MillEffect` and `DiscardEffect`; the application's ratchets and architecture tests and
the tests naming a changed class; the AI polarity, selector, evaluator, strategy and deep-copy tests. The application, AI, engine
testFixtures and all 26 letters' card-test sources compile. Not run: the full suites, the AI fuzz and stress tests, the frontend.

**For the next run (Each, run 9: 69 rare records).**

- 30 were read and left without a row, as each waits for something three or more share; build these first:
  - a REVEAL step of each player's top cards (the seven `EachPlayerReveals...` top-card records);
  - players, a filter and a destination on the per-player library-search queue (Old-Growth Dryads, Turtle Tracks, Hired Giant, Boldwyr
    Heavyweights, Scholarship Sponsor, Enigma Ridges, the Weird Harvest record; ledgered From the Ashes);
  - a "starting with you, each player may pay any amount of mana" step recording the total (the five join-forces records);
  - a "controls a matching permanent / the most creatures" `PlayerPredicate` and a `PlayerSubject` on lose-the-game and investigate
    (Bonder's Ornament, Nicol Bolas, Angel of Destiny, No Witnesses);
  - a `PlayerSubject` on the shuffle-into-library steps, recording per player (Survive's record, Archival Whorl, The Great Aurora);
  - and the parameters shared with classes of other families: a total-mana-value stop on the exile-until step (Tasha's Hideous
    Laughter, Dream Harvest), a copier subject (Mirrorform; step 10's copy verb), a set subject on must-block (Predatory Rampage).
- Then the 39 not read yet (villainous choices, "may ... or lose life", the keep-and-sacrifice and chosen-type loops).
- Unblocked or narrowed by this run, still ledgered: the three choose-a-card-in-hand perpetual records (CHOOSE exists; they need an
  own-hand subject for `ChooseCardEffect`, which must stay private), the random-opponent records (`ChoosePlayerAtRandomEffect`), Reap
  Intellect's same-name flag (both its steps exist), the opponents forms of "loses life for each ... they control" (87, 89, 90 in the
  ledger: `LoseLifeEffect` evaluates per player for each player only).

### Step 9, run 9 (2026-11-22, Each closed)

The defect run 8 assigned here, the six shared blockers it named with the records and ledger rows waiting for them, the rows it
listed as narrowed, then the rest of the Each family's rare records. Ratchet 4,491 / 3,729 → 4,450 / 3,686 records / handlers (45
records and 47 handlers deleted, 4 and 4 added). 76 card files changed by scratch scripts (Wernog only lost an unused import). Oracle
text checked with `get_card` for all 76; rulings with `get_rulings` where the handler or the text left a doubt. CR 101.4, 104.4a, 111.7,
121.2c, 303.4f, 303.4g, 400.3, 603.10a, 605.3a, 608.2c, 608.2d, 608.2e, 611.2c, 701.20b, 701.23a, 701.23b, 701.24d, 701.55b and 701.55d
checked with `get_rule` (2026-11-22).

| Family | Audited | Absorbed | Split | Survivor | Dead | Blocked (cause) | Unique | Left unaudited |
|---|---|---|---|---|---|---|---|---|
| Each | 79 rows (the 69 rare records, the head of the search queue, ledgered rows, four survivors) | 23 | 14 | 4 (two new records) | 1 | 35: 33 player loop (each read: choose in turn, then act, or one offer per player through its own continuation), 2 other (step 10's copy verb; one card) | 2 | no rare record; 6 records of three or more cards |
| Other families (the rows waiting for the steps, and their survivors) | 22 rows | 3 | 4 | 13 (two new records) | 0 | 2 other | 0 | Exile none, Reveal 120, Choose 99, Destroy 40, Search 40, Set 27, Shuffle 18. All families: 2,617 rare records without a row |

- **REVEAL.** `RevealTopCardOfLibraryEffect` takes players and a count per player and records REVEAL. `cardsThisWay(REVEAL)` names a
  card while it is in that library (CR 701.20b); `PutCardsOntoBattlefieldEffect` (new, through the entry batch), the put-into-hand, the
  put-into-graveyard and the exile-cards steps move it. `SetTriggeringSpellXValueEffect` is Naya Soulbeast's second half. The seven Each
  records and the two ledgered reveal-to-hand records (10 cards) went.
- **Per-player library search.** New `EachPlayerSearchesLibraryEffect` (players, filter, a count per searcher, destination, may) on the
  existing queue: what was found moves together, then who searched shuffles. Seven records and Field of Ruin's went, with three bespoke
  queues of `LibrarySearchFollowUp` and a `GameData` field. Boldwyr Heavyweights and From the Ashes keep theirs (see the ledger).
- **Any amount of mana.** New `EachPlayerPaysAnyManaEffect`, recording PAY_MANA with each player's part; five copies of the loop went.
- **Players.** `PlayerPredicate`s for controls a matching permanent, the most creatures, attacked by the source, no cards in hand and a
  minimum of life lost; `ControllerLosesGameEffect` and a token's controller take several players.
- **Shuffle into library.** `EachPlayerShufflesZonesIntoLibraryEffect` takes players and zones and records SHUFFLE_INTO_LIBRARY per
  player; five records went, the Whirlpools' ledgered one among them.
- **Shared parameters.** A total mana value stops the exile-until step; `LoseLifeEffect.eachTheirOwn` took the four ledgered loss records;
  the hand choice that perpetually changes a card took three siblings (its one prompt was private and already applied all of it, so no
  hand subject was needed); Reap Intellect is steps. `TargetPlayerChoosesOneEffect.villainousChoiceFacedBy` took the four each-opponent
  villainous choices (CR 701.55d: one player's whole process at a time, so no choose-then-act loop).

**Behaviour changes.**

1. The Legend of Yangchen's chapter I offers every player the permanents the Saga controller's opponents control (Oracle: "your
   opponents"), and the chosen are exiled as one event (its ruling).
   `TheLegendOfYangchenTest.chapterIOpponentChoosesFromAmongThePermanentsTheSagaControllersOpponentsControl`,
   `.chapterIExilesTheChosenPermanentsAsOneEvent`.
2. Over the Top's card that can't enter stays on top, unshuffled (CR 701.20b; nothing says shuffle).
   `OverTheTopTest.blockedPermanentCardStaysOnTop`. Clear the Land and the per-player search put cards onto the battlefield through the
   entry batch, which asks copy and Aura choices and leaves a blocked card where it is (CR 303.4f, 303.4g).
3. Under Aven Mindcensor a player who may search is asked whatever the searchable cards and those below them hold, and nothing is
   logged of either (Oracle: "may search"; CR 701.23a; New Frontiers, Natural Balance, Hired Giant and the queue's other may-searches).
   The next player then searches and what was found moves at the end; HEAD logged a miss and dropped both. (The run's first tree skipped
   the choice and logged the miss; corrected in the orchestrator's review.)
   `NewFrontiersTest.mindcensoredMaySearchDoesNotDependOnTheLibrary`, `.decliningUnderMindcensorNeitherShufflesNorSearches`,
   `.restrictedSearchWithNoMatchContinuesToTheNextPlayer`, `.oppositionAgentIsOfferedTheEmptyMaySearch`,
   `HiredGiantTest.mindcensoredOpponentStillChooses`.
4. A "may search" by a player who can't search isn't chosen, so that library isn't shuffled (CR 608.2d).
   `NewFrontiersTest.playerWhoCantSearchDoesNotShuffle`. A may-searcher with an empty library isn't recorded as having searched.
5. Turtle Tracks' search is a may: who takes nothing hasn't searched (Oracle). `TurtleTracksTest.decliningIsNotSearching`.
6. Enigma Ridges' search is not a may: who finds nothing has searched (Oracle). `EnigmaRidgesTest.failingToFindStillSearches`.
7. Found cards move together also for Old-Growth Dryads, Hired Giant, Turtle Tracks, Enigma Ridges and Scholarship Sponsor (CR 608.2e).
   Noble Benefactor's log doesn't name the card (Oracle: no reveal). `NobleBenefactorTest.searchForAnyCardRevealsNothing`.
8. Demolition Field's two searches are two instructions, the first player's land entering first (CR 608.2c); the controller of a land
   that wasn't destroyed still searches (its ruling). `DemolitionFieldTest.controllerOfALandThatWasNotDestroyedStillSearches`.
9. The owned permanents of all players shuffled away leave as one event (CR 608.2e, 603.10a; The Great Aurora, Sway of the Stars).
   `TheGreatAuroraTest.everyPlayersPermanentsLeaveAsOneEvent`. A token in a graveyard isn't shuffled in (CR 111.7).
10. Draws, token creation and mills after a join-forces payment are in APNAP order (CR 121.2c, 101.4) and the mills are one event. No
    new test: the cards' tests cover the amounts.
11. Reap Intellect searches as the same-name step does (the searcher alone sees the zones searched, CR 701.23a) and, with nothing exiled
    from the hand, doesn't search (its ruling). `ReapIntellectTest.nothingExiledMeansNoSearch`.
12. A multi-player "loses the game" goes on past a player whose loss is replaced (Oracle: each loses). No test: it needs three players.
13. AI only: Field of Ruin and Demolition Field are destroy spells with steps, Bonder's Ornament isn't scored as its own draw, the
    up-to-N draw is a card-drawing effect for the players it names.
14. Log lines and prompts are the steps'.
15. Borderland Explorer's search is a may in the queue too: asked with nothing to find, and who takes no card neither shuffles nor has
    searched (Oracle). `BorderlandExplorerTest.maySearchIsOfferedWithNothingToFind`.
16. Casting from the library during a may-search is searching (Panglacial Wurm's ruling), and no longer ends the queue when it was the
    last card offered; a searcher whose find Opposition Agent exiles shuffles with the others (Oracle: "each player who searched").
    `NewFrontiersTest.castingWhileSearchingContinuesTheQueue`, `.searcherUnderOppositionAgentStillShuffles`.

**Seen, not changed.**

- The queue's may-searches (New Frontiers, Natural Balance, Hired Giant, Old-Growth Dryads, Veteran Explorer, Weird Harvest, Noble
  Benefactor, Turtle Tracks, Demolition Field, Borderland Explorer, Wave of Vitriol, From the Ashes) have no separate "do you search?":
  the searcher (under Opposition Agent, the Agent's player) is shown the matching searchable cards with the offer, and only taking one
  or casting while searching counts as searching. So a player can't choose to search and find nothing (to shuffle, or under Aven
  Mindcensor with nothing in the top four), and has seen those matches on declining (CR 701.23a, 701.23b). An empty library gets
  the offer only where searches are watched or the find is revealed. Boldwyr Heavyweights asks first and was left out for that.
- Krenko's Buzzcrusher's may-search (its own queue, `startNextEachPlayerBasicLandSearch`) is still forced under Aven Mindcensor: it
  records a search, shuffles and logs "finds no" without asking (Oracle: "may search").
- Borderland Explorer reveals each find before the next player searches (its ruling: revealed simultaneously).
- Sunimret's reverse-miracle offer reads the bottom card also when only the top four may be searched.
- Opposition Agent's player can take a card that is offered only to be cast (Panglacial Wurm) as the find, and the AI's search
  strategy answers only for the searcher, not for the Agent's player.
- A cast that empties a multi-card search's offer drops what that search had accumulated (`continueLibrarySearchAfterLibraryAction`).
- Ol Rin's Searing Light and Gilded Ambusher exile each opponent's choice before the next chooses, Dispersal returns and discards one
  opponent at a time, The Second Doctor and Kwain draw before the next player chooses (CR 101.4, 608.2e).
- Winds of Abandon's player searches once for that many, where its text has one search for each creature.
- The two-player win declaration of "loses the game" doesn't make a draw of simultaneous losses (CR 104.4a).
- `ChooseCardsFromTargetHandEffect.exileAllCopiesOfChosenNames` and its continuation have no card left.
- Sothera and Descent into Madness still exile one at a time (run 8's note), and Summon: Valefor and The Horus Heresy as noted there.

**Tests edited.** In the review, `NewFrontiersTest.restrictedSearchWithNoMatchContinuesToTheNextPlayer` and
`NaturalBalanceTest.restrictedSearchWithNoTopMatchCompletes` answer the prompt they asserted was skipped (Oracle: "may search"), their
later assertions unchanged. The new cases above, and `ClearTheLandTest.cardsBelowTheRevealedFiveAreNotShown` (what a reveal shows).
`TheLegendOfYangchenTest.chapterIAllowsOpponentToExileTheSagaAfterBothPlayersChoose` is replaced by the opponent-choice case: it asserted
the defect. Log assertions follow the survivors' lines: `WeirdHarvestTest`, `HiredGiantTest`, the three Whirlpool tests. `MissyTest`,
`SycoraxCommanderTest` and `TheValeyardTest` name the option labels on the cards. `RevealTopCardOfLibraryEffectHandlerTest` builds the
handler with its two new collaborators and `CreateTokenEffectHandlerTest` stubs the resolver's list form. `TargetPolarityGuardTest` names
the replacing destroy. Deleted with their classes: four handler tests (their cases are in the card tests);
`DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffectHandlerTest`'s six support cases moved, unchanged, to
`LibrarySearchSupportEachPlayerBasicLandTest`.

**Tests run after the review's queue fix:** 129 classes (136 suites, 1,121 cases), all pass: every test class using Aven Mindcensor,
the queue's cards, Panglacial Wurm, Opposition Agent, a sample of `SearchLibraryEffect` (80), the library-choice and copy tests and
`EffectCountRatchetTest`. The larger run below was not repeated.

**Tests run** (one Gradle run of the tree before that fix, its XML deleted first, one `--tests` list per task, counted from the XML by class name):
944 classes (1,315 suites, 12,477 cases, 29 skipped by an assumption), all pass: the changed cards' tests and Struggle // Survive's, every
card test of the new steps and of the changed survivors of few cards, samples of `CreateTokenEffect` (120), `LoseLifeEffect` (100),
`SearchLibraryEffect` (90), `ExileGraveyardCardsEffect` (60), `MillEffect`, `DrawCardEffect` and `DestroyTargetPermanentEffect`; the
application's architecture tests and the application and AI tests naming a changed class. The application, AI, engine testFixtures and all
26 letters' card-test sources compile. Not run: the full suites, the AI fuzz and stress tests, the frontend. Not tested: anything that
needs three players (the villainous choice's order, several losers).

**For the next run (run 10, family Return: 97 rare records; then Put 97, Reveal 120).**

- The REVEAL step and its four movers now express much of the Reveal family's top-card tail (`RevealTopCardDrawManaValueEffect`,
  `RevealTopCardGainLifeEqualToManaValueEffect`, the matching-to-hand / else records); a Put run should read
  `PutCardsOntoBattlefieldEffect` first.
- Still blocked on REVEAL's other half: Kinship and the look-then-may-reveal records (the reveal follows a prompt), the reveal-until
  records (their own loop, no recorded step), the counter-on-match record (the triggering spell as a counter subject).
- The other villainous-choice records (Ensnared by the Mara, the Genesis, Great Intelligence and Master ones) can take
  `TargetPlayerChoosesOneEffect` in their families' runs; removing the dead hand-choice flag belongs to Choose.

### Step 9, run 10 (2026-11-22, Return mostly done)

The two search defects run 9 saw, the shared blockers of the Return family with the ledger rows that waited for them, then the family's
rare records. Ratchet 4,450 / 3,686 → 4,415 / 3,651 records / handlers (37 records and 37 handlers deleted, 2 and 2 added). 53 card files
changed by scratch scripts (Meren only lost an unused import). Oracle text checked with `get_card` for all 53 (an adventure half under its
front face); rulings with `get_rulings` where the handler or the text left a doubt. CR 101.2, 101.4, 108.2b, 110.2a, 111.8, 115.1, 205.1a, 303.4f,
303.4g, 400.2, 400.3, 400.4a, 400.7, 400.7e, 400.7j, 406.3, 406.3a, 603.3d,
603.6, 603.6a, 603.6c, 603.7c, 603.10a, 608.2d, 608.2e, 608.2h, 614.1c, 701.17c, 701.23a, 707.2, 708.2a,
708.3 and 708.9 checked with `get_rule` (2026-11-22).

| Family | Audited | Absorbed | Split | Survivor | Dead | Blocked (cause) | Unique | Left unaudited |
|---|---|---|---|---|---|---|---|---|
| Return | 87 rows (80 of the 97 rare records, ledgered rows, survivors) | 20 | 10 | 4 (one new record) | 3 | 14 other: each a gap one or two classes have (named in its note) | 36 | 17 rare records on the five shared blockers below; 27 records of three or more cards |
| Other families (waiting rows and the steps they use) | 8 rows | 0 | 4 (Mill) | 4 (one new record) | 0 | 0 | 0 | Mill none, Perpetually 57, Put 97. All families: 2,537 rare records without a row |

- **Chosen permanents to a hand.** New `ReturnChosenPermanentsToHandEffect` (candidates subject, minimum, maximum), recording RETURN. It
  took five records (10 cards) with their three prompts and contexts.
- **What a return left.** RETURN records each permanent returned to a hand as it last existed and the cards in that hand:
  `AmountThisWay(RETURN, POWER)`, and `cardsThisWay(RETURN)` for perpetual riders (digital only, as a sought card). Five records went with
  it, `PerpetuallyBoostCardEffect.toCards` and the new `PerpetuallyChangeCastCostEffect`.
- **That card, this card.** `triggeringCards()` also names the card an event exiled, face up or face down, and checks a single event card's stay;
  `sourceCard()` and `inYourGraveyard(cards)` are new. `PutCardsIntoHandEffect` takes graveyard cards.
  `PutCardsOntoBattlefieldEffect` takes a controller, face-down characteristics and what a card enters with (counters, effects without a
  duration). They took 15 of the family's records and the four Mill rows whose second half is "put into your hand".
- **Search queue.** `startNextEachPlayerBasicLandSearch`, its two follow-up components and the deferred-lands `GameData` field are gone
  with their only user, Krenko's Buzzcrusher.

**Behaviour changes.**

1. Krenko's Buzzcrusher's "may search" is the queue's: asked under Aven Mindcensor whatever the library holds, nothing logged of it, who
   takes no card hasn't searched (Oracle; CR 701.23a). `KrenkosBuzzcrusherTest.mindcensoredMaySearchDoesNotDependOnTheLibrary` (failed
   before the fix). What was found enters together (CR 608.2e). `.foundLandsEnterTogether`.
2. Borderland Explorer's finds are set aside and revealed together after the last search (its ruling).
   `BorderlandExplorerTest.findsAreRevealedTogether` (failed before the fix).
3. A card that left its graveyard or exile and came back is not returned by Ashnod's Intervention, Kaya the Inexorable, Soul Collector,
   Dread Slaver, Ashcloud Phoenix, Deathmist Raptor (face down), Missy, Princess Yue, Vraska, Yarus, It That Betrays (graveyard), Cleansing
   Meditation and Heart-Shaped Herb (CR 400.7).
   `AshnodsInterventionTest.doesNotReturnACardExiledAgain`, `SoulCollectorTest.doesNotReturnACardThatLeftTheGraveyardAndCameBack`,
   `DeathmistRaptorTest.doesNotReturnFaceDownAfterLeavingTheGraveyard`,
   `YarusRoarOfTheOldGodsTest.doesNotReturnACardThatLeftTheGraveyardAndCameBack`.
4. Permanents one instruction returns leave as one event (CR 603.10a): Wash Out, Dromar, Llawan, Trial, Cephalid Constable, Kazandu
   Stomper, Khalni Gem, Split the Party, the sweep spells and Rambling Possum. `WashOutTest.permanentsLeaveAsOneEvent`,
   `TrialErrorTest.trialReturnsAsOneEvent`, `CephalidConstableTest.targetsLeaveAsOneEvent`,
   `SplitThePartyTest.chosenCreaturesLeaveAsOneEvent`.
5. Mistblade Shinobi's target is chosen as the ability is put on the stack, the "may" as it resolves (CR 603.3d, 115.1; it chose among
   hexproof-checked permanents as it resolved). `MistbladeShinobiTest`'s four cases.
6. The returns now on the entry batch ask what a returned Aura enchants (It That Betrays' ruling; CR 303.4f, 303.4g) and a face-up card's
   as-enters choices. `ItThatBetraysTest.returnedAuraAsksWhatItEnchants`.
7. A card returned face down is seen entering (CR 603.6a; Yedora's Forest triggers landfall, Missy's Cyberman creature watchers) and
   none of its own as-enters choices is asked, nor is it named as it enters (CR 708.3).
   `YedoraGraveGardenerTest.returningForestTriggersLandfall`, `MissyTest.faceDownReturnAsksNoEntryChoice`.
8. Vraska's Treasure and Princess Yue's Moon are effects on the permanent: what Vraska makes it is no copiable value (CR 707.2, 205.1a;
   her ruling on what it keeps). `VraskaTheSilencerTest.payingReturnsCreatureAsTappedTreasure`.
9. Lae'zel's Acrobatics' delayed return doesn't return a card that left exile and was exiled again (CR 603.7c, 400.7).
   `LaezelsAcrobaticsTest.delayedReturnDoesNotFollowACardExiledAgain`.
10. A milled card a replacement exiled can be put into the hand also by Grub's Command, Free the Fae, Speak Secrets and Zoological Study
    (CR 701.17c). `GrubsCommandTest.milledGoblinExiledInsteadIsPutIntoHand`.
11. Heart-Shaped Herb doesn't offer a sacrifice with no creature to sacrifice, and takes a lone one unasked (CR 608.2d).
    `HeartShapedHerbTest.noCreatureToSacrificeDoesNotMakeMonarch`.
12. A destroyed, sacrificed or triggering token is no card to a card subject (CR 108.2b), and a perpetual rider after a return marks the
    returned physical card. `StarfallInvocationTest`'s token cases.
13. AI only: Grub's mill is benign as `MillEffect`'s target; Consign to Dream's polarity is its bounce half's; three dies / sacrifice
    returns no longer carry `ControlStealingEffect`, which the AI reads only on spells and enters abilities.
14. Log lines and prompt classes are the steps'.
15. "That card" finds a card exiled face down, in that stay: exile is a public zone (CR 400.2, 400.7e; CR 406.3 says only who may look).
    Athreos, Kaya's Ghostform, Ashnod's Intervention and Kaya the Inexorable keep what their old handlers did; It That Betrays gains it,
    whose handler skipped such a card. A card put onto the battlefield from there is face up unless the effect says otherwise.
    `AthreosShroudVeiledTest.returnsACreatureExiledFaceDownFaceUp` and `AshnodsInterventionTest.returnsACreatureExiledFaceDownWithoutNamingIt`
    use The Foretold Soldier's own exile; `TriggeringCardsFaceDownExileTest` is engine level, for the readers no card combines with it.
16. A filter of `triggeringCards` sees a card exiled face down without characteristics (CR 406.3a), and `CardIsPermanentPredicate` accepts
    a card without a type. Conspiracy Theorist is the only filtered user and none of this applies to it: its discard records set no exile
    stay, so it reads only the graveyard.
17. A permanent put into a graveyard and exiled from there in the same batch records no exile stay: "that card" of a dies trigger is
    looked for only in the first zone (CR 603.6c). `AthreosShroudVeiledTest.doesNotFollowADeadCreatureToItsSecondZone` (failed before).
18. The choice, perpetual cost and type, counter, suspend and plot steps don't name a card in exile face down in their log line
    (CR 406.3); the put into a hand already didn't.

**Conditions against the deleted handlers.** Stays: items 3 and 9 (CR 400.7, 603.7c). Tokens: no card to return (CR 108.2b, 111.8). The
graveyard and controller conditions of `inYourGraveyard` are the old handlers'. The entry batch's "can't enter" check is new on Athreos's
exile half and the returns whose handler had none (CR 101.2), and such a card, or an Aura without a host (CR 303.4g), stays without a log
line. Face down was the only narrowing without a rule behind it.

**Seen, not changed.**

- `ReturnDyingOpponentCreatureUnderYourControlEffect` (Necroskitter, Lim-Dûl the Necromancer, The Reaper, King No More), the four
  host-choosing Aura returns (Traveling Plague, Takklemaggot, Infectious Rage, Reins of the Vinesteed) and Gift of Immortality's creature
  return a card that left the graveyard and came back (CR 400.7: no stay check in their handlers).
- Infectious Rage returns under its owner's control; the instructed player is the ability's controller (CR 110.2a).
- Reins of the Vinesteed reads the dead creature's printed subtypes, not its types as it last existed (CR 608.2h).
- Veko adds Spirit where "becomes a Spirit" replaces the creature types (CR 205.1a).
- Kairi's bound reads a face-down permanent's card mana value (CR 708.2a), and it, Leadership Vacuum and Resounding Wave's cycling return
  one permanent at a time (CR 603.10a). Resounding Wave's cycling trigger also chooses up to two as it resolves where its text has two
  targets (CR 603.3d).
- Vault 13's chapter III asks a host for the first of several returned Auras only; the others stay in exile (CR 303.4f).
- Unfinished Business puts its two attachments onto the battlefield one after the other.
- Rambling Possum offers only saddlers its controller still controls; its text doesn't say "you control".
- A face-down entry's triggers are suppressed by the card's printed types, so a face-down Forest of a creature card counts as a creature
  to a "creatures entering don't cause triggers" effect (CR 708.2a).
- The chosen returns to a hand don't leave out a permanent that can't be chosen (`CantBeChosenEffect`, one playtest card whose text
  Scryfall didn't return here).

- `cardsThisWay(kind, filter)`, the readers that copy, offer to cast or read the name or mana value of a named card, and the entry
  batch's "can't enter" filter read a face-down exiled card's hidden face (CR 406.3a); no card points the first readers at
  `triggeringCards`.
- An entry from face-down exile that asks its controller a choice (an Aura's host) names the card in that player's prompt as it enters,
  also to a controller who couldn't look at it (CR 406.3); the opponent doesn't get the prompt (tested).
- Neither the old returns nor the entry batch check that the card is a permanent card, so a manifested instant that died can come back
  face up (CR 400.4a).

**Tests edited.** The new cases above. Renamed prompts, same assertions: `KhalniGemTest` (one prompt for both lands),
`StarfallInvocationTest`, `PatientNaturalistTest`, `PicklockPranksterFreeTheFaeTest`, `MostDecrepitOldBirdTest` and
`MonsterManualZoologicalStudyTest` (the choice step, answered by card id). `MistbladeShinobiTest` asserted the timing item 5 fixes.
`VraskaTheSilencerTest` asserted that the card's printed type changes (item 8) and now reads the layered type and subtypes.
`HeartShapedHerbTest` lost the answer to a choice with one option, and one case the offer CR 608.2d forbids.
`ReturnToHandEffectHandlerTest` builds `BounceSupport` without its unused collaborator; `CardImmutabilityArchTest` no longer lists
Vraska's handler. Deleted with their code: `LibrarySearchSupportEachPlayerBasicLandTest`, five queue cases of
`LibraryChoiceHandlerServiceTest` (the queue's card tests cover them), `ReturnDiscardedCardFromGraveyardToHandEffectHandlerTest` (Pitchstone
Wall's tests cover it) and `ReturnPermanentsOnCombatDamageToPlayerEffectHandlerTest` (it tested the choose-at-resolution branch item 5
removes).

**Tests run.** One Gradle run of the final tree, one `--tests` list per task, counted from that run's XML by file name: 640 classes
(955 suites, 9,492 cases, 29 skipped by `TriggerRouteRecordTest`'s assumptions), all pass: every changed card's tests and the fronts
constructing one, every card test of the new and changed steps, of the cards reading the changed subjects and of Aven Mindcensor, Panglacial
Wurm, Opposition Agent and Stormfront Riders, samples of `ReturnToHandEffect` (110), `ReturnCardFromGraveyardEffect` (90),
`SearchLibraryEffect` (90) and `MillEffect` (30), the application's architecture tests and the application and AI tests naming a changed
class. After it only a banner comment left a service test, which was rerun with `EffectCountRatchetTest`. The application, AI, engine
testFixtures and all 26 letters' card-test sources compile. Not run: the full suites, the AI fuzz and stress tests, the frontend. Not
tested: more than two players; a copy or other as-enters choice (beyond the Aura's) on the returns moved to the entry batch; that the new
cases other than the two search fixes fail on HEAD.

After the orchestrator's review (items 15 to 18): 12 of the 14 new cases failed before the fix (the two that pass both ways assert that
a hidden card which stays is not named), then one run of 177 classes (209 suites, 1,981 cases), all pass: the tests of every `triggeringCards` and `sourceCard` user,
of the readers of the exile stay and of the changed log lines, the card tests using Leyline of the Void or Rest in Peace,
`TriggeringCardsFaceDownExileTest`, `EffectCountRatchetTest` and `CardImmutabilityArchTest`. Not run: the other users of
`CardIsPermanentPredicate` (a null guard only) and of the zone-change record.

**For the next run (the Return family's 17 rare records, then Put 97, Reveal 120).** None of the 17 has a row, as each waits for something
three or more share; build these first:

- "up to N" on `ChooseCardEffect`: the three milled-offer payload records go with the Mill rows that build them
  (`MillControllerAndMayReturnMilled...`, the Squirrel, land, counter and perpetual ones, the any-number one). The second half is ready
  (`PutCardsIntoHandEffect` over graveyard cards); about 30 card tests answer one may per card today and will answer one choice.
- a `CardSubject` for exiled cards (exiled with the source, by name, with a counter) with that choice: Vault 13, Bag of Devouring,
  Imbraham, Sengir Nosferatu; also the ledgered Duplicity and Mysterious Limousine rows ("the cards exiled with the source").
- a creature-type choice step and predicates that read it and the chosen parity, as the colour step has: Raise the Palisade, the
  Haunting Voyage record, Zimone's Hypothesis; also ledgered Extinction Event and about 25 unaudited chosen-type records elsewhere.
- one "choose a card for each filter, then they return together" step: Sudden Reclamation, Gruesome Menagerie, Revival Experiment.
- the four engine-built payloads with baked card ids (`ReturnExiledCardToBattlefieldUnderOwnerControlEffect`,
  `ReturnExiledCardToHandEffect`, the two delayed graveyard returns): their builders move to `RegisterDelayed*` with
  `PutCardsOntoBattlefieldEffect` / `PutCardsIntoHandEffect` over the step's cards (both can express the payloads now).
- Narrowed, still ledgered or unaudited: the triggering-card return heads (`ReturnTriggeringCardFromGraveyardToBattlefieldEffect`,
  `ReturnDyingCreatureToOwnerBattlefieldEffect` and the two kindred heads) have their one path in `PutCardsOntoBattlefieldEffect`;
  `SacrificeUnlessReturnOwnPermanentTypeToHandEffect`'s return primitive records RETURN.

### Step 9, run 11 (2026-11-22, Return closed, Put only started)

The two defects run 10 saw, its five shared blockers with the records and ledger rows waiting for them, the Aura returns, and the first
Put record the new subjects unblocked. Ratchet 4,415 / 3,651 → 4,384 / 3,620 records / handlers (33 records and 33 handlers deleted, 2
and 2 added). 58 card files changed by scratch scripts. Oracle text checked with `get_card` for all 58 (a back face and an adventure half
under their front faces); rulings with `get_rulings` where the handler or the text left a doubt. CR 110.2a, 110.4, 400.2, 400.4a, 400.7,
400.7e, 400.7f, 400.7j, 401.4, 101.2, 108.2b, 406.3, 406.3a, 303.4f, 303.4g, 603.7b, 603.7c, 603.10a, 607.2a, 608.2d, 701.17c, 702.73a, 708.2a and 708.3 checked with
`get_rule` (2026-11-22).

| Family | Audited | Absorbed | Split | Survivor | Blocked (cause) | Unique | Left unaudited |
|---|---|---|---|---|---|---|---|
| Return | 25 rows (the 17 records, the Aura rows and their payloads) | 12 | 9 | 0 | 0 | 4 | no rare record; 27 records of three or more cards |
| Put | 1 row | 0 | 1 | 0 | 0 | 0 | 96 rare records |
| Other families (waiting rows and the steps they use) | 18 rows | 1 | 10 (Mill 6, Exile 3, Destroy 1) | 5 (two new records) | 2 other (notes narrowed) | 0 | Mill none. All families: 2,517 rare records without a row |

- **Up to N, one of each.** `ChooseCardEffect` takes a count (`exactly`, `upTo`) or one choice per quality (`oneOfEach`, `upToOneOfEach`)
  and records all chosen cards. With `cardsThisWay(MILL, filter)` and `PutCardsIntoHandEffect` it took the six milled-offer records
  (35 cards), their three payloads and may handlers, and the three "a card for each filter" records.
- **Exile subjects.** `exiledWithSource([players])`, `exiledCards([players,] filter)`, `cardsInGraveyards(players, filter)` and
  `except(cards, excluded)`; `PutCardsOnBottomOfLibraryEffect` is new. Vault 13, Bag of Devouring, Imbraham, Sengir Nosferatu, the Duplicity
  and Mysterious Limousine rows and Oblivion Sower went, with three bespoke prompts.
- **Choices kept on the entry.** The creature-type step (new) and the parity step keep their answer on the stack entry, read by three
  predicates. Three Return records, Extinction Event's row and the two parity destroy records went.
- **Engine payloads.** Three of the four are `PutCardsOntoBattlefieldEffect` / `PutCardsIntoHandEffect` over cards bound to their stays
  (`DelayedExiledCardReturns`, engine). The fourth stays: Seraph's link and Endless Whispers' target (ledger).
- **Aura hosts.** `attachedTo(permanents or players)` on the put step (Reins of the Vinesteed, Gift of Immortality's and Next of Kin's
  payload, Lynde's payload). Traveling Plague and Screams from Within needed no host: CR 303.4f's choice is the entering player's.
- **The put step records RETURN** with what entered, for "for each card returned this way" (`returned()`, `AmountThisWay(RETURN)`).

**Behaviour changes.**

1. An instant or sorcery card isn't returned face up from a graveyard or exile by the entry batch, `GraveyardReturnSupport`, the dies
   returns, the exile-until and delayed exile returns (CR 400.4a; face down it can be, CR 708.3).
   `NecroskitterTest.doesNotReturnAnInstantThatWasAFaceDownCreature`, `AthreosShroudVeiledTest` (same case, the batch),
   `LuminousBroodmothTest` (a dies head), `OblivionRingTest.instantThatWasAFaceDownCreatureStaysExiled` (all failed before the fix).
2. A card that left the graveyard and came back isn't returned by Necroskitter, Lim-Dûl the Necromancer, The Reaper, the four host-choosing
   Aura returns and Gift of Immortality (CR 400.7); the first group also asks whether the card can enter (CR 101.2).
   `doesNotReturn…ThatLeftTheGraveyardAndCameBack` in `NecroskitterTest`, `TravelingPlagueTest`, `InfectiousRageTest`, `TakklemaggotTest`,
   `ReinsOfTheVinesteedTest` and `GiftOfImmortalityTest` (all failed before). The same check was missing and is added in Necrotic
   Plague's return, the attached-to-the-entering-creature return (7 cards), Gift's and Ghoulish Impetus' registration (no test of their own).
   An ability of an Aura on "enchanted creature leaves the battlefield" now records the stay the Aura goes to, as on "dies" (CR 400.7f).
3. The delayed returns of an exiled card check its stay (CR 603.7c): Koya, Ugin's Spirit, Necropotence, Feather, Tatsumasa, the
   exile-and-return-at-the-next-end-step effect and its replacement in `PermanentRemovalService`, Rescue from the Underworld's exiled
   card. `KoyaDeathFromAboveTest.doesNotReturnACardExiledAgain`, `UginTheIneffableTest.doesNotFollowACardExiledAgain`. Ugin's is a
   delayed ability of Ugin's about the token, so it triggers also when the card has left (CR 603.7b).
4. What the put step now moves enters through the entry batch: as-enters and Aura choices are asked, a card that can't enter stays (the
   delayed exile returns, Rescue, Sengir Nosferatu, Vault 13, Mysterious Limousine, Timothar, Oblivion Sower). Vault 13 asks a host for
   each returned Aura (run 10's note). `Vault13DwellersJourneyTest.chapterIIIAsksAHostForEachReturnedAura`.
5. Ghoulish Impetus returns under its controller's control from whichever graveyard (Oracle: no "your graveyard"; CR 110.2a).
   `GhoulishImpetusTest.returnsUnderTheInstructedPlayersControl`.
6. Reins of the Vinesteed reads the dead creature's types as it last existed (its ruling; run 10's note).
   `ReinsOfTheVinesteedTest.readsTheDeadCreaturesTypesAsItLeftTheBattlefield`.
7. The creatures Extinction Event exiles leave as one event (CR 603.10a). `ExtinctionEventTest.creaturesLeaveAsOneEvent`. Mutinous
   Massacre reads a face-down creature's mana value as 0 (CR 708.2a). `MutinousMassacreTest.faceDownCreatureIsEven`.
8. The milled offers are one choice, made before any card moves (Oracle: "a card", "up to two", "any number"; CR 608.2d), and what is
   chosen leaves as one event. Cache Grab, Elvish Elegy and Vivien also find a milled card a replacement exiled (CR 701.17c).
   `CacheGrabTest.milledCardExiledInsteadCanBePutIntoHand`. Sparring Dummy's Lesson counts wherever it was milled to, as before.
9. Sudden Reclamation's two cards are chosen, then return together. Revival Experiment and Gruesome Menagerie keep their timing.
10. AI only: the parity destroys are board wipes, Mysterious Limousine's polarity is its exile step's, a milled offer is answered as a
    graveyard choice. Log lines and prompts are the steps'.

**What a player sees.** The new subjects show a chooser nothing new on these cards: `exiledCards` judges a face-down card without
characteristics (tested by Oblivion Sower's face-down land), `exiledWithSource` is offered for choice only by Vault 13 and Bag of
Devouring, which exile face up, and Duplicity's returns choose nothing and name no card that was face down in exile.
`ChooseCardEffect`'s Javadoc says a card points it only at cards the chooser may look at; nothing enforces that.

**Seen, not changed.**

- Puts outside the guarded paths don't ask CR 400.4a (their filters or sources name permanent cards in every case read here, but the
  handlers with their own loop weren't all read: `ReturnVoyagingCardFromExileEffect`, the exiled-permanent choice handlers, Meld).
- An Aura that went elsewhere as its host left and is put into a graveyard later is still found by "return this card": the awaited stay
  is the next one, whenever that is (CR 400.7f allows only the simultaneous or state-based put).
- `ForcedCostOrElseEffect`'s fallback resolves on a carrier entry, so it can't read step results; Koya's delayed ability is a
  `MayPayManaEffect` for that reason.
- Infectious Rage returns under its owner's control (CR 110.2a; run 10's note, its test asserts it).
- Lim-Dûl's returned Aura (a bestow card) enters through the old path without a host choice (CR 303.4f), as do the triggering-card heads
  except `ReturnTriggeringCardFromGraveyardToBattlefieldEffect`.
- `PutExiledCardOntoBattlefieldUnderControllerEffect` and the Mirror of Life Trapping return check no stay (CR 400.7).

**Tests edited.** The new cases above and `GameDataDeepCopyTest`'s. `BaseCardTest.chooseOfferedCards` is new. The 34 test classes of the
milled-offer cards answer one choice with the positions they accepted one may at a time; where one asserted between two answers, the
assertion now precedes the answer (`AirliftChaplainTest`, `BlanchwoodProwlerTest`, `CacheGrabTest`, `FallajiArchaeologistTest`,
`OstrichHorseTest`, `RavenousGigamoleTest`, `TomakulScrapsmithTest`, and Nashi's loop is one answer). Renamed prompts, same assertions:
`GruesomeMenagerieTest` (also the decline's message), `SuddenReclamationTest`, `SengirNosferatuTest`, `KianneTest`, `OblivionSowerTest`
(without the deleted prompt's owner), `Vault13DwellersJourneyTest` (and a second host in the Aura case: a lone host is taken unasked,
where the old prompt asked). `LibraryChoiceHandlerServiceTest` and `PendingInteractionContractTest` lost the deleted classes,
`SpecializedInteractionAiStrategiesTest` the case of Oblivion Sower's deleted prompt.

**Tests run.** The final tree in one Gradle run, one `--tests` list per task, counted from that run's XML by file name: 1,118 classes
(1,446 suites, 13,487 cases, 29 skipped by an assumption), all pass: every changed card's tests and the fronts constructing one, every
card test of the cards using the steps and the changed handlers, samples of the exile-until, flicker, graveyard-return, search,
create-token, persist, undying and mill effects (45 cards each) and of the effects whose handlers ask the support's entry check (10
each), `EffectCountRatchetTest` with the new baselines, the application's architecture tests and the application and AI tests naming a
changed class. After it only `DuplicityTest` and `Vault13DwellersJourneyTest` gained assertions and were rerun (26 cases). The ten
tests of the two defects failed before their fixes. The application, AI and engine sources with their tests, testFixtures and all 26
letters' card and card-test sources compile. Not run: the full suites, the AI fuzz and stress tests, the frontend. Not verified: more
than two players; that the other new cases fail on HEAD; a copy or other as-enters choice on the returns moved to the entry batch; cards
without a test class (Roost of Drakes, Support Skyforge).

**For the next run (run 12: Put, 96 rare records, not yet audited; then Reveal, 120).**

- Read with this run's parts first: the exiled-with-source Put records (`PutTargetCardExiledWithSource…` ×4, Kardum's, Hoarding Dragon's,
  Clone Shell's, The Spot's) need a target-in-exile subject or the linked/imprinted card in `exiledWithSource()`; the command-zone puts
  (5) a command-zone subject; the put-from-hand records (15) a hand subject for `ChooseCardEffect` with a private prompt.
- A "top card of a graveyard" subject and a top position (and a random order) for the library step would take the graveyard-to-library
  records (Soldevi Digger, Guiding Spirit, Stillness in Motion, Endurance and their siblings).
- The random-card puts (3) wait for a random choice on `ChooseCardEffect`; the engine payload
  `PutExiledCardOntoBattlefieldUnderControllerEffect` for its three makers.
- Narrowed, still ledgered or unaudited: about 25 chosen-type records can take `ChooseCreatureTypeAtResolutionEffect` in their families'
  runs; `CopyCardsExiledWithSourceAndMayCastCopiesEffect` and Mirror of Life Trapping's row name what is left for them.

### Step 9, run 12 (2026-11-22, Put closed)

The two defects run 11 saw, the shared blockers it named for Put with the records and ledger rows waiting for them, then the family's
rare records. Ratchet 4,384 / 3,620 → 4,343 / 3,579 records / handlers (42 records deleted, 1 added, the renamed library step among
both). 49 card files changed by scratch scripts. Oracle text checked with `get_card` for all 49 (a back face under its front face);
rulings with `get_rulings` for 22 of the cards and those whose handler left a doubt. CR 101.2, 108.2b, 400.2, 400.7, 400.7f, 400.7j, 401.4,
402.3, 406.3, 406.3a, 407.2, 603.3d, 603.7c, 608.2b, 608.2d, 608.2h, 701.17c, 704.3, 704.5m, 903.3 and 903.9b checked with `get_rule`
(2026-11-22).

| Family | Audited | Absorbed | Split | Survivor | Blocked (cause) | Unique | `step-9` | Left unaudited |
|---|---|---|---|---|---|---|---|---|
| Put | 106 rows (the 96 rare records, ledgered rows, survivors) | 19 | 18 | 14 | 25 other: each note names what is left; costs wait for step 10, two end-of-combat records for 5a's delayed-mechanism unification | 29 | 1 | no rare record; 26 records of three or more cards |
| Other families (the waiting rows) | 9 rows | 0 | 5 (Mill 3, Damage 1, MayPut 1) | 1 | 3 other (notes narrowed) | 0 | 0 | Reveal 120, not started. All families: 2,420 rare records without a row |

- **Target in exile.** `targetExiledCard(filter[, owner])` and `targetCardExiledWithSource(filter)`; the put steps declare the target of
  their subject (`CardSubject.targetSpec()`). The exile leaf of `TargetPredicate` carries the owner and the ante zone, so four
  validators and the Pull from Eternity check are gone.
- **The linked card.** `exiledWithSource()` needed no imprint fallback: every production imprint path links the exile entry. Clone
  Shell's fallback to an unlinked imprinted card is deleted (it was the CR 400.7 hole).
- **Hidden and command zones.** `cardsInYourHand` / `cardsInHands`, `cardsInYourLibrary` (random choice only), `cardsInYourCommandZone`
  with `CardIsCommanderPredicate`, `topOfGraveyard`. `ChooseCardEffect` takes a chooser (`by`) and a random choice (`atRandom`).
- **The library step.** `PutCardsOnBottomOfLibraryEffect` is `PutCardsIntoLibraryEffect` now (top, bottom, random order; from a hand
  too), recording the new `StepKind.PUT_INTO_LIBRARY`. With it the three ledgered hand-to-library rows went (9 cards).
- **By parameters.** Four put-from-hand records are `PutCardToBattlefieldEffect` with a predicate or bound; Yuna's counters are
  `PutCounterOnTargetPermanentEffect`.

**Behaviour changes.**

1. "Return this card" of an Aura's ability on the enchanted permanent leaving finds the Aura only in the stay it got at the same time
   or by the state-based action (CR 400.7f), not in a graveyard it reaches later. `TravelingPlagueTest`: the exiled and the
   came-back case failed before the fix; the same-time and the state-based batch case guard what must still be found.
2. Rona doesn't put a land that was exiled again as the offer stood (CR 400.7; `ExileCardAtRandomEffect` binds the stay).
   `RonaHeraldOfInvasionTest.doesNotPutALandExiledAgain` (failed before). Run 11's note on Mirror of Life Trapping was wrong: the link
   ends as the card leaves exile. `MirrorOfLifeTrappingTest.doesNotReturnACardExiledAgain` passes on HEAD.
3. Face-down exiled cards aren't shown or named: the prompts of Ruin Processor's and Ulamog's Nullifier's choices (and of the five cards
   on the first's head), the log of the two return-all-exiled-with-source hand records and of the random one (CR 406.3).
   `RuinProcessorTest`, `UlamogsNullifierTest`, `BottledCloisterTest`, `KardumPatronOfFlamesTest` (all four failed before).
4. The Spot does nothing once it left the graveyard and came back; Hoarding Dragon and Clone Shell don't take a card exiled again;
   Grinning Totem's cleanup doesn't follow one (CR 400.7, 603.7c). A test in each card's class.
5. A card in the ante zone is no target "in exile" of any effect but the ante exchange (CR 407.2).
   `BladeOfTheSwarmTest.cannotTargetAnAntedCard`.
6. Road of Return moves only a commander (Oracle "your commander", CR 903.3; its ruling).
   `RoadOfReturnTest.commanderModeLeavesACardThatIsNoCommander`. Derevi put from the command zone is a commander on the battlefield.
   `DereviEmpyrialTacticianTest.isACommanderOnTheBattlefield`.
7. Jhoira, and Arcade Gannon through the same predicate, read the source's counters as last known once it left (CR 608.2h).
   `JhoiraAgelessInnovatorTest.readsTheCountersJhoiraLastHad`.
8. Lluwen's offer is one choice (Oracle "a ... card") and finds a milled card exiled instead (CR 701.17c).
   `LluwenImperfectNaturalistTest.milledCardExiledInsteadCanBePutOnTop`.
9. Valakut Awakening and Into the Fire choose the cards at once, without a number first, and then order them (CR 608.2d, 401.4).
10. What the put step now moves enters through the entry batch (as-enters and Aura choices; a card that can't enter stays, CR 101.2):
    Gyruda, Your Mistake Is My Triumph, Rona's land, Derevi, Tariel, Manabond and the split hand puts.
11. Log lines and prompt classes are the steps'.

**What a player sees.** The hand choice is a prompt to the hand's owner alone and offers only their cards; its log line and the library
step's name no card of a hand, and the random library pick shows and names only the card that reaches the graveyard
(`PendantOfProsperityTest`, `FoldIntoAetherTest`, `ValakutAwakeningTest`, `GeistOfRegretTest` read both players' messages or the log).
The other Put handlers' log lines into or out of a hidden zone were read: beyond item 3 none names a hidden card.

**Seen, not changed.**

- The other prompts built on the projection's `exiledCardViews` (about 17) show a face-down exiled card's face to the chooser; not
  checked which of them can offer one (CR 406.3).
- Liege of the Tangle chooses its target lands as the ability resolves (CR 603.3d).
- Gruesome Encore exiles at your next end step where its text says the next end step (the absorb-ready row fixes it).
- The opponent-owned processor head and Ulamog's Nullifier offer face-down cards one by one where Ruin Processor chooses piles.
- An Aura attached elsewhere before the state-based check and put into the graveyard later for being unattached is found by the first
  host's ability: CR 400.7f's second sentence read by its words.
- `PutCardToBattlefieldThenEffect` judges a predicate without X or the source's last-known state when its entry has no snapshot.
- Dead now and left: `MultiGraveyardChoice.reorderToLibraryTop` with its handler branch, `beginCardChoiceWithArtifactCounters` and
  `beginCardChoiceThenReturnSourceToHand` with their contexts.

**Tests edited.** The new cases above. Renamed prompts and answers, same assertions: `FoldIntoAetherTest`, `PendantOfProsperityTest`,
`OriginOfTheAvengersTest`, `OviyaAutomechArtisanTest`, `HakbalOfTheSurgingSoulTest`, `MetathranAerostatTest` (the hand choice by card id),
`CurrencyConverterTest`, `CommandBeaconTest` (the choice step), `LluwenImperfectNaturalistTest` (item 8), `ValakutAwakeningTest` and
`IntoTheFireTest` (item 9). `HoardingDragonTest` and `CloneShellTest` link the card they set up as exiled with the source, as every
production path does (CR 400.7). `RoadOfReturnTest` designates the commander it moves and answers CR 903.9b's choice. The two service
tests build the replacement records. Deleted with its handler:
`PutTargetPlayerGraveyardOnBottomOfLibraryEffectHandlerTest` (Endurance's tests cover it).

**Tests run.** The final tree in one Gradle run, one `--tests` list per task, counted from that run's XML by file name: 982 classes
(1,210 suites, 9,746 cases, none skipped), all pass: every changed card's tests and the fronts constructing one, the card tests of the cards
using the changed steps, subjects, validators and exile target specs, every enchanted-permanent trigger card, samples of the put,
put-from-hand, counter and graveyard-return heads, the application's architecture tests with `EffectCountRatchetTest`, the application
and AI tests naming a changed class. The application, AI, engine and all 26 letters' card-test sources compile
on the final tree. Not run: the full suites, the AI fuzz and stress tests, the
frontend (the hand choice uses the graveyard multi-pick prompt; not looked at). Not verified: more than two players; that the new cases
other than the seven named as failing before fail on HEAD; a face-down card on the random hand return; the 24 picked classes without a
test of that name.

**For the next run (run 13: Reveal, 120 rare records).**

- The library step refuses a card already in a library: accepting cards still revealed there (REVEAL) would give the family's
  "the rest on the bottom" halves a step; Proteus Staff and Reality Scramble name what else they need.
- The hand subject, `by` and `atRandom` are there for the random reveals from a hand and the reveal-and-choose records; a step that
  shows a hand has to say who sees it.
- `PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect` is absorb-ready (`step-9`, the note names the builder calls and
  readers).
- Five Mill rows ledgered `blocked-step-model` in 4f (`MillControllerAndPutMilled…` and siblings) can split on MILL, `ChooseCardEffect`
  and the put step as Gyruda did; `ShuffleCardsFromHandIntoLibraryThenEffect` waits for a shuffle step over a card subject,
  `PutCardToBattlefieldThenEffect` (8 cards) for attacking entry on the put step.

### Step 9, run 13 (2026-11-22, Reveal closed)

The four defects run 12 left, the parts it named for Reveal with the ledger rows waiting for them, then the family's 120 rare records.
Ratchet 4,343 / 3,579 → 4,266 / 3,492 records / handlers (80 records and 90 handlers deleted, 3 and 3 added). 96 card files changed by
scratch scripts. Oracle text checked with `get_card` for all 96 (a back face under its front face); rulings with `get_rulings` for the
cards whose handler or text left a doubt (Genesis Storm, Aid from the Cowl, Matter Reshaper, A Premonition of Your Demise, Portent of
Calamity, Panoptic Mirror, Turtles Forever, Pact Weapon, Jace). The audit of the cards left unchanged read their text from the
card-data cache. CR 400.3, 400.7, 401.4, 402.3, 406.3, 603.3d, 603.12, 608.2c, 608.2d, 608.2h, 609.3, 614.1, 701.20a, 701.20b, 701.20d,
701.20e and 701.23b checked with `get_rule` (2026-11-22).

| Family | Audited | Absorbed | Split | Survivor | Blocked (cause) | Unique | `step-9` | Left unaudited |
|---|---|---|---|---|---|---|---|---|
| Reveal | the 120 rare records | 2 | 71 | 0 | 6 step-model (free cast of a revealed library card), 16 other (each note names what is left) | 25 | 0 | no rare record; 17 records of three or more cards |
| Other rows (the waiting rows, the defects' records, the steps) | 19 rows | 5 (Put 2, Look 3) | 2 (Reveal-and-discard, Proteus Staff) | 5 (three new records) | 3 step-model, 3 other (notes narrowed) | 0 | 1 (`RevealSubtypeFromHandEffect`) | Put none, Look 37. All families: 2,297 rare records without a row |

- **Reveal until.** `RevealCardsFromTopUntilEffect` (players, a filter, how many matches, at most) moves nothing and records REVEAL;
  `Subjects.revealedThisWay([filter])` and `restRevealedThisWay(filter)` name "that card" and "the rest" for the move steps.
- **Library step.** `PutCardsIntoLibraryEffect` takes cards still revealed in a library (CR 701.20b), and only those.
- **Hands.** `RevealCardsFromHandEffect` shows a hand, cards of it or cards at random to everybody, or with `seenOnlyBy` to exactly
  the players named (CR 701.20e, no card in the log). The three Look records of a hand folded into it. `DiscardCardsEffect` discards
  the named cards through the random discard's replacement path.
- **Readers.** `AmountThisWay` over REVEAL takes a card filter, power and toughness; a card filter of `cardsThisWay` reads X, the source's
  last-known state and the triggering spell's mana value; `HappenedThisWay.toACard(REVEAL)` judges with the game's state;
  `ColorManaSymbolsInRevealedCards` reads REVEAL; `SetBasePowerToughnessToAmountEffect` takes a duration; `ChooseCardEffect` can offer a
  card for one quality only (`offeringEachOnce`).

**Behaviour changes.**

1. A card pick's prompt carries the bounds its answer is checked against, set in one place from `legalOptions()`: the minimum of
   `MultiGraveyardChoice` (`ChooseCardEffect`'s `oneOf`, `exactly`, `oneOfEach`, `announced`, and every other mandatory form) and of the
   others with one. That also fixed four maxima: the optional exiled-card play (0, so nothing could be picked), a library-and/or-graveyard
   search for several (1), the graveyard-to-library cost (all cards), Illuminated Folio's pair (0). Three options contradicted their
   handlers and were corrected first: the exiled-spell copy may choose none ("you may copy", Panoptic Mirror's ruling), Turtles Forever
   may find fewer (CR 701.23b), a lone find in a graveyard is mandatory. `SpecializedInteractionProjectionTest` (two cases),
   `SuddenReclamationTest.mandatoryChoicePromptHasAMinimum`, `IlluminatedFolioTest.promptAsksForTwoCards` (all failed before).
2. No prompt over exiled cards shows or names a card exiled face down to a player who may not look at it (CR 406.3): every
   `exiledCardViews` prompt, Knowledge Pool's, the time-counter cost's and Brilliant Ultimatum's pile text.
   `SpecializedInteractionProjectionTest.exiledCardPromptsHideFaceDownCards`, `JestersScepterTest.newControllerDoesNotSeeTheCardsToPayWith`
   (both failed before). Which could offer one: the exiled-with-source cost (Jester's Scepter under a new controller: a real leak), and,
   with a mixed pool only (a copied or gained linked ability, a replaced exile), the cast-any-number and may-play prompts over
   exiled-with-source or counter pools, the exiled-spell copy and Knowledge Pool's; the time-counter cost for a face-down card with
   time counters. Which can't: the two creature-copy prompts, Koh's, Assimilation Aegis', the hit-counter and suspended-card choices,
   the activated exiled-card choice and Mirror of Fate's filter face-up cards; Espers to Magicite, Plargg and Nassari, Brilliant
   Ultimatum, Aminatou's Augury and Eye of the Storm offer what they just exiled face up or copies.
3. Liege of the Tangle targets as its ability goes on the stack (CR 603.3d), and its counters are put through the counter step, so
   replacement effects apply (CR 614.1). `LiegeOfTheTangleTest.targetsAreChosenAsTheAbilityGoesOnTheStack` (failed before).
4. Gruesome Encore exiles at the next end step, whoever's (Oracle). `GruesomeEncoreTest.exiledAtTheNextEndStepOfAnyPlayer` (failed
   before). The haste-and-exile return records the card's owner as the graveyard it entered from (CR 400.3), where it recorded the
   controller. No test of its own.
5. Genesis Storm may put the card (Oracle, its ruling). `GenesisStormTest.mayLeaveTheRevealedCard`.
6. Aid from the Cowl offers the bottom also for a permanent card not put onto the battlefield (ruling 2017-02-09).
   `AidFromTheCowlTest.declinedPermanentMayGoToTheBottom`.
7. Matter Reshaper's Aura with nothing to enchant goes to the hand (ruling 2016-01-22). `MatterReshaperTest`.
8. A Premonition of Your Demise's damage is a reflexive ability, targeted then (CR 603.12, ruling 2024-09-20). Its test.
9. Every card revealed is named to all (CR 701.20a): Old Stickfingers logged only a number, Genesis Hydra only a number and showed the
   cards to the chooser alone. `revealedCardsAreNamed` in both tests.
10. The discards of Amnesia, Infernal Kirin, Shatter Assumptions, Trapfinder's Trick, the sacrificed-colors record, Rag Man, Rise // Fall
    and Revealing Eye take the discard replacements (CR 614.1). `AmnesiaTest.discardReplacementApplies`.
11. What the put step now moves enters through the entry batch: as-enters, copy and Aura choices are asked (a lone host is taken
    unasked), several enter together, and what can't enter stays where it is (CR 101.2, 303.4g), not in the graveyard, the battlefield or
    the bottom. Dreamshaper Shaman, Getaway Barrel and Part in Friendship still bottom it with the rest.
12. Sacred Guide, Hermit Druid and the other reveal filters judge a card as it is in the library; "that card's power" reads the
    characteristic-defining amounts.
13. Prompts are the steps': Atraxa, Portent of Calamity (the free cast is a choice and then the cast's own yes or no), Niv-Mizzet (one
    pair after another), Genesis Hydra, Retraced Image, Revealing Eye. Sapling of Colfenor's life gain and loss are steps, so a death
    replacement is asked about after the reveal was shown. Log lines are the steps'.
14. AI only: the deleted records' polarity entries and two bespoke strategies went; those choices are answered as graveyard choices.

**What a player sees.** Read for each of the 120 records and the new steps. A reveal from a library is a log line naming each card,
from a hand that and the modal to every player; a look is the modal to its viewer alone and a line naming none
(`SlayersBountyTest.lookNamesNoCardInTheLog`, the viewer tests of Urza's Bauble and Port Inspector). Beyond items 2 and 9 only Invasion
of Alara's handler hid what everybody may see (below). Extract Brain's look is shown to its controller alone.

**The client after a rejected answer** (read, unchanged). `game-choice.service.ts` closes the picker and clears it before it sends;
`game.component.ts` handles no `ERROR` message and the server neither re-sends the prompt nor a state on a rejection, so the player has
no picker for a decision that still waits until a reconnect replays it. The minimum only avoids the rejection the picker knows of.

**Seen, not changed.**

- `MULTI_PERMANENT_PICK` prompts carry no minimum on the wire at all, the same dead end as item 1 (frontend and networking).
- Pact Weapon reveals the last card of a replaced draw and Jace, Mirror Mage the one card of a draw replaced by one draw; their rulings
  say neither is revealed.
- Invasion of Alara exiles nothing and names nothing when no card qualifies, and logs no names when one does (Oracle: exile).
- Sirocco's and Wand of Ith's discards skip the discard replacements (CR 614.1).
- `cardsThisWay(REVEAL)` still names a card a later step reordered within its library (CR 701.20d); no card reads it so.
- The index prompts over pooled exile cards (`graveyardIndexPick` with a card pool) weren't checked for CR 406.3.
- The view's implicit look permission (exiled face down with a permanent) and `mayLookAtExiledCard` are two answers to one question.
- Kaboom! and the family's free casts from a library keep their own loops (ledger).
- Dead now and left: `PortentOfCalamityState` with its two branches, the card-type follow-up's exile and graveyard forms.

**Tests edited.** The new cases above. Contradicted the text or a ruling: `GenesisStormTest` (answers the may), `AidFromTheCowlTest`
(the second offer), `MatterReshaperTest` (item 7), `APremonitionOfYourDemiseTest` (item 8), `LiegeOfTheTangleTest` (targets on the
stack; its two log assertions went with the bespoke lines), `GruesomeEncoreTest.delayedExileRetainsCasterAfterControlChange` (the next
end step is the opponent's) and its delayed kind. Renamed prompts and answers, same assertions: `AtraxaGrandUnifierTest`,
`PortentOfCalamityTest` (and the cast's yes), `GenesisHydraTest`, `RetracedImageTest`, `ConcealingCurtainsTest`,
`NivMizzetRebornTest` (one pair per answer: its rejections are per pair now, and the list of the ten revealed cards is asserted on the reveal's log line, in order).
`AidFromTheCowlTest`'s Aura case gets a second host (item 11). `GameEngineComponentScanTest` names another normal and may pair,
`SpellTriggersTest`, `AiTargetSelectorTest` and `ValidTargetServiceTest` build surviving records;
`SpecializedInteractionAiStrategiesTest` lost the deleted Niv-Mizzet prompt's case. Seven handler tests deleted with their handlers.

**Tests run.** The final tree in one Gradle run, one `--tests` list per task, counted from that run's XML by file name: 1,040 classes
(1,304 suites, 12,580 cases, 29 skipped by an assumption, 0 failures): every changed card's tests and the fronts constructing one, the
card tests of the reveal, choice, library, hand and put steps, samples of the graveyard-return, discard, graveyard-exile, may-play,
counter and copy heads, the application's architecture and interaction tests with `EffectCountRatchetTest`, the application, AI and
engine tests naming a changed class. The engine, AI, application, webservice and all 26 card-test modules compile. The eight tests of
the four defects failed before their fixes. Not run: the full suites, the AI fuzz and stress tests, the frontend. Not verified: that
the other new cases fail on HEAD; more than two players; a client round trip of the new bounds; each entry change of item 11.

**For the next run (run 14: Choose, 99 rare records).**

- `RevealSubtypeFromHandEffect` is split-ready (`step-9`, the note names the parts and the may handler's check to move).
- The family's reveal-then records can use the hand step, `revealedThisWay` and `DiscardCardsEffect` (the name-and-reveal records of
  Choose first: a chosen-name predicate over `revealedThisWay` is what they need).
- The reveal heads of three or more cards (17) now have steps that express most of them (top card to hand or battlefield,
  `RevealTargetHandEffect`); they are step 10's unless a run takes them.
- Two classes each wait for: a card subject on the copy effects, a delayed ability about a RETURN step, qualities found at resolution
  on `ChooseCardEffect`.

### Step 9, run 14 (2026-11-22, Choose started)

The defects run 13 left, `RevealSubtypeFromHandEffect`, the planar verdicts, then the first part of Choose: the chosen name with every
record it splits, the chosen player, and the hand and exile choices whose second halves existed. Stopped there: 80 of the family's 99 rare
records are left. Ratchet 4,266 / 3,492 → 4,246 / 3,472 records / handlers (21 records and handlers deleted, 1 added). 28 card files
changed by scratch scripts. Oracle text checked with `get_card` for all 28; rulings with `get_rulings` for Invasion of Alara, Pact Weapon,
Jace, Wand of Ith, Intellectual Offering, Armored Kincaller, Brain Pry, Spoils of the Vault, Tunnel Vision and Cursed Scroll. CR 201.4,
400.2, 402.3, 406.3, 608.2c, 609.3, 614.1, 614.6 and 709.4a checked with `get_rule` (2026-11-22).

| Family | Audited | Absorbed | Split | Survivor | Blocked (cause) | Unique | `step-9` | Left unaudited |
|---|---|---|---|---|---|---|---|---|
| Choose | 23 rows (19 rare records, 1 of three cards, 1 ledgered row, 2 steps) | 0 | 20 | 2 (one new record) | 1 other (note narrowed: no name on the same-name exile) | 0 | 0 | 80 rare records |
| Reveal | 7 ledgered rows | 0 | 1 (`RevealSubtypeFromHandEffect`) | 0 | 6 other (corrected from unique: a planar-deck card subject) | 0 | 0 | no rare record. All families: 2,278 rare records without a row |

- **The chosen name.** `ChooseCardNameAtResolutionEffect` takes excluded types and "other than a basic land card name", and the
  resolution keeps the name (`StackEntry.chosenCardName`) for `CardHasNameChosenThisResolutionPredicate`, read like the chosen creature
  type. Eleven records, ten choice contexts and their completions went.
- **The chosen player.** `ChoosePlayerEffect(players, label)` records CHOOSE for `playersThisWay(CHOOSE)`, unasked with one player.
- **By existing parts.** The four perpetual hand choices and two exiled-card choices are `ChooseCardEffect` and the verb over
  `cardsThisWay(CHOOSE)`. `RevealCardsFromHandEffect.atRandom` takes an amount.
- **Reveal a card from hand, if you do.** `MayEffectHandler` asks nothing for a may over a hand choice and its reveal when no card
  qualifies, and performs the may's else branch.
- **Planar.** Six records share one cause, so they are blocked-other, not unique: no card subject names the planar deck's cards. Not
  built: the subject needs the reveal, bottom-order and planeswalk steps to be of use.

**Behaviour changes.**

1. Sirocco's and Wand of Ith's discards take the discard replacements and preventions (CR 614.1). `discardTakesDiscardReplacements` in
   both tests (failed before).
2. Invasion of Alara exiles also when no card qualifies and names every card it exiles (Oracle "exile cards", CR 406.3).
   `exilesAndNamesCardsWhenNoneQualifies`, `namesTheExiledCards` (both failed before).
3. A card a replacement puts into the hand from the library is not a card drawn this way (CR 614.6; the rulings of Pact Weapon and Jace,
   Mirror Mage): `DrawCardEffectHandler` records only cards that were drawn, for every reader of the DRAW step. A test in both cards'
   classes with Sages of the Anima (both failed before).
4. The names' discards (Cabal Therapy, Cabal Therapist, Brain Pry, Nebuchadnezzar, Dementia Sliver) take the discard replacements and
   preventions (CR 614.1). `NebuchadnezzarTest.discardTakesDiscardReplacements`.
5. Brain Pry draws also when the discard can't happen (Oracle "If they can't"). `BrainPryTest.drawsWhenDiscardIsPrevented`. Its only
   matching card is discarded unasked. A discard that Library of Leng puts on the library is a discard, so no card is drawn, and none
   with the replacement declined (`discardToLibraryIsADiscard`, `declinedReplacementIsADiscard`; both passed without a fix).
6. "Has the chosen name" is either name of a split card in every converted record (CR 709.4a; nine compared the full name).
   `CursedScrollTest.splitCardHasEitherChosenName`.
7. The name prompt of this step offers the catalog's names and those of cards the chooser can know: no card of another hand or library,
   no face-down face (CR 400.2, 406.3). `CursedScrollTest.offeredNamesLeaveOutHiddenCards`. From the stack it offers no spell cast face
   down (CR 708.4) and no source of an ability that is or last was face down on the battlefield or lies face down in exile (CR 708.2,
   406.3; added on review): the five `offeredNamesLeaveOut…Spell` / `…Source…` cases of `CursedScrollTest`, all failing before that fix,
   over cards no catalog lists (in the application the catalog's names would hide the difference for an implemented card). Every
   converted prompt, and the four cards that had the step, now offer the catalog's names; a name of an excluded type is rejected
   everywhere.
8. A perpetual cost change doesn't name a card in a hand or library in the log (CR 400.2).
   `BloodsproutTalismanTest.logDoesNotNameTheChosenHandCard`.
9. Entries and moves are the steps': what is put into a graveyard or exile from a library goes through their services, Lockbox's and
   Isperia's riders read the recorded reveal, the two players of the Offerings draw, untap and create in one step in APNAP order.
   Prompts, log lines and answers by card id are the steps'.
10. AI only: the dig's name strategy reads the parked steps and names only an offered, choosable card. The step's other names come from
    what the AI knows, not the catalog's first (added on review): most held in its own hand or library where the effect reveals from
    there, else most seen of the others' public cards, then of any, then of its own. `ColorChoiceAiStrategyTest`'s seven new cases over a
    30,000-name list failed before. Cursed Scroll's, Magus of the Scroll's and Diviner's Lockbox's damage and draw are inside a
    condition, which the AI's value readers don't look into.

**1a, the pooled index prompts.** All 25 creators of a `GraveyardChoice` with a pool were read. The wire message carries the indices,
the prompt and a flag, no card and no name, and no creator's text names a pooled card, so there was nothing to mask and no test could
fail first. `SpecializedInteractionProjectionTest.pooledIndexPickSendsNoCard` guards that (passes on HEAD).

**1d, what is still missing for a replaced draw.** A draw replaced by exactly one draw of the same player (Enduring Renewal's
"otherwise", Aladdin's Lamp, Chains of Mephistopheles, Breathstealer's Crypt, a draw from the bottom or from anywhere) still reveals.
`DrawService` applies some 20 replacements, each in its own branch and several after a prompt, and reports none of them: it needs a
"this draw was replaced" record on the draw that the branches set and that survives the pauses, a mechanism of its own.

**What a player sees.** The hand choice of the perpetual and reveal steps is the owner's prompt alone and its log names no card; the
revealed card alone is shown to all (`ArmoredKincallerTest`, `BloodsproutTalismanTest`). The exile choices mask face-down cards. The
name prompt: item 7.

**Seen, not changed.**

- Other name prompts still offer the names of every card in the game, hidden hands and libraries included, with no catalog around them
  to hide it: Comply and Academic Probation, The Clone Saga's chapter, `CreateTokenWithChosenNameEffect` (CR 400.2). The prompts of
  `PlayerInputService` add the catalog, which covers those names only where every card is in it.
- The stack view is one for all players and carries a face-down spell's card and description: `GameViewProjectionFactory.getStackViews`
  passes `entry.getCard()` to the view factory unmasked (CR 708.4, 708.5; read, not reproduced).
- The AI's other name strategies read an opponent's hand (Null Chamber, Comply) or library (the name-and-mill guesses).
- The perpetual hand choices left (offspring, the two triggered-ability grants, the shield static, Pull of the Mist Moon) name the
  hidden card in the log (CR 400.2).
- Three draw handlers beside `DrawCardEffectHandler` record every new card in hand as drawn (each opponent draws, the two
  draw-per-permanent records; CR 614.6).
- The client shows graveyards for an index pick over a pool, so a pooled card in exile can't be picked there (frontend).
- `MULTI_PERMANENT_PICK` still has no minimum on the wire, which a permanent choice step will need.

**Tests edited.** The new cases above, `ChoosePlayerEffectHandlerTest` and the chosen name in the AI's `GameDataDeepCopyTest`. Renamed
contexts, prompts and answers, same assertions: the name cards' tests, `ArmoredKincallerTest`, `SkullwinderTest`, `MuseVesselTest`,
`AuthorOfShadowsTest`, the four perpetual cards' tests (an index becomes that card's id; an out-of-range index an unknown id, rejected
with `IllegalStateException`), `ColorChoiceInteractionHandlerTest` (the step's context), `StepTriggerServiceTest` (builds the split
steps), `PendingInteractionContractTest` (the dead state is gone). Contradicted the step's rule: `BrainPryTest` answers no prompt for a
single matching card; its rejections of an empty, illegal or wrong player's answer moved to the two-card case.

**Tests run.** The final sources in two Gradle runs, one `--tests` list per task, with no source change between them (the second
added the engine's component-scan test; tasks with unchanged filters kept their results). Counted from those runs' XML by the test cases'
class names, as long file names are shortened: 716 classes (1,125 suites, 11,104 cases, 29 skipped by an assumption, 0 failures): every
changed card's and changed test class, card samples of the hand reveal, discard, choice, may, draw, draw-replacement, play-permission and
put-into-graveyard users, the application's architecture tests with `EffectCountRatchetTest`, its interaction tests and the application
and AI tests of the changed mechanisms. Every project's main and test sources compile on the final tree. Failed before their fixes: the
seven tests of items 1 to 3. Not run: the full suites, the AI fuzz and stress tests, the frontend. Not verified: that the other new cases
fail on HEAD; the player choice through a game of more than two players (its handler test only); the size of the catalog-wide name list
on the wire in the application; the AI's answers to the new prompts.
After the review's fixes the touched classes ran again in one Gradle run: 48 classes (80 suites, 679 cases, 0 failures), the changed
cards' and changed tests, the name step's cards, the prompt, AI and component-scan tests and `EffectCountRatchetTest`.

**For the next run (run 15: Choose, 80 rare records).**

- First, the other "choose a card name" prompts (Comply and Academic Probation, The Clone Saga's chapter, the named-token effect, and
  `PlayerInputService`'s collectors): offer what `collectCardNamesKnownTo` offers and no hidden hand, library or face (CR 400.2, 406.3,
  708.4), with the AI's answers to them reading no opponent's hidden zone.
- A choose-permanents step recording CHOOSE (count, a minimum, "different" power or mana value) with `thisWay(CHOOSE)` unblocks about
  fifteen: the three choose-and-destroy-the-rest records, the three different-quality records, up to one permanent, the counters, boost,
  token-copy, block, attach and two-creature records, and the ledger's `ChooseOpponentCreatureAndPerpetuallyBoostEffect`.
- `ChoosePlayerEffect` is there for Boreas Charger (a search count), Multiple Choice (a chooser on the chosen return), Fateful Handoff
  and Discerning Financier (a player on the gain-control step), Backdraft and `ChooseOpponentGainsControlOfSourceEffect`.
- A perpetual grant step over a card subject (abilities, static, damage, blitz, casting grants) unblocks seven hand choices, a conjure
  step over a card subject Holographic Double and Nashi, a void-counter card predicate Dauthi Voidwalker.
- The chosen name is recorded: a name on `ExileCardsWithSameNameEffect` frees Thought Hemorrhage and the ledgered name-exile head;
  the three lasting name restrictions need their verb to read the entry's name (and their prompts fixed as above).
- The two causes run 13 named with two classes each (a copy over a card subject, qualities found at resolution) got no third here.

### Step 9, run 15 (2026-11-22, Choose partly done)

The three defects run 14 left, the choose-permanents step, the perpetual ability grant and some users of the player and name steps. Stopped
there at a compiling, tested point: 64 of the family's 80 rare records are left. Ratchet 4,246 / 3,472 → 4,227 / 3,454 records / handlers
(21 records and 20 handlers deleted, 2 and 2 added). 29 card files changed by scratch scripts. Oracle text checked for all 29 with
`get_card` (a back face under its front face; the specialized face of Alora from the card-data cache, which the tool doesn't give);
rulings with `get_rulings` for Duneblast, Gix's Command, Haphazard Bombardment, Sigardian Zealot, Sigarda's Vanguard, Zenos, The Day of
the Doctor, Multiple Choice, Predict, Lammastide Weave and Struggle for Project Purity. CR 101.4, 201.4, 201.4a, 201.4b, 202.3b, 400.2,
406.3, 608.2d, 608.2h, 609.3, 614.6, 701.17c, 708.2, 708.4 and 709.4a checked with `get_rule` (2026-11-22).

| Family | Audited | Absorbed | Split | Survivor | Blocked (cause) | Unique | `step-9` | Left unaudited |
|---|---|---|---|---|---|---|---|---|
| Choose | 18 rows (16 rare records, 1 ledgered row, 1 new step) | 1 | 16 | 1 (new record) | 0 | 0 | 0 | 64 rare records |
| Other families | 8 rows (name-and-mill 2, perpetual grants 3, three survivors with a subject) | 1 | 3 | 4 (one new record) | 0 | 0 | 0 | Perpetually 55 rare records. All families: 2,258 rare records without a row |

- **Choose permanents.** `ChoosePermanentsEffect` (a candidates subject, a minimum, a maximum, a quality that must differ) records
  CHOOSE; `thisWay(CHOOSE)` is now a permanent subject too. "The rest" is `DestroyAllPermanentsEffect.sparing(filter, subject)`, whose
  boolean for created permanents became that subject, or `except(all, thisWay(CHOOSE))`. The boost, the token copy, the perpetual boost
  and the combat requirement take the chosen permanents.
- **Perpetual abilities.** `PerpetuallyGrantAbilitiesEffect` over a card subject; the choice, random or the player's, is `ChooseCardEffect`.
- **Player.** A player `playersThisWay` names can gain control of a target (`TargetPlayerGainsControlOfSourceCreatureEffect`).
- **Names.** One collector (`PlayerInputService.cardNamesOfferedTo`) and one check of the answer (`ChoiceHandlerService.isCardNameAllowed`)
  for every name prompt; `LibraryRevealSupport`'s collectors are gone. The two name-and-mill records moved onto the step, with
  `AmountThisWay.milledManaValue`. The other users keep their records: each needs its second half to read the entry's name.

**Behaviour changes.**

1. Every "choose a card name" prompt (as a permanent enters, Null Chamber, the two exile-by-name records, Mindblaze, Sphinx Ambassador,
   Liar's Pendulum, Comply and Academic Probation, Conjurer's Ban, The Clone Saga, the named token, the two reveal-top guesses) offers the
   catalog's names and those of the cards its chooser can know: no other hand or library, no face-down face (CR 400.2, 406.3, 708.4).
   `CardNamePromptsTest.offersNoHiddenName` (17 prompts; failed before), `LammastideWeaveTest.offeredNamesLeaveOutHiddenCards`.
2. Each takes only a card's name (CR 201.4, 201.4a): no made-up name (Null Chamber, Sphinx Ambassador, Liar's Pendulum, Comply and the
   name-and-mill took any), no token's name, no name outside the revealed cards (Alhammarret), no other basic land's at Null Chamber. The
   named token still takes any name ("name it" is no card name). `takesOnlyACardName`, `takesNoTokenName`, `takesOnlyARevealedName`
   (failed before where the old prompt took it). A name is no longer taken for being that of a card in a hidden hand or library, so a
   card no catalog lists is nameable only where it is offered; in the application every card is listed. The excluded-type check still
   looks at the game's cards of that name (as run 14's step did).
3. "Has the chosen name" is either name of a split card also for the exile-by-name records, Thought Hemorrhage, Sphinx Ambassador, the
   targeted reveal and the mills (CR 709.4a). `CranialExtractionTest.splitCardHasEitherChosenName` (failed before).
4. The five perpetual hand choices don't name the card in the log (CR 400.2). `logDoesNotNameTheChosenHandCard` in the five tests (failed
   before).
5. The three other draw handlers record only cards drawn (CR 614.6): Struggle for Project Purity no longer draws for a card Sages of the
   Anima put into an opponent's hand. A test in the three cards' classes (failed before). Unlike `DrawCardEffectHandler` they have no
   "more than instructed" cap: "each card drawn this way" counts every card of a draw replaced by drawing more.
6. Haphazard Bombardment chooses exactly four while four qualify (Oracle). `cantChooseFewerThanFour` (failed before).
7. The multi-permanent prompt carries a minimum for this step and for `ReturnChosenPermanentsToHandEffect`, equal to what the answer is
   checked against, in `legalOptions()` and on the wire. The same test.
8. The choice's log names no face-down permanent's card (CR 708.2). `GixsCommandTest.logDoesNotNameChosenFaceDownCreature` (failed before).
9. The converted choices: a permanent that can't be chosen is neither offered nor taken unasked; a choice with one possible answer is
   unasked (Deadly Vanity with one candidate); a transformed permanent's mana value for "different mana values" is its front face's
   (CR 202.3b, Running Is Useless). The counters take the counter step's replacement order. No test of their own.
10. A card filter of `HappenedThisWay` over a step other than REVEAL, and of `AmountThisWay`, is judged with the game's state, so a
    predicate that reads it (the chosen name, effective types) works there.
11. Prompts, log lines and answers are the steps': a single choice is a permanent pick, a hand choice answers by card id.
12. AI only: the name prompts are answered from what the AI can know (Null Chamber, Comply, the exile-by-name records and the mills read
    an opponent's hand or library, the as-enters name a face-down permanent, the reveal guesses their own library's order); the five
    `ColorChoiceAiStrategyTest` cases failed before. A permanent choice keeps or improves the AI's own where the following steps do that,
    and never answers two that share a quality. Foreshadow's draw is inside a condition the value readers don't look into.

**What a player sees.** The name lists: item 1, sent to the chooser alone and now searchable for every name prompt (the existing flag;
frontend unchanged). The perpetual choices show their owner's hand to that player alone and log "A card in …'s hand". The permanent choice
lists battlefield ids only; its log, item 8.

**Seen, not changed.**

- The stack view's face-down leak (`getStackViews`): fixed outside this plan by b6fa735b07 (`FaceDownVisibility`).
- The client ignores a minimum on `MULTI_PERMANENT_PICK`, so too few can still be sent and rejected (frontend).
  `GameSimulator` answers such a pick with the first maximum, which a "different" choice may reject.
- The name step's dig strategy reads the AI's own library order, which the player doesn't know.
- A name prompt that requires a type offers a copy's name by the copy's types (a copy that is also an artifact; CR 201.4a wants the
  Oracle card's).
- `ChoiceHandlerService`'s Thought Hemorrhage and exile-by-name completions search a library that can't be searched; their move onto
  `ExileCardsWithSameNameEffect` would fix it.
- The as-enters permanent choices log the chosen permanent by its card, face down or not (CR 708.2; read, not reproduced). Other
  permanent choices' logs weren't audited.
- `EachOpponentDrawsCardEffectHandler` draws in seating order, not APNAP (CR 101.4).

**Tests edited.** The new cases above, two `MultiPermanentChoiceAiStrategyTest` cases and two constructors (the destroy-all and reveal-top
handler tests). Contradicted the rules: `PlayerInputServiceTest`'s two exile-by-name cases offered the names of the opponent's hand (CR
400.2; the cards are in the chooser's hand now, same assertions), and `ConjurersBanTest.namePromptDoesNotRevealOpponentsHiddenCards`
took a listed card's name for a leak (CR 201.4; it hides unlisted cards now, same assertion). Renamed prompts, contexts and answers, same
assertions: `DuneblastTest`, `MountDoomTest`, `SeasonOfWeavingTest`, `ForeshadowTest`, `PredictTest`, `LammastideWeaveTest`,
`PassTheTorchTest`, `RacketeerBossTest`, and `SelflessGlyphweaverTest` (Deadly Vanity is a permanent pick: its "exactly 1" message became
the rejection of an answer it didn't offer, and the offered three are asserted).

**Tests run.** The final sources in one Gradle run, one `--tests` list per task, counted from that run's XML by the test cases' class
names: 741 classes (1,011 suites, 9,730 cases, 30 skipped, the 29 of `TriggerRouteRecordTest`'s assumption and the MCTS benchmark, 0
failures): every changed card's and changed test class, the cards of the changed verbs (destroy all, name prompts, perpetual grants,
gain control, subtype choices, the card-filtered conditions and amounts), the application's architecture tests with
`EffectCountRatchetTest`, and the application and AI tests naming a changed mechanism. Every project's main and test sources compile on
the final tree. Failed before their fixes: the tests so marked. Not run: the full suites, the AI fuzz and stress tests, the frontend. Not
verified: that the other new cases fail on HEAD; more than two players (the player choices, APNAP); item 9's changes by a test; the size of
the catalog-wide list on the wire for the prompts that had none; MCTS over the new step.

**For the next run (run 16: Choose, 64 rare records).**

- The name's remaining users: a name on `ExileCardsWithSameNameEffect` (with its "up to four" bound) frees Thought Hemorrhage and the
  ledgered twelve-card head (`exiledFromHand()` is its draw and token rider); one restriction step reading the entry's name frees Comply /
  Academic Probation and Conjurer's Ban, a registration that binds the name The Clone Saga's; the two reveal-top guesses need a chooser
  on the name step.
- The player step: a player subject as the chosen return's chooser (Multiple Choice), a difference amount over the chosen player (Boreas
  Charger); `ChooseOpponentGainsControlOfSourceEffect` (nine cards) is read as a static marker by four and built by two registrars, read
  those first. Backdraft chooses one of the player's sorceries too.
- The permanent step: a random choice (The Nipton Lottery, the one per opponent destroyed at random), a chooser per player (the two
  for-each-player records), an amount for the difference of two chosen powers (Spry and Mighty), then the attach, unattach, land-types
  and opponent-chooses-one records.
- The other perpetual hand choices (blitz, noncombat damage, spell-casting ability, incorporation, enter-exile, static, offspring) change
  the card in four different ways; a conjure step over a card subject frees Holographic Double and Nashi.
- The as-enters markers: three permanent choices differ only in candidates and a decline, but their continuations also serve Upgrade,
  the phylactery and the text-box exchange; read those before a fold.

### Step 9, run 16 (2026-11-22, Choose partly done)

The four defects run 15 left, the chosen name on the same-name exile and on the lasting name rules, one-for-each-player and random on the
choose-permanents step, then plain verdicts. Stopped there at a compiling, tested point: 43 of the family's 64 rare records are left. Ratchet
4,227 / 3,454 → 4,220 / 3,448 records / handlers (9 records and 8 handlers deleted, 2 and 2 added). 20 card files changed by scratch scripts.
Oracle text checked with `get_card` for all 20; rulings with `get_rulings` for Thought Hemorrhage, Memoricide, Cranial Extraction,
Necromentia, Unmoored Ego, Academic Probation, Conjurer's Ban, The Clone Saga, Unstable Glyphbridge and Chaos Defiler. CR 101.4, 201.4, 400.2,
603.7a, 608.2c, 608.2d, 608.2h, 609.3, 701.23a, 701.23b, 708.2 and 709.4a checked with `get_rule` (2026-11-22).

| Family | Audited | Absorbed | Split | Survivor | Blocked (cause) | Unique | `step-9` | Left unaudited |
|---|---|---|---|---|---|---|---|---|
| Choose | 23 rows (21 rare records, 1 ledgered row, 1 step) | 0 | 7 | 1 | 0 | 15 | 0 | 43 rare records |
| Other families | 5 rows (same-name exile, two new steps, the floating payload, Meteor's ledgered row) | 1 | 1 | 3 (two new records) | 0 | 0 | 0 | All families: 2,236 rare records without a row |

- **The chosen name.** `ExileCardsWithSameNameEffect.anyNumberWithChosenName` / `upToWithChosenName` / `allWithChosenName` is the second half
  of the thirteen exile-by-name cards; the draw and the token read `AmountThisWay.exiledFromHand()`. Two contexts, their prompts and their
  completions went. `CantPlayChosenNameUntilNextTurnEffect` is Comply's, Academic Probation's and Conjurer's Ban's step and their floating
  effect; `RegisterChosenNameCombatDamageTriggerEffect` The Clone Saga's. Three more contexts and completions went.
- **One for each player.** `ChoosePermanentsEffect.oneForEach(players, candidates, label)` is one chooser's choice per player, recorded
  together at the end; it is no player loop, so Ultimate Magic: Meteor's blocked-player-loop row was wrong and split with Glyphbridge and
  Chaos Defiler. `atRandom` asks nobody.

**Behaviour changes.**

1. The exile-by-name cards don't look into or find in a library that can't be searched, still shuffle it, and search only the top four under
   Aven Mindcensor (CR 609.3; the card). `MemoricideTest` and `ThoughtHemorrhageTest.leavesOutLibraryThatCantBeSearched` (failed before).
2. The log of Metamorphic Alteration's as-enters choice names no face-down creature's card: the copy line was text with both names (CR 708.2).
   `MetamorphicAlterationTest.logNamesNoFaceDownCreature` (failed before). The choices' own "chooses" lines are card segments, which
   b6fa735b07 already masks: `DauntlessBodyguardTest.logNamesNoFaceDownCreature` passed on HEAD.
3. Each opponent draws in APNAP order (CR 101.4). `EachOpponentDrawsCardEffectHandlerTest` (failed before).
4. AI only: a name to dig for is what the AI's library holds most of, not its top card. `ColorChoiceAiStrategyTest.digDoesNotReadTheLibrarysOrder`
   (failed before).
5. Thought Hemorrhage's search is shown also when only graveyard copies match: the searcher looks at all cards in the zones searched
   (CR 701.23a), as Cranial Extraction's was. Graveyard copies are exiled whatever is answered (CR 701.23b). Its test answers it.
6. Chaos Defiler's random pick is among the chosen permanents without indestructible (ruling, 2022-10-07). No test of its own: it shows
   only with two or more opponents, which the card harness doesn't seat.
7. Entries and moves are the steps': the rider draw and token are their own steps after the shuffle, Thought Hemorrhage's reveal is recorded
   and its damage dealt by the damage step. Prompts and log lines are the steps': no "exiles 0 cards" line when nothing matches (the shuffle
   is logged), Glyphbridge asks by single permanent picks, "an artifact card name" has its article.
8. AI only: a chosen-name search names what is seen of the searched player; The Clone Saga one of the AI's own creatures; a chosen permanent
   without power is weighed by mana value (a face-down one's is 0), as the deleted prompts' generic answer did.

**What a player sees.** The search prompt shows what that search may look at and nothing of a library that can't be searched (item 1, the
step's `SearchedZones`). The name prompts are run 15's. A permanent choice lists battlefield ids; its prompt names the player and no card.

**Seen, not changed** (for the owner's decision where it is a leak).

- Opaque card ids: not looked at. Targeting predicates over face-down spells: fixed outside this plan by bcead89079
  (`CastingPermissionService.spellAsEffectsSeeIt`).
- Copy log lines built as text naming a face-down permanent's card (CR 708.2), with lists of names among them
  (`BecomeCopyOfSubjectEffectHandler`, the `MakeTargetCopy…` and `BecomeCopyOfTargetPermanent…` handlers, `TurnFaceUpCopyService`'s
  source): fixed outside this plan by bcead89079 (`FaceDownVisibility`).
- Scour the Scene's choice offers battlefield creatures by card id, and the perpetual hand choices left (offspring, static, enter-exile)
  still go by their own prompts; not audited for what they show.
- Mindblaze's completion shuffles with `Collections.shuffle`, so nothing sees a shuffle.
- `CardChoiceHandlerService.offerSameNamedCopiesExile` searches without the prohibition check; no card sets its flag
  (`exileAllCopiesOfChosenNames`), so it is dead code.
- The multi-zone exile prompt's draw, token and set-code fields have no setter left; the prompt's overloads can go.
- A lasting name rule compares a land's full name, and which half of a split spell the forbidden-name check compares wasn't traced (CR 709.4a).
- The frontend's multi-pick minimum (run 15).

**Tests edited.** The new cases above and `UnstableGlyphbridgeTest.choosesForEachPlayerInTurn`. Deleted with its handler:
`ChooseCardNameAndExileFromZonesEffectHandlerTest` (five cases that asserted the arguments passed to a deleted prompt method) and two
`PlayerInputServiceTest` cases that asserted the deleted context's fields; the cards' tests cover the cap and the riders. Log line of the
survivor: `DispossessTest`, `MemoricideTest`, `ThoughtHemorrhageTest` asserted "exiles 0 cards" (the shuffle line now). Contradicted
CR 701.23a: `ThoughtHemorrhageTest.mayTargetSelfWithNoHandCopies` (item 5; same assertions). Renamed contexts, classes and answers, same
assertions: `FailureComplyTest`, `AcademicProbationTest`, `CardNamePromptsTest`, `PlayerInputServiceTest`, `ColorChoiceAiStrategyTest`,
`UnstableGlyphbridgeTest` (an empty answer became one not offered).

**Tests run.** The final sources in one Gradle run, one `--tests` list per task, counted from that run's XML by the test cases' class names:
250 classes (573 suites, 6,679 cases, 31 skipped: the 29 of `TriggerRouteRecordTest`'s assumption, the AI fuzz and stress tests' switched-off
cases; 0 failures): every changed card's and changed test class (a back face under its front face), the cards of the changed steps and
readers (same-name exiles, name prompts and rules, permanent choices, as-enters choices, the aura copies, the draw), the application's
architecture tests with `EffectCountRatchetTest`, and the application and AI tests naming a changed mechanism. Every project's main and test
sources compile on the final tree. Failed before their fixes: the tests so marked. Not run: the full suites, the frontend, other users of
`DestroyTargetPermanentEffect` and `DealDamageToPlayersEffect` (unchanged). Not verified: more than two players in a game (the choice per
opponent and Chaos Defiler's random pick; APNAP by the handler test only), item 8's new answers by a test of their own, MCTS over the
converted steps, the AI's valuation of Thought Hemorrhage's damage step.

**For the next run (run 17: Choose, 43 rare records, none given a verdict it didn't earn).**

- As-enters markers: read. The three permanent markers differ in candidates and one card's decline, and only their completions are shared
  with Upgrade, the phylactery, Metamorphic Alteration and the text-box exchange: one marker over a candidates subject with one completion
  fits Pitlord / Bodyguard and Predatory Sludge; Scheming Fence's decline is one card's.
- The permanent step: a chooser (My Will Is Irresistible, whose "the rest" also needs the earlier choice), one for each quality (Sundering
  Titan), gain control over a subject (The Nipton Lottery, which `atRandom` starts), a lesser-power-than-the-target filter (Kamiz), an
  amount for two chosen powers (Spry and Mighty), attach and unattach over a subject, Winnowing's filter over the same player's choice.
- Revealed hands with parts that exist: `RevealCardsFromHandEffect` + `ChooseCardEffect` over `revealedThisWay` + discard / put onto the
  battlefield fits Distended Mindbender, Treacherous Urge, Zara and, with a chosen-color filter, Addle (their tests answer by index).
- `ChooseCardEffect` + the perpetual verbs for the hand choices; a conjure step over a card subject (Holographic Double, Nashi); the void
  counter and suspended filters over `exiledCards` (Dauthi Voidwalker, Amy Pond).
- Player step users: Multiple Choice, Boreas Charger, the relay pair. Numbers: the five number choices beside
  `ChooseNumberAtResolutionEffect` differ in where the answer is kept; Mindblaze waits for one with no upper bound that the AI answers.

### Step 9, run 17 (2026-11-22, Choose closed)

Run 16's small defects and dead code, the shared parts it named that cards could use, then a verdict for each of the family's 43 rare
records: Choose has none left. Ratchet 4,220 / 3,448 → 4,207 / 3,436 records / handlers (14 records and 12 handlers deleted, 1 record
added). 16 card files rewritten by scratch scripts and 11 more with one constructor argument fewer (the dead flag). Oracle text checked
with `get_card` for all 16 rewritten cards
and for the cards given a blocked or most of those given a unique verdict (not the subtype, at-random, form, number, Esper Terra and Valki
cards); rulings with `get_rulings` for Treacherous Urge, Kamiz, Mission Briefing, Dauthi Voidwalker, The Nipton Lottery, Volcano Hellion,
Multiple Choice, Mindblaze, Armed and Armored, Distended Mindbender and Spry and Mighty. CR 400.2, 400.7, 406.3a, 508.4, 603.7a, 603.7c,
603.7e, 608.2c, 608.2d, 608.2h, 609.3, 610.3b, 611.2a, 614.1c, 701.20b, 701.21a, 701.24a, 702.127a, 709.3a, 709.4a and 709.4d checked
with `get_rule` (2026-11-22).

| Family | Audited | Absorbed | Split | Survivor | Blocked (cause) | Unique | `step-9` | Left unaudited |
|---|---|---|---|---|---|---|---|---|
| Choose | 44 rows (43 rare records, 1 new record) | 2 | 11 | 1 (new) | 8 other (conjure over a card subject 2, a counter-kind choice 2, same-name searches per permanent, an exactly-that-many condition, entering attacking, the token copy's step) | 22 | 0 | no rare record |
| Other families | 6 rows (Killer Instinct's waiting row, five survivors, one of them Attach's rare record) | 0 | 1 | 5 | 0 | 0 | 0 | All families: 2,192 rare records without a row |

- **Delayed, about a put.** `RegisterDelayedStepTriggerEffect.aboutPutOntoBattlefield` was waited for by three classes (run 13's two and
  the hand-to-battlefield choice): Treacherous Urge and Killer Instinct split; the else-graveyard record still needs a put of the revealed
  card into the graveyard, and Zara an entering attacking.
- **Subjects on survivors.** Gain control and attach any number take a permanent subject, the chosen return a player, the graveyard cast
  permission a card subject. Kamiz's "with lesser power" is a predicate the resolver binds like `PermanentIsTargetCardPredicate`.
- **As-enters.** `ChoosePermanentOnEnterEffect(candidates, label)` is both permanent markers, with one context and completion. Scheming
  Fence's decline stays its own.
- **Dead code.** Run 16's two items went (`offerSameNamedCopiesExile` with its flag through the effect, the prompt and the support's
  overloads; the multi-zone prompt's draw, token and set-code fields with seven overloads), and what this run orphaned: the revealed-hand
  choice's second band, three choice contexts, one interaction with its handler, the targeted form of the hand-to-battlefield choice.

**Behaviour changes.**

1. Mindblaze's shuffle is a shuffle that abilities see (Oracle, CR 701.24a). `MindblazeTest.shuffleTriggersShuffleAbilities` (failed before).
2. A forbidden name (Meddling Mage, Conjurer's Ban and every other name rule) stops only that half of a split card, as only the half being
   cast is evaluated (CR 709.3a), and the fused spell by either (CR 709.4d). Before, the card wasn't offered as playable from a hand,
   couldn't be cast from exile at all, and a cast during a resolution offered, and would cast, the forbidden half.
   `FireIceTest.meddlingMageNamingIceLeavesFireCastable`, `WearTearTest.namedHalfFromExile`,
   `GalvanothTest.splitCardOffersNoHalfWithForbiddenName` (failed before); `ConjurersBanTest.chosenHalfLeavesOtherHalfCastable` and
   `WearTearTest.namedHalfForbidsFuseOnly` guard what already held. An aftermath card is judged by its first half outside a graveyard
   (CR 702.127a); suspend uses the same check; a land is compared by `hasName`. The AI skips a forbidden half.
3. The converted choices are the steps': a lone Dwarf or lesser attacker is taken unasked (CR 608.2d: there is nothing to choose), a
   permanent that can't be chosen is neither offered nor drawn at random (Kamiz, Armed and Armored, The Nipton Lottery), Kamiz's "lesser
   power" reads a target that left as it last was (CR 608.2h), Distended Mindbender's two cards leave the hand together. Prompts, answers
   by card id and log lines are the steps'.
4. Killer Instinct's sacrifice is at the next end step, whoever's (Oracle; it was "your next", the same on your own upkeep). What both
   delayed sacrifices are about is bound as that permanent (`KillerInstinctTest.returnedCreatureIsNotSacrificed`, a guard).
5. Volcano Hellion's and Scrying Glass's number is the resolution's, no longer written on the permanent; Multiple Choice's return records
   its RETURN; Dauntless Bodyguard's and Tyrannical Pitlord's entry hands X, targets, costs and convoke on to the enters abilities as
   Predatory Sludge's did.
6. AI only: a `ChooseCardEffect` of the AI's own among another player's revealed hand or its own graveyard takes the highest mana value,
   as the deleted prompts' strategies did (so Revealing Eye and the choices among its own milled cards do too); a creature to attach
   Equipment to is its own strongest. `MultiGraveyardChoiceAiStrategyTest` (3 cases), `MultiPermanentChoiceAiStrategyTest.attachesToItsOwnStrongest`.

**What a player sees.** The revealed-hand choices follow a public reveal (CR 701.20b) and offer that hand's matching cards to the chooser.
Pull of the Mist Moon's choice is the owner's hand alone and logs no name (`logDoesNotNameTheChosenHandCard`). Dauthi Voidwalker's lists
only cards with a void counter; a face-down card is judged without characteristics and none is offered (CR 406.3a). The permanent choices
list battlefield ids and log no face-down permanent's card.

**Seen, not changed** (for the owner's decision where it is a leak).

- Run 16's copy log lines and the face-down spell predicates: fixed outside this plan by bcead89079.
- Token copies of face-down permanents, other consumers of a face-down object's real characteristics and opaque card ids: not looked at.
- The as-enters permanent choices (these two, Scheming Fence) offer a permanent that can't be chosen; so do Clarion Ultimatum, Spry and
  Mighty and Akiri.
- A Room is judged by both its doors' names, so one forbidden name stops both doors (CR 709.3a); the whole-card type and colour
  prohibitions of a split card weren't traced.
- Zara attacks what the attackers' defender was: her controller doesn't choose between that player and a planeswalker of theirs (Oracle,
  CR 508.4).
- Nashi's graveyard choice is secret; nothing here changed it, and `ChooseCardEffect` would name the card (why it is blocked).
- Akiri's handler reads Equipment by the printed subtype (`permanentHasSubtype`).
- Scour the Scene's choice offers battlefield creatures by card id (run 16).
- The frontend's multi-pick minimum (run 15).
- A hand's playable hint for a split card is the card's: with one half forbidden and only that half payable it is still offered, and
  the cast refuses it.

**Tests edited.** The new cases above. Contradicted the steps' rules of item 3 (a lone candidate is unasked): `ArmedAndArmoredTest` (three
cases answer no Dwarf prompt and assert none is open) and `KamizObscuraOculusTest.landDiscardDoesNotIncreasePowerAndNonattackingKamizCannotBeChosen`
(no prompt; the other attacker gets no double strike). Renamed prompts and answers by card id, same assertions: `AddleTest`,
`DistendedMindbenderTest` (the deleted second band's two field assertions became "no second prompt follows"; the hand is whole until both
are chosen, so the second id is index 2), `TreacherousUrgeTest` (the scheduled action became the delayed ability watching that permanent),
`PullOfTheMistMoonTest` (the grant is a perpetual ability), `DauthiVoidwalkerTest` (the prompt's free flag is asserted on the permission),
`MissionBriefingTest`, `KamizObscuraOculusTest`'s other cases. Constructors and renamed methods: `AsEntersInteractionServiceTest` (stubs
the subject resolver), `GainControlOfTargetEffectHandlerTest`, `PlayerInputServiceTest`, `SpecializedInteractionProjectionTest`,
`SpecializedInteractionAiStrategiesTest`.

**Tests run.** The final sources in one Gradle run, one `--tests` list per task, counted from that run's XML by the test cases' class
names: 1,100 classes (1,604 suites, 15,120 cases, 29 skipped, 0 failures): the 39 test classes that name one of the 27 changed card
classes, every changed test class, the cards of the changed survivors and steps (`--tests-for`), the split-card, name-rule, revealed-hand
and multi-zone prompt tests, `Mastery|DeathReplacement`, the application's architecture tests with `EffectCountRatchetTest`, and the
application and AI tests naming a changed mechanism (227 of 360 and 34 of 57 classes). After it one test assertion was added and the
baselines were set: both classes ran again (14 cases, 0 failures). Every project's main and test sources compile on the final tree. Failed
before their fixes: the four tests so marked (`namedHalfFromExile` before its free-cast setup was completed: the card wasn't playable).
Not run: the full suites, the AI fuzz, stress and benchmark tests, the frontend. Not
verified: that the other new cases fail on HEAD; the AI's skip of a forbidden half and Kamiz's last-known power by a test of their own;
the new delayed binding with several permanents; more than two players; MCTS over the converted steps; aftermath, suspend and
search-cast paths of item 2 by a test.

**For the next run (run 18: Grant, 108 rare records; then Create 67, Prevent 66, May 58, Perpetually 55, Conjure 49, Target 48).**

- Choose's blocked causes have two classes or one each, so the family that brings a third builds the part: a conjure step over a card
  subject (Holographic Double, Nashi; Conjure's 49 will), a recorded counter-kind choice, same-name searches over a permanent subject, an
  exactly-that-many condition, entering attacking on the put step.
- The perpetual verbs Choose left unique (blitz, the damage bonus, spell-casting grants, incorporate) are one card each here: recount
  them in the Perpetually run, with `ChooseCardEffect` as their first half.
- The delayed ability about a put now exists: grep the ledger's delayed-sacrifice rows before giving one that blocker.
- The gain-control record takes a subject: the sibling records that only name whose permanent is gained can fold.
- Seven latest `step-9` ledger rows are left (Sacrifice, Destroy, the can't-be-countered trio, the two may-draws).

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
