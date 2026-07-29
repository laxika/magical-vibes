package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Destroys every token still on the battlefield that was created with the source permanent —
 * "destroy all tokens created with this enchantment. They can't be regenerated" (Tombstone
 * Stairwell). The tokens are looked up in {@code GameData.sourceCreatedTokens}, which is populated
 * by the paired creation effect (an {@link EachPlayerCreatesTokenEffect} with
 * {@code recordAsCreatedWithSource}), so the tokens are found regardless of who controls them now.
 *
 * <p>Works on {@code END_STEP_TRIGGERED} (the stack entry carries the source permanent id) and on
 * {@code ON_SELF_LEAVES_BATTLEFIELD}, where the source is already gone and the collector bakes its
 * id into {@code sourcePermanentId} instead.
 *
 * @param cannotBeRegenerated       whether the destroyed tokens can be regenerated
 * @param sourcePermanentId         source permanent id baked in by a leaves-the-battlefield
 *                                  collector; null means "read it off the stack entry"
 */
public record DestroyTokensCreatedWithSourceEffect(boolean cannotBeRegenerated, UUID sourcePermanentId)
        implements CardEffect {

    /** Card-facing shape; the source permanent is read off the resolving stack entry. */
    public DestroyTokensCreatedWithSourceEffect(boolean cannotBeRegenerated) {
        this(cannotBeRegenerated, null);
    }
}
