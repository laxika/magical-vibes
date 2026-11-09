#!/usr/bin/env python3
"""Step 15, run 17z7b: the agent-docs rows of Next of Kin.

The card writes `Triggers.creatureDies(ObjectRelation.ENCHANTED)` and its own `MayEffect`; the enchanted-death default
puts it on the stack, and the effect reads the entry's triggering permanent's mana value as it resolves.

Idempotent (whole-fragment edits).

Usage: step15-run17z7b-docs.py
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| `PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect()` | Triggers.dies(ObjectRelation.ENCHANTED) | "
         "Next of Kin. Reads the dying creature's mana value from the stack entry's triggering permanent (the dying "
         "creature as it last existed on the battlefield, CR 603.10a),",
         "| `MayEffect(PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect(), prompt)` | "
         "`Triggers.creatureDies(ObjectRelation.ENCHANTED)` | Next of Kin. The card writes its \"you may\", asked as the "
         "ability resolves (CR 603.5); the enchanted-death default puts it on the stack. Reads the dying creature's mana "
         "value from the stack entry's triggering permanent (`TriggeringPermanentManaValue`: the dying creature as it "
         "last existed on the battlefield, CR 608.2h),"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect()` — optional Next of Kin death trigger: "
         "controller chooses",
         "- `PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect()` — Next of Kin death trigger, inside "
         "the card's `MayEffect` on `Triggers.creatureDies(ObjectRelation.ENCHANTED)`: controller chooses"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect` | `()` | Next of Kin death trigger: "
         "optionally chooses",
         "| `PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect` | `()` | Next of Kin death trigger, "
         "inside the card's `MayEffect` (the enchanted-death default puts it on the stack): chooses"),
    ],
}


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: expected exactly one occurrence of: {old[:100]}")
            text = text.replace(old, new)
        path.write_text(text)
        print(f"{name}: up to date")


if __name__ == "__main__":
    sys.exit(main())
