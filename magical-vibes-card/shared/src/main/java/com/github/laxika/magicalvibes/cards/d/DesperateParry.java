package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class DesperateParry extends Card {

    public DesperateParry() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(-4, 0));
    }
}
