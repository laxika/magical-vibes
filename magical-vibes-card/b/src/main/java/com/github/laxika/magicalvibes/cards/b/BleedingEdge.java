package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WAR", collectorNumber = "78")
public class BleedingEdge extends Card {

    public BleedingEdge() {
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-2, -2));
        addEffect(EffectSlot.SPELL, new AmassGoblinsEffect(2, CardSubtype.ZOMBIE));
    }
}