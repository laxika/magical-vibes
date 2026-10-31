package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AER", collectorNumber = "54")
public class CruelFinality extends Card {

    public CruelFinality() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(-2, -2))
                .addSpellEffect(new ScryEffect(1));
    }
}
