package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "SHM", collectorNumber = "176")
public class SyggRiverCutthroat extends Card {

    public SyggRiverCutthroat() {
        // At the beginning of each end step, if an opponent lost 3 or more life this turn,
        // you may draw a card. (Damage causes loss of life.)
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new OpponentLostLifeThisTurn(3),
                new MayEffect(new DrawCardEffect(1), "Draw a card?")));
    }
}
