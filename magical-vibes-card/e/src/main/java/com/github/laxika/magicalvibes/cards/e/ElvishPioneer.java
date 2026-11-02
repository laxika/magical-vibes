package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "8ED", collectorNumber = "243")
@CardRegistration(set = "ONS", collectorNumber = "257")
public class ElvishPioneer extends Card {

    public ElvishPioneer() {
        // When this creature enters, you may put a basic land card from your hand onto the battlefield tapped.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new PutCardToBattlefieldEffect(CardPredicateUtils.basicLand(), "basic land", true),
                "Put a basic land card from your hand onto the battlefield tapped?"
        ));
    }
}
