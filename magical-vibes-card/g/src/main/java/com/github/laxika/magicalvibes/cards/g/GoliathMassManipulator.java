package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "766")
public class GoliathMassManipulator extends Card {

    public GoliathMassManipulator() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new DrawCardEffect(new PermanentCount(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentPowerAtLeastPredicate(4)
                                )),
                                CountScope.CONTROLLER
                        ))
                ),
                "Power-up — {4}{G}: Put two +1/+1 counters on Goliath. Then draw a card for each creature you control "
                        + "with power 4 or greater. Activate each power-up ability only once. Reduce the cost by his "
                        + "mana cost if he entered this turn."
        ).withPowerUp());
    }
}
