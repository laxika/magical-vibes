#!/usr/bin/env python3
"""Step 14, run 5 of effect-slot-refactor.md: rewrites the agent-docs rows of the cards that
scripts/step14-step-options.py converted, so they show an "at the beginning of [step or phase], if …" ability's
intervening "if" as `TriggerOptions.DEFAULT.withInterveningIf(c)` instead of a `ConditionalEffect(c, effect)` wrapper,
and Survival as `Triggers.secondMainPhase(ActivePlayer.YOU)` with the option instead of `SurvivalTriggerEffect`.

A row is rewritten when it names a converted card (its class path such as `w/WaryZoneGuard.java`, or its name). In
such a row, each two-argument `ConditionalEffect(c, e)` / `new ConditionalEffect(c, e)` whose nearest `Triggers.`
before it is a step trigger becomes `TriggerOptions.DEFAULT.withInterveningIf(c), e`. AFTER is applied by exact
replacement afterwards. --dry-run prints the diffs.

--survival-only rewrites only the Survival rows (the first run of the script missed the rows whose trigger column
follows the effect column; run once more with it).

Usage: python3 scripts/step14-step-docs.py [--dry-run] [--survival-only] [names file, default
scripts/step14-step-converted.txt]
"""
import difflib
import importlib.util
import os
import re
import sys

_here = os.path.dirname(__file__)
_spec = importlib.util.spec_from_file_location("pilot_options", os.path.join(_here, "step14-pilot-options.py"))
po = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(po)

DOCS = "agent-docs"
WRAPPER = re.compile(r"(?<![\w.])(new )?ConditionalEffect\(")
SURVIVAL = re.compile(r"(?<![\w.])(new )?SurvivalTriggerEffect\(")
TRIGGER = re.compile(r"Triggers\.(\w+)\(")
STEPS = {"upkeep", "drawStep", "precombatMainPhase", "beginningOfCombat", "endOfCombat", "postcombatMainPhase",
         "secondMainPhase", "endStep"}
GENERIC = re.compile(r"^(?:\| |- )`(?:new )?ConditionalEffect\(")
AFTER = {
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md": [(
        "Acrobatic Cheerleader. The wrapper evaluates only at the first postcombat main phase for the permanent "
        "object; a card that leaves and returns can evaluate again",
        "Acrobatic Cheerleader, with `.withLimit(TriggerLimit.ONCE)` for \"this ability triggers only once\". "
        "`secondMainPhase` triggers in the turn's second main phase only (CR 505.1); the limit marks the permanent "
        "object, so a card that leaves and returns can trigger again"), (
        "`ConditionalEffect(new SourceIsTapped(), new PerpetuallyGrantKeywordToSourceAndTopLibraryCardEffect(keyword, "
        "CardAllOfPredicate(CREATURE, CardNotPredicate(CardKeywordPredicate(keyword)))))`",
        "`TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped()), new "
        "PerpetuallyGrantKeywordToSourceAndTopLibraryCardEffect(keyword, CardAllOfPredicate(CREATURE, "
        "CardNotPredicate(CardKeywordPredicate(keyword))))`")],
}


def name_pattern(name):
    words = re.sub(r"(?<!^)(?=[A-Z])", " ", name).split()
    return re.compile(r"(?:/%s\.java|\b%s\b)" % (re.escape(name), r"\W*".join(re.escape(w) for w in words)), re.I)


def after_step_trigger(line, at):
    if "Survival" in line and re.match(r"(?:new )?ConditionalEffect\(new SourceIsTapped\(\)", line[at:]):
        return True
    triggers = list(TRIGGER.finditer(line, 0, at))
    if not triggers or triggers[-1].group(1) not in STEPS:
        return False
    before = line[max(0, at - 8):at]
    return "STATIC" not in before and "SPELL" not in before


def unwrap_survival(line):
    """`SurvivalTriggerEffect(X)` → X, and the row's postcombat main phase trigger becomes the second main phase."""
    out, pos, found = [], 0, False
    for m in SURVIVAL.finditer(line):
        if m.start() < pos:
            continue
        try:
            end = po.close_paren(line, m.end())
        except (IndexError, AttributeError):
            continue
        out.append(line[pos:m.start()] + line[m.end():end - 1])
        pos, found = end, True
    out.append(line[pos:])
    line = "".join(out)
    if found:
        line = line.replace("Triggers.postcombatMainPhase(ActivePlayer.YOU)", "Triggers.secondMainPhase(ActivePlayer.YOU)")
        if "secondMainPhase" not in line:
            line = re.sub(r"(?<![\w.])(?=(?:new )?ConditionalEffect\()", "Triggers.secondMainPhase(ActivePlayer.YOU) ",
                          line, count=1)
    return line


def rewrite(line):
    line = unwrap_survival(line)
    out, pos = [], 0
    for m in WRAPPER.finditer(line):
        if m.start() < pos or not after_step_trigger(line, m.start()):
            continue
        try:
            end = po.close_paren(line, m.end())
        except (IndexError, AttributeError):
            continue
        args = po.split_args(line[m.end():end - 1])
        if len(args) != 2:
            continue
        once = po.call(args[1], "new OnceOnlyTriggerEffect") or po.call(args[1], "OnceOnlyTriggerEffect")
        options = "TriggerOptions.DEFAULT.withInterveningIf(%s)" % args[0]
        effect = args[1]
        if once is not None and len(once) == 1:
            options += ".withLimit(TriggerLimit.ONCE)"
            effect = once[0]
        out.append(line[pos:m.start()] + "%s, %s" % (options, effect))
        pos = end
    out.append(line[pos:])
    return "".join(out)


def main():
    dry = "--dry-run" in sys.argv
    survival_only = "--survival-only" in sys.argv
    files = [a for a in sys.argv[1:] if not a.startswith("--")]
    names_file = files[0] if files else os.path.join(_here, "step14-step-converted.txt")
    names = open(names_file).read().split()
    patterns = [name_pattern(n) for n in names]
    for root, _, docs in os.walk(DOCS):
        for file in sorted(docs):
            if not file.endswith(".md"):
                continue
            path = os.path.join(root, file)
            original = open(path).read()
            lines = original.split("\n")
            for i, line in enumerate(lines):
                if line.startswith("- `SurvivalTriggerEffect(") or (survival_only and "Survival" not in line):
                    continue
                if (("ConditionalEffect" in line or "SurvivalTriggerEffect" in line) and not GENERIC.match(line)
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
