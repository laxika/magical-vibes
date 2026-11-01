#!/usr/bin/env python3
"""Lists every card class that registers a creature-death slot (effect-slot-refactor.md step 11).

For each card: its slots, whether it also has a "when this dies" trigger, and the Oracle sentences
about a creature dying, read from the server's card-data cache. Prints a TSV on stdout.
Usage: python3 scripts/step11-death-scan.py > out.tsv
"""
import json
import os
import re
import sys

SLOTS = ["ON_ENCHANTED_PLAYER_NONTOKEN_CREATURE_DIES", "ON_HAUNTED_CREATURE_DIES", "ON_ALLY_CREATURE_DIES",
         "ON_ALLY_ARTIFACT_OR_CREATURE_DIES", "ON_ALLY_CREATURE_OR_PLANESWALKER_DIES", "ON_DAMAGED_CREATURE_DIES",
         "ON_ANY_CREATURE_DIES", "ON_ALLY_NONTOKEN_CREATURE_DIES", "ON_ANY_NONTOKEN_CREATURE_DIES",
         "ON_EQUIPPED_CREATURE_DIES", "ON_OPPONENT_CREATURE_DIES", "GRAVEYARD_ON_ALLY_CREATURE_DIES",
         "GRAVEYARD_ON_OPPONENT_CREATURE_DIES"]
SLOT_RE = re.compile(r"EffectSlot\.(" + "|".join(SLOTS) + r")\b")
REG_RE = re.compile(r'@CardRegistration\(set\s*=\s*"([^"]+)",\s*collectorNumber\s*=\s*"([^"]+)"')
CACHE = "card-data-cache"
_sets = {}


def load_set(code):
    code = code.lower()
    if code not in _sets:
        printings = {}
        for folder in (CACHE, os.path.join(CACHE, "only-scryfall"), os.path.join(CACHE, "only-mtgjson")):
            path = os.path.join(folder, "set-%s.json" % code)
            if os.path.exists(path):
                for p in json.load(open(path)).get("printings", []):
                    printings.setdefault(p["collectorNumber"], p)
        _sets[code] = printings
    return _sets[code]


def oracle(regs):
    for code, number in regs:
        p = load_set(code).get(number)
        if p:
            faces = [p.get("front")] + [p.get("back")] + [h for h in p.get("splitHalves") or []]
            return " // ".join(f["text"] for f in faces if f and f.get("text")), p["front"]["typeLine"]
    return None, None


def main():
    print("file\tslots\tselfDies\ttype\tsentences")
    for root, _, files in os.walk("magical-vibes-card"):
        for name in sorted(files):
            if not name.endswith(".java"):
                continue
            path = os.path.join(root, name)
            src = open(path).read()
            slots = SLOT_RE.findall(src)
            if not slots:
                continue
            regs = REG_RE.findall(src)
            text, type_line = oracle(regs)
            self_dies = "Triggers.dies(ObjectRelation.THIS)" in src or "dies(THIS)" in src
            sentences = []
            for sentence in re.split(r"(?<=[.\n])", text or ""):
                if re.search(r"\bdies?\b|put into a graveyard|haunts", sentence):
                    sentences.append(sentence.strip())
            print("\t".join([path, ",".join(slots), str(self_dies), type_line or "?",
                             " | ".join(sentences) if text is not None else "NO-ORACLE"]))


if __name__ == "__main__":
    sys.exit(main())
