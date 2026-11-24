package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "251")
@CardRegistration(set = "MKC", collectorNumber = "235")
@CardRegistration(set = "C19", collectorNumber = "58")
public class ScrollOfFate extends Card {

    public ScrollOfFate() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(ChooseCardEffect.oneOf(Subjects.cardsInYourHand(new CardTruePredicate()),
                                "a card in your hand to manifest"),
                        new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE)).manifested()),
                "{T}: Manifest a card from your hand."));
    }
}
