package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeEqualToCommanderColorIdentityCost;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "330")
public class WarRoom extends Card {

    public WarRoom() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new PayLifeEqualToCommanderColorIdentityCost(), new DrawCardEffect()),
                "{3}, {T}, Pay life equal to the number of colors in your commanders' color identity: Draw a card."
        ));
    }
}
