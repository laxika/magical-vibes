package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ClashForControlOfEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "55")
public class CaptivatingGlance extends Card {

    public CaptivatingGlance() {
        target(TargetFilters.creature())
                // At the beginning of your end step, clash with an opponent. If you win, gain control
                // of enchanted creature. Otherwise, that player gains control of enchanted creature.
                .addTrigger(Triggers.endStep(ActivePlayer.YOU),
                        new ClashForControlOfEnchantedCreatureEffect());
    }
}
