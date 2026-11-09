#!/usr/bin/env python3
"""Step 15, run 17z6e: a cast trigger's "where X is the amount of mana spent to cast that spell" is `ManaSpentToCast`.

The cast collector gave the ability the mana spent on the triggering spell as its X when an effect's class was on its
`effectNeedsSpellManaSpentX` list, so an `XValue` meant the mana spent only there. The judge is gone: the spell records
its mana spent as it becomes cast, and `ManaSpentToCast` reads it from the entry's snapshot of the spell as the ability
resolves. Each `XValue` of the four cards' cast triggers means the mana spent by its Oracle text.

Idempotent, per letter module.

Usage: step15-run17z6e-cards.py <letter> [<letter> ...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

CARDS = {
    "a": {
        "AberrantManawurm": [(
            "                new BoostSelfEffect(new XValue(), new Fixed(0)));",
            "                new BoostSelfEffect(new ManaSpentToCast(), new Fixed(0)));",
        )],
        "AetherfluxConduit": [(
            "addTrigger(Triggers.youCastSpell().anySpell(), new EnergyCountersEffect(new XValue()));",
            "addTrigger(Triggers.youCastSpell().anySpell(), new EnergyCountersEffect(new ManaSpentToCast()));",
        )],
    },
    "m": {
        "ManaformHellkite": [(
            """CardType.CREATURE, new Fixed(1), "Dragon Illusion", new XValue(), new XValue(),""",
            """CardType.CREATURE, new Fixed(1), "Dragon Illusion", new ManaSpentToCast(), new ManaSpentToCast(),""",
        )],
    },
    "s": {
        "ShantottoTacticianMagician": [(
            "                new BoostSelfEffect(new XValue(), new Fixed(0)),",
            "                new BoostSelfEffect(new ManaSpentToCast(), new Fixed(0)),",
        )],
    },
}

IMPORT_X = f"import {PKG}.model.amount.XValue;\n"
IMPORT_SPENT = f"import {PKG}.model.amount.ManaSpentToCast;\n"


def add_import(text, line):
    if line in text:
        return text
    lines = text.splitlines(keepends=True)
    imports = [i for i, l in enumerate(lines) if l.startswith("import ")]
    target = line.strip()
    for i in imports:
        if lines[i].strip() > target and not lines[i].startswith("import java."):
            lines.insert(i, line)
            return "".join(lines)
    java = [i for i in imports if lines[i].startswith("import java.")]
    lines.insert(java[0] if java else imports[-1] + 1, line)
    return "".join(lines)


def convert(path, replacements):
    text = path.read_text()
    if all(new in text and old not in text for old, new in replacements):
        return False
    for old, new in replacements:
        if old not in text:
            raise SystemExit(f"{path}: the expected line is missing: {old.strip()}")
        text = text.replace(old, new)
    if "new XValue()" not in text:
        text = text.replace(IMPORT_X, "")
    text = add_import(text, IMPORT_SPENT)
    path.write_text(text)
    return True


def main(letters):
    for letter in letters:
        for name, replacements in CARDS.get(letter, {}).items():
            path = (ROOT / f"magical-vibes-card/{letter}/src/main/java" / PKG.replace(".", "/")
                    / f"cards/{letter}/{name}.java")
            print(f"{name}: {'converted' if convert(path, replacements) else 'already converted'}")


if __name__ == "__main__":
    main(sys.argv[1:] or sorted(CARDS))
