package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "6")
@CardRegistration(set = "KLR", collectorNumber = "10")
public class AviaryMechanic extends Card {

    public AviaryMechanic() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new ReturnPermanentControlledByPlayerToHandEffect(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        "permanent"
                ),
                "Return another permanent you control to its owner's hand?"
        ));
    }
}
