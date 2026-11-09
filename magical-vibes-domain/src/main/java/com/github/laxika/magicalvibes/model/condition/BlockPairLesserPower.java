package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.effect.BlockParticipant;

import java.util.Objects;

/**
 * "Becomes blocked by a creature with lesser power" ({@link BlockParticipant#BLOCKER}) / "blocks a creature with lesser
 * power" ({@link BlockParticipant#ATTACKER}), No Quarter: the event condition of a "whenever a creature becomes blocked
 * by a creature" ability, on the pair the event was about. The attacking creature is the condition context's
 * triggering permanent and the blocking creature its target; {@code lesser}'s power is less than the other's, as the
 * block is declared (CR 509.3f), so a later power change doesn't undo the ability.
 *
 * @param lesser the side of the pair with the lesser power
 */
public record BlockPairLesserPower(BlockParticipant lesser) implements Condition {

    public BlockPairLesserPower {
        Objects.requireNonNull(lesser, "lesser");
    }

    @Override
    public String conditionName() {
        return lesser == BlockParticipant.BLOCKER
                ? "blocked by a creature with lesser power" : "blocks a creature with lesser power";
    }

    @Override
    public String conditionNotMetReason() {
        return lesser == BlockParticipant.BLOCKER
                ? "the blocking creature doesn't have lesser power" : "the blocked creature doesn't have lesser power";
    }
}
