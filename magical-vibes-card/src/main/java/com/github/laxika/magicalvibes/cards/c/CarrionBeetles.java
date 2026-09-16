package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

@CardRegistration(set = "USG", collectorNumber = "122")
public class CarrionBeetles extends Card {

    public CarrionBeetles() {
        // {2}{B}, {T}: Exile up to three target cards from a single graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(new ExileGraveyardCardsEffect(3, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD)),
                "{2}{B}, {T}: Exile up to three target cards from a single graveyard.",
                null, 0, 3
        ));
    }
}
