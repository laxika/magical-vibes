package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BlankPlanarDieRollsCauseChaosEffect;

@CardRegistration(set = "OPC2", collectorNumber = "1")
public class ChaoticAether extends Card {

    public ChaoticAether() {
        addEffect(EffectSlot.ENCOUNTER_TRIGGERED, new BlankPlanarDieRollsCauseChaosEffect());
    }
}
