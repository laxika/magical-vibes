package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardLastDrawnCardCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "SUM", collectorNumber = "256")
public class JandorsRing extends Card {

    public JandorsRing() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new DiscardLastDrawnCardCost(), new DrawCardEffect(1)),
                "{2}, {T}, Discard the last card you drew this turn: Draw a card."
        ));
    }
}
