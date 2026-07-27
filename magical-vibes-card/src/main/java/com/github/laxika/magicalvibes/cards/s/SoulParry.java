package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOM", collectorNumber = "21")
public class SoulParry extends Card {

    public SoulParry() {
        target(TargetFilters.creature(), 1, 2).addEffect(EffectSlot.SPELL, PreventDamageEffect.allByTargetCreatures());
    }
}
