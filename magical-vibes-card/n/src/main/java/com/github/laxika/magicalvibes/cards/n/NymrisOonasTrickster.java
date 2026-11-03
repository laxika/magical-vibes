package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOC", collectorNumber = "141")
public class NymrisOonasTrickster extends Card {

    public NymrisOonasTrickster() {
        // Whenever you cast your first spell during each opponent's turn, look at the top two
        // cards of your library. Put one into your hand and the other into your graveyard.
        addTrigger(Triggers.youCastSpell().duringOpponentsTurn().nthThisTurn(1),
                LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(2, 1));
    }
}
