package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

/**
 * Poison the Well — {2}{B/R}{B/R} Sorcery.
 * Destroy target land. Poison the Well deals 2 damage to that land's controller.
 */
@CardRegistration(set = "SHM", collectorNumber = "193")
public class PoisonTheWell extends Card {

    public PoisonTheWell() {
        // Deal the damage while the land is still on the battlefield so its controller resolves,
        // then destroy it.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Target must be a land."
        ))
                .addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PERMANENT_CONTROLLER))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
