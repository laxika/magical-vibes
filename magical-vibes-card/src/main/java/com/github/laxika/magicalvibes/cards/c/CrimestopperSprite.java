package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CollectEvidenceCostPaid;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "49")
public class CrimestopperSprite extends Card {

    public CrimestopperSprite() {
        addEffect(EffectSlot.SPELL, new CollectEvidenceCost(6, true));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new ConditionalEffect(
                                new CollectEvidenceCostPaid(),
                                new PutCounterOnTargetPermanentEffect(CounterType.STUN))));
    }
}
