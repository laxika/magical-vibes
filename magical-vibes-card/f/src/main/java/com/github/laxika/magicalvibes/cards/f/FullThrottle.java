package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhasesAfterMainEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAttackedCreaturesEachCombatThisTurnEffect;

@CardRegistration(set = "DFT", collectorNumber = "127")
public class FullThrottle extends Card {

    public FullThrottle() {
        addSpellEffect(new AdditionalCombatPhasesAfterMainEffect(2));
        addSpellEffect(new UntapAttackedCreaturesEachCombatThisTurnEffect());
    }
}
