package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSagaCardFromGraveyardAndCopyChapterEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "314")
@CardRegistration(set = "MB2", collectorNumber = "550")
public class TheManyDeedsOfBelzenlok extends Card {

    public TheManyDeedsOfBelzenlok() {
        addTrigger(Triggers.chapter(1),
                new ExileTargetSagaCardFromGraveyardAndCopyChapterEffect(1));
        addTrigger(Triggers.chapter(2),
                new ExileTargetSagaCardFromGraveyardAndCopyChapterEffect(2));
        addTrigger(Triggers.chapter(3),
                new ExileTargetSagaCardFromGraveyardAndCopyChapterEffect(3));
    }
}
