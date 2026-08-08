package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCantGainLifeThisTurnEffect;

@CardRegistration(set = "BOK", collectorNumber = "101")
public class FlamesOfTheBloodHand extends Card {

    public FlamesOfTheBloodHand() {
        // If that player or that planeswalker's controller would gain life this turn, that player
        // gains no life instead. Resolved before the damage so the lock is already in place if the
        // damage itself would cause life gain (e.g. a lifelinking source).
        addEffect(EffectSlot.SPELL, new TargetPlayerCantGainLifeThisTurnEffect());

        // Flames of the Blood Hand deals 4 damage to target player or planeswalker.
        // The damage can't be prevented.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerOrPlaneswalkerEffect(4, true));
    }
}
