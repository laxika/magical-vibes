package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ONS", collectorNumber = "245")
@CardRegistration(set = "CMM", collectorNumber = "271")
public class AnimalMagnetism extends Card {

    public AnimalMagnetism() {
        addSpellEffect(RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new Fixed(5)));
        addSpellEffect(ChoosePlayerEffect.opponent());
        addSpellEffect(ChooseCardEffect.oneOf(Subjects.revealedThisWay(new CardTypePredicate(CardType.CREATURE)),
                        "a creature card to put onto the battlefield")
                .by(Subjects.playersThisWay(StepKind.CHOOSE)));
        addSpellEffect(new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE)));
        addSpellEffect(new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay()));
    }
}
