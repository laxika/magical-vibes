#!/usr/bin/env python3
"""Step 15, run 17y5: "that creature deals that much damage" is `DealDamageToPlayersEffect` with the trigger event's
damage source as the damage's source.

Greatbow Doyen's `ReflectAllyDamageToDamagedCreatureControllerEffect` was a trigger-condition record whose class-keyed
handler built the stack entry with the Archer as its source; Saskia's `DealDamageToChosenPlayerEffect` and
`DealDamageToEachOtherOpponentEffect` (Kediss, Hydra Omnivore, Amarant Coral, Kosei, Vincent; Parapet Thrasher's
`int` form) carried the combat collector's class-keyed `CombatDamageDealerReferencingEffect` marker, and their handlers
rebuilt an entry with the creature that dealt the combat damage as its source. Each card now writes the existing
`DealDamageToPlayersEffect` with its recipient (`TRIGGERING_PERMANENT_CONTROLLER`, `CHOSEN_PLAYER`,
`EACH_OTHER_OPPONENT`), "that much" the `EventValue`, and `.dealtByEventDamageSource()` where the creature that dealt
the damage is not the ability's source ("that Archer", "it" of "a creature you control" / "a commander you control");
"it" of "whenever this creature deals combat damage" is the ability's source, and so is "this creature deals 4 damage".

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module.

Usage: step15-run17y5-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"
E = "model.effect."
A = "model.amount."

DEAL = [E + "DealDamageToPlayersEffect", E + "DamageRecipient"]
EACH_OTHER = "new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.EACH_OTHER_OPPONENT)"
OLD_EACH_OTHER = "new DealDamageToEachOtherOpponentEffect()"

CARDS = {
    "GreatbowDoyen": (
        [("                new ReflectAllyDamageToDamagedCreatureControllerEffect());",
          "                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER)\n"
          "                        .dealtByEventDamageSource());")],
        DEAL + [A + "EventValue"],
        [E + "ReflectAllyDamageToDamagedCreatureControllerEffect"]),
    "SaskiaTheUnyielding": (
        [("                new DealDamageToChosenPlayerEffect(new EventValue()));",
          "                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.CHOSEN_PLAYER)\n"
          "                        .dealtByEventDamageSource());")],
        DEAL,
        [E + "DealDamageToChosenPlayerEffect"]),
    "KedissEmberclawFamiliar": (
        [("                " + OLD_EACH_OTHER + ");",
          "                " + EACH_OTHER + ".dealtByEventDamageSource());")],
        DEAL + [A + "EventValue"],
        [E + "DealDamageToEachOtherOpponentEffect"]),
    "HydraOmnivore": (
        [("                " + OLD_EACH_OTHER + ");", "                " + EACH_OTHER + ");")],
        DEAL + [A + "EventValue"],
        [E + "DealDamageToEachOtherOpponentEffect"]),
    "AmarantCoral": (
        [("                " + OLD_EACH_OTHER + ");", "                " + EACH_OTHER + ");")],
        DEAL + [A + "EventValue"],
        [E + "DealDamageToEachOtherOpponentEffect"]),
    "KoseiPenitentWarlord": (
        [("                                " + OLD_EACH_OTHER + ")),",
          "                                " + EACH_OTHER + ")),")],
        DEAL,
        [E + "DealDamageToEachOtherOpponentEffect"]),
    "VincentVengefulAtoner": (
        [("                new ConditionalEffect(new SourcePowerAtLeast(7), " + OLD_EACH_OTHER + "));",
          "                new ConditionalEffect(new SourcePowerAtLeast(7),\n"
          "                        " + EACH_OTHER + "));")],
        DEAL + [A + "EventValue"],
        [E + "DealDamageToEachOtherOpponentEffect"]),
    "ParapetThrasher": (
        [("                                new DealDamageToEachOtherOpponentEffect(4)),",
          "                                new DealDamageToPlayersEffect(4, DamageRecipient.EACH_OTHER_OPPONENT)),")],
        DEAL,
        [E + "DealDamageToEachOtherOpponentEffect"]),
}

GONE = ["ReflectAllyDamageToDamagedCreatureControllerEffect", "DealDamageToChosenPlayerEffect",
        "DealDamageToEachOtherOpponentEffect", "CombatDamageDealerReferencingEffect"]


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
    for fqcn in removed:
        text = remove_unused_import(text, fqcn)
    text = sort_imports(text)
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
