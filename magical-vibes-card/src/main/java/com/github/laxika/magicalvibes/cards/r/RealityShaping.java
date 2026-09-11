package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCardFromHandToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "OPC2", collectorNumber = "6")
public class RealityShaping extends Card {

    public RealityShaping() {
        addEffect(EffectSlot.ENCOUNTER_TRIGGERED, new EachPlayerMayPutCardFromHandToBattlefieldEffect(
                new CardIsPermanentPredicate(), "permanent", false, false, true));
    }
}
