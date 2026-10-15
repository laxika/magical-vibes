package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RevealAnyNumberOfCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "63")
public class NightshadeSeer extends Card {

    public NightshadeSeer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(
                        new RevealAnyNumberOfCardsFromHandEffect(
                                new CardColorPredicate(CardColor.BLACK)),
                        new BoostTargetCreatureEffect(
                                new Scaled(new EventValue(), -1),
                                new Scaled(new EventValue(), -1))),
                "{2}{B}, {T}: Reveal any number of black cards in your hand. Target creature gets -X/-X until end of turn, where X is the number of cards revealed this way."));
    }
}
