package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals a random card from the controller's hand for the targeted permanent and records that
 * card's mana value on the resolving stack entry for a following effect to use.
 *
 * <p>Bind this effect to the same creature target group as the following damage effects so every
 * step reads the same target from the stack entry.
 */
public record RevealRandomCardFromTargetPermanentControllerHandRecordManaValueEffect()
        implements CardEffect {
}
