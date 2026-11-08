package com.github.laxika.magicalvibes.model.effect;

/**
 * Death trigger for auras: return this aura from its owner's graveyard to the battlefield attached to a creature
 * that shares a creature type with the creature that died. Used by Reins of the Vinesteed inside its own
 * {@link MayEffect} ("you may return this card …").
 *
 * <p>"The creature that died" is the stack entry's triggering card, the card as it was put into the graveyard
 * ({@code StackEntry.getTriggeringCards()}), which the enchanted-death collector records; its creature types are read
 * from it as the ability resolves, so a token that ceased to exist still has them.</p>
 */
public record ReturnSourceAuraToSharedTypeCreatureOnDeathEffect() implements CardEffect {
}
