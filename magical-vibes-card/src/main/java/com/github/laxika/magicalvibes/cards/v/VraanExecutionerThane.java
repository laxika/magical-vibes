package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "ONE", collectorNumber = "114")
public class VraanExecutionerThane extends Card {

    public VraanExecutionerThane() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new OncePerTurnTriggerEffect(SequenceEffect.of(
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(2))));
    }
}
