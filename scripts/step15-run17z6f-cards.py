#!/usr/bin/env python3
"""Step 15, run 17z6f: the Kirins' "with that spell's mana value" reads the triggering spell as the ability resolves.

The cast collector gave the ability the triggering spell's mana value as its X when an effect's own filter read X
(`effectFilterReadsManaValueX`, keyed by `DestroyAllPermanentsEffect` and `RevealHandAndDiscardMatchingCardsEffect`).
The judge is gone: Celestial Kirin's destroy and Infernal Kirin's discard filter by the new
`PermanentManaValueEqualsTriggeringSpellPredicate` / `CardManaValueEqualsTriggeringSpellPredicate`, which their handlers
bind to the entry's triggering spell as the ability resolves (the live spell, else the entry's snapshot of it).

Idempotent, per letter module.

Usage: step15-run17z6f-cards.py <letter> [<letter> ...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

COMMENT_OLD = ("        // \"With that spell's mana value\": the ability's X, which the cast collector makes the spell's mana "
               "value.\n")
COMMENT_NEW = ("        // \"With that spell's mana value\": the triggering spell's, read as the ability resolves (the spell as it "
               "last\n        // existed on the stack once it has left).\n")

CARDS = {
    "c": {
        "CelestialKirin": ("PermanentManaValueEqualsXPredicate", "PermanentManaValueEqualsTriggeringSpellPredicate"),
    },
    "i": {
        "InfernalKirin": ("CardManaValueEqualsXPredicate", "CardManaValueEqualsTriggeringSpellPredicate"),
    },
}


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


def convert(path, old, new):
    text = path.read_text()
    if f"new {new}()" in text and f"new {old}()" not in text:
        return False
    for expected in (f"new {old}()", COMMENT_OLD):
        if expected not in text:
            raise SystemExit(f"{path}: the expected text is missing: {expected.strip()}")
    text = text.replace(f"new {old}()", f"new {new}()").replace(COMMENT_OLD, COMMENT_NEW)
    text = text.replace(f"import {PKG}.model.filter.{old};\n", "")
    text = add_import(text, f"import {PKG}.model.filter.{new};\n")
    path.write_text(text)
    return True


def main(letters):
    for letter in letters:
        for name, (old, new) in CARDS.get(letter, {}).items():
            path = (ROOT / f"magical-vibes-card/{letter}/src/main/java" / PKG.replace(".", "/")
                    / f"cards/{letter}/{name}.java")
            print(f"{name}: {'converted' if convert(path, old, new) else 'already converted'}")


if __name__ == "__main__":
    main(sys.argv[1:] or sorted(CARDS))
