#!/usr/bin/env python3
"""Step 15, run 17z11f6: agent-docs rows of the forms the run changed or removed.

Idempotent; fails if a doc still names a removed form outside a "Gone" line. Optional repository-root argument.
Each edit replaces the one line holding its key (a substring unique to that line) with the new text; an edit whose
key is None appends the text after the line holding the anchor given instead.
"""
import pathlib
import re
import sys

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else '.')
DOCS = root / 'agent-docs'

APPLE_ROW = ('| "{T}, Pay 4 life, Sacrifice Apple of Eden: Look at target opponent\'s hand and exile those cards face down. '
             'You may play those cards this turn, and mana of any type can be spent to cast them. Until end of turn, '
             'whenever you play a land or cast a spell this way, its owner draws a card. At the beginning of the next end '
             'step, return the exiled cards to their owner\'s hand. Activate only as a sorcery." | `PayLifeCost(4)` + '
             '`SacrificeSelfCost()` + `ExileTargetPlayerHandFaceDownWithSourceAndReturnAtNextEndStepEffect()` + '
             '`AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect(null)` + '
             '`RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(new TriggeredAbility(Triggers.youCastSpell().exiledWithThis(), '
             'DrawCardForTargetPlayerEffect.ofTriggeringCardOwner(1)))` and the same with '
             '`Triggers.youPlayLand().exiledWithThis()` | activated ability | Apple of Eden, Isu Relic (ACR 70). The '
             'registration records the Apple as the registered abilities\' source (CR 603.7e), so "this way" is "a card '
             'exiled with this", judged by the cast and land-play collectors; "its owner" is the played card\'s owner. '
             'The hand cards are source-tracked, playable from exile with any mana through the end of turn, and each card '
             'still in exile returns at the next end step if it was not played |')

NUKA_ROW = ('| "Equipped creature gets +3/+0 and has intimidate. Whenever equipped creature attacks, until the end of '
            'defending player\'s next turn, that player gets two rad counters whenever they cast a spell." | '
            '`STATIC StaticBoostEffect(3, 0, EQUIPPED_CREATURE)` + `STATIC GrantKeywordEffect(INTIMIDATE, '
            'EQUIPPED_CREATURE)` + `Triggers.attacks() RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect'
            '.untilWatchedPlayersNextTurn(new TriggeredAbility(Triggers.playerCastsSpell().withStackEntryFilter(new '
            'StackEntryControlledByWatchedPlayerPredicate()), new GiveTargetPlayerRadCountersEffect(2)))` | Equipment '
            'static effects + attack trigger | Nuka-Nuke Launcher (PIP 138); the registration records the defending player '
            '(a planeswalker\'s controller too) as its watched player, the filter matches that player\'s spells, the '
            'watcher expires after that player\'s next turn, and "that player" is the caster the collector records. |')

BALTHIER_ROW = ('| "Vehicles you control get +1/+1 and have reach and vigilance. Whenever a Vehicle crewed by Balthier and '
                'Fran this turn attacks, if it\'s the first combat phase of the turn, you may pay {1}{R}{G}. If you do, '
                'after this phase, there is an additional combat phase." | `StaticBoostEffect(1, 1, Set.of(REACH, '
                'VIGILANCE), OWN_PERMANENTS, PermanentHasSubtypePredicate(VEHICLE))` + '
                '`addTrigger(Triggers.creatureAttacks().withFilter(new PermanentAllOfPredicate(List.of(new '
                'PermanentHasSubtypePredicate(VEHICLE), new PermanentCrewedThisTurnBySourcePredicate()))), '
                'TriggerOptions.DEFAULT.withInterveningIf(new FirstCombatPhase()), MayPayManaEffect("{1}{R}{G}", '
                'AdditionalCombatPhaseEffect(1)))` | STATIC + Triggers.creatureAttacks() | Balthier and Fran (FIN). A '
                'Vehicle is crewed by a creature tapped to pay its crew cost (CR 702.122c); the filter reads the crew '
                'record of this turn, the first combat phase is an intervening "if" (CR 603.4), and the "you may pay" is '
                'asked as the ability resolves (CR 603.5); no additional main phase |')

CONSTABLE_ROW = ('| "Whenever ~ deals combat damage to a player, return up to that many target permanents that player '
                 'controls to their owners\' hands." | `ReturnPermanentsOnCombatDamageToPlayerEffect.upToThatManyTargets()` '
                 '| Triggers.dealsCombatDamageToPlayer() | Cephalid Constable. The card declares the targets: up to the '
                 'damage dealt (the choice\'s event value) among the permanents of the player dealt damage, chosen as the '
                 'ability goes on the stack (CR 603.3d) |')

EDITS = {
    'EFFECTS_QUICK_REFERENCE.md': [
        ('- `TargetPlayerSpellCastTriggerEffect(List<CardEffect>)`',
         '- Gone: `TargetPlayerSpellCastTriggerEffect` (step 15 of effect-slot-refactor.md, run 17z11f6): "until the end '
         'of that player\'s next turn, whenever they cast a spell" is `RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect'
         '.untilWatchedPlayersNextTurn(new TriggeredAbility(Triggers.playerCastsSpell().withStackEntryFilter(new '
         'StackEntryControlledByWatchedPlayerPredicate()), effects...))` (Nuka-Nuke Launcher); "that player" is the caster'),
        ('- `TemporaryGlobalTriggerEffect` — trigger descriptor payloads',
         '- Gone: `TemporaryGlobalTriggerEffect`, `PlayFromOutsideHandTriggerEffect` (step 15, run 17z11f6): a registered '
         'ability\'s registration records the registering source permanent and the player the resolving ability names '
         '(`TemporaryGlobalTriggeredAbility.sourcePermanentId`, `.watchedPlayerId`), and its filters read them: '
         '`Triggers.youCastSpell().exiledWithThis()` / `Triggers.youPlayLand().exiledWithThis()` (Apple of Eden, Isu Relic), '
         '`StackEntryControlledByWatchedPlayerPredicate` (Nuka-Nuke Launcher)'),
        ('- `ReturnPermanentsOnCombatDamageToPlayerEffect()` / `(PermanentPredicate)` / `(PermanentPredicate, int fixedCount)`',
         '- `ReturnPermanentsOnCombatDamageToPlayerEffect()` / `(PermanentPredicate)` / `(PermanentPredicate, int fixedCount)` '
         '— Ninja-style return of permanents the player dealt combat damage controls, chosen as it resolves; '
         '`ReturnPermanentsOnCombatDamageToPlayerEffect.upToThatManyTargets()` is "return up to that many target permanents '
         'that player controls" (Cephalid Constable), its targets chosen as the ability goes on the stack'),
        ('- `DrawCardForTargetPlayerEffect(DynamicAmount, boolean requiresUntapped, boolean targets[, int targetGroup][, boolean opponentDrawStepOnly])`',
         '- `DrawCardForTargetPlayerEffect(DynamicAmount, boolean requiresUntapped, boolean targets[, int targetGroup][, '
         'boolean opponentDrawStepOnly])` or `(int)` — target/entry player draws; `XValue` for "target player draws X"; '
         '`forTargetGroup(amount, group)` routes a targeted activated-ability draw to that positional target group; '
         '`forOpponentDrawStep(amount)` restricts an `Triggers.drawStep(ActivePlayer.ANY)` draw to opponents\' draw steps; '
         '`ofTriggeringCardOwner(amount)` is "its owner draws" of the card a "whenever you play a card" event was about '
         '(Apple of Eden, Isu Relic)'),
        ('- `RegisterDelayedVehicleAttackEffect(CardEffect)`',
         '- Gone: `RegisterDelayedVehicleAttackEffect` (step 15, run 17z11f6): "whenever a Vehicle crewed by this this '
         'turn attacks" is `Triggers.creatureAttacks().withFilter(AllOf(PermanentHasSubtypePredicate(VEHICLE), '
         'PermanentCrewedThisTurnBySourcePredicate))` (Balthier and Fran)'),
        ('- `PlayFromOutsideHandTriggerEffect` — **Don\'t author**',
         '- "Whenever you cast a spell / play a land from anywhere other than your hand" is '
         '`addTrigger(Triggers.youCastSpell().notCastFrom(Zone.HAND), effects...)` / '
         '`addTrigger(Triggers.youPlayLand().notPlayedFrom(Zone.HAND), effects...)`, and "from exile" '
         '`.castFrom(Zone.EXILE)` / `.playedFrom(Zone.EXILE)` (a target as a target group); the cast and land-play '
         'collectors read such an ability itself (Gone: `PlayFromOutsideHandTriggerEffect`, run 17z11f6)'),
    ],
    'EFFECTS_INDEX.md': [
        ('| `ReturnPermanentsOnCombatDamageToPlayerEffect` |',
         '| `ReturnPermanentsOnCombatDamageToPlayerEffect` | `()`, `(PermanentPredicate filter)` or `(PermanentPredicate '
         'filter, int fixedCount)`; `upToThatManyTargets()` | return permanents when combat damage dealt to player '
         '(Ninja-style). Optional filter restricts which permanents can be chosen (e.g. `PermanentIsCreaturePredicate` for '
         'creatures only). `fixedCount` = 0 means "up to the combat damage dealt"; a positive value is a flat cap '
         'independent of the damage (`1` for "you may return target creature that player controls" — Mistblade Shinobi). '
         'Choosing fewer is always allowed, which supplies the "you may". `upToThatManyTargets()` (Cephalid Constable) '
         'declares the targets, up to the damage dealt among the damaged player\'s permanents, chosen as the ability goes '
         'on the stack, and returns the selected legal permanents at resolution. |'),
        ('| `DrawCardForTargetPlayerEffect` |',
         '| `DrawCardForTargetPlayerEffect` | `(DynamicAmount amount, boolean requireSourceUntapped, boolean targetsPlayer'
         '[, int targetGroup][, boolean opponentDrawStepOnly][, boolean triggeringCardOwner])` | the player in the entry\'s '
         'targetId draws that many cards ("target player draws X" = `XValue`, e.g. Blue Sun\'s Zenith); the '
         '`requireSourceUntapped` flag is used by no card (Howling Mine writes the intervening `SourceUntapped`); when '
         '`targetsPlayer=true`, auto-derives player targeting for activated abilities; a nonnegative `targetGroup` routes '
         'the draw to that positional activated-ability target group. `forOpponentDrawStep(amount)` marks an '
         '`Triggers.drawStep(ActivePlayer.ANY)` instance as applying only during an opponent\'s draw step. '
         '`ofTriggeringCardOwner(amount)` draws for the owner of the card a "whenever you play a card" event was about, '
         'read from the entry as it resolves ("its owner draws a card", Apple of Eden, Isu Relic). Convenience ctors: '
         '`(int amount)` = `(Fixed, false, false)`, `(int amount, boolean requireSourceUntapped)`. When the entry carries a '
         'multi-target list instead of a single `targetId`, every legal target player draws that many (Jace, Memory '
         'Adept\'s -7 "any number of target players each draw twenty cards") |'),
        ('| `RegisterDelayedVehicleAttackEffect` |',
         '| Gone: `RegisterDelayedVehicleAttackEffect` (step 15, run 17z11f6) | | "Whenever a Vehicle crewed by this this '
         'turn attacks" is `Triggers.creatureAttacks().withFilter(AllOf(PermanentHasSubtypePredicate(VEHICLE), '
         'PermanentCrewedThisTurnBySourcePredicate))` (Balthier and Fran) |'),
        ('| `PlayFromOutsideHandTriggerEffect` |',
         '| Gone: `PlayFromOutsideHandTriggerEffect`, `TargetPlayerSpellCastTriggerEffect`, `TemporaryGlobalTriggerEffect` '
         '(step 15, run 17z11f6) | | "Whenever you cast a spell / play a land from anywhere other than your hand" is '
         '`Triggers.youCastSpell().notCastFrom(Zone.HAND)` / `Triggers.youPlayLand().notPlayedFrom(Zone.HAND)`; a '
         'registered ability\'s filters read what its registration recorded: `exiledWithThis()` the registering source '
         'permanent (Apple of Eden, Isu Relic), `StackEntryControlledByWatchedPlayerPredicate` the watched player '
         '(Nuka-Nuke Launcher) |'),
    ],
    'ORACLE_TEXT_EFFECT_MAP.md': [
        ('| "Equipped creature gets +3/+0 and has intimidate. Whenever equipped creature attacks, until the end of '
         'defending player\'s next turn, that player gets two rad counters whenever they cast a spell." |', NUKA_ROW),
        ('| "Vehicles you control get +1/+1 and have reach and vigilance. Whenever a Vehicle crewed by Balthier and Fran',
         BALTHIER_ROW),
        ('| "Whenever ~ deals combat damage to a player, return up to that many target permanents that player',
         CONSTABLE_ROW),
        ('| "{T}, Pay 4 life, Sacrifice Apple of Eden:', APPLE_ROW),
    ],
    'PREDICATES_REFERENCE.md': [
        ('| `PermanentCrewedBySourceThisTurnPredicate` |',
         '| `PermanentCrewedBySourceThisTurnPredicate` | `()` | permanents that were tapped to pay the source Vehicle\'s '
         'crew cost this turn | source permanent snapshot |\n'
         '| `PermanentCrewedThisTurnBySourcePredicate` | `()` | Vehicles the source was tapped to crew this turn (CR '
         '702.122c): "a Vehicle crewed by Balthier and Fran this turn" | the source permanent\'s id; '
         '`GameData.creaturesThatCrewedPermanentThisTurn` |'),
    ],
    'TRIGGER_SLOT_TARGETING.md': [
        ('| `Triggers.crewsVehicle()` |',
         '| `Triggers.crewsVehicle()` | `TriggerCollectionService.checkCrewsVehicleTriggers` from `CrewCostHandler`; the '
         'Vehicle is stored as the triggered entry\'s `triggeringPermanentId` so effects can resolve against "that '
         'Vehicle" | Non-targeting |\n'
         '| `Triggers.permanentEnteringCausesAbilityToTrigger()` | `EtbTriggerService.queueTriggeredAbilityCounters`, as '
         'an own enters ability goes on the stack: the abilities go on the stack after it as written, recording it as the '
         'triggering spell, its controller as the triggering player and its stack object as the non-targeting "that '
         'ability"; a watcher that lost its abilities doesn\'t trigger (Strict Proctor\'s `CounterUnlessPaysEffect(2)`) '
         '| Non-targeting |'),
    ],
}

STACK_PREDICATE_ANCHOR = '`StackEntryControlledByChosenPlayerPredicate`'
STACK_PREDICATE_ROW = ('| `StackEntryControlledByWatchedPlayerPredicate` | `()` | a spell cast by the player the registration '
                       'of a registered ability recorded (`TemporaryGlobalTriggeredAbility.watchedPlayerId`): "whenever '
                       'they cast a spell" (Nuka-Nuke Launcher); read only by the registered any-player cast collector, '
                       'any other reader matches nothing | the registration\'s watched player |')

REMOVED = ['RegisterDelayedVehicleAttackEffect', 'PlayFromOutsideHandTriggerEffect', 'TargetPlayerSpellCastTriggerEffect',
           'TemporaryGlobalTriggerEffect', 'StrictProctorEffect', 'TriggeredAbilityCounterEffect',
           'withTriggerTimeTargets', 'markSourceOncePerTurnOnAccept']

ARCH_OLD = ("the event's object as a non-targeting referent whatever the effects are; no effect is bound by its class)")
ARCH_NEW = ("the event's object as a non-targeting referent whatever the effects are; no effect is bound by its class; "
            "accepting the \"you may\" of a \"do this only once each turn\" ability uses up the limit its "
            "`TriggerLimit.DO_ONLY_ONCE_EACH_TURN` marked on the resolving entry, read from no effect)")


def architecture():
    path = DOCS / 'ARCHITECTURE.md'
    s = path.read_text()
    if ARCH_NEW in s:
        return
    if s.count(ARCH_OLD) != 1:
        raise SystemExit('ARCHITECTURE.md: the may-handler sentence is not there once')
    path.write_text(s.replace(ARCH_OLD, ARCH_NEW))


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        lines = path.read_text().split('\n')
        for key, new in edits:
            if all(line in lines for line in new.split('\n')):
                continue
            hits = [i for i, line in enumerate(lines) if key in line]
            if not hits:
                raise SystemExit(f'{name}: no line holds {key!r}')
            if len(hits) > 1:
                raise SystemExit(f'{name}: {len(hits)} lines hold {key!r}')
            lines[hits[0]:hits[0] + 1] = new.split('\n')
        path.write_text('\n'.join(lines))
    path = DOCS / 'PREDICATES_REFERENCE.md'
    lines = path.read_text().split('\n')
    if STACK_PREDICATE_ROW not in lines:
        hits = [i for i, line in enumerate(lines) if STACK_PREDICATE_ANCHOR in line and line.startswith('|')]
        if len(hits) != 1:
            raise SystemExit(f'PREDICATES_REFERENCE.md: {len(hits)} rows hold {STACK_PREDICATE_ANCHOR}')
        lines.insert(hits[0] + 1, STACK_PREDICATE_ROW)
        path.write_text('\n'.join(lines))
    architecture()
    bad = []
    for path in DOCS.glob('*.md'):
        for n, line in enumerate(path.read_text().split('\n'), 1):
            if any(re.search(r'\b' + form + r'\b', line) for form in REMOVED) and 'Gone' not in line:
                bad.append(f'{path.name}:{n}')
    if bad:
        raise SystemExit('still named: ' + ', '.join(bad))


if __name__ == '__main__':
    main()
