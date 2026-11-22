package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Until your next turn, your opponents can't cast spells with the chosen name", "until your next turn, spells with
 * that name can't be cast and lands with that name can't be played": a rule about the card name chosen earlier in the
 * resolution, which the floating effect it makes carries as {@code cardName}. It ends as the controller's next turn
 * starts.
 *
 * @param players  who can't cast the spells; nobody here is targeted
 * @param landsToo whether lands with that name can't be played either; only a rule about every player says so
 * @param cardName the name, bound as the step resolves; {@code null} on the card
 */
public record CantPlayChosenNameUntilNextTurnEffect(PlayerSubject players, boolean landsToo, String cardName)
        implements CardEffect {

    public CantPlayChosenNameUntilNextTurnEffect {
        Objects.requireNonNull(players, "players");
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("Whom the rule is about is no target: " + players);
        }
        if (landsToo && !players.equals(Subjects.eachPlayer())) {
            throw new IllegalArgumentException("Lands can't be played by any player: " + players);
        }
    }

    /** "Your opponents can't cast spells with the chosen name." */
    public static CantPlayChosenNameUntilNextTurnEffect opponentsCantCast() {
        return new CantPlayChosenNameUntilNextTurnEffect(Subjects.eachOpponent(), false, null);
    }

    /** "Spells with that name can't be cast and lands with that name can't be played." */
    public static CantPlayChosenNameUntilNextTurnEffect nobodyCanPlay() {
        return new CantPlayChosenNameUntilNextTurnEffect(Subjects.eachPlayer(), true, null);
    }

    /** This rule about {@code name}. */
    public CantPlayChosenNameUntilNextTurnEffect named(String name) {
        return new CantPlayChosenNameUntilNextTurnEffect(players, landsToo, name);
    }
}
