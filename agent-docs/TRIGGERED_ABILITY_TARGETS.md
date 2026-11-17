# Targets of triggered abilities

How a triggered ability's targets are declared and chosen. Spell and activated-ability targets are
validated on their own path (`TargetValidationService`, `ValidTargetService`, `TargetLegalityService`):
see `EFFECTS_INDEX.md` § "Effect targeting declarations".

## When they are chosen

A triggered ability is one object on the stack (CR 603.3). Its mode is announced (CR 603.3c) and its
targets are chosen (CR 603.3d) as it is put on the stack, never as it resolves. The stack door,
`TriggeredAbilityStackService` (`service/trigger/pipeline/`), does both for every ability the pipeline
matched (`RulesEventPipeline` → `TriggerIndex` → `TriggerMatcher`), for abilities a resolution made
(`putMade`) and for delayed ones (`TemporaryGlobalTriggeredAbility`, `putDelayed`):

- when its controller must choose, the door parks the entry (`StackEntry.isParkedAtStackDoor()`) and
  the choice's handler finishes it (`finishParked`);
- an ability with no legal target, or no mode that can be chosen, is removed from the stack;
- only after that does it go to the queue its controller orders the simultaneous abilities in.

Valid candidates come from `TriggerTargetCollector` (`service/trigger/`). An ability of several target
groups is prompted group by group (`ETBTokenTargetService.processNextETBTokenMultiTargetTrigger`), which
records each group's count in `StackEntry.targetGroupSizes`, so a declined "up to" group doesn't shift
the next group's targets. The targets of one group of several targets are announced together in one
choice (CR 601.2c).

## How a card declares them

- **The effect declares what it can target** through `targetSpec()`. An effect that targets but leaves
  it at `TargetSpec.NONE` is never offered a target; `CardEffectTargetingConsistencyTest` guards the
  `Target*Effect` names.
- **The card narrows it with a target group:** `target(filter).addTrigger(Triggers.x(), effects…)`.
  The group's `TargetFilter` is the target restriction ("target creature an opponent controls",
  "target opponent").
- **A trigger filter is not a target restriction.** `Triggers.x().withFilter(…)`, the relation, and
  `TriggerOptions` decide which events trigger the ability; they never restrict what it may target.
  "Whenever a creature you control dies, target opponent loses 1 life" is a `creatureDies(YOU_CONTROL)`
  trigger plus a `target(...)` group for the opponent.
- **"That creature" is not a target.** When the event names the object an effect acts on and the card
  declares no target for that effect, the effect acts on the event's object without targeting it
  (CR 115.10a). A printed "target …" in such an ability (a block ability's "target creature") must be
  declared with `target(...)`.
- **Several targets, one ability:** one `addTrigger` holds every effect; bind each targeted effect to
  its group with `target(filter, min, max).bindEffect(effect)`. Two `addTrigger` calls are two abilities
  that are targeted separately.
- **"Up to one":** `target(filter, 0, 1)`; the controller may choose none. An effect whose
  `hasOptionalTarget()` is true may also go on the stack without its target.
- **An ability with no card to hold its group** (an emblem's, or a granted ability's "target
  opponent") names its filter itself: `new TriggeredAbility(…).withTargetFilter(filter)`. The
  constructor rejects it on a trigger whose reader doesn't honour it.
- **Saga chapters** declare targets by chapter number: `setSagaChapterTargetFilter`,
  `setSagaChapterTargetGroups`, `setSagaChapterTargetRequired`.
- **"Of defending player's choice"** combat targets: `setCombatTriggerTargetChosenByDefendingPlayer(true)`.

## Modes

A triggered "choose one —" is `ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(…))`: its mode is
announced as the ability is put on the stack (CR 603.3c), then that mode's targets are chosen. A choice
made as the ability resolves ("put a counter on it or create a Servo") is `ChooseOneAtResolutionEffect`
and targets nothing as it triggers. A mode chosen at random is picked by the door among the modes that
can be chosen.

## "You may" stays in the effects

An optional ability goes on the stack whether or not its controller means to take the option; the "you
may" is asked as it resolves (CR 603.5), so it is a `MayEffect` among the ability's effects, never a
`TriggerOptions` field. A targeting effect wrapped in `MayEffect` still has its target chosen as the
ability is put on the stack: answering the "you may" never chooses a target. `MayEffectHandler` doesn't
ask a "you may" whose wrapped effect `actsOnlyOnItsTargets()` when the entry has no target. Override
`actsOnlyOnItsTargets()` only for an effect that does nothing but act on its targets: a "you may pay
… If you do, … up to N target …" must still be asked with none chosen. A card a resolving effect has
its controller *choose* (no "target" in the text) is chosen by that effect's handler as it resolves.

## Intervening "if"

`TriggerOptions.withInterveningIf(condition)` (CR 603.4) is checked as the ability triggers, before any
target is chosen, and again as it resolves; an ability whose condition is false never asks for targets.

## Common pitfalls

- **No target offered though the filter matches:** the effect leaves `targetSpec()` at `NONE`.
- **The controller can pick themselves for "target opponent":** the group's filter is missing
  `PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.OPPONENT))`.
- **One ability's filter leaks into another's target:** declare each ability's group with
  `target(...).addTrigger(...)` or `bindEffect`. An ability whose effects are bound to no group reads
  the card's bare groups (no effect bound), never a group bound to another ability's effects.
- **A restriction on the triggering object written as a target filter** (or the reverse): the
  triggering object's restriction is the trigger's `.withFilter(…)`, the target's is the group's.
