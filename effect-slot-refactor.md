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
| 2 | Ability model types | done |
| 3 | `Card` stores abilities behind the slot adapter | done |
| 4 | Granted, temporary and delayed triggers store abilities | done |
| 5 | Spell abilities leave the enum | done |
| 6 | Static abilities leave the enum | done |
| 7 | Activated and mana abilities in one list | done |
| 8 | Replacement effects, special actions and markers leave the enum | done |
| 9 | Rules events for zone changes, and shadow mode | done |
| 10a | Trigger index and the generic trigger pipeline; the pilot in shadow mode | done |
| 10b | Switch the pilot to the pipeline, convert its cards, delete its slots | done |
| 11 | Creature-death triggers and self-inclusion | done |
| 12a | The rest of the zone-change family, except `ON_ENTER_BATTLEFIELD` | done |
| 12b | `ON_ENTER_BATTLEFIELD` | done |
| 13 | Remaining event families (repeatable) | done (runs 1 to 11b: every family; `EffectSlot` has no constant) |
| 14 | Trigger options replace trigger-only wrapper effects | done (repeatable by family: run 1, the source's own zone changes; run 2, creature death; run 3a, other zone and permanent changes but "another object enters"; run 3b, "another object enters"; run 4a, the own enters abilities' options read, and the "this or another" enters merges; run 4b, the own enters abilities' intervening "if"s as options; run 4c, every own enters intervening "if" checked as it triggers; run 5, steps and phases; run 6a, the spell, land-play and activation abilities' generic wrappers; run 6b, `SpellCastTriggerEffect` as the cast trigger's filter, options and effects; run 6c, the other spell, copy and land-play condition-as-effect classes as filters; run 7, combat; run 8, damage; run 9, counters; run 10, becomes the target; run 11, library, draw and discard; run 12, tap and untap; run 13, life; run 14, sagas; run 15, everything else; run 16a, the leftovers that need no collector rewritten: the `onlyIfSacrificed` flags, event conditions, the CR 603.5 may fixes, the Eerie merges; run 16b, the spell-cast and spell-copy collectors read the ability and their legacy records go; run 16c, the land-play and activation collectors read the ability and their wrappers go; run 16d, family 2's rest: no spell, land-play or activation ability has a legacy view, every reader judges their options, and the cards' last family 2 wrappers go; run 16e, family 1 (steps and phases): every step reader reads the abilities and judges their options; run 16f, family 3 (combat): every attack, block and combat damage collector reads the abilities through `CombatAbilityReader`; run 16g, family 4 (damage): every damage collector reads the abilities through `DamageAbilityReader`; run 16h, family 5 (counters): every counter collector reads the abilities through `CounterAbilityReader`; run 16i, family 6 (becomes the target): every becomes-the-target collector reads the abilities through `TargetAbilityReader`; run 16j, family 7 (library, draw and discard): every library collector reads the abilities through `LibraryAbilityReader`; run 16k, family 8 (tap and untap): every tap collector reads the abilities through `TapAbilityReader`; run 16l, family 9 (life): every life gain, loss and payment collector reads the abilities through `LifeAbilityReader`; run 16m, family 10 (sagas): no chapter or final-chapter ability has a legacy view, and the final-chapter collectors read the abilities through `SagaAbilityReader`; run 16n, family 11's player and keyword actions: every `ActionWatch` collector reads the abilities through `ActionAbilityReader`; run 16o, family 11's rest: every `MiscWatch` collector and the planar reader read the abilities through `MiscAbilityReader`; run 16p, the zone changes but "when this enters" and the permanent changes: no such ability has a legacy view, and the readers outside the trigger pipeline read them through `ZoneChangeAbilityReader`; run 16q, "when this enters": no ability has a legacy view; the own enters collector reads the abilities and judges their limit, the other readers read `Card.getOwnEntryEffects()`, and `OnceOnlyTriggerEffect` is gone; run 16r, every triggered ability is one object on the stack: the abilities of one Oracle ability are merged by script, the own enters and combat collectors group effects by ability, and `TriggerMode` is gone; run 16s, `ConditionalEffect`'s `interveningIf` / `triggerTimeOnly` flags are gone: a `ConditionalEffect` is an effect's "if", and every collector judges the intervening "if" and event condition from the ability; run 16t, the trigger-only wrapper classes: the `Triggering…ConditionalEffect` filters, `SelfExiledFromBattlefieldEffect`, `OncePerTurnPerCreatureTriggerEffect` and `OncePerTurnTriggerEffect`'s keyed, per-grant, mark-on-acceptance and first-time forms are gone, their cards written with trigger filters, `TriggerLimit`s and `TriggeringCardMatches`; Bill Potts' `OncePerTurnTriggerEffect` and the may-unwrapping moved to step 15, with the user) |
| 15 | Effects read the trigger event (repeatable) | open (run 1, with the user: the dying card, `DyingCreatureCardAwareEffect` gone; effects read the stack entry's triggering card; run 2, with the user: the departed permanent's last-known information, its 13 `…AwareEffect` interfaces gone; effects read the stack entry's triggering permanent; run 3: the rest of the departed object, the last-known rebuilds and baked dying-card ids run 2 kept, 4 classes gone and 5 lose their bound ids; run 4: the departed permanent's trigger-time rebuilds and skips run 3 kept, 6 classes gone, their skips trigger filters; run 5: the last departed-permanent rebuilds of `LegacyTriggerBindings` but the counter distribution, 3 classes gone, modular's "you may" and flag on the card; run 6, with the user: the death counter distribution reads its total from the entry and announces its targets and division as it goes on the stack (a rules fix), Martyr's Bond's "it" is the triggering permanent; run 7: the enters-side rebindings of the source to the entering permanent, Confusion in the Ranks' "another player" and "it" and Lucy MacLean's exclusion read the triggering permanent, its current types a rules fix; run 8: the damage source, `DamageSourceAwareEffect` and `DamageSourceControllerAwareEffect` gone, "that source" is the stack entry's triggering permanent and "that source's controller" its controller as the ability resolves (a rules fix for five cards), 4 effects replaced by existing ones with `TRIGGERING_PERMANENT_CONTROLLER` recipients, "each source" a trigger option on the cards; run 9: the event's permanents, `CombatDamageDealerAwareEffect` and `AttackingCreaturesAwareEffect` gone, "them" / "each of them" are the stack entry's triggering permanents and attacking snapshots, "whenever you attack with one or more [filter]" a trigger filter, 6 effects replaced by existing ones, one dead effect gone; run 10: the event's amount, `CombatDamageAmountAwareEffect` gone, every combat damage to a player entry carries the damage as its event value and the targeted choices carry it into theirs, Froghemoth's "up to that many" an event-aware target count, the graveyard targets restricted by the damage still fixed as they are chosen; `CombatExcessDamageAwareEffect` left for its own run; run 11: the excess damage, `CombatExcessDamageAwareEffect` gone, every "deals combat damage to a creature" entry carries the excess damage dealt to that creature as its event value and the collector judges the abilities' intervening "if" against it, Overclocked Electromancer an `EventValueAtLeast(1)` intervening "if" and `EnergyCountersEffect(new EventValue())`); run 12: the event's cards, `TriggeringCardsAwareEffect` gone, "those cards" / "the discarded cards" are the stack entry's triggering cards, Polluted Cistern's drain the existing `LoseLifeEffect` with a `CardTypesAmongTriggeringCards` amount; run 13a: the chosen card, the accepted players and the produced mana colors, `ChosenCardAwareEffect`, `AcceptedPlayersAwareEffect` and `ProducedManaColorAwareEffect` gone, the choices set the stack entry's chosen card, accepted players and produced mana colors and insert their follow-ups unchanged, a held-out card rides in the may ability, Hinterland Chef the existing `PerpetuallyGrantChosenCardCharacteristicsEffect`; `GrantingPermanentAwareEffect` split off as run 13b; runs 13b to 17 are planned in the step's run table) |
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

This step was split, with the user, in two:

- **10a** builds the pipeline and its pieces, maps the pilot slots to `Trigger`s and runs shadow mode over the
  pilot's card tests (items 1 and 2).
- **10b** switches the slots, converts their cards and deletes the constants and collector code (items 3 to
  5), and times the pilot's card test classes.

### Step 11: Creature-death triggers and self-inclusion

**Depends on:** 10b.

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

This step was split, with the user, in two:

- **12a** moves every zone-change slot except `ON_ENTER_BATTLEFIELD`, including the three
  owner-keyed creature put-into-graveyard slots step 11 left (`ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD`
  and its two `GRAVEYARD_` forms).
- **12b** moves `ON_ENTER_BATTLEFIELD` alone.

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

This step was split, with the user, by family, as step 13 was; run 3 was split again into 3a and 3b, with the user,
because of its size (690 abilities), run 4 into 4a and 4b, with the user, for the same reason (about 600 wrapped
abilities in 941 card classes, and about 60 engine and AI readers that inspect the own enters effects), and 4b again
into 4b and 4c, as the user allowed, so that 4b stays a conversion that keeps behaviour and 4c holds the rules fixes; run 6 into 6a, 6b and 6c,
with the user, because of its size (about 1,190 card classes, 980 of them `SpellCastTriggerEffect` abilities); run 16 into 16a and 16b
onward, with the user, because deleting the wrapper classes, `TriggerMode` and the `interveningIf` flag means every
collector reads options and groups effects by ability (681 legacy-view reads in about 60 main files, the 43,700
lines of `service/trigger`). Each run converts every wrapper kind of one
family's abilities, makes that family's collectors read options and filters instead, and deletes the wrapper classes
no other family still uses. The order:

| Run | Family |
|---|---|
| 1 | The source's own zone changes (step 10's pilot): dies, sacrificed, leaves the battlefield, put into a graveyard, milled, all `ObjectRelation.THIS` |
| 2 | Creature death (step 11's `CreatureDeathWatch`), with the 32 "this or another [restricted] creature" cards merged |
| 3a | The other zone changes and the permanent changes (step 12a's `ZoneChangeWatch`) but "another object enters", with their split "this or another" cards merged |
| 3b | "Another object enters" (step 12a part 2's entering watches) |
| 4a | The source's own enters abilities (step 12b's `EtbTriggerService`): the collector and every reader of them read the abilities' options, the 60 "this or another" enters cards merged, the cards 3b left for run 4 |
| 4b | The rest of the own enters abilities' wrappers: the intervening-if `ConditionalEffect`s (about 520), by script; the Oracle wording decides which are intervening "if"s |
| 4c | The own enters intervening "if"s the collector checks only as they resolve (CR 603.4): the collector checks every option condition as the ability triggers, with what it needs to know then (the sacrificed card), and the run 4b `KEEP` cards |
| 5 | Step 13's family 1: steps and phases |
| 6a | Step 13's family 2 (spells, land plays, activations): the generic wrappers around its abilities (intervening-if `ConditionalEffect`, `OncePerTurnTriggerEffect`, the land-play `TriggeringCardConditionalEffect`, the activation `TriggeringPermanentConditionalEffect` and `Triggering…AbilityConditionalEffect`s) |
| 6b | Family 2: `SpellCastTriggerEffect`'s spell filter and conditions become `SpellTrigger` filters and options, its target filter a target group, its mana cost a `MayPayManaEffect`, with the wrappers nested inside it |
| 6c | Family 2: the other condition-as-effect classes (`NthSpellCastTriggerEffect`, `SpellCopyTriggerEffect`, `KickedSpellCastTriggerEffect`, the `First…SpellCastTriggerEffect`s, `SpellCastFromHandTriggerEffect`, `PlayFromOutsideHandTriggerEffect`, …) |
| 7 to 15 | Step 13's families 3 to 11, in its order |
| 16a | What no family owns and needs no collector rewritten: the `onlyIfSacrificed` flags, the `atTriggerTime` and die-result conditions as the trigger's event condition, the may questions asked before the stack (CR 603.5), the Eerie merges |
| 16b | The legacy view's retirement begins: the spell-cast and spell-copy collectors read the filtered abilities themselves |
| 16c | Family 2's rest: the filtered land-play abilities and every activation ability are read by their collectors themselves; the land-play zone records and the `Triggering…AbilityConditionalEffect`s go |
| 16d | Family 2's rest: the unfiltered spell and land-play abilities with options, the "when you cast this spell", "when this spell is countered", suspended-card and mana-ability-resolves readers, and the wrappers family 2 cards still author; no family 2 ability has a legacy view |
| 16e | Step 13's family 1 (steps and phases): every step reader reads the abilities and judges their options through `StepAbilityReader`; no step ability has a legacy view |
| 16f | Step 13's family 3 (combat): every attack, block and combat damage collector reads the abilities through `CombatAbilityReader` (filter, limit and multiplicity from the ability); no combat ability has a legacy view, and `TriggerMode.PER_BLOCKER` / `ONCE_PER_BLOCK` and `BatchedCombatDamageToYouTriggerEffect` are gone |
| 16g | Step 13's family 4 (damage): every damage collector reads the abilities through `DamageAbilityReader` (filter, limit and "each source" from the ability); no damage ability has a legacy view, and `PerDamageSourceTriggerEffect` is gone |
| 16h | Step 13's family 5 (counters): every counter collector reads the abilities through `CounterAbilityReader` (filter, "you put" / "each counter" event and limit from the ability); no counter ability has a legacy view, and `YouPutCounterOnControlledCreatureTriggerEffect`, `YouRemoveTimeCounterFromExiledCardTriggerEffect` and `TriggerMode.PER_COUNTER` are gone |
| 16i | Step 13's family 6 (becomes the target): every becomes-the-target and target-choice collector reads the abilities through `TargetAbilityReader` (filter, "you control" event and limit from the ability); no target ability has a legacy view, and `TriggeringSpellControllerConditionalEffect`, `AbilityTargetChoiceTriggerEffect` and `TargetChoiceTriggerEffect` are gone |
| 16j | Step 13's family 7 (library, draw and discard): every draw, discard, cycle, scry, surveil, library search and shuffle collector reads the abilities through `LibraryAbilityReader` (filter, own event, count and limit from the ability); no library ability has a legacy view, and `CyclingTriggerEffect`, `OpponentCausedDiscardTriggerEffect`, `NthCardDrawTriggerEffect`, `FirstOrSecondCardDrawTriggerEffect`, `ExceptFirstDrawStepTriggerEffect`, `FirstDrawStepDrawOnlyTriggerEffect`, `DrawTriggerEffect` and the four counting `OncePerTurnTriggerEffect` factories are gone |
| 16k | Step 13's family 8 (tap and untap): every tap, untap and tap-for-mana collector reads the abilities through `TapAbilityReader` (filter, "for the first time during your turn", "one or more" and limit from the ability); no tap ability has a legacy view, and `OncePerTurnPerCreatureTriggerEffect`'s `global` component is gone |
| 16l | Step 13's family 9 (life): every life gain, loss and payment collector reads the abilities through `LifeAbilityReader` (own event, "for the first time each turn" and limit from the ability); no life ability has a legacy view, an ability with a life and a legacy condition (MACH-1) keeps one for the other condition only, and `FirstOpponentLifeLossEachTurnTriggerEffect` and `OncePerTurnTriggerEffect`'s `firstLifeGain` / `firstLifeLoss` components are gone |
| 16m | Step 13's family 10 (sagas): no chapter or final-chapter ability has a legacy view; the final-chapter collectors read the abilities through `SagaAbilityReader` ("triggers only once each turn" from the ability), and the ability rejects the options they don't read as it is built |
| 16n | Step 13's family 11, its first half (step 13 run 11a's player and keyword actions): every `ActionWatch` collector reads the abilities through `ActionAbilityReader` (the explore, exploit and scheme filters and "triggers only once each turn" from the ability); no action ability has a legacy view, and `SchemeSetInMotionTriggerEffect` and `SchemeTriggerCollectorService` are gone |
| 16o | Step 13's family 11, its second half (step 13 run 11b): every `MiscWatch` collector (control change, attaching, crew, saddle and station, Room doors and full unlocks, day and night, state triggers) and the planar reader read the abilities through `MiscAbilityReader` (the door, the first crew or saddle each turn and "triggers only once each turn" from the ability, a state trigger's game state); no family 11 ability has a legacy view, and `TriggeringRoomDoorConditionalEffect` is gone |
| 16p | The zone changes but "when this enters" and the permanent changes (steps 10 to 12a's pipeline families): no such ability has a legacy view; the readers outside the pipeline (face-up planes, schemes, registered abilities) read them through `ZoneChangeAbilityReader`, and the ability rejects what the pipeline can't read as it is built |
| 16q | The own enters abilities (`EtbTriggerService` and its about 55 readers): no ability has a legacy view; the collector reads the abilities and judges their limit, the readers read the own-entry effects (`Card.getOwnEntryEffects()`); `legacyEffects()`, `hasLegacyView()`, the `getLegacy…` getters, `Trigger.collectedAsAbility()` and `OnceOnlyTriggerEffect` are gone |
| 16r | The own enters collector and the combat collectors that split by `TriggerMode.INDEPENDENT` put each ability on the stack as one object, after a card script merges the abilities of one Oracle ability; `TriggerMode` is deleted |
| 16s | `ConditionalEffect`'s `interveningIf` / `triggerTimeOnly` flags are deleted: the cards' intervening "if"s and event conditions are `TriggerOptions` and every collector judges them from the ability |
| 16t | The trigger-only wrapper classes left are deleted (the `Triggering…ConditionalEffect` filters, `SelfExiledFromBattlefieldEffect`, `OncePerTurnTriggerEffect`'s forms but Bill Potts', `OncePerTurnPerCreatureTriggerEffect`); the event bindings and resolution effects some of them were mistaken for, Bill Potts' wrapper and the may-unwrapping go with step 15 |

Mark the step `done` only after the last run of 16.

Closed after run 16t, with the user: Bill Potts' `OncePerTurnTriggerEffect` and the pipeline's may-unwrapping,
the two things the "Done when" above still names, moved to step 15, because both need an effect that reads the
trigger event (step 15 lists them).

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

Moved here from step 14 after its run 16t, with the user (step 14's Findings, runs 16q to 16t, give the details):

- Bill Potts' `OncePerTurnTriggerEffect`: its one ability has a cast and an activation trigger condition whose effects
  are two event bindings, so it becomes one `TriggeredAbility` with `TriggerLimit.ONCE_EACH_TURN` once one effect copies
  "that spell or ability" from the trigger event; then `OncePerTurnTriggerEffect`, `OncePerTurnTriggerSupport` and the
  cast and activation collectors' handling of it go.
- The pipeline's may-unwrapping (16s's 31 event bindings and Pontiff of Blight): remove it as their effects read the
  event. Where the old path asked a may question before the ability was on the stack, the new path asks it on
  resolution (CR 603.5); list those cards in Findings.
- The event bindings and resolution effects step 14 found among the wrappers: `AllyCombatDamageTriggerEffect`,
  `EvolveTriggerEffect`, `ChampionOfTheHareishTriggerEffect`, `SpellCastTriggerEffect` (emblems), Olivia's
  `StateTriggerEffect`, `TriggeringPermanentEntryExclusionEffect`; Oran-Rief Hydra's and Emeria Shepherd's "instead"
  as an effect "if" on the land's last-known subtypes; and the collectors 16s lists that judge an authored conditional
  by its condition's type as the ability triggers.

**Runs.** Runs 1 to 11 were scoped one at a time, each adding the stack-entry state for one kind of event data (the
triggering card, the triggering permanent's last-known snapshot, the triggering permanents, the event value, the
damage source). Run 12 added the triggering cards. With that state in place, the rest is mostly swapping effects onto
fields that already exist, so it goes in larger runs, agreed with the user after run 12. A "start step 15" takes the
first row that isn't done, without asking for a scope. Counts were taken at `57d5915df9`: the 93 classes in
`model/effect/` with `Triggering` in their name, grouped by what they read, and the card classes that construct one.

| Run | Scope | Size at `57d5915df9` |
|---|---|---|
| 13a | done: three of the four `…AwareEffect` interfaces left, none of them a trigger event's: `ChosenCardAwareEffect`, `AcceptedPlayersAwareEffect`, `ProducedManaColorAwareEffect`. Each binding moves to the stack entry, or to the pending choice or may ability that carries it, so no choice handler or collector rebuilds an effect | 13 implementers |
| 13b | `GrantingPermanentAwareEffect` (split from 13, see run 13a's Findings): the granting permanent reaches the stack entry from the granted ability, so `GrantedTriggeredAbilitySupport` and the two grant-until handlers stop rebuilding effects | 6 implementers, 7 cards |
| 14 | The `…TriggeringSpell…` classes, and Bill Potts' `OncePerTurnTriggerEffect` (its effects copy "that spell or ability" from the trigger event): each becomes an existing effect reading the stack entry's triggering spell where one fits, otherwise it keeps its class but loses its bound data. `OncePerTurnTriggerEffect` and `OncePerTurnTriggerSupport` go | 31 classes, 39 cards |
| 15 | The classes that read the triggering permanent, creature, land, attacker, Aura, Equipment or artifact (`…TriggeringPermanent…`, `…TriggeringCreature…`, `…TriggeringLand…`, `…TriggeringAttacker…`, `TriggeringObjectController…` and the like), onto the triggering-permanent fields of runs 2 to 9 | 42 classes, 72 cards |
| 16 | The classes that read the triggering card or player (`…TriggeringCard…`, `…TriggeringPlayer…`, the triggering scheme, the Aura card in a graveyard), and `StackEntry.triggeringCardIds` merged into the triggering cards of run 12 | 20 classes, 72 cards |
| 17 | The pipeline cleanup: the may-unwrapping (asking on resolution, CR 603.5, and listing each card whose question moves), the graveyard-target rebuilds kept since run 3 (with Venerable Warsinger's and Soundwave's), `DamagedPlayerControlsTargetEffect`'s binding, and the event bindings and "instead" effects step 14 found among the wrappers (the list above). After it, the step's "done" condition is checked | 44 `unwrap` mentions in `service/trigger` |

A run may split a row (17a, 17b) when its first family's tests fail, and records the split in Findings. Every run
coordinates with `agent-docs/EFFECT_MERGE_PLAN.md`, converts cards and tests by script per letter module, and runs
the card test classes of every card that constructs a changed class.

Each run records in Findings which group it moved and what is left. Mark the step `done` only when
no `…AwareEffect` remains, the pipeline no longer rewrites or unwraps effects, and `OncePerTurnTriggerEffect` is gone.

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

### Step 2

**Types.** Nothing outside the new types' own tests uses them yet.

- `model.Ability`: a sealed marker interface that permits `SpellAbility`, `ActivatedAbility`,
  `TriggeredAbility` and `StaticAbility`. It has to live in `model`: a sealed type in the unnamed module
  can only permit classes of its own package, and `ActivatedAbility` is there. `ActivatedAbility` is now
  `non-sealed` and implements `Ability`; nothing else about it changed.
- `model.SpellAbility(List<EffectRegistration> effects)`.
- `model.StaticAbility(List<CardEffect> effects, AbilityZones zones)`. Its one-argument constructor
  gives the battlefield.
- `model.TriggeredAbility(List<TriggerCondition> conditions, TriggerOptions options, List<EffectRegistration> effects)`.
  Its convenience constructor takes one `Trigger` and uses that trigger's default zones.
- `model.AbilityZones(Set<Zone> zones)`, with `of(...)`, `functionsIn(Zone)` and constants for the
  battlefield, graveyard, hand, exile and command zone.
- `model.trigger.Trigger`: a sealed interface with `defaultZones()`, the battlefield unless a trigger
  overrides it (CR 113.6, 113.6k). For now it permits only `LegacySlotTrigger(EffectSlot slot)`.
- `model.trigger.TriggerCondition(Trigger trigger, AbilityZones zones)`.
- `model.trigger.TriggerOptions(Condition interveningIf, TriggerLimit limit, TriggerMultiplicity multiplicity)`,
  with `DEFAULT` and the withers `withInterveningIf`, `withLimit` and `withMultiplicity`.
- `TriggerLimit`: `NONE`, `ONCE_EACH_TURN`, `DO_ONLY_ONCE_EACH_TURN` (CR 603.2h) and `ONCE`.
- `TriggerMultiplicity`: `EACH` (CR 603.2c) and `ONE_OR_MORE`.

**Invariants, each with a unit test.**

- Every list and set is an unmodifiable copy. Every ability has at least one effect, a triggered ability
  has at least one condition, and `AbilityZones` has at least one zone.
- The first effect of a spell or triggered ability can't be marked simultaneous with the effect before
  it, because the effect before it would be in another stack object. **Step 3 must keep each
  `addSimultaneousEffects` group inside one ability**, even in slots whose collectors group as *each*.
- Every registration in a spell ability has `TriggerMode.NORMAL`. That is true of every card today: no
  `SPELL` registration has a mode.
- `LegacySlotTrigger` accepts every slot except `SPELL`, `STATIC` and `COMMAND_ZONE_STATIC`. Step 3
  therefore builds a `SpellAbility` for `SPELL`, and a `StaticAbility` for the battlefield (`STATIC`) or
  the command zone (`COMMAND_ZONE_STATIC`). The other non-trigger slots (`ON_TAP`, `ON_SACRIFICE`, the
  replacement, special-action and `GRANT_*` slots) go into legacy triggered abilities until steps 7 and 8.
- A `LegacySlotTrigger` is the only condition of its ability, and that ability's options are `DEFAULT`.
  Its collectors and wrapper effects stay the one source of its trigger-level properties.
- `LegacySlotTrigger.defaultZones()` reads the slot name's prefix: `GRAVEYARD_`, `COMMAND_ZONE_`,
  `EXILE_`, `EXILED_` or `HAND_`, and the battlefield otherwise. Nothing reads it yet. The collectors still
  decide where a legacy slot works, and some unprefixed slots look in other zones (the inventory's Zone
  column).

**Where this differs from the Design.**

- **A triggered ability has a list of trigger conditions, each with its own zones**, not one `Trigger`.
  CR 113.6k (checked) lets the trigger conditions of one ability function in different zones (Absolver
  Thrull: "When this creature enters or the creature it haunts dies"). "Whenever this creature enters or
  attacks" is also one ability, and a "triggers only once each turn" limit covers both conditions. Today
  such a card registers in two slots and has two abilities; the step that moves the second family can
  merge them.
- **`TriggerMultiplicity` has two values.** The `TriggerMode` values don't each become a multiplicity.
  Which occurrences one event contains (a creature, a token, a blocker, a counter) is a property of the
  event, decided when its family moves. For example, "becomes blocked by a creature" triggers once for
  each blocker (**CR 509.3d**, checked), but "becomes blocked" triggers once (**CR 509.3c**, checked):
  those are two trigger events, not two multiplicities. Mapping for step 14:
  - `NORMAL`, `PER_COUNTER`, `PER_TOKEN` and `PER_BLOCKER` become `EACH`. Step 13.5 checks the counter
    event's occurrences against the card rulings.
  - `ONCE_PER_BATCH`, `ONCE_PER_BLOCK` and `LIBRARY_ARRIVALS_BATCHED` become `ONE_OR_MORE` over step 9's
    event batches.
  - `INDEPENDENT` becomes a separate `TriggeredAbility`. It describes grouping, not an option.
  - `TOKEN_CREATION` becomes a token-creation trigger event, not an enters trigger.
- **Limits.** `OnceOnlyTriggerEffect` becomes `ONCE`. `OncePerTurnTriggerEffect` becomes `ONCE_EACH_TURN`,
  or `DO_ONLY_ONCE_EACH_TURN` when `markOnAcceptance` is set. Its `first…` flags are trigger conditions,
  not limits, and `OncePerTurnPerCreatureTriggerEffect` is one too. Examples:
  - Deathless Knight (`ELD` 208): "When you gain life for the first time each turn".
  - Stalwart Successor (`TDM` 227): "if it's the first time counters have been put on that creature this
    turn" is an intervening "if".

  Step 14 makes them `Trigger` filters or intervening-if conditions.
- **Effects are `EffectRegistration`s**, reused rather than replaced. They keep the simultaneous flag,
  which the Design asks for, and the legacy `TriggerMode` until step 14 deletes it, so step 3's
  `getEffectRegistrations(slot)` is a lossless view. `StaticAbility` holds plain `CardEffect`s: no
  `STATIC` or `COMMAND_ZONE_STATIC` registration has a mode or a simultaneous flag today.
- **`SpellAbility` has no target groups yet.** About 2,600 `target(…).addEffect(EffectSlot.…)` calls in
  card classes bind effects in 85 slots. Most are `SPELL` (1,311), then `ON_ENTER_BATTLEFIELD` (577),
  `STATIC` (155, mostly the enchant target of Aura spells) and `ON_ATTACK` (125). A target group therefore
  belongs to whichever ability holds the bound effects. Step 5 adds an immutable target-group record to
  `SpellAbility`, and the other slots' bindings stay on `Card` until their families move.
- **`SPELL` also holds casting costs of permanent spells**, such as `RepeatableAdditionalManaCost` for
  multikicker (Zethi, Arcane Blademaster; Zephyrim; Wolfbriar Elemental). These are not spell abilities,
  which only instants and sorceries have (CR 113.3a). Step 5 must find every one and give it another home.
- `Ability` declares no methods. `SpellAbility` has no zones because it always works on the stack.
  `ActivatedAbility` gets zones in step 7.

**Authoring API.** These names are settled. The number in parentheses is the step that adds each one.

| Today | Target |
|---|---|
| `addEffect(EffectSlot.SPELL, e)` | `addSpellEffect(e)` (5) |
| `addSimultaneousEffects(EffectSlot.SPELL, a, b)` | `addSimultaneousSpellEffects(a, b)` (5) |
| `target(f).addEffect(EffectSlot.SPELL, e)` | `target(f).addSpellEffect(e)` (5) |
| `getEffects(EffectSlot.SPELL)` | `getSpellAbility()` and `getSpellEffects()` (5) |
| `addEffect(EffectSlot.STATIC, e)` | `addStaticEffect(e)` (6) |
| `addEffect(EffectSlot.COMMAND_ZONE_STATIC, e)` | `addStaticEffect(AbilityZones.COMMAND, e)` (6) |
| `getEffects(EffectSlot.STATIC)` | `getStaticEffects(Zone.BATTLEFIELD)` (6) |
| `addEffect(EffectSlot.ON_…, e)` | `addTrigger(Triggers.…(…), e…)` (10 to 13) |
| a zone prefix or a trigger-level wrapper | `addTrigger(TriggerCondition, TriggerOptions, e…)` (10 to 14) |
| several conditions, or simultaneous effects | `addTriggeredAbility(TriggeredAbility)` (first step that needs it) |
| `getEffects(EffectSlot.ON_…)` | `getTriggeredAbilities()` (10) |

- The effects of one `addTrigger` call are one ability, in order.
- `Triggers` in `model.trigger` has one static factory per trigger event (`dies`, `enters`, `leaves`,
  `upkeep`, `castsSpell`, `becomesTarget`, …). Each returns its event family's `Trigger` record.
- How the event's object relates to the source is an `ObjectRelation` enum in `model.trigger`:
  - `THIS`;
  - `YOU_CONTROL` and `ANY`, which include this only if it matches the filter ("a creature you control");
  - `ANOTHER_YOU_CONTROL` and `ANOTHER`;
  - `THIS_OR_ANOTHER_YOU_CONTROL` and `THIS_OR_ANOTHER`: this counts whatever its characteristics, and the
    filter applies only to the other objects ("this creature or another Cleric");
  - `OPPONENT_CONTROLS`, `ENCHANTED` and `EQUIPPED`.
- For players, the existing `PlayerRelation` is used, extended as step 13.1 needs.
- Step 10 adds `Triggers`, `ObjectRelation` and the first event family record, the zone-change trigger.

**Three cards converted by hand**, as the target for the scripts:

- **Char** (`RAV` 117, step 5): "Char deals 4 damage to any target and 2 damage to you."

  ```
  before: addSimultaneousEffects(EffectSlot.SPELL,
                  new DealDamageToAnyTargetEffect(4),
                  new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER));
  after:  addSimultaneousSpellEffects(
                  new DealDamageToAnyTargetEffect(4),
                  new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER));
  ```

  The card holds one `SpellAbility` with two registrations, the second marked simultaneous with the first.
  `SpellAbilityTest` builds exactly this.
- **Rotlung Reanimator** (`ONS` 164, steps 10 and 11): "Whenever this creature or another Cleric dies,
  create a 2/2 black Zombie creature token."

  ```
  before: addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.blackZombie(1));
          addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                  new CardSubtypePredicate(CardSubtype.CLERIC), CreateTokenEffect.blackZombie(1)));
  after:  addTrigger(Triggers.dies(ObjectRelation.THIS_OR_ANOTHER,
                          new PermanentHasSubtypePredicate(CardSubtype.CLERIC)),
                  CreateTokenEffect.blackZombie(1));
  ```

  Today the card has two abilities, though its Oracle text has one. After the change it has one
  `TriggeredAbility`: one battlefield condition, `DEFAULT` options, and one effect. A wrath that destroys
  it and two other Clerics still triggers it three times (**CR 603.10a**, checked).
- **Arden Angel** (`SLD` 1344, step 13.1): "At the beginning of your upkeep, if Arden Angel is in your
  graveyard, roll a four-sided die. If the result is 1, return Arden Angel from your graveyard to the
  battlefield."

  ```
  before: addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                  new ConditionalEffect(new SourceCardInGraveyard(),
                          new RollD4Effect(new ReturnSourceCardFromGraveyardToBattlefieldEffect(false))));
  after:  addTrigger(new TriggerCondition(Triggers.upkeep(PlayerRelation.SELF), AbilityZones.GRAVEYARD),
                  TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInGraveyard()),
                  new RollD4Effect(new ReturnSourceCardFromGraveyardToBattlefieldEffect(false)));
  ```

  Its effect moves it out of the graveyard, so it functions only there (**CR 113.6m**, checked). The
  `GRAVEYARD_` prefix becomes the condition's zones, and the intervening-if `ConditionalEffect` becomes
  the option, checked again on resolution (CR 603.4).

**Tests changed:** none. New tests in `magical-vibes-application` (`model/` and `model/trigger/`):
`AbilityZonesTest`, `SpellAbilityTest`, `StaticAbilityTest`, `TriggeredAbilityTest`,
`LegacySlotTriggerTest`, `TriggerConditionTest` and `TriggerOptionsTest`. The inventory was not
regenerated, because step 2 moves no slot. Its count of 53 domain types that hold an `EffectSlot` now
leaves out `LegacySlotTrigger`, which step 16 deletes.

### Step 3

**Storage.** `Card.abilities` is a `List<Ability>` in the order abilities were added, and
`Card.getAbilities()` returns it read-only. It holds no activated abilities: those stay in the four per-zone
lists until step 7. The slot `EnumMap`, its effect cache and the three Saga side maps are gone. A `Card` has
79 instance fields now, not 82 (`CardFreezeTest`). It also no longer allocates four `EnumMap`s of about 515
entries each.

**How `addEffect`, `addSimultaneousEffects` and `SpellTarget.addEffect` build abilities.** `SpellTarget` is
unchanged: it already calls `Card.addEffect`.

- `SPELL`: one `SpellAbility` holds every registration.
- `STATIC` and `COMMAND_ZONE_STATIC`: each effect is its own `StaticAbility`, with zones `BATTLEFIELD` or
  `COMMAND`. A trigger mode or a simultaneous group is rejected there. No card or engine path adds one.
- Every other slot: `TriggeredAbility`s with a `LegacySlotTrigger` and `DEFAULT` options. The new
  package-private `model.LegacySlotGrouping` decides the grouping. It was generated from the TSV's Grouping
  column:
  - `ALL`: 94 slots (the TSV's *all* minus `SPELL`). These include `ON_TAP`, `ON_SACRIFICE`, the eight
    `GRANT_*` slots, `ON_BLOCK` and the six `SAGA_CHAPTER_*` slots.
  - `ALL_BUT_INDEPENDENT`: 4 slots.
  - `EACH`: everything else, *split*, *varies* and *none* included.

  The step 1 recommendation counted the four *all-but-independent* slots twice: once to build from the
  Grouping column, once in "the 49" to keep one per registration. They use their real grouping, which is
  well defined.
- A group from `addSimultaneousEffects` always goes into one ability, whatever the slot's grouping. Joining
  an existing ability replaces that element with a new record, so the ability keeps its position, which is
  where its first registration was.

**The slot views.** `getEffects(slot)` and `getEffectRegistrations(slot)` read a `SlotIndex`. It is built
lazily from the list, held in a `volatile` field, and dropped by every mutator. It is immutable and refers
to no card, so copies share it until one of them changes. The old cache was a plain `EnumMap` that frozen
cards filled in from several threads. A view lists the registrations of the slot's abilities in list order.
That equals the order they were added, with one exception. In an *all-but-independent* slot, a plain
registration added after an `INDEPENDENT` one is listed before it. No card does that: the only cards with
`INDEPENDENT` in those slots, Champions from Beyond and Barbed Foliage, use it on every registration.
`isSimultaneousWithPrevious` searches the slots in ordinal order, as before. A `STATIC` view builds plain
`NORMAL` registrations.

**Changes outside `Card`.** The step's Done-when says nothing outside `Card` and `SpellTarget` changes.
These did, each because the step's own bullets needed it:

- `getEffectRegistrations` is now read-only. Before, it handed out the internal list, and two handlers
  edited it in place:
  - `ReturnSourceAsAuraEffectHandler` now calls the new `Card.removeSlotEffectsIf(BiPredicate<EffectSlot, CardEffect>)`.
  - `ExileTargetCreatureCardThenConjureSkeletonDuplicateEffectHandler` now calls `removeEffectsIf`.

  No other caller edits the returned list; the grep covered every module, tests included.
- `LayerSystemService.hashCard`: `forEachEffectSlotSize` is gone. An unfrozen card now hashes the number of
  its abilities and the identity of each one. Abilities are immutable and a change replaces one, so this
  also catches a change that keeps the count, which slot sizes missed:
  `LayeredBoardCacheTest#abilityReplacedInPlaceOnAnUnfrozenCardInvalidates`. `LAYER_SYSTEM.md` is updated.
- `TriggeredAbility` gets a fourth component, `SagaChapterTargets chapterTargets`, a new record of the
  filters, groups and required flag. There is also a three-argument constructor that gives
  `SagaChapterTargets.NONE`, plus `withEffects` and `withChapterTargets`. The `setSagaChapterTarget…`
  setters now change the chapter's ability, so the chapter's effects must be added first; otherwise they
  throw. `CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md` says so.
- One card set a chapter target before adding the chapter's effect: The Grand Evolution (`shared`), in
  chapter II. One line was moved by hand, which wasn't worth a script.

**Behaviour changes, all at edges that no current card or test reaches.**

- `removeEffectsIf` drops abilities left with no effects. When it removes the first effect of a
  simultaneous group, the next effect is no longer marked simultaneous. Before, that effect stayed marked
  and joined whatever effect came before it in the slot.
- `createRuntimeCopyWithFace` now takes the face's Saga chapter target groups with its abilities. Before,
  it copied the face's filters and required flags but kept the original card's groups.

**For later steps.**

- Engine copy paths (`CopySupport`, `PermanentCopierService`, `TokenCopySupport`, `MutationSupport` and
  others) still rebuild a card by calling `addEffect(slot, effect, mode)` slot by slot over
  `EffectSlot.values()`. Each copy's abilities are therefore in slot order, not the source's order, and
  lose the simultaneous flag, as they did before. The step that makes copies copy abilities (CR 707.2)
  fixes both.
- Cross-slot grouping isn't represented. One example: the three `ON_CREATURE_ATTACKS_YOU…` slots share one
  stack object on one card. It stays in the collector until that family moves.
- `slotOf` in `Card` switches over `Trigger`, which permits only `LegacySlotTrigger`. When step 10 adds
  another kind, that switch stops compiling. Abilities with a real trigger have no slot view, and the
  views should skip them.

**Tests changed.**

- `CardFreezeTest`: field count 82 → 79.
- New `CardAbilitiesTest` (`magical-vibes-application`, `model/`): grouping per slot kind, simultaneous
  groups, the read-only views, removal, Saga chapter targets, runtime copies and clearing.
- New `LayeredBoardCacheTest#abilityReplacedInPlaceOnAnUnfrozenCardInvalidates`.

**Test runs.** These are not card failures. The application tests run for this step (`StepTriggerServiceTest`,
`CardImmutabilityArchTest`, `SimulationCopyCompletenessTest`, `LayeredBoardCacheTest`, the step 2 type tests)
fail in exactly 18 cases, and the same 18 fail at HEAD without the change.
The card test classes of every Saga, every card with `addSimultaneousEffects` or `ON_BLOCK`, the
`INDEPENDENT` attack-trigger cards and the cards behind the two changed handlers (547 classes, 4,651 tests)
all pass. The full card suite was not run here.

### Step 4

**Granted abilities are `Ability` values, not just `TriggeredAbility`s.** The granted maps also hold
static abilities. Toxic and protection, for example, are read through
`getTemporaryTriggeredEffects(EffectSlot.STATIC)` and `getPersistentTriggeredEffects(EffectSlot.STATIC)`, and
three cards grant `STATIC` effects through `GrantEffectTo…`. So every holder stores an `Ability`. The new
`model.LegacySlots` is the one place that maps a slot and an effect to an ability and back:

- `ability(slot, effect)`: a `SpellAbility`, a `StaticAbility` of the battlefield or the command zone, or a
  `TriggeredAbility` with a `LegacySlotTrigger`;
- `grant` is the null-tolerant form, for optional grants;
- `abilities(...)` and `registrationAbilities(...)`, which keep trigger modes and put a simultaneous
  registration in the ability of the one before it;
- `triggered`, `slotOf`, `effectsOf` and `effectsIn(abilities, slot)`.

`Card.slotOf` now delegates to `LegacySlots.slotOf`. The class goes away with `EffectSlot` in step 16.

**Runtime state converted.** No runtime state is keyed by `EffectSlot` any more.

- `Permanent`: the four maps are now `temporaryAbilities`, `combatAbilities` and `persistentAbilities`
  (`List<Ability>`) and `untilNextUpkeepAbilities` (`Map<UUID, List<Ability>>`, keyed by the expiry player).
  They are hidden from Lombok. The readers keep their signatures (`getTemporaryTriggeredEffects(slot)`,
  `getPersistentTriggeredEffects(slot)`, the `add…TriggeredEffect(slot, effect)` adders). There are new
  `addTemporaryAbility`, `addPersistentAbility` and `removePersistentTriggeredEffectsIf(slot, filter)`.
  `getPersistentTriggeredEffects` now returns a read-only copy. `RemovePerpetualTriggeredAbilityEffectHandler`
  used to edit the list it returned, and now calls `removePersistentTriggeredEffectsIf`. A permanent no
  longer allocates four `EnumMap`s of about 515 entries each, which matters for simulation copies.
- `EndOfTurnStateField.TEMPORARY_TRIGGERED_EFFECTS` (kind `SLOT_LISTS`) became `TEMPORARY_ABILITIES` (kind
  `LIST`), and the `SLOT_LISTS` kind and `EndOfTurnStateShare.slotLists` are gone. `LIST` compares
  elements with `equals`, as `SLOT_LISTS` did within each slot. Abilities are value records, so the
  per-player shares split and recombine the same way.
- `StackEntry`:
  - `grantedAbilitiesOnEntry` and `persistentAbilitiesOnEntry` are `List<Ability>`, with read-only getters
    `getGrantedAbilitiesOnEntry()` and `getPersistentAbilitiesOnEntry()`.
  - `StackResolutionService` passes them to the new `Permanent` adders.
  - The dead default `CastSpellsFromGraveyardPermission.grantedTriggeredEffectsOnEntry()` had no overrides
    or readers and is deleted. `ORACLE_TEXT_EFFECT_MAP.md` named it for Serra Paragon, which actually uses
    `entryTriggeredAbilityGrant()`; the entry is corrected.
- `GameData.perpetualTriggeredAbilityGrants` is now a `Map<UUID, List<Ability>>` of immutable lists. It
  has helpers:
  - `grantPerpetualAbility(cardId, slot, effect)`: repeated grants stack, CR 113.2c (checked).
  - `grantPerpetualAbilityOnce`: the old "add if absent" writers.
  - `removePerpetualAbilitiesIf`, `perpetualAbilities(cardId)` and `perpetualAbilityEffects(cardId, slot)`.

  The 20 writing sites (in 19 handlers) were each a copy-the-map-and-merge block;
  `scripts/step4-perpetual-grant-writers.py` rewrote 19 of them. The random-hand-card handler declares a
  local inside its block, so the script refuses it and it was converted by hand, as were the one removal
  (`computeIfPresent`) and the four readers. The simulation copy now shares
  the immutable lists.
- `TemporaryGlobalTriggeredAbility` holds a `TriggeredAbility ability`. Its existing constructors keep their
  signatures, and `slot()` and `effect()` are derived, so its twenty-odd readers are unchanged.
- `CloneOperationState.additionalSlotEffects` became `additionalAbilities` (`List<Ability>`).
  `TokenCreationEvent.additionalEffects` became `additionalAbilities`. Both are converted from the effect
  records' slot maps where they are filled: `CloneService`, and `PermanentControlSupport.createOrdered`,
  whose public `applyCreateToken(…, Map<EffectSlot, List<EffectRegistration>>)` keeps its signature. They
  are added to the new card through the new `Card.addAbility(Ability)`. That method adds an ability the way
  its registrations would be added, so it follows the slot grouping of step 3.

**Effect and interaction records converted, by `scripts/step4-grant-records.py`.** Each one now holds
`Ability grantedAbility`, or `List<Ability> grantedAbilities` for the list forms. It keeps a constructor with
its old component list. A compact constructor that read the old components became that constructor's
prologue, which Java 25 allows before `this(…)`. Accessors under the old names derive the slot and effect,
so card classes and handlers did not change. The records:

- the `GrantEffectTo…` family (10) and `GrantTriggeredAbility{,ToCastSpell,ToCreatedPermanents}Effect`;
- `PerpetuallyGrant…` (10), `Choose{Card,Cards}FromHandToPerpetuallyGrantTriggeredAbilityEffect` and
  `SeekLibraryAndPerpetuallyModifySoughtCardEffect`;
- `RegisterGlobalTriggeredAbility…` (3);
- `PendingInteraction.PerpetualTriggeredAbilityCardChoice` and `…CardsChoice`.

**Not converted, and why.**

- The `…TriggerWatcher` records and the emblem effects hold no slot, only an effect with a fixed event.
  Wrapping them in a `LegacySlotTrigger` would mean inventing a slot. They get real `Trigger`s when their
  event families move (steps 11 to 13).
- Records that name a slot as a selector, not as an ability: `BecomeCopy…`'s `retainedEffectSlot` ("the
  copy keeps this card's abilities in slot X") and `RemovePerpetualTriggeredAbilityEffect`'s slot and
  effect type.
- The slot-keyed "additional abilities" maps in copy and token records (`additionalSlotEffects`,
  `CreateTokenEffect`'s token effects, `Conjure…`, `TurnFaceUpCopyEffect`). These are card data that card
  classes build with `Map.of(EffectSlot.…, …)`, and they hold `SPELL` and `STATIC` effects as well as
  triggers. Steps 5 and 6 should give them `List<Ability>` along with the authoring API. Where their
  contents reach runtime state, they are converted to abilities (above).

**For later steps.**

- The compatibility constructors and derived accessors on the converted records are the adapters; delete
  them when card classes construct abilities directly. `LegacySlots.grant` keeps the old behaviour, where a
  missing slot or effect meant no grant.
- A list-form record (`grantedAbilities`) derives `slot()` from its first ability, so it now needs at least
  one effect. Every current card passes one or more.
- `effect-slot-inventory.md` is the step 1 snapshot and was not regenerated. Its "Runtime state keyed by
  EffectSlot" section lists the fields this step removed.

**A step 3 regression, fixed here.** Step 3 made `Card` reject a `STATIC` registration with a trigger mode,
on the grounds that no card adds one. But `TokenCopySupport` adds a token copy's "except it has" effects as
`INDEPENDENT` in every slot, `STATIC` included. Three card test classes broke, and step 3's test selection
missed them: Firion, Wild Rose Warrior; Dollhouse of Horrors; and Brenard, Ginger Sculptor. A static or spell
ability doesn't trigger, so `Card` and `LegacySlots` now drop the trigger mode of a `STATIC`,
`COMMAND_ZONE_STATIC` or `SPELL` registration, and every static effect is its own ability, even in a
simultaneous group. The only paths whose behaviour changes are the ones that threw. Nothing reads the trigger
mode of a static or spell registration. `CardAbilitiesTest#aStaticEffectKeepsNoTriggerModeAndStaysItsOwnAbility`
replaces the step 3 test that asserted the rejection.

**Tests changed.**

- `CardAbilitiesTest`: `aStaticEffectCanNeitherTriggerNorBeSimultaneous` became
  `aStaticEffectKeepsNoTriggerModeAndStaysItsOwnAbility` (the regression above).
- `PassTheTorchTest`, `MappingTheMazeTest` and `LurkingSpinecrawlerTest` read the perpetual-grant map's old
  shape, and now read `perpetualAbilityEffects(cardId, slot)`. They were changed by
  `scripts/step4-perpetual-grant-tests.py`.
- New `GrantedAbilitiesTest` (`magical-vibes-application`, `model/`): the `LegacySlots` conversions and
  views, `Card.addAbility`, the `Permanent` durations and removal, the `StackEntry` on-entry abilities, the
  `GameData` perpetual-grant helpers, a converted grant record and `TemporaryGlobalTriggeredAbility`.
- `GameDataDeepCopyTest` (`magical-vibes-ai`): a permanent's granted abilities and the perpetual grants
  survive a simulation copy and stay independent.

**Test runs.** Application tests run for this step: `GrantedAbilitiesTest`, `CardAbilitiesTest`,
`CardFreezeTest`, `LayeredBoardCacheTest`, `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest`,
`StepTriggerServiceTest`, `TurnCleanupServiceTest` and `UntapStepServiceTest`. They fail in 29 cases, and
the same 29 fail at HEAD without the change: the 18 of step 3, plus 11 in `TurnCleanupServiceTest` and
`UntapStepServiceTest`. `GameDataDeepCopyTest` passes.
The card test classes of every card that uses a converted record, or an effect whose handler calls a
changed grant API, plus the card tests that read granted state, were run: 1,026 classes and 8,561 tests.
All pass except the 15 tests of the regression above, which pass after the fix. The full card suite was
not run here.

### Step 5

`EffectSlot.SPELL` is gone. Two decisions went beyond the step text, both taken with the user: casting costs
moved out of the spell ability now (step 2 said step 5 must), and the spell ability carries its own target
groups.

**Spell abilities.**

- `SpellAbility(List<EffectRegistration> effects, List<TargetGroup> targets)`, with a one-argument constructor,
  `withEffects` and `withTargets`. It rejects a `CostEffect` (CR 113.3a, checked: a spell ability is what the
  spell follows as it resolves), and the group at position `i` must have index `i`.
- Authoring: `addSpellEffect(e)`, `addSimultaneousSpellEffects(a, b, …)`, `target(f).addSpellEffect(e)`.
- Readers: `getSpellAbility()` (an `Optional`), `getSpellEffects()`, `getSpellEffectRegistrations()`.
- `Card.addAbility(SpellAbility)` still works and joins the card's spell ability.
- `LegacySlots.slotOf(SpellAbility)` is `null`, so a spell ability is in no slot view. `LegacySlots.ability` and
  `registrationAbilities` no longer build one, and `LegacySlotTrigger` no longer has a `SPELL` case.

**Casting costs** (CR 601.2b, 601.2f, checked) are a new `Card` field, `List<CostEffect> castingCosts`.

- `addCastingCost(c)` adds one; `getCastingCosts()` reads them.
- `getCastingCostsAndSpellEffects()` is costs first, then the spell effects. It is what `getEffects(SPELL)`
  returned, for the cast paths that pull costs out of one list (`AdditionalSpellCostService.extractAndRemove`)
  and put the rest on the stack. Free and prepared casts keep the costs in the stack entry until they are paid.
- The costs are copied with the card, copied from the text source by `createRuntimeTextBoxCopy` and from the
  face by `createRuntimeCopyWithFace`, and cleared by `becomeMissile` and `clearRulesTextAndAbilities`.
  `removeEffectsIf` and `removeSlotEffectsIf` reach them too; the slot argument is `null` for a cost or a spell
  effect.
- 485 card classes have casting costs, 109 of them only costs (permanents with multikicker, delve, sacrifice
  costs and so on), so those have no spell ability now. `CardFreezeTest`: 79 → 80 fields.

**Target groups.**

- The group data moved out of `SpellTarget` into an immutable `TargetGroup`, with the same getters plus
  `getDistinctScope()`. `SpellTarget` is now only the builder that `target(…)` returns: the card, the index,
  `addSpellEffect`, `addEffect`, `bindEffect`, `inDistinctScope` and `group()`. It no longer points back to a
  card from a group.
- `Card.getSpellTargets()` became `getTargetGroups()`, read-only (it used to hand out the internal list). It
  holds every group in declaration order.
- `SpellAbility.targets()` holds the spell's groups: the card's leading groups that are bound to a spell
  effect or to no effect (a positional group a sibling reads), up to the first group bound only to other
  abilities. `Card.syncSpellTargets` keeps it in step after every declaration, binding, scope change, copy and
  removal.
- `getSpellTargetGroups()` is the spell ability's groups, or every group for a card without one: an Aura spell's
  enchant ability defines its target (CR 303.4a, checked).
- A scan of all 32,371 card classes before and after: the spell's groups equal the card's for every card but 17.
  - Five have trailing trigger groups: Aggressive Biomancy, Cry of Contrition, Death Pulse, Mythos of Illuna,
    Seize the Soul.
  - Twelve have a spell ability whose groups all belong to triggers: the nine Rooms, Benediction of Moons,
    Dirge of Dread, Runaway Growth and Secret Arcade.

**Readers converted, by `scripts/step5-spell-ability.py`.** It takes each `getEffects(SPELL)` read and looks at
what its statement asks for:

- only cost types: `getCastingCosts()`; `.stream().filter(CostEffect.class::isInstance)` collapses into
  `getCastingCosts().stream()`;
- only other effect types, asked positively: `getSpellEffects()`;
- anything else, including `allMatch`, negative filters, positions and sizes: `getCastingCostsAndSpellEffects()`.

In main code this left 59 `getCastingCosts`, 41 `getSpellEffects` and 173 `getCastingCostsAndSpellEffects`
reads. The combined view behaves exactly as the slot did. Each combined read that only wants one side can be
narrowed by hand later.

**Converted by hand in the engine.**

- The copy loops over `EffectSlot.values()` now also copy the spell ability and costs, through the new
  `Card.addCastingCostsAndSpellEffectsFrom(source, keep)`: `CopySupport`, `TokenCopySupport`,
  `PermanentCopierService`, `MutationSupport`, `LayerSystemService.buildFullTextCopy` and
  `EachOpponentChooses…TokenCopy…`. Each spell effect is added on its own, so a copy still loses the simultaneous
  flag, as before (step 3 noted the same for slots).
- These all-slot scans also read the spell effects and costs, as the `SPELL` slot did before:
  `GameViewProjectionFactory`, `ChoiceHandlerService`, `CreatureControlService`, `TriggeredAbilityQueueService`,
  `PerpetuallyBoostCardEffectHandler` and the three `EffectResolution` "spell or ETB" checks.
- `SPELL` left the excluded-slot sets of the two `GainAbilities…` handlers and
  `CreateTokenWithTriggeredAbilitiesOfExiledCards…`, and the three `slot != SPELL` filters in
  `PredicateEvaluationService`. `TargetPolarityClassifier` reads the ETB slot, then the spell.
- `TargetLegalityService.effectiveGroupMaxTargets` used to read the gift limit through the group's back
  reference. It now takes the card.

**Readers switched to the spell's own groups.** These ask which groups the spell's effects are bound to:
`hasRequiredSpellOnStackTargetGroup`, the alternate-cost-only group check and `validateBeholdConditionalTargetGroups`
in `SpellCastingService`. None of the 17 cards reaches them, so nothing changes. The other 241 `getTargetGroups()`
reads were not switched:

- they run for spells and abilities alike, card types not known ahead of time;
- they mix the list with `Card`'s derived getters (`getMinTargets`, `getMaxTargets`, `getMultiTargetFilters`,
  `getTargetFilter`, `getEffectiveMin/MaxTargets`, `doesPositionAllowPlayerTargets`), which sum over every group.

Switching them needs spell-scoped versions of those getters. For all but the 17 cards it changes nothing. A
later step should do it, together with the trigger families that take their groups into `TriggeredAbility`.

**Cards and tests, by script.**

- `scripts/step5-spell-ability.py`, run per letter module, changed 7,421 card classes and 49 card test classes.
  It rewrote the engine, AI, networking and test-fixture sources with `--engine`, which also renames the
  `SpellTarget` type to `TargetGroup` and `getSpellTargets()` to `getTargetGroups()`.
- `scripts/step5-builder-costs.py` lifts a cost the first script left in a target chain
  (`target(f).addCastingCost(c)`) into its own statement. It ran on 28 card classes and one service test.
  Those costs were bound to the target group before. Nothing read a cost's binding.
- `scripts/step5-cost-types.py` lists the `CostEffect` subtypes both scripts use.
- `scripts/step5-docs.py` rewrote the agent-docs snippets. Each table doc has a note on what its `SPELL` label
  means now.
- One-line seds, verified by the scan:
  - the 17 MDFC fronts that read `backFace.getEffects(SPELL)`, no back face having costs, read
    `getSpellEffects()`;
  - Shadrix Silverquill reads `getTargetGroups()`.
- Builder variables in the engine (`SpellTarget x = card.target(…)` in `SpellCastingService` and
  `TriggeredAbilityQueueService`) were renamed back by hand after the type rename.

**For later steps.**

- Step 6: step 4 asked steps 5 and 6 to give the slot-keyed "additional abilities" maps `List<Ability>`. No card
  builds such a map with a `SPELL` key, so step 5 had nothing to convert there.
- Schemes keep their "set in motion" effects in the spell ability, and a few permanents (Animate Dead, Dance of
  the Dead, Grafted Identity, Spellweaver Volute, Runaway Growth, the Rooms) hold engine data there. CR 113.3a
  gives spell abilities to instants and sorceries only, so these belong to the step that moves their family.
- The combined `getCastingCostsAndSpellEffects()` has 173 readers. Narrow them as the cast code is touched; the
  list-stripping cast paths should eventually take the costs from `getCastingCosts()` and the effects from the
  spell ability.

**Tests changed.**

- `CardFreezeTest`: 79 → 80.
- `CardAbilitiesTest`, `GrantedAbilitiesTest`, `LegacySlotTriggerTest` and `TargetPolarityGuardTest`: `SPELL`
  became the spell ability (`LegacySlots` no longer builds one; the guard reads spell effects and costs).
- `CastingCostServiceTest`'s `spellWith(CostEffect…)` helper adds casting costs.
- `CardImmutabilityArchTest` keeps whitelisting the `SpellTarget` builder.
- Every other application, AI and card test was converted by the script with no change of meaning.
- New tests:
  - `CardAbilitiesTest`: costs kept apart, a cost rejected as a spell effect, a card with only costs, the
    spell's groups, trailing trigger groups, positional groups, scope changes, an Aura without a spell ability,
    copies, removal and selective copying;
  - `SpellAbilityTest`: costs rejected, group indices, no groups.

**Test runs.**

- Application, against HEAD (the same classes run in a HEAD worktree): 58 classes covering every changed
  service and model type. The failures equal HEAD's, 338 cases, mostly mock wiring in service tests.
  `CostEffectClassificationTest` (`RevealSourceChosenSubtypeCost`) and `CardEffectTargetingConsistencyTest`
  (`TargetPlayerExilesCardFromHandEffect`) fail at HEAD too, on files this step didn't touch.
- AI: 19 classes, 1,171 tests. The same 10 fail at HEAD.
- Card: the classes of every card with casting costs, the 17 cards above, the cards using simultaneous spell
  effects, back faces or the changed handlers' effects, and 400 random spell cards (2,878 classes, 24,124 tests). One test fails:
  `KetriaTest#chaosAuraStaysExiledWhenThereIsNothingItCanEnchant`. It is flaky at HEAD as well (it failed
  in 2 of 4 runs there and 1 of 4 with the change), so it is not a regression of this step. The full card
  suite was not run here.
- A scan that builds every card class confirmed that all 32,371 construct, and that each card's spell effects
  and casting costs equal what its `SPELL` slot held, card for card.

### Step 6

`EffectSlot.STATIC` and `EffectSlot.COMMAND_ZONE_STATIC` are gone. Three decisions went beyond the step text:
the readers ask for every static ability rather than the battlefield's, the hand and stack keywords got their
zones, and the slot-keyed ability maps of the token and copy records became ability lists (step 4 asked steps 5
and 6 for that).

**Static abilities.**

- Authoring: `addStaticEffect(e)`, `addStaticEffect(AbilityZones, e)` and `target(f).addStaticEffect(e)` (an
  Aura's enchant ability, CR 303.4a, checked). Each call adds one `StaticAbility` with one effect, as before.
- `StaticAbility.of(e…)` and `StaticAbility.of(List)` build one for a token's or a copy's ability list.
- Zones: `CardEffect.staticAbilityZones()` names the zones a static ability made of that effect functions from.
  It is the battlefield (CR 113.6, checked) except for:
  - `KickerEffect` (CR 702.33a), `BuybackEffect` (CR 702.27a), `GiftEffect` (CR 702.174a) and
    `CantBeCounteredEffect` (CR 113.6g): the stack;
  - `ReduceOwnCastCostEffect` (CR 113.6d): the stack. `ConditionalEffect` passes its wrapped effect's zones on,
    so a conditional reduction keeps them;
  - `SpliceEffect` (CR 702.47a): the hand.

  All CR numbers here were checked. `AbilityZones.STACK` is new.
- The Ur-Dragon's Eminence is one ability that functions in two zones, `AbilityZones.of(BATTLEFIELD, COMMAND)`
  (CR 113.6b, checked). It used to be the same effect registered in `STATIC` and in `COMMAND_ZONE_STATIC`.
  Rule with an Even Hand and Marchesa's Surprise Party have command-zone abilities.

**Readers.** The plan's table said `getEffects(STATIC)` becomes `getStaticEffects(Zone.BATTLEFIELD)`. It became
`getStaticEffects()`, every static ability whatever its zones, and `getEffects(COMMAND_ZONE_STATIC)` became
`getStaticEffects(Zone.COMMAND)`:

- The engine reads `STATIC` off cards in every zone: planes in the command zone, kicker on a card in hand, the
  reduction on a spell. A battlefield filter would have dropped kicker and the other zoned keywords, or forced
  them to keep a false battlefield zone.
- The engine already knows where a card is and which effect types it wants. The one reader that filters by zone
  is the command-zone scan.
- The two views differ for exactly the three command-zone cards. Those never reach a battlefield reader, and no
  engine code scans the command zone through `getStaticEffects()` (checked by grep).

There are 712 `getStaticEffects()` reads in main code and 4 `getStaticEffects(Zone.COMMAND)` reads. `Card` also
has `getStaticAbilities()`, `getStaticEffects(Zone)`, `addStaticAbilitiesFrom(source, keep)` and
`removeAbilityEffectsIf(BiPredicate<Ability, CardEffect>)`. `SlotIndex` caches the static abilities and effects
next to the slot views. `LegacySlots.slotOf` returns `null` for a static ability, so it is in no slot view, and
`LegacySlots.ability(slot, e)` only builds triggered abilities. `LegacySlots` gained `staticEffectsIn`,
`withEffect` and `label`.

**Granted static abilities.**

- `Permanent`: `getTemporaryStaticEffects()`, `getPersistentStaticEffects()` and
  `removePersistentStaticEffectsIf`, plus `addCombatAbility` and `addUntilNextUpkeepAbility` next to step 4's
  adders.
- `GameData`: `grantPerpetualAbility(cardId, Ability)` and `perpetualStaticEffects(cardId)`.
- `GameQueryService.getEffectiveGraveyardStaticEffects`.
- A grant record's derived `slot()` is `null` for a static grant, so the grant handlers now add
  `grantedAbility()` and log `LegacySlots.label(...)`. Where a handler adjusts the effect first (until end of
  combat, the chosen creature), it uses `LegacySlots.withEffect`.
- `scripts/step6-grant-ability-constructors.py` gave five grant records an `Ability` twin of each slot
  constructor. `BattlefieldPlacementService.applyPerpetualTriggeredAbilityGrants` handles a static grant on
  its own.

**Copy paths.** Every loop over `EffectSlot.values()` used to copy `STATIC` with the rest. Each now calls
`addStaticAbilitiesFrom` with the same filter: `CopySupport`, `TokenCopySupport`, `PermanentCopierService`
(clone copy and chosen modes), `MutationSupport`, `LayerSystemService.buildFullTextCopy` and
`EachOpponentChooses…TokenCopy…`. These scans read the static effects too: `GameViewProjectionFactory`,
`TriggeredAbilityQueueService`, `ChoiceHandlerService`, `CreatureControlService`,
`PerpetuallyBoostCardEffectHandler`, and the "has abilities" checks in `PredicateEvaluationService`. A copy keeps
each static ability's zones.

**A broken path fixed.** `PermanentCopierService`'s chosen-mode copy (a copy of Catch of the Day) used to add
the chosen modes' effects to the list returned by `getEffectRegistrations`. Since step 3 that list is read-only,
so the call threw. It now adds them through `addStaticEffect` and `addEffect`.

**Ability lists in token and copy records.** These records now hold `List<Ability>`:

- `CreateTokenEffect.tokenEffects`, which keeps its name;
- `additionalAbilities` in `BecomeCopyOfExiledCreaturePermanentlyEffect`, `ConjureCardIntoControllerLibraryEffect`,
  `ConjureCardNamedIntoLibraryEffect`, `CopyPermanentOnEnterEffect`, `CreateTokenCopyOfTargetPermanentEffect` and
  `TurnFaceUpCopyEffect`;
- `additionalAbilitiesOverride` in `BecomeCopyOfTargetCreatureUntilEndOfTurnEffect`.

The old components were `Map<EffectSlot, CardEffect>` and `Map<EffectSlot, List<CardEffect>>`, and some were
named `additionalSlotEffects` or `additionalEffects`.

- A card writes `List.of(StaticAbility.of(e), LegacySlots.triggered(EffectSlot.ON_DEATH, e2))`.
  `LegacySlots.triggered(slot, List)` builds one ability whose effects the card regroups as the slot's collectors
  do (`Card.addAbility`).
- A list keeps its order. `Map.of` has no defined iteration order, so a token with two slot-keyed abilities used
  to get them in an order that could change from run to run.
- The handlers add each ability with `Card.addAbility`. A token blueprint's static `SequenceEffect` is still split
  into separate static abilities, in `TokenCardFactory`.
- Lookups by key became filters on `LegacySlots.slotOf`. In `CreateTokenAndLinkToSource` the blueprint's own
  leaves-the-battlefield ability is replaced, as the map's `put` replaced it.

**Five triggers filed as static.** Guardian of New Benalia, Goblin Morale Sergeant, Benalish Knight-Counselor,
Aradesh, the Founder and Firebender Ascension are triggered abilities ("whenever this creature enlists a creature",
"whenever a creature you control attacking causes a triggered ability of that creature to trigger"). Their
collectors were registered on `STATIC`. They have a new legacy slot,
`ON_ALLY_ATTACKING_CREATURE_ABILITY_TRIGGERS`, with the same collectors and the same dispatch from
`checkAttackingCreatureTriggeredAbilityTriggers`. They belong to step 13's combat family.

**Cards and tests, by script.**

- `scripts/step6-static-ability.py`, run per letter module and over every other module, changed 8,328 card
  classes, 12 card test classes and the engine, AI, networking, domain and application sources. It reads the
  converted records' constructor and factory signatures from a git revision. That way it knows which positional
  `Map.of()` is the slot map, since `CreateTokenCopyOfTargetPermanentEffect` also takes a counter map.
- By hand:
  - a dozen card call sites the script reported: `java.util.Map.of()`, `Map.<EffectSlot, CardEffect>of(…)`, a
    `Map.of()` after a comment, and Outlaws' Merriment's own `token(…)` helper;
  - The Ur-Dragon;
  - the engine handlers that read the maps.
- `scripts/strip-unused-imports.py` then removed the imports left unused in the changed files.
- `scripts/step6-docs.py` rewrote the agent-docs snippets. It converts slot-keyed maps whatever their slots.
  Each doc that uses `STATIC` as a label has a note on what it means now.

**For later steps.**

- Step 8: `STATIC` still held replacement effects (`EntersTappedEffect` and the others step 1 listed) and echo
  (`RegisterEchoAtNextUpkeepEffect`, a triggered ability). They are static abilities of the battlefield now, as
  before, and move with step 8's replacement effects and the trigger families.
- Some battlefield readers scan cards in other zones through `getStaticEffects()`. Planes are one example: their
  static abilities function in the command zone, but the plane cards don't say so. A step that makes a reader
  filter by zone has to give those abilities their zones first.
- The `Grant…` records keep their slot constructors and derived `slot()`. `slot()` is `null` for a static grant,
  so new handler code should read `grantedAbility()`.
- Copies still lose the simultaneous flag and use slot order for triggered abilities (step 3). Static abilities
  are copied in the source's order.

**Tests changed.**

- `CardAbilitiesTest`:
  - `eachStaticEffectIsAStaticAbilityOfItsZone` reads the zone views.
  - `aStaticEffectKeepsNoTriggerModeAndStaysItsOwnAbility` became
    `anAddedStaticAbilityKeepsItsZonesAndStaysOutOfTheSlotViews`, because `addSimultaneousEffects(STATIC, …)`
    no longer exists.
  - `effectsCanBeRemovedFromSomeSlotsOnly` uses `removeAbilityEffectsIf`.
  - New: `aStaticEffectFunctionsFromTheZonesItsRuleNames` and `staticAbilitiesAreCopiedWithTheirZones`.
- `GrantedAbilitiesTest`:
  - `aSlotAndAnEffectStandForTheAbilityOfTheirKind` became `aSlotAndAnEffectStandForATriggeredAbility`, plus a
    new `aStaticAbilityIsInNoSlot`.
  - The slot-view and `addAbility` tests use `StaticAbility.of`.
  - New: `aPermanentsStaticGrantsAreReadApartFromItsTriggers` and `perpetualStaticGrantsAreReadApartFromTriggers`.
- `LegacySlotTriggerTest`: every slot is now a legacy trigger, and the test that rejected the static slots is
  gone.
- `PlanechaseServiceTest`: the fixture helper builds static planes with `addStaticEffect`.
- Every other application, AI and card test was converted by the script with no change of meaning.

**Test runs.**

- Application, against HEAD: the same test classes were run in a HEAD worktree. They cover every changed model
  type and the services whose tests the script touched. The failures equal HEAD's, 223 cases, mostly mock wiring
  in service tests. The new tests pass.
- AI: 14 classes, 777 tests. Four fail, and the same four fail at HEAD: two kicker casts in
  `RandomAiDecisionEngineTest` and two unclassified-effect checks in `TargetPolarityGuardTest`.
- Card: 1,409 classes and 11,580 tests, all passing. They are the classes of:
  - every card whose source uses a token or copy ability list, a grant record, a copy, conjure, mutate or
    turn-face-up effect, or an effect whose handler changed;
  - the five enlist cards, The Ur-Dragon, Rule with an Even Hand, Marchesa's Surprise Party and Catch of the Day;
  - 120 random kicker, buyback, splice, gift, cost-reduction and can't-be-countered cards;
  - 300 random other converted cards;
  - the converted card tests.
- The whole build, every card module and every test source compiles. The full card suite was not run here.

### Step 7

`EffectSlot.ON_TAP` and `EffectSlot.ON_SACRIFICE` are gone, and `Card` has one activated ability list. Five
decisions went beyond the step text:

- the cards' tap-for-mana abilities go through the ordinary mana-ability path, with no marker that keeps the old
  tap path alive;
- a land's basic land type gives it its mana ability (CR 305.6, checked), so the basics print none;
- a converted mana ability is listed after the card's other abilities, which keeps every ability index;
- the dedicated "sacrifice this" action is deleted;
- exile-only abilities got a zone too.

**One list, with zones.**

- `ActivatedAbility` has `AbilityZones zones` (CR 113.6, 113.6b, checked), the battlefield by default, with
  `withZones(AbilityZones)` (fluent, like the other construction-time setters, and copied by `copyWith`) and
  `functionsIn(Zone)`. `exileOnly` and `withExileOnly()` became `AbilityZones.EXILE`.
- `Card.activatedAbilities` is one list in the order abilities were added, hidden from Lombok:
  - `getActivatedAbilities()`: every ability, read-only;
  - `getActivatedAbilities(Zone)`: those that function in that zone, from a per-zone index built on first use,
    held in a `volatile` field, dropped by every mutator and shared by copies, like step 3's `SlotIndex`;
  - `addActivatedAbility(ability)`, `addActivatedAbility(AbilityZones, ability)`, `removeActivatedAbilitiesIf`,
    `clearActivatedAbilities`.
- `addGraveyardActivatedAbility`, `addHandActivatedAbility`, `addStackActivatedAbility` and the three getters are
  gone. The keyword helpers build their abilities with their zone: `unearthAbility`, `embalmAbility` and
  `scavengeAbility` the graveyard, `ninjutsuAbility` and `addCycling` the hand, so a granted one carries it too.
- `CardFreezeTest`: 80 → 78 fields (four lists became a list and its index).
- Three engine handlers edited the old lists through their getters (`ReturnSourceAsAuraEffectHandler`,
  `LicidBecomeAuraEffectHandler`, the emblem activation in `AbilityActivationService`); they use the mutators.

**Readers.** `scripts/step7-activated-abilities.py --readers` rewrote every reader in the engine, AI, networking,
application tests and card tests: `getActivatedAbilities()` → `getActivatedAbilities(Zone.BATTLEFIELD)` (what the
old list held) and the zone getters → `getActivatedAbilities(Zone.X)`. By hand:

- the exile readers (`GameViewProjectionFactory`, `CardViewFactory`, `PermanentViewFactory`, the two exile
  activations) read `Zone.EXILE`. `AbilityActivationService.getEffectiveActivatedAbilities` reads the battlefield
  abilities, plus the exile ones while the card is in exile (`printedActivatedAbilities`), the same list and
  indices as before;
- the "does it have abilities" scans in `PredicateEvaluationService` that unioned the four lists read the whole
  list, and `otherZoneActivatedAbilityCount` counts every ability that doesn't function on the battlefield. Both
  now see exile-only abilities, which the old union missed;
- `EachOpponentChooses…TokenCopy…` copies the whole list. The other copy paths (`CopySupport`,
  `TokenCopySupport`, `PermanentCopierService`, `MutationSupport`) copied only the battlefield list and still do;
  CR 707.2 (checked) would have them copy every ability, which no current card notices.

**Mana abilities (CR 605.1a, checked).** Of the 121 `ON_TAP` cards:

- 107 became `addActivatedAbility(ManaAbilities.tapFor(color))` or the new `tapFor(color, amount)`, the form
  Tundra and Birds of Paradise already used. `scripts/step7-mana-ability-last.py` then moved the statement after
  the card's other battlefield abilities in the 62 cards where it came first. The old engine kept `ON_TAP` out of
  the ability list (and the grant handlers appended it last), so Mutavault's animation is still ability 0 and no
  index that tests, the AI or the client use moved. Ability order is not a rules concept. A first attempt that
  kept the printed order broke about 540 tests across the suite for nothing but the index.
- The twelve whose only tap mana is their basic land type's (the ten basics and snow basics, Dryad Arbor,
  Gobland; checked against their Scryfall type lines) print no mana ability: the type gives them "{T}: Add
  [mana]" (CR 305.6, checked). `AbilityActivationService.getEffectiveActivatedAbilities` already added that
  ability for land types a permanent didn't print; it now adds it for every basic land type, after the printed and
  granted abilities, unless an equal ability is there (`GameQueryService.missingIntrinsicManaAbilities`, one shared
  instance per color from `ManaAbilities.intrinsic(color)`). So a Forest's granted abilities keep index 0, and the
  client sees its mana ability: `GameViewProjectionFactory` appends the same list to the permanent view.
  `ManaAbilities.intrinsic(card)` and `withIntrinsic(card)` give it for a card off the battlefield (the AI's land
  evaluation, "has a mana ability" predicates, craft materials, and the handlers that copy the abilities of cards
  in a graveyard, in exile or on top of the library).
- Gyre Engineer's two registrations are one ability, "{T}: Add {G}{U}.", and Mutable Explorer's token lists its
  mana ability after the animation; both by hand.

Every engine, AI and networking reader of `ON_TAP` was deleted or folded into the activated-ability path:

- `AbilityActivationService.tapPermanent` keeps one special path, for a land whose mana comes from its land types:
  set by an effect (CR 305.7, checked) or its basic land types (CR 305.6, checked). Anything else activates its
  first plain "{T}: Add …" ability (no other cost, condition or rider), else its first {T} mana ability, so Great
  Hall of the Biblioplex and Rootrider Faun still tap for their plain mana rather than a costed or filtered one. A Forest therefore taps for {G} through its type, as before. The single-type branch now
  produces through the same code the `ON_TAP` loop used (snow, cave, desert, artifact and effective-color tags),
  so a nonbasic land with one basic land type and no `ON_TAP` (Murmuring Bosk) gets those tags too.
- `tapForeignLandForMana` (Piracy) treats basic land types like set ones. A Tundra under Piracy now offers {W} or
  {U}; before, it activated its first mana ability.
- `ActivatedAbilityExecutionService.doResolveManaAbility` tags mana from a basic land as basic land mana and passes
  the flag to the land-mana replacements, as the `ON_TAP` path did. Wastes and Snow-Covered Wastes, which have no
  basic land type, use this path now.
- `PotentialManaService`: the `ON_TAP` branches, `hasLivePrintedTapMana` and `hasOnTapManaEffects` are gone; the
  intrinsic and activated branches produce the same pools. `hasFreeTapManaAbility(Card)` is new, for the AI.
- `LandTapTriggerCollectorService`: the fallbacks for a tap that reports no colors read the land card's basic land
  types (`intrinsicManaColors`), which is what `ON_TAP` held for the cards that ever reached them. One handler used
  to union the produced colors with the land's `ON_TAP` colors; it now falls back only when none were reported.
  Reading every printed {T} mana ability there was tried and gave Gaea's Cradle, tapped for nothing,
  Overabundance's bonus mana, and gave Ultima, Origin of Oblivion's {C} bonus to a land tapped for colored mana.
- The seventeen "gains the activated abilities of …" handlers no longer synthesize a "{T}: Add mana." ability from
  `ON_TAP`. The card-based ones read `ManaAbilities.withIntrinsic(card)`, the permanent-based ones add
  `missingIntrinsicManaAbilities`. A gained mana ability keeps its card's description.
- `DrainTargetPlayersLandManaEffectHandler` lists each land's mana abilities; the "Tap for mana" option is gone.
- AI: `AiManaManager` taps every source through an ability index, and the mana-source heuristics in `GamePhase`,
  `BoardEvaluator`, `HardAiDecisionEngine` and `SpellEvaluator` count any card with a free {T} mana ability, so
  Birds of Paradise and Tundra count now too. `GameSimulator` lost its `tapPermanent` shortcut.
- `CardView.hasTapAbility` is gone, and the client taps every permanent through its listed mana abilities. A Forest
  with one ability is activated directly; Mutavault shows its two abilities, as it showed "tap for mana" plus its
  ability before.

Activated-ability indices moved in two places, both rules-correct:

- A basic land now has its mana ability in the list, after any granted ones. "Activate ability 0" on a Forest
  with no grants taps it for {G} instead of failing, so tests that proved a grant was gone by expecting ability 0
  to fail now expect ability 1 to fail.
- A permanent that keeps a converted card's printed abilities and gains another (Llanowar Elves given Supper for
  Spiders' Food ability or turned into a Treasure by Vraska, the Silencer; Tazri's grant; a Clone of the Dane that
  copies Llanowar Elves) has its mana ability at 0 and the new one at 1.

**"Sacrifice this".** Aura of Silence was the only `ON_SACRIFICE` card. It is an activated ability with
`SacrificeSelfCost`, like Seal of Cleansing, so the normal activation path does its checks: Pithing Needle, Arrest,
the activation locks and the death-replacement question before payment. `AbilityActivationService.sacrificePermanent`,
`GameService.sacrificePermanent`, `GameData.SacrificeAbilityReplay`, the `SACRIFICE_PERMANENT` message and request
(server and client) and `GameTestHarness.sacrificePermanent` are deleted. Nothing in the client sent that message.

**Cards and tests, by script.**

- `scripts/step7-activated-abilities.py`, run per letter module, changed 623 card classes: 119 `ON_TAP`
  registrations (12 of them left as a comment that the land type gives the ability), 215 graveyard, 295 hand and
  one stack ability, and 3 exile-only abilities. With `--readers` it also rewrote the engine, AI, networking and
  application sources, and 47 card test classes. Ten of those used `harness.sacrificePermanent` and now call
  `harness.activateAbility(player, index, null, target)`.
- `scripts/step7-mana-ability-last.py` moved the converted mana ability after the other abilities in 62 cards.
- `scripts/step7-shift-ability-indices.py` shifts ability indices in a test; it was used for the first, abandoned
  ordering and then on single lines for the basic-land cases below.
- By hand: Gyre Engineer, Mutable Explorer, Aura of Silence; `EachOpponentChooses…TokenCopy…` (method references
  the script didn't match); the `ON_TAP` readers above.
- `scripts/strip-unused-imports.py` then cleaned the changed files. In a few touched files it also removed imports
  that were already unused.
- `scripts/step7-docs.py` rewrote the agent-docs snippets; the prose that named `ON_TAP` or `ON_SACRIFICE` was
  edited by hand. `ACTIVATED_ABILITY_GUIDE.md` has a new section 1a on mana abilities and "sacrifice this".

**For later steps.**

- Activated abilities are not in `Card.getAbilities()`: the step text asked for one activated list, and a separate
  list kept `LegacySlots`, `addAbility` and the layer hash untouched. Moving them into the ability list is a small
  follow-up whenever something needs all four kinds in one place.
- `ActivatedAbility` is still a mutable class with fluent setters. `withZones` follows that style; a card must
  set an ability's zones before adding it, or the card's zone index goes stale.
- A nonbasic land whose type an effect set still shows its printed abilities in the client (the permanent view
  doesn't apply CR 305.7, checked), while the engine's list has the new type's mana ability instead. This was so
  before too. Basics are fine: they print no ability and the view lists the mana of their current types.
- Copy paths copy only battlefield abilities (above).

**Tests changed.**

- `CardFreezeTest`: 80 → 78.
- `AbilityActivationServiceTest`: the `sacrificePermanent` nest is deleted (the action is gone; Aura of Silence's
  card tests cover the ability). The land fixtures have a mana ability, and the tests that tap them stub the
  land's basic land type, since that is the path `tapPermanent` takes for an Island. The replaced-land test hands
  the type's mana to the replacement service. "Non-summoning-sick creature with tap-for-mana can be tapped" is
  deleted: Llanowar Elves now goes through `activateAbilityInternal`, which this mock-based class can't run;
  `LlanowarElvesTest` covers it through the engine.
- `GameActionAvailabilityServiceTest`: the mana-land fixture is a land with a basic land type; Rishadan Port's
  ability is index 1.
- `LandTapTriggerCollectorServiceTest`: a colored fixture land has the matching basic land type instead of a
  printed ability, a colorless one prints `ManaAbilities.tapFor(COLORLESS)`, and the Ultima test passes the produced
  {C} the engine always reports.
- `PermanentViewFactoryTest`: the fixture uses `ManaAbilities.tapFor`; the `hasTapAbility` assertion is removed.
  `LegacySlotTriggerTest` lost the `ON_TAP` row.
- `AiManaManagerTest`: fixtures use mana abilities, and the 38 expectations of a `null` ability index (the old tap
  path) expect index 0. Six display names that described `ON_TAP` were renamed.
- `EasyAiDecisionEngineTest` (3), `MediumAiDecisionEngineTest` (2), `HardAiDecisionEngineTest` (2): the AI taps a
  Plains through `handleActivateAbility`, not `handleTapPermanent`. These mock-wired classes load no oracle data,
  so a `new Plains()` had no type; it only worked through the printed `ON_TAP`. A `plains()` helper sets its type.
- `AiTestPlayabilityStub` lists a permanent's abilities with `ManaAbilities.withIntrinsic`, as the engine does.
- New tests: `CardAbilitiesTest` (the zone views, an ability in several zones, read-only views that follow
  changes, copies) and `ManaAbilitiesTest` (`tapFor(color, amount)`, the shared intrinsic abilities, `intrinsic(card)`
  and `withIntrinsic(card)`).
- Card tests, all by hand, each for one of the index changes above or a message:
  - a basic land with no grant has its mana ability at 0, so "the grant is gone" checks expect ability 1 to fail
    (or "Invalid ability index"): Chromatic Lantern, Glamorous Outlaw, Joiner Adept, Koth of the Hammer, Masked
    Bandits, Mystic Skull, Nexos, Nylea's Presence, Overlaid Terrain, Prismatic Omen, Rain of Filth, Rakish
    Revelers, Realmwright, Resonating Lute, Sheltered Aerie, Shattered Seraph, Spara's Adjudicators, Thespian's
    Stage, The World Tree, Ultima, Unbridled Growth; Aquitect's Will and Topsoil Turner assert the Forest's or
    Mountain's own ability instead of an empty list; Yavimaya, Cradle of Growth's Swamp lists {B} before {G};
  - a converted permanent that loses its non-mana abilities (Blood Sun, Town-Razer Tyrant on Hostile Desert)
    still has its mana ability at 0;
  - a granted ability comes after a converted card's mana ability: Burning Anger, Hostile Realm, Orochi Merge-Keeper,
    Riftstone Portal, Sachi, Supper for Spiders, Tazri, The Ever-Changing 'Dane, Tyvar Kell, Vraska, the Silencer;
  - Detention Vortex: Llanowar Elves now has an ability, and the Aura's "can't be activated" is the error;
  - Aura of Silence: activation goes through `activateAbility`, and the ability-less permanent's error changed.
- Frontend: `targeting-choice.service.spec.ts` fixtures give lands a mana ability instead of `hasTapAbility`, and a
  land tapped during a payment sends `ACTIVATE_ABILITY` with the payment intent; mock data lost the field.

**Test runs.**

- Application, against HEAD (the same classes in a HEAD worktree): 26 classes covering the changed services. The
  failures equal HEAD's (mostly mock wiring in service tests), minus HEAD's three `sacrificePermanent` failures,
  whose tests are deleted. The new tests pass.
- AI: the whole module (1,619 tests). Its 12 failures all fail at HEAD too: two kicker casts in
  `RandomAiDecisionEngineTest`, two in `TargetPolarityGuardTest`, and Wear // Tear and Reign of Chaos in the Easy,
  Medium and Hard engine tests.
- Frontend: `tsc` on the app and spec projects, and `ng test`: 21 files, 195 tests, all passing.
- Card: 1,856 classes, 17,157 tests, all passing: every converted card and every card whose source changed, the
  cards on the changed mana, grant, copy and land-type paths, every test that taps a permanent, and every test that
  creates a converted non-basic card and activates an ability.
- Everything compiles except `CardBrowserServiceTest` in `magical-vibes-webservice`, which fails to compile at
  HEAD too (`CardCatalog.findLoadedPrintingByName`). The full card suite was not run here.

### Step 8

Twelve constants are gone: `GRAVEYARD_DRAW_REPLACEMENT`, `MAY_SKIP_DRAW_STEP_DRAW`, `MULLIGAN_ACTION`,
`ON_OPENING_HAND_REVEAL` and the eight `GRANT_*`. `ON_ANY_CARD_EXILED` stays as The War Doctor's trigger (step 12);
only Bell Borca's half moved. Every "enters with" and "as this enters" effect left `ON_ENTER_BATTLEFIELD`. Every
remaining constant is a trigger condition, and the regenerated inventory says so ("By kind: triggered 499").

**One kind for all of them: a static ability.** No `Ability` subtype was added; the sealed hierarchy keeps
CR 113.3's four kinds.

- A replacement effect printed on a card is generated by a static ability (CR 113.3d, 614.1, checked), and an
  "enters with" or "as this enters" effect is a replacement effect (CR 614.1c, checked).
- Serum Powder, No-Regrets Egret, the Leylines and the Chancellors are static abilities whose text names the zone
  they function in, the hand (CR 113.6b, checked). They let a player take an action during the mulligan or from
  the opening hand (CR 103.5, 103.6, checked); they aren't special actions of CR 116.
- "The first spell you cast each turn has cascade" is a static ability that gives spells a triggered keyword
  (CR 702.85a, 702.40a, 702.144a, checked).

**Zones** come from `CardEffect.staticAbilityZones()`, as in step 6:

- `DredgeEffect`: the graveyard (CR 702.52a, checked). `DredgeSupport` reads
  `GameQueryService.getEffectiveGraveyardStaticEffects`.
- `MulliganActionEffect`, a new interface for `SerumPowderEffect` and `NoRegretsEgretEffect`: the hand. It owns
  `mulliganActionDescription()`, which left `CardEffect`. `MulliganService` reads `getStaticEffects(Zone.HAND)`.
- `PregameChoiceEffect`: the hand, and `MayEffect` passes its wrapped effect's zones on, so
  `addStaticEffect(new MayEffect(new LeylineStartOnBattlefieldEffect(), …))` is a static ability of the hand.
  `MulliganService.startGame` reads `getStaticEffects(Zone.HAND)`, also for the Legacy cards it scans in the
  library. `SecretMissionEffect` is a `PregameChoiceEffect` too, but Marchesa's Surprise Party names the command
  zone explicitly. No other static `MayEffect` existed (checked by grep).
- An entry replacement keeps the default battlefield. CR 113.6h (checked) says it functions as the object enters,
  and CR 614.12 (checked) has it checked on the permanent as it would exist on the battlefield, which is the card
  the readers already look at.

**The small slots.**

- `MAY_SKIP_DRAW_STEP_DRAW`: Island Sanctuary and Fasting are static `MaySkipDrawReplacementEffect`s with the new
  `drawStepOnly()`. `DrawService`'s any-draw loop skips them; its draw-step loop and
  `StepTriggerService.findMaySkipDrawStepDrawSources` read them from the static abilities.
- `GRANT_*`: one record, `GrantCastTriggerToSpellsEffect(SpellCastTriggerGrant grant, CardEffect ability)`, where
  the enum names the eight cases. The eight code paths in `TriggerCollectionService.checkSpellCastTriggers` are
  unchanged and read their grants through `castTriggerGrants(perm, grant)`. Step 13.2 should turn the enum into
  spell predicates and conditions.
- Bell Borca: a static `NoteManaValueOfExiledCardsEffect`, read by `GameData.notifyCardsExiled` from
  `getStaticEffects()`. Its guard in `TriggerCollectionService.dispatchSlotEffect` is gone.
- Deleted with the slots: `StepTriggerService.handleOpeningHandTriggers` (dead for every card since step 1), the
  `NON_ABILITY_SLOTS` sets of the two "gains the abilities of exiled cards" handlers, and
  `CreateTokenWithTriggeredAbilitiesOfExiledCardsEffectHandler.isTriggeredAbilitySlot`.

**Entry replacements.**

- Which effects moved: everything `EtbTriggerService` filtered out of the enters trigger
  (`isEntryReplacementEffect`, the `ReplacementEffect` implementers and the twelve-class `instanceof` list),
  read off the built cards by `scripts/step8-etb-scan.java` at HEAD. It found 6,338 `ON_ENTER_BATTLEFIELD`
  registrations, 1,019 of them entry replacements on 995 cards: `EnterWithCountersEffect` 494 (and 141 inside a
  `ConditionalEffect`), `ChooseColorOnEnterEffect` 65, `ChooseSubtypeOnEnterEffect` 63, `CopyPermanentOnEnterEffect`
  53 and 60 other classes.
- Oracle text was checked for every card whose `ON_ENTER_BATTLEFIELD` effects had no "when … enters" wording
  (56 cards). Four more classes are "as this enters" choices (CR 614.1c) that the engine handled in place but did
  not filter out of the trigger, and they moved too: `ChooseNumberOnEnterEffect` (Ominous Lockbox, Sanctum
  Prelate, Shapeshifter, Squall, Gunblade Duelist, Talion), `ChooseAnotherCreatureOnEnterEffect` (Dauntless
  Bodyguard, Tyrannical Pitlord), `YouAndOpponentChooseCardNamesOnEnterEffect` (Null Chamber) and
  `ChooseOpponentPermanentOnEnterEffect` (Predatory Sludge). So did The Mystical Archive's
  `DraftCardFromSpellbookToExileEffect`, which the filter treated as a replacement on lands only.
- `ProtectionFromChosenColorEffect` is no longer a `ChooseColorEffect`. Voice of All's self-scoped protection
  used to double as its colour choice; as a static ability it would have made every static reader of
  `ChooseColorEffect` ask Sanctuary Blade and Ward of Lights, whose protection was already static, for a colour as
  they enter. Voice of All and Order of the Stars now have two abilities, as printed: a `ChooseColorOnEnterEffect`
  and the protection. The layer system and `GrantedAbilityViewFactory` read the self-scoped protection
  (`scope() == null`) from the static abilities.
- After the change the scan finds 5,309 `ON_ENTER_BATTLEFIELD` registrations and no entry replacement among them.
  `EtbTriggerService`'s filter and `isEntryReplacementEffect` are deleted: nothing else puts an entry replacement
  in the slot. `CloneService`'s vanishing for a copy now adds its `EnterWithCountersEffect` as a static ability;
  the other engine paths that add to the slot add triggers.

**Readers.**

- `scripts/step8-entry-replacements.py --readers` rewrote the 89 statements that read
  `getEffects(ON_ENTER_BATTLEFIELD)` and name only entry-replacement types to `getStaticEffects()`.
- By hand, because they named one of the classes some cards already had as static abilities, or read both the slot
  and the static abilities: `BattlefieldPlacementService` (the "enters with" counters, the entry-replacement
  registry and the deferred chosen-subtype counters now read the static abilities only), `StackResolutionService`,
  `AsEntersInteractionService`, `BattlefieldEntryService`, `CloneService`, `OpponentChoiceOnEnterSupport`,
  `PermanentChoiceBattlefieldHandlerService`, `ChoiceHandlerService`, `StepTriggerService`, `TurnCleanupService`,
  `PermanentControlSupport` (a token's devour), `GrantedAbilityViewFactory`, `LayerSystemService` and
  `EffectResolution.hasColorsSpentCounterEffect`.
- `EffectResolution.hasColorSpentCondition`, `hasColorManaPairsSpentToCastAmount` and `hasManaSpentToCastAmount`,
  which tell the cast path to snapshot the mana spent, also scan the static abilities now; an "enters with X
  counters" reads that snapshot.
- Readers that ask about the enters trigger (targets, modal ETBs, the AI's ETB value, `RememberTargetPlayerEffect`,
  echo, the "exile until this leaves" family) still read the slot. No entry-replacement class implements an AI
  capability interface, so the AI's ETB evaluation sees the same effects.

**Cards and tests, by script.**

- `scripts/step8-entry-replacements.py`, run on every card module, changed 1,060 card classes: the small slots,
  every entry replacement whose card had as many convertible registrations as the scan counted, and two token
  ability lists (Dragon Broodmother's devour, The Girl in the Fireplace's time counters, now
  `StaticAbility.of`). Imports it added were sorted in by a second pass on three files, because the first run
  appended them.
- It reported two cards. Agent's Toolkit adds its four "enters with" counters in a loop and was converted by hand.
  Inspired Skypainter's `BecomePreparedEffect.triggerOnly()` is a real trigger ("When this creature enters, it
  becomes prepared") and stays.
- By hand: Voice of All and Order of the Stars (the colour choice), the two long `GrantCastTriggerToSpellsEffect`
  lines' wrapping, and the stale slot comments in Island Sanctuary and Maelstrom Nexus.
- `--by-class` converted the fixture cards of eight application test classes, and `--readers` the
  `GameTestHarness` devour check. `scripts/strip-unused-imports.py` cleaned the changed files; in eleven of them it
  also removed imports that were already unused.
- `scripts/step8-docs.py` rewrote the agent-docs snippets and relabelled 118 `ORACLE_TEXT_EFFECT_MAP.md` rows whose
  Oracle text has an "enters with" or "as enters" ability but no enters trigger. The prose that named the removed
  slots, the reference entries of the moved effects, the domain Javadocs, the `STATIC` notes and both card skills
  were edited by hand.
- `effect-slot-inventory.md` was regenerated from the updated classification TSV with the new `--keep-baseline`
  option, which keeps the step 1 Baseline section. The TSV lost the rows of every constant steps 5 to 8 deleted and
  gained `ON_ALLY_ATTACKING_CREATURE_ABILITY_TRIGGERS` (step 6). Finished steps are listed as "done" in the
  summary. The header names the commit the working tree was on.

**Rules fixes.**

- The four "as this enters" choices above were also left in the enters trigger. Where the entry flow put them
  there, an ability with nothing to do went on the stack after the choice. A new test for Dauntless Bodyguard
  fails at HEAD and passes now. Talion's passes at HEAD too, so its entry path never put one there. Null Chamber's
  and Sanctum Prelate's new tests couldn't be run at HEAD (the HEAD worktree downloaded different card data, and
  their fixtures failed to build). All four new tests pass.
- Black Vise registered its `ChooseOpponentOnEnterEffect` as a static ability, which the as-enters readers never
  read (they read the slot). It now gets the as-enters opponent choice like the cards that used the slot.
- `CreateTokenWithTriggeredAbilitiesOfExiledCardsEffectHandler` copied every slot of the exiled cards, so the
  token also got their "enters with" effects, which aren't triggered abilities. It no longer does.
- Carnifex Demon, Crack in Time, Deity of Scars, Dockworker Drone and Idris already had a static
  `EnterWithCountersEffect`. The readers that only read the slot (the entry log message, chosen-subtype deferred
  counters, the colours-spent snapshot) now see it.

**For later steps.**

- Left in `ON_ENTER_BATTLEFIELD`, none of them a trigger: dash's `ReturnSelfToHandIfDashCostPaidEffect` (19 cards,
  inert, dropped by `EtbEffectResolver`) and blitz's `ConditionalEffect(CastForAlternateCost, …)` haste and
  end-step sacrifice (the blitz cards; dropped there and read by `StackResolutionService`). Both are static abilities of
  their keyword (CR 702.109a, 702.152a, checked). A static `ConditionalEffect(CastForAlternateCost, haste)` would
  start granting haste through the layer system, so the keyword family that moves them needs its own reader.
  Planeswalkerificate's `InitializeEnchantedPermanentToughnessAsLoyaltyEffect` is engine setup, not a printed
  ability.
- Mirrored Lotus's reflect (`ReflectEffect`, "As this enters, each opponent may pay {0}") is an as-enters
  replacement that is still a trigger. Moving it needs an entry handler.
- Other slots still hold replacement effects: `ON_TURNED_FACE_UP` (step 1's four), `ON_SELF_DISCARDED_BY_OPPONENT`
  (`EnterBattlefieldOnDiscardEffect`) and `ON_EQUIPMENT_ATTACHED` ("as this becomes attached" choices). Their
  families move them.
- A static `ConditionalEffect` around an entry replacement ("if it was kicked, it enters with …") reaches the layer
  system's conditional static handlers. Its wrapped effect has no static handler, so nothing applies, but the
  condition is evaluated on each static-bonus query.
- `getEffects(ON_ENTER_BATTLEFIELD)` lost the moved effects, so positions in that view changed for 995 cards.
  Target groups are bound by effect, not position, and no main-code reader indexes the view (checked by grep).

**Tests changed.**

- `StepTriggerServiceTest`: the four `handleOpeningHandTriggers` tests are deleted with the method.
- `LegacySlotTriggerTest`: the `GRAVEYARD_DRAW_REPLACEMENT` row is gone; the other graveyard row covers the prefix.
- `StackResolutionServiceTest`, `AsEntersInteractionServiceTest`, `BattlefieldEntryServiceTest`,
  `BattlefieldPlacementServiceTest`, `EtbTriggerServiceTest`, `GrantedAbilityViewFactoryTest` and
  `FlickerEffectHandlerTest`: fixture cards add their entry replacements with `addStaticEffect` (by script).
  `GameTestHarness` reads devour from the static abilities.
- New: `CardAbilitiesTest#dredgeMulliganAndOpeningHandActionsFunctionFromTheZonesTheirRulesName` and
  `#anEntersWithAbilityIsAStaticAbilityOfTheBattlefield`; card tests that the as-enters choice puts nothing on the
  stack for Talion, Sanctum Prelate, Dauntless Bodyguard and Null Chamber.

**Test runs.**

- Application, against HEAD (the same 22 classes in a HEAD worktree): 1,363 tests; the failures equal HEAD's,
  76 cases, mostly mock wiring in service tests. The four deleted `StepTriggerServiceTest` cases passed at HEAD.
  `CardAbilitiesTest`, with its new tests, passes.
- AI: the whole module (1,619 tests). The same 10 failures as listed for step 7, in the same families: two kicker
  casts in `RandomAiDecisionEngineTest`, Easy's Strength of the Tajuru (a multikicker sorcery), two in
  `TargetPolarityGuardTest`, and Wear // Tear and Reign of Chaos in the Easy, Medium and Hard engine tests.
- Card: 1,071 classes, 9,243 tests, all passing: the test class of every changed card, of the cards that already
  had these effects as static abilities (Black Vise, the five static `EnterWithCountersEffect` cards, the three
  static "enters prepared" cards, Sanctuary Blade, Ward of Lights, Cho-Manno's Blessing), Inspired Skypainter,
  The War Doctor, Distracted Botanist, Mirrored Lotus, Planeswalkerificate, Clone, Phantasmal Image and Serum
  Powder. The four new as-enters tests were run again on their own and pass.
- The whole build, every card module and every test source compiles. The full card suite was not run here.

### Step 9

**Types** (`magical-vibes-domain`, `model.rules`). Nothing reads them yet except the tests and the shadow recorder.

- `RulesEvent`: a sealed interface, for now permitting only `ZoneChangeEvent`. It has `deepCopy()` for simulation
  copies. It lives apart from `model.event`, whose domain events no rules code may read.
- `ZoneChangeEvent(Card card, UUID ownerId, Zone from, Zone to, ZoneChangeCause cause, Permanent permanent,
  UUID controllerId)`:
  - `card` is the physical card; for a merged or melded permanent it is the original card.
  - `from` is `null` when the object came from outside the game or nothing knew where it was: a created token, a
    conjured card, a card a test put straight into a zone.
  - `permanent` is the last-known copy for a departure and a copy taken as it entered for an entry. Moves between
    other zones have none.
  - `controllerId` is set for departures and entries only.
  - It has `leftBattlefield()`, `enteredBattlefield()` and `withTo(zone)`.
- `ZoneChangeCause`: `OTHER`, `DESTROY`, `SACRIFICE`, `STATE_BASED`, `DISCARD`, `MILL` and `CAST`.
  `ZoneChangeCause.of(DepartureCause)` maps `STATE_BASED_DESTROY` to `DESTROY`: lethal damage destroys (CR 704.5g).
- `RulesEventBatch(long id, List<RulesEvent> events)`: one occurrence, never empty (CR 603.2c).
- `RulesEventLog`: the mutable log on `GameData.rulesEvents`. It holds:
  - the closed batches and the open one, with nested `beginBatch`/`endBatch` scopes, the outermost of which closes
    the batch;
  - the zone each card was last seen arriving in (`track`, `zoneOf`);
  - the departures whose card hasn't arrived anywhere yet;
  - per-object causes (`setCause`/`restoreCause`);
  - a suppression depth (`suppress`/`resume`).

  An event emitted outside a scope is a batch of its own. `drain()` takes the closed batches and leaves an open
  batch open. `pending()` reads everything without taking it. `copyFrom` deep-copies all of it.
- `RulesObserver`: a test hook on `GameData.rulesObserver`. It has `triggerQueued(Route, item)` and
  `eventsDrained(batches)`. The routes are `STACK`, `MANA_ABILITY_QUEUE`, `SUSPENDED_QUEUE`, `MAY_ABILITY` and
  `INTERACTION`.

**Where zone changes are emitted.** There are three kinds of choke point, which together cover every place where a
card changes zones. Step 1 found no single choke point, with about 140 `processCreatureETBEffects` callers and 179
`addCardToGraveyard` callers, so the hooks sit below them.

- **Departures**: `PermanentRemovalService.processRemovalCleanup`, through the new `emitDeparture`. Both
  `removeFromBattlefield` (now given its destination by its nine callers) and `processAlreadyRemovedToGraveyard`
  reach it, and every battlefield leave path goes through one of the two. The snapshot is the one `DepartureService`
  published in `simultaneousDyingPermanents` before a batch left (CR 603.10a). Without a batch, it is the existing
  departure snapshot with the effective card it left as. Phasing never reaches it (CR 702.26d).
- **Entries**: `BattlefieldPlacementService.completePlacement`, right after the permanent is added to the
  battlefield. `from` is the card's tracked zone, or `Permanent.getEnteredFromZone()` when the zone is unknown.
  Upgrade places a permanent and then undoes its entry: `PermanentRemovalService.exileInsteadOfEntering` exiles it
  instead, and `UpgradeSupport.applyUpgrade` merges it into the artifact it covers. Both call the new
  `RulesEventLog.retractEntry`, so an entry that didn't happen leaves no event.
- **Every other arrival**: GameData's own zone lists report each card added to them. That covers the hand, library
  and graveyard lists (which already existed for commander moves), the new `CommandZoneMap`/`CommandZoneList`, the
  new `ExileZoneList` behind `exiledCards`, and spells reaching the stack in `TriggerAwareStackList` (CR 601.2a; not
  copies, cause `CAST`). This needs no change at the 500-odd call sites that add cards to these lists. A card added
  straight to a list is still a zone change, and so are test setups (`harness.setGraveyard` and so on), which
  produce events with `from == null`. Putting a whole list into a zone map (`setHand`, `setLibrary`, game setup)
  only tracks the cards' zone and emits nothing.

**How arrivals and departures fit together.**

- An arrival names the zone the log last saw the card in, so a card that was taken out of its zone before it was
  added elsewhere (the usual order) still has the right `from`. An arrival in the zone the card is already in emits
  nothing: a library reordered or a card put back on top. `LibraryCardList.set` (shuffles, Bombardment's swap)
  never emits.
- A departure is emitted as the permanent leaves, with the destination its caller intended. When its card then
  arrives in another zone because a replacement redirected it (CR 614.6), for example `GraveyardService`'s
  extra-turn exile, egg counters, the library replacements or Rest in Peace, the departure event is rewritten in
  place to the actual zone. A departure is therefore always one event. The card ids of every component
  (`cardsLeavingBattlefield`, the original and the current card) wait for that arrival. The first one to arrive
  ends the wait.
- A departure whose card never arrives keeps its intended zone. That happens for tokens (CR 111.7: a token that left
  the battlefield went to that zone before it ceased to exist) and for `removePermanentWithoutDestination`
  (`OUTSIDE_GAME`). Spellmorph's `STACK` departure is then met by its stack arrival.

**Causes.** The engine names a cause around the call that moves one object:

- `PermanentRemovalService.departToGraveyard`: every `DepartureIntent` cause, so destroy, sacrifice and state-based
  removals, including a sacrifice that a replacement sends into exile;
- `GraveyardService.discardCard`: `DISCARD`, which still applies when Library of Leng puts the card on the library
  or madness exiles it;
- `GraveyardService.millCards`: `MILL`, per card.

Everything else is `OTHER`. An entry's cause is `OTHER` unless one was named for the permanent's id; none is yet.

**Batches.**

- The existing scopes open rules event batches as well: `beginPermanentLeaveBatch`/`endPermanentLeaveBatch` (so
  `DepartureService.perform` and `performSimultaneousRemovals`), `beginGraveyardLeaveBatch`/`endGraveyardLeaveBatch`
  (which also wraps `BattlefieldEntryBatchSupport.placeCards` and the mass graveyard returns) and
  `GameData.runAsSingleCardExileBatch`.
- `millCards` opens its own batch.
- Everything else is one batch per event. Tokens created by one effect, for example, are separate batches. The
  `collectingSimultaneousCreatureEntryTriggers` flag is not a scope, and token creation sets none. Step 12 needs
  real scopes there; `TOKEN_CREATION` is a token-creation event.
- The batches are drained in `TriggeredAbilityQueueService.beginTriggerOrder`, after its awaiting-input guard and
  before the APNAP ordering, which is the point where step 10's matcher will run (CR 603.3). For now the drained
  batches only reach the observer. A batch scope left open by mistake is never drained, so keep scopes in
  `try`/`finally`.

**Copies.**

- `simulationFrameCopy` fills the copy's zone lists while the copy's log is suppressed, then `copyFrom`s this game's
  log. That copy includes pending batches, waiting departures (remapped to the copied events), tracked zones and
  causes.
- `restoreMutableStateFrom` (turn restart) suppresses the log while it refills the lists, then restores the log from
  the snapshot. `rulesEvents` and `rulesObserver` are now in `preservedDuringTurnRestart`.
- `rulesObserver` is never copied. It is in `SimulationCopyCompletenessTest.DELIBERATELY_NOT_COPIED`.
- Cost per simulation frame: one map entry per card the game has seen, plus the pending batches, which are drained
  before every priority.

**The shadow recorder** (`magical-vibes-engine` test fixtures, `testutil`).

- `ShadowTriggerRecorder.attach(gameData)` installs itself as `rulesObserver`. `GameTestHarness` attaches one to
  every game and exposes it as `getShadowRecorder()`.
- The old path: `GameData` reports these routes:
  - every `TRIGGERED_ABILITY` entry added to the stack (`enqueueTrigger`, direct `stack.add`, the `queueMayAbility`
    variants, all of which end in `stack.add`);
  - `enqueueTrigger`'s mana-ability and subgame queues;
  - every `PendingMayAbility` (the list is now an `ObservedMayAbilityList`; this includes resolution-time "may"
    questions);
  - `queueInteraction` and `queueInteractionFirst`.

  An item is recorded once by identity. A stack entry that waited in the mana-ability queue isn't recorded again
  when it is flushed, and the extra copies `activeTriggeredAbilityCopies` makes are never reported. Each record
  holds the route, a `ShadowTrigger(sourceCardId, effects)` and the item.
- The new path's input: drained batches plus `pending()`. `clear()` forgets everything so far without draining the
  game.
- `differences()` keeps the old-path records with a source that the `ShadowComparison` `compares`, and matches them
  as a multiset against `comparison.newPath(gameData, batches)`. Effects compare by value, because collectors bind
  `…AwareEffect`s into new records. Interactions have no readable source or effects. They are kept for diagnosis
  only, and a trigger that reaches the stack after its interaction is compared through its stack entry.
- `ShadowComparison.CURRENT` is `NONE`: no family is in shadow mode. `BaseCardTest`'s new `@AfterEach
  assertShadowTriggerPathsAgree` runs `assertPathsAgree()` in every card test, so it runs and compares nothing yet.
  A test can pass another comparison through `setComparison`.

**For step 10.**

- Set `ShadowComparison.CURRENT` to the pilot's comparison:
  - `compares`: the trigger's source card has those effects in a pilot slot (by value, unwrapped where the collector
    unwraps);
  - `newPath`: what the matcher triggers for the batches.

  Every card test then shadows the pilot with no other change.
- The old collectors queue at the choke point, inside the action, while the batches are drained later, at
  `beginTriggerOrder`. The comparison covers the whole test, not each batch. Step 10's matcher must read the
  snapshots on the events, not the live board, for look-back triggers (CR 603.10a).
- Old-path may abilities that are asked before the stack (`MAY_ABILITY`) are recorded, and so are the
  resolution-time ones. `compares` must tell them apart, for example by whether the effects are a pilot slot's
  registrations.
- Gaps the matcher will meet:
  - A card moved between two players' zones of the same kind (one graveyard to another) is no event: the log tracks
    a `Zone`, not an owner's zone.
  - Cards arriving from a sideboard come from `null`: sideboards aren't tracked.
  - The ante zone is reported as `EXILE`, because the engine represents it as exile entries.
  - A commander whose zone move is deferred (`deferCommanderZoneMove`) arrives only when the owner's choice is made.
    If that happens after the departure's batch was drained, its departure keeps the intended zone and the later
    arrival is a second event from that zone.

**Tests changed.**

- `SimulationCopyCompletenessTest`: `rulesObserver` added to `DELIBERATELY_NOT_COPIED`.
- `GameDataDeepCopyTest` (`magical-vibes-ai`): new `deepCopyPreservesPendingRulesEventsIndependently`. Pending
  events and tracked zones survive a copy and stay independent; the copy emits nothing while it is built and
  carries no observer.
- New `RulesEventLogTest` (`magical-vibes-application`, `model/rules/`): tracked origins, moves inside a zone,
  batches and scopes, drain, departures and their rewrite, entries, retracted entries, causes, suppression and
  copies.
- New `ZoneChangeRulesEventTest` (`magical-vibes-application`, `mechanics/`). It resolves each choke point through
  the engine with real cards:
  - destroy with last-known power (Giant Growth, then Terror);
  - a Wrath of God as one batch of three;
  - sacrifice (Diabolic Edict);
  - lethal damage (Shock);
  - bounce (Unsummon);
  - exile (Swords to Plowshares);
  - top of library (Griptide);
  - Rest in Peace turning a death into one move to exile;
  - tokens entering from nowhere and dying (Raise the Alarm, Wrath of God);
  - a creature cast from hand to stack to battlefield;
  - an instant from stack to graveyard;
  - a mill as one batch (Tome Scour);
  - discard (Mind Rot);
  - draw (Divination);
  - graveyard to hand (Raise Dead);
  - graveyard to exile (Coffin Purge);
  - a library reorder emitting nothing;
  - the drain before priority;
  - an old-path trigger being recorded (Soul Warden).
- `BaseCardTest` and `GameTestHarness` gained the recorder and its `@AfterEach`.
- Three card tests that the user's full run reported failing were flaky, depending on randomness that this step
  doesn't touch. Each failure was reproduced and explained before the test was changed:
  - `SeedlingCharmTest#returnsAuraToOwnerRatherThanController` and
    `HurkylsRecallTest#doesNotReturnArtifactsOwnedByAnotherPlayer` assert by name that a card is not in a hand. The
    random 10E white opening hand sometimes holds a Pacifism or an Icy Manipulator. The first test now empties
    player 2's hand; `HurkylsRecallTest` empties both hands in a `@BeforeEach`, which also makes its by-name
    `assertInHand` checks meaningful.
  - `FearOfChangeTest`: the conjured mana-value-4 creature is random.
    - `conjuringTheCreatureTriggersConjureAbilities`: when the creature has an enters question of its own (Gavony
      Silversmith, Skeleton Archer, Solemn Simulacrum, Species Specialist), `resolveAllTriggers` stops at it before
      Thayan Evokers' ability resolves. The test now asserts that the conjure ability is on the stack with Thayan
      Evokers' `ON_CONTROLLER_CONJURES` effects; resolving it is Thayan Evokers' own test.
    - The two "conjures a creature with mana value four" tests: when the creature is a Clone or Phyrexian Metamorph,
      it waits on its "enter as a copy" question. The tests now decline the question, and then the 0/0 dies, so they
      look for the conjured card (`isConjured()`) on the battlefield or in the graveyard.

    Probes of 250 to 400 draws per Fear of Change scenario now pass.

**Test runs.**

- Card: 3,302 classes, 25,839 tests, all passing. That is the card test classes of steps 10 and 11
  (`--gradle-args 10` and `11`), plus every card whose class names a mill, discard, madness, mutate, meld, flicker,
  blink or exile-instead-of-graveyard effect. The Upgrade cards (Weaponized Scrap; Iron Spider, Stark Upgrade; and
  Biogenic Upgrade) and Do Over (turn restart) were run again after `retractEntry` and pass. Every card test now
  runs the shadow recorder and its `@AfterEach`.
- AI: the whole module (1,620 tests). It has the same 10 failures as steps 7 and 8.
- Application: the new tests, `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest`, `CardFreezeTest`,
  `CommanderGameTest`, `GameDataExileTrackingTest`, `GameRegistrySessionTest`, `DepartureServiceOrphanedStepBatchTest`,
  `AsEntersInteractionServiceTest`, `PermanentRemovalServiceTest` and `BattlefieldPlacementServiceTest` (172
  tests). There are 72 failures. The last two classes have 70 of them (mock wiring: null `DepartureService`,
  `layerSystemServiceProvider` and `AsEntersDiscardSupport`), and the two architecture tests have the known one
  each. With the change stashed, HEAD fails the same four classes with the same per-class counts.
- The full suite was not run here.


### Step 10a

10a builds the trigger pipeline and runs the pilot slots (`ON_DEATH`, `ON_SELF_LEAVES_BATTLEFIELD`, the four
`ON_SELF_PUT_INTO_GRAVEYARD_FROM_…` slots and the dead `ON_SELF_MILLED`) in shadow mode. No slot moved, no card
changed and the old collectors still put every card's abilities on the stack. 10b switches the slots.

**Domain types.**

- `model.trigger.ObjectRelation`: `THIS`, `YOU_CONTROL`, `ANY`, `ANOTHER_YOU_CONTROL`, `ANOTHER`,
  `THIS_OR_ANOTHER_YOU_CONTROL`, `THIS_OR_ANOTHER`, `OPPONENT_CONTROLS`, `ENCHANTED`, `EQUIPPED`, with
  `includesThis()`.
- `model.trigger.ZoneChangeTrigger(ObjectRelation relation, Set<Zone> from, Zone to, Set<ZoneChangeCause> causes)`.
  An empty `from` means any zone, outside the game included; a `null` `to` means any zone; empty `causes` means any
  cause. A move inside one zone is rejected.
  - `looksBack()` is true for leaving the battlefield or a graveyard (CR 603.10a).
  - `defaultZones()`: the source's own look-back move functions from the zone it left. Its other moves function
    from the zone it arrives in (CR 603.10): "put into a graveyard from your hand", "from anywhere" (CR 603.6c)
    and milled all function from the graveyard. Every other relation functions from the battlefield.
- `model.trigger.Triggers`: `dies`, `sacrificed` (any destination, since a replacement that exiles a sacrificed
  permanent doesn't undo the sacrifice), `leavesBattlefield`, `putIntoGraveyardFrom(zone)`,
  `putIntoGraveyardFromAnywhere`, `milled`. `Trigger` permits `ZoneChangeTrigger`.
- `Card.addTrigger(Trigger, CardEffect...)`, `addTrigger(TriggerCondition, TriggerOptions, CardEffect...)`,
  `addTriggeredAbility(TriggeredAbility)` and `getTriggeredAbilities()`.
  - An ability with a real trigger has no slot: `LegacySlots.slotOf` returns `null`, the slot views skip it, and
    `addAbility` appends it as it is.
  - No card uses `addTrigger` yet, so agent-docs and the card skills don't name it; 10b changes them with the
    pilot's cards.
- `Permanent.getTemporaryTriggeredAbilities()` and `getPersistentTriggeredAbilities()`.
- `ZoneChangeEvent` gains two fields, both captured just before the permanent leaves:
  - `grantedAbilities`: the "when this dies" abilities other permanents grant the departing permanent. For now
    these are the legacy `ON_DEATH` grants, from `DepartureService`'s batch snapshot or
    `PermanentRemovalService.grantedSelfDeathEffects`.
  - `deathTriggersSuppressed`: Hushbringer.

  Both are taken before the permanent leaves, because `DepartureService` restores its `simultaneousDying…` maps
  before a batch closes. `died()` is new.
- `ZoneChangeCause.CRAFT`: exiled to pay a craft cost (CR 702.167a). `PermanentRemovalService` sets it for
  `exiledWhileActivatingCraftAbility`.
- `GameData`:
  - `keyedOnceOnlyTriggersFired`: `TriggerLimit.ONCE` per source and ability, copied for simulations.
  - `setRulesEventListener` (carried into simulation copies like `cardsExiledListener`).
  - `CollectingTrigger` with `beginCollectingTrigger`/`endCollectingTrigger`, transient and only for the
    observer.
  - `RulesEventLog(Consumer<RulesEventBatch> onClose)` hands every batch to `GameData`'s listener as it closes.

**Matching happens when a batch closes, not at `beginTriggerOrder`.** This changes step 9's plan. An occurrence is
complete once its outermost scope ends, and matching it then is what the old collectors do at their choke points,
so trigger timing stays the same. Matching at the drain would have run after prompts and auto-pass, and only once
pending questions were answered. `beginTriggerOrder` still drains, which now only releases the closed batches.
Scopes were added so that a departure's batch closes after its card arrived:

- the ten leave bodies of `PermanentRemovalService`;
- the core `GraveyardService.addCardToGraveyard`;
- the two `AuraAttachmentService` sweeps, each as one batch.

**A step 9 gap, fixed here.** The two Aura sweeps (`removeOrphanedAuras`, `enforceAttachmentLegality`) take Auras
off the battlefield themselves, not through `PermanentRemovalService`. Their departure was therefore a plain
arrival with no snapshot. They now emit it (`emitSweptDeparture`).

**The pipeline** (`service/trigger/pipeline`).

- `RulesEventPipeline.process` is the batch listener. `GameSetupService` registers it on every game and subgame.
  - For each event it asks the index for candidates and matches them. It triggers an ability with a real
    trigger: once per matching event for `EACH`, once per batch for `ONE_OR_MORE`.
  - An ability still in a slot is reported to the observer only, and only if the observer shadows that slot.
- `TriggerIndex.movingObjectCandidates` gives two sets of candidates for the object that moved:
  - The departed permanent as it last existed:
    - the effective card's abilities;
    - its persistent abilities;
    - the event's granted abilities;
    - its temporary abilities;
    - its delayed death effects.

    Everything but the temporary abilities and the delayed death effects is dropped when it had lost all its
    abilities. The temporary abilities are kept as the old collector keeps them (Infuse with Vitality after
    Humble), and the delayed death effects because they aren't abilities of the permanent. A face-down permanent
    has none of its card's abilities.
  - The card in the zone it went to: its abilities and its perpetual grants. It has none in a graveyard under
    Yixlid Jailer.

  Each candidate's zone decides which trigger conditions it can meet. Abilities perpetually removed are left out.
  Only `THIS` is matched: step 11 adds the other objects, with the look-back of a simultaneous batch.
- `TriggerMatcher` checks:
  - the condition's zones (CR 113.6);
  - the relation;
  - `ZoneChangeTrigger.matchesMove`;
  - Hushbringer's suppression of a creature dying, which doesn't stop a sacrifice trigger
    (`DaemogothWoeEaterTest`).
- `TriggeredAbilityStackService.trigger` puts the ability on the stack:
  - It checks the intervening "if" as it triggers and wraps every effect in an intervening-if
    `ConditionalEffect` for the check on resolution (CR 603.4).
  - Limits: `ONCE_EACH_TURN` in `keyedOncePerTurnTriggersFiredThisTurn` under `trigger:<ability hash>`;
    `DO_ONLY_ONCE_EACH_TURN` as `OncePerTurnTriggerEffect.markOnAcceptance` does (the unkeyed set, marked on
    acceptance through `markSourceOncePerTurnOnAcceptance`, CR 603.2h); `ONCE` in `keyedOnceOnlyTriggersFired`.
    Equal abilities of one source share a key.
  - A targeting ability queues a `DeathTriggerTarget` with `creaturesOnly` off, which `TriggerTargetCollector`
    serves as it goes on the stack (CR 603.3d). Any other ability goes through `enqueueTrigger`.
  - Additional-trigger copies are counted as the slot collectors count them, through a `TriggerContext`. That is
    a bridge until step 16.
  - A graveyard source's entry records the card's graveyard entry version (CR 400.7).
- `LegacySlotTriggers` maps the pilot slots to their `Trigger`. `ON_DEATH` registrations that only trigger on a
  sacrifice map to `Triggers.sacrificed`.
- `LegacyTriggerOptions` reads a slot ability's trigger-only wrappers for the pipeline:
  - the death conditionals and death intervening "if", through the collector's own new
    `TriggerCollectionService.legacyDeathTriggerEffect`;
  - the intervening "if"s handlers wrote as effects: "had a counter" (Promising Duskmage, Ochre Jelly, Nine-Lives
    Familiar, Ambitious Augmenter, the move-counters effects);
  - "into your graveyard" (Brood of Cockroaches, Triumph of Saint Katherine);
  - "during combat" (Cathedral Membrane);
  - exiled only, and exiled for craft;
  - the leave collector's trigger-time-only `ConditionalEffect` check. An intervening "if" there is checked only
    on resolution, as the old collector checks it.
  - `OncePerTurnTriggerEffect` is unwrapped without checking its limit, because the collector has used the limit
    up before the batch closes.

  14 is the step that turns all of these into options and filters.

**Shadow mode.**

- `RulesObserver` gains `shadowedSlots()` and `wouldTrigger(slot, source, effects)`. `triggerQueued` now takes the
  `CollectingTrigger`.
  - `TriggerCollectionService.dispatch` marks the slot, the dispatched effect and the source.
  - The four GraveyardService self-graveyard collectors mark their slot.
  - `dispatch` also reports a prompt a handler begins directly instead of queueing it (Reveillark).
- `ShadowTriggerRecorder` keys an old-path trigger by its source card id and the class of the dispatched
  effect, not the effects that reach the stack. Handlers rewrite effects (Arcbound modular into `MayEffect`,
  token counts into `CreateTokenEffect`, …), which step 15 removes. It reads the source from any record item
  (`TriggeredModalTrigger` carries no effect list). Only records made inside a shadowed collector count, once
  each.
- `ShadowComparison` is now a record:
  - `slots`;
  - `nothingToDoDeclines`: effects whose handler skips an ability that would do nothing; a trigger only the
    pipeline reports for one of them is no difference;
  - `knownDifferences`: tests whose comparison is skipped, with the reason.

  `CURRENT` shadows the pilot. `BaseCardTest` passes the test's `Class#method`.

**Differences shadow mode found, and what became of them.** The first run of the pilot's card tests had 590
failing tests. All of them were shadow differences: no other test broke. The causes and fixes, in order:

1. The handlers' rewrites and `TriggeredModalTrigger`. Fixed by keying on the dispatched effect.
2. Leave effects that are filters ("when this is exiled", craft). Moved into `LegacyTriggerOptions`.
3. The Aura-sweep departures (the step 9 gap above).
4. "From anywhere" triggers when the card dies (Dread, Purity, Bone Rattler, Emrakul): the index now also offers
   the card in its new zone.
5. Sacrifice triggers when a replacement exiles the permanent (Carrot Cake, Ordeal of Nylea): `Triggers.sacrificed`
   takes any destination.
6. Handler intervening "if"s and filters. Moved into `LegacyTriggerOptions`.
7. The source a copy names. The old collectors name the effective card (Phantasmal Image as Gixian Puppeteer, Clone
   as Harold and Bob, back faces), and so does the pipeline.
8. A recorder bug: a pending question from another slot (Wernog's enter trigger) swallowed a leave trigger with
   the same key. The consumption step is gone.

What is left is recorded in `ShadowComparison`:

- **Old-path declines, `nothingToDoDeclines`.** The handler puts nothing on the stack when the ability would do
  nothing. None of these abilities has an intervening "if", so per CR 603.2 it triggers and resolves to nothing.
  This affects `RemoveLinkedPermanentEffect` (Dance of Many, Diabolic Servitude), `CreateTokensForExiledCardsWithSourceEffect`
  (Skyclave Apparition), `CreateTokenForImprintedCardOwnerEffect` (Severance Priest),
  `SacrificeEnchantedCreatureOnLeaveEffect` (Animate Dead, Necromancy), `ExileEquippedCreatureEffect` (Oathkeeper),
  `TargetPlayerGainsLifeEffect` (Laquatus's Champion) and `ManifestTopCardsForEachDyingSourceCounterEffect` with
  X = 0 (Omarthis). After 10b these abilities go on the stack and do nothing. The change is rules-correct but
  visible, and 10b should check those cards' tests.
- **Limits of shadow mode, `knownDifferences`.**
  - Once-each-turn limits really used earlier in the turn: Minthara, Terrors of the Track, Oni-Cult Anvil.
  - Attachments that left in the same event: Gunner Conscript and Stone Haven Outfitter. The departure event has
    no look-back of its attachments; step 11 needs one.
  - Triggers queued where the recorder doesn't look: Foul Emissary's `pendingSpellCastCostTriggers` and Jugan's
    divided counters.
- **Recorded as known differences, for 10b to decide.**
  - Tatsumasa's "return when that token dies" is printed on the token as an `ON_DEATH` ability, though it is a
    delayed trigger (CR 603.7). The old death check misses Humility; the event's layer-aware flag doesn't. 10b
    should make it a delayed trigger.
  - Serra Paragon on Echoing Deeps: the old collector names the physical card, though it names the copied card
    for a creature copy.

**For 10b.**

- Switching a slot means converting its cards to `addTrigger(Triggers.…)`. The pipeline then puts them on the
  stack through `TriggeredAbilityStackService`, which reads options rather than wrappers. Until step 14 the
  converted abilities keep their wrappers, so the stack service must read them through `LegacyTriggerOptions`
  as shadow mode does. That adapter still needs a limit check that works when the pipeline is the only path:
  with no collector running first, `OncePerTurnTriggerSupport.unwrapIfAvailable` is right again.
- The death and leave handlers still decide each entry's details: the bound `…AwareEffect`s, the event value,
  the dying permanent's mana value, the exile entry version, the cast mode. 10b has to give the stack service
  those, or route the bound effects through `LegacyTriggerOptions`, before the handlers can go.
- Haunt on instants and sorceries ("when this spell is put into a graveyard during its resolution",
  CR 702.55a) needs a resolution cause on the stack-to-graveyard move. It isn't in shadow mode: the old path
  collects it outside `dispatch`.
- "When you sacrifice this … while casting with emerge" needs the casting spell on the event.
- Time the pilot's card test classes before and after the switch (the step asks for it).

**Tests changed.**

- `SimulationCopyCompletenessTest`: `collectingTrigger` is deliberately not copied (scratch state for one dispatch).
- `CardAbilitiesTest`: three new tests. A trigger is a triggered ability of its own that no slot view shows; it
  keeps its condition's zones and options; and an ability with a real trigger is added, and copied with the card,
  as it is.
- New `ZoneChangeTriggerTest` (`model/trigger/`): the default zones of the source's own moves and of others',
  move matching (zones, causes, from anywhere, outside the game), the rejected in-zone move, and `includesThis`.
- New `TriggerPipelineTest` (`mechanics/`). Real triggers on test cards, resolved through the engine:
  - a dies trigger goes on the stack and resolves;
  - a bounce doesn't trigger dies;
  - leaves the battlefield triggers for a bounce and for an exile;
  - Rest in Peace turns a death into no dies trigger but a leaves trigger;
  - a permanent that lost its abilities has no dies trigger;
  - put into a graveyard from hand triggers from the graveyard under its owner's control;
  - a false intervening "if" keeps it from triggering, and a true one wraps the effect for resolution;
  - once each turn;
  - a targeted trigger waits as a `DeathTriggerTarget`.
- `ZoneChangeRulesEventTest#theSlotCollectorsTriggersAreRecorded` reads `effectClasses()`.
- `BaseCardTest` and `GameTestHarness`: the comparison runs in every card test, keyed by test, with the pilot
  shadowed.

**Test runs.**

- Card, with the pilot shadowed:
  - the pilot's card test classes (`--gradle-args 10`): 590 failing tests at first, then 165, 46 and 1, and 0
    with the known differences recorded;
  - step 9's wider selection (steps 10 and 11 plus every mill, discard, madness, mutate, meld, flicker, blink and
    exile-instead card): 3,302 classes, 25,839 tests, all passing. No failure in any run was other than a shadow
    difference.
- AI: the whole module (1,620 tests), with the same 10 failures as steps 7 to 9.
- Application:
  - the new and changed tests above, plus `RulesEventLogTest`, `TriggerConditionTest`, `LegacySlotTriggerTest`,
    `TriggeredAbilityTest`, `GrantedAbilitiesTest`, `CardFreezeTest`, `CardImmutabilityArchTest`,
    `GameSetupServiceAllRandomTest`, `CommanderGameTest`, `GameRegistrySessionTest`, `AuraAttachmentServiceTest`,
    `DepartureServiceOrphanedStepBatchTest` and `GameDataExileTrackingTest`;
  - the mock-wired `PermanentRemovalServiceTest`, `BattlefieldPlacementServiceTest`,
    `DeathTriggerCollectorServiceTest` and `PlanechaseServiceTest`.

  905 tests ran. The failures are the 70 of step 9 in `PermanentRemovalServiceTest` and
  `BattlefieldPlacementServiceTest`, the known one each in the two architecture tests, and 11 in
  `DeathTriggerCollectorServiceTest` and `PlanechaseServiceTest`. Those 11 fail the same way at HEAD with the
  change stashed: mock wiring, and a planar ability with no card.
- The full suite was not run here.

### Step 10b

10b switched the pilot to the trigger pipeline. `ON_DEATH`, `ON_SELF_LEAVES_BATTLEFIELD`, the four
`ON_SELF_PUT_INTO_GRAVEYARD_FROM_…` slots and `ON_SELF_MILLED` are gone from `EffectSlot`, with their collectors and
handlers. Every card ability that used them has a real trigger and reaches the stack only through the pipeline.

**Authoring.**

- `addTrigger(Triggers.dies(ObjectRelation.THIS), effect)`, and `target(filter).addTrigger(…)` (new on `SpellTarget`).
  A grant takes `new TriggeredAbility(trigger, effect…)` (a new convenience constructor) where it took a slot and an
  effect. `GrantEffectToTargetEffect.toSourcePermanent(Ability)` is new.
- `Triggers.sacrificed` for "when you sacrifice this". The old `ON_DEATH` registrations whose effect
  `onlyTriggersOnSacrifice()` became it (14 cards). The `onlyIfSacrificed` flags and `SacrificeOnlyEffect` are now
  redundant with the trigger; step 14 removes them.
- `Triggers.putIntoGraveyardWhileResolving` for haunt on an instant or sorcery (CR 702.55a): a stack-to-graveyard
  move with the new `ZoneChangeCause.RESOLUTION`, which `StackResolutionService.handleSpellDisposition` names around
  the move of a spell that resolved. A countered spell or one that didn't resolve isn't that move. Benediction of
  Moons, Cry of Contrition and Seize the Soul.
- `ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` became `Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, THIS)`. That
  trigger equals `Triggers.dies(THIS)` (CR 700.4), so those 49 abilities now go through the death handlers rather than
  GraveyardService's old plain collector.
- `Card.getTriggerEffects(trigger)`, `getUnslottedTriggeredAbilities()` and `getTriggeredEffects()`, and
  `TriggeredAbility.hasTrigger(trigger)`, for code that used to read these slots.

**Scripts.**

- `scripts/step10b-pilot-scan.java` listed every card's registrations in the pilot slots before the step (1,085, in
  1,163 files with the grants), marking the sacrifice-only ones and the haunt spells.
- `scripts/step10b-pilot-triggers.py --scan` rewrote card classes and card tests per letter module: `addEffect`,
  `LegacySlots.triggered` and grant argument pairs, `getTemporaryTriggeredEffects(ON_DEATH)` in one test, and
  comments that named the slots. One card it couldn't convert, done by hand: Jaxis, the Troublemaker passed a
  `List.<CardEffect>of(…)` to `LegacySlots.triggered`.
- `scripts/step10b-docs.py` rewrote agent-docs the same way. Lines naming a sacrifice or haunt were checked by hand,
  and the sacrifice-only ones changed to `Triggers.sacrificed`.

**The pipeline after the switch.**

- `TriggeredAbilityStackService` puts an ability of one effect on the stack through two adapters, each removed by a
  later step:
  - `LegacyTriggerOptions` (step 14) now reads the wrappers by the kind of zone change, not by slot: the
    creature-death conditionals, `OncePerTurnTriggerEffect` (checked and used up, now that no collector runs first),
    the death intervening "if" and "put into your graveyard" for dies; conditionals and intervening "if" for
    sacrificed; once each turn for leaves.
  - `LegacyTriggerBindings` (step 15, new) holds the pilot's 56 old `@CollectsTrigger` handlers and the sacrifice-only one,
    moved from `DeathTriggerCollectorService` and reading the `ZoneChangeEvent` instead of a
    `TriggerContext`. It dispatches by effect class as `TriggerCollectorRegistry` did (exact class, then the class a
    `MayEffect`, `MayPayManaEffect` or `MayPayTapPermanentsEffect` wraps, then the default). The binding, routes
    (`DeathTriggerTarget`, `SelfTriggeredAbilityTarget`, `TriggeredModalTrigger`, the emerge cost queue) and declines
    are the handlers' own, so behaviour is unchanged. That includes 10a's "nothing to do" declines (Dance of Many,
    Skyclave Apparition and the rest): their handlers still put nothing on the stack. CR 603.2 would have them
    trigger and do nothing; step 15 decides that with the event data.
- An ability of several effects is one stack object (CR 603.3), with a dies ability's effects bound to the card that
  died. No converted card has one yet: the script kept one ability per registration, as the collectors resolved them.
- `RulesEventPipeline` puts one event's abilities on the stack in the order the old collectors did: leaves the
  battlefield, arrives in a zone (from anywhere, from hand or library, milled, haunt), dies, sacrificed
  (`LegacyTriggerBindings.Kind`'s order). Worldspine Wurm's test relies on it.
- `TriggerIndex`: the abilities printed on a departed permanent's card name that card as their source, the physical
  card when the last-known snapshot is a copy of it (the snapshot holds a layered runtime copy, which effects that
  move "this card" don't find). Abilities the permanent gained (persistent, temporary, granted by other permanents,
  delayed) name the card that moved: Serra Paragon's grant on an Echoing Deeps that entered as a copy, 10a's known
  difference, now exiles Echoing Deeps.
- `LegacySlotTriggers` maps nothing and `ShadowComparison.CURRENT` is `NONE`: no family is in shadow mode. Step 11
  maps the creature-death slots there first.

**The departure event carries more look-back** (CR 603.10a), all captured as the permanent leaves:

- `attachments` (`DepartedAttachments`): the Auras and Equipment attached to it, those leaving in the same event
  included (`DyingCreatureAttachmentSnapshot.capture` at `emitDeparture`). This fixes 10a's two attachment known
  differences (Gunner Conscript, Stone Haven Outfitter).
- `costOf`: the spell it was sacrificed to pay for, named by `SpellCastingService.sacrificeForSpellCost` through the
  new `RulesEventLog.setPayingCostOf` (copied with the log). Foul Emissary's emerge trigger reads it.
- `grantedAbilities` are now every granted ability with a `THIS` zone-change trigger
  (`GrantedTriggeredAbilitySupport.grantedSelfZoneChangeAbilities`), and `GameData.simultaneousDyingGrantedSelfDeathEffects`
  became `simultaneousDyingGrantedSelfAbilities` (`List<TriggeredAbility>`, copied for simulations as before);
  `DepartureSnapshot.grantedSelfDeathEffects` became `grantedSelfAbilities`.

**Batch timing.** `DepartureService.perform` now ends a multi-permanent leave batch before it restores the
`simultaneousDying…` maps, so the batch is matched while its look-back is still published. Kodama of the Center
Tree ("Spirits you control" counted before any Spirit left) needed it. The batched "leave without dying" collectors
in `endPermanentLeaveBatch` now also run before the restore; none of them reads those maps.

**Engine code that walked the slots.** A real trigger is in no slot, so every place that rebuilt or scanned a card's
triggered abilities slot by slot would have lost it. These now also walk `getUnslottedTriggeredAbilities()` or read
`getTriggeredEffects()`: `TokenCopySupport`, `CopySupport`, `PermanentCopierService`, `MutationSupport`,
`LayerSystemService.buildFullTextCopy`, `EachOpponentChooses…TokenCopy…`, `CreateTokenWithTriggeredAbilitiesOfExiledCards`
(now passes `List<Ability>` to a new `PermanentControlSupport.applyCreateToken` overload), the two "gain the abilities
of exiled cards" static handlers, `PredicateEvaluationService`'s "has abilities" checks, and five effect scans
(`TriggeredAbilityQueueService`, `GameViewProjectionFactory`, `ChoiceHandlerService`, `CreatureControlService`,
`PerpetuallyBoostCardEffectHandler`, `CastTargetInstantOrSorceryFromGraveyardHandler`).

Perpetual and entry grants that passed `(slot, effect)` now pass the ability: the `PerpetuallyGrant…` handlers,
`SeekLibraryAndPerpetuallyModifySoughtCardEffectHandler`, `BattlefieldPlacementService.applyPerpetualTriggeredAbilityGrants`,
`GrantTriggeredAbilityToCastSpellEffectHandler` and Serra Paragon's cast permission (new
`StackEntry.addPersistentAbilityOnEntry`, `GameData.grantPerpetualAbilityOnce(UUID, Ability)`). Still slot-based, with
no card granting a pilot trigger through them: `ChooseCard(s)FromHandToPerpetuallyGrantTriggeredAbilityEffect`
(its `PendingInteraction` carries a slot), `PerpetuallyGrantTriggeredAbilityToRandomMatchingHandCardEffect` and
`RemovePerpetualTriggeredAbilityEffect`. Step 11 or whichever step first grants a real trigger through them converts
them.

**Removed:** the constants; `collectDeathTrigger`, `collectSelfSacrificedTriggers`, `collectSpellHauntTrigger`,
`legacyDeathTriggerEffect`, `effectivePrintedDeathEffects` and the leave collector (its delayed-trigger part stays as
`checkDelayedLeavesBattlefieldTriggers`: the `DelayedZoneChangeTrigger`s and the leave watchers a spell registers,
CR 603.7); GraveyardService's four self-graveyard collectors, the self-mill collector and the
`selfGraveyardTriggerSuppressed` parameter chain; `SacrificeOnlyTriggerCollectorService`; the pilot handlers and their
helpers in `DeathTriggerCollectorService` (its Distribute handler keeps only the "a creature dies" branch);
`TriggerContext.SelfDeath` and `SelfLeaves`; `GrantedTriggeredAbilitySupport.grantedTriggeredEffectsFromAttachments`.
`DeathTriggerTarget` and `SelfTriggeredAbilityTarget` stay: other slots use them.

**Rules fixes, each covered by an existing card test that now passes through the engine:**

- Tatsumasa, the Dragon's Fang: "return Tatsumasa when that token dies" is a delayed trigger (CR 603.7), not an
  ability of the token. `CreateTokenEffectHandler` now takes the placeholder ability off the token and adds the bound
  return as the token's delayed death effect, so it triggers under Humility (10a's known difference).
- Gunner Conscript and Stone Haven Outfitter: their attachments that left in the same event count (above).
- Serra Paragon on a copy land: above.
- "This ability triggers only once each turn" now also keeps a death trigger from being copied by Teysa-like effects;
  the collector checked it only for leave triggers.

**Timing** of the pilot's card test classes (`--gradle-args 10`: 1,152 classes, 8,403 tests), both runs with the
classes already compiled, default Gradle settings:

| Run | HEAD (`dc792b6fd0`, pilot shadowed) | 10b |
|---|---|---|
| First pair, wall | 142 s | 173 s |
| First pair, summed class time | 3,585 s | 3,723 s |
| Second pair (back to back, build cache off), wall | 165 s | 183 s |
| Second pair, summed class time | 3,457 s | 3,532 s |

  The machine is shared, so the wall times are noisy; summed class time rose by 2 to 4 %. HEAD's numbers include the
  shadow recorder's comparison, which 10b no longer runs. The HEAD runs used a worktree at `dc792b6fd0` with the
  repository's card-data cache copied in (without it, 73 tests fail to load card data).

**Tests changed.**

- `LegacyTriggerBindingsTest` (new, `service/trigger/pipeline`): the 31 pilot handler tests of
  `DeathTriggerCollectorServiceTest`, moved with the handlers and driven through `LegacyTriggerBindings.trigger` with a
  zone-change event. Five tests of a `null` dying permanent were dropped: a departure always carries one now. Three
  moved tests already failed at HEAD and are fixed: the blocked-attackers tests now block through
  `addBlockingTargetId` (and "not blocking" now expects the ability to trigger with no creatures to damage, as the
  handler does), and the Vastwood Hydra distribution expects `DistributeCountersAmongControlledCreaturesEffect`.
- `DeathTriggerCollectorServiceTest`: the moved tests removed.
- `TriggerPipelineTest`: two new tests, a leaves trigger goes on the stack before the dies trigger of the same death,
  and a dies trigger of two effects is one stack object.
- `CardAbilitiesTest`, `GrantedAbilitiesTest`, `TriggeredAbilityTest`, `TriggerConditionTest`, `LegacySlotTriggerTest`
  and `GameDataDeepCopyTest` used `ON_DEATH` as an arbitrary one-ability-per-registration slot; they use
  `ON_SELF_BECOMES_UNTAPPED` now.
- `PermanentRemovalServiceTest` (mock-wired, failing at HEAD): its stubs and verifies follow the removed
  `addCardToGraveyard` parameter and `checkDelayedLeavesBattlefieldTriggers`; the `collectDeathTrigger` verify is gone.
- `RabidAttackTest` (by the script): reads `getTemporaryTriggeredAbilities()`.

**Test runs.**

- Card:
  - the pilot's card test classes (1,152 classes, 8,403 tests): 52 failures at first, all from four causes fixed in
    this step (the perpetual grants passing a slot, the stack entry naming the snapshot's card copy, the batch matched
    after its look-back was restored, the order of one event's triggers), then all passing;
  - step 11's card test classes plus every card using a copy, mutate, specialize, perpetual-grant or "gain the
    abilities of" effect, not already in the pilot (801 classes, 6,515 tests): one failure,
    `InallaArchmageRitualistTest#eminenceWorksFromTheCommandZone`, which also fails at HEAD. It was a step 10a bug:
    `TriggerCollectionService.dispatch` named `match.permanent().getCard()` for the observer, null for a command-zone
    source; it names `match.sourceCard()` now, and the test passes.
- AI: the whole module (1,620 tests), with the same 10 failures as steps 7 to 10a.
- Application: the tests above plus `RulesEventLogTest`, `ZoneChangeRulesEventTest`, `ZoneChangeTriggerTest`,
  `TriggerConditionTest`, `LegacySlotTriggerTest`, `TriggeredAbilityTest`, `GrantedAbilitiesTest`,
  `CardAbilitiesTest`, `CardFreezeTest`, `CardImmutabilityArchTest`, `SimulationCopyCompletenessTest`,
  `GameSetupServiceAllRandomTest`, `CommanderGameTest`, `GameRegistrySessionTest`, `AuraAttachmentServiceTest`,
  `DepartureServiceOrphanedStepBatchTest`, `GameDataExileTrackingTest` and the mock-wired
  `PermanentRemovalServiceTest`, `BattlefieldPlacementServiceTest`, `DeathTriggerCollectorServiceTest` and
  `PlanechaseServiceTest` (895 tests). 80 failures, every one also failing at HEAD in the same test (checked in the
  HEAD worktree): mock wiring in the four mock-wired classes and the known one in each architecture test.
- The full suite was not run here.

**For step 11.**

- Map the creature-death slots in `LegacySlotTriggers` and set `ShadowComparison.CURRENT` to them; the shadow path
  reads their wrappers through `LegacyTriggerOptions.resolve` with the trigger's `Kind`.
- Their handlers move into `LegacyTriggerBindings` the way the pilot's did. Its `Ctx` reads only the moving object;
  other relations need the source permanent as well.
- `TriggerIndex` still lists only the moving object's abilities, and `TriggerMatcher` matches only `THIS`.
- Cards that authored "this or another" as a dies ability plus an ally slot (Archghoul of Thraben, Tributary
  Instructor, Xathrid Necromancer and the rest named in their comments) are the self-inclusion cases step 11 merges.

### Step 11

Step 11 moved the thirteen creature-death slots to the trigger pipeline in one go (the user chose not to split it
the way step 10 was split): `ON_ALLY_CREATURE_DIES`, `ON_ANY_CREATURE_DIES`, `ON_OPPONENT_CREATURE_DIES`,
`ON_ALLY_NONTOKEN_CREATURE_DIES`, `ON_ANY_NONTOKEN_CREATURE_DIES`, `ON_ENCHANTED_PLAYER_NONTOKEN_CREATURE_DIES`,
`ON_ALLY_CREATURE_OR_PLANESWALKER_DIES`, `ON_ALLY_ARTIFACT_OR_CREATURE_DIES`, `ON_EQUIPPED_CREATURE_DIES`,
`ON_DAMAGED_CREATURE_DIES`, `ON_HAUNTED_CREATURE_DIES`, `GRAVEYARD_ON_ALLY_CREATURE_DIES` and
`GRAVEYARD_ON_OPPONENT_CREATURE_DIES` are gone, with their `check…` collectors, the batched collectors, the printed-type
self-inclusion heuristic and their `@CollectsTrigger` registrations. `refactors-to-do.md` entry 2 is removed.

**Authoring.**

- `ObjectRelation` gains `ENCHANTED_PLAYER_CONTROLS`, `HAUNTED` (functions from exile, CR 702.55c), `DAMAGED_BY_THIS`
  and `DAMAGED_BY_ATTACHED`, plus `youControl()` and `thisAlwaysMatches()`. `includesThis()` keeps its meaning.
- `ZoneChangeTrigger` gains a `PermanentPredicate filter` (`null` for any object). `Triggers.dies(filter, relation)`,
  `creatureDies(relation)`, `creatureDies(filter, relation)`, `nontokenCreatureDies(…)` and the constants `CREATURE`,
  `NONTOKEN_CREATURE`, `ARTIFACT_OR_CREATURE`, `CREATURE_OR_PLANESWALKER`.
- `TriggerOptions.ONE_OR_MORE` for "whenever one or more … die" (CR 603.2c); `OneOrMoreCreatureDeathTriggerEffect`,
  `OneOrMoreArtifactOrCreatureDeathTriggerEffect`, `BatchedCreatureDeathTriggerEffect` and
  `BatchedArtifactOrCreatureDeathTriggerEffect` are deleted.
- `TriggeredAbility(TriggerCondition, CardEffect...)` for a grant that functions from another zone (Kami of Mourning's
  graveyard ability); `SpellTarget.addTrigger` takes several effects (one ability).
- The relation follows the Oracle wording: "a creature you control" `YOU_CONTROL` includes the source, "another"
  never does, "this creature or another" always does. A card no longer adds a `Triggers.dies(THIS)` copy of the
  ability for its own death. The `implement-card` and `review-card` skills say so.

**The pipeline.**

- `TriggerIndex.observerCandidates(gameData, event, batch)` lists the abilities of the other objects that may see a
  permanent leave, as they were just before the event (CR 603.10a): the battlefield (printed abilities unless lost or
  face down, `lostPrintedAbilityWatcherIdsBeforeLeave` included; persistent, granted and temporary ones; Relic Vial's
  "as long as you control a Cleric" grant judged with the departed permanents), the permanents that left in the same
  batch (their departure events: last-known copy, granted abilities, controller), graveyard cards that didn't move in
  the batch, and the exiled cards that haunted it.
- **Look-back comes from the batch's own events, not the `simultaneousDying…` maps.** Several departure paths restore
  those maps before the batch closes (Barter in Blood's each-player sacrifice), so a watcher that died alongside never
  saw the others. `PermanentRemovalService.performSimultaneousRemovals` now also opens one rules batch.
- `TriggerMatcher.matchedTrigger(…, movingObject)` checks each relation. The moving object's own abilities match
  `THIS` and, while it left the battlefield, the relations that include the source (with the filter, except for
  "this or another"). The filter is judged on the departed copy without the game: its card holds the types, subtypes,
  colors and keywords it left with, and a layered query would read the game after the departure (Athreos stops being
  a creature once its own devotion is gone).
- `RulesEventPipeline` triggers observers first (each, then "one or more" once per batch), then the moving objects'
  own abilities as before. That keeps the old order (observer collectors queued during the removal, before 10b's
  batch-close self triggers), but observers now queue at batch close, after the old-path triggers of the same death
  (undying, persist, delayed effects); the controller still orders them (CR 603.3b).
- `TriggeredAbilityStackService.triggerOnce` checks options as for any ability, then calls
  `TriggerCollectionService.triggerCreatureDeath` / `triggerCreatureDeaths`: the old collectors' per-watcher bodies,
  ported unchanged and picked by the new `CreatureDeathWatch` (the old slot, derived from relation, filter base and
  zone). Their handlers stay in `DeathTriggerCollectorService`, off `@CollectsTrigger` and in an explicit registry
  keyed by watch and effect class (`dispatchCreatureDeath`, same resolution order as the registry); 64 were re-keyed by
  `scripts/step11-rekey-death-handlers.py`. Step 15 replaces both, step 14 the wrappers they read.
- The departure event gains `hauntedBy` (`handleExileReturnOnLeave` ends the haunt link before the batch closes), and
  the last-known copy now merges the per-source damage tracker (`creatureCardsDamagedThisTurnBySourcePermanent`) into
  `permanentIdsThatDealtDamageThisTurn`, which `DAMAGED_BY_THIS` / `DAMAGED_BY_ATTACHED` read; `GraveyardService` keeps
  only the Krovikan Vampire bookkeeping and the delayed damaged-creature watchers.
- `processAlreadyRemovedToGraveyard` gives its departure the effective last-known card and toughness, like the other
  leave path.
- Granted zone-change abilities are captured whole now: `grantedSelf…` became `grantedZoneChangeAbilities`
  (`GrantedTriggeredAbilitySupport`, `GameData.simultaneousDyingGrantedZoneChangeAbilities`, `DepartureSnapshot`), and
  `simultaneousDyingGrantedCreatureDeathEffects` / `DepartureSnapshot.grantedCreatureDeathEffects` are gone.
- **Still collected at the choke point** (`checkCreatureDeathRegisteredTriggers`, keyed by the ability's trigger, not a
  slot): global abilities a spell registered until end of turn (Warhost's Frenzy, Gnawing Crescendo, Shriveling Rot,
  Consumed by History, Infested Thrinax), Desperate Measures' delayed "when it dies" (now `dies(THIS)` on the watched
  creature), emblems, the delayed death watchers and face-up planes (Agyrem, Grand Ossuary, CR 901.7). They aren't
  objects the index lists; a later step moves them with delayed triggers.
- `RegisterGlobalTriggeredAbilityUntilEndOfTurnEffectHandler` keeps the granted ability instead of rebuilding a slot
  ability; `BecomeCopyOfDyingCreatureEffectHandler` re-grants Cemetery Puca's ability as an ability; the AI's
  `SpellEvaluator` looks for a "you control" / "any" dies trigger.

**Shadow mode was not run.** The old ally, opponent, graveyard, equipped, damaged and haunted collectors put most
abilities on the stack directly, outside `dispatch`, so the recorder couldn't key them. The step ported each
collector's per-watcher body unchanged instead, so only the choice of watchers is new code, and the card tests check
that (below).

**Cards.**

- `scripts/step11-death-scan.py` lists every slot user with its Oracle sentences from the server's card-data cache;
  `scripts/step11-death-classify.py` turns the wording into a relation (`scripts/step11-death-classification.tsv`,
  442 registrations). The nine cards without a registration of their own (back faces) and the twelve the classifier
  couldn't read were checked by hand, the back faces with the Scryfall MCP; 47 relations are corrected in the script.
- `scripts/step11-death-triggers.py` converted 413 card classes per letter module, `scripts/step11-death-manual.py` the
  rest by exact replacement: the "one or more" cards, Soul Shredder (one ability that functions from the battlefield
  and the graveyard), the planes (command zone), Kami of Mourning's graveyard grant, Desperate Measures, chained target
  groups. 437 card files changed.
- 82 duplicate `Triggers.dies(THIS)` registrations were dropped: the ability now includes its source. Nine cards'
  consecutive plain registrations became one ability, as the old ally collector stacked them (Dark Prophecy, Bastion of
  Remembrance, Vindictive Vampire, …), and Seize the Soul's haunt ability.
- **32 "this creature or another [restricted] creature" cards stay split**, `Triggers.dies(THIS)` plus
  `ANOTHER_YOU_CONTROL` / `ANOTHER` with the effect's conditional wrapper: the restriction ("another Zombie", "with
  power 5 or greater", "colorless", "with flash or haste") is still a wrapper, which would reject the source itself
  once it lost the characteristic (Azlask under Painter's Servant, an Amoeboid Changeling that lost its types). One
  event can't meet both halves, so they behave as the one Oracle ability. Step 14 merges them when the wrappers become
  filters judged on the last-known copy. Archghoul of Thraben, Xathrid Necromancer, Kalastria Highborn, Mycoid
  Shepherd, Dragon-Kami's Egg and the rest are listed in the script's `RELATION` table.

**Rules fixes**, each covered by a card test that resolves it:

- The cards in entry 2 trigger on their own death with one ability, not a copy (Indebted Samurai, Titania, Skyclave
  Shadowcat, Meltstrider Eulogist, Tributary Instructor, Shadow Urchin, Kheru Bloodsucker, Yarus, Blowfly
  Infestation). Knucklebone Witch is on `ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD`, step 12's.
- "A creature" abilities with no copy before now include their source: Cemetery Puca's copied ability triggers for
  the copy's own death, Moonlit Wake animated by Opalescence sees itself die, Genestealer Patriarch's and Lifeline's
  copies are gone.
- Abilities other objects grant are separate abilities (CR 603.3), no longer folded into the printed ally ability's
  stack object.
- An optional ally ability that triggers for its source's own death asks its question (CR 603.5) where the old copy
  skipped it (Indebted Samurai).

**For later steps.**

- Step 12: the remaining zone-change slots, including `ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD` and
  the other "put into a graveyard from the battlefield" families, can reuse `observerCandidates` and the relation
  matching. Their collectors still run at the choke point. `CreatureDeath` and `CreatureDeathWatch` are the pattern for
  porting a family's collectors before step 15.
- Step 14: `CreatureDeathWatch`'s collectors read the wrappers (`TriggeringCard/PermanentConditionalEffect`,
  `OncePerTurnTriggerEffect`, intervening-if `ConditionalEffect`); the 32 split cards merge then. The once-each-turn
  "one or more" cards (Morbid Opportunist, Xander's Wake, Homicide Investigator, …) keep `EACH` with
  `OncePerTurnTriggerEffect` and could become `ONE_OR_MORE` with a limit.
- Step 15: the death handlers still take `TriggerMatchContext` / `TriggerContext.CreatureDeath`, built from the event.
- Registered global, delayed, emblem and planar death triggers are still collected at the choke point (above).

**Tests changed.**

- `TriggerPipelineTest`: four new tests through the engine: a "creature you control" ability triggers for its own
  source's death; Moonlit Wake animated by Opalescence triggers for its own death; an "another creature" ability
  doesn't; CR 603.10a's example, Planar Cleansing destroying Moonlit Wake with two creatures, triggers it twice.
- `CemeteryPucaTest#copiesCopiableCharacteristicsOfCreatureThatWasAlreadyACopy`: the first Puca's copied ability now
  also triggers for its own death; the test declines that prompt and accepts the second Puca's.
- `IndebtedSamuraiTest#ownDeathTriggersAbility`: the merged ability asks its "may" question on its own death.
- `GameDataDeepCopyTest`: the granted-death-effects copy test now copies `simultaneousDyingGrantedZoneChangeAbilities`;
  a `DepartureSnapshot` lost an argument. `TurnCleanupServiceTest` registers a different slot for its cleanup check.
  `PermanentRemovalServiceTest` (mock-wired) verifies `checkCreatureDeathRegisteredTriggers`.
- Comments in `DeathTriggerCollectorServiceTest`, `ThrabenSentryTest`, `RotWolfTest` and `MurderOfCrowsTest` name the
  triggers.

**Test runs.**

- Card: every test class of a converted card and every card test that constructs one (666 classes): all passing
  after the fixes above. The first run of the converted cards' classes had 85 failures, from the global-registration
  handler, haunt links, look-back from the maps, the restrictions on "this or another" cards and the three
  self-inclusion changes above; the wider run then caught Amoeboid Changeling losing its types under a filter.
- AI: the whole module (1,620 tests), the same 10 failures as steps 7 to 10b.
- Application: `TriggerPipelineTest`, `ZoneChangeRulesEventTest`, `LegacyTriggerBindingsTest`, `RulesEventLogTest`,
  `ZoneChangeTriggerTest`, `TriggerConditionTest`, `LegacySlotTriggerTest`, `TriggeredAbilityTest`,
  `GrantedAbilitiesTest`, `CardAbilitiesTest`, `CardFreezeTest`, `CardImmutabilityArchTest`,
  `SimulationCopyCompletenessTest`, `GameSetupServiceAllRandomTest`, `CommanderGameTest`, `GameRegistrySessionTest`,
  `AuraAttachmentServiceTest`, `DepartureServiceOrphanedStepBatchTest`, `GameDataExileTrackingTest`,
  `TurnCleanupServiceTest`, `EnterTriggerCollectorServiceTest` and the mock-wired `PermanentRemovalServiceTest`,
  `BattlefieldPlacementServiceTest`, `DeathTriggerCollectorServiceTest`, `PlanechaseServiceTest`. 93 failures, the same
  tests as at HEAD (checked in a HEAD worktree): mock wiring, the two architecture tests' known ones, and
  `TurnCleanupServiceTest` / `EnterTriggerCollectorServiceTest`, which fail there too.
- The full suite was not run here.

### Step 12a

12a moved every zone-change slot but `ON_ENTER_BATTLEFIELD` to the trigger pipeline, in two parts (two commits). Part 1
moved the sacrifice, put-into-a-graveyard-from-the-battlefield, leaves-the-battlefield / exiled / returned-to-hand and
outside-the-battlefield families; part 2 the phasing / transform / turned-face-up family and the "another object
enters" family. `EffectSlot` keeps `ON_ENTER_BATTLEFIELD` for 12b and no other zone-change constant.

#### Part 1

**The bridge.** Each moved slot becomes a `ZoneChangeWatch` constant of the same name (engine, `service/trigger`),
listing the trigger conditions its cards were converted to (`Key`: trigger, zone the ability functions from,
multiplicity). `TriggeredAbilityStackService.triggerOnce` looks the watch up first (`ZoneChangeWatch.of`) and calls
`TriggerCollectionService.triggerZoneChange`, which builds the slot's old `TriggerContext` from the event and runs the
old collector's per-source body. Handlers moved from `@CollectsTrigger(slot = …)` to `@CollectsWatchTrigger(watch = …)`
(`scripts/step12a-rekey.py`); `TriggerCollectorRegistry` is keyed by `Object` (a slot or a watch).
`GameEngineConfig.registerTriggerCollectors` scans the new annotation.

**Relations.** A slot keeps the self-inclusion its collector had (the inventory's Self column); where a collector
judged it by the effect (`ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD`, `ON_ANY_CREATURE_EXILED_FROM_BATTLEFIELD`),
the body still does. Exception: the sacrifice family was classified by Oracle wording (`RELATION` in
`scripts/step12a-zone-change-triggers.py`), because its collector used a card-text heuristic ("sacrifices another").

**Families:** sacrifice, put into a graveyard from the battlefield (with step 11's three owner-keyed slots), leaves
the battlefield / exiled from it / returned to hand, moves outside the battlefield (graveyard, library, hand, exile,
command zone). Part 2 below moved the rest.

**Engine changes so far.**

- Domain: `ObjectRelation.YOU_OWN`, `ANOTHER_YOU_OWN`, `OPPONENT_OWNS`; `PlayerRelation`; `ZoneChangeTrigger` gains
  `excludedTo`, `causedBy`, `duringYourTurn`; `ZoneChangeEvent.causeControllerId` (from
  `GameData.resolvingControllerForRulesEvents`: the resolving controller or the parked resolution's, never while a
  spell's sacrifice cost is paid); `PermanentChange`, `PermanentChangeEvent`, `PermanentChangeTrigger` (not emitted
  yet); many `Triggers` factories and filter constants.
- `DepartureCause.STATE_BASED_SACRIFICE`: a Saga past its final chapter is sacrificed (CR 704.5s), so "whenever you
  sacrifice" sees it (Narci, Fable Singer).
- `TriggerIndex` lists face-up exiled cards' abilities that function from exile (haunting cards included).
- `RulesEventPipeline` adds the batch's dying cards to `graveyardLookBackExcludedCardIds` while it matches
  (CR 603.10a; Decaying Soil's threshold grant).
- `TriggerCollectionService.recordSacrifice` replaces `checkAllyPermanentSacrificedTriggers` (bookkeeping only);
  `harness.sacrifice(permanent)` sacrifices through the engine for card tests.
- Removed: the leave-batch watcher maps (`permanentLeaveBatchWatchers`, `…WatcherControllers`, `…PendingCreatures`,
  `…PendingPermanents`).
- Batches. `EffectResolutionService` opens one per resolving effect (CR 603.2c: one effect, one event). Moves a
  player makes after a choice, outside that scope, get their own: `GraveyardService.addCardsFromLibraryToGraveyard`,
  `resolvePutTopCardsIntoGraveyard`, `resolveMillPlayersSimultaneously`, the hand-to-library choice handler, and each
  `beginGraveyardLeaveBatch` in `SpellCastingService` now ends after the cards reach exile, not before. A multi-card
  discard cost paid one choice at a time keeps one batch open across the choices (`GameData.discardCostBatchOpen`,
  copied for simulations; CR 601.2h), replacing `deferOncePerBatchGraveyardTriggers` and the `deferredBatch*` fields.
- Cost-time triggers. The old `notify…` calls fired synchronously, so the stack-size windows that defer cost-time
  triggers (CR 603.3) wrapped them; the event now fires where the card moves, after the window. Activated-ability
  costs that move cards go through `AbilityActivationService.payCostDeferringTriggers` (one batch, then deferral).
  Spell costs already deferred everything paid before the spell goes on the stack.
- Isolated batches. `RulesEventLog.beginIsolatedBatch`/`endIsolatedBatch` open a batch of its own inside an open
  scope and hand the scope back after. The outermost `PermanentRemovalService.beginPermanentLeaveBatch` uses it, so
  a destroy-all inside a resolving effect is matched while `DepartureService`'s `simultaneousDying*` snapshots are
  still published (One with the Kami sees a creature's Equipment that died with it as making it modified).
- Look-back observers. A permanent that entered in the same batch is a new object (CR 400.7) and doesn't see an
  earlier departure of that batch (CR 603.10a): a flickered Imperial Cosmographer doesn't see itself leave.
- Entry suppression. `TriggerIndex.entryCausesNoTriggers` drops a batch's artifact or creature entering the
  battlefield while Hushbringer-style statics apply, so the whole zone change triggers nothing (Syr Konrad's "leaves
  your graveyard" half when a creature is reanimated); it replaces `GraveyardService.withoutSuppressedEntries`.
- Scroll Rack's set-aside cards are recorded as exiled from hand (`rulesEvents.arrived(card, player, EXILE)`), so
  "put into exile from your hand" sees them, and their return to the library is a move from exile.
- Owners. `RulesEventLog` remembers the player whose zone it last saw each card in and uses that owner when an event
  names none (test setups put cards straight into a zone), so `YOU_OWN` and `OPPONENT_OWNS` match.
- Removed from `GraveyardService` and `GameData`: every `graveyardLeaveNotificationPending*` collection,
  `graveyardExileNotificationPendingCounts`, `kayaExileNotification*`, `graveyardOrBattlefieldExileNotificationPending`,
  `notifyCardsExiledFromBattlefield`, and the per-type leave notifiers; `beginGraveyardLeaveBatch`/`end…` now only
  open and close a rules event batch (the depth counter stays as a guard).
- Still collected at the choke point, keyed by trigger: Takenuma (plane, `checkPlanarCreatureLeavesBattlefieldTriggers`),
  My Champion Stands Supreme (scheme, `checkSchemePermanentLeavesBattlefieldTriggers`), emblems' artifact-graveyard
  abilities (`checkEmblemArtifactGraveyardTriggers`).

**Behaviour changes to list:** Camellia, the Seedmiser triggers once for one or more Foods (CR 603.2c); Jaws,
Relentless Predator sees every destroy of a noncreature artifact, not only two removal paths; Kothophed's
"owned by another player" merged into "put into an opponent's graveyard" (CR 400.3); the exile-then-return-to-hand
handler no longer pretends a permanent was returned to hand; a commander bounce whose owner later chooses the command
zone still triggers "returned to hand" abilities (the departure's batch closes before the choice); The War Doctor's
exile half and The Heron Moon trigger once for one or more cards, as their Oracle text says (the old collector got
that from the exile listener's batching); Wan Shi Tong, All-Knowing and Dutiful Knowledge-Seeker no longer trigger on
a token put into a library (a token isn't a card, CR 111.1); Kaya, Spirits' Justice can choose a creature card exiled
from the battlefield as well as from the graveyard ("from among them"); Advanced Reconstruction's level-2 ability
triggers when a card is cast from your graveyard (the card leaves it for the stack) and goes on the stack above that
spell.

**For later steps.** `GraveyardService`'s private `addCardToGraveyard` overloads still carry
`suppressLibraryBatchTriggers`, `suppressLibraryMillTriggers` and `suppressOncePerBatchLandTriggers`, which nothing
reads any more; prune them in the cleanup step. Known gap: a simultaneous mill across players that pauses for one player's
replacement-order choice (The Water Crystal plus Bruvac) closes its batch at the pause, so a "one or more nonland cards
are milled" trigger fires for the cards milled before the pause and again for the rest. The old code held the trigger
until the journal replay; the pipeline needs a batch that survives the pause (the same holds for any effect that
stops for a choice mid-event).


#### Part 2: phasing, transforming, turning face up

Twelve constants are gone: `ON_SELF_PHASES_OUT`, `ON_ANY_OTHER_PERMANENT_PHASES_OUT`, `ON_SELF_PHASES_IN`,
`ON_TRANSFORM_TO_BACK_FACE`, `ON_TRANSFORM_TO_FRONT_FACE`, `ON_EQUIPPED_CREATURE_TRANSFORMS`, `ON_ALLY_PERMANENT_TRANSFORMS`,
`ON_TURNED_FACE_UP`, `ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP`, `ON_SELF_OR_ANY_PERMANENT_TURNS_FACE_UP`,
`ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP` and `GRAVEYARD_ON_ALLY_PERMANENT_TURNS_FACE_UP`. None of these is a zone change
(CR 702.26d); they moved with the family because step 12 lists them.

- **Events.** `PermanentChangeEvent` (part 1's type, now with the X paid to turn a permanent face up) is emitted at the
  choke points: `PhasingService` (a phase-out before the permanent leaves the battlefield list, with a copy, all of one
  phasing event in one batch; a phase-in after it is back), `AnimationSupport.transformed` (both directions and Olag's
  copy toggle), and `TurnFaceUpCopyService.turnedFaceUp`, which both face-up paths (`GameService.finishTurningFaceUp`,
  `TurnFaceUpCopyService.finishTurnFaceUp`) call where they collected triggers.
- **Matching.** `RulesEventPipeline.triggerPermanentChanges`: `TriggerIndex.permanentChangeCandidates` lists the
  battlefield after the event (the changed permanent included), a phased-out permanent's own abilities as it last
  existed (CR 603.10b), and graveyard cards; `TriggerMatcher.matchedPermanentChange` checks the change, zones, relation
  (`THIS`, the "you control" / "another" relations, `EQUIPPED` / `ENCHANTED`) and filter. "One or more" abilities trigger
  once for the batch first, then each event's observers and the changed permanent's own abilities.
- **Bridge.** The constants are `ZoneChangeWatch` entries (keyed by `PermanentChangeTrigger`);
  `TriggeredAbilityStackService.triggerPermanentChange` checks options and calls
  `TriggerCollectionService.triggerPermanentChange`, which runs each old collector's per-source body. The bodies of the
  source's own transform and face-up abilities moved out of the choke points: `AnimationSupport.putTransformAbilityOnStack`
  and `TurnFaceUpCopyService.putTurnedFaceUpAbilityOnStack` (GameService's version, which counted additional-trigger
  copies; the copy-choice path's narrower duplicate is gone).
- **Replacements out of the slot.** "As this is turned face up" (CR 614.1e: megamorph and negamorph counters, Vesuvan
  Shapeshifter's copy, Gift of Doom's Aura, the power/toughness form choice) are static abilities now, read from
  `getStaticEffects()`. "As this transforms into [this face]" is the new static `AsTransformsIntoThisEffect` (Sephiroth's
  emblem, Olag's copy), applied by `AnimationSupport.transformed` before the event.
- **Grouping.** The registrations the old collectors put on the stack as one object are one ability: seven face-up cards
  (Riptide Survivor, Rattleclaw Mystic, Maelstrom Djinn, Greenbelt Radical, Essence of Antiquity, Coveted Falcon with two
  target groups bound by `bindEffect`, Alley Assailant), Casal Pathbreaker Owlbear, Avacyn the Purifier and Huntmaster
  of the Fells' transform ability.
- **Behaviour changes:** a permanent that lost all its abilities no longer triggers its own transform or face-up ability
  (the old transform collector didn't check), nor an Equipment's "when equipped creature transforms"; a phase-out
  trigger is matched when the phasing event ends.
- **Cards** by `scripts/step12a-permanent-change-triggers.py` (185 files; Acid-Spewer Dragon and Aquamorph Entity are in
  its `MANUAL` table: a fully qualified megamorph replacement, and a P/T form choice already static for "as this enters").

#### Part 2: another object entering

The other 38 entering constants are gone (`ON_ALLY_CREATURE_ENTERS_BATTLEFIELD`, the creature, nontoken, artifact, token,
land, Equipment, enchantment, opponent, enchanted-player, "from a graveyard" / "from exile", "enters transformed" and
graveyard / command-zone variants, and `ON_ALLY_CREATURE_OR_ENCHANTMENT_ENTERS_BATTLEFIELD`, which only a spell's
registered ability used).

- **Relations.** Each slot keeps its collector's self-inclusion (`ZoneChangeWatch`): the "ally" slots
  `ANOTHER_YOU_CONTROL`, the "self or ally" slots `YOU_CONTROL`, `ON_ANY_OTHER_…` `ANOTHER`, landfall `YOU_CONTROL` with
  the collector's "a printed land doesn't see itself unless gated on the entering permanent" check left in its body.
  `scripts/step12a-enters-scan.py` lists every registration with its Oracle wording: 65 cards say "this or another" on an
  "another" slot and also have their own `ON_ENTER_BATTLEFIELD` copy of the ability, and 20 say "a [type] you control"
  where the collector excluded the source (Arcades, Brainstealer Dragon, Faramir, Pollywog Prodigy, the Trials, …).
  12b, which moves those cards' own enters abilities, is where they merge into one ability with the Oracle relation.
- **Matching at the old choke point.** An entry's batch closing doesn't match the entering abilities (except the
  token watches): `EtbTriggerService.collectEntersTriggers` calls `RulesEventPipeline.triggerEnteringObservers` as the
  entering permanent's own enters abilities are processed, which is where the old collectors ran. The entry event is
  the one the rules log still holds for the permanent (or one built from it once drained), with the permanent as it is
  then (`PermanentRemovalService.snapshotEffectivePermanentCard`), and the filters are judged on that copy. Matching at
  batch close was tried first and broke three things the old timing gets right: the mass-entry paths that place a
  group across several choices (Ghoul's Night Out, Another Round) and process its enters abilities once all of it is on
  the battlefield, so its members see one another; a permanent's "enters transformed" or "as it enters" state, which
  is set after placement (Corruption of Towashi); and the stack order, the entering permanent's own enters abilities
  below the ones its entry triggers.
- **Candidates.** For an entry `TriggerIndex.otherMoveCandidates` also lists the permanents that entered in the batch,
  for their enters abilities only (CR 603.6a: the newcomers are checked, the entering permanent itself included), and
  the cards in command zones. `TriggerMatcher` matches a newcomer's own entry through the observer path (never as the
  moving object): `THIS` and the self-inclusive relations for itself, the "another" relations never.
- **"One or more."** While `collectingSimultaneousCreatureEntryTriggers` marks an event, a "one or more" ability that
  already triggered for it names the next entrant on its stack objects instead of triggering again (the old
  collectors' mechanism, now keyed by the ability). One token creation sets it too (`PermanentControlSupport`), as the
  old token path grouped "one or more creatures" over a creation's tokens.
- **Bridge.** `TriggerCollectionService.triggerEntry` runs each old collector's per-source body (`ZoneChangeWatch.entering()`
  routes there). The token watches get the creation's whole batch (a per-token ability is `EACH`, and an effect with a
  resolution condition still triggers per token). The choke point keeps what isn't a card ability: evolve, watchers,
  boons, emblems, the abilities a spell registered until end of turn (keyed by trigger now), planes
  (`ZoneChangeWatch.effectsOf`) and exiled voyages; its stack window still taxes what the entry triggered (Strict
  Proctor). One token creation is one rules batch.
- **"Whenever you create one or more tokens"** isn't an enters trigger: a token's entry has the new zone-change cause
  `CREATE` (CR 701.7a; a token enters the battlefield only by being created), and `Triggers.tokensCreated` watches it, so
  Elesh Norn neither doubles nor suppresses it (Staff of the Storyteller, Akim the Soaring Wind).
- **"Enters from a graveyard"** is an enters ability, checked after the event (CR 603.6a), not a look-back
  leaves-the-graveyard one: `ZoneChangeTrigger.looksBack()` is false for a move to the battlefield, so
  `entersFrom(GRAVEYARD, …, THIS)` functions from the battlefield (Phyrexian Dragon Engine, Treacherous Pit-Dweller,
  Canoptek Tomb Sentinel).
- **A part 1 regression fixed.** The pipeline's additional-trigger count for a dying creature's own ability passed no
  trigger context, so a "creature you control with power 2 or less" doubler (Delney, Streetwise Lookout) didn't see the
  dying creature as its controller's; it now passes a creature-death context as the old self-death collector did.
  `DelneyStreetwiseLookoutTest` (two cases) and `ChiefOfTheWildsTest` (one) failed at part 1's commit and pass now.
- **Other engine changes.** `BecomeCopyOfTargetCreatureEffect.retainingItsAbility()` keeps the triggered ability holding
  the copy effect (Protean Thaumaturge); `BecomeCopyOfEnteringCreatureEffectHandler` re-adds the copy ability as an
  ability; the exile-until-leaves helper reads `ZoneChangeWatch.ON_ANY_CREATURE_ENTERS_BATTLEFIELD.effectsOf`.
- **Cards** by `scripts/step12a-enters-triggers.py` (967 files): the `TriggerMode`s became multiplicities, consecutive
  landfall and Curse registrations without `INDEPENDENT` merged into one ability (20 landfall cards), and `MANUAL` covers
  Down in the Valley, Protean Thaumaturge, Staff of the Storyteller and Akim.
- **Docs.** `scripts/step12a-docs.py` rewrote agent-docs and the skills for both part 2 families (387 lines); the face-up
  and as-transforms replacement rows were fixed by hand, and the skills describe the new triggers.

**For later steps.**

- 12b: the 65 "this or another" cards above merge their `ON_ENTER_BATTLEFIELD` copy into the ability with
  `THIS_OR_ANOTHER_YOU_CONTROL`; the 20 plain-wording cards need their relation checked. (12b deferred both to step
  14; see its Findings.) The source's own enters
  abilities and `triggerEnteringObservers` both run at `collectEntersTriggers`, so 12b can move them together and keep
  the old order (its own below the others').
- Cleanup: `TriggerCollectionService.checkAllyTokenEntersTriggers` only collects boons.
- Step 14: the wrappers the entry bodies read (`TriggeringCardConditionalEffect`, `EnterCreatureConditionalEffect`,
  `OncePerTurnTriggerEffect`, the intervening "if"s) and landfall's self-exclusion become filters and options.

**Tests changed.**

- `EnterTriggerCollectorServiceTest` (mock-wired): drives the entry bodies through `triggerZoneChange`, doing the
  pipeline's relation and type match itself; the remaining choke-point collectors are still called where the old one
  did both. Three of its cases failed before on test bugs (an unstubbed toughness query, a predicate stubbed on the card
  rather than its effective characteristics, an outdated assertion on Sigil Captain's re-checked condition) and are
  fixed; the Naban case stubs the additional-trigger count the body now computes.
- `TriggerPipelineTest`: a "creature you control enters" ability triggers for its own entry and an "another" one doesn't
  (CR 603.6a); a "one or more tokens" ability triggers once for Raise the Alarm's two tokens (CR 603.2c).
- `ValleyMightcallerTest`, `IchorplateGolemTest`, `HonoredDreyleaderTest` put a permanent on the battlefield and then
  called an old entry collector directly; they call the new `GameTestHarness.announceEntry`, which emits the entry
  event and matches it as the engine does.

**Test runs.**

- Card, permanent-change family: the converted cards and every card that morphs, disguises, manifests, cloaks,
  phases or transforms (1,243 classes): 3 failures at first (two replacements the script missed), then none.
- Card, both families: the converted cards, every card test that constructs one, and the selection above (2,914
  classes, 24,947 tests). The first runs failed in 36 classes, from the causes this section describes (entries
  matched at batch close, "enters from a graveyard" looking back, the token-creation trigger, the slot-style dispatch
  of "enters transformed", tests calling the old collectors) plus the part 1 regression; the final run had no failure.
  An earlier run also showed ~100 Spring start-up errors caused by a second Gradle build rebuilding a card jar under
  the running tests, not by the change.
- Application: the pipeline, rules-event, trigger-model, copy and freeze tests, `EnterTriggerCollectorServiceTest`, and
  every test of a changed service (`EtbTriggerServiceTest`, `BattlefieldPlacementServiceTest`, `PermanentRemovalServiceTest`,
  `StepTriggerServiceTest`, `TurnCleanupServiceTest`, `GameLifecycleEventSequenceTest`, `CombatDecisionRetryEventTest`,
  …; 1,400 tests). The failures are the same tests that fail at HEAD (checked in a HEAD worktree): mock wiring in the
  mock-wired classes and the known ones in the two architecture tests.
- AI: the whole module (1,620 tests), the same 10 failures as steps 7 to 11.
- The full suite was not run here.

### Step 12b

`ON_ENTER_BATTLEFIELD` is gone from `EffectSlot`, and with it the last zone-change constant: step 12 is done. Every
"when this enters" ability is a triggered ability with the real trigger `Triggers.enters(ObjectRelation.THIS)`
(`enters(null, THIS)`, any zone, any cause).

**Authoring.**

- `addTrigger(Triggers.enters(ObjectRelation.THIS), effect)`, on the card or a target group
  (`target(filter).addTrigger(…)`); grants take `new TriggeredAbility(Triggers.enters(ObjectRelation.THIS), effect)`.
- New: `Triggers.enters(ObjectRelation)`; `Card.addTrigger(Trigger, TriggerMode, CardEffect...)` and
  `SpellTarget.addTrigger(Trigger, TriggerMode, CardEffect...)` for the 77 `INDEPENDENT` registrations;
  `Card.addSimultaneousTrigger(Trigger, CardEffect...)` for the 6 `addSimultaneousEffects` ones;
  `Card.getTriggerEffectRegistrations(Trigger)` (registrations with their modes); `Permanent.getGrantedTriggerEffects(Trigger)`
  (temporary, then persistent abilities); `GrantedTriggeredAbilitySupport.grantedTriggerEffects` /
  `TriggerCollectionService.grantedTriggerEffects` (abilities other permanents' statics grant).
- `Card.getTriggerEffects(Trigger)` and the new registrations view read a per-trigger index built with the card's
  slot index (derived, rebuilt when the abilities change; no new copied state). `isSimultaneousWithPrevious` now also
  walks the abilities with a real trigger.
- `TriggerMode.INDEPENDENT` stays on the registration, not in `TriggerOptions`: it isn't a multiplicity. The enters
  collector still pools a card's plain "when this enters" abilities into one stack object that shares its targets,
  as the slot collector did, and `INDEPENDENT` keeps one its own. Per CR 603.3 every ability is its own stack object;
  pooling is the old collector's grouping, which step 15 (or whichever step puts the own enters abilities through
  `TriggeredAbilityStackService`) replaces by merging what the card text says is one ability and dropping the mode.

**The engine.**

- The own enters abilities are still put on the stack where the slot collector ran,
  `EtbTriggerService.processCreatureETBEffects`, now reading them by trigger: the card's
  (`getTriggerEffects`/`getTriggerEffectRegistrations`), the ones granted for a duration (`getGrantedTriggerEffects`),
  the perpetual entry grants (`perpetualEnterEffectsByCardId`, unchanged) and those other permanents grant. Its
  grouping, the may/modal/evoke/graveyard-target splits, `EtbEffectResolver` and the suppression checks are unchanged.
  They are put on the stack before the abilities that see the permanent enter, as before.
- `RulesEventPipeline.triggerEnteringObservers` skips the `enters(THIS)` trigger (CR 603.6a makes the newcomer a
  candidate, and `TriggerMatcher` would match its own entry), and batch-close matching already skips every enters
  trigger but the token watches. The moving-object path never matches an enters trigger. So the own abilities reach
  the stack once, only through `EtbTriggerService`.
- The 57 engine, 23 AI, 2 networking and 4 test-fixture readers of the slot read the trigger
  (`scripts/step12b-readers.py`); the writers (evoke's `SacrificeSelfIfEvokedEffect`, offspring and blitz grants,
  Specialize, the perpetual "becomes an Island" draw, the conjured duplicate's drain, the hand-cast offspring copy)
  add the trigger by hand. `PermanentRemovalService`'s "returns through a leaves trigger" check reads the enters,
  trains and turned-face-up effects.
- `PermanentCopierService` (a copy of a permanent with chosen independent modes) also rebuilds the
  mode-conditional effects of abilities with a real trigger.
- `GrantTriggeredAbilityEffectHandler`'s per-grant once-each-turn key rebuilt the grant from its slot, which a real
  trigger doesn't have; it keeps the ability's trigger now (`LegacySlots.withEffect`).
- `TokenCopySupport`: a token copy's added triggered abilities are objects of their own (CR 603.3). A slot ability was
  added `INDEPENDENT`; a real-trigger one was added as it was, so an added "when this enters" ability joined the copied
  card's own pooled object. It is added with `INDEPENDENT` registrations now (Aggressive Biomancy, below).

**Cards.** `scripts/step12b-enters-triggers.py` converted 4,783 card classes, 3 card tests, 13 application tests,
1 AI test and the `DeclaredEtbTargets` fixture: `addEffect`, chained target-group `addEffect`, `addSimultaneousEffects`,
`LegacySlots.triggered` (nested ones first: Jewel Mine Overseer conjures a card with an enters ability),
`GrantTriggeredAbilityEffect`, `GrantTriggeredAbilityToCastSpellEffect`, and `getEffects`/`getEffectRegistrations` in
tests, one ability per registration, so the collector sees the same registrations in the same order. `MANUAL`:
Huntmaster of the Fells (a helper that took the slot; its two effects are now one ability, the object the collector
made of them), Mirkwood Elk (a helper shared with `ON_ATTACK`), Rope-Line Attendant (a perpetual hand grant inside the
enters registration). `TriggeredAbilityTest` used the slot as an arbitrary legacy slot and uses
`ON_SELF_BECOMES_UNTAPPED` now. `scripts/step12b-docs.py` rewrote agent-docs and the skills (820 lines);
`ARCHITECTURE.md` and the implement-card skill describe the pooling and `INDEPENDENT` by hand.

**Not done here, for step 14.** 12a's Findings left 12b the "this or another" cards: 60 cards say "this
[permanent] or another [type] you control enters" and still have an `enters(THIS)` copy beside an `ANOTHER_YOU_CONTROL`
ability (`scripts/step12b-self-or-another-scan.py` lists them with their wording; 31 have no restriction beyond the
trigger's filter and the same effect on both halves). Merging them into one `THIS_OR_ANOTHER_YOU_CONTROL` ability was
tried and reverted: the merged ability is collected by the self-or-ally watches, whose `@CollectsWatchTrigger`
handlers are keyed by watch and effect class, so the effects the ally watches bind (Verdant Sun's Avatar's
toughness-based life gain) lost their handler, and the source's own entry took the other path's target flow; 37 tests
in 13 classes failed. The two halves still behave as the one Oracle ability (one entry can't meet both), as step 11's
split death cards do. Step 14, which replaces the wrappers and the per-watch bindings, merges them with step 11's 32,
and checks the "plain wording" cards 12a listed whose "another" relation excludes a source that could match.

**For later steps.**

- Step 14/15: the own enters abilities are the last zone-change abilities collected outside
  `TriggeredAbilityStackService`; they read their wrappers in `EtbEffectResolver` and `processCreatureETBEffects`
  (`OncePerTurnTriggerEffect`, kicked/cast/evoked/prowl intervening ifs, `ChooseOneAtTriggerTimeEffect`).
- Dash's `ReturnSelfToHandIfDashCostPaidEffect` and blitz's haste/sacrifice riders (step 8's markers) are now
  `enters(THIS)` abilities that `EtbEffectResolver` drops; the keyword family that moves them must keep reading them
  from the trigger (`CardChoiceHandlerService` adds the granted blitz riders the same way).
- Engine and AI code checks `getTriggerEffects(Triggers.enters(ObjectRelation.THIS))` where it checked the slot
  (modal enters abilities, targets, AI value). Once the abilities are grouped by ability, these should read abilities.

**Tests changed.**

- `TriggerPipelineTest`: two new tests, a "when this enters" ability triggers once for its own entry and not for
  another permanent's (CR 603.6a, the observer pipeline skips it), and a card's plain enters abilities share one stack
  object while an `INDEPENDENT` one is its own.
- `TriggeredAbilityTest`: the arbitrary second legacy slot is `ON_SELF_BECOMES_UNTAPPED`.
- By script: `CardAbilitiesTest`, `ETBTokenTargetServiceTest`, `EtbTriggerServiceTest`, `BattlefieldPlacementServiceTest`,
  `SpellCastingServiceTest`, `TargetLegalityServiceTest`, `ValidTargetServiceTest`, `StackEntryTargetGroupsTest`,
  `CardViewFactoryTest`, `CreatureControlServiceTest`, `InstantCategoryClassifierTest`, `TargetPolarityGuardTest`,
  `TheBookOfVileDarknessTest`, `EnduranceTest`, `CacklingCounterpartTest` add or read the trigger instead of the slot.

**Test runs.**

- Card: every test class of a converted card and every card test that constructs one (7,695 classes): 4 failures. Aggressive Biomancy's copied Merchant of Secrets lost
  its draw with the fight target (the `TokenCopySupport` cause above, fixed). Blood Age Muster's
  `canTriggerAgainOnALaterTurn` conjures a random spellbook card and passes on a rerun. Wildgrove Summoner's
  `seekingAnimatedForestsTriggersCardsPutIntoHandFromLibrary` and Signature Spells'
  `seekingBeforeExilingTriggersCardsPutIntoHandFromLibraryAbilities` fail the same way at HEAD (`2f7a5cdd00`, checked
  with the change stashed): a 12a regression in the "put into your hand from your library" watch for seek, left for a
  separate fix. After the fix, the token-copy cards' and the 31 merge candidates' classes and every test constructing
  them (441 classes) pass.
- Application: the pipeline, model, copy, freeze and architecture tests and every changed or mock-wired service test
  (85 classes, 1,185 tests): 111 failures, exactly the tests that fail at HEAD (same classes run with the change
  stashed): mock wiring and the known architecture/simulation-copy ones.
- AI: the whole module (1,620 tests), the same 10 failures as steps 7 to 12a.
- The full suite was not run here.

### Step 13

Step 13 is repeatable: each run moves one family of its table. Run 1 moved family 1, run 2 family 2, run 3 family 3,
run 4 family 4, run 5 family 5, run 6 family 6, run 7 family 7, run 8 family 8, run 9 family 9, run 10 family 10, run 11a the player and keyword actions of family 11, and run 11b the rest of family 11. No trigger constant is left, so the step is done.

#### Run 1: steps and phases

The 34 step and phase constants are gone: `UPKEEP_TRIGGERED` and its command-zone, graveyard, each, suspended,
exiled, scream-counter, opponent, enchanted-player and enchanted-permanent-controller forms; `DRAW_TRIGGERED`,
`EACH_DRAW_TRIGGERED` and the two enchanted draw forms; `END_STEP_TRIGGERED`, `CONTROLLER_END_STEP_TRIGGERED` and their
graveyard, command-zone, opponent and enchanted forms; the four beginning-of-combat slots; `END_OF_COMBAT_TRIGGERED`;
the three precombat main slots, `POSTCOMBAT_MAIN_TRIGGERED` and `SECOND_MAIN_PHASE_TRIGGERED`. Family 2 (spells and
abilities) is next; families 2 to 11 are left.

**Authoring.**

- `model.trigger.StepTrigger(Moment moment, ActivePlayer player)`: "at the beginning of [player's] [step or phase]"
  (**CR 603.2b**, checked). `Moment` is `UPKEEP`, `DRAW_STEP`, `PRECOMBAT_MAIN_PHASE`, `BEGINNING_OF_COMBAT`,
  `END_OF_COMBAT`, `POSTCOMBAT_MAIN_PHASE` (every one), `SECOND_MAIN_PHASE` (the turn's second main phase only,
  **CR 505.1** checked) and `END_STEP`.
- `model.trigger.ActivePlayer`: `YOU`, `ANY`, `OPPONENT`, `ENCHANTED_PLAYER`, `ENCHANTED_PERMANENT_CONTROLLER`. It is
  its own enum rather than `trigger.PlayerRelation`, whose simple name clashes with `filter.PlayerRelation`, which
  many of these cards already import for their target filters.
- `Triggers.upkeep`, `drawStep`, `precombatMainPhase`, `beginningOfCombat`, `endOfCombat()`, `postcombatMainPhase`,
  `secondMainPhase`, `endStep`. The zone is the condition's: `addTrigger(new TriggerCondition(Triggers.upkeep(
  ActivePlayer.YOU), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT, effect)` for the old `GRAVEYARD_`, `EXILED_` and
  `COMMAND_ZONE_` forms.
- Readers: `Card.getTriggerEffects(Trigger, Zone)` / `getTriggerEffectRegistrations(Trigger, Zone)` (the abilities
  with that trigger as a condition that functions in the zone), `TriggeredAbility.hasTrigger(Trigger, Zone)`,
  `Permanent.getTemporaryTriggerEffects` / `getPersistentTriggerEffects(Trigger)`,
  `GameData.perpetualTriggerEffects(UUID, Trigger, Zone)`, `GameQueryService.getEffectiveGraveyardTriggerEffects`,
  `PlanechaseService.step(GameData, StepTrigger...)` and `trigger(…, Trigger, …)`.
- The copy effects that named a retained slot: `BecomeCopyOfTargetCreaturePermanentlyEffect` and
  `ExileTargetCreatureCardFromGraveyardAndBecomeCopyEffect` take a `Trigger` now (a `LegacySlotTrigger` keeps the old
  registration path: Sunfrill Imitator's `ON_ATTACK`); `new BecomeCopyOfTargetCreatureEffect()` keeps the ability
  that holds it (`retainedEffectSlot` `null`), as `retainingItsAbility()` does.
- `PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(Ability)` is new; `GrantEffectToTargetEffectHandler`'s "if
  it doesn't have that ability" check reads abilities with a real trigger (Musician, Balduvian Shaman).

**No rules event, no index.** A step beginning is not a game action and has no batch, so this run added no
`RulesEvent` kind and no `TriggerIndex` candidates for it. As 12b did for the source's own enters abilities,
`StepTriggerService` still collects the abilities where the slot collectors ran, now reading them by trigger and zone,
with every step's old grouping, target routing, wrapper checks, face-down and ability-loss checks (including the
inconsistencies the step 1 inventory lists) unchanged. A later run or step 16 decides whether step beginnings become
rules events once `TriggerContext` goes.

**Where two slots met in one trigger**, the collectors are told apart without a slot:

- All Hallow's Eve (`EXILED_SCREAM_COUNTER_UPKEEP_TRIGGERED`) and Tawnos Endures / Darigaaz's conjured card
  (`EXILED_UPKEEP_TRIGGERED`) are both `upkeep(YOU)` from exile. The scream-counter loop takes the card's
  `RemoveScreamCounterFromExiledCardEffect` while it has a scream counter; the exile loop skips that effect.
- Strangling Grasp (`ENCHANTED_PERMANENT_CONTROLLER_AFFECTED_UPKEEP_TRIGGERED`) is "at the beginning of your upkeep",
  `upkeep(YOU)`. `StepTriggerService.affectsEnchantedPermanentController` routes an Aura's `upkeep(YOU)` effect that is
  bound to its enchant target and names a player to the Aura loop, which bakes in the enchanted permanent's
  controller; no other Aura's "your upkeep" effect matches (checked over every Aura with such an ability).
- `ENCHANTED_PLAYER_END_STEP_TRIGGERED` fired at every end step. Curse of Obsession and Tenuous Truce say "enchanted
  player's end step" and are `endStep(ENCHANTED_PLAYER)` (their `TargetPlayerIsActive` wrapper is now always true;
  step 14 drops it). Fraying Sanity says "each end step" and is `endStep(ANY)`: a Curse's `endStep(ANY)` ability is
  collected by the Curse loop (enchanted player as `targetId`), and the generic end-step loop skips Curses. Fraying
  Sanity is the only Curse with one.

**Zones kept as they were.** Face-up schemes and planes still author their step abilities with the battlefield
default; their readers present them as permanents (schemes) or read them zone-agnostically (planes), as before. The
eminence read of the active player's command zone (`upkeep`/`beginningOfCombat(YOU)` from a non-scheme command-zone
card) still reads battlefield abilities, the surprise step 1 recorded. Agyrem's death trigger (step 11) already
functions from `AbilityZones.COMMAND`; the step that moves schemes and planes onto the index should do the same for
these. Only Oloro (`upkeep(YOU)`) and Dark Wings Bring Your Downfall (`endStep(ANY)`) function from the command zone.

**Cards.** `scripts/step13-step-triggers.py` converted 2,315 card classes, 7 card tests, 4 application tests
(`StepTriggerServiceTest`, `PlanechaseServiceTest`, `LibraryChoiceHandlerServiceTest`, `GrantedAbilitiesTest`) and one
AI test (`GameDataDeepCopyTest`): `addEffect` (on the card and on target groups, keeping `INDEPENDENT`), `addSimultaneousEffects`,
`LegacySlots.triggered`, the grant constructors (`GrantTriggeredAbilityEffect`, `GrantEffectToTargetEffect`,
`PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect`, `GrantTriggeredAbilityToCreatedPermanentsEffect`,
`GrantEffectToSourceEffect`), the copy effects' retained slot, comments naming a slot, and in tests the slot getters,
the planar helpers, `addPersistentTriggeredEffect` and mocked `grantedTriggeredEffects`. One ability per
registration, so the collectors see the same effects in the same order. `MANUAL`: Vesuvan Doppelganger (its copy keeps
its ability), Goblin Trapfinder (the seek grant), Fraying Sanity (`endStep(ANY)`). By hand: Sunfrill Imitator, the
`CliffsideMarketTest` and `PlanechaseServiceTest` helpers, `TargetPolarityGuardTest`, and the arbitrary legacy slots of
`TriggeredAbilityTest`, `TriggerConditionTest`, `LegacySlotTriggerTest` and `GrantedAbilitiesTest`.
`LegacySlotTrigger` no longer knows an `EXILED_` prefix (no slot has one). `scripts/step13-docs.py` rewrote agent-docs
(1,085 lines); the Fraying Sanity and Curse rows, `ARCHITECTURE.md` and the implement-card and review-card skills were
written by hand. The engine's other readers and writers of the slots (grants, Specialize, conjure, clone,
cumulative upkeep, the Oath-style target check, the conspiracy marker in `GameSetupService`, …) read or add the trigger.

**For later runs.**

- Step 14: the step bodies still read the wrappers (`ConditionalEffect` intervening "if"s, `OnceOnlyTriggerEffect`,
  `SurvivalTriggerEffect` for "second main phase" kept on `postcombatMainPhase`, `MonarchEndStepTriggeredEffect`,
  `OpponentDrawStepOnlyEffect`, `UpkeepPlayerDependentEffect`, …) and the "chosen player" upkeep of
  `RememberTargetPlayerEffect` / `ChooseOpponentOnEnterEffect` cards.
- Step 15: Strangling Grasp's and the Curses' "that player" is still baked as `targetId` by the collectors; effects
  that read the enchanted player or enchanted permanent's controller from the event would retire
  `affectsEnchantedPermanentController` and the Curse routing.
- Sagas (family 10) still use `SAGA_CHAPTER_*`, read in `StepTriggerService.handleSagaLoreCounters`.

**Tests changed.**

- New `StepTriggerTest` (`model/trigger/`): default zones, the factories' moments and players, null rejection, a card's
  abilities of one trigger read by zone (battlefield, graveyard, none from exile, all without a zone), and a perpetual
  grant read by its zone.
- `AllHallowsEveTest#doesNotTriggerWhenExiledWithoutScreamCounters` (new): the merged exile-upkeep read doesn't fire
  its scream-counter ability without a counter.
- `StepTriggerServiceTest` (mock-wired): its setup stubs `getEffectiveGraveyardTriggerEffects` instead of the slot
  reader (the eight graveyard step tests failed without it); stubs of `grantedTriggeredEffects` became
  `grantedTriggerEffects`, and display names name the triggers.
- By script: the seven card tests and four application tests named above, and `GameDataDeepCopyTest`.
- `CliffsideMarketTest` has a trigger overload of its life-exchange helper, `PlanechaseServiceTest` a trigger fixture.
- `TriggeredAbilityTest`, `TriggerConditionTest`, `LegacySlotTriggerTest` and `GrantedAbilitiesTest` used deleted
  slots as arbitrary graveyard, command-zone, exiled or upkeep slots; they use `GRAVEYARD_ON_CONTROLLER_CASTS_SPELL`,
  `COMMAND_ZONE_ON_ALLY_CREATURES_ATTACK` and `ON_SELF_BECOMES_UNTAPPED` now (`LegacySlotTriggerTest`'s `EXILED_` row
  is gone). `LegacySlotTriggerTest` runs 35 fewer cases: it is parameterised over `EffectSlot.values()`.
- `TargetPolarityGuardTest` grants the ability instead of a slot and an effect.

**Test runs.**

- Card: every test class of a converted card and every card test that uses one (3,688 classes, 33,455 tests): one
  failure, `SignatureSpellsTest#seekingBeforeExilingTriggersCardsPutIntoHandFromLibraryAbilities`, which 12b recorded
  as failing at HEAD (the 12a seek regression).
- Application: the step, planar, grant, trigger-model, pipeline, copy, freeze, architecture, setup and turn-cleanup
  test classes (20 classes, 738 tests): 31 failures, the same tests that fail at HEAD (`a8de626acf`, run in a
  worktree): mock wiring in `StepTriggerServiceTest`, `TurnCleanupServiceTest` and `PlanechaseServiceTest`, and the
  known ones in the two architecture tests and `CardEffectTargetingConsistencyTest`.
- AI: the whole module (1,620 tests), the same 10 failures as steps 7 to 12b; `TargetPolarityGuardTest`'s two also
  fail at HEAD.
- `:magical-vibes-webservice:compileTestJava` fails at HEAD too (`CardBrowserServiceTest`'s `CardCatalog` lacks
  `findLoadedPrintingByName`); unrelated.
- The full suite was not run here.

#### Follow-up: failing card tests after run 1

The full card suite after run 1 had ten failing classes, most of them failing since step 12a or 12b. Fixed:

- **Exiled "until".** 12a's one batch per resolving effect made the cards a cascade, discover or "exile cards from
  the top of your library until …" loop exiles one occurrence, so a "one or more cards are put into exile" ability
  (Laelia, the Blade Reforged; The War Doctor) triggered once. Such cards are exiled one at a time (Gatherer ruling of
  2024-06-07). New `RulesEventLog.separately(Runnable)` runs a move as an isolated batch;
  `scripts/step13-exile-until-separately.py` wraps the exile in the 34 "until" handlers' loops (cascade, discover,
  Unpredictable Cyclone's draw replacement, the `ExileTopUntil…` / `ExileUntil…` / `RevealUntil…` family, Ensnared by
  the Mara, Tibalt's Trickery, Possibility Storm, …). "Exile the top N cards" stays one event. Daring Discovery,
  Contest of Claws, Ryan Sinclair, Ensnared by the Mara.
- **Ante.** Anteing puts a card into the ante zone (**CR 407.4**, checked), which the engine keeps in the exile list.
  `addToAnte` marks the card first and the exile list only tracks an anted card's arrival, so "put into exile"
  abilities don't see it. Amulet of Quoz.
- **Seek then exile / seek onto the battlefield.** Seek puts the card into its owner's hand first;
  `SeekLibraryEffectHandler` now adds it to the hand before exiling it (Signature Spells) or putting it onto the
  battlefield (Wildgrove Summoner), so "put into your hand from your library" abilities (Kithkin Brinefarer, Volatile
  Rift) trigger. 12b recorded these two as failing at HEAD.
- **A watcher that left in the same event names its card.** `TriggerIndex.lookBackCandidates` names the physical card
  for a departed permanent's printed abilities, as `departedPermanentCandidates` does, and the zone-change bridge
  (`TriggerCollectionService.namedSourcePermanent`) gives the old bodies a source permanent with that card, so their
  stack entries no longer carry the last-known layered copy. Words of Wind with Warped Devotion.
- **Test flakes.** `GlacialPlatingTest`'s insufficient-snow-mana case empties both hands (a playable card in the random
  opening hand kept the upkeep open); `BloodAgeMusterTest#canTriggerAgainOnALaterTurn` passes with
  `passUntilWithNoAttackers` (the random conjure can be Charging Strifeknight, which has haste). Each passed 60
  repetitions.

Runs: the ten classes (with `--rerun`), and every card test of a card with another object's zone-change trigger,
seek, ante, cascade, discover or a changed handler, or using one (1,614 classes, 14,034 tests): all passing.
Application: `TriggerPipelineTest`, `RulesEventLogTest`, `ZoneChangeRulesEventTest`, `GameDataExileTrackingTest`,
`LegacyTriggerBindingsTest`: passing; `SimulationCopyCompletenessTest` fails as at HEAD.

#### Run 2: spells and abilities

The 24 spell, land-play and activation constants are gone: `ON_ANY_PLAYER_CASTS_SPELL`, `ON_CONTROLLER_CASTS_SPELL`,
`ON_OPPONENT_CASTS_SPELL` and their graveyard, hand, exile and command-zone forms; `ON_SELF_CAST`; the two copy, two
counter and `ON_SELF_SPELL_COUNTERED_OR_FIZZLED` slots; the three land-play slots; the six activation slots and the
graveyard exhaust form; `ON_SELF_MANA_ABILITY_RESOLVES`. Family 3 (combat) is next; families 3 to 11 are left.

**Authoring.**

- `model.trigger.SpellTrigger(SpellEvent event, PlayerRelation player)`: a spell is cast (**CR 601.2i**, checked),
  copied (**CR 707.10**, checked) or countered (**CR 701.6a**, checked). `SpellEvent` is `CAST`, `CAST_THIS`, `COPIED`,
  `COUNTERS` ("you counter a spell"), `COUNTERED` ("a spell you control is countered") and `THIS_COUNTERED_OR_FIZZLES`.
  The two "this spell" events function from the stack by default.
- `model.trigger.LandPlayTrigger(ObjectRelation relation)`: a land is played, a special action (**CR 116.2a**,
  checked). `YOU_CONTROL` includes the source, `ANOTHER_YOU_CONTROL` doesn't, `OPPONENT_CONTROLS`.
- `model.trigger.AbilityTrigger(AbilityEvent event, PlayerRelation player)`: `ACTIVATED` (mana abilities included),
  `NONMANA_ACTIVATED`, `EXHAUST_ACTIVATED`, `ETERNALIZE_OR_EMBALM_ACTIVATED`, `THIS_MANA_ABILITY_RESOLVES`.
- Named factories, so card classes import no relation enum (`trigger.PlayerRelation` clashes with
  `filter.PlayerRelation`, as run 1 found): `Triggers.youCastSpell()`, `opponentCastsSpell()`, `playerCastsSpell()`,
  `youCastThisSpell()`, `youCopySpell()`, `opponentCopiesSpell()`, `youCounterSpell()`, `yourSpellCountered()`,
  `thisSpellCounteredOrFizzles()`, `youPlayLand()`, `youPlayAnotherLand()`, `opponentPlaysLand()`, `youActivateAbility()`,
  `opponentActivatesAbility()`, `youActivateNonManaAbility()`, `opponentActivatesNonManaAbility()`,
  `youActivateExhaustAbility()`, `youActivateEternalizeOrEmbalm()`, `thisManaAbilityResolves()`. Another zone is the
  condition's, as in run 1.
- The spell filter and the "first / Nth spell", kicked, mana-spent and triggering-permanent conditions stay in the
  effects (`SpellCastTriggerEffect`, `NthSpellCastTriggerEffect`, `FirstSpellCastTriggerEffect`, the `Triggering…`
  wrappers, …) for step 14.
- Grants that took a slot take the ability or the trigger: `RemovePerpetualTriggeredAbilityEffect` holds a `Trigger`
  (a `LegacySlotTrigger` for a slot, Pass the Torch); the four perpetual hand grants have a `(Trigger, effects, filter)`
  constructor, and their handlers and `PendingInteraction`s grant the abilities (`PerpetualTargetCardChoice` carries
  `List<Ability>`, and the random grant joins several effects into one ability); `BecomeCopyOfTargetCreatureUntilEndOfTurnEffect`
  keeps a `retainedTrigger` (Hulkling); Artisan of Forms uses `BecomeCopyOfTargetCreatureEffect.retainingItsAbility()`,
  which now also finds the copy effect inside a `SpellCastTriggerEffect`. `GameData.removePerpetualAbilitiesIf` and
  `Permanent.removePersistentTriggeredEffectsIf` take a `Trigger`; `RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffectHandler`
  keeps the granted ability, as the end-of-turn one has since step 11.

**No rules event, no index.** As in run 1, the old collectors still run at their choke points
(`checkSpellCastTriggers`, `checkSpellCopyTriggers`, the counter, land-play, activation and mana-resolution checks).
`service/trigger/SpellAbilityWatch` has one constant per old slot, naming its trigger and the zone the collector reads
(`effectsOf`, `registrationsOf`); `TriggerCollectionService.dispatchWatch` is `dispatchSlot` for a watch (printed
abilities by trigger and zone, granted ones through the new static-bonus form of
`GrantedTriggeredAbilitySupport.grantedTriggerEffects`). The 134 handler registrations moved from `@CollectsTrigger(slot = …)` to the
new `@CollectsSpellAbilityTrigger(watch = …)` (`scripts/step13-spell-rekey.py`); `TriggerCollectorRegistry` and
`GameEngineConfig` scan it. Temporary global abilities are matched with `ability().hasTrigger(…)`. Grouping, target
routing, wrapper checks and ability-loss checks are unchanged, so the step makes no rules change. Shadow mode was not run,
for run 1's reason: the same collector bodies put the same abilities on the stack, and the card tests check the
choice of abilities.

**Cards.** `scripts/step13-spell-triggers.py` (run 1's script with this family's `SLOTS`, the registration and grant
heads and the perpetual readers) converted 1,526 card classes and 2 card tests, one ability per registration in the
old order (4 `ON_SELF_CAST` registrations keep `INDEPENDENT`). `MANUAL`: Artisan of Forms. The engine's other readers
and writers (the cascade predicate, `EffectResolution`'s mana-spent scan, the trigger-order label, Gale's
specialization, the perpetual "when you cast this spell" grants, the subtype-choice activation copy) read or add the
trigger. `LegacySlotTrigger` no longer knows a `HAND_` prefix (no slot has one). `scripts/step13-spell-docs.py` rewrote
agent-docs (583 lines; "slot" after a converted trigger reads "ability"); the implement-card and review-card skills and
`ARCHITECTURE.md` were written by hand.

**For later runs.**

- Step 14: the cast collectors still read `SpellCastTriggerEffect`'s filter and the `First…` / `Nth…` /
  `Kicked…SpellCastTriggerEffect` conditions, and the activation collectors the `Triggering…AbilityConditionalEffect`
  wrappers; those become trigger filters and options there.
- Schemes and planes still author these abilities with the battlefield default and are read as before (run 1's note).
- `grantedTriggerEffects` returns every effect of a granted ability, where the slot form returned the first; no grant
  of this family has more than one.

**Tests changed.**

- New `SpellAbilityTriggerTest` (`model/trigger/`): default zones (stack for "this spell"), the factories' events and
  players, land-play relations, null rejection, a card's cast abilities read by zone, and removing a perpetual
  "when you cast this spell" ability by its trigger.
- `SpellCastTriggerCollectorServiceTest` and `TriggerCollectorRegistryTest` dispatch to the watches instead of the slots.
- `GrantedAbilitiesTest`: the perpetual grants and the hand grant use `Triggers.youCastThisSpell()`.
- `TriggeredAbilityTest`, `LegacySlotTriggerTest` and `GameDataDeepCopyTest` used deleted slots as arbitrary legacy
  slots: they use `GRAVEYARD_ON_CONTROLLER_SURVEILS`, `EXILE_ON_ALLY_CREATURES_ATTACK` and `ON_SELF_BECOMES_UNTAPPED`;
  `LegacySlotTriggerTest`'s `HAND_` row is gone.
- By script: `MappingTheMazeTest` and `LurkingSpinecrawlerTest` read `perpetualTriggerEffects`.

**Test runs.**

- Card: every test class of a converted card and every card test that constructs one (2,346 classes, 20,508 tests):
  all passing.
- Application: the trigger-model, registry, collector, pipeline, planar, freeze, architecture, simulation-copy and setup
  classes (531 tests): 9 failures, the same tests that fail at HEAD (`4fae14813d`, run in a worktree):
  `SpellCastTriggerCollectorServiceTest` (4, mock wiring), `PlanechaseServiceTest` (2), and the known ones in
  `CardImmutabilityArchTest`, `SimulationCopyCompletenessTest` and `CardEffectTargetingConsistencyTest`.
- AI: the whole module (1,620 tests), the same 10 failures as steps 7 to 13 run 1.
- The full suite was not run here.

#### Run 3: combat

The 52 combat constants are gone: `ON_ATTACK`, the ally, any, "attacks you" and "attacks while you defend" attack
slots, `ON_ALLY_CREATURES_ATTACK` and its graveyard, exile and command-zone forms, the attack-a-player slots,
`ON_ANY_PLAYER_ATTACKS`, the planeswalker-attacked and attacking-creature-ability slots; the four unblocked slots;
`ON_BLOCK`, `ON_BLOCKS_MULTIPLE_CREATURES` and the any-creature block slots; the four becomes-blocked slots; and the
combat damage slots (to a player, opponent, owner, creature, blocking creature, player or planeswalker, player or
battle, you; by this, equipped, ally, any, same-name and goaded creatures; dealt to this, to you, to you or your
planeswalker from the graveyard). Family 4 (damage) is next; families 4 to 11 are left.

**Authoring.**

- `model.trigger.CombatTrigger(CombatEvent event, ObjectRelation relation, PermanentPredicate filter)`: creatures attack
  (**CR 508.1**, checked), block (**CR 509.1**, checked), become blocked (**CR 509.1h**, checked) or deal combat damage
  (**CR 510.2**, checked). `CombatEvent` has one value per event and recipient (`ATTACKS`, `ATTACKS_YOU`,
  `ONE_OR_MORE_ATTACK`, `ATTACKS_UNBLOCKED`, `BLOCKS`, `BECOMES_BLOCKED`, `COMBAT_DAMAGE_TO_PLAYER`, …); the relation
  names whose creature (`THIS`, `YOU_CONTROL`, `ANY`, `EQUIPPED`, `ENCHANTED`, `OPPONENT_CONTROLS`); the filter is only
  set for "a creature with the same name" and "one or more goaded creatures", whose collectors check it as before.
- One named `Triggers` factory per slot reading (47; the graveyard, exile and command-zone forms reuse the battlefield
  one with a `TriggerCondition` zone): `attacks()`, `creatureYouControlAttacks()`, `creatureAttacks()`, `youAttack()`,
  `playerAttacks()`, `creatureAttacksYou()`, `creatureAttacksYouOrYourPlaneswalker()`, `blocks()`, `becomesBlocked()`,
  `attacksUnblocked()`, `dealsCombatDamageToPlayer()`, `creatureYouControlDealsCombatDamageToPlayer()`,
  `equippedCreatureDealsCombatDamageToPlayer()`, `dealsCombatDamage()`, `dealtCombatDamage()`, …
  (`scripts/step13-combat-slots.tsv` maps each slot to its factory and zone).
- **Kept as the slots were:** on an Aura or Equipment the `THIS` forms of attacks, blocks, becomes blocked and deals
  combat damage to a player are the attached creature's ("whenever enchanted creature attacks", or the ability the
  attachment grants it, still told apart by `attackTriggerOwnedByAura` / `attackTriggerGrantedByEquipment`), and on a
  Curse `dealsCombatDamageToPlayer()` is "a creature deals combat damage to enchanted player". The enum's Javadoc says
  so. Step 14 or the step that moves combat onto rules events splits them into `ENCHANTED` / `EQUIPPED` relations.
- `TriggerMode` stays on the registration (`ONCE_PER_BLOCK`, `PER_BLOCKER`, `ONCE_PER_BATCH`, `INDEPENDENT`), as 12b
  kept `INDEPENDENT`: `Card.addTrigger(trigger, mode, effects)`, new `Card.addSimultaneousTrigger(trigger, mode,
  effects)` (Assembled Alphas' per-blocker pair). Step 14 turns the multiplicity modes into `TriggerOptions`.
- New readers and constructors: `GrantedTriggeredAbilitySupport.grantedTriggerEffects(game, permanent, controllerId,
  trigger)` (the snapshot form), `TemporaryGlobalTriggeredAbility(controllerId, sourceCard, ability)`,
  `GrantEffectToTargetEffect.toSource(Ability, duration)`, `ChooseCardFromHandToPerpetuallyGrantTriggeredAbilityEffect
  (Trigger, effects, filter)`. `BecomeCopyOfTargetCreaturePermanentlyEffect.additionalRetainedEffectSlots` is
  `additionalRetainedTriggers` (Aurora Shifter keeps its combat damage ability by trigger).
- `LegacySlotGrouping.ALL_BUT_INDEPENDENT` is gone: its four slots were all combat ones. `LegacySlotTrigger` no longer
  knows the `COMMAND_ZONE_` and `EXILE_` prefixes (no slot has one).

**No rules event, no index.** As in runs 1 and 2, the attack, block and combat damage collectors in
`CombatAttackService`, `CombatBlockService`, `CombatDamageService`, `CombatTriggerService` and `TriggerCollectionService`
still run where the slots were collected. `service/trigger/CombatWatch` has one constant per old slot, naming its
trigger and the zone the collector reads; it and `SpellAbilityWatch` implement the new `TriggerWatch` (`trigger()`,
`zone()`, `effectsOf`, `registrationsOf`), so `dispatchWatch` takes either. Methods that took a combat slot take a
`CombatWatch` (`checkAuraTriggersForCreature`, the source-deals-combat-damage dispatcher, the graveyard ally-damage
check, the attack-trigger source record); `collectTemporaryGlobalTriggers` takes an `EffectSlot` or a `TriggerWatch`.
The 11 handler registrations moved to `@CollectsCombatTrigger(watch = …)`, scanned by `TriggerCollectorRegistry` and
`GameEngineConfig`. The engine and AI readers were converted by `scripts/step13-combat-readers.py`
(`getEffects`/`getEffectRegistrations` → `watch.effectsOf/registrationsOf`, the temporary, persistent, granted and
graveyard readers, watcher slot checks) and the rest by hand. Grouping, target routing, wrapper checks, face-down and
ability-loss checks are unchanged, so the run makes no rules change. Shadow mode was not run, for run 1's reason.

**Writers.** Specialize (Lukamina, Imoen, Ambergris), the Protean War Engine conjure, the dethrone copy keyword
(`CloneService`), cipher, the suspect-and-perpetually-grant handler, the Imoen token binding
(`ExileDyingCreatureCardAndCreateTokenEffectHandler`), `TokenCardFactory`'s Aura attack check, the rampage
"already has it" check, the until-next-turn global registration (Tasha) and the chosen-color grant (Zombie Boa, which
rebuilt its ability from the slot) add or read the trigger.

**Cards.** `scripts/step13-combat-triggers.py` (run 2's script reading the TSV, with this family's grant heads:
the until-end-of-turn/end-of-combat grants, the chosen-color, chosen-creature, source and cast-spell grants, the
until-next-turn registration and the perpetual source-card grant) converted 3,103 card classes and 24 card tests, one
ability per registration in the old order, any `TriggerMode` kept. `MANUAL`: Sunfrill Imitator and Aurora Shifter (the
copy's retained triggers), `RunedTerrorTest` (a hand-made temporary global ability). Card comments and Javadoc naming a
slot name its factory; 17 test display names said "ON_ATTACK trigger" and say "attack trigger". The application and
AI tests were converted by the same two scripts plus hand edits (below). `scripts/step13-combat-docs.py` rewrote
agent-docs (1,220 lines); the skills and `ARCHITECTURE.md` were written by hand.

**For later runs.**

- Step 14: the combat collectors still read their wrappers (`AllyCombatDamageTriggerEffect`'s dealer filter and
  once-per-step flags, `TriggeringPermanentConditionalEffect`, `BlockPairConditionalEffect`, `BlockedCreatureTriggerEffect`,
  `BatchedCombatDamageToYouTriggerEffect`, `OncePerTurnTriggerEffect` for "attacks for the first time each turn", …)
  and the registration modes above.
- 70 card classes have both an `attacks()` and a `blocks()` ability, many of them for one "whenever this creature
  attacks or blocks" ability (Takenuma Bleeder). They stay two abilities, as the two slots were; only one of the two
  events can happen to a creature in a combat, so it behaves the same. Step 14 merges such pairs into one ability with
  two trigger conditions.
- The Aura/Equipment and Curse readings of the `THIS` forms (above).

**Tests changed.**

- New `CombatTriggerTest` (`model/trigger/`): default zone, the factories' events and relations, the same-name and
  goaded filters, null rejection, a card's attack abilities read by zone, and registrations keeping their trigger modes
  (`addSimultaneousTrigger` with `PER_BLOCKER` included).
- New `CombatWatchTest`: every watch has its own (trigger, zone) pair, so the collectors tell them apart, and a watch
  reads only the abilities of its zone.
- `CardAbilitiesTest`: the all-grouped slot test uses `ON_SELF_CYCLED` instead of `ON_BLOCK`; the
  independent-registrations test is deleted with the all-but-independent grouping it tested.
- `CombatDamageServiceTest` (mock-wired): its attacker helper takes a trigger, its granted-trigger stub stubs
  `grantedTriggerEffects` with the trigger; the slot registrations by script.
- `DamageTriggerCollectorServiceTest` and `TargetLegalityServiceTest`: dispatch to the watches / add the trigger.
- `GrantedAbilitiesTest`, `TriggeredAbilityTest` and `GameDataDeepCopyTest` used `ON_ATTACK` as an arbitrary legacy
  slot: they use `ON_SELF_CYCLED`. `TriggerConditionTest` uses `Triggers.youCastThisSpell()` (stack default) instead of
  the command-zone slot; `LegacySlotTriggerTest`'s command-zone and exile rows are gone, and its per-slot case runs 52
  fewer times.
- By script: the 24 card tests (17 display names, `PassTheTorchTest`, `StormTheCitadelTest`, `RunedTerrorTest`, …).

**Test runs.**

- Card: every test class of a converted card and every card test that uses one (4,699 classes, 38,842 tests): all
  passing.
- Application: the combat, trigger-collector, registry, pipeline, trigger-model, copy, layer, freeze, architecture,
  simulation-copy, step, turn-cleanup, targeting and setup classes (40 classes; 1,470 tests, 1,517 at HEAD: the 55
  deleted cases above, and 8 new): 107 failures, exactly the
  tests that fail at HEAD (`3b511e8f46`, same classes run in a worktree): mock wiring (`StoriedEffectHandler` not
  injected in `CombatDamageServiceTest`, …) and the known ones in `CardImmutabilityArchTest`,
  `SimulationCopyCompletenessTest` and `CardEffectTargetingConsistencyTest`.
- AI: the whole module (1,620 tests), the same 10 failures as steps 7 to 13 run 2.
- The full suite was not run here.

#### Run 4: damage

The 40 damage constants are gone: `ON_DAMAGE_TO_PLAYER`, `ON_DAMAGE_TO_OPPONENT`, `ON_SELF_DEALS_DAMAGE`, `ON_DEALT_DAMAGE`,
`ON_NONCOMBAT_DAMAGE_TO_SELF`, the four enchanted-creature and the enchanted-player slots, the "a creature / permanent /
creature an opponent controls is dealt damage" slots and the three excess-damage slots, the ally-creature-to-creature
and to-planeswalker slots, the "you're dealt damage" slots (any source, by a source you control, by an opponent's
source, prevented), the opponent-dealt-damage slots, the ally-source-to-opponent / player / creature slots and their
noncombat forms, the "one or more creatures you control deal damage" slots, the any-source and instant-or-sorcery
slots, the "you or a permanent you control" slots, the planar "deals damage to its controller" slot and the three
`GRAVEYARD_` forms. Family 5 (counters) is next; families 5 to 11 are left.

**Authoring.**

- `model.trigger.DamageTrigger(DamageEvent event, ObjectRelation relation)`: damage is dealt, combat or noncombat
  (**CR 120.1**, **CR 120.2**, checked), excess damage (**CR 120.10**, checked) or damage to you is prevented
  (**CR 615.1**, checked). `DamageEvent` has one value per event and recipient (`DEALS_DAMAGE`, `DEALS_DAMAGE_TO_PLAYER`,
  `DEALT_DAMAGE`, `DEALT_NONCOMBAT_DAMAGE`, `CREATURE_DEALT_DAMAGE`, `YOU_DEALT_DAMAGE`, `ENCHANTED_PLAYER_DEALT_DAMAGE`,
  `DAMAGE_TO_YOU_PREVENTED`, …); the relation names the damage source for a "deals" event and for a player being dealt
  damage, and the damaged permanent for a permanent's "is dealt" event. The combat-only forms stay `CombatTrigger`s.
- One named `Triggers` factory per slot reading (39; the graveyard form of "a source you control deals noncombat damage
  to an opponent" reuses the battlefield one with a `TriggerCondition` zone): `dealsDamage()`, `dealsDamageToPlayer()`,
  `dealsDamageToOpponent()`, `dealtDamage()`, `dealtNoncombatDamage()`, `enchantedCreatureDealtDamage()`,
  `creatureDealtDamage()`, `permanentYouControlDealtDamage()`, `creatureYouControlDealsDamageToCreature()`,
  `youAreDealtDamage()`, `youAreDealtDamageBySourceOpponentControls()`, `permanentDealsDamageToYou()`,
  `sourceYouControlDealsDamageToOpponent()`, `sourceYouControlDealsNoncombatDamageToOpponent()`,
  `instantOrSorceryYouControlDealsDamage()`, … (`scripts/step13-damage-slots.tsv` maps each slot to its factory and
  zone).
- **Kept as the slots were:** on an Aura or Equipment the `THIS` forms of `dealsDamageToPlayer()` /
  `dealsDamageToOpponent()` are the attached creature's. `creatureYouControlDealsDamageToCreature()` stays the old
  slot's mixed bag: its effect says whether it is "this creature" (most of its 29 cards), "a creature you control",
  "equipped creature" (Giant's Skewer, Sword of Kaldra) or "a creature" under any player's control (Toxin Sliver), as
  the collector's effect-class checks did. `permanentYouControlDealtDamage()` is narrowed by its effect ("a Dragon you
  control", "this or a creature it's paired with"), `sourceDealsDamage()` and `creatureDealsDamageToYouOrYourPermanent()`
  filter their source in the effect, and the planar `creatureDealsDamageToItsController()` keeps the battlefield default
  (planes are read zone-agnostically, run 1's note). Step 14 or the step that moves damage onto rules events splits them.
- `GrantEffectToTargetEffect.toEnchantedPermanent` takes an `Ability` (its only user, Genju of the Fields, grants
  `dealsDamage()`); the slot form is gone. `LegacySlotGrouping.ALL` lost its three enchanted-creature slots.
- `GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffectHandler` binds the caster into the granted ability with
  `LegacySlots.withEffect` instead of rebuilding the grant from its slot, which a real trigger doesn't have (Hellish
  Rebuke's granted `dealsDamageToPlayer()` ability was lost; its card test caught it).

**No rules event, no index.** As in runs 1 to 3, the damage collectors in `TriggerCollectionService`, `DamageSupport`
and `CombatDamageService` still run where the slots were collected. `service/trigger/DamageWatch` (a `TriggerWatch`)
has one constant per old slot, naming its trigger and the zone the collector reads. The 95 handler registrations moved
from `@CollectsTrigger(slot = …)` to `@CollectsDamageTrigger(watch = …)` (`scripts/step13-damage-rekey.py`), scanned by
`TriggerCollectorRegistry` and `GameEngineConfig`. The readers were converted by `scripts/step13-damage-readers.py`
(run 3's reader script on this family's TSV); by hand: the methods that took a damage slot take a `DamageWatch`
(`checkDamageToCreatureTriggers`, the excess-damage watch list, the ally-source-to-opponent-or-player loop,
`dispatchPlanarDamageTrigger`), `checkAnyPermanentDealtDamageTriggers` uses `dispatchWatch`, and Klement's
specialization adds `dealtDamage()`. Grouping, target routing, wrapper checks, face-down and ability-loss checks are
unchanged, so the run makes no rules change. Shadow mode was not run, for run 1's reason.

**Cards.** `scripts/step13-damage-triggers.py` (run 3's script reading the damage TSV, with
`GrantEffectToTargetEffect.toEnchantedPermanent` as one more grant head) converted 295 card classes (the step 1
inventory's count) and 2 card tests, one ability per registration in the old order, any `TriggerMode` kept. Nothing
needed `MANUAL`. Card comments naming a slot name its factory. The domain effects' Javadoc that named a slot names its
factory (a one-off rewrite of `{@code SLOT}` / `{@link EffectSlot#SLOT}`); engine Javadoc links point at the
`DamageWatch` constant, and engine comments keep the slot name, which is the watch constant's.
`scripts/step13-damage-docs.py` rewrote agent-docs (274 lines); the skills and `ARCHITECTURE.md` were written by hand.
`effect-slot-inventory.md` was re-rendered with `--keep-baseline`.

**For later runs.**

- Step 14: the damage collectors still read their wrappers (`TriggeringPermanentConditionalEffect` for "one or more
  Heroes", "a Dragon you control" and the like, `OncePerTurnTriggerEffect`, `OncePerTurnPerCreatureTriggerEffect`
  (Stensia), `PerDamageSourceTriggerEffect`, intervening-if `ConditionalEffect`s) and several handler-enforced filters
  (the source must be one you control for some `opponentDealtNoncombatDamage()` and `sourceDealsDamage()` handlers, the
  damaged permanent must be yours for `permanentYouControlDealtDamage()`).
- Step 15: the `DamageSourceControllerAwareEffect`, `DamageSourceAwareEffect`, `CombatDamageDealerAwareEffect` and
  `CombatDamageAmountAwareEffect` bindings and the `Damaged…` / `DamageSource…` effects read the event there.
- The mixed readings listed under "Kept as the slots were".

**Tests changed.**

- New `DamageTriggerTest` (`model/trigger/`): default zone, the factories' events and relations, the damage triggers
  differ from the combat ones, null rejection, a card's damage abilities read by zone.
- New `DamageWatchTest`: every damage, combat and spell-and-ability watch has its own (trigger, zone) pair, and a watch
  reads only the abilities of its zone.
- `DamageTriggerCollectorServiceTest`, `MiscTriggerCollectorServiceTest` and `TriggerCollectorRegistryTest` dispatch to
  the watches (by the reader script); `DamageTriggerCollectorServiceTest`'s three hand-built cards add the trigger.
- `TurnCleanupServiceTest` registers its temporary global ability with `creatureDealtDamage()`.
- By script: `GenjuOfTheFieldsTest` and `StirThePrideTest` read `getTemporaryTriggerEffects(Triggers.dealsDamage())`.
- `LegacySlotTriggerTest`'s per-slot case runs 40 fewer times (it is parameterised over `EffectSlot.values()`).

**Test runs.**

- Card: every test class of a converted card, the back-face cards' front-face tests and every card test that
  constructs one (446 classes, 3,883 tests): all passing after the two fixes above. The first run had failed
  `HellishRebukeTest` (5, the lost grant) and `BloodfeatherPhoenixTest` (2: a generator mistake had given
  `sourceYouControlDealsNoncombatDamageToOpponent()` the instant-or-sorcery event, so the Phoenix's graveyard ability
  answered the other graveyard reader; `DamageWatchTest` now rejects such a collision).
- Application: the trigger-model, watch, registry, damage and misc collector, combat damage, turn-cleanup, planar,
  targeting, pipeline, grant-view, freeze, architecture and simulation-copy classes (24 classes, 797 tests): 75
  failures, exactly the tests that fail at HEAD (`2138409bdd`, same classes run in a worktree): mock wiring in
  `CombatDamageServiceTest`, `TargetLegalityServiceTest`, `TurnCleanupServiceTest`, `DamageTriggerCollectorServiceTest`
  and `PlanechaseServiceTest`, and the known ones in `CardImmutabilityArchTest`, `SimulationCopyCompletenessTest` and
  `CardEffectTargetingConsistencyTest`.
- AI: the whole module (1,620 tests), the same 10 failures as steps 7 to 13 run 3.
- Rechecked during run 5 against a worktree baseline, both commits with the main tree's `card-data-cache` (run 5's
  findings explain why): the application classes this run lists plus run 5's (39 classes; 1,110 tests at `88efd999a0`,
  1,143 at `2138409bdd`: 40 deleted `LegacySlotTriggerTest` cases, 7 new) fail the same 145 tests at both commits, and
  the AI module the same 10. No test hit a `cardRegistry` error at either commit.
- The full suite was not run here.

#### Run 5: counters

The 43 counter constants are gone: `ON_SELF_COUNTERS_PUT`, `ON_ALLY_COUNTER_PUT_ON_CREATURE`, the four "you put
counters" slots (on a creature, another creature, a creature you don't control, a permanent or player) and the
opponent one; the eleven +1/+1-counter-put slots (on this, on a Human, a non-Hydra, another creature, another permanent
for the first time this turn, a permanent or creature you control, by you or by anyone, any creature, other Heroes,
`ON_CONTROLLER_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_CREATURE`) and their graveyard form; the five -1/-1 slots; the time,
lore and loyalty slots (put and removed, `ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE`); `ON_SELF_PLUS_ONE_PLUS_ONE_COUNTER_REMOVED`
and `ON_ALLY_COUNTERS_REMOVED_FROM_PERMANENT`; renowned (this, a creature you control), monstrous, evolves, trains, the
two Class level slots; `ON_CONTROLLER_GETS_ENERGY`, `ON_CONTROLLER_PROLIFERATES` and its graveyard form. Family 6
(becomes the target) is next; families 6 to 11 are left.

**Authoring.**

- `model.trigger.CounterTrigger(CounterEvent event, ObjectRelation relation, PermanentPredicate filter)`: counters
  (**CR 122.1**, checked; counters a permanent enters with count, **CR 122.6**, checked) are put on or removed from a
  permanent, or a keyword action about counters happens: becomes renowned (**CR 702.112b**, checked) or monstrous
  (**CR 701.37b**, checked), evolves (**CR 702.100b**, checked), trains (**CR 702.149c**, checked), a Class gains a level
  (**CR 716.2a**, checked), you get energy (**CR 107.14**, checked), you proliferate (**CR 701.34a**, checked).
  `CounterEvent` has one value per counter kind, who puts the counters and cadence (`COUNTERS_PUT`, `YOU_PUT_COUNTERS`,
  `PLUS_ONE_COUNTERS_PUT`, `EACH_MINUS_ONE_COUNTER_PUT`, `YOU_PUT_EACH_LORE_COUNTER`, …: "each" is once per counter, the
  rest once per permanent); the relation names the permanent the counters go on or come off (`ANY` for an event about
  you alone); the filter is its type (`Triggers.CREATURE`, and the new `HUMAN_CREATURE`, `HERO_CREATURE`,
  `NON_HYDRA_CREATURE`, `PLANESWALKER`, `SAGA`), which the collectors check in code as before.
  `TIME_COUNTER_REMOVED_IN_EXILE` functions from exile by default (`defaultZones()`), so its cards need no condition.
- One named `Triggers` factory per slot reading (41; the two graveyard forms reuse the battlefield factory with a
  `TriggerCondition` zone): `countersPutOnThis()`, `plusOneCountersPutOnThis()`, `plusOneCountersPutOnCreatureYouControl()`,
  `youPutPlusOneCountersOnCreatureYouControl()`, `youPutCountersOnCreature()`, `minusOneCountersPutOnCreature()`,
  `minusOneCounterPutOnCreature()`, `timeCountersRemovedFromThis()`, `timeCounterRemovedFromThisInExile()`,
  `becomesMonstrous()`, `evolves()`, `trains()`, `reachesLevelTwo()`, `youGetEnergy()`, `youProliferate()`, …
  (`scripts/step13-counter-slots.tsv` maps each slot to its factory, event, relation, filter and zone).
- **Kept as the slots were:** `ON_CONTROLLER_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_CREATURE` (Terrasymbiosis, Ant-Man) and
  `ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_CREATURE` (Stocking the Pantry, Casey Jones) have the same Oracle wording
  ("whenever you put one or more +1/+1 counters on a creature you control") but two collectors at the same choke point:
  one puts a stack object per effect and queues a `MayEffect` through `queueMayAbility`, the other groups the effects,
  supports targets and resolves a `MayEffect` on the stack. Giving both one trigger would run each collector over the
  other's cards. The first is `YOU_PUT_PLUS_ONE_COUNTERS_ONCE_EACH_TURN` (`youPutPlusOneCountersOnCreatureYouControlOnceEachTurn()`,
  named for its two cards, both limited to once each turn); step 14 turns the limit into `TriggerOptions` and merges
  the two. `TriggerMode.PER_COUNTER` stays on its registrations (Fathom Mage, Bloodcrazed Hoplite).
- `ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTER_ON_CREATURE` had no card; it is `youPutMinusOneCounterOnCreature()` and its
  collector path is kept. Its agent-docs rows named Nest of Scarabs and the "one or more" wording, which is the other
  factory's (Nest of Scarabs uses `youPutMinusOneCountersOnCreature()`); both rows were corrected by hand.
- `LegacySlotGrouping.ALL` lost its 28 counter slots.

**No rules event, no index.** As in runs 1 to 4, the counter collectors in `TriggerCollectionService`,
`PermanentCounterSupport`, `TrainEffectHandler`, `EvolveTriggerEffectHandler`, `ClassLevelUpEffectHandler` and
`PermanentRemovalService` (the training look-back) still run where the slots were collected.
`service/trigger/CounterWatch` (a `TriggerWatch`) has one constant per old slot, naming its trigger and the zone the
collector reads. The 15 handler registrations moved from `@CollectsTrigger(slot = …)` to
`@CollectsCounterTrigger(watch = …)` (`scripts/step13-counter-rekey.py`), scanned by `TriggerCollectorRegistry` and
`GameEngineConfig`. The readers were converted by `scripts/step13-counter-readers.py` (run 4's reader script on this
family's TSV, which now also turns `dispatchSlot` of a family slot into `dispatchWatch`); by hand: the two methods that
took a counter slot take a `CounterWatch` (`queueLevelReachedTriggers`, the +1/+1 "on a permanent you control"
collector), `ClassLevelUpEffectHandler` picks its watch, and the counters-put-on-this collector finds a granted ability
(Danny Pink) with `grant.grantedAbility() instanceof TriggeredAbility ability && ability.hasTrigger(…)`. Grouping,
target routing, wrapper checks and ability-loss checks are unchanged, so the run makes no rules change. Shadow mode was
not run, for run 1's reason.

**Cards.** `scripts/step13-counter-triggers.py` (run 4's script reading the counter TSV) converted 116 card classes
(the step 1 inventory's count) and no card test (none named a counter slot), one ability per registration in the old
order, any `TriggerMode` kept. Nothing needed `MANUAL`; Danny Pink's grant argument was re-indented by hand. The domain
effects' Javadoc that named a slot names its factory; engine Javadoc links point at the `CounterWatch` constant.
`scripts/step13-counter-docs.py` rewrote agent-docs (103 lines); the skills and `ARCHITECTURE.md` were written by hand.
`effect-slot-inventory.md` was re-rendered with `--keep-baseline`.

**For later runs.**

- Step 14: the counter collectors still read their wrappers (`OncePerTurnTriggerEffect` and its `markOnAcceptance`,
  `OncePerTurnPerCreatureTriggerEffect`, `YouPutCounterOnControlledCreatureTriggerEffect`, `TriggeringPermanentConditionalEffect`
  for "another colorless creature", `YouRemoveTimeCounterFromExiledCardTriggerEffect`, the `SourceCounterThreshold`
  intervening "if"s of the plan-counter cards), `TriggerMode.PER_COUNTER`, and the two "you put +1/+1 counters on a
  creature you control" events above.
- Step 15: the counter count (`EventValue`), the triggering permanent of the "counters put on a creature" abilities and
  `PutSameCountersOnSourceEffect`'s counters are still bound by the collectors.
- Sagas (family 10) still use `SAGA_CHAPTER_*`; `youPutLoreCounterOnSagaYouControl()` (Sigurd) is a counter trigger.
- Several collectors read only printed abilities (`source.getCard()`), not granted ones, as the slots did.

**Tests changed.**

- New `CounterTriggerTest` (`model/trigger/`): default zones (exile for the exiled time-counter trigger), the factories'
  events, relations and filters, the once-each-turn form kept apart, null rejection, abilities read by zone, and a
  registration keeping `TriggerMode.PER_COUNTER`.
- New `CounterWatchTest`: every counter, damage, combat and spell-and-ability watch has its own (trigger, zone) pair, a
  watch reads only the abilities of its zone, and the exiled time-counter watch reads the exiled card.
- `MiscTriggerCollectorServiceTest`: dispatches to the energy and proliferate watches (by the reader script).
- `LegacySlotTriggerTest`'s per-slot case runs 43 fewer times (it is parameterised over `EffectSlot.values()`).

**Test runs.**

- Card: every test class of a converted card and every card test that constructs one (173 classes, 1,794 tests): all
  passing.
- Application: the trigger-model, watch, registry, misc, damage and discard collector, combat, combat damage, placement,
  timestamp, activation, stack resolution, library and graveyard handler, freeze, architecture, ratchet, pipeline and
  simulation-copy classes (34 classes; 776 tests, 808 at HEAD: 43 deleted cases, 11 new): 119 failures, exactly the tests
  that fail at HEAD (`88efd999a0`, same classes run in a worktree): mock wiring in `ActivatedAbilityExecutionServiceTest`,
  `BattlefieldPlacementServiceTest`, `CombatDamageServiceTest`, `CombatServiceTest`, `DamageTriggerCollectorServiceTest`,
  `PermanentTimestampTest` and `PutTargetCardsFromGraveyardOnBottomOfLibraryEffectHandlerTest`, and the known ones in
  `CardImmutabilityArchTest`, `SimulationCopyCompletenessTest` and `CardEffectTargetingConsistencyTest`.
- AI: the whole module (1,620 tests): 10 failures, exactly the tests that fail at HEAD (`88efd999a0`, run in a
  worktree): the modal-mode, kicker and Reign of Chaos choices in the four AI decision engine tests and the two
  `TargetPolarityGuardTest` cases, the same 10 as steps 7 to 13 run 4.
- A fresh worktree fetches the sets its card-data cache lacks, and the provider's current `set-sld.json` maps SLD #921
  to another card, so card registration rejects Goblin Matron and every Spring-context test fails (321 AI tests at
  first). Both baselines above were run with the main tree's `card-data-cache` copied into the worktree.
- The full suite was not run here.

#### Run 6: becomes the target

The 21 becomes-the-target constants are gone: `ON_BECOMES_TARGET_OF_SPELL`, `ON_BECOMES_TARGET_OF_AURA_SPELL`,
`ON_BECOMES_TARGET_OF_OPPONENT_SPELL` (ward, 235 cards), `ON_BECOMES_TARGET_OF_OPPONENT_SPELL_ONLY`,
`ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY`, the two controller slots, the ally, another-ally, any and opponent creature
slots (opponent's spell or ability, any spell or ability, an activated ability, an opponent's spell, a spell, a backup
ability, an instant or sorcery), the creature-or-creature-spell slot, the three ally permanent slots (with or without
the player, another) and `ON_ANY_PLAYER_CHOOSES_TARGETS`. Family 7 (library, draw and discard) is next; families 7 to
11 are left.

**Authoring.**

- `model.trigger.TargetTrigger(TargetEvent event, ObjectRelation relation, PermanentPredicate filter)`: a player or
  object becomes the target of a spell (**CR 601.2c**, checked) or an ability (**CR 602.2b**, **CR 603.3d**, checked),
  or a player chooses targets (**CR 115.1**, checked). `TargetEvent` names what targets and, when it isn't a permanent,
  what becomes the target (`BECOMES_TARGET_OF_SPELL`, `…_OF_AURA_SPELL`, `…_OF_INSTANT_OR_SORCERY_SPELL`,
  `…_OF_OPPONENT_SPELL`, `…_OF_SPELL_OR_ABILITY`, `…_OF_OPPONENT_SPELL_OR_ABILITY` (ward, **CR 702.21a**, checked),
  `…_OF_YOUR_SPELL_OR_ABILITY`, `ONE_OR_MORE_BECOME_TARGET_OF_ACTIVATED_ABILITY`, `…_OF_BACKUP_ABILITY`
  (**CR 702.165a**, checked), `PERMANENT_OR_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY`,
  `YOU_BECOME_TARGET_OF_SPELL`, `YOU_BECOME_TARGET_OF_OPPONENT_SPELL_OR_ABILITY`,
  `YOU_OR_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY`, `PLAYER_CHOOSES_TARGETS`); the relation names the
  permanent that becomes the target (`ANY` for an event about you or about choosing targets); the filter is
  `Triggers.CREATURE` for the creature forms, which the collectors check in code as before.
- One named `Triggers` factory per slot (21, all from the battlefield): `becomesTargetOfSpell()`,
  `becomesTargetOfAuraSpell()`, `becomesTargetOfOpponentSpellOrAbility()` (ward), `becomesTargetOfOpponentSpell()`,
  `becomesTargetOfSpellOrAbility()`, `youBecomeTargetOfSpell()`, `creatureYouControlBecomesTargetOfOpponentSpellOrAbility()`,
  `creatureBecomesTargetOfSpellOrAbility()`, `youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility()`,
  `playerChoosesTargets()`, … (`scripts/step13-target-slots.tsv` maps each slot to its factory, event, relation and
  filter). The old `ON_BECOMES_TARGET_OF_OPPONENT_SPELL` was already "a spell or ability an opponent controls"; its
  factory says so.
- **Kept as the slots were:** on an Aura or Equipment the `THIS` forms of becomes the target of a spell, an Aura spell,
  a spell or ability and an opponent's spell or ability (the counter-unless path) are the attached permanent's (the
  enum's Javadoc says so). Step 14 or the step that moves targeting onto rules events splits them into `ENCHANTED` /
  `EQUIPPED` relations.
- `PerpetuallyGrantKeywordsToTargetCreatureEffect(keywords, Ability)` and
  `PerpetuallyGrantKeywordToMatchingCardsEffect(handFilter, permanentFilter, keywords, Ability)` are new: perpetual
  ward grants (Wilson's green face, Rimewall Protector, …) pass the ability instead of the slot and its effect; the slot
  constructors stay for other families. `PutRandomCreatureFromTargetOpponentLibraryOntoBattlefieldEffectHandler` grants
  its perpetual ward as `new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(), ward)`.
- `LegacySlotGrouping.ALL` lost its 12 target slots; `LegacySlots.slotOf` knows `TargetTrigger`.

**No rules event, no index.** As in runs 1 to 5, the becomes-the-target collectors in `TriggerCollectionService`
(`checkBecomesTargetOfSpellTriggers`, `checkBecomesTargetOfAbilityTriggers`, `checkTargetChoiceTriggers` and the
`collect…` methods they call) still run where the slots were collected. `service/trigger/TargetWatch` (a
`TriggerWatch`) has one constant per old slot, naming its trigger and the zone the collector reads. The family had no
`@CollectsTrigger` handlers, so there is no new annotation and the registry is unchanged. The readers were converted by
`scripts/step13-target-readers.py` (run 5's reader script on this family's TSV); by hand: the perpetual ward grant and
the Javadoc links (`{@link EffectSlot#X}` → `{@link TargetWatch#X}`). Grouping, target routing, wrapper checks, ward
suppression, face-down and ability-loss checks are unchanged, so the run makes no rules change. Shadow mode was not run,
for run 1's reason.

**Cards.** `scripts/step13-target-triggers.py` (run 5's script reading the target TSV, plus a pass that turns a slot
and its effect inside the two perpetual keyword grants into one `TriggeredAbility`) converted 342 card classes and no
card test (none named a target slot), one ability per registration in the old order. Nothing needed `MANUAL`; Phantasmal
Image's now unused `CardEffect` import was dropped by hand. `scripts/step13-target-docs.py` rewrote agent-docs (100
lines); by hand: the grant rows of this family show `new TriggeredAbility(…)` (the earlier runs' rows still show the
`Grant…(Triggers.x(), effect, …)` shorthand), "`Triggers.x()` slot" reads "ability", the `TRIGGER_SLOT_TARGETING.md`
row with `…_OR_ABILITY` suffixes and the `ACTIVATED_ABILITY_GUIDE.md` ward row ("an opponent's spell" → "spell or
ability"), the skills and `ARCHITECTURE.md`. `effect-slot-inventory.md` was re-rendered with `--keep-baseline`.

**For later runs.**

- Step 14: the target collectors still read their wrappers (`TriggeringSpellControllerConditionalEffect`,
  `TriggeringSpellOpponentControllerConditionalEffect`, `TriggeringPermanentConditionalEffect` for "a Dragon you
  control", `TriggeringAbilityFromNamedLandConditionalEffect`, `AbilityTargetChoiceTriggerEffect`,
  `OncePerTurnTriggerEffect` for "for the first time each turn" and Professor Hojo's "only once each turn", the
  backup ability's once-per-turn cap), and `playerChoosesTargets()` covers both "a player chooses one or more targets"
  (Psychic Battle) and "a spell or ability is put onto the stack, if it has a single target" (Grip of Chaos) and Loki's
  "becomes the target of an ability you control", told apart by their effects.
- Step 15: the targeted permanent (`targetId` / `triggeringPermanentId`), the targeting spell or ability
  (`resolveForBecomesTargetOfSpellOrAbility`, the `TriggeringSpellReferencingEffect`s) and Mirror Shield Hoplite's
  backup ability are still bound by the collectors.
- Ward's `isCounterUnlessTrigger` checks (`hasWardAbility`, `hasGrantedCounterUnlessTrigger`) still look for a
  `CounterUnlessEffect` among the ward trigger's effects; ward as a keyword ability with a cost would replace them.
- The Aura/Equipment readings of the `THIS` forms (above).

**Tests changed.**

- New `TargetTriggerTest` (`model/trigger/`): default zone, the factories' events, relations and filters, null
  rejection, a card's target abilities read by trigger and zone, a perpetual ward grant read by its trigger, and the
  two new perpetual keyword grant constructors.
- New `TargetWatchTest`: every target, counter, damage, combat and spell-and-ability watch has its own (trigger, zone)
  pair, every target watch reads the battlefield, and a watch reads only its trigger's abilities from its zone.
- `LegacySlotTriggerTest`'s per-slot case runs 21 fewer times (it is parameterised over `EffectSlot.values()`).
- No other test named a target slot.

**Test runs.**

- Card: every test class of a converted card and every card test that references one (603 classes, 5,567 tests): all
  passing.
- Application: the trigger-model, watch, registry, granted-ability, freeze, architecture, simulation-copy, pipeline,
  legacy-binding, activation, counter-spell, counter-unless, spell-casting, spell-cast collector and target-legality
  classes (29 classes; 772 tests, 784 at HEAD: 21 deleted cases, 9 new): 68 failures, exactly the tests that fail at
  HEAD (`5129141755`, same classes run in a worktree with the main tree's `card-data-cache`, no `cardRegistry` errors):
  mock wiring in `ActivatedAbilityExecutionServiceTest`, `SpellCastingServiceTest`, `SpellCastTriggerCollectorServiceTest`,
  `TargetLegalityServiceTest` and `CounterSpellEffectHandlerTest`, and the known ones in `CardImmutabilityArchTest`,
  `SimulationCopyCompletenessTest` and `CardEffectTargetingConsistencyTest`.
- AI: the whole module (1,620 tests): the same 10 failures as steps 7 to 13 run 5 (the modal-mode, kicker and Reign of
  Chaos choices in the four AI decision engine tests and the two `TargetPolarityGuardTest` cases).
- The full suite was not run here.

#### Run 7: library, draw and discard

The 23 library, draw and discard constants are gone: `ON_CONTROLLER_DRAWS`, `ON_OPPONENT_DRAWS`,
`ON_ENCHANTED_PLAYER_DRAWS`, `ON_CONTROLLER_DRAWS_SECOND_CARD` and the two `GRAVEYARD_` second-card slots; the
controller, opponent and discard-event discard slots, `ON_SELF_DISCARDED` and `ON_SELF_DISCARDED_BY_OPPONENT`;
`ON_ANY_PLAYER_CYCLES` and `ON_SELF_CYCLED`; the scry and surveil slots and `GRAVEYARD_ON_CONTROLLER_SURVEILS`; the two
library-search slots; the three shuffle slots. Family 8 (tap and untap) is next; families 8 to 11 are left.

**Authoring.**

- `model.trigger.LibraryTrigger(LibraryEvent event, PlayerRelation player)`: a player draws a card (**CR 121.1**,
  checked), discards one (**CR 701.9a**, checked) or cycles one, scries (**CR 701.22a**, checked), surveils
  (**CR 701.25a**, checked), searches their library (**CR 701.23a**, checked) or shuffles it (**CR 701.24a**, checked).
  `LibraryEvent` is `DRAWS`, `DRAWS_SECOND_CARD`, `ENCHANTED_PLAYER_DRAWS`, `DISCARDS` (cycling included),
  `DISCARDS_ONE_OR_MORE` (once per discard event), `CYCLES`, `THIS_CYCLED`, `THIS_DISCARDED`, `SCRIES`, `SURVEILS`,
  `SEARCHES_OWN_LIBRARY`, `SHUFFLES_LIBRARY` and `SPELL_OR_ABILITY_CAUSES_ITS_CONTROLLER_TO_SHUFFLE`; the player is who
  does it. For `THIS_DISCARDED` the player is the controller of the spell or ability that causes the discard:
  `youDiscardThisCard()` is `ANY`, `opponentCausesYouToDiscardThisCard()` is `OPPONENT`. "Enchanted player draws" is
  its own event because `trigger.PlayerRelation` has no enchanted value and `TriggerMatcher.causedBy` switches over it
  exhaustively.
- **Zones.** "When you cycle this card" triggers from whatever zone the card winds up in (**CR 702.29c**, checked), and
  a discard is a move to the graveyard, so `THIS_CYCLED` and `THIS_DISCARDED` function by default from the graveyard,
  exile and library (the last two for madness-style and Library of Leng-style replacements). The old abilities
  functioned from the battlefield by default but their collectors read the card's slot in any zone; the
  `LibraryWatch` constants read them as graveyard abilities, which all of them now are, so no card behaves differently.
  `TriggerIndex` only indexes zone-change and permanent-change triggers, so the new zones reach no other reader.
- One named `Triggers` factory per slot reading (21; the two graveyard forms reuse the battlefield factory with a
  `TriggerCondition` zone): `youDrawCard()`, `youDrawSecondCardEachTurn()`, `opponentDrawsSecondCardEachTurn()`,
  `opponentDrawsCard()`, `enchantedPlayerDrawsCard()`, `opponentDiscardsCard()`, `youDiscardCard()`,
  `youDiscardOneOrMoreCards()`, `playerCyclesCard()`, `youCycleThisCard()`, `youDiscardThisCard()`,
  `opponentCausesYouToDiscardThisCard()`, `youScry()`, `opponentScries()`, `youSurveil()`, `opponentSurveils()`,
  `youSearchYourLibrary()`, `opponentSearchesTheirLibrary()`, `opponentShufflesLibrary()`, `playerShufflesLibrary()`,
  `spellOrAbilityCausesItsControllerToShuffle()` (`scripts/step13-library-slots.tsv` maps each slot to its factory,
  zone, event and player).
- **Kept as the slots were:** "whenever you cycle a card" is still `youDiscardCard()` + `CyclingTriggerEffect` on 12
  cards, beside `playerCyclesCard()`; about 9 cards still say "your second card each turn" as `youDrawCard()` +
  `NthCardDrawTriggerEffect(2, …)`; Pure Intentions' temporary global ability still registers `opponentDiscardsCard()`
  with the opposite meaning (it fires when its own controller is made to discard by an opponent); Psychogenic Probe's
  `playerShufflesLibrary()` doesn't check that a spell or ability caused the shuffle; Obstinate Baloth-style
  `EnterBattlefieldOnDiscardEffect` replacements still sit in an `opponentCausesYouToDiscardThisCard()` ability and are
  read in place by the seven discard paths. The step 1 inventory's per-collector differences (ability-loss checks,
  granted triggers, may handling) are unchanged.
- `BecomeCopyOfTargetPermanentUntilYourNextTurnEffect.retainedAbilitySlot` is `retainedTrigger` (Crystalline
  Resonance keeps its `youDiscardCard()` ability by trigger; its handler re-adds the registration with `addTrigger`).
- `LegacySlotGrouping.ALL` lost its 5 library slots (`ON_SELF_CYCLED`, `ON_SELF_DISCARDED`,
  `ON_SELF_DISCARDED_BY_OPPONENT` and the two search slots); `LegacySlots.slotOf` knows `LibraryTrigger`.

**No rules event, no index.** As in runs 1 to 6, `DrawService`, the discard, discard-event, cycle, scry and surveil
collectors in `TriggerCollectionService`, `LibrarySearchTriggerHelper` and `LibraryShuffleHelper` still run where the
slots were collected. `service/trigger/LibraryWatch` (a `TriggerWatch`) has one constant per old slot, naming its
trigger and the zone the collector reads. The 72 handler registrations of `DiscardTriggerCollectorService`,
`ScryTriggerCollectorService` and `MiscTriggerCollectorService` moved from `@CollectsTrigger(slot = …)` to
`@CollectsLibraryTrigger(watch = …)` (`scripts/step13-library-rekey.py`), scanned by `TriggerCollectorRegistry` and
`GameEngineConfig`. The readers were converted by `scripts/step13-library-readers.py` (run 5's reader script on this
family's TSV); by hand: `DrawService`'s three methods that took a slot take a `LibraryWatch` (and read granted
abilities with `grantedTriggerEffects(…, watch.trigger())`), and `LibrarySearchTriggerHelper` picks its watch before
reading. The run-time conjured Choking Tethers (`ConjureSkywriterDjinnSpellbookEffectHandler`) was converted by the
card script. Grouping, target routing, wrapper checks, face-down and ability-loss checks are unchanged, so the run makes
no rules change. Shadow mode was not run, for run 1's reason.

**Cards.** `scripts/step13-library-triggers.py` (run 5's script reading the library TSV, with
`BecomeCopyOfTargetPermanentUntilYourNextTurnEffect` as one more trigger-argument head) converted 332 card classes (the
step 1 inventory's count) and no card test (`HoofprintsOfTheStagTest`'s comment naming the slot was changed by hand),
one ability per registration in the old order. Nothing needed `MANUAL`. The domain Javadoc that named a slot names its
factory; engine comments, log lines and the collector tests' display names keep the slot name, which is the
`LibraryWatch` constant's. `scripts/step13-library-docs.py` rewrote agent-docs (148 lines); by hand: Pure Intentions'
`ORACLE_TEXT_EFFECT_MAP.md` row shows `new TriggeredAbility(…)`, the skills and `ARCHITECTURE.md`.
`effect-slot-inventory.md` was re-rendered with `--keep-baseline`.

**For later runs.**

- Step 14: the collectors still read their wrappers: `NthCardDrawTriggerEffect`, `FirstOrSecondCardDrawTriggerEffect`,
  `ExceptFirstDrawStepTriggerEffect`, `FirstDrawStepDrawOnlyTriggerEffect`, `DrawRevealTriggerEffect` /
  `FirstDrawRevealTriggerEffect`, `CyclingTriggerEffect`, `OpponentCausedDiscardTriggerEffect`,
  `TriggeringCardConditionalEffect` (any discarded card for a discard event), `OncePerTurnTriggerEffect` with the
  first-cycling, first-nonland-discard, first-discard-event and first-surveil keys, `OnceOnlyTriggerEffect`, and the
  intervening-if `ConditionalEffect`s of the self-cycle and self-discard abilities. The "second card" pair and the
  "whenever you cycle" pair above merge there.
- Step 15: the drawn card, the discarded cards and their count (`eventValue`), the scry's bottomed and looked-at counts,
  the drawing, discarding, scrying, searching and shuffling player (`targetId`) are still bound by the collectors.
- `EnterBattlefieldOnDiscardEffect` is a replacement effect (step 8's kind) left in a trigger; the step that deletes
  the legacy paths should move it to the replacement model.

**Tests changed.**

- New `LibraryTriggerTest` (`model/trigger/`): default zones (battlefield for a player's action; graveyard, exile and
  library for this card being cycled or discarded), the factories' events and players, null rejection, a card's
  second-card abilities read by zone, and a cycling ability read from the graveyard.
- New `LibraryWatchTest`: every library, target, counter, damage, combat and spell-and-ability watch has its own
  (trigger, zone) pair, every library watch reads a zone its trigger functions from (the `GRAVEYARD_` forms by their
  condition), a watch reads only its trigger's abilities from its zone, and the self-cycle and self-discard watches
  read the card.
- `DiscardTriggerCollectorServiceTest`, `ScryTriggerCollectorServiceTest`, `MiscTriggerCollectorServiceTest` and
  `TriggerCollectorRegistryTest` dispatch to the watches (by the reader script).
- `DrawServiceTest`, `ShuffleHandIntoLibraryAndDrawEffectHandlerTest` and `CardAbilitiesTest`'s Death Pulse target
  group add the triggers (by the card script's call and reader passes).
- `CardAbilitiesTest`, `GrantedAbilitiesTest`, `TriggeredAbilityTest` and `GameDataDeepCopyTest` used
  `ON_SELF_CYCLED` and `GRAVEYARD_ON_CONTROLLER_SURVEILS` as arbitrary legacy slots (the first as the all-grouped
  one): they use `ON_CONTROLLER_CLASHES` (also all-grouped) and `GRAVEYARD_ON_CONTROLLER_COMMITS_CRIME`, both in family
  11. `LegacySlotTriggerTest`'s graveyard row uses the latter, and its per-slot case runs 23 fewer times (it is
  parameterised over `EffectSlot.values()`).

**Test runs.**

- Card: every test class of a converted card (and Skywriter Djinn's, which conjures Choking Tethers) and every card
  test that references one (706 classes, 5,991 tests): all passing.
- Application: the trigger-model, watch, registry, discard, scry and misc collector, draw, shuffle-hand, step, planar,
  spell-cast collector, card-ability, granted-ability, freeze, architecture, simulation-copy, targeting-consistency,
  pipeline and legacy-binding classes (27 classes; 682 tests, 695 at HEAD: 23 deleted cases, 10 new): 29 failures,
  exactly the tests that fail at HEAD (`4ee79ba960`, same classes run in a worktree with the main tree's
  `card-data-cache`): mock wiring in `StepTriggerServiceTest`, `SpellCastTriggerCollectorServiceTest`,
  `PlanechaseServiceTest`, `ShuffleHandIntoLibraryAndDrawEffectHandlerTest` (3) and `DrawServiceTest` (the
  additional-trigger copy), and the known ones in `CardImmutabilityArchTest`, `SimulationCopyCompletenessTest` and
  `CardEffectTargetingConsistencyTest`.
- AI: the whole module (1,620 tests): the same 10 failures as steps 7 to 13 run 6 (the modal-mode, kicker and Reign of
  Chaos choices in the four AI decision engine tests and the two `TargetPolarityGuardTest` cases).
- The full suite was not run here.

#### Run 8: tap and untap

The 16 tap and untap constants are gone: `ON_SELF_TAPPED_FOR_MANA`, `ON_ANY_PLAYER_TAPS_LAND`,
`ON_ANY_PLAYER_TAPS_PERMANENT_FOR_MANA`, the three "you tap a creature / nonland permanent / artifact token for mana"
slots; `ON_ENCHANTED_PERMANENT_TAPPED`, `ON_ALLY_PERMANENT_BECOMES_TAPPED`, `ON_OPPONENT_PERMANENT_BECOMES_TAPPED`,
`ON_ANY_CREATURE_BECOMES_TAPPED` and `ON_CONTROLLER_TAPS_OPPONENT_PERMANENT`; `ON_SELF_TAPPED_TO_PAY_TEAMWORK_COST`;
`ON_SELF_BECOMES_UNTAPPED`, `ON_ALLY_PERMANENT_BECOMES_UNTAPPED`, `ON_ANY_PERMANENT_BECOMES_UNTAPPED` and
`ON_CONTROLLER_UNTAPS_DURING_UNTAP_STEP`. Family 9 (life) is next; families 9 to 11 are left.

**Authoring.**

- `model.trigger.TapTrigger(TapEvent event, ObjectRelation relation, PermanentPredicate filter)`: a permanent is tapped
  (**CR 701.26a**, checked) or untapped (**CR 701.26b**, checked); "becomes tapped / untapped" only on a change of
  state (**CR 603.2e**, checked). `TapEvent` is `TAPPED_FOR_MANA`, `BECOMES_TAPPED`, `YOU_TAP`,
  `TAPPED_TO_PAY_TEAMWORK_COST` (**CR 702.194a**, checked), `BECOMES_UNTAPPED` and `YOU_UNTAP_DURING_UNTAP_STEP`
  (**CR 502.3**, checked); the relation names the tapped or untapped permanent (`THIS`, `YOU_CONTROL`,
  `OPPONENT_CONTROLS`, `ANY`, `ENCHANTED`); the filter is its type (`Triggers.LAND`, `CREATURE`, `NONLAND` and the new
  `ARTIFACT_TOKEN`), which the collectors check in code as before. Every event functions from the battlefield.
- One named `Triggers` factory per slot (16): `tappedForMana()`, `playerTapsLandForMana()`,
  `playerTapsPermanentForMana()`, `youTapCreatureForMana()`, `youTapNonlandPermanentForMana()`,
  `youTapArtifactTokenForMana()`, `enchantedPermanentBecomesTapped()`, `permanentYouControlBecomesTapped()`,
  `permanentOpponentControlsBecomesTapped()`, `creatureBecomesTapped()`, `youTapPermanentOpponentControls()`,
  `tappedToPayTeamworkCost()`, `becomesUntapped()`, `permanentYouControlBecomesUntapped()`, `permanentBecomesUntapped()`,
  `youUntapPermanentsDuringUntapStep()` (`scripts/step13-tap-slots.tsv` maps each slot to its factory, event and
  relation).
- **Kept as the slots were:** `playerTapsLandForMana()` holds every land-tap wording (any player, you, an opponent,
  enchanted land, this land, a land of a type); the effect class still narrows it, and "an opponent" still means the
  tapping player. `enchantedPermanentBecomesTapped()` is read on every permanent attached to the tapped one, so
  Darksteel Garrison's "fortified land" uses it. `youTapPermanentOpponentControls()` still only sees taps made while a
  spell or ability resolves (the tapping player is the resolving controller), so costs and attacks never trigger it.
  The step 1 inventory's surprises are unchanged: effect-driven mass taps through `TapUntapSupport#tapPermanent` are
  unbatched, so a `TriggerMode.ONCE_PER_BATCH` registration (Deeproot Pilgrimage, Rewrite History, Sharae of Numbing
  Depths) fires once per permanent there; the opponent-tap collector has no permanent or player target step; about 9
  direct `Permanent.untap()` calls (untap-symbol costs, `MultiplePermanentUntapCostHandler`) bypass the untap
  collectors; the any-player-taps-permanent collector re-finds the tapped permanent and skips one sacrificed in the
  cost (Treasure); the ability-loss checks differ per collector.
- `LegacySlotGrouping.ALL` lost `ON_SELF_TAPPED_FOR_MANA`; `LegacySlots.slotOf` knows `TapTrigger`.
- `RegisterGlobalTriggeredAbilityUntilNextTurnEffect` (Don't Move) and the perpetual land grants
  (`PerpetuallyGiveLandExtraManaEffectHandler`, `PerpetuallyGrantTapDrawToRandomLandInLibraryEffectHandler`) hold or
  add the ability with its trigger; the temporary global watchers are matched with `ability().hasTrigger(…)`.

**No rules event, no index.** As in runs 1 to 7, the tap, untap and tap-for-mana collectors in
`TriggerCollectionService` still run at their call sites (the ~34 tap call sites, `TapUntapSupport#untapPermanent`,
`UntapStepService`, the two mana-ability paths, the teamwork cost). `service/trigger/TapWatch` (a `TriggerWatch`) has
one constant per old slot, naming its trigger and the zone the collector reads. The 44 handler registrations of
`LandTapTriggerCollectorService`, `ManaTapTriggerCollectorService` and `MiscTriggerCollectorService` moved from
`@CollectsTrigger(slot = …)` to `@CollectsTapTrigger(watch = …)` (`scripts/step13-tap-rekey.py`), scanned by
`TriggerCollectorRegistry` and `GameEngineConfig`. The readers in the engine and AI (`PredicateEvaluationService`,
`PotentialManaService`, `PermanentRemovalService`, `AiManaManager`, `AiTargetSelector`, `GameSimulator`) and the
collector tests were converted by `scripts/step13-tap-readers.py` (run 7's reader script on this family's TSV); by hand:
`checkNonlandPermanentTapForManaTriggers` loops over `TapWatch`es instead of a `List<EffectSlot>`. Grouping, target
routing, wrapper checks, face-down and ability-loss checks are unchanged, so the run makes no rules change. Shadow mode
was not run, for run 1's reason.

**Cards.** `scripts/step13-tap-triggers.py` (run 7's script reading the tap TSV) converted 207 card classes (the step 1
inventory's count), the two perpetual-grant handlers and `AiManaManagerTest`, one ability per registration in the old
order, any `TriggerMode` kept (3 `ONCE_PER_BATCH`). No card test named a tap slot. Nothing needed `MANUAL`. The domain
Javadoc that named a slot names its factory; engine comments and the collector tests' display names keep the slot
name, which is the `TapWatch` constant's. `scripts/step13-tap-docs.py` rewrote agent-docs (162 lines); by hand: three
lines that still said "slot" or showed the old `RegisterGlobalTriggeredAbilityUntilNextTurnEffect(EffectSlot, …)`
signature, the skills and `ARCHITECTURE.md`. `effect-slot-inventory.md` was re-rendered with `--keep-baseline`.

**For later runs.**

- Step 14: the tap collectors still read their wrappers: `TriggeringPermanentConditionalEffect` (the tapped or
  untapped permanent's type, "this creature" for a permanent-you-control trigger), `OncePerTurnTriggerEffect`,
  `OncePerTurnPerCreatureTriggerEffect` (global, "first time that creature has become tapped this turn"), the
  intervening-if `ConditionalEffect`s, `ChooseOneAtTriggerTimeEffect`, and `TriggerMode.ONCE_PER_BATCH` with
  `begin/endPermanentTapTriggerBatch`.
- Step 15: the tapped or untapped permanent (`triggeringPermanentId`), its controller (baked `targetId` for
  `TRIGGERING_PERMANENT_CONTROLLER` damage, `eventPlayerIds`), the land-tap context (tapping player, produced colors)
  and the untap-step count (`eventValue`) are still bound by the collectors.
- The many tap and untap call sites are the obvious choke points for a tap rules event when the collectors go.
- Tests that used `ON_SELF_BECOMES_UNTAPPED` as an arbitrary legacy slot use `ON_SELF_LOSES_CONTROL` (family 11) now.

**Tests changed.**

- New `TapTriggerTest` (`model/trigger/`): every factory functions from the battlefield and names its own trigger,
  the events, relations and filters, null rejection, abilities read by trigger and zone, and a registration keeping
  `TriggerMode.ONCE_PER_BATCH`.
- New `TapWatchTest`: every tap, library, target, counter, damage, combat and spell-and-ability watch has its own
  (trigger, zone) pair, every tap watch reads a `TapTrigger` on the battlefield, and a watch reads only its trigger's
  abilities.
- `LandTapTriggerCollectorServiceTest`, `MiscTriggerCollectorServiceTest` and `TriggerCollectorRegistryTest` dispatch
  to the watches (by the reader script); `AiManaManagerTest` adds the triggers (by the card script).
- `TriggeredAbilityTest`, `TriggerConditionTest`, `CardAbilitiesTest`, `GrantedAbilitiesTest`, `LegacySlotTriggerTest`
  and `GameDataDeepCopyTest` used `ON_SELF_BECOMES_UNTAPPED` as an arbitrary battlefield, each-grouped legacy slot:
  they use `ON_SELF_LOSES_CONTROL`. `LegacySlotTriggerTest`'s per-slot case runs 16 fewer times (it is parameterised
  over `EffectSlot.values()`).

**Test runs.**

- Card: every test class of a converted card and every card test that references one (388 classes, 3,518 tests): all
  passing.
- Application: the trigger-model, watch, registry, land-tap and misc collector, card-ability, granted-ability,
  activation, activated-ability execution, untap step, permanent removal, spell casting, tap-cost, step, turn-cleanup,
  freeze, architecture, simulation-copy, targeting-consistency, pipeline and legacy-binding classes (25 classes; 929
  tests, 936 at HEAD: 16 deleted cases, 9 new): 138 failures, exactly the tests that fail at HEAD (`4d70229c9e`, same
  classes run in a worktree with the main tree's `card-data-cache`): mock wiring in `AbilityActivationServiceTest`,
  `ActivatedAbilityExecutionServiceTest`, `PermanentRemovalServiceTest`, `SpellCastingServiceTest`,
  `LandTapTriggerCollectorServiceTest` (2), `StepTriggerServiceTest`, `TurnCleanupServiceTest` and
  `UntapStepServiceTest`, and the known ones in `CardImmutabilityArchTest`, `SimulationCopyCompletenessTest` and
  `CardEffectTargetingConsistencyTest`.
- AI: the whole module (1,620 tests): the same 10 failures as steps 7 to 13 run 7 (the modal-mode, kicker and Reign of
  Chaos choices in the four AI decision engine tests and the two `TargetPolarityGuardTest` cases).
- The full suite was not run here.

#### Run 9: life

The 8 life constants are gone: `ON_CONTROLLER_GAINS_LIFE`, `ON_OPPONENT_GAINS_LIFE`, `ON_ANY_PLAYER_GAINS_LIFE`,
`ON_CONTROLLER_LOSES_LIFE`, `ON_OPPONENT_LOSES_LIFE`, `ON_CONTROLLER_PAYS_LIFE` and the two graveyard life-gain slots
(`GRAVEYARD_ON_CONTROLLER_GAINS_LIFE`, `GRAVEYARD_ON_OPPONENT_GAINS_LIFE`). Family 10 (sagas) is next; families 10 and
11 are left. `ON_OPPONENT_PAYS_TAX` is a mana payment, not a life one, and stays in family 11.

**Authoring.**

- `model.trigger.LifeTrigger(LifeEvent event, PlayerRelation player)`: a player gains, loses or pays life
  (**CR 119.3**, **119.4**, checked); damage to a player is a loss of life (**CR 120.3a**, checked), and gaining 0 life
  is no event (**CR 119.9**, checked). `LifeEvent` is `GAINS`, `LOSES` and `PAYS`; the player is whose life total it
  is. Every event functions from the battlefield by default.
- One named `Triggers` factory per player and event (6; the two graveyard forms reuse the battlefield factory with a
  `TriggerCondition` zone): `youGainLife()`, `opponentGainsLife()`, `playerGainsLife()`, `youLoseLife()`,
  `opponentLosesLife()`, `youPayLife()` (`scripts/step13-life-slots.tsv` maps each slot to its factory, zone, event and
  player).
- **Kept as the slots were:** "the first time each turn" is still `OncePerTurnTriggerEffect` (with `firstLifeGain` /
  `firstLifeLoss`) and an intervening "if" a `ConditionalEffect`, unwrapped by the collectors. `youPayLife()` and
  `youLoseLife()` stay separate events, as the slots were: paying life is also a loss of life (CR 119.4), and the
  engine calls the life-loss collector for payments as before. The step 1 inventory's surprises are unchanged: the
  life collectors have no `CardEffect` default handler (an effect class without a handler never triggers); the
  opponent-loses-life and controller-loses-life collectors read no granted abilities, and the latter dispatches
  through the registry directly (no additional-trigger copies); the pay-life collector has no ability-loss check on
  printed abilities; the duplicate `MayPayManaEffect` and `SequenceEffect` handlers in `MiscTriggerCollectorService`
  and `LifeGainTriggerCollectorService` still overwrite each other in bean scan order (the rekey kept each on its bean,
  so the same one wins).
- `LegacySlots.slotOf` knows `LifeTrigger`; no life slot was in `LegacySlotGrouping.ALL`.
- False Cure's temporary global ability holds `new TriggeredAbility(Triggers.playerGainsLife(), …)` and is matched with
  `ability().hasTrigger(…)`; `SpecializeShadowheartEffectHandler` adds the specialized card's ability with
  `addTrigger(Triggers.youLoseLife(), …)`.

**No rules event, no index.** As in runs 1 to 8, `checkLifeGainTriggers`, `checkLifeLossTriggers`,
`checkLifePaymentTriggers` and the two graveyard life-gain collectors in `TriggerCollectionService` still run where the
slots were collected. `service/trigger/LifeWatch` (a `TriggerWatch`) has one constant per old slot, naming its trigger
and the zone the collector reads (the graveyard forms read `getEffectiveGraveyardTriggerEffects`, which only returns
abilities that function from the graveyard). The 52 handler registrations of `MiscTriggerCollectorService` and
`LifeGainTriggerCollectorService` moved from `@CollectsTrigger(slot = …)` to `@CollectsLifeTrigger(watch = …)`
(`scripts/step13-life-rekey.py`), scanned by `TriggerCollectorRegistry` and `GameEngineConfig`. The readers in
`TriggerCollectionService` and the collector tests were converted by `scripts/step13-life-readers.py` (run 8's reader
script on this family's TSV). Grouping, target routing, wrapper checks and ability-loss checks are unchanged, so the
run makes no rules change. Shadow mode was not run, for run 1's reason.

**Cards.** `scripts/step13-life-triggers.py` (run 8's script reading the life TSV) converted 105 card classes (the step
1 inventory's count) and `SpecializeShadowheartEffectHandler`, one ability per registration in the old order. No card
test named a life slot. Nothing needed `MANUAL`; the continuation line of four multi-line grants (False Cure,
Field-Tested Frying Pan, Light of Promise, Sunbond) was re-indented by hand. The domain Javadoc that named a slot names
its factory; engine comments and the collector tests' display names keep the slot name, which is the `LifeWatch`
constant's. `scripts/step13-life-docs.py` rewrote agent-docs (59 lines); by hand: False Cure's three rows show
`new TriggeredAbility(Triggers.playerGainsLife(), …)` (the docs script wrote the old `(slot, effect)` signature), the
"Slot:" labels in `EFFECTS_INDEX.md` read "Trigger:", the skills and `ARCHITECTURE.md`. `effect-slot-inventory.md` was
re-rendered with `--keep-baseline`.

**For later runs.**

- Step 14: the life collectors still read their wrappers: `OncePerTurnTriggerEffect` (`firstLifeGain`,
  `firstLifeLoss`, `markOnAcceptance`, the unkeyed once flag, keyed by card id from the graveyard),
  `FirstOpponentLifeLossEachTurnTriggerEffect`, and the intervening-if `ConditionalEffect`s
  (`unwrapLifeChangeConditional`). The single-life-lost `SequenceEffect` check (Ob Nixilis) is a trigger condition too.
- Step 15: the gaining, losing or paying player (`targetId`), the amount (`eventValue`) and the life-gain source card
  and its stack entry type (`DealDamageOnSpellLifeGainEffect`) are still bound by the collectors.
- The duplicate handlers above should be merged when the collectors go; Marina Vendrell's Grimoire depends on the
  `MiscTriggerCollectorService` `SequenceEffect` handler that sets the event value.

**Tests changed.**

- New `LifeTriggerTest` (`model/trigger/`): every factory functions from the battlefield and names its own trigger,
  the events and players, null rejection, and a card's life-gain abilities read by trigger and zone (battlefield and
  graveyard).
- New `LifeWatchTest`: every life, tap, library, target, counter, damage, combat and spell-and-ability watch has its
  own (trigger, zone) pair, every life watch reads a `LifeTrigger` in the battlefield or (for the `GRAVEYARD_` forms)
  the graveyard, and a watch reads only its trigger's abilities from its zone.
- `MiscTriggerCollectorServiceTest` and `EnterTriggerCollectorServiceTest` dispatch to the watches (by the reader
  script) and add the abilities with `addTrigger(Triggers.youGainLife(), …)` (three calls, by hand: the reader script
  ran on them before the card script).
- `GrantedAbilitiesTest#aGlobalTriggerHoldsATriggeredAbility` used `ON_ANY_PLAYER_GAINS_LIFE` as an arbitrary legacy
  slot: it uses `ON_SELF_LOSES_CONTROL`. `LegacySlotTriggerTest`'s per-slot case runs 8 fewer times (it is
  parameterised over `EffectSlot.values()`).

**Test runs.**

- Card: every test class of a converted card and every card test that references one (168 classes, 1,358 tests), and
  the two Shadowheart tests (34 tests): all passing.
- Application: the trigger-model (`model.trigger.*`), watch, registry, misc and enter collector, pipeline,
  granted-ability, card-ability, triggered-ability, freeze, architecture, simulation-copy, targeting-consistency,
  legacy-binding, turn-cleanup and the six damage handler classes (510 tests, 510 at HEAD: 8 deleted cases, 8 new):
  48 failures, exactly the tests that fail at HEAD (`57ebe72889`, same classes run in a worktree with the main tree's
  `card-data-cache`): mock wiring in `TurnCleanupServiceTest` and the six damage handler tests, and the known ones in
  `CardImmutabilityArchTest`, `SimulationCopyCompletenessTest` and `CardEffectTargetingConsistencyTest`.
- AI: the whole module (1,620 tests): the same 10 failures as steps 7 to 13 run 8 (the modal-mode, kicker and Reign of
  Chaos choices in the four AI decision engine tests and the two `TargetPolarityGuardTest` cases).
- The full suite was not run here.

#### Run 10: sagas

The 8 Saga constants are gone: `SAGA_CHAPTER_I` to `SAGA_CHAPTER_VI`, `ON_SAGA_FINAL_CHAPTER_ABILITY_RESOLVES` and
`ON_SAGA_FINAL_CHAPTER_ABILITY_TRIGGERS`. Family 11 (everything else) is next and is the last one left.

**Authoring.**

- `model.trigger.ChapterTrigger(int chapter)`: a chapter ability "{rN}—[Effect]", which triggers when lore counters put
  on the Saga make their number go from less than N to at least N (**CR 714.2b**, checked); "{rN1}, {rN2}—" is two
  chapter abilities (**CR 714.2c**, checked). `numeral()` gives the printed Roman numeral (**CR 714.2a**, checked),
  which the engine's chapter names now use. `Triggers.chapter(n)`.
- `model.trigger.FinalChapterTrigger(FinalChapterEvent event)`: the final chapter ability (**CR 714.2e**, checked) of a
  Saga you control `TRIGGERS` or `RESOLVES`. `Triggers.finalChapterAbilityTriggers()` /
  `finalChapterAbilityResolves()`. "You control" is part of the trigger, as the collector checked it; no card needs
  another player yet.
- **One ability per chapter.** The chapter slots were grouped `ALL` (every registration in one ability).
  `Card.addTriggeredAbility` (so every `addTrigger` / `addSimultaneousTrigger`) now adds an ability whose only
  condition is a `ChapterTrigger` to the card's ability of that chapter when there is one, so each registration is
  converted on its own and helpers that add a chapter piece by piece keep working. When merging, the chapter keeps its
  own targets, or takes the added ability's when it has none (never throws: copy code re-adds a chapter one effect at
  a time, each piece holding the chapter's targets).
- The chapter target API takes the chapter number: `setSagaChapterTargetFilter(int, …)`,
  `setSagaChapterTargetRequired(int)`, `setSagaChapterTargetGroups(int, …)` and their getters. The targets are still
  held by the chapter's `TriggeredAbility` (`SagaChapterTargets`), so no new `Card` state.
- `Card.getSagaFinalChapter()` is the greatest chapter among the card's chapter abilities, 0 when it has none
  (**CR 714.2d**, checked), instead of six presence checks; `SagaChapterService.maximumChapter` (the read-ahead
  maximum) is gone in its favour. Same values for chapters 1 to 6.
- `LegacySlotGrouping.ALL` lost the six chapter slots; `LegacySlots.slotOf` knows both new triggers.

**No rules event, no index.** As in runs 1 to 9, the chapter readers still run where the slots were read:
`SagaChapterService.triggerSagaChapter`, `StepTriggerService.handleSagaLoreCounters`, the
`PermanentCounterSupport.triggerSagaChapter` fallback, `ExileAndReturnTransformedService.initializeReturnedSaga`
(chapter I), `TargetLegalityService.sagaChapterIgnoresCardTargetConstraint` (loops over the card's chapters instead of
slot names starting with `SAGA_CHAPTER`) and `TriggeredAbilityQueueService.sagaChapterTargetRequired`, now reading
`card.getTriggerEffects(Triggers.chapter(n))` and the chapter's targets by number. Copied chapters read the card
without a zone, as before. `StackResolutionService.triggerSagaChapter` and its `sagaChapterAnyTargetFilter` were dead
code (no callers; the step 1 inventory's surprise) and were deleted rather than converted. The final-chapter
collectors in `TriggerCollectionService` dispatch through `service/trigger/SagaWatch` (a `TriggerWatch`, one constant
per old slot); `SagaTriggerCollectorService`'s two registrations moved from `@CollectsTrigger(slot = …)` to
`@CollectsSagaTrigger(watch = …)` (by hand: two lines), scanned by `TriggerCollectorRegistry` and `GameEngineConfig`.
The step 1 inventory's other Saga surprises are unchanged: the duplicated chapter logic in `StepTriggerService` and
`SagaChapterService`, `initializeReturnedSaga` pushing chapter I without targeting, `addLoreCounterAndTriggerChapter`
triggering only the final count, `PermanentCounterSupport` giving the chapter to the effect's controller, proliferate
adding lore counters without triggering chapters, no ability-loss or face-down check, the final chapter recognised by
the stack entry's description, and the final-chapter "triggers" check skipped for a modal final chapter. Shadow mode
was not run, for run 1's reason.

**One rules fix: a copied Saga keeps its chapters' targets.** The chapter targets live only on the chapter's ability,
and every copy path (token copies, "enter as a copy", full-text copies, mutate) rebuilt chapters through
`addEffect(slot, …)`, which made a new ability without them: a copy of Time of Ice offered the copier's own creatures
to its "tap target creature an opponent controls" chapter, and lost "required" flags and multi-target groups. The copy
paths now re-add the ability whole (`addTriggeredAbility`), so the copy has the chapter as printed (**CR 707.2**,
checked). Test: `CopyEnchantmentTest#copiedSagaChapterKeepsItsTargetRestriction` (fails at HEAD, `d57650d94c`).

**Cards.** `scripts/step13-saga-triggers.py` (reading `scripts/step13-saga-slots.tsv`) converted 217 card classes:
768 `addTrigger(Triggers.chapter(n), …)` and 3 `addSimultaneousTrigger` registrations (chained on target groups too),
150 chapter target calls, and the 3 final-chapter cards (Tom Bombadil; Narci, Fable Singer; Historian's Boon). In
card classes it also turns a helper parameter or loop variable of type `EffectSlot` holding a chapter into an `int`
chapter number (`addCopyChapter(int chapter)`, `for (int chapter : List.of(1, 2))`, `new int[]{1, 2, 3}`; 17 cards),
and its `--polish` pass renames such a parameter named `slot` to `chapter` and joins the loop's numbers on one line (run
as a post-pass here). Nothing needed `MANUAL`. It also converted the four application tests below.
`scripts/step13-saga-docs.py` rewrote agent-docs (81 lines; "Triggers.chapter(1)/II" chapter ranges in the pattern
tables then read "Chapters I/II"); by hand: the Saga section's authoring paragraph in
`CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md`, the chapter row of `TRIGGER_SLOT_TARGETING.md` (it named the deleted
`StackResolutionService` reader), the chapter V/VI and final-chapter rows of `ACTIVATED_ABILITY_GUIDE.md`, the skills
and `ARCHITECTURE.md`. `effect-slot-inventory.md` was re-rendered with `--keep-baseline`.

**For later runs.**

- A Saga that enters as a copy through "enter as a copy" (Copy Enchantment) gets no lore counter and no chapter I:
  that path doesn't call `SagaChapterService.initializeSaga` (CR 714.3a: a Saga enters with a lore counter). It was so
  at HEAD; the new test reaches chapter I through the draw-step lore counter instead. Fix it on purpose with a test.
- Step 14: none of the chapter readers take wrappers off the chapter's effects; the chapter's `ConditionalEffect`s,
  `MayEffect`s and the lone `ChooseOneAtTriggerTimeEffect` / `ChooseOneAtRandomEffect` handling stay with the readers.
  The final-chapter watchers still read `OncePerTurnTriggerEffect` (Tom Bombadil).
- Step 15: the final-chapter watchers' event value (the Saga's mana value) is still bound by
  `SagaTriggerCollectorService`. Chapter target selection still goes through the Saga-specific
  `PermanentChoiceContext.SagaChapterTarget` / `SagaChapterPlayerTarget` / `SagaChapterGraveyardTarget` records, which
  the generalized trigger target collector should absorb, together with `SagaChapterTargets` on `TriggeredAbility`.
- Chapters become rules events (a lore counter put on a permanent, CR 714.2b's threshold) when the readers go; the
  lore-counter choke points are the ones listed above.

**Tests changed.**

- New `SagaTriggerTest` (`model/trigger/`): default zones, chapter numbers and numerals, the chapter-number check, the
  final-chapter events, the effects of one chapter (and simultaneous effects) forming one ability, a chapter re-added
  effect by effect keeping its targets, a target-less chapter taking an added ability's targets, and the final chapter
  as the greatest chapter.
- New `SagaWatchTest`: every Saga, life, tap, library, target, counter, damage, combat and spell-and-ability watch has
  its own (trigger, zone) pair, every Saga watch reads a `FinalChapterTrigger` on the battlefield, and a watch reads
  only its trigger's abilities.
- New `CopyEnchantmentTest#copiedSagaChapterKeepsItsTargetRestriction` (the rules fix above).
- `CardAbilitiesTest`, `StepTriggerServiceTest`, `StackResolutionServiceTest` and `StateBasedActionServiceTest` add
  chapters with `addTrigger(Triggers.chapter(n), …)` and set their targets by number (by the card script);
  `CardAbilitiesTest#chapterTargetsNeedTheChapterEffectsFirst` expects the new message ("no chapter 2 ability") instead
  of the slot name. `LegacySlotTriggerTest`'s per-slot case runs 8 fewer times (it is parameterised over
  `EffectSlot.values()`).

**Test runs.**

- Card: every test class of a converted card and every card test that references one, mentions `CounterType.LORE` or
  a Saga (302 classes, 3,128 tests): all passing; `CopyEnchantmentTest` (5 tests) passing.
- Application: the trigger-model (`model.trigger.*`), watch, registry, step-trigger, stack-resolution, state-based
  action, card-ability, granted-ability, triggered-ability, freeze, architecture, simulation-copy,
  targeting-consistency, legacy-binding, pipeline and deep-copy classes (566 tests): 32 failures, exactly the tests that
  fail at HEAD (`d57650d94c`, `StateBasedActionServiceTest` and `StepTriggerServiceTest` rerun in a worktree with the
  main tree's `card-data-cache`): mock wiring in `StateBasedActionServiceTest` (13) and `StepTriggerServiceTest` (16),
  and the known ones in `CardImmutabilityArchTest`, `SimulationCopyCompletenessTest` and
  `CardEffectTargetingConsistencyTest`. After the merge rule changed, the trigger-model, Saga watch, card-ability,
  freeze and registry classes again (200 tests): all passing.
- AI: the whole module (1,620 tests): the same 10 failures as steps 7 to 13 run 9 (the modal-mode, kicker and Reign of
  Chaos choices in the four AI decision engine tests and the two `TargetPolarityGuardTest` cases).
- The full suite was not run here.

#### Run 11a: player and keyword actions

Family 11 is 69 constants of unrelated kinds, so it is split. Run 11a moved the player and keyword actions (the
table's first bullet), 45 constants: `ON_CONTROLLER_SETS_SCHEME_IN_MOTION`, `ON_CONTROLLER_GIVES_GIFT`,
`ON_ALLY_CASE_SOLVES`, `ON_CONTROLLER_FORETELLS`, `ON_CONTROLLER_FORAGES`, `ON_PLAYERS_FINISH_VOTING`,
`ON_CONTROLLER_TEMPTS_RING`, `ON_RING_TEMPTS_YOU`, `ON_CONTROLLER_INVESTIGATES` and `…_INVESTIGATES_EACH_TIME`,
`ON_CONTROLLER_MANIFESTS_DREAD`, `ON_CONTROLLER_COMPLETES_DUNGEON` (and its graveyard form), the dice slots
(`ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE`, `…_ROLLS_DIE`, `…_ROLLS_HIGHEST_NATURAL_RESULT`,
`GRAVEYARD_ON_CONTROLLER_ROLLS_NATURAL_20`), `ON_CONTROLLER_BENDS`, `ON_CONTROLLER_EXERTS`,
`ON_CONTROLLER_COLLECTS_EVIDENCE`, `ON_CONTROLLER_DISCOVERS`, `ON_CONTROLLER_SEEKS`, `ON_CONTROLLER_CONJURES` (and its
graveyard form), the two monarch slots, the three coin-flip slots, `ON_OPPONENT_PAYS_TAX`, `ON_CONTROLLER_CLASHES`,
`ON_PLAYER_LOSES_GAME`, the three crime slots, `ON_ALLY_CREATURE_FIGHTS`, `ON_ALLY_CREATURE_EXPLORES`,
`ON_ALLY_CREATURE_CONNIVES`, `ON_EXPLOIT`, `ON_ALLY_CREATURE_EXPLOITS` and `…_EXPLOITS_NONTOKEN_CREATURE`,
`ON_CHAMPIONED`, `ON_SELF_BECOMES_PLOTTED`, `ON_SELF_MUTATES` and `ON_ALLY_CREATURE_MUTATES`.

**Left for the next run (11b), 24 constants:** Planechase (`PLANESWALK_TO_TRIGGERED`, `PLANESWALK_FROM_TRIGGERED`,
`CHAOS_TRIGGERED`, `ENCOUNTER_TRIGGERED`), control change (`ON_SELF_BECOMES_CONTROLLED`, `ON_SELF_LOSES_CONTROL`,
`ON_OPPONENT_GAINS_CONTROL_OF_YOUR_PERMANENT`), attaching (`ON_AURA_ATTACHED_TO_SELF`,
`ON_ALLY_AURA_ATTACHED_TO_ALLY_CREATURE`, `ON_ALLY_AURA_ATTACHED_TO_OPPONENT_NONLAND_PERMANENT`,
`ON_EQUIPMENT_ATTACHED`, and `ON_EQUIPMENT_ATTACHED_TO_CREATURE`, which nothing uses), crew, saddle and station
(`ON_CREWS_VEHICLE`, `ON_ALLY_CREATURE_CREWS_VEHICLE`, `ON_SELF_SADDLES_OR_CREWS_DURING_MAIN_PHASE`,
`ON_SELF_BECOMES_STATIONED`, `ON_SELF_BECOMES_CREWED`, `ON_SELF_BECOMES_SADDLED`), day and night
(`ON_DAY_NIGHT_CHANGE`, `GRAVEYARD_ON_DAY_NIGHT_CHANGE`), Rooms (`ON_ALLY_ROOM_FULLY_UNLOCKED`,
`GRAVEYARD_ON_ALLY_ROOM_FULLY_UNLOCKED`, `ON_SELF_ROOM_DOOR_UNLOCKED`) and state triggers (`STATE_TRIGGERED`, a trigger
that watches a game state, **CR 603.8**, checked). When the last of them goes, `EffectSlot` has no constant, so the application and
AI tests that exercise the legacy slot machinery with an arbitrary slot (`ON_SELF_LOSES_CONTROL`,
`ON_SELF_BECOMES_CONTROLLED`, `GRAVEYARD_ON_DAY_NIGHT_CHANGE`; `TriggeredAbilityTest`, `TriggerConditionTest`,
`CardAbilitiesTest`, `GrantedAbilitiesTest`, `LegacySlotTriggerTest`, `GameDataDeepCopyTest`) must be rewritten or
deleted with the machinery they test: run 11b overlaps step 16 there.

**Authoring.**

- `model.trigger.PlayerActionTrigger(PlayerAction action, PlayerRelation player)`: a player takes a keyword or game
  action, or one happens to them: sets a scheme in motion, gives a gift, solves a Case, foretells, forages, finishes
  voting, the Ring tempts them (two values, below), investigates (first time each turn, or every time), manifests
  dread, completes a dungeon, rolls one or more dice, rolls a die, rolls a die's highest natural result, rolls a
  natural 20, bends, exerts a creature, collects evidence, discovers, seeks, conjures, becomes the monarch, wins or
  loses a coin flip, pays a mana tax, clashes, loses the game, commits a crime (**CR 700.13**, checked).
- `model.trigger.ObjectActionTrigger(ObjectAction action, ObjectRelation object)`: a creature fights, explores,
  connives, exploits a creature (**CR 702.110b**, checked) or a nontoken creature, a creature is championed with this,
  a card becomes plotted, a creature mutates. "Becomes plotted" functions from exile, where a plotted card is
  (**CR 702.170a**, checked): it is the trigger's default zone, so its watch reads exile; the reader read the card
  without a zone, so nothing changes.
- One named `Triggers` factory per player and action (42; three of the four graveyard forms reuse the battlefield
  factory with a `TriggerCondition` zone, and the natural-20 one has its own): `youSetSchemeInMotion()`, `youGiveGift()`, `youSolveCase()`, `youForetell()`,
  `youForage()`, `playersFinishVoting()`, `ringTemptsYouAndYouChooseRingBearer()`, `ringTemptsYou()`,
  `youInvestigateFirstTimeEachTurn()`, `youInvestigate()`, `youManifestDread()`, `youCompleteDungeon()`,
  `youRollOneOrMoreDice()`, `youRollDie()`, `youRollHighestNaturalResult()`, `youRollNatural20()`, `youBend()`,
  `youExertCreature()`, `youCollectEvidence()`, `youDiscover()`, `youSeek()`, `youConjure()`, `youBecomeMonarch()`,
  `opponentBecomesMonarch()`, `youWinCoinFlip()`, `playerWinsCoinFlip()`, `youLoseCoinFlip()`, `opponentPaysTax()`,
  `youClash()`, `playerLosesGame()`, `youCommitCrime()`, `opponentCommitsCrime()`, `creatureYouControlFights()`,
  `creatureYouControlExplores()`, `creatureYouControlConnives()`, `exploits()`, `creatureYouControlExploits()`,
  `creatureYouControlExploitsNontokenCreature()`, `championsCreature()`, `becomesPlotted()`, `mutates()`,
  `creatureYouControlMutates()` (`scripts/step13-action-slots.tsv` maps each slot to its factory, zone, kind, action and
  relation).
- **Kept as the slots were:** "the Ring tempts you" is two triggers. `TemptTheRingEffect` fires
  `ringTemptsYouAndYouChooseRingBearer()` once the Ring-bearer is chosen (its handler keeps Gandalf's "a creature other
  than this" check), and `RingTemptsYouEffect` fires `ringTemptsYou()` (Sauron), so a card of one kind doesn't trigger
  from the other tempt effect: an existing inconsistency, for a run that fixes it on purpose. "For the first time each
  turn" investigating is still its own trigger (step 14 makes it an option). The step 1 inventory's surprises are
  unchanged (the losing-the-game triggers never resolve in a 2-player game, the clash and champion target routing, …).
- **One ability per registration group.** Six of these slots (`ON_ALLY_CREATURE_EXPLORES`, `ON_EXPLOIT`,
  `ON_CONTROLLER_CLASHES`, `ON_PLAYER_LOSES_GAME`, `ON_CHAMPIONED`, `ON_SELF_BECOMES_PLOTTED`) were grouped `ALL`. Their
  multi-effect cards (10, every one with adjacent registrations) now pass all the effects to one `addTrigger` call, so
  each card's ability is one ability; the collectors read the effects in the same order. No slot left in
  `EffectSlot` was grouped `ALL`, so `LegacySlotGrouping` and `Card`'s `ALL` branch (`indexOfFirstAbility`) were deleted:
  `addEffect(slot, …)` always adds an ability.
- `LegacySlots.slotOf` knows both new triggers.

**No rules event, no index.** As in runs 1 to 10, the collectors still run where the slots were collected:
`TriggerCollectionService`'s `check…Triggers` methods for each action, `GameOutcomeService` (losing the game) and
`PermanentChoiceBattlefieldHandlerService` (champion). `service/trigger/ActionWatch` (a `TriggerWatch`) has one
constant per old slot, naming its trigger and the zone the collector reads (graveyard forms from the graveyard,
plotted from exile). The 40 handler registrations in `MiscTriggerCollectorService`, `DamageTriggerCollectorService`
and the small dice, dungeon, Ring, investigate, seek, discover, conjure, forage, exert, scheme, voting, manifest-dread,
collect-evidence and bending collector services moved from `@CollectsTrigger(slot = …)` to
`@CollectsActionTrigger(watch = …)` (`scripts/step13-action-rekey.py`), scanned by `TriggerCollectorRegistry` and
`GameEngineConfig`. The readers were converted by `scripts/step13-action-readers.py` (run 9's reader script on this
family's TSV); by hand: `dispatchFaceUpSchemes` takes a `TriggerWatch` (its `EffectSlot` overload is gone), the
monarch collector picks an `ActionWatch`, and the Javadoc that named a slot (domain: the factory; engine: the
`ActionWatch` constant). Grouping, target routing, wrapper checks and ability-loss checks are unchanged, so the run
makes no rules change. Shadow mode was not run, for run 1's reason.

**Cards.** `scripts/step13-action-triggers.py` (run 9's card script on this family's TSV, plus a merge of adjacent
registrations of the six grouped slots) converted 149 card classes (155 action-trigger registrations, 4 of them from
the graveyard) and `FearOfChangeTest`, which read `ON_CONTROLLER_CONJURES`. Nothing needed `MANUAL`.
`scripts/step13-action-docs.py` rewrote agent-docs (85 lines); by hand: one "trigger slot" wording, the skills and
`ARCHITECTURE.md`. `effect-slot-inventory.md` was re-rendered with `--keep-baseline`.

**For later runs.**

- Step 14: the action collectors still read their wrappers: `OncePerTurnTriggerEffect` (crime, investigate),
  intervening-if `ConditionalEffect`s (monarch, dice), `TriggeringCardConditionalEffect` (explores, Case),
  `IfWonClashEffect` / `IfLostClashEffect` (clash results, which are event data rather than trigger conditions), and
  the first-time-each-turn investigate trigger.
- Step 15: the dice results (`eventValue`), the sought, conjured and discovered cards, the exploited, explored,
  conniving, fighting and mutating creatures, the tax payer and the losing player are still bound by the collectors.
- Fix on purpose: the two Ring triggers should be one ("Whenever the Ring tempts you") fired by both tempt effects.

**Tests changed.**

- New `ActionTriggerTest` (`model/trigger/`): default zones (exile for plotted), every factory naming its own
  trigger, actions and relations, null rejection, and a card's action abilities read by trigger and zone.
- New `ActionWatchTest`: every action, Saga, life, tap, library, target, counter, damage, combat and
  spell-and-ability watch has its own (trigger, zone) pair, every action watch reads an action trigger in a zone it
  functions from, and a watch reads only its trigger's abilities.
- `FearOfChangeTest` reads `getTriggerEffects(Triggers.youConjure())` (by the card script).
- `CardAbilitiesTest#aSlotGroupedAllGetsOneAbilityAtThePositionOfItsFirstRegistration` is deleted with the `ALL`
  grouping. `CardAbilitiesTest`, `GrantedAbilitiesTest`, `TriggeredAbilityTest`, `LegacySlotTriggerTest` and
  `GameDataDeepCopyTest` used `ON_CONTROLLER_CLASHES` and `GRAVEYARD_ON_CONTROLLER_COMMITS_CRIME` as arbitrary
  battlefield and graveyard legacy slots: they use `ON_SELF_BECOMES_CONTROLLED` and `GRAVEYARD_ON_DAY_NIGHT_CHANGE`.
  `LegacySlotTriggerTest`'s per-slot case runs 45 fewer times (it is parameterised over `EffectSlot.values()`).

**Test runs.**

- Card: every test class of a converted card and every card test that references one (334 classes, 2,664 tests):
  all passing.
- Application: the trigger-model (`model.trigger.*`), watch, registry, misc, damage and enter collector,
  card-ability, granted-ability, triggered-ability, freeze, architecture, simulation-copy, targeting-consistency,
  legacy-binding, pipeline and step-trigger classes (591 tests): 25 failures, all failing at HEAD: mock wiring in
  `StepTriggerServiceTest` (16) and `DamageTriggerCollectorServiceTest` (6, rerun at `d57650d94c` in a worktree with
  the main tree's `card-data-cache`), and the known ones in `CardImmutabilityArchTest`,
  `SimulationCopyCompletenessTest` and `CardEffectTargetingConsistencyTest`.
- AI: the whole module (1,620 tests): the same 10 failures as steps 7 to 13 run 10.
- The full suite was not run here.

#### Run 11b: the rest of family 11

The last 24 constants are gone: Planechase (`PLANESWALK_TO_TRIGGERED`, `PLANESWALK_FROM_TRIGGERED`, `CHAOS_TRIGGERED`,
`ENCOUNTER_TRIGGERED`), control change (`ON_SELF_BECOMES_CONTROLLED`, `ON_SELF_LOSES_CONTROL`,
`ON_OPPONENT_GAINS_CONTROL_OF_YOUR_PERMANENT`), attaching (`ON_AURA_ATTACHED_TO_SELF`,
`ON_ALLY_AURA_ATTACHED_TO_ALLY_CREATURE`, `ON_ALLY_AURA_ATTACHED_TO_OPPONENT_NONLAND_PERMANENT`, `ON_EQUIPMENT_ATTACHED`,
and `ON_EQUIPMENT_ATTACHED_TO_CREATURE`, which nothing used and which was deleted without a replacement), crew, saddle
and station (`ON_CREWS_VEHICLE`, `ON_ALLY_CREATURE_CREWS_VEHICLE`, `ON_SELF_SADDLES_OR_CREWS_DURING_MAIN_PHASE`,
`ON_SELF_BECOMES_STATIONED`, `ON_SELF_BECOMES_CREWED`, `ON_SELF_BECOMES_SADDLED`), day and night (`ON_DAY_NIGHT_CHANGE`,
`GRAVEYARD_ON_DAY_NIGHT_CHANGE`), Rooms (`ON_ALLY_ROOM_FULLY_UNLOCKED`, `GRAVEYARD_ON_ALLY_ROOM_FULLY_UNLOCKED`,
`ON_SELF_ROOM_DOOR_UNLOCKED`) and `STATE_TRIGGERED`. `EffectSlot` is an empty enum now, so step 13 is done. It stays,
with `LegacySlotTrigger`, `LegacySlots`, the `addEffect(slot, …)` adapter, the slot-keyed grant, copy, perpetual and
registry paths and the `EffectSlot.values()` loops, all of them dead code that step 16 deletes.

**Authoring.**

- `model.trigger.PlanarTrigger(PlanarEvent event)`: "when you planeswalk to / away from this plane" (**CR 701.31b**,
  checked), "whenever chaos ensues" (**CR 311.7**, checked), "when you encounter this phenomenon" (**CR 312.5**,
  checked). It functions from the command zone by default (**CR 901.7**, checked); the planar readers read the card
  without a zone, as before (a chaos ability of a revealed card in the planar deck, CR 311.7, still triggers).
  `Triggers.planeswalkToThis()`, `planeswalkAwayFromThis()`, `chaosEnsues()`, `encounterThis()`.
- `model.trigger.ControlChangeTrigger(ControlEvent event, ObjectRelation object)`: `youGainControlOfThis()`,
  `youLoseControlOfThis()`, `opponentGainsControlOfPermanentYouControl()`.
- `model.trigger.AttachTrigger(AttachEvent event)`: `auraBecomesAttachedToThis()`,
  `auraYouControlBecomesAttachedToCreatureYouControl()`,
  `auraYouControlBecomesAttachedToNonlandPermanentOpponentControls()` (the collector still checks the mana values),
  `becomesAttachedToCreature()` (this Equipment).
- `model.trigger.DayNightTrigger()`: "whenever day becomes night or night becomes day" (**CR 731.1a**, checked),
  `Triggers.dayBecomesNightOrNightBecomesDay()`; the graveyard form is its `TriggerCondition` from the graveyard.
- `model.trigger.StateTrigger()`: a state trigger (**CR 603.8**, checked), `Triggers.state()`. Its game state is still
  its `StateTriggerEffect`'s condition. `Card.addTriggeredAbility` (so every `addTrigger`) rejects any other effect in
  an ability with a `StateTrigger` ("A state trigger requires a StateTriggerEffect"), where `addEffect` checked the
  `STATE_TRIGGERED` slot.
- New `ObjectActionTrigger` actions, reused rather than a new type: `CREWS_VEHICLE` (**CR 702.122a**, checked;
  `crewsVehicle()`, `creatureYouControlCrewsVehicle()`), `SADDLES_OR_CREWS_DURING_MAIN_PHASE` (**CR 702.171a**,
  checked), `BECOMES_CREWED` (**CR 702.122e**, checked), `BECOMES_SADDLED`, `BECOMES_STATIONED` (**CR 702.184a**,
  checked) and `DOOR_UNLOCKED` (**CR 709.5h**, checked; `doorUnlocked()`, the door is still the
  `TriggeringRoomDoorConditionalEffect`'s). New `PlayerActionTrigger` action `FULLY_UNLOCKS_ROOM` (**CR 709.5i**,
  checked; `youFullyUnlockRoom()`). `scripts/step13-misc-slots.tsv` maps each slot to its factory and zone.
- **Kept as the slots were:** "for the first time each turn" crewing and saddling is still an
  `OncePerTurnTriggerEffect`; Planechase cards author their step abilities with the battlefield default (run 1's note)
  while their planar abilities function from the command zone; state triggers on planes are read without a zone.
- `LegacySlots.slotOf` knows the five new trigger types. The private `TriggerCollectionService.dispatchSlot` overloads
  had no caller left and were deleted; `queueGraveyardEerieTriggers` takes a `TriggerWatch`;
  `PlanechaseService.trigger(…, EffectSlot, …)` is gone (the `Trigger` overload stays).

**No rules event, no index.** As in runs 1 to 11a, the collectors still run where the slots were collected:
`TriggerCollectionService` (control, attach, crew, saddle, station, day and night, Rooms), `CreatureControlService`
(gaining or losing control of this), `StateTriggerService`, `StackResolutionService` (a Room's door targets),
`CombatAttackService` (the delayed Vehicle attack) and `PlanechaseService` with the two chaos effect handlers.
`service/trigger/MiscWatch` (a `TriggerWatch`) has one constant per old slot except the four planar ones, which
`PlanechaseService` and the chaos handlers read through the `Triggers` factory directly (they had no handlers). The 9
handler registrations in `MiscTriggerCollectorService`, `EnterTriggerCollectorService` and
`DayNightTriggerCollectorService` moved from `@CollectsTrigger(slot = …)` to `@CollectsMiscTrigger(watch = …)`
(`scripts/step13-misc-rekey.py`), scanned by `TriggerCollectorRegistry` and `GameEngineConfig`; no
`@CollectsTrigger(slot = …)` registration is left. The readers were converted by `scripts/step13-misc-readers.py`; by
hand: the planar calls, the eerie helper, four long lines. Grouping, target routing, wrapper checks and ability-loss
checks are unchanged, so the run makes no rules change. Shadow mode was not run, for run 1's reason.

Also fixed: three Javadoc links to slots deleted by earlier runs (`GraveyardService.notifyCardLeftGraveyard`,
`EnchantedControllerSacrificesCreatureOnLeaveEffect`, `DrawCardForEachAuraAttachedToDyingCreatureEffect`).

**Cards.** `scripts/step13-misc-triggers.py` (run 11a's card script on this run's TSV, with no grouped slot; a slot
handed to `PlanechaseService.trigger` or to a test helper names the trigger) converted 293 card classes (339
registrations, 2 of them from the graveyard), 21 card tests and the application tests below. Nothing needed `MANUAL`.
By hand: the `EffectSlot` parameters of four card-test helpers (`TheCheetahPlanetTest`, `KetriaTest`,
`FurnaceLayerTest` take a `Trigger`; `CliffsideMarketTest` lost its slot overload), rejoining the short wrapped
`planar.trigger` calls of six card tests. 18 card classes and `BowieBaseOneTest` still import `EffectSlot` without
using it (as at HEAD); step 16 drops those imports. `scripts/step13-misc-docs.py` rewrote agent-docs (117 lines); by
hand: the day-and-night "trigger slot" rows, `PLANECHASE.md`'s "planar event slots", `ACTIVATED_ABILITY_GUIDE.md`'s
"EffectSlot quick reference" heading and scheme paragraph, the skills and `ARCHITECTURE.md`. `effect-slot-inventory.md`
was re-rendered with `--keep-baseline`.

**For later runs.**

- Step 14: the collectors of this run still read their wrappers: `OncePerTurnTriggerEffect` (crewed, saddled, the
  Equipment attach of Inchblade Companion), `TriggeringRoomDoorConditionalEffect` (which door), the intervening-if
  `ConditionalEffect`s of the planar reader, and `StateTriggerEffect`'s condition, which becomes the `StateTrigger`'s
  game state.
- Step 15: the crewed Vehicle, the stationing creature, the attached Aura, the new controller, the Room and the
  day/night change are still bound by the collectors (`triggeringPermanentId`, `TriggerContext` records).
- Step 16: with `EffectSlot` empty, `LegacySlotTrigger` can't be built, so the legacy slot paths (the adapter, the
  slot-keyed `Permanent` / `StackEntry` / `GameData` grant methods, `GrantEffectToTargetEffect`'s and
  `TemporaryGlobalTriggeredAbility`'s slot constructors and `slot()`, `LegacySlots.registrationAbilities`, the
  `EffectSlot.values()` loops in the copy and layer code, `GameData.CollectingTrigger.slot`, the shadow recorder) have no
  live caller and can go with the enum.

**Tests changed.**

- New `MiscTriggerTest` (`model/trigger/`): planar triggers function from the command zone and the rest from the
  battlefield, every factory names its own trigger, the events and relations, null rejection, a card's abilities read
  by trigger and zone, and a state trigger rejecting a non-`StateTriggerEffect`.
- New `MiscWatchTest`: every misc, action, Saga, life, tap, library, target, counter, damage, combat and
  spell-and-ability watch has its own (trigger, zone) pair, no misc watch reads a planar trigger, each reads its
  trigger in a zone it functions from, and a watch reads only its trigger's abilities.
- `StateTriggerServiceTest` (by the card script, then by hand): registers with `addTrigger(Triggers.state(), …)`; the
  rejection case expects the new message, and its name and display names no longer say "slot".
- `PlanechaseServiceTest` (by script; its `EffectSlot` fixture overload deleted), `SpellCastingServiceTest`,
  `StackResolutionServiceTest` (by script), `MiscTriggerCollectorServiceTest` (by the reader script; its one
  `addEffect` by hand) use the triggers and watches.
- The tests that used these slots as arbitrary legacy slots can't name a slot any more: `CardAbilitiesTest` adds and
  reads `Triggers.youLoseControlOfThis()` abilities instead (same cases, renamed from "slot" to ability or trigger;
  the two `EffectSlot.values()` loops became "no triggered abilities"). `GrantedAbilitiesTest` grants
  `TriggeredAbility` values through the ability forms of the same `Permanent`, `GameData`, `StackEntry`, grant and
  global-trigger APIs; `aSlotAndAnEffectStandForATriggeredAbility` became `aTriggeredAbilityWithARealTriggerIsInNoSlot`,
  and `simultaneousRegistrationsBecomeOneAbility` (`LegacySlots.registrationAbilities`) and the slot half of the
  `LegacySlots.effectsIn` / `grant` cases are deleted. `TriggeredAbilityTest` uses real triggers (default zones from
  `chaosEnsues()`, the command zone); its two `LegacySlotTrigger`-only cases (only condition, no options) are deleted.
  `LegacySlotTriggerTest` keeps only `needsASlot` (its `@EnumSource(EffectSlot.class)` and CSV cases had no slot left).
  `TriggerConditionTest` and the AI's `GameDataDeepCopyTest` use real triggers and the ability-based grant APIs.

**Test runs.**

- Card: every test class of a converted card and every card test that references one, plus the changed card tests
  (1,071 classes, 9,109 tests): all passing.
- Application: the trigger-model (`model.trigger.*`), watch, registry, misc and enter collector, state-trigger,
  planechase, stack-resolution, spell-casting, creature-control, card-ability, granted-ability, triggered-ability,
  freeze, architecture, simulation-copy, targeting-consistency, legacy-binding, pipeline, step-trigger and state-based
  action classes (774 tests): 45 failures, exactly the tests that fail at HEAD (`8982236074`, the same 8 classes rerun
  in a worktree with the main tree's `card-data-cache`): mock wiring in `StepTriggerServiceTest` (16),
  `StateBasedActionServiceTest` (13), `SpellCastingServiceTest` (10) and `CreatureControlServiceTest` (1),
  `PlanechaseServiceTest`'s two source-less planeswalking cases, and the known ones in `CardImmutabilityArchTest`,
  `SimulationCopyCompletenessTest` and `CardEffectTargetingConsistencyTest`.
- AI: the whole module (1,620 tests): the same 10 failures as steps 7 to 13 run 11a.
- The full suite was not run here.

### Step 14

Step 14 is repeatable by family (its section has the order). Run 1 converted the source's own zone changes, run 2
the creature-death family, run 3a the other zone and permanent changes but "another object enters", run 3b "another
object enters", run 4a the reading of the own enters abilities' options and the "this or another" enters merges, run 4b
the own enters abilities' intervening "if"s, run 4c the rules fixes: every own enters intervening "if" checked
as the ability triggers, run 5 the steps and phases, run 6a the spell, land-play and activation abilities' generic
wrappers, run 6b `SpellCastTriggerEffect`, as the cast trigger's filter, the ability's options and its effects, run
6c the other spell, copy and land-play condition-as-effect classes, as the trigger's filter, run 7 the combat
abilities (step 13's family 3), all of their wrapper kinds in one run, with the user; run 8 the damage abilities
(step 13's family 4), run 9 the counter abilities (family 5), run 10 the becomes-the-target abilities (family 6),
run 11 the library, draw and discard abilities (family 7), run 12 the tap and untap abilities (family 8), run 13 the
life abilities (family 9), run 14 the Saga abilities (family 10) and run 15 everything else (family 11: actions,
control change, attaching, crew, saddle and station, day and night, Rooms, Planechase, state triggers) the same way.
Run 16, what no family owns, is the last; it was split, with the user, into 16a (the leftovers that need no collector
rewritten) and 16b onward (the legacy view's retirement, family by family: 16b the spell-cast and spell-copy collectors).

#### Run 1: the source's own zone changes

Every "when this dies / is sacrificed / leaves the battlefield / is put into a graveyard from … / is milled" ability
(`ObjectRelation.THIS`, step 10's pilot) is free of trigger-only wrappers, and `LegacyTriggerOptions` is gone: the
pilot path reads only the ability's `TriggerOptions` and its trigger's filter.

**Authoring.**

- A condition on the object as it last existed is the trigger's filter: `Triggers.dies(Triggers.CREATURE,
  ObjectRelation.THIS)` ("if it was a creature"), `dies(new PermanentHasCountersPredicate(…), THIS)` ("if it had a
  counter on it"), `dies(new PermanentIsEnchantedPredicate(), THIS)`, `dies(new PermanentOwnedBySourceControllerPredicate(),
  THIS)` ("is put into your graveyard"), `leavesBattlefield(Triggers.TOKEN, THIS)`. Such a condition can't change
  before the ability resolves (CR 603.10a, checked), so a filter behaves as the intervening "if" (CR 603.4, checked)
  would. `ObjectRelation.thisAlwaysMatches()` no longer includes `THIS`: a `THIS` trigger's filter is about the source.
- Any other intervening "if" is `TriggerOptions.DEFAULT.withInterveningIf(condition)`, through the new
  `Card.addTrigger(Trigger, TriggerOptions, CardEffect...)` and `SpellTarget.addTrigger(Trigger, TriggerOptions,
  CardEffect...)`.
- An Oracle ability with two trigger conditions and a limit is one `TriggeredAbility` with both conditions, so the limit
  (keyed by the ability) covers both: Oni-Cult Anvil ("one or more artifacts you control leave … during your turn …
  only once each turn") and Minthara of the Absolute ("one or more permanents you control leave … only once each
  turn"), whose two halves shared the unkeyed `OncePerTurnTriggerEffect` mark before. `ZoneChangeWatch.ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD_DURING_CONTROLLER_TURN`
  gained the key `leavesBattlefield(Triggers.ARTIFACT, ANOTHER_YOU_CONTROL).onlyDuringYourTurn()` for Anvil's other
  condition (the watch body passes a plain effect through).
- Deleted: `DyingPermanentWasCreatureConditionalEffect`, `DyingPermanentWasNonlandCreatureConditionalEffect`,
  `SacrificeOnlyEffect` (and its handler; `Triggers.sacrificed(THIS)` already says it),
  `DrawCardIfDyingSourceHadCounterEffect` (Promising Duskmage: a filter and `DrawCardEffect(1)`), and
  `TriggerCollectionService`'s `unwrapSelfDeathConditional`, `passesSelfDeathFilters`, `passesSelfInterveningIf`,
  `passesDeathInterveningIf` and `deathConditionIsMet` (whose `SourceIsCreature` special case became the `CREATURE`
  filter).

**The engine.** `TriggerMatcher.relatesToItself` applies a `THIS` trigger's filter (`ownFilterMatches`): card types
from the last-known card (as the old `wasCreature` / `wasLand` read them; a layered query would read the game after
the departure), Auras and Equipment from the departure's `attachments`, power and toughness from the last-known
values, anything else through `PredicateEvaluationService` with the source as its own source (owner, combat damage
dealt this turn, modified), as the old `TriggeringPermanentConditionalEffect` unwrapping did.
`TriggeredAbilityStackService.triggerBound` hands the authored effect, wrapped in the option's intervening "if", to
`LegacyTriggerBindings`; "triggers only once each turn" (no extra copies, Clara Oswald) is read from the limit.
Triumph of Saint Katherine's "only from its controller's graveyard" check moved from `passesSelfDeathFilters` into
`LegacyTriggerBindings.dies` (a handler concern, step 15's). The shadow reporter reports the raw effect (dead code: no
slot is left to shadow). The two engine-built blitz abilities (`SpellCastingService` for a granted blitz,
`CardChoiceHandlerService` for a perpetual one) use the option.

**Cards.** `scripts/step14-pilot-options.py` (run per letter module; `scripts/step14-pilot-scan.py` lists the
abilities with their Oracle text) converted 69 card classes. `new ConditionalEffect(c, x)` became the intervening-if
option, except `SourceIsCreature` / `NotCondition(SourceIsToken)` (filters `CREATURE` / `NONTOKEN`) and
`SourceCardInGraveyard` (below); `ConditionalEffect.atTriggerTime`, `TriggeringPermanentConditionalEffect`,
`TriggeringCardConditionalEffect(LAND)` and the `DyingPermanentWas…` wrappers became filters; `SacrificeOnlyEffect`
was dropped. Its `MANUAL` table holds Oni-Cult Anvil, Minthara and Syr Vondam, Sunstar Exemplar ("dies … while its
power is 4 or greater": `dies(new PermanentPowerAtLeastPredicate(4), THIS)`; its exile half keeps the
`ConditionalEffect`, now bound to the target group with `bindEffect`). The last two kept their wrapper in a local
variable, which the first pass of the script didn't follow; the card tests caught them. `scripts/step14-pilot-docs.py`
rewrote agent-docs (14 rows); the implement-card and review-card skills were edited by hand.

**Behaviour changes.**

- Gamekeeper, Academy Rector and Antique Collector's perpetual grant ("you may exile it / shuffle it … if you do") wrap
  their effect in `ConditionalEffect(SourceCardInGraveyard, …)`. That isn't an intervening "if" (no "if" follows the
  trigger condition, CR 603.4), so the condition is no longer checked as the ability triggers, only on resolution: the
  ability now triggers when the card didn't reach the graveyard and does nothing. Their `interveningIf` flag is still
  `true` but no reader of the dies path looks at it any more (the leaves handlers still do); run 16 deletes the flag.
- Old-Growth Troll's and Bronzehide Lion's "was a creature" is judged on the card types they died with, not by a
  layered `isCreature` query after the death.
- Nothing else changes: the other conversions read the same conditions at the same times.

**Kept as they were, for later runs.**

- Blitz's "when this creature dies, draw a card" is the intervening-if option `CastForAlternateCost` on a printed
  ability (the permanent has the ability only as long as its blitz cost was paid, **CR 702.152a**, checked), as the
  `ConditionalEffect` was.
- Mongrel Pack's "dies during combat" is a trigger condition, kept as the intervening-if option `DuringCombat`: the
  ability resolves in the same combat step, so the second check doesn't change anything.
- Handler-level trigger conditions in `LegacyTriggerBindings` stay for step 15: `SelfExiledFromBattlefieldEffect` /
  `SelfExiledWhileActivatingCraftAbilityEffect` (and the intervening "if" they read from their wrapped
  `ConditionalEffect`), Brood of Cockroaches' "put into your graveyard", Triumph of Saint Katherine's graveyard check,
  Cathedral Membrane's "during combat".
- The "this or another" cards keep their split `THIS` halves (now with filters or options) beside their other-object
  halves: Zone of Flame, Resourceful Defense, Nadier's Nightblade, Costume Closet, Long Feng. Runs 2 to 4 merge them
  once their family's watches read filters.
- The `onlyIfSacrificed` flags on `DrawCardEffect`, `GainLifeEffect`, `DiscardEffect`, `DealDamageToAnyTargetEffect`,
  `SearchLibraryEffect`, `SequenceEffect` and `onlyTriggersOnSacrifice()` are unused by cards but still read by
  `TriggerCollectionService` and a few handlers; run 16 deletes them.

**Tests changed.** `TriggerPipelineTest`: three new tests through the engine, a limit covers every trigger condition of
its ability, a `THIS` filter ("had a +1/+1 counter") is judged on the permanent as it last existed, and an "if it was a
creature" filter reads the types it died with (an enchantment doesn't trigger it); its `zone` helper also reads the
library. No other test changed.

**Test runs.**

- Card: the test class of every card with an ability of this family plus Triumph of Saint Katherine, Brood of
  Cockroaches and Riveteers Provocateur (1,052 classes): first run 7 failures in three classes (Oni-Cult Anvil's
  unwatched filtered condition; Minthara and Syr Vondam, whose variables the script missed), then all passing.
- Application: `TriggerPipelineTest`, `LegacyTriggerBindingsTest`, `CardAbilitiesTest`, `ZoneChangeTriggerTest`,
  `TriggeredAbilityTest`, `TriggerConditionTest`, `TriggerOptionsTest`, `ZoneChangeRulesEventTest`, `RulesEventLogTest`,
  `CardFreezeTest`, `MiscTriggerTest`, `ActionTriggerTest`, `SpellCastingServiceTest`, `GrantedAbilitiesTest` and the
  ten `…WatchTest` classes: the only failures are `SpellCastingServiceTest`'s 10, which fail at HEAD.
- AI: the whole module (1,620 tests): the same 10 failures as steps 7 to 13 (the four decision engines' modal, kicker
  and Reign of Chaos cases, two `TargetPolarityGuardTest`).
- The full suite was not run here.

#### Run 2: creature death

Every "whenever [another] creature dies" ability that step 11's `CreatureDeathWatch` collects is free of trigger-only
wrappers but four (below), and the 32 "this creature or another [restricted] creature" cards step 11 left split are one
ability each. The creature-death collectors no longer read `TriggeringCardConditionalEffect`,
`TriggeringPermanentConditionalEffect`, `OncePerTurnTriggerEffect` or a chosen-mode `ConditionalEffect`.

**Authoring.**

- A restriction on the creature that died is the trigger's filter: `Triggers.creatureDies(new
  PermanentHasSubtypePredicate(ZOMBIE), ANOTHER_YOU_CONTROL)`, `nontokenCreatureDies(filter, R)`, `dies(new
  PermanentAllOfPredicate(List.of(Triggers.CREATURE_OR_PLANESWALKER, filter)), R)`. A card predicate becomes its
  permanent form (`CardSubtypePredicate` → `PermanentHasSubtypePredicate`, `CardColorPredicate(c)` →
  `PermanentColorInPredicate(Set.of(c))`, …).
- "This creature or another [restricted] creature" is `THIS_OR_ANOTHER_YOU_CONTROL` / `THIS_OR_ANOTHER` with the filter
  (the source matches whatever it was). An intervening "if" about whichever creature died ("if it had counters on it",
  "if it was enchanted or equipped") covers the source too, which such a filter can't say, so those are one
  `TriggeredAbility` with two trigger conditions and the same filter, `dies(filter, THIS)` and `creatureDies(filter,
  ANOTHER_YOU_CONTROL)`: Reluctant Role Model, Host of the Hereafter, Slurrk, Basri's Lieutenant, Cass, Hand of
  Vengeance. `SpellTarget.addTriggeredAbility` (new) binds such an ability to a target group.
- "Dies during your turn" is `.onlyDuringYourTurn()` (Vogar); any other intervening "if" is
  `TriggerOptions.withInterveningIf` (Mausoleum Harpy, Battle of Hoover Dam's and Phenomenon Investigators' chosen mode,
  Embalmed Ascendant's max speed, Pelt Collector's power comparison); "triggers only once each turn" is
  `TriggerLimit.ONCE_EACH_TURN`, "do this only once each turn" `DO_ONLY_ONCE_EACH_TURN` (CR 603.2h, checked: The Reaper,
  Puca's Covenant). A `ConditionalEffect(SourceCardInGraveyard, …)` stays an effect, as in run 1 (Snarlfang Vermin).

**The engine.**

- `TriggerMatcher.filterMatches` judges another object's departure with `departedMatches`, the observer counterpart of
  run 1's `lastKnownMatches`: last-known subtypes and colors from the departure, other characteristics from the
  last-known card, last-known power and toughness, the Auras and Equipment attached as it left, and anything else
  through `PredicateEvaluationService` with the watcher as the filter's source and the departed permanent as the
  triggering one (its id and last-known controller). That is the context the wrappers were judged in.
  `PermanentHasGreatestPowerAmongControllerCreaturesPredicate` reads the triggering permanent's controller from that
  context (Kraven, the Hunter), where the old opponent collector passed the dying creature's controller as the source's.
- `TriggeredAbilityStackService.triggerOnce` checks an intervening "if" with the moved permanent as the triggering one
  (its last-known power), and passes the ability's limit to `TriggerCollectionService.triggerCreatureDeath(s)`: it
  says whether accepting a "you may" uses up "do this only once each turn" and whether effects like Teysa's may add
  copies ("triggers only once each turn" can't). The limit itself is checked and marked by the pipeline, keyed by the
  ability, where the wrappers marked the source unkeyed.
- `CreatureDeathWatch.of` routes `OPPONENT_CONTROLS` before the nontoken check: `creatureDies(Not(token),
  OPPONENT_CONTROLS)` equals `NONTOKEN_CREATURE` and would otherwise reach the "any nontoken" collector (Shelob).
- The ally collector leaves the physical card that died out of its graveyard targets, so a copy's "another target
  creature card" no longer offers the copy's own card (Jackdaw Savior on a Mockingbird). Its separate stack entries
  for wrapped effects are gone: one ability is one stack object.
- `DeathTriggerCollectorService.handleAllyNontokenPermanentConditional` is deleted. `unwrapCreatureDeathConditional`,
  `unwrapTriggeringCardConditional` and `matchesDyingStats` stay: other families, planes and the kept cards read them.

**Cards.** `scripts/step14-death-scan.py` lists the family's abilities and wrappers; `scripts/step14-death-options.py`
(per letter module) converted 174 card classes, following local variables and constants to their one definition (and
refusing one shared with another family's ability: Pelt Collector's enters half keeps its wrapper for run 4), merging
the "this or another" halves (`MERGE`), with exact replacements for Jackdaw Savior, The Ghoul, Pelt Collector and the
five two-condition cards. `scripts/step14-death-docs.py` rewrote 56 agent-docs rows; the implement-card and
review-card skills were edited by hand.

**Kept for later runs.**

- Oathkeeper, Takeno's Daisho: "return that card ... if it's a Samurai card" is about the card, not the creature as it
  died, so its `TriggeringCardConditionalEffect` stays (checked as it triggers, on the printed card, as before).
- Kami of Mourning's granted ability: no permanent predicate compares with the source card's mana value yet.
- Agyrem: a plane, collected at the choke point by its exact trigger (`collectPlanarCreatureDeathTriggers`).
- The Master, Gallifrey's End: without the wrapper its "you may" would reach the nontoken may handler, which asks it
  before the ability is on the stack (CR 603.5, checked). Run 16 removes the may-unwrapping, then it converts.
- Effect-level conditions nested in a `SequenceEffect` stay effects (Venom, Overgrowth Elemental, Sephiroth, Blood
  Spatter Analysis, Silumgar Scavenger).
- Rinoa, Angel Wing says "do this only once each turn" but keeps `ONCE_EACH_TURN`, as its wrapper did: its "you may
  return one of them" isn't modeled as a "may", so nothing would mark the action taken. A pre-existing deviation.

**Behaviour changes.** Only the wording-driven ones above: a merged "this or another [X]" card's own death goes through
the creature-death collector instead of the "when this dies" one, and the one-ability merges are one stack object.

**Tests changed.** `TriggerPipelineTest`: four new tests through the engine: a "this or another Zombie" ability
triggers for its non-Zombie source and another Zombie but not another non-Zombie; another creature's filter ("had a
+1/+1 counter") is judged as it last existed; a creature-death ability limited to once each turn triggers once for two
deaths; an intervening "if that creature's power is greater than this creature's" is checked with the dying creature's
last-known power as it triggers and again as it resolves. No card test changed.

**Test runs.**

- Card: the test class of every converted card and every card test that constructs one (199 classes). The first run
  had 14 failures in 10 classes (the counters "if" on three "this or another" cards, Kraven's and Hawkeye's filter
  context, Amoeboid Changeling's lost types, Shelob's routing, Susurian Voidborn's lost target group, Jackdaw Savior on
  a copy), all fixed above; the second run passed.
- Application: `TriggerPipelineTest`, `LegacyTriggerBindingsTest`, `CardAbilitiesTest`, `ZoneChangeTriggerTest`,
  `TriggeredAbilityTest`, `TriggerConditionTest`, `TriggerOptionsTest`, `ZoneChangeRulesEventTest`,
  `RulesEventLogTest`, `CardFreezeTest`, `MiscTriggerTest`, `ActionTriggerTest`, `GrantedAbilitiesTest`,
  `TriggerCollectionServiceTest`, `DeathTriggerCollectorServiceTest` and the `…WatchTest` classes (272 tests): only
  `DeathTriggerCollectorServiceTest`'s 6 mock-wired cases fail, as at HEAD (checked in a HEAD worktree).
- AI: the whole module (1,620 tests), the same 10 failures as before.
- The full suite was not run here.

#### Run 3a: other objects' zone changes and permanent changes

Run 3 was split, with the user, into 3a (this run: every `ZoneChangeWatch` but the "another object enters" ones) and
3b ("whenever [an object] enters", 471 abilities in 453 card classes, step 12a part 2's entering watches). Every
sacrifice, put-into-a-graveyard, leaves-the-battlefield, exile, returned-to-hand, library, mill, leaves-a-graveyard,
command-zone, phasing, transform and turned-face-up ability of another object (and the source's own transform and
face-up ones) is free of trigger-only wrappers but the six kept below, and the split "this or another" cards of the
family are one ability each.

**Authoring.**

- A restriction on the object that moved is the trigger's filter: the restriction where the watched trigger's filter
  is `null` (`Triggers.sacrificed(new PermanentHasSubtypePredicate(FOOD), YOU_CONTROL)`), `AllOf(X, restriction)` where
  it is `X` (`Triggers.dies(AllOf(Triggers.NONTOKEN, HasSubtype(CURSE)), YOU_OWN)`). A card predicate becomes its
  permanent form, as in run 2 (`CardTypePredicate(LAND)` → `PermanentIsLandPredicate`, `CardIsHistoricPredicate` →
  `PermanentIsHistoricPredicate`, …).
- "A permanent / an artifact you control" is the relation: `TriggeringPermanentControllerConditionalEffect` became
  `YOU_CONTROL` (`.another` `ANOTHER_YOU_CONTROL`) on `dies(null, ANY)`, and
  `TriggeringArtifactControllerConditionalEffect` `YOU_CONTROL` / `ANOTHER_YOU_CONTROL` (its "nontoken" flag the
  `NONTOKEN` filter) on `dies(ARTIFACT, ANY)`.
- "During your turn" is `.onlyDuringYourTurn()`; on a permanent change, which has none, "if it's your turn" is the
  intervening-if option `ControllerTurn` (Jeering Instigator). Any other intervening "if" is
  `TriggerOptions.withInterveningIf` (Class levels, Outpost Siege's mode, Moonshadow's and Bloodchief Ascension's
  counters, Nihilith's suspend, Lurking Skirge, Defiled Crypt's door); `OncePerTurnTriggerEffect` (and `.keyed`) is
  `ONCE_EACH_TURN`, `.markOnAcceptance` `DO_ONLY_ONCE_EACH_TURN` (CR 603.2h, checked), `OnceOnlyTriggerEffect` `ONCE`.
  `ConditionalEffect.unless` and `ConditionalEffect(SourceCardInGraveyard, …)` stay effects.
- "This [permanent] or another [restricted] one" is one `TriggeredAbility` with two trigger conditions, the source's
  own (unchanged, collected by the pilot path) and the other one's (collected by its watch): Zone of Flame, Resourceful
  Defense ("if it had counters on it" is now the `THIS` condition's filter too), Nadier's Nightblade, Costume Closet,
  Vela the Night-Clad, Splinter the Mentor, Slayer's Bounty, Scrap Trawler, Slagstone Refinery, Rat King, Esoteric
  Duplicator. Long Feng's "another creature you control or a land you control" is `dies(AnyOf(CREATURE, LAND),
  ANOTHER_YOU_CONTROL)` beside its `dies(LAND, THIS)`.

**The engine.**

- `ZoneChangeWatch.of` finds the watch of a narrowed trigger: an exact key first, then the key with the filter's first
  `AllOf` part, then (unless `CreatureDeathWatch.collects` claims a dies trigger: a creature, nontoken creature or
  creature-or-planeswalker filter, or "one or more artifacts and/or creatures"; never an owner or `ENCHANTED`
  relation) the key with no filter, and for `onlyDuringYourTurn()` the key without it (the matcher checks it). New
  keys: `dies(null, YOU_CONTROL)` on `ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD`, `dies(ARTIFACT, YOU_CONTROL)`
  and `dies(ARTIFACT, ANOTHER_YOU_CONTROL)` on `ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD`.
  `ZoneChangeTrigger.withFilter` / `anyTurn` and `PermanentChangeTrigger.withFilter` are new; `abilitiesOf` uses the
  same lookup, so the scheme collector (My Champion Stands Supreme) reads its narrowed trigger and judges the filter.
- A temporary engine test (deleted) dumped every card ability's watch before and after the conversion: all kept their
  watch but five. Vat of Rebirth, Long Feng, Exuberant Fuseling and Graha Tia ("another … you control") moved from the
  any-permanent watch to `ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD`, Sly Requisitioner ("a nontoken
  artifact you control") to the artifact watch; their effects are handled the same there (their card tests pass).
  Exuberant Fuseling and Graha Tia say "another"/"other": their wrapper nesting had left the source out, so they are
  `ANOTHER_YOU_CONTROL`, not the `YOU_CONTROL` their controller wrapper suggests.
- The watch bodies and handlers stop reading the wrappers. Where a wrapper handler did more than filter, that is now the
  plain effect's path: "whenever you sacrifice" binds the sacrificed card and its mana value, targets a graveyard
  return and puts "you may" / "you may pay" on the stack for every effect (its `MayEffect`, `MayPayManaEffect` and
  `ConditionalEffect` handlers are gone; no unwrapped card used them); the artifact-graveyard default binds counters
  and mana value and routes declared targets (the controller wrapper's handler keeps only Urza's Miter's checks); the
  land-graveyard default carries the land's controller; the opponent-permanent-graveyard watch got a default (its only
  handler was the wrapper's). Deleted handlers: the sacrifice, opponent-sacrifice, nontoken/ally/your/opponent
  permanent-graveyard, any-permanent controller, land, enchanted-leaves, any-creature-sacrificed once-each-turn, card
  and creature-card graveyard and hand-from-library wrapper handlers; the leaves, returned-to-hand, graveyard-creature,
  graveyard-land and face-up/transform bodies' inline unwrapping.
- The pipeline's limit reaches the bodies: `TriggerMatchContext.markSourceOncePerTurnOnAcceptance` is set from
  `DO_ONLY_ONCE_EACH_TURN` (Riveteers Ascendancy, Screeching Scorchbeast). The face-up filter context names the
  watcher's permanent (Salt Road Ambushers' "another").
- Deleted: `EnchantedPermanentLeavesConditionalEffect`, `ConditionalTriggeringPermanentEffect`.

**Cards.** `scripts/step14-zone-change-scan.py` lists the family's abilities and wrappers (`--all` for every ability),
`scripts/step14-zone-change-watches.py` groups them by watch. `scripts/step14-zone-change-options.py` (per letter
module) converted 176 card classes, following local variables to their one definition; its `AFTER` table merges four
"this or another" cards and fixes long lines, its `MERGE` set merges seven more. Long Feng (a comment between
arguments) and Projektor Inspector (a helper shared with its enters half; its face-up half is converted, the enters
half keeps its wrapper for 3b) were converted by hand. `scripts/step14-zone-change-docs.py` rewrote 61 agent-docs rows;
the implement-card and review-card skills and `ARCHITECTURE.md` were edited by hand.

**Kept for later runs.**

- Urza's Miter: "if it wasn't sacrificed", which no trigger option says (`TriggeringArtifactControllerConditionalEffect`
  stays for it).
- Crawling Sensation: "the first time one or more land cards are put into your graveyard each turn" counts events
  before the source entered (`OncePerTurnTriggerEffect.firstLandGraveyardEvent`).
- Trail of Mystery: "if it's a creature" about the turned-up permanent, read from the stack entry as it resolves.
- Disa the Restless: `CardPutIntoGraveyardFromNonBattlefieldThisTurnPredicate` has no permanent form; it would be
  `putIntoGraveyardFromAnywhereButBattlefield`, which no watch of its kind keys.
- Corruption of Towashi and Oasis of Renewal: one "once each turn" shared with an enters half, for 3b / run 4
  (Oasis keeps the leave-graveyard card-conditional handler for that).
- Psychomancer: its halves are a target group's chain with a third, handler-level exile half
  (`SelfExiledFromBattlefieldEffect`, step 15).
- Al Bhed Salvagers and Susurian Voidborn keep their creature and artifact halves: the creature half excludes
  artifacts, so no death triggers both.

**For run 3b.** The entering bodies still read `TriggeringCardConditionalEffect`, `TriggeringPermanentConditionalEffect`,
`OncePerTurnTriggerEffect` and intervening-if `ConditionalEffect`s (`unwrapTriggeringCardConditional`, the entry
handlers in `EnterTriggerCollectorService`); `ZoneChangeWatch.of` already finds a narrowed entering trigger's watch.
Enters halves left wrapped beside a converted half: Projektor Inspector, Perimeter Enforcer, Case of the Pilfered Proof,
Corruption of Towashi (one "do this only once each turn" with its transform half). Oasis of Renewal's limit is shared
with its own enters abilities (run 4).

**Behaviour changes.** None intended. A filter on a card that died is judged on the permanent as it last existed,
where the wrappers read the printed card for some watches (Yomiji's "legendary", "artifact or creature"), as runs 1 and
2 did. The five cards above are collected by a neighbouring watch, with the same result.

**Tests changed.**

- `TriggerPipelineTest`: five new tests through the engine: a sacrifice ability narrowed by a filter keeps its watch and
  triggers only for a matching permanent; a sacrifice ability's "you may" goes on the stack (CR 603.5, checked); an
  "artifact you control" ability ignores an opponent's artifact; an ability of a `THIS` and an `ANOTHER_YOU_CONTROL`
  condition with one filter triggers for another permanent and for its own departure; a "leaves your graveyard during
  your turn" ability triggers only during it.
- `MiscTriggerCollectorServiceTest` (mock-wired): the may and may-pay sacrifice cases now reach the default handler;
  "skips a false intervening condition" (the pipeline checks it now), Disa's dead creature-card-graveyard handler case
  and the targeted-conditional case's wrapper were removed or unwrapped.
- `DeathTriggerCollectorServiceTest` (mock-wired): the enchanted-leaves conditional cases are gone with their handler;
  the opponent-permanent filter case tests the new default (the filter is the matcher's now).

**Test runs.**

- Card: the test class of every converted card, of the kept cards and of the plain cards of the watches whose
  handlers changed, and every card test that constructs one (338 classes, 3,079 tests): first run 2 failures, Oasis of
  Renewal's, which still needed the leave-graveyard card-conditional handler (restored); after the merges and the
  restore, the classes of the changed cards (20 classes) and Oasis pass. Vela the Night-Clad, Rat King, Slagstone
  Refinery and Esoteric Duplicator have no card test; `TriggerPipelineTest` covers the two-condition shape.
- Application: `TriggerPipelineTest`, `LegacyTriggerBindingsTest`, `CardAbilitiesTest`, `ZoneChangeTriggerTest`,
  `TriggeredAbilityTest`, `TriggerConditionTest`, `TriggerOptionsTest`, `ZoneChangeRulesEventTest`,
  `RulesEventLogTest`, `CardFreezeTest`, `MiscTriggerTest`, `ActionTriggerTest`, `GrantedAbilitiesTest`,
  `TriggerCollectionServiceTest`, `DeathTriggerCollectorServiceTest`, `MiscTriggerCollectorServiceTest`,
  `EnterTriggerCollectorServiceTest`, the `…WatchTest` and architecture classes (388 tests): only
  `DeathTriggerCollectorServiceTest`'s 6 mock-wired cases fail, as at HEAD.
- AI: the whole module (1,620 tests), the same 10 failures as before.
- `magical-vibes-webservice`'s `CardBrowserServiceTest` doesn't compile (`CardCatalog.findLoadedPrintingByName`); this
  run touches neither module.
- The full suite was not run here.

#### Run 3b: another object entering

Every "whenever [an object] enters" ability (an `enters`, `entersFrom` or `tokensCreated` trigger whose relation isn't
`THIS`, collected by step 12a part 2's entering watches) is free of trigger-only wrappers but the ten kept below. The
entering bodies no longer read `TriggeringCardConditionalEffect`, `TriggeringPermanentConditionalEffect`,
`EnterCreatureConditionalEffect`, `EnteringLandConditionalEffect` or `OncePerTurnTriggerEffect` (Sigil Captain's and
Akim's excepted).

**Authoring.**

- A restriction on the permanent that entered is the trigger's filter, as in runs 2 and 3a:
  `Triggers.enters(AllOf(Triggers.CREATURE, PermanentHasSubtypePredicate(ELF)), ANOTHER_YOU_CONTROL)`. The entering
  conditionals became filters too: power (`PermanentPowerAtLeastPredicate` / `AtMost`), counters
  (`PermanentHasCountersPredicate`), tapped (`PermanentIsTappedPredicate`), and where it came from, with two new
  predicates: `PermanentEnteredOrCastFromZonePredicate(zone)` ("entered from a graveyard or you cast it from a
  graveyard") and `PermanentWasCastPredicate` ("if it wasn't cast"; "if you didn't cast it from your hand" is
  `Not(PermanentCastFromZonePredicate(HAND))`). "If you cast it", which was checked only as it triggered
  (`ConditionalEffect.atTriggerTime(WasCast(true))`), is the filter `PermanentCastBySourceControllerThisTurnPredicate`
  (The Sibsig Ceremony).
- "This creature or another [X] you control" is `THIS_OR_ANOTHER_YOU_CONTROL` with the filter `AllOf(CREATURE, X)`,
  the source counting whatever its subtypes are (the old body let the source past a non-gating card filter); "a [X]
  you control" is `YOU_CONTROL`, which judges the source by the filter too. `scripts/step14-enters-options.py` decides
  by the Oracle wording ("or another", "and/or one or more other") with a `RELATIONS` table for "this or another …,
  if that creature …" (Breathless Knight, Alex Wilder), and drops a `CardIsSelfPredicate` / `IsSourcePermanent`
  alternative the new relation says.
- On `enters(null, ANY)` the two-argument `TriggeringPermanentConditionalEffect` also meant "you control": those
  abilities are `YOU_CONTROL` (a new key of `ON_ANY_PERMANENT_ENTERS_BATTLEFIELD`).
- Limits and intervening "if"s are options as in 3a. One Oracle ability with two conditions under one limit is one
  `TriggeredAbility` (K'Li, the Resourceful; Expedition Supplier and Extraordinary Journey, whose "this or another"
  halves each had their own copy of the limit, are one ability with `THIS_OR_ANOTHER_YOU_CONTROL` / `ANY`). Corruption
  of Towashi's "do this only once each turn" is `DO_ONLY_ONCE_EACH_TURN` on both of its halves, which mark the source
  unkeyed, so they still share it.
- Kolodin, Triumph Caster's animation names the Vehicle with `GrantScope.TRIGGERING_PERMANENT` (the handler told it
  apart by the wrapper; `AnimationSupport.animateSingle` resolves that scope to the entry's triggering permanent).

**The engine.**

- `TriggerMatcher.enteredMatches` judges an entered permanent's filter: characteristics (types, subtypes, colors,
  keywords, token) on the copy with the characteristics it has as its enters abilities are processed, anything else
  (layered power, counters, origin, "other than this", "you control") on the permanent with the watching ability's
  source as the filter's source and the entered permanent as the triggering one. Before, every predicate ran without
  a context, so a power filter read unlayered values and a source-relative one failed.
- `TriggeredAbilityStackService.triggeringContext` checks an intervening "if" with an entered permanent's layered power
  (Pelt Collector's enters half), and the "during your turn" check is null-safe for a moving object without a
  controller.
- `ZoneChangeWatch` keys: `enters(CREATURE, THIS_OR_ANOTHER_YOU_CONTROL)` (each and "one or more") on the self-or-ally
  creature watches, `enters(null, YOU_CONTROL)` and `enters(NONTOKEN_CREATURE, ANY)` on the any-permanent watch (the
  latter keeps Overburden and Genesis Chamber, whose effects name the entering creature's controller, where they were).
- The bodies: `triggerAllyCreatureEnters` serves the self-or-ally creature watches too (`dispatchSelfOrAllyCreature…`
  is gone); the artifact-type, Equipment and nontoken-artifact watches share one loop; the limit reaches every body
  through `dispatchEntering` (a limited ability is one trigger, no Panharmonicon or Clara copy, as the wrapper made it;
  `TriggerMatchContext.triggersOnlyOnceEachTurn` is new) or, for the enchantment, landfall and token bodies, read from
  the candidate. Landfall's "a printed land doesn't see its own entry" exception reads whether the trigger narrows the
  lands (`narrowedLandfall`). Oran-Rief Hydra's and Emeria Shepherd's "if it's a Forest … otherwise" branches are
  effect-level conditions, still read by the landfall body (`branchOnEnteringLand`).
- Deleted: the any-permanent card- and permanent-conditional handlers, the ally-permanent permanent-conditional
  handler and `enqueueAllyPermanentEnter` (the registry's defaults do the same; a targeted "you may" on the
  any-permanent watch goes to `handleEnterMay`, as the conditional handler sent it), the ally-token card-conditional
  handler, `unwrapEnteringCardSubtypeConditional`, `unwrapEnteringLandConditional`, `enteringCardMatches`,
  `effectiveEnteringCard`, and the classes `EnteringCreatureMinPower…`, `MaxPower…`, `HasCounters…`, `FromGraveyard…`,
  `FromExile…`, `NotCast…`, `NotCastFromHand…ConditionalEffect`, `EnteringLandConditionalEffect`,
  `EnteringLandTappedConditionalEffect` and `EnteringLandNotFromHandConditionalEffect`.
- Abilities the pipeline doesn't match read the filter themselves: face-up planes (`planarEnteringEffects`: Murasa,
  Isle of Vesuva, and Nyx, whose narrowed trigger is the enchantment watch's) and the abilities a spell registers until
  end of turn (`watchesEntering`: Theoretical Duplication).

**Cards.** `scripts/step14-enters-scan.py` lists the family's wrapped abilities by watch;
`scripts/step14-enters-options.py` (per letter module, `--dry-run` prints every relation decision and every watch
change) converted 427 card classes, with `MANUAL` (Kolodin, Corruption of Towashi, Nature's Wrath and Expedition
Supplier, whose wrappers come from a helper method) and `AFTER` (K'Li's merge, long lines, Voldaren Bloodcaster's
two-pass conversion, stale comments) tables; Extraordinary Journey was converted by hand. `scripts/step14-enters-docs.py`
rewrote 178 agent-docs rows, the implement-card and review-card skills and `PREDICATES_REFERENCE.md`; `ARCHITECTURE.md`
was edited by hand.

**Watches that changed.** Nine abilities now narrow a generic watch's trigger to a specific one and are collected
there, with the same results (their card tests pass): Lethal Vapors, Spreading Plague, Tainted Aether, Blasting
Station ("a creature enters"), Captain Storm and Gonti's Aether Heart ("an artifact you control"), Gladewalker
Ritualist ("another creature you control"), Nyx ("another enchantment you control"), Tetzin ("another artifact you
control").

**Behaviour changes (rules fixes, from the Oracle wording).**

- "A [X] you control enters" now judges the source by the filter when it enters itself; the old self-or-ally body let
  it through: April O'Neil, Bishop of Wings, Geralf the Fleshwright and Mary Jane Watson no longer trigger for their
  own entry.
- "This creature or another [X] with …" no longer requires the source to meet the restriction: Overseer of Vault 76
  ("power 3 or less") and the cards that already spelled it with an `IsSourcePermanent` alternative.
- An entering filter is judged as the game sees the permanent after the event (CR 603.6a): layered power and
  subtypes, where some wrappers read the printed card.

**Kept for later runs.**

- Sigil Captain: "if that creature is 1/1" is checked again on resolution through its wrapper
  (`EnteringCreatureExactStatsConditionalEffect`, the last `EnterCreatureConditionalEffect`).
- Akim, the Soaring Wind: "the first time you create one or more tokens each turn"
  (`OncePerTurnTriggerEffect.firstTokenCreation`).
- Archaeomancer's Map, Confounding Conundrum, Invader Parasite, Tunnel Ignus: their intervening "if" is about the
  entering land or its controller, which the opponent-land body binds and the pipeline's context doesn't (step 15).
- Phyrexian Dragon Engine, Archfiend's Vessel (`entersFrom(GRAVEYARD, THIS)`), Field of the Dead (its "another land"
  shares its effect with its own enters copy) and Oasis of Renewal: run 4.

**Tests changed.**

- `TriggerPipelineTest`: four new tests through the engine: a "this or another Zombie" enters ability triggers for its
  non-Zombie source and another Zombie but not another non-Zombie; an enters filter on power reads a 2/2 entering
  under Glorious Anthem as a 3/3; an enters ability limited to once each turn triggers once for two creatures; a
  "Bear you control" ability ignores an opponent's Bear.
- `EnterTriggerCollectorServiceTest` (mock-wired): the cases that tested a wrapper's gating (power, counters, subtype,
  token type, once-per-turn) were removed, the gating being the matcher's; the others use the plain effect.

**Test runs.**

- Card: the test class of every card with an ability of this family, of the kept cards, and every card test that
  constructs one (1,732 classes). The first run failed in 10 classes: the two helper-wrapped cards above, Kolodin, the
  three planes, Theoretical Duplication, Overburden's watch move, K'Li's split limit and Foe-Liage's null controller;
  after the fixes those classes and the whole `e` and `n` selections pass.
- Application: the pipeline, rules-event, trigger-model, collector, watch, ETB, placement, predicate and architecture
  classes (655 tests): the failures are `DeathTriggerCollectorServiceTest`'s 6, `CardImmutabilityArchTest` and
  `SimulationCopyCompletenessTest`'s known ones, and `BattlefieldPlacementServiceTest`'s 24 and
  `CostEffectClassificationTest`'s 1, which fail the same way at HEAD (checked in a HEAD worktree).
- AI: the whole module (1,620 tests), the same 10 failures as before.
- The full suite was not run here.

#### Run 4a: the own enters abilities read their options; "this or another" enters merged

Run 4 was split, with the user, into 4a (this run) and 4b (the intervening-if `ConditionalEffect`s of the own enters
abilities). The "when this enters" collector and every engine, AI, networking and test-fixture reader of the own enters
abilities read the abilities' options, so a card can author "when this enters, if …" and "triggers only once each turn"
as `TriggerOptions`, and the "this [permanent] or another [type] you control enters" cards step 12b left split are one
ability each.

**Authoring.**

- "When this enters, if …" (CR 603.4, checked) is `addTrigger(Triggers.enters(ObjectRelation.THIS),
  TriggerOptions.DEFAULT.withInterveningIf(condition), effect)`; an `INDEPENDENT` one is the new
  `Card.addTrigger(Trigger, TriggerMode, TriggerOptions, CardEffect...)` (and `SpellTarget`'s), Catharsis.
  "Triggers only once each turn" is `withLimit(TriggerLimit.ONCE_EACH_TURN)` and covers every trigger condition of the
  ability (Oasis of Renewal).
- "This [permanent] or another [type] you control enters" is one `TriggeredAbility` with two trigger conditions,
  `enters(THIS)` and `enters(filter, ANOTHER_YOU_CONTROL)` (`ANOTHER` for "or another Zombie enters"), added with
  `addTriggeredAbility` (`target(filter).addTriggeredAbility` on a target group), as run 3a merged its "this or
  another" cards. The source's own condition is collected by `EtbTriggerService` as before and the other by its
  entering watch as before, so no entry is collected differently; only the ability is one object (CR 603.3, checked).
  `THIS_OR_ANOTHER_YOU_CONTROL` (3b's form for creature watches) wasn't used: 12b's attempt with it moved the source's
  own entry to the watch's target flow and failed 37 tests, and no watch sees a non-creature source's own entry.

**The engine.**

- `TriggeredAbility.legacyEffects()` gives the ability's registrations wrapped in its options in the form the cards
  wrote them before step 14: an intervening "if" as `ConditionalEffect(condition, effect, true)`, `ONCE_EACH_TURN` as
  `OncePerTurnTriggerEffect.keyed(effect, limitKey())`, `DO_ONLY_ONCE_EACH_TURN` as `.markOnAcceptance` (`ONCE` isn't
  represented: no own enters ability has it). `TriggeredAbility.limitKey()` is the key the pipeline's
  `TriggeredAbilityStackService.limitKey` already used, now shared, so a limit marked by the enters collector is seen
  by the pipeline for the ability's other condition and the other way round.
- `Card.getLegacyTriggerEffects(Trigger)` / `getLegacyTriggerEffectRegistrations(Trigger)` read a second per-trigger
  view built with the card's index (derived, no new copied state, nothing for `CardFreezeTest`), so a wrapped effect
  keeps its identity across reads (`isSimultaneousWithPrevious` also looks there; `getEffectTargetIndex` already
  unwraps a `ConditionalEffect`). An ability without options gives its registrations as they are, so nothing changes
  for the cards 4b hasn't converted.
- Every reader of `getTriggerEffects` / `getTriggerEffectRegistrations(Triggers.enters(ObjectRelation.THIS))` reads the
  legacy view (`scripts/step14-own-enters-readers.py`, 60 call sites in 26 files: `EtbTriggerService`,
  `EtbEffectResolver`'s callers, targeting, kicker, blitz and evoke checks, `CardViewFactory`, the AI), so
  `EtbEffectResolver` and the code that inspects a kicked or gated enters ability see the wrapper they saw before.
  `Permanent.getGrantedTriggerEffects` and `GrantedTriggeredAbilitySupport.grantedTriggerEffects` wrap granted
  abilities the same way (no granted ability has options yet).
- `EtbEffectResolver`: an intervening "if" that is an `AnyOf` of `CastFromZone` / `EnteredFromZone` (Archfiend's
  Vessel) is checked as the ability triggers and unwrapped, as each part alone was; as a plain gate it would have been
  kept for a resolution check of the cast zone, which the resolving ability doesn't know.
- `TriggerMatcher.matchedTrigger` returns an ability's first matching condition, so a merged ability's own entry
  matches its `enters(THIS)` condition, which the pipeline skips (`OWN_ENTRY`), and another permanent's entry its
  other condition.

**Cards.** `scripts/step14-own-enters-merge.py` (per letter module; `--dry-run` prints the diffs) merged 63 card
classes: the 56 whose two halves have the same effect text (31 that 12b's scan listed and 25 with a nested filter its
regex missed; `scripts/step14-own-enters-scan.py` lists the own enters abilities with wrappers for 4b), and by exact
replacement (`MANUAL`) Kappa Cannoneer and Pactdoll Terror (two pooled own abilities against one `SequenceEffect`),
Fear of Sleep Paralysis (three conditions, "and whenever you fully unlock a Room"), Field of the Dead (its intervening
"if" an option on both conditions; the landfall condition stays `YOU_CONTROL`, its own entry matching `THIS` first),
Cacophony Unleashed (its "if you cast it" ability stays apart for 4b) and Coercive Recruiter (two target groups of one
filter become one). Also by `MANUAL`: Archfiend's Vessel ("if it entered from your graveyard or you cast it from your
graveyard": one `enters(THIS)` ability with the option `AnyOf(EnteredFromZone, CastFromZone)` where a cast half and an
`entersFrom(GRAVEYARD, THIS)` half stood), Catharsis (its two `atTriggerTime(ColorSpentToCast)` wrappers are options;
the resolver checks a mana-spent condition only as it triggers, as the wrapper said) and Oasis of Renewal (two
abilities of `enters(THIS)` and `leavesGraveyard(PermanentIsLandPredicate / Not(…), YOU_OWN)` with
`ONE_OR_MORE.withLimit(ONCE_EACH_TURN)`, where four abilities shared two keyed wrappers and the graveyard halves a
`TriggeringCardConditionalEffect`). `AFTER` fixes three comments about the halves. `scripts/step14-own-enters-docs.py`
rewrote 6 `ORACLE_TEXT_EFFECT_MAP.md` rows; the implement-card skill and `ARCHITECTURE.md` were edited by hand
(review-card already described both forms).

**Kept for later runs.**

- Split, needing a filter on the source's own entry, which `EtbTriggerService` can't read (it reads the exact
  `enters(THIS)` trigger): Death Match ("whenever a creature enters" on an enchantment: its own half is "if it's a
  creature", `atTriggerTime(SourceIsCreature)`), and the plain-wording cards 12a listed whose own half is "if it is
  [the type]": Weldfast Wingsmith, Path of Discovery, Epharas Enlightenment, Airbender Ascension, Ghostly Dancers, Merry
  Warden of Isengard, Ingenious Smith. Each entry still meets one half only, so they behave as the one Oracle ability.
- Split, the halves' effects differing: Sokka and Suki and Hammer of Nazahn (attach to this vs. to "that" Equipment or
  creature: one effect would need the triggering permanent to be the source on its own entry, step 15).
- Phyrexian Dragon Engine: "when this enters from your graveyard" would be the filter "you own it", but
  `PermanentOwnedBySourceControllerPredicate` reads a reanimated permanent's controller as its owner (its card test
  "returning from an opponent's graveyard" failed with it), so its `atTriggerTime(EnteredFromZone)` wrapper stays.
- MACH-1, Swooping Scoundrel: its once-each-turn limit is shared with a "whenever you gain life" condition, whose
  collector (life, step 13 family 9) doesn't read options yet (run 13).
- The keyword-built intervening "if"s: evoke's `SacrificeSelfIfEvokedEffect` (CR 702.74a, checked; 35 cards, added by
  `SpellCastingService` too), tribute's `TributeNotPaidEffect` (12) and `LoseGameIfNotCastFromHandEffect` (Phage): each
  would need a condition and the readers that look for the effect by class (`EtbTriggerService`, the AI); run 16 or the
  keyword family.
- The engine writers that add an own enters `ConditionalEffect` (offspring and the specialize/perpetual paths in
  `CardChoiceHandlerService`, the hand-cast kicker copy in `HandCastCardCharacteristicsService`): 4b.

**For run 4b.** About 520 own enters abilities still carry an intervening-if `ConditionalEffect` (478 `new
ConditionalEffect(`, 43 factory forms, 41 `unless`, which stay; `scripts/step14-own-enters-scan.py --summary`). Their
conversion to `TriggerOptions.DEFAULT.withInterveningIf(c)` keeps behaviour by construction (the legacy view gives the
resolver the same `ConditionalEffect`), but `EtbEffectResolver` checks only the condition types its switch names (and
the `isEtbTriggerGate()` ones) as the ability triggers; any other intervening "if" is checked only as it resolves, an
old-path deviation from CR 603.4 that the conversion carries over unless 4b makes the collector check every option
condition as the ability triggers. 4b should also decide by the Oracle wording ("enters, if") which default-flag
`ConditionalEffect`s are really intervening "if"s.

**Behaviour changes.** None intended: each merged card's own entry and the other entries are collected where they
were, and the converted wrappers are given to the collector in the form it read before. The merged abilities are one
object each (a limit or an option now covers both conditions). Archfiend's Vessel's graveyard entry is collected by the
enters collector instead of the "enters from a graveyard" watch.

**Tests changed.**

- `TriggerPipelineTest`: three new tests through the engine: a "when this enters" intervening "if" option is checked
  as the ability triggers and again as it resolves; a "this or another creature" ability of two conditions triggers
  once for its own entry and once for another creature's; a once-each-turn limit covers an ability's own entry and its
  other condition.
- `CardAbilitiesTest`: two new tests of the legacy view (an intervening "if" wraps each effect once, with a stable
  identity, the plain view unchanged; a limit is keyed by its ability).
- No card test changed.

**Test runs.**

- Card: every test class of a changed card and every card test that constructs one (93 classes, 881 tests): the only
  failure was Phyrexian Dragon Engine's (above, reverted).
- Application: `TriggerPipelineTest`, `CardAbilitiesTest`, `LegacyTriggerBindingsTest`, `ZoneChangeTriggerTest`,
  `TriggeredAbilityTest`, `TriggerOptionsTest`, `TriggerConditionTest`, `CardFreezeTest`, `EtbTriggerServiceTest`,
  `EnterTriggerCollectorServiceTest`, `GrantedAbilitiesTest`, `BattlefieldPlacementServiceTest`,
  `SpellCastingServiceTest`, `TargetLegalityServiceTest`, `ValidTargetServiceTest`, `CardViewFactoryTest`,
  `ETBTokenTargetServiceTest`, `StackEntryTargetGroupsTest`, `CreatureControlServiceTest`, `MiscTriggerTest`,
  `ActionTriggerTest`, `RulesEventLogTest`, `ZoneChangeRulesEventTest`, the `…WatchTest`, architecture and copy classes
  (745 tests): 57 failures, all failing the same way at HEAD (`BattlefieldPlacementServiceTest` 24,
  `SpellCastingServiceTest` 10, `CreatureControlServiceTest` 1, `CardImmutabilityArchTest` and
  `SimulationCopyCompletenessTest` 1 each as recorded; `TargetLegalityServiceTest` 14, `ValidTargetServiceTest` 2 and
  `ETBTokenTargetServiceTest` 4 checked in a HEAD worktree).
- AI: the whole module (1,620 tests), the same 10 failures as before.
- The full suite was not run here.

#### Run 4b: the own enters abilities' intervening "if"s are options

Run 4b was split again, as the user allowed, into 4b (this run: the conversion, which keeps behaviour) and 4c (the
rules fixes it found). Every "when this enters, if …" ability (CR 603.4, checked) is authored with
`TriggerOptions.DEFAULT.withInterveningIf(condition)`, and an "If …" sentence inside an own enters ability's effect text
that the collector doesn't read as it triggers is `ConditionalEffect.unless`.

**Authoring.**

- "When this enters, if …" is `addTrigger(Triggers.enters(ObjectRelation.THIS)[, TriggerMode.INDEPENDENT],
  TriggerOptions.DEFAULT.withInterveningIf(condition), effect…)`, on the card or its target group. So are the
  intervening "if"s keywords define: ravenous (CR 702.156a, checked: "if X is 5 or more"), offspring (CR 702.175a,
  checked), squad (CR 702.157a, checked), gift (CR 702.174b, checked), and the trigger conditions on how the permanent
  entered that the collector can't read as a filter: "when this land enters untapped", escape's "when it enters this
  way", "when this enters during the declare attackers step". Blitz's two own enters markers (CR 702.152a, checked: no
  triggered ability) carry the option `CastForAlternateCost` as they carried the wrapper; the collector still drops
  them.
- Which wrappers are intervening "if"s was decided by the Oracle text: the card has a "When … enters …, if" ability, or a
  keyword above. Ten cards' wrappers are the effect text's own "If …" sentence, on a condition the collector doesn't
  read as it triggers, and became `ConditionalEffect.unless` (the collector and the resolution treat both forms alike
  for such a condition): Celestial Regulator, Dream Thief, Fear of Immobility, Hotshot Investigators, Invasion of
  Pyrulea, Kraul Harpooner, Reptilian Recruiter, Ainok Guide, Forsaken Crossroads, Rasaad, Monk of Selûne (CR 610.3b,
  checked).

**The engine.** No reader changed: run 4a's legacy view gives the collector, the targeting code and the AI the same
`ConditionalEffect(condition, effect, true)` for an option as the card wrote before, so `EtbEffectResolver` unwraps,
gates or keeps it as before. The engine's own writers of own enters wrappers author the option too:
`CardChoiceHandlerService` (offspring granted to a card in hand, and a granted blitz's two markers) and
`HandCastCardCharacteristicsService` (the offspring copy of a hand-cast card). The offspring payment count still reads
the legacy view.

**Cards.** `scripts/step14-own-enters-options.py` (per letter module; `--dry-run` prints the diffs) converted 389
abilities in 351 card classes and flipped 11 abilities in the 10 cards above; it stops on any wrapped own enters
ability it has no decision for, so none is decided silently. An effect passed as a local variable is followed to its
one definition (its type becomes `CardEffect`). `scripts/step14-own-enters-options-docs.py` rewrote 38 agent-docs rows
of converted cards (`ORACLE_TEXT_EFFECT_MAP.md`, three `CARD_PATTERNS_*` files, `CARD_IMPLEMENTATION_PLAYBOOK.md`),
leaving a condition's own reference entries, which cover its resolution-time use too.
The implement-card skill says when to use the option, `unless`, and the plain wrapper.

**Kept, with their reason (the script's `KEEP` and `SKIP`).**

- An "… instead" or "Then if …" sentence on a cast-time fact, which the collector reads as the ability triggers (no
  intervening "if", but the same result whenever it's checked): Adipose Offspring, All-Star Kicker, Berg Strider,
  Desolation Angel, Desolation Giant, Dunbarrow Revivalist, Fblthp, the Lost, Grim Reaper's Sprint, Kinetic Ooze,
  Kitnap, Rix Maadi Reveler, Sea Gate Stormcaller, Sphinx of Lost Truths, Turncoat Kunoichi. `unless` would move the
  check to resolution, where the kicker and cost flags of an enters trigger's stack entry haven't been checked; run 16
  decides them with the `interveningIf` flag.
- Armored Kincaller: "If you do or if you control another Dinosaur" is no intervening "if", but
  `ControlsAnotherPermanent` is an ETB gate the collector checks as the ability triggers: a rules fix, run 4c.
- Bloodhall Priest, Naomi, Pillar of Order, Omnivorous Flytrap: "whenever this enters or attacks, if …" share the
  wrapped effect with an attacks ability, whose collector doesn't read options yet; step 13 family 3's run (run 7) makes
  each one ability with both conditions.
- Mythos of Illuna: its "if it's a creature" ability is also granted to a token copy through a `TriggeredAbility` the
  script doesn't follow. Aether Flash, Carnival of Souls, Path of Discovery, Weldfast Wingsmith (and Death Match, run
  4a): the own half's "if it's a [creature]" is a filter on the source's own entry, which `EtbTriggerService` can't read
  yet.
- Nested `ConditionalEffect`s (64 abilities: inside a `SequenceEffect`, `MayEffect`, modal option and so on) are
  conditions of the effect text, not of the ability, and stay; so does Thieving Skydiver's inner "If that artifact is an
  Equipment" under its converted option.
- Evoke, tribute and Phage's keyword effects, as run 4a listed.

**For run 4c.** `EtbEffectResolver` checks an intervening "if" as the ability triggers only for the condition types its
switch names and the `isEtbTriggerGate()` ones; any other is checked only as it resolves. Among the converted
abilities that leaves Acererak the Archlich (`NotCondition(ControllerHasCompletedDungeon)`), Deathbringer Regent
(`AllOf(CastFromZone, AnyPlayerControlsPermanentCount)`), Inquisitor Captain and Ran and Shaw (`AllOf(WasCast, …)`)
and Graven Archfiend (`SacrificedCardMatches`, which reads the sacrificed card that `StackResolutionService` attaches
to the trigger's stack entry only after it's collected). A blanket check of every option condition at trigger time
needs that card in the resolver's `ConditionContext` first; the ten `unless` flips above keep the blanket check from
reaching the effect-text conditions. Run 4c also fixes Armored Kincaller (its gate as an `unless`) and decides the
`KEEP` cards, with card tests through the engine.

**Behaviour changes.** None: the collector and every reader see the wrappers they saw before.

**Tests changed.** None. Two card tests' comments still say "resolve ConditionalEffect", which the stack entry still
holds (Gwyllion Hedge-Mage, Selkie Hedge-Mage).

**Test runs.**

- Card: the test class of every changed card and every card test that constructs one (570 classes, 4,800 tests), plus
  Cottontail Caretaker and Zinnia, Valley's Voice (the offspring writers): no failure.
- Application: `TriggerPipelineTest`, `CardAbilitiesTest`, `LegacyTriggerBindingsTest`, `ZoneChangeTriggerTest`,
  `TriggeredAbilityTest`, `TriggerOptionsTest`, `TriggerConditionTest`, `CardFreezeTest`, `EtbTriggerServiceTest`,
  `EnterTriggerCollectorServiceTest`, `GrantedAbilitiesTest`, `BattlefieldPlacementServiceTest`,
  `SpellCastingServiceTest`, `TargetLegalityServiceTest`, `ValidTargetServiceTest`, `CardViewFactoryTest`,
  `ETBTokenTargetServiceTest`, `StackEntryTargetGroupsTest`, `CreatureControlServiceTest`, `MiscTriggerTest`,
  `ActionTriggerTest`, `RulesEventLogTest`, `ZoneChangeRulesEventTest` (715 tests): 55 failures, per class the ones
  run 4a recorded as failing at HEAD (`BattlefieldPlacementServiceTest` 24, `SpellCastingServiceTest` 10,
  `TargetLegalityServiceTest` 14, `ETBTokenTargetServiceTest` 4, `ValidTargetServiceTest` 2,
  `CreatureControlServiceTest` 1).
- AI: `TargetPolarityGuardTest`: its 2 known failures (no converted card among them).
- The full suite was not run here.

#### Run 4c: every own enters intervening "if" is checked as the ability triggers

The rules fixes run 4b found (CR 603.4, checked): the "when this enters" collector checks every intervening "if" as the
ability triggers, with the card sacrificed to cast the permanent known by then, and the cards 4b kept are decided.

**The engine.**

- `EtbEffectResolver`: an intervening-if `ConditionalEffect` (an option, through run 4a's legacy view, or a card's own
  wrapper) whose condition its switch doesn't name is checked as the ability triggers and kept, so it is checked again
  as it resolves; before, only `isEtbTriggerGate()` conditions, ravenous's `SpellXAtLeast` and `NoMonarch` were, and
  every other condition only as the ability resolved. Those three cases are now the default case. The one exception is
  `TargetPermanentMatches` without an Aura's target, whose target isn't chosen as the ability triggers (CR 603.3d,
  checked): it passes through, as before. `isEtbTriggerGate()` now decides only that a targeted gated ability chooses
  its target as it goes on the stack.
- `Permanent.castSacrificedCard` (copied by `Permanent(Permanent)`; `Permanent` has no `CardFreezeTest` or
  `simulationCopy` ratchet of its own): the card sacrificed to pay the permanent's spell's sacrifice cost, set by
  `StackResolutionService.createEnteringPermanent` from the spell's stack entry. The resolver's `ConditionContext`
  gives it as the sacrificed card, so `SacrificedCardMatches` (Graven Archfiend) is checked as the ability triggers;
  `carrySacrificedCardToEtbEntries` still gives it to the trigger's stack entry for the resolution check.
- The abilities this changes: Acererak the Archlich (`NotCondition(ControllerHasCompletedDungeon)`), Graven Archfiend
  (`SacrificedCardMatches`) and Pharika's Spawn (`SourceWasCastWithEscape`) aren't put on the stack when their
  condition is false. Run 4b's list also named Deathbringer Regent, Inquisitor Captain and Ran and Shaw, but their
  `AllOf`s were gates already (`AllOf.isEtbTriggerGate()` admits `WasCast`, and every other part is a gate), so they
  were checked as they triggered. A scan of every own enters option and top-level intervening-if wrapper found no
  other condition the switch didn't name.

**Cards.** `scripts/step14-own-enters-fixes.py` (exact replacements, `--dry-run` prints the diffs):

- Armored Kincaller: "you may reveal a Dinosaur card from your hand. If you do or if you control another Dinosaur,
  you gain 3 life" has no intervening "if" (CR 603.4: the "if" doesn't follow the trigger condition). Its two
  abilities, the second gated on controlling another Dinosaur as it triggered, are one ability whose two
  `ConditionalEffect.unless` are checked as it resolves, so a Dinosaur that arrives while the ability is on the stack
  counts.
- Kitnap: gift's "when this permanent enters, if its gift cost was paid" (CR 702.174b, checked) is the option, as on
  the other gift cards 4b converted (its script kept the card for its stun sentence). No behaviour change.

**Decided: the run 4b `KEEP` cards stay.** Adipose Offspring, All-Star Kicker, Berg Strider, Desolation Angel,
Desolation Giant, Dunbarrow Revivalist, Fblthp, the Lost, Grim Reaper's Sprint, Kinetic Ooze, Kitnap (its stun
sentence), Rix Maadi Reveler, Sea Gate Stormcaller, Sphinx of Lost Truths and Turncoat Kunoichi write an "… instead" or
"Then if …" sentence as an intervening-if wrapper, which isn't one, but the result is the same whenever the condition is
checked: each condition is a fact fixed as the permanent enters (kicked, bargained, emerge, sneak or spectacle paid, the
gift, X, snow mana, entered or cast from a library), or "if it's your main phase", which can't change while the ability
is on the stack (CR 500.2, checked: a phase ends only with an empty stack). The collector pools the halves of one Oracle
ability into one stack entry, as their own enters effects aren't `INDEPENDENT`. Each card's test class covers both
branches through the engine. Run 16, which removes the `interveningIf` flag, gives them their final form; the wrappers
should then be `unless` with a resolution check of the enters trigger's stack entry flags (kicked, spectacle, gift,
alternate cost), which still has to be verified for each flag.

**Still kept** (unchanged from 4a and 4b, for the runs named there): Death Match, Weldfast Wingsmith, Path of Discovery,
Aether Flash, Carnival of Souls and the other "if it is [the type]" own halves; Phyrexian Dragon Engine; MACH-1;
Bloodhall Priest, Naomi, Pillar of Order, Omnivorous Flytrap; Mythos of Illuna; evoke, tribute and Phage's keyword
effects; the nested `ConditionalEffect`s.

**Behaviour changes.**

- Acererak the Archlich, Graven Archfiend and Pharika's Spawn: the ability isn't put on the stack when its intervening
  "if" is false as it triggers (before, it was put on the stack and did nothing).
- Armored Kincaller: a Dinosaur you control when the ability resolves counts, though it wasn't there as it triggered.

**Tests changed.**

- `EtbEffectResolverTest`: two new tests (a sacrificed-card intervening "if" is checked as the ability triggers, with
  the card sacrificed to cast it; a condition on the target passes through). It also has 3 failures at HEAD, unrelated
  to this run and failing the same way with HEAD's resolver (checked by restoring it): `ColorSpentToCast: snapshots the
  cast-time result into the ETB trigger`, `GainLifeEqualToToughness: materialises toughness read at trigger time`,
  `Collect-evidence gate keeps the ETB effect only when evidence was paid` (each expects the form the resolver gave
  before a later change).
- `GravenArchfiendTest`: two new tests (the ability isn't put on the stack without the sacrifice; it is with a non-Demon
  creature sacrificed). `PharikasSpawnTest`: one new test (a normal cast puts no ability on the stack).
  `AcererakTheArchlichTest.doesNotTriggerAfterDungeonCompletion`: now also asserts the stack is empty, as its name says.
- `ArmoredKincallerTest`: two new tests (a Dinosaur arriving before the ability resolves counts; one leaving before it
  resolves doesn't). Its other tests pass unchanged.

**Test runs.**

- Card: the test class of every card with a "when this enters" intervening "if" (option or wrapper), the `KEEP` cards,
  Armored Kincaller, Kitnap and the offspring writers' cards (377 classes, 2,908 tests): no failure.
- Application: run 4b's classes plus `EtbEffectResolverTest`, `SimulationCopyCompletenessTest` and
  `StackResolutionServiceTest` (831 tests with the AI's): per class the failures recorded at HEAD
  (`BattlefieldPlacementServiceTest` 24, `SpellCastingServiceTest` 10, `TargetLegalityServiceTest` 14,
  `ETBTokenTargetServiceTest` 4, `ValidTargetServiceTest` 2, `CreatureControlServiceTest` 1,
  `SimulationCopyCompletenessTest` 1) and `EtbEffectResolverTest`'s 3 above.
- AI: `GameDataDeepCopyTest` passes; `TargetPolarityGuardTest` has its 2 known failures.
- The full suite was not run here.

#### Run 5: steps and phases

Step 13 family 1: every "at the beginning of [step or phase]" ability (`Triggers.upkeep`, `drawStep`,
`precombatMainPhase`, `beginningOfCombat`, `endOfCombat()`, `postcombatMainPhase`, `secondMainPhase`, `endStep`, in any
zone, collected by `StepTriggerService`) is authored with its trigger-level properties as `TriggerOptions`, and
`SurvivalTriggerEffect` is gone.

**Authoring.**

- "At the beginning of …, if …" (CR 603.4, checked) is `addTrigger(<step trigger>[, TriggerMode.X],
  TriggerOptions.DEFAULT.withInterveningIf(condition), effect…)`, on the card or its target group; a
  `new TriggerCondition(…)` ability's `TriggerOptions.DEFAULT` gains the option. So are the intervening "if"s keywords
  and card types define: vanishing (CR 702.63a, checked: "if this permanent has a time counter on it"), impending
  (CR 702.176a, checked), a Case's "To solve" ability and its "Solved —" step abilities (CR 719.3c, checked), and the
  abilities a permanent has only at a Class level, in a chosen mode, with a door unlocked, at max speed or harnessed
  (as runs 2 and 3a made those conditions options in their families).
- "This ability triggers only once" is `.withLimit(TriggerLimit.ONCE)` (Acrobatic Cheerleader, Jet Collector, Pearl
  Collector), no longer an `OnceOnlyTriggerEffect` under the wrapper.
- "At the beginning of your second main phase" is `Triggers.secondMainPhase(ActivePlayer.YOU)`, the turn's second main
  phase (CR 505.1 and 505.1a, checked: every main phase after the first is a postcombat main phase, but only one is the
  second), never `postcombatMainPhase`. That covers Survival, which was `postcombatMainPhase` +
  `SurvivalTriggerEffect`.
- Which wrappers are intervening "if"s was decided by the Oracle text: the card has an "At …, if" ability, or it is in
  the script's `INTERVENING_CARDS` (the keywords above; one Oracle ability split over two abilities, as Arvad, Asylum
  Visitor and Kjeldoran Home Guard are; werewolf and day/night back faces, which have no Oracle text of their own in the
  cache, and Marchesa, Resolute Monarch).

**The engine.** The readers see what they saw before, as in run 4a:

- `Card.getLegacyTriggerEffects(Trigger, Zone)` / `getLegacyTriggerEffectRegistrations(Trigger, Zone)` give the
  abilities with that trigger functioning in the zone, each effect wrapped in its ability's options by
  `TriggeredAbility.legacyEffects()` (from the slot index, so an effect keeps its identity across calls);
  `GameQueryService.getEffectiveGraveyardLegacyTriggerEffects` is their graveyard form. `legacyEffects()` now also
  gives the limit `ONCE` as an `OnceOnlyTriggerEffect` inside the intervening "if", the form the step cards wrote; its
  only other user, Sleuth Instructor (turned face up), is collected through the trigger pipeline, which reads the
  options and not this view.
- `scripts/step14-step-readers.py` moved every card-ability reader of a step trigger to the legacy view:
  `StepTriggerService` (all but the Saga chapter reader), `Permanent.hasCumulativeUpkeep`, `GameSetupService`'s
  conspiracy filter, `PermanentRemovalService` (Wall of Nets' end-of-combat exile), `PermanentCounterSupport`,
  `TargetLegalityService`'s active-player Oath check and `PlanechaseService.trigger`, which reads every planar trigger
  that way, so a plane's intervening-if option is checked as its wrapper was.
- `StepTriggerService.handleMainPhaseTriggers` no longer unwraps Survival: `SurvivalTriggerEffect` and
  `SurvivalTriggerSupport` are deleted, with `GameData.survivalTriggersEvaluated`, which nothing read or wrote any more.

**Cards.** `scripts/step14-step-options.py` (per letter module; `--dry-run` prints the diffs, `--report` the decision
of each ability) converted 660 abilities in 647 card classes; it stops on any wrapped step ability it has no decision
for, so none is decided silently. An effect passed as a local variable is followed to its one definition.
`scripts/step14-step-scan.py` lists the step abilities that still carry a wrapper. Curse of Obsession's and Tenuous
Truce's `TargetPlayerIsActive` wrapper was always true on `endStep(ENCHANTED_PLAYER)` and is dropped.
`scripts/step14-step-docs.py` rewrote the agent-docs rows of the converted cards (`ORACLE_TEXT_EFFECT_MAP.md`, six
`CARD_PATTERNS_*` files); the `SurvivalTriggerEffect` entries of `EFFECTS_INDEX.md` and `EFFECTS_QUICK_REFERENCE.md`
are gone, their `OnceOnlyTriggerEffect` entries name the `ONCE` limit, and `CARD_IMPLEMENTATION_PLAYBOOK.md`'s
transform-trigger and condition rows show the option (by hand).

**Kept, with their reason (the script's `NOT_INTERVENING`).** The wrapper is the effect text's own "if", not an
intervening "if"; the step collectors check a wrapper's condition as the ability triggers whatever its flag says, so
`unless` would change when it is checked, and run 16 decides them with the `interveningIf` flag: Aggression ("destroy
that creature if it didn't attack this turn"), Clockwork Dragon and Clockwork Vorrac (a delayed "at end of combat"
removal written as an end-of-combat ability gated on having attacked or blocked), Kjeldoran Frostbeast (gated on
having been in combat), Laboratory Drudge and Ludevic, Necro-Alchemist ("draw a card if …"), Throat Wolf, and Does
Machines' "If it isn't a creature" (its Level 3 condition is an option). The nested `ConditionalEffect`s (inside a
`SequenceEffect`, `MayEffect` and so on: Glimmer Seeker's two branches, Search for Azcanta's transform) are effect-text
conditions and stay, as in run 4b. Ashling, Rimebound's `TriggerMode.INDEPENDENT` is run 16's; granted abilities
(Tawnos Endures, Okoye's attacks ability) belong to their families.

**Behaviour changes.**

- Survival (Cautious Survivor, Cynical Loner, Defiant Survivor, Glimmer Hoarder, Glimmer Seeker, House Cartographer,
  Improvising Aerialist, Kona, Rescue Beastie, Reluctant Role Model, Rip, Spawn Hunter, Rootwise Survivor, Savior of the
  Small, Shrewd Storyteller, Veteran Survivor, Wary Zone Guard, Effie, Fast Learner): after an
  additional combat phase with no main phase between the two combats, the main phase that follows is the turn's second
  and the ability triggers; before, any additional combat this turn stopped it.
- Acrobatic Cheerleader, Amber-Plate Ainok, Jet Collector, Lost Monarch of Ifnir, Pearl Collector and Scheming
  Silvertongue (the other "second main phase" cards, which were on `postcombatMainPhase` without the Survival wrapper):
  they trigger only in the turn's second main phase, not in an additional main phase after it.

**Tests changed.**

- `StepTriggerServiceTest`: the Survival wrapper test is now "a second-main-phase ability triggers in the turn's second
  main phase and no later one", plus "a 'triggers only once' limit is read from the ability's options"; the two Survival
  queueing tests author the plain ability; the graveyard stub reads `getEffectiveGraveyardLegacyTriggerEffects`.
- `AcrobaticCheerleaderTest`: its helper moved to the postcombat main phase several times in one turn and counted
  each as a new turn's second main phase; it now starts each move in a new turn whose first main phase has begun
  (`mainPhasesBegunThisTurn = 1`), as its tests' names say (3 tests failed before: the later moves were the turn's
  third and fourth main phases).
- `WaryZoneGuardTest.survivalDoesNotTriggerDuringLaterMainPhaseOfSameTurn` modelled the later main phase with
  `combatPhasesThisTurn = 2` only, which is the turn's second main phase after an additional combat; it now sets
  `mainPhasesBegunThisTurn = 2` (a third main phase). New: `survivalTriggersInTheSecondMainPhaseAfterAnAdditionalCombat`.
  The official Survival rulings (2024-09-20) agree: the ability triggers at the second main phase a player takes in
  a turn and not in any later main phase.

**Test runs.**

- Card: the test class of every changed card and every card test that constructs one (944 classes, 9,130 tests):
  the 4 failures above, which pass after the test changes, with Sleuth Instructor's (the other `ONCE` user).
- Application: `StepTriggerServiceTest`, `TriggerPipelineTest`, `CardAbilitiesTest`, `LegacyTriggerBindingsTest`,
  `TriggeredAbilityTest`, `TriggerOptionsTest`, `TriggerConditionTest`, `CardFreezeTest`, `GrantedAbilitiesTest`,
  `TargetLegalityServiceTest`, `PermanentRemovalServiceTest`, `GameQueryServiceTest`, `SimulationCopyCompletenessTest`,
  `TurnProgressionServiceTest`, `PlanechaseServiceTest`, `MiscTriggerTest` (947 tests): per class the failures at
  HEAD (`StepTriggerServiceTest` 16, `TargetLegalityServiceTest` 14, `PlanechaseServiceTest` 2,
  `SimulationCopyCompletenessTest` 1) and 57 in `PermanentRemovalServiceTest` (46), `TurnProgressionServiceTest` (10)
  and `GameQueryServiceTest` (1), mock-wiring failures that fail by the same names at HEAD `daa7858aff` (checked in a
  worktree with the main tree's `card-data-cache`).
- AI: `GameDataDeepCopyTest` passes; `TargetPolarityGuardTest` has its 2 known failures.
- `magical-vibes-webservice`'s test sources don't compile at HEAD (`CardBrowserServiceTest`'s anonymous `CardCatalog`
  lacks `findLoadedPrintingByName`, added in run 3a); unrelated to this run, not fixed here.
- The full suite was not run here.

#### Run 6a: spells, land plays and activations, the generic wrappers

Run 6 (step 13 family 2) was split, with the user, into 6a (this run), 6b (`SpellCastTriggerEffect`'s filter and
conditions, 980 abilities) and 6c (the other condition-as-effect classes). Every spell, land-play and activation
ability (`Triggers.youCastSpell`, `opponentCastsSpell`, …, `youPlayLand`, `youActivateAbility`, …,
`thisManaAbilityResolves`, read by the `SpellAbilityWatch` collectors) is authored without the generic trigger-only
wrappers around its effect, but for the six cards kept below.

**Authoring.**

- The land played is the trigger's filter: `Triggers.youPlayLand().withFilter(cardPredicate)` (`LandPlayTrigger` gained
  a nullable `CardPredicate filter`), for "whenever you play an Island / a Desert / a legendary land / a land that shares
  a card type with the exiled card".
- The activated ability is the activation trigger's filter: `youActivateAbility().withSource(permanentPredicate)` ("an
  ability of an artifact", "of enchanted artifact") and `.withKind(AbilityTrigger.AbilityKind.X)` ("a loyalty /
  ninjutsu / outlast / power-up / boast ability", "with / without {T} in its activation cost": `LOYALTY`, `NINJUTSU`,
  `OUTLAST`, `POWER_UP`, `BOAST`, `WITH_TAP_COST`, `WITHOUT_TAP_COST`). `AbilityTrigger` gained nullable `source` and
  `kind`.
- An intervening "if" (CR 603.4, checked) is `TriggerOptions.DEFAULT.withInterveningIf(c)`: "when you cast this spell,
  if it was kicked / if you have fewer than seven cards in hand / Descend 8", a cast-from-zone condition, a mode's
  ability (Mirrodin Besieged, Glacierwood Siege), Pardic Dragon's "if this card is suspended", Vazi's Treasure mana,
  Hidden Herd's and Hidden Stag's type checks, Fastbond's "if it wasn't the first land". "This ability triggers only
  once each turn" is `ONCE_EACH_TURN` (a keyed wrapper's key is dropped: the limit is keyed by the ability), "do this
  only once each turn" `DO_ONLY_ONCE_EACH_TURN` (CR 603.2h, checked).
- A `TriggeringCardConditionalEffect(new CardTruePredicate(), …)` (Fastbond, Hidden Stag) is dropped.

**The engine.**

- `Trigger.watchKey()` (new, default `this`) is the trigger without the filters the collectors still judge from
  wrappers; `LandPlayTrigger` and `AbilityTrigger` drop their filters. `Card`'s legacy view is keyed by it
  (`getLegacyTriggerEffects(trigger)` / `(trigger, zone)` find a filtered ability by its unfiltered trigger, through
  `TriggeredAbility.hasWatchedTrigger`); the plain view (`getTriggerEffects`) still matches exactly.
- `TriggeredAbility.legacyEffects()` also gives the filters as the wrappers the collectors judged, in the order the
  cards nested them: the ability kind's `Triggering…AbilityConditionalEffect` outermost (the activation collectors
  call `resolveForActivatedAbility` first), then the limit's `OncePerTurnTriggerEffect`, then the land-play
  `TriggeringCardConditionalEffect` or activation `TriggeringPermanentConditionalEffect`, then the intervening "if"'s
  `ConditionalEffect` (inside the land-play filter, where `handleConditionalLandPlay` looks for it). The conditions of
  one ability must agree on those filters (`IllegalStateException` otherwise), so "you or an opponent plays a Desert"
  can be one ability, but a filtered and an unfiltered land-play condition can't share one.
- `SpellAbilityWatch.effectsOf` / `registrationsOf` read the legacy view, so every collector and handler of the family
  sees what it saw. So do the graveyard cast and exhaust readers (`getEffectiveGraveyardLegacyTriggerEffects`) and
  `EffectResolution.hasManaSpentToCastAmount`'s "when you cast this spell" scan.
- The land-play default handlers (`PlayedCardNameTriggerCollectorService`) check an intervening "if" as the ability
  triggers (CR 603.4), as `handleConditionalLandPlay` already did; without that, Fastbond and Hidden Stag, whose
  always-true filter wrapper routed them to the conditional handler, would have been checked only on resolution.
- The wrapper classes stay for the legacy view; run 16 deletes them.

**Cards.** `scripts/step14-spell-scan.py` lists the family's abilities with their wrappers and Oracle text (`--all`,
`--summary`); `scripts/step14-spell-options.py` (per letter module; `--dry-run`, `--report`) converted 65 abilities in
58 card classes (`scripts/step14-spell-converted.txt`), following local variables to their one definition (Haunting
Wind, Artifact Possession, Imprison). `scripts/step14-spell-docs.py` rewrote the agent-docs rows of the converted cards
and marked the `Triggering…AbilityConditionalEffect` entries "don't author"; the implement-card and review-card skills
and `ARCHITECTURE.md` were edited by hand.

**Kept, with their reason (the script's `KEEP`).**

- Bill Potts: one Oracle ability ("cast … or activate …", "triggers only once each turn") over two conditions with
  different effects (a spell copy and an ability copy), which one `TriggeredAbility` can't hold; its two unkeyed
  wrappers share the source's mark.
- The Fourth Doctor: "When you do, create a Food token" is the play permission's reflexive trigger, modelled as two
  abilities (cast, land) sharing one unkeyed once-each-turn mark.
- Folk Hero, Tyvar the Bellicose: granted abilities with `OncePerTurnTriggerEffect.perGrant` (one limit per granting
  permanent), which no `TriggerLimit` says.
- Bloodbraid Marauder: "has cascade as long as …" is a condition on having the ability, checked only as the spell is
  cast (`ConditionalEffect.atTriggerTime`); an intervening "if" would check it again on resolution. Run 16, with the
  `triggerTimeOnly` flag.
- Apex Devastator: four `TriggerMode.INDEPENDENT` cascades, run 16's.

**For runs 6b and 6c.**

- The wrappers nested inside a `SpellCastTriggerEffect` (Edgar Markov's eminence "if", the Cemetery Gatekeeper and
  Cemetery Protector cast halves' `TriggeringCardConditionalEffect`, Sapphire Collector's `OnceOnlyTriggerEffect` inside
  `SpellCastTriggerEffect.nth`, the effect-text `ConditionalEffect`s in `resolvedEffects`) and
  `SpellCastTriggerEffect.withIntervening` / its `intervening` field (36 abilities) are 6b's. The `intervening` field is
  checked with the spell's mana spent and mana value as context (`SpellManaSpentAtLeast`,
  `SpellManaValueEqualsSourceCounters`), which the option's check will need.
- Readers that still match the family's triggers exactly on the plain view: `TriggeredAbilityQueueService`'s stack
  label (a `SpellCastTriggerEffect` with a color filter), `ChoiceHandlerService`'s subtype-choice activation copy and
  `PredicateEvaluationService`'s cascade check. They see the same effects as before (no converted ability is one of
  theirs), but a filtered trigger or an option would hide an ability from them.
- Cemetery Gatekeeper's and Cemetery Protector's land halves could merge with their cast halves once 6b gives the
  cast trigger a filter ("whenever a player plays a land or casts a spell, if it shares a card type …").

**Behaviour changes.** None intended: the collectors read the same wrappers. A once-each-turn limit is now keyed by
its ability instead of the source's unkeyed mark, which matters only for a source with two such abilities (none among
the converted cards: Jin-Gitaxias's were already keyed apart).

**Tests changed.** `SpellAbilityTriggerTest`: five new tests: a filtered land-play or activation trigger is watched by
its unfiltered trigger (and null filters are rejected); the legacy view gives a land-play filter inside the limit and
outside the intervening "if", with a stable identity; it gives an ability kind outside the limit and a source filter
inside it; the conditions of one ability share their land-play filter, and a filtered and an unfiltered condition
can't. No card test changed; Hidden Stag's `doesNotTriggerControllerLandPlayWhileEnchantment` covers the land-play
default handler's trigger-time check through the engine.

**Test runs.**

- Card: the test class of every converted card and every card test that constructs one (91 classes, 947 tests): all
  passing.
- Application: the trigger-model, collector, registry, step, pipeline, watch, freeze, architecture and copy classes
  (532 tests): 23 failures, all failing at HEAD: `StepTriggerServiceTest` 16, `SpellCastTriggerCollectorServiceTest` 4
  (the same names at HEAD `edf279f06f`, checked in a worktree), and the known ones in `SimulationCopyCompletenessTest`,
  `CardImmutabilityArchTest` and `CardEffectTargetingConsistencyTest`.
- AI: the whole module (1,620 tests), the same 10 failures as before.
- The full suite was not run here.

#### Run 6b: `SpellCastTriggerEffect` becomes the cast trigger's filter, options and effects

Every "whenever [you / an opponent / a player] casts a [quality] spell" ability that wrote a `SpellCastTriggerEffect`
(`Triggers.youCastSpell`, `opponentCastsSpell`, `playerCastsSpell`, on the battlefield, in a graveyard, hand, exile or
the command zone, on the card, a target group, a token's abilities or a static grant) is authored as its trigger's
filter, its options and its effects, but for the four cards kept below.

**Authoring.**

- `SpellTrigger` gained a nullable `SpellFilter` (new, `model/trigger/SpellFilter`): what the spell must be
  (`withFilter(cardPredicate)`), what its stack object must be (`withStackEntryFilter(stackEntryPredicate)`: "that
  targets this creature", "cast from your graveyard"), the turn (`duringYourTurn()`, `duringOpponentsTurn()`,
  `notDuringCastersTurn()`), `duringCombat()`, the casting player's nth matching spell this turn (`nthThisTurn(n)`,
  `nthOrLaterThisTurn(n)`), `paidWithManaFromThis()`, `paidWithTreasureMana()`, `withTwoOrMoreCardTypes()` and
  `expend(n)`. `anySpell()` is the filter of any spell. Only a `CAST` trigger takes one. **A cast trigger with a filter is
  what the generic spell-cast collector reads; one without is still collected by its effect's class** (run 6c's
  condition-as-effect classes, and the effects whose collector binds the spell or its caster, such as
  `DealDamageToPlayersEffect`, `GivePoisonCountersEffect` or `MillEffect` on a plain trigger), so a plain
  `Triggers.youCastSpell()` with an ordinary effect does nothing, as before. An explicit filter was needed because the
  class-keyed handlers include ordinary effects that the converted abilities also hold: no rule over the effects
  could tell the two apart.
- The intervening condition is `TriggerOptions.DEFAULT.withInterveningIf(c)` (CR 603.4, checked), "triggers only
  once" around every effect (Sapphire Collector) the `ONCE` limit.
- An outer `MayEffect(SpellCastTriggerEffect(…), prompt)` is a `MayEffect` of the effect (`SequenceEffect.of` for
  several), the mana cost a `MayPayManaEffect(cost, effect, prompt)` ("Pay {N}?" where the targeted collector asked
  that, the graveyard collector's "Pay {N} to return <name> from your graveyard to your hand?" for the three graveyard
  cards).
- The target filter is a target group: dropped when the statement was already on `target(sameFilter)`, otherwise the
  statement is put on `target(filter)`. Brinelin, the Moon Kraken, Hraesvelgr of the First Brood and Necklace of
  Girion share the group their enters ability declares with the same filter; Conciliator's Duelist's bare
  `target(creature, 0, 1)` became its cast ability's group.
- `anyPlayer` is an emblem-only flag the card abilities never read, so it is dropped.

**The engine.**

- `SpellTrigger.watchKey()` drops the filter, so the `SpellAbilityWatch` collectors find the ability.
  `TriggeredAbility.legacyEffects()` gives a filtered cast ability as one `SpellCastTriggerEffect` of all its
  effects (one stack object, CR 603.3, checked), with the filter's fields, the intervening "if" as its `intervening`
  (which the collector checks with the spell's mana spent and mana value, as `withIntervening` did, and wraps around the
  effects for the resolution check), and the limits around it as 6a gave them. A lone plain `MayPayManaEffect` becomes
  the record's mana cost, and on the battlefield or in exile it and a lone plain `MayEffect` become the outer "you may"
  the collector reads; the graveyard, hand and command-zone readers take the cost from the record and a "you may"
  inside it, as those cards wrote them. A written `SequenceEffect` stays one effect.
- The generic collector (`SpellCastTriggerCollectorService.handleGenericSpellCastTrigger`) takes a missing target
  filter from the target group the ability's effects are bound to (directly or through the "you may" / "you may pay"
  around them), and decides "multi-target" from the groups its effects are bound to (Ms. Bumbleflower's two) instead of
  every group of the card, so a card with two
  targeted cast abilities (Deathbringer Liege, Lozhan, Dragon's Legacy, Aragorn, the Uniter, Circu, Dimir Lobotomist,
  Toph, Hardheaded Teacher) or one next to an "up to one" enters target keeps the single-target choice.
- An optional ability is never asked before it is on the stack any more: the cast-time `PendingMayAbility` branch
  (effects that read the mana spent, target a graveyard card or a spell, or choose a mode) wraps the effects in the
  `MayEffect` / `MayPayManaEffect` and continues to the stack branches, so the targets are chosen as it triggers
  (CR 603.3d, checked) and the choice is made as it resolves (CR 603.5, checked).
- The granted-ability readers (`Permanent.getGrantedTriggerEffects`, `GrantedTriggeredAbilitySupport
  .grantedTriggerEffects`) find abilities by `watchKey()` (`TriggeredAbility.hasWatchedTrigger(Trigger)`, new).
  `TriggeredAbilityQueueService`'s stack label reads the legacy view. `BecomeCopyOfTargetCreatureUntilEndOfTurnEffect
  Handler` keeps the retained ability whole, its filter included (Hulkling, Young Avenger).
  `TextChangeTransformer` rewrites inside a `MayEffect`, so a text change still reaches the spell filter of an
  optional cast ability (Mind Bend and Trait Doctoring on Dragon's Claw).

**Cards.** `scripts/step14-spell-cast-options.py` (per letter module; `--dry-run`, `--report`; run twice for an ability
nested in a token's abilities, Tatsunari, Toad Rider) converted 1,085 abilities in 1,043 card classes; Brinelin,
Hraesvelgr, Necklace of Girion, Conciliator's Duelist, Phoenix of Iteration (a helper's effects) and Hex, Kellan's
Companion (one ability on the battlefield and in exile, CR 113.6k, checked) were done by hand.
`scripts/step14-spell-cast-converted.txt` lists all 1,091. `scripts/step14-spell-cast-docs.py` rewrote 112 agent-docs
spans; the `SpellCastTriggerEffect` entries of `EFFECTS_INDEX.md` and `EFFECTS_QUICK_REFERENCE.md` say "don't author",
the pattern files carry a note on the rows still written in the old shorthand, and `PREDICATES_REFERENCE.md`,
`ACTIVATED_ABILITY_GUIDE.md`, `LAYER_SYSTEM.md`, `ARCHITECTURE.md` and the implement-card skill were edited by hand.

**Kept, with their reason (the script's `KEEP`).**

- Leshrac's Sigil: "you may pay {B}{B}. If you do, look at that player's hand" pays into an effect that declares a
  hand target while the player is the caster, bound as a non-target; a converted "you may pay" would offer a target.
  Step 15 (the event's player).
- Gale, Conduit of the Arcane: its specialized faces' abilities are built at runtime by `SpecializeGaleEffectHandler`
  from the card's helper.
- Passionate Archaeologist: a granted ability with a target filter ("target opponent"); a granted ability has no card
  target group.
- Emblems (`CreateEmblemEffect`) still hold `SpellCastTriggerEffect`s; they are no triggered abilities, and the
  emblem collector in `TriggerCollectionService` reads them. Delayed and registered spell-cast triggers
  (`RegisterDelayed…SpellCastTriggerEffect`, `RegisterGlobalTriggeredAbility…`) are their own records.

**For run 6c and later.**

- The run 6a readers that matched the family on the plain view: the stack label now reads the legacy view;
  `ChoiceHandlerService`'s subtype-choice activation copy reads `youActivateAbility()` and `PredicateEvaluationService`'s
  cascade check `youCastThisSpell()`, neither of which this run filters; they still match exactly.
- Cemetery Gatekeeper and Cemetery Protector can now merge their land and cast halves (a filtered `youPlayLand` and a
  filtered `youCastSpell` condition in one ability): not done here.
- 6c's classes, once filters too, can drop the "a filtered trigger" marker: when no class-keyed cast handler is left,
  a plain cast trigger can be the generic one and `anySpell()` can go.

**Behaviour changes.**

- Uncover the Moon-Letters ("you may draw cards equal to the amount of mana spent"): its "you may" was asked as the
  spell was cast, before the trigger was on the stack; it is now asked as the ability resolves (CR 603.5). The same
  holds for any optional cast ability whose effect reads the mana spent or targets a graveyard card or a spell; no
  other converted card's tests show it.
- The inner-"you may" cards the legacy view now gives with an outer "you may" (Dragon's Claw, Verduran Enchantress, the
  ki-counter Spirits, …) are judged on the effect inside the may (targets, the spell it refers to): the same choices on
  the same stack object, but read as the outer-"you may" cards always were.
- Mirrodin Besieged and Glacierwood Siege: the chosen-mode intervening "if" is also checked as the ability resolves
  (it can't change).

**Tests changed.**

- `SpellAbilityTriggerTest`: five new tests (a filtered cast trigger is watched by its unfiltered trigger and the
  filter's methods reject a non-cast trigger and bad counts; the legacy view gives one `SpellCastTriggerEffect` of the
  ability's effects with the filter and the intervening "if", stable across calls; the optional forms per zone; the
  limits around it; a granted filtered ability is found by `hasWatchedTrigger`).
- `SpellCastTriggerCollectorServiceTest`: "adds to pending may abilities when wrapped in MayEffect" (the cast-time
  prompt this run removes) is now "puts an optional trigger on the stack, its choice made as it resolves"; new: an
  optional trigger that reads the mana spent goes on the stack with that X; a targeting trigger takes its filter from
  its bound target group and is not the card's "up to one" choice.
- `TextChangeTransformerTest`: new, a color word replacement rewrites the spell filter of an optional spell-cast
  trigger.
- `UncoverTheMoonLettersTest` (4 tests): the "you may" is answered after the trigger resolves (CR 603.5), no longer
  right after the cast.

**Test runs.**

- Card: the test class of every converted card and every card test that constructs one (1,666 classes, 14,729
  tests). The first run had 21 failures in 6 classes: Deathbringer Liege, Lozhan, Dragon's Legacy and Quiet
  Contemplation (the multi-target and sequence fixes above), Mind Bend and Trait Doctoring (the text change inside a
  "you may") and Uncover the Moon-Letters (the test change above). The second run, after the fixes, had 2 (Ms.
  Bumbleflower, fixed by reading every bound group); the 210 classes of the cards with target groups and cast abilities
  (and their users), rerun after that fix, all pass (1,832 tests).
- Application: the trigger-model, collector, registry, step, pipeline, text-change, freeze, architecture and copy
  classes (444 tests): 22 failures, all failing at HEAD: `StepTriggerServiceTest` 16, `SpellCastTriggerCollectorServiceTest`
  3 (CasterLosesLifeOnChosenColorSpellCastEffect, PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect's may,
  same-name artifact search; checked in a worktree at `0f5aadba57`, where the test rewritten above fails too), and the
  known ones in `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and `CardEffectTargetingConsistencyTest`.
- AI: `GameDataDeepCopyTest` passes (no new state).
- The full suite was not run here.

#### Run 6c: the other condition-as-effect classes of family 2 become filters

Every spell, copy and land-play ability that wrote one of the condition-as-effect classes below is authored as its
trigger's filter and the ability's effects, but for the cards kept below. 120 abilities in 117 card classes.

**Authoring.**

- `SpellFilter` gained `castFrom` / `notCastFrom` (a `Zone`: `Triggers.youCastSpell().castFrom(Zone.GRAVEYARD)`,
  `.notCastFrom(Zone.HAND)`, judged from the cast event's zone; the spell counts `nthThisTurn` / `nthOrLaterThisTurn`
  then count only the spells cast from `castFrom`), `kicker` (`.kicked()`, "whenever you cast a kicked spell", once;
  `.eachKick()`, "whenever a player kicks a spell", once for each kick, CR 702.33d checked) and `everyPlayersSpells`
  (`.countingEveryPlayersSpells()`: the counts count every player's spells, "the first noncreature spell of a turn").
  The classes map to:

  | Class | Filter |
  |---|---|
  | `NthSpellCastTriggerEffect(n, effects[, scope[, zone]])` | `[.castFrom(zone)].nthThisTurn(n)[.countingEveryPlayersSpells()]` |
  | `KickedSpellCastTriggerEffect(effects[, perKick])` | `.kicked()` / `.eachKick()` |
  | `FirstMatchingSpellCastTriggerEffect(filter, effects)` | `.withFilter(filter).nthThisTurn(1)` |
  | `FirstMulticoloredSpellCastTriggerEffect` | `.withFilter(new CardIsMulticoloredPredicate()).nthThisTurn(1)` |
  | `FirstNoncreatureSpellCastTriggerEffect` | a noncreature filter, `.nthThisTurn(1).countingEveryPlayersSpells()` |
  | `SpellCastFromHandTriggerEffect` / `CastFromGraveyardTriggerEffect` | `.castFrom(Zone.HAND)` / `.castFrom(Zone.GRAVEYARD)` |
  | `CastFromLibraryTriggerEffect` | a creature filter, `.castFrom(Zone.LIBRARY)` |
  | `TeamworkSpellCastTriggerEffect` | `.withStackEntryFilter(new StackEntryCastWithTeamworkPredicate())` (new) |
  | `ChosenSubtypeSpellCastTriggerEffect(effects[, creatureOnly])` | `.withFilter(new CardHasSourceChosenSubtypePredicate(creatureOnly))` |
  | `SpellCastFromOutsideStartingDeckTriggerEffect` | `.withFilter(new CardNotPredicate(new CardInStartingDeckPredicate()))` (new predicate) |
  | `PlayFromOutsideHandTriggerEffect` | `.notCastFrom(Zone.HAND)`; on a land play `.notPlayedFrom(Zone.HAND)` |
  | `LandPlayFromExileTriggerEffect` | `Triggers.youPlayLand().playedFrom(Zone.EXILE)` |
  | `SpellCopyTriggerEffect(spell, effects, …)` | `Triggers.youCopySpell().withFilter(spell)` / `.anySpell()`, `.withStackEntryFilter(condition)` |

- A copy trigger (`SpellEvent.COPIED`) takes a `SpellFilter` too, of the spell and its stack object only (a copy isn't
  cast, CR 707.10 checked; `SpellFilter.spellAndStackEntryOnly()`). Its filter alone decides which copied spells count:
  every converted ability that didn't take every spell type already filtered on "instant or sorcery" (or "instant").
- `LandPlayTrigger` gained `playedFrom` / `notPlayedFrom` (CR 116.2a checked: a land is played from the zone it was in).
- A target filter is a target group, as in run 6b: a magecraft card's copy half reuses the effects its cast half already
  bound to `target(filter)` (so it is just `addTrigger(...)`), any other is put on `target(filter[, 0, 1])` (Parnesse's
  "up to one target opponent").

**The engine.**

- `TriggeredAbility.legacyEffects()` gives the new cast filters to the `SpellCastTriggerEffect` it builds (new fields
  `castFrom`, `notCastFrom`, `kicked`, `triggerPerKick`, `countEveryPlayersSpells`; `withSpellFilter` and
  `withResolvedEffects` replace two full constructor calls), a filtered copy ability as one `SpellCopyTriggerEffect` of
  all its effects taking every spell type, and a land-play ability with a zone as one `LandPlayFromExileTriggerEffect` /
  `PlayFromOutsideHandTriggerEffect` of its effects (an ability with options, or a land filter with a zone, throws: none
  exists). The spell conditions of one ability must be of one event in the legacy view.
- The generic spell-cast collector takes the cast zone from the event (`TriggerContext.SpellCast.castZone()`), judges
  `castFrom` / `notCastFrom`, counts a kicked spell's kicks (the old handler's `kickedCount`: multikicker payments
  counted, bargain and non-kicker additional costs not) and gives the count as the event value (on the stack entry and
  in the targeted interactions), puts one more copy of the entry on the stack for each further kick of an `eachKick()`
  ability, and counts the spells this turn by `castFrom` and `everyPlayersSpells`.
- The spell-copy collector and the land-from-exile collector take a missing target filter (and the copy collector an
  "up to one" minimum) from the target group the ability's effects are bound to.
- Deleted: the classes `NthSpellCastTriggerEffect`, `KickedSpellCastTriggerEffect`, `FirstMatchingSpellCastTriggerEffect`,
  `FirstMulticoloredSpellCastTriggerEffect`, `FirstNoncreatureSpellCastTriggerEffect`, `SpellCastFromHandTriggerEffect`,
  `CastFromGraveyardTriggerEffect`, `CastFromLibraryTriggerEffect`, `TeamworkSpellCastTriggerEffect`,
  `ChosenSubtypeSpellCastTriggerEffect`, `SpellCastFromOutsideStartingDeckTriggerEffect`, their ten
  `SpellCastTriggerCollectorService` handlers and `SpellCastFromOutsideStartingDeckTriggerCollectorService`.
  `SpellCopyTriggerEffect`, `LandPlayFromExileTriggerEffect` and `PlayFromOutsideHandTriggerEffect` stay as the legacy
  view's records (the last also for Apple of Eden, below); run 16 deletes what is left.

**Cards.** `scripts/step14-spell-condition-options.py` (per letter module; `--dry-run`, `--report`) converted 116
abilities in 115 card classes; Curse of Shaken Faith (one copy-trigger variable shared by two abilities) and Rocco,
Street Chef (one land-trigger variable with a target filter shared by two abilities) were done by hand.
`scripts/step14-spell-condition-converted.txt` lists all 120. `scripts/step14-spell-condition-docs.py` rewrote the
agent-docs spans of the converted cards; the class entries of `EFFECTS_INDEX.md` and `EFFECTS_QUICK_REFERENCE.md` say
"removed" (with the filter to author) or "don't author", and `ARCHITECTURE.md`, `PREDICATES_REFERENCE.md` (the two new
predicates), `TRIGGER_SLOT_TARGETING.md`, three pattern rows and the implement-card skill were edited by hand.

**Kept, with their reason.**

- Apple of Eden, Isu Relic: its two `PlayFromOutsideHandTriggerEffect` abilities are registered global abilities bound
  to the cards exiled with the source ("whenever you play a land or cast a spell this way",
  `TemporaryGlobalTriggerEffect.bindTo`), not a zone filter.
- The Lord of Pain (`FirstSpellCastTriggerEffect`): its target, "another target player", is relative to the caster;
  step 15 (the event's player).
- `CopyControllerActivatedAbilityTriggerEffect` (14 cards): its effect is "copy that ability" and its conditions (life
  paid, sacrificed, X in the cost) read the activation; step 15.
- `ModalSpellCastTriggerEffect` (Riku of Many Paths): the number of modes chosen is the spell's; step 15.
  `CardAdvantageSpellCastTriggerEffect` (Toofer) triggers on the spell's resolution, not its cast.
  `TargetPlayerSpellCastTriggerEffect` (Nuka-Nuke Launcher) is a registered "that player" ability.
  `ChosenNameSpellCastTriggerEffect` (Medomai's Prophecy) is a chapter effect, not a family 2 ability.
- The bespoke condition-and-effect classes whose handlers bind the spell (`EyeOfTheStorm…`, `KnowledgePool…`,
  `PossibilityStorm…`, `DiscordCopy…`, `Rashmi…`, `SunbirdsInvocation…`, `KioraSovereignOfTheDeep…`,
  `HullbreakerHorror…`, `SpellweaverHelix…` / `Volute…`, `FlurryCopyOrDraw…`, `DuelistsConvocationInternational…`,
  `JodahTheUnifier…`, the `PlayedCard…ExiledWith…` classes): step 15.

**For later runs.**

- The magecraft cards ("whenever you cast or copy an instant or sorcery spell") are still two abilities, a filtered cast
  and a filtered copy trigger sharing their effects; one ability of two conditions needs a legacy view per watched
  trigger (`legacyEffects()` gives one record for an ability). So do Cemetery Gatekeeper's and Cemetery Protector's
  land and cast halves, Shadow of the Goblin's and the exile cast and land halves (Urianger Augurelt, Pia Nalaar, Gwen
  Stacy, Klaw, Prosper, Scalesoul Gnome, Rocco).
- A cast trigger without a filter is now collected only by the event-binding effects' handlers (step 15's); the
  "a filtered trigger" marker (`anySpell()`) can go once those are event references.
- Saproling Infestation ("whenever a player kicks a spell") still puts one ability on the stack for each multikicker
  payment, as before; no official ruling on multikicker was found, so the behaviour is unchanged.

**Behaviour changes.** None intended. The converted abilities reach the stack through the generic collector instead of
their own handlers, which differ only in what the generic path always did: the entry carries the caster as its
non-targeting player and the triggering spell's id, and "the first multicolored spell" reads the spell's effective
colors. No converted ability asked a "you may" before it was on the stack (the Nth handler's cast-time prompt had no
user).

**Tests changed.**

- `SpellCastTriggerCollectorServiceTest`: the tests of the deleted classes (an Nth-spell target request, the four
  cast-from-graveyard tests, the first-multicolored and first-noncreature tests) build the ability with its filter and
  take its legacy view (`legacy(trigger, effects…)`), stubbing the spell filters through `everySpellMatches()`.
- `SpellAbilityTriggerTest`: "a filtered cast trigger is watched by its unfiltered trigger" now expects a counter
  trigger, not a copy trigger, to reject a filter; four new tests: the cast-zone, kicker and count-scope filters; their
  legacy `SpellCastTriggerEffect`; a filtered copy trigger and its `SpellCopyTriggerEffect` (and a cast-only filter
  rejected on it); a land-play zone and its records (and the options and conflicting-zone errors).
- No card test changed.

**Test runs.**

- Card: the test class of every changed card and every card test that constructs one (196 classes, 1,696 tests): all
  passing.
- Application: the `service.trigger` package, `SpellAbilityTriggerTest`, `TextChangeTransformerTest`, `CardFreezeTest`,
  `TriggeredAbilityTest`, `StepTriggerServiceTest`, `TriggerPipelineTest`, `TargetLegalityServiceTest`,
  `PredicateEvaluationServiceTest`, `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest`,
  `CardEffectTargetingConsistencyTest` (1,139 tests): 50 failures, all failing at HEAD: `StepTriggerServiceTest` 16,
  `TargetLegalityServiceTest` 14, `SpellCastTriggerCollectorServiceTest` 3 (the run 6b ones), the known one each in
  `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and `CardEffectTargetingConsistencyTest`, and
  `DamageTriggerCollectorServiceTest` 6, `DeathTriggerCollectorServiceTest` 6 and `LandTapTriggerCollectorServiceTest`
  2 (mock wiring; the same names fail at HEAD `2410096d0b`, checked in a worktree).
- AI: not run (no new `GameData`, `StackEntry` or `Permanent` state).
- The full suite was not run here.

#### Run 7: combat

Step 13 family 3, every wrapper kind in one run (with the user): every attack, block, becomes-blocked, unblocked and
combat damage ability that the `CombatWatch` collectors read is authored without trigger-only wrappers, but for the
abilities kept below. 818 abilities in 805 card classes were converted, and 48 "attacks or blocks" pairs were merged.

**Authoring.**

- A restriction on the creature is the trigger's filter: `CombatTrigger.withFilter(PermanentPredicate)`, as in
  `Triggers.creatureYouControlAttacks().withFilter(dragon)`, `Triggers.becomesBlocked().withFilter(white)` or
  `Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(vampire)`. For "this creature blocks / becomes
  blocked" the filter is the creature on the other side (CR 509.3f, checked); for every other event it is the
  attacking, blocking, blocked or damage-dealing creature, judged as the collectors judged the wrapper. A second filter
  is rejected. `CombatTrigger.watchKey()` drops the filter but keeps the same-name and goaded filters, whose own
  collectors read them.
- Multiplicity is a `TriggerOptions` value:
  - "Whenever this creature blocks", once each combat (CR 509.3a, checked), and "whenever one or more creatures you
    control deal combat damage to a player / deal combat damage to you" are `TriggerOptions.ONE_OR_MORE`.
  - "Becomes blocked by a creature", once for each blocker (CR 509.3d, checked), is the new
    `TriggerMultiplicity.EACH_BLOCKER` (`TriggerOptions.EACH_BLOCKER`).
  - The defaults stay "blocks a creature", once per creature blocked (509.3b), and "becomes blocked", once (509.3c).
  - `TriggerMode.ONCE_PER_BLOCK` and `PER_BLOCKER` are no longer authored. `INDEPENDENT` stays (run 16).
- An intervening "if" is `TriggerOptions.DEFAULT.withInterveningIf(c)` (CR 603.4, checked). So is a condition that
  belongs to the trigger event: "attacks alone", "with three or more creatures", "attacks a player who …", "a player who
  is afraid of you", "during your turn", saddled, a Class level, a station threshold, a chosen mode. The other limits:
  "triggers only once each turn" and "attacks for the first time each turn" are `ONCE_EACH_TURN`, "do this only once
  each turn" is `DO_ONLY_ONCE_EACH_TURN` (CR 603.2h), "this ability triggers only once" is `ONCE`.
- "Whenever this creature attacks or blocks" is one ability with both trigger conditions:
  `Card.addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), [options,] effects…)`. A `SpellTarget` form exists
  too, and the new `addSimultaneousTrigger(trigger, options, effects…)` and `TriggeredAbility(trigger, options,
  effects…)` take options.
- On the ally combat damage triggers a plain effect is the usual form. `AllyCombatDamageTriggerEffect` is authored only
  for how the effect reads the event: `boundToDealer(effect)` ("put a +1/+1 counter on it") and
  `opponentsDealtDamage(effect)` (Mindblade Render). Step 15 replaces both with event references.

**The engine.**

- `TriggeredAbility.legacyEffects()` rebuilds the combat wrappers in the order the cards nested them, outermost first:
  the limit, the filter as a `TriggeringPermanentConditionalEffect`, a `BatchedCombatDamageToYouTriggerEffect`, the
  intervening "if", then `OnceOnlyTriggerEffect`.
  - On "a creature you control (or one with this one's name) deals combat damage to a player, a player or battle, or a
    player or planeswalker", every effect is given as an `AllyCombatDamageTriggerEffect` carrying the filter and
    "one or more". An authored record takes these in (`withTriggerOptions`) and keeps the intervening "if" inside it.
    With nothing to take in, it keeps its identity.
  - The multiplicity becomes the registrations' `ONCE_PER_BLOCK` / `PER_BLOCKER`.
  - A multiplicity the collectors can't read (`ONE_OR_MORE` on an attack, `EACH_BLOCKER` off "becomes blocked", a
    registration mode next to a multiplicity) throws, as do combat conditions with different filters.
- `CombatWatch.effectsOf` and `registrationsOf` read the legacy view (`Card.getLegacyTriggerEffects(trigger, zone)`).
  `scripts/step14-combat-readers.py` moved every other combat reader to it:
  - `Permanent.getLegacyTemporaryTriggerEffects` and `getLegacyPersistentTriggerEffects` (new);
  - `TemporaryGlobalTriggeredAbility.legacyEffect()` (new) in the loops over the registered global abilities;
  - `hasWatchedTrigger` in place of `hasTrigger`;
  - `getEffectiveGraveyardLegacyTriggerEffects`, which now also reads the perpetual grants through the new
    `GameData.perpetualLegacyTriggerEffects` (Forgeborn Phoenix's perpetually granted ability).
  - By hand: the token-factory Aura attack check, the Imoen binding, the until-next-turn registration and the
    source-deals-combat-damage dispatcher. `TriggerCollectionService.watches` finds a registered ability by watch key.
- `collectTemporaryGlobalTriggers` keeps the plain effect for `ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER`: a registered
  global "creature you control deals combat damage" ability (Mistway Spy, Surge to Victory) is collected there with
  its plain effect, not as the ally record.
- Raphael, Tag Team Tough's "first time each turn" marker (`markFirstTimeEachTurnSelfCombatDamageSpent`) marks through
  `OncePerTurnTriggerSupport.markIfNeeded`, so it also marks the keyed limit the legacy view gives.
- No wrapper class is deleted: the legacy view still builds them. Run 16 deletes what is left.

**Cards.**

- `scripts/step14-combat-scan.py` lists the family's wrapped abilities; `scripts/step14-combat-chains.py` counts their
  wrapper chains.
- `scripts/step14-combat-options.py` (per letter module; `--dry-run`, `--report`) did the conversion and the merges. It
  follows local variables to their one definition and converts a card-predicate variable to its permanent form
  (Raiyuu was finished by hand). It stops on any wrapped ability it can't decide. Its tables:
  - `INTERVENING_CARDS`: one Oracle ability split over two abilities, Class and station levels, trigger-event
    conditions, double team, a back face without Oracle text.
  - `NOT_INTERVENING`, the effect-text "if"s, which stay effects: Canoptek Wraith, Family's Favor, Karai, Twenty-Toed
    Toad, Mishra, Claimed by Gix.
- A nested `ConditionalEffect` under the intervening one stays an effect (Firemane Commando).
- `scripts/step14-combat-converted.txt` lists every decision.
- Tidus, Yuna's Guardian was done by hand: its `new OncePerTurnTriggerEffect(ally, true)` is `ONE_OR_MORE` with
  `DO_ONLY_ONCE_EACH_TURN` and the filter.
- Script fixes, applied by hand to the cards already written: fully qualified names inside a converted expression
  lost their import (Akki Ronin, Ainok Strike Leader, Choco).
- Aurelia's, Godo's and Gilded Cerodon's comments were edited by script.
- `scripts/step14-combat-docs.py` rewrote 129 agent-docs lines. It turns the wrappers in combat rows into the filter,
  the options and the effect; it adds "don't author" notes to the class entries of `AllyCombatDamageTriggerEffect`,
  `BatchedCombatDamageToYouTriggerEffect`, `TriggeringPermanentConditionalEffect` and
  `TriggeringCardConditionalEffect`; and it replaces `TriggerMode.PER_BLOCKER` / `ONCE_PER_BLOCK`. Rows of the kept
  cards are left alone.
- `ARCHITECTURE.md`, `TRIGGER_SLOT_TARGETING.md` and the implement-card and review-card skills were edited by hand.

**Kept, with their reason.**

- `TriggeringPermanentControllerConditionalEffect` ("a creature you control" on a watch of any creature's), whose
  collectors unwrap it before the permanent filter: Edward Kenway, Etrata, Kestia, Nahiri, Forged in Fury, Swordsman,
  Sharp Scoundrel, and the registered abilities of The Last Ronin, The Revelations of Ezio and Harald Unites the Elves.
  The step that moves combat onto rules events can make them `YOU_CONTROL` relations.
- Card filters whose collector judges the card and queues the effect once per blocked attacker
  (`BlockedCreatureTriggerEffect`): Spined Sliver, Berserk Murlodont (`creatureBecomesBlocked`), Tattered Ratter
  (`creatureYouControlBecomesBlocked`), and Senu, Keen-Eyed Protector (`creatureYouControlAttacksUnblocked`, from exile).
- Flagged `TriggeringPermanentConditionalEffect`s: Mirror Shield's two granted abilities.
- Desert Werewurm: "triggers only once each turn" is written inside its intervening "if". The legacy view would put the
  limit outside, and the `youAttack` collector marks the limit as it unwraps it.
- Pirated Copy: a granted same-name ability that authors its dealer filter in the ally record. The legacy view reads it
  unchanged.
- Effects shared with another family's ability: Bloodhall Priest and Omnivorous Flytrap ("enters or attacks, if …"),
  Naomi, Pillar of Order. One ability with an enters and an attacks condition needs the enters readers to take it.
- The 70 `blocks()` abilities on `TriggerMode.NORMAL` mostly say "whenever this blocks", which is once each combat
  (CR 509.3a), but they trigger once for each creature blocked. This is a pre-existing deviation; it matters only for a
  creature that blocks several. Not changed here.
- Most combat collectors check an intervening "if" only as the ability resolves, not as it triggers. The any-creature
  attack collector, for example, checks only `AttackedTargetIsMonarch` at trigger time. The legacy view rebuilds the
  same wrappers, so nothing changed. A run like 4c's would make every combat collector check the option as the
  ability triggers.

**For later runs.**

- The Aura/Equipment and Curse readings of the `THIS` forms (run 3) are still in place. Splitting them into `ENCHANTED`
  / `EQUIPPED` relations needs the attack-trigger ownership flags (`attackTriggerOwnedByAura`,
  `attackTriggerGrantedByEquipment`) moved onto the trigger. That belongs to the step that moves combat onto rules
  events.
- "Blocks or becomes blocked by a creature" pairs (Arrogant Bloodlord, Giant Shark, the Basilisks) stay two abilities:
  their halves have different multiplicities, and options are per ability.
- Raiyuu, Storm's Edge, Raph & Leo and Godo split one Oracle ability over several abilities with one limit or "if"
  each, as before.
- The once-each-turn limit is keyed by the ability, where the unkeyed wrappers shared the source's mark. Aurelia's and
  Godo's halves now have one limit each; they all trigger on the first attack and none later, as before. Several
  combat collectors still check and set the unkeyed mark whatever the key (the `youAttack` and attack collectors), so
  behaviour is unchanged there.

**Behaviour changes.**

- The 25 "whenever a [quality] creature you control attacks" abilities that filtered on the card
  (`TriggeringCardConditionalEffect` on `creatureYouControlAttacks`) now filter on the attacking permanent, layered:
  the ability triggers when the creature is declared as an attacker (CR 508.3a, checked) and the trigger event matches
  then (CR 603.2, checked), so its characteristics as it attacks count, not its printed card's. A creature that is a Dragon only
  through an effect now triggers Utvara Hellkite; a printed Dragon that lost the type no longer does. The cards: Akki
  Ronin's and the other Samurai-or-Warrior "attacks alone" cards, Utvara Hellkite, Kolaghan, Ojutai, Silumgar, Atarka,
  Sanctum Seeker, Knight's Charge, Mardu Ascendancy, Militia's Pride, Linden, Zoraline, Breeches, Putrefying Rotboar
  and Tolsimir. See `scripts/step14-combat-converted.txt` (`C`).
- "Attacks or blocks" merged cards are one ability each. Only one of the two events can happen to a creature in one
  combat, so nothing observable changed.
- Nothing else changed: the legacy view hands every collector the wrappers and modes it read before.

**Tests changed.**

- `CombatTriggerTest`, eight new tests:
  - a filter is dropped from the watch key but the same-name and goaded filters stay, and a second filter is rejected;
  - the legacy view gives the filter inside the limit and outside the intervening "if", with a stable identity;
  - an ally combat damage ability is read as the ally record, plain, filtered with "one or more", or authored with
    `boundToDealer`;
  - an authored record without options keeps its identity;
  - the multiplicities become `ONCE_PER_BLOCK`, `PER_BLOCKER` and the batched wrapper;
  - an unreadable multiplicity is rejected;
  - an attacks-or-blocks ability is read by both watches;
  - granted and registered abilities are read in the legacy form.
- `UtvaraHellkiteTest`: new test, a Grizzly Bears that is a Dragon through Arachnoform triggers the ability as it
  attacks. This is the behaviour change above.
- No other test changed.

**Test runs.**

- Card: the test class of every converted card and every card test that constructs one (1,258 classes, 10,830 tests).
  The first run had 8 failures in 3 classes, all fixed above: Forgeborn Phoenix (the perpetual reader), Gossip's
  Talent (the intervening "if" inside an authored ally record) and Raphael, Tag Team Tough (the keyed marker).
- After the engine fixes, every card that names an ally or same-name combat damage trigger or a perpetual triggered
  grant, plus the changed cards (194 classes, 1,568 tests), was rerun. One more fix came out of it (Mistway Spy and
  Surge to Victory, the registered-global reader), and the run after that passed.
- Application: the `service.trigger` package and the trigger-model, watch, legacy-slot, freeze, granted-ability,
  combat damage, step, targeting, query, text-change and architecture classes. All failures also fail at HEAD:
  - `StepTriggerServiceTest` 16, `TargetLegalityServiceTest` 14, `SpellCastTriggerCollectorServiceTest` 3,
    `DamageTriggerCollectorServiceTest` 6, `DeathTriggerCollectorServiceTest` 6, `LandTapTriggerCollectorServiceTest` 2,
    and the known one each in `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and
    `CardEffectTargetingConsistencyTest`, as in run 6c.
  - `CombatDamageServiceTest` 40 and `GameQueryServiceTest` 1 (mock wiring). The same counts fail at HEAD
    `bb17de13b7`, checked in a worktree.
- AI: the whole module, with the same 10 failures as before.
- The full suite was not run here.

#### Run 8: damage

Step 13 family 4, every wrapper kind in one run: every damage ability that the `DamageWatch` collectors read is
authored without trigger-only wrappers, but Zagras (below). The family is small: 33 wrapped abilities in 30 card
classes were converted, and three cards whose one Oracle ability was split over several damage abilities were merged.

**Authoring.**

- A restriction on the permanent is the trigger's filter: `DamageTrigger.withFilter(PermanentPredicate)`, as in
  `Triggers.permanentYouControlDealtDamage().withFilter(dragon)` or
  `Triggers.creaturesYouControlDealDamageToPlayer().withFilter(pirate)`. The filter is the permanent the collectors
  judged the `TriggeringPermanentConditionalEffect` against: the damaged permanent for an "is dealt damage" event and for
  "this deals damage" (`DEALS_DAMAGE` with `THIS`, Briarbridge Patrol's "to one or more creatures"), the dealer for
  every other "deals damage" event. A second filter is rejected; `DamageTrigger.watchKey()` drops the filter.
- An intervening "if" is `TriggerOptions.DEFAULT.withInterveningIf(c)` (CR 603.4, checked), as is a condition that
  belongs to the trigger event ("deals 4 or more damage", "is dealt 3 or more damage", "during your turn") and an
  unlocked Room door. "Triggers only once each turn" and "for the first time each turn" are `ONCE_EACH_TURN`, "do this
  only once each turn" `DO_ONLY_ONCE_EACH_TURN` (CR 603.2h, checked).
- New `TriggerMultiplicity.EACH_SOURCE` (`TriggerOptions.EACH_SOURCE`): "whenever a source deals damage to this" is
  once for each source in one damage event, where simultaneous combat damage from several creatures is several
  occurrences of one event (CR 603.2c, checked). Only damage triggers have it (Nested Ghoul).
- New `TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE`: once each turn for each creature the event names, keyed by the
  source and the creature (Stensia, the non-global `OncePerTurnPerCreatureTriggerEffect`). Only the legacy collectors
  read it; the trigger pipeline's `TriggeredAbilityStackService` throws on it. The counter and tap runs can use it for
  Stalwart Successor, Captain America and Tale of Katara and Toph; the global form (`global = true`) needs its own value.
- One Oracle ability over several damage watches is one ability: `addTrigger(List.of(t1, t2, …), [options,] effects)`
  (Stensia's three "a creature deals damage to one or more players" watches, Flesh Reaver's "a creature or opponent",
  Vraska, Swarm's Eminence's "a player or planeswalker", with the same filter on both).

**The engine.**

- `TriggeredAbility.legacyEffects()` rebuilds the damage wrappers in the order the cards nested them, outermost
  first: `PerDamageSourceTriggerEffect` (each source), the filter as a `TriggeringPermanentConditionalEffect`, the limit
  (`OncePerTurnTriggerEffect` keyed by the ability, `markOnAcceptance`, or `OncePerTurnPerCreatureTriggerEffect`), the
  intervening "if", then `OnceOnlyTriggerEffect`. The damage filter sits outside the limit, where combat's sits inside
  it: The Sensational She-Hulk nested it so, and its collector dispatches on the outer class. `ONE_OR_MORE` and
  `EACH_BLOCKER` on a damage condition, `EACH_SOURCE` off one, and damage conditions with different filters throw.
- `DamageWatch.effectsOf` and `registrationsOf` read the legacy view. `scripts/step14-damage-readers.py` (run 7's
  reader script on the damage watches, plus the readers that take a `DamageWatch watch` parameter or loop over
  watches) moved the temporary, persistent, graveyard and registered-global readers to their legacy forms (25 calls in
  `TriggerCollectionService`, `DamageSupport` and `CombatDamageService`).
- By hand: the "a source you control deals damage to you" collector (Stick, Fearless Mentor) marked the unkeyed
  once-each-turn mark whatever the wrapper's key, so with the keyed limit of the legacy view it never stopped
  triggering; it marks through `OncePerTurnTriggerSupport.markIfNeeded` now. The other collectors of the converted
  limits (Hercules, Molten Lavamancer) already did.
- No wrapper class is deleted: the legacy view still builds them. Run 16 deletes what is left.

**Cards.**

- `scripts/step14-damage-scan.py` lists the family's wrapped abilities; `scripts/step14-damage-chains.py` counts their
  wrapper chains (both run 7's scripts on the damage factories).
- `scripts/step14-damage-options.py` (per letter module; `--dry-run`, `--report`) did the conversion and the merges,
  with run 7's helpers. It follows local variables to their one definition and stops on any wrapped ability it can't
  decide. Its tables: `QUALIFIERS` (`EventValueAtLeast`, `SourceRoomDoorUnlocked`), `INTERVENING_CARDS` (Molten
  Lavamancer's "during your turn") and `MERGE` (the three cards above). `scripts/step14-damage-converted.txt` lists
  every decision.
- `scripts/step14-damage-docs.py` rewrote 12 agent-docs lines and added a "don't author" note to
  `OncePerTurnPerCreatureTriggerEffect`'s `EFFECTS_INDEX.md` entry. `ARCHITECTURE.md` and the implement-card and
  review-card skills were edited by hand.
- Check: every card's `DamageWatch` registrations were dumped before and after the conversion (a throwaway test,
  deleted): they are equal but for the once-each-turn key of Hercules, Molten Lavamancer and Stick, which is now the
  ability's.

**Kept, with their reason.**

- Zagras, Thief of Heartbeats: `TriggeringPermanentConditionalEffect.combatDamageOnly` ("deals combat damage to a
  planeswalker") is a flagged wrapper with no predicate. A combat trigger for "a creature you control deals combat
  damage to a planeswalker" (or a combat-only flag on the damage trigger) can replace it.
- The effect-text "unless"s (`ConditionalEffect.unless`: Xyru Specter, The Incredible Hulk, Cecil, Dark Knight) are
  effects.
- The mixed readings step 13 run 4 listed stay: `creatureYouControlDealsDamageToCreature()` effects that say whether
  the dealer is this creature, equipped creature or any creature (`DestroyDamagedCreatureEffect` and its kin), the
  `sourceDealsDamage()` and `creatureDealsDamageToYouOrYourPermanent()` effects that filter their source, the
  handler-enforced "source you control" and "permanent you control" checks. They are event-reading effects, not
  wrappers; step 15 or the step that moves damage onto rules events splits them.
- Most damage collectors check an intervening "if" only as the ability resolves; the legacy view rebuilds the same
  wrappers, so nothing changed. A run like 4c's would make them check it as the ability triggers.

**Behaviour changes.** None: the legacy view hands every collector the wrappers it read before. The merged abilities
trigger on the same events: their watches are disjoint, and Stensia's per-creature limit was already shared by the
source.

**Tests changed.**

- `DamageTriggerTest`, seven new tests: a filter is dropped from the watch key and a second one rejected; the legacy
  view gives the filter outside the limit and the limit outside the intervening "if", with a stable identity; "triggers
  only once each turn" is keyed by the ability; "each source" is the outermost wrapper and "each creature" the limit;
  an unreadable multiplicity or two filters are rejected; an ability with several damage conditions is read by each
  watch; granted abilities are read in the legacy form.
- No other test changed.

**Test runs.**

- Card: the test class of every converted card (Zagras included), the front faces of The Sensational She-Hulk and The
  Incredible Hulk (Jennifer Walters, Bruce Banner) and every card test that constructs one (39 classes, 352 tests): all
  passing.
- Application: the `service.trigger` and `model.trigger` packages and the freeze, triggered-ability, combat damage,
  turn-cleanup, planechase, query, targeting, step and architecture classes (276 classes, 1,432 tests). Every failure
  also fails at HEAD:
  - the same counts as run 7: `StepTriggerServiceTest` 16, `TargetLegalityServiceTest` 14,
    `SpellCastTriggerCollectorServiceTest` 3, `DamageTriggerCollectorServiceTest` 6, `DeathTriggerCollectorServiceTest`
    6, `LandTapTriggerCollectorServiceTest` 2, `CombatDamageServiceTest` 40, `GameQueryServiceTest` 1, and the known one
    each in `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and `CardEffectTargetingConsistencyTest`;
  - `TurnCleanupServiceTest` 10 and `PlanechaseServiceTest` 2, and `DamageTriggerCollectorServiceTest`'s 6: the same
    test names fail at HEAD `35b3fbf3e1`, checked in a worktree with the main tree's `card-data-cache`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 9: counters

Step 13 family 5, every wrapper kind in one run: every counter ability that the `CounterWatch` collectors read is
authored without trigger-only wrappers, but the abilities kept below. 31 abilities in 29 card classes were converted;
Ant-Man, Colony Commander's and Ichor Aberration's split halves were merged into one ability each, and the
`youPutPlusOneCountersOnCreatureYouControlOnceEachTurn()` event step 13 kept apart is gone.

**Authoring.**

- A further restriction on the permanent is the trigger's filter: `CounterTrigger.withFilter(PermanentPredicate)`, kept
  in the new `qualifier` component because `filter` already holds the type the watch is keyed by:
  `Triggers.youPutPlusOneCountersOnAnotherCreature().withFilter(new PermanentIsColorlessPredicate())` (Omarthis),
  `Triggers.youPutCountersOnCreatureYouControl().withFilter(GOBLIN_ORC_ARMY)` (The Great Goblin). A second filter is
  rejected.
- "You" and "each counter" are events, as step 13 made the other "you put" and "a … counter" wordings, with new
  factories: `youPutCountersOnCreatureYouControl()` (`YOU_PUT_COUNTERS`, `YOU_CONTROL`; Mikey & Leo, The Great Goblin),
  `youPutPlusOneCountersOnThis()` (`YOU_PUT_PLUS_ONE_COUNTERS`, `THIS`; Exemplar of Light, Blaster),
  `youRemoveTimeCounterFromThisInExile()` (new `YOU_REMOVE_TIME_COUNTER_IN_EXILE`, from exile by default; Watcher of
  Hours) and `plusOneCounterPutOnThis()` (new `EACH_PLUS_ONE_COUNTER_PUT`; Fathom Mage, Bloodcrazed Hoplite).
  `TriggerMode.PER_COUNTER` is no longer authored.
- An intervening "if" is `TriggerOptions.DEFAULT.withInterveningIf(c)` (CR 603.4, checked), as is a condition of the
  trigger event: "during your turn" (Contagion Dispenser, Brotherhood Scribe), a chosen mode (Hollowmurk Siege), "if this
  card is in your graveyard" (Moss-Pit Skeleton, Ichor Aberration), and "when the last time counter is removed" (the
  vanishing sacrifice of Calciderm, Chronozoa, Aven Riftwatcher and Regenerations Restored, CR 702.63a, checked; Veiling
  Oddity's and Riftmarked Knight's suspend `NotCondition(SourceCardSuspended)`). "Triggers only once each turn" / "for
  the first time each turn" are `ONCE_EACH_TURN`, "do this only once each turn" `DO_ONLY_ONCE_EACH_TURN` (CR 603.2h,
  checked; Terrasymbiosis, Earth Kingdom General), Stalwart Successor's "if it's the first time counters have been put
  on that creature this turn" run 8's `ONCE_EACH_TURN_FOR_EACH_CREATURE`.
- Ichor Aberration's "whenever you proliferate, if this is in your graveyard or on the battlefield" is one ability with
  `new TriggerCondition(Triggers.youProliferate(), AbilityZones.of(Zone.BATTLEFIELD, Zone.GRAVEYARD))`. Ant-Man's
  "whenever you put a +1/+1 counter on a creature" is one `youPutPlusOneCountersOnCreature()` ability with
  `ONCE_EACH_TURN`, where a "creature you control" (once-each-turn event) half and an "another creature" half shared the
  unkeyed mark.

**The engine.**

- `CounterTrigger.watchKey()` drops the qualifier and gives the four events above the event whose collector reads them
  (`CounterTrigger.watchedEvent()`): `COUNTERS_PUT`, `PLUS_ONE_COUNTERS_PUT`, `TIME_COUNTER_REMOVED_IN_EXILE`. Only those
  (event, relation) pairs are mapped; every other counter trigger is its own key but for its qualifier.
- `TriggeredAbility.legacyEffects()` rebuilds the counter wrappers in the order the cards nested them, outermost first:
  the "you" wrapper (`YouPutCounterOnControlledCreatureTriggerEffect`, `YouRemoveTimeCounterFromExiledCardTriggerEffect`),
  the qualifier as a `TriggeringPermanentConditionalEffect`, the limit (keyed `OncePerTurnTriggerEffect`,
  `markOnAcceptance`, `OncePerTurnPerCreatureTriggerEffect`), the intervening "if", then `OnceOnlyTriggerEffect`; the
  each-counter event becomes the registrations' `PER_COUNTER`. Any multiplicity other than `EACH`, a registration mode
  next to the each-counter event, and counter conditions of one ability with different qualifiers or watched events
  throw.
- `CounterWatch.effectsOf` and `registrationsOf` read the legacy view. By hand (three calls, no reader script): the two
  graveyard readers (`GRAVEYARD_ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_CREATURE`, `GRAVEYARD_ON_CONTROLLER_PROLIFERATES`)
  use `getEffectiveGraveyardLegacyTriggerEffects`, and the "counters put on this" collector reads a granted ability
  (Danny Pink) with `hasWatchedTrigger` and its `legacyEffects()`.
- The "one or more counters are put on a creature you control" collector (`fireCounterPutOnControlledCreatureTriggers`)
  checked the intervening "if" before the limit; it now peels the wrappers in the legacy view's order ("you", filter,
  limit, then the "if") and sets the limit's mark only for an effect it queues. Hollowmurk Siege's mode "if" was outside
  its limit; the legacy view puts it inside, and the reorder keeps the limit unmarked when the mode doesn't match.
- `CounterEvent.YOU_PUT_PLUS_ONE_COUNTERS_ONCE_EACH_TURN`, its factory, `CounterWatch.ON_CONTROLLER_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_CREATURE`
  and its collector (`fireControllerPutPlusOnePlusOneCountersOnCreatureTriggers`, one stack object per effect, a
  `MayEffect` through `queueMayAbility`) are deleted: Terrasymbiosis is read by the
  `youPutPlusOneCountersOnCreatureYouControl()` collector, Ant-Man by the `youPutPlusOneCountersOnCreature()` one.
- No wrapper class is deleted: the legacy view still builds them. Run 16 deletes what is left.

**Cards.**

- `scripts/step14-counter-scan.py` (run 8's scan on the counter factories) lists the family's wrapped abilities.
- `scripts/step14-counter-options.py` did the conversion: the family is small, so it is a table of exact replacements
  per card class, which stops on a replacement that doesn't match, plus the import fix-up (unused wrapper imports
  dropped, `TriggerOptions` / `TriggerLimit` / `Zone` added).
- `scripts/step14-counter-docs.py` rewrote 25 agent-docs lines (exact replacements) and added "don't author" notes to
  the entries of `YouPutCounterOnControlledCreatureTriggerEffect`, `YouRemoveTimeCounterFromExiledCardTriggerEffect` and
  `OncePerTurnPerCreatureTriggerEffect`. The three classes' Javadoc, `ARCHITECTURE.md` and the implement-card and
  review-card skills were edited by hand.
- `scripts/step13-counter-slots.tsv` is step 13's record and still names the deleted once-each-turn factory; the scan
  script only reads its factory names.
- Check: every family card's `CounterWatch` registrations were dumped before and after the conversion (a throwaway
  test, deleted). They are equal but for the once-each-turn keys, which are now the abilities', and the changes listed
  here: Ant-Man and Terrasymbiosis on their new watches, Hardened Bonds on the +1/+1 watch, Hollowmurk Siege's "if"
  inside its limit and Moss-Pit Skeleton's "if" outside its `MayEffect`.

**Kept, with their reason.**

- "When the Nth [kind] counter is put on this" (`ConditionalEffect.atTriggerTime(new SourceCounterThreshold(N, kind),
  …)`): the nine plan-counter enchantments (Villainous Syndication, Rewrite History, Robot Domination, Political Triumph,
  Glorious Purpose, Death to Our Enemies, Doom Reigns Supreme, Claim the Kingdom, Construct a Cosmic Cube), Midnight
  Clock and Shang-Chi. The condition belongs to the event and is checked only as the ability triggers (the collector
  compares the counts before and after the placement); an intervening "if" would check it again on resolution. Run 16,
  with the `triggerTimeOnly` flag (as Bloodbraid Marauder).
- Effect-text "if"s, which stay effects: Regenerations Restored's "Then if this enchantment has no time counters on it"
  (`ConditionalEffect.unless`), Venser, Corpse Puppet's first mode, Auntie Ool's "draw a card if you control that
  creature. If you don't …" (still two abilities for one Oracle ability, as before).
- Event-reading effects (step 15): Rikku's `MakeCreatureUnblockableEffect.forTriggeringPermanentNotBlockableByOpponents()`
  and Kros's `GoadTriggeringCreatureUntilNextTurnEffect`.

**Behaviour changes.**

- Hardened Bonds ("whenever a +1/+1 counter is put on a creature you control", YTDM 16) was on
  `countersPutOnCreatureYouControl()` and triggered on a counter of any kind. It is on
  `plusOneCountersPutOnCreatureYouControl()` now, with `ONCE_EACH_TURN`. Rules fix, with a card test.
- Terrasymbiosis: its "you may draw that many cards" was asked through `queueMayAbility` before the ability was on the
  stack; it now goes on the stack and is asked as it resolves (CR 603.5, checked), and accepting it marks "do this only
  once each turn" through the stack entry, as Earth Kingdom General's did.
- Moss-Pit Skeleton: its may question was asked before the ability was on the stack (the graveyard collector queues a
  top-level `MayEffect`); the ability is now the intervening "if" around the `MayEffect`, so it goes on the stack and
  the question is asked on resolution, after the "if" is checked again.
- Ant-Man: the merged ability is read by the "you put +1/+1 counters on a creature" collector, which every +1/+1
  placement path calls; the old "another creature" half was called only from the placement effects. A +1/+1 counter
  you put on another player's creature through one of the other paths (damage prevention to counters, the multi-
  permanent choice handlers) now triggers it, as its Oracle text says.
- Nothing else changed: the legacy view hands every other collector the wrappers and modes it read before.

**For later runs.**

- The plan-counter `atTriggerTime` thresholds above (run 16).
- Several counter collectors still check and set the unkeyed once-each-turn mark whatever the key (the +1/+1 on
  this, Human, another creature, controlled permanent and "counters put on a creature you control" collectors); the
  "counters put on this" collector honours the key.
- Most counter collectors check an intervening "if" only as the ability resolves, or not at all (the graveyard +1/+1
  collector relies on the zone); the legacy view rebuilds the same wrappers, so nothing changed. A run like 4c's would
  make them check it as the ability triggers.
- Step 15: as step 13 run 5 listed.

**Tests changed.**

- `CounterTriggerTest`: the test that kept the once-each-turn event apart is gone (the event is); eight new tests: a
  filter is dropped from the watch key and a second one rejected; the "you" and each-counter events are watched by
  the event about any player; the legacy view gives "you put" and the filter outside the limit and the limit outside
  the intervening "if", with a stable identity; the other limits and the "you remove" wrapper; the each-counter event
  is a `PER_COUNTER` registration; an unreadable multiplicity or two readings are rejected; an ability that functions
  from two zones is read by both watches; granted abilities are read in the legacy form.
- `HardenedBondsTest`: new test, a shield counter put on a creature you control doesn't trigger it (and a +1/+1 counter
  later that turn still does). This is the rules fix above; it fails with the old trigger.
- No other test changed.

**Test runs.**

- Card: the test class of every card of the family (all 116, converted or not) and every card test that constructs
  one (174 classes, 1,429 tests): all passing; then `HardenedBondsTest` again with the new test.
- Application: the `service.trigger` and `model.trigger` packages and the freeze, triggered-ability, architecture,
  simulation-copy, deep-copy, targeting, step, query, activated-ability execution, placement, as-enters, combat, combat
  damage, timestamp, stack-resolution and graveyard/library handler classes (1,606 tests): 161 failures, exactly the
  test names that fail at HEAD `3ae62c89dc` (1,599 tests there), checked in a worktree with the main tree's
  `card-data-cache`: run 8's counts, plus the mock wiring of `ActivatedAbilityExecutionServiceTest` (35),
  `BattlefieldPlacementServiceTest` (24), `CombatServiceTest` (5), `PermanentTimestampTest` (5) and
  `PutTargetCardsFromGraveyardOnBottomOfLibraryEffectHandlerTest` (1).
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 10: becomes the target

Step 13 family 6, every wrapper kind in one run: every becomes-the-target ability that the `TargetWatch` collectors
read is authored without trigger-only wrappers, but the two kept below. The family is small: 36 card classes were
changed, 26 of them converted and 10 of them losing an ability that never triggered (below).

**Authoring.**

- A restriction on the permanent is the trigger's filter: `TargetTrigger.withFilter(PermanentPredicate)`, kept in the
  new `qualifier` component because `filter` already holds the creature type the watch is keyed by:
  `Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility().withFilter(dragon)` (Thunderbreak Regent, Scalelord
  Reckoner), Diffusion Sliver, Silverfur Partisan, Swarm Shambler, Fey Steed, Daru Spiritualist. A second filter is
  rejected.
- "Whose spell" is the event, as run 9 made "you put": new `TargetEvent.BECOMES_TARGET_OF_YOUR_SPELL` and
  `PLAYER_OR_PERMANENT_BECOMES_TARGET_OF_YOUR_ABILITY`, and the existing `BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY` with
  `THIS`, with new factories: `becomesTargetOfYourSpell()` (Stormchaser Drake, Illuminator Virtuoso),
  `becomesTargetOfYourSpellOrAbility()` (the eleven valiant cards) and `playerOrPermanentBecomesTargetOfYourAbility()`
  (Loki, God of Mischief). "Of a spell or ability an opponent controls" on this permanent is ward's trigger,
  `becomesTargetOfOpponentSpellOrAbility()`, whatever its effect: Lae'zel, Githyanki Warrior's cast-time grant and
  Dragon's Disciple's "Dragons you control have ward {1}" (now the usual ward grant,
  `new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(1))`).
- "For the first time each turn" (valiant) and "this ability triggers only once each turn" (Professor Hojo, Loki,
  Niambi's perpetual grant) are `ONCE_EACH_TURN`. Catch of the Day's chosen-mode ward is an
  intervening "if" (CR 603.4, checked), as run 7 and run 9 made chosen modes.

**The engine.**

- `TargetTrigger.watchKey()` drops the qualifier and gives the three narrowed events the event whose collector reads
  them (`TargetTrigger.watchedEvent()`): `BECOMES_TARGET_OF_SPELL`, `BECOMES_TARGET_OF_SPELL_OR_ABILITY` (both only with
  `THIS`) and `PLAYER_CHOOSES_TARGETS`.
- `TriggeredAbility.legacyEffects()` rebuilds the target wrappers in the order the cards nested them, outermost first:
  the event's wrapper (`TriggeringSpellControllerConditionalEffect`, `AbilityTargetChoiceTriggerEffect`), the filter as
  a `TriggeringPermanentConditionalEffect`, the limit (keyed `OncePerTurnTriggerEffect`), the intervening "if". A
  multiplicity other than `EACH`, and target conditions of one ability with different filters or watched events, throw.
- `TargetWatch.effectsOf` and `registrationsOf` read the legacy view, and the spell-or-ability and ward collectors read
  the temporary and persistent grants through `getLegacyTemporaryTriggerEffects` / `getLegacyPersistentTriggerEffects`
  (four calls, by hand). The static-grant reader (`grantedTriggerEffects`) already gave the legacy view.
- The "this becomes the target of a spell or ability" collector checked and set the unkeyed once-each-turn mark; it
  checks and marks the ability's key through `OncePerTurnTriggerSupport` now, so the keyed limit of the legacy view still
  stops it. The Hojo and target-choice collectors already did.
- The ward watch's non-counter collector (`collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers`) reads the
  abilities granted to the permanent itself (temporary and persistent) as well as its printed ones, and reads them while
  the printed ones are suppressed (face down, cloaked, abilities lost), as the spell-or-ability collector did for
  Lae'zel's grant. No such grant with a non-counter effect existed before this run.
- `TriggeringSpellOpponentControllerConditionalEffect` is deleted (no card, and the legacy view doesn't build it),
  with its check in the spell-or-ability collector. `TriggeringSpellControllerConditionalEffect` and
  `AbilityTargetChoiceTriggerEffect` stay for the legacy view; run 16 deletes them.

**Cards.**

- `scripts/step14-target-scan.py` (run 9's scan on the target factories) lists the family's wrapped abilities.
- `scripts/step14-target-options.py` (`--dry-run`, `--report`) did the conversion: it peels the wrappers of each
  family ability, outermost first, into the event, filter and options, re-indents what is left and stops on a wrapper
  it can't decide. Its tables: `INTERVENING` (Catch of the Day), `DELETE` (the disguise abilities below),
  `NOT_INTERVENING` and `KEEP`. A second pass (`--reformat-filters`, now part of the conversion) moved the filters the
  first pass wrote on the trigger's line to a line of their own. Running it again changes nothing.
- The ten disguise cards' `addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new ConditionalEffect(new
  SourceIsFaceDown(), new CounterUnlessPaysEffect(2)))` are deleted: Sanguine Savior, Rakish Scoundrel, Mistway Spy,
  Museum Nightwatch, Faerie Snoop, Exit Specialist, Defenestrated Phantom, Bayek of Siwa, Alley Assailant, Aveline de
  Grandpré. The ability never triggered: the ward collector skips a face-down permanent's printed abilities, and the
  condition is false face up. A permanent cast face down with disguise gets ward {2} from the engine
  (`Permanent.setFaceDownAsDisguised`, `enqueueCloakedWardTrigger`), as Rampaging Ursaguana already relied on.
  `SourceIsFaceDown` stays (an activation condition uses it).
- `scripts/step14-target-docs.py` rewrote 16 agent-docs lines (exact replacements): the target rows, the "don't author"
  entries of `TriggeringSpellControllerConditionalEffect` and `AbilityTargetChoiceTriggerEffect`, the deleted class's
  entries, and the disguise pattern row. The two classes' Javadoc, `ARCHITECTURE.md` and the implement-card and
  review-card skills were edited by hand.

**Kept, with their reason.**

- Skophos Maze-Warden: `TriggeringAbilityFromNamedLandConditionalEffect` is a condition on the targeting ability's
  source (an ability of a land you control named Labyrinth of Skophos, targeting another creature), which no
  `TargetTrigger` field holds. A source filter on the trigger can replace it.
- Rayne, Academy Chancellor's `ConditionalEffect(new Enchanted(), …)` is effect text ("you may draw an additional card
  if Rayne is enchanted"), still a second ability of the one Oracle ability, as before.
- Event-reading effects (step 15): `DealDamageToTriggeringSpellControllerEffect` (Thunderbreak Regent, Retromancer,
  Bonecrusher Giant), `TriggeringSpellControllerSacrificesPermanentsEffect` (Lava Runner) and
  `TriggeringObjectControllerGainsControlOfEnchantedPermanentEffect` (Fractured Loyalty).
- Fey Steed keeps "you or a permanent you control" with a creature-or-planeswalker filter, and Daru Spiritualist "a
  creature" with a "you control" filter: their collectors judge the filter as before. `YOU_CONTROL` watches of those
  events would say it directly.

**Behaviour changes.**

- Dragon's Disciple: "Dragons you control have ward {1}" was a "becomes the target of a spell or ability" grant with
  an opponent-controller wrapper, so effects that stop ward abilities from triggering didn't see it. It is ward's
  trigger now. Rules fix, with a card test (Nowhere to Run stops it).
- Lae'zel, Githyanki Warrior: its granted ability is collected by the ward watch's non-counter collector instead of
  the spell-or-ability collector; it triggers on the same events and goes on the stack the same way.
- The valiant "first time each turn" limit is keyed by the ability, where the unkeyed mark was shared with the source's
  other unkeyed once-each-turn abilities. None of the converted cards has another.
- Nothing else changed: the legacy view hands every other collector the wrappers it read before.

**For later runs.**

- Skophos Maze-Warden's source condition (above), and the Aura/Equipment readings of the `THIS` forms (step 13 run 6).
- The ward collector checks Catch of the Day's intervening "if" only as the ability triggers (it unwraps the
  condition), not again as it resolves (CR 603.4); the chosen mode can't change, so nothing is observable. No other
  target ability has an intervening "if" left.
- Step 15: as step 13 run 6 listed.

**Tests changed.**

- `TargetTriggerTest`, six new tests: a filter is dropped from the watch key and a second one rejected; the narrowed
  events are watched by the events about anyone; the legacy view gives the controller wrapper outside the keyed limit,
  with a stable identity, and the filter outside the intervening "if"; the other narrowed events' wrappers; an
  unreadable multiplicity or two readings are rejected; granted abilities are read in the legacy form.
- `DragonsDiscipleTest`: new test, Nowhere to Run stops the granted ward from triggering. This is the rules fix above.
  The old grant was collected by the spell-or-ability collector, which doesn't check ward suppression (reasoned from
  the code, not run against the old grant: its wrapper class is deleted).
- No other test changed.

**Test runs.**

- Card: the test class of every card that uses a becomes-the-target factory (converted or not, ward included), Nowhere
  to Run, and every card test that constructs one of them (553 classes, 5,080 tests): all passing, the new Dragon's
  Disciple test included.
- Application: the `model.trigger` and `service.trigger` packages and the freeze, architecture, simulation-copy,
  deep-copy, triggered-ability, target-legality, counter-spell, counter-unless, granted-ability, targeting-consistency
  and legacy-slot classes (885 tests): 36 failures, all known from HEAD: `TargetLegalityServiceTest` 14,
  `DamageTriggerCollectorServiceTest` 6, `DeathTriggerCollectorServiceTest` 6, `SpellCastTriggerCollectorServiceTest` 3,
  `LandTapTriggerCollectorServiceTest` 2, the known one each in `SimulationCopyCompletenessTest`,
  `CardImmutabilityArchTest` and `CardEffectTargetingConsistencyTest`, and `CounterSpellEffectHandlerTest` 2 (the
  Mockito wiring step 13 run 6 listed as failing at HEAD; the handler reads nothing this run changed; not rerun at HEAD
  here).
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 11: library, draw and discard

Step 13 family 7, every wrapper kind in one run: every library, draw and discard ability that the `LibraryWatch`
collectors read is authored without trigger-only wrappers, but the abilities kept below. 73 card classes were
converted by script; Hostile Investigator's and Planetarium of Wan Shi Tong's split halves were merged into one
ability each, and Lady Octopus's "first or second card" is one ability with two conditions.

**Authoring.**

- `LibraryTrigger` has two new components, both dropped from `watchKey()`:
  - `qualifier`, the discarded card's restriction: `LibraryTrigger.withFilter(CardPredicate)`, as in
    `Triggers.youDiscardCard().withFilter(new CardTypePredicate(CardType.CREATURE))` (Bone Miser, Waste Not, Surly
    Badgersaur, Urza, Mishra, Doctor Doom's "one or more land cards", Anje Falkenrath's "if it has madness", …). Only
    the discard events take one; a second filter is rejected.
  - `nth`, the player's Nth such event this turn, counting the events before the source was on the battlefield:
    `youDrawSecondCardEachTurn()` is `youDrawCard().nthTimeEachTurn(2)`, with new factories
    `youDrawNthCardEachTurn(n)` and `opponentDrawsNthCardEachTurn(n)` (Emerald Collector's third card, Alandra's
    fifth, Zurzoth's "first card each turn"), and `.firstTimeEachTurn()` for "for the first time each turn" (Valiant
    Rescuer's cycling, Veronica's nonland discard, Rielle's discard event, Whispering Snitch's surveil). "First or
    second card" is `List.of(youDrawNthCardEachTurn(1), youDrawSecondCardEachTurn())`. A count is a property of the
    event, not a limit: it is not `ONCE_EACH_TURN`, which counts the ability's own triggers.
- `LibraryEvent.DRAWS_SECOND_CARD` is gone (it is `DRAWS` with a count). New events, each read by another event's
  collector (`LibraryTrigger.watchedEvent()`): `DRAWS_EXCEPT_FIRST_IN_DRAW_STEP`
  (`opponentDrawsCardExceptFirstInDrawStep()`: Orcish Bowmasters, Xyris, Leela), `DRAWS_FIRST_IN_DRAW_STEP`
  (`youDrawFirstCardInDrawStep()`: Wiretapping) and `DISCARDS_BY_OPPONENT` (`opponentCausesYouToDiscardCard()`:
  Spiritual Focus), and `CYCLES` with `YOU` (`youCycleCard()`, "whenever you cycle [another] card": 12 cards), read
  by the "you discard a card" collector.
- An intervening "if" is `TriggerOptions.DEFAULT.withInterveningIf(c)` (**CR 603.4**, checked), as are the Class levels
  (Wizard Class, Cool but Rude) and max speed (Aether Syphon), as earlier runs made them. "This ability triggers only
  once each turn" is `ONCE_EACH_TURN`, "do this only once each turn" `DO_ONLY_ONCE_EACH_TURN`, "this ability triggers
  only once" `ONCE`.

**The engine.**

- `TriggeredAbility.legacyEffects()` gives an ability any of whose library conditions is read by another trigger
  (`legacyLibraryEffects()`) its wrappers in the order the cards nested them, outermost first: `OnceOnlyTriggerEffect`,
  the filter as `TriggeringCardConditionalEffect`, the limit (`OncePerTurnTriggerEffect` keyed by the ability, or
  marked on acceptance), the counting `OncePerTurnTriggerEffect` of "first time each turn" (`firstCycling`,
  `firstNonlandDiscard`, `firstDiscardEvent`, `firstSurveil`), the event's wrapper (`CyclingTriggerEffect`,
  `OpponentCausedDiscardTriggerEffect`, `NthCardDrawTriggerEffect`, `FirstOrSecondCardDrawTriggerEffect` for counts
  1 and 2, `ExceptFirstDrawStepTriggerEffect`, `FirstDrawStepDrawOnlyTriggerEffect`), then the intervening "if".
  `ONCE` is outermost here (Emerald Collector wrote it so), where other families put it innermost. The ability's
  conditions must all be library triggers of one event, player and filter (only a draw's count may differ, as 1 and
  2); a filter off a discard, a count no collector reads ("first" on a scry, an opponent's or a filtered one but
  nonland), `ONE_OR_MORE`-style multiplicities, a limit next to a "first time" count and
  `ONCE_EACH_TURN_FOR_EACH_CREATURE` throw.
- `LibraryWatch.effectsOf` and `registrationsOf` read the legacy view. `ON_CONTROLLER_DRAWS_SECOND_CARD` is deleted:
  every count is read by the `youDrawCard()` / `opponentDrawsCard()` collectors through the `NthCardDrawTriggerEffect`
  they already unwrapped. `GRAVEYARD_ON_CONTROLLER_DRAWS_SECOND_CARD` and `GRAVEYARD_ON_OPPONENT_DRAWS_SECOND_CARD`
  became `GRAVEYARD_ON_CONTROLLER_DRAWS` (`youDrawCard()`, graveyard) and `GRAVEYARD_ON_OPPONENT_DRAWS`
  (`opponentDrawsCard()`, graveyard): `DrawService` runs both graveyard collectors on every draw now and unwraps the
  count like the battlefield collectors (Wolfbat, Dogged Detective).
- By hand: the "you discard a card" collector marked the unkeyed once-each-turn mark whatever the wrapper's key, so
  with the keyed limit of the legacy view it never stopped triggering (Urza, Powerstone Prodigy; Mishra, Excavation
  Prodigy); it marks through `OncePerTurnTriggerSupport.markIfNeeded` now. The graveyard surveil reader reads
  `getEffectiveGraveyardLegacyTriggerEffects` (Blood Operative's intervening "if"). The other collectors of the
  converted limits (draws, opponent discards, discard events, scry, surveil) already honoured the key.
- No wrapper class is deleted: the legacy view still builds them. Run 16 deletes what is left.

**Cards.**

- `scripts/step14-library-scan.py` (run 10's scan on the library factories) lists the family's wrapped abilities.
- `scripts/step14-library-options.py` (`--dry-run`, `--report`) did the conversion: it peels each family ability's
  wrappers, outermost first, into the trigger, filter, count and options, re-indents what is left and stops on a
  wrapper it can't place. Only a two-argument `new ConditionalEffect(c, e)` directly under the trigger is taken as the
  intervening "if"; `unless`, `atTriggerTime` and conditionals inside other effects stay. Its `MANUAL` table holds the
  two merges. Running it again changes nothing.
- By hand: Crystalline Resonance's `BecomeCopyOfTargetPermanentUntilYourNextTurnEffect` retains `youCycleCard()` (the
  script converted only the trigger argument; the handler reads and re-adds the ability by its exact trigger).
- `scripts/step14-library-docs.py` rewrote 39 agent-docs passages (exact replacements): the converted rows, and
  "don't author" notes on the entries of `CyclingTriggerEffect`, `OpponentCausedDiscardTriggerEffect`,
  `FirstOrSecondCardDrawTriggerEffect` (with `NthCardDrawTriggerEffect`), `ExceptFirstDrawStepTriggerEffect`,
  `FirstDrawStepDrawOnlyTriggerEffect` and the four counting `OncePerTurnTriggerEffect` factories. Those classes'
  Javadoc, `ARCHITECTURE.md` and the implement-card and review-card skills were edited by hand.

**Kept, with their reason.**

- `ConditionalEffect.atTriggerTime`: Wedding Ring ("an opponent who controls a Wedding Ring draws a card during their
  turn", a condition of the event) and Entropic Battlecruiser (its station 1+ ability, which exists only while the
  threshold is met, so it isn't checked again on resolution). Run 16, with the `triggerTimeOnly` flag.
- Effect-text conditions, which stay effects: Yidaro's two `ConditionalEffect.unless`, Wiretapping's "then if you have
  nine or more cards in hand".
- The first-draw reveal effects (`RevealFirstDraw…Effect`, `RevealEachDrawEffect`, through `FirstDrawRevealTriggerEffect`
  / `DrawRevealTriggerEffect`): the reveal is a choice made as the card is drawn and the effect reads the drawn card;
  step 15, or a reveal event.
- "A player draws / discards" is still two abilities, "you" and "an opponent" (Sea of Sand, Phyrexian Tyranny, Spirit
  Cairn, Telekinetic Bonds, Ian Malcolm, King T'Challa, Krang): there is no any-player draw or discard watch.
- Event-reading effects (step 15): `LoseLifeUnlessPaysEffect.forTriggeringPlayer`, `CreateTokenCopyOfCardEffect
  .fromTriggeringCard` (Hashaton), the `EventValue` discard counts, `DamageRecipient.TRIGGERING_PLAYER`.

**Behaviour changes.**

- The "your second card each turn" abilities are collected in the `youDrawCard()` pass, in their permanent's ability
  order, where the old second-card pass ran after every permanent's "whenever you draw a card" abilities. This only
  changes the engine's fixed order of simultaneous triggers one player controls, which that player chooses
  (**CR 603.3b**, checked).
- The once-each-turn limit of Urza, Mishra, Tataru Taru, Nimrodel Watcher, Diviner of Fates, Dying to Serve and Hostile
  Investigator is keyed by the ability, where the unkeyed wrapper shared the source's mark. None of them has another
  unkeyed once-each-turn ability. Hostile Investigator's and Planetarium's merged halves share their one limit, as they
  shared the mark before.
- Nothing else changed: the legacy view hands every collector the wrappers it read before.

**For later runs.**

- The `atTriggerTime` conditions above (run 16).
- Pure Intentions' inverted temporary `opponentDiscardsCard()`, Psychogenic Probe's unchecked cause and
  `EnterBattlefieldOnDiscardEffect` in a trigger, as step 13 run 7 listed.
- `SequenceEffect.controllerDrawCount` and `PutCountersOnSourceEffect.controllerDrawCount`
  (`CardEffect.triggersOnControllerDrawCount`) are a draw count inside an effect that no card sets; the count on the
  trigger replaces them, and the step that deletes the legacy paths can drop them.
- Most library collectors check an intervening "if" only as the ability resolves, or as it triggers only (the draw
  collectors and the self-cycle and self-discard readers check it as it triggers and again on resolution); the legacy
  view rebuilds the same wrappers, so nothing changed. A run like 4c's would make them uniform.
- Step 15: as step 13 run 7 listed.

**Tests changed.**

- `LibraryTriggerTest`, eight new tests: a filter and a count are dropped from the watch key, and a second filter, a
  second count or a count below one rejected; the cycle, opponent-caused and draw-step events are watched by the
  collectors that read them; the legacy view gives the filter outside the keyed limit and the intervening "if" inside
  the count, with a stable identity; "this ability triggers only once" outside the count; the four "first time each
  turn" wrappers; the event wrappers and "first or second" from two conditions; an unreadable count, filter, pair of
  counts, multiplicity or limit next to a count is rejected; granted abilities are read in the legacy form.
- `LibraryWatchTest`: the second-card case reads the abilities through `ON_CONTROLLER_DRAWS` and
  `GRAVEYARD_ON_CONTROLLER_DRAWS` with their `NthCardDrawTriggerEffect` (the second-card watch is gone).
- No card test changed.

**Test runs.**

- Card: the test class of every card that uses a library factory (converted or not) and every card test that
  constructs one (698 classes, 5,914 tests). The first run had 3 failures, all in `CrystallineResonanceTest` (the
  retained trigger fixed by hand above); its rerun passed.
- Application: the `service.trigger` and `model.trigger` packages and the draw, freeze, triggered-ability,
  card-ability, granted-ability, deep-copy, simulation-copy, architecture, targeting-consistency, shuffle-hand and
  grant-trigger classes (743 tests): 24 failures, all known from HEAD: `DamageTriggerCollectorServiceTest` 6,
  `DeathTriggerCollectorServiceTest` 6, `SpellCastTriggerCollectorServiceTest` 3, `LandTapTriggerCollectorServiceTest` 2,
  `ShuffleHandIntoLibraryAndDrawEffectHandlerTest` 3 and `DrawServiceTest`'s additional-trigger copy (step 13 run 7's
  HEAD failures), and the known one each in `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and
  `CardEffectTargetingConsistencyTest`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 12: tap and untap

Step 13 family 8, every wrapper kind in one run: every tap and untap ability that the `TapWatch` collectors read is
authored without trigger-only wrappers, but the abilities kept below. 89 card classes were converted by script (88 by
the peeling pass, Mine Layer by its `MANUAL` entry); five cards' "a [quality] permanent becomes tapped" pairs were
merged into one ability each.

**Authoring.**

- `TapTrigger` has a new `qualifier` component, the tapped or untapped permanent's restriction:
  `TapTrigger.withFilter(PermanentPredicate)`, as in `Triggers.permanentYouControlBecomesTapped().withFilter(merfolk)`
  (Judge of Currents, Magda, Deeproot Pilgrimage), `permanentOpponentControlsBecomesTapped().withFilter(…)` (Lifetap,
  Thoughtleech, Verity Circle, Roots of Life, …), `youTapPermanentOpponentControls().withFilter(creature)` (Solitary
  Sanctuary, Icewrought Sentry, Hylda, Sharae) and `permanentBecomesUntapped().withFilter(…)` (Fishing Pole). A second
  filter is rejected, and a filter on a trigger whose collector doesn't judge one (the mana, enchanted, creature-tap,
  teamwork and untap-step triggers) throws in the legacy view.
- "Whenever this permanent becomes tapped" is the new `Triggers.becomesTapped()` (`BECOMES_TAPPED` with `THIS`), where
  the cards wrote `permanentYouControlBecomesTapped()` with a `PermanentIsSourceCardPredicate` filter: 62 card classes
  (the grants of Haunted One, Legolas's Quick Reflexes and Hangar Scrounger among them) and the perpetual land grant
  (`PerpetuallyGrantTapDrawToRandomLandInLibraryEffectHandler`, by hand). "While it has a -1/-1 counter" / "if it's enchanted or equipped" are
  its filter (Encumbered Reejerey, Warchanter Skald).
- "For the first time during your turn" is a new event, `TapEvent.BECOMES_TAPPED_FOR_FIRST_TIME_DURING_YOUR_TURN` (a
  creature, counting its taps of the turn before the source arrived): `becomesTappedForFirstTimeDuringYourTurn()` (Tale
  of Katara and Toph's grant) and `creatureYouControlBecomesTappedForFirstTimeDuringYourTurn()` (Captain America,
  Living Legend). It is not a limit: it counts the creature's taps, not the ability's triggers.
- "One or more … become tapped" is `TriggerOptions.ONE_OR_MORE` (Deeproot Pilgrimage, Rewrite History, Sharae), only on
  the "you control / an opponent controls becomes tapped" and "you tap" triggers whose collectors batch. "This ability
  triggers only once each turn" is `ONCE_EACH_TURN` (Interface Ace, Nanoform Sentinel, Sharae), an intervening "if"
  (**CR 603.4**, checked) `withInterveningIf` (Barbflare Gremlin, Interface Ace's "during your turn", Encumbered
  Reejerey, Warchanter Skald).
- "Whenever a [quality] permanent becomes tapped", which the cards wrote as a "you control" and an "opponent controls"
  ability sharing one effect, is one ability with both conditions, each filtered: Royal Decree, Temporal Distortion,
  Haunting Wind, Hawkeye's Bow, Mine Layer. An event matches exactly one of the two, so it triggers as the two did.

**The engine.**

- `TapTrigger.watchKey()` drops the qualifier, and gives the `narrowed()` triggers ("this permanent becomes tapped" and
  the first-time event) the "a permanent you control becomes tapped" trigger, whose collector reads them.
- `TriggeredAbility.legacyEffects()` gains a tap view (`tapView()`): outermost first, the filter as a
  `TriggeringPermanentConditionalEffect` ("this permanent", the narrowed trigger's type and the qualifier, joined in a
  `PermanentAllOfPredicate` when there are several), the global `OncePerTurnPerCreatureTriggerEffect` for the
  first-time event, then the generic limit and intervening "if"; "one or more" is the registrations'
  `TriggerMode.ONCE_PER_BATCH`. Conditions with different filters or events, a multiplicity other than `EACH` /
  `ONE_OR_MORE`, "one or more" off the batching collectors, a limit next to the first-time event and an authored
  trigger mode next to `ONE_OR_MORE` throw.
- `TapWatch.effectsOf` and `registrationsOf` read the legacy view. By hand: the "a permanent you control becomes tapped"
  and "you tap a permanent an opponent controls" collectors read the temporary and persistent grants through
  `getLegacyTemporaryTriggerEffects` / `getLegacyPersistentTriggerEffects` (Hangar Scrounger's backup grant); the ally,
  opponent and you-tap collectors marked the unkeyed once-each-turn mark whatever the wrapper's key, so the keyed
  limit of the legacy view never stopped them (Interface Ace, Nanoform Sentinel, Sharae): they mark through
  `OncePerTurnTriggerSupport.markIfNeeded` now.
- No wrapper class is deleted: the legacy view still builds them, and other families still author some. Run 16 deletes
  what is left.

**Cards.**

- `scripts/step14-tap-scan.py` (run 10's scan on the tap factories, the new ones included) lists the family's wrapped
  abilities.
- `scripts/step14-tap-options.py` (`--dry-run`, `--report`) did the conversion: it peels each family ability's wrappers,
  outermost first, into the trigger, filter and options, re-indents what is left and stops on a wrapper it can't
  place. Only a two-argument `new ConditionalEffect(c, e)` directly under the trigger and its filter, on a card of its
  `INTERVENING` table, is taken as the intervening "if"; any other `ConditionalEffect` stays an effect (Fishing Pole's
  "if you do", Saint Traft's resolution counts). Its `MANUAL` table holds the five merges and Hangar Scrounger's shared
  effect; its `COMMENTS` table two card comments that named the removed wrappers (City of Brass, Encumbered Reejerey).
  Running it again changes nothing. The scan's variable check misses an effect held in a constant: Mine Layer's was
  found by hand and added to `MANUAL`.
- `scripts/step14-tap-docs.py` rewrote 33 agent-docs passages (exact replacements), among them the stale Don't Move row
  that still showed two controller-relative registrations. The Javadoc of `TriggeringPermanentConditionalEffect` and
  `OncePerTurnPerCreatureTriggerEffect`, `ARCHITECTURE.md` and the implement-card and review-card skills were edited by
  hand.

**Kept, with their reason.**

- Well Rested: `OncePerTurnTriggerEffect.perGrant` gives each granting Aura its own limit on the creature, which
  `ONCE_EACH_TURN` (keyed by the ability, and two Well Rested grant equal abilities) can't say. Folk Hero and Tyvar
  use it in other families.
- The Ninth Doctor: `ConditionalEffect.atTriggerTime(new DuringYourUntapStep(), …)`, a condition of the event (run 16,
  with the `triggerTimeOnly` flag, or an untap-step untap event).
- Freyalise's Winds: "whenever a permanent becomes tapped" is still two abilities without a filter (it had no wrapper,
  so the scan didn't list it); merging it is a later cleanup. Champions of the Shoal's "enters or becomes tapped" is
  still an enters ability and a tap ability.
- Disciple of Deceit only names a predicate in a local; nothing to convert.

**Behaviour changes.**

- The once-each-turn limit of Interface Ace, Nanoform Sentinel and Sharae of Numbing Depths is keyed by the ability,
  where the unkeyed wrapper shared the source's mark. None of them has another unkeyed once-each-turn ability.
- Royal Decree, Temporal Distortion, Haunting Wind, Hawkeye's Bow and Mine Layer have one tap ability where they had
  two; each tap triggers one of its conditions, as it triggered one of the two abilities.
- Nothing else changed: the legacy view hands every collector the wrappers it read before. No may question moved.

**For later runs.**

- Interface Ace's "during your turn" is an intervening "if" (as the card wrote it), a condition of the event really;
  a "during your turn" event qualifier would say it directly.
- The first-time event only counts creatures (the collector's `creatureTapCountsThisTurn`), and is read only by the
  "you control" collector.
- Step 15: as step 13 run 8 listed.

**Tests changed.**

- `TapTriggerTest`, nine new tests: a filter is dropped from the watch key and a second one rejected; "this permanent"
  and the first-time event are watched by "a permanent you control becomes tapped"; the legacy view gives "this
  permanent" outside the keyed limit and the intervening "if" inside it, with a stable identity; it joins "this
  permanent" and a filter; it gives the first-time wrapper inside the filter; "one or more" is `ONCE_PER_BATCH`; one
  ability with a "you control" and an "opponent controls" condition is read by both watches; what the collectors can't
  read is rejected; granted abilities are read in the legacy form. The factory list covers the three new factories.
- No card test changed.

**Test runs.**

- Card: the test class of every card that uses a tap factory (converted or not), Ambassador of Evendo (the perpetual
  land grant) and every card test that constructs one of them (336 classes, 3,029 tests): all passing. Mine Layer was
  converted after that run; its test class passed on its own (9 tests).
- Application: the `service.trigger` and `model.trigger` packages and the freeze, triggered-ability, card-ability,
  granted-ability, simulation-copy, architecture, targeting-consistency, untap-step, legacy-slot, mana-ability trigger
  deferral and permanent-removal classes (835 tests): 78 failures, all known from HEAD: `DamageTriggerCollectorServiceTest`
  6, `DeathTriggerCollectorServiceTest` 6, `SpellCastTriggerCollectorServiceTest` 3, `LandTapTriggerCollectorServiceTest`
  2, `UntapStepServiceTest` 1, the known one each in `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and
  `CardEffectTargetingConsistencyTest`, and `PermanentRemovalServiceTest` 46 and `ManaAbilityTriggerDeferralTest` 11:
  the same 57 cases fail at HEAD (`e73987ca4c`, run in a worktree with the main tree's `card-data-cache`).
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 13: life

Step 13 family 9, every wrapper kind in one run: every life gain, loss and payment ability that the `LifeWatch`
collectors read is authored without trigger-only wrappers, but Wedding Ring's (kept below). 20 card classes were
converted by script and Shadowheart's specialized ability by hand; Wax-Wane Witness's, Moonstone Harbinger's, The Master
of Lake-town's and MACH-1's split halves were merged into one ability each.

**Authoring.**

- `LifeTrigger` has a new `firstEachTurn` component, dropped from `watchKey()`: `youGainLife().firstTimeEachTurn()`
  and `youLoseLife().firstTimeEachTurn()` for "for the first time each turn" (Attended Healer, Cleric of Life's Bond,
  Vanguard Seraph, Leech Collector, Gourmand's Talent, Cat Collector, Deathless Knight from the graveyard, Gonti's
  Machinations, Vengeful Warchief, Intermediate Chirography). As run 11's count, it counts the player's events of the
  turn, those before the source arrived included; it is not a limit.
- New event `LifeEvent.LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN`, `Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn()`
  (Valgavoth), read by the "an opponent loses life" collector; it can't also take `firstTimeEachTurn()`.
- "During your turn" (Vampire Scrivener, Kefka, Cat Collector's "each of your turns", Wax-Wane Witness, Moonstone
  Harbinger, Shadowheart's specialized ability) and the Class levels (Gourmand's Talent, Cleric Class, Intermediate
  Chirography) are intervening "if"s (**CR 603.4**, checked), as earlier runs made them; "this ability triggers only once
  each turn" is `ONCE_EACH_TURN` (Moonstone Harbinger, MACH-1), "do this only once each turn" (**CR 603.2h**, checked)
  `DO_ONLY_ONCE_EACH_TURN` (Nykthos Paragon, Emet-Selch).
- "Whenever you gain or lose life" (Wax-Wane Witness, Moonstone Harbinger) and "whenever a player loses life" (The
  Master of Lake-town) are one ability with both conditions; MACH-1's "when this enters and whenever you gain life" is
  one ability with an enters and a life condition, so its once-each-turn limit covers both, as run 4a left it to this
  run.
- `ShadowheartSharranCleric.specializedLifeLossTrigger` is `specializedLifeLossAbility`, which returns the whole
  `TriggeredAbility` (the intervening "if" in its options); `SpecializeShadowheartEffectHandler` adds it with
  `addTriggeredAbility`.

**The engine.**

- `TriggeredAbility.legacyEffects()` gains a life view (`lifeView()`): "for the first time each turn" is the counting
  `OncePerTurnTriggerEffect` where the limits go (outside the intervening "if"): a gain's keyed by `limitKey()`, so two
  such abilities of one source (Gourmand's Talent's levels 2 and 3, which wrote their own keys) don't share a mark, a
  loss's `firstLifeLoss`, which marks nothing. "An opponent loses life for the first time during each of their turns" is
  one `FirstOpponentLifeLossEachTurnTriggerEffect` of every effect (`legacyFirstOpponentLifeLossEffects()`), the only
  condition, without options. A count on another player or on a payment, a limit next to a count, conditions that count
  and don't, `ONCE`, `ONCE_EACH_TURN_FOR_EACH_CREATURE` and a multiplicity other than `EACH` throw.
- `LifeWatch.effectsOf` and `registrationsOf` read the legacy view; the two graveyard life-gain collectors read
  `getEffectiveGraveyardLegacyTriggerEffects`. Granted abilities were already read in the legacy form.
- By hand: the "you lose life" and "you pay life" collectors marked the unkeyed once-each-turn mark whatever the
  wrapper's key, so a keyed limit never stopped them; they check and mark through `OncePerTurnTriggerSupport` now
  (Moonstone Harbinger's merged ability shares its limit between the gain and the loss collector that way). The other
  life collectors already did, or read no limit. `CastTargetInstantOrSorceryFromGraveyardHandler`, which marks
  Emet-Selch's "do this only once each turn" when the cast is accepted, looked for the `markOnAcceptance` wrapper among
  the card's raw triggered effects; it reads the abilities' legacy view now (the card test caught it).
- No wrapper class is deleted: the legacy view still builds them. Run 16 deletes what is left.

**Cards.**

- `scripts/step14-life-scan.py` (run 12's scan on the life factories) lists the family's wrapped abilities.
- `scripts/step14-life-options.py` (`--dry-run`, `--report`) did the conversion: it peels each family ability's
  wrappers, outermost first, into the trigger's count and the options, re-indents what is left and stops on a wrapper
  it can't place. Only a two-argument `new ConditionalEffect(c, e)` (or Cleric Class's `atTriggerTime`) directly under
  the trigger, on a card of its `INTERVENING` table, is the intervening "if"; the conditionals inside effects (The
  Destined White Mage's full party, Prize Pig's and Marina Vendrell's Grimoire's "then if", Amalia's `unless`) stay.
  Its `MANUAL` table holds the four merges, Valgavoth and Deathless Knight's graveyard condition. Running it again
  changes nothing.
- `scripts/step14-life-docs.py` rewrote 12 agent-docs passages (exact replacements): the Nykthos Paragon, Valgavoth
  and Deathless Knight rows, The Master of Lake-town's pattern and `MillOpponentOnLifeLossEffect`'s entry, and "don't
  author" notes on `FirstOpponentLifeLossEachTurnTriggerEffect` and `firstLifeGain` / `firstLifeLoss`. Those classes'
  Javadoc, `ARCHITECTURE.md` and the implement-card and review-card skills were edited by hand.

**Kept, with their reason.**

- Wedding Ring: `ConditionalEffect.atTriggerTime` ("an opponent who controls a Wedding Ring gains life during their
  turn"), a condition of the event, as its draw half (run 11). Run 16, with the `triggerTimeOnly` flag.
- Ob Nixilis, Captive Kingpin's "one or more opponents each lose exactly 1 life" is no wrapper: the opponent-loses-life
  collector's `SequenceEffect` handler checks the amount for any `SequenceEffect`. A trigger amount filter would say it,
  but the handler judges it by effect class; it goes with the collectors (step 15 or 16).
- Emet-Selch's "one or more opponents lose life" still triggers for each loss (its "do this only once each turn" caps
  it); there is no batched life-loss event.

**Behaviour changes.**

- "Whenever you gain life for the first time each turn" is keyed by the ability, where the unkeyed wrapper marked the
  source's shared mark (Attended Healer, Cleric of Life's Bond, Vanguard Seraph, Leech Collector, Cat Collector);
  Gourmand's Talent's own keys are replaced by the abilities'. The count already let only the turn's first gain event
  through, so only another unkeyed once-each-turn ability of the same source could notice, and none of them has one.
- Moonstone Harbinger's once-each-turn limit is keyed by its ability, shared by its gain and loss conditions as the
  two abilities shared the unkeyed mark; MACH-1's likewise between its enters and life-gain conditions.
- Wax-Wane Witness, Moonstone Harbinger, The Master of Lake-town and MACH-1 have one ability where they had two; each
  event triggers one of its conditions, as it triggered one of the two abilities.
- Cleric Class's level-2 condition was `atTriggerTime`; it is an intervening "if" now, but the "you gain life"
  collector strips every top-level `ConditionalEffect` as the ability triggers (`unwrapLifeChangeConditional`), so it is
  still checked only then, as before.
- Nothing else changed: the legacy view hands every collector the wrappers it read before. No may question moved.

**For later runs.**

- The "you gain life" and "you lose life" collectors check an intervening "if" only as the ability triggers
  (`unwrapLifeChangeConditional` unwraps it), and the opponent collectors' `ConditionalEffect` handlers likewise; CR
  603.4 wants it checked again on resolution. A run like 4c's would make them check it then (it changes behaviour, so
  it is a rules fix with its own tests).
- Ob Nixilis's amount condition and Emet-Selch's "one or more opponents", above.
- Step 15: as step 13 run 9 listed.

**Tests changed.**

- `LifeTriggerTest`, seven new tests: "for the first time each turn" is dropped from the watch key and can't be added
  twice; the opponent's first loss during their turn is watched by "an opponent loses life"; the legacy view gives the
  count outside the intervening "if", keyed for a gain, with a stable identity; a graveyard count is read in the
  graveyard; one ability with a gain and a loss condition shares its keyed limit; the first-loss-during-their-turn
  record holds every effect; what no life collector reads is rejected. The factory list covers the new factory.
- No card test changed.

**Test runs.**

- Card: the test class of every card that uses a life factory (converted or not), Shadowheart's, and every card test
  that constructs one of them (162 classes, 1,368 tests). The first run had 1 failure,
  `EmetSelchOfTheThirdSeatTest#successfulCastUsesOncePerTurnOpportunity` (the acceptance mark fixed above); its rerun
  passed (7 tests).
- Application: the `service.trigger` and `model.trigger` packages and the freeze, triggered-ability, card-ability,
  granted-ability, simulation-copy, architecture and targeting-consistency classes (736 tests): 20 failures, all known
  from HEAD (runs 11 and 12): `DamageTriggerCollectorServiceTest` 6, `DeathTriggerCollectorServiceTest` 6,
  `SpellCastTriggerCollectorServiceTest` 3, `LandTapTriggerCollectorServiceTest` 2, and the known one each in
  `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and `CardEffectTargetingConsistencyTest`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 14: sagas

Step 13 family 10, every wrapper kind in one run: every Saga ability (the chapter abilities and the `SagaWatch`
final-chapter abilities) is authored without trigger-only wrappers. The family had one: Tom Bombadil's "this ability
triggers only once each turn", converted by script (1 card class). Family 11 (everything else, run 15) was next.

**Authoring.**

- "This ability triggers only once each turn" on a final-chapter ability is `TriggerLimit.ONCE_EACH_TURN`:
  `addTrigger(Triggers.finalChapterAbilityResolves(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
  …)` (Tom Bombadil).
- **A chapter ability takes no options.** "{rN}—[Effect]" is "when one or more lore counters are put onto this Saga,
  if the number of lore counters on it was less than N and became at least N, [effect]" (**CR 714.2b**, checked): the
  threshold is the chapter's only intervening "if", and an "if" in its text doesn't immediately follow a trigger
  condition, so it has its normal meaning (**CR 603.4**, checked) and is checked on resolution. The family's 11
  chapter `ConditionalEffect`s (The First Iroan Games, Summon: Fenrir, Leaves from the Vine, Ifrit's "if this Saga has
  three or more lore counters", Nightmares and Daydreams, Heaven Sent's and Inventive Iteration's `unless` pairs, The
  Misty Mountains Cold's "then if") stay effects. The `TriggeredAbility` constructor now rejects a chapter ability
  with options other than `DEFAULT`, since the chapter readers read the plain effects and would drop them.

**The engine.**

- `SagaWatch.effectsOf` and `registrationsOf` read the legacy view, as `LifeWatch` does: the generic limit wrapper,
  `OncePerTurnTriggerEffect` keyed by `limitKey()`, which `dispatchSlotEffect` unwraps and marks through
  `OncePerTurnTriggerSupport` as it did the unkeyed wrapper. Granted abilities were already read in the legacy form.
- `TriggeredAbility.legacyEffects()` rejects, for an ability with a `FinalChapterTrigger`
  (`requireFinalChapterOptions()`), the options its collector doesn't read: an intervening "if" (`SagaTriggerCollectorService`
  puts the effect on the stack as it is, so it would be checked only on resolution), "do this only once each turn"
  (`dispatchWatch` marks nothing on acceptance), `ONCE` and a multiplicity other than `EACH`.
- The chapter readers (`SagaChapterService`, `StepTriggerService`, the `PermanentCounterSupport` fallback,
  `initializeReturnedSaga`) are unchanged: they read the chapter's plain effects, which hold no trigger-only wrapper.
- No wrapper class is deleted: the legacy view still builds `OncePerTurnTriggerEffect`, and other families author it.

**Cards.**

- `scripts/step14-saga-scan.py` (run 13's scan on `Triggers.chapter`, `finalChapterAbilityTriggers` and
  `finalChapterAbilityResolves`) lists the family's wrapped abilities: 25, of which one limit; the rest are the chapter
  conditionals above and the abilities chapters create, kept below. A check of the effects passed in locals and of
  whole Saga files for the other wrapper kinds (`OnceOnlyTriggerEffect`, `TriggerMode`, `atTriggerTime`, the batched
  wrappers, `interveningIf`) found nothing else.
- `scripts/step14-saga-options.py` (`--dry-run`, `--report`) did the conversion with run 13's peeling, re-indenting
  and import code: every chapter `ConditionalEffect` stays an effect, and a chapter ability with a trigger-only wrapper
  stops it. Its own `placed` re-indents an effect that started inside a line (under the peeled wrapper) from that
  line's indent; run 13's cut Tom Bombadil's continuation line to the margin. Running it again changes nothing.
- `scripts/step14-saga-docs.py` rewrote 5 agent-docs passages (exact replacements): the Saga authoring paragraph of
  `CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md`, the final-chapter row of `ACTIVATED_ABILITY_GUIDE.md` and the
  `OncePerTurnTriggerEffect` entries of `EFFECTS_INDEX.md` and `EFFECTS_QUICK_REFERENCE.md`. `ARCHITECTURE.md` and the
  implement-card and review-card skills were edited by hand.

**Kept, with their reason.**

- The abilities chapters create are their own families' records, not Saga abilities:
  `RegisterDelayedControllerSpellCastTriggerEffect` (Showdown of the Skalds, Summon: Fenrir, Summon: Good King Mog XII,
  Rediscover the Way, Origin of Thor, Nightmares and Daydreams, Kumano Faces Kakkazan, Battle of Frost and Fire, Summon:
  Brynhildr) and the combat abilities `RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect` registers (The Last Ronin,
  The Revelations of Ezio, Harald Unites the Elves; run 7 kept their `TriggeringPermanentControllerConditionalEffect`).
- `ChosenNameSpellCastTriggerEffect` (Medomai's Prophecy) is a chapter effect (run 6c). `ChooseOneAtTriggerTimeEffect`
  (Life of Toshiro Umezawa) and `ChooseOneAtRandomEffect` (Summon: Magus Sisters) are modal effects, not trigger
  options.

**Behaviour changes.**

- Tom Bombadil's once-each-turn limit is keyed by its ability, where the unkeyed wrapper marked the source's shared
  mark. It has no other once-each-turn ability, so nothing can notice.
- Nothing else changed. No may question moved.

**For later runs.**

- Run 10's Saga items are unchanged (the duplicated chapter logic, chapter I of a returned Saga without targeting, the
  final chapter recognised by the stack entry's description, proliferate not triggering chapters, …), and Copy
  Enchantment's copy of a Saga still gets no lore counter.
- Step 15: the final-chapter watchers' event value (the Saga's mana value) and the Saga-specific chapter target
  records, as run 10 listed.

**Tests changed.**

- `SagaTriggerTest`, three new tests: a chapter ability with options is rejected (through `Card.addTrigger` and the
  `TriggeredAbility` constructor); the legacy view gives a final-chapter limit as `OncePerTurnTriggerEffect` keyed by
  the ability, the plain view the effect; the legacy view rejects an intervening "if", `DO_ONLY_ONCE_EACH_TURN`,
  `ONCE` and `ONE_OR_MORE` on a final-chapter ability.
- `SagaWatchTest`, one new test: the watch hands the collector the keyed wrapper, the same object from `effectsOf` and
  `registrationsOf`.
- No card test changed. `TomBombadilTest#putsASagaOntoTheBattlefieldAfterOnlyOneFinalChapterResolvesEachTurn` covers
  the limit through the engine.

**Test runs.**

- Card: the test classes of the three final-chapter cards (Tom Bombadil; Narci, Fable Singer; Historian's Boon), of
  every card the scan listed and of every card test that constructs one of the final-chapter cards; Ifrit through
  Clive, Ifrit's Dominant (20 classes, 202 tests): all passing.
- Application: the `service.trigger` and `model.trigger` packages and the freeze, triggered-ability, card-ability,
  granted-ability, simulation-copy, architecture and targeting-consistency classes (731 tests): 20 failures, the ones
  known from HEAD (runs 11 to 13): `DamageTriggerCollectorServiceTest` 6, `DeathTriggerCollectorServiceTest` 6,
  `SpellCastTriggerCollectorServiceTest` 3, `LandTapTriggerCollectorServiceTest` 2, and the known one each in
  `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and `CardEffectTargetingConsistencyTest`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 15: everything else (family 11)

Step 13 family 11, every wrapper kind in one run: every player and keyword action, control change, attach, crew,
saddle and station, day and night, Room, Planechase and state-trigger ability is authored without trigger-only
wrappers, but the ones kept below. 112 card classes were converted by script (11 of them by its `MANUAL` table).
Run 16 is the last run of step 14.

**Authoring.**

- "This ability triggers only once each turn" is `TriggerLimit.ONCE_EACH_TURN`: the 13 crime cards (Magda the
  Hoardmaster, Raven of Fell Omens, Gisa the Hellraiser, …), Curator of Sun's Creation (discover) and Inchblade
  Companion (Equipment attach).
- Intervening "if"s (**CR 603.4**, checked) are options: Knights of the Black Rose ("if you were the monarch as the turn
  began"), Overzealous Muscle ("during your turn", as earlier runs made it), Barbarian Class's level.
- "Becomes crewed / saddled for the first time each turn" is its own event, as step 13 kept "investigates for the first
  time each turn": new `ObjectAction`s `BECOMES_CREWED_FOR_FIRST_TIME_EACH_TURN` / `BECOMES_SADDLED_FOR_FIRST_TIME_EACH_TURN`,
  `Triggers.becomesCrewedForFirstTimeEachTurn()` (Mindlink Mech), `becomesSaddledForFirstTimeEachTurn()` (Stubborn
  Burrowfiend). It is judged as before, by the collector's unkeyed once-each-turn mark on the Vehicle or Mount.
- `ObjectActionTrigger` has two new components, both dropped from `watchKey()`: `filter`, the card an explore or
  exploit is taken with (`creatureYouControlExplores().withFilter(land)`, Nicanzil, now two abilities as its Oracle text
  says; `creatureYouControlExploits().withFilter(nonHuman)`, Henry Wu), and `door`, the Room door of "when you unlock
  this door" (**CR 709.5h**, checked): `Triggers.doorUnlocked(0)` / `doorUnlocked(1)` (30 Rooms). `doorUnlocked()` is
  the watch key. Other actions reject a filter or a door.
- `PlayerActionTrigger` has a new `filter` component (only on "you set a scheme in motion", dropped from `watchKey()`):
  `youSetSchemeInMotion().withFilter(nonOngoingScheme)` (My Laughter Echoes); Plots That Span Centuries authors its
  plain effect. `SchemeSetInMotionTriggerEffect` is no longer authored.
- **A state trigger's game state is its trigger** (**CR 603.8**, checked): `StateTrigger(predicate, sourcePredicate,
  battlefieldPredicate, maximumBattlefieldMatches, minimumBattlefieldMatches, referencedPermanentPredicate)`, built by
  `Triggers.state(StateTriggerPredicate)`, `sourceState(PermanentPredicate)`, `battlefieldHasAtMost(n, predicate)`,
  `battlefieldHasAtLeast(n, predicate)` and `.withSource(…)`, `.withBattlefieldAtMost(…)`, `.referencing(…)`; the
  ability's effects are its own (62 cards). `Triggers.state()` (no game state) is only the watch key: `Card` rejects it,
  and rejects a `StateTriggerEffect` among a state trigger's effects. The four "when …, if this permanent is an
  enchantment, …" cards (Opal Avenger, Lurking Jackals, Veiled Crocodile, Hidden Predators) have the enchantment check
  as their intervening "if" (`SourceMatchesPermanentPredicate`); Hidden Predators' game state is
  `battlefieldHasAtLeast(1, opponent's creature with power 4 or greater)` (it referred to that creature only to require
  it). The authored description ("Bomb Squad's fuse trigger") is gone; the reader names the ability "[card]'s
  state-triggered ability".

**The engine.**

- `ActionWatch` and `MiscWatch` read the legacy view (`effectsOf` / `registrationsOf`), so every family 11 collector
  that reads through them gets the wrappers it read before. By hand: the crime, conjure and day/night graveyard readers
  use `getEffectiveGraveyardLegacyTriggerEffects`, `CreatureControlService`'s granted control-change triggers the
  legacy temporary and persistent getters.
- `TriggeredAbility.legacyEffects()`: a state trigger is one `StateTriggerEffect.of(state, effects)` (each effect in the
  intervening "if"'s `ConditionalEffect`; a limit or multiplicity throws); a "you set a scheme in motion" ability one
  `SchemeSetInMotionTriggerEffect(filter, effects)` (options throw); an object-action trigger's first-time event, card
  filter and door the unkeyed `OncePerTurnTriggerEffect`, `TriggeringCardConditionalEffect` and
  `TriggeringRoomDoorConditionalEffect`, outside everything (options on a filtered or door ability, and a limit next to
  a first-time event, throw: their collectors read the wrapper first). Limits and intervening "if"s use the generic
  wrappers.
- `StateTriggerService` checks a state trigger's intervening "if" as the game state triggers it (the top-level
  intervening-if `ConditionalEffect`s of the record, through the new `ConditionEvaluationService` dependency; the
  unit-test constructors skip it), and names a record without a description after its card.
- `Card.targetIndicesForEffect` reads a target binding through `OncePerTurnTriggerEffect`,
  `TriggeringRoomDoorConditionalEffect` and `TriggeringCardConditionalEffect`, and `appendSpellTargetingForEffectsFrom`
  through it, so a Room entering with a door unlocked copies the door ability's targets from its legacy wrapper (the
  card bound the plain effect now). `CreateTokenCopyOfSourceEffect` was found by class: Inchblade Companion's token
  copy drops the ability by `withoutSourceEffect(CreateTokenCopyOfSourceEffect.class)` now.
- No wrapper class is deleted: the legacy view still builds them. Run 16 deletes what is left.

**Cards.**

- `scripts/step14-misc-scan.py` (run 13's scan on the factories of `step13-action-slots.tsv` and
  `step13-misc-slots.tsv`) lists the family's wrapped abilities: 189 card classes, 92 with locals (checked by hand for
  wrappers; none but the ones below).
- `scripts/step14-misc-options.py` (`--dry-run`, `--report`) did the conversion: run 13's peeling for the limits and
  intervening "if"s (its `INTERVENING` and `KEEP` tables decide every top-level `ConditionalEffect`), a door rewrite, and
  a `StateTriggerEffect` rewrite that maps each constructor and factory form to the `Triggers` state factories and
  spreads the record's `List.of` effects into the call. Its `MANUAL` table holds Mindlink Mech, Stubborn Burrowfiend,
  Inchblade Companion, Nicanzil, Henry Wu, the two schemes and the four "if this permanent is an enchantment" state
  triggers. Running it again changes nothing.
- `scripts/step14-misc-docs.py` rewrote 48 agent-docs passages (exact replacements). `ARCHITECTURE.md`, the
  implement-card and review-card skills and the `Trigger.watchKey()`, watch and `StateTriggerEffect` Javadoc were edited
  by hand.

**Kept, with their reason.**

- Pompeii ("whenever you roll a blank") and Mr. House ("whenever you roll a 4 or higher"): `EventValueAtLeast`
  conditions on the die result, conditions of the event rather than intervening "if"s; a die-result filter on the
  trigger would say them (run 16 or step 15).
- The conditionals that don't follow the trigger condition stay effects (CR 603.4's last sentence): Valor's Reach's and
  Sokenzan's "if it's a main phase", The Hippodrome's and Kharasha Foothills', Avatar Aang's "then if", Henry Wu's
  "if the exploited creature had power 3 or greater", Victor's resolution counts.
- Olivia, Crimson Bride grants a state trigger through a static ability, still a `StateTriggerEffect` with its
  description.
- The Eerie cards ("whenever an enchantment you control enters and whenever you fully unlock a Room") are still an
  enters ability and an unlock ability sharing one effect (no wrapper); merging them is a later cleanup, as run 12 left
  Freyalise's Winds. Paranormal Analyst's triggering card and Goblin Crash Pilot's delayed trigger are event data and an
  effect.

**Behaviour changes.**

- Rules fix: Veiled Crocodile and Hidden Predators check "if this permanent is an enchantment" again as the ability
  resolves (CR 603.4); they checked it only as the state triggered. New card tests
  (`doesNotTransformWhenNoLongerEnchantmentAtResolution`) resolve it through the engine; the Veiled Crocodile one fails
  on the old form. Opal Avenger and Lurking Jackals already checked both times (a source predicate and a
  `ConditionalEffect`); they check the one option now.
- A state trigger's stack and log text is "[card]'s state-triggered ability" for every card; four cards had their own
  (Aretopolis, Bomb Squad, Garruk Relentless, Orcish Mine).
- The once-each-turn limits of the crime cards, Curator of Sun's Creation and Inchblade Companion are keyed by the
  ability, where the unkeyed wrapper shared the source's mark; none of them has another unkeyed once-each-turn ability.
- Nicanzil has two abilities where it had one; the explore collector still puts every matching effect of the card on
  one stack entry, as before.
- Nothing else changed: the legacy view hands every collector the wrappers it read before. No may question moved.

**For later runs.**

- Run 16: the may-unwrapping, `TriggerMode`, `ConditionalEffect`'s `interveningIf` / `triggerTimeOnly` flags, the
  `onlyIfSacrificed` flags and the wrapper classes left, as planned; the kept die-result conditions above; the Eerie
  merges.
- The explore collector merges the effects of all of a card's explore abilities into one stack object (CR 603.3 wants
  one per ability); the Ring's two triggers (step 13 run 11a) are still apart.
- Step 15: as step 13 runs 11a and 11b listed; also the state trigger's referenced permanent.

**Tests changed.**

- `MiscTriggerTest`: the state-trigger rejection case expects the new messages (no game state, a `StateTriggerEffect`);
  seven new tests: a state trigger's watch key and its one-record legacy view with the intervening "if" (stable
  identity; a limit rejected), a door's and an explored card's watch key and wrappers (options rejected), which actions
  take a door or a filter, the first-time crew and saddle events' watch keys and unkeyed wrapper (a limit rejected),
  the scheme record with and without a filter (options rejected), a limit read as the keyed mark. The factory list
  covers the new factories.
- `StateTriggerServiceTest` (by a one-off script, then by hand): the fixtures author `StateTrigger`s and pass the
  effects to `addTrigger`; the description assertions expect "[card]'s state-triggered ability" or compare the
  effects; the rejection case is `rejectsStateTriggerWithoutGameState`; new `checksInterveningIfAsItTriggers` (with a
  `ConditionEvaluationService` mock).
- `SpellCastingServiceTest#preparingRoomDoorPreservesTheUnlockAbilityTargetGroup` and
  `StackResolutionServiceTest#enteringRoomRetainsItsDoorAbilityTargetGroupsAfterModalCasting` author
  `Triggers.doorUnlocked(1)` with the plain effect.
- `VeiledCrocodileTest`, `HiddenPredatorsTest`: the rules-fix tests above.

**Test runs.**

- Card: the test class of every card that uses a family 11 factory (converted or not) and every card test that
  constructs one of them (699 classes, 5,498 tests): all passing.
- Application: the `service.trigger` and `model.trigger` packages and the state-trigger, stack-resolution,
  spell-casting, creature-control, planechase, state-based action, freeze, triggered-ability, card-ability,
  granted-ability, legacy-slot, architecture, simulation-copy and targeting-consistency classes (1,015 tests): 46
  failures, all known from HEAD (runs 11b to 14): `SpellCastingServiceTest` 10, `StateBasedActionServiceTest` 13,
  `DamageTriggerCollectorServiceTest` 6, `DeathTriggerCollectorServiceTest` 6, `SpellCastTriggerCollectorServiceTest` 3,
  `LandTapTriggerCollectorServiceTest` 2, `PlanechaseServiceTest` 2, `CreatureControlServiceTest` 1, and the known one
  each in `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and `CardEffectTargetingConsistencyTest`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 16a: the leftovers that need no collector rewritten

Run 16 was split, with the user, into 16a and 16b onward. Everything run 16 was planned to delete (the wrapper classes,
`TriggerMode`, `ConditionalEffect`'s `interveningIf` / `triggerTimeOnly` flags, the may-unwrapping) is still read by the
collectors through the legacy view: 681 reads of `TriggeredAbility.legacyEffects()`, the `getLegacy…` getters and the
watches' `effectsOf` / `registrationsOf` in about 60 main files (`TriggerCollectionService` 208, `CombatDamageService`
51, `StepTriggerService` 40, the combat services, the AI), and the 43,700 lines of `service/trigger` pick handlers by
wrapper class. Deleting them is the collectors reading options and grouping effects by ability, which is 16b onward.
16a did what needs no collector rewritten. Step 14 isn't done.

**Authoring.**

- **The event condition** (**CR 603.2**, checked): `TriggerOptions` has a fourth component, `eventCondition` (with a
  three-argument constructor and `withEventCondition(condition)`; the other withers keep it). It is a condition the
  trigger event itself requires ("whenever this creature attacks while you control a Dinosaur", "attacks alone", "an
  opponent who controls a Wedding Ring draws a card during their turn", "when the fourth plan counter is put on this",
  "whenever you roll a 4 or higher") or the one under which the ability exists at all ("Max speed —", a station, level
  or Spaceship threshold, an unlocked door, Struggle for Project Purity's chosen mode, Bloodbraid Marauder's delirium
  cascade). It is checked as the event occurs and never again as the ability resolves, where an intervening "if" is
  checked both times (CR 603.4). It replaces every authored `ConditionalEffect.atTriggerTime` but Typhoid Mary's.
- "When you sacrifice this" is the trigger's cause (`Triggers.sacrificed(...)`); the `onlyIfSacrificed` record
  components are gone from `DrawCardEffect`, `GainLifeEffect`, `DiscardEffect`, `DealDamageToAnyTargetEffect`,
  `SearchLibraryEffect`, `SequenceEffect` and `RegisterDelayedReturnSourceTransformedEffect` (now
  `(boolean underOwnerControl)`), with their `sacrificeOnly(…)` factories and `CardEffect.onlyTriggersOnSacrifice()`.
  The Ordeals and Daemogoth Woe-Eater author the plain effects.
- The Eerie abilities ("whenever an enchantment you control enters and whenever you fully unlock a Room", CR 709.5i,
  checked) are one `TriggeredAbility` of both trigger conditions (CR 603.3, checked): 16 cards, Fear of Infinity's from
  the graveyard. Fear of Sleep Paralysis already was.

**The engine.**

- `TriggeredAbility.legacyEffects()` gives the event condition as the `ConditionalEffect.atTriggerTime` the cards
  wrote, innermost of the legacy view's wrappers (`legacyBaseEffects()`, which every legacy branch now reads instead of
  the raw effects), so every collector that reads the legacy view judges it as before. The pipeline
  (`TriggeredAbilityStackService`) checks it with the intervening "if" as the ability triggers and doesn't wrap the
  effects in it.
- `StepTriggerService` read granted upkeep and end-step abilities raw (`getTemporaryTriggerEffects` /
  `getPersistentTriggerEffects`), which dropped every option of a granted step ability; it reads the legacy getters now.
  Obsidian Fireheart's granted "at the beginning of your upkeep, this land deals 1 damage to you" (its blaze-counter
  event condition) is the only granted step ability with options; its test caught it.
- Teysa Karlov's "a creature dying causes a triggered ability to trigger an additional time" is skipped for the
  collectors of a "whenever [a permanent] is sacrificed" watch, judged from the watch's trigger cause
  (`TriggerCollectionService.triggeredBySacrifice`), where the effects' `onlyTriggersOnSacrifice()` said it for three
  effect classes. The Teysa Karlov rulings (2019-01-25) say "whenever you sacrifice a creature" triggers only once.
- CR 603.5 (checked): four abilities asked their "you may" before they were on the stack, through `pendingMayAbilities`
  of the may-unwrapping path. They go on the stack with their `MayEffect` / `MayPayManaEffect` and ask as they
  resolve: Mimic Vat (the imprint, its source permanent as the non-targeting entry target, so it still imprints after
  the Vat leaves), Vraska the Silencer ("you may pay {1}"), Mishra, Artificer Prodigy and Bounteous Kirin. The
  `DiscardAllCardsWithCastSpellManaValueEffect` handler's may branch asks before the stack too, but no card authors
  it wrapped.

**The may-unwrapping is not removed.** A temporary reflection scan (every registered card × every watch's legacy effects,
against the `TriggerCollectorRegistry`, `DeathTriggerCollectorService` and `LegacyTriggerBindings` handler maps) found
200 abilities whose `MayEffect` / `MayPayManaEffect` reaches a handler only by unwrapping:

- 169 are the spell-cast legacy view's own `MayEffect(SpellCastTriggerEffect)` (its "plain may" form): they go when the
  spell-cast collectors read the ability (16b).
- 31 are event bindings keyed by the wrapped effect's class, which step 15 replaces with effects that read the event:
  the cast-spell copy (Alania, Chancellor of Tales, Gorion, Mica, Owlin Spiralmancer, Pyromancer Ascension, Reality Is
  Mine to Control, Spinerock Tyrant, Swarm Intelligence, Tawnos), the Kirins (Skyfire, Bounteous, Cloudhoof), Mishra,
  Mold Adder (its color filter is a condition-as-effect: a `SpellFilter` would say it), Mana Echoes, Lazav, Screeching
  Scorchbeast, and the creature-death Cemetery Puca, Koh, Mimic Vat, Nim Deathmantle, Vraska the Silencer and Lim-Dûl.
  None asks before the stack any more; all keep their may.

**Cards.**

- `scripts/step14-only-if-sacrificed.py` dropped the flag argument from every call of a constructor arity that took it
  (and the `this(…)` calls inside each record), only where it was `false`, the parameter or a getter read (Biolume Egg's
  `true` on its `sacrificed(THIS)` trigger), and turned the `sacrificeOnly` factories into the plain constructors (8
  card classes and 6 engine files changed; the records' declarations were edited by hand).
- `scripts/step14-event-conditions.py` turned `ConditionalEffect.atTriggerTime(C, E)` into
  `TriggerOptions.DEFAULT.withEventCondition(C), E` in place (or appended `.withEventCondition(C)` to the call's
  options) when every effect of the call is one of the same condition: 56 card classes, plus its `MANUAL` table (Sidar
  Jabari's local variable, Pompeii's "whenever you roll a blank" and Mr. House's "whenever you roll a 4 or higher",
  which were intervening-if `ConditionalEffect`s around the effect). Running it again changes nothing.
- `scripts/step14-eerie-merges.py` merged the 16 Eerie pairs.
- `scripts/step14-only-if-sacrificed-docs.py` and `scripts/step14-event-conditions-docs.py` rewrote the agent-docs
  and skill passages (exact replacements); `ARCHITECTURE.md` was edited by hand.

**Kept, with their reason.**

- Typhoid Mary, Fractured: "choose one at random. If you discarded a card this turn, you choose one instead" is the
  effect's own "if", judged as the ability resolves; the card writes it as two attack abilities gated by
  `atTriggerTime`, which is a modelling of its own (a later fix: one ability with a resolution-time choice).
- Sidar Jabari's "if Sidar Jabari is in the command zone or on the battlefield" stays an intervening-if
  `ConditionalEffect` effect, as earlier runs left it; only its knight condition is the event condition, so the legacy
  view gives exactly the old wrapper.
- `CreateTokenForEmergeSacrificeEffect` (Foul Emissary) is now a bare wrapper of a `CreateTokenEffect`, kept for its
  `LegacyTriggerBindings` handler (step 15).

**Behaviour changes.**

- Rules fix (CR 603.5): Mimic Vat, Vraska the Silencer, Mishra, Artificer Prodigy and Bounteous Kirin put their
  ability on the stack and ask as it resolves; their tests answer the may after the ability resolves, and the first
  test of each asserts the ability is on the stack with no prompt yet.
- Teysa Karlov doesn't double a "whenever you sacrifice" ability whatever its effects; the new Slaughter-Priest of Mogis
  test passes on the old code too (the old path didn't double it either), so no card is known to change.
- Granted step abilities read their options (Obsidian Fireheart's blaze-counter condition), as above.
- The Eerie cards' two trigger conditions are one ability; nothing else is known to change (their tests pass).
- Nothing else changed: the legacy view hands every collector the `atTriggerTime` wrapper it read before.

**For 16b onward.**

- Retire the legacy view family by family: each family's collectors read the ability's options (intervening "if",
  limit, multiplicity, event condition) and filters, and group effects by ability, so `TriggerMode` (`INDEPENDENT` in
  102 authored registrations of 73 card classes, and the legacy view's `PER_BLOCKER`, `ONCE_PER_BLOCK`, `PER_COUNTER`,
  `ONCE_PER_BATCH`) goes; then the may-unwrapping in `TriggerCollectorRegistry.dispatch`,
  `DeathTriggerCollectorService.dispatchCreatureDeath` and `LegacyTriggerBindings.dispatch` (the 169 spell-cast ones
  go with the spell-cast view, the 31 bindings with step 15), the `interveningIf` / `triggerTimeOnly` flags and the
  wrapper classes left.
- The wrappers cards still author, kept by earlier runs for their reasons: `OncePerTurnTriggerEffect.perGrant` (Well
  Rested, Folk Hero, Tyvar), the granted `TriggeringCardConditionalEffect` / `TriggeringPermanentConditionalEffect`
  forms, the authored `AllyCombatDamageTriggerEffect` event bindings, Olivia's granted `StateTriggerEffect`, Typhoid Mary.
- The explore collector's one stack object for several abilities; Freyalise's Winds' and Champions of the Shoal's
  merges (run 12).

**Tests changed.**

- `MimicVatTest`, `VraskaTheSilencerTest`, `MishraArtificerProdigyTest`, `BounteousKirinTest`,
  `EncroachingMycosynthTest#grantedArtifactSpellTriggersMishra`: the CR 603.5 flow (the ability resolves, then the may
  is answered; the pass that used to resolve the accepted ability is gone).
- `DeathTriggerCollectorServiceTest#imprintBakesDyingCardId`: the imprint goes on the stack with its may.
- `SpellCastTriggerCollectorServiceTest#snapshotsArtifactSpellName` (Mishra): the stack form; it failed at HEAD on an
  unstubbed `cardHasType` mock, which it stubs now.
- New: `TriggerOptionsTest` (an event condition kept by the withers; the legacy view's innermost atTriggerTime inside
  an intervening "if" and a limit), `TriggerPipelineTest#aFalseEventConditionKeepsItFromTriggeringAndATrueOneIsNotCheckedAgainOnResolution`,
  `SlaughterPriestOfMogisTest#teysaKarlovDoesNotDoubleTheSacrificeTrigger`.

**Test runs.**

- Card: the test class of every changed card and of the cards whose collectors changed (97 classes), the front faces of
  Blitzwing, Goldbug and Invasion of Kylem, and every card that grants an upkeep or end-step ability (49 classes): all
  passing.
- Application: the `service.trigger` and `model.trigger` packages and the step-trigger, trigger-pipeline, stack-resolution,
  ETB resolver, freeze, architecture, simulation-copy and targeting-consistency classes (974 tests): 39 failures, all
  failing at HEAD (`StepTriggerServiceTest` 16, `DamageTriggerCollectorServiceTest` 6, `DeathTriggerCollectorServiceTest`
  6, `EtbEffectResolverTest` 3 (checked at HEAD in this run), `SpellCastTriggerCollectorServiceTest` 2,
  `LandTapTriggerCollectorServiceTest` 2, `CounterSpellEffectHandlerTest` 1 and the known one each in
  `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and `CardEffectTargetingConsistencyTest`).
- AI: the whole module (1,620 tests), with the same 10 failures as before (two each in the four AI decision engine tests and `TargetPolarityGuardTest`).
- The full suite was not run here.

#### Run 16b: the spell-cast and spell-copy collectors read the ability

The first family of the legacy view's retirement (16b onward), as 16a's findings planned: the "whenever [a player]
casts / copies a [quality] spell" abilities with a `SpellFilter` (step 13's family 2, the cast and copy part; 1,205
abilities, 1,170 cast and 35 copy, in 1,120 card classes, by a temporary reflection scan of every registered
card). Their collectors read the ability itself (its filter, options and effects) and the legacy view no longer
builds a record for them. The land-play and activation abilities of family 2 still read the legacy view (16c onward).

**What the scan found.** Every filtered spell ability's effects are `TriggerMode.NORMAL`, its multiplicity `EACH` and
none has an event condition; the options are intervening "if"s (46) and, all on the battlefield, 13 "triggers only
once each turn", 3 "do this only once each turn" and 1 "triggers only once" (Sapphire Collector). Off the battlefield:
17 in a graveyard, 3 in exile (Hex, Kellan's Companion's two with intervening "if"s), 2 in the command zone, 1 in a
hand (Summitfest Closing Ceremony, also in the graveyard).

**The engine.**

- A filtered `SpellTrigger` is its own `watchKey()` (new `SpellTrigger.unfiltered()` names the trigger it narrows),
  so `Card.getLegacyTriggerEffects(youCastSpell(), zone)`, the granted, perpetual and temporary legacy getters no
  longer give filtered spell abilities. `TriggeredAbility.legacyEffects()` lost its cast and copy branches
  (`legacySpellCastEffects`, `legacySpellCopyEffects`); a filtered spell ability's view is the generic one, which no
  collector reads.
- New `CastAbility` (`service/trigger`): what the generic spell-cast collector judges, the `SpellFilter`, the
  effects (one stack object, **CR 603.3**), the intervening "if" (**CR 603.4**), a lone plain "you may [pay {N}]" as
  the optional part on the battlefield and in exile (targets chosen as it triggers, **CR 603.3d**, the choice made as
  it resolves, **CR 603.5**), and a target filter only an authored record names. `CastAbility.of(TriggeredAbility,
  SpellFilter, onStackPath)` builds it from an ability, `CastAbility.of(SpellCastTriggerEffect, MayEffect)` from the
  kept authored records. `SpellCastTriggerCollectorService.handleGenericSpellCastTrigger` judges a `CastAbility`;
  `collectCastAbility(match, castAbility, spellCast, perManaFromSource)` (the per-mana copies of "when you spend this
  mana", **CR 106.6**) and `collectCopyAbility(match, filter, effects, spellCopy)` are the new entry points. The
  `SpellCastTriggerEffect` handlers convert the authored record. The once-only unwrap and mark inside the handler are
  gone: the dispatch judges limits.
- `TriggerCollectionService.dispatchWatch` hands a battlefield cast or copy watch to the new `dispatchSpellWatch`,
  which walks the permanent's abilities in order (a filtered one to the collector, any other through its legacy
  registrations, new `Card.getLegacyTriggerEffectRegistrations(TriggeredAbility)` keeping their identity), then the
  granted ones the same way (`GrantedTriggeredAbilitySupport.grantedTriggeredAbilities`, now public). Face-up schemes
  go through it too. `collectSpellAbility` judges and marks the limit (`TriggeredAbilityStackService.limitReached` /
  `markLimit`, now public, keyed by `TriggeredAbility.limitKey()`), says "triggers only once each turn" and "do this
  only once each turn" to the `dispatch` wrapper (no Clara Oswald copies, as the `OncePerTurnTriggerEffect` said)
  and marks "do this only once each turn" on acceptance (**CR 603.2h**), and a granted cascade is one copy (new
  `dispatch(…, singleCopy)`). The printed filter is read through the permanent's text changes (new
  `TextChangeTransformer.transformSpellFilter`, the own replacements then the global ones, in timestamp order when an
  own one is timestamped, **CR 612.1**, **CR 613.7**).
- The exile and planar readers (`dispatchCardSpellAbilities`), the graveyard reader (new
  `GameQueryService.getEffectiveGraveyardTriggeredAbilities`), the hand and command-zone readers and the departed
  mana-source rider read the abilities. The graveyard reader also checks an intervening "if" (none is written); the
  graveyard, hand and command-zone readers throw on options they don't judge, where the legacy view's wrapped form
  made them skip the ability silently (none is written). The hand and command-zone readers put the ability's effects
  on the stack as written (a lone "you may pay" kept its payment, where the record dropped it; none is written).
- `TriggeredAbilityQueueService.triggerOrderLabel` and `BecomeCopyOfTargetCreatureUntilEndOfTurnEffectHandler`'s
  retained ability (Hulkling, Young Avenger) read the ability's spell trigger.
- `SpellCopyTriggerEffect` is deleted (only the legacy view built it). `SpellCastTriggerEffect` stays for emblems and
  the kept authored abilities (Pontiff of Blight, Leshrac's Sigil, Gale, Conduit of the Arcane, Passionate
  Archaeologist, Folk Hero), which reach the collector through the registry, Pontiff's through its may-unwrapping.

**The may-unwrapping is not removed.** The 169 spell-cast abilities that reached a handler only by it
(`MayEffect(SpellCastTriggerEffect)`) no longer exist; the 31 event bindings 16a listed and Pontiff of Blight's
authored extort still need it (step 15).

**Behaviour changes.**

- "Triggers only once" (Sapphire Collector) is keyed by the ability (`keyedOnceOnlyTriggersFired`), as the pipeline
  keys it, where the unwrapped `OnceOnlyTriggerEffect` used the source's unkeyed mark; the card has no other such
  ability.
- A filtered cast ability that functions on the battlefield no longer triggers from a suspended card in exile (the
  time-counter reader read the battlefield watch); no card with suspend has one, but a card exiled with time counters
  by another effect would have triggered there (**CR 113.6**).
- Nothing else changed: the scan found no options, zones or effect shapes the old readers judged differently.

**For 16c onward.**

- The rest of family 2 (land-play and activation abilities, `TriggeringCardConditionalEffect`,
  `TriggeringPermanentConditionalEffect`, the `Triggering…AbilityConditionalEffect`s, the land-play zone records),
  then the other families, as 16a planned.
- The kept authored `SpellCastTriggerEffect`s (Pontiff's extort could be a filtered granted ability now; Leshrac's
  Sigil and Passionate Archaeologist need step 15's event player) and the emblem collector.
- The cast readers off the battlefield still judge only part of the filter (spell, stack object, nth,
  mana-spent gates); a later run can send them through the generic collector.

**Tests changed.**

- `SpellAbilityTriggerTest`: a filtered cast trigger is its own watch key and narrows `unfiltered()`; the legacy view
  of the unfiltered trigger leaves filtered abilities out (granted ones too); a copy trigger's filter; the three tests
  of the deleted cast records and limits removed.
- `SpellCastTriggerCollectorServiceTest`: the tests built on the legacy record (`legacy(…)`) collect a `CastAbility`
  through `collectCastAbility`; the copy tests use `collectCopyAbility` (the target filter from the bound target
  group, permanent spells always counted).
- New `CastAbilityTest` (an ability's filter, effects and intervening "if"; the optional part per path; an authored
  record's fields as a filter). `TextChangeTransformerTest`: two tests for `transformSpellFilter`.

**Test runs.**

- Card: the test class of every card with a filtered cast or copy ability, every card test that constructs one, and
  the text-change, retained-copy, authored-record and 16a cards (1,834 classes, 16,336 tests): all passing. Sapphire
  Collector's again after the last `CastAbility` change.
- Application: the `service.trigger` and `model.trigger` packages and the pipeline, step-trigger, stack-resolution,
  text-change, trigger-queue, spell-casting, planechase, ETB resolver, freeze, architecture, legacy-slot,
  granted-ability, simulation-copy and targeting-consistency classes (1,071 tests): 50 failures, all known from HEAD
  (`StepTriggerServiceTest` 16, `SpellCastingServiceTest` 10, `DeathTriggerCollectorServiceTest` 6,
  `DamageTriggerCollectorServiceTest` 6, `EtbEffectResolverTest` 3, `SpellCastTriggerCollectorServiceTest` 2,
  `LandTapTriggerCollectorServiceTest` 2, `PlanechaseServiceTest` 2 and the known one each in
  `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and `CardEffectTargetingConsistencyTest`).
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- `magical-vibes-webservice`'s test sources don't compile (`CardBrowserServiceTest`'s anonymous `CardCatalog` lacks
  `findLoadedPrintingByName`); this run doesn't touch them.
- The full suite was not run here.

#### Run 16c: the land-play and activation collectors read the ability

The rest of step 13's family 2, as 16b's findings planned: the "whenever [a player] plays a land" abilities with a land
filter or a zone, and every "whenever [a player] activates an ability" ability (78 card classes by a grep of the
cards' `Triggers` calls: 36 with a land-play ability, 42 with an activation ability). Their collectors read the
ability itself, and the legacy view no longer builds a wrapper or a record for them. No card class changed.

**What the scan found.** Every activation ability has one effect; the options are Wizened Mentor's, Elrond's and
Satoru Umezawa's "triggers only once each turn" and Vazi's intervening "if" ("if mana from a Treasure was spent to
activate it"). Of the land-play abilities, 9 have a land filter (Hidden Herd's with an intervening "if") and 9 a zone
("from exile" 8, Shadow of the Goblin's "from anywhere other than your hand"); every one has one effect too (a
`SequenceEffect` where the card says several things). Hidden Stag's and Fastbond's intervening "if"s are on
unfiltered abilities. None has a limit, an event condition or a multiplicity, and no land-play or activation ability is
granted but Tyvar the Bellicose's "a mana ability of this creature resolves", which has no filter.

**The engine.**

- A filtered `LandPlayTrigger` (new `filtered()`: a land filter, `playedFrom` or `notPlayedFrom`) is its own
  `watchKey()`, and new `unfiltered()` names the trigger it narrows, as 16b made `SpellTrigger`. `AbilityTrigger` is
  always its own key now, with `unfiltered()` too, and new `AbilityKind.matches(ActivatedAbility)` says what the kind
  wrappers said. `TriggeredAbility.legacyEffects()` lost its land-play and activation branches
  (`legacyLandPlayZoneEffects`, `legacyKindWrapper`).
- Land play: `TriggerCollectionService.dispatchSpellWatch` reads the land-play watches on the battlefield too, and the
  granted and planar readers (`dispatchCardSpellAbilities`; the face-up planes' land-play abilities go through it now)
  send a filtered land-play ability to new `PlayedCardNameTriggerCollectorService.collectLandPlayAbility`. It judges
  the zone, the land filter and the intervening "if" (**CR 603.4**) and puts the effects on the stack as one object
  (**CR 603.3**), with the player who played the land as "that player" (the non-targeting target) and the land as the
  triggering card; an intervening "if" is wrapped around the effects for the check on resolution, and so is Cemetery
  Protector's and Cemetery Gatekeeper's "if it shares a card type with the exiled card" (the land filter as a
  `TriggeringCardConditionalEffect`, as the old handler kept it). When the effects are bound to a target group or
  target a permanent (Rocco, Street Chef's "from exile" ability), the targets are chosen as it triggers
  (**CR 603.3d**), as the zone records' handler did. The limit is judged and marked around the collector by new
  `collectLimitedAbility`, which 16b's `collectSpellAbility` now uses too. Unfiltered land-play abilities still read
  the legacy view (their effects are event bindings, or plain effects the default handler takes, with Hidden Stag's
  and Fastbond's intervening "if"s as `ConditionalEffect`s).
- Activations: the seven activation collectors (`checkControllerActivatesAbilityTriggers`, its copy, ninjutsu,
  exhaust, embalm-or-eternalize, non-mana and opponent forms, and `collectNonTapActivationTriggersBeforeCosts`) iterate
  the card's activation abilities (`activationAbilities`, by `AbilityTrigger.unfiltered()`) instead of the legacy
  view's effects. New `activationEffects` judges the kind and the source filter (from the watcher's point of view, as
  `resolveTriggeringPermanentConditional` did), the limit (`TriggeredAbilityStackService.limitReached` / `markLimit`,
  keyed by `limitKey()`, the key the legacy `OncePerTurnTriggerEffect.keyed` marked), the event condition and the
  intervening "if": Vazi's Treasure condition from the activation, and not again (as
  `resolveTreasureManaActivationConditional` did); any other wrapped for the check on resolution. "Do this only once
  each turn" marks on acceptance (**CR 603.2h**). An ability's effects are one stack object. `PreCostActivationTriggers`
  names the abilities it collected before the costs (`abilities()`), where it named effects by identity. The graveyard
  exhaust reader (Afterburner Expert) reads `getEffectiveGraveyardTriggeredAbilities` and throws on options it doesn't
  judge.
- Deleted: `LandPlayFromExileTriggerEffect`, the seven `Triggering…AbilityConditionalEffect`s (loyalty, ninjutsu,
  outlast, power-up, boast, with and without {T}), `CardEffect.resolveForActivatedAbility`, the zone records' and the
  land filter's land-play handlers, and `PlayFromOutsideHandTriggerCollectorService` (only the legacy view's record
  reached it; Apple of Eden's registered "this way" abilities are judged by the temporary-global readers through
  `TemporaryGlobalTriggerEffect.matches`, so `PlayFromOutsideHandTriggerEffect` stays for them).
  `TriggeringCardConditionalEffect` and `TriggeringPermanentConditionalEffect` stay: other families' legacy view and
  some cards still give them.

**Behaviour changes.**

- None known. The land-play stack entries now always carry the triggering land and "that player" (Shadow of the
  Goblin's and an opponent's "from exile" entries didn't; their effects don't read them), and an ability with several
  effects would be one stack object (no land-play or activation ability has several).

**Kept, with their reason.**

- The land filter is not read through text changes (**CR 612.1**): the old wrapper wasn't either
  (`TextChangeTransformer` has no `TriggeringCardConditionalEffect` case). Jokulmorder's "whenever you play an Island"
  under a land-type text change is a later fix (a `transformCardPredicate` like `transformSpellFilter`, with a test).
- Bill Potts's one "this ability triggers only once each turn" over a cast and an activation trigger is still its two
  authored `OncePerTurnTriggerEffect`s sharing the source's unkeyed mark; the copy collector unwraps it (one ability of
  both conditions is a later merge).
- The Fourth Doctor's `OncePerTurnTriggerEffect(CreateFoodWhenPlayingCardFromTopOfLibraryEffect)` (an event binding,
  step 15).
- `collectNonTapActivationTriggersBeforeCosts` reads the non-mana watches' `WITHOUT_TAP_COST` abilities, as before; no
  card has one (Haunting Wind, Artifact Possession and Powerleech are "activates an ability", read by the activation
  collectors after the costs).
- The activation collectors' copies for Clara Oswald's "triggers an additional time" ignore the limit, as before.

**For 16d onward.**

- The unfiltered spell and land-play abilities with options (Hidden Stag, Fastbond, the cast ones 16b listed) still
  read the legacy view's `ConditionalEffect` / `OncePerTurnTriggerEffect`; a `youPlayLand().anyLand()` like
  `anySpell()` would let the land-play collector read them, the event bindings staying for step 15.
- `ON_SELF_MANA_ABILITY_RESOLVES` (Tyvar's granted ability, `OncePerTurnTriggerEffect.perGrant`) reads the legacy view.
- The other families, as 16a planned.

**Tests changed.**

- `SpellAbilityTriggerTest`: the four tests of the deleted legacy forms (a land filter and a kind as wrappers, the
  shared land filter of one ability's conditions, the zone records) became a filtered land-play trigger being its own
  watch key and narrowing `unfiltered()`, the unfiltered trigger's legacy view leaving filtered land-play abilities out
  (an unfiltered one's intervening "if" still wrapped), an activation trigger's `unfiltered()`, and new
  `anAbilityKindIsJudgedFromTheActivatedAbility`.
- `ActivatedAbilityExecutionServiceTest` builds `PreCostActivationTriggers` as before (its second list is now of
  abilities); unchanged.

**Test runs.**

- Card: the test class of every card with a land-play or activation ability and every card test that uses one of
  those cards (127 classes, 1,120 tests): all passing.
- Application: the `service.trigger` and `model.trigger` packages and the activation-execution, ability-activation,
  pipeline, text-change, freeze, architecture, simulation-copy and targeting-consistency classes (911 tests): 69
  failures, all known from HEAD (`ActivatedAbilityExecutionServiceTest` 35 and `AbilityActivationServiceTest` 15,
  checked at HEAD in this run with the change stashed: the same 50; `DeathTriggerCollectorServiceTest` 6,
  `DamageTriggerCollectorServiceTest` 6, `SpellCastTriggerCollectorServiceTest` 2, `LandTapTriggerCollectorServiceTest`
  2 and the known one each in `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and
  `CardEffectTargetingConsistencyTest`).
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 16d: family 2's rest

The plan left 16d onward unscoped ("family by family"); the user chose to close family 2 in 16d, as 16c's findings
listed it, and to start step 13's family 1 (steps and phases: 2,315 card classes, 681 with options, the 7,790 lines
of `StepTriggerService`) in 16e. After 16d no spell, land-play or activation ability has a legacy view: every reader of
family 2 reads the ability itself and judges its options.

**What the scan found.** A temporary reflection scan of every registered card, its own abilities and the abilities its
effects grant, for the family 2 abilities that still went through the legacy view with options, a wrapper or a
non-default `TriggerMode`: 25 abilities.

- "When you cast this spell" (from the stack): Apex Devastator's four `INDEPENDENT` cascades, Bloodbraid Marauder's
  event condition (delirium), and the intervening "if"s of Bygone Marvels, Kozilek, the Great Distortion, Scourge of the
  Skyclaves and Unbreakable Remnant.
- Unfiltered cast abilities whose effect is an event binding (copy that spell): Chandra, Hope's Beacon, Donal, Herald
  of Wings, Jin-Gitaxias, Progress Tyrant and The Sixth Doctor ("triggers only once each turn"), Cosmic Crucible, Iron
  Man, Bleeding Edge and Spider-Verse ("do this only once each turn"); Bill Potts's authored
  `OncePerTurnTriggerEffect`; Pardic Dragon's "if this card is suspended", read from exile.
- Unfiltered land-play abilities with an intervening "if": Hidden Stag (two) and Fastbond.
- Authored wrappers: The Fourth Doctor's two `OncePerTurnTriggerEffect`s around one Food binding, and the granted
  `OncePerTurnTriggerEffect.perGrant` of Folk Hero (around an authored `SpellCastTriggerEffect`) and Tyvar the
  Bellicose ("whenever a mana ability of this creature resolves").

No ability mixes a family 2 trigger with another family's.

**The model.**

- New `Trigger.collectedAsAbility()`, true for `SpellTrigger`, `LandPlayTrigger` and `AbilityTrigger`, and
  `TriggeredAbility.collectedAsAbility()`. Such an ability has no legacy view: `legacyEffects()` throws, the card's slot
  index leaves it out of the legacy maps, and the `getLegacy…` getters of `Card`, `Permanent`, `GameData`
  (`perpetualLegacyTriggerEffects`) and `GrantedTriggeredAbilitySupport.grantedTriggerEffects` throw for a family 2
  trigger (new `TriggeredAbility.requireLegacyView`), as do `SpellAbilityWatch.effectsOf` and `registrationsOf`. A
  reader left on the legacy view fails at once instead of reading the effects without their options; one did
  (`CastTargetInstantOrSorceryFromGraveyardHandler`'s "do this only once each turn" sweep over the source's abilities,
  found by Harness the Storm's tests), and reads a family 2 ability's limit from its options now.
- `TriggeredAbility` has a fifth component, `instance` (**CR 113.2c**, checked: each instance of an ability functions
  independently): which of several equal instances one object has it is, or `null`, with `withInstance`; `limitKey()`
  differs by it and the withers keep it. `GrantTriggeredAbilityEffectHandler` makes a granted ability with a limit the
  granting permanent's instance (`"granted-by:" + id`), so each grant keeps its own "triggers only once each turn", as
  `perGrant` did for its wrapper. The 4-argument constructor stays.

**The engine.**

- `TriggerCollectionService.dispatchWatch` hands every spell, land-play and activation watch to `dispatchSpellWatch`
  (the counters and countered watches too); `readsFilteredAbilities` is gone. An unfiltered ability (no spell filter,
  land filter or zone), on the battlefield, granted, in exile or on a face-up plane, goes to the new
  `collectUnfilteredAbility`: new `triggeredEffects` checks the event condition and the intervening "if" as it
  triggers (**CR 603.2**, **CR 603.4**) and wraps the intervening "if" around each effect for the check as it
  resolves; a limit is judged and marked by `collectLimitedAbility` (16c), keyed by `limitKey()`, and only an ability
  with one effect may have one; each effect goes to the watch's handlers, which bind the event data (step 15), with the
  printed abilities' effects read through text changes as before.
- The "when you cast this spell" reader reads the card's abilities (judging the event condition and intervening "if"
  in the spell's context, throwing on a limit) and the replicate check reads the raw effects; the "when this spell is
  countered or doesn't resolve" reader, the suspended-card reader (Pardic Dragon, Deep-Sea Kraken) and the
  mana-ability-resolves reader (Tyvar's grants) read abilities through `triggeredEffects` / `collectUnfilteredAbility`.
  New `requireNoLimit` for the readers of a card that isn't a permanent. `EffectResolution.hasManaSpentToCastAmount`
  reads the raw "when you cast this spell" effects.
- The cast-spell copy handler reads "do this only once each turn" from
  `TriggerMatchContext.markSourceOncePerTurnOnAcceptance()` (the limit's flag) as well as from an authored wrapper.

**Cards.** `scripts/step14-family-two-leftovers.py` (exact replacements, idempotent): Tyvar's and Folk Hero's granted
abilities take `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)` (Folk Hero's `SpellCastTriggerEffect`
became `Triggers.youCastSpell().withFilter(new CardSharesCreatureTypeWithSourcePredicate())` with a plain
`DrawCardEffect`, read by the spell-cast collector); The Fourth Doctor's two abilities are one of both conditions
(`youCastSpell()`, `youPlayLand()`) with the limit; Apex Devastator's cascades drop `TriggerMode.INDEPENDENT` (each
`addTrigger` is an ability of its own). `scripts/step14-family-two-leftovers-docs.py` rewrote the `ARCHITECTURE.md`,
`EFFECTS_QUICK_REFERENCE.md` and `ORACLE_TEXT_EFFECT_MAP.md` passages.

**Behaviour changes.**

- Rules fix (CR 113.2c): a granted ability with an options limit is an instance per granting permanent, so a creature
  that two Danny Pinks (or two Niambi, Beloved Protectors) grant the ability draws once for each, where both instances
  shared one key and only one triggered. New `DannyPinkTest#eachDannyPinkGrantsAnIndependentOncePerTurnAbility` and
  `TyvarTheBellicoseTest#eachTyvarGrantsAnIndependentOncePerTurnAbility`; with the instance turned off they fail, as do
  Folk Hero's per-grant test and Tyvar's blink test.
- Bloodbraid Marauder's cascade: delirium is judged by the reader, and the cascade goes on the stack as the cascade
  object ("'s cascade ability", with the spell's X, kicker and zone), where the event condition's wrapper put it in the
  generic self-cast object. Its tests pass.
- Nothing else is known to change: the other readers hand the handlers what the legacy view did (the intervening "if"
  as the `ConditionalEffect` the land-play default handlers and Pardic Dragon's handler judge).

**Kept, with their reason.**

- The "when you cast this spell" reader still puts the plain effects of all the card's such abilities on the stack as
  one object. Grouping them by ability broke 12 cards that write one Oracle ability as several `addTrigger` calls
  (Hydroid Krasis's "you gain half X life and draw half X cards", Decimator of the Provinces, Abundant Maw, Hideous
  Taskmaster, Flayer of Loyalties, The Beast, Deathless Prince, Kozilek Compleated, Mutated Cultist, Wingbright Thief,
  Oblivion Sower, Gluttonous Hellkite, Sauron, Lord of the Rings); a later run merges those by script and then groups.
- An unfiltered ability's event bindings still receive its intervening "if" as a `ConditionalEffect` (Hidden Stag,
  Fastbond, Pardic Dragon), and an unfiltered ability with a limit may have one effect only: its handler puts it on
  the stack. Step 15 makes those effects read the event, and then the generic collector can take them.
- Bill Potts's two authored `OncePerTurnTriggerEffect`s share the source's unkeyed mark across a cast and an
  activation condition whose event bindings differ (`CopyControllerCastSpellOnSpellCastEffect`,
  `CopyControllerActivatedAbilityTriggerEffect`): one ability of both conditions needs step 15's "copy that spell or
  ability".
- `OncePerTurnTriggerEffect.perGrant` stays for Well Rested (a tap trigger, still on the legacy view), and the
  may-unwrapping for the 31 event bindings 16a listed and Pontiff of Blight's authored extort.

**For 16e onward.**

- Family 1 (steps and phases), then the other families, as 16a planned. Each can reuse `collectUnfilteredAbility`'s
  shape: options judged by the reader, effects to the handlers, until step 15.
- Merge the "when you cast this spell" abilities cards split, then group that reader's effects by ability (CR 603.3).

**Tests changed.**

- `SpellAbilityTriggerTest`: the legacy-view tests of the unfiltered land-play and cast triggers and the copy trigger
  assert that the unfiltered trigger finds only its own abilities and that the legacy getters throw.
- New: `TriggeredAbilityTest#anInstanceOfAnAbilityKeepsItsOwnLimitKeyAndTheWithersKeepTheInstance` and
  `#aSpellLandPlayOrActivationAbilityHasNoLegacyView`, the Danny Pink and Tyvar tests above.

**Test runs.**

- Card: the test class of every card with a "when you cast this spell", "when this spell is countered", "you counter
  a spell", "a spell you control is countered" or mana-ability-resolves ability, an unfiltered cast, copy or land-play
  ability, or a replicate or demonstrate effect, and of Danny Pink, Niambi, Mirror Box, Hidden Stag and Fastbond (378
  classes, 3,116 tests), and Folk Hero's: all passing.
- Application: the `service.trigger`, `model.trigger`, `architecture` and `service.spell` packages and the
  triggered-ability, granted-ability, freeze, pipeline, targeting-consistency, stack-resolution, text-change,
  step-trigger and planechase classes, with the engine module's `service.trigger` and `service.effect` packages (1,076
  application tests): 48 failures, all known from HEAD (`StepTriggerServiceTest` 16, `SpellCastingServiceTest` 10,
  `DeathTriggerCollectorServiceTest` 6, `DamageTriggerCollectorServiceTest` 6, `SpellCastTriggerCollectorServiceTest`
  2, `LandTapTriggerCollectorServiceTest` 2, `PlanechaseServiceTest` 2, the known one each in
  `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and `CardEffectTargetingConsistencyTest`, and
  `CostEffectClassificationTest`'s unclassified `RevealSourceChosenSubtypeCost`, which this run doesn't touch). The
  engine module's tests pass.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 16e: steps and phases read the ability

Step 13's family 1, as 16d's findings planned: every "at the beginning of [step or phase]" ability (a `StepTrigger`,
collected by `StepTriggerService` and the planar die's step triggers) has no legacy view any more, and every reader of
one reads the ability and judges its options.

**What the scan found.** A temporary reflection scan of every card class, its own abilities and the abilities its
effects grant (2,461 step abilities in 2,315 card classes): the options are intervening "if"s (660, all printed), 12
event conditions (one granted: Obsidian Fireheart's blaze-counter upkeep), and "this ability triggers only once" on the
three second-main-phase Collectors / Cheerleader (Acrobatic Cheerleader, Jet Collector, Pearl Collector). Every
multiplicity is `EACH`, no ability mixes a step trigger with another family's, and the only `TriggerMode` is Ashling,
Rimebound's two `INDEPENDENT` precombat-main abilities. Off the battlefield: 28 in a graveyard, 3 in exile (All
Hallow's Eve, Curse of the Cabal, Tawnos Endures' granted one, which still authored its intervening "if" as a
`ConditionalEffect`), 2 in the command zone (Oloro, Dark Wings Bring Your Downfall). Only Abstract Paintmage and Sanctum
of Stone Fangs wrote one Oracle ability as two step abilities (both precombat main).

**The model.** `StepTrigger.collectedAsAbility()` is true: `TriggeredAbility.legacyEffects()` and the `getLegacy…`
getters throw for a step trigger, and the card's slot index leaves step abilities out of its legacy maps. New
`Card.getTriggeredAbilities(Trigger, Zone)` and `getTriggeredAbilities(Trigger)` (the abilities with that trigger,
through the slot index's trigger map, so a card without one costs a lookup) and a
`TriggeredAbility(TriggerCondition, TriggerOptions, CardEffect...)` constructor.

**The engine.**

- New `StepAbilityReader` (`service/turn`, a plain helper over `ConditionEvaluationService`): for each ability it
  checks the event condition (**CR 603.2**) and the intervening "if" (**CR 603.4**) as it triggers, in the condition
  context the step's body gives (the source permanent, or its card for one off the battlefield or a face-up scheme at
  the beginning of combat; its controller; for "that player" the player whose step it is), judges and marks "this
  ability triggers only once" by `TriggeredAbility.limitKey()` on the source (`TriggeredAbilityStackService.limitReached`
  / `markLimit`), throws on any other limit or a multiplicity (none is written), and returns the effects with the
  intervening "if" wrapped around each as the `ConditionalEffect` that checks it again as it resolves; the event
  condition isn't wrapped. `effectsByAbility` gives one list for each ability.
- `StepTriggerService`'s 47 legacy reads read abilities through it: the printed ones by trigger and zone
  (`printedEffects`), the ones granted for a duration (`durationGrantedEffects`, `StepAbilityReader.granted`), the
  ones static abilities grant (`staticGrantedEffects`, `GrantedTriggeredAbilitySupport.grantedTriggeredAbilities`), a
  graveyard card's own and perpetual ones (`graveyardEffects`, `GameQueryService.getEffectiveGraveyardTriggeredAbilities`)
  and an exiled card's perpetual ones. The bodies keep their routing by effect kind and their checks of authored
  `ConditionalEffect`s (until step 15). The main-phase bodies no longer unwrap `OnceOnlyTriggerEffect`
  (`OnceOnlyTriggerSupport` stays for the draw and attack collectors). The precombat-main body puts each ability's
  non-targeting effects on the stack as one object (**CR 603.3**), where it combined every non-`INDEPENDENT` effect of
  the permanent into one; the registration's `TriggerMode` is no longer read.
- `GrantedUpkeepEffectSupport` gives abilities (`grantedUpkeepAbilities`, `grantedEachUpkeepAbilities`; a granted
  cumulative upkeep is the ability "at the beginning of your upkeep, …", **CR 702.24a**), where it gave the first effect
  of each granted ability raw, dropping its options and its other effects.
- `PlanechaseService.trigger` reads a plane's step abilities through the reader (every other planar trigger still
  through the legacy view).
- Code that only inspects which effects a card has reads the raw effects: `Permanent.hasCumulativeUpkeep`,
  `GameSetupService`'s conspiracy marker (`getTriggeredAbilities(endStep(YOU), COMMAND)`), `TargetLegalityService`'s
  Oath active-player check, `PermanentCounterSupport` (Divine Intervention) and `PermanentRemovalService` (Wall of
  Nets).

**Cards.** `scripts/step14-step-abilities-leftovers.py` (exact replacements, idempotent): Ashling, Rimebound drops
`TriggerMode.INDEPENDENT` (each `addTrigger` is an ability of its own); Abstract Paintmage's "add {U}{R}" and Sanctum of
Stone Fangs' "each opponent loses N life and you gain N life" are one ability of both effects each; Tawnos Endures'
granted "if this card is exiled" is `TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInExile())`. The
`ARCHITECTURE.md` step paragraph, the Tawnos Endures `ORACLE_TEXT_EFFECT_MAP.md` row and the implement-card skill's step
paragraph (one Oracle ability is one `addTrigger`, never `INDEPENDENT`) were edited by hand.

**Behaviour changes.**

- Granted upkeep abilities (`GrantedUpkeepEffectSupport`) put all their effects on the stack and have their options
  judged, where only the first effect went, raw.
- Event-condition step abilities go on the stack without the `atTriggerTime` wrapper, so a body routes them by their
  own effect's kind (Deadwood Treefolk's vanishing, Endrider Spikespitter, the Room doors, …).
- An intervening "if" is checked as the ability triggers by every step body; the enchanted-player and
  enchanted-permanent-controller draw-step bodies and the suspended-card upkeep body didn't check one (no ability
  there has one but Curse of the Cabal, whose condition the time counters the body requires already imply).
- "This ability triggers only once" is keyed by the ability, not the source (the three cards have one such ability).
- Nothing else is known to change: each body judges the options in the context it judged the legacy wrappers in.

**Kept, with their reason.**

- The step bodies still route effects by kind and put most effects on the stack one by one (one per effect, or all of
  a permanent's together): grouping every body by ability waits for step 15's event-reading effects, which retire the
  routing. Only the precombat-main body, which read `TriggerMode.INDEPENDENT`, groups by ability now.
- The authored effect-text `ConditionalEffect`s run 5 kept (`NOT_INTERVENING`: Aggression, Clockwork Dragon, Clockwork
  Vorrac, Kjeldoran Frostbeast, Laboratory Drudge, Ludevic, Throat Wolf, Does Machines) are still checked by the bodies
  as the ability triggers; when the `interveningIf` flag goes they become `ConditionalEffect.unless` (checked only as
  they resolve).
- `TriggerMode` stays: other families still author `INDEPENDENT`.

**For 16f onward.**

- The other families (combat, damage, counters, targets, library, tap, life, sagas, misc, zone changes), as 16a
  planned; each can reuse `StepAbilityReader`'s shape (options judged in the reader's context, effects to the handlers).
- Merge the "when you cast this spell" abilities cards split (16d).

**Tests changed.**

- `StepTriggerServiceTest`: the graveyard stub reads `getEffectiveGraveyardTriggeredAbilities`; the granted
  beginning-of-combat and end-step tests stub `grantedTriggeredAbilities` (honouring its predicate) instead of the
  legacy `grantedTriggerEffects`. New: an upkeep event condition judged as it triggers and not put on the stack, a
  granted upkeep ability's intervening "if" judged and wrapped, and each precombat-main ability one stack object.
- `TriggeredAbilityTest#aStepAbilityHasNoLegacyViewAndIsFoundByTriggerAndZone` (new).

**Test runs.**

- Card: the test class of every card with a step ability (by the scan), and every card test that names a step card
  without a test of its own (back faces) or one of the four changed cards (2,270 classes, 20,771 tests): all passing.
- Application: `StepTriggerServiceTest`, `TriggeredAbilityTest`, `PlanechaseServiceTest`, `StepTriggerTest`,
  `TriggerOptionsTest`, `CardFreezeTest`, `SimulationCopyCompletenessTest`, `GrantedAbilitiesTest`,
  `TargetLegalityServiceTest`, `PermanentRemovalServiceTest`, `MiscTriggerTest`, `SpellAbilityTriggerTest`,
  `TriggerPipelineTest`, `TurnProgressionServiceTest`, `GameQueryServiceTest`, `LegacyTriggerBindingsTest`,
  `TriggerConditionTest`, `CardAbilitiesTest` (981 tests with the new ones): 92 failures, all known from HEAD
  (`PermanentRemovalServiceTest` 46, `StepTriggerServiceTest` 16, `TargetLegalityServiceTest` 14,
  `TurnProgressionServiceTest` 10, `PlanechaseServiceTest` 2, `SimulationCopyCompletenessTest` 1, `GameQueryServiceTest`
  1); `StepTriggerServiceTest`'s two granted-ability tests failed until they stubbed the ability reader.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- `magical-vibes-webservice`'s test sources still don't compile (`CardBrowserServiceTest`); not touched here.
- The full suite was not run here.

#### Run 16f: combat reads the ability

Step 13's family 3, the user's choice for 16f (all of combat in one run, where 16e's findings listed the families
left): every "whenever [a creature] attacks / blocks / becomes blocked / deals combat damage" ability (a
`CombatTrigger`, collected by `CombatAttackService`, `CombatBlockService`, `CombatDamageService`,
`CombatTriggerService`, `TriggerCollectionService` and the combat handlers of `DamageTriggerCollectorService`) has no
legacy view any more, and every reader of one reads the ability.

**What the scan found.** A temporary reflection scan of every card class, its own abilities and the abilities its
effects grant (3,486 combat abilities in 3,078 card classes, 201 granted): 403 intervening "if"s, 31 event conditions,
254 filters on the creature, 148 "one or more" (62 "blocks" once each combat, 74 ally combat damage, 5 "combat damage to
you", the rest attack-or-block abilities), 63 "each blocker", and 13 limits: "attacks for the first time each turn" on
seven attack abilities (Aurelia, the Warleader, Godo, Bandit Warlord, Fear of Missing Out, Scourge of the Throne), "triggers
only once each turn" on Akki Battle Squad, Ms. Marvel, Elastic Ally and Raphael, Tag Team Tough, "do this only once each
turn" on Calix, Guided by Fate and Tidus, Yuna's Guardian, and "triggers only once" on Ruby Collector. No ability mixes
a combat trigger with another family's; 48 are "attacks or blocks"; 18 registrations are `INDEPENDENT`. Off the
battlefield: 15 in a graveyard, 3 in the command zone, 2 in exile. No card authored `TriggerMode.PER_BLOCKER` or
`ONCE_PER_BLOCK` (only the legacy view made them) or `BatchedCombatDamageToYouTriggerEffect`; 18 author an
`AllyCombatDamageTriggerEffect` event binding (`boundToDealer`, `opponentsDealtDamage`, Pirated Copy's same-name one).
179 cards write one Oracle combat ability as several `addTrigger` calls (Godo's untap and extra combat, the Swords'
two effects, Agate-Blade Assassin's drain), which the bodies' bundling keeps one stack object.

**The model.** `CombatTrigger.collectedAsAbility()` is true: `TriggeredAbility.legacyEffects()`, the `getLegacy…`
getters and `TemporaryGlobalTriggeredAbility.legacyEffect()` throw for a combat ability, the card's slot index leaves
combat abilities out of its legacy maps, and `CombatWatch.effectsOf` / `registrationsOf` throw. The legacy view's
combat branch (`CombatView`) is gone, with `TriggerMode.PER_BLOCKER`, `TriggerMode.ONCE_PER_BLOCK`,
`BatchedCombatDamageToYouTriggerEffect` and `AllyCombatDamageTriggerEffect.withTriggerOptions` / `oneOrMoreDealers`.
A multiplicity no combat collector reads is rejected as the `TriggeredAbility` is built (new
`requireReadableMultiplicity`: "each source" off a damage trigger, "each blocker" off "becomes blocked", "one or more"
on a combat ability without a "blocks", "becomes blocked", ally combat damage or "combat damage to you" condition),
where the legacy view rejected it as the card was indexed.

**The engine.**

- New `CombatAbilityReader` (`service/trigger`, static): finds the abilities a `CombatWatch` reads (printed by watch key
  and zone, granted until end of turn or combat and until the permanent leaves, by a static ability through new
  `GrantedTriggeredAbilitySupport.grantedWatchedAbilities`, a graveyard card's own and perpetual ones, a registered
  one's `ability()`), and reads each effect as a `CombatEffect`: the effect with the intervening "if" around it (a
  `ConditionalEffect`, CR 603.4) and the event condition inside that (`ConditionalEffect.atTriggerTime`, CR 603.2), as
  the bodies judged them, plus the ability, its filter on the creature (the one the watch key drops), its limit (judged
  and marked by `TriggeredAbility.limitKey()` on the source, `limitReached` / `markLimit`) and its multiplicity. On an
  authored `AllyCombatDamageTriggerEffect` the intervening "if" goes inside the record. `allyCombatDamage` reads the ally
  combat damage abilities: the filter and "one or more" from the ability, the event bindings from an authored record.
  `effects(abilities, trigger[, filterMatches])` and `registrations` serve the collectors that read no limit or
  multiplicity (all but the attack, "whenever you attack", block, becomes-blocked, ally combat damage and "combat
  damage to you" bodies) and throw on one; `rawEffects` serves the code that only inspects a combat ability's effects
  (bushido's static bonus, the AI's threat estimates).
- Every body judges the filter itself, against the creature and in the filter context it judged the
  `TriggeringPermanentConditionalEffect` in (the attacker, the blocker, the blocked attacker, the damage dealer); the
  bodies that read "one or more" and "each blocker" read the multiplicity (the block body's once-per-block set, the
  becomes-blocked bodies' per-blocker split, the ally and "to you" damage bodies' batched sets) where they read
  `ONCE_PER_BLOCK`, `PER_BLOCKER`, the ally record's `oncePerDamageStep` and `BatchedCombatDamageToYouTriggerEffect`;
  the limits are read from the ability (the attack body's "first time each turn" by the attack count and marker, the
  "whenever you attack" body's unkeyed once-each-turn and once-only marks, the ally damage body's keyed limit and "do this
  only once each turn" marked on acceptance), where the bodies unwrapped `OncePerTurnTriggerEffect` /
  `OnceOnlyTriggerEffect`. Authored wrappers (Desert Were-Worm's, Mirror Shield's, the Senu and Berserk Murlodont filters)
  are still judged as before.
- The bodies keep their routing by effect kind and their bundling: the printed abilities of one watch that aren't
  `INDEPENDENT` go on the stack as one object, a granted ability as one of its own.

**Behaviour changes.**

- A granted attack ability with a filter (the Young Hero Role's "if its toughness is 3 or less") is its own stack object
  (CR 603.3), where the filter wrapper kept it from being split off the creature's printed attack abilities.
- Nothing else is known to change: each body judges the filter, limit and multiplicity in the context and at the point
  it judged the legacy wrappers, and the scan found no filtered combat ability with an intervening "if" that a body
  judged differently.

**Kept, with their reason.**

- The intervening "if" and the event condition still reach the bodies as `ConditionalEffect`s: the attack bodies judge
  them in contexts of their own (`MinimumAttackers` with the attacker count, `AttackedTargetMatches` with the attacked
  player, the battalion and pack-tactics conditions unwrapped as the event's), so a reader-side check would need each
  body's context. Step 15's event-reading effects retire that routing; then the `interveningIf` / `triggerTimeOnly`
  flags can go.
- Grouping by ability: the bodies still bundle a permanent's printed abilities of one watch into one stack object (and
  read `TriggerMode.INDEPENDENT`), because 179 cards write one Oracle ability as several `addTrigger` calls and others
  (Momentum Rumbler's two "whenever this creature attacks, if …") are two Oracle abilities that bundling merges. A later run merges the
  former by script, checks the latter against the Oracle text, and then groups by ability (CR 603.3).
- `AllyCombatDamageTriggerEffect` stays for the 18 authored event bindings (step 15).

**For 16g onward.**

- The other families (damage, counters, targets, library, tap, life, sagas, misc, zone changes), as 16a planned; each
  can reuse `CombatAbilityReader`'s shape (the reader gives the filter, limit and multiplicity, the bodies judge them
  in their own contexts).
- Combat's grouping by ability, above; the "when you cast this spell" merges (16d).

**Tests changed.**

- `CombatTriggerTest`: the legacy-view tests (the filter inside the limit, the ally record, the trigger modes and the
  batched wrapper, the granted and registered legacy forms) became reader tests: no legacy view, the reader's filter,
  limit and intervening "if", the event condition inside the intervening "if", the ally reading and an authored
  record's identity, the multiplicity, a multiplicity rejected as the ability is built, the strict reader rejecting a
  filter, limit or multiplicity, attack-or-block read by both watches, granted and registered abilities.
  `aRegistrationKeepsItsTriggerMode` uses `INDEPENDENT` (the deleted modes).
- `CombatWatchTest`: reads through the reader. `DamageTriggerTest`: "each source" on an attack trigger is rejected as
  the ability is built. `TriggerOptionsTest`: its legacy-view example uses a damage trigger.
  `CombatDamageServiceTest#grantedDamageTriggerSurvivesItsSourceDyingInCombat` stubs `grantedWatchedAbilities`.

**Test runs.**

- Card: the test class of every card with a combat ability (by the scan), and every card test that names one without a
  test of its own (3,067 classes, 24,321 tests): all passing.
- Application: the `service.trigger` and `model.trigger` packages and the combat-service, triggered-ability,
  card-abilities, freeze, simulation-copy, granted-ability, pipeline, legacy-binding and planechase classes (963 tests):
  64 failures, all failing at HEAD (`CombatDamageServiceTest` 40, `DamageTriggerCollectorServiceTest` 6,
  `DeathTriggerCollectorServiceTest` 6, `CombatServiceTest` 5 (`processEndOfCombatSacrifices`, checked at HEAD in this
  run), `SpellCastTriggerCollectorServiceTest` 2, `LandTapTriggerCollectorServiceTest` 2, `PlanechaseServiceTest` 2,
  `SimulationCopyCompletenessTest` 1).
- AI: the whole module (1,620 tests), with the same 10 failures as before (two each in the four AI decision engine
  tests and `TargetPolarityGuardTest`).
- The full suite was not run here.

#### Run 16g: damage reads the ability

Step 13's family 4, next in 16a's order after combat (the plan left 16g unscoped; the run took the next family, as
16f's findings listed them): every "whenever [a source] deals damage / [a permanent or player] is dealt damage /
damage to you is prevented" ability (a `DamageTrigger`, collected by `TriggerCollectionService`, `CombatDamageService`,
`DamageSupport` and the damage handlers of `DamageTriggerCollectorService` and `MiscTriggerCollectorService`) has no
legacy view any more, and every reader of one reads the ability.

**What the scan found.** A temporary reflection scan of every card class, its own abilities, the abilities its effects
grant and the cards it makes (279 damage abilities in 288 card classes, 17 only granted): 11 intervening "if"s (Aegar,
Angel of Deliverance, Crude Abattoir, Farsight Mask, Fear of Burning Alive, Innocent Bystander, Kiyomaro, Molten
Lavamancer, Pain Magnification, Phyrexian Totem, Spinneret and Spiderling), no event condition, 14 filters (the Pirate
and Hero "one or more" abilities of Breeches, Malcolm, Francisco and The Thing; Briarbridge Patrol's "to one or more
creatures"; Donna Noble and the two Wrathful cards' "a permanent you control is dealt damage"; Hooded Blightfang and
Vraska, Swarm's Eminence's deathtouch dealers; Rite of Passage, Sonic and The Sensational She-Hulk's "a creature you
control is dealt damage"; Ohabi Caleria's Archers), 5 limits (Hercules and Stick "triggers only once each turn", Molten
Lavamancer "triggers only once each turn" with its intervening "if", She-Hulk "do this only once each turn", Stensia
"once each turn for each creature" over three conditions), and one "each source" (Nested Ghoul). No granted damage
ability has an option or a filter; no ability mixes a damage trigger with another family's; no registration has a
`TriggerMode`. Off the battlefield: three in a graveyard (Asgardian Inspiration, Bloodfeather Phoenix, Chandra's
Phoenix) and one plane (Stensia). The only authored wrapper is Zagras's `TriggeringPermanentConditionalEffect
.combatDamageOnly` (run 8 kept it).

**The model.** `DamageTrigger.collectedAsAbility()` is true: `TriggeredAbility.legacyEffects()`, the `getLegacy…`
getters and `DamageWatch.effectsOf` / `registrationsOf` throw for a damage ability, and the card's slot index leaves
damage abilities out of its legacy maps. The legacy view's damage branch (`DamageView`) is gone, with
`PerDamageSourceTriggerEffect` (only the legacy view built it). `TriggeredAbility` rejects "one or more" on a damage
condition as it is built (no damage collector reads it; the "one or more" damage events are events of their own); damage
conditions of one ability may now have different filters, since each watch reads its own condition's.

**The engine.**

- New `WatchedAbilities` (`service/trigger`, static): the generic finders `CombatAbilityReader` had (`printed` by watch
  key and zone, `temporary`, `persistent`, `withWatchedTrigger`, `rawEffects`), moved so both family readers share them
  (every caller rewritten by a one-off substitution).
- New `DamageAbilityReader` (`service/trigger`, static), `CombatAbilityReader`'s shape: each effect as a `DamageEffect`
  with the intervening "if" around it (a `ConditionalEffect`, CR 603.4) and the event condition inside that (CR 603.2),
  plus the ability, the watched condition's filter, the limit (`limitReached` / `markLimit` by `limitKey()` on the
  source; for `ONCE_EACH_TURN_FOR_EACH_CREATURE` by the source and the creature, in
  `GameData.oncePerCreatureTriggersFiredThisTurn`) and "each source". The strict `effects` / `printedEffects` /
  `temporaryEffects` / `persistentEffects` / `registrations` serve the bodies whose abilities have no filter, limit or
  multiplicity and throw on one; the `filterMatches` overloads judge a filter.
- Every body judges the filter itself, against the permanent and in the filter context the
  `TriggeringPermanentConditionalEffect` handler judged it in (the watcher as the ability's source): the source for
  "a source you control deals damage to a player", the dealer for "to a planeswalker" and "to a creature", each dealer
  for "one or more creatures you control" (creatures only unless filtered, for opponents), the damaged permanent for
  "a permanent you control / a creature is dealt damage" (and for registered abilities), the damaged permanents' last
  snapshots for "this deals damage to one or more creatures". The handlers keyed on the wrapper were re-keyed to the
  effect they enqueue (`CardEffect` defaults for the "source to a player", "one or more" and "to a planeswalker"
  watches, `DealDamageToPlayersEffect` for Donna Noble, `MayPayManaEffect` for Ohabi Caleria) and lost their filter
  checks; Zagras's authored wrapper keeps its handler.
- The limits are read from the ability where the bodies unwrapped `OncePerTurnTriggerEffect`,
  `OncePerTurnPerCreatureTriggerEffect` and `PerDamageSourceTriggerEffect`: Stick's and Molten Lavamancer's
  `TriggerMatchContext` carries "triggers only once each turn" (as the wrapper did) and She-Hulk's "do this only once
  each turn" (the handler marks the stack entry, or wraps the target choice's effect as before); Nested Ghoul's "each
  source" splits the combat dealt-damage triggers per source; Stensia's limit is marked for the plane and the creature.
- The self-deals-damage snapshots (`PendingSourceDamage.getSelfDealsDamageAbilities()`,
  `CombatDamageState.selfDealsDamageAbilities`, copied for simulations as before) hold abilities instead of effects, so
  Briarbridge Patrol's filter travels with them; the "one or more creatures deal damage to an opponent" snapshot
  (`AllyDamageTriggerSnapshot`) likewise. `dispatchWatch` has a damage branch; the graveyard damage readers read
  `getEffectiveGraveyardTriggeredAbilities`.

**Behaviour changes.**

- Molten Lavamancer: its intervening "if" ("if it's your turn") stays on the stack entry and is checked again as the
  ability resolves (CR 603.4), where the once-per-turn handler stripped it. It can't become false while the ability is
  on the stack, so no test can observe it.
- Nothing else is known to change: each body judges the filter, limit and "each source" in the context and at the point
  the handlers judged the wrappers.

**Kept, with their reason.**

- The intervening "if" and the event condition still reach the handlers as `ConditionalEffect`s (several judge them
  with the damage amount); step 15 retires that routing.
- Hercules's "triggers only once each turn" doesn't cap copies from effects that make abilities trigger additional times
  on the noncombat path, as before: the old body matched the unwrapped effect. Making it consistent with Stick's is a
  rules question for a run with a test.
- Zagras's `TriggeringPermanentConditionalEffect.combatDamageOnly` (run 8's reason) and the effect-text
  `ConditionalEffect.unless`s.
- The "mixed readings" step 13 run 4 listed (`creatureYouControlDealsDamageToCreature()` effects that say whether the
  dealer is this creature, equipped creature or any creature, …): event-reading effects for step 15.

**For 16h onward.**

- The other families (counters, targets, library, tap, life, sagas, misc, zone changes), as 16a planned; each can reuse
  `WatchedAbilities` and a reader of `DamageAbilityReader`'s shape.
- Combat's grouping by ability and the "when you cast this spell" merges (16d, 16f).

**Tests changed.**

- `DamageTriggerTest`: the legacy-view tests became reader tests: no legacy view (getters and the watch throw), the
  reader's filter, limit and intervening "if", "triggers only once each turn" marked by the ability, "each source" and
  "each creature", "one or more" rejected as the ability is built, the strict reader, each condition read with its own
  filter, granted abilities.
- `DamageWatchTest`: reads through the reader. `TriggerOptionsTest`: its legacy-view example uses a death trigger.
- `DamageTriggerCollectorServiceTest`: the handlers that judged a wrapper's filter or limit get the plain effect (the
  collector judges those now): the two "skips a non-matching permanent / creature" tests are gone (the filter isn't the
  handler's; the card tests of Wrathful Raptors, Rite of Passage and the rest cover it), She-Hulk's test asserts the
  stack entry's "mark on acceptance", and two new Molten Lavamancer tests replace the two in
  `MiscTriggerCollectorServiceTest` (its once-per-turn handler is gone).
- `CombatTriggerTest`, `CombatWatchTest`: the finders are `WatchedAbilities`'.

**Test runs.**

- Card: the test class of every card with a damage ability (by the scan, back faces and made cards included), and every
  card test that constructs one (446 classes, 3,883 tests): all passing.
- Application: the `service.trigger` and `model.trigger` packages and the combat-damage, combat, turn-cleanup,
  planechase, freeze, simulation-copy, triggered-ability, granted-ability, pipeline, card-abilities and legacy-binding
  classes (975 tests): 74 failures after the fixes, all failing at HEAD: `CombatDamageServiceTest` 40,
  `TurnCleanupServiceTest` 10, `DeathTriggerCollectorServiceTest` 6, `CombatServiceTest` 5,
  `DamageTriggerCollectorServiceTest` 5 (the same names fail at `2f2aa8b384`, checked in a worktree; HEAD's sixth,
  She-Hulk's once-per-turn wrapper test, was rewritten and passes), `SpellCastTriggerCollectorServiceTest` 2,
  `LandTapTriggerCollectorServiceTest` 2, `PlanechaseServiceTest` 2, `SimulationCopyCompletenessTest` 1, and
  `MiscTriggerCollectorServiceTest`'s none. The first run had two more of this run's own: She-Hulk's rewritten test
  asserted a stack entry where a targeted may opens a target choice, and Zagras's handler judged "combat damage only"
  before the creature check, leaving a stub unused; both fixed.
- Engine: the whole module, with one failure, `GameMutationBoundaryRatchetTest`
  (`GameService.completeTurnFaceUpAfterCostSacrificeAnswers`), which fails at `2f2aa8b384` too (checked in a worktree).
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 16h: counters read the ability

Step 13's family 5, next in 16a's order after damage, as 16g's findings listed the families left: every "whenever
counters are put on or removed from [a permanent]", "you put counters", "becomes renowned / monstrous", "evolves",
"trains", "a Class reaches level N", "you get {E}" and "you proliferate" ability (a `CounterTrigger`, collected by
`PermanentCounterSupport`, `TriggerCollectionService` and the train, evolve and Class level-up handlers) has no legacy
view any more, and every reader of one reads the ability.

**What the scan found.** A temporary reflection scan of every card class, its own abilities, the abilities its effects
grant and the cards it makes (127 counter abilities in 116 card classes, one granted: Danny Pink's): 11 intervening
"if"s (the four vanishing sacrifices and Veiling Oddity's and Riftmarked Knight's suspend `NotCondition`s, Brotherhood
Scribe's and Contagion Dispenser's `ControllerTurn`, Hollowmurk Siege's chosen mode, Moss-Pit Skeleton's and Ichor
Aberration's zone), 11 event conditions (the nine plan-counter cards, Midnight Clock and Shang-Chi's
`SourceCounterThreshold`, as 16a made them), two filters (Omarthis's colorless, The Great Goblin's Goblin, Orc or
Army), 14 limits (eleven "triggers only once each turn", Danny Pink's granted one among them, Earth Kingdom General's
and Terrasymbiosis's "do this only once each turn", Stalwart Successor's once for each creature), seven abilities of a narrower event
(Mikey & Leo and The Great Goblin "you put counters on a creature you control", Exemplar of Light and Blaster "you put
+1/+1 counters on this", Fathom Mage and Bloodcrazed Hoplite once for each +1/+1 counter, Watcher of Hours "you
remove a time counter"). Every multiplicity is `EACH`, no ability mixes a counter trigger with another family's, and
no registration has a `TriggerMode`. Off the battlefield: 10 in exile, Moss-Pit Skeleton and Voidwing Hybrid in a graveyard and
Ichor Aberration on the battlefield and in a graveyard. Roiling Horror, Polukranos, Wildfire Cerberus, Shipbreaker Kraken,
Auntie Ool, Cool but Rude, Sharktocrab, Obelisk Spider, Regenerations Restored and Kros (three) write one Oracle
ability as several `addTrigger` calls, which the collectors' bundling keeps one stack object where it did.

**The model.** `CounterTrigger.collectedAsAbility()` is true: `TriggeredAbility.legacyEffects()`, the `getLegacy…`
getters and `CounterWatch.effectsOf` / `registrationsOf` throw for a counter ability, and the card's slot index leaves
counter abilities out of its legacy maps. The legacy view's counter branch (`CounterView`) is gone, with
`YouPutCounterOnControlledCreatureTriggerEffect`, `YouRemoveTimeCounterFromExiledCardTriggerEffect` and
`TriggerMode.PER_COUNTER` (only the legacy view built them). `TriggeredAbility` rejects "one or more" on a counter
condition as it is built (no counter collector reads a multiplicity; "each counter" is an event of its own).
`OncePerTurnPerCreatureTriggerEffect` stays: the legacy view still gives its global form to the tap collector.

**The engine.**

- New `CounterAbilityReader` (`service/trigger`, static), `DamageAbilityReader`'s shape on `WatchedAbilities`' finders:
  each effect as a `CounterEffect` with the intervening "if" around it (a `ConditionalEffect`, CR 603.4) and the event
  condition inside that (CR 603.2), plus the ability, the watched condition's filter (its `qualifier`), its own event
  (`byController()` for "you put" / "you remove", `eachCounter()` for once for each +1/+1 counter) and the limit
  (`limitReached` / `markLimit` by `limitKey()` on the source; for `ONCE_EACH_TURN_FOR_EACH_CREATURE` by the source and
  the creature). The strict `printedEffects` / `effects` / `registrations` serve the collectors that judge none of
  them and throw on one; `requireNoFilter`, `requireWatchedEvent` and `requireNoLimit` serve the collectors that judge
  some.
- The per-creature limit moved from `DamageEffect` to `TriggeredAbilityStackService.limitReached` / `markLimit` with a
  creature, which both readers use.
- `PermanentCounterSupport`'s collectors judge the ability's parts where they unwrapped the wrappers: "counters put on
  a creature you control" the "you put", the filter, the limit (Stalwart Successor's on the turn's first placement on
  the creature) and then the intervening "if", marking only for an ability that triggers; "+1/+1 counters put on this"
  "each counter" and "you put" from the event; "you put +1/+1 counters on another creature" the filter (Omarthis); the
  Hero, Human, another-creature and controlled-permanent collectors the limit (`WithinLimits`, "do this only once each
  turn" marked on acceptance as before); "counters put on this" the plan-counter thresholds as before and Danny Pink's
  granted ability through the reader.
- `TriggerCollectionService`: `dispatchWatch` has a counter branch (`dispatchCounterWatch`) for the energy,
  proliferate, counters-removed and time, lore and "counters on a permanent or player" watches, which judges each
  ability's limit around its effects' dispatch (Contagion Dispenser) and keeps the dispatch path's text change and
  ability-loss checks; the renowned collector judges the filter; the graveyard +1/+1 and proliferate collectors read
  `getEffectiveGraveyardTriggeredAbilities`; the exiled time-counter collector judges "you remove" (Watcher of Hours)
  where the `YouRemoveTimeCounterFromExiledCardTriggerEffect` handler did.
- The monstrous and exiled time-counter collectors hand their handlers every effect they read
  (`TriggerContext.SelfBecomesMonstrous.effects`, `TimeCounterRemovedFromExile.effects`) and dispatch the first one,
  where the handlers re-read the card and compared the first effect by identity (a wrapper the reader builds has no
  stable identity).
- Code that only inspects which effects a card has reads the raw effects: `PermanentRemovalService`'s training
  look-back (`WatchedAbilities.rawEffects`). The train, evolve and Class level-up handlers read the strict reader.

**Behaviour changes.**

- "Triggers only once each turn" on a counter ability is keyed by the ability (`keyedOncePerTurnTriggersFiredThisTurn`),
  where the Hero, Human, another-creature, controlled-permanent, "+1/+1 counters put on this" and "counters put on a
  creature you control" collectors checked and set the source's unkeyed mark, which every other once-each-turn wrapper
  of the source shared. No card of the family has a second limited ability (checked), so no test can observe it.
- A limited ability with several effects is judged once for the ability: the "counters put on this" and dispatch
  collectors marked the limit as each effect was taken, so a second effect would have been skipped. No card has one.
- Nothing else is known to change: each collector judges the filter, event and limit in the context and at the point it
  judged the wrappers.

**Kept, with their reason.**

- The intervening "if" and the event condition still reach the collectors and handlers as `ConditionalEffect`s, and
  several collectors strip the intervening "if" as the ability triggers (the "counters put on a creature you control"
  and "time counters removed from this" ones), so it isn't checked again as it resolves, as before; the "you put
  counters" and graveyard collectors check it only as it resolves. Step 15's event-reading effects retire that routing.
- The bundling of one card's abilities into one stack object (the monstrous, exiled time-counter and several
  `PermanentCounterSupport` collectors), for the split abilities above; merging them by script and grouping by ability
  (CR 603.3) is a later run's, as for combat.
- The collectors that don't mark "do this only once each turn" on acceptance (Human, "counters put on this" and
  "creature you control", "+1/+1 counters put on this") throw on it rather than reading it: no card writes one there.

**For 16i onward.**

- The other families (targets, library, tap, life, sagas, misc, zone changes), as 16a planned; each can reuse
  `WatchedAbilities` and a reader of `CounterAbilityReader`'s shape.
- Combat's and counters' grouping by ability, and the "when you cast this spell" merges (16d, 16f).

**Tests changed.**

- `CounterTriggerTest`: the legacy-view tests became reader tests: no legacy view (getters, temporary abilities and the
  watch throw), the reader's "you put", filter, limit and intervening "if", the event condition inside the intervening
  "if", "triggers only once each turn" marked by the ability (not the unkeyed mark), the other limits, "each counter"
  and "you remove", the strict reader, a multiplicity rejected as the ability is built, an ability of two zones read
  by both watches, granted abilities. `aRegistrationKeepsItsTriggerMode` uses `INDEPENDENT` (`PER_COUNTER` is gone).
- `CounterWatchTest`: reads through the reader.

**Test runs.**

- Card: the test class of every card with a counter ability (by the scan), and every card test that constructs one
  (173 classes, 1,431 tests): all passing.
- Application: the `service.trigger` and `model.trigger` packages and the combat-damage, combat, turn-cleanup,
  planechase, removal, freeze, simulation-copy, triggered-ability, granted-ability, pipeline, card-abilities and
  legacy-binding classes (1,036 tests): 119 failures, all failing at HEAD with the counts 16e to 16g recorded:
  `PermanentRemovalServiceTest` 46, `CombatDamageServiceTest` 40, `TurnCleanupServiceTest` 10,
  `DeathTriggerCollectorServiceTest` 6, `CombatServiceTest` 5, `DamageTriggerCollectorServiceTest` 5,
  `SpellCastTriggerCollectorServiceTest` 2, `LandTapTriggerCollectorServiceTest` 2, `PlanechaseServiceTest` 2,
  `SimulationCopyCompletenessTest` 1. `CounterTriggerTest`, `CounterWatchTest`, `DamageTriggerTest` and
  `MiscTriggerCollectorServiceTest` pass.
- Engine: the whole module, with the one failure at HEAD, `GameMutationBoundaryRatchetTest`
  (`GameService.completeTurnFaceUpAfterCostSacrificeAnswers`).
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 16i: becomes the target reads the ability

Step 13's family 6, next in 16a's order after counters, as 16h's findings listed the families left: every "whenever
[this, a creature or permanent you control, another, any creature, you] becomes the target of [a spell, an Aura spell,
an instant or sorcery spell, a spell or ability, an opponent's or your spell or ability, an activated or backup
ability]" and "whenever a player chooses targets" ability (a `TargetTrigger`, collected by the becomes-the-target and
target-choice collectors of `TriggerCollectionService`) has no legacy view any more, and every reader of one reads the
ability.

**What the scan found.** A temporary reflection scan of every card class (32,371), its own abilities and the
abilities its effects grant (345 target abilities in 331 card classes: 272 printed, 73 granted, 63 of them ward grants): one intervening
"if" (Catch of the Day's chosen-mode ward), no event condition, seven filters (Thunderbreak Regent, Scalelord Reckoner
and Diffusion Sliver on the ally creature watch, Swarm Shambler, Silverfur Partisan, Daru Spiritualist, Fey Steed),
14 limits, all "triggers only once each turn" (the eleven valiant cards' "for the first time each turn", Professor
Hojo, Loki, God of Mischief, Niambi's granted one), and 14 abilities of a narrower event (the eleven valiant cards,
Stormchaser Drake and Illuminator Virtuoso "a spell you control", Loki "an ability you control"). Every ability has one
effect, every multiplicity is `EACH`, no ability mixes a target trigger with another family's, no registration has a
`TriggerMode`, and every ability functions from the battlefield.

**The model.** `TargetTrigger.collectedAsAbility()` is true: `TriggeredAbility.legacyEffects()`, the `getLegacy…`
getters and `TargetWatch.effectsOf` / `registrationsOf` throw for a target ability, and the card's slot index leaves
target abilities out of its legacy maps. The legacy view's target branch (`TargetView`) is gone, with
`TriggeringSpellControllerConditionalEffect`, `AbilityTargetChoiceTriggerEffect` and its `TargetChoiceTriggerEffect`
interface (only the legacy view built them). `TriggeredAbility` rejects "one or more" on a target condition as it is
built (no target collector reads a multiplicity; "one or more creatures you control become the target of an
activated ability" is an event of its own).

**The engine.**

- New `TargetAbilityReader` (`service/trigger`, static), `CounterAbilityReader`'s shape on `WatchedAbilities`' finders:
  each effect as a `TargetEffect` with the intervening "if" around it (a `ConditionalEffect`, CR 603.4) and the event
  condition inside that (CR 603.2), plus the ability, the watched condition's filter (its `qualifier`), its own event
  (`byController()` for "a spell [or ability] you control" and "an ability you control") and the limit
  (`limitReached` / `markLimit` by `limitKey()` on the source). It throws on a multiplicity, on "do this only once each
  turn" and "once each turn for each creature" (no target collector reads them), and on two conditions of one ability
  that one watch would read differently. The strict `printedEffects` / `effects` / `grantedToPermanentEffects` serve
  the collectors that judge none of them and throw on one.
- The collectors judge the ability's parts where they unwrapped the wrappers: the "this becomes the target of a
  spell" and "of a spell or ability" collectors the "you control" event against the targeting spell's controller and
  the latter the limit (judged once for each ability, marked as the ability is put on the stack); the target-choice
  collector Loki's "an ability you control" and limit; the activated-ability collector Professor Hojo's limit (judged
  once for the ability); the "you or a permanent you control", ally creature, ally creature spell-only and
  instant-or-sorcery collectors the filter against the targeted permanent from the watcher's point of view, and the
  any-creature collector Daru Spiritualist's in the context it judged the wrapper in. Every other collector, the ward
  collector included, reads the strict effects.
- Abilities granted to the permanent are read by `WatchedAbilities.temporary` / `persistent` (where the collectors
  read `getLegacyTemporaryTriggerEffects` / `getLegacyPersistentTriggerEffects`) and those a static ability grants by
  `GrantedTriggeredAbilitySupport.grantedWatchedAbilities` (where they read `grantedTriggerEffects`), the ward checks
  `hasWardAbility` and `hasGrantedCounterUnlessTrigger` included.

**Behaviour changes.**

- A limited ability with several effects is judged once for the ability (the activated-ability collector took the
  limit effect by effect). No card has one.
- Nothing else is known to change: each collector judges the filter, event and limit in the context and at the point
  it judged the wrappers, and the strict reads give the effects the legacy view gave for abilities without them.

**Kept, with their reason.**

- The intervening "if" and the event condition still reach the collectors as `ConditionalEffect`s: the ward collector
  checks Catch of the Day's as the ability triggers and strips it, so it isn't checked again as it resolves (run 10's
  note; the chosen mode can't change). Step 15's event-reading effects retire that routing.
- The collectors' grouping (the "becomes the target of a spell" collector's split by targeting and triggering-spell
  effects, the opponent-spell-only collector's printed abilities as one stack object): every target ability has one
  effect, so no card writes one Oracle ability as several; grouping by ability (CR 603.3) is a later run's, as for
  combat and counters.
- Skophos Maze-Warden's authored `TriggeringAbilityFromNamedLandConditionalEffect` (run 10's reason), judged by its
  own `resolveForBecomesTargetOfSpellOrAbility`, and the Aura/Equipment readings of the `THIS` forms (step 13 run 6).

**For 16j onward.**

- The other families (library, tap, life, sagas, misc, zone changes), as 16a planned; each can reuse
  `WatchedAbilities` and a reader of `TargetAbilityReader`'s shape.
- Combat's and counters' grouping by ability, and the "when you cast this spell" merges (16d, 16f).

**Tests changed.**

- `TargetTriggerTest`: the legacy-view tests became reader tests: no legacy view (getters, temporary abilities and the
  watch throw), the reader's "you control" event, filter, limit and intervening "if", the other narrowed events read by
  the watch of the event about anyone, the event condition inside the intervening "if", "triggers only once each turn"
  marked by the ability (not the unkeyed mark), the strict reader, "one or more" rejected as the ability is built and a
  limit or two readings the reader can't read rejected, granted abilities.
- `TargetWatchTest`: reads through the reader.

**Test runs.**

- Card: the test class of every card with a target ability (by the scan, granting cards included), Nowhere to Run,
  Makeshift Mannequin, High Troller, Psychic Battle, Grip of Chaos, Glyph Keeper, every card test that constructs one
  of them and every disguise and cloak test (620 classes, 5,741 tests): all passing.
- Application: the `service.trigger` and `model.trigger` packages and the freeze, simulation-copy, triggered-ability,
  granted-ability, pipeline, card-abilities, legacy-slot, legacy-binding, target-legality, counter-spell,
  counter-unless, spell-casting, activated-ability-execution, immutability and targeting-consistency classes (1,155
  tests): 79 failures, all failing at HEAD: `ActivatedAbilityExecutionServiceTest` 35, `TargetLegalityServiceTest` 14
  and `SpellCastingServiceTest` 10 (their mock wiring; with `CounterSpellEffectHandlerTest` 2 the same counts at
  `2b07533a41`, checked in this run), `DeathTriggerCollectorServiceTest` 6, `DamageTriggerCollectorServiceTest` 5,
  `SpellCastTriggerCollectorServiceTest` 2, `LandTapTriggerCollectorServiceTest` 2, and the known one each in
  `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and `CardEffectTargetingConsistencyTest`.
  `TargetTriggerTest` and `TargetWatchTest` pass.
- Engine: the whole module, with the one failure at HEAD, `GameMutationBoundaryRatchetTest`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 16j: library, draw and discard read the ability

Step 13's family 7, next in 16a's order after becomes the target, as 16i's findings listed the families left: every
"whenever [you, an opponent, enchanted player, a player] draws a card [the Nth card each turn, except the first in a draw
step, the first in a draw step]", "discards a card [of a kind, for the first time each turn]", "discards one or more
cards", "cycles a card", "when you cycle / discard this card", "scries", "surveils", "searches their library" and
"shuffles" ability (a `LibraryTrigger`, collected by `DrawService`, the discard, cycle, scry, surveil and discard-event
collectors of `TriggerCollectionService`, `LibrarySearchTriggerHelper` and `LibraryShuffleHelper`) has no legacy view
any more, and every reader of one reads the ability.

**What the scan found.** A temporary reflection scan of every card class (32,371), its own abilities, the abilities its
effects grant and the cards it makes (362 library abilities in 332 card classes: 358 printed, 4 granted: Diviner's Wand,
Sliv-Mizzet, Astrologian's Planisphere's "your third card each turn", Pure Intentions' temporary global one): every
ability has one effect, no registration has a `TriggerMode`, no ability mixes a library trigger with another family's,
and every multiplicity is `EACH`. 12 intervening "if"s (Aether Syphon's max speed, Wizard Class's and Cool but Rude's
levels, Starving Revenant, Human Torch, Starscream, Kederekt Parasite, Tataru Taru, Zurzoth, Ajani's Last Stand, Radiant
Smite, Blood Operative), 2 event conditions (Wedding Ring, Entropic Battlecruiser), 23 filters (the discard abilities: Waste
Not, Bone Miser, Surly Badgersaur, Urza, Mishra, Veronica, Doctor Doom, Anje Falkenrath, …), 60 counts (55 "your / their
Nth card each turn", Lady Octopus's "first or second", and the four "first time each turn": Valiant Rescuer, Veronica,
Rielle, Whispering Snitch), 10 limits (seven "triggers only once each turn", Emerald Collector's "only once",
Planetarium's "do this only once each turn"), and 17 abilities of a narrower event (12 "you cycle a card", Spiritual
Focus, Orcish Bowmasters, Xyris, Leela, Wiretapping). Hostile Investigator (an "you discard one or more" and an "an
opponent discards" condition) and Planetarium (scry and surveil) are the abilities two watches read. Off the battlefield:
75 "this card is cycled / discarded" abilities, Wolfbat, Dogged Detective and Blood Operative in a graveyard.

**The model.** `LibraryTrigger.collectedAsAbility()` is true: `TriggeredAbility.legacyEffects()`, the `getLegacy…`
getters and `LibraryWatch.effectsOf` / `registrationsOf` throw for a library ability, and the card's slot index leaves
library abilities out of its legacy maps. The legacy view's library branch (`legacyLibraryEffects()`) is gone, with
`CyclingTriggerEffect`, `OpponentCausedDiscardTriggerEffect`, `NthCardDrawTriggerEffect`,
`FirstOrSecondCardDrawTriggerEffect`, `ExceptFirstDrawStepTriggerEffect`, `FirstDrawStepDrawOnlyTriggerEffect`, their
`DrawTriggerEffect` interface, and `OncePerTurnTriggerEffect`'s `firstCycling` component and its `firstCycling`,
`firstNonlandDiscard`, `firstDiscardEvent` and `firstSurveil` factories and keys (only the legacy view built them).
`TriggeredAbility` rejects "one or more" on a library condition as it is built ("whenever you discard one or more cards"
is an event of its own).

**The engine.**

- New `LibraryAbilityReader` (`service/trigger`, static), `TargetAbilityReader`'s shape on `WatchedAbilities`' finders:
  each effect as a `LibraryEffect` with the intervening "if" around it (a `ConditionalEffect`, CR 603.4) and the event
  condition inside that (CR 603.2), plus the ability, the watched condition's filter on the discarded card (its
  `qualifier`), its own event (`event()`), its counts (the `nth` of every condition the watch reads: `counts()`,
  `countMatches(n)`, `firstTimeEachTurn()`) and the limit (`limitReached` / `markLimit` by `limitKey()` on the source,
  `marksLimitOnAcceptance()` for "do this only once each turn", CR 603.2h). It throws on a multiplicity, on "once each
  turn for each creature", on conditions one watch reads with a different event, player or filter, and on counted and
  uncounted conditions together. The strict `printedEffects` / `printedRegistrations` / `effects` serve the readers that
  judge none of them and throw on one; `requireNoFilter`, `requireWatchedEvent`, `requireNoCount`,
  `requireNoCountButFirst` and `requireNoLimit` serve the collectors that judge some.
- `DrawService`'s draw collectors judge the limit, the event (every draw, all but the first of a draw step, only that
  one) and the player's draw count this turn (CR 121.1) where they unwrapped the once-per-turn, once-only, Nth-draw and
  draw-step wrappers; granted abilities are read by `GrantedTriggeredAbilitySupport.grantedWatchedAbilities` (Astrologian's
  Planisphere's count with them). The graveyard, planar and enchanted-player collectors read no limit (and the
  enchanted-player one the strict effects).
- `TriggerCollectionService`: the "you discard a card" collector (`collectControllerDiscardAbilities`) judges the filter,
  the event (any discard, a cycling, an opponent-caused discard), the first cycling and first nonland discard of the turn
  and the limit; a cycling and an opponent-caused discard go to `DiscardTriggerCollectorService.collectCycling` /
  `collectOpponentCausedDiscard`, the old wrapper handlers' bodies, no longer keyed on a wrapper. The "an opponent
  discards" collector judges the filter and the limit. `dispatchWatch` has a library branch (`dispatchLibraryWatch`) for
  the "a player cycles", scry, surveil and discard-event watches, which judges a discard event's filter (one discarded
  card is enough), the first discard event or surveil of the turn and the limit; `dispatchSlotEffect` lost its
  discard-event and surveil branches. The self-cycle, self-discard and graveyard-surveil readers read the strict effects
  (the last through `getEffectiveGraveyardTriggeredAbilities`).
- The surveil and scry handlers read "do this only once each turn" from `TriggerMatchContext.markSourceOncePerTurnOnAcceptance`
  (Planetarium); the surveil handler keyed on `OncePerTurnTriggerEffect` is gone (nothing reached it).
- `LibrarySearchTriggerHelper`, `LibraryShuffleHelper`, the self-cycle target check of `TriggeredAbilityQueueService` and
  the seven `EnterBattlefieldOnDiscardEffect` replacement readers read the strict effects. `TriggeredAbilityQueueService`'s
  target-group lookup lost its `NthCardDrawTriggerEffect` branch (no card authors it since run 11).

**Behaviour changes.**

- Emerald Collector's "this ability triggers only once" is keyed by the ability (`keyedOnceOnlyTriggersFired`), where the
  wrapper marked the source's unkeyed once-only mark. It has no other once-only ability, so no test can observe it.
- The first discard event and first surveil of the turn (Rielle, Whispering Snitch) are judged by the count alone; the
  wrappers also marked a per-source key, which could never be set without the count failing afterwards.
- Nothing else is known to change: each collector judges the filter, event, count and limit in the context and at the
  point it judged the wrappers, and passes the same "triggers only once each turn" flag the wrapper gave the dispatch.

**Kept, with their reason.**

- The intervening "if" and the event condition still reach the collectors as `ConditionalEffect`s (the draw collectors
  check the intervening "if" as the ability triggers and strip Aether Syphon's max speed; the self-cycle and
  self-discard-by-opponent readers check it as it triggers): step 15 retires that routing.
- Urza's and Mishra's "triggers only once each turn" doesn't cap copies from effects that make abilities trigger
  additional times, as before: the old "you discard a card" collector dispatched the unwrapped effect. The draw
  collectors count their own copies (`countAdditionalDrawTriggeredAbilityTriggers`) uncapped, as before. Making them
  consistent with the other collectors is a rules question for a run with a test, as 16g said for Hercules.
- The collectors that don't mark "do this only once each turn" (the draw and "you discard a card" collectors) throw on
  it rather than reading it: no card writes one there.
- Pure Intentions' inverted temporary `opponentDiscardsCard()`, Psychogenic Probe's unchecked cause and
  `EnterBattlefieldOnDiscardEffect` in a trigger (step 13 run 7's notes); `SequenceEffect.controllerDrawCount` /
  `PutCountersOnSourceEffect.controllerDrawCount` (run 11's note); the first-draw reveal effects (step 15).

**For 16k onward.**

- The other families (tap, life, sagas, misc, zone changes), as 16a planned; each can reuse `WatchedAbilities` and a
  reader of `LibraryAbilityReader`'s shape. `OncePerTurnTriggerEffect`, `OnceOnlyTriggerEffect` and
  `TriggeringCardConditionalEffect` stay: other families' legacy views still build them.
- Combat's and counters' grouping by ability, and the "when you cast this spell" merges (16d, 16f).

**Tests changed.**

- `LibraryTriggerTest`: the legacy-view tests became reader tests: no legacy view (getters, temporary abilities and the
  watch throw), the reader's filter, limit, count and intervening "if", the event condition inside the intervening "if",
  the limits marked by the ability (not the unkeyed marks), "do this only once each turn" marked as the action is taken,
  "for the first time each turn" as a count of one, the narrowed events and "first or second", the strict reader, a
  multiplicity, per-creature limit or two readings rejected, an ability two watches read, granted abilities with their
  count.
- `LibraryWatchTest`: reads through the reader. `DiscardTriggerCollectorServiceTest`: the cycling handler tests call
  `collectCycling` (the "ordinary discard" case is the collector's now; a test checks no handler is keyed on the cycling
  event). `MiscTriggerCollectorServiceTest`: the surveil once-per-turn handler test is gone with the handler, and the
  Planetarium test passes "do this only once each turn" on the match; `ScryTriggerCollectorServiceTest` likewise.
  `DrawServiceTest`: the once-per-turn opponent-draw test authors the limit as an option (it authored the wrapper, which
  no card does since run 11) and asserts the ability's keyed mark.

**Test runs.**

- Card: the test class of every card with a library ability (by the scan, granting cards included), Skywriter Djinn's,
  and every card test that constructs one (699 classes, 5,923 tests): all passing.
- Application: the `service.trigger` and `model.trigger` packages and the draw, freeze, simulation-copy,
  triggered-ability, granted-ability, pipeline, card-abilities, legacy-slot, legacy-binding, shuffle-hand, deep-copy,
  trigger-options, immutability and targeting-consistency classes (826 tests): 22 failures, all failing at HEAD:
  `DeathTriggerCollectorServiceTest` 6, `DamageTriggerCollectorServiceTest` 5,
  `ShuffleHandIntoLibraryAndDrawEffectHandlerTest` 3, `SpellCastTriggerCollectorServiceTest` 2,
  `LandTapTriggerCollectorServiceTest` 2, `DrawServiceTest`'s additional-trigger copy, and the known one each in
  `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and `CardEffectTargetingConsistencyTest`. The first run had
  one more of this run's own, `DrawServiceTest`'s authored once-per-turn wrapper, rewritten above.
- Engine: the whole module, with the one failure at HEAD, `GameMutationBoundaryRatchetTest`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 16k: tap and untap read the ability

Step 13's family 8, next in 16a's order after library, draw and discard, as 16j's findings listed the families left:
every "whenever [this, a permanent you control, a permanent an opponent controls, a creature, enchanted permanent]
becomes tapped [for the first time during your turn]", "whenever you tap [this, a land, a creature, a nonland
permanent, an artifact token, a permanent] for mana", "whenever you tap a permanent an opponent controls", "tapped to
pay a teamwork cost", "becomes untapped" and "you untap permanents during your untap step" ability (a `TapTrigger`,
collected by the tap, untap and tap-for-mana collectors of `TriggerCollectionService` and their handlers) has no legacy
view any more, and every reader of one reads the ability.

**What the scan found.** A temporary reflection scan of every card class (31,682 with a `@CardRegistration`), its own
abilities and every ability reachable from its fields (granted, temporary global and token abilities: 214 tap
abilities in 207 card classes, 9 of them granted: Haunted One, Hangar Scrounger, Legolas's Quick Reflexes, Unctus,
Wildfire Awakener, Tale of Katara and Toph, Well Rested, Sasaya and Don't Move's registered one): every ability has one
effect, every registration is `TriggerMode.NORMAL`, and no ability mixes a tap trigger with another family's. 4
intervening "if"s (Barbflare Gremlin, Encumbered Reejerey, Interface Ace, Warchanter Skald), 1 event condition (The
Ninth Doctor), 3 "triggers only once each turn" (Interface Ace, Nanoform Sentinel, Sharae), 3 "one or more" (Deeproot
Pilgrimage, Rewrite History, Sharae), 2 first-time events (Captain America, Living Legend; Tale of Katara and Toph's
grant), 63 "this permanent becomes tapped" and 26 qualifiers. The only authored wrappers left are Well Rested's
`OncePerTurnTriggerEffect.perGrant` (kept, as run 12 said) and Scaretiller's `ChooseOneAtTriggerTimeEffect` (an
effect). The perpetual land grants (Ambassador of Evendo, Vigorous Farming) build their abilities in handlers.

**The model.** `TapTrigger.collectedAsAbility()` is true: `TriggeredAbility.legacyEffects()`, the `getLegacy…` getters
and `TapWatch.effectsOf` / `registrationsOf` throw for a tap ability, and the card's slot index leaves tap abilities out
of its legacy maps. The legacy view's tap branch (`tapView()`, `TapView`, `judgesTapFilter`, `batchesTaps`,
`legacyTapFilter`) is gone, with `OncePerTurnPerCreatureTriggerEffect`'s `global` component (only the tap view built
it). `TriggeredAbility` rejects "one or more" on a tap condition as it is built unless its collector batches the
permanents tapped together ("a permanent you control / an opponent controls becomes tapped", "you tap a permanent an
opponent controls").

**The engine.**

- New `TapAbilityReader` (`service/trigger`, static), `LibraryAbilityReader`'s shape on `WatchedAbilities`' finders:
  each effect as a `TapEffect` with the intervening "if" around it (a `ConditionalEffect`, **CR 603.4**, checked) and
  the event condition inside that (**CR 603.2**, checked), plus the ability, the filter on the tapped or untapped
  permanent ("this permanent" as `PermanentIsSourceCardPredicate` and the type of a narrowed trigger, and the
  qualifier, joined in a `PermanentAllOfPredicate`, as the legacy view joined them), `firstTime()`, `oneOrMore()` and
  the limit (`limitReached` / `markLimit` by `limitKey()` on the source). It throws on a trigger mode other than
  `NORMAL`, on a limit other than "triggers only once each turn", on a limit next to the first-time event, on a
  qualifier where no collector judges one, and on two conditions one watch reads with a different filter or event.
  The strict `printedEffects` / `printedRegistrations` / `effects` / `registrations` serve the readers that judge none
  of them and throw on one.
- The "a permanent you control becomes tapped" collector judges the filter, the first-time event (the creature's tap
  count this turn during its controller's turn, as before), "one or more" (the open tap batch, keyed by the read effect)
  and the limit, for printed, temporary, persistent and statically granted abilities; the "an opponent controls" and
  "you tap a permanent an opponent controls" collectors the filter, "one or more" and the limit; the "a permanent you
  control becomes untapped" collector the filter (and throws on a limit, which it never read); the "a permanent becomes
  untapped" collector the filter and the limit. The temporary global watchers (Don't Move) judge a tap ability's filter
  from the ability. The mana, land-tap, enchanted, teamwork, self-untap and untap-step readers, `dispatchWatch`'s new
  tap branch and the planar readers read the strict effects.
- The self-untap handler (`MiscTriggerCollectorService.handleSelfBecomesUntapped`) finds the first of the card's
  effects by equality: the reader wraps an effect in its options afresh on every read, where the legacy view's cached
  wrappers kept their identity (The Ninth Doctor's event condition).
- `PredicateEvaluationService`, `PotentialManaService`, `PermanentRemovalService`, `AiManaManager`, `AiTargetSelector`
  and `GameSimulator` only inspect which effects a tap ability has: they read `WatchedAbilities.rawEffects`.

**Behaviour changes.**

- The "a permanent you control becomes tapped" collector judges the limit before the modal and graveyard-target prompts
  and marks it when an ability goes to either prompt too, where the old one judged the wrapper only on the stack and
  target paths. No card has a limit on an ability with a modal or graveyard-target effect, so no test can observe it.
- Nothing else is known to change: each collector judges the filter, first-time event, "one or more" and limit in the
  context and at the point it judged the wrappers, and the inspectors find the same effect classes (the only wrapped
  ones were Barbflare Gremlin's and The Ninth Doctor's, around effects none of them looks for).

**Kept, with their reason.**

- The intervening "if" and the event condition still reach the collectors as `ConditionalEffect`s: step 15 retires that
  routing.
- The self-untap handler still puts every effect of a card's "this becomes untapped" abilities on one stack entry
  (Siren of the Silent Song wrote "each opponent discards a card, then mills a card" as two abilities); grouping by
  ability is a later cleanup, with combat's and counters'.
- Well Rested's authored `OncePerTurnTriggerEffect.perGrant`, Freyalise's Winds' two unfiltered abilities and Champions
  of the Shoal's enters-or-tapped pair, as run 12 left them. `TriggeringPermanentConditionalEffect` and
  `OncePerTurnPerCreatureTriggerEffect` stay: other families still read them.
- `TemporaryGlobalTriggeredAbility.legacyEffect()` stays for the life watchers.

**For 16l onward.**

- The other families (life, sagas, misc, zone changes), as 16a planned; each can reuse `WatchedAbilities` and a reader
  of `TapAbilityReader`'s shape. `OncePerTurnTriggerEffect` and `OnceOnlyTriggerEffect` stay: other families' legacy
  views still build them.
- Combat's, counters' and the self-untap handler's grouping by ability, and the "when you cast this spell" merges (16d,
  16f).

**Tests changed.**

- `TapTriggerTest`: the legacy-view tests became reader tests: no legacy view (the ability, the getters, a granted
  ability and the watch throw), "this permanent", the limit and the intervening "if", the event condition inside the
  intervening "if", "this permanent" joined with a filter, the first-time event with "this permanent" or the type, "one
  or more", the limit marked by the ability (not the unkeyed mark), one ability read by the "you control" and "opponent
  controls" watches, the strict reader, what the collectors can't read rejected (a filter where none is judged, "one or
  more" off the batching triggers as the ability is built, a limit next to the first-time event, "triggers only once",
  an authored trigger mode, two filters), granted abilities. `aRegistrationKeepsItsTriggerMode` is gone: the tap
  collectors read no trigger mode, and the reader rejects one.
- `TapWatchTest`: reads through the reader.

**Test runs.**

- Card: the test class of every card with a tap ability (by the scan, granting cards included), Ambassador of Evendo's
  and Vigorous Farming's, and every card test that constructs one of them (338 classes, 3,043 tests): all passing.
- Application: the `service.trigger` and `model.trigger` packages and the freeze, triggered-ability, card-ability,
  granted-ability, simulation-copy, architecture, targeting-consistency, untap-step, legacy-slot, mana-ability trigger
  deferral, permanent-removal, deep-copy, trigger-options and pipeline classes (912 tests): 76 failures, all failing at
  HEAD: `PermanentRemovalServiceTest` 46, `ManaAbilityTriggerDeferralTest` 11 and `UntapStepServiceTest` 1 (the same
  cases at `ff2bfc18a3`, run in a worktree with the main tree's `card-data-cache`), `DeathTriggerCollectorServiceTest`
  6, `DamageTriggerCollectorServiceTest` 5, `SpellCastTriggerCollectorServiceTest` 2, `LandTapTriggerCollectorServiceTest`
  2, and the known one each in `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and
  `CardEffectTargetingConsistencyTest`. `TapTriggerTest` and `TapWatchTest` pass.
- Engine: the whole module, with the one failure at HEAD, `GameMutationBoundaryRatchetTest`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 16l: life reads the ability

Step 13's family 9, next in 16a's order after tap and untap, as 16k's findings listed the families left: every
"whenever [you, an opponent, a player] gains life [for the first time each turn]", "loses life [for the first time each
turn, for the first time during each of their turns]" and "whenever you pay life" ability (a `LifeTrigger`, collected
by the life-gain, life-loss and life-payment collectors of `TriggerCollectionService`, the two graveyard life-gain
collectors and the temporary global watchers) has no legacy view any more, and every reader of one reads the ability.

**What the scan found.** A temporary reflection scan of every card class (32,373), its own abilities and every ability
reachable from its fields (111 life abilities in 106 card classes, 8 of them reachable: the static grants of
Field-Tested Frying Pan, Light of Promise and Sunbond, False Cure's temporary global one, Ajani, Strength of the Pride's
Pridemate token, the back faces Kefka, Court Mage and Valentin, Dean of the Vein), and Shadowheart's specialized ability,
built in its handler: every registration is `TriggerMode.NORMAL` and every multiplicity `EACH`. 10 intervening "if"s
(the "during your turn" ones of Vampire Scrivener, Kefka, Cat Collector, Wax-Wane Witness, Moonstone Harbinger,
Shadowheart; the Class levels of Gourmand's Talent, Cleric Class, Intermediate Chirography), 1 event condition (Wedding
Ring), 14 counts (Attended Healer, Cleric of Life's Bond, Vanguard Seraph, Leech Collector, Gourmand's Talent's two, Cat
Collector, Deathless Knight in the graveyard, Gonti's Machinations, Vengeful Warchief, Intermediate Chirography), 2
"triggers only once each turn" (Moonstone Harbinger, MACH-1), 2 "do this only once each turn" (Nykthos Paragon,
Emet-Selch), Valgavoth's first loss during their turn (two effects, the only ability with more than one). Three abilities
have two conditions: Wax-Wane Witness and Moonstone Harbinger (gain and loss), The Master of Lake-town (you and an
opponent lose life), and one mixes families: MACH-1 (enters and gain life).

**The model.** `LifeTrigger.collectedAsAbility()` is true: `TriggeredAbility.legacyEffects()`, the `getLegacy…` getters
and `LifeWatch.effectsOf` / `registrationsOf` throw for a life ability, and the card's slot index leaves life abilities
out of its legacy maps. The legacy view's life branches (`lifeView()`, `LifeView`, `legacyFirstOpponentLifeLossEffects`)
are gone, with `FirstOpponentLifeLossEachTurnTriggerEffect`, `OncePerTurnTriggerEffect`'s `firstLifeGain` and
`firstLifeLoss` components and factories (only the legacy view built them) and
`TemporaryGlobalTriggeredAbility.legacyEffect()` (the life watchers were its last readers). `TriggeredAbility` rejects
"one or more" on a life condition as it is built.

MACH-1 is the first ability that mixes a condition read as an ability with one read by the legacy view (the own enters
collector). New `TriggeredAbility.hasLegacyView()` (some condition isn't collected as an ability): `legacyEffects()` throws
only when it is false, and the slot index files a mixed ability under its legacy conditions' watch keys only, so the
enters collector still finds MACH-1 (with its keyed `OncePerTurnTriggerEffect`) and the life collector reads the same
ability; both mark `limitKey()` in `keyedOncePerTurnTriggersFiredThisTurn`, so the limit stays shared, as run 13 left it.
`CastTargetInstantOrSorceryFromGraveyardHandler` reads the legacy view of the abilities that have one.

**The engine.**

- New `LifeAbilityReader` (`service/trigger`, static), `TapAbilityReader`'s shape on `WatchedAbilities`' finders: each
  effect as a `LifeEffect` with the intervening "if" around it (a `ConditionalEffect`, **CR 603.4**, checked) and the
  event condition inside that (**CR 603.2**, checked), plus the ability, its own event (`event()`: the watched one or
  "for the first time during each of their turns"), `firstTimeEachTurn()` and the limit (`limitReached` / `markLimit` by
  `limitKey()`, `marksLimitOnAcceptance()` for "do this only once each turn", **CR 603.2h**, checked,
  `triggersOnlyOnceEachTurn()`). It throws on a trigger mode, on `ONCE` and the per-creature limit, on a count of
  another player's or a payment's events, on a limit next to a count, on options next to "for the first time during each
  of their turns" and on two conditions one watch reads. The strict `printedEffects` / `printedRegistrations` /
  `effects` serve the readers that judge none of them and throw on one.
- `TriggerCollectionService`: the "an opponent loses life" collector (`collectOpponentLifeLossAbility`) judges the limit
  and puts Valgavoth's ability on the stack itself (`collectFirstOpponentLifeLossEachTurn`, the old wrapper handler's
  body: the active player's first loss of the turn, once for each opponent, all effects one stack object, **CR 603.3**,
  checked); the "you lose life" collector (`collectControllerLifeLossAbility`) the count and the limit; the "you gain
  life" collector the count and the limit, for printed and statically granted abilities; the pay-life collector the
  limit; the graveyard "you gain life" collector the count. `dispatchWatch` has a life branch for "an opponent gains
  life" (strict); the graveyard "an opponent gains life" collector and the temporary global watchers (False Cure) read
  the strict effects, the graveyard ones through `getEffectiveGraveyardTriggeredAbilities`.
- `MiscTriggerCollectorService`: Emet-Selch's handler is keyed on `CastTargetInstantOrSorceryFromGraveyardEffect` (it was
  keyed on the wrapper; the collector judges the limit, accepting the cast marks it); Valgavoth's handler moved into the
  collector; the "you lose life" `OncePerTurnTriggerEffect` handler is gone (the collector always unwrapped the wrapper
  before dispatching, so nothing reached it); Nykthos Paragon's handler no longer checks the wrapper (the collector
  judges the limit).

**Behaviour changes.**

- "Whenever you gain life for the first time each turn" is judged by the count alone; the wrapper also marked a keyed
  limit (and, from the graveyard, Deathless Knight's unkeyed card mark), which could never be set without the count
  failing afterwards. As 16j did for the first discard event.
- Nykthos Paragon's "do this only once each turn" is judged by the collector before the intervening "if" and the
  dispatch, where the handler judged it; it has no intervening "if", so nothing else differs.
- Nothing else is known to change: each collector judges the count and limit in the context and at the point it
  judged the wrappers, passes the same "triggers only once each turn" flag (a count is not one, so "for the first time
  each turn" abilities still get additional triggers, as before), and the "you lose life" collector still dispatches
  through the registry directly (no additional-trigger copies).

**Kept, with their reason.**

- The intervening "if" and the event condition still reach the collectors as `ConditionalEffect`s, and the "you gain /
  lose life" collectors still check the intervening "if" only as the ability triggers (`unwrapLifeChangeConditional`),
  as run 13 noted: rechecking it on resolution (CR 603.4) changes behaviour and is a rules fix for a run with its tests.
- Ob Nixilis's "exactly 1 life" `SequenceEffect` handler and Emet-Selch's "one or more opponents" (run 13's notes);
  the step 1 surprises (the opponent-loses and you-lose collectors read no granted abilities, the pay-life collector has
  no ability-loss check on printed abilities, the duplicate `MayPayManaEffect` / `SequenceEffect` handlers).
- `OncePerTurnTriggerEffect` and `OnceOnlyTriggerEffect` stay: other families' legacy views and the enters collector
  (MACH-1) still build them.

**For 16m onward.**

- The other families (sagas, misc, zone changes), as 16a planned; each can reuse `WatchedAbilities` and a reader of
  `LifeAbilityReader`'s shape. When the own enters abilities leave the legacy view, MACH-1's limit is read from the
  ability by both collectors and `hasLegacyView()` can go with the legacy view.
- Combat's, counters' and the self-untap handler's grouping by ability, and the "when you cast this spell" merges (16d,
  16f).

**Tests changed.**

- `LifeTriggerTest`: the legacy-view tests became reader tests: no legacy view (the ability, the getters and the watch
  throw), the count and the intervening "if", the event condition inside the intervening "if", a graveyard count read in
  the graveyard, the gain-and-loss ability's shared keyed limit, "do this only once each turn" marked as taken, the
  first loss during their turn read with every effect (and rejected by the strict reader), MACH-1's enters-and-gain
  ability (legacy view for the enters condition only, one limit), the strict reader, what the life collectors can't
  read rejected ("one or more" as the ability is built, a count of an opponent's gains or of payments, a limit next to a
  count, `ONCE`, options on the first loss during their turn, two conditions one watch reads, a trigger mode).
- `LifeWatchTest`: reads through the reader. `EnterTriggerCollectorServiceTest#lifeGainOncePerTurnTriggerFiresOnlyOnce`
  authors the limit as an option (it authored the unkeyed wrapper, which no card does) and asserts the ability's keyed
  mark. `CombatTriggerTest#grantedAndRegisteredAbilitiesAreReadByTheReader` asserts that the ability has no legacy view
  instead of calling the removed `TemporaryGlobalTriggeredAbility.legacyEffect()`.

**Test runs.**

- Card: the test class of every card with a life ability (by the scan, granting cards and Shadowheart included) and
  every card test that constructs one of them (163 classes, 1,376 tests): all passing.
- Application: the `service.trigger` and `model.trigger` packages and the freeze, triggered-ability, card-ability,
  granted-ability, granted-ability view, simulation-copy, architecture, targeting-consistency, enters-trigger and
  pipeline classes (828 tests): 18 failures, all failing at HEAD: `DeathTriggerCollectorServiceTest` 6,
  `DamageTriggerCollectorServiceTest` 5, `SpellCastTriggerCollectorServiceTest` 2, `LandTapTriggerCollectorServiceTest`
  2, and the known one each in `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and
  `CardEffectTargetingConsistencyTest`. `LifeTriggerTest`, `LifeWatchTest` and `EnterTriggerCollectorServiceTest` pass.
- Engine: the whole module, with the one failure at HEAD, `GameMutationBoundaryRatchetTest`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 16m: sagas read the ability

Step 13's family 10, next in 16a's order after life, as 16l's findings listed the families left: no Saga ability (a
chapter ability, a `ChapterTrigger`, or "whenever the final chapter ability of a Saga you control triggers / resolves",
a `FinalChapterTrigger`) has a legacy view any more, and every reader of one reads the ability.

**What the scan found.** A grep of every card class: 771 chapter registrations in 214 card classes, which take no
options (run 14), and 3 final-chapter abilities (Tom Bombadil; Narci, Fable Singer; Historian's Boon), each of one
effect and `TriggerMode.NORMAL`, with no granted or temporary one. Tom Bombadil's "this ability triggers only once each
turn" is the family's only option. No card changed.

**The model.** `ChapterTrigger.collectedAsAbility()` and `FinalChapterTrigger.collectedAsAbility()` are true:
`TriggeredAbility.legacyEffects()`, the `getLegacy…` getters and `SagaWatch.effectsOf` / `registrationsOf` throw for a
Saga ability, and the card's slot index leaves Saga abilities out of its legacy maps. The chapter readers
(`SagaChapterService`, `StepTriggerService`, the `PermanentCounterSupport` fallback, `initializeReturnedSaga`,
`TargetLegalityService`) already read the plain effects (`getTriggerEffects(Triggers.chapter(n))`), so nothing of theirs
changed. The legacy view's final-chapter check (`requireFinalChapterOptions`) moved into the `TriggeredAbility`
constructor, as runs 16k and 16l did for "one or more": an ability with a final-chapter condition is rejected as it is
built with an intervening "if", "do this only once each turn", `ONCE` or "one or more", as before, and now also with an
event condition (**CR 603.2**, checked), which no final-chapter collector checks (the legacy view handed the handler the
`atTriggerTime` wrapper, which it put on the stack unjudged; no card wrote one).

**The engine.**

- New `SagaAbilityReader` (`service/trigger`, static), `CounterAbilityReader`'s shape on `WatchedAbilities`' finders:
  each effect as a `SagaEffect` with its ability and the limit (`limitReached` / `markLimit` by `limitKey()`,
  `triggersOnlyOnceEachTurn()`). It throws on a registration's trigger mode, which the collectors don't read.
- `TriggerCollectionService.dispatchWatch` has a Saga branch, `dispatchSagaWatch` (`dispatchCounterWatch`'s shape): it
  reads the printed abilities (text-changed, CR 612.1) and those static abilities grant, judges the limit before the
  ability's effects are dispatched and marks it once one triggered, and passes "triggers only once each turn" to the
  dispatch, so nothing makes the ability trigger additional times. Where the generic branch read the legacy view, the
  keyed `OncePerTurnTriggerEffect` was judged and marked the same way, by `OncePerTurnTriggerSupport`, in the same
  `keyedOncePerTurnTriggersFiredThisTurn` set, and the wrapper as the match's raw effect said "triggers only once each
  turn".
- `SagaTriggerCollectorService`'s handler is unchanged: it still gets each effect on its own.

**Behaviour changes.** None known: the collectors judge the same limit, by the same key, at the same point, and pass the
same "triggers only once each turn". The new rejection of an event condition on a final-chapter ability changes no
card.

**Kept, with their reason.**

- The final-chapter handler still puts each effect of an ability on its own stack entry (every final-chapter ability
  has one effect); grouping by ability is the later cleanup with combat's, counters' and the self-untap handler's.
- Run 10's and 14's Saga items (the duplicated chapter logic, chapter I of a returned Saga without targeting, the final
  chapter recognised by the stack entry's description, proliferate not triggering chapters, Copy Enchantment's Saga copy
  without a lore counter) and step 15's (the Saga's mana value bound by the handler, the Saga-specific chapter target
  records).
- `OncePerTurnTriggerEffect` and `OnceOnlyTriggerEffect` stay: the other families' legacy views (misc, action, zone
  changes, the own enters collector) still build them.

**For 16n onward.**

- The families left on the legacy view (misc, player and keyword actions, zone changes, the own enters abilities), as
  16a planned; each can reuse `WatchedAbilities` and a reader of `SagaAbilityReader`'s or `LifeAbilityReader`'s shape.
  When the own enters abilities leave the legacy view, MACH-1's limit is read from the ability by both collectors and
  `hasLegacyView()` can go with the legacy view.
- Combat's, counters', the self-untap handler's and the final-chapter handler's grouping by ability, and the "when you
  cast this spell" merges (16d, 16f).

**Tests changed.**

- `SagaTriggerTest`: the two legacy-view tests became: no legacy view for a chapter or final-chapter ability (the
  abilities, the getters and the watch throw; the plain effects still read), the reader's limit marked by the ability
  (not the unkeyed mark), a final-chapter ability without a limit read with every effect and rejected by the other
  watch, the options no collector reads rejected as the ability is built (the event condition added, through
  `Card.addTrigger` too), and a trigger mode rejected by the reader.
- `SagaWatchTest#aWatchReadsOnlyTheAbilitiesOfItsTrigger` reads through the reader;
  `aWatchHandsTheCollectorTheLimitAsTheKeyedWrapperWithAStableIdentity` is gone with the wrapper.

**Test runs.**

- Card: the test class of every card with a chapter or final-chapter ability or the Saga subtype, every card test that
  mentions lore counters, a Saga or a chapter, and every card test that constructs one of those cards (300 classes,
  3,097 tests): all passing.
- Application: the `service.trigger` and `model.trigger` packages and the freeze, triggered-ability, card-ability,
  granted-ability, simulation-copy, architecture, targeting-consistency, step-trigger, stack-resolution, state-based
  action, legacy-slot, pipeline, deep-copy, Saga chapter, trigger-options, counter-support and exile-and-return classes
  (1,077 tests): 47 failures, all failing at HEAD: `StepTriggerServiceTest` 16 and `StateBasedActionServiceTest` 13
  (mock wiring, as runs 10 and 16a found them), `DeathTriggerCollectorServiceTest` 6,
  `DamageTriggerCollectorServiceTest` 5, `SpellCastTriggerCollectorServiceTest` 2, `LandTapTriggerCollectorServiceTest`
  2, and the known one each in `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and
  `CardEffectTargetingConsistencyTest`. No test with Saga, lore or chapter in its name failed (44 ran).
- Engine: the whole module, with the one failure at HEAD, `GameMutationBoundaryRatchetTest`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 16n: player and keyword actions read the ability

Step 13's family 11, next in 16a's order after sagas, as 16m's findings listed the families left. Family 11 is split as
step 13 split it: this run takes its first half (step 13 run 11a, the `ActionWatch` collectors), and the misc
collectors (`MiscWatch`: control change, attaching, crew, saddle and station, Room doors and full unlocks, day and night,
state triggers) are left for 16o. No player or keyword action ability (a `PlayerActionTrigger` or `ObjectActionTrigger`
that an `ActionWatch` reads: dice, coin flips, crimes, the Ring, the monarch, dungeons, voting, investigate, explore,
connive, exploit, clash, mutate, plot, champion, losing the game and the other keyword actions) has a legacy view any
more, and every reader of one reads the ability.

**What the scan found.** A grep of every card class for the 42 `ActionWatch` factories: 149 card classes, every ability
printed (`addTrigger`, none granted, temporary or built by the engine), one condition each and `TriggerMode.NORMAL`
throughout. 14 "triggers only once each turn" (the 13 crime cards and Curator of Sun's Creation, discover), 3 intervening
"if"s (Knights of the Black Rose, Overzealous Muscle, Barbarian Class), 2
event conditions (Pompeii, Mr. House), 4 filters (Nicanzil's two explore abilities, Henry Wu's exploit, My Laughter
Echoes' scheme). No card changed.

**The model.** `PlayerActionTrigger.collectedAsAbility()` is true for every action but `FULLY_UNLOCKS_ROOM`, and
`ObjectActionTrigger.collectedAsAbility()` for `FIGHTS`, `EXPLORES`, `CONNIVES`, `EXPLOITS`,
`EXPLOITS_NONTOKEN_CREATURE`, `CHAMPIONS`, `BECOMES_PLOTTED` and `MUTATES`: the crew, saddle, station, door and Room
actions still have a legacy view, for the `MiscWatch` collectors. For an action ability `TriggeredAbility.legacyEffects()`,
the `getLegacy…` getters and `ActionWatch.effectsOf` / `registrationsOf` throw, and the card's slot index leaves it out
of its legacy maps. The legacy view's scheme branch (`legacySchemeEffects`) and its explored or exploited card's
`TriggeringCardConditionalEffect` are gone (an object-action filter is only allowed on explore and exploit, which have no
legacy view now), with `SchemeSetInMotionTriggerEffect` (only the legacy view built it). `TriggeredAbility` rejects "one
or more" on an action condition as it is built ("roll one or more dice" and "investigate one or more times" are events of
their own).

**The engine.**

- New `ActionAbilityReader` (`service/trigger`, static), `TapAbilityReader`'s shape on `WatchedAbilities`' finders:
  each effect as an `ActionEffect` with the intervening "if" around it (a `ConditionalEffect`, **CR 603.4**, checked)
  and the event condition inside that (**CR 603.2**, checked), plus the ability, the watched condition's card filter
  (`filter()`: the explored card, the exploited creature, the scheme) and the limit (`limitReached` / `markLimit` by
  `limitKey()`, `triggersOnlyOnceEachTurn()`). It throws on a trigger mode, on a limit other than "triggers only once
  each turn", on options on a "you set a scheme in motion" ability, and on two conditions of one ability with different
  filters. The strict `printedEffects` / `effects` serve the collectors that judge neither a filter nor a limit and throw
  on one.
- `TriggerCollectionService.dispatchWatch` has an action branch, `dispatchActionWatch` (`dispatchSagaWatch`'s shape): it
  reads the printed abilities (text-changed, **CR 612.1**, checked) and those static abilities grant, judges the filter
  against the card the action is taken with (`actionCard`: the exploited creature or the scheme set in motion; a filter
  on any other watch throws), judges the limit before the effects are dispatched and marks it once one triggered, and
  passes "triggers only once each turn" to the dispatch. Where the generic branch read the legacy view, the crime and
  discover limits were the keyed `OncePerTurnTriggerEffect`, judged and marked by the same key in the same set, and the
  exploit filter was the `TriggeringCardConditionalEffect` the handler judged.
- "Whenever you set a scheme in motion": `SchemeTriggerCollectorService`'s handler moved into the collector
  (`collectSchemeAbility`, through `dispatch`, so additional-trigger effects still apply): all of an ability's effects are
  one stack object (**CR 603.3**, checked), as the `SchemeSetInMotionTriggerEffect` record made them, with the scheme
  snapshot bound as before. The class is gone.
- The explore collector judges each ability's filter against the explored card and queues each matching ability as
  its own stack object (**CR 603.3**); the other direct readers (the planar die, the natural-20, dungeon, crime and
  conjure graveyard readers, the monarch, Case, tax, exploit, clash, coin-flip, plot and mutate collectors,
  `GameOutcomeService`'s losing-the-game reader, the champion reader and `MiscTriggerCollectorService`'s mutate handler,
  which regroups a card's mutate effects by target group) read the strict effects. The exploit collector's last-known
  path for an exploiting creature that sacrificed itself (Henry Wu) reads its abilities through
  `dispatchActionAbility`, so its filter is judged there too. The crime and conjure graveyard readers read
  `getEffectiveGraveyardTriggeredAbilities`, as `getEffectiveGraveyardLegacyTriggerEffects` did.
- `MiscTriggerCollectorService`: the crime `OncePerTurnTriggerEffect` handler is gone (the dispatch always unwrapped the
  wrapper first, so nothing reached it, as 16l found the you-lose one), and the exploit handler no longer unwraps a
  `TriggeringCardConditionalEffect` (the collector judges the filter). The tax collector no longer unwraps a
  once-per-turn wrapper (no tax ability has a limit; the strict reader rejects one), and the Case collector no longer
  unwraps a `TriggeringCardConditionalEffect` (the legacy view never gave one and no card writes one).

**Behaviour changes.**

- The explore collector puts each of a card's explore abilities on the stack as its own object, where it merged every
  matching effect of the card into one (run 15's note, CR 603.3). Nicanzil is the only card with two explore abilities,
  and only one of them matches any explored card, so no card is known to change.
- Nothing else is known to change: each collector judges the filter and limit at the point and in the context it judged
  the wrappers, by the same key, and passes the same "triggers only once each turn".

**Kept, with their reason.**

- The intervening "if" and the event condition still reach the collectors as `ConditionalEffect`s (the monarch
  collector and the crime handler check the intervening "if" as the ability triggers), as the earlier runs left them:
  making the effects read the trigger event is step 15.
- The direct readers still read the printed abilities only, as before (no granted ones; the dungeon and natural-20
  graveyard readers no perpetual ones and no graveyard ability loss), and the exploit, clash and plot collectors still
  put every effect of a card's ability on one stack object (one ability each since step 13's `ALL` merges).
- Step 13 run 11a's items: the Ring's two triggers, the clash and champion target routing, the losing-the-game triggers;
  step 15's event bindings (the dice results, the sought, conjured and discovered cards, the exploited, explored,
  conniving, fighting and mutating creatures, the tax payer, the losing player).
- `OncePerTurnTriggerEffect`, `OnceOnlyTriggerEffect` and `TriggeringCardConditionalEffect` stay: the other families'
  legacy views (misc, zone changes, the own enters collector) and the cards' granted forms still build them.

**For 16o onward.**

- Family 11's second half (`MiscWatch`: control change, attaching, crew, saddle and station, Room doors and full
  unlocks, day and night, state triggers), then the zone changes and the own enters abilities, as 16a planned; each can
  reuse `WatchedAbilities` and a reader of `ActionAbilityReader`'s shape. `ObjectActionTrigger` and `PlayerActionTrigger`
  decide `collectedAsAbility()` by action until then. When the own enters abilities leave the legacy view, MACH-1's limit
  is read from the ability by both collectors and `hasLegacyView()` can go with the legacy view.
- Combat's, counters', the self-untap handler's and the final-chapter handler's grouping by ability, and the "when you
  cast this spell" merges (16d, 16f).

**Tests changed.**

- `ActionTriggerTest`: new reader tests: no legacy view for an action ability (the ability, the getters and the watch
  throw; the plain effects still read), every `ActionWatch` trigger read as an ability and the misc actions with a
  legacy view, the limit marked by the ability (not the unkeyed mark), the explore, exploit and scheme filters from the
  watched trigger, the event condition inside the intervening "if", the strict reader rejecting a filter or a limit,
  and what the collectors can't read rejected ("one or more" as the ability is built, "do this only once each turn" and
  `ONCE`, options on a scheme ability, a trigger mode, an unwatched condition).
- `MiscTriggerTest`: the explored card's wrapper and the scheme record tests are gone with the legacy view of them
  (`aDoorAndAnExploredCardAreDroppedFromTheWatchKeyAndADoorIsReadAsItsWrapper` keeps the door and both watch keys and
  checks a door with a limit throws; `aSchemeTriggerIsWatchedWithoutItsFilter` keeps the scheme's watch key);
  `aLimitIsReadAsTheKeyedMark` (crime) is gone, read by `ActionTriggerTest` now.
- `ActionWatchTest#aWatchReadsOnlyTheAbilitiesOfItsTriggerAndZone` reads through the reader.

**Test runs.**

- Card: the test class of every card with an action ability (146), every card test that constructs one of those cards,
  and every card test that mentions explore, exploit, the monarch, a scheme, clash, mutate, plot, connive, coin flips,
  investigate, a dungeon, a natural 20 or a crime (915 classes, 7,218 tests): the first run failed only the 49 mutate
  tests of 8 classes (the mutate handler's own read of the watch), and after that fix every card test that mentions
  mutation (2,408 classes) passes.
- Application: the `service.trigger` and `model.trigger` packages and the freeze, triggered-ability, card-ability,
  granted-ability, granted-ability view, simulation-copy, architecture, targeting-consistency, legacy-slot, pipeline,
  stack-resolution, spell-casting, state-trigger and Planechase classes (1,002 tests): 30 failures, all failing at HEAD:
  `SpellCastingServiceTest` 10, `DeathTriggerCollectorServiceTest` 6, `DamageTriggerCollectorServiceTest` 5,
  `SpellCastTriggerCollectorServiceTest` 2, `LandTapTriggerCollectorServiceTest` 2, `PlanechaseServiceTest` 2, and the
  known one each in `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and
  `CardEffectTargetingConsistencyTest`. `ActionTriggerTest`, `ActionWatchTest`, `MiscTriggerTest` and `MiscWatchTest`
  pass.
- Engine: the whole module, with the one failure at HEAD, `GameMutationBoundaryRatchetTest`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 16o: the rest of family 11 reads the ability

Step 13's family 11, its second half (step 13 run 11b), as 16n's findings left it: no control change, attach, crew,
saddle or station, Room door or full unlock, day and night, state or planar ability has a legacy view any more, and every
reader of one reads the ability. With it no family 11 ability has a legacy view: only the zone change and permanent
change triggers (and `LegacySlotTrigger`) still have one.

**What the scan found.** A temporary reflection scan (every registered card class and back face, their abilities and the
`TriggeredAbility` values nested in their effects) found 336 abilities of the family in 293 card classes, all printed
(none granted, temporary or built by the engine), every registration `TriggerMode.NORMAL`: 189 planar (143 chaos), 30
Room doors (no Room has two abilities on one door, and every door and full-unlock ability has one effect), 17 full
unlocks (the Eerie abilities, each with its enters condition), 62 state triggers, 10 day and night, 11 attach,
3 control change and 14 crew, saddle and station abilities. The options: Inchblade Companion's "triggers only once each
turn" (Equipment attach) and the four state triggers' intervening "if" (Opal Avenger, Lurking Jackals, Veiled
Crocodile, Hidden Predators); the first crew or saddle each turn is Mindlink Mech's and Stubborn Burrowfiend's event. No
card changed.

**The model.**

- `collectedAsAbility()` is true for `ControlChangeTrigger`, `AttachTrigger`, `DayNightTrigger`, `StateTrigger`,
  `PlanarTrigger` and every `ObjectActionTrigger` and `PlayerActionTrigger` action: `TriggeredAbility.legacyEffects()`,
  the `getLegacy…` getters and `MiscWatch.effectsOf` / `registrationsOf` throw for them, and the card's slot index leaves
  them out of its legacy maps. An Eerie ability ("whenever an enchantment you control enters and whenever you fully
  unlock a Room") keeps its legacy view for the enters collectors, as MACH-1's does.
- The legacy view's state branch (`legacyStateEffects`), its door and first-time wrappers (`objectActionView`,
  `legacyObjectActionWrapper`, `filteredTrigger`) are gone, and with them `TriggeringRoomDoorConditionalEffect` (only the
  legacy view built it) and `Card`'s target binding through it. `legacyEffects()` now wraps only the options.
- `TriggeredAbility` rejects as it is built "one or more" on any family 11 condition (no collector of the family reads
  it), and on a state trigger (**CR 603.8**, checked) any option but an intervening "if" (**CR 603.4**, checked): a limit,
  a multiplicity or an event condition (**CR 603.2**, checked), which the legacy view rejected as a card's abilities were
  indexed. No card writes one.

**The engine.**

- New `MiscAbilityReader` (`service/trigger`, static), `ActionAbilityReader`'s shape: each effect as a `MiscEffect` with
  its ability, the watched condition's door, "for the first time each turn" and the limit (`limitReached` / `markLimit`:
  the first crew or saddle by the unkeyed once-each-turn mark on the Vehicle or Mount, as the collectors judged it
  before; "triggers only once each turn" by `limitKey()`). The intervening "if" and the event condition still wrap the
  effects. It throws on a trigger mode, a limit other than "triggers only once each turn", and a limit next to a first
  time; the strict `printedEffects` / `effects` throw on a door, a first time or a limit. `stateTriggers(card)` reads a
  card's state triggers as one `StateTriggerEffect` of its game state and effects each, the form `StateTriggerService`
  judges (Olivia's granted `StateTriggerEffect` is unchanged).
- `TriggerCollectionService.dispatchWatch` has a misc branch, `dispatchMiscWatch` (`dispatchActionWatch`'s shape): the
  printed abilities (text-changed, **CR 612.1**, checked) and those static abilities grant; a door ability triggers only
  for its own door (**CR 709.5h**, checked) and is one stack object of all its effects (**CR 603.3**, checked), put on the
  stack by `collectDoorAbility`, the former `MiscTriggerCollectorService` door handler moved into the collector; the
  limit is judged before and marked once one effect triggered. The generic legacy branch of `dispatchWatch` is gone (no
  watch reaches it).
- The direct readers read the abilities: crewed, saddled and stationed (`dispatchPrintedMiscAbilities`, which judges the
  first time and the limit per ability, so the crewed and saddled `OncePerTurnTriggerEffect` handlers are gone), the
  Equipment attach collector (Inchblade Companion's limit by the ability, where `OncePerTurnTriggerSupport` judged the
  keyed wrapper by the same key), the crew, saddle-or-crew-during-main-phase (printed and granted), Aura attach,
  graveyard day and night (`graveyardMiscEffects`, as `getEffectiveGraveyardLegacyTriggerEffects` read them; that method
  and `GameData.perpetualLegacyTriggerEffects` are gone) and graveyard Eerie readers, `CreatureControlService` (printed,
  temporary and persistent control-change abilities), `CombatAttackService`'s delayed Vehicle attack, `StateTriggerService`
  (battlefield and planar state triggers), `PlanechaseService.trigger` (its non-step triggers) and the Room's door target
  groups copied as it enters (`StackResolutionService`, from the door abilities' effects as the card bound them).

**Behaviour changes.** None known. Each collector judges the door, the first time and the limit at the point and by the
key it judged the wrappers, and every ability of the family has one effect where a collector groups (the door handler
merged the effects of all of a card's abilities on the unlocked door; no Room has two). The state-trigger reader names
and indexes its triggers as before. The new rejections change no card.

**Kept, with their reason.**

- The intervening "if" and the event condition still reach the collectors as `ConditionalEffect`s, as the earlier runs
  left them (step 15). The first crew or saddle each turn is still the unkeyed mark set when the ability triggers, not
  the first time the Vehicle or Mount became crewed or saddled that turn whether or not it triggered.
- `StateTriggerEffect` stays: the reader builds it, and Olivia, Crimson Bride grants one through a static ability.
- `TriggerWatch.effectsOf` / `registrationsOf` now throw for every watch; deleting them (and their tests' throw checks)
  is a later cleanup. Step 13 run 11b's and step 15's items (the event bindings, the planar and state readers' zones).
- `OncePerTurnTriggerEffect` and `OnceOnlyTriggerEffect` stay: the zone-change and own enters legacy views still build
  them.

**For 16p onward.**

- The families left on the legacy view: the zone changes (`ZoneChangeWatch`, the creature-death and pilot collectors,
  the permanent changes) and the own enters abilities, as 16a planned; each can reuse `WatchedAbilities` and a reader of
  `MiscAbilityReader`'s shape. When the own enters abilities leave, MACH-1's and the Eerie abilities' legacy view goes and
  `hasLegacyView()` can go with the legacy view.
- Combat's, counters', the self-untap handler's and the final-chapter handler's grouping by ability, and the "when you
  cast this spell" merges (16d, 16f).

**Tests changed.**

- `MiscTriggerTest`: the legacy-view tests became reader tests: no legacy view for any trigger of the family (the
  abilities, the getters and every `MiscWatch` throw; the plain effects still read), the Eerie ability's legacy view kept
  for its enters condition and its unlock read as an ability, a state trigger read as one `StateTriggerEffect` (two
  abilities, with and without an intervening "if") and the options a state trigger can't take rejected as it is built,
  the door read from the ability (the strict reader rejects it), the first crew or saddle each turn judged and marked by
  the unkeyed mark (a limit next to it rejected), Inchblade's limit marked by the ability, the event condition inside
  the intervening "if", and what the collectors can't read rejected ("one or more" on every factory, the other limits, a
  trigger mode, an unwatched condition).
- `ActionTriggerTest`: `everyActionWatchTriggerIsReadAsAnAbilityAndTheMiscActionsStillHaveALegacyView` became
  `everyActionTriggerIsReadAsAnAbilityTheMiscActionsIncluded`; the "one or more" rejection expects the new message.
- `MiscWatchTest#aWatchReadsOnlyTheAbilitiesOfItsTriggerAndZone` reads through the reader.
- `TriggeredAbilityTest#aSpellLandPlayOrActivationAbilityHasNoLegacyView` uses `Triggers.dies(THIS)` as the trigger that
  still has a legacy view, where it used `youLoseControlOfThis()`.

**Test runs.**

- Card: the test class of every card with a family 11 ability of this run (by the scan), every card test that
  constructs one of those cards, and every card test that mentions crewing, saddling, stationing, unlocking, Rooms, day
  and night, Planechase or chaos, state triggers, control changes or attaching (1,793 classes, 15,958 tests): all
  passing.
- Application: the `service.trigger` and `model.trigger` packages and the state-trigger, Planechase, stack-resolution,
  spell-casting, creature-control, state-based action, game-query, freeze, triggered-ability, card-ability,
  granted-ability, granted-ability view, simulation-copy, architecture, targeting-consistency, legacy-slot and pipeline
  classes (1,402 tests): 45 failures, all failing at HEAD: `StateBasedActionServiceTest` 13, `SpellCastingServiceTest`
  10, `DeathTriggerCollectorServiceTest` 6, `DamageTriggerCollectorServiceTest` 5, `SpellCastTriggerCollectorServiceTest`
  2, `LandTapTriggerCollectorServiceTest` 2, `PlanechaseServiceTest` 2 (the source-less planeswalking cases),
  `CreatureControlServiceTest` 1, `GameQueryServiceTest` 1, and the known one each in `SimulationCopyCompletenessTest`,
  `CardImmutabilityArchTest` and `CardEffectTargetingConsistencyTest`. `MiscTriggerTest`, `MiscWatchTest`,
  `ActionTriggerTest`, `StateTriggerServiceTest` and `MiscTriggerCollectorServiceTest` pass.
- Engine: the whole module, with the one failure at HEAD, `GameMutationBoundaryRatchetTest`.
- AI: the whole module, with the same 10 failures as before.
- The full suite was not run here.

#### Run 16p: the zone changes and the permanent changes read the ability

The first of the "16p onward" runs, split as 16n and 16o split family 11: the zone changes but "when this enters" and
the permanent changes (phasing, transforming, being turned face up), the families steps 10 to 12a moved to the trigger
pipeline. No such ability has a legacy view any more; "when this enters" and its about 60 readers are left for 16q.

**What the scan found.** A temporary reflection scan (every registered card class and back face, their abilities and
the `TriggeredAbility` values nested in their effects and static abilities) found 3,091 abilities with a zone-change
trigger other than "when this enters" or a permanent-change trigger, in 2,901 card classes (236 nested: registered,
granted or delayed). None has a trigger mode other than `NORMAL`. 229 have options (102 "one or more", 64 "triggers only
once each turn", 13 "do this only once each turn", 70 an intervening "if", 1 `ONCE`, 1 an event condition, some
together); the pipeline judges all of them. Of the nested ones only Polluted Cistern's ("one or more", an ability a
static ability grants, which the pipeline reads) has an option. 64 abilities pair "when this enters" with another zone change (constellation's "whenever this or another
enchantment you control enters"), 17 a zone change with a trigger already read as an ability (the Eerie abilities). No
card changed.

What still reads the legacy view was already only "when this enters": every `getLegacy…` call is with
`Triggers.enters(ObjectRelation.THIS)`, the granted readers (`Permanent.getGrantedTriggerEffects`,
`GrantedTriggeredAbilitySupport.grantedTriggerEffects`) are called only by `EtbTriggerService` with it, and the pipeline
(`TriggeredAbilityStackService.trigger`, `triggerOnce`, `triggerPermanentChange`) reads the abilities' effects and
options itself. What dropped options was the readers outside the pipeline, which read the raw effects.

**The model.**

- `ZoneChangeTrigger.ownEntry()` (new): whether the trigger is `Triggers.enters(ObjectRelation.THIS)` (**CR 603.6a**,
  checked). `ZoneChangeTrigger.collectedAsAbility()` is true for every other zone change, and
  `PermanentChangeTrigger.collectedAsAbility()` for every permanent change, so `TriggeredAbility.legacyEffects()`, the
  `getLegacy…` getters and `requireLegacyView` throw for them and the card's slot index leaves them out of its legacy
  maps. An ability with "when this enters" and another zone change keeps its legacy view for the own entry only (its
  other trigger isn't a legacy key any more); the Eerie abilities have none now.
- `TriggeredAbility` rejects, as it is built, on a zone change other than "when this enters" or a permanent change,
  what the pipeline doesn't read: "once each turn for each creature" (`limitReached` threw on it as the ability
  triggered) and a trigger mode (the pipeline puts the ability on the stack as one object, **CR 603.3**, checked, and
  ignored it). No card writes either.

**The engine.**

- New `ZoneChangeAbilityReader` (`service/trigger`, static): `printed(card, trigger)` (the abilities with the trigger,
  in any zone), `strictEffects(abilities)` and `strictEffect(ability)` (one effect), which throw on any option or
  trigger mode (and `strictEffect` on more than one effect), which these readers don't judge and dropped before.
- The readers outside the pipeline read through it: the face-up planes' creature-death (Agyrem, **CR 901.7**,
  checked), nonblack-card-to-graveyard, creature-leaves (Takenuma), landfall and creature-enters readers
  (`planarEnteringEffects`), the face-up schemes' "another permanent leaves the battlefield" reader, and the abilities a
  resolved spell or ability registered (**CR 603.7**, checked): "whenever a creature dies" (and "when it dies this
  turn"), "when it leaves the battlefield", "whenever a creature you control enters", "whenever a creature or
  enchantment you control enters" and "whenever a creature an opponent controls enters". `ZoneChangeWatch.effectsOf`
  stays for the code that only inspects which effects a card has (`PermanentRemovalService`).
- `TokenCopySupport`: a token copy's added zone-change or permanent-change ability keeps its trigger mode (`NORMAL`)
  instead of being marked `INDEPENDENT`, which only the "when this enters" collector reads and the ability now rejects
  (Jaxis, the Troublemaker's "when this creature dies, draw a card"). The other families' added abilities are marked as
  before.

**Behaviour changes.** None known: no card's zone-change or permanent-change ability has an option the readers outside
the pipeline would now reject, and the pipeline read `INDEPENDENT` on none.

**Kept, with their reason.**

- The cards still author trigger-level wrappers inside zone-change abilities, kept by the earlier runs: the keyed
  `OncePerTurnTriggerEffect` factories (Akim's first token creation, Crawling Sensation's first land card to the
  graveyard, Dennick's "triggers only once each turn"), the `TriggeringCardConditionalEffect` /
  `TriggeringPermanentConditionalEffect` / `TriggeringArtifactControllerConditionalEffect` filters (Agyrem, Emeria
  Shepherd, Oran-Rief Hydra, Disa, Oathkeeper, The Master, Urza's Miter, …), 12 intervening-if `ConditionalEffect`s at
  the top of an ability (Academy Rector, Bridge from Below, Tunnel Ignus, Trail of Mystery, …) and `EvolveTriggerEffect`
  / `ChampionOfTheHareishTriggerEffect`. Their handlers judge them as before; turning them into filters, limits and
  options is the zone-change part of 16q onward.
- The handlers' event bindings (`LegacyTriggerBindings`, `DeathTriggerCollectorService`) and their may-unwrapping
  (step 15).
- `GameQueryService.getEffectiveGraveyardTriggerEffects` has no caller; left for the step 16 cleanup.
- The pipeline still drops a "one or more" ability whose watch has no "one or more" key (`ZoneChangeWatch.of` is
  `null`); no card has one. A check needs the engine's watch table, so it isn't in `TriggeredAbility`.

**For 16q onward.**

- "When this enters" is the last family with a legacy view: `EtbTriggerService` and the readers that inspect a card's
  own enters effects (`TargetLegalityService`, `ValidTargetService`, `SpellCastingService`, `EffectResolution`, the AI,
  `CardViewFactory`, …). When it leaves, `hasLegacyView()`, `legacyEffects()`, the `getLegacy…` getters and the
  granted legacy readers go, and with them `OncePerTurnTriggerEffect`'s keyed form and `OnceOnlyTriggerEffect` as
  wrappers the legacy view builds.
- Then `TriggerMode` (read only by the own enters collectors, combat's grouping and `TokenCopySupport`), the
  may-unwrapping, the `interveningIf` / `triggerTimeOnly` flags and the wrapper classes the zone-change cards above still
  author.

**Tests changed.**

- `ZoneChangeTriggerTest`: new reader tests: only "when this enters" has a legacy view (every other zone change and the
  permanent changes are read as abilities, the getters throw, the plain effects still read), an ability of "when this
  enters" and another zone change keeps its legacy view for the own entry only, what the pipeline can't read rejected as
  the ability is built ("once each turn for each creature", a trigger mode; "when this enters" still takes one), and
  `ZoneChangeAbilityReader` reading an ability without options and rejecting a limit, an intervening "if" and a second
  effect.
- `TriggeredAbilityTest#aSpellLandPlayOrActivationAbilityHasNoLegacyView` and
  `TriggerOptionsTest#theLegacyViewGivesTheEventConditionAsATriggerTimeOnlyConditionalInsideTheOtherWrappers` use
  `Triggers.enters(THIS)` as the trigger that still has a legacy view, where they used `Triggers.dies(THIS)`.
- `MiscTriggerTest`: `anEerieAbilityKeepsItsLegacyViewForItsEntersConditionAndIsReadAsAnAbilityForTheUnlock` became
  `anEerieAbilityHasNoLegacyViewAndIsReadAsAnAbilityForTheUnlock`.

**Test runs.**

- Card: the test class of every card with a zone-change or permanent-change ability other than "when this enters" (by
  the scan), and every card test that mentions Planechase, a scheme, a token copy or the Eerie unlocks (3,298 classes,
  25,904 tests): all passing.

- Application: the `service.trigger` and `model` packages and the state-trigger, Planechase, stack-resolution,
  spell-casting, creature-control, state-based action, game-query, freeze, granted-ability, simulation-copy,
  architecture, targeting-consistency, legacy-slot, pipeline, zone-change, scheme and ETB-resolver classes (1,618
  tests): 53 failures, all failing at HEAD: the 45 of 16o's set and 8 in the classes this run's wider filter took in
  (`EtbEffectResolverTest` 3, known since 16a, and one each in `EffectResolutionServiceTest`, `CombatDamageServiceTest`,
  `DealDamageToPlayersEffectHandlerTest`, `InputCompletionServiceTest` and `PermanentRemovalServiceTest`, checked at
  HEAD in this run).
- Engine: the whole module, with the one failure at HEAD, `GameMutationBoundaryRatchetTest`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 16q: "when this enters" reads the ability

The second of the "16p onward" runs: the source's own enters abilities ("when this enters", **CR 603.6a**, checked), the
last trigger with a legacy view. No triggered ability has one now: `EtbTriggerService` reads the abilities and judges
their limit itself, and the code that inspects a card's own enters effects reads a narrower own-entry view. Grouping the
collector's effects by ability (so `TriggerMode` can go) is left for 16r, because the cards aren't written for it yet.

**What the scan found.** A temporary reflection scan (every registered card class and back face, their abilities and the
`TriggeredAbility` values nested in their effects) found 5,351 abilities with "when this enters" in 4,785 card classes
(5,299 printed, 52 nested: granted or registered). 398 have an intervening "if", one an event condition (Death Match's
`SourceIsCreature`), three a limit, all "triggers only once each turn" (MACH-1, Swooping Scoundrel and Oasis of
Renewal's two, Oasis's with "one or more" for its graveyard condition). No nested one has an option. 77 registrations in
53 card classes are `TriggerMode.INDEPENDENT`. No own enters ability authors `OncePerTurnTriggerEffect` or
`OnceOnlyTriggerEffect`. 466 card classes have two or more printed own enters abilities (1,005 abilities), and only 8
abilities have more than one effect: the earlier conversions wrote one ability per effect, and the collector pools them.
No card changed.

**The model.**

- `TriggeredAbility.hasOwnEntry()` (new): whether one condition is "when this enters" (`ZoneChangeTrigger.ownEntry()`).
  `TriggeredAbility.ownEntryEffects()` (new, in place of `legacyEffects()`): the registrations with the intervening "if"
  as a `ConditionalEffect(condition, effect, true)` around each effect (**CR 603.4**, checked) and the event condition as
  the `ConditionalEffect.atTriggerTime` inside it (**CR 603.2**, checked), as the legacy view gave them; the limit isn't
  wrapped. It throws for an ability without "when this enters".
- `Card.getOwnEntryEffects()` / `getOwnEntryEffectRegistrations()` / `getOwnEntryEffectRegistrations(TriggeredAbility)`
  / `getOwnEntryAbilities()` replace `getLegacyTriggerEffects(Trigger)`, `getLegacyTriggerEffectRegistrations(Trigger)`,
  their zone forms and `getLegacyTriggerEffectRegistrations(TriggeredAbility)`; the card's index builds one own-entry
  view (derived, no copied state, nothing for `CardFreezeTest`) in place of the legacy maps, so the wrapped effects keep
  their identity. `Permanent.getGrantedOwnEntryAbilities()` replaces `getGrantedTriggerEffects(Trigger)`;
  `getLegacyTemporaryTriggerEffects` / `getLegacyPersistentTriggerEffects` (no caller) are gone.
- `TriggeredAbility.legacyEffects()`, `hasLegacyView()`, `requireLegacyView`, `collectedAsAbility()` and
  `Trigger.collectedAsAbility()` with its 22 overrides are gone: with no legacy view every trigger is read as an ability.
- `TriggeredAbility` rejects, as it is built, on an ability with "when this enters" what its collector doesn't read: a
  limit but "triggers only once each turn" ("triggers only once", "do this only once each turn", **CR 603.2h**, checked,
  "once each turn for each creature"), and a multiplicity when "when this enters" is the only condition (the permanent's
  own entry is one event). No card writes one.
- `OnceOnlyTriggerEffect` is deleted: only the legacy view built it. Its branches in `Card`'s target binding,
  `TriggerTargetCollector`, `SpellCastTriggerCollectorService` and `CombatAttackService`, and
  `OnceOnlyTriggerSupport.unwrapIfAvailable` / `markIfNeeded`, went with it (the attack collector's unkeyed once-only
  mark for `TriggerLimit.ONCE` stays).

**The engine.**

- `EtbTriggerService.processCreatureETBEffects` reads the printed abilities (`Card.getOwnEntryAbilities()`), those
  granted to the permanent for a duration and those a static ability grants
  (`GrantedTriggeredAbilitySupport.grantedOwnEntryAbilities`, in place of `grantedTriggerEffects(…, Trigger)`), and
  judges each ability's limit before it reads its effects (`TriggeredAbilityStackService.limitReached` by
  `TriggeredAbility.limitKey()`, the key the pipeline marks for the ability's other conditions), marking it where it
  marked the keyed `OncePerTurnTriggerEffect` before. The `OncePerTurnTriggerEffect` unwrapping is gone. The grouping is
  unchanged: the card's plain effects pool into one stack object, the `INDEPENDENT`, may, modal, evoke and granted ones
  are objects of their own.
- `scripts/step14-own-entry-readers.py` rewrote the 60 call sites of the legacy getters in 27 files (engine, test
  fixtures, AI, networking, `EffectResolution`) to the own-entry getters, and removed `collectedAsAbility()` with its
  overrides; the watch and reader Javadocs that linked it were edited to match.
- `CastTargetInstantOrSorceryFromGraveyardHandler` reads "do this only once each turn" from the options of any ability
  (Emet-Selch); its legacy branch read the wrapper only an own enters ability could have had.

**Behaviour changes.** None known: every reader gets the effects it got before, but for MACH-1's and Oasis of Renewal's
own enters effects, which the readers outside the collector now see without the `OncePerTurnTriggerEffect` around them
(neither targets through it), and the collector judges their limit by the same key as before.

**Kept, with their reason.**

- `TriggerMode` and the collector's pooling: grouping by ability would split the 466 cards' pooled abilities into
  several stack objects (CR 603.3, checked), a behaviour change on each. 16r needs a card script that merges the
  abilities of one Oracle ability first (by the Oracle text, as run 4a merged the "this or another" halves), then the
  collector can put each ability on the stack as one object and `INDEPENDENT` goes.
- The intervening "if" and the event condition still reach the collector and the readers as `ConditionalEffect`s, as
  the other families' readers left them (step 15); with them the `interveningIf` / `triggerTimeOnly` flags.
- The may-unwrapping (the 31 event bindings 16a listed and Pontiff of Blight, step 15), the wrapper classes cards still
  author (`OncePerTurnTriggerEffect`'s keyed and `perGrant` forms, the `Triggering…ConditionalEffect` filters, 16p's list),
  and run 4c's `KEEP` cards' wrappers.
- `TriggerWatch.effectsOf` / `registrationsOf`, which throw for every watch, and `LegacySlotTrigger` / `LegacySlots`
  (no `EffectSlot` constant is left): step 16.
- `gameData.perpetualEnterEffectsByCardId` holds effects, not abilities; the collector reads them as before.

**For 16r onward.**

- Merge the own enters abilities of one Oracle ability by script (the 466 card classes above), then make the collector
  put each ability on the stack as one object (CR 603.3) and delete `TriggerMode` (read by the own enters collector,
  combat's grouping and `TokenCopySupport`).
- Then the may-unwrapping, the `interveningIf` / `triggerTimeOnly` flags and the wrapper classes left, as 16p listed.

**Docs.** `scripts/step14-own-entry-docs.py` removed `OnceOnlyTriggerEffect` from `EFFECTS_INDEX.md`,
`EFFECTS_QUICK_REFERENCE.md` (in its place, `TriggerLimit.ONCE`), `CARD_PATTERN_INDEX.md` (Ruby Collector's current
form) and the implement-card and review-card skills; `ARCHITECTURE.md` was edited by hand.

**Tests changed.**

- The per-family legacy-view tests (`ActionTriggerTest`, `CombatTriggerTest`, `CounterTriggerTest`, `DamageTriggerTest`,
  `LibraryTriggerTest`, `LifeTriggerTest`, `MiscTriggerTest`, `SagaTriggerTest`, `SpellAbilityTriggerTest`,
  `TapTriggerTest`, `TargetTriggerTest`, `TriggeredAbilityTest`): the asserts on `collectedAsAbility()` and on the
  `getLegacy…` getters throwing are gone with the API; `hasLegacyView()` false became `hasOwnEntry()` false, and
  `legacyEffects` throwing became `ownEntryEffects` throwing. `ActionTriggerTest#everyActionTriggerIsReadAsAnAbilityTheMiscActionsIncluded`
  is removed: it asserted only `collectedAsAbility()`.
- `ZoneChangeTriggerTest`: `onlyWhenThisEntersHasALegacyView` became `onlyWhenThisEntersIsReadAsItsEffects`,
  `anAbilityOfWhenThisEntersAndAnotherZoneChangeKeepsItsLegacyViewForTheOwnEntryOnly` became
  `…IsReadAsItsEffectsForTheOwnEntry`; new `whatTheEntersCollectorCannotReadIsRejectedAsTheAbilityIsBuilt`.
- `LifeTriggerTest#anEntersAndLifeGainAbilityIsReadAsItsEffectsForTheOwnEntryAndSharesItsLimit` (was
  `…KeepsItsLegacyView…`): the own-entry effect is the plain effect, the limit judged by the collector.
- `TriggerOptionsTest#theOwnEntryEffectsGiveTheEventConditionAsATriggerTimeOnlyConditionalInsideTheInterveningIf` (was
  `theLegacyViewGives…`): no limit wrapper outside.
- `CardAbilitiesTest`: `theOwnEntryEffectsWrapEachEffectInItsAbilitysInterveningIfOnce` (also the identity by ability)
  and `theOwnEntryEffectsLeaveALimitToTheCollector` (were `theLegacyView…`).
- `EtbTriggerServiceTest#oncePerTurnEtbSkipsAfterTheSourceHasTriggered` authored an unkeyed `OncePerTurnTriggerEffect`
  inside an own enters ability, a form no card writes and the collector no longer unwraps; it authors
  `TriggerLimit.ONCE_EACH_TURN` now and checks that the collector's own mark (by the ability's key) stops the second
  entry, where it set the mark by hand.

**Test runs.**

- Card: the test class of every card with a "when this enters" ability, printed or nested (by the scan; 4,768 classes,
  36,762 tests): all passing. The scan was run again on the new domain classes: every registered card class builds,
  so the new rejections reject no card.
- Application: the `model.trigger` and `service.trigger` packages and the card-abilities, triggered-ability, pipeline,
  ETB collector and resolver, enter-collector, granted-ability, battlefield-placement, spell-casting, targeting,
  card-view, ETB-token-target, stack-entry-target-group, creature-control, stack-resolution, freeze, simulation-copy,
  architecture, targeting-consistency, permanent-removal, combat-attack, turn-cleanup, step-trigger, aura-attachment,
  choice-handler, game-query, legacy-slot and zone-change classes (2,067 tests): 150 failures, all failing at HEAD
  (`PermanentRemovalServiceTest` 46, `BattlefieldPlacementServiceTest` 24, `StepTriggerServiceTest` 16,
  `TargetLegalityServiceTest` 14, `TurnCleanupServiceTest` 10, `SpellCastingServiceTest` 10,
  `DeathTriggerCollectorServiceTest` 6, `DamageTriggerCollectorServiceTest` 5, `ETBTokenTargetServiceTest` 4,
  `EtbEffectResolverTest` 3, `ValidTargetServiceTest` 2, `SpellCastTriggerCollectorServiceTest` 2,
  `LandTapTriggerCollectorServiceTest` 2, and one each in `GameQueryServiceTest`, `CreatureControlServiceTest`,
  `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest` and `CardEffectTargetingConsistencyTest`), but for
  `EtbTriggerServiceTest#oncePerTurnEtbSkipsAfterTheSourceHasTriggered`, rewritten above, which passes.
- Engine: the whole module, with the one failure at HEAD, `GameMutationBoundaryRatchetTest`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 16r: every triggered ability is one object on the stack, and `TriggerMode` is gone

The third of the "16p onward" runs: the own enters collector puts each "when this enters" ability on the stack as one
object (CR 603.3, checked) instead of pooling a card's plain abilities into one, the combat collectors that split
abilities by `TriggerMode.INDEPENDENT` split them by ability, and `TriggerMode` is deleted. Done in one run with the
user, the card merges and the combat collectors included.

**What the scan found.** 463 card classes had two or more printed "when this enters" abilities (16q counted 466 with
its reflection scan; the regex scan misses lists of triggers). About 360 were one Oracle ability written as one
`addTrigger` per effect, mostly `target(F).addTrigger(T, a).addTrigger(T, b)`, which the collector pooled; 53 marked
an ability of its own with `TriggerMode.INDEPENDENT`. The combat collectors pooled the same way: 109 card classes had
two or more "whenever this attacks" statements, 23 "whenever you attack", 8 "whenever a creature you control attacks"
(more with a filter), and `TriggerMode.INDEPENDENT` was on 11 combat statements and on the abilities Clone and token
copies add. `TriggerMode.ONCE_PER_BATCH` was read by one dispatch whose callers all passed "don't care", and the other
constants by nothing.

**The cards.** `scripts/step14-own-enters-ability-merge.py` (one `--family` per trigger) merges the statements of one
Oracle ability into one `addTrigger` of all their effects, in the constructor only, by the same trigger (a combat
trigger with the same `.withFilter(…)`) and the same options, leaving out `INDEPENDENT` ones and those of a keyword's
own trigger (evoke, living weapon, soulbond, exploit, champion, partner with). It merges a card only when its Oracle
text has one ability of that trigger (and no keyword line of its own ability: gift, CR 702.174b, checked; myriad,
exalted, …), else as its `MANUAL_GROUPS` say (Vibrance's {G}{G} ability, Finest Hour's second ability). An ability
whose effects target through different groups or none ("target player mills two cards. You gain 2 life") keeps each
effect's binding: the targeted effect becomes a local variable bound with `target(F).bindEffect(v)` in place, and one
`addTrigger` takes every effect (Watcher of the Wayside, Gurmag Rakshasa, Sting, Bilbo's Sword, …; 51 card classes). Merged: 384 card classes' "when this enters" abilities, 102 "whenever this attacks" (mobilize's
token creation and end-step sacrifice are one ability, CR 702.181a, checked), 21 "whenever you attack", 9 "whenever a
creature you control attacks", 4 "blocks", 4 "becomes blocked" and Revenge of Ravens. Shredder, Shadow Master's
"attacks a player" ability (its intervening "if" on one statement only) was merged on its own. `scripts/step14-trigger-
mode-removal.py` took `TriggerMode.INDEPENDENT` / `NORMAL` and the import out of 71 card classes.

**The model.**

- `TriggerMode` is deleted. `EffectRegistration` is `(effect, simultaneousWithPrevious)`; `Card.addTrigger(trigger,
  mode, …)`, `addSimultaneousTrigger(trigger, mode, …)`, `addEffect(slot, effect, mode)`,
  `addSimultaneousEffects(slot, mode, …)` and `SpellTarget`'s mode overloads are gone, and with them the rejections of a
  mode in `TriggeredAbility`, `SpellAbility` and the family readers, and the `mode` component of the readers' effect
  records (`CombatEffect`, `CounterEffect`, `DamageEffect`, `LibraryEffect`, `TargetEffect`).
- `Card.hasSeveralOwnEntryAbilities()` (new): more than one "when this enters" ability besides the evoke sacrifice. It
  replaces "has an `INDEPENDENT` registration" where an evoked permanent's abilities are marked with their triggering
  card, so its controller orders them with the sacrifice (CR 603.3b, checked; `EtbTriggerService`,
  `ETBTokenTargetService`, `PermanentChoiceTriggerHandlerService`). Emptiness is newly marked, Briarhorn no longer.
- `SpellTarget.bindEffect`'s Javadoc names the new use: an effect of an ability whose effects target through
  different groups.

**The engine.**

- `EtbTriggerService.processCreatureETBEffects` reads each printed and granted "when this enters" ability as an
  `OwnEntryAbility` (the ability and its own-entry effects). An ability that is one `MayEffect` takes the may paths, a
  trigger-time mode choice (CR 603.3c, checked) and the evoke sacrifice (CR 702.74a, checked) theirs, as before; every
  other ability's effects go on the stack as one object (`mandatoryEffects`, then `queueMandatoryETBEffects`), a "you
  may" among them asked as it resolves (CR 603.5, checked). A card's only ability takes the targets the entering
  event supplied, as the pooled object did; with several, `queueSeparateOwnEntryAbility` (the old per-effect
  `INDEPENDENT` path, generalized to an ability) gives each its target group's share, and they go on the stack in
  printed order, as the trigger pipeline puts a source's abilities there. A granted ability is one object with all its
  effects (it was one per effect). The cast-time mode's target filter goes only to the ability with the mode. The
  limit is marked for each ability that triggered.
- `CombatAbilityReader.byAbility` / `splitLaterAbilities` (new): the self attack collector (both branches), "whenever
  a creature you control attacks", "a creature attacks you" and "a player attacks" keep the source's first printed
  ability where they bundled its effects and put every later printed ability on the stack as an object of its own;
  "whenever you attack" bundles the first with the granted ones as before; the attached Aura and Equipment collector
  (`CombatTriggerService`) puts each ability on the stack as one object. `CombatEffect.independent()` is gone.
- The "whenever a creature you control attacks" collector judges an intervening "if" again after it takes the
  attacks-alone one off (Finest Hour's "if it's the first combat phase", now an intervening "if" in the card, CR 603.4,
  checked).
- `TokenCopySupport` adds a token copy's added abilities as they are; Clone's dethrone and the perpetual Island's draw
  are plain abilities; the copy handlers and `PermanentCopierService`, `MutationSupport`, `LayerSystemService` copy
  registrations without a mode. `TriggerCollectionService.dispatchRegistrations` lost its `oncePerBatchOnly` parameter.

**Behaviour changes** (rules fixes, CR 603.3).

- Separate "when this enters" abilities the collector pooled are separate objects: Squealing Devil, Emptiness, Night
  Clubber and Riveteers Provocateur (blitz's haste and sacrifice ability), Finch Formation, Fountainport Charmer,
  Intrepid Rabbit (offspring), and the cards whose keyword ability shares its trigger. Invasion of Ergamon's Treasure
  and "then you may discard a card" are one object, the may asked as it resolves.
- Separate combat abilities the collectors pooled are separate objects: Scurry of Squirrels' two myriad, Finest Hour,
  Angelic Benediction and Sovereigns of Lost Alara (exalted and their other ability), Barbed Foliage and Aurelia, the
  Law Above; Conclave Sledge-Captain's three backup abilities each choose their own target as
  they go on the stack.
- Finest Hour's second ability doesn't trigger in a later combat phase (CR 603.4).
- The order of a card's several abilities on the stack, which their controller chooses (CR 603.3b): with automatic
  ordering the last printed resolves first. The pooled object resolved its effects in printed order, the
  `INDEPENDENT` ones went below it; no order keeps both, so the tests that assumed one were changed (below).

**Kept, with their reason.**

- The may-unwrapping (the event bindings 16a listed and Pontiff of Blight, step 15), `ConditionalEffect`'s
  `interveningIf` / `triggerTimeOnly` flags, which the collectors and readers still get the intervening "if" and the
  event condition through, and the wrapper classes cards still author (16p's list, run 4c's `KEEP` cards).
- The block, becomes-blocked and combat damage collectors read abilities without `INDEPENDENT` and weren't regrouped;
  the merged block cards keep their pooled behaviour. A collector that pools still needs its family's statements of
  one ability merged.
- `scripts/step8-etb-scan.java` still names `TriggerMode`; it is a historical scan, not built.

**For 16s onward.**

- The may-unwrapping, `ConditionalEffect`'s `interveningIf` / `triggerTimeOnly` flags and the wrapper classes left, as
  16p and 16q listed. The own enters collector still gets the intervening "if" as a `ConditionalEffect` around each
  effect (`TriggeredAbility.ownEntryEffects()`).
- `scripts/step14-own-enters-ability-merge.py` takes a `--family`; another collector that starts grouping by ability
  needs its family added first.

**Docs.** `scripts/step14-trigger-mode-docs.py` rewrote the passages that named `TriggerMode` in the implement-card and
review-card skills (one Oracle ability is one `addTrigger`; an ability whose effects target through different groups
binds them with `bindEffect`), `EFFECTS_QUICK_REFERENCE.md`, `TRIGGER_SLOT_TARGETING.md`, `ACTIVATED_ABILITY_GUIDE.md`,
`CARD_IMPLEMENTATION_PLAYBOOK.md` (whose `ONCE_PER_BATCH` / `TOKEN_CREATION` examples were already stale) and
`ARCHITECTURE.md`'s token-copy note; `ARCHITECTURE.md`'s enters-collector, combat and tap passages were edited by hand.

**Tests changed.**

- `TriggerPipelineTest#plainWhenThisEntersAbilitiesShareOneStackObjectAndAnIndependentOneHasItsOwn` became
  `eachWhenThisEntersAbilityIsOneStackObjectOfItsOwn`: an ability of two effects is one object, a second ability
  another.
- `CombatTriggerTest#aRegistrationKeepsItsTriggerMode` became `theEffectsAreReadByAbilityAndKeepTheirSimultaneity`
  (`byAbility` tells two equal abilities apart, `splitLaterAbilities` keeps the first one bundled).
- Removed with the API they tested: `CounterTriggerTest#aRegistrationKeepsItsTriggerMode`,
  `SagaTriggerTest#theReaderRejectsATriggerMode`, `SpellAbilityTest#aSpellAbilityDoesNotTrigger`, and the trigger-mode
  rejections in `ZoneChangeTriggerTest`, `LifeTriggerTest`, `ActionTriggerTest`, `MiscTriggerTest` and
  `TapTriggerTest`. `CardAbilitiesTest`, `TriggeredAbilityTest` and `SpellAbilityTest` build `EffectRegistration`s
  without a mode.
- Card tests, for the separate objects: `SquealingDevilTest` (two cases: the sacrifice resolves on a pass of its own),
  `FinestHourTest` (two cases: exalted and the untap ability both resolve), `IntrepidRabbitTest` (offspring and the
  boost resolve before the token copy's target), `ConclaveSledgeCaptainTest` (the three backup targets are chosen
  before any resolves, `resolveBackupTargets`), `BroodlordTest` (one case) and `CourtHussarTest` (three: both
  abilities resolve before the choice), `HagHedgeMageTest` (one: the player target is chosen first, as its ability goes
  on the stack first), and `MarshCrocodileTest` (four) and `SawtoothLoonTest` (three), whose cases are about the
  return resolving first: they turn automatic ordering off and put the other ability on the stack first, as
  `DeceitTest` does.

**Test runs.**

- Card: the test class of every card with a "when this enters" ability, a combat trigger whose collector changed
  (attacks, "you attack", "a creature you control attacks", a creature attacks you, a player attacks, blocks, becomes
  blocked), an evoke sacrifice, a token copy, Clone or "becomes a copy" effect, and of every card the run changed
  (6,775 classes, 52,546 tests): all passing.
- Application: the whole module (5,025 tests), with a HEAD baseline in a worktree (with main's card-data cache): the
  same 621 failures at HEAD and here, none new. Most were outside the classes earlier runs took in (for example
  `CastingCostServiceTest` 44, `EffectResolutionServiceTest` 43, `ActivatedAbilityExecutionServiceTest` 35,
  `MayCastHandlerServiceTest` 30 and many effect-handler tests, Mockito wiring).
- Engine: the whole module, with the one failure at HEAD, `GameMutationBoundaryRatchetTest`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 16s: `ConditionalEffect`'s `interveningIf` / `triggerTimeOnly` flags are gone

The fourth of the "16p onward" runs, chosen with the user from the three leftovers: the flags. A `ConditionalEffect` is
now `(condition, wrapped)`, the "if" of an effect, checked as the effect resolves. A triggered ability's intervening "if"
(**CR 603.4**, checked: "this rule only applies to an 'if' that immediately follows a trigger condition") and its event
condition (**CR 603.2**, checked) are only its `TriggerOptions`, judged from the ability as it triggers. The may-unwrapping
stays (its 31 event bindings and Pontiff of Blight need step 15), and so do the authored wrapper classes 16p listed.

**What the scans found.** `scripts/step14-intervening-if-scan.py` (regex, every trigger statement of a card class with
a `ConditionalEffect`) found 440 statements: 87 with a flagged `ConditionalEffect` (the two-argument constructor, whose
flag defaulted to intervening) at the top of the ability, 170 nested, 213 `ConditionalEffect.unless`, 2
`atTriggerTime`. A temporary reflection scan (every registered card and back face, printed and nested abilities) found
the abilities with an intervening "if" or event condition by trigger family, which names the collectors that must judge
them from the ability: steps 634, zone changes 450 (mostly "when this enters"), combat 424, spells 52, counters 22,
library 14, life 12, damage 11, tap 5, player actions 5, state 4, land plays 4, permanent changes 1, activations 1.

**The cards** (`scripts/step14-intervening-if-flags.py`, rerunnable).

- `ConditionalEffect.unless(c, e)` became `new ConditionalEffect(c, e)` (242 calls in 196 card classes), and the
  three-argument constructor lost its flag.
- Exalted's "attacks alone" is the trigger event (**CR 702.83a**, **702.83b**, checked): `withInterveningIf(new
  AttacksAlone())` became `withEventCondition(new AttacksAlone())` in 82 card classes.
- By the Oracle text, 21 abilities whose flagged `ConditionalEffect` wrapped every effect took its condition as an
  option. As the intervening "if": Trail of Mystery, Persistent Marshstalker (its "with one or more Rats" became the
  event condition), Progenitor Mimic, Clockwork Dragon and Vorrac, Bridge from Below (both), Ashiok's token, Finest Hour
  ("attacks alone" became the event condition), Kozilek's Return and Phoenix of Iteration (the graveyard cast reader
  takes only an intervening "if"). As the event condition, the pseudo-filters of the trigger event: Weldfast Wingsmith,
  Path of Discovery, Carnival of Souls, Aether Flash ("this or another" enters), Throat Wolf, Twenty-Toed Toad's "with
  two or more creatures", Seifer's "by two or more creatures", Snarlfang Vermin, Kjeldoran Frostbeast.
- Its `MANUAL` table converted 19 abilities by hand: the intervening "if"s written inside a "you may" (Firemane Angel,
  Gatewatch Beacon, Ian the Reckless) or through a local variable (Bloodhall Priest, Naomi, Omnivorous Flytrap, Carpet of
  Flowers); the Oracle abilities written as several whose "Then if" / "if you control …" the old collectors judged as
  they triggered, now one ability (Hadana's Climb, Majestic Myriarch, Alacrian Armory, Does Machines); Stormbeacon Blade's "draw a card
  if you control three or more attacking creatures", an effect's "if" written as an option; Karai's "instead" and Typhoid
  Mary's "if you discarded a card this turn, you choose one instead" (its mode is chosen as the ability is put on the
  stack, **CR 700.2b**, checked), each an ability per branch with the branch as its event condition (one Karai ability
  with a shared graveyard target is a later fix).
- Every other `ConditionalEffect` in a triggered ability is an "if" of its effect by the Oracle text ("draw a card if
  …", "Then if …", "If …, instead …", "you may …. If you do"), checked as it resolves.
- Left as they are, their reasons in the script: Tunnel Ignus, Confounding Conundrum, Archaeomancer's Map and Invader
  Parasite (the "a land an opponent controls enters" collector judges their conditions by type against the entering
  land and its controller, which the pipeline's context lacks), Edgar Markov (the command-zone cast reader takes no
  options; the zone already says where it is) and Desert Were-Worm (the "you attack" collector counts its "for the first
  time each turn" through the condition around its `OncePerTurnTriggerEffect`). Sidar Jabari's eminence `ConditionalEffect`
  stays (16a), now checked as it resolves only, which its zones make equivalent.

**The model.** `ConditionalEffect(condition, wrapped)`; `unless`, `atTriggerTime` and the three- and four-argument
constructors are gone. `TriggeredAbility.ownEntryEffects()` wraps only the intervening "if" (a plain `ConditionalEffect`
around each effect); the event condition is the collector's to judge.

**The engine.**

- `ConditionEvaluationService.triggerConditionsMet(gameData, options, context[, eventValue])` judges an ability's
  intervening "if" and event condition; `judge(gameData, context[, eventValue])` gives a `TriggerJudge` (new,
  `service/trigger`) whose `filter` keeps the abilities that trigger, for the collectors that read flattened effect lists.
  `isInterveningIfMet` is gone. The readers gained judged overloads (`printedEffects(card, watch, judge)` on every family
  reader, `temporaryEffects` / `persistentEffects` for damage, `printedRegistrations` for tap and life).
- The family readers (step, damage, combat, counter, library, target, misc, action, life, tap, played-card-name, the
  pipeline) no longer wrap the event condition, and wrap the intervening "if" in a plain `ConditionalEffect` for the
  check on resolution. Every collector that judged a reader's wrapper by its flag judges the ability instead, with the
  context it used: `TriggerCollectionService.watchJudge` for the watches it dispatches (with the dice result, the damage to
  the controller and the life-gaining player as the event's value or "that player"), the counter, library, action and
  misc per-ability dispatches, the combat damage, block, becomes-blocked, attached Aura and Equipment, self-attack, "you
  attack", "a creature you control attacks", "a player attacks" (its event condition), graveyard and command-zone attack
  collectors, the damage collectors (dealt damage, deals damage, noncombat damage, excess damage, opponent dealt damage),
  draw, discard, cycle, monarch, land tap, dice, planar, exiled-card time counters. The `ConditionalEffect` handlers that
  judged a flag (discard, dice, land tap, energy, proliferate, crime, leave the graveyard, combat damage, damage) only
  route now.
- The pipeline-fed zone-change collectors (creature deaths, entering, leaving, landfall, the `LegacyTriggerBindings`)
  lost their re-checks: the pipeline judged the options. The pipeline reads a card source's own card for its options
  (graveyard abilities) and gives a permanent change the changed permanent ("if it's a creature", Trail of Mystery).
  On an ability triggered by an object entering, the intervening "if" goes inside a "you may" that targets nothing,
  whose collectors bind it to the entering object (Gatewatch Beacon).
- `EtbTriggerService` judges each "when this enters" ability's event condition and intervening "if" as it triggers,
  through `EtbEffectResolver.ownEntryConditionMet`, which reads a fact fixed as the permanent entered (kicked, the mana
  spent, a cost paid) the way the effects' own conditions are read; the resolver's per-effect handler (now a `Verdict`
  switch) judges only those facts and keeps any other condition for resolution. An effect "if" on the colour of mana spent
  leaves a no-op effect, so the ability still triggers (Plaxmanta, Court Hussar).
- `StepTriggerService` dropped the re-checks of what `StepAbilityReader` judged, and two upkeep branches (`AllOf`,
  `ControlsPermanentCountAtMost`) that judged flagged conditionals; beginning of combat judges only "Max speed —" as the
  ability triggers.
- The self-attack collector judges each ability with the attacker, the number of attackers and the attack target, and
  takes the pack tactics wrapper off after judging it (the attackers as declared); `CombatAttackService`'s flagged
  filters and the trigger-time-only unwrap are gone, and so is `EffectResolutionService`'s "attacks alone" exception.
- `EffectResolutionService` checks every `ConditionalEffect` as it resolves (no trigger-time-only skip). It skips a "you
  may" around its ability's intervening "if" once the "if" is false (CR 603.4), and recognises one by identity: the
  readers wrap the ability's own `TriggerOptions.interveningIf()` object, which is one of the source card's
  (`isInterveningIfOf`); a "you may" around an effect's "if" is asked (**CR 603.5**, checked) and the "if" then decides.
  `EnterTriggerCollectorService`'s pre-check of the latter kind is gone.
- State triggers judge every conditional of a `StateTriggerEffect` (the reader's intervening "if"); Syr Vondam's exile
  half (`SelfExiledFromBattlefieldEffect` around a power condition) is judged as it triggers, as before.
- "Whenever an opponent gains life" has a default handler (Wedding Ring's ability reached it only as a wrapper).
- The spell-copy event binding with an intervening "if" (`CopyControllerCastSpellOnSpellCastEffect.withIntervening`,
  Pyromancer Ascension) puts it around its "you may", so the choice isn't asked once it is false.

**Behaviour changes** (rules fixes, **CR 603.4**).

- An "if" of an effect that cards wrote with the flagged constructor is checked as the ability resolves, where some
  collectors checked it as the ability triggered: the ability goes on the stack and does nothing if it is false then
  (Laboratory Drudge, Aggression, Ludevic, Firemane Commando, Leaves from the Vine, Summon Fenrir, The First Iroan Games,
  Twenty-Toed Toad's win, Rayne, Gamekeeper, Academy Rector, Creeping Chill, Canoptek Wraith, Family's Favor's grant,
  Tellah, …).
- A "you may" around an effect's "if" is asked, then the "if" decides (Deathbringer Liege, The Hippodrome, Leviathan,
  Old One-Eye, Undead Butler, Gogo, Gitaxian Anatomist; the energy and oil "you may pay" cards already asked).
- Seifer's intervening "if" ("attacking one of your opponents") is checked as it triggers too; the event conditions of
  the pseudo-filters above are no longer checked again as they resolve.

**Kept, with their reason.** The may-unwrapping (step 15). The authored wrapper classes 16p listed
(`OncePerTurnTriggerEffect`'s keyed and per-grant forms, the `Triggering…ConditionalEffect` filters,
`AllyCombatDamageTriggerEffect`, `SpellCastTriggerEffect`, `EvolveTriggerEffect`, `ChampionOfTheHareishTriggerEffect`,
Olivia's `StateTriggerEffect`, `SelfExiledFromBattlefieldEffect`). The collectors that judge an authored conditional by
its condition's type as the ability triggers (the upkeep life, counter and library branches, the self-attack cascade,
the "a player attacks" fan-out, the opponent-land collector's land conditions, `EtbEffectResolver`'s cast-time facts):
they read facts of the event the effects can't read yet (step 15).

**For 16t onward.** The wrapper classes left (above), then step 15. A collector that judges an authored conditional by
type keeps the card's condition out of `TriggerOptions` until its event is readable.

**Docs.** `scripts/step14-intervening-if-flags-docs.py` wrote the rule (a `ConditionalEffect` is an effect's "if"; an
intervening "if" is `withInterveningIf`, an event condition `withEventCondition`) into the implement-card skill,
`EFFECTS_QUICK_REFERENCE.md` and `CARD_IMPLEMENTATION_PLAYBOOK.md`'s conditions table, renamed every `unless` form and
the "Intervening-if wrapper" rows of `EFFECTS_INDEX.md`; `ARCHITECTURE.md` was edited by hand; the readers' class
Javadocs were rewritten.

**Tests changed.**

- Reader tests (`ActionTriggerTest`, `CombatTriggerTest`, `CounterTriggerTest`, `LibraryTriggerTest`, `LifeTriggerTest`,
  `MiscTriggerTest`, `TapTriggerTest`, `TargetTriggerTest`): `…EventConditionIsInsideTheInterveningIf` became
  `theReaderWrapsOnlyTheInterveningIfAndLeavesTheEventConditionToTheCollector`; `TriggerOptionsTest`'s own-entry test
  likewise (`theOwnEntryEffectsWrapOnlyTheInterveningIfAndLeaveTheEventConditionToTheCollector`). The other model and
  service tests build `ConditionalEffect`s without a flag.
- `EtbEffectResolverTest`: the Metalcraft, Morbid, Raid, ControlsAnotherPermanent and sacrificed-card gates assert
  `ownEntryConditionMet` as the ability triggers and that `resolve` keeps the conditional.
- Handler tests whose handler no longer judges (the dispatch does): `DamageTriggerCollectorServiceTest` (three "does not
  queue" cases removed, stubs dropped), `DiscardTriggerCollectorServiceTest`, `MiscTriggerCollectorServiceTest` (two
  "skips" cases removed), `EnterTriggerCollectorServiceTest` (`allyCreatureSourceCounterConditionalGatesAtTriggerTime`
  removed), `LandTapTriggerCollectorServiceTest` (`skipsConditionalSequenceWhenConditionFails` removed).
  `StepTriggerServiceTest`: fifteen abilities author their intervening "if" as `TriggerOptions`. `DrawServiceTest` stubs
  `triggerConditionsMet` on its mocked service.
- Card tests: `TheHippodromeTest#chaosDoesNotDestroyCreatureWhosePowerIsAboveZeroAtResolution` answers the "you may"
  that is now asked.

**Test runs.**

- Card: the test class of every card with a `ConditionalEffect`, an intervening "if", an event condition, "attacks
  alone" or a "you may", every card in the reflection scan and every card the run changed (5,506 classes, 47,360 tests),
  after a first run of 3,722 classes whose 101 failures led to the fixes above: 6 failures, all fixed and rerun with the
  173 classes of the cards with an intervening "if" and a "you may" or an intervening event binding (1,720 tests): all
  passing.
- Application: the whole module (5,016 tests) against a HEAD baseline in a worktree (with main's card-data cache): the
  same 621 failures at HEAD less 3 that now pass (`DeathTriggerCollectorServiceTest$AnyCreatureDeathHandlers`), none new.
- Engine: the whole module, with the one failure at HEAD, `GameMutationBoundaryRatchetTest`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 16t: the trigger-only wrapper classes are gone

The fifth of the "16p onward" runs, its scope chosen with the user: every trigger-only wrapper class left. Deleted:
`TriggeringCardConditionalEffect` (and its handler), `TriggeringPermanentConditionalEffect`,
`TriggeringPermanentControllerConditionalEffect`, `TriggeringArtifactControllerConditionalEffect`,
`TriggeringAbilityFromNamedLandConditionalEffect` (and `CardEffect.resolveForBecomesTargetOfSpellOrAbility`),
`SelfExiledFromBattlefieldEffect`, `OncePerTurnPerCreatureTriggerEffect` (no card wrote it) and
`BlockedCreatureTriggerEffect` (only `TriggeringCardConditionalEffect` implemented it). `OncePerTurnTriggerEffect` is
narrowed to `(CardEffect wrapped)`, Bill Potts' (below); its keyed, `perGrant`, `markOnAcceptance`, `firstTokenCreation`
and `firstLandGraveyardEvent` forms are gone.

**What the scan found.** 53 card classes named a wrapper: 49 authored one, 4 only imported or named it in a comment.
`AllyCombatDamageTriggerEffect`, `EvolveTriggerEffect`, `ChampionOfTheHareishTriggerEffect`, `SpellCastTriggerEffect`
(emblems) and Olivia's `StateTriggerEffect`, which 16s listed with the wrappers, are event bindings or resolution effects,
not trigger-only wrappers: step 15. So is `TriggeringPermanentEntryExclusionEffect`, a capability of
`PutCardToBattlefieldEffect` that the enter collector reads (it isn't dead, as the run's scoping question said). The
engine itself built wrappers in four places: Clone's extra dethrone (`CloneService`), Zombie Boa's chosen-colour grant,
the land-play reader's re-check of the Cemetery cards' type (`PlayedCardNameTriggerCollectorService`) and the damaged-
creature collector's "do this only once each turn" target choice (`DamageTriggerCollectorService`).

**The cards** (`scripts/step14-wrapper-classes.py`, rerunnable; 48 card classes, and 4 stale imports or comments).

- A filter on what triggers the ability is the trigger's: `withFilter(…)` on combat, damage and target triggers, the
  zone change's filter, `PermanentControlledBySourceControllerPredicate` in the filter for "a … you control" on the
  any-creature attack and combat-damage triggers (Swordsman, Nahiri, Kestia, Etrata, Edward Kenway; the saga chapters of
  The Revelations of Ezio, Harald Unites the Elves and The Last Ronin, whose "attacks alone" is its event condition).
  Tattered Ratter, Spined Sliver, Berserk Murlodont, Senu, The Master, Kami of Mourning (new
  `PermanentManaValueGreaterThanSourceCardManaValuePredicate`), Disa (`putIntoGraveyardFromAnywhereButBattlefield`, in
  place of its `CardPutIntoGraveyardFromNonBattlefieldThisTurnPredicate` re-check), Agyrem, Mirror Shield ("blocks or
  becomes blocked by a creature with deathtouch" is the filter on the creature on the other side, CR 509.3f, checked).
- "When this is put into exile from the battlefield" is `Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, THIS)` (the
  four God-Eternals, Ilharg, Urza's Sylex, Slagstone Refinery, Psychomancer, Kaya's and Ashnod's Intervention's grants,
  Elemental Expressionist's); Syr Vondam's "while its power is 4 or greater" is the filter on the source, as its dies half.
- "If it wasn't sacrificed" (Urza's Miter) is `Triggers.diesWithoutBeingSacrificed(ARTIFACT, YOU_CONTROL)`, every
  cause but a sacrifice (CR 701.21a, checked): whether it was sacrificed can't change once it died.
- Zagras' "deals combat damage to a planeswalker" is `Triggers.creatureYouControlDealsCombatDamageToPlaneswalker()`
  (new `DamageEvent.DEALS_COMBAT_DAMAGE_TO_PLANESWALKER` and `DamageWatch`, read by the planeswalker damage collector
  for combat damage only), where the wrapper's `combatDamageOnly` flag said it.
- Skophos Maze-Warden's "an ability of a land you control named Labyrinth of Skophos" is the target trigger's new
  `source` filter (`TargetTrigger.withSource`), which only the any-creature becomes-the-target collector reads (the
  reader rejects it elsewhere); "another creature" its filter.
- Merfolk Tunnel-Guide's boon filter is the boon's (`CreateBoonEffect` / `Boon` gain a `CardPredicate filter`).
- An "if" of an effect on the triggering card is `ConditionalEffect(new TriggeringCardMatches(p), effect)` (new
  condition), checked as it resolves against the stack entry's triggering card: Venom, Overgrowth Elemental, Jenson,
  Eshki, Oathkeeper ("if it's a Samurai card"), the Cemetery cards' re-check of the exiled card's type.
- Oran-Rief Hydra's and Emeria Shepherd's "if that land is a Forest (Plains) … instead" were judged as the land entered;
  each branch is an ability whose trigger is filtered by the land (`AllOf(LAND, forest)` / `AllOf(LAND, not forest)`),
  so the land is still judged as it entered and one of the two triggers. Making the "instead" an effect "if" read on
  resolution needs the landfall entry to keep the land's last-known subtypes (step 15).
- "Triggers only once each turn": Well Rested's granted ability `TriggerLimit.ONCE_EACH_TURN` (one instance per
  granting Aura, `TriggeredAbility.instance()`), Dennick's two conditions one ability with the limit.
- "For the first time each turn" (new `TriggerLimit.FIRST_TIME_EACH_TURN`, counted whether or not the source was there
  for the earlier events): Akim, Crawling Sensation, Desert Were-Worm (its total power the event condition, CR 603.2,
  checked). Only the token-creation, land-to-graveyard and "whenever you attack" collectors read it; the pipeline leaves
  it to them, `TriggeredAbility` rejects it on a trigger other than a zone change or combat event, and
  `triggerZoneChange` throws on any other zone-change watch.
- Beifong's Bounty Hunters' unused `SELF_DEATH_TRIGGER` constant is gone.

**The engine.**

- `EffectResolutionService` gives a `TriggeringCardMatches` "if" the entry's triggering card (`findCardById`), as the old
  handler read it.
- The collectors that unwrapped a wrapper judge the trigger's filter instead, or lost a branch no card reached any more:
  the any-creature attack collector, the ally attack collector, the turn-scoped "whenever a creature attacks this turn"
  reader (now with the watcher's point of view for the filter and the ability's options), the combat-damage-to-opponent
  and to-owner collectors, the planar attack and creature-death readers (Agyrem's colour judged on the creature as it
  last existed: `ZoneChangeAbilityReader.printedNarrowing`, `TriggeredAbilityStackService.departedFilterMatches`), the
  unblocked collectors (the exile one reads Senu's filter), the any-creature becomes-blocked collector (Spined Sliver),
  the becomes-blocked collectors' granted abilities (Mirror Shield's continuous grant, Zombie Boa's temporary one: a
  filtered one is an ability of its own for each blocker it admits, CR 509.3d, checked), the equipped-creature death
  collector (Oathkeeper's return bound to the dying card and its "if" read on resolution), the boon collector, the
  granted becomes-untapped collector (Well Rested's limit judged by the ability), the token-creation and
  land-to-graveyard collectors (`FIRST_TIME_EACH_TURN`), the "you attack" collector (Desert Were-Worm), and the
  pipeline's leaves-the-battlefield path, which marks the exile entry version on an exile-only own trigger (CR 400.7,
  checked) where the wrapper's handler did. The dead helpers went with them (`unwrapTriggeringCardConditional`,
  `unwrapCreatureDeathConditional`, `resolveTriggeringPermanentConditional`, `branchOnEnteringLand`,
  `OncePerTurnTriggerSupport`'s keyed marks, `GrantTriggeredAbilityEffectHandler`'s per-grant key).
- `PermanentChoiceContext.SpellTargetTriggerAnyTarget` carries `markSourceOncePerTurnOnAcceptance`, where the damaged-
  creature collector wrapped its effect in a mark-on-acceptance `OncePerTurnTriggerEffect`.
- Clone's extra dethrone is `Triggers.attacks()` with the event condition "attacks the player with the most life, while
  it has dethrone" (CR 702.105b, checked); Zombie Boa's grant filters its becomes-blocked trigger by the chosen colour.

**Behaviour changes.** None intended. Two of the Triggering… collectors judged a subtype by the permanent's effective
subtypes and the filters do too (`PermanentHasSubtypePredicate`); Oathkeeper's "Samurai card" is read on the card in the
graveyard as it resolves, which the old collector read from the printed card as it triggered (the same card).

**Kept, with their reason.**

- Bill Potts' `OncePerTurnTriggerEffect`: its one ability has two trigger conditions (a cast and an activation) whose
  effects are two different event bindings, so it can't be one `TriggeredAbility` with one limit until an effect copies
  "that spell or ability" from the event (step 15). The cast and activation collectors still read the narrowed wrapper.
- The event bindings and resolution effects above, the may-unwrapping (16s's 31 bindings and Pontiff of Blight), and
  the conditionals some collectors judge by type as the ability triggers (16s's list): step 15.

**For step 15.** Bill Potts' wrapper; Oran-Rief Hydra's and Emeria Shepherd's "instead" as an effect "if" on the land's
last-known subtypes; `TriggeringPermanentEntryExclusionEffect`; then 16s's list. Step 14 is closed, with the user: Bill Potts'
wrapper and the may-unwrapping moved to step 15's section.

**Docs.** `scripts/step14-wrapper-classes-docs.py` replaced the deleted classes' entries in `EFFECTS_INDEX.md` and
`EFFECTS_QUICK_REFERENCE.md` with the trigger filters, limits and `TriggeringCardMatches`, rewrote the rows and patterns
that named them to the cards' current forms (`ORACLE_TEXT_EFFECT_MAP.md`, the `CARD_PATTERN*` files,
`ACTIVATED_ABILITY_GUIDE.md`, `TRIGGER_SLOT_TARGETING.md`, `PREDICATES_REFERENCE.md`, `ARCHITECTURE.md`; many already
described cards earlier runs converted) and dropped the deleted classes from the implement-card and review-card skills'
"never wrap" lists.

**Tests changed.**

- `DamageTriggerCollectorServiceTest`: `doesNotQueueCombatOnlyPlaneswalkerTriggerForNoncombatDamage` removed (the
  collector, not the handler, leaves noncombat damage out now; `ZagrasThiefOfHeartbeatsTest` covers it);
  `queuesCombatOnlyPlaneswalkerTriggerForCombatDamage` dispatches the plain effect on the combat watch; the
  "do this only once each turn" target-choice test asserts the choice's mark in place of the wrapper.
- `DeathTriggerCollectorServiceTest`: the four `TriggeringArtifactControllerConditionalEffect` cases test the artifact
  default handler with the plain effects; the "opponent's artifact" case is removed (the trigger's relation says it).
- `MiscTriggerCollectorServiceTest`: the Oasis of Renewal card-conditional case is removed with its dead handler; the
  Nykthos Paragon case passes the plain effect.
- New `DennickPiousApparitionTest` (the card had none): its one ability triggers for your and an opponent's creature
  cards, only once each turn across both.

**Test runs.**

- Card: the converted cards and every card read by a collector or reader the run changed (attack, block, becomes-
  blocked, combat damage, damage to a planeswalker, becomes-the-target, token creation, land and creature cards to a
  graveyard, "you attack", untap, boons, exile-from-the-battlefield, dethrone, sagas' registered abilities, planes,
  equipped-creature deaths, artifact deaths, `ConditionalEffect`s and limits; 4,073 classes, 34,239 tests): all passing,
  after a first run of the 57 converted cards' classes whose 27 failures (Spined Sliver, Berserk Murlodont, Mirror
  Shield, Zombie Boa) led to the becomes-blocked collectors reading filters.
- Application: the whole module (5,013 tests) against a HEAD baseline in a worktree (with main's card-data cache): the
  same 618 failures at HEAD and here, none new.
- Engine: the whole module, with the one failure at HEAD, `GameMutationBoundaryRatchetTest`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

### Step 15

#### Run 1: the dying card

Step 15 had no run table; run 1's scope was chosen with the user from three proposals (the departed object's last-known
information, its counters only, or the dying card): `DyingCreatureCardAwareEffect`, the binding of "that card" / "it"
of a death or other graveyard arrival into the effect. Effects now read the stack entry's triggering card as they
resolve, and the interface, its `boundToDyingCard` / `boundToDyingCardEntryVersion` and every collector's binding are
gone. The other 24 `…AwareEffect` interfaces are untouched.

**What the scan found.** 33 classes implemented the interface: 30 effects with a bound id (`dyingCardId`,
`returnedCardId` on `SuspectReturnedPermanentEffect`, `cardId` and `graveyardEntryVersion` on
`RegisterDelayedReturnCardFromGraveyardToHandEffect`, `dyingCardGraveyardEntryVersion` on the two return-to-battlefield
effects) and three delegators (`MayPayLifeEffect`, `SacrificeSelfThenEffect`, `SequenceEffect`). About 35 binding sites
in `TriggerCollectionService`, `DeathTriggerCollectorService`, `LegacyTriggerBindings`, `ManifestDreadTriggerCollectorService`
and `ResolveEffectOnTargetDeathThisTurnEffectHandler`, plus the engine-built bound copies (Tiana's optional return, the
emblem's artifact return, Nim Deathmantle's and Vraska's rebuilt may-pays). A card scan (every card class naming one of
the effects, by statement) found one targeted ability that carries a dying-card effect, Illicit Masquerade ("exile it.
Return up to one other target creature card"), whose target is chosen as the ability goes on the stack.

**The model.**

- The effects lose the bound id: 15 records have no component left (`scripts/step15-dying-card-records.py`), the rest keep
  their other components (`ReturnTriggeringCardToOwnerHandEffect(UUID handOwnerId)`,
  `ReturnDyingCreatureToBattlefieldEffect(boolean attachSource, CounterType, int)`,
  `RegisterDelayedReturnCardFromGraveyardToHandEffect(boolean optional)`, …); `ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect`
  loses its "other" filter, which only a bound id switched on and no card reached (Nethroi and Reunion of the House
  aren't death triggers).
- `StackEntry.setTriggeringCard(cardId, graveyardEntryVersion)` and `isTriggeringCardIncarnation(version)` (new): the
  card the event was about and its graveyard incarnation (**CR 400.7**, checked), copied with the entry as before.
- `PermanentChoiceContext.DeathTriggerTarget` gains `triggeringCardId` / `triggeringCardGraveyardEntryVersion`
  (`withTriggeringCard`, `carryTriggeringCardTo`), and `GraveyardTargetOperationState` the same two fields (copied in
  `GameData`'s simulation copy), so a death trigger whose targets are chosen as it goes on the stack still gives its
  entry the card: every entry `TriggeredAbilityQueueService` and `PermanentChoiceTriggerHandlerService.handleDeathTrigger`
  build from the context, and the graveyard-target path through `GraveyardChoiceHandlerService`. The multi-target
  (`ETBTokenMultiTargetTrigger`) and counter-distribution paths don't carry it; no dying-card effect reaches them.

**The engine.**

- The handlers read `entry.getTriggeringCardId()` where they read the bound id, and judge the incarnation through
  `isTriggeringCardIncarnation` where they judged the bound version. Fallbacks stay: The Locust God's delayed return and
  `ReturnSourceAsAuraEffect` take the source card when the entry has no triggering card, `SuspectReturnedPermanentEffect`
  the spell's target (It Doesn't Add Up), Endless Whispers' `forOpponent` return the source card.
- Each collector that bound the card sets it on what it queues instead (`markDyingCard` in `DeathTriggerCollectorService`
  and `LegacyTriggerBindings`, `setTriggeringCard` in `TriggerCollectionService`, the pipeline's `triggerAll`), with the
  id the binding used (for a copy that died through the pipeline, the physical card). The may-ability entries
  (`queueMayAbility`, then the entry on top of the stack) carry it, so the old reason to bind ("the may flow drops the
  entry's triggering-card id") is gone with the binding: since run 16a a "you may" resolves on the stack entry.
- Handlers that only bound the card are gone, the default handler of their watch doing the rest: the equipped-creature
  exile-and-track, and the "nontoken creature you control dies" exile-and-track, Spirit token copy and delayed return.
  `bindDyingCard`, `bindAllyNontokenDyingCard` and the may-pay rebuilds' bindings are gone; Nim Deathmantle's rebuilt
  `MayPayManaEffect` keeps the card's own return effect (it rebuilt one without its counter rider).

**Behaviour changes.** No card test changed.

- CR 400.7: the return effects that checked the bound graveyard incarnation only on some paths
  (`ReturnDyingCreatureToBattlefieldEffect`, `ReturnDyingCreatureToOwnerBattlefieldEffect`, the delayed hand return) now
  check the entry's on every path that records one; the equipped-creature returns (Oathkeeper, Sword of the Realms),
  the "any creature" and opponent-creature entries and the one-or-more death entries now record it. A card that left
  the graveyard and came back before the ability resolved is a new object and isn't returned.
- The "whenever a creature an opponent controls dies" and "whenever a creature dies" "you may" abilities give their
  entry the dying card, which no collector bound before (no card is known to have needed it).

**Kept, with their reason.** The other `…AwareEffect` interfaces, the may-unwrapping (the creature-death dispatch still
hands a may's inner effect to its handler), Bill Potts' `OncePerTurnTriggerEffect`, the `…Triggering…` classes and the
other effects with a baked `dyingCardId` that never implemented the interface (`BecomeCopyOfDyingCreatureEffect`,
`ReturnDyingOpponentCreatureUnderYourControlEffect`, `ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect`,
`StealDyingOpponentPermanentUnlessPaysLifeEffect`, `ExileTriggeringCreatureAndReturnSourceToHandEffect`): later runs.

**For later runs.** The grouping proposed with run 1, by the event data the effects read: the departed object's
last-known information (`DyingCreatureCounter(s)`, `DyingCreatureName`, `DyingCreatureManaValue`,
`DyingCreaturePermanent`, `DyingCreatureAttachments`, `LeavingCreatureCounters`, `LeavingCreatureName`,
`LeavingPermanentCounters`, `LeavingPermanentId`, `ArtifactGraveyardCounters`, `SacrificedPermanentManaValue` /
`SacrificedPermanentCard`); damage and combat (`CombatDamageAmount`, `CombatDamageDealer`, `CombatExcessDamage`,
`DamageSource`, `DamageSourceController`, `AttackingCreatures`); the bindings that aren't trigger events
(`ChosenCard`, `AcceptedPlayers`, `GrantingPermanent`, `ProducedManaColor`, `TriggeringCards`); then Bill Potts, the
may-unwrapping and the `…Triggering…` classes. The baked-id effects above can follow this run's pattern: the entry
already carries the card on the paths that bind them.

**Scripts.** `scripts/step15-dying-card-records.py` (the single-component records), `scripts/step15-dying-card-cards.py`
(per letter: `RegisterDelayedReturnCardFromGraveyardToHandEffect(null)` became the no-arg constructor and two stale
comments, 9 card classes: Agyrem, Liesa, Mangara's Blessing, Pure Intentions, Rienne, The Locust God, The
Scarab God, The Scorpion God, Tiana), `scripts/step15-dying-card-docs.py` (exact replacements in `EFFECTS_INDEX.md`,
`EFFECTS_QUICK_REFERENCE.md`, `ORACLE_TEXT_EFFECT_MAP.md` and two `CARD_PATTERNS` files); the other records, the
handlers and the collectors were edited by hand, and `ARCHITECTURE.md` gained a note on the triggering card. The skills
didn't name the interface.

**Tests changed.**

- `DeathTriggerCollectorServiceTest`: the equipped return, token-copy, nontoken may-pay, imprint and opponent-default
  cases assert the entry's triggering card in place of the effect's bound id; the Tiana case (failing at HEAD: it
  expected a `MayEffect` the handler hasn't built since the optional return moved into the effect) asserts the
  optional delayed return and the triggering card, and passes; its other two cases only lost the `null` argument.
- `LegacyTriggerBindingsTest#transformedDeathBindsThePhysicalCardId` became
  `transformedDeathGivesThePhysicalCardAsTheTriggeringCard`.
- `ImprintDyingCreatureEffectHandlerTest`: the five cases set the entry's triggering card in place of the effect's id.

**Test runs.**

- Card: the test class of every card with a death, sacrifice, graveyard-arrival, manifest-dread or discarded-this-card
  trigger or naming one of the changed effects (1,640 card classes), and every card test that constructs one
  (2,893 classes, 24,062 tests): all passing.
- Application: the whole module (5,013 tests) against a HEAD baseline in a worktree (with main's card-data cache):
  617 failures here, 618 at HEAD, none new; the Tiana case above is the one fixed.
- Engine: the whole module, with the one failure at HEAD, `GameMutationBoundaryRatchetTest`.
- AI: the whole module (1,620 tests), with the same 10 failures as before.
- The full suite was not run here.

#### Run 2: the departed permanent's last-known information

Run 1's "for later runs" listed this group first; the user chose it whole over its counters-only part. The 13
interfaces that bound the last-known information of the permanent a death, departure or sacrifice was about into the
effect are gone: `DyingCreatureCounter(s)`, `DyingCreatureName`, `DyingCreatureManaValue`, `DyingCreaturePermanent`,
`DyingCreatureAttachments`, `LeavingCreatureCounters`, `LeavingCreatureName`, `LeavingPermanentCounters`,
`LeavingPermanentId`, `ArtifactGraveyardCounters`, `SacrificedPermanentManaValue` and `SacrificedPermanentCard`
`…AwareEffect`, with every `boundTo…` method and every collector binding. Effects read the stack entry's triggering
permanent as they resolve. 11 `…AwareEffect` interfaces are left.

**What the scan found.** 25 effect classes implemented them, plus four delegators (`MayEffect`, `SequenceEffect`,
`MayPayManaEffect`, and `ReturnCardFromGraveyardEffect`, whose multi-line `implements` the first scan missed). About 50
binding sites in `TriggerCollectionService`, `DeathTriggerCollectorService`, `LegacyTriggerBindings`,
`MiscTriggerCollectorService`, `CommanderZoneTriggerCollectorService`, plus the two "unbind to find the target group"
helpers in `TriggeredAbilityQueueService` and `PermanentChoiceTriggerHandlerService`. Several bindings were also the
trigger's condition: a collector skipped the trigger when the snapshot was empty (The Ooze, Donatello, Reyhan's
command-zone half, Sporogenesis, Dual Nature's tokens, Shadow Urchin's own death).

**The model.**

- `StackEntry.setTriggeringPermanent(lastKnown[, DepartedAttachments])` (new) keeps a copy of the permanent the event
  was about as it last existed on the battlefield (**CR 603.10a**, **CR 608.2h**, checked) and what was attached to
  it; `getTriggeringPermanentSnapshot()`, `getTriggeringAttachments()`, `triggeringPermanentCounters()` (counter-type
  order) and `triggeringPermanentCounterCount(type or null)` read it. The copy constructor deep-copies it, so the
  simulation copy does (`GameDataDeepCopyTest` case added).
- `CountersOnTriggeringPermanent(CounterType)` (new `DynamicAmount`, null = every counter) reads it for amounts.
- Targeted triggers carry it to the entry: `DeathTriggerTarget` gains `triggeringPermanent` / `triggeringAttachments`
  (`withTriggeringPermanent`; `carryTriggeringCardTo` became `carryTriggeringObjectTo` and carries both),
  `SelfTriggeredAbilityTarget` and `EntersTriggerTarget` gain `triggeringPermanent` (`withTriggeringPermanent`), and
  `SpellTargetTriggerAnyTarget` gains `departedPermanent` (`withDepartedPermanent`; its existing
  `triggeringPermanentSnapshot` is the permanent an Aura source was attached to, so the name differs). Every entry those
  contexts build carries it, the auto-chosen, no-target and random-target paths included.
- `TriggerContext.ArtifactGraveyard` holds the artifact's last-known `Permanent` instead of a counter map, and
  `CommanderPutIntoCommandZone` the departed commander; `checkEmblemArtifactGraveyardTriggers` and
  `triggerDelayedEffectOnDeath` take the permanent instead of a counter map; `bindSelfDeathEffect` lost its
  attachments argument and `bindDyingCreatureAttachments` is gone.

**The effects.**

- Lost their bound component and now read the entry: `RegisterDelayedTokenCopyEffect()`,
  `TransformSelfWhenChosenPermanentLeavesEffect()`, `SearchLibraryForSameNameAsDyingCreatureToHandEffect()`,
  `RevealUntilCreatureSharingTypeWithDyingCreatureToHandEffect()`, `ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect()`,
  `RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect(filter)` (the entry's triggering card; the library
  "one or more" path gives each card's entry its card), `PutPlusOnePlusOneCountersOnTargetForEachDyingSourceCounterEffect()`,
  `PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect()` (the entry's existing `dyingPermanentManaValue`),
  `PutLeavingCreatureCountersOnSourceEffect()`, `PutCountersOnTargetForEachLeavingSourceCountersEffect(predicate)`,
  `PutCounterOnTargetForEachDyingSourceCounterEffect(type, optional, predicate, modular)`,
  `MoveDyingSourceCountersToTargetCreatureEffect(controllerOnly, requiresCounters)`, `MoveDyingSourceCountersToSourceEffect()`,
  `ExileTokensWithSameNameAsLeavingCreatureEffect()`, `ExileTargetPlayerTopCardsForSacrificedManaValueMayPlayUntilNextEndStepEffect()`,
  `ExileSacrificedPermanentWithSourceEffect(counterIfToken)`, `CreateTokenWithDyingSourceCounterPTEffect(template, type)`,
  `CreateTokenCopyOfSacrificedPermanentEffect(amount)` (a triggering permanent means the sacrifice-trigger form, none
  the activation-cost form), `ConjureDuplicatesOfDyingCreatureIntoLibraryEffect()`.
- Gone, replaced by existing effects with the new amount (AGENTS.md's reuse rule):
  `CreateTokensForEachDyingSourceCounterEffect` and `CreateTokensForEachLeavingSourceCounterEffect` became
  `template.withAmount(new CountersOnTriggeringPermanent(type))` (`CreateTokenEffect.withAmount(DynamicAmount)` is new
  and keeps the simultaneous tokens), and `ExileTopCardsForEachDyingCreatureCounterMayPlayUntilNextEndStepEffect`
  became `ExileTopCardsMayPlayUntilNextEndStepEffect(new CountersOnTriggeringPermanent())`, whose count is now a
  `DynamicAmount` (an `int` constructor keeps the other cards).
- `ReturnTargetCreatureCardsFromGraveyardToBattlefieldWithTotalPowerEffect`'s binding to the dying creature's power was
  dead (no death trigger has it) and is gone.
- `ReturnCardFromGraveyardEffect`'s sacrifice binding became the plain `withManaValueLessThan(int)`, which the sacrifice
  collector applies to a graveyard-targeting return (Riveteers Ascendancy's "lesser mana value"): its target is chosen
  before there is a stack entry, so this one still narrows the effect at trigger time.

**The engine.** Each collector that bound the information sets the triggering permanent on what it queues: the
self-death and self-leave paths of `LegacyTriggerBindings` (`markDyingCard` / `withDyingCard` now also give the
permanent and its attachments, the leave default and the may-pay death path set it), the pipeline's `triggerAll` for a
departure, the ally / batched ally / equipped / any / nontoken / enchanted-player death defaults and the ally and
nontoken "you may" handlers, the any-permanent-graveyard may (Yuna), the artifact graveyard default (Donatello), the
leave collectors (another permanent, another creature, ally permanent, ally creature, scheme), the sacrifice default
(which now always gives its entry the sacrificed card as triggering card, not only for the card-aware effects), the
commander-zone collector and the delayed "when that creature dies this turn" path. The effect-class handlers that only
bound or rebuilt (`handleCreateTokensForEachDyingSourceCounter`, `handleExileTopCardsForEachDyingCreatureCounter`,
`handleConditionalSelfLeavesTrigger`, `handlePutCountersOnTargetForEachLeavingSourceCounters`, the any-death token
handler) are gone; the defaults do the rest. `isBoundToTargetGroup` no longer unbinds.

**Cards (by script).** `scripts/step15-lki-cards.py` (idempotent, per letter module), 17 card classes: the 13 users of
the three gone classes (Big Mother Mouser, Boss's Chauffeur, Chasm Skulker, Eumidian Hatchery, Felisa, Hangarback
Walker, Hooded Hydra, J'Tun Owl Keeper, Kinsbaile Borderguard, Mister Gutsy, Shadow Urchin, Sporogenesis, The Ooze),
Essence Channeler (lost its empty `Map.of()` snapshot argument), and the trigger conditions the collectors used to judge
by skipping, now written as filters the pipeline judges on the departed permanent: The Ooze's "with a +1/+1 counter on
it", Donatello's "if it had counters on it", Sporogenesis' "with a fungus counter on it", Dual Nature's "nontoken
creature", Reyhan's command-zone "if it had one or more +1/+1 counters on it" (as its death half already was). The
watch lookup falls back from a filter it doesn't know to the first predicate of an `AllOf` or to no filter, so the
filtered triggers keep their collectors.

**Behaviour changes.** None intended and no card test changed. The filters replace the skips one for one (**CR 603.4**,
checked, for the intervening "if"s). `CreateTokenEffect.withAmount(DynamicAmount)` keeps a template's simultaneous
tokens where the old `withAmount(int)` rebuild dropped them (none of the 13 templates has any). A sacrifice trigger's
entry now always names the sacrificed card as its triggering card.

**Kept, with their reason.** `MoveDyingSourceCountersToTargetCreatureEffect.requiresCounters` is still judged by the
self-death handler (an effect-class judgment of an intervening "if"); the other LKI rebuilds that never implemented an
interface (`DrawCardForEachDyingSourceCounterEffect`, `ManifestTopCardsForEachDyingSourceCounterEffect`,
`ExileIfHadCounterElseReturnWithCounterEffect`, `PutCounterOnTargetForEachLeavingSourceCounterEffect`,
`CreateTokenWithDyingSourceCountersEffect`, `CreateTokenWithDyingSourcePowerCountersEffect`,
`GainLifeEqualToDyingCreatureToughnessEffect`, `snapshotDyingPermanentManaValue`, `snapshotDynamicMaxManaValue`,
`snapshotArtifactManaValue`) and run 1's baked-id effects; Riveteers Ascendancy's target restriction. Paths that don't
carry the triggering permanent: the dynamic-target `MultiPermanentChoiceContext.SelfTriggeredAbilityTargets`, the
graveyard-target operation (`GraveyardTargetOperationState`), `queueMayAbilityForPlayer`'s `PendingMayAbility`, the
opponent-creature-death default and the ally may-pay handlers; no effect of this run reaches them.

**For later runs.** The rebuilds listed under "Kept" can now read `triggeringPermanentCounterCount` /
`CountersOnTriggeringPermanent` (draw and manifest "for each counter" become `DrawCardEffect` / `ManifestTopCardEffect`
with the amount); then the damage and combat group, the bindings that aren't trigger events, Bill Potts, the
may-unwrapping and the `…Triggering…` classes, as run 1 listed.

**Tests changed.**

- `DeathTriggerCollectorServiceTest`: "Default effect binds the artifact's counter snapshot" became "Default effect
  carries the artifact as it last existed as the triggering permanent" (asserts the context's `departedPermanent` and
  the unchanged effect); "Default effect does not fire when the artifact had no counters" is gone: that is now
  Donatello's trigger filter, which its card test `doesNotTriggerForArtifactWithoutCounters` covers.
- `LegacyTriggerBindingsTest`: the `bindSelfDeathEffect` stub lost an argument; "Snapshots counters and preserves the
  target predicate" became "Carries the dying creature as the triggering permanent and preserves the target
  predicate".
- `PermanentRemovalServiceTest`: the two emblem artifact-graveyard verifications match the permanent's counters instead
  of a counter map.
- `GameDataDeepCopyTest` (AI): new case for the stack entry's triggering permanent and attachments.

**Test runs.**

- Card: the test class of every card with a death, leave, sacrifice, command-zone or mutate trigger or naming one of
  the changed effects (2,708 card classes; 2,670 test classes, 21,669 tests): all passing.
- Application, engine and AI: the whole modules against a HEAD baseline in a worktree (with main's card-data cache):
  application 617 failures of 5,012 here and 617 of 5,013 at HEAD (the one fewer test is the removed Donatello case),
  engine 1 (`GameMutationBoundaryRatchetTest`), AI 10, the same failing cases in both trees, none new. The two
  rewritten `PermanentRemovalServiceTest` emblem cases fail in both trees on the class's `DepartureService` mock wiring.
- The full suite was not run here.

#### Run 3: the rest of the departed object

Run 2's "for later runs" listed this group first: the last-known-information rebuilds that never implemented an
interface, and run 1's effects with a baked `dyingCardId`. Their collectors no longer rebuild or bind them; each effect
reads the stack entry's triggering card or permanent as it resolves. The 11 `…AwareEffect` interfaces of the damage,
combat and non-event groups are untouched.

**What the scan found.** Twelve classes. Seven rebuilt from the departed permanent in a collector:
`DrawCardForEachDyingSourceCounterEffect`, `ManifestTopCardsForEachDyingSourceCounterEffect`,
`ExileIfHadCounterElseReturnWithCounterEffect`, `PutCounterOnTargetForEachLeavingSourceCounterEffect`,
`CreateTokenWithDyingSourceCountersEffect`, `CreateTokenWithDyingSourcePowerCountersEffect` and
`GainLifeEqualToDyingCreatureToughnessEffect`. Five had a baked dying-card id: `BecomeCopyOfDyingCreatureEffect`,
`ReturnDyingOpponentCreatureUnderYourControlEffect`, `ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect`,
`StealDyingOpponentPermanentUnlessPaysLifeEffect` and `ExileTriggeringCreatureAndReturnSourceToHandEffect`. They have
about 20 binding sites in `LegacyTriggerBindings`, `DeathTriggerCollectorService`, `TriggerCollectionService` and
`MiscTriggerCollectorService`. 23 card classes use them.

**Gone, replaced by existing effects (AGENTS.md's reuse rule), cards by script.**

- `DrawCardForEachDyingSourceCounterEffect(type[, then])` became `DrawCardEffect(new CountersOnTriggeringPermanent(type))`.
  Cleopatra's "then you lose 2 life" is `SequenceEffect.of(draw, new LoseLifeEffect(2))`, because the "a creature dies"
  collector puts each effect of an ability on the stack separately (it would become two stack objects).
- `ManifestTopCardsForEachDyingSourceCounterEffect` became `ManifestTopCardEffect(new CountersOnTriggeringPermanent())`.
  `ManifestTopCardEffect` gained a `DynamicAmount amount` (the no-arg form is `Fixed(1)`), and its handler manifests one
  card at a time (**CR 701.40e**, checked).
- `PutCounterOnTargetForEachLeavingSourceCounterEffect` and its handler became `PutCounterOnTargetPermanentEffect(type,
  new CountersOnTriggeringPermanent(type))`. Selfless Police Captain's `target(creatureYouControl())` keeps its targets.
- `GainLifeEqualToDyingCreatureToughnessEffect` became `GainLifeEffect(new TriggeringPermanentToughness())`. The ally
  creature-death entry (Proper Burial, South Wind Avatar) and the "permanent put into an opponent's graveyard" default
  (Grim Feast) record the last-known toughness with `setTriggeringPermanentToughnessAtTrigger`. Grim Feast's creature
  check, which its handler made, is now the trigger's filter: `Triggers.dies(Triggers.CREATURE, OPPONENT_OWNS)`.

**Kept, now resolution effects.** No existing effect expresses these two (there is no if/else effect, and a
`Condition` can't see the departed permanent):

- `ExileIfHadCounterElseReturnWithCounterEffect` (Bogardan Phoenix) has a handler that reads
  `triggeringPermanentCounterCount` and inserts `ExileSourceCardFromGraveyardEffect` or
  `ReturnTriggeringCardFromGraveyardToBattlefieldEffect(false, true, type, 1)` after itself on the entry.
- `CreateTokenWithDyingSourceCountersEffect` (Ambitious Augmenter) has a handler that inserts its token and a
  `PutCountersOnCreatedPermanentsEffect` for each counter type the triggering permanent had. Because the effects are
  inserted rather than resolved inline, a token-creation replacement choice can pause between them, as with the old
  rebuilt list. "If it had one or more counters on it" is the trigger's filter:
  `Triggers.dies(new PermanentHasCountersPredicate(CounterType.ANY), THIS)`.
- `CreateTokenWithDyingSourcePowerCountersEffect` (Furnace Gremlin, Bloated Processor) already read the entry's event
  value as it resolved, and its collector only recorded the dying power, so it is unchanged.

**The baked ids.** The records lost their ids: `BecomeCopyOfDyingCreatureEffect()`,
`ReturnDyingOpponentCreatureUnderYourControlEffect(grantSubtype)`,
`ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect(lifeCost)`, `StealDyingOpponentPermanentUnlessPaysLifeEffect(lifeCost)`
(it also lost `payingPlayerId`) and `ExileTriggeringCreatureAndReturnSourceToHandEffect()`. Their handlers read the
entry instead:

- Cemetery Puca copies the triggering permanent's card, or the triggering card when there is no snapshot.
- "That opponent" for Prince of Thralls and Nazgûl Battle-Mace is `getTriggeringPermanentControllerId()`.
- The "unless … pays N life" questions asked as the effect resolves carry the card into their `PendingMayAbility`
  through the new `withTriggeringCard(cardId, version)`. The answer handlers (`MayPenaltyChoiceHandlerService`,
  `ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeHandler`) read `triggeringCardId()`.

Collectors changed:

- Removed, the defaults doing the rest: the "any creature" Puca and draw-for-counters handlers, the opponent-creature
  return and toughness handlers, Grim Feast's watch handler, and the five `LegacyTriggerBindings` rebuilds with
  `drawThen` and `countConcreteCounters`.
- Changed to set the triggering card and permanent controller instead of binding them: Diabolic Servitude's linked-death
  handler (it still judges the link), Athreos' handler (`DeathTriggerTarget.withTriggeringCard`), and the two steal
  collectors.
- The opponent creature-death "you may" handler now passes the source permanent and the trigger's mark-on-acceptance
  limit (The Reaper King, No More's "do this only once each turn"), and gives the entry the triggering permanent.

**Cards (by script).** `scripts/step15-departed-object-cards.py` (idempotent, per letter module) changed 14 card
classes:

- The users of the four gone classes: Cleopatra, Dusk Urchins, Marketback Walker, Vexing Sphinx, Vogar, Watchful
  Blisterzoa, Omarthis, Selfless Police Captain, Proper Burial, South Wind Avatar and Grim Feast (with its filter).
- Ambitious Augmenter (its filter).
- Necroskitter and The Reaper King, No More. Their Oracle "you may" was added by the collector, which wrapped the bare
  effect in a `MayEffect`. It is now written on the card as `new MayEffect(new ReturnDyingOpponentCreatureUnderYourControlEffect(), …)`.

**Behaviour changes.**

- Rules fix, CR 603.2 (checked; Omarthis has no intervening "if", CR 603.4): Omarthis' death trigger used to be
  skipped when it had no counters. Now it goes on the stack and manifests nothing. New card test
  `OmarthisGhostfireInitiateTest#deathWithNoCountersStillPutsTheAbilityOnTheStack`.
- Lim-Dûl, Necroskitter and The Reaper King, No More triggered only while the dying card was still in a graveyard as
  the trigger was collected. Now they trigger for every matching death, and the return does nothing if the card is
  gone, for example a token.
- Cemetery Puca's payment prompt is the card's own text ("…a copy of that creature?"), not one naming the dying card.
- The "any counters" filter on Ambitious Augmenter ignores `SILVER`, as `PermanentHasCountersPredicate(ANY)` does
  everywhere. The old skip counted silver counters too.
- New card test `GrimFeastTest#doesNotTriggerForNoncreaturePermanent` covers the creature check moving into the trigger
  filter.

**Kept, with their reason.**

- The three `snapshot…ManaValue` helpers (`snapshotDyingPermanentManaValue`, `snapshotDynamicMaxManaValue`,
  `snapshotArtifactManaValue`). They narrow a graveyard target's mana value, and that target is chosen before a stack
  entry exists, like Riveteers Ascendancy (run 2).
- The other trigger-time rebuilds in `LegacyTriggerBindings`, `DeathTriggerCollectorService` and
  `TriggerCollectionService`, none of which this run's scan listed:
  - `RegisterDelayedCreateTokenCopyOfDyingSourceEffect.withInitialCounters` and its minimum-counter skip;
  - `RegisterDelayedSelfReturnFromGraveyardWithOneFewerCounterEffect`;
  - `PutCountersOnTargetForEachDyingSourcePowerEffect(power)`;
  - `ExileEquippedCreatureEffect(id)`;
  - `SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect`'s damaging ids;
  - the self-leaves `GainLifeEffect` / `TargetPlayerGainsLifeEffect` freezes;
  - `CreateTokenIfDyingSourceHadCounterEffect` and `ReturnTriggeringPermanentToBattlefieldWithCounterEffect`'s counter
    skips;
  - `MoveDyingSourceCountersToTargetCreatureEffect.requiresCounters`;
  - `CreateTokenWithDyingSourcePowerEffect` and `CreateTokenWithDyingSourceCounterPTEffect`'s event values.
- Found after this run and fixed in its own commit: Nazgûl Battle-Mace's Oracle grants "whenever an opponent sacrifices a
  nontoken permanent…" to the equipped creature, but the card class had the trigger on the Equipment itself. It is now a
  `GrantTriggeredAbilityEffect` to `EQUIPPED_CREATURE` (`NazgLBattleMaceTest#unattachedMaceDoesNotTrigger`).

**For later runs.** The rebuilds under "Kept" can read `getTriggeringPermanentSnapshot()` / `CountersOnTriggeringPermanent`,
and their skips can become trigger filters. Then come the damage and combat interfaces, the bindings that aren't trigger
events, Bill Potts, the may-unwrapping and the `…Triggering…` classes, as run 1 listed.

**Scripts.** `scripts/step15-departed-object-cards.py` (the card classes) and `scripts/step15-departed-object-docs.py`
(exact line replacements in `ORACLE_TEXT_EFFECT_MAP.md`, `EFFECTS_INDEX.md`, `EFFECTS_QUICK_REFERENCE.md`, two
`CARD_PATTERNS` files and `ACTIVATED_ABILITY_GUIDE.md`). The effects, handlers and collectors were edited by hand.
`ARCHITECTURE.md`'s triggering-card note gained `PendingMayAbility.withTriggeringCard`. The skills didn't name these
classes.

**Tests changed.**

- `DeathTriggerCollectorServiceTest`: the two Cemetery Puca cases called the removed handler, and expected a pending
  may ability that it hadn't built since the may moved onto the stack. They became one case, "BecomeCopyOfDyingCreature's
  may-pay goes on the stack unchanged with the dying creature", which dispatches through the "any creature" watch and
  asserts the unchanged effect, the triggering card and the triggering permanent.
- `LegacyTriggerBindingsTest`: the two leaving-source counter snapshot cases became "Leaving-source counter trigger
  carries the leaving permanent and queues target selection". It asserts the unchanged `PutCounterOnTargetPermanentEffect`
  and the pending target choice's triggering permanent.
- New card tests: `OmarthisGhostfireInitiateTest#deathWithNoCountersStillPutsTheAbilityOnTheStack` and
  `GrimFeastTest#doesNotTriggerForNoncreaturePermanent`.

**Test runs.**

- Card: the 23 converted or affected card classes, then the test class of every card with a death, leave, sacrifice
  or destroy trigger or naming one of the changed effects (1,714 card classes; 1,694 test classes, 12,984 tests):
  all passing.
- Application: the whole module against a HEAD baseline in a worktree (with main's card-data cache). 616 failures of
  5,010 here and 617 of 5,012 at HEAD. Compared per case, there are no new failures; the one fewer is the old Cemetery
  Puca case, which failed at HEAD and was replaced.
- Engine 1 (`GameMutationBoundaryRatchetTest`) and AI 10, the known HEAD failures.
- The webservice module's test sources don't compile at HEAD (`CardBrowserServiceTest`'s anonymous `CardCatalog`
  lacks `findLoadedPrintingByName`); untouched here.
- The full suite was not run here.

#### Run 4: the departed permanent's trigger-time rebuilds and skips

Run 3's "for later runs" listed this group first: the trigger-time rebuilds and skips its "Kept" list named, which read
the departed permanent in a collector and baked what they read into the effect, or declined the ability. Each is now
either a trigger filter, judged on the permanent as it last existed on the battlefield (**CR 603.10a**, checked), or an
existing effect with an amount that reads the stack entry as it resolves. Two markers with the same shape that the
list missed, `GainLifeEqualToDyingSourcePowerEffect` and `TargetPlayerLosesLifeEqualToPowerEffect`, were done too. The
11 `…AwareEffect` interfaces are untouched.

**What the scan found.** Eleven classes, with their rebuilds in `LegacyTriggerBindings` (nine handlers, the Brine Hag
rebuild in the death default and the self-leaves `GainLifeEffect` freeze), `DeathTriggerCollectorService` (two) and
`MiscTriggerCollectorService` (one). 21 card classes use them; one more (Revered Unicorn) relied on the freeze.

**Gone, replaced by existing effects (AGENTS.md's reuse rule), cards by script.**

- `PutCountersOnTargetForEachDyingSourcePowerEffect` (Galuf's Final Act) became
  `PutCounterOnTargetPermanentEffect(PLUS_ONE_PLUS_ONE, new SourcePower(), null, new PermanentIsCreaturePredicate(),
  false, null, true)`: an up-to-one target restricted to creatures.
- `GainLifeEqualToDyingSourcePowerEffect` (Bottle Golems, Conclave Mentor, Packsong Pup, Willow Geist) became
  `GainLifeEffect(new SourcePower())`, and `TargetPlayerLosesLifeEqualToPowerEffect` (Mortis Dogs)
  `LoseLifeEffect(new SourcePower(), LoseLifeRecipient.TARGET_PLAYER)`. Both markers had no handler; the collector
  rewrote them. `SourcePower` reads the source's last-known power from the entry's source snapshot once the source has
  left. The old `EFFECTS_INDEX.md` entry said `SourcePower` recomputes from the live battlefield, which is wrong, and
  Mortis Dogs' boost, anthem and attack-boost tests pass unchanged. No new amount was needed: for these self-death
  abilities the source is the dying creature.
- `RegisterDelayedSelfReturnFromGraveyardWithOneFewerCounterEffect` (Nine-Lives Familiar) became
  `RegisterDelayedSelfReturnFromGraveyardEffect(REVIVAL, new Sum(new CountersOnTriggeringPermanent(REVIVAL), new
  Fixed(-1)))` on `Triggers.dies(new PermanentHasCountersPredicate(REVIVAL), THIS)`.
  `RegisterDelayedSelfReturnFromGraveyardEffect.counterAmount` is now a `DynamicAmount`, which its handler evaluates
  as the registering ability resolves. The `int` constructors keep the other five cards.
- `CreateTokenIfDyingSourceHadCounterEffect` (Toxrill, Village Pillagers) became the plain token. The counter is the
  trigger's filter: `Triggers.creatureDies(new PermanentHasCountersPredicate(SLIME / ANY), OPPONENT_CONTROLS)`.
- `ReturnTriggeringPermanentToBattlefieldWithCounterEffect(CounterType, Zone)` (Athreos) became
  `ReturnTriggeringPermanentToBattlefieldEffect(Zone)`, its handler renamed with it. "With a coin counter on it" is
  both triggers' filter: `creatureDies(coinCounter, ANY)` and `exiledFrom(BATTLEFIELD, AllOf(CREATURE, coinCounter),
  ANY)`, so the exile watch is still found by its first predicate. Both collectors lost their counter check.

**Kept, now reading the entry.**

- `ExileEquippedCreatureEffect()` lost its bound id. Its handler exiles what the entry's triggering permanent was
  attached to.
- `RegisterDelayedCreateTokenCopyOfDyingSourceEffect(DynamicAmount)` (Ochre Jelly) lost the minimum, divisor and
  baked count. Ochre Jelly is `new Divided(new CountersOnTriggeringPermanent(PLUS_ONE_PLUS_ONE), 2)` on
  `Triggers.dies(new PermanentCounterCountAtLeastPredicate(PLUS_ONE_PLUS_ONE, 2), THIS)`.
- `SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect(int, int)` (Brine Hag) lost its damaging ids. The handler
  reads `getPermanentIdsThatDealtDamageThisTurn()` from the entry's triggering permanent (the departure snapshot
  records them), falling back to the live source and then the source snapshot.
- `MoveDyingSourceCountersToTargetCreatureEffect(boolean controllerOnly)` lost `requiresCounters` and
  `alwaysTriggers`. Iron Apprentice's and Scolding Administrator's "if it had counters on it" is
  `Triggers.dies(new PermanentHasCountersPredicate(CounterType.ANY), THIS)` (**CR 603.4**, checked).
- `CreateTokenWithDyingSourcePowerEffect`, `CreateTokenWithDyingSourcePowerCountersEffect` and
  `CreateTokenWithDyingSourceCounterPTEffect` keep their handlers, and their binding handlers are gone. The first two
  read the entry's event value, which the death default records as the last-known power. The third reads the
  triggering permanent's counters, which the default sets too.
- Revered Unicorn's `GainLifeEffect(new CountersOnTriggeringPermanent(AGE))`: the self-leaves `GainLifeEffect` freeze
  is gone, and so is the `TargetPlayerGainsLifeEffect` handler's amount freeze. That handler still binds the remembered
  target player, and it now gives its entry the triggering permanent.

**Behaviour changes.**

- Rules fix, CR 603.2 (checked; no intervening "if"): an unattached Oathkeeper's "exile equipped creature" was skipped.
  Now it goes on the stack and exiles nothing. New card test
  `OathkeeperTakenosDaishoTest#unattachedOathkeeperStillPutsItsAbilityOnTheStack`.
- Galuf's Final Act's granted trigger declares a permanent target narrowed to creatures where it declared a creature
  target. The legal targets are the same; new card test `GalufsFinalActTest#deathTriggerTargetsOnlyCreatures`.
- "If it had counters on it" on Iron Apprentice and Scolding Administrator, and "with a counter on it" on Village
  Pillagers, now ignore `SILVER`, as `PermanentHasCountersPredicate(ANY)` does everywhere (as for Ambitious Augmenter
  in run 3). The old checks counted every counter.
- Brine Hag reads the damage record of the Brine Hag that died. The old trigger-time lookup by card id gave the same
  set for one death. But for a card that died, came back and died again in one turn, the second death's lookup also
  found the first life's damage sources, which dealt damage to another object (CR 400.7).
- The rebuilt abilities now reach the stack through the death and leave defaults, so their entries also carry the
  source id and snapshot, the triggering card and the triggering permanent.

**Kept, with their reason.**

- These rebuilds in `LegacyTriggerBindings` are still there:
  - `DistributeCountersAmongCreaturesOnDeathEffect`: it bakes its count, and its division is announced as it
    triggers;
  - `PutCounterOnTargetForEachDyingSourceCounterEffect`'s modular rebuild and its `MayEffect` wrapping;
  - `DealDamageToBlockedAttackersOnDeathEffect`'s blocked-attacker ids and combat-step skip;
  - Martyr's Bond;
  - `GainLifeEqualToLifeLostWhenEnteredEffect`;
  - `CreateTokenForTargetPlayerEffect`'s P/T freeze (Phantasmal Sphere);
  - the three `snapshot…ManaValue` helpers, for run 3's reason.
- Toxrill's "a creature you don't control" is written `OPPONENT_CONTROLS`, which is the same in a two-player game;
  unchanged.

**For later runs.** The rebuilds under "Kept" (`snapshotDynamicMaxManaValue` waits on a stack entry existing before
graveyard targets are chosen), then the damage and combat interfaces, the bindings that aren't trigger events, Bill
Potts, the may-unwrapping and the `…Triggering…` classes, as run 1 listed.

**Scripts.**

- `scripts/step15-departed-rebuilds-cards.py` (idempotent, per letter module) changed 22 card classes. Those are the
  users of the six gone classes, the `MoveDyingSourceCountersToTargetCreatureEffect` users that named the removed
  forms, Iron Apprentice's and Scolding Administrator's filters, Ochre Jelly and Revered Unicorn.
- `scripts/step15-departed-rebuilds-docs.py` makes exact line replacements in `ORACLE_TEXT_EFFECT_MAP.md`,
  `EFFECTS_INDEX.md`, `EFFECTS_QUICK_REFERENCE.md` and two `CARD_PATTERNS` files.
- The effects, handlers and collectors were edited by hand. The skills didn't name these classes.

**Tests changed.**

- `LegacyTriggerBindingsTest`:
  - "Snapshots the dying creature's effective power" (Galuf) became "Counters equal to its power wait for their target
    unchanged, with the dying creature as it last existed". It asserts the unchanged effect, the source snapshot's
    last-known power and the triggering permanent.
  - The two Mortis Dogs bake cases ("Bakes effective power from permanent", "Clamps negative power to 0") are gone:
    the effect is no longer rebuilt. `MortisDogsTest` covers the amount.
  - "Binds the Equipment's last-known attachment" became "Carries the Equipment's last-known attachment as the
    triggering permanent".
  - "Does not fire when the Equipment was unattached" became "Still triggers when the Equipment was unattached", the
    rules fix above.
- New card tests: `OathkeeperTakenosDaishoTest#unattachedOathkeeperStillPutsItsAbilityOnTheStack` and
  `GalufsFinalActTest#deathTriggerTargetsOnlyCreatures`.

**Test runs.**

- Card: the test classes of the 22 converted cards and of every other card whose path changed (Oathkeeper, Brine Hag,
  Flesh Carver, Grakmaw, Unwilling Vessel, Furnace Gremlin, Bloated Processor, Laquatus's Champion, Soul Scourge, Host
  of the Hereafter, Reluctant Role Model, the five other `RegisterDelayedSelfReturnFromGraveyardEffect` users, Ukkima,
  Kaito, Aven Riftwatcher, Tocasia's Onulet, Leader's Talent). That is 44 test classes and 810 test cases, all passing.
- Application: `LegacyTriggerBindingsTest`, `DeathTriggerCollectorServiceTest` and `MiscTriggerCollectorServiceTest`.
  The one failure, `DeathTriggerCollectorServiceTest$RegisterDelayedReturn` "May prompt includes dying card name",
  also fails at HEAD (`9fcb2e25aa`, checked in a worktree with main's card-data cache).
- Engine: the whole module. The one failure is the known HEAD one, `GameMutationBoundaryRatchetTest`.
- AI: compiles. Its tests weren't run, since no AI code changed.
- The full suite was not run here.

#### Run 5: the last departed-permanent rebuilds

Run 4's "for later runs" listed this group first: the rebuilds its "Kept" list left in `LegacyTriggerBindings`. Five of
the seven are gone; each effect now reads the stack entry as it resolves, or its skip is the ability's event condition.
The counter distribution and the three `snapshot…ManaValue` helpers are kept (below). The 11 `…AwareEffect` interfaces
are untouched.

**What the scan found.** Seven rebuild handlers in `LegacyTriggerBindings`: `DistributeCountersAmongCreaturesOnDeathEffect`,
`PutCounterOnTargetForEachDyingSourceCounterEffect` (modular and "you may"), `DealDamageToBlockedAttackersOnDeathEffect`,
Martyr's Bond (`SacrificePermanentsSharingDyingPermanentTypeEffect`), `GainLifeEqualToLifeLostWhenEnteredEffect`,
`CreateTokenForTargetPlayerEffect`'s P/T freeze, and the `snapshot…ManaValue` helpers. Martyr's Bond's marker was dead:
the card has been written with `SacrificePermanentsEffect` and `PermanentSharesCardTypeWithSourcePermanentPredicate`
since step 14 run 3a, and its `DeathTriggerCollectorService` handler was dead too. 29 card classes use the rest.

**Gone.**

- `SacrificePermanentsSharingDyingPermanentTypeEffect`, its two handlers (`LegacyTriggerBindings`,
  `DeathTriggerCollectorService`) and their `permanentTypePredicate` helpers: no card used them.
- `GainLifeEqualToLifeLostWhenEnteredEffect` (Soulgorger Orgg) became `GainLifeEffect(new
  LifeLostWhenTriggeringPermanentEntered())`, a new `DynamicAmount` that reads `getLifeLostWhenEntered()` from the
  entry's triggering permanent. Oracle's "the life **you** lost when it entered" keeps the old controller check: 0 when
  the ability's controller isn't the player who lost it (new card test
  `SoulgorgerOrggTest#anotherControllerGainsNoLifeWhenItLeaves`).
- Phantasmal Sphere's P/T freeze. Its X is `CountersOnTriggeringPermanent(PLUS_ONE_PLUS_ONE)`, which
  `CreateTokenForTargetPlayerEffectHandler` already evaluated against the entry. Its trigger, and Baffling End's, now
  go through the self-leaves default, so their target choice carries the source id and snapshot and the triggering
  permanent.

**Kept, now reading the entry.**

- `DealDamageToBlockedAttackersOnDeathEffect(int)` (Cathedral Membrane). Its handler damages the creatures in the
  triggering permanent's `getBlockedAttackerIdsThisCombat()`, not the entry's target ids. "Dies during combat" is the
  ability's event condition, `TriggerOptions.DEFAULT.withEventCondition(new DuringCombat())`. It is checked as it
  triggers and isn't an intervening "if" (**CR 603.2**, **CR 603.4**, checked). `DuringCombat` is the same five steps
  the skip checked.
- `PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType, PermanentPredicate, boolean modular)` lost
  `optional`. A "you may" is a `MayEffect` on the card (**CR 603.5**, checked), which the death `MayEffect` handler
  queues with its target as before (**CR 603.3d**, checked). `modularAbility()` is modular's ability (**CR 702.43a**,
  checked): the +1/+1 count, the artifact-creature target and `modular = true`. Cards write it
  `new MayEffect(PutCounterOnTargetForEachDyingSourceCounterEffect.modularAbility(), MODULAR_PROMPT)`. The collector
  used to switch the flag on from the dying card's printed Modular keyword; the cards now say it. (`modular()` would
  clash with the record accessor.)

**Behaviour changes.**

- Rules fix, CR 702.43a (checked): Blaster, Combat DJ grants "modular 1" to other nontoken artifact creatures and
  Vehicles you control. That granted ability is a modular triggered ability, but the old keyword check didn't mark it,
  because the dying creature had no printed Modular, so Zabaz, the Glimmerwasp didn't add its counter. New card test
  `BlasterCombatDJTest#grantedModularIsAModularTriggeredAbilityForZabaz` (fails with the flag off: 2 instead of 3).
- The "you may" prompts are the card's text: modular's `MODULAR_PROMPT`, and Soulstinger's "…for each -1/-1 counter on
  this creature?". They no longer use a prompt naming the dying card.
- Cathedral Membrane's entry has no target ids; the damaged creatures are the same.

**Kept, with their reason.**

- `DistributeCountersAmongCreaturesOnDeathEffect` and its two rebuilds (`LegacyTriggerBindings`, Grand Ossuary's in
  `DeathTriggerCollectorService`). Its targeted forms (Quirion Beastcaller, Jugan, Grand Ossuary) announce the division
  as the ability goes on the stack (**CR 601.2d**, as run 4 kept it), so the total must be known then. It can move once
  the announcement reads the entry's triggering permanent. Vastwood Hydra's untargeted form could move on its own.
- The three `snapshot…ManaValue` helpers, for run 3's reason.
- Martyr's Bond's "another nonland permanent" trigger reaches `DeathTriggerCollectorService.handleAllyPermanentGraveyardSacrifice`.
  That handler sets the entry's **source** snapshot to the dying permanent, so the source-relative
  `PermanentSharesCardTypeWithSourcePermanentPredicate` compares with "it". This is a trigger-time rebinding of the same
  kind: a predicate that reads the triggering permanent would retire it.
- Mongrel Pack writes "dies during combat" as an intervening "if" (`withInterveningIf(new DuringCombat())`). It is an
  event condition like Cathedral Membrane's. The result is the same, because the death trigger resolves in the combat
  step it died in. Unchanged here.

**For later runs.** The distribution and Martyr's Bond's rebinding above, then the damage and combat interfaces, the
bindings that aren't trigger events, Bill Potts, the may-unwrapping and the `…Triggering…` classes, as run 1 listed.

**Scripts.**

- `scripts/step15-departed-leftovers-cards.py` (idempotent, per letter module) changed 29 card classes: the 24 modular
  cards (the Arcbound cards, Zabaz, Power Depot, Scrapyard Recombiner, both Blaster faces), Soulstinger, Servant of the
  Scale, Phantasmal Sphere, Soulgorger Orgg and Cathedral Membrane. Run on `magical-vibes-card-test/c`, it also changes
  `CathedralMembraneTest`.
- `scripts/step15-departed-leftovers-docs.py` makes exact line edits in `ORACLE_TEXT_EFFECT_MAP.md` (with a new
  Cathedral Membrane row), `EFFECTS_INDEX.md` (with the new amount), `EFFECTS_QUICK_REFERENCE.md` and
  `CARD_PATTERNS_CREATURES_ETB.md`.
- The effects, the amount, the handlers and the collectors were edited by hand. The skills didn't name these classes.

**Tests changed.**

- `LegacyTriggerBindingsTest`:
  - The four Cathedral Membrane cases ("Triggers during combat when creature was blocking", "Does not trigger outside
    combat", "Triggers with no creatures to damage when it blocked none", "Does not trigger when step is null") became
    one: "Blocked-attacker damage goes on the stack unchanged, with the dying blocker as its triggering permanent".
    The combat check is now the event condition, which `TriggeredAbilityStackService` judges before the bindings run;
    `CathedralMembraneTest#noTriggerOutsideCombat` covers it.
  - "Carries the dying creature as the triggering permanent and preserves the target predicate" became "Modular's
    \"you may\" waits for its target unchanged, with the dying creature as the triggering permanent".
- `CathedralMembraneTest#deathTriggerDeals6DamageToBlockedCreature` no longer asserts that the blocked attacker is
  among the entry's target ids (by script). The attacker dying to the 6 damage still shows which creature was hit.
- New card tests: `BlasterCombatDJTest#grantedModularIsAModularTriggeredAbilityForZabaz` and
  `SoulgorgerOrggTest#anotherControllerGainsNoLifeWhenItLeaves`.

**Test runs.**

- Card: the test class of every card test naming a converted card, Baffling End, Martyr's Bond, Grief Tyrant or Reyhan:
  47 test classes, 360 test cases, all passing.
- Application: `LegacyTriggerBindingsTest`, `DeathTriggerCollectorServiceTest` and
  `CreateTokenForTargetPlayerEffectHandlerTest`. The one failure, `DeathTriggerCollectorServiceTest$RegisterDelayedReturn`
  "May prompt includes dying card name", is run 4's known HEAD failure.
- Engine: the whole module. The one failure is the known HEAD one, `GameMutationBoundaryRatchetTest`.
- AI: compiles (`compileTestJava`). Its tests weren't run, since no AI code changed.
- The full suite was not run here.

#### Run 6: the death counter distribution and Martyr's Bond

Run 5's "for later runs" listed this group first: `DistributeCountersAmongCreaturesOnDeathEffect`'s two rebuilds and
Martyr's Bond's source rebinding. Both are gone. The distributions read their total from the stack entry, and "it" in
Martyr's Bond is the triggering permanent. The scan found a rules bug, and the user chose to fix it here: nothing in a
real game ever chose the targeted division. The 11 `…AwareEffect` interfaces are untouched.

**What the scan found.**

- `DistributeCountersAmongCreaturesOnDeathEffect` was rebuilt in two places:
  - `LegacyTriggerBindings` (self death: Vastwood Hydra, Quirion Beastcaller, Jugan) baked the count, wrapped the
    "you may" in a `MayEffect`, and pushed the entry itself.
  - `DeathTriggerCollectorService` (any creature dies) did the same for Grand Ossuary, but that handler was dead: a
    plane's "whenever a creature dies" goes through `TriggerCollectionService.collectPlanarCreatureDeathTriggers`,
    not the any-creature collector. Grand Ossuary got its "they control" from the resolution handler, which fell back
    to the entry's triggering permanent controller.
- The targeted forms (Jugan, Quirion, Grand Ossuary) announced only a division already staged in
  `pendingETBDamageAssignments`, and only tests stage one. In a real game no player was asked for targets or a
  division, so these abilities placed no counters (resolution fell back to the same empty buffer).
- Martyr's Bond's "another nonland permanent" trigger reached `handleAllyPermanentGraveyardSacrifice`, which set the
  entry's source snapshot to the dying permanent so that `PermanentSharesCardTypeWithSourcePermanentPredicate`
  compared with "it". No other card reached that handler.

**The model.**

- `DistributeCountersAmongCreaturesOnDeathEffect(CounterType, DynamicAmount total, Recipients)`, where `Recipients` is
  `ANY_CREATURE`, `CREATURES_YOU_CONTROL` or `CREATURES_ITS_CONTROLLER_CONTROLS`. The factories are
  `amongAnyTargetCreatures`, `amongTargetCreaturesYouControl` and `amongTargetCreaturesItsControllerControls`. Its
  target spec is `NONE`; the targets travel with the division on the entry. The `count`, `countFromSourceCounters`,
  `countFromSourcePower`, `optional` and `targeted` components are gone. A "you may" is a `MayEffect` on the card
  (**CR 603.5**, checked).
- `TriggeringPermanentPower` (new `DynamicAmount`) reads the entry's `triggeringPermanentPowerAtTrigger`, at least 0.
  It is the power sibling of `TriggeringPermanentToughness`.
- `StackedDividedDamageTriggerService` announces this effect's division too, also inside a `MayEffect`, as the
  ability goes on the stack (**CR 601.2c**, **CR 601.2d**, **CR 603.3d**, checked):
  - It evaluates `total` against the entry and collects targets through the cast-time
    `DistributeCountersAmongTargetsEffect.chosenAmongTargetCreatures(type, total, [PermanentControlledBySourceControllerPredicate])`.
  - It asks for one or more targets, then the amount on each when there are several.
  - A staged division is checked before it is taken over, as the old announcement checked it: every recipient must be
    a legal target receiving at least 1, and the amounts must add up to the total. Divided damage and prevention still
    take a staged division unchecked, as before.
  - The `Division` record carries the counter type, which makes the prompts say "counters", not "damage". It also
    carries the choosing player.
- `MultiPermanentChoiceContext.StackedDividedDamageTargets` gains a nullable `counterType`, which it passes to the
  per-target `CounterDistributionAssignment`. Before, the existing `DistributeCountersAmongTargetsEffect` case asked
  "how much damage to deal".
- `FilterContext` gains `triggeringPermanentSnapshot` (`withTriggeringPermanentSnapshot`). The new
  `PermanentSharesCardTypeWithTriggeringPermanentPredicate` compares with it. `SacrificePermanentsEffectHandler` fills
  it from the entry's triggering permanent. `PredicateEvaluationService.sharesCardTypeWith` serves both "shares a card
  type" predicates.

**The engine.**

- Gone:
  - `LegacyTriggerBindings.handleDistributeCountersAmongCreaturesOnDeath` and its `announceStagedDistribution`, with
    the class's now unused `TargetLegalityService`.
  - `DeathTriggerCollectorService.handleDistributeCountersAmongCreaturesOnDeath` and its `announceStagedDistribution`
    (dead), with the unused `targetLegalityService` field.
  - `DeathTriggerCollectorService.handleAllyPermanentGraveyardSacrifice` (Martyr's Bond).
- The self-death defaults (`handleDeathDefault`, `handleDeathMayEffect`) put the distributions on the stack with the
  dying creature as the triggering permanent.
- The ally-permanent graveyard default (`handleAllyPermanentGraveyardDefault`) now gives its stack entry the
  permanent that was put into the graveyard as its triggering permanent.
- `DistributeCountersAmongCreaturesOnDeathEffectHandler` resolves only the announced division; the fallback to
  `pendingETBDamageAssignments` at resolution is gone. Its `distributingPlayerId` is the ability's controller, or for
  `CREATURES_ITS_CONTROLLER_CONTROLS` the entry's triggering permanent controller. The announcement uses it for "you
  control", the prompt and targetability, and resolution uses it to recheck the recipients (**CR 608.2b**, checked).
- AI: `MultiPermanentChoiceAiStrategy` puts a beneficial stacked counter distribution (any counter but -1/-1 and
  -2/-1) on its own strongest creatures. It used to pick the opponent's, as for damage.

**Cards (by script).**

- `scripts/step15-distribution-cards.py` (idempotent, per letter module) changed 4 classes:
  - Vastwood Hydra: `new MayEffect(new DistributeCountersAmongControlledCreaturesEffect(PLUS_ONE_PLUS_ONE, new
    CountersOnTriggeringPermanent(PLUS_ONE_PLUS_ONE)), prompt)`. It reuses the untargeted "distribute among creatures
    you control" effect, which was what the rebuild turned it into.
  - Quirion Beastcaller: `amongTargetCreaturesYouControl(PLUS_ONE_PLUS_ONE, new CountersOnTriggeringPermanent(PLUS_ONE_PLUS_ONE))`.
  - Jugan: `new MayEffect(amongAnyTargetCreatures(PLUS_ONE_PLUS_ONE, new Fixed(5)), prompt)`.
  - Grand Ossuary: `amongTargetCreaturesItsControllerControls(PLUS_ONE_PLUS_ONE, new TriggeringPermanentPower())`.
- `scripts/step15-martyrs-bond-card.py`: Martyr's Bond's predicate became `PermanentSharesCardTypeWithTriggeringPermanentPredicate`.

**Behaviour changes.**

- Rules fix, **CR 601.2c-d** and **CR 603.3d** (checked): Jugan's, Quirion Beastcaller's and Grand Ossuary's
  controller is now asked for the targets and the division as the ability goes on the stack. Before, no player was
  asked, and the abilities placed no counters outside tests that staged a division. New card tests:
  `JuganTheRisingStarTest#controllerChoosesTargetsAndDivisionAsTheAbilityGoesOnTheStack`,
  `QuirionBeastcallerTest#controllerChoosesAmongOwnCreaturesAsTheAbilityGoesOnTheStack` and
  `GrandOssuaryTest#dyingCreaturesControllerChoosesAmongTheirOwnCreatures`.
- Each chosen target must receive at least one counter (CR 601.2d). With zero counters to distribute (Quirion with no
  +1/+1 counters), or no legal target, the ability is removed from the stack (CR 603.3d), as the other stacked
  divisions are. New card test `QuirionBeastcallerTest#deathWithNoCountersAsksForNoTargets`.
- Grand Ossuary: the ability stays under the planar controller (**CR 901.6**, checked). The dying creature's
  controller chooses the targets among their creatures and the division; that player's choosing is the old behaviour
  the user chose to keep. No official ruling says who chooses (searched). Targetability is judged for the distributing
  player.
- The "you may" prompts are the cards' own text. Before, the collector's text named the baked count.
- A division staged after Jugan's ability is on the stack is ignored at resolution, as `deathDistributionIsLockedBeforeResolution`
  already expected; the resolution-time buffer fallback is gone.

**Kept, with their reason.**

- The three `snapshot…ManaValue` helpers, for run 3's reason (a graveyard target is chosen before a stack entry
  exists).
- Confusion in the Ranks and Fatal Grudge still compare "it" through `PermanentSharesCardTypeWithSourcePermanentPredicate`,
  with an `EnterTriggerCollectorService` path that sets the source snapshot to the entering permanent. That rebinding is
  of the same kind as Martyr's Bond's and can move to the new predicate once the enters entry carries its triggering
  permanent in `FilterContext`.
- `checkStagedDivision` applies only to the death counter distribution. Divided damage and prevention take a staged
  division unchecked, as before.

**For later runs.** The enters-side "shares a card type with it" rebinding above, then the damage and combat
interfaces, the bindings that aren't trigger events, Bill Potts, the may-unwrapping and the `…Triggering…` classes, as
run 1 listed.

**Scripts.** `scripts/step15-distribution-cards.py`, `scripts/step15-martyrs-bond-card.py` (card classes),
`scripts/step15-distribution-tests.py` (card tests, per letter test module), `scripts/step15-distribution-docs.py`
(exact line edits in `ORACLE_TEXT_EFFECT_MAP.md`, with new Quirion and Grand Ossuary rows, `EFFECTS_INDEX.md`, with the
new amount, `EFFECTS_QUICK_REFERENCE.md`, `CARD_PATTERNS_CREATURES_TRIGGERED.md` and `PREDICATES_REFERENCE.md`, with
the new predicate). The effect, amount, predicate, services, handlers, collectors and AI were edited by hand.
`ARCHITECTURE.md`'s stacked-division and triggering-permanent paragraphs were updated. The skills didn't name these
classes.

**Tests changed.**

- `LegacyTriggerBindingsTest`: "Snapshots counter count and stacks a MayEffect" became "A \"you may\" counter
  distribution goes on the stack unchanged, with the dying creature as its triggering permanent"; the binding no
  longer takes a `TargetLegalityService`.
- `JuganTheRisingStarTest`: "Accepting with no targets places no counters" (by script) staged nothing and accepted the
  "you may". Nothing is staged now, so the controller is asked for targets first, and the test became the targets-and-division
  test above.
- `QuirionBeastcallerTest`: "Death distribution can choose zero targets" (by script) passed only because nothing
  resolved while the choice waited. It became the own-creatures test and the no-counters test above.
- `GrandOssuaryTest`: the new test above (by script).
- New unit tests: `PredicateEvaluationServiceTest` ("PermanentSharesCardTypeWithTriggeringPermanentPredicate compares
  with the triggering permanent's last-known types") and `MultiPermanentChoiceAiStrategyTest` ("A stacked +1/+1
  counter distribution picks the AI's own strongest creatures", "A stacked damage division still picks the opponent's
  strongest creatures").

**Test runs.**

- Card: the test class of every card naming a changed effect, amount or predicate, every divided-damage, divided-prevention
  and counter-distribution card, every `SacrificePermanentsEffect` card, and every card with a "put into a graveyard
  from the battlefield" or an "another permanent you control dies" / "a permanent you own dies" trigger: 506 test
  classes (516 result files with the nested ones), 4,522 test cases, all passing.
- Application: `LegacyTriggerBindingsTest`, `DeathTriggerCollectorServiceTest`, `PredicateEvaluationServiceTest`,
  `SacrificePermanentsEffectHandlerTest` and `CardFreezeTest`. There are 7 failures, and HEAD `8c30cbeea4` has the same 7
  (checked in a worktree with main's card-data cache): "May prompt includes dying card name" (run 4's) and 6
  `SacrificePermanentsEffectHandlerTest` cases (`TargetPlayerPermanents` 4, `TargetPlayerOrPermanentController` 2,
  Mockito wiring). Those 6 also fail with HEAD's handler in this tree.
- Engine: the whole module. The one failure is the known HEAD one, `GameMutationBoundaryRatchetTest`.
- AI: `MultiPermanentChoiceAiStrategyTest` and `GameDataDeepCopyTest`, all passing.
- The full suite was not run here.

#### Run 7: the enters-side rebindings of the source

Run 6's "for later runs" listed this group first: the enters paths that made the entering permanent stand in for the
ability's source, so that a source-relative predicate compared with "it". They are gone; the predicates read the
triggering permanent. The 11 `…AwareEffect` interfaces are untouched.

**What the scan found.**

- Confusion in the Ranks ("its controller chooses target permanent another player controls that shares a card type with
  it") was written with `PermanentNotPredicate(PermanentControlledBySourceControllerPredicate)` and
  `PermanentSharesCardTypeWithSourcePermanentPredicate`, and three engine paths made that mean the entering permanent:
  - `EnterTriggerCollectorService.enqueueAnyPermanentEnter` rebuilt the exchange at trigger time with
    `PermanentControlledByPlayerPredicate(enteringControllerId)` baked in;
  - the same default passed the entering permanent as the target choice's `targetSourcePermanentId`, so
    `TriggerTargetCollector`'s filter context had it as the source snapshot;
  - `PermanentChoiceTriggerHandlerService.handleEntersTrigger` set the stack entry's **source** snapshot to the entering
    permanent for any `triggeringPermanentIsFirstTarget` exchange, and `ExchangeControlOfTargetPermanentsEffectHandler`
    put the entering permanent in its filter context as the source.
- Lucy MacLean's "target player other than that token's controller" (`TargetPlayerOtherThanTriggeringPermanentControllerEffect`):
  `handleTokenEnterCopyForTargetPlayer` passed the entering token as the target source, and `TriggerTargetCollector`
  excluded the controller of its source snapshot.
- Fatal Grudge, named in run 6's Findings, isn't a trigger: its "the sacrificed permanent" is the spell's cost snapshot,
  which `SacrificePermanentsEffectHandler` falls back to as the source. Braids, Arisen Nightmare's handler does the same
  with the permanent sacrificed as it resolves. Neither is a trigger event; both are kept.

**The model.**

- `PermanentControlledByTriggeringPermanentControllerPredicate` (new): permanents controlled by the controller of the
  permanent `FilterContext.triggeringPermanentId` names; nothing while it is off the battlefield.
- `PermanentSharesCardTypeWithTriggeringPermanentPredicate` reads the permanent `triggeringPermanentId` names while it
  is on the battlefield, by its current (layered) types, and otherwise `triggeringPermanentSnapshot` as before (Martyr's
  Bond) (**CR 608.2h**, checked).
- `TargetLegalityService`'s battlefield resolution recheck gives its filter context and `TargetValidationContext` the
  entry's triggering permanent id and snapshot, as `TriggerTargetCollector` already gave them when the target was
  chosen.

**The engine.**

- Gone: the exchange rebuild in `enqueueAnyPermanentEnter` and the entry's source rebinding in
  `handleEntersTrigger`.
- `enqueueAnyPermanentEnter` and `handleTokenEnterCopyForTargetPlayer` pass the watching permanent as the target choice's
  target source.
- `ExchangeControlOfTargetPermanentsEffectHandler`'s triggering-permanent form gives its filter context the triggering
  permanent's id, not a source snapshot.
- `TriggerTargetCollector` excludes the controller of the `triggeringPermanentId` it is given.

**Cards (by script).** `scripts/step15-enters-rebinding-card.py`: Confusion in the Ranks' target became
`PermanentAllOfPredicate(PermanentNotPredicate(PermanentControlledByTriggeringPermanentControllerPredicate()),
PermanentSharesCardTypeWithTriggeringPermanentPredicate())`.

**Behaviour changes.**

- Rules fix, **CR 608.2h** (checked): Confusion in the Ranks judged "shares a card type with it" by the entering
  permanent's printed and granted types. It now uses its current characteristics: under March of the Machines an
  entering noncreature artifact is an artifact creature, so another player's creature is a legal target. New card test
  `ConfusionInTheRanksTest#enteringPermanentIsJudgedByItsCurrentTypes` (fails with the old comparison).
- The "any permanent enters" default's targeted choices (Confusion in the Ranks, Nakia) and Lucy MacLean's target
  choice have the ability's source as their source; Nakia's target filter doesn't read the source.
- The resolution recheck of a battlefield target now sees the entry's triggering permanent. Before, a target restriction
  that read it (`PermanentIsTriggeringPermanentPredicate`, `PermanentManaValueEqualsTriggeringPermanentPredicate`, …)
  had no triggering permanent there. The test classes of every card using such a predicate pass unchanged.

**Kept, with their reason.**

- Fatal Grudge and Braids, Arisen Nightmare (above): not trigger events.
- The `sourceIsEnteringPermanent` paths (Pandemonium's "have it deal damage"): the entering creature is the damage
  source by its Oracle text, so it is the entry's source on purpose.
- The enters "you may pay" path that queues its target choice with the entering permanent as `sourcePermanentId`
  (`EnterTriggerCollectorService`, the `MayPayManaEffect` branch). It is part of the may-unwrapping, which a later run
  removes.
- `handleTokenEnterCopyForTargetPlayer` still wraps Lucy MacLean's effect in a `MayEffect` (the may-unwrapping).

**For later runs.** The damage and combat interfaces, the bindings that aren't trigger events, Bill Potts, the
may-unwrapping and the `…Triggering…` classes, as run 1 listed.

**Scripts.** `scripts/step15-enters-rebinding-card.py` (the card class), `scripts/step15-enters-rebinding-tests.py`
(the card test, on `magical-vibes-card-test/c`), `scripts/step15-enters-rebinding-docs.py` (exact line edits in
`ORACLE_TEXT_EFFECT_MAP.md`, `EFFECTS_QUICK_REFERENCE.md` and `PREDICATES_REFERENCE.md`, with the new predicate). The
predicates, services, handlers and collectors were edited by hand, and so was `ARCHITECTURE.md`'s triggering-permanent
paragraph. The skills didn't name these classes.

**Tests changed.**

- New card test `ConfusionInTheRanksTest#enteringPermanentIsJudgedByItsCurrentTypes` (by script).
- New unit tests in `PredicateEvaluationServiceTest`: "PermanentSharesCardTypeWithTriggeringPermanentPredicate compares
  with the triggering permanent on the battlefield first" and "PermanentControlledByTriggeringPermanentControllerPredicate
  follows the triggering permanent's controller".
- No existing test changed.

**Test runs.**

- Card: the test class of every card using an exchange of control, a predicate or effect that reads the triggering
  permanent, `TargetPlayerOtherThanTriggeringPermanentControllerEffect` or `CreateTokenCopyOfEnteringTokenForTargetPlayerEffect`,
  plus Nakia, Fatal Grudge, Gilded Drake and Lucy MacLean: 164 test classes, 1,510 test cases, all passing. Then
  `ConfusionInTheRanksTest` (12) and `MarchOfTheMachinesTest` with the new test, passing.
- Application: `PredicateEvaluationServiceTest`, `EnterTriggerCollectorServiceTest` and `TriggerTargetCollectorTest`
  (258 tests), all passing. `TargetLegalityServiceTest` has 14 failures, the same 14 at HEAD `d3c9c6d5b2` (checked by
  stashing; stack, graveyard and colour cases outside the changed path).
- Engine: the whole module. The one failure is the known HEAD one, `GameMutationBoundaryRatchetTest`.
- AI: compiles. Its tests weren't run, since no AI code changed.
- The full suite was not run here.

#### Run 8: the damage source

Run 7's "for later runs" listed the damage and combat interfaces next. This run takes their damage-source half:
`DamageSourceAwareEffect` and `DamageSourceControllerAwareEffect`, the binding of "that source" and "that source's
controller" of a damage event into the effect. Both are gone. The stack entry carries the source as its triggering
permanent, and effects read it as they resolve. The four combat interfaces (`CombatDamageAmount`, `CombatDamageDealer`,
`CombatExcessDamage` and `AttackingCreatures…AwareEffect`) are untouched: 9 `…AwareEffect` interfaces are left.

**What the scan found.**

- 8 effects implemented the two interfaces, plus `SequenceEffect` as a delegator. Tephraderm's
  `DealDamageToDamageSourceCreatureOrSpellControllerEffect` bound the source; the seven `DamageSourceController…Effect`s
  (mills, sacrifices, poison counter, loses life, loses life unless sacrifices, gains control, exiles a random hand
  card) bound its controller and the damage.
- The binding sites:
  - `CombatDamageService.processDealtDamageTriggers` and `processCombatDamageReceivedTriggers`;
  - `CombatDamageService.requiresDistinctDamageSource`, which gave an ability one trigger per damage source because
    of its effect's class;
  - three `DamageTriggerCollectorService` handlers;
  - Visions of Brutality's rebuild in `TriggerCollectionService.collectEnchantedCreatureDealsDamageTriggers`.
- 9 card classes used them.
- Two engine-built uses of the gains-control effect come from the trigger-only wrapper
  `DamageSourceControllerGainsControlOfThisPermanentEffect`: Contested War Zone (`handleControlTheftOnDamage`) and
  Contested Game Ball (`TriggerCollectionService.queueCreatureCombatDamageToYouTriggers`).
- The binding fixed "that source's controller" as the ability triggered. Only Phyrexian Obliterator and Crag Saurian
  carried the source to read its current controller. Archfiend of Spite's official ruling (2019-08-23, read through
  the new `get_rulings` tool of the card-info MCP) says otherwise: "The player who loses life or permanents is the
  player who controls the source as Archfiend of Spite's triggered ability resolves … If the source is no longer in
  the zone it was in as it dealt damage, the source's last known information is used" (**CR 608.2h**, checked).

**The model.**

- "That source" is the entry's triggering permanent. The collector sets `setTriggeringPermanentId` (null for a spell
  source) and `setTriggeringPermanentControllerId` (the source's controller as it dealt the damage). "That much" is
  the event value, as before.
- `GameQueryService.findTriggeringPermanentController(gameData, entry)` (new) reads them. It returns the triggering
  permanent's current controller while the permanent is on the battlefield. Otherwise it returns the controller the
  entry recorded as it removed the permanent (`lastKnownPermanentController`), or else the recorded controller.
- The `TRIGGERING_PERMANENT_CONTROLLER` recipients use it:
  - `LoseLifeRecipient` and `PoisonRecipient` gain the value.
  - `MillRecipient.UNTAPPED_PERMANENT_CONTROLLER` became `TRIGGERING_PERMANENT_CONTROLLER`. It keeps its
    `eventPlayerIds` fallback for Mesmeric Orb.
  - `SacrificeRecipient` and `DamageRecipient` already had it.
- "Whenever a source deals damage to this" is `TriggerOptions.EACH_SOURCE` on the card (**CR 603.2c**, checked), as
  Nested Ghoul already wrote it. The combat collector reads only that option, and `requiresDistinctDamageSource` is
  gone. Combat sets the triggering permanent only for an "each source" ability, because a combined "is dealt damage"
  event has no single source. A noncombat damage event has one source, so the dealt-damage default
  (`addDealtDamageEntry`, now given the event context) always sets it.

**Gone, replaced by existing effects (AGENTS.md's reuse rule), cards by script.**

| Card | Effect now |
|---|---|
| Belltower Sphinx | `MillEffect(new EventValue(), MillRecipient.TRIGGERING_PERMANENT_CONTROLLER)` |
| Phyrexian Obliterator | `SacrificePermanentsEffect(new EventValue(), new PermanentTruePredicate(), SacrificeRecipient.TRIGGERING_PERMANENT_CONTROLLER)` |
| Reaper of Sheoldred | `GivePoisonCountersEffect(1, PoisonRecipient.TRIGGERING_PERMANENT_CONTROLLER)` |
| Visions of Brutality | `LoseLifeEffect(new EventValue(), LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER)` |

`DamageSourceControllerMillsEffect`, `…SacrificesPermanentsEffect`, `…GetsPoisonCounterEffect` and `…LosesLifeEffect`
and their handlers are gone, with the two interfaces.

**Kept, now reading the entry.** No existing effect expresses these four. Each lost its bound components.

- `DealDamageToDamageSourceCreatureOrSpellControllerEffect()` (Tephraderm). It damages the triggering permanent if
  that is still a creature, or else the recorded controller of a spell source.
- `DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect()` (Archfiend of Spite).
  `handleOpponentDamageSourceDealtDamage` still judges "a source an opponent controls" by the controller as the
  damage was dealt. The dealt-damage collector judges no trigger filter, so this condition stays a class-keyed
  handler.
- `DamageSourceControllerExilesRandomHandCardEffect()` (Rona, Tolarian Obliterator).
- `DamageSourceControllerGainsControlOfDamagedPermanentEffect()` (Crag Saurian, Contested War Zone). It keeps a
  nullable `newControllerId` for Contested Game Ball, built by its collector: that card's Oracle text says "the
  attacking player", not a creature's controller (its ruling: one trigger "no matter how many creatures deal combat
  damage to you"). The component goes with the Contested wrapper.

**Cards (by script).** `scripts/step15-damage-source-cards.py` (idempotent, per letter module) changed 9 classes: the
four users of the gone effects, Archfiend of Spite (its `(0, null)` marker arguments), and the "each source" option on
Belltower Sphinx, Phyrexian Obliterator, Reaper of Sheoldred, Archfiend of Spite, Crag Saurian, Rona, Tolarian
Obliterator and Tephraderm. It also renamed Mesmeric Orb's recipient.

**Behaviour changes.**

- Rules fix, **CR 608.2h** (checked), Archfiend of Spite's official ruling: for Belltower Sphinx, Reaper of Sheoldred,
  Archfiend of Spite, Rona, Tolarian Obliterator and Contested War Zone, "that source's controller" is now the
  source's controller as the ability resolves, not the one it had as it dealt the damage. New card tests, each
  changing the source's controller with Act of Aggression while the ability waits:
  - `BelltowerSphinxTest#sourcesControllerAsTheAbilityResolvesMills`
  - `ReaperOfSheoldredTest#sourcesControllerAsTheAbilityResolvesGetsThePoisonCounter`
  - `ArchfiendOfSpiteTest#sourcesControllerAsTheAbilityResolvesLosesTheLife`
  - `ContestedWarZoneTest#creaturesControllerAsTheAbilityResolvesGainsControl`

  These follow from the old binding (the controller was fixed at trigger time); they weren't run at HEAD. Rona shares
  Archfiend's code path and has no new test.
- Phyrexian Obliterator resolves through `SacrificePermanentsEffectHandler`. That handler applies the checks the
  bespoke one skipped: an effect that can't make the player sacrifice, permanents that can't be sacrificed, and creature
  tokens protected from resolving triggered abilities. All Obliterator tests pass unchanged.
- Contested War Zone's entry now carries the attacking creature as its triggering permanent.
- Every noncombat dealt-damage entry carries its source as the triggering permanent. No dealt-damage card read the
  triggering permanent before (checked by grep).

**Kept, with their reason.**

- The four combat interfaces: a later run.
- Archfiend's opponent check as a class-keyed handler: the dealt-damage collectors judge no trigger filter.
- `DamageSourceControllerGainsControlOfThisPermanentEffect` (Contested War Zone and Game Ball): a trigger-only wrapper
  whose collectors build the resolution effect. It goes with the wrapper classes.
- Tephraderm's two Oracle abilities ("a creature" / "a spell") are one ability with one effect. Splitting them needs
  a filter on the damage source, which no dealt-damage collector judges.

**For later runs.** The combat interfaces (`CombatDamageAmount`, `CombatDamageDealer`, `CombatExcessDamage` and
`AttackingCreatures…AwareEffect`), the bindings that aren't trigger events (`ChosenCard`, `AcceptedPlayers`,
`GrantingPermanent`, `ProducedManaColor`, `TriggeringCards`), Bill Potts, the may-unwrapping and the `…Triggering…`
classes, as run 1 listed. The doc lines for Souls of the Faultless named `DamageSourceControllerLosesLifeEffect`,
which the card no longer used (it is `LoseLifeEffect(…, ACTIVE_PLAYER)`). They are corrected.

**Scripts.**

- `scripts/step15-damage-source-cards.py`: the card classes.
- `scripts/step15-damage-source-tests.py`: the four new card tests, per letter test module.
- `scripts/step15-damage-source-docs.py`: exact line edits in `EFFECTS_INDEX.md` (with the new `LoseLifeRecipient`
  row), `EFFECTS_QUICK_REFERENCE.md`, `ORACLE_TEXT_EFFECT_MAP.md` and two `CARD_PATTERNS` files.

The effects, recipients, handlers, collectors and `GameQueryService` were edited by hand, as was `ARCHITECTURE.md`'s
triggering-permanent paragraph. `AGENTS.md` and the `implement-card` / `review-card` skills now point at the card-info
MCP's `get_rulings` (committed separately).

**Tests changed.**

- `DamageTriggerCollectorServiceTest`: the binding cases are gone. "ON_DEALT_DAMAGE binds the source controller for
  control gain", the two `DamageSourceSacrifice` cases and the two `DamageSourcePoisonCounter` cases asserted bound
  effect fields. They became the `DealtDamageSource` nest:
  - "The default queues the effect unchanged, with the damage source as the triggering permanent"
  - "A spell source has no triggering permanent, only its controller"
  - Archfiend of Spite's opponent check, both ways (it had no unit test).
- New unit tests:
  - `GameQueryServiceTest$FindTriggeringPermanentController`: 4 cases.
  - `MillEffectHandlerTest$TriggeringPermanentController`: 2 cases, the new amount and Mesmeric Orb's fallback.
- New card tests: the four above (by script). No existing card test changed.

**Test runs.**

- Card: the test class of every card with a "dealt damage", "dealt noncombat damage", "enchanted creature deals
  damage", "a permanent deals damage to you" or "a creature deals combat damage to you" trigger. Also Mesmeric Orb,
  both Rona faces, Nested Ghoul and Stuffy Doll. That is 109 test classes and 795 test cases, all passing.
- Application:
  - `DamageTriggerCollectorServiceTest`, `CombatDamageServiceTest`, `GameQueryServiceTest` and `MillEffectHandlerTest`
    have 5, 40, 1 and 0 failures. These are the known HEAD failures (the same named `DamageTriggerCollectorServiceTest`
    cases and `GameQueryServiceTest#conditionalExtraLandPlaysRequireTheirConditionAndApplyOnlyToTheirController`).
  - The architecture tests `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest`, `CardEffectTargetingConsistencyTest`
    and `CostEffectClassificationTest` fail as at HEAD. `TargetSpecRatchetTest`, `TriggerCollectorRegistryTest` and
    `CardFreezeTest` pass.
- Engine: the whole module. The one failure is the known HEAD one, `GameMutationBoundaryRatchetTest`.
- AI: compiles. Its tests weren't run, since no AI code changed.
- The full suite was not run here.

#### Run 9: the event's permanents

Run 8 left four combat interfaces. This run takes the two that bound a *group of permanents* from the event into the
effect: `CombatDamageDealerAwareEffect` (the creatures of a "one or more creatures deal combat damage" batch) and
`AttackingCreaturesAwareEffect` (the matching attackers of "whenever you attack with one or more …"). Both are gone,
with `AttackingPermanentSnapshot`. The two that bind an *amount*, `CombatDamageAmountAwareEffect` and
`CombatExcessDamageAwareEffect`, are left for a run of their own (below): 7 `…AwareEffect` interfaces are left.

**What the scan found.**

- `CombatDamageDealerAwareEffect`: 5 effects plus `MayEffect` and `SequenceEffect` as delegators, bound in one place,
  the `ONE_OR_MORE` branch of `CombatDamageService.checkAllyCreatureCombatDamageToPlayerTriggers`. 6 cards (Descendants'
  Fury, Heroes in a Half Shell, Kaito, Dancing Shadow, Vodalian Tide Mage, Vulture, Feathered Fiend, Zurgo and Ojutai).
  `SacrificeOneOfCombatDamageDealersThenRevealMatchingCreatureEffect` was dead: no card used it (Descendants' Fury uses
  the `…UntilSharedCreatureType` one), though `ORACLE_TEXT_EFFECT_MAP.md` named it for Descendants' Fury.
- `AttackingCreaturesAwareEffect`: 4 effects, bound in `CombatAttackService`'s "whenever you attack" collector and, for
  Valor's Reach, `TriggerCollectionService.checkPlanarAllyCreaturesAttackTriggers`. The effect's predicate was the
  trigger condition ("no matching attacker means no trigger"); the collector threw on a trigger filter. 5 cards (Lulu's
  green face, Tyvar the Bellicose, Valor's Reach, Vrestin, Menoptra Leader, Witch-king, Sky Scourge).
- The stack entry already had both stores: `triggeringPermanentIds` (an event's group of permanents) and
  `attackingPermanentSnapshots` (the declared attackers with last-known characteristics), and existing effects and
  amounts read the snapshots (`BoostAllOwnCreaturesEffect.forDeclaredAttackers`,
  `TotalPowerOfControlledCreatures.declaredAttackers`, `GrantKeywordEffect` with `GrantScope.TARGETS` and the
  attack collector's declared recipients).

**The model.**

- "Whenever you attack with one or more [filter] creatures" is `Triggers.youAttack().withFilter(filter)`. The collector
  judges the filter against each declared attacker as the ability triggers (**CR 603.2**, checked): no match, no
  trigger. For that ability, the matching attackers stand in for the declared ones: the entry's attacking snapshots,
  its declared `GrantScope.TARGETS` recipients, its attacker count, and its triggering permanents
  (`setTriggeringPermanentIds`). A filtered ability is its own group, so the filter is its group's: a printed or
  granted filtered ability never shares a stack object with the first printed ability (**CR 603.3**, checked).
- A "one or more" combat damage entry carries the matching dealers as its triggering permanents. The collector computes
  them for every `ONE_OR_MORE` ability (it computed them only for the dealer-aware effects).
- `EachPermanentScope.TRIGGERING_PERMANENTS` (new value): the entry's triggering permanents wherever they are on the
  battlefield. Only `PutCounterOnEachMatchingPermanentEffect` reads it; the other `EachPermanentScope` readers treat an
  unknown scope as `ALL_PLAYERS`, so don't use it with them.
- Valor's Reach (a plane): the planar "whenever you attack" collector no longer binds; when the ability has a
  `GrantScope.TARGETS` grant, the entry's declared recipients are the attacking player's attacking creatures, as the
  battlefield collector does for Love on the Battlefield.

**Gone, replaced by existing effects (AGENTS.md's reuse rule), cards by script.**

| Card | Now |
|---|---|
| Tyvar the Bellicose | `youAttack().withFilter(Elf)`, `GrantKeywordEffect(DEATHTOUCH, GrantScope.TARGETS)` |
| Valor's Reach | `GrantKeywordEffect(DOUBLE_STRIKE, GrantScope.TARGETS)` (its `ExactlyAttackers(2)` "if" unchanged) |
| Lulu, Wild Hollyphant | `youAttack().withFilter(other flyers)`, `BoostAllOwnCreaturesEffect(2, 2).forDeclaredAttackers()` |
| Witch-king, Sky Scourge | `youAttack().withFilter(Wraith)`, `ExileTopCardsMayPlayThisTurnEffect(TotalPowerOfControlledCreatures.declaredAttackers(null))` |
| Vrestin, Menoptra Leader | `youAttack().withFilter(Insect)`, `PutCounterOnEachMatchingPermanentEffect(PLUS_ONE_PLUS_ONE, 1, PermanentTruePredicate, TRIGGERING_PERMANENTS)` |
| Heroes in a Half Shell, Vulture, Feathered Fiend | the same counter effect in place of `PutCountersOnCombatDamageDealersEffect` |

`BoostTriggeringAttackersEffect`, `GrantKeywordToTriggeringAttackersEffect`, `PutCounterOnTriggeringAttackersEffect`,
`ExileTopCardsForAttackingCreaturesMayPlayThisTurnEffect`, `PutCountersOnCombatDamageDealersEffect` and their handlers
are gone, and so is the dead `SacrificeOneOfCombatDamageDealersThenRevealMatchingCreatureEffect` with its handler, its
`PermanentChoiceContext` record and its two choice-handler branches.

**Kept, now reading the entry.** No existing effect expresses "choose one of them, then …":
`ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect()`, `ReturnOneOfCombatDamageDealersToHandThenEffect(then,
description)` and `SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect()` lost their dealer ids and
read `entry.getTriggeringPermanentIds()`. Their cards didn't change (the no-argument constructors stay).

**Behaviour changes.** No existing card test changed.

- A filtered "whenever you attack" ability is its own stack object (CR 603.3). Before, Tyvar's, Lulu's, Witch-king's and
  Vrestin's ability shared an entry with any "whenever you attack" ability granted to the same permanent. No card test
  covers a granted one.
- Tyvar's deathtouch and Lulu's +2/+2 go through the shared handlers, which don't skip a matching attacker that stopped
  being a creature before resolution (the old handlers did). The objects are fixed as the ability triggers, and **CR
  611.2c** (checked) fixes the affected set when the effect begins, so the boost and keyword apply to those objects;
  it matters only if the permanent is a creature again later that turn. No test.
- Every "one or more creatures deal combat damage" entry that reaches the plain stack-entry path now carries its
  dealers as triggering permanents. No handler that reads them is used by such a card besides the three above and the
  counter effect (checked by grep: Fear of Ridicule's token copy goes through `TokenCopySupport`).

**Kept, with their reason.**

- `CombatDamageAmountAwareEffect` (37 implementers) and `CombatExcessDamageAwareEffect` (Overclocked Electromancer):
  the event's amount, a run of its own. Their snapshots (`snapshotCombatDamage`) fix a graveyard target's mana value
  before a stack entry exists, like the `snapshot…ManaValue` helpers of runs 3 to 6. The excess-damage collector
  (`processCombatDamageToCreatureTriggers`) reads only effects, not the ability's options, so Electromancer's "if that
  creature was dealt excess damage this turn" can't be an intervening "if" there yet.
- The dealers ride only the plain stack-entry path of the ally combat damage collector. Its modal, graveyard-target
  and `AttackTriggerTarget` paths don't carry them; no dealer effect reaches them. Likewise a filtered "whenever you
  attack" ability's targeted, graveyard-target and modal paths get the narrowed attacker list but no triggering
  permanents; none of the five cards is targeted.
- The "whenever you attack" readers outside the battlefield collector (command zone, exile, graveyard, temporary
  global triggers) don't judge a trigger filter; no filtered card works from there.
- Lulu's other four faces still write "one or more other creatures with flying" as an intervening "if"
  (`MinimumMatchingAttackers`) with the count as event value. The filter would say it better, and an intervening "if"
  rechecked as the ability resolves is wrong if the flyers left (**CR 603.4**, checked); unchanged here.
- `GrantKeywordEffect` with `GrantScope.TARGETS` and a filter (Frontier Warmonger) is still rewritten by the attack
  collector into declared recipients as the ability triggers; a later run can make that filter a trigger filter too.

**For later runs.** The event's amount (`CombatDamageAmountAwareEffect`, `CombatExcessDamageAwareEffect`), the bindings
that aren't trigger events (`ChosenCard`, `AcceptedPlayers`, `GrantingPermanent`, `ProducedManaColor`,
`TriggeringCards`), Bill Potts, the may-unwrapping and the `…Triggering…` classes, as run 1 listed.

**Scripts.**

- `scripts/step15-event-permanents-cards.py` (idempotent, per letter module): the 7 card classes above.
- `scripts/step15-event-permanents-tests.py` (idempotent, per letter test module): two new Witch-king tests.
- `scripts/step15-event-permanents-docs.py`: exact line edits in `EFFECTS_INDEX.md`, `EFFECTS_QUICK_REFERENCE.md` (the
  "whenever you attack with one or more …" line and the new scope), `ORACLE_TEXT_EFFECT_MAP.md` (with Descendants'
  Fury's row pointing at the effect it uses), `CARD_PATTERN_INDEX.md`, `CARD_PATTERNS_CREATURES_TRIGGERED.md` and
  `ACTIVATED_ABILITY_GUIDE.md` (the `youAttack()` filter).

The effects, scope, handlers and collectors were edited by hand, as were `Triggers.youAttack()`'s Javadoc and
`ARCHITECTURE.md`'s triggering-permanent paragraph. The skills didn't name these classes.

**Tests changed.**

- New card tests (by script): `WitchKingSkyScourgeTest#doesNotTriggerWhenNoWraithAttacks` (the trigger filter; an
  unfiltered trigger would exile the Bears' 2) and `WitchKingSkyScourgeTest#wraithThatLeftCountsWithItsLastKnownPower`
  (the card's official ruling, **CR 608.2h**, checked). The card had one test.
- No existing test changed; no unit test named the removed classes.

**Test runs.**

- Card: before the new tests, the test class of every card with a `Triggers.youAttack()` / `playerAttacks()` trigger
  or a "creature you control deals combat damage to a player (or battle)" trigger, every user of
  `PutCounterOnEachMatchingPermanentEffect`, `forDeclaredAttackers` / `declaredAttackers` and the three kept effects,
  and every plane test: 576 test classes, 4,476 test cases, all passing. Then the converted cards' classes, Love on
  the Battlefield and the new tests, all passing (the 11 converted or affected classes also passed at HEAD).
- Application: `CombatAttackServiceTest`, `CombatDamageServiceTest`, `PlanechaseServiceTest`, `CardFreezeTest`,
  `TriggerCollectorRegistryTest`, `TargetSpecRatchetTest` and the architecture tests. The failures are the known HEAD
  ones: `CombatDamageServiceTest` 40, `PlanechaseServiceTest` 2, and `SimulationCopyCompletenessTest`,
  `CardImmutabilityArchTest`, `CardEffectTargetingConsistencyTest`, `CostEffectClassificationTest` 1 each; their
  messages name none of the changed classes.
- Engine: the whole module. The one failure is the known HEAD one, `GameMutationBoundaryRatchetTest`.
- AI: compiles (`compileTestJava`). Its tests weren't run, since no AI code changed.
- The full suite was not run here.

#### Run 10: the event's amount

Run 9 left the two interfaces that bind an *amount*. This run takes `CombatDamageAmountAwareEffect`, the binding of a
"deals combat damage to a player" trigger's "that many" / "that much" into the effect. It is gone: the stack entry
carries the damage as its event value, set by the collector whatever the effect. `CombatExcessDamageAwareEffect`
(Overclocked Electromancer) is left for a run of its own (below): 6 `…AwareEffect` interfaces are left.

**What the scan found.**

- 36 records implemented the interface. 33 only exposed their amount (`combatDamageAmount()`) so that
  `CombatDamageService.readsCombatDamage` could decide whether to set the entry's event value. Seven also rebuilt
  themselves with the damage as the ability triggered (`snapshotCombatDamage`):
  - read as the ability resolves: `SeekCardsToHandEffect` (Soulstealer Axe), `MayCastAnySpellFromHandWithoutPayingManaCostEffect`
    (Buster Sword, Glamdring, Aisha of Sparks and Smoke), `DealDamageToTargetPermanentEffect` (Rampaging Raptor) and
    `DealDamageToTargetCreatureOrPlaneswalkerEffect` (Questing Beast). The two damage effects were rebuilt because their
    target is chosen through `AttackTriggerTarget`, whose entry had no event value;
  - a graveyard target chosen as the ability goes on the stack: `ReturnCardFromGraveyardEffect` (Venerable Warsinger's
    "mana value X or less"), `ExileTargetCardFromGraveyardAndMayCastCopyEffect` (Soundwave's "mana value equal to the
    damage dealt") and `ExileUpToCombatDamageCardsFromGraveyardEffect` (Froghemoth's "up to that many").
- The rebuild sites: `processCombatDamageToPlayerTriggers` (every path), the same-name collector and the ally
  collector. The event value was set only by `setCombatDamageEventValue` (the plain, same-name, attached and curse
  entries) and the may path, each through `readsCombatDamage`; the targeted paths had none.
- Two spell-side readers used the interface as "an effect reads the event value": `FightSupport` and
  `DealDamageToTargetCreatureEffectHandler`, which store the excess damage on the spell's entry when a later effect
  reads it. A card scan (every card with a fight or creature-damage effect and an `EventValue`) found three users:
  The Last Agni Kai (`AwardPersistentManaEffect`), Goblin Negotiation (`CreateTokenEffect`) and Archaic's Agony
  (`ExileTopCardsMayPlayUntilNextTurnEffect`, already named by the handler).
- `ExileRandomCardWithManaValueFromDamagedPlayerLibraryEffectHandler` evaluated the effect's `combatDamageAmount()`.

**The model.**

- Every entry the "deals combat damage to a player" collectors build (self, same name, attached Aura or Equipment,
  curse, one or more allies) has the damage dealt as its event value; the ally collector keeps its batch total. The
  may path queues it as the pending may's event value.
- `PermanentChoiceContext.AttackTriggerTarget` gains `eventValue` (`withEventValue`), and `ETBTokenMultiTargetTrigger`
  `withEventValue` for the event value it already had. The combat collectors give every targeted choice the damage,
  and every entry built from an `AttackTriggerTarget` (`PermanentChoiceTriggerHandlerService.handleAttackTrigger`, both
  forms, and the declined and no-target entries of `TriggeredAbilityQueueService.processNextAttackTriggerTarget`)
  carries it.
- `GraveyardCardChoosingEffect.graveyardChoiceMaxTargets(int eventValue)` (new, default the fixed count): "up to that
  many". `ExileUpToCombatDamageCardsFromGraveyardEffect` lost its component and answers it with the damage;
  `GraveyardTargetingService.handleCombatDamageGraveyardChoiceTargeting` takes the damage.

**Gone.** `CombatDamageAmountAwareEffect`, every `combatDamageAmount()` and the four resolution-time
`snapshotCombatDamage`s (`scripts/step15-combat-damage-amount-records.py`), `readsCombatDamage`,
`setCombatDamageEventValue` and the collectors' rebuilds. The excess readers name the effects their cards use
(`AwardPersistentManaEffect` in `FightSupport`, `CreateTokenEffect` in `DealDamageToTargetCreatureEffectHandler`), as
each already did for its other readers. Kamachal's handler reads the entry's event value.

**Kept, with their reason.**

- The graveyard targets whose restriction reads the damage, run 3's reason (a graveyard target is chosen before a stack
  entry exists): `CombatDamageService.withGraveyardTargetFixedToDamage` fixes Venerable Warsinger's limit
  (`ReturnCardFromGraveyardEffect.withMaxManaValue(int)`, a plain method that the two death-trigger rebuilds,
  `snapshotDyingPermanentManaValue` and `snapshotDynamicMaxManaValue`, now share) and Soundwave's
  (`ExileTargetCardFromGraveyardAndMayCastCopyEffect.withManaValueEqualToDamage(int)`) on every graveyard-target path
  of the self and ally collectors. Before, the self collector's generic graveyard path was narrowed by the general
  rebuild; it still is.
- `DamagedPlayerControlsTargetEffect.forDamagedPlayer(defenderId, damageDealt)` (Mordant Dragon's
  `DealDamageToTargetCreatureDamagedPlayerControlsEffect`) still binds the damaged player and the damage into the
  effect; it isn't one of the `…AwareEffect` interfaces. A later run can read both from the entry.
- `TriggeredModalTrigger` and `SpellGraveyardTargetTrigger` carry no event value; no combat damage mode or graveyard
  target effect reads one at resolution (the graveyard restrictions above are fixed as they are chosen).
- `CombatExcessDamageAwareEffect`: "if that creature was dealt excess damage this turn" is an intervening "if" the
  deals-combat-damage-to-a-creature collector can't judge yet (it reads only effects, not the ability's options), and
  the excess is a different amount from the damage that collector's other entries would carry.

**Behaviour changes.** No card test changed.

- A combat damage trigger's entry has the damage as its event value on every path, including the targeted and attached
  ones that had none (an attached Aura's or Equipment's targeted "that much" read 0). No card was found that reaches
  such a path with an `EventValue` (checked by a scan of every card with a combat damage trigger and an `EventValue`).
- Rampaging Raptor, Questing Beast, Soulstealer Axe and the free-cast Equipment read the damage from the entry as the
  ability resolves instead of a rebuilt effect: the same number.

**For later runs.** `CombatExcessDamageAwareEffect` (with the to-creature collector reading the abilities), the bindings
that aren't trigger events (`ChosenCard`, `AcceptedPlayers`, `GrantingPermanent`, `ProducedManaColor`,
`TriggeringCards`), the graveyard-target rebuilds kept since run 3 (now with Warsinger's and Soundwave's),
`DamagedPlayerControlsTargetEffect`'s binding, Bill Potts, the may-unwrapping and the `…Triggering…` classes.

**Scripts.**

- `scripts/step15-combat-damage-amount-records.py`: the 33 records (implements list, `combatDamageAmount()`, the four
  resolution-time snapshots, unused imports). Its first version also collapsed unrelated double blank lines; that was
  fixed and every converted record was checked against a clean rerun on HEAD's files.
- `scripts/step15-combat-damage-amount-docs.py`: exact edits in `ORACLE_TEXT_EFFECT_MAP.md` (Empyreal Voyager,
  Froghemoth, Mark of Sakiko), `EFFECTS_INDEX.md` and `EFFECTS_QUICK_REFERENCE.md` (the interface's lines, Froghemoth,
  `graveyardChoiceMaxTargets(int)`, and a line on a combat damage trigger's `EventValue`).

The three graveyard-target records, the contexts, collectors, handlers and `GraveyardTargetingService` were edited by
hand, as was a sentence in `ARCHITECTURE.md`'s damage paragraph. No card class changed; the skills didn't name the
interface.

**Tests changed.**

- New unit test `GraveyardTargetingServiceTest` "combat damage graveyard targeting caps \"up to that many\" at the damage
  dealt" (Froghemoth's limit through the new parameter).
- No existing test changed.

**Test runs.**

- Card: the test class of every card naming combat damage, a dynamic graveyard mana value limit,
  `ExileTargetCardFromGraveyardAndMayCastCopyEffect`, `MayCastAnySpellFromHandWithoutPayingManaCostEffect` or
  `SeekCardsToHandEffect`, and every fight or creature-damage card with an `EventValue`: 1,094 test classes, 8,781 test
  cases, all passing.
- Application: `GraveyardTargetingServiceTest`, `CombatDamageServiceTest`, `DealDamageToTargetCreatureEffectHandlerTest`,
  `DeathTriggerCollectorServiceTest`, `LegacyTriggerBindingsTest`, `CardFreezeTest`, `TriggerCollectorRegistryTest`,
  `TargetSpecRatchetTest` and the architecture tests. The failures are HEAD's: `CombatDamageServiceTest` 40,
  `DealDamageToTargetCreatureEffectHandlerTest` 7 (`DamageJournalService` not wired) and `GraveyardTargetingServiceTest`
  1 (`handleReturnToHandETBTargeting`, strict stubbing), both checked at HEAD `8e9ceb01a3` in a worktree; the Tiana case
  of `DeathTriggerCollectorServiceTest`; `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest`,
  `CardEffectTargetingConsistencyTest` and `CostEffectClassificationTest` 1 each.
- Engine: the whole module. The one failure is the known HEAD one, `GameMutationBoundaryRatchetTest`.
- AI: compiles; `GameDataDeepCopyTest` passes.
- The full suite was not run here.

#### Run 11: the excess damage

Run 10 left `CombatExcessDamageAwareEffect`, the binding of "that excess damage" of a "deals combat damage to a
creature" trigger into the effect. It is gone: the stack entry carries the excess as its event value, set by the
collector whatever the effect, and the collector judges each ability's intervening "if" against it as the ability
triggers. 5 `…AwareEffect` interfaces are left, none of them a trigger event's: `ChosenCard`, `AcceptedPlayers`,
`GrantingPermanent`, `ProducedManaColor` and `TriggeringCards`.

**What the scan found.**

- One implementer, `GainEnergyEqualToExcessCombatDamageEffect` (Overclocked Electromancer), with one reader,
  `CombatDamageService.processCombatDamageToCreatureTriggers`: it skipped the effect when the excess was 0 (the card's
  intervening "if", written as the effect's class) and set the excess as the entry's event value only for it.
- The collector read the abilities' effects (`printedCombatEffects` and `grantedCombatEffects`), not the abilities, so
  it could judge no option; run 10 left the interface for that reason.
- 23 card classes use `Triggers.dealsCombatDamageToCreature()`, plus two grants (Kaldra Compleat's germ and Snarlfang
  Vermin's `SuspectAndPerpetuallyGrantAbilityToTargetCreatureEffect`). None but Electromancer is written with an
  intervening "if", event condition or filter, and none reads `EventValue`.

**The model.**

- The collector keeps each damage source's abilities (printed, until end of turn / combat / next upkeep, until it
  leaves, granted by static abilities, in that order, captured before combat deaths as before) instead of their
  effects. For each creature dealt damage it computes the excess (**CR 120.4a**, checked: marked damage, damage from
  other sources at the same time and deathtouch taken into account; the existing `combatExcessDamage`), judges the
  abilities with `ConditionEvaluationService.judge(gameData, ctx, excess)` (context: the source, its controller and the
  damaged creature as target id), reads the effects of those that trigger through `CombatAbilityReader.effects`, and
  gives every entry the excess as its event value.
- Overclocked Electromancer (by script): `TriggerOptions.DEFAULT.withInterveningIf(new EventValueAtLeast(1))` and the
  existing `EnergyCountersEffect(new EventValue())`. The intervening "if" (**CR 603.4**, checked) is judged with the
  excess as the ability triggers: no excess, no trigger. The reader wraps it as a `ConditionalEffect` checked again on
  resolution against the entry's event value, which doesn't change.
- `Triggers.dealsCombatDamageToCreature()`'s Javadoc says what the event value is.

**Gone.** `CombatExcessDamageAwareEffect`, `GainEnergyEqualToExcessCombatDamageEffect` and its handler, and the
collector's two `instanceof` checks.

**Behaviour changes.** No existing card test changed.

- Every "deals combat damage to a creature" entry carries the excess (often 0) as its event value; before, only
  Electromancer's did, the others 0. No card reads it (checked by grep).
- The collector judges the intervening "if" and event condition of every such ability as it triggers. None but
  Electromancer has one.
- "If that creature was dealt excess damage this turn" is judged on this combat damage step's excess. A creature dealt
  excess damage earlier this turn (an indestructible one, say) and none in this step would, by the Oracle text, still
  let the ability trigger, but "that excess damage" would then be 0 and the controller gets no energy either way; only
  whether an ability goes on the stack differs. Unchanged from before (the turn-long set
  `GameData.permanentsDealtExcessDamageThisTurn` exists, but no condition reads it for the triggering creature).

**For later runs.** The bindings that aren't trigger events (`ChosenCard`, `AcceptedPlayers`, `GrantingPermanent`,
`ProducedManaColor`, `TriggeringCards`), the graveyard-target rebuilds kept since run 3 (with Warsinger's and
Soundwave's), `DamagedPlayerControlsTargetEffect`'s binding, Bill Potts, the may-unwrapping and the `…Triggering…`
classes.

**Scripts.**

- `scripts/step15-excess-damage-cards.py` (idempotent, per letter module): Overclocked Electromancer.
- `scripts/step15-excess-damage-tests.py` (idempotent, per letter test module): two new Electromancer tests.
- `scripts/step15-excess-damage-docs.py`: exact edits in `ORACLE_TEXT_EFFECT_MAP.md` (Electromancer's row),
  `EFFECTS_INDEX.md` (the two rows gone), `EFFECTS_QUICK_REFERENCE.md` (a line on the to-creature trigger's
  `EventValue` in place of the two) and `ACTIVATED_ABILITY_GUIDE.md` (the trigger's row).

The collector, the trigger factory's Javadoc and a sentence in `ARCHITECTURE.md`'s damage paragraph were edited by hand.
The skills didn't name these classes.

**Tests changed.**

- New card tests (by script): `OverclockedElectromancerTest#doesNotTriggerWithoutExcessDamage` (the intervening "if"
  judged as the ability triggers: the log shows no trigger, not just no energy) and
  `OverclockedElectromancerTest#triggersOnceWithExcessDamage` (the same log line appears once, so the first test isn't
  vacuous). The harness resolves the trigger as combat damage ends, so neither can inspect the stack entry; a first
  version that asserted the entry's event value found an empty stack and was rewritten.
- No existing test changed; no unit test named the removed classes.

**Test runs.**

- Card: the test class of every card with a `Triggers.dealsCombatDamageToCreature()` trigger, an
  `EventValueAtLeast` condition, an `EnergyCountersEffect(new EventValue())` or the suspect-and-grant effect: 76 test
  classes, 591 test cases. The one failure was the first version of the new event-value test (above); after the
  rewrite `OverclockedElectromancerTest` passes, 12 cases.
- Application: `CombatDamageServiceTest`, `CardFreezeTest`, `TriggerCollectorRegistryTest`, every `…RatchetTest` and
  the architecture tests. The failures are HEAD's: `CombatDamageServiceTest` 40 (`StoriedEffectHandler` not wired, and
  one null key), and `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest`,
  `CardEffectTargetingConsistencyTest` and `CostEffectClassificationTest` 1 each; their messages name none of the
  changed classes.
- Engine: the whole module. The one failure is the known HEAD one, `GameMutationBoundaryRatchetTest`.
- AI: compiles (`compileTestJava`).
- The full suite was not run here.

#### Run 12: the event's cards

Run 11 left 5 `…AwareEffect` interfaces. Four bind something that isn't a trigger event (a hand choice, a multi-player
choice, the granting permanent, a mana ability's color); the fifth, `TriggeringCardsAwareEffect`, bound the cards of a
"one or more cards" event ("those cards", "the discarded cards") into the effect. It is gone: the stack entry carries
the cards, set by the collector whatever the effect. 4 `…AwareEffect` interfaces are left, none of them a trigger
event's: `ChosenCard`, `AcceptedPlayers`, `GrantingPermanent` and `ProducedManaColor`.

**What the scan found.** Three implementers, one card each, and three binding sites:

- `SeekCardSharingCardTypeWithDiscardedCardsEffect` (Diviner of Fates), bound by its own discard-event collector in
  `DiscardTriggerCollectorService`;
- `PutCounterThenMayBecomeCopyOfCardsEffect` (Spirit of Resilience), bound by
  `MiscTriggerCollectorService.handleControllerArtifactOrCreatureCardsLeaveGraveyard` (the default of both
  leaves-the-graveyard watches);
- `EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect` (Polluted Cistern's granted ability), bound by
  `handleCardPutIntoGraveyardDefault` for a library-to-graveyard batch.

The rulings of Spirit of Resilience (it "copies exactly what was printed on the original card") confirm the effect
wants the `Card`s as they were in the event, not ids looked up again.

**The model.**

- `StackEntry.setTriggeringCards(List<Card>)` / `getTriggeringCards()` (new): the cards a "one or more cards" event was
  about, an immutable list the copy constructor shares (the cards are frozen), so the simulation copy has it.
- The three collectors set it on the entry they build: the discard-event seek collector; the leaves-the-graveyard
  default for every non-targeted entry; the card-put-into-graveyard default for every non-targeted entry of a
  library-to-graveyard batch.
- `CardTypesAmongTriggeringCards` (new `DynamicAmount`): "for each card type among those cards", counted as the old
  effect did (each `CardType` the card has, once).
- Polluted Cistern (by script): `LoseLifeEffect(new CardTypesAmongTriggeringCards(), LoseLifeRecipient.EACH_OPPONENT)`.
  The two other effects lost their component and read the entry; their cards already used the no-argument
  constructors.

**Gone.** `TriggeringCardsAwareEffect`, `EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect` and its
handler, the two records' card lists and `withTriggeringCards`, and the three bindings.

**Kept, with their reason.**

- The targeted paths of the two `MiscTriggerCollectorService` defaults (`SelfTriggeredAbilityTarget`) don't carry the
  cards: no targeted effect reads them, and none was bound before (a bound effect in a targeted ability was only ever
  one of the three, none targeted).
- `StackEntry.triggeringCardIds` (ids, set by two discard collectors, one exile collector and two discard
  follow-ups, read by five effect handlers) is a sibling of the new list. A later run can merge them; most of its
  readers look for the cards in the graveyard, and `CreateTokensForDistinctDiscardedCardTypesEffectHandler` finds them
  by id (`GameQueryService.findCardById`), which misses a card that ceased to exist.
- `BecomeCopyOfOneOfCardsUntilEndOfTurnEffect(cards)`, which Spirit of Resilience's handler builds for its may
  question, is a resolution-built effect, not a trigger binding.

**Behaviour changes.** None. A library-to-graveyard or leaves-the-graveyard entry now carries the event's cards
whatever its effect; only the three effects read them.

**For later runs.** The bindings that aren't trigger events (`ChosenCard`, `AcceptedPlayers`, `GrantingPermanent`,
`ProducedManaColor`), merging `triggeringCardIds` into the triggering cards, the graveyard-target rebuilds kept since
run 3 (with Warsinger's and Soundwave's), `DamagedPlayerControlsTargetEffect`'s binding, Bill Potts, the may-unwrapping
and the `…Triggering…` classes.

**Scripts.**

- `scripts/step15-triggering-cards-cards.py` (idempotent, per letter module): Polluted Cistern.
- `scripts/step15-triggering-cards-docs.py` (idempotent): exact edits in `ORACLE_TEXT_EFFECT_MAP.md` (Spirit of
  Resilience, Polluted Cistern), `EFFECTS_INDEX.md` (the seek row, the drain row gone, the new amount),
  `EFFECTS_QUICK_REFERENCE.md` (Spirit of Resilience, Polluted Cistern) and `CARD_PATTERNS_CREATURES_TRIGGERED.md`
  (Spirit of Resilience).

The records, handlers, collectors, `StackEntry`, `AmountEvaluationService` and a sentence in `ARCHITECTURE.md` were
edited by hand. The skills didn't name these classes.

**Tests changed.**

- New unit tests: `MiscTriggerCollectorServiceTest#cardsLeavingGraveyardAreTheEntrysTriggeringCards`,
  `MiscTriggerCollectorServiceTest#cardsPutIntoGraveyardFromLibraryAreTheEntrysTriggeringCards` and
  `DiscardTriggerCollectorServiceTest#discardEventSeekHasTheDiscardedCardsAsItsTriggeringCards`.
- No existing test changed; no test named the removed classes.

**Test runs.**

- Card: the test class of every card with a `Triggers.leavesGraveyard`, `Triggers.putIntoGraveyardFrom`,
  `Triggers.putIntoGraveyardFromAnywhere[ButBattlefield]` or `Triggers.youDiscardOneOrMoreCards` trigger or naming one
  of the changed effects or the new amount: 164 test classes, 1,386 test cases, all passing.
- Application: `MiscTriggerCollectorServiceTest`, `DiscardTriggerCollectorServiceTest` (the three new cases pass),
  `CardFreezeTest`, `TriggerCollectorRegistryTest`, `CardEffectTargetingConsistencyTest`, every `…RatchetTest` and the
  architecture tests: 138 cases. The 4 failures are HEAD's, 1 each in `SimulationCopyCompletenessTest`,
  `CardImmutabilityArchTest` (ArchUnit imported no class), `CardEffectTargetingConsistencyTest` and
  `CostEffectClassificationTest`; their messages name none of the changed classes.
- Engine: the whole module. The one failure is the known HEAD one, `GameMutationBoundaryRatchetTest`.
- AI: compiles (`compileTestJava`).
- The full suite was not run here.

#### Run 13a: the chosen card, the accepted players and the produced mana colors

Run 12 left 4 `…AwareEffect` interfaces, none of them a trigger event's. This run moves three; the fourth,
`GrantingPermanentAwareEffect`, is split off as run 13b (below). 1 `…AwareEffect` interface is left.

**What the scan found.** The three interfaces had 13 implementers, bound in 9 places, all of them choice handlers or
the effect handlers that hand a choice's result on, plus one collector:

- `ChosenCardAwareEffect` (10 implementers): `PutChosenCardFromHandOntoBattlefieldEffect`,
  `PerpetuallyGrantCardCharacteristicsEffect`, `PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect`,
  `MayPutSelectedCardOntoBattlefieldEffect`, `HoldDraftedSpellbookCardEffect`, `GrizzledHuntmasterEffect`,
  `ExileSameNameCardsFromHandAndLibraryThenSeekEffect`, `ExileCardFromHandWithManaValueTimeCountersEffect`,
  `ExileCardFromHandAndBecomeForetoldEffect`, `ChooseCreatureCardFromHandAndConjureDuplicateIntoHandEffect`, bound by
  the spellbook-draft, outside-the-game, exile-from-hand, revealed-hand and library-search choice handlers. Six of
  them were effects that ask for the card themselves and re-entered with the card bound (`null` meaning "ask").
- `AcceptedPlayersAwareEffect` (1): `DealDamageToPlayersEffect`'s `SELECTED_OPPONENTS` (Snort), bound by
  `EachPlayerMayDiscardHandThenDrawEffectHandler` after the last "you may".
- `ProducedManaColorAwareEffect` (2): C.A.M.P.'s `PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect`,
  bound by its land-tap collector and by the any-color choice that settles a parked tap, and
  `AddManaOfTypeProducedByTappedPermanentEffect`, which bound nothing and implemented the interface only so
  `ChoiceHandlerService` could find the parked abilities that wait for the color.

**The model.**

- The chosen card is the existing `StackEntry.chosenObjectCard` (already set by the revealed-hand and library-search
  handlers and read by `PerpetuallyGrantChosenCardCharacteristicsEffect` and others). The spellbook-draft,
  outside-the-game and exile-from-hand handlers now set it too, just before they insert their follow-up, and every
  handler inserts the follow-up unchanged.
- An effect that asks for the card and then acts on it inserts its own after-the-choice step as the choice's
  follow-up: a `cardChosen` component with an `afterChoice()` factory (`ExileCardFromHandAndBecomeForetoldEffect`,
  `ExileCardFromHandWithManaValueTimeCountersEffect`, `ExileSameNameCardsFromHandAndLibraryThenSeekEffect`,
  `ChooseCreatureCardFromHandAndConjureDuplicateIntoHandEffect`), or a `Step` for Grizzled Huntmaster's four steps
  (choose the hand card, exile the same-named cards, choose the outside card, conjure). The effect's own handler builds
  that step; no choice handler does.
- `MayPutSelectedCardOntoBattlefieldEffect`'s held-out library card (in no zone until "put it onto the battlefield?"
  is answered) rides from the entry's chosen card into the may ability: `PendingMayAbility.heldCard` (new component,
  `withHeldCard`), read by the may handler.
- Hinterland Chef's draft follow-up is the existing `PerpetuallyGrantChosenCardCharacteristicsEffect` (by script);
  `PerpetuallyGrantCardCharacteristicsEffect` lost its `cardId` and grants only to the source's own card.
- `StackEntry.acceptedPlayerIds` (new): the players who said yes to the resolution's "each player may", set by
  `EachPlayerMayDiscardHandThenDrawEffectHandler` before its follow-up; `SELECTED_OPPONENTS` reads it.
  `DealDamageToPlayersEffect` lost `selectedPlayerIds`, `withSelectedPlayerIds` and `withAcceptedPlayerIds`.
- `StackEntry.producedManaColors` (a set, replacing the single `producedManaColor`) and
  `StackEntry.awaitingProducedManaColor` (new): every mana-tap ability parked in `pendingManaAbilityTriggers` until an
  any-color choice settles the color is marked, and `ChoiceHandlerService` resolves the marked ones. C.A.M.P.'s parked
  entry carries its target filter, which tells `queueResolvedProducedManaTriggerTarget` to choose its target instead of
  resolving it; its target choice carries the colors (`SpellTargetTriggerAnyTarget.producedManaColors`, new component,
  `withProducedManaColors`) into the entry it builds, in all three places that build one.
- The copy constructor shares the new entry state (immutable), so simulation copies have it.

**Gone.** `ChosenCardAwareEffect`, `AcceptedPlayersAwareEffect`, `ProducedManaColorAwareEffect`, every
`withChosenCard` / `withAcceptedPlayerIds` / `withProducedManaColors` on an effect, the bound components (`cardId`,
`card`, `chosenCard`, `cardName`, `conjureSourceCard`, `selectedPlayerIds`, `producedManaColors`) and
`LibraryChoiceHandlerService.withChosenCard`.

**Split: run 13b, `GrantingPermanentAwareEffect`.** Its 6 implementers (`MayEffect`, which forwards,
`TransformGrantingPermanentEffect`, `SacrificePermanentThenEffect`'s granting-permanent modes,
`SacrificeGrantingEquipmentThenEffect`, `CreateTokenWithAttachedCountCountersEffect`, `BecomePreparedEffect`; 7 cards:
Optimus Prime, Autobot Leader, Stensian Sanguinist, Trickster's Talisman, Spare Dagger, Sunfire Torch, Dire
Blunderbuss, Animal Friend) are bound in three places: `GrantedTriggeredAbilitySupport.grantedTriggeredAbilities`
for an Aura's or Equipment's grant, and `GrantEffectToTargetUntilEndOfCombatEffectHandler` /
`GrantEffectToChosenCreatureUntilEndOfTurnEffectHandler` at grant time. Unlike this run's bindings, no choice carries
the granting permanent to the stack: the first is a reader behind 76 collector call sites, and the attack and combat
damage collectors that put these abilities on the stack flatten them into bare effects (`CombatAbilityReader.effectsOf`)
before they build entries. Moving it needs the granted `TriggeredAbility` to name the permanent that granted it and the
entries built from it to carry that (`StackEntry`), through the attack and combat-damage-to-a-player collectors at
least, with a guard so a granted ability another collector puts on the stack fails loudly instead of losing its
granting permanent. That is its own run.

**Behaviour changes.** None. Two edge cases keep their old behaviour: an exile-from-hand choice with a follow-up exiles
one card (every such effect asks for one), and the may-put fallback to the hand's last card is kept though every
current card reaches it through a library search.

**For later runs.** `GrantingPermanentAwareEffect` (13b), then runs 14 to 17 as planned.

**Scripts.**

- `scripts/step15-chosen-card-cards.py` (idempotent, per letter module): Hinterland Chef.
- `scripts/step15-chosen-card-docs.py` (idempotent): exact edits in `ORACLE_TEXT_EFFECT_MAP.md` (Hinterland Chef),
  `EFFECTS_INDEX.md` (the two perpetual grants, `PutChosenCardFromHandOntoBattlefieldEffect`, the discard-and-draw
  follow-up, `selectedOpponents`, `HoldDraftedSpellbookCardEffect`), `EFFECTS_QUICK_REFERENCE.md` (the two perpetual
  grants, the discard-and-draw follow-up, `selectedOpponents`) and `CARD_PATTERN_INDEX.md` (Hinterland Chef).

The records, handlers, choice handlers, collectors, `StackEntry`, `PendingMayAbility`, `PermanentChoiceContext` and a
paragraph in `ARCHITECTURE.md` were edited by hand. The skills didn't name these classes.

**Tests changed.**

- `LibraryChoiceHandlerServiceTest#selectedCardAwareFollowUpReceivesTheChosenCard` is now
  `selectedCardFollowUpIsInsertedUnchangedWithTheChosenCardOnTheEntry`: it asserted the follow-up bound with the card;
  it now asserts the follow-up inserted unchanged and the card as the entry's chosen card.
- New: `LandTapTriggerCollectorServiceTest#campTriggerParkedForTheManaColorTakesTheChosenColorIntoItsTargetChoice` and
  `DealDamageToPlayersEffectHandlerTest#selectedOpponentsTakeNoDamageWhenNoPlayerAccepted`;
  `LandTapTriggerCollectorServiceTest#waitsForTheLandsManaColorChoice` also asserts the parked entry is marked. A
  positive "damages the accepted opponents" unit case was dropped: `DealDamageToPlayersEffectHandlerTest` can't deal
  damage at HEAD (13 of its 15 cases fail on a null `DamageJournalService` in its mock wiring), so Snort's card test
  (`damagesOnlyAcceptingOpponents`) covers it through the engine.

**Test runs.**

- Card: the test class of every card that constructs a changed class, a spellbook draft or conjure, a library-search
  follow-up, a seek, a `DealDamageToPlayersEffect`, an "each player may" discard or shuffle, a hand-choice follow-up, an
  outside-the-game choice or a tap-for-mana trigger: 765 test classes (768 result files), 5,965 cases, all passing (1
  skipped).
- Application: `LibraryChoiceHandlerServiceTest`, `LandTapTriggerCollectorServiceTest`,
  `DealDamageToPlayersEffectHandlerTest`, `CardFreezeTest`, `SimulationCopyCompletenessTest`, `GameDataDeepCopyTest`,
  `TriggerCollectorRegistryTest`, `CardEffectTargetingConsistencyTest`, every `…RatchetTest` and the architecture
  tests. The failures are HEAD's: `DealDamageToPlayersEffectHandlerTest` 13 (rerun at HEAD with the change stashed: the
  same 13 of 15), `LandTapTriggerCollectorServiceTest` 2 (the known pair), and 1 each in
  `SimulationCopyCompletenessTest`, `CardImmutabilityArchTest`, `CardEffectTargetingConsistencyTest` and
  `CostEffectClassificationTest`. The new and changed cases pass.
- Engine: the whole module. The one failure is the known HEAD one, `GameMutationBoundaryRatchetTest`.
- AI: compiles (`compileJava`, `compileTestJava`).
- The full suite was not run here.

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
