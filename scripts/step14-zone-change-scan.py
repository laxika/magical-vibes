#!/usr/bin/env python3
"""Lists every triggered ability of step 12a's zone-change and permanent-change family (an ability that
ZoneChangeWatch collects) with the trigger-only wrappers its effects carry, and the card's Oracle text from the
server's card-data cache (effect-slot-refactor.md step 14, run 3).

The family is every ZoneChangeTrigger or PermanentChangeTrigger built by a Triggers factory, except the source's own
dies / sacrificed / leaves / put into a graveyard / milled abilities (run 1), the creature-death family (run 2) and
"when this enters" (run 4). A line's `vars` column names the local variables the ability passes, whose wrappers a
textual scan can't see.

Usage: python3 scripts/step14-zone-change-scan.py [--all] > out.tsv
"""
import importlib.util
import os
import re
import sys

spec = importlib.util.spec_from_file_location("pilot", os.path.join(os.path.dirname(__file__), "step14-pilot-scan.py"))
pilot = importlib.util.module_from_spec(spec)
spec.loader.exec_module(pilot)

ABILITY = re.compile(r"\baddTrigger\(|\baddSimultaneousTrigger\(|new TriggeredAbility\(|new TriggerCondition\(")
FACTORY = re.compile(r"Triggers\.(\w+)\(((?:[^()]|\((?:[^()]|\([^()]*\))*\))*)\)")
# Factories whose THIS form run 1 converted (the source's own departure).
PILOT = {"dies", "sacrificed", "leavesBattlefield", "putIntoGraveyardFrom", "putIntoGraveyardFromAnywhere",
         "putIntoGraveyardWhileResolving", "milled"}
DEATH = {"dies", "creatureDies", "nontokenCreatureDies"}
# The dies triggers step 12a's ZoneChangeWatch keys, by filter and relation; the rest are run 2's.
ZONE_CHANGE_RELATIONS = {"YOU_OWN", "OPPONENT_OWNS", "ENCHANTED", "ANOTHER_YOU_OWN"}
ZONE_CHANGE_FILTERS = {"null", "Triggers.ARTIFACT", "Triggers.ENCHANTMENT", "Triggers.LAND", "Triggers.NONTOKEN",
                       "Triggers.AURA_OR_EQUIPMENT"}
OTHER = {"sacrificedOrDestroyed", "destroyed", "leavesBattlefieldWithoutDying", "putIntoGraveyardOrExileFromBattlefield",
         "putIntoGraveyardFromAnywhereButBattlefield", "putIntoGraveyardFromAnywhereBut", "mills", "exiledFrom",
         "leavesGraveyard", "putIntoHandFrom", "putIntoLibrary", "putIntoCommandZone", "enters", "tokensCreated",
         "entersFrom", "phasesOut", "phasesIn", "transforms", "transformsToBackFace", "transformsToFrontFace",
         "turnedFaceUp"}
WRAPPERS = ["OncePerTurnTriggerEffect", "OnceOnlyTriggerEffect", "OncePerTurnPerCreatureTriggerEffect",
            "new ConditionalEffect(", "ConditionalEffect.unless", "ConditionalEffect.atTriggerTime",
            "TriggeringPermanentConditionalEffect", "TriggeringCardConditionalEffect",
            "TriggeringPermanentControllerConditionalEffect", "ConditionalTriggeringPermanentEffect",
            "TriggeringPermanentEntryExclusionEffect", "EnterCreatureConditionalEffect",
            "TriggeringArtifactControllerConditionalEffect", "EnchantedPermanentLeavesConditionalEffect", "TriggerMode.",
            "Batched", "OneOrMore"]


def split(text):
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
    return [a.strip() for a in args]


def in_family(name, args):
    relation = args[-1].replace("ObjectRelation.", "") if args and args[-1] else ""
    if name in DEATH:
        if relation == "THIS":
            return False
        if relation in ZONE_CHANGE_RELATIONS:
            return True
        return name == "dies" and len(args) == 2 and args[0] in ZONE_CHANGE_FILTERS
    if name in PILOT:
        return relation != "THIS"
    if name == "enters":
        return relation != "THIS"
    return name in OTHER


def family(stmt):
    """The first factory call of the ability that belongs to the family, or None."""
    for m in FACTORY.finditer(stmt):
        if in_family(m.group(1), split(m.group(2))):
            return m
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
                m = family(stmt)
                if not m:
                    continue
                found = [w for w in WRAPPERS if w in stmt]
                tail = stmt[m.end():]
                variables = sorted(set(v for v in re.findall(r"[,(]\s*([a-z]\w*)\s*(?=[,)])", tail)
                                       if v not in ("null", "true", "false")))
                if not found and not variables and not show_all:
                    continue
                text = pilot.oracle(pilot.REG.findall(src)) or "NO-ORACLE"
                print("\t".join([path, ",".join(found), ",".join(variables), re.sub(r"\s+", " ", stmt),
                                 text.replace("\n", " | ")]))


if __name__ == "__main__":
    sys.exit(main())
