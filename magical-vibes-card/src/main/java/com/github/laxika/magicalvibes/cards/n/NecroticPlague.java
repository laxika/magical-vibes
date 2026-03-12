package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToOpponentCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "M11", collectorNumber = "107")
public class NecroticPlague extends Card {

    public NecroticPlague() {
        setTargetFilter(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));

        // Enchanted creature has "At the beginning of your upkeep, sacrifice this creature."
        addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED, new SacrificeEnchantedCreatureEffect());

        // When enchanted creature dies, its controller chooses target creature one of their
        // opponents controls. Return Necrotic Plague from its owner's graveyard to the battlefield
        // attached to that creature.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD, new ReturnSourceAuraToOpponentCreatureOnDeathEffect());
    }
}
