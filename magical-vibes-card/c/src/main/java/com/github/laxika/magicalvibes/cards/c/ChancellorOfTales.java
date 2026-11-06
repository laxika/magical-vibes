package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastWithAdventurePredicate;

@CardRegistration(set = "WOE", collectorNumber = "45")
public class ChancellorOfTales extends Card {

    public ChancellorOfTales() {
        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryCastWithAdventurePredicate()),
                new MayEffect(new CopyTriggeringSpellEffect(), "Copy that spell?"));
    }
}
