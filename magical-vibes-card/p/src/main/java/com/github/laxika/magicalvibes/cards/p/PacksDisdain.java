package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreaturePerChosenTypeCountEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOR", collectorNumber = "73")
public class PacksDisdain extends Card {

    public PacksDisdain() {
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreaturePerChosenTypeCountEffect(-1, -1));
    }
}
