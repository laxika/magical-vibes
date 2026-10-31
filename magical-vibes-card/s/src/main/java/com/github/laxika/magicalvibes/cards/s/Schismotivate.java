package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GPT", collectorNumber = "129")
public class Schismotivate extends Card {

    public Schismotivate() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(4, 0));
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(-4, 0));
    }
}
