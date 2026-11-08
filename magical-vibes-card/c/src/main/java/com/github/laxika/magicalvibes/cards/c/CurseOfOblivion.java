package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "95")
public class CurseOfOblivion extends Card {

    public CurseOfOblivion() {
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER),
                new ExileGraveyardCardsEffect(2, GraveyardExileScope.TRIGGERING_PLAYER));
    }
}
