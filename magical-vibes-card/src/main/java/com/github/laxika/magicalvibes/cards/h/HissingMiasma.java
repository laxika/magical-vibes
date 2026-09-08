package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "GPT", collectorNumber = "51")
public class HissingMiasma extends Card {

    public HissingMiasma() {
        // Whenever a creature attacks you, its controller loses 1 life.
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU,
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
