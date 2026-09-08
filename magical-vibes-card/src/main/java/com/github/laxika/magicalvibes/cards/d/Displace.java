package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EMN", collectorNumber = "55")
public class Displace extends Card {

    public Displace() {
        // Exile up to two target creatures you control, then return them under their owners' control.
        target(TargetFilters.creatureYouControl(), 0, 2)
                .addEffect(EffectSlot.SPELL, FlickerEffect.flickerTarget());
    }
}
