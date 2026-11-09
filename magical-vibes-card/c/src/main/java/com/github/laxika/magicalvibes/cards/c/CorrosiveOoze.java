package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "158")
public class CorrosiveOoze extends Card {

    public CorrosiveOoze() {
        // Whenever this creature blocks or becomes blocked by an equipped creature,
        // destroy all Equipment attached to that creature at end of combat.
        PermanentIsEquippedPredicate equipped = new PermanentIsEquippedPredicate();
        addTrigger(Triggers.blocks().withFilter(equipped),
                new DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect());
        addTrigger(Triggers.becomesBlocked().withFilter(equipped),
                TriggerOptions.EACH_BLOCKER,
                new DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect());
    }
}
