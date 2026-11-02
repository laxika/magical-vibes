package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToOpponentCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "107")
public class NecroticPlague extends Card {

    public NecroticPlague() {
        target(TargetFilters.creature())
                // Enchanted creature has "At the beginning of your upkeep, sacrifice this creature."
                .addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                        new SacrificeEnchantedCreatureEffect(true))

                // When enchanted creature dies, its controller chooses target creature one of their
                // opponents controls. Return Necrotic Plague from its owner's graveyard to the battlefield
                // attached to that creature.
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new ReturnSourceAuraToOpponentCreatureOnDeathEffect());
    }
}
