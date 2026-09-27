package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesMasterOfCeremoniesEffect;

@CardRegistration(set = "SLD", collectorNumber = "2379")
public class MasterOfCeremonies extends Card {

    public MasterOfCeremonies() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new EachOpponentChoosesMasterOfCeremoniesEffect());
    }
}
