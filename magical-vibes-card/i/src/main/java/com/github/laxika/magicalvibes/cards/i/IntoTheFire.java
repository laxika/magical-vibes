package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "144")
@CardRegistration(set = "MOM", collectorNumber = "364")
public class IntoTheFire extends Card {

    public IntoTheFire() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Into the Fire deals 2 damage to each creature, planeswalker, and battle",
                        DealDamageEffect.toEach(Subjects.controlledBy(Subjects.eachPlayer(),
                                new PermanentAnyOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                        new PermanentIsPlaneswalkerPredicate(), new PermanentIsBattlePredicate()))),
                                2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Put any number of cards from your hand on the bottom of your library, then draw that many cards plus one",
                        List.of(
                                ChooseCardEffect.upTo(Subjects.cardsInYourHand(null), Integer.MAX_VALUE,
                                        "cards from your hand to put on the bottom of your library"),
                                PutCardsIntoLibraryEffect.onBottom(Subjects.cardsThisWay(StepKind.CHOOSE)),
                                new DrawCardEffect(new Sum(new AmountThisWay(StepKind.PUT_INTO_LIBRARY),
                                        new Fixed(1)))))
        )));
    }
}
