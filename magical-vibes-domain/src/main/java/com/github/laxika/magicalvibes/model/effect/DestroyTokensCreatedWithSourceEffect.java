package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys every token still on the battlefield that was created with the source permanent —
 * "destroy all tokens created with this enchantment. They can't be regenerated" (Tombstone
 * Stairwell). The tokens are looked up in {@code GameData.sourceCreatedTokens}, which is populated
 * by the paired creation effect (an {@link EachPlayerCreatesTokenEffect} with
 * {@code recordAsCreatedWithSource}), so the tokens are found regardless of who controls them now.
 *
 * <p>The source is the stack entry's source permanent, which a "when this leaves the battlefield" ability's entry
 * keeps after the source is gone.
 *
 * @param cannotBeRegenerated whether the destroyed tokens can be regenerated
 */
public record DestroyTokensCreatedWithSourceEffect(boolean cannotBeRegenerated) implements CardEffect {
}
