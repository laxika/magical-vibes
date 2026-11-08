package com.github.laxika.magicalvibes.model.condition;

import java.util.UUID;

/**
 * True when the player the context names is {@code playerId}: the targeted player, or the player dealt damage as a
 * damage-to-a-player trigger's collector judges it. As a damage trigger's event condition it is "whenever this deals
 * damage to [that player]" (Hellish Rebuke's "the player who cast Hellish Rebuke"). A {@code null} {@code playerId} is
 * a placeholder the granting effect fills in with its controller as it resolves ({@link #withPlayer}); it matches no
 * player.
 */
public record TargetPlayerIs(UUID playerId) implements Condition {

    /** This condition naming {@code newPlayerId}. */
    public TargetPlayerIs withPlayer(UUID newPlayerId) {
        return new TargetPlayerIs(newPlayerId);
    }

    @Override
    public String conditionName() {
        return "that player is the named player";
    }

    @Override
    public String conditionNotMetReason() {
        return "that player is not the named player";
    }
}
