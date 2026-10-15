package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "281")
public class AjaniWiseCounselor extends Card {

    public AjaniWiseCounselor() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new GainLifeEffect(new PermanentCount(
                        new PermanentIsCreaturePredicate(), CountScope.CONTROLLER))),
                "+2: You gain 1 life for each creature you control."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new BoostAllOwnCreaturesEffect(2, 2)),
                "−3: Creatures you control get +2/+2 until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(new PutCounterOnTargetPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, new ControllerLifeTotal())),
                "−9: Put X +1/+1 counters on target creature, where X is your life total.",
                TargetFilters.creature()
        ));
    }
}
