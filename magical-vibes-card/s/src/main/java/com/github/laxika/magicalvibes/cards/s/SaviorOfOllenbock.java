package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturesUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "34")
@CardRegistration(set = "DBL", collectorNumber = "301")
public class SaviorOfOllenbock extends Card {

    public SaviorOfOllenbock() {
        addTrigger(Triggers.trains(),
                new ExileTargetCreaturesUntilSourceLeavesEffect(1, false));
    }
}
