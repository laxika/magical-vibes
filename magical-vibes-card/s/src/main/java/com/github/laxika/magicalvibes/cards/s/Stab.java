package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FDN", collectorNumber = "71")
public class Stab extends Card {

    public Stab() {
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(-2, -2));
    }
}
