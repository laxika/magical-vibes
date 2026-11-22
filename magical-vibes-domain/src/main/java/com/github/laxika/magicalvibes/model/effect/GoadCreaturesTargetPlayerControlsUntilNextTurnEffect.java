package com.github.laxika.magicalvibes.model.effect;


import java.util.Objects;

/**
 * Goads each creature the player {@code player} names controls as the effect resolves, until the controller's next turn
 * (CR 701.15a): target player by default, or "that player" of the trigger event ({@link Subjects#triggeringPlayer()},
 * Marisi, Breaker of the Coil, Komainu Battle Armor), who isn't targeted.
 *
 * @param player whose creatures are goaded
 */
public record GoadCreaturesTargetPlayerControlsUntilNextTurnEffect(PlayerSubject player) implements CardEffect {

    public GoadCreaturesTargetPlayerControlsUntilNextTurnEffect {
        Objects.requireNonNull(player, "player");
    }

    public GoadCreaturesTargetPlayerControlsUntilNextTurnEffect() {
        this(Subjects.targetPlayer());
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
