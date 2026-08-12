package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CHR", collectorNumber = "7")
public class IndestructibleAura extends Card {

    public IndestructibleAura() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, PreventDamageEffect.allToTargetCreatures());
    }
}
