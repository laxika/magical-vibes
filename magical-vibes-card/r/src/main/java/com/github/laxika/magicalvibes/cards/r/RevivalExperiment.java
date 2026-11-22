package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;


@CardRegistration(set = "C21", collectorNumber = "74")
public class RevivalExperiment extends Card {

    public RevivalExperiment() {
        addSpellEffect(ChooseCardEffect.upToOneOfEach(Subjects.cardsInYourGraveyard(null),
                new ChooseCardEffect.Each(new CardTypePredicate(CardType.ARTIFACT), "an artifact card"),
                new ChooseCardEffect.Each(new CardTypePredicate(CardType.CREATURE), "a creature card"),
                new ChooseCardEffect.Each(new CardTypePredicate(CardType.ENCHANTMENT), "an enchantment card"),
                new ChooseCardEffect.Each(new CardTypePredicate(CardType.LAND), "a land card"),
                new ChooseCardEffect.Each(new CardTypePredicate(CardType.PLANESWALKER), "a planeswalker card"),
                new ChooseCardEffect.Each(new CardTypePredicate(CardType.BATTLE), "a battle card")));
        addSpellEffect(new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE)));
        addSpellEffect(new LoseLifeEffect(
                new Scaled(new AmountThisWay(StepKind.RETURN), 3), Subjects.you()));
        addSpellEffect(new ExileSpellEffect());
    }
}
