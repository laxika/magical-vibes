package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "37")
public class CouriersCapsule extends Card {

    public CouriersCapsule() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2)),
                "{1}{U}, {T}, Sacrifice Courier's Capsule: Draw two cards."
        ));
    }
}
