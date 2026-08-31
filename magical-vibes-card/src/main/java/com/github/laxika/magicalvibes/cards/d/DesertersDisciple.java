package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "131")
public class DesertersDisciple extends Card {

    public DesertersDisciple() {
        PermanentPredicate targetFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtMostPredicate(2),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MakeCreatureUnblockableEffect()),
                "{T}: Another target creature you control with power 2 or less can't be blocked this turn.",
                new ControlledPermanentPredicateTargetFilter(
                        targetFilter,
                        "Target must be another creature you control with power 2 or less"
                )
        ));
    }
}
