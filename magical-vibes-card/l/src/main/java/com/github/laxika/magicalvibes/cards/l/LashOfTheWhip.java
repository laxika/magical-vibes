package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "94")
public class LashOfTheWhip extends Card {

    public LashOfTheWhip() {
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(-4, -4));
    }
}
