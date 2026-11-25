package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "107")
public class NeedletoothRaptor extends Card {

    public NeedletoothRaptor() {
        // Enrage — Whenever this creature is dealt damage, it deals 5 damage to target creature an opponent controls.
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.dealtDamage(), DealDamageEffect.toTargetCreature(5));
    }
}
