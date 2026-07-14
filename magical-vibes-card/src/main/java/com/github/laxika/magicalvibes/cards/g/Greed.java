package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "140")
@CardRegistration(set = "6ED", collectorNumber = "135")
public class Greed extends Card {

    public Greed() {
        // {B}, Pay 2 life: Draw a card.
        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new PayLifeCost(2), new DrawCardEffect(1)),
                "{B}, Pay 2 life: Draw a card."));
    }
}
