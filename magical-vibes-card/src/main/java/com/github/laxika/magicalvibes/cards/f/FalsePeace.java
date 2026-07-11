package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SkipNextCombatPhaseEffect;

@CardRegistration(set = "POR", collectorNumber = "14")
public class FalsePeace extends Card {

    public FalsePeace() {
        // Target player skips all combat phases of their next turn.
        addEffect(EffectSlot.SPELL, new SkipNextCombatPhaseEffect(true));
    }
}
