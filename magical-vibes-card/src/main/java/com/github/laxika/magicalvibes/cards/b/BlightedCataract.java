package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "DDT", collectorNumber = "26")
public class BlightedCataract extends Card {

    public BlightedCataract() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {5}{U}, {T}, Sacrifice this land: Draw two cards.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}{U}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2)),
                "{5}{U}, {T}, Sacrifice this land: Draw two cards."
        ));
    }
}
