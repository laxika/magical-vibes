package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "182")
public class MyrPropagator extends Card {

    public MyrPropagator() {
        addActivatedAbility(new ActivatedAbility(true, "{3}",
                List.of(new CreateTokenCopyOfSourceEffect()),
                "{3}, {T}: Create a token that's a copy of this creature."));
    }
}
