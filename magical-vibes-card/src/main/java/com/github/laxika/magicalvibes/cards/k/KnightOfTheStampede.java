package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "RIX", collectorNumber = "138")
public class KnightOfTheStampede extends Card {

    public KnightOfTheStampede() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.DINOSAUR), 2, CostModificationScope.SELF));
    }
}
