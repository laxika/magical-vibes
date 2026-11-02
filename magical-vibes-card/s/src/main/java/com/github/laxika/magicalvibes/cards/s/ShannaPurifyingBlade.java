package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.effect.PayXManaDrawXCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMU", collectorNumber = "218")
public class ShannaPurifyingBlade extends Card {

    public ShannaPurifyingBlade() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new PayXManaDrawXCardsEffect(new LifeGainedThisTurn(CountScope.CONTROLLER)));
    }
}
