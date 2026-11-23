package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;

import java.util.Objects;

/**
 * Registers a global triggered ability for a stated duration: "this turn, whenever …" ({@code UNTIL_END_OF_TURN}) or
 * "until your next turn, whenever …" ({@code UNTIL_YOUR_NEXT_TURN}, Vraska the Unseen, Don't Move). This is used for
 * effects that watch an event across all players' permanents and therefore cannot be stored on one permanent; the
 * ability triggers every time its event occurs within the duration (CR 603.7b).
 *
 * @param grantedAbility the delayed triggered ability
 * @param duration       how long it lasts: until end of turn, or until the controller's next turn begins
 */
public record RegisterGlobalTriggeredAbilityEffect(Ability grantedAbility, Duration duration)
        implements CardEffect {

    public RegisterGlobalTriggeredAbilityEffect {
        Objects.requireNonNull(duration, "duration");
        if (!Duration.UNTIL_END_OF_TURN.equals(duration) && !Duration.UNTIL_YOUR_NEXT_TURN.equals(duration)) {
            throw new IllegalArgumentException("A global triggered ability lasts until end of turn or until your next "
                    + "turn: " + duration);
        }
    }

    /** {@code grantedAbility} until end of turn. */
    public RegisterGlobalTriggeredAbilityEffect(Ability grantedAbility) {
        this(grantedAbility, Duration.UNTIL_END_OF_TURN);
    }

    /** "Until your next turn, [grantedAbility]": it ends as its controller's next turn begins. */
    public static RegisterGlobalTriggeredAbilityEffect untilYourNextTurn(Ability grantedAbility) {
        return new RegisterGlobalTriggeredAbilityEffect(grantedAbility, Duration.UNTIL_YOUR_NEXT_TURN);
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect triggeredEffect() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
