package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.RevealXCardsFromHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "40")
public class MartyrOfFrost extends Card {

    public MartyrOfFrost() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new RevealXCardsFromHandCost(new CardColorPredicate(CardColor.BLUE)),
                        new SacrificeSelfCost(),
                        new CounterUnlessPaysEffect(0, true, false)
                ),
                "{2}, Reveal X blue cards from your hand, Sacrifice this creature: Counter target spell unless its controller pays {X}."
        ).withXValueFromCardsInHand(CardColor.BLUE));
    }
}
