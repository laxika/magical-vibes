package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "237")
public class Dawnstrider extends Card {

    public Dawnstrider() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        PreventDamageEffect.allCombat()
                ),
                "{G}, {T}, Discard a card: Prevent all combat damage that would be dealt this turn."
        ));
    }
}
