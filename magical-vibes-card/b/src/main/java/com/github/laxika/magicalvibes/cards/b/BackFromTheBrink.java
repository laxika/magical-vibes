package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfExiledCostCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "44")
public class BackFromTheBrink extends Card {

    public BackFromTheBrink() {
        // Exile a creature card from your graveyard and pay its mana cost:
        // Create a token that's a copy of that card. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE, true, true),
                        new CreateTokenCopyOfExiledCostCardEffect()
                ),
                "Exile a creature card from your graveyard and pay its mana cost: Create a token that's a copy of that card. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
