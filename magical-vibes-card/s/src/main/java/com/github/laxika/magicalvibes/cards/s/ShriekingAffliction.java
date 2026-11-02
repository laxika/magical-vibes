package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "76")
public class ShriekingAffliction extends Card {

    public ShriekingAffliction() {
        // "At the beginning of each opponent's upkeep, if that player has one or fewer cards in
        // hand, they lose 3 life."
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT), new ConditionalEffect(
                new ActivePlayerHandAtMost(1),
                new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PLAYER)));
    }
}
