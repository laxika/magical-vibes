package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

@CardRegistration(set = "MH2", collectorNumber = "124")
public class FlameBlitz extends Card {

    public FlameBlitz() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new DealDamageToEachMatchingPermanentEffect(
                        5, new PermanentIsPlaneswalkerPredicate(), EachPermanentScope.ALL_PLAYERS));
        addCycling("{2}");
    }
}
