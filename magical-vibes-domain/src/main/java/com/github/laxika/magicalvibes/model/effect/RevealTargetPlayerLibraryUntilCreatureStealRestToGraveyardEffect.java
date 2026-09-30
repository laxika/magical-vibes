package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player reveals cards from the top of their library until they reveal a creature card. That
 * player puts all noncreature cards revealed this way into their graveyard, then the caster puts the
 * creature card onto the battlefield under their control (the card keeps its original owner). If the
 * library is exhausted without revealing a creature, every revealed card is put into the graveyard.
 * <p>
 * Used by Telemin Performance and Curse of Unbinding. The latter uses the enchanted-player
 * variant, which reads the player baked into the upkeep trigger instead of declaring a new target.
 */
public record RevealTargetPlayerLibraryUntilCreatureStealRestToGraveyardEffect(boolean enchantedPlayer)
        implements CardEffect {

    public RevealTargetPlayerLibraryUntilCreatureStealRestToGraveyardEffect() {
        this(false);
    }

    public static RevealTargetPlayerLibraryUntilCreatureStealRestToGraveyardEffect forEnchantedPlayer() {
        return new RevealTargetPlayerLibraryUntilCreatureStealRestToGraveyardEffect(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return enchantedPlayer ? TargetSpec.NONE : TargetSpec.harmful(TargetPredicates.player());
    }
}
