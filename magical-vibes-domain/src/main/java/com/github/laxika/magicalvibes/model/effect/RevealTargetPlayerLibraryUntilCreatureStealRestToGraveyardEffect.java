package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player reveals cards from the top of their library until they reveal a creature card. That
 * player puts all noncreature cards revealed this way into their graveyard, then the caster puts the
 * creature card onto the battlefield under their control (the card keeps its original owner). If the
 * library is exhausted without revealing a creature, every revealed card is put into the graveyard.
 * <p>
 * Used by Telemin Performance. Targets a player.
 */
public record RevealTargetPlayerLibraryUntilCreatureStealRestToGraveyardEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
