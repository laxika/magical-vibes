package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "136")
public class ChaosChanneler extends Card {

    public ChaosChanneler() {
        addTrigger(Triggers.attacks(), new RollD20Effect(
                new ExileTopCardMayPlayThisTurnEffect(1, false),
                new ExileTopCardMayPlayThisTurnEffect(2, false),
                new ExileTopCardMayPlayThisTurnEffect(3, false)));
    }
}
