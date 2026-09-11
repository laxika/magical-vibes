package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Aura death trigger that returns the enchanted creature to its owner's hand, then offers a mana
 * payment to return the Aura to its owner's hand.
 *
 * <p>The follow-up payment is created only when the creature card was actually returned. The
 * dying creature's card ID is bound when the enchanted-permanent death trigger is collected.</p>
 */
public record ReturnEnchantedCreatureToOwnerHandThenMayPayEffect(
        UUID dyingCreatureCardId,
        String manaCost
) implements CardEffect {

    /**
     * Card-definition constructor; the dying creature's card ID is not known yet.
     */
    public ReturnEnchantedCreatureToOwnerHandThenMayPayEffect(String manaCost) {
        this(null, manaCost);
    }
}
