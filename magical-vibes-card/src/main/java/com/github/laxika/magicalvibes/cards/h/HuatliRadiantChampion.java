package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DrawOnControlledCreatureEntersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "159")
public class HuatliRadiantChampion extends Card {

    public HuatliRadiantChampion() {
        PermanentCount creaturesYouControl = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);

        // +1: Put a loyalty counter on Huatli for each creature you control.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new PutCountersOnSelfEffect(CounterType.LOYALTY, creaturesYouControl)),
                "+1: Put a loyalty counter on Huatli for each creature you control."
        ));

        // −1: Target creature gets +X/+X until end of turn, where X is the number of creatures
        // you control.
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new BoostTargetCreatureEffect(creaturesYouControl, creaturesYouControl)),
                "−1: Target creature gets +X/+X until end of turn, where X is the number of creatures you control.",
                TargetFilters.creature()
        ));

        // −8: You get an emblem with "Whenever a creature you control enters, you may draw a card."
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new CreateEmblemEffect(
                        List.of(new DrawOnControlledCreatureEntersEffect()),
                        "Whenever a creature you control enters, you may draw a card.")),
                "−8: You get an emblem with \"Whenever a creature you control enters, you may draw a card.\""
        ));
    }
}
