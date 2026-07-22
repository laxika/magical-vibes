package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "159")
public class HoneymoonHearse extends Card {

    public HoneymoonHearse() {
        // Tap two untapped creatures you control: This Vehicle becomes an artifact creature until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsCreaturePredicate()),
                        AnimatePermanentsEffect.crew()
                ),
                "Tap two untapped creatures you control: This Vehicle becomes an artifact creature until end of turn."
        ));
    }
}
