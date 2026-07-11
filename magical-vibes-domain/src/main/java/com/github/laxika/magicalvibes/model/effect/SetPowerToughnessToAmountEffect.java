package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Characteristic-defining ability (CDA) that sets a creature's power and toughness to
 * dynamically computed amounts ("CARDNAME's power and toughness are each equal to …").
 *
 * <p>Applied as a continuous self bonus on a 0/0 base: the {@code power}/{@code toughness}
 * {@link DynamicAmount}s are evaluated by the engine's {@code AmountEvaluationService} and
 * added on top of the base P/T (layer 7b of the layer system), before counters and other
 * boosts. This replaces the former per-derivation {@code PowerToughnessEqualTo*} family.</p>
 */
public record SetPowerToughnessToAmountEffect(DynamicAmount power, DynamicAmount toughness) implements CardEffect {

    @Override
    public boolean isPowerToughnessDefining() { return true; }
}
