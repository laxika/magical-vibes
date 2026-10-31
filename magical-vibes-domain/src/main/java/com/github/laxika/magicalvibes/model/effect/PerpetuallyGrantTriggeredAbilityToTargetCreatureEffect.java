package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Perpetually grants a triggered ability to a targeted creature. */
public record PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(
        Ability grantedAbility,
        PermanentPredicate filter) implements CardEffect {

    /** The grant as the slot and effect that stand for it. */
    public PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(EffectSlot triggeredAbilitySlot,
                                                                  CardEffect triggeredAbility,
                                                                  PermanentPredicate filter) {
        this(LegacySlots.grant(triggeredAbilitySlot, triggeredAbility), filter);
    }

    public PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(
            EffectSlot triggeredAbilitySlot, CardEffect triggeredAbility) {
        this(triggeredAbilitySlot, triggeredAbility, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), filter);
    }

    /** The slot of {@link #grantedAbility}, or {@code null} when there is none. */
    public EffectSlot triggeredAbilitySlot() {
        return grantedAbility == null ? null : LegacySlots.slotOf(grantedAbility);
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect triggeredAbility() {
        return grantedAbility == null ? null : LegacySlots.effectsOf(grantedAbility).getFirst();
    }
}
