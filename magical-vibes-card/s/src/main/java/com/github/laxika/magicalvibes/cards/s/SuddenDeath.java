package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TSP", collectorNumber = "134")
@CardRegistration(set = "TSR", collectorNumber = "143")
public class SuddenDeath extends Card {

    public SuddenDeath() {
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(-4, -4));
    }
}
