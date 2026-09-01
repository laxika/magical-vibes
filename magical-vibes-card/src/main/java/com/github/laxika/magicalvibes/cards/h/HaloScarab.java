package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "239")
public class HaloScarab extends Card {

    public HaloScarab() {
        // {2}, Exile this card from your graveyard: Create a Treasure token.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new ExileSelfFromGraveyardCost(), CreateTokenEffect.ofTreasureToken(1)),
                "{2}, Exile this card from your graveyard: Create a Treasure token."
        ));
    }
}
