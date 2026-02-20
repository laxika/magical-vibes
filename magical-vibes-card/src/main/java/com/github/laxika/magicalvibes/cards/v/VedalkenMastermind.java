package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "123")
public class VedalkenMastermind extends Card {

    public VedalkenMastermind() {
        addActivatedAbility(new ActivatedAbility(
            true,
            "{U}",
            List.of(new ReturnTargetPermanentToHandEffect()),
            true,
            "{U}, {T}: Return target permanent you control to its owner's hand.",
            new ControlledPermanentPredicateTargetFilter(
                    new PermanentTruePredicate(),
                    "Target must be a permanent you control"
            )
        ));
    }
}
