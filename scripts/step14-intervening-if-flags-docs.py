#!/usr/bin/env python3
"""Rewrites the agent-docs and skill passages that describe ConditionalEffect's interveningIf / triggerTimeOnly
flags, ConditionalEffect.unless and ConditionalEffect.atTriggerTime (effect-slot-refactor.md step 14, run 16s): a
ConditionalEffect is now only the "if" of an effect, checked as it resolves; a triggered ability's intervening "if"
(CR 603.4) and its event condition (CR 603.2) are TriggerOptions.

Exact replacements; running it again changes nothing.

Usage: python3 scripts/step14-intervening-if-flags-docs.py
"""
import glob
import re

RULE = ('`ConditionalEffect(condition, effect)` is the "if" of an effect, checked only as the effect resolves. A '
        'triggered ability\'s intervening "if" ("When/Whenever/At [trigger event], if [condition], [effect]", CR '
        '603.4) is never a `ConditionalEffect`: write it as `TriggerOptions.DEFAULT.withInterveningIf(condition)`, '
        'which the collector checks as the ability triggers and wraps around its effects to check again as it '
        'resolves; a condition of the trigger event is `withEventCondition(condition)` (CR 603.2). An "if" anywhere '
        'else in the text ("draw a card if …", "Then if …", "If …, instead …") is a `ConditionalEffect`.')

EXACT = {
    ".claude/skills/implement-card/SKILL.md": [
        ("Never write `ConditionalEffect.atTriggerTime` around a triggered ability's effects. and",
         "Never write a `ConditionalEffect` for either: " + RULE + " And"),
        ('is no intervening "if" and is `ConditionalEffect.unless(condition, effect)`, checked only as the ability '
         'resolves; an "… instead" or "Then if …" sentence on a cast-time fact (kicked, an alternative cost, the '
         'gift, X) keeps `new ConditionalEffect(…)` until step 14 run 16, since the enters collector reads those as '
         'the ability triggers (Desolation Angel).',
         'is no intervening "if" and is `new ConditionalEffect(condition, effect)`, checked only as the ability '
         'resolves; on a fact fixed as the permanent entered ("… instead" or "Then if …" on kicked, an alternative '
         'cost, the gift, X) the enters collector reads the same condition as the ability triggers, with the same '
         'result (Desolation Angel).'),
    ],
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        ("Metalcraft / Morbid / Raid / ControlsAnotherPermanent / OpponentControlsMoreLands are ETB trigger gates",
         RULE + "\n\nMetalcraft / Morbid / Raid / ControlsAnotherPermanent / OpponentControlsMoreLands are ETB trigger "
         "gates"),
        ("; use `ConditionalEffect.unless` for a resolution-only", "; a `ConditionalEffect` is the resolution-only"),
    ],
    "agent-docs/CARD_IMPLEMENTATION_PLAYBOOK.md": [
        ("Which engine layers support each ConditionalEffect. Check this before using a conditional in a new context.",
         "Which engine layers support each condition. Check this before using a conditional in a new context. "
         "\"Trigger Time\" means the condition works as a triggered ability's intervening \"if\" or event condition, "
         "written as `TriggerOptions.DEFAULT.withInterveningIf(condition)` / `withEventCondition(condition)`, never "
         "as a `ConditionalEffect` around the effects. " + RULE),
    ],
    "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("`SequenceEffect` of two resolution-only `ConditionalEffect.unless` branches",
         "two resolution-only `ConditionalEffect` branches of one ability"),
    ],
}

# "Intervening-if wrapper" rows describe a condition; the wrapper is the ability's option now.
WRAPPER = re.compile(r"Intervening-if wrapper: ")
WRAPPER_NEW = 'Intervening-if condition, written as `TriggerOptions.DEFAULT.withInterveningIf(…)` on the ability: '


def main():
    files = sorted(glob.glob("agent-docs/*.md")) + sorted(glob.glob(".claude/skills/*/SKILL.md"))
    for path in files:
        src = open(path).read()
        new = src
        for old, replacement in EXACT.get(path, []):
            if replacement in new:
                continue
            if old not in new:
                raise SystemExit("%s: passage not found: %s" % (path, old[:80]))
            new = new.replace(old, replacement)
        new = new.replace("ConditionalEffect.unless(", "ConditionalEffect(")
        new = new.replace("`ConditionalEffect.unless`", "`ConditionalEffect`")
        new = new.replace("ConditionalEffect#unless", "ConditionalEffect")
        new = WRAPPER.sub(WRAPPER_NEW, new)
        if new != src:
            open(path, "w").write(new)
            print("updated", path)


if __name__ == "__main__":
    main()
