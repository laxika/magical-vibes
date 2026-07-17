package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "150")
@CardRegistration(set = "9ED", collectorNumber = "138")
public class HighwayRobber extends Card {

    public HighwayRobber() {
        // When Highway Robber enters, target player loses 2 life and you gain 2 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER), new GainLifeEffect(2)));
    }
}
