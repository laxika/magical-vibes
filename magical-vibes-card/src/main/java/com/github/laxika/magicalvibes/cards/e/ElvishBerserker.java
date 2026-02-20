package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerBlockingCreatureEffect;

@CardRegistration(set = "10E", collectorNumber = "260")
public class ElvishBerserker extends Card {

    public ElvishBerserker() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfPerBlockingCreatureEffect(1, 1));
    }
}
