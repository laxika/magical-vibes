package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class GoHogWild extends Card {

    public GoHogWild() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 2));
    }
}
