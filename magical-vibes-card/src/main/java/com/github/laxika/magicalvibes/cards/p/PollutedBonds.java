package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "SHM", collectorNumber = "74")
public class PollutedBonds extends Card {

    public PollutedBonds() {
        // Whenever a land an opponent controls enters, that player loses 2 life and you gain 2 life.
        addEffect(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD, SequenceEffect.of(
                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER), new GainLifeEffect(2)));
    }
}
