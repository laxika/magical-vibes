package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "WOE", collectorNumber = "28")
public class SaviorOfTheSleeping extends Card {

    public SaviorOfTheSleeping() {
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
