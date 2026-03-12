package com.github.laxika.magicalvibes.model.effect;

/**
 * Put one or more +1/+1 counters on the creature enchanted by the source aura.
 *
 * @param count number of +1/+1 counters to place (default 1)
 */
public record PutPlusOnePlusOneCounterOnEnchantedCreatureEffect(int count) implements CardEffect {

    public PutPlusOnePlusOneCounterOnEnchantedCreatureEffect() {
        this(1);
    }
}
