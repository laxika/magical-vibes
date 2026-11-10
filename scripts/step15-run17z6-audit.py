#!/usr/bin/env python3
"""Step 15, run 17z6: the "done" audit's class-keyed handler scan.

Run 17z2 found that `step15-run17u-audit.py`'s `List.of(new ...)` pattern misses the rebuilds made through
`triggeredEffect()`, factories, binding interfaces and class-keyed handlers. This scan reads every handler the trigger
collectors pick by an effect's class: the `@Collects...Trigger(value = X.class, ...)` methods,
`DeathTriggerCollectorService.registerCreatureDeath(..., X.class, ...)` and `LegacyTriggerBindings.register(h, X.class,
...)`, skipping the `CardEffect.class` defaults. For each it prints the effect class and what the handler body does
with it:

- REBUILD: constructs an effect (`new ...Effect(`) or calls a record's `triggeredEffect()` / `effectFor...(`;
- UNWRAP: reads a wrapper's inner effect (`.wrapped()`, `.thenEffect()`, `.elseEffect()`, `.tokenEffect()`) to put it on
  the stack;
- BIND: records a value read from the effect's fields onto the entry (`entry.set...(effect.` / `(e.`);
- ROUTE: none of these, so the class only picks the target choice or queue the ability goes through.

The labels are a heuristic, not a classification: BIND only sees a value read from the effect's own fields, so a handler
that records event data (a target, value, snapshot or triggering object) the watch's `CardEffect.class` default doesn't
record is labelled ROUTE, and the defaults, which may branch on a class inside, are skipped. Run 17z12's independent
verification read the handlers by hand against their defaults; the ones it found binding, rebuilding or unwrapping by
class are listed in `OPEN` with their row of step 15 and printed as `OPEN(row)` whatever the heuristic says.

Read-only. Usage: step15-run17z6-audit.py [--summary]
"""
import pathlib
import re
import sys
from collections import Counter

ROOT = pathlib.Path(__file__).resolve().parent.parent
E = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service"
ANNOTATED = re.compile(r"@Collects\w*Trigger\(\s*value\s*=\s*(\w+)\.class[^)]*\)\s*(?:@Collects\w*Trigger\([^)]*\)\s*)*"
                       r"(?:private |public |protected )?(?:static )?boolean (\w+)\(", re.S)
DEATH = re.compile(r"registerCreatureDeath\(\s*CreatureDeathWatch\.\w+,\s*(\w+)\.class,.*?->\s*(\w+)\(", re.S)
LEGACY = re.compile(r"\bregister\(h,\s*(\w+)\.class,\s*this::(\w+)\)")
REBUILD = re.compile(r"new (?!MayEffect\(|ArrayList|StackEntry|Permanent\()\w+Effect\(|triggeredEffect\(\)|effectFor\w*\(")
UNWRAP = re.compile(r"\.(wrapped|thenEffect|elseEffect|tokenEffect|innerEffect)\(\)")
BIND = re.compile(r"\.set\w+\((?:effect|e|may|mayPay|marker|trigger)\.")

# Run 17z12: handlers that record event data only for their class, rebuild or unwrap the card's effect (the rows of
# step 15 that hold them; effect-slot-refactor.md, run 17z12's Findings). Defaults are named too. Run 17z12b closed the
# enter and turned-face-up collectors' handlers (they were deleted or record the entering permanent whatever the effects
# are); the may-pay handler's "that creature" target and source bindings are row 17z12f's. Run 17z12c closed the damage
# and death collectors' handlers (deleted: the enchanted-creature, any-creature and any-creature-death watches' defaults
# record the event whatever the effects are). Run 17z12f closed the may-pay handler (the ability is the watcher's, the
# entering permanent recorded whatever the payload is, a targeted payload's target chosen as it goes on the stack) and the
# creature-enters-from-graveyard default (its targeted abilities are the watcher's, through the enters target choice).
# Run 17z12d closed the discard and milled-card handlers (deleted: the discard watches' defaults record the discard,
# the cards-put-into-a-graveyard defaults the cards with their graveyard stays, whatever the effects are; Hedge
# Shredder's and Colossal Grave-Reaver's "one or more land / creature cards" are trigger filters).
# Run 17z12e closed the last row: the seek default records the sought cards whatever the effects are (Lurker in the
# Deep's "during your turn" is its event condition), the "choose up to X" bound rides on the modal choice
# (`TriggeredModalTrigger.withModeCount`) instead of a rebuilt modal, C.A.M.P.'s target is its effect's and the
# never-written counter step of the enchanted-land mana sequence is gone. Run 17z13's independent check opened rows
# 17z13b to 17z13e; the handlers below are theirs (the reader sites of those rows are not handlers and aren't listed).
# Run 17z13b closed its row: the emblem death and artifact-graveyard handlers are gone with `@CollectsEmblemTrigger`
# (the emblems' abilities are `TriggeredAbility`s their readers judge). Run 17z13c closed its row: Storm Cauldron's
# land-tap handler is gone (the card writes `ReturnToHandEffect.triggering()`, which the land-tap default puts on the
# stack with the tapped land as its triggering permanent), and Relic Bind's handler is the enchanted-tap watch's
# `ChooseOneEffect` route (the card's own modal goes to the mode choice as written, ROUTE). Row 17z13e's sites are
# reader branches and handlers to scope first; none is listed here. Run 17z13e scoped them with
# `step15-run17z13e-audit.py`, which compares what each handler records with its watch's default (this scan's labels
# only see what a handler does to the effect): the life and enchanted-permanent-tapped handlers are gone, and the
# handlers left are that audit's rows 17z13e2 to 17z13e7, listed there by watch.
OPEN = {}


def method_body(text, name):
    match = re.search(r"(?:private |public |protected )?(?:static )?boolean " + name + r"\([^)]*\)\s*\{", text)
    if not match:
        return ""
    depth, i = 1, match.end()
    while depth and i < len(text):
        depth += {"{": 1, "}": -1}.get(text[i], 0)
        i += 1
    return text[match.end():i]


def main():
    summary = Counter()
    rows = []
    for path in sorted((E / "trigger").rglob("*.java")):
        text = path.read_text()
        for pattern in (ANNOTATED, DEATH, LEGACY):
            for match in pattern.finditer(text):
                effect, handler = match.group(1), match.group(2)
                row = OPEN.get((path.name, handler))
                if effect == "CardEffect" and row is None:
                    continue
                body = method_body(text, handler)
                kinds = [f"OPEN({row})"] if row else [
                    kind for kind, regex in (("REBUILD", REBUILD), ("UNWRAP", UNWRAP), ("BIND", BIND))
                    if regex.search(body)] or ["ROUTE"]
                for kind in kinds:
                    summary[(path.name, kind)] += 1
                rows.append(f"{path.relative_to(E)}: {effect} -> {handler}: {'+'.join(kinds)}")
    for (name, handler), row in OPEN.items():
        if not any(f"{name}: " in r and f"-> {handler}:" in r for r in rows):
            summary[(name, f"OPEN({row})")] += 1
            rows.append(f"trigger/{name}: (no class-keyed registration) -> {handler}: OPEN({row})")
    if "--summary" in sys.argv:
        for (name, kind), count in sorted(summary.items()):
            print(f"{name}: {kind} {count}")
        print("handlers:", len(rows))
    else:
        print("\n".join(rows))


if __name__ == "__main__":
    main()
