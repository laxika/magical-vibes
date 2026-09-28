package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "759")
public class AntMansAirForce extends Card {

    public AntMansAirForce() {
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK, new BoostTargetCreatureEffect(-1, 0));
    }
}
