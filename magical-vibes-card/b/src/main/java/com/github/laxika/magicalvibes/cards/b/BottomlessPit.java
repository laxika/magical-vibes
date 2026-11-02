package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STH", collectorNumber = "51")
@CardRegistration(set = "SLD", collectorNumber = "1404")
public class BottomlessPit extends Card {

    public BottomlessPit() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new DiscardEffect(1, DiscardRecipient.ACTIVE_PLAYER, true));
    }
}
