package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "TMP", collectorNumber = "227")
public class FlailingDrake extends Card {

    public FlailingDrake() {
        // Whenever this creature blocks or becomes blocked by a creature, that creature gets +1/+1
        // until end of turn. The blocking/blocked creature is carried as the trigger's non-targeting
        // target; the becomes-blocked side fires once per blocker.
        addEffect(EffectSlot.ON_BLOCK, new BoostTargetCreatureEffect(1, 1));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostTargetCreatureEffect(1, 1), TriggerMode.PER_BLOCKER);
    }
}
