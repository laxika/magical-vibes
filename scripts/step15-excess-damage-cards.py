#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 11: "that excess damage" reads the stack entry.

Idempotent; run once per letter module:

    for m in magical-vibes-card/*/; do python3 scripts/step15-excess-damage-cards.py "$m"; done

The "deals combat damage to a creature" collector no longer looks for CombatExcessDamageAwareEffect. Every entry it
builds carries the excess damage dealt to that creature (CR 120.4a) as its event value, and it judges each ability's
intervening "if" against that excess as the ability triggers (CR 603.4). So Overclocked Electromancer's "if that
creature was dealt excess damage this turn, you get X {E}, where X is that excess damage" is an intervening "if"
EventValueAtLeast(1) on the trigger and the existing EnergyCountersEffect(new EventValue()), instead of
GainEnergyEqualToExcessCombatDamageEffect.
"""
import pathlib
import re
import sys

EFFECT = "com.github.laxika.magicalvibes.model.effect."

CARDS = {
    "OverclockedElectromancer.java": {
        "replace": [
            ("""        addTrigger(Triggers.dealsCombatDamageToCreature(),
                new GainEnergyEqualToExcessCombatDamageEffect());""",
             """        addTrigger(Triggers.dealsCombatDamageToCreature(),
                TriggerOptions.DEFAULT.withInterveningIf(new EventValueAtLeast(1)),
                new EnergyCountersEffect(new EventValue()));"""),
        ],
        "add": ["com.github.laxika.magicalvibes.model.amount.EventValue",
                "com.github.laxika.magicalvibes.model.condition.EventValueAtLeast",
                "com.github.laxika.magicalvibes.model.trigger.TriggerOptions"],
        "remove": [EFFECT + "GainEnergyEqualToExcessCombatDamageEffect"],
    },
}


def fix_imports(src, add, remove):
    """Drops the removed imports and adds each missing one after the project import that sorts just before it."""
    for name in remove:
        src = src.replace(f"import {name};\n", "")
    for name in add:
        line = f"import {name};\n"
        if line in src:
            continue
        project = re.findall(r"^import com\.github\.[^;]+;\n", src, flags=re.M)
        before = [existing for existing in project if existing < line]
        anchor = before[-1] if before else project[0]
        at = src.index(anchor) + (len(anchor) if before else 0)
        src = src[:at] + line + src[at:]
    return src


def convert(path, spec):
    src = path.read_text()
    converted = src
    for old, new in spec["replace"]:
        if new in converted:
            continue
        if old not in converted:
            raise SystemExit(f"{path}: neither the old nor the new form of {old!r}")
        converted = converted.replace(old, new)
    converted = fix_imports(converted, spec["add"], spec["remove"])
    if converted != src:
        path.write_text(converted)
        print(f"converted {path}")


def main(module):
    for name, spec in CARDS.items():
        for path in sorted(pathlib.Path(module).rglob(name)):
            if "/build/" not in str(path):
                convert(path, spec)


if __name__ == "__main__":
    main(sys.argv[1])
