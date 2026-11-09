#!/usr/bin/env python3
"""Step 15, run 17z7b: Next of Kin writes its own "you may" and "when enchanted creature dies".

The enchanted-death collector's class-keyed `handlePutLesserManaValueCreatureAndReturnAura` wrapped the card's
`PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect` in a `MayEffect` of its own, skipped a dying
permanent whose card isn't a creature card and baked the creature's mana value into the entry. The card now writes
`Triggers.creatureDies(ObjectRelation.ENCHANTED)` (the creature judged as it last existed on the battlefield, by the
trigger pipeline) and its "you may" as a `MayEffect`; the enchanted-death default puts it on the stack, and the effect
reads the entry's triggering permanent's mana value as it resolves.

Idempotent, per letter module.

Usage: step15-run17z7b-cards.py <letter> [<letter> ...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

NEXT_OF_KIN = ("""                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect());""",
               """                .addTrigger(Triggers.creatureDies(ObjectRelation.ENCHANTED), new MayEffect(
                        new PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect(),
                        "Put a creature card with lesser mana value from your hand or the command zone onto the"
                                + " battlefield?"));""")

CARDS = {
    "n": {"NextOfKin": ([NEXT_OF_KIN], ["model.effect.MayEffect"])},
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


def convert(path, replacements, imports):
    text = path.read_text()
    if all(new in text for _, new in replacements):
        return False
    for old, new in replacements:
        if old not in text:
            raise SystemExit(f"{path}: the expected text is missing: {old.strip()[:80]}")
        text = text.replace(old, new)
    for name in imports:
        text = add_import(text, f"import {PKG}.{name};\n")
    path.write_text(text)
    return True


def main(letters):
    for letter in letters:
        for name, (replacements, imports) in CARDS.get(letter, {}).items():
            path = (ROOT / f"magical-vibes-card/{letter}/src/main/java" / PKG.replace(".", "/")
                    / f"cards/{letter}/{name}.java")
            print(f"{name}: {'converted' if convert(path, replacements, imports) else 'already converted'}")


if __name__ == "__main__":
    main(sys.argv[1:] or sorted(CARDS))
