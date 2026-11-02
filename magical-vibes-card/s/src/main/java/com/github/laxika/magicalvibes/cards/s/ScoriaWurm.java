package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandOnCoinFlipLossEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "227")
@CardRegistration(set = "USG", collectorNumber = "212")
public class ScoriaWurm extends Card {

    public ScoriaWurm() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ReturnSelfToHandOnCoinFlipLossEffect());
    }
}
