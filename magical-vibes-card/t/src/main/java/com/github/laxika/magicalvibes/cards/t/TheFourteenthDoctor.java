package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPutIntoGraveyardFromLibraryThisTurnPredicate;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardInGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1583")
public class TheFourteenthDoctor extends Card {

    public TheFourteenthDoctor() {
        var doctor = new CardSubtypePredicate(CardSubtype.DOCTOR);
        addTrigger(Triggers.youCastThisSpell(),
                RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new Fixed(14)),
                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay(doctor)),
                PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.revealedThisWay(new CardNotPredicate(doctor))));
        addStaticEffect(CopyCreatureCardInGraveyardOnEnterEffect.of(Subjects.cardsInYourGraveyard(
                        new CardAllOfPredicate(List.of(
                                doctor,
                                new CardPutIntoGraveyardFromLibraryThisTurnPredicate()))))
                .gainingHaste());
    }
}
