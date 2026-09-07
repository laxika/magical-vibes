package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "106")
public class ZagothMamba extends Card {

    public ZagothMamba() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_SELF_MUTATES, new BoostTargetCreatureEffect(-2, -2));
    }
}
