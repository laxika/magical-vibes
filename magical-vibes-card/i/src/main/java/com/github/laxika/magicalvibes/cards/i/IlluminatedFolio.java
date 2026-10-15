package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTwoCardsSharingColorCost;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "254")
public class IlluminatedFolio extends Card {

    public IlluminatedFolio() {
        // {1}, {T}, Reveal two cards from your hand that share a color: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}",
                List.of(new RevealTwoCardsSharingColorCost(), new DrawCardEffect()),
                "{1}, {T}, Reveal two cards from your hand that share a color: Draw a card."
        ));
    }
}
