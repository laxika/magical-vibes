package com.github.laxika.magicalvibes.model.effect;

/**
 * "Whenever a creature becomes blocked by a creature with lesser power, destroy the blocking
 * creature" ({@code BLOCKER}) / "Whenever a creature blocks a creature with lesser power, destroy the
 * attacking creature" ({@code ATTACKER}). Registered in {@code ON_ANY_CREATURE_BECOMES_BLOCKED}; the
 * power comparison is a trigger condition, checked once when blockers are declared, so later power
 * changes don't stop the destruction. Used by No Quarter.
 */
public record DestroyWeakerBlockParticipantEffect(BlockParticipant participant)
        implements BlockPairConditionalEffect, RemovalEffect {

    @Override
    public BlockParticipant actsOn() {
        return participant;
    }

    @Override
    public boolean firesForPair(int attackerPower, int blockerPower) {
        return participant == BlockParticipant.BLOCKER
                ? blockerPower < attackerPower
                : attackerPower < blockerPower;
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
