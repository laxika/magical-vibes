package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "22")
public class CoastalDrake extends Card {

    private static final PermanentAllOfPredicate KAVU_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.KAVU)
    ));

    public CoastalDrake() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(ReturnToHandEffect.target()),
                "{1}{U}, {T}: Return target Kavu to its owner's hand.",
                new PermanentPredicateTargetFilter(KAVU_CREATURE, "Target must be a Kavu creature")
        ));
    }
}
