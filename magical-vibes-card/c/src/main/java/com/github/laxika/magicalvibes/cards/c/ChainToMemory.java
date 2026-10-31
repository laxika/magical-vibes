package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THB", collectorNumber = "46")
public class ChainToMemory extends Card {

    public ChainToMemory() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(-4, 0))
                .addSpellEffect(new ScryEffect(2));
    }
}
