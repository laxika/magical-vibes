package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsAttachedToOpposingCreaturesEffect;

@CardRegistration(set = "YMKM", collectorNumber = "11")
public class PerforatorCrocodile extends Card {

    public PerforatorCrocodile() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConjureCardsAttachedToOpposingCreaturesEffect("Stab Wound"));
        addCycling("{1}{B}");
    }
}
