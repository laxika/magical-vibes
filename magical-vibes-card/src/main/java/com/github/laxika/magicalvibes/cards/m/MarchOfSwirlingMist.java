package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfCardsFromHandCost;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NEO", collectorNumber = "61")
public class MarchOfSwirlingMist extends Card {

    public MarchOfSwirlingMist() {
        addEffect(EffectSlot.SPELL,
                new ExileAnyNumberOfCardsFromHandCost(new CardColorPredicate(CardColor.BLUE), 2));

        PhaseOutEffect phaseOut = new PhaseOutEffect(PhaseOutSubject.TARGET);
        targetUpTo(new XValue(), TargetFilters.creature(), 100);
        registerEffectTargetIndex(phaseOut, 0);
        addEffect(EffectSlot.SPELL, phaseOut);
    }
}
