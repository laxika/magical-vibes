package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "EVE", collectorNumber = "46")
public class SyphonLife extends Card {

    public SyphonLife() {
        // Target player loses 2 life and you gain 2 life.
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
        addCastingOption(new Retrace());
    }
}
