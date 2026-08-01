package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "86")
public class AshZealot extends Card {

    public AshZealot() {
        // First strike and haste are auto-loaded as keywords from Scryfall.

        // Whenever a player casts a spell from a graveyard, this creature deals 3 damage to that player.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new DealDamageToPlayersEffect(3, DamageRecipient.TRIGGERING_PLAYER)),
                new StackEntryCastFromZonePredicate(Zone.GRAVEYARD)));
    }
}
