#!/usr/bin/env python3
"""Step 14, run 8 of effect-slot-refactor.md: every engine and AI reader of a damage trigger reads its abilities
through the legacy view (TriggeredAbility.legacyEffects(), found by the trigger's watchKey()), which gives the
collectors the intervening "if", the limit, the damage filter and "once for each source" as the wrappers the cards
wrote before the cards moved them into TriggerOptions and the trigger's filter. Run 7's reader script
(step14-combat-readers.py) did the same for the combat watches.

- `x.getTemporaryTriggerEffects(DamageWatch.Y.trigger())` / `getPersistentTriggerEffects(…)` become their
  `getLegacy…` forms, and so do the readers that take the watch as a `DamageWatch watch` parameter or loop over
  watches with a `for (DamageWatch watch : …)` (`watch.trigger()`);
- `gameQueryService.getEffectiveGraveyardTriggerEffects(…, DamageWatch.Y.trigger())` becomes
  `getEffectiveGraveyardLegacyTriggerEffects`;
- `….hasTrigger(DamageWatch.Y.trigger())` becomes `hasWatchedTrigger`;
- in a loop over the registered global abilities that reads a damage watch, `watcher.effect()` becomes
  `watcher.legacyEffect()`.

Usage: python3 scripts/step14-damage-readers.py
"""
import importlib.util
import os
import re
import sys

_here = os.path.dirname(__file__)
_spec = importlib.util.spec_from_file_location("combat_readers", os.path.join(_here, "step14-combat-readers.py"))
combat = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(combat)

ROOTS = combat.ROOTS
DAMAGE = r"DamageWatch\.[A-Z_]+\.trigger\(\)"
SIMPLE = [
    (re.compile(r"\.getTemporaryTriggerEffects\((\s*%s)" % DAMAGE), r".getLegacyTemporaryTriggerEffects(\1"),
    (re.compile(r"\.getPersistentTriggerEffects\((\s*%s)" % DAMAGE), r".getLegacyPersistentTriggerEffects(\1"),
    (re.compile(r"\.getEffectiveGraveyardTriggerEffects\(([^;]*?%s)" % DAMAGE, re.S),
     r".getEffectiveGraveyardLegacyTriggerEffects(\1"),
    (re.compile(r"\.hasTrigger\((\s*%s)" % DAMAGE), r".hasWatchedTrigger(\1"),
]
# Methods that take the watch as a `DamageWatch watch` parameter and read `watch.trigger()`.
PARAMETER = [
    (re.compile(r"\.getTemporaryTriggerEffects\(watch\.trigger\(\)\)"), ".getLegacyTemporaryTriggerEffects(watch.trigger())"),
    (re.compile(r"\.getPersistentTriggerEffects\(watch\.trigger\(\)\)"), ".getLegacyPersistentTriggerEffects(watch.trigger())"),
]
METHOD = re.compile(r"\n    (?:private|public|protected)[^\n;{]*\(([^)]*)\)\s*\{", re.S)
WATCH_LOOP = re.compile(r"for \(DamageWatch watch : [^)]*\)\s*\{")


def parameter_readers(src):
    """Rewrites the `watch.trigger()` readers inside the methods with a `DamageWatch watch` parameter and inside the
    loops over damage watches."""
    out, pos = [], 0
    for m in METHOD.finditer(src):
        if not re.search(r"\bDamageWatch watch\b", m.group(1)):
            continue
        end = combat.block_end(src, m.end())
        body = src[m.end():end]
        for pattern, replacement in PARAMETER:
            body = pattern.sub(replacement, body)
        out.append(src[pos:m.end()] + body)
        pos = end
    out.append(src[pos:])
    src = "".join(out)
    out, pos = [], 0
    for m in WATCH_LOOP.finditer(src):
        end = combat.block_end(src, m.end())
        body = src[m.end():end]
        for pattern, replacement in PARAMETER:
            body = pattern.sub(replacement, body)
        out.append(src[pos:m.end()] + body)
        pos = end
    out.append(src[pos:])
    return "".join(out)


def global_loops(src):
    out, pos = [], 0
    for m in combat.LOOP.finditer(src):
        end = combat.block_end(src, m.end())
        body = src[m.end():end]
        if "DamageWatch." not in body:
            continue
        var = m.group(1)
        new = re.sub(r"\b%s\.effect\(\)" % var, var + ".legacyEffect()", body)
        out.append(src[pos:m.end()] + new)
        pos = end
    out.append(src[pos:])
    return "".join(out)


def main():
    total = 0
    for root_dir in ROOTS:
        for root, _, files in os.walk(root_dir):
            for name in files:
                if not name.endswith(".java"):
                    continue
                path = os.path.join(root, name)
                src = open(path).read()
                new = src
                for pattern, replacement in SIMPLE:
                    new = pattern.sub(replacement, new)
                new = parameter_readers(new)
                new = global_loops(new)
                if new != src:
                    open(path, "w").write(new)
                    total += 1
                    print(path)
    print("%d files rewritten" % total, file=sys.stderr)


if __name__ == "__main__":
    main()
