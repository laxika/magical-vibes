package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

/**
 * Registers a global triggered ability through the end of the controller's next turn, or, with
 * {@code untilWatchedPlayersNextTurn}, through the end of the next turn of the player the resolving ability names ("until
 * the end of defending player's next turn", Nuka-Nuke Launcher), the registration's watched player.
 */
public record RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect(Ability grantedAbility,
                                                                      TargetFilter targetFilter,
                                                                      boolean untilWatchedPlayersNextTurn)
        implements CardEffect {

    /** Through the end of the controller's next turn ("until the end of your next turn"). */
    public RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect(Ability grantedAbility, TargetFilter targetFilter) {
        this(grantedAbility, targetFilter, false);
    }

    /** The grant as the slot and effect that stand for it. */
    public RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect(EffectSlot slot,
                                                                  CardEffect triggeredEffect,
                                                                  TargetFilter targetFilter) {
        this(LegacySlots.grant(slot, triggeredEffect), targetFilter);
    }

    /**
     * "Until the end of [that player]'s next turn, [ability]": through the end of the next turn of the player the
     * resolving ability names (its target, or the defending player of an attack trigger).
     */
    public static RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect untilWatchedPlayersNextTurn(
            Ability grantedAbility) {
        return new RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect(grantedAbility, null, true);
    }

    /** The slot of {@link #grantedAbility}, or {@code null} when there is none. */
    public EffectSlot slot() {
        return grantedAbility == null ? null : LegacySlots.slotOf(grantedAbility);
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect triggeredEffect() {
        return grantedAbility == null ? null : LegacySlots.effectsOf(grantedAbility).getFirst();
    }
}
