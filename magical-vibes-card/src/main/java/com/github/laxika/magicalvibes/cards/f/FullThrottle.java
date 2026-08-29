package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhasesAfterMainEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAttackedCreaturesEachCombatThisTurnEffect;

@CardRegistration(set = "DFT", collectorNumber = "127")
public class FullThrottle extends Card {

    public FullThrottle() {
        addEffect(EffectSlot.SPELL, new AdditionalCombatPhasesAfterMainEffect(2));
        addEffect(EffectSlot.SPELL, new UntapAttackedCreaturesEachCombatThisTurnEffect());
    }
}
