package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "126")
public class UndercityScrounger extends Card {

    public UndercityScrounger() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(CreateTokenEffect.ofTreasureToken(1)),
                "{T}: Create a Treasure token. Activate only if a creature died this turn.",
                ActivationTimingRestriction.MORBID
        ));
    }
}
