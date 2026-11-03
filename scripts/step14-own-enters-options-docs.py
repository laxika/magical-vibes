#!/usr/bin/env python3
"""Step 14, run 4b of effect-slot-refactor.md: rewrites the agent-docs rows of the cards that
scripts/step14-own-enters-options.py converted, so they show a "when this enters, if …" ability's intervening "if" as
`TriggerOptions.DEFAULT.withInterveningIf(c)` instead of a `ConditionalEffect(c, effect)` wrapper.

A row is rewritten when it names a converted card (its class path such as `d/DeadeyeTormentor.java`, or its name).
In such a row, each `ConditionalEffect(c, e)` / `new ConditionalEffect(c, e)` that isn't a STATIC or SPELL one and
whose condition isn't an `AllConditions(…)` becomes `TriggerOptions.DEFAULT.withInterveningIf(c), e`, the arguments
`addTrigger(Triggers.enters(ObjectRelation.THIS), …)` takes. AFTER is applied by exact replacement afterwards, for
the rows whose prose the generic rewrite leaves awkward. --dry-run prints the diffs.

Usage: python3 scripts/step14-own-enters-options-docs.py [--dry-run] <names file: one converted class name a line>
"""
import difflib
import importlib.util
import os
import re
import sys

_spec = importlib.util.spec_from_file_location(
    "pilot_options", os.path.join(os.path.dirname(__file__), "step14-pilot-options.py"))
po = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(po)

DOCS = "agent-docs"
SKIP_CARDS = {"BloodhallPriest", "NaomiPillarOfOrder", "OmnivorousFlytrap", "MythosOfIlluna"}
WRAPPER = re.compile(r"(?<![\w.])(new )?ConditionalEffect\(")

# A condition's own reference entry (a row or bullet that starts with the wrapper) describes the condition for every
# use, a resolution-time "if" included, so it stays.
GENERIC = re.compile(r"^(?:\| |- )`(?:new )?ConditionalEffect\(")
# Conditions of other trigger families in a row that also names a converted card.
OTHER_FAMILIES = ("AllConditions", "AttackedTargetIsMonarch")

AFTER = {
    "agent-docs/CARD_IMPLEMENTATION_PLAYBOOK.md": [(
        "wrap the ETB effect in `TriggerOptions.DEFAULT.withInterveningIf(new CastForProwlCost()), innerEffect` on "
        "`Triggers.enters(ObjectRelation.THIS)`",
        "`addTrigger(Triggers.enters(ObjectRelation.THIS), "
        "TriggerOptions.DEFAULT.withInterveningIf(new CastForProwlCost()), innerEffect)`")],
    "agent-docs/CARD_PATTERNS_CREATURES_ETB.md": [(
        "Two TriggerOptions.DEFAULT.withInterveningIf(new Metalcraft()), wrapped wrappers: BoostSelfEffect(3,3) + "
        "GrantKeywordEffect(HASTE, SELF)",
        "Triggers.enters(ObjectRelation.THIS), TriggerOptions.DEFAULT.withInterveningIf(new Metalcraft()) on two "
        "abilities: BoostSelfEffect(3,3) + GrantKeywordEffect(HASTE, SELF)")],
}


def name_pattern(name):
    words = re.sub(r"(?<!^)(?=[A-Z])", " ", name).split()
    return re.compile(r"(?:/%s\.java|\b%s\b)" % (re.escape(name), r"\W*".join(re.escape(w) for w in words)), re.I)


def rewrite(line):
    out, pos = [], 0
    for m in WRAPPER.finditer(line):
        if m.start() < pos:
            continue
        before = line[max(0, m.start() - 8):m.start()]
        if "STATIC" in before or "SPELL" in before:
            continue
        try:
            end = po.close_paren(line, m.end())
        except (IndexError, AttributeError):
            continue
        args = po.split_args(line[m.end():end - 1])
        if len(args) != 2 or args[0].startswith(OTHER_FAMILIES):
            continue
        start = m.start()
        prefix = line[pos:start]
        if prefix.endswith("ObjectRelation.THIS) "):
            prefix = prefix[:-1] + ", "
        out.append(prefix + "TriggerOptions.DEFAULT.withInterveningIf(%s), %s" % (args[0], args[1]))
        pos = end
    out.append(line[pos:])
    return "".join(out)


def main():
    dry = "--dry-run" in sys.argv
    names_file = [a for a in sys.argv[1:] if not a.startswith("--")][0]
    names = [n for n in open(names_file).read().split() if n not in SKIP_CARDS]
    patterns = [name_pattern(n) for n in names]
    for root, _, files in os.walk(DOCS):
        for file in sorted(files):
            if not file.endswith(".md"):
                continue
            path = os.path.join(root, file)
            original = open(path).read()
            lines = original.split("\n")
            for i, line in enumerate(lines):
                if ("ConditionalEffect" in line and not GENERIC.match(line)
                        and any(p.search(line) for p in patterns)):
                    lines[i] = rewrite(line)
            src = "\n".join(lines)
            for old, new in AFTER.get(path, []):
                if old not in src:
                    print("AFTER NOT FOUND in %s: %s" % (path, old[:100]))
                src = src.replace(old, new, 1)
            if src == original:
                continue
            if dry:
                sys.stdout.writelines(difflib.unified_diff(original.splitlines(True), src.splitlines(True), path, path))
            else:
                open(path, "w").write(src)
                print(path)


if __name__ == "__main__":
    sys.exit(main())
