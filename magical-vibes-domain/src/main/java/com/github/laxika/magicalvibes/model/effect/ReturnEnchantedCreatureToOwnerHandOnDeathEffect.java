package com.github.laxika.magicalvibes.model.effect;

/**
 * Death trigger for auras: when the enchanted creature dies, return that card from its owner's graveyard to its
 * owner's hand. Used by Demonic Vigor and the Zendikons.
 *
 * <p>"That card" is the stack entry's triggering card ({@code StackEntry.getTriggeringCards()}), the card the enchanted permanent put into the graveyard,
 * with the graveyard incarnation the enchanted-death collector recorded for it (CR 400.7): a card that left the
 * graveyard and came back is a new object and stays where it is.</p>
 *
 * @param followUpManaCost optional mana cost offered after the creature is returned (Puppet Master)
 * @param followUpPrompt prompt for the optional follow-up payment
 */
public record ReturnEnchantedCreatureToOwnerHandOnDeathEffect(
        String followUpManaCost,
        String followUpPrompt
) implements CardEffect {

    /** The ordinary unconditional return. */
    public ReturnEnchantedCreatureToOwnerHandOnDeathEffect() {
        this(null, null);
    }
}
