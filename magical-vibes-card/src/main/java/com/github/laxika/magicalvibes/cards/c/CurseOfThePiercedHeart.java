package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedPlayerEffect;

@CardRegistration(set = "ISD", collectorNumber = "138")
public class CurseOfThePiercedHeart extends Card {

    public CurseOfThePiercedHeart() {
        addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
                new DealDamageToEnchantedPlayerEffect(1));
    }
}
