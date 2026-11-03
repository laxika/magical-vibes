package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "80")
@CardRegistration(set = "THB", collectorNumber = "310")
public class WavebreakHippocamp extends Card {

    public WavebreakHippocamp() {
        // Whenever you cast your first spell during each opponent's turn, draw a card.
        addTrigger(Triggers.youCastSpell().duringOpponentsTurn().nthThisTurn(1), new DrawCardEffect(1));
    }
}
