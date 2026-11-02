package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterTransformSourceAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "203")
public class AangAtTheCrossroads extends Card {

    public AangAtTheCrossroads() {
        setBackFaceCard(new AangDestinedSavior());

        CardEffect creatureWithManaValueAtMostFour = LookAtTopCardsEffect
                .mayPutMatchingOntoBattlefieldRestOnBottomRandom(5, new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardMaxManaValuePredicate(4)
                )));
        addTrigger(Triggers.enters(ObjectRelation.THIS), creatureWithManaValueAtMostFour);

        addTrigger(Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new RegisterTransformSourceAtNextUpkeepEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "AangDestinedSavior";
    }
}
