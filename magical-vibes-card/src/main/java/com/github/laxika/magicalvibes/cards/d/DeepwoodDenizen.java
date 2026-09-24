package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCounterSum;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "155")
public class DeepwoodDenizen extends Card {

    public DeepwoodDenizen() {
        PermanentCounterSum plusOneCountersOnCreaturesYouControl = new PermanentCounterSum(
                CounterType.PLUS_ONE_PLUS_ONE,
                new PermanentIsCreaturePredicate(),
                CountScope.CONTROLLER);

        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}{G}",
                List.of(new ReduceActivationCostEffect(plusOneCountersOnCreaturesYouControl), new DrawCardEffect(1)),
                "{5}{G}, {T}: Draw a card. This ability costs {1} less to activate for each +1/+1 counter on creatures you control."
        ));
    }
}
