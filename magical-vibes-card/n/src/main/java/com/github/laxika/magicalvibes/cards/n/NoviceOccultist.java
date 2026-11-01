package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "117")
@CardRegistration(set = "DBL", collectorNumber = "117")
public class NoviceOccultist extends Card {

    public NoviceOccultist() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new DrawCardEffect(1),
                new LoseLifeEffect(1)));
    }
}
