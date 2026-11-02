package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StunCountersCantBeRemovedFromOpponentsPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "12")
@CardRegistration(set = "DSC", collectorNumber = "43")
public class FearOfSleepParalysis extends Card {

    public FearOfSleepParalysis() {
        SequenceEffect eerie = SequenceEffect.upToOneTarget(
                new TapPermanentsEffect(TapUntapScope.TARGET),
                new PutCounterOnTargetPermanentEffect(CounterType.STUN));
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), eerie)
                .addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL), eerie)
                .addTrigger(Triggers.youFullyUnlockRoom(), eerie);
        addStaticEffect(new StunCountersCantBeRemovedFromOpponentsPermanentsEffect());
    }
}
