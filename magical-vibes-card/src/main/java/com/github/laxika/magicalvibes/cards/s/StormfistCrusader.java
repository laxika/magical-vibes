package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "ELD", collectorNumber = "203")
public class StormfistCrusader extends Card {

    public StormfistCrusader() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                SequenceEffect.of(
                        new EachPlayerDrawsCardEffect(1),
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_PLAYER)));
    }
}
