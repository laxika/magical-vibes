package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * The effect's controller looks at the hand of the player {@code player} names: the target player by default; "its
 * controller's hand" after "counter target spell" ({@code controllerOf(targetSpell())}, whose spell target the counter
 * effect declares; Lay Bare).
 *
 * <p>As an {@code Triggers.dealsCombatDamageToPlayer()} ability it needs the damaged player bound as the
 * stack entry's target ("look at that player's hand" — Walker of Secret Ways).
 *
 * @param player whose hand is looked at
 */
public record LookAtHandEffect(PlayerSubject player) implements CardEffect {

    public LookAtHandEffect {
        Objects.requireNonNull(player, "player");
    }

    /** The controller looks at the target player's hand. */
    public LookAtHandEffect() {
        this(Subjects.targetPlayer());
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
