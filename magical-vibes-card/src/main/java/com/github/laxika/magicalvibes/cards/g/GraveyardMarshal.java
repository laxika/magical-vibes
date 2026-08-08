package com.github.laxika.magicalvibes.cards.g;

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

@CardRegistration(set = "M19", collectorNumber = "99")
public class GraveyardMarshal extends Card {

    public GraveyardMarshal() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE),
                        new CreateTokenEffect(
                                1, "Zombie", 2, 2,
                                CardColor.BLACK, List.of(CardSubtype.ZOMBIE),
                                Set.of(), Set.of(), true
                        )
                ),
                "{2}{B}, Exile a creature card from your graveyard: Create a tapped 2/2 black Zombie creature token."
        ));
    }
}
