package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "40K", collectorNumber = "26")
public class SisterOfSilence extends Card {

    public SisterOfSilence() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(StackEntryType.abilitiesAnd(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                "Target must be an instant or sorcery spell, or an activated or triggered ability."
        )).addTrigger(Triggers.enters(ObjectRelation.THIS), new CounterSpellEffect());
    }
}
