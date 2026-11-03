package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessLessThanBasicLandTypesAmongControlledLandsPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "227")
public class ZarOjanenScionOfEfrava extends Card {

    public ZarOjanenScionOfEfrava() {
        addTrigger(Triggers.becomesTapped(),
                new PutCounterOnEachMatchingPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentToughnessLessThanBasicLandTypesAmongControlledLandsPredicate()
                        )),
                        EachPermanentScope.ALL_PLAYERS));
    }
}
