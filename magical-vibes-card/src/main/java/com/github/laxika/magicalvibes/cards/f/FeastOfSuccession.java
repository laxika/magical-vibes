package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "CMM", collectorNumber = "158")
public class FeastOfSuccession extends Card {

    public FeastOfSuccession() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-4, -4));
        addEffect(EffectSlot.SPELL, new BecomeMonarchEffect());
    }
}
