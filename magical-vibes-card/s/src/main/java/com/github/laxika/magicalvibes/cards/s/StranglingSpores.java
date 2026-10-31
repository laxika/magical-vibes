package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M19", collectorNumber = "122")
public class StranglingSpores extends Card {

    public StranglingSpores() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(-3, -3));
    }
}
