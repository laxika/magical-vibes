package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class Scrollboost extends Card {

    public Scrollboost() {
        // One or two target creatures each get +2/+2 until end of turn.
        target(TargetFilters.creature(), 1, 2)
                .addSpellEffect(BoostEffect.target(2, 2));
    }
}
