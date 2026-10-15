package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "268")
public class GideonsCompany extends Card {

    public GideonsCompany() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));

        PermanentPredicate gideonPlaneswalker = new PermanentAllOfPredicate(List.of(
                new PermanentIsPlaneswalkerPredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.GIDEON)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(PutCounterOnTargetPermanentEffect.withTargetRestriction(
                        CounterType.LOYALTY, 1, gideonPlaneswalker)),
                "{3}{W}: Put a loyalty counter on target Gideon planeswalker.",
                new PermanentPredicateTargetFilter(gideonPlaneswalker,
                        "Target must be a Gideon planeswalker")
        ));
    }
}
