#!/usr/bin/env python3
"""Step 14, run 10 of effect-slot-refactor.md: the becomes-the-target abilities (step 13 family 6, the TargetWatch
collectors) lose their trigger-only wrappers.

- "Of a spell or ability you control" (TriggeringSpellControllerConditionalEffect around a "this permanent" trigger) is
  the trigger's event: Triggers.becomesTargetOfYourSpellOrAbility() / becomesTargetOfYourSpell(). "Of a spell or
  ability an opponent controls" (TriggeringSpellOpponentControllerConditionalEffect) is
  Triggers.becomesTargetOfOpponentSpellOrAbility(), ward's trigger. Loki's "a player or permanent becomes the target of
  an ability you control" (AbilityTargetChoiceTriggerEffect) is Triggers.playerOrPermanentBecomesTargetOfYourAbility().
- A TriggeringPermanentConditionalEffect is the trigger's filter (TargetTrigger.withFilter).
- "Triggers only once each turn" / "for the first time each turn" (OncePerTurnTriggerEffect) is
  TriggerLimit.ONCE_EACH_TURN.
- Catch of the Day's chosen-mode ConditionalEffect is the intervening "if" (INTERVENING).
- The ten disguise cards' "ward {2} while face down" abilities (a ConditionalEffect on SourceIsFaceDown around
  CounterUnlessPaysEffect(2)) are deleted (DELETE): the engine gives a face-down disguised permanent ward {2}, and the
  printed abilities never triggered (the ward collector skips a face-down permanent's printed abilities, and the
  condition is false face up).
- Kept: Rayne, Academy Chancellor's effect-text "if" (NOT_INTERVENING) and Skophos Maze-Warden's
  TriggeringAbilityFromNamedLandConditionalEffect, a condition on the targeting ability's source (KEEP).

Each statement whose effects carry a wrapper is rewritten by peeling the wrappers, outermost first, and re-indenting
what is left; a wrapper the script doesn't know stops it. Unused imports of the removed wrappers are dropped and the
imports the new form needs added.

Usage: python3 scripts/step14-target-options.py [--dry-run] [--report] [--reformat-filters]

--reformat-filters only lays out the filters the first conversion pass wrote on the trigger's line as the conversion
now writes them (reformat_filters).
"""
import importlib.util
import os
import re
import sys

HERE = os.path.dirname(__file__)
spec = importlib.util.spec_from_file_location("scan", os.path.join(HERE, "step14-target-scan.py"))
scan = importlib.util.module_from_spec(spec)
spec.loader.exec_module(scan)

FAMILY = scan.FAMILY
ABILITY = scan.ABILITY
WRAPPERS = ("TriggeringSpellControllerConditionalEffect", "TriggeringSpellOpponentControllerConditionalEffect",
            "AbilityTargetChoiceTriggerEffect", "OncePerTurnTriggerEffect", "TriggeringPermanentConditionalEffect",
            "ConditionalEffect")

INTERVENING = {"CatchOfTheDay"}
DELETE = {"SanguineSavior", "RakishScoundrel", "MistwaySpy", "MuseumNightwatch", "FaerieSnoop", "ExitSpecialist",
          "DefenestratedPhantom", "BayekOfSiwa", "AlleyAssailant", "AvelineDeGrandpre"}
NOT_INTERVENING = {"RayneAcademyChancellor"}
KEEP = {"SkophosMazeWarden"}

NARROW = {
    ("TriggeringSpellControllerConditionalEffect", "becomesTargetOfSpellOrAbility"): "becomesTargetOfYourSpellOrAbility",
    ("TriggeringSpellControllerConditionalEffect", "becomesTargetOfSpell"): "becomesTargetOfYourSpell",
    ("TriggeringSpellOpponentControllerConditionalEffect", "becomesTargetOfSpellOrAbility"):
        "becomesTargetOfOpponentSpellOrAbility",
    ("AbilityTargetChoiceTriggerEffect", "playerChoosesTargets"): "playerOrPermanentBecomesTargetOfYourAbility",
}

PKG = "com.github.laxika.magicalvibes"
IMPORTS = {
    "TriggerOptions": PKG + ".model.trigger.TriggerOptions",
    "TriggerLimit": PKG + ".model.trigger.TriggerLimit",
}
REMOVABLE = list(WRAPPERS) + ["SourceIsFaceDown", "CounterUnlessPaysEffect"]


def close_paren(src, i):
    """The index just past the parenthesis that closes the one before src[i]."""
    depth, in_str = 1, False
    while depth:
        c = src[i]
        if in_str:
            if c == "\\":
                i += 1
            elif c == '"':
                in_str = False
        elif c == '"':
            in_str = True
        else:
            depth += (c == "(") - (c == ")")
        i += 1
    return i


def split_args(src, start, end):
    """The (start, end) spans of the top-level, comma-separated arguments in src[start:end]."""
    spans, depth, in_str, at = [], 0, False, start
    i = start
    while i < end:
        c = src[i]
        if in_str:
            if c == "\\":
                i += 1
            elif c == '"':
                in_str = False
        elif c == '"':
            in_str = True
        elif c in "([{":
            depth += 1
        elif c in ")]}":
            depth -= 1
        elif c == "," and depth == 0:
            spans.append((at, i))
            at = i + 1
        i += 1
    spans.append((at, end))
    return [strip_span(src, s, e) for s, e in spans]


def strip_span(src, s, e):
    while src[s].isspace():
        s += 1
    while src[e - 1].isspace():
        e -= 1
    return s, e


def column(src, i):
    return i - (src.rfind("\n", 0, i) + 1)


def placed(src, span, col):
    """src[span] re-indented so that its first character sits at column col."""
    s, e = span
    shift = col - column(src, s)
    lines = src[s:e].split("\n")
    out = [lines[0]]
    for line in lines[1:]:
        if shift >= 0:
            out.append(" " * shift + line if line.strip() else line)
        else:
            cut = min(-shift, len(line) - len(line.lstrip()))
            out.append(line[cut:])
    return "\n".join(out)


def peel(src, span, name):
    """The wrappers around the effect at span, outermost first, and the effect's span."""
    wrappers = []
    while True:
        s, e = span
        m = re.match(r"new (\w+)\(", src[s:e])
        if not m or m.group(1) not in WRAPPERS:
            return wrappers, span
        wrapper = m.group(1)
        args = split_args(src, s + m.end(), e - 1)
        if wrapper == "ConditionalEffect":
            if len(args) != 2 or name not in INTERVENING:
                raise SystemExit("%s: a ConditionalEffect the tables don't decide: %s" % (name, src[s:e]))
            wrappers.append(("if", src[args[0][0]:args[0][1]]))
            span = args[1]
        elif wrapper == "TriggeringPermanentConditionalEffect":
            if len(args) != 2:
                raise SystemExit("%s: a flagged TriggeringPermanentConditionalEffect: %s" % (name, src[s:e]))
            wrappers.append(("filter", args[0]))
            span = args[1]
        else:
            if len(args) != 1:
                raise SystemExit("%s: a %s with arguments: %s" % (name, wrapper, src[s:e]))
            wrappers.append((wrapper, None))
            span = args[0]


def rewrite(src, start, name, report):
    """The replacement for the ability expression starting at src[start], or None to leave it."""
    m = ABILITY.match(src, start)
    end = close_paren(src, m.end())
    args = split_args(src, m.end(), end - 1)
    trigger_span = args[0]
    fm = re.fullmatch(r"Triggers\.(\w+)\(\)", src[trigger_span[0]:trigger_span[1]])
    if not fm:
        return None
    factory = fm.group(1)
    if len(args) != 2:
        if any(w in src[start:end] for w in WRAPPERS):
            raise SystemExit("%s: several effects with a wrapper: %s" % (name, src[start:end]))
        return None
    wrappers, effect_span = peel(src, args[1], name)
    if not wrappers:
        return None
    filt, limit, cond = None, None, None
    for kind, value in wrappers:
        if kind == "filter":
            filt = value
        elif kind == "if":
            cond = value
        elif kind == "OncePerTurnTriggerEffect":
            limit = "ONCE_EACH_TURN"
        else:
            narrowed = NARROW.get((kind, factory))
            if narrowed is None:
                raise SystemExit("%s: no event for %s on %s" % (name, kind, factory))
            factory = narrowed
    line_start = src.rfind("\n", 0, start) + 1
    indent = len(src[line_start:start]) - len(src[line_start:start].lstrip())
    arg_col = indent + 8
    head = src[start:m.end()]
    trigger = "Triggers.%s()" % factory
    if filt:
        filter_col = indent + 16
        trigger += "\n%s.withFilter(%s)" % (" " * filter_col, placed(src, filt, filter_col + len(".withFilter(")))
    options = None
    if cond and limit:
        options = "TriggerOptions.DEFAULT.withInterveningIf(%s).withLimit(TriggerLimit.%s)" % (cond, limit)
    elif cond:
        options = "TriggerOptions.DEFAULT.withInterveningIf(%s)" % cond
    elif limit:
        options = "TriggerOptions.DEFAULT.withLimit(TriggerLimit.%s)" % limit
    parts = [trigger] + ([options] if options else [])
    effect = placed(src, effect_span, arg_col)
    one_line = head + ", ".join(parts + [effect]) + ")"
    if "\n" not in one_line and column(src, start) + len(one_line) + 1 <= 120:
        new = one_line
    else:
        new = head + (",\n" + " " * arg_col).join(parts + [effect]) + ")"
    report.append("%s\t%s\t%s" % (name, ",".join(k for k, _ in wrappers), re.sub(r"\s+", " ", new)))
    return start, end, new


def delete_statement(src, name, report):
    """src without its one face-down ward ability."""
    pattern = re.compile(r"\n[ \t]*addTrigger\(Triggers\.becomesTargetOfOpponentSpellOrAbility\(\),\s*"
                         r"new ConditionalEffect\(new SourceIsFaceDown\(\), new CounterUnlessPaysEffect\(2\)\)\);")
    matches = pattern.findall(src)
    if len(matches) != 1:
        raise SystemExit("%s: expected one face-down ward ability" % name)
    report.append("%s\tdelete\t%s" % (name, re.sub(r"\s+", " ", matches[0])))
    return pattern.sub("", src)


def fix_imports(src):
    body = src[src.index("public class"):]
    for name in REMOVABLE:
        if not re.search(r"\b%s\b" % name, body):
            src = re.sub(r"import [\w.]+\.%s;\n" % name, "", src)
    for name, fq in IMPORTS.items():
        if re.search(r"\b%s\b" % name, body) and ("import %s;" % fq) not in src:
            line = "import %s;" % fq
            imports = [i for i in re.findall(r"^import [\w.]+;$", src, re.M) if i.startswith("import com.")]
            before = [i for i in imports if i < line]
            if before:
                at = src.index(before[-1] + "\n") + len(before[-1]) + 1
            else:
                at = src.index(imports[0])
            src = src[:at] + line + "\n" + src[at:]
    return src


def reformat_filters(src):
    """src with every "Triggers.x().withFilter(…)" of a family trigger laid out as the conversion now writes it: the
    filter on a line of its own, 16 columns in from the statement, and the lines of a multi-line filter 8 columns in
    from that. The run's first pass wrote them on the trigger's line."""
    pattern = re.compile(r"(Triggers\.\w+\(\))\s*\.withFilter\(")
    at = 0
    while True:
        m = next((m for m in pattern.finditer(src, at) if FAMILY.match(src, m.start())), None)
        if m is None:
            return src
        end = close_paren(src, m.end())
        line_start = src.rfind("\n", 0, m.start()) + 1
        line = src[line_start:m.start()]
        indent = len(line) - len(line.lstrip())
        filter_col = indent + 16
        s, e = strip_span(src, m.end(), end - 1)
        lines = src[s:e].split("\n")
        rest = [l for l in lines[1:] if l.strip()]
        if rest:
            least = min(len(l) - len(l.lstrip()) for l in rest)
            lines = [lines[0]] + [" " * (filter_col + 8) + l[least:] if l.strip() else l for l in lines[1:]]
        new = m.group(1) + "\n" + " " * filter_col + ".withFilter(" + "\n".join(lines) + ")"
        src = src[:m.start()] + new + src[end:]
        at = m.start() + len(new)


def convert(src, name, report):
    if "--reformat-filters" in sys.argv:
        return reformat_filters(src)
    if name in DELETE:
        return delete_statement(src, name, report)
    edits = []
    for m in ABILITY.finditer(src):
        end = close_paren(src, m.end())
        if not FAMILY.search(src[m.start():end]):
            continue
        # Only the innermost ability expression that holds the family trigger is rewritten.
        inner = [n for n in ABILITY.finditer(src, m.end(), end) if FAMILY.search(src[n.start():close_paren(src, n.end())])]
        if inner:
            continue
        edit = rewrite(src, m.start(), name, report)
        if edit:
            edits.append(edit)
    for start, end, new in reversed(edits):
        src = src[:start] + new + src[end:]
    return reformat_filters(src) if edits else src


def main():
    dry = "--dry-run" in sys.argv
    report = []
    changed = 0
    for root, _, files in os.walk("magical-vibes-card"):
        if "/build/" in root or "/src/main/" not in root + "/":
            continue
        for n in sorted(files):
            if not n.endswith(".java"):
                continue
            name = n[:-5]
            if name in KEEP or name in NOT_INTERVENING:
                continue
            path = os.path.join(root, n)
            src = open(path).read()
            if not FAMILY.search(src):
                continue
            new = convert(src, name, report)
            if new == src:
                continue
            new = fix_imports(new)
            changed += 1
            print(path)
            if not dry:
                open(path, "w").write(new)
    if "--report" in sys.argv:
        print("\n".join(report), file=sys.stderr)
    print("%d card classes" % changed, file=sys.stderr)


if __name__ == "__main__":
    main()
