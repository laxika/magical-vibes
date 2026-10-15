package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;
import com.github.laxika.magicalvibes.model.condition.ControllerLostLifeLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "RIX", collectorNumber = "16")
public class PaladinOfAtonement extends Card {

    public PaladinOfAtonement() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new ConditionalEffect(
                new ControllerLostLifeLastTurn(),
                new PutCountersOnSourceEffect(1, 1, 1)));
        addEffect(EffectSlot.ON_DEATH, new GainLifeEffect(new SourceToughness()));
    }
}
