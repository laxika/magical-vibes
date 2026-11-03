package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastWithAdventurePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "313")
public class StorytellerPixie extends Card {

    public StorytellerPixie() {
        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryCastWithAdventurePredicate()),
                new DrawCardEffect());
    }
}
