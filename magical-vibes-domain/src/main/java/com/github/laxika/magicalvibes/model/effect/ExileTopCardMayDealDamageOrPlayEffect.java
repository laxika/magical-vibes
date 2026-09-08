package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Exiles the controller's top library card and offers to deal damage to a target creature equal
 * to that card's mana value. If declined, the controller may play the exiled card until end of
 * turn.
 *
 * @param exiledCardId the card exiled by the resolved effect, or {@code null} on the card definition
 * @param targetCreatureId the creature chosen by the spell
 */
public record ExileTopCardMayDealDamageOrPlayEffect(UUID exiledCardId, UUID targetCreatureId)
        implements CardEffect {

    public ExileTopCardMayDealDamageOrPlayEffect() {
        this(null, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
