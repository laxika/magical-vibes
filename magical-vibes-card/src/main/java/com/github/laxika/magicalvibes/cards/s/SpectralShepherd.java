package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "41")
public class SpectralShepherd extends Card {

    public SpectralShepherd() {
        // {1}{U}: Return target Spirit you control to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(ReturnToHandEffect.target()),
                "{1}{U}: Return target Spirit you control to its owner's hand.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.SPIRIT),
                        "Target must be a Spirit you control")));
    }
}
