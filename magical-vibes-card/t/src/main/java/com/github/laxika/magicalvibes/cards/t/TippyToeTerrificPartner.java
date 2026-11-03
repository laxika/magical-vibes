package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.AddFoodTokenToTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "734")
public class TippyToeTerrificPartner extends Card {

    public TippyToeTerrificPartner() {
        addStaticEffect(new AddFoodTokenToTokenCreationEffect());
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new GainedLifeThisTurn()),
                new DrawCardEffect(1));
    }
}
