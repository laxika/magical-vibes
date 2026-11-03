package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "197")
@CardRegistration(set = "SLD", collectorNumber = "1276")
@CardRegistration(set = "2XM", collectorNumber = "203")
@CardRegistration(set = "EOC", collectorNumber = "118")
@CardRegistration(set = "BRC", collectorNumber = "126")
public class JhoiraWeatherlightCaptain extends Card {

    public JhoiraWeatherlightCaptain() {
        // Whenever you cast a historic spell, draw a card.
        // (Artifacts, legendaries, and Sagas are historic.)
        addTrigger(Triggers.youCastSpell().withFilter(new CardIsHistoricPredicate()), new DrawCardEffect());
    }
}
