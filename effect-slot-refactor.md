# EffectSlot refactor

A step-by-step plan for replacing `EffectSlot` with abilities that are objects of their own. A
triggered ability gets a trigger built from an event kind and filters, instead of one of 515 enum
names, and one generic pipeline matches game events against those triggers and puts the abilities
on the stack. This replaces `refactors-to-do.md` entry 2 ("a creature" vs "another creature" for
creature-death triggers), which is removed in step 11.

To start a step, use the prompt at the bottom with the step number. Steps are meant to be done in
order; each one lists what it depends on. Counts and line numbers were taken at `c3b769cfa8`;
re-check them with a grep before acting on them.

## Status

| Step | Title | Status |
|---|---|---|
| 1 | Slot inventory and baseline | done |
| 2 | Ability model types | open |
| 3 | `Card` stores abilities behind the slot adapter | open |
| 4 | Granted, temporary and delayed triggers store abilities | open |
| 5 | Spell abilities leave the enum | open |
| 6 | Static abilities leave the enum | open |
| 7 | Activated and mana abilities in one list | open |
| 8 | Replacement effects, special actions and markers leave the enum | open |
| 9 | Rules events for zone changes, and shadow mode | open |
| 10 | Trigger index and the generic trigger pipeline | open |
| 11 | Creature-death triggers and self-inclusion | open |
| 12 | The rest of the zone-change family | open |
| 13 | Remaining event families (repeatable) | open |
| 14 | Trigger options replace trigger-only wrapper effects | open |
| 15 | Effects read the trigger event (repeatable) | open |
| 16 | Delete `EffectSlot` and the legacy paths | open |

## Why

`EffectSlot` (`magical-vibes-domain/.../model/EffectSlot.java`) has 515 constants in 1,715 lines.
Card classes use 511 of them; 188 appear exactly once across all card classes, and 269 at most
twice. Almost every new trigger wording therefore needs a new constant and new engine code to fire
it.

**One name packs several independent choices.** A constant such as
`GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER` encodes:

- the kind of ability: spell (`SPELL`), static (`STATIC`), triggered, or something else (below);
- the event: dies, enters, a spell is cast, damage, counters, becomes tapped, and so on;
- whose object it is: `SELF`, `ALLY`, `OPPONENT`, `ANY`, `ENCHANTED`, `EQUIPPED`, with
  `CONTROLLER` and `YOU` used for the same relation in different names;
- the object's type: creature, nontoken, artifact, land, and so on;
- the zone the source works from: 45 constants start with `GRAVEYARD_`, 7 with `COMMAND_ZONE_`,
  6 with `EXILE` and 1 with `HAND_`. **CR 113.6** (checked) makes this a property of the ability,
  and **CR 113.6k** (checked) lets the trigger conditions of one ability function in different
  zones;
- one trigger per object or one per event: `ON_ALLY_CREATURE_ENTERS_BATTLEFIELD` vs
  `ON_ALLY_CREATURES_ENTERS_BATTLEFIELD`;
- whether the source itself counts: `ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD` is its own
  constant.

**Not every slot is a trigger.** **CR 113.3** (checked) has four kinds of abilities. The enum holds
all four, plus things that aren't abilities:

- `SPELL` holds spell abilities (**CR 113.3a**, checked): 11,239 references in card classes and
  213 in engine code.
- `STATIC` and `COMMAND_ZONE_STATIC` hold static abilities: 10,071 references in card classes and
  720 in engine code.
- `ON_TAP` holds "{T}: Add …" mana abilities (**CR 605.1a**, checked), 121 references in card
  classes. `ON_SACRIFICE` holds activated "sacrifice this" abilities, read by
  `AbilityActivationService.sacrificePermanent`. Other activated abilities live in four
  `ActivatedAbility` lists on `Card`, one per zone.
- `GRAVEYARD_DRAW_REPLACEMENT` and `MAY_SKIP_DRAW_STEP_DRAW` are replacement effects,
  `MULLIGAN_ACTION` holds special actions, and `ON_ANY_CARD_EXILED` and the eight `GRANT_*`
  constants are markers that the engine detects by presence.
- `ON_ENTER_BATTLEFIELD` (6,361 references) holds ETB triggers, but also "enters with" and "as
  enters" effects such as `EnterWithCountersEffect` (86 registrations) and
  `ChooseSubtypeOnEnterEffect` (63). Those are replacement effects (**CR 614.1c**, checked), not
  triggered abilities.

**The rest of each trigger condition is spread over effect wrappers.** The slot names the event.
Filters, options and event data are wrapped around the effect:

- limits: `OncePerTurnTriggerEffect`, `OnceOnlyTriggerEffect`, `OncePerTurnPerCreatureTriggerEffect`;
- intervening "if" (**CR 603.4**, checked): a `ConditionalEffect` with `interveningIf()`;
- filters on the triggering object: 16 `Triggering…ConditionalEffect` /
  `ConditionalTriggering…Effect` classes, and `TriggeringPermanentEntryExclusionEffect`;
- batching: `BatchedCreatureDeathTriggerEffect`, `BatchedArtifactOrCreatureDeathTriggerEffect`, the
  `OneOrMore…TriggerEffect` classes, and `TriggerMode` (`ONCE_PER_BATCH`, `PER_COUNTER`,
  `PER_TOKEN`, `PER_BLOCKER`, …) on the registration;
- trigger conditions written as effects: many of the 96 `…TriggerEffect` classes, such as
  `FirstSpellCastTriggerEffect`, `NthCardDrawTriggerEffect` and `KickedSpellCastTriggerEffect`;
- event data: 25 `…AwareEffect` interfaces that collectors call to bind the dying creature's card,
  counters or attachments, the damage source and so on; one-off rewrites such as
  `GainLifeEqualToDyingCreatureToughnessEffect` → `GainLifeEffect`; and 114 effect classes with
  `Triggering` in their name.

Collectors take these off one at a time. `TriggerCollectionService.checkAllyCreatureDeathTriggers`
checks more than ten effect types in one loop before it decides what to queue.
`TriggerCollectorRegistry` picks a handler by slot and effect class, and also unwraps `MayEffect`,
`MayPayManaEffect` and `MayPayTapPermanentsEffect` to find one, so an effect's class decides how
its trigger is collected. There are about 900 `@CollectsTrigger` registrations. "May" triggers
don't reach the stack the same way everywhere: for `ON_ALLY_CREATURE_DIES`, a `MayPayManaEffect`
goes through `queueMayAbility` while a `MayPayLifeEffect` goes straight onto the stack.
**CR 603.5** (checked) says an optional triggered ability goes on the stack either way and the
choice is made on resolution.

**A triggered ability isn't an object.** It is "the effects in slot X", and those effects are kept
in at least eleven slot-keyed places:

- `Card`: `effectRegistrations`, `sagaChapterTargetFilters`, `sagaChapterTargetGroups` and
  `sagaChaptersWithRequiredTarget`;
- `Permanent`: `temporaryTriggeredEffects`, `combatTriggeredEffects`, `persistentTriggeredEffects`
  and `untilNextUpkeepTriggeredEffects`;
- `StackEntry`: `grantedTriggeredEffectsOnEntry` and `persistentTriggeredEffectsOnEntry`;
- `TemporaryGlobalTriggeredAbility`, the `…TriggerWatcher` records, and 41 domain types with an
  `EffectSlot` field or parameter (the `Grant…`, `PerpetuallyGrant…`,
  `RegisterGlobalTriggeredAbility…` and `BecomeCopy…` effects among them).

Each collector decides for itself whether two effects in one slot are one ability or two.
`TriggerMode.INDEPENDENT` exists to say "this registration is its own triggered ability", and
`addSimultaneousEffects` to say two effects are one event. **CR 603.3** (checked) puts each
triggered ability on the stack as one object, so the grouping is a rules question, not a storage
detail.

**Engine cost grows with names, not concepts.**

- `TriggerCollectionService` is 20,186 lines with 336 public `check…Trigger(s)` methods, and the
  `service/trigger` package is about 47,000 lines. 354 engine source files reference `EffectSlot`,
  with 1,363 `getEffects(EffectSlot.…)` / `getEffectRegistrations(EffectSlot.…)` lookups.
- `TriggerContext` already has about 140 event records, mostly one per slot family.
- Target selection depends on which pipeline a slot feeds. `PermanentChoiceContext` has about 40
  trigger-target records, and `agent-docs/TRIGGER_SLOT_TARGETING.md` exists to track which slot
  supports which target filters.
- Triggered abilities reach the stack in several ways: `GameData.enqueueTrigger`,
  `queueInteraction`, `queueMayAbility` (101 calls), and a direct `stack.add` of a
  `TRIGGERED_ABILITY` entry (293 places in engine code).
- `Card.forEachEffectSlotSize`, which the layer board fingerprint uses, walks all ~500 slots for
  every card.

**The shape causes rules bugs.** This is `refactors-to-do.md` entry 2. "Whenever this or another
creature you control dies" is written as `ON_DEATH` plus `ON_ALLY_CREATURE_DIES`.
`checkAllyCreatureDeathTriggers` decides self-inclusion with a heuristic based on the printed card
type, and about 20 printed creatures whose Oracle text includes their own death don't trigger on
it.

**What constrains the design:**

- Cards freeze once live (`agent-docs/ARCHITECTURE.md`). Abilities are built in the constructor and
  frozen with the card. A new `Card` field is copied in `Card(Card source)` and counted in
  `CardFreezeTest`. Copy effects copy abilities as copiable values (**CR 707.2**, checked).
- The AI simulator copies `GameData` (`simulationFrameCopy()`). Anything the trigger pipeline keeps
  between calls must be copied, or be derived data that rebuilds itself.
- The domain events in `agent-docs/DOMAIN_EVENTS.md` are post-mutation facts for projection, and
  no rules code may read them. Trigger matching needs its own synchronous rules events inside the
  action.
- 26,301 card class files and about 100 card test files reference `EffectSlot`, so card changes
  must be made by a script.
- Which abilities a permanent has is a layer 6 result (**CR 613.1f**, checked).
  `hasActiveTriggeredAbilities` checks ability loss today, and granted triggers come from static
  abilities. Matching must see abilities as the layers leave them.

## Design

### Change 1: abilities are objects

`Card` holds a list of `Ability` values, a sealed hierarchy that follows CR 113.3:

- `SpellAbility`: today's `SPELL` effects and their `SpellTarget` groups.
- `ActivatedAbility`: the existing class. Mana abilities (today's `ON_TAP`) and "sacrifice this"
  abilities (today's `ON_SACRIFICE`) fold into it, and all zones share one list.
- `TriggeredAbility`: a `Trigger`, `TriggerOptions` and its effects (Change 2).
- `StaticAbility`: today's `STATIC` and `COMMAND_ZONE_STATIC` effects.

Replacement effects that sit in slots today (`GRAVEYARD_DRAW_REPLACEMENT`,
`MAY_SKIP_DRAW_STEP_DRAW`, "enters with" and "as enters") move to the existing replacement model
(`model/effect/ReplacementEffect` and the `model/replacement/` package); step 8 decides how.

Every ability lists the zones it functions from (CR 113.6, 113.6k). The default is the
battlefield, or the stack for a spell ability. This replaces the `GRAVEYARD_`, `COMMAND_ZONE_`,
`EXILE` and `HAND_` prefixes and the four per-zone activated ability lists.

Effects stay what they are today: `CardEffect` records resolved by their handlers.

### Change 2: a triggered ability is a trigger, options and effects

- **`Trigger`** is an event kind plus filters built from the predicates that already exist
  (`PermanentPredicate`, `CardPredicate`, `PlayerPredicate`, `StackEntryPredicate`, `Condition`).
  Who is involved is a relation to the ability's source and controller (this, you control, an
  opponent controls, any, enchanted, equipped, "this or another"), not part of a name. For example:
  - `ON_ALLY_NONTOKEN_CREATURE_DIES`: a zone change from the battlefield to a graveyard
    (**CR 700.4**, checked), where the object is a nontoken creature you control other than this.
  - `OPPONENT_UPKEEP_TRIGGERED`: the beginning of upkeep, where the active player is an opponent.
  - `ON_BECOMES_TARGET_OF_OPPONENT_SPELL`: becomes the target, where the object is this and the
    source is a spell an opponent controls.

  A `CustomTrigger` names a bespoke matcher for the few conditions predicates can't express. An
  architecture test keeps a ratchet on how many there are.
- **`TriggerOptions`** holds the trigger-level properties that wrappers hold today:
  - an intervening "if" condition, checked when the ability triggers and again on resolution
    (CR 603.4);
  - a limit: once each turn, "do this only once each turn" (**CR 603.2h**, checked), or once only;
  - multiplicity: once per event, once per occurrence within the event (**CR 603.2c**, checked),
    per counter, per token or per blocker, or once for "one or more" in a batch. This absorbs
    `TriggerMode`.

  "You may" is **not** an option. CR 603.5 has the ability go on the stack anyway and the choice
  made on resolution, so `MayEffect` and the may-pay effects stay effects.
- **Effects**: one `TriggeredAbility` is one stack object (CR 603.3). Effects that happen at the
  same time keep today's simultaneous flag. The grouping that each collector decides today becomes
  explicit data.
- **Event data**: the matched event is stored on the stack entry. Effects read it through amounts
  and references in the style of `EventValue` (the triggering object, its last-known power, the
  damage amount, the spell). Over time these replace the `…AwareEffect` binding and the
  `…Triggering…` effect variants (step 15).

Saga chapters become triggered abilities with a lore-counter threshold (**CR 714.2b**, checked).
State triggers (`STATE_TRIGGERED`, **CR 603.8**, checked) become a `Trigger` kind that watches a
game state instead of an event. Delayed triggers (**CR 603.7**, checked), granted triggers, emblem
triggers and perpetual grants hold a `TriggeredAbility` instead of a slot and an effect.

The card authoring API this implies (illustrative only; step 2 settles the names):

```
before: addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, new MayEffect(new DrawCardEffect(1)));
after:  addTrigger(Triggers.dies(nontokenCreature(), Relation.ANOTHER_YOU_CONTROL),
                   new MayEffect(new DrawCardEffect(1)));
```

### Change 3: rules events and one trigger pipeline

- **Rules events**: a sealed `RulesEvent` hierarchy, grown out of `TriggerContext` but organized by
  event kind rather than by slot:
  - zone change (object, from, to, cause, last-known snapshot);
  - a step or phase begins;
  - a spell is cast or copied, or an ability is activated;
  - damage is dealt; counters are put or removed;
  - tapped or untapped; attackers or blockers declared;
  - life gained or lost; cards drawn or discarded;
  - control changed, phased, transformed, turned face up;
  - keyword actions (scry, surveil, explore, investigate, …).

  Choke points emit events into the current action's event batch, and events from one occurrence
  share a batch. That batch is what "one or more" and per-occurrence triggering read (CR 603.2c).
  Zone changes take their snapshot from the existing `DepartureSnapshot` and
  `BattlefieldEntryBatchSupport` paths, so look-back triggers (**CR 603.10** and **603.10a**,
  checked) see the object and its abilities as they were just before the event.
- **Trigger index**: maps each event kind to the (source, ability) pairs for every ability that
  currently functions, by zone, as layer 6 leaves them. It is derived data: rebuilt when the layer
  board fingerprint or a zone changes, never copied by hand, and so correct in a simulation copy.
- **Matcher**: for each event, the index gives the candidates and predicates are checked through
  `PredicateEvaluationService` / `ConditionEvaluationService`. No per-slot code.
- **Putting abilities on the stack**: one service checks the options (intervening "if" when the
  ability triggers, limits), chooses targets through one generalized `TriggerTargetCollector`
  (**CR 603.3d**, checked), and calls `GameData.enqueueTrigger`. APNAP ordering (**CR 603.3b**,
  checked) is unchanged.

**Shadow mode.** The old and new paths run side by side while families move (from step 9). A
test-only recorder captures what the old collectors put toward the stack (through
`enqueueTrigger`, `queueInteraction`, `queueMayAbility` and direct `stack.add`), by source and
effect identity, and compares that with what the matcher would trigger. A difference fails the test
until it is either fixed or recorded in Findings as an old-path bug.

## Ground rules for every step

- Read `agent-docs/ARCHITECTURE.md` before changing engine code.
- Behaviour stays the same unless the step says otherwise. A rules fix is made on purpose, with a
  card test that resolves the case through the engine (no wiring tests), and is listed in Findings.
- A new `Card` field is copied in `Card(Card source)`, frozen with the card, and counted in
  `CardFreezeTest`. New `GameData`, `StackEntry` or `Permanent` state is copied for simulations
  (`simulationFrameCopy()`, `GameDataDeepCopyTest`) unless it is derived data that rebuilds itself.
- Card classes and card tests are changed by a script kept in `scripts/`, run per letter module,
  never by hand. A card the script can't convert is listed in Findings and done on its own.
- When a family moves, delete its `EffectSlot` constants, its `check…` methods, its
  `@CollectsTrigger` handlers and its trigger-target records. Don't keep both paths in production
  code.
- Convert every reader of a slot that a step removes: engine, AI (`magical-vibes-ai`), networking
  (`CardViewFactory`) and test fixtures.
- Update `agent-docs/` (`ORACLE_TEXT_EFFECT_MAP.md`, `EFFECTS_INDEX.md`,
  `EFFECTS_QUICK_REFERENCE.md`, the `CARD_PATTERNS_*` files, `ACTIVATED_ABILITY_GUIDE.md`,
  `TRIGGER_SLOT_TARGETING.md`) and the `implement-card` / `review-card` skills whenever the
  authoring API changes, so card agents stop writing the old form.
- Verify every CR number with the `rules` MCP before writing it.
- Run only the tests for the code you touched, plus the card test classes of the cards the step
  converted (put the `--tests` filter after each module's test task). Many tests fail at HEAD:
  compare against the step 1 baseline and report only new failures. Never run the full suite; ask
  the user to run it. Don't commit until the user says so.
- At the end of a step, set its row in **Status** to `done`. Record anything a later step needs to
  know in that step's **Findings**, and list the tests you had to change and why.

## Steps

### Step 1: Slot inventory and baseline

**Depends on:** nothing.

Write `effect-slot-inventory.md` next to this file, with one row per constant:

- its kind: spell, static, activated, mana, replacement, special action, marker or triggered;
- for a trigger: the event kind, the object and relation filters, the zone, the multiplicity, and
  whether the source itself counts;
- every engine method that reads it, and how that method groups several registrations on one card
  (one stack object for all, or one each);
- the wrappers and `…TriggerEffect` / `…AwareEffect` classes its cards combine with it;
- the number of card classes that use it.

Then record a baseline: for each event family in step 13's table (and the pilot, death and
zone-change slots of steps 10 to 12), the card test classes of its cards and which of them fail at
HEAD.

**Done when:** the inventory covers every constant, the baseline is recorded, and Findings sums up
the families and lists every slot whose kind or behaviour was a surprise.

### Step 2: Ability model types

**Depends on:** 1.

Add the domain types with no behaviour change:

- `Ability` and its subtypes;
- `Trigger`, with a `LegacySlotTrigger(EffectSlot)` variant for the migration;
- `TriggerOptions`;
- the zones an ability functions from.

Make them immutable records, so freezing and copying stay trivial. Settle the authoring API names
from the Design and show two or three cards converted by hand in Findings, as the target for the
scripts.

**Done when:** the types exist with unit tests for their invariants, and nothing uses them yet.

### Step 3: `Card` stores abilities behind the slot adapter

**Depends on:** 2.

- `Card` keeps a list of abilities.
- `addEffect(slot, …)`, `addSimultaneousEffects` and `SpellTarget.addEffect` become adapters that
  build abilities with a `LegacySlotTrigger`. They group registrations into abilities exactly the
  way the step 1 inventory says today's collectors do.
- `getEffects(slot)` and `getEffectRegistrations(slot)` become views over the list, cached as
  today.
- The saga side maps move onto the chapter abilities.
- `forEachEffectSlotSize` is replaced by hashing the ability list.
- Copy and freeze the list, and update `CardFreezeTest`.

**Done when:** the effect `EnumMap` and the saga side maps are gone, nothing outside `Card` and
`SpellTarget` changed, and the baseline tests fail exactly as before.

### Step 4: Granted, temporary and delayed triggers store abilities

**Depends on:** 3.

Everything that holds a triggered ability at runtime holds a `TriggeredAbility` with a
`LegacySlotTrigger` instead of a slot and an effect:

- the slot-keyed maps on `Permanent` (`temporaryTriggeredEffects`, `combatTriggeredEffects`,
  `persistentTriggeredEffects`, `untilNextUpkeepTriggeredEffects`);
- the slot-keyed maps on `StackEntry` (`grantedTriggeredEffectsOnEntry`,
  `persistentTriggeredEffectsOnEntry`);
- `TemporaryGlobalTriggeredAbility`, the `…TriggerWatcher` records and emblems;
- the effects that carry an `EffectSlot` (`Grant…`, `PerpetuallyGrant…`,
  `RegisterGlobalTriggeredAbility…`, `BecomeCopy…`; the inventory lists all 41 types).

`GrantedTriggeredAbilitySupport.grantedTriggeredEffects(gameData, perm, slot)` and the other
readers keep their signatures for now. Copy everything for simulations.

**Done when:** no runtime state is keyed by `EffectSlot`, and the baseline tests fail exactly as
before.

### Step 5: Spell abilities leave the enum

**Depends on:** 3.

Give `SpellAbility` its own authoring API (including `SpellTarget`'s binding) and accessor.
Convert the 213 engine references, the AI and networking readers, and, by script, every card class
and test card. Delete `EffectSlot.SPELL`.

**Done when:** `EffectSlot.SPELL` is gone and the instant and sorcery card tests fail exactly as at
the baseline.

### Step 6: Static abilities leave the enum

**Depends on:** 3.

The same for `STATIC` and `COMMAND_ZONE_STATIC`, which becomes a static ability that functions from
the command zone. The layer system, cost modification and every static handler read static
abilities through the new accessor. Delete both constants.

**Done when:** both constants are gone, and the static-heavy test classes listed in step 1 fail
exactly as at the baseline.

### Step 7: Activated and mana abilities in one list

**Depends on:** 3.

- `ON_TAP` mana effects become mana abilities on `ActivatedAbility` (CR 605.1a).
  `LandTapTriggerCollectorService`, `CardViewFactory` and the AI read them from there.
- `ON_SACRIFICE` becomes an activated ability with a "sacrifice this" cost. Keep
  `AbilityActivationService`'s checks (Pithing Needle, Arrest, the activation locks) and the
  departure question that is asked before payment.
- `activatedAbilities`, `graveyardActivatedAbilities`, `handActivatedAbilities` and
  `stackActivatedAbilities` become one list, each ability with the zones it functions from.

**Done when:** `ON_TAP` and `ON_SACRIFICE` are gone, `Card` has one activated ability list, and the
land, mana and sacrifice-ability tests fail exactly as at the baseline.

### Step 8: Replacement effects, special actions and markers leave the enum

**Depends on:** 5, 6, 7.

- Move `GRAVEYARD_DRAW_REPLACEMENT`, `MAY_SKIP_DRAW_STEP_DRAW`, `MULLIGAN_ACTION`,
  `ON_ANY_CARD_EXILED` and the eight `GRANT_*` markers to the kind the inventory gave them
  (replacement effect, special action or static ability).
- Move the "enters with" and "as enters" effects out of `ON_ENTER_BATTLEFIELD` into replacement
  effects (CR 614.1c, **CR 614.12** checked), checking each class against its Oracle text. Whatever
  is left in the slot must be a real trigger.

**Done when:** every remaining constant is a trigger condition, and the inventory is updated to
say so.

### Step 9: Rules events for zone changes, and shadow mode

**Depends on:** 4, 8.

- Add `RulesEvent` and the zone-change event with its snapshot.
- Emit it everywhere a card changes zones: departures (`DepartureService`), battlefield entry
  (`BattlefieldEntryBatchSupport`), graveyard, exile, hand and library moves, mill and discard. The
  inventory lists the choke points the zone-change collectors run from today.
- Group the events from one occurrence into one batch on the current action, and copy pending
  batches for simulations.
- Add the shadow recorder (test fixtures only), and a harness option that fails a test when the old
  and new paths disagree.

**Done when:** every zone change emits one event with the right snapshot (tested at each choke
point), and the recorder runs in tests with nothing to compare yet.

### Step 10: Trigger index and the generic trigger pipeline

**Depends on:** 9.

Build the trigger index, the matcher, and the service that puts abilities on the stack (options,
intervening "if", limits, targets through the generalized `TriggerTargetCollector`). Until step 14,
the service also reads the trigger-only wrappers, through one adapter (`LegacyTriggerOptions`), so
cards don't have to change before their family moves.

Pilot it on the source's own zone changes: `ON_DEATH`, `ON_SELF_LEAVES_BATTLEFIELD` and the
`ON_SELF_PUT_INTO_GRAVEYARD_FROM_…` slots:

1. Map each slot to a `Trigger`.
2. Run shadow mode over their card tests.
3. Switch the slots to the new pipeline.
4. Convert their cards by script.
5. Delete the constants and their collector code.

Time the pilot's card test classes before and after, and record the numbers in Findings.

**Done when:** the pilot slots are gone, their card tests fail exactly as at the baseline, and
shadow mode left no unexplained difference.

### Step 11: Creature-death triggers and self-inclusion

**Depends on:** 10.

This replaces `refactors-to-do.md` entry 2.

Move these slots, and their `GRAVEYARD_` forms:

- `ON_ALLY_CREATURE_DIES`, `ON_ANY_CREATURE_DIES` and `ON_OPPONENT_CREATURE_DIES`;
- the nontoken, artifact-or-creature and creature-or-planeswalker variants;
- `ON_DAMAGED_CREATURE_DIES`, `ON_HAUNTED_CREATURE_DIES` and `ON_EQUIPPED_CREATURE_DIES`;
- the enchanted-player variant.

Classify every card by its Oracle text with the scryfall MCP: "a creature" (includes itself, with
CR 603.10a look-back) or "another creature". Delete the printed-type heuristic in
`checkAllyCreatureDeathTriggers` and the duplicate `ON_DEATH` registrations. The batched variants
become the "one or more" multiplicity.

Add these tests:

- a creature that triggers on its own death;
- an animated noncreature source dying;
- an "another creature" card not triggering on its own death;
- a wrath that kills a "whenever a creature dies" watcher along with other creatures (CR 603.10a's
  example).

**Done when:**

- the constants are gone;
- the about 20 cards in entry 2 trigger on their own death;
- entry 2 is removed from `refactors-to-do.md`;
- the other death card tests fail exactly as at the baseline.

### Step 12: The rest of the zone-change family

**Depends on:** 11.

Move the remaining zone-change triggers:

- enters: `ON_ENTER_BATTLEFIELD` and every `…ENTERS_BATTLEFIELD` / `…ENTERS_FROM_…` constant.
  **CR 603.6a** (checked) checks the newcomers too;
- leaves the battlefield (**CR 603.6c**, checked);
- put into a graveyard from anywhere;
- exiled, returned to hand, milled, put into a library;
- cards leaving a graveyard, and sacrificed;
- phased out (**CR 603.10b**, checked), transformed, turned face up.

Per-token and per-object behaviour comes from the multiplicity. Convert `ON_ENTER_BATTLEFIELD`
(6,361 references) last and on its own, after the smaller zone-change slots.

**Done when:** every zone-change constant is gone and its card tests fail exactly as at the
baseline.

### Step 13: Remaining event families (repeatable)

**Depends on:** 12.

Each run moves the next family in the table below:

1. Add its event kinds and emit them at the choke points.
2. Map its slots.
3. Run shadow mode.
4. Switch the family to the new pipeline.
5. Convert its cards by script.
6. Delete the constants, collector methods, handlers and trigger-target records.

The slot counts come from slot names and are approximate; the step 1 inventory is the real list.

| Order | Family | Slots | Notes |
|---|---|---|---|
| 1 | Steps and phases | ~36 | upkeep, draw, combat, main, end step; own, each, opponent or enchanted player; graveyard and command zone forms |
| 2 | Spells and abilities | ~20 | cast, copied, countered, activated; the "first spell" / "Nth spell" conditions now in `…TriggerEffect` classes |
| 3 | Combat | ~58 | attacks, blocks, becomes blocked, unblocked, combat damage |
| 4 | Damage | ~33 | is dealt, deals, excess, noncombat, prevented |
| 5 | Counters | ~34 | put, removed, per counter, proliferate, level, evolve, train |
| 6 | Becomes the target | ~21 | of a spell, an ability, an opponent's spell, an Aura spell |
| 7 | Library, draw and discard | ~24 | draws, Nth draw, discards, scry, surveil, search, shuffle, cycle |
| 8 | Tap and untap | ~16 | only a change of state triggers (**CR 603.2e**, checked) |
| 9 | Life | ~10 | gained, lost, paid |
| 10 | Sagas | ~9 | chapter thresholds (CR 714.2b), the final chapter |
| 11 | Everything else | ~80 | keyword and player actions (dice, coin flips, crimes, investigate, explore, connive, exploit, the Ring, monarch, dungeons, voting), attaching, control change, crew / saddle / station, day and night, Rooms, Planechase, state triggers |

Each run records in Findings which family it moved and what is left. Mark the step `done` only
when no trigger constant is left.

### Step 14: Trigger options replace trigger-only wrapper effects

**Depends on:** 13.

Convert, by script, every triggered ability whose effects carry a trigger-level property into
`TriggerOptions` and `Trigger` filters. That covers:

- `OncePerTurnTriggerEffect`, `OnceOnlyTriggerEffect`, `OncePerTurnPerCreatureTriggerEffect`;
- an intervening-if `ConditionalEffect`;
- the `Batched…` and `OneOrMore…` trigger effects;
- the `Triggering…Conditional` filters and `TriggeringPermanentEntryExclusionEffect`;
- the `…TriggerEffect` classes that are really trigger conditions (the inventory lists them);
- a non-default `TriggerMode`.

`MayEffect` and the may-pay effects stay (CR 603.5). Remove the may-unwrapping from the pipeline.
Where the old path asked a may question before the ability was on the stack, the new path asks it
on resolution; list those cards in Findings.

A `ConditionalEffect` that isn't an intervening "if" stays an effect. Delete only the trigger-only
classes, `TriggerMode` and `LegacyTriggerOptions`.

**Done when:** no triggered ability's effects contain a trigger-only wrapper, those classes and
`TriggerMode` are gone, and the converted cards' tests fail exactly as at the baseline.

### Step 15: Effects read the trigger event (repeatable)

**Depends on:** 14.

Each run takes a group of event-binding effects and replaces it with an existing effect plus a
reference to the stored event, following AGENTS.md's reuse rule. For example,
`DrawCardForTriggeringPlayerEffect` becomes `DrawCardEffect` aimed at the event's player, and
`GainLifeEqualToDyingCreatureToughnessEffect` becomes `GainLifeEffect` with the event object's
last-known toughness.

Start with the 25 `…AwareEffect` interfaces, because the pipeline calls them, then the 114
`…Triggering…` effect classes. Coordinate with `agent-docs/EFFECT_MERGE_PLAN.md` so the two plans
don't merge the same classes twice.

Each run records in Findings which group it moved and what is left. Mark the step `done` only when
no `…AwareEffect` remains and the pipeline no longer rewrites effects.

### Step 16: Delete `EffectSlot` and the legacy paths

**Depends on:** 15.

- Delete `EffectSlot`, `LegacySlotTrigger`, the `addEffect(slot, …)` adapter,
  `TriggerCollectorRegistry`, `@CollectsTrigger`, `TriggerContext` (replaced by `RulesEvent`),
  `TriggerMatchContext`, and whatever is left of `TriggerCollectionService` and the
  `…TriggerCollectorService` classes.
- Delete the shadow recorder.
- Check that every triggered ability reaches the stack through the one service, with no direct
  `stack.add` of a `TRIGGERED_ABILITY` entry.
- Update `agent-docs/ARCHITECTURE.md` with abilities, rules events and the trigger pipeline.
- Replace `TRIGGER_SLOT_TARGETING.md` with a short note on targeting for triggered abilities.
- Do a final pass over the other `agent-docs/` files and the skills.

**Done when:** a grep for `EffectSlot` finds nothing, and the docs describe only the new model.

## Findings

Steps add notes here for later steps, under their number.

### Step 1

**Files.** `effect-slot-inventory.md` is generated by `scripts/effect-slot-inventory.py`. The script reads
the mechanical columns from the sources: card classes, readers, wrappers, trigger modes and choke points.
The semantic columns come from `scripts/effect-slot-classification.tsv`, which is kept by hand. Ten
agents filled it in by reading each slot's collector and handlers. The non-trigger rows were written by
hand. On merge, the kinds and step assignments were checked and seven were corrected; the other columns
are the agents' reading, spot-checked only. To change the
inventory, edit the TSV and re-run the script; never edit the inventory itself. The script has two more
options:

- `--gradle-args <step>` prints the card-test filters for a step's cards, one `--tests` after each
  module's test task.
- `--results` reads the JUnit reports of a full card-test run into the Baseline section.
- `--all-passed` records a full run with no failures.

**Baseline.** In the user's full card-test run at `7b169f25de`, every card test class passed. The JUnit
reports were not on this machine, so the inventory was rendered with `--all-passed`. Later steps
therefore have a simple rule: "fail exactly as at the baseline" means that no card test fails. A failure
in the card test classes of a step's cards (`--gradle-args <step>`) is a regression that the step caused,
unless the step records it in Findings as an intended rules fix.

**Families.**

| Step | Constants | Card classes | Grouping (see the inventory's Columns) |
|---|---|---|---|
| 5 | 1 | 7,420 | all |
| 6 | 2 | 7,955 | none |
| 7 | 2 | 121 | all |
| 8 | 13 | 57 | all 8, none 3, each 2 |
| 10 | 7 | 1,156 | each 7 |
| 11 | 16 | 456 | each 10, split 6 |
| 12 | 137 | 6,955 | each 127, all 5, split 4, varies 1 |
| 13.1 | 34 | 2,315 | each 24, split 8, all 2 |
| 13.2 | 24 | 1,526 | each 23, split 1 |
| 13.3 | 51 | 3,098 | each 21, all 16, split 7, all-but-independent 4, varies 3 |
| 13.4 | 40 | 295 | each 35, all 3, varies 2 |
| 13.5 | 43 | 116 | all 28, each 14, split 1 |
| 13.6 | 21 | 342 | all 12, split 5, each 4 |
| 13.7 | 23 | 332 | each 18, all 5 |
| 13.8 | 16 | 207 | each 14, all 1, split 1 |
| 13.9 | 8 | 105 | each 8 |
| 13.10 | 8 | 217 | all 6, each 2 |
| 13.11 | 69 | 438 | each 56, split 6, all 6, none 1 |

Kinds: 498 triggered, 10 static, 2 replacement, 2 special action, 1 spell, 1 activated, 1 mana. The
inventory has 53 domain types that hold or take an `EffectSlot`, not 41.

**Corrections to the plan.**

- The real slot counts for step 13's table are 34, 24, 51, 40, 43, 21, 23, 16, 8, 8 and 69.
- Step 10's pilot is the plan's list plus the dead `ON_SELF_MILLED`. `GRAVEYARD_ON_SELF_RETURNED_TO_HAND`
  stays in step 12.
- The step 11 list grows by three slots: `ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD`
  and its two `GRAVEYARD_ON_CREATURE_PUT_INTO_…_FROM_BATTLEFIELD` forms. These are creature deaths keyed
  to the owner of the graveyard.
- Step 8 changes:
  - The eight `GRANT_*` slots are static abilities that grant a triggered keyword to spells: cascade,
    storm (**CR 702.40a**, checked: a triggered ability that functions on the stack) and demonstrate.
    They are not markers. `TriggerCollectionService#checkSpellCastTriggers` builds the granted trigger
    itself, as one stack object per granting permanent, with a direct `stack.add`.
  - `ON_ANY_CARD_EXILED` is a marker only for Bell Borca (`NoteManaValueOfExiledCardsEffect`, read by
    `GameData#notifyCardsExiled`). The War Doctor's effect in it is a trigger, which stays for step 12.
  - Step 8 also takes `ON_OPENING_HAND_REVEAL` (below), plus the replacement effects listed under
    "Mixed slots".

**Mixed slots: slots that hold more than one kind.** The effect classes are in each slot's notes.

- `ON_ENTER_BATTLEFIELD` reads about 50 `ReplacementEffect` implementers and about 13 as-enters choice
  classes in place, and filters them out of the trigger. Some of its effects are also checked by
  presence elsewhere.
- `STATIC` also holds several other kinds:
  - replacement effects: `EntersTappedEffect` (569 registrations), `ExileInsteadOfGraveyardReplacementEffect`,
    `RiotEffect`, `UnleashEffect` and others;
  - abilities that function on the stack or in hand: `KickerEffect` (266; **CR 702.33a**, checked),
    `BuybackEffect`, `SpliceEffect`, `GiftEffect`, `ReduceOwnCastCostEffect`;
  - echo, which is a triggered ability: `RegisterEchoAtNextUpkeepEffect` (51).

  Step 6 must give these their zones, and step 8 should move the replacement effects along with the
  "enters with" ones.
- `ON_TURNED_FACE_UP` holds four replacement effects: `PutCountersOnTurnFaceUpEffect`,
  `TurnFaceUpCopyEffect`, `TurnFaceUpAttachAuraEffect` and `ChoosePowerToughnessFormEffect`. Its two
  readers also disagree: `GameService#finishTurningFaceUp` and `TurnFaceUpCopyService#finishTurnFaceUp`.
- `ON_SELF_DISCARDED_BY_OPPONENT` holds `EnterBattlefieldOnDiscardEffect`, a replacement checked at 7
  discard sites.
- `ON_EQUIPMENT_ATTACHED` holds "as this becomes attached" choices.
- `ON_DEATH` also holds:
  - "when you sacrifice this" (`onlyTriggersOnSacrifice`), which is collected from the sacrifice path;
  - haunt on instants and sorceries, fired at resolution.

  It fires on the same event as `ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD`, but from a different choke
  point and with different ability-loss handling.
- `ON_OPENING_HAND_REVEAL` (29 cards) is not a trigger. Every registration is
  `MayEffect(PregameChoiceEffect)`, an opening-hand action (**CR 103.6**, checked) handled in
  `MulliganService#startGame`. `StepTriggerService#handleOpeningHandTriggers` is dead for every current
  card.
- `MAY_SKIP_DRAW_STEP_DRAW` duplicates a replacement that is also read from `STATIC`
  (`MaySkipDrawReplacementEffect`).
- These are triggered mana abilities (**CR 605.1b**, checked), which do not use the stack:
  - `ON_ANY_PLAYER_TAPS_LAND`, mostly, though it also holds about 10 stack triggers;
  - the three `ON_CONTROLLER_TAPS_…_FOR_MANA` slots.
- `COMMAND_ZONE_END_STEP_TRIGGERED` has no cards. `GameSetupService#initializeGame` uses "this slot is
  non-empty" to start a card in the command zone.
- `DRAW_TRIGGERED` holds Giant Oyster's delayed trigger. `UPKEEP_TRIGGERED` is read again for Divine
  Intervention's last-counter trigger.

**Unused slots.**

- Four slots have no card class. Delete each when its family moves:
  - `ON_SELF_MILLED`;
  - `ON_EQUIPMENT_ATTACHED_TO_CREATURE`, which has no reader either, yet
    `agent-docs/TRIGGER_SLOT_TARGETING.md` and `CARD_PATTERNS_PERMANENTS_ARTIFACTS.md` still describe it;
  - `ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTER_ON_CREATURE`, whose Javadoc names a card that uses the plural
    slot;
  - `COMMAND_ZONE_END_STEP_TRIGGERED`.
- Three slots are only keys of turn-scoped global triggers that spells create. Step 4 turns them into
  `TriggeredAbility` values:
  - `ON_ALLY_CREATURE_OR_ENCHANTMENT_ENTERS_BATTLEFIELD` (Rite of Harmony);
  - `ON_ANY_PLAYER_GAINS_LIFE` (False Cure);
  - `ON_ANY_CREATURE_BECOMES_TAPPED` (Don't Move).

**Grouping, for step 3.** Across all 515 constants: each 365, all 95, split 39, varies 6,
all-but-independent 4, none 6.

A collector that dispatches through `TriggerCollectorRegistry` always groups as *each*, because a
handler only ever gets one registration. The other collectors split by effect class, which is not how
**CR 603.3** (checked) delimits abilities:

- `ON_ENTER_BATTLEFIELD`:
  - plain registrations share one stack object and one target choice;
  - each of these is its own stack object: a `MayEffect`, a `ChooseOneAtTriggerTimeEffect`, an
    `INDEPENDENT` registration, `SacrificeSelfIfEvokedEffect`;
  - graveyard- and spell-targeting effects split off.
- The step slots each group differently:
  - `UPKEEP_TRIGGERED`: modal effects are separate. An any-target effect or Surveil pulls the whole list
    into one object. Player-target effects share one object, and so do graveyard-target effects.
  - The beginning-of-combat slots ignore `TriggerMode`.
  - `PRECOMBAT_MAIN_TRIGGERED` honours `INDEPENDENT` only for untargeted effects.
  - `POSTCOMBAT_MAIN_TRIGGERED` and `SECOND_MAIN_PHASE_TRIGGERED` use another split.
  - `EACH_PRECOMBAT_MAIN_TRIGGERED` and `END_OF_COMBAT_TRIGGERED` use *all*.
  - The graveyard step slots use *each*.
- `ON_ALLY_LAND_ENTERS_BATTLEFIELD`: one `INDEPENDENT` registration makes every registration separate.
- `ON_ALLY_CREATURE_DIES`: plain registrations share one object; conditional, may and targeted ones are
  separate.
- `ON_BLOCK`: one `ONCE_PER_BLOCK` registration applies to every registration on the card.
- The three `ON_CREATURE_ATTACKS_YOU…` slots on one card share one stack object across the slots.
- The planar slots give each untargeted effect its own stack object. Kessig, for example, gets three.

Recommendation for step 3: build abilities from the Grouping column for *each*, *all* and
*all-but-independent*. For the 49 *split*, *varies* and *all-but-independent* slots, keep one ability per
registration and leave the partitioning to the collectors; `getEffects(slot)` views don't change, so this
changes no behaviour. The step that moves each family then sets the real abilities from Oracle text, and
shadow mode shows the differences.

**Self-inclusion, for steps 11 and 12.** 20 slots decide it with a heuristic (`self` = heuristic). The
main ones:

- `ON_ALLY_CREATURE_DIES` uses the printed type, and its batched path always excludes the source.
- `ON_ALLY_LAND_ENTERS_BATTLEFIELD`: a printed land skips its own entry unless the card has a
  `TriggeringPermanentConditionalEffect`.
- `ON_ANY_CREATURE_SACRIFICED` and `ON_ANY_PERMANENT_SACRIFICED` check whether the card's text contains
  "sacrifices another".
- `ON_ANY_CREATURE_DIES` excludes the source's own death except for two effect classes. That is why
  "whenever a creature dies" cards also register `ON_DEATH`.
- `ON_DAMAGED_CREATURE_DIES` sees the source's own death only in a simultaneous batch.
- The becomes-target slots fire through an attached Aura or Equipment.

**Multiplicity and batching, for step 9's event batches.** "One or more" triggers batch only inside an
explicit scope:

- `collectingSimultaneousCreatureEntryTriggers`;
- `beginPermanentLeaveBatch`;
- the graveyard-leave batch;
- `GameData`'s card-exile batch;
- the tap batch scopes in four services.

Outside a scope they fire once per object. That happens for:

- `ON_ALLY_CREATURES_ENTERS_BATTLEFIELD`;
- `ON_ALLY_PERMANENTS_LEAVE_BATTLEFIELD`;
- `ON_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING`;
- Kaya and Ketramose on a board exile;
- mass taps for `ON_ALLY_PERMANENT_BECOMES_TAPPED`;
- Ranar's exile slot;
- Moonshadow on a wrath;
- Robot Domination on a mill.

Other multiplicity problems:

- `ON_SELF_OR_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING` is per creature on every path, so Aang,
  Airbending Master over-triggers.
- `PER_COUNTER` uses the count the caller passes, and most callers pass 1.
- +1/+1 counters fire `ON_ALLY_COUNTER_PUT_ON_CREATURE` only once.
- The counter-removal slots are polled from state-based actions, so removals between two checks merge
  into one trigger.

Many zone changes have no single choke point. Battlefield entry alone has about 140
`processCreatureETBEffects` calls and about 156 `handleCreatureEnteredBattlefield` calls. The inventory
lists the callers of each zone-change reader.

**Collector inconsistencies that shadow mode (step 10) will surface.**

- **Ability loss and face-down.** `dispatchSlot` is the only full check. Many collectors check only the
  until-end-of-turn flag, or nothing. Face-down permanents keep their real `Card`, so a collector without
  `isFaceDown` fires the printed abilities of a face-down permanent.
- **Extra-trigger copies.** "Triggers an additional time" copies are counted only in
  `TriggerCollectionService#dispatch`. Collectors that call `registry.dispatch` directly, or read their
  slot themselves, skip them.
- **Granted triggers.** Only some collectors read granted triggered abilities.
- **Stack entry.** Direct `stack.add` calls bypass `enqueueTrigger`.
- **Printed characteristics.** Some collectors test printed types, subtypes or power and toughness.
- **Default handlers.** 70 trigger slots have handlers only for specific effect classes and no
  `CardEffect` default. Any other effect class is dropped silently, unless the collector handles it
  outside the registry.
- **Effect classes as filters.** Some effect classes narrow a slot's filter. For example, a
  `TriggeringPermanentConditionalEffect` narrows `ON_ANY_PERMANENT_ENTERS_BATTLEFIELD` to "you control"
  unless `anyController` is set.
- **Registry key collisions.** Both `MiscTriggerCollectorService` and `LifeGainTriggerCollectorService`
  register three keys: `(ON_CONTROLLER_GAINS_LIFE, MayPayManaEffect)`,
  `(ON_CONTROLLER_GAINS_LIFE, SequenceEffect)` and `(ON_CONTROLLER_LOSES_LIFE, SequenceEffect)`.
  `TriggerCollectorRegistry.register` overwrites, so whichever bean is scanned last wins.
- **Unreachable handlers.** Collectors unwrap `OncePerTurnTriggerEffect` and `ConditionalEffect` before
  they dispatch, so those handlers never run on four slot groups: the crime slots,
  `ON_CONTROLLER_LOSES_LIFE`, `ON_CONTROLLER_GAINS_LIFE` and `ON_ANY_CREATURE_SACRIFICED`.

**Suspected rules bugs.** These come from reading the code and are not tested or fixed. Each one needs
a card test that resolves the case through the engine before it is changed.

- Proliferate never triggers Saga chapters (`MultiPermanentChoiceHandlerService#proliferatePermanent`).
  **CR 714.2b** (checked) triggers on any lore counter put on the Saga.
- When an effect puts a lore counter on a Saga, the chapter ability is controlled by that effect's
  controller (`PermanentCounterSupport`).
- A "target" chapter goes on the stack without a target unless its card calls
  `setSagaChapterTargetRequired`; only 17 cards do. A doubled lore counter skips a chapter.
- A cast permanent's modal ETB uses the mode chosen at cast time (`etbMode`). **CR 700.2b** (checked)
  says modes are chosen as the ability is put on the stack.
- `PutCountersOnSourceEffectHandler` (about 387 cards) skips the +1/+1-counter watcher notifications, so
  seven counter slots never fire from it. It may also fire two others twice.
- Aven Riftwatcher, Calciderm and Chronozoa may get two vanishing sacrifice triggers.
- `BEGINNING_OF_COMBAT_TRIGGERED` scans the active player's command zone unconditionally.
- Prized Amalgam checks the caster's graveyard, not the graveyard the creature was cast from.
- `ON_OPPONENT_OWNED_CARD_EXILED` reads only the last exiled card.
- `ON_CONTROLLER_DEALT_COMBAT_DAMAGE` can trigger twice in one step.
- The monarch, coin-flip-loss, player-loses-game, exploit and championed triggers have no ability-loss
  check.

**Slots whose kind or behaviour was a surprise.** The inventory's notes give the details; search
`SURPRISE`.

- 6: `STATIC`
- 8: `ON_ANY_CARD_EXILED`, `ON_OPENING_HAND_REVEAL`, the eight `GRANT_*`
- 10: `ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE`, `ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD`,
  `ON_SELF_MILLED`
- 11: `ON_ALLY_CREATURE_DIES`, `ON_ALLY_ARTIFACT_OR_CREATURE_DIES`, `ON_DAMAGED_CREATURE_DIES`,
  `ON_ANY_NONTOKEN_CREATURE_DIES`, `GRAVEYARD_ON_ALLY_CREATURE_DIES`, `GRAVEYARD_ON_OPPONENT_CREATURE_DIES`
- 12: `ON_ENTER_BATTLEFIELD`, `ON_ALLY_CREATURES_ENTERS_BATTLEFIELD`,
  `ON_ALLY_CREATURE_OR_ENCHANTMENT_ENTERS_BATTLEFIELD`, `ON_SELF_OR_ALLY_CREATURES_ENTERS_BATTLEFIELD`,
  `ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD`, `ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD`,
  `ON_CONTROLLER_CARDS_EXILED_FROM_HAND`, `ON_CONTROLLER_SPELL_OR_ABILITY_EXILES_PERMANENT`,
  `ON_ANY_NONCREATURE_ARTIFACT_SACRIFICED_OR_DESTROYED`, `ON_ANY_CREATURE_SACRIFICED`,
  `ON_ANY_ARTIFACT_EXILED_FROM_BATTLEFIELD`, `ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD`,
  `ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD`,
  `ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD`,
  `ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD`,
  `ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD`, `ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE`,
  `ON_NONBLACK_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE`, `ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE`,
  `ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE`, `ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE`,
  `ON_ALLY_LAND_ENTERS_BATTLEFIELD`, `ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD`,
  `ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD`, `ON_CREATURE_ENTERS_FROM_GRAVEYARD`,
  `ON_ANY_PERMANENT_ENTERS_BATTLEFIELD`, `ON_NONLAND_CARDS_MILLED`, `ON_ANY_CREATURE_EXILED_FROM_BATTLEFIELD`,
  `ON_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING`, `ON_SELF_OR_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING`,
  `ON_ALLY_PERMANENTS_LEAVE_BATTLEFIELD`, `GRAVEYARD_ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD`,
  `GRAVEYARD_ON_CREATURE_ENTERS_FROM_GRAVEYARD_OR_CAST_FROM_GRAVEYARD`,
  `GRAVEYARD_ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD`, `ON_CONTROLLER_CARDS_EXILED_FROM_LIBRARY_OR_GRAVEYARD`,
  `ON_CONTROLLER_CREATURES_OR_CREATURE_CARDS_EXILED`, `ON_OPPONENT_OWNED_CARD_EXILED`,
  `ON_CARDS_EXILED_FROM_GRAVEYARDS_OR_BATTLEFIELD_DURING_YOUR_TURN`, `ON_TRANSFORM_TO_BACK_FACE`,
  `ON_TURNED_FACE_UP`, `ON_ANY_PERMANENT_RETURNED_TO_HAND`, `ON_CONTROLLER_PERMANENT_RETURNED_TO_HAND`
- 13.1: `UPKEEP_TRIGGERED`, `EACH_UPKEEP_TRIGGERED`, `DRAW_TRIGGERED`, `EACH_DRAW_TRIGGERED`,
  `COMMAND_ZONE_END_STEP_TRIGGERED`, `OPPONENT_END_STEP_TRIGGERED`,
  `ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED`, `BEGINNING_OF_COMBAT_TRIGGERED`,
  `EACH_BEGINNING_OF_COMBAT_TRIGGERED`
- 13.2: `ON_SELF_MANA_ABILITY_RESOLVES`, `ON_SELF_SPELL_COUNTERED_OR_FIZZLED`, `GRAVEYARD_ON_CONTROLLER_CASTS_SPELL`
- 13.3: `ON_BLOCK`, `ON_COMBAT_DAMAGE_TO_PLAYER`, `ON_COMBAT_DAMAGE_TO_CREATURE`, `ON_ATTACKS_UNBLOCKED`,
  `ON_ENCHANTED_CREATURE_ATTACKS_UNBLOCKED`, `ON_ALLY_CREATURES_ATTACK`, `ON_CREATURE_ATTACKS_YOU`,
  `ON_ANY_CREATURE_BECOMES_BLOCKED`, `ON_SELF_DEALS_COMBAT_DAMAGE`, `ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU`
- 13.4: `ON_DAMAGE_TO_PLAYER`, `ON_ANY_PERMANENT_DEALT_DAMAGE`, `ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE`,
  `ON_CONTROLLER_DEALT_DAMAGE`, `ON_CONTROLLER_DEALT_DAMAGE_BY_ALLY_SOURCE`, `ON_CONTROLLER_DAMAGE_PREVENTED`,
  `ON_OPPONENT_DEALT_DAMAGE`, `ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT`, `ON_ALLY_SOURCE_DEALS_DAMAGE_TO_PLAYER`,
  `ON_SOURCE_DEALS_DAMAGE_TO_ITS_CONTROLLER_PLANAR`, `ON_ALLY_CREATURES_DEAL_DAMAGE_TO_PLAYER`,
  `ON_OPPONENT_SOURCE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT`, `ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_CREATURE`
- 13.5: `ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE`, `ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT`,
  `ON_SELF_REACHES_LEVEL_TWO`, `ON_SELF_REACHES_LEVEL_THREE`, `ON_SELF_COUNTERS_PUT`,
  `ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_NON_HYDRA_CREATURE`,
  `ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_ANOTHER_CREATURE`,
  `ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_ANOTHER_PERMANENT_FIRST_TIME_EACH_TURN`,
  `ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_PERMANENT`, `ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_CREATURE`,
  `ON_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_ALLY_CREATURE`, `ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_ANY_CREATURE`,
  `ON_CONTROLLER_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_CREATURE`, `ON_YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER`,
  `ON_YOU_PUT_COUNTERS_ON_ANOTHER_CREATURE`, `ON_OPPONENT_PUT_COUNTERS_ON_CREATURE_THEY_CONTROL`,
  `ON_YOU_PUT_LORE_COUNTERS_ON_SAGA`, `ON_ALLY_COUNTER_PUT_ON_CREATURE`, `ON_SELF_LOYALTY_COUNTERS_REMOVED`,
  `ON_SELF_TIME_COUNTERS_REMOVED`, `ON_ALLY_COUNTERS_REMOVED_FROM_PERMANENT`,
  `ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTER_ON_CREATURE`
- 13.6: `ON_BECOMES_TARGET_OF_AURA_SPELL`, `ON_BECOMES_TARGET_OF_OPPONENT_SPELL`,
  `ON_BECOMES_TARGET_OF_OPPONENT_SPELL_ONLY`, `ON_CONTROLLER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY`,
  `ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY`, `ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL`,
  `ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL`
- 13.7: `ON_CONTROLLER_DRAWS`, `ON_CONTROLLER_DRAWS_SECOND_CARD`, `ON_OPPONENT_DISCARDS`,
  `ON_CONTROLLER_DISCARDS`, `ON_SELF_DISCARDED_BY_OPPONENT`
- 13.8: `ON_ANY_PLAYER_TAPS_PERMANENT_FOR_MANA`, `ON_ALLY_PERMANENT_BECOMES_TAPPED`,
  `ON_OPPONENT_PERMANENT_BECOMES_TAPPED`, `ON_ANY_CREATURE_BECOMES_TAPPED`
- 13.9: `ON_ANY_PLAYER_GAINS_LIFE`, `ON_CONTROLLER_GAINS_LIFE`, `ON_CONTROLLER_LOSES_LIFE`
- 13.10: `SAGA_CHAPTER_I` to `SAGA_CHAPTER_VI`, `ON_SAGA_FINAL_CHAPTER_ABILITY_RESOLVES`,
  `ON_SAGA_FINAL_CHAPTER_ABILITY_TRIGGERS`
- 13.11: `CHAOS_TRIGGERED`, `ON_CONTROLLER_SETS_SCHEME_IN_MOTION`, `ON_EQUIPMENT_ATTACHED`,
  `ON_ALLY_ROOM_FULLY_UNLOCKED`, `ON_CONTROLLER_BECOMES_MONARCH`, `ON_OPPONENT_BECOMES_MONARCH`,
  `ON_CONTROLLER_LOSES_COIN_FLIP`, `ON_PLAYER_LOSES_GAME`, `ON_CHAMPIONED`,
  `ON_ALLY_AURA_ATTACHED_TO_OPPONENT_NONLAND_PERMANENT`, `ON_EQUIPMENT_ATTACHED_TO_CREATURE`,
  `ON_SELF_BECOMES_CREWED`

**Tests changed:** none. Step 1 changes no engine or card code.

## Prompt

Replace `<N>` with the step number:

```
Implement step <N> of effect-slot-refactor.md. First read the whole file (Why, Design, Ground
rules) and step <N>'s section, and check in the Status table that every step it depends on is done;
if one isn't, stop and tell me. Follow the ground rules: read agent-docs/ARCHITECTURE.md before
touching engine code, keep new state copyable for simulations, convert cards only by script,
verify every CR number with the rules MCP, and test behaviour through the engine. Run only the
tests for what you touched — never the full suite; ask me to run it. When the step is done, mark
it done in Status, add anything later steps need under Findings, and tell me which tests changed
and why. Don't commit until I say so.
```
