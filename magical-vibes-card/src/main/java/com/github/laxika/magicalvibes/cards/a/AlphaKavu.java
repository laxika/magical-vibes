package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "77")
public class AlphaKavu extends Card {

    private static final PermanentAllOfPredicate KAVU_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.KAVU)
    ));

    public AlphaKavu() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}",
                List.of(new BoostTargetCreatureEffect(-1, 1, KAVU_CREATURE)),
                "{1}{G}: Target Kavu creature gets -1/+1 until end of turn.",
                new PermanentPredicateTargetFilter(KAVU_CREATURE, "Target must be a Kavu creature")
        ));
    }
}
