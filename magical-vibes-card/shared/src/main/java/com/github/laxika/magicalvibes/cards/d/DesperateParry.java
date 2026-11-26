package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class DesperateParry extends Card {

    public DesperateParry() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(-4, 0));
    }
}
