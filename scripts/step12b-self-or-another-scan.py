#!/usr/bin/env python3
"""Lists the card classes that have both a "when this enters" ability (Triggers.enters(ObjectRelation.THIS)) and an
ability that sees another object enter (Triggers.enters / entersFrom with another relation), with the Oracle
sentences about something entering and the wording step12a-enters-scan gives them: "this-or-another" when the text
says "this creature or another …", "another" or "plain" otherwise (effect-slot-refactor.md step 12b). Prints a TSV.

Usage: python3 scripts/step12b-self-or-another-scan.py > out.tsv
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
scan = __import__("step11-death-scan")
enters_scan = __import__("step12a-enters-scan")

SELF = "Triggers.enters(ObjectRelation.THIS)"
OTHER_RE = re.compile(r"Triggers\.(?:enters|entersFrom)\((?:Zone\.\w+, )?([\w.]+|null),\s*ObjectRelation\.(\w+)\)")


def main():
    print("file\tother triggers\twordings\tsentences")
    for root, _, files in os.walk("magical-vibes-card"):
        for name in sorted(files):
            if not name.endswith(".java") or "/build/" in root:
                continue
            path = os.path.join(root, name)
            src = open(path).read()
            if SELF not in src:
                continue
            others = sorted({"%s %s" % m for m in OTHER_RE.findall(src) if m[1] != "THIS"})
            if not others:
                continue
            regs = scan.REG_RE.findall(src)
            text, _ = scan.oracle(regs)
            sentences = [s.strip() for s in enters_scan.SENTENCE_RE.findall(text or "")]
            card_names = enters_scan.names(regs)
            wordings = sorted({enters_scan.wording(s, card_names) for s in sentences}) or ["?"]
            print("\t".join([path, ",".join(others), ",".join(wordings), " | ".join(sentences)]))


if __name__ == "__main__":
    main()
