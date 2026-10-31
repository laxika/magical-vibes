package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;

/**
 * Grants the source permanent a triggered ability until its controller's next upkeep.
 *
 * <p>The grant is represented as a floating continuous effect so it survives turn cleanup and
 * expires at the correct upkeep boundary.
 */
public record GrantEffectToSourceUntilNextUpkeepEffect(
        Ability grantedAbility
) implements CardEffect {

    /** The grant as the slot and effect that stand for it. */
    public GrantEffectToSourceUntilNextUpkeepEffect(EffectSlot slot, CardEffect grantedEffect) {
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
