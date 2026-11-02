#!/usr/bin/env python3
"""For the 3a part of effect-slot-refactor.md step 14 run 3: maps each ability that step14-zone-change-scan.py lists to
the ZoneChangeWatch constant whose key its trigger is, and lists per watch the wrappers and their wrapped effects.

Usage: python3 scripts/step14-zone-change-scan.py | python3 scripts/step14-zone-change-watches.py
"""
import collections
import csv
import re
import sys

WATCH = "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/trigger/ZoneChangeWatch.java"


def norm(text):
    return re.sub(r"\s+", "", text).replace("ObjectRelation.", "").replace("Triggers.", "").replace("Zone.", "")


def keys():
    src = open(WATCH).read()
    body = src[src.index("public enum ZoneChangeWatch {"):src.index("public record Key")]
    out = {}
    for m in re.finditer(r"\n    ([A-Z_]+)\(", body):
        start, depth, i = m.end(), 1, m.end()
        while depth:
            depth += (body[i] == "(") - (body[i] == ")")
            i += 1
        inner = body[start:i - 1]
        for k in re.finditer(r"(battlefield|in)\(((?:[^()]|\((?:[^()]|\((?:[^()]|\([^()]*\))*\))*\))*)\)", inner):
            args = k.group(2)
            multi = "ONE_OR_MORE" if "ONE_OR_MORE" in args else "EACH"
            zone = "BATTLEFIELD"
            trig = args
            if k.group(1) == "in":
                zone, trig = args.split(",", 1)
                zone = zone.strip().replace("Zone.", "")
            trig = re.sub(r",\s*TriggerMultiplicity\.\w+\s*$", "", trig.strip())
            out[(norm(trig), zone, multi)] = m.group(1)
    return out


def main():
    table = keys()
    rows = list(csv.reader(sys.stdin, delimiter="\t"))[1:]
    by_watch = collections.defaultdict(list)
    for path, wrappers, variables, stmt, oracle in rows:
        if re.search(r"Triggers\.(enters|entersFrom|tokensCreated)\(", stmt):
            continue
        m = re.search(r"(Triggers\.\w+\((?:[^()]|\((?:[^()]|\([^()]*\))*\))*\)(?:\.\w+\([^()]*\))*)", stmt)
        trig = norm(m.group(1))
        zone = "BATTLEFIELD"
        z = re.search(r"AbilityZones\.(\w+)", stmt)
        if z:
            zone = {"GRAVEYARD": "GRAVEYARD", "EXILE": "EXILE"}.get(z.group(1), z.group(1))
        multi = "ONE_OR_MORE" if "ONE_OR_MORE" in stmt else "EACH"
        watch = table.get((trig, zone, multi)) or table.get((trig, zone, "EACH")) or "?" + trig
        by_watch[watch].append((path.split("/")[-1], wrappers, variables, stmt))
    for watch in sorted(by_watch):
        print("== %s (%d)" % (watch, len(by_watch[watch])))
        for name, wrappers, variables, stmt in by_watch[watch]:
            tail = stmt[stmt.index(")") + 1:] if ")" in stmt else stmt
            print("   %-38s %-60s %s" % (name, wrappers + (" vars=" + variables if variables else ""),
                                       re.sub(r"\s+", " ", stmt)[-170:]))


if __name__ == "__main__":
    sys.exit(main())
