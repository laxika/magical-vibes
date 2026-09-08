package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;

@CardRegistration(set = "PCY", collectorNumber = "24")
public class SoulCharmer extends Card {

    public SoulCharmer() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new MayPayManaEffect(
                "{2}",
                null,
                "Pay {2} to prevent gaining 2 life?",
                MayPayPayer.TARGET_PERMANENT_CONTROLLER,
                new GainLifeEffect(2),
                0));
    }
}
