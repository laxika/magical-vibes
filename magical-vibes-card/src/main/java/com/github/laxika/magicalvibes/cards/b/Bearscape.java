package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "229")
public class Bearscape extends Card {

    public Bearscape() {
        // {1}{G}, Exile two cards from your graveyard: Create a 2/2 green Bear creature token.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new ExileNCardsFromGraveyardCost(2, null),
                        new CreateTokenEffect("Bear", 2, 2, CardColor.GREEN,
                                List.of(CardSubtype.BEAR), Set.of(), Set.of())
                ),
                "{1}{G}, Exile two cards from your graveyard: Create a 2/2 green Bear creature token."
        ));
    }
}
