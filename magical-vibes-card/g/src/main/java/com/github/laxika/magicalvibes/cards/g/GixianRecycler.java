package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBRO", collectorNumber = "27")
public class GixianRecycler extends Card {

    public GixianRecycler() {
        ConjureCardToGraveyardEffect conjureDuplicate =
                new ConjureCardToGraveyardEffect("YBRO", "27");
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, ObjectRelation.THIS), conjureDuplicate);
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.HAND, ObjectRelation.THIS), conjureDuplicate);
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, ObjectRelation.THIS), conjureDuplicate);

        addUnearth("{1}{B}");
    }
}
