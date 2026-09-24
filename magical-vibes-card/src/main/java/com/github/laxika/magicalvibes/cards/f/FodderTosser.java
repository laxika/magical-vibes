package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "226")
public class FodderTosser extends Card {

    public FodderTosser() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(2)
                ),
                "{T}, Discard a card: This artifact deals 2 damage to target player or planeswalker."
        ));
    }
}
