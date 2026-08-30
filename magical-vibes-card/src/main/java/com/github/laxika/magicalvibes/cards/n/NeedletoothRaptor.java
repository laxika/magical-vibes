package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RIX", collectorNumber = "107")
public class NeedletoothRaptor extends Card {

    public NeedletoothRaptor() {
        // Enrage — Whenever this creature is dealt damage, it deals 5 damage to target creature an opponent controls.
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_DEALT_DAMAGE, new DealDamageToTargetCreatureEffect(5));
    }
}
