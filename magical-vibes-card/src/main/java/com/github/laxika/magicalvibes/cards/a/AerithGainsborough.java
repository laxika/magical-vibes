package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "4")
@CardRegistration(set = "FIN", collectorNumber = "374")
@CardRegistration(set = "FIN", collectorNumber = "423")
@CardRegistration(set = "FIN", collectorNumber = "519")
public class AerithGainsborough extends Card {

    public AerithGainsborough() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new PutCountersOnSourceEffect(1, 1, 1));
        addEffect(EffectSlot.ON_DEATH, new PutCounterOnEachMatchingPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                        new PermanentControlledBySourceControllerPredicate()
                )),
                EachPermanentScope.ALL_PLAYERS));
    }
}
