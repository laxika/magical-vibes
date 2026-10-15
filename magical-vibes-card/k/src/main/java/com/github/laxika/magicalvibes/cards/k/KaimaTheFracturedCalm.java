package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesAndPutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedBySourceControllerAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "NEC", collectorNumber = "3")
@CardRegistration(set = "NEC", collectorNumber = "74")
public class KaimaTheFracturedCalm extends Card {

    public KaimaTheFracturedCalm() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new GoadCreaturesAndPutCountersOnSourceEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                                new PermanentIsEnchantedBySourceControllerAuraPredicate()))));
    }
}
