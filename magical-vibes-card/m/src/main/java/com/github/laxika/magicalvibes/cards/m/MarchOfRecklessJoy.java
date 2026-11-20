package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfCardsFromHandCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "NEO", collectorNumber = "154")
public class MarchOfRecklessJoy extends Card {

    public MarchOfRecklessJoy() {
        addCastingCost(new ExileAnyNumberOfCardsFromHandCost(
                new CardColorPredicate(CardColor.RED), 2));
        addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(new XValue(), false)
                .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN).upTo(2));
    }
}
