package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "165")
@CardRegistration(set = "ECL", collectorNumber = "323")
public class AuroraAwakener extends Card {

    public AuroraAwakener() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect(
                        new ColorsAmongControlledPermanents(), new CardIsPermanentPredicate()));
    }
}
