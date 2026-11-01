#!/usr/bin/env python3
"""Classifies every creature-death slot registration by its Oracle wording (effect-slot-refactor.md step 11):
"a creature" includes the source, "another creature" doesn't, "this or another" always does. Reads the TSV that
step11-death-scan.py prints and writes card<TAB>slot<TAB>relation<TAB>sentence, with relation "?" where the wording
needs a human. Usage: python3 scripts/step11-death-classify.py scan.tsv > classification.tsv
"""
import csv
import re
import sys

FIXED = {
    "ON_OPPONENT_CREATURE_DIES": "OPPONENT_CONTROLS",
    "GRAVEYARD_ON_OPPONENT_CREATURE_DIES": "OPPONENT_CONTROLS",
    "ON_EQUIPPED_CREATURE_DIES": "EQUIPPED",
    "ON_HAUNTED_CREATURE_DIES": "HAUNTED",
    "ON_ENCHANTED_PLAYER_NONTOKEN_CREATURE_DIES": "ENCHANTED_PLAYER_CONTROLS",
}
YOU_CONTROL_SLOTS = {"ON_ALLY_CREATURE_DIES", "ON_ALLY_NONTOKEN_CREATURE_DIES",
                     "ON_ALLY_CREATURE_OR_PLANESWALKER_DIES", "ON_ALLY_ARTIFACT_OR_CREATURE_DIES",
                     "GRAVEYARD_ON_ALLY_CREATURE_DIES"}


def relation(slot, sentences):
    if slot in FIXED:
        return FIXED[slot], ""
    text = " | ".join(s for s in sentences if re.search(r"\bdies?\b|graveyard from the battlefield", s))
    lower = text.lower()
    you = slot in YOU_CONTROL_SLOTS
    if slot == "ON_DAMAGED_CREATURE_DIES":
        if re.search(r"dealt damage by (equipped|enchanted) creature", lower):
            return "DAMAGED_BY_ATTACHED", text
        return "DAMAGED_BY_THIS", text
    if re.search(r"\bor another\b", lower):
        return ("THIS_OR_ANOTHER_YOU_CONTROL" if you else "THIS_OR_ANOTHER"), text
    if re.search(r"\banother\b|\bother\b", lower):
        return ("ANOTHER_YOU_CONTROL" if you else "ANOTHER"), text
    if re.search(r"whenever (a|an|one or more)\b", lower) or re.search(r"when (a|an)\b", lower):
        return ("YOU_CONTROL" if you else "ANY"), text
    return "?", text


def main():
    rows = list(csv.reader(open(sys.argv[1]), delimiter="\t"))[1:]
    for path, slots, _, _, sentences in rows:
        card = path.rsplit("/", 1)[1][:-5]
        for slot in dict.fromkeys(slots.split(",")):
            rel, text = relation(slot, sentences.split(" | "))
            print("\t".join([card, slot, rel, text]))


if __name__ == "__main__":
    main()
