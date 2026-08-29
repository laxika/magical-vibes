package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Deals damage to any target and, when a creature actually receives damage, removes a keyword
 * from that creature until end of turn. The creature is also marked to be exiled instead of dying
 * this turn when {@code exileInsteadOfDie} is true.
 */
public record DealDamageToAnyTargetThenRemoveKeywordIfDamagedEffect(
        DynamicAmount damage, Keyword keyword, boolean exileInsteadOfDie)
        implements DamageDealingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public boolean canDamageCreatures() {
        return true;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
