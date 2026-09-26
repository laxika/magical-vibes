package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostRandomCreatureCardInHandEffect;

@CardRegistration(set = "YDSK", collectorNumber = "23")
public class GoldenSidekick extends Card {

    public GoldenSidekick() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new PerpetuallyBoostRandomCreatureCardInHandEffect(new EventValue()));
    }
}
