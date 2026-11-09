#!/usr/bin/env python3
"""Step 15, run 17z7c: Lucy MacLean, Positively Armed writes its own "you may".

The enter collector's class-keyed `handleTokenEnterCopyForTargetPlayer` wrapped the card's
`CreateTokenCopyOfEnteringTokenForTargetPlayerEffect` in a `MayEffect` of its own and bound the entering token, and the
target-choice handler marked the "do this only once each turn" by the wrapped class. The card now writes its "you may"
as a `MayEffect`; the token-enter collectors queue the target choice with the entering token as the entry's triggering
permanent, and the choice carries the ability's limit (`TriggerLimit.DO_ONLY_ONCE_EACH_TURN`) to the entry.

Idempotent, per letter module.

Usage: step15-run17z7c-cards.py <letter> [<letter> ...]
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

LUCY = ("""        var ability = new CreateTokenCopyOfEnteringTokenForTargetPlayerEffect();""",
        """        var ability = new MayEffect(new CreateTokenCopyOfEnteringTokenForTargetPlayerEffect(),
                "Have target player create a token copy?");""")

CARDS = {
    "l": {"LucyMacLeanPositivelyArmed": ([LUCY], ["model.effect.MayEffect"])},
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
