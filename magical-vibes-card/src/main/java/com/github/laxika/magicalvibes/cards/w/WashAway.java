package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTruePredicate;

@CardRegistration(set = "VOW", collectorNumber = "87")
public class WashAway extends Card {

    public WashAway() {
        addCastingOption(AlternateHandCast.cleave("{1}{U}{U}",
                new StackEntryPredicateTargetFilter(new StackEntryTruePredicate(),
                        "Target must be a spell.")));
        target(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(new StackEntryCastFromZonePredicate(Zone.HAND)),
                "Target must be a spell that wasn't cast from its owner's hand."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
