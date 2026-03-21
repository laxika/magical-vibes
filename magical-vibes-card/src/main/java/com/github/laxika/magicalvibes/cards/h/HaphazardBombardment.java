package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentPermanentsAndPutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyRandomOpponentPermanentWithCounterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "DOM", collectorNumber = "131")
public class HaphazardBombardment extends Card {

    public HaphazardBombardment() {
        // "When this enchantment enters, choose four nonenchantment permanents you don't
        // control and put an aim counter on each of them."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseOpponentPermanentsAndPutCountersEffect(
                        CounterType.AIM, 4,
                        new PermanentNotPredicate(new PermanentIsEnchantmentPredicate())));

        // "At the beginning of your end step, if two or more permanents you don't control
        // have an aim counter on them, destroy one of those permanents at random."
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new DestroyRandomOpponentPermanentWithCounterEffect(CounterType.AIM, 2));
    }
}
