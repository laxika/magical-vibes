package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "176")
@CardRegistration(set = "ZNC", collectorNumber = "103")
public class SyggRiverCutthroat extends Card {

    public SyggRiverCutthroat() {
        // At the beginning of each end step, if an opponent lost 3 or more life this turn,
        // you may draw a card. (Damage causes loss of life.)
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new ConditionalEffect(
                new OpponentLostLifeThisTurn(3),
                new MayEffect(new DrawCardEffect(1), "Draw a card?")));
    }
}
