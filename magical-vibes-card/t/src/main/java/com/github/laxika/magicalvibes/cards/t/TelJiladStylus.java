package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "260")
public class TelJiladStylus extends Card {

    public TelJiladStylus() {
        // {T}: Put target permanent you own on the bottom of your library.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutTargetOnBottomOfLibraryEffect()),
                "{T}: Put target permanent you own on the bottom of your library.",
                new OwnedPermanentPredicateTargetFilter(
                        new PermanentTruePredicate(),
                        "Target must be a permanent you own"
                )
        ));
    }
}
