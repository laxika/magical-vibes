package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "36")
public class RitualOfSteel extends Card {

    public RitualOfSteel() {
        // Enchant creature — enchanted creature gets +0/+2.
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(0, 2, GrantScope.ENCHANTED_CREATURE));

        // When this Aura enters, draw a card at the beginning of the next turn's upkeep.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
