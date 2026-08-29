package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestOpponentHandSize;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "SOK", collectorNumber = "91")
public class AdamaroFirstToDesire extends Card {

    public AdamaroFirstToDesire() {
        GreatestOpponentHandSize opponentHandSize = new GreatestOpponentHandSize();
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(opponentHandSize, opponentHandSize));
    }
}
