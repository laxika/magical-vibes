package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "105")
public class MirrorSheen extends Card {

    public MirrorSheen() {
        // {1}{U/R}{U/R}: Copy target instant or sorcery spell that targets you.
        // You may choose new targets for the copy.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U/R}{U/R}",
                List.of(new CopySpellEffect()),
                "{1}{U/R}{U/R}: Copy target instant or sorcery spell that targets you. You may choose new targets for the copy.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                                new StackEntryTargetsYouPredicate()
                        )),
                        "Target must be an instant or sorcery spell that targets you.")));
    }
}
