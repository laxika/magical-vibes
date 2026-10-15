package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "301")
public class VivienOfTheArkbow extends Card {

    public VivienOfTheArkbow() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)),
                "+2: Put two +1/+1 counters on up to one target creature.",
                TargetFilters.creature(),
                +2, null, null,
                List.of(), 0, 1
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new TargetDealsPowerDamageToTargetEffect()),
                "\u22123: Target creature you control deals damage equal to its power to target creature "
                        + "you don't control.",
                null, -3, null, null,
                List.of(TargetFilters.creatureYouControl(), TargetFilters.creatureAnOpponentControls()),
                2, 2
        ));

        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(
                        new BoostAllOwnCreaturesEffect(4, 4),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES)
                ),
                "\u22129: Creatures you control get +4/+4 and gain trample until end of turn."
        ));
    }
}
