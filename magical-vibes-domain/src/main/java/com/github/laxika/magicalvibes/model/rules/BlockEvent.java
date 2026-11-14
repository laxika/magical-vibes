package com.github.laxika.magicalvibes.model.rules;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Creatures blocked or became blocked, or attackers turned out unblocked: the events "whenever this creature blocks",
 * "becomes blocked", "attacks and isn't blocked" and the other block abilities trigger on (CR 509.3, 509.1h). One
 * declaration of blockers, or one block an effect makes, is one occurrence (CR 603.2c).
 *
 * @param kind               how the blocks came to be, which decides which block abilities the occurrence is
 * @param attackingPlayerId  the controller of the attacking creatures
 * @param defendingPlayerId  the controller of the blocking creatures
 * @param blocks             each blocking creature and the attacker it blocks, in declaration order
 * @param blockedAttackerIds the attackers that became blocked, in declaration order (a band's other members with them,
 *                           CR 702.22h)
 * @param unblockedAttackerIds the attackers no creature blocks, after a declaration
 * @param newlyBlocked       whether the attackers weren't blocked before ({@link Kind#ENTERED_BLOCKING})
 * @param blockerWasBlocking whether the blocker an effect made block was already blocking ({@link Kind#EFFECT})
 */
public record BlockEvent(Kind kind, UUID attackingPlayerId, UUID defendingPlayerId, List<Block> blocks,
                         List<UUID> blockedAttackerIds, List<UUID> unblockedAttackerIds, boolean newlyBlocked,
                         boolean blockerWasBlocking) implements RulesEvent {

    public BlockEvent {
        Objects.requireNonNull(kind, "kind");
        blocks = List.copyOf(blocks);
        blockedAttackerIds = List.copyOf(blockedAttackerIds);
        unblockedAttackerIds = List.copyOf(unblockedAttackerIds);
    }

    @Override
    public BlockEvent deepCopy() {
        return this;
    }

    /** A blocking creature and the attacking creature it blocks. */
    public record Block(UUID blockerId, UUID attackerId) {
    }

    /** How the blocks came to be. */
    public enum Kind {
        /** The defending player declared blockers (CR 509.1): every block ability, then the unblocked attackers'. */
        DECLARED,
        /** No creature could block: every attacker is unblocked. */
        NONE_DECLARED,
        /** An effect made a creature block an attacker. */
        EFFECT,
        /**
         * A creature entered the battlefield blocking an attacker: it didn't block (CR 509.3a), but the attacker became
         * blocked by it (CR 509.3c, 509.3d).
         */
        ENTERED_BLOCKING,
        /** An effect made an attacker blocked with no creature blocking it (CR 509.1h). */
        WITHOUT_BLOCKERS
    }
}
