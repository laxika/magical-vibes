package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;

@CardRegistration(set = "ZEN", collectorNumber = "130")
public class HellfireMongrel extends Card {

    public HellfireMongrel() {
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new DealDamageIfFewCardsInHandEffect(2, 2));
    }
}
