package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "PLS", collectorNumber = "40")
public class DarkSuspicions extends Card {

    public DarkSuspicions() {
        // At the beginning of each opponent's upkeep, that player loses X life, where X is the
        // number of cards in that player's hand minus the number of cards in your hand. X cannot
        // be negative.
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED,
                new LoseLifeEffect(
                        new Max(new Fixed(0), new Sum(
                                new CardsInHand(CountScope.TARGET_PLAYER),
                                new Scaled(new CardsInHand(CountScope.CONTROLLER), -1))),
                        LoseLifeRecipient.TARGET_PLAYER));
    }
}
