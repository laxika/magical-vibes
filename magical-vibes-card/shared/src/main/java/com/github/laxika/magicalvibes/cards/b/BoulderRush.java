package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class BoulderRush extends Card {

    public BoulderRush() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 0));
    }
}
