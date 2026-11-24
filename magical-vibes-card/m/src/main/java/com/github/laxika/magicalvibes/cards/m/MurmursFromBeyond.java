package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SOK", collectorNumber = "47")
@CardRegistration(set = "CMD", collectorNumber = "53")
public class MurmursFromBeyond extends Card {

    public MurmursFromBeyond() {
        addSpellEffect(RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new Fixed(3)));
        addSpellEffect(ChoosePlayerEffect.opponent());
        addSpellEffect(ChooseCardEffect.oneOf(Subjects.revealedThisWay(), "a revealed card to put into the graveyard")
                .by(Subjects.playersThisWay(StepKind.CHOOSE)));
        addSpellEffect(new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.cardsThisWay(StepKind.CHOOSE)));
        addSpellEffect(new PutCardsIntoHandEffect(Subjects.revealedThisWay()));
    }
}
