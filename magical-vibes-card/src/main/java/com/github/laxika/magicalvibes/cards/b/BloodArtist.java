package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "INR", collectorNumber = "97")
public class BloodArtist extends Card {

    public BloodArtist() {
        // Whenever this creature or another creature dies, target player loses 1 life and you gain 1 life.
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER), new GainLifeEffect(1)));
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER), new GainLifeEffect(1)));
    }
}
