#!/usr/bin/env python3
"""Step 15, run 17r: the agent docs show a life ability's "during your turn" and vanishing's last time counter as event
conditions.

Exact edits: each must apply once, or have been applied (idempotent). Fails when a doc still shows "during your turn"
of a life trigger or vanishing's last-counter sacrifice as an intervening "if".

Usage: step15-run17r-docs.py
"""
import pathlib
import sys

DOCS = pathlib.Path("agent-docs")

EDITS = {
    DOCS / "ORACLE_TEXT_EFFECT_MAP.md": [
        ("""| "Whenever you gain life, this creature gains flying until end of turn." | `GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)` | Triggers.youGainLife() | Kalastria Nightwatch (`BFZ` 115); the keyword grant resolves as a non-targeting self-trigger and expires at end of turn |
""",
         """| "Whenever you gain life, this creature gains flying until end of turn." | `GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)` | Triggers.youGainLife() | Kalastria Nightwatch (`BFZ` 115); the keyword grant resolves as a non-targeting self-trigger and expires at end of turn |
| "Whenever you gain life during your turn, put a +1/+1 counter on this creature." | `PutCountersOnSourceEffect(1, 1, 1)` | `Triggers.youGainLife()`, `TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn())` | Vampire Scrivener (`SNC` 98). "During your turn" is a condition of the event, judged as the ability triggers, not an intervening "if"; likewise "whenever you lose life during your turn", "whenever an opponent loses life during your turn" (Kefka, `FIN` 231) and a Class level's life ability (`withEventCondition(new SourceClassLevelAtLeast(N))`, Cleric Class) |
"""),
        ("""| STATIC + Triggers.upkeep(ActivePlayer.YOU) | Calciderm (`PLC`). The upkeep effect removes a time counter only while one remains, and the last-counter sacrifice is a separate stack trigger |""",
         """| STATIC + Triggers.upkeep(ActivePlayer.YOU) | The upkeep effect removes a time counter only while one remains, and the last-counter sacrifice is a separate stack trigger. Calciderm (`PLC`) writes the abilities out: `Triggers.upkeep(ActivePlayer.YOU)` with `withInterveningIf(new SourceCounterThreshold(1, TIME))` and `RemoveCounterFromSourceEffect(TIME, 1)`, then `Triggers.timeCountersRemovedFromThis()` with `withEventCondition(new NotCondition(new SourceCounterThreshold(1, TIME)))` and `SacrificeSelfEffect()` (the last counter's removal is the trigger event, CR 702.63a) |"""),
    ],
    DOCS / "EFFECTS_QUICK_REFERENCE.md": [
        ("""- `ConditionalEffect(new ControllerTurn(), CardEffect)` — during your turn
""",
         """- `ConditionalEffect(new ControllerTurn(), CardEffect)` — during your turn; a trigger's "whenever you gain life during your turn" is the ability's event condition instead, `TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn())` (Vampire Scrivener)
"""),
    ],
}

FORBIDDEN = [
    "withInterveningIf(new NotCondition(new SourceCounterThreshold(1, TIME)))",
    "withInterveningIf(new NotCondition(new SourceCounterThreshold(1, CounterType.TIME)))",
]


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
