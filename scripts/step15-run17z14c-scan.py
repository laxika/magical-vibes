#!/usr/bin/env python3
"""Step 15, run 17z14c: the trigger collectors' calls that read a permanent's abilities of one watch as one flat effect
list (rows 17z14c2, 17z14d to 17z14g).

One triggered ability is one stack object of its effects (CR 603.3). A collector that reads `*AbilityReader.printedEffects`,
`effects(<several abilities>, ...)`, `readPrinted`, `read(<several abilities>, ...)`, `temporaryEffects`,
`persistentEffects`, `grantedToPermanentEffects`, `registrations` or `ZoneChangeAbilityReader.strictEffects` of several
abilities and then loops one stack object per effect, or puts the whole list in one object, splits an ability or joins
several. Reads of one ability (`read(ability, ...)`, `effects(List.of(ability), ...)`), the by-ability readers
(`readByAbility`, `printedEffectsByAbility`, `effectsByAbility`, `byAbility(..., abilities -> ...)`) and `effectsOf` of
one ability's reads are not listed. Each listed call is a lead to read by hand, not a verdict: an engine-made watcher's
ability (`watcher.ability()`) is one ability, and a read that only checks whether an ability exists puts nothing on the
stack.

Read-only. Usage: step15-run17z14c-scan.py [FILE...] (default: every engine source file); prints one line per call, then
the count per file.
"""
import collections
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ENGINE = ROOT / "magical-vibes-engine/src/main/java"
CALL = re.compile(r"\b(\w+)AbilityReader\.(printedEffects|effects|strictEffects|temporaryEffects|persistentEffects"
                  r"|grantedToPermanentEffects|registrations|printedRegistrations|readPrinted|readPrintedAsJudged"
                  r"|readGrantedToPermanent|read)\(\s*([^,)]*)")
ONE_ABILITY = re.compile(r"^(List\.of\(|\(TriggeredAbility|ability$|triggered$|grantedAbility$|granted$|candidate$"
                         r"|watcher\.ability\(|[\w.]*\.ability\(\)$)")
METHOD = re.compile(r"^    (?:public |private |protected |static |final |synchronized )*[\w<>\[\], .?]+\s(\w+)\(")


def scan(path):
    lines = path.read_text().split("\n")
    method = "?"
    hits = []
    for number, line in enumerate(lines, 1):
        found = METHOD.match(line)
        if found and not re.match(r"^    (return|if|for|while|new) ", line):
            method = found.group(1)
        if re.search(r"\b(abilities|read)\s*->", line):
            continue
        for reader, call, argument in CALL.findall(line):
            argument = argument.strip()
            if call in ("read", "effects", "registrations") and ONE_ABILITY.search(argument):
                continue
            hits.append(f"{path.name}:{number} [{method}] {reader}AbilityReader.{call}({argument}")
    return hits


def main():
    paths = [pathlib.Path(arg) for arg in sys.argv[1:]] or sorted(ENGINE.rglob("*.java"))
    counts = collections.Counter()
    for path in paths:
        for hit in scan(path):
            print(hit)
            counts[path.name] += 1
    for name, count in counts.most_common():
        print(f"{count:4d} {name}")
    print(f"{sum(counts.values())} calls in {len(counts)} files")


if __name__ == "__main__":
    main()
