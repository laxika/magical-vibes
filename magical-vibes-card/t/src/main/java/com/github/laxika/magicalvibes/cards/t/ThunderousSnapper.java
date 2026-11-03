package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "215")
public class ThunderousSnapper extends Card {

    public ThunderousSnapper() {
        // Whenever you cast a spell with mana value 5 or greater, draw a card.
        addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryNotPredicate(new StackEntryMaxManaValuePredicate(4))),
                new DrawCardEffect());
    }
}
