#!/usr/bin/env python3
"""Step 15, run 17o: "when this land enters untapped" is the ability's event condition.

Five lands (Dwarven Mine, Gingerbread Cabin, Idyllic Grange, Mystic Sanctuary,
Witch's Cottage) wrote "enters untapped" as an intervening "if" (`SourceUntapped`), which `EtbEffectResolver` judged
by its class and unwrapped, "sampled at entry". It is part of the trigger event (CR 603.4: no "if" follows the trigger
condition), so it is the event condition: judged as the land enters and not again as the ability resolves, when the
land may have been tapped for mana since.

- Mythos of Illuna's copy gains "When this permanent enters, if it's a creature, it fights up to one target creature you
  don't control": an intervening "if" (CR 603.4) the card wrote as an effect's `ConditionalEffect`, which
  `EtbEffectResolver` judged by its class; it is the granted ability's intervening "if".

Idempotent.

Usage: step15-run17o-cards.py <letter module dir>...
"""
import pathlib
import sys

MYTHOS_BEFORE = """        ConditionalEffect fightEffect = new ConditionalEffect(
                new SourceIsCreature(), new EnteringCreatureFightsTargetCreatureEffect());
        CreateTokenCopyOfTargetPermanentEffect enhancedCopy =
                CreateTokenCopyOfTargetPermanentEffect.withAdditionalEffects(
                        false, List.of(new TriggeredAbility(Triggers.enters(ObjectRelation.THIS), fightEffect)));"""
MYTHOS_AFTER = """        EnteringCreatureFightsTargetCreatureEffect fightEffect = new EnteringCreatureFightsTargetCreatureEffect();
        TriggerOptions ifCreature = TriggerOptions.DEFAULT.withInterveningIf(new SourceIsCreature());
        CreateTokenCopyOfTargetPermanentEffect enhancedCopy =
                CreateTokenCopyOfTargetPermanentEffect.withAdditionalEffects(
                        false, List.of(new TriggeredAbility(Triggers.enters(ObjectRelation.THIS), ifCreature,
                                fightEffect)));"""

CARDS = {"DwarvenMine", "GingerbreadCabin", "IdyllicGrange", "MysticSanctuary", "WitchsCottage"}


def main():
    changed = []
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).glob("src/main/java/**/*.java")):
            if path.stem not in CARDS and path.stem != "MythosOfIlluna":
                continue
            source = path.read_text()
            result = source.replace(".withInterveningIf(new SourceUntapped())", ".withEventCondition(new SourceUntapped())")
            if path.stem == "MythosOfIlluna" and MYTHOS_BEFORE in result:
                result = result.replace(MYTHOS_BEFORE, MYTHOS_AFTER).replace(
                        ".addTrigger(Triggers.enters(ObjectRelation.THIS), fightEffect);",
                        ".addTrigger(Triggers.enters(ObjectRelation.THIS), ifCreature, fightEffect);")
                if "import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;" not in result:
                    result = result.replace("import com.github.laxika.magicalvibes.model.trigger.Triggers;",
                                            "import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;\nimport com.github.laxika.magicalvibes.model.trigger.Triggers;")
                body = result.split("public class", 1)[1]
                if "ConditionalEffect" not in body.replace("ConditionalReplacementEffect", ""):
                    result = result.replace("import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;\n", "")
            if result != source:
                path.write_text(result)
                changed.append(path.stem)
    print(f"{len(changed)} card classes changed: {' '.join(changed)}")


if __name__ == "__main__":
    main()
