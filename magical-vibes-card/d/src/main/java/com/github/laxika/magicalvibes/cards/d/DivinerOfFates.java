package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardSharingCardTypeWithDiscardedCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YSNC", collectorNumber = "22")
public class DivinerOfFates extends Card {

    public DivinerOfFates() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawDiscardAndConniveEffect());
        addTrigger(Triggers.youDiscardOneOrMoreCards(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new SeekCardSharingCardTypeWithDiscardedCardsEffect());
    }
}
