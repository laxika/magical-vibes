package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.RevealAnyNumberOfCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "135")
public class Metalworker extends Card {

    public Metalworker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RevealAnyNumberOfCardsFromHandEffect(new CardTypePredicate(CardType.ARTIFACT)),
                        new AwardManaEffect(ManaColor.COLORLESS, new Scaled(new EventValue(), 2))
                ),
                "{T}: Reveal any number of artifact cards in your hand. Add {C}{C} for each card revealed this way."
        ));
    }
}
