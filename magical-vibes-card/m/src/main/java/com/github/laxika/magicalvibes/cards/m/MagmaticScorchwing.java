package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.NoCardsInLibraryMatching;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YONE", collectorNumber = "12")
public class MagmaticScorchwing extends Card {

    public MagmaticScorchwing() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(
                        new NoCardsInLibraryMatching(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.LAND),
                                new CardNotPredicate(new CardSupertypePredicate(CardSupertype.BASIC))))),
                        new DealDamageToAnyTargetEffect(3)));
    }
}
