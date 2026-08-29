package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;

@CardRegistration(set = "PCY", collectorNumber = "61")
public class DeathCharmer extends Card {

    public DeathCharmer() {
        // Whenever this creature deals combat damage to a creature, that creature's controller loses
        // 2 life unless they pay {2}. The damaged creature is baked as targetId by
        // ON_COMBAT_DAMAGE_TO_CREATURE (non-targeting).
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new MayPayManaEffect(
                "{2}",
                null,
                "Pay {2} to prevent losing 2 life?",
                MayPayPayer.TARGET_PERMANENT_CONTROLLER,
                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER),
                0));
    }
}
