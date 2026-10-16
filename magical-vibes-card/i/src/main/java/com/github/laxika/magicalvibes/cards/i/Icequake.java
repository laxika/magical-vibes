package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Icequake — {1}{B}{B} Sorcery.
 * Destroy target land. If that land was a snow land, Icequake deals 1 damage to that land's controller.
 */
@CardRegistration(set = "ICE", collectorNumber = "134")
@CardRegistration(set = "ME2", collectorNumber = "94")
public class Icequake extends Card {

    public Icequake() {
        // The snow check and controller are snapshotted before the land is destroyed, but the
        // damage is dealt afterwards so a destroyed snow Glacial Chasm no longer prevents it.
        target(TargetFilters.land())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                        EventStat.NONE,
                        new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER),
                        ThenEffectRecipient.TARGET_CONTROLLER_AS_TARGET,
                        new PermanentHasSupertypePredicate(CardSupertype.SNOW)));
    }
}
