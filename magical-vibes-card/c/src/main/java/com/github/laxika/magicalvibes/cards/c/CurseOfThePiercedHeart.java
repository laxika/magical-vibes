package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "138")
public class CurseOfThePiercedHeart extends Card {

    public CurseOfThePiercedHeart() {
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER),
                new DealDamageToPlayersEffect(1, DamageRecipient.ENCHANTED_PLAYER_OR_PLANESWALKER));
    }
}
