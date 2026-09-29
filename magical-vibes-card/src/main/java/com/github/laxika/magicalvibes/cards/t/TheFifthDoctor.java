package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "127")
public class TheFifthDoctor extends Card {

    public TheFifthDoctor() {
        PermanentPredicate peacefulCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentAttackedThisTurnPredicate()),
                new PermanentNotPredicate(new PermanentEnteredBattlefieldThisTurnPredicate())));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, SequenceEffect.of(
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, peacefulCreature),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, peacefulCreature)));
    }
}
