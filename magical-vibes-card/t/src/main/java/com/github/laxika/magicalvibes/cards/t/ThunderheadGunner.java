package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "148")
public class ThunderheadGunner extends Card {

    public ThunderheadGunner() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DiscardCardTypeCost(null, null), new DrawCardEffect(1)),
                "Discard a card: Draw a card. Activate only as a sorcery and only once each turn.",
                null,
                null,
                1,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
