#!/usr/bin/env python3
"""EffectSlot refactor step 14, run 16q: "when this enters" has no legacy view.

Rewrites, in place, the readers of the legacy view of the own enters abilities to the own-entry view
(`Card.getOwnEntryEffects()` / `getOwnEntryEffectRegistrations()`), and removes `Trigger.collectedAsAbility()`
with its overrides: with no legacy view left, every trigger's abilities are read as abilities.

    python3 scripts/step14-own-entry-readers.py [--dry-run]

Running it again changes nothing.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SOURCES = ["magical-vibes-domain/src/main", "magical-vibes-engine/src/main", "magical-vibes-engine/src/testFixtures",
           "magical-vibes-ai/src/main", "magical-vibes-networking/src/main"]

ENTERS = r"Triggers\.enters\(ObjectRelation\.THIS\)"
READERS = [
    (re.compile(r"getLegacyTriggerEffectRegistrations\(" + ENTERS + r"\)"), "getOwnEntryEffectRegistrations()"),
    (re.compile(r"getLegacyTriggerEffects\(" + ENTERS + r"\)"), "getOwnEntryEffects()"),
]
# An override of collectedAsAbility(): its Javadoc (or line comment), @Override and body.
OVERRIDE = re.compile(r"\n(?:    /\*\*(?:(?!\*/).)*?\*/\n|    //[^\n]*\n)*    @Override\n    public boolean collectedAsAbility\(\) \{\n"
                      r"(?:        [^\n]*\n)+?    \}\n", re.S)
DEFAULT = re.compile(r"\n    /\*\*(?:(?!\*/).)*?\*/\n    default boolean collectedAsAbility\(\) \{\n        return false;\n    \}\n",
                     re.S)


def rewrite(text):
    for pattern, replacement in READERS:
        text = pattern.sub(replacement, text)
    text = OVERRIDE.sub("\n", text)
    text = DEFAULT.sub("\n", text)
    return text


def unused_imports(text):
    for name in ("Triggers", "ObjectRelation"):
        imp = f"import com.github.laxika.magicalvibes.model.trigger.{name};\n"
        if imp in text and not re.search(r"\b" + name + r"\b", text.replace(imp, "")):
            text = text.replace(imp, "")
    return text


def main():
    dry = "--dry-run" in sys.argv
    changed = 0
    for source in SOURCES:
        for path in sorted((ROOT / source).rglob("*.java")):
            text = path.read_text()
            new = rewrite(text)
            if new == text:
                continue
            new = unused_imports(new)
            new = re.sub(r"\n\n\n+", "\n\n", new)
            changed += 1
            print(path.relative_to(ROOT))
            if not dry:
                path.write_text(new)
    print(f"{changed} files", file=sys.stderr)


if __name__ == "__main__":
    main()
