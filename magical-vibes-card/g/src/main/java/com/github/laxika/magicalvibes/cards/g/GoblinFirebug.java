package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "98")
public class GoblinFirebug extends Card {

    public GoblinFirebug() {
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), SacrificeRecipient.CONTROLLER));
    }
}
