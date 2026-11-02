package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "155")
public class AkkiUnderminer extends Card {

    public AkkiUnderminer() {
        // Whenever this creature deals combat damage to a player, that player sacrifices a
        // permanent of their choice. The damaged player is bound as the trigger's target, and the
        // any-permanent filter routes to the multi-permanent choice so they pick it themselves.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new SacrificePermanentsEffect(1, new PermanentTruePredicate(), SacrificeRecipient.TARGET_PLAYER));
    }
}
