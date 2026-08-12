package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "95")
public class BookOfRass extends Card {

    public BookOfRass() {
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new PayLifeCost(2), new DrawCardEffect(1)),
                "{2}, Pay 2 life: Draw a card."));
    }
}
