package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STX", collectorNumber = "174")
public class DaemogothTitan extends Card {

    public DaemogothTitan() {
        // Whenever this creature attacks or blocks, sacrifice a creature.
        addTrigger(Triggers.attacks(), sacrificeCreature());
        addTrigger(Triggers.blocks(), sacrificeCreature());
    }

    private SacrificePermanentsEffect sacrificeCreature() {
        return new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.CONTROLLER);
    }
}
