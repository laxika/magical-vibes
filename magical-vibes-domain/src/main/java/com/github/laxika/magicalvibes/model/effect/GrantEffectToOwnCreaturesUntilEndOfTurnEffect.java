package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * When resolved, grants every creature the controller currently controls a temporary triggered
 * ability until end of turn. Creatures that enter (or change control) later this turn do not
 * receive it — snapshot at resolution (e.g. Driven / Despair).
 *
 * <p>The granted effect is stored via
 * {@link com.github.laxika.magicalvibes.model.Permanent#addTemporaryTriggeredEffect(EffectSlot, CardEffect)}
 * and cleared at cleanup by {@link com.github.laxika.magicalvibes.model.Permanent#resetModifiers()}.
 *
 * @param grantedAbility the granted ability
 * @param filter        optional filter limiting the snapshot of creatures receiving the trigger
 */
public record GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
        Ability grantedAbility,
        PermanentPredicate filter
) implements CardEffect {

    /** The grant as the slot and effect that stand for it. */
    public GrantEffectToOwnCreaturesUntilEndOfTurnEffect(EffectSlot slot,
                                                         CardEffect grantedEffect,
                                                         PermanentPredicate filter) {
        this(LegacySlots.grant(slot, grantedEffect), filter);
    }

    public GrantEffectToOwnCreaturesUntilEndOfTurnEffect(EffectSlot slot, CardEffect grantedEffect) {
        this(slot, grantedEffect, null);
    }

    /** The slot of {@link #grantedAbility}, or {@code null} when there is none. */
    public EffectSlot slot() {
        return grantedAbility == null ? null : LegacySlots.slotOf(grantedAbility);
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : LegacySlots.effectsOf(grantedAbility).getFirst();
    }
}
