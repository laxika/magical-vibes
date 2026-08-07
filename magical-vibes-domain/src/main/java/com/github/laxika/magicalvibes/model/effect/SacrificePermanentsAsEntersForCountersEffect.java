package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "As this permanent enters, sacrifice any number of permanents you control matching {@code filter}.
 * It enters with {@code countersPerPermanent} times that many +1/+1 counters on it." (Shimatsu the
 * Bloodcloaked.)
 * <p>
 * An as-enters replacement effect (CR 614.1c): placed in {@code EffectSlot.ON_ENTER_BATTLEFIELD} and
 * handled during {@code BattlefieldEntryService.handleCreatureEnteredBattlefield} before ETB triggers
 * fire. The entering permanent itself is never offered — it isn't on the battlefield yet as far as
 * the ability is concerned. Contrast {@link DevourEffect}, which is the Devour keyword: it is limited
 * to creatures and records the devoured cards on the permanent.
 */
public record SacrificePermanentsAsEntersForCountersEffect(PermanentPredicate filter,
                                                           int countersPerPermanent)
        implements ReplacementEffect {
}
