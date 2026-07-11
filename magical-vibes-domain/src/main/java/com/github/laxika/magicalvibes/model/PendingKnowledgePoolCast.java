package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Knowledge Pool: tracks which Knowledge Pool permanent is currently resolving a
 * {@code KNOWLEDGE_POOL_CAST_CHOICE} (used to look up the exiled card pool when the
 * player answers, and for prompt replay on reconnect).
 *
 * @param sourcePermanentId the Knowledge Pool permanent whose cast choice is active
 */
public record PendingKnowledgePoolCast(UUID sourcePermanentId) implements PendingInteraction {
}
