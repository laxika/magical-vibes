package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreaturePermanentlyEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "12")
public class IrmaPartTimeMutant extends Card {

    public IrmaPartTimeMutant() {
        target(0, 1).addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), SequenceEffect.of(
                new BecomeCopyOfTargetCreaturePermanentlyEffect(
                        "Irma, Part-Time Mutant",
                        Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new PermanentAllOfPredicate(List.of(
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        ))),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
