package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PainsRewardEffect;

@CardRegistration(set = "SOK", collectorNumber = "85")
public class PainsReward extends Card {

    public PainsReward() {
        addEffect(EffectSlot.SPELL, new PainsRewardEffect());
    }
}
