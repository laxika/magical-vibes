package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

@CardRegistration(set = "RNA", collectorNumber = "208")
public class SphinxOfNewPrahv extends Card {

    public SphinxOfNewPrahv() {
        addEffect(EffectSlot.STATIC, new IncreaseOpponentCostForTargetingControlledPermanentEffect(
                new PermanentIsSourcePermanentPredicate(), 2, false));
    }
}
