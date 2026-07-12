package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "49")
public class RiverKelpie extends Card {

    public RiverKelpie() {
        // Persist is auto-loaded as a keyword from Scryfall; its return mechanic is handled by the engine.

        // Whenever this creature or another permanent enters from a graveyard, draw a card.
        addEffect(EffectSlot.ON_PERMANENT_ENTERS_FROM_GRAVEYARD, new DrawCardEffect());

        // Whenever a player casts a spell from a graveyard, draw a card.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new DrawCardEffect()),
                new StackEntryCastFromZonePredicate(Zone.GRAVEYARD)));
    }
}
