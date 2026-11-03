package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "27")
@CardRegistration(set = "M21", collectorNumber = "343")
@CardRegistration(set = "FCA", collectorNumber = "25")
@CardRegistration(set = "SOC", collectorNumber = "155")
@CardRegistration(set = "CMM", collectorNumber = "42")
@CardRegistration(set = "CMM", collectorNumber = "466")
@CardRegistration(set = "TDC", collectorNumber = "124")
@CardRegistration(set = "BLC", collectorNumber = "145")
public class MangaraTheDiplomat extends Card {

    public MangaraTheDiplomat() {
        // Whenever an opponent attacks with two or more creatures at you and/or your planeswalkers, draw a card.
        addTrigger(Triggers.playerAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new OpponentAttacksWithAtLeastCreatures(2)),
                new DrawCardEffect());

        // Whenever an opponent casts their second spell each turn, draw a card.
        addTrigger(Triggers.opponentCastsSpell().nthThisTurn(2), new DrawCardEffect());
    }
}
