package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "ISD", collectorNumber = "138")
public class CurseOfThePiercedHeart extends Card {

    public CurseOfThePiercedHeart() {
        addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(1, DamageRecipient.ENCHANTED_PLAYER));
    }
}
