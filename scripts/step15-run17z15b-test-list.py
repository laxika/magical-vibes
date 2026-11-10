#!/usr/bin/env python3
"""Step 15, run 17z15b (read-only): the card test classes of the cards on the routes and watches the run changed.

A card is listed when it writes
- a "deals (combat) damage to a player / an opponent" trigger (its own, an Aura's or Equipment's, or "a creature you
  control") beside a mode choice, a graveyard card or a target: the routes that carry the collector's record now;
- "one or more creatures die", an "attacks" or "attacks and isn't blocked" trigger beside a graveyard card, an own
  zone-change trigger beside a mode choice or a paid graveyard target;
- "another creature / permanent you control leaves the battlefield" or "enters from a graveyard";
- a cast trigger beside a graveyard card (the choice's X is the spell's mana value);
- "you put +1/+1 counters on a permanent / creature" or "you put -1/-1 counters on this";
- or is one of the five cards `step15-run17z15b-cards.py` converts, or constructs an effect the converted cards use.

Usage: python3 scripts/step15-run17z15b-test-list.py [--gradle]
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DAMAGE = re.compile(r"Triggers\.(dealsCombatDamage\w*|dealsDamageTo(Player|Opponent)\w*"
                    r"|creatureYouControlDealsCombatDamage\w*|equippedCreatureDealsCombatDamage\w*)\(")
CHOICE = re.compile(r"ChooseOne|Graveyard|Target|target\(")
GRAVEYARD_TRIGGER = re.compile(r"Triggers\.(attacks|attacksUnblocked|youCastSpell|opponentCastsSpell"
                               r"|playerCastsSpell)\(")
OWN_ZONE_CHANGE = re.compile(r"Triggers\.\w+\((\w+\.\w+, )?ObjectRelation\.THIS\)")
ONE_OR_MORE_DEATHS = re.compile(r"Triggers\.dies\([^;]*?TriggerOptions\.ONE_OR_MORE", re.S)

by_letter = {}
for path in sorted((ROOT / "magical-vibes-card").glob("*/src/main/java/**/*.java")):
    text = path.read_text()
    listed = (ONE_OR_MORE_DEATHS.search(text)
              or re.search(r"leavesBattlefield\((Triggers\.CREATURE|null), ObjectRelation\.ANOTHER_YOU_CONTROL\)"
                           r"|entersFrom\(Zone\.GRAVEYARD, null, ObjectRelation\.THIS\)"
                           r"|youPutPlusOneCountersOn\w+\(|youPutMinusOneCountersOnThis\("
                           r"|becomesUnattachedFromPermanent\(|StackEntrySnowManaOfItsColorSpentPredicate"
                           r"|GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect"
                           r"|SacrificeTriggeringPermanentEffect|DestroyReferencedPermanentEffect", text)
              or (DAMAGE.search(text) and CHOICE.search(text))
              or (GRAVEYARD_TRIGGER.search(text) and "Graveyard" in text)
              or (OWN_ZONE_CHANGE.search(text) and ("ChooseOne" in text
                                                    or ("MayPayManaEffect" in text and "Graveyard" in text))))
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
