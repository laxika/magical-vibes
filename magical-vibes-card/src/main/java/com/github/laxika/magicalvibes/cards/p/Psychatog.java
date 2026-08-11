package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "292")
public class Psychatog extends Card {

    public Psychatog() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DiscardCardTypeCost(null, null), new BoostSelfEffect(1, 1)),
                "Discard a card: This creature gets +1/+1 until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ExileNCardsFromGraveyardCost(2, null), new BoostSelfEffect(1, 1)),
                "Exile two cards from your graveyard: This creature gets +1/+1 until end of turn."
        ));
    }
}
