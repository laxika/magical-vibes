package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "63")
@CardRegistration(set = "LTC", collectorNumber = "145")
public class MoriaScavenger extends Card {

    public MoriaScavenger() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DrawCardEffect(1),
                        new DiscardCardThenEffect(
                                null,
                                new AmassGoblinsEffect(1, CardSubtype.ORC),
                                "a card",
                                new CardTypePredicate(CardType.CREATURE))),
                "{T}, Discard a card: Draw a card. If the discarded card was a creature card, "
                        + "amass Orcs 1."));
    }
}
