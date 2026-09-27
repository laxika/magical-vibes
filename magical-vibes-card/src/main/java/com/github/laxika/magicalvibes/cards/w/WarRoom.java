package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeForEachCommanderColorCost;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "422")
public class WarRoom extends Card {

    public WarRoom() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {3}, {T}, Pay life equal to the number of colors in your commanders' color identity: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new PayLifeForEachCommanderColorCost(), new DrawCardEffect()),
                "{3}, {T}, Pay life equal to the number of colors in your commanders' color identity: Draw a card."
        ));
    }
}
