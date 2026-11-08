#!/usr/bin/env python3
"""Step 15, run 17x2: the damage family's abilities read the stack entry.

Rewrites the card classes whose damage-trigger effect a collector rebuilt with the event's data bound in:

- All Will Be One: "that much damage" is `DealDamageToAnyTargetEffect(new EventValue())`, the counters put as the
  entry's event value (the collector rebuilt `DealDamageToAnyTargetEffect(0)` with the count).
- Flameblade Angel: "deal 1 damage to that source's controller" is the existing
  `DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER)`, the source the entry's triggering
  permanent (replacing `DealDamageToDamageSourceControllerEffect`, which the collector rebuilt with the source's ids).
- Hot Soup, Mortal Wound, Mire Blight, Sporogenic Infection, Cryoshatter: "whenever enchanted / equipped creature is
  dealt damage, destroy it" is `DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING)`, the creature dealt
  damage (the collector rewrote the cards' `ATTACHED` to it).

Idempotent: a file already converted is left alone. Per letter module.

Usage: step15-run17x2-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

DESTROY_OLD = """Triggers.enchantedCreatureDealtDamage(),
                new DestroyReferencedPermanentEffect(PermanentReference.ATTACHED));"""
DESTROY_NEW = """Triggers.enchantedCreatureDealtDamage(),
                new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));"""
DESTROY_OLD_INDENTED = """Triggers.enchantedCreatureDealtDamage(),
                        new DestroyReferencedPermanentEffect(PermanentReference.ATTACHED));"""
DESTROY_NEW_INDENTED = """Triggers.enchantedCreatureDealtDamage(),
                        new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));"""

# card -> (letter, [(old, new)], imports to add, imports to remove)
CARDS = {
    "AllWillBeOne": ("a", [
        ("""                new DealDamageToAnyTargetEffect(0));""",
         """                new DealDamageToAnyTargetEffect(new EventValue()));"""),
    ], ["model.amount.EventValue"], []),
    "FlamebladeAngel": ("f", [
        ("""                        new DealDamageToDamageSourceControllerEffect(1),""",
         """                        new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER),"""),
    ], ["model.effect.DamageRecipient", "model.effect.DealDamageToPlayersEffect"],
        ["model.effect.DealDamageToDamageSourceControllerEffect"]),
    "HotSoup": ("h", [(DESTROY_OLD, DESTROY_NEW)], [], []),
    "MortalWound": ("m", [(DESTROY_OLD_INDENTED, DESTROY_NEW_INDENTED)], [], []),
    "MireBlight": ("m", [(DESTROY_OLD_INDENTED, DESTROY_NEW_INDENTED)], [], []),
    "SporogenicInfection": ("s", [(DESTROY_OLD, DESTROY_NEW)], [], []),
    "Cryoshatter": ("c", [(DESTROY_OLD_INDENTED, DESTROY_NEW_INDENTED)], [], []),
}


def imp(cls):
    return f"import {PKG}.{cls};\n"


def import_line(name):
    return f"import {name[5:]};\n" if name.startswith("java:") else imp(name)


def convert(name, letter, edits, add, remove):
    path = ROOT / f"magical-vibes-card/{letter}/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"
    text = path.read_text()
    original = text
    for old, new in edits:
        if new in text:
            continue
        if old not in text:
            raise SystemExit(f"{name}: expected text not found:\n{old}")
        text = text.replace(old, new)
    for cls in remove:
        text = text.replace(import_line(cls), "")
    lines = text.split("\n")
    for cls in add:
        line = import_line(cls).rstrip("\n")
        if line in lines:
            continue
        imports = [i for i, l in enumerate(lines) if l.startswith("import ")]
        same_kind = [i for i in imports if lines[i].startswith("import java.") == line.startswith("import java.")]
        if same_kind:
            position = next((i for i in same_kind if lines[i] > line), same_kind[-1] + 1)
        else:
            position = imports[-1] + 1
            lines.insert(position, "")
            position += 1
        lines.insert(position, line)
    text = "\n".join(lines)
    # A java.util import removed last leaves the blank line that separated it.
    text = text.replace("\n\n\n@CardRegistration", "\n\n@CardRegistration")
    if text != original:
        path.write_text(text)
        print(f"converted {name}")


def main():
    letters = set("abcdefghijklmnopqrstuvwxyz") if sys.argv[1:] == ["all"] else set(sys.argv[1:])
    if not letters:
        raise SystemExit(__doc__)
    for name, (letter, edits, add, remove) in CARDS.items():
        if letter in letters:
            convert(name, letter, edits, add, remove)


if __name__ == "__main__":
    main()
