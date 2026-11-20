package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "211")
@CardRegistration(set = "INR", collectorNumber = "416")
@CardRegistration(set = "SOI", collectorNumber = "221")
@CardRegistration(set = "SIR", collectorNumber = "208")
public class PackGuardian extends Card {

    public PackGuardian() {
        // When this creature enters, you may discard a land card. If you do, create a 2/2 green
        // Wolf creature token.
        // (Flash is loaded from Scryfall.)
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                SequenceEffect.of(
                        DiscardEffect.matching(1, Subjects.you(), new CardTypePredicate(CardType.LAND)),
                        new ConditionalEffect(HappenedThisWay.discarded(),
                                new CreateTokenEffect("Wolf", 2, 2,
                                        CardColor.GREEN, List.of(CardSubtype.WOLF),
                                        Set.of(), Set.of()))),
                "Discard a land card to create a 2/2 green Wolf token?"
        ));
    }
}
