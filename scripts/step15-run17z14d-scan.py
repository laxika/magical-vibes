#!/usr/bin/env python3
"""Step 15, run 17z14d: the collectors that put one stack object per effect of a list they loop over (read-only).

One triggered ability is one stack object of its effects (CR 603.3). `step15-run17z14c-scan.py` lists the flat-list
reader calls; this lists the other half, which that scan can't see: a `for (CardEffect x : ...)` loop whose body builds
`List.of(x)` (a stack entry or a queued choice of that one effect). Each line is a lead to read by hand, not a verdict:
a loop over the abilities of an engine-made watcher (one effect each), or over effects that are each an ability of their
own (a perpetual grant), puts nothing apart.

Usage: step15-run17z14d-scan.py [FILE...] (default: every engine source file); prints one line per site, then the count
per file.
"""
import collections
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ENGINE = ROOT / "magical-vibes-engine/src/main/java"
LOOP = re.compile(r"^(\s*)for \((?:final )?CardEffect (\w+) :")
METHOD = re.compile(r"^    (?:public |private |protected |static |final |synchronized )*[\w<>\[\], .?]+\s(\w+)\(")


def scan(path):
    lines = path.read_text().split("\n")
    method = "?"
    loops = []
    hits = []
    for number, line in enumerate(lines, 1):
        found = METHOD.match(line)
        if found and not re.match(r"^    (return|if|for|while|new|else) ", line):
            method = found.group(1)
            loops = []
        indent = len(line) - len(line.lstrip())
        if line.strip():
            loops = [(depth, name) for depth, name in loops if indent > depth or line.strip() == "}" and indent > depth]
        loop = LOOP.match(line)
        if loop:
            loops.append((len(loop.group(1)), loop.group(2)))
            continue
        for _, name in loops:
            if re.search(r"List\.of\(\s*" + re.escape(name) + r"\s*\)", line):
                hits.append(f"{path.name}:{number} [{method}] List.of({name})")
                break
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
    print(f"{sum(counts.values())} sites in {len(counts)} files")


if __name__ == "__main__":
    main()
