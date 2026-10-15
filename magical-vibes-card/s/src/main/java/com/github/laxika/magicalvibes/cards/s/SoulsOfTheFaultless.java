package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "GPT", collectorNumber = "131")
public class SoulsOfTheFaultless extends Card {

    public SoulsOfTheFaultless() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_SELF, SequenceEffect.of(
                new GainLifeEffect(new EventValue()),
                new DamageSourceControllerLosesLifeEffect()));
    }
}
