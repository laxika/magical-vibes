package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.ExchangeTargetPlayersLifeTotalsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "3")
public class AxisOfMortality extends Card {

    public AxisOfMortality() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new ExchangeTargetPlayersLifeTotalsEffect(),
                "You may have two target players exchange life totals."
        ));
    }
}
