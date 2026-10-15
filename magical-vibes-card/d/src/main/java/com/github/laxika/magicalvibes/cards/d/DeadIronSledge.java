package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyEquippedCreatureAndCombatOpponentEffect;

@CardRegistration(set = "MRD", collectorNumber = "162")
public class DeadIronSledge extends Card {

    public DeadIronSledge() {
        addEffect(EffectSlot.ON_BLOCK, new DestroyEquippedCreatureAndCombatOpponentEffect());
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new DestroyEquippedCreatureAndCombatOpponentEffect(), TriggerMode.PER_BLOCKER);
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
