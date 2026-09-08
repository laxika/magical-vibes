package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RevealXCardsFromHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "113")
public class MartyrOfSpores extends Card {

    public MartyrOfSpores() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new RevealXCardsFromHandCost(new CardColorPredicate(CardColor.GREEN)),
                        new SacrificeSelfCost(),
                        new BoostTargetCreatureEffect(new XValue(), new XValue())
                ),
                "{1}, Reveal X green cards from your hand, Sacrifice this creature: Target creature gets +X/+X until end of turn."
        ).withXValueFromCardsInHand(CardColor.GREEN));
    }
}
