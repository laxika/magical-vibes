package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "47")
@CardRegistration(set = "DMC", collectorNumber = "69")
public class TorstenFounderOfBenalia extends Card {

    public TorstenFounderOfBenalia() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new LookAtTopCardsEffect(
                new Fixed(7), new Fixed(7),
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.LAND))),
                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, true,
                LibrarySearchDestination.HAND, true));
        addTrigger(Triggers.dies(ObjectRelation.THIS), CreateTokenEffect.whiteSoldier(7));
    }
}
