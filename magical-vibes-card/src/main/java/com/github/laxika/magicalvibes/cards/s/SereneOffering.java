package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TMP", collectorNumber = "40")
public class SereneOffering extends Card {

    public SereneOffering() {
        // Gain life first so the target's mana value is read before it is destroyed.
        target(TargetFilters.enchantment())
                .addEffect(EffectSlot.SPELL, new GainLifeEffect(new TargetManaValue()))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(false));
    }
}
