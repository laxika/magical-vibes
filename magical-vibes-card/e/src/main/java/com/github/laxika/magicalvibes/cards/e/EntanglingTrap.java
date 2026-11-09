package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "13")
public class EntanglingTrap extends Card {

    public EntanglingTrap() {
        addTrigger(Triggers.youClash(), new TapPermanentsEffect(TapUntapScope.TARGET),
                new ConditionalEffect(new EventValueAtLeast(1), new SkipNextUntapEffect(TapUntapScope.TARGET)));
    }
}
