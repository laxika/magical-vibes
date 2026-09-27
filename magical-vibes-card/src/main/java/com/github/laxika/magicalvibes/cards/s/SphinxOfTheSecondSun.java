package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalBeginningPhaseEffect;

@CardRegistration(set = "SLD", collectorNumber = "1720")
public class SphinxOfTheSecondSun extends Card {

    public SphinxOfTheSecondSun() {
        addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED, new AdditionalBeginningPhaseEffect(true));
    }
}
