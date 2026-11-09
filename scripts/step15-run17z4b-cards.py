#!/usr/bin/env python3
"""Step 15, run 17z4b: the triggered mana abilities' produced-mana effects resolve by their own handlers.

The land-tap collector resolved Winter's Night's and Keeper of Progenitus's mana by class
(`AddProducedManaWhenSnowLandTappedEffect`, `AddProducedManaWhenLandOfSubtypeTappedEffect`, the `EFFECT_MERGE_PLAN.md`
land-tap trio), applied Winter's Night's untap lock by class (`TappedSnowLandDoesntUntapEffect`) as a second ability,
and judged Lavaleaper's "a basic land" as a component of the effect. Each card now writes the ability as printed:

- Winter's Night: one triggered mana ability, "whenever a player taps a snow land for mana" the trigger's `withFilter`,
  `SequenceEffect.of(new AddOneOfEachManaTypeProducedByLandEffect(false), new SkipNextUntapEffect(TRIGGERING))`;
- Keeper of Progenitus: `withFilter(new PermanentHasAnySubtypePredicate(Mountain, Forest, Plains))` +
  `AddOneOfEachManaTypeProducedByLandEffect(false)`;
- Lavaleaper: `withFilter(new PermanentHasSupertypePredicate(BASIC))` + `AddOneOfEachManaTypeProducedByLandEffect(false)`
  (the effect's `landFilter` component is gone);
- Regal Behemoth: `AddOneOfEachManaTypeProducedByLandEffect.anyColor(true, true)` (the factory lost the land filter).

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17z4b-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"
E = "model.effect."
F = "model.filter."

ONE = "new AddOneOfEachManaTypeProducedByLandEffect(false)"

CARDS = {
    "WintersNight": (
        [("""        addTrigger(Triggers.playerTapsLandForMana(), new AddProducedManaWhenSnowLandTappedEffect());
        addTrigger(Triggers.playerTapsLandForMana(), new TappedSnowLandDoesntUntapEffect());""",
          f"""        // Whenever a player taps a snow land for mana, that player adds one mana of any type that land produced.
        // That land doesn't untap during its controller's next untap step.
        addTrigger(Triggers.playerTapsLandForMana().withFilter(new PermanentHasSupertypePredicate(CardSupertype.SNOW)),
                SequenceEffect.of({ONE},
                        new SkipNextUntapEffect(TapUntapScope.TRIGGERING)));""")],
        [E + "AddOneOfEachManaTypeProducedByLandEffect", E + "SequenceEffect", E + "SkipNextUntapEffect",
         E + "TapUntapScope", F + "PermanentHasSupertypePredicate", "model.CardSupertype"],
        [E + "AddProducedManaWhenSnowLandTappedEffect", E + "TappedSnowLandDoesntUntapEffect"]),
    "KeeperOfProgenitus": (
        [("""        addTrigger(Triggers.playerTapsLandForMana(),
                new AddProducedManaWhenLandOfSubtypeTappedEffect(
                        List.of(CardSubtype.MOUNTAIN, CardSubtype.FOREST, CardSubtype.PLAINS)));""",
          f"""        addTrigger(Triggers.playerTapsLandForMana().withFilter(new PermanentHasAnySubtypePredicate(
                        Set.of(CardSubtype.MOUNTAIN, CardSubtype.FOREST, CardSubtype.PLAINS))),
                {ONE});""")],
        [E + "AddOneOfEachManaTypeProducedByLandEffect", F + "PermanentHasAnySubtypePredicate", "java:java.util.Set"],
        [E + "AddProducedManaWhenLandOfSubtypeTappedEffect", "java:java.util.List"]),
    "Lavaleaper": (
        [("""        addTrigger(Triggers.playerTapsLandForMana(),
                new AddOneOfEachManaTypeProducedByLandEffect(false,
                        new PermanentHasSupertypePredicate(CardSupertype.BASIC)));""",
          f"""        // Whenever a player taps a basic land for mana, that player adds one mana of any type that land produced.
        addTrigger(Triggers.playerTapsLandForMana().withFilter(new PermanentHasSupertypePredicate(CardSupertype.BASIC)),
                {ONE});""")],
        [], []),
    "RegalBehemoth": (
        [("AddOneOfEachManaTypeProducedByLandEffect.anyColor(true, null, true)",
          "AddOneOfEachManaTypeProducedByLandEffect.anyColor(true, true)")],
        [], []),
}

GONE = ["AddProducedManaWhenSnowLandTappedEffect", "AddProducedManaWhenLandOfSubtypeTappedEffect",
        "TappedSnowLandDoesntUntapEffect"]


def import_line(fqcn):
    return f"import {fqcn[5:]};" if fqcn.startswith("java:") else f"import {PKG}.{fqcn};"


def add_import(text, fqcn):
    line = import_line(fqcn)
    if line in text:
        return text
    imports = list(re.finditer(r"^import [^;]+;$", text, re.M))
    pos = imports[0].start()
    return text[:pos] + line + "\n" + text[pos:]


def remove_unused_import(text, fqcn):
    line = import_line(fqcn) + "\n"
    simple = line.rsplit(".", 1)[1].rstrip(";\n")
    if line in text and not re.search(r"\b" + simple + r"\b", text.replace(line, "")):
        text = text.replace(line, "")
    return text


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
    original = text
    edits, imports, removed = CARDS[name]
    for old, new in edits:
        if old in text:
            text = text.replace(old, new)
        elif new not in text:
            raise SystemExit(f"{name}: expected text not found:\n{old}")
    if text == original:
        return
    for fqcn in imports:
        text = add_import(text, fqcn)
    for fqcn in removed + ["model.CardSubtype"]:
        text = remove_unused_import(text, fqcn)
    text = sort_imports(text)
    text = re.sub(r"\n{3,}", "\n\n", text)
    for gone in GONE:
        if re.search(r"\b" + gone + r"\b", text):
            raise SystemExit(f"{name}: still names {gone}")
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
