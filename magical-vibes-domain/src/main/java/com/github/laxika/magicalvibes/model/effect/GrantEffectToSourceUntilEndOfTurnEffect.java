package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/**
 * When resolved, grants the source permanent itself a temporary triggered ability until end of
 * turn (e.g. Cruel Deceiver's "this creature gains '…' until end of turn"). Non-targeting — the
 * recipient is the resolving stack entry's source permanent, so nothing is chosen or validated.
 *
 * <p>The granted effect is stored via
 * {@link com.github.laxika.magicalvibes.model.Permanent#addTemporaryTriggeredEffect(EffectSlot, CardEffect)}
 * and cleared at cleanup by {@link com.github.laxika.magicalvibes.model.Permanent#resetModifiers()}.
 *
 * @param slot          the trigger slot to grant
 * @param grantedEffect the effect to fire when the trigger condition is met
 */
public record GrantEffectToSourceUntilEndOfTurnEffect(
        EffectSlot slot,
        CardEffect grantedEffect
) implements CardEffect {
}
