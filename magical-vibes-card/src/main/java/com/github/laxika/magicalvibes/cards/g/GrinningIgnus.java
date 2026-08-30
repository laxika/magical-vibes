package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandCost;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "104")
public class GrinningIgnus extends Card {

    public GrinningIgnus() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(
                        new ReturnSelfToHandCost(),
                        new AwardManaEffect(ManaColor.COLORLESS, 2),
                        new AwardManaEffect(ManaColor.RED)
                ),
                "{R}, Return this creature to its owner's hand: Add {C}{C}{R}. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
