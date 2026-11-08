#!/usr/bin/env python3
"""Step 15, run 17p: the agent docs show Covercast's mana spent as the ability's intervening "if".

Exact edits: each must apply once, or have been applied (idempotent). Fails when a doc still shows a zone-resident
cast trigger's mana spent as a `ConditionalEffect` payload.

Usage: step15-run17p-docs.py
"""
import pathlib
import sys

DOCS = pathlib.Path("agent-docs")

EDITS = {
    DOCS / "ACTIVATED_ABILITY_GUIDE.md": [
        ("""; a `ConditionalEffect(SpellManaSpentAtLeast)` payload is checked as an intervening-if before the trigger goes on the stack (Summitfest Closing Ceremony covercast) |""",
         """; "if five or more mana was spent to cast it" is the ability's intervening "if", `TriggerOptions.DEFAULT.withInterveningIf(new SpellManaSpentAtLeast(5))`, judged with the spell's mana spent as X (Summitfest Closing Ceremony's Covercast; the graveyard reader likewise) |"""),
    ],
    DOCS / "ORACLE_TEXT_EFFECT_MAP.md": [
        ("""`Triggers.youCastSpell() from the graveyard SpellCastTriggerEffect(INSTANT_OR_SORCERY, ConditionalEffect(new SpellManaSpentAtLeast(5), IntensifySourceCardEffect(1)))`""",
         """`Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY)` from the graveyard and from the hand, `TriggerOptions.DEFAULT.withInterveningIf(new SpellManaSpentAtLeast(5))`, `IntensifySourceCardEffect(1)`"""),
    ],
}

FORBIDDEN = ["a `ConditionalEffect(SpellManaSpentAtLeast)` payload", "ConditionalEffect(new SpellManaSpentAtLeast(5), IntensifySourceCardEffect"]


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
    for path in DOCS.glob("*.md"):
        for number, line in enumerate(path.read_text().splitlines(), 1):
            for pattern in FORBIDDEN:
                if pattern in line:
                    print(f"{path}:{number}: still shows {pattern}")
                    failed = True
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
