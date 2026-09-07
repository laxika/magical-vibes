package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "34")
public class Compulsion extends Card {

    public Compulsion() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new DiscardCardTypeCost(null, null), new DrawCardEffect()),
                "{1}{U}, Discard a card: Draw a card."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{1}{U}, Sacrifice this enchantment: Draw a card."
        ));
    }
}
