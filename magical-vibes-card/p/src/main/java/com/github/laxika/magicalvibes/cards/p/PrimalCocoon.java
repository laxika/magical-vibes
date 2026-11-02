package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "191")
public class PrimalCocoon extends Card {

    public PrimalCocoon() {
        // "When enchanted creature attacks" is the Aura's own ability, so the sacrifice is of the Aura itself.
        setAttackTriggerOwnedByAura(true);

        target(TargetFilters.creature())
                // At the beginning of your upkeep, put a +1/+1 counter on enchanted creature.
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                        new PutCounterOnReferencedPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE))

                // When enchanted creature attacks or blocks, sacrifice Primal Cocoon.
                .addTrigger(Triggers.attacks(), new SacrificeSelfEffect())
                .addTrigger(Triggers.blocks(), new SacrificeSelfEffect());
    }
}
