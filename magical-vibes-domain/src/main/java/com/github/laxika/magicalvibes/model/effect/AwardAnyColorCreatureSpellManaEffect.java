package com.github.laxika.magicalvibes.model.effect;

/**
 * Produces one mana of any color (player chooses) that can only be spent to cast a creature
 * spell (of any type). Unlike {@link AwardAnyColorChosenSubtypeCreatureManaEffect}, the mana is
 * usable for every creature spell rather than one chosen creature type. Used by Ancient Ziggurat.
 */
public record AwardAnyColorCreatureSpellManaEffect() implements ManaProducingEffect {

    @Override
    public int estimatedWildcardMana() {
        return 1;
    }
}
