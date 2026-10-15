package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "USG", collectorNumber = "152")
public class RavenousSkirge extends Card {

    public RavenousSkirge() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(2, 0));
    }
}
