package com.github.laxika.magicalvibes.model.effect;

/**
 * Doubles the life total of the one player {@code player} names: the controller of the resolving spell or ability by
 * default, target player (Beacon of Immortality) or "its controller" (Celestial Mantle:
 * {@code controllerOf(triggering())}, the creature that dealt the damage, whatever the Aura enchants now).
 *
 * @param player whose life total is doubled; only {@link Subjects#targetPlayer()} declares a target
 */
public record DoubleControllerLifeEffect(PlayerSubject player) implements CardEffect, DoublingEffect {

    public DoubleControllerLifeEffect {
        if (!(player instanceof PlayerSubject.You || player instanceof PlayerSubject.TargetPlayer
                || player instanceof PlayerSubject.ControllerOf)) {
            throw new IllegalArgumentException("One player's life total is doubled: " + player);
        }
    }

    public DoubleControllerLifeEffect() {
        this(Subjects.you());
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
