package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "69")
public class LingeringDeath extends Card {

    public LingeringDeath() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.endStep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                        new SacrificeEnchantedCreatureEffect());
    }
}
