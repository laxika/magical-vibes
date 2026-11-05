#!/usr/bin/env python3
"""Lists every triggered ability a card class writes (addTrigger, addSimultaneousTrigger, new TriggeredAbility,
addTriggeredAbility) whose effects hold a ConditionalEffect, with the form of each one (new: the two-argument
constructor, whose interveningIf flag defaults to true; true / false: the three-argument constructor; unless;
atTriggerTime), whether it is a top-level effect of the ability, and the card's Oracle text from the server's
card-data cache (effect-slot-refactor.md step 14, run 16s).

Usage: python3 scripts/step14-intervening-if-scan.py [--summary] > out.tsv
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
COND = re.compile(r"new ConditionalEffect\(|ConditionalEffect\.unless\(|ConditionalEffect\.atTriggerTime\(")


def statements(src):
    for m in ABILITY.finditer(src):
        i, depth = m.end(), 1
        while depth and i < len(src):
            depth += (src[i] == "(") - (src[i] == ")")
            i += 1
        yield m.start(), src[m.start():i]


def call_end(stmt, start):
    i, depth = stmt.index("(", start) + 1, 1
    while depth:
        depth += (stmt[i] == "(") - (stmt[i] == ")")
        i += 1
    return i


def top_args(text):
    """The comma-separated arguments of the call starting at text[0] == '('."""
    args, depth, cur = [], 0, []
    for ch in text[1:-1]:
        if ch in "([{":
            depth += 1
        elif ch in ")]}":
            depth -= 1
        if ch == "," and depth == 0:
            args.append("".join(cur))
            cur = []
        else:
            cur.append(ch)
    args.append("".join(cur))
    return args


def conditionals(stmt):
    """(form, condition, depth-1?) of every ConditionalEffect in the statement."""
    out = []
    for m in COND.finditer(stmt):
        end = call_end(stmt, m.start())
        args = top_args(stmt[stmt.index("(", m.start()):end])
        if m.group().startswith("ConditionalEffect.unless"):
            form = "unless"
        elif m.group().startswith("ConditionalEffect.atTriggerTime"):
            form = "atTriggerTime"
        elif len(args) >= 3:
            form = args[2].strip()
        else:
            form = "new"
        depth = 0
        for ch in stmt[:m.start()]:
            depth += (ch == "(") - (ch == ")")
        out.append((form, " ".join(args[0].split()), depth == 1))
    return out


def main():
    summary = "--summary" in sys.argv
    counts = collections.Counter()
    print("file\tforms\tstatement\toracle")
    for root, _, files in os.walk("magical-vibes-card"):
        if "/build/" in root or "/test/" in root:
            continue
        for name in sorted(files):
            if not name.endswith(".java"):
                continue
            path = os.path.join(root, name)
            src = open(path).read()
            for _, stmt in statements(src):
                found = conditionals(stmt)
                if not found:
                    continue
                for form, _, top in found:
                    counts[(form, top)] += 1
                if summary:
                    continue
                forms = ";".join("%s%s(%s)" % (form, "@top" if top else "", cond) for form, cond, top in found)
                text = pilot.oracle(pilot.REG.findall(src)) or "NO-ORACLE"
                print("\t".join([path, forms, " ".join(stmt.split()), " | ".join(text.split("\n"))]))
    for key, n in sorted(counts.items()):
        print("# %s top=%s: %d" % (key[0], key[1], n), file=sys.stderr)


if __name__ == "__main__":
    main()
