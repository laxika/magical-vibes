package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "66")
@CardRegistration(set = "DDD", collectorNumber = "42")
@CardRegistration(set = "MMA", collectorNumber = "86")
public class FaerieMacabre extends Card {

    public FaerieMacabre() {
        // Flying comes from Scryfall.
        // Discard this card: Exile up to two target cards from graveyards.
        addHandActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ExileCardsFromGraveyardEffect(2, 0)),
                "Discard this card: Exile up to two target cards from graveyards."));
    }
}
