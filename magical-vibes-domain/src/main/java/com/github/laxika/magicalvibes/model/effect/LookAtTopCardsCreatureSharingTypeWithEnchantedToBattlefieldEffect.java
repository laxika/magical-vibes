package com.github.laxika.magicalvibes.model.effect;

/**
 * Looks at the top N cards of the controller's library. The player may put a creature card
 * that shares a creature type with the enchanted creature (the permanent the source aura
 * is attached to) onto the battlefield. Remaining cards go to the bottom of the library
 * in any order.
 *
 * <p>Used by auras like Call to the Kindred. The source aura's {@code attachedTo} is used
 * to determine the enchanted creature at resolution time.
 *
 * @param count number of cards to look at from the top of the library
 */
public record LookAtTopCardsCreatureSharingTypeWithEnchantedToBattlefieldEffect(
        int count
) implements CardEffect {
}
