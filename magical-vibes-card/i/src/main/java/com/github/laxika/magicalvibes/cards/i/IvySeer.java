package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RevealAnyNumberOfCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "110")
public class IvySeer extends Card {

    public IvySeer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(
                        new RevealAnyNumberOfCardsFromHandEffect(
                                new CardColorPredicate(CardColor.GREEN)),
                        new BoostTargetCreatureEffect(new EventValue(), new EventValue())),
                "{2}{G}, {T}: Reveal any number of green cards in your hand. Target creature gets +X/+X until end of turn, where X is the number of cards revealed this way."));
    }
}
