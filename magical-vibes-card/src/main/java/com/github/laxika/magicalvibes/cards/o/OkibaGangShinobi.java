package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "BOK", collectorNumber = "76")
public class OkibaGangShinobi extends Card {

    public OkibaGangShinobi() {
        // Ninjutsu {3}{B}
        addNinjutsu("{3}{B}");

        // Whenever this creature deals combat damage to a player, that player discards two cards.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER));
    }
}
