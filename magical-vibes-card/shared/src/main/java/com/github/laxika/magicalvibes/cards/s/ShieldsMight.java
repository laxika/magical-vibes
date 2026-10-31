package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class ShieldsMight extends Card {

    public ShieldsMight() {
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(2, 2));
    }
}
