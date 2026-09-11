package com.github.laxika.magicalvibes.model.effect;

/** Sets the other creature attacking with the source's base power and toughness until end of turn. */
public record SetBasePowerToughnessOfOtherAttackingCreatureEffect(int power, int toughness)
        implements OtherAttackingCreatureReferenceEffect {
}
