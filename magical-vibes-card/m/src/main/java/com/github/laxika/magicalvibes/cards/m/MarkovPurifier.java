package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "241")
@CardRegistration(set = "DBL", collectorNumber = "508")
public class MarkovPurifier extends Card {

    public MarkovPurifier() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new GainedLifeThisTurn()),
                UnlessPaysEffect.youMayPay("{2}", new DrawCardEffect(1)).withPrompt("Pay {2} to draw a card?"));
    }
}
