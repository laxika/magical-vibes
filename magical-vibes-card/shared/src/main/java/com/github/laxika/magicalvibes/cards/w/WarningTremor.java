package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

public class WarningTremor extends Card {

    public WarningTremor() {
        addSpellEffect(DealDamageEffect.toAnyTarget(2));
        addSpellEffect(new ReduceCastCostForNextMatchingSpellEffect(
                new CardSubtypePredicate(CardSubtype.DRAGON), 2));
    }
}
