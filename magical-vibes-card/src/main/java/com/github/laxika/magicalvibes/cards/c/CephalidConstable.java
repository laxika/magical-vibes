package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "72")
@CardRegistration(set = "JUD", collectorNumber = "35")
public class CephalidConstable extends Card {

    public CephalidConstable() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ReturnPermanentsOnCombatDamageToPlayerEffect());
    }
}
