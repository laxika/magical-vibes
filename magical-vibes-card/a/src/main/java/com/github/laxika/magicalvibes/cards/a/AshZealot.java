package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "86")
@CardRegistration(set = "PIO", collectorNumber = "294")
public class AshZealot extends Card {

    public AshZealot() {
        // First strike and haste are auto-loaded as keywords from Scryfall.

        // Whenever a player casts a spell from a graveyard, this creature deals 3 damage to that player.
        addTrigger(Triggers.playerCastsSpell(), new SpellCastTriggerEffect(
                null,
                List.of(new DealDamageToPlayersEffect(3, DamageRecipient.TRIGGERING_PLAYER)),
                new StackEntryCastFromZonePredicate(Zone.GRAVEYARD)));
    }
}
