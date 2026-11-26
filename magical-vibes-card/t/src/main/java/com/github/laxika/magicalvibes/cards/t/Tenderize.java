package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TMT", collectorNumber = "133")
public class Tenderize extends Card {

    public Tenderize() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(DealDamageEffect.bite());
        target(TargetFilters.creatureAnOpponentControls());
    }
}
