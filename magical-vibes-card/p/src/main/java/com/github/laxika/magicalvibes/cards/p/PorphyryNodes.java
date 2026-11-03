package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureWithLeastPowerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Porphyry Nodes — {W} Enchantment.
 * Destroys a creature with the least power during its controller's upkeep and sacrifices itself
 * when there are no creatures on the battlefield.
 */
@CardRegistration(set = "PLC", collectorNumber = "28")
@CardRegistration(set = "TSR", collectorNumber = "32")
public class PorphyryNodes extends Card {

    public PorphyryNodes() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new DestroyCreatureWithLeastPowerEffect(true));

        addTrigger(Triggers.battlefieldHasAtMost(0, new PermanentIsCreaturePredicate()), new SacrificeSelfEffect());
    }
}
