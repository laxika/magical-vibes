package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;

/**
 * When resolved, grants the source permanent itself a temporary triggered ability until end of
 * turn (e.g. Cruel Deceiver's "this creature gains '…' until end of turn"). Non-targeting — the
 * recipient is the resolving stack entry's source permanent, so nothing is chosen or validated.
 *
 * <p>The granted effect is stored via
 * {@link com.github.laxika.magicalvibes.model.Permanent#addTemporaryTriggeredEffect(EffectSlot, CardEffect)}
 * and cleared at cleanup by {@link com.github.laxika.magicalvibes.model.Permanent#resetModifiers()}.
 *
 * @param grantedAbility the granted ability
 */
public record GrantEffectToSourceUntilEndOfTurnEffect(
        Ability grantedAbility
) implements CardEffect {

    /** The grant as the slot and effect that stand for it. */
    public GrantEffectToSourceUntilEndOfTurnEffect(EffectSlot slot, CardEffect grantedEffect) {
        this(LegacySlots.grant(slot, grantedEffect));
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
