#!/usr/bin/env python3
"""Step 15, run 17z15d (read-only): the card test classes of the cards on the collectors and marks the run changed.

A card is listed when it writes
- an attack, block, becomes-blocked, unblocked-attack or combat-damage / damage-to-a-player trigger (the combat
  collectors' ability sources, the block-step mode choice, the per-ability "one or more" and limit marks);
- any `TriggerLimit` (every limit is kept under its ability's key now, "do this only once each turn" included);
- a tap trigger, a crew or saddle trigger, backup, "an opponent loses life for the first time during each of their
  turns" or "becomes unattached";
- a mode chosen as a triggered ability goes on the stack (`ChooseOneAtTriggerTimeEffect`), whose targeted modes carry
  the collector's record, or a triggered ability beside two or more declared target groups (the group-by-group walk);
- an ability granted for a duration (`Grant…UntilEndOfTurn…` with a `TriggeredAbility`), which more collectors read;
- or is one of the six cards `step15-run17z15d-cards.py` converts, Arm with Aether or Calix, Guided by Fate.

Usage: python3 scripts/step15-run17z15d-test-list.py [--gradle]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
COMBAT = re.compile(r"Triggers\.(attacks\w*|youAttack\w*|blocks\w*|becomesBlocked\w*|\w*Unblocked\w*"
                    r"|dealsCombatDamage\w*|dealsDamageTo(Player|Opponent)\w*|\w*DealsCombatDamage\w*"
                    r"|\w*BecomesBlocked\w*|\w*BecomeBlocked\w*|creatureYouControlAttacks\w*|creatureAttacks\w*"
                    r"|\w*BecomesTapped\w*|\w*Crewed\w*|\w*Saddled\w*|crews\w*|saddles\w*"
                    r"|\w*BackupAbility\w*|opponentLosesLifeForFirstTime\w*|becomesUnattached\w*)\(")
OTHER = re.compile(r"TriggerLimit\.|ChooseOneAtTriggerTimeEffect|Keyword\.BACKUP|addBackup"
                   r"|Grant\w*Until\w*Effect\(\s*new TriggeredAbility")
NAMED = {"AureliaTheWarleader", "GodoBanditWarlord", "ScourgeOfTheThrone", "FearOfMissingOut", "MirrorShieldHoplite",
         "KomainuBattleArmor", "ArmWithAether", "CalixGuidedByFate"}

by_letter = {}
for path in sorted((ROOT / "magical-vibes-card").glob("*/src/main/java/**/*.java")):
    text = path.read_text()
    walked = "addTrigger(" in text and len(re.findall(r"\btarget\w*\(", text)) >= 2
    if not (path.stem in NAMED or COMBAT.search(text) or OTHER.search(text) or walked):
        continue
    letter = path.parts[path.parts.index("magical-vibes-card") + 1]
    name = path.stem + "Test"
    test_letter = path.stem[0].lower()
    test = (ROOT / "magical-vibes-card-test" / test_letter / "src/test/java/com/github/laxika/magicalvibes/cards"
            / test_letter)
    if (test / (name + ".java")).exists():
        by_letter.setdefault(test_letter, []).append(name)

if "--gradle" in sys.argv:
    print(" ".join(f":magical-vibes-card-test:{letter}:test " + " ".join(f"--tests *.{name}" for name in names)
                   for letter, names in sorted(by_letter.items())))
else:
    for letter, names in sorted(by_letter.items()):
        for name in names:
            print(letter, name)
    print(sum(len(names) for names in by_letter.values()), "test classes in", len(by_letter), "modules",
          file=sys.stderr)
