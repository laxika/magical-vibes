package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseManaValueParityAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasChosenParityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "206")
@CardRegistration(set = "DSC", collectorNumber = "15")
@CardRegistration(set = "DSC", collectorNumber = "46")
public class ZimonesHypothesis extends Card {

    public ZimonesHypothesis() {
        addSpellEffect(new MayEffect(
                new PutCounterOnChosenPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1,
                        new PermanentIsCreaturePredicate()),
                "Put a +1/+1 counter on a creature?"));
        addSpellEffect(new ChooseManaValueParityAtResolutionEffect());
        addSpellEffect(ReturnToHandEffect.allPermanentsMatching(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasChosenParityPredicate(PermanentHasChosenParityPredicate.Quality.POWER)))));
    }
}
