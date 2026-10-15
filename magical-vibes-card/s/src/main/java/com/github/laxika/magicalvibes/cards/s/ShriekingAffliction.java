package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "RTR", collectorNumber = "76")
public class ShriekingAffliction extends Card {

    public ShriekingAffliction() {
        // "At the beginning of each opponent's upkeep, if that player has one or fewer cards in
        // hand, they lose 3 life."
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new ConditionalEffect(
                new ActivePlayerHandAtMost(1),
                new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PLAYER)));
    }
}
