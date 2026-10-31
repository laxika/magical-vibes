package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

public class WarningTremor extends Card {

    public WarningTremor() {
        addSpellEffect(new DealDamageToAnyTargetEffect(2));
        addSpellEffect(new ReduceCastCostForNextMatchingSpellEffect(
                new CardSubtypePredicate(CardSubtype.DRAGON), 2));
    }
}
