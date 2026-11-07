#!/usr/bin/env python3
"""Step 15, run 17l: the agent docs show the converted combat abilities' trigger-event conditions as event conditions.

Exact edits: each must apply once, or have been applied (idempotent). Fails when a doc still shows Creepy Crawler's or
Master of Cruelties' condition as a `ConditionalEffect`.

Usage: step15-run17l-docs.py
"""
import pathlib
import sys

DOCS = pathlib.Path("agent-docs")

EDITS = {
    DOCS / "CARD_IMPLEMENTATION_PLAYBOOK.md": [
        ('''"whenever this creature attacks a player" (not a planeswalker or battle); Master of Cruelties |''',
         '''"whenever this creature attacks a player" (not a planeswalker or battle): a condition of the trigger event, so Master of Cruelties' "attacks a player and isn't blocked" is `Triggers.attacksUnblocked()` with `TriggerOptions.DEFAULT.withEventCondition(new SourceAttackingPlayer())` |'''),
    ],
    DOCS / "EFFECTS_QUICK_REFERENCE.md": [
        ('''- `ConditionalEffect(new TargetPlayerIsAfraidOfController(), SequenceEffect.of(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER), new DrawCardEffect()))` - in `Triggers.dealsCombatDamageToPlayer()`, checks''',
         '''- `TargetPlayerIsAfraidOfController()` - event condition of `Triggers.dealsCombatDamageToPlayer()` ("deals combat damage to a player who is afraid of you": `TriggerOptions.DEFAULT.withEventCondition(...)`, `SequenceEffect.of(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER), new DrawCardEffect())`), checks'''),
    ],
    DOCS / "ORACLE_TEXT_EFFECT_MAP.md": [
        ('''| `ConditionalEffect(new TargetPlayerIsAfraidOfController(), SequenceEffect.of(new DiscardEffect(1, TARGET_PLAYER), new DrawCardEffect()))` | `Triggers.dealsCombatDamageToPlayer()` | Creepy Crawler''',
         '''| `TriggerOptions.DEFAULT.withEventCondition(new TargetPlayerIsAfraidOfController())`, `SequenceEffect.of(new DiscardEffect(1, TARGET_PLAYER), new DrawCardEffect())` | `Triggers.dealsCombatDamageToPlayer()` | Creepy Crawler'''),
        ('''| Triggers.dealsCombatDamageToPlayer() | Candela, Aegis of Adagia (YEOE 1). The normal hand-card choice is declinable, and the chosen creature enters untapped. |''',
         '''| Triggers.dealsCombatDamageToPlayer() with `TriggerOptions.DEFAULT.withEventCondition(new SourceCounterThreshold(8, CounterType.CHARGE))` (the station band the ability exists in) | Candela, Aegis of Adagia (YEOE 1). The normal hand-card choice is declinable, and the chosen creature enters untapped. |'''),
    ],
}

FORBIDDEN = ["ConditionalEffect(new TargetPlayerIsAfraidOfController()",
             "`ConditionalEffect(new SourceAttackingPlayer(), wrapped)` | - | yes | - | \"whenever this creature attacks a "
             "player\" (not a planeswalker or battle); Master"]


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
                    print(f"{path}:{number}: still shows {pattern[:60]}")
                    failed = True
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
