package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "ELD", collectorNumber = "104")
public class RevengeOfRavens extends Card {

    public RevengeOfRavens() {
        // Whenever a creature attacks you or a planeswalker you control, that creature's controller
        // loses 1 life and you gain 1 life. The attacking creature is the trigger's non-targeting
        // targetId, so TARGET_PERMANENT_CONTROLLER routes the life loss to its controller.
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU,
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER));
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU, new GainLifeEffect(1));
    }
}
