package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureAtEndOfCombatEffect;

@CardRegistration(set = "WTH", collectorNumber = "114")
public class SawtoothOgre extends Card {

    public SawtoothOgre() {
        // Whenever this creature blocks or becomes blocked by a creature, this creature deals
        // 1 damage to that creature at end of combat.
        DealDamageToTargetCreatureAtEndOfCombatEffect damage =
                new DealDamageToTargetCreatureAtEndOfCombatEffect(1);
        addEffect(EffectSlot.ON_BLOCK, damage);
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, damage, TriggerMode.PER_BLOCKER);
    }
}
