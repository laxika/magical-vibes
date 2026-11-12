package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "180")
public class DestructiveUrge extends Card {

    public DestructiveUrge() {
        target(TargetFilters.creature());
        // "That player" is the player dealt damage, which the ability names without targeting.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), SacrificeRecipient.TARGET_PLAYER));
    }
}
