package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MMQ", collectorNumber = "235")
public class ClearTheLand extends Card {

    public ClearTheLand() {
        CardPredicate land = new CardTypePredicate(CardType.LAND);
        addSpellEffect(SequenceEffect.of(
                RevealTopCardOfLibraryEffect.topCards(Subjects.eachPlayer(), new Fixed(5)),
                new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.REVEAL, land), true),
                ExileGraveyardCardsEffect.cards(
                        Subjects.cardsThisWay(StepKind.REVEAL, new CardNotPredicate(land)))));
    }
}
