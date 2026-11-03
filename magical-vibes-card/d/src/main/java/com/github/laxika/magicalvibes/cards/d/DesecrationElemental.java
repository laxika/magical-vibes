package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "45")
@CardRegistration(set = "TD2", collectorNumber = "57")
public class DesecrationElemental extends Card {

    public DesecrationElemental() {
        // Fear is auto-loaded from Scryfall.

        // Whenever a player casts a spell, sacrifice a creature.
        addTrigger(Triggers.playerCastsSpell().anySpell(),
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), SacrificeRecipient.CONTROLLER));
    }
}
