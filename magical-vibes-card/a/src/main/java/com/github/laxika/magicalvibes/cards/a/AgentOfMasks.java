package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * At the beginning of your upkeep, each opponent loses 1 life. You gain life equal to the life
 * lost this way.
 */
@CardRegistration(set = "GPT", collectorNumber = "100")
@CardRegistration(set = "MM3", collectorNumber = "149")
public class AgentOfMasks extends Card {

    public AgentOfMasks() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true));
    }
}
