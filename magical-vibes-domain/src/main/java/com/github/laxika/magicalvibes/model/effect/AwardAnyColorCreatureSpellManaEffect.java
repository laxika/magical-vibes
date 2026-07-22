package com.github.laxika.magicalvibes.model.effect;

/**
 * Produces {@code amount} mana of any one color (player chooses) that can only be spent to cast a
 * creature spell (of any type). Unlike {@link AwardAnyColorChosenSubtypeCreatureManaEffect}, the
 * mana is usable for every creature spell rather than one chosen creature type. Used by Ancient
 * Ziggurat (1) and Somberwald Sage (3).
 */
public record AwardAnyColorCreatureSpellManaEffect(int amount) implements ManaProducingEffect {

    public AwardAnyColorCreatureSpellManaEffect() {
        this(1);
    }

    @Override
    public int estimatedWildcardMana() {
        return amount;
    }
}
