package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "GRN", collectorNumber = "71")
public class GruesomeMenagerie extends Card {

    public GruesomeMenagerie() {
        addSpellEffect(ChooseCardEffect.oneOfEach(Subjects.cardsInYourGraveyard(new CardTypePredicate(CardType.CREATURE)),
                creatureWithManaValue(1), creatureWithManaValue(2), creatureWithManaValue(3)));
        addSpellEffect(new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE)));
    }

    private static ChooseCardEffect.Each creatureWithManaValue(int manaValue) {
        return new ChooseCardEffect.Each(
                new CardAllOfPredicate(List.of(
                        new CardMinManaValuePredicate(manaValue),
                        new CardMaxManaValuePredicate(manaValue))),
                "a creature card with mana value " + manaValue);
    }
}
