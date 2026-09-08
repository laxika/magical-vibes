package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "122")
public class CarrionBeetles extends Card {

    public CarrionBeetles() {
        // {2}{B}, {T}: Exile up to three target cards from a single graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(new ExileCardsFromGraveyardEffect(3, 0, true)),
                "{2}{B}, {T}: Exile up to three target cards from a single graveyard."
        ));
    }
}
