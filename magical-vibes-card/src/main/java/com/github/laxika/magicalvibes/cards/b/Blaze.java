package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "190")
@CardRegistration(set = "9ED", collectorNumber = "175")
@CardRegistration(set = "POR", collectorNumber = "118")
@CardRegistration(set = "P02", collectorNumber = "91")
@CardRegistration(set = "PTK", collectorNumber = "102")
@CardRegistration(set = "8ED", collectorNumber = "177")
@CardRegistration(set = "7ED", collectorNumber = "175")
@CardRegistration(set = "6ED", collectorNumber = "168")
public class Blaze extends Card {

    public Blaze() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new XValue()));
    }
}
