package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DTK", collectorNumber = "118")
public class ShamblingGoblin extends Card {

    public ShamblingGoblin() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_DEATH, new BoostTargetCreatureEffect(-1, -1));
    }
}
