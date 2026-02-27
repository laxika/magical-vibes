package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a token that's a copy of the card imprinted on the source permanent.
 *
 * @param grantHaste     if true, the token gains haste (e.g. Mimic Vat)
 * @param exileAtEndStep if true, the token is exiled at the beginning of the next end step (e.g. Mimic Vat)
 */
public record CreateTokenCopyOfImprintedCardEffect(boolean grantHaste, boolean exileAtEndStep) implements CardEffect {

    /**
     * Backward-compatible constructor for Mimic Vat: grants haste and exiles at end step.
     */
    public CreateTokenCopyOfImprintedCardEffect() {
        this(true, true);
    }
}
