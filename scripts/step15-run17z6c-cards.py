#!/usr/bin/env python3
"""Step 15, run 17z6c: Foul Emissary's "when you sacrifice this while casting a spell with emerge" is a trigger.

`CreateTokenForEmergeSacrificeEffect` was a binding the legacy sacrifice handler unwrapped by class, judging the spell
whose cost the creature was sacrificed to pay. The trigger now names the spell being cast:
`Triggers.sacrificed(ObjectRelation.THIS).whileCasting(new CardKeywordPredicate(Keyword.EMERGE))`, with the token
creation as the ability's own effect.

Idempotent, per letter module.

Usage: step15-run17z6c-cards.py <letter> [<letter> ...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

CARDS = {
    "f": ["FoulEmissary"],
}

OLD_TRIGGER = """        addTrigger(Triggers.sacrificed(ObjectRelation.THIS),
                new CreateTokenForEmergeSacrificeEffect(new CreateTokenEffect(
                        1, "Eldrazi Horror", 3, 2, null,
                        List.of(CardSubtype.ELDRAZI, CardSubtype.HORROR), false)));"""
NEW_TRIGGER = """        addTrigger(Triggers.sacrificed(ObjectRelation.THIS).whileCasting(new CardKeywordPredicate(Keyword.EMERGE)),
                new CreateTokenEffect(1, "Eldrazi Horror", 3, 2, null,
                        List.of(CardSubtype.ELDRAZI, CardSubtype.HORROR), false));"""

IMPORT_EFFECT = f"import {PKG}.model.effect.CreateTokenForEmergeSacrificeEffect;\n"
NEW_IMPORTS = [
    f"import {PKG}.model.Keyword;\n",
    f"import {PKG}.model.filter.CardKeywordPredicate;\n",
]


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


def convert(path):
    text = path.read_text()
    if NEW_TRIGGER in text:
        return False
    if OLD_TRIGGER not in text:
        raise SystemExit(f"{path}: the expected trigger is missing")
    text = text.replace(OLD_TRIGGER, NEW_TRIGGER).replace(IMPORT_EFFECT, "")
    for line in NEW_IMPORTS:
        text = add_import(text, line)
    path.write_text(text)
    return True


def main(letters):
    for letter in letters:
        for name in CARDS.get(letter, []):
            path = (ROOT / f"magical-vibes-card/{letter}/src/main/java" / PKG.replace(".", "/")
                    / f"cards/{letter}/{name}.java")
            print(f"{name}: {'converted' if convert(path) else 'already converted'}")


if __name__ == "__main__":
    main(sys.argv[1:] or sorted(CARDS))
