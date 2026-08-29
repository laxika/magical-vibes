package com.github.laxika.magicalvibes.cards.i;

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

@CardRegistration(set = "FEM", collectorNumber = "9")
@CardRegistration(set = "FEM", collectorNumber = "151")
public class IcatianLieutenant extends Card {

    private static final PermanentAllOfPredicate SOLDIER_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.SOLDIER)
    ));

    public IcatianLieutenant() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new BoostTargetCreatureEffect(1, 0, SOLDIER_CREATURE)),
                "{1}{W}: Target Soldier creature gets +1/+0 until end of turn.",
                new PermanentPredicateTargetFilter(SOLDIER_CREATURE, "Target must be a Soldier creature")
        ));
    }
}
