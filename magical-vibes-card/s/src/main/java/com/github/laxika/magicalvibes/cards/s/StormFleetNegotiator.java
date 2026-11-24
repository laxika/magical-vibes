package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCC", collectorNumber = "46")
@CardRegistration(set = "LCC", collectorNumber = "78")
public class StormFleetNegotiator extends Card {

    public StormFleetNegotiator() {
        // Parley: a Map token for each nonland card the reveal step revealed, then each player draws.
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                new RevealTopCardOfLibraryEffect(Subjects.eachPlayer()),
                CreateTokenEffect.ofMapToken(1).withAmount(AmountThisWay.revealedCards(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)))),
                new DrawCardEffect(1, Subjects.eachPlayer())));
    }
}
