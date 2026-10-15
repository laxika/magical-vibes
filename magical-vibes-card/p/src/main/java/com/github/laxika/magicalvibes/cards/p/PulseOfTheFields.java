package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHasMoreLifeThanController;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "DST", collectorNumber = "11")
public class PulseOfTheFields extends Card {

    public PulseOfTheFields() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(4));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new AnOpponentHasMoreLifeThanController(), ReturnToHandEffect.selfSpell()));
    }
}
