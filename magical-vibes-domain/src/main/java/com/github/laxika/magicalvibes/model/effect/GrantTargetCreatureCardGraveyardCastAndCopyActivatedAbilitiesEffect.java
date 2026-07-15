package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants permission to cast a targeted creature card from a graveyard this turn.
 * When that specific card is cast this way, the source permanent gains all
 * activated abilities of that card until end of turn.
 */
public record GrantTargetCreatureCardGraveyardCastAndCopyActivatedAbilitiesEffect() implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.ANY_GRAVEYARD_CARD); }
}
