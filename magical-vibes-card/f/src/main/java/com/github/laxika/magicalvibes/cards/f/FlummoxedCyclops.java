package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.OpponentsAttackWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "135")
public class FlummoxedCyclops extends Card {

    public FlummoxedCyclops() {
        addTrigger(Triggers.playerAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new OpponentsAttackWithAtLeastCreatures(2)),
                CantBlockThisCombatEffect.self());
    }
}
