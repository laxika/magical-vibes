package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "XLN", collectorNumber = "77")
public class ShoreKeeper extends Card {

    public ShoreKeeper() {
        // {7}{U}, {T}, Sacrifice this creature: Draw three cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}{U}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(3)),
                "{7}{U}, {T}, Sacrifice Shore Keeper: Draw three cards."
        ));
    }
}
