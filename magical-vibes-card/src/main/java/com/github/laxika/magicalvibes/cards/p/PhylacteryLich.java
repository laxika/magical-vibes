package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutPhylacteryCounterOnTargetPermanentEffect;

@CardRegistration(set = "M11", collectorNumber = "110")
public class PhylacteryLich extends Card {

    public PhylacteryLich() {
        // "As Phylactery Lich enters, put a phylactery counter on an artifact you control."
        // This does NOT target — shroud/hexproof don't prevent it. The artifact is chosen
        // as the creature enters, not when the spell is cast (MTG rulings).
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutPhylacteryCounterOnTargetPermanentEffect());
    }
}
