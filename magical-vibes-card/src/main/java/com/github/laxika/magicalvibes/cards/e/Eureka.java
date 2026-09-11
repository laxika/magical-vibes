package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCardFromHandToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "LEG", collectorNumber = "185")
@CardRegistration(set = "ME1", collectorNumber = "117")
@CardRegistration(set = "VMA", collectorNumber = "208")
public class Eureka extends Card {

    public Eureka() {
        addEffect(EffectSlot.SPELL, new EachPlayerMayPutCardFromHandToBattlefieldEffect(
                new CardIsPermanentPredicate(), "permanent", false, true, true));
    }
}
