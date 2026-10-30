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
| 1 | Slot inventory and baseline | open |
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
