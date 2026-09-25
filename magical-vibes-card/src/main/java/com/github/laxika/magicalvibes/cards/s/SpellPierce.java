package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "81")
@CardRegistration(set = "ZEN", collectorNumber = "67")
@CardRegistration(set = "DFT", collectorNumber = "64")
@CardRegistration(set = "NEO", collectorNumber = "80")
@CardRegistration(set = "MM3", collectorNumber = "51")
@CardRegistration(set = "MP2", collectorNumber = "17")
@CardRegistration(set = "SLD", collectorNumber = "41")
@CardRegistration(set = "2X2", collectorNumber = "63")
@CardRegistration(set = "SLZ", collectorNumber = "30")
@CardRegistration(set = "SLZ", collectorNumber = "151")
@CardRegistration(set = "SLZ", collectorNumber = "272")
@CardRegistration(set = "SOA", collectorNumber = "23")
@CardRegistration(set = "SLD", collectorNumber = "2388")
public class SpellPierce extends Card {

    public SpellPierce() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))
                ),
                "Target must be a noncreature spell."
        ))
                .addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(2));
    }
}
