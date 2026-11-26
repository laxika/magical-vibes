package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class LocthwainScorn extends Card {

    public LocthwainScorn() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(-3, -3));
        addSpellEffect(new GainLifeEffect(2));
    }
}
