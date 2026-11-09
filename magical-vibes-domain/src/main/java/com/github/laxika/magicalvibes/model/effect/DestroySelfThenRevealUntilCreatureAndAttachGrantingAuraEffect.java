package com.github.laxika.magicalvibes.model.effect;

/**
 * "Destroy this creature. Reveal cards from the top of your library until you reveal a creature card. Put that card
 * onto the battlefield and attach [this Aura] to it, then put all other cards revealed this way on the bottom of your
 * library in a random order." The ability an Aura grants the creature it enchants (Shifting Shadow's "enchanted
 * creature has '…'", a {@link GrantTriggeredAbilityEffect}): the creature is the ability's source and its controller
 * controls it and reveals, and the Aura is the ability's granting permanent, read from the stack entry
 * ({@code StackEntry.requireGrantingPermanentId()}). A creature that has left is not destroyed, and an Aura that has
 * left is not attached; the cards are revealed either way.
 */
public record DestroySelfThenRevealUntilCreatureAndAttachGrantingAuraEffect() implements CardEffect {
}
