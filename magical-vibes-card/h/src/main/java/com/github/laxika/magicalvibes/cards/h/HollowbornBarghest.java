package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "SHM", collectorNumber = "68")
public class HollowbornBarghest extends Card {

    public HollowbornBarghest() {
        // "At the beginning of your upkeep, if you have no cards in hand, each opponent loses 2 life."
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new ActivePlayerHandEmpty(), new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT)));
        // "At the beginning of each opponent's upkeep, if that player has no cards in hand, they lose 2 life."
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new ConditionalEffect(
                new ActivePlayerHandEmpty(), new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER)));
    }
}
