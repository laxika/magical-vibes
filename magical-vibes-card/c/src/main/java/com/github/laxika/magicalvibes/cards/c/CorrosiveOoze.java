package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "158")
public class CorrosiveOoze extends Card {

    public CorrosiveOoze() {
        // Whenever this creature blocks or becomes blocked by an equipped creature,
        // destroy all Equipment attached to that creature at end of combat.
        addTrigger(Triggers.blocks(), new DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect());
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER,
                new DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect());
    }
}
