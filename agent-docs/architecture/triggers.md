# Triggers

Triggered abilities: how cards declare them, the rules events they trigger on, the pipeline that matches the two, the
stack door that puts abilities on the stack, and what a stack entry records about its trigger event. Grep for the class
or wording you are changing and read that section.

## Abilities

### Declaring a triggered ability

- `Card.addTrigger(Triggers.x()…, effects)` (domain `model/trigger/Triggers`, named factories per family);
  `addTrigger(trigger, options, effects)` takes `TriggerOptions`; `addTrigger(List.of(t1, t2), options, effects)` is one
  ability with several trigger conditions ("attacks or blocks"); `addSimultaneousTrigger` one whose effects happen at
  once; `addTriggeredAbility` one built whole. `SpellTarget.addTrigger` binds one to a target group; a grant is
  `new TriggeredAbility(trigger, effect)`.
- One Oracle ability is one `addTrigger` of all its effects and one object on the stack (CR 603.3): Abstract Paintmage,
  Sanctum of Stone Fangs. Effects that target through different groups are bound with `SpellTarget.bindEffect`.
- A `Trigger` (sealed, one record per family) is pure data; `TriggerCondition` pairs it with the zones it functions
  from. `Trigger.watchKey()` is the trigger without the filters the matchers judge from the ability (filter, Room door,
  state), and for some the trigger of the event whose matcher reads it.
- `ObjectRelation` and the filter say which object a trigger watches: "a creature you control" includes the source,
  "another" doesn't, "this or another" always does. "This or another [type] you control" is one ability with a `THIS`
  and an `ANOTHER_…` condition. A restriction on the object is the trigger's `withFilter(…)`, never a wrapper effect.
- "You may" is no option: it stays a `MayEffect` and is asked as the ability resolves (CR 603.5); `MayEffect` and
  `MayPayManaEffect` expose the offered effect's `targetSpec`, so its targets are chosen as it goes on the stack
  (CR 603.3d).
- Code that only inspects a card's effects (cumulative upkeep, the conspiracy marker, the Oath active-player check,
  Wall of Nets, the AI) reads `Card.getTriggerEffects(trigger)`, `getTriggeredEffects()` or
  `WatchedAbilities.rawEffects`. "When this enters" effects are read with `Card.getOwnEntryEffects()` /
  `getOwnEntryEffectRegistrations()` (the intervening "if" wrapped as a `ConditionalEffect`, built once with the card's
  index so they keep their identity), never `getTriggerEffects(Triggers.enters(ObjectRelation.THIS))`.
- Not triggers: "as this is turned face up", "as this transforms into" and "as this Equipment becomes attached"
  (`AsThisBecomesAttachedEffect`, CR 614.1c) are static abilities applied at the choke point. "You may exert this as it
  attacks" is an optional attack cost (CR 701.43d, 508.1g): `ExertAsItAttacksEffect` (Combat Celebrant's condition) is
  asked off the stack as attackers are declared and exerts at once (`SkipNextUntapEffect.exert()`); the exertion emits
  `ActionEvent.Exerted` and the linked "when you do" (`Triggers.exertedAsItAttacks()`, `ActionEvent.ExertedAsItAttacks`,
  CR 607.2h), a printed ability that goes through `put`.

### Options

`TriggerOptions(interveningIf, limit, multiplicity, eventCondition)`.

- **Intervening "if"** (`withInterveningIf`): judged as it triggers and wrapped by the door in a `ConditionalEffect`
  checked again on resolution (CR 603.4). Examples: `SourceDealtDamageToTargetPlayerThisTurn` (Vessel of the
  All-Consuming, judged against the damaged player), `TargetPlayerAttackedBySubtypeControllerControlledThisTurn` (Ramses;
  `GameOutcomeService` judges against the player who lost), `TriggeringSpellMatches(StackEntryIsSingleTargetPredicate)`
  (Grip of Chaos, on the spell whose targets were chosen), `NotCondition(TriggeringPermanentHasSubtype(KREE))` (Captain
  Marvel), renown's "if it isn't renowned" and Semblance Scanner's "if it's not a token" (with the damage dealer as
  triggering permanent), evolve's `TriggeringPermanentHasGreaterPowerOrToughnessThanSource`.
- **Event condition** (`withEventCondition`): what the event itself requires ("attacks while you control a Dinosaur",
  `onlyDuringYourTurn()`, "Max speed", an unlocked door); checked only as the event occurs, never on resolution
  (CR 603.2). The Pandorica's untap ability has `SourceKeepsPermanentPhasedOut`; the player an engine-made ability names
  (the ability Takklemaggot gains) is `new TargetPlayerIs(player)`. A fact fixed by the event (the attack as declared,
  Treasure mana spent to activate, Vazi) is an `EventFactCondition`, judged once.
- **Limit** (`TriggerLimit`): `ONCE_EACH_TURN`; `DO_ONLY_ONCE_EACH_TURN` (CR 603.2h, marked only when the action is
  taken); `ONCE`; `ONCE_EACH_TURN_FOR_EACH_CREATURE` (Stensia, Stalwart Successor: by the creature the event names, the
  request's `withLimitCreature`); `FIRST_TIME_EACH_TURN` (Glyph Keeper; counted even for events before the source
  arrived). All are kept under `TriggeredAbility.limitKey()` for the source
  (`GameData.keyedOncePerTurnTriggersFiredThisTurn` / `keyedOnceOnlyTriggersFired`); "do this only once" is marked from
  `StackEntry.onceEachTurnActionKey` (`TriggeredAbilityStackService.markActionTaken`). One key covers all conditions of
  an ability (MACH-1's "when this enters and whenever you gain life"). Never add a per-permanent limit set or flag.
- **Multiplicity** (`TriggerMultiplicity`): `EACH` (per matching occurrence, CR 603.2c), `ONE_OR_MORE` (once per batch;
  "blocks" once each combat, CR 509.3a), `ONE_OR_MORE_PLAYERS` (delayed combat damage to players), `EACH_BLOCKER`
  (CR 509.3d), `EACH_SOURCE` (CR 603.2c), `EACH_BAND`. Counts are the trigger's own: `youDrawNthCardEachTurn(n)`,
  `.firstTimeEachTurn()`, `LifeEvent.LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN`.
- The `TriggeredAbility` constructor rejects options no matcher reads: a multiplicity the trigger's events don't have
  ("one or more" only on batching combat events and the tap triggers that batch), `ONCE_EACH_TURN_FOR_EACH_CREATURE`
  off damage and counter triggers, `FIRST_TIME_EACH_TURN` off zone-change, combat and becomes-the-target triggers, any
  limit but `ONCE_EACH_TURN` or any multiplicity on "when this enters", any option on a Saga chapter, everything but
  `ONCE_EACH_TURN` on a final-chapter trigger, everything but an intervening "if" on a state trigger, and a
  `targetFilter` off the triggers whose readers read one.

### Granted abilities

- A granted ability names its granter (`TriggeredAbility.grantingPermanentId()`: the Aura or Equipment of a static
  grant, set by `GrantTriggeredAbilityEffectHandler`; the resolving source of a one-shot grant,
  `GrantEffectToTargetUntilEndOfCombatEffect`). The door copies it to the entry (`setGrantingPermanentId`), a "you may"
  carries it (`PendingMayAbility.grantingPermanentId`), and effects that name it ("sacrifice this Equipment",
  "transform Optimus Prime", "other than Animal Friend", `SacrificePermanentThenEffect`,
  `BecomePreparedEffect.ofGrantingPermanent()`) read `StackEntry.requireGrantingPermanentId()`, which throws when it is
  missing.
- A granted ability with a limit is the granter's instance (`TriggeredAbility.instance()`), so each grant keeps its own
  limit (CR 113.2c). The granter is the granting permanent of a static grant, or the floating effect of a resolved one
  (`GrantTriggeredAbilityEffectHandler.asGrantedInstance`), so the same ability given twice until end of turn is two
  instances. An Aura-granted ability is the permanent's own (CR 113.8): the door stamps the granter and the
  ability's own target filter (Custody Battle). Pip-Boy 3000's own "whenever equipped creature attacks, choose one" is
  its controller's.
- On an Aura or Equipment, the `THIS` attack, block, becomes-blocked, deals-damage-to-a-player/opponent and
  becomes-the-target triggers are the attached permanent's.

## Rules events

### The log and its choke points

- `GameData.rulesEvents` (`model/rules/RulesEventLog`) records `RulesEvent`s synchronously inside the action, one
  `RulesEventBatch` per occurrence (CR 603.2c). Rules code reads them (unlike `model.event` domain events). `RulesEvent`
  is sealed; each event has a `deepCopy()` for simulation copies.
- A `ZoneChangeEvent` (card, from, to, cause, last-known permanent, controller) is emitted at three kinds of choke
  point: `PermanentRemovalService.processRemovalCleanup` for every departure (snapshot from `DepartureService`'s batch
  snapshot, CR 603.10a), `BattlefieldPlacementService.completePlacement` for every entry, and GameData's own hand,
  library, graveyard, exile, command-zone and stack lists for every other arrival, so adding a card straight to a list is
  still a zone change. The log remembers each card's last zone; a move inside one zone emits nothing; a departure a
  replacement redirects (CR 614.6) is rewritten, not doubled.
- Moving code names its reason (`setCause` / `restoreCause`: destroy, sacrifice, discard, mill) and opens a scope for a
  multi-card move (`beginBatch` / `endBatch`; every leave path in `PermanentRemovalService`, `addCardToGraveyard`, mill
  and the Aura sweeps do), so a departure's batch closes only after its card arrived. Refilling zone lists without moving
  cards (simulation copies, turn-restart restore) runs between `suppress()` and `resume()`.
- Every other event is emitted with `RulesEventLog.occur`, as an occurrence of its own, where the action completes and
  after the bookkeeping its matcher reads. `RulesEventLog.setCastingSpell` names the spell whose costs (or a mana ability
  activated with a cast `ManaPaymentIntent`) are being paid, as `ZoneChangeEvent.castingSpell`
  (`ZoneChangeTrigger.whileCasting(CardPredicate)`, emerge).
- A departure carries what look-back abilities read (CR 603.10a): the last-known permanent (with the permanents that
  damaged it this turn), the zone-change abilities others granted it, its attachments, the exiled cards that haunted
  it, Hushbringer's suppression and the spell whose cost it paid. Simultaneous death batches snapshot granted
  zone-change abilities before moving any permanent, so a paired or co-dying granter's abilities survive; the snapshot is
  cleared when the event ends.

### Families

| Event | Emitted by | Matched by |
|---|---|---|
| `ZoneChangeEvent`, `PermanentChangeEvent` | the choke points; phase, transform, turn face up | `RulesEventPipeline` |
| `StepEvent` | `StepTriggerService.beginStep` | `RulesEventPipeline.triggerStep` |
| `AttackEvent` / `BlockEvent` | `CombatAttackService.finishAttackerDeclaration` / `CombatBlockService` | `AttackTriggers` / `BlockTriggers` |
| `DamageDealtEvent` / `CombatDamageStepEvent` (`DEALT`, `AFTER_STATE_BASED_ACTIONS`) | damage paths / combat damage step | `DamageTriggers` / `CombatDamageTriggers` |
| `SpellEvent` (cast, copied, countered, card advantage, land played, activated, mana ability resolved) | `SpellEvents.cast` / `activated`, resolution and counter paths | `SpellTriggers` |
| `TargetEvent` | `TargetTriggers.checkBecomesTargetOf{Spell,Ability}Triggers`, `checkTargetChoiceTriggers` | `TargetTriggers` |
| `CounterChangeEvent` | `PermanentCounterSupport`, keyword handlers | `CounterTriggers` |
| `TapEvent` | `TapUntapSupport`, tap costs, mana paths, untap step | `TapTriggers` |
| `LifeEvent` | `LifeSupport` | `LifeTriggers` |
| `LibraryEvent` | `DrawService.completeDrawCard`, `GameData.discarded` / `finishDiscardEvent` / `surveilled`, `LibrarySearchTriggerHelper`, `LibraryShuffleHelper` | `LibraryTriggers` |
| `ActionEvent` | `ActionEvents` | `ActionTriggers` |
| `ControlChangeEvent`, `AttachmentEvent`, `CrewEvent`, `DayNightEvent`, `RoomEvent` | `CreatureControlService` and where they happen | the matching `…Triggers` |
| `SagaEvent` | `SagaChapterService` (final chapter triggers), `StackResolutionService` (it resolves) | `SagaTriggers` |
| `EntryCausedTriggerEvent` | `EtbTriggerService` | `EntryCausedTriggerTriggers` |
| `TurnEvent` (turn began, step or phase ended, cleanup's damage removal) | `TurnProgressionService`, `TurnSupport` (an effect ends combat), `TurnCleanupService.resetEndOfTurnModifiers` | no ability; `DurationClock` |

Saga chapters and state triggers are not rules events.

`RulesEventPipeline.process` first hands each batch to `DurationClock` (`service/duration`), which ends the floating
effects whose `Duration.End` the batch's events are (CR 611.2a; turn, step, cleanup, untap-step, cast and planeswalk
events), so an ability the same event triggers sees them ended.

## The pipeline

Each batch goes, as it closes, to `RulesEventPipeline.process` → `TriggerIndex` candidates → `TriggerMatcher` / the
family matcher → `TriggeredAbilityStackService` (package `service/trigger/pipeline`). `GameSetupService` registers the
pipeline on every game and simulation copies carry it; `beginTriggerOrder` only drains closed batches. Read-only
families (attack, block, spell, control change, crew, day/night, Room, Saga, entry-caused) detect inside one query
scope per event (`inScope`); families that mutate as they detect don't.

Every matcher reads an ability whole, as the layers leave it, judges the trigger's filter, multiplicity and event
condition, records its event whatever the effects are, and hands it to the door as one object (CR 603.3). No matcher
judges, records or routes by an effect's class, and none reads, wraps or rebuilds `effects()`.

### Candidates: `TriggerIndex`

- `permanentAbilities`: a permanent's (or face-up scheme view's) abilities as the layers leave them (CR 613.1f): its
  card's and printed echo's (none face down, CR 708.2, or when lost), read through text changes (spell filters by
  `TextChangeTransformer.transformSpellFilter`, CR 612.1); the granted ones the layered state keeps, resolved grants
  (floating `GrantTriggeredAbilityEffect`s of any duration) and others' static grants alike, in timestamp order against
  a "loses all abilities" (CR 613.7; granted cumulative upkeep too); one per keyword-trigger instance. No reader
  assembles printed and granted abilities by hand or re-checks face down, cloaked or lost abilities.
- `graveyardAbilities` (own and perpetually gained, none while an effect like Yixlid Jailer's applies), `cardAbilities`
  (exile, command zone), `delayedAbilities` / `delayedAbilitiesAbout`.
- `observerCandidates(event, batch)`: who sees a permanent leave, as just before the event (CR 603.10a): the
  battlefield, the permanents that left in the same batch (departure snapshots), graveyard and face-up exiled cards
  already there (haunting cards among them). For other moves, as after it, an entering permanent included (CR 603.6a).
  Face-up planes, emblems and schemes are listed for every move, functioning from the command zone (CR 901.7, 114.4,
  314.4): a plane's under the planar controller (its record stamps `sourcePlanarObject`), an emblem's under its
  controller (request `ofEmblem`, `madeByResolution`), a scheme's as its permanent view.
- `movingObjectCandidates`: the moved object's own abilities, both as the departed permanent last existed (on the
  snapshot) and as the card in its new zone (CR 400.7); the candidate's zone says which.
- `permanentChangeCandidates`: battlefield (the changed permanent included) and graveyard cards after the change; a
  phased-out permanent isn't there, but its own abilities look back (CR 603.10b).
- `stepCandidates`: delayed step abilities whose wait is over, planar objects (planar controller, CR 901.6), emblems
  (CR 114.4), then per player in APNAP order their permanents and schemes, then command-zone, graveyard and exile cards
  whose abilities function there (CR 113.6; an exiled "each upkeep" only while suspended), and at upkeep the active
  player's refine and suspend counter abilities (`StepCandidate.ExileCounterAbility`, below the exile abilities).
- `entryCausesNoTriggers`: an entry under Hushbringer's kind of ability triggers nothing, "leaves your graveyard"
  included.

### Watchers

Life, tap, draw, cast and attacks-you matchers read `Watcher`s (a permanent under its controller, an emblem under its
owner, CR 114.4, or a delayed ability): `EventAbilitySupport.watchers` (permanents, emblems, then delayed abilities about
no object), `delayedWatching(permanentId)`, `abilities(gameData, watcher, watched)` and `put(gameData, ability, watcher,
…)` (an emblem's as `ofEmblem` with `madeByResolution`). `EventAbilitySupport.abilities(permanent, watched)` is
`permanentAbilities` by watch key; `ownAbilities` / `grantedAbilities` split them where a card's own abilities depend on
where it is (an Aura's "enchanted creature becomes the target", printed ward on an attached permanent, an unattached
animated Equipment). The spell matcher reads emblems by the same keys (`dispatchEmblems`); the combat-damage ally pass
reads the controller's emblems as watchers. No emblem ability has its own reader.

### Zone changes

- `TriggerMatcher.matchedTrigger`: the first condition the event meets (move, cause, casting spell, zone the condition
  functions from, CR 113.6, or the command zone for planes and emblems, CR 901.7, 114.4; relation). A `THIS` filter is
  judged on the last-known copy ("when this dies, if it was a creature"), any other on the moving object; other "if"s
  and limits are `TriggerOptions`, never wrappers.
- Order: observers first (each-event ones in event order, then "one or more"), then the moving objects' own, each
  event's in `OwnZoneChange` order (leaves, arrives, dies, sacrificed); controllers order them later (CR 603.3b).
- "From anywhere" (also `ZoneChangeTrigger.notFrom`) is never leaves-the-battlefield (CR 603.6c); text naming the
  battlefield among the zones left ("from graveyards and/or the battlefield") is, so write the zones as printed. Only
  leaves-the-battlefield, sacrifice and moves to a hand or library look back (CR 603.10a,
  `looksBackOnAPermanentLeaving`); otherwise the filter is judged on the card in its new zone, and an observer that left
  in the same event doesn't see it (Planar Void's and Bloodchief Ascension's rulings).
- Own zone change ("when this dies / leaves / is put into a graveyard from anywhere", "when you sacrifice this"):
  `TriggeredAbilityStackService.trigger` builds `ownZoneChangeRecord` (snapshot, triggering permanent with attachments,
  last-known power as event value, mana value, how it was cast, the card's new incarnation, CR 400.7, the card its linked
  ability exiled, CR 607.2a) and calls `put`. A relation other than `THIS`, or a move out of a graveyard, goes to
  `triggerOnce`.
- Another object's move: `triggerOnce` + `ZoneChangeRequests.zoneChangeRequest`, read from the matched trigger: batch
  count or total power for "one or more" dies, the enchanted permanent for `ENCHANTED`, your commander, "first time each
  turn" as tokens created (`CREATE`) or land cards put into a graveyard (anything else throws), and "that player" by
  relation: controller for `OPPONENT_CONTROLS` / `ENCHANTED_PLAYER_CONTROLS`, owner for `OPPONENT_OWNS` and an `ANY` /
  `ANOTHER` return to hand, caster (else controller) for an `ANY` entry, sacrificer for an `ANY` / `ANOTHER` sacrifice.
  Any other "that player" is declared on the effect (Liability's `Subjects.ownerOf(Subjects.triggeringCard())`) or as a
  target (Lucy MacLean).
- `ZoneChangeRequests.zoneChangeRecord`: "that card" with its graveyard and exile incarnations, the permanent as it
  last existed (controller, power, toughness, mana value), the source snapshot, a non-targeting "that player"; an entry
  with the entering permanent's controller, power, toughness and attacked player; a batch's objects and count or total
  power. A departed permanent is triggering permanent with its owner; its exile stay is recorded only when in exile.
  Which cards ("a Desert card", "from your hand or library") is the trigger's filter and zones.
- "One or more tokens enter" triggers once per batch, "a token enters" once per token: the card writes which
  (CR 603.2c); no resolution condition splits a batch. Token watches (`CREATE`, `Triggers.tokensCreated`, token filters)
  see a creation as one event when its batch closes; every other "whenever [an object] enters" is matched as the entering
  permanent's own enters abilities are processed (`RulesEventPipeline.matchedAsItsEntryIsProcessed`).
- Planechase: the planeswalk, encounter and chaos readers (`PlanechaseService.trigger`) judge each event condition and
  hand the ability to the door with `sourcePlanarObject`; a choice queued for a planar ability holds its record
  (`GameData.queuedTriggerRecord`) so `PlanechaseService.checkPhenomena` doesn't leave the phenomenon early.

### Permanent changes

Phasing, transforming and turning face up are `PermanentChangeEvent`s (not zone changes, CR 702.26d), matched by
`TriggerMatcher.matchedPermanentChange` against `PermanentChangeTrigger`s ("one or more" once per batch, then each
event's), judged as after the change except a phased-out permanent, whose own abilities look back (CR 603.10b). They go
through `triggerPermanentChange` / `permanentChangeRequest` and `put`; "that permanent" unless `THIS` (Ravager of the
Fells declares its groups with `CONTROLLED_BY_FIRST_TARGET`).

### Entering the battlefield

- Own "when this enters" (`ZoneChangeTrigger.ownEntry()`) is not matched by the pipeline:
  `EtbTriggerService.processCreatureETBEffects` → `putOwnEntryAbilities` puts, through `put` with `TriggerCause.Entered`,
  the abilities granted to the permanent, its perpetual grants, static grants
  (`GrantedTriggeredAbilitySupport.grantedOwnEntryAbilities`) and printed ones (`Card.getOwnEntryAbilities()`, none if
  it lost them as it entered), before the abilities that see it enter. `ownEntryRecord` holds the paid X and costs, the
  permanent and what it is attached to, and an Aura's enchanted object. Event conditions are judged with the entry facts
  (`EtbEffectResolver.ownEntryConditionMet`, CR 603.2); an effect's "if" about such a fact (kicked, tribute, evoke's
  sacrifice, CR 702.74a) is judged as it triggers through `CardEffect.asItTriggersOnEntering(EntryFacts)` (CR 603.4).
  Several enter abilities besides the evoke sacrifice (`Card.hasSeveralOwnEntryAbilities`) are separate objects the
  controller orders (CR 603.3b). A resolved permanent spell's enter abilities choose targets as they go on the stack
  (CR 603.3d); only an Aura's enchant target carries over.
- Others' enters abilities: `EtbTriggerService.collectEntersTriggers` → `RulesEventPipeline.triggerEnteringObservers`
  checks every permanent on the battlefield then, newcomers and the entering permanent included (CR 603.6a);
  `matchedTrigger` returns the source's own condition for its own entry, which is skipped. The filter's types,
  subtypes, colors and keywords are judged on a copy with the characteristics it has then, anything else (power,
  counters, origin, "other than this") on the permanent with the watcher as source ("this creature or another Ally" is
  `THIS_OR_ANOTHER_YOU_CONTROL`).
- Mass entries are processed after all permanents are on the battlefield with their entry choices made, so they see
  one another. While `GameData.collectingSimultaneousCreatureEntryTriggers` marks the event
  (`BattlefieldEntryBatchSupport.placeCards`, graveyard return-all paths, Living Death, one token creation, …) a "one or
  more … enter" ability that already triggered names the next permanent too (`GameData.simultaneousCreatureEntryTriggers`,
  keyed by the whole ability). A new mass-entry path that skips the flag triggers it once per entrant.
- A delayed "one or more … enter" ability (a one-time boon) is matched as the entry's batch closes, once, naming every
  permanent of the event (`matchedAsItsEntryIsProcessed`, CR 603.2c), since it may be used up as it triggers.
- Then `EtbTriggerService` runs what the pipeline doesn't match (Cosima's exiled voyage) and emits an `EntryCausedTriggerEvent` per ability the entry caused (Strict Proctor).
- "Put counters on it", "it gets +X/+Y", "transform it", "attach this to it", "become a copy of that creature", "deals
  N damage to it" (`DealDamageToTargetCreatureEffect(Subjects.triggering(), n)`), graft, Agent's Toolkit, soulbond, "it connives", endure,
  Madame Null, Mana
  Echoes, Eldrazi Mimic and the graveyard Auras and Equipment that return attached to "that creature" read the entry's
  triggering permanent (`onTriggeringPermanent`, `Subjects.triggering()`, `Subjects.triggering()`,
  `ofTriggeringPermanent()`); the entering creature is recorded (Archon of Redemption, Hamletback Goliath, Cream of the
  Crop), never bound in.
- Additional enter abilities on token copies are separate abilities with their own targets (CR 603.3).

### Steps and phases

- `StepTrigger` (`Triggers.upkeep`, `drawStep`, `precombatMainPhase`, `beginningOfCombat`, `endOfCombat`,
  `postcombatMainPhase`, `secondMainPhase`, `endStep`, `cleanup()` (CR 514.3a); `ActivePlayer` `YOU`, `ANY`,
  `OPPONENT`, `ENCHANTED_PLAYER`, `ENCHANTED_PERMANENT_CONTROLLER`; CR 603.2b).
- `StepTriggerService.beginStep` emits a `StepEvent` (moment, active player; the second main phase is its own) as an
  occurrence (CR 603.2b, 603.2c), matched at once. `TriggerMatcher.matchedStep` relates the active player to the
  ability; it triggers once however many conditions the step meets. `RulesEventPipeline.triggerStep` orders matches by
  `StepMatch.StepGroup` (`STEP_ORDER`; precombat main puts a permanent's own abilities last-first, CR 603.3b), judges
  event conditions (no multiplicity or limit but "only once") and puts each with `stepRecord`: the active player; a
  permanent source as it was and its attachment (`attachedPermanentSnapshot`, CR 608.2h); a graveyard source's stay and
  triggering card (CR 400.7); "that player" (whose step it is; the enchanted player for a Curse) as triggering player
  and non-targeting `targetId` (CR 115.10a), also read by the event condition (Razor Pendulum). An Aura acting on the
  enchanted permanent's controller names permanent and controller; "at end of combat" names nothing. An undeclared
  player target is that player (Pillar Tombs of Aku). Curse of Surveillance (targets chosen as it triggers) reads the
  triggering player, Curse of Oblivion (non-targeting) its `targetId`.
- Each step handler ends with `beginNextStepTriggerChoice` (the door's queued choices begin before priority); a test
  that triggers by calling a service directly uses `GameTestHarness.beginTriggerChoices()`.
- Engine-made step abilities (echo, initiative, rad, opening-hand reveals, suspend delay counters) are put by
  `StepTriggerService` around the event.

### Combat

- `CombatTrigger` (attacks, attacks you, attacks unblocked, blocks, becomes blocked, combat damage to a player,
  creature, planeswalker, battle, you, …; relation; filter), CR 508.1, 509.1, 510.2.
- `AttackEvent` (attackers in order, what each attacks, enlisted creatures) follows the keyword attack abilities
  (decayed, melee, battle cry, dethrone, training, double team); `BlockEvent` (`Kind`) precedes the flanking and delayed
  block abilities (CR 508.3a, 509.3). Candidates: `permanentAbilities`, `graveyardAbilities`, `cardAbilities`, planes,
  emblems, delayed abilities, matched by `TriggerMatcher.combatCondition`. The matcher judges the filter (the blocked
  creature, any of them with `ONE_OR_MORE`; each blocker with `EACH_BLOCKER`, any one without; CR 509.3f), multiplicity
  and event condition. "Attacks for the first time each turn" is `ONCE_EACH_TURN` by the attack count.
- Attack record: attacked target, defending player, attacker count as X, declared attackers' snapshots, "that creature"
  / "that player" non-targeting. "Whenever you attack" records its attackers (the band's for `EACH_BAND`; "they gain …"
  is `Subjects.triggeringAttackers()`); a filtered `youAttack()` records the matching ones as triggering permanents and
  attacking snapshots (`TotalPowerOfControlledCreatures.declaredAttackers`,
  `BoostAllOwnCreaturesEffect.forDeclaredAttackers`, `Subjects.target()`).
- Block record (`BlockTriggers`): the creature on the other side as triggering permanent (id, controller, last-known
  copy) and non-targeting `targetId`, with its power and toughness as it triggered; an Aura or Equipment keeps the
  attached creature as triggering permanent and the other as `targetId`. A declared target (Elite Javelineer) is
  chosen; an undeclared one is that creature (CR 115.10a). A "[filter] creature (you control) becomes blocked" watcher is
  the source (CR 113.7) and records the blocked creature; its `EACH_BLOCKER` form (No Quarter, Righteous Indignation)
  triggers once per declared pair, the blocker its target and the comparison its event condition.
- For an ability its source's own attack caused (`causedBySourceAttacking()`, CR 508.3a) the door calls
  `AttackTriggers.triggerAttackingSourceAbility`: "a creature you control attacking causes a triggered ability of that
  creature to trigger" abilities (Firebender Ascension) go on the stack above it, the attacker as triggering permanent
  and that ability as triggering spell.
- Enchanted-creature unblocked readers have the Aura as source (CR 113.7) and the attacker as triggering permanent
  (Cloak of Confusion, Farrel's Mantle: `AssignNoCombatDamageEffect.ofTriggeringPermanent`,
  `DealDamageToTargetCreatureEffect.byTriggeringPermanent`, CR 608.2h).
- `CharacteristicState.meleeInstances` keeps several melee abilities, each counting players (not planeswalkers or
  battles) attacked.

### Damage

- `DamageTrigger` (deals damage, to whom, noncombat, an instant's or sorcery's; is dealt damage, noncombat or excess
  (CR 120.10); you or a player is dealt damage; damage to you is prevented (CR 615.1); CR 120.1). Filters: `withFilter`
  is the damaged permanent for "is dealt damage" and "this deals damage", else the dealer; `withDamagedFilter` the
  creature dealt damage; `withSpellFilter` a spell or card source by its own characteristics (not Ghostly Flame's
  colors); `withCombatDamageOnly()`.
- `DamageTriggers` / `CombatDamageTriggers` read printed, granted, graveyard, planar and delayed abilities
  (`TriggerMatcher.damageCondition`), judge the filter with the watcher as source, the limit (per creature for Stensia)
  and `EACH_SOURCE` by splitting per source. A source or watcher that may have left (a combat-damage dealer, a departed
  ally watcher, an Equipment) is read as it last existed. 0 damage isn't dealt and triggers nothing (CR 120.8).
- The combat damage step takes `EventAbilitySupport.snapshot` as damage is dealt and puts what it triggered after
  state-based actions inside `asOf` (CR 603.2, 510.2, 704.3), so a dealer or watcher that died or changed is read as
  then (the scope is keyed to the `GameData` and on the thread only for the call). Those abilities wait in
  `CombatDamageState` (abilities, not effects, as in `PendingSourceDamage`), stay on the stack through state-based
  actions so a named creature that dies is stamped with its last-known information (CR 608.2h), and are moved above what
  those actions triggered. "Is dealt damage" abilities are read as damage is dealt and put after deaths through
  `DamageTriggers.collectDealtDamageAbilities` (`DamageToCreature.damagedLastKnown`), so limits, "if"s and additional
  triggers (Roaming Throne) apply alike. A "one or more creatures you control deal combat damage" mark is keyed by the
  ability's place among its source's abilities.
- Records. To a player (`DamageTriggers.playerDamageRecord`, every damage-to-a-player reader): the player as triggering
  player and, untargeted, non-targeting `targetId`; damage as event value (a choice carries it,
  `AttackTriggerTarget.eventValue`); source and controller; an attachment's dealer as triggering permanent. For "a
  source an opponent controls deals damage to you", "that player" is that opponent. To a creature in combat: the excess
  ("that excess damage", CR 120.4a), against which the "if" is judged. "This is dealt damage"
  (`collectDealtDamageAbilities`): damage as event value and X, source as triggering permanent with its controller then,
  the damaged permanent's last-known information with damage keywords as source snapshot. "A creature / permanent you
  control is dealt damage" (`damagedPermanentRecord`): the damaged permanent as triggering permanent. Event conditions
  see the source as triggering permanent and its controller as "that player" (Archfiend of Spite:
  `NotCondition(TargetPlayerIsController())`).
- `dealsDamage().withFilter(…)` triggers once if any recipient (snapshots before state-based actions) matches, even if
  source or recipient dies. The deals-damage-to-a-creature family (`collectDealsDamageToCreatureAbilities`) records the
  damaged creature and the damage; source-deals-damage triggers record the source and its controller as it dealt the
  damage; `sourceDealsDamageToYou()` the damage to you; the only-target-creature trigger that creature;
  `creatureDealsCombatDamageToYou()` the creature as non-targeting `targetId` and triggering permanent;
  `youAreDealtDamageBySourceYouControl()` triggers only for a source the damaged player controls.
- `sourceDealsDamageToPermanentOrPlayer()` triggers once per source and recipient: the recipient is the non-targeting
  `targetId` (and triggering player or permanent), its damage the event value read by event conditions (Ghyrson Starn's
  "exactly 1 damage"; its "that permanent or player" is dealt damage by `DealDamageToAnyTargetEffect.toEventObject`). A
  declared "any target other than that permanent" (Toralf) is `PermanentIsTriggeringPermanentPredicate`.
- `playerDealtDamage()` (Sower of Discord) triggers once per player and damage event, sources summed:
  `CombatDamageService` per combat damage step before state-based actions, `DamageSupport.flushSourceDamageReflections`
  over the `PendingSourceDamage` batches sharing a resolution damage event (`getDamageEvent()`,
  `GameData.openDamageEvent`).
- "That creature deals that much damage" (CR 608.2h) is the entry's event damage source
  (`StackEntry.setEventDamageSource`, keywords recorded as it leaves), dealt through
  `DealDamageToPlayersEffect.dealtByEventDamageSource()` / `DamageSupport.entryDealtByEventDamageSource`, so its
  lifelink, infect and colors apply (Greatbow Doyen, Saskia, Kediss). Delayed damage triggers keep the watched
  permanent as damage source, its controller in `StackEntry.damageSourceControllerId`.
- "When that creature deals combat damage to a player this turn" is a registered delayed ability (Optimus Prime,
  Autobot Leader). A target declared as chosen at random (`CardEffect.targetChosenAtRandom()`, Power Pack) is picked as
  the ability goes on the stack.

### Spells, activations and land plays

- `SpellTrigger` (CR 601.2i), `LandPlayTrigger` (CR 116.2a), `AbilityTrigger`. `SpellTriggers` candidates: permanents
  and schemes as the layers leave them (CR 612.1), planes, delayed abilities, emblems, suspended cards, and the
  caster's graveyard, hand, exile and command-zone cards. Cast and copy abilities are judged by their `SpellFilter`
  (`collectCastAbility` / `collectCopyAbility`; unfiltered "casts a spell" is `SpellFilter.ANY` via
  `collectedSpellFilter`), land plays by their trigger (`collectLandPlayAbility`: the player as "that player", the land
  as triggering card), others by event condition (`collectUnfilteredAbility`). A missing target filter comes from the
  bound group. Records: `spellWatchRecord`, the cast record, `activationRecord`.
- Cast filter: `withFilter`, `withStackEntryFilter`, `duringYourTurn`, `nthThisTurn`, `firstThisGame` (the caster's
  first spell of the game, `SpellEvent.Cast.firstSpellOfGame`), `.castFrom` / `.notCastFrom`
  (counts then count that zone's spells), `.kicked()` / `.eachKick()` (kick count as event value),
  `.countingEveryPlayersSpells()`, `paidWithTreasureMana`. A cast ability's event condition is checked only at the cast
  (CR 603.2).
- `SpellEvents.cast` (CR 601.2i) records the cast for the turn, moves what the payment leaves (conspire and casualty
  paid, spell-copying mana, the game's first spell) into the `Cast` event, emits it and clears the payment records;
  `SpellEvents.activated` likewise (`Activated.manaLinkedCopies`).
- A cast puts, in order: the matched abilities, then the copies the payment made (Primal Wellspring, Pyromancer's
  Goggles, conspire, casualty, Sunken Palace) and a granted replicate or demonstrate; controllers order them
  (CR 603.3b).
- "When you cast this spell" (`putSelfCastAbility`): own, perpetual and granted replicate/demonstrate abilities, each
  one object (CR 603.3), the spell (X, kick, zone) as triggering spell; "you may" asked on resolution (CR 603.5).
- `.grantedToTheSpell()` makes the ability the spell's: each effect its own object sourced by the spell, never doubled
  (Maelstrom Nexus, The First Sliver, Abaddon the Despoiler, Rain of Riches, The Twelfth Doctor; `nthThisTurn` counts
  only spells its zone and Treasure filters admit). "The next … spell you cast this turn has cascade" is a `nextThisTurn`
  delayed ability with such a trigger, used up by that spell.
- Activations: `SpellEvent.Activated` once per activation, when the ability is on the stack and paid (CR 602.2b,
  CR 601.2i; mana abilities' entries wait in `pendingManaAbilityTriggers`). `activationAbilities` /
  `activationTriggers` judge event, player, `kind` (`AbilityKind.LOYALTY`, `AbilityKind.matches`), `source` (from the
  watcher's view, on the permanent as it last existed; none for a card in hand, graveyard, exile or command zone,
  CR 109.2; an Aura or Equipment that left paying costs is attached to nothing), `withStackEntryFilter`, `withPayment`.
  `activationRecord`: the ability's entry as triggering spell, `triggeringActivatedAbility`, the permanent and
  controller as triggering permanent, life paid as event value, the activator as "that player". Nothing is collected
  before the costs are paid; Circling Vultures' discard is a special action (CR 116.2e). Delayed activation abilities:
  `collectDelayedActivationTriggers`.
- Card advantage (Toofer, Keeper of the Full Grip): `StackEntry.cardCountsBeforeResolution`, judged by
  `StackResolutionService` as the spell finishes.
- Trial and Error's "this spell is countered or fizzles" has the spell as it last existed as triggering spell. A copy
  trigger's "that spell" is the copy (Parnesse, the Subtle Brush's `CopyCopiedSpellForTargetPlayerEffect`), the copier
  "that player", the copy's mana value the event value. Simultaneous cast triggers keep their spell identity;
  separate next-spell-copy effects each make their own entry.

### Becoming the target

- `TargetTrigger` (CR 601.2c, 602.2b; ward, CR 702.21a). `TargetTriggers` reads abilities through
  `EventAbilitySupport.abilities` and static grants (`grantedWatchedAbilities`), judges the filter on the targeted
  permanent, `byController()` and the limit, and puts via `putTargetAbility`; `recordTargetingObject` names the
  targeting spell or ability as triggering spell (live, or as it last existed, CR 608.2h, `CopySupport.triggeringSpell`)
  and its controller as triggering player, plus "that creature" / "that player".
- "You or a permanent you control becomes the target" triggers per target; "you and/or at least one permanent" is
  `TriggerOptions.ONE_OR_MORE` on it (Leyline of Combustion), the only target trigger allowing it.
- Ward is any "of a spell or ability an opponent controls" ability on this permanent, whatever its effect; granted ones
  are read too. The door aims an effect acting only on a spell or ability at it, untargeted, when the record names no
  object but a player; ward is found by that rule, not by a counter class.

### Counters, tapping and life

- `CounterChangeEvent` (permanents, players, exiled cards; proliferate, renown, monstrosity, training, evolve, Class,
  {E}; CR 122.1, counters it enters with too, CR 122.6). `timeCounterRemovedFromThisInExile()` functions from exile;
  `youRemoveLastCounterFromThis(kind)` is read via `PermanentCounterSupport.notifyLastCounterRemoved`. `CounterTriggers`
  reads printed, granted (Danny Pink), graveyard and exiled abilities and judges the `qualifier`, `byController()`,
  `eachCounter()` and the limit. A placement entry (`eventRecord`,
  `countersPutRecord`) records the creature as triggering permanent, the placer as triggering player,
  `StackEntry.eventCounterType` and the count as event value (`PutSameCountersOnSourceEffect`).
- `TapEvent` (CR 701.26a, 701.26b, 603.2e; tapping for mana, CR 106.12). `becomesTapped()` and
  `BECOMES_TAPPED_FOR_FIRST_TIME_DURING_YOUR_TURN` are read by the "a permanent you control becomes tapped" matcher
  (`TapTrigger.narrowed()`), "one or more" by the open batch (`beginPermanentTapTriggerBatch`). `TapTriggers` reads
  printed, granted (Haunted One, Hangar Scrounger, Well Rested), delayed (Don't Move) and planar abilities. Record:
  tapping player,
  tapped permanent with snapshot (Haunted One) and controller (`TRIGGERING_PERMANENT_CONTROLLER`), produced colors,
  source snapshot. Who taps which permanent is the trigger (`TapTrigger.judgedTapRelation()`); "its controller adds" is
  the tapped permanent's controller (`TappedLandManaSupport.tappedPermanentController`, Piracy). Relic Bind's mode is
  announced from the stack by `StackedModalTriggerService`; a player-only mode declares a `PlayerPredicateTargetFilter`
  so it isn't read as "that player".
- Triggered mana abilities (`TapTriggers.isTriggeredManaEffect`, CR 605.1b) resolve at once (`putManaAbility`,
  CR 605.4a); one that `readsProducedMana()` waits for the mana choice (`holdForProducedMana`,
  `GameData.pendingManaAbilityTriggers`, `awaitingProducedManaColor`), then resolves or goes through `putHeld`
  (`putResolvedProducedManaTrigger`); a targeted one (C.A.M.P.) carries its filter and colors
  (`SpellTargetTriggerAnyTarget.withProducedManaColors`).
- An ability that triggers while a mana ability is activated or resolves (`manaAbilityActivationDepth`, set around
  `ActivatedAbilityExecutionService.completeActivationAfterCosts` of a mana ability, the direct tap path and Piracy's
  foreign tap; `manaAbilityResolutionDepth`) is held unchosen in `pendingManaAbilityTriggers`
  (`StackEntry.heldForManaAbility`): Sanguine Bond on Pristine Talisman's life gain, Disciple of the Vault on an
  artifact sacrificed for mana. No prompt opens while mana is paid for a spell (CR 601.2g, 605.3a). Every flush site
  hands the queue to `GameData.putManaAbilityTriggersOnStack`: a held entry goes through the game's held-trigger door
  (`RulesEventPipeline.putHeld` → `putHeld`, set at setup), which chooses its mode and targets as it goes on the
  stack (CR 603.3, 603.3c, 603.3d); any other goes on as it is. The undo window (`revertableManaActivations`) tracks
  an activation's held entries by the queue's growth.
- `LifeEvent` (CR 119.3, 119.4, 119.9); paying is a loss too (CR 119.4) and its own event; `LifeSupport` keeps life lost
  this turn and speed's inherent ability (CR 702.179d). `LifeTriggers` reads printed, granted (Field-Tested Frying Pan,
  Light of Promise, Sunbond), graveyard (Deathless Knight, Punishing Fire) and delayed (False Cure) abilities and
  judges `firstTimeEachTurn()`, `LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN` (Valgavoth: one object) and the limit; record:
  "that player", amount as event value. Life restrictions use the ability's controller even if its source changed
  control or left.

### Library, draw and discard

- `LibraryEvent` (CR 121.1, 701.9a, 701.22a, 701.25a): draw, revealed drawn card, discard, discard event, cycle, scry,
  surveil, search, shuffle. `LibraryTriggers` reads abilities via `EventAbilitySupport` and judges the draw count this
  turn (CR 121.1), the discard filter (`withFilter`; one card of a discard event is enough), cycling or opponent-caused
  discards, the turn's first cycling, nonland discard, discard event or surveil, `nth` and the limit (CR 603.2h).
  Record: the acting player as
  triggering player (an opponent also non-targeting "that player"), discarded cards with their stays
  (`eventCardGraveyardEntryVersions`; a lone card also triggering card: Gutmorn, Pactbound Servant), the count as event
  value. Draw requests carry `TriggerCause.Drew`. Cycling has the discard record (CR 702.29a), carried by
  `DiscardControllerTriggerTarget.withDiscard`.
- "This card is cycled or discarded" functions where the card ends up and is read as it has it there (CR 702.29c):
  none from a graveyard under Yixlid Jailer (its ruling on Krosan Tusker). A cycling ability with announced targets
  (Rampaging War Mammoth) goes through `putWithAnnouncedTargets`.
- `GameData.finishDiscardEvent` then checks state triggers (CR 603.8). A shuffle ability goes on the stack unlogged (a
  shuffle can happen outside a mutation scope); its cause is captured at emission.

### Actions and the other families

- `PlayerActionTrigger` (crime, CR 700.13, and other player actions) and `ObjectActionTrigger` (exploits, CR 702.110b;
  becomes plotted, functioning from exile, CR 702.170a; crew, saddle, station, door unlocks). `ActionEvents` keeps the
  bookkeeping first (first investigation, a scheme set in motion N times as N events, CR 701.32c, a player's
  simultaneous sacrifices as one `ActionEvent.Sacrificed`, "whenever you sacrifice one or more permanents").
  `ActionTriggers` reads abilities as the layers leave them (none face down or ability-less, CR 708.2, 613.1f) in this
  order: the actor's "you", schemes, opponents', everyone's, delayed abilities that watch no object, then graveyard
  abilities; it judges explore/exploit/scheme filters, a Ring-bearer other than the source (Gandalf,
  Friend of the Shire), a tax paid to the taxing player's permanents, and reads a departed source as printed (an
  exploiter that sacrificed itself, a mutated pile).
- Control change, attachment (Siona sees Cloudform as an Aura), crew/saddle/station (cost-payment abilities held by
  `CrewTriggers.holdForActivation` in `GameData.pendingActivatedAbilityCostTriggers` until the ability is on the stack,
  CR 603.3; "becomes crewed for the first time each turn" is the unkeyed once-each-turn mark), day/night (CR 732.1a),
  Rooms, entry-caused triggers (Strict Proctor) and `PlanarTrigger` (command zone, CR 901.7) are matched by their
  `…Triggers` class.
- Saga chapters: `ChapterTrigger` (`Triggers.chapter(n)`, CR 715.2b), no options. `Card.addTrigger` joins one chapter's
  effects into one ability holding its targets (`setSagaChapterTargetFilter` / `Groups` / `Required`);
  `getSagaFinalChapter()` is the greatest (CR 715.2d). A Saga on the battlefield has the chapter abilities the layers
  leave it (`GameQueryService.sagaChapterAbilities` / `sagaFinalChapterNumber`: none once it lost its abilities), so
  one without any gets no lore counter (CR 715.3c), triggers nothing and isn't sacrificed (CR 704.5s). A Saga entering
  while an effect makes it lose all abilities (`losesAllAbilitiesByEffects`; not a CR 305.7 land type, which keeps
  intrinsic abilities) has no intrinsic "enters with a lore counter" ability (CR 715.3a, 614.12).
  `SagaChapterService.triggerSagaChapter` and `StepTriggerService.handleSagaLoreCounters` read chapters; `putChapterAbility` calls `put`, one choosing targets
  (`chapterTargets()`, its effects, a random mode's filter) parked with the chapter's choice. `FinalChapterTrigger`
  (CR 715.2e) is matched by `SagaTriggers` (Saga's controller; its mana value as event value).
- State triggers (`StateTrigger`, CR 603.8): `StateTriggerService` checks the state against
  `TriggerIndex.permanentAbilities`, the "if" as it triggers (CR 603.4), calls `put` and marks
  `GameData.stateTriggerOnStack` only when it triggered.

## The stack door

`TriggeredAbilityStackService` is the only way a triggered ability reaches the stack: every matcher,
`PlanechaseService.trigger`, Saga chapters, state triggers, own enters abilities and engine-made abilities use it.

### The request

`TriggeredAbilityRequest`: the ability, source card and permanent (`null` for a card elsewhere; a graveyard card's
ability names that incarnation), controller, description, the `ConditionContext` for its "if", and a `TriggerRecord`
stamping the event; plus `causedByAttack()` / `causedBySourceAttacking()` (CR 508.3a), `withCause`, `withLimitCreature`,
`withCreatureDying()` (Teysa Karlov; not for an ability that triggers on the sacrifice itself, per her ruling),
`ofEmblem()`. The matcher judges filter, multiplicity and event condition
(`ConditionEvaluationService.eventConditionMet` / `triggerConditionsMet`, or a `TriggerJudge`) and nothing more; it never
builds an entry.

### The contract

`put(gameData, request)`, in order:

1. Judge the intervening "if" and wrap each effect (CR 603.4), except an `EventFactCondition` and a mode chosen as it
   triggers (the card writes the "if" into each mode: Jin Sakai); `TriggeringSpellMatches` uses the context's spell.
   Check and mark the limit (CR 603.2h; `limitCreatureId` per creature). Count additional triggers
   (`countAdditionalTriggeredAbilityTriggers`), one object each (CR 603.3), unless it triggers only once each turn.
2. Build one entry of the effects as written ("you may" stays, CR 603.5), with the granting permanent and
   `isTriggeredBySourceAttacking`.
3. Stamp the record whatever the effects are; aim an effect that acts only on a spell or ability at the record's
   triggering spell (non-targeting, CR 115.10a) unless the record names a non-player object.
4. Hand the record off and route inside `GameData.routeTriggeredAbility`, asking effects only through `CardEffect`: a
   mode chosen as it triggers parks (CR 603.3c), targets park (CR 603.3d), no legal choice removes it.
5. An ability that chooses nothing goes to `enqueue` / `GameData.enqueueTrigger`; ordering is
   `TriggeredAbilityQueueService`'s (CR 603.3b).

The door also stamps `TriggeredAbility.targetFilter()`, read by the target choice
(`TriggeredAbilityQueueService.targetFilterOf`) and resolution.

### Targets and modes

- **Mode.** `modesChosenAsItTriggers` (CR 603.3c) parks with the record's attacked target and triggering permanent;
  "choose up to X" reads X from the stamped entry (`maximumModesChosenAsItTriggers`, Riku of Many Paths), none removes it
  (CR 700.2b). A mode the stack can't announce (`StackedModalTriggerService.announcesMode` false: a graveyard target,
  "choose up to", "choose both instead") is announced as it goes on the stack, `choicesMaxCondition` judged in the
  request context. A random mode (`modesChosenAtRandomAsItTriggers`, Umaro, Raging Yeti) is picked among choosable modes,
  none removes it (CR 603.3c). A mode's targets are chosen before the next ability's mode (`ChoiceHandlerService`).
- **"That creature" vs a target.** An effect chooses a target unless the record names an object it could act on (a
  non-targeting `targetId` other than the source, or the source when also the triggering permanent: a lone attacking
  Noble Hierarch's own exalted) and the card doesn't declare `target(...)`. The named object stands in (CR 115.10a) only
  for an effect that could target nothing but its kind, so "any target" beside "that creature" is chosen. An undeclared
  player-targeting effect is "that player" (cards printing "target opponent" declare it: Brash Taunter, Daredevil,
  Fearless Fighter).
- **Graveyard.** `targetChosenAtRandom` (Power Pack) is picked now, none removes it. A graveyard target or choice
  (`targetSpec`, `graveyardCardChoice()`, which looks through "you may" and "if") parks for one graveyard choice with the
  record's X, power and attackers, narrowed to "that player's graveyard" when the record has a triggering player
  (`choosesFromEventPlayersGraveyard`), up to the event's amount for a count reading it (Froghemoth). The effect's own
  description picks the target for any departure (`GraveyardTargetingSupport.findTarget`: Iname, Life Aspect; Exhumer
  Thrull). "Another target card" is judged against the source's physical card (Gixian Puppeteer); "other than it" is
  `CardIsTriggeringCardPredicate`, bound by `AmountEvaluationService.bindTriggeringCard`; `CardIsSelfPredicate` is the
  source's card.
- **Groups.** Several groups (bound groups; a bare positional group belongs to the next bound group's ability: Gimli's
  Reckless Might), a dynamic count, one "up to one" group, or an Aura's group after what it enchants (Super-Soldier
  Serum) are chosen group by group (`ETBTokenMultiTargetTrigger`); otherwise one choice (`AttackTriggerTarget`), a
  group's targets announced together (CR 601.2c). An effect in a group no other ability has is bound by position
  (`targetsOwnGroup`, Glen Elendra's exchange); one bound to an Aura's first group declares no target (Strangling Grasp,
  Insubordination); an Aura's bare group belongs to no ability; a trailing bare group belongs to the last bound group's
  ability (Iroh, Tea Master). A graveyard card beside other groups or under the card's constraint (Orzhova) and a
  division made as it goes on the stack (`dividesAmongTargetsAsItGoesOnTheStack`, Towashi) are walked group by group; a
  target that may be a permanent or graveyard card (Taskmaster, Mercenary Mimic) is one choice; a joint
  battlefield-and-graveyard selection (`battlefieldAndGraveyardChoice`: Angel of Serenity, Savior of Ollenbock, Aurelia's
  Vindicator) is the mixed route, also used by controller end-step abilities with bound groups (graveyard candidates pass
  the group's predicate and the effects' shared validators). `MultiTargetConstraint.MANA_VALUE_AT_MOST_FIRST_TARGET`
  offers a first target only if a later group has a legal partner (CR 601.2c); `choosesSecondTargetAgainstFirst`
  (Puca's Mischief, Spawnbroker) likewise.
- **Up to.** An "up to one" group is walked only if it may target a player; permanent-only, its controller declines by
  choosing themselves; "up to N" with no legal target goes on with none and isn't given the record's "that player"
  (`StackEntry.hasTargetToChoose`).
- **Chooser.** The defending player where a combat ability says so (`combatTriggerTargetChosenByDefendingPlayer`: the
  record's triggering, else defending, player); the active player for `targetChosenByActivePlayer`,
  `MayChoicePlayer.ACTIVE_PLAYER` or `TargetFilter.activePlayerChoosesTarget`; an opponent for
  `targetChosenByAnOpponent` (Karplusan Minotaur: parked with no chooser, its pump naming the opponent or asking,
  `ChooseOpponentToChooseTriggerTarget`); else the controller.
- **Other.** `AttackCounterMoveEffect` (Decimator Beetle) parks for two choices. A random permanent or player target
  (Witch Hunt, Furnace Layer) is picked among legal ones (CR 603.3d). A spell-or-ability target is offered what its
  `StackEntryPredicateTargetFilter` admits (`TargetLegalityService.checkSpellTargetOnStack`). Choices offer players as
  players, filtered by the player filter, with the record's triggering permanent and X; the group walk applies each
  effect's restriction (Irma, Part-Time Mutant).

### Parking and `finishParked`

- A parked entry (`StackEntry.isParkedAtStackDoor`) travels as the choice's `triggerRecord` (`TriggeredModalTrigger`,
  `SpellGraveyardTargetTrigger`, `AttackTriggerTarget`, `EntersTriggerTarget`, `ETBSpellTargetTrigger`,
  `ETBTokenMultiTargetTrigger` with `withProgress`, `CounterDistributionAssignment`, `GraveyardTargetOperationState`;
  `withTriggerRecord`). The choice holds the record itself, not a copy, so a departure's stamp reaches the answer;
  simulations copy it.
- The handler finishes it through `finishParked(gameData, record, choice)` (or `(gameData, record, card, choice)` for a
  mode's targets on the runtime card copy, CR 603.3c): a new stack object of the record with the choice applied, then,
  once targeted, the becomes-the-target and crime checks (CR 700.13) and the Firebender Ascension watchers. Callers: the
  `AttackTriggerTarget` answers, `processNextAttackTriggerTarget`'s "up to one" none branch,
  `queueChosenTriggeredModalTrigger`'s no-target branch, the group walk's last group (`ETBTokenTargetService`,
  `StackEntry.adoptChosenTargets`), `GraveyardChoiceHandlerService` (a narrowed graveyard names that player as target)
  and `TriggeredAbilityQueueService.pushSpellGraveyardTriggeredAbilityWithoutTargets`.
- The pumps (`processNextAttackTriggerTarget`, `processNextETBTokenMultiTargetTrigger`,
  `processNextSpellGraveyardTargetTrigger`, `processNextTriggeredModalTrigger`) begin nothing while a choice awaits
  input. `GameData.graveyardTargetOperation` is one per game: a graveyard choice that would begin during another choice
  is queued as `SpellGraveyardTargetTrigger.deferring(...)`; never write over a choice in progress.
- A graveyard target restricted by the event is chosen before the entry exists, so it reads the record
  (`GameData.queuedTriggerRecord(choice)`), never an X of the choice or a rebuilt effect: the mana value limit
  (`dynamicMaxManaValue(new EventValue())`, `TriggeringCardManaValue`) is evaluated with the record as the amount's entry
  and the filter gets the record's event value (`CardManaValueEqualsEventValuePredicate`); on resolution the same
  amounts read the entry (`TargetValidationContext.eventValue`). A death trigger evaluates its limit as it triggers with
  the dying object as source (`AmountEvaluationService.evaluateAtDeath`, so `TriggeringPermanentManaValue` reads the
  last-known object, CR 603.10a; Soulshift) into `StackEntry.graveyardTargetMaxManaValue`. A matcher gives a choice the
  record's X, never the damage or a mana value: "that much" is `EventValue`.
- Choices begin after the triggering action at the pumps (`TriggeredAbilityQueueService.beginStackDoorChoices`, from
  `StackResolutionService`, `InputCompletionService`, and `EtbTriggerService` after own enters abilities unless a
  legend rule violation is pending: state-based actions come first).

Besides `put` (which holds what triggers while a mana ability is activated or resolves): `trigger` / `triggerOnce` /
`triggerPermanentChange`, `putManaAbility`, `holdForProducedMana` / `putHeld`, `putWithAnnouncedTargets`, `madeAbility` + `putMade` / `putDelayed`, `putInherent` (each in its section).

### Only the door builds a triggered entry

`TriggeredAbilityStackService` is the only subclass of the domain's `StackDoor`, whose protected `newTriggeredEntry`,
`enqueue` and `handOff` build, release and hand off entries; `GameData.enqueueTrigger` and `handOffTriggerRecord` are
package-private to it. `StackEntry`'s public constructors refuse `TRIGGERED_ABILITY`, and the stack refuses an
unreleased triggered entry and a carrier. A copy of a stack object (CR 707.10) or a record handed between handlers is
`StackEntry.derivedFrom(source, ...)` (keeps type and release); an object that stands in during a resolution and never
reaches the stack is `StackEntry.carrier(...)`. Tests use `TestStackDoor`. `TriggerStackDoorRatchetTest` (engine) fails
when a main source file other than the door, `StackDoor`, `StackEntry` and `StackEntryType` names `TRIGGERED_ABILITY`
as a value; use `StackEntryType.isAbility`.

### Handing off the trigger record

- The door builds the plain entry (source snapshot via `setSourcePermanentSnapshot` and everything known of the event)
  first and calls `GameData.handOffTriggerRecord(entry)` before any route, putting it in the game's one-shot hand.
  `queueInteraction` / `queueInteractionFirst` keep it with a queued choice (`pollPendingInteraction` returns it),
  `InteractionState.beginInteraction` with a prompt (`InteractionHandlerRegistry.dispatchAnswer` returns it), and the
  first `TRIGGERED_ABILITY` entry of the same source card that reaches the stack or `enqueueTrigger` takes it through
  `StackEntry.carryTriggerRecord`: the trigger event (`copyTriggerEventFrom`), source snapshot, how it was cast
  (alternative cost, kicker, optional and repeated costs, gift), once-each-turn key, active player, copy flag, X, and
  the non-targeting "that player" when the entry has none. Putting the handed-off entry itself empties the hand.
- So every route records what the plain entry records (CR 603.3c, 603.3d). Never copy event fields into a choice
  context or set them in a choice handler (the carry overwrites them); put missing data on the record in the matcher
  (the delayed end-step abilities' "that creature"). The entry takes the record's X when the choice set none; a target
  restricted by the event value is judged against the entry's event value; the choice's own
  `graveyardTargetMaxManaValue` stays.
- `routeTriggeredAbility` (`beginTriggerRouting` / `endTriggerRouting`) empties the hand on entry and drops leftovers on
  exit; a choice queued inside it with nothing handed off goes to `GameData.interactionWithoutTriggerRecord`, which
  `BaseCardTest` fails. `StackResolutionService.resolveTopOfStack` and the end of every game action
  (`GameMutationCoordinator`) drop leftovers, so a removed ability's record can't reach a later ability of its card.
- A permanent leaving while an ability naming it is still choosing is stamped on its record: `PermanentRemovalService`
  walks `GameData.stackEntriesAndTriggerRecords()` (stack, hand, queued choices, prompts) and `carryTriggerRecord`
  passes the last-known maps on; stamp last-known information through that method, not `gameData.stack`.
- "When this enters" and "whenever this attacks" records have no triggering permanent: an effect meaning its own
  permanent by "it" falls back to the entry's source (the `triggeringOrSource()` subject,
  `ExchangeControlOfTargetPermanentsEffectHandler`).

### Ordering and divisions

- CR 603.3b is one interaction per batch: once nothing waits for targets or a may choice,
  `TriggeredAbilityQueueService.beginTriggerOrder` (from `AutoPassService` before priority and before each
  combat-trigger auto-resolution) asks each player in APNAP order with two or more non-equivalent unordered triggers to
  place them bottom-up (`ChoiceContext.TriggerOrder`), only within the positions their batch occupies.
  `StackEntry.triggerOrderChosen` (survives simulation copies) keeps ordered entries, and all entries when the top
  resolves, out of later batches. `GameData.playersAutoOrderingTriggers` skips the prompt; the harness sets it for both
  players (`harness.setAutoOrderTriggers(player, false)` to test ordering).
- Divided triggers reaching the stack without a division (`DealDividedDamageEffect` / `DistributeCountersAmongTargetsEffect`
  `CHOSEN` with `etbAssignments` and no target restriction, e.g. Glint Weaver; `PreventDividedDamageEffect`;
  `DistributeCountersAmongCreaturesOnDeathEffect`, also in a `MayEffect`, e.g. Jugan) announce it before priority:
  `AutoPassService` calls `StackedDividedDamageTriggerService.announceNextDivision` after
  `StackedModalTriggerService.announceNextMode`. Amounts are evaluated against the entry (a death trigger reads the dying
  creature: `CountersOnTriggeringPermanent`, `TriggeringPermanentPower`). Kuldotha Flamefiend's
  `SacrificeArtifactThenDealDividedDamageEffect` may announce zero targets (`StackedDividedDamageTargets.allowsNoTargets`,
  `divisionAnnounced` with an empty division). A divided enter ability with a declared group (Fury, Bogardan Hellkite,
  Verdurous Gearhulk) divides in `ETBTokenTargetService`. The controller (Grand Ossuary: the dying creature's controller)
  picks one to `maxTargets` (`MultiPermanentChoiceContext.StackedDividedDamageTargets`) and divides via
  `ChoiceContext.CounterDistributionAssignment` with `stackEntryId` (`StackEntry.announceDamageAssignments`); no legal
  target removes it (CR 603.3d). The division is the entry's; copies keep it while offering new legal targets.

## What an entry records

Matchers record the event whatever the effects are; effects read it as they resolve. Nothing is bound into an effect.

- **Triggering permanent** (`setTriggeringPermanent(lastKnown, attachments)`, CR 603.10a): "its counters", "that
  creature's name", "the Auras attached to it", read via `getTriggeringPermanentSnapshot()`,
  `getTriggeringAttachments()`, `triggeringPermanentCounters()`, `CountersOnTriggeringPermanent`. Predicates read
  `FilterContext.triggeringPermanentId` while it is on the battlefield and `triggeringPermanentSnapshot` after
  (`PermanentSharesCardTypeWithTriggeringPermanentPredicate`, `PermanentControlledByTriggeringPermanentControllerPredicate`;
  id set by `TriggerTargetCollector` and `TargetLegalityService`, snapshot by `SacrificePermanentsEffectHandler`). Target
  choices carry it (`withTriggeringPermanent` on `DeathTriggerTarget`, `SelfTriggeredAbilityTarget`,
  `EntersTriggerTarget`; `SpellTargetTriggerAnyTarget.withDepartedPermanent`). A damage source is one too
  (`setTriggeringPermanentId`, `setTriggeringPermanentControllerId`; a spell source has only the controller); "that
  source's controller" is `GameQueryService.findTriggeringPermanentController` (current, else last known, CR 608.2h),
  read by `TRIGGERING_PERMANENT_CONTROLLER`. Pawpatch Recruit's "other than that creature" is
  `PermanentIsTriggeringPermanentPredicate`. Also the tapped permanent, the permanent an attachment's becomes-the-target
  ability watched (Fractured Loyalty) and the "attacks you" attacker.
- **Triggering permanents** (`setTriggeringPermanentIds`, "them"): `ONE_OR_MORE` combat damage dealers, a filtered
  `youAttack()`'s attackers; read by `EachPermanentScope.TRIGGERING_PERMANENTS`.
- **Triggering card** (`setTriggeringCard(cardId, graveyardEntryVersion)`): "that card" of a graveyard arrival, checked
  with `isTriggeringCardIncarnation` (CR 400.7), carried by `DeathTriggerTarget.withTriggeringCard`,
  `GraveyardTargetOperationState.triggeringCardId`, `PendingMayAbility.withTriggeringCard`. Every graveyard matcher
  records it with its incarnation; "its owner's hand" finds the owner from the graveyard holding it.
- **Triggering cards** (`setTriggeringCards`, as they were): "those cards", set by the discard-event,
  library-to-graveyard, leaves-the-graveyard and creatures-exiled matchers and discard follow-ups (Kaya, Spirits'
  Justice: `DrawTriggerPermanentTarget.withTriggeringCards`); read by `getTriggeringCards()`, `triggeringCardIds()`,
  `CardTypesAmongTriggeringCards`.
- **Triggering player** (`setTriggeringPlayerId`): monarch, discarder, damaged player, caster; carried by
  `SpellTargetTriggerAnyTarget.withTriggeringPlayer` (judged by its filter: Chandra's Incinerator). Many matchers also
  set "that player" as non-targeting `targetId` (`TRIGGERING_PLAYER` recipients). "That player controls" targets use
  `FilterContext.triggeringPlayerId` (`PermanentControlledByTriggeringPlayerPredicate`, Scalelord Reckoner), carried by
  `AttackTriggerTarget` and rechecked via `TargetValidationContext.triggeringPlayerId`; "another target player" is
  `PlayerOtherThanTriggeringPlayerPredicate` (The Lord of Pain).
- **Event value**: "that much / that many", read by `EventValue` and the event-value predicates.
- **Linked exiled card** (`linkedExiledCardId`, Hoarding Dragon, CR 607.2a): set from the imprint as it triggers.
- **Choice results.** A chosen card (`setChosenObjectCard`, set before the follow-up is inserted unchanged; effects that
  ask then act insert their own step: `GrizzledHuntmasterEffect.Step`; a later step reads `StepKind.CHOOSE`; a held card rides `PendingMayAbility.withHeldCard`), accepted players
  (`setAcceptedPlayerIds`, `Subjects.acceptedPlayers()`), produced mana (`setProducedManaColors`), picks
  (`StackEntry.recordPicks`, `ResolutionResults.pickedIds`, recorded by `LibraryChoiceHandlerService` and read by
  `LibrarySelectionFollowUp.effect()`: `MakeChosenPermanentAttackingEffect.pickedPermanent()`,
  `Subjects.picked(filter)`), a question's card
  (`PendingMayAbility.targetCardId`, `BecomeCopyOfCardUntilEndOfTurnEffect.ofQuestionCard()`), votes
  (`triggeringVotingResult`). A handler may rebuild its own effect with its progress but never puts an effect with a
  chosen object bound in on the stack.
- **Enchanted permanent to graveyard**: marked `awaitingSourceGraveyardEntry`; `GameData.markGraveyardEntry` records the
  Aura's stay as it follows (CR 704.5m, CR 400.7); `DealDamageToEnchantedPermanentControllerEffect.dealtByEnchantedPermanent`
  takes its source from the triggering permanent.
- `StackEntry.copyTriggerEventFrom` copies every trigger-event field; the copy constructor (so simulation copies) and
  `CopySupport.createCopyStackEntry` call it, so a copied ability reads the same event (CR 707.10). A new field goes
  there.

### Triggering spell

- `setTriggeringSpell` (the spell's entry as it triggered): effects read the live spell, then
  `getTriggeringSpellSnapshot()` (CR 608.2h). Cast records take it as they trigger; a "you may" carries it
  (`PendingMayAbility.withTriggeringSpell`); a `QueueReflexiveAbilityEffect`'s entry copies it.
- Its mana value is `TargetSpellManaValue`, never `EventValue` (the event value is the kick count of a kicked-spell
  trigger, else the mana value as a fallback); cascade, Kellan, Living Breakthrough, Lady Loki, Ancient Cellarspawn, The
  Ruinous Powers, Baral and Kari Zev use it. A target restricted by it (`PermanentManaValueEqualsXPredicate`,
  `PermanentMaxManaValueXPredicate`: Skyfire Kirin, Hammerhead Tyrant; `SpellGraveyardTargetTrigger.xValue`,
  `ReturnCardFromGraveyardEffect.requiresManaValueEqualsX` / `AtMostX`) is chosen with it as X. An effect's own filter by
  it (`PermanentManaValueEqualsTriggeringSpellPredicate`, `CardManaValueEqualsTriggeringSpellPredicate`: the Kirins) is
  bound on resolution by `AmountEvaluationService.bindTriggeringSpellManaValue`. A cast trigger's X is the spell's.
- `ManaSpentToCast`, `SpellManaSpentAtLeast`, `SpellCreatureManaSpentAtLeast`, `SpellManaSpentGreaterThanSourcePower`
  read `StackEntry.manaSpentToCast` / `creatureManaSpentToCast` (0 for an uncast copy, CR 707.10) from the snapshot or
  `ConditionContext.triggeringSpell`; `ColorsSpentToCast` reads `colorsSpentToCast` (CR 608.2h).
- `CopyTriggeringSpellEffect` (`permanentSpellCopiesAreTokens()`, `withGrantedKeywords`, `withoutSupertypes`,
  `asAdditionally`, `withCopies`; `CopyControllerCastSpellEffectHandler`); copy-for-each (Mirrorwing Dragon, Precursor
  Golem, Hive Mind, Glimmervoid Basin) via `CopySupport.triggeringSpell`. `CopyTriggeringSpellOrAbilityEffect` (Bill
  Potts, Rings of Brighthearth, Illusionist's Bracers) behind the printed "you may". Thousand-Year Storm:
  `withCopies(new SpellsCastBeforeTriggeringSpellThisTurn(filter))` up to `triggeringSpellCastIndex`; storm
  (`CopyTriggeringSpellEffect.storm()`, CR 702.40a) up to `triggeringSpellTurnCastIndex`; "copy this spell" entries record
  both places, X and repeated cost payments (replicate, gravestorm).
- "Counter that spell" (`CounterSpellEffect`, `CounterUnlessPaysEffect`) is aimed at it untargeted (Chalice of the Void,
  Void Mirror, Cephalid Shrine); Knowledge Pool, Possibility Storm, Eye of the Storm and Ice Cave read spell, caster and
  source from the entry.
- `TriggeringSpellMatches` on an effect is judged on resolution; Spellweaver Helix's "if" at the cast and on resolution.
  `PermanentIsTargetOfTriggeringSpellPredicate` (Loki, God of Lies, Dack Fayden's emblem) and Vesuvan Duplimancy use the
  spell's targets as it triggered; `TriggeringSpellTargetCount` counts on resolution. "Same name as that spell" is
  `CardSharesNameWithTriggeringSpellPredicate` (Harness the Storm, against `SpellGraveyardTargetTrigger.triggeringSpell`;
  `CardsInGraveyard`).

### Graveyard stays

`SourceCardInGraveyard` reads `StackEntry.sourceCardGraveyardEntryVersion()` (recorded by every matcher of a
graveyard card's ability with `setSourceGraveyardEntryVersion`, carried by `AttackTriggerTarget.withSourceGraveyardEntryVersion`
and `PendingMayAbility.sourceGraveyardEntryVersion`, else the triggering card's when the source is the triggering card),
so leaving and returning can't satisfy an older Bridge from Below ability (CR 400.7). Every "this card" effect
(`ReturnSourceCard…`, `ExileSourceCard…`, `ShuffleSelfFromGraveyardIntoLibrary`, a `CardIsSelfPredicate` return,
persist, `ForcedCostOrElseEffect`) reads the source's stay; `getTriggeringCardGraveyardEntryVersion()` is the event
card's. Delayed graveyard returns use `CardIdSetPredicate` with entry versions.

## Delayed abilities

- A delayed ability a resolution creates (CR 603.7a), with a duration or none ("when that land dies or is put into
  exile": `untilTriggered`, CR 603.7b), is a `TemporaryGlobalTriggeredAbility` (domain `model/`) in
  `GameData.temporaryGlobalTriggeredAbilities`: ability, source card and permanent (CR 603.7d, 603.7e), controller,
  duration, the permanent it is about (`watchedPermanentId`, CR 603.7c), the named player, the maker's linked exiled
  card. Factories: `about`, `thisTurn`, `nextThisTurn`, `untilYourNextTurn`, `boon`, `atNextStep`,
  `atNextStepUnlessPaid`, `atNextStepThisTurn`, `atEveryStep`, `forTheRestOfTheGame`; `withWatchedPlayer` names
  "that player" (Ertai's Meddling's `atEveryStep(upkeep(CHOSEN_PLAYER))`, which ends as its card leaves exile).
- An opening-hand reveal (CR 103.6b) makes one too, sourced by the revealed card (CR 603.7g): Chancellor of the Annex's
  `forTheRestOfTheGame` "when each opponent casts their first spell of the game" (`firstThisGame`), and `atNextStep`
  "at the beginning of the first upkeep" (`upkeep(ANY)`), "your first upkeep" (`upkeep(YOU)`), "your first main
  phase of the game" (`precombatMainPhase(YOU)`).
- Families match them like any ability: a `Watcher.of(delayed)` and a `TriggerCandidate.of(delayed)` (an observer, or
  the departing permanent's own when about it, so "this" is that permanent), functioning anywhere
  (`functionsAnywhere`); nothing a permanent loses removes it. One about a combat (`DelayedReferents.duringCombatPhase`)
  is listed only during it.
- Its request has no source permanent, so no doubler counts it (CR 603.2d). Its `EventAbilitySupport.DelayedRecord`
  stamps it made by a resolution with its linked card and target filter and names the maker's source permanent
  (`TriggeredAbilityRequest.entrySourcePermanentId`, CR 603.7e: Kjeldoran Elite Guard's sacrifice, Electric Seaweed's
  damage). The door removes an `untilTriggered` or `next` one (`triggersOnce`) as it triggers; cleanup ends
  `endsThisTurn` ones and durationless ones about a permanent that left.
- It triggers whether or not its maker's source remains; a source-relative filter (Loki's power) reads the source or
  `departedPermanentSnapshots`. A value chosen as the maker resolves is bound into the delayed ability, never kept on a
  permanent (Leori: `ChoiceContext.SpellPlaneswalkerTypeChoice` becomes the source filter).
- About a departing permanent it records the card it became (CR 400.7) and the controller it left with
  (`triggeringPermanentControllerId`, CR 603.10a); "dies under your control" (Desperate Measures) is judged on that
  controller (`TriggerMatcher.lastKnownMatches`, CR 603.10a). "It" is the event's object, never the source: Okoye's
  `Subjects.triggering()`, Basri's `PutCounterOnTargetPermanentEffect`, The Sea Devils'
  `byTriggeringPermanent`, an unblocked attacker's `UntapPermanentsEffect(Subjects.triggering())`, `AssignNoCombatDamageEffect` /
  `RemoveTargetFromCombatEffect` `.ofTriggeringPermanent()` (Gaze of Pain still chooses its target on the stack).
- Damage families read them via `delayedWatching` of the dealer, for "a creature you control deals combat damage to a
  player [or planeswalker]" (one naming a player is about that player alone), "deals combat damage to you" and "to this
  planeswalker" (its source permanent). Untap and control-change matchers list those about the permanent; "another"
  excludes the maker's source. Attack and block families list them.
- "When you next cast / activate … this turn" (`RegisterGlobalTriggeredAbilityEffect.nextThisTurn`, the remaining
  `CopyNext…ThisTurnEffect`s) is `nextThisTurn` (a mana-ability handler for a mana ability), triggering only
  the next time (CR 603.7b); a cast-and-activation one (Repeated Reverberation, Magus Lucea Kane) is used up by the
  first. Registrations (`RegisterDelayedControllerSpellCastTriggerEffect`, `RegisterDelayedTargetPlayerSpellCastTriggerEffect`,
  The Ruinous Powers' and Medomai's Prophecy's handlers) make one from
  `delayedAbility()` for their `Duration` (`thisTurn`, `nextThisTurn`, `untilYourNextTurn`, `boon`).
- **A noted number** ("note its power", then a boon "where X is the noted number") is a `NoteAmountEffect` step before the
  registration; `RegisterDelayedStepTriggerEffect` and `RegisterDelayedControllerSpellCastTriggerEffect` keep it on the
  delayed ability (`TemporaryGlobalTriggeredAbility.keepingNotedAmount`), whose effects read `AmountThisWay.noted()`.
- **Boons** are delayed abilities with no stated duration (`boon`, `CreateBoonEffect`), counted by `ControllerHasBoon`;
  an N-time boon has `uses`, and the door uses one as it triggers (`GameData.useDelayedAbility`). `boonUsedAsItResolves`
  (Underbridge Warlock) instead triggers at each of its steps whose "if" holds until its `ConsumeBoonEffect` resolves.
- "When you lose control of that Equipment [this turn]" and "when you lose control of it, tap it" are about the
  permanent, matched by `ControlChangeTriggers` (the permanent its triggering permanent). The cleanup step ends a "this
  turn" one only after control is recomputed (`GameData.endControlLossDelayedAbilitiesOfThisTurn`, CR 514.2), so it
  triggers as the control effect that ends with it ends.
- A duration, requirement or replacement a resolution schedules is a floating effect with a later `Duration.start`,
  not an ability.
- **Step.** `atNextStep`, `atNextStepUnlessPaid`, `atEveryStep` with a `StepTrigger` go on the stack before every other
  step ability. `TriggerMatcher.delayedStepMatch` reads `ANY` as "the next", `YOU` as "your next", `CHOSEN_PLAYER` as the
  named player's (`watchedPlayerId`). `DelayedReferents` names its objects and the turn, extra turn or combat phase it
  waits for (CR 500.8; `combatPhase` counts combat phases begun this turn, and adding a phase moves later waits back),
  what its permanent had attached (`permanentAttachments`) and a different triggering object (`triggeringPermanentId`);
  `RulesEventPipeline.delayedStepRecord` stamps them. It triggers once (CR 603.7b), even if its objects left, which it
  then doesn't affect (CR 603.7c); a false "if" as the step begins removes it. `atEveryStep` repeats (CR 603.7b; paradigm's, CR 702.192a); its
  `lastsWhile` (`CardsStillInZone`, `SourceRemainsTapped`) is judged in `RulesEventPipeline.putStepAbility`, false
  removing it untriggered.
- **Grouping.** A delayed ability belongs to its resolution (CR 603.7a): `resolveTopOfStack` counts resolutions
  (`GameData.resolutionOrdinal`, copied for simulations and kept across turn restarts) and
  `DelayedPermanentActions.schedule` stamps `DelayedPermanentAction.scheduledBy`. A resolution's scheduled permanent moves for a step are one ability with a
  `ResolveDelayedPermanentActionEffect` per permanent (`DelayedPermanentActions.schedule`, by source card, controller,
  step and resolution); delayed exile returns are one `ResolvePendingExileReturnEffect` (`PendingExileReturns.schedule`),
  attack and enchant choices made on resolution.
- **Examples.** Earthbend (CR 701.66a): two durationless abilities about the land, one per zone, independent of its
  abilities; the named object keeps an old trigger from returning a later incarnation. An activation's delayed ability
  is the activator's (CR 603.7e): Giant Oyster's `DelayedDrawStepTriggerWhileSourceTapped` (read by `StepTriggerService`,
  ended when the source untaps or leaves) and its untaps-or-leaves ability per resolution (`RegisterDelayedEventTriggerEffect.about…`,
  the creature as its triggering permanent; Tawnos's Coffin uses
  `ResolvePendingExileReturnEffect`). Cyclopean Tomb's linked lands live in
  its effect (`GameData.markCyclopeanTombLandRemoved` / `trackCyclopeanTombLands`).

## Reflexive abilities and abilities a resolution makes

- Reflexive (CR 603.12) and delayed (CR 603.7) abilities go through the door, never `stack.add`. The maker builds the
  entry with `madeAbility(...)`, a reflexive one from the resolving entry (`ReflexiveAbilitySupport.record(gameData,
  resolvingEntry, effects)`: card, controller, source, X, trigger event via `copyTriggerEventFrom`, snapshot, active
  player, alternative cost, "that player" non-targeting; `recordFor` / `resolvingEntryOf` for a choice handler's
  follow-up), stamps what it needs (cost payments as X, the permanent sacrificed, its own target filter) and calls
  `putMade` or `putDelayed`. The door marks `StackEntry.madeByResolution` and routes as `put` does (CR 603.3c, 603.3d);
  choices begin after the resolution.
- Its target groups are only those its own effects are bound to (and their bare groups), even if the maker's effect is
  bound there too; never another ability's group or the card's first filter. Targets the maker gave are kept; a delayed
  ability about an object acts on it and chooses nothing (CR 603.7c). The resolution's "that player" stands in for a
  target only as `put` judges an event's object.
- Reflexive "that player" (Dokuchi Silencer, CR 603.12) is the creating entry's triggering player: the combat damage
  matcher gives a non-targeted "you may" entry the damaged player, `DiscardCardThenEffectHandler` gives `DiscardFollowUp`
  `withTriggeringPlayer`, the target is validated with it (`validateActivatedAbilityTargeting`'s triggering-player
  overload) and `MayAbilityTriggerTarget.withTriggeringPlayer` passes it on. Triggered modals and the reflexive effects
  (`QueueReflexiveAbilityEffect`) reuse it the same way.
- No value is bound into a reflexive effect or picked by effect class: a number paid on resolution is the record's X
  (`XValue`, `CardManaValueEqualsXPredicate`); what the preceding action determined ("that much", a die roll, a revealed
  card's mana value, excess damage) is the event value (`EventValue`, `CardManaValueEqualsEventValuePredicate`,
  `CardManaValueAtMostEventValuePredicate`, `PermanentManaValueAtMostEventValuePredicate` via `FilterContext.eventValue`);
  a creature sacrificed as a cost has `SacrificedPermanentPower`, one a step of the maker sacrificed `AmountThisWay.sacrificedPower()` (a `QueueReflexiveAbilityEffect` record carries its maker's step results); a discarded card is the record's snapshot
  (`LastDiscardedCardManaValue`, `LastDiscardedCardTypeCount`, carried by `carryTriggerRecord`).
  `QueueReflexiveAbilityEffect` has no "event value as X". A random "that player" or "the tapped creature" is set on the
  record through `QueueReflexiveAbilityEffectHandler.resolve(gameData, entry, queueEffect, recordSetup)`, and targets are
  written with `PermanentControlledByTriggeringPlayerPredicate` / `PermanentPowerAtMostTriggeringPermanentPowerPredicate`.
  `aboutPickedPermanent(effect)` takes the next pick as source, one ability per created token.
- "If you do" (CR 608.2c: a paid cost's effects, an accepted question's payload, the after-cast effect, a library
  selection's attachment) resolves inline (`EffectResolutionService.resolveNestedInParkedResolution`); only a
  `QueueReflexiveAbilityEffect` in it makes an ability. Modal reflexive abilities announce modes at the door.
- Doublers don't count made abilities (Roaming Throne's rulings don't settle reflexive ones). `putMade(gameData, entry,
  copies)` puts a dungeon room's ability once per venture (CR 309.4c). "When this creature's echo is paid" uses `putMade`.
- An ability with groups its card can't declare (Earth Rumble's fight on a sorcery) is built with `madeAbility`, carried
  through its handler's two-step choice (CR 603.3d) and handed to `putMade` with targets.
- `CreateTokenThenEffect` ("create N tokens. When you do") triggers once per time the written action occurred
  (CR 603.12a): once per written quantity of written tokens, rounded down (one doubler twice, two four times, a tripler
  three times; Ajani, Nacatl Avenger's ruling is the one-token case, no ruling covers more); replacement tokens (Divine
  Visitation) and added kinds (Chatterfang's Squirrels) don't count; an unfixed quantity triggers once.

## Keyword and engine-made abilities

- Prowess, evolve and increment are `KeywordTriggers` abilities (CR 702.108a, 702.100a, 702.191a), one per instance a
  permanent has as the layers leave it (`GameQueryService.triggerKeywordInstances`, CR 702.100d, 702.108b, 702.191b),
  matched, doubled and put like other abilities, their "if"s checked twice (CR 603.4). Echo (`KeywordTriggers.echo`,
  CR 702.30a) uses the static `EchoEffect`'s cost; `CameUnderControlSinceLastUpkeep` reads
  `Permanent.controllerUpkeepsSinceControlled` (restarted by entering or a control change, counted at the controller's
  upkeep, phased out or not).
- Keyword and rule abilities are `TriggeredAbility`s (`static final` when fixed) put with a record, so "if", limit and
  doublers apply (CR 603.2d): decayed, melee, battle cry, dethrone, training, double team, granted exalted with
  `causedBySourceAttacking()` (CR 508.3a); enlist's "when you do" without it (it triggers on the enlist tap, CR 702.154a:
  Isshin doesn't double it, Firebender Ascension doesn't count it, as with exert); flanking; a Siege's defeat; vanishing;
  suspend, egg and refine counters; a cloaked permanent's ward; undying, persist, madness; a spell's cascade, storm,
  demonstrate, replicate, conspire, casualty (source the spell, no source permanent; CR 702.40a, 702.144a).
- Inherent abilities with no source (monarch, CR 726.2; initiative, CR 727.2; rad; speed; planeswalking) use
  `putInherent`: the holder controls it, no doubler, no target; the initiative's combat-damage ability names the taker
  non-targeting (`TakeInitiativeEffect(true)`).
- A spell's, reveal's or rule's "at the beginning of …" (epic, rebound, Judgment of Alexander, regeneration shields,
  exile returns, Gaze of Pain) is `madeAbility` + `putDelayed` (CR 603.7d, 603.7e). "Combat damage uses the stack" is
  one `putMade` object.

## Doublers: `TriggerCause`

The door counts "triggers an additional time" effects (CR 603.2d) from the request's `TriggerCause` (`withCause`):
`Entered` (Panharmonicon), `CreatureDied` (with `withCreatureDying()`, Teysa Karlov), `BecameTarget` (Valiant
Emberkin), `SpellCastOrCopied` (instant or sorcery), `TurnedFaceUp`, `Drew` and `Damage` (combat damage to a player, a
creature dealt damage), the permanents it names looked up as it counts. Richer events (`DamageTriggers`'
`SourceDealsDamage`, `SourceDealsCombatDamage`, `DamageToCreature`) hand over their `cause()`; attack-caused abilities
carry the attack flag. Doublers include last-known simultaneous death snapshots. None counts a delayed ability, a made
ability, an inherent one, an emblem's (except effects naming an emblem's creature-death ability), a spell's granted one,
or one limited to once each turn.

## Last-known information and look-back

- Leaves-the-battlefield, sacrifice and to-hand/library abilities look back (CR 603.10a): departure snapshots,
  `observerCandidates` before the event, `departedFilterMatches`, `lastKnownMatches`; phasing out too (CR 603.10b).
- "If that land is a Forest" / "that creature's types" read the triggering permanent on resolution; once it left,
  `PermanentRemovalService` has recorded its layered types, subtypes and colors on every entry it triggered
  (`StackEntry.triggeringPermanentLastKnownCard`, CR 608.2h), which `EffectResolutionService` gives a
  `TriggeringPermanentHasSubtype` "if" or "instead".
- `TriggeringPermanentPower` / `TriggeringPermanentToughness`: current value on the battlefield, else the value
  recorded as it triggered, else the departure snapshot.
- A spell that left the stack is read from its snapshot (CR 608.2h); a source that left before its ability is put is
  read as it last existed.

## Tools

- **Trigger ledger** (`TriggerLedger`, test fixtures, fed by the `RulesEventRecorder` every `GameTestHarness` attaches as the game's `RulesObserver`): with `MV_TRIGGER_LEDGER=<file>`, each triggered ability reaching
  the stack or a holding queue in a card test appends one line (test, position, source name, effect classes, the entry's
  event and choice fields, ids by role: `SOURCE`, `SELFCARD`, `p1`/`p2`, `perm:`, `card:`, `stack:`; `(additional)` for
  doubled objects). Each floating effect that begins or ends (`GameData.addFloatingEffect`, `putFloatingEffect`,
  `expireFloatingEffects`, `endFloatingEffect`, `replaceFloatingEffect`; nothing else changes `floatingEffects`) appends
  a `floating began` / `floating ended` line: source name, effect class, its `Duration`, the objects it affects by role
  and the turn and step it happened in. Run the same card tests before and after a change and diff; JVMs share the
  file, so sort it and drop the per-test sequence numbers (two players' abilities arrive in map order).
- **Record audit** (`MV_TRIGGER_RECORD_AUDIT=<file>`, `BaseCardTest.watchTriggerRecords`): `ROUTING` (also a test
  failure) and `UNSCOPED` choices begun without a record, `LEFT` records that left the hand untaken
  (`GameData.triggerRecordLeftBehind`), `MISSED` when such a record's ability is on the stack without one; each with
  engine frames.
- **Route test** (`TriggerRouteRecordTest`, application): compares every route of every `Triggers` factory with the
  plain entry, field by field; a new factory fails `everyTriggerFactoryHasAWatchOrAReason` until it has a watch or a
  reason in `UNMEASURED`; pairs reaching no entry are in `EXCLUDED`.
- **Ratchet**: `TriggerStackDoorRatchetTest` (see Only the door builds a triggered entry).
