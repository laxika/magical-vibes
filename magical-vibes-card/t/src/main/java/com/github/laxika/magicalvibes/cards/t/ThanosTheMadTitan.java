package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseManaValueParityAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import java.util.List;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasChosenParityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "MSH", collectorNumber = "233")
public class ThanosTheMadTitan extends Card {

    public ThanosTheMadTitan() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{C}{W}{U}{B}{R}{G}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new ChooseManaValueParityAtResolutionEffect(),
                        new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                                new PermanentHasChosenParityPredicate(PermanentHasChosenParityPredicate.Quality.MANA_VALUE))))),
                "Power-up — {C}{W}{U}{B}{R}{G}: Put two +1/+1 counters on Thanos. Choose odd or even. "
                        + "Destroy each other creature with mana value of the chosen quality. Activate each "
                        + "power-up ability only once. Reduce the cost by his mana cost if he entered this turn."
        ).withPowerUp());
    }
}
