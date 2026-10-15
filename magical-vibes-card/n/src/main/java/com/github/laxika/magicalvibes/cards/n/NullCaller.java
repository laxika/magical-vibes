package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "88")
public class NullCaller extends Card {

    public NullCaller() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE),
                        new CreateTokenEffect(
                                1, "Zombie", 2, 2,
                                CardColor.BLACK, List.of(CardSubtype.ZOMBIE),
                                Set.of(), Set.of(), true
                        )
                ),
                "{3}{B}, Exile a creature card from your graveyard: Create a tapped 2/2 black Zombie creature token."
        ));
    }
}
