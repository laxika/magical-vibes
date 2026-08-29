package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsChooseOneMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TDM", collectorNumber = "219")
public class RiverwheelSweep extends Card {

    public RiverwheelSweep() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.SPELL, new PutCounterOnTargetPermanentEffect(CounterType.STUN, 3));
        addEffect(EffectSlot.SPELL, new ExileTopCardsChooseOneMayPlayUntilNextTurnEffect(2));
    }
}
