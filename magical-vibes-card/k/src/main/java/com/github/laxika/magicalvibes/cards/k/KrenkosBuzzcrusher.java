package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyUpToOneNonbasicLandPerPlayerThenSearchEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "136")
@CardRegistration(set = "MKM", collectorNumber = "405")
public class KrenkosBuzzcrusher extends Card {

    public KrenkosBuzzcrusher() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DestroyUpToOneNonbasicLandPerPlayerThenSearchEffect());
    }
}
