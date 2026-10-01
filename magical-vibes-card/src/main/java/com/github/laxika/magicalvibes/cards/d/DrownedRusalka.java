package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "24")
public class DrownedRusalka extends Card {

    public DrownedRusalka() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new SacrificeCreatureCost(), new DiscardEffect(1, DiscardRecipient.CONTROLLER, false),
                        new DrawCardEffect()),
                "{U}, Sacrifice a creature: Discard a card, then draw a card."
        ));
    }
}
