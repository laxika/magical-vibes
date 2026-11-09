#!/usr/bin/env python3
"""Step 15, run 17z6b: the cast triggers' "that spell's mana value" amounts read the triggering spell as they resolve.

`SpellCastTriggerCollectorService` decided by the effect's class (the `TriggeringSpellManaValueEffect` marker and
`effectCarriesTriggeringSpellManaValue`) whether to bake the spell's mana value as the entry's event value, which these
cards read through `EventValue`. The marker is gone; the collector records the cast event's value on every entry (the
kick count for a kicked-spell trigger, else the spell's mana value as the ability triggers), and these cards read
`TargetSpellManaValue` instead: the triggering spell as the ability resolves, else as it last existed on the stack
(the entry's snapshot of it, CR 608.2h), the recorded value only where neither is kept.

Every `new EventValue()` of these classes is "that spell's mana value" of a cast (or Deekah's copy) trigger; the import
is swapped. Idempotent: a file already converted is left alone. Per letter module.

Usage: step15-run17z6b-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
OLD_IMPORT = "import com.github.laxika.magicalvibes.model.amount.EventValue;"
NEW_IMPORT = "import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;"

# Card class -> the number of `new EventValue()` it holds, all of them the triggering spell's mana value.
CARDS = {
    "BrimazBlightOfOreskos": 1,
    "CabarettiRevels": 1,
    "ChromeHostSeedshark": 1,
    "DancingFromDarkToDawn": 1,
    "DeekahFractalTheorist": 1,
    "Dovescape": 1,
    "EndrekSahrMasterBreeder": 1,
    "GrahaTiaScionReborn": 2,
    "KeeperOfSecrets": 1,
    "MetallurgicSummonings": 2,
    "OvikaEnigmaGoliath": 1,
    "PassionateArchaeologist": 1,
    "PureReflection": 2,
    "SecretArcadeDustyParlor": 1,
    "SharkTyphoon": 2,
    "VialSmasherTheFierce": 1,
}


def sort_imports(text):
    """Keeps the project's import block sorted: the package's imports first, then java.*."""
    imports = list(re.finditer(r"^import [^;]+;$", text, re.M))
    if not imports:
        return text
    start, end = imports[0].start(), imports[-1].end()
    block = text[start:end]
    if re.search(r"^(?!import |\s*$).+", block, re.M):
        return text
    lines = [m.group(0) for m in imports]
    ours = sorted(l for l in lines if not l.startswith("import java"))
    java = sorted(l for l in lines if l.startswith("import java"))
    joined = "\n".join(ours) + ("\n\n" + "\n".join(java) if java and ours else "\n".join(java))
    return text[:start] + joined + text[end:]


def convert_file(path, name):
    if name not in CARDS:
        return
    text = path.read_text()
    count = text.count("new EventValue()")
    if count == 0:
        if "new TargetSpellManaValue()" not in text:
            raise SystemExit(f"{name}: neither EventValue nor TargetSpellManaValue found")
        return
    if count != CARDS[name]:
        raise SystemExit(f"{name}: expected {CARDS[name]} EventValue amounts, found {count}")
    text = text.replace("new EventValue()", "new TargetSpellManaValue()")
    if not re.search(r"\bEventValue\b", text.replace(OLD_IMPORT, "")):
        text = text.replace(OLD_IMPORT + "\n", "")
    if NEW_IMPORT not in text:
        first = re.search(r"^import [^;]+;$", text, re.M)
        text = text[:first.start()] + NEW_IMPORT + "\n" + text[first.start():]
    text = sort_imports(text)
    path.write_text(text)
    print(f"converted {name}")


def main():
    modules = sorted(set("abcdefghijklmnopqrstuvwxyz")) + ["shared"] if sys.argv[1:] == ["all"] else sys.argv[1:]
    if not modules:
        raise SystemExit(__doc__)
    for module in modules:
        root = ROOT / f"magical-vibes-card/{module}/src/main/java"
        if not root.exists():
            continue
        for path in sorted(root.rglob("*.java")):
            convert_file(path, path.stem)


if __name__ == "__main__":
    main()
