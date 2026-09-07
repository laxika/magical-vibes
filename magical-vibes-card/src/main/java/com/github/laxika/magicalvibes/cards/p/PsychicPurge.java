package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "LEG", collectorNumber = "68")
public class PsychicPurge extends Card {

    public PsychicPurge() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));
        addEffect(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT,
                new LoseLifeEffect(5, LoseLifeRecipient.EACH_OPPONENT));
    }
}
