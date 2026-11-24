package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "61")
public class InvasionOfArcavios extends Card {

    public InvasionOfArcavios() {
        setBackFaceCard(new InvocationOfTheFounders());

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SearchLibraryAndOrGraveyardForCardToHandEffect.inZones(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)
                        )), Zone.LIBRARY, Zone.GRAVEYARD, Zone.OUTSIDE_GAME));
    }

    @Override
    public String getBackFaceClassName() {
        return "InvocationOfTheFounders";
    }
}
