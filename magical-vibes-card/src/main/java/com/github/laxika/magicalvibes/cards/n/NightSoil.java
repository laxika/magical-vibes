package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromSingleGraveyardCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FEM", collectorNumber = "71a")
@CardRegistration(set = "FEM", collectorNumber = "71b")
@CardRegistration(set = "FEM", collectorNumber = "71c")
@CardRegistration(set = "FEM", collectorNumber = "124")
@CardRegistration(set = "FEM", collectorNumber = "125")
public class NightSoil extends Card {

    public NightSoil() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new ExileNCardsFromSingleGraveyardCost(2, CardType.CREATURE),
                        new CreateTokenEffect("Saproling", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.SAPROLING), Set.of(), Set.of())
                ),
                "{1}, Exile two creature cards from a single graveyard: Create a 1/1 green Saproling creature token."
        ));
    }
}
