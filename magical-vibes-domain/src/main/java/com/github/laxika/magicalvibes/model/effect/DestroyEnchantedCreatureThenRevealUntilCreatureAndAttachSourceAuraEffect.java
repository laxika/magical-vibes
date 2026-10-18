package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys a creature, then reveals cards from that creature's controller's library until a
 * creature card is found. The creature enters the battlefield and the source Aura attaches to that
 * exact permanent; the other revealed cards go on the bottom in a random order.
 *
 * <p>When {@code grantedToCreature} is set the ability is one the Aura gives the creature
 * (Shifting Shadow: "Enchanted creature has '... destroy this creature ...'"): the creature is the
 * ability's source and its controller controls it, so the creature destroyed is the source itself
 * even if the Aura has since moved. Otherwise the Aura's current host is destroyed.</p>
 *
 * @param grantedToCreature whether the triggered ability belongs to the enchanted creature
 */
public record DestroyEnchantedCreatureThenRevealUntilCreatureAndAttachSourceAuraEffect(
        boolean grantedToCreature) implements CardEffect {

    public DestroyEnchantedCreatureThenRevealUntilCreatureAndAttachSourceAuraEffect() {
        this(false);
    }
}
