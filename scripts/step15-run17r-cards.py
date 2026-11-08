#!/usr/bin/env python3
"""Step 15, run 17r: the life and time-counter abilities' trigger-event intervening "if"s are event conditions.

The life collectors judged an ability's intervening "if" as it triggered and pushed its effect unwrapped
(`unwrapLifeChangeConditional` and the life `ConditionalEffect` handlers), and so did the time-counter removal handler.
Each ability below was written with an intervening "if" that is a condition of the trigger event or one the ability
exists under, so it is the ability's event condition (`withEventCondition`), judged as the ability triggers and not
again as it resolves, as the collectors already treated it:

- "during your turn" / "for the first time during each of your turns" (a condition of the event, not an "if"): Cat
  Collector, Kefka, Ruler of Ruin, Moonstone Harbinger, Shadowheart, Sharran Cleric's specialized abilities, Vampire
  Scrivener (both), Wax-Wane Witness;
- a Class's level (CR 716.2a: "As long as this Class is level N or greater, it has [abilities]"): Cleric Class,
  Gourmand's Talent (both), Intermediate Chirography;
- vanishing's "When the last time counter is removed from this permanent, sacrifice it" (CR 702.63a: the last counter's
  removal is the trigger event): Aven Riftwatcher, Calciderm, Chronozoa, Regenerations Restored. Their upkeep
  ability's "if this permanent has a time counter on it" is a real intervening "if" and stays.

Idempotent.

Usage: step15-run17r-cards.py <letter module dir>...
"""
import pathlib
import re
import sys

LAST_TIME_COUNTER = "new NotCondition(new SourceCounterThreshold(1, CounterType.TIME))"

CONDITIONS = {
    "CatCollector": [("new ControllerTurn()", 1)],
    "KefkaRulerOfRuin": [("new ControllerTurn()", 1)],
    "MoonstoneHarbinger": [("new ControllerTurn()", 1)],
    "ShadowheartSharranCleric": [("new ControllerTurn()", 1)],
    "VampireScrivener": [("new ControllerTurn()", 2)],
    "WaxWaneWitness": [("new ControllerTurn()", 1)],
    "ClericClass": [("new SourceClassLevelAtLeast(2)", 1)],
    "GourmandsTalent": [("new SourceClassLevelAtLeast(2)", 1), ("new SourceClassLevelAtLeast(3)", 1)],
    "IntermediateChirography": [("new SourceCounterThreshold(1, CounterType.LEVEL)", 1)],
    "AvenRiftwatcher": [(LAST_TIME_COUNTER, 1)],
    "Calciderm": [(LAST_TIME_COUNTER, 1)],
    "Chronozoa": [(LAST_TIME_COUNTER, 1)],
    "RegenerationsRestored": [(LAST_TIME_COUNTER, 1)],
}


TEXT = {
    "ShadowheartSharranCleric": ('black, which has none. "During your turn" is the ability\'s intervening "if".',
                                 'black, which has none. "During your turn" is the ability\'s event condition.'),
}


def convert(source, condition, expected):
    pattern = re.compile(r"\.withInterveningIf\((\s*)" + re.escape(condition))
    converted = re.compile(r"\.withEventCondition\(\s*" + re.escape(condition))
    found = len(pattern.findall(source))
    if found == 0:
        if len(converted.findall(source)) != expected:
            raise SystemExit(f"condition not found: {condition}")
        return source
    if found != expected:
        raise SystemExit(f"{found} abilities with {condition}, expected {expected}")
    return pattern.sub(lambda m: ".withEventCondition(" + m.group(1) + condition, source)


def main():
    changed = []
    seen = set()
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).glob("src/main/java/**/*.java")):
            conditions = CONDITIONS.get(path.stem)
            if conditions is None:
                continue
            seen.add(path.stem)
            source = path.read_text()
            result = source
            for condition, expected in conditions:
                result = convert(result, condition, expected)
            if path.stem in TEXT:
                old, new = TEXT[path.stem]
                if old in result:
                    result = result.replace(old, new)
                elif new not in result:
                    raise SystemExit(f"text not found in {path.stem}: {old}")
            if result != source:
                path.write_text(result)
                changed.append(path.stem)
    print(f"{len(changed)} card classes changed: {' '.join(changed)}")
    print(f"{len(seen)} card classes seen")


if __name__ == "__main__":
    main()
