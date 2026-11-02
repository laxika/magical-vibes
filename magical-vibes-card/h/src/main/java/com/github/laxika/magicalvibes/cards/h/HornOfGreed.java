package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STH", collectorNumber = "135")
@CardRegistration(set = "SLD", collectorNumber = "2018")
@CardRegistration(set = "MAR", collectorNumber = "98")
public class HornOfGreed extends Card {

    public HornOfGreed() {
        // Whenever a player plays a land, that player draws a card.
        addTrigger(Triggers.youPlayLand(), new DrawCardEffect());
        addTrigger(Triggers.opponentPlaysLand(), new DrawCardForTargetPlayerEffect(1));
    }
}
