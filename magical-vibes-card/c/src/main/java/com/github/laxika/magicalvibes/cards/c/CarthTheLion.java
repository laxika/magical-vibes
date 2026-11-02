package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.IncreaseLoyaltyAbilityCostEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "189")
public class CarthTheLion extends Card {

    public CarthTheLion() {
        LookAtTopCardsEffect search = LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                7, new CardTypePredicate(CardType.PLANESWALKER));
        addTrigger(Triggers.enters(ObjectRelation.THIS), search);
        addTrigger(Triggers.dies(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE_OR_PLANESWALKER,
                                new PermanentIsPlaneswalkerPredicate())),
                        ObjectRelation.YOU_CONTROL),
                search);
        addStaticEffect(new IncreaseLoyaltyAbilityCostEffect(new PermanentIsPlaneswalkerPredicate(), 1));
    }
}
