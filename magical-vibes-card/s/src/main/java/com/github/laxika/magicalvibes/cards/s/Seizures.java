package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "ICE", collectorNumber = "159")
public class Seizures extends Card {

    public Seizures() {
        // Enchant creature. Whenever enchanted creature becomes tapped, this Aura deals 3 damage
        // to that creature's controller unless that player pays {3}.
        target(TargetFilters.creature()).addTrigger(Triggers.enchantedPermanentBecomesTapped(),
                UnlessPaysEffect.unlessPays(Subjects.controllerOf(Subjects.triggering()), "{3}",
                        new DealDamageToPlayersEffect(3, Subjects.controllerOf(Subjects.triggering()))));
    }
}
