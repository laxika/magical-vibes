package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class SpewFlame extends Card {

    public SpewFlame() {
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(5));
    }
}
