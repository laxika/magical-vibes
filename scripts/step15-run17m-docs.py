#!/usr/bin/env python3
"""Step 15, run 17m: the agent docs show "Max speed — [ability]" as the ability's event condition.

Exact edits: each must apply once, or have been applied (idempotent). Fails when a doc still shows `MaxSpeed` as an
intervening "if" or a granted triggered ability's `ConditionalEffect`.

Usage: step15-run17m-docs.py
"""
import pathlib
import sys

DOCS = pathlib.Path("agent-docs")

EDITS = {
    DOCS / "EFFECTS_QUICK_REFERENCE.md": [
        (''' - `ConditionalEffect(new MaxSpeed(), CardEffect)` — the controller's persistent speed is 4;''',
         ''' - `MaxSpeed()` — the controller's persistent speed is 4 (a static `ConditionalEffect(new MaxSpeed(), effect)`; on a triggered "Max speed — [ability]" the ability's event condition, `TriggerOptions.DEFAULT.withEventCondition(new MaxSpeed())`, as the ability exists only at max speed);'''),
    ],
    DOCS / "ORACLE_TEXT_EFFECT_MAP.md": [
        ('''+ `ConditionalEffect(MaxSpeed, ReturnAllCardsExiledWithSourceEffect(true, null, false, Set.of(HASTE), ..., sacrificeAtEndStep=true))` |''',
         '''+ `TriggerOptions.DEFAULT.withEventCondition(new MaxSpeed())`, `ReturnAllCardsExiledWithSourceEffect(true, null, false, Set.of(HASTE), ..., sacrificeAtEndStep=true)` |'''),
        ('''`TriggerOptions.DEFAULT.withInterveningIf(new MaxSpeed())`, `new MayPayManaEffect("{1}", new CopyTriggeringSpellEffect(true).withoutNewTargetChoice(), prompt)` | Triggers.youCastSpell() | Mendicant Core, Guidelight''',
         '''`TriggerOptions.DEFAULT.withEventCondition(new MaxSpeed())` ("Max speed —": the ability exists only at max speed), `new MayPayManaEffect("{1}", new CopyTriggeringSpellEffect(true).withoutNewTargetChoice(), prompt)` | Triggers.youCastSpell() | Mendicant Core, Guidelight'''),
    ],
}

FORBIDDEN = ["withInterveningIf(new MaxSpeed())", "ConditionalEffect(MaxSpeed, ReturnAllCardsExiledWithSourceEffect"]


def main():
    failed = False
    for path, edits in EDITS.items():
        text = path.read_text()
        for old, new in edits:
            if text.count(old) == 1:
                text = text.replace(old, new)
            elif new not in text:
                print(f"{path}: edit not found: {old[:90]}")
                failed = True
        path.write_text(text)
    for path in list(DOCS.glob("*.md")) + list(pathlib.Path(".claude/skills").glob("*/SKILL.md")):
        for number, line in enumerate(path.read_text().splitlines(), 1):
            for pattern in FORBIDDEN:
                if pattern in line:
                    print(f"{path}:{number}: still shows {pattern}")
                    failed = True
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
