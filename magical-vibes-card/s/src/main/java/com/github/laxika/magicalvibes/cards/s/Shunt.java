package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "10E", collectorNumber = "233")
@CardRegistration(set = "DST", collectorNumber = "68")
public class Shunt extends Card {

    public Shunt() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(new StackEntryIsSingleTargetPredicate(),
                        new StackEntryNotPredicate(new StackEntryTypeInPredicate(Set.of(
                                StackEntryType.ACTIVATED_ABILITY, StackEntryType.TRIGGERED_ABILITY))))),
                "Target spell must have a single target."
        )).addEffect(EffectSlot.SPELL, new ChangeTargetOfTargetSpellWithSingleTargetEffect());
    }
}
