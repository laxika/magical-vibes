package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveal the top {@code count} cards of your library, then put those cards on the bottom of
 * your library in any order. If a card with the same name as the source was revealed this way,
 * the source deals {@code damage} to any target.
 *
 * <p>The spell always chooses an "any target" as it is cast; if no copy is revealed, no damage
 * is dealt to that target (Stomping Slabs — MOR 107).
 *
 * @param count  the number of cards to reveal from the top of the library
 * @param damage the damage dealt to the target when a copy of the source is revealed
 */
public record RevealTopCardsBottomThenDamageIfCopyRevealedEffect(int count, int damage) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER_OR_PERMANENT);
    }
}
