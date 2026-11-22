package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "196")
@CardRegistration(set = "FCA", collectorNumber = "49")
public class AtraxaGrandUnifier extends Card {

    public AtraxaGrandUnifier() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new Fixed(10)),
                ChooseCardEffect.upToOneOfEach(Subjects.revealedThisWay(),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.LAND), "a land card"),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.CREATURE), "a creature card"),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.ENCHANTMENT), "an enchantment card"),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.SORCERY), "a sorcery card"),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.INSTANT), "an instant card"),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.ARTIFACT), "an artifact card"),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.PLANESWALKER), "a planeswalker card"),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.BATTLE), "a battle card"),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.KINDRED), "a kindred card"))
                        .offeringEachOnce(),
                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE)),
                PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.revealedThisWay()));
    }
}
