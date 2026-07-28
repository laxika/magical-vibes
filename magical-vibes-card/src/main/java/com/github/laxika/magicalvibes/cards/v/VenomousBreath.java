package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentsOfTargetAtEndOfCombatEffect;

/**
 * Venomous Breath — {3}{G} Instant.
 * "Choose target creature. At this turn's next end of combat, destroy all creatures that blocked or
 * were blocked by it this turn."
 */
@CardRegistration(set = "ICE", collectorNumber = "273")
public class VenomousBreath extends Card {

    public VenomousBreath() {
        addEffect(EffectSlot.SPELL, new DestroyCombatOpponentsOfTargetAtEndOfCombatEffect());
    }
}
