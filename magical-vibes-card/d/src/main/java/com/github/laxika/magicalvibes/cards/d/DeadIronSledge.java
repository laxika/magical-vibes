package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyEquippedCreatureAndCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "162")
public class DeadIronSledge extends Card {

    public DeadIronSledge() {
        addTrigger(Triggers.blocks(), new DestroyEquippedCreatureAndCombatOpponentEffect());
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER,
                new DestroyEquippedCreatureAndCombatOpponentEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
