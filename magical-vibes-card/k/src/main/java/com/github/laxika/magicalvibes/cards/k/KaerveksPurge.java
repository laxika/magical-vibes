package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Kaervek's Purge — {X}{B}{R} Sorcery.
 * Destroy target creature with mana value X. If that creature dies this way, Kaervek's Purge deals
 * damage equal to the creature's power to the creature's controller.
 */
@CardRegistration(set = "MIR", collectorNumber = "270")
public class KaerveksPurge extends Card {

    public KaerveksPurge() {
        // EventStat.POWER snapshots the creature's effective power before it leaves; requiresDestruction
        // means a regenerated / indestructible creature deals no damage.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentManaValueEqualsXPredicate())),
                "Target must be a creature with mana value X."
        ))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                        EventStat.POWER,
                        new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TARGET_PLAYER),
                        ThenEffectRecipient.TARGET_CONTROLLER_AS_TARGET,
                        null,
                        true));
    }
}
