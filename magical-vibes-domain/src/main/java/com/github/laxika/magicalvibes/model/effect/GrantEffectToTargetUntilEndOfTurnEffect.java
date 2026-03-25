package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/**
 * When resolved, grants the targeted creature a temporary triggered ability until end of turn.
 * The granted effect is stored on the permanent via
 * {@link com.github.laxika.magicalvibes.model.Permanent#addTemporaryTriggeredEffect(EffectSlot, CardEffect)}
 * and cleared at the cleanup step by {@link com.github.laxika.magicalvibes.model.Permanent#resetModifiers()}.
 *
 * <p>Example: Verdant Rebirth grants {@link EffectSlot#ON_DEATH} +
 * {@link ReturnSourceCardFromGraveyardToOwnerHandEffect} until end of turn.
 *
 * @param slot           the trigger slot to grant (e.g. {@link EffectSlot#ON_DEATH})
 * @param grantedEffect  the effect to fire when the trigger condition is met
 */
public record GrantEffectToTargetUntilEndOfTurnEffect(
        EffectSlot slot,
        CardEffect grantedEffect
) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
