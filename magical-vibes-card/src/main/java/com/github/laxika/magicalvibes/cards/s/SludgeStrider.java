package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "CON", collectorNumber = "126")
public class SludgeStrider extends Card {

    public SludgeStrider() {
        // Whenever another artifact you control enters or leaves the battlefield, you may pay {1}.
        // If you do, target player loses 1 life and you gain 1 life. Both triggers share one ability;
        // the "may pay {1}" and the player target resolve on the stack via the MayPayManaEffect flow.
        CardEffect drain = new MayPayManaEffect("{1}",
                SequenceEffect.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER),
                        new GainLifeEffect(1)),
                "pay {1}");
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, drain);
        addEffect(EffectSlot.ON_ANOTHER_ARTIFACT_LEAVES_BATTLEFIELD, drain);
    }
}
