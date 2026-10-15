package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MiracleCast;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "20")
public class Zephyrim extends Card {

    public Zephyrim() {
        // Squad {2}
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{2}")));

        // Create a token copy of Zephyrim for each time its squad cost was paid.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenCopyOfSourceEffect(false, new RepeatedAdditionalCostCount("{2}")));

        // Miracle {1}{W}
        addCastingOption(new MiracleCast("{1}{W}"));
    }
}
