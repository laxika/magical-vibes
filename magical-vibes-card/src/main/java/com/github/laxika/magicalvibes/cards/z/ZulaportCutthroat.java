package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "BFZ", collectorNumber = "126")
public class ZulaportCutthroat extends Card {

    public ZulaportCutthroat() {
        SequenceEffect deathTrigger = SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(1));
        addEffect(EffectSlot.ON_DEATH, deathTrigger);
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, deathTrigger);
    }
}
