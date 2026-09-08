package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "180")
public class Bramblesnap extends Card {

    public Bramblesnap() {
        // Tap an untapped creature you control: This creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentIsCreaturePredicate()),
                        new BoostSelfEffect(1, 1)),
                "Tap an untapped creature you control: This creature gets +1/+1 until end of turn."));
    }
}
