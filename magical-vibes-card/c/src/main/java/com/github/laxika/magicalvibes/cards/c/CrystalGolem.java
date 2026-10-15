package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;

/**
 * Crystal Golem — {4} Artifact Creature — Golem 3/3.
 * "At the beginning of your end step, this creature phases out."
 */
@CardRegistration(set = "MIR", collectorNumber = "298")
public class CrystalGolem extends Card {

    public CrystalGolem() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new PhaseOutEffect(PhaseOutSubject.SOURCE));
    }
}
