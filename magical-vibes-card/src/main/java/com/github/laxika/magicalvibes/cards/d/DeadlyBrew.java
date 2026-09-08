package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesCreatureOrPlaneswalkerThenMayReturnAnotherPermanentEffect;

@CardRegistration(set = "STX", collectorNumber = "176")
public class DeadlyBrew extends Card {

    public DeadlyBrew() {
        addEffect(EffectSlot.SPELL,
                new EachPlayerSacrificesCreatureOrPlaneswalkerThenMayReturnAnotherPermanentEffect());
    }
}
