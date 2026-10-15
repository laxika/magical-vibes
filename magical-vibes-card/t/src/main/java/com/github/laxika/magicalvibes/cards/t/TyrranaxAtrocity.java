package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;

@CardRegistration(set = "ONE", collectorNumber = "188")
public class TyrranaxAtrocity extends Card {

    public TyrranaxAtrocity() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new GivePoisonCountersEffect(3, PoisonRecipient.TARGET_PLAYER));
    }
}
