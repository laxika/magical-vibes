package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Resolution effect for Knowledge Pool's triggered ability.
 * On resolution: exile the original spell from the stack into the KP pool,
 * then the casting player may cast a nonland card from among other cards
 * exiled with KP without paying its mana cost.
 *
 * <p>The trigger is controlled by the KP controller (CR 603.3a), but the
 * resolution involves "that player" (the caster) making choices, so
 * {@code castingPlayerId} tracks who cast the original spell.</p>
 */
public record KnowledgePoolExileAndCastEffect(
        UUID originalSpellCardId,
        UUID knowledgePoolPermanentId,
        UUID castingPlayerId
) implements CardEffect {
}
