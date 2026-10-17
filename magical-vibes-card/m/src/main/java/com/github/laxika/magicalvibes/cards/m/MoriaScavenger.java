package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardMatches;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "63")
@CardRegistration(set = "LTC", collectorNumber = "145")
@CardRegistration(set = "LTC", collectorNumber = "470")
public class MoriaScavenger extends Card {

    public MoriaScavenger() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null, false, 1, false, false, true),
                        new DrawCardEffect(1),
                        new ConditionalEffect(
                                new ImprintedCardMatches(new CardTypePredicate(CardType.CREATURE),
                                        "a creature card", "discarded card"),
                                new AmassGoblinsEffect(1, CardSubtype.ORC))),
                "{T}, Discard a card: Draw a card. If the discarded card was a creature card, "
                        + "amass Orcs 1."));
    }
}
