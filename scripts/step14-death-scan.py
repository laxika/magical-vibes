#!/usr/bin/env python3
"""Lists every "whenever [another object] dies" ability of step 11's creature-death family (a dies trigger whose
relation isn't THIS, collected through CreatureDeathWatch) with the trigger-only wrappers its effects carry, and the
card's Oracle text from the server's card-data cache (effect-slot-refactor.md step 14, run 2).

A line's `vars` column names the local variables the ability passes, whose wrappers a textual scan can't see.

Usage: python3 scripts/step14-death-scan.py [--all] > out.tsv
"""
import importlib.util
import os
import re
import sys

spec = importlib.util.spec_from_file_location("pilot", os.path.join(os.path.dirname(__file__), "step14-pilot-scan.py"))
pilot = importlib.util.module_from_spec(spec)
spec.loader.exec_module(pilot)

ABILITY = re.compile(r"\baddTrigger\(|\baddSimultaneousTrigger\(|new TriggeredAbility\(|new TriggerCondition\(")
DIES = re.compile(r"Triggers\.(dies|creatureDies|nontokenCreatureDies)\(((?:[^()]|\((?:[^()]|\([^()]*\))*\))*)\)")
# The dies triggers step 12a's ZoneChangeWatch keys (run 3's family), by filter and relation.
ZONE_CHANGE_RELATIONS = {"YOU_OWN", "OPPONENT_OWNS", "ENCHANTED", "ANOTHER_YOU_OWN"}
ZONE_CHANGE_FILTERS = {"null", "Triggers.ARTIFACT", "Triggers.ENCHANTMENT", "Triggers.LAND", "Triggers.NONTOKEN",
                       "Triggers.AURA_OR_EQUIPMENT"}
WRAPPERS = ["OncePerTurnTriggerEffect", "OnceOnlyTriggerEffect", "OncePerTurnPerCreatureTriggerEffect",
            "new ConditionalEffect(", "ConditionalEffect.unless", "ConditionalEffect.atTriggerTime",
            "TriggeringPermanentConditionalEffect", "TriggeringCardConditionalEffect",
            "TriggeringPermanentControllerConditionalEffect", "ConditionalTriggeringPermanentEffect",
            "TriggeringPermanentEntryExclusionEffect", "TriggerMode.", "Batched", "OneOrMore"]


def family(stmt):
    """Whether the ability has a dies trigger of the creature-death family."""
    for m in DIES.finditer(stmt):
        args = [a.strip() for a in pilot_split(m.group(2))]
        relation = args[-1].replace("ObjectRelation.", "")
        if relation == "THIS":
            continue
        if relation in ZONE_CHANGE_RELATIONS:
            continue
        if m.group(1) == "dies" and len(args) == 2 and args[0] in ZONE_CHANGE_FILTERS:
            continue
        if m.group(1) == "dies" and len(args) == 1:
            continue
        return True
    return False


def pilot_split(text):
    args, depth, start = [], 0, 0
    for i, c in enumerate(text):
        if c in "([{":
            depth += 1
        elif c in ")]}":
            depth -= 1
        elif c == "," and depth == 0:
            args.append(text[start:i])
            start = i + 1
    args.append(text[start:])
    return args


def statements(src):
    for m in ABILITY.finditer(src):
        i, depth = m.end(), 1
        while depth:
            depth += (src[i] == "(") - (src[i] == ")")
            i += 1
        yield src[m.start():i]


def main():
    show_all = "--all" in sys.argv
    print("file\twrappers\tvars\tability\toracle")
    for root, _, files in os.walk("magical-vibes-card"):
        if "/build/" in root or "/test/" in root:
            continue
        for name in sorted(files):
            if not name.endswith(".java"):
                continue
            path = os.path.join(root, name)
            src = open(path).read()
            for stmt in statements(src):
                if not family(stmt):
                    continue
                found = [w for w in WRAPPERS if w in stmt]
                tail = stmt[DIES.search(stmt).end():]
                variables = sorted(set(v for v in re.findall(r"[,(]\s*([a-z]\w*)\s*(?=[,)])", tail)
                                       if v not in ("null", "true", "false")))
                if not found and not variables and not show_all:
                    continue
                text = pilot.oracle(pilot.REG.findall(src)) or "NO-ORACLE"
                print("\t".join([path, ",".join(found), ",".join(variables), re.sub(r"\s+", " ", stmt),
                                 text.replace("\n", " | ")]))


if __name__ == "__main__":
    sys.exit(main())
