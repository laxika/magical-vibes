package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSC", collectorNumber = "12")
@CardRegistration(set = "DSC", collectorNumber = "43")
public class FearOfSleepParalysis extends Card {

    public FearOfSleepParalysis() {
        SequenceEffect eerie = SequenceEffect.upToOneTarget(
                new TapPermanentsEffect(TapUntapScope.TARGET),
                new PutCounterOnTargetPermanentEffect(CounterType.STUN));
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, eerie)
                .addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, eerie)
                .addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED, eerie);
    }
}
