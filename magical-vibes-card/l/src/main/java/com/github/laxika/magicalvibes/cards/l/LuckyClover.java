package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastWithAdventurePredicate;

@CardRegistration(set = "ELD", collectorNumber = "226")
public class LuckyClover extends Card {

    public LuckyClover() {
        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryCastWithAdventurePredicate()),
                new CopyTriggeringSpellEffect());
    }
}
