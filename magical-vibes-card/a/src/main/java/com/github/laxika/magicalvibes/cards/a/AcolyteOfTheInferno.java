package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;

@CardRegistration(set = "ORI", collectorNumber = "128")
public class AcolyteOfTheInferno extends Card {

    public AcolyteOfTheInferno() {
        // Renown 1
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RenownEffect(1));
        // Whenever this creature becomes blocked by a creature, it deals 2 damage to that creature.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DealDamageToTargetCreatureEffect(2), TriggerMode.PER_BLOCKER);
    }
}
