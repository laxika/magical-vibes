package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "158")
public class VaultRobber extends Card {

    public VaultRobber() {
        // {1}, {T}, Exile a creature card from your graveyard: Create a Treasure token.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE),
                        CreateTokenEffect.ofTreasureToken(1)
                ),
                "{1}, {T}, Exile a creature card from your graveyard: Create a Treasure token."
        ));
    }
}
