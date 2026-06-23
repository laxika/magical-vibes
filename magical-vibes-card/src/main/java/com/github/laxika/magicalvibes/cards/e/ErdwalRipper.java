package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "DKA", collectorNumber = "86")
public class ErdwalRipper extends Card {

    public ErdwalRipper() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
