package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PayXManaSearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "27")
@CardRegistration(set = "MM2", collectorNumber = "36")
@CardRegistration(set = "C17", collectorNumber = "77")
public class TajNarSwordsmith extends Card {

    public TajNarSwordsmith() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PayXManaSearchLibraryEffect(new CardSubtypePredicate(CardSubtype.EQUIPMENT)));
    }
}
