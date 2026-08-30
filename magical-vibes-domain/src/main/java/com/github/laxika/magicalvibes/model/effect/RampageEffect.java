package com.github.laxika.magicalvibes.model.effect;

/**
 * When the source creature becomes blocked, it gets +N/+N until end of turn for each creature
 * blocking it beyond the first.
 */
public record RampageEffect(int bonusPerAdditionalBlocker) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
