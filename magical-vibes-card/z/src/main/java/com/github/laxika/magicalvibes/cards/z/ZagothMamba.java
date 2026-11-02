package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "106")
public class ZagothMamba extends Card {

    public ZagothMamba() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.mutates(), new BoostTargetCreatureEffect(-2, -2));
    }
}
