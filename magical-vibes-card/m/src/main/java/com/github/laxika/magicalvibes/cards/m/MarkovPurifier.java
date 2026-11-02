package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "241")
@CardRegistration(set = "DBL", collectorNumber = "508")
public class MarkovPurifier extends Card {

    public MarkovPurifier() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new GainedLifeThisTurn(),
                new MayPayManaEffect("{2}", new DrawCardEffect(1), "Pay {2} to draw a card?")));
    }
}
