package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "145")
public class ParasiticBond extends Card {

    public ParasiticBond() {
        // Enchant creature. At the beginning of the upkeep of enchanted creature's controller,
        // this Aura deals 2 damage to that player.
        target(TargetFilters.creature()).addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                new DealDamageToPlayersEffect(2, DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER));
    }
}
