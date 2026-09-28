package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "DSC", collectorNumber = "150")
public class NightshadeHarvester extends Card {

    public NightshadeHarvester() {
        // Whenever a land an opponent controls enters, that player loses 1 life.
        // Put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD, SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER),
                new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
