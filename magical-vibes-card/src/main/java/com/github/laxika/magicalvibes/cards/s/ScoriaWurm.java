package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandOnCoinFlipLossEffect;

@CardRegistration(set = "10E", collectorNumber = "227")
public class ScoriaWurm extends Card {

    public ScoriaWurm() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ReturnSelfToHandOnCoinFlipLossEffect());
    }
}
