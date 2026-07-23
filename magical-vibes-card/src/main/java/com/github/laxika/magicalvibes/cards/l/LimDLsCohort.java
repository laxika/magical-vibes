package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.PreventTargetCreatureRegenerationThisTurnEffect;

@CardRegistration(set = "ICE", collectorNumber = "145")
public class LimDLsCohort extends Card {

    public LimDLsCohort() {
        // Whenever this creature blocks or becomes blocked by a creature, that creature
        // can't be regenerated this turn. The combat opponent is carried as the trigger's
        // (non-targeting) target.
        addEffect(EffectSlot.ON_BLOCK, new PreventTargetCreatureRegenerationThisTurnEffect());
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new PreventTargetCreatureRegenerationThisTurnEffect(),
                TriggerMode.PER_BLOCKER);
    }
}
