#!/usr/bin/env python3
"""Effect-slot refactor step 14 run 16a: agent-docs and skills passages for `TriggerOptions.withEventCondition`.

Exact replacements; an insertion is skipped when its text is already there, so running it again changes nothing.
"""
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

EVENT_CONDITION = (
    'A condition the trigger event itself requires ("whenever this creature attacks **while** you control a '
    'Dinosaur", "attacks alone", "an opponent draws a card during their turn", "when the fourth plan counter is put '
    'on this", "whenever you roll a 4 or higher") or one under which the ability exists at all ("Max speed —", a '
    'station, level or Spaceship threshold, an unlocked door, a chosen mode) is the event condition '
    '`TriggerOptions.DEFAULT.withEventCondition(condition)`: it is checked as the event occurs and never again as '
    'the ability resolves (CR 603.2), where an intervening "if" (`withInterveningIf`) is checked both times '
    '(CR 603.4). Never write `ConditionalEffect.atTriggerTime` around a triggered ability\'s effects.'
)

EDITS = {
    'agent-docs/EFFECTS_INDEX.md': [
        ('`ConditionalEffect.atTriggerTime` checks an event qualification during trigger collection without checking '
         'it again on resolution.',
         'A condition of the trigger event is `TriggerOptions.withEventCondition(condition)`, checked as the ability '
         'triggers and not again on resolution (CR 603.2).'),
    ],
    'agent-docs/CARD_IMPLEMENTATION_PLAYBOOK.md': [
        ('| `ConditionalEffect.atTriggerTime(new NotCondition(new SourceExertedThisTurn()), MayEffect(<exert>))` |',
         '| `TriggerOptions.DEFAULT.withEventCondition(new NotCondition(new SourceExertedThisTurn()))` + '
         '`MayEffect(<exert>)` |'),
        ('the attack-trigger collector unwraps the trigger-time conditional so the exert choice is still offered.',
         'the event condition is checked as the creature attacks, and the exert choice is made as the ability '
         'resolves.'),
    ],
    'agent-docs/EFFECTS_QUICK_REFERENCE.md': [
        ('`EtbEffectResolver` checks every `withInterveningIf` condition of a "when this enters" ability as it '
         'triggers.\n',
         '`EtbEffectResolver` checks every `withInterveningIf` condition of a "when this enters" ability as it '
         'triggers.\n- ' + EVENT_CONDITION + '\n'),
    ],
    'agent-docs/ORACLE_TEXT_EFFECT_MAP.md': [
        ('| "Whenever this creature attacks while saddled, seek a land card and put it onto the battlefield tapped." |',
         '| "Ferocious — Whenever this creature attacks while you control a creature with power 4 or greater, this '
         'creature gets +2/+2 until end of turn." | `addTrigger(Triggers.attacks(), TriggerOptions.DEFAULT'
         '.withEventCondition(new ControlsPermanent(PermanentAllOfPredicate(IsCreature, PowerAtLeast(4)))), '
         'BoostSelfEffect(2, 2))`; "while" is a condition of the attack, checked as it attacks only (CR 603.2) | '
         'Triggers.attacks() | Nighthowl Pursuer |\n'
         '| "Whenever this creature attacks while saddled, seek a land card and put it onto the battlefield tapped." |'),
    ],
    '.claude/skills/implement-card/SKILL.md': [
        ('"when the Nth [kind] counter is put on this" is still `ConditionalEffect.atTriggerTime(new '
         'SourceCounterThreshold(N, kind), …)` (checked only as it triggers)',
         '"when the Nth [kind] counter is put on this" is the event condition `TriggerOptions.DEFAULT'
         '.withEventCondition(new SourceCounterThreshold(N, kind))` (checked only as it triggers)'),
        ('Any other intervening "if" (CR 603.4: "when this dies, if you have no cards in hand, …") is an option: '
         '`addTrigger(Triggers.dies(ObjectRelation.THIS), TriggerOptions.DEFAULT.withInterveningIf(new '
         'ControllerHandEmpty()), effect)` (also on a target group), ',
         'Any other intervening "if" (CR 603.4: "when this dies, if you have no cards in hand, …") is an option: '
         '`addTrigger(Triggers.dies(ObjectRelation.THIS), TriggerOptions.DEFAULT.withInterveningIf(new '
         'ControllerHandEmpty()), effect)` (also on a target group). ' + EVENT_CONDITION + ' '),
    ],
    '.claude/skills/review-card/SKILL.md': [
        ('any other intervening "if" `TriggerOptions.DEFAULT.withInterveningIf(…)` and "triggers only once each turn" '
         '`withLimit(TriggerLimit.ONCE_EACH_TURN)`,',
         'any other intervening "if" `TriggerOptions.DEFAULT.withInterveningIf(…)`, a condition of the trigger event '
         '("attacks while …", "Max speed —", "when the Nth counter is put on this"; checked only as it triggers, '
         'CR 603.2) `withEventCondition(…)` and "triggers only once each turn" '
         '`withLimit(TriggerLimit.ONCE_EACH_TURN)`,'),
    ],
}


def main():
    dry = '--dry-run' in sys.argv
    for rel, edits in EDITS.items():
        path = os.path.join(ROOT, rel)
        s = open(path).read()
        for old, new in edits:
            if old not in s:
                if new in s:
                    continue
                print(f'MISSING in {rel}: {old[:100]!r}')
                continue
            if new in s and old in new:
                continue
            s = s.replace(old, new, 1)
        if not dry:
            open(path, 'w').write(s)


if __name__ == '__main__':
    main()
