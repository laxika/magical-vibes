package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

@CardRegistration(set = "M20", collectorNumber = "49")
public class BorealElemental extends Card {

    public BorealElemental() {
        addStaticEffect(new IncreaseOpponentCostForTargetingControlledPermanentEffect(
                new PermanentIsSourcePermanentPredicate(), 2, false));
    }
}
