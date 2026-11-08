package com.github.laxika.magicalvibes.model.effect;

/**
 * Possibility Storm's ability, on a {@code Triggers.playerCastsSpell().castFrom(Zone.HAND)} ability: the caster exiles
 * the triggering spell, then exiles cards from the top of their library until one shares a card type with it, may cast
 * that card without paying its mana cost, and finally puts every card exiled with the enchantment on the bottom of their
 * library in a random order.
 * <p>
 * It reads the trigger event from the stack entry as it resolves: "it" is the triggering spell, exiled only while it is
 * still on the stack; once it has left (another Possibility Storm exiled it first), its card types are those of the
 * entry's triggering spell snapshot, the spell as it last existed on the stack (CR 608.2h). "That player" is the caster
 * (the entry's non-targeting {@code targetId}), and the cards exiled with the enchantment are those of the entry's
 * source permanent, so the ability still works once the enchantment has left the battlefield.
 */
public record PossibilityStormExileAndCastEffect() implements CardEffect {
}
