package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RevealAnyNumberOfCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "78")
public class CinderSeer extends Card {

    public CinderSeer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(
                        new RevealAnyNumberOfCardsFromHandEffect(
                                new CardColorPredicate(CardColor.RED)),
                        new DealDamageToAnyTargetEffect(new EventValue())),
                "{2}{R}, {T}: Reveal any number of red cards in your hand. This creature deals X damage to any target, where X is the number of cards revealed this way."));
    }
}
