package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the permanent enchanted by the source Aura, then reveals cards from that permanent's
 * controller's library until a creature card is found. The creature enters the battlefield and
 * the source Aura attaches to that exact permanent; the other revealed cards go on the bottom in
 * a random order.
 */
public record DestroyEnchantedCreatureThenRevealUntilCreatureAndAttachSourceAuraEffect()
        implements CardEffect {
}
