package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "53")
public class Dwindle extends Card {

    public Dwindle() {
        target(TargetFilters.creature());
        // Enchanted creature gets -6/-0.
        addStaticEffect(new StaticBoostEffect(-6, 0, GrantScope.ENCHANTED_CREATURE));
        // When enchanted creature blocks, destroy it.
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE,
                new DestroyReferencedPermanentEffect(PermanentReference.ATTACHED));
    }
}
