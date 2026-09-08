package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Static effect that makes every spell, or every matching spell, unable to be countered. */
public record SpellsCantBeCounteredEffect(CardPredicate predicate) implements CardEffect {

    public SpellsCantBeCounteredEffect() {
        this(null);
    }
}
