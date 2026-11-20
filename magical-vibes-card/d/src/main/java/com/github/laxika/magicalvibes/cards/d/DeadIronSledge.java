package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "162")
public class DeadIronSledge extends Card {

    public DeadIronSledge() {
        addTrigger(Triggers.blocks(), destroyBoth());
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, destroyBoth());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }

    /**
     * "Destroy both creatures": the equipped creature the trigger recorded and the creature on the other side of the
     * block, its non-targeting combat opponent, as one event.
     */
    private static DestroyTargetPermanentEffect destroyBoth() {
        return new DestroyTargetPermanentEffect(Subjects.allOf(Subjects.triggering(), Subjects.target()));
    }
}
