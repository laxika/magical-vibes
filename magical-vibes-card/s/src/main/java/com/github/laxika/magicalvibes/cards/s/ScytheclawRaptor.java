package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "165")
@CardRegistration(set = "LCI", collectorNumber = "323")
@CardRegistration(set = "PIO", collectorNumber = "156")
public class ScytheclawRaptor extends Card {

    public ScytheclawRaptor() {
        // Whenever a player casts a spell, if it's not their turn, this creature deals 4 damage to them.
        addTrigger(Triggers.playerCastsSpell().notDuringCastersTurn(),
                new DealDamageToPlayersEffect(4, DamageRecipient.TRIGGERING_PLAYER));
    }
}
