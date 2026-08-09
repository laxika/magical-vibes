package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.MatchingCardsInHand;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "10")
public class JasmineSeer extends Card {

    public JasmineSeer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}",
                List.of(new GainLifeEffect(new Scaled(
                        new MatchingCardsInHand(CountScope.CONTROLLER, new CardColorPredicate(CardColor.WHITE)),
                        2
                ))),
                "{2}{W}, {T}: Reveal any number of white cards in your hand. You gain 2 life for each card revealed this way."
        ));
    }
}
