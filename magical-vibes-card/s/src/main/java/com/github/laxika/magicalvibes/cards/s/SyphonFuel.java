package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DFT", collectorNumber = "108")
public class SyphonFuel extends Card {

    public SyphonFuel() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(-6, -6))
                .addSpellEffect(new GainLifeEffect(2));
    }
}
