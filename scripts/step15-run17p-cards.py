#!/usr/bin/env python3
"""Step 15, run 17p: "when the last time counter is removed from this card while it's exiled" is the event condition.

Riftmarked Knight and Veiling Oddity wrote "the last time counter" as an intervening "if"
(`NotCondition(SourceCardSuspended)`), which `ExiledCardTriggerCollectorService` recognised by its classes, gated on
the event's remaining counters and unwrapped. It is part of the trigger event (CR 603.4: no "if" follows the trigger
condition), so it is the ability's event condition, which the time-counter collector judges from the exiled card as
the counter is removed.

- Summitfest Closing Ceremony's Covercast ("whenever you cast another instant or sorcery spell, if five or more mana was
  spent to cast it, this card intensifies", from the hand and the graveyard): an intervening "if" (CR 603.4) the card
  wrote as an effect's `ConditionalEffect`, which the zone-resident cast readers judged by its class
  (`spellManaSpentGatesMet`); it is the ability's intervening "if", judged with the spell's mana spent as X.

Idempotent.

Usage: step15-run17p-cards.py <letter module dir>...
"""
import pathlib
import sys

LAST_COUNTER_CARDS = {"RiftmarkedKnight", "VeilingOddity"}


SUMMITFEST_BEFORE = """        CardEffect covercast = new ConditionalEffect(
                new SpellManaSpentAtLeast(5),
                new IntensifySourceCardEffect(1));
        addTrigger(new TriggerCondition(Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                covercast);
        addTrigger(new TriggerCondition(Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY), AbilityZones.HAND),
                TriggerOptions.DEFAULT,
                covercast);"""
SUMMITFEST_AFTER = """        TriggerOptions fiveManaSpent = TriggerOptions.DEFAULT.withInterveningIf(new SpellManaSpentAtLeast(5));
        addTrigger(new TriggerCondition(Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY), AbilityZones.GRAVEYARD),
                fiveManaSpent,
                new IntensifySourceCardEffect(1));
        addTrigger(new TriggerCondition(Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY), AbilityZones.HAND),
                fiveManaSpent,
                new IntensifySourceCardEffect(1));"""


def convert(name, source):
    if name == "SummitfestClosingCeremony" and SUMMITFEST_BEFORE in source:
        source = source.replace(SUMMITFEST_BEFORE, SUMMITFEST_AFTER)
        for imp in ("import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;\n",
                    "import com.github.laxika.magicalvibes.model.effect.CardEffect;\n"):
            name_only = imp.rsplit(".", 1)[1].rstrip(";\n")
            if name_only not in source.split("public class", 1)[1]:
                source = source.replace(imp, "")
        return source
    if name in LAST_COUNTER_CARDS:
        source = source.replace(".withInterveningIf(new NotCondition(new SourceCardSuspended()))",
                                ".withEventCondition(new NotCondition(new SourceCardSuspended()))")
    return source


def main():
    changed = []
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).glob("src/main/java/**/*.java")):
            source = path.read_text()
            result = convert(path.stem, source)
            if result != source:
                path.write_text(result)
                changed.append(path.stem)
    print(f"{len(changed)} card classes changed: {' '.join(changed)}")


if __name__ == "__main__":
    main()
