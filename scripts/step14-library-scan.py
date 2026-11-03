#!/usr/bin/env python3
"""Lists every library, draw and discard ability (a Triggers factory of scripts/step13-library-slots.tsv, which the LibraryWatch collectors
read) whose effects carry a trigger-only wrapper or are passed as a local variable, with the wrappers, the condition
of each ConditionalEffect and the card's Oracle text (effect-slot-refactor.md step 14, run 11).

Usage: python3 scripts/step14-target-scan.py [--all] [--summary] > out.tsv
"""
import collections
import importlib.util
import os
import re
import sys

HERE = os.path.dirname(__file__)
spec = importlib.util.spec_from_file_location("pilot", os.path.join(HERE, "step14-pilot-scan.py"))
pilot = importlib.util.module_from_spec(spec)
spec.loader.exec_module(pilot)


def factories():
    names = set()
    for line in open(os.path.join(HERE, "step13-library-slots.tsv")):
        if line.startswith("#") or not line.strip():
            continue
        names.add(line.split("\t")[1])
    return sorted(names, key=len, reverse=True)


ABILITY = re.compile(r"\baddTrigger\(|\baddSimultaneousTrigger\(|new TriggeredAbility\(|\baddTriggeredAbility\(")
FAMILY = re.compile(r"Triggers\.(" + "|".join(factories()) + r")\(")
WRAPPERS = ["OncePerTurnTriggerEffect", "OnceOnlyTriggerEffect", "OncePerTurnPerCreatureTriggerEffect",
            "new ConditionalEffect(", "ConditionalEffect.unless", "ConditionalEffect.atTriggerTime",
            "ConditionalEffect.", "TriggeringPermanentConditionalEffect", "TriggeringCardConditionalEffect",
            "Triggering", "TriggerEffect", "Batched", "OneOrMore", "BlockPairConditionalEffect",
            "TriggerMode.", "TriggerOptions"]
COND = re.compile(r"(?:new ConditionalEffect\(|ConditionalEffect\.(\w+)\()\s*(new [\w.]+|[A-Z][\w.]*)")
CLASSES = re.compile(r"new (\w+(?:TriggerEffect|ConditionalEffect|Conditional\w*Effect))\(|(\w+TriggerEffect)\.\w+\(")
MODES = re.compile(r"TriggerMode\.(\w+)")


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
    classes = collections.Counter()
    modes = collections.Counter()
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
                m = FAMILY.search(stmt)
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
                for c in CLASSES.finditer(stmt):
                    classes[c.group(1) or c.group(2)] += 1
                for c in MODES.finditer(stmt):
                    modes[c.group(1)] += 1
                if variables:
                    counts["vars"] += 1
                files_with.add(path)
                if not summary:
                    text = pilot.oracle(pilot.REG.findall(src)) or "NO-ORACLE"
                    rows.append("\t".join([path, m.group(1), ",".join(found), ",".join(variables),
                                           re.sub(r"\s+", " ", stmt), text.replace("\n", " | ")]))
    if summary:
        print("files", len(files_with))
        for k, v in counts.most_common():
            print("%6d %s" % (v, k))
        print()
        for k, v in conds.most_common():
            print("%6d %s" % (v, k))
        print()
        for k, v in classes.most_common():
            print("%6d %s" % (v, k))
        print()
        for k, v in modes.most_common():
            print("%6d mode %s" % (v, k))
    else:
        print("file\ttrigger\twrappers\tvars\tability\toracle")
        print("\n".join(rows))


if __name__ == "__main__":
    sys.exit(main())
