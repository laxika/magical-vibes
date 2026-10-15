package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "77")
public class ArcMage extends Card {

    public ArcMage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        DealDividedDamageEffect.chosenAmongAnyTargets(2)
                ),
                "{2}{R}, {T}, Discard a card: This creature deals 2 damage divided as you choose among one or two targets."
        ));
    }
}
