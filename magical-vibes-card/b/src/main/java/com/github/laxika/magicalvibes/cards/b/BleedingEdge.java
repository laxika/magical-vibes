package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WAR", collectorNumber = "78")
public class BleedingEdge extends Card {

    public BleedingEdge() {
        target(TargetFilters.creature(), 0, 1)
                .addSpellEffect(new BoostTargetCreatureEffect(-2, -2));
        addSpellEffect(new AmassGoblinsEffect(2, CardSubtype.ZOMBIE));
    }
}