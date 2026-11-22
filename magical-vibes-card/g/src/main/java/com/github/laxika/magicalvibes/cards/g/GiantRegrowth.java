package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "YSNC", collectorNumber = "13")
public class GiantRegrowth extends Card {

    public GiantRegrowth() {
        addSpellEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .targetGraveyard(true)
                .build());
        addSpellEffect(PerpetuallyBoostCardEffect.toCards(
                Subjects.cardsThisWay(StepKind.RETURN, new CardTypePredicate(CardType.CREATURE)), 3, 3));
    }
}
