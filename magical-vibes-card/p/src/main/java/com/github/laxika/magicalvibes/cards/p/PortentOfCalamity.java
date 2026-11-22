package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastDiscardedCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "BLB", collectorNumber = "66")
public class PortentOfCalamity extends Card {

    public PortentOfCalamity() {
        var landCard = new CardTypePredicate(CardType.LAND);
        addSpellEffect(RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new XValue()));
        addSpellEffect(ChooseCardEffect.upToOneOfEach(Subjects.revealedThisWay(),
                                new ChooseCardEffect.Each(landCard, "a land card"),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.CREATURE), "a creature card"),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.ENCHANTMENT), "an enchantment card"),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.SORCERY), "a sorcery card"),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.INSTANT), "an instant card"),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.ARTIFACT), "an artifact card"),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.PLANESWALKER), "a planeswalker card"),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.BATTLE), "a battle card"),
                                new ChooseCardEffect.Each(new CardTypePredicate(CardType.KINDRED), "a kindred card"))
                        .offeringEachOnce());
        addSpellEffect(ExileGraveyardCardsEffect.cards(Subjects.cardsThisWay(StepKind.CHOOSE)));
        addSpellEffect(new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay()));
        addSpellEffect(new ConditionalEffect(HappenedThisWay.atLeast(StepKind.EXILE, 4),
                        SequenceEffect.of(
                                ChooseCardEffect.upTo(Subjects.cardsThisWay(StepKind.EXILE, new CardNotPredicate(landCard)), 1,
                                        "a spell to cast without paying its mana cost"),
                                new MayCastDiscardedCardWithoutPayingManaCostEffect(Subjects.cardsThisWay(StepKind.CHOOSE)))));
        addSpellEffect(new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.EXILE)));
    }
}
