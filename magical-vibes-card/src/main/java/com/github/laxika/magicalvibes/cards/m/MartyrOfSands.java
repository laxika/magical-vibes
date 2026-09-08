package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealXCardsFromHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "15")
public class MartyrOfSands extends Card {

    public MartyrOfSands() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new RevealXCardsFromHandCost(new CardColorPredicate(CardColor.WHITE)),
                        new SacrificeSelfCost(),
                        new GainLifeEffect(new Scaled(new XValue(), 3))
                ),
                "{1}, Reveal X white cards from your hand, Sacrifice this creature: You gain three times X life."
        ).withXValueFromCardsInHand(CardColor.WHITE));
    }
}
