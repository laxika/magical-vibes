package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "49")
@CardRegistration(set = "TDC", collectorNumber = "166")
@CardRegistration(set = "C19", collectorNumber = "93")
@CardRegistration(set = "LTC", collectorNumber = "524")
public class RiverKelpie extends Card {

    public RiverKelpie() {
        // Persist is auto-loaded as a keyword from Scryfall; its return mechanic is handled by the engine.

        // Whenever this creature or another permanent enters from a graveyard, draw a card.
        addTrigger(Triggers.entersFrom(Zone.GRAVEYARD, null, ObjectRelation.ANY), new DrawCardEffect());

        // Whenever a player casts a spell from a graveyard, draw a card.
        addTrigger(Triggers.playerCastsSpell()
                        .withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.GRAVEYARD)),
                new DrawCardEffect());
    }
}
