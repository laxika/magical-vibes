#!/usr/bin/env python3
"""Step 15, run 17z15 (read-only): the card test classes of the cards on the collector routes the run changed.

A card is listed when it writes a "you may" beside an attack, combat-damage, damage-to-a-player or transform trigger,
chooses graveyard cards on a combat-damage trigger, or constructs an effect whose handler the run changed
(`GrantColorUntilEndOfTurnEffect`, `SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect`), or watches
"an attacking creature's ability triggers".

Usage: python3 scripts/step15-run17z15-test-list.py [--gradle]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TRIGGER = re.compile(r"Triggers\.(attacks|creatureYouControlAttacks|dealsCombatDamageToPlayer|dealsDamageToPlayer"
                     r"|dealsCombatDamage\w*|transformsTo\w+|transforms)\(")
GRAVEYARD = re.compile(r"ExileCardsFromGraveyardEffect|ExileGraveyardCardsEffect")
ALWAYS = re.compile(r"GrantColorUntilEndOfTurnEffect|SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect"
                    r"|attackingCreatureYouControlAbilityTriggers")

by_letter = {}
for path in sorted((ROOT / "magical-vibes-card").glob("*/src/main/java/**/*.java")):
    text = path.read_text()
    listed = ALWAYS.search(text) or (TRIGGER.search(text) and ("MayEffect(" in text or GRAVEYARD.search(text)))
    if not listed:
        continue
    letter = path.parts[path.parts.index("magical-vibes-card") + 1]
    name = path.stem + "Test"
    test = ROOT / "magical-vibes-card-test" / letter / "src/test/java/com/github/laxika/magicalvibes/cards" / letter
    if (test / (name + ".java")).exists():
        by_letter.setdefault(letter, []).append(name)

if "--gradle" in sys.argv:
    print(" ".join(f":magical-vibes-card-test:{letter}:test " + " ".join(f"--tests *.{name}" for name in names)
                   for letter, names in sorted(by_letter.items())))
else:
    for letter, names in sorted(by_letter.items()):
        for name in names:
            print(letter, name)
    print(sum(len(names) for names in by_letter.values()), "test classes in", len(by_letter), "modules",
          file=sys.stderr)
