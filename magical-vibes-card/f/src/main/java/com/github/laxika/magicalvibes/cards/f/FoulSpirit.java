package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "P02", collectorNumber = "73")
@CardRegistration(set = "ME4", collectorNumber = "81")
public class FoulSpirit extends Card {

    public FoulSpirit() {
        // When this creature enters, sacrifice a land.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificePermanentsEffect(
                1, new PermanentIsLandPredicate(), SacrificeRecipient.CONTROLLER));
    }
}
