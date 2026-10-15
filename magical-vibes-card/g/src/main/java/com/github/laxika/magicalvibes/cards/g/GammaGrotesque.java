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
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "562")
public class GammaGrotesque extends Card {

    public GammaGrotesque() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{G}{G}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 3),
                        new DrawCardEffect(new PermanentCount(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasCountersPredicate(CounterType.ANY)
                                )),
                                CountScope.CONTROLLER
                        ))
                ),
                "Power-up — {4}{G}{G}: Put three +1/+1 counters on this creature. Then draw a card for each "
                        + "creature you control with a counter on it. Activate each power-up ability only once. "
                        + "Reduce the cost by its mana cost if it entered this turn."
        ).withPowerUp());
    }
}
