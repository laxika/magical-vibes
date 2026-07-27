package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PTK", collectorNumber = "152")
public class TauntingChallenge extends Card {

    public TauntingChallenge() {
        // All creatures able to block target creature this turn do so.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new MustBeBlockedByAllCreaturesThisTurnEffect());
    }
}
