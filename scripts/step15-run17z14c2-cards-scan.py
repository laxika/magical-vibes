#!/usr/bin/env python3
"""Step 15, run 17z14c2: the cards with an ability on a watch whose collector the run changed.

Run 17z14c2 made `TriggerCollectionService`'s damage, becomes-the-target and planar collectors put each ability on the
stack whole (CR 603.3) and separate abilities apart (CR 603.3b). A card is affected when it writes an ability of two or
more effects on one of their triggers (one object now, one per effect before where the collector looped), or two or
more abilities on one of them (two objects now, one before where the collector joined them).

Read-only. Usage:
  step15-run17z14c2-cards-scan.py            the cards with a multi-effect ability or several abilities, per factory
  step15-run17z14c2-cards-scan.py --all      every card that names one of the factories (the card tests to run)

A factory is a `Triggers.<name>(` call: the factories are read from `DamageWatch`, `TargetWatch` and `CombatWatch` (the
constants in `WATCHES` below) and `PLANAR` lists the zone-change factories the planar collectors read on planes.
"""
import collections
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
TRIGGER_DIR = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/trigger"

WATCHES = {
    "DamageWatch": {
        "ON_DAMAGE_TO_PLAYER", "ON_DAMAGE_TO_OPPONENT", "ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU",
        "ON_ENCHANTED_CREATURE_DEALS_DAMAGE", "ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_CREATURE",
        "ON_OPPONENT_CREATURE_DEALT_DAMAGE", "ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_DAMAGE",
        "ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_NONCOMBAT_DAMAGE",
        "ON_OPPONENT_CREATURE_DEALT_EXCESS_NONCOMBAT_DAMAGE", "ON_DEALT_DAMAGE", "ON_NONCOMBAT_DAMAGE_TO_SELF",
        "ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_OPPONENT", "ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_PLAYER",
        "ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU", "ON_CREATURE_DEALS_DAMAGE_TO_ENCHANTED_PLANESWALKER",
    },
    "TargetWatch": None,
    "CombatWatch": {
        "ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT", "ON_ANY_CREATURE_COMBAT_DAMAGE_TO_PLAYER",
        "ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OWNER", "ON_GOADED_CREATURES_COMBAT_DAMAGE_TO_OPPONENT",
        "ON_CONTROLLER_DEALT_COMBAT_DAMAGE", "ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU",
        "ON_ALLY_ATTACKING_CREATURE_ABILITY_TRIGGERS",
    },
}
PLANE_ONLY = {
    "CombatWatch": {"ON_ALLY_CREATURES_ATTACK", "ON_ANY_CREATURE_ATTACKS", "ON_ALLY_CREATURE_ATTACKS",
                    "ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER"},
}
PLANAR = {"creatureDies", "leavesBattlefield", "landEnters", "entersBattlefield"}
CONSTANT = re.compile(r"^\s{4}(\w+)\(\s*Triggers\.(\w+)\(", re.M)
CALL = re.compile(r"(addTrigger|new TriggeredAbility)\(\s*Triggers\.(\w+)\(")


def factories():
    found, plane_only = set(), set()
    for enum, wanted in WATCHES.items():
        text = (TRIGGER_DIR / f"{enum}.java").read_text()
        for constant, factory in CONSTANT.findall(text):
            if wanted is None or constant in wanted:
                found.add(factory)
            if constant in PLANE_ONLY.get(enum, ()):
                plane_only.add(factory)
    return found, plane_only


def arguments(text, start):
    """The top-level arguments of the call whose '(' is at `start`."""
    depth, quoted, current, out = 0, False, "", []
    for index in range(start, len(text)):
        char = text[index]
        if quoted:
            current += char
            if char == '"' and text[index - 1] != "\\":
                quoted = False
            continue
        if char == '"':
            quoted = True
            current += char
        elif char in "([{":
            depth += 1
            if depth > 1:
                current += char
        elif char in ")]}":
            depth -= 1
            if depth == 0:
                out.append(current.strip())
                return out
            current += char
        elif char == "," and depth == 1:
            out.append(current.strip())
            current = ""
        else:
            current += char
    return out


def is_plane(text):
    return "AbilityZones.COMMAND" in text or "Triggers.chaosEnsues" in text


def main():
    wanted, plane_only = factories()
    everything = "--all" in sys.argv[1:]
    rows = collections.defaultdict(list)
    names = set()
    for path in sorted(CARDS.rglob("*.java")):
        text = path.read_text()
        if "Triggers." not in text:
            continue
        plane = is_plane(text)
        per_factory = collections.defaultdict(list)
        for match in CALL.finditer(text):
            factory = match.group(2)
            if factory in wanted or plane and (factory in plane_only or factory in PLANAR):
                args = arguments(text, match.end(1))
                per_factory[factory].append(sum(1 for arg in args[1:] if not arg.startswith("TriggerOptions")))
        for factory, counts in per_factory.items():
            names.add(path.stem)
            if len(counts) > 1 or max(counts) > 1:
                rows[factory].append(f"{path.stem} {counts}")
    if everything:
        print("\n".join(sorted(names)))
        return
    for factory in sorted(rows):
        print(f"{factory}: {len(rows[factory])}")
        for row in rows[factory]:
            print(f"    {row}")
    print(f"{sum(len(found) for found in rows.values())} rows, {len(names)} cards name a factory")


if __name__ == "__main__":
    main()
