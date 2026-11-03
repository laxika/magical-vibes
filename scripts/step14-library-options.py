#!/usr/bin/env python3
"""Converts the library, draw and discard abilities (the Triggers factories of scripts/step13-library-slots.tsv, which
the LibraryWatch collectors read) from trigger-only wrapper effects to trigger filters and options
(effect-slot-refactor.md step 14, run 11).

Each family ability with one effect has its wrappers peeled, outermost first:

- new TriggeringCardConditionalEffect(P, X)              -> trigger.withFilter(P)
- new OncePerTurnTriggerEffect(X)                         -> TriggerLimit.ONCE_EACH_TURN
- OncePerTurnTriggerEffect.markOnAcceptance(X)            -> TriggerLimit.DO_ONLY_ONCE_EACH_TURN
- OncePerTurnTriggerEffect.firstCycling / firstNonlandDiscard / firstDiscardEvent / firstSurveil(X)
                                                          -> trigger.firstTimeEachTurn()
- new OnceOnlyTriggerEffect(X)                            -> TriggerLimit.ONCE
- new NthCardDrawTriggerEffect(N, X)                      -> youDrawNthCardEachTurn(N) / opponentDrawsNthCardEachTurn(N)
- new FirstOrSecondCardDrawTriggerEffect(X)               -> List.of(youDrawNthCardEachTurn(1), youDrawSecondCardEachTurn())
- new ExceptFirstDrawStepTriggerEffect(X)                 -> opponentDrawsCardExceptFirstInDrawStep()
- new FirstDrawStepDrawOnlyTriggerEffect(X)               -> youDrawFirstCardInDrawStep()
- new CyclingTriggerEffect(X)                             -> youCycleCard()
- new OpponentCausedDiscardTriggerEffect(X)               -> opponentCausesYouToDiscardCard()
- new ConditionalEffect(C, X)                             -> TriggerOptions.DEFAULT.withInterveningIf(C) (CR 603.4)

ConditionalEffect.unless / atTriggerTime and conditionals nested inside other effects stay effects. MANUAL holds the
cards whose abilities are merged; they are replaced exactly. The script stops on a wrapper it can't place.

Usage: python3 scripts/step14-library-options.py [--dry-run] [--report]
"""
import importlib.util
import os
import re
import sys

HERE = os.path.dirname(__file__)
spec = importlib.util.spec_from_file_location("scan", os.path.join(HERE, "step14-library-scan.py"))
scan = importlib.util.module_from_spec(spec)
spec.loader.exec_module(scan)

IMPORT_PREFIX = "com.github.laxika.magicalvibes.model."
WRAPPER_IMPORTS = ["effect.TriggeringCardConditionalEffect", "effect.OncePerTurnTriggerEffect",
                   "effect.OnceOnlyTriggerEffect", "effect.NthCardDrawTriggerEffect",
                   "effect.FirstOrSecondCardDrawTriggerEffect", "effect.ExceptFirstDrawStepTriggerEffect",
                   "effect.FirstDrawStepDrawOnlyTriggerEffect", "effect.CyclingTriggerEffect",
                   "effect.OpponentCausedDiscardTriggerEffect", "effect.ConditionalEffect"]

MANUAL = {
    "HostileInvestigator.java": [(
        """        OncePerTurnTriggerEffect investigate = new OncePerTurnTriggerEffect(
                CreateTokenEffect.ofClueToken(1));
        addTrigger(Triggers.youDiscardOneOrMoreCards(), investigate);
        addTrigger(Triggers.opponentDiscardsCard(), investigate);
""",
        """        addTrigger(List.of(Triggers.youDiscardOneOrMoreCards(), Triggers.opponentDiscardsCard()),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                CreateTokenEffect.ofClueToken(1));
""")],
    "PlanetariumOfWanShiTong.java": [(
        """        addTrigger(Triggers.youScry(), OncePerTurnTriggerEffect.markOnAcceptance(
                new CastTopOfLibraryWithoutPayingManaCostEffect(CASTABLE_CARD_TYPES)));
        addTrigger(Triggers.youSurveil(), OncePerTurnTriggerEffect.markOnAcceptance(
                new CastTopOfLibraryWithoutPayingManaCostEffect(CASTABLE_CARD_TYPES)));
""",
        """        addTrigger(List.of(Triggers.youScry(), Triggers.youSurveil()),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                new CastTopOfLibraryWithoutPayingManaCostEffect(CASTABLE_CARD_TYPES));
""")],
}

FIRST_TIME = ("firstCycling", "firstNonlandDiscard", "firstDiscardEvent", "firstSurveil")


def split_args(body):
    args, depth, cur, quote = [], 0, "", None
    i = 0
    while i < len(body):
        ch = body[i]
        if quote:
            cur += ch
            if ch == "\\":
                cur += body[i + 1]
                i += 1
            elif ch == quote:
                quote = None
        elif ch in "\"'":
            quote = ch
            cur += ch
        elif ch in "([{":
            depth += 1
            cur += ch
        elif ch in ")]}":
            depth -= 1
            cur += ch
        elif ch == "," and depth == 0:
            args.append(cur)
            cur = ""
        else:
            cur += ch
        i += 1
    args.append(cur)
    return args


def call(expr, head):
    """The arguments of expr if it is exactly head(...), else None."""
    e = expr.strip()
    if not e.startswith(head + "("):
        return None
    i, depth = len(head) + 1, 1
    while depth:
        depth += (e[i] == "(") - (e[i] == ")")
        i += 1
    if e[i:].strip():
        return None
    return split_args(e[len(head) + 1:i - 1])


def one_line(text):
    return re.sub(r"\s*\n\s*", " ", text.strip()).replace("( ", "(").replace(" )", ")")


LIMIT = 120


def fitted(text, src_offset, src, col, prefix_len, suffix_len):
    """text on one line when it fits from column col with its prefix and suffix, else re-indented as written."""
    flat = one_line(text)
    if col + prefix_len + len(flat) + suffix_len <= LIMIT:
        return flat
    stripped = text.strip()
    start = src_offset + text.find(stripped)
    return reindent(stripped, line_indent(src, start), col)


class Stop(Exception):
    pass


def peel(effect):
    state = {"filter": None, "limit": None, "first": False, "nth": None, "event": None, "if": None}
    e = effect
    while True:
        a = call(e, "new TriggeringCardConditionalEffect")
        if a and len(a) == 2:
            if state["filter"]:
                raise Stop("two filters")
            state["filter"], e = a[0], a[1]
            continue
        a = call(e, "new OncePerTurnTriggerEffect")
        if a and len(a) == 1:
            state["limit"], e = "ONCE_EACH_TURN", a[0]
            continue
        a = call(e, "OncePerTurnTriggerEffect.markOnAcceptance")
        if a and len(a) == 1:
            state["limit"], e = "DO_ONLY_ONCE_EACH_TURN", a[0]
            continue
        found = False
        for name in FIRST_TIME:
            a = call(e, "OncePerTurnTriggerEffect." + name)
            if a and len(a) == 1:
                state["first"], e, found = name, a[0], True
                break
        if found:
            continue
        a = call(e, "new OnceOnlyTriggerEffect")
        if a and len(a) == 1:
            state["limit"], e = "ONCE", a[0]
            continue
        a = call(e, "new NthCardDrawTriggerEffect")
        if a and len(a) == 2:
            state["nth"], e = int(a[0].strip()), a[1]
            continue
        for head, event in (("new FirstOrSecondCardDrawTriggerEffect", "firstOrSecond"),
                            ("new ExceptFirstDrawStepTriggerEffect", "exceptFirst"),
                            ("new FirstDrawStepDrawOnlyTriggerEffect", "firstDrawStep"),
                            ("new CyclingTriggerEffect", "cycle"),
                            ("new OpponentCausedDiscardTriggerEffect", "opponentCaused")):
            a = call(e, head)
            if a and len(a) == 1:
                if state["event"]:
                    raise Stop("two event wrappers")
                state["event"], e, found = event, a[0], True
                break
        if found:
            continue
        a = call(e, "new ConditionalEffect")
        if a and len(a) == 2:
            if state["if"]:
                raise Stop("two intervening ifs")
            state["if"], e = a[0], a[1]
            continue
        return state, e


def trigger_expr(factory, state):
    event, nth = state["event"], state["nth"]
    if event == "cycle":
        assert factory == "youDiscardCard", factory
        base = "Triggers.youCycleCard()"
    elif event == "opponentCaused":
        assert factory == "youDiscardCard", factory
        base = "Triggers.opponentCausesYouToDiscardCard()"
    elif event == "exceptFirst":
        assert factory == "opponentDrawsCard", factory
        base = "Triggers.opponentDrawsCardExceptFirstInDrawStep()"
    elif event == "firstDrawStep":
        assert factory == "youDrawCard", factory
        base = "Triggers.youDrawFirstCardInDrawStep()"
    elif event == "firstOrSecond":
        assert factory == "youDrawCard" and nth is None, factory
        return "List.of(Triggers.youDrawNthCardEachTurn(1), Triggers.youDrawSecondCardEachTurn())"
    elif nth is not None:
        if factory == "youDrawCard":
            base = "Triggers.youDrawSecondCardEachTurn()" if nth == 2 else "Triggers.youDrawNthCardEachTurn(%d)" % nth
        elif factory == "opponentDrawsCard":
            base = ("Triggers.opponentDrawsSecondCardEachTurn()" if nth == 2
                    else "Triggers.opponentDrawsNthCardEachTurn(%d)" % nth)
        else:
            raise Stop("a count on " + factory)
    else:
        base = "Triggers.%s()" % factory
    if event and nth is not None:
        raise Stop("a count with an event wrapper")
    return base


def reindent(text, old_col, new_col):
    lines = text.split("\n")
    out = [lines[0]]
    delta = new_col - old_col
    for line in lines[1:]:
        stripped = line.lstrip(" ")
        lead = len(line) - len(stripped)
        out.append(" " * max(new_col, lead + delta) + stripped if stripped else "")
    return "\n".join(out)


def column(src, offset):
    return offset - (src.rfind("\n", 0, offset) + 1)


def line_indent(src, offset):
    """The leading whitespace of the line offset is on: what the lines after it were indented relative to."""
    start = src.rfind("\n", 0, offset) + 1
    line = src[start:offset]
    return len(line) - len(line.lstrip(" "))


def convert_statement(src, start, stmt, factory, report):
    head = re.match(r"\s*(addTrigger|addSimultaneousTrigger|new TriggeredAbility|addTriggeredAbility)\(", stmt)
    name = head.group(1)
    body_start = start + head.end()
    args = split_args(stmt[head.end():-1])
    trigger_arg = args[0]
    rest = args[1:]
    has_options = False
    if rest and rest[0].strip() == "TriggerOptions.DEFAULT":
        has_options = True
        rest = rest[1:]
    if not rest or any(r.strip().startswith("TriggerOptions") or r.strip().startswith("TriggerMode") for r in rest):
        if any(any(peel(r)[0].values()) for r in rest[1:]):
            raise Stop("a wrapper next to options or a mode already written")
        return None
    if len(rest) != 1:
        state, _ = peel(rest[0])
        if any(state.values()):
            raise Stop("a wrapped ability with several effects")
        return None
    effect_arg = rest[0]
    state, inner = peel(effect_arg)
    if not any(state.values()):
        return None
    trig = trigger_expr(factory, state)
    line_start = src.rfind("\n", 0, start) + 1
    line = src[line_start:start]
    ind = " " * (len(line) - len(line.lstrip(" ")) + 8)
    if state["filter"]:
        suffix = ".firstTimeEachTurn()),".__len__() if state["first"] else 2
        filt = fitted(state["filter"], src.find(state["filter"], start), src, len(ind), len(".withFilter("),
                      suffix)
        trig += "\n%s.withFilter(%s)" % (ind, filt)
        if state["first"] and "\n" in filt:
            trig += "\n" + ind
    if state["first"]:
        trig += ".firstTimeEachTurn()"
    old_trigger = "Triggers.%s()" % factory
    if old_trigger not in trigger_arg:
        raise Stop("trigger argument " + trigger_arg)
    new_trigger_arg = trigger_arg.strip().replace(old_trigger, trig, 1)
    options = None
    if state["if"] or state["limit"]:
        options = "TriggerOptions.DEFAULT"
        if state["if"]:
            flat = one_line(state["if"])
            if len(ind) + len("TriggerOptions.DEFAULT.withInterveningIf(") + len(flat) + 2 <= LIMIT:
                options += ".withInterveningIf(%s)" % flat
            elif len(ind) + 8 + len(flat) + 2 <= LIMIT:
                options += ".withInterveningIf(\n%s        %s)" % (ind, flat)
            else:
                raise Stop("an intervening if too long to place: " + flat)
        if state["limit"]:
            limit = ".withLimit(TriggerLimit.%s)" % state["limit"]
            last = options.split("\n")[-1]
            if (len(ind) if "\n" not in options else 0) + len(last) + len(limit) + 1 > LIMIT:
                options += "\n" + ind + "        " + limit
            else:
                options += limit
    elif has_options:
        options = "TriggerOptions.DEFAULT"
    inner_text = inner.strip()
    inner_offset = body_start + len(",".join(args[:-1])) + 1 + (len(effect_arg) - len(effect_arg.lstrip())) \
        + effect_arg.lstrip().find(inner_text)
    inner_col = line_indent(src, inner_offset)
    out = name + "(" + new_trigger_arg
    if options:
        out += ",\n" + ind + options
    out += ",\n" + ind + reindent(inner_text, inner_col, len(ind)) + ")"
    report.append("%s -> %s | %s" % (factory, one_line(new_trigger_arg),
                                       options or "-"))
    return out


def convert(path, src, report):
    name = os.path.basename(path)
    if name in MANUAL:
        for old, new in MANUAL[name]:
            if old not in src:
                raise Stop("manual replacement does not match")
            src = src.replace(old, new)
        report.append("manual merge")
    edits = []
    for m in scan.ABILITY.finditer(src):
        i, depth = m.end(), 1
        while depth:
            depth += (src[i] == "(") - (src[i] == ")")
            i += 1
        stmt = src[m.start():i]
        f = scan.FAMILY.search(stmt)
        if not f:
            continue
        new = convert_statement(src, m.start(), stmt, f.group(1), report)
        if new is not None:
            edits.append((m.start(), i, new))
    for s, e, new in reversed(edits):
        src = src[:s] + new + src[e:]
    return src


def fix_imports(src):
    lines = src.split("\n")
    body = "\n".join(l for l in lines if not l.startswith("import "))
    keep = []
    for line in lines:
        m = re.match(r"import (com\.github\.laxika\.magicalvibes\.model\.[\w.]+);", line)
        if m and m.group(1)[len(IMPORT_PREFIX):] in WRAPPER_IMPORTS:
            simple = m.group(1).rsplit(".", 1)[1]
            if not re.search(r"\b%s\b" % simple, body):
                continue
        keep.append(line)
    src = "\n".join(keep)
    wanted = []
    if "TriggerOptions." in src:
        wanted.append(IMPORT_PREFIX + "trigger.TriggerOptions")
    if "TriggerLimit." in src:
        wanted.append(IMPORT_PREFIX + "trigger.TriggerLimit")
    if "List.of(" in src:
        wanted.append("java.util.List")
    for imp in wanted:
        pkg = imp.rsplit(".", 1)[0]
        if ("import %s;" % imp) in src or ("import %s.*;" % pkg) in src:
            continue
        imports = [m for m in re.finditer(r"^import (?!static)([^\n]+);$", src, re.M)]
        group = [m for m in imports if m.group(1).startswith("java.") == imp.startswith("java.")]
        if not group:
            last = imports[-1]
            src = src[:last.end()] + "\n\nimport %s;" % imp + src[last.end():]
            continue
        after = [m for m in group if m.group(1) < imp]
        if after:
            pos = after[-1].end()
            src = src[:pos] + "\nimport %s;" % imp + src[pos:]
        else:
            pos = group[0].start()
            src = src[:pos] + "import %s;\n" % imp + src[pos:]
    return src


def main():
    dry = "--dry-run" in sys.argv
    show = "--report" in sys.argv
    changed = 0
    for root, _, files in os.walk("magical-vibes-card"):
        if "/build/" in root or "/test/" in root:
            continue
        for fname in sorted(files):
            if not fname.endswith(".java"):
                continue
            path = os.path.join(root, fname)
            src = open(path).read()
            if not scan.FAMILY.search(src):
                continue
            report = []
            try:
                new = convert(path, src, report)
            except Stop as e:
                print("STOP %s: %s" % (path, e))
                return 1
            if new == src:
                continue
            new = fix_imports(new)
            changed += 1
            if show:
                print(fname + ": " + "; ".join(report))
            if not dry:
                open(path, "w").write(new)
    print("changed %d card classes" % changed)
    return 0


if __name__ == "__main__":
    sys.exit(main())
