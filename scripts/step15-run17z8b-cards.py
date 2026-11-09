#!/usr/bin/env python3
"""Step 15, run 17z8b: Riku of Many Paths writes "whenever you cast a modal spell, choose up to X" as printed.

`ModalSpellCastTriggerEffect` was a record the cast collector's class-keyed handler rebuilt into an "up to X"
`ChooseOneEffect` from the triggering spell's mode count, skipping a spell that recorded none. The card now writes
`Triggers.youCastSpell().withStackEntryFilter(new StackEntryIsModalSpellPredicate())` and
`ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(options, true, 0, 3), new TriggeringSpellModeCount())`: the generic
cast collector evaluates the maximum from the triggering spell as the ability goes on the stack, where its modes are
chosen (CR 603.3c).

Idempotent, per letter module.

Usage: step15-run17z8b-cards.py <letter> [<letter> ...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

RIKU_OPEN = ("""        addTrigger(Triggers.youCastSpell(), new ModalSpellCastTriggerEffect(List.of(""",
             """        List<ChooseOneOption> options = List.of(""")
RIKU_CLOSE = ("""                                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()))
        )));""",
              """                                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of())));

        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryIsModalSpellPredicate()),
                new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(options, true, 0, options.size()),
                        new TriggeringSpellModeCount()));""")
RIKU_IMPORT = ("import com.github.laxika.magicalvibes.model.effect.ModalSpellCastTriggerEffect;\n", "")

CARDS = {
    "r": {"RikuOfManyPaths": ([RIKU_OPEN, RIKU_CLOSE, RIKU_IMPORT],
                              ["model.amount.TriggeringSpellModeCount", "model.effect.ChooseOneAtTriggerTimeEffect",
                               "model.effect.ChooseOneEffect", "model.filter.StackEntryIsModalSpellPredicate"])},
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
