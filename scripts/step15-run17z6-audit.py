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
                if effect == "CardEffect":
                    continue
                body = method_body(text, handler)
                kinds = [kind for kind, regex in (("REBUILD", REBUILD), ("UNWRAP", UNWRAP), ("BIND", BIND))
                         if regex.search(body)] or ["ROUTE"]
                for kind in kinds:
                    summary[(path.name, kind)] += 1
                rows.append(f"{path.relative_to(E)}: {effect} -> {handler}: {'+'.join(kinds)}")
    if "--summary" in sys.argv:
        for (name, kind), count in sorted(summary.items()):
            print(f"{name}: {kind} {count}")
        print("handlers:", len(rows))
    else:
        print("\n".join(rows))


if __name__ == "__main__":
    main()
