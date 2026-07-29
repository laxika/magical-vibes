package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSelfAndCombatOpponentEffect;

@CardRegistration(set = "MIR", collectorNumber = "63")
public class DreamFighter extends Card {

    public DreamFighter() {
        // Whenever this creature blocks or becomes blocked by a creature, this creature and that
        // creature phase out. The combat opponent is carried as the trigger's (non-targeting) target.
        addEffect(EffectSlot.ON_BLOCK, new PhaseOutSelfAndCombatOpponentEffect());
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new PhaseOutSelfAndCombatOpponentEffect(),
                TriggerMode.PER_BLOCKER);
    }
}
