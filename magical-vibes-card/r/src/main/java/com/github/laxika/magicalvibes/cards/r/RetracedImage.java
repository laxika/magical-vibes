package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithAPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "TOR", collectorNumber = "46")
public class RetracedImage extends Card {

    public RetracedImage() {
        addSpellEffect(ChooseCardEffect.announced(Subjects.cardsInYourHand(new CardTruePredicate()), "a card in your hand to reveal"));
        addSpellEffect(RevealCardsFromHandEffect.cards(Subjects.you(), Subjects.cardsThisWay(StepKind.CHOOSE)));
        addSpellEffect(new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(new CardSharesNameWithAPermanentPredicate())));
    }
}
