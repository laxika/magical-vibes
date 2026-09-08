package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Static replacement effect that doubles this permanent's combat damage to players. */
public record DoubleSelfCombatDamageToPlayersEffect() implements SourceDamageMultiplyingEffect {

    private static final PermanentPredicate SOURCE_FILTER = new PermanentIsSourcePermanentPredicate();

    @Override
    public int damageMultiplier() {
        return 2;
    }

    @Override
    public PermanentPredicate sourceFilter() {
        return SOURCE_FILTER;
    }

    @Override
    public boolean appliesToNonCombatDamage() {
        return false;
    }

    @Override
    public boolean appliesToCombatDamageTarget(Permanent target) {
        return target == null;
    }
}
