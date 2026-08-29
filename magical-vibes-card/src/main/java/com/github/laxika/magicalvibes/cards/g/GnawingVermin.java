package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BRO", collectorNumber = "101")
public class GnawingVermin extends Card {

    public GnawingVermin() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(2, MillRecipient.TARGET_PLAYER));
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_DEATH, new BoostTargetCreatureEffect(-1, -1));
    }
}
