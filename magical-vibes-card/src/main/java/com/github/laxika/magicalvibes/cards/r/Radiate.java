package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPermanentOrPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsAnyPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TOR", collectorNumber = "113")
public class Radiate extends Card {

    public Radiate() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryTypeInPredicate(Set.of(
                                StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                        new StackEntryIsSingleTargetPredicate(),
                        new StackEntryAnyOfPredicate(List.of(
                                new StackEntryTargetsPermanentPredicate(new PermanentTruePredicate()),
                                new StackEntryTargetsAnyPlayerPredicate()))
                )),
                "Target must be an instant or sorcery spell that targets only one permanent or player."
        )).addEffect(EffectSlot.SPELL, new CopySpellForEachOtherPermanentOrPlayerEffect());
    }
}
