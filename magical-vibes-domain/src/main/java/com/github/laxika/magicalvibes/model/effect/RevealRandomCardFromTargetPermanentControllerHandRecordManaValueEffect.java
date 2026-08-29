package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals a random card from the controller's hand for the targeted permanent and records that
 * card's mana value on the resolving stack entry for a following effect to use.
 *
 * <p>This effect is intentionally unbound: the regular damage effects on the same spell own the
 * creature target, while this effect reads that shared target from the stack entry.
 */
public record RevealRandomCardFromTargetPermanentControllerHandRecordManaValueEffect()
        implements CardEffect {
}
