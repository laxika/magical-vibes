package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

@CardRegistration(set = "HOU", collectorNumber = "9")
public class DisposalMummy extends Card {

    public DisposalMummy() {
        // "When this creature enters, exile target card from an opponent's graveyard."
        // Graveyard-targeting ETBs never target at cast time; the opponent's-graveyard card is
        // chosen as the trigger goes on the stack (see BattlefieldEntryService ETB routing).
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_OPPONENT_GRAVEYARD));
    }
}
