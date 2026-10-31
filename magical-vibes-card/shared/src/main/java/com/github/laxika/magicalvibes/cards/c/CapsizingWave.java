package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class CapsizingWave extends Card {

    public CapsizingWave() {
        target(TargetFilters.creature())
                .addSpellEffect(ReturnToHandEffect.target());
    }
}
