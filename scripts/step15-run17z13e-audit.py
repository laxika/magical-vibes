#!/usr/bin/env python3
"""Step 15, run 17z13e: what each trigger handler records, against its watch's default.

Run 17z13's independent check found handlers picked by an effect's class that record the event's data, the source's or
the ability's only for that class (row 17z13e). `step15-run17z6-audit.py` labels a handler by what it does to the
effect (rebuild, unwrap, bind a field of the effect); this scan compares what it records. For every watch it lists the
`CardEffect.class` default and every class-keyed handler (`@Collects...Trigger(value = X.class, watch = W)`,
`DeathTriggerCollectorService.registerCreatureDeath(W, X.class, ...)`, `LegacyTriggerBindings.register(h, X.class, ...)`)
with the record tokens its body sets, following the same file's private helpers:

- `set:F` an entry, choice or may-ability setter (`.setF(`), `with:F` a choice's wither (`.withF(`);
- `ctor:target` a `new StackEntry(...)` whose target argument isn't `null`;
- `call:M` a cross-service call of a recording or queueing helper (`record...`, `enqueue...`, `queue...`).

Each handler is classified:

- ROUTE: records what its watch's default records (the class only picks a target choice or queue);
- RECORD-GAP: records event, source or ability data the default doesn't, or omits data the default records, or is a
  watch's only handlers and they record different things (a violation: the data should be recorded whatever the effects
  are);
- REWRITE: builds, merges or unwraps effects (a violation);
- DUP: the same (watch, class) key is registered twice (the registry keeps the last one: a violation).

The labels are a heuristic: a handler that records the same through a choice the default doesn't use, or a helper of
another service, can be labelled RECORD-GAP. `HAND` holds the hand classification where the heuristic's label is wrong
(the reason in the comment); `ROWS` names the row of step 15 that holds a watch's handlers that aren't ROUTE, which
`--summary` counts; `READERS` (keyed by file and effect class) holds the verdicts on the step and draw readers'
class-keyed branches, all row 17z13e8's to read. The run's Findings (effect-slot-refactor.md, run 17z13e) give the
details. `TriggerCollectorRegistry.register` throws on a duplicate key since run 17z13e, so DUP can only come from two
annotations naming one key (the registry rejects those at start-up too). The rows' done condition: no RECORD-GAP,
REWRITE or DUP left, no reader branch unread.

Read-only. Usage: step15-run17z13e-audit.py [--summary] [--watch NAME]
"""
import pathlib
import re
import sys
from collections import Counter, defaultdict

ROOT = pathlib.Path(__file__).resolve().parent.parent
S = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service"
T = S / "trigger"

ANNOTATION = re.compile(r"@Collects(\w*)Trigger\(\s*([^)]*)\)", re.S)
METHOD = re.compile(r"\n    (?:(?:private|public|protected) )?(?:static )?(?!new |return )[\w<>, .?\[\]]+ (\w+)\(([^)]*)\)\s*"
                    r"(?:throws [\w, ]+)?\{")
HANDLER = re.compile(r"\n    (?:(?:private|public|protected) )?(?:static )?boolean (\w+)\(")
DEATH = re.compile(r"registerCreatureDeath\(\s*CreatureDeathWatch\.(\w+),\s*(\w+)\.class,.*?(?:this::(\w+)|->\s*(\w+)\()",
                   re.S)
LEGACY_BLOCK = re.compile(r"private void (register\w+Handlers)\(\)\s*\{(.*?)\n    \}", re.S)
LEGACY = re.compile(r"\bregister\(h,\s*(\w+)\.class,\s*this::(\w+)\)")
SETTER = re.compile(r"\.(set|with)([A-Z]\w*)\(")
CALL = re.compile(r"\b(\w+)\.((?:record|enqueue|queue)\w*)\(")
LOCAL_CALL = re.compile(r"(?<![.\w])(\w+)\(")
NEW_ENTRY = re.compile(r"new StackEntry\(")
NEW_CHOICE = re.compile(r"new (\w*ChoiceContext\.\w+|PendingMayAbility|\w+TriggerTarget)\(")
REBUILD = re.compile(r"new (?!MayEffect\(|ArrayList|StackEntry|Permanent\(|SequenceEffect\(List\.copyOf)\w+Effect\(|"
                     r"triggeredEffect\(\)|effectFor\w*\(")
UNWRAP = re.compile(r"\.(wrapped|thenEffect|elseEffect|tokenEffect|innerEffect)\(\)")
IGNORED_SETTERS = {"Accessible"}

# Run 17z13e's hand classification (file, handler) -> label, where the heuristic's is wrong.
HAND = {
    # The life default's graveyard target choice with Emet-Selch's "target card" counts; the record is the default's
    # (the same `queueGraveyardTarget`), the heuristic compares it with the default's stack-entry branch.
    ("LifeGainTriggerCollectorService.java", "handleOpponentLifeLossCastFromGraveyard"): "ROUTE",
}

# The rows of step 15 that hold what is left (effect-slot-refactor.md, run 17z13e's Findings): the first matching
# watch prefix names the row a handler of that watch is in. The labels stay the heuristic's; the row reads them by hand.
ROWS = [
    ("TapWatch.ON_ANY_PLAYER_TAPS_LAND", "17z13e2"), ("TapWatch.ON_ANY_PLAYER_TAPS_PERMANENT_FOR_MANA", "17z13e2"),
    ("TapWatch.ON_CONTROLLER_TAPS_", "17z13e2"), ("TapWatch.ON_CONTROLLER_UNTAPS", "17z13e2"),
    ("DamageWatch.", "17z13e3"), ("CombatWatch.", "17z13e3"),
    ("CreatureDeathWatch.", "17z13e4"), ("Legacy.", "17z13e4"),
    ("ZoneChangeWatch.", "17z13e5"),
    ("LibraryWatch.", "17z13e6"),
    ("ActionWatch.", "17z13e7"), ("CounterWatch.", "17z13e7"), ("SpellAbilityWatch.", "17z13e7"),
    ("MiscWatch.", "17z13e7"), ("TapWatch.ON_ENCHANTED_PERMANENT_TAPPED", "17z13e7"),
]

# The rows done since run 17z13e (run 17z13e2's Findings): their watches have one default each and no class-keyed
# handler; `--summary` reports what is left on their watches, which must be none.
DONE_ROWS = {"17z13e2", "17z13e6"}

# The reader branches keyed by an effect's class (file, effect class) -> verdict. Row 17z13e's three (the your-upkeep
# `SurveilEffect`, the your-draw-step `MayEffect` and Haphazard Bombardment's branches) are gone; the rest are row
# 17z13e8's to read.
READERS = {}


def row_of(watch):
    return next((row for prefix, row in ROWS if watch.startswith(prefix)), "?")
READER_FILES = ["turn/StepTriggerService.java", "DrawService.java"]
READER_HIT = re.compile(r"instanceof (?:[\w.]+\.)?(\w+Effect)\b|(\w+Effect)\.class::isInstance")


def body_at(text, start):
    depth, i = 1, start
    while depth and i < len(text):
        depth += {"{": 1, "}": -1}.get(text[i], 0)
        i += 1
    return text[start:i]


def split_args(s):
    args, depth, cur = [], 0, ""
    for ch in s:
        if ch in "([{<":
            depth += 1
        elif ch in ")]}>":
            if depth == 0:
                break
            depth -= 1
        if ch == "," and depth == 0:
            args.append(cur.strip())
            cur = ""
        else:
            cur += ch
    args.append(cur.strip())
    return args


def methods_of(text):
    found = {}
    for m in METHOD.finditer(text):
        found.setdefault(m.group(1), []).append(body_at(text, m.end()))
    return found


def tokens(text, methods, name, seen=None, depth=0):
    seen = seen if seen is not None else set()
    if name in seen or depth > 5:
        return set(), False
    seen.add(name)
    out, rewrite = set(), False
    for body in methods.get(name, []):
        for kind, field in SETTER.findall(body):
            if field not in IGNORED_SETTERS:
                out.add(f"{kind}:{field[0].lower() + field[1:]}")
        for receiver, call in CALL.findall(body):
            if receiver not in ("this", "log") and call not in ("enqueueTrigger",):
                out.add(f"call:{call}")
        for m in NEW_ENTRY.finditer(body):
            args = split_args(body[m.end():])
            if len(args) >= 6 and args[5] != "null":
                out.add("ctor:target")
        for choice in NEW_CHOICE.findall(body):
            out.add(f"route:{choice.split('.')[-1]}")
        if REBUILD.search(body) or UNWRAP.search(body):
            rewrite = True
        for call in LOCAL_CALL.findall(body):
            if call in methods and call != name:
                sub, sub_rewrite = tokens(text, methods, call, seen, depth + 1)
                out |= sub
                rewrite = rewrite or sub_rewrite
    return out, rewrite


def handlers():
    """(file, watch, effect class, handler) for every registration."""
    rows = []
    for path in sorted(T.rglob("*.java")):
        text = path.read_text()
        for ann in ANNOTATION.finditer(text):
            fields = dict(re.findall(r"(\w+)\s*=\s*([\w.]+)", ann.group(2)))
            if "value" not in fields or "watch" not in fields:
                continue
            method = HANDLER.search(text, ann.end())
            rows.append((path, fields["watch"], fields["value"].removesuffix(".class"), method.group(1)))
        for m in DEATH.finditer(text):
            rows.append((path, "CreatureDeathWatch." + m.group(1), m.group(2), m.group(3) or m.group(4)))
        for block in LEGACY_BLOCK.finditer(text):
            for m in LEGACY.finditer(block.group(2)):
                rows.append((path, "Legacy." + block.group(1), m.group(1), m.group(2)))
    return rows


# What a handler records about the event, the source and the ability; a choice's wither records the same field its
# entry's setter does, and queueing a choice or a "you may" is routing.
QUEUEING = {"call:queueInteraction", "call:queueMayAbility", "call:queueMayAbilityForPlayer"}


def record(tok):
    return {t.replace("with:", "set:") for t in tok if not t.startswith("route:") and t not in QUEUEING}


def main():
    only = sys.argv[sys.argv.index("--watch") + 1] if "--watch" in sys.argv else None
    cache = {}
    by_watch = defaultdict(list)
    keys = Counter()
    for path, watch, effect, handler in handlers():
        if path not in cache:
            text = path.read_text()
            cache[path] = (text, methods_of(text))
        text, methods = cache[path]
        tok, rewrite = tokens(text, methods, handler)
        by_watch[watch].append((path.name, effect, handler, tok, rewrite))
        keys[(watch, effect)] += 1
    summary = Counter()
    by_row = Counter()
    lines = []
    for watch in sorted(by_watch):
        rows = by_watch[watch]
        defaults = [r for r in rows if r[1] == "CardEffect"]
        default_tok = record(defaults[0][3]) if defaults else None
        classed = [r for r in rows if r[1] != "CardEffect"]
        if not classed:
            continue
        sibling_sets = {frozenset(record(r[3])) for r in classed}
        for name, effect, handler, tok, rewrite in classed:
            if keys[(watch, effect)] > 1:
                label = "DUP"
            elif rewrite:
                label = "REWRITE"
            elif default_tok is not None:
                label = "ROUTE" if record(tok) == default_tok else "RECORD-GAP"
            else:
                label = "ROUTE" if len(sibling_sets) == 1 and len(classed) > 1 else "RECORD-GAP"
            label = HAND.get((name, handler), label)
            summary[label] += 1
            if label != "ROUTE":
                by_row[row_of(watch)] += 1
            if only and only not in watch:
                continue
            extra = sorted(record(tok) - default_tok) if default_tok is not None else sorted(record(tok))
            missing = sorted(default_tok - record(tok)) if default_tok is not None else []
            lines.append(f"{label:10} {watch} {effect} -> {name}.{handler}"
                         f"{'' if defaults else ' [no default]'} +{extra} -{missing}")
        if defaults and (not only or only in watch):
            lines.append(f"{'DEFAULT':10} {watch} -> {defaults[0][0]}.{defaults[0][2]} {sorted(default_tok)}")
    readers = Counter()
    for rel in READER_FILES:
        text = (S / rel).read_text()
        for m in READER_HIT.finditer(text):
            effect = m.group(1) or m.group(2)
            verdict = READERS.get((pathlib.Path(rel).name, effect), "UNREAD(17z13e8)")
            readers[verdict] += 1
            if not only:
                line = text.count("\n", 0, m.start()) + 1
                lines.append(f"READER     {rel}:{line} {effect}: {verdict}")
    if "--summary" in sys.argv:
        for label, count in sorted(summary.items()):
            print(f"{label}: {count}")
        for verdict, count in sorted(readers.items()):
            print(f"reader {verdict}: {count}")
        for row, count in sorted(by_row.items()):
            print(f"{'LEFT in done' if row in DONE_ROWS else 'open in'} row {row}: {count}")
        for row in sorted(DONE_ROWS - set(by_row)):
            print(f"done row {row}: 0 class-keyed handlers left")
        print("class-keyed handlers:", sum(summary.values()))
    else:
        print("\n".join(lines))


if __name__ == "__main__":
    main()
