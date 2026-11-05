package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "209")
public class ObscuraInterceptor extends Card {

    public ObscuraInterceptor() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawDiscardAndConniveEffect(),
                new QueueReflexiveAbilityEffect(
                new ReturnTargetSpellToHandEffect(), true));
    }
}
