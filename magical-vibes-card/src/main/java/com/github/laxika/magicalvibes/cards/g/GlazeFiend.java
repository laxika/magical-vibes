package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "ALA", collectorNumber = "77")
public class GlazeFiend extends Card {

    public GlazeFiend() {
        // Whenever another artifact you control enters, this creature gets +2/+2 until end of turn.
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, new BoostSelfEffect(2, 2));
    }
}
