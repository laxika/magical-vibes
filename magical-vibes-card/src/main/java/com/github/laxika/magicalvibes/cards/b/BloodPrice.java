package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "ZNR", collectorNumber = "93")
public class BloodPrice extends Card {

    public BloodPrice() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsEffect(
                new Fixed(4), new Fixed(2), null, LookDestination.BOTTOM_OF_LIBRARY, false));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(2));
    }
}
