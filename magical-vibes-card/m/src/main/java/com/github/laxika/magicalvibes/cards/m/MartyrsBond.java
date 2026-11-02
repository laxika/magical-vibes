package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCardTypeWithSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB1", collectorNumber = "174")
@CardRegistration(set = "SS2", collectorNumber = "2")
@CardRegistration(set = "CMD", collectorNumber = "19")
public class MartyrsBond extends Card {

    public MartyrsBond() {
        SacrificePermanentsEffect sacrificeMatchingType = new SacrificePermanentsEffect(
                1,
                new PermanentSharesCardTypeWithSourcePermanentPredicate(),
                SacrificeRecipient.EACH_OPPONENT);
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, ObjectRelation.THIS), sacrificeMatchingType);
        addTrigger(Triggers.dies(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                sacrificeMatchingType);
    }
}
