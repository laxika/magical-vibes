package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LEG", collectorNumber = "83")
public class VenarianGold extends Card {

    public VenarianGold() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new PutCounterOnReferencedPermanentEffect(CounterType.SLEEP, new XValue()));

        addEffect(EffectSlot.STATIC,
                DoesntUntapWithCounterEffect.enchantedWithCounterOnEnchantedPermanent(CounterType.SLEEP));
        addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                new RemoveCounterFromReferencedPermanentEffect(CounterType.SLEEP));
    }
}
