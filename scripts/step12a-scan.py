#!/usr/bin/env python3
"""Lists every card class that registers one of the given zone-change slots (effect-slot-refactor.md step 12a),
with the Oracle sentences that match a keyword, read from the server's card-data cache. Prints a TSV on stdout:
file, slots, sentences. Usage: python3 scripts/step12a-scan.py KEYWORD_REGEX SLOT [SLOT ...] > out.tsv
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
step11 = __import__("step11-death-scan")
REG_RE = step11.REG_RE


def main():
    keyword = re.compile(sys.argv[1], re.I)
    slot_re = re.compile(r"EffectSlot\.(" + "|".join(sys.argv[2:]) + r")\b")
    for root, _, files in os.walk("magical-vibes-card"):
        for name in sorted(files):
            if not name.endswith(".java"):
                continue
            path = os.path.join(root, name)
            src = open(path).read()
            slots = slot_re.findall(src)
            if not slots:
                continue
            text, _ = step11.oracle(REG_RE.findall(src))
            sentences = [s.strip() for s in re.split(r"(?<=[.\n])", text or "") if keyword.search(s)]
            print("\t".join([name[:-5], ",".join(slots),
                             " | ".join(sentences) if text is not None else "NO-ORACLE"]))


if __name__ == "__main__":
    sys.exit(main())
