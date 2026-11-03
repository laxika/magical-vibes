package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "68")
public class HollowbornBarghest extends Card {

    public HollowbornBarghest() {
        // "At the beginning of your upkeep, if you have no cards in hand, each opponent loses 2 life."
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new ActivePlayerHandEmpty()),
                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT));
        // "At the beginning of each opponent's upkeep, if that player has no cards in hand, they lose 2 life."
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),
                TriggerOptions.DEFAULT.withInterveningIf(new ActivePlayerHandEmpty()),
                new LoseLifeEffect(2, LoseLifeRecipient.ACTIVE_PLAYER));
    }
}
