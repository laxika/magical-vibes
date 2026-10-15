package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfCardsFromHandCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUpToNUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "NEO", collectorNumber = "154")
public class MarchOfRecklessJoy extends Card {

    public MarchOfRecklessJoy() {
        addEffect(EffectSlot.SPELL, new ExileAnyNumberOfCardsFromHandCost(
                new CardColorPredicate(CardColor.RED), 2));
        addEffect(EffectSlot.SPELL,
                new ExileTopCardsMayPlayUpToNUntilNextTurnEffect(new XValue(), 2));
    }
}
