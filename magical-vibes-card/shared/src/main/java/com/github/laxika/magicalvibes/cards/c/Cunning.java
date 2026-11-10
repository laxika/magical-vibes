package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtNextCleanupEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "28")
public class Cunning extends Card {

    public Cunning() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(3, 3, GrantScope.ENCHANTED_CREATURE))
                .addTrigger(Triggers.attacks(), new SacrificeSelfAtNextCleanupEffect())
                .addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new SacrificeSelfAtNextCleanupEffect());
    }
}
