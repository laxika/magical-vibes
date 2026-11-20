package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record GrantEffectEffect(CardEffect effect, PermanentSubject subject, PermanentPredicate filter)
        implements CardEffect {

    public GrantEffectEffect(CardEffect effect, PermanentSubject subject) {
        this(effect, subject, null);
    }
}
