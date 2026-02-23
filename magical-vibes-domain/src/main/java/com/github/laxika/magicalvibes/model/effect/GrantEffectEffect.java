package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record GrantEffectEffect(CardEffect effect, GrantScope scope, PermanentPredicate filter) implements CardEffect {

    public GrantEffectEffect(CardEffect effect, GrantScope scope) {
        this(effect, scope, null);
    }
}
