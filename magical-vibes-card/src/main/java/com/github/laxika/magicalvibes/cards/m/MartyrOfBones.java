package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealXCardsFromHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "65")
public class MartyrOfBones extends Card {

    public MartyrOfBones() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new RevealXCardsFromHandCost(new CardColorPredicate(CardColor.BLACK)),
                        new SacrificeSelfCost(),
                        new ExileCardsFromGraveyardEffect(0, 0, true)
                ),
                "{1}, Reveal X black cards from your hand, Sacrifice this creature: Exile up to X target cards from a single graveyard."
        ).withXValueFromCardsInHand(CardColor.BLACK));
    }
}
