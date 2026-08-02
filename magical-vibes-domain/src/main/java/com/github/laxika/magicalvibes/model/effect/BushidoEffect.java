package com.github.laxika.magicalvibes.model.effect;

/**
 * A Bushido ability with the printed numeric value. The combat trigger slots that represent
 * Bushido use this effect so other cards can distinguish Bushido from unrelated block triggers.
 */
public record BushidoEffect(int amount) implements CardEffect {

    public BoostSelfEffect asBoost() {
        return new BoostSelfEffect(amount, amount);
    }
}
