package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;

@CardRegistration(set = "THB", collectorNumber = "60")
public class ProteanThaumaturge extends Card {

    public ProteanThaumaturge() {
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new BecomeCopyOfTargetCreatureEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD));
    }
}
