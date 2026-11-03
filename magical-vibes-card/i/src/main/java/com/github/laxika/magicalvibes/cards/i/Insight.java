package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "6ED", collectorNumber = "75")
@CardRegistration(set = "TMP", collectorNumber = "68")
public class Insight extends Card {

    public Insight() {
        // Whenever an opponent casts a green spell, you draw a card.
        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardColorPredicate(CardColor.GREEN)),
                new DrawCardEffect());
    }
}
