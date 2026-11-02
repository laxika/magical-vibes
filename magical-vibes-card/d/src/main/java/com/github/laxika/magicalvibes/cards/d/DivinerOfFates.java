package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardSharingCardTypeWithDiscardedCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YSNC", collectorNumber = "22")
public class DivinerOfFates extends Card {

    public DivinerOfFates() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawDiscardAndConniveEffect());
        addTrigger(Triggers.youDiscardOneOrMoreCards(),
                new OncePerTurnTriggerEffect(new SeekCardSharingCardTypeWithDiscardedCardsEffect()));
    }
}
