package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "127")
@CardRegistration(set = "WHO", collectorNumber = "413")
@CardRegistration(set = "WHO", collectorNumber = "556")
@CardRegistration(set = "WHO", collectorNumber = "732")
@CardRegistration(set = "WHO", collectorNumber = "1004")
@CardRegistration(set = "WHO", collectorNumber = "1147")
public class TheFifthDoctor extends Card {

    public TheFifthDoctor() {
        PermanentPredicate peacefulCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentAttackedThisTurnPredicate()),
                new PermanentNotPredicate(new PermanentEnteredBattlefieldThisTurnPredicate())));

        addTrigger(Triggers.endStep(ActivePlayer.YOU), SequenceEffect.of(
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, peacefulCreature),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, peacefulCreature)));
    }
}
