package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "90")
public class UnwillingIngredient extends Card {

    public UnwillingIngredient() {
        // {2}{B}, Exile this card from your graveyard: You draw a card and you lose 1 life.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new DrawCardEffect(1),
                        new LoseLifeEffect(1)
                ),
                "{2}{B}, Exile this card from your graveyard: You draw a card and you lose 1 life."
        ));
    }
}
