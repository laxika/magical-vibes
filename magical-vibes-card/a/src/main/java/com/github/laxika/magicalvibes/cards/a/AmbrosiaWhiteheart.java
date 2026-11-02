package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "6")
@CardRegistration(set = "FIN", collectorNumber = "325")
@CardRegistration(set = "FIN", collectorNumber = "424")
public class AmbrosiaWhiteheart extends Card {

    public AmbrosiaWhiteheart() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new ReturnPermanentControlledByPlayerToHandEffect(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        "permanent"
                ),
                "Return another permanent you control to its owner's hand?"
        ));
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new BoostSelfEffect(1, 0));
    }
}
