package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasExactlyTwoColorsPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "208")
@CardRegistration(set = "MUL", collectorNumber = "53")
@CardRegistration(set = "MUL", collectorNumber = "118")
@CardRegistration(set = "MUL", collectorNumber = "183")
public class NivMizzetReborn extends Card {

    public NivMizzetReborn() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new Fixed(10)),
                new ChooseCardEffect(Subjects.revealedThisWay(), "a card for each color pair", true,
                        new Fixed(1), false, List.of(
                                colorPair(CardColor.WHITE, CardColor.BLUE, "white and blue"),
                                colorPair(CardColor.WHITE, CardColor.BLACK, "white and black"),
                                colorPair(CardColor.WHITE, CardColor.RED, "white and red"),
                                colorPair(CardColor.WHITE, CardColor.GREEN, "white and green"),
                                colorPair(CardColor.BLUE, CardColor.BLACK, "blue and black"),
                                colorPair(CardColor.BLUE, CardColor.RED, "blue and red"),
                                colorPair(CardColor.BLUE, CardColor.GREEN, "blue and green"),
                                colorPair(CardColor.BLACK, CardColor.RED, "black and red"),
                                colorPair(CardColor.BLACK, CardColor.GREEN, "black and green"),
                                colorPair(CardColor.RED, CardColor.GREEN, "red and green"))),
                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE)),
                PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.revealedThisWay()));
    }

    private static ChooseCardEffect.Each colorPair(CardColor first, CardColor second, String colors) {
        return new ChooseCardEffect.Each(new CardAllOfPredicate(List.of(new CardHasExactlyTwoColorsPredicate(),
                new CardColorPredicate(first), new CardColorPredicate(second))), "a card that's exactly " + colors);
    }
}
