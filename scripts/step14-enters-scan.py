#!/usr/bin/env python3
"""For effect-slot-refactor.md step 14 run 3b: lists every "whenever [an object] enters" ability (an enters,
entersFrom or tokensCreated trigger other than "when this enters", which step 12a part 2's entering watches collect)
whose effects carry a trigger-only wrapper or are passed as a local variable, grouped by the ZoneChangeWatch whose key
its trigger is, with the wrappers, the outermost wrapped effect's class and the card's Oracle text.

Usage: python3 scripts/step14-enters-scan.py [--oracle] > out.txt
"""
import collections
import importlib.util
import os
import re
import sys

_here = os.path.dirname(__file__)


def _load(name, file):
    spec = importlib.util.spec_from_file_location(name, os.path.join(_here, file))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


scan = _load("zone_change_scan", "step14-zone-change-scan.py")
watches = _load("zone_change_watches", "step14-zone-change-watches.py")
pilot = scan.pilot

ENTERING = re.compile(r"Triggers\.(enters|entersFrom|tokensCreated)\(")
WRAPPER = re.compile(r"new (TriggeringCardConditionalEffect|TriggeringPermanentConditionalEffect|"
                     r"OncePerTurnTriggerEffect|OnceOnlyTriggerEffect|ConditionalEffect|"
                     r"Entering(?:Creature|Land)\w+ConditionalEffect)\(|TriggeringCardConditionalEffect\.gatingSource\(|"
                     r"OncePerTurnTriggerEffect\.\w+\(|ConditionalEffect\.\w+\(")


def entering_abilities(src):
    for stmt in scan.statements(src):
        m = scan.family(stmt)
        if m and ENTERING.match(m.group(0)):
            yield stmt, m


def main():
    table = watches.keys()
    show_oracle = "--oracle" in sys.argv
    by_watch = collections.defaultdict(list)
    for root, _, files in os.walk("magical-vibes-card"):
        if "/build/" in root or "/test/" in root:
            continue
        for name in sorted(files):
            if not name.endswith(".java"):
                continue
            path = os.path.join(root, name)
            src = open(path).read()
            for stmt, m in entering_abilities(src):
                found = [w.group(0) for w in WRAPPER.finditer(stmt)]
                tail = stmt[m.end():]
                variables = sorted(set(v for v in re.findall(r"[,(]\s*([a-z]\w*)\s*(?=[,)])", tail)
                                       if v not in ("null", "true", "false")))
                if not found and not variables:
                    continue
                trig = re.search(r"(Triggers\.\w+\((?:[^()]|\((?:[^()]|\([^()]*\))*\))*\)(?:\.\w+\([^()]*\))*)", stmt)
                key = watches.norm(trig.group(1))
                zone = "BATTLEFIELD"
                z = re.search(r"AbilityZones\.(\w+)", stmt)
                if z:
                    zone = z.group(1)
                multi = "ONE_OR_MORE" if "ONE_OR_MORE" in stmt else "EACH"
                watch = table.get((key, zone, multi)) or table.get((key, zone, "EACH")) or "?" + key
                text = pilot.oracle(pilot.REG.findall(src)) if show_oracle else ""
                by_watch[watch].append((name, " ".join(found), ",".join(variables), re.sub(r"\s+", " ", stmt),
                                        (text or "").replace("\n", " | ")))
    for watch in sorted(by_watch):
        print("== %s (%d)" % (watch, len(by_watch[watch])))
        for name, found, variables, stmt, text in by_watch[watch]:
            print("   %-36s %s%s" % (name, found, " vars=" + variables if variables else ""))
            print("        " + stmt[:400])
            if text:
                print("        ORACLE: " + text[:400])


if __name__ == "__main__":
    sys.exit(main())
