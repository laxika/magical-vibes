package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "APC", collectorNumber = "99")
public class FlowstoneCharger extends Card {

    public FlowstoneCharger() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(3, -3));
    }
}
