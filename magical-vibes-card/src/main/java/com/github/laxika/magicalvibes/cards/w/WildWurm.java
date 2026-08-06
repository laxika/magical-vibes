package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandOnCoinFlipLossEffect;

@CardRegistration(set = "TMP", collectorNumber = "212")
public class WildWurm extends Card {

    public WildWurm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnSelfToHandOnCoinFlipLossEffect());
    }
}
