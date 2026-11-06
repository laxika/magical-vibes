package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates {@code amount} copies of a sacrificed permanent: the permanent sacrificed to pay the activated ability's
 * cost, or, for a sacrifice trigger, the stack entry's triggering permanent as it last existed on the battlefield
 * (CR 603.10a), whose copies gain haste and are sacrificed at the next end step unless their controller pays.
 */
public record CreateTokenCopyOfSacrificedPermanentEffect(int amount) implements CardEffect {

    public CreateTokenCopyOfSacrificedPermanentEffect() {
        this(1);
    }
}
