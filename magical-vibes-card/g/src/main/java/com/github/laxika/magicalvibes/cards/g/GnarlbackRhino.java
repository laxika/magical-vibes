package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "300")
@CardRegistration(set = "2X2", collectorNumber = "148")
@CardRegistration(set = "PIO", collectorNumber = "176")
public class GnarlbackRhino extends Card {

    public GnarlbackRhino() {
        // Whenever you cast a spell that targets this creature, draw a card.
        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryTargetsSourcePredicate()),
                new DrawCardEffect(1));
    }
}
