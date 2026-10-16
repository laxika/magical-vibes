package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "XLN", collectorNumber = "111")
public class LurkingChupacabra extends Card {

    public LurkingChupacabra() {
        // Whenever a creature you control explores, target creature an opponent controls
        // gets -2/-2 until end of turn.
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ALLY_CREATURE_EXPLORES, new BoostTargetCreatureEffect(-2, -2));
    }
}
