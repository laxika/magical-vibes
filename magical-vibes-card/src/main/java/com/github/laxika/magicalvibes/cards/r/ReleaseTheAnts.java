package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "MOR", collectorNumber = "98")
public class ReleaseTheAnts extends Card {

    public ReleaseTheAnts() {
        // Deal 1 damage to any target (target chosen at cast time), then clash with an opponent.
        // If you win, return Release the Ants to its owner's hand instead of the graveyard.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));
        addEffect(EffectSlot.SPELL, new ClashEffect(ReturnToHandEffect.selfSpell()));
    }
}
