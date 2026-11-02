package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "128")
public class AcolyteOfTheInferno extends Card {

    public AcolyteOfTheInferno() {
        // Renown 1
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new RenownEffect(1));
        // Whenever this creature becomes blocked by a creature, it deals 2 damage to that creature.
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER, new DealDamageToTargetCreatureEffect(2));
    }
}
