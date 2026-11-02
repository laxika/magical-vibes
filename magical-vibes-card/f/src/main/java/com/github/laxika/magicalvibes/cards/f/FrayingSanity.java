package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsPutIntoGraveyardByTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "35")
@CardRegistration(set = "WOT", collectorNumber = "19")
@CardRegistration(set = "PIP", collectorNumber = "175")
@CardRegistration(set = "PIP", collectorNumber = "459")
@CardRegistration(set = "PIP", collectorNumber = "703")
@CardRegistration(set = "PIP", collectorNumber = "987")
public class FrayingSanity extends Card {

    public FrayingSanity() {
        // At the beginning of each end step, enchanted player mills X cards, where X is the number
        // of cards put into their graveyard from anywhere this turn.
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                new MillEffect(new CardsPutIntoGraveyardByTargetPlayerThisTurn(), MillRecipient.TARGET_PLAYER));
    }
}
