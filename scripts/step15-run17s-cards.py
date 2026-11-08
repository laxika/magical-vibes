#!/usr/bin/env python3
"""Step 15, run 17s: the conditional-judging collector sites' cards.

- Hellish Rebuke (AFC): "When this permanent deals damage to the player who cast Hellish Rebuke" is the granted
  ability's event condition, `TargetPlayerIs` (the player dealt damage, as the damage collectors judge it; its null
  player filled with the caster as the spell resolves), not the `DamagedPlayerOnlyEffect` wrapper the damage collectors
  unwrapped.
- Curse of the Cabal (TSP): "you may sacrifice a permanent. If you do, put two time counters on this card" is the
  recorded sacrifice count's `ConditionalEffect(new EventValueAtLeast(1), ...)` (as Leviathan and Gisa's Favorite
  Shovel write it), not a gate on the stack entry's event value inside `PutTimeCountersOnSuspendedCardEffect`'s
  handler, which made the suspended-card cast handler set an event value for Pardic Dragon.

Idempotent.

Usage: step15-run17s-cards.py <letter module dir>...
"""
import pathlib
import sys

EDITS = {
    "HellishRebuke": [
        ("import com.github.laxika.magicalvibes.model.effect.DamagedPlayerOnlyEffect;\n",
         "import com.github.laxika.magicalvibes.model.condition.TargetPlayerIs;\n"),
        ("import com.github.laxika.magicalvibes.model.trigger.Triggers;\n",
         "import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;\n"
         "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n"),
        ("""                        new TriggeredAbility(Triggers.dealsDamageToPlayer(),
                        new DamagedPlayerOnlyEffect(null,
                                SequenceEffect.of(new SacrificeSelfEffect(), new LoseLifeEffect(2)))),
""",
         """                        new TriggeredAbility(Triggers.dealsDamageToPlayer(),
                                TriggerOptions.DEFAULT.withEventCondition(new TargetPlayerIs(null)),
                                SequenceEffect.of(new SacrificeSelfEffect(), new LoseLifeEffect(2))),
"""),
    ],
    "CurseOfTheCabal": [
        ("import com.github.laxika.magicalvibes.model.condition.SourceCardSuspended;\n",
         "import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;\n"
         "import com.github.laxika.magicalvibes.model.condition.SourceCardSuspended;\n"
         "import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;\n"),
        ("""                                new PutTimeCountersOnSuspendedCardEffect(2)),
""",
         """                                new ConditionalEffect(new EventValueAtLeast(1),
                                        new PutTimeCountersOnSuspendedCardEffect(2))),
"""),
    ],
}

DONE_MARKERS = {
    "HellishRebuke": "new TargetPlayerIs(null)",
    "CurseOfTheCabal": "new ConditionalEffect(new EventValueAtLeast(1),",
}


def main():
    changed = []
    seen = set()
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).glob("src/main/java/**/*.java")):
            edits = EDITS.get(path.stem)
            if edits is None:
                continue
            seen.add(path.stem)
            source = path.read_text()
            if DONE_MARKERS[path.stem] in source:
                continue
            result = source
            for old, new in edits:
                if result.count(old) != 1:
                    raise SystemExit(f"{path}: expected one occurrence of {old!r}")
                result = result.replace(old, new)
            path.write_text(result)
            changed.append(str(path))
    for path in changed:
        print("converted", path)
    print(f"{len(changed)} converted, {len(seen)} seen")


if __name__ == "__main__":
    main()
