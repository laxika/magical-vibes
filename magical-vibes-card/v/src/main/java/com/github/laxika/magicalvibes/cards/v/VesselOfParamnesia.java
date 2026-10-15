package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "95")
public class VesselOfParamnesia extends Card {

    public VesselOfParamnesia() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(
                        new SacrificeSelfCost(),
                        new MillEffect(3, MillRecipient.TARGET_PLAYER),
                        new DrawCardEffect()
                ),
                "{U}, Sacrifice this enchantment: Target player mills three cards. Draw a card."
        ));
    }
}
