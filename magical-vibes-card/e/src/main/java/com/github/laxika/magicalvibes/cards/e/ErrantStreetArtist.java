package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsCopyPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "41")
public class ErrantStreetArtist extends Card {

    public ErrantStreetArtist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(new CopySpellEffect()),
                "{1}{U}, {T}: Copy target spell you control that wasn't cast. You may choose new targets for the copy.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryControlledByPredicate(),
                                new StackEntryIsCopyPredicate()
                        )),
                        "Target must be a spell you control that wasn't cast."
                )
        ));
    }
}
