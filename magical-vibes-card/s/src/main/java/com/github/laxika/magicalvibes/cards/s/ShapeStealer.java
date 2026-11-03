package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetSelfBasePowerToughnessFromCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "55")
public class ShapeStealer extends Card {

    public ShapeStealer() {
        addTrigger(Triggers.blocks(), new SetSelfBasePowerToughnessFromCombatOpponentEffect());
        addTrigger(Triggers.becomesBlocked(),
                TriggerOptions.EACH_BLOCKER,
                new SetSelfBasePowerToughnessFromCombatOpponentEffect());
    }
}
