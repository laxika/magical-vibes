package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RIX", collectorNumber = "29")
public class TrapjawTyrant extends Card {

    public TrapjawTyrant() {
        // Enrage — Whenever this creature is dealt damage, exile target creature an opponent controls
        // until this creature leaves the battlefield.
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_DEALT_DAMAGE, new ExileTargetPermanentUntilSourceLeavesEffect());
    }
}
