package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "311")
public class RishadanPawnshop extends Card {

    public RishadanPawnshop() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new ShuffleTargetPermanentIntoLibraryEffect()),
                "{2}, {T}: Shuffle target nontoken permanent you control into its owner's library.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                        "Target must be a nontoken permanent you control"
                )
        ));
    }
}
