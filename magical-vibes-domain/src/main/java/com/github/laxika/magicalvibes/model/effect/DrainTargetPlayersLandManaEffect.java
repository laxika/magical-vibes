package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "[Player] activates a mana ability of each land they control, then loses all unspent mana": the player
 * {@code player} names activates one mana ability of each of their untapped lands (choosing among several), then
 * loses all unspent mana. With {@code delayedColorless} unset the resolving object's controller adds the mana lost
 * this way (Drain Power, {@code targetPlayer()}); set, that controller adds that much {C} at the beginning of their
 * next main phase this turn (Pygmy Hippo, {@code defendingPlayer()}).
 */
public record DrainTargetPlayersLandManaEffect(PlayerSubject player, boolean delayedColorless) implements CardEffect {

    public DrainTargetPlayersLandManaEffect {
        Objects.requireNonNull(player, "player");
    }

    /** Drain Power: target player's lands, the mana lost added to your pool now. */
    public DrainTargetPlayersLandManaEffect() {
        this(Subjects.targetPlayer(), false);
    }

    /** "You add an amount of {C} equal to the amount of mana that player lost this way" at your next main phase. */
    public static DrainTargetPlayersLandManaEffect forDelayedColorless(PlayerSubject player) {
        return new DrainTargetPlayersLandManaEffect(player, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
