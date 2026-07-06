package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to any target (creature, planeswalker, or player). The amount is a
 * {@link DynamicAmount} evaluated at resolution (fixed number, X paid, source's power,
 * counters on the source, …).
 *
 * @param damage             the amount of damage to deal
 * @param cantRegenerate     when true, a creature dealt damage this way can't regenerate
 *                           this turn (e.g. Incinerate)
 * @param exileInsteadOfDie  when true, a creature dealt damage this way that would die
 *                           this turn is exiled instead (replacement effect, CR 614.6 —
 *                           e.g. Red Sun's Zenith)
 */
public record DealDamageToAnyTargetEffect(DynamicAmount damage, boolean cantRegenerate,
                                          boolean exileInsteadOfDie) implements CardEffect {

    public DealDamageToAnyTargetEffect(int damage) {
        this(new Fixed(damage), false, false);
    }

    public DealDamageToAnyTargetEffect(int damage, boolean cantRegenerate) {
        this(new Fixed(damage), cantRegenerate, false);
    }

    public DealDamageToAnyTargetEffect(DynamicAmount damage) {
        this(damage, false, false);
    }

    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
