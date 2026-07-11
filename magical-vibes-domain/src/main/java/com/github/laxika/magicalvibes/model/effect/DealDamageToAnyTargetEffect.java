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
 * @param targetGroup        which target the damage applies to: {@code -1} (default) resolves
 *                           against the entry's single target ({@code targetId}); {@code >= 0}
 *                           resolves against that target group's chosen targets (see
 *                           {@code StackEntry.targetsForGroup} — for cards without
 *                           {@code target(...)} declarations the index addresses the flat
 *                           additional-target list, e.g. Goblin Barrage's kicked target)
 */
public record DealDamageToAnyTargetEffect(DynamicAmount damage, boolean cantRegenerate,
                                          boolean exileInsteadOfDie, int targetGroup) implements CardEffect {

    public DealDamageToAnyTargetEffect(DynamicAmount damage, boolean cantRegenerate, boolean exileInsteadOfDie) {
        this(damage, cantRegenerate, exileInsteadOfDie, -1);
    }

    public DealDamageToAnyTargetEffect(int damage) {
        this(new Fixed(damage), false, false, -1);
    }

    public DealDamageToAnyTargetEffect(int damage, boolean cantRegenerate) {
        this(new Fixed(damage), cantRegenerate, false, -1);
    }

    public DealDamageToAnyTargetEffect(DynamicAmount damage) {
        this(damage, false, false, -1);
    }

    /** Damage aimed at the given target group's chosen target instead of the entry's single target. */
    public static DealDamageToAnyTargetEffect forTargetGroup(int damage, int targetGroup) {
        return new DealDamageToAnyTargetEffect(new Fixed(damage), false, false, targetGroup);
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
