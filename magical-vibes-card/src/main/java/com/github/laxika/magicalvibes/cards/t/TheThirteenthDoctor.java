package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayFromOutsideHandTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "4")
@CardRegistration(set = "WHO", collectorNumber = "448")
@CardRegistration(set = "WHO", collectorNumber = "195")
@CardRegistration(set = "WHO", collectorNumber = "564")
@CardRegistration(set = "WHO", collectorNumber = "609")
@CardRegistration(set = "WHO", collectorNumber = "1039")
@CardRegistration(set = "WHO", collectorNumber = "1155")
public class TheThirteenthDoctor extends Card {

    public TheThirteenthDoctor() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new PlayFromOutsideHandTriggerEffect(
                        List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                        TargetFilters.creature()));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasCountersPredicate(CounterType.ANY)))));
    }
}
