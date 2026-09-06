package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCardFromHandToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "LEG", collectorNumber = "185")
public class Eureka extends Card {

    public Eureka() {
        addEffect(EffectSlot.SPELL, new EachPlayerMayPutCardFromHandToBattlefieldEffect(
                new CardIsPermanentPredicate(), "permanent", false, true, true));
    }
}
