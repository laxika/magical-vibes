package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/** Grants the controller a one-shot alternative cost of paying life equal to the next spell's mana value. */
public record GrantPayLifeEqualToSpellManaValueForNextSpellEffect() implements NextMatchingSpellEffect {

    @Override
    public CardPredicate predicate() {
        return new CardTruePredicate();
    }
}
