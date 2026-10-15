package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "91")
public class CagedZombie extends Card {

    public CagedZombie() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT)),
                "{1}{B}, {T}: Each opponent loses 2 life. Activate only if a creature died this turn.",
                ActivationTimingRestriction.MORBID
        ));
    }
}
