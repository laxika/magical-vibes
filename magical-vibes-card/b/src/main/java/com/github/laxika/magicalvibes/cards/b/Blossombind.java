package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CantBecomeUntappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "45")
public class Blossombind extends Card {

    public Blossombind() {
        target(TargetFilters.creature())
                // When this Aura enters, tap enchanted creature.
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(TapUntapScope.ENCHANTED))
                // Enchanted creature can't become untapped.
                .addStaticEffect(new CantBecomeUntappedEffect(GrantScope.ENCHANTED_CREATURE))
                // Enchanted creature can't have counters put on it.
                .addStaticEffect(new CantHaveCountersEffect(GrantScope.ENCHANTED_CREATURE));
    }
}
