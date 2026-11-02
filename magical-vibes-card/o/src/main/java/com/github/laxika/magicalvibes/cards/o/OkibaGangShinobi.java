package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "76")
@CardRegistration(set = "PC2", collectorNumber = "35")
@CardRegistration(set = "PCA", collectorNumber = "35")
public class OkibaGangShinobi extends Card {

    public OkibaGangShinobi() {
        // Ninjutsu {3}{B}
        addNinjutsu("{3}{B}");

        // Whenever this creature deals combat damage to a player, that player discards two cards.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER));
    }
}
