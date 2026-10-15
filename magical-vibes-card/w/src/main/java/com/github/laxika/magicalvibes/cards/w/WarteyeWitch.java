package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "MH1", collectorNumber = "115")
public class WarteyeWitch extends Card {

    public WarteyeWitch() {
        ScryEffect deathEffect = new ScryEffect(1);
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, deathEffect);
        addEffect(EffectSlot.ON_DEATH, deathEffect);
    }
}
