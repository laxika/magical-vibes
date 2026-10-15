package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "DSK", collectorNumber = "141")
public class InfernalPhantom extends Card {

    public InfernalPhantom() {
        BoostSelfEffect eerie = new BoostSelfEffect(2, 0);
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, eerie);
        addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED, eerie);
        addEffect(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(new SourcePower()));
    }
}
