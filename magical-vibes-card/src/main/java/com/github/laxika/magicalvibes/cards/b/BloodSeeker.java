package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "M12", collectorNumber = "81")
public class BloodSeeker extends Card {

    public BloodSeeker() {
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD,
                new MayEffect(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER), "Have that player lose 1 life?"));
    }
}
