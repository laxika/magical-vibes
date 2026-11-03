#!/usr/bin/env python3
"""Step 14, run 7 of effect-slot-refactor.md: every engine and AI reader of a combat trigger reads its abilities
through the legacy view (TriggeredAbility.legacyEffects(), found by the trigger's watchKey()), which gives the
collectors the intervening "if", the limit, the combat filter and the multiplicity as the wrappers and trigger modes
the cards wrote before the cards moved them into TriggerOptions and the trigger's filter.

- `x.getTemporaryTriggerEffects(CombatWatch.Y.trigger())` / `getPersistentTriggerEffects(…)` become their
  `getLegacy…` forms;
- `gameQueryService.getEffectiveGraveyardTriggerEffects(…, CombatWatch.Y.trigger())` becomes
  `getEffectiveGraveyardLegacyTriggerEffects`;
- `….hasTrigger(CombatWatch.Y.trigger())` becomes `hasWatchedTrigger`;
- in a loop over the registered global abilities that reads a combat watch, `watcher.effect()` becomes
  `watcher.legacyEffect()`;
- the few readers that name a combat factory directly (`hasTrigger(Triggers.attacks())`) find it by watch key.

Usage: python3 scripts/step14-combat-readers.py
"""
import os
import re
import sys

ROOTS = ["magical-vibes-engine/src/main/java", "magical-vibes-ai/src/main/java"]
COMBAT = r"CombatWatch\.[A-Z_]+\.trigger\(\)"
SIMPLE = [
    (re.compile(r"\.getTemporaryTriggerEffects\((\s*%s)" % COMBAT), r".getLegacyTemporaryTriggerEffects(\1"),
    (re.compile(r"\.getPersistentTriggerEffects\((\s*%s)" % COMBAT), r".getLegacyPersistentTriggerEffects(\1"),
    (re.compile(r"\.getEffectiveGraveyardTriggerEffects\(([^;]*?%s)" % COMBAT, re.S),
     r".getEffectiveGraveyardLegacyTriggerEffects(\1"),
    (re.compile(r"\.hasTrigger\((\s*%s)" % COMBAT), r".hasWatchedTrigger(\1"),
]
DIRECT = {
    "TokenCardFactory.java": [("triggered.hasTrigger(Triggers.attacks())", "triggered.hasWatchedTrigger(Triggers.attacks())")],
    "ExileDyingCreatureCardAndCreateTokenEffectHandler.java": [
        ("triggered.hasTrigger(Triggers.dealsCombatDamageToPlayer())",
         "triggered.hasWatchedTrigger(Triggers.dealsCombatDamageToPlayer())")],
    "RegisterGlobalTriggeredAbilityUntilNextTurnEffectHandler.java": [
        ("ability.hasTrigger(Triggers.creatureAttacksYouOrYourPlaneswalker())",
         "ability.hasWatchedTrigger(Triggers.creatureAttacksYouOrYourPlaneswalker())")],
}
LOOP = re.compile(r"for \((?:final )?(?:var|TemporaryGlobalTriggeredAbility) (\w+) : (?:List\.copyOf\()?"
                  r"gameData\.temporaryGlobalTriggeredAbilities\)?\)\s*\{")


def block_end(src, i):
    """Index just past the brace that closes the block opened just before i."""
    depth = 1
    while depth:
        c = src[i]
        if c == '"':
            i = re.compile(r'(?<!\\)"').search(src, i + 1).start()
        depth += (c == "{") - (c == "}")
        i += 1
    return i


def global_loops(src):
    out, pos = [], 0
    for m in LOOP.finditer(src):
        end = block_end(src, m.end())
        body = src[m.end():end]
        if "CombatWatch." not in body:
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
                new = global_loops(new)
                for old, rep in DIRECT.get(name, []):
                    if old not in new:
                        raise SystemExit("no %r in %s" % (old, path))
                    new = new.replace(old, rep)
                if new != src:
                    open(path, "w").write(new)
                    total += 1
                    print(path)
    print("%d files rewritten" % total, file=sys.stderr)


if __name__ == "__main__":
    main()
