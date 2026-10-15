package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExchangeTargetPlayersLifeTotalsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "XLN", collectorNumber = "3")
public class AxisOfMortality extends Card {

    public AxisOfMortality() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new ExchangeTargetPlayersLifeTotalsEffect(),
                "You may have two target players exchange life totals."
        ));
    }
}
