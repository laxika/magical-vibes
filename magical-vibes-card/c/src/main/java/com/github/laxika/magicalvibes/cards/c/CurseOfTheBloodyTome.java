package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "50")
public class CurseOfTheBloodyTome extends Card {

    public CurseOfTheBloodyTome() {
        // At the beginning of enchanted player's upkeep, that player mills two cards.
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER), new MillEffect(2, MillRecipient.ACTIVE_PLAYER));
    }
}
