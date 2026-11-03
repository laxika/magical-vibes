#!/usr/bin/env python3
"""Lists every "when this enters" ability (Triggers.enters(ObjectRelation.THIS) or entersFrom(…, THIS), which step
12b's EtbTriggerService collects) whose effects carry a trigger-only wrapper or are passed as a local variable, with
the wrappers, the condition of each ConditionalEffect and the card's Oracle text (effect-slot-refactor.md step 14,
run 4).

Usage: python3 scripts/step14-own-enters-scan.py [--all] [--summary] > out.tsv
"""
import collections
import importlib.util
import os
import re
import sys

spec = importlib.util.spec_from_file_location("pilot", os.path.join(os.path.dirname(__file__), "step14-pilot-scan.py"))
pilot = importlib.util.module_from_spec(spec)
spec.loader.exec_module(pilot)

ABILITY = re.compile(r"\baddTrigger\(|\baddSimultaneousTrigger\(|new TriggeredAbility\(|\baddTriggeredAbility\(")
OWN = re.compile(r"Triggers\.(?:enters\(ObjectRelation\.THIS\)|enters\([^()]*,\s*ObjectRelation\.THIS\)|"
                 r"entersFrom\([^()]*ObjectRelation\.THIS\))")
WRAPPERS = ["OncePerTurnTriggerEffect", "OnceOnlyTriggerEffect", "OncePerTurnPerCreatureTriggerEffect",
            "new ConditionalEffect(", "ConditionalEffect.unless", "ConditionalEffect.atTriggerTime",
            "ConditionalEffect.", "TriggeringPermanentConditionalEffect", "TriggeringCardConditionalEffect",
            "TriggeringPermanentEntryExclusionEffect", "TriggerMode.", "Batched", "OneOrMore", "TributeNotPaidEffect",
            "SacrificeSelfIfEvokedEffect", "LoseGameIfNotCastFromHandEffect", "ChooseOneAtTriggerTimeEffect"]
COND = re.compile(r"(?:new ConditionalEffect\(|ConditionalEffect\.(\w+)\()\s*(new [\w.]+|[A-Z][\w.]*)")


def statements(src):
    for m in ABILITY.finditer(src):
        i, depth = m.end(), 1
        while depth:
            depth += (src[i] == "(") - (src[i] == ")")
            i += 1
        yield src[m.start():i]


def main():
    show_all = "--all" in sys.argv
    summary = "--summary" in sys.argv
    counts = collections.Counter()
    conds = collections.Counter()
    files_with = set()
    rows = []
    for root, _, files in os.walk("magical-vibes-card"):
        if "/build/" in root or "/test/" in root:
            continue
        for name in sorted(files):
            if not name.endswith(".java"):
                continue
            path = os.path.join(root, name)
            src = open(path).read()
            for stmt in statements(src):
                m = OWN.search(stmt)
                if not m:
                    continue
                found = [w for w in WRAPPERS if w in stmt]
                tail = stmt[m.end():]
                variables = sorted(set(v for v in re.findall(r"[,(]\s*([a-z]\w*)\s*(?=[,)])", tail)
                                       if v not in ("null", "true", "false")))
                if not found and not variables and not show_all:
                    continue
                for w in found:
                    counts[w] += 1
                for c in COND.finditer(stmt):
                    conds[(c.group(1) or "new") + " " + c.group(2)] += 1
                if variables:
                    counts["vars"] += 1
                files_with.add(path)
                if not summary:
                    text = pilot.oracle(pilot.REG.findall(src)) or "NO-ORACLE"
                    rows.append("\t".join([path, ",".join(found), ",".join(variables), re.sub(r"\s+", " ", stmt),
                                           text.replace("\n", " | ")]))
    if summary:
        print("files", len(files_with))
        for k, v in counts.most_common():
            print("%6d %s" % (v, k))
        print()
        for k, v in conds.most_common():
            print("%6d %s" % (v, k))
    else:
        print("file\twrappers\tvars\tability\toracle")
        print("\n".join(rows))


if __name__ == "__main__":
    sys.exit(main())
