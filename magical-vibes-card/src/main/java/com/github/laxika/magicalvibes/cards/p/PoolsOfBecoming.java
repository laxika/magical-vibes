package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutHandOnBottomOfLibraryAndDrawEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopPlanarCardsAndTriggerChaosEffect;

@CardRegistration(set = "OHOP", collectorNumber = "30")
public class PoolsOfBecoming extends Card {

    public PoolsOfBecoming() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new PutHandOnBottomOfLibraryAndDrawEffect());
        addEffect(EffectSlot.CHAOS_TRIGGERED, new RevealTopPlanarCardsAndTriggerChaosEffect(3));
    }
}
