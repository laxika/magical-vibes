package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMU", collectorNumber = "218")
public class ShannaPurifyingBlade extends Card {

    public ShannaPurifyingBlade() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                UnlessPaysEffect.youMayPayX("{X}", new LifeGainedThisTurn(CountScope.CONTROLLER),
                        new DrawCardEffect(new XValue())));
    }
}
