package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "8ED", collectorNumber = "37")
public class Redeem extends Card {

    public Redeem() {
        // Prevent all damage that would be dealt this turn to up to two target creatures.
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL, PreventDamageEffect.allToTargetCreatures());
    }
}
