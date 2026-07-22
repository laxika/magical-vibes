package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "115")
public class Griselbrand extends Card {

    public Griselbrand() {
        // Pay 7 life: Draw seven cards.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(7), new DrawCardEffect(7)),
                "Pay 7 life: Draw seven cards."));
    }
}
