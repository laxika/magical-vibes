package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Resolution-time step a token-creating handler splices right after itself when its tokens are put onto the
 * battlefield attacking: once the creation has finished (after any pause for the replacement order prompt or
 * Jinnie Fay's choice, CR 616.1), every creature token the entry created from {@code createdFromIndex} of
 * {@code StackEntry.createdPermanentIds} on, added tokens such as Queen Allenal's Soldier included, is attacking,
 * and {@code controllerId} chooses which defending player, planeswalker or battle each one attacks (CR 508.4),
 * unless {@code attackTargetId} is set, in which case every one of them attacks that player or permanent instead.
 * Tokens are only attacking while {@code controllerId} is the active player in a combat phase.
 *
 * @param controllerId       the player who created the tokens and chooses their attack targets
 * @param createdFromIndex   the size of the entry's created permanents before the creation
 * @param sacrificeAtEndStep whether every token of the creation is sacrificed at the beginning of the next end step
 * @param attackTargetId     the player or permanent every created token attacks, or {@code null} to let
 *                           {@code controllerId} choose
 * @param enteredAttackingIds the permanents an effect put onto the battlefield attacking while another prompt was
 *                            open (an exiled card returned attacking whose entry asked a copy choice): those of them
 *                            still attacking get their attack targets chosen instead of the created tokens, or
 *                            {@code null} for the created tokens
 */
public record ChooseAttackTargetsForCreatedTokensEffect(UUID controllerId, int createdFromIndex,
                                                        boolean sacrificeAtEndStep, UUID attackTargetId,
                                                        List<UUID> enteredAttackingIds)
        implements CardEffect {

    public ChooseAttackTargetsForCreatedTokensEffect(UUID controllerId, int createdFromIndex,
                                                     boolean sacrificeAtEndStep, UUID attackTargetId) {
        this(controllerId, createdFromIndex, sacrificeAtEndStep, attackTargetId, null);
    }

    public ChooseAttackTargetsForCreatedTokensEffect {
        enteredAttackingIds = enteredAttackingIds == null ? null : List.copyOf(enteredAttackingIds);
    }

    /** The attack-target choice of {@code permanentIds}, put onto the battlefield attacking by an effect. */
    public static ChooseAttackTargetsForCreatedTokensEffect forEnteredAttacking(UUID controllerId,
                                                                                List<UUID> permanentIds) {
        return new ChooseAttackTargetsForCreatedTokensEffect(controllerId, 0, false, null, permanentIds);
    }
}
