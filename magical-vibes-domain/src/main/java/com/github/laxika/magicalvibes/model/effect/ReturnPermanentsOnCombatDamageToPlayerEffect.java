package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record ReturnPermanentsOnCombatDamageToPlayerEffect(PermanentPredicate filter) implements CardEffect {

    public ReturnPermanentsOnCombatDamageToPlayerEffect() {
        this(null);
    }
}
