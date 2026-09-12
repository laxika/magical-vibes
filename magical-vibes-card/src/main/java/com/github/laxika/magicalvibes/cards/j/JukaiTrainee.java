package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "NEO", collectorNumber = "196")
public class JukaiTrainee extends Card {

    public JukaiTrainee() {
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(1, 1));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(1, 1));
    }
}
