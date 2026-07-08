package com.github.laxika.magicalvibes.model.effect;

/**
 * Adds {@code amount} mana of any one color (the controller chooses a single color), spendable only
 * to cast instant and sorcery spells. Used by the mana ability Resonating Lute grants to lands.
 */
public record AwardAnyOneColorInstantSorceryOnlyManaEffect(int amount) implements ManaProducingEffect {

    public AwardAnyOneColorInstantSorceryOnlyManaEffect() {
        this(1);
    }
}
