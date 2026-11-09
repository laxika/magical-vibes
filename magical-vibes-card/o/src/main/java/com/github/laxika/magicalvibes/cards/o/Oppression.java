package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "7ED", collectorNumber = "152")
@CardRegistration(set = "USG", collectorNumber = "143")
@CardRegistration(set = "SLD", collectorNumber = "1424")
@CardRegistration(set = "WOT", collectorNumber = "32")
public class Oppression extends Card {

    public Oppression() {
        // Whenever a player casts a spell, that player discards a card.
        addTrigger(Triggers.playerCastsSpell().anySpell(),
                new DiscardEffect(1, DiscardRecipient.TRIGGERING_PLAYER, false));
    }
}
