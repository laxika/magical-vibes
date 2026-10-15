package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CantBecomeUntappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "45")
public class Blossombind extends Card {

    public Blossombind() {
        target(TargetFilters.creature())
                // When this Aura enters, tap enchanted creature.
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapPermanentsEffect(TapUntapScope.ENCHANTED))
                // Enchanted creature can't become untapped.
                .addEffect(EffectSlot.STATIC,
                        new CantBecomeUntappedEffect(GrantScope.ENCHANTED_CREATURE))
                // Enchanted creature can't have counters put on it.
                .addEffect(EffectSlot.STATIC,
                        new CantHaveCountersEffect(GrantScope.ENCHANTED_CREATURE));
    }
}
