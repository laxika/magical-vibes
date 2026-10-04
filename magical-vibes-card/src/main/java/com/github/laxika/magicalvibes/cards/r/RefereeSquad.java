package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOM", collectorNumber = "327")
public class RefereeSquad extends Card {

    public RefereeSquad() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new PutCounterOnTargetPermanentEffect(CounterType.STUN));
    }
}
