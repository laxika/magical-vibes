#!/usr/bin/env python3
"""Lists every triggered ability of the step 10 pilot (the source's own zone changes: dies, sacrificed, leaves the
battlefield, put into a graveyard, milled, all with ObjectRelation.THIS) whose effects carry a trigger-only wrapper,
with the card's Oracle text from the server's card-data cache (effect-slot-refactor.md step 14, run 1).

Usage: python3 scripts/step14-pilot-scan.py [--all] > out.tsv
"""
import json
import os
import re
import sys

ABILITY = re.compile(r"\baddTrigger\(|\baddSimultaneousTrigger\(|new TriggeredAbility\(")
PILOT = re.compile(r"Triggers\.(dies|sacrificed|leavesBattlefield|putIntoGraveyardFrom|putIntoGraveyardFromAnywhere|"
                   r"putIntoGraveyardWhileResolving|milled)\((?:[^()]|\([^()]*\))*?\bTHIS\)")
WRAPPERS = ["OncePerTurnTriggerEffect", "OnceOnlyTriggerEffect", "new ConditionalEffect(", "ConditionalEffect.unless",
            "ConditionalEffect.atTriggerTime", "TriggeringPermanentConditionalEffect", "TriggeringCardConditionalEffect",
            "DyingPermanentWas", "SacrificeOnlyEffect", "TriggerMode.", "IfDyingSourceHad"]
REG = re.compile(r'@CardRegistration\(set\s*=\s*"([^"]+)",\s*collectorNumber\s*=\s*"([^"]+)"')
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
            faces = [p.get("front"), p.get("back")] + list(p.get("splitHalves") or [])
            return " // ".join(f["text"] for f in faces if f and f.get("text"))
    return None


def statements(src):
    for m in ABILITY.finditer(src):
        i, depth = m.end(), 1
        while depth:
            depth += (src[i] == "(") - (src[i] == ")")
            i += 1
        yield src[m.start():i]


def main():
    show_all = "--all" in sys.argv
    print("file\twrappers\tability\toracle")
    for root, _, files in os.walk("magical-vibes-card"):
        if "/build/" in root or "/test/" in root:
            continue
        for name in sorted(files):
            if not name.endswith(".java"):
                continue
            path = os.path.join(root, name)
            src = open(path).read()
            for stmt in statements(src):
                if not PILOT.search(stmt):
                    continue
                found = [w for w in WRAPPERS if w in stmt]
                if not found and not show_all:
                    continue
                text = oracle(REG.findall(src)) or "NO-ORACLE"
                print("\t".join([path, ",".join(found), re.sub(r"\s+", " ", stmt), text.replace("\n", " | ")]))


if __name__ == "__main__":
    sys.exit(main())
