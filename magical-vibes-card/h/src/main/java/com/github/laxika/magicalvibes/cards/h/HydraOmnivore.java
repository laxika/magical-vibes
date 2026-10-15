package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOtherOpponentEffect;

@CardRegistration(set = "CMD", collectorNumber = "161")
@CardRegistration(set = "DSC", collectorNumber = "185")
@CardRegistration(set = "C18", collectorNumber = "153")
public class HydraOmnivore extends Card {

    public HydraOmnivore() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DealDamageToEachOtherOpponentEffect());
    }
}
