package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromOwnGraveyardEffect;

@CardRegistration(set = "ISD", collectorNumber = "95")
public class CurseOfOblivion extends Card {

    public CurseOfOblivion() {
        addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
                new ExileCardsFromOwnGraveyardEffect(2));
    }
}
