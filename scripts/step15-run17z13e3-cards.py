#!/usr/bin/env python3
"""Step 15, run 17z13e3: the damage-to-a-player cards write the trigger conditions their class-keyed handlers judged.

The damage-to-a-player watches have one default each now (`DamageTriggerCollectorService.handlePlayerDamageDefault`,
`handleOpponentSourceDamageDefault`), which records the event whatever the effects are. What the deleted handlers judged
from an effect's class or fields becomes the card's trigger, its `withFilter` or its event condition:

- Talon of Pain's "a source you control other than this artifact" (`PutCountersOnSelfEffect.excludeDamageSource`, gone):
  the event condition `NotCondition(TriggeringPermanentMatches(PermanentIsSourcePermanentPredicate))` (a spell source has
  no triggering permanent and counts);
- Flesh Reaver's "whenever this creature deals damage to … an opponent" (the `DealDamageToPlayersEffect` handler's
  "the source is this" check): the trigger's filter `PermanentIsSourcePermanentPredicate`;
- Dragonborn Champion's "5 or more damage" (`DrawCardIfEventValueAtLeastEffect`, which the handler unwrapped by class):
  the event condition `EventValueAtLeast(5)` with `DrawCardEffect(1)`;
- Niv-Mizzet, Visionary's and Chandra's Incinerator's "whenever a source you control deals noncombat damage to an
  opponent" (written as "an opponent is dealt noncombat damage", the "you control" judged by their effects' handlers):
  `Triggers.sourceYouControlDealsNoncombatDamageToOpponent()`;
- Pain Magnification's "whenever an opponent is dealt 3 or more damage by a single source" (written as an intervening
  "if"): the event condition `EventValueAtLeast(3)`.

Idempotent. Usage: step15-run17z13e3-cards.py [LETTER...]  (run per letter module: c d f n p t)
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

IMPORTS = {
    "TriggerOptions": f"{PKG}.model.trigger.TriggerOptions",
    "NotCondition": f"{PKG}.model.condition.NotCondition",
    "TriggeringPermanentMatches": f"{PKG}.model.condition.TriggeringPermanentMatches",
    "EventValueAtLeast": f"{PKG}.model.condition.EventValueAtLeast",
    "PermanentIsSourcePermanentPredicate": f"{PKG}.model.filter.PermanentIsSourcePermanentPredicate",
    "DrawCardEffect": f"{PKG}.model.effect.DrawCardEffect",
}
UNUSED = {
    "DrawCardIfEventValueAtLeastEffect": f"{PKG}.model.effect.DrawCardIfEventValueAtLeastEffect",
}

RULES = {
    "TalonOfPain": [(
        re.compile(r"addTrigger\(Triggers\.sourceYouControlDealsDamageToOpponent\(\),\s*"
                   r"new PutCountersOnSelfEffect\(CounterType\.CHARGE, true\)\);"),
        "addTrigger(Triggers.sourceYouControlDealsDamageToOpponent(),\n"
        "                TriggerOptions.DEFAULT.withEventCondition(new NotCondition(\n"
        "                        new TriggeringPermanentMatches(new PermanentIsSourcePermanentPredicate()))),\n"
        "                new PutCountersOnSelfEffect(CounterType.CHARGE));")],
    "FleshReaver": [(
        re.compile(r"Triggers\.sourceYouControlDealsDamageToOpponent\(\)\)"),
        "Triggers.sourceYouControlDealsDamageToOpponent()\n"
        "                                .withFilter(new PermanentIsSourcePermanentPredicate()))")],
    "DragonbornChampion": [(
        re.compile(r"addTrigger\(Triggers\.sourceYouControlDealsDamageToPlayer\(\),\s*"
                   r"new DrawCardIfEventValueAtLeastEffect\(5\)\);"),
        "addTrigger(Triggers.sourceYouControlDealsDamageToPlayer(),\n"
        "                TriggerOptions.DEFAULT.withEventCondition(new EventValueAtLeast(5)),\n"
        "                new DrawCardEffect(1));")],
    "NivMizzetVisionary": [(
        re.compile(r"Triggers\.opponentDealtNoncombatDamage\(\)"),
        "Triggers.sourceYouControlDealsNoncombatDamageToOpponent()")],
    "ChandrasIncinerator": [(
        re.compile(r"Triggers\.opponentDealtNoncombatDamage\(\)"),
        "Triggers.sourceYouControlDealsNoncombatDamageToOpponent()")],
    "PainMagnification": [(
        re.compile(r"TriggerOptions\.DEFAULT\.withInterveningIf\(new EventValueAtLeast\(3\)\)"),
        "TriggerOptions.DEFAULT.withEventCondition(new EventValueAtLeast(3))")],
}


def add_import(text, fqcn):
    """Inserts {@code import fqcn;} in order among the file's imports of the same root package."""
    line = f"import {fqcn};\n"
    lines = text.splitlines(keepends=True)
    imports = [i for i, l in enumerate(lines) if l.startswith("import ")]
    root = fqcn.split(".")[0]
    same = [i for i in imports if lines[i][7:].split(".")[0] == root]
    after = [i for i in same if lines[i] < line]
    at = (after[-1] + 1) if after else same[0]
    lines.insert(at, line)
    return "".join(lines)


def fix_imports(text):
    body = text[text.index("\n@"):]
    for name, fqcn in UNUSED.items():
        if not re.search(r"\b" + name + r"\b", body):
            text = text.replace(f"import {fqcn};\n", "")
    for name, fqcn in IMPORTS.items():
        if re.search(r"\b" + name + r"\b", body) and f"import {fqcn};" not in text:
            text = add_import(text, fqcn)
    return text


def convert(path):
    rules = RULES.get(path.stem)
    if not rules:
        return False
    text = path.read_text()
    new = text
    for pattern, replacement in rules:
        new = pattern.sub(replacement, new)
    if new == text:
        return False
    path.write_text(fix_imports(new))
    return True


def main():
    letters = sys.argv[1:] or [p.name for p in sorted((ROOT / "magical-vibes-card").iterdir())
                               if (p / "src/main/java").is_dir()]
    changed = []
    for letter in letters:
        base = ROOT / "magical-vibes-card" / letter / "src/main/java"
        for path in sorted(base.rglob("*.java")):
            if convert(path):
                changed.append(path.stem)
    print(f"{len(changed)} card classes changed: {' '.join(changed)}")


if __name__ == "__main__":
    main()
