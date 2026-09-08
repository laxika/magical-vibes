package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "H09", collectorNumber = "24")
@CardRegistration(set = "SCG", collectorNumber = "139")
public class SliverOverlord extends Card {

    public SliverOverlord() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new SearchLibraryEffect(
                        new CardSubtypePredicate(CardSubtype.SLIVER),
                        LibrarySearchDestination.HAND)),
                "{3}: Search your library for a Sliver card, reveal that card, put it into your hand, then shuffle."
        ));

        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(GainControlOfTargetEffect.withTargetPredicate(ControlDuration.PERMANENT, sliver)),
                "{3}: Gain control of target Sliver.",
                new PermanentPredicateTargetFilter(sliver, "Target must be a Sliver")
        ));
    }
}
