package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "156")
@CardRegistration(set = "6ED", collectorNumber = "315")
@CardRegistration(set = "9ED", collectorNumber = "312")
@CardRegistration(set = "8ED", collectorNumber = "316")
@CardRegistration(set = "7ED", collectorNumber = "321")
@CardRegistration(set = "SLD", collectorNumber = "368")
public class TeferisPuzzleBox extends Card {

    public TeferisPuzzleBox() {
        // At the beginning of each player's draw step, that player puts the cards in their hand on
        // the bottom of their library in any order, then draws that many cards.
        addTrigger(Triggers.drawStep(ActivePlayer.ANY), PutCardsIntoLibraryEffect.onBottom(Subjects.cardsInHands(Subjects.activePlayer(), null)),
                new DrawCardEffect(new AmountThisWay(StepKind.PUT_INTO_LIBRARY), Subjects.activePlayer()));
    }
}
